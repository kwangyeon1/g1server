package org.yht;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CatchMailSend {

	@Autowired
	CustomProperties customProperties;

	//로컬,테스트,운영 구분
	@Value("${spring.profiles.active}")
	private String profile;

	//application.yml 파일에 정의된 메일 전송 관련 설정값들
	@Value("${spring.mail.error.host}")
	private String host;

	@Value("${spring.mail.error.port}")
	private int port;

	@Value("${spring.mail.error.username}")
	private String username;

	@Value("${spring.mail.error.password}")
	private String password;

	//배치 메소드 내 catch문에서 호출 - catch문에서 처리되는 예외에 대한 메일 전송
	//발생한 예외, stuff(클래스명+메소드명) 파라미터로 받음
	public void exceptionHandle(Exception e, String stuff){

		if(!"local".equals(profile) && !"dev".equals(profile) && !"ups1".equals(profile) && !"ups2".equals(profile) ) {

	    	if(e != null){

	    		String cause = "";
			    try{
					cause = e.getCause().toString();
				}catch(Exception ex){
					cause = e.toString();
				}

			    String exceptMessage = "";

			    try{
			    	exceptMessage = e.getMessage().toString();
				}catch(Exception ex){
					exceptMessage = Util.getPrintStacTraceString(e);
				}

			    //메일 전송
				StringBuffer content = new StringBuffer();
				content.append("<b>Method :</b> " + stuff + "<br/>");
				content.append("<b>Cause :</b> " + cause + "<br/>");
				content.append("<b>Exception Message :</b><br/> " + exceptMessage);

			    log.debug("cause : " + cause);
			    log.debug("stuff : " + stuff);
			    log.debug("exceptMessage : " + exceptMessage);

			    String serviceCode = "";
			    if(stuff.contains(".cargo.")) {
			    	serviceCode = "CARGO";
			    }else if(stuff.contains(".customs.")) {
			    	serviceCode = "CUSTOMS";
			    }else if(stuff.contains(".gw.edi.")) {
			    	serviceCode = "GW_EDI";
			    }else if(stuff.contains(".bms.")) {
					serviceCode = "BMS";
				}

			    // 위 내용을 조합하여 별도 파일 생성 또는 메일을 전송한다.
			    MailSmtpSender sender = new MailSmtpSender("mail.yht.co.kr", 587, "gonemaster@yht.co.kr", "6111^!!!!!");
			    String[] receiver = Util.null2str(customProperties.getManagerEmail()).split(",");
			    try {
			    	String senderName = profile.toUpperCase();
			    	if("PROD".equals(senderName)){
			    		senderName = "G1Express";
			    	}
			    	sender.sendSmtpMail("Expmaster@yht.co.kr", senderName + " DAEMON", receiver, "["+serviceCode+"] Express SQL Error", content.toString());
			    } catch (Exception ex) {
			    	// TODO Auto-generated catch block
					log.error(Util.getPrintStacTraceString(ex));
				}

	    	}
		}
	}

}
