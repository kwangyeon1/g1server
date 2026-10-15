package org.yht.exps.express.controller.eCommerce;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.eCommerce.epn.Epn;
import org.yht.exps.express.model.eCommerce.epn.EpnList;
import org.yht.exps.express.model.eCommerce.epn.search.SearchEpnBatchUpdate;
import org.yht.exps.express.model.eCommerce.epn.search.SearchEpnList;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadList;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.service.eCommerce.EpnService;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/epn")
public class EpnController {
	
	@Resource(name = "epnService")
	EpnService epnService;
	
	
	@ApiOperation(value = "전자상거래 EPN 조회", notes="EPN 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<EpnList> selectEpnList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchEpnList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<EpnList>(epnService.selectEpnList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 EPN 리스트 엑셀파일생성", notes="전자상거래 EPN 리스트 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectEpnListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchEpnList search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", epnService.selectEpnList(search).getEpnList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("EPN", map), HttpStatus.OK);
	}
	
    @ApiOperation(value = "적하목록 통관정보 이관 카운트", notes="전자상거래 EPN을 저장합니다.")
    @RequestMapping(value="/batch/targetCount", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> manifestLicenseTargetCount(@PathVariable("entCode") String entCode, @RequestBody SearchEpnBatchUpdate search) throws Exception {
        search.setEntCode(entCode);
        return new ResponseEntity<OrderSaveResult>(epnService.manifestLicenseTargetCount(search), HttpStatus.OK);
    }

    @RequestMapping(value="/manifestLicense/Update", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> manifestLicenseUpdate(@PathVariable("entCode") String entCode, @RequestBody SearchEpnBatchUpdate search) throws Exception {
        search.setEntCode(entCode);
        return new ResponseEntity<OrderSaveResult>(epnService.manifestLicenseUpdate(search), HttpStatus.OK);
    }
    
	@ApiOperation(value = "전자상거래 EPN 저장", notes="전자상거래 EPN을 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertEpn(@PathVariable("entCode") String entCode, @RequestBody Epn epn) throws Exception {
		epn.setEntCode(entCode);
		return new ResponseEntity<OrderSaveResult>(epnService.saveEpn(epn), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 EPN 저장", notes="전자상거래 EPN을 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batchInsert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertEpnBatch(@PathVariable("entCode") String entCode, @RequestBody EpnList epnList) throws Exception {
		return new ResponseEntity<OrderSaveResult>(epnService.insertEpnBatch(epnList), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 EPN update", notes="EPN을 update합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "epnJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/update/{epnJobNo}", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateOrder(@PathVariable("entCode") String entCode, @PathVariable("epnJobNo") String epnJobNo, @RequestBody Epn epn) throws Exception {
		epn.setEntCode(entCode);
		epn.setEpnJobNo(epnJobNo);
		return new ResponseEntity<OrderSaveResult>(epnService.saveEpn(epn), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 EPN 삭제", notes="EPN을 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteEpnList(@PathVariable("entCode") String entCode, @RequestBody Epn epn) throws Exception {
		epn.setEntCode(entCode);
		return new ResponseEntity<OrderSaveResult>(epnService.deleteEpnList(epn), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 EPN 조회", notes="전자상거래 EPN을 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "epnJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/get/{epnJobNo}", method = RequestMethod.GET)
	public ResponseEntity<Epn> selectEpn(@PathVariable("entCode") String entCode, @PathVariable("epnJobNo") String epnJobNo, Epn epn) throws Exception {
		epn.setEntCode(entCode);
		epn.setEpnJobNo(epnJobNo);
		return new ResponseEntity<Epn>(epnService.selectEpn(epn), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 EPN 엑셀 파일 READ", notes="전자상거래 EPN 엑셀 파일을 읽어옵니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "excelUploadType", value = "엑셀타입", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/read/excel/{excelUploadType}", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> readExcelEpn(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
		return new ResponseEntity<OrderSaveResult>(epnService.readExcelEpn(uploadExcel, entCode, excelUploadType), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 EPN 엑셀 업로드", notes="전자상거래 EPN 엑셀을 업로드합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert/excel/{excelUploadType}", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertExcelEpn(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestBody ExcelUploadList excelUploadList, HttpServletRequest request) throws Exception {
			return new ResponseEntity<OrderSaveResult>(epnService.insertExcelEpn(excelUploadList, excelUploadType, request), HttpStatus.OK);
	}
	
}
