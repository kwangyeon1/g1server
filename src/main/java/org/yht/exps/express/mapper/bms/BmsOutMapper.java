package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsComm.BmsEcmPod;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.out.BmsContainer;
import org.yht.exps.express.model.bms.out.BmsDelivery;
import org.yht.exps.express.model.bms.out.BmsEncourageNoti;
import org.yht.exps.express.model.bms.out.BmsInOut;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutDelivery;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.exps.express.model.bms.out.BmsOutSample;
import org.yht.exps.express.model.bms.out.ftp.FtpOut;
import org.yht.exps.express.model.bms.out.search.SearchBmsInOut;
import org.yht.exps.express.model.bms.out.search.SearchBmsOut;
import org.yht.exps.express.model.mobile.bms.BmsLocalInspectResult;
import org.yht.exps.express.model.sms.user.EcmCustomer;

import java.util.List;
import java.util.Map;

@Mapper
public interface BmsOutMapper {
	// 재고 조회
	List<InventoryIn> selectInventoryInList(SearchBmsOut search);
	// 반출 조회
	List<BmsOut> selectBmsOutList(SearchBmsOut search);
	List<BmsOut> selectBmsOutList2(SearchBmsOut search);
	// 반출승인/승인취하 조회
	List<BmsOutPlan> selectBmsOutPlanList(SearchBmsOut search);
	List<BmsOutPlan> selectBmsOutPlanList2(SearchBmsOut search);
	// 견품반출 조회
	List<BmsOutSample> selectBmsOutSampleList(SearchBmsOut search);

	List<Integer> selectTicOutSeqListFromBmsOut(Map<String, Object> map);

    //폐기신청 데이터 한개 조회 ( disposalNo 로 조회 // blNo로 조회)
	BmsOut     selectBmsOutDetail(SearchBmsOut search);
	//재고 리스트  한개 조회 ( inNo 로 조회 // blNo로 조회)
	InventoryIn  selectInventoryDetail(SearchBmsOut search);

	BmsOutPlan selectBmsOutPlanDetail(SearchBmsOut search);
	
	BmsOutSample selectBmsOutSampleDetail(SearchBmsOut search);
	List<Map<String, Object>> selectSampleWithPkgList(BmsOutSample search);

	List<BmsContainer> selectInContainerList(SearchBmsOut search);

	List<BmsContainer> selectOutContainerList(SearchBmsOut search);

	List<BmsDelivery> selectDoList(SearchBmsOut search);


	int insertBmsOut(BmsOut bmsOut);
	int updateBmsOut(BmsOut bmsOut);
	int updateInitPkgAndTic(BmsOut bmsOut);
	BmsOutPlan selectOutPlanNotExistInNo(Map<String, Object> param);
	List<InventoryIn> selectBmsInListByCargoNo(Map<String, Object> param);
	Map<String, Object>  selectBmsInExistStock(Map<String, Object> param);
	BmsOutPlan selectOutPlan31(Map<String, Object> param);
	Map<String, Object> selectBmsInMinusStock(Map<String, Object> param);
	int updateInNoOfOutPlan(BmsOutPlan bmsOutPlan);
	int updateInNoOfBmsOut(Map<String, Object> param);
	void deleteBmsOut(BmsOut bmsOut);
	BmsOutPlan selectBmsOutPlanCheck(BmsOut bmsOut);
	
	void deleteBmsOutPlan(BmsOutPlan bmsOutPlan);
	
	int insertBmsOutSample(BmsOutSample bmsOutSample);
	int updateBmsOutSample(BmsOutSample bmsOutSample);
	int updateReturnDateFromSample(BmsOutSample bmsOutSample);
	void deleteBmsOutSample(BmsOutSample bmsOutSample);

	void deleteOutContainerByNo(BmsOut bmsOut);

	int insertOutContainer(BmsContainer bmsContainer);

	int updatePkgSampleInfo(Map<String, Object> param);
	
	 /*
	*//***
	 * 폐기 신청시 에 DISPOSAL_YN : Y
	 * 폐기 삭제 처리시에 DISPOSAL_YN : N
	 * @param in
	 *//*
	void updateBmsInDisposalYn(InventoryIn in);
     // 폐기 신청 일괄 생성
	Integer selectDisposalBatchCheckCount(InventoryIn inventoryIn);
	Disposal selectRecentDisposalData();
	void insertDisposalBatchGen(Disposal disposal);

	// 폐기 반출 일괄 생성
	Integer selectOutBatchCheckCount(Disposal disposal);

	void insertOutBatchGen(Disposal disposal);
	void updateInCtWtCbm(Disposal disposal);
	void updateBmsDisposalOutYn(Disposal disposal);
*/
	void updateTicOutSeqFromBmsOut(Map<String, Object> map);
	
	void updateInCtWtCbm(BmsOut bmsOut);
	void updateOutPlanApprovalCbm(BmsOut bmsOut);
	void updateBmsPlanOutYn(BmsOut bmsOut);
	List<FtpOut> selectBmsOutFtpList(SearchBmsOut search);
	/** FTP 전송성공 업데이트 */
	void update61BmsOutFtpSendY(Map<String, Object> map);

	Integer selectOutBatchCheckInCount ( BmsOut bmsOut);
	BmsOut selectLatest61CarryOut(BmsOut bmsOut);
	void insertFromOutToInBatchGen(BmsOut bmsOut);
	int updateLastPodStatusFrom21BmsIn(Map<String, Object> map);
	int updateBmsOutSetInNo(Map<String, Object> param);
	InventoryIn selectBmsInByInNo(Map<String, Object> param);
	InventoryIn selectBmsInFirstInByCargoNo(Map<String, Object> param);
	int updateBmsInRestoreBeforeBmsOut(Map<String, Object> param);
	int updateTicOutBackInFinish(Map<String, Object> map);
	int insert53BmsOutBMSA3(BmsOut bmsOut);
	int insertBmsEcmPod(BmsEcmPod ecmPod);
	int insertOutPlanTempForHms(Map<String, Object> map);

	/** 선반입반출신고독려통보 조회 */
	List<BmsEncourageNoti> selectBmsEncourageNotiList(SearchBmsOut search);
	
	String selectChangeDelInNoByCustomsReportNo(String customsReportNo);
	String selectErrorDesc(String srchDeclarationNo,String srchDocType); /** 등록 ENTRY - 오류내역 조회 */
	
	EcmCustomer selectCustomerInfoByBusNo(SearchBmsOut search);
	
	Integer selectBmsLocalInspectResultByInNo ( BmsOut bmsOut); // 특송창고 검사결과 조회
	int insertBmsLocalInspectResult( BmsOut bmsOut); // 특송창고 검사결과 저장
	int deleteBmsLocalInspectResult(BmsOut bmsOut); // 특송창고 검사결과 삭제
	BmsLocalInspectResult selectOneInspectResultListOfBmsOut(BmsOut bmsOut);
	
	BmsInOut selectCustomsReportNoOrCargoNoFromInOut(SearchBmsInOut bmsOut);
	
	int insertBmsAllocateCarNotiByInClass( Map<String, Object> map); // 이고반입 배차예정정보 저장
	
	void updateBmsOutPlanNo(BmsOut bmsOut);
	void updateBmsOutLastPodStatus(BmsOut bmsOut);
	Map<String, Object> selectBmsOutByOutNo(Map<String, Object> map);
	Map<String, Object> selectBmsOutWithBmsIn(Map<String, Object> map);
	List<Map<String, Object>> selectPkgListByInPlanNo(Map<String, Object> map);
	int selectCheckBmsOut61Count(Map<String, Object> map);
	
	List<BmsOutDelivery> selectDeliveryList(SearchBmsOut search); // 택배사연계 조회
	int updateDeliverySeq(Map<String, Object> map);	  // 택배사연계 재전송
	
	int updateBmsOutScanDateTime(int outNo);
	int updateBmsOutPlanScanDateTime(int outPlanNo);
	
	int updateBmsOutCustomsInspect(Map<String, Object> param);	// 세관검사장(BMSA2) 변경
	
	BmsOut selectBmsOutByOutNoOrOdJobNo(Map<String, Object> search);	// 접수통보후반출수정
	int updateBmsOutCtWt(Map<String, Object> search);
	Map<String, Object> getSumOutCtWtGroupBmsOut(Map<String, Object> search);
	Map<String, Object> getSumOutCtWtGroupBmsOutWithout(Map<String, Object> search);
	Map<String, Object> getInCtWtBmsOut(Map<String, Object> search);
	int updateStockAndCtWtForChange(Map<String, Object> param);
	int updateOutYnFromOutPlan(Map<String, Object> param);
	Map<String, Object> getBmsOutChangeDelByOutNo(Map<String, Object> param);
	int insertBmsOutChangeDelByOutNo(Map<String, Object> param);
	int updateBmsOutScanResult(BmsOut bmsOut);
	
	Map<String, Object> selectCargoByinNo(String cargoRefNo);
	int selectCargoByOutPlanNo(String cargoRefNo);
	int updateNo(Map<String, Object> map);
}
