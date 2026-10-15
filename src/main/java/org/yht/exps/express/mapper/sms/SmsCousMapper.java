package org.yht.exps.express.mapper.sms;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.sms.search.SearchEcmCustomerDetail;
import org.yht.exps.express.model.sms.search.SearchEcmCustomerList;
import org.yht.exps.express.model.sms.search.SearchEcmCustomerTreeList;
import org.yht.exps.express.model.sms.user.EcmCustomer;
import org.yht.exps.express.model.sms.user.EcmCustomerBank;
import org.yht.exps.express.model.sms.user.EcmCustomerDelete;
import org.yht.exps.express.model.sms.user.EcmCustomerInterface;
import org.yht.exps.express.model.sms.user.EcmCustomerMapping;
import org.yht.exps.express.model.sms.user.EcmUser;
import org.yht.exps.express.model.sms.user.EcmUserApprove;
import org.yht.exps.express.model.sms.user.EcmUserDelete;
import org.yht.exps.express.model.sms.user.EcmUserDetail;
import org.yht.exps.express.model.sms.user.EcmUserSave;
import org.yht.exps.express.model.sms.user.search.SearchEcmUserDetail;
import org.yht.exps.express.model.sms.user.search.SearchEcmUserList;

@Mapper
public interface SmsCousMapper {
	
List<EcmUser> selectEcmUserList(SearchEcmUserList search);
	
	List<Map<String,Object>> selectEcmCustomerTreeList(SearchEcmCustomerTreeList search);
	
	void updateEcmUserApprove(EcmUserApprove ecmUserApprove);
	
	void deleteEcmUser(EcmUserDelete ecmUserDelete);
	
	void deleteEcmUserList(EcmUserDelete ecmUserDelete);
	
	void insertEcmUser(EcmUserSave ecmUserSave);	
	
	void insertEcmUserOption(EcmUserSave ecmUserSave);	
	
	void updateEcmUser(EcmUserSave ecmUserSave);
	
	EcmUserDetail selectEcmUserDetail(SearchEcmUserDetail search);
	
	String selectMakeUserId(Map<String,Object> search);
	String selectLoginUserSendModifyYn(Map<String,Object> search);
	void deleteEcmCustomer(EcmCustomerDelete ecmCustomerDelete);
	
	void deleteEcmCustomerList(EcmCustomerDelete ecmCustomerDelete);
	
	void insertEcmCustomer(EcmCustomer ecmCustomer);	
	
	void insertEcmCustomerSolution(EcmCustomer ecmCustomer);	
	
	void insertEcmCustomerBusiness(EcmCustomer ecmCustomer);
	
	void insertEcmCustomerBankInfo(EcmCustomer ecmCustomer);
	
	void deleteEcmCustomerBankInfo(EcmCustomer ecmCustomer);

	void insertEcmCustomerInterface(EcmCustomer ecmCustomer);
	
	void deleteEcmCustomerInterface(EcmCustomer ecmCustomer);
	
	void updateEcmCustomer(EcmCustomer ecmCustomer);	
	
	List<EcmCustomer> selectEcmCustomerList(SearchEcmCustomerList search);
	
	List<EcmCustomer> selectEcmCustomerAllList(SearchEcmCustomerList search);
	
	EcmCustomer selectEcmCustomerDetail(SearchEcmCustomerDetail search);
	
	EcmCustomerBank selectEcmCustomerBank(SearchEcmCustomerDetail search);
	
	List<EcmCustomerInterface> selectEcmCustomerInterface(SearchEcmCustomerDetail search);
	
	void mappingExec(EcmCustomerMapping ecmCustomerMapping);
	
	void updateLevel(EcmCustomerMapping ecmCustomerMapping);
	
	Integer selectSmsCustomerCodeDupCheck(Map<String,Object> checkMap);
	
	Integer selectSmsCustomerNameDupCheck(Map<String,Object> checkMap);
	
	String selectSMSCustomerLevel(Map<String,Object> custMap);
	
	void insertSMSCustomer(Map<String,Object> customMap);
	
	void insertSMSCustomerSolution(Map<String,Object> customMap);
	
	void insertSMSCustomerBusiness(Map<String,Object> customMap);
	
	Integer selectSmsUserIdDupCheck(Map<String,Object> checkMap);
	
	void insertSMSUser(Map<String,Object> userMap);
	
	void insertSMSUserOption(Map<String,Object> userMap);
	
	String selectUserType(Map<String,Object> search);
	
	int checkEcmCustomerUserCnt(EcmCustomerDelete ecmCustomerDelete);
	
	void insertCustomerInterface(Map<String, Object> kreamMap);

	Map<String, Object> selectCustomerInterface(SearchEcmCustomerDetail search);


	void insertKreaminfo(EcmCustomer ecmCustomer);

	List<EcmUser> selectEcmUserListVe(SearchEcmUserList search);
	List<EcmCustomer> selectEcmCustomerAllListVe(SearchEcmCustomerList search);

}
