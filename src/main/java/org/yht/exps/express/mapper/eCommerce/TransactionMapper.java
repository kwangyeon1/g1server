package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.transaction.TransactionDet;
import org.yht.exps.express.model.eCommerce.transaction.TransactionOdItem;
import org.yht.exps.express.model.eCommerce.transaction.search.SearchTransactionList;

@Mapper
public interface TransactionMapper {
	List<TransactionDet> selectTransOdList(SearchTransactionList search);
	List<TransactionOdItem> selectTransItemList(SearchTransactionList search);
	List<String> selectTransOdJobNoList(String ecIfSbmtNo);
	List<Map<String, Object>> selectTransOdInfo(String odJobNo);
	List<Map<String, Object>> selectTransProductInfoList(String odJobNo);
	List<Map<String, Object>> selectChkTransCustomsInfo(String odJobNo);
	List<Map<String, Object>> selectTransOdExcelList(SearchTransactionList search);
	String selectEdiPortalMailboxId(String odJobNo);
	List<String> selectCustomerCode(String odJobNo);
	Map<String, Object> selectCustomerInfo(Map<String, Object> map);
	Integer selectMaxSeqNoCnt();
	void deleteTransactionOd(Map<String, Object> map);
	void deleteTransactionInfo(Map<String, Object> map);
	void updateSendInfo(Map<String, Object> map);
	void updateCheckCustomsId(Map<String, Object> odInfo);
}

