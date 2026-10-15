package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsPkgNo.search.SearchBmsPkgNoList;
import org.yht.exps.express.model.mobile.bms.MobPalo;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwlo;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;

@Mapper
public interface PdaLocationPaloMapper {
	public List<Map<String, Object>> selectLocationPlace(SearchInList search);
	int updateOrigLocation(Map<String, Object> search);
	
	public List<MobPalo> selectPkgNoInfo(SearchMobBwlo search);
	public MobPalo selectPkgNoInfoNew(SearchMobBwlo search);
	public MobPalo selectPkgNoInfoNew13F(SearchMobBwlo search);
	List<MobPalo> selectPkgNoByPlanNoAndBlNo(SearchMobBwlo search);
	List<MobPalo> selectPkgNoByPlanNoAndBlNo13F(SearchMobBwlo search);
	public List<MobPalo> selectEmptyLocation(SearchBmsPkgNoList search);
	int updateBmsInAndInPlanPkgLocationInfo(Map<String, Object> search);
	int updateBmsInAndInPlanPkgLocationInfo2(Map<String, Object> search);
	int updateBmsInLocationInfo(Map<String, Object> search);
	int updateLocationFromInPlanPkg(Map<String, Object> search);
	int updateInPlanPkgLocationInfo(Map<String, Object> search);
	int batchUpdateInPlanPkgLocationInfo(Map<String, Object> search);
	public Map<String, Object> selectBmsInHoldInfo(SearchBmsPkgNoList search);
	public Map<String, Object> selectBmsInInspectionInfo(SearchBmsPkgNoList search);
	public List<Map<String, Object>> selectScanLogInfo(Map<String, Object> search);
	
	Map<String, Object> selectLocationInfoDetail(Map<String, Object> search);
	List<InPlanPkg> selectPkgWithBmsInByLocation(Map<String, Object> search);
	List<InPlanPkg> selectPkgWithBmsInByPackingNoList(Map<String, Object> search);
	InPlanPkg selectOnePkgWithBmsInByPackingNo(Map<String, Object> search);
	InPlanPkg selectLocationFromPkg(Map<String, Object> search);
	List<InPlanPkg> selectLocationFromAllPkgByScan(Map<String, Object> search);
	
	int insertLocationScanLog(Map<String, Object> search);
	int updateInLocationInfo(Map<String, Object> logParam);
	
	int insertLocation(Map<String, Object> search);
	
	int getCntPkgOutCt(SearchMobBwlo search);
	
	int getCntOutDateTimeFromBmsOut(SearchMobBwlo search);
	
	List<MobPalo> getPkgPaloListByCurrentLocation(Map<String, Object> search);
	MobPalo getPkgPaloByPkgNo(Map<String, Object> search);
	
	int updatePkgLocationFromCurrentToTarget(Map<String, Object> search);
	int updatePkgLocationInSeqList(Map<String, Object> search);
	int updatePkgLocationAndOrigLocation(Map<String, Object> search);
	int	updateOnlyOrigLocation(Map<String, Object> search);
	List<MobPalo> getPkgPaloListByOrigLocation(Map<String, Object> search);
	int updatePkgOrigLocationInSeqList(Map<String, Object> search);
}
