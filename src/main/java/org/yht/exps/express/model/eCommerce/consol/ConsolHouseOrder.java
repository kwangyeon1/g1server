package org.yht.exps.express.model.eCommerce.consol;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "ConsolHouseOrder", description = "전자상거래 Consol HouseOrder 리스트")
@Alias("consolHouseOrder")
@EqualsAndHashCode(callSuper=false)
public class ConsolHouseOrder extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="콘솔여부")
	private String consolYn;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="오더번호")
	private String poNo;
	@ApiModelProperty(value="통관_ID")
	private String customsId;
	@ApiModelProperty(value="통관_ID_구분")
	private String customsIdType;
	@ApiModelProperty(value="마스터관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="MAWB No")
	private String mblNo;
	@ApiModelProperty(value="HAWB No")
	private String hblNo;
	@ApiModelProperty(value="송하인")
	private String shipperName;
	@ApiModelProperty(value="수하인")
	private String consigneeName;
	@ApiModelProperty(value="수하인(한)")
	private String consigneeNameLoc;
	@ApiModelProperty(value="송하인주소")
	private String shipperAddress;
	@ApiModelProperty(value="수하인주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인_핸드폰")
	private String consigneeHp;
	@ApiModelProperty(value="수하인 국가코드")
	private String shipperCountryCode;
	@ApiModelProperty(value="통관_용도구분")
	private String cdUseType;
	@ApiModelProperty(value="거래구분")
	private String ecmType;	
	
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
	
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="금액")
	private double amountLoc;
	@ApiModelProperty(value="금액(외화)")
	private double amountCur;
	@ApiModelProperty(value="CBM")
	private double cbm;
	@ApiModelProperty(value="품명")
	private String commodityItem;
	@ApiModelProperty(value="통관구분")
	private String cdType;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="항공/해상구분")
	private String bound;
	@ApiModelProperty(value="수출/수입구분")
	private String boundType;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="편선명")
	private String vesselFlight;
	@ApiModelProperty(value="허용품목코드")
	private String allowedItemCode;
	@ApiModelProperty(value="voyage")
	private String voyage;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="전자상거래여부")
	private String ecmYn;
	
	@ApiModelProperty(value="하우스sum")
	private int sumCount;
	@ApiModelProperty(value="수량sum")
	private int sumPieces;
	@ApiModelProperty(value="중량sum")
	private double sumGwt;
	@ApiModelProperty(value="cbm sum")
	private double sumCbm;
	@ApiModelProperty(value="amount sum")
	private double sumAmount;
	/* cargo Manifest용 */
	@ApiModelProperty(value="유저명")
	private String userName;
	@ApiModelProperty(value="회사명")
	private String customerName;
	@ApiModelProperty(value="전화번호")
	private String userTel;
	@ApiModelProperty(value="팩스번호")
	private String userFax;
	@ApiModelProperty(value="출발지")
	private String origName;
	@ApiModelProperty(value="도착지")
	private String destName;
	@ApiModelProperty(value="도착지")
	private String containerInfo;
	
}
