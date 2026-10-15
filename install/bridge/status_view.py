"""Local diagnostics only: no model calls, source approval, or business DB access."""
import hashlib
import json
import sqlite3
import time
from contextlib import closing
from datetime import datetime
from pathlib import Path


def load(path, default=None):
    try:
        return json.loads(Path(path).read_text(encoding='utf-8'))
    except (OSError, ValueError):
        return default


def desktop_evidence(bridge):
    proof = load(bridge.runtime / 'desktop-handoff-verification.json', {})
    result = {'verified': False, 'observed_at': proof.get('observed_at')}
    try:
        evidence = Path(proof['evidence_file']).resolve()
        # Inspect only the explicitly recorded session, never credentials or unrelated chats.
        if (not evidence.is_relative_to((Path.home() / '.codex/sessions').resolve())
                or proof['session_id'] not in evidence.name or evidence.suffix != '.jsonl'
                or evidence.stat().st_size > 64 * 1024 * 1024):
            return result
        report = load(bridge.safe(bridge.runtime, proof['new_report']), {})
        if report.get('reviewer') != 'official Hermes CLI':
            return result
        with evidence.open('rb') as stream:
            for line in stream:
                if hashlib.sha256(line.rstrip(b'\r\n')).hexdigest() != proof['evidence_record_sha256']:
                    continue
                record = json.loads(line)
                payload = record.get('payload', {})
                content = payload.get('content', [])
                text = '\n'.join(c.get('text', '') for c in content if isinstance(c, dict))
                result['verified'] = (record.get('type') == 'response_item'
                    and payload.get('role') == 'developer'
                    and '이전 Hermes 검토 요약:' in text and 'report.json' in text)
                break
    except (OSError, ValueError, KeyError, TypeError):
        pass
    return result


def diagnostics(bridge, state):
    actions = []
    checks = {}
    if not bridge.settings.get('enabled', True):
        actions.append('자동 연동이 꺼져 있습니다. install/bridge/config.local.json의 enabled 설정을 확인하세요.')
    if state.get('queue', {}).get('read_error'):
        actions.append('작업 큐를 읽지 못했습니다. SQLite 잠금·파일 손상 여부를 확인하세요.')
    worker = state['worker']
    age = max(0, time.time() - worker.get('at', 0))
    allowance = bridge.settings['hermes_timeout_seconds'] + 120 if worker.get('status') == 'REVIEWING' else 60
    checks['worker'] = ('STOPPED' if not state['worker_running'] else
                        'UNRESPONSIVE' if age > allowance else worker.get('status', 'UNKNOWN'))
    if state['status'] == 'NOT_INSTALLED':
        actions.append('.\\install\\setup-bridge.ps1 -Execute 로 설치하세요.')
    elif not state['worker_running']:
        actions.append('.\\install\\bridge.ps1 start 로 작업자를 시작하세요.')
    elif checks['worker'] == 'UNRESPONSIVE':
        actions.append('작업자 응답이 오래됐습니다. worker.log를 확인하고 stop 후 중지를 확인한 뒤 start 하세요.')
    if not state['hermes_command']:
        actions.append('공식 Hermes 설치를 확인하세요: .\\install\\install-hermes.ps1 -Execute')
    if checks['worker'] == 'WAITING_DISK_SPACE':
        actions.append('프로젝트 디스크 여유 공간을 확보하세요.')
    if checks['worker'] == 'RATE_LIMITED':
        actions.append('시간당 Hermes 호출 제한으로 대기 중입니다. 제한 시간이 지나면 재개됩니다.')

    latest = None
    errors = 0
    if bridge.queue.path.exists():
        try:
            with closing(sqlite3.connect(bridge.queue.path.as_uri() + '?mode=ro', uri=True, timeout=.25)) as db:
                row = db.execute('SELECT report,updated FROM sessions ORDER BY updated DESC LIMIT 1').fetchone()
                errors = db.execute('SELECT COUNT(*) FROM jobs WHERE error IS NOT NULL').fetchone()[0]
            if row:
                report = load(bridge.safe(bridge.runtime, row[0]), {})
                latest = {'path': row[0], 'at': row[1], 'status': report.get('status'),
                          'valid_sources': bridge.valid_report(report),
                          'hermes_response': report.get('reviewer') == 'official Hermes CLI'}
        except (OSError, ValueError, sqlite3.Error):
            actions.append('검토 기록을 읽지 못했습니다. install/.runtime/bridge의 큐·보고서를 확인하세요.')
    checks['latest_review'] = latest
    failed = state.get('queue', {}).get('jobs', {}).get('FAILED', 0)
    checks['jobs_with_errors'] = errors
    if failed or errors or checks['worker'] == 'REVIEW_FAILED':
        actions.append('검토 실패 또는 재시도 기록이 있습니다. worker.log와 reviews의 응답을 확인하세요. 인증 문제면 hermes setup, 해결 후 bridge.ps1 retry를 실행하세요.')
    if latest and not latest['valid_sources']:
        actions.append('이전 검토 이후 원문이 바뀌었습니다. 다음 요청의 새 검토 완료를 확인하세요.')
    if not state['last_hook']:
        actions.append('앱 이벤트 미수신: Codex 앱 설정의 프로젝트 Hooks 신뢰 상태를 확인하고 이 프로젝트에서 요청하세요.')

    baseline = load(bridge.install / 'knowledge/source-baseline.json', {})
    current = bridge.harness.sources()
    previous = baseline.get('source_hashes', {})
    changed = sum(previous.get(p) != current.get(p) for p in previous.keys() | current.keys())
    index = load(bridge.install / '.runtime/search/index.json', {})
    retrieval = ('STALE_KNOWLEDGE' if not baseline or changed else 'NO_INDEX' if not index else
                 'STALE_INDEX' if index.get('manifest') != bridge.harness.documents() else 'READY')
    checks['retrieval'] = {'status': retrieval, 'changed_sources': changed}
    if retrieval == 'STALE_KNOWLEDGE':
        actions.append(f'원문 {changed}개 변경: .\\install\\run.ps1 sync 후 install/knowledge/generated/review-queue.md의 관련 원문·문서를 검토하세요.')
        actions.append('실제 검토를 마친 뒤 .\\install\\run.ps1 accept-review --note "실제 검토 내용", 이어서 .\\install\\run.ps1 sync 를 실행하세요.')
    elif retrieval != 'READY':
        actions.append('.\\install\\run.ps1 index-docs 로 검색 색인을 갱신하세요.')
    proof = desktop_evidence(bridge)
    checks['desktop_handoff'] = proof
    if not proof['verified']:
        actions.append('앱 수신까지는 미검증입니다. 같은 앱 채팅에서 검토 완료 후 다시 요청하여 이전 Hermes 요약의 전달을 확인하세요.')

    blocked = (not bridge.settings.get('enabled', True) or not state['worker_running']
               or not state['hermes_command'] or checks['worker'] in
               ('UNRESPONSIVE', 'WAITING_DISK_SPACE', 'WAITING_HERMES_INSTALL'))
    overall = ('STOPPED' if blocked else 'ATTENTION' if failed or errors or state.get('queue', {}).get('read_error') or retrieval != 'READY'
               or (latest and not latest['valid_sources']) or checks['worker'] == 'REVIEW_FAILED' else
               'UNVERIFIED' if not proof['verified'] or not latest or not latest['hermes_response'] or not state['last_hook'] else
               'PROCESSING' if checks['worker'] in ('REVIEWING', 'RATE_LIMITED') else 'HEALTHY')
    return {'overall': overall, 'enabled': bridge.settings.get('enabled', True),
            'checks': checks, 'actions': actions}


def local_time(value):
    if not value:
        return '없음'
    try:
        date = datetime.fromtimestamp(value).astimezone() if isinstance(value, (int, float)) else datetime.fromisoformat(value.replace('Z', '+00:00')).astimezone()
        return date.strftime('%Y-%m-%d %H:%M:%S %z')
    except (ValueError, TypeError):
        return '시각 확인 불가'


def format_status(state):
    health = state['health']
    checks = health['checks']
    labels = {'STOPPED': '동작 중단 / 시작 또는 복구 필요', 'ATTENTION': '부분 동작 / 조치 필요',
              'UNVERIFIED': '실행 중 / 전체 연동은 아직 미검증',
              'PROCESSING': '정상 동작 / 처리 중', 'HEALTHY': '정상 동작'}
    worker_labels = {'IDLE': '대기 중 (새 요청이 없으면 정상)', 'REVIEWING': 'Hermes 검토 중',
                     'STOPPED': '중지됨', 'UNRESPONSIVE': '응답 지연', 'COMPLETED': '검토 완료',
                     'WAITING_HERMES_INSTALL': 'Hermes 설치 대기', 'WAITING_DISK_SPACE': '디스크 공간 부족',
                     'RATE_LIMITED': '호출 제한으로 대기', 'REVIEW_FAILED': '검토 실패'}
    review = checks['latest_review']
    proof = checks['desktop_handoff']
    hook = state.get('last_hook') or {}
    jobs = state.get('queue', {}).get('jobs', {})
    search_labels = {'READY': '준비됨 (정적 최신성 검사 통과)', 'STALE_KNOWLEDGE': '사용 불가: 원문 변경 검토 필요',
                     'NO_INDEX': '사용 불가: 색인 없음', 'STALE_INDEX': '사용 불가: 색인 갱신 필요'}
    lines = [f"하네스 상태: {labels[health['overall']]}",
             f"  자동 연동: {'켜짐' if health['enabled'] else '꺼짐'}",
             f"  작업자: {worker_labels.get(checks['worker'], checks['worker'])} / PID {state['worker'].get('pid', '-')} / 최근 응답 {local_time(state['worker'].get('at'))}",
             f"  Hermes: {'실행 파일 발견' if state['hermes_command'] else '실행 파일 없음'} / {'실제 검토 응답 확인' if review and review['hermes_response'] else '실제 검토 응답 미확인'}",
             f"  최근 훅: {hook.get('event', '미수신')} / {local_time(hook.get('at'))} (핸들러 수신 기록)",
             f"  최근 검토: {local_time(review['at']) if review else '없음'} / {'검토 범위의 원문 해시 유효' if review and review['valid_sources'] else '유효한 원문 근거 미확인'}",
             f"  앱으로 요약 전달: {'실제 앱 컨텍스트 수신 확인' if proof['verified'] else '미검증'} / 확인 시각 {local_time(proof.get('observed_at'))}",
             '    위 전달 결과는 확인된 과거 요청의 근거이며 모든 새 요청의 성공을 보증하지 않습니다.',
             f"  문서 검색: {search_labels[checks['retrieval']['status']]} / 변경 원문 {checks['retrieval']['changed_sources']}개",
             f"  작업 큐: 대기 {jobs.get('PENDING', 0)} / 처리 중 {jobs.get('RUNNING', 0)} / 실패 {jobs.get('FAILED', 0)} / 재시도·오류 기록 {checks['jobs_with_errors']}"]
    if review:
        lines.append('  최근 보고서: install/.runtime/bridge/' + review['path'])
    if health['actions']:
        lines.append('필요한 조치:')
        lines.extend('  - ' + action for action in health['actions'])
    else:
        lines.append('필요한 조치: 없음. 평소처럼 Codex 앱에서 작업하면 됩니다.')
    lines.append('상세 JSON: .\\install\\bridge.ps1 status --json')
    return '\n'.join(lines)
