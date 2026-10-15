package org.yht.exps.express.controller.bms;

import java.util.Arrays;
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
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.bms.bmsAllocateCar.BmsAllocateCar;
import org.yht.exps.express.model.bms.bmsAllocateCar.BmsAllocateCarList;
import org.yht.exps.express.model.bms.bmsAllocateCar.BmsAllocateCarSaveResult;
import org.yht.exps.express.model.bms.bmsAllocateCar.search.SearchBmsAllocateCarList;
import org.yht.exps.express.service.bms.BmsAllocateCarService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "bms")
@RequestMapping("/{entCode}/bms/bmsAllocateCar")
public class BmsAllocateCarController {

	@Resource(name = "bmsAllocateCarService")
	BmsAllocateCarService bmsAllocateCarService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@ApiOperation(value = "배차예정정보 조회", notes="배차예정정보 리스트를 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsAllocateCarList> selectBmsAllocateCarList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsAllocateCarList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsAllocateCarList>(bmsAllocateCarService.selectBmsAllocateCarList(search), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "배차예정정보 리스트 엑셀 다운로드", notes="배차예정정보 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBmsAllocateCarExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsAllocateCarList search, HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsAllocateCarService.selectBmsAllocateCarList(search).getBmsAllocateCarList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	@ApiOperation(value = "일괄배차등록 팝업 상세조회", notes="일괄배차등록 팝업 상세조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get", method = RequestMethod.GET)
	public ResponseEntity<BmsAllocateCar> selectBmsAllocateCarDetail(@PathVariable("entCode") String entCode, SearchBmsAllocateCarList search) throws Exception {
	   return new ResponseEntity<>(bmsAllocateCarService.selectBmsAllocateCarDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "일괄배차등록 팝업 update", notes="일괄배차등록 팝업 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
			//@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/update", method = RequestMethod.PUT)
	public ResponseEntity<BmsAllocateCarSaveResult> updateBmsAllocateCar(@PathVariable("entCode") String entCode, @RequestBody BmsAllocateCar bmsAllocateCar, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsAllocateCar, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		//bmsOut.setOdJobNo(odJobNo);
		return new ResponseEntity<BmsAllocateCarSaveResult>(bmsAllocateCarService.updateBmsAllocateCar(bmsAllocateCar, request), HttpStatus.OK);
	}
	
}
