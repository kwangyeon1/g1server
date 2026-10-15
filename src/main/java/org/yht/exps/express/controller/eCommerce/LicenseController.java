package org.yht.exps.express.controller.eCommerce;

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
import org.yht.exps.express.model.eCommerce.license.License;
import org.yht.exps.express.model.eCommerce.license.LicenseCommodityItemList;
import org.yht.exps.express.model.eCommerce.license.LicenseDelete;
import org.yht.exps.express.model.eCommerce.license.LicenseGoodsItemList;
import org.yht.exps.express.model.eCommerce.license.LicenseList;
import org.yht.exps.express.model.eCommerce.license.LicenseSaveResult;
import org.yht.exps.express.model.eCommerce.license.search.SearchLicenseBatchUpdate;
import org.yht.exps.express.model.eCommerce.license.search.SearchLicenseList;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.eCommerce.LicenseService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/license")
public class LicenseController {
 
	@Resource(name = "licenseService")
	LicenseService licenseService;
	@Resource(name = "commonService")
	CommonService commonService;	
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "전자상거래 면장 조회", notes="전자상거래 면장을 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<LicenseList> selectLicenseEList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchLicenseList search) throws Exception {
		return new ResponseEntity<LicenseList>(licenseService.selectLicenseEList(search), HttpStatus.OK);
	}	
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "면장 엑셀파일생성 (다운로드)", notes="면장 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectLicenseExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchLicenseList search, HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", licenseService.selectLicenseExcelList(search).getLicenseList());
		
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getMainTitle(), map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 면장 상세 조회", notes="ECM 면장 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/{licenseJobNo}", method = RequestMethod.GET)
	public ResponseEntity<License> selectLicenseDetail(@PathVariable("entCode") String entCode, SearchLicenseList search) throws Exception {
		return new ResponseEntity<License>(licenseService.selectLicenseDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 면장 란 리스트 조회", notes="ECM 면장 란 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/goodsItemList", method = RequestMethod.GET)
	public ResponseEntity<LicenseGoodsItemList> selectLicenseGoodsItemList(@PathVariable("entCode") String entCode, SearchLicenseList search) throws Exception {
		return new ResponseEntity<LicenseGoodsItemList>(licenseService.selectLicenseGoodsItemList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 면장 규격 리스트 조회", notes="ECM 면장 규격 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/commodityItemList", method = RequestMethod.GET)
	public ResponseEntity<LicenseCommodityItemList> selectLicenseCommodityItemList(@PathVariable("entCode") String entCode, SearchLicenseList search) throws Exception {
		return new ResponseEntity<LicenseCommodityItemList>(licenseService.selectLicenseCommodityItemList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 면장 란 - 오더아이템 조회", notes="ECM 면장 란 - 오더아이템을 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/search/goodsItem/od/list", method = RequestMethod.GET)
	public ResponseEntity<LicenseGoodsItemList> selectLicenseGoodsItemOdList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchLicenseList search) throws Exception {
		return new ResponseEntity<LicenseGoodsItemList>(licenseService.selectLicenseGoodsItemOdList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 면장 규격 - 오더아이템 조회", notes="ECM 면장 규격 - 오더아이템을 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/search/commodityItem/od/list", method = RequestMethod.GET)
	public ResponseEntity<LicenseCommodityItemList> selectLicenseCommodityItemOdList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchLicenseList search) throws Exception {
		return new ResponseEntity<LicenseCommodityItemList>(licenseService.selectLicenseCommodityItemOdList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 면장 거래처 조회", notes="ECM 면장 거래처를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/search/customer", method = RequestMethod.GET)
	public ResponseEntity<License> selectLicenseCustomer(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchLicenseList search) throws Exception {
		return new ResponseEntity<License>(licenseService.selectLicenseCustomer(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 면장 EDI 특송부호 조회", notes="ECM 면장 EDI 특송부호 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/search/edi", method = RequestMethod.GET)
	public ResponseEntity<String> selectLicenseEdi(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchLicenseList search) throws Exception {
		return new ResponseEntity<String>(licenseService.selectLicenseEdi(search), HttpStatus.OK);
	}
	
	/**
	 * ECM 면장 Save
	 * @param entCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "ECM 면장 insert", notes="ECM 면장 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<LicenseSaveResult> insertLicense(@PathVariable("entCode") String entCode, @RequestBody License license, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, license, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<LicenseSaveResult>(licenseService.insertLicense(license, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 면장 update", notes="전자상거래 면장 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update", method = RequestMethod.PUT)
	public ResponseEntity<LicenseSaveResult> updateLicense(@PathVariable("entCode") String entCode, @RequestBody License license, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, license, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<LicenseSaveResult>(licenseService.updateLicense(license, request), HttpStatus.OK);
	}
	
	/**
	 * ECM 면장 삭제
	 * @param entCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "ECM 면장 삭제처리", notes="ECM 면장 삭제처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete", method = RequestMethod.DELETE)
	public ResponseEntity<LicenseSaveResult> deleteLicense(@PathVariable("entCode") String entCode, @RequestBody LicenseDelete licenseDelete, HttpServletRequest request) throws Exception {
		licenseDelete.setEntCode(entCode);		
		Util.getSessionUserInfo(request, jwtTokenProvider, licenseDelete, "updateUserId", "logUserName", null, "logUserNameEng");
		licenseDelete.setUserName(licenseDelete.getLogUserName());
		return new ResponseEntity<LicenseSaveResult>(licenseService.deleteLicense(licenseDelete, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM X면장 ExcelUpload 유효성체크 ", notes="ECM 면장 ExcelUpload 유효성체크.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload/check/x", method = RequestMethod.POST)
	public ResponseEntity<LicenseSaveResult> excelUploadCheckX(@PathVariable("entCode") String entCode, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
		return new ResponseEntity<LicenseSaveResult>(licenseService.excelUploadCheckX(uploadExcel, entCode), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM E면장 ExcelUpload 유효성체크 ", notes="ECM 면장 ExcelUpload 유효성체크.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload/check/e", method = RequestMethod.POST)
	public ResponseEntity<LicenseSaveResult> excelUploadCheckE(@PathVariable("entCode") String entCode, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
		return new ResponseEntity<LicenseSaveResult>(licenseService.excelUploadCheckE(uploadExcel, entCode), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 면장 Excel 저장", notes="ECM 면장 Excel 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload", method = RequestMethod.POST)
	public ResponseEntity<LicenseSaveResult> excelUpload(@PathVariable("entCode") String entCode, @RequestBody LicenseList licenseList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, licenseList, "registerUserId", null, null);
		return new ResponseEntity<LicenseSaveResult>(licenseService.excelUpload(licenseList), HttpStatus.OK);
	}

	
	@ApiOperation(value = "KTNET X면장 전송", notes="KTNET X면장 전송")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/send/xLicense", method = RequestMethod.POST)
	public ResponseEntity<LicenseSaveResult> sendXLicense(@PathVariable("entCode") String entCode, @RequestBody SearchLicenseBatchUpdate search, HttpServletRequest request) throws Exception {
		search.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", null, null);
		return new ResponseEntity<LicenseSaveResult>(licenseService.sendXLicense(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "KTNET E면장 전송", notes="KTNET E면장 전송")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/send/eLicense", method = RequestMethod.POST)
	public ResponseEntity<LicenseSaveResult> sendELicenseCreate(@PathVariable("entCode") String entCode, @RequestBody SearchLicenseBatchUpdate search, HttpServletRequest request) throws Exception {
		search.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", null, null);
		return new ResponseEntity<LicenseSaveResult>(licenseService.sendELicenseCreate(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "KTNET X면장 전송건수조회", notes="KTNET X면장 전송건수조회")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/search/cnt", method = RequestMethod.POST)
	public ResponseEntity<LicenseSaveResult> selectLienseCount(@PathVariable("entCode") String entCode, @RequestBody SearchLicenseBatchUpdate search) throws Exception {
		search.setEntCode(entCode);
		return new ResponseEntity<LicenseSaveResult>(licenseService.selectLienseCount(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "X/E면장 수신결과 수동전송", notes="X/E면장 수신결과 수동전송")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/update/resultSend", method = RequestMethod.POST)
	public ResponseEntity<LicenseSaveResult> updateResultSend(@PathVariable("entCode") String entCode, @RequestBody SearchLicenseBatchUpdate search, HttpServletRequest request) throws Exception {
		search.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", null, null);
		return new ResponseEntity<LicenseSaveResult>(licenseService.updateResultSend(search), HttpStatus.OK);
	}
	

}
