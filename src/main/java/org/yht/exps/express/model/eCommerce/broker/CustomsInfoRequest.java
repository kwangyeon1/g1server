package org.yht.exps.express.model.eCommerce.broker;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.yht.exps.express.model.eCommerce.broker.CustomsShipmentRequest.RequestHeader;

import lombok.Data;

@Data
@XmlRootElement(name = "CustomsInfoRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomsInfoRequest {
	
	@XmlElement(name = "RequestHeader")
	private RequestHeader requestHeader;
	
	@XmlElement(name = "DeliverNos")
    private DeliverNos deliverNos;
	
	@Data
	@XmlRootElement(name = "DeliverNos")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class DeliverNos {
		@XmlElement(name = "DeliverNo")
		private List<String> deliverNo;
	}
}
