package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsOutNotice.*;
import org.yht.exps.express.model.bms.bmsOutNotice.search.SearchBmsOutNotice;
import org.yht.exps.express.model.bms.disposal.InventoryIn;

import java.util.List;
import java.util.Map;

@Mapper
public interface BmsOutNoticeMapper {

	List<BmsOutNotice> selectBmsOutNoticeList(SearchBmsOutNotice search); /** 반출통고 리스트 조회 */
	List<InventoryIn> selectInventoryList(SearchBmsOutNotice search); /** 미승인재고 리스트 조회 */
	List<BmsOutNoticePlan> selectBmsOutNoticeOmittedList(SearchBmsOutNotice search); /** 누락통보 리스트 조회 */

	BmsOutNotice selectBmsOutNoticeDetail(SearchBmsOutNotice search); /** 등록 ENTRY - 반출통고 1건 상세 */
	InventoryIn selectInventoryDetail(SearchBmsOutNotice search); /** 등록 ENTRY - 반입 1건 상세 */
	String selectErrorDesc(String srchDeclarationNo); /** 등록 ENTRY - 오류내역 조회 */

	List<BmsOutNotice> selectBmsOutNoticeListBySrchNo(SearchBmsOutNotice search); /** 조회번호, 타입에 따라 반출통고 데이터 조회 */
	List<InventoryIn> selectInventoryListBySrchNo(SearchBmsOutNotice search); /** 조회번호, 타입에 따라 반입 데이터 조회 */

	void updateBmsOutNotice(BmsOutNotice bmsOutNotice); /** 등록 ENTRY - 반출통고 UPDATE */
	void insertBmsOutNotice(BmsOutNotice bmsOutNotice); /** 등록 ENTRY - 반출통고 INSERT */
	void updateBmsInDate(BmsOutNotice bmsOutNotice); /** 등록 ENTRY - 반입 통고일자, 체화일자 UPDATE */
	void updateOutNoticeYn(BmsOutNotice bmsOutNotice); /** 반출통고누락 반출통고등록여부 UPDATE */
	void updateOutNoticePrintYn(BmsOutNoticeList bmsOutNoticeList);

	void deleteBmsOutNotice(BmsOutNotice bmsOutNotice); /** 반출통고 삭제 */
	int updatePodStatusFromBmsOutNotice(BmsOutNotice bmsOutNotice); /** 반출통고 삭제 */
	List<BmsOutNotice> selectOutNoticeInNoticeNoWithBms01(Map<String, Object> map);

	InventoryIn selectBmsInCalcOutNoticeDateByInNo(Map<String, Object> map);
	
	/** 엑셀업로드 */
	List<BmsOutNotice> selectBmsOutNoticeForExcelUpload(String cargoReferenceNo);
	List<InventoryIn> selectBmsInForExcelUpload(Map<String, Object> map);
	void insertExcelUploadOutNotice(Map<String, Object> map);
	void updateExcelUploadOutNotice(Map<String, Object> map);
	
}
