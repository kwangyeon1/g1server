package org.yht.exps.express.model.edi.separateMng;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SeparateMngDoc", description = "전자상거래  별도관리물품해제신고")
@Alias("separateMngDoc")
public class SeparateMngDoc {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;	
	@ApiModelProperty(value="관리번호")
	private String ediJobNo;
	
	@ApiModelProperty(value="별도관리물품해제신고 리스트")
	private List<SeparateMng> separateMngList;	
		
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;	
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자코드")
	private String registerUserName;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="제출일자")
	private String issueDate;	
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
	
}

