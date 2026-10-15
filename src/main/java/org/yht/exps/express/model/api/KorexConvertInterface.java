package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"receiverIdentifier", "senderIdentifier", "accessKey", "clntNum", "clntMgmCustCd", "prngDivCd","cgosts","address","zipcode"})
public class KorexConvertInterface{

	private String ReceiverIdentifier;
	private String SenderIdentifier;
	private String AccessKey;
	private String ClntNum;
	private String ClntMgmCustCd;
	private String PrngDivCd;
	private String Cgosts;
	private String Address;
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
	@XmlElement(name="ClntNum")
	public String getClntNum() {
		return ClntNum;
	}
	public void setClntNum(String clntNum) {
		ClntNum = clntNum;
	}
	@XmlElement(name="ClntMgmCustCd")
	public String getClntMgmCustCd() {
		return ClntMgmCustCd;
	}
	public void setClntMgmCustCd(String clntMgmCustCd) {
		ClntMgmCustCd =clntMgmCustCd;
	}
	@XmlElement(name="PrngDivCd")
	public String getPrngDivCd() {
		return PrngDivCd;
	}
	public void setPrngDivCd(String prngDivCd) {
		PrngDivCd = prngDivCd;
	}
	@XmlElement(name="Cgosts")
	public String getCgosts() {
		return Cgosts;
	}
	public void setCgosts(String cgosts) {
		Cgosts = cgosts;
	}
	@XmlElement(name="Address")
	public String getAddress() {
		return Address;
	}
	public void setAddress(String address) {
		Address = address;
	}
	@XmlElement(name="Zipcode")
	public String getZipcode() {
		return Zipcode;
	}
	public void setZipcode(String zipcode) {
		Zipcode = zipcode;
	}
}