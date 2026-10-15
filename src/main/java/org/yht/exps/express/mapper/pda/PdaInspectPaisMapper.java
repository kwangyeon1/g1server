package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.BmsInspectResultRst;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;

@Mapper
public interface PdaInspectPaisMapper {
	
	List<InPlanPkg> selectPkgWithBmsIn(Map<String, Object> search);
	
	List<BmsInspectResult> selectBmsInWithInspectResult(Map<String, Object> search);
	
	List<BmsInspectResult> selectBmsInWithInspectResultV2(Map<String, Object> search);
	
	List<BmsInspectResult> selectBmsInWithInspectResultByInPlanNoList(Map<String, Object> search);
	
	List<BmsInspectResult> selectBmsInspectResultPicking(Map<String, Object> search);
	List<InPlanPkg> selectBmsInForAddInspectResult(Map<String, Object> search);
	int updateBmsInPlanPkgInspectStatus(Map<String, Object> search);
	int updateBmsInspectResultStartInfo(Map<String, Object> search);
	List<Map<String, Object>> selectBmsInspectScanLogInfo(Map<String, Object> search);
	int insertBmsInspectScanLog(Map<String, Object> search);
	
	
	Map<String, Object> selectOneBmsInByPackingNo(Map<String, Object> search);
	Map<String, Object> selectOneBmsInByPackingNoV2(Map<String, Object> search);
	
	int insertInspectScanLog(Map<String, Object> search);
	
	InPlanPkg selectPkgByPackingNo(Map<String, Object> search);
	
	int checkAllPkgNotInspectE(Map<String, Object> search);
	
	int checkAllInspectResultNotFinish(Map<String, Object> search);
	
	List<InPlanPkg> selectPkgBmsInByInPlanNo(Map<String, Object> search);
	
	List<BmsInspectResult> selectInspectResultNotFinished(Map<String, Object> search);
	
	int updateInspectStatusOfPkg(Map<String, Object> search);
	
	int updateInspectStatusOfAllPkg(Map<String, Object> search);
	
	int updateInspectResultFinishByInspectionTypeList(Map<String, Object> search);
	
	int updateBmsInAllInpectionFinishYnByInNoList(Map<String, Object> search);
	
	int updateInspectResultFinishWhenExistType(Map<String, Object> search);
	
	int updateBmsInAndPlanAboutInspection(Map<String, Object> search);
	
	Map<String, Object> selectBmsInByInPlanNo(Map<String, Object> search);
	
	int updateInspectResultStatusInit(Map<String, Object> search);
	
	List<BmsInspectResult> selectInspectResultCodeValueByInNo(Map<String, Object> search);
	List<BmsInspectResult> selectInspectResultCodeValueByInPlanNoOrInNo(Map<String, Object> search);
	
	int insertInspectResult(Map<String, Object> search);

	List<BmsInspectResult> selectInspectResult(Map<String, Object> search);
	void updateInspectResultRemark(Map<String, Object> search);
	void updateInspectResultRemarkByKey(Map<String, Object> search);
	void updateBmsInAllInspectionFinishYn(Map<String, Object> search);
	
	List<String> selectInspectionTypeByTicInspectResultCode();
	List<String> selectInspectionTypeAll();
	
	List<Map<String, Object>> selectInspectResultByInNo(Map<String, Object> search);
	List<Map<String, Object>> selectNotFinishedInspectResultByInPlanNo(Map<String, Object> search);
	
	Map<String, Object> selectBmsIn21ByInPlanNo(Map<String, Object> search);
	
	Map<String, Object> selectBmsInsInNo(Map<String, Object> search);
	Map<String, Object> selectBmsInsOutNo(Map<String, Object> search);
	int updateInsComplete(Map<String, Object> search);
	List<BmsInspectResult> selectBmsInsCompleteYn(Map<String, Object> search);
	int updateInsCompleteYn(Map<String, Object> search);
	
	Map<String, Object> selectBmsInByCargoNo(Map<String, Object> search);
	Map<String, Object> selectBmsInByInNoOrInPlanNo(Map<String, Object> search);
	int updateInspectResultFinishByTicType(Map<String, Object> search);
}
