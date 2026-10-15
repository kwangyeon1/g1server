package org.yht.exps.express.mapper.mobile;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.mobile.od.MobEcod;
import org.yht.exps.express.model.mobile.od.MobEcodMulti;
import org.yht.exps.express.model.mobile.od.search.SearchMobEcod;

@Mapper
public interface MobEcodMapper {
	
	MobEcod selectOrderDetail(SearchMobEcod search);
	MobEcod selectOrderDetailVe(SearchMobEcod search);
	MobEcod selectOrderDetail2(SearchMobEcod search);
	
	List<MobEcod> selectInventoryList(SearchMobEcod search);
	
	Integer selectCarryoutCnt(SearchMobEcod search);
	
	void carryoutSave(MobEcod mobEcod);
	
	MobEcod selectLocationList(MobEcod mobEcod);
	
	void inventorySave(MobEcod mobEcod);
	
	List<MobEcodMulti> selectOrderMulti(SearchMobEcod search);
	
	void insertOrderMulti(MobEcodMulti multi);
	
	void updateOrderMulti(MobEcodMulti multi);
	
	void multiBatchSave(MobEcod mobEcod);
	void multiBatchDelete(MobEcod mobEcod);
	
	void saveReceiptList(MobEcod mobEcod);
	
	void saveReceiptList2(MobEcod mobEcod);
	
	void deleteOrderMulti(MobEcod mobEcod);
	
	void updateOrderReceiptMulti(MobEcod mobEcod);

	List<MobEcod> selectOrderDetailByCarryoutList(SearchMobEcod search);
	
	MobEcod selectMultiOrderDetail(SearchMobEcod search);	
	
	List<MobEcodMulti> selectMultiOrderList(SearchMobEcod search);
	
	void insertMultiOrderDelivery(MobEcodMulti multi);
	
	void updateMultiOrderDeliveryMul(MobEcodMulti multi);
	
	MobEcod selectCaseOrderDetail(SearchMobEcod search);	
	
	void deleteCaseOrderDelivery(MobEcodMulti multi);
	
	void saveCaseOrderDelivery(MobEcodMulti multi);
	void updateCaseOrderDelivery(MobEcodMulti multi);
	MobEcod selectCaseOrderDetailMultiVe(SearchMobEcod search);
}
