package org.yht.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.jwt.JwtConfigurer;
import org.yht.jwt.JwtTokenProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableWebSecurity
@Configuration
@Order
class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
    JwtTokenProvider jwtTokenProvider;
	@Value("${spring.profiles.active}")
	private String profile;
	@Value("${custom.interface-server-yn}")
	private String interfaceServerYn;
	@Value("${custom.edi-send-yn}")
	private String ediSendYn;

	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//@formatter:off
		
		if("Y".equals(interfaceServerYn)){
	        http
            .httpBasic().disable()
            .csrf().disable()
            .headers()
            	.frameOptions().sameOrigin()
            	.and()
            .sessionManagement()
            	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            	.and()
           	.exceptionHandling()
        		.accessDeniedHandler(accessDeniedHandler())
        		.authenticationEntryPoint(authenticationEntryPoint())
        		.and()
            .authorizeRequests()
	            .antMatchers("/if/order/registUTAIOrderInfo").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/if/order/registUTAOOrderInfo").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/if/hms/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/if/**").permitAll()
	        	.antMatchers("/workstationinterconnection/v1/imagenotification/").permitAll()
            	.antMatchers("/**/pay/**").permitAll()
            	.antMatchers("/**/payAuthorization/**").permitAll()
            	.anyRequest().authenticated()
            	.and()
            .apply(new JwtConfigurer(jwtTokenProvider));
		}else if(!"Y".equals(ediSendYn)){
			http
            .httpBasic().disable()
            .csrf().disable()
            .headers()
            	.frameOptions().sameOrigin()
            	.and()
            .sessionManagement()
            	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            	.and()
           	.exceptionHandling()
        		.accessDeniedHandler(accessDeniedHandler())
        		.authenticationEntryPoint(authenticationEntryPoint())
        		.and()
            .authorizeRequests()
	    		.antMatchers("/websocket/**").permitAll()
	    		.antMatchers("/pub/**").permitAll()
	    		.antMatchers("/sub/**").permitAll()
	        	.antMatchers("/**/security/get/token/**").permitAll()
	        	.antMatchers("/**/security/refresh/token/**").permitAll()
	        	.antMatchers("/**/security/check/token").permitAll()
	        	.antMatchers("/**/security/getPublicKeys").permitAll()
	        	.antMatchers("/**/security/test").permitAll()
	        	.antMatchers("/**/ecm/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/sms/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/common/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/express/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/notification/**").hasRole(SmsUserService.ROLE_GUEST)
	        	.antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**", "/sample/**").permitAll()
	        	.antMatchers("/if/order/registUTAIOrderInfo").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/if/order/registUTAOOrderInfo").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/if/hms/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/if/**").permitAll()
	        	.antMatchers("/**/pay/**").permitAll()
            	.antMatchers("/**/payAuthorization/**").permitAll()
            	.antMatchers("/**/nosess/**").permitAll()
	        	.anyRequest().authenticated()
	        	.and()
            .apply(new JwtConfigurer(jwtTokenProvider));
		}else if("pantos1".equals(profile)) {
			http
            .httpBasic().disable()
            .csrf().disable()
            .headers()
            	.frameOptions().sameOrigin()
            	.and()
            .sessionManagement()
            	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            	.and()
           	.exceptionHandling()
        		.accessDeniedHandler(accessDeniedHandler())
        		.authenticationEntryPoint(authenticationEntryPoint())
        		.and()
            .authorizeRequests()
	    		.antMatchers("/websocket/**").permitAll()
	    		.antMatchers("/pub/**").permitAll()
	    		.antMatchers("/sub/**").permitAll()
	        	.antMatchers("/**/security/get/token/**").permitAll()
	        	.antMatchers("/**/security/refresh/token/**").permitAll()
	        	.antMatchers("/**/security/check/token").permitAll()
	        	.antMatchers("/**/security/getPublicKeys").permitAll()
	        	.antMatchers("/**/security/test").permitAll()
	        	.antMatchers("/**/security/testCheck").permitAll()
	        	.antMatchers("/**/ecm/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/sms/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/common/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/express/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/notification/**").hasRole(SmsUserService.ROLE_GUEST)
	        	.antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**", "/sample/**").permitAll()
	        	.antMatchers("/if/workstationinterconnection/v1/imagenotification/").permitAll()
	        	.antMatchers("/workstationinterconnection/v1/imagenotification/").permitAll()
	        	.antMatchers("/if/**").hasRole(SmsUserService.ROLE_USER)
	        	.anyRequest().authenticated()
	        	.and()
            .apply(new JwtConfigurer(jwtTokenProvider));
		}else{
	        http
            .httpBasic().disable()
            .csrf().disable()
            .headers()
            	.frameOptions().sameOrigin()
            	.and()
            .sessionManagement()
            	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            	.and()
           	.exceptionHandling()
        		.accessDeniedHandler(accessDeniedHandler())
        		.authenticationEntryPoint(authenticationEntryPoint())
        		.and()
            .authorizeRequests()
	    		.antMatchers("/websocket/**").permitAll()
	    		.antMatchers("/pub/**").permitAll()
	    		.antMatchers("/sub/**").permitAll()
	        	.antMatchers("/**/security/get/token/**").permitAll()
	        	.antMatchers("/**/security/refresh/token/**").permitAll()
	        	.antMatchers("/**/security/check/token").permitAll()
	        	.antMatchers("/**/security/getPublicKeys").permitAll()
	        	.antMatchers("/**/security/test").permitAll()
	        	.antMatchers("/**/security/testCheck").permitAll()
	        	.antMatchers("/**/ecm/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/sms/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/common/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/express/**").hasRole(SmsUserService.ROLE_USER)
	        	.antMatchers("/**/notification/**").hasRole(SmsUserService.ROLE_GUEST)
	        	.antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**", "/sample/**").permitAll()
	        	.antMatchers("/if/**").hasRole(SmsUserService.ROLE_USER)
	        	.anyRequest().authenticated()
	        	.and()
            .apply(new JwtConfigurer(jwtTokenProvider));
		}

        //@formatter:on
	}

	@Override
	protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
		authManagerBuilder.userDetailsService(new SmsUserService());
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Bean
	RestAccessDeniedHandler accessDeniedHandler() {
		return new RestAccessDeniedHandler();
	}

	@Bean
	RestAuthenticationEntryPoint authenticationEntryPoint() {
		return new RestAuthenticationEntryPoint();
	}
}