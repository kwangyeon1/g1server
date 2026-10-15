package org.yht.exps.express.model.eCommerce.license.search;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchLicenseBatchUpdate", description = "전자상거래 면장 리스트 검색조건")
@Alias("searchLicenseBatchUpdate")
public class SearchLicenseBatchUpdate extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="거래처코드")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="업데이트대상 관리번호리스트")
	private List<String> checkDataList;
	@ApiModelProperty(value="업데이트대상구분")
	private String targetData;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
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
	@ApiModelProperty(value="결과조회상태")
	private String resultStatus;
	@ApiModelProperty(value="결과오류사유")
	private String resultErrorReason;
	@ApiModelProperty(value="면장 오더번호 리스트")
	private List<String> srchLicenseJobNoList;
	@ApiModelProperty(value="검색 리스트")
//	private List<Map<String,Object>> srchLicenseList;
	private List<String> srchLicenseList;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;

	@ApiModelProperty(value="Bound")
	private String srchBound;
	@ApiModelProperty(value="BoundType")
	private String srchBoundType;
	@ApiModelProperty(value="목적국")
	private String srchEdiCityPort;
	
	@ApiModelProperty(value="수정자")
	private String 	updateUserId;
	
	@ApiModelProperty(value="상위업체API KEY")
	private String apiConsumerKey;
	@ApiModelProperty(value="상위업체API ID")
	private String apiConsumerId;
}
