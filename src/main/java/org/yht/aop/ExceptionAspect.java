package org.yht.aop;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yht.MailSmtpSender;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.util.EncryptionUtils;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class ExceptionAspect {

	public String entCode;
	
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
	@Value("${spring.mail.error.rawKey}")
	private String rawKey;
	@Value("${custom.edi-send-yn}")
	private String ediSendYn;
	
	
	//예외 처리(메일 전송 / 파일 생성)
	@AfterThrowing(value = "execution(* org.yht..controller..*Controller.*(..))", throwing = "e")
	public void exceptionHandle(JoinPoint joinPoint, Exception e) throws Throwable {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		
		String uri = request.getRequestURI();	// /YHTXX/ecm/eCommerce/delivery/deliveryM/search/list
		String[] uris = uri.split("/");
		String entCode = "";
		if (uris.length > 0)	entCode = uris[1].toUpperCase();
		
		String type = "";
		String name = joinPoint.getSignature().getDeclaringTypeName();
		
		if(!"ups1".equals(profile) && !("ups2").equals(profile) && ("bms1").equals(profile) && ("bms2").equals(profile)) {
		if("Y".equals(ediSendYn)){
	    	
	    	if(e != null){
			    
	    		String cause = "";
			    try{
					cause = e.getCause().toString();
				}catch(Exception ex){
					cause = e.toString();		
				}
			    
			    Signature signature = joinPoint.getSignature();
			    String stuff = signature.toString();						// Method Full String( List ems.business.service.sms.SmsCopgService.selectSmsCopg001g1List(Map))
			    String arguments = Arrays.toString(joinPoint.getArgs());	// Parameters
			    String exceptMessage = "";	
			    
			    try{
			    	exceptMessage = e.getMessage().toString();	
				}catch(Exception ex){
					StringWriter sw = new StringWriter(); 
					e.printStackTrace(new PrintWriter(sw));					
					exceptMessage = sw.toString();	
				}
			    
				if(cause.contains("connect")) {
					if(!cause.contains("org.apache.catalina.connector.RequestFacade")) {
						/** 취약성점검사항     정상작동하지않는 내용이어서 제거함 */
					}
				}
			
						
				//메일 전송
				StringBuffer content = new StringBuffer();
				content.append("<b>Method :</b> " + stuff + "<br/>");
				content.append("<b>Cause :</b> " + cause + "<br/>");
				content.append("<b>Parameters :</b> " + arguments + "<br/>");
				content.append("<b>Exception Message :</b><br/> " + exceptMessage);
			    
			    String serviceCode = "";
			   		    
			    log.debug("cause : " + cause);
			    log.debug("stuff : " + stuff);
			    log.debug("exceptMessage : " + exceptMessage);
			    
			    if(stuff.contains(".api.")) {
			    	serviceCode = "API";
			    } else if(stuff.contains(".commonMng.")) {
			    	serviceCode = "COMMON_MNG";
			    } else if(stuff.contains(".consumerService.")) {
			    	serviceCode = "CONSUMER_SERVICE";
			    } else if(stuff.contains(".eCommerce.")) {
			    	serviceCode = "ECM";
			    }else if(stuff.contains(".edi.")) {
			    	serviceCode = "EDI";
			    } else if(stuff.contains(".mobile.od.")) {
			    	serviceCode = "MOBILE_OD";
			    } else if(stuff.contains(".selectMng.")) {
			    	serviceCode = "SELECT_MNG";
			    } else if(stuff.contains(".sms.")) {
			    	serviceCode = "SMS";
			    } else if(stuff.contains(".systemMng.")) {
			    	serviceCode = "SYSTEM_MNG";
			    } else {
			    	serviceCode = "ETC";
			    }
			    
			    
			    // 위 내용을 조합하여 별도 파일 생성 또는 메일을 전송한다.
			    MailSmtpSender sender = new MailSmtpSender(host, port, username, EncryptionUtils.decryptAES(password, rawKey));
			    String[] receiver = Util.null2str(customProperties.getManagerEmail()).split(",");
			    try {
			    	String senderName = profile.toUpperCase();
			    	if(senderName.contains("PROD")){
			    		senderName = "G1Express";
			    	}
			    	
			    	sender.sendSmtpMail("Expmaster@yht.co.kr", senderName + " REST", receiver, "["+serviceCode+"] Express SQL Error", content.toString());
			    } catch (Exception ex) {
			    	// TODO Auto-generated catch block
			    	log.error(Util.getPrintStacTraceString(e));
				}
			    
			    // 2025.01.20 오더조회 오류 장부장님께 전송
			    if(stuff.contains(".eCommerce.OrderController.selectOrderList")) {
			    	receiver = new String[] {"brag@yht.co.kr"};
			    	try {
			    		String senderName = profile.toUpperCase();
			    		if(senderName.contains("PROD")){
			    			senderName = "G1Express";
			    		}
			    		
			    		sender.sendSmtpMail("Expmaster@yht.co.kr", senderName + " REST", receiver, "["+serviceCode+"] Express SQL Error", content.toString());
			    	} catch (Exception ex) {
			    		// TODO Auto-generated catch block
			    		log.error(Util.getPrintStacTraceString(e));
			    	}
			    }
		    
	    	}
		}
		}
	}
}