package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsStock.BmsExcelStock;
import org.yht.exps.express.model.bms.bmsStock.BmsStock;
import org.yht.exps.express.model.bms.bmsStock.search.SearchStock;

import java.util.List;
import java.util.Map;

@Mapper
public interface BmsStockMapper {
    List<BmsStock> selectStockList(SearchStock search);
    List<BmsStock> selectStockList2(SearchStock search);
}
