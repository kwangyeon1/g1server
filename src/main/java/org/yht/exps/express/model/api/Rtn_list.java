package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



@JsonIgnoreProperties(ignoreUnknown=true)//선언해준 필드 외에 필드는 제외
public class Rtn_list {
	
	private String rtnCd;
	private String fareSctCd;
	private String ordNo;
	private String invNo;
	private String ustRtgSctCd;
	private String rtnMsg;
	private String ordSct;
	
	public String getRtnCd() {
		return rtnCd;
	}
	public void setRtnCd(String rtnCd) {
		this.rtnCd = rtnCd;
	}
	public String getFareSctCd() {
		return fareSctCd;
	}
	public void setFareSctCd(String fareSctCd) {
		this.fareSctCd = fareSctCd;
	}
	public String getOrdNo() {
		return ordNo;
	}
	public void setOrdNo(String ordNo) {
		this.ordNo = ordNo;
	}
	public String getInvNo() {
		return invNo;
	}
	public void setInvNo(String invNo) {
		this.invNo = invNo;
	}
	public String getUstRtgSctCd() {
		return ustRtgSctCd;
	}
	public void setUstRtgSctCd(String ustRtgSctCd) {
		this.ustRtgSctCd = ustRtgSctCd;
	}
	public String getRtnMsg() {
		return rtnMsg;
	}
	public void setRtnMsg(String rtnMsg) {
		this.rtnMsg = rtnMsg;
	}
	public String getOrdSct() {
		return ordSct;
	}
	public void setOrdSct(String ordSct) {
		this.ordSct = ordSct;
	}
	@Override
	public String toString() {
		return "Rtn_list [rtnCd=" + rtnCd + ", fareSctCd=" + fareSctCd + ", ordNo=" + ordNo + ", invNo=" + invNo
				+ ", ustRtgSctCd=" + ustRtgSctCd + ", rtnMsg=" + rtnMsg + ", ordSct=" + ordSct + "]";
	}
	
	

}
