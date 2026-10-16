## 탐색 원칙

기본은 주 에이전트의 grep/파일 읽기다. 요청마다 검색 결과를 선주입하지 않는다.
파일·클래스·메서드·테이블 이름을 알면 grep을 사용한다.
이름을 모르거나 개념과 코드의 대응을 찾지 못할 때만 MCP의 locate를 사용한다.
locate는 위치 후보다. 수정 전에 현재 원문과 호출자·SQL·예외 처리·테스트를 읽는다.
짧은 파일은 전체를 읽고, 큰 파일은 관련 심볼과 주변 흐름을 충분히 확장해서 읽는다.
stale=true의 줄 번호는 과거 위치다. 현재 파일에서 심볼을 다시 찾는다.
미커밋 신규 파일은 색인에 없다. grep/파일 목록으로 확인한다.
용어 별칭은 grep 단서다. 용어 사전의 주석을 업무 동작의 확정 근거로 삼지 않는다.
용어 사전: install/knowledge/glossary.json (reviewed=true만 요청 훅에서 사용).
지도 원본: install/knowledge/project-map.md. 지도 변경 후 install-guidance로 관리 블록을 갱신한다.
비밀값·접속 설정·생성 코드·로그는 검색 자료에 복사하지 않는다.
상태/운영: install/AGENTS.md, install/README.md, install/NAVIGATION.md.
실행·테스트 성공은 실제 근거로 판단하고 미검증 범위를 명시한다.

{{PROJECT_MAP}}
