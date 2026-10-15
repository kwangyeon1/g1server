package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChange;
import org.yht.exps.express.model.bms.bmsInCorrection.ChangeItem;
import org.yht.exps.express.model.bms.bmsInCorrection.DeleteCarryInChange;
import org.yht.exps.express.model.bms.bmsInCorrection.search.SearchInCorrection;
import org.yht.exps.express.model.bms.carryin.CarryIn;

import java.util.List;

@Mapper
public interface BmsInCorrectionMapper {
    List<CarryIn> selectCarryInList(SearchInCorrection search);

    List<CarryInChange> selectCarryInChangeList(SearchInCorrection search);

    CarryInChange selectCarryInChangeDetail(SearchInCorrection search);

    CarryIn selectCarryInDetail(SearchInCorrection search);

    List<ChangeItem> selectChangeItemListByChangeNo(SearchInCorrection search);

    int insertCarryInChange(CarryInChange carryInChange);

    int updateCarryInChange(CarryInChange carryInChange);

    int insertChangeItem(ChangeItem changeItem);

    int selectChangeNo(CarryInChange carryInChange);

    void deleteCarryInChange(CarryInChange carryInChange);

    void deleteCarryInChangeItem(ChangeItem changeItem);

    int deleteChangeItem(int changeNo);

    String selectNotificationMsg(String customsReportNo, String changeCount, String lastPodStatus);
}
