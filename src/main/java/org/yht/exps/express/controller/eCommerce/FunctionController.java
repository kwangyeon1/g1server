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
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.eCommerce.function.FunctionOrder;
import org.yht.exps.express.model.eCommerce.function.FunctionSaveResult;
import org.yht.exps.express.model.eCommerce.function.OrderPenaltyList;
import org.yht.exps.express.service.eCommerce.FunctionService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/func")
public class FunctionController {
	
	@Resource(name = "functionService")
	FunctionService functionService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
		
	@ApiOperation(value = "전자상거래 반입적용/취소", notes="반입적용/취소합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryin/update", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> carryin(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		if(functionOrder.getSrchInfo() != null){
			functionOrder.getSrchInfo().put("srchLoginId", functionOrder.getUpdateUserId());
		}
		return new ResponseEntity<FunctionSaveResult>(functionService.carryin(functionOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 반입적용/취소 (등록 화면용)", notes="반입적용/취소합니다. (등록 화면용)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryinRegistration/update", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> carryinRegistration(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<FunctionSaveResult>(functionService.carryinRegistration(functionOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 반출", notes="반출합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryout/update", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> carryout(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<FunctionSaveResult>(functionService.carryout(functionOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 발송보류/취소", notes="발송보류/취소합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendHold/update", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> sendHold(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		if(functionOrder.getSrchInfo() != null){
			functionOrder.getSrchInfo().put("srchLoginId", functionOrder.getUpdateUserId());
			functionOrder.getSrchInfo().put("userId", functionOrder.getUpdateUserId());
		}
		return new ResponseEntity<FunctionSaveResult>(functionService.sendHold(functionOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 발송", notes="발송합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/send/update", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> send(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		if(functionOrder.getSrchInfo() != null){
			functionOrder.getSrchInfo().put("srchLoginId", functionOrder.getUpdateUserId());
		}
		return new ResponseEntity<FunctionSaveResult>(functionService.send(functionOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 입항일자 적용", notes="입항일자 적용합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/obDate/update", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> obDate(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		if(functionOrder.getSrchInfo() != null){
			functionOrder.getSrchInfo().put("srchLoginId", functionOrder.getUpdateUserId());
		}
		return new ResponseEntity<FunctionSaveResult>(functionService.obDate(functionOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 개인통관부호검증", notes="개인통관부호검증 적용합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customsIdVali/update", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> customsIdVali(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		functionOrder.setUserName(functionOrder.getLogUserName());
		if(functionOrder.getSrchInfo() != null){
			functionOrder.getSrchInfo().put("srchLoginId", functionOrder.getUpdateUserId());
			functionOrder.getSrchInfo().put("userId", functionOrder.getUpdateUserId());
		}
		return new ResponseEntity<FunctionSaveResult>(functionService.customsIdVali(functionOrder, request), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 개인통관부호검증 대상건수 조회", notes="개인통관부호검증 대상건수 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customsIdVali/targetCount", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> customsIdValiTargetCount(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		if(functionOrder.getSrchInfo() != null){
			functionOrder.getSrchInfo().put("cryptographyYn", functionOrder.getCryptographyYn());
			functionOrder.getSrchInfo().put("srchLoginId", functionOrder.getUpdateUserId());
			functionOrder.getSrchInfo().put("userId", functionOrder.getUpdateUserId());
		}
		functionSaveResult.setResultCode(Util.null2str(functionService.selectOrderList(functionOrder.getSrchInfo()).size()));
		return new ResponseEntity<FunctionSaveResult>(functionSaveResult, HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 과태료검증", notes="과태료검증 적용합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/penaltyVali/update", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> penaltyVali(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		if(functionOrder.getSrchInfo() != null){
			functionOrder.getSrchInfo().put("srchLoginId", functionOrder.getUpdateUserId());
		}
		return new ResponseEntity<FunctionSaveResult>(functionService.penaltyVali(functionOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 과태료 list 조회", notes="과태료 Order 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/penalty/search/list", method = RequestMethod.GET)
	public ResponseEntity<OrderPenaltyList> selectPenaltyOrderList(@PathVariable("entCode") String entCode, @QueryStringArgResolver FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderPenaltyList>(functionService.selectPenaltyOrderList(functionOrder), HttpStatus.OK);
	}
	
	/**
	 * 과태료 list
	 * @param entCode
	 * @param functionOrder
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "과태료 list 조회", notes="과태료 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/penalty/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectOrderExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver FunctionOrder functionOrder,
						HttpServletRequest request) throws Exception {
		
		String srchLoginId = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"));
		if(!Util.isEmpty(srchLoginId)){
			if(functionOrder.getSrchInfo() != null){
				functionOrder.getSrchInfo().put("srchLoginId", srchLoginId);
			}
		}
		functionOrder.setPage(0);
		OrderPenaltyList penList = functionService.selectPenaltyOrderList(functionOrder);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(functionOrder, Map.class);
		map.put("data", penList.getPenaltyOrderList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Penalty_List", map), HttpStatus.OK);
	}

	@ApiOperation(value = "전자상거래 등록/등록 취소", notes="등록/등록 취소합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/orderRegistration/update", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> orderRegistration(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<FunctionSaveResult>(functionService.orderRegistration(functionOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 개인통관부호 인증번호 재발급", notes="개인통관부호검증 적용합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customsIdReissu/update", method = RequestMethod.POST)
	public ResponseEntity<FunctionSaveResult> customsIdReissuance(@PathVariable("entCode") String entCode, @RequestBody FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, functionOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		functionOrder.setUserName(functionOrder.getLogUserName());
		if(functionOrder.getSrchInfo() != null){
			functionOrder.getSrchInfo().put("srchLoginId", functionOrder.getUpdateUserId());
			functionOrder.getSrchInfo().put("userId", functionOrder.getUpdateUserId());
		}
		return new ResponseEntity<FunctionSaveResult>(functionService.customsIdReissuance(functionOrder, request), HttpStatus.OK);
	}
}
