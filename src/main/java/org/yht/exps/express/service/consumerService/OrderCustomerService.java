package org.yht.exps.express.service.consumerService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.consumerService.OrderCustomerMapper;
import org.yht.exps.express.mapper.eCommerce.FunctionMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.consumerService.order.OrderCustomerDelete;
import org.yht.exps.express.model.consumerService.order.OrderCustomerItem;
import org.yht.exps.express.model.consumerService.order.OrderCustomerItemList;
import org.yht.exps.express.model.consumerService.order.OrderCustomerList;
import org.yht.exps.express.model.consumerService.order.OrderCustomerMulti;
import org.yht.exps.express.model.consumerService.order.OrderCustomerMultiList;
import org.yht.exps.express.model.consumerService.order.OrderCustomerSaveResult;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomer;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderItem;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.User;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.OrderCommonService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class OrderCustomerService {

	@Autowired
	CustomProperties customProperties;
	@Autowired
    CommonMapper commonMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	OrderCustomerMapper orderCustomerMapper;
	@Autowired
	EnterpriseMapper enterpriseMapper;
	@Autowired
	SmsUserService smsUserService;
	@Autowired
	StringRedisTemplate redisTemplate;
	@Autowired
    FunctionMapper functionMapper;
	
	@Resource(name = "orderCommonService")
	OrderCommonService orderCommonService;
	
	@Resource(name = "commonService")
	CommonService commonService;
	
	@Value("${spring.profiles.active}")
	private String profile;
	
	private Logger orderParamsLog = LoggerFactory.getLogger("orderParams");
	
	/**
	 * Order List
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCustomerList selectOrderList(SearchOrderCustomerList search) throws Exception {
		String logVal = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> OrderCustomerController.selectOrderList!! " + search.getSrchEntCode() + " / "
			+ search.getLoginId() + "(" + search.getUserName() + ") / " + search.getSrchDateGbn() + " / " + search.getSrchStartDate() + " ~ " + search.getSrchEndDate();
		orderParamsLog.info(logVal);
		search.setProfileName(profile);
//		orderParamsLog.debug(search.toString());
		
		//유저의 거래처코드 추출
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("entCode", search.getSrchEntCode());
		param.put("userId", search.getSrchLoginId());
		
		//Map<String,Object> userInfo = orderCustomerMapper.selectUserInfo(param);
		//if(userInfo != null) {
		//	search.setLoginCustomerCd((String)userInfo.get("CUSTOMER_CODE"));
		//	search.setLoginCustomerLev((String)userInfo.get("CUSTOMER_LEVEL"));
		//	search.setLoginCustomerUpperLev((String)userInfo.get("UPPER_LEVEL"));
		//	search.setLoginCustomerLowLev((String)userInfo.get("LOW_LEVEL"));
		//}
		
		OrderCustomerList orderCustomerList = new OrderCustomerList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
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
		//
		
		/* 로그인 유저의 거래처 레벨이 없으면 조회하지 않고 오류 표현 */
		List<Order> list = null;
		if(Util.null2str(search.getLoginCustomerLevNan()).equals("Y")) {
			list = new ArrayList<Order>();
			orderCustomerList.setResultCode("loginCustomerLevNan");
		}else {
			Util.getCryptographyYn(search);
			Util.accessControlDatabase(search, "loginCustomerUpperLev|loginCustomerLowLev|userLanguage|loginCustomerLev|srchDateFormat", true, "srchDateGbn", "od.PO_DATE|od.AR_DATE|od.OB_DATE|od.REGISTER_DATE_TIME", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.CONSIGNEE_HP|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
			list  = orderCustomerMapper.selectOrderList(search);
		}
//		/** 상/하위업체 조회 */
//		Class cls;
//		Field field;
//		String customerCode;
//		for(Order order : list) {
//			cls = order.getClass();
//			if(Util.null2str(search.getLoginCustomerCd()).equals("")){	/** 도입사 */
//				order.setUpperCusCode(order.getCustCodeLv1());
//				for(int i = 7; i >= 2; i--){
//					field = cls.getDeclaredField("custCodeLv" + i);
//					field.setAccessible(true);
//					customerCode = Util.null2str(field.get(order));
//					if(!customerCode.equals("")){
//						order.setLowCusCode(customerCode);
//						break;
//					}
//				}
//			}else{
//				field = cls.getDeclaredField("custCodeLv" + search.getLoginCustomerLev());
//				field.setAccessible(true);
//				order.setUpperCusCode(Util.null2str(field.get(order)));
//				field = cls.getDeclaredField("custCodeLv" + (Util.null2int(search.getLoginCustomerLev()) + 1));
//				field.setAccessible(true);
//				order.setLowCusCode(Util.null2str(field.get(order)));
//			}
//		}
		orderCustomerList.setOrderCustomerList(list);
		if(list.size() > 0) {
			orderCustomerList.setTotal(list.get(0).getTotal());
		}else {
			orderCustomerList.setTotal(0);
		}
		return orderCustomerList;
	}
	
	/**
	 * 전자상거래 Order - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCustomerSaveResult deleteOrder(HttpServletRequest request, Order orderCustomer) throws Exception {
		OrderCustomerSaveResult orderSaveResult = new OrderCustomerSaveResult();
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		/** 콘솔체크 */
		tmpMap.put("odJobNo", orderCustomer.getOdJobNo());
		tmpMap.put("chkInvoiceYn", "Y");
		tmpMap = orderMapper.selectOrderSaveChk(tmpMap);
		if(tmpMap != null && tmpMap.size() > 0){
			if(Util.null2int(tmpMap.get("CONSOL_YN_CNT")) > 0){
				orderSaveResult.setResultCode("A11215");	/** 이미 콘솔된 데이터가 존재합니다. */
				return orderSaveResult;
			}
			if(Util.null2int(tmpMap.get("INVOICE_CNT")) > 0){
				orderSaveResult.setResultCode("A11176");	/** 정산서가 발행된 건이 있어 변경할 수 없습니다. */
				return orderSaveResult;
			}
		}
		
		orderCustomerMapper.deleteOrder(orderCustomer);
		orderCustomerMapper.deleteOrderAdditional(orderCustomer);
		orderCustomerMapper.deleteOrderContainer(orderCustomer);
		orderCustomerMapper.deleteOrderItem(orderCustomer);
		orderCustomerMapper.deleteOrderSpecialItem(orderCustomer);
		orderCustomerMapper.deleteOrderCost(orderCustomer);
		orderCustomerMapper.deleteOrderMulti(orderCustomer);
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(orderCustomer.getEntCode());
		processLogs.setEntBusinessCode(orderCustomer.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(orderCustomer.getBound());
		processLogs.setBoundType(orderCustomer.getBoundType());
		processLogs.setProcessUserId(orderCustomer.getUpdateUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("DELETE");
		processLogs.setDescription("Delete Order [HBL_NO : "+ orderCustomer.getHblNo() +"]");
		processLogs.setJobLog(new Gson().toJson(orderCustomer));
		processLogs.setResult("ok");
		processLogs.setJobNo(orderCustomer.getOdJobNo());
		processLogs.setEntBusinessPrintName(orderCustomer.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(orderCustomer.getLogUserName());
		processLogs.setProcessUserNameEng(orderCustomer.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 일괄 Order - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCustomerSaveResult deleteOrderList(HttpServletRequest request, OrderCustomerDelete orderCustomerDelete) throws Exception {
		
		OrderCustomerSaveResult orderSaveResult = new OrderCustomerSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		if(orderCustomerDelete == null) {
			return null;
		}
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		/** 콘솔체크 */
		tmpMap.put("orderDeleteList", orderCustomerDelete.getOrderCustomerList());
		tmpMap.put("chkInvoiceYn", "Y");
		tmpMap = orderMapper.selectOrderSaveChk(tmpMap);
		if(tmpMap != null && tmpMap.size() > 0){
			if(Util.null2int(tmpMap.get("CONSOL_YN_CNT")) > 0){
				orderSaveResult.setResultCode("A11215");	/** 이미 콘솔된 데이터가 존재합니다. */
				return orderSaveResult;
			}
			if(Util.null2int(tmpMap.get("INVOICE_CNT")) > 0){
				orderSaveResult.setResultCode("A11176");	/** 정산서가 발행된 건이 있어 변경할 수 없습니다. */
				return orderSaveResult;
			}
		}
		
		List<Order> list = orderCustomerDelete.getOrderCustomerList();

		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", orderCustomerDelete.getEntCode());
		tmpLogMap.put("ent_business_code", orderCustomerDelete.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", orderCustomerDelete.getUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "DELETE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		
		// 사업장명 가져오기
		if (!"".equals(Util.null2str(orderCustomerDelete.getUserBusinessCode()))) {
			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(orderCustomerDelete.getUserBusinessCode());
			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
		}
		
		// 사용자명 가져오기
		if (!"".equals(Util.null2str(orderCustomerDelete.getUserId()))) {
			User user = smsUserService.getUser(orderCustomerDelete.getUserId());
			tmpLogMap.put("process_user_name", user.getUserName());
			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
		}
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		for(int i=0;i<list.size();i++) {
			Order orderCustomer = list.get(i);
			orderCustomerMapper.deleteOrder(orderCustomer);
			orderCustomerMapper.deleteOrderAdditional(orderCustomer);
			orderCustomerMapper.deleteOrderContainer(orderCustomer);
			orderCustomerMapper.deleteOrderItem(orderCustomer);
			orderCustomerMapper.deleteOrderSpecialItem(orderCustomer);
			orderCustomerMapper.deleteOrderCost(orderCustomer);
			orderCustomerMapper.deleteOrderMulti(orderCustomer);
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(i));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("job_no",orderCustomer.getOdJobNo());
			logMap.put("description", "등록/발송 > Delete Order [HBL_NO : "+ orderCustomer.getHblNo() +"]");
			logMap.put("bound", orderCustomer.getBound());
			logMap.put("bound_type", orderCustomer.getBoundType());
			logMap.put("job_log", "");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		
		}
		
		commonService.insertLogBulk(bulkList);
		return orderSaveResult;
	}
	
	
	/**
	 * 전자상거래 Order - insert
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCustomerSaveResult saveOrder(HttpServletRequest request, Order order) throws Exception {
		OrderCustomerSaveResult orderCustomerSaveResult = new OrderCustomerSaveResult();
		order.setProfileName(profile);
		String action = order.getAction();	/** insert/update구분 */
		ProcessLogs processLogs = new ProcessLogs();
		Util.accessControlDatabase(order, "gmtTime|dateFormat", false, null, null, null, null);
//		//오더번호 중복체크
//		String dupOrderNo = orderCustomerMapper.dupOrderNo(order);
//		if("Y".equals(dupOrderNo)) {
//			//오더번호가 존재하는지 중복체크
//			orderCustomerSaveResult.setResultCode("DUP_ORDER");
//			return orderCustomerSaveResult;
//		}
		
		Map<String,Object> tmpMap;
		Map<String, Object> orderSaveMap = new HashMap<String, Object>();
		
		if(action.equals("insert")){
			//거래처인 경우
			if(order.getLoginCustomerCode() != null && order.getLoginCustomerCode() != "") {
				//LVL1~7 추출
				Map<String,Object> paramMap = new HashMap<String,Object>();
				paramMap.put("entCode", order.getEntCode());
				paramMap.put("userId", order.getUpdateUserId());
				Map<String,Object> lvlMap = orderCustomerMapper.selectCusLvl(paramMap);
				
				if(lvlMap == null) {
					//로그인유저의 거래처코드가 없는 경우 에러 
					orderCustomerSaveResult.setResultCode("NOT_CUS_CODE");
					return orderCustomerSaveResult;
				}
				
				String userCusCd = (String)lvlMap.get("USER_CUSTOMER_CODE");
				//로그인유저의 거래처코드가 없는 경우 에러 
				if(StringUtils.isEmpty(userCusCd)) {
					orderCustomerSaveResult.setResultCode("NOT_CUS_CODE");
					return orderCustomerSaveResult;
				}
				
				//로그인유저의 거래처코드의 레벨로 상위계층레벨 거래처코드 셋팅 
				order.setCustCodeLv1((String)lvlMap.get("CUST_CODE_LV1"));
				order.setCustCodeLv2((String)lvlMap.get("CUST_CODE_LV2"));
				order.setCustCodeLv3((String)lvlMap.get("CUST_CODE_LV3"));
				order.setCustCodeLv4((String)lvlMap.get("CUST_CODE_LV4"));
				order.setCustCodeLv5((String)lvlMap.get("CUST_CODE_LV5"));
				order.setCustCodeLv6((String)lvlMap.get("CUST_CODE_LV6"));
				order.setCustCodeLv7((String)lvlMap.get("CUST_CODE_LV7"));
			}
			
		}else if(action.equals("update")){
			orderSaveMap.put("chkEditOrder", "Y");
		}
		
		/** 오더저장공통 */
		orderSaveMap.put("action", action);
		orderSaveMap.put("chkDupBlNo", "Y");
		if(action.equals("insert")){
			orderSaveMap.put("createOdJobNo", "Y");
		}
		
		if(!orderCommonService.orderSave(order, "cusOrder", orderSaveMap, 1)){
			if(orderSaveMap.get("duplicateBlNo") != null){	/** 비엘번호중복 */
				orderCustomerSaveResult.setResultCode("duplicateBlNo");	//TODO js에서 해당오류시 알림처리 추가해야함
			}else if(orderSaveMap.get("resultCode") != null){
				orderCustomerSaveResult.setResultCode(Util.null2str(orderSaveMap.get("resultCode")));
			}
			return orderCustomerSaveResult;
		}
		
//		/** 오더 PACKING의 오더관리번호 업데이트 */
//		if("IMP".equals(order.getBoundType()) && ("YHTXX".equals(order.getEntCode()) || "UPSXK".equals(order.getEntCode()))){
//			int packingCnt = 0;
//			if(Util.null2int(orderMapper.updatePackingOdJobNo(order)) > 0 || action.equals("update")){
//				Map<String, Object> packingMap = new HashMap<String, Object>();
//				packingMap.put("odJobNo", order.getOdJobNo());
//				packingMap.put("hblNo", order.getHblNo().trim().replace("\t", ""));
//				packingCnt = orderMapper.selectPackingCount(packingMap);
//			}
//			if(packingCnt != order.getPieces()){
//				order.setPackingErrorYn("Y");
//			}else{
//				order.setPackingErrorYn("N");
//			}
//		}
		
		if(action.equals("insert")){
			
			//insert 처리
			orderCustomerMapper.insertOrder(order);
			
			//insert TB_ECM_OD_ADDITIONAL 처리
			orderCustomerMapper.insertOrderAdditonal(order);
			
			//POD 테이블에 INSERT
			Map<String, Object> podInfo = new HashMap<String, Object>();
			
			podInfo.put("POD_STATUS", order.getStatus());
			podInfo.put("REMARKS", "");
			podInfo.put("SOLUTION", "ECM");
			podInfo.put("updateUserId", order.getUpdateUserId());
			podInfo.put("gmtTime", order.getGmtTime());
			podInfo.put("POD_JOB_NO",DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
			podInfo.put("entCode", order.getEntCode());
			podInfo.put("entBusinessCode", order.getEntBusinessCode());
			podInfo.put("userLanguage", order.getUserLanguage());
			podInfo.put("odJobNo", order.getOdJobNo());
			podInfo.put("hblNo", order.getHblNo());
			
			functionMapper.insertPodInfo(podInfo);
			
			processLogs.setJobFuncType("ADD");
			processLogs.setDescription("New Order [HBL_NO : "+order.getHblNo()+"]");
			
		}else if(action.equals("update")){
			if("IMP".equals(order.getBoundType()) && !"Y".equals(orderSaveMap.get("CD_TYPE")) && "Y".equals(order.getCdType()) && !Util.isEmpty(orderSaveMap.get("SIMPLE_DOC_NO"))){
				tmpMap = new HashMap<String,Object>();
				tmpMap.put("simpleDocNo", orderSaveMap.get("SIMPLE_DOC_NO"));
				tmpMap.put("srchType", "sendCnt");
				if(commonMapper.selectSimpleDocNoCount(tmpMap) > 0){
					order.setAction("delSimpleDocNo");
				}
			}
			//update 처리
			orderCustomerMapper.updateOrder(order);
			//update TB_ECM_OD_ADDITIONAL 처리
			orderCustomerMapper.updateOrderAdditonal(order);
			order.setAction("update");
			//item 삭제
			orderCustomerMapper.deleteOrderItem(order);
			//po_info 삭제
			if("IMP".equals(Util.null2str(order.getBoundType()))) {
				Map<String,Object> poDeleteMap = new HashMap<String,Object>();
				poDeleteMap.put("entCode", order.getEntCode());
				poDeleteMap.put("odJobNo", order.getOdJobNo());
				orderMapper.deletePoInfo(poDeleteMap);
				
				if("local".equals(profile) || "hubn".equals(profile)) {
					//멀티 삭제
					orderMapper.deleteMulti(poDeleteMap);	
				}
			}
			
			tmpMap = new HashMap<String,Object>();
			tmpMap.put("odJobNo", order.getOdJobNo());
			tmpMap.put("hblNo", order.getHblNo());
			orderMapper.updatePodBlNo(tmpMap);
			
			processLogs.setJobFuncType("UPDATE");
			processLogs.setDescription("Modify Order [HBL_NO : "+order.getHblNo()+"]");
		}
		
		Map<String,Object> poInsertMap = new HashMap<String,Object>();
		List<String> poInfoList = order.getPoInfoList();
		if(poInfoList != null && poInfoList.size() > 0) {
			poInsertMap.put("poInfoList", poInfoList);
			poInsertMap.put("odJobNo", order.getOdJobNo());
			orderMapper.insertPoInfo(poInsertMap);
		}
		
		List<OrderItem> itemList = order.getItemList();
		if(itemList !=null) {
			DateTimeFormatter inputFormatter  = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for(int i=0;i<itemList.size();i++) {
                OrderItem item = itemList.get(i);
                item.setSeqNoId(i+1);

                if("IMP".equals(order.getBoundType())){
                    if(!"".equals(Util.null2str(item.getOrderEndDatetime()))) {
                        String formatted = LocalDateTime.parse(item.getOrderEndDatetime(), inputFormatter)
                                .format(outputFormatter);

                        item.setOrderEndDatetime(formatted);
                    }else{
                        item.setOrderEndDatetime(null);
                    }
                }
            }
			
		}
		//item 저장
		orderCustomerMapper.insertItem(order);
		//sub 저장
		orderCustomerMapper.deleteSub(order);
		orderCustomerMapper.updateSub(order);
		
		if("local".equals(profile) || "bms11".equals(profile) || "hubn1".equals(profile)) {
			if("IMP".equals(order.getBoundType())) {
				//수정/재저장 시 PK(OD_JOB_NO, SEQ_NO_ID) 중복 방지를 위해 기존 멀티 삭제 후 재삽입
				orderCustomerMapper.deleteOrderMulti(order);
				orderCustomerMapper.insertMulti(order);
			}
		}
		
//		//수량 중량 볼륨 금액 업데이트
//		orderCustomerMapper.updateSumOrder(order);
		
		/** 오더 PACKING의 오더관리번호 업데이트 */
		if("IMP".equals(order.getBoundType()) && ("YHTXX".equals(order.getEntCode()) || "UPSXK".equals(order.getEntCode()))){
			orderMapper.updatePackingOdJobNo(order);
			
			tmpMap = new HashMap<String,Object>();
			tmpMap.put("odJobNo", order.getOdJobNo());
			commonMapper.updatePackingErrorYn(tmpMap);
		}
		
		processLogs.setEntCode(order.getEntCode());
		processLogs.setEntBusinessCode(order.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(order.getBound());
		processLogs.setBoundType(order.getBoundType());
		processLogs.setProcessUserId(order.getUpdateUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobLog(new Gson().toJson(order));
		processLogs.setResult("ok");
		processLogs.setJobNo(order.getOdJobNo());
		processLogs.setEntBusinessPrintName(order.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(order.getLogUserName());
		processLogs.setProcessUserNameEng(order.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		//오더번호 셋팅
		orderCustomerSaveResult.setOdJobNo(order.getOdJobNo());
		
		if(action.equals("update") && !Util.null2str(orderSaveMap.get("dbCdType")).equals(order.getCdType())){
			Map<String, Object> updateCdTypeMap = new HashMap<String, Object>();
			updateCdTypeMap.put("odJobNo", order.getOdJobNo());
			updateCdTypeMap.put("cdType", order.getCdType());
			commonMapper.updateBmsCdType(updateCdTypeMap);
			
			if("Y".equals(orderSaveMap.get("dbCdType"))){
				tmpMap = new HashMap<String,Object>();
				tmpMap.put("odJobNo", order.getOdJobNo());
				tmpMap = orderMapper.selectOrderChk(tmpMap);
				tmpMap.put("CD_TYPE", "Y");
				tmpMap.put("cdType", order.getCdType());
				commonService.insertBmsCargoProgress(tmpMap);
			}
		}
		
		return orderCustomerSaveResult;
	}
	
	/**
	 * Order Detail
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Order selectOrderDetail(SearchOrderCustomer search) throws Exception {
		//유저의 거래처코드 추출
		/*
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("entCode", search.getEntCode());
		param.put("userId", search.getSrchLoginId());
		Map<String,Object> userInfo = orderCustomerMapper.selectUserInfo(param);
		if(userInfo != null) {
			search.setLoginCustomerCd((String)userInfo.get("CUSTOMER_CODE"));
			search.setLoginCustomerLev((String)userInfo.get("CUSTOMER_LEVEL"));
			search.setLoginCustomerUpperLev((String)userInfo.get("UPPER_LEVEL"));
			search.setLoginCustomerLowLev((String)userInfo.get("LOW_LEVEL"));
		}		
		*/
		Util.accessControlDatabase(search, "userLanguage|loginCustomerUpperLev|loginCustomerLowLev", false, null, null, null, null);
		search.setProfileName(profile);
		Order order  = orderCustomerMapper.selectOrder(search);
		if(order != null && "IMP".equals(order.getBoundType())){
			order.setPoInfoList(orderCustomerMapper.selectPoInfo(search));
		}
		
		return order;
	}
	
	/**
	 * Order Item List
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCustomerItemList selectOrderItem(SearchOrderCustomer search) throws Exception {
		
		OrderCustomerItemList orderCustomerItemList = new OrderCustomerItemList();
		search.setProfileName(profile);
		List<OrderCustomerItem> list = orderCustomerMapper.selectOrderItem(search);
		orderCustomerItemList.setOrderCustomerItemList(list);
		
		if(list != null && list.size() > 0) {
			orderCustomerItemList.setTotal(list.size());
		}else {
			orderCustomerItemList.setTotal(0);
		}
		
		return orderCustomerItemList;
	}
	
	/**
	 * Order Sub List
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCustomerItemList selectOrderSub(SearchOrderCustomer search) throws Exception {
		
		OrderCustomerItemList orderCustomerItemList = new OrderCustomerItemList();
		List<OrderCustomerItem> list = orderCustomerMapper.selectOrderSub(search);
		orderCustomerItemList.setOrderCustomerItemList(list);
		
		if(list != null && list.size() > 0) {
			orderCustomerItemList.setTotal(list.size());
		}else {
			orderCustomerItemList.setTotal(0);
		}
		
		return orderCustomerItemList;
	}
	
	/**
	 * 전자상거래Customer Order function 할 bl list 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCustomerList selectFuncList(SearchOrderCustomerList search) throws Exception {
		
		String putNo = Util.null2str(search.getPutNo());
		
		//유저의 거래처코드 추출
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("entCode", search.getSrchEntCode());
		param.put("userId", search.getSrchLoginId());
		Map<String,Object> userInfo = orderCustomerMapper.selectUserInfo(param);
		if(userInfo != null) {
			search.setLoginCustomerCd((String)userInfo.get("CUSTOMER_CODE"));
			search.setLoginCustomerLev((String)userInfo.get("CUSTOMER_LEVEL"));
			search.setLoginCustomerUpperLev((String)userInfo.get("UPPER_LEVEL"));
			search.setLoginCustomerLowLev((String)userInfo.get("LOW_LEVEL"));
		}
		
		//전체 건 검색
		OrderCustomerList orderCustomerList = new OrderCustomerList();
		search.setPage(0);
		
		String funcNo = "";
		List<String> dataList = new ArrayList<String>();
		Util.getCryptographyYn(search);
		Util.accessControlDatabase(search, "loginCustomerUpperLev|loginCustomerLowLev|userLanguage|loginCustomerLev|srchDateFormat", true, "srchDateGbn", "|od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO|od.PO_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.CONSIGNEE_HP|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		List<Order> list  = orderCustomerMapper.selectOrderList(search);				
		if(list == null) {
			return orderCustomerList;
		}
		
		Map<String, Object> resultData = new HashMap<String, Object>();
		for (int i = 0; i < list.size(); i++) {
			Order row = list.get(i);
			
			if("BL".equals(putNo)) {
				funcNo = row.getHblNo();
			}
			else if("OD".equals(putNo)) {
				funcNo = row.getOdJobNo();
			}
			
			dataList.add(funcNo);
		}
		
		resultData.put("dataList", dataList);
		resultData.put("dataSize", dataList.size());
		orderCustomerList.setResultData(resultData);
		orderCustomerList.setResultCode("200");	
		
		return orderCustomerList;
	}
	
	/**
	 * Order multi List
	 * @param search
	 * @return
	 */
	public OrderCustomerMultiList selectOrderMulti(SearchOrderCustomer search) {
		OrderCustomerMultiList orderCustomerMultiList = new OrderCustomerMultiList();
		List<OrderCustomerMulti> list = orderCustomerMapper.selectOrderMulti(search);
		orderCustomerMultiList.setOrderCustomerMultiList(list);
		
		if(list != null && list.size() > 0) {
			orderCustomerMultiList.setTotal(list.size());
		}else {
			orderCustomerMultiList.setTotal(0);
		}
		
		return orderCustomerMultiList;
	}
}