package org.yht.exps.express.model.systemMng.alimTalk.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchAlimTalkTemplate", description = "시스템관리 알림톡 템플릿 리스트 검색조건")
@Alias("searchAlimTalkTemplate")
public class SearchAlimTalkTemplate extends SearchList {
	
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	
	@ApiModelProperty(value="템플릿타입")
	private String srchTemplateType;
	@ApiModelProperty(value="템플릿타입시퀀스번호")
	private String srchTemplateSeqNo;
	
}
