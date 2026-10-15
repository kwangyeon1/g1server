package org.yht.exps.express.mapper.systemMng;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.com.fileshare.FileShare;
import org.yht.exps.express.model.systemMng.HubNotice;
import org.yht.exps.express.model.systemMng.HubQnA;
import org.yht.exps.express.model.systemMng.HubQnADelete;
import org.yht.exps.express.model.systemMng.search.SearchHubMainNoticeList;
import org.yht.exps.express.model.systemMng.search.SearchHubQnADetail;
import org.yht.exps.express.model.systemMng.search.SearchHubQnAList;

@Mapper
public interface HubSystemMngMapper {
	
	/* eHub문의사항 Q&A */
	List<HubQnA> selectQnAList(SearchHubQnAList search);
	List<HubQnA> selectSubQnAList(SearchHubQnAList search);
	Map<String, Object> selectQnASummary(SearchHubQnAList param);
	// 로그인한 거래처로 조회
	List<HubQnA> selectCustomerCodeList(SearchHubQnAList search);

	void insertQnA(HubQnA HubQnA);
	void updateQnA(HubQnA HubQnA);
	void deleteQnA(HubQnADelete HubQnADelete);

	void insertSubQnA(HubQnA HubQnA);
	void updateSubQnA(HubQnA HubQnA);
	void updateFinishSubQnA(HubQnA HubQnA);
	void deleteSubQnA(HubQnADelete HubQnADelete);

	HubQnA selectQnADetail(SearchHubQnADetail search);
	HubQnA selectSubQnADetail(SearchHubQnADetail search);

	List<HubNotice> selectHubMainNoticeList(SearchHubMainNoticeList search);
	
	int getDetailCount(HubQnADelete HubQnADelete);
	
	//BL유효성검사
	int getHblCount(HubQnA HubQnA);
	void insertQnafileShare(FileShare fileShare);
}
