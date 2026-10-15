package org.yht.exps.express.mapper.edi;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.edi.SendDelivery;

@Mapper
public interface CustomsMapper {
	
	
	List<Map<String, Object>> selectSendDelivery(Object ediDocNo);
	List<Map<String, Object>> selectSendUnmanagementHouse(Map<String, Object> search);
	List<Map<String, Object>> selectSendUnmanagementItem(SendDelivery sendDelivery);
	List<Map<String, Object>> selectSendFulfillment(SendDelivery sendDelivery);
	List<Map<String, Object>> selectSendAbnormalities(SendDelivery sendDelivery);
	List<Map<String, Object>> selectSendBmsInPlanAbnormalities(SendDelivery sendDelivery);
	List<Map<String, Object>> selectSendAbnormalitiesAbnormal(Map<String, Object> search);
	List<Map<String, Object>> selectSendBmsInPlanAbnormalitiesAbnormal(Map<String, Object> search);
	void updateSendAbnormalitiesReportd(Map<String, Object> search);
	void updateSendBmsInPlanAbnormalitiesReportd(Map<String, Object> search);
	void updateDeliveryChangedSeqNo(Map<String, Object> search);
	void updateEidLinked(Map<String, Object> map);
	void updateUnmanagementDate(Map<String, Object> map);
	void updateEdiStatus(Map<String, Object> map);
}
