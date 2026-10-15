package org.yht.exps.express.model.eCommerce.license.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchLicenseList", description = "전자상거래 면장 리스트 검색조건")
@Alias("searchLicenseList")
public class SearchLicenseList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="거래처코드")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="검색타입")
	private String srchLicenseType;
	@ApiModelProperty(value="검색텍스트")
	private String srchLicenseText;
	@ApiModelProperty(value="HBL NO")
	private String srchHblNo;
	@ApiModelProperty(value="오더관리번호")
	private String srchOdJobNo;
	@ApiModelProperty(value="거래처코드")
	private String srchCustomerCode;
	@ApiModelProperty(value="상위업체")
	private String srchCustCodeLv1;
	@ApiModelProperty(value="수신상태")
	private String srchResultStatus;
	@ApiModelProperty(value="송신상태")
	private String srchSendResultCode;
	
	@ApiModelProperty(value="면장관리번호")
	private String licenseJobNo;
	@ApiModelProperty(value="란번호")
	private String seqNoId;
	@ApiModelProperty(value="순번")
	private String cmditySeqNoId;
	@ApiModelProperty(value="HSCODE")
	private String hsCode;
	@ApiModelProperty(value="면장구분")
	private String licenseType;
	@ApiModelProperty(value="오더번호")
	private String poNo;
	@ApiModelProperty(value="면장 오더번호 리스트")
	private List<String> srchLicenseJobNoList;
	@ApiModelProperty(value="검색 리스트")
	private List<String> srchLicenseList;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;

	@ApiModelProperty(value="Bound")
	private String srchBound;
	@ApiModelProperty(value="BoundType")
	private String srchBoundType;
	@ApiModelProperty(value="목적국")
	private String srchEdiCityPort;
	
}
