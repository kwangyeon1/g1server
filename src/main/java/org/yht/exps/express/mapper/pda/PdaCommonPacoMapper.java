package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;

@Mapper
public interface PdaCommonPacoMapper {
	
	List<InPlanPkg> checkDuplicatePkgNo(Map<String, Object> search);
}
