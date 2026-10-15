package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.consol.ConsolClose;
import org.yht.exps.express.model.eCommerce.consol.ConsolHouseOrder;
import org.yht.exps.express.model.eCommerce.consol.ConsolMasterOrder;
import org.yht.exps.express.model.eCommerce.consol.ConsolOrder;
import org.yht.exps.express.model.eCommerce.consol.ConsolRelease;
import org.yht.exps.express.model.eCommerce.consol.search.SearchConsolOrderList;
import org.yht.exps.express.model.eCommerce.order.Order;

@Mapper
public interface ConsolMapper {
	
	List<ConsolMasterOrder> selectMasterOrderList(SearchConsolOrderList search);
	List<ConsolHouseOrder> selectHouseOrderList(SearchConsolOrderList search);
	List<ConsolMasterOrder> selectImpMasterList(SearchConsolOrderList search);
	List<ConsolHouseOrder> selectImpHouseList(SearchConsolOrderList search);
	List<ConsolHouseOrder> selectImpHouseConsolList(SearchConsolOrderList search);
	List<ConsolMasterOrder> selectExpMasterList(SearchConsolOrderList search);
	List<ConsolHouseOrder> selectExpHouseList(SearchConsolOrderList search);
	List<ConsolHouseOrder> selectExpHouseConsolList(SearchConsolOrderList search);
	List<ConsolHouseOrder> selectCargoManifest(SearchConsolOrderList search);
	
	Map<String, Object> selectFinishYnChk(Map<String, Object> search);
	Map<String, Object> selectFinishYnChkList(Map<String, Object> search);
	List<Map<String, Object>> selectConsolTarget(ConsolOrder consolOrder);
	
	
	void insertMasterOrder(ConsolOrder consolOrder);
	void updateConsolOrder(ConsolOrder consolOrder);
	void updateConsolOrderBatch(Order order);
	void updateDeConsol(ConsolRelease ConsolRelease);
	void updateCloseOrderDeconsol(Map<String, Object> podMap);
	void updateClose(ConsolClose ConsolClose);
	void deleteMasterOrder(ConsolClose ConsolClose);
	void deleteMasterOrderAdditional(ConsolClose ConsolClose);
	void updateDeconsolAll(ConsolClose ConsolClose);
	void updateDeconsolExrate(Map<String, Object> map);
	void updateConsolYn(ConsolOrder consolOrder);
	void updateConsolYn(Map<String, Object> consolOrder);
	void updateSimpleDocNoImp(Map<String, Object> consolOrder);
	void updateSimpleDocNoExp(Map<String, Object> consolOrder);
	void updateSimpleDocNo(Order order);
	void updateLastPodStatus(Map<String, Object> consolOrder);
	void updateHsnOrderClose(String masterOdJobNo);
	void updateHsnMax(Map<String, Object> updateHsn);
	void updateLoadingLocation(Map<String, Object> updateHsn);
	void updateComObl(ConsolOrder consolOrder);
	void updateComOrder(ConsolOrder consolOrder);
	void updateComOrderBus(ConsolOrder consolOrder);
	String selectHsnChk(String masterOdJobNo);
	List<Map<String, Object>> selectUpdateHsnList(String masterOdJobNo);
	void updateHsnNull(ConsolClose ConsolClose);
	void deletePod(ConsolClose ConsolClose);
	void deletePodInfo(String odJobNo);
	List<Map<String, Object>> selectCloseOrderList(Map<String, Object> ConsolClose);
	
	
	List<Map<String, Object>> selectConsolIncongruityOrder(ConsolOrder consolOrder);
	ConsolOrder selectConsolMasterInfo(ConsolOrder consolOrder);
	Map<String, Object> selectHblValidate(Map<String, Object> search);
	List<Map<String, Object>> selectOrderListModNo(Map<String, Object> map);
	Map<String, Object> selectMinSimpleDocNo(Map<String, Object> map);
	Map<String, Object> selectLastConsolPodInfo(String odJobNo);
	Map<String, Object> selectPodInfo(Map<String, Object> podMap);
	List<ConsolOrder> selectConsolBltypeiertCheck(ConsolOrder consolOrder);
	List<String> selectCustCodeList(Map consol);
	void updateMasterCustLvInfo(ConsolOrder consolOrder);
	String selectEcmYnCheck(ConsolOrder consolOrder);
	Map<String, Object> selectEcmYnSimpleDocNo(Map<String, Object> houseData);
	void insertPickUpComplete(Map<String, Object> podMap);
}
