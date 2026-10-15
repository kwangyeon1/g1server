package org.yht.exps.express.model.eCommerce.broker;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.yht.exps.express.model.eCommerce.broker.CustomsShipmentResponse.ResponseHeader;

import lombok.Data;

@Data
@XmlRootElement(name = "CustomsInfoResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomsInfoResponse {
	
	@XmlElement(name = "ResponseHeader")
	private ResponseHeader ResponseHeader;
	
	@XmlElement(name = "CustomsInfos")
    private CustomsInfos CustomsInfos;
	
	@Data
	@XmlRootElement(name = "CustomsInfos")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class CustomsInfos {
		List<CustomsInfo> CustomsInfo;
	}
	
	@Data
	@XmlRootElement(name = "CustomsInfo")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class CustomsInfo {
		//운송장번호
		@XmlElement(name = "DeliverNo")
		private String DeliverNo;
		//통관 결과 값 (오류, 제출, 전제 등)
		@XmlElement(name = "CustomsResult")
		private String CustomsResult;
		//통관 특이사항 메모
		@XmlElement(name = "CustomsMemo")
		private String CustomsMemo;
		//협정 신고 여부 (Y/N)
		@XmlElement(name = "AgreementYN")
		private String AgreementYN;
		//거래 구분 (11, 15, 94 등 세관 신고거래구분)
		@XmlElement(name = "ElectTag")
		private String ElectTag;
		//분할 여부 (Y/N)
		@XmlElement(name = "DivideYN")
		private String DivideYN;
		//검역 여부 (Y/N)
		@XmlElement(name = "InspectionYN")
		private String InspectionYN;
		//납부서번호
		@XmlElement(name = "PaymentNo")
		private String PaymentNo;
		//관세 (KRW)
		@XmlElement(name = "Tax")
		private String Tax;
		//부가세 (KRW)
		@XmlElement(name = "Vat")
		private String Vat;
		//개별세 (KRW)
		@XmlElement(name = "specialconsumptiontax")
		private String specialconsumptiontax;
		//농특세 (KRW)
		@XmlElement(name = "ruraldevelopmenttax")
		private String ruraldevelopmenttax;
		//교육세 (KRW)
		@XmlElement(name = "Educationtax")
		private String Educationtax;
		//주세 (KRW)
		@XmlElement(name = "Liquortax")
		private String Liquortax;
	}
}
