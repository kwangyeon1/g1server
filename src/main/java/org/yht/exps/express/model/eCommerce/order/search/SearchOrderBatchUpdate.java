package org.yht.exps.express.model.eCommerce.order.search;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchOrderBatchUpdate", description = "전자상거래 일괄변경")
@Alias("searchOrderBatchUpdate")
public class SearchOrderBatchUpdate{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="업데이트타입")
	private String updateType;
	@ApiModelProperty(value="업데이트대상구분")
	private String targetData;
	@ApiModelProperty(value="업데이트대상 관리번호리스트")
	private List<String> checkDataList;
	@ApiModelProperty(value="선택데이터M/H구분")
	private String checkDataTypeMh;
	@ApiModelProperty(value="선택데이터 적하SubGrid여부")
	private String checkMfSubGridYn;
	@ApiModelProperty(value="업데이트대상 데이터리스트(적하SubGrid)")
	private List<Map<String, Object>> checkMfSubGridDataList;
	@ApiModelProperty(value="업데이트대상 조회조건")
	private SearchOrderList searchOrderList;
	
	@ApiModelProperty(value="업데이트대상 조회조건-등록,발송")
	private SearchOrderCustomerList searchOrderCustomerList;
	
	@ApiModelProperty(value="업데이트컬럼")
	private String updateField;
	@ApiModelProperty(value="업데이트값")
	private String updateValue;
	@ApiModelProperty(value="업데이트컬럼2")
	private String updateField2;
	@ApiModelProperty(value="업데이트값2")
	private String updateValue2;
	@ApiModelProperty(value="컨테이너")
	private List<SearchOrderBatchUpdateCntr> containerList;
	@ApiModelProperty(value="주문번호")
	private List<String> poInfoList;
	
	/** 컨테이너정보 */
	@ApiModelProperty(value="하우스수량적용여부")
	private boolean containerPiecesYn;
	@ApiModelProperty(value="컨테이너안분여부")
	private boolean containerDivideYn;
	
	/** 홀드 */
	@ApiModelProperty(value="홀드")
	private Map<String, Object> holdMap;
	
	@ApiModelProperty(value="House 안분여부")
	private boolean divideFlag;
	
	/** 이례사항 */
	@ApiModelProperty(value="이례사항")
	private Map<String, Object> unusualMap;
	
	/** UPS 선별자,확인자,XRAY판독자 **/
	@ApiModelProperty(value="목록선별자")
	private String selectorUserId;
	@ApiModelProperty(value="목록선별자이름")
	private String selectorUserName;
	@ApiModelProperty(value="확인자")
	private String confirmerUserId;
	@ApiModelProperty(value="확인자이름")
	private String confirmerUserName;
	@ApiModelProperty(value="XRAY판독자1")
	private String xrayReader1Name;
	@ApiModelProperty(value="XRAY판독자2")
	private String xrayReader2Name;
	
	/** 저장 */
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정자권한")
	private String userAuth;
	@ApiModelProperty(value="GMT_TIME")
	private String gmtTime;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="암호화여부(동적쿼리)")
	private String dynamicCryptographyYn;
	@ApiModelProperty(value="암호화여부(업데이트동적쿼리)")
	private String updateCryptographyYn;
	
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
