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
import org.yht.exps.express.model.bms.bmsStock.BmsStockPkgScan;
import org.yht.exps.express.model.mobile.bms.MobBwst;
import org.yht.exps.express.model.mobile.bms.MobBwstSaveResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwst;
import org.yht.exps.express.service.pda.BmsMobBwstService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "pda")
@RequestMapping("/{entCode}/bms/mob/bw/st")
public class BmsMobBwstController {
	
	@Resource(name = "bmsMobBwstService")
	BmsMobBwstService bmsMobBwstService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "재고PKG SCAN 정보 조회", notes="재고실사 - 재고PKG SCAN 조회합니다.")
    @RequestMapping(value = "/search/scan/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> selectInitStockScanInfo(HttpServletRequest request) throws Exception {
        return new ResponseEntity<Map<String, Object>>(bmsMobBwstService.selectInitStockScanInfo(), HttpStatus.OK);
    }

	@ApiOperation(value = "재고실사 PKG SCAN 갱신", notes="재고실사 - 재고PKG SCAN 시작종료 갱신합니다.")
	@RequestMapping(value="/insertStockScanInfo", method = RequestMethod.POST)
	public void insertStockScanInfo(@RequestBody Map<String, Object> map, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, map, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		bmsMobBwstService.insertStockScanInfo(map, req);
	}

	@ApiOperation(value = "재고실사 조회", notes="재고실사 - 재고실사 PKG 조회합니다.")
	@RequestMapping(value="/search/stock", method = RequestMethod.POST)
	public ResponseEntity<BmsStockPkgScan> selectStockInfo(@RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsStockPkgScan>(bmsMobBwstService.selectStockInfo(search, req), HttpStatus.OK);
	}

	@ApiOperation(value = "재고PKG SCAN 로그 조회", notes="재고실사 - 재고PKG SCAN 로그 조회합니다.")
	@RequestMapping(value = "/search/scanLog", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> selectScanLogInfo(@QueryStringArgResolver Map<String, Object> search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<List<Map<String, Object>>>(bmsMobBwstService.selectScanLogInfo(search), HttpStatus.OK);
	}

	@ApiOperation(value = "재고실사처리 - 반입 조회", notes="재고실사처리 - 반입 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/realstock/carryin", method = RequestMethod.GET)
	public ResponseEntity<MobBwst> selectRealStockCarryInDetail(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMobBwst search, HttpServletRequest req) throws Exception {
		search.setEntCode(entCode);
		return new ResponseEntity<MobBwst>(bmsMobBwstService.selectRealStockCarryInDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "재고실사처리 - 재고스캔 insert", notes="재고실사처리 - 재고스캔 insert합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert/realstock/scan", method = RequestMethod.POST)
	public ResponseEntity<MobBwstSaveResult> insertRealStockScan(@PathVariable("entCode") String entCode, @RequestBody SearchMobBwst search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.setEntCode(entCode);
		return new ResponseEntity<MobBwstSaveResult>(bmsMobBwstService.insertRealStockScan(search, req), HttpStatus.OK);
	}
	
//	@ApiOperation(value = "재고PKG SCAN 정보 조회", notes="재고실사 - 재고PKG SCAN 조회합니다.")
//    @RequestMapping(value = "/selectPkgByLocation", method = RequestMethod.GET)
//    public ResponseEntity<Map<String, Object>> selectPkgByLocation(@PathVariable("entCode") String entCode,  @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
//        return new ResponseEntity<Map<String, Object>>(bmsMobBwstService.selectPkgWithBmsInByLocation(search, req), HttpStatus.OK);
//    }
	
	@ApiOperation(value = "재고PKG SCAN 정보 조회", notes="재고실사 - 재고PKG SCAN 조회합니다.")
    @RequestMapping(value = "/selectPkgByLocation", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> selectPkgByLocation(@RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
        return new ResponseEntity<Map<String, Object>>(bmsMobBwstService.selectPkgWithBmsInByLocation(search, req), HttpStatus.OK);
    }
	
	@ApiOperation(value = "재고실사 조회", notes="재고실사 - 재고실사 PKG 조회합니다.")
	@RequestMapping(value="/selectStockInfoByLocation", method = RequestMethod.POST)
	public ResponseEntity<BmsStockPkgScan> selectStockInfoByLocation(@RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsStockPkgScan>(bmsMobBwstService.selectStockInfoByLocation(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "PKG와 SCAN로그 조회", notes="재고실사 - PKG와 SCAN로그 조회")
    @RequestMapping(value = "/selectPkgWithLogByLocation", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> selectStockScanLogByLocation(@QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
        return new ResponseEntity<Map<String, Object>>(bmsMobBwstService.selectPkgWithLogByLocation(search, req), HttpStatus.OK);
    }
	
	@ApiOperation(value = "재고실사시작시간 조회", notes="재고실사 - 재고실사 재고실사시작시간 조회합니다")
	@RequestMapping(value="/getStockScanStartEnd", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getStockScanStartEnd(@QueryStringArgResolver Map<String, Object> search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<Map<String, Object>>(bmsMobBwstService.getStockScanStartEnd(), HttpStatus.OK);
	}
	
}
