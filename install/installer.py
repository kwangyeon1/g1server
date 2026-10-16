"""Korean terminal installation wizard. No third-party TUI dependency."""
import copy
import json
import os
from pathlib import Path
import sys
from glossary import entries
from project_profile import detect_project


def choose(input_fn, prompt, options, default):
    while True:
        answer = input_fn(prompt).strip() or default
        if answer in options:
            return answer
        print('선택 가능한 값: ' + ', '.join(options))


def run(install, input_fn=input, execute_fn=None):
    install = Path(install).resolve()
    root = install.parent
    original = (install / 'config.json').read_text(encoding='utf-8')
    config = json.loads(original)
    proposed = copy.deepcopy(config)
    print('\n프로젝트 하네스 설치 — Codex / Claude 공통')
    print('대상: ' + str(root))
    print('감지한 구성: ' + ', '.join(detect_project(root)))
    print('Python 표준 라이브러리만 사용합니다. 외부 패키지·Qdrant·업무 DB를 설치하지 않습니다.')
    print('\n[1/3] 탐색 방식')
    print('1 A: 지도 + 에이전트의 grep/원문 읽기')
    print('2 B: A + 검토된 용어 별칭 훅')
    print('3 C: B + 필요할 때 호출하는 BM25 locate (권장)')
    mode = choose(input_fn, '선택 [3]: ', ('1', '2', '3'), '3')
    proposed.setdefault('navigation', {}).update(glossary_hook_enabled=mode != '1', locate_enabled=mode == '3')
    print('\n[2/3] 검색 제외와 용어 사전')
    print('접속 설정·비밀 파일·생성 코드·로그·도구 내부 상태는 locate 색인에서 제외합니다.')
    print('추가 보호 패턴: ' + ', '.join(config.get('sensitive_source_globs', [])))
    print('추가 제외 패턴 예: private/**, legacy/generated/** (프로젝트 기준 / 사용)')
    while True:
        value = input_fn('이번 설치에 더할 제외 패턴, 쉼표 구분 [추가 없음]: ').strip()
        try:
            from configure import patterns
            added = patterns(value)
            nav = proposed['navigation']
            nav['exclude_globs'] = list(dict.fromkeys(nav.get('exclude_globs', []) + added))
            break
        except ValueError as exc:
            print(str(exc))
    glossary = entries(install)
    approved = sum(item.get('reviewed') is True for item in glossary)
    print(f'용어 사전: 검토 완료 {approved}개 / 검토 대기 {len(glossary)-approved}개')
    print('초안은 자동 승인하지 않습니다. 설치 후 configure.py --glossary로 근거를 검토하세요.')
    print('기존 지식·용어 사전과 사용자 지침은 보존합니다.')
    print('\n[3/3] 변경 미리보기')
    print('생성: install/.venv, install/.runtime (색인·검증 기록·복구 사본)')
    print('병합: AGENTS.md, CLAUDE.md, .codex/hooks.json, .codex/config.toml,')
    print('      .claude/settings.json, .mcp.json')
    print('설정: install/config.json의 navigation 항목')
    print('훅: 요청 시 용어 별칭만. 작업 완료 이벤트 수집·검토 작업자는 없습니다.')
    print('locate: Git HEAD 기준 증분 색인. Git/첫 커밋이 없으면 grep을 계속 사용하세요.')
    print('MCP/훅 신뢰는 각 앱에서 확인해야 합니다.')
    if choose(input_fn, '1 설치 / 2 취소 [2]: ', ('1', '2'), '2') != '1':
        print('취소했습니다. 파일을 변경하지 않았습니다.')
        return 0
    # Validate all merges before making settings or environment changes.
    from agent_setup import plan
    from harness import Harness
    interpreter = install / '.venv' / ('Scripts/python.exe' if os.name == 'nt' else 'bin/python')
    plan(root, install, interpreter)
    Harness(root, install).install_guidance(plan=True)
    if (install / 'config.json').read_text(encoding='utf-8') != original:
        raise ValueError('설치 도중 설정이 변경됐습니다. 다시 실행하세요.')
    if proposed != config:
        import hashlib
        from configure import atomic_text
        backup = install / '.runtime/install-backups' / ('config.' + hashlib.sha256(original.encode()).hexdigest()[:16] + '.json')
        atomic_text(backup, original)
        atomic_text(install / 'config.json', json.dumps(proposed, ensure_ascii=False, indent=2) + '\n')
    if execute_fn is None:
        from bootstrap import execute
        execute_fn = execute
    code = execute_fn()
    print('\n설치 결과: ' + ('완료, 프로젝트 지식 검토 대기' if code == 2 else '완료' if code == 0 else '실패'))
    print('앱을 다시 열고 project-harness-locate 및 프로젝트 훅을 확인하세요.')
    print('검증: python install/navigation.py locate "업무 개념"')
    return code


def review_glossary(install, input_fn=input):
    """Explicit human approval, separate from source-baseline approval."""
    from configure import atomic_text
    from navigation import Locator
    path = Path(install) / 'knowledge/glossary.json'
    original = path.read_text(encoding='utf-8')
    data = json.loads(original)
    locator = Locator(install=install)
    print('용어 사전 검토: 정의·주석의 주변 원문을 확인하고 승인하세요. 업무 지식 전체 승인은 아닙니다.')
    count = 0
    for item in data.get('entries', []):
        if item.get('reviewed') is True:
            continue
        print('\n' + item['term'] + ' = ' + ', '.join(item.get('symbols', [])))
        evidence = item.get('evidence', [])
        valid = bool(evidence)
        for ref in evidence:
            name = ref['path']; number = ref['line']
            file = locator.root / name
            if not locator.allowed(name) or not file.is_file() or file.stat().st_size > 2097152:
                valid = False
                print('근거 확인 불가: ' + name)
                continue
            lines = file.read_text(encoding='utf-8', errors='replace').splitlines()
            if type(number) is not int or not 1 <= number <= len(lines):
                valid = False
                print('근거 줄 범위를 확인하세요: ' + name)
                continue
            print(f'{name}:{number}')
            for i in range(max(0, number-3), min(len(lines), number+2)):
                print(f'  {i+1}: {lines[i]}')
        if valid and input_fn('원문과 의미를 검토했으면 REVIEW, Enter는 보류: ').strip() == 'REVIEW':
            item['reviewed'] = True
            count += 1
    if count:
        if path.read_text(encoding='utf-8') != original:
            raise ValueError('검토 중 사전이 변경됐습니다. 다시 실행하세요.')
        import hashlib
        backup = Path(install) / '.runtime/install-backups' / ('glossary.' + hashlib.sha256(original.encode()).hexdigest()[:16] + '.json')
        atomic_text(backup, original)
        atomic_text(path, json.dumps(data, ensure_ascii=False, indent=2) + '\n')
    print(f'이번에 승인한 용어: {count}개')
    return 0
