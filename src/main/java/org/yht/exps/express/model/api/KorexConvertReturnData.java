package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"returnCode", "returnMessage", "dsfAddr", "bransNm", "empNm", "nickNm","korexNo1","korexNo2","korexNo3"})
public class KorexConvertReturnData{

	private String ReturnCode;
	private String ReturnMessage;
	private String DsfAddr;
	private String BransNm;
	private String EmpNm;
	private String NickNm;
	private String KorexNo1;
	private String KorexNo2;
	private String KorexNo3;
	
	@XmlElement(name = "ReturnCode")
	public String getReturnCode() {
		return ReturnCode;
	}
	public void setReturnCode(String returnCode) {
		ReturnCode = returnCode;
	}
	@XmlElement(name = "ReturnMessage")
	public String getReturnMessage() {
		return ReturnMessage;
	}
	public void setReturnMessage(String returnMessage) {
		ReturnMessage = returnMessage;
	}
	@XmlElement(name = "DsfAddr")
	public String getDsfAddr() {
		return DsfAddr;
	}
	public void setDsfAddr(String dsfAddr) {
		DsfAddr = dsfAddr;
	}
	@XmlElement(name = "BransNm")
	public String getBransNm() {
		return BransNm;
	}
	public void setBransNm(String bransNm) {
		BransNm = bransNm;
	}
	@XmlElement(name = "EmpNm")
	public String getEmpNm() {
		return EmpNm;
	}
	public void setEmpNm(String empNm) {
		EmpNm = empNm;
	}
	@XmlElement(name = "NickNm")
	public String getNickNm() {
		return NickNm;
	}
	public void setNickNm(String nickNm) {
		NickNm = nickNm;
	}
	@XmlElement(name = "KorexNo1")
	public String getKorexNo1() {
		return KorexNo1;
	}
	public void setKorexNo1(String korexNo1) {
		KorexNo1 = korexNo1;
	}
	@XmlElement(name = "KorexNo2")
	public String getKorexNo2() {
		return KorexNo2;
	}
	public void setKorexNo2(String korexNo2) {
		KorexNo2 = korexNo2;
	}
	@XmlElement(name = "KorexNo3")
	public String getKorexNo3() {
		return KorexNo3;
	}
	public void setKorexNo3(String korexNo3) {
		KorexNo3 = korexNo3;
	}
}


