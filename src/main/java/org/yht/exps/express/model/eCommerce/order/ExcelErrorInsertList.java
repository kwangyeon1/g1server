package org.yht.exps.express.model.eCommerce.order;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ExcelErrorInsertList", description = "엑셀업로드 오류내역 저장")
@Alias("excelErrorInsertList")
public class ExcelErrorInsertList {
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="사용자사업장코드")
	private String userEntBusinessCode;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자아이디")
	private String userId;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="처리상태")
	private String resultStatus;
	@ApiModelProperty(value="오류내역삭제리스트")
	private List<String> deleteDocumentList;
	@ApiModelProperty(value="오더리스트")
	private List<Order> orderList;
	@ApiModelProperty(value="오더컨테이너리스트")
	private List<OrderContainer> orderContainerList;
	@ApiModelProperty(value="오더수출면장리스트")
	private List<OrderLicense> orderLicenseList;
	@ApiModelProperty(value="오더아이템리스트")
	private List<OrderItem> orderItemList;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	@ApiModelProperty(value="성공 건수")
	private int count = 0;
}
