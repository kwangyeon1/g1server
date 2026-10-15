package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChange;
import org.yht.exps.express.model.bms.bmsInCorrection.ChangeItem;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOut;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChange;
import org.yht.exps.express.model.bms.bmsOutCorrection.search.SearchOutCorrection;

import java.util.List;

@Mapper
public interface BmsOutCorrectionMapper {
    List<CarryOut> selectCarryOutList(SearchOutCorrection search);

    List<CarryOutChange> selectCarryOutChangeList(SearchOutCorrection search);

    CarryOutChange selectCarryOutChangeDetail(SearchOutCorrection search);

    CarryOut selectCarryOutDetail(SearchOutCorrection search);

    List<ChangeItem> selectChangeItemListByChangeNo(SearchOutCorrection search);

    int insertCarryOutChange(CarryOutChange carryOutChange);

    int updateCarryOutChange(CarryOutChange carryOutChange);

    int selectChangeNo(CarryOutChange carryOutChange);

    int insertChangeItem(ChangeItem changeItem);

    int deleteChangeItem(int changeNo);

    void deleteCarryOutChange(CarryOutChange carryOutChange);

    void deleteCarryOutChangeItem(ChangeItem changeItem);
}
