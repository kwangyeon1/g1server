package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsCargo.BmsMasterDest;

@Mapper
public interface PdaCargoReceiveMapper {
	Map<String, Object> selectExistMasterScan(Map<String, Object> search);
	List<BmsMasterDest> selectMasterDest(Map<String, Object> search);
	List<BmsMasterDest> selectMasterDestList(Map<String, Object> search);
	void insertMasterDest(Map<String, Object> search);
	void updateMasterDest(Map<String, Object> search);
	void deleteMasterDest(Map<String, Object> search);
}