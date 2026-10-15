package org.yht.exps.express.service.edi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.edi.SeparateMngMapper;
import org.yht.exps.express.model.edi.separateMng.SeparateMng;
import org.yht.exps.express.model.edi.separateMng.SeparateMngDoc;
import org.yht.exps.express.model.edi.separateMng.SeparateMngList;
import org.yht.exps.express.model.edi.separateMng.SeparateMngSaveResult;
import org.yht.exps.express.model.edi.separateMng.search.SearchSeparateMngList;
import org.yht.util.Util;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class SeparateMngService {
		
	@Autowired
	SeparateMngMapper separateMngMapper;
	@Autowired
	CommonMapper commonMapper;
	
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
		
	/**
	 * ECM 별도관리물품해제신고 제출번호 그룹 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public SeparateMngList selectSeparateDocList(SearchSeparateMngList search) throws Exception {
		SeparateMngList separateMngList = new SeparateMngList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, "srchDateGbn|srchDateFormat", true, null, "", null, "");
		List<SeparateMng> list  = separateMngMapper.selectSeparateDocList(search);
		separateMngList.setSeparateMngDocList(list);
		if(list != null && list.size() > 0) {
			separateMngList.setTotal(list.size());
		}else {
			separateMngList.setTotal(0);
		}
		
		return separateMngList;
	}
	
	/**
	 * ECM 별도관리물품해제신고 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public SeparateMngList selectSeparateList(SearchSeparateMngList search) throws Exception {
		SeparateMngList separateMngList = new SeparateMngList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, "srchNoType|srchDateGbn|srchDateFormat", true, null, "", null, "");
		List<SeparateMng> list  = separateMngMapper.selectSeparateList(search);
		separateMngList.setSeparateMngList(list);
		if(list != null && list.size() > 0) {
			separateMngList.setTotal(list.size());
		}else {
			separateMngList.setTotal(0);
		}
		
		return separateMngList;
	}
		
	/**
	 * 별도관리물품해제신고 생성
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public SeparateMngSaveResult insertSeparateMng(SeparateMngDoc separateMngDoc) throws Exception {
		SeparateMngSaveResult separateMngSaveResult = new SeparateMngSaveResult();
		Util.accessControlDatabase(separateMngDoc, "gmtTime", false, null, null, null, null);
		
		Map<String, Object> map = new HashMap<String, Object>();
		String hblNoStr = "";
		String hblNoStr2 = "";
		String forwarderSign = "";
		List<SeparateMng> separateMngList = separateMngDoc.getSeparateMngList();
		List<Map<String, Object>> checkList = null;
		if(separateMngList == null) {
			separateMngSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
		}else{
			map.put("separateMngList", separateMngDoc.getSeparateMngList());
			map.put("entCode", separateMngDoc.getEntCode());
			map.put("entBusinessCode", separateMngDoc.getEntBusinessCode());
			checkList = separateMngMapper.selectSpecialData(map);
			if(checkList == null || checkList.size() == 0) {
				separateMngSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
			}else {
				for(Map<String, Object> checkData : checkList){
					if(!"".equals(Util.null2str(checkData.get("SPECIAL_ITEM_DOC_NO")))){
						separateMngSaveResult.setResultCode("A00015");	/** 해당건을 수정, 삭제 할 수 없습니다. */
						hblNoStr += ", " + Util.null2str(checkData.get("HBL_NO"));
					}else if("".equals(Util.null2str(checkData.get("EDI_PORTAL_MAILBOX_ID")))){
						separateMngSaveResult.setResultCode("A11348");	/** 관세청사서함정보가 없습니다. */
						hblNoStr2 += ", " + Util.null2str(checkData.get("HBL_NO"));
					}
				}
			}
		}
		
		if(!"".equals(Util.null2str(separateMngSaveResult.getResultCode()))){
			if("A00015".equals(separateMngSaveResult.getResultCode())){
				map.clear();
				map.put("errorHblNo", hblNoStr.substring(2));
				separateMngSaveResult.setResultData(map);
			}else if("A11348".equals(separateMngSaveResult.getResultCode())){
				map.clear();
				map.put("errorHblNo", hblNoStr2.substring(2));
				separateMngSaveResult.setResultData(map);
			}
			return separateMngSaveResult;
		}else{
			map.clear();
			map.put("entCode", separateMngDoc.getEntCode());
			map.put("entBusinessCode", separateMngDoc.getEntBusinessCode());
			map = ediCommonService.getFwSendAccInfo(map);
			if (map == null || "".equals(Util.null2str(map.get("B2G_FORWARDER_SIGN")))) {
				separateMngSaveResult.setResultCode("A11296");	/** 신청인부호를 4자리로 입력하십시오. */
				return separateMngSaveResult;
			}else{
				forwarderSign = Util.null2str(map.get("B2G_FORWARDER_SIGN"));
			}
		}
		
		
		//마스터별 리스트 재조합 Key : Master_odJobNo 
		Map<String,List<Map<String, Object>>> masMap = new HashMap<String,List<Map<String, Object>>>();
		for(int i = 0; i < checkList.size(); i++) {
			Map<String, Object> data = checkList.get(i);
			String masterOdJobNo = Util.null2str(data.get("MASTER_OD_JOB_NO"));
			List<Map<String, Object>> hList = masMap.get(masterOdJobNo);
			if(hList == null) {
				hList = new ArrayList<Map<String, Object>>();
			}
			
			hList.add(data);
			
			//마스터 키값으로 리스트 셋팅
			masMap.put(masterOdJobNo, hList);
		}
		
		//MAP의 KEY값을 이용하여 VALUE값 가져오기
		//마스터별로 제출번호 생성해야 함.
	    for (String masterOdJobNo : masMap.keySet()){
	    	
	    	List<Map<String, Object>> hList = masMap.get(masterOdJobNo);
	    	Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("gmtTime", separateMngDoc.getGmtTime());
			tmpMap.put("entCode", separateMngDoc.getEntCode());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("registerUserId", separateMngDoc.getRegisterUserId());
			tmpMap.put("updateUserId", separateMngDoc.getRegisterUserId());
			tmpMap.put("solution", "ECM");
			tmpMap.put("stockType", "EDI");
			tmpMap.put("stockSign", forwarderSign);
			tmpMap.put("stockSeqLength", "8");
			tmpMap.put("signType", "dateFirst");
			tmpMap.put("dateLength", "2");
			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", "EDI_CODE (GOVCBRDAX)");
			String ediDocNo = commonMapper.selectNextStockMaxNo(tmpMap);	/** 년도(2)+특송업체부호(4)+일련번호(8) */
			String ediJobNo = "";
			
			Map<String, Object> data = hList.get(0);
			tmpMap = new HashMap<String, Object>();
			/** 관리번호 */
			tmpMap.put("gmtTime", separateMngDoc.getGmtTime());
			tmpMap.put("entCode", separateMngDoc.getEntCode());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("registerUserId", separateMngDoc.getRegisterUserId());
			tmpMap.put("updateUserId", separateMngDoc.getRegisterUserId());
			tmpMap.put("solution", "ECM");
			tmpMap.put("stockType", "EDI");
			tmpMap.put("stockSign", "EDI");
			tmpMap.put("stockSeqLength", 7);
			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", "EDI-JOB NO");
			ediJobNo = commonMapper.selectNextStockMaxNo(tmpMap);
			
			data.put("ediJobNo", ediJobNo);
			data.put("entCode", separateMngDoc.getEntCode());
			data.put("entBusinessCode", separateMngDoc.getEntBusinessCode());
			data.put("ediDocNo", ediDocNo);
			data.put("issueDate", separateMngDoc.getIssueDate());
			data.put("registerUserId", separateMngDoc.getRegisterUserId());
			data.put("gmtTime", separateMngDoc.getGmtTime());
			
			//insert 처리
			separateMngMapper.insertSeparateMng(data);
			map.clear();
			map.put("ediDocNo", ediDocNo);
			map.put("odJobNoList", hList);
			separateMngMapper.updateSeparateJob(map);
	    	
	    }
		
		return separateMngSaveResult;
	}
	
	/**
	 * 별도관리물품해제신고 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public SeparateMngSaveResult deleteBatchSeparateMng(SeparateMngDoc separateMngDoc) throws Exception {
		SeparateMngSaveResult separateMngSaveResult = new SeparateMngSaveResult();
		
		List<SeparateMng> separateMngList = separateMngDoc.getSeparateMngList();		
		if(separateMngList == null) {
			separateMngSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("separateMngList", separateMngDoc.getSeparateMngList());
		map.put("searchEdiLinked", "Y");
		map.put("action", separateMngDoc.getAction());
		List<Map<String, Object>> checkList = separateMngMapper.selectSpecialData(map);
		String hblNoStr = "";
		if(checkList == null || checkList.size() == 0) {
			separateMngSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
		}else {
			for(Map<String, Object> checkData : checkList){
				if(checkData.get("EDI_JOB_NO") == null
						|| (!"".equals(Util.null2str(checkData.get("EDI_STATUS"))) && !"2".equals(Util.null2str(checkData.get("EDI_STATUS"))) && !"4".equals(Util.null2str(checkData.get("EDI_STATUS"))))){
					separateMngSaveResult.setResultCode("A00015");	/** 해당건을 수정, 삭제 할 수 없습니다. */
					hblNoStr += ", " + Util.null2str(checkData.get("HBL_NO"));
				}
			}
		}
		
		if(!"".equals(Util.null2str(separateMngSaveResult.getResultCode()))){
			if("A00015".equals(separateMngSaveResult.getResultCode())){
				map.clear();
				map.put("errorHblNo", hblNoStr.substring(2));
				separateMngSaveResult.setResultData(map);
			}
			return separateMngSaveResult;
		}
				
		separateMngMapper.deleteBatchSeparateMng(separateMngDoc);
		
		return separateMngSaveResult;
	}
	
}