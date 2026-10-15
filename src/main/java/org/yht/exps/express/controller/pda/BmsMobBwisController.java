package org.yht.exps.express.controller.pda;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.bms.carryin.CarryInSaveResult;
import org.yht.exps.express.model.mobile.bms.MobBwis;
import org.yht.exps.express.model.mobile.bms.MobBwisSaveResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwis;
import org.yht.exps.express.service.pda.BmsMobBwisService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
	
@RestController
@RequestMapping("/{entCode}/bms/mob/bw/is")
public class BmsMobBwisController {
	
	@Resource(name="bmsMobBwisService")
	BmsMobBwisService bmsMobBwisService;
	
	@ApiOperation(value = "검사대상확인 - 검사대상", notes="검사대상확인 - 검사대상 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/inspect/result", method = RequestMethod.GET)
	public ResponseEntity<MobBwis> selectInspectResult(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMobBwis search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<MobBwis>(bmsMobBwisService.selectInspectResult(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "검사대상확인 - 검사 시작 업데이트", notes="검사대상확인 - 검사 시작 업데이트합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/seInspect/start", method = RequestMethod.GET)
	public ResponseEntity<MobBwisSaveResult> updateSeInspectStart(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMobBwis search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<MobBwisSaveResult>(bmsMobBwisService.updateSeInspectStart(search), HttpStatus.OK);
	}
}
