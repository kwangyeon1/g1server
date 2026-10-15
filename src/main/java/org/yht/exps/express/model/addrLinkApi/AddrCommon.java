package org.yht.exps.express.model.addrLinkApi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/*
 *  우편번호 헤더정보
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Common", propOrder = {
		"totalCount","currentPage","countPerPage","errorCode","errorMessage"
	}
)
public class AddrCommon {
	
	private String totalCount;
	private String currentPage;
	private String countPerPage;
	private String errorCode;
	private String errorMessage;
	
	public String getTotalCount() {
		if (this.totalCount == null) {
			return "";
		} else {
			return totalCount;
		}
	}
	public void setTotalCount(String totalCount) {
		this.totalCount = totalCount;
	}
	public String getCurrentPage() {
		if (this.currentPage == null) {
			return "";
		} else {
			return currentPage;
		}
	}
	public void setCurrentPage(String currentPage) {
		this.currentPage = currentPage;
	}
	public String getCountPerPage() {
		if (this.countPerPage == null) {
			return "";
		} else {
			return countPerPage;
		}
	}
	public void setCountPerPage(String countPerPage) {
		this.countPerPage = countPerPage;
	}
	public String getErrorCode() {
		if (this.errorCode == null) {
			return "";
		} else {
			return errorCode;
		}
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMessage() {
		if (this.errorMessage == null) {
			return "";
		} else {
			return errorMessage;
		}
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	

}	
