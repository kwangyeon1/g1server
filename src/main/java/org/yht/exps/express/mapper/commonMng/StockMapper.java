package org.yht.exps.express.mapper.commonMng;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.commonMng.stock.DeleteStock;
import org.yht.exps.express.model.commonMng.stock.InsertStock;
import org.yht.exps.express.model.commonMng.stock.RegrantStock;
import org.yht.exps.express.model.commonMng.stock.Stock;
import org.yht.exps.express.model.commonMng.stock.VoidStock;
import org.yht.exps.express.model.commonMng.stock.search.SearchStockDetail;
import org.yht.exps.express.model.commonMng.stock.search.SearchStockList;
import org.yht.exps.express.model.commonMng.stock.search.SearchVoidStockList;

@Mapper
public interface StockMapper {
	
	List<Stock> selectStockList(SearchStockList search);
	void insertStock(InsertStock insert);
	void deleteStock(DeleteStock delete);
	Stock selectStockDetail(SearchStockDetail search);
	List<VoidStock> selectVoidStockList(SearchVoidStockList search);
	void updateStockUseYn(InsertStock stock);
	void updateStock(InsertStock insert);
	void regrantStock(RegrantStock regrant);
	int checkStockDuplication(RegrantStock regrant);
	
	int checkReStockDuplication(RegrantStock regrant);
	
	List<Stock> selectStockListV2(SearchStockList search);
}
