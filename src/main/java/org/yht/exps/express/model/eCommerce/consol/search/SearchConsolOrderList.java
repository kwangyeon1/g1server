package org.yht.exps.express.model.eCommerce.consol.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchConsolOrderList", description = "MasterOrder조회")
@Alias("searchConsolOrderList")
public class SearchConsolOrderList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="콘솔여부", example="Y or N")
	private String srchConsolYn;
	@ApiModelProperty(value="SOLUTION", example="ECM")
	private String srchSolution;
	@ApiModelProperty(value="항공/해상 구분")
	private String srchBound;
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AO")
	private String srchBoundType;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회번호(하우스)")
	private String srchNoHouse;
	@ApiModelProperty(value="조회번호타입(하우스)")
	private String srchNoTypeHouse;
	@ApiModelProperty(value="조회하려는 상위업체")
	private String srchCuslv1;
	@ApiModelProperty(value="조회하려는 하위업체")
	private String srchCuslv2;
	@ApiModelProperty(value="조회하려는 업체타입")
	private String srchCusType;
	@ApiModelProperty(value="로그인한 유저 명")
	private String logUserName;
	@ApiModelProperty(value="마스터관리번호")
	private String srchMasterOdJobNo;
	@ApiModelProperty(value="통관목록제출번호")
	private String srchSimpleDocNo;
	@ApiModelProperty(value="로그인한 업체코드")
	private String srchEcmCustomerCode;
	@ApiModelProperty(value="로그인한 업체레벨")
	private String level;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="하우스조회시작일", example="2021-01-01")
	private String srchHouseStartDate;
	@ApiModelProperty(value="하우스조회종료일", example="2021-01-01")
	private String srchHouseEndDate;
	@ApiModelProperty(value="하우스입력자", example="YHTXXKRadmin")
	private String srchHouseRegistUser;
	@ApiModelProperty(value="데이터조회하지않음")
	private String srchNoRecords;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchTypeHouse;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoHouseList;
	
	@ApiModelProperty(value="출발지")
	private String srchOrigCode;
	@ApiModelProperty(value="도착지")
	private String srchDestCode;
	
}