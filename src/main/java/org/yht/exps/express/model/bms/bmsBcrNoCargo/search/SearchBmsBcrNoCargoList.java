package org.yht.exps.express.model.bms.bmsBcrNoCargo.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;
import org.yht.util.Util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchBmsBcrNoCargoList", description = "BCR 무적 조회 파라미터")
@Alias("SearchBmsBcrNoCargoList")
public class SearchBmsBcrNoCargoList extends SearchList {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회번호 LIKE 검색 유무")
	private String srchNoLikeYn;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="조회일자 구분")
	private String srchDateGbn;
	@ApiModelProperty(value="조회 시작일자")
	private String srchStartDate;
	@ApiModelProperty(value="조회 종료일자")
	private String srchEndDate;
	@ApiModelProperty(value="조회일자타입", example="dateTime")
	private String srchDateType;
	@ApiModelProperty(value="조회체크완료여부", example="미체크: N, 체크완료: Y")
	private String srchChangeClass;
	@ApiModelProperty(value="조회 사용자 ID")
	private String srchUserId;
	@ApiModelProperty(value="엑셀파일명")
	private String excelTitle;
	@ApiModelProperty(value="엑셀다운여부")
	private String excelDownYn;

	/* Set Multi Search Data */
	public void setSrchNoData(String scanNo, int page, int pageSize) {
		if( "".equals(Util.null2str(scanNo)) ) return;

		searchNoList = new ArrayList<String>();
		searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(scanNo)).split(",")));

		if( searchNoList.size() > 1 ) {
			searchType = "multi";
		} else {
			searchType = StringUtils.EMPTY;
			srchNo = Util.null2str(scanNo).trim().replace(",", "");
		}
	}
}