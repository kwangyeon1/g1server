package org.yht.exps.express.service.sms;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.mapper.sms.SmsCousMapper;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.search.SearchEcmCustomerDetail;
import org.yht.exps.express.model.sms.search.SearchEcmCustomerList;
import org.yht.exps.express.model.sms.search.SearchEcmCustomerTreeList;
import org.yht.exps.express.model.sms.user.CustomerSaveResult;
import org.yht.exps.express.model.sms.user.EcmCustomer;
import org.yht.exps.express.model.sms.user.EcmCustomerDelete;
import org.yht.exps.express.model.sms.user.EcmCustomerList;
import org.yht.exps.express.model.sms.user.EcmCustomerMapping;
import org.yht.exps.express.model.sms.user.EcmCustomerTreeList;
import org.yht.exps.express.model.sms.user.EcmUser;
import org.yht.exps.express.model.sms.user.EcmUserApprove;
import org.yht.exps.express.model.sms.user.EcmUserDelete;
import org.yht.exps.express.model.sms.user.EcmUserDetail;
import org.yht.exps.express.model.sms.user.EcmUserList;
import org.yht.exps.express.model.sms.user.EcmUserSave;
import org.yht.exps.express.model.sms.user.UserSaveResult;
import org.yht.exps.express.model.sms.user.search.SearchEcmUserDetail;
import org.yht.exps.express.model.sms.user.search.SearchEcmUserList;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTime;
import org.yht.util.EncryptionUtils;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Slf4j
@Service
@Transactional
public class SmsCousService {
	
	@Autowired
	CustomProperties customProperties;
		
	@Autowired
	SmsCousMapper smsCousMapper;
	
	@Autowired
    CommonService commonService;
	
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	@Value("${spring.profiles.active}")
	private String profile;
	
	/**
	 * ECM 웹 사용자 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public EcmUserList selectEcmUserList(SearchEcmUserList search) throws Exception {
		EcmUserList ecmUserList = new EcmUserList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		List<EcmUser> list  = new ArrayList<>();
		if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")){
			list  = smsCousMapper.selectEcmUserListVe(search);
		}else {
			list  = smsCousMapper.selectEcmUserList(search);
		}

		ecmUserList.setEcmUserList(list);
		if(list != null && list.size() > 0) {
			ecmUserList.setTotal(list.size());
		}else {
			ecmUserList.setTotal(0);
		}
		
		return ecmUserList;
	}
	
	/**
	 * ECM 거래처 트리 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public EcmCustomerTreeList selectEcmCustomTreeList(SearchEcmCustomerTreeList search) throws Exception {
		EcmCustomerTreeList ecmCustomerList = new EcmCustomerTreeList();

		JSONArray menuArray = JSONArray.fromObject(smsCousMapper.selectEcmCustomerTreeList(search));

		for(int i = 0; i < menuArray.size(); i++) {
			JSONObject menuObj = menuArray.getJSONObject(i);
		
			// JSONObject를 TreeView 구조에 맞게 재생성
			menuObj.put("items", new JSONArray());			
		}
		
		for(int i = 0; i < menuArray.size(); i++) {
			JSONObject menuObj = menuArray.getJSONObject(i);
			String upper = menuObj.getString("upper");
			
			if(!"null".equals(upper)) {
				boolean result = Common.setMenuArray(menuArray, menuObj, upper);
				if(result)	menuArray.remove(i--);
			}
		}
		
		ecmCustomerList.setEcmCustomerTreeList(menuArray);
		
		return ecmCustomerList;
	}
		
	/**
	 * ECM 웹 - 승인여부 업데이트
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public UserSaveResult updateEcmUserApprove(EcmUserApprove ecmUserApprove) throws Exception {
		smsCousMapper.updateEcmUserApprove(ecmUserApprove);
		UserSaveResult userSaveResult = new UserSaveResult();
		return userSaveResult;
	}
	
	/**
	 * ECM 웹 - 삭제처리
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public UserSaveResult deleteEcmUser(EcmUserDelete ecmUserDelete) throws Exception {

		smsCousMapper.deleteEcmUser(ecmUserDelete);		
		UserSaveResult userSaveResult = new UserSaveResult();
		return userSaveResult;
	}
	
	/**
	 * ECM 웹 - 삭제처리
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public UserSaveResult deleteEcmUserList(EcmUserDelete ecmUserDelete, HttpServletRequest request) throws Exception {
		List<EcmUser> ecmUserList = ecmUserDelete.getEcmUserList();
		if(ecmUserList == null || ecmUserList.size() == 0) {
			return null;
		}
		
		//메인삭제
		smsCousMapper.deleteEcmUserList(ecmUserDelete);
		
		UserSaveResult userSaveResult = new UserSaveResult();
		return userSaveResult;
	}
	
	/**
	 * ECM 웹 - 사용자 insert
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public UserSaveResult insertEcmUser(EcmUserSave ecmUserSave, HttpServletRequest request) throws Exception {
		
		String action = ecmUserSave.getAction();
		//신규
		if("insert".equals(action)) {
			Map<String,Object> param = new HashMap<String,Object>();
			ecmUserSave.setUserId(smsCousMapper.selectMakeUserId(param));
			ecmUserSave.setUserLoginPw(EncryptionUtils.encrypt_sha2(1000, ecmUserSave.getUserLoginPw()));
			
			smsCousMapper.insertEcmUser(ecmUserSave);		
			smsCousMapper.insertEcmUserOption(ecmUserSave);	
			
			ProcessLogs processLogs = new ProcessLogs();
			
			processLogs.setEntCode(ecmUserSave.getEntCode());
			processLogs.setSolution("ECM");
			processLogs.setProcessUserId(ecmUserSave.getUpdateUserId());
			processLogs.setLogJobType("CUSTOMER");
			processLogs.setJobFuncType("ADD");
			processLogs.setDescription("거래처 등록 > 회원저장");
			processLogs.setJobLog(new Gson().toJson(ecmUserSave));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(ecmUserSave.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(ecmUserSave.getLogUserName());
			processLogs.setProcessUserNameEng(ecmUserSave.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}
		
		UserSaveResult userSaveResult = new UserSaveResult();
		userSaveResult.setUserId(ecmUserSave.getUserId());
		
		return userSaveResult;
	}
	
	/**
	 * ECM 웹 - 사용자 update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public UserSaveResult updateEcmUser(EcmUserSave ecmUserSave) throws Exception {
		
		String action = ecmUserSave.getAction();
		//수정
		if("update".equals(action)) {
			ecmUserSave.setUserLoginPw(EncryptionUtils.encrypt_sha2(1000, ecmUserSave.getUserLoginPw()));
			smsCousMapper.updateEcmUser(ecmUserSave);
		}
		
		UserSaveResult userSaveResult = new UserSaveResult();
		userSaveResult.setUserId(ecmUserSave.getUserId());
		
		return userSaveResult;
	}
		
	/**
	 * ECM 웹 - 사용자 상세조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public EcmUserDetail selectEcmUserDetail(SearchEcmUserDetail search) throws Exception {
		EcmUserDetail detail  = smsCousMapper.selectEcmUserDetail(search);	
		return detail;
	}
	
	/**
	 * ECM 거래처 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public EcmCustomerList selectEcmCustomerList(SearchEcmCustomerList search) throws Exception {
		EcmCustomerList ecmCustomerList = new EcmCustomerList();
		
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
 		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		//업체 전체 추출
		if("ALL".equals(search.getSrchType())) {
			if(search.getPage() != 0 && search.getPageSize() != 0){
				search.setStartNum(search.getPageSize() * (search.getPage() - 1));
			}
			Util.accessControlDatabase(search, "userLanguage", true, null, "", null, "");
			List<EcmCustomer> list  = new ArrayList<>();
			//허브넷구분
			if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")){
				list  = smsCousMapper.selectEcmCustomerAllListVe(search);
			}else {
				list  = smsCousMapper.selectEcmCustomerAllList(search);
			}

			if(list != null && list.size() > 0) {
				ecmCustomerList.setTotal(list.get(0).getTotal());
			}else {
				ecmCustomerList.setTotal(0);
			}
			ecmCustomerList.setEcmCustomerList(list);
		}
		else {
			List<EcmCustomer> list  = smsCousMapper.selectEcmCustomerList(search);
			ecmCustomerList.setEcmCustomerList(list);
			if(list != null && list.size() > 0) {
				ecmCustomerList.setTotal(list.size());
			}else {
				ecmCustomerList.setTotal(0);
			}
		}
		
		return ecmCustomerList;
	}
	
	/**
	 * ECM 거래처 사용자등록여부 체크
	 * @param ecmCustomerDelete
	 * @return
	 * @throws Exception
	 */
	public int checkEcmCustomerUserCnt(EcmCustomerDelete ecmCustomerDelete) throws Exception {
		return smsCousMapper.checkEcmCustomerUserCnt(ecmCustomerDelete);
	}

	/**
	 * ECM 거래처 - 삭제처리
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public CustomerSaveResult deleteEcmCustomer(EcmCustomerDelete ecmCustomerDelete) throws Exception {

		String entCode = ecmCustomerDelete.getEntCode();
		
		//하위거래처추출 후 삭제
		List<EcmCustomer> delList = new ArrayList<EcmCustomer>();
		
		SearchEcmCustomerTreeList treeParam = new SearchEcmCustomerTreeList();
		treeParam.setSrchEntCode(entCode);
		treeParam.setSrchParentId(ecmCustomerDelete.getCustomerCode());	
		
		List<Map<String,Object>> treeList = smsCousMapper.selectEcmCustomerTreeList(treeParam);
		//리스트에 담기
		for(int i=0;i<treeList.size();i++) {
			Map<String,Object> row = treeList.get(i);
			EcmCustomer cus = new EcmCustomer();
			cus.setEntCode(entCode);
			cus.setCustomerCode((String)row.get("id"));
			delList.add(cus);
		}
		
		if(delList !=null && delList.size() >0) {
			ecmCustomerDelete.setEcmCustomerList(delList);
			
			//하위건들 삭제 방식에서 맵핑 대기업체로 이동하는 방식으로 변경됨
			//하위건들 맵핑 대기업체로 이동하기 위한 update
			EcmCustomerMapping lowDel2 = new EcmCustomerMapping();
			
			lowDel2.setParentCustomerCode("");
			lowDel2.setGmtTime(ecmCustomerDelete.getGmtTime());
			lowDel2.setMappingYn(ecmCustomerDelete.getMappingYn());
			lowDel2.setUpdateUserId(ecmCustomerDelete.getUpdateUserId());
			lowDel2.setEntCode(ecmCustomerDelete.getEntCode());
			lowDel2.setEcmCustomerList(delList);
			
			//매핑해제
			smsCousMapper.mappingExec(lowDel2);
			
			//레벨세팅
			smsCousMapper.updateLevel(lowDel2);
		}
		
		smsCousMapper.deleteEcmCustomer(ecmCustomerDelete);		
		CustomerSaveResult customerSaveResult = new CustomerSaveResult();
		return customerSaveResult;
	}
	
	/**
	 * ECM 거래처코드리스트 - 삭제처리
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public CustomerSaveResult deleteEcmCustomerList(EcmCustomerDelete ecmCustomerDelete) throws Exception {
		List<EcmCustomer> ecmCustomerList = ecmCustomerDelete.getEcmCustomerList();
		if(ecmCustomerList == null || ecmCustomerList.size() == 0) {
			return null;
		}
		
		String entCode = ecmCustomerDelete.getEntCode();		
		//하위사용자추출 후 삭제
		List<EcmCustomer> delList = new ArrayList<EcmCustomer>();	
		
		for(int j=0;j<ecmCustomerList.size();j++) {
			EcmCustomer tmpUsr = ecmCustomerList.get(j);
			
			SearchEcmCustomerTreeList treeParam = new SearchEcmCustomerTreeList();
			treeParam.setSrchEntCode(entCode);
			treeParam.setSrchParentId(tmpUsr.getCustomerCode());	
			
			List<Map<String,Object>> treeList = smsCousMapper.selectEcmCustomerTreeList(treeParam);
			//리스트에 담기
			for(int i=0;i<treeList.size();i++) {
				Map<String,Object> row = treeList.get(i);
				EcmCustomer usr = new EcmCustomer();
				usr.setEntCode(entCode);
				usr.setCustomerCode((String)row.get("id"));
				delList.add(usr);
			}
		}
		
		//하위건들 삭제
		if(delList !=null && delList.size() >0) {
			EcmCustomerDelete lowDel = new EcmCustomerDelete();
			lowDel.setEntCode(entCode);
			lowDel.setEcmCustomerList(delList);
			
			//하위건들 삭제 방식에서 맵핑 대기업체로 이동하는 방식으로 변경
			//하위건들 맵핑 대기업체로 이동하기 위한 update
			EcmCustomerMapping lowDel2 = new EcmCustomerMapping();
			
			lowDel2.setParentCustomerCode("");
			lowDel2.setGmtTime(ecmCustomerDelete.getGmtTime());
			lowDel2.setMappingYn(ecmCustomerDelete.getMappingYn());
			lowDel2.setUpdateUserId(ecmCustomerDelete.getUpdateUserId());
			lowDel2.setEntCode(ecmCustomerDelete.getEntCode());
			lowDel2.setEcmCustomerList(delList);
			
			//매핑해제
			smsCousMapper.mappingExec(lowDel2);
			
			//레벨세팅
			smsCousMapper.updateLevel(lowDel2);
		}
		//메인삭제
		smsCousMapper.deleteEcmCustomerList(ecmCustomerDelete);
		
		CustomerSaveResult customerSaveResult = new CustomerSaveResult();
		return customerSaveResult;
	}
	
	/**
	 * ECM 거래처 - insert
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public CustomerSaveResult insertEcmCustomer(EcmCustomer ecmCustomer, HttpServletRequest request) throws Exception {
		String action = ecmCustomer.getAction();
		//신규
		if("insert".equals(action)) {

			//허브넷일경우 로직추가
			if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")) {
				smsCousMapper.insertKreaminfo(ecmCustomer);
			}
			smsCousMapper.insertEcmCustomer(ecmCustomer);		
			smsCousMapper.insertEcmCustomerSolution(ecmCustomer);	
			
			//대표사업장 정보 추가
			smsCousMapper.insertEcmCustomerBusiness(ecmCustomer);
			if(!Objects.isNull(ecmCustomer.getBankInfo())) {
				smsCousMapper.insertEcmCustomerBankInfo(ecmCustomer);
			}
			if(!Util.isEmpty(ecmCustomer.getInterfaceList())) {
				smsCousMapper.insertEcmCustomerInterface(ecmCustomer);
			}
			ProcessLogs processLogs = new ProcessLogs();

			// 전자상거래 정보
			Map<String,Object> kreamMap = new HashMap<String, Object>();
			kreamMap.put("ENT_CODE", ecmCustomer.getEntCode());
			kreamMap.put("CUSTOMER_CODE", ecmCustomer.getCustomerCode());
			kreamMap.put("SOLUTION", "ECM");
			kreamMap.put("IF_SERVICE_CODE", "ECM_GOV_SIGN");
			kreamMap.put("IF_ETC_1", Util.null2str(ecmCustomer.getCommBusinessCode()));
			kreamMap.put("IF_ETC_2", Util.null2str(ecmCustomer.getClientId()));
			kreamMap.put("IF_ETC_3", Util.null2str(ecmCustomer.getClientSecret()));
			kreamMap.put("REGISTER_USER_ID", ecmCustomer.getUpdateUserId());
			kreamMap.put("UPDATE_USER_ID", ecmCustomer.getUpdateUserId());
			kreamMap.put("gmtTime", ecmCustomer.getGmtTime());
			smsCousMapper.insertCustomerInterface(kreamMap);

			processLogs.setEntCode(ecmCustomer.getEntCode());
			processLogs.setSolution("ECM");
			processLogs.setProcessUserId(ecmCustomer.getUpdateUserId());
			processLogs.setLogJobType("CUSTOMER");
			processLogs.setJobFuncType("ADD");
			processLogs.setDescription("거래처 등록");
			processLogs.setJobLog(new Gson().toJson(ecmCustomer));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(ecmCustomer.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(ecmCustomer.getLogUserName());
			processLogs.setProcessUserNameEng(ecmCustomer.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}
		
		CustomerSaveResult customerSaveResult = new CustomerSaveResult();
		customerSaveResult.setCustomerCode(ecmCustomer.getCustomerCode());
		
		return customerSaveResult;
	}
	
	/**
	 * ECM 거래처 - update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public CustomerSaveResult updateEcmCustomer(EcmCustomer ecmCustomer, HttpServletRequest request) throws Exception {
		
		String action = ecmCustomer.getAction();
		//수정qwert
		if("update".equals(action)) {
			smsCousMapper.updateEcmCustomer(ecmCustomer);
			smsCousMapper.deleteEcmCustomerBankInfo(ecmCustomer);
//			smsCousMapper.deleteEcmCustomerInterface(ecmCustomer);
			if(!Objects.isNull(ecmCustomer.getBankInfo())) {
				smsCousMapper.insertEcmCustomerBankInfo(ecmCustomer);
			}
			if(!Util.isEmpty(ecmCustomer.getInterfaceList())) {
				smsCousMapper.insertEcmCustomerInterface(ecmCustomer);
			}
			
			if(profile.contains("local") || profile.contains("hubn") || profile.contains("bms")) {
				if(ecmCustomer.getKreamApi1() != null || ecmCustomer.getKreamApi2() != null) {
					Map<String,Object> kreamMap = new HashMap<String, Object>();
					kreamMap.put("ENT_CODE", ecmCustomer.getEntCode());
					kreamMap.put("CUSTOMER_CODE", ecmCustomer.getCustomerCode());
					kreamMap.put("SOLUTION", "ECM");
					kreamMap.put("IF_SERVICE_CODE", "KREAM-ORDER-RECEIVE");
					kreamMap.put("IF_LOGIN_ID", ecmCustomer.getKreamApi1());
					kreamMap.put("IF_LOGIN_PW", ecmCustomer.getKreamApi2());
					kreamMap.put("REGISTER_USER_ID", ecmCustomer.getUpdateUserId());
					kreamMap.put("UPDATE_USER_ID", ecmCustomer.getUpdateUserId());
					kreamMap.put("gmtTime", ecmCustomer.getGmtTime());
					smsCousMapper.insertCustomerInterface(kreamMap);
				}
			}

			// 전자상거래 정보
			Map<String,Object> kreamMap = new HashMap<String, Object>();
			kreamMap.put("ENT_CODE", ecmCustomer.getEntCode());
			kreamMap.put("CUSTOMER_CODE", ecmCustomer.getCustomerCode());
			kreamMap.put("SOLUTION", "ECM");
			kreamMap.put("IF_SERVICE_CODE", "ECM_GOV_SIGN");
			kreamMap.put("IF_ETC_1", Util.null2str(ecmCustomer.getCommBusinessCode()));
			kreamMap.put("IF_ETC_2", Util.null2str(ecmCustomer.getClientId()));
			kreamMap.put("IF_ETC_3", Util.null2str(ecmCustomer.getClientSecret()));
			kreamMap.put("REGISTER_USER_ID", ecmCustomer.getUpdateUserId());
			kreamMap.put("UPDATE_USER_ID", ecmCustomer.getUpdateUserId());
			kreamMap.put("gmtTime", ecmCustomer.getGmtTime());
			smsCousMapper.insertCustomerInterface(kreamMap);

			ProcessLogs processLogs = new ProcessLogs();
			
			processLogs.setEntCode(ecmCustomer.getEntCode());
			processLogs.setSolution("ECM");
			processLogs.setProcessUserId(ecmCustomer.getUpdateUserId());
			processLogs.setLogJobType("CUSTOMER");
			processLogs.setJobFuncType("UPDATE");
			processLogs.setDescription("거래처 수정");
			processLogs.setJobLog(new Gson().toJson(ecmCustomer));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(ecmCustomer.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(ecmCustomer.getLogUserName());
			processLogs.setProcessUserNameEng(ecmCustomer.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}
		
		CustomerSaveResult customerSaveResult = new CustomerSaveResult();
		customerSaveResult.setCustomerCode(ecmCustomer.getCustomerCode());
		
		return customerSaveResult;
	}
		
	/**
	 * ECM 거래처 - 거래처 상세조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public EcmCustomer selectEcmCustomerDetail(SearchEcmCustomerDetail search) throws Exception {
		Util.accessControlDatabase(search, "userLanguage", false, null, "", null, "");
		EcmCustomer detail  = smsCousMapper.selectEcmCustomerDetail(search);
		detail.setBankInfo(smsCousMapper.selectEcmCustomerBank(search));
		detail.setInterfaceList(smsCousMapper.selectEcmCustomerInterface(search));
		
		if(profile.contains("local") || profile.contains("hubn") || profile.contains("bms")) {
			search.setIfServiceCode("KREAM-ORDER-RECEIVE");
			Map<String,Object> kreamMap = smsCousMapper.selectCustomerInterface(search);
			if(kreamMap != null) {
				detail.setKreamApi1(Util.null2str(kreamMap.get("IF_LOGIN_ID")));
				detail.setKreamApi2(Util.null2str(kreamMap.get("IF_LOGIN_PW")));
			}
		}

		search.setIfServiceCode("ECM_GOV_SIGN");
		Map<String,Object> serviceMap = smsCousMapper.selectCustomerInterface(search);
		if(serviceMap != null) {
			detail.setCommBusinessCode(Util.null2str(serviceMap.get("IF_ETC_1")));
			detail.setClientId(Util.null2str(serviceMap.get("IF_ETC_2")));
			detail.setClientSecret(Util.null2str(serviceMap.get("IF_ETC_3")));
		}

		return detail;
	}
	
	/**
	 * ECM 웹 - 맵핑처리 또는 해제 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public CustomerSaveResult mappingExec(EcmCustomerMapping ecmCustomerMapping) throws Exception {
	
		//맵핑처리 또는 해제
		smsCousMapper.mappingExec(ecmCustomerMapping);
		//레벨 셋팅
		smsCousMapper.updateLevel(ecmCustomerMapping);
		
		CustomerSaveResult customerSaveResult = new CustomerSaveResult();
		return customerSaveResult;
	}
	
	/**
	 * 상(하)위 업체 - 엑셀 업로드 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public CustomerSaveResult insertEcmCustomerExcel(String entCode, String updateUserId, String gmtTime, File file, String userBusinessCode, HttpServletRequest request) throws Exception{
		CustomerSaveResult customerSaveResult = new CustomerSaveResult();
		
		if(file != null){
			
			String[] fieldName = null;
			String[] fieldTitle = null;
			String[] fieldLength = null;
			String[] fieldType = null;

			fieldName = 	new String[]{"No","CUSTOMER_TYPE","CUSTOMER_CODE","CUSTOMER_NAME","CUSTOMER_NAME_ENG","ADDRESS_ENG","ATTN_ENG","TEL","FAX","ATTN_HP","EMAIL","HOME_PAGE_URL","AMS_COUNTRY_CODE","CURRENCY_CODE","CB_CODE","DELIVERY_CODE","PARENT_CUSTOMER_CODE"}; // 컬럼명
			fieldTitle =	new String[]{"No","거래처구분","거래처코드","거래처명","거래처명(Eng)","거래처주소(Eng)","담당자(Eng)","TEL No","FAX No","담당자 HP","E-MAIL","Homepage","국가코드","통화","관세사 코드","배송사","상위업체"}; 		 // 타이틀
			fieldLength =	new String[]{"99999","2","10","100","100","180","50","50","50","30","50","50","2","3","10","30","10"}; 			 // 길이
			fieldType =		new String[]{"N","S","S","S","S","S","S","S","S","S","S","S","S","S","S","S","S"}; 			 // 타입 
				
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				Sheet sheet = wb.getSheetAt(0);
				Row row;
				int lastRow = sheet.getLastRowNum();
				DataFormatter df = new DataFormatter();
				
				int fieldSize = fieldName.length;
				String extension;
				String fieldValue = "";
				
				if(file.getName().indexOf(".xlsx") > -1){
					extension = "xlsx";
				}else{
					extension = "xls";
				}
				
				String match = "[0-9a-zA-Z]*$";
	
				Map<String, Object> dataMap = null;
				List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
				StringBuffer errMsg = new StringBuffer();
				
				if ("xls".equals(extension)) {
					HSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){ // 0은 헤더이므로 1부터 시작 
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						String CUSTOMER_TYPE = "";
						String CUSTOMER_CODE = "";
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							
							//필수값 체크
							if(j == 1) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[거래처구분] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 2) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[거래처코드] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 3) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[거래처명] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 4) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[거래처명(Eng)] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 12) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[국가코드] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 13) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[통화] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//데이터 유효성 체크
							if(j == 1) {
								CUSTOMER_TYPE = fieldValue;
								if(!fieldValue.equals("HC") && !fieldValue.equals("SC")) {
									errMsg.append("[거래처구분] 항목은 HC 또는 SC만 가능합니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							if(j == 2) {
								if(!fieldValue.matches(match)) {
									errMsg.append("[거래처코드] 항목은 영문 또는 숫자만 가능합니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//중복체크
							if(j == 2) {
								Map<String, Object> checkMap = new HashMap<String, Object>();
								checkMap.put("entCode", entCode);
								checkMap.put("customerCode", fieldValue);
								int cnt = smsCousMapper.selectSmsCustomerCodeDupCheck(checkMap);
								if(cnt > 0) {
									errMsg.append("입력하신 [거래처코드] 이미 사용중 입니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
//							if(j == 3) {
//								Map<String, Object> checkMap = new HashMap<String, Object>();
//								checkMap.put("entCode", entCode);
//								checkMap.put("customerName", fieldValue);
//								int cnt = smsCousMapper.selectSmsCustomerNameDupCheck(checkMap);
//								if(cnt > 0) {
//									errMsg.append("입력하신 [거래처명] 이미 사용중 입니다.(행 : " + (i+1) + ") <br />");
//								}
//							}
							
							if(j == 16) {
								CUSTOMER_CODE = fieldValue;
							}

							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						
						if(CUSTOMER_TYPE.equals("SC")) {
							//필수값 체크
							if(StringUtils.isEmpty(CUSTOMER_CODE)) {
								errMsg.append("[거래처코드]가 SC 하위업체인 경우에는 [상위업체] 필수값 입니다.(행 : " + (i+1) + ") <br />");
							}else {
								//데이터 체크
								Map<String, Object> checkMap = new HashMap<String, Object>();
								checkMap.put("entCode", entCode);
								checkMap.put("customerCode", CUSTOMER_CODE);
								int cnt = smsCousMapper.selectSmsCustomerCodeDupCheck(checkMap);
								if(cnt < 1) {
									errMsg.append("입력하신 [상위업체]는 없는 업체입니다.(행 : " + (i+1) + ") <br />");
								}else {
									
								}
							}
						}
						if(errMsg.length() > 1000){
							break;
						}
						dataMap.put("row", i);
						dataList.add(dataMap);
					}
					
				}else{
					XSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){ // 0은 헤더이므로 1부터 시작
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						String CUSTOMER_TYPE = "";
						String CUSTOMER_CODE = "";
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							
							//필수값 체크
							if(j == 1) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[거래처구분] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 2) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[거래처코드] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 3) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[거래처명] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 4) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[거래처명(Eng)] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 12) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[국가코드] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 13) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[통화] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//데이터 유효성 체크
							if(j == 1) {
								CUSTOMER_TYPE = fieldValue;
								if(!fieldValue.equals("HC") && !fieldValue.equals("SC")) {
									errMsg.append("[거래처구분] 항목은 HC 또는 SC만 가능합니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							if(j == 2) {
								if(!fieldValue.matches(match)) {
									errMsg.append("[거래처코드] 항목은 영문 또는 숫자만 가능합니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//중복체크
							if(j == 2) {
								Map<String, Object> checkMap = new HashMap<String, Object>();
								checkMap.put("entCode", entCode);
								checkMap.put("customerCode", fieldValue);
								int cnt = smsCousMapper.selectSmsCustomerCodeDupCheck(checkMap);
								if(cnt > 0) {
									errMsg.append("입력하신 [거래처코드] 이미 사용중 입니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
//							if(j == 3) {
//								Map<String, Object> checkMap = new HashMap<String, Object>();
//								checkMap.put("entCode", entCode);
//								checkMap.put("customerName", fieldValue);
//								int cnt = smsCousMapper.selectSmsCustomerNameDupCheck(checkMap);
//								if(cnt > 0) {
//									errMsg.append("입력하신 [거래처명] 이미 사용중 입니다.(행 : " + (i+1) + ") <br />");
//								}
//							}
							
							if(j == 16) {
								CUSTOMER_CODE = fieldValue;
							}

							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						
						if(CUSTOMER_TYPE.equals("SC")) {
							//필수값 체크
							if(StringUtils.isEmpty(CUSTOMER_CODE)) {
								errMsg.append("[거래처코드]가 SC 하위업체인 경우에는 [상위업체] 필수값 입니다.(행 : " + (i+1) + ") <br />");
							}
						}
						if(errMsg.length() > 1000){
							break;
						}
						dataMap.put("row", i);
						dataList.add(dataMap);
					}
				}
				
				/** 취약성점검추가(Denial of Service: StringBuilder) */
				if(errMsg.length() == 0 && dataList.size() > 3000){
					errMsg.append("최대 입력은 3,000건 입니다.");
				}
				
				if(errMsg.length() != 0){
					customerSaveResult.setResultCode("dataErr");
					Map<String, Object> errMap = new HashMap<String, Object>();
					errMap.put("errStr", errMsg);
					customerSaveResult.setResultData(errMap);
					return customerSaveResult;
				}
				
				if(dataList.size() > 0){
					try {
						String tableExtend[] = {"", "A", "B"}; //주소 60바이트로 잘라서 넣어주기
						errMsg = new StringBuffer();
						for(Map<String, Object> coustomMap : dataList){
							int line = (int) coustomMap.get("row");
							boolean chk = true;
							coustomMap.put("entCode", entCode);
							coustomMap.put("entBusinessCode", "");
							coustomMap.put("gmtTime", gmtTime);
							coustomMap.put("solution", "ECM");
							coustomMap.put("updateUserId", updateUserId);
							coustomMap.put("LB_CFT_TYPE", "1");

							//ADDRESS_ENG 60바이트로 자르기
							if(!"".equals(Util.null2str(coustomMap.get("ADDRESS_ENG")))) {
								String Address_ENG = Util.fullWidthToHalfWidth(coustomMap.get("ADDRESS_ENG").toString());
								String[] tempStr = Util.strByteCut(Address_ENG, 60);
								for(int l = 0; l < tempStr.length; l++) {
									if(l == 0) coustomMap.put("ADDRESS_ENG"+tableExtend[l], tempStr[l]);
									else coustomMap.put("ADDRESS_"+tableExtend[l]+"_ENG", tempStr[l]);
								}
							}
							
							if("SC".equals(Util.null2str(coustomMap.get("CUSTOMER_TYPE")))) {
								//데이터 체크
								Map<String, Object> checkMap = new HashMap<String, Object>();
								checkMap.put("entCode", entCode);
								checkMap.put("customerCode", coustomMap.get("PARENT_CUSTOMER_CODE").toString());
								int cnt = smsCousMapper.selectSmsCustomerCodeDupCheck(checkMap);
								if(cnt < 1) {
									chk = false;
									errMsg.append("입력하신 " + coustomMap.get("PARENT_CUSTOMER_CODE").toString() + " [상위업체]는 없는 업체입니다.(행 : " + (line+1) + ") <br />");
								}
							}

							//CUSTOMER_LEVEL 구하기
							if("SC".equals(Util.null2str(coustomMap.get("CUSTOMER_TYPE")))) {
								String Level = smsCousMapper.selectSMSCustomerLevel(coustomMap);
								if(Level != null && !"".equals(Level)) {
									int Lev = Integer.parseInt(Level) + 1;
									coustomMap.put("CUSTOMER_LEVEL",Lev);
								}else {
									coustomMap.put("CUSTOMER_LEVEL","2");
								}
							}else coustomMap.put("CUSTOMER_LEVEL","1");
							
							//Insert
							if(chk) {
								smsCousMapper.insertSMSCustomer(coustomMap);
								smsCousMapper.insertSMSCustomerSolution(coustomMap);
								smsCousMapper.insertSMSCustomerBusiness(coustomMap);
							}
						}
						
						if(errMsg.length() != 0){
							customerSaveResult.setResultCode("dataErr");
							Map<String, Object> errMap = new HashMap<String, Object>();
							//errMsg.append("오류 처리된 "+ errMsg.length() + " 건 이외는 업로드 되었습니다. <br />");
							errMap.put("errStr", errMsg);
							customerSaveResult.setResultData(errMap);
							return customerSaveResult;
						}
//						
				}catch(Exception e) {
						throw e;
					}finally{
						wb.close();
					}
				}
			
				String backupPath = customProperties.getBackupDir() + "excel/" + entCode + "/" + DateTime.getDate("yyyy-MM");
				File dir = new File(backupPath);
				if (dir.exists() == false)	dir.mkdirs();
				String backupFilePath = backupPath + "/" + file.getName();
				file.renameTo(new File(backupFilePath));
			}catch(Exception e) {
				throw e;
			}finally{
				if (is != null) is.close();
			}
		}else{
			customerSaveResult.setResultCode("ERROR");
		}
		
		return customerSaveResult;
	}


	/**
	 * 상(하)위 업체 - 엑셀 업로드 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public UserSaveResult insertEcmUserInfoExcel(String entCode, String updateUserId, String gmtTime, File file, String userBusinessCode, String bussSolutionData, String headEntBusinessCode, HttpServletRequest request) throws Exception{
		UserSaveResult userSaveResult = new UserSaveResult();
		
		if(file != null){
			
			String[] fieldName = null;
			String[] fieldTitle = null;
			String[] fieldLength = null;
			String[] fieldType = null;

			fieldName = 	new String[]{"No","CUSTOMER_CODE","USER_LOGIN_ID","USER_LOGIN_PW","USER_NAME_ENG","USER_CELLPHONE","USER_TEL","USER_EMAIL","DEFAULT_BOUND","DEFAULT_BOUND_TYPE"}; // 컬럼명
			fieldTitle =	new String[]{"No","거래처코드","사용자ID","사용자PWD","사용자명(ENG)","HP No","TEL No","E-MAIL","Bound","Bound-Type"}; 		 // 타이틀
			fieldLength =	new String[]{"99999","10","20","64","50","50","50","50","1","1"}; 			 // 길이
			fieldType =		new String[]{"N","S","S","S","S","S","S","S","S","S"}; 			 // 타입 
				
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				Sheet sheet = wb.getSheetAt(0);
				Row row;
				int lastRow = sheet.getLastRowNum();
				DataFormatter df = new DataFormatter();
				
				int fieldSize = fieldName.length;
				String extension;
				String fieldValue = "";
				
				if(file.getName().indexOf(".xlsx") > -1){
					extension = "xlsx";
				}else{
					extension = "xls";
				}
				
				String match = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$"; //영문+숫자+특수문자 8~15자리
				
				Map<String, Object> dataMap = null;
				List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
				StringBuffer errMsg = new StringBuffer();
				
				if ("xls".equals(extension)) {
					HSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){ // 0은 헤더이므로 1부터 시작 
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							
							//필수값 체크
							if(j == 1) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[거래처코드] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 2) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[사용자ID] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 3) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[사용자PWD] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 4) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[사용자명(ENG)] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 8) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[Bound] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 9) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[Bound-Type] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//데이터 유효성 체크
							if(j == 1) {
								Map<String, Object> checkMap = new HashMap<String, Object>();
								checkMap.put("entCode", entCode);
								checkMap.put("customerCode", fieldValue);
								int cnt = smsCousMapper.selectSmsCustomerCodeDupCheck(checkMap);
								if(cnt < 1) {
									errMsg.append("입력하신 [거래처코드]는 없는 업체입니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							if(j == 3) {
								if(!Util.null2str(fieldValue).matches(match)) {
									errMsg.append("[사용자PWD] 8 ~ 15자의 (영문/숫자/특수문자) 혼용만 입력할 수 있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//중복체크
							if(j == 2) {
								Map<String, Object> checkMap = new HashMap<String, Object>();
								checkMap.put("entCode", entCode);
								checkMap.put("userLoginId", fieldValue);
								int cnt = smsCousMapper.selectSmsUserIdDupCheck(checkMap);
								if(cnt > 0) {
									errMsg.append("입력하신 [사용자ID] 이미 사용중 입니다.(행 : " + (i+1) + ") <br />");
								}
							}

							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						dataList.add(dataMap);
					}
					
				}else{
					XSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){ // 0은 헤더이므로 1부터 시작
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							
							//필수값 체크
							if(j == 1) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[거래처코드] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 2) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[사용자ID] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 3) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[사용자PWD] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 4) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[사용자명(ENG)] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 8) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[Bound] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							if(j == 9) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[Bound-Type] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//데이터 유효성 체크
							if(j == 1) {
								Map<String, Object> checkMap = new HashMap<String, Object>();
								checkMap.put("entCode", entCode);
								checkMap.put("customerCode", fieldValue);
								int cnt = smsCousMapper.selectSmsCustomerCodeDupCheck(checkMap);
								if(cnt < 1) {
									errMsg.append("입력하신 [거래처코드]는 없는 업체입니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							if(j == 3) {
								if(!Util.null2str(fieldValue).matches(match)) {
									errMsg.append("[사용자PWD] 8 ~ 15자의 (영문/숫자/특수문자) 혼용만 입력할 수 있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//중복체크
							if(j == 2) {
								Map<String, Object> checkMap = new HashMap<String, Object>();
								checkMap.put("entCode", entCode);
								checkMap.put("userLoginId", fieldValue);
								int cnt = smsCousMapper.selectSmsUserIdDupCheck(checkMap);
								if(cnt > 0) {
									errMsg.append("입력하신 [사용자ID] 이미 사용중 입니다.(행 : " + (i+1) + ") <br />");
								}
							}

							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						dataList.add(dataMap);
					}
				}
				if(errMsg.length() != 0){
					userSaveResult.setResultCode("dataErr");
					Map<String, Object> errMap = new HashMap<String, Object>();
					errMap.put("errStr", errMsg);
					userSaveResult.setResultData(errMap);
					return userSaveResult;
				}
				
				if(dataList.size() > 0){
					try {
						SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
						Date date = new Date(System.currentTimeMillis());
						
						for(Map<String, Object> userMap : dataList){
							userMap.put("entCode", entCode);
							userMap.put("entBusinessCode", headEntBusinessCode);
							userMap.put("gmtTime", gmtTime);
							userMap.put("solution", "ECM");
							userMap.put("updateUserId", updateUserId);
							userMap.put("userClass", "ECM-USER");
							userMap.put("useYn", "Y");
							userMap.put("dateFormat", "23");
							userMap.put("positionCode", "23");
							userMap.put("departmentCode", "02");
							userMap.put("searchOptionHideYn", "N");
							userMap.put("searchStartTerm", "1");
							userMap.put("searchEndTerm", "1");
							userMap.put("searchAllUserYn", "Y");
							userMap.put("searchDefaultUserYn", "N");
							userMap.put("pwUpdateDate", formatter.format(date));
							userMap.put("pwUpdateDays", "0");
							userMap.put("pwErrorCount", "0");
							userMap.put("pwUpdateAlertDays", "0");
							userMap.put("spreadYn", "Y");
							userMap.put("validUseDays", "0");
							userMap.put("srchServices", bussSolutionData);
							
							//사용자 ID 구하기
							String UserId = smsCousMapper.selectMakeUserId(userMap);
							userMap.put("userId", UserId);
							
							//USER_TYPE 값
							String USER_TYPE = smsCousMapper.selectUserType(userMap);
							userMap.put("userType", USER_TYPE);

							//PWD 암호화
							String PWD = EncryptionUtils.encrypt_sha2(1000, Util.null2str(userMap.get("USER_LOGIN_PW")));
							userMap.put("USER_LOGIN_PW", PWD);
							
							//Insert
							smsCousMapper.insertSMSUser(userMap);
							smsCousMapper.insertSMSUserOption(userMap);	
						}
				}catch(Exception e) {
						throw e;
					}finally{
						wb.close();
					}
				}
			
				String backupPath = customProperties.getBackupDir() + "excel/" + entCode + "/" + DateTime.getDate("yyyy-MM");
				File dir = new File(backupPath);
				if (dir.exists() == false)	dir.mkdirs();
				String backupFilePath = backupPath + "/" + file.getName();
				file.renameTo(new File(backupFilePath));
			}catch(Exception e) {
				throw e;
			}finally{
				if (is != null) is.close();
			}
		}else{
			userSaveResult.setResultCode("ERROR");
		}
		
		return userSaveResult;
	}


	
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row) throws Exception {
		//TODO 엑셀 셀타입 텍스트로 고정시킬수있는지?  없다면 추가개발필요
		if(type.equals("N")){
			if (Util.null2str(data).equals("")){
				data = "0";
			} else {
				try {
//					int idx = data.indexOf("E");	/** 8자리가 넘어가면 123456789.123 => 1.23456789123E8 로 표현됨 */
//					if(idx > -1){
//						int k = Util.null2int(data.substring(idx + 1)) + 1;
//						data = data.substring(0, idx).replace(".", "");
//						data = data.substring(0, k) + "." + data.substring(k);
//					}
					int dotIndex = data.indexOf(".");
					if (dotIndex > 0) {
						data = data.substring(0, dotIndex);
					}
					
					if(data.length() > Util.null2int(length)){
						errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
					}
					Integer.parseInt(data);	/** 숫자인지 체크 */
				}catch (Exception e){
					errMsg.append("[" + name + "] DATA TYPE 오류(행 : " + row + ", 입력값 : " + data + ") <br />");
				}
			}
		}else if(type.equals("DATE")){
			if (Util.null2str(data).equals("")) {
				data = null;
			}else{
				try{
//					if(!data.startsWith("2")){
//						String[] tmpData = data.split("/");
//						data = "20" + tmpData[2] + (tmpData[0].length() == 1 ? "0" + tmpData[0] : tmpData[0]) + tmpData[1];
//					}else{
						SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyyMMdd");
						data = data.replaceAll("[-./]", "").substring(0, 8);
						data = sdFormat.format(sdFormat2.parse(data));
//					}
				}catch (Exception e){
					errMsg.append("[" + name + "] DATA TYPE 오류(행 : " + row + ", 입력값 : " + data + ") <br />");
				}
			}
		}else if("DBL".equals(type)){
			if (Util.null2str(data).equals("")) {
				data = "0";
			} else {
				try {
					Double.parseDouble(data);
				} catch (Exception e) {
					errMsg.append("[" + name + "] DATA TYPE 오류(행 : " + row + ", 입력값 : " + data + ") <br />");
				}
			}
//			int idx = data.indexOf("E");	/** 8자리가 넘어가면 123456789.123 => 1.23456789123E8 로 표현됨 */
//			if(idx > -1){
//				int k = Util.null2int(data.substring(idx + 1)) + 1;
//				data = data.substring(0, idx).replace(".", "");
//				data = data.substring(0, k) + "." + data.substring(k);
//			}
			String[] lengthCnt = Util.null2str(length).split("\\.");
			String[] lengthData = data.split("\\.");
			int j = 0;
			if(lengthCnt.length > 1){
				j = Util.null2int(lengthCnt[1]);
			}
			if(lengthData[0].length() > Util.null2int(lengthCnt[0]) - j) {
				errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + ") <br />");
			} else if(lengthData.length > 1 && lengthData[1].length() > j) {
				errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + ") <br />");
			}
			
		}else if("S".equals(type)){
			if(Util.null2str(data).equals("")){
				data = null;
			}else{
				if(data.getBytes("euc-kr").length > Util.null2int(length)) {
					errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
				}
			}
		}else{
			if(Util.null2str(data).equals("")){
				data = null;
			}else{
				if(data.length() > Util.null2int(length)) {
					errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
				}
			}
		}
		return data;
	}
	
}
