package org.yht.exps.express.service.eCommerce;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.PcccMapper;
import org.yht.exps.express.mapper.systemMng.SystemMngMapper;
import org.yht.exps.express.model.eCommerce.pccc.CustomsCodeVerifiedInfo;
import org.yht.exps.express.model.eCommerce.pccc.CustomsCodeVerifiedInfo.PersEcmQryRtnErrInfo;
import org.yht.exps.express.model.eCommerce.pccc.Pccc;
import org.yht.exps.express.model.eCommerce.pccc.PcccBatch;
import org.yht.exps.express.model.eCommerce.pccc.PcccBatchList;
import org.yht.exps.express.model.eCommerce.pccc.PcccList;
import org.yht.exps.express.model.eCommerce.pccc.PcccSaveResult;
import org.yht.exps.express.model.eCommerce.pccc.PodDetail;
import org.yht.exps.express.model.eCommerce.pccc.search.SearchPcccList;
import org.yht.exps.express.model.eCommerce.pccc.search.SearchPod;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.InterfaceLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.OrderCommonService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.EncryptionUtils;
import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PcccService {

	@Autowired
	CommonMapper commonMapper;
	@Autowired
	PcccMapper pcccMapper;
	@Autowired
	SystemMngMapper systemMngMapper;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	CustomProperties customProperties;
	@Resource(name = "fileService")
	FileService fileService;

	@Resource(name = "trackingOrderDeliveryParser")
	TrackingOrderDeliveryParser trackingOrderDeliveryParser;
	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name = "orderCommonService")
	OrderCommonService orderCommonService;
	
	/**
	 * Pccc List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public PcccList selectPcccList(SearchPcccList search) throws Exception {
		PcccList pcccList = new PcccList();
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
		Util.accessControlDatabase(search, "userLanguage|srchCusType|srchDateFormat", true, null, null, "srchNoType", "|cusId.HBL_NO|cusId.PO_NO");
		List<Pccc> list  = pcccMapper.selectPcccList(search);
		pcccList.setPcccList(list);
		if(list.size() > 0) {
			pcccList.setTotal(list.get(0).getTotal());
		}else {
			pcccList.setTotal(0);
		}
		return pcccList;
	}
	
	/**
	 * Pccc Detail
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Pccc selectPcccDetail(SearchPcccList search) throws Exception {
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		Pccc pccc  = pcccMapper.selectPcccDetail(search);
		if(pccc != null) {
			// 상세화면팝업에서 알람톡 History 조회
			if("detail".equals(Util.null2str(search.getSearchType()))) {
				pccc.setAlarmList(pcccMapper.selectAlarm(search));
			}
		}
		
		return pccc;
	}
	
	/**
	 * Pccc Verify
	 * @param pccc
	 * @return
	 * @throws Exception
	 */
	public PcccSaveResult pcccCustomsVerify(Pccc pccc){
		PcccSaveResult pcccSaveResult = new PcccSaveResult();
		pcccSaveResult.setResult(true);
		long startTime = 0;
		try {
			// 도입사별 관세청 개인통관부호 인증키 조회
			String unipassCustomsVerifyKey = Util.null2str(pcccMapper.selectUnipassCustomsVerifyKey(Util.null2str(pccc.getEntCode())));
			// 개인통관고유부호
			String customsId = null;
			if(!"".equals(Util.null2str(pccc.getAfterCustomsId()))){
				customsId = Util.null2str(pccc.getAfterCustomsId());
			}else {
				customsId = Util.null2str(pccc.getOrigCustomsId());
			}
			// 휴대전화번호
			String mobile = null;
			if(!"".equals(Util.null2str(pccc.getAfterConsigneeHp()))){
				mobile = Util.null2str(pccc.getAfterConsigneeHp()).replaceAll("[^0-9]", "");
			}else {
				mobile = Util.null2str(pccc.getOrigConsigneeHp()).replaceAll("[^0-9]", "");
			}
			// 이름
			String name = null;
			if(!"".equals(Util.null2str(pccc.getAfterConsigneeName()))){
				name = Util.null2str(pccc.getAfterConsigneeName());
			}else {
				name = Util.null2str(pccc.getOrigConsigneeName());
			}
			
			if(pcccSaveResult.getResult()){
				// UNIPASS API 접근
				URI uri = null;
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
				
				startTime = System.currentTimeMillis();
				UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
				        .scheme("https")
				        .host("unipass.customs.go.kr")
				        .port("38010")
				        .path("/ext/rest/persEcmQry/retrievePersEcm")
				        .queryParam("crkyCn", URLDecoder.decode(Util.null2str(unipassCustomsVerifyKey), "UTF-8"))
				        .queryParam("persEcm", customsId)
				        .queryParam("pltxNm", name)
				        .queryParam("cralTelno", mobile);	//핸드폰번호 없는 경우 전화번호로 검증
				
				//우편번호
				String zipCode = null;
				if(!"".equals(Util.null2str(pccc.getAfterConsigneeZipCode()))){
						zipCode = Util.null2str(pccc.getAfterConsigneeZipCode());
				}else {
					if(!"".equals(Util.null2str(pccc.getOrigConsigneeZipCode()))){
						zipCode = Util.null2str(pccc.getOrigConsigneeZipCode());
					}
				}
				if(!"".equals(zipCode)) {
					uriComponentsBuilder.queryParam("custPsno", zipCode);
				}
				
				// 영문 성,이름
				String consigneeLName = null; //영문 성
				String consigneeFName = null; //영문 이름
				if(!"".equals(Util.null2str(pccc.getAfterConsigneeLName())) && !"".equals(Util.null2str(pccc.getAfterConsigneeFName()))) {
					consigneeLName = Util.null2str(pccc.getAfterConsigneeLName());
					consigneeFName = Util.null2str(pccc.getAfterConsigneeFName());
				}else if(!"".equals(Util.null2str(pccc.getOrigConsigneeLName())) && !"".equals(Util.null2str(pccc.getOrigConsigneeFName()))) {
					consigneeLName = Util.null2str(pccc.getOrigConsigneeLName());
					consigneeFName = Util.null2str(pccc.getOrigConsigneeFName());
				}
				
				if(!"".equals(consigneeLName) && !"".equals(consigneeFName)) {
					uriComponentsBuilder.queryParam("englFmnm", consigneeLName);
					uriComponentsBuilder.queryParam("englNm", consigneeFName);
				}
				
				uri = uriComponentsBuilder.build().encode().toUri();
				
				// 유호기간 시작일자 ex) 2026-01-23
				// 유효기간 만료일자 ex) 2026-03-23
				CustomsCodeVerifiedInfo customsCodeVerifiedInfo = restTemplate.getForObject(uri, CustomsCodeVerifiedInfo.class);			
				String tCnt = Util.null2str(customsCodeVerifiedInfo.getTCnt());
				String valtPridStrtDt = Util.null2str(customsCodeVerifiedInfo.getValtPridStrtDt()); /* 유효기간 시작일자 */
				if(!"".equals(valtPridStrtDt) && valtPridStrtDt.length() == 8) {
					valtPridStrtDt = valtPridStrtDt.substring(0, 4) + "-"
							+ valtPridStrtDt.substring(4, 6) + "-"
							+ valtPridStrtDt.substring(6, 8);
				}
				String valtPridXpirDt = Util.null2str(customsCodeVerifiedInfo.getValtPridXpirDt()); /* 유효기간 만료일자 */
				if(!"".equals(valtPridStrtDt) && valtPridXpirDt.length() == 8) {
					valtPridXpirDt = valtPridXpirDt.substring(0, 4) + "-"
							+ valtPridXpirDt.substring(4, 6) + "-"
							+ valtPridXpirDt.substring(6, 8);
				}
				
				if("2027-02-29".equals(valtPridStrtDt)) {
					valtPridStrtDt = "2027-02-28";
				}
				if("2027-02-29".equals(valtPridXpirDt)) {
					valtPridXpirDt = "2027-02-28";
				}
				
				String errorReson = null;
				long endTime = System.currentTimeMillis() - startTime;
				long apiTime = endTime / 1000;
				//2024.09.24 unipass api 시작시간 끝난 시간 체크해서 5초 이상 걸린경우 로그 남김.
				if(apiTime > 4) {
					log.info("entCode : " + Util.null2str(pccc.getEntCode()) + " customsCodeVerification unipass api running time : " + endTime + "초");
				}
				
				if("0".equals(tCnt)) {	/** 실패 */
					List<PersEcmQryRtnErrInfo> persEcmQryRtnErrInfos = customsCodeVerifiedInfo.getPersEcmQryRtnErrInfos();
					if(persEcmQryRtnErrInfos != null){
						if(persEcmQryRtnErrInfos.size() > 1){
							String errorMsg = "";
							for(int i=0; i<persEcmQryRtnErrInfos.size(); i++) {
								String errMsgCn = Util.null2str(persEcmQryRtnErrInfos.get(i).getErrMsgCn());
								if(errMsgCn.length() > 1) {
									errorMsg += errMsgCn.replace("+"," ").split("\\.")[0] + "." + " ";
								}
							}
							
							//300자가 넘으면 295자까지 자르기
							errorMsg = errorMsg.length() > 300 ? errorMsg.substring(0, 295) : errorMsg;
							
							pcccSaveResult.setResult(false);
							pcccSaveResult.setCustomsIdVerify("N");
							pcccSaveResult.setCustomsIdError(errorMsg);
							
						}else {
							errorReson = Util.null2str(persEcmQryRtnErrInfos.get(0).getErrMsgCn());
							if(errorReson.contains("일치하지 않습니다.")){
								//2025.01.19 일치하지 않는 건도 오류로 처리 
								pcccSaveResult.setResult(false);
								pcccSaveResult.setCustomsIdVerify("N");
								pcccSaveResult.setCustomsIdError(errorReson.replace("+"," ").split("\\.")[0] + ".");
							}else if(errorReson.contains("존재하지 않습니다.") || errorReson.contains("현재 사용중이 아닌")){
								pcccSaveResult.setResult(false);
								pcccSaveResult.setCustomsIdVerify("N");
								pcccSaveResult.setCustomsIdError(errorReson.replace("+"," ").split("\\.")[0] + ".");
							}else if(errorReson.contains("유효기간이")) {
								pcccSaveResult.setResult(false);
								pcccSaveResult.setCustomsIdVerify("N");
								pcccSaveResult.setCustomsIdError(errorReson.replace("+"," ").split("\\.")[0] + ".");
							}
						}
					}else{
						pcccSaveResult.setResult(false);
						pcccSaveResult.setCustomsIdVerify("N");
						pcccSaveResult.setCustomsIdError("");
					}
                }else if("-1".equals(tCnt)){
                	pcccSaveResult.setResult(false);
                	pcccSaveResult.setCustomsIdVerify("N");
                	pcccSaveResult.setCustomsIdError(customsCodeVerifiedInfo.getNtceInfo());
            	} else {
            		//검증완료 결과 처리(customsIdJobNo가 있는경우에만 처리)
            		if(!"".equals(Util.null2str(pccc.getCustomsIdJobNo()))) {
            			Map<String, Object> result = new HashMap<String, Object>();
                		result.put("odJobNo", Util.null2str(pccc.getCustomsIdJobNo()));
                		result.put("entCode", Util.null2str(pccc.getEntCode()));
                		result.put("customsId", customsId);
                		result.put("consigneeName", name);
                		result.put("consigneeHp", mobile);
                		result.put("zipCode", zipCode);
                		result.put("consigneeFName", consigneeFName);
                		result.put("consigneeLName", consigneeLName);
                		result.put("gmtTime", Util.null2str(pccc.getGmtTime()));
                		result.put("updateUserId", Util.null2str(pccc.getUpdateUserId()));
                		result.put("status", "05");
                		
                		//검증결과 정상시 저장실행
                		Util.accessControlDatabase(result, "gmtTime", false, null, null, null, null);
                		pcccMapper.updatePccc(result);

                		customsCodeCorrectedSend(result);
            		}
            		
            		pcccSaveResult.setCustomsIdVerify("Y");
            		pcccSaveResult.setCustomsIdError("정상");
            		pcccSaveResult.setCustomsIdStartDate(valtPridStrtDt);
            		pcccSaveResult.setCustomsIdEndDate(valtPridXpirDt);
                }
				
			}
		}catch(Exception e) {
			log.error(e.getMessage());
		}
		return pcccSaveResult;
	}
	
	/**
	 * Pccc 알람톡 발송금지
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public PcccSaveResult alarmSendStop(PcccList list) throws Exception {
		PcccSaveResult pcccSaveResult = new PcccSaveResult();
		Util.accessControlDatabase(list, "gmtTime", false, null, null, null, null);
		pcccMapper.updateAlarmSendStop(list);
		
		return pcccSaveResult;
	}
	
	/**
	 * Pccc Batch List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public PcccBatchList selectPcccBatchList(SearchPcccList search) throws Exception {
		PcccBatchList pcccBatchList = new PcccBatchList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<PcccBatch> list  = pcccMapper.selectPcccBatchList(search);
		pcccBatchList.setPcccBatchList(list);
		if(list.size() > 0) {
			pcccBatchList.setTotal(list.get(0).getTotal());
		}else {
			pcccBatchList.setTotal(0);
		}
		return pcccBatchList;
	}
	
	/**
	 * Pccc 알림톡 설정저장
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public PcccSaveResult pcccBatchSave(PcccBatch pcccBatch) throws Exception {
		PcccSaveResult pcccSaveResult = new PcccSaveResult();
		Boolean flag = true;
		Util.accessControlDatabase(pcccBatch, "gmtTime", false, null, null, null, null);
		//상위업체 설정된 데이터 확인
		PcccBatch data = pcccMapper.selectPcccBatch(pcccBatch);
		if(data != null) {
			pcccBatch.setAction("EDIT");
		}
		if("NEW".equals(pcccBatch.getAction())){
			String pcccSetJobNo = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			pcccBatch.setPcccSetJobNo(pcccSetJobNo);
			pcccMapper.insertPcccBatch(pcccBatch);
		}else if("EDIT".equals(pcccBatch.getAction())) {
			pcccMapper.updatePcccBatch(pcccBatch);
		}else {
			flag = false;
		}
		pcccSaveResult.setResult(flag);
		return pcccSaveResult;
	}
	
	/**
	 * 전자상거래 PCCC BATCH - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public PcccSaveResult deletePcccBatchList(PcccBatchList pcccBatchList, HttpServletRequest request) throws Exception {
		PcccSaveResult orderSaveResult = new PcccSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			PcccMapper pcccMapper = sqlSession.getMapper(PcccMapper.class);
			List<PcccBatch> delPcccBatchList = pcccBatchList.getPcccBatchList();
			
			BulkData bulkData = null;
			String index = "ecm-process-coupang-logs-" + DateTime.getDate("yyyyMMdd");
			Map<String, Object> logMap = null;
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", pcccBatchList.getEntCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", pcccBatchList.getUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "DELETE");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", pcccBatchList.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", pcccBatchList.getLogUserName());
			tmpLogMap.put("process_user_name_eng", pcccBatchList.getLogUserNameEng());
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			int cnt = 1;
			int seq = 1;
			for(PcccBatch pcccBatchDelete : delPcccBatchList) {
				//PCCC BATCH 삭제
				pcccMapper.deletePcccBatch(pcccBatchDelete);
				
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", pcccBatchDelete.getPcccSetJobNo());
				logMap.put("description", "PCCC BATCH DELETE [CUST_CODE_LV1 : "+pcccBatchDelete.getCustCodeLv1() +"]");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				cnt++;
			}
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		commonService.insertLogBulk(bulkList);
		
		// 처리 로그 기록
		return orderSaveResult;
	}
	
	/**
	 * Pccc OutCall
	 * @param pccc
	 * @return
	 * @throws Exception
	 */
	public PcccSaveResult pcccOutCall(Pccc pccc) throws Exception{
		PcccSaveResult pcccSaveResult = new PcccSaveResult();
		Util.accessControlDatabase(pccc, "gmtTime", false, null, null, null, null);
		pcccMapper.updatePcccOutCall(pccc);
		return pcccSaveResult;
	}
	
	/**
	 * POD Detail
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public PodDetail selectPodDetail(SearchPod search) throws Exception {
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		
		//멀티송장번호 리스트조회
		List<Map<String, Object>> multiNoList = pcccMapper.selectMultiNoList(search);
		
		//팝업창에서 Multi송장번호로 들어오는 경우에는 Muti의 부모 하우스번호를 찾는다.
		Map<String,Object> odMap = new HashMap<String,Object>();
		if("srchMultiNo".equals(Util.null2str(search.getSrchNoType()))) {
			odMap = pcccMapper.selectOdInfo(search);
			if(odMap != null) {
				search.setMultiDeliveryNo(search.getSrchNo());
				search.setSrchNoType("srchBlNo");
				search.setSrchNo(Util.null2str(odMap.get("HBL_NO")));
			}else {
				search.setSrchNoType("srchBlNo");
				search.setSrchNo("null");
			}
		}else if(multiNoList != null && multiNoList.size() > 0) {
			String srchNo = search.getSrchNo();
			List<Map<String, Object>> orderRows = multiNoList.stream()
                    .filter(d -> srchNo.equals(String.valueOf(d.get("CODE"))))
                    .collect(Collectors.toList());
			
			if (!orderRows.isEmpty()) {
				odMap = pcccMapper.selectOdInfo(search);
				if(odMap != null) {
					search.setMultiDeliveryNo(search.getSrchNo());
					search.setSrchNoType("srchBlNo");
					search.setSrchNo(Util.null2str(odMap.get("HBL_NO")));
				}else {
					search.setSrchNoType("srchBlNo");
					search.setSrchNo("null");
				}
			}
		}
		
		if("detail".equals(Util.null2str(search.getSearchType()))) {
			//개인통관부호 검증실시
			Pccc pccc = pcccMapper.selectCustomsCodeVerificationInfo(search);
			if(pccc != null) {
				PcccSaveResult pcccSaveResult = pcccCustomsVerify(pccc);
				if(pcccSaveResult != null) {
					pcccSaveResult.setOdJobNo(pccc.getOdJobNo());
					pcccMapper.UpdateCustomsCodeVerified(pcccSaveResult);
				}
			}
		}
		
		PodDetail pod  = pcccMapper.selectPodDetail(search);
		if(pod != null) {
			// 상세화면팝업에서 알람톡 History 조회
			if("detail".equals(Util.null2str(search.getSearchType()))) {
				SearchPcccList alarmSearch = new SearchPcccList();
				alarmSearch.setOdJobNo(Util.null2str(search.getOdJobNo()));
				alarmSearch.setSrchNo(Util.null2str(search.getSrchNo()));
				alarmSearch.setSrchNoType(Util.null2str(search.getSrchNoType()));
				pod.setAlarmList(pcccMapper.selectAlarm(alarmSearch));
				
				pod.setMultiNoList(multiNoList);
			}
			if("pod".equals(Util.null2str(search.getSearchType()))) {
				if(Util.null2str(pod.getMrn()) !="" && Util.null2str( pod.getHblNo()) !="") {
					List<Map<String,Object>> cargoList = getCargoList(pod);
					if(cargoList != null) {
						pod.setPodList(cargoList);
					}
				}
			}

			if("delivery".equals(Util.null2str(search.getSearchType()))) {
//				if(Util.null2str(pod.getMrn()) !="" && Util.null2str( pod.getHblNo()) !="") {
					String deliveryCode = pod.getDeliveryCode();
					Map<String,Object>searchDeliveryMap = new HashMap<>();
					searchDeliveryMap.put("srchEntCode",Util.null2str(pod.getEntCode()));
					searchDeliveryMap.put("odJobNo",Util.null2str(pod.getOdJobNo()));
					searchDeliveryMap.put("hblNo",Util.null2str(pod.getHblNo()));
					searchDeliveryMap.put("deliveryCode",Util.null2str(pod.getDeliveryCode()));
					
					
					//멀티건 확인
					List<Map<String,Object>> multiList = pcccMapper.selectOrderMultiList(searchDeliveryMap);
					if(multiList != null && multiList.size() > 0) {
						for(Map<String,Object> multiMap : multiList) {
							
							deliveryCode = "".equals(Util.null2str(multiMap.get("multiChangeDeliveryCode"))) ? Util.null2str(multiMap.get("deliveryCode")) : Util.null2str(multiMap.get("multiChangeDeliveryCode")); 
							
							multiMap.put("entCode", Util.null2str(pod.getEntCode()));
							
							if("KOREX".equals(deliveryCode)) {
								long result = trackingOrderDeliveryParser.trackingProcessKorexPlus(multiMap,Util.null2str(pod.getEntCode()));
							}else if("KD".equals(deliveryCode)) {
								long result = trackingOrderDeliveryParser.trackingProcessKD(multiMap,Util.null2str(pod.getEntCode()));		
							}
						}
					}
					
//					if(!"ECM420".equals(pod.getLastPodStatus()) && "".equals(Util.null2str(search.getNoSessPodYn()))) {
//						
//						//배송사에 따른 트래킹 처리 
//						//대한통운
//						if("KOREX".equals(deliveryCode)) {
////						long result = trackingOrderDeliveryParser.trackingProcessKorexPlus(searchDeliveryMap,Util.null2str(pod.getEntCode()));
//							//경동	
//						}else if("KD".equals(deliveryCode)) {
//						long result = trackingOrderDeliveryParser.trackingProcessKD(searchDeliveryMap,Util.null2str(pod.getEntCode()));		
//						}
//						
//					}
					
					List<Map<String, Object>>getDeliveryResult = new ArrayList<Map<String,Object>>();
					if(!"".equals(Util.null2str(search.getMultiDeliveryNo()))) {
						searchDeliveryMap.put("deliveryNo", search.getMultiDeliveryNo());
						getDeliveryResult = pcccMapper.selectOrderPodStatusMultiList(searchDeliveryMap);
					}else {
						getDeliveryResult = pcccMapper.selectOrderPodStatusList(searchDeliveryMap);
						
					}

					if(getDeliveryResult != null) {
						pod.setDeliveryList(getDeliveryResult);
					}
					//pod.setDeliveryList(pcccMapper.selectOrderPodStatusList(searchDeliveryMap));
//				}
			}
		}
		
		return pod;
	}
	
	/**
	 * getCargoList
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getCargoList(PodDetail search) {
		List<Map<String, Object>> podList = new ArrayList<>();
		String mrn = Util.null2str(search.getMrn());
		if(!"".equals(mrn)) mrn = mrn.substring(0, 2);
		
		String targetUrl = "http://mcube.ne.kr/interface/cargoTracking.do?yhtService=FTIS&mblNo=&hblNo=" 
				+ search.getHblNo().toUpperCase() + "&blYy=20" + mrn;
		
		try {
			org.jsoup.nodes.Document doc = Jsoup.connect(targetUrl).userAgent("Mozilla/5.0").maxBodySize(0).timeout(10000).get();

			org.jsoup.select.Elements rows = doc.select(".summery table tbody:nth-of-type(2) tr");
            for (int i = 0; i < rows.size(); i++) {
                Element row = rows.get(i);
                Element firstTd = row.selectFirst("td:first-child");
                String rowNo = (firstTd != null) ? firstTd.text().trim() : "";

                if (rowNo.matches("\\d+")) {
                    Map<String, Object> item = new HashMap<>();
                    
                    item.put("step", row.select("td:nth-child(2)").text().trim());

                    if (i + 1 < rows.size()) {
                        String dateStr = rows.get(i + 1).select("td:first-child").text().trim();
                        item.put("dateTime", dateStr.replaceAll("\\s+", " "));
                    }

                    podList.add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return podList;
	}
	
	/**
	 * 시스템 로그
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
		
		if(documentApi.existDocument(index, referenceNoId)) {
			GetResponse response = documentApi.getDocument(index, referenceNoId);
			interfaceLogs = mapper.convertValue(response.getSourceAsMap(), InterfaceLogs.class);
		}
      
		return interfaceLogs;
	}
	
	/**
	 * Pccc 알림톡 전송
	 * @param pcccBatch
	 * @return
	 * @throws Exception
	 */
	public PcccSaveResult sendKakao(PcccBatch pcccBatch) throws Exception {

	    String KEY_FIELD = "CUSTOMS_ID_JOB_NO"; 
	    String ERROR_CODE_FIELD = "ERROR_CODE";
	    String ZIP_CODE = "P5";
	    String TEMPLATE_TYPE_ZIP = "COUPANG_CUSTOMS_ZIP";
	    String TEMPLATE_TYPE_GENERAL = "COUPANG_CUSTOMS_ID";

	    PcccSaveResult pcccSaveResult = new PcccSaveResult();
	    Map<String, Object> returnMap = new HashMap<String, Object>();

	    List<Map<String, Object>> dataList = pcccBatch.getDataList();
	    List<Map<String, Object>> sendDataList = pcccMapper.selectSendDataList(dataList);
	    
	    Map<String, String> errorCodeMap = new HashMap<String, String>();
	    for (Map<String, Object> d : dataList) {
	        String key = Util.null2str(d.get(KEY_FIELD));
	        if (!key.equals("")) {
	            errorCodeMap.put(key, Util.null2str(d.get(ERROR_CODE_FIELD)));
	        }
	    }

	    List<Map<String, Object>> zipErrorList = new ArrayList<Map<String, Object>>();
	    List<Map<String, Object>> customsErrorList = new ArrayList<Map<String, Object>>();
	    List<Map<String, Object>> kroSendList = new ArrayList<Map<String, Object>>();
	    List<Map<String, Object>> cmoSendList = new ArrayList<Map<String, Object>>();

	    for (Map<String, Object> dataMap : sendDataList) {
	    	String inputMethod = Util.null2str(dataMap.get("INPUT_METHOD"));

	        if ("KRO".equals(inputMethod)) {
	            kroSendList.add(dataMap);
	            continue;
	        }else if ("CMO".equals(inputMethod)) {
	            cmoSendList.add(dataMap);
	            continue;
	        }else {
	        	String key = Util.null2str(dataMap.get(KEY_FIELD));
	        	String errorCodeStr = errorCodeMap.containsKey(key) ? errorCodeMap.get(key) : Util.null2str(dataMap.get(ERROR_CODE_FIELD));
	        	
	        	boolean zipError = false;
	        	boolean customsError = false;
	        	if (!errorCodeStr.equals("")) {
	        		String[] codes = errorCodeStr.split(",");
	        		for (String code : codes) {
	        			String c = code.trim();
	        			if (c.equals("")) {
	        				continue;
	        			}
	        			if (c.equals(ZIP_CODE)) {
	        				zipError = true;
	        			} else {
	        				customsError = true;
	        			}
	        		}
	        	}
	        	
	        	if (zipError) {
	        		zipErrorList.add(dataMap);
	        	}
	        	if (customsError) {
	        		customsErrorList.add(dataMap);
	        	}
	        }
	    }

	    boolean allSuccess = true;
	    int stockCnt = 0;

	    if (!zipErrorList.isEmpty()) {
	        PcccSaveResult zipResult = sendKakaoTemplate(pcccBatch, TEMPLATE_TYPE_ZIP, zipErrorList, false, stockCnt);
	        if (!zipResult.getResult()) {
	            allSuccess = false;
	            returnMap.put("errStr",
	                    zipResult.getReturnMap() != null ? zipResult.getReturnMap().get("errStr") : "쿠팡 오류 알림톡 전송 실패");
	        }else {
	        	stockCnt += zipErrorList.size();
	        }
	    }

	    if (!customsErrorList.isEmpty()) {
	        PcccSaveResult generalResult = sendKakaoTemplate(pcccBatch, TEMPLATE_TYPE_GENERAL, customsErrorList, true, stockCnt);
	        if (!generalResult.getResult()) {
	            allSuccess = false;
	            returnMap.put("errStr",
	                    generalResult.getReturnMap() != null ? generalResult.getReturnMap().get("errStr") : "쿠팡 알림톡 전송 실패");
	        }else {
	        	stockCnt += customsErrorList.size();
	        }
	    }
	    
	    if (!kroSendList.isEmpty()) {
	        PcccSaveResult kroResult = sendKakaoTemplate(pcccBatch, "CUSTOMS_ID_VERIFY", kroSendList, true, stockCnt);
	        if (!kroResult.getResult()) {
	            allSuccess = false;
	            returnMap.put("errStr",
	            		kroResult.getReturnMap() != null ? kroResult.getReturnMap().get("errStr") : "크림 알림톡 전송 실패");
	        }else {
	        	stockCnt += kroSendList.size();
	        }
	    }
	    
	    if (!cmoSendList.isEmpty()) {
	        PcccSaveResult cmoResult = sendKakaoTemplate(pcccBatch, "CUSTOMS_ID_VERIFY", cmoSendList, true, stockCnt);
	        if (!cmoResult.getResult()) {
	            allSuccess = false;
	            returnMap.put("errStr",
	            		cmoResult.getReturnMap() != null ? cmoResult.getReturnMap().get("errStr") : "일반오류 알림톡 전송 실패");
	        }else {
	        	stockCnt += kroSendList.size();
	        }
	    }

	    if (zipErrorList.isEmpty() && customsErrorList.isEmpty() && kroSendList.isEmpty() && cmoSendList.isEmpty()) {
	        returnMap.put("errStr", "전송 대상 오류코드가 없습니다.");
	        allSuccess = false;
	    }

	    pcccSaveResult.setResult(allSuccess);
	    if (!allSuccess) {
	        pcccSaveResult.setReturnMap(returnMap);
	    }

	    return pcccSaveResult;
	}

	/**
	 * 알림톡 템플릿 설정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private PcccSaveResult sendKakaoTemplate(PcccBatch pcccBatch, String templateType, List<Map<String, Object>> sendDataList, boolean saveHistory, int stockCnt) throws Exception {

	    PcccSaveResult pcccSaveResult = new PcccSaveResult();
	    Map<String, Object> returnMap = new HashMap<String, Object>();
	    Map<String, Object> tmpMap = new HashMap<String, Object>();
	    List<Map<String, Object>> dataList = pcccBatch.getDataList();
	    String srchTemplateCode = "";
	    // 알림톡 설정정보 조회
	    tmpMap.put("srchEntCode", Util.null2str(pcccBatch.getEntCode()));
	    tmpMap.put("srchTemplateType", templateType);
	    if("COUPANG_CUSTOMS_ID".equals(templateType)) {
	    	srchTemplateCode = "CUS_01";
	    }else if("COUPANG_CUSTOMS_ZIP".equals(templateType)) {
	    	srchTemplateCode = "CUS_02";
	    }else {
	    	if("KRO".equals(Util.null2str(sendDataList.get(0).get("INPUT_METHOD")))) {
	    		srchTemplateCode = "KRO_01";
	    	}else if("CMO".equals(Util.null2str(sendDataList.get(0).get("INPUT_METHOD")))) {
	    		srchTemplateCode = "CMO_01";
	    	}
	    }
	    tmpMap.put("srchTemplateCode", srchTemplateCode);
	    Map<String, Object> kakaoInfo = (Map<String, Object>) pcccMapper.selectKakaoInfo(tmpMap);
	    if (kakaoInfo == null) {
	        returnMap.put("errStr", "카카오톡 설정정보가 없습니다.");
	        pcccSaveResult.setReturnMap(returnMap);
	        return pcccSaveResult;
	    }
	    kakaoInfo.putAll((Map<String, Object>) pcccMapper.selectKakaoInfoEnterprise(tmpMap));

	    if (kakaoInfo != null && kakaoInfo.size() > 0
	            && !Util.null2str(kakaoInfo.get("SMS_USER_ID")).equals("")
	            && !Util.null2str(kakaoInfo.get("SMS_CID_NUM")).equals("")
	            && !Util.null2str(kakaoInfo.get("SMS_SEND_TEL_NO")).equals("")
	            && !Util.null2str(kakaoInfo.get("TEMPLATE_CODE")).equals("")) {

	        String templateCode = Util.null2str(kakaoInfo.get("TEMPLATE_CODE"));
	        String sendMsg = null;
	        String resendMsg = null;
	        String[] returnMsg = null;
	        String KakaoNextType = Util.null2str(kakaoInfo.get("RESEND_MSG_TYPE"));
	        String butUrl1 = Util.null2str(kakaoInfo.get("BUTTON1_URL"));
	        String butUrl2 = Util.null2str(kakaoInfo.get("BUTTON2_URL"));
	        String butUrl3 = Util.null2str(kakaoInfo.get("BUTTON3_URL"));
	        String butUrl4 = Util.null2str(kakaoInfo.get("BUTTON4_URL"));
	        String butUrl5 = Util.null2str(kakaoInfo.get("BUTTON5_URL"));
	        String msgType = null;

	        String butStr = "";
	        String resendButStr = "";
	        sendMsg = Util.null2str(kakaoInfo.get("TEMPLATE"));
	        resendMsg = Util.null2str(kakaoInfo.get("RESEND_TEMPLATE"));
	        msgType = Util.null2str(kakaoInfo.get("MSG_TYPE"));
	        if (!butUrl1.equals("")) {
	            butStr += "<KakaoButName1>" + Util.null2str(kakaoInfo.get("BUTTON1_NAME")) + "</KakaoButName1><KakaoButType1>" + Util.null2str(kakaoInfo.get("BUTTON1_TYPE")) + "</KakaoButType1><KakaoButMobileUrl1><![CDATA[" + butUrl1 + "]]></KakaoButMobileUrl1><KakaoButPcUrl1><![CDATA[" + butUrl1 + "]]></KakaoButPcUrl1>";
	            resendButStr += "\n\n" + Util.null2str(kakaoInfo.get("BUTTON1_NAME")) + " : <![CDATA[" + butUrl1 + "]]>";
	        }
	        if (!butUrl2.equals("")) {
	            butStr += "<KakaoButName2>" + Util.null2str(kakaoInfo.get("BUTTON2_NAME")) + "</KakaoButName2><KakaoButType2>" + Util.null2str(kakaoInfo.get("BUTTON2_TYPE")) + "</KakaoButType2><KakaoButMobileUrl2><![CDATA[" + butUrl2 + "]]></KakaoButMobileUrl2><KakaoButPcUrl2><![CDATA[" + butUrl2 + "]]></KakaoButPcUrl2>";
	            resendButStr += "\n\n" + Util.null2str(kakaoInfo.get("BUTTON2_NAME")) + " : <![CDATA[" + butUrl2 + "]]>";
	        }
	        if (!butUrl3.equals("")) {
	            butStr += "<KakaoButName3>" + Util.null2str(kakaoInfo.get("BUTTON3_NAME")) + "</KakaoButName3><KakaoButType3>" + Util.null2str(kakaoInfo.get("BUTTON3_TYPE")) + "</KakaoButType3><KakaoButMobileUrl3><![CDATA[" + butUrl3 + "]]></KakaoButMobileUrl3><KakaoButPcUrl3><![CDATA[" + butUrl3 + "]]></KakaoButPcUrl3>";
	            resendButStr += "\n\n" + Util.null2str(kakaoInfo.get("BUTTON3_NAME")) + " : <![CDATA[" + butUrl3 + "]]>";
	        }
	        if (!butUrl4.equals("")) {
	            butStr += "<KakaoButName4>" + Util.null2str(kakaoInfo.get("BUTTON4_NAME")) + "</KakaoButName4><KakaoButType4>" + Util.null2str(kakaoInfo.get("BUTTON4_TYPE")) + "</KakaoButType4><KakaoButMobileUrl4><![CDATA[" + butUrl4 + "]]></KakaoButMobileUrl4><KakaoButPcUrl4><![CDATA[" + butUrl4 + "]]></KakaoButPcUrl4>";
	            resendButStr += "\n\n" + Util.null2str(kakaoInfo.get("BUTTON4_NAME")) + " : <![CDATA[" + butUrl4 + "]]>";
	        }
	        if (!butUrl5.equals("")) {
	            butStr += "<KakaoButName5>" + Util.null2str(kakaoInfo.get("BUTTON5_NAME")) + "</KakaoButName5><KakaoButType5>" + Util.null2str(kakaoInfo.get("BUTTON5_TYPE")) + "</KakaoButType5><KakaoButMobileUrl5><![CDATA[" + butUrl5 + "]]></KakaoButMobileUrl5><KakaoButPcUrl5><![CDATA[" + butUrl5 + "]]></KakaoButPcUrl5>";
	            resendButStr += "\n\n" + Util.null2str(kakaoInfo.get("BUTTON5_NAME")) + " : <![CDATA[" + butUrl5 + "]]>";
	        }
	        StringBuffer sendData = new StringBuffer();
	        int cnt = 1;

	        tmpMap.clear();
	        tmpMap.put("gmtTime", pcccBatch.getGmtTime());
	        tmpMap.put("entCode", pcccBatch.getEntCode());
	        tmpMap.put("entBusinessCode", "");
	        tmpMap.put("updateUserId", pcccBatch.getUpdateUserId());
	        tmpMap.put("solution", "ECM");
	        tmpMap.put("stockType", "SMS");
	        tmpMap.put("stockSign", "SMS");
	        tmpMap.put("stockSeqLength", 8);
	        tmpMap.put("dateLength", null);
	        tmpMap.put("signType", null);
	        tmpMap.put("stockDateFormat", null);
	        tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
	        tmpMap.put("stockRemark", "ALARM-JOB NO");

	        sendData.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?><root><AccessIdentifier><UserID>" + Util.null2str(kakaoInfo.get("SMS_USER_ID")) + "</UserID><CidNum>" + Util.null2str(kakaoInfo.get("SMS_CID_NUM")) + "</CidNum></AccessIdentifier>");
	        for (int i = 0; i < sendDataList.size(); i++) {
	            Map<String, Object> dataMap = sendDataList.get(i);
	            tmpMap.put("seqNo", stockCnt + i);
	            dataMap.put("seqNoId", commonMapper.selectNextStockMaxNo(tmpMap));

	            sendData.append("<SendData><Seq>" + cnt++ + "</Seq><MsgType>" + msgType + "</MsgType><SendType>1</SendType><DstAddr>" + Util.null2str(dataMap.get("CONSIGNEE_HP")) + "</DstAddr><CallBack>" + Util.null2str(kakaoInfo.get("SMS_SEND_TEL_NO")) + "</CallBack>");
	            returnMsg = kakaoMsgReplace(templateType, dataMap, kakaoInfo, sendMsg, resendMsg, butStr, resendButStr, dataList, i, srchTemplateCode);

	            if (returnMsg[0].equals("orderNull")) {
	                returnMap.put("errStr", "해당 하우스가 존재하지 않습니다.");
	                pcccSaveResult.setReturnMap(returnMap);
	                return pcccSaveResult;
	            }
	            sendData.append("<Subject>" + Util.null2str(kakaoInfo.get("SUBJECT")) + "</Subject><TextType>0</TextType><Message>" + returnMsg[0] + "</Message><Message2>" + returnMsg[1] + "</Message2>");
	            sendData.append("<KakaoTemplate>" + templateCode + "</KakaoTemplate><KakaoNextType>" + KakaoNextType + "</KakaoNextType><KakaoAdFlag>N</KakaoAdFlag>");
	            if (!returnMsg[2].equals("")) {
	                sendData.append(returnMsg[2]);
	            }
	            sendData.append("<FileCnt>0</FileCnt></SendData>");
	        }
	        sendData.append("</root>");
	        URL url = new URL("http://sms.mcube.ne.kr/if/sms/registSmsMsgInfo.do");
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();
	        con.setConnectTimeout(5000);
	        con.setReadTimeout(5000);
	        con.setRequestMethod("POST");
	        con.setRequestProperty("Content-Type", "text/plain");
	        con.setDoInput(true);
	        con.setDoOutput(true);
	        con.setUseCaches(false);
	        con.setDefaultUseCaches(false);

	        String AESPWD = "tO/SdUc9hkSKc9rt+drUGA==";
	        String rtn = EncryptionUtils.encryptAES(sendData.toString(), AESPWD, "");

	        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
	        wr.write(rtn);
	        wr.flush();

	        int responseCode = con.getResponseCode();
	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            StringBuilder sb = new StringBuilder();
	            String line = "";
	            while ((line = br.readLine()) != null) {
	                sb.append(line);
	            }
	            String responseStr = EncryptionUtils.decryptAES(sb.toString(), AESPWD, "");

	            int seq = 1;
	            String msgKey = "";
	            String returnCodeDetail = "";
	            String returnMessageDetail = "";

	            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	            Document doc = dBuilder.parse(new InputSource(new StringReader(responseStr)));
	            doc.getDocumentElement().normalize();

	            NodeList returnCode = doc.getElementsByTagName("Return");
	            if (returnCode.item(0).getTextContent().equals("F")) {
	                NodeList returnMessage = doc.getElementsByTagName("ReturnMessage");
	                returnMap.put("errStr", returnMessage.item(0).getTextContent());
	            } else {
	                NodeList messageList = doc.getElementsByTagName("Message");
	                if (messageList.getLength() > 0) {
	                    for (int i = 0; i < messageList.getLength(); i++) {
	                        Node messageNode = messageList.item(i);
	                        NodeList ChildList = messageNode.getChildNodes();
	                        for (int j = 0; j < ChildList.getLength(); j++) {
	                            Node messageChild = ChildList.item(j);
	                            if (messageChild.getNodeName().equals("Seq")) {
	                                seq = Util.null2int(messageChild.getTextContent());
	                            } else if (messageChild.getNodeName().equals("ReturnCodeDetail")) {
	                                returnCodeDetail = messageChild.getTextContent();
	                            } else if (messageChild.getNodeName().equals("ReturnMessageDetail")) {
	                                returnMessageDetail = messageChild.getTextContent();
	                            } else if (messageChild.getNodeName().equals("MsgKey")) {
	                                msgKey = messageChild.getTextContent();
	                            }
	                        }
	                        Map<String, Object> dataMap = sendDataList.get(seq - 1);
	                        dataMap.put("msgKey", msgKey);
	                        if (returnCodeDetail.equals("T")) {
	                            dataMap.put("returnMessageDetail", "");
	                        } else {
	                            dataMap.put("returnMessageDetail", returnMessageDetail);
	                        }
	                    }
	                    if(saveHistory) {
	                    	SqlSession sqlSession = null;
	                    	try {
	                    		sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
	                    		PcccMapper pcccMapper = sqlSession.getMapper(PcccMapper.class);
	                    		for (Map<String, Object> dataMap : sendDataList) {
	                    			dataMap.put("solution", "ECM");
	                    			dataMap.put("entCode", dataMap.get("ENT_CODE"));
	                    			dataMap.put("customsIdJobNo", dataMap.get("CUSTOMS_ID_JOB_NO"));
	                    			dataMap.put("entBusinessCode", "");
	                    			dataMap.put("templateType", templateType);
	                    			dataMap.put("templateCode", templateCode);
	                    			dataMap.put("attnKey", Util.null2str(dataMap.get("CONSIGNEE_NAME")));
	                    			dataMap.put("dstAddrKey", Util.null2str(dataMap.get("CONSIGNEE_HP")));
	                    			dataMap.put("gmtTime", pcccBatch.getGmtTime());
	                    			dataMap.put("updateUserId", pcccBatch.getUpdateUserId());
	                    			Util.accessControlDatabase(dataMap, "customsIdJobNo|gmtTime|attnKey|dstAddrKey", false, null, null, null, null);
	                    			pcccMapper.insertKakaoSendResultBatch(dataMap);
	                    		}
	                    		
	                    		sqlSession.commit();
	                    		sqlSession.clearCache();
	                    	} finally {
	                    		sqlSession.close();
	                    	}
	                    }
	                }
	            }
	        } else {
	            returnMap.put("errStr", "송신 에러코드 : " + responseCode);
	        }
	    } else {
	        returnMap.put("errStr", "카카오톡 설정정보가 없습니다.");
	    }

	    if (Util.null2str(returnMap.get("errStr")).equals("")) {
	        pcccSaveResult.setResult(true);
	    } else {
	        pcccSaveResult.setResult(false);
	        pcccSaveResult.setReturnMap(returnMap);
	    }

	    return pcccSaveResult;
	}
	
	/**
	 * 알림톡 replace
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private String[] kakaoMsgReplace(String templateType, Map<String, Object> dataMap, Map kakaoInfo, String sendMsg, String resendMsg, String butStr, String resendButStr, List<Map<String, Object>> dataList, int listIdx, String templateCode) throws Exception {
			
		if(templateType.equals("COUPANG_CUSTOMS_ID")){
			try{
				dataMap.putAll(pcccMapper.selectSendSmsCustomsIdVerify(dataMap));
			}catch(NullPointerException e){
				Util.getPrintStacTraceString(e);
				String[] returnStr = new String[4];
				returnStr[0] = "orderNull";
				returnStr[1] = "";
				returnStr[2] = "";
				return returnStr;
			}
			byte[] rawKey = EncryptionUtils.getRawKey(Util.null2str(dataMap.get("ENT_CODE")));
			String entCode = Util.null2str(kakaoInfo.get("ENT_CODE"));
			String odJobNo = Util.null2str(dataMap.get("CUSTOMS_ID_JOB_NO"));
			String hblNo = Util.null2str(dataMap.get("HBL_NO"));
			String seqNoId = Util.null2str(dataMap.get("seqNoId"));
			String customsIdJobNo = Util.null2str(dataMap.get("CUSTOMS_ID_JOB_NO"));
			String customsSeqNoId = Util.null2str(dataMap.get("CUSTOMS_SEQ_NO_ID"));
			String inputMethod = Util.null2str(dataMap.get("INPUT_METHOD"));
			
			if(!odJobNo.equals("")){
				odJobNo = EncryptionUtils.encryptAES2(odJobNo, rawKey);
			}
			if(!hblNo.equals("")){
				hblNo = EncryptionUtils.encryptAES2(hblNo, rawKey);
			}
			if(!seqNoId.equals("")){
				seqNoId = EncryptionUtils.encryptAES2(seqNoId, rawKey);
			}
			if(!customsSeqNoId.equals("")){
				customsSeqNoId = EncryptionUtils.encryptAES2(customsSeqNoId, rawKey);
			}
			if(!customsIdJobNo.equals("")){
				customsIdJobNo = EncryptionUtils.encryptAES2(customsIdJobNo, rawKey);
			}
			
			if(!inputMethod.equals("")){
				inputMethod = EncryptionUtils.encryptAES2(inputMethod, rawKey);
			}
			
			sendMsg = sendMsg.replace("#{송하인명}", Util.getCdata(Util.null2str(dataMap.get("SHIPPER_NAME"))))
					.replace("#{수하인명}", Util.null2str(dataMap.get("CONSIGNEE_NAME")))
					.replace("#{오더번호}", Util.null2str(dataMap.get("PO_NO")))
					.replace("#{품명}", Util.null2str(dataMap.get("COMMODITY_ITEM")));
			resendMsg = resendMsg.replace("#{송하인명}", Util.getCdata(Util.null2str(dataMap.get("SHIPPER_NAME"))))
					.replace("#{수하인명}", Util.null2str(dataMap.get("CONSIGNEE_NAME")))
					.replace("#{오더번호}", Util.null2str(dataMap.get("PO_NO")))
					.replace("#{품명}", Util.null2str(dataMap.get("COMMODITY_ITEM")));
			dataMap.put("beforeValue", dataMap.get("CUSTOMS_ID") + "|" + dataMap.get("CONSIGNEE_NAME") + "|" + dataMap.get("CONSIGNEE_HP") );
			
			butStr = butStr.replace("#{parameters}", "p1=" + entCode + "&p2=" + odJobNo + "&p3=" + hblNo + "&p4=" + seqNoId + "&p5=" + customsSeqNoId + "&p6=" + customsIdJobNo + customsIdJobNo + "&p7=" + inputMethod)
						   .replace("#{PARAMETERS}", "p1=" + entCode + "&p2=" + odJobNo + "&p3=" + hblNo + "&p4=" + seqNoId + "&p5=" + customsSeqNoId + "&p6=" + customsIdJobNo + customsIdJobNo + "&p7=" + inputMethod);
			resendButStr = resendButStr.replace("#{parameters}", "p1=" + entCode + "&p2=" + odJobNo + "&p3=" + hblNo + "&p4=" + seqNoId + "&p5=" + customsSeqNoId + "&p6=" + customsIdJobNo + "&p7=" + inputMethod)
									   .replace("#{PARAMETERS}", "p1=" + entCode + "&p2=" + odJobNo + "&p3=" + hblNo + "&p4=" + seqNoId + "&p5=" + customsSeqNoId + "&p6=" + customsIdJobNo + "&p7=" + inputMethod);
		
		}else if(templateType.equals("COUPANG_CUSTOMS_ZIP")) {
			try{
				dataMap.putAll(pcccMapper.selectSendSmsCustomsIdVerify(dataMap));
			}catch(NullPointerException e){
				Util.getPrintStacTraceString(e);
				String[] returnStr = new String[4];
				returnStr[0] = "orderNull";
				returnStr[1] = "";
				returnStr[2] = "";
				return returnStr;
			}
			byte[] rawKey = EncryptionUtils.getRawKey(Util.null2str(dataMap.get("entCode")));
			String entCode = Util.null2str(kakaoInfo.get("ENT_CODE"));
			String odJobNo = Util.null2str(dataMap.get("OD_JOB_NO"));
			String customsIdJobNo = Util.null2str(dataMap.get("CUSTOMS_ID_JOB_NO"));
			String customsSeqNoId = Util.null2str(dataMap.get("CUSTOMS_SEQ_NO_ID"));
			String hblNo = Util.null2str(dataMap.get("HBL_NO"));
			String seqNoId = Util.null2str(dataMap.get("seqNoId"));

			//수하인 주소 구까지만 표현
			String convertAddr = Util.null2str(dataMap.get("CONSIGNEE_ADDRESS"));
			Pattern pattern = Pattern.compile("^((?:\\S+[시도])\\s+(?:(?:\\S+시\\s+)?\\S+(?:구|군)|\\S+시))");
			Matcher matcher = pattern.matcher(convertAddr);
			if (matcher.find()) {
				convertAddr = matcher.group(1);
			}
			
			if(!odJobNo.equals("")){
				odJobNo = EncryptionUtils.encryptAES2(odJobNo, rawKey);
			}
			if(!hblNo.equals("")){
				hblNo = EncryptionUtils.encryptAES2(hblNo, rawKey);
			}
			if(!seqNoId.equals("")){
				seqNoId = EncryptionUtils.encryptAES2(seqNoId, rawKey);
			}
			
			if(!customsSeqNoId.equals("")){
				customsSeqNoId = EncryptionUtils.encryptAES2(customsSeqNoId, rawKey);
			}
			
			if(!customsIdJobNo.equals("")){
				customsIdJobNo = EncryptionUtils.encryptAES2(customsIdJobNo, rawKey);
			}
			
			sendMsg = sendMsg.replace("#{송하인명}", Util.getCdata(Util.null2str(dataMap.get("SHIPPER_NAME"))))
					.replace("#{수하인명}", Util.null2str(dataMap.get("CONSIGNEE_NAME")))
					.replace("#{오더번호}", Util.null2str(dataMap.get("PO_NO")))
					.replace("#{품명}", Util.null2str(dataMap.get("COMMODITY_ITEM")))
					.replace("#{수하인주소1}", convertAddr);
			resendMsg = resendMsg.replace("#{송하인명}", Util.getCdata(Util.null2str(dataMap.get("SHIPPER_NAME"))))
					.replace("#{수하인명}", Util.null2str(dataMap.get("CONSIGNEE_NAME")))
					.replace("#{오더번호}", Util.null2str(dataMap.get("PO_NO")))
					.replace("#{품명}", Util.null2str(dataMap.get("COMMODITY_ITEM")))
					.replace("#{수하인주소1}", convertAddr);
			dataMap.put("beforeValue", dataMap.get("CUSTOMS_ID") + "|" + dataMap.get("CONSIGNEE_NAME") + "|" + dataMap.get("CONSIGNEE_HP") );
			
			butStr = butStr.replace("#{parameters}", "p1=" + entCode)
						   .replace("#{PARAMETERS}", "p1=" + entCode);
			resendButStr = resendButStr.replace("#{parameters}", "p1=" + entCode)
									   .replace("#{PARAMETERS}", "p1=" + entCode);
			
		}else if(templateType.equals("CUSTOMS_ID_VERIFY")) {
			try{
				dataMap.putAll(pcccMapper.selectSendSmsCustomsIdVerify(dataMap));
			}catch(NullPointerException e){
				Util.getPrintStacTraceString(e);
				String[] returnStr = new String[4];
				returnStr[0] = "orderNull";
				returnStr[1] = "";
				returnStr[2] = "";
				return returnStr;
			}
			byte[] rawKey = EncryptionUtils.getRawKey(Util.null2str(dataMap.get("entCode")));
			String entCode = Util.null2str(kakaoInfo.get("ENT_CODE"));
			String odJobNo = Util.null2str(dataMap.get("OD_JOB_NO"));
			String customsIdJobNo = Util.null2str(dataMap.get("CUSTOMS_ID_JOB_NO"));
			String customsSeqNoId = Util.null2str(dataMap.get("CUSTOMS_SEQ_NO_ID"));
			String hblNo = Util.null2str(dataMap.get("HBL_NO"));
			String seqNoId = Util.null2str(dataMap.get("seqNoId"));

			//수하인 주소 구까지만 표현
			String convertAddr = Util.null2str(dataMap.get("CONSIGNEE_ADDRESS"));
			Pattern pattern = Pattern.compile("^((?:\\S+[시도])\\s+(?:(?:\\S+시\\s+)?\\S+(?:구|군)|\\S+시))");
			Matcher matcher = pattern.matcher(convertAddr);
			if (matcher.find()) {
				convertAddr = matcher.group(1);
			}
			
			if(!odJobNo.equals("")){
				odJobNo = EncryptionUtils.encryptAES2(odJobNo, rawKey);
			}
			if(!hblNo.equals("")){
				hblNo = EncryptionUtils.encryptAES2(hblNo, rawKey);
			}
			if(!seqNoId.equals("")){
				seqNoId = EncryptionUtils.encryptAES2(seqNoId, rawKey);
			}
			
			if(!customsSeqNoId.equals("")){
				customsSeqNoId = EncryptionUtils.encryptAES2(customsSeqNoId, rawKey);
			}
			
			if(!customsIdJobNo.equals("")){
				customsIdJobNo = EncryptionUtils.encryptAES2(customsIdJobNo, rawKey);
			}
			
			
			if("KRO_01".equals(templateCode)) {
				sendMsg = sendMsg.replace("#{송하인명}", Util.getCdata(Util.null2str(dataMap.get("SHIPPER_NAME"))))
						.replace("#{수하인명}", Util.null2str(dataMap.get("CONSIGNEE_NAME")))
						.replace("#{오더번호}", Util.null2str(dataMap.get("PO_NO")))
						.replace("#{품명}", Util.null2str(dataMap.get("COMMODITY_ITEM")))
						.replace("#{수하인주소1}", convertAddr);
				resendMsg = resendMsg.replace("#{송하인명}", Util.getCdata(Util.null2str(dataMap.get("SHIPPER_NAME"))))
						.replace("#{수하인명}", Util.null2str(dataMap.get("CONSIGNEE_NAME")))
						.replace("#{오더번호}", Util.null2str(dataMap.get("PO_NO")))
						.replace("#{품명}", Util.null2str(dataMap.get("COMMODITY_ITEM")))
						.replace("#{수하인주소1}", convertAddr);
				dataMap.put("beforeValue", dataMap.get("CUSTOMS_ID") + "|" + dataMap.get("CONSIGNEE_NAME") + "|" + dataMap.get("CONSIGNEE_HP") );
				
				butStr = butStr.replace("#{parameters}", "p1=" + entCode)
						.replace("#{PARAMETERS}", "p1=" + entCode);
				resendButStr = resendButStr.replace("#{parameters}", "p1=" + entCode)
						.replace("#{PARAMETERS}", "p1=" + entCode);
			}else if("CMO_01".equals(templateCode)) {
				sendMsg = sendMsg.replace("#{송하인명}", Util.getCdata(Util.null2str(dataMap.get("SHIPPER_NAME"))))
						.replace("#{수하인명}", Util.null2str(dataMap.get("CONSIGNEE_NAME")))
						.replace("#{오더번호}", Util.null2str(dataMap.get("PO_NO")))
						.replace("#{품명}", Util.null2str(dataMap.get("COMMODITY_ITEM")))
						.replace("#{수하인주소1}", convertAddr);
				resendMsg = resendMsg.replace("#{송하인명}", Util.getCdata(Util.null2str(dataMap.get("SHIPPER_NAME"))))
						.replace("#{수하인명}", Util.null2str(dataMap.get("CONSIGNEE_NAME")))
						.replace("#{오더번호}", Util.null2str(dataMap.get("PO_NO")))
						.replace("#{품명}", Util.null2str(dataMap.get("COMMODITY_ITEM")))
						.replace("#{수하인주소1}", convertAddr);
				dataMap.put("beforeValue", dataMap.get("CUSTOMS_ID") + "|" + dataMap.get("CONSIGNEE_NAME") + "|" + dataMap.get("CONSIGNEE_HP") );
				
				butStr = butStr.replace("#{parameters}", "p1=" + entCode)
						.replace("#{PARAMETERS}", "p1=" + entCode);
				resendButStr = resendButStr.replace("#{parameters}", "p1=" + entCode)
						.replace("#{PARAMETERS}", "p1=" + entCode);
			}
			
		}
		resendMsg += resendButStr;
		
		String[] returnStr = new String[4];
		returnStr[0] = sendMsg;
		returnStr[1] = resendMsg;
		returnStr[2] = butStr;
		return returnStr;
	}
	
public void customsCodeCorrectedSend(Map<String, Object> sendMap) throws Exception {
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			// ES Index
			String index = "ecm-interface-logs-";
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			Map<String, Object> sendCustomsInfo = new HashMap<String, Object>();
			DocumentApi documentApi = new DocumentApi(client);
			IndexApi indexApi = new IndexApi(client);
			String logId = "";
			
			Map<String, Object> pcccCustomsInfo = pcccMapper.selectSendCustomsInfo(sendMap);
			Map<String, Object> returnMap = null;
			if(pcccCustomsInfo != null) {
				sendCustomsInfo.put("requestId", Util.null2str(pcccCustomsInfo.get("REQUEST_ID")));
				sendCustomsInfo.put("coupangOrderId", Util.null2str(pcccCustomsInfo.get("PO_NO")));
				sendCustomsInfo.put("pccc", Util.null2str(pcccCustomsInfo.get("CUSTOMS_ID")));
				sendCustomsInfo.put("receiverPhoneNumber", Util.null2str(pcccCustomsInfo.get("CONSIGNEE_HP")));
				String pcccDateTime = Util.null2str(pcccCustomsInfo.get("PCCC_DATE_TIME"));
				String correctedAt = "";
				if(!"".equals(pcccDateTime)) {
					SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
 					Date formatDate = dtFormat.parse(pcccDateTime);
					correctedAt = newDtFormat.format(formatDate);
				}
				sendCustomsInfo.put("correctedAt", correctedAt);
				
				// JSON 문자열 생성
				String jsonBody = mapper.writeValueAsString(sendCustomsInfo);
				log.info("jsonBody : {}", jsonBody);
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("entCode", sendMap.get("entCode"));
				paramMap.put("deliveryCode", "COUPANG-PCCC");
				Map<String, Object> apiMap = pcccMapper.selectDeliveryApiInfo(paramMap);
				
				if(apiMap != null) {
					String sendUrl = Util.null2str(apiMap.get("RECV_API_URL"));
					String sendPath = Util.null2str(apiMap.get("TRACKING_API_URL"));
					String accessKey = Util.null2str(apiMap.get("RECV_API_ID"));
					String secretKey = Util.null2str(apiMap.get("RECV_API_PW"));
					String signature = HmacGenerator.generate("POST", sendPath, accessKey, secretKey);
					
					URL url = new URL(sendUrl+sendPath);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					// API HEADER
					conn.setRequestProperty("Authorization", signature);
					conn.setRequestProperty("Content-Type", "application/json");
					conn.setRequestMethod("POST");
					conn.setReadTimeout(18000);
					conn.setDoOutput(true);
					conn.setDoInput(true);
					conn.getOutputStream().write(jsonBody.getBytes("UTF-8")); // POST 호출
					conn.connect();
					log.info("sendPath??"+sendPath);
					log.info("[" + Util.getNow("yyyy-MM-dd HH:mm:ss") + "][CustomsCodeCorrected] signature : " + signature);
					
					BufferedReader in = null;
					
					int responseCode = conn.getResponseCode();
					log.info("coupang_api::res=>"+responseCode);
					if (responseCode == 200) in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));	// HTTP OK
			        else in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));						// HTTP ERROR

			        String inputLine;
			        StringBuilder response = new StringBuilder();

			        while ((inputLine = in.readLine()) != null) {
			            response.append(inputLine);
			        }
			        in.close();
			        
			        log.info("response : {}", response);
			        ObjectMapper jsonToMap = new ObjectMapper(); // json To Map 변환용
			        returnMap = jsonToMap.readValue(Util.null2str(response), Map.class);
			        
			        if (responseCode == 200) {
			        	pcccMapper.updateSendCustomsInfo(sendCustomsInfo);
			        	// history 등록 
			        	tmpMap = new HashMap<String, Object>();
			        	logId = Util.null2str(pcccCustomsInfo.get("REQUEST_ID"));
			        	index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			        	String if_status = Util.null2str(returnMap.get("resultCode"), Util.null2str(returnMap.get("code")));
			        	String if_remark = Util.null2str(returnMap.get("resultMessage"), Util.null2str(returnMap.get("message")));
			        	
			        	// 처리결과에 따른 로그 기록
			        	tmpMap.clear();
			        	tmpMap.put("ent_code", Util.null2str(sendMap.get("entCode"))); 		 	// 도입사코드
			        	tmpMap.put("customer_code", "COUPANG"); 		 			// 고객코드
			        	tmpMap.put("solution", "ECM");      		 				// SOLUTION
			        	tmpMap.put("bound", "");		 	 						// 바운드 (AIR)
			        	tmpMap.put("bound_type", "");	 							// 바운드타입
			        	tmpMap.put("hbl_no", sendCustomsInfo.get("HBL_NO"));			// HBL_NO
			        	tmpMap.put("if_service_code", "");
			        	tmpMap.put("if_doc_code", "COUPANG_PCCC_SEND");
			        	tmpMap.put("if_doc_name", "쿠팡 PCCC 전송");
			        	tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
			        	tmpMap.put("if_vender", "COUPANG");							// 중개사업자
			        	tmpMap.put("if_send_recv_type", "S");						// 송수신타입(S:SEND, R:RECV)
			        	tmpMap.put("if_send_recv_method", "JSON To JSON");			// 송수신방법
			        	tmpMap.put("if_file_path", "");			 					// 파일경로
			        	tmpMap.put("if_file_name", "");			 					// 파일명
			        	tmpMap.put("if_status", if_status);			 				// 처리상태
			        	tmpMap.put("if_status_info", "");		 					// 송수신상태_설명
			        	tmpMap.put("if_sender_id", "COUPANG");						// 송신식별자
			        	tmpMap.put("if_sender_name", "");		 					// 송신자명
			        	tmpMap.put("if_receiver_id", "");		 					// 수신식별자
			        	tmpMap.put("if_receiver_name", "");	     					// 수신자명
			        	tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
			        	tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
			        	tmpMap.put("if_remark", if_remark);			 				// 비고( 에러로그 등)
			        	tmpMap.put("register_user_id", "MANUAL");
			        	tmpMap.put("register_user_name", "MANUAL");
			        	tmpMap.put("register_date_time", System.currentTimeMillis());
			        	tmpMap.put("es_job_no", logId);
			        	
			        	// 전송 이력 1건 (file_data 배열에 누적)
			        	List<Map<String, Object>> jobLogList = new ArrayList<Map<String, Object>>();
			        	Map<String, Object> jobLogMap = new HashMap<String, Object>();
			        	jobLogMap.put("send_date", DateTime.getDate("yyyy-MM-dd HH:mm:ss"));
			        	jobLogMap.put("send_data", sendCustomsInfo);
			        	jobLogMap.put("if_status", if_status);
			        	jobLogMap.put("if_remark", if_remark);
			        	
			        	indexApi = new IndexApi(client);
			        	if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			        	documentApi = new DocumentApi(client);
			        	
			        	GetResponse esResponse = documentApi.getDocument(index, logId);
			        	if(esResponse == null) {
			        		jobLogList.add(jobLogMap);
			        		tmpMap.put("file_data", new Gson().toJson(jobLogList));
			        		documentApi.createDocument(index, logId, tmpMap);
			        	}else {
			        		tmpMap.clear();
			        		
			        		ObjectMapper esMapper = new ObjectMapper();
			        		esMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
			        		esMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			        		esMapper.setSerializationInclusion(Include.NON_NULL);
			        		
			        		InterfaceLogs interfaceLog = esMapper.convertValue(esResponse.getSourceAsMap(), InterfaceLogs.class);
			        		String fileData = interfaceLog.getFileData();
			        		jobLogList = esMapper.readValue(fileData, new TypeReference<List<Map<String, Object>>>() {});
			        		jobLogList.add(jobLogMap);
			        		
			        		tmpMap.put("file_data", new Gson().toJson(jobLogList));
			        		tmpMap.put("update_user_id", "MANUAL");
			        		tmpMap.put("update_user_name", "MANUAL");
			        		tmpMap.put("update_date_time", System.currentTimeMillis());
			        		
			        		documentApi.updateDocument(index, logId, tmpMap);
			        	}
			        }
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
	}
}