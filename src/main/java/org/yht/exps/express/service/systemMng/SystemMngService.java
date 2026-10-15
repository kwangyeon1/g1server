package org.yht.exps.express.service.systemMng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import org.apache.commons.text.StringEscapeUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.systemMng.SystemMngMapper;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.sms.backup.OdBackup;
import org.yht.exps.express.model.systemMng.AddressZipCode;
import org.yht.exps.express.model.systemMng.AddressZipCodeList;
import org.yht.exps.express.model.systemMng.AlarmGroup;
import org.yht.exps.express.model.systemMng.AlarmGroupList;
import org.yht.exps.express.model.systemMng.DeliveryInterface;
import org.yht.exps.express.model.systemMng.DeliveryInterfaceList;
import org.yht.exps.express.model.systemMng.InterfaceKey;
import org.yht.exps.express.model.systemMng.InterfaceKeyCount;
import org.yht.exps.express.model.systemMng.InterfaceKeyCountList;
import org.yht.exps.express.model.systemMng.InterfaceKeyList;
import org.yht.exps.express.model.systemMng.MonthlyCount;
import org.yht.exps.express.model.systemMng.MonthlyCountList;
import org.yht.exps.express.model.systemMng.ZipCode;
import org.yht.exps.express.model.systemMng.ZipCodeList;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEnt;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntDelete;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntInsert;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntList;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplate;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplateDelete;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplateInsert;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplateList;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkSaveResult;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkSend;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplate;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplateDelete;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplateInsert;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplateList;
import org.yht.exps.express.model.systemMng.alimTalk.search.SearchAlimTalkEnt;
import org.yht.exps.express.model.systemMng.alimTalk.search.SearchAlimTalkTemplate;
import org.yht.exps.express.model.systemMng.search.SearchAddressZipCodeList;
import org.yht.exps.express.model.systemMng.search.SearchAlarmGroup;
import org.yht.exps.express.model.systemMng.search.SearchDeliveryInterface;
import org.yht.exps.express.model.systemMng.search.SearchDeliveryInterfaceList;
import org.yht.exps.express.model.systemMng.search.SearchInterfaceKey;
import org.yht.exps.express.model.systemMng.search.SearchInterfaceKeyCountList;
import org.yht.exps.express.model.systemMng.search.SearchInterfaceKeyList;
import org.yht.exps.express.model.systemMng.search.SearchZipCode;
import org.yht.exps.express.model.systemMng.search.SearchZipCodeList;
import org.yht.util.EncryptionUtils;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class SystemMngService {

	@Autowired
	SystemMngMapper systemMngMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Value("${spring.profiles.active}")
	private String profile;
	/**
	 * InterfaceKey List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public InterfaceKeyList selectInterfaceKeyList(SearchInterfaceKeyList search) throws Exception {
		InterfaceKeyList interfaceKeyList = new InterfaceKeyList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, "srchDateGbn|srchDateFormat", true, null, "", null, "");
		List<InterfaceKey> list  = systemMngMapper.selectInterfaceKeyList(search);
		interfaceKeyList.setInterfaceKeyList(list);
		if(list.size() > 0) {
			interfaceKeyList.setTotal(list.get(0).getTotal());
		}else {
			interfaceKeyList.setTotal(0);
		}
		return interfaceKeyList;
	}
	
	/**
	 * InterfaceKeyCount List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public InterfaceKeyCountList selectInterfaceKeyCountList(SearchInterfaceKeyCountList search) throws Exception {
		InterfaceKeyCountList interfaceKeyCountList = new InterfaceKeyCountList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
	
		Util.accessControlDatabase(search, null, true, null, "", null, "");
		List<InterfaceKeyCount> list  = systemMngMapper.selectInterfaceKeyCountList(search);
		interfaceKeyCountList.setInterfaceKeyCountList(list);
		if(list.size() > 0) {
			interfaceKeyCountList.setTotal(list.get(0).getTotal());
		}else {
			interfaceKeyCountList.setTotal(0);
		}
		return interfaceKeyCountList;
	}
	
	/**
	 * 전자상거래 InterfaceKey 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public InterfaceKey selectInterfaceKey(SearchInterfaceKey search) throws Exception {
		return systemMngMapper.selectInterfaceKey(search);
	}
	
	/**
	 * 전자상거래 InterfaceKey - insert/update
	 * @param interfaceKey
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveInterfaceKey(InterfaceKey interfaceKey) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		String action = interfaceKey.getAction();	/** insert/update구분 */
		Util.accessControlDatabase(interfaceKey, "gmtTime", false, null, "", null, "");
		
		if(action.equals("insert")){
			/** 중복체크 */
			Map<String, Object> chkMap = systemMngMapper.selectInterfaceKeyDupChk(interfaceKey);
			if(chkMap != null && chkMap.size() > 0){
				orderSaveResult.setResultCode("duplicate");
				return orderSaveResult;
			}
			interfaceKey.setAccessKey(EncryptionUtils.encrypt_sha2(1000, interfaceKey.getCustomerCode().toLowerCase()));
			systemMngMapper.insertInterfaceKey(interfaceKey);
		}else if(action.equals("update")){
			systemMngMapper.updateInterfaceKey(interfaceKey);
		}
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", interfaceKey.getEntCode());
		tmpMap.put("customerCode", interfaceKey.getCustomerCode());
		tmpMap.put("accessKey", interfaceKey.getAccessKey());
		orderSaveResult.setResultData(tmpMap);
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 InterfaceKey - delete
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult deleteInterfaceKey(List<SearchInterfaceKey> search, String srchEntCode) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("srchEntCode", srchEntCode);
		tmpMap.put("customerCodeList", search);
		systemMngMapper.deleteInterfaceKey(tmpMap);
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 배송사 Interface List 조회
	 * @param SearchStockList
	 * @return
	 * @throws Exception
	 */	
	public DeliveryInterfaceList selectDeliveryInterfaceList(SearchDeliveryInterfaceList search) throws Exception {
		
		
		DeliveryInterfaceList deliveryInterfaceList = new DeliveryInterfaceList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, "srchDateGbn|srchDateFormat", true, null, "", null, "");
		List<DeliveryInterface> list  = systemMngMapper.selectDeliveryInterfaceList(search);
		deliveryInterfaceList.setDeliveryInterfaceList(list);
		if(list.size() > 0) {
			deliveryInterfaceList.setTotal(list.get(0).getTotal());
		}else {
			deliveryInterfaceList.setTotal(0);
		}
		
		return deliveryInterfaceList;
		
		
	}
	
	/**
	 * 전자상거래 DeliveryInterface 조회(Entry)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public DeliveryInterface selectDeliveryInterface(SearchDeliveryInterface search) throws Exception {

		if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")){
			return systemMngMapper.selectDeliveryInterfaceVe(search);
		}else {
			return systemMngMapper.selectDeliveryInterface(search);
		}
	}	
	
//	/**
//	 * 전자상거래 DeliveryInterface 조회(Entry) - API
//	 * @param search
//	 * @return
//	 * @throws Exception
//	 */
//	public DeliveryInterface selectDeliveryInterfaceApi(SearchDeliveryInterface search) throws Exception {
//		DeliveryInterface deliveryInterface = new DeliveryInterface();
//		deliveryInterface.setGridData(systemMngMapper.selectDeliveryInterfaceApi(search));
//		return deliveryInterface;
//	}	

	
	/**
	 * 전자상거래 배송사 Interface Insert
	 * @param InsertStock
	 * @return
	 * @throws Exception
	 */	
	public OrderSaveResult saveDeliveryInterface(DeliveryInterface deliveryInterface) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		String action = deliveryInterface.getAction();	/** insert/update구분 */
		Util.accessControlDatabase(deliveryInterface, "gmtTime", false, null, "", null, "");
		if(action.equals("insert")){ // 등록
			/** 거래처별 바운드 중복저장 불가   update시에는 거래처/바운드 수정불가 항목이어서 insert시에만 체크  */
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("srchEntCode", deliveryInterface.getEntCode());
//			2024.03.07 바운드 COM 사용하지 않음
//			if("COM".equals(deliveryInterface.getBound())){
//				map.put("srchBoundIn", "'AIR', 'SEA', 'COM'");
//			}else{
//				map.put("srchBoundIn", "'" + deliveryInterface.getBound() + "', 'COM'");
//			}
			map.put("srchBound", deliveryInterface.getBound());
			map.put("srchCustomerCode", deliveryInterface.getCustomerCode());
			map.put("srchLogisticsCode", deliveryInterface.getLogisticsCode());
			map = systemMngMapper.selectDeliveryInterfaceCheck(map);
			if(map != null && !Util.isEmpty(map.get("CUSTOMER_CODE"))){
				orderSaveResult.setResultCode("A00073");	/** 중복되는 #1# 값이 있습니다. */
				return orderSaveResult;
			}
			if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11") || profile.equals("ups1") || profile.equals("pantos1")){
				systemMngMapper.insertDeliveryInterfaceVe(deliveryInterface);
			}else {
				systemMngMapper.insertDeliveryInterface(deliveryInterface);
			}

		}else if(action.equals("update")){ // 수정
			if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11") || profile.equals("ups1") || profile.equals("pantos1")){
				systemMngMapper.updateDeliveryInterfaceVe(deliveryInterface);
			}else {
				systemMngMapper.updateDeliveryInterface(deliveryInterface);
			}
		}
		return orderSaveResult;
	}	
	
	/**
	 * 전자상거래 배송사 Interface Delete
	 * @param 
	 * @return
	 * @throws Exception
	 */	
	
	public OrderSaveResult deleteDeliveryInterface(SearchDeliveryInterface delete) throws Exception {
		OrderSaveResult result = new OrderSaveResult();
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		
		for (Map<String, Object> deleteMap : deleteList) {
			systemMngMapper.deleteDeliveryInterface(deleteMap);
		}
		
		return result;
	}		
	
	/**
	 * 월별건수 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MonthlyCountList selectMonthlyCountList(SearchInterfaceKeyList search) throws Exception {
		MonthlyCountList monthlyCountList = new MonthlyCountList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
	
		Util.accessControlDatabase(search, null, true, null, "", null, "");
		List<MonthlyCount> list  = systemMngMapper.selectMonthlyCountList(search);
		monthlyCountList.setMonthlyCountList(list);
		if(list.size() > 0) {
			monthlyCountList.setTotal(list.get(0).getTotal());
		}else {
			monthlyCountList.setTotal(0);
		}
		return monthlyCountList;
	}
	
	/**
	 * 알림 서비스 저장
	 * @param SearchAlarmGroup
	 * @return
	 * @throws Exception
	 */	
	public OrderSaveResult saveAlarmGroup(SearchAlarmGroup search) throws Exception {
		OrderSaveResult result = new OrderSaveResult();
		Util.accessControlDatabase(search, "gmtTime|srchDateFormat", true, null, "", null, "");
		if(Util.isEmpty(search.getSrchAlarmType())){
			search.setSrchMode("listCount");
			List<AlarmGroup> list  = systemMngMapper.selectAlarmGroupList(search);
			if(list != null && list.size() > 0){
				result.setResultCode("A11253");	/** 이미 생성된 건이 존재합니다. */
			}else{
				systemMngMapper.insertAlarmGroup(search);
			}
		}else{
			if(Util.null2int(systemMngMapper.selectAlarmGroupUserSaveChk(search)) > 0) {
				result.setResultCode("A11253");	/** 이미 생성된 건이 존재합니다. */
			}else{
				if("insert".equals(search.getAction())){
					Util.accessControlDatabase(search, "gmtTime", false, null, "", null, "");
					systemMngMapper.insertAlarmGroupUser(search);
				}else{
					systemMngMapper.updateAlarmGroupUser(search);
				}
			}
		}
		
		return result; 
	}
	
	/**
	 * 알림 서비스 삭제
	 * @param SearchAlarmGroup
	 * @return
	 * @throws Exception
	 */	
	public OrderSaveResult deleteAlarmGroup(SearchAlarmGroup search) throws Exception {
		OrderSaveResult result = new OrderSaveResult();
		Util.accessControlDatabase(search, "gmtTime|srchDateFormat", true, null, "", null, "");
		if(Util.isEmpty(search.getSrchGroupId())){
			systemMngMapper.deleteAlarmGroupUser(search);
		}else{
			search.setSrchMode("listCount");
			List<AlarmGroup> list  = systemMngMapper.selectAlarmGroupList(search);
			if(list != null && list.size() == 1){
				if(Util.null2int(list.get(0).getUserCount()) != 0){
					result.setResultCode("A11253");	/** 이미 생성된 건이 존재합니다. */
				}else{
					systemMngMapper.deleteAlarmGroup(search);
				}
			}
		}
		
		return result; 
	}
	
	/**
	 * 알림 서비스 리스트 조회
	 * @param SearchAlarmGroup
	 * @return
	 * @throws Exception
	 */	
	public AlarmGroupList selectAlarmGroupList(SearchAlarmGroup search) throws Exception {
		AlarmGroupList alarmGroupList = new AlarmGroupList();
		search.setPage(0);
		Util.camelCaseToUnderscore(search);
		Util.accessControlDatabase(search, "gmtTime|srchDateFormat", true, null, "", null, "");
		List<AlarmGroup> list  = systemMngMapper.selectAlarmGroupList(search);
		alarmGroupList.setAlarmGroupList(list);
		return alarmGroupList; 
	}
	
	/**
	 * 알림 서비스 수신자 리스트 조회
	 * @param SearchAlarmGroup
	 * @return
	 * @throws Exception
	 */	
	public AlarmGroupList selectAlarmGroupUserList(SearchAlarmGroup search) throws Exception {
		AlarmGroupList alarmGroupList = new AlarmGroupList();
		search.setPage(0);
		Util.camelCaseToUnderscore(search);
		Util.accessControlDatabase(search, "srchDateFormat", false, null, "", null, "");
		List<AlarmGroup> list  = systemMngMapper.selectAlarmGroupUserList(search);
		alarmGroupList.setAlarmGroupList(list);
		return alarmGroupList; 
	}
	
	/**
	 * 알림톡 업체등록정보 List(main) 조회
	 * @param SearchAlimTalkEnt
	 * @return
	 * @throws Exception
	 */	
	
	public AlimTalkEntList selectAlimTalkEntList(SearchAlimTalkEnt search) throws Exception {
		
		AlimTalkEntList alimTalkEntList = new AlimTalkEntList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, null, true, null, "", null, "");
		List<AlimTalkEnt> list  = systemMngMapper.selectAlimTalkEntList(search);
		alimTalkEntList.setAlimTalkEntList(list);
		if(list.size() > 0) {
			alimTalkEntList.setTotal(list.get(0).getTotal());
		}else {
			alimTalkEntList.setTotal(0);
		}
		
		return alimTalkEntList; 
	}		
	
	/**
	 * 알림톡 업체에 등록된 템플릿 List(sub) 조회
	 * @param SearchAlimTalkEnt
	 * @return
	 * @throws Exception
	 */	
	
	public AlimTalkEntTemplateList selectAlimTalkEntTemplateList(SearchAlimTalkEnt search) throws Exception {
		
		AlimTalkEntTemplateList alimTalkEntTemplateList = new AlimTalkEntTemplateList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, null, true, null, null, null, null);
		List<AlimTalkEntTemplate> list  = systemMngMapper.selectAlimTalkEntTemplateList(search);
		alimTalkEntTemplateList.setAlimTalkEntTemplateList(list);
		if(list.size() > 0) { 
			alimTalkEntTemplateList.setTotal(list.get(0).getTotal());
		}else {
			alimTalkEntTemplateList.setTotal(0);
		}
		
		return alimTalkEntTemplateList; 
	}		
	
	/**
	 * 시스템관리 알림톡 조회
	 * @param SearchAlimTalkEnt
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkEntTemplate selectAlimTalkData(SearchAlimTalkEnt search) throws Exception {
		Util.accessControlDatabase(search, null, true, null, null, null, null);
		List<AlimTalkEntTemplate> list = systemMngMapper.selectAlimTalkEntTemplateList(search);
		if(list != null && list.size() > 0){
			return list.get(0);
		}else {
			return new AlimTalkEntTemplate();
		}
	}
	
	/**
	 * 시스템관리 알림톡 템플릿 조회
	 * @param SearchAlimTalkEnt
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkTemplate selectAlimTalkTemplate(SearchAlimTalkTemplate search) throws Exception {
		Util.accessControlDatabase(search, "srchNoType", true, null, "", null, "");
		List<AlimTalkTemplate> list = systemMngMapper.selectAlimTalkTemplateList(search);
		if(list != null && list.size() > 0){
			return list.get(0);
		}else {
			return new AlimTalkTemplate();
		}
	}
	
	/**
	 * 알림톡 업체 데이터 등록 
	 * @param AlimTalkEntInsert
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkSaveResult updateEnt(AlimTalkEntInsert insert) throws Exception {
		AlimTalkSaveResult result = new AlimTalkSaveResult();
		//List<Map<String, Object>> insertList = new ArrayList<Map<String, Object>>();

		systemMngMapper.updateEnt(insert);

		return result;
		
		
	}
	
	
	/**
	 * 알림톡 알림톡 데이터 등록 
	 * @param AlimTalkTemplateInsert
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkSaveResult insertTemplate(AlimTalkTemplateInsert insert, HttpServletRequest request) throws Exception {
		AlimTalkSaveResult result = new AlimTalkSaveResult();
		systemMngMapper.insertTemplate(insert);

		return result;
		
	}	
	
	/**
	 * 알림톡 알림톡 데이터 수정 
	 * @param AlimTalkTemplateInsert
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkSaveResult updateTemplate(AlimTalkTemplateInsert insert, HttpServletRequest request) throws Exception {
		AlimTalkSaveResult result = new AlimTalkSaveResult();
		Util.accessControlDatabase(insert, "gmtTime", false, null, "", null, "");
		systemMngMapper.updateTemplate(insert);

		return result;
		
	}

	/**
	 * 알림톡 템플릿 List(sub2) 조회
	 * @param SearchAlimTalkTemplate
	 * @return
	 * @throws Exception
	 */	
	
	public AlimTalkTemplateList selectAlimTalkTemplateList(SearchAlimTalkTemplate search) throws Exception {
		
		AlimTalkTemplateList alimTalkTemplateList = new AlimTalkTemplateList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, "srchNoType", true, null, "", null, "");
		List<AlimTalkTemplate> list  = systemMngMapper.selectAlimTalkTemplateList(search);
		alimTalkTemplateList.setAlimTalkTemplateList(list);
		if(list.size() > 0) {
			alimTalkTemplateList.setTotal(list.get(0).getTotal());
		}else {
			alimTalkTemplateList.setTotal(0);
		}
		
		return alimTalkTemplateList; 
	}	
	
	
	/**
	 * 알림톡 업체-템플릿 연결 등록 
	 * @param AlimTalkTemplateInsert
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkSaveResult insertEntTemplate(AlimTalkEntTemplateInsert insert) throws Exception {
		AlimTalkSaveResult result = new AlimTalkSaveResult();
		
		if(systemMngMapper.selectEntTemplateDupChk(insert) > 0){
			result.setResultCode("dup");
		}else if(systemMngMapper.selectEntTemplateDupChk2(insert) > 0) {
			result.setResultCode("dup2");
		}else{
			systemMngMapper.insertEntTemplate(insert);
		}
		

		return result;
		
	}	
	
	/**
	 * 알림톡 업체-템플릿 연결 수정 
	 * @param AlimTalkTemplateInsert
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkSaveResult updateEntTemplate(AlimTalkEntTemplateInsert insert) throws Exception {
		AlimTalkSaveResult result = new AlimTalkSaveResult();
		Util.accessControlDatabase(insert, "gmtTime", false, null, "", null, "");
		systemMngMapper.updateEntTemplate(insert);
		return result;
		
	}
	
	
	/**
	 * 알림톡 업체 삭제
	 * @param DeleteStock
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkSaveResult deleteEnt(AlimTalkEntDelete delete) throws Exception {
		AlimTalkSaveResult result = new AlimTalkSaveResult();
		systemMngMapper.deleteEnt(delete);
		
		return result;
	}		
	
	/**
	 * 알림톡 템플릿 삭제
	 * @param DeleteStock
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkSaveResult deleteTemplate(AlimTalkTemplateDelete delete, HttpServletRequest request) throws Exception {
		AlimTalkSaveResult result = new AlimTalkSaveResult();
		systemMngMapper.deleteTemplate(delete);
		
		return result;
	}	
	
	/**
	 * 알림톡 업체 - 템플릿 삭제
	 * @param DeleteStock
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkSaveResult deleteEntTemplate(AlimTalkEntTemplateDelete delete) throws Exception {
		AlimTalkSaveResult result = new AlimTalkSaveResult();
		systemMngMapper.deleteEntTemplate(delete);
		
		return result;
	}		
	
	/**
	 * 알림톡 전송(공통-템플릿정보)
	 * @param AlimTalkTemplateInsert
	 * @return
	 * @throws Exception
	 */	
	public List<Map<String, Object>> sendAlimTalkEnt(AlimTalkSend send) throws Exception {
		
		Map<String, Object> smsMap = null;									// SMS 템플릿 정보 담기 위한 Map
		Map<String, Object> sendMap = new HashMap<String, Object>();		// 템플릿전송을 위해 파라메터 replace 처리 한 Map
		Map<String, Object> sendInfoMap = new HashMap<String, Object>();	// 세종 db에 Insert 하기 위한 Map
		
		List<Map<String, Object>> dataList = null;				// 하우스 정보 List
		//List<String> mblNoList = null;
		List<String> hblNoList = null;
		List<Map<String, Object>> sendList = new ArrayList<Map<String, Object>>();	// 세종 전송 목록 List
		
		String template = "";				// 알림톡 원본 메시지
		String reSendTemplate = "";			// 알림톡 실패시 재전송 원본 LMS 메시지
		String replaceTemplate = "";		// 알림톡 파라미터 적용한 메시지	
		String replaceReSendTemplate = "";	// 알림톡 실패시 파라미터 적용한 재전송 메시지
		String btn1Link = "";				// 알림톡 버튼1 URL 원본
		String btn2Link = "";				// 알림톡 버튼2 URL 원본
		String btn3Link = "";				// 알림톡 버튼3 URL 원본
		String btn4Link = "";				// 알림톡 버튼4 URL 원본
		String btn5Link = "";				// 알림톡 버튼5 URL 원본
		String replaceBtn1Link = "";		// 알림톡 버튼1 파라미터 적용한 URL
		String replaceBtn2Link = "";		// 알림톡 버튼2 파라미터 적용한 URL
		String replaceBtn3Link = "";		// 알림톡 버튼3 파라미터 적용한 URL
		String replaceBtn4Link = "";		// 알림톡 버튼4 파라미터 적용한 URL
		String replaceBtn5Link = "";		// 알림톡 버튼5 파라미터 적용한 URL		
		
		String reason = "";					// 어떤 조건에 의해 알림톡 전송이 되면 안되는 것들에 대한 이유
		
		int alreadySendCnt = 0;				// 이미 전송한 bl건수
		
		StringBuffer attach = new StringBuffer();	// 알림톡 버튼(JSON)
		StringBuffer alreadySendHBLNos = new StringBuffer();	// 이미 한번 전송된 하우스번호 내역
		
		
		try{
				
				
			
//			if(!"".equals(Util.null2str(send.getHblNoArr()))) {
//				hblNoList = Arrays.asList(String.valueOf(send.getHblNoArr()).split("\\,"));
//				send.setHblNoList(hblNoList);
//			}
//			
//			smsMap = systemMngMapper.sendAlimTalkEnt(send);
//	
//			
//			// 하우스번호, 마스터 번호 , 검색조건 필요  
//			
//			if(smsMap != null && !smsMap.isEmpty()) {
//				// 세종텔레콤 전송 파라미터 셋팅
//				sendMap.put("subid", smsMap.get("SUB_ID").toString());			// FWD ID(세종텔레콤에서 부여한 고객사 ID)
//				sendMap.put("cidnum", smsMap.get("CID_NO").toString());			// FWD CID(세종텔레콤에서 부여한 고객사 CID)
//				sendMap.put("msg_type", smsMap.get("MSG_TYPE").toString());
//				sendMap.put("subject", smsMap.get("SUBJECT").toString());
//				sendMap.put("k_template_code", smsMap.get("TEMPLATE_CODE").toString());
//				sendMap.put("k_next_type", smsMap.get("RESEND_MSG_TYPE").toString());
//				sendMap.put("sender_key", smsMap.get("SENDER_KEY").toString());
//				sendMap.put("callback", smsMap.get("TEL").toString());
//				sendMap.put("sendYn", "Y"); // 알림톡 데이터 체크 후 전송가능 여부 
//				// 템플릿 작성
//				template = smsMap.get("TEMPLATE").toString();
//				reSendTemplate = smsMap.get("RESEND_TEMPLATE").toString();
//				template = template.replace("#{업체명}", Util.null2str(smsMap.get("KAKAO_PLUS_NAME")));
//				template = template.replace("#{업체대표전화번호}", Util.null2str(smsMap.get("TEL")));
//				reSendTemplate = reSendTemplate.replace("#{업체명}", Util.null2str(smsMap.get("KAKAO_PLUS_NAME")));
//				reSendTemplate = reSendTemplate.replace("#{업체대표전화번호}", String.valueOf(Util.null2str(smsMap.get("TEL"))));
//				
//				btn1Link = Util.null2str(smsMap.get("BUTTON1_URL"));
//				btn2Link = Util.null2str(smsMap.get("BUTTON2_URL"));
//				btn3Link = Util.null2str(smsMap.get("BUTTON3_URL"));
//				btn4Link = Util.null2str(smsMap.get("BUTTON4_URL"));
//				btn5Link = Util.null2str(smsMap.get("BUTTON5_URL"));			
//				
//				/*=========================================================================================*/
//				
//				dataList = systemMngMapper.selectTemplateDataList(send); // 알림톡 템플릿에 들어갈 파라메터 조회
//				
//				for(Map<String, Object> dataMap : dataList) {
//					if("E12".equals(send.getTemplateType())) {//반송 안내
//						if(!"".equals(Util.null2str(dataMap.get("ReturnDeliverySmsReqDateTime")))){
//							sendMap.put("sendYn", "N");
//							reason = "이미 전송된 상태입니다.";
//							if(!"".equals(String.valueOf(alreadySendHBLNos))){
//								alreadySendHBLNos.append(", ").append(Util.null2str(dataMap.get("HBL_NO")));
//							} else {
//								alreadySendHBLNos.append(Util.null2str(dataMap.get("HBL_NO")));
//							}
//							alreadySendCnt++;
//						}
//					}else if("E13".equals(send.getTemplateType())) {//배송사변경 안내
//						if(!"".equals(Util.null2str(dataMap.get("ChangeDeliverySmsReqDateTime")))){
//							sendMap.put("sendYn", "N");
//							reason = "이미 전송된 상태입니다.";
//							if(!"".equals(String.valueOf(alreadySendHBLNos))){
//								alreadySendHBLNos.append(", ").append(Util.null2str(dataMap.get("HBL_NO")));
//							} else {
//								alreadySendHBLNos.append(Util.null2str(dataMap.get("HBL_NO")));
//							}
//							alreadySendCnt++;
//						}
//					}else if("E14".equals(send.getTemplateType())) {//통관보류 안내
//						if(!"".equals(Util.null2str(dataMap.get("ClearanceHoldSmsReqDateTime")))){
//							sendMap.put("sendYn", "N");
//							reason = "이미 전송된 상태입니다.";
//							if(!"".equals(String.valueOf(alreadySendHBLNos))){
//								alreadySendHBLNos.append(", ").append(Util.null2str(dataMap.get("HBL_NO")));
//							} else {
//								alreadySendHBLNos.append(Util.null2str(dataMap.get("HBL_NO")));
//							}
//							alreadySendCnt++;
//						}
//					}else {
//						if(!"".equals(Util.null2str(dataMap.get("RECV_DATE_TIME")))){
//							sendMap.put("sendYn", "N");
//							reason = "이미 전송된 상태입니다.";
//							if(!"".equals(String.valueOf(alreadySendHBLNos))){
//								alreadySendHBLNos.append(", ").append(Util.null2str(dataMap.get("HBL_NO")));
//							} else {
//								alreadySendHBLNos.append(Util.null2str(dataMap.get("HBL_NO")));
//							}
//							alreadySendCnt++;
//						}
//					}
//				}	
//				
//				if(alreadySendHBLNos.length() > 0 && "N".equals(send.getAlreadySendYn())) {
//					sendMap.put("totalCnt", dataList.size());		// 전체건수
//					sendMap.put("result", "D");
//					sendMap.put("alreadySendCnt", alreadySendCnt);
//					sendMap.put("alreadySendHBLNos", String.valueOf(alreadySendHBLNos));
//					sendList.add(sendInfoMap);
//					return sendList;
//					
//				}else {
//					reason = "";
//					for(Map<String, Object> dataMap : dataList) {
//						
//						attach.setLength(0);
//						
//						if(!"".equals(Util.null2str(dataMap.get("ConsigneeMobile")))) {
//							dataMap.put("ConsigneeMobile", String.valueOf(dataMap.get("ConsigneeMobile")).replaceAll("^8201", "821"));
//						}
//	
//						/*
//						// 카카오톡전송용 계좌은행,계좌번호,계좌명 세팅
//						template = template.replace("#{계좌은행}", BankName);
//						template = template.replace("#{계좌번호}", AccountNo);
//						template = template.replace("#{계좌명}", DepoSitorName);
//						// 카카오톡전송 불가시 재전송용 계좌은행,계좌번호,계좌명 세팅
//						reSendTemplate = reSendTemplate.replace("#{계좌은행}", BankName);
//						reSendTemplate = reSendTemplate.replace("#{계좌번호}", AccountNo);
//						reSendTemplate = reSendTemplate.replace("#{계좌명}", DepoSitorName);
//						*/
//						
//						
//						replaceTemplate = template;
//						replaceTemplate = replaceTemplate.replace("#{송하인명}", Util.null2str(dataMap.get("SHIPPER_NAME")));
//						replaceTemplate = replaceTemplate.replace("#{수하인명}", Util.null2str(dataMap.get("CONSIGNEE_NAME")));
//						replaceTemplate = replaceTemplate.replace("#{수하인주소}", Util.null2str(dataMap.get("CONSIGNEE_ADDRESS")));
//						replaceTemplate = replaceTemplate.replace("#{수하인전화번호}", Util.null2str(dataMap.get("CONSIGNEE_HP")));
//						replaceTemplate = replaceTemplate.replace("#{송장번호}", Util.null2str(dataMap.get("HBL_NO")));
//						replaceTemplate = replaceTemplate.replace("#{변경송장번호}", Util.null2str(dataMap.get("DELIVERY_NO_CHANGED")));
//						replaceTemplate = replaceTemplate.replace("#{출항편명}", Util.null2str(dataMap.get("VESSEL_FLIGHT")));
//						replaceTemplate = replaceTemplate.replace("#{출항일시}", Util.null2str(dataMap.get("OB_DATE")));
//						replaceTemplate = replaceTemplate.replace("#{출발항}", Util.null2str(dataMap.get("ORIG_CODE")));
//						replaceTemplate = replaceTemplate.replace("#{도착항}", Util.null2str(dataMap.get("DEST_CODE")));
//						replaceTemplate = replaceTemplate.replace("#{도착일}", Util.null2str(dataMap.get("AR_DATE")).split(" ")[0]);
//						replaceTemplate = replaceTemplate.replace("#{입항일시}", Util.null2str(dataMap.get("AR_DATE")));
//						replaceTemplate = replaceTemplate.replace("#{입항일}", Util.null2str(dataMap.get("AR_DATE")).split(" ")[0]);
//						replaceTemplate = replaceTemplate.replace("#{반입창고}", Util.null2str(dataMap.get("WHName")));
//						replaceTemplate = replaceTemplate.replace("#{반입일}", Util.null2str(dataMap.get("CARRYOUT_DATE")).split(" ")[0]);
//						replaceTemplate = replaceTemplate.replace("#{반입일시}", Util.null2str(dataMap.get("CARRYOUT_DATE")));
//						replaceTemplate = replaceTemplate.replace("#{통관일시}", Util.null2str(dataMap.get("CLEARANCE_RECEIPT_DATE")));
//						replaceTemplate = replaceTemplate.replace("#{품명}", Util.null2str(dataMap.get("COMMODITY_ITEM")));
//						replaceTemplate = replaceTemplate.replace("#{배송접수일시}", Util.null2str(dataMap.get("DELIVERY_RECV_DATE")));
//						replaceTemplate = replaceTemplate.replace("#{택배사}", Util.null2str(dataMap.get("TR_NAME")));
//						replaceTemplate = replaceTemplate.replace("#{변경택배사}", Util.null2str(dataMap.get("DELIVERY_NAME_CHANGED")));
//						replaceTemplate = replaceTemplate.replace("#{배송일시}", Util.null2str(dataMap.get("DELIVERY_DATE")));
//						replaceTemplate = replaceTemplate.replace("#{출발국가명}", Util.null2str(dataMap.get("ORIG_COUNTRY")));
//						//replaceTemplate = replaceTemplate.replace("#{관세}", Util.null2str(dataMap.get("Duty_Amount")));
//						//replaceTemplate = replaceTemplate.replace("#{부가세}", Util.null2str(dataMap.get("Vat_Amount")));
//						//replaceTemplate = replaceTemplate.replace("#{기타세금}", Util.null2str(dataMap.get("ETC_Amount")));
//						//replaceTemplate = replaceTemplate.replace("#{통관료}", Util.null2str(dataMap.get("Customs_Amount")));
//						//replaceTemplate = replaceTemplate.replace("#{창고료}", Util.null2str(dataMap.get("Wh_Amount")));
//						//replaceTemplate = replaceTemplate.replace("#{총합계}", Util.null2str(dataMap.get("Total_Amount")));
//						replaceTemplate = replaceTemplate.replace("#{관세사}", Util.null2str(dataMap.get("CB_NAME")));
//						
//						
//						if("".equals(Util.null2str(dataMap.get("CUSTOMS_ID")))){
//							replaceTemplate = replaceTemplate.replace("#{사유1}", "누락");
//							replaceTemplate = replaceTemplate.replace("#{사유2}", "미기재");
//						}else {
//							if("N".equals(Util.null2str(dataMap.get("CUSTOMS_ID_VERIFY"))) 
//								|| "E".equals(Util.null2str(dataMap.get("CUSTOMS_ID_VERIFY")))){
//								replaceTemplate = replaceTemplate.replace("#{사유1}", "오류");
//								replaceTemplate = replaceTemplate.replace("#{사유2}", "오류");
//							}
//						}
//						
//						if("".equals(Util.null2str(dataMap.get("IREEName")))){
//							replaceTemplate = replaceTemplate.replace("#{보류사유}", Util.null2str(dataMap.get("HoldNotice")));
//						}else {
//							replaceTemplate = replaceTemplate.replace("#{보류사유}", Util.null2str(dataMap.get("IREEName")));
//						}
//						
//						//착불인 경우
//						if("02".equals(Util.null2str(dataMap.get("DELIVERY_PAYMENT_TYPE")))){
//							replaceTemplate = replaceTemplate.replace("#{추가비용안내내용}", "추가 배송비용이 착불로 발생됨을 안내드립니다.");
//							replaceTemplate = replaceTemplate.replace("#{추가비용유무}", "착불배송비용 : "+ Util.null2str(dataMap.get("DELIVERY_FEE")));
//						}else {
//							replaceTemplate = replaceTemplate.replace("#{추가비용안내내용}", "추가 배송비용은 발생되지않습니다.");
//							replaceTemplate = replaceTemplate.replace("#{추가비용유무}", "");
//						}
//						
//						replaceReSendTemplate = reSendTemplate;
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{송하인명}", Util.null2str(dataMap.get("SHIPPER_NAME")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{수하인명}", Util.null2str(dataMap.get("CONSIGNEE_NAME")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{수하인주소}", Util.null2str(dataMap.get("CONSIGNEE_ADDRESS")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{수하인전화번호}", Util.null2str(dataMap.get("CONSIGNEE_TEL")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{송장번호}", Util.null2str(dataMap.get("HBL_NO")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{변경송장번호}", Util.null2str(dataMap.get("DELIVERY_NO_CHANGED")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{출항편명}", Util.null2str(dataMap.get("VESSEL_FLIGHT")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{출항일시}", Util.null2str(dataMap.get("OB_DATE")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{출발항}", Util.null2str(dataMap.get("ORIG_CODE")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{도착항}", Util.null2str(dataMap.get("DEST_CODE")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{도착일}", Util.null2str(dataMap.get("AR_DATE")).split(" ")[0]);
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{입항일시}", Util.null2str(dataMap.get("AR_DATE")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{입항일}", Util.null2str(dataMap.get("AR_DATE")).split(" ")[0]);
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{반입창고}", Util.null2str(dataMap.get("WHName")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{반입일}", Util.null2str(dataMap.get("CARRYIN_DATE")).split(" ")[0]);
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{반입일시}", Util.null2str(dataMap.get("CARRYIN_DATE")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{통관일시}", Util.null2str(dataMap.get("CLEARANCE_RECEIPT_DATE")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{품명}", Util.null2str(dataMap.get("COMMODITY_ITEM")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{배송접수일시}", Util.null2str(dataMap.get("DELIVERY_RECV_DATE")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{택배사}", Util.null2str(dataMap.get("DeliveryName")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{변경택배사}", Util.null2str(dataMap.get("DELIVERY_NAME_CHANGED")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{배송일시}", Util.null2str(dataMap.get("DELIVERY_DATE")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{출발국가명}", Util.null2str(dataMap.get("ORIG_COUNTRY")));
//						//replaceReSendTemplate = replaceReSendTemplate.replace("#{관세}", Util.null2str(dataMap.get("Duty_Amount")));
//						//replaceReSendTemplate = replaceReSendTemplate.replace("#{부가세}", Util.null2str(dataMap.get("Vat_Amount")));
//						//replaceReSendTemplate = replaceReSendTemplate.replace("#{기타세금}", Util.null2str(dataMap.get("ETC_Amount")));
//						//replaceReSendTemplate = replaceReSendTemplate.replace("#{통관료}", Util.null2str(dataMap.get("Customs_Amount")));
//						//replaceReSendTemplate = replaceReSendTemplate.replace("#{창고료}", Util.null2str(dataMap.get("Wh_Amount")));
//						//replaceReSendTemplate = replaceReSendTemplate.replace("#{총합계}", Util.null2str(dataMap.get("Total_Amount")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{ENT_CODE}", Util.null2str(send.getEntCode()));
//						//replaceReSendTemplate = replaceReSendTemplate.replace("#{BOUND}", Util.null2str(map.get("BOUND")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{REFNO}", Util.null2str(dataMap.get("OD_JOB_NO")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{HBLNO}", Util.null2str(dataMap.get("HBL_NO")));
//						//replaceReSendTemplate = replaceReSendTemplate.replace("#{COMPANYID}", Util.null2str(map.get("CompanyID")));
//						replaceReSendTemplate = replaceReSendTemplate.replace("#{관세사}", Util.null2str(dataMap.get("CB_NAME")));
//						
//						if("".equals(Util.null2str(dataMap.get("CUSTOMS_ID")))){
//							replaceReSendTemplate = replaceReSendTemplate.replace("#{사유1}", "누락");
//							replaceReSendTemplate = replaceReSendTemplate.replace("#{사유2}", "미기재");
//						}else {
//							if("N".equals(Util.null2str(dataMap.get("CUSTOMS_ID_VERIFY"))) 
//								|| "E".equals(Util.null2str(dataMap.get("CUSTOMS_ID_VERIFY")))){
//								replaceReSendTemplate = replaceReSendTemplate.replace("#{사유1}", "오류");
//								replaceReSendTemplate = replaceReSendTemplate.replace("#{사유2}", "오류");
//							}
//						}
//						
//						if("".equals(Util.null2str(dataMap.get("IREEName")))){
//							replaceReSendTemplate = replaceReSendTemplate.replace("#{보류사유}", Util.null2str(dataMap.get("HoldNotice")));
//						}else {
//							replaceReSendTemplate = replaceReSendTemplate.replace("#{보류사유}", Util.null2str(dataMap.get("IREEName")));
//						}
//						
//						//착불인 경우
//						if("02".equals(Util.null2str(dataMap.get("DELIVERY_PAYMENT_TYPE")))){
//							replaceReSendTemplate = replaceReSendTemplate.replace("#{추가비용안내내용}", "추가 배송비용이 착불로 발생됨을 안내드립니다.");
//							replaceReSendTemplate = replaceReSendTemplate.replace("#{추가비용유무}", "착불배송비용 : "+ Util.null2str(dataMap.get("DELIVERY_FEE")));
//						}else {
//							replaceReSendTemplate = replaceReSendTemplate.replace("#{추가비용안내내용}", "추가 배송비용은 발생되지않습니다.");
//							replaceReSendTemplate = replaceReSendTemplate.replace("#{추가비용유무}", "");
//						}
//						
//						if(!"".equals(btn1Link)){
//							replaceBtn1Link = btn1Link;
//							replaceBtn1Link = replaceBtn1Link.replace("#{ENT_CODE}", Util.null2str(send.getEntCode()));
//							//replaceBtn1Link = replaceBtn1Link.replace("#{BOUND}", Util.null2str(map.get("BOUND")));
//							replaceBtn1Link = replaceBtn1Link.replace("#{REFNO}", Util.null2str(dataMap.get("OD_JOB_NO")));
//							replaceBtn1Link = replaceBtn1Link.replace("#{HBLNO}", Util.null2str(dataMap.get("HBL_NO")));
//							//replaceBtn1Link = replaceBtn1Link.replace("#{COMPANYID}", Util.null2str(map.get("CompanyID")));
//						}
//						if(!"".equals(btn2Link)){
//							replaceBtn2Link = btn2Link;
//							replaceBtn2Link = replaceBtn2Link.replace("#{ENT_CODE}", Util.null2str(send.getEntCode()));
//							//replaceBtn2Link = replaceBtn2Link.replace("#{BOUND}", Util.null2str(map.get("BOUND")));
//							replaceBtn2Link = replaceBtn2Link.replace("#{REFNO}", Util.null2str(dataMap.get("OD_JOB_NO")));
//							replaceBtn2Link = replaceBtn2Link.replace("#{HBLNO}", Util.null2str(dataMap.get("HBL_NO")));
//							//replaceBtn2Link = replaceBtn2Link.replace("#{COMPANYID}", Util.null2str(map.get("CompanyID")));
//						}
//						if(!"".equals(btn3Link)){
//							replaceBtn3Link = btn3Link;
//							replaceBtn3Link = replaceBtn3Link.replace("#{ENT_CODE}", Util.null2str(send.getEntCode()));
//							//replaceBtn3Link = replaceBtn3Link.replace("#{BOUND}", Util.null2str(map.get("BOUND")));
//							replaceBtn3Link = replaceBtn3Link.replace("#{REFNO}", Util.null2str(dataMap.get("OD_JOB_NO")));
//							replaceBtn3Link = replaceBtn3Link.replace("#{HBLNO}", Util.null2str(dataMap.get("HBL_NO")));
//							//replaceBtn3Link = replaceBtn3Link.replace("#{COMPANYID}", Util.null2str(map.get("CompanyID")));
//						}
//						if(!"".equals(btn4Link)){
//							replaceBtn4Link = btn4Link;
//							replaceBtn4Link = replaceBtn4Link.replace("#{ENT_CODE}", Util.null2str(send.getEntCode()));
//							//replaceBtn4Link = replaceBtn4Link.replace("#{BOUND}", Util.null2str(map.get("BOUND")));
//							replaceBtn4Link = replaceBtn4Link.replace("#{REFNO}", Util.null2str(dataMap.get("OD_JOB_NO")));
//							replaceBtn4Link = replaceBtn4Link.replace("#{HBLNO}", Util.null2str(dataMap.get("HBL_NO")));
//							//replaceBtn4Link = replaceBtn4Link.replace("#{COMPANYID}", Util.null2str(map.get("CompanyID")));
//						}
//						if(!"".equals(btn5Link)){
//							replaceBtn5Link = btn5Link;
//							replaceBtn5Link = replaceBtn5Link.replace("#{ENT_CODE}", Util.null2str(send.getEntCode()));
//							//replaceBtn5Link = replaceBtn5Link.replace("#{BOUND}", Util.null2str(map.get("BOUND")));
//							replaceBtn5Link = replaceBtn5Link.replace("#{REFNO}", Util.null2str(dataMap.get("OD_JOB_NO")));
//							replaceBtn5Link = replaceBtn5Link.replace("#{HBLNO}", Util.null2str(dataMap.get("HBL_NO")));
//							//replaceBtn5Link = replaceBtn5Link.replace("#{COMPANYID}", Util.null2str(map.get("CompanyID")));
//						}
//						
//						// 20210428  버튼 URL로 유효성 검사 -> URL이 없는 상담같은 버튼이 전송되지 않는 문제가 생김 , 버튼 타입으로 유효성 검사하도록 변경 
//						if(!"".equals(Util.null2str(smsMap.get("BUTTON1_TYPE")))){
//							attach.append("{\"attachment\":{\"button\":[");
//							
//							attach.append("{\"name\":\"" + smsMap.get("BUTTON1_NAME").toString() + "\",\"type\":\"" + smsMap.get("BUTTON1_TYPE").toString() + "\",\"url_mobile\":\"" + replaceBtn1Link + "\",\"url_pc\":\"" + replaceBtn1Link + "\"}");
//							if(!"".equals(Util.null2str(smsMap.get("BUTTON2_TYPE")))){
//								attach.append(",{\"name\":\"" + smsMap.get("BUTTON2_NAME").toString() + "\",\"type\":\"" + smsMap.get("BUTTON2_TYPE").toString() + "\",\"url_mobile\":\"" + replaceBtn2Link + "\",\"url_pc\":\"" + replaceBtn2Link + "\"}");
//							}
//							if(!"".equals(Util.null2str(smsMap.get("BUTTON3_TYPE")))){
//								attach.append(",{\"name\":\"" + smsMap.get("BUTTON3_NAME").toString() + "\",\"type\":\"" + smsMap.get("BUTTON3_TYPE").toString() + "\",\"url_mobile\":\"" + replaceBtn3Link + "\",\"url_pc\":\"" + replaceBtn3Link + "\"}");
//							}
//							if(!"".equals(Util.null2str(smsMap.get("BUTTON4_TYPE")))){
//								attach.append(",{\"name\":\"" + smsMap.get("BUTTON4_NAME").toString() + "\",\"type\":\"" + smsMap.get("BUTTON4_TYPE").toString() + "\",\"url_mobile\":\"" + replaceBtn4Link + "\",\"url_pc\":\"" + replaceBtn4Link + "\"}");
//							}
//							if(!"".equals(Util.null2str(smsMap.get("BUTTON5_TYPE")))){
//								attach.append(",{\"name\":\"" + smsMap.get("BUTTON5_NAME").toString() + "\",\"type\":\"" + smsMap.get("BUTTON5_TYPE").toString() + "\",\"url_mobile\":\"" + replaceBtn5Link + "\",\"url_pc\":\"" + replaceBtn5Link + "\"}");
//							}
//							attach.append("]}}");
//							
//							sendMap.put("k_attach", attach.toString()); // 버튼 JSON
//						}
//	
//						
//						if("E05".equals(send.getTemplateType())){
//							sendMap.put("dstaddr", Util.null2str(dataMap.get("SHIPPER_TEL"))); // 송하인 전화번호
//						}else if("E15".equals(send.getTemplateType())){
//							sendMap.put("dstaddr", Util.null2str(dataMap.get("FromMobile"))); // 반송인 전화번호
//						}else {
//							sendMap.put("dstaddr", Util.null2str(dataMap.get("CONSIGNEE_HP"))); // 수하인 전화번호
//						}
//						
//						if("6".equals(sendMap.get("msg_type").toString()) || "7".equals(sendMap.get("msg_type").toString())) {
//							sendMap.put("text", replaceTemplate);
//							sendMap.put("text2", replaceReSendTemplate);
//						}else {
//							sendMap.put("text", replaceReSendTemplate);
//						}
//						
//						// YHTXX 테스트용  dstaddr에 핸드폰번호 기입 후 테스트
//						if("YHTXX".equals(send.getEntCode())){ 
//							sendMap.put("dstaddr", "01057300189");
//							sendMap.put("callback", "0262778092");
//						}
//						
//						sendMap.put("HBL_NO", dataMap.get("HBL_NO")); // 하우스번호
//						sendMap.put("OD_JOB_NO", dataMap.get("OD_JOB_NO")); // 관리번호
//						sendMap.put("TEMPLATE_TYPE", send.getTemplateType()); // 템플릿 타입
//						sendMap.put("BOUND", dataMap.get("BOUND")); //바운드
//
//						
//						//템플릿별 체크
//						
//	
//						sendInfoMap = new HashMap<String, Object>();
//						sendInfoMap.putAll(sendMap);
//						sendList.add(sendInfoMap);
//	
//						
//					} // for end
//				
//				}
//				
//			}else{
//				sendMap.put("result", "F");	// 등록된 템플릿이 없음.
//				sendList.add(sendMap);
//			}
	
			return sendList;
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			return sendList;
		}
		
	}
	

	/**
	 * 알림톡 전송(세종)
	 * @param AlimTalkTemplateInsert
	 * @return
	 * @throws Exception
	 */	
	public List<Map<String, Object>> sendAlimTalkSejong(List<Map<String, Object>> sendList) throws Exception {
		
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		int mseq = 0;
		int sendCnt = 0;					// 세종 인터페이스 전송횟수
		int errorACnt = 0;					// 세종 인터페이스 errMsgA에 해당되는 건수
		int errorBCnt = 0;					// 세종 인터페이스 errMsgB에 해당되는 건수
		int errorCCnt = 0;					// 세종 인터페이스 errMsgC에 해당되는 건수
		

		StringBuffer notSendHBLNos = new StringBuffer();	// 어떤 조건에 의해 알림톡 전송이 되면 안되는 것들에 대한 하우스번호 내역
		StringBuffer errorAHBLNos = new StringBuffer();	// 세종 인터페이스시 errMsgA에 해당되는 하우스번호 내역
		StringBuffer errorBHBLNos = new StringBuffer();	// 세종 인터페이스시 errMsgB에 해당되는 하우스번호 내역
		StringBuffer errorCHBLNos = new StringBuffer();	// 세종 인터페이스시 errMsgC에 해당되는 하우스번호 내역
		
		try {
				
			for(Map<String, Object> sendMap : sendList) {
				
				// 초기화 
				mseq = 0;
				sendCnt = 0;
				errorACnt = 0;
				errorBCnt = 0;	
				errorCCnt = 0;					
				resultMap = new HashMap<String, Object>();
				
				if("Y".equals(Util.null2str(sendMap.get("sendYn")))){

					if(!"".equals(Util.null2str(sendMap.get("dstaddr")))){
						if(!"".equals(Util.null2str(sendMap.get("callback")))){
							systemMngMapper.sendSMS(sendMap);
							mseq = (int)sendMap.get("mseq");
							
							
							if(mseq > 0) { // 전송 성공
								resultMap.put("mseq",mseq);   // 세종에 INSERT한 mseq한 값 
								resultMap.put("HBL_NO",sendMap.get("HBL_NO")); // 하우스번호 
								resultMap.put("OD_JOB_NO",sendMap.get("OD_JOB_NO")); // 관리번호
								resultMap.put("TEMPLATE_TYPE",sendMap.get("TEMPLATE_TYPE"));  // 템플릿 타입
								resultMap.put("TEMPLATE_CODE",sendMap.get("k_template_code")); // 세종에 등록된 템플릿 코드 
								
								sendCnt++;
								
							}else { // 전송 실패 
								resultMap.put("errorMsgA", "세종텔레콤 DB insert 에러 HBL No");
								resultMap.put("errorAHBLNo", Util.null2str(sendMap.get("HBL_NO")));
							}
						}else{
							resultMap.put("errorMsgB", "송신자 번호가 없는 HBL No");
							resultMap.put("errorBHBLNo", Util.null2str(sendMap.get("HBL_NO")));
						}
					}else{
						resultMap.put("errorMsgC", "수신자 번호가 없는 HBL No");
						resultMap.put("errorCHBLNo", Util.null2str(sendMap.get("HBL_NO")));
					}
				}else{
				} 

				if(sendCnt > 0){
					resultMap.put("result", "Y");
				}else{
					resultMap.put("result", "N");
				}
				
				resultList.add(resultMap);	
				
				
			}
			
			
			
			return resultList;
			
			
			
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			return resultList;
		}
		
	}	
		
	/**
	 * 알림톡 전송 결과값 업데이트
	 * @param AlimTalkTemplateInsert
	 * @return
	 * @throws Exception
	 */	
	public AlimTalkSaveResult sendAlimTalkResult(Map<String, Object> logMap) throws Exception {
		AlimTalkSaveResult result = new AlimTalkSaveResult();
		
		//systemMngMapper.updateSmsDateTime(logMap);

		
		return result;
	}	
	
	/**
	 * ZipCode List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ZipCodeList selectZipCodeList(SearchZipCodeList search) throws Exception {
		ZipCodeList zipcodeList = new ZipCodeList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
//		List<ZipCode> list  = systemMngMapper.selectZipCodeList(search);
//		zipcodeList.setZipcodeList(list);
//		if(list.size() > 0) {
//			zipcodeList.setTotal(list.get(0).getTotal());
//		}else {
//			zipcodeList.setTotal(0);
//		}
		return zipcodeList;
	}
	
	/**
	 * 전자상거래 ZipCode 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ZipCode selectZipCode(SearchZipCode search) throws Exception {
		return systemMngMapper.selectZipCode(search);
	}
	

	/**
	 * AddressZipCode List
	 * 
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public AddressZipCodeList selectAddressZipCodeList(SearchAddressZipCodeList search) throws Exception {

		AddressZipCodeList addressZipCodeList = new AddressZipCodeList();

		if (search.getPage() != 0 && search.getPageSize() != 0) {
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if (Util.null2str(search.getChangeField()).equals("Y")) {
			Util.camelCaseToUnderscore(search);
		}

		Util.accessControlDatabase(search, null, true, "srchDateGbn", "REGISTER_DATE_TIME|UPDATE_DATE_TIME", "srchType", "CENTRAL_STATION_NO|CENTRAL_STATION|DELIVERY_NO|DELIVERY|DELIVERY_TEAM_NO|DELIVERY_PEOPLE_NO");
		List<AddressZipCode> list = systemMngMapper.selectAddressZipCodeList(search);
		addressZipCodeList.setAddressZipCodeList(list);
		if (list.size() > 0) {
			addressZipCodeList.setTotal(list.get(0).getTotal());
		} else {
			addressZipCodeList.setTotal(0);
		}
		
		return addressZipCodeList;
	}
	
	/**
	 * AddressZipCode ModifyDate
	 * 
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public String selectAddressZipCodeModifyDate() throws Exception {

		String modifyDate = systemMngMapper.selectAddressZipCodeModifyDate();
		return StringEscapeUtils.escapeHtml4(modifyDate); 
	}
	

	public OrderSaveResult odBackup(OdBackup odBackup) throws Exception {
		
		OrderSaveResult result = new OrderSaveResult();
		Map<String,Object> dataMap = new HashMap<>();
		
		if("YHTXX".equals(odBackup.getEntCode().toUpperCase())) dataMap.put("db_name", "exps");
		else dataMap.put("db_name", odBackup.getEntCode());
		
//		if("srch".equals(odBackup.getBackupType())) {
//			odBackup.setOdJobNoList(systemMngMapper.selectBackupOrderList(odBackup.getSearchOrderList()));
//		}
		
		List<String> odJobNoList = odBackup.getOdJobNoList();
		
		if(odJobNoList.size() == 0) {
			result.setResultCode("empty");
			return result;
		}
		
		try {
			for(int i = 0; i < odJobNoList.size(); i+=500){
				if(i + 500 < odJobNoList.size()) {
					dataMap.put("dataList",odJobNoList.subList(i, i + 500));
				}else{
					dataMap.put("dataList",odJobNoList.subList(i, odJobNoList.size()));
				}
//				systemMngMapper.insertBackupDB(dataMap);
//				systemMngMapper.deleteOrigDB(dataMap);
			}
		}catch (Exception e) {
			//log.error(Util.getPrintStacTraceString(e));
			result.setResultCode(e.getMessage());
			return result;
		}
		
		result.setResultCode("success");
		
		return result;
	}

	/**
	 * 월별 사용자및 업체 건수 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MonthlyCountList selectMonthlyUserCountList(SearchInterfaceKeyList search) throws Exception {
		MonthlyCountList monthlyCountList = new MonthlyCountList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
	
		Util.accessControlDatabase(search, null, true, null, "", null, "");
		List<MonthlyCount> list  = systemMngMapper.selectMonthlyUserCountList(search);
		monthlyCountList.setMonthlyCountList(list);
		if(list.size() > 0) {
			monthlyCountList.setTotal(list.get(0).getTotal());
		}else {
			monthlyCountList.setTotal(0);
		}
		return monthlyCountList;
	}
	/**
	 * 전자상거래 DeliveryInterface 조회(Entry) - API
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public DeliveryInterface selectDeliveryInterfaceApi(SearchDeliveryInterface search) throws Exception {
		DeliveryInterface deliveryInterface = new DeliveryInterface();
		deliveryInterface.setGridData(systemMngMapper.selectDeliveryInterfaceApi(search));
		return deliveryInterface;
	}
	/** 그리드 그룹 세팅 삭제 */
	public void deleteGridConfigGroup(String entCode, String userId, String gridId, String groupCode) throws Exception {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("ENT_CODE", entCode);
		param.put("USER_ID", userId);
		param.put("GRID_ID", gridId);
		param.put("GROUP_CODE", groupCode);
		systemMngMapper.deleteGridConfig(param);
	}
	/**
	 * 그리드 환경설정(그룹별) 저장 - 기존 그룹 데이터 삭제 후 재저장
	 */
	public void saveGridConfig(String entCode, String userId, String gmtTime, String gridId, String groupCode,
							   List<String> titleList, List<String> hideList, List<String> lockList, List<Object> widthList) throws Exception {

		Map<String, Object> delParam = new HashMap<String, Object>();
		delParam.put("ENT_CODE", entCode);
		delParam.put("USER_ID", userId);
		delParam.put("GRID_ID", gridId);
		delParam.put("GROUP_CODE", groupCode);
		systemMngMapper.deleteGridConfig(delParam);

		List<Map<String, Object>> gridConfigList = new ArrayList<Map<String, Object>>();
		if (titleList != null) {
			for (int i = 0; i < titleList.size(); i++) {
				String title = Util.null2str(titleList.get(i));
				if ("".equals(title)) continue;

				Map<String, Object> gridConfig = new HashMap<String, Object>();
				gridConfig.put("ENT_CODE", entCode);
				gridConfig.put("USER_ID", userId);
				gridConfig.put("GRID_ID", gridId);
				gridConfig.put("GROUP_CODE", groupCode);
				gridConfig.put("COL_NAME", title);
				gridConfig.put("gmtTime", "".equals(Util.null2str(gmtTime)) ? "0" : gmtTime);

				String hide  = (hideList  != null && i < hideList.size())  ? Util.null2str(hideList.get(i))  : "";
				String lock  = (lockList  != null && i < lockList.size())  ? Util.null2str(lockList.get(i))  : "";
				String width = (widthList != null && i < widthList.size()) ? Util.null2str(widthList.get(i)) : "";

				gridConfig.put("VIEW_YN", "true".equals(hide) ? "N" : "Y");
				gridConfig.put("LOCK_YN", "true".equals(lock) ? "Y" : "N");
				gridConfig.put("DISPLAY_INDEX", new BigDecimal(i));

				if (width.indexOf(";") > -1) width = width.replaceAll(";", "");
				if ("".equals(width))              gridConfig.put("COL_WIDTH", "100px");
				else if (width.indexOf("px") > -1) gridConfig.put("COL_WIDTH", width);
				else                               gridConfig.put("COL_WIDTH", width + "px");

				gridConfigList.add(gridConfig);
			}
		}

		if (gridConfigList.size() > 0) {
			systemMngMapper.insertGridConfig(gridConfigList);
		}
	}

	/** 그리드 환경설정(그룹별) 조회 */
	public List<Map<String, Object>> getGridConfigList(String entCode, String userId, String gridId, String groupCode) throws Exception {
		Map<String, Object> search = new HashMap<String, Object>();
		search.put("entCode", entCode);
		search.put("userId", userId);
		search.put("gridId", gridId);
		search.put("groupCode", groupCode);
		return systemMngMapper.selectGridConfigList(search);
	}

	/** 그리드 그룹 목록 조회 */
	public List<Map<String, Object>> getGridConfigGroupList(String entCode, String userId, String gridId) throws Exception {
		Map<String, Object> search = new HashMap<String, Object>();
		search.put("entCode", entCode);
		search.put("userId", userId);
		search.put("gridId", gridId);
		return systemMngMapper.selectGridConfigGroupList(search);
	}
}