package org.yht;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.yht.util.Util;

/**
 * MailSmtpSender class
 * SMTP를 이용하여 메일을 발송한다.
 * 
 * <pre>
 * 
 * [사용방법]
 * 
 * 1. 선언
 * @param host SMTP Host 주소(ex. mail.yht.co.kr)
 * @param port SMTP Port(ex. 587)
 * MailSmtpSender mailSender = new MailSmtpSender(String host, int port);
 * 
 * 2-1. 첨부파일 없는 메일 전송
 * @param authId 인증 아이디
 * @param authPasswd 인증 패스워드
 * @param from 보내는 사람 메일 주소
 * @param fromName 보내는 사람 표현 이름
 * @param to 받는 메일 주소
 * @param cc 참조 메일 주소
 * @param bcc 숨은 참조 메일 주소
 * @param subject 메일 제목
 * @param content 메일 내용
 * sendSmtpMail(String authId, String authPasswd, String from,
 *  				String fromName, String[] to, String[] cc, String[] bcc,
 *  				String subject, String content);
 *  
 * 2-2. 첨부파일 포함 메일 전송
 * @param authId 인증 아이디
 * @param authPasswd 인증 패스워드
 * @param from 보내는 사람 메일 주소
 * @param fromName 보내는 사람 표현 이름
 * @param to 받는 메일 주소
 * @param cc 참조 메일 주소
 * @param bcc 숨은 참조 메일 주소
 * @param subject 메일 제목
 * @param content 메일 내용
 * @param attachFiles 첨부파일
 * sendSmtpMail(String authId, String authPasswd, String from, 
 * 					String fromName, String[] to, String[] cc, String[] bcc,
 * 					String subject, String content, File[] attachFiles);
 * 
 * </pre>
 * 
 */
public class MailSmtpSender {
    private Properties props = null;
 
    private String smtpMailHost = "";
    private int smtpMailPort = 0;
    private String smtpMailId = "";
    private String smtpMailPw = "";
    
//    @Value("${spring.mail.host}")
//	private static String hostVal;
//    
//    @Value("${spring.mail.port}")
//	private static int portVal;
//    
//    @Value("${spring.mail.username}")
//	private static String usernameVal;
//    
//    @Value("${spring.mail.password}")
//	private static String passwordVal;
    
    /**
      * constructor
      */
//    public MailSmtpSender() {
////        this("mail.yht.co.kr", 587
////        		, "gonemaster@yht.co.kr", "6111^!!!!");
//    	
////    	this(hostVal, portVal, usernameVal, passwordVal);
//        
//    }
 
    /**
     * constructor
     * @param host
     * @param port
     */
//    public MailSmtpSender(String host, int port) {
//    	this(host, port, Prop.getMessage("SSL_USE"), Prop.getMessage("DEFAULT_SMTP_SOCKET_FACTORY_CLASS"));
//    }
    
    /**
     * constructor
     * @param host
     * @param port
     * @param sslUse
     */
//    public MailSmtpSender(String host, int port, String sslUse) {    	
//    	this(host, port, sslUse, Prop.getMessage("DEFAULT_SMTP_SOCKET_FACTORY_CLASS"));
//    }
    
    /**
     * constructor
     * @param map
     */
    public MailSmtpSender(Map<String, Object> mailAuthMap) {
    	this(Util.null2str(mailAuthMap.get("SMTP_MAIL_HOST"))
    			, Util.null2int(mailAuthMap.get("SMTP_MAIL_PORT"))
    			, Util.null2str(mailAuthMap.get("SMTP_MAIL_ID"))
    			, Util.null2str(mailAuthMap.get("SMTP_MAIL_PW")));
    }
    
    /**
     * constructor
     * @param map
     */
    public MailSmtpSender(String host, int port, String id, String pw) {
    	this.smtpMailHost = host;
    	this.smtpMailPort = port;
    	this.smtpMailId = id;
    	this.smtpMailPw = pw;
    	
    	props = new Properties();
    	 
        // 기본 프로퍼티 설정
        props.put("mail.smtp.auth", "true");
        props.put("mail.transport.protocol", "smtp");
        
        //TLS방식 연결
        if(this.smtpMailHost.equalsIgnoreCase("smtp.office365.com")) {
        	props.put("mail.smtp.starttls.enable", "true");
        	props.put("mail.smtp.ssl.trust", this.smtpMailHost);
        //SSL방식 연결 추후 테스트
        } else if("Y".equals("N")) {
        	props.put("mail.smtp.starttls.enable", "true");
        	props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
        	props.put("mail.smtp.starttls.enable", "false");
        }
        
        // 지정 가능 프로퍼티 설정
        props.put("mail.smtp.host", this.smtpMailHost);
        props.put("mail.smtp.port", this.smtpMailPort);
    }
    
    /**
     * SMTP를 이용하여 메일을 발송한다. (첨부파일 처리는 없다.)
     *
     * @param from 보내는 사람 메일 주소
     * @param fromName 보내는 사람 표현 이름
     * @param to 받는 메일 주소
     * @param subject 메일 제목
     * @param content 메일 내용
     * @throws Exception
     */
   public void sendSmtpMail(String from, String fromName, String[] to, String subject, String content) throws Exception {
	   this.sendSmtpMail(from, fromName, to, null, null, subject, content);
   }
   
    /**
      * SMTP를 이용하여 메일을 발송한다. (인증을 위해 mail 계정정보가 필요하며, 첨부파일 처리는 없다.)
      *
      * @param authId 인증 아이디
      * @param authPasswd 인증 패스워드
      * @param from 보내는 사람 메일 주소
      * @param fromName 보내는 사람 표현 이름
      * @param to 받는 메일 주소
      * @param cc 참조 메일 주소
      * @param bcc 숨은 참조 메일 주소
      * @param subject 메일 제목
      * @param content 메일 내용
      * @throws Exception
      */
    public void sendSmtpMail(String from,
            String fromName, String[] to, String[] cc, String[] bcc,
            String subject, String content) throws Exception {
    	this.sendSmtpMail(from, fromName, to, cc, bcc, subject, content, null);
    }
 
    /**
      * SMTP를 이용하여 메일을 발송한다. (인증을 위해 mail 계정정보가 필요하며, 첨부파일 처리.)
      *
      * @param authId 인증 아이디
      * @param authPasswd 인증 패스워드
      * @param from 보내는 사람 메일 주소
      * @param fromName 보내는 사람 표현 이름
      * @param to 받는 메일 주소
      * @param cc 참조 메일 주소
      * @param bcc 숨은 참조 메일 주소
      * @param subject 메일 제목
      * @param content 메일 내용
      * @param attachFiles 첨부파일
      * @throws Exception
      */
    public void sendSmtpMail(String from,
            String fromName, String[] to, String[] cc, String[] bcc,
            String subject, String content, File[] attachFiles)
            throws Exception {
    	
    	MimeMessage msg = createMessage(this.smtpMailId, this.smtpMailPw, from, fromName, to, cc,
                bcc, attachFiles); // create message
 
        // subject & content
        msg.setSubject(subject, "utf-8");
        msg.setSentDate(new Date());
 
        if(attachFiles == null) {
        	 // subject & content
            msg.setSubject(subject, "utf-8");
            msg.setSentDate(new Date());
            msg.setContent(content, "text/html;charset=utf-8");
        } else {
        	Object contentObj = msg.getContent();
	        if (contentObj != null && contentObj instanceof Multipart) {
	            MimeBodyPart contentPart = new MimeBodyPart();
	            contentPart.setText(content);
	            contentPart.setHeader("Content-Type", "text/html;charset=utf-8");
	 
	            Multipart mPart = (Multipart) contentObj;
	            mPart.addBodyPart(contentPart);
	        } else {
	            msg.setContent(content, "text/html;charset=utf-8");
	        }
        }
        Transport.send(msg); // send mail
    }
 
    // 메시지를 생성한다.
    private MimeMessage createMessage(String authId, String authPasswd,
            String from, String fromName, String[] to, String[] cc,
            String[] bcc, File[] attachFiles) throws Exception {
        MailSmtpAuthenticator auth = new MailSmtpAuthenticator(authId,
                authPasswd); 
        
        //getDefaultInstance는 매번 새로운 session을 생성하지 않는다.
        Session mailSession = Session.getInstance(props, auth);
        //Session mailSession = Session.getDefaultInstance(props, auth);
 
        // create message
        MimeMessage msg = new MimeMessage(mailSession);
 
        if (fromName != null) {
            msg.setFrom(new InternetAddress(from, MimeUtility.encodeText(
                    fromName, "UTF-8", "B")));
        } else {
            msg.setFrom(new InternetAddress(from));
        }
 
        // add to
        InternetAddress[] toAddr = null;
        if (to != null && to[0].length() > 0) {
            toAddr = new InternetAddress[to.length];
            for (int i = 0; i < to.length; i++) {
                toAddr[i] = new InternetAddress(to[i]);
            }
        } else {
            toAddr = new InternetAddress[1];
            toAddr[0] = new InternetAddress(authId);
        }
        msg.setRecipients(Message.RecipientType.TO, toAddr);
 
        // add cc (참조)
        if (cc != null && cc[0].length() > 0) {
            InternetAddress[] ccAddr = new InternetAddress[cc.length];
            for (int i = 0; i < cc.length; i++) {
                ccAddr[i] = new InternetAddress(cc[i]);
            }
 
            msg.setRecipients(Message.RecipientType.CC, ccAddr);
        }
 
        // add bcc (숨은참조)
        if (bcc != null && bcc[0].length() > 0) {
            InternetAddress[] bccAddr = new InternetAddress[bcc.length];
            for (int i = 0; i < bcc.length; i++) {
                bccAddr[i] = new InternetAddress(bcc[i]);
            }
 
            msg.setRecipients(Message.RecipientType.BCC, bccAddr);
        }
 
        if (attachFiles != null && attachFiles[0].length() > 0) {
            // 파일 첨부
            Multipart multipart = new MimeMultipart();
 
            for (File attachFile : attachFiles) {
                if (attachFile != null && attachFile.exists()) {
                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
 
                    mimeBodyPart.setDataHandler(new DataHandler(
                            new FileDataSource(attachFile)));
                    mimeBodyPart.setFileName(attachFile.getName());
 
                    multipart.addBodyPart(mimeBodyPart);
                }
            }
 
            msg.setContent(multipart);
        }
 
        return msg;
    }
 
    private static class MailSmtpAuthenticator extends Authenticator {
 
        private String id = null;
        private String passwd = null;
 
        public MailSmtpAuthenticator(String id, String passwd) {
            this.id = id;
            this.passwd = passwd;
        }
 
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(id, passwd);
        }
    } 
            
}