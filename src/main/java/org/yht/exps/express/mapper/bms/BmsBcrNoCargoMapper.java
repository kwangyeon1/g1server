package org.yht.exps.express.mapper.bms;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsBcrNoCargo.BmsBcrNoCargo;
import org.yht.exps.express.model.bms.bmsBcrNoCargo.search.SearchBmsBcrNoCargoList;

@Mapper
public interface BmsBcrNoCargoMapper {

	String selectMaxScanDateTime();
	List<BmsBcrNoCargo> selectBcrNoCargoList(SearchBmsBcrNoCargoList search);
	void updateBcrNoCargo(Map<String, Object> map);
	List<Map<String, Object>> selectBcrCargoTransferList(Map<String, Object> paramMap);
	List<Map<String, Object>> selectBcrCargoTransferListPantos(Map<String, Object> paramMap);
	void insertBcrCheck(Map<String, Object> map);
}
