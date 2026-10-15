package org.yht.exps.express.model.eCommerce.broker.search;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchBrokerBatchUpdate", description = "전자상거래 Broker 일괄변경")
@Alias("searchBrokerBatchUpdate")
public class SearchBrokerBatchUpdate{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="사업장코드", example="YHTXX")
	private String userBusinessCode;
	@ApiModelProperty(value="유저아이디", example="YHTXX")
	private String updateUserId;
	@ApiModelProperty(value="업데이트타입")
	private String updateType;
	@ApiModelProperty(value="업데이트대상구분")
	private String targetData;
	@ApiModelProperty(value="업데이트대상 관리번호리스트")
	private List<String> checkDataList;
	@ApiModelProperty(value="업데이트대상 조회조건")
	private SearchOrderList searchOrderList;
	@ApiModelProperty(value="업데이트컬럼")
	private String updateField;
	@ApiModelProperty(value="업데이트값")
	private String updateValue;
	@ApiModelProperty(value="통관지연사유코드")
	private String clearanceViolationCode;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="통관지연사유")
	private String clearanceViolationReason;
	@ApiModelProperty(value="BL구분")
	private String blTypeMh;
	
	@ApiModelProperty(value="관세사 리스트")
	private List<Map<String,Object>> customsOfficerList;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;	
	
}