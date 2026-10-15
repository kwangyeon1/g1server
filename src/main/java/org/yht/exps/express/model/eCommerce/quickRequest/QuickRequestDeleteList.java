package org.yht.exps.express.model.eCommerce.quickRequest;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "QuickRequestDeleteList", description = "고객서비스 반품관리 리스트(삭제용)")
@Alias("quickRequestDeleteList")
@EqualsAndHashCode(callSuper=false)
public class QuickRequestDeleteList extends ResultList{
	@ApiModelProperty(value="리스트")
	private List<Map<String, Object>> dataList;
	
	@ApiModelProperty(value="도입사")
	private String entCode;
	@ApiModelProperty(value="수정자")
	private String updateUserId;
	@ApiModelProperty(value="입력자")
	private String registerUserId;	
	@ApiModelProperty(value="로그유저명")
	private String logUserName;	
	@ApiModelProperty(value="로그유저영문명")
	private String logUserNameEng;	
	@ApiModelProperty(value="시간")
	private String gmtTime;
}
