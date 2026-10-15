package org.yht.exps.express.model.eCommerce.broker;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "Order")
@XmlAccessorType(XmlAccessType.FIELD)
public class TempOrder {
		//운송장번호
	    @XmlElement(name = "DeliverNo")
	    private String DeliverNo;
	    //거래처 코드
	    @XmlElement(name = "CustomerId")
	    private String CustomerId;
	    //고객사 주문번호
	    @XmlElement(name = "OrderRefNo")
	    private String OrderRefNo;
	    //사이트 URL
	    @XmlElement(name = "SiteUrl")
	    private String SiteUrl;
	    //통관구분 (일반통관 값 고정 : G)
	    @XmlElement(name = "CustomsTag")
	    private String CustomsTag;
	    //인도조건 (FOB, CFR, DAP)
	    @XmlElement(name = "IncotermsTag")
	    private String IncotermsTag;
	    //청구지 (Duty paid, Collect, Third, Unpaid)
	    @XmlElement(name = "BillingTag")
	    private String BillingTag;
	    //사업자 의심 (Y/N)
	    @XmlElement(name = "CompanyYN")
	    private String CompanyYN;
	    //검역 의심 (Y/N)
	    @XmlElement(name = "InspectionYN")
	    private String InspectionYN;
	    //목록 취하 여부 (Y/N)
	    @XmlElement(name = "ListCustomsCancelYN")
	    private String ListCustomsCancelYN;
	    //목록 취하 상세 사유
	    @XmlElement(name = "ListCustomsCancelDetail")
	    private String ListCustomsCancelDetail;
	    //협정 의심 여부 (Y/N)
	    @XmlElement(name = "AgreementYN")
	    private String AgreementYN;
	    //총중량 (kg)
	    @XmlElement(name = "GrossWeight")
	    private double GrossWeight;
	    //패키지수량 (박스수량)
	    @XmlElement(name = "NumberOfPack")
	    private int NumberOfPack;
	    //Master B/L No
	    @XmlElement(name = "MblNo")
	    private String MblNo;
	    //입항일 (yyyyMMdd)
	    @XmlElement(name = "ArrivalDate")
	    private String ArrivalDate;
	    //출발지 국가 코드 (2 자리)
	    @XmlElement(name = "NationCode")
	    private String NationCode;
	    //항공 운송비 화폐
	    private String FlightCurrencyCode;
	    //항공 운송비
	    private int FlightAmount;
	    
	    @XmlElement(name = "Sender")
	    private Sender Sender;
	    @XmlElement(name = "Receiver")
	    private Receiver Receiver;
	    @XmlElement(name = "Items")
	    private List<Item> Items;
	
	@Data
	@XmlRootElement(name = "Sender")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Sender {
		//보내는 사람 영문 이름
	    @XmlElement(name = "Name")
	    private String Name;
	    //보내는 사람 영문 주소
	    @XmlElement(name = "Address")
	    private String Address;
	    //보내는 사람 전화번호
	    @XmlElement(name = "TelNo")
	    private String TelNo;
	    //전자상거래 유형 코드
	    @XmlElement(name = "Code")
	    private String Code;
	    //해외판매자 부호
	    @XmlElement(name = "RegistrationNo")
	    private String RegistrationNo;
	    //구매/배송대행업자 명
	    @XmlElement(name = "AgentName")
	    private String AgentName;
	    //구매/배송대행업자 부호
	    @XmlElement(name = "AgentRegistrationNo")
	    private String AgentRegistrationNo;
	    //판매중개자 명
	    @XmlElement(name = "BuyerName")
	    private String BuyerName;
	    //판매중개자 부호
	    @XmlElement(name = "BuyerRegistrationNo")
	    private String BuyerRegistrationNo;
	}
	
	@Data
	@XmlRootElement(name = "Receiver")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Receiver {
		//받는 사람 한글 이름
	    @XmlElement(name = "KorName")
	    private String KorName;
	    //최초 제공 성함
	    @XmlElement(name = "OriginName")
	    private String OriginName;
	    //받는 사람 통관고유부호
	    @XmlElement(name = "SocialSecurityNo")
	    private String SocialSecurityNo;
	    //받는 사람 우편번호
	    @XmlElement(name = "ZipNo")
	    private String ZipNo;
	    //받는 사람 주소
	    @XmlElement(name = "Address")
	    private String Address;
	    //받는 사람 전화 1
	    @XmlElement(name = "TelNo")
	    private String TelNo;
	    //받는 사람 전화 2
	    @XmlElement(name = "MobileNo")
	    private String MobileNo;
	    //받는 사람 E-mail
	    @XmlElement(name = "Email")
	    private String Email;
	}
	
	@Data
	@XmlRootElement(name = "Item")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Item {
		//상품코드
	    @XmlElement(name = "Code")
	    private String Code;
	    //상품명
	    @XmlElement(name = "Name")
	    private String Name;
	    //브랜드명
	    @XmlElement(name = "BrandName")
	    private String BrandName;
	    //대표품명
	    @XmlElement(name = "RepresentativeName")
	    private String RepresentativeName;
	    //세관제출품명
	    @XmlElement(name = "CustomsTradeName")
	    private String CustomsTradeName;
	    //수량
	    @XmlElement(name = "Quantity")
	    private int Quantity;
	    //라인별 무게
	    @XmlElement(name = "Weight")
	    private double Weight;
	    //상품총가격 (수량 x 단가)
	    @XmlElement(name = "UnitPrice")
	    private double UnitPrice;
	    //화폐 단위
	    @XmlElement(name = "CurrencyUnit")
	    private String CurrencyUnit;
	    //HsCode
	    @XmlElement(name = "HsCode")
	    private String HsCode;
	    //원산지 코드
	    @XmlElement(name = "CountryOrigin")
	    private String CountryOrigin;
	}
}
