package org.yht.exps.express.mapper.systemMng;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.systemMng.AddressZipCode;
import org.yht.exps.express.model.systemMng.Alarm;
import org.yht.exps.express.model.systemMng.AlarmGroup;
import org.yht.exps.express.model.systemMng.DeliveryInterface;
import org.yht.exps.express.model.systemMng.InterfaceKey;
import org.yht.exps.express.model.systemMng.InterfaceKeyCount;
import org.yht.exps.express.model.systemMng.MonthlyCount;
import org.yht.exps.express.model.systemMng.ZipCode;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEnt;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntDelete;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntInsert;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplate;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplateDelete;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplateInsert;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkSend;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplate;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplateDelete;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplateInsert;
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

@Mapper
public interface SystemMngMapper {
	List<Alarm> selectAlarm(Map<String, Object> map);
	void deleteAlarm(Map<String, Object> map);
	
	/** Interface Key 관리 */
	List<InterfaceKey> selectInterfaceKeyList(SearchInterfaceKeyList search);
	List<InterfaceKeyCount> selectInterfaceKeyCountList(SearchInterfaceKeyCountList search);
	InterfaceKey selectInterfaceKey(SearchInterfaceKey search);
	Map<String, Object> selectInterfaceKeyDupChk(InterfaceKey InterfaceKey);
	void insertInterfaceKey(InterfaceKey InterfaceKey);
	void updateInterfaceKey(InterfaceKey InterfaceKey);
	void deleteInterfaceKey(Map<String, Object> search);

	/** 배송사 Interface 관리 */
	List<DeliveryInterface> selectDeliveryInterfaceList(SearchDeliveryInterfaceList search);
	DeliveryInterface selectDeliveryInterface(SearchDeliveryInterface search);
//	List<Map<String, Object>> selectDeliveryInterfaceApi(SearchDeliveryInterface search);
	Map<String, Object> selectDeliveryInterfaceCheck(Map<String, Object> search);
	void insertDeliveryInterface(DeliveryInterface insert);
	void updateDeliveryInterface(DeliveryInterface update);
	void deleteDeliveryInterface(Map<String, Object> delete);
	
	List<MonthlyCount> selectMonthlyCountList(SearchInterfaceKeyList search);
	void deleteAlarmGroup(SearchAlarmGroup search);
	void deleteAlarmGroupUser(SearchAlarmGroup search);
	void insertAlarmGroup(SearchAlarmGroup search);
	void insertAlarmGroupUser(SearchAlarmGroup search);
	void updateAlarmGroupUser(SearchAlarmGroup search);
	Integer selectAlarmGroupUserSaveChk(SearchAlarmGroup search);
	List<AlarmGroup> selectAlarmGroupList(SearchAlarmGroup search);
	List<AlarmGroup> selectAlarmGroupUserList(SearchAlarmGroup search);
	
	/** 알림톡 */
	List<AlimTalkEnt> selectAlimTalkEntList(SearchAlimTalkEnt search);
	List<AlimTalkEntTemplate> selectAlimTalkEntTemplateList(SearchAlimTalkEnt search);
	void updateEnt(AlimTalkEntInsert insert);
	void insertTemplate(AlimTalkTemplateInsert insert);
	void updateTemplate(AlimTalkTemplateInsert insert);
	List<AlimTalkTemplate> selectAlimTalkTemplateList(SearchAlimTalkTemplate search);
	List<AlimTalkTemplate> selectAlimTalkTemplateList(SearchAlimTalkEnt search);
	void insertEntTemplate(AlimTalkEntTemplateInsert insert);
	int selectEntTemplateDupChk(AlimTalkEntTemplateInsert insert);
	int selectEntTemplateDupChk2(AlimTalkEntTemplateInsert insert);
	void updateEntTemplate(AlimTalkEntTemplateInsert insert);
	void deleteEnt(AlimTalkEntDelete delete);
	void deleteTemplate(AlimTalkTemplateDelete delete);
	void deleteEntTemplate(AlimTalkEntTemplateDelete delete);
	Map<String, Object> sendAlimTalkEnt(AlimTalkSend send);
	List<Map<String, Object>> selectTemplateDataList(AlimTalkSend send);
	int sendSMS(Map<String, Object> sendMap);	
	void updateSmsDateTime(Map<String, Object> logMap);
	
	/** AddressZipCode */
	List<AddressZipCode> selectAddressZipCodeList(SearchAddressZipCodeList search);
	void insertAddressZipCode(Map<String, Object> map);
	void updateAddressZipCode(Map<String, Object> map);
	void deleteAddressZipCode(Map<String, Object> map);
	String selectAddressZipCodeModifyDate();
	
	/** ZipCode 관리 */
	List<ZipCode> selectZipCodeList(SearchZipCodeList search);
	ZipCode selectZipCode(SearchZipCode search);
	void insertZipCode(Map<String, Object> map);
	void updateZipCode(Map<String, Object> map);
	void deleteZipCode(Map<String, Object> search);
	void textUpload(List<String> zipCodeList);
	/** backup관리 */
	List<String> selectBackupOrderList(SearchOrderList search);
	void insertBackupDB(Map<String, Object> dataMap);
	void deleteOrigDB(Map<String, Object> dataMap);
	List<MonthlyCount> selectMonthlyUserCountList(SearchInterfaceKeyList search);
	List<Map<String, Object>> selectDeliveryInterfaceApi(SearchDeliveryInterface search);

	DeliveryInterface selectDeliveryInterfaceVe(SearchDeliveryInterface search);
	void updateDeliveryInterfaceVe(DeliveryInterface update);
	void insertDeliveryInterfaceVe(DeliveryInterface insert);
	/** 그리드 환경설정(그룹별) - TB_SMS_GRID_CONFIGURATION, GROUP_CODE로 그룹 구분 */
	List<Map<String, Object>> selectGridConfigList(Map<String, Object> search);
	List<Map<String, Object>> selectGridConfigGroupList(Map<String, Object> search);
	void deleteGridConfig(Map<String, Object> gridConfig);
	void insertGridConfig(List<Map<String, Object>> gridConfigList);
}