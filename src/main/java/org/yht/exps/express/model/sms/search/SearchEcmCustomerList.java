package org.yht.exps.express.model.sms.search;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.SearchList;
import org.yht.exps.express.model.search.Sort;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchEcmCustomerList", description = "ECM 거래처 리스트 검색 조건")
@Alias("searchEcmCustomerList")
public class SearchEcmCustomerList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="검색거래처구분", example="거래처구분")
	private String srchCustomerType;
	@ApiModelProperty(value="검색거래처코드", example="거래처코드")
	private String srchCustomerCode;
	@ApiModelProperty(value="검색거래처명", example="거래처명")
	private String srchCustomerName;
	@ApiModelProperty(value="맵핑대기여부", example="Y")
	private String srchMappingWaitYn;
	@ApiModelProperty(value="상위업체구분", example="Y")
	private String srchUpperType;
	
	@ApiModelProperty(value="검색타입", example="ALL")
	private String srchType;
	@ApiModelProperty(value="도입사계약일자")
	private String contractDate;
	
	@ApiModelProperty(value="조회타입", example="excel")
	private String srchMode;
	@ApiModelProperty(value="그리드기본정렬", example="HBL_NO DESC")
	private String defaultSort;
	@ApiModelProperty(value="그리드기본정렬2", example="HBL_NO DESC")
	private String defaultSort2;
	@ApiModelProperty(value="그리드정렬")
	private List<Sort> sort;
	@ApiModelProperty(value="그리드페이지사이즈", example="50")
	private int pageSize;
	@ApiModelProperty(value="그리드페이지", example="2")
	private int page;
	@ApiModelProperty(value="그리드시작번호", example="51")
	private int startNum;
	@ApiModelProperty(value="그리드필터")
	private Filter filter;
}
