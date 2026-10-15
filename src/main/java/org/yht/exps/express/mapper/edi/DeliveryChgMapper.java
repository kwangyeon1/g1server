package org.yht.exps.express.mapper.edi;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChg;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChgDeclareList;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChgDoc;
import org.yht.exps.express.model.edi.deliveryChg.search.SearchDeliveryChgList;

@Mapper
public interface DeliveryChgMapper {
	
	List<DeliveryChg> selectDeliveryDocList(SearchDeliveryChgList search);
	
	List<DeliveryChg> selectDeliveryList(SearchDeliveryChgList search);
		
	void insertDeliveryChg(DeliveryChg deliveryChg);
	
	void updateDeliveryJob(DeliveryChg deliveryChg);
	
	DeliveryChg getDeliveryDocNo(DeliveryChgDoc deliveryChgDoc);
	
	Map<String, Object> selectEdiCreateChk(DeliveryChgDoc deliveryChgDoc);
	int selectEdiSendChk(DeliveryChgDoc deliveryChgDoc);
	/* int */List<String> selectEdiSendChk2(Map<String, Object> deliveryChtDocTmpMap);	// 매개변수 오류로 인해 나누어 조회할 때 사용하는 쿼리
	List<DeliveryChg> selectDeliveryChgList(DeliveryChgDoc deliveryChgDoc);
	void updateBatchDeliveryChg(DeliveryChgDoc deliveryChgDoc);
	
	void updateExcelDeliveryChg(Map<String, Object> deliveryChtDocTmpMap);
	
	void deleteBatchDeliveryChg(DeliveryChgDoc deliveryChgDoc);
	
	Map<String, Object> selectHblValidate(Map<String, Object> search);
	List<Map<String, Object>> selectHblValidate2(Map<String, Object> search);
	int selectHblValidate3(Map<String, Object> search);
	
	List<DeliveryChg> selectOdList(DeliveryChgDoc deliveryChgDoc);
	List<DeliveryChg> selectDeliveryListTargetCount(SearchDeliveryChgList search);
	
	List<Map<String, Object>> selectOrderList(Map<String,Object> search);
		
}
