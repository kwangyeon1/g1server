package org.yht.exps.express.model.mobile.bms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(value = "MobBwis", description = "PDA 검사대상확인")
@Alias("mobBwis")
public class MobBwis {
	
//	@ApiModelProperty(value="반입번호")
//    private Integer inNo;
	@ApiModelProperty(value="BL번호")
    private String blNo;
	@ApiModelProperty(value="반입일자")
    private String carryinDate;
	@ApiModelProperty(value="반입수량")
    private Integer ct;
	@ApiModelProperty(value="검사완료여부")
	private String inspectionFinishYn;
	@ApiModelProperty(value="선별결과코드(목록선별)")
    @Builder.Default
    private String seInspectionCode = "";
	@ApiModelProperty(value="검역구분코드")
    @Builder.Default
    private String inspectionType = "";
    @ApiModelProperty(value="검역구분코드2")
    @Builder.Default
    private String inspectionTypeA = "";
    @ApiModelProperty(value="검역구분코드3")
    @Builder.Default
    private String inspectionTypeB = "";
    @ApiModelProperty(value="XRAY판독구분코드")
    @Builder.Default
    private String xrayInspectionCode = "";
    @ApiModelProperty(value="통관검사계획코드")
    @Builder.Default
    private String paln5emInspectionCode = "";
    @ApiModelProperty(value="tic_out_scan_user")
    @Builder.Default
    private String ticOutScanUserId = "";

}
