package org.yht.jwt;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.yht.exps.express.model.properties.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider {
	
	@Autowired
	JwtProperties jwtProperties;

    @Autowired
    private UserDetailsService userDetailsService;
    
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(jwtProperties.getSecretKey().getBytes());
    }

    public String createToken(String userLoginId, String userId, String userName, String userNameEng, List<String> roles, boolean refreshToken) {

        Date now = new Date();
        Date validity = null;
        if (!refreshToken)	validity = new Date(now.getTime() + jwtProperties.getValidityInMs());
        else				validity = new Date(now.getTime() + jwtProperties.getValidityInMsRefresh());
        
    	Map<String, Object> headers = new HashMap<String, Object>();
    	headers.put("typ", "JWT");
    	headers.put("alg", "HS256");
    	
        Claims claims = Jwts.claims().setSubject(userLoginId);
        claims.put("roles", roles);
        claims.put("userId", userId);
        claims.put("userName", userName);
        claims.put("userNameEng", userNameEng);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    public String refreshToken(String userLoginId, String userId, String userName, String userNameEng, List<String> roles) {
    	Map<String, Object> headers = new HashMap<String, Object>();
    	headers.put("typ", "JWT");
    	headers.put("alg", "HS256");
    	
        Claims claims = Jwts.claims().setSubject(userLoginId);
        claims.put("roles", roles);
        claims.put("userId", userId);
        claims.put("userName", userName);
        claims.put("userNameEng", userNameEng);
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getValidityInMs());

        return Jwts.builder()
            .setClaims(claims)
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }
    public Map<String, Object> getUserInfo(String token){
    	Map<String, Object> map = new HashMap<String, Object>();
    	Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    	map.put("USER_ID", claims.get("userId"));
    	map.put("USER_NAME", claims.get("userName"));
    	map.put("USER_LOGIN_ID", claims.getSubject());
    	map.put("USER_NAME_ENG", claims.get("userNameEng"));
        return map;
    }

    public String resolveToken(HttpServletRequest req) {
    	/** 
    	 * 토큰정보 브라우서에서 호출시 헤더에서 쿠키로 변경
    	 * 배치,인터페이스등에서는 헤더사용 
    	 * local환경에서는 JSESSIONID 겹치기 때문에 헤더사용
    	 * /exps/common/refreshToken.do호출시에 브라우저에서 호출하지 않기때문에 헤더사용
    	 */
    	Cookie[] cookies = req.getCookies();
    	if(cookies != null){
    		for(Cookie cookie : cookies){
    			if(cookie.getName().equals("token")){
    				return cookie.getValue();
    			}
    		}
    	}
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
    
    public String createToken4(String username, List<String> roles, boolean refreshToken) {

        Date now = new Date();
        Date validity = null;
        if (!refreshToken)	validity = new Date(now.getTime() + jwtProperties.getValidityInMs());
        else				validity = new Date(now.getTime() + jwtProperties.getValidityInMsRefresh());
        
    	Map<String, Object> headers = new HashMap<String, Object>();
    	headers.put("typ", "JWT");
    	headers.put("alg", "HS256");
    	
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    /** 세션ID검증 추가(BMS전용)  */
    public String createToken3(String userLoginId, String userId, String userName, String userNameEng, String sessionId, List<String> roles, boolean refreshToken) {

        Date now = new Date();
        Date validity = null;
        if (!refreshToken)	validity = new Date(now.getTime() + jwtProperties.getValidityInMs());
        else				validity = new Date(now.getTime() + jwtProperties.getValidityInMsRefresh());
        
    	Map<String, Object> headers = new HashMap<String, Object>();
    	headers.put("typ", "JWT");
    	headers.put("alg", "HS256");
    	
        Claims claims = Jwts.claims().setSubject(userLoginId);
        claims.put("roles", roles);
        claims.put("userId", userId);
        claims.put("userName", userName);
        claims.put("userNameEng", userNameEng);
        claims.put("sessionId", sessionId);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    /** 세션ID검증 추가(BMS전용) */
    public String refreshToken3(String userLoginId, String userId, String userName, String userNameEng, String sessionId, List<String> roles) {
    	Map<String, Object> headers = new HashMap<String, Object>();
    	headers.put("typ", "JWT");
    	headers.put("alg", "HS256");
    	
        Claims claims = Jwts.claims().setSubject(userLoginId);
        claims.put("roles", roles);
        claims.put("userId", userId);
        claims.put("userName", userName);
        claims.put("userNameEng", userNameEng);
        claims.put("sessionId", sessionId);
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getValidityInMs());

        return Jwts.builder()
            .setClaims(claims)
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    /** 세션ID검증 추가(BMS전용) */
    public Map<String, Object> getUserInfo3(String token){
    	Map<String, Object> map = new HashMap<String, Object>();
    	Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    	map.put("USER_ID", claims.get("userId"));
    	map.put("USER_NAME", claims.get("userName"));
    	map.put("USER_LOGIN_ID", claims.getSubject());
    	map.put("USER_NAME_ENG", claims.get("userNameEng"));
    	map.put("SESSION_ID", claims.get("sessionId"));
        return map;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

            if (claims.getBody().getExpiration().before(new Date())) {
                return false;
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
        	return false;
        }
    }

}