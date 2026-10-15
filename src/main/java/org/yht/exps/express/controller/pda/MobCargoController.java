package org.yht.exps.express.controller.pda;


import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.exps.express.model.mobile.bms.MobCargo;
import org.yht.exps.express.model.mobile.bms.MobCargoList;
import org.yht.exps.express.model.mobile.bms.search.SearchMobCargo;
import org.yht.exps.express.service.pda.MobCargoService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "mobile")
@RequestMapping("/{entCode}/bms/mob/cargo")
public class MobCargoController {
	
	@Resource(name = "mobCargoService")
	MobCargoService mobCargoService;	
	
	@ApiOperation(value = "보세구역관리 화물 상세 조회", notes="보세구역관리  화물 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/{hblNo}", method = RequestMethod.GET)
	public ResponseEntity<MobCargo> selectCargoDetail(@PathVariable("entCode") String entCode, SearchMobCargo search) throws Exception {
		return new ResponseEntity<MobCargo>(mobCargoService.selectCargoDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "보세구역관리 화물조회 중복체크", notes="보세구역관리 화물조회 중복체크합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/checkCargoListByBlNo", method = RequestMethod.GET)
	public ResponseEntity<MobCargoList> selectOrderDetailByCarryoutList(@PathVariable("entCode") String entCode, SearchMobCargo search) throws Exception {
		return new ResponseEntity<MobCargoList>(mobCargoService.checkCargoListByBlNo(search), HttpStatus.OK);
	}
	
}
