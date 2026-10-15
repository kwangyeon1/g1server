package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrder;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrder;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderCommon;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderContainer;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderDimension;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderHts;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderLicense;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderOtherCharge;
import org.yht.exps.express.model.eCommerce.masterOrder.search.SearchMasterOrder;
import org.yht.exps.express.model.eCommerce.masterOrder.search.SearchMasterOrderList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderDelete;

@Mapper
public interface MasterOrderMapper {
	
	Map<String, Object> selectMasterOrderChk(Map<String, Object> map);
	Map<String, Object> selectUserInfo(MasterOrder order);
	Map<String, Object> selectHouseSum(MasterOrder order);
	List<Map<String, Object>> selectHouseContainerList(MasterOrder order);
	MasterOrder selectMasterOrder(SearchMasterOrder search);
	List<MasterOrder> selectEcmOrderList(MasterOrder order);
	List<MasterOrderOtherCharge> selectOtherCharge(SearchMasterOrder search);
	List<MasterOrderDimension> selectDimension(SearchMasterOrder search);
	List<MasterOrderLicense> selectLicense(SearchMasterOrder search);
	List<MasterOrderHts> selectHts(SearchMasterOrder search);
	List<MasterOrderContainer> selectContainer(SearchMasterOrder search);
	List<MasterOrderContainer> selectCntrType(SearchMasterOrder search);
	List<MasterOrderCommon> selectMasterOrderListCommon(SearchMasterOrderList search);
	
	List<Order> selectHouseOrderList(Map<String, Object> map);
	List<OrderDelete> selectDeleteHouseList(Map<String, Object> map);
	
	
	void insertMasterObl(MasterOrder order);
	void insertMasterOblRealDeliverer(MasterOrder order);
	void insertMasterOrder(MasterOrder order);
	void insertMasterOrderBus(MasterOrder order);
	void insertMasterOrderEcm(MasterOrder order);
	void insertMasterOrderAdditional(MasterOrder order);
	void insertOtherCharge(Map<String, Object> map);
	void insertDimension(Map<String, Object> map);
	void insertLicense(Map<String, Object> map);
	void insertHts(Map<String, Object> map);
	void insertMark(Map<String, Object> map);
	void insertContainer(Map<String, Object> map);
	void insertCntrType(Map<String, Object> map);
	
	void updateMasterObl(MasterOrder order);
	void updateMasterOblRealDeliverer(MasterOrder order);
	void updateMasterOrder(MasterOrder order);
	void updateMasterOrderBus(MasterOrder order);
	void updateMasterOrderEcm(MasterOrder order);
	void updateMasterOrderAdditional(MasterOrder order);
	void updatePodBlNo(Map<String, Object> map);
	void updateHouseOrderBatch(Order order);
	
	void deleteMasterObl(Map<String, Object> map);
	void deleteMasterOblRealDeliverer(Map<String, Object> map);
	void deleteMasterOrder(Map<String, Object> map);
	void deleteMasterOrderBus(Map<String, Object> map);
	void deleteMasterOrderEcm(Map<String, Object> map);
	void deleteMasterOrderAdditional(Map<String, Object> map);
	void deleteOtherCharge(Map<String, Object> map);
	void deleteDimension(Map<String, Object> map);
	void deleteLicense(Map<String, Object> map);
	void deleteHts(Map<String, Object> map);
	void deleteMark(Map<String, Object> map);
	void deleteContainer(Map<String, Object> map);
	void deleteCntrType(Map<String, Object> map);
	List<HouseOrder> selectHouseComOrder(MasterOrder data);
	void updateHouseSrNo(HouseOrder order);

	Map<String, Object> selectMasterOrderChkEms(Map<String, Object> tmpMap);
}
