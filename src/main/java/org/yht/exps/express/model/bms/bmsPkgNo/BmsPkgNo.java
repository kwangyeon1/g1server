package org.yht.exps.express.model.bms.bmsPkgNo;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BmsPkgNo", description = "보세구역관리(BMS) 박스번호조회 공통 리스트")
@Alias("BmsPkgNo")
@EqualsAndHashCode(callSuper=false)
public class BmsPkgNo extends ResultList{

	@ApiModelProperty(value="박스번호")
	private String packingNo;
	@ApiModelProperty(value="BL번호")
	private String blNo;
	@ApiModelProperty(value="MBL번호")
	private String mblNo;
	@ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNo;
	@ApiModelProperty(value="예정수량")
	private Integer exCt;
	@ApiModelProperty(value="삭제여부")
	private String deleteYn;
	@ApiModelProperty(value="seq")
	private String seq;
	
	@ApiModelProperty(value="새로운 화물관리번호")
	private String newCargoReferenceNo;

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
