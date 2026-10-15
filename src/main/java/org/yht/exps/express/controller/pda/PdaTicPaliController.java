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
import org.yht.exps.express.model.bms.carryin.BmsInspectResultRst;
import org.yht.exps.express.model.bms.carryin.CarryInRst;
import org.yht.exps.express.service.pda.PdaTicPaliService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/pda/pa/li")
public class PdaTicPaliController {

	@Resource(name="pdaTicPaliService")
	PdaTicPaliService pdaTicPaliService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "이고반입확인: 이고반출관련 반입조회", notes="이고반출관련 반입조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectBmsInRelatedTicBmsOut", method = RequestMethod.GET)
	public ResponseEntity<BmsInspectResultRst> selectBmsInRelatedTicBmsOut(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<BmsInspectResultRst>(pdaTicPaliService.selectBmsInRelatedTicBmsOut(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "이고반입확인: 이고반출관련 반입조회", notes="이고반출관련 반입조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectBoxRelatedTicBmsOut", method = RequestMethod.GET)
	public ResponseEntity<BmsInspectResultRst> selectBoxRelatedTicBmsOut(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<BmsInspectResultRst>(pdaTicPaliService.selectBoxRelatedTicBmsOut(search, req), HttpStatus.OK);
	}
	
	/** 이고반출확정 */
	@ApiOperation(value = "이고반출확정", notes="이고반출확정 - 이고반출 출고픽킹된 박스 리스트 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectBmsOutTicOutScanPicking", method = RequestMethod.GET)
	public ResponseEntity<CarryInRst> selectBmsOutTicOutScanPicking(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<CarryInRst>(pdaTicPaliService.selectBmsOutTicOutScanPicking(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "이고반출확정", notes="이고반출확정 - 박스스캔 (이고반출구분 업데이트)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateBmsOutTicOutScanStatus", method = RequestMethod.POST)
	public ResponseEntity<CarryInRst> updateBmsOutTicOutScanStatus(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInRst>(pdaTicPaliService.updateBmsOutTicOutScanStatus(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "이고반입확인: 이고반출SCAN 최종처리및 이고반입생성대기처리 업데이트", notes="이고반입확인: 이고반출SCAN 최종처리및 이고반입생성대기처리 업데이트")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateFinishTicMoveScan", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> updateBmsInPkgLocationPicking(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInspectResultRst>(pdaTicPaliService.updateFinishTicMoveScan(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "이고반입확인: 이고반출SCAN 최종처리및 이고반입생성대기처리 업데이트", notes="이고반입확인: 이고반출SCAN 최종처리및 이고반입생성대기처리 업데이트")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateBoxFinishTicMoveScan", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> updateBoxFinishTicMoveScan(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInspectResultRst>(pdaTicPaliService.updateBoxFinishTicMoveScan(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "이고반입확인: 일괄입고완료", notes="이고반입확인: 일괄입고완료")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batchInspectResultFinish", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> batchBmsInPkgLocationPicking(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInspectResultRst>(pdaTicPaliService.batchInspectResultFinish(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "이고반입확인: 일괄입고완료", notes="이고반입확인: 일괄입고완료")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batchBoxFinish", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> batchBoxFinish(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInspectResultRst>(pdaTicPaliService.batchBoxFinish(search, req), HttpStatus.OK);
	}

	/**
	 * 이고 반출 대상 조회
	 * @param entCode
	 * @param search
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "이고 반출 대상 조회", notes="이고 반출 대상 조회합니다.")
	@RequestMapping(value="/selectBmsInWithInspectResult", method = RequestMethod.POST)
	public ResponseEntity<CarryInRst> selectBmsInWithInspectResult(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInRst>(pdaTicPaliService.selectBmsInWithInspectResult(search, req), HttpStatus.OK);
	}

	/**
	 * 이고 반출 대상 스캔 로그 조회
	 * @param entCode
	 * @param search
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "이고반출 - 이고반출 스캔로그 조회", notes="이고반출 - 이고반출 스캔로그 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/scanLog", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> selectScanLogInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<List<Map<String, Object>>>(pdaTicPaliService.selectScanLogInfo(search), HttpStatus.OK);
	}
}
