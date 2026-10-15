# Portable Codex + Hermes harness

이 install 폴더를 대상 프로젝트 루트에 압축 해제합니다. 빈 폴더에서도 시작할 수 있습니다.
기존 install 폴더가 있다면 덮어쓰기 전에 내용을 확인하세요. 기존 하네스의 지식·개인 설정을 배포본으로 덮어쓰지 마세요.
Python 3.11 이상이 필요합니다. 코어는 표준 라이브러리만 사용하며 프로젝트 패키지를 설치하지 않습니다.

## Windows 설치

```powershell
.\install\bootstrap.ps1             # 설치 계획
.\install\bootstrap.ps1 -Execute    # 실제 로컬 설치
```

PowerShell 정책이 스크립트 실행을 차단하면 Python으로 `python install/bootstrap.py --execute`를 실행할 수 있습니다.
코어 설치는 프로젝트 전용 Python 환경·지침 관리 블록·검색 색인·작업 큐·프로젝트 훅을 만듭니다.
기존 AGENTS.md/CLAUDE.md의 관리 블록 밖 내용과 다른 훅은 보존합니다. 변경 전 복구본은 .runtime에 남깁니다.
기존 프로젝트에 원문이 있으면 종료 코드 2와 REVIEW_REQUIRED는 설치 실패가 아니라 문서 검토 필요 상태입니다.
완전히 빈 프로젝트는 검토할 원문이 없다는 기준만 기록합니다. 코드가 추가되면 다시 검토가 필요합니다.

## Linux / macOS 설치

```sh
sh install/bootstrap.sh
sh install/bootstrap.sh --execute
sh install/run.sh doctor
```

Python venv 지원이 필요합니다. 해당 OS의 Python 설치 방법을 사용하세요. .venv는 OS·컴퓨터 사이에 복사하지 않습니다.
이 배포본의 Windows 실제 설치 검증과 POSIX 스크립트 제공은 구분합니다. 다른 OS에서는 자체 확인이 필요합니다.

## 프로젝트에 맞게 준비

설정·지식 문서 작성·검토·검색의 구체적인 예시는 [guide.txt](guide.txt)를 참고하세요. 이전 안내 답변을 그대로 저장한 파일입니다.
guide의 첫 config.json 링크는 이전 답변의 로컬 링크이므로, 실제로 수정할 파일은 대상 프로젝트의 `install/config.json`입니다. Node 예시는 기존 설정에 병합할 일부 항목입니다.

1. install/config.json의 source_globs, exclude_dirs, sensitive_source_globs를 확인합니다. 소스 언어가 목록에 없으면 패턴을 추가합니다.
2. knowledge/project.md, testing.md, database.md, decisions.md를 실제 코드와 프로젝트 지침에 맞게 작성합니다.
3. `run.ps1 sync` → generated/review-queue.md와 관련 원문·문서 검토 → `run.ps1 accept-review --note "실제 검토 내용"` → `run.ps1 sync` 순서로 실행합니다. Linux는 run.sh를 사용합니다.
4. `run.ps1 search-docs '프로젝트' --limit 3`으로 확인합니다. 소스 변경을 승인 없이 무시하는 설정은 없습니다.

위 수동 절차는 그대로 사용할 수 있습니다. 예시를 보며 순서대로 입력하려면 **프로젝트 준비 TUI**를 실행하세요.

```powershell
.\install\configure.ps1
```

Linux/macOS: `sh install/configure.sh`. Python 직접 실행: `python install/configure.py`.
Python 3.11+만 필요하며, bootstrap 이후 실행을 권장합니다. 별도 TUI 라이브러리를 설치하지 않습니다.

TUI는 다음 순서로 진행합니다.

1. 소스 패턴·제외 폴더·민감 파일 패턴과 선택적인 빌드/테스트 명령 입력. 각 항목에 짧은 예시를 표시합니다.
2. project.md의 목적·진입점·처리 흐름 작성.
3. testing.md의 테스트 명령·성공 조건·검증 범위 작성.
4. database.md의 저장소·주요 테이블·처리 기준 작성. DB가 없으면 사용 안 함으로 기록합니다.
5. decisions.md의 설계 결정 작성 후 전체 변경 미리보기와 저장 선택.
6. sync 결과·검토 목록 확인. 추적 원문을 TUI에서 읽거나 외부 편집기로 검토한 뒤 실제 검토 내용과 `REVIEW`를 입력하면 accept-review와 sync를 실행합니다. 나중에 검토할 수도 있습니다.
7. 프로젝트 검색어로 BM25 결과 확인. 예: 주문 전송 실패, 중복 주문.

Enter는 현재 값 유지입니다. 선택 항목의 `-`는 비우기, `:back`은 이전 단계, `:quit` 또는 Ctrl+C는 종료입니다.
문서는 유지·추가·전체 다시 작성 중 선택하며 여러 줄 입력 후 한 줄에 `.`을 입력해 마칩니다.
저장 전에는 설정·문서를 변경하지 않습니다. 저장 후의 종료는 이미 저장한 파일·sync·검토 기준을 되돌리지 않습니다.
수정 전 사본은 `.runtime/configure/<ID>/`에 보관하며, 입력 도중 다른 작업에서 설정·문서가 바뀌면 덮어쓰지 않고 중단합니다.
빌드·테스트 명령은 등록만 하며 TUI에서 실행하지 않습니다. Hermes 모델 호출이나 업무 DB 작업도 수행하지 않습니다.
검토를 미루면 STALE_KNOWLEDGE와 종료 코드 2가 나올 수 있습니다. 작성·입력만으로 검토 완료가 자동 승인되지는 않습니다.

자동 탐색은 Python, Node/JS/TS, Java/Kotlin, Go, Rust, .NET, C/C++, Ruby, PHP 등 일반 텍스트 원문과 빌드 설정을 대상으로 합니다.
node_modules, 가상환경, .git, install, build/dist 등은 순회에서 제외합니다. 비밀 파일명 패턴(.env, 개인 키 등)도 제외합니다.
임의 이름에 저장한 비밀값까지 자동 탐지하는 것은 아니므로 프로젝트별 민감 경로 설정을 검토하세요.
프로젝트 감지는 안내용이며 프로젝트 빌드·테스트·DB·배포·부하 도구를 자동 설치하거나 실행하지 않습니다.

## Hermes 연결

```powershell
.\install\install-hermes.ps1 -Execute  # 공식 Hermes가 아직 없을 때만
hermes setup                          # 모델·인증은 각 사용자 환경에서 설정
.\install\bridge.ps1 start
.\install\bridge.ps1 status
```

Linux/macOS에서는 `sh install/install-hermes.sh --execute`, `sh install/bridge.sh start`를 사용합니다.
Codex 데스크톱 앱 설정에서 대상 프로젝트 Hooks 정의를 검토·신뢰하고 이 프로젝트에서 요청합니다.
Hermes 설치·인증·앱 훅 신뢰는 bootstrap만으로 완료되지 않습니다. 패키지에 인증·대화·사용자 경로는 없습니다.
훅의 즉시 BM25 검색·이전 검토 인계 → Codex 작업 → Hermes 비동기 검토·요약 → 다음 요청 인계 순서입니다.
status는 작업자, 실제 응답, 수신 기록, 검색 최신성을 구분합니다. JSON은 status --json입니다.
새 설치에서 앱 컨텍스트 수신 근거가 없으면 미검증으로 표시합니다. 이전 프로젝트의 검증 결과를 가져오지 않습니다.
설치된 검색 검사: `.\install\verify-search.ps1` 또는 `install/.venv/bin/python install/bridge/verify_search.py`.
프로젝트별 검색어는 `--query '검색할 단어'`로 지정합니다.
검색 검사는 모델 호출·가짜 앱 이벤트 없이 검색 경로만 확인합니다. 실제 앱 인계 검증과는 다릅니다.

## 빌드·테스트 연결

config.json의 commands에 사용자가 승인한 명령을 문자열 배열로 등록합니다. 기본은 비어 있습니다.
예: `"commands": {"test": ["python", "-m", "unittest", "discover"]}`.
명령은 프로젝트 루트에서 shell=False로 실행합니다. 파이프·리다이렉션은 해석하지 않습니다.
Windows에서는 도구에 맞는 실행 파일 또는 명시적인 `powershell.exe -File ...` 명령을 등록합니다.

```powershell
.\install\run.ps1 run-check test              # 계획만 출력
.\install\run.ps1 run-check test --execute    # 등록된 명령 실행·로그 보존
```

종료 코드 성공은 명령 성공이며 테스트 개수·업무 정확성까지 보증하지 않습니다. 로그를 확인하세요.
Gradle Java 전용 check-build/run-tests와 MSSQL 전용 db-*는 선택 어댑터입니다. 다른 스택의 공통 기능이 아닙니다.
DB는 대상 프로젝트의 연결 도구·스키마·권한을 확인하고 사용합니다. 특정 DB MCP가 있다고 가정하지 않습니다.

## 순정 파일과 생성물

PACKAGE-MANIFEST.json에 ZIP에 포함한 모든 원본의 경로·SHA256이 있습니다.
bootstrap.py/.ps1/.sh, harness.py, project_profile.py, 실행·설치 래퍼, bridge 코드·기본 설정·프롬프트,
templates, 초기 knowledge, 선택 gradle/sql 어댑터, tests, 이 문서는 배포 원본입니다.
source-baseline.json은 원본에 포함되지 않으며 검토 후 생성됩니다.
.venv, __pycache__, .runtime, knowledge/generated, knowledge/hermes, 개인 config.local.json은 배포에 포함되지 않습니다.
같은 프로젝트를 이전할 때는 knowledge와 검토 기준을 별도로 보존하세요. 새 프로젝트에는 이전 업무 지식·실행 기록을 이식하지 않습니다.
배포본에는 과거 삭제용 cleanup-legacy.ps1이 없습니다. 현재 디렉터리를 정리하거나 기존 파일을 삭제하는 설치 단계도 없습니다.

## 순정 ZIP 다시 만들기

원본 저장소에서 `python install/build_bundle.py`를 실행합니다.
허용 목록의 코드와 portable 초기 자료만 패키징하며 순정 파일 목록과 ZIP SHA256을 함께 생성합니다.
