package org.yht.exps.express.controller.pda;

import java.util.Arrays;
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
import org.yht.exps.express.model.bms.carryin.BmsInPlanPkgResult;
import org.yht.exps.express.model.bms.carryin.InPlanPkgList;
import org.yht.exps.express.model.mobile.bms.BmsBigSizeScan;
import org.yht.exps.express.model.mobile.bms.BmsBigSizeScanList;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBigSizeScan;
import org.yht.exps.express.service.pda.BmsMobPabsService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/bms/mob/pa/bs")
public class BmsMobPabsController {
	
	@Resource(name="bmsMobPabsService")
	BmsMobPabsService bmsMobPabsService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "거대화물지정", notes="거대화물지정합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updatedBmsInBigsize", method = RequestMethod.POST)
	public ResponseEntity<BmsInPlanPkgResult> updatedBmsInBigsize(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.put("srchNo", StringEscapeUtils.escapeHtml4(Util.null2str(search.get("srchNo"))));
		return new ResponseEntity<BmsInPlanPkgResult>(bmsMobPabsService.updatedBmsInBigsize(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "거대화물지정취소 - 박스번호조회", notes="거대화물지정취소 - 박스번호 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/pkgNo", method = RequestMethod.GET)
	public ResponseEntity<BmsInPlanPkgResult> selectPkgNoInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPkgNoList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsInPlanPkgResult>(bmsMobPabsService.selectPkgNoInfo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "거대화물지정취소 - 반입예정,반입 거대지정취소", notes="거대화물지정취소 - 반입예정,반입 거대지정취소합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/bigSizeYn", method = RequestMethod.POST)
	public ResponseEntity<BmsInPlanPkgResult> updateBigSizeYn(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInPlanPkgResult>(bmsMobPabsService.updateBigSizeYn(listData, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "거대화물지정취소 - 거대화물스캔로그 조회", notes="거대화물지정취소 - 거대화물스캔로그 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/scanLog", method = RequestMethod.GET)
	public ResponseEntity<BmsBigSizeScanList> selectScanLogInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMobBigSizeScan search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<BmsBigSizeScanList>(bmsMobPabsService.selectScanLogInfo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "거대화물지정취소 - 거대화물스캔로그 저장", notes="거대화물지정취소 - 거대화물스캔로그 저장합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/save/bigSizeScan", method = RequestMethod.POST)
	public ResponseEntity<BmsInPlanPkgResult> insertBigSizeScanLog(@PathVariable("entCode") String entCode, @RequestBody BmsBigSizeScan bmsBigSizeScan, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, bmsBigSizeScan, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsInPlanPkgResult>(bmsMobPabsService.insertBigSizeScanLog(bmsBigSizeScan, req), HttpStatus.OK);
	}
	
}
