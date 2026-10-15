package org.yht.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.time.DateUtils;

import com.google.common.base.CaseFormat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Converter {

    public static String E2K(String en) {
        if(en == null) return en;

        try{
            return new String(en.getBytes("8859_1"), "KSC5601");
        } catch(Exception e){
        	log.error(e.getMessage());
        }
        return en;
    }

    public static String K2E(String ko) {
        if(ko == null) return ko;

        try{
            return new String(ko.getBytes("KSC5601"), "8859_1");
        } catch(Exception e){
        	log.error(e.getMessage());
        }
        return ko;
    }

    public static String kToe(String en){
		try{
			if( en == null) return en;

			return new String(en.getBytes("EUC-KR"),"8859_1");
		}catch(Exception e){
			return en;
		}
	}

    public static String eTok(String ko){
		try{
			if( ko == null) return ko;
			
			return new String(ko.getBytes("8859_1"),"EUC-KR");
		}catch(Exception e){
			return ko;
		}
	}
    
    public static String lowerFirst(String s) {
        if (s == null || s.length() < 1)
            return s;
        return (s.substring(0, 1).toLowerCase() + s.substring(1));
    }

    public static String strip(String s, String delims) {
        if (s == null || s.length() == 0 || delims == null)
            return s;

        StringBuffer sb = new StringBuffer();

        StringTokenizer st = new StringTokenizer(s, delims);
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
        }

        return sb.toString();
    }
    public static String upperFirst(String s) {
        if (s == null || s.length() < 1)
            return s;
        return (s.substring(0, 1).toUpperCase() + s.substring(1));
    }
    public static String arrayToString(String[] values, String gubun) {
        StringBuffer sb = new StringBuffer();
        if (values == null || values.length < 1)
            return "";
        sb.append(values[0]);
        for (int i = 1; i < values.length; i++) {
            sb.append(gubun).append(values[i]);
        }
        return sb.toString();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String[] stringToArray(String text, String gubun) {
        ArrayList array = new ArrayList();
        String cur = text;
        while (cur != null) {
            int i = cur.indexOf(gubun);
            if (i < 0) {
                array.add(cur);
                cur = null;
            } else {
                array.add(cur.substring(0, i));
                cur = cur.substring(i + gubun.length());
            }
        }
        return (String[]) array.toArray(new String[array.size()]);
    }

    public static String str2alert(String s) {
        if (s == null)
            return null;
        StringBuffer buf = new StringBuffer();
        char[] c = s.toCharArray();
        int len = c.length;
        for (int i = 0; i < len; i++) {
            if (c[i] == '\n')
                buf.append("\\n");
            else if (c[i] == '\t')
                buf.append("\\t");
            else if (c[i] == '"')
                buf.append("'");
            else
                buf.append(c[i]);
        }
        return buf.toString();
    }
    public static String java2msg(String s) {
        if (s == null)
            return null;
        StringBuffer buf = new StringBuffer();
        char[] c = s.toCharArray();
        int len = c.length;
        for (int i = 0; i < len; i++) {
            if (c[i] == '\n')
                buf.append("\\n");
            else if (c[i] == '\t')
                buf.append("\\t");
            else
                buf.append(c[i]);
        }
        return buf.toString();
    }
    public static String java2html(String s) {
        if (s == null)
            return null;
        StringBuffer buf = new StringBuffer();
        char[] c = s.toCharArray();
        int len = c.length;
        for (int i = 0; i < len; i++) {
            if (c[i] == '&')
                buf.append("&amp;");
            else if (c[i] == '<')
                buf.append("&lt;");
            else if (c[i] == '>')
                buf.append("&gt;");
            else if (c[i] == '"')
                buf.append("&quot;");
            else if (c[i] == '\'')
                buf.append("&#039;");
            else
                buf.append(c[i]);
        }
        return buf.toString();
    }
    
    public static String java2html3(String s) {
        if (s == null)
            return null;
        StringBuffer buf = new StringBuffer();
        char[] c = s.toCharArray();
        int len = c.length;
        for (int i = 0; i < len; i++) {
			if (c[i] == '\\')
                buf.append("\\\\");
            else if (c[i] == '"')
                buf.append("\\\"");
            else if (c[i] == '\'')
                buf.append("\\\'");
            else if (String.valueOf(c[i]).hashCode() == 10 || String.valueOf(c[i]).hashCode() == 13){
                buf.append("");
            }
            else
                buf.append(c[i]);
        }
        return buf.toString();
    }
    
    public static String java2html4(String s) {
    	if (s == null)
    		return null;
    	StringBuffer buf = new StringBuffer();
    	char[] c = s.toCharArray();
    	int len = c.length;
    	for (int i = 0; i < len; i++) {
    		if (c[i] == '\\')
    			buf.append("\\\\");
    		else if (c[i] == '"')
    			buf.append("\\\"");
    		else if (c[i] == '\'')
    			buf.append("\\\'");
    		else if (String.valueOf(c[i]).hashCode() == 10){
    			buf.append("");
    		}else if (String.valueOf(c[i]).hashCode() == 13) 
    			buf.append("\\n");
    		else
    			buf.append(c[i]);
    	}
    	return buf.toString();
    }
    
    public static String singleQuotTo2(String s) {
        if (s == null)
            return null;
        StringBuffer buf = new StringBuffer();
        char[] c = s.toCharArray();
        int len = c.length;
        for (int i = 0; i < len; i++) {
			if (c[i] == '\'')
                buf.append("''");
            else
                buf.append(c[i]);
        }
        return buf.toString();
    }
    
    public static String encodingExceptSpecialChar(String s) {
		s = s.replaceAll("\\+", "%20");
    	s = s.replaceAll("%21", "!");
		s = s.replaceAll("%23", "#");
		s = s.replaceAll("%24", "$");
		s = s.replaceAll("%25", "%");
		s = s.replaceAll("%26", "&");
		s = s.replaceAll("%28", "(");
		s = s.replaceAll("%29", ")");
		s = s.replaceAll("%2B", "+");
		s = s.replaceAll("%3B", ";");
		s = s.replaceAll("%3D", "=");
		s = s.replaceAll("%40", "@");
		s = s.replaceAll("%5B", "[");
		s = s.replaceAll("%5D", "]");
		s = s.replaceAll("%5E", "^");
    	
    	return s;
    }
    
    /**
     * vo를 map형식으로 변환해서 반환 
     * @param vo
     * @return
     * @throws Exception
     */
    public static Map<String, Object> domainToMap(Object vo) throws Exception {
    	return domainToMapWithExcept(vo, true, null);
    }
    
    /**
     * vo를 map형식으로 변환해서 반환
     * @param vo
     * @param snake
     * @return
     * @throws Exception
     */
    public static Map<String, Object> domainToMap(Object vo, boolean snake) throws Exception {
    	return domainToMapWithExcept(vo, snake, null);
    }

    /**
     * 특정 변수를 제외해서 vo를 map형식으로 변환해서 반환. 
     * @param vo
     * @param snake
     * @param arrExceptList 제외할 property 명 리스트
     * @return
     * @throws Exception
     */
    public static Map<String, Object> domainToMapWithExcept(Object vo, boolean snake, String[] arrExceptList) throws Exception {
    	Map<String, Object> result = new HashMap<String, Object>();
    	BeanInfo info = Introspector.getBeanInfo(vo.getClass());
    	for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
    		Method reader = pd.getReadMethod();
    		if (reader != null) {
    			// 제외항목에 포함될 경우 제외
    			if (arrExceptList != null && arrExceptList.length > 0 && isContain(arrExceptList, pd.getName()))	continue;
    			// getClass로 인한 class값이 생성 되므로 해당 class값도 제외
    			if (String.valueOf(reader.invoke(vo)).endsWith(vo.getClass().getName()))							continue;
    			if (snake)	result.put(camelToSnake(pd.getName()), reader.invoke(vo));
    			else		result.put(pd.getName(), reader.invoke(vo));
    		}
    	}
    	result.put("@timestamp", Util.null2str(DateUtils.nowWithMillisResolution()));
    	
    	return result;
   }
    
    public static Boolean isContain(String[] arrList, String name) {
    	for (String arr : arrList) {
    		if (StringUtils.contains(arr, name))	return true;
    	}
    	
    	return false;
    }
    
    /**
     * Camel 형식(replaceString)의 문자를 Snake 형식(replace_string)의 문자로 변환
     * @param str
     * @return
     */
    public static String camelToSnake(String str) {
    	if (str != null)	return str.replaceAll("([A-Z]+)","\\_$1").toLowerCase(); 
    	else				return str;  
    }
    
    /**
     * Snake 형식(replace_string)의 문자를 Camel 형식(replaceString)의 문자로 변환
     * @param str
     * @return
     */
    public static String snakeToCamel(String str) {
    	return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, str);
    }
    
    /**
     * DB 필드명 등 String값으로 Domain Value Set
     * 		Mf mf = new Mf();
	 *		Converter.setDomainValue(mf, "MF_JOB_NO", "1234");
     * @param vo
     * @param fieldName
     * @param value
     * @throws Exception
     */
    public static void setDomainValue(Object vo, String fieldName, Object value) throws Exception {
    	setDomainValue(vo, true, fieldName, value);
    }
    @SuppressWarnings("rawtypes")
	public static void setDomainValue(Object vo, boolean camel, String fieldName, Object value) throws Exception {
    	if (camel)	fieldName = snakeToCamel(fieldName);
    	
		Class cls = vo.getClass();
		Field field = cls.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(vo, value);
    }
    
    /**
     * DB 필드명 등 String값으로 Domain Value Get
     * 		Mf mf = new Mf();
	 *		Converter.getDomainValue(mf, "MF_JOB_NO");
     * @param vo
     * @param fieldName
     * @return
     * @throws Exception
     */
    public static Object getDomainValue(Object vo, String fieldName) throws Exception {
    	return getDomainValue(vo, true, fieldName);
    }
    @SuppressWarnings("rawtypes")
	public static Object getDomainValue(Object vo, boolean camel, String fieldName) throws Exception {
    	if (camel)	fieldName = snakeToCamel(fieldName);
    	
		Class cls = vo.getClass();
		Field field = cls.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(vo);
    }
    
    /**
     * 특수문자 변환
     * @param str
     * @param rep
     * @return
     */
    public static String changeSpeialChar(String str, String rep) {
    	// 한글유니코드(\uAC00-\uD7A3), 숫자 0~9(0-9), 영어 소문자a~z(a-z), 대문자A~Z(A-Z), 공백(\s)을 제외한(^) 단어일 경우 변경
    	return str.replaceAll("[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]", rep);
    }
}