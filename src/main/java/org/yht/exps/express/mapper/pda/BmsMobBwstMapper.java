package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsStock.BmsStockPkgScan;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.exps.express.model.mobile.bms.MobBwst;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwst;

@Mapper
public interface BmsMobBwstMapper {

	public String selectInitStockScanInfo();
	public void insertStockScanInfo(Map<String, Object> map);
	
	public void updateStockScanInfo(Map<String, Object> map);
	public BmsStockPkgScan selectStockInfo(Map<String, Object> search);
	public void insertStockPkgScan(BmsStockPkgScan stockPkgScan);
	public int insertBmsEcmPodForBatch(Map<String, Object> map);
	public List<Map<String, Object>> selectScanLogInfo(Map<String, Object> search);
	public MobBwst selectRealStockCarryInDetail(SearchMobBwst search);
	public int insertRealStockScan(SearchMobBwst search);
	
	Map<String, Object> insertStockPkgScanBySortResult(Map<String, Object> map);
	
	Map<String, Object> selectLocationInfoDetail(Map<String, Object> search);
	List<BmsStockPkgScan> selectScanLogInfoByLocation(Map<String, Object> search);
	List<BmsStockPkgScan> selectPkgWithLogByLocation(Map<String, Object> search);
	BmsStockPkgScan selectStockInfoByLocation(Map<String, Object> search);
	Integer checkPkgHasBmsIn(Map<String, Object> search);
	BmsOutPlan selectOutPlanInfoByPkgNo(Map<String, Object> search);
	
	Integer selectCurrentStockScanSeq();
	int checkScannedByScanDate();
	
	Map<String, Object> getStockScanStartEnd();
}
