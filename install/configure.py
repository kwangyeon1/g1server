"""Dependency-free, numbered terminal wizard for project knowledge setup."""
import contextlib
import copy
import difflib
import io
import json
import os
from pathlib import Path
import sys
import tempfile
import uuid

from harness import Harness, quiet

INSTALL = Path(__file__).resolve().parent
DOCS = {
    'project': ('프로젝트 설명', '목적: 주문을 검증하고 배송사에 전달하는 서비스\n진입점: src/order/service.py\n처리 흐름: 요청 수신 → 검증 → 저장'),
    'testing': ('테스트 지식', '명령: python -m unittest discover\n성공 조건: 중복 주문을 두 번 발송하지 않음\n범위: 단위 테스트, 실제 배송사 API는 미검증'),
    'database': ('데이터 저장소', '종류: PostgreSQL\n주요 테이블: orders, order_events\n처리 기준: READY 주문 처리 후 DONE으로 갱신\nDB가 없다면: 데이터베이스 사용 안 함'),
    'decisions': ('구성 결정', '외부 API 성공은 실제 응답으로 확인한다.\n테스트 로그는 install/.runtime/runs에 보존한다.\n인증정보는 문서에 기록하지 않는다.')
}


class Back(Exception):
    pass


class Cancel(Exception):
    pass


def patterns(value, directories=False, required=False):
    values = [p.strip() for p in value.split(',') if p.strip()]
    if required and not values:
        raise ValueError('하나 이상의 소스 패턴이 필요합니다.')
    for p in values:
        if '\\' in p or ':' in p or p.startswith('/') or '..' in p.split('/'):
            raise ValueError('프로젝트 기준 상대 패턴과 /를 사용하세요. 예: src/**/*.py')
        if directories and any(c in p for c in '/*?[]'):
            raise ValueError('제외 폴더는 경로·패턴이 아닌 폴더 이름입니다. 예: logs, tmp')
    return list(dict.fromkeys(values))


def command(value):
    parsed = json.loads(value)
    if not isinstance(parsed, list) or not parsed or not all(isinstance(x, str) and x.strip() for x in parsed):
        raise ValueError('비어 있지 않은 문자열 배열을 입력하세요. 예: ["python", "-m", "pytest"]')
    return parsed


def atomic_text(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, name = tempfile.mkstemp(prefix='.configure-', suffix='.tmp', dir=path.parent)
    try:
        with os.fdopen(fd, 'w', encoding='utf-8', newline='\n') as f:
            f.write(text)
        os.replace(name, path)
    finally:
        if os.path.exists(name):
            os.unlink(name)


class Wizard:
    def __init__(self, install=INSTALL, input_fn=input):
        self.install = Path(install).resolve()
        self.root = self.install.parent
        self.input = input_fn
        self.original = {}
        for name in ['config.json'] + ['knowledge/' + key + '.md' for key in DOCS]:
            path = self.path(name)
            self.original[name] = path.read_text(encoding='utf-8') if path.exists() else None
        if self.original['config.json'] is None:
            raise ValueError('install/config.json이 없습니다. 순정 ZIP의 install 폴더를 먼저 풀어주세요.')
        self.config = json.loads(self.original['config.json'])
        self.docs = {key: self.original['knowledge/' + key + '.md'] or '' for key in DOCS}

    def path(self, name):
        path = self.install / name
        if path.is_symlink() or not path.resolve().is_relative_to(self.install):
            raise ValueError('설치 폴더 밖 또는 심볼릭 링크 파일은 편집할 수 없습니다: ' + name)
        return path

    def ask(self, prompt, preserve=False):
        raw = self.input(prompt)
        text = raw.strip()
        if text == ':back':
            raise Back()
        if text == ':quit':
            raise Cancel()
        return raw if preserve else text

    def choose(self, prompt, allowed, default):
        while True:
            answer = self.ask(prompt) or default
            if answer in allowed:
                return answer
            print('선택 가능한 번호: ' + ', '.join(allowed))

    def configure_scope(self):
        print('\n[1/7] 원문 범위와 선택적 빌드·테스트 명령')
        print('Enter: 현재 값 유지 / 새 값 입력: 목록 전체 교체 / :back: 이전 단계 / :quit: 저장 전 취소')
        print('예: 현재 값이 *.java, *.xml일 때 *.yml만 입력하면 결과는 *.yml만 남습니다.')
        print('추가하려면 전체 목록을 입력하세요. 예: *.java, *.xml, *.yml')
        print('-는 단독 입력할 때만 선택 항목을 비웁니다. -*.yml은 YML 제외 문법이 아닙니다.')
        draft = copy.deepcopy(self.config)
        for key, example in [('source_globs', '*.py, *.ts, *.json 또는 src/**/*.java, *.gradle'),
                             ('exclude_dirs', 'logs, tmp, playwright-report'),
                             ('sensitive_source_globs', '**/application*.yml, application*.yml, **/*secret*')]:
            print('\n' + key + ': ' + ', '.join(draft.get(key, [])))
            print('예시: ' + example)
            if key == 'source_globs':
                print('필수 항목: Enter로 유지하거나 전체 목록을 입력하세요. -로 비울 수 없습니다.')
                print('YML·DatabaseConfig.java의 내용 공개를 막으려면 여기서는 Enter로 유지하고, 아래 sensitive_source_globs에서 설정하세요.')
            elif key == 'exclude_dirs':
                print('폴더 이름만 입력합니다. 예: logs, tmp → 두 폴더를 탐색에서 제외 / -만 입력 → 사용자 지정 제외 목록 비우기')
            else:
                print('내용 공개 제외 예: *.yml, **/*.yml, DatabaseConfig.java, **/DatabaseConfig.java')
                print('기존 민감 패턴도 유지하려면 위 현재 목록과 새 패턴을 모두 입력하세요. -만 입력하면 사용자 지정 민감 목록을 비웁니다.')
            print('소스는 변경 감지 대상, 제외 폴더는 탐색 제외, 민감 패턴은 내용 공개 제외입니다.' if key == 'source_globs' else '')
            while True:
                value = self.ask('새 값(쉼표로 구분): ')
                if not value:
                    break
                try:
                    draft[key] = patterns('' if value == '-' else value, key == 'exclude_dirs', key == 'source_globs')
                    break
                except ValueError as exc:
                    print(str(exc))
        print('\n명령은 저장만 합니다. 여기서는 빌드·테스트를 실행하지 않습니다.')
        print('예시: ["python", "-m", "pytest"] / ["npm.cmd", "run", "build"]')
        print('도구와 인수를 JSON 배열로 입력하세요. Windows의 npm은 npm.cmd를 지정합니다.')
        for key in ('build', 'test'):
            print(key + ' 현재 값: ' + json.dumps(draft.get('commands', {}).get(key, []), ensure_ascii=False))
            while True:
                value = self.ask(key + ' 명령(Enter 유지 / - 등록 해제): ')
                if not value:
                    break
                try:
                    if value == '-':
                        draft.setdefault('commands', {}).pop(key, None)
                    else:
                        draft.setdefault('commands', {})[key] = command(value)
                    break
                except ValueError as exc:
                    print('입력 오류: ' + str(exc))
        self.config = draft

    def edit_doc(self, index, key):
        title, example = DOCS[key]
        print(f'\n[{index}/7] knowledge/{key}.md — {title}')
        print('입력 예시 (실제 코드와 대조해 작성):\n' + example)
        print('인증정보·접속 비밀번호를 입력하지 마세요.')
        print('현재 문서:\n' + (self.docs[key] or '(없음)'))
        choice = self.choose('1 유지 / 2 내용 추가 / 3 전체 다시 작성 [1]: ', ('1', '2', '3'), '1')
        if choice == '1':
            return
        print('여러 줄로 입력하고 마치면 한 줄에 . 을 입력하세요. :back은 이 단계 입력을 취소합니다.')
        lines = []
        while True:
            line = self.ask('> ', preserve=True)
            if line.strip() == '.':
                break
            lines.append(line)
        body = '\n'.join(lines).strip()
        if not body:
            print('입력 내용이 없어 기존 문서를 유지합니다.')
            return
        self.docs[key] = ((self.docs[key].rstrip() + '\n\n') if choice == '2' else '') + body + '\n'

    def proposed(self):
        result = {'config.json': json.dumps(self.config, ensure_ascii=False, indent=2) + '\n'}
        # Preserve exact formatting when the configuration values did not change.
        if self.config == json.loads(self.original['config.json']):
            result['config.json'] = self.original['config.json']
        result.update({'knowledge/' + k + '.md': v for k, v in self.docs.items()})
        return {k: v for k, v in result.items() if v != self.original[k] and (v or self.original[k] is not None)}

    def save(self):
        changes = self.proposed()
        # Detect concurrent edits even to retained documents before syncing/approving.
        for name, old in self.original.items():
            path = self.path(name)
            current = path.read_text(encoding='utf-8') if path.exists() else None
            if current != old:
                raise ValueError('다른 작업에서 파일이 바뀌었습니다. 저장을 중단합니다: ' + name)
        if not changes:
            return None
        backup = self.install / '.runtime/configure' / uuid.uuid4().hex
        for name in changes:
            if self.original[name] is not None:
                atomic_text(backup / name, self.original[name])
        applied = []
        try:
            for name, body in changes.items():
                atomic_text(self.path(name), body)
                applied.append(name)
        except Exception:
            for name in reversed(applied):
                if self.original[name] is None:
                    self.path(name).unlink(missing_ok=True)
                else:
                    atomic_text(self.path(name), self.original[name])
            raise
        print('저장 완료. 수정 전 사본: ' + str(backup))
        return backup

    def review(self, h):
        print('\n[6/7] 동기화 및 원문 검토')
        code = h.sync()
        queue = self.install / 'knowledge/generated/review-queue.md'
        print(queue.read_text(encoding='utf-8'))
        print('검토 기준 등록은 실제 원문·문서 대조 후 선택합니다. 테스트 통과를 뜻하지 않습니다.')
        while True:
            choice = self.choose('1 원문 읽기 / 2 검토 완료 기록 / 3 등록을 미루고 검색 확인 [3]: ', ('1', '2', '3'), '3')
            if choice == '3':
                return code
            if choice == '1':
                name = self.ask('프로젝트 상대 경로 (예: src/order/service.py): ')
                if name not in h.sources() or h.sensitive_source(name):
                    print('현재 추적 대상이 아니거나 민감 파일입니다. 적절한 로컬 편집기에서 확인하세요.')
                    continue
                line = self.ask('시작 줄 [1], 최대 160줄 표시: ') or '1'
                if not line.isdigit() or int(line) < 1:
                    print('1 이상의 줄 번호를 입력하세요.'); continue
                lines = (h.root / name).read_text(encoding='utf-8', errors='replace').splitlines()
                start = int(line) - 1
                print('\n'.join(f'{i + 1}: {s}' for i, s in enumerate(lines[start:start + 160], start)))
                continue
            print('예시: 주문 서비스 처리 조건과 mapper SQL을 확인하고 project/testing/database 설명을 대조함')
            note = self.ask('실제 검토 내용 (공백이면 등록 취소): ')
            if not note:
                continue
            if self.ask('원문·문서를 실제 검토했다면 REVIEW 입력: ') != 'REVIEW':
                print('등록하지 않았습니다.'); continue
            try:
                quiet(h.accept_review, note)
                code = h.sync()
                print('검토 기준 등록 및 색인 갱신 완료.')
                return code
            except ValueError as exc:
                print(str(exc))
                print('검토 중 원문이 바뀌었다면 새 변경을 확인해야 합니다.')
                code = h.sync()
                print(queue.read_text(encoding='utf-8'))

    def search(self, h):
        print('\n[7/7] BM25 검색 확인')
        print('예시: 주문 전송 실패 / 중복 주문 / 데이터베이스 / 프로젝트')
        while True:
            query = self.ask('검색어 [프로젝트]: ') or '프로젝트'
            output = io.StringIO()
            try:
                with contextlib.redirect_stdout(output):
                    code = h.search_docs(query, 3)
                result = json.loads(output.getvalue())
            except ValueError as exc:
                print(str(exc)); continue
            print('검색 상태: ' + result['status'])
            for item in result.get('results', []):
                print(f"\n{item['path']}:{item['start_line']}\n{item['excerpt'][:500]}")
            if result['status'] == 'STALE_KNOWLEDGE':
                print('원문 검토 기준 등록이 필요합니다. TUI의 검토 단계 또는 run.ps1 accept-review를 사용하세요.')
            if self.choose('1 다른 검색어 / 2 종료 [2]: ', ('1', '2'), '2') == '2':
                return code

    def run(self):
        print('=== 프로젝트 준비 TUI ===\n대상: ' + str(self.root))
        print('1 범위·명령 → 2~5 지식 문서 → 미리보기·저장 → 6 검토 → 7 검색')
        print('Enter는 현재 값 유지, :back은 저장 전 이전 단계, :quit/Ctrl+C는 종료입니다.')
        step = 0
        while step < 5:
            try:
                if step == 0:
                    self.configure_scope()
                else:
                    self.edit_doc(step + 1, list(DOCS)[step - 1])
                step += 1
                if step == 5:
                    print('\n=== 저장 전 변경 미리보기 ===')
                    changes = self.proposed()
                    for name, body in changes.items():
                        print(''.join(difflib.unified_diff((self.original[name] or '').splitlines(True),
                                                         body.splitlines(True), fromfile=name, tofile=name)))
                    if not changes:
                        print('설정·지식 문서 변경 없음. 다음 단계에서는 sync를 실행합니다.')
                    choice = self.choose('1 저장하고 sync / 2 처음부터 다시 편집 / 3 취소 [3]: ', ('1', '2', '3'), '3')
                    if choice == '3':
                        raise Cancel()
                    if choice == '2':
                        step = 0
            except Back:
                step = max(0, min(step, 5) - 1)
        self.save()
        print('이후 종료해도 저장된 설정·문서와 이미 수행한 sync/검토 등록은 유지됩니다.')
        h = Harness(self.root, self.install)
        while True:
            try:
                self.review(h)
                return self.search(h)
            except Back:
                print('저장 이후에는 검토 단계로 돌아갑니다.')


def main():
    import argparse
    parser = argparse.ArgumentParser(description=__doc__)
    parser.parse_args()
    if sys.version_info < (3, 11):
        print('Python 3.11 이상이 필요합니다.')
        return 1
    try:
        return Wizard().run()
    except (Cancel, KeyboardInterrupt, EOFError):
        print('\n종료했습니다. 저장 단계 이전 입력은 파일에 반영되지 않습니다.')
        return 0
    except (ValueError, OSError) as exc:
        print('진행 중단: ' + str(exc))
        return 1


if __name__ == '__main__':
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    raise SystemExit(main())
