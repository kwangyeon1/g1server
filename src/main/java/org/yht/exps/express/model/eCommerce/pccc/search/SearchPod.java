package org.yht.exps.express.model.eCommerce.pccc.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchPod", description = "POD조회")
@Alias("searchPod")
public class SearchPod extends SearchList {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="생성구분", example="API")
	private String srchInputMethod;
	@ApiModelProperty(value="진행상태")
	private String srchPcccStatus;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="mrn")
	private String mrn;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회하려는 상위업체")
	private String srchCuslv1;
	@ApiModelProperty(value="조회하려는 하위업체")
	private String srchCuslv2;
	@ApiModelProperty(value="조회하려는 업체타입")
	private String srchCusType;
	@ApiModelProperty(value="로그인한 유저 명")
	private String logUserName;
	@ApiModelProperty(value="Pccc관리번호")
	private String srchCustomsIdJobNo;
	@ApiModelProperty(value="Pccc알람톡관리번호")
	private String srchPcccSetJobNo;
	@ApiModelProperty(value="로그인한 업체레벨")
	private String level;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="데이터조회하지않음")
	private String srchNoRecords;
	@ApiModelProperty(value="관리 / 아웃콜")
	private String dataType;
	@ApiModelProperty(value="비로그인 Y/N")
	private String noSessPodYn;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="boundType")
	private String boundType;
	@ApiModelProperty(value="멀티송장번호")
	private String multiDeliveryNo;
	
}