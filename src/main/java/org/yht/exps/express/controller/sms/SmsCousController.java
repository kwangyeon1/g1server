package org.yht.exps.express.controller.sms;

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
import org.yht.exps.express.model.sms.search.SearchEcmCustomerDetail;
import org.yht.exps.express.model.sms.search.SearchEcmCustomerList;
import org.yht.exps.express.model.sms.search.SearchEcmCustomerTreeList;
import org.yht.exps.express.model.sms.user.CustomerSaveResult;
import org.yht.exps.express.model.sms.user.EcmCustomer;
import org.yht.exps.express.model.sms.user.EcmCustomerDelete;
import org.yht.exps.express.model.sms.user.EcmCustomerList;
import org.yht.exps.express.model.sms.user.EcmCustomerMapping;
import org.yht.exps.express.model.sms.user.EcmCustomerTreeList;
import org.yht.exps.express.model.sms.user.EcmUserApprove;
import org.yht.exps.express.model.sms.user.EcmUserDelete;
import org.yht.exps.express.model.sms.user.EcmUserDetail;
import org.yht.exps.express.model.sms.user.EcmUserList;
import org.yht.exps.express.model.sms.user.EcmUserSave;
import org.yht.exps.express.model.sms.user.UserSaveResult;
import org.yht.exps.express.model.sms.user.search.SearchEcmUserDetail;
import org.yht.exps.express.model.sms.user.search.SearchEcmUserList;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.sms.SmsCousService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "SmsCous")
@RequestMapping("/{entCode}/sms/smsCous")
public class SmsCousController {
	
	@Resource(name = "smsCousService")
	SmsCousService smsCousService;
	
	@Resource(name = "fileService")
	FileService fileService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "ECM 웹 사용자 리스트 조회", notes="ECM 웹 사용자 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<EcmUserList> selectEcmUserList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchEcmUserList search) throws Exception {
		return new ResponseEntity<EcmUserList>(smsCousService.selectEcmUserList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 웹 거래처 트리 리스트 조회", notes="ECM 웹 거래처 트리 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/item/search/list", method = RequestMethod.GET)
	public ResponseEntity<EcmCustomerTreeList> selectEcmUserTreeList(@PathVariable("entCode") String entCode, SearchEcmCustomerTreeList search) throws Exception {
		return new ResponseEntity<EcmCustomerTreeList>(smsCousService.selectEcmCustomTreeList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 웹 사용자 승인여부처리", notes="ECM 웹 사용자 승인여부처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/approve/batch", method = RequestMethod.POST)
	public ResponseEntity<UserSaveResult> updateEcmUserApprove(@PathVariable("entCode") String entCode, @RequestBody EcmUserApprove ecmUserApprove, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, ecmUserApprove, "updateUserId", null, null);
		return new ResponseEntity<UserSaveResult>(smsCousService.updateEcmUserApprove(ecmUserApprove), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 웹 사용자 삭제처리", notes="ECM 웹 사용자 삭제처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete/{userId}", method = RequestMethod.DELETE)
	public ResponseEntity<UserSaveResult> deleteEcmUser(@PathVariable("entCode") String entCode, @RequestBody EcmUserDelete ecmUserDelete) throws Exception {
		return new ResponseEntity<UserSaveResult>(smsCousService.deleteEcmUser(ecmUserDelete), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 웹 사용자 삭제일괄처리", notes="ECM 웹 사용자 삭제처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete/batch", method = RequestMethod.DELETE)
	public ResponseEntity<UserSaveResult> deleteEcmUserList(@PathVariable("entCode") String entCode, @RequestBody EcmUserDelete ecmUserDelete, HttpServletRequest request) throws Exception {
		return new ResponseEntity<UserSaveResult>(smsCousService.deleteEcmUserList(ecmUserDelete, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 웹 사용자 insert", notes="ECM 웹 사용자 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<UserSaveResult> insertEcmUser(@PathVariable("entCode") String entCode, @RequestBody EcmUserSave ecmUserSave, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, ecmUserSave, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<UserSaveResult>(smsCousService.insertEcmUser(ecmUserSave, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 웹 사용자 update", notes="ECM 웹 사용자 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/{userId}", method = RequestMethod.PUT)
	public ResponseEntity<UserSaveResult> updateEcmUser(@PathVariable("entCode") String entCode, @RequestBody EcmUserSave ecmUserSave, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, ecmUserSave, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<UserSaveResult>(smsCousService.updateEcmUser(ecmUserSave), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 웹 사용자 상세 조회", notes="ECM 웹 사용자 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/{userId}", method = RequestMethod.GET)
	public ResponseEntity<EcmUserDetail> selectEcmUserDetail(@PathVariable("entCode") String entCode, SearchEcmUserDetail search) throws Exception {
		return new ResponseEntity<EcmUserDetail>(smsCousService.selectEcmUserDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 거래처 리스트 조회", notes="ECM 거래처 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customer/search/list", method = RequestMethod.GET)
	public ResponseEntity<EcmCustomerList> selectEcmCustomerList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchEcmCustomerList search) throws Exception {
		return new ResponseEntity<EcmCustomerList>(smsCousService.selectEcmCustomerList(search), HttpStatus.OK);
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
	@RequestMapping(value="/customer/search/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectCustomerExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchEcmCustomerList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		EcmCustomerList orderList = smsCousService.selectEcmCustomerList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getEcmCustomerList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Customer List", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 거래처 사용자등록여부 조회", notes="ECM 거래처 사용자등록여부 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customer/check/user/cnt", method = RequestMethod.POST)
	public ResponseEntity<Integer> checkEcmCustomerUserCnt(@PathVariable("entCode") String entCode, @RequestBody EcmCustomerDelete ecmCustomerDelete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, ecmCustomerDelete, "updateUserId", null, null);
		return new ResponseEntity<Integer>(smsCousService.checkEcmCustomerUserCnt(ecmCustomerDelete), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 거래처 삭제처리", notes="ECM 거래처 삭제처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customer/delete/{customerCode}", method = RequestMethod.DELETE)
	public ResponseEntity<CustomerSaveResult> deleteEcmCustomer(@PathVariable("entCode") String entCode, @RequestBody EcmCustomerDelete ecmCustomerDelete, HttpServletRequest request) throws Exception {
		return new ResponseEntity<CustomerSaveResult>(smsCousService.deleteEcmCustomer(ecmCustomerDelete), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 거래처 삭제일괄처리", notes="ECM 거래처 삭제처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customer/delete/batch", method = RequestMethod.DELETE)
	public ResponseEntity<CustomerSaveResult> deleteEcmCustomerList(@PathVariable("entCode") String entCode, @RequestBody EcmCustomerDelete ecmCustomerDelete, HttpServletRequest request) throws Exception {
		return new ResponseEntity<CustomerSaveResult>(smsCousService.deleteEcmCustomerList(ecmCustomerDelete), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 거래처 insert", notes="ECM 거래처 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customer/insert", method = RequestMethod.POST)
	public ResponseEntity<CustomerSaveResult> insertEcmCustomer(@PathVariable("entCode") String entCode, @RequestBody EcmCustomer ecmCustomer, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, ecmCustomer, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CustomerSaveResult>(smsCousService.insertEcmCustomer(ecmCustomer, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 거래처 update", notes="ECM 거래처 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customer/update/{customerCode}", method = RequestMethod.PUT)
	public ResponseEntity<CustomerSaveResult> updateEcmCustomer(@PathVariable("entCode") String entCode, @RequestBody EcmCustomer ecmCustomer, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, ecmCustomer, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CustomerSaveResult>(smsCousService.updateEcmCustomer(ecmCustomer, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 거래처 상세 조회", notes="ECM 거래처상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customer/get/{customerCode}", method = RequestMethod.GET)
	public ResponseEntity<EcmCustomer> selectEcmCustomerDetail(@PathVariable("entCode") String entCode, SearchEcmCustomerDetail search) throws Exception {
		return new ResponseEntity<EcmCustomer>(smsCousService.selectEcmCustomerDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 거래처 맵핑 또는 해제", notes="ECM 거래처 맵핑 또는 해제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customer/mappingExec", method = RequestMethod.PUT)
	public ResponseEntity<CustomerSaveResult> mappingExec(@PathVariable("entCode") String entCode, @RequestBody EcmCustomerMapping ecmCustomerMapping, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, ecmCustomerMapping, "updateUserId", null, null);
		return new ResponseEntity<CustomerSaveResult>(smsCousService.mappingExec(ecmCustomerMapping), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 상(하)위 업체 등록 엑셀 업로드", notes="ECM 상(하위) 업체 엑셀 업로드 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customer/insert/Excel/{updateUserId}/{gmtTime}/{userBusinessCode}", method = RequestMethod.POST)
	public ResponseEntity<CustomerSaveResult> insertEcmCustomerExcel(HttpServletRequest request
			, @PathVariable("entCode") String entCode
			, @PathVariable("updateUserId") String updateUserId
			, @PathVariable("gmtTime") String gmtTime
			, @PathVariable("userBusinessCode") String userBusinessCode) throws Exception {

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
		MultipartFile file = multipartRequest.getFile("excelUpload_file");
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(file, new String[]{"xlsx", "xls"})) {
			CustomerSaveResult customerSaveResult = new CustomerSaveResult();
			customerSaveResult.setResultCode("extError");
			return new ResponseEntity<CustomerSaveResult>(customerSaveResult, HttpStatus.OK);
		}
		
		updateUserId = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"), updateUserId);
		
		FileUploadResponse result = fileService.storeFile(file, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")){
			return new ResponseEntity<CustomerSaveResult>(smsCousService.insertEcmCustomerExcel(entCode, updateUserId, gmtTime, fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile(), userBusinessCode, request), HttpStatus.OK);
		}else {
			return new ResponseEntity<CustomerSaveResult>(smsCousService.insertEcmCustomerExcel(entCode, updateUserId, gmtTime,  null, null, null), HttpStatus.OK);
		}
	}
	
	@ApiOperation(value = "ECM 상(하)위 업체 사용자 ID 생성 엑셀 업로드", notes="ECM 상(하위) 업체 사용자 ID 생성 엑셀 업로드 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/UserInfo/insert/Excel/{updateUserId}/{gmtTime}/{userBusinessCode}/{bussSolutionData}/{headEntBusinessCode}", method = RequestMethod.POST)
	public ResponseEntity<UserSaveResult> insertEcmUserInfoExcel(HttpServletRequest request
			, @PathVariable("entCode") String entCode
			, @PathVariable("updateUserId") String updateUserId
			, @PathVariable("gmtTime") String gmtTime
			, @PathVariable("userBusinessCode") String userBusinessCode
			, @PathVariable("bussSolutionData") String bussSolutionData
			, @PathVariable("headEntBusinessCode") String headEntBusinessCode) throws Exception {

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
		MultipartFile file = multipartRequest.getFile("excelUpload_file");
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(file, new String[]{"xlsx", "xls"})) {
			UserSaveResult userSaveResult = new UserSaveResult();
			userSaveResult.setResultCode("extError");
			return new ResponseEntity<UserSaveResult>(userSaveResult, HttpStatus.OK);
		}
		updateUserId = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"), updateUserId);
		FileUploadResponse result = fileService.storeFile(file, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")){
			return new ResponseEntity<UserSaveResult>(smsCousService.insertEcmUserInfoExcel(entCode, updateUserId, gmtTime, fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile(), userBusinessCode, bussSolutionData, headEntBusinessCode, request), HttpStatus.OK);
		}else {
			return new ResponseEntity<UserSaveResult>(smsCousService.insertEcmUserInfoExcel(entCode, updateUserId, gmtTime,  null, null, null, null, null), HttpStatus.OK);
		}
	}
	
}
