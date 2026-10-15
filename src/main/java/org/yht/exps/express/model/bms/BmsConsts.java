package org.yht.exps.express.model.bms;

public interface BmsConsts {
    public String BMS_CONST_STOCK    = "STOCK";
    public String BMS_CONST_DISPOSAL = "DISPOSAL";
    public String BMS_CONST_OUT      = "OUT";
    public String BMS_CONST_OUT_PLAN = "PLAN";
    public String BMS_CONST_SAMPLE = "SAMPLE";

    public String BMS_CONST_ESTIMATE = "ESTIMATE";
    /***
     * BMS 솔루션 명
     */
    public String BMS_CONST_SOLUTION = "BMS";

    public String BMS_CONST_OUT_NOTICE       = "NOTICE"; /** 반출통고신고 - 반출통고 */
    public String BMS_CONST_OUT_NOTICE_PLAN  = "OMITTED"; /** 반출통고신고 - 누락통보 */

    /**
     * 처리 종류
     */
    interface DbType {
        /**
         * 반입예정_PKG
         */
        public String BMS_IN_PLAN_PKG        = "BMS_IN_PLAN_PKG"; // 반입
        /**
         * 반입예정
         */
        public String BMS_IN_PLAN   = "BMS_IN_PLAN"; // 반입예정
        /**
         * 반입
         */
        public String BMS_IN        = "BMS_IN"; // 반입
        /**
         * 반출승인
         */
        public String BMS_OUT_PLAN  = "BMS_OUT_PLAN"; // 반출승인
        /**
         * 반출
         */
        public String BMS_OUT       = "BMS_OUT"; // 반출
        /**
         * 견품반출
         */
        public String BMS_OUT_SAMPLE       = "BMS_OUT_SAMPLE"; // 견품반출
        /**
         * 폐기
         */
        public String BMS_DISPOSAL  = "BMS_DISPOSAL"; // 폐기
        /**
         * 창고료
         */
        public String BMS_ESTIMATE  = "BMS_ESTIMATE"; // 창고료
        /**
         * 반입물품이상
         */
        public String BMS_ABNORMAL  ="BMS_ABNORMAL"; //반입물품이상
        /**
         * 반입물품이상정정
         */
        public String BMS_ABNORMAL_CORR  ="BMS_ABNORMAL_CORR"; //반입물품이상정정
        /**
         * 반입정정
         */
        public String BMS_CHANGE_IN = "BMS_CHANGE_IN"; //반입정정
        /**
         * 반출정정
         */
        public String BMS_CHANGE_OUT = "BMS_CHANGE_OUT"; //반출정정

        /**
         * 반출 통고
         */
        public String BMS_OUT_NOTICE = "BMS_OUT_NOTICE";
        
        /**
         * 반출 통고
         */
        public String BMS_LOCAL_INSPECT_RESULT = "BMS_LOCAL_INSPECT_RESULT";
        
        /**
         * 이고반출SCAN 로그
         */
        public String BMS_TIC_MOVE_SCAN = "BMS_TIC_MOVE_SCAN";

        /**
         * 이고 반출
         */
        public String BMS_TIC_PALI       = "BMS_TIC_PALI"; // 이고 반출

        /**
         * 화물조회
         */
        public String BMS_CARGO     = "BMS_CARGO";

        /**
         * 재고실사
         */
        public String BMS_STOCK_SCAN     = "BMS_STOCK_SCAN";

        /**
         * 검사사유관리
         */
        public String BMS_INSPECT_RESULT     = "BMS_INSPECT_RESULT";
        
        /**
         * 하기결과이상보고
         */
        public String BMS_ABNORMAL_RESULT     = "BMS_ABNORMAL_RESULT";
    }

    /**
     * elastic search의 processLogs 의 JobFuncType매핑용
     */
    interface JobFuncType {
        public String INSERT = "ADD";
        public String UPDATE = "UPDATE";
        public String DELETE = "DELETE";
        public String EXCEL  = "EXCEL";
        public String BATCH  = "BATCH";
    }

    /***
     * 로그 입력 단계에서 코드 사용해서 입력 처리
     *
     */
    interface PodStatus {

        /**
         * 입력
        public String BMS01 = "BMS01"; // 입력
        /**
         * 송신실패
         */
        public String BMS02 = "BMS02"; // 송신실패
        /**
         * 송신
         */
        public String BMS03 = "BMS03"; // 송신
        /**
         * 접수
         */
        public String BMS04 = "BMS04"; // 접수
        /**
         * 오류
         */
        public String BMS05 = "BMS05"; // 오류
        /**
         * 승인
         */
        public String BMS06 = "BMS06"; // 승인
        /**
         * 거부
         */
     /*   public String BMS07 = "BMS07"; // 거부
        public String BMS08 = "BMS08"; // 보류
        public String BMS09 = "BMS09"; // 기각
        public String BMS20 = "BMS20"; // 입전통관
        public String BMS21 = "BMS21"; // 입전보운
        public String BMS30 = "BMS30"; // TIC
*/
        public String BMSA2 = "BMSA2"; // 세관검사장
        public String BMSA3 = "BMSA3"; // 통관후 입고
        public String BMSA4 = "BMSA4"; // 수입통관입고
        /**
         * 삭제
         */
      //  public String BMS99 = "BMS99"; // 삭제

        public String INSERT = "INSERT";
        public String UPDATE = "UPDATE";
        public String DELETE = "DELETE";
        public String SEND   = "SEND";
        public String BATCH  = "BATCH"; // 배치 처리 BMS_OUT to BMS_IN
        public String BATCH_01  = "BATCH_01"; //
        public String BATCH_02  = "BATCH_02"; //
        public String MFN_INSERT = "MFN_INSERT"; //반입에서 MFN(화물있으나 적하목록없음) 생성
        public String MGN_INSERT = "MGN_INSERT"; //반입에서 MGN(적하목록있으나 화물없음) 생성
        public String IPR_UPDATE = "IPR_UPDATE"; //지재권 지정
        public String BIGSIZE_UPDATE = "BIGSIZE_UPDATE"; // 거대화물지정
        public String ISPTCUSTOMS_UPDATE = "ISPTCUSTOMS_UPDATE"; // 집중검사지정
        public String LOCATION_SAVE   = "LOCATION_SAVE"; // PDA 로케이션 저장
        public String LOCATION_PICKING   = "LOCATION_PICKING"; // PDA 로케이션 피킹
        public String LOCATION_PICKING_BATCH   = "LOCATION_PICKING_BATCH"; // PDA 로케이션 일괄피킹
        public String INSPECT_PICKING   = "INSPECT_PICKING"; // PDA 검사 피킹
        public String BIGSIZE_UPDATE_Y = "BIGSIZE_UPDATE_Y"; // 거대화물지정
        public String BIGSIZE_UPDATE_N = "BIGSIZE_UPDATE_N"; // 거대화물지정취소
        public String INSPECTION_FINISH_YN = "INSPECTION_FINISH_YN"; // PDA 검사완료
        public String INSPECT_RESULT = "INSPECT_RESULT"; // PDA 검사사유관리
        public String PDA_TIC_IN_CHECK = "PDA_TIC_IN_CHECK"; // PDA 이고반입 확인
        public String SAMPLE_SIGNATURE_SAVE = "SAMPLE_SIGNATURE_SAVE"; // 견품반출 서명 저장
        public String SAMPLE_TAKING_OUT_IN = "SAMPLE_TAKING_OUT_IN"; // 견품반출 후 입고 저장
        public String BMS_TIC_PALI_CHK = "BMS_TIC_PALI_CHK"; // 이고반출대상 확인
        public String BMS_TIC_PALI_CFM = "BMS_TIC_PALI_CFM"; // 이고반출확정
        public String BMS_OUT_RELEASE = "BMS_OUT_RELEASE"; // PDA 출고
        public String BMS_OUT_CANCLE = "BMS_OUT_CANCLE"; // PDA 출고 취소
        public String BMS_CARGO_RESTOCK = "BMS_CARGO_RESTOCK"; // 화물조회 출고 후 재입고
        public String BMS_STOCK_SCAN = "BMS_STOCK_SCAN"; // 재고실사
        public String BMS_STOCK_SCAN_START = "BMS_STOCK_SCAN_START"; // 재고실사 시작
        public String BMS_STOCK_SCAN_END = "BMS_STOCK_SCAN_END"; // 재고실사 종료
        public String BMS_IN_CHANGE_CUSTOMS = "BMS_IN_CHANGE_CUSTOMS"; // 반입정정수정 세관직권정정
        public String CUSTOM_POD_NAME = "CUSTOM_POD_NAME"; // pod_name을 임의로 입력하기
        public String OUT_SCAN = "OUT_SCAN"; // 출고SCAN 수기저장
    }
}
