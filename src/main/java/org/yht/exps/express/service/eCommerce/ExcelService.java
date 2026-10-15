package org.yht.exps.express.service.eCommerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.api.SearchApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.elasticsearch.client.data.SortData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.model.eCommerce.order.ExcelError;
import org.yht.exps.express.model.eCommerce.order.ExcelErrorCondition;
import org.yht.exps.express.model.eCommerce.order.ExcelErrorInsertList;
import org.yht.exps.express.model.eCommerce.order.ExcelErrorList;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadResult;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadResultList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderContainer;
import org.yht.exps.express.model.eCommerce.order.OrderItem;
import org.yht.exps.express.model.eCommerce.order.OrderLicense;
import org.yht.exps.express.model.eCommerce.order.OrderMulti;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchExcelUploadResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrder;
import org.yht.exps.express.model.search.SearchElastic;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.service.OrderCommonService;
import org.yht.util.Converter;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@Slf4j
@Service
@Transactional
public class ExcelService {
	
	@Resource(name = "orderService")
	OrderService orderService;
	@Resource(name = "orderCommonService")
	OrderCommonService orderCommonService;
	
	@Autowired
	StringRedisTemplate redisTemplate;
	
	@Autowired
	RestHighLevelClient client;
	
	@Autowired
    OrderMapper orderMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
    
    @Value("${custom.elasticsearch-scroll-page}")
    private int elasticsearchScrollPage_;
    
    /**
     * 엑셀 업로드 결과 리스트 조회
     * @param searchExcelUploadResult
     * @return
     * @throws Exception
     */
	public ExcelUploadResultList selectResultList(SearchExcelUploadResult searchExcelUploadResult) throws Exception {
		ExcelUploadResultList excelUploadResultList = new ExcelUploadResultList();

        try {
			// 검색할 Indices
	        String[] indices = new String[1];
	        if("invoice".equals(searchExcelUploadResult.getSrchBusinessType())){
	        	indices[0] = "ecm-invoice-excel-upload-result-*";
	        }else{
	        	indices[0] = "ecm-excel-upload-result-*";
	        }
	        
	        // Sort 조건 처리
	        List<SortData> sortList = new ArrayList<SortData>();

	        if (searchExcelUploadResult.getSort() == null || searchExcelUploadResult.getSort().size() == 0) {
		        SortData sortData = new SortData();
		        sortData.setField("register_date_time");
		        sortData.setType(SortOrder.DESC);
		        sortList.add(sortData);
	        } else {
	        	for (int i = 0; i < searchExcelUploadResult.getSort().size(); i++) {
	        		Sort data = searchExcelUploadResult.getSort().get(i);
	    	        SortData sortData = new SortData();
			        sortData.setField(Converter.camelToSnake(data.getField()));
			        if ("asc".equals(data.getDir()))	sortData.setType(SortOrder.ASC);
			        else								sortData.setType(SortOrder.DESC);
			        sortList.add(sortData);
	        	}
	        }
	        
	        // Parameter 처리
			int size = searchExcelUploadResult.getPageSize();
	        int from = searchExcelUploadResult.getPage() > 0 ? (searchExcelUploadResult.getPage() - 1) * size : 0;
	        long startDate = searchExcelUploadResult.getSrchStartDate();
	        long endDate = searchExcelUploadResult.getSrchEndDate();

	        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
	        boolQueryBuilder.filter(QueryBuilders.matchQuery("ent_code", searchExcelUploadResult.getSrchEntCode()));
	        if(!"".equals(Util.null2str(searchExcelUploadResult.getSrchEntBusinessCode()))) {
	        	boolQueryBuilder.filter(QueryBuilders.matchQuery("ent_business_code", searchExcelUploadResult.getSrchEntBusinessCode()));
	        }
	        if(!"".equals(Util.null2str(searchExcelUploadResult.getSrchBound()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("bound", searchExcelUploadResult.getSrchBound()));
	        }
	        if(!"".equals(Util.null2str(searchExcelUploadResult.getSrchBoundType()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("bound_type", searchExcelUploadResult.getSrchBoundType()));
	        }
	        if(!"".equals(Util.null2str(searchExcelUploadResult.getSrchExcelType()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("excel_type", searchExcelUploadResult.getSrchExcelType()));
	        }
	        if(!"".equals(Util.null2str(searchExcelUploadResult.getSrchRegisterUserId()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("register_user_id", searchExcelUploadResult.getSrchRegisterUserId()));
	        }
	        if(!"".equals(Util.null2str(searchExcelUploadResult.getSrchStatus()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("status", searchExcelUploadResult.getSrchStatus().toLowerCase()));
	        }
        	boolQueryBuilder.filter(QueryBuilders.rangeQuery(searchExcelUploadResult.getSrchDateGbn()).gte(startDate).lte(endDate));

	        
	        SearchApi searchApi = new SearchApi(client);
	        SearchResponse response;
	        if (size == 0)	response = searchApi.searchScroll(boolQueryBuilder, elasticsearchScrollPage_, indices, searchExcelUploadResult.getSrchScrollId(), sortList);
        	else			response = searchApi.search(boolQueryBuilder, from, size, 0, indices, sortList);
	        
	        if (response != null) {
	        	// 대기순번 검색을 위한 Redis 데이터 리스트
	        	JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
				String key = "E-COMMERCE_UPLOAD_EXCEL_" + searchExcelUploadResult.getSrchEntBusinessCode();
				ListOperations<String, String> listOperations = redisTemplate.opsForList();
				Long redisListSize = listOperations.size(key);
				
		        String scrollId = response.getScrollId();
		    	SearchHits hits = response.getHits();
		    	excelUploadResultList.setScrollId(scrollId);

				final ObjectMapper mapper = new ObjectMapper();
				mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				mapper.setSerializationInclusion(Include.NON_NULL);
				
		    	SearchHit[] resultHits = hits.getHits();
		    	if (size == 0)	excelUploadResultList.setTotal(resultHits.length);
		    	else			excelUploadResultList.setTotal(hits.getTotalHits().value);
		    	
		    	List<ExcelUploadResult> list = new ArrayList<ExcelUploadResult>();
		    	if (resultHits.length > 0) {
		    		for (SearchHit hit : resultHits) {
						ExcelUploadResult excelUploadResult = mapper.convertValue(hit.getSourceAsMap(), ExcelUploadResult.class);
						excelUploadResult.setIndexName(hit.getIndex());
						// 대기순번 세팅
						int order = 0;
						if (redisListSize > 0) {
							for (int i = 0; i < redisListSize; i++) {
								String result = listOperations.index(key, i);
								Object obj = parser.parse(result);
								JSONObject jsonObj = (JSONObject) obj;
								if (jsonObj.get("entBusinessCode").equals(excelUploadResult.getEntBusinessCode())) {
									order++;
									if (jsonObj.get("resultId").equals(excelUploadResult.getReferenceNoId())) {
										excelUploadResult.setOrder(order);
										break;
									}
								}
							}
						}
						
						list.add(excelUploadResult);
					}
					
		    	} else {
		    		// 메모리 확보를 위해 ScrollId를 ElasticSearch에서 삭제한다.
		    		if (!"".equals(Util.null2str(searchExcelUploadResult.getSrchScrollId()))) {
		    			searchApi.clearScollRequest(searchExcelUploadResult.getSrchScrollId());
		    		}
		    	}
				
		    	excelUploadResultList.setExcelUploadResultList(list);
	        }
			
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		} finally {}
        
		return excelUploadResultList;
	}
	
	/**
	 * 엑셀 업로드 에러 리스트 조회
	 * @param index
	 * @param searchElastic
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ExcelErrorList selectErrorList(String index, SearchElastic searchElastic) throws Exception {
		ExcelErrorList excelErrorList = new ExcelErrorList();

		try {
			BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
	        
			// 검색할 Indices
	        String[] indices = new String[1];
	        indices[0] = index;
	        if(index.indexOf("ecm-excel-upload-error-ref") > -1){
	        	boolQueryBuilder.filter(QueryBuilders.matchQuery("reference_no_id", searchElastic.getReferenceNoId()));
	        }
	        
	        // Sort 조건 처리
	        List<SortData> sortList = new ArrayList<SortData>();
	        SortData sortData = new SortData();
	        sortData.setField("line");
	        sortData.setType(SortOrder.ASC);
	        sortList.add(sortData);
	        
	        // Parameter 처리
			int size = searchElastic.getPageSize();
	        int from = searchElastic.getPage() > 0 ? (searchElastic.getPage() - 1) * size : 0;

	        SearchApi searchApi = new SearchApi(client);
	        SearchResponse response;
	        if (size == 0)	response = searchApi.searchScroll(boolQueryBuilder, elasticsearchScrollPage_, indices, searchElastic.getSrchScrollId(), sortList);
        	else			response = searchApi.search(boolQueryBuilder, from, size, 0, indices, sortList);
	        
	        if (response != null) {
		        String scrollId = response.getScrollId();
		    	SearchHits hits = response.getHits();
		    	excelErrorList.setScrollId(scrollId);
		    	
				final ObjectMapper mapper = new ObjectMapper();
				mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				mapper.setSerializationInclusion(Include.NON_NULL);
				
		    	SearchHit[] resultHits = hits.getHits();
		    	if (size == 0)	excelErrorList.setTotal(resultHits.length);
		    	else			excelErrorList.setTotal(hits.getTotalHits().value);
		    	
		    	List<ExcelError> list = new ArrayList<ExcelError>();
		    	if (resultHits.length > 0) {
		    		for (SearchHit hit : resultHits) {
						Map<String, Object> source = hit.getSourceAsMap();
						
						
						List<HashMap<String, Object>> errorFieldList = (List<HashMap<String, Object>>) source.get("error_fields");
						if (errorFieldList != null && errorFieldList.size() > 0) {
							for (Map<String, Object> errorField : errorFieldList) {
								ExcelError excelError = mapper.convertValue(source, ExcelError.class);
								excelError.setErrorType(Util.null2str(errorField.get("error_type")));
								excelError.setTable(Util.null2str(errorField.get("table")));
								excelError.setFieldValue(Util.null2str(errorField.get("field_value")));
								excelError.setField(Util.null2str(errorField.get("field")));
								excelError.setFieldName(Util.null2str(errorField.get("field_name")));
								excelError.setErrorCondition(mapper.convertValue(errorField.get("error_condition"), ExcelErrorCondition.class));
								
								list.add(excelError);
							}
						} else {
							ExcelError excelError = mapper.convertValue(source, ExcelError.class);
							list.add(excelError);
						}
					}
	
		    	} else {
		    		// 메모리 확보를 위해 ScrollId를 ElasticSearch에서 삭제한다.
		    		if (!"".equals(Util.null2str(searchElastic.getSrchScrollId()))) {
		    			searchApi.clearScollRequest(searchElastic.getSrchScrollId());
		    		}
		    	}
				
		    	excelErrorList.setExcelErrorList(list);
	        }
			
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		} finally {}
        
		return excelErrorList;
	}
	
	/**
	 * 엑셀 업로드 오류 내역 수정 저장
	 * @param orderList
	 * @param resultIndex
	 * @param resultId
	 * @param errorIndex
	 */
	public void insertOrderList(ExcelErrorInsertList insertData, String resultIndex, String resultId, String errorIndex, OrderSaveResult orderSaveResult) throws Exception{
		List<Order> orderList = insertData.getOrderList();
		List<OrderContainer> cntrList = insertData.getOrderContainerList();
		List<OrderLicense> licenseList = insertData.getOrderLicenseList();
		List<OrderItem> itemList = insertData.getOrderItemList();
		Map<String, String> chkMap = new HashMap<String, String>();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(insertData, "dateFormat|gmtTime|userLanguage", false, null, null, null, null);
		Map<String, Object> tmpMap;
		String userId = insertData.getUserId();
		String cryptographyYn = "";
		Map<String, Object> entMap = commonMapper.selectEnterpriseInfo(insertData.getEntCode());
		if(entMap != null && entMap.size() > 0){
			cryptographyYn = Util.null2str(entMap.get("ENCRYPTION_YN"));
		}
		
		/** 하우스오더별 로그 */
		Map<String, Order> logData = new HashMap<String, Order>();
		Order logOrder;
		OrderContainer logContainer;
		OrderLicense logLicense;
		OrderItem logItem;
		List<OrderContainer> logCntrList;
		List<OrderLicense> logLicenseList;
		List<OrderItem> logItemList;
		/** 오더 건수 */
		Map<String, Order> cntData = new HashMap<String, Order>();
		
		if(orderList != null && orderList.size() > 0){
			for(Order order : orderList){
				order.setRegisterUserId(userId);
				logOrder = order.clone();
				logData.put(order.getOdJobNo(), logOrder);
				cntData.put(order.getOdJobNo(), logOrder);
			}
		}
		
		if(cntrList != null && cntrList.size() > 0){
			for(OrderContainer container : cntrList){
				logCntrList = null;
				logContainer = container.clone();
				logOrder = logData.get(logContainer.getOdJobNo());
				if(logOrder == null){
					logOrder = new Order();
					logOrder.setOdJobNo(logContainer.getOdJobNo());
				}else{
					logCntrList = logOrder.getContainerList();
				}
				if(logCntrList == null){
					logCntrList = new ArrayList<OrderContainer>();
				}
				logCntrList.add(logContainer);
				logOrder.setContainerList(logCntrList);
				logData.put(logOrder.getOdJobNo(), logOrder);
			}
		}
		
		if(licenseList != null && licenseList.size() > 0){
			for(OrderLicense license : licenseList){
				license.setRegisterUserId(userId);
				logLicenseList = null;
				logLicense = license.clone();
				logOrder = logData.get(logLicense.getOdJobNo());
				if(logOrder == null){
					logOrder = new Order();
					logOrder.setOdJobNo(logLicense.getOdJobNo());
				}else{
					logLicenseList = logOrder.getLicenseList();
				}
				if(logLicenseList == null){
					logLicenseList = new ArrayList<OrderLicense>();
				}
				logLicenseList.add(logLicense);
				logOrder.setLicenseList(logLicenseList);
				logData.put(logOrder.getOdJobNo(), logOrder);
			}
		}
		
		if(itemList != null && itemList.size() > 0){
			for(OrderItem item : itemList){
				logItemList = null;
				logItem = item.clone();
				logOrder = logData.get(logItem.getOdJobNo());
				if(logOrder == null){
					logOrder = new Order();
					logOrder.setOdJobNo(logItem.getOdJobNo());
				}else{
					logItemList = logOrder.getItemList();
				}
				if(logItemList == null){
					logItemList = new ArrayList<OrderItem>();
				}
				logItemList.add(logItem);
				logOrder.setItemList(logItemList);
				logData.put(logOrder.getOdJobNo(), logOrder);
			}
		}
		
		
		
		String errorStr = "";
		Map<String, Object> map = new HashMap<String, Object>();
		if(cntrList != null && cntrList.size() > 0){
			for(OrderContainer container : cntrList){
				if(chkMap.get(container.getOdJobNo() + container.getCntrNo()) != null){
					errorStr += "컨테이너번호 " + container.getCntrNo() + "는 이미 등록되어 있습니다.<br />";
				}else{
					chkMap.put(container.getOdJobNo() + container.getCntrNo(), "Y");
				}
			}
			map.put("dataList", cntrList);
			List<Map<String, Object>> dupCntrNoList = orderMapper.selectCntrNoDupChk(map);
			if(dupCntrNoList.size() > 0){
				for(Map<String, Object> dupCntrNo : dupCntrNoList){
					errorStr += "컨테이너번호 " + Util.null2str(dupCntrNo.get("CNTR_NO")) + "는 이미 등록되어 있습니다.<br />";
				}
			}
		}
		if(licenseList != null && licenseList.size() > 0){
			chkMap.clear();
			for(OrderLicense license : licenseList){
				if(chkMap.get(license.getOdJobNo() + license.getLicenseNo()) != null){
					errorStr += "수출면장번호 " + license.getLicenseNo() + "는 이미 등록되어 있습니다.<br />";
				}else{
					chkMap.put(license.getOdJobNo() + license.getLicenseNo(), "Y");
				}
			}
			map.put("dataList", licenseList);
			List<Map<String, Object>> dupLicenseNoList = orderMapper.selectLicenseNoDupChk(map);
			if(dupLicenseNoList.size() > 0){
				for(Map<String, Object> dupLicenseNo : dupLicenseNoList){
					errorStr += "수출면장번호 " + Util.null2str(dupLicenseNo.get("LICENSE_NO")) + "는 이미 등록되어 있습니다.<br />";
				}
			}
		}
		
		List<String> poInfoList = null;
		List<OrderMulti> multiList = null;
		String[] poNoArr;
		String[] multiDeliveryNo = null;
		boolean poNoError;
		boolean multiError;
		
		
		if(orderList != null && orderList.size() > 0){
			for(Order order : orderList){
				if(!Util.isEmpty(order.getOrderNo())){
					poInfoList = new ArrayList<String>();
					poNoArr = order.getOrderNo().replace(" ", "").split(",");
					poNoError = false;
					
					for(int poIdx = 0;  poIdx < poNoArr.length; poIdx++){
						if(poNoArr[poIdx].length() > 35){
							poNoError = true;
							break;
						}
						if(poNoArr[poIdx].length() != 0){
							poInfoList.add(poNoArr[poIdx]);
						}
					}
					
					if(poNoError || poInfoList.size() > 50){
						errorStr += "ORDER_NO(주문번호) " + order.getOrderNo() + "가 최대 입력값을 초과하였습니다.<br />";
					}else if(poInfoList.size() > 0){
						order.setPoInfoList(poInfoList);
						logOrder = logData.get(order.getOdJobNo());
						if(logOrder != null){
							logOrder.setPoInfoList(poInfoList);
							logData.put(order.getOdJobNo(), logOrder);
						}
					}
				}
				
				
				if (!Util.isEmpty(order.getHblNo()) && !Util.isEmpty(order.getMultiDeliveryNo())) {
					multiList = new ArrayList<OrderMulti>();
					multiDeliveryNo = order.getMultiDeliveryNo().replace(" ", "").split(",");
					multiError = false;
					for(String deliveryNo : multiDeliveryNo){
						if(Util.isEmpty(deliveryNo)) {	/** 문자가 ",ABCDE"일경우 ABCDE만 입력되도록 */
							continue;
						}
						if(deliveryNo.length() > 30){
							multiError = true;
							break;
						}
						OrderMulti logOdMulti = new OrderMulti();
						logOdMulti.setDeliveryNo(deliveryNo);
						OrderMulti odMulti = new OrderMulti();
						odMulti.setOdJobNo(order.getOdJobNo());
//						odMulti.setSeqNoId(multiCnt++);
						odMulti.setDeliveryNo(deliveryNo);
						odMulti.setDeliveryCode(order.getDeliveryCode());
						odMulti.setPaymentGubn("01");
						odMulti.setRegisterUserId(order.getRegisterUserId());
//						odMulti.setGmtTime(insertData.getGmtTime());
						multiList.add(odMulti);
					}
					if(multiError){
						errorStr += "MULTI_DELIVERY_NO(멀티송장번호) " + order.getMultiDeliveryNo() + "가 최대 입력값을 초과하였습니다.<br />";
					}else{
						order.setMultiList(multiList);
					}
				}
			}
		}
		
		
		if(errorStr.length() > 0){
			orderSaveResult.setResultCode("ERROR");
			map.clear();
			map.put("errorStr", errorStr);
			orderSaveResult.setResultData(map);
			return;
		}
		
		Order masterOrder = null;
		Order tmpOrder = null;
		SearchOrder search = new SearchOrder();
		search.setUserLanguage(insertData.getUserLanguage());
		search.setSrchEntCode(insertData.getEntCode());
		search.setDateFormat(insertData.getDateFormat());
		search.setCryptographyYn(cryptographyYn);
		
		Map<String, List<OrderItem>> orderDataAdd = new HashMap<String, List<OrderItem>>();
		List<OrderItem> errorItemList;
		List<OrderItem> dbItemList;
		Map<String, Object> userInfoMap = null;
		Map<String, Object> cntrInfoMap = null;
		Map<String, Object> exrateMap = null;
		Map<String, Object> orderSaveMap = new HashMap<String, Object>();
		map.clear();
		map.put("dateFormat", insertData.getDateFormat());
		map.put("userId", insertData.getUserId());
		map.put("entCode", insertData.getEntCode());
		map.put("gmtTime", insertData.getGmtTime());
		map.put("entBusinessCode", "");
		map.put("updateUserId", insertData.getUserId());
		map.put("solution", "ECM");
		map.put("stockType", "OD");
		map.put("stockSeqLength", "8");
		map.put("dateLength", "2");
		map.put("signType", "noHyphen");
		map.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		map.put("groupCode", "CONTAINER");
		
		boolean masterData = false;
		int seqNo = 1;
		
		if (orderList != null && orderList.size() > 0){
			for(Order order : orderList){
				if(order.getBlTypeMh().equals("M")){
					masterOrder = order;
					break;
				}
			}
		}
		if(masterOrder != null){	/** 엑셀오류내역에 마스터정보가 있을경우 */
			tmpOrder = new Order();
			tmpOrder.setMasterOdJobNo(masterOrder.getOdJobNo());
			tmpOrder.setBound(masterOrder.getBound());
			tmpOrder.setBoundType(masterOrder.getBoundType());
			tmpOrder.setObDate(masterOrder.getObDate());
			tmpOrder.setArDate(masterOrder.getArDate());
			tmpOrder.setOrigCode(masterOrder.getOrigCode());
			tmpOrder.setDestCode(masterOrder.getDestCode());
			tmpOrder.setDestPort(masterOrder.getDestPort());
			
		}else{
			if(orderList != null && orderList.size() > 0){
				tmpOrder = new Order();
				tmpOrder.setMasterOdJobNo(orderList.get(0).getMasterOdJobNo());
				tmpOrder.setBound(orderList.get(0).getBound());
				tmpOrder.setBoundType(orderList.get(0).getBoundType());
				tmpOrder.setObDate(orderList.get(0).getObDate());
				tmpOrder.setArDate(orderList.get(0).getArDate());
				tmpOrder.setPoDate(orderList.get(0).getPoDate());
				tmpOrder.setOrigCode(orderList.get(0).getOrigCode());
				tmpOrder.setDestCode(orderList.get(0).getDestCode());
				tmpOrder.setDestPort(orderList.get(0).getDestPort());
			}else if(cntrList != null && cntrList.size() > 0){
				search.setSrchOdJobNo(cntrList.get(0).getOdJobNo());
				tmpOrder = orderMapper.selectOrderExcelError(search);
			}else if(licenseList != null && licenseList.size() > 0){
				search.setSrchOdJobNo(licenseList.get(0).getOdJobNo());
				tmpOrder = orderMapper.selectOrderExcelError(search);
			}else if(itemList != null && itemList.size() > 0){
				search.setSrchOdJobNo(itemList.get(0).getOdJobNo());
				tmpOrder = orderMapper.selectOrderExcelError(search);
			}
		}
		
		if(tmpOrder != null){
			if(!Util.isEmpty(tmpOrder.getMasterOdJobNo())) {
				masterData = true;	/** 마스터정보유무(DB데이터 OR 오류내역) */
//				map.put("bound", tmpOrder.getBound());
				map.put("boundType", tmpOrder.getBoundType());
				map.put("origCode", tmpOrder.getOrigCode());
				map.put("destCode", tmpOrder.getDestCode());
				if(tmpOrder.getBoundType().equals("EXP")){
					map.put("srchDate", tmpOrder.getObDate());
					map.put("ediCityPort", tmpOrder.getOrigCode());
				}else{
					map.put("srchDate", tmpOrder.getArDate());
					map.put("ediCityPort", tmpOrder.getDestPort());
				}
			}
			if(tmpOrder.getBoundType().equals("EXP")){
				map.put("exrateType", "EXRATE_SELLING_CUSTOMS");
			}else{
				map.put("exrateType", "EXRATE_BUYING_CUSTOMS");
			}
		}
		map.put("bound", insertData.getBound());
		userInfoMap = commonMapper.selectUserInfo(map);
		map.put("entBusinessCode", userInfoMap.get("HEAD_ENT_BUSINESS_CODE"));
		map.put("ecmItemNameAllYn", userInfoMap.get("ECM_ITEM_NAME_ALL_YN"));
		
		if(!masterData){
			map.put("srchDate", Util.null2str(tmpOrder.getPoDate(), Util.getNow("yyyy-MM-dd")));
		}
		map.put("srchEntCode", insertData.getEntCode());
		map.put("srchDateFormat", insertData.getDateFormat());
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(map, "exrateType|srchDateFormat", false, null, null, null, null);
		exrateMap = commonMapper.selectExchangeRate(map);
		
		if(masterOrder != null){
			masterOrder.setCustomsCode(Util.null2str(userInfoMap.get("CUSTOMS_CODE")));
			masterOrder.setCustomsDptCode(Util.null2str(userInfoMap.get("CUSTOMS_DPT_CODE")));
			masterOrder.setOrigCountry(Util.null2str(userInfoMap.get("ORIG_COUNTRY")));
			masterOrder.setDestCountry(Util.null2str(userInfoMap.get("DEST_COUNTRY")));
			masterOrder.setShipperCountryCode(Util.null2str(userInfoMap.get("ORIG_COUNTRY")));
			masterOrder.setConsigneeCountryCode(Util.null2str(userInfoMap.get("DEST_COUNTRY")));
			masterOrder.setEdiMailboxSeqNo(Util.null2str(userInfoMap.get("EDI_MAILBOX_SEQ_NO")));
			if(Util.isEmpty(masterOrder.getLineCode()) && masterOrder.getBound().equals("AIR")){
				masterOrder.setLineCode(Util.null2str(userInfoMap.get("LINE_CODE")));
			}
			if(Util.null2str(masterOrder.getCarryoutCode()).equals("") && masterOrder.getBoundType().equals("EXP")) {
				masterOrder.setCarryoutCode("GG");
			}
		}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(map, "userLanguage|srchEtcField", false, null, null, null, null);
		if(cntrList != null && cntrList.size() > 0){
			for(OrderContainer cntr : cntrList){
				/** 컨테이너EDI코드 저장 */
				if(!Util.null2str(map.get("CNTR_EDI_" + cntr.getCntrCode())).equals("")){
					cntr.setCntrEdiCode(Util.null2str(map.get("CNTR_EDI_" + cntr.getCntrCode())));
				}else{
					map.put("srchCode", cntr.getCntrCode());
					cntrInfoMap = commonMapper.getCodeDetail(map);
					if(cntrInfoMap != null && cntrInfoMap.size() > 0){
						cntr.setCntrEdiCode(Util.null2str(cntrInfoMap.get("ETC3")));
						map.put("CNTR_EDI_" + cntr.getCntrCode(), cntr.getCntrEdiCode());
					}
				}
			}
			orderMapper.insertContainerExcelError(cntrList);
		}
		if(licenseList != null && licenseList.size() > 0){
			for(OrderLicense odLicense : licenseList){
				orderMapper.insertLicenseExcelError(odLicense);
			}
		}
		
		if(itemList != null && itemList.size() > 0){
			/** 세부품목 단가/수량/금액처리 */
			double unitPrice;
			double amount;
			int pieces;
			for(OrderItem item : itemList){
				unitPrice = Util.null2dbl(item.getUnitPrice());
				amount = Util.null2dbl(item.getAmount());
				pieces = Util.null2int(item.getPieces());
				if(unitPrice == 0 && pieces != 0 && amount != 0){	/** 단가X/수량O/금액O : 금액 / 수량 = 단가 */
					item.setUnitPrice(amount / pieces);
				}else if(unitPrice != 0 && pieces != 0 && amount == 0){	/** 단가O/수량O/금액X : 단가 * 수량 = 금액 */
					item.setAmount(unitPrice * pieces);
				}else if(unitPrice != 0 && pieces == 0 && amount != 0){	/** 단가O/수량X/금액O : 금액 / 단가 = 수량 */
					item.setPieces(Util.null2int(amount / unitPrice));
				}else if(unitPrice == 0 && pieces == 0 && amount != 0){	/** 단가X/수량X/금액O : 금액 = 단가, 수량1 */
					item.setUnitPrice(amount);
					item.setPieces(1);
				}else if(unitPrice != 0 && pieces == 0 && amount == 0){	/** 단가O/수량X/금액X  : 단가 = 금액, 수량1 */
					item.setAmount(unitPrice);
					item.setPieces(1);
				}
				
				if(!Util.isEmpty(item.getHsCode()) && item.getHsCode().length() == 1){
					item.setHsCode("0" + item.getHsCode());
				}
				
				/** 오더별로 세부품목 리스트생성 */
				errorItemList = orderDataAdd.get(item.getOdJobNo());
				if(errorItemList == null){
					errorItemList = new ArrayList<OrderItem>();
				}
				errorItemList.add(item);
				orderDataAdd.put(item.getOdJobNo(), errorItemList);
			}
			orderMapper.insertItemExcelError(itemList);
		}
		if (orderList != null && orderList.size() > 0){
			map.clear();
			Map<String, Object> podMap = new HashMap<String, Object>();
			OrderItem orderItem;
			podMap.put("userLanguage", insertData.getUserLanguage());
			podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
			List<String> srchCodeList = new ArrayList<String>();
			srchCodeList.add("ECM003");
			srchCodeList.add("ECM010");
			podMap.put("srchCodeList", srchCodeList);
			
			podMap.put("entCode", insertData.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", insertData.getGmtTime());
			podMap.put("solution", "ECM");
			podMap.put("updateUserId", insertData.getUserId());
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			podMap.put("podTableName", 	"TB_ECM_POD_01");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
			String multiFreightYn = Util.null2str(userInfoMap.get("MULTI_FREIGHT_YN"));
			int seq = 1;
			
			for(Order order : orderList){
				if(order.getBlTypeMh().equals("M")){	/** 마스터저장은 마지막에 처리 */
					continue;
				}
				if(!Util.isEmpty(order.getClearancePaymentType()) && order.getClearancePaymentType().length() == 1){
					order.setClearancePaymentType("0" + order.getClearancePaymentType());
				}
				if(!Util.isEmpty(order.getDeliveryPaymentType()) && order.getDeliveryPaymentType().length() == 1){
					order.setDeliveryPaymentType("0" + order.getDeliveryPaymentType());
				}
				if(!Util.isEmpty(order.getAllowedItemCode()) && order.getAllowedItemCode().length() == 1){
					order.setAllowedItemCode("0" + order.getAllowedItemCode());
				}
				
				orderSaveMap.clear();
				orderSaveMap.put("ecmItemNameAllYn", userInfoMap.get("ECM_ITEM_NAME_ALL_YN"));
				
				search.setSrchOdJobNo(order.getOdJobNo());
				dbItemList = orderMapper.selectItem(search);
				
				if(orderDataAdd.get(order.getOdJobNo()) != null){
					orderDataAdd.remove(order.getOdJobNo());
				}
				order.setItemList(dbItemList);
				
				/** 세부품목이 없을경우 오더데이터로으로 세부품목1건 생성 */
				if(dbItemList.size() == 0){
					/** 오더의 수량, 중량, 금액CUR, 품명, HsCode가 있을경우만 생성 */
					if(Util.null2int(order.getPiecesReal()) != 0 && Util.null2dbl(order.getGwt()) != 0 && Util.null2dbl(order.getAmountCur()) != 0 && !Util.null2str(order.getCommodityItem()).equals("") && !Util.null2str(order.getAllowedItemCode()).equals("")){
						orderItem = new OrderItem();
						orderItem.setOdJobNo(order.getOdJobNo());
						orderItem.setSeqNoId(1);
						orderItem.setHsCode(order.getAllowedItemCode());
						orderItem.setDetailItemName(order.getCommodityItem());
						orderItem.setDetailItemNameLoc(order.getCommodityItemLoc());
						orderItem.setPieces(order.getPiecesReal());
						orderItem.setGwt(order.getGwt());
						orderItem.setAmount(order.getAmountCur());
						orderItem.setUnitPieces(order.getUnitPieces());
						orderItem.setCurrencyCode(order.getCurrencyCode());
						if(Util.null2int(order.getPiecesReal()) != 0 && Util.null2dbl(order.getAmountCur()) != 0){
							orderItem.setUnitPrice(Util.null2dbl(order.getAmountCur()) / Util.null2int(order.getPiecesReal()));
						}
						errorItemList = new ArrayList<OrderItem>();
						errorItemList.add(orderItem);
						orderMapper.insertItemExcelError(errorItemList);
					}
				}
				
				if(exrateMap != null && Util.null2int(exrateMap.get("CNT")) > 0){
					/** 공통 - 환율처리 */
					orderSaveMap.put("exChangeYn", "Y");
					orderSaveMap.put("exrateMap", exrateMap);
				}
				/** 공통 - 출발국가 */
				if(order.getBoundType().equals("IMP") && Util.isEmpty(order.getMasterOdJobNo())){
					orderSaveMap.put("setOrigCountry", "Y");
				}
				orderSaveMap.put("setConsigneeAddress", "Y");
				
				orderSaveMap.put("divideValue", userInfoMap.get("DIVIDE_VALUE"));
				orderCommonService.orderSave(order, "excelOrderError", orderSaveMap, seqNo++);
				
				if(!Util.isEmpty(order.getConsigneeAddressAdd())){
					order.setConsigneeAddress(Util.null2str(order.getConsigneeAddress()) + order.getConsigneeAddressAdd());
				}
				if(!Util.isEmpty(order.getConsigneeAddressLocAdd())){
					order.setConsigneeAddressLoc(Util.null2str(order.getConsigneeAddressLoc()) + order.getConsigneeAddressLocAdd());
				}
				if(Util.isEmpty(order.getPoNo())) {
					order.setPoNo(order.getHblNo());
				}
				
				/** 통관구분값이 "일반", "배제" 일 경우 개인통관부호 값이 없는 상태이면-->  검증결과: NULL, 검증결과내역: Null 처리 */
				if((!Util.null2str(order.getCdType()).equals("B") && !Util.null2str(order.getCdType()).equals("N")) || !Util.null2str(order.getCustomsId()).equals("")){
					if(Util.null2str(order.getCustomsIdType()).equals("A")){
						/** 개인통관식별자가 A일경우 검증(N) or 미입력 */
						if(!Util.isEmpty(order.getCustomsId())){
							order.setCustomsIdVerify("NV");
						}else{
							order.setCustomsIdVerify("X");
						}
					}else{
						/** 개인통관식별자가 A가 아닐경우 검증제외 */
						order.setCustomsIdVerify("O");
					}
				}
				
				if(Util.isEmpty(order.getHblNo())){
					this.setHblNoStock(order, order.getStockLv(), 1, false, multiFreightYn, insertData.getGmtTime());
					if(Util.isEmpty(order.getHblNo())){
						orderSaveResult.setResultCode("stockNull");
						map.clear();
						map.put("errorStr", "line : " + order.getNum() + " - stock is null");
						orderSaveResult.setResultData(map);
						throw new RuntimeException("stockNull");
					}
				}
				
				multiList = order.getMultiList();
				if(multiList != null && multiList.size() > 0){
					order.setMultiYn("Y");
				}else{
					order.setMultiYn("N");
				}
				logOrder = logData.get(order.getOdJobNo());
				if(logOrder != null){
					logOrder.setMultiList(multiList);
					logData.put(order.getOdJobNo(), logOrder);
				}
				
//				/** 오더 PACKING의 오더관리번호 업데이트 */
//				if("IMP".equals(order.getBoundType()) && ("YHTXX".equals(insertData.getEntCode()) || "UPSXK".equals(insertData.getEntCode()))){
//					int packingCnt = 0;
//					if(Util.null2int(orderMapper.updatePackingOdJobNo(order)) > 0){
//						Map<String, Object> packingMap = new HashMap<String, Object>();
//						packingMap.put("odJobNo", order.getOdJobNo());
//						packingMap.put("hblNo", order.getHblNo().trim().replace("\t", ""));
//						packingCnt = orderMapper.selectPackingCount(packingMap);
//					}
//					if(packingCnt != order.getPieces()){
//						order.setPackingErrorYn("Y");
//					}else{
//						order.setPackingErrorYn("N");
//					}
//				}
				
				Util.accessControlDatabase(order, "dateFormat|gmtTime", false, null, null, null, null);
				order.setCryptographyYn(cryptographyYn);
				orderCommonService.setOrderUpperCase(order);
				orderMapper.insertOrderExcelError(order);
				
				podMap.put("odJobNo", order.getOdJobNo());
				podMap.put("blNo", order.getHblNo());
				podMap.put("podStatus", order.getLastPodStatus());
				podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				orderMapper.insertOrderPod(podMap);
				
				if(order.getPoInfoList() != null && order.getPoInfoList().size() > 0){
					map.put("odJobNo", order.getOdJobNo());
					map.put("poInfoList", order.getPoInfoList());
					orderMapper.insertPoInfo(map);
				}
				
				if(order.getMultiList() != null && order.getMultiList().size() > 0){
					map.put("odJobNo", order.getOdJobNo());
					map.put("multiList", order.getMultiList());
					map.put("gmtTime", insertData.getGmtTime());
					map.put("registerUserId", insertData.getUserId());
					map.put("dateFormat", insertData.getDateFormat());
					orderMapper.insertMulti(map);
				}
				
				if(!Util.isEmpty(order.getAllowedItemCode()) || !Util.isEmpty(order.getCurrencyCode()) || "2".equals(order.getGwtType())){
					orderMapper.updateItemExcelError(order);
				}
				
				/** 오더 PACKING의 오더관리번호 업데이트 */
				if("IMP".equals(order.getBoundType()) && ("YHTXX".equals(insertData.getEntCode()) || "UPSXK".equals(insertData.getEntCode()))){
					orderMapper.updatePackingOdJobNo(order);
					tmpMap = new HashMap<String,Object>();
					tmpMap.put("odJobNo", order.getOdJobNo());
					commonMapper.updatePackingErrorYn(tmpMap);
				}
			}
		}
		
		Order order;
		for(String odJobNo : orderDataAdd.keySet()){	/** 세부품목 변경있는 데이터 중 오더저장하지않은 데이터 처리 */
			search.setSrchOdJobNo(odJobNo);
			itemList = orderMapper.selectOrderItem(search);
			if(itemList != null && itemList.size() > 0){
				order = orderMapper.selectOrder(search);
				order.setEntCode(insertData.getEntCode());
				order.setGmtTime(insertData.getGmtTime());
				order.setDateFormat(insertData.getDateFormat());
				order.setUpdateUserId(insertData.getUserId());
				order.setBound(tmpOrder.getBound());
				order.setBoundType(tmpOrder.getBoundType());
				order.setItemList(itemList);
				order.setGwtType(orderDataAdd.get(odJobNo).get(0).getGwtType());
				orderSaveMap.clear();
				orderSaveMap.put("ecmItemNameAllYn", userInfoMap.get("ECM_ITEM_NAME_ALL_YN"));
				
				if(masterData && masterOrder == null){
					/** 공통 - 환율처리 */
					orderSaveMap.put("exChangeYn", "Y");
					orderSaveMap.put("exrateMap", exrateMap);
				}
				
				orderCommonService.orderSave(order, "excelItemError", orderSaveMap, seqNo++);
				orderMapper.updateOrderExcelError(order);	/** bound포함 호출   특정필드만 업데이트 */
				if("2".equals(order.getGwtType())){
					order.setAllowedItemCode(null);
					order.setCurrencyCode(null);
					orderMapper.updateItemExcelError(order);
				}
			}
		}
		
		if(masterOrder != null){	/** 마스터정보오류로 마스터오더저장되지않은경우 마스터오더저장 */
			Util.accessControlDatabase(masterOrder, "dateFormat|gmtTime", false, null, null, null, null);
			masterOrder.setCryptographyYn(cryptographyYn);
			orderMapper.insertOrderExcelError(masterOrder);
			List<Order> odList = orderMapper.selectOrderListExcelError(masterOrder);	/** 마스터 저장후 해당 마스터에 콘솔된 하우스의 환율 등 처리 */
			
			SqlSession sqlSession = null;
			try {
				int cnt = 1;
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
				
				for(Order od : odList){
					od.setEntCode(insertData.getEntCode());
					od.setGmtTime(insertData.getGmtTime());
					od.setUpdateUserId(insertData.getUserId());
					od.setDateFormat(insertData.getDateFormat());
					orderSaveMap.clear();
					/** 공통 - 환율처리 */
					orderSaveMap.put("exChangeYn", "Y");
					orderSaveMap.put("exrateMap", exrateMap);
					orderCommonService.orderSave(od, "excelMasterError", orderSaveMap, seqNo++);
					
					
					od.setMblNo(masterOrder.getMblNo());
					od.setLineCode(masterOrder.getLineCode());
					od.setVesselFlight(masterOrder.getVesselFlight());
					od.setObDate(masterOrder.getObDate());
					od.setArDate(masterOrder.getArDate());
					od.setOrigCode(masterOrder.getOrigCode());
					od.setDestCode(masterOrder.getDestCode());
					od.setDestPort(masterOrder.getDestPort());
					od.setOrigCountry(masterOrder.getOrigCountry());
					od.setDestCountry(masterOrder.getDestCountry());
					od.setShipperCountryCode(masterOrder.getOrigCountry());
					od.setConsigneeCountryCode(masterOrder.getDestCountry());
					od.setMrn(masterOrder.getMrn());
					od.setMsn(masterOrder.getMsn());
					od.setCustomsCode(masterOrder.getCustomsCode());
					od.setCustomsDptCode(masterOrder.getCustomsDptCode());
					od.setEdiMailboxSeqNo(masterOrder.getEdiMailboxSeqNo());
					od.setCarryoutCode(masterOrder.getCarryoutCode());
					orderBatchMapper.updateOrderExcelError(od);	/** bound없이 호출   특정필드만 업데이트 */
					
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
			}catch(Exception e) {
				throw e;
			}finally{
				sqlSession.close();
			}
		}

		List<String> deleteDocumentList = insertData.getDeleteDocumentList();
		if(deleteDocumentList != null && deleteDocumentList.size() > 0){
			DocumentApi documentApi = new DocumentApi(client);
			for(String documentId : deleteDocumentList){
				/** 저장처리한데이터제거 */
				documentApi.deleteDocument(errorIndex, documentId);
			}
			
			// 처리 결과 상태 검증오류로 변경
			updateExcelUploadResult(resultIndex, resultId, insertData.getResultStatus(), insertData.getCount() + Util.null2int(cntData.size()));
		}else{
			if(errorIndex.indexOf("ecm-excel-upload-error-ref") == -1){
				// Error Index 제거
				IndexApi indexApi = new IndexApi(client);
				indexApi.deleteIndex(errorIndex);
			}
			// 처리 결과 상태 완료로 변경
			updateExcelUploadResult(resultIndex, resultId, insertData.getResultStatus(), insertData.getCount() + Util.null2int(cntData.size()));
		}
		
		/** 하우스별오더저장 */
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		BulkData bulkData;
		String tmpId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5);
		String id;
		int seq = 1;
		tmpMap = new HashMap<String, Object>();
		Map<String, Object> logMap = null;
		tmpMap.put("ent_code", insertData.getEntCode());
		tmpMap.put("ent_business_code", insertData.getUserEntBusinessCode());
		tmpMap.put("process_user_id", insertData.getUserId());
		tmpMap.put("solution", "ECM");
		tmpMap.put("bound", insertData.getBound());
		tmpMap.put("bound_type", insertData.getBoundType());
		tmpMap.put("log_job_type", "DATA");
		tmpMap.put("result", "ok");
		tmpMap.put("ent_business_print_name", insertData.getLogUserBusinessPrintName());
		tmpMap.put("process_user_name", insertData.getLogUserName());
		tmpMap.put("process_user_name_eng", insertData.getLogUserNameEng());
		tmpMap.put("process_date_time", System.currentTimeMillis());
		tmpMap.put("job_func_type", "ADD");
		for(String logOdJobNo : logData.keySet()){
			logOrder = logData.get(logOdJobNo);
			id = tmpId + Util.setSeqNo(seq++);
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpMap);
			logMap.put("job_log", new Gson().toJson(logOrder));
			logMap.put("description", "excel Error save[HBL_NO : " + logOrder.getHblNo()+"]");
			logMap.put("job_no", logOrder.getOdJobNo());
			logMap.put("referenceNoId", id);
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(id + Util.setSeqNo(seq++));
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}
		
		if (bulkList != null && bulkList.size() > 0) {
			// Index 확인 후 Index 생성
			IndexApi indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			DocumentApi documentApi = new DocumentApi(client);
			documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
		}
	}
	
	/** 하우스비엘번호,멀티화물송장번호 스탁생성 */
	private void setHblNoStock(Order od, int custCodeLv, int seqNoId, boolean multiFlag, String multiFreightYn, String gmtTime) throws Exception {
		/** 멀티화물여부(multiFreightYn)가 'Y'일경우
		 * 	hblNo는 도입사코드+YYMMDD+일련번호로 생성
		 * 	멀티화물송장번호는 멀티stock으로 생성     멀티stock이 없을경우 일반stock으로 처리
		 * 	
		 *  멀티화물여부가 'N'일경우
		 *  hblNo와 첫번째멀티송장번호는 일반stock으로 생성
		 *  두번째멀티송장번호부터는 멀티stock으로 생성     멀티stock이 없을경우 일반stock으로 처리
		 */
		if(Util.isEmpty(od.getHblNo()) && "Y".equals(multiFreightYn) && Util.null2int(od.getPieces()) > 1){
			Map<String, Object> odStockMap = new HashMap<String, Object>();
			odStockMap.put("gmtTime", gmtTime);
			odStockMap.put("entCode", od.getEntCode());
			odStockMap.put("entBusinessCode", "");
			odStockMap.put("registerUserId", od.getRegisterUserId());
			odStockMap.put("updateUserId", od.getRegisterUserId());
			odStockMap.put("solution", "ECM");
			odStockMap.put("stockType", "OD");
			odStockMap.put("stockSign", od.getEntCode());
			odStockMap.put("stockSeqLength", 8);
			odStockMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			odStockMap.put("stockRemark", "ORDER-HBL NO");
			odStockMap.put("seqNo", od.getOdJobNo());
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(odStockMap, "gmtTime", false, null, null, null, null);
			od.setHblNo(commonMapper.selectNextStockMaxNo(odStockMap));
			
			od.setCreateStockNoCnt(od.getPieces());
			if("KOREX".equals(od.getDeliveryCode()) || "EMS".equals(od.getDeliveryCode())){
				multiFlag = true;
			}
		}
		
		if ((Util.isEmpty(od.getHblNo()) || Util.null2int(od.getCreateStockNoCnt()) > 0) && !Util.isEmpty(od.getDeliveryCode())) {
			String agentCode = null;
			if (custCodeLv == 3) {
				agentCode = od.getCustCodeLv3();
			} else if (custCodeLv == 2) {
				agentCode = od.getCustCodeLv2();
			} else if (custCodeLv == 1) {
				agentCode = od.getCustCodeLv1();
			} else if (custCodeLv == 0) {
				agentCode = od.getEntCode();
			}
			String currentNo = null;
			if (!Util.isEmpty(agentCode)) {
				Map<String, Object> hblStockParams = new HashMap<String, Object>();
				hblStockParams.put("entCode", od.getEntCode());
				hblStockParams.put("bound", od.getBound());
				hblStockParams.put("boundType", od.getBoundType());
				if(Util.isEmpty(od.getHblNo()) || !multiFlag){
					hblStockParams.put("deliveryCode", od.getDeliveryCode());
				}else{
					if(multiFlag){
						hblStockParams.put("deliveryCode", od.getDeliveryCode() + "MUL");
					}
				}
				hblStockParams.put("agentCode", agentCode);
				/** hblStockParams의 값이 같은경우 쿼리 실행오류로 추가 */
				hblStockParams.put("odJobNo", od.getOdJobNo());
				hblStockParams.put("createCnt", od.getCreateStockNoCnt());
				Util.accessControlDatabase(hblStockParams, "odJobNo|createCnt", false, null, null, null, null);
				Map<String, Object> hblStockMap = commonMapper.selectHblNoStock(hblStockParams);
				currentNo = Util.null2str(hblStockMap.get("CURRENT_NO"));
				if (!Util.null2str(currentNo).equals("")) {
					if (Util.isEmpty(od.getHblNo())) {
						od.setHblNo(currentNo);
						if(od.getDeliveryCode().equals("KOREX") || od.getDeliveryCode().equals("EMS")){
							multiFlag = true;
						}
					}

					/** hblNo가 없고 수량이 1보다 클경우 수량만큼 stockNo, 멀티화물 생성 */
					if (Util.null2int(od.getPieces()) > 1) {
						if (Util.null2int(od.getCreateStockNoCnt()) == 0) {
							od.setCreateStockNoCnt(od.getPieces() - 1);
						} else {
							od.setCreateStockNoCnt(od.getCreateStockNoCnt() - 1);
						}

						OrderMulti odMulti = new OrderMulti();
						odMulti.setOdJobNo(od.getOdJobNo());
						odMulti.setDeliveryNo(currentNo);
						odMulti.setDeliveryCode(Util.null2str(hblStockParams.get("deliveryCode")));
						odMulti.setPieces(1);
						odMulti.setPaymentGubn("01");
						odMulti.setRegisterUserId(od.getRegisterUserId());
						List<OrderMulti> odMultiList;
						if (od.getMultiList() == null) {
							odMultiList = new ArrayList<OrderMulti>();
						} else {
							odMultiList = od.getMultiList();
						}
						odMultiList.add(odMulti);
						od.setMultiList(odMultiList);
					}
				}
			}
			if(Util.isEmpty(currentNo)){	/** stockNo를 생성하지못하면 상위레벨로 다시 생성, custCodeLv0(도입사)로 stockNo생성할수없으면 종료 */
				 if(custCodeLv > 0){
					 this.setHblNoStock(od, --custCodeLv, seqNoId, multiFlag, multiFreightYn, gmtTime);
				 }else{
					 if(!multiFlag){
						 od.setHblNo(null);	/** 하우스번호 제거해서 HblNo or StockNo Null 오류처리 */
					 }else{
						 this.setHblNoStock(od, od.getStockLv(), seqNoId, false, multiFreightYn, gmtTime);	/** 대한통운멀티,우체국멀티 stock이 없을경우 대한통운,우체국으로 처리 */
					 }
				 }
			}else if(Util.null2int(od.getCreateStockNoCnt()) > 0){	/** 하우스 수량만큼 반복해서 stockNo생성 */
				this.setHblNoStock(od, custCodeLv, seqNoId, multiFlag, multiFreightYn, gmtTime);
			}
		}
	}
	
	/**
	 * 결과 데이터 상태 변경
	 * @param indexName
	 * @param id
	 * @param result
	 */
	public void updateExcelUploadResult(String indexName, String id, String result, int count) {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		try {
			DocumentApi documentApi = new DocumentApi(client);
			
			GetResponse response = documentApi.getDocument(indexName, id);
			ExcelUploadResult excelUploadResult = mapper.convertValue(response.getSourceAsMap(), ExcelUploadResult.class);
			if("ok".equals(result)){
				excelUploadResult.setErrorIndexName(null);
				excelUploadResult.setCount(count);
			}
			excelUploadResult.setStatus(result);
			
			documentApi.updateDocument(indexName, id, Converter.domainToMap(excelUploadResult));
		} catch (Exception e) {
			log.error("오류가 발생 했습니다.", e);
		}
	}
}
