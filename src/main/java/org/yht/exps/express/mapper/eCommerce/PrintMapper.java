package org.yht.exps.express.mapper.eCommerce;

import java.util.Map;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.print.PrintInfo;
import org.yht.exps.express.model.eCommerce.print.SearchPrintInfo;

@Mapper
public interface PrintMapper {
	
	List<PrintInfo> selectPrintInfoLabel(SearchPrintInfo search);
	
	void deletePrintFormUbi(SearchPrintInfo search);
	List<Map<String,Object>> getPrintFormItemList(SearchPrintInfo search);
	
	Map<String, Object> selectManualEmptyBarcodeList(Map<String, Object> map);
	Map<String, Object> selectManualBarcodeList(Map<String, Object> map);
	void insertPrintFormUbi(Map<String, Object> map);
	void insertHanjinPrintData(Map<String, Object> map);
}
