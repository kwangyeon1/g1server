package org.yht.exps.express.controller.eCommerce;

import java.util.ArrayList;
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
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.eCommerce.pccc.Pccc;
import org.yht.exps.express.model.eCommerce.pccc.PcccBatch;
import org.yht.exps.express.model.eCommerce.pccc.PcccBatchList;
import org.yht.exps.express.model.eCommerce.pccc.PcccList;
import org.yht.exps.express.model.eCommerce.pccc.PcccSaveResult;
import org.yht.exps.express.model.eCommerce.pccc.PodDetail;
import org.yht.exps.express.model.eCommerce.pccc.search.SearchPcccList;
import org.yht.exps.express.model.eCommerce.pccc.search.SearchPod;
import org.yht.exps.express.model.sms.logs.InterfaceLogs;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.eCommerce.PcccService;
import org.yht.exps.express.service.sms.SmsLogsService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/pccc")
public class PcccController {
	
	@Resource(name = "pcccService")
	PcccService pcccService;
	
	@Resource(name = "smsLogsService")
	SmsLogsService smsLogsService;
	
	@Resource(name = "fileService")
	FileService fileService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "전자상거래 PCCC 조회", notes="PCCC 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<PcccList> selectPcccList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchPcccList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", "logUserName", null, null, true, false, false, false);
		return new ResponseEntity<PcccList>(pcccService.selectPcccList(search), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "전자상거래 PCCC관리 엑셀파일생성", notes="PCCC관리 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectPcccListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchPcccList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		if("pccc".equals(Util.null2str(search.getExcelType()))) {
			String[] columnsArr = {"num","poDateTime","elapseDate","statusExcel","inputMethodExcel","","hblNo","poNo","sendTime1","sendTime2","sendTime3","sendTime4","sendTime5","pcccDateTime","errorCode","errorDesc","cbCode","cbTel","custNameLv2","","","",""};
			String[] columnsWidthArr = {"50","90","50","70","90","100","130","130","130","130","130","130","130","130","90","180","90","130","100","90","120","120","130"};
			Integer[] numFormatsArr = {0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
			String[] excelTypesArr = {"center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center"};
			String[] titlesArr = {"NO","주문일","경과일","주문상태","PRE/POST","OMS_NO","HAWB_NO","ORDER_NO","진행상황1","진행상황2","진행상황3","진행상황4","진행상황5","최종 수집 날짜", "오류코드", "오류사유", "관세사", "관세사 연락처", "사업유형", "OB","아웃콜 D+2","아웃콜 D+3","통화구분"};
			
			List<String> columns = new ArrayList<String>();
			List<String> columnsWidth = new ArrayList<String>();
			List<Integer> numFormats = new ArrayList<Integer>();
			List<String> excelTypes = new ArrayList<String>();
			List<String> titles = new ArrayList<String>();
			for(int i = 0; i < columnsArr.length; i++){
				columns.add(columnsArr[i]);
				columnsWidth.add(columnsWidthArr[i]);
				numFormats.add(numFormatsArr[i]);
				excelTypes.add(excelTypesArr[i]);
				titles.add(titlesArr[i]);
			}
			map.put("columns", columns);
			map.put("columnsWidth", columnsWidth);
			map.put("numFormats", numFormats);
			map.put("excelTypes", excelTypes);
			map.put("titles", titles);
		}
		
		map.put("data", pcccService.selectPcccList(search).getPcccList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("pcccList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 PCCC관리(팝업) 조회", notes="PCCC관리를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/search/detail/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<Pccc> selectPcccDetail(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo,  SearchPcccList search) throws Exception {
		search.setSrchEntCode(entCode);
		search.setOdJobNo(odJobNo);
		return new ResponseEntity<Pccc>(pcccService.selectPcccDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 PCCC수정 저장", notes="PCCC수정 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/pcccCustomsVerify/{odJobNo}", method = RequestMethod.PUT)
	public ResponseEntity<PcccSaveResult> pcccCustomsVerify(@PathVariable("entCode") String entCode,@PathVariable("odJobNo") String odJobNo,  @RequestBody Pccc pccc) throws Exception {
		pccc.setEntCode(entCode);
		pccc.setCustomsIdJobNo(odJobNo);
		PcccSaveResult pcccSaveResult = pcccService.pcccCustomsVerify(pccc);
		return new ResponseEntity<PcccSaveResult>(pcccSaveResult, HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 PCCC 알람톡 발송금지", notes="PCCC 알람톡 발송금지합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
	})
	@RequestMapping(value="/alarmSendStop", method = RequestMethod.PUT)
	public ResponseEntity<PcccSaveResult> pcccCustomsVerify(@PathVariable("entCode") String entCode,  @RequestBody PcccList pcccList) throws Exception {
		pcccList.setEntCode(entCode);
		return new ResponseEntity<PcccSaveResult>(pcccService.alarmSendStop(pcccList), HttpStatus.OK);
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
	public ResponseEntity<InterfaceLogs> getProcessLogs(
						@PathVariable("entCode") String entCode,
						@PathVariable("index") String index,
						@PathVariable("referenceNoId") String referenceNoId,
						HttpServletRequest request) throws Exception {
		
		return new ResponseEntity<InterfaceLogs>(pcccService.getInterfaceLogs(index, referenceNoId), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 PCCC관리 알림톡설정 조회", notes="PCCC관리 알림톡설정 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/batch/list", method = RequestMethod.GET)
	public ResponseEntity<PcccBatchList> selectPcccBatchList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchPcccList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", "logUserName", null, null, true, false, false, false);
		return new ResponseEntity<PcccBatchList>(pcccService.selectPcccBatchList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 PCCC 알림톡설정 저장", notes="PCCC 알림톡설정 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
	})
	@RequestMapping(value="/pcccBatchSave", method = RequestMethod.PUT)
	public ResponseEntity<PcccSaveResult> pcccBatchSave(@PathVariable("entCode") String entCode,  @RequestBody PcccBatch pcccBatch) throws Exception {
		pcccBatch.setEntCode(entCode);
		return new ResponseEntity<PcccSaveResult>(pcccService.pcccBatchSave(pcccBatch), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 PCCC 알림톡설정 삭제", notes="전자상거래 PCCC 알림톡설정를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/pcccBatch/delete", method = RequestMethod.DELETE)
	public ResponseEntity<PcccSaveResult> deletePcccBatchList(@PathVariable("entCode") String entCode, @RequestBody PcccBatchList pcccBatchList, HttpServletRequest request) throws Exception {
		pcccBatchList.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, pcccBatchList, "userId", "logUserName", null, "logUserNameEng");
		pcccBatchList.setUserName(pcccBatchList.getLogUserName());
		return new ResponseEntity<PcccSaveResult>(pcccService.deletePcccBatchList(pcccBatchList, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 PCCC아웃콜수정 저장", notes="PCCC아웃콜수정 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/pcccOutCall/{odJobNo}", method = RequestMethod.PUT)
	public ResponseEntity<PcccSaveResult> pcccOutCallCustomsVerify(@PathVariable("entCode") String entCode,@PathVariable("odJobNo") String odJobNo,  @RequestBody Pccc pccc) throws Exception {
		pccc.setEntCode(entCode);
		pccc.setCustomsIdJobNo(odJobNo);
		PcccSaveResult pcccSaveResult = pcccService.pcccOutCall(pccc);
		return new ResponseEntity<PcccSaveResult>(pcccSaveResult, HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "전자상거래 PCCC 알림톡전송", notes="PCCC 알림톡 전송합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
	})
	@RequestMapping(value="/sendKakao", method = RequestMethod.PUT)
	public ResponseEntity<PcccSaveResult> sendKakao(@PathVariable("entCode") String entCode,  @RequestBody PcccBatch pcccBatch,  HttpServletRequest request) throws Exception {
		pcccBatch.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, pcccBatch, "updateUserId", null, null);
		return new ResponseEntity<PcccSaveResult>(pcccService.sendKakao(pcccBatch), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 POD상태(팝업) 조회", notes="POD상태(팝업) 조회를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/pod/detail", method = RequestMethod.GET)
	public ResponseEntity<PodDetail> selectPodDetail(@PathVariable("entCode") String entCode,  SearchPod search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<PodDetail>(pcccService.selectPodDetail(search), HttpStatus.OK);
	}
	
}
