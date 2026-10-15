package org.yht.exps.express.model.eCommerce.pccc;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "persEcmQryRtnVo")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomsCodeVerifiedInfo {
	
	@XmlElement(name = "ntceInfo")
	private String ntceInfo;
	
    @XmlElement(name = "tCnt")
    private String tCnt;
    
    @XmlElement(name = "valtPridStrtDt")
    private String valtPridStrtDt;
    @XmlElement(name = "valtPridXpirDt")
    private String valtPridXpirDt;
    
    
	@XmlElement(name = "persEcmQryRtnErrInfoVo")
	private List<PersEcmQryRtnErrInfo> persEcmQryRtnErrInfos;
	 
	@Data
	@XmlRootElement(name = "persEcmQryRtnErrInfoVo")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class PersEcmQryRtnErrInfo {
		@XmlElement(name = "errMsgCn")
		private String errMsgCn;
		
	}
}
