package org.yht.exps.express.model.eCommerce.broker;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.exps.express.model.com.fileshare.FileShareFile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "BrokerMasterSub", description = "전자상거래 broker-M House 리스트")
@Alias("brokerMasterSub")
@EqualsAndHashCode(callSuper=false)
public class BrokerMasterSub extends ResultList{
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
	@ApiModelProperty(value="bound", example="AIR/SEA")
	private String bound;
	@ApiModelProperty(value="boundType", example="EXP/IMP")
	private String boundType;
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
	@ApiModelProperty(value="식품신고")
	private String foodYn;
	@ApiModelProperty(value="분할폐기")
	private String splitWaistYn;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="Order No")
	private String poNo;
	@ApiModelProperty(value="H.B/l No")
	private String hblNo;
	@ApiModelProperty(value="M.B/L No")
	private String mblNo;
	@ApiModelProperty(value="수화인")
	private String consigneeName;
	@ApiModelProperty(value="현재진행상태코드")
	private String lastPodStatus;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="CBM")
	private double cbm;
	@ApiModelProperty(value="금액")
	private double amountLoc;
	@ApiModelProperty(value="금액(외화)")
	private double amountCur;
	@ApiModelProperty(value="품명")
	private String commodityItem;
	
	@ApiModelProperty(value="통관상태명")
	private String clearanceStatusName;
	@ApiModelProperty(value="통관상태")
	private String clearanceStatus;
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
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="반입")
	private String carryinYn;
	@ApiModelProperty(value="FTA")
	private String ftaYn;
	@ApiModelProperty(value="안내")
	private String smsInformYn;
	@ApiModelProperty(value="sms전송일자")
	private String clearanceNoticeDate;
	
	@ApiModelProperty(value="부가세")
	private double surtax;
	@ApiModelProperty(value="통관수수료")
	private double clearanceFee;
	@ApiModelProperty(value="검역수수료")
	private double qurantineFee;
	@ApiModelProperty(value="기타세금")
	private double etcFee;
	@ApiModelProperty(value="세금대납")
	private String taxPayForYn;
	@ApiModelProperty(value="관세")
	private double tariff;
	@ApiModelProperty(value="송장출력여부")
	private String printYn;
	@ApiModelProperty(value="결제구분")
	//private String deliveryPaymentType;
	private String clearancePaymentType;
	@ApiModelProperty(value="상위업체")
	private String custNameLv1;
	@ApiModelProperty(value="하위업체")
	private String custNameLv2;
	@ApiModelProperty(value="파일공유 유무")
	private String fileShare;		
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;		
	@ApiModelProperty(value="관부가세안내여부")
	private String clearanceNoticeYn;
	
	@ApiModelProperty(value="청구여부")
	private String invoiceYn;
	
	@ApiModelProperty(value="수하인명(한)")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인주소(한)")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수하인전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수하인HP")
	private String consigneeHp;
	
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
	@ApiModelProperty(value="주문번호리스트")
	private String poNoList;
	
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
	
	/** 수입신고화면 추가 */
	@ApiModelProperty(value="세금합계")
	private double totalFee;
	@ApiModelProperty(value="관세사비고")
	private String tariffRemarks;
	@ApiModelProperty(value="입금일자")
	private String cbPaymentDateTime;
	@ApiModelProperty(value="전솧일자")
	private String cbSendDateTime;
	@ApiModelProperty(value="수리일자")
	private String clearanceDate;
	@ApiModelProperty(value="관세사 I/F")
	private String cbIfStatus;
	@ApiModelProperty(value="통관신고구분")
	private String cclType;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="수정자이름")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="도입사")
	private String entCode;
	@ApiModelProperty(value="파일명")
	private String fileName;
	@ApiModelProperty(value="파일이름")
	private String fileNameRemote;
	@ApiModelProperty(value="파일번호")
	private String referenceNoId;
	@ApiModelProperty(value="파일사이즈")
	private String fileSize;
	@ApiModelProperty(value="파일확장자")
	private String fileExt;
	
	@ApiModelProperty(value="반입_일자")
	private String carryinDate;
	@ApiModelProperty(value="반출_일자")
	private String carryoutDate;
	@ApiModelProperty(value="전자상거래여부")
	private String ecmYn;
	@ApiModelProperty(value="파일삭제여부")
	private String fileDeleteYn;
	@ApiModelProperty(value="파일리스트")
	private List<FileShareFile> fileShareFile;
}
