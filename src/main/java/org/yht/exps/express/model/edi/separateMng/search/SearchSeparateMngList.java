package org.yht.exps.express.model.edi.separateMng.search;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.edi.separateMng.SeparateMng;
import org.yht.exps.express.model.search.SearchList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchSeparateMngList", description = "전자상거래 별도관리물품해제신고 리스트 검색 조건")
@Alias("searchSeparateMngList")
public class SearchSeparateMngList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="검색타입")
	private String srchNoType;
	@ApiModelProperty(value="검색text")
	private String srchNo;
	@ApiModelProperty(value="자료생성구분")
	private String srchDataGen;
	@ApiModelProperty(value="신고여부")
	private String srchSendStatus;
	@ApiModelProperty(value="신고여부")
	private String srchCustomsCode;
	
	@ApiModelProperty(value="통관구분")
	private String srchCdType;
	@ApiModelProperty(value="별도관리물품해제신고_제출번호")
	private String ediDocNo;
	
	@ApiModelProperty(value="오더 리스트 json String")
	private String deliveryChgListStr;	
	
	@ApiModelProperty(value="오더 리스트")
	private List<SeparateMng> separateMngList;	
	
	
}
