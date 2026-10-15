package org.yht.exps.express.controller.pda;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.bms.carryin.InPlanPkgList;
import org.yht.exps.express.service.pda.PdaCommonPacoService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/pda/pa/co")
public class PdaCommonPacoController {
	
	@Resource(name="pdaCommonPacoService")
	PdaCommonPacoService pdaCommonPacoService;
	
	
	@ApiOperation(value = "박스번호 중복 조회", notes="박스번호 중복 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/duplicatePkgNo", method = RequestMethod.GET)
	public ResponseEntity<InPlanPkgList> checkDuplicatePkgNo(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String, Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<InPlanPkgList>(pdaCommonPacoService.checkDuplicatePkgNo(search), HttpStatus.OK);
	}
}
