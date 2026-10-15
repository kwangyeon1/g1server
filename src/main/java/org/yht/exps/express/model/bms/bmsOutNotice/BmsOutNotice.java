package org.yht.exps.express.model.bms.bmsOutNotice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "BmsOutNotice", description = "반출통고 VO 객체")
@Alias("bmsOutNotice")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsOutNotice extends ResultList {

	/** TB_BMS_OUT_NOTICE */
    @ApiModelProperty(value="반출통고번호")
    private Integer outNoticeNo;
    @ApiModelProperty(value="세관코드")
    @Builder.Default private String customsCode = "";
    @ApiModelProperty(value="세관과코드")
    @Builder.Default private String customsDptCode = "";
    @ApiModelProperty(value="반입번호")
    @Builder.Default private Integer inNo = 0;
    @ApiModelProperty(value="화물관리번호")
    @Builder.Default private String cargoReferenceNo = "";
    @ApiModelProperty(value="BL번호")
    @Builder.Default private String blNo = "";
    @ApiModelProperty(value="MBL번호")
    @Builder.Default private String mblNo = "";
    @ApiModelProperty(value="현재진행상태일시")
    @Builder.Default private String lastPodDate = "";
    @ApiModelProperty(value="현재진행상태코드")
    @Builder.Default private String lastPodStatus = "";
    @ApiModelProperty(value="접수일수")
    @Builder.Default private String lastPodRaqDate = "";
    @ApiModelProperty(value="반출통고일자")
    @Builder.Default private String outNoticeDate = "";
    @ApiModelProperty(value="체화일자")
    @Builder.Default private String periodOverDate = "";
    @ApiModelProperty(value="통고출력여부")
    @Builder.Default private String printYn = "";
    @ApiModelProperty(value="포장종류")
    @Builder.Default private String pkgType = "";
    @ApiModelProperty(value="수량")
    @Builder.Default private Integer noticeCt = 0;
    @ApiModelProperty(value="중량")
    @Builder.Default private Double noticeWt = 0.00;
    @ApiModelProperty(value="화주코드")
    @Builder.Default private String customerCode = "";
    @ApiModelProperty(value="화주명")
    @Builder.Default private String customerName = "";
    @ApiModelProperty(value="화주우편번호")
    @Builder.Default private String customerZipcode = "";
    @ApiModelProperty(value="화주기본주소")
    @Builder.Default private String customerAddress1 = "";
    @ApiModelProperty(value="화주상세주소")
    @Builder.Default private String customerAddress2 = "";
    @ApiModelProperty(value="화주전화번호")
    @Builder.Default private String customerTel = "";
    @ApiModelProperty(value="화주비고")
    @Builder.Default private String customerRemarks = "";
    @ApiModelProperty(value="송하인코드")
    @Builder.Default private String shipperCode = "";
    @ApiModelProperty(value="송하인명")
    @Builder.Default private String shipperName = "";
	@ApiModelProperty(value="수하인코드")
    @Builder.Default private String consigneeCode = "";
	@ApiModelProperty(value="수하인상호")
    @Builder.Default private String consigneeName = "";
	@ApiModelProperty(value="통지처코드")
    @Builder.Default private String notifyCode = "";
	@ApiModelProperty(value="통지처상호")
    @Builder.Default private String notifyName = "";
	@ApiModelProperty(value="영문품명")
    @Builder.Default private String commodityItemLoc = "";
	@ApiModelProperty(value="장치위치")
    @Builder.Default private String locationPlace = "";
    @ApiModelProperty(value="입력자코드")
    @Builder.Default private String registerUserId = "";
    @ApiModelProperty(value="입력자이름")
    @Builder.Default private String registerUserName = "";
    @ApiModelProperty(value="입력일시")
    @Builder.Default private String registerDateTime = "";
    @ApiModelProperty(value="수정자코드")
    @Builder.Default private String updateUserId = "";
    @ApiModelProperty(value="수정자이름")
    @Builder.Default private String updateUserName = "";
    @ApiModelProperty(value="수정일시")
    @Builder.Default private String updateDateTime = "";

	/** 그리드 컬럼 */
	@ApiModelProperty(value="파일공유여부")
	private String fileShare;
	@ApiModelProperty(value="관세청")
	private String blStatus;
	@ApiModelProperty(value="오더관리번호")
    private String odJobNo;
	@ApiModelProperty(value="세관신고번호")
    private String customsReportNo;
	@ApiModelProperty(value="반입일자")
    private String carryinDate;
	@ApiModelProperty(value="화물구분")
    private String cdType;
	@ApiModelProperty(value="용도구분")
    private String cdUseType;
	@ApiModelProperty(value="반입구분")
	private String inClass;
	@ApiModelProperty(value="조회타입구분")
	private String srchGbn;

	/** 그리드 상단 Count */
	@ApiModelProperty(value="총건수")
	private int totalCnt;
	@ApiModelProperty(value="목록건수")
	private int totalSimpleCnt;
	@ApiModelProperty(value="일반건수")
	private int totalGeneralCnt;
	@ApiModelProperty(value="총건수")
	private int cnt;
	@ApiModelProperty(value="목록건수")
	private int simpleCnt;
	@ApiModelProperty(value="일반건수")
	private int generalCnt;

	/** 그 외 */
	@ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="대표사업장코드", example="YHTXX-KR")
    private String entBusinessCode;
    @ApiModelProperty(value="반출통고누락번호")
    private Integer outNoticePlanNo;
    @ApiModelProperty(value="반출통고등록여부")
    private String outNoticeYn;

    /** 화물관리번호 11-4-4 표시 */
	@ApiModelProperty(value="화물관리번호_표시")
    public String getCargoReferenceNoDisp() {
        String ret = getCargoReferenceNoDisp(this.cargoReferenceNo);
        return ret;
    }

    private String getCargoReferenceNoDisp(String _cargoReferenceNo) {
        String ret = "";
        if(!_cargoReferenceNo.equals("") ) {
            if(_cargoReferenceNo.length() > 10) {
                if(_cargoReferenceNo.length() == 19) {
                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11, 15) + "-" + _cargoReferenceNo.substring(15, 19);
                } else if (_cargoReferenceNo.length() == 18) { /** 마이그레이션 값은 18이다 */
                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11, 15) + "-" + _cargoReferenceNo.substring(15);
                } else {
                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11);
                }
            } else {
                ret = _cargoReferenceNo;
            }
        }
        return ret;
    }
    
    private String batchUpdateTargetYn;

    /** Web XML Send */
    private String typeCode; 		 	/** 문서형태구분: 전송될 전자문서의 유형을 기재(ex. GOVCBR5II) */
    private String officeId;			/** 신고세관/과 */
    private String functionCode; 	 	/** 전자문서기능(9: 원본, 35: 재전송) */
    private String bmsCustomsCode;   	/** 세관코드 */
    private String bmsCustomsDptCode;	/** 세관과코드 */
    private String bmsWhCode; 			/** 보세구역번호 */

    /** 시스템 로그 - ES */
    @ApiModelProperty(value="처리자")
    private String logUserName;
    @ApiModelProperty(value="처리자(영)")
    private String logUserNameEng;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;

}
