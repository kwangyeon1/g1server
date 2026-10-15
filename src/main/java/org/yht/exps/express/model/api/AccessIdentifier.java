package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlType(name="AccessIdentifier", propOrder = {"senderIdentifier", "receiverIdentifier", "accessKey"})
public class AccessIdentifier{
	private String SenderIdentifier;
	private String ReceiverIdentifier;
	private String AccessKey;
	
	@XmlElement(name="SenderIdentifier")
	@JsonProperty("SenderIdentifier")
	public String getSenderIdentifier() {
		return SenderIdentifier;
	}
	public void setSenderIdentifier(String senderIdentifier) {
		SenderIdentifier = senderIdentifier;
	}
	@XmlElement(name="ReceiverIdentifier")
	@JsonProperty("ReceiverIdentifier")
	public String getReceiverIdentifier() {
		return ReceiverIdentifier;
	}
	public void setReceiverIdentifier(String receiverIdentifier) {
		ReceiverIdentifier = receiverIdentifier;
	}
	@XmlElement(name="AccessKey")
	@JsonProperty("AccessKey")
	public String getAccessKey() {
		return AccessKey;
	}
	public void setAccessKey(String accessKey) {
		AccessKey = accessKey;
	}
}