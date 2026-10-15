package org.yht.exps.express.controller.bms;

import java.util.List;
import java.util.Map;

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
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.edi.SendDelivery;
import org.yht.exps.express.model.edi.abnormal.Abnormal;
import org.yht.exps.express.model.edi.abnormal.AbnormalList;
import org.yht.exps.express.model.edi.abnormal.AbnormalSaveResult;
import org.yht.exps.express.model.edi.abnormal.search.SearchAbnormalList;
import org.yht.exps.express.service.bms.BmsCargoAbnormalService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "EDI")
@RequestMapping("/{entCode}/bms/edi/abnormal")
public class BmsCargoAbnormalController {
	
	@Autowired
	BmsCargoAbnormalService bmsCargoAbnormalService;	
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
		
	@ApiOperation(value = "ECM 하기이상결과보고신고 리스트 조회 ", notes="ECM 하기이상결과보고신고 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<AbnormalList> selectAbnormalList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAbnormalList search) throws Exception {
		return new ResponseEntity<AbnormalList>(bmsCargoAbnormalService.selectAbnormalList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 하기이상결과보고신고 엔트리 리스트 조회 ", notes="ECM 하기이상결과보고신고 엔트리 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/entry/search/list", method = RequestMethod.GET)
	public ResponseEntity<AbnormalList> selectAbnormalEntryList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAbnormalList search) throws Exception {
		return new ResponseEntity<AbnormalList>(bmsCargoAbnormalService.selectAbnormalEntryList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 하기이상결과보고신고된 껀 리스트 조회", notes="ECM 하기이상결과보고신고 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/declare/list", method = RequestMethod.GET)
	public ResponseEntity<AbnormalList> selectAbnormalDeclareList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAbnormalList search) throws Exception {
		return new ResponseEntity<AbnormalList>(bmsCargoAbnormalService.selectAbnormalDeclareList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 하기이상결과보고 추가팝업 리스트 조회", notes="ECM 하기이상결과보고 추가팝업 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/addPopup/list", method = RequestMethod.GET)
	public ResponseEntity<AbnormalList> selectAbnormalAddPopupList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAbnormalList search) throws Exception {
		return new ResponseEntity<AbnormalList>(bmsCargoAbnormalService.selectAbnormalAddPopupList(search), HttpStatus.OK);
	}
		
	@ApiOperation(value = "하기이상결과보고 insert", notes="하기이상결과보고 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batch/insert", method = RequestMethod.POST)
	public ResponseEntity<AbnormalSaveResult> insertAbnormalList(@PathVariable("entCode") String entCode, @RequestBody AbnormalList abnormalList) throws Exception {
		return new ResponseEntity<AbnormalSaveResult>(bmsCargoAbnormalService.insertAbnormalList(abnormalList), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 하기이상결과보고신고된 건 delete", notes="ECM 하기이상결과보고신고 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batch/declare/delete", method = RequestMethod.DELETE)
	public ResponseEntity<AbnormalSaveResult> deleteAbnormalDeclareList(@PathVariable("entCode") String entCode, @RequestBody AbnormalList abnormalList) throws Exception {
		return new ResponseEntity<AbnormalSaveResult>(bmsCargoAbnormalService.deleteAbnormalDeclareList(abnormalList), HttpStatus.OK);
	}
		
	@ApiOperation(value = "하기이상결과보고신고 ExcelUpload 유효성체크 ", notes="하기이상결과보고신고 ExcelUpload 유효성체크.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload/check", method = RequestMethod.POST)
	public ResponseEntity<AbnormalSaveResult> excelUploadCheck(@PathVariable("entCode") String entCode, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
		return new ResponseEntity<AbnormalSaveResult>(bmsCargoAbnormalService.excelUploadCheck(uploadExcel, entCode), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 하기이상결과보고신고 Excel 저장", notes="ECM 하기이상결과보고신고 Excel 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload", method = RequestMethod.POST)
	public ResponseEntity<AbnormalSaveResult> excelUpload(@PathVariable("entCode") String entCode, @RequestBody AbnormalList abnormalList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, abnormalList, "registerUserId", null, null);
		return new ResponseEntity<AbnormalSaveResult>(bmsCargoAbnormalService.excelUpload(abnormalList), HttpStatus.OK);
	}
	
	@ApiOperation(value = "하기코드를 존재하는 하우스 건수 조회", notes="하기코드 존재하는 하우스 건수 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/noRegister/count", method = RequestMethod.POST)
	public Map<String, Object> selectNoRegisterList(@PathVariable("entCode") String entCode, @RequestBody Abnormal abnormal, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, abnormal, null, "logUserName", null, "logUserNameEng");
		return bmsCargoAbnormalService.getNoRegisterCount(abnormal, request);
	}
	
	@ApiOperation(value = "하기이상결과보고 무적화물생성", notes="하기이상결과보고 무적화물생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/noRegister/insert", method = RequestMethod.POST)
	public ResponseEntity<AbnormalSaveResult> insertNoRegister(@PathVariable("entCode") String entCode, @RequestBody Abnormal abnormal, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, abnormal, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<AbnormalSaveResult>(bmsCargoAbnormalService.insertNoRegister(abnormal, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "하기이상결과보고 적하정정 insert", notes="하기이상결과보고 적하정정 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batch/ediMod/insert", method = RequestMethod.POST)
	public ResponseEntity<AbnormalSaveResult> insertEdiModList(@PathVariable("entCode") String entCode, @RequestBody AbnormalList abnormalList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, abnormalList, "registerUserId", null, null);
		return new ResponseEntity<AbnormalSaveResult>(bmsCargoAbnormalService.insertEdiModList(abnormalList), HttpStatus.OK);
	}
	
	/**
	 * 엑셀 list
	 * @param entCode
	 * @param functionOrder
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectAbnormalDeclareListExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchAbnormalList search,
						HttpServletRequest request) throws Exception {				 
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsCargoAbnormalService.selectAbnormalDeclareList(search).getAbnormalList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Abnormal_List", map), HttpStatus.OK);
	}

	/**
	 * 하기이상결과보고 적하 삭제 여부 Update
	 * @param entCode
	 * @param abnormalList
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "하기이상결과보고 적하삭제", notes="하기이상결과보고 적하 삭제 여부 Update")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/deleteManifest", method = RequestMethod.POST)
	public ResponseEntity<AbnormalSaveResult> deleteManifest(@PathVariable("entCode") String entCode, @RequestBody AbnormalList abnormalList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, abnormalList, "registerUserId", null, null);
		return new ResponseEntity<AbnormalSaveResult>(bmsCargoAbnormalService.deleteManifest(abnormalList, request), HttpStatus.OK);
	}

	/**
	 * 하기결과이상보고 송신
	 * @param entCode
	 * @param sendDelivery
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "하기결과이상보고", notes="하기결과이상보고를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendAbnormalitiesReport", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendAbnormalitiesReport(@PathVariable("entCode") String entCode, @RequestBody SendDelivery sendDelivery, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendDelivery, "registerUserId", "registerUserName", null);
		return new ResponseEntity<OrderSaveResult>(bmsCargoAbnormalService.sendAbnormalitiesReport(sendDelivery, request), HttpStatus.OK);
	}

	/**
	 * 하기결과이상보고 > 추가 팝업 조회
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "ECM 하기이상결과보고신고 추가 팝업 조회 ", notes="ECM 하기이상결과보고신고 추가 팝업 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/popup/search/list", method = RequestMethod.POST)
	public ResponseEntity<AbnormalList> selectAddPopupSearchList(@PathVariable("entCode") String entCode, @RequestBody AbnormalList abnormalList) throws Exception {
		return new ResponseEntity<AbnormalList>(bmsCargoAbnormalService.selectAddPopupSearchList(abnormalList), HttpStatus.OK);
	}

	/**
	 * 하기결과이상보고 조회 > 삭제 
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "ECM 하기이상결과보고신고 삭제", notes="ECM 하기이상결과보고신고 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/declare/delete", method = RequestMethod.DELETE)
	public ResponseEntity<AbnormalSaveResult> deleteAbnormalDeclare(@PathVariable("entCode") String entCode, @RequestBody AbnormalList abnormalList) throws Exception {
		return new ResponseEntity<AbnormalSaveResult>(bmsCargoAbnormalService.deleteAbnormalDeclare(abnormalList), HttpStatus.OK);
	}

	/**
	 * 하기결과이상보고 결과 조회
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "ECM 하기이상결과보고 결과 리스트 조회", notes="ECM 하기이상결과보고 결과 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/result/list", method = RequestMethod.GET)
	public ResponseEntity<AbnormalList> selectAbnormalResultList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAbnormalList search) throws Exception {
		return new ResponseEntity<AbnormalList>(bmsCargoAbnormalService.selectAbnormalResultList(search), HttpStatus.OK);
	}

	/**
	 * 엑셀 list
	 * @param entCode
	 * @param functionOrder
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/result/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectAbnormalResultListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchAbnormalList search, HttpServletRequest request) throws Exception {				 
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		System.out.println("search : " + search);
		map.put("data", bmsCargoAbnormalService.selectAbnormalResultList(search).getAbnormalList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Abnormal_List", map), HttpStatus.OK);
	}

	/**
	 * 하기결과이상보고 > 무적MFN 팝업 MRN 조회
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "ECM 하기이상결과보고신고 무적MFN 팝업 MRN 조회 ", notes="ECM 하기이상결과보고신고 무적MFN 팝업 리스트 MRN 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/popup/mrn/search/list", method = RequestMethod.GET)
	public ResponseEntity<AbnormalList> selectAddPopupMrnSearchList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAbnormalList search) throws Exception {
		return new ResponseEntity<AbnormalList>(bmsCargoAbnormalService.selectAddPopupMrnSearchList(search), HttpStatus.OK);
	}

	/**
	 * 하기결과이상보고 > 무적MFN 팝업 조회
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "ECM 하기이상결과보고신고 무적MFN 팝업 조회 ", notes="ECM 하기이상결과보고신고 무적MFN 팝업 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/popup/noCargo/search/list", method = RequestMethod.POST)
	public ResponseEntity<AbnormalList> selectAddPopupNoCargoSearchList(@PathVariable("entCode") String entCode, @RequestBody AbnormalList abnormalList) throws Exception {
		return new ResponseEntity<AbnormalList>(bmsCargoAbnormalService.selectAddPopupNoCargoSearchList(abnormalList), HttpStatus.OK);
	}
	
	/**
	 * 하기결과이상보고 > 무적Add HBL 다중입력 조회
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "ECM 하기이상결과보고신고 HBL 다중입력 조회 ", notes="ECM 하기이상결과보고신고 HBL 다중입력을 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/popup/noCargo/search/hblList", method = RequestMethod.POST)
	public ResponseEntity<List<Map<String,Object>>> selectAddPopupNoCargoHblSearchList(@PathVariable("entCode") String entCode, @RequestBody AbnormalList abnormalList) throws Exception {
		return new ResponseEntity<List<Map<String,Object>>>(bmsCargoAbnormalService.selectAddPopupNoCargoHblSearchList(abnormalList), HttpStatus.OK);
	}
}
