package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"receiverIdentifier", "senderIdentifier", "accessKey","SZipcode", "zipcode"})
public class HanjinConvertInterface{

	private String ReceiverIdentifier;
	private String SenderIdentifier;
	private String AccessKey;
	private String SZipcode;
	private String Zipcode;
	
	@XmlElement(name="ReceiverIdentifier")
	public String getReceiverIdentifier() {
		return ReceiverIdentifier;
	}
	public void setReceiverIdentifier(String receiverIdentifier) {
		ReceiverIdentifier = receiverIdentifier;
	}
	@XmlElement(name="SenderIdentifier")
	public String getSenderIdentifier() {
		return SenderIdentifier;
	}
	public void setSenderIdentifier(String senderIdentifier) {
		SenderIdentifier = senderIdentifier;
	}
	@XmlElement(name="AccessKey")
	public String getAccessKey() {
		return AccessKey;
	}
	public void setAccessKey(String accessKey) {
		AccessKey = accessKey;
	}
	@XmlElement(name="SZipcode")
	public String getSZipcode() {
		return SZipcode;
	}
	public void setSZipcode(String sZipcode) {
		SZipcode = sZipcode;
	}
	@XmlElement(name="Zipcode")
	public String getZipcode() {
		return Zipcode;
	}
	public void setZipcode(String zipcode) {
		Zipcode = zipcode;
	}
	
}