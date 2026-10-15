package org.yht.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class EncryptionUtils {
	 /**
	  * MD5 방식의 암호화
	  * @param strData(32자리)
	  * @return
	  */
	 public static String encrypt_md5(String strData) {
		 String strENCData = "";
		 try {
			 MessageDigest md = MessageDigest.getInstance("MD5");
			 
	         byte[] bytData = strData.getBytes();
	         md.update(bytData);
	         
	         byte[] digest = md.digest();
	         for (int i = 0; i < digest.length; i++) {
	        	 strENCData += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1).toUpperCase();
	         }
		 } catch (NoSuchAlgorithmException e) {
			 log.error(Util.getPrintStacTraceString(e));
		 }
		 
		 return strENCData;
	 }
	 
	 /**
	  * SHA1 방식의 암호화
	  * @param strData(40자리)
	  * @return
	  */
	 public static String encrypt_sha1(String strData) {
		 String strENCData = "";
	      try {
	    	  MessageDigest md = MessageDigest.getInstance("SHA1");
	    	  
	    	  byte[] bytData = strData.getBytes();
	    	  md.update(bytData);
	    	  
	    	  byte[] digest = md.digest();
	    	  for (int i = 0; i < digest.length; i++) {
	    		  strENCData += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1).toUpperCase();
	    	  }
	      } catch (NoSuchAlgorithmException e) {
	    	  log.error(Util.getPrintStacTraceString(e));
	      }
	      
	      return strENCData;
	 }
	 
	 /**
	  * SHA256 방식의 암호화 - salt값을 사용하지 않는 경우
	  * @param strData(64자리)
	  * @return
	  */
	 public static String encrypt_sha2(String strData) {
		 String strENCData = "";
	      try {
	    	  MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	  
	    	  byte[] bytData = strData.getBytes("UTF-8");
	    	  md.update(bytData);
	    	  
	    	  byte[] digest = md.digest();
	    	  for (int i = 0; i < digest.length; i++) {
	    		  strENCData += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1).toUpperCase();
	    	  }
	      } catch (NoSuchAlgorithmException e) {
	    	  log.error(Util.getPrintStacTraceString(e));
	      } catch ( UnsupportedEncodingException e) {
	    	  log.error(Util.getPrintStacTraceString(e));
	      }
	      
	      return strENCData;
	 }
	 
	 /**
	  * SHA256 방식의 암호화 - salt값을 사용하는 경우
	  * @param strData(64자리)
	  * @return
	  */
	 public static String encrypt_sha2(String strData, byte[] salt) {
		 String strENCData = "";
	      try {
	    	  MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	  
	    	  byte[] bytData = strData.getBytes("UTF-8");
	    	  md.update(salt);
	    	  md.update(bytData);
	    	  
	    	  byte[] digest = md.digest();
	    	  for (int i = 0; i < digest.length; i++) {
	    		  strENCData += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1).toUpperCase();
	    	  }
	      } catch (NoSuchAlgorithmException e) {
	    	  log.error(Util.getPrintStacTraceString(e));
	      } catch ( UnsupportedEncodingException e) {
	    	  log.error(Util.getPrintStacTraceString(e));
	      }
	      
	      return strENCData;
	 }
	 
	 /**
	  * SHA256 방식의 암호화 - salt값을 사용하지 않고 iterationNb만큼 암호화 시키는 경우
	  * @param strData(64자리)
	  * @return
	  */
	 public static String encrypt_sha2(int iterationNb, String strData) {
		 String strENCData = "";
		 
	      try {
	    	  MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	  
	    	  byte[] bytData = strData.getBytes("UTF-8");
	    	  md.update(bytData);
	    	  
	    	  byte[] digest = md.digest();
	    	  
	    	  for (int i = 0; i < iterationNb; i++) {
	    		  md.reset();
	    		  digest = md.digest(digest);
	    	  }
		       
	    	  for (int i = 0; i < digest.length; i++) {
	    		  strENCData += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1).toUpperCase();
	    	  }
	      } catch (NoSuchAlgorithmException e) {
	    	  log.error(Util.getPrintStacTraceString(e));
	      } catch ( UnsupportedEncodingException e) {
	    	  log.error(Util.getPrintStacTraceString(e));
	      }
	      
	      return strENCData;
	 }
	 
	 /**
	  * SHA256 방식의 암호화 - salt값을 사용하고 iterationNb만큼 암호화 시키는 경우
	  * @param strData(64자리)
	  * @return
	  */
	 public static String encrypt_sha2(int iterationNb, String strData, byte[] salt) {
		 String strENCData = "";
		 
	      try {
	    	  MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	  
	    	  byte[] bytData = strData.getBytes("UTF-8");
	    	  md.update(salt);
	    	  md.update(bytData);
	    	  
	    	  byte[] digest = md.digest();
	    	  
	    	  for (int i = 0; i < iterationNb; i++) {
	    		  md.reset();
	    		  digest = md.digest(digest);
	    	  }
		       
	    	  for (int i = 0; i < digest.length; i++) {
	    		  strENCData += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1).toUpperCase();
	    	  }
	      } catch (NoSuchAlgorithmException e) {
	    	  log.error(Util.getPrintStacTraceString(e));
	      } catch ( UnsupportedEncodingException e) {
	    	  log.error(Util.getPrintStacTraceString(e));
	      }
	      
	      return strENCData;
	 }
	 
	/*
	 * AES 128 Alogrithm
	 * 아래의 모듈을 통하여 암호활 경우 리턴되는 값의 길이는 다음과 같습니다.
	 * 주민번호(13BYTE) => 32BYTE
	 * 신용카드번호(14BYTE) => 32BYTE
	 * 신용카드번호(15BYTE) => 32BYTE
	 * 신용카드번호(16BYTE) => 64BYTE
	 * 계좌번호(?) => 32BYTE
	 * 따라서 용도에 따라 DB저장 시 컬럼의 크기를 32BYTE 또는 64BYTE 이상으로 설정해주시기 바랍니다.
	 */
	/**

	 * AES KEY Generator
	 * passwd 가 변경될 경우 아래의 메소드를 실행하여 재생성해야한다.
	 * 모듈 효율성을 위해 생성된 키값을 미리 선전하여 키생성 로직을 제외
	 * @return
	 * @throws Exception
	 */
	 public static byte[] getRawKey(String passwd) throws Exception {
		 KeyGenerator kgen = KeyGenerator.getInstance("AES");
		 SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		 byte[] seed = passwd.getBytes();
		 sr.setSeed(seed);
		 kgen.init(128, sr); // 192 and 256 bits may not be available
		 SecretKey skey = kgen.generateKey();
		 byte[] raw = skey.getEncoded();
		 return raw;
	 }
	 
	 /**
	  * AES ENCRYPT_MODE
	  * @param message AES로 암호화할 값
	  * @return
	  * @throws Exception
	  * EncryptionUtils.encryptAES(message);
	  */
	public static String encryptAES(String message, String rawKey){
		String str = "";
		try {
			if(message!=null && message.length()>0) {
				Cipher cipher = Cipher.getInstance("AES/GCM/noPadding", "BC");
				//setup key
				byte[] keyBytes= new byte[16];
				byte[] b= rawKey.getBytes("UTF-8");
				int len= b.length; 
				if (len > keyBytes.length) {
					len = keyBytes.length;
				}
				System.arraycopy(b, 0, keyBytes, 0, len);
				SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
				
				//the below may make this less secure, hard code byte array the IV in both java and .net clients
				IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
				cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);
				byte [] results = cipher.doFinal(message.getBytes("UTF-8"));
				Encoder encoder = Base64.getEncoder();
				str =  encoder.encodeToString(results);
			}
		} catch(Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		}
		
		return str;
	}
	 
	/**
	  * AES ENCRYPT_MODE
	  * @param message AES로 암호화할 값
	  * @return
	  * @throws Exception
	  * EncryptionUtils.encryptAES(message);
	  */
	 public static String encryptAES2(String message, byte[] rawKey) throws Exception {
		 String str = "";
		 
		 try {
			 if(message!=null && message.length()>0) {
				 SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
				 Cipher cipher = Cipher.getInstance("AES");
				 cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
				 byte[] encrypted = cipher.doFinal(message.getBytes());
				 str = Hex.encodeHexString(encrypted);
			 }
		 } catch(Exception e) {
			 log.error(Util.getPrintStacTraceString(e));
		 }
		 
		 return str;
	 }
	 
	 /**
	  * AES DECRYPT_MODE
	  * @param message AES로 암호화된 값
	  * @return
	  * @throws Exception
	  * EncryptionUtils.decryptAES(message);
	  */
	public static String decryptAES(String message, String rawKey){
		String str = "";
		try {
			if(message!=null && message.length()>0) {
				Cipher cipher = Cipher.getInstance("AES/GCM/noPadding", "BC");
				//setup key
				byte[] keyBytes= new byte[16];
				byte[] b= rawKey.getBytes("UTF-8");
				int len= b.length; 
				if (len > keyBytes.length) {
					len = keyBytes.length;
				}
				System.arraycopy(b, 0, keyBytes, 0, len);
				SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
				IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
				cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);
				Decoder dcoder = Base64.getDecoder();
				byte [] results = cipher.doFinal(dcoder.decode(message));
				str = new String(results,"UTF-8");
			}
		} catch(Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		}
		
		return str;
	}
	

	public static String encryptAES(String text, String password, String encoderType) throws Exception{

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	
		//setup key
		byte[] keyBytes= new byte[16];
		byte[] b= password.getBytes("UTF-8");
		int len= b.length; 
	
		if (len > keyBytes.length) 
			len = keyBytes.length;
	
		System.arraycopy(b, 0, keyBytes, 0, len);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
	
		//the below may make this less secure, hard code byte array the IV in both java and .net clients
		IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
		cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);
		byte [] results = cipher.doFinal(text.getBytes("UTF-8"));
	
//		if(encoderType.equals("BASE64")){
//			BASE64Encoder encoder = new BASE64Encoder();
//			return encoder.encode(results);
//		}else{
			Encoder encoder = Base64.getEncoder();
			return encoder.encodeToString(results);
//		}
	
	}
	
	public static String decryptAES(String text, String password, String decoderType) throws Exception{

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	
		//setup key
		byte[] keyBytes= new byte[16];
		byte[] b= password.getBytes("UTF-8");
		int len= b.length; 
		if (len > keyBytes.length) 
			len = keyBytes.length;
	
		System.arraycopy(b, 0, keyBytes, 0, len);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
		cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);
//		if(decoderType.equals("BASE64")){
//			BASE64Decoder decoder = new BASE64Decoder();
//			byte [] results = cipher.doFinal(decoder.decodeBuffer(text));
//			return new String(results,"UTF-8");
//		}else{
			Decoder dcoder = Base64.getDecoder();
			byte [] results = cipher.doFinal(dcoder.decode(text));
			return new String(results,"UTF-8");
//		}
		
	}
	 
	 public static void main(String[] args) throws Exception {
		 String mdResult = "", shaResult="", sha2Result="", sha2WithSaltResult="", sha2LoopResult="", sha2LoopWithSaltResult="";
		 String saltStr = "+82rnfmaQkd";
		 byte[] salt = saltStr.getBytes();
		  
		 mdResult = EncryptionUtils.encrypt_md5("aaa");
		 shaResult = EncryptionUtils.encrypt_sha1("aaa");
		 sha2Result = EncryptionUtils.encrypt_sha2("rnfmaQkd");
		 sha2WithSaltResult = EncryptionUtils.encrypt_sha2("rnfmaQkd", salt);
		 sha2LoopResult = EncryptionUtils.encrypt_sha2(1000, "admin");
		 sha2LoopWithSaltResult = EncryptionUtils.encrypt_sha2(1000, "rnfmaQkd", salt);
		  
		 
		 // 아래부터는 AES 128 테스트(복호화가 필요한 데이터일 경우 AES 128 기법을 사용, 주민번호, 계좌번호 등등)
		 String ssn = "1";
		 // 암복호화에 필요한 Raw Key값을 생성하기 위한 값은 별도의 파일에 저장하여 사용하거나
		 // 사용자별로 생성하여 별도의 파일에 보관 후 암복호화시 Load하여 사용.
		 byte[] rawKey = EncryptionUtils.getRawKey("yhtUstd2Border");
		 
		 
		 
		 }
}