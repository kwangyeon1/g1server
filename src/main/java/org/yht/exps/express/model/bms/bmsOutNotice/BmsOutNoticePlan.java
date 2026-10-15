package org.yht.exps.express.model.bms.bmsOutNotice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "BmsOutNoticePlan", description = "누락통보 VO 객체")
@Alias("bmsOutNoticePlan")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsOutNoticePlan extends ResultList {

	/** TB_BMS_OUT_NOTICE_PLAN */
    @ApiModelProperty(value="반출통고누락번호")
    private Integer outNoticePlanNo;
    @ApiModelProperty(value="화물관리번호")
    @Builder.Default private String cargoReferenceNo = "";
    @ApiModelProperty(value="MBL번호")
    @Builder.Default private String mblNo = "";
    @ApiModelProperty(value="BL번호")
    @Builder.Default private String blNo = "";
    @ApiModelProperty(value="체화일자")
    @Builder.Default private String periodOverDate = "";
    @ApiModelProperty(value="반입보세구역")
    @Builder.Default private String inLocCode = "";
    @ApiModelProperty(value="통고예정수량")
    @Builder.Default private Integer inoutNoticePlanCt = 0;
    @ApiModelProperty(value="통고예정중량")
    @Builder.Default private Double inoutNoticePlanWt = 0.00;
    @ApiModelProperty(value="반입관리번호")
    @Builder.Default private Integer inNo = 0;
    @ApiModelProperty(value="반출통고등록여부")
    @Builder.Default private String outNoticeYn = "";
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
	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="파일공유여부")
	private String fileShare;
	@ApiModelProperty(value="관세청")
	private String blStatus;
	@ApiModelProperty(value="오더관리번호")
    private String odJobNo;
	@ApiModelProperty(value="반입일자")
    private String carryinDate;
    @ApiModelProperty(value="반출통고일자")
    private String outNoticeDate;
	@ApiModelProperty(value="화물구분")
    private String cdType;
	@ApiModelProperty(value="용도구분")
    private String cdUseType;
	@ApiModelProperty(value="거래구분")
    private String ecmType;
	@ApiModelProperty(value="선별")
    private String seInspectionCode;
	@ApiModelProperty(value="검역1")
    private String inspectionType;
	@ApiModelProperty(value="검역2")
    private String inspectionTypeA;
	@ApiModelProperty(value="검역3")
    private String inspectionTypeB;
	@ApiModelProperty(value="XRAY판독")
    private String xrayInspectionCode;
	@ApiModelProperty(value="XRAY검사")
    private String xrayCheck;
	@ApiModelProperty(value="통관검사계획코드")
    private String paln5emInspectionCode;
	@ApiModelProperty(value="입항일자")
    private String arDate;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="선기명")
	private String vesselFlight;
	@ApiModelProperty(value="항차")
	private String voyage;
    @ApiModelProperty(value="포장종류")
    private String pkgType;
	@ApiModelProperty(value="장치위치")
    private String locationPlace;
	@ApiModelProperty(value="XRAY호기")
    private String xrayLineNo;
	@ApiModelProperty(value="XRAY판독수량")
    private String xrayScanCt;
	@ApiModelProperty(value="XRAY이미지수량")
    private String xrayImageCt;
	@ApiModelProperty(value="세관신고번호")
    private String customsReportNo;
	@ApiModelProperty(value="반입구분")
    private String inClass;
	@ApiModelProperty(value="반입근거번호")
    private String inApprovalNo;
	@ApiModelProperty(value="운송기한")
    private String transPeriodDate;
	@ApiModelProperty(value="화주코드")
	private String customerCode;
	@ApiModelProperty(value="화주명")
	private String customerName;
    @ApiModelProperty(value="송하인코드")
    private String shipperCode;
    @ApiModelProperty(value="송하인명")
    private String shipperName;
    @ApiModelProperty(value="송하인기본주소")
    private String shipperAddress1;
	@ApiModelProperty(value="수하인코드")
    private String consigneeCode;
	@ApiModelProperty(value="수하인상호")
    private String consigneeName;
	@ApiModelProperty(value="수하인기본주소")
    private String consigneeAddress1;
	@ApiModelProperty(value="수하인사업자")
    private String consigneeBusinessId;
	@ApiModelProperty(value="수하인전화번호")
    private String consigneeTel;
	@ApiModelProperty(value="통지처코드")
    private String notifyCode;
	@ApiModelProperty(value="통지처상호")
    private String notifyName;
	@ApiModelProperty(value="통지처기본주소")
    private String notifyAddress1;
	@ApiModelProperty(value="통지처전화번호")
    private String notifyTel;
	@ApiModelProperty(value="영문품명")
	private String commodityItemLoc;
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNo;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="참조번호")
    private String referenceNo;
	@ApiModelProperty(value="반출일자")
	private String carryoutDate;
	@ApiModelProperty(value="반출시간")
	private String carryoutTime;
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

}
