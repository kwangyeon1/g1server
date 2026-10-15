package org.yht.exps.express.mapper.bms;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsEc.BmsEcData;
import org.yht.exps.express.model.bms.bmsEc.BmsEcEtc;
import org.yht.exps.express.model.bms.bmsEc.BmsEcEtcDetail;
import org.yht.exps.express.model.bms.bmsEc.BmsEcIn;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInDetail;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInPlan;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInPlanDetail;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInPlanRsgDetail;
import org.yht.exps.express.model.bms.bmsEc.BmsEcOut;
import org.yht.exps.express.model.bms.bmsEc.BmsEcOutDetail;
import org.yht.exps.express.model.bms.bmsEc.BmsEcOutPlan;
import org.yht.exps.express.model.bms.bmsEc.BmsEcSend;
import org.yht.exps.express.model.bms.bmsEc.search.SearchEc;

@Mapper
public interface BmsEcMapper {
	List<BmsEcInPlan> selectEcInPlanList(SearchEc search);
	List<BmsEcInPlanDetail> selectEcInPlanDetailList(SearchEc search);
	List<BmsEcInPlanRsgDetail> selectEcInPlanDetailRsgList(SearchEc search);
	List<BmsEcIn> selectEcInList(SearchEc search);
	List<BmsEcInDetail> selectEcInDetailList(SearchEc search);
	List<BmsEcEtc> selectEcEtcList(SearchEc search);
	List<BmsEcEtcDetail> selectEcEtcDetailList(SearchEc search);
	List<BmsEcOutPlan> selectEcOutPlanList(SearchEc search);
	List<BmsEcOut> selectEcOutList(SearchEc search);
	List<BmsEcOutDetail> selectEcOutDetailList(SearchEc search);
	
	String selectEcInInterfaceId(BmsEcData ecData);
	String selectEcEtcInterfaceId(BmsEcData ecData);
	String selectEcOutPlanInterfaceId(BmsEcData ecData);
	String selectEcOutInterfaceId(BmsEcData ecData);
	
	int updateEcInResend(BmsEcSend ecDataList);
	int updateEcEtcResend(BmsEcSend ecDataList);
	int updateEcOutPlanResend(BmsEcSend ecDataList);
	int updateEcOutResend(BmsEcSend ecDataList);
}
