package org.yht.exps.express.model.systemMng.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.SearchList;
import org.yht.exps.express.model.search.Sort;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchHubMainNoticeList", description = "eHub 메인 공지사항 리스트 검색 조건")
@Alias("searchHubMainNoticeList")
public class SearchHubMainNoticeList extends SearchList {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="검색 거래처코드")
	private String srchCustomerCode;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="조회타입", example="SYSTEM")
	private String srchNoticeType;
	@ApiModelProperty(value="관리번호")
	private String referenceNo;
	@ApiModelProperty(value="공지확정여부", example="")
	private String srchNoticeYn;
	@ApiModelProperty(value="날짜검색여부", example="")
	private String srchDateYn;

	@ApiModelProperty(value="사용자ID", example="aaaa")
	private String srchUserId;
	@ApiModelProperty(value="사용자구분", example="사용자구분")
	private String srchUserType;

	@ApiModelProperty(value="다국어타입", example="")
	private String srchLangType;

}
