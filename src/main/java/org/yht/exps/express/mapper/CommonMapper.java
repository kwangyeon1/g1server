package org.yht.exps.express.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.SearchExcelList;
import org.yht.exps.express.model.SearchOdJobNo;
import org.yht.exps.express.model.sms.logs.BatchDataLogs;
import org.yht.exps.express.model.sms.logs.search.SearchBatchDataLogs;
import org.yht.exps.express.model.sms.message.Message;

@Mapper
public interface CommonMapper {
	Map<String, Object> checkClosedOrderInfo(Map<String, Object> search);
	String getNextStockMaxNo(Map<String, Object> search);
	List<Message> selectMessage(Map<String, Object> search);
	List<Map<String, Object>> selectMblNoList(Map<String, Object> map);
	String selectNextStockMaxNo(Map<String, Object> map);
	String selectNextStockNo(Map<String, Object> map);
	void updatePackingErrorYn(Map<String, Object> map);
	int updateNextStockMaxNo(Map<String, Object> map);
	int insertStockMaxNo(Map<String, Object> map);
	void updateLastPodStatus(Map<String, Object> map);
	List<String> selectCustCode(Map<String, Object> map);
	Map<String, Object> selectBmsCdType(Map<String, Object> map);
	List<Map<String, Object>> selectBmsCdTypeList(Map<String, Object> map);
	void updateBmsCdType(Map<String, Object> map);
	Map<String, Object> selectStockMaxNo(Map<String, Object> map);
	Map<String, Object> selectHeadEntBusinessInfo(Map<String, Object> map);
	
	Map<String, Object> selectAlarmGroupKakaoInfo(Map<String, Object> map);
	List<Map<String, Object>> selectAlarmGroupUser(Map<String, Object> map);
	void insertAlarmGroupKakaoResult(Map<String, Object> map);
	void insertMailLog(Map<String, Object> map);
	void insertMailLogDetail(Map<String, Object> map);
	void updateMailLog(Map<String, Object> map);
	String selectMailSeqNoId(int seqNo);
	List<Map<String, Object>> selectMailLogList(Map<String, Object> map);
	List<Map<String, Object>> selectMailAttchList(Map<String, Object> map);
	
	List<BatchDataLogs> selectEdiGovRecvList(SearchBatchDataLogs searchBatchDataLogs);
	Map<String, Object> selectLastPodInfo(Map<String, Object> map);
	Map<String, Object> selectSystemOption2(Map<String, Object> map);
	String selectSystemOption(Map<String, Object> map);
	
	Map<String, Object> selectEdiExpressSign(Map<String, Object> paramMap);
	Map<String, Object> selectOrderChk(Map<String, Object> paramMap);

	List<Map<String, Object>> selectExcelItemList(Map<String, Object> map);
	
	List<Map<String, Object>> selectCustomsList(Map<String, Object> map);
	void updateCustoms(Map<String, Object> search);
	Map<String, Object> getCodeDetail(Map<String, Object> map);
	
	Map<String, Object> selectUserInfo(Map<String, Object> map);
	Map<String, Object> selectCodeName(Map<String, Object> map);
	Map<String, Object> selectHblNoStock(Map<String, Object> map);
	String selectEntCode(String search);
	List<Map<String, Object>> getDomesticCodeList(Map<String, Object> map);
	Map<String, Object> selectExchangeRate(Map<String, Object> search);
	Map<String, Object> selectExchangeRate2(Map<String, Object> search);
	int selectChkDupBlNo(Map<String, Object> search);
	int selectChkOrderClose(Map<String, Object> search);
	
	void insertGovSend(Map<String, Object> map);
	void updatePod(Map<String, Object> map);
	void insertPod(Map<String, Object> map);
	/** 적하목록전송 */
	void insertInterfaceLog(Map<String, Object> map);
	Map<String, Object> selectBusEdiEcmInfo(Map<String, Object> map);
	Map<String, Object> selectBusEdiCodeInfo(Map<String, Object> map);
	
	int selectExcelOrderCount(SearchExcelList search);
	int selectSimpleDocNoCount(Map<String, Object> map);
	List<Map<String, Object>> selectMakeExcelDownload(SearchExcelList search); 
	List<Map<String, Object>> selectExcelDownFormatItem(Map<String, Object> map);
	
	Map<String, Object> selectSimpleDocSendYn(Map<String, Object> map);
	Map<String, Object> selectSimpleDocNoInfo(Map<String, Object> map);
	Map<String, Object> selectEnterpriseInfo(String entCode);
	Map<String, Object> getOdJobNo(SearchOdJobNo search);
	List<Map<String, Object>> selectCustomerInfoList(Map<String, Object> map);
	Map<String, Object> selectCustomerInfo(Map<String, Object> map);
	String selectEnterPriseEmsYn(String entCode);
	Map<String, Object> selectCustomerApiKeyInfo(Map<String, Object> map);
	Map<String, Object> selectUpsExchangeRate(Map<String, Object> tmpMap);
	void updateItemCustoms(Map<String, Object> errMap);
	Map<String, Object> selectManifestLastPodInfo(Map<String, Object> resetData);
	
	
}
