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
import org.yht.exps.express.model.eCommerce.broker.BrokerHouseList;
import org.yht.exps.express.model.eCommerce.broker.BrokerHouseSubList;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterList;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterSubList;
import org.yht.exps.express.model.eCommerce.broker.search.SearchBrokerBatchUpdate;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.service.eCommerce.BrokerService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/broker")
public class BrokerController {
	
	@Resource(name = "brokerService")
	BrokerService brokerService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	
	@ApiOperation(value = "전자상거래 broker-M 조회", notes="broker-M 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/brokerM/search/list", method = RequestMethod.GET)
	public ResponseEntity<BrokerMasterList> selectBrokerMasterList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
		return new ResponseEntity<BrokerMasterList>(brokerService.selectBrokerMasterList(search), HttpStatus.OK);
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
	@RequestMapping(value="/brokerMSub/search/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectBrokerMExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchOrderList search,
						HttpServletRequest request) throws Exception {
		
 
		search.setPage(0);
		BrokerMasterSubList orderList = brokerService.selectBrokerMasterSubList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getBrokerMasterSubList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Broker_List", map), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/brokerMSub/search/excelPost", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBrokerMExcelPost(
						@PathVariable("entCode") String entCode,
						@RequestBody SearchOrderList search,
						HttpServletRequest request) throws Exception {
		
 
		search.setPage(0);
		BrokerMasterSubList orderList = brokerService.selectBrokerMasterSubList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getBrokerMasterSubList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Broker_List", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 broker-M House 조회", notes="broker-M House 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/brokerMSub/search/list", method = RequestMethod.GET)
	public ResponseEntity<BrokerMasterSubList> selectBrokerMasterSubList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
		return new ResponseEntity<BrokerMasterSubList>(brokerService.selectBrokerMasterSubList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 관세사지정 리스트 조회", notes="관세사지정 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customsOfficer/search/list", method = RequestMethod.GET)
	public ResponseEntity<BrokerMasterList> selectCustomsOfficerList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
		return new ResponseEntity<BrokerMasterList>(brokerService.selectCustomsOfficerList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 broker-H 조회", notes="broker-H 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/brokerH/search/list", method = RequestMethod.GET)
	public ResponseEntity<BrokerHouseList> selectBrokerHouseList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
		return new ResponseEntity<BrokerHouseList>(brokerService.selectBrokerHouseList(search), HttpStatus.OK);
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
	@RequestMapping(value="/brokerH/search/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectBrokerHExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchOrderList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		BrokerHouseList orderList = brokerService.selectBrokerHouseList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getBrokerHouseList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Broker_List", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 broker-H 금액 조회", notes="broker-H 금액 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/brokerHSub/search/list", method = RequestMethod.GET)
	public ResponseEntity<BrokerHouseSubList> selectBrokerHouseSubList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
		return new ResponseEntity<BrokerHouseSubList>(brokerService.selectBrokerHouseSubList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 broker-M 통관완료", notes="broker-M 통관완료 처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/batch/Update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> batchUpdate(@PathVariable("entCode") String entCode, @RequestBody SearchBrokerBatchUpdate search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(brokerService.batchUpdate(search,request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로그인네트웍 LCSV2 API - 수입 신고 요청 정보 전송", notes="수입 신고 요청 정보를 전송합니다. ")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customsShipmentRequest", method = RequestMethod.GET)
	public ResponseEntity<OrderSaveResult> customsShipmentRequest(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "registerUserId", null, null);
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", false, null, null, null, null);
		OrderSaveResult orderSaveResult = brokerService.selectCustomsShipmentRequest(search);
		if(!"".equals(Util.null2str(orderSaveResult.getResultCode()))) {
			return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
		}
		return new ResponseEntity<OrderSaveResult>(brokerService.customsShipmentRequest(search, orderSaveResult), HttpStatus.OK);
	}
	
	@ApiOperation(value = "로그인네트웍 LCSV2 API - 통관 정보 수신 요청", notes="통관 정보를 수신 요청합니다. ")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customsInfoRequest", method = RequestMethod.GET)
	public ResponseEntity<OrderSaveResult> customsInfoRequest(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", false, null, null, null, null);
		OrderSaveResult orderSaveResult = brokerService.selectCustomsShipmentRequest(search);
		if(!"".equals(Util.null2str(orderSaveResult.getResultCode()))) {
			return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
		}
		return new ResponseEntity<OrderSaveResult>(brokerService.customsInfoRequest(search, orderSaveResult), HttpStatus.OK);
	}
	
}
