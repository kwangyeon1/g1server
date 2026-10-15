package org.yht.exps.express.service.eCommerce;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.PodMapper;
import org.yht.exps.express.model.eCommerce.pod.PodInfo;
import org.yht.exps.express.model.eCommerce.pod.PodList;
import org.yht.exps.express.model.eCommerce.pod.PodSaveResult;
import org.yht.exps.express.model.eCommerce.pod.search.SearchPodList;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PodService {

	@Autowired
	CommonMapper commonMapper;
	@Autowired
	PodMapper podMapper;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;

	@Autowired
	TrackingPodInfoSendParser trackingPodInfoSendParser;
	@Resource(name = "trackingOrderDeliveryParser")
	TrackingOrderDeliveryParser trackingOrderDeliveryParser;
	/**
	 * Pod List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public PodList selectPodList(SearchPodList search) throws Exception {
		PodList podList = new PodList();
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
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, null, null, "srchNoType", "rst.HBL_NO|rst.PO_NO|rst.RETURN_REQUEST_ID");
		List<PodInfo> list  = podMapper.selectPodList(search);
		podList.setPodList(list);
		if(list.size() > 0) {
			podList.setTotal(list.get(0).getTotal());
		}else {
			podList.setTotal(0);
		}
		return podList;
	}
	
	/**
	 * Pod Tracking
	 * @param searchPodList
	 * @return
	 * @throws Exception
	 */
	public PodSaveResult tracking(SearchPodList search) throws Exception{
		PodSaveResult podSaveResult = new PodSaveResult();
		trackingPodInfoSendParser.callTrackingPodInfoSendExecutor(search);
		return podSaveResult;
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
		if(documentApi.existDocument(index, referenceNoId)) {
			GetResponse response = documentApi.getDocument(index, referenceNoId);
			processLogs = mapper.convertValue(response.getSourceAsMap(), ProcessLogs.class);
		}
      
		return processLogs;
	}
	
	/**
	 * Pod Update
	 * @param searchPodList
	 * @return
	 * @throws Exception
	 */
	public PodSaveResult podUpdate(SearchPodList search) throws Exception{
		PodSaveResult podSaveResult = new PodSaveResult();
		String deliveryCode = null;
		for(String odJobNo : search.getSearchNoList()) {
			Map<String,Object> searchDeliveryMap = new HashMap<>();
			searchDeliveryMap.put("odJobNo", odJobNo);
			//멀티건 확인
			List<Map<String,Object>> multiList = podMapper.selectOrderMultiList(searchDeliveryMap);
			if(multiList != null && multiList.size() > 0) {
				for(Map<String,Object> multiMap : multiList) {
					deliveryCode = "".equals(Util.null2str(multiMap.get("multiChangeDeliveryCode"))) ? Util.null2str(multiMap.get("deliveryCode")) : Util.null2str(multiMap.get("multiChangeDeliveryCode")); 
					multiMap.put("entCode", Util.null2str(search.getSrchEntCode()));
					if("KOREX".equals(deliveryCode)) {
						long result = trackingOrderDeliveryParser.trackingProcessKorexPlus(multiMap,Util.null2str(search.getSrchEntCode()));
					}else if("KD".equals(deliveryCode)) {
						long result = trackingOrderDeliveryParser.trackingProcessKD(multiMap,Util.null2str(search.getSrchEntCode()));		
					}
				}
			}
		}
		
		return podSaveResult;

	}

	/**
	 * Pod(Common) List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public PodList selectPodListCommon(SearchPodList search) throws Exception {
		PodList podList = new PodList();
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
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, null, null, "srchNoType", "rst.HBL_NO|rst.PO_NO|rst.RETURN_REQUEST_ID");
		List<PodInfo> list  = podMapper.selectPodListCommon(search);
		podList.setPodList(list);
		if(list.size() > 0) {
			podList.setTotal(list.get(0).getTotal());
		}else {
			podList.setTotal(0);
		}
		return podList;
	}
}