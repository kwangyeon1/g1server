package org.yht.exps.express.controller.bms;

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
import org.yht.exps.express.model.bms.bmsEc.BmsEcEtcList;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInList;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInPlanList;
import org.yht.exps.express.model.bms.bmsEc.BmsEcOutList;
import org.yht.exps.express.model.bms.bmsEc.BmsEcOutPlanList;
import org.yht.exps.express.model.bms.bmsEc.BmsEcSend;
import org.yht.exps.express.model.bms.bmsEc.BmsEcSendResult;
import org.yht.exps.express.model.bms.bmsEc.search.SearchEc;
import org.yht.exps.express.service.bms.BmsEcService;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "bms")
@RequestMapping("/{entCode}/bms/bmsEc")
public class BmsEcController {

	@Resource(name = "bmsEcService")
	BmsEcService bmsEcService;

	@ApiOperation(value = "EC송수신조회", notes="반입예정 마스터 리스트를 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/inPlan/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEcInPlanList> selectEcInPlanList(@QueryStringArgResolver SearchEc search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcInPlanList>(bmsEcService.selectEcInPlanList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회 엑셀 파일 생성", notes="반입예정 마스터 리스트 엑셀 파일 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/inPlan/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectEcInPlanListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchEc search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsEcService.selectEcInPlanList(search).getEcInPlanMasterList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("EcInPlanMasterList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회", notes="반입예정 상세 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/inPlan/detail/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEcInPlanList> selectEcInPlanDetailList(@QueryStringArgResolver SearchEc search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcInPlanList>(bmsEcService.selectEcInPlanDetailList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회", notes="반입예정 상세 RSG 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/inPlan/detailRsg/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEcInPlanList> selectEcInPlanDetailRsgList(@QueryStringArgResolver SearchEc search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcInPlanList>(bmsEcService.selectEcInPlanDetailRsgList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회", notes="반입 마스터 리스트를 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/in/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEcInList> selectEcInList(@QueryStringArgResolver SearchEc search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcInList>(bmsEcService.selectEcInList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회 엑셀 파일 생성", notes="반입 마스터 리스트 엑셀 파일 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/in/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectEcInListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchEc search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsEcService.selectEcInList(search).getEcInMasterList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("EcInMasterList", map), HttpStatus.OK);
	}

	@ApiOperation(value = "EC송수신조회", notes="반입 상세 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/in/detail/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEcInList> selectEcInDetailList(@QueryStringArgResolver SearchEc search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcInList>(bmsEcService.selectEcInDetailList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회", notes="통관 마스터 리스트를 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/etc/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEcEtcList> selectEcEtcList(@QueryStringArgResolver SearchEc search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcEtcList>(bmsEcService.selectEcEtcList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회 엑셀 파일 생성", notes="통관 마스터 리스트 엑셀 파일 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/etc/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectEcEtcListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchEc search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsEcService.selectEcEtcList(search).getEcEtcMasterList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("EcEtcMasterList", map), HttpStatus.OK);
	}

	@ApiOperation(value = "EC송수신조회", notes="통관 상세 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/etc/detail/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEcEtcList> selectEcEtcDetailList(@QueryStringArgResolver SearchEc search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcEtcList>(bmsEcService.selectEcEtcDetailList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회", notes="반출승인 리스트를 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/outPlan/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEcOutPlanList> selectEcOutPlanList(@QueryStringArgResolver SearchEc search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcOutPlanList>(bmsEcService.selectEcOutPlanList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회 엑셀 파일 생성", notes="반출승인 마스터 리스트 엑셀 파일 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/outPlan/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectEcOutPlanListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchEc search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsEcService.selectEcOutPlanList(search).getEcOutPlanList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("EcOutPlanList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회", notes="반출 마스터 리스트를 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/out/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEcOutList> selectEcOutList(@QueryStringArgResolver SearchEc search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcOutList>(bmsEcService.selectEcOutList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC송수신조회 엑셀 파일 생성", notes="반출 마스터 리스트 엑셀 파일 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/out/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectEcOutListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchEc search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsEcService.selectEcOutList(search).getEcOutMasterList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("EcOutMasterList", map), HttpStatus.OK);
	}

	@ApiOperation(value = "EC송수신조회", notes="반출 상세 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/out/detail/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEcOutList> selectEcOutDetailList(@QueryStringArgResolver SearchEc search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcOutList>(bmsEcService.selectEcOutDetailList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EC 재송신", notes = "EC 데이터 재송신합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/exec/send/ecList", method = RequestMethod.POST)
	public ResponseEntity<BmsEcSendResult> resendEcData(@PathVariable("entCode") String entCode, @RequestBody BmsEcSend params, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsEcSendResult>(bmsEcService.resendEcData(params, request), HttpStatus.OK);
	}
	
}
