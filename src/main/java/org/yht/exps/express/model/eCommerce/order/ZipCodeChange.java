package org.yht.exps.express.model.eCommerce.order;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ZipCodeChange", description = "전자상거래 우편번호변환")
@Alias("zipCodeChange")
public class ZipCodeChange {
	
	@ApiModelProperty(value="관리번호리스트")
	private List<String> odJobNoList;
	@ApiModelProperty(value="선택데이터 적하SubGrid여부")
	private String checkMfSubGridYn;
	@ApiModelProperty(value="업데이트대상 데이터리스트(적하SubGrid)")
	private List<Map<String, Object>> checkMfSubGridDataList;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="Master/House구분", example="H")
	private String blTypeMh;
	@ApiModelProperty(value="업데이트대상 조회조건")
	private SearchOrderList searchOrderList;
	@ApiModelProperty(value="업데이트대상 조회조건 - 등록,발송화면")
	private SearchOrderCustomerList searchOrderCustomerList;
	@ApiModelProperty(value="업데이트대상구분")
	private String targetData;
	
	@ApiModelProperty(value="호출화면 (등록,발송 화면에서 호출시 ORDER_CUSTOMER")
	private String screen;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
}