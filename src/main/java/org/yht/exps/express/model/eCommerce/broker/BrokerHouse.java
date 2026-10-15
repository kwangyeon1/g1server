package org.yht.exps.express.model.eCommerce.broker;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "BrokerHouse", description = "전자상거래 broker-H 리스트")
@Alias("brokerHouse")
public class BrokerHouse extends ResultList{
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="관세사")
	private String cbCode;
	@ApiModelProperty(value="관세사명")
	private String cbName;
	@ApiModelProperty(value="대리점")
	private String agentCode;
	@ApiModelProperty(value="화주")
	private String shipperCode;
	@ApiModelProperty(value="목록구분")
	private String cdType;
	@ApiModelProperty(value="용도구분")
	private String cdUseType;
	@ApiModelProperty(value="용도구분명")
	private String cdUseTypeName;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="거래구분명")
	private String ecmTypeName;
	@ApiModelProperty(value="검사")
	private String inspectionYn;
	@ApiModelProperty(value="선별")
	private String seInspectionYn;
	@ApiModelProperty(value="취하")
	private String dropYn;
	@ApiModelProperty(value="취하코드")
	private String dropCode;
	@ApiModelProperty(value="취하사유")
	private String dropReason;
	@ApiModelProperty(value="X-Ray")
	private String xrayYn;
	@ApiModelProperty(value="일검")
	private String geInspectionYn;
	@ApiModelProperty(value="검역")
	private String inspectionType;
	@ApiModelProperty(value="Order No")
	private String poNo;
	@ApiModelProperty(value="PO(오더) 일자")
	private String poDate;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="MAWB No")
	private String mblNo;
	@ApiModelProperty(value="HAWB No")
	private String hblNo;
	@ApiModelProperty(value="수화인")
	private String consigneeName;
	@ApiModelProperty(value="현재진행상태코드")
	private String lastPodStatus;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="중량")
	private double  gwt;
	@ApiModelProperty(value="CBM")
	private double cbm;
	@ApiModelProperty(value="금액(외화)")
	private double amountCur;
	@ApiModelProperty(value="금액")
	private double  amountLoc;
	@ApiModelProperty(value="품명")
	private String commodityItem;
	@ApiModelProperty(value="미통관코드")
	private String clearanceViolationCode;
	@ApiModelProperty(value="미통관사유")
	private String clearanceViolationReason;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="출발항")
	private String origCode;
	@ApiModelProperty(value="도착항")
	private String destCode;
	@ApiModelProperty(value="편명")
	private String vesselFlight;
	@ApiModelProperty(value="FTA")
	private String ftaYn;
	@ApiModelProperty(value="안내")
	//private String smsInformYn;
	private String clearanceNoticeYn;
	@ApiModelProperty(value="식품신고")
	private String foodYn;
	@ApiModelProperty(value="분할폐기")
	private String splitWaistYn;
	@ApiModelProperty(value="세금대납")
	private String taxPayForYn;
	@ApiModelProperty(value="송장출력여부")
	private String printYn;
	@ApiModelProperty(value="결제구분")
	//private String deliveryPaymentType;
	private String clearancePaymentType;
	@ApiModelProperty(value="상위업체")
	private String custNameLv1;
	@ApiModelProperty(value="하위업체")
	private String custNameLv2;
	@ApiModelProperty(value="통관상태")
	private String clearanceStatus;
	@ApiModelProperty(value="파일공유 유무")
	private String fileShare;	
	@ApiModelProperty(value="관세사 I/F")
	private String cbIfStatus;
	@ApiModelProperty(value="통관신고구분")
	private String cclType;
	
	@ApiModelProperty(value="bound", example="AIR/SEA")
	private String bound;
	@ApiModelProperty(value="boundType", example="EXP/IMP")
	private String boundType;
	
	@ApiModelProperty(value="수하인명(한)")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인HP")
	private String consigneeHp;
	@ApiModelProperty(value="sms전송일자")
	private String clearanceNoticeDate;
	@ApiModelProperty(value="수하인주소(한)")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수하인전화번호")
	private String consigneeTel;
	
	@ApiModelProperty(value="전자상거래유형")
	private String ecmBuyType;
	@ApiModelProperty(value="구매대행등록번호")
	private String agentRegNo;
	@ApiModelProperty(value="구매대행 업자명")
	private String agentName;
	@ApiModelProperty(value="해외판매자 부호")
	private String sellerRegNo;
	@ApiModelProperty(value="해외판매자 명")
	private String sellerName;
	@ApiModelProperty(value="판매중개자 부호")
	private String salesBrokerRegNo;
	@ApiModelProperty(value="판매중개자 명")
	private String salesBrokerName;
	@ApiModelProperty(value="주문번호 리스트")
	private String poNoList;
	@ApiModelProperty(value="홈페이지")
	private String url;
	@ApiModelProperty(value="운임조건")
	private String incoterms;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;
	@ApiModelProperty(value="화주_주소")
	private String shipperAddress;
	@ApiModelProperty(value="화주_전화번호")
	private String shipperTel;
	@ApiModelProperty(value="수하인_우편번호")
	private String consigneeZipcode;
	@ApiModelProperty(value="통관_ID")
	private String customsId;
	@ApiModelProperty(value="상세품목정보")
	private String itemsInfo;
	@ApiModelProperty(value="상세품목리스트")
	private List<Map<String, Object>> itemList;
	@ApiModelProperty(value="전자상거래여부")
	private String ecmYn;
	
	
	/**
	 * TODO 신고상태,처리,수수료청구
	 */
	/* 하우스기준 합계목록 */
	@ApiModelProperty(value="하우스sum")
	private int sumCount;
	@ApiModelProperty(value="수량sum")
	private int sumPieces;
	@ApiModelProperty(value="중량sum")
	private double sumGwt;
	@ApiModelProperty(value="신고전sum")
	private int cls010Cnt;
	@ApiModelProperty(value="신고의뢰sum")
	private int cls030Cnt;
	@ApiModelProperty(value="미통관sum")
	private int cls040Cnt;
	@ApiModelProperty(value="수리완료sum")
	private int cls050Cnt;
	
}
