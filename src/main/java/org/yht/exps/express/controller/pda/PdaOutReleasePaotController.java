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
import org.yht.exps.express.model.bms.carryin.CarryInRst;
import org.yht.exps.express.service.pda.PdaOutReleasePaotService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/pda/pa/ot")
public class PdaOutReleasePaotController {
	
	@Resource(name="pdaOutReleasePaotService")
	PdaOutReleasePaotService pdaOutReleasePaotService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "반출출고", notes="박스스캔 출고 최종처리 및 출고처리(OUT_SCAN_DATE_TIME, OUT_CT) 업데이트 합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateFinishOutRelease", method = RequestMethod.POST)
	public ResponseEntity<CarryInRst> updateFinishOutRelease(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInRst>(pdaOutReleasePaotService.updateFinishOutRelease(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "출고스캔로그 조회", notes="출고스캔로그 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/scanLog", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> selectScanLogInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<List<Map<String, Object>>>(pdaOutReleasePaotService.selectScanLogInfo(search), HttpStatus.OK);
	}

	@ApiOperation(value = "반출출고 취소", notes="박스스캔 출고 최종처리 및 출고처리(OUT_SCAN_DATE_TIME, OUT_CT) 취소 합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateFinishOutCancle", method = RequestMethod.POST)
	public void updateFinishOutCancle(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		pdaOutReleasePaotService.updateFinishOutCancle(search, req);
	}
}
