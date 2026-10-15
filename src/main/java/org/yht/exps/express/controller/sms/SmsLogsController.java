package org.yht.exps.express.controller.sms;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
import org.yht.exps.express.model.sms.logs.BatchDataLogs;
import org.yht.exps.express.model.sms.logs.BatchDataLogsList;
import org.yht.exps.express.model.sms.logs.InterfaceLogs;
import org.yht.exps.express.model.sms.logs.InterfaceLogsList;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.logs.ProcessLogsCompareList;
import org.yht.exps.express.model.sms.logs.ProcessLogsList;
import org.yht.exps.express.model.sms.logs.search.SearchBatchDataLogs;
import org.yht.exps.express.model.sms.logs.search.SearchInterfaceLogs;
import org.yht.exps.express.model.sms.logs.search.SearchProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.sms.SmsLogsService;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Logs")
@RequestMapping("/{entCode}/sms/logs")
public class SmsLogsController {
	
	@Resource(name = "smsLogsService")
	SmsLogsService smsLogsService;
	
	@Resource(name = "commonService")
	CommonService commonService;
	
	/**
	 * 시스템 로그 List
	 * @param entCode
	 * @param searchProcessLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "시스템 로그 조회", notes="시스템 로그 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<ProcessLogsList> selectOrder(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchProcessLogs searchProcessLogs,
						HttpServletRequest request) throws Exception {
		
		if ("".equals(Util.null2str(searchProcessLogs.getSrchEntCode())))	searchProcessLogs.setSrchEntCode(entCode);

		return new ResponseEntity<ProcessLogsList>(smsLogsService.selectProcessLogs(searchProcessLogs), HttpStatus.OK);
	}
	
	/**
	 * 시스템 로그 비교
	 * @param entCode
	 * @param referenceNoId1
	 * @param referenceNoId2
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "시스템 로그 비교", notes="시스템 로그 비교를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/get/systemLogCompare/{referenceNoId1}/{referenceNoId2}", method = RequestMethod.GET)
	public ResponseEntity<ProcessLogsCompareList> selectProcessLogCompare(
			@PathVariable("entCode") String entCode,
			@PathVariable("referenceNoId1") String referenceNoId1,
			@PathVariable("referenceNoId2") String referenceNoId2) throws Exception {
		
		return new ResponseEntity<ProcessLogsCompareList>(smsLogsService.selectProcessLogCompare(entCode, referenceNoId1, referenceNoId2), HttpStatus.OK);
	}
	
	/**
	 * 시스템 로그 List
	 * @param entCode
	 * @param searchProcessLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "시스템 로그 조회", notes="시스템 로그 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectOrderExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchProcessLogs searchProcessLogs,
						HttpServletRequest request) throws Exception {
		
		if ("".equals(Util.null2str(searchProcessLogs.getSrchEntCode())))	searchProcessLogs.setSrchEntCode(entCode);

		ProcessLogsList list = smsLogsService.selectProcessLogs(searchProcessLogs);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(searchProcessLogs, Map.class);
		map.put("data", list.getProcessLogsList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("System_Log_List", map), HttpStatus.OK);
	}
	
	/**
	 * 시스템 로그
	 * @param entCode
	 * @param index
	 * @param referenceNoId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "시스템 로그 조회", notes="시스템 로그 데이터를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "index", value = "ES Index", required = true, dataType = "string", paramType = "path", defaultValue = "ecm-process-logs-20210706"),
		@ApiImplicitParam(name = "referenceNoId", value = "데이터 ID", required = true, dataType = "string", paramType = "path", defaultValue = "2021070613163916233043")
	})	
	@RequestMapping(value="/get/{index}/{referenceNoId}", method = RequestMethod.GET)
	public ResponseEntity<ProcessLogs> getProcessLogs(
						@PathVariable("entCode") String entCode,
						@PathVariable("index") String index,
						@PathVariable("referenceNoId") String referenceNoId,
						HttpServletRequest request) throws Exception {
		
		return new ResponseEntity<ProcessLogs>(smsLogsService.getProcessLogs(index, referenceNoId), HttpStatus.OK);
	}
	
	
	/**
	 * 시스템 로그 삭제 - 검색건 count
	 * @param entCode
	 * @param searchProcessLogs
	 * @param request
	 * @return 
	 * @throws Exception
	 */
	@ApiOperation(value = "시스템 로그 조회", notes="시스템 로그 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/delete/targetCount", method = RequestMethod.POST)
	public ResponseEntity<Integer> deleteLogsTargetCount(
						@PathVariable("entCode") String entCode,
						@RequestBody SearchProcessLogs searchProcessLogs,
						HttpServletRequest request) throws Exception {
		
		if ("".equals(Util.null2str(searchProcessLogs.getSrchEntCode())))	searchProcessLogs.setSrchEntCode(entCode);
		
		return new ResponseEntity<Integer>(smsLogsService.deleteLogsTargetCount(searchProcessLogs), HttpStatus.OK);
	}
	
	/**
	 * 시스템 로그 delete
	 * @param entCode
	 * @param 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "시스템 로그 리스트 삭제", notes="시스템 로그 리스트를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/delete", method = RequestMethod.POST)
	public ResponseEntity<Map> deleteLogs(
						@PathVariable("entCode") String entCode,
						@RequestBody SearchProcessLogs searchProcessLogs,
						HttpServletRequest request) throws Exception {
		
		if ("".equals(Util.null2str(searchProcessLogs.getSrchEntCode())))	searchProcessLogs.setSrchEntCode(entCode);

		return new ResponseEntity<Map>(smsLogsService.deleteProcessLogs(searchProcessLogs), HttpStatus.OK);
	}
	
	
	
	/**
	 * 수입화물진행정보 데이터처리조회
	 * @param entCode
	 * @param searchProcessLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "수입화물진행정보 데이터처리 조회", notes="수입화물진행정보 데이터처리 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/batchData/cargoProgress/search", method = RequestMethod.GET)
	public ResponseEntity<BatchDataLogs> cargoProgressLog(@PathVariable("entCode") String entCode,@QueryStringArgResolver SearchBatchDataLogs searchBatchDataLogs, HttpServletRequest req) throws Exception {
		Util.setDB(req, searchBatchDataLogs.getSrchEntCode());
		return new ResponseEntity<BatchDataLogs>(smsLogsService.cargoProgressLog(searchBatchDataLogs, req), HttpStatus.OK);
	}
	/**
	 * EDI 수신 데이터처리조회
	 * @param entCode
	 * @param searchProcessLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "관세청 수신 데이터처리 조회", notes="관세청 수신 데이터처리 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/batchData/ediRecvCheck/search/list", method = RequestMethod.GET)
	public ResponseEntity<BatchDataLogsList> ediRecvCheckLogList(@PathVariable("entCode") String entCode,@QueryStringArgResolver SearchBatchDataLogs searchBatchDataLogs, HttpServletRequest req) throws Exception {
		Util.setDB(req, searchBatchDataLogs.getSrchEntCode());
		return new ResponseEntity<BatchDataLogsList>(smsLogsService.ediRecvCheckLogList(searchBatchDataLogs, req), HttpStatus.OK);
	}
	
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
		return new ResponseEntity<InterfaceLogsList>(smsLogsService.interfaceLogList(searchInterfaceLogs), HttpStatus.OK);
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
		
		return new ResponseEntity<InterfaceLogs>(smsLogsService.getInterfaceLogs(index, referenceNoId), HttpStatus.OK);
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

		InterfaceLogsList list = smsLogsService.interfaceLogList(searchInterfaceLogs);
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(searchInterfaceLogs, Map.class);
		map.put("data", list.getInterfaceLogsList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("System_Log_List", map), HttpStatus.OK);
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

		return new ResponseEntity<Integer>(smsLogsService.deleteInterfaceLogsTargetCount(searchInterfaceLogs), HttpStatus.OK);
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

		return new ResponseEntity<Map>(smsLogsService.deleteInterfaceLogs(searchInterfaceLogs), HttpStatus.OK);
	}
}
