package org.yht.exps.express.model.systemMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "AlarmGroup", description = "알림 서비스")
@Alias("alarmGroup")
@EqualsAndHashCode(callSuper=false)
public class AlarmGroup extends ResultList{
	@ApiModelProperty(value="groupId")
	private String groupId;
	@ApiModelProperty(value="groupName")
	private String groupName;	
	@ApiModelProperty(value="solution")
	private String solution;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="수신자수")
	private String userCount;
	@ApiModelProperty(value="수신자")
	private String name;	
	@ApiModelProperty(value="전화번호")
	private String tel;	
	@ApiModelProperty(value="이메일")
	private String email;
}