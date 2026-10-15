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
import org.yht.exps.express.model.bms.bmsInspection.BmsInspectionList;
import org.yht.exps.express.model.bms.bmsInspection.search.SearchBmsInspectionList;
import org.yht.exps.express.model.bms.carryin.BatchCarryIn;
import org.yht.exps.express.model.bms.carryin.CarryInSaveResult;
import org.yht.exps.express.service.bms.BmsInspectionService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "bms")
@RequestMapping("/{entCode}/bms/bmsInspection")
public class BmsInspectionController {

	@Resource(name = "bmsInspectionService")
	BmsInspectionService bmsInspectionService;
	
    @Autowired
    JwtTokenProvider jwtTokenProvider;

	@ApiOperation(value = "검사계획통보 조회", notes="검사계획통보 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsInspectionList> selectBmsInspectionList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsInspectionList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsInspectionList>(bmsInspectionService.selectBmsInspectionList(search), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "검사계획통보 리스트 엑셀 다운로드", notes="검사계획통보 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBmsInspectionExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsInspectionList search, HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsInspectionService.selectBmsInspectionList(search).getBmsInspectionList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	/***
	 * 검사계획 통보 - 특송장 이고 - 반출입 생성 처리
	 * @param entCode
	 * @param batchCarryIn
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "특송장 이고 - 반출입 생성 처리", notes="검사계획통보 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/exec/batch", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> execBatchCarryIn(@PathVariable("entCode") String entCode, @RequestBody BatchCarryIn batchCarryIn, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, batchCarryIn, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		
		batchCarryIn.setEntCode(entCode);
		return new ResponseEntity<CarryInSaveResult>(bmsInspectionService.executeBatchCarryIn(batchCarryIn, request), HttpStatus.OK);
	}

}
