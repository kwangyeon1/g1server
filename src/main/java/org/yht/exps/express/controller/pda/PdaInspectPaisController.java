package org.yht.exps.express.controller.pda;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.BmsInspectResultRst;
import org.yht.exps.express.service.pda.PdaInspectPaisService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/pda/pa/is")
public class PdaInspectPaisController {
	
	@Resource(name="pdaInspectPaisService")
	PdaInspectPaisService pdaInspectPaisService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "BL리스트와 검사결과,박스갯수", notes="BL리스트및 검사결과,박스갯수 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectBmsInWithInspectResult", method = RequestMethod.GET)
	public ResponseEntity<BmsInspectResultRst> selectBmsInWithInspectResult(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.selectBmsInWithInspectResult(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "BL리스트와 검사결과,박스갯수", notes="BL리스트및 검사결과,박스갯수 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectBmsInWithInspectResultV2", method = RequestMethod.GET)
	public ResponseEntity<BmsInspectResultRst> selectBmsInWithInspectResultV2(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.selectBmsInWithInspectResultV2(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "검사피킹", notes="검사피킹 - 박스 피킹 리스트 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectBmsInspectResultPicking", method = RequestMethod.GET)
	public ResponseEntity<BmsInspectResultRst> selectBmsInspectResultPicking(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.selectBmsInspectResultPicking(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "검사피킹", notes="검사피킹 - 박스 피킹 (검사결과 업데이트)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateBmsInspectResultPicking", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> updateBmsInspectResultPicking(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.put("packingNo", StringEscapeUtils.escapeHtml4(Util.null2str(search.get("packingNo"))));
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.updateBmsInspectResultPicking(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "검사스캔로그 조회", notes="검사스캔로그 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/scanLog", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> selectBmsInspectScanLogInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<List<Map<String, Object>>>(pdaInspectPaisService.selectBmsInspectScanLogInfo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "scan: 검사완료", notes="검사완료 - 박스 검사상태설정 (박스 검사상태 업데이트)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateBmsInPkgInspectionStatusE", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> updateBmsInPkgLocationPicking(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.updateBmsInPkgInspectionStatusE(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "scan: 검사완료", notes="검사완료 - 박스 검사상태설정 (박스 검사상태 업데이트)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateBmsInPkgInspectionStatusEV2", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> updateBmsInPkgInspectionStatusEV2(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.updateBmsInPkgInspectionStatusEV2(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "일괄검사완료", notes="검사완료 - 일괄검사완료 (검사완료 업데이트)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batchInspectResultFinish", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> batchBmsInPkgLocationPicking(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.batchInspectResultFinish(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "BL리스트와 검사결과,박스갯수", notes="BL리스트및 검사결과,박스갯수 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectInspectResultByInNo", method = RequestMethod.GET)
	public ResponseEntity<BmsInspectResultRst> selectInspectResultByInNo(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.selectInspectResultByInNo(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "검사결과 추가", notes="검사결과 추가")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/addInspectResult", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> addInspectResult(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.addInspectResult(search, req), HttpStatus.OK);
	}

	@ApiOperation(value = "검사사유관리", notes="검사사유관리 - 조회")
	@RequestMapping(value="/selectInspectResult", method = RequestMethod.GET)
	public ResponseEntity<BmsInspectResult> selectInspectResult(@QueryStringArgResolver Map<String, Object> search) throws Exception {
		return new ResponseEntity<BmsInspectResult>(pdaInspectPaisService.selectInspectResult(search), HttpStatus.OK);
	}

	@ApiOperation(value = "검사사유관리", notes="검사사유관리 - 저장")
	@RequestMapping(value="/updateInspectResult", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> updateInspectResult(@RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.put("inspectRemark", StringEscapeUtils.escapeHtml4(Util.null2str(search.get("inspectRemark"))));
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.updateInspectResult(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "이고검사완료", notes="검사사유관리 - 이고검사완료")
	@RequestMapping(value="/updateInspectComplete", method = RequestMethod.POST)
	public ResponseEntity<BmsInspectResultRst> updateInspectComplete(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInspectResultRst>(pdaInspectPaisService.updateInspectComplete(search, req), HttpStatus.OK);
	}
}
