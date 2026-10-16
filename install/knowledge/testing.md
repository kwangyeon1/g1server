# 테스트 지식

하네스 테스트는 프로젝트 루트에서 `python -m unittest discover -s install/tests -v`로 실행한다.
임시 프로젝트 설치·설정 보존·용어 훅·MCP·증분 색인·보호 경로를 검사한다.
하네스 테스트와 서버 업무 테스트의 검증 범위는 구분한다.

서버 업무 테스트 명령·환경·성공 조건은 별도 작성 대상이며 현재 등록된 실행 근거는 없다.
install/config.json의 commands.build/test를 등록한 뒤 run-check로 실행하고 install/.runtime/runs의 로그와 결과를 확인한다.
테스트 파일의 존재나 명령 종료 코드만으로 모든 업무가 검증됐다고 판단하지 않는다.
