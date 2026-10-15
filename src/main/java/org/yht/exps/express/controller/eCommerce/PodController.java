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
import org.yht.exps.express.model.eCommerce.pod.PodList;
import org.yht.exps.express.model.eCommerce.pod.PodSaveResult;
import org.yht.exps.express.model.eCommerce.pod.search.SearchPodList;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.eCommerce.PodService;
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
@RequestMapping("/{entCode}/ecm/eCommerce/pod")
public class PodController {
	
	@Resource(name = "podService")
	PodService podService;
	
	@Resource(name = "smsLogsService")
	SmsLogsService smsLogsService;
	
	@Resource(name = "fileService")
	FileService fileService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "전자상거래 POD 조회", notes="POD 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<PodList> selectPodList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchPodList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", "logUserName", null, null, true, false, false, false);
		return new ResponseEntity<PodList>(podService.selectPodList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 POD 관리 엑셀파일생성", notes="POD 관리 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectPodListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchPodList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		if("ECM-L0234".equals(Util.null2str(search.getMenuId()))) {
			String[] columnsArr = {"num","hblNo","custCodeLv1","poDate","poNo","arDate","customsStart","customsClear","deliveryCode","ecm361","ecm370","ecm380","ecm390","ecm420","ecm510","returnRequestId","ecm550","ecm520","ecm530","ecm540"};
			String[] columnsWidthArr = {"60","120","110","90","120","90","130","130","90","130","130","130","130","130","130","130","130","130","130","130"};
			Integer[] numFormatsArr = {0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
			String[] excelTypesArr = {"center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center","center"};
			String[] titlesArr = {"번호","H.B/L No","상위업체","오더일자","오더번호","입항일","통관시작","통관완료","배송사","택배집하","간선이동","간선이동중","배송출발","배송완료","반품지시","반품송장번호","미배송반품","택배집하_반품","배송출발_반품","배송완료_반품"};
			
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
			map.put("data", podService.selectPodList(search).getPodList());
		}else {
			map.put("data", podService.selectPodListCommon(search).getPodList());
		}
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("podList", map), HttpStatus.OK);
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
	public ResponseEntity<ProcessLogs> getProcessLogs(
						@PathVariable("entCode") String entCode,
						@PathVariable("index") String index,
						@PathVariable("referenceNoId") String referenceNoId,
						HttpServletRequest request) throws Exception {
		return new ResponseEntity<ProcessLogs>(podService.getProcessLogs(index, referenceNoId), HttpStatus.OK);
	}		
	
	@ApiOperation(value = "전자상거래 POD 트럐킹전송(쿠팡)", notes="POD 트럐킹 전송합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
	})
	@RequestMapping(value="/tracking", method = RequestMethod.PUT)
	public ResponseEntity<PodSaveResult> tracking(@PathVariable("entCode") String entCode,  @RequestBody SearchPodList search,  HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", null, null);
		return new ResponseEntity<PodSaveResult>(podService.tracking(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 POD 업데이트(쿠팡)", notes="POD 업데이트합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
	})
	@RequestMapping(value="/podUpdate", method = RequestMethod.PUT)
	public ResponseEntity<PodSaveResult> podUpdate(@PathVariable("entCode") String entCode,  @RequestBody SearchPodList search,  HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", null, null);
		return new ResponseEntity<PodSaveResult>(podService.podUpdate(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 POD(공통) 조회", notes="POD 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/common", method = RequestMethod.GET)
	public ResponseEntity<PodList> selectPodListCommon(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchPodList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", "logUserName", null, null, true, false, false, false);
		return new ResponseEntity<PodList>(podService.selectPodListCommon(search), HttpStatus.OK);
	}
}
