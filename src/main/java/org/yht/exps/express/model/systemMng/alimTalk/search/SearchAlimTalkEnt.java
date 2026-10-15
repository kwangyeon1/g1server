package org.yht.exps.express.model.systemMng.alimTalk.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchAlimTalkEnt", description = "전자상거래 알림톡 업체정보, 템플릿 리스트 검색조건")
@Alias("searchAlimTalkEnt")
public class SearchAlimTalkEnt extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	
	@ApiModelProperty(value="바운드", example="COMMON, AI, AO, OI , OO")
	private String srchBound;	
	
	@ApiModelProperty(value="템플릿 타입")
	private String srchTemplateType;
	@ApiModelProperty(value="시퀀스번호")
	private String srchTemplateSeqNo;
}
