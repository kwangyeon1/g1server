package org.yht.exps.express.controller.pda;


import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.mobile.bms.BmsLocalInspectResultList;
import org.yht.exps.express.model.mobile.bms.LocalInspectRstSaveResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMblFromInspectResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMobInspectResult;
import org.yht.exps.express.service.pda.BmsLocalInspectResultService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "mobile")
@RequestMapping("/{entCode}/bms/bwli")
public class BmsLocalInspectResultController {
	
	@Resource(name="bmsLocalInspectResultService")
	BmsLocalInspectResultService bmsLocalInspectResultService;
	
	@ApiOperation(value = "특송창고_검사결과에 이고반출을 확인 결과를 update합니다", notes="특송창고_검사결과를 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateTlcOutOfInspectResultByBlNo", method = RequestMethod.PUT)
	public ResponseEntity<LocalInspectRstSaveResult> updateTlcOutOfInspectResultByBlNo(@PathVariable("entCode") String entCode, @RequestBody SearchMobInspectResult searchMobInspectResult, HttpServletRequest request) throws Exception {
		searchMobInspectResult.setEntCode(entCode);
		return new ResponseEntity<LocalInspectRstSaveResult>(bmsLocalInspectResultService.updateTlcOutOfInspectResultByBlNo(searchMobInspectResult, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "특송창고_검사결과에 이고반입 확인 결과를 update합니다", notes="특송창고_검사결과를 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateTicComeBackOfInspectResultByBlNo", method = RequestMethod.PUT)
	public ResponseEntity<LocalInspectRstSaveResult> updateTicComeBackInspectResultByBlNo(@PathVariable("entCode") String entCode, @RequestBody SearchMobInspectResult searchMobInspectResult, HttpServletRequest request) throws Exception {
		searchMobInspectResult.setEntCode(entCode);
		return new ResponseEntity<LocalInspectRstSaveResult>(bmsLocalInspectResultService.updateTicComeBackOfInspectResultByBlNo(searchMobInspectResult, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "출고전 상태를 확인하여 결과를 반환하고, 정상일때 반출승인의 출고SCAN날짜를 update합니다", notes="반출승인을 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateOutScanDate", method = RequestMethod.PUT)
	public ResponseEntity<LocalInspectRstSaveResult> updateOutScanDate(@PathVariable("entCode") String entCode, @RequestBody SearchMobInspectResult searchMobInspectResult, HttpServletRequest request) throws Exception {
		searchMobInspectResult.setEntCode(entCode);
		return new ResponseEntity<LocalInspectRstSaveResult>(bmsLocalInspectResultService.updateOutScanDate(searchMobInspectResult, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "어제 오늘 2일간의 MBL번호들을 리스트로 출력합니다(중복MBL제외)", notes="MBL번호들을 리스트로 출력합니다.(중복MBL제외)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectMblList", method = RequestMethod.GET)
	public ResponseEntity<BmsLocalInspectResultList> selectMblList(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsLocalInspectResultList>(bmsLocalInspectResultService.selectMblList(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "MBL번호와 검사코드로 필터링된 검사결과 리스트를 출력합니다", notes="필터링된 검사결과 리스트를 출력합니다")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectInspectResultFilteredByCodeGroup", method = RequestMethod.GET)
	public ResponseEntity<BmsLocalInspectResultList> selectInspectResultFilteredByCodeGroup(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMblFromInspectResult search, HttpServletRequest request) throws Exception {
		search.setEntCode(entCode);
		return new ResponseEntity<BmsLocalInspectResultList>(bmsLocalInspectResultService.selectInspectResultFilteredByCodeGroup(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "특송창고 검사결과값을 일괄 finish 업데이트합니다", notes="검사결과값을 일괄 finish 업데이트합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateInspectResultSetFinish", method = RequestMethod.PUT)
	public ResponseEntity<LocalInspectRstSaveResult> updateInspectResultSetFinish(@PathVariable("entCode") String entCode, @RequestBody SearchMblFromInspectResult search, HttpServletRequest request) throws Exception {
		search.setEntCode(entCode);
		return new ResponseEntity<LocalInspectRstSaveResult>(bmsLocalInspectResultService.updateInspectResultSetFinish(search, request), HttpStatus.OK);
	}
	
}
