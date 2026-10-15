package org.yht.exps.express.model.edi.abnormal;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AbnormalSaveResult", description = "Abnormal Save 결과")
@Alias("abnormalSaveResult")
public class AbnormalSaveResult {
	
	@ApiModelProperty(value="관리번호")
	private String ediJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	
	@ApiModelProperty(value="엑셀joblist")
	private List<Abnormal> odList;
	
}
