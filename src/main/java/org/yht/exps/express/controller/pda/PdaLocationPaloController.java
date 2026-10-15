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
import org.yht.exps.express.model.bms.bmsPkgNo.search.SearchBmsPkgNoList;
import org.yht.exps.express.model.mobile.bms.MobPaloList;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwlo;
import org.yht.exps.express.model.bms.carryin.BmsInPlanPkgResult;
import org.yht.exps.express.model.bms.carryin.BmsInspectResultRst;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.service.pda.PdaLocationPaloNewService;
import org.yht.exps.express.service.pda.PdaLocationPaloService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/pda/pa/lo")
public class PdaLocationPaloController {
	
	@Resource(name="pdaLocationPaloService")
	PdaLocationPaloService pdaLocationPaloService;
	
	@Resource(name="pdaLocationPaloNewService")
	PdaLocationPaloNewService pdaLocationPaloNewService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	
	@ApiOperation(value = "로케이션적치 - 로케이션정보 조회", notes="로케이션적치 - 로케이션정보 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/location/info", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> selectLocationPlace(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<List<Map<String, Object>>>(pdaLocationPaloService.selectLocationPlace(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - orig location 업데이트", notes="로케이션적치 - orig location 업데이트.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateOrigLocation", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> updateOrigLocation(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.put("srchEntCode", entCode);
		search.put("srchOrigLocation", StringEscapeUtils.escapeHtml4(Util.null2str(search.get("srchOrigLocation"))));
		return new ResponseEntity<Map<String, Object>>(pdaLocationPaloService.updateOrigLocation(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 박스번호조회", notes="로케이션적치 - 박스번호 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/pkgNo", method = RequestMethod.GET)
	public ResponseEntity<MobPaloList> selectPkgNoInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMobBwlo search, HttpServletRequest req) throws Exception {
		search.setEntCode(entCode);
		return new ResponseEntity<MobPaloList>(pdaLocationPaloService.selectPkgNoInfo(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 박스번호조회", notes="로케이션적치 - 박스번호 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/pkgNoNew", method = RequestMethod.GET)
	public ResponseEntity<MobPaloList> selectPkgNoInfoNew(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMobBwlo search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.setSrchNo(StringEscapeUtils.escapeHtml4(Util.null2str(search.getSrchNo())));
		search.setEntCode(entCode);
		return new ResponseEntity<MobPaloList>(pdaLocationPaloService.selectPkgNoInfoNew(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 박스번호조회", notes="로케이션적치 - 박스번호 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/pkgNoNew13F", method = RequestMethod.POST)
	public ResponseEntity<MobPaloList> selectPkgNoInfoNew13F(@PathVariable("entCode") String entCode, @RequestBody SearchMobBwlo search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.setSrchNo(StringEscapeUtils.escapeHtml4(Util.null2str(search.getSrchNo())));
		search.setEntCode(entCode);
		return new ResponseEntity<MobPaloList>(pdaLocationPaloNewService.selectPkgNoInfoNew13F(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 박스번호조회", notes="로케이션적치 - 박스번호 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batchSaveLocation", method = RequestMethod.POST)
	public ResponseEntity<MobPaloList> batchSaveLocation(@PathVariable("entCode") String entCode, @RequestBody SearchMobBwlo search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.setEntCode(entCode);
		return new ResponseEntity<MobPaloList>(pdaLocationPaloService.batchSaveLocation(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 박스번호조회", notes="로케이션적치 - 박스번호 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batchSaveLocation13F", method = RequestMethod.POST)
	public ResponseEntity<MobPaloList> batchSaveLocation13F(@PathVariable("entCode") String entCode, @RequestBody SearchMobBwlo search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.setEntCode(entCode);
		return new ResponseEntity<MobPaloList>(pdaLocationPaloNewService.batchSaveLocation13F(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 공로케이션조회", notes="로케이션적치 - 공로케이션 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/emptyLocation", method = RequestMethod.GET)
	public ResponseEntity<MobPaloList> selectEmptyLocation(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPkgNoList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<MobPaloList>(pdaLocationPaloService.selectEmptyLocation(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 로케이션스캔로그 조회", notes="로케이션적치 - 로케이션스캔로그 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/scanLog", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> selectScanLogInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<List<Map<String, Object>>>(pdaLocationPaloService.selectScanLogInfo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 홀드조회", notes="로케이션적치 - 홀드 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/holdInfo", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> selectBmsInHoldInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPkgNoList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<Map<String, Object>>(pdaLocationPaloService.selectBmsInHoldInfo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 검사,검역조회", notes="로케이션적치 - 검사,검역 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/inspectionInfo", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> selectBmsInInspectionInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPkgNoList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<Map<String, Object>>(pdaLocationPaloService.selectBmsInInspectionInfo(search), HttpStatus.OK);
	}	
	
	@ApiOperation(value = "검사결과 추가", notes="검사결과 추가")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/addLocation", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> addInspectResult(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.put("locationName", StringEscapeUtils.escapeHtml4(Util.null2str(search.get("locationName"))));
		return new ResponseEntity<Map<String, Object>>(pdaLocationPaloService.addLocation(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "scan: 로케이션및 박스리스트 조회", notes="로케이션및 박스리스트 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectPkgWithBmsInByLocation", method = RequestMethod.GET)
	public ResponseEntity<BmsInPlanPkgResult> selectPkgWithBmsInByLocation(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<BmsInPlanPkgResult>(pdaLocationPaloService.selectPkgWithBmsInByLocation(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "scan: 로케이션피킹 - 박스조회를 통한 로케이션 조회", notes="박스조회를 통한 로케이션 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectLocationFromPkg", method = RequestMethod.GET)
	public ResponseEntity<BmsInPlanPkgResult> selectLocationFromPkg(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<BmsInPlanPkgResult>(pdaLocationPaloService.selectLocationFromPkg(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "scan: 로케이션피킹 - 박스조회를 통한 로케이션 조회", notes="박스조회를 통한 로케이션 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectLocationFromAllPkgByScan", method = RequestMethod.GET)
	public ResponseEntity<BmsInPlanPkgResult> selectLocationFromAllPkgByScan(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<BmsInPlanPkgResult>(pdaLocationPaloService.selectLocationFromAllPkgByScan(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "scan: 박스 피킹", notes="로케이션피킹 - 박스 피킹 (박스 로케이션 업데이트)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateBmsInPkgLocationPicking", method = RequestMethod.POST)
	public ResponseEntity<BmsInPlanPkgResult> updateBmsInPkgLocationPicking(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInPlanPkgResult>(pdaLocationPaloService.updateBmsInPkgLocationPicking(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "박스 일괄피킹", notes="로케이션피킹 - 박스 일괄피킹 (박스 로케이션 업데이트)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batchBmsInPkgLocationPicking", method = RequestMethod.POST)
	public ResponseEntity<BmsInPlanPkgResult> batchBmsInPkgLocationPicking(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInPlanPkgResult>(pdaLocationPaloService.batchBmsInPkgLocationPicking(search, req), HttpStatus.OK);
	}

	@ApiOperation(value = "로케이션적치 - 현재 로케이션 리스트 조회", notes="로케이션적치 - 현재 로케이션 리스트 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getPkgListByCurrentLocation13F", method = RequestMethod.GET)
	public ResponseEntity<MobPaloList> getPkgListByCurrentLocation(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMobBwlo search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.setEntCode(entCode);
		return new ResponseEntity<MobPaloList>(pdaLocationPaloNewService.getPkgListByCurrentLocation13F(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 현재 로케이션에서 변경할 로케이션으로 pkg 일괄 update", notes="로케이션적치 - 현재 로케이션에서 변경할 로케이션으로 pkg 일괄 update")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batchLocationFromCurrentToTarget13F", method = RequestMethod.POST)
	public ResponseEntity<MobPaloList> batchLocationFromCurrentToTarget(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.put("entCode", entCode);
		return new ResponseEntity<MobPaloList>(pdaLocationPaloNewService.batchLocationFromCurrentToTarget13F(search, req), HttpStatus.OK);
	}

	//------------ 14A----------------------//
	@ApiOperation(value = "로케이션적치 - 해당 박스 최초로케이션 변경", notes="로케이션적치 - 해당 박스 최초로케이션 변경")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/pkgNoNew14A", method = RequestMethod.POST)
	public ResponseEntity<MobPaloList> selectPkgNoInfoNew14A(@PathVariable("entCode") String entCode, @RequestBody SearchMobBwlo search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.setSrchNo(StringEscapeUtils.escapeHtml4(Util.null2str(search.getSrchNo())));
		search.setEntCode(entCode);
		return new ResponseEntity<MobPaloList>(pdaLocationPaloNewService.selectPkgNoInfoNew14A(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로케이션적치 - 해당 박스 최초로케이션 변경", notes="로케이션적치 - 해당 박스 최초로케이션 변경")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/pkgSameOrigLoaction", method = RequestMethod.POST)
	public ResponseEntity<MobPaloList> updatePkgSameOrigLoaction(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.put("entCode", entCode);
		return new ResponseEntity<MobPaloList>(pdaLocationPaloNewService.updatePkgSameOrigLoaction(search, req), HttpStatus.OK);
	}
}
