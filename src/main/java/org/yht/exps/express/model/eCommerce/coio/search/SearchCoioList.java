package org.yht.exps.express.model.eCommerce.coio.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchCoioList", description = "전자상거래 반입/반출 조회")
@Alias("searchCoioList")
public class SearchCoioList extends SearchList {
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;	
	@ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
	private String srchNoLikeYn;
	@ApiModelProperty(value="조회업체")
	private String srchCus;
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	@ApiModelProperty(value="조회타입")
	private String srchType;
	@ApiModelProperty(value="조회 출/도착지 타입")
	private String srchOrigDestType;
	@ApiModelProperty(value="조회 출/도착지")
	private String srchOrigDest;	
	@ApiModelProperty(value="편명")
	private String srchVesselFlight;
	@ApiModelProperty(value="통관구분 리스트(멀티체크)")
	private List<String> srchCdTypeList;
	@ApiModelProperty(value="검사요청(멀티체크)", example="Y or N")
	private String srchInspectionYn;
	@ApiModelProperty(value="선별대상(멀티체크)", example="Y or N")
	private String srchSeInspectionYn;
	@ApiModelProperty(value="X-Ray(멀티체크)", example="Y or N")
	private String srchXrayYn;
	@ApiModelProperty(value="취하보류통보(멀티체크)", example="Y or N")
	private String srchDropYn;
	@ApiModelProperty(value="반출입구분 리스트(멀티체크)")
	private List<String> srchCarryList;
	@ApiModelProperty(value="통관완료여부", example="Y or N")
	private String srchClearanceYn;	
	@ApiModelProperty(value="홀드코드")
	private String srchHoldCode;
	@ApiModelProperty(value="조회 로케이션")
	private String srchWhLocation;		
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="SOLUTION", example="ECM")
	private String srchSolution;
	@ApiModelProperty(value="Master/House구분", example="H")
	private String srchBlTypeMh;
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AE")
	private String srchBoundType;
	@ApiModelProperty(value="콘솔여부", example="Y or N")
	private String srchConsolYn;
	@ApiModelProperty(value="마스터관리번호")
	private String srchModJobNo;
	@ApiModelProperty(value="관리번호")
	private String srchOdJobNo;
	@ApiModelProperty(value="대리점 코드")
	private String srchAgentCode;	
	@ApiModelProperty(value="검사,선별등 중복조회여부")
	private String srchMultiStatusYn;
	@ApiModelProperty(value="cus Like검색 여부", example="Y or N")
	private String srchCusLikeYn;
	@ApiModelProperty(value="출발지,도착지 Like검색 여부", example="Y or N")
	private String srchOrigDestLikeYn;
	@ApiModelProperty(value="수하인명")
	private String srchConsigneeName;
	@ApiModelProperty(value="배송사코드")
	private String srchDeliveryCode;
	@ApiModelProperty(value="관세사코드")
	private String srchCbCode;
	@ApiModelProperty(value="거래처코드")
	private String srchCustCodeLv1;
	@ApiModelProperty(value="배송상태")
	private String srchDeliveryStatus;
	
}
