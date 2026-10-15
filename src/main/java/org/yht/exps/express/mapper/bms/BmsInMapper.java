package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsComm.BmsEcmPod;
import org.yht.exps.express.model.bms.carryin.BmsInPlanContainer;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.Container;
import org.yht.exps.express.model.bms.carryin.InPlan;
import org.yht.exps.express.model.bms.carryin.InPlanCheckResult;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.bms.carryin.OdAbnormal;
import org.yht.exps.express.model.bms.carryin.WithHolding;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.model.bms.disposal.SpCallVo;
import org.yht.exps.express.model.bms.out.BmsOut;

import java.util.List;
import java.util.Map;

@Mapper
public interface BmsInMapper {
	//반입예정조회
	List<InPlan> selectInPlanList(SearchInList search);
	List<InPlan> selectInPlanList2(SearchInList search);
	//반입조회
	List<CarryIn> selectCarryInList(SearchInList search);
	List<CarryIn> selectCarryInList2(SearchInList search);
	List<CarryIn> selectCarryInList3(SearchInList search);
	//박스조회
	List<InPlanPkg> selectInPlanPkgList(SearchInList search);
	List<InPlanPkg> selectInPlanPkgList2(SearchInList search);
	
	List<Map<String, Object>> selectXrayResultForDownload(SearchInList search);
	
	//반입예정 개별 1건 조회
	InPlan selectDetailInPlan(SearchInList search);

	//반입예정 컨테이너 리스트 정보조회
	List<Container> selectContainerListByInPlanNo(String inPlanNo);

	//반입 개별 1건 조회
	CarryIn selectDetailCarryIn(SearchInList search);
	
	//반입 개별 1건 조회
    int updateCtWtByInNo(Map<String, Object> search);

	//반입 컨테이너 리스트 정보조회
	List<Container> selectContainerListByInNo(String inNo);

    int updateCarryIn(CarryIn carryIn);

	//반입신고 -insert
    int insertCarryIn(CarryIn carryIn);

	int insertHandInputCarryIn(CarryIn carryIn);

	void updateBmsInPlanInYn(InPlan inPlan);

	void insertContainerInfo(Container container);
	
	List<BmsEcmPod> selectPodSelect(Map<String, Object> param);

	//반입테이블에 중복되는 화물관리번호가 있늕니 체크
	int checkCargoReferenceNo(String cargoReferenceNo);

	//반입등록시 반입예정, 반입테이블에 중복되는 화물관리번호 있는지 체크
	int checkCargoReferenceNoFromInAndInPlan(String cargoReferenceNo);
	
	//반입예정 데이터 삭제
	void deleteInPlan(InPlan inPlan);
	
	//반입예정_PKG (TB_BMS_IN_PLAN_PKG) 데이터 삭제
	void deleteInPlanPkg(InPlan inPlan);
	
	void updateBmsInPlanYn(Map<String, Object> map);

	//반입 or 재고 데이터 삭제
	void deleteCarryIn(CarryIn carryIn);
	
	//박스 삭제 전 체크용 조회 (SEQ 기준)
	InPlanPkg selectInPlanPkgBySeq(Map<String, Object> map);
	int softDeleteInPlanPkg(Map<String, Object> map);
	int softDeleteAllInPlanPkgByInPlanNo(Map<String, Object> map);

	InPlanCheckResult checkInPlanBeforeDelete(Object inPlanNo);

	int checkCarryInBeforeDelete(String inNo);

	//컨테이너 정보삭제
	int deleteContainerInfo(CarryIn carryIn);

	Integer selectCarryInBatchCheckOutCount(CarryIn carryIn);
	//오늘, 반출에서 이고차수 최대값가져오기
	Integer selectTodayMaxTicOutSeqFromBmsOut();
	
	// 반출구분 70인 반출 생성
	Integer selectBatchCheckOutCountByOutClass(CarryIn carryIn);
		
	Integer selectCarryInBatchCheckStockCount(CarryIn carryIn);
	Integer selectCarryInBatchCheckReceiptCount(CarryIn carryIn);
	Map<String, Object> selectCarryInOutPlanRemainQty(Map<String, Object> map);
	
	Integer selectCheckBMSA4out(Map<String, Object> map);
	Integer sumWhenFinalBmsOutOrPlan(Map<String, Object> map); // 화물관리번호에 해당하는 완전출고된것들을 SUM (하지만 이고혹은 부분출고승인 혹은 부분출고는 no sum)
	Integer selectCheckBmsIn21Count(CarryIn carryIn); // 화물관리번호에 해당하는 이고반입 count
	Integer selectCheck61CarryOutByInNo(CarryIn carryIn);
	int deleteOnlyTicInspectResult(Map<String, Object> map); //  반입번호에 해당하는 이고관련 검사결과만 전부 삭제
	int deleteTicIn(Map<String, Object> map); // 화물관리번호에 해당하는 21번 이고반입 삭제
	int deleteTicOut(Map<String, Object> map); // 화물관리번호에 해당하는 61번 이고반출 삭제
	int updateTicPkgInit(Map<String, Object> map); // IN_PLAN_NO에 해당하는 박스의 TIC 이고상태를 초기화 update
	int updateTicInInit(Map<String, Object> map); // 반입번호에 해당하는 반입 이고관련 칼럼 초기화
	int updateTicLogFinishedY(Map<String, Object> map);
	int insertTicInByCargoNo(Map<String, Object> map); // 화물관리번호에 해당하는 21번 이고반입을 history에 insert
	int insertTicOutByCargoNo(Map<String, Object> map); // 화물관리번호에 해당하는 61번 이고반출을 history에 insert
	void insertCarryInOutBatchGen(CarryIn carryIn);
	int updateCalcBmsInStockWithBmsOut(Map<String, Object> map);
	void insertCarryInOutBatchGenPantk(CarryIn carryIn);
	// 반출구분 70인 반출 생성
	void insertCarryOut70BatchGen(CarryIn carryIn);
	void updateCarryInCtWtCbm(CarryIn carryIn);


	String selectInNoByInPlanNo(Integer inPlanNo);
	String selectChangeDelInNoByCustomsReportNo(String customsReportNo);
	String selectInNoByCustomsReportNo(String customsReportNo);

	int checkExistCarryIn(int inPlanNo);

	int checkExistInPlan(int inPlanNo);

    String selectErrorNotification(String customsReportNo);

	int insertInPlan(InPlan inPlan);
	
	int insertInPlanForXxxUpload(InPlan inPlan);

	int insertBmsInPlanContainer(BmsInPlanContainer bmsContainer);
	
	/** 반입신고번호 생성 프로시저 호출*/
    void callProcBMSInSeq(SpCallVo voIn);

	/** MFN(화물있으나적하목록없음) 자료, TB_ECM_OD_ABNORMAL 테이블에 존재 여부 체크*/
	int checkExistMfn(CarryIn carryIn);
	/** 하기이상정보(TB_ECM_OD_ABNORMAL)에 MFN(화물있으나적하목록없음), MGN(적하목록있으나화물없음) 자료생성*/
	int insertOdAbnormal(OdAbnormal odAbnormal);
	/**MFN 자료 생성 후, 반입(TB_BMS_IN)데이터 MFN_ISSUE_YN Y로 업데이트*/
	// int updateMfnIssueYn(int inNo);

	/** MGN(적하목록있으나화물없음) 자료, TB_ECM_OD_ABNORMAL 테이블에 존재 여부 체크*/
	int checkExistMgn(CarryIn carryIn);

	/**MGN 자료 생성 후, 반입(TB_BMS_IN)데이터 MGN_ISSUE_YN Y로 업데이트*/
	int updateMgnIssueYn(int inNo);
	
	/**MFN 자료 생성 후, 반입예정(TB_BMS_IN_PLAN)데이터 MFN_ISSUE_YN Y로 업데이트*/
	// int updateMfnIssueYnByInPlan(int inPlanNo);
	/**MGN 자료 생성 후, 반입예정(TB_BMS_IN_PLAN)데이터 MGN_ISSUE_YN Y로 업데이트*/
	int updateMgnIssueYnByInPlan(int inPlanNo);

	/** 보류리스트 조회*/
    List<WithHolding> selectWithholdingList(SearchInList search);
	
    int updateIprFromBmsIn(Map<String, Object> map);
    int updateIprBmsInPlanWhereHasBmsIn(Map<String, Object> map);
    int updateIprFromBmsInPlan(Map<String, Object> map);
    int updateIprBmsInWhereHasBmsInPlan(Map<String, Object> map);
    
    int updateBigSizeYnFromBmsIn(Map<String, Object> map);
    int updateBigSizeYnBmsInPlanWhereHasBmsIn(Map<String, Object> map);
    int updateBigSizeYnFromBmsInPlan(Map<String, Object> map);
    int updateBigSizeYnBmsInWhereHasBmsInPlan(Map<String, Object> map);
    
    int updateIsptCustomsYnFromBmsIn(Map<String, Object> map);
    int updateIsptCustomsYnBmsInPlanWhereHasBmsIn(Map<String, Object> map);
    int updateIsptCustomsYnFromBmsInPlan(Map<String, Object> map);
    int updateIsptCustomsYnBmsInWhereHasBmsInPlan(Map<String, Object> map);
    
	/** 반입자료 삭제 후 IN_YN: C으로 변경 (판토스) */
	int updateInPlanInYnC(CarryIn carryIn);
	
	/** 오더주문번호(OD_JOB_NO)를 통해 반입,반입예정의 hbl,mbl,화물관리번호등을 조회 */
	List<CarryIn> selectNoInfoCarryInWithPlan(Map<String, Object> map);
	
	/** 일괄반입자료생성 */
	Integer selectCarryInBatchCheckInPlanCount(CarryIn carryIn);
	void insertFromInPlanToInBatchGen(CarryIn carryIn);
	
	Integer selectBmsLocalInspectResultByInNo ( CarryIn carryIn); // 특송창고 검사결과 조회
	int insertBmsLocalInspectResult( CarryIn carryIn); // 특송창고 검사결과 저장
	
	int insertBmsAllocateCarNotiByOutClass( Map<String, Object> map); // 이고반출 배차예정정보 저장
	Integer checkCarryInBatchTicCount(Map<String, Object> carryIn);
	Integer checkCarryInBigsizeCount(CarryIn in); // 반입의 거대_지재_집중_우범_체크
	Integer checkCarryInBatchTicCount2(CarryIn in); // inspectResult체크
	
	List<Map<String, Object>> getXrayHistoryList(Map<String, Object> paramMap);
	List<Map<String, Object>> getBmsInWithTicInspectionCodeFromInList(Map<String, Object> paramMap);
	
	public BmsInspectResult selectInspectResultByInNoAndType(Map<String, Object> search);
	int insertInspectResult(Map<String, Object> search);
	int updateInspectResultByInNoAndType(Map<String, Object> search);
	
	CarryIn selectCarryInByInNoOrOdJobNoOrPackingNo(Map<String, Object> search);
	List<BmsOut> getOutListByInNo(Map<String, Object> search);
	Map<String, Object> getOutListByInNoOutCheck(Map<String, Object> param);
	Map<String, Object> getSumOutCtWtGroupBmsOut(Map<String, Object> search);
	int updateStockAndCtWtForChange(Map<String, Object> param);
	int updateInYnFromInPlan(Map<String, Object> param);
	int insertBmsInChangeDelByInNo(Map<String, Object> param);
	int insertBmsOutChangeDelByOutNo(Map<String, Object> param);
	int deleteBmsOut(Map<String, Object> param);
	int deleteBmsOutPlan(Map<String, Object> param);
	
	Map<String, Object> getBmsInChangeDelByInNo(Map<String, Object> param);
	Map<String, Object> getBmsOutChangeDelByOutNo(Map<String, Object> param);
	CarryIn selectBmsInLastPodResendCheck(Map<String, Object> param);
	Integer selectBmsOutLastPodCheck(Map<String, Object> param);
	int updateXrayResultResend(Map<String, Object> param);
	Map<String, Object> selectBmsXrayResultCheck(Map<String, Object> param);
	List<Map<String, Object>> selectBmsInLastPodResendCheckList(Map<String, Object> param);
	Integer selectBmsOutLastPodCheckList(Map<String, Object> param);
	Map<String, Object> selectBmsXrayResultCheckList(Map<String, Object> param);
	
	Map<String, Object> selectCargoByOutPlanNo(CarryIn carryIn);
	int updateNo(CarryIn carryIn);
	
	int updatePodStatus(Integer inNo);
	Map<String, Object> selectLastPodStatusByInNo(Integer inNo);
	int updateOutPodStatus(Integer outNo);
	
	int updatePodStatusClear(Integer inNo);
	int updateOutPodStatusClear(Integer outNo);
	
	CarryIn selectFirstBmsInByCargoNo(CarryIn carryIn);
	int updateOrigInPlanNoOfBmsInByInNo(CarryIn carryIn);
	int updatePkgTicMoveInByInPlanNo(Map<String, Object> param);
	
	Map<String, Object> selectHoldStatusByOdJobNo(Map<String, Object> param);
	int insertHoldOn(Map<String, Object> param);
	int updateHoldOnClear(Map<String, Object> param);
	
	int updateXrayCtClear(Integer inPlanNo);
	int updateCarryInXrayCtClear(Map<String, Object> search);

}
