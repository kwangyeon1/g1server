package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.BmsTicMoveScan;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.bms.out.BmsOut;

@Mapper
public interface PdaTicPaliMapper {
	
	List<BmsInspectResult> selectBmsInRelatedTicBmsOut(Map<String, Object> search);
	List<BmsInspectResult> selectBoxRelatedTicBmsOut(Map<String, Object> search);
	
	List<InPlanPkg> selectBmsOutTicOutScanPicking(Map<String, Object> search);
	int selectCompletedOutScanCountByPackingNo(Map<String, Object> search);
	List<InPlanPkg> selectOneBmsInForNoOut(Map<String, Object> search);
	Map<String, Object> selectOneBmsInForNoOutStatus(Map<String, Object> search);
	int updateBmsInPlanPkgMoveOutCt(Map<String, Object> search);
	int updateOutScanDateFromBmsOut(Map<String, Object> search);
	int updateTicOutApprovalNo50FromBmsOut(Map<String, Object> search);
	int countDuplicatedTicMoveScanLog(Map<String, Object> search);
	int checkBmsPkgAndTicMoveScanCount(Map<String, Object> search);
	int updateBmsTicMoveOutScanStatus(Map<String, Object> search);
	int insertBmsTicMoveScanLog(Map<String, Object> search);

	BmsInspectResult selectBmsInRelatedTicBmsOutByPackingNo(Map<String, Object> search);
	BmsInspectResult selectBoxRelatedTicBmsOutByPackingNo(Map<String, Object> search);
	List<InPlanPkg> selectBoxPkgWhereTicStatus(Map<String, Object> search);
	
	BmsInspectResult selectTicBmsOutRelatedByPackingNo(Map<String, Object> search);
	
	int insertTicMoveScanLog(Map<String, Object> search);
	
	List<BmsInspectResult> selectInspectResultNotFinished(Map<String, Object> search);
	
	List<BmsInspectResult> selectAllInspectResultNotFinished(Map<String, Object> search);
	
	int countWaitPkgFromTicMoveScanLog(Map<String, Object> search);
	
	List<InPlanPkg> selectPkgList(Map<String, Object> search);
	
	Map<String, Object> selectWaitPkgFromTicMoveScanLog(Map<String, Object> search);
	
	int updateFinishTicMoveScanLogByInNo(Map<String, Object> search);
	int updateTicMoveScanLogByC(Map<String, Object> search);
	
	int updateFinishTicMoveScanLogByInNoAndOutNo(Map<String, Object> search);
	
	int updatePkgTicMoveInByInPlanNo(Map<String, Object> search);
	
	int updatePkgTicStatus(Map<String, Object> search);
	
	int updateBmsInAllInspectionFinishYn(Map<String, Object> search);

	/* 이고반출대상확인 */
	List<Map<String, Object>> selectBmsInWithInspectResult(Map<String, Object> search);
	Map<String, Object> selectSrchPkgInOutCt(Map<String, Object> search);
	String selectTicOutScanYn(Map<String, Object> search);
	void insertTicMoveScan(BmsTicMoveScan bmsTicMoveScan);
	int selectBmsTicMoveScanCount(Map<String, Object> searchMap);
	void updateTicMoveScan(BmsTicMoveScan bmsTicMoveScan);
	void updateBmsOutTicOutScan(Map<String, Object> searchMap);
	List<Map<String, Object>> selectScanLogInfo(Map<String, Object> search);
	
	int updateInspectResultFinishByInspectionResultType(Map<String, Object> search);
	int updateAllTicInspectResultFinish(Map<String, Object> search);
	
	BmsOut selectBmsOutByOutNo(int outNo);
}
