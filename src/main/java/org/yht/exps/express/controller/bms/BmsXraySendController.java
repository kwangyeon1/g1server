package org.yht.exps.express.controller.bms;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendBlList;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendList;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendLogList;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendSaveResult;
import org.yht.exps.express.model.bms.bmsXraySend.search.SearchXraySend;
import org.yht.exps.express.model.bms.bmsXraySend.search.SearchXraySendLog;
import org.yht.exps.express.service.bms.BmsXraySendService;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "BMS")
@RequestMapping("/{entCode}/bms/xraySend/")
public class BmsXraySendController {
	
	@Resource(name = "bmsXraySendService")
	BmsXraySendService bmsXraySendService;
	
	/**
	 * xray 재전송 List
	 * @param entCode
	 * @param searchProcessLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "xray 재전송 조회", notes="xray 재전송 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<XraySendList> xraySendList(@PathVariable("entCode") String entCode,@QueryStringArgResolver SearchXraySend searchXraySend) throws Exception {
		searchXraySend.setSrchEntCode(entCode);
		XraySendList xraySendList = new XraySendList();
		xraySendList = bmsXraySendService.xraySendList(searchXraySend);
		return new ResponseEntity<XraySendList>(xraySendList, HttpStatus.OK);
	}
	
	@ApiOperation(value = "xray 재전송 조회", notes="xray 재전송 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/send/update", method = RequestMethod.POST)
	public ResponseEntity<XraySendSaveResult> updateXraySend(@PathVariable("entCode") String entCode,@RequestBody SearchXraySend searchXraySend) throws Exception {
		return new ResponseEntity<XraySendSaveResult>(bmsXraySendService.updateXraySend(searchXraySend), HttpStatus.OK);
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
	@RequestMapping(value="/logs/search/list", method = RequestMethod.GET)
	public ResponseEntity<XraySendLogList> interfaceLogList(@PathVariable("entCode") String entCode,@QueryStringArgResolver SearchXraySendLog search) throws Exception {
		if ("".equals(Util.null2str(search.getSrchEntCode())))	search.setSrchEntCode(entCode);
		return new ResponseEntity<XraySendLogList>(bmsXraySendService.xraySendLogList(search), HttpStatus.OK);
	}
	
	/**
	 * xray > ADD BL 조회
	 * @param entCode
	 * @param searchProcessLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "xray ADD BL 조회", notes="xray ADD BL 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/getBmsByBlNo", method = RequestMethod.GET)
	public ResponseEntity<XraySendBlList> selectXraySendBl(@PathVariable("entCode") String entCode,@QueryStringArgResolver SearchXraySend searchXraySend) throws Exception {
		searchXraySend.setSrchEntCode(entCode);
		XraySendBlList xraySendBlList = new XraySendBlList();
		xraySendBlList = bmsXraySendService.selectXraySendBl(searchXraySend);
		return new ResponseEntity<XraySendBlList>(xraySendBlList, HttpStatus.OK);
	}
	
	/**
	 * xray > ADD BL
	 * @param entCode
	 * @param searchProcessLogs
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "xray ADD BL", notes="xray BL 리스트 ADD")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/addBl", method = RequestMethod.POST)
	public ResponseEntity<XraySendSaveResult> addXraySendBl(@PathVariable("entCode") String entCode,@RequestBody SearchXraySend searchXraySend) throws Exception {
		searchXraySend.setSrchEntCode(entCode);
		return new ResponseEntity<XraySendSaveResult>(bmsXraySendService.addXraySendBl(searchXraySend), HttpStatus.OK);
	}
	
}
