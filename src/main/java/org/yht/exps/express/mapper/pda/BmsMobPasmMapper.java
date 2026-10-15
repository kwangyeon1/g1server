package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.mobile.bms.BmsSampleScan;

@Mapper
public interface BmsMobPasmMapper {
	public Map<String, Object> selectTakeOutSample(Map<String, Object> search);
	public void updateSampleInOutCt(Map<String, Object> map);
	public void updateSampleData(Map<String, Object> map);
	public List<Map<String, Object>> selectSampleTakingOutList(Map<String, Object> search);
	public void updateSampleTakingOutList(Map<String, Object> map);
	public List<Map<String, Object>> selectScanLogInfo(Map<String, Object> search);
	public void insertSampleScanLog(BmsSampleScan scanLog);
}
