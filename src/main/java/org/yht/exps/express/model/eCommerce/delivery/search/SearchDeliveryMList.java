package org.yht.exps.express.model.eCommerce.delivery.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchDeliveryMList", description = "전자상거래 Delivery-M 리스트 검색조건")
@Alias("searchDeliveryMList")
public class SearchDeliveryMList extends SearchList {
	@ApiModelProperty(value="배송상태")
	private String srchStatus;
	@ApiModelProperty(value="배송사 코드", example="우체국, 대한통운")
	private String srchDeliveryCode;	
	@ApiModelProperty(value="비용구분")
	private String srchDeliveryPaymentType; 
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;		
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	@ApiModelProperty(value="조회업체")
	private String srchCus;
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="MBL No")
	private String srchMblNo;
	@ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
	private String srchNoLikeYn;
	@ApiModelProperty(value="")
	private String srchGroupCode;
	@ApiModelProperty(value="오더번호 리스트")
	private List<String> srchOdJobNoList;
	@ApiModelProperty(value="마스터관리번호")
	private String srchMasterOdJobNo;
	@ApiModelProperty(value="마스터관리번호리스트")
	private String srchMasterOdJobNoListStr;
	@ApiModelProperty(value="조회대상 Like검색 여부", example="Y or N")
	private String srchCusLikeYn;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="엑셀 템플릿 코드")
	private String documentCode;
	@ApiModelProperty(value="엑셀 문서명")
	private String documentName;
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AO")
	private String srchBoundType;	
	@ApiModelProperty(value="체크로 선택된 건 리스트")
	private List<String> dataList;
	@ApiModelProperty(value="쿼리에서 Master(MASTER_OD_JOB_NO) /House구분(OD_JOB_NO)", example="H")
	private String blTypeMh;	
	@ApiModelProperty(value="배송상태")
	private String deliveryStatus;	
	@ApiModelProperty(value="배송사지정유무")
	private String deliveryCodeYn;	
	@ApiModelProperty(value="멀티운송장여부", example="Y or N")
	private String srchMultiYn;
	@ApiModelProperty(value="반품여부", example="Y or N")
	private String srchReturnYn;
	@ApiModelProperty(value="profile명")
	private String profileName;
	@ApiModelProperty(value="배송상태")
	private String srchDeliveryStatus;
	
}
