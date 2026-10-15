package org.yht.exps.express.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtil {

	/**
	 * Request 에서 Body Data를 꺼냄
	 * @param request
	 * @return
	 */
	public static String getBodyDataByRequest( HttpServletRequest request) {
		
		String charEncoding = request.getCharacterEncoding(); // 인코딩 설정
		Charset  encoding = StringUtils.isBlank(charEncoding) ? StandardCharsets.UTF_8 : Charset.forName(charEncoding);
		
		String collect = null;
		try {
			
			InputStream is = request.getInputStream();
			byte[] rawData = IOUtils.toByteArray(is); // InputStream 을 별도로 저장한 다음 getReader() 에서 새 스트림으로 생성

			// body 파싱
			collect = new BufferedReader(new InputStreamReader( getInputStream( rawData), encoding)).lines().collect(Collectors.joining(System.lineSeparator()));
			if (StringUtils.isEmpty(collect)) { // body 가 없을경우 로깅 제외
				
				return null;
			}
			if (request.getContentType() != null && request.getContentType().contains(
					
				ContentType.MULTIPART_FORM_DATA.getMimeType())) { // 파일 업로드시 로깅제외
				return null;
			}

		} catch( Exception e) {
			
			log.error(Util.getPrintStacTraceString(e));
			
		}
		return collect;
	}
	
	private static ServletInputStream getInputStream( byte[] rawData) throws IOException {
	
		final ByteArrayInputStream bais = new ByteArrayInputStream( rawData);
		ServletInputStream sis = new ServletInputStream() {
			
			@Override
			public int read() throws IOException {
				return bais.read();
			}
			
			@Override
			public void setReadListener(ReadListener readListener) {
				//setReadListener
			}
			
			@Override
			public boolean isReady() {
				return false;
			}
			
			@Override
			public boolean isFinished() {
				return false;
			}
		};
		
		return sis;
	}
	
	/**
	 * 이전 url에서 http www 를 뺀 주소 리턴
	 * @param request
	 * @return String
	 * @throws Exception
	 */
	public static String extractUrl(HttpServletRequest request){
	    try {
	    	
	    	String referer = request.getHeader("referer");
	    	
	    	if(referer != null && referer.length() > 0) {
	    		referer = referer.replaceAll("(?i:https?://([^/]+)/.*)", "$1");
	    	
	    		if(referer.startsWith("www")){
	    			referer = referer.substring("www".length()+1);
	    		}
	    	
	    	} else {
	    		referer = ""; 
	    	}
	    	
	    	/***
	        String REGEX = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	        Pattern p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
	        Matcher m = p.matcher(content);
	        if (m.find()) {
	            return m.group();
	        }
	        ***/
	        return referer;
	    } catch (Exception e) {
	        return "";
	    }
	}
}
