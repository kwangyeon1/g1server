package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsCargo.BmsCargo;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.out.BmsOut;

@Mapper
public interface PdaCargoMapper {

	Map<String, Object> selectPkgInfo(Map<String, Object> search);
	List<BmsCargo> selectCargoTracking(Map<String, Object> cargoMap);
	List<Map<String, Object>> selectInspectResultNotFinished(Map<String, Object> cargoMap);
	List<Map<String, Object>> selectHoldInfo(Map<String, Object> cargoMap);
	int selectHoldInfoCnt(Map<String, Object> cargoMap);
	int selectExistDivideCount(Map<String, Object> cargoMap);
	List<Map<String, Object>> selectCargoTrackingBox(Map<String, Object> search);
	int updateHold(Map<String, Object> search);
	int insertPodHistory(Map<String, Object> search);
	Map<String, Object> selectCargoPrintNo(Map<String, Object> cargoMap);
	CarryIn selectInitBmsInInfo(Map<String, Object> map);
	BmsOut selectInitBmsOutInfo(Map<String, Object> map);
	Map<String, Object> selectEntBusOptionDate(Map<String, Object> search);
	void insertReStockInData(CarryIn initBmsIn);
	void insertReStockOutData(BmsOut initBmsOut);
	String selectSortHistoryMsg(Map<String, Object> search);
	String selectXrayPassTime(Map<String, Object> search);
	String selectAdviceWork(Map<String, Object> search);
}