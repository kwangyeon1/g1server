package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.yht.exps.express.model.bms.disposal.Disposal;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.disposal.SpCallVo;
import org.yht.exps.express.model.bms.disposal.search.SearchDisposal;

import java.util.List;
import java.util.Map;

@Mapper
public interface BmsDisposalMapper {
	//폐기신청 재고 조회
	List<InventoryIn> selectInventoryInList(SearchDisposal search);
	List<InventoryIn> selectInventoryInListExcel(SearchDisposal search);


	//폐기신청 폐기 조회
	List<Disposal> selectDisposalList(SearchDisposal search);
	List<Disposal> selectDisposalListExcel(SearchDisposal search);
    //폐기신청 데이터 한개 조회 ( disposalNo 로 조회 // blNo로 조회)
	Disposal     selectDisposalDetail(SearchDisposal search);
	//재고 리스트  한개 조회 ( inNo 로 조회 // blNo로 조회)
	InventoryIn  selectInventoryDetail(SearchDisposal search);



	int insertDisposal(Disposal disposal);
	int updateDisposal(Disposal disposal);
	void deleteDisposal(Disposal disposal);
	
	// 해당 폐기가 이미 반출 됬는지 확인정보
	List<Disposal> getAlreadyOutFromDisposal(@Param("map") List<Map<String, Object>> map);

	/***
	 *
	 * @param spCallVo
	 * @return
	 */
	void   callProcBMSInOutSeq(SpCallVo spCallVo);
	/***
	 * 폐기 신청시 에 DISPOSAL_YN : Y
	 * 폐기 삭제 처리시에 DISPOSAL_YN : N
	 * @param in
	 */
	void updateBmsInDisposalYn(InventoryIn in);
     // 폐기 신청 일괄 생성
	Integer selectDisposalBatchCheckCount(InventoryIn inventoryIn);
	Integer selectDisposalExcelUploadCheckCount(String cargoReferenceNo);
	InventoryIn selectInventoryForDisposalExcelUpload(String cargoReferenceNo);
	Disposal selectRecentDisposalData(String entCode);
	void insertDisposalBatchGen(Disposal disposal);
	void insertDisposalBatchGenWithExcelData(Disposal disposal);

	// 폐기 반출 일괄 생성
	Integer selectOutBatchCheckCount(Disposal disposal);

	void insertOutBatchGen(Disposal disposal);
	void updateInCtWtCbm(Disposal disposal);
	void updateBmsDisposalOutYn(Disposal disposal);

	/***
	 * 폐기신청 관세청신고 해상 Excel 자료 내리기
	 * @param search
	 * @return
	 */
	List<Map<String, Object>> selectDisposalExcelSeaList(SearchDisposal search);
	/***
	 * 폐기신청 관세청신고 항공 Excel 자료 내리기
	 * @param search
	 * @return
	 */
	List<Map<String, Object>> selectDisposalExcelAirList(SearchDisposal search);

	/***
	 * (폐기번호, 반입.오더관리번호) 리스트 조회 
	 * @param search
	 * @return
	 */
	List<InventoryIn> selectOdJobNoListFromDisp(Map<String, Object> map);
	
	// 폐기스캔
	List<Disposal> selectDiscardScan(String blNo);
	int insertDiscardScanLog(Map<String, Object> paramMap);
	int updateDiscardScan(Map<String, Object> paramMap);


}
