package org.yht.exps.express.model.systemMng;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HubQnADelete", description = "eHub 삭제할 문의사항")
@Alias("hubQnADelete")
public class HubQnADelete {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	
	@ApiModelProperty(value="리스트")
	private List<HubQnA> hubQnAList;
	@ApiModelProperty(value="관리번호")
	private String referenceNo;
	@ApiModelProperty(value="일련번호")
	private int seqNo;
	@ApiModelProperty(value="파일명(임시)")
	private String fileTempName;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	
	@ApiModelProperty(value="수정자권한")
	private String 	userAuth;	
	@ApiModelProperty(value="수정자")
	private String 	updateUserId;	
	@ApiModelProperty(value="시간")
	private String 	gmtTime;	
}
