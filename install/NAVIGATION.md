# 에이전틱 탐색과 위치 검색

요청 → 검토된 용어 별칭 훅 → 주 에이전트 grep/원문 읽기 → 필요할 때 locate → 원문 확인 → 작업과 검증.
용어 훅은 로컬 사전의 승인된 항목만 읽습니다. 검색은 에이전트가 locate를 호출할 때 수행합니다.

## locate 계약

파일·클래스·메서드·테이블 이름을 모를 때만 사용합니다. 이름을 알면 grep을 사용합니다.
결과는 후보이며 현재 원문·호출자·SQL·예외 처리·테스트를 읽고 수정합니다.

```json
{"path":"src/Port.java","start_line":1,"end_line":80,"symbol":"Port","via":["bm25"],"index_commit":"<Git commit>","stale":false,"stale_reason":null}
```

본문·excerpt·정답·수정안은 반환하지 않습니다. symbol은 해당 창에 포함된 심볼 이름을 한 줄로 요약하며 정밀 AST 서명이 아닙니다.
via는 bm25 또는 glossary+bm25입니다. glossary는 승인된 별칭으로 검색어를 확장했다는 뜻입니다.
한국어는 원 단어와 2/3음절 n-gram, 식별자는 CamelCase/밑줄 구성요소로 토큰화합니다.
형태소 분석/의미 검색이 아니므로 공통 음절만 겹치는 오탐은 가능합니다.
80줄 창/60줄 간격은 후보 위치를 찾기 위한 내부 색인 단위이며 본문은 응답에 포함하지 않습니다.

Git HEAD의 허용된 파일을 인덱싱하며 커밋 변경 시 blob ID가 바뀐 파일만 재색인합니다.
설정 변경 시 정책을 다시 계산하고 필요하면 색인을 재구성합니다. 정책 강화로 제외된 파일은 검색 시에도 걸러집니다.
working tree가 달라지면 stale=modified/deleted로 표시하고 과거 줄 번호임을 명시합니다.
줄바꿈 CRLF/LF 차이는 동일한 텍스트로 처리합니다. 신규 미커밋 파일은 grep으로 확인합니다.
UTF-8 텍스트, 파일당 최대 2 MiB(더 작은 max_source_bytes 적용)를 대상으로 합니다.
Git 없는 폴더·빈 Git 저장소는 명시적으로 검색 불가이며 설치는 가능합니다.
설치 직후의 프로젝트별 초안이 미커밋이면 locate에 포함되지 않지만 용어 훅은 승인된 로컬 사전을 읽습니다.

## 보호 정책

YML/YAML/properties, config/settings 이름, .env/키/인증 파일, 로그·generated·빌드 산출물,
.git/.codex/.claude/.agents/.aws/.ssh/가상환경, minified/map/일부 코드생성 파일은 기본 제외합니다.
파일 상단 @generated/auto-generated/do not edit 표시, 바이너리, 심볼릭 링크도 제외합니다.
추가 제외: install/config.json의 sensitive_source_globs, exclude_dirs, navigation.exclude_globs.
슬래시 없는 민감 패턴은 하위 디렉터리의 파일명에도 적용합니다. DatabaseConfig.java도 중첩 경로를 보호합니다.
이름에 config가 든 일반 소스도 보수적으로 제외될 수 있으며 그 경우 직접 grep/읽기를 사용합니다.
임의의 일반 파일명에 적힌 비밀이나 모든 생성기를 자동 판별하는 기능은 아닙니다. 프로젝트별 제외를 추가하세요.
토큰 색인은 로컬 .runtime/locate/index.sqlite에만 저장합니다. 저장소 본문을 원격으로 전송하지 않습니다.

## 용어 사전 초안과 검토

공통코드·상수·주석에서 term, aliases, symbols를 찾고 evidence에 실제 path/line을 붙입니다.
초안의 reviewed는 false로 둡니다. `python install/configure.py --glossary`에서 근거를 읽고 승인합니다.
변경될 수 있는 업무 의미를 자동 승인하지 않습니다. 매칭은 한글 부분 문자열(조사 허용), 영문 식별자는 경계로 구분합니다.
사전 파일 최대 128 KiB/300항목, 요청 최대 12,000자, 응답 최대 6항목/1,200자입니다.
사전/지도 수정은 검토 가능한 원본 변경입니다. 업무 의미가 바뀌면 사전 항목을 수정하고 reviewed=false로 되돌려 재검토하세요.
사전과 지도 초안 작성은 사용자 또는 주 에이전트가 수행하며 설치기는 자동 추출하지 않습니다.

## 세 조건의 측정

TUI 1(A): navigation.glossary_hook_enabled=false, locate_enabled=false.
TUI 2(B): glossary_hook_enabled=true, locate_enabled=false.
TUI 3(C): 둘 다 true. MCP 등록은 유지하되 비활성 locate는 DISABLED를 반환합니다.
정확한 도구 노출 자체의 영향까지 비교하려면 A/B에서 앱의 해당 MCP를 비활성화하고 기록하세요.

같은 시작 커밋·요청·모델·도구 예산으로 별도 세션에서 반복하고 순서를 바꾸세요.
호출자 누락과 근거 누락은 사람이 원문으로 판정합니다. review_findings에는 별도 검토에서 확인한 지적 건수를 기록합니다.
실제 앱 usage의 입력/출력 토큰과 소요 시간을 사용하세요. 모델을 호출하지 않은 테스트에서 추정하지 않습니다.
막힌 상황에 locate를 호출했는지는 도구 로그를 보고 기록합니다. 미측정은 null이며 0이 아닙니다.

```json
{"condition":"C","task_id":"customs-inspection","commit":"<commit>","model":"<model/version>","missed_callers":null,"missing_evidence":null,"review_findings":null,"input_tokens":null,"output_tokens":null,"elapsed_seconds":null,"blocked":null,"locate_called_when_blocked":null,"evidence":"<검토 및 실행 기록 위치>"}
```

```sh
python install/navigation_eval.py record install/.runtime/navigation-trials.jsonl --input trial.json
python install/navigation_eval.py summary install/.runtime/navigation-trials.jsonl
```

record는 입력한 측정 JSON을 JSONL에 추가합니다. summary는 수치의 측정 건수·중앙값과 막힌 상황의 도구 호출 건수를 요약합니다.
이 명령은 모델 실행이나 토큰 수집을 자동 수행하지 않으며, 누락값을 성공으로 계산하지 않습니다.
하네스는 주 에이전트의 도구 선택이나 병렬 grep을 강제하지 않습니다. 도구 설명문·지도와 실제 사용 측정으로 검토합니다.
