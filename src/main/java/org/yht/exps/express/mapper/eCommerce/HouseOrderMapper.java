package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrder;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderCommon;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderContainer;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderDimension;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderHts;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderLicense;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderOtherCharge;
import org.yht.exps.express.model.eCommerce.houseOrder.search.SearchHouseOrder;
import org.yht.exps.express.model.eCommerce.houseOrder.search.SearchHouseOrderList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderDelete;

@Mapper
public interface HouseOrderMapper {
	
	Map<String, Object> selectHouseOrderChk(Map<String, Object> map);
	Map<String, Object> selectUserInfo(HouseOrder order);
	Map<String, Object> selectHouseDetail(HouseOrder order);
	Map<String, Object> selectMasterInfo(HouseOrder order);
	List<Map<String, Object>> selectHouseContainerList(HouseOrder order);
	HouseOrder selectHouseOrder(SearchHouseOrder search);
	List<HouseOrder> selectEcmOrderList(HouseOrder order);
	List<HouseOrderOtherCharge> selectOtherCharge(SearchHouseOrder search);
	List<HouseOrderDimension> selectDimension(SearchHouseOrder search);
	List<HouseOrderLicense> selectLicense(SearchHouseOrder search);
	List<HouseOrderHts> selectHts(SearchHouseOrder search);
	List<HouseOrderContainer> selectContainer(SearchHouseOrder search);
	List<HouseOrderContainer> selectCntrType(SearchHouseOrder search);
	List<HouseOrderCommon> selectHouseOrderListCommon(SearchHouseOrderList search);
	
	List<Order> selectHouseOrderList(Map<String, Object> map);
	List<OrderDelete> selectDeleteHouseList(Map<String, Object> map);
	
	
	void insertHouseObl(HouseOrder order);
	void insertHouseOblRealDeliverer(HouseOrder order);
	void insertHouseOrder(HouseOrder order);
	void insertHouseOrderBus(HouseOrder order);
	void insertHouseOrderEcm(HouseOrder order);
	void insertHouseOrderAdditional(HouseOrder order);
	void insertOtherCharge(Map<String, Object> map);
	void insertDimension(Map<String, Object> map);
	void insertLicense(Map<String, Object> map);
	void insertSblLicense(Map<String, Object> map);
	void insertHts(Map<String, Object> map);
	void insertMark(Map<String, Object> map);
	void insertContainer(Map<String, Object> map);
	void insertCntrType(Map<String, Object> map);
	
	void updateHouseObl(HouseOrder order);
	void updateHouseOblRealDeliverer(HouseOrder order);
	void updateHouseOrder(HouseOrder order);
	void updateHouseOrderBus(HouseOrder order);
	void updateHouseOrderEcm(HouseOrder order);
	void updateHouseOrderAdditional(HouseOrder order);
	void updatePodBlNo(Map<String, Object> map);
	void updateHouseOrderBatch(Order order);
	void updateSbl(HouseOrder order);
	
	void deleteHouseObl(Map<String, Object> map);
	void deleteHouseOblRealDeliverer(Map<String, Object> map);
	void deleteHouseOrder(Map<String, Object> map);
	void deleteHouseOrderBus(Map<String, Object> map);
	void deleteHouseOrderEcm(Map<String, Object> map);
	void deleteHouseOrderAdditional(Map<String, Object> map);
	void deleteOtherCharge(Map<String, Object> map);
	void deleteDimension(Map<String, Object> map);
	void deleteLicense(Map<String, Object> map);
	void deleteSblLicense(Map<String, Object> map);
	void deleteHts(Map<String, Object> map);
	void deleteMark(Map<String, Object> map);
	void deleteContainer(Map<String, Object> map);
	void deleteCntrType(Map<String, Object> map);
	
	HouseOrder selectSrNo(HouseOrder data);

    Map<String, Object> selectHouseOrderChkEms(Map<String, Object> tmpMap);
    int selectChkDupBlNo(Map<String, Object> map);
}
