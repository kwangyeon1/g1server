package org.yht.exps.express.service.bms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
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
import org.yht.exps.express.model.sms.logs.InterfaceLogs;
import org.yht.exps.express.model.sms.logs.InterfaceLogsList;
import org.yht.exps.express.model.sms.logs.search.SearchInterfaceLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.util.Converter;
import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsLogsService {
    @Autowired
    CommonService commonService;
    @Autowired
	CommonMapper commonMapper;
	@Autowired
	RestHighLevelClient client;
	
	@Value("${custom.elasticsearch-scroll-page}")
	private int elasticsearchScrollPage_;
	
	/**
	 * 인터페이스 로그 리스트 조회
	 * @param searchInterfaceLogs
	 * @return InterfaceLogsList
	 * @throws Exception
	 */
	public InterfaceLogsList interfaceLogList(SearchInterfaceLogs searchInterfaceLogs) throws Exception {
		InterfaceLogsList interfaceLogsList = new InterfaceLogsList();

       try {
    	   if("notSearch".equals(searchInterfaceLogs.getSrchMode())){
    		   interfaceLogsList.setTotal(0);
    		   interfaceLogsList.setInterfaceLogsList(new ArrayList<InterfaceLogs>());
    		   return interfaceLogsList;
    	   }
    	   
			// 검색할 Indices
	        String[] indices = new String[1];
	        indices[0] = "bms-interface-logs-*";
       	
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
	        
	        boolQueryBuilder.must(QueryBuilders.matchQuery("ent_code", searchInterfaceLogs.getSrchEntCode()).operator(Operator.AND));
	        // term query -> match query
	        //boolQueryBuilder.filter(QueryBuilders.termQuery("ent_code", searchInterfaceLogs.getSrchEntCode()));
	        
	        boolQueryBuilder.filter(QueryBuilders.rangeQuery(searchInterfaceLogs.getSrchDateGbn()).gte(startDate).lte(endDate));
	        
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchSolution()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("solution", searchInterfaceLogs.getSrchSolution()).operator(Operator.AND));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchBound()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("bound", searchInterfaceLogs.getSrchBound()).operator(Operator.AND));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchBoundType()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("bound_type", searchInterfaceLogs.getSrchBoundType()).operator(Operator.AND));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfDocCode()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("if_doc_code", searchInterfaceLogs.getSrchIfDocCode()).operator(Operator.AND));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfDocReferenceNo()))) {
	        	boolQueryBuilder.filter(QueryBuilders.wildcardQuery("if_doc_reference_no", searchInterfaceLogs.getSrchIfDocReferenceNo() + "*"));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchRegisterUserId()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("register_user_id", searchInterfaceLogs.getSrchRegisterUserId()).operator(Operator.AND));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfSendRecvType()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("if_send_recv_type", searchInterfaceLogs.getSrchIfSendRecvType()).operator(Operator.AND));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchHblNo()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("hbl_no", searchInterfaceLogs.getSrchHblNo()).operator(Operator.AND));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfSenderId()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("if_sender_id", searchInterfaceLogs.getSrchIfSenderId()).operator(Operator.AND));
	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfReceiverId()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("if_receiver_id", searchInterfaceLogs.getSrchIfReceiverId()).operator(Operator.AND));
	        }
//	        if("ERROR".equals(searchInterfaceLogs.getSrchIfStatus())){
//	        	boolQueryBuilder.filter(QueryBuilders.termQuery("if_status", "ERROR"));
//	        }else if("SUCCESS".equals(searchInterfaceLogs.getSrchIfStatus())){
//	        	boolQueryBuilder.mustNot(QueryBuilders.termQuery("if_status", "ERROR"));
//	        }
	        if(!"".equals(Util.null2str(searchInterfaceLogs.getSrchIfStatus()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("if_status", searchInterfaceLogs.getSrchIfStatus()).operator(Operator.AND));
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
			log.error(e.getMessage());
			e.printStackTrace();
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
		if(response != null) {
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
						if (br != null) try{ br.close(); }catch(IOException e){}
					}
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
