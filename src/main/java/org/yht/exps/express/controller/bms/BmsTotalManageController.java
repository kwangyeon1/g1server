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
import org.yht.exps.express.model.bms.bmsTotalManage.BmsTotalManageList;
import org.yht.exps.express.model.bms.bmsTotalManage.search.SearchTotalManage;
import org.yht.exps.express.service.bms.BmsTotalManageService;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "bms")
@RequestMapping("/{entCode}/bms/bmsTotalManage")
public class BmsTotalManageController {

	@Resource(name = "bmsTotalManageService")
	BmsTotalManageService bmsTotalManageService;

	@ApiOperation(value = "통합 관리", notes="통합 관리 마스터를 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/master/list", method = RequestMethod.GET)
	public ResponseEntity<BmsTotalManageList> selectTotalManageList(@QueryStringArgResolver SearchTotalManage search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsTotalManageList>(bmsTotalManageService.selectTotalManageList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "통합 관리", notes="통합 관리 하우스를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/house/list", method = RequestMethod.GET)
	public ResponseEntity<BmsTotalManageList> selectTotalManageDetailList(@QueryStringArgResolver SearchTotalManage search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<BmsTotalManageList>(bmsTotalManageService.selectTotalManageDetailList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "통합관리 마스터 리스트 엑셀 파일 생성", notes="통합관리 마스터 리스트 엑셀 파일 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/master/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectTotalManageListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchTotalManage search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsTotalManageService.selectTotalManageList(search).getTotalManageMasterList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("TotalManageMasterList", map), HttpStatus.OK);
	}

	@ApiOperation(value = "통합관리 하우스 리스트 엑셀 파일 생성", notes="통합관리 하우스 리스트 엑셀 파일 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/house/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectTotalManageDetailListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchTotalManage search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsTotalManageService.selectTotalManageDetailList(search).getTotalManageHouseList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("TotalManageHouseList", map), HttpStatus.OK);
	}

	@ApiOperation(value = "통합 관리", notes="통합 관리 - 입항화물도착 조회")
	@RequestMapping(value="/get/cargoReceive/list", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> selectCargoReceiveList(@QueryStringArgResolver Map<String, Object> search) throws Exception {
		return new ResponseEntity<Map<String, Object>>(bmsTotalManageService.selectCargoReceiveList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "통합 관리", notes="통합 관리 - 택배 인터페이스 차수 조회")
	@RequestMapping(value="/get/delivery/count", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> selectDeliveryCount(@QueryStringArgResolver Map<String, Object> search) throws Exception {
		return new ResponseEntity<Map<String, Object>>(bmsTotalManageService.selectDeliveryCount(search), HttpStatus.OK);
	}

	@ApiOperation(value = "통합관리 택배 인터페이스 엑셀 파일 생성", notes="통합관리 택배 인터페이스 엑셀 파일 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/delivery/list", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> selectDeliveryList(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search) throws Exception {
		return new ResponseEntity<Map<String, Object>>(bmsTotalManageService.selectDeliveryListExcel(search), HttpStatus.OK);
	}

	@ApiOperation(value = "통합관리 택배 인터페이스 엑셀 파일 생성", notes="통합관리 택배 인터페이스 엑셀 파일 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/delivery/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectDeliveryListExcel(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("mainTitle", search.get("excelTitle"));
		map.put("data", search.get("dataList"));
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("deliveryList", map), HttpStatus.OK);
	}
}
