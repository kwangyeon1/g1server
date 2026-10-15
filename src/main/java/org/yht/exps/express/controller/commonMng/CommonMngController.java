package org.yht.exps.express.controller.commonMng;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.exps.express.service.commonMng.CommonMngService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "CommonMng")
@RequestMapping("/{entCode}/ecm/commonMng")
public class CommonMngController {
	
	@Resource(name = "commonMngService")
	CommonMngService commonMngService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "전자상거래 개인통관부호 검증 ", notes="전자상거래 개인통관부호 검증합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customsVerify", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> customsVerify(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> map, HttpServletRequest request) throws Exception {
		Map<String, Object> userMap = Util.getSessionUserInfo(request, jwtTokenProvider);
		if(!Util.isEmpty(userMap.get("USER_ID"))){
			map.put("logUserName", userMap.get("USER_NAME"));
			map.put("logUserNameEng", userMap.get("USER_NAME_ENG"));
		}
		return new ResponseEntity<Map<String, Object>>(commonMngService.customsVerify(map), HttpStatus.OK);
	}
	
	
}
