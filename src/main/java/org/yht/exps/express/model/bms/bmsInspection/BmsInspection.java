package org.yht.exps.express.model.bms.bmsInspection;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BmsInspection", description = "검사계획통보 조회 리스트")
@Alias("BmsInspection")
@EqualsAndHashCode(callSuper=false)
public class BmsInspection extends ResultList{

	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="반입번호")
	private Integer inNo;
	@ApiModelProperty(value="검사계획통보번호")
	private Integer inspectNo;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="파일공유여부")
	private String fileShare;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="관세청")
	private String blStatus;
	@ApiModelProperty(value="문서구분(통보여부)")
	private String docType;
	@ApiModelProperty(value="문서구분명(통보여부명)")
	private String docTypeName;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="검사예정일자")
	private String inspectDate;
	@ApiModelProperty(value="검사예정시간")
	private String inspectTime;
	@ApiModelProperty(value="BL번호")
	private String blNo;
	@ApiModelProperty(value="HBL번호")
	private String hblNo;
	@ApiModelProperty(value="MBL번호")
	private String mblNo;
	@ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNo;
	@ApiModelProperty(value="화물구분")
	private String cdType;
	@ApiModelProperty(value="수입신고번호")
	private String approvalNo;
	@ApiModelProperty(value="검사구분코드")
	private String inspectType;
	@ApiModelProperty(value="검사구분")
	private String inspectTypeName;
	@ApiModelProperty(value="반입신고번호")
	private String customsReportNo;
	@ApiModelProperty(value="세관담당자명")
	private String customsAttnName;
	@ApiModelProperty(value="세관담당자전화번호")
	private String customsAttnTel;
	@ApiModelProperty(value="선기명")
	private String vesselFlight;
	@ApiModelProperty(value="항차")
	private String voyage;
	@ApiModelProperty(value="포장종류")
	private String pkgType;
	@ApiModelProperty(value="수량")
	private int ct;
	@ApiModelProperty(value="중량")
	private double wt;
	@ApiModelProperty(value="장치위치")
	private String locationPlace;
	@ApiModelProperty(value="검사대상요청내용")
	private String targetContents;
	@ApiModelProperty(value="검사준비요청내용")
	private String preperationContents;
	@ApiModelProperty(value="검사협조요청내용")
	private String cooperationContents;
	@ApiModelProperty(value="검사품명")
	private String itemName;
	@ApiModelProperty(value="참조번호")
	private String referenceNo;
	@ApiModelProperty(value="입력자ID")
	private String registerUserId;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자ID")
	private String updateUserId;
	@ApiModelProperty(value="수정자")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="화주코드")
	private String customerCode;
	@ApiModelProperty(value="화주명")
	private String customerName;
	@ApiModelProperty(value="수하인명")
	private String consigneeName;
	@ApiModelProperty(value="통지처명")
	private String notifyName;
	@ApiModelProperty(value="영문품명")
	private String commodityItemLoc;
	@ApiModelProperty(value="거대여부")
	private String bigSizeYn;
	@ApiModelProperty(value="홀드여부")
	private String holdYn;
	@ApiModelProperty(value="홀드코드")
	private String holdCode;
	@ApiModelProperty(value="홀드사유")
	private String holdReason;
	@ApiModelProperty(value="홀드처리여부")
	private String holdReleaseYn;
	@ApiModelProperty(value="홀드처리담당자")
	private String holdReleaseAttn;
	@ApiModelProperty(value="홀드처리일자")
	private String holdReleaseDate;
    @ApiModelProperty(value="관세사명")
    private String brokerName;

	/** 그리드 컬럼 */
	@ApiModelProperty(value="반입일자")
    private String carryinDate;

	//TB_ECM_OD_ADDITIONAL
    @ApiModelProperty(value="목록취하여부")
    private String dropYn;

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
        if(_cargoReferenceNo != null) {
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
