package org.yht.exps.express.controller.pda;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.service.pda.BmsMobPasmService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/bms/mob/pa/sm")
public class BmsMobPasmController {

	@Resource(name="bmsMobPasmService")
	BmsMobPasmService bmsMobPasmService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@ApiOperation(value = "견품반출 박스 번호 조회", notes="견품반출 조회합니다.")
	@RequestMapping(value="/selectTakeOutSample", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> selectTakeOutSample(@RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<Map<String, Object>>(bmsMobPasmService.selectTakeOutSample(search), HttpStatus.OK);
	}

	@ApiOperation(value = "견품반출 서명 저장", notes="견품반출 서명을 저장합니다.")
	@RequestMapping(value="/updateTakeOutSample", method = RequestMethod.PUT)
	public void updateTakeOutSample(@RequestBody Map<String, Object> map, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, map, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		bmsMobPasmService.updateTakeOutSample(map, req);
	}

	@ApiOperation(value = "견품 반출 후 입고 박스 번호 조회", notes="견품반출 후 입고 조회합니다.")
	@RequestMapping(value="/selectSampleTakingOutList", method = RequestMethod.POST)
	public ResponseEntity<List<Map<String, Object>>> selectSampleInitData(@RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<List<Map<String, Object>>>(bmsMobPasmService.selectSampleTakingOutList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "견품반출 후 입고 저장", notes="견품반출 후 입고 정보를 저장합니다.")
	@RequestMapping(value="/updateSampleTakingOutList", method = RequestMethod.PUT)
	public void updateSampleTakingOutList(@RequestBody Map<String, Object> map, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, map, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		bmsMobPasmService.updateSampleTakingOutList(map, req);
	}

	@ApiOperation(value = "견품반출 - 견품반출스캔로그 조회", notes="견품반출 - 견품반출스캔로그 조회합니다.")
	@RequestMapping(value="/search/scanLog", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> selectScanLogInfo(@QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<List<Map<String, Object>>>(bmsMobPasmService.selectScanLogInfo(search), HttpStatus.OK);
	}
}
