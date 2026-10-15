package org.yht.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.Filters;
import org.yht.exps.express.model.search.Sort;
import org.yht.jwt.JwtTokenProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Util {
	private static final SecureRandom random = new SecureRandom();
	private static final String accessControlDB = "(?is).*[';\"=&|!(){}$%@].*|.*\\bUNION\\b.*|.*\\bSELECT\\b.*|.*\\bTHEN\\b.*|.*\\bMERGE\\b.*|.*\\bIF\\b.*|.*\\bINSTANCE\\b.*|.*\\bEND\\b.*|.*\\bCOLUMM\\b.*|.*\\bDATABASE\\b.*|.*\\bCONCAT\\b.*|.*\\bCOUNT\\b.*|.*\\bLOWER\\b.*|.*\\bINSERT\\b.*|.*\\bUPDATE\\b.*|.*\\bDELETE\\b.*";
	private static final String[] cryptographyCols = {"CUSTOMS_ID", "CONSIGNEE_TEL", "CONSIGNEE_HP"/*, "CONSIGNEE_NAME_LOC"*/};
	
	@Autowired
	private CustomProperties customProperties;
	private static CustomProperties staticCustomProperties;
	
	@Value("${spring.profiles.active}")
	private String profile;
	private static String staticProfile;
	
	
	@PostConstruct     
	private void initStaticCustomProperties () {
		staticCustomProperties = this.customProperties;
		staticProfile = this.profile;
	}
	
	

	/**
	 * null을 공백문자로 리턴
	 * @param obj
	 * @return
	 */
	public static String null2str(Object src){
		return null2str(src, "");
	}
	public static String null2str(Object obj, String dValue){
		return (obj == null || "null".equals(obj.toString().trim()) || "".equals(obj.toString().trim())) ? dValue : obj.toString().trim();
	}
	public static String null2str(String src){
		return null2str(src, "");
	}
	public static String null2str(String src, String dValue){
		return (src == null || "null".equals(src.trim())) ? dValue : src.trim();
	}
	public static String null2strNoTrim(Object src){
		return null2strNoTrim(src, "");
	}
	public static String null2strNoTrim(Object obj, String dValue){
		return (obj == null || "null".equals(obj.toString()) || "".equals(obj.toString())) ? dValue : obj.toString();
	}
	
	/**
	 * null을 0로 리턴
	 * @param src
	 * @return
	 */
	public static int null2int(String src){
		if(src == null || "null".equals(src) || "".equals(src)){
			return 0;
		}else {
			int idx = src.indexOf(".");
			if(idx > -1){
				src = src.substring(0, idx);
			}
			return Integer.parseInt(src.replaceAll(",", "").trim());
		}
	}
	public static int null2int(Object src){
//		return (src == null || "null".equals(src.toString().trim()) || "".equals(src.toString().trim())) ? 0 : Integer.parseInt(src.toString().replaceAll(",", "").trim());
		return null2int(null2str(src));
	}
	public static long null2long(String src){
		return (src == null || "null".equals(src) || "".equals(src)) ? 0 : Long.parseLong(src.replaceAll(",", "").trim());
	}
	public static long null2long(Object src){
		return (src == null || "null".equals(src.toString().trim()) || "".equals(src.toString().trim())) ? 0 : Long.parseLong(src.toString().replaceAll(",", "").trim());
	}
	public static double null2dbl(Object src){
		double rtnNum = 0.0;
		
		if(src == null || "null".equals(src.toString().trim()) || "".equals(src.toString().trim())) {
			rtnNum = 0.0;
		} else {
			rtnNum = Double.parseDouble(src.toString().replaceAll(",", "").trim());
		}
		
		return rtnNum;
	}
	
	public static Integer decimal2int(Object src) {
		if (src instanceof BigDecimal) {
		    return ((BigDecimal) src).intValue();
		} else if (src instanceof Integer) {
		    return (int) src;
		} else {
		    return null;
		}
	}
	
	public static int str2intOrZero(String str) {
	    if (str == null) return 0;
	    try {
	        return Integer.parseInt(str.trim());
	    } catch (NumberFormatException e) {
	        return 0;
	    }
	}
	
	public static long str2longOrZero(String str) {
		if (str == null || str.trim().isEmpty()) return 0L;
	    try {
	        return Long.parseLong(str.trim());
	    } catch (NumberFormatException e) {
	        return 0L;
	    }
	}
	
	public static long obj2longOrZero(Object obj) {
		if (obj == null || "null".equals(obj.toString().trim()) || "".equals(obj.toString().trim()))  return 0L;
	    try {
	        return Long.parseLong(obj.toString().trim());
	    } catch (NumberFormatException e) {
	        return 0L;
	    }
	}
	
	public static BigDecimal obj2BigDecimalOrZero(Object obj) {
	    if (obj == null || "null".equalsIgnoreCase(obj.toString().trim()) || "".equals(obj.toString().trim())) {
	        return BigDecimal.ZERO;
	    }

	    try {
	        if (obj instanceof BigDecimal) {
	            return (BigDecimal) obj;
	        } else if (obj instanceof Number) {
	            // ✅ Double, Integer, Long 등 Number 타입은 BigDecimal.valueOf 사용
	            return BigDecimal.valueOf(((Number) obj).doubleValue());
	        } else {
	            // ✅ 문자열인 경우 new BigDecimal("...") 로 변환 (정확)
	            return new BigDecimal(obj.toString().trim());
	        }
	    } catch (NumberFormatException e) {
	        return BigDecimal.ZERO;
	    }
	}
	
	public static boolean isInteger(Object obj) {
	    return obj != null && obj.toString().matches("^-?\\d+$");
	}

	public static boolean isDecimal(Object obj) {
	    return obj != null && obj.toString().matches("^-?\\d*(\\.\\d+)?$");
	}
	
	/**
	 * 
	 * @param src
	 * @param digit
	 * @param type : 변환 타입. (U-반올림, C-무조건 반올림, D-내림)
	 * @return
	 */
	public static double null2dbl(Object src, int digit, String type) {
		double rtnNum = 0.0;
		
		if(src == null || "null".equals(src.toString().trim()) || "".equals(src.toString().trim())) {
			rtnNum = 0.0;
		} else {
			rtnNum = Double.parseDouble(src.toString().replaceAll(",", "").trim());
			/*
			if(digit != 0) {
				double tmpNum = 1.0;
				for(int i=0; i<digit; i++) tmpNum *= 10;
				
				rtnNum = Math.round(rtnNum * tmpNum) / tmpNum;
			}
			*/
			// 반올림 지정 자리수가 0일 경우도 반올림 처리가 가능하도록 변경
			// 2016.08.01 by jupitz
			
			switch(type) {
				case "U" :
					rtnNum = Double.parseDouble(new BigDecimal(rtnNum).setScale(digit, BigDecimal.ROUND_HALF_UP).toString());
					break;
				case "C" :
//					null2dbl(500*200.0002, 4, "C"); 결과값 100000.1001으로 나와서 임시처리
//					rtnNum = Double.parseDouble(new BigDecimal(rtnNum).setScale(digit, BigDecimal.ROUND_CEILING).toString());
					String rtnNumStr = new BigDecimal(rtnNum).setScale(10, BigDecimal.ROUND_HALF_UP).toString();
					rtnNum = Double.parseDouble(new BigDecimal(rtnNumStr).setScale(digit, BigDecimal.ROUND_CEILING).toString());
					break;
				case "D" :
					rtnNum = Double.parseDouble(new BigDecimal(rtnNum).setScale(digit, BigDecimal.ROUND_DOWN).toString());
					break;
				default :
					rtnNum = Double.parseDouble(new BigDecimal(rtnNum).setScale(digit, BigDecimal.ROUND_FLOOR).toString());
					break;
					
			}
		}
		
		return rtnNum;
	}
	
	/**
	 * null이거나 빈값이면 true 그렇지 않으면 false를 리턴
	 * @param src
	 * @return
	 */
	public static boolean isEmpty(String src){
		return (src == null || "null".equals(src) || "".equals(src)) ? true : false;
	}
	public static boolean isEmpty(Object src){
		return isEmpty(null2str(src));
	}
	
	public static boolean isEmpty2(String src){
		return (src == null || "".equals(src)) ? true : false;
	}
	public static boolean isEmpty2(Object src){
		return isEmpty(null2str(src));
	}

	/**
	 * 서블릿에서 한국어 처리
	 * @param src
	 * @return
	 */
	public static String null2strKorean(String src){
		String reeturnString = "";

		try{
			reeturnString = new String(src.getBytes("Cp1252"), "EUC-KR");
		}catch(Exception e){
			reeturnString = src;
		}
		return (src == null) ? "" : reeturnString; 
	}

	/**
	 * 첫 알파벳을 대문자로 리턴
	 * @param src
	 * @return
	 */
	public static String getFirstUpperCase(String src){
		String returnString = "";

		if(src == null || src.trim().equals("")){
			return "";
		}else{
			returnString  = src;
			byte [] bytes = src.getBytes();

			if(bytes[0] > 96 && bytes[0] < 128){
				returnString = String.valueOf(src.charAt(0)).toUpperCase().concat(src.substring(1));
			}
		}

		return returnString;
	}

	//Object를 넘겨받아 문자열로 변환 후 지정한 길이 이상일경우 자르는 함수
	public static String cutLength(Object src, int len) {
		return cutLength(src, len, "");
	}
	
	//Object를 넘겨받아 문자열로 변환 후 지정한 길이 이상일경우 자르는 함수
	public static String cutLength(Object src, int len, String dValue) {
		try {
			String str = null2str(src, dValue);
			
			if(str.length() > len) str = str.substring(0, len);
			
			return str;
		} catch (Exception e) {
			return src.toString();
		}
	}
	
	public static String cutLength(String str, int startIdx, int len) {
		try {
			if(str.length() > startIdx){
				if(str.length() > startIdx + len){
					str = str.substring(startIdx, startIdx + len);
				}else{
					str = str.substring(startIdx, str.length());
				}
			}else {
				str = "";
			}
			return str;
		} catch (Exception e) {
			return str;
		}
	}

	/**
	 * column 명칭 등을 받아 set, get 메소드 명칭을 만든다.
	 * @param src
	 * @param div
	 * @return
	 */
	public static String getSetGetMethodName(String src, String div){
		String returnString = "";

		if(src == null || src.trim().equals("")){
			return "";
		}else{
			StringBuffer buf  = new StringBuffer();
			String[] srcArray = src.split("_");

			for(int i=0 ; i<srcArray.length ; i++){
				buf.append(getFirstUpperCase(srcArray[i].toLowerCase()));
			}

			if("set".equals(div)){
				returnString = "set" + buf.toString();
			}else if("get".equals(div)){
				returnString = "get" + buf.toString();
			}
		}

		return returnString;
	}
	
	/** 사용자ID/사용자명/사용자로그인ID 파라미터값 사용하지 않고 토큰값 사용 */
	public static Map<String, Object> getSessionUserInfo(HttpServletRequest request, JwtTokenProvider jwtTokenProvider){
		try{
			String token = "";
			if("local".equals(staticProfile)){
				token = Util.null2str(request.getHeader("authorization")).replace("Bearer ", "");
			}else{
				Cookie[] cookies = request.getCookies();
		    	if(cookies != null){
		    		for(Cookie cookie : cookies){
		    			if(cookie.getName().equals("token")){
		    				token = cookie.getValue();
		    			}
		    		}
		    	}
			}
			return jwtTokenProvider.getUserInfo(token);
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			return new HashMap<String, Object>();
		}	
	}
	public static void getSessionUserInfo(HttpServletRequest request, JwtTokenProvider jwtTokenProvider, Object model, String userIdKey, String userNameKey, String userLoginIdKey) throws Exception{
		getSessionUserInfo(request, jwtTokenProvider, model, userIdKey, userNameKey, userLoginIdKey, null, false, false, false, false);
	}
	public static void getSessionUserInfo(HttpServletRequest request, JwtTokenProvider jwtTokenProvider, Object model, String userIdKey, String userNameKey, String userLoginIdKey, String userNameEngKey) throws Exception{
		getSessionUserInfo(request, jwtTokenProvider, model, userIdKey, userNameKey, userLoginIdKey, userNameEngKey, false, false, false, false);
	}
	public static void getSessionUserInfo(HttpServletRequest request, JwtTokenProvider jwtTokenProvider, Object model, String userIdKey, String userNameKey, String userLoginIdKey
			, boolean extendsId, boolean extendsName, boolean extendsLoginId) throws Exception{
		getSessionUserInfo(request, jwtTokenProvider, model, userIdKey, userNameKey, userLoginIdKey, null, false, false, false, false);
	}
	public static void getSessionUserInfo(HttpServletRequest request, JwtTokenProvider jwtTokenProvider, Object model, String userIdKey, String userNameKey, String userLoginIdKey, String userNameEngKey
			, boolean extendsId, boolean extendsName, boolean extendsLoginId, boolean extendsNameEng) throws Exception{
		try{
			String token = "";
			if("local".equals(staticProfile)){
				token = Util.null2str(request.getHeader("authorization")).replace("Bearer ", "");
			}else{
				Cookie[] cookies = request.getCookies();
		    	if(cookies != null){
		    		for(Cookie cookie : cookies){
		    			if(cookie.getName().equals("token")){
		    				token = cookie.getValue();
		    			}
		    		}
		    	}
			}
			Map<String, Object> userData = jwtTokenProvider.getUserInfo(token);
			if(model != null){
				Class<?> cls = model.getClass();
				Field field = null;
				
				if(userIdKey != null) {
					if(!Util.isEmpty(userData.get("USER_ID"))){	/** 2025-08-13 토큰생성수정소스 반영전 생성된 토큰에는 해당 정보가 없으므로 데이터 여부 체크      추후 if문 제거 */
						if(extendsId){
							field = cls.getSuperclass().getDeclaredField(userIdKey);
						}else{
							field = cls.getDeclaredField(userIdKey);
						}
						field.setAccessible(true);
						field.set(model, userData.get("USER_ID"));
					}
				}
				if(userNameKey != null){
					if(!Util.isEmpty(userData.get("USER_NAME"))){
						if(extendsName){
							field = cls.getSuperclass().getDeclaredField(userNameKey);
						}else{
							field = cls.getDeclaredField(userNameKey);
						}
						field.setAccessible(true);
						field.set(model, userData.get("USER_NAME"));
					}
				}
				if(userLoginIdKey != null){
					if(!Util.isEmpty(userData.get("USER_LOGIN_ID"))){
						if(extendsLoginId){
							field = cls.getSuperclass().getDeclaredField(userLoginIdKey);
						}else{
							field = cls.getDeclaredField(userLoginIdKey);
						}
						field.setAccessible(true);
						field.set(model, userData.get("USER_LOGIN_ID"));
					}
				}
				if(userNameEngKey != null){
					if(!Util.isEmpty(userData.get("USER_NAME_ENG"))){
						if(extendsNameEng){
							field = cls.getSuperclass().getDeclaredField(userNameEngKey);
						}else{
							field = cls.getDeclaredField(userNameEngKey);
						}
						field.setAccessible(true);
						field.set(model, userData.get("USER_NAME_ENG"));
					}
				}
			}
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
		}
	}
	
	public static void getSessionUserInfoBMS(HttpServletRequest request, JwtTokenProvider jwtTokenProvider, Object model, List<String> userIdKeyList, String userNameKey, String userLoginIdKey, String userNameEngKey) throws Exception{
		getSessionUserInfoBMS(request, jwtTokenProvider, model, userIdKeyList, userNameKey, userLoginIdKey, userNameEngKey, false, false, false, false);
	}
	public static void getSessionUserInfoBMS(HttpServletRequest request, JwtTokenProvider jwtTokenProvider, Object model, List<String> userIdKeyList, String userNameKey, String userLoginIdKey, String userNameEngKey
			, boolean extendsId, boolean extendsName, boolean extendsLoginId, boolean extendsNameEng) throws Exception{
		if(model == null) return;
			
		String token = "";
		if("local".equals(staticProfile)){
			token = Util.null2str(request.getHeader("authorization")).replace("Bearer ", "");
		}else{
			Cookie[] cookies = request.getCookies();
	    	if(cookies != null){
	    		for(Cookie cookie : cookies){
	    			if(cookie.getName().equals("token")){
	    				token = cookie.getValue();
	    			}
	    		}
	    	}
		}
		
		Map<String, Object> userData = jwtTokenProvider.getUserInfo(token);
		
		Class<?> cls = model.getClass();
		Field field = null;
		
		if(userIdKeyList != null && userIdKeyList.size() > 0) {
			for(String userIdKey : userIdKeyList) {
				if(userIdKey != null) {
					if(!Util.isEmpty(userData.get("USER_ID"))){	/** 2025-08-13 토큰생성수정소스 반영전 생성된 토큰에는 해당 정보가 없으므로 데이터 여부 체크      추후 if문 제거 */
						if(extendsId){
							field = cls.getSuperclass().getDeclaredField(userIdKey);
							field.setAccessible(true);
							field.set(model, userData.get("USER_ID"));
						}else{
							setFieldIfExists(model, userIdKey, userData.get("USER_ID"));
						}
					}
				}
			}
		}
		
		if(userNameKey != null){
			if(!Util.isEmpty(userData.get("USER_NAME"))){
				if(extendsName){
					field = cls.getSuperclass().getDeclaredField(userNameKey);
					field.setAccessible(true);
					field.set(model, userData.get("USER_NAME"));
				}else{
					setFieldIfExists(model, userNameKey, userData.get("USER_NAME"));
				}
			}
		}
		if(userLoginIdKey != null){
			if(!Util.isEmpty(userData.get("USER_LOGIN_ID"))){
				if(extendsLoginId){
					field = cls.getSuperclass().getDeclaredField(userLoginIdKey);
					field.setAccessible(true);
					field.set(model, userData.get("USER_LOGIN_ID"));
				}else{
					setFieldIfExists(model, userLoginIdKey, userData.get("USER_LOGIN_ID"));
				}
			}
		}
		if(userNameEngKey != null){
			if(!Util.isEmpty(userData.get("USER_NAME_ENG"))){
				if(extendsNameEng){
					field = cls.getSuperclass().getDeclaredField(userNameEngKey);
					field.setAccessible(true);
					field.set(model, userData.get("USER_NAME_ENG"));
				}else{
					setFieldIfExists(model, userNameEngKey, userData.get("USER_NAME_ENG"));
				}
			}
		}
			
	}

	/**
	 * 리플렉션을 사용해 특정 필드가 있을 경우에만 안전하게 set
	 */
	public static void setFieldIfExists(Object obj, String fieldName, Object value) {
	    if (obj == null) return;

	    try {
	    	
	    	// obj 자체가 List인 경우
	        if (obj instanceof List<?>) {
	            List<?> list = (List<?>) obj;
	            for (Object element : list) {
	                if (element == null) continue;
	                setFieldIfExists(element, fieldName, value); // 내부 Map/객체 재귀 호출
	            }
	            return;
	        }
	        
	    	// obj 자체가 Map일 경우
	        if (obj instanceof Map<?, ?>) {
	            Map<String, Object> map = (Map<String, Object>) obj;
	            if (map.containsKey(fieldName)) {
	                map.put(fieldName, value);
	            }

	            for (Object entryValue : map.values()) {
	            	if (entryValue == null) continue;
	            	// 해당 key가 있으면 값을 교체
		            if (entryValue instanceof List<?> || entryValue instanceof Map<?, ?>) { // value가 List나 Map일 수도 있으므로 내부도 재귀적으로 탐색
	                    setFieldIfExists(entryValue, fieldName, value);
	                }
	            }

	            return; // Map일 경우는 여기서 끝
	        }
	        
	        Class<?> cls = obj.getClass();
	        Field[] fields = cls.getDeclaredFields();

	        for (Field field : fields) {
	        	field.setAccessible(true);
	            Object fieldValue = field.get(obj);

	            // 필드가 List인 경우
	            if (fieldValue instanceof List<?>) {
	                List<?> list = (List<?>) fieldValue;
	                for (Object element : list) {
	                    if (element == null) continue;

	                    if (element instanceof Map<?, ?>) {
	                        Map<String, Object> map = (Map<String, Object>) element;
	                        // 리스트 안의 map에서 특정 key를 가진다면 set
	                        if (map.containsKey(fieldName)) {
	                            map.put(fieldName, value);
	                        }
	                    } else {
	                        // 리스트 안의 일반 객체인 경우, 재귀 호출로 내부 필드에 접근
	                        setFieldIfExists(element, fieldName, value);
	                    }
	                }
	            }
	            else if (!field.getName().equals(fieldName)) continue;
	            // 필드가 단일 객체거나 기본 타입일 경우
	            else {
	                field.set(obj, value);
	            }

	            //break; // 해당 필드 처리 후 종료
	        }

	    } catch (Exception e) {
	        log.error(Util.getPrintStacTraceString(e));
	    }
	}

	/**
	 * column 명칭 등을 받아 '_'를 없애고 다음 문자를 대문자로 만들어 리턴 
	 * @param src
	 * @return
	 */
	public static String getToVarName(String src){
		String returnString = "";

		if(src == null || src.trim().equals("")){
			return "";
		}else{
			StringBuffer buf  = new StringBuffer();
			String[] srcArray = src.split("_");

			for(int i=0 ; i<srcArray.length ; i++){
				if(i == 0){
					buf.append(srcArray[i].toLowerCase());
				}else{
					buf.append(getFirstUpperCase(srcArray[i].toLowerCase()));
				}
			}

			returnString = buf.toString();
		}

		return returnString;
	}
	public static String setAddress(String address) {
		String str = "";
		if(address.length() > 59){
			String beforeStr[] = address.split(",");
			for(var k=0; k<beforeStr.length; k++){
				if((str.length() + (beforeStr[k]+",").length()) > 59){
					break;
				}else{
					str += beforeStr[k]+',';
				}
			}
			str += "\n" + address.substring(str.length(), address.length()).trim();
		}else{
			str = address;
		}
		return str;
	}
	/**
	 * 'null' 출력방지 및 문자열로 변환하여 반환
	 */
	public static String print(Object o)
	{
		if(o == null)
			return "";
		StringBuffer sb = new StringBuffer(o.toString());
		for(int i = 0; i < sb.length(); i++)
		{
			char ch = sb.charAt(i);
			if(ch == '"')
			{
				sb.replace(i, i + 1, "&quot;");
				i += 5;
			}
			if(ch == '\'')
			{
				sb.replace(i, i + 1, "&#39;");
				i += 4;
			}
		}

		return sb.toString();
	}


	public static String printDateOnly(String o) {
		if (o == null) {
			return "";
		}else{
			return print(DateTime.date2strDateOnly(DateTime.str2dateDateOnly(o)));
		}
	}

	/**
	 * 숫자(금액)를 #,##0 문자열로 변환하여 반환(단, 숫자는 Long Class type)
	 */
	public static String money(Long n) {
		if (n == null) {
			return "";
		}

		return money(n.longValue());
	}

	/**
	 * 금액표시 : 문자열(금액) 입력을 String type  #,##0 으로 반환  // CH02
	 */
	public static String money(String n) {
		if (n == null) {
			return null;
		}
		n = n.trim();

		long m = 0l;
		try {
			m = Long.parseLong(n);
		}
		catch (Exception e) {
			//log.error(Util.getPrintStacTraceString(e));
			return "";
		}
		return money(m);
	}


	/**
	 * 매개변수로 들어온 8703041231313 주민등록번호를
	 * 870304-1231313 문자열로 Return한다.
	 */
	public static String ssnHyphen(String juminNo) {
		String jumin1, jumin2;

		if (juminNo.length() < 7) {
			return juminNo;
		}
		else {
			jumin1 = juminNo.substring(0, 6);
			jumin2 = juminNo.substring(6, juminNo.length());
		}
		return jumin1 + "-" + jumin2;
	}

	/**
	 * 매개변수로 들어온 8703041231313 주민등록번호를
	 * 870304-1****** 문자열로 Return한다.
	 */
	public static String ssnAster(String juminNo) {
		StringBuffer sb = new StringBuffer(juminNo);
		for (int i = 7; i < juminNo.length() && juminNo.length() > 8; i++) {
			sb.replace(i, i + 1, "*");

		}
		return ssnHyphen(sb.toString());
	}

	/**
	 * 매개변수로 들어온 문자열에서 '-'을 찾아 제거하고    //CH03
	 * '-'이 없는 연결된 문자열을 Return한다.
	 */
	public static String stripHyphen(String org) {

		for (; ; ) {
			int index = org.indexOf("-");

			if (index >= 0) {
				org = org.substring(0, index) + org.substring(index + 1);
			}
			else {
				break;
			}
		}
		return org;
	}

	@SuppressWarnings("unused")
	public static String convertAmtToHanja(String ps_amt){

		String sArr_hanja[] = {"영", "壹", "貳", "參", "四", "五", "六", "七", "八", "九"};
		String sResult_hanja = "";
		String sAmt_hanja  = "";
		String sOne_num    = "";
		String sBack_amount= "";
		Float fCur_amount  = Float.valueOf(ps_amt);
		int   iAmt_length  = ps_amt.length();
		int   i = 0;

		for (i = iAmt_length; i > 0; i--) {
			int j = 0;
			j = i-1;
			sBack_amount = sBack_amount+ps_amt.charAt(j);
		}

		for (i=1; i <= iAmt_length ; i++) {
			int j = 0;
			j = i -1;
			sOne_num = "";
			sOne_num = sOne_num + sBack_amount.charAt(j);

			if ( sOne_num.equals("0") ) {
				sAmt_hanja = "";
			} else {
				Integer iTemp_hanja = null;
				sAmt_hanja = "";
				iTemp_hanja = Integer.valueOf(sOne_num);
				String sOne_hanja  = sArr_hanja[iTemp_hanja.intValue()];

				sAmt_hanja = sAmt_hanja + sOne_hanja;

				if ( (i == 4) || (i == 8) || (i == 12) ) {
					sAmt_hanja = sAmt_hanja + "阡";
				} else if ( (i == 3) || (i == 7) || (i == 11) || (i == 15) ) {
					sAmt_hanja = sAmt_hanja + "百";
				} else if ( (i == 2) || (i == 6) || (i == 10) || (i == 14) ) {
					sAmt_hanja = sAmt_hanja + "拾";
				}
			}
			if (i == 13) {
				sAmt_hanja = sAmt_hanja + "兆";
			} else if (i == 9) {
				sAmt_hanja = sAmt_hanja + "億";
			} else if (i == 5) {
				sAmt_hanja = sAmt_hanja + "萬";
			}

			sResult_hanja = sAmt_hanja+sResult_hanja;
		}

		return sResult_hanja;
	}

	@SuppressWarnings("unused")
	public static String convertAmtToHangul(String ps_amt){

		String sArr_hanja[] = {"영", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구"};
		String sResult_hanja = "";
		String sAmt_hanja  = "";
		String sOne_num    = "";
		String sBack_amount= "";
		Float fCur_amount  = Float.valueOf(ps_amt);
		int   iAmt_length  = ps_amt.length();
		int   i = 0;

		for (i = iAmt_length; i > 0; i--) {
			int j = 0;
			j = i-1;
			sBack_amount = sBack_amount+ps_amt.charAt(j);
		}

		for (i=1; i <= iAmt_length ; i++) {
			int j = 0;
			j = i -1;
			sOne_num = "";
			sOne_num = sOne_num + sBack_amount.charAt(j);

			if ( sOne_num.equals("0") ) {
				sAmt_hanja = "";
			} else {
				Integer iTemp_hanja = null;
				sAmt_hanja = "";
				iTemp_hanja = Integer.valueOf(sOne_num);
				String sOne_hanja  = sArr_hanja[iTemp_hanja.intValue()];

				sAmt_hanja = sAmt_hanja + sOne_hanja;

				if ( (i == 4) || (i == 8) || (i == 12) ) {
					sAmt_hanja = sAmt_hanja + "천";
				} else if ( (i == 3) || (i == 7) || (i == 11) || (i == 15) ) {
					sAmt_hanja = sAmt_hanja + "백";
				} else if ( (i == 2) || (i == 6) || (i == 10) || (i == 14) ) {
					sAmt_hanja = sAmt_hanja + "십";
				}
			}
			if (i == 13) {
				sAmt_hanja = sAmt_hanja + "조";
			} else if (i == 9) {
				sAmt_hanja = sAmt_hanja + "억";
			} else if (i == 5) {
				sAmt_hanja = sAmt_hanja + "만";
			}

			sResult_hanja = sAmt_hanja+sResult_hanja;
		}

		return sResult_hanja;
	}	

	public static String numberToWords(int number){
		String[] th = {"","thousand","million", "billion","trillion"};
		String[] dg = {"zero","one","two","three","four", "five","six","seven","eight","nine"};
		String[] tn = {"ten","eleven","twelve","thirteen", "fourteen","fifteen","sixteen", "seventeen","eighteen","nineteen"};
		String[] tw = {"twenty","thirty","forty","fifty", "sixty","seventy","eighty","ninety"};
		
		String s = null2str(number);
		int x = s.indexOf(".");
		
		if (x == -1) x = s.length();
		if (x > 15) return "too big";
		
		String[] n = s.split(""); 
		String str = ""; 
		int sk = 0; 
		for (int i=0; i < x; i++) {
			if ((x-i)%3==2) {
				if (n[i].equals("1")) {
					str += tn[null2int(n[i+1])] + " ";
					i++;
					sk=1;
				} else if (null2int(n[i])!=0) {
					str += tw[null2int(n[i])-2] + " ";
					sk=1;
				}
			} else if (null2int(n[i])!=0) {
				str += dg[null2int(n[i])] +" ";
				if ((x-i)%3==0) str += "hundred ";sk=1;
			}
			if ((x-i)%3==1) {
				if (sk > 0) str += th[(x-i-1)/3] + " ";
				sk=0;
			}
		} 
		if (x != s.length()) {
			int y = s.length();
			str += "point ";
			for (int i=x+1; i<y; i++) str += dg[null2int(n[i])] +" ";
		}
		return str.toUpperCase();
	}
	
	
	public static String getOnlyNumberChar(String org){
		StringBuffer buf = new StringBuffer();

		if(org == null) org = "";

		for(int i=0 ; i<org.length() ; i++){

			char ch = org.charAt(i);
			if(ch >= '0' && ch <= '9'){
				buf.append(ch);
			}
		}

		return buf.toString();
	}

	// password return
	//    public static String getPassword(String password){
	//    	String hash = password == null ? "" : password;
	//    	
	//    	if("".equals(hash)) return "";
	//    	
	//    	try{
	//			MessageDigest md = MessageDigest.getInstance("SHA1");
	//			MessageDigest md = MessageDigest.getInstance("SHA-256");
	//			
	//			md.update(password.getBytes("UTF-8"));
	//			
	//			byte[] raw = md.digest();
	//			
	//			hash = (new BASE64Encoder()).encode(raw);
	//    	}catch(Exception e){
	//    		log.error(Util.getPrintStacTraceString(e));
	//    	}
	//    	
	//    	return hash;
	//    }

	// 문자열을 이미지로
	@SuppressWarnings("unused")
	public static BufferedImage imgText(String text, String fontUrl){
		BufferedImage buffer = null;

		try{
			float size           = 20.0f;
			String fontName      = "";
			Color background     = Color.lightGray;
			Color color          = Color.black;

			Font font            = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(fontUrl));
			font                 = font.deriveFont(size);

			buffer               = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2        = buffer.createGraphics();

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			FontRenderContext fc = g2.getFontRenderContext();
			Rectangle2D bounds   = font.getStringBounds(text, fc);

			int width            = (int)bounds.getWidth();
			int height           = (int)bounds.getHeight();

			buffer               = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			g2                   = buffer.createGraphics();

			g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setFont          (font);
			g2.setColor         (background);
			g2.fillRect         (0, 0, width, height);
			g2.setColor         (color);
			g2.drawString       (text, 0, (int)-bounds.getY());

		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
		}

		return buffer;
	}

	// 랜덤 숫자를 자리 수에 맞게 리턴
	public static int getRandomNum(int size){
		if(size == 0 || size > 9) return 0;

		int maxInt   = 10;
		int smallInt = 10;

		for(int i=0 ; i<size - 1 ; i++){
			maxInt = maxInt * 10;
		}
		for(int i=0 ; i<size - 2 ; i++){
			smallInt = smallInt * 10;
		}

//		Random random = new Random();
		int num       = 0;

		do{
			num = random.nextInt(maxInt - 1);
		}while(size != 1 && num < smallInt);

		return num;
	}

	//현재시간
	public static String getNow(){
		return getNow(null);
	}

	public static String getNow(String format){
		String date = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(System.currentTimeMillis()));

		if(format == null || format == ""){
			format = "yyyy-MM-dd HH:mm:ss";
		}

		date = new SimpleDateFormat(format).format(cal.getTime());

		return date;
	}

	//String 날짜 Date타입 변환
	public static Date strToDate(String strDate, String format){
		SimpleDateFormat f = null;
		if(format == null || format == ""){
			f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}else{
			f = new SimpleDateFormat(format);
		}

		Date dateTime 	   = new Date();
		try {
			dateTime = f.parse(strDate);
		} catch (ParseException e) {
			log.error(Util.getPrintStacTraceString(e));
		}
		return dateTime;

	}

	//String 데이터타입 format변경
	public static String dateStrToStr(String strDate, String format){
		SimpleDateFormat f = null;
		Date dateTime = new Date();
		String rtnStr = "";
		
		if(format == null || format == ""){
			f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}else{
			f = new SimpleDateFormat(format);
		}
		 
		try {
			dateTime = f.parse(strDate);
			rtnStr = f.format(dateTime);
		} catch (ParseException e) {
			// 2014.09.12 blue
			try{
				rtnStr =  dateStrToStr2(strDate, "yyyy-MM-dd", format);
            }catch(Exception e2){
            	log.error(Util.getPrintStacTraceString(e2));
            }
		}
		return rtnStr;

	}
	
	//String 데이터타입 format변경
	public static String dateStrToStr(String strDate, String format, Locale locale){
		SimpleDateFormat f = null;
		
		if(format == null || format == ""){
			f = new SimpleDateFormat("yyyy-MM-dd", new Locale("ko", "KR"));
		}else if(locale == null){
			f = new SimpleDateFormat(format);
		}else{
			f = new SimpleDateFormat(format, locale);
		}

		return f.format(strDate);

	}
	
	//String 데이터타입 format변경
	public static String dateStrToStr2(String strDate, String oldFormat, String newFormat){
		SimpleDateFormat oldF = null;
		SimpleDateFormat newF = null;
		new SimpleDateFormat("yyyy-MM-dd"); 
		Date dateTime = new Date();
		if(oldFormat == null || oldFormat == "")	oldF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		else										oldF = new SimpleDateFormat(oldFormat);
		
		if(newFormat == null || newFormat == "")	newF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		else										newF = new SimpleDateFormat(newFormat);
		 
		try {
			dateTime = oldF.parse(strDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.error(Util.getPrintStacTraceString(e));
		}
		return newF.format(dateTime);

	}
	
	//String 데이터타입 format변경
	public static String dateStrToStr2(String strDate, String oldFormat, String newFormat, Locale locale){
		SimpleDateFormat oldF = null;
		SimpleDateFormat newF = null;
		new SimpleDateFormat("yyyy-MM-dd"); 
		Date dateTime = new Date();
		if(oldFormat == null || oldFormat == "")	oldF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		else										oldF = new SimpleDateFormat(oldFormat);
		
		if(newFormat == null || newFormat == "")	newF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
		else										newF = new SimpleDateFormat(newFormat, locale);
		 
		try {
			dateTime = oldF.parse(strDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.error(Util.getPrintStacTraceString(e));
		}
		return newF.format(dateTime);
	}

	public static int getInt(String str){
		try{
			if(str == null){
				return 0;
			}else{
				return Integer.parseInt(str);
			}
		}catch(Exception e){
			return 0;
		}
	}
	
	/**
	 * 숫자형인지 판단한다.
	 * 
	 * @param str
	 * @return
	 */
    public static boolean isNumber(String str) {
        if (str == null)
            return false;
        Pattern p = Pattern.compile("([\\p{Digit}]+)(([\\p{Digit}]+))?");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * DB 접속 도입사 코드 체크
     * @param code
     * @return
     */
    public static boolean checkEnterprise(String code) {
    	boolean result = false;
    	
    	for (int i = 0; i < staticCustomProperties.getSystemDbList().length; i++) {
    		if (code.equals(staticCustomProperties.getSystemDbList()[i])) {
    			result = true;
    			break;
    		}
    	}
    	
    	for (int i = 0; i < staticCustomProperties.getEntDbList().length; i++) {
    		if (code.equals(staticCustomProperties.getEntDbList()[i])) {
    			result = true;
    			break;
    		}
    	}
    	
    	return result;
    }
    
    /**
     * DB 선택
     * @param request
     * @param country
     * @param trailer
     * @return
     */
    public static boolean setDB(HttpServletRequest request, String code) {
		if (!checkEnterprise(code))	return false;
		
		HttpSession session = request.getSession(true);
		session.setAttribute("db_key", code);
		
		return true;
    }

    /**
     * Client 접속 IP 가져오기
     * @param request
     * @return
     */
    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
    
    public static void getCryptographyYn(Object params) throws Exception {
    	getCryptographyYn(params, null, null);
    }
    public static void getCryptographyYn(Object params, String colKey) throws Exception {
    	getCryptographyYn(params, colKey, null);
    }
    public static void getCryptographyYn(Object params, String colKey, String cryptographyKey) throws Exception {
    	if(colKey == null)			colKey = "srchNoType";
    	if(cryptographyKey == null)	cryptographyKey = "dynamicCryptographyYn";
    	if(params instanceof Map){
    		Map map = (Map)params;
    		if("Y".equals(map.get(cryptographyKey))){
    			String column = Util.null2str(map.get(colKey));
    			int idx = Util.null2str(column).indexOf(".");
    			if(idx > -1){
    				column = column.substring(idx + 1);
    			}
    			for(String cryptographyCol : cryptographyCols){
    	    		if(cryptographyCol.equals(column)){
//    	    			if(cryptographyCol.equals("CONSIGNEE_NAME_LOC")){
//    	    				map.put(cryptographyKey, "YY");
//    	    			}else{
    	    				map.put(cryptographyKey, "Y");
//    	    			}
    	    			return;
    	    		}
    	    	}
    			map.put(cryptographyKey, "N");
    		}
    	}else{
    		Class<?> cls = params.getClass();
    		Field field = null;
    		Field field2 = null;
			try{
				field = cls.getDeclaredField(cryptographyKey);
			}catch(NoSuchFieldException e){
				field = cls.getSuperclass().getDeclaredField(cryptographyKey);
			}
			field.setAccessible(true);
			
			if("Y".equals(field.get(params))){
				try{
					field2 = cls.getDeclaredField(colKey);
				}catch(NoSuchFieldException e){
					field2 = cls.getSuperclass().getDeclaredField(colKey);
				}
				field2.setAccessible(true);
				
				String column = Util.null2str(field2.get(params));
				int idx = Util.null2str(column).indexOf(".");
				if(idx > -1){
					column = column.substring(idx + 1);
				}
				
				for(String cryptographyCol : cryptographyCols){
		    		if(cryptographyCol.equals(column)){
//		    			if(cryptographyCol.equals("CONSIGNEE_NAME_LOC")){
//		    				field.set(params, "YY");
//    	    			}else{
    	    				field.set(params, "Y");
//    	    			}
		    			return;
		    		}
		    	}
				field.set(params, "N");
			}
    	}
    }
	public static void camelCaseToUnderscore(Object search) throws Exception {
		Method getFilter = search.getClass().getMethod("getFilter");
		Method getSort = search.getClass().getMethod("getSort");
		String[] exceptArr = {"specialCode1", "specialCode2", "specialCode3", "remarks1", "remarks2", "remarks3"};
		
		/** 
		 * 오류 발생시 확인
		 * cdType1Cnt
		 * ([a-z])([A-Z]+) : CD_TYPE1CNT
		 * ([a-z0-9])([A-Z]+) : CD_TYPE1_CNT
		 */
		Filter filter = (Filter)getFilter.invoke(search);
		if(filter != null){
			List<Filters> filterList = filter.getFilters();
			if(filterList != null){
				for(int i = 0; i < filterList.size(); i++){
//					filterList.get(i).setField(filterList.get(i).getField().replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase());
					String field = filterList.get(i).getField();
					if("lastPod5tlStatus".equals(field)) {
						filterList.get(i).setField(field.replaceAll("([a-z])([A-Z0-9])", "$1_$2").toUpperCase()); //-> LAST_POD_5TL_STATUS
					} else {
						filterList.get(i).setField(filterList.get(i).getField().replaceAll("([a-z0-9])([A-Z]+)", "$1_$2").toUpperCase());
					}
					for(String s :exceptArr) {
						if(s.equals(field)) {
							//-> SPECIAL_CODE_1, REMARKS_1
							filterList.get(i).setField(field.replaceAll("([a-z])([A-Z0-9])", "$1_$2").replaceAll("([0-9])([a-zA-Z])", "$1_$2").toUpperCase());
						}
					}
				}
			}
		}
		List<Sort> sortList = (List<Sort>)getSort.invoke(search);
		if(sortList != null){
			for(int i = 0; i < sortList.size(); i++){
//				sortList.get(i).setField(sortList.get(i).getField().replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase());
				String field = sortList.get(i).getField();
				if("lastPod5tlStatus".equals(field)) {
					sortList.get(i).setField(field.replaceAll("([a-z])([A-Z0-9])", "$1_$2").toUpperCase()); //-> LAST_POD_5TL_STATUS
				} else {
					sortList.get(i).setField(sortList.get(i).getField().replaceAll("([a-z0-9])([A-Z]+)", "$1_$2").toUpperCase());
				}
				for(String s :exceptArr) {
					if(s.equals(field)) {
						//-> SPECIAL_CODE_1, REMARKS_1
						sortList.get(i).setField(field.replaceAll("([a-z])([A-Z0-9])", "$1_$2").replaceAll("([0-9])([a-zA-Z])", "$1_$2").toUpperCase());
					}
				}
			}
		}
	}
	public static void camelCaseToUnderscoreMap(Map<String, Object> search) throws Exception {
		Map<String, Object> filter = (Map<String, Object>)search.get("filter");
		if(filter != null){
			List<Map<String, Object>> filterList = (List<Map<String, Object>>)filter.get("filters");
			if(filterList != null){
				for(int i = 0; i < filterList.size(); i++){
//					filterList.get(i).setField(filterList.get(i).getField().replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase());
					filterList.get(i).put("field", Util.null2str(filterList.get(i).get("field")).replaceAll("([a-z0-9])([A-Z]+)", "$1_$2").toUpperCase());
				}
			}
		}
		
		List<Map<String, Object>> sortList = (List<Map<String, Object>>)search.get("sort");
		if(sortList != null){
			for(int i = 0; i < sortList.size(); i++){
//				sortList.get(i).setField(sortList.get(i).getField().replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase());
				sortList.get(i).put("field", Util.null2str(sortList.get(i).get("field")).replaceAll("([a-z0-9])([A-Z]+)", "$1_$2").toUpperCase());
			}
		}
	}
	public static void escapeNestedDataForXss(Object data, String keyList) throws Exception {
		if (data == null) {
            return;
        }
		
		if (data instanceof List) {
            // List 처리
            List<Object> list = (List<Object>) data;
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof String) {
                    // String 값 이스케이프 처리
                    list.set(i, StringEscapeUtils.escapeHtml4((String) item));
                } else {
                    // List 또는 Map 같은 중첩 구조 재귀 처리
                    escapeNestedDataForXss(item, null);
                }
            }
        } else if (data instanceof Map) {
            // Map 처리
            Map<Object, Object> map = (Map<Object, Object>) data;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    // String 값 이스케이프 처리
                    map.put(entry.getKey(), StringEscapeUtils.escapeHtml4((String) value));
                } else {
                    // List 또는 Map 같은 중첩 구조 재귀 처리
                    escapeNestedDataForXss(value, null);
                }
            }
        } else {
            // 클래스 객체인 경우 지정한 필드만 처리
        	Class<?> cls = data.getClass();
			Field field = null;

			if(keyList != null) {
				for(String key : keyList.split("\\|")){
					try{
						field = cls.getDeclaredField(key);
					}catch(NoSuchFieldException e){
						field = cls.getSuperclass().getDeclaredField(key);
					}
					field.setAccessible(true);
					field.set(data, StringEscapeUtils.escapeHtml4(Util.null2str(field.get(data))));
				}
			}
        }
    }
	public static void accessControlDatabase(Object params, String keyList, boolean pageBottom, String srchDateKey, String srchDateKeyList, String srchNoKey, String srchNoKeyList) throws Exception {
		boolean flag;
		if(params instanceof Map){
			Map map = (Map)params;
			if(keyList != null) {
				for(String key : keyList.split("\\|")){
					if(Util.null2str(map.get(key)).matches(accessControlDB)){
						log.error("Access Control: Database - " + key + ":" + Util.null2str(map.get(key)));
						throw new Exception("Access Control: Database");
					}
				}
			}
			if(srchDateKey != null){
				flag = false;
				String srchDateGbn = Util.null2str(map.get(srchDateKey));
				for(String key : srchDateKeyList.split("\\|")){
					if(srchDateGbn.equals(key)){
						flag = true;
						break;
					}
				}
				if(!flag){
					log.error("Access Control: Database(srchDate) - " + srchDateGbn + "(" + srchDateKeyList + ")");
					throw new Exception("Access Control: Database(srchDate)");
				}
			}
			if(srchNoKey != null){
				flag = false;
				String srchNoType = Util.null2str(map.get(srchNoKey));
				for(String key : srchNoKeyList.split("\\|")){
					if(srchNoType.equals(key)){
						flag = true;
						break;
					}
				}
				if(!flag){
					log.error("Access Control: Database(srchNo) - " + srchNoType + "(" + srchNoKeyList + ")");
					throw new Exception("Access Control: Database(srchNo)");
				}
			}
		}else{
			Class<?> cls = params.getClass();
			Field field = null;
			if(pageBottom){
				flag = false;
				try{
					try{
						field = cls.getDeclaredField("sort");
					}catch(NoSuchFieldException e){
						field = cls.getSuperclass().getDeclaredField("sort");
					}
					flag = true;
				}catch(Exception e){log.error(e.getMessage());}
				if(flag){
					field.setAccessible(true);
					List<Sort> sortList = (List<Sort>)field.get(params);
					if(sortList != null){
						for(int i = 0; i < sortList.size(); i++){
							if(Util.null2str(sortList.get(i).getField()).matches(accessControlDB)){
								log.error("Access Control: Database(sort) - " + sortList.get(i).getField());
								throw new Exception("Access Control: Database(sort)");
							}
						}
					}
				}
				if(keyList == null) {
					keyList = "";
				}else{
					keyList += "|";
				}
				keyList += "defaultSort|defaultSort2|startNum|pageSize|srchDateFormat";
			}
			
			
			
			if(keyList != null) {
				for(String key : keyList.split("\\|")){
					try{
						field = cls.getDeclaredField(key);
					}catch(NoSuchFieldException e){
						field = cls.getSuperclass().getDeclaredField(key);
					}
					field.setAccessible(true);
					if(Util.null2str(field.get(params)).matches(accessControlDB)){
						log.error("Access Control: Database - " + key + ":" + Util.null2str(field.get(params)));
						throw new Exception("Access Control: Database");
					}
				}
			}
			if(srchDateKey != null){
				flag = false;
				try{
					field = cls.getDeclaredField(srchDateKey);
				}catch(NoSuchFieldException e){
					field = cls.getSuperclass().getDeclaredField(srchDateKey);
				}
				field.setAccessible(true);
				String srchDateGbn = Util.null2str(field.get(params));
				for(String key : srchDateKeyList.split("\\|")){
					if(srchDateGbn.equals(key)){
						flag = true;
						break;
					}
				}
				if(!flag){
					log.error("Access Control: Database(srchDate) - " + srchDateGbn + "(" + srchDateKeyList + ")");
					throw new Exception("Access Control: Database(srchDate)");
				}
			}
			if(srchNoKey != null){
				flag = false;
				try{
					field = cls.getDeclaredField(srchNoKey);
				}catch(NoSuchFieldException e){
					field = cls.getSuperclass().getDeclaredField(srchNoKey);
				}
				field.setAccessible(true);
				String srchNoType = Util.null2str(field.get(params));
				for(String key : srchNoKeyList.split("\\|")){
					if(srchNoType.equals(key)){
						flag = true;
						break;
					}
				}
				if(!flag){
					log.error("Access Control: Database(srchNo) - " + srchNoType + "(" + srchNoKeyList + ")");
					throw new Exception("Access Control: Database(srchNo)");
				}
			}
		}
	}
	
	public static List<Map<String, Object>> integrateListByKey(List<Map<String, Object>> dataList, String groupKey) {
	    Map<String, Map<String, Object>> resultMap = new LinkedHashMap<>();

	    for (Map<String, Object> row : dataList) {
	        // groupKey가 없거나 null이면 바로 NPE 발생 가능 (의도한 대로)
	        String groupVal = (String) row.get(groupKey);

	        Map<String, Object> existing = resultMap.getOrDefault(groupVal, new HashMap<>());
	        existing.put(groupKey, groupVal);

	        for (Map.Entry<String, Object> entry : row.entrySet()) {
	            String key = entry.getKey();
	            Object value = entry.getValue();

	            if (key.equals(groupKey)) continue;
	            if (value == null) continue;
	            if (value instanceof String && ((String) value).isBlank()) continue;

	            existing.put(key, value);
	        }

	        resultMap.put(groupVal, existing);
	    }

	    return new ArrayList<>(resultMap.values());
	}
	
	/**
	 * type, length에 맞춰서 문자열 리턴
	 * 		"String", "20", "한글1한글"		=> [한글1한글           ]
	 * 		"Number", "15", "12345"		=> [000000000012345]
	 * 		"Number", "15,2", "123.45"	=> [000000000123.45]
	 * )
	 * @param value
	 * @param type		- String, Number
	 * @param length		- Ex) '20', '15', '15,2'
	 * @return
	 * @throws Exception
	 */
	public static String getFileWriteData(String value, String type, String length) throws Exception {
		BigDecimal tempNumber = null;
		
		String retStr = "";
		String lengthGubun[] = null;
		
		if("String".equals(type)) {
			retStr = Util.rpad(value, Integer.parseInt(length), ' ');
		} else if("Number".equals(type)) {
			// 소수점 포함된 자료일 경우
			if(length.indexOf(",") != -1) {
				lengthGubun = length.split(",");
				tempNumber = new BigDecimal(value).setScale(Integer.parseInt(lengthGubun[1]), BigDecimal.ROUND_DOWN);
				retStr = Util.lpad(tempNumber.toString(), Integer.parseInt(lengthGubun[0]), '0');
			} else {
				retStr = Util.lpad(value, null2int(length), '0');
			}
		} else {
			throw new Exception(type.toString() + " 해당 항목의 타입이 존재하지 않습니다.");
		}
		
		return retStr;
	}
	/** 
	 * 		"Number", "15", "12345"		=> [12345          ]
	 * 		"Number", "15,2", "123.45"	=> [123.45         ]
	 */
	public static String getFileWriteData2(String value, String type, String length) throws Exception {
		BigDecimal tempNumber = null;
		
		String retStr = "";
		String lengthGubun[] = null;
		
		if("String".equals(type)) {
			retStr = Util.rpad(value, Integer.parseInt(length), ' ');
		} else if("Number".equals(type)) {
			// 소수점 포함된 자료일 경우
			if(length.indexOf(",") != -1) {
				lengthGubun = length.split(",");
				tempNumber = new BigDecimal(value).setScale(Integer.parseInt(lengthGubun[1]), BigDecimal.ROUND_DOWN);
				retStr = Util.rpad(tempNumber.toString(), Integer.parseInt(lengthGubun[0]), ' ');
			} else {
				retStr = Util.rpad(value, null2int(length), ' ');
			}
		} else {
			throw new Exception(type.toString() + " 해당 항목의 타입이 존재하지 않습니다.");
		}
		
		return retStr;
	}
	
	public static String rpad(String strSource, int iLength) throws Exception {
		return rpad(strSource, iLength, ' ');
	}
	/**
	  * 주어진 길이(iLength)만큼 주어진 문자(cPadder)를 strSource의 오른쪽에 붙혀서 보내준다.
	  * ex) lpad("abc", 5, '^') ==> "abc^^"
	  *     lpad("abcdefghi", 5, '^') ==> "abcde"
	  *     lpad(null, 5, '^') ==> "^^^^^"
	  *
	  * @param strSource
	  * @param iLength
	  * @param cPadder
	  */
	public static String rpad(String strSource, int iLength, char cPadder) throws Exception {
		StringBuffer sbBuffer = null;
		
		// 길이가 '0' 일 경우
		if(iLength == 0) {
			return strSource;
		}
		
		if (!"".equals(strSource)) {
			int iByteSize = getKor2ByteLength(strSource);
			
			if (iByteSize > iLength) {
				return rpad(getKOR2bytePassing(strSource, iLength), iLength, cPadder);
			} else if (iByteSize == iLength) {
				return strSource;
			} else {
				int iPadLength = iLength - iByteSize;
				sbBuffer = new StringBuffer(strSource);
				
				for (int j = 0; j < iPadLength; j++) {
					sbBuffer.append(cPadder);
				}
				return sbBuffer.toString();
			}
		}
		
		sbBuffer = new StringBuffer();
		
		for (int j = 0; j < iLength; j++) {
			sbBuffer.append(cPadder);
		}
		
		return sbBuffer.toString();
	}
	public static String rpad2(String strSource, int iLength) throws Exception {
		return rpad2(strSource, iLength, ' ', false);
	}
	public static String rpad2(Object objSource, int iLength, boolean trim) throws Exception {
		return rpad2(Util.null2str(objSource), iLength, ' ', trim);
	}
	public static String rpad2(String strSource, int iLength, char cPadder, boolean trim) throws Exception {
		StringBuffer sbBuffer = null;
		
		// 길이가 '0' 일 경우
		if(iLength == 0 || iLength > 1000) {	/** 취약성점검추가(Denial of Service: StringBuilder) */
			return strSource;
		}

		if(trim){
			strSource = strSource.trim();
		}
		if (!"".equals(strSource)) {
			int iByteSize = strSource.length();
			
			// 파일길이
			if (iByteSize > iLength) {
				return strSource.substring(0, iLength);
			} else if (iByteSize == iLength) {
				return strSource;
			} else {
				sbBuffer = new StringBuffer(strSource);
				
				int iPadLength = iLength - iByteSize;
				for (int j = 0; j < iPadLength; j++) {
					sbBuffer.append(cPadder);
				}
				
				return sbBuffer.toString();
			}
		}
		
		sbBuffer = new StringBuffer();
		
		for (int j = 0; j < iLength; j++) {
			sbBuffer.append(cPadder);
		}
		return sbBuffer.toString();
	}
	
	/**
	 * 한글일 경우 길이를 한글자의 길이를 '2' 로 계산후 리턴 
	 * 
	 * @param line
	 * @return
	 * @throws Exception
	 */
	public static int getKor2ByteLength(String str) {
		if (str == null || str.length() == 0) {
			return 0;
		}
		int byteLength = 0;
		for(int index = 0 ; index < str.length() ; ++index) {
			if(is1Byte(str.substring(index, index+1))) {
				//1byte이면 길이에 1을 더한다.
				byteLength += 1;
			} else {
				//2byte이면 길이에 2을 더한다.
				byteLength += 2;
			}
		}
		
		return byteLength;
	}
	
	/**
	 * 2byte 문자 여부
	 * 
	 * @param data	- 문자
	 * @return			- 1byte:false, 2byte:true
	 */
	public static boolean is1Byte(String data) {
		char[] charArray = data.toCharArray();
		
		if( charArray[ 0 ] < 256) {
			// 1byte character 이면
			return true;
		}
		else {
			// 2byte character 이면
			return false;
		}
	}
	
	/**
	 * 한글을 2byte로 계산해서 문자열 리턴
	 * 
	 * @param str			- 문자열
	 * @param cutByte	- 문자열 제한 byte 길이
	 * @return
	 * @throws Exception
	 */
	public static String getKOR2bytePassing(Object str, int cutByte) throws Exception {
		return getKOR2bytePassing(null2str(str), cutByte);
	}
	public static String getKOR2bytePassing(String str, int cutByte) throws Exception {
		char[] charArray = str.toCharArray();
		
		int itemMaxByteLength = cutByte;	// 파일 내용의 항목별 길이
		int lineReadIndex = 0;					// 문자열을 한문자씩 읽는 인덱스
		int readByteLengthByitem = 0;		// 현재 읽은 항목의 길이
		int readCharByteSize = 0;
		
		StringBuffer stringBuffer = new StringBuffer();
		
		for(; lineReadIndex < charArray.length;) {
			if(charArray[ lineReadIndex ] < 256) {
				// 1byte character 이면
				readCharByteSize = 1;				// 현재 읽은 문자 길이
			}
			else {
				// 2byte character 이면
				readCharByteSize = 2;				// 현재 읽은 문자 길이
			}
			
			readByteLengthByitem += readCharByteSize;
			
			// 읽은 길이가 제한 길이보다 크거나 같으면 종료
			if(readByteLengthByitem >= itemMaxByteLength) {
				// 종료전 길이가 같은 경우만 문자추가함.
				if(readByteLengthByitem == itemMaxByteLength) {
					stringBuffer.append(charArray[ lineReadIndex ]);
				}
				break;
			} else {
				stringBuffer.append(charArray[ lineReadIndex ]);
			}
			
			++lineReadIndex;
		}
		
		return stringBuffer.toString();
	}
	
	public static double getMaxDbl(Object num, double maxDbl) throws Exception {
		double returnNum = null2dbl(num);
		if(returnNum > maxDbl){
			returnNum = maxDbl;
		}
		return returnNum;
	}
	public static int getMaxInt(Object num, int maxInt) throws Exception {
		int returnNum = null2int(num);
		if(returnNum > maxInt){
			returnNum = maxInt;
		}
		return returnNum;
	}
	public static int getMssqlInt(Object num, String type) throws Exception {
		int returnNum = null2int(num);
		if("smallint".equals(type)){
			if(returnNum > 32767){
				returnNum = 32767;
			}
		}
		return returnNum;
	}
	public static String setSeqNo(int seqNo) throws Exception {
		return setSeqNo(seqNo, 5);
	}
	public static String setSeqNo(int seqNo, int length) throws Exception {
		String seqNoStr = null2str(seqNo);
		int seqNoLength = seqNoStr.length();
		if(seqNoLength > length){
			seqNoStr = seqNoStr.substring(seqNoLength - length);
		}
		return lpad(seqNoStr, length, '0');
	}
	/**
	  * 주어진 길이(iLength)만큼 주어진 문자(cPadder)를 strSource의 왼쪽에 붙혀서 보내준다.
	  * ex) lpad("abc", 5, '^') ==> "^^abc"
	  *     lpad("abcdefghi", 5, '^') ==> "abcde"
	  *     lpad(null, 5, '^') ==> "^^^^^"
	  *
	  * @param strSource
	  * @param iLength
	  * @param cPadder
	  */
	public static String lpad(String strSource, int iLength, char cPadder) throws Exception {
		StringBuffer sbBuffer = null;
		
		// 길이가 '0' 일 경우
		if(iLength == 0 || iLength > 1000) {	/** 취약성점검추가(Denial of Service: StringBuilder) */
			return strSource;
		}
				
		if (!"".equals(strSource)) {
			int iByteSize = getKor2ByteLength(strSource);
			
			// 파일길이
			if (iByteSize > iLength) {
				return lpad(getKOR2bytePassing(strSource, iLength), iLength, cPadder);
			} else if (iByteSize == iLength) {
				return strSource;
			} else {
				int iPadLength = iLength - iByteSize;
				sbBuffer = new StringBuffer();
				
				for (int j = 0; j < iPadLength; j++) {
					sbBuffer.append(cPadder);
				}
				
				sbBuffer.append(strSource);
				
				return sbBuffer.toString();
			}
		}
		
		sbBuffer = new StringBuffer();
		
		for (int j = 0; j < iLength; j++) {
			sbBuffer.append(cPadder);
		}
		return sbBuffer.toString();
	}
	
	/**
	 * type, length에 맞춰서 문자열 리턴
	 * 		"String", "20", "한글1한글"		=> [한글1한글           ] => 공백제거후[한글1한글]
	 * 		"Number", "15", "12345"		=> [000000000012345]
	 * 		"Number", "15,2", "123.45"	=> [000000000123.45]
	 * )
	 * @param value
	 * @param type		- String, Number
	 * @param length		- Ex) '20', '15', '15,2'
	 * @return
	 * @throws Exception
	 */
	public static String getFileWriteDataWithRtrim(String value, String type, String length) throws Exception {
		return getFileWriteData(value, type, length).trim();
	}
	
	/**
	 * 한글포함 substring 구현
	 * @param strData
	 * @param iStartPos
	 * @param iByteLength
	 * @return
	 */
	public static String subString(String strData, int iStartPos, int iByteLength) {
		byte[] bytTemp = null;
		int iRealStart = 0;
		int iRealEnd = 0;
		int iLength = 0;
		int iChar = 0;
		try {
			// UTF-8로 변환하는경우 한글 2Byte, 기타 1Byte로 떨어짐
			bytTemp = strData.getBytes("EUC-KR");
			iLength = bytTemp.length;
			for (int iIndex = 0; iIndex < iLength; iIndex++) {
				if(iStartPos <= iIndex)   break;
				iChar = (int)bytTemp[iIndex];
				if ((iChar > 127)|| (iChar < 0)) {
					// 한글의 경우(2byte 통과처리)
					// 한글은 2Byte이기 때문에 다음 글자는 볼것도 없이 스킵한다
					iRealStart++;
					iIndex++;
				} else {
					// 기타 글씨(1Byte 통과처리)
					iRealStart++;
				}
			}
			iRealEnd = iRealStart;
			int iEndLength = iRealStart + iByteLength;
			for (int iIndex = iRealStart; iIndex < iEndLength; iIndex++) {
				iChar = (int)bytTemp[iIndex];
				if ((iChar > 127) || (iChar < 0)) {
					// 한글의 경우(2byte 통과처리)
					// 한글은 2Byte이기 때문에 다음 글자는 볼것도 없이 스킵한다
					iRealEnd++;
					iIndex++;
				} else {
					// 기타 글씨(1Byte 통과처리)
					iRealEnd++;
				}
			}
		} catch(Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		}
		// param iByteLength = 0 이라면 iRealStart부터 끝까지 substring
		return (iByteLength == 0 ? strData.substring(iRealStart) : strData.substring(iRealStart, iRealEnd));
	}
	
	/**
	 * type, length, start, end 에 맞춰서 문자열 리턴
	 * 
	 * @param readLine	- 문자열
	 * @param type		- String, Number
	 * @param length	- Ex) '20', '15', '15,2'
	 * @param start		- 읽기 시작위치
	 * @param end		- 일기 끝위치
	 * @return
	 * @throws Exception
	 */
	public static String getFileReadData(String readLine, String type, String length, int start, int end) throws Exception {
		char[] charArray = readLine.toCharArray();
		
		String lengthGubun[] = null;
		StringBuffer stringBuffer = new StringBuffer();
		
		if("String".equals(type)) {
			lengthGubun = length.split(",");
		} else if("Number".equals(type)) {
			// 소수점 포함된 자료일 경우
			if(length.indexOf(",") != -1) {
				lengthGubun = length.split(",");
			} else {
				lengthGubun = length.split(",");
			}
		} else {
			throw new Exception(type.toString() + " 해당 항목의 타입이 존재하지 않습니다.");
		}
		
		int itemMaxByteLength = Integer.parseInt(lengthGubun[0]);	// 파일 내용의 항목별 길이
		int lineReadIndex = 0;				// 문자열을 한문자씩 읽는 인덱스
		int readByteLengthByitem = 0;	// 현재 읽은 항목의 길이
		int readByteLengthByline = 0;	// 현재 읽은 항목의 전체 길이
		int readCharByteSize = 0;			// 현재 읽은 문자 하나의 byte 길이
		
		for(; lineReadIndex < charArray.length;) {
			if(charArray[ lineReadIndex ] < 256) {
				// 1byte character 이면
				readByteLengthByline += 1;		// 읽은 항목의 전체 길이
				readCharByteSize = 1;
			}
			else {
				// 2byte character 이면
				readByteLengthByline += 2;		// 항목의 전체 길이 구하기
				readCharByteSize = 2;
			}
			
			// 읽은 전체 길이가 시작길이, 끝길이 사이값일 경우 
			// start + 1 이유는 substring(4, 6) 일 경우 
			if((readByteLengthByline - 1) >= start && readByteLengthByline <= end) {
				stringBuffer.append(charArray[ lineReadIndex ]);
				
				readByteLengthByitem += readCharByteSize;
				
				// 읽은 길이가 제한 길이보다 크거나 같으면 종료
				if(readByteLengthByitem >= itemMaxByteLength) {
					break;
				}
			}
			
			++lineReadIndex;
		}
		
		BigDecimal tempNumber = null;
		
		if("String".equals(type)) {
			lengthGubun = length.split(",");
		} else if("Number".equals(type)) {
			// 소수점 포함된 자료일 경우
			if(length.indexOf(",") != -1) {
				tempNumber = new BigDecimal(stringBuffer.toString()).setScale(Integer.parseInt(lengthGubun[1]), BigDecimal.ROUND_DOWN);
				stringBuffer = new StringBuffer(tempNumber.toString());
			}
		} else {
			throw new Exception(type.toString() + " 해당 항목의 타입이 존재하지 않습니다.");
		}
		
		return stringBuffer.toString();
	}
	
	public static void main(String[] args) {
		try {

		} catch(Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		}
	}
	
	/** 엑셀 열 알파벳을 숫자로 변환 */
	public static int excelEng2int(Object obj){
		String str = null2str(obj);
		if(str.equals("")){
			return 0;
		}
		int returnInt = 0;
		char[] data = str.toCharArray();
		int cnt = 0;
		for(int i = data.length - 1; i >= 0; i--){
			returnInt += ((int)data[cnt++] - 64) * Math.pow(26, i);
		}
		return returnInt - 1;
    }
	/** 엑셀 숫자를 열 알파벳으로 변환 */
	public static String int2excelEng(int number) {
		StringBuilder sb = new StringBuilder();
		while (number-- > 0) {
			sb.append((char)('A' + (number % 26)));
			number /= 26;
		}
		return sb.reverse().toString();
	}
	
	public static String getPrintStacTraceString(Exception e){
		String rtn = "";
		try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream printStream = new PrintStream(out);
			e.printStackTrace(printStream);
			rtn = out.toString();
		}catch(Exception ee){log.error(ee.getMessage());}
		return rtn;
	}
	
	public static String convert2CamelCase(String underScore) {
		// '_' 가 나타나지 않으면 이미 camel case 로 가정함.
		// 단 첫째문자가 대문자이면 camel case 변환 (전체를 소문자로) 처리가
		// 필요하다고 가정함. --> 아래 로직을 수행하면 바뀜

		if(underScore.indexOf('_') < 0 && Character.isLowerCase(underScore.charAt(0))) return underScore;

		StringBuilder result = new StringBuilder();
		boolean nextUpper = false;
		int len = underScore.length();

		for (int i = 0; i < len; i++) {
			char currentChar = underScore.charAt(i);
			if(currentChar == '_') {
				nextUpper = true;
			}else {
				if(nextUpper) {
					result.append(Character.toUpperCase(currentChar));
					nextUpper = false;
				}else {
					result.append(Character.toLowerCase(currentChar));
				}
			}
		}

		return result.toString();
	}
	
	/* 특주 전각문자를 반각문자로 변환 */
	public static String fullWidthToHalfWidth(String str){
		
		// 특수문자 치환
		str = str.replaceAll("（", "(");
		str = str.replaceAll("）", ")");
		str = str.replaceAll("　", " ");
		str = str.replaceAll("：", ":");
		str = str.replaceAll("，", ",");
		str = str.replaceAll("ㅡ", "-");
		str = str.replaceAll("＆", "&");
		str = str.replaceAll("＃", "#");
		str = str.replaceAll("＊", "*");
		str = str.replaceAll("＠", "@");
		str = str.replaceAll("！", "!");
		str = str.replaceAll("＾", "^");
		str = str.replaceAll("％", "%");
		str = str.replaceAll("＄", "$");
		str = str.replaceAll("‘", "'");
		str = str.replaceAll("’", "'");
		str = str.replaceAll("′", "'");
		str = str.replaceAll(" ", " ");
		
		// 특수문자 제거
		str = str.replaceAll("<", ""); // xml태그인식으로 인해 제거 
		str = str.replaceAll(">", ""); // xml태그인식으로 인해 제거 
		str = str.replaceAll("\\\\", "");
		
		
		
		str = str.replaceAll("\\\\s", "''s");
		str = str.replaceAll("(\\r\\n|\\r|\\n|\\n\\r|\\\\)", " ");

		return str;		
	}	
	
	/**
	 * JSONObject를 Map으로 리턴
	 * @param jObject
	 * @return Map 데이타
	 * @exception Exception
	 */
	public static Map<String, Object> JSONObjectToMap(JSONObject jObject) throws Exception{
        Map<String, Object> map = new HashMap<String, Object>();
        for(Iterator<?> keys = jObject.keys(); keys.hasNext();){
            String key = (String)keys.next();
            try{
				if(jObject.get(key) instanceof JSONArray){
					map.put(key, JSONArrayToList(jObject.getJSONArray(key)));
				}else if(jObject.get(key) instanceof JSONObject){
					map.put(key, JSONObjectToMap(jObject.getJSONObject(key)));
				}else{
					map.put(key, jObject.get(key));
				}
			}catch (Exception e) {
				log.error(Util.getPrintStacTraceString(e));
			}
        }

        return map;
        
    }

	
	/**
	 * JSONArray를 List로 리턴
	 * @param jArray
	 * @return Map 데이타
	 * @exception Exception
	 */
    public static List JSONArrayToList(JSONArray jArray) throws Exception{
        List list = new ArrayList();
        for(int i = 0; i < jArray.length(); i++){
            try {
				if(jArray.get(i) instanceof JSONArray){
					list.add(JSONArrayToList((JSONArray)jArray.get(i)));
				}else if(jArray.get(i) instanceof JSONObject){
					list.add(JSONObjectToMap((JSONObject)jArray.get(i)));
				}else{
					list.add(jArray.get(i));
				}
			} catch (Exception e) {
				log.error(Util.getPrintStacTraceString(e));
			}
        }

        return list;
    }
  
    
    /**
	 * JSONObject형태의 스트링을 Map과 List로 리턴
	 * @param params
	 * @return Map 데이타
	 * @exception Exception
	 */
	public static Map<String, Object> stringToMap(String params) throws Exception{
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		try{
			if(!params.equals("undefined")) {
				JSONObject jObj = new JSONObject(params);
				map = JSONObjectToMap(jObj);				
			}
			
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
		}
		
		return map;
		
	}
	
	/**
	 * 문자열 Byte로 자르기 
	 */
	public static String[] strByteCut(String raw, int len){
		String encoding = "UTF-8";
				
		String[] ary = null;
		
		try {
			
			byte[] rawBytes = raw.getBytes(encoding);
			int rawLength = rawBytes.length;
			
			int index = 0;
			int minus_byte_num = 0;
			int offset = 0;
			   
			int hangul_byte_num = encoding.equals("UTF-8") ? 3 : 2;
			
			if(rawLength > len){
				int aryLength = (rawLength / len) + (rawLength % len != 0 ? 1 : 0);
				ary = new String[aryLength];
				
				for(int i=0; i<aryLength; i++){
					minus_byte_num = 0;
					offset = len;
					if(index + offset > rawBytes.length){
						offset = rawBytes.length - index;
					}
					
					for(int j=0; j<offset; j++){     
						if(((int)rawBytes[index + j] & 0x80) != 0){
							minus_byte_num ++;
						}
					}
					
					if(minus_byte_num % hangul_byte_num != 0){
						offset -= minus_byte_num % hangul_byte_num;
					}     
					
					ary[i] = new String(rawBytes, index, offset, encoding);     
					index += offset ;
				}    
			} else {
				ary = new String[]{raw};
			}
			
		} catch(Exception e) {
			log.error(Util.getPrintStacTraceString(e));	
		}
		
		return ary;
	}
	
	/**
     * 파일 읽어서 stream 반환
     * @param file
     * @return
     */
    public static InputStream getStreamFromResource(String file) {
        ClassLoader classLoader = Util.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(file);

        if (inputStream == null) {
            throw new IllegalArgumentException("file \"" + file + "\" not found.");
        } else {
            return inputStream;
        }
    }
	
    /**
	 * 날짜 차이 구하기
	 * 
	 * @return	date1-date2 
	 */
	public static long diffDate (Date date1, Date date2) {
		Calendar first = Calendar.getInstance();
		Calendar second = Calendar.getInstance();
		
		first.setTime(date1);
		second.setTime(date2);
		
		return (first.getTimeInMillis() - second.getTimeInMillis()) /(24*60*60*1000);
	}
	
	public static long diffDate (String date1, String date2) {
		
		return diffDate (Util.strToDate(date1,"yyyy-MM-dd"), Util.strToDate(date2,"yyyy-MM-dd"));
	}
	
	
	/**
	 * 특수문자 이스케이프 치환
	 * @param xmlData
	 * @return
	 */
	public static String escapeStr(String xmlData) {
		
		String escapeStr = xmlData.replace("&", "&amp;");
		
		return escapeStr;
	}
    
	/**
	 * EMS-KP, EMS환적 Stock 부여
	 * @param stockNo
	 * @param operation
	 * @return
	 */
	public static String createEmsKpStockDigit(String stockNo, boolean operation) {
		String rstStockNo = ""; 

		
		if(operation) {
			stockNo = String.valueOf(Integer.parseInt(stockNo.substring(0,8))+1);
		}else {
			stockNo = String.valueOf(Integer.parseInt(stockNo.substring(0,8))-1);
		}
		
		String[] stockNoArr = stockNo.split("");
		
		String No1 = stockNoArr[0];
		String No2 = stockNoArr[1];
		String No3 = stockNoArr[2];
		String No4 = stockNoArr[3];
		String No5 = stockNoArr[4];
		String No6 = stockNoArr[5];
		String No7 = stockNoArr[6];
		String No8 = stockNoArr[7];
		
		long weight1 = Long.parseLong(No1) * 8;
		long weight2 = Long.parseLong(No2) * 6;
		long weight3 = Long.parseLong(No3) * 4;
		long weight4 = Long.parseLong(No4) * 2;
		long weight5 = Long.parseLong(No5) * 3;
		long weight6 = Long.parseLong(No6) * 5;
		long weight7 = Long.parseLong(No7) * 9;
		long weight8 = Long.parseLong(No8) * 7;
		
		long step1 = weight1 + weight2 + weight3 + weight4 + weight5 + weight6 + weight7 + weight8;
		
		long step2 = step1 % 11;
				
		long step3 = 11 - step2;
				
		if(step3 == 10) {
			step3 = 0;
		} else if (step3 == 11) {
			step3 = 5;
		}
		
		String checkdigit = Long.toString(step3);
		rstStockNo = stockNo + checkdigit;

		return rstStockNo;
	}
	/**
     * 문자열을 CDATA로 리턴
     * @param str
     * @return
     */
    public static String getCdata(String str) {
    	return "<![CDATA[" + str + "]]>"; 
    }
}
