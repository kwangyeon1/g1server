package org.yht.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.yht.util.Util;

@Component
public class HttpInterceptor implements HandlerInterceptor {
	
	@Override
	public boolean preHandle(HttpServletRequest request,
							 HttpServletResponse response,
							 Object handler) {

		// URL에 항상 오는 ENT_CODE값을 이용하여 DB Connection을 결정한다.
		String[] urls = request.getRequestURI().split("/");
		String entCode = "";
		if (urls.length > 0)	entCode = urls[1].toUpperCase();
		
		if ("".equals(entCode) || !Util.setDB(request, entCode)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return false;
			
		} else {
			return true;
		}
	}
	
	@Override
	public void postHandle( HttpServletRequest request,
							HttpServletResponse response,
							Object handler,
							ModelAndView modelAndView) {
		//postHandle
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request,
								HttpServletResponse response, 
								Object handler, 
								Exception ex) {
		//afterCompletion
	}
}
