package org.yht.exps.express.mapper.bms;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalHbl;
import org.yht.exps.express.model.bms.bmsAbnormal.search.SearchCarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalCorr;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalHblCorr;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.search.SearchCarryInAbnormalCorr;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChange;
import org.yht.exps.express.model.bms.bmsInCorrection.search.SearchInCorrection;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChange;
import org.yht.exps.express.model.bms.bmsOutCorrection.search.SearchOutCorrection;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNotice;
import org.yht.exps.express.model.bms.bmsOutNotice.search.SearchBmsOutNotice;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.search.SearchBmsOut;

/***
 * BMS 송신시 사용하는 Mapper 정의
 */
@Mapper
public interface EdiBmsSendMapper {

	/***
	 * GOVCBR632(반입신고서) 목록 조회
	 */
	List<InventoryIn> selectEdiBmsInSendList(SearchInList search);

	/***
	 * GOVCBR632(반입신고서) XML 송신성공 건 LAST_POD_STATUS 업데이트
	 */
	void batchUpdateEdiBmsInSendSucc(Map<String, Object> insert);

	/***
	 * GOVCBR632(반입신고서) XML 송신실패 건 LAST_POD_STATUS 업데이트
	 */
	void batchUpdateEdiBmsInSendErr(Map<String, Object> insert);

	/***
	 * GOVCBR6NB(반출신고서) 목록 조회
	 */
	List<BmsOut> selectEdiBmsOutSendList(SearchBmsOut search);

	/***
	 * GOVCBR6NB(반출신고서) XML 송신성공 건 LAST_POD_STATUS 업데이트
	 */
	void batchUpdateEdiBmsOutSendSucc(Map<String, Object> insert);

	/***
	 * GOVCBR6NB(반출신고서) XML 송신실패 건 LAST_POD_STATUS 업데이트
	 */
	void batchUpdateEdiBmsOutSendErr(Map<String, Object> insert);

	/***
	 * GOVCBR5II(목록보고서) 목록 조회
	 */
	List<BmsOutNotice> selectEdiBmsOutNoticeSendList(SearchBmsOutNotice search);

	/***
	 * GOVCBR5II(목록보고서) XML 송신성공 건 LAST_POD_STATUS 업데이트
	 */
	void batchUpdateEdiBmsOutNoticeSendSucc(Map<String, Object> insert);

	/***
	 * GOVCBR5II(목록보고서) XML 송신실패 건 LAST_POD_STATUS 업데이트
	 */
	void batchUpdateEdiBmsOutNoticeSendErr(Map<String, Object> insert);

	/**
	 * GOVCBR5LC(반입신고정정신청서) 목록 조회
	 */
	List<CarryInChange> selectCarryInChangeList(SearchInCorrection search);

	/**
	 * 반입정정신청 항목 조회
	 */
	List<CarryInChange> selectCarryInChangeItemList(Integer changeNo);

	/**
	 * GOVCBR5LD(반출신고정정신청서) 목록 조회
	 */
	List<CarryOutChange> selectCarryOutChangeList(SearchOutCorrection search);

	/**
	 * 반출정정신청 항목 조회
	 */
	List<CarryOutChange> selectCarryOutChangeItemList(Integer changeNo);

	/**
	 * GOVCBR5LC(반입신고정정신청서) or GOVCBR5LD(반출신고정정신청서) XML 송신성공 건 LAST_POD_STATUS 업데이트
	 */
	void batchUpdateEdiBmsChangeSendSucc(Map<String, Object> insert);

	/**
	 * GOVCBR5LC(반입신고정정신청서) or GOVCBR5LD(반출신고정정신청서) XML 송신실패 건 LAST_POD_STATUS 업데이트
	 */
	void batchUpdateEdiBmsChangeSendErr(Map<String, Object> insert);

	/***
	 * GOVCBRDKO(특송화물 반입물품 이상보고서) 목록 조회
	 */
	List<CarryInAbnormal> selectEdiBmsAbnormalSendList(SearchCarryInAbnormal search);
	List<CarryInAbnormalHbl> selectEdiBmsAbnormalHblSendList(Integer abnNo);

	/***
	 * GOVCBRDKO(특송화물 반입물품 이상보고서) XML 전송 후 DB 업데이트
	 */
	void batchUpdateEdiBmsAbnormalSend(Map<String, Object> insert);

	/***
	 * GOVCBRDKP(특송화물 반입물품 이상보고 정정신청서) 목록 조회
	 */
	List<CarryInAbnormalCorr> selectEdiBmsAbnormalCorrSendList(SearchCarryInAbnormalCorr search);
	List<CarryInAbnormalHblCorr> selectEdiBmsAbnormalHblCorrSendList(Integer abnChangeNo);

	/***
	 * GOVCBRDKP(특송화물 반입물품 이상보고 정정신청서) XML 전송 후 DB 업데이트
	 */
	void batchUpdateEdiBmsAbnormalCorrSend(Map<String, Object> insert);

}
