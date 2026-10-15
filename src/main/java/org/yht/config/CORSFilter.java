package org.yht.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet Filter implementation class CORSFilter
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSFilter implements Filter {

	/**
     * Default constructor.
     */
    public CORSFilter() {
    	//CORSFilter
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        String clientOrigin = request.getHeader("origin");
        
        // Authorize (allow) all domains to consume the content
        response.setHeader("Access-Control-Allow-Origin", clientOrigin);
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with, authorization, x-auth-token, origin, content-type, accept");

        response.setHeader("X-XSS-Protection", "1;mode=block");
        response.setHeader("Content-Security-Policy", "script-src * 'self' 'unsafe-inline' 'unsafe-eval'");
        response.setHeader("X-Frame-Options", "SAMEORIGN");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload;");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-control", "no-store");
        response.setHeader("Pragma", "no-cache");

        // For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS handshake
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
        	// pass the request along the filter chain
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
    	//init
    }

    /**
     * @see Filter#destroy()
     */
    @Override
    public void destroy() {
    	//destory
    }
}
