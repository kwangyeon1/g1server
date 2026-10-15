package org.yht.exps.express.model.eCommerce.pod.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchPodList", description = "PCCC조회")
@Alias("SearchPodList")
public class SearchPodList extends SearchList {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="BL구분")
	private String srchBoundTypeDetTab;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회하려는 상위업체")
	private String srchCuslv1;
	@ApiModelProperty(value="조회하려는 하위업체")
	private String srchCuslv2;
	@ApiModelProperty(value="반품YN")
	private String srchReturnYn;
	@ApiModelProperty(value="반송장유무")
	private String srchReturnDeliveryYn;
	@ApiModelProperty(value="배송사")
	private String srchDelivery;
	@ApiModelProperty(value="로그인한 유저 명")
	private String logUserName;
	@ApiModelProperty(value="로그인한 업체레벨")
	private String level;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="데이터조회하지않음")
	private String srchNoRecords;
	@ApiModelProperty(value="관리 / 아웃콜")
	private String dataType;
	@ApiModelProperty(value="action")
	private String action;
	@ApiModelProperty(value="업데이트 유저ID")
	private String updateUserId;
	@ApiModelProperty(value="메뉴ID")
	private String menuId;
	@ApiModelProperty(value="로그인거래처코드id")
	private String loginCustomerCd;
	@ApiModelProperty(value="로그인거래처Lev")
	private String loginCustomerLev;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="진행상태")
	private List<String> srchStatusList;
	
}