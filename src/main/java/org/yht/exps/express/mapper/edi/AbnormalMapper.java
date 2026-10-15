package org.yht.exps.express.mapper.edi;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.edi.abnormal.Abnormal;
import org.yht.exps.express.model.edi.abnormal.AbnormalList;
import org.yht.exps.express.model.edi.abnormal.search.SearchAbnormalList;

@Mapper
public interface AbnormalMapper {
		
	List<Abnormal> selectAbnormalList(SearchAbnormalList search);
	List<Abnormal> selectBmsInPlanAbnormalList(SearchAbnormalList search);
	List<Abnormal> selectAbnormalEntryList(SearchAbnormalList search);
	List<Abnormal> selectBmsInPlanAbnormalEntryList(SearchAbnormalList search);
	List<Abnormal> selectAbnormalDeclareList(SearchAbnormalList search);
	List<Abnormal> selectBmsInPlanAbnormalDeclareList(SearchAbnormalList search);
	List<Abnormal> selectAbnormalAddPopupList(SearchAbnormalList search);
		
	void insertAbnormalList(Abnormal abnormal);
	List<Map<String, Object>> selectAbnormalDeleteCheck(AbnormalList list);
	void deleteAbnormalList(AbnormalList list);
	void deleteSpecialItemList(List<Map<String, Object>> delSpecialItemList);
	void updateAbnormalAdditional(Abnormal abnormal);
	void deleteAbnormalDeclareList(AbnormalList list);
	void deleteAbnormalDeclarePod(Abnormal abnormal);
	void updateLastPodMax(Abnormal abnormal);
	
	void insertAbnormal(Abnormal abnormal);
	void insertBmsInPlanAbnormal(Abnormal abnormal);
	void insertAbnormalMgnMfn(Abnormal abnormal);
	void insertAbnormalOrder(Abnormal abnormal);
	void insertAbnormalOrderAdditional(Abnormal abnormal);
	
	void insertSpecialItem(Abnormal abnormal);
	
	void insertMblCorrectByAb(Map<String, Object> map);
	void updateMblCorrectByAb(Map<String, Object> map);
	void updateDiscWhCode(Map<String, Object> map);
	void updateDiscItemCode(Map<String, Object> map);
	
	Map<String, Object> getSeqNoIdByAb(Abnormal abnormal);
	
	Integer getIsModData(AbnormalList abnormalList);
	Integer getIsModContainerData(AbnormalList abnormalList);
	
	void insertHblCorrectByAb(Abnormal abnormal);
	void updateModContainerNo(Map<String, Object> map);
	void insertContainerCorrectByAb(Map<String, Object> map);
	void insertOrderContainer(Map<String, Object> map);
	void updateOrderContainer(Map<String, Object> map);
	void deleteOrderContainer(Map<String, Object> map);
		
	void insertExcelAbnormal(Map<String, Object> map);
	void insertBmsInPlanExcelAbnormal(Map<String, Object> map);
	void updateAbnormal(Map<String, Object> map);
	void updateAbnormalSendDate(Map<String, Object> map);
	void updateAbnormalFinishYn(Map<String, Object> map);
		
	Map<String, Object> selectHblValidate(Map<String, Object> search);
	List<Abnormal> selectAbnormalHouseList(Map<String, Object> search);
	Map<String, Object> selectAbnormalCntrInfo(Map<String, Object> search);
	List<Map<String, Object>> selectAbnormalCntrInfoList(Map<String, Object> search);
	
	Map<String, Object> selectAbnormalChk(Map<String, Object> search);
	Map<String, Object> selectBmsInPlanAbnormalChk(Map<String, Object> search);
	List<Abnormal> selectOdList(AbnormalList abnormalList);
	Map<String, Object> getNoRegisterCount(Abnormal abnormal);
	int selectAbnormalDupChk(Map<String, Object> search);
	List<Map> selectHouseList(Map<String, Object> map);
	List<Map> selectHouseListMgnMfn(Abnormal abnormal);
	void insertSpecialManageItemMgnMfn(Map<String, Object> ab);
	void insertSpecialManageItem(Map<String, Object> ab);
	void updateUnmanagementDate(Abnormal abnormal);
	
		
}
