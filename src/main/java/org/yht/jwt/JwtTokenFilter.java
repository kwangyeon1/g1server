package org.yht.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.yht.util.Util;

public class JwtTokenFilter extends GenericFilterBean {

    private JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
        throws IOException, ServletException {
    	
    	HttpServletRequest request = (HttpServletRequest) req;

    	String[] servletPath = request.getServletPath().split("\\/");
    	
    	/** HMS API수신위해 /if/hms/**, /if/order/registUTAIOrderInfo, /if/order/registUTAIOrderInfo에서 토큰검증하도록 수정 */
    	if(servletPath.length > 3 && servletPath[1].equals("if") && (servletPath[2].equals("hms") || servletPath[3].contains("registUTA"))){
    		/** 접속할 DB 선택하지 않을경우 오류발생해서 YHTXX로 설정함 */
    		HttpSession session = request.getSession(true);
    		session.setAttribute("db_key", "YHTXX");
            String token = jwtTokenProvider.resolveToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
    	}else if (servletPath.length > 1 && Util.checkEnterprise(servletPath[1])) {
    		HttpSession session = request.getSession(true);
    		session.setAttribute("db_key", servletPath[1]);
    		
            String token = jwtTokenProvider.resolveToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);

                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
   		
    	}
    	
        filterChain.doFilter(req, res);
    }

}