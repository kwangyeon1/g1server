package org.yht.exps.express.service.bms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import org.yht.elasticsearch.client.api.SearchApi;
import org.yht.elasticsearch.client.data.SortData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsXraySendMapper;
import org.yht.exps.express.model.bms.bmsXraySend.XraySend;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendBl;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendBlList;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendList;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendLog;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendLogList;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendSaveResult;
import org.yht.exps.express.model.bms.bmsXraySend.search.SearchXraySend;
import org.yht.exps.express.model.bms.bmsXraySend.search.SearchXraySendLog;
import org.yht.exps.express.model.search.Sort;
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
public class BmsXraySendService {
    @Autowired
    CommonService commonService;
    @Autowired
	CommonMapper commonMapper;
    @Autowired
    BmsXraySendMapper bmsXraySendMapper;
	@Autowired
	RestHighLevelClient client;
	
	@Value("${custom.elasticsearch-scroll-page}")
	private int elasticsearchScrollPage_;
	
    /**
     * xray 재전송 리스트 조회
     * @param search
     * @return
     * @throws Exception
     */
	public XraySendList xraySendList(SearchXraySend search) throws Exception {
		XraySendList xraySendList = new XraySendList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
 		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
 		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchHblNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchHblNo())).split(",")));
		}
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchHblNo(Util.null2str(search.getSrchHblNo()).trim().replace(",", ""));
		}
		
		List<XraySend> list = new ArrayList<XraySend>();
		if(search.getSrchSendStatus() != null && !"".equals(Util.null2str(search.getSrchSendStatus()))) {
			list = bmsXraySendMapper.selectXraySendTypeList(search);
		}else {
			list = bmsXraySendMapper.selectXraySendList(search);
		}
		xraySendList.setXraySendList(list);
		
		if(list.size() > 0) {
			xraySendList.setTotal(list.get(0).getTotal());
		}else {
			xraySendList.setTotal(0);
		}
		return xraySendList;
	}

	/**
	 * xray 재전송 상태 변경
	 * @param searchXraySend
	 * @return
	 */
	public XraySendSaveResult updateXraySend(SearchXraySend searchXraySend) {
		XraySendSaveResult saveResult = new XraySendSaveResult();
		if(searchXraySend.getSeqXrayList() != null && searchXraySend.getSeqXrayList().size() > 0) {
			bmsXraySendMapper.updateXraySend(searchXraySend);
		}
		if(searchXraySend.getSeqApprovalList() != null && searchXraySend.getSeqApprovalList().size() > 0) {
			bmsXraySendMapper.updateReleaseArrovalSend(searchXraySend);
		}
		return saveResult;
	}

	public XraySendLogList xraySendLogList(SearchXraySendLog search) {
		XraySendLogList xraySendLogList = new XraySendLogList();
		try {
			
			// 검색할 Indices
	        String[] indices = new String[1];
	        indices[0] = "bms-interface-logs-*";
	        
	        // Sort 조건 처리
	        List<SortData> sortList = new ArrayList<SortData>();
	        
	        if (search.getSort() == null || search.getSort().size() == 0) {
		        SortData sortData1 = new SortData();
		        sortData1.setField("register_date_time");
		        sortData1.setType(SortOrder.DESC);
		        sortList.add(sortData1);
	        } else {
	        	for (int i = 0; i < search.getSort().size(); i++) {
	        		Sort data = search.getSort().get(i);
	    	        SortData sortData = new SortData();
			        sortData.setField(Converter.camelToSnake(data.getField()));
			        if ("asc".equals(data.getDir()))	sortData.setType(SortOrder.ASC);
			        else								sortData.setType(SortOrder.DESC);
			        sortList.add(sortData);
	        	}
	        }
	        
	        // Parameter 처리
			int size = search.getPageSize();
	        int from = search.getPage() > 0 ? (search.getPage() - 1) * size : 0;
			long startDate = search.getSrchStartDate();
			long endDate = search.getSrchEndDate();

	        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
	        
	        boolQueryBuilder.must(QueryBuilders.matchQuery("ent_code", search.getSrchEntCode()).operator(Operator.AND));
	        // term query -> match query
	        //boolQueryBuilder.filter(QueryBuilders.termQuery("ent_code", searchInterfaceLogs.getSrchEntCode()));
	        
//	        boolQueryBuilder.filter(QueryBuilders.rangeQuery(search.getSrchDateGbn()).gte(startDate).lte(endDate));
	        
	        if(!"".equals(Util.null2str(search.getSrchBoundType()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("bound_type", search.getSrchBoundType()).operator(Operator.AND));
	        }
	        
	        if(!"".equals(Util.null2str(search.getSrchIfDocCode()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("if_doc_code", search.getSrchIfDocCode()).operator(Operator.AND));
	        }
	        if(!"".equals(Util.null2str(search.getSrchHblNo()))) {
	        	boolQueryBuilder.must(QueryBuilders.matchQuery("hbl_no", search.getSrchHblNo()).operator(Operator.AND));
	        }
	        
	        SearchApi searchApi = new SearchApi(client);
	        SearchResponse response;
	        if (size == 0) {
	        	/** 시스템 개발자(KRadmin)만 최대 10000건 엑셀 다운로드 */
        		response = searchApi.searchScroll(boolQueryBuilder, elasticsearchScrollPage_, indices, search.getSrchScrollId(), sortList);
	        }else {
	        	response = searchApi.search(boolQueryBuilder, from, size, 15000, indices, sortList);
	        }
	        
	        
	        String scrollId = response.getScrollId();
	        xraySendLogList.setScrollId(scrollId);
	        
	    	SearchHits hits = response.getHits();
	    	
			final ObjectMapper mapper = new ObjectMapper();
			mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setSerializationInclusion(Include.NON_NULL);
			
	    	SearchHit[] resultHits = hits.getHits();
	    	if (size == 0)	xraySendLogList.setTotal(resultHits.length);
	    	else			xraySendLogList.setTotal(hits.getTotalHits().value);
	    	
	    	List<XraySendLog> list = new ArrayList<XraySendLog>();
	    	if (resultHits.length > 0) {
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    	for (SearchHit hit : resultHits) {
		    		XraySendLog processLogs = mapper.readValue(hit.getSourceAsString(), XraySendLog.class);
		    		processLogs.setIndex(hit.getIndex());
		    		processLogs.setRegisterDateTimeS(sdf.format(Util.null2long(processLogs.getRegisterDateTime())));
		    		list.add(processLogs);
		        }
		    	
	    	} else {
	    		// 메모리 확보를 위해 ScrollId를 ElasticSearch에서 삭제한다.
	    		if (!"".equals(Util.null2str(search.getSrchScrollId()))) {
	    			searchApi.clearScollRequest(search.getSrchScrollId());
	    		}
	    	}
			
	    	xraySendLogList.setXraySendLogList(list);
	    	
		}catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {}
		
		return xraySendLogList;
	}
	
	/**
     * xray 재전송 리스트 조회
     * @param search
     * @return
     * @throws Exception
     */
	public XraySendBlList selectXraySendBl(SearchXraySend search) throws Exception {
		XraySendBlList xraySendBlList = new XraySendBlList();
		
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
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

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|P.AR_DATE|P.APPROVAL_DATE|P.APPROVAL_CANCEL_DATE", null, null);
		List<XraySendBl> bmsOutxraySendBlList  = bmsXraySendMapper.selectXraySendBl(search);
		if(bmsOutxraySendBlList.size() > 0) {
			xraySendBlList.setTotal(bmsOutxraySendBlList.get(0).getTotal());
		}else {
			xraySendBlList.setTotal(0);
		}
		xraySendBlList.setXraySendBlList(bmsOutxraySendBlList);
		return xraySendBlList;
	}
	
	/**
	 * xray > ADD BL
	 * @param searchXraySend
	 * @return
	 * @throws Exception
	 */
	public XraySendSaveResult addXraySendBl(SearchXraySend searchXraySend) throws Exception {
		XraySendSaveResult xraySendSaveResult = new XraySendSaveResult();
		if("xrayScan".equals(Util.null2str(searchXraySend.getSrchSendStatus()))) {
			for(XraySendBl xraySendBl : searchXraySend.getXraySendBlList()) {
				bmsXraySendMapper.insertXraySend(xraySendBl);
			}
		}else if("releaseApproval".equals(Util.null2str(searchXraySend.getSrchSendStatus()))) {
			for(XraySendBl xraySendBl : searchXraySend.getXraySendBlList()) {
				bmsXraySendMapper.insertReleaseArrovalSend(xraySendBl);
			}
		}
		return xraySendSaveResult;
	}
}
