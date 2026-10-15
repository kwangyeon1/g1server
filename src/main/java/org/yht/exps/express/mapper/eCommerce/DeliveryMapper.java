package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryH;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryHList;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryM;
import org.yht.exps.express.model.eCommerce.delivery.UpdateDelivery;
import org.yht.exps.express.model.eCommerce.delivery.search.SearchDeliveryMList;

@Mapper
public interface DeliveryMapper {

	List<DeliveryM> selectDeliveryMList(SearchDeliveryMList search);
	List<DeliveryH> selectDeliveryHList(SearchDeliveryMList search);
	void assignDeliveryExcel(Map<String, Object> map);
	void updateDelivery(UpdateDelivery modify);
	void cancelDelivery(UpdateDelivery cancel);
	String selectHoldSingleStatus(Map<String, Object> map);
	String selectHoldMultiStatus(Map<String, Object> map);
	void holdDeliverySingle(Map<String, Object> map);
	void holdDeliveryMulti(Map<String, Object> map);
	List<Map<String, Object>> selectOdJobNos(UpdateDelivery cancel);
	List<Map<String, Object>> selectHoldOdJobNos(UpdateDelivery hold);
	void updateDeliveryPod350(Map<String, Object> update);
	Map<String, Object> selectDeliveryInterface(Map<String, Object> map);
	List<Map<String, Object>> selectCancelDeliveryList(Map<String, Object> map);
	List<Map<String, Object>> selectHoldDeliveryList(Map<String, Object> map);
	List<Map<String, Object>> selectSendDeliveryList(UpdateDelivery send);
	
	String selectKorexDeliveryOrderPK(String value);
	void insertKorexDeliveryOrderDB2DB(Map<String, Object> map);
	void insertHanjinDeliveryOrderDB2DB(Map<String, Object> map);
	List<Map<String, Object>> selectAssignDeliveryList(UpdateDelivery assign);
	void assignDelivery(Map<String, Object> dataMap);
	Map<String, Object> selectAssignDeliveryExcel(Map<String, Object> dataMap);

	List<Map<String, Object>> selectSendDeliveryListToMultiVE(UpdateDelivery send);
	int countMulti(Map<String, Object> map);

	List<Map<String, Object>> selectSendDeliveryListToMulti(UpdateDelivery send);
	List<Map<String, Object>> selectSendDeliveryListToMultiSingle(UpdateDelivery send);
	List<Map<String, Object>> selectSendDeliveryListToOdSingle(UpdateDelivery send);
	int countMultiByDelNo(Map<String, Object> map);
	int countOdbyHbl(Map<String, Object> map);
	Map<String, Object> beforeOdChk(Map<String, Object> dataMap);

	int countRecOd(Map<String, Object> map);
	int countRecMulti(Map<String, Object> map);
	List<DeliveryM> selectDeliveryMListVE(SearchDeliveryMList search);
	List<DeliveryH> selectDeliveryHListVE(SearchDeliveryMList search);
	int updateChangeDelivery(Map<String, Object> map);
	void updateMulDate(Map<String, Object> map);
	void deleteChkPod(Map<String, Object> map);
	String selectCheckNo(Map<String, Object> map);
}

