package org.yht.exps.express.model.bms.bmsPrint;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BmsPrint", description = "보세구역관리(BMS) 출력 공통 리스트")
@Alias("BmsPrint")
@EqualsAndHashCode(callSuper=false)
public class BmsPrint extends ResultList{

	@ApiModelProperty(value="번호")
	private int num;

	@ApiModelProperty(value="반입번호")
	private String inNo;
	@ApiModelProperty(value="반출번호")
	private String outNo;
	@ApiModelProperty(value="검사계획통보번호")
	private String inspectNo;
	@ApiModelProperty(value="반출통고번호")
	private String outNoticeNo;
	@ApiModelProperty(value="반입이상관리번호")
	private String abnNo;
	@ApiModelProperty(value="창고료번호(매출번호)")
	private String estimateNo;

	@ApiModelProperty(value="BL번호")
	private String blNo;
	@ApiModelProperty(value="HBL번호")
	private String hblNo;
	@ApiModelProperty(value="MBL번호")
	private String mblNo;
	@ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNo;
	@ApiModelProperty(value="신고번호(제출번호)")
	private String customsReportNo;
	@ApiModelProperty(value="화물구분")
	private String cdType;
	
	//TB_ECM_OD_ADDITIONAL
	@ApiModelProperty(value="목록취하여부")
	private String dropYn;

	@ApiModelProperty(value="반입일자")
	private String carryinDate;
	@ApiModelProperty(value="반출일자")
	private String carryoutDate;
    @ApiModelProperty(value="제출일자")
    private String abnReqDate;

	@ApiModelProperty(value="입력자ID")
	private String registerUserId;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;

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
