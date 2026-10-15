package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.function.FunctionOrder;
import org.yht.exps.express.model.eCommerce.function.PenaltyOrder;

@Mapper
public interface FunctionMapper {
		
	List<Map<String, Object>> selectOrderList(Map<String,Object> search);
	List<Map<String, Object>> selectOrderListEnt(Map<String,Object> search);
	List<Map<String, Object>> selectOrderFunctionChk(Map<String,Object> search);
	
	
	void updateCarryin(FunctionOrder functionOrder);
	void updateSendHold(FunctionOrder functionOrder);

	void updatePodInfo(Map<String, Object> podInfo);
	
	void insertPodInfo(Map<String, Object> podInfo);	

	void deletePodInfo(FunctionOrder functionOrder);	
	
	void updateLastPodStatus(FunctionOrder functionOrder);
	void updateLastPodStatus2(FunctionOrder functionOrder);
	void updateOdLastStatus(FunctionOrder functionOrder);

	void updateObDate(FunctionOrder functionOrder);	
	
	void updatePenalty(Map<String, Object> row);	
	
	List<PenaltyOrder> selectPenaltyOrderList(Map<String,Object> search);
	List<Map<String, Object>> selectOrderRegistrationList(Map<String, Object> search);
	List<Map<String, Object>> selectCustomsRessiuItemList(Map<String, Object> dataMap);
}
