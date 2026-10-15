package org.yht.exps.express.model.addrLinkApi;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/*
 * 우편번호 조회 결과정보
 */
@XmlRootElement(name = "results")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "results", propOrder = {
		"common","juso"
})

public class Results {
	
	@XmlElement(type = Juso.class)
	private List<Juso> juso;

	@XmlElement(type = AddrCommon.class)
	private AddrCommon common;

	public List<Juso> getJuso() {
		return juso;
	}

	public void setJuso(List<Juso> juso) {
		this.juso = juso;
	}

	public AddrCommon getCommon() {
		return common;
	}

	public void setCommon(AddrCommon common) {
		this.common = common;
	}
	
}
