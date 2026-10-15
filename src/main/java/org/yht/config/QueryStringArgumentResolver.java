package org.yht.config;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class QueryStringArgumentResolver implements HandlerMethodArgumentResolver {
 
    @Autowired
    private ObjectMapper mapper;
 
 
    @Override
    public boolean supportsParameter(final MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(QueryStringArgResolver.class) != null;
    }
 
 
    @Override
    public Object resolveArgument(final MethodParameter methodParameter,
                                  final ModelAndViewContainer modelAndViewContainer,
                                  final NativeWebRequest nativeWebRequest,
                                  final WebDataBinderFactory webDataBinderFactory) {
 
        final HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        Object obj = null;
        String json = null;
        try {
	        json = qs2json(request.getQueryString());
	        json = json.replaceAll("\"\\[", "\\[").replaceAll("\\]\"", "\\]");
	        json = json.replaceAll("\"\\{", "\\{").replaceAll("\\}\"", "\\}");
	        json = json.replaceAll("	", "");
	        
	        obj = mapper.readValue(json, methodParameter.getParameterType());
	        
        } catch (Exception e) {
        	log.error(Util.getPrintStacTraceString(e));
        	try {
        		log.error("QueryStringArgumentResolver Error");
        	}catch(Exception ee){log.error("QueryStringArgumentResolver Error");}
        }
        
        return obj;
    }
 
 
    private String qs2json(String a) throws UnsupportedEncodingException {
    	/** 
    	 * 특정문자 변환처리함
    	 * %26 : &
    	 * %3D : =
    	 */
    	a = URLDecoder.decode(a.replace("%26", "　").replace("%3D", "！"), "UTF-8");
        String res = "{\"";
 
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == '=') {
                res += "\"" + ":" + "\"";
            } else if (a.charAt(i) == '&') {
                res += "\"" + "," + "\"";
            } else if(a.charAt(i) == '　') {
            	res += "&";
            } else if(a.charAt(i) == '！') {
            	res += "=";
            } else {
                res += a.charAt(i);
            }
        }
        res += "\"" + "}";
        return res;
    }
}