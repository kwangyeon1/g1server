package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsStock.BmsStock;
import org.yht.exps.express.model.bms.bmsStock.search.SearchStock;

import java.util.List;
import java.util.Map;

@Mapper
public interface BmsStockInspectionMapper {
    List<BmsStock> selectStockInspectionList(SearchStock search);
    
    List<BmsStock> selectStockInspectionListForPantk(SearchStock search);

    void insertExcelUploadStock(Map<String, Object> excelStock);

    void deleteStockInspectionTemp();
    
    Map<String, Object> selectCargoStatus(BmsStock bmsStock);
}
