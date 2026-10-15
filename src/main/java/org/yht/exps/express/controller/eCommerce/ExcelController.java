package org.yht.exps.express.controller.eCommerce;

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
import org.yht.exps.express.model.eCommerce.order.ExcelError;
import org.yht.exps.express.model.eCommerce.order.ExcelErrorCondition;
import org.yht.exps.express.model.eCommerce.order.ExcelErrorInsertList;
import org.yht.exps.express.model.eCommerce.order.ExcelErrorList;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadResultList;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchExcelUploadResult;
import org.yht.exps.express.model.search.SearchElastic;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.eCommerce.ExcelService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Excel")
@RequestMapping("/{entCode}/ecm/eCommerce/excel")
public class ExcelController {
	
	@Resource(name = "excelService")
	ExcelService excelService;
	
	@Resource(name = "commonService")
	CommonService commonService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	/**
	 * 엑셀 업로드 Error List
	 * @param entCode
	 * @param searchExcelUploadResult
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "엑셀 업로드 결과 리스트 조회", notes="에러 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/result/list", method = RequestMethod.GET)
	public ResponseEntity<ExcelUploadResultList> selectResultList(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchExcelUploadResult searchExcelUploadResult,						
						HttpServletRequest request) throws Exception {
		
		if(!"".equals(Util.null2str(searchExcelUploadResult.getSrchRegisterUserId()))){
			Util.getSessionUserInfo(request, jwtTokenProvider, searchExcelUploadResult, "srchRegisterUserId", null, null);
		}
		
		return new ResponseEntity<ExcelUploadResultList>(excelService.selectResultList(searchExcelUploadResult), HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param entCode
	 * @param searchMf
	 * @param request
	 * @return
	 * @throws Exception
	 */
	/**
	 * 엑셀 업로드 Error List
	 * @param entCode
	 * @param index
	 * @param searchElastic
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "엑셀 업로드 에러 리스트 조회", notes="에러 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "index", value = "에러 INDEX", required = true, dataType = "string", paramType = "path", defaultValue = "ecm-excel-upload-20201117020827932-yhtxx-kr")
	})	
	@RequestMapping(value="/search/error/list/{index}", method = RequestMethod.GET)
	public ResponseEntity<ExcelErrorList> selectErrorList(
						@PathVariable("entCode") String entCode,
						@PathVariable("index") String index,
						@QueryStringArgResolver SearchElastic searchElastic,						
						HttpServletRequest request) throws Exception {
		
		return new ResponseEntity<ExcelErrorList>(excelService.selectErrorList(index, searchElastic), HttpStatus.OK);
	}
	@ApiOperation(value = "엑셀 업로드 에러 리스트 엑셀파일생성", notes="에러 리스트를 엑셀파일 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "index", value = "에러 INDEX", required = true, dataType = "string", paramType = "path", defaultValue = "ecm-excel-upload-20201117020827932-yhtxx-kr")
	})	
	@RequestMapping(value="/search/error/list/excel/{index}", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectErrorListExcel(
			@PathVariable("entCode") String entCode,
			@PathVariable("index") String index,
			@QueryStringArgResolver SearchElastic searchElastic,						
			HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(searchElastic, Map.class);
		List<ExcelError> list = excelService.selectErrorList(index, searchElastic).getExcelErrorList();
		int i = 1;
		String errorReason;
		ExcelErrorCondition condition;
		for(ExcelError error : list){
			errorReason = Util.null2str(error.getErrorReason());
			if(!Util.isEmpty(error.getErrorType())) {
				errorReason = error.getErrorType();
			}
			condition = error.getErrorCondition();
			if(!"".equals(errorReason) && condition != null){
				errorReason += " (Length: " + Util.null2str(condition.getItemLength());
				errorReason += ", Required: " + Util.null2str(condition.getItemRequired());
				errorReason += ", Type: " + Util.null2str(condition.getItemType());
				if(!Util.isEmpty(condition.getItemPossible())){
					errorReason += ", Possible: " + Util.null2str(condition.getItemPossible());
				}
				errorReason += ")";
			}
			
			error.setNum(i++);
			error.setErrorReason(errorReason);
		}
		map.put("data", list);
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("ExcelErrorList", map), HttpStatus.OK);
	}
	
	/**
	 * 엑셀 업로드 Error List 저장
	 * @param entCode
	 * @param entBusinessCode
	 * @param userId
	 * @param resultIndex
	 * @param resultId
	 * @param errorIndex
	 * @param orderList
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "에러 리스트 저장", notes="에러 리스트를 수정하여 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "entBusinessCode", value = "사업장코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX-KR"),
		@ApiImplicitParam(name = "userId", value = "등록자ID", required = true, dataType = "string", paramType = "path", defaultValue = "201808-001"),
		@ApiImplicitParam(name = "resultIndex", value = "결과 INDEX", required = true, dataType = "string", paramType = "path", defaultValue = "ecm-excel-upload-result-20201117020827932-yhtxx-kr"),
		@ApiImplicitParam(name = "resultId", value = "결과 데이터 ID", required = true, dataType = "string", paramType = "path", defaultValue = "20210702020009287-YHTXX-KR"),
		@ApiImplicitParam(name = "errorIndex", value = "에러 INDEX", required = true, dataType = "string", paramType = "path", defaultValue = "ecm-excel-upload-error-20201117020827932-yhtxx-kr")
	})	
	@RequestMapping(value="/insert/{entBusinessCode}/{userId}/{resultIndex}/{resultId}/{errorIndex}", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertOrderList(
						@PathVariable("entCode") String entCode,
						@PathVariable("entBusinessCode") String entBusinessCode,
						@PathVariable("userId") String userId,
						@PathVariable("resultIndex") String resultIndex,
						@PathVariable("resultId") String resultId,
						@PathVariable("errorIndex") String errorIndex,
						@RequestBody ExcelErrorInsertList insertData,
						HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, insertData, "userId", "logUserName", null, "logUserNameEng");
		userId = Util.null2str(insertData.getUserId());
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		// 데이터 저장
		try {
			excelService.insertOrderList(insertData, resultIndex, resultId, errorIndex, orderSaveResult);
			
			if(!"ERROR".equals(orderSaveResult.getResultCode())){
				// 처리 로그 기록
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(entCode);
				processLogs.setEntBusinessCode(entBusinessCode);
				processLogs.setSolution("ECM");
				processLogs.setBound("COM");
				processLogs.setBoundType("COM");
				processLogs.setProcessUserId(userId);
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType("ADD");
				processLogs.setDescription("Excel Error List ADD => ES Index(" + resultIndex + ")");
				processLogs.setJobLog(new Gson().toJson(insertData));
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(insertData.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(insertData.getLogUserName());
				processLogs.setProcessUserNameEng(insertData.getLogUserNameEng());
				commonService.insertLog(processLogs, request);
			}
		}catch(Exception e){
			if(!"stockNull".equals(orderSaveResult.getResultCode())){
				throw e;
			}
		}
		
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
}
