package org.yht.exps.express.mapper.bms;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsCargo.BmsCargoHistory;
import org.yht.exps.express.model.bms.bmsCargo.BmsCargoInfo;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutPlan;

@Mapper
public interface BmsCargoMapper {

	BmsCargoInfo selectCarryIn(Map<String, Object> map);
	
	List<BmsCargoInfo> selectCarryOut(Map<String, Object> map);
	
	List<InPlanPkg> selectPkg(Map<String, Object> map);
	
	InPlanPkg selectPkgByPkgNo(Map<String, Object> map);
	
	Map<String, Object> selectHoldInfo(Map<String, Object> map);

	List<BmsInspectResult> selectInspectResultNotCompleteByInNo(Map<String, Object> map);
	
	BmsInspectResult selectInspectResultCompleteByInNoAndType(Map<String, Object> map);
	
	Map<String, Object> selectXray(Map<String, Object> map);
	
	String selectSortHistoryMsg(Map<String, Object> search);
	
	String selectXrayPassTimeByInPlanNo(Map<String, Object> search);
	
	List<CarryIn> selectInList(Map<String, Object> search);
	
	List<BmsOut> selectOutList(Map<String, Object> search);
	
	List<BmsOutPlan> selectOutPlanList(Map<String, Object> search);
	
	List<BmsCargoHistory> selectCargoInOutHistory(Map<String, Object> search);
	
}
