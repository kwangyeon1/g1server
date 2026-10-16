#!/usr/bin/env python3
"""Dependency-free, local context harness. No model calls or implicit daemon runs."""
from __future__ import annotations

import argparse
import contextlib
import io
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import uuid
from datetime import datetime, timezone
from types import SimpleNamespace
import xml.etree.ElementTree as ET

INSTALL = Path(__file__).resolve().parent


def now():
    return datetime.now(timezone.utc).isoformat()


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read_json(path, default=None):
    return json.loads(path.read_text(encoding="utf-8")) if path.exists() else default


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, tmp = tempfile.mkstemp(dir=path.parent, prefix=".write-", suffix=".tmp")
    try:
        with os.fdopen(fd, "w", encoding="utf-8", newline="\n") as stream:
            json.dump(value, stream, ensure_ascii=False, indent=2)
            stream.write("\n")
        os.replace(tmp, path)
    finally:
        if os.path.exists(tmp):
            os.unlink(tmp)


def emit(value):
    print(json.dumps(value, ensure_ascii=False, indent=2))


def quiet(function, *args):
    with contextlib.redirect_stdout(io.StringIO()):
        return function(*args)


def mcp_payload(path):
    payload = read_json(path)
    if isinstance(payload, dict) and "content" in payload:
        if payload.get("isError"):
            raise ValueError("MCP returned an error")
        blocks = [json.loads(c["text"]) for c in payload["content"] if c.get("type") == "text"]
        payload = next((b for b in blocks if isinstance(b, dict) and "success" in b), {})
    if not isinstance(payload, dict) or payload.get("success") is not True:
        raise ValueError("Expected original successful mssql-pantos response")
    return payload


def safe_path(root, value):
    path = (root / value).resolve()
    if not path.is_relative_to(root.resolve()):
        raise ValueError("Path must remain inside the project")
    return path


def strip_comments(text):
    # Retain strings and line boundaries; used only as a navigation aid, not a Java parser.
    pattern = r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/'
    return re.sub(pattern, lambda m: re.sub(r"[^\n]", " ", m[0])
                  if m[0].startswith(("//", "/*")) else m[0], text)


class Harness:
    def __init__(self, root, install=INSTALL):
        self.root = Path(root).resolve()
        self.install = Path(install).resolve()
        self.runtime = self.install / ".runtime"
        self.config = read_json(self.install / "config.json")
        self.record_task = None
        if not self.root.is_dir():
            raise ValueError("Project root must be an existing directory")

    def sources(self):
        if self.config.get("source_mode") == "portable":
            from project_profile import scan_sources
            return scan_sources(self.root, self.install, self.config)
        found = {}
        for pattern in self.config["source_globs"]:
            for path in self.root.glob(pattern):
                if any(part in self.config.get('exclude_dirs', []) for part in path.relative_to(self.root).parts[:-1]):
                    continue
                if path.is_file() and path.resolve().is_relative_to(self.root) and not path.is_symlink():
                    found[path.relative_to(self.root).as_posix()] = digest(path)
        return dict(sorted(found.items()))

    def inventory(self):
        hashes = self.sources()
        schedules = []
        for name in hashes:
            if not name.endswith(".java"):
                continue
            text = strip_comments((self.root / name).read_text(encoding="utf-8", errors="replace"))
            for number, line in enumerate(text.splitlines(), 1):
                if "@Scheduled(" in line:
                    schedules.append({"path": name, "line": number, "annotation": line.strip()})
        build_path = self.root / "build.gradle"
        build = build_path.read_text(encoding="utf-8") if build_path.is_file() else ""
        return {"captured_at": now(), "source_hashes": hashes,
                "build_evidence": {"java": re.findall(r"sourceCompatibility\s*=\s*['\"]?([\d.]+)", build),
                                   "spring_boot": re.findall(r"org.springframework.boot['\"]\s+version\s+['\"]([^'\"]+)", build)},
                "scheduled_annotations": schedules,
                "limitations": "Static navigation only; not call-graph, runtime configuration or DB verification."}

    def refresh(self):
        data = self.inventory()
        write_json(self.install / "knowledge/generated/source-map.json", data)
        baseline = read_json(self.install / "knowledge/source-baseline.json", {})
        previous, current = baseline.get("source_hashes", {}), data["source_hashes"]
        changes = [{"path": p, "change": "added" if p not in previous else
                    "deleted" if p not in current else "modified"}
                   for p in sorted(previous.keys() | current.keys()) if previous.get(p) != current.get(p)]
        report = {"at": now(), "status": "REVIEW_REQUIRED" if changes or not baseline else "CURRENT",
                  "changes": changes, "review_targets": self.config["review_targets"],
                  "note": "CURRENT compares hashes only; semantic documentation correctness requires review."}
        write_json(self.runtime / "doc-drift.json", report)
        self.generate_knowledge(data, report)
        emit({"status": report["status"], "changed_files": len(changes), "tracked_files": len(current),
              "report": "install/.runtime/doc-drift.json"})
        return 2 if report["status"] == "REVIEW_REQUIRED" else 0

    def accept_review(self, note):
        if not note.strip():
            raise ValueError("Provide a non-empty review note")
        generated = read_json(self.install / "knowledge/generated/source-map.json")
        if not generated or generated["source_hashes"] != self.sources():
            raise ValueError("Run refresh, review/update knowledge, then accept-review before more source changes")
        write_json(self.install / "knowledge/source-baseline.json",
                   {"reviewed_at": now(), "review_note": note, "source_hashes": generated["source_hashes"]})
        return self.refresh()

    def doctor(self):
        tools = {k: shutil.which(k) for k in ("git", "rg", "java", "sqlcmd")}
        git_status = "unavailable"
        if tools["git"]:
            result = subprocess.run([tools["git"], "-C", str(self.root), "rev-parse", "--is-inside-work-tree"],
                                    capture_output=True, text=True, timeout=15)
            git_status = "available" if result.returncode == 0 and result.stdout.strip() == "true" else "unavailable; file hashes still work"
        report = {"at": now(), "python": sys.version.split()[0], "platform": sys.platform,
                  "project_root": str(self.root), "executables": tools, "git": git_status,
                  "search_engine": "Git HEAD BM25 locate; local SQLite; no embeddings", "gradle_wrapper": (self.root / "gradlew.bat").exists(),
                  "application_not_run": True, "database_not_connected": True,
                  "note": "Core requires Python 3.11+. Java/Gradle and sqlcmd are needed only for their execution commands."}
        write_json(self.runtime / "doctor.json", report)
        emit(report)
        return 0 if sys.version_info >= (3, 11) else 1

    def setup(self, execute, skip_agent_guidance=False):
        target = self.install / ".venv"
        if not execute:
            emit({"status": "PLAN", "actions": ["Create isolated Python venv", "Install root AGENTS.md/CLAUDE.md managed blocks",
                  "Generate current project/test facts and review queue", "Build document index", "Verify and record installation"],
                  "destination": str(target), "packages_to_download": []})
            return 0
        if not skip_agent_guidance:
            self.install_guidance(plan=True)
        if not target.exists():
            subprocess.run([sys.executable, "-m", "venv", "--without-pip", str(target)], check=True)
        interpreter = target / ("Scripts/python.exe" if os.name == "nt" else "bin/python")
        if not interpreter.exists():
            raise ValueError("Existing .venv is incomplete or belongs to another OS; do not share venvs")
        subprocess.run([str(interpreter), "-c", "import sys; assert sys.version_info >= (3,11)"], check=True)
        for name in ("tasks", "runs", "locate"):
            (self.runtime / name).mkdir(parents=True, exist_ok=True)
        if not skip_agent_guidance:
            self.install_guidance()
        quiet(self.refresh)
        quiet(self.index)
        report = {"status": "INSTALLED_LOCAL_ONLY" if skip_agent_guidance else "INSTALLED", "at": now(), "version": 2, "python": str(interpreter),
                  "packages_installed": [], "root_guidance": [] if skip_agent_guidance else ["AGENTS.md", "CLAUDE.md"],
                  "source_files": len(self.sources()), "db_connected_by_installer": False}
        write_json(self.runtime / "installation.json", report)
        validation = subprocess.run([str(interpreter), str(self.install / "harness.py"), "--project-root", str(self.root), "verify-install"],
                                    capture_output=True, text=True, encoding="utf-8", timeout=60)
        if validation.returncode not in (0, 2):
            raise ValueError("Installation verification failed: " + validation.stdout[-2000:] + validation.stderr[-2000:])
        report["verification"] = json.loads(validation.stdout)["status"]
        write_json(self.runtime / "installation.json", report)
        emit(report)
        return validation.returncode

    def install_guidance(self, plan=False):
        marker = self.config.get("guidance_marker", "daemon-harness")
        start, end = f"<!-- {marker}:start -->", f"<!-- {marker}:end -->"
        edits = []
        for name, template in (("AGENTS.md", "agents-root.md"), ("CLAUDE.md", "agents-root.md")):
            path = safe_path(self.root, name)
            if (self.root / name).is_symlink():
                raise ValueError("Root guidance must not be a symlink")
            old = path.read_text(encoding="utf-8") if path.exists() else ""
            body = (self.install / "templates" / template).read_text(encoding="utf-8").strip()
            map_path = self.install / 'knowledge/project-map.md'
            body = body.replace('{{PROJECT_MAP}}', map_path.read_text(encoding='utf-8').strip() if map_path.exists() else '')
            block = start + "\n" + body + "\n" + end
            if old.count(start) != old.count(end) or old.count(start) > 1:
                raise ValueError(f"Repair malformed managed markers in {name} before installing")
            if start in old:
                left, right = old.index(start), old.index(end)
                if left > right:
                    raise ValueError(f"Reversed managed markers in {name}")
                new = old[:left] + block + old[right + len(end):]
            else:
                new = old + ("\n\n" if old and not old.endswith("\n\n") else "") + block + "\n"
            edits.append((path, old, new))
        if plan:
            return
        for path, old, new in edits:
            if old == new:
                continue
            if old:
                backup = self.runtime / "install-backups" / (path.name + "." + hashlib.sha256(old.encode()).hexdigest()[:16])
                backup.parent.mkdir(parents=True, exist_ok=True)
                if not backup.exists():
                    backup.write_text(old, encoding="utf-8")
            path.write_text(new, encoding="utf-8", newline="\n")

    def generate_knowledge(self, inventory, drift):
        hashes = inventory["source_hashes"]
        fingerprint = hashlib.sha256(json.dumps(hashes, sort_keys=True).encode()).hexdigest()
        test_files = [p for p in hashes if p.startswith("src/test/")]
        main_files = [p for p in hashes if p.startswith("src/main/") and p.endswith(".java")]
        mapper_files = [p for p in hashes if p.endswith(".xml")]
        generated = self.install / "knowledge/generated"
        generated.mkdir(parents=True, exist_ok=True)
        facts = ["# 현재 프로젝트 구조 — 자동 갱신", "", f"소스 지문: `{fingerprint}`",
                 f"주 Java {len(main_files)}개 / 테스트 파일 {len(test_files)}개 / mapper XML {len(mapper_files)}개.",
                 f"빌드에서 읽은 버전: {json.dumps(inventory['build_evidence'], ensure_ascii=False)}", "",
                 "정적 탐색 결과이며 업무 규칙이나 현재 DB 스키마를 추론하지 않는다.", "", "## 활성 스케줄 위치", ""]
        facts += [f"- `{s['path']}:{s['line']}` {s['annotation']}" for s in inventory["scheduled_annotations"]]
        facts += ["", "## Mapper 원문", ""] + [f"- `{p}`" for p in mapper_files]
        if self.config.get("source_mode") == "portable":
            from project_profile import detect_project, is_test
            test_files = [p for p in hashes if is_test(p)]
            types = detect_project(self.root)
            facts = ["# 현재 프로젝트 구조 — 자동 갱신", "", f"소스 지문: `{fingerprint}`",
                     f"추적 파일 {len(hashes)}개 / 테스트 후보 {len(test_files)}개.",
                     "감지된 프로젝트: " + ", ".join(types),
                     "정적 파일 탐색이며 빌드·테스트·배포 성공을 뜻하지 않는다.", "",
                     "## 현재 원문", ""] + [f"- `{p}`" for p in hashes]
        (generated / "project-facts.md").write_text("\n".join(facts) + "\n", encoding="utf-8")
        tests = ["# 현재 테스트 지식 — 자동 갱신", "", f"소스 지문: `{fingerprint}`",
                 "기존 테스트의 존재는 검증 근거가 아니다. 실행 결과도 당시 소스에 대한 근거다.", "",
                 "## 발견한 테스트 원문 (품질 미판정)", ""] + [f"- `{p}`" for p in test_files]
        tests += ["", "## 검토하여 등록한 클래스", ""]
        tests += [f"- `{p}`" for p in self.config["validated_test_classes"]] or ["- 없음"]
        tests += ["", "## 최근 실행 근거 (최대 20개)", ""]
        runs = sorted((self.runtime / "runs").glob("*/result.json"), reverse=True)[:20]
        for path in runs:
            record = read_json(path)
            same = record.get("source_hashes") == hashes
            tests.append(f"- `{path.relative_to(self.root).as_posix() if path.is_relative_to(self.root) else path.relative_to(self.install).as_posix()}`: "
                         f"{record.get('status', 'UNKNOWN')}; 현재 소스 일치={same}; {record.get('scope', '기록 범위 확인 필요')}")
        if not runs:
            tests.append("- 실행 기록 없음. 업무 동작은 미검증.")
        (generated / "test-facts.md").write_text("\n".join(tests) + "\n", encoding="utf-8")
        review = ["# 업무 설명 검토 목록 — 자동 갱신", "", f"상태: {drift['status']}",
                  "자동 사실 문서는 최신 원문에서 생성된다. 업무 설명은 아래 변경과 관련된 부분을 에이전트가 검토한다.",
                  "검토 전에는 기존 설명이 현재 동작과 일치한다고 가정하지 않는다.", ""]
        review += [f"- {c['change']}: `{c['path']}`" for c in drift["changes"]]
        review += ["", "검토 대상:"] + [f"- `{p}`" for p in self.config["review_targets"]]
        (generated / "review-queue.md").write_text("\n".join(review) + "\n", encoding="utf-8")

    def sync(self):
        code = quiet(self.refresh)
        quiet(self.index)
        return code

    def session(self, args):
        path = self.task_path(args.task)
        if args.action == "start":
            quiet(self.task, SimpleNamespace(action="start", task=args.task, goal=args.goal, next=args.next))
        elif not path.exists():
            raise ValueError("Unknown task; start it first")
        elif args.action == "resume":
            quiet(self.task, SimpleNamespace(action="note", task=args.task, kind="progress", text="작업 재개", next=None, status="IN_PROGRESS"))
        code = self.sync()
        state = read_json(path)
        if args.action in ("checkpoint", "finish"):
            blocked = bool(code or (args.action == "finish" and not state["evidence"]))
            status = "WAITING" if blocked else ("DONE" if args.action == "finish" else "IN_PROGRESS")
            quiet(self.task, SimpleNamespace(action="note", task=args.task, kind="progress", text=args.summary,
                                            next=args.next if args.action == "checkpoint" else ("문서 검토 또는 검증 근거 보완" if blocked else "없음"), status=status))
            code = 2 if blocked else 0
        context = self.context_packet(args.task)
        emit({"status": read_json(path)["status"], "knowledge_status": read_json(self.runtime / "doc-drift.json")["status"],
              "context_file": str(context), "context": context.read_text(encoding="utf-8"),
              "note": "DONE은 기록 완료이며 업무 테스트 PASS가 아니다."})
        return code

    def context_packet(self, task):
        state = read_json(self.task_path(task))
        drift = read_json(self.runtime / "doc-drift.json", {})
        handoff = self.handoff(task)
        text = [f"# 작업 컨텍스트: {task}", f"목표: {state['goal']}", f"상태: {state['status']}", f"다음: {state['next']}",
                f"지식 검토: {drift.get('status', 'UNKNOWN')}", "", "## 결정·제약·가정 (최근 12개; 전체는 handoff)"]
        notes = [e for e in state["events"] if e["kind"] in ("decision", "constraint", "assumption")][-12:]
        text += [f"- {e['kind']}: {e['text'][:500]}" for e in notes]
        recent = [e for e in state["events"] if e["kind"] in ("progress", "verification")][-3:]
        text += ["", "## 최근 진행·검증"] + [f"- {e['text'][:600]}" for e in recent]
        text += ["", "## 변경 원문 (최대 20개)"] + [f"- {c['change']}: `{c['path']}`" for c in drift.get("changes", [])[:20]]
        text += ["", "## 최근 근거 (최대 5개)"]
        text += [f"- {e['result']}: {e['description'][:300]} → `{e['snapshot']}`" for e in state["evidence"][-5:]]
        text += ["", "## 필요할 때 읽을 원문", "- install/knowledge/generated/project-facts.md",
                 "- install/knowledge/generated/test-facts.md", "- install/knowledge/generated/review-queue.md",
                 f"- install/.runtime/tasks/{task}/handoff.md (전체 제약과 결정)",
                 "- install/knowledge/database.md (대상 프로젝트의 조회 도구로 현재 상태 확인; 저장 결과는 당시 스냅샷)",
                 "- install/run.ps1 search-code 또는 search-docs (Linux: sh install/run.sh)"]
        path = handoff.parent / "context.md"
        path.write_text("\n".join(text) + "\n", encoding="utf-8")
        return path

    def attach_run(self, run, result):
        if self.record_task:
            quiet(self.evidence, SimpleNamespace(task=self.record_task, file=(run / "result.json").relative_to(self.root).as_posix(),
                  result="PASS" if result["status"] == "PASS" else "FAIL" if result["status"] in ("FAIL", "ERROR", "TIMEOUT", "NO_TESTS") else "INFO",
                  description=f"{result['status']}: {result.get('scope', '실행 범위 확인')}"))
            self.context_packet(self.record_task)

    def db_request(self, args):
        if not self.task_path(args.task).exists():
            raise ValueError("Start a session first")
        for name in (args.schema, args.table):
            if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", name):
                raise ValueError("Exact schema/table identifiers required")
        if not args.database.strip():
            raise ValueError("Explicit database required")
        request_id = "db-" + uuid.uuid4().hex[:12]
        folder = self.runtime / "db" / request_id
        folder.mkdir(parents=True)
        query = (self.install / "sql/inspect-table-mcp.sql").read_text(encoding="utf-8")
        query = query.replace("__SCHEMA__", args.schema).replace("__TABLE__", args.table).replace("__DATABASE__", args.database.replace("'", "''"))
        (folder / "query.sql").write_text(query, encoding="utf-8")
        record = {"id": request_id, "created_at": now(), "task": args.task, "database": args.database,
                  "schema": args.schema, "table": args.table, "status": "PENDING", "query_sha256": digest(folder / "query.sql"),
                  "tool": "mssql-pantos.execute_sql", "kind": "schema", "source_hashes": self.sources()}
        write_json(folder / "request.json", record)
        emit({"request": request_id, "query_file": str(folder / "query.sql"), "sql": query,
              "next": "Call the existing mssql-pantos MCP with this exact SQL; save its JSON response; db-capture --request ID --result-file PATH"})
        return 0

    def db_capture(self, request_id, result_file):
        if not re.fullmatch(r"db-[a-f0-9]{12}", request_id):
            raise ValueError("Invalid DB request ID")
        folder = self.runtime / "db" / request_id
        record = read_json(folder / "request.json")
        if not record or record["status"] != "PENDING":
            raise ValueError("Unknown or already captured request")
        if digest(folder / "query.sql") != record["query_sha256"]:
            raise ValueError("Query changed since request creation")
        source = safe_path(self.root, result_file)
        if source.stat().st_size > 20 * 1024 * 1024:
            raise ValueError("DB response exceeds 20 MiB")
        payload = read_json(source)
        if isinstance(payload, dict) and "content" in payload:
            if payload.get("isError"):
                raise ValueError("MCP returned an error")
            blocks = [json.loads(c["text"]) for c in payload["content"] if c.get("type") == "text"]
            payload = next((b for b in blocks if isinstance(b, dict) and "success" in b), {})
        if not isinstance(payload, dict) or payload.get("success") is not True:
            raise ValueError("Expected successful mssql-pantos JSON response; do not fabricate a success wrapper")
        statements = payload.get("data", {}).get("statements", [])
        rows = [row for s in statements for row in s.get("rows", [])]
        identity = next((row for row in rows if row.get("database_name") == record["database"] and
                         row.get("schema_name") == record["schema"] and row.get("table_name") == record["table"] and row.get("captured_at_utc")), None)
        if identity is None or not any("column_name" in row and "type_name" in row for row in rows):
            raise ValueError("Response lacks matching DB/table/time and column metadata")
        shutil.copyfile(source, folder / "response.json")
        record.update(status="SCHEMA_CAPTURED", captured_at=identity["captured_at_utc"], imported_at=now(),
                      response_sha256=digest(folder / "response.json"), provenance="MCP response imported by agent; not cryptographically attested")
        record["query_file"] = (folder / "query.sql").relative_to(self.root).as_posix()
        record["response_file"] = (folder / "response.json").relative_to(self.root).as_posix()
        write_json(folder / "request.json", record)
        quiet(self.evidence, SimpleNamespace(task=record["task"], file=(folder / "request.json").relative_to(self.root).as_posix(), result="INFO",
                                            description=f"DB 스키마 {record['database']}.{record['schema']}.{record['table']}; captured_at={record['captured_at']}; 업무 PASS 아님"))
        self.context_packet(record["task"])
        emit({k: v for k, v in record.items() if k != "source_hashes"})
        return 0

    def read_code(self, name, start, lines):
        path = safe_path(self.root, name)
        relative = path.relative_to(self.root).as_posix()
        if relative not in self.sources():
            raise ValueError("Read only a file tracked by config source_globs")
        if self.sensitive_source(relative):
            raise ValueError("Connection configuration is hash-tracked only; inspect locally with values redacted instead of copying it into evidence")
        content = path.read_text(encoding="utf-8", errors="replace").splitlines()
        if start > len(content) or lines > 200:
            raise ValueError("Start must exist and at most 200 lines can be read per call")
        record = {"path": relative, "read_at": now(), "sha256": digest(path), "start_line": start,
                  "lines": [{"line": i + 1, "text": content[i]} for i in range(start - 1, min(len(content), start - 1 + lines))]}
        if self.record_task:
            artifact = self.runtime / "tasks" / self.record_task / "code" / (uuid.uuid4().hex[:12] + ".json")
            write_json(artifact, record)
            quiet(self.evidence, SimpleNamespace(task=self.record_task, file=artifact.relative_to(self.root).as_posix(), result="INFO",
                                                description=f"현재 코드 원문 {relative}:{start}, {len(record['lines'])}줄"))
        emit(record)
        return 0

    def db_capture_objects(self, args):
        if not self.task_path(args.task).exists():
            raise ValueError("Start a session first")
        response_file = safe_path(self.root, args.result_file)
        identity_file = safe_path(self.root, args.identity_file)
        for path in (response_file, identity_file):
            if path.stat().st_size > 20 * 1024 * 1024:
                raise ValueError("MCP response exceeds 20 MiB")
        response, identity = mcp_payload(response_file), mcp_payload(identity_file)
        data = response.get("data", {})
        identity_data = identity.get("data", {})
        rows = identity_data.get("rows", []) + [r for s in identity_data.get("statements", []) for r in s.get("rows", [])]
        if not any(r.get("DB_NAME", r.get("database_name")) == args.database for r in rows):
            raise ValueError("Database does not match the saved connection identity response")
        objects = [o for o in data.get("results", []) if o.get("schema") == args.schema and o.get("name") == args.table]
        if data.get("object_type") != "table" or len(objects) != 1 or not objects[0].get("columns") or data.get("truncated"):
            raise ValueError("Expected complete search_objects response for the exact schema/table")
        folder = self.runtime / "db" / ("db-" + uuid.uuid4().hex[:12])
        folder.mkdir(parents=True)
        shutil.copyfile(response_file, folder / "response.json")
        shutil.copyfile(identity_file, folder / "connection-identity.json")
        record = {"status": "SCHEMA_CAPTURED_PARTIAL", "task": args.task, "database": args.database,
                  "schema": args.schema, "table": args.table, "imported_at": now(),
                  "tool": "mssql-pantos.search_objects", "request_parameters": {"object_type": "table", "schema": args.schema,
                  "pattern": data.get("pattern"), "detail_level": data.get("detail_level")},
                  "columns": len(objects[0]["columns"]), "indexes": len(objects[0].get("indexes", [])),
                  "response_file": (folder / "response.json").relative_to(self.root).as_posix(),
                  "identity_file": (folder / "connection-identity.json").relative_to(self.root).as_posix(),
                  "response_sha256": digest(folder / "response.json"), "identity_sha256": digest(folder / "connection-identity.json"),
                  "scope": "Column/index metadata only. No guarantee of identity/FK/CHECK/trigger completeness. Not business PASS.",
                  "provenance": "Original MCP responses supplied by agent; import time is not DB server capture time; connection identity may be older."}
        write_json(folder / "request.json", record)
        quiet(self.evidence, SimpleNamespace(task=args.task, file=(folder / "request.json").relative_to(self.root).as_posix(), result="INFO",
                                            description=f"현재 MCP 테이블 메타데이터 {args.database}.{args.schema}.{args.table}; 열/인덱스 범위"))
        self.context_packet(args.task)
        emit(record)
        return 0

    def verify_install(self):
        interpreter = self.install / ".venv" / ("Scripts/python.exe" if os.name == "nt" else "bin/python")
        checks = {"python_311": sys.version_info >= (3, 11), "venv_exists": interpreter.exists(),
                  "using_installed_venv": Path(sys.prefix).resolve() == (self.install / ".venv").resolve(),
                  "installation_receipt": (self.runtime / "installation.json").exists()}
        receipt = read_json(self.runtime / "installation.json", {})
        requested_guidance = bool(receipt.get("root_guidance"))
        guidance = {}
        for name in ("AGENTS.md", "CLAUDE.md"):
            path = self.root / name
            marker = self.config.get("guidance_marker", "daemon-harness")
            guidance[name] = path.exists() and f"<!-- {marker}:start -->" in path.read_text(encoding="utf-8")
            if requested_guidance:
                checks[name] = guidance[name]
        for name in ("project-facts.md", "test-facts.md", "review-queue.md"):
            checks[name] = (self.install / "knowledge/generated" / name).exists()
        from navigation import Locator
        navigation = Locator(self.root, self.install).status()
        checks["navigation_initialized"] = navigation.get('status') in ('CURRENT', 'WAITING_GIT_COMMIT', 'DISABLED')
        baseline = read_json(self.install / "knowledge/source-baseline.json", {})
        reviewed = baseline.get("source_hashes") == self.sources()
        status = "READY" if all(checks.values()) and reviewed else "READY_REVIEW_REQUIRED" if all(checks.values()) else "INCOMPLETE"
        if status == "READY" and not requested_guidance:
            status = "READY_LOCAL_ONLY"
        report = {"status": status, "at": now(), "checks": checks, "source_review_current": reviewed,
                  "root_agent_guidance": guidance, "navigation": navigation,
                  "automatic_project_guidance_installed": requested_guidance and all(guidance.values())}
        write_json(self.runtime / "installation-check.json", report)
        emit(report)
        return 0 if status in ("READY", "READY_LOCAL_ONLY") else 2 if status == "READY_REVIEW_REQUIRED" else 1

    def search_code(self, query, limit):
        results = []
        for name in self.sources():
            for number, line in enumerate((self.root / name).read_text(encoding="utf-8", errors="replace").splitlines(), 1):
                if query.casefold() in line.casefold():
                    results.append({"path": name, "line": number, "text": "[sensitive configuration match; values omitted]" if self.sensitive_source(name) else line[:500]})
                    if len(results) >= limit:
                        emit({"results": results, "limit_reached": True})
                        return 0
        emit({"results": results, "limit_reached": False})
        return 0

    def sensitive_source(self, name):
        from navigation import path_matches
        return path_matches(name, self.config.get("sensitive_source_globs", []))

    def task_path(self, task):
        if not re.fullmatch(r"[a-zA-Z0-9][a-zA-Z0-9_-]{0,63}", task):
            raise ValueError("Task ID: 1-64 letters, numbers, underscores or hyphens")
        return self.runtime / "tasks" / task / "state.json"

    def task(self, args):
        path = self.task_path(args.task)
        state = read_json(path)
        if args.action == "start":
            if state:
                raise ValueError("Task already exists; use task note/show")
            state = {"id": args.task, "goal": args.goal, "created_at": now(), "status": "IN_PROGRESS",
                     "next": args.next, "events": [], "evidence": []}
        elif state is None:
            raise ValueError("Unknown task")
        elif args.action == "note":
            state["events"].append({"at": now(), "kind": args.kind, "text": args.text})
            if args.next is not None:
                state["next"] = args.next
            if args.status:
                state["status"] = args.status
        if args.action != "show":
            state["updated_at"] = now()
            write_json(path, state)
            self.handoff(args.task)
        emit(state)
        return 0

    def handoff(self, task):
        path = self.task_path(task)
        state = read_json(path)
        if not state:
            raise ValueError("Unknown task")
        data = [f"# {task}", "", f"목표: {state['goal']}", f"상태: {state['status']}",
                f"다음 작업: {state['next']}", "", "## 최근 결정·제약·가정", ""]
        # Constraints and decisions remain; verbose logs are referenced, not pasted.
        durable = [e for e in state["events"] if e["kind"] in ("decision", "constraint", "assumption")]
        recent = [e for e in state["events"] if e["kind"] not in ("decision", "constraint", "assumption")][-10:]
        data.extend(f"- {e['at']} [{e['kind']}] {e['text']}" for e in durable + recent)
        data.extend(["", "## 검증 근거", ""])
        data.extend(f"- [{e['result']}] {e['description']} — `{e['snapshot']}` (SHA256 {e['sha256']})" for e in state["evidence"])
        data.extend(["", "상태·결론은 작성자의 기록이다. 실행 결과와 현재 원문을 확인할 것.",
                     "상세 이력: 같은 폴더의 state.json. 메모만으로 DB 처리 완료를 판단하지 않는다.", ""])
        out = path.parent / "handoff.md"
        out.write_text("\n".join(data), encoding="utf-8", newline="\n")
        return out

    def evidence(self, args):
        path = self.task_path(args.task)
        state = read_json(path)
        if not state:
            raise ValueError("Start task first")
        source = safe_path(self.root, args.file)
        if not source.is_file():
            raise ValueError("Evidence file not found")
        if source.stat().st_size > 20 * 1024 * 1024:
            raise ValueError("Evidence exceeds 20 MiB; retain targeted output")
        if source.name.startswith(".env") or source.name.startswith("application"):
            raise ValueError("Do not attach application/credential configuration")
        dest = path.parent / "evidence" / (uuid.uuid4().hex[:12] + "-" + source.name)
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, dest)
        state["evidence"].append({"at": now(), "source": source.relative_to(self.root).as_posix(),
                                  "snapshot": dest.relative_to(self.install).as_posix(),
                                  "sha256": digest(dest), "result": args.result,
                                  "description": args.description, "asserted_by": "operator"})
        write_json(path, state)
        self.handoff(args.task)
        emit(state["evidence"][-1])
        return 0

    def index(self):
        """Compatibility alias for the single commit-based locate index."""
        from navigation import Locator
        if not self.config.get('navigation', {}).get('locate_enabled', True):
            report = {'status': 'DISABLED'}
        else:
            try:
                report = Locator(self.root, self.install).update()
            except (ValueError, FileNotFoundError) as exc:
                report = {'status': 'WAITING_GIT_COMMIT', 'note': str(exc)}
        emit(report)
        return 0

    def search_docs(self, query, limit):
        """Legacy command alias: location metadata only, no separate document search."""
        from navigation import Locator
        try:
            emit(Locator(self.root, self.install).locate(query, limit))
            return 0
        except (ValueError, FileNotFoundError) as exc:
            emit({'status': 'UNAVAILABLE', 'results': [], 'note': str(exc)})
            return 2

    def run_dir(self, kind):
        path = self.runtime / "runs" / (datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ") + "-" + kind + "-" + uuid.uuid4().hex[:8])
        path.mkdir(parents=True)
        return path

    def build_plan(self, online=False):
        if not (self.root / 'build.gradle').exists() and not (self.root / 'build.gradle.kts').exists():
            raise ValueError('check-build/run-tests are Gradle adapters; configure commands and use run-check for this project')
        wrapper = [str(self.root / "gradlew.bat")] if os.name == "nt" else ["sh", str(self.root / "gradlew")]
        return wrapper + ["--no-daemon", "--console=plain"] + ([] if online else ["--offline"]) + ["--rerun-tasks", "--no-build-cache", "compileJava"]

    def check_build(self, execute, timeout, online=False):
        cmd = self.build_plan(online)
        if not execute:
            emit({"status": "PLAN", "argv": cmd, "scope": "compileJava only; existing tests are not validation evidence",
                  "note": "No Gradle execution. --offline still needs an already cached wrapper distribution/dependencies."})
            return 0
        run = self.run_dir("compile")
        result = self.execute(cmd, run, timeout)
        if result["status"] == "COMMAND_OK":
            result["status"] = "BUILD_OK"
        result["scope"] = "Java compilation only; NOT daemon/DB/integration verification"
        write_json(run / "result.json", result)
        self.attach_run(run, result)
        emit(result)
        return 0 if result["status"] == "BUILD_OK" else 1

    def run_tests(self, args):
        if not re.fullmatch(r"[A-Za-z_][\w]*(?:\.[A-Za-z_][\w]*)+", args.test_class):
            raise ValueError("Use a fully qualified test class; wildcards are not accepted")
        if args.test_class not in self.config["validated_test_classes"]:
            raise ValueError("Test is not registered in config.json validated_test_classes. Review or create a meaningful isolated test first.")
        cmd = self.build_plan(args.online)[:-1]
        cmd += ["--init-script", str(self.install / "gradle/harness.init.gradle"),
                "-PharnessTestClass=" + args.test_class, "harnessSelectedTest"]
        if not args.execute:
            emit({"status": "PLAN", "argv": cmd, "scope": args.test_class})
            return 0
        run = self.run_dir("test")
        result_dir = run / "junit"
        cmd.insert(-1, "-PharnessResults=" + str(result_dir))
        result = self.execute(cmd, run, args.timeout)
        counts = parse_junit(result_dir)
        result["junit"] = counts
        if result["status"] == "COMMAND_OK":
            result["status"] = ("FAIL" if counts["failures"] or counts["errors"] or counts["parse_errors"] else
                                "NO_TESTS" if counts["tests"] - counts["skipped"] <= 0 else "PASS")
        result["scope"] = args.test_class + "; not proof of daemon/DB behavior unless explicitly tested"
        write_json(run / "result.json", result)
        self.attach_run(run, result)
        emit(result)
        return 0 if result["status"] == "PASS" else 1

    def run_check(self, name, execute, timeout):
        cmd = self.config.get('commands', {}).get(name)
        if not isinstance(cmd, list) or not cmd or not all(isinstance(p, str) and p for p in cmd):
            raise ValueError('Configure commands.' + name + ' as an argv array in install/config.json first')
        if not execute:
            emit({'status': 'PLAN', 'argv': cmd, 'cwd': str(self.root)})
            return 0
        run = self.run_dir(name)
        result = self.execute(cmd, run, timeout)
        result['scope'] = 'Configured command exit status; inspect logs for actual validation coverage'
        write_json(run / 'result.json', result)
        emit(result)
        return 0 if result['status'] == 'COMMAND_OK' else 1

    def execute(self, cmd, run, timeout, env=None):
        start = now()
        record = {"started_at": start, "argv": cmd, "status": "RUNNING",
                  "source_hashes": self.sources(), "run_directory": str(run)}
        write_json(run / "result.json", record)
        with (run / "stdout.log").open("wb") as stdout, (run / "stderr.log").open("wb") as stderr:
            proc = None
            try:
                proc = subprocess.Popen(cmd, cwd=self.root, env=env, stdout=stdout, stderr=stderr,
                                        start_new_session=os.name != "nt")
                code = proc.wait(timeout=timeout)
                record.update(exit_code=code, status="COMMAND_OK" if code == 0 else "FAIL")
            except subprocess.TimeoutExpired:
                record["status"] = "TIMEOUT"
                try:
                    if os.name == "nt":
                        killed = subprocess.run(["taskkill", "/PID", str(proc.pid), "/T", "/F"],
                                                stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=10)
                        if killed.returncode != 0:
                            # Some Windows sandboxes restrict taskkill; use our process handle.
                            proc.kill()
                            record["cleanup_note"] = "Direct child terminated; descendant cleanup unverified"
                    else:
                        import signal
                        os.killpg(proc.pid, signal.SIGKILL)
                    proc.wait(timeout=5)
                    record["exit_code"] = proc.returncode
                except (OSError, subprocess.SubprocessError) as cleanup_error:
                    record.update(status="TIMEOUT_CLEANUP_FAILED", error=str(cleanup_error))
            except subprocess.SubprocessError as error:
                record.update(status="ERROR", error=str(error))
            except OSError as error:
                record.update(status="ERROR", error=str(error))
        record["finished_at"] = now()
        record["log_hashes"] = {p.name: digest(p) for p in (run / "stdout.log", run / "stderr.log")}
        write_json(run / "result.json", record)
        return record

    def db_inspect(self, args):
        server = os.environ.get("HARNESS_DB_SERVER")
        database = os.environ.get("HARNESS_DB_NAME")
        if not server or not database:
            raise ValueError("Set HARNESS_DB_SERVER and HARNESS_DB_NAME explicitly")
        if args.schema and not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", args.schema):
            raise ValueError("Invalid schema identifier")
        if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", args.table):
            raise ValueError("Use a single exact table identifier, not a pattern")
        template = self.install / "sql/inspect-table.sql"
        query = template.read_text(encoding="utf-8").replace("__SCHEMA__", args.schema).replace("__TABLE__", args.table)
        query = query.replace("__DATABASE__", database.replace("'", "''"))
        if not args.execute:
            emit({"status": "PLAN", "operation": "Read sys catalog only; no business rows or mutation",
                  "database": database, "schema": args.schema, "table": args.table,
                  "sql_template": "install/sql/inspect-table.sql", "auth": args.auth})
            return 0
        sqlcmd = shutil.which("sqlcmd")
        if not sqlcmd:
            raise ValueError("sqlcmd is unavailable; configure the project database tools and see install/knowledge/database.md")
        run = self.run_dir("db-schema")
        script = run / "query.sql"
        script.write_text(query, encoding="utf-8", newline="\n")
        cmd = [sqlcmd, "-S", server, "-d", database, "-b", "-l", "15", "-t", "30", "-W", "-x", "-f", "65001", "-i", str(script)]
        if args.auth == "integrated":
            cmd.append("-E")
        else:
            user = os.environ.get("HARNESS_DB_USER")
            if not user or not os.environ.get("SQLCMDPASSWORD"):
                raise ValueError("SQL auth requires HARNESS_DB_USER and SQLCMDPASSWORD environment variables")
            cmd += ["-U", user]
        result = self.execute(cmd, run, 60)
        result["status"] = "SCHEMA_CAPTURED" if result["status"] == "COMMAND_OK" else result["status"]
        result["scope"] = "Schema metadata at captured time; NOT fixture or business verification"
        result["query_sha256"] = digest(script)
        write_json(run / "result.json", result)
        self.attach_run(run, result)
        emit(result)
        return 0 if result["status"] == "SCHEMA_CAPTURED" else 1


def parse_junit(folder):
    counts = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0, "parse_errors": []}
    for path in sorted(folder.glob("TEST-*.xml")):
        try:
            root = ET.parse(path).getroot()
            if root.tag not in ("testsuite", "testsuites"):
                raise ValueError("Not a JUnit report")
            for case in root.iter("testcase"):
                counts["tests"] += 1
                for tag, key in (("failure", "failures"), ("error", "errors"), ("skipped", "skipped")):
                    if case.find(tag) is not None:
                        counts[key] += 1
        except (ET.ParseError, ValueError) as error:
            counts["parse_errors"].append({"file": path.name, "error": str(error)})
    return counts


def positive(value):
    number = int(value)
    if number < 1 or number > 10000:
        raise argparse.ArgumentTypeError("Expected integer from 1 to 10000")
    return number


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project-root", type=Path, default=INSTALL.parent)
    sub = parser.add_subparsers(dest="command", required=True)
    for name in ("doctor", "refresh", "index-docs", "sync", "verify-install"):
        sub.add_parser(name)
    p = sub.add_parser("setup")
    p.add_argument("--execute", action="store_true")
    p.add_argument("--skip-agent-guidance", action="store_true")
    p = sub.add_parser('install-guidance')
    p.add_argument('--execute', action='store_true')
    p = sub.add_parser("accept-review")
    p.add_argument("--note", required=True)
    p = sub.add_parser('run-check')
    p.add_argument('name', choices=['build', 'test'])
    p.add_argument('--execute', action='store_true')
    p.add_argument('--timeout', type=positive, default=600)
    for name in ("search-code", "search-docs"):
        p = sub.add_parser(name)
        p.add_argument("query")
        p.add_argument("--limit", type=positive, default=10)
    p = sub.add_parser("task")
    tasks = p.add_subparsers(dest="action", required=True)
    p = tasks.add_parser("start")
    p.add_argument("task")
    p.add_argument("--goal", required=True)
    p.add_argument("--next", required=True)
    p = tasks.add_parser("show")
    p.add_argument("task")
    p = tasks.add_parser("note")
    p.add_argument("task")
    p.add_argument("--kind", choices=["decision", "constraint", "assumption", "progress", "verification"], required=True)
    p.add_argument("--text", required=True)
    p.add_argument("--next")
    p.add_argument("--status", choices=["IN_PROGRESS", "WAITING", "DONE"])
    p = sub.add_parser("evidence")
    p.add_argument("task")
    p.add_argument("--file", required=True)
    p.add_argument("--result", choices=["WAITING", "PASS", "FAIL", "INFO"], required=True)
    p.add_argument("--description", required=True)
    p = sub.add_parser("check-build")
    p.add_argument("--task")
    p.add_argument("--execute", action="store_true")
    p.add_argument("--timeout", type=positive, default=600)
    p.add_argument("--online", action="store_true")
    p = sub.add_parser("run-tests")
    p.add_argument("--task")
    p.add_argument("--class", dest="test_class", required=True)
    p.add_argument("--execute", action="store_true")
    p.add_argument("--timeout", type=positive, default=600)
    p.add_argument("--online", action="store_true")
    p = sub.add_parser("db-inspect")
    p.add_argument("--task")
    p.add_argument("--schema", default="dbo")
    p.add_argument("--table", required=True)
    p.add_argument("--auth", choices=["integrated", "sql"], default="integrated")
    p.add_argument("--execute", action="store_true")
    p = sub.add_parser("session")
    sessions = p.add_subparsers(dest="action", required=True)
    for name in ("start", "resume", "checkpoint", "finish"):
        s = sessions.add_parser(name)
        s.add_argument("task")
        if name == "start":
            s.add_argument("--goal", required=True)
            s.add_argument("--next", required=True)
        if name in ("checkpoint", "finish"):
            s.add_argument("--summary", required=True)
        if name == "checkpoint":
            s.add_argument("--next", required=True)
    p = sub.add_parser("db-request")
    p.add_argument("task")
    p.add_argument("--database", required=True)
    p.add_argument("--schema", default="dbo")
    p.add_argument("--table", required=True)
    p = sub.add_parser("db-capture")
    p.add_argument("--request", required=True)
    p.add_argument("--result-file", required=True)
    p = sub.add_parser("read-code")
    p.add_argument("path")
    p.add_argument("--start", type=positive, default=1)
    p.add_argument("--lines", type=positive, default=60)
    p.add_argument("--task")
    p = sub.add_parser("db-capture-objects")
    p.add_argument("task")
    p.add_argument("--database", required=True)
    p.add_argument("--schema", default="dbo")
    p.add_argument("--table", required=True)
    p.add_argument("--identity-file", required=True)
    p.add_argument("--result-file", required=True)
    args = parser.parse_args()
    h = Harness(args.project_root)
    if args.command == 'run-check':
        return h.run_check(args.name, args.execute, args.timeout)
    if args.command in ("check-build", "run-tests", "db-inspect", "read-code") and args.task:
        if not h.task_path(args.task).exists():
            raise ValueError("Start the task before attaching execution results")
        h.record_task = args.task
    if args.command == "read-code":
        return h.read_code(args.path, args.start, args.lines)
    if args.command == "db-capture-objects":
        return h.db_capture_objects(args)
    if args.command == "verify-install":
        return h.verify_install()
    if args.command == "session":
        return h.session(args)
    if args.command == "sync":
        code = h.sync()
        from navigation import Locator
        emit({"status": "REVIEW_REQUIRED" if code else "CURRENT", "index": Locator(h.root, h.install).status(), "generated_knowledge": "UPDATED"})
        return code
    if args.command == "db-request":
        return h.db_request(args)
    if args.command == "db-capture":
        return h.db_capture(args.request, args.result_file)
    if args.command == "doctor":
        return h.doctor()
    if args.command == "setup":
        return h.setup(args.execute, args.skip_agent_guidance)
    if args.command == 'install-guidance':
        h.install_guidance(plan=not args.execute)
        emit({'status': 'UPDATED' if args.execute else 'PLAN', 'files': ['AGENTS.md', 'CLAUDE.md']})
        return 0
    if args.command == "refresh":
        return h.refresh()
    if args.command == "accept-review":
        return h.accept_review(args.note)
    if args.command == "search-code":
        return h.search_code(args.query, args.limit)
    if args.command == "index-docs":
        return h.index()
    if args.command == "search-docs":
        return h.search_docs(args.query, args.limit)
    if args.command == "task":
        return h.task(args)
    if args.command == "evidence":
        return h.evidence(args)
    if args.command == "check-build":
        return h.check_build(args.execute, args.timeout, args.online)
    if args.command == "run-tests":
        return h.run_tests(args)
    if args.command == "db-inspect":
        return h.db_inspect(args)


if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    try:
        raise SystemExit(main())
    except (ValueError, OSError, subprocess.SubprocessError) as error:
        print(json.dumps({"status": "ERROR", "error": str(error)}, ensure_ascii=False), file=sys.stderr)
        raise SystemExit(1)
