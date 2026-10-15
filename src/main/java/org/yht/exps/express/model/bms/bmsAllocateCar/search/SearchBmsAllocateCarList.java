package org.yht.exps.express.model.bms.bmsAllocateCar.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchBmsAllocateCarList", description = "배차예정정보 조회 파라미터")
@Alias("SearchBmsAllocateCarList")
public class SearchBmsAllocateCarList extends SearchList {

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
	@ApiModelProperty(value="정정구분", example="신규(초기값): 1, 정정: 2, 취하: 3")
	private String srchChangeClass;
	@ApiModelProperty(value="반출입구분", example="반입: I, 반출: O")
	private String srchInoutClass;
	@ApiModelProperty(value="처리구분", example="미처리: N, 처리완료: Y")
	private String srchAllocateYn;
	@ApiModelProperty(value="차량검색구분")
	private String srchTruckGuBun;
	@ApiModelProperty(value="검색실차량번호", example="01가1234")
	private String srchRealTruckNo = "";
	@ApiModelProperty(value="검색수신차량번호", example="01가1234")
	private String srchCompanyTruckNo = "";
	@ApiModelProperty(value="검색차량기사명", example="김기사")
	private String srchCompanyTruckDriver = "";
	@ApiModelProperty(value="검색차량기사연락처", example="01012345678")
	private String srchCompanyTruckTel = "";
	@ApiModelProperty(value="엑셀파일명")
	private String excelTitle;
	@ApiModelProperty(value="엑셀다운여부")
	private String excelDownYn;

}