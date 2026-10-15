package org.yht.exps.express.model.bms.bmsAirDO.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchBmsAirDOList", description = "항공DO조회 파라미터")
@Alias("SearchBmsAirDOList")
public class SearchBmsAirDOList extends SearchList {

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
	@ApiModelProperty(value="발행취소여부")
	private String srchCancelYn;
	@ApiModelProperty(value="발행취소여부명(진행상태표시용)")
	private String srchCancelYnName;
	@ApiModelProperty(value="거래처구분")
	private String srchCustomerGuBun;
	@ApiModelProperty(value="검색거래처코드(화주)", example="거래처코드(화주)")
	private String srchCustomerCode;
	@ApiModelProperty(value="검색거래처명(화주)", example="거래처명(화주)")
	private String srchCustomerName;
	@ApiModelProperty(value="검색거래처코드(포워더)", example="거래처코드(포워더)")
	private String srchFwdCode;
	@ApiModelProperty(value="검색거래처명(포워더)", example="거래처명(포워더)")
	private String srchFwdName;
	@ApiModelProperty(value="검색거래처명(품명)", example="거래처명(품명)")
	private String srchCommodityItemLocName;
	@ApiModelProperty(value="엑셀파일명")
	private String excelTitle;
	@ApiModelProperty(value="엑셀다운여부")
	private String excelDownYn;

}