## 프로젝트 지도 — g1server (소스에서 확인한 탐색 초안)

- 이 저장소는 독립 하네스를 시험하는 서버 예시다. 하네스는 특정 업무·언어에 종속되지 않는다.
- 실행 진입점: src/main/java/org/yht/Application.java.
- HTTP 진입점: src/main/java/org/yht/exps/express/controller/.
- 업무 서비스: src/main/java/org/yht/exps/express/service/.
- Mapper 인터페이스: src/main/java/org/yht/exps/express/mapper/.
- SQL XML: src/main/resources/org/yht/exps/express/mapper/.
- 공통 상수: src/main/java/org/yht/exps/express/model/bms/BmsConsts.java.
- 반출 관련 API: controller/bms/BmsOutController.java → service/bms/BmsOutService.java.
- 반출 DB 접근: mapper/bms/BmsOutMapper.java → resources의 mapper/bms/BmsOutMapper.xml.
- 세관검사장 변경 단서: updateCustomsInspect, updateBmsOutCustomsInspect, BMSA2, LAST_POD_STATUS.
- 반입 단서: BmsIn*. PDA 단서: controller/pda/, service/pda/, mapper/pda/.
- 화면 소유 위치는 이 서버 지도에서 확정하지 않는다. API 경로를 통해 별도 화면 프로젝트를 확인한다.
- 빌드 설정: build.gradle. 업무 테스트 명령은 install/config.json의 commands에서 별도 등록한다.
- 검색 제외: YML/YAML 및 config/settings 이름 패턴, src/main/java/org/yht/config/**. 다른 민감 경로는 설정에 추가한다.
- 설치 코드: install/. 설치 상태·검색 색인·검증 기록: install/.runtime/ (Git 제외).
- 용어 초안은 install/knowledge/glossary.json의 evidence를 확인해 사람이 승인한 뒤 사용한다.
