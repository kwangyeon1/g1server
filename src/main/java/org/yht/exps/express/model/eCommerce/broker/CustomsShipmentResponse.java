package org.yht.exps.express.model.eCommerce.broker;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "CustomsShipmentResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomsShipmentResponse {
	
	@XmlElement(name = "ResponseHeader")
	private ResponseHeader ResponseHeader;

	@XmlElement(name = "ErrorOrders")
    private ErrorOrders ErrorOrders;
	
	@Data
	@XmlRootElement(name = "ResponseHeader")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ResponseHeader {
		//‘success’ or ‘error’
		@XmlElement(name = "Result")
		private String Result;
		//응답 메시지
		@XmlElement(name = "Message")
		private String Message;
	}
	
	@Data
	@XmlRootElement(name = "ErrorOrders")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ErrorOrders {
		List<ErrorOrder> ErrorOrder;
	}
	
	@Data
	@XmlRootElement(name = "ErrorOrder")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ErrorOrder {
		//운송장번호
		@XmlElement(name = "DeliverNo")
		private String DeliverNo;
		//에러 상세 메시지
		@XmlElement(name = "ErrorMsg")
		private String ErrorMsg;
		
		private String temp;
	}
}
