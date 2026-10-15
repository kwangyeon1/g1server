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
import org.yht.exps.express.model.sms.logs.InterfaceLogs;
import org.yht.exps.express.model.sms.logs.InterfaceLogsList;
import org.yht.exps.express.model.sms.logs.search.SearchInterfaceLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.bms.BmsLogsService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "BMS")
@RequestMapping("/{entCode}/bms/logs")
public class BmsLogsController {
	
	@Resource(name = "bmsLogsService")
	BmsLogsService bmsLogsService;
	
	@Resource(name = "commonService")
	CommonService commonService;
	
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    
	/**
	 * 인터페이스 로그 List
	 * @param entCode
	 * @param searchProcessLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "인터페이스 로그 조회", notes="인터페이스 로그 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/interface/search/list", method = RequestMethod.GET)
	public ResponseEntity<InterfaceLogsList> interfaceLogList(@PathVariable("entCode") String entCode,@QueryStringArgResolver SearchInterfaceLogs searchInterfaceLogs) throws Exception {
		if ("".equals(Util.null2str(searchInterfaceLogs.getSrchEntCode())))	searchInterfaceLogs.setSrchEntCode(entCode);
		return new ResponseEntity<InterfaceLogsList>(bmsLogsService.interfaceLogList(searchInterfaceLogs), HttpStatus.OK);
	}
	
	/**
	 * 인터페이스 로그
	 * @param entCode
	 * @param index
	 * @param referenceNoId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "인터페이스 로그 조회", notes="인터페이스 로그 데이터를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "index", value = "ES Index", required = true, dataType = "string", paramType = "path", defaultValue = "ecm-interface-logs-20210706"),
		@ApiImplicitParam(name = "referenceNoId", value = "데이터 ID", required = true, dataType = "string", paramType = "path", defaultValue = "2021070613163916233043")
	})	
	@RequestMapping(value="/get/interfaceLog/{index}/{referenceNoId}", method = RequestMethod.GET)
	public ResponseEntity<InterfaceLogs> getInterfaceLogs(
						@PathVariable("entCode") String entCode,
						@PathVariable("index") String index,
						@PathVariable("referenceNoId") String referenceNoId,
						HttpServletRequest request) throws Exception {
		
		return new ResponseEntity<InterfaceLogs>(bmsLogsService.getInterfaceLogs(index, referenceNoId), HttpStatus.OK);
	}
	
	
	
	/**
	 * 인터페이스 로그 List
	 * @param entCode
	 * @param SearchInterfaceLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "인터페이스 로그 조회", notes="인터페이스 로그 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/list/excel2", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectOrderExcel2(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchInterfaceLogs searchInterfaceLogs,
						HttpServletRequest request) throws Exception {
		
		if ("".equals(Util.null2str(searchInterfaceLogs.getSrchEntCode())))	searchInterfaceLogs.setSrchEntCode(entCode);

		InterfaceLogsList list = bmsLogsService.interfaceLogList(searchInterfaceLogs);
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(searchInterfaceLogs, Map.class);
		map.put("data", list.getInterfaceLogsList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("BMS_System_Log_List", map, "xlsx"), HttpStatus.OK);
	}
	
	
	/**
	 * 인터페이스 로그 - 검색건 count
	 * @param entCode
	 * @param searchInterfaceLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "시스템 로그 조회", notes="시스템 로그 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/interface/delete/targetCount", method = RequestMethod.POST)
	public ResponseEntity<Integer> deleteLogsTargetCount(
						@PathVariable("entCode") String entCode,
						@RequestBody SearchInterfaceLogs searchInterfaceLogs,
						HttpServletRequest request) throws Exception {
		
		if ("".equals(Util.null2str(searchInterfaceLogs.getSrchEntCode())))	searchInterfaceLogs.setSrchEntCode(entCode);

		return new ResponseEntity<Integer>(bmsLogsService.deleteInterfaceLogsTargetCount(searchInterfaceLogs), HttpStatus.OK);
	}
	
	
	/**
	 * 인터페이스 로그 delete
	 * @param entCode
	 * @param searchInterfaceLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "시스템 로그 리스트 삭제", notes="시스템 로그 리스트를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/interface/delete", method = RequestMethod.POST)
	public ResponseEntity<Map> deleteInterfaceLogs(
						@PathVariable("entCode") String entCode,
						@RequestBody SearchInterfaceLogs searchInterfaceLogs,
						HttpServletRequest request) throws Exception {
		
		if ("".equals(Util.null2str(searchInterfaceLogs.getSrchEntCode())))	searchInterfaceLogs.setSrchEntCode(entCode);

		Util.getSessionUserInfoBMS(request, jwtTokenProvider, searchInterfaceLogs, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		
		return new ResponseEntity<Map>(bmsLogsService.deleteInterfaceLogs(searchInterfaceLogs), HttpStatus.OK);
	}
}
