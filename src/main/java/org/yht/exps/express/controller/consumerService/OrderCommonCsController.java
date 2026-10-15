package org.yht.exps.express.controller.consumerService;

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
import org.yht.exps.express.mapper.consumerService.OrderCommonCsMapper;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.consumerService.order.OrderCustomerList;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList;
import org.yht.exps.express.model.eCommerce.bondedTrans.BondedTrans;
import org.yht.exps.express.model.eCommerce.bondedTrans.BondedTransList;
import org.yht.exps.express.model.eCommerce.bondedTrans.BondedTransSaveResult;
import org.yht.exps.express.model.eCommerce.bondedTrans.search.SearchBondedTrans;
import org.yht.exps.express.model.eCommerce.bondedTrans.search.SearchBondedTransList;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterSub;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterSubList;
import org.yht.exps.express.model.eCommerce.changeDelivery.ChangeDelivery;
import org.yht.exps.express.model.eCommerce.changeDelivery.ChangeDeliveryList;
import org.yht.exps.express.model.eCommerce.changeDelivery.ChangeDeliverySaveResult;
import org.yht.exps.express.model.eCommerce.changeDelivery.search.SearchChangeDelivery;
import org.yht.exps.express.model.eCommerce.changeDelivery.search.SearchChangeDeliveryList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgOrder;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgOrderList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgSaveResult;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWg;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWgOrderList;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadOdChg;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.eCommerce.quickRequest.QuickRequest;
import org.yht.exps.express.model.eCommerce.quickRequest.QuickRequestDeleteList;
import org.yht.exps.express.model.eCommerce.quickRequest.QuickRequestList;
import org.yht.exps.express.model.eCommerce.quickRequest.QuickRequestSaveResult;
import org.yht.exps.express.model.eCommerce.quickRequest.search.SearchQuickRequest;
import org.yht.exps.express.model.eCommerce.quickRequest.search.SearchQuickRequestList;
import org.yht.exps.express.model.eCommerce.returnMng.ReturnMng;
import org.yht.exps.express.model.eCommerce.returnMng.ReturnMngDeleteList;
import org.yht.exps.express.model.eCommerce.returnMng.ReturnMngList;
import org.yht.exps.express.model.eCommerce.returnMng.ReturnMngSaveResult;
import org.yht.exps.express.model.eCommerce.returnMng.search.SearchReturnMngList;
import org.yht.exps.express.model.eCommerce.unusualMng.UnusualMngList;
import org.yht.exps.express.model.eCommerce.unusualMng.search.SearchUnusualMngList;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.consumerService.OrderCommonCsService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "orderCommonCs")
@RequestMapping("/{entCode}/ecm/order/commonCs")
public class OrderCommonCsController {
	
	@Resource(name = "orderCommonCsService")
	OrderCommonCsService orderCommonCsService;
	@Resource(name = "commonService")
	CommonService commonService;
	
	@Autowired
	OrderCommonCsMapper orderCommonCsMapper;
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	/**
	 * E-commerce > 고객서비스 > 오더 > 검사관리
	 * @param entCode
	 * @param SearchOrderCustomerList
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "전자상거래 Customer Order list 조회", notes="Customer Order 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<OrderCustomerList> selectOrderList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderCustomerList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", "userName", "loginId", null, false, true, false, false);
		
		if(search == null || search.getSrchEntCode() == null){
			return new ResponseEntity<OrderCustomerList>(new OrderCustomerList(), HttpStatus.BAD_REQUEST);
		}else{
			return new ResponseEntity<OrderCustomerList>(orderCommonCsService.selectOrderList(search), HttpStatus.OK);
		}
	}
	
	/**
	 * E-commerce > 고객서비스 > 오더 > 검사관리 - Detail
	 * @param entCode
	 * @param SearchOrderList
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "전자상거래 검사관리 Detail 조회", notes="검사관리 Detail를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/detail", method = RequestMethod.GET)
	public ResponseEntity<Order> selectInspectionDetail(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderCustomerList search) throws Exception {
		return new ResponseEntity<Order>(orderCommonCsService.selectInspectionDetail(search), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 > 오더 > 검사관리 - Detail > 엔트리저장
	 * @param entCode
	 * @param BrokerMasterSub
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "고객서비스검사관리 Detail  저장", notes="검사관리 Detail 정 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/search/update/{odJobNo}", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> saveInspectionDetailUpdate(@PathVariable("entCode") String entCode,@PathVariable("odJobNo") String odJobNo,  @RequestBody Order order) throws Exception {
		order.setOdJobNo(odJobNo);
		OrderSaveResult orderSaveResult = orderCommonCsService.saveInspectionDetailUpdate(order);
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
	/**
	 * E-commerce > 고객서비스 > 오더 > 검사관리-대리점/화주(발송) > 엑셀 list
	 * @param entCode
	 * @param functionOrder
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectInspectionList(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchOrderCustomerList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		OrderCustomerList orderList = orderCommonCsService.selectOrderList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getOrderCustomerList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("InspectionMng_List", map), HttpStatus.OK);
	}
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > 미착/무적 > 엑셀업로드
	 * @param entCode
	 * @param excelUploadType
	 * @param MultipartFile
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "오더변경 ExcelUpload", notes="오더변경 ExcelUpload합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "excelUploadType", value = "엑셀타입", required = true, dataType = "string", paramType = "path", defaultValue = "odChg")
	})
	@RequestMapping(value="/excelUpload/check/{excelUploadType}", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> excelUploadCheck(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
		return new ResponseEntity<OrderSaveResult>(orderCommonCsService.excelUploadCheck(uploadExcel, entCode, excelUploadType), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > 미착/무적 > 엑셀업로드
	 * @param entCode
	 * @param excelUploadType
	 * @param MultipartFile
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "오더변경 Excel 저장", notes="오더변경 Excel 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload/{excelUploadType}", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> excelUpload(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestBody ExcelUploadOdChg excelUploadOdChg, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, excelUploadOdChg, "updateUserId", "logUserName", null, "logUserNameEng");
		if("insertMgn".equals(excelUploadType) || "insertMfn".equals(excelUploadType)){
			return new ResponseEntity<OrderSaveResult>(orderCommonCsService.excelUploadInsertMgnMfn(excelUploadOdChg, excelUploadType, request), HttpStatus.OK);
		}else if("insertBondedTrans".equals(excelUploadType)) {
			return new ResponseEntity<OrderSaveResult>(orderCommonCsService.excelUploadInsertBondedTrans(excelUploadOdChg, excelUploadType, request), HttpStatus.OK);
		}else if("insertChangeDelivery".equals(excelUploadType)) {
			return new ResponseEntity<OrderSaveResult>(orderCommonCsService.excelUploadInsertChangeDelivery(excelUploadOdChg, excelUploadType, request), HttpStatus.OK);
		}else if("insertQuickRequest".equals(excelUploadType)) {
			return new ResponseEntity<OrderSaveResult>(orderCommonCsService.excelUploadInsertQuickRequest(excelUploadOdChg, excelUploadType, request), HttpStatus.OK);
		}else if("insertReturn".equals(excelUploadType)) {
			return new ResponseEntity<OrderSaveResult>(orderCommonCsService.excelUploadInsertReturn(excelUploadOdChg, excelUploadType, request), HttpStatus.OK);
		}else {
			OrderSaveResult result = new OrderSaveResult();
		    result.setResultCode("NO DATA");
		    return new ResponseEntity<>(result, HttpStatus.OK);
		}
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 수입신고
	 * @param entCode
	 * @param SearchOrderList
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "전자상거래 broker-M House 조회", notes="broker-M House 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/brokerMSub/search/list", method = RequestMethod.GET)
	public ResponseEntity<BrokerMasterSubList> selectBrokerMasterSubList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
		return new ResponseEntity<BrokerMasterSubList>(orderCommonCsService.selectBrokerMasterSubList(search), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 수입신고 > 엑셀다운로드
	 * @param entCode
	 * @param SearchOrderList
	 * @return
	 * @throws Exception
	 */
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
		BrokerMasterSubList orderList = orderCommonCsService.selectBrokerMasterSubList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getBrokerMasterSubList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Broker_List", map), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 수입신고 > 엔트리
	 * @param entCode
	 * @param SearchOrderList
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "고객서비스 Broker 팝업 조회", notes="Broker를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/brokerMSub/search/detail/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<BrokerMasterSub> selectBrokerDetail(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo,  SearchOrderList search) throws Exception {
		search.setSrchEntCode(entCode);
		search.setSrchOdJobNo(odJobNo);
		return new ResponseEntity<BrokerMasterSub>(orderCommonCsService.selectBrokerDetail(search), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 수입신고 > 엔트리저장
	 * @param entCode
	 * @param BrokerMasterSub
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "고객서비스 Broker 팝업 저장", notes="Broker수정 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/brokerMSub/search/update/{odJobNo}", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> saveBrokerUpdate(@PathVariable("entCode") String entCode,@PathVariable("odJobNo") String odJobNo,  @RequestBody BrokerMasterSub broker) throws Exception {
		broker.setOdJobNo(odJobNo);
		OrderSaveResult orderSaveResult = orderCommonCsService.saveBrokerUpdate(broker);
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 이영(중량)화물관리
	 * @param entCode
	 * @param searchMultiWgOrderList
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "고객서비스 이영(중량) 화물관리 조회", notes="이영(중량) 화물관리 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multiWgOrder/search/list", method = RequestMethod.GET)
	public ResponseEntity<MultiWgOrderList> selectSubMultiWgList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMultiWgOrderList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<MultiWgOrderList>(orderCommonCsService.selectSubMultiWgList(search), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 이영(중량)화물관리 > 엔트리
	 * @param entCode
	 * @param searchMultiWg
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "전자상거래 멀티(중량) Order 상세 조회", notes="전자상거래 멀티(중량) Order 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multiWgOrder/get/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<MultiWgOrder> selectMultiOrderDetail(@PathVariable("entCode") String entCode, SearchMultiWg search) throws Exception {
		return new ResponseEntity<MultiWgOrder>(orderCommonCsService.selectMultiOrderDetail(search), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 이영(중량)화물관리 > 엔트리저장
	 * @param entCode
	 * @param MultiWgList
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "전자상거래 멀티(중량) List save", notes="전자상거래 멀티(중량) List 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multiWgOrder/saveMultiWgList", method = RequestMethod.POST)
	public ResponseEntity<MultiWgSaveResult> saveMultiWgList(@PathVariable("entCode") String entCode, @RequestBody MultiWgList multiWgList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, multiWgList, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<MultiWgSaveResult>(orderCommonCsService.saveMultiWgList(multiWgList, request), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 이영(중량)화물관리 > 신규저장
	 * @param entCode
	 * @param MultiWgList
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "전자상거래 멀티(중량) Insert ", notes="전자상거래 멀티(중량) 추가합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multiWgOrder/insertMultiWg", method = RequestMethod.POST)
	public ResponseEntity<MultiWgSaveResult> insertMultiWg(@PathVariable("entCode") String entCode, @RequestBody MultiWgOrder multiWgOrder, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, multiWgOrder, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<MultiWgSaveResult>(orderCommonCsService.insertMultiWg(multiWgOrder, request), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 이영(중량)화물관리 > 엑셀다운로드
	 * @param entCode
	 * @param MultiWgList
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/multiWgOrder/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectMultiWgListExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchMultiWgOrderList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		MultiWgOrderList orderList = orderCommonCsService.selectSubMultiWgList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getMultiWgOrderList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Multi_List", map), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 보세화물관리
	 * @param entCode
	 * @param searchBondedTransList
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "고객서비스 보세화물관리 조회", notes="보세화물관리 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/bondedTrans/search/list", method = RequestMethod.GET)
	public ResponseEntity<BondedTransList> selectBondedTransList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBondedTransList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BondedTransList>(orderCommonCsService.selectBondedTransList(search), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 보세화물관리 > 엔트리
	 * @param entCode
	 * @param searchBondedTrans
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "고객서비스 보세화물관리 상세 조회", notes="고객서비스 보세화물관리 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/bondedTrans/get/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<BondedTrans> selectBondedTransDetail(@PathVariable("entCode") String entCode, SearchBondedTrans search) throws Exception {
		return new ResponseEntity<BondedTrans>(orderCommonCsService.selectBondedTransDetail(search), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 보세화물관리> 엔트리저장
	 * @param entCode
	 * @param bondedTrans
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "보세화물관리 save", notes="보세화물관리 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/bondedTrans/saveBondedTrans", method = RequestMethod.POST)
	public ResponseEntity<BondedTransSaveResult> saveBondedTrans(@PathVariable("entCode") String entCode, @RequestBody BondedTrans bondedTrans, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, bondedTrans, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BondedTransSaveResult>(orderCommonCsService.saveBondedTrans(bondedTrans, request), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 보세화물관리 > 엑셀다운로드
	 * @param entCode
	 * @param searchBondedTransList
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/bondedTrans/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectBondedTransListExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchBondedTransList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		BondedTransList orderList = orderCommonCsService.selectBondedTransList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getBondedTransList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("BondedTrans_List", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 OrderList 삭제", notes="OrderList를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/bondedTrans/delete", method = RequestMethod.DELETE)
	public ResponseEntity<BondedTransSaveResult> deletebondedTransList(@PathVariable("entCode") String entCode, @RequestBody SearchBondedTransList searchBondedTransList, HttpServletRequest request) throws Exception {
		searchBondedTransList.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, searchBondedTransList, "userId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BondedTransSaveResult>(orderCommonCsService.deleteBondedTransList(searchBondedTransList, request), HttpStatus.OK);
	}
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 송장교체(주소변경)
	 * @param entCode
	 * @param searchChangeDeliveryList
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "고객서비스 송장교체(주소변경) 조회", notes="송장교체(주소변경) 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/changeDelivery/search/list", method = RequestMethod.GET)
	public ResponseEntity<ChangeDeliveryList> selectChangeDeliveryList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchChangeDeliveryList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<ChangeDeliveryList>(orderCommonCsService.selectChangeDeliveryList(search), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 송장교체(주소변경) > 엔트리
	 * @param entCode
	 * @param searchChangeDelivery
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "고객서비스 송장교체(주소변경) 상세 조회", notes="고객서비스 송장교체(주소변경) 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/changeDelivery/get/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<ChangeDelivery> selectChangeDeliveryDetail(@PathVariable("entCode") String entCode, SearchChangeDelivery search) throws Exception {
		return new ResponseEntity<ChangeDelivery>(orderCommonCsService.selectChangeDeliveryDetail(search), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 송장교체(주소변경) > 엔트리저장
	 * @param entCode
	 * @param changeDelivery
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "송장교체(주소변경) save", notes="송장교체(주소변경) 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/changeDelivery/saveChangeDelivery", method = RequestMethod.POST)
	public ResponseEntity<ChangeDeliverySaveResult> saveChangeDelivery(@PathVariable("entCode") String entCode, @RequestBody ChangeDelivery changeDelivery, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, changeDelivery, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ChangeDeliverySaveResult>(orderCommonCsService.saveChangeDelivery(changeDelivery, request), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 송장교체(주소변경) > Confirm
	 * @param entCode
	 * @param changeDelivery
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "송장교체(주소변경) Confirm", notes="송장교체(주소변경) Confirm합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/changeDelivery/confirmDelivery", method = RequestMethod.POST)
	public ResponseEntity<ChangeDeliverySaveResult> confirmDelivery(@PathVariable("entCode") String entCode, @RequestBody ChangeDelivery changeDelivery, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, changeDelivery, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ChangeDeliverySaveResult>(orderCommonCsService.confirmDelivery(changeDelivery, request), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 송장교체(주소변경) > 엑셀다운로드
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/changeDelivery/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectChangeDeliveryListExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchChangeDeliveryList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		ChangeDeliveryList orderList = orderCommonCsService.selectChangeDeliveryList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getChangeDeliveryList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("ChangeDelivery_List", map), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 퀵요청
	 * @param entCode
	 * @param SearchUnusualMngList
	 * @return
	 * @throws Exception
	 * 
	 */
	@ApiOperation(value = "고객서비스 퀵요청 조회", notes="퀵요청 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/quickRequest/search/list", method = RequestMethod.GET)
	public ResponseEntity<QuickRequestList> selectQuickRequestList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchQuickRequestList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<QuickRequestList>(orderCommonCsService.selectQuickRequestList(search), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 퀵요청 > 엔트리
	 * @param entCode
	 * @param SearchReturnMng
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "고객서비스 퀵요청 상세 조회", notes="고객서비스 퀵요청 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/quickRequest/get/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<QuickRequest> selectQuickRequestDetail(@PathVariable("entCode") String entCode, SearchQuickRequest search) throws Exception {
		return new ResponseEntity<QuickRequest>(orderCommonCsService.selectQuickRequestDetail(search), HttpStatus.OK);
	}
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 퀵요청 > 엔트리저장
	 * @param entCode
	 * @param changeDelivery
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "퀵요청 save", notes="퀵요청 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/quickRequest/saveQuickRequest", method = RequestMethod.POST)
	public ResponseEntity<QuickRequestSaveResult> saveQuickRequest(@PathVariable("entCode") String entCode, @RequestBody QuickRequest quickRequest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, quickRequest, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<QuickRequestSaveResult>(orderCommonCsService.saveQuickRequest(quickRequest, request), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 퀵요청 > 삭제
	 * @param entCode
	 * @param deleteDelivery
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "퀵요청 delete", notes="퀵요청 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/quickRequest/deleteQuickRequest", method = RequestMethod.DELETE)
	public ResponseEntity<QuickRequestSaveResult> deleteQuickRequest(@PathVariable("entCode") String entCode, @RequestBody QuickRequestDeleteList quickRequestDeleteList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, quickRequestDeleteList, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<QuickRequestSaveResult>(orderCommonCsService.deleteQuickRequest(quickRequestDeleteList, request), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 퀵요청 > 엑셀다운로드
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/quickRequest/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectQuickRequestListExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchQuickRequestList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		QuickRequestList orderList = orderCommonCsService.selectQuickRequestList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getQuickRequestList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("QuickRequest_List", map), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 반품관리
	 * @param entCode
	 * @param SearchUnusualMngList
	 * @return
	 * @throws Exception
	 * 
	 */
	@ApiOperation(value = "고객서비스 반품관리 조회", notes="반품관리 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/returnMng/search/list", method = RequestMethod.GET)
	public ResponseEntity<ReturnMngList> selectReturnMngList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchReturnMngList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<ReturnMngList>(orderCommonCsService.selectReturnMngList(search), HttpStatus.OK);
	}
	
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 반품관리 > 엑셀다운로드
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/returnMng/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectReturnMngListExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchReturnMngList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		ReturnMngList orderList = orderCommonCsService.selectReturnMngList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getReturnMngList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("RetunMng_List", map), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 반품관리 > function
	 * @param entCode
	 * @param changeDelivery
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "반품관리 function", notes="반품관리 function.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/returnMng/saveReturnMngFunction", method = RequestMethod.POST)
	public ResponseEntity<ReturnMngSaveResult> saveReturnMngFunction(@PathVariable("entCode") String entCode, @RequestBody SearchReturnMngList returnMngList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, returnMngList, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ReturnMngSaveResult>(orderCommonCsService.saveReturnMngFunction(returnMngList, request), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 반품관리 > 엔트리저장
	 * @param entCode
	 * @param changeDelivery
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "반품관리 save", notes="반품관리 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/returnMng/saveReturnMng", method = RequestMethod.POST)
	public ResponseEntity<ReturnMngSaveResult> saveReturnMng(@PathVariable("entCode") String entCode, @RequestBody ReturnMng returnMng, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, returnMng, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ReturnMngSaveResult>(orderCommonCsService.saveReturnMng(returnMng, request), HttpStatus.OK);
	}
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 반품관리 > 삭제
	 * @param entCode
	 * @param deleteDelivery
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "반품관리 delete", notes="반품관리 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/returnMng/deleteReturnMng", method = RequestMethod.DELETE)
	public ResponseEntity<ReturnMngSaveResult> saveRdeturnMng(@PathVariable("entCode") String entCode, @RequestBody ReturnMngDeleteList returnMngDeleteList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, returnMngDeleteList, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ReturnMngSaveResult>(orderCommonCsService.deleteReturnMng(returnMngDeleteList, request), HttpStatus.OK);
	}
	
	/**
	 * E-commerce > 고객서비스 >  E-commerce  > CS관리 > 이례화물관리
	 * @param entCode
	 * @param SearchUnusualMngList
	 * @return
	 * @throws Exception
	 * 
	 */
	@ApiOperation(value = "고객서비스 이례화물관리 조회", notes="이례화물관리 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/unusualMng/search/list", method = RequestMethod.GET)
	public ResponseEntity<UnusualMngList> selectUnusualMngList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchUnusualMngList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<UnusualMngList>(orderCommonCsService.selectUnusualMngList(search), HttpStatus.OK);
	}
	
	
	/**
	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 이례화물관리 > 엑셀다운로드
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/unusualMng/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectUnusualMngListExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchUnusualMngList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		UnusualMngList orderList = orderCommonCsService.selectUnusualMngList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getUnusualMngList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("UnusualMng_List", map), HttpStatus.OK);
	}
	
	
//	/**
//	 *  E-commerce > 고객서비스 >  E-commerce  > CS관리 > 이례화물관리 > 엔트리저장
//	 * @param entCode
//	 * @param changeDelivery
//	 * @param request
//	 * @return
//	 * @throws Exception
//	 */
//	@ApiOperation(value = "이례화물관리 save", notes="이례화물관리 저장합니다.")
//	@ApiImplicitParams({
//		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
//	})
//	@RequestMapping(value="/returnMng/saveUnusualMng", method = RequestMethod.POST)
//	public ResponseEntity<UnusualMngSaveResult> saveUnusualMng(@PathVariable("entCode") String entCode, @RequestBody UnusualMng returnMng, HttpServletRequest request) throws Exception {
//		Util.getSessionUserInfo(request, jwtTokenProvider, returnMng, "registerUserId", "logUserName", null, "logUserNameEng");
//		return new ResponseEntity<UnusualMngSaveResult>(orderCommonCsService.saveUnusualMng(returnMng, request), HttpStatus.OK);
//	}
	
	@ApiOperation(value = "고객서비스 Broker 팝업 파일 저장", notes="Broker수정 파일 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/brokerMSub/fileShare", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> saveBrokerFileShareUpdate(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
		BrokerMasterSub broker = new ObjectMapper().readValue(request.getParameter("params"), BrokerMasterSub.class);
		OrderSaveResult orderSaveResult = orderCommonCsService.saveBrokerFileShareUpdate(broker, request);
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
}