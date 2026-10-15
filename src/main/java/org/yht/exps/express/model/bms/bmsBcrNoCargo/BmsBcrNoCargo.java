package org.yht.exps.express.model.bms.bmsBcrNoCargo;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BmsBcrNoCargo", description = "BCR 무적")
@Alias("BmsBcrNoCargo")
@EqualsAndHashCode(callSuper=false)
public class BmsBcrNoCargo extends ResultList{

	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="BCRKEY")
	private int serial;
	@ApiModelProperty(value="SCAN번호")
	private String scanNo;
	@ApiModelProperty(value="SCAN결과")
	private String scanResult;
	@ApiModelProperty(value="SCAN일자")
	private String scanDate;
	@ApiModelProperty(value="SCAN시간")
	private String scanTime;
	@ApiModelProperty(value="체크완료여부")
	private String checkYn;
	@ApiModelProperty(value="체크완료비고")
	private String checkRemarks;
	@ApiModelProperty(value="입력자ID")
	private String registerUserId = "";
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자ID")
	private String updateUserId = "";
	@ApiModelProperty(value="수정자")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
}
