package org.yht.exps.express.model.systemMng.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;
import org.yht.exps.express.model.systemMng.AlarmGroup;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchAlarmGroup", description = "알림 서비스 조회")
@Alias("searchAlarmGroup")
public class SearchAlarmGroup extends SearchList{
	
	@ApiModelProperty(value="GroupId")
	private String srchGroupId;
	@ApiModelProperty(value="수신자명")
	private String srchName;
	@ApiModelProperty(value="수신자전화번호")
	private String srchTel;
	@ApiModelProperty(value="수신자이메일")
	private String srchEmail;
	@ApiModelProperty(value="전송구분")
	private String srchAlarmType;
	@ApiModelProperty(value="일련번호")
	private String srchSeqNoId;
	@ApiModelProperty(value="도입사코드")
	private String srchEntCode;
	@ApiModelProperty(value="처리타입")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="리스트")
	private List<AlarmGroup> alarmGroupList;
}