package org.yht.exps.express.model.bms.bmsBlDivide;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BmsBlDivide", description = "보수&BL분할 조회 리스트")
@Alias("BmsBlDivide")
@EqualsAndHashCode(callSuper=false)
public class BmsBlDivide extends ResultList{

	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="BL분할결과번호")
	private int bl5hfNo;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="파일공유여부")
	private String fileShare;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="관세청")
	private String blStatus;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="반입일자")
	private String carryinDate;
	@ApiModelProperty(value="통보일자")
	private String noticeDate;
	@ApiModelProperty(value="통보시간")
	private String noticeTime;
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
	@ApiModelProperty(value="분할구분코드")
	private String divideClass;
	@ApiModelProperty(value="분할구분")
	private String divideClassName;
	@ApiModelProperty(value="반입신고번호")
	private String customsReportNo;
	@ApiModelProperty(value="세관담당자")
	private String customsAttnName;
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
	@ApiModelProperty(value="분할HSN")
	private String hsn;
	@ApiModelProperty(value="영문품명")
	private String commodityItemLoc;
	@ApiModelProperty(value="장치위치")
	private String locationPlace;
	@ApiModelProperty(value="입력자ID")
	private String registerUserId;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="화주코드")
	private String customerCode;
	@ApiModelProperty(value="화주명")
	private String customerName;
	@ApiModelProperty(value="송하인명")
	private String shipperName;
	@ApiModelProperty(value="수하인명")
	private String consigneeName;
	@ApiModelProperty(value="통지처명")
	private String notifyName;
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

	/** 반입 정보 */
	@ApiModelProperty(value="BL반입번호")
	private int inNo;
	@ApiModelProperty(value="BL반입예정번호")
	private int inPlanNo;
	
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
