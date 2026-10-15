package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.mobile.bms.BmsOutReleaseResult;

@Mapper
public interface PdaOutReleasePaotMapper {
	
	List<BmsOutReleaseResult> selectExistBlDivideByBms5HF(Map<String, Object> search);
	List<BmsOutReleaseResult> selectAllResultForOutRelease(Map<String, Object> search);
	int updateOutScanDtFromBmsOutAndOutPlan(Map<String, Object> search);
	int updateOutScanDtFromBmsOutAndOutPlan2(Map<String, Object> search);
	int updateOutScanCtFromOutPlan(Map<String, Object> search);
	int updateOutCtFromBmsInPlanPkg(Map<String, Object> search);
	int insertBmsOutScanLog(Map<String, Object> search);
	int insertBmsOutScanLogSelectPkg(Map<String, Object> search);
	void deleteBmsOutScanLog(Map<String, Object> logMap);
	List<Map<String, Object>> selectScanLogInfo(Map<String, Object> search);
	void deleteStockPkgScan(Map<String, Object> logMap);
	int selectExistsScanOutCount(Map<String, Object> search);
	List<Map<String, Object>> selectOutPlanListCtByCargoNoAndOutClass(Map<String, Object> search);
	String selectNewCargoReferenceNoFromPkg(Map<String, Object> search);
	String selectXrayPassTime(Map<String, Object> search);
	
}
