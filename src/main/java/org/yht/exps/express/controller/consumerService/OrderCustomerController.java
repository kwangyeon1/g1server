package org.yht.exps.express.controller.consumerService;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.mapper.consumerService.OrderCustomerMapper;
import org.yht.exps.express.mapper.sms.SmsCousMapper;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.consumerService.order.OrderCustomerDelete;
import org.yht.exps.express.model.consumerService.order.OrderCustomerItemList;
import org.yht.exps.express.model.consumerService.order.OrderCustomerList;
import org.yht.exps.express.model.consumerService.order.OrderCustomerMultiList;
import org.yht.exps.express.model.consumerService.order.OrderCustomerSaveResult;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomer;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.consumerService.OrderCustomerService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "orderCustomer")
@RequestMapping("/{entCode}/ecm/order/customer")
public class OrderCustomerController {
	
	@Resource(name = "orderCustomerService")
	OrderCustomerService orderCustomerService;
	@Resource(name = "commonService")
	CommonService commonService;
	
	@Autowired
	OrderCustomerMapper orderCustomerMapper;
	@Autowired
	SmsCousMapper smsCousMapper;
	@Autowired
	JwtTokenProvider jwtTokenProvider;


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
			return new ResponseEntity<OrderCustomerList>(orderCustomerService.selectOrderList(search), HttpStatus.OK);
		}
	}
	
	@ApiOperation(value = "전자상거래Customer Order 삭제", notes="Customer Order를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete/{odJobNo}", method = RequestMethod.DELETE)
	public ResponseEntity<OrderCustomerSaveResult> deleteOrder(HttpServletRequest request, @PathVariable("entCode") String entCode, @RequestBody Order orderCustomer) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, orderCustomer, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderCustomerSaveResult>(orderCustomerService.deleteOrder(request, orderCustomer), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래Customer Order 삭제일괄처리", notes="전자상거래Customer Order 일괄삭제처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete/batch", method = RequestMethod.DELETE)
	public ResponseEntity<OrderCustomerSaveResult> deleteOrderList(HttpServletRequest request, @PathVariable("entCode") String entCode, @RequestBody OrderCustomerDelete orderCustomerDelete) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, orderCustomerDelete, "userId", null, null);
		return new ResponseEntity<OrderCustomerSaveResult>(orderCustomerService.deleteOrderList(request, orderCustomerDelete), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Customer Order insert", notes="전자상거래 Customer Order 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderCustomerSaveResult> insertOrder(HttpServletRequest request, @PathVariable("entCode") String entCode, @RequestBody Order order) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, order, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderCustomerSaveResult>(orderCustomerService.saveOrder(request, order), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Customer Order update", notes="전자상거래Customer Order 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/{odJobNo}", method = RequestMethod.PUT)
	public ResponseEntity<OrderCustomerSaveResult> updateOrder(HttpServletRequest request, @PathVariable("entCode") String entCode, @RequestBody Order order) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, order, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderCustomerSaveResult>(orderCustomerService.saveOrder(request, order), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래Customer Order 상세 조회", notes="전자상거래 Customer Order 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<Order> selectOrderDetail(@PathVariable("entCode") String entCode, SearchOrderCustomer search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		return new ResponseEntity<Order>(orderCustomerService.selectOrderDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래Customer Order item list 조회", notes="전자상거래 Customer Order item list 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/item", method = RequestMethod.GET)
	public ResponseEntity<OrderCustomerItemList> selectOrderItem(@PathVariable("entCode") String entCode, SearchOrderCustomer search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		return new ResponseEntity<OrderCustomerItemList>(orderCustomerService.selectOrderItem(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래Customer Order item list 조회", notes="전자상거래 Customer Order item list 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/multi", method = RequestMethod.GET)
	public ResponseEntity<OrderCustomerMultiList> selectOrderMulti(@PathVariable("entCode") String entCode, SearchOrderCustomer search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		return new ResponseEntity<OrderCustomerMultiList>(orderCustomerService.selectOrderMulti(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래Customer Order Sub list 조회", notes="전자상거래 Customer Order Sub list 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/subOrder", method = RequestMethod.GET)
	public ResponseEntity<OrderCustomerItemList> selectOrderSub(@PathVariable("entCode") String entCode, SearchOrderCustomer search) throws Exception {
		return new ResponseEntity<OrderCustomerItemList>(orderCustomerService.selectOrderSub(search), HttpStatus.OK);
	}
		
	@ApiOperation(value = "전자상거래Customer Order function 할 bl list 조회", notes="전자상거래 Customer Order function 할 bl list 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/func/list", method = RequestMethod.GET)
	public ResponseEntity<OrderCustomerList> selectFuncList(@PathVariable("entCode") String entCode, SearchOrderCustomerList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		return new ResponseEntity<OrderCustomerList>(orderCustomerService.selectFuncList(search), HttpStatus.OK);
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
	public ResponseEntity<DownloadFileInfo> selectMultiWgListExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchOrderCustomerList search,
						HttpServletRequest request) throws Exception {
		
		Util.accessControlDatabase(search, "loginCustomerUpperLev|loginCustomerLowLev|userLanguage|loginCustomerLev|srchDateFormat", true, "srchDateGbn", "od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.CONSIGNEE_HP|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}
		
		search.setSrchMode("listCount");
		search.setPage(1);
		return new ResponseEntity<DownloadFileInfo>(commonService.makeExcelDownload(map, search, orderCustomerMapper, "selectOrderList", "org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList", false), HttpStatus.OK);
		
//		search.setPage(0);
//		OrderCustomerList orderList = orderCustomerService.selectOrderList(search);
//				 
//		ObjectMapper objectMapper = new ObjectMapper();
//		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
//		map.put("data", orderList.getOrderCustomerList());
//		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Order_List", map), HttpStatus.OK);
	}
	
	/**
	 * 엑셀 list post
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
	@RequestMapping(value="/search/list/excelPost", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectMultiWgListExcelPost(
						@PathVariable("entCode") String entCode,
						@RequestBody SearchOrderCustomerList search,
						HttpServletRequest request) throws Exception {
		
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		Util.accessControlDatabase(search, "loginCustomerUpperLev|loginCustomerLowLev|userLanguage|loginCustomerLev|srchDateFormat", true, "srchDateGbn", "od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.CONSIGNEE_HP|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}
		
		Util.getCryptographyYn(search);
		
		/** 일별로 엑셀 다운로드 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		long diffDate = Util.diffDate(search.getSrchEndDate(), search.getSrchStartDate());
		
		Date strDate = Util.strToDate(search.getSrchStartDate(), "yyyy-MM-dd");
		Date startDate = new Date();
		Date endDate = new Date();
		
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		
		start.setTime(strDate);
		end.setTime(strDate);
		
		end.add(Calendar.DATE, 1);
		
		List<DownloadFileInfo> downloadList = new ArrayList<DownloadFileInfo>();
		DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
		downloadFileInfo.setSeq(0); // 일별로 엑셀 다운로드시 NUM 항목값이 날짜별 ROW NUMBER로 들어가서 시퀀스로 대체
		for(int i=0; i<diffDate+1; i++) {
			if(i > 0) {
				start.add(Calendar.DATE, 1);
				end.add(Calendar.DATE, 1);
			}
			startDate = new Date(start.getTimeInMillis());
			endDate = new Date(end.getTimeInMillis());
			search.setSrchStartDate(sdf.format(startDate));
			search.setSrchEndDate(sdf.format(endDate));
			search.setSrchMode("listCount");
			if(Util.null2str(entCode).equals("ACGAK")||Util.null2str(entCode).equals("YHTXX")) {
				search.setDefaultSort("OB_DATE DESC, OD_JOB_NO");
			}
			search.setPage(1);
			downloadFileInfo = commonService.makeExcelDownload(map, search, orderCustomerMapper, "selectOrderList", "org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList", false);
			if(downloadFileInfo.getSeq() > 0) {
				map.put("seq", downloadFileInfo.getSeq());
			}
		}
		return new ResponseEntity<DownloadFileInfo>(downloadFileInfo, HttpStatus.OK);
	}

	@ApiOperation(value = "로그인 유저 거래처 발송수정여부 조회")
	@RequestMapping(value="/get/sendModifyYn", method = RequestMethod.GET)
	public ResponseEntity<String> selectSendModifyYn(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
		Map<String, Object> userInfo = Util.getSessionUserInfo(request, jwtTokenProvider);
		Map<String, Object> param = new HashMap<>();
		param.put("entCode", entCode);
		param.put("userId", userInfo.get("USER_ID"));
		String sendModifyYn = smsCousMapper.selectLoginUserSendModifyYn(param);
		return new ResponseEntity<String>(sendModifyYn == null ? "N" : sendModifyYn, HttpStatus.OK);
	}
}
