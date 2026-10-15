package org.yht.exps.express.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "custom")
@Data
public class CustomProperties {
//	String[] ernterprises;
	String[] systemDbList;
	String[] entDbList;
	String fileUploadDir;
	String dbUrlMaster;
	String dbPwdMaster;
	String dbPwdExps;
	String dbUrlBase;
	String dbUrlBase2;
	String dbUrlXray;
	String dbPwdXray;
	String dbUrlNewbilling;
	String dbPwdNewbilling;
	String dbUrlSejong;
	String dbUrlKorex;
	String dbPwdKorex;
	String dbUrlKorexadd1;
	String dbUrlKorexadd2;
	String dbPwdKorexadd;
	String dbUrlHanjin;
	String excelTemplate;
	String excelDownload;
	String enterpriseDir;
	String interfaceDir;
	String backupDir;
	String xsdDir;
    String dbUrlCwhp;
    String dbPwdCwhp;
	int elasticsearchScrollPage;
	
	/** 적하목록전송 */
	String ediSendYn;
	
	/** ebMS RESTful Service API */
	String ebmsIp;
	String ebmsPort;
	String ebmsUrl;
	String ebmsAccessKey;
	String ebmsFtpPw;
	String ebmsFtpPath;
	
	/** HMS RESTful Service API */
	String hmsUrl;
	String hmsClientId;
	String hmsClientSecret;
	
	// interface 스키마 (XSD)  로컬 임시 
	String apiCustomsXsd;
	
	// interface 경로 (XML) 
	String apiCustomsXml;	
	
	/* 우체국 전송 경로 */
	String exAgentPath;
	String exAgentSend;
	
	// 롯데택배 API URL
	String lotteUrl;
	
	String preUrl;
	String interfaceUrl;
	
	String esaypayProtocol;
	String esaypayUrl;
	
	String errFileDir;
	String managerEmail;
	
	// ubi report 관련
	String labelDir;
	String reportUrl;
	String reportDomain;
	String reportJrfTopLoc;
	String reportFontLoc;
	String reportDs;
	
	/** Q&A 파일경로 */
	String fileQnADir;
}
