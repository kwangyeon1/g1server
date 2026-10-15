package org.yht.exps.express.mapper.edi;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.edi.separateMng.SeparateMng;
import org.yht.exps.express.model.edi.separateMng.SeparateMngDoc;
import org.yht.exps.express.model.edi.separateMng.search.SearchSeparateMngList;

@Mapper
public interface SeparateMngMapper {
	
	List<SeparateMng> selectSeparateDocList(SearchSeparateMngList search);
	
	List<SeparateMng> selectSeparateList(SearchSeparateMngList search);
	List<Map<String, Object>> selectSpecialData(Map<String, Object> map);
		
	void insertSeparateMng(Map<String, Object> map);
	
	void updateSeparateJob(Map<String, Object> map);
	
	SeparateMng getSeparateDocNo(SeparateMngDoc separateMngDoc);
			
	void deleteBatchSeparateMng(SeparateMngDoc separateMngDoc);
		
}
