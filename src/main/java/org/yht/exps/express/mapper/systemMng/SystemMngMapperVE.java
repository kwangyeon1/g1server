package org.yht.exps.express.mapper.systemMng;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.com.fileshare.FileShare;
import org.yht.exps.express.model.systemMng.QnADeleteVE;
import org.yht.exps.express.model.systemMng.QnAVE;
import org.yht.exps.express.model.systemMng.search.SearchQnADetailVE;
import org.yht.exps.express.model.systemMng.search.SearchQnAListVE;

@Mapper
public interface SystemMngMapperVE {
	/* 문의사항 Q&A */
	List<QnAVE> selectQnAList(SearchQnAListVE search);
	List<QnAVE> selectSubQnAList(SearchQnAListVE search);
	// 로그인한 거래처로 조회
	List<QnAVE> selectCustomerCodeList(SearchQnAListVE search);

	void insertQnA(QnAVE QnAVE);
	void updateQnA(QnAVE QnAVE);
	void deleteQnA(QnADeleteVE QnADeleteVE);

	void insertSubQnA(QnAVE QnAVE);
	void updateSubQnA(QnAVE QnAVE);
	void updateFinishSubQnA(QnAVE QnAVE);
	void deleteSubQnA(QnADeleteVE QnADeleteVE);

	QnAVE selectQnADetail(SearchQnADetailVE search);
	QnAVE selectSubQnADetail(SearchQnADetailVE search);

	int getDetailCount(QnADeleteVE QnADeleteVE);
	
	void updateResendInfo(QnAVE QnAVE);
	void updateDlvChangeInfo(QnAVE QnAVE);
	void updateQuickInfo(QnAVE QnAVE);
	void insertCsHistory(QnAVE QnAVE);
	
	List<Map<String, Object>> selectQnAHistoryList(SearchQnAListVE search);
	
	void insertQnafileShare(FileShare fileShare);
	
	Map<String, Object> getOrderInfo(QnAVE qna);
	Map<String, Object> getOrderReturnInfo(QnAVE qna);
	
	void insertMultiInfo(Map<String, Object> multiParams);
	void updateOrderReturnInfo(Map<String, Object> multiParams);
	void updateOrderMultiInfo(Map<String, Object> multiParams);
	
	void deleteQnAFileShare(QnADeleteVE qnADeleteVE);
	
	Map<String, Object> selectOdInfo(SearchQnAListVE search);
}
