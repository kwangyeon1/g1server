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
import org.yht.exps.express.model.bms.bmsBlDivide.BmsBlDivideList;
import org.yht.exps.express.model.bms.bmsBlDivide.search.SearchBmsBlDivideList;
import org.yht.exps.express.service.bms.BmsBlDivideService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "bms")
@RequestMapping("/{entCode}/bms/bmsBlDivide")
public class BmsBlDivideController {

	@Resource(name = "bmsBlDivideService")
	BmsBlDivideService bmsBlDivideService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@ApiOperation(value = "보수&BL분할 조회", notes="보수&BL분할 리스트를 반환합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsBlDivideList> selectBmsBlDivideList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsBlDivideList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsBlDivideList>(bmsBlDivideService.selectBmsBlDivideList(search), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "보수&BL분할 리스트 엑셀 다운로드", notes="보수&BL분할 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBmsBlDivideExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsBlDivideList search, HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsBlDivideService.selectBmsBlDivideList(search).getBmsBlDivideList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

}
