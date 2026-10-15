package org.yht.exps.express.model.addrLinkApi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/*
 * 우편번호 주소 정보
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Juso", propOrder = {
		"roadAddr", "roadAddrPart1", "roadAddrPart2", "jibunAddr", "engAddr", "zipNo", "admCd"
		,"rnMgtSn", "bdMgtSn", "detBdNmList", "korAddr"
	}
)
public class Juso {
	private String roadAddr;
	private String roadAddrPart1;
	private String roadAddrPart2;
	private String jibunAddr;
	private String engAddr;
	private String zipNo;
	private String admCd;
	private String rnMgtSn;
	private String bdMgtSn;
	private String detBdNmList;
	private String korAddr;
	
	public String getRoadAddr() {
		if (this.roadAddr == null) {
			return "";
		} else {
			return roadAddr;
		}
	}
	public void setRoadAddr(String roadAddr) {
		this.roadAddr = roadAddr;
	}
	public String getRoadAddrPart1() {
		if (this.roadAddrPart1 == null) {
			return "";
		} else {
			return roadAddrPart1;
		}
	}
	public void setRoadAddrPart1(String roadAddrPart1) {
		this.roadAddrPart1 = roadAddrPart1;
	}
	public String getRoadAddrPart2() {
		if (this.roadAddrPart2 == null) {
			return "";
		} else {
			return roadAddrPart2;
		}
	}
	public void setRoadAddrPart2(String roadAddrPart2) {
		this.roadAddrPart2 = roadAddrPart2;
	}
	public String getJibunAddr() {
		if (this.jibunAddr == null) {
			return "";
		} else {
			return jibunAddr;
		}
	}
	public void setJibunAddr(String jibunAddr) {
		this.jibunAddr = jibunAddr;
	}
	public String getEngAddr() {
		if (this.engAddr == null) {
			return "";
		} else {
			return engAddr;
		}
	}
	public void setEngAddr(String engAddr) {
		this.engAddr = engAddr;
	}
	public String getZipNo() {
		if (this.zipNo == null) {
			return "";
		} else {
			return zipNo;
		}
	}
	public void setZipNo(String zipNo) {
		this.zipNo = zipNo;
	}
	public String getAdmCd() {
		if (this.admCd == null) {
			return "";
		} else {
			return admCd;
		}
	}
	public void setAdmCd(String admCd) {
		this.admCd = admCd;
	}
	public String getRnMgtSn() {
		if (this.rnMgtSn == null) {
			return "";
		} else {
			return rnMgtSn;
		}
	}
	public void setRnMgtSn(String rnMgtSn) {
		this.rnMgtSn = rnMgtSn;
	}
	public String getBdMgtSn() {
		if (this.bdMgtSn == null) {
			return "";
		} else {
			return bdMgtSn;
		}
	}
	public void setBdMgtSn(String bdMgtSn) {
		this.bdMgtSn = bdMgtSn;
	}
	public String getDetBdNmList() {
		if (this.detBdNmList == null) {
			return "";
		} else {
			return detBdNmList;
		}
	}
	public void setDetBdNmList(String detBdNmList) {
		this.detBdNmList = detBdNmList;
	}
	
	public String getKorAddr() {
		if (this.korAddr == null) {
			return "";
		} else {
			return korAddr;
		}
	}
	public void setKorAddr(String korAddr) {
		this.korAddr = korAddr;
	}
	@Override
	public String toString() {
		return "Juso [roadAddr=" + roadAddr + ", roadAddrPart1=" + roadAddrPart1 + ", roadAddrPart2=" + roadAddrPart2
				+ ", jibunAddr=" + jibunAddr + ", engAddr=" + engAddr + ", zipNo=" + zipNo + ", admCd=" + admCd
				+ ", rnMgtSn=" + rnMgtSn + ", bdMgtSn=" + bdMgtSn + ", detBdNmList=" + detBdNmList + ", korAddr="
				+ korAddr + "]";
	}
	
}	
