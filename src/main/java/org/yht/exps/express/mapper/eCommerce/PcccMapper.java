package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.pccc.Pccc;
import org.yht.exps.express.model.eCommerce.pccc.PcccBatch;
import org.yht.exps.express.model.eCommerce.pccc.PcccList;
import org.yht.exps.express.model.eCommerce.pccc.PcccSaveResult;
import org.yht.exps.express.model.eCommerce.pccc.PodDetail;
import org.yht.exps.express.model.eCommerce.pccc.search.SearchPcccList;
import org.yht.exps.express.model.eCommerce.pccc.search.SearchPod;
import org.yht.exps.express.model.systemMng.Alarm;

@Mapper
public interface PcccMapper {
	
	List<Pccc> selectPcccList(SearchPcccList search);
	Pccc selectPcccDetail(SearchPcccList search);
	String selectUnipassCustomsVerifyKey(String entCode);
	void updatePccc(Map<String, Object> result);
	void updateAlarmSendStop(PcccList list);
	List<PcccBatch> selectPcccBatchList(SearchPcccList search);
	PcccBatch selectPcccBatch(PcccBatch pcccBatch);
	void insertPcccBatch(PcccBatch pcccBatch);
	void updatePcccBatch(PcccBatch pcccBatch);
	void deletePcccBatch(PcccBatch pcccBatch);
	void updatePcccOutCall(Pccc pccc);
	
	//개인통관부호검증 성공건 쿠팡전송
	Map<String, Object> selectSendCustomsInfo(Map<String,Object> sendMap);
	void updateSendCustomsInfo(Map<String, Object> updateMap);
	//알람톡 전송
	List<Alarm> selectAlarm(SearchPcccList search);
	List<Map<String,Object>>  selectSendDataList(List<Map<String,Object>>  map);
	Map<String, Object> selectKakaoInfo(Map<String, Object> map);
	Map<String, Object> selectKakaoInfoEnterprise(Map<String, Object> map);
	Map<String, Object> selectSendSmsCustomsIdVerify(Map<String, Object> map);
	void insertKakaoSendResultBatch(Map<String,Object> map);
	
	
	//POD상태조회
	PodDetail selectPodDetail(SearchPod search);

	//배송상태조회
	List<Map<String, Object>> selectOrderPodStatusList(Map<String, Object> searchMap);

	void batchInsertPodStatus(Map<String, Object> map);
	Map<String, Object> selectDeliveryApiInfo(Map<String, Object> map);
	void updateOrderLastPodStatus(Map<String, Object> map);
	Map<String, Object> selectOrderPodStatus(Map<String, Object> searchMap);
	void batchTrackingDeletePod(Map<String, Object> searchMap);
	void updateOrderDeliveryInfo(Map<String, Object> map);
	Map<String, Object> selectDeliveryApiInfoAll(Map<String, Object> map);
	void batchDeletePod(Map<String, Object> searchMap);
	List<Map<String, Object>> selectOrderMultiList(Map<String, Object> searchDeliveryMap);
	void updateOdMultiDate(Map<String, Object> multiUpdateMap);
	void updateOdDate(Map<String, Object> updateOdMap);
	int countMulti(Map<String, Object> chkMultMap);
	int selectPodStatusTrackingCnt(Map<String, Object> podInfoMap);
	void batchInsertCoupangPodStatus(Map<String, Object> podInfoMap);
	Map<String, Object> selectDeliveryApiInfoAllMapping(Map<String, Object> srchDeliveryApiMap);
	Map<String, Object> selectOdInfo(SearchPod search);
	List<Map<String, Object>> selectOrderPodStatusMultiList(Map<String, Object> searchDeliveryMap);
	Pccc selectCustomsCodeVerificationInfo(SearchPod search);
	void UpdateCustomsCodeVerified(PcccSaveResult pcccSaveResult);
	
	List<Map<String, Object>> selectMultiNoList(SearchPod search);
}
