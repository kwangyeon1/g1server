package org.yht.exps.express.model.eCommerce.order.search;


import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchPrintOut", description = "출력물 정보 조회")
@Alias("searchPrintOut")
public class SearchPrintOut{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="거래처")
	private String srchCustomerCode;
	@ApiModelProperty(value="배송사 코드", example="우체국, 대한통운")
	private String srchDeliveryCode;	
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AO, AI, OO, OI")
	private String srchBoundType;		
	
	@ApiModelProperty(value="등록자아이디")
	private String registerUserId;
	@ApiModelProperty(value="출력이력체크여부")
	private String printCheckYn;
	@ApiModelProperty(value="관리번호")
	String[] odJobNoArr; 

	
	@ApiModelProperty(value="print form code")
	private String srchPrintFormCode;
	@ApiModelProperty(value="insertDataList")
	private List insertDataList;
	@ApiModelProperty(value="gmtTime")
	private int gmtTime;
}
