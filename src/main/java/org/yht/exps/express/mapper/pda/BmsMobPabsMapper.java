package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsPkgNo.search.SearchBmsPkgNoList;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.mobile.bms.BmsBigSizeScan;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBigSizeScan;

@Mapper
public interface BmsMobPabsMapper {
	public List<InPlanPkg> selectPkgNoInfo(SearchBmsPkgNoList search);
	int selectCountBigSizeYnFromScanLog(Map<String, Object> search);
	public List<BmsBigSizeScan> selectScanLogInfo(SearchMobBigSizeScan search);
	public InPlanPkg selectOneInPlanPkg(Map<String, Object> search);
	public int insertBigSizeScanLog(BmsBigSizeScan bmsBigSizeScan);
	int batchInsertBmsInFromInPlan(Map<String, Object> search);
	int selectCountBmsInByInPlan(Map<String, Object> search);
	int updateBmsInPlanInYn(Map<String, Object> search);
	int updateInPlanPkg(Map<String, Object> search);
	int updateBigSizeYnFromBmsInAndInPlan(Map<String, Object> search);
	public BmsInspectResult selectInspectResultByTypeIsBigSize(Integer inNo);
	int insertInspectResult(Map<String, Object> search);
	public Map<String, Object> selectBmsInByInPlanNo(Map<String, Object> search);
	int updateInspectResultByTypeIsBigSize(Map<String, Object> search);
	
}
