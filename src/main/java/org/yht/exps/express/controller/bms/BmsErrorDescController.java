package org.yht.exps.express.controller.bms;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.bms.bmsErrorDesc.BmsErrorDescList;
import org.yht.exps.express.model.bms.bmsErrorDesc.search.SearchBmsErrorDescList;
import org.yht.exps.express.service.bms.BmsErrorDescService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "bms")
@RequestMapping("/{entCode}/bms/errorDesc")
public class BmsErrorDescController {

	@Resource(name = "bmsErrorDescService")
	BmsErrorDescService bmsErrorDescService;

	/**
	 * 반입신고 출력용 추가 데이터 리스트
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "오류통보 or 결과통보 리스트", notes="오류통보 or 결과통보 리스트를 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/get/list", method = RequestMethod.GET)
	public ResponseEntity<BmsErrorDescList> selectBmsInPrintList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsErrorDescList search) throws Exception {
		search.setSrchEntCode(entCode); 
		return new ResponseEntity<BmsErrorDescList>(bmsErrorDescService.selectBmsErrorDescList(search), HttpStatus.OK);
	}

}
