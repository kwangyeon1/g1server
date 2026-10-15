package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"returnCode", "returnMessage", "HUB_COD", "TML_COD", "TML_NAM", "DOM_MID",
		"CEN_COD","DOM_PDZ","CEN_NAM","ES_NAM","STML_COD","STML_NAM","PDZ_NAM","DOM_RGN"})
public class HanjinReturnConvertData{

	private String ReturnCode;
	private String ReturnMessage;
	
	private String HUB_COD;
	private String TML_COD;
	private String TML_NAM;
	private String DOM_MID;
	private String CEN_COD;
	private String DOM_PDZ;
	private String CEN_NAM;
	private String ES_NAM;
	private String STML_COD;
	private String STML_NAM;
	private String PDZ_NAM;
	private String DOM_RGN;
	
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
	@XmlElement(name = "HUB_COD")
	public String getHUB_COD() {
		return HUB_COD;
	}
	public void setHUB_COD(String hUB_COD) {
		HUB_COD = hUB_COD;
	}
	@XmlElement(name = "TML_COD")
	public String getTML_COD() {
		return TML_COD;
	}
	public void setTML_COD(String tML_COD) {
		TML_COD = tML_COD;
	}
	@XmlElement(name = "TML_NAM")
	public String getTML_NAM() {
		return TML_NAM;
	}
	public void setTML_NAM(String tML_NAM) {
		TML_NAM = tML_NAM;
	}
	@XmlElement(name = "DOM_MID")
	public String getDOM_MID() {
		return DOM_MID;
	}
	public void setDOM_MID(String dOM_MID) {
		DOM_MID = dOM_MID;
	}
	@XmlElement(name = "CEN_COD")
	public String getCEN_COD() {
		return CEN_COD;
	}
	public void setCEN_COD(String cEN_COD) {
		CEN_COD = cEN_COD;
	}
	@XmlElement(name = "DOM_PDZ")
	public String getDOM_PDZ() {
		return DOM_PDZ;
	}
	public void setDOM_PDZ(String dOM_PDZ) {
		DOM_PDZ = dOM_PDZ;
	}
	@XmlElement(name = "CEN_NAM")
	public String getCEN_NAM() {
		return CEN_NAM;
	}
	public void setCEN_NAM(String cEN_NAM) {
		CEN_NAM = cEN_NAM;
	}
	@XmlElement(name = "ES_NAM")
	public String getES_NAM() {
		return ES_NAM;
	}
	public void setES_NAM(String eS_NAM) {
		ES_NAM = eS_NAM;
	}
	@XmlElement(name = "STML_COD")
	public String getSTML_COD() {
		return STML_COD;
	}
	public void setSTML_COD(String sTML_COD) {
		STML_COD = sTML_COD;
	}
	@XmlElement(name = "STML_NAM")
	public String getSTML_NAM() {
		return STML_NAM;
	}
	public void setSTML_NAM(String sTML_NAM) {
		STML_NAM = sTML_NAM;
	}
	@XmlElement(name = "PDZ_NAM")
	public String getPDZ_NAM() {
		return PDZ_NAM;
	}
	public void setPDZ_NAM(String pDZ_NAM) {
		PDZ_NAM = pDZ_NAM;
	}
	@XmlElement(name = "DOM_RGN")
	public String getDOM_RGN() {
		return DOM_RGN;
	}
	public void setDOM_RGN(String dOM_RGN) {
		DOM_RGN = dOM_RGN;
	}

}


