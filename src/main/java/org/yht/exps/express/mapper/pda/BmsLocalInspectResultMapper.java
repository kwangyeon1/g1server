package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.exps.express.model.mobile.bms.BmsInForPda;
import org.yht.exps.express.model.mobile.bms.BmsLocalInspectResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMblFromInspectResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMobInspectResult;

@Mapper
public interface BmsLocalInspectResultMapper {
	
	List<BmsOut> selectBmsOutByBlNo(SearchMobInspectResult search);
	
	CarryIn selectBmsInByBlNo(SearchMobInspectResult search);

	BmsLocalInspectResult selectBmsLocalInspectResultByBlNo(SearchMobInspectResult search);
	
	BmsLocalInspectResult selectTicData(SearchMobInspectResult search);
	
	BmsInForPda selectBmsInDetailByBlNo(SearchMobInspectResult search);
	
	BmsOutPlan selectOneBmsOutPlan(SearchMobInspectResult search);
	
	void updateBmsLocalInspectResult(SearchMobInspectResult search);
	
	void updateBmsTicComebackLocalInspectResult(SearchMobInspectResult search);
	
	void updateScanDateBmsOutPlan(Integer outPlanNo);
	
	List<BmsLocalInspectResult> selectMblList();
	
	List<BmsLocalInspectResult> selectInspectResultFilteredByCodeGroup(SearchMblFromInspectResult search);
	
	void updateInspectResultByInspectionKeyword(BmsLocalInspectResult inspectResult);
	
	List<BmsLocalInspectResult> getSelectedInspectResultList(List<BmsLocalInspectResult> list);
	
	void updateInspectionFinishedYn(Map<String, Object> param);
	
	CarryIn selectBmsInByInNo(Map<String, Object> param);
	
}