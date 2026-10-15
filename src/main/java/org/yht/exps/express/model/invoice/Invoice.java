package org.yht.exps.express.model.invoice;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.invoice.search.SearchInvoiceList;
import org.yht.util.Util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ApiModel(value = "Invoice", description = "전자상거래 정산관리 택배사")
@Alias("invoice")
@EqualsAndHashCode(callSuper=false)
public class Invoice implements Cloneable{
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="정산일자")
	private String invoiceDate;
	@ApiModelProperty(value="입출항일자")
	private String obarDate;
	@ApiModelProperty(value="정산번호")
	private String invoiceNo;
	@ApiModelProperty(value="정산처코드")
	private String accountCode;
	@ApiModelProperty(value="정산처명")
	private String accountName;
	@ApiModelProperty(value="담당자ID")
	private String accountAttnId;
	@ApiModelProperty(value="담당자명")
	private String accountAttn;
	@ApiModelProperty(value="계산서 담당자 메일전송여부")
	private String accountAttnEbillYn;
	@ApiModelProperty(value="담당자전화번호")
	private String accountTel;
	@ApiModelProperty(value="담당자팩스")
	private String accountFax;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="정산서관리번호")
	private String invoiceJobNo;
	@ApiModelProperty(value="고객정산번호")
	private String invoiceNoCustomer;
	@ApiModelProperty(value="오더관리번호리스트")
	private List<String> odJobNoList;
	@ApiModelProperty(value="추가비용관리번호리스트")
	private List<OrderCostJobNo> orderCostJobNoList;
	@ApiModelProperty(value="정산서타입")
	private String invoiceType;
	@ApiModelProperty(value="국내/해외")
	private String localOversea;
	@ApiModelProperty(value="매출/매입")
	private String arAp;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="ECM odChgInv 사용여부")
	private String ecmOdChgYn;
	@ApiModelProperty(value="MBL NO")
	private String mblNo;
	@ApiModelProperty(value="HBL NO")
	private String hblNo;
	
	@ApiModelProperty(value="조회조건")
	private SearchInvoiceList searchInvoiceList;
	@ApiModelProperty(value="ECM odChgInv 리스트")
	private List<Map<String, Object>> ecmOdChgList;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
	public Invoice clone() throws CloneNotSupportedException{
		Invoice vo = null;
		try {
			vo = (Invoice) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(Util.getPrintStacTraceString(e));
			throw e;
		}
		return vo;
	}
	
}
