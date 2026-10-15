package org.yht.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
	private final QueryStringArgumentResolver argumentResolver;
	
	@Autowired
	@Qualifier(value = "httpInterceptor")
	private HandlerInterceptor interceptor;
	
	@Override
    public void addArgumentResolvers(
            final List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(argumentResolver);
    }
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		List<String> notLoadList = new ArrayList<String>();
		notLoadList.add("/swagger*/**");
        notLoadList.add("/webjars/**");
        notLoadList.add("/sample/**");
        notLoadList.add("/configuration/**");
        notLoadList.add("/v2/api-docs");
        notLoadList.add("/if/**");	//ifController 경로에 entCode 삭제돼서 Util.setDB() 자동으로 실행되지 않게 추가함
        notLoadList.add("/workstationinterconnection/**");	//XrayInterfaceController 경로에 entCode 삭제돼서 Util.setDB() 자동으로 실행되지 않게 추가함
		
		registry.addInterceptor(interceptor)
				.addPathPatterns("/**")
				.excludePathPatterns(notLoadList);
	}
}