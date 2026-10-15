package org.yht.exps.express.controller.pda;

import java.util.Arrays;
import java.util.List;
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
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.model.mobile.bms.MobBwlo;
import org.yht.exps.express.model.mobile.bms.MobBwloSaveResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwlo;
import org.yht.exps.express.service.pda.BmsMobBwloService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/bms/mob/bw/lo")
public class BmsMobBwloController {
	
	@Resource(name="bmsMobBwloService")
	BmsMobBwloService bmsMobBwloService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "로케이션재고확인 - 재고수량 조회", notes="로케이션재고확인 - 재고수량 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/location/stock", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> selectLocationStock(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<List<Map<String, Object>>>(bmsMobBwloService.selectLocationStock(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 로케이션정보 조회", notes="로케이션적치 - 로케이션정보 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/location/info", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> selectLocationInfoDetail(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<List<Map<String, Object>>>(bmsMobBwloService.selectLocationInfoDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 반입 조회", notes="로케이션적치 - 반입 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/location/carryin", method = RequestMethod.GET)
	public ResponseEntity<MobBwlo> selectLocationCarryInDetail(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMobBwlo search, HttpServletRequest req) throws Exception {
		search.setEntCode(entCode);
		return new ResponseEntity<MobBwlo>(bmsMobBwloService.selectLocationCarryInDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 장치위치 업데이트", notes="로케이션적치 - 장치위치 업데이트합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/location/carryin", method = RequestMethod.PUT)
	public ResponseEntity<MobBwloSaveResult> updateLocationCarryIn(@PathVariable("entCode") String entCode, @RequestBody SearchMobBwlo search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.setEntCode(entCode);
		return new ResponseEntity<MobBwloSaveResult>(bmsMobBwloService.updateLocationCarryIn(search, req), HttpStatus.OK);
	}

}
