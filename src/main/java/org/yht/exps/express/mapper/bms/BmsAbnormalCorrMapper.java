package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalCorr;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalHblCorr;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.search.SearchCarryInAbnormalCorr;

import java.util.List;

@Mapper
public interface BmsAbnormalCorrMapper {
    List<CarryInAbnormalCorr> selectAbnormalCorrList(SearchCarryInAbnormalCorr search);

    List<CarryInAbnormal> selectAbnormalList(SearchCarryInAbnormalCorr search);

    int checkExistAbnormalCorr(SearchCarryInAbnormalCorr search);

    int checkExistAbnormal(SearchCarryInAbnormalCorr search);

    /**반입이상정정 Master입항정보 입력*/
    int insertAbnormalCorr(CarryInAbnormalCorr carryInAbnormalCorr);

    /**반입이상정정 House/Master Single 정보 입력*/
    int insertAbnormalHblCorr(CarryInAbnormalHblCorr carryInAbnormalHblCorr);

    int selectAbnChangeNo();

    CarryInAbnormalCorr selectDetailAbnormalCorr(SearchCarryInAbnormalCorr search);

    List<CarryInAbnormalHblCorr> selectAbnormalHblCorrList(SearchCarryInAbnormalCorr search);

    int updateAbnormalCorr(CarryInAbnormalCorr carryInAbnormalCorr);

    int deleteAbnormalCorr(int abnChangeNo);
    int deleteAbnormalHBLCorr(int abnChangeNo);

    int selectMaxChangeCount(SearchCarryInAbnormalCorr search);
}
