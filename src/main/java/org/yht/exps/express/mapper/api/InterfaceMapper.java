package org.yht.exps.express.mapper.api;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.api.HblReturnTracking;

@Mapper
public interface InterfaceMapper {
	 
	Map<String, Object> selectInterfaceInfo(Map<String, Object> map);
	List<Map<String, Object>> selectTrackingHbl(Map<String, Object> map);
	List<Map<String, Object>> selectDeliveryTracking(Map<String, Object> map);
	Map<String, Object> selectOrderRegisterDate(Map<String, Object> map);
	Map<String, Object> selectBmsOutPlanRegisterDate(Map<String, Object> map);
	Map<String, Object> selectDeliveryInterfaceApi(Map<String, Object> map);
	Map<String, Object> selectStockAPI(Map<String, Object> map);
	Map<String, Object> getCreateStockAPI(Map<String, Object> map);
	Map<String, Object> getKorexAddressCode(Map<String, Object> map);
	Map<String, Object> checkKorexPostCode(String zipcode);
	Map<String, Object> getHanjinAddressCode(Map<String, Object> map);
	void insertPacking(Map<String, Object> map);
	int updateBmsOutPlanScanDate(Map<String, Object> map);
	int updateBmsOutScanDate(Map<String, Object> map);
	void updateInterfaceCnt(Map<String, Object> map);
	List<HblReturnTracking> selectOblTracking(Map<String, Object> paramMap); 
	List<Map<String, Object>> selectOrderTracking(Map<String, Object> paramMap);
	List<Map<String, Object>> selectOrderTrackingCustomsInfo(Map<String, Object> paramMap);
	List<Map<String, Object>> selectOrderTrackingMultiInfo(Map<String, Object> paramMap);
	void insertImageDisplayInfo(Map<String, Object> paramMap);
	int selectPoNoCnt(Map<String, Object> map);
	List<Map<String, Object>> selectEmsOrderRequestBl(Map<String, Object> map);
	Map<String, Object> getCreateEmsStockAPI(Map<String, Object> paramMap);
	Map<String, Object> selectUnusedStockAPI(Map<String, Object> paramMap);
	List<Map<String, Object>> selectEcmOrderRequestBl(Map<String, Object> map);
	List<Map<String, Object>> selectEcmOrderMultiBl(Map<String, Object> map);
	List<Map<String, Object>> selectOrderTrackingVE(Map<String, Object> paramMap);
}
