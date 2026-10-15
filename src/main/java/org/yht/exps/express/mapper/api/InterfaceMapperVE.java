package org.yht.exps.express.mapper.api;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.api.CoupangPccc;

@Mapper
public interface InterfaceMapperVE {
	Map<String, Object> selectReturnOrderInfo(Map<String, Object> params);
	Map<String, Object> selectReturnOrderInfoPop(Map<String, Object> params);
	List<Map<String, Object>> selectReturnChk(Map<String, Object> params);
	void updateReturnOrder(Map<String, Object> orderMap);
	void updateReturnOrderAd(Map<String, Object> orderMap);
	void updateReturnOrderJob(Map<String, Object> orderMap);
	void updateReturnOrderAdJob(Map<String, Object> orderMap);
	void insertReturninfo(Map<String, Object> map);
	void updateCustomsCancelInfo(Map<String, Object> params);
	void deleteChkPod(Map<String, Object> map);
	void deleteReturninfo(Map<String, Object> orderMap);
	int selectDupReqId(Map<String, Object> coupang);
	void insertReturnMulti(Map<String, Object> map);
	String selectReturnMultiSeqNoId(Map<String, Object> map);
	void insertReturnResendInfo(Map<String, Object> map);
	void insertCustomsInfo(Map<String, Object> map);
	void insertResendinfo(Map<String, Object> map);
	void updateReturnMulti(Map<String, Object> chkReturnMultiMap);

}
