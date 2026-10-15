package org.yht.exps.express.model.bms.bmsAirDO;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BmsAirDO", description = "항공DO조회 리스트")
@Alias("BmsAirDO")
@EqualsAndHashCode(callSuper=false)
public class BmsAirDO extends ResultList{

	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="DELIVERY번호")
	private int deliveryNo;
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
	@ApiModelProperty(value="수신일자")
	private String recvDate;
	@ApiModelProperty(value="수신시간")
	private String recvTime;
	@ApiModelProperty(value="취소여부")
	private String cancelYn;
	@ApiModelProperty(value="취소일자")
	private String cancelDate;
	@ApiModelProperty(value="취소시간")
	private String cancelTime;
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
	@ApiModelProperty(value="고객명")
	private String customerName;
	@ApiModelProperty(value="포워더코드")
	private String fwdCode;
	@ApiModelProperty(value="포워더명")
	private String fwdName;
	@ApiModelProperty(value="포워더전화번호")
	private String fwdTel;
	@ApiModelProperty(value="포워더DO담당자")
	private String fwdAttnName;
	@ApiModelProperty(value="화주코드")
	private String customerCodeIn;
	@ApiModelProperty(value="화주명")
	private String customerNameIn;
	@ApiModelProperty(value="영문품명")
	private String commodityItemLoc;
	@ApiModelProperty(value="중량")
	private double wt;
	@ApiModelProperty(value="LC여부")
	private String lcYn;
	@ApiModelProperty(value="LC번호")
	private String lcNo;
	@ApiModelProperty(value="LC개설은행")
	private String lcBank;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="입력자ID")
	private String registerUserId;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;

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
