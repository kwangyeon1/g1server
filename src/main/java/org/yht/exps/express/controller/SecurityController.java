package org.yht.exps.express.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.AuthenticationToken;
import org.yht.exps.express.model.RSAKeys;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.EcmUser;
import org.yht.exps.express.model.sms.user.User;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.RSA;
import org.yht.util.Util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "Security")
@RequestMapping("/{entCode}/security")
public class SecurityController {
	
	@Resource(name = "commonService")
	CommonService commonService;
	
	@Resource(name = "smsUserService")
	SmsUserService smsUserService;
	
	@Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;
    
    @Autowired
	private CustomProperties customProperties;
    
	@Value("${jwt.secret-key}")
	private String secretKey_;
	@Value("${custom.edi-send-yn}")
	private String ediSendYn;
	
	protected static final Log logger = LogFactory.getLog(SecurityController.class);
	
	/**
	 * 토큰 발행
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "토큰 발행", notes="인증(ID 존재 확인) 후 인증 토큰값을 반환합니다.")
	@ApiImplicitParams({
        @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
        @ApiImplicitParam(name = "entBusinessCode", value = "사업장코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX-KR"),
        @ApiImplicitParam(name = "username", value = "로그인 ID", required = true, dataType = "string", paramType = "path", defaultValue = "username")
	})	
	@RequestMapping(value = "/get/token/{entBusinessCode}/{username}/{passwd}/{azureYn}", method = RequestMethod.GET)
	public ResponseEntity<AuthenticationToken> login(
						@PathVariable("entCode") String entCode,
						@PathVariable("entBusinessCode") String entBusinessCode,
						@PathVariable("username") String username,
						@PathVariable("passwd") String passwd,
						@PathVariable("azureYn") String azureYn,
						HttpServletRequest request) throws Exception {
		// 로그인 기록은 개발환경에서 불필요한 로그가 너무 많이 쌓이므로 라이브 환경에서만 적용
		final List<String> roles = new ArrayList<>();
		
		User user;
		if(Util.null2str(username).equals("guest")){
			user = new User();
			user.setUserId("guest");
		}else{
			user = smsUserService.getUserByLoginId(username);
		}
		
		//azure로 로그인한 경우는 통과
		//호출한 url 과 확인용 url이 같지 않으면 비정상 호출로 간주하여 오류를 발생
		if(!"Y".equals(azureYn)) {
			if(!passwd.equals(user.getUserLoginPw()) && !Util.null2str(username).equals("guest")){
				
				if ("Y".equals(ediSendYn)) {
					
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(entCode);
					if (!"login".equals(entBusinessCode)) {
						processLogs.setEntBusinessCode(entBusinessCode);
					} else {
						processLogs.setEntBusinessCode(user.getEntBusinessCode());
					}
					processLogs.setSolution("COM");
					processLogs.setBound("COM");
					processLogs.setBoundType("COM");
					processLogs.setProcessUserId(user.getUserId());
					processLogs.setLogJobType("ACCESS");
					processLogs.setJobFuncType("LOGIN");
					processLogs.setDescription("Express System(Login Error)");
					commonService.insertLog(processLogs, request);
				}
				
				return new ResponseEntity<AuthenticationToken>(new AuthenticationToken(username, null, null, roles), HttpStatus.BAD_REQUEST);
			}
		}
		
		final UserDetails details = smsUserService.loadUserByUsername(username);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority authority : details.getAuthorities()) {
			roles.add(authority.toString());
			authorities.add(new SimpleGrantedAuthority(authority.toString()));
		}

//		에러 파일 생성 여부 웹단에서 체크
//		String res = "";
//		if(checkServerErrorFile(entCode).equals("Y")) {	//오류 파일이 존재하는 경우 화면단에서 로그인 막음
//			res = "server db fail";
//		}
		String token = jwtTokenProvider.createToken(details.getUsername(), "", "", "", roles, false);
		String refreshToken = jwtTokenProvider.createToken(details.getUsername(), "", "", "", roles, true);
		
		return new ResponseEntity<AuthenticationToken>(new AuthenticationToken(details.getUsername(), token, refreshToken, roles), HttpStatus.OK);
	}
	/** 2025-08-13 사용자ID/사용자명 토큰정보에 추가    추후 기존 토큰생성로직 제거   (기존의 username은 사용자로그인ID임)    */
	@RequestMapping(value = "/get/token/{entBusinessCode}/{userLoginId}/{userId}/{userName}/{userNameEng}/{passwd}/{azureYn}", method = RequestMethod.GET)
	public ResponseEntity<AuthenticationToken> login2(
						@PathVariable("entCode") String entCode,
						@PathVariable("entBusinessCode") String entBusinessCode,
						@PathVariable("userLoginId") String userLoginId,
						@PathVariable("userId") String userId,
						@PathVariable("userName") String userName,
						@PathVariable("userNameEng") String userNameEng,
						@PathVariable("passwd") String passwd,
						@PathVariable("azureYn") String azureYn,
						HttpServletRequest request) throws Exception {
		// 로그인 기록은 개발환경에서 불필요한 로그가 너무 많이 쌓이므로 라이브 환경에서만 적용
		final List<String> roles = new ArrayList<>();
		
		User user;
		if(Util.null2str(userLoginId).equals("guest")){
			user = new User();
			user.setUserId("guest");
		}else{
			user = smsUserService.getUserByLoginId(userLoginId);
		}
		
		//azure로 로그인한 경우는 통과
		if(!"Y".equals(azureYn)) {
		//호출한 url 과 확인용 url이 같지 않으면 비정상 호출로 간주하여 오류를 발생
		if(!passwd.equals(user.getUserLoginPw()) && !Util.null2str(userLoginId).equals("guest")){
			
			if ("Y".equals(ediSendYn)) {
				
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(entCode);
				if (!"login".equals(entBusinessCode)) {
					processLogs.setEntBusinessCode(entBusinessCode);
				} else {
					processLogs.setEntBusinessCode(user.getEntBusinessCode());
				}
				processLogs.setSolution("COM");
				processLogs.setBound("COM");
				processLogs.setBoundType("COM");
				processLogs.setProcessUserId(user.getUserId());
				processLogs.setLogJobType("ACCESS");
				processLogs.setJobFuncType("LOGIN");
				processLogs.setDescription("Express System(Login Error)");
				commonService.insertLog(processLogs, request);
			}
			
			return new ResponseEntity<AuthenticationToken>(new AuthenticationToken(userLoginId, null, null, roles), HttpStatus.BAD_REQUEST);
		}
		
		}
		
		final UserDetails details = smsUserService.loadUserByUsername(userLoginId);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority authority : details.getAuthorities()) {
			roles.add(authority.toString());
			authorities.add(new SimpleGrantedAuthority(authority.toString()));
		}

//		에러 파일 생성 여부 웹단에서 체크
//		String res = "";
//		if(checkServerErrorFile(entCode).equals("Y")) {	//오류 파일이 존재하는 경우 화면단에서 로그인 막음
//			res = "server db fail";
//		}
        
		String token = jwtTokenProvider.createToken(details.getUsername(), userId, userName, userNameEng, roles, false);
		String refreshToken = jwtTokenProvider.createToken(details.getUsername(), userId, userName, userNameEng, roles, true);
		
		return new ResponseEntity<AuthenticationToken>(new AuthenticationToken(details.getUsername(), token, refreshToken, roles), HttpStatus.OK);
	}
	@RequestMapping(value = "/get/token/{entBusinessCode}/{userLoginId}/{userId}/{passwd}/{azureYn}", method = RequestMethod.GET)
	public ResponseEntity<AuthenticationToken> login4(
						@PathVariable("entCode") String entCode,
						@PathVariable("entBusinessCode") String entBusinessCode,
						@PathVariable("userLoginId") String userLoginId,
						@PathVariable("userId") String userId,
						@PathVariable("passwd") String passwd,
						@PathVariable("azureYn") String azureYn,
						@QueryStringArgResolver EcmUser ecmUser,
						HttpServletRequest request) throws Exception {
		// 로그인 기록은 개발환경에서 불필요한 로그가 너무 많이 쌓이므로 라이브 환경에서만 적용
		final List<String> roles = new ArrayList<>();
		
		User user;
		if(Util.null2str(userLoginId).equals("guest")){
			user = new User();
			user.setUserId("guest");
		}else{
			user = smsUserService.getUserByLoginId(userLoginId);
		}
		
		//호출한 url 과 확인용 url이 같지 않으면 비정상 호출로 간주하여 오류를 발생
		if(!"Y".equals(azureYn)) {
			if(!passwd.equals(user.getUserLoginPw()) && !Util.null2str(userLoginId).equals("guest")){
				
				if ("Y".equals(ediSendYn)) {
					
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(entCode);
					if (!"login".equals(entBusinessCode)) {
						processLogs.setEntBusinessCode(entBusinessCode);
					} else {
						processLogs.setEntBusinessCode(user.getEntBusinessCode());
					}
					processLogs.setSolution("COM");
					processLogs.setBound("COM");
					processLogs.setBoundType("COM");
					processLogs.setProcessUserId(user.getUserId());
					processLogs.setLogJobType("ACCESS");
					processLogs.setJobFuncType("LOGIN");
					processLogs.setDescription("Express System(Login Error)");
					commonService.insertLog(processLogs, request);
				}
				
				return new ResponseEntity<AuthenticationToken>(new AuthenticationToken(userLoginId, null, null, roles), HttpStatus.BAD_REQUEST);
			}
		}
		
		final UserDetails details = smsUserService.loadUserByUsername(userLoginId);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority authority : details.getAuthorities()) {
			roles.add(authority.toString());
			authorities.add(new SimpleGrantedAuthority(authority.toString()));
		}

//		에러 파일 생성 여부 웹단에서 체크
//		String res = "";
//		if(checkServerErrorFile(entCode).equals("Y")) {	//오류 파일이 존재하는 경우 화면단에서 로그인 막음
//			res = "server db fail";
//		}
		
		String token = jwtTokenProvider.createToken(details.getUsername(), userId, ecmUser.getUserName(), ecmUser.getUserNameEng(), roles, false);
		String refreshToken = jwtTokenProvider.createToken(details.getUsername(), userId, ecmUser.getUserName(), ecmUser.getUserNameEng(), roles, true);
		
		return new ResponseEntity<AuthenticationToken>(new AuthenticationToken(details.getUsername(), token, refreshToken, roles), HttpStatus.OK);
	}
	/** 세션ID검증 추가(BMS전용) */
	@RequestMapping(value = "/get/token/bms/{entBusinessCode}/{userLoginId}/{userId}/{passwd}/{azureYn}", method = RequestMethod.GET)
	public ResponseEntity<AuthenticationToken> login3(
						@PathVariable("entCode") String entCode,
						@PathVariable("entBusinessCode") String entBusinessCode,
						@PathVariable("userLoginId") String userLoginId,
						@PathVariable("userId") String userId,
						@PathVariable("passwd") String passwd,
						@PathVariable("azureYn") String azureYn,
						@QueryStringArgResolver EcmUser ecmUser,
						HttpServletRequest request) throws Exception {
		// 로그인 기록은 개발환경에서 불필요한 로그가 너무 많이 쌓이므로 라이브 환경에서만 적용
		final List<String> roles = new ArrayList<>();
		
		User user;
		if(Util.null2str(userLoginId).equals("guest")){
			user = new User();
			user.setUserId("guest");
		}else{
			user = smsUserService.getUserByLoginId(userLoginId);
		}
		
		//azure로 로그인한 경우는 통과
		//호출한 url 과 확인용 url이 같지 않으면 비정상 호출로 간주하여 오류를 발생
		if(!"Y".equals(azureYn)) {
			if(!passwd.equals(user.getUserLoginPw()) && !Util.null2str(userLoginId).equals("guest")){
				
				if ("Y".equals(ediSendYn)) {
					
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(entCode);
					if (!"login".equals(entBusinessCode)) {
						processLogs.setEntBusinessCode(entBusinessCode);
					} else {
						processLogs.setEntBusinessCode(user.getEntBusinessCode());
					}
					processLogs.setSolution("COM");
					processLogs.setBound("COM");
					processLogs.setBoundType("COM");
					processLogs.setProcessUserId(user.getUserId());
					processLogs.setLogJobType("ACCESS");
					processLogs.setJobFuncType("LOGIN");
					processLogs.setDescription("Express System(Login Error)");
					commonService.insertLog(processLogs, request);
				}
				
				return new ResponseEntity<AuthenticationToken>(new AuthenticationToken(userLoginId, null, null, roles), HttpStatus.BAD_REQUEST);
			}
		}
		
		final UserDetails details = smsUserService.loadUserByUsername(userLoginId);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority authority : details.getAuthorities()) {
			roles.add(authority.toString());
			authorities.add(new SimpleGrantedAuthority(authority.toString()));
		}

//		에러 파일 생성 여부 웹단에서 체크
//		String res = "";
//		if(checkServerErrorFile(entCode).equals("Y")) {	//오류 파일이 존재하는 경우 화면단에서 로그인 막음
//			res = "server db fail";
//		}
        /** 웹로그인시 토큰생성할때 세션아이디를 토큰정보에 추가하고 토큰갱신시 최초 토큰생성할때와 같은 세션에서 요청했는지 검증추가함 */
		String token = jwtTokenProvider.createToken3(details.getUsername(), userId, ecmUser.getUserName(), ecmUser.getUserNameEng(), ecmUser.getUserSessionId(), roles, false);
		String refreshToken = jwtTokenProvider.createToken3(details.getUsername(), userId, ecmUser.getUserName(), ecmUser.getUserNameEng(), ecmUser.getUserSessionId(), roles, true);
		
		return new ResponseEntity<AuthenticationToken>(new AuthenticationToken(details.getUsername(), token, refreshToken, roles), HttpStatus.OK);
	}
	
	/**
	 * Token 갱신
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "토큰값 갱신", notes="토큰값을 갱신하여 반환합니다.")
	@ApiImplicitParams({
        @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
        @ApiImplicitParam(name = "entBusinessCode", value = "사업장코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX-KR"),
        @ApiImplicitParam(name = "username", value = "로그인 ID", required = true, dataType = "string", paramType = "path", defaultValue = "username")
	})	
	@RequestMapping(value = "/refresh/token/{entBusinessCode}/{username}", method = RequestMethod.GET)
	public ResponseEntity<AuthenticationToken> refresh(
						@PathVariable("entCode") String entCode,
						@PathVariable("entBusinessCode") String entBusinessCode,
						@PathVariable("username") String username,
						HttpServletRequest request) throws Exception {
		
		AuthenticationToken authenticationToken = null;
		
		final UserDetails details = smsUserService.loadUserByUsername(username);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		final List<String> roles = new ArrayList<>();
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority authority : details.getAuthorities()) {
			roles.add(authority.toString());
			authorities.add(new SimpleGrantedAuthority(authority.toString()));
		}
		
		String refreshToken = jwtTokenProvider.resolveToken(request);
		String result = validateToken(refreshToken);
		String token = null;
		if (!"fail".equals(result)) {
			// 기간 만료일 경우 갱신용 Token을 재발급 한다.
			if ("expired".equals(result))	refreshToken = jwtTokenProvider.createToken(details.getUsername(), "", "", "", roles, true);
			token = jwtTokenProvider.refreshToken(details.getUsername(), "", "", "", roles);
			result = "success";
		}
		
		authenticationToken = new AuthenticationToken(details.getUsername(), token, refreshToken, roles);
		authenticationToken.setResult(result);
		
		// 로그인 기록은 개발환경에서 불필요한 로그가 너무 많이 쌓이므로 라이브 환경에서만 적용
		if("Y".equals(ediSendYn)){
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(entCode);
			if (!"login".equals(entBusinessCode)) {
				processLogs.setEntBusinessCode(entBusinessCode);
			} else {
				User user = smsUserService.getUserByLoginId(username);
				if (user != null) {
					processLogs.setEntBusinessCode(user.getEntBusinessCode());
				}
			}
			processLogs.setSolution("COM");
			processLogs.setBound("COM");
			processLogs.setBoundType("COM");
			processLogs.setProcessUserId(username);
			processLogs.setLogJobType("ACCESS");
			processLogs.setJobFuncType("LOGIN");
			processLogs.setDescription("Express System(Token Refresh)");
			commonService.insertLog(processLogs, request);
		}
		
		return new ResponseEntity<AuthenticationToken>(authenticationToken, HttpStatus.OK);
	}
	/** 2025-08-13 사용자ID/사용자명 토큰정보에 추가    추후 기존 토큰생성로직 제거   (기존의 username은 사용자로그인ID임)    */
	@RequestMapping(value = "/refresh/token/{entBusinessCode}/{userLoginId}/{userId}/{userName}/{userNameEng}", method = RequestMethod.GET)
	public ResponseEntity<AuthenticationToken> refresh2(
						@PathVariable("entCode") String entCode,
						@PathVariable("entBusinessCode") String entBusinessCode,
						@PathVariable("userLoginId") String userLoginId,
						@PathVariable("userId") String userId,
						@PathVariable("userName") String userName,
						@PathVariable("userNameEng") String userNameEng,
						HttpServletRequest request) throws Exception {
		
		AuthenticationToken authenticationToken = null;
		
		final UserDetails details = smsUserService.loadUserByUsername(userLoginId);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		final List<String> roles = new ArrayList<>();
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority authority : details.getAuthorities()) {
			roles.add(authority.toString());
			authorities.add(new SimpleGrantedAuthority(authority.toString()));
		}
		
		String refreshToken = jwtTokenProvider.resolveToken(request);
		String result = validateToken(refreshToken);
		String token = null;
		if (!"fail".equals(result)) {
			// 기간 만료일 경우 갱신용 Token을 재발급 한다.
			if ("expired".equals(result))	refreshToken = jwtTokenProvider.createToken(details.getUsername(), userId, userName, userNameEng, roles, true);
			token = jwtTokenProvider.refreshToken(details.getUsername(), userId, userName, userNameEng, roles);
			result = "success";
		}
		
		authenticationToken = new AuthenticationToken(details.getUsername(), token, refreshToken, roles);
		authenticationToken.setResult(result);
		
		// 로그인 기록은 개발환경에서 불필요한 로그가 너무 많이 쌓이므로 라이브 환경에서만 적용
		if("Y".equals(ediSendYn)){
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(entCode);
			if (!"login".equals(entBusinessCode)) {
				processLogs.setEntBusinessCode(entBusinessCode);
			} else {
				User user = smsUserService.getUserByLoginId(userLoginId);
				if (user != null) {
					processLogs.setEntBusinessCode(user.getEntBusinessCode());
				}
			}
			processLogs.setSolution("COM");
			processLogs.setBound("COM");
			processLogs.setBoundType("COM");
			processLogs.setProcessUserId(userLoginId);
			processLogs.setLogJobType("ACCESS");
			processLogs.setJobFuncType("LOGIN");
			processLogs.setDescription("Express System(Token Refresh)");
			commonService.insertLog(processLogs, request);
		}
		
		return new ResponseEntity<AuthenticationToken>(authenticationToken, HttpStatus.OK);
	}
	@RequestMapping(value = "/refresh/token/{entBusinessCode}/{userLoginId}/{userId}", method = RequestMethod.GET)
	public ResponseEntity<AuthenticationToken> refresh4(
						@PathVariable("entCode") String entCode,
						@PathVariable("entBusinessCode") String entBusinessCode,
						@PathVariable("userLoginId") String userLoginId,
						@PathVariable("userId") String userId,
						@QueryStringArgResolver EcmUser ecmUser,
						HttpServletRequest request) throws Exception {
		AuthenticationToken authenticationToken = null;
		
		final UserDetails details = smsUserService.loadUserByUsername(userLoginId);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		final List<String> roles = new ArrayList<>();
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority authority : details.getAuthorities()) {
			roles.add(authority.toString());
			authorities.add(new SimpleGrantedAuthority(authority.toString()));
		}
		
		String refreshToken = jwtTokenProvider.resolveToken(request);
		String result = validateToken(refreshToken);
		String token = null;
		if (!"fail".equals(result)) {
			// 기간 만료일 경우 갱신용 Token을 재발급 한다.
			if ("expired".equals(result))	refreshToken = jwtTokenProvider.createToken(details.getUsername(), userId, ecmUser.getUserName(), ecmUser.getUserNameEng(), roles, true);
			token = jwtTokenProvider.refreshToken(details.getUsername(), userId, ecmUser.getUserName(), ecmUser.getUserNameEng(), roles);
			result = "success";
		}
		
		authenticationToken = new AuthenticationToken(details.getUsername(), token, refreshToken, roles);
		authenticationToken.setResult(result);
		
		// 로그인 기록은 개발환경에서 불필요한 로그가 너무 많이 쌓이므로 라이브 환경에서만 적용
		if("Y".equals(ediSendYn)){
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(entCode);
			if (!"login".equals(entBusinessCode)) {
				processLogs.setEntBusinessCode(entBusinessCode);
			} else {
				User user = smsUserService.getUserByLoginId(userLoginId);
				if (user != null) {
					processLogs.setEntBusinessCode(user.getEntBusinessCode());
				}
			}
			processLogs.setSolution("COM");
			processLogs.setBound("COM");
			processLogs.setBoundType("COM");
			processLogs.setProcessUserId(userLoginId);
			processLogs.setLogJobType("ACCESS");
			processLogs.setJobFuncType("LOGIN");
			processLogs.setDescription("Express System(Token Refresh)");
			commonService.insertLog(processLogs, request);
		}
		
		return new ResponseEntity<AuthenticationToken>(authenticationToken, HttpStatus.OK);
	}
	
	/** 세션ID검증 추가(BMS전용) */
	@RequestMapping(value = "/refresh/token/bms/{entBusinessCode}/{userLoginId}/{userId}", method = RequestMethod.GET)
	public ResponseEntity<AuthenticationToken> refresh3(
						@PathVariable("entCode") String entCode,
						@PathVariable("entBusinessCode") String entBusinessCode,
						@PathVariable("userLoginId") String userLoginId,
						@PathVariable("userId") String userId,
						@QueryStringArgResolver EcmUser ecmUser,
						HttpServletRequest request) throws Exception {
		AuthenticationToken authenticationToken = null;
		
		final UserDetails details = smsUserService.loadUserByUsername(userLoginId);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		final List<String> roles = new ArrayList<>();
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority authority : details.getAuthorities()) {
			roles.add(authority.toString());
			authorities.add(new SimpleGrantedAuthority(authority.toString()));
		}
		String refreshToken = jwtTokenProvider.resolveToken(request);
		String result = validateToken(refreshToken);
		String token = null;
		if ("success".equals(result)) {
			Map<String, Object> map = jwtTokenProvider.getUserInfo3(refreshToken);
			if(Util.null2str(map.get("SESSION_ID")).equals(ecmUser.getUserSessionId())){
				/** BMS는 refreshToken 재생성하지않음 refreshToken유효시간 지날경우 재로그인해야함 */
				token = jwtTokenProvider.refreshToken3(details.getUsername(), userId, ecmUser.getUserName(), ecmUser.getUserNameEng(), ecmUser.getUserSessionId(), roles);
			}else{
				result = "sessionId fail";
			}
		}
		
		authenticationToken = new AuthenticationToken(details.getUsername(), token, refreshToken, roles);
		authenticationToken.setResult(result);
		
		// 로그인 기록은 개발환경에서 불필요한 로그가 너무 많이 쌓이므로 라이브 환경에서만 적용
		if("Y".equals(ediSendYn)){
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(entCode);
			if (!"login".equals(entBusinessCode)) {
				processLogs.setEntBusinessCode(entBusinessCode);
			} else {
				User user = smsUserService.getUserByLoginId(userLoginId);
				if (user != null) {
					processLogs.setEntBusinessCode(user.getEntBusinessCode());
				}
			}
			processLogs.setSolution("COM");
			processLogs.setBound("COM");
			processLogs.setBoundType("COM");
			processLogs.setProcessUserId(userLoginId);
			processLogs.setLogJobType("ACCESS");
			processLogs.setJobFuncType("LOGIN");
			processLogs.setDescription("Express System(Token Refresh)");
			commonService.insertLog(processLogs, request);
		}
		return new ResponseEntity<AuthenticationToken>(authenticationToken, HttpStatus.OK);
	}
	
	/**
	 * Check
	 * @param username
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "토큰값 체크", notes="토큰값을 체크하여 정산 사용자 여부를 반환합니다.")
	@RequestMapping(value = "/check/token", method = RequestMethod.GET)
	public ResponseEntity<AuthenticationToken> check(HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setResult("success");
		
		if ("anonymousUser".equals(authentication.getPrincipal())) {
			authenticationToken.setResult("fail");
		} else {
			String token = jwtTokenProvider.resolveToken(request);
			
			if (token != null) {
				if (!jwtTokenProvider.validateToken(token))
					authenticationToken.setResult("fail");	
			} else {
				authenticationToken.setResult("fail");
			}
		}
		
		return new ResponseEntity<AuthenticationToken>(authenticationToken, HttpStatus.OK);
	}
	
	/**
	 * RSA 공개키, 개인키 생성
	 * 
	 * @param request
	 * @throws Exception
	 */
	@ApiOperation(value = "RSA 공개키, 개인키 생성", notes="로컬 시스템에서 암복호화에 사용할 공개키, 개인키를 생성하여 반환합니다.")
	@RequestMapping(value = "/getPublicKeys", method = RequestMethod.GET)
	public ResponseEntity<RSAKeys> getPublicKeys(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		RSAKeys keys = new RSAKeys();
		
		RSA rsa = RSA.getEncKey();
		session.setAttribute("__rsaPrivateKey__", rsa.getPrivateKey());
		keys.setPublicKeyModulus(rsa.getPublicKeyModulus());
		keys.setPublicKeyExponent(rsa.getPublicKeyExponent());
		
		return new ResponseEntity<RSAKeys>(keys, HttpStatus.OK);
	}
	
	private String validateToken(String token) {
		String result = "";
		
        try {
            Jwts.parser().setSigningKey(Base64.getEncoder().encodeToString(secretKey_.getBytes())).parseClaimsJws(token);
            result = "success";
            
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
            result = "fail";
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
            result = "expired";
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
            result = "fail";
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
            result = "fail";
        }
        
        return result;
	}
	
	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public ResponseEntity<String> test(HttpServletRequest request) {
		try{
			String chk = Util.null2str(request.getParameter("chk"));
			if("qweradf".equals(chk)){
				return new ResponseEntity<String>(commonService.selectEntCode("YHTXX"), HttpStatus.OK);
			}else{
				logger.error("/security/test 잘못된 접근입니다.");
				return new ResponseEntity<String>("null", HttpStatus.OK);
			}
		}catch(Exception e){
			logger.error("/security/test " + e.getMessage());
			return new ResponseEntity<String>("null", HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/testCheck", method = RequestMethod.GET)
	public ResponseEntity<String> testCheck(HttpServletRequest request) {
			return new ResponseEntity<String>("ok", HttpStatus.OK);
	}
	
	/**
	 * 서버단에 오류 파일 생성 여부 확인
	 * 
	 * @param 
	 * @throws Exception
	 */
	@ApiOperation(value = "오류 파일 여부", notes="서버단에 오류 파일 생성 여부 확인를 체크하여 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	public String checkServerErrorFile(String entCode) throws Exception {
		
		String result = "";
		String filePath = "";
		String fileName = "";
		
		filePath = customProperties.getErrFileDir();
		fileName = entCode + ".txt";	//오류 파일 생성 시에 도입사명으로 생성함.
		
		File file = new File(filePath+fileName);
		
		if(file.exists()) {
			result = "Y";
		}
        
		return result;
	}
	
	
	
}