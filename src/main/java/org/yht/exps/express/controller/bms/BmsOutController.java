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
import org.yht.exps.express.model.bms.disposal.InventoryInList;
import org.yht.exps.express.model.bms.out.BatchBmsOut;
import org.yht.exps.express.model.bms.out.BmsEncourageNotiList;
import org.yht.exps.express.model.bms.out.BmsInOut;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutDeliveryList;
import org.yht.exps.express.model.bms.out.BmsOutDetail;
import org.yht.exps.express.model.bms.out.BmsOutDetailList;
import org.yht.exps.express.model.bms.out.BmsOutList;
import org.yht.exps.express.model.bms.out.BmsOutPlanList;
import org.yht.exps.express.model.bms.out.BmsOutSample;
import org.yht.exps.express.model.bms.out.BmsOutSampleList;
import org.yht.exps.express.model.bms.out.BmsOutSaveResult;
import org.yht.exps.express.model.bms.out.DeleteBmsOut;
import org.yht.exps.express.model.bms.out.search.SearchBmsInOut;
import org.yht.exps.express.model.bms.out.search.SearchBmsOut;
import org.yht.exps.express.service.bms.BmsOutFtpSendService;
import org.yht.exps.express.service.bms.BmsOutService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags="BMS")
@RequestMapping("/{entCode}/bms/out")
public class BmsOutController {

	@Resource(name="bmsOutService")
	BmsOutService bmsOutService;
	@Resource(name="bmsOutFtpSendService")
	BmsOutFtpSendService bmsOutFtpSendService;

	@Autowired
    JwtTokenProvider jwtTokenProvider;

	@ApiOperation(value = "반출신고 - 재고 리스트 조회", notes="반출신고 - 재고 리스트 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inventory/search/list", method = RequestMethod.POST)
	public ResponseEntity<InventoryInList> selectInventoryInList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<InventoryInList>(bmsOutService.selectInventoryInList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "반출신고 - 반출 리스트 조회", notes="반출신고 - 반출 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryout/search/list", method = RequestMethod.POST)
	public ResponseEntity<BmsOutList> selectBmsOutList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsOutList>(bmsOutService.selectBmsOutList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "반출신고 - 반출승인/승인취하 리스트 조회", notes="반출신고 - 반출승인/승인취하 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/approvalYn/search/list", method = RequestMethod.POST)
	public ResponseEntity<BmsOutPlanList> selectBmsOutPlanList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsOutPlanList>(bmsOutService.selectBmsOutPlanList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반출신고 - 견품반출 리스트 조회", notes="반출신고 - 견품반출 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sample/search/list", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSampleList> selectBmsOutSampleList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsOutSampleList>(bmsOutService.selectBmsOutSampleList(search), HttpStatus.OK);
	}

	/***
	 * 반출 이고차수 드롭다운 List
	 *
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "반출 이고차수 드롭다운 List", notes="반출 이고차수 드롭다운 List")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/tic/getTicOutSeqListFromBmsOut", method = RequestMethod.GET)
	public ResponseEntity<Map> getTicOutSeqListFromBmsOut(@PathVariable("entCode") String entCode, Map<String, Object> param) throws Exception {
	   return new ResponseEntity<>(bmsOutService.getTicOutSeqListFromBmsOut(param), HttpStatus.OK);
	}
	
	@RequestMapping(value="/tic/updateTicOutSeqFromBmsOut", method = RequestMethod.POST)
	public ResponseEntity<Map> updateTicOutSeqFromBmsOut(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<Map>(bmsOutService.updateTicOutSeqFromBmsOut(listData, request), HttpStatus.OK);
	}
	
	/***
	 * 반출신고 그리드, 재고 그리드에서 상세 호출시에 사용하는 API로
	 *
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "반출신고 상세 한건 조회", notes="폐기 신청 상세 화면 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get", method = RequestMethod.GET)
	public ResponseEntity<BmsOutDetail> selectBmsOutInventoryDetail(@PathVariable("entCode") String entCode, SearchBmsOut search) throws Exception {
	   return new ResponseEntity<>(bmsOutService.selectBmsOutInventoryDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "견품반출 상세 한건 조회", notes="견품반출 상세 화면 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getBmsOutSample", method = RequestMethod.GET)
	public ResponseEntity<BmsOutDetail> selectBmsOutSampleDetail(@PathVariable("entCode") String entCode, SearchBmsOut search) throws Exception {
	   return new ResponseEntity<>(bmsOutService.selectBmsOutSampleDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반출신고 BL No  조회", notes="폐기 신청 반출신고 BL No  상세 화면 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getBmsOutByBlNo", method = RequestMethod.GET)
	public ResponseEntity<BmsOutDetailList> getBmsOutByBlNo(@PathVariable("entCode") String entCode, SearchBmsOut search) throws Exception {
		//search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsOutService.selectBmsOutByBlNo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "견품반출 BL No  조회", notes="견품반출 BL No  상세 화면 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getBmsOutSampleByBlNo", method = RequestMethod.GET)
	public ResponseEntity<BmsOutDetailList> getBmsOutSampleByBlNo(@PathVariable("entCode") String entCode, SearchBmsOut search) throws Exception {
		//search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsOutService.selectBmsOutSampleByBlNo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반입,출입에서 반입번호로 조회해서 각각의 신고번호와 화물관리번호를 조회한다", notes="신고번호와 화물관리번호를 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectCustomsReportNoOrCargoNoFromInOut", method = RequestMethod.GET)
	public ResponseEntity<BmsInOut> selectCustomsReportNoOrCargoNoFromInOut(@PathVariable("entCode") String entCode, SearchBmsInOut search) throws Exception {
		return new ResponseEntity<>(bmsOutService.selectCustomsReportNoOrCargoNoFromInOut(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반출신고 이고반출확인처리", notes="반출신고 이고반출확인처리")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/bmsout/confirmTic61out", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> confirmTic61out(@PathVariable("entCode") String entCode, @RequestBody BmsOut bmsOut, HttpServletRequest request) throws Exception {
		bmsOut.setEntCode(entCode);
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsOut, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.confirmTic61out(bmsOut, request), HttpStatus.OK);
	}
	

	@ApiOperation(value = "반출신고  insert", notes="반출신고을 insert합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/bmsout/insert", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> insertDisposal(@PathVariable("entCode") String entCode, @RequestBody BmsOut bmsOut, HttpServletRequest request) throws Exception {
		//bmsOut.setEntCode(entCode);
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsOut, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.insertBmsOut(bmsOut, request), HttpStatus.OK);
	}
	@ApiOperation(value = "반출신고 update", notes="반출신고을 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
			//@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/bmsout/update", method = RequestMethod.PUT)
	public ResponseEntity<BmsOutSaveResult> updateDisposal(@PathVariable("entCode") String entCode, @RequestBody BmsOut bmsOut, HttpServletRequest request) throws Exception {
		//bmsOut.setEntCode(entCode);
		//bmsOut.setOdJobNo(odJobNo);
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsOut, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.updateBmsOut(bmsOut, request), HttpStatus.OK);
	}
	@ApiOperation(value = "반출신고/반출승인통고  삭제", notes="반출신고 삭제합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/bmsout/delete", method = RequestMethod.DELETE)
	public ResponseEntity<BmsOutSaveResult> deleteBmsOutAndOutPlan(@PathVariable("entCode") String entCode, @RequestBody DeleteBmsOut delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		delete.setEntCode(entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.deleteBmsOutAndOutPlan(delete, request), HttpStatus.OK);
	}
	@ApiOperation(value = "5. 특송장이고반입일괄생성", notes="특송장이고반입일괄생성  BMS_OUT=>BMS_IN")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/bmsout/exec/batch", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> executeBatchOutToIn(@PathVariable("entCode") String entCode, @RequestBody BatchBmsOut  batchBmsOut, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, batchBmsOut, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		batchBmsOut.setEntCode(entCode);
		if("BATCH_OUT_IN".equals(batchBmsOut.getBatchExecType())) { // 1.특송장이고 FTP송신 FTPSEND
			return new ResponseEntity<BmsOutSaveResult>(bmsOutService.executeBatchOutToIn(batchBmsOut, request), HttpStatus.OK);
		}else { //FTP_XML_OUT 5.특송장이고반입일괄생성 반출 그리드 IN
			return new ResponseEntity<BmsOutSaveResult>(bmsOutFtpSendService.sendOutFtpData(batchBmsOut), HttpStatus.OK);
		}
	}
	@ApiOperation(value = "견품반출 insert", notes="견품반출을 insert합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/bmsout/sample/insert", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> insertBmsOutSample(@PathVariable("entCode") String entCode, @RequestBody BmsOutSample bmsOutSample, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsOutSample, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.insertBmsOutSample(bmsOutSample, request), HttpStatus.OK);
	}
	@ApiOperation(value = "견품반출 update", notes="견품반출을 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
			//@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/bmsout/sample/update", method = RequestMethod.PUT)
	public ResponseEntity<BmsOutSaveResult> updateBmsOutSample(@PathVariable("entCode") String entCode, @RequestBody BmsOutSample bmsOutSample, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsOutSample, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		//bmsOut.setOdJobNo(odJobNo);
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.updateBmsOutSample(bmsOutSample, request), HttpStatus.OK);
	}
	
	@RequestMapping(value="/bmsout/sample/reset", method = RequestMethod.PUT)
	public ResponseEntity<BmsOutSaveResult> updateResetSampleIn(@PathVariable("entCode") String entCode, @RequestBody BmsOutSample bmsOutSample, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsOutSample, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.updateResetSampleIn(bmsOutSample, request), HttpStatus.OK);
	}

	/** 선반입반출신고독려통보 조회 */
	@ApiOperation(value = "반출신고 - 선반입반출신고독려통보 조회", notes = "반출신고 - 선반입반출신고독려통보 리스트 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/encourageNoti/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEncourageNotiList> selectBmsEncourageNotiList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsEncourageNotiList>(bmsOutService.selectBmsEncourageNotiList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "정정전원본 키 조회", notes="정정전원본 키 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getChangeDelInNoByCustomsReportNo", method = RequestMethod.GET)
	public ResponseEntity<String> getChangeDelInNoByCustomsReportNo(@PathVariable("entCode") String entCode, String customsReportNo) throws Exception {
		return new ResponseEntity<String>(bmsOutService.getChangeDelInNoByCustomsReportNo(customsReportNo),HttpStatus.OK);
	}

	/**	반출등록  ENTRY - 오류내역 조회 */
	@ApiOperation(value = "등록 ENTRY - 오류내역 조회", notes="등록 ENTRY - 오류내역을 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/get/errorDesc", method = RequestMethod.GET)
	public ResponseEntity<String> selectErrorDesc(@PathVariable("entCode") String entCode, String srchDeclarationNo) throws Exception {
		// 6NB. 반출신고서 전송후 오류 수신 정보
		return new ResponseEntity<String>(bmsOutService.selectErrorDesc(srchDeclarationNo,"6NB"), HttpStatus.OK);
	}

	/** 관세청 XML 송신(Send) - GOVCBR6NB(반출신고서) */
	@ApiOperation(value = "관세청 XML 송신(Send)", notes = "GOVCBR6NB(반출신고서) 목록을 전송합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/exec/send/bmsOutList", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> sendOutToXml(@PathVariable("entCode") String entCode, @RequestBody BatchBmsOut params, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, params, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		params.setEntCode(entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.sendOutToXml(params, request), HttpStatus.OK);
	}

	/**
	 * 엑셀 다운로드
	 * @param entCode
	 * @param search
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "반출신고 - 재고 리스트 엑셀 다운로드", notes="반출신고 - 재고 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/inventory/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectInventoryInExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutService.selectInventoryInList(search).getInventoryInList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "반출신고 - 반출 리스트 엑셀 다운로드", notes="반출신고 - 반출 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/carryout/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBmsOutExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutService.selectBmsOutList(search).getBmsOutList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "반출신고 - 반출승인/승인취하 리스트 엑셀 다운로드", notes="반출신고 - 반출승인/승인취하 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/approvalYn/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBmsOutPlanExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutService.selectBmsOutPlanList(search).getBmsOutPlanList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "반출신고 - 선반입반출신고독려통보 리스트 엑셀 다운로드", notes = "반출신고 - 선반입반출신고독려통보 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/encourageNoti/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBmsEncourageNotiExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutService.selectBmsEncourageNotiList(search).getBmsEncourageNotiList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "반출신고 - 견품반출 리스트 엑셀 다운로드", notes="반출신고 - 견품반출 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/sample/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBmsOutSampleExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutService.selectBmsOutSampleList(search).getBmsOutSampleList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}
	
	@ApiOperation(value = "택배사연계 - 택배사연계 리스트 조회", notes="택배사연계 - 택배사연계 리스트 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delivery/search/list", method = RequestMethod.POST)
	public ResponseEntity<BmsOutDeliveryList> selectDeliveryList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsOutDeliveryList>(bmsOutService.selectDeliveryList(search), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "택배사연계 - 택배사연계 리스트 엑셀 다운로드", notes="택배사연계 - 택배사연계 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/delivery/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectDeliveryExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutService.selectDeliveryList(search).getBmsOutDeliveryList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}
	
	@ApiOperation(value = "택배사연계 - 택배사연계 재전송 update", notes="택배사연계 재전송 update합니다.")
	@RequestMapping(value="/delivery/updateDeliverySeq", method = RequestMethod.POST)
	public ResponseEntity<Map> updateDeliverySeq(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<Map>(bmsOutService.updateDeliverySeq(listData, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "출고SCAN수기저장", notes = "출고SCAN 수기 저장합니다.")
	@RequestMapping(value="/bmsout/scan/update", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> updateOutScan(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.updateOutScan(listData, request), HttpStatus.OK);
	}
	
	/**
	 * 접수통보후반출수정 반출접수체크
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/raq/checkLastPodStatus", method = RequestMethod.GET)
	public ResponseEntity<BmsOutSaveResult> checkLastPodStatus(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String, Object> search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.checkLastPodStatus(search, request), HttpStatus.OK);
	}
	
	/**
	 * 접수통보후반출수정 수량,중량수정
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/raq/modCtWt", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> modCtWt(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.modCtWt(search, request), HttpStatus.OK);
	}
	
	/**
	 * 접수통보후반출수정 반출삭제
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/raq/delOut", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> delOut(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.put("entCode", entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.delOut(search, request), HttpStatus.OK);
	}
	
	/**
	 * 반입정보매핑
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/bmsout/update/inMapping", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> updateInMapping(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.updateInMapping(listData, request), HttpStatus.OK);
	}
	
	/**
	 * 세관검사장 변경 : 반출 진행상태를 세관검사장(BMSA2)으로 변경
	 * 
	 * @param entCode
	 * @param listData
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "세관검사장 변경", notes = "반출 진행상태를 세관검사장(BMSA2)으로 변경합니다.")
	@RequestMapping(value="/bmsout/customsInspect/update", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> updateCustomsInspect(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		listData.put("entCode", entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsOutService.updateCustomsInspect(listData, request), HttpStatus.OK);
	}

}
