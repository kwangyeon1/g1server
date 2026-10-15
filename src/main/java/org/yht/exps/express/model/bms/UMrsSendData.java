package org.yht.exps.express.model.bms;

import lombok.*;
import org.apache.ibatis.type.Alias;

/***
 * UMrs 서버로 전송하기 위한 모델 정의
 */
@Data
@Alias("uMrsSendData")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class UMrsSendData {

    /***
     * 사용자에 대한 포털 메일박스 ID: TB_SMS_ENT_BUS_EDI_ECM.EDI_PORTAL_MAILBOX_ID
     */
    private String ediPortalMailboxId;
    /***
     * 사용자에 대한 포털 메일박스 ID(ediPortalMailboxId) 로 요청한 개인 엑세스 토큰: TB_SMS_ENT_BUS_EDI_ECM.EDI_PORTAL_MAILBOX_ID
     */
    private String token;
    /**
     * 문서 전송을 위한 문서 코드 : GOVCBR632.반입신고서(송신)
     */
    private String  doucmentCode;
    /***
     * 반입/반출/반입정정/반출정정을 위한 신고 번호 (반출통고는 화물관리번호)
     */
    private String customsReportNo;
    /***
     * 문서코드에 형식의 XML 변환된 스트링
     */
    private String xml;
    /**
     * 전송측
     */
    private Integer keyId;

    /**
     * 응답내용: 정상처리
     */
    String customsErrDesc;

    /**
     * 응답코드 : C200 : 정상, 이외코드 : 비정상
     */
    String customsErrCode;

    /**
     * 추가 Error 코드
     */
    String customsErrDescAdditional;

    // 내부 사용 사용자 ID
    // @ApiModelProperty(value="입력자코드")
    private String registerUserId;

    /**
     * 에러메시지용 key
     *
     * 반입신고서, 반출신고서, 목록보고서 -> BL_NO
     * 반입신고정정신청서, 반출신고정정신청서 -> CUSTOMS_REPORT_NO
     * 반입물품이상보고서 -> MBL_NO
     */
    private String errDescKey;

    /** 작업내역 조회 */
    private String odJobNo;
    private Object esJsonJobLogList;

}
