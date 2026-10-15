package org.yht.exps.express.controller.selectMng;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.selectMng.DeleteSelfSelection;
import org.yht.exps.express.model.selectMng.GoodsSelectionInfo;
import org.yht.exps.express.model.selectMng.GoodsSelectionInfoList;
import org.yht.exps.express.model.selectMng.GoodsSelectionMailList;
import org.yht.exps.express.model.selectMng.SaveGoodsSelectionInfo;
import org.yht.exps.express.model.selectMng.SelectionItemList;
import org.yht.exps.express.model.selectMng.SelectionList;
import org.yht.exps.express.model.selectMng.SelfSelection;
import org.yht.exps.express.model.selectMng.SelfSelectionList;
import org.yht.exps.express.model.selectMng.UpdateSelectionList;
import org.yht.exps.express.model.selectMng.search.SearchGoodsSelectionInfo;
import org.yht.exps.express.model.selectMng.search.SearchSelectionItemList;
import org.yht.exps.express.model.selectMng.search.SearchSelectionList;
import org.yht.exps.express.model.selectMng.search.SearchSelfSelection;
import org.yht.exps.express.model.selectMng.search.SearchSelfSelectionList;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.bms.UpsNewbillingService;
import org.yht.exps.express.service.selectMng.SelectMngService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "SelectMng")
@RequestMapping("/{entCode}/ecm/selectMng")
public class SelectMngController {
	
	@Resource(name = "selectMngService")
	SelectMngService selectMngService;
	@Resource(name = "fileService")
	FileService fileService;
	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name = "upsNewbillingService")
    UpsNewbillingService upsNewbillingService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "목록선별 리스트 조회", notes="목록선별 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<SelectionList> selectSelectionList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchSelectionList search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfo(req, jwtTokenProvider, search, "userId", "logUserName", null, "logUserNameEng", true, false, false, false);
		return new ResponseEntity<SelectionList>(selectMngService.selectSelectionList(search, req), HttpStatus.OK);
	}
	@ApiOperation(value = "목록선별 리스트 엑셀파일생성", notes="목록선별 리스트 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectSelectionListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchSelectionList search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfo(req, jwtTokenProvider, search, "userId", "logUserName", null, "logUserNameEng", true, false, false, false);
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", selectMngService.selectSelectionList(search, req).getSelectionList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Selection_List", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "목록선별 리스트 세부품목 조회", notes="목록선별 세부품목 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/item/search/list", method = RequestMethod.GET)
	public ResponseEntity<SelectionItemList> selectSelectionItemList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchSelectionItemList search) throws Exception {
		return new ResponseEntity<SelectionItemList>(selectMngService.selectSelectionItemList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "목록선별 리스트 수정", notes="목록선별 리스트를 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selection/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateSelectionList(@PathVariable("entCode") String entCode, @RequestBody UpdateSelectionList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
		OrderSaveResult orderSaveResult = selectMngService.updateSelectionList(search, request);
		if("updateBmsCdType".equals(orderSaveResult.getResultCode())){
			List<Map<String, Object>> updateCdTypeList = (List<Map<String, Object>>)orderSaveResult.getResultData().get("updateCdTypeList");
			commonService.insertBmsCargoProgress(updateCdTypeList);
			orderSaveResult.setResultCode(null);
			orderSaveResult.setResultData(null);
		}
		
//		String profile = System.getProperty("spring.profiles.active");
//		boolean newbillingFlag = false;
//		if((("ups1".equals(profile) && "UPSXK".equals(entCode)) || "local".equals(profile)) && !"Y".equals(search.getAutoSelectionYn()) && "Y".equals(search.getSelectionYn())){
//			newbillingFlag = true;
//		}
//		
//		if(newbillingFlag && Util.isEmpty(orderSaveResult.getResultCode())){
//	    	try{
//	    		UpsNewbilling newbilling = new UpsNewbilling();
//	    		newbilling.setCryptographyYn(search.getCryptographyYn());
//	    		newbilling.setCdType("P");
//	    		newbilling.setMasterOdJobNo(search.getMasterOdJobNo());
//	    		upsNewbillingService.selectManifestToNewbillingCustomsTransfer(newbilling);
//	    		
//	    		Util.setDB(request, "NEWBILLING");
//	    		return new ResponseEntity<>(upsNewbillingService.manifestToNewbillingCustomsTransfer(newbilling), HttpStatus.OK);
//	    	}catch(Exception e){
//				return new ResponseEntity<>(upsNewbillingService.setNewbillingError(), HttpStatus.OK);
//	    	}
//		}
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
	
	@ApiOperation(value = "목록선별 리스트 제출번호 수정", notes="목록선별 리스트를 제출번호수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selection/simpleDocNoUpdate", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateSelectionSimpleDocNo(@PathVariable("entCode") String entCode, @RequestBody UpdateSelectionList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
		OrderSaveResult orderSaveResult = selectMngService.updateSelectionSimpleDocNo(search, request);
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
	
	@ApiOperation(value = "목록선별 임시저장 수정", notes="목록선별 임시저장을 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/tempSelection/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateTempSelectionList(@PathVariable("entCode") String entCode, @RequestBody UpdateSelectionList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(selectMngService.updateTempSelectionList(search, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "목록선별품명 임시저장 수정", notes="목록선별품명 임시저장을 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/tempSelectionItem/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateTempSelectionItemList(@PathVariable("entCode") String entCode, @RequestBody UpdateSelectionList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(selectMngService.updateTempSelectionItemList(search, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "검사선별관리 리스트 조회", notes="검사선별관리 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inspection/search/list", method = RequestMethod.GET)
	public ResponseEntity<SelectionList> selectInspectionList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchSelectionList search) throws Exception {
		return new ResponseEntity<SelectionList>(selectMngService.selectInspectionList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "검사선별관리 리스트 수정", notes="검사선별관리 리스트를 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inspection/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateInspectionList(@PathVariable("entCode") String entCode, @RequestBody SaveGoodsSelectionInfo selection, HttpServletRequest request) throws Exception {
		return new ResponseEntity<OrderSaveResult>(selectMngService.updateInspectionList(selection, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자체선별(기초) 리스트 조회", notes="통관목록 자체선별(기초) 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selfSelection/search/list", method = RequestMethod.GET)
	public ResponseEntity<SelfSelectionList> selectSelfSelectionList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchSelfSelectionList search) throws Exception {
		return new ResponseEntity<SelfSelectionList>(selectMngService.selectSelfSelectionList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "통관목록 자체선별(기초) 리스트 엑셀파일생성", notes="통관목록 자체선별(기초) 리스트 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/selfSelection/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectSelfSelectionListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchSelfSelectionList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", selectMngService.selectSelfSelectionList(search).getSelfSelectionList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("selfSelection", map, "xlsx"), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자체선별(기초) 조회", notes="통관목록 자체선별(기초)를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "jobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/selfSelection/get/{jobNo}", method = RequestMethod.GET)
	public ResponseEntity<SelfSelection> selectSelfSelection(@PathVariable("entCode") String entCode, @PathVariable("jobNo") String jobNo, SearchSelfSelection search) throws Exception {
		return new ResponseEntity<SelfSelection>(selectMngService.selectSelfSelection(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자체선별(기초) 저장", notes="통관목록 자체선별(기초)를 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selfSelection/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertSelfSelection(@PathVariable("entCode") String entCode, @RequestBody SelfSelection selfSelection, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, selfSelection, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(selectMngService.insertSelfSelection(selfSelection), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자체선별(기초) 수정", notes="통관목록 자체선별(기초)를 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selfSelection/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateSelfSelection(@PathVariable("entCode") String entCode, @RequestBody SelfSelection selfSelection, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, selfSelection, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(selectMngService.updateSelfSelection(selfSelection, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자체선별(기초) 삭제", notes="통관목록 자체선별(기초)를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selfSelection/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteSelfSelection(@PathVariable("entCode") String entCode, @RequestBody DeleteSelfSelection delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, delete, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(selectMngService.deleteSelfSelection(delete, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자체선별(기초) 저장(엑셀업로드)", notes="통관목록 자체선별(기초)를 저장합니다.(엑셀업로드)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "updateUserId", value = "수정자", required = true, dataType = "string", paramType = "path", defaultValue = "201909-007"),
		@ApiImplicitParam(name = "gmtTime", value = "GMT", required = true, dataType = "string", paramType = "path", defaultValue = "0")
	})
	@RequestMapping(value="/selfSelection/excelUpload/{updateUserId}/{gmtTime}/{userBusinessCode}", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> selfSelectionExcelUpload(HttpServletRequest request
			, @PathVariable("entCode") String entCode
			, @PathVariable("updateUserId") String updateUserId
			, @PathVariable("gmtTime") String gmtTime
			, @PathVariable("userBusinessCode") String userBusinessCode) throws Exception {
		updateUserId = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"), updateUserId);
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
		MultipartFile file = multipartRequest.getFile("excelUpload_file");
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(file, new String[]{"xlsx", "xls"})) {
			OrderSaveResult orderSaveResult = new OrderSaveResult();
			orderSaveResult.setResultCode("extError");
			return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
		}
		
		FileUploadResponse result = fileService.storeFile(file, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")){
			return new ResponseEntity<OrderSaveResult>(selectMngService.selfSelectionExcelUpload(entCode, updateUserId, gmtTime, fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile(), userBusinessCode, request), HttpStatus.OK);
		}else {
			return new ResponseEntity<OrderSaveResult>(selectMngService.selfSelectionExcelUpload(entCode, updateUserId, gmtTime,  null, null, null), HttpStatus.OK);
		}
	}
	
	@ApiOperation(value = "통관목록 자동선별(기초) 리스트 조회", notes="통관목록 자동선별(기초) 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/goodsSelectionInfo/search/list", method = RequestMethod.GET)
	public ResponseEntity<GoodsSelectionInfoList> selectGoodsSelectionInfoList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchGoodsSelectionInfo search) throws Exception {
		return new ResponseEntity<GoodsSelectionInfoList>(selectMngService.selectGoodsSelectionInfoList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "통관목록 자동선별(기초) 리스트 엑셀파일생성", notes="통관목록 자동선별(기초) 리스트 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/goodsSelectionInfo/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectGoodsSelectionInfoListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchGoodsSelectionInfo search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", selectMngService.selectGoodsSelectionInfoList(search).getGoodsSelectionInfoList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("goodsSelection", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자동선별(기초) 조회", notes="통관목록 자동선별(기초)를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "itemName", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/goodsSelectionInfo/get/{itemName}", method = RequestMethod.GET)
	public ResponseEntity<GoodsSelectionInfo> selectGoodsSelectionInfo(@PathVariable("entCode") String entCode, @PathVariable("itemName") String itemName) throws Exception {
		return new ResponseEntity<GoodsSelectionInfo>(selectMngService.selectGoodsSelectionInfo(entCode, itemName), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자동선별(기초) 저장", notes="통관목록 자동선별(기초)를 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/goodsSelectionInfo/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertGoodsSelectionInfo(@PathVariable("entCode") String entCode, @RequestBody SaveGoodsSelectionInfo goodsSelectionInfo, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, goodsSelectionInfo, "updateUserId", "logUserName", null, "logUserNameEng");
		List<GoodsSelectionInfo> goodsSelectionInfoList = goodsSelectionInfo.getGoodsSelectionInfoList();
		if(goodsSelectionInfoList != null){
			for(GoodsSelectionInfo data : goodsSelectionInfoList){
				data.setUpdateUserId(goodsSelectionInfo.getUpdateUserId());
			}
		}
		return new ResponseEntity<OrderSaveResult>(selectMngService.insertGoodsSelectionInfo(goodsSelectionInfo), HttpStatus.OK);
	}
	@ApiOperation(value = "통관목록 자동선별(기초) 저장(Backup File 업데이트)", notes="통관목록 자동선별(기초)를 저장합니다.(Backup File 업데이트)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/goodsSelectionInfoBackupFile/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertGoodsSelectionInfoBackupFile(@PathVariable("entCode") String entCode, @RequestBody SaveGoodsSelectionInfo goodsSelectionInfo, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, goodsSelectionInfo, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(selectMngService.insertGoodsSelectionInfoBackupFile(goodsSelectionInfo), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자동선별(기초) 수정", notes="통관목록 자동선별(기초)를 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/goodsSelectionInfo/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateGoodsSelectionInfo(@PathVariable("entCode") String entCode, @RequestBody SaveGoodsSelectionInfo goodsSelectionInfo, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, goodsSelectionInfo, "updateUserId", "logUserName", null, "logUserNameEng");
		List<GoodsSelectionInfo> goodsSelectionInfoList = goodsSelectionInfo.getGoodsSelectionInfoList();
		if(goodsSelectionInfoList != null){
			for(GoodsSelectionInfo data : goodsSelectionInfoList){
				data.setUpdateUserId(goodsSelectionInfo.getUpdateUserId());
			}
		}
		return new ResponseEntity<OrderSaveResult>(selectMngService.updateGoodsSelectionInfo(goodsSelectionInfo, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자동선별(기초) 삭제", notes="통관목록 자동선별(기초)를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/goodsSelectionInfo/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteGoodsSelectionInfo(@PathVariable("entCode") String entCode, @RequestBody SaveGoodsSelectionInfo goodsSelectionInfo, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, goodsSelectionInfo, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(selectMngService.deleteGoodsSelectionInfo(goodsSelectionInfo, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자동선별(기초) 저장(엑셀업로드)", notes="통관목록 자동선별(기초)를 저장합니다.(엑셀업로드)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "updateUserId", value = "수정자", required = true, dataType = "string", paramType = "path", defaultValue = "201909-007"),
		@ApiImplicitParam(name = "gmtTime", value = "GMT", required = true, dataType = "string", paramType = "path", defaultValue = "0")
	})
	@RequestMapping(value="/goodsSelectionInfo/excelUpload/{updateUserId}/{gmtTime}/{userBusinessCode}", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> goodsSelectionInfoExcelUpload(HttpServletRequest request
			, @PathVariable("entCode") String entCode
			, @PathVariable("updateUserId") String updateUserId
			, @PathVariable("gmtTime") String gmtTime
			, @PathVariable("userBusinessCode") String userBusinessCode) throws Exception {
		updateUserId = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"), updateUserId); 
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
		MultipartFile file = multipartRequest.getFile("excelUpload_file");
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(file, new String[]{"xlsx", "xls"})) {
			OrderSaveResult orderSaveResult = new OrderSaveResult();
			orderSaveResult.setResultCode("extError");
			return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
		}
		
		FileUploadResponse result = fileService.storeFile(file, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")){
			return new ResponseEntity<OrderSaveResult>(selectMngService.goodsSelectionInfoExcelUpload(entCode, updateUserId, gmtTime, fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile(), userBusinessCode, request), HttpStatus.OK);
		}else {
			return new ResponseEntity<OrderSaveResult>(selectMngService.goodsSelectionInfoExcelUpload(entCode, updateUserId, gmtTime,  null, null, null), HttpStatus.OK);
		}
	}
	
	@ApiOperation(value = "통관목록 자동선별 선별마감 여부 조회", notes="통관목록 자동선별 선별마감 여부를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/check/selectionYn", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> checkSelectionYn(@PathVariable("entCode") String entCode, @RequestBody UpdateSelectionList search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<OrderSaveResult>(selectMngService.checkSelectionYn(search, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자동선별 선별마감 E-Mail 전송 리스트 조회", notes="통관목록 자동선별 선별마감 E-Mail 전송할 리스트를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/goodsSelectionInfo/search/list/mail", method = RequestMethod.GET)
	public ResponseEntity<GoodsSelectionMailList> selectGoodsSelectionMailList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchSelectionList search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<GoodsSelectionMailList>(selectMngService.selectGoodsSelectionMailList(search, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록 자동선별 선별마감 E-Mail 전송", notes="통관목록 자동선별 선별마감 E-Mail을 전송합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/goodsSelectionInfo/send/mail", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendGoodsSelectionMail(@PathVariable("entCode") String entCode, @RequestBody GoodsSelectionMailList search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<OrderSaveResult>(selectMngService.sendGoodsSelectionMail(search, request), HttpStatus.OK);
	}
}
