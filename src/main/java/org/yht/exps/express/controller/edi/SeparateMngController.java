package org.yht.exps.express.controller.edi;

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
import org.yht.exps.express.model.edi.separateMng.SeparateMngDoc;
import org.yht.exps.express.model.edi.separateMng.SeparateMngList;
import org.yht.exps.express.model.edi.separateMng.SeparateMngSaveResult;
import org.yht.exps.express.model.edi.separateMng.search.SearchSeparateMngList;
import org.yht.exps.express.service.edi.SeparateMngService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "EDI")
@RequestMapping("/{entCode}/ecm/edi/separateMng")
public class SeparateMngController {
	
	@Resource(name = "separateMngService")
	SeparateMngService separateMngService;	
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "ECM 별도관리물품해제신고 리스트 조회", notes="ECM 별도관리물품해제신고 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/doc/search/list", method = RequestMethod.GET)
	public ResponseEntity<SeparateMngList> selectSeparateDocList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchSeparateMngList search) throws Exception {
		return new ResponseEntity<SeparateMngList>(separateMngService.selectSeparateDocList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 별도관리물품해제신고 하우스 리스트 조회", notes="ECM 별도관리물품해제신고 하우스 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<SeparateMngList> selectSeparateList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchSeparateMngList search) throws Exception {
		return new ResponseEntity<SeparateMngList>(separateMngService.selectSeparateList(search), HttpStatus.OK);
	}
		
	@ApiOperation(value = "별도관리물품해제신고 insert", notes="별도관리물품해제신고 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<SeparateMngSaveResult> insertDeliveryChg(@PathVariable("entCode") String entCode, @RequestBody SeparateMngDoc separateMngDoc, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, separateMngDoc, "registerUserId", null, null);
		return new ResponseEntity<SeparateMngSaveResult>(separateMngService.insertSeparateMng(separateMngDoc), HttpStatus.OK);
	}
	
	@ApiOperation(value = "별도관리물품해제신고 삭제", notes="별도관리물품해제신고 일괄삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batch/delete", method = RequestMethod.DELETE)
	public ResponseEntity<SeparateMngSaveResult> deleteBatchSeparateMng(@PathVariable("entCode") String entCode, @RequestBody SeparateMngDoc separateMngDoc) throws Exception {
		return new ResponseEntity<SeparateMngSaveResult>(separateMngService.deleteBatchSeparateMng(separateMngDoc), HttpStatus.OK);
	}
}
