package org.yht.exps.express.controller.eCommerce;

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
import org.yht.exps.express.model.eCommerce.print.PrintInfo;
import org.yht.exps.express.model.eCommerce.print.SearchPrintInfo;
import org.yht.exps.express.service.eCommerce.PrintService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/print")
public class PrintController {
	
	@Resource(name = "printService")
	PrintService printService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	
	//오더조회>Print>label 선택 시 출력할 라벨 양식 list
	@ApiOperation(value = "B/L 라벨 출력정보 조회", notes="B/L 라벨 출력정보 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectPrintInfoLabel", method = RequestMethod.PUT)
	public ResponseEntity<List<PrintInfo>> selectPrintInfoLabel(@PathVariable("entCode") String entCode, @RequestBody SearchPrintInfo searchPrintInfo) throws Exception {
		return new ResponseEntity<List<PrintInfo>>(printService.selectPrintInfoLabel(searchPrintInfo), HttpStatus.OK);
	}
	
	//리테일 소스의 /exps/ems/saveBarcodeFromUbi.do 에 해당
	//전자상거래에는 YHTXX-KR_EMS_MANUAL_BC_AIR_H_UMAC 양식만 추가되어 있음
	@ApiOperation(value = "Barcode 출력 임시 테이블 저장", notes="Barcode 출력 임시 테이블 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/saveBarcodeFromUbi", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> saveBarcodeFromUbi(@PathVariable("entCode") String entCode, @RequestBody SearchPrintInfo searchPrintInfo, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, searchPrintInfo, "userId", "userName", null);
		return new ResponseEntity<Map<String, Object>>(printService.saveBarcodeFromUbi(searchPrintInfo), HttpStatus.OK);
	}
	
}
