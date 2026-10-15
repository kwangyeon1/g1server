package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsAbnormal.AbnormalSpCallVo;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalHbl;
import org.yht.exps.express.model.bms.bmsAbnormal.search.SearchCarryInAbnormal;
import org.yht.exps.express.model.bms.carryin.InPlan;

import java.util.List;

@Mapper
public interface BmsAbnormalMapper {

    List<CarryInAbnormal> selectAbnormalList(SearchCarryInAbnormal search);

    List<InPlan> selectInPlanList(SearchCarryInAbnormal search);

    int deleteAbnormalMaster(int abnNo);

    int deleteAbnormalHBL(int abnNo);

    int checkExistAbnormal(CarryInAbnormal carryInAbnormal);

    int checkExistInPlan(int inPlanNo);

    List<CarryInAbnormal> selectAbnormalHblList(SearchCarryInAbnormal search);
    List<CarryInAbnormal> selectAbnormalHblNotInList(SearchCarryInAbnormal search);

    CarryInAbnormal selectDetailAbnormal(SearchCarryInAbnormal search);

    List<CarryInAbnormalHbl> selectAbnormalHblListByMBL(SearchCarryInAbnormal search);

    int insertAbnormal(CarryInAbnormal carryInAbnormal);

    int updateAbnormal(CarryInAbnormal carryInAbnormal);

    void callProcBMSAbnormalSeq(AbnormalSpCallVo voIn);

    int checkExistAbnormalBl(CarryInAbnormal carryInAbnormal);

    int insertAbnormalHbl(CarryInAbnormalHbl carryInAbnormalHbl);

    int selectAbnNoByCustomsReportNo(String customsReportNo);

    String selectMblNoByBlNo(String srchNo);
}
