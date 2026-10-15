package org.yht.exps.express.controller.edi;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChgDoc;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChgList;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChgSaveResult;
import org.yht.exps.express.model.edi.deliveryChg.search.SearchDeliveryChgList;
import org.yht.exps.express.service.edi.DeliveryChgService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "EDI")
@RequestMapping("/{entCode}/ecm/edi/deliveryChg")
public class DeliveryChgController {
	
	@Resource(name = "deliveryChgService")
	DeliveryChgService deliveryChgService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	
	@ApiOperation(value = "ECM 배송지변경신고 리스트 조회", notes="ECM 배송지변경신고 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/doc/search/list", method = RequestMethod.GET)
	public ResponseEntity<DeliveryChgList> selectDeliveryDocList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchDeliveryChgList search) throws Exception {
		return new ResponseEntity<DeliveryChgList>(deliveryChgService.selectDeliveryDocList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록 수입 하우스 리스트 조회", notes="ECM 적하목록 수입 하우스 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<DeliveryChgList> selectDeliveryList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchDeliveryChgList search) throws Exception {
		return new ResponseEntity<DeliveryChgList>(deliveryChgService.selectDeliveryList(search), HttpStatus.OK);
	}
		
	@ApiOperation(value = "배송지 변경 신고 신고탭 리스트 엑셀파일생성", notes="배송지 변경 신고 신고탭 리스트 엑셀파일생성")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/declare/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectDeliveryListDeclareExcel(@PathVariable("entCode") String entCode, @RequestBody SearchDeliveryChgList search, HttpServletRequest req) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", deliveryChgService.selectDeliveryList(search).getDeliveryChgList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("delivery_chg_declare", map), HttpStatus.OK);
	}	

	@ApiOperation(value = "ECM 적하목록 수입 하우스 리스트 엑셀파일생성", notes="ECM 적하목록 수입 하우스 리스트 엑셀파일생성")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectDeliveryListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchDeliveryChgList search, HttpServletRequest req) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		String fileName = "";
		if("delivery".equals(Util.null2str(search.getExcelType()))) {
			fileName = "delivery_chg_List";
			
			String[] columnsArr = {"mblNo","hblNo","deliveryDateExcel","deliveryNameChanged","deliveryAddressChanged","deliveryReasonChanged"};
			String[] columnsWidthArr = {"90","90","220","70","170","130"};
			Integer[] numFormatsArr = {0,-1,-1,-1,-1,-1};
			String[] excelTypesArr = {"center","center","center","center","center","center"};
			String[] titlesArr = {"MBLNo","HBLNo","배송일자(셀서식 텍스트 형식)","배송업체","실제배송 수하인주소","사유"};
			
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
		}else {
			fileName = "delivery_chg";
		}
		map.put("data", deliveryChgService.selectDeliveryList(search).getDeliveryChgList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(fileName, map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "배송지변경신고 insert", notes="배송지변경신고 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<DeliveryChgSaveResult> insertDeliveryChg(@PathVariable("entCode") String entCode, @RequestBody SearchDeliveryChgList deliveryChgDoc, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, deliveryChgDoc, "userId", "logUserName", null, "logUserNameEng", true, false, false, false);
		return new ResponseEntity<DeliveryChgSaveResult>(deliveryChgService.insertDeliveryChg(deliveryChgDoc), HttpStatus.OK);
	}
	
	@ApiOperation(value = "배송지변경신고 카운트", notes="배송지변경신고 카운트")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert/targetCount", method = RequestMethod.POST)
	public ResponseEntity<DeliveryChgSaveResult> insertDeliveryChgTargetCount(@PathVariable("entCode") String entCode, @RequestBody SearchDeliveryChgList search) throws Exception {
		DeliveryChgSaveResult deliveryChgSaveResult = new DeliveryChgSaveResult();
		deliveryChgSaveResult.setResultCode(Util.null2str(deliveryChgService.selectDeliveryListTargetCount(search)));
		return new ResponseEntity<DeliveryChgSaveResult>(deliveryChgSaveResult, HttpStatus.OK);
	}
	
	@ApiOperation(value = "배송지변경신고 일괄변경", notes="배송지변경신고일괄변경합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batch/Update", method = RequestMethod.PUT)
	public ResponseEntity<DeliveryChgSaveResult> updateBatchDeliveryChg(@PathVariable("entCode") String entCode, @RequestBody DeliveryChgDoc deliveryChgDoc, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, deliveryChgDoc, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<DeliveryChgSaveResult>(deliveryChgService.updateBatchDeliveryChg(deliveryChgDoc, request), HttpStatus.OK);
	}

	@ApiOperation(value = "배송지변경신고 삭제", notes="배송지변경신고일괄삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batch/delete", method = RequestMethod.DELETE)
	public ResponseEntity<DeliveryChgSaveResult> deleteBatchDeliveryChg(@PathVariable("entCode") String entCode, @RequestBody DeliveryChgDoc deliveryChgDoc, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, deliveryChgDoc, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<DeliveryChgSaveResult>(deliveryChgService.deleteBatchDeliveryChg(deliveryChgDoc), HttpStatus.OK);
	}
	
	@ApiOperation(value = "배송지변경신고 결과조회", notes="배송지변경신고일괄삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/result/list", method = RequestMethod.GET)
	public ResponseEntity<DeliveryChgList> selectDeliveryChgResult(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchDeliveryChgList searchDeliveryChgList) throws Exception {
		if ("".equals(Util.null2str(searchDeliveryChgList.getSrchEntCode())))	searchDeliveryChgList.setSrchEntCode(entCode);
		return new ResponseEntity<DeliveryChgList>(deliveryChgService.selectDeliveryChgResult(searchDeliveryChgList), HttpStatus.OK);
	}
	
	@ApiOperation(value = "배송지변경신고 결과조회 엑셀 다운로드", notes="배송지변경신고 결과조회 엑셀 다운로드합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/result/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectDeliveryChgResultExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchDeliveryChgList searchDeliveryChgList) throws Exception {
		if ("".equals(Util.null2str(searchDeliveryChgList.getSrchEntCode())))	searchDeliveryChgList.setSrchEntCode(entCode);
		
		searchDeliveryChgList.setPage(0);
		DeliveryChgList list = deliveryChgService.selectDeliveryChgResult(searchDeliveryChgList);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(searchDeliveryChgList, Map.class);
		map.put("data", list.getDeliveryChgResultList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("changeDeliveryResult_list", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "배송지변경신고 ExcelUpload", notes="배송지변경신고 ExcelUpload합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload/check", method = RequestMethod.POST)
	public ResponseEntity<DeliveryChgSaveResult> excelUploadCheck(@PathVariable("entCode") String entCode, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
		return new ResponseEntity<DeliveryChgSaveResult>(deliveryChgService.excelUploadCheck(uploadExcel, entCode), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 배송지변경신고 Excel 저장", notes="ECM 배송지변경신고 Excel 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload", method = RequestMethod.POST)
	public ResponseEntity<DeliveryChgSaveResult> excelUpload(@PathVariable("entCode") String entCode, @RequestBody DeliveryChgDoc deliveryChgDoc, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, deliveryChgDoc, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<DeliveryChgSaveResult>(deliveryChgService.excelUpload(deliveryChgDoc), HttpStatus.OK);
	}
	
	@ApiOperation(value = "배송지변경신고 과태료검증", notes="배송지변경신고 과태료검증 적용합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/penaltyVali/update", method = RequestMethod.POST)
	public ResponseEntity<DeliveryChgSaveResult> penaltyVali(@PathVariable("entCode") String entCode, @RequestBody SearchDeliveryChgList searchDeliveryChgList, HttpServletRequest request) throws Exception {
		return new ResponseEntity<DeliveryChgSaveResult>(deliveryChgService.penaltyVali(searchDeliveryChgList, request), HttpStatus.OK);
	}
	
}
