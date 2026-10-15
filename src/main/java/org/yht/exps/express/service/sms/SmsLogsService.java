package org.yht.exps.express.service.sms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.SearchApi;
import org.yht.elasticsearch.client.data.SortData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.model.sms.logs.BatchDataLogs;
import org.yht.exps.express.model.sms.logs.BatchDataLogsList;
import org.yht.exps.express.model.sms.logs.InterfaceLogs;
import org.yht.exps.express.model.sms.logs.InterfaceLogsList;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.logs.ProcessLogsCompare;
import org.yht.exps.express.model.sms.logs.ProcessLogsCompareList;
import org.yht.exps.express.model.sms.logs.ProcessLogsList;
import org.yht.exps.express.model.sms.logs.search.SearchBatchDataLogs;
import org.yht.exps.express.model.sms.logs.search.SearchInterfaceLogs;
import org.yht.exps.express.model.sms.logs.search.SearchProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.util.Converter;
import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class SmsLogsService {
    @Autowired
    CommonService commonService;
    @Autowired
	CommonMapper commonMapper;
	@Autowired
	RestHighLevelClient client;
	
	@Value("${custom.elasticsearch-scroll-page}")
	private int elasticsearchScrollPage_;
	
	/**
	 * 시스템 로그 리스트 조회
	 * @param searchProcessLogs
	 * @return ProcessLogsList
	 * @throws Exception
	 */
	public ProcessLogsList selectProcessLogs(SearchProcessLogs searchProcessLogs) throws Exception {
		ProcessLogsList processLogsList = new ProcessLogsList();

	    try {
			// 검색할 Indices
		    String[] indices = new String[1];
		    indices[0] = "ecm-process-logs-*";
      	
	        // Sort 조건 처리
	        List<SortData> sortList = new ArrayList<SortData>();

	        if (searchProcessLogs.getSort() == null || searchProcessLogs.getSort().size() == 0) {
		        SortData sortData1 = new SortData();
		        sortData1.setField("process_date_time");
		        sortData1.setType(SortOrder.DESC);
		        sortList.add(sortData1);
		        
		        SortData sortData2 = new SortData();
		        sortData2.setField("process_user_id");
		        sortData2.setType(SortOrder.ASC);
		        sortList.add(sortData2);

		        SortData sortData3 = new SortData();
		        sortData3.setField("log_job_type");
		        sortData3.setType(SortOrder.ASC);
		        sortList.add(sortData3);

	        } else {
	        	for (int i = 0; i < searchProcessLogs.getSort().size(); i++) {
	        		Sort data = searchProcessLogs.getSort().get(i);
	    	        SortData sortData = new SortData();
			        sortData.setField(Converter.camelToSnake(data.getField()));
			        if ("asc".equals(data.getDir()))	sortData.setType(SortOrder.ASC);
			        else								sortData.setType(SortOrder.DESC);
			        sortList.add(sortData);
	        	}
	        }
	        
	        // Parameter 처리
			int size = searchProcessLogs.getPageSize();
	        int from = searchProcessLogs.getPage() > 0 ? (searchProcessLogs.getPage() - 1) * size : 0;
			long startDate = searchProcessLogs.getSrchStartDate();
			long endDate = searchProcessLogs.getSrchEndDate();

	        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
	        boolQueryBuilder.filter(QueryBuilders.termQuery("ent_code", searchProcessLogs.getSrchEntCode()));
	        boolQueryBuilder.filter(QueryBuilders.rangeQuery(searchProcessLogs.getSrchDateGbn()).gte(startDate).lte(endDate));
	        if(!"".equals(Util.null2str(searchProcessLogs.getSrchEntBusinessCode()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("ent_business_code", searchProcessLogs.getSrchEntBusinessCode()));
	        }
	        if(!"".equals(Util.null2str(searchProcessLogs.getSrchSolution()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("solution", searchProcessLogs.getSrchSolution()));
	        }
	        if(!"".equals(Util.null2str(searchProcessLogs.getSrchBound()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("bound", searchProcessLogs.getSrchBound()));
	        }
	        if(!"".equals(Util.null2str(searchProcessLogs.getSrchBoundType()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("bound_type", searchProcessLogs.getSrchBoundType()));
	        }
	        if(!"".equals(Util.null2str(searchProcessLogs.getSrchLogJobType()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("log_job_type", searchProcessLogs.getSrchLogJobType()));
	        }
	        if(!"".equals(Util.null2str(searchProcessLogs.getSrchJobFuncType()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("job_func_type", searchProcessLogs.getSrchJobFuncType()));
	        }
	        if(!"".equals(Util.null2str(searchProcessLogs.getSrchProcessUserId()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("process_user_id", searchProcessLogs.getSrchProcessUserId()));
	        }
	        if(!"".equals(Util.null2str(searchProcessLogs.getSrchJobNo())) && !"".equals(Util.null2str(searchProcessLogs.getSrchJobNo2()))){
	        	boolQueryBuilder.filter(QueryBuilders.termsQuery("job_no", searchProcessLogs.getSrchJobNo(), searchProcessLogs.getSrchJobNo2()));
	        }else if(!"".equals(Util.null2str(searchProcessLogs.getSrchJobNo()))) {
	        	boolQueryBuilder.filter(QueryBuilders.wildcardQuery("job_no", "*" + searchProcessLogs.getSrchJobNo() + "*"));
	        }
	        
	        
	        
	        if(!"".equals(Util.null2str(searchProcessLogs.getSrchDescription()))) {
				BoolQueryBuilder textOrQuery = QueryBuilders.boolQuery();
            	textOrQuery.should(QueryBuilders.matchQuery("description", searchProcessLogs.getSrchDescription().toLowerCase()));
            	textOrQuery.should(QueryBuilders.matchQuery("job_log", searchProcessLogs.getSrchDescription().toLowerCase()));
        		boolQueryBuilder.filter(textOrQuery);
	        }
	        
	        SearchApi searchApi = new SearchApi(client);
	        SearchResponse response;
	        if (size == 0)	response = searchApi.searchScroll(boolQueryBuilder, elasticsearchScrollPage_, indices, searchProcessLogs.getSrchScrollId(), sortList);
	        else			response = searchApi.search(boolQueryBuilder, from, size, 15000, indices, sortList);
	        
	        String scrollId = response.getScrollId();
	        processLogsList.setScrollId(scrollId);
	        
	    	SearchHits hits = response.getHits();
	    	
			final ObjectMapper mapper = new ObjectMapper();
			mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setSerializationInclusion(Include.NON_NULL);
			
	    	SearchHit[] resultHits = hits.getHits();
	    	if (size == 0)	processLogsList.setTotal(resultHits.length);
	    	else			processLogsList.setTotal(hits.getTotalHits().value);
	    	
	    	List<ProcessLogs> list = new ArrayList<ProcessLogs>();
	    	if (resultHits.length > 0) {
		    	for (SearchHit hit : resultHits) {
		    		ProcessLogs processLogs = mapper.readValue(hit.getSourceAsString(), ProcessLogs.class);
		    		processLogs.setIndex(hit.getIndex());
		    		list.add(processLogs);
		        }
		    	
	    	} else {
	    		// 메모리 확보를 위해 ScrollId를 ElasticSearch에서 삭제한다.
	    		if (!"".equals(Util.null2str(searchProcessLogs.getSrchScrollId()))) {
	    			searchApi.clearScollRequest(searchProcessLogs.getSrchScrollId());
	    		}
	    	}
	    	
	    	processLogsList.setProcessLogsList(list);
			
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		} finally {}
       
	    return processLogsList;
	}
	
	/**
	 * 시스템 로그 리스트 조회
	 * @param searchProcessLogs
	 * @return ProcessLogsList
	 * @throws Exception
	 */
	public ProcessLogsCompareList selectProcessLogCompare(String entCode, String referenceNoId1, String referenceNoId2) throws Exception {
		ProcessLogsCompareList processLogsCompareList = new ProcessLogsCompareList();
		
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		Gson gson = new GsonBuilder().create();
		Map<String,Object> map1 = null;
		Map<String,Object> map2 = null;
		
		String[] indices = new String[1];
	    indices[0] = "ecm-process-logs-*";
	    
	 // Sort 조건 처리
        List<SortData> sortList = new ArrayList<SortData>();
        SortData sortData1 = new SortData();
        sortData1.setField("process_date_time");
        sortData1.setType(SortOrder.ASC);
        sortList.add(sortData1);
	    
	    SearchApi searchApi = new SearchApi(client);
	    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(QueryBuilders.termQuery("ent_code", entCode));
        boolQueryBuilder.filter(QueryBuilders.termsQuery("reference_no_id", referenceNoId1, referenceNoId2));
        
        SearchResponse response = searchApi.search(boolQueryBuilder, 0, 2, 100, indices, sortList);
        SearchHits hits = response.getHits();
        SearchHit[] resultHits = hits.getHits();
        
        if (resultHits.length == 2) {
    		ProcessLogs processLogs = mapper.readValue(resultHits[0].getSourceAsString(), ProcessLogs.class);
    		String log = processLogs.getJobLog();
    		if(!Util.isEmpty(log)){
    			map1= gson.fromJson(log, Map.class);
    		}
    		processLogs = mapper.readValue(resultHits[1].getSourceAsString(), ProcessLogs.class);
    		log = processLogs.getJobLog();
    		if(!Util.isEmpty(log)){
    			map2= gson.fromJson(log, Map.class);
    		}
    		
    		if(map1 != null && map2 != null) {
    			/*
    			case1> 1번에만 field 항목 존재하는 경우
    			case2> 2번에만 field 항목 존재하는 경우
    			case3> 1번,2번 field 둘다 항목 존재하는 경우
    			*/
    			
    			//1.map에  key 값을 추출하여 셋팅
    			//MAP의 KEY값을 이용하여 VALUE값 가져오기
    			//case3> 1번,2번 field 둘다 항목 존재하는 경우셋팅	    
    		    LinkedList<ProcessLogsCompare> list1 = new LinkedList<ProcessLogsCompare>();
    		    LinkedList<ProcessLogsCompare> list2 = new LinkedList<ProcessLogsCompare>();
    		    
    		    ProcessLogsCompare processLogsCompare = null;
    		    
    			Map<String,Object> mas = new HashMap<String,Object>();
    		    for (String mapkey : map1.keySet()){
    		    	
    		    	Object mapObj1 = map1.get(mapkey);
    		    			    	
    		    	Object mapObj2 = map2.get(mapkey);
    		    	//키 포함하는지 여부
    		    	boolean isKey = map2.containsKey(mapkey);
    		    	
    		    	//map2 에도 같은 키 존재하는 경우만 일치여부 판단함.
    		    	String isSame = "N";
    		    	if(isKey) {
    			    	if(mapObj1 != null && mapObj2 != null) {
    			    		//String 형인 경우
    				    	if(mapObj1 instanceof java.lang.String || mapObj1 instanceof java.lang.Double) {
    				    		
    				    		if( (mapObj2 instanceof java.lang.String || mapObj2 instanceof java.lang.Double) 
    				    				&& mapObj1.equals(mapObj2)) {
    				    			isSame = "Y";
    				    		}

    				    		//동일키값 존재하는 경우만 map에 셋팅함.
    					    	mas.put(mapkey, isSame);
    					    	
    				    		if ("N".equals(isSame)) {
    						    	processLogsCompare =  new ProcessLogsCompare();
    						    	processLogsCompare.setKey(mapkey);
    						    	processLogsCompare.setValue(mapObj1);
    						    	processLogsCompare.setIsSame(isSame);
    						    	processLogsCompare.setGb("G");
    						    	list1.add(processLogsCompare);
    						    	
    						    	processLogsCompare =  new ProcessLogsCompare();
    						    	processLogsCompare.setKey(mapkey);
    						    	processLogsCompare.setValue(mapObj2);
    						    	processLogsCompare.setIsSame(isSame);
    						    	processLogsCompare.setGb("G");
    						    	list2.add(processLogsCompare);
    				    		}
    				    	}
    				    	//ArrayList인 경우는 비교하지 않으나  둘다  null 인경우만 같은 항목으로 처리함.
    				    	else if(mapObj1 instanceof java.util.ArrayList) {
    				    		if(mapObj2 instanceof java.util.ArrayList) {
    				    			if ( ((java.util.ArrayList) mapObj1).isEmpty() && ((java.util.ArrayList) mapObj2).isEmpty() ) {
    				    				
    				    				isSame = "Y";
    				    				//동일키값 존재하는 경우만 map에 셋팅함.
    							    	mas.put(mapkey, isSame);
    							    	
    							    	processLogsCompare =  new ProcessLogsCompare();
    							    	processLogsCompare.setKey(mapkey);
    							    	processLogsCompare.setValue(mapObj1);
    							    	processLogsCompare.setIsSame(isSame);
    							    	processLogsCompare.setGb("G");
    							    	list1.add(processLogsCompare);
    							    	
    							    	processLogsCompare =  new ProcessLogsCompare();
    							    	processLogsCompare.setKey(mapkey);
    							    	processLogsCompare.setValue(mapObj2);
    							    	processLogsCompare.setIsSame(isSame);
    							    	processLogsCompare.setGb("G");
    							    	list2.add(processLogsCompare);
    				    			}
    				    		}			    		
    				    	}	    	
    			    	}
    			    	else if (mapObj1 == null && mapObj2 == null) {
    			    		//동일키값 존재하는 경우만 map에 셋팅함.
    				    	mas.put(mapkey, isSame);
    				    	
//    			    		Map<String,Object> row1 = new HashMap<String,Object>();
//    				    	row1.put("KEY",mapkey);
//    				    	row1.put("VALUE",mapObj1);
//    				    	row1.put("ISSAME","Y");
//    				    	row1.put("GB","G");
//    				    	list1.add(row1);
//    				    	
//    				    	Map<String,Object> row2 = new HashMap<String,Object>();
//    				    	row2.put("KEY",mapkey);
//    				    	row2.put("VALUE",mapObj2);
//    				    	row2.put("ISSAME","Y");
//    				    	row2.put("GB","G");
//    				    	list2.add(row2);
    			    	}
    			    	//공통 키존재하고 ArrayList 가 아닌건만 우선순위가 첫번째인 라인으로 셋팅됨.
    			    	//왜냐면 ArrayList 셋팅하면 배열의수가 다르므로 화면에서 왼쪽 오른쪽 비교 의미가 없게 됨. 
    			    	else if (mapObj1 == null && mapObj2 != null) {
    			    		if(false == mapObj2 instanceof java.util.ArrayList) {
    					    	processLogsCompare =  new ProcessLogsCompare();
    					    	processLogsCompare.setKey(mapkey);
    					    	processLogsCompare.setValue(mapObj1);
    					    	processLogsCompare.setIsSame("N");
    					    	processLogsCompare.setGb("G");
    					    	list1.add(processLogsCompare);
    					    	
    					    	processLogsCompare =  new ProcessLogsCompare();
    					    	processLogsCompare.setKey(mapkey);
    					    	processLogsCompare.setValue(mapObj2);
    					    	processLogsCompare.setIsSame("N");
    					    	processLogsCompare.setGb("G");
    					    	list2.add(processLogsCompare);
    			    		}
    			    	}
    			    	//공통 키존재하고 ArrayList 가 아닌건만 우선순위가 첫번째인 라인으로 셋팅됨.
    			    	//왜냐면 ArrayList 셋팅하면 배열의수가 다르므로 화면에서 왼쪽 오른쪽 비교 의미가 없게 됨. 
    			    	else if (mapObj1 != null && mapObj2 == null) {
    			    		if(false == mapObj2 instanceof java.util.ArrayList) {
    					    	processLogsCompare =  new ProcessLogsCompare();
    					    	processLogsCompare.setKey(mapkey);
    					    	processLogsCompare.setValue(mapObj1);
    					    	processLogsCompare.setIsSame("N");
    					    	processLogsCompare.setGb("G");
    					    	list1.add(processLogsCompare);
    					    	
    					    	processLogsCompare =  new ProcessLogsCompare();
    					    	processLogsCompare.setKey(mapkey);
    					    	processLogsCompare.setValue(mapObj2);
    					    	processLogsCompare.setIsSame("N");
    					    	processLogsCompare.setGb("G");
    					    	list2.add(processLogsCompare);
    			    		}
    			    	}
    		    	}
    		    }

    		    //case1> 1번에만 field 항목 존재하는 경우를 셋팅함.
    		    for (String mapkey : map1.keySet()){
    		    	//1,2 동일키값 존재하는 경우는 skip함.왜냐면 이미 윗단에  list에 셋팅돼있음.
    		    	Object masObj = mas.get(mapkey);
    		    	if(masObj != null) {
    		    		continue;
    		    	}
    		    	
    		    	processLogsCompare =  new ProcessLogsCompare();
    		    	processLogsCompare.setKey(mapkey);
    		    	processLogsCompare.setValue(map1.get(mapkey));
    		    	processLogsCompare.setIsSame("A");
    		    	processLogsCompare.setGb("B");
    		    	list1.add(processLogsCompare);
    		    }
    			
    		    //case2> 2번에만 field 항목 존재하는 경우
    		    for (String mapkey : map2.keySet()){
    		    	//1,2 동일키값 존재하는 경우는 skip함.왜냐면 이미 윗단에  list에 셋팅돼있음.
    		    	Object masObj = mas.get(mapkey);
    		    	if(masObj != null) {
    		    		continue;
    		    	}
    		    	
    		    	processLogsCompare =  new ProcessLogsCompare();
    		    	processLogsCompare.setKey(mapkey);
    		    	processLogsCompare.setValue(map2.get(mapkey));
    		    	processLogsCompare.setIsSame("A");
    		    	processLogsCompare.setGb("B");
    		    	list2.add(processLogsCompare);
    		    }
    		    
    		    processLogsCompareList.setProcessLogsList1(list1);
    		    processLogsCompareList.setProcessLogsList2(list2);
    		}
    	}
	    
		return  processLogsCompareList;
	}
	
	
	/**
	 * 시스템 로그
	 * @param index
	 * @param referenceNoId
	 * @return ProcessLogs
	 * @throws Exception
	 */
	public ProcessLogs getProcessLogs(String index, String referenceNoId) throws Exception {
		ProcessLogs processLogs = null;

		final ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		DocumentApi documentApi = new DocumentApi(client);
		
		GetResponse response = documentApi.getDocument(index, referenceNoId);
		processLogs = mapper.convertValue(response.getSourceAsMap(), ProcessLogs.class);
       
		return processLogs;
	}
	
	/**
	 * 시스템 로그 삭제 개수 카운트
	 * @param searchProcessLogs
	 * @return int
	 * @throws Exception
	 */
	public int deleteLogsTargetCount(SearchProcessLogs searchProcessLogs) throws Exception {
		ProcessLogsList processLogsList = selectProcessLogs(searchProcessLogs);
		return (int) processLogsList.getTotal();
	}
	
	/**
	 * 시스템 로그 삭제
	 * @param searchProcessLogs
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Integer> deleteProcessLogs(SearchProcessLogs searchProcessLogs) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		DocumentApi documentApi = new DocumentApi(client);
		
		int totalCnt = 0;
		int successCnt = 0;
		int failCnt = 0;
		
		//선택한 데이터 삭제하는 경우
		if("chkData".equals(searchProcessLogs.getTargetData())) {
			
			totalCnt = searchProcessLogs.getCheckDataList().size();
			
			for(int i=0; i<searchProcessLogs.getCheckDataList().size(); i++) {
				
				DeleteResponse response = documentApi.deleteDocument(Util.null2str(searchProcessLogs.getCheckDataList().get(i).get("index")), Util.null2str(searchProcessLogs.getCheckDataList().get(i).get("referenceNoId")));

				if("DELETED".equals(Util.null2str(response.getResult())) || "deleted".equals(Util.null2str(response.getResult()))) {
					successCnt++;
				}else {		//ex.not_found
					failCnt++;
				}
				
			}
		
		//검색조건으로 조회해 데이터 삭제하는 경우	
		}else {
			
//			List<ProcessLogs> processLogsList = selectProcessLogs(searchProcessLogs).getProcessLogsList();
			//-> 시스템로그(ES) 검색건 삭제 시 pageSize만큼의 데이터만 삭제되어 검색건 전체 삭제되도록 수정함.
			List<ProcessLogs> processLogsList = new ArrayList<ProcessLogs>();
			
			int srchDataCnt = searchProcessLogs.getSrchDataCnt();			//검색 데이터 건수(화면에서 전달)
			int pageSize = searchProcessLogs.getPageSize();					//페이지 사이즈
			
			int totalPage = (int) Math.ceil((double) srchDataCnt/pageSize);	//몇 페이지까지 있는지 == pageSize만큼의 데이터를 몇 번 가져와야 하는지
			
			//페이지 사이즈 만큼씩 잘라서 데이터 가져오기 -> 전체 데이터 개수가 92개고 페이지 사이즈가 50일 경우 두 번 불러와서 list에 추가 후 삭제하기 위함
			for(int page=1; page<=totalPage; page++) {
				searchProcessLogs.setPage(page);
				processLogsList.addAll(selectProcessLogs(searchProcessLogs).getProcessLogsList());
			}
			
			totalCnt = processLogsList.size();
			
			for(ProcessLogs pl : processLogsList) {
				
				DeleteResponse response = documentApi.deleteDocument(Util.null2str(pl.getIndex()), Util.null2str(pl.getReferenceNoId()));

				if("DELETED".equals(Util.null2str(response.getResult())) || "deleted".equals(Util.null2str(response.getResult()))) {
					successCnt++;
				}else {		//ex.not_found
					failCnt++;
				}
				
			}
		}
       
		Map<String, Integer> resMap = new HashMap<String, Integer>();
		resMap.put("totalCnt", totalCnt);
		resMap.put("successCnt", successCnt);
		resMap.put("failCnt", failCnt);
		
		return resMap;
	}
	
	/**
	 * 수입화물진행정보 데이터처리조회
	 * @param searchInterfaceLogs
	 * @return InterfaceLogsList
	 * @throws Exception
	 */
	public BatchDataLogs cargoProgressLog(SearchBatchDataLogs searchBatchDataLogs, HttpServletRequest req) throws Exception {
		BatchDataLogs batchDataLogs = new BatchDataLogs();
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("srchEntCode", searchBatchDataLogs.getSrchEntCode());
			map = commonMapper.selectHeadEntBusinessInfo(map);
			if(map != null && map.size() > 0){
				String[] indices = new String[1];
				
				indices[0] = "ecm-cargo-progress-" + Util.null2str(map.get("ENT_BUSINESS_CODE")).toLowerCase();
				BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
				if(!"".equals(Util.null2str(searchBatchDataLogs.getSrchHblNo()))) {
					boolQueryBuilder.filter(QueryBuilders.termQuery("hblNo.keyword", searchBatchDataLogs.getSrchHblNo()));
				}
				
				final ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				mapper.setSerializationInclusion(Include.NON_NULL);
				
				SearchApi searchApi = new SearchApi(client);
				SearchResponse response = searchApi.search(boolQueryBuilder, 0, 1, 999999, indices);
				SearchHits hits = response.getHits();
				SearchHit[] resultHits = hits.getHits();
				if (resultHits.length > 0){
					batchDataLogs = mapper.readValue(resultHits[0].getSourceAsString(), BatchDataLogs.class);
					batchDataLogs.setTotalCount(Util.null2int(hits.getTotalHits().value));
		    	}
			}
		}catch(Exception e) {
			log.error(Util.getPrintStacTraceString(e));
			batchDataLogs.setRemarks("System Error.");
		}
		return batchDataLogs;
	}
	/**
	 * EDI수신 데이터처리조회
	 * @param searchInterfaceLogs
	 * @return InterfaceLogsList
	 * @throws Exception
	 */
	public BatchDataLogsList ediRecvCheckLogList(SearchBatchDataLogs searchBatchDataLogs, HttpServletRequest req) throws Exception {
		BatchDataLogsList batchDataLogsList = new BatchDataLogsList();
		Util.accessControlDatabase(searchBatchDataLogs, null, true, null, "", null, "");
		List<BatchDataLogs> list  = commonMapper.selectEdiGovRecvList(searchBatchDataLogs);
		batchDataLogsList.setBatchDataLogsList(list);
		if(list.size() > 0) {
			batchDataLogsList.setTotal(list.get(0).getTotal());
		}else {
			batchDataLogsList.setTotal(0);
		}
		return batchDataLogsList;
	}
	
	/**
	 * 인터페이스 로그 리스트 조회
	 * @param searchInterfaceLogs
	 * @return InterfaceLogsList
	 * @throws Exception
	 */
	public InterfaceLogsList interfaceLogList(SearchInterfaceLogs searchInterfaceLogs) throws Exception {
		InterfaceLogsList interfaceLogsList = new InterfaceLogsList();

       try {
			// 검색할 Indices
	        String[] indices = new String[1];
	        indices[0] = "ecm-interface-logs-*";
       	
	        // Sort 조건 처리
	        List<SortData> sortList = new ArrayList<SortData>();

	        if (searchInterfaceLogs.getSort() == null || searchInterfaceLogs.getSort().size() == 0) {
		        SortData sortData1 = new SortData();
		        sortData1.setField("register_date_time");
		        sortData1.setType(SortOrder.DESC);
		        sortList.add(sortData1);
	        } else {
	        	for (int i = 0; i < searchInterfaceLogs.getSort().size(); i++) {
	        		Sort data = searchInterfaceLogs.getSort().get(i);
	    	        SortData sortData = new SortData();
			        sortData.setField(Converter.camelToSnake(data.getField()));
			        if ("asc".equals(data.getDir()))	sortData.setType(SortOrder.ASC);
			        else								sortData.setType(SortOrder.DESC);
			        sortList.add(sortData);
	        	}
	        }
	        
	        // Parameter 처리
			int size = searchInterfaceLogs.getPageSize();
	        int from = searchInterfaceLogs.getPage() > 0 ? (searchInterfaceLogs.getPage() - 1) * size : 0;
			long startDate = searchInterfaceLogs.getSrchStartDate();
			long endDate = searchInterfaceLogs.getSrchEndDate();

	        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
	        boolQueryBuilder.filter(QueryBuilders.termQuery("ent_code", searchInterfaceLogs.getSrchEntCode()));
	        boolQueryBuilder.filter(QueryBuilders.rangeQuery(searchInterfaceLogs.getSrchDateGbn()).gte(startDate).lte(endDate));
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchSolution()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("solution", searchInterfaceLogs.getSrchSolution()));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchBound()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("bound", searchInterfaceLogs.getSrchBound()));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchBoundType()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("bound_type", searchInterfaceLogs.getSrchBoundType()));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfDocCode()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("if_doc_code", searchInterfaceLogs.getSrchIfDocCode()));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfDocReferenceNo()))) {
	        	boolQueryBuilder.filter(QueryBuilders.wildcardQuery("if_doc_reference_no", searchInterfaceLogs.getSrchIfDocReferenceNo() + "*"));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchRegisterUserId()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("register_user_id", searchInterfaceLogs.getSrchRegisterUserId()));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfSendRecvType()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("if_send_recv_type", searchInterfaceLogs.getSrchIfSendRecvType()));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchHblNo()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("hbl_no", searchInterfaceLogs.getSrchHblNo()));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfSenderId()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("if_sender_id", searchInterfaceLogs.getSrchIfSenderId()));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfReceiverId()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("if_receiver_id", searchInterfaceLogs.getSrchIfReceiverId()));
	        }
//	        if("ERROR".equals(searchInterfaceLogs.getSrchIfStatus())){
//	        	boolQueryBuilder.filter(QueryBuilders.termQuery("if_status", "ERROR"));
//	        }else if("SUCCESS".equals(searchInterfaceLogs.getSrchIfStatus())){
//	        	boolQueryBuilder.mustNot(QueryBuilders.termQuery("if_status", "ERROR"));
//	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfStatus()))) {
	        	boolQueryBuilder.filter(QueryBuilders.termQuery("if_status", searchInterfaceLogs.getSrchIfStatus()));
	        }
	        
	        SearchApi searchApi = new SearchApi(client);
	        SearchResponse response;
	        if (size == 0) {
	        	/** 시스템 개발자(KRadmin)만 최대 10000건 엑셀 다운로드 */
	        	if ("AD".equals(Util.null2str(searchInterfaceLogs.getUserAuth()))) {
	        		response = searchApi.searchScroll(boolQueryBuilder, 10000, indices, searchInterfaceLogs.getSrchScrollId(), sortList);
	        	}else {
	        		response = searchApi.searchScroll(boolQueryBuilder, elasticsearchScrollPage_, indices, searchInterfaceLogs.getSrchScrollId(), sortList);
	        	}
	        }else {
	        	response = searchApi.search(boolQueryBuilder, from, size, 15000, indices, sortList);
	        }
	        
	        String scrollId = response.getScrollId();
	        interfaceLogsList.setScrollId(scrollId);
	        
	    	SearchHits hits = response.getHits();
	    	
			final ObjectMapper mapper = new ObjectMapper();
			mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setSerializationInclusion(Include.NON_NULL);
			
	    	SearchHit[] resultHits = hits.getHits();
	    	if (size == 0)	interfaceLogsList.setTotal(resultHits.length);
	    	else			interfaceLogsList.setTotal(hits.getTotalHits().value);
	    	
	    	List<InterfaceLogs> list = new ArrayList<InterfaceLogs>();
	    	if (resultHits.length > 0) {
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		    	for (SearchHit hit : resultHits) {
		    		InterfaceLogs processLogs = mapper.readValue(hit.getSourceAsString(), InterfaceLogs.class);
		    		processLogs.setIndex(hit.getIndex());
	    			processLogs.setRegisterDateTimeS(sdf.format(Util.null2long(processLogs.getRegisterDateTime())));
		    		list.add(processLogs);
		        }
		    	
	    	} else {
	    		// 메모리 확보를 위해 ScrollId를 ElasticSearch에서 삭제한다.
	    		if (!"".equals(Util.null2str(searchInterfaceLogs.getSrchScrollId()))) {
	    			searchApi.clearScollRequest(searchInterfaceLogs.getSrchScrollId());
	    		}
	    	}
	    	
	    	interfaceLogsList.setInterfaceLogsList(list);
			
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		} finally {}
       
       return interfaceLogsList;
	}
	
	/**
	 * 인터페이스 로그
	 * @param index
	 * @param referenceNoId
	 * @return InterfaceLogs
	 * @throws Exception
	 */
	public InterfaceLogs getInterfaceLogs(String index, String referenceNoId) throws Exception {
		InterfaceLogs interfaceLogs = null;

		final ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		DocumentApi documentApi = new DocumentApi(client);
		
		GetResponse response = documentApi.getDocument(index, referenceNoId);
		interfaceLogs = mapper.convertValue(response.getSourceAsMap(), InterfaceLogs.class);
		
		String filePath = Util.null2str(interfaceLogs.getIfFilePath());
		String fileName = Util.null2str(interfaceLogs.getIfFileName());
		if(!filePath.equals("") && !fileName.equals("")){
			
			File file = new File(filePath + fileName);
			if(file.exists()){
				BufferedReader br = null;
				try{
					br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
					String lineStr = "";
					StringBuffer fileStr = new StringBuffer();
					while((lineStr = br.readLine()) != null){
						fileStr.append(lineStr);
	    			}
					interfaceLogs.setFileData(fileStr.toString());
				}catch(Exception e){
					throw e;
				}finally{
	    			if (br != null) try{ br.close(); }catch(IOException e){log.error(e.getMessage());}
	    		}
			}
		}
		return interfaceLogs;
	}
	
	
	/**
	 * 인터페이스 로그 삭제 개수 카운트
	 * @param searchInterfaceLogs
	 * @return int
	 * @throws Exception
	 */
	public int deleteInterfaceLogsTargetCount(SearchInterfaceLogs searchInterfaceLogs) throws Exception {
		InterfaceLogsList interfaceLogList = interfaceLogList(searchInterfaceLogs);
		return (int) interfaceLogList.getTotal();
	}
	
	
	/**
	 * 인터페이스 로그 삭제
	 * @param searchInterfaceLogs
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Integer> deleteInterfaceLogs(SearchInterfaceLogs searchInterfaceLogs) throws Exception {
		
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		DocumentApi documentApi = new DocumentApi(client);
		
		int totalCnt = 0;
		int successCnt = 0;
		int failCnt = 0;
		
		//선택한 데이터 삭제하는 경우
		if("chkData".equals(searchInterfaceLogs.getTargetData())) {
			
			totalCnt = searchInterfaceLogs.getCheckDataList().size();
			
			for(int i=0; i<searchInterfaceLogs.getCheckDataList().size(); i++) {
				
				DeleteResponse response = documentApi.deleteDocument(Util.null2str(searchInterfaceLogs.getCheckDataList().get(i).get("index")), Util.null2str(searchInterfaceLogs.getCheckDataList().get(i).get("esJobNo")));

				if("DELETED".equals(Util.null2str(response.getResult())) || "deleted".equals(Util.null2str(response.getResult()))) {
					successCnt++;
				}else {		//ex.not_found
					failCnt++;
				}
				
			}
			
		//검색조건으로 조회해 데이터 삭제하는 경우	
		}else {
//			List<InterfaceLogs> interfaceLogsList = interfaceLogList(searchInterfaceLogs).getInterfaceLogsList();
			//-> 인터페이스로그(ES) 검색건 삭제 시 pageSize만큼의 데이터만 삭제되어 검색건 전체 삭제되도록 수정함.
			List<InterfaceLogs> interfaceLogsList = new ArrayList<InterfaceLogs>();
			
			int srchDataCnt = searchInterfaceLogs.getSrchDataCnt();			//검색 데이터 건수(화면에서 전달)
			int pageSize = searchInterfaceLogs.getPageSize();				//페이지 사이즈
			int totalPage = (int) Math.ceil((double) srchDataCnt/pageSize);	//몇 페이지까지 있는지 == pageSize만큼의 데이터를 몇 번 가져와야 하는지
			
			//페이지 사이즈 만큼씩 잘라서 데이터 가져오기 -> 전체 데이터 개수가 92개고 페이지 사이즈가 50일 경우 두 번 불러와서 list에 추가 후 삭제하기 위함
			for(int page=1; page<=totalPage; page++) {
				searchInterfaceLogs.setPage(page);
				interfaceLogsList.addAll(interfaceLogList(searchInterfaceLogs).getInterfaceLogsList());
			}
			totalCnt = interfaceLogsList.size();
			
			for(InterfaceLogs il : interfaceLogsList) {
				
				DeleteResponse response = documentApi.deleteDocument(Util.null2str(il.getIndex()), Util.null2str(il.getEsJobNo()));

				if("DELETED".equals(Util.null2str(response.getResult())) || "deleted".equals(Util.null2str(response.getResult()))) {
					successCnt++;
				}else {		//ex.not_found
					failCnt++;
				}
				
			}
		}
       
		Map<String, Integer> resMap = new HashMap<String, Integer>();
		resMap.put("totalCnt", totalCnt);
		resMap.put("successCnt", successCnt);
		resMap.put("failCnt", failCnt);
		
		return resMap;
	}
}
