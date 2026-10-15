package org.yht.exps.express.mapper.pda;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.mobile.bms.MobCargo;
import org.yht.exps.express.model.mobile.bms.search.SearchMobCargo;

@Mapper
public interface MobCargoMapper {
	
	MobCargo selectCargoDetailByCarryIn(SearchMobCargo search);
	MobCargo selectCargoDetailByInPlan(SearchMobCargo search);
	MobCargo selectCargoDetailByOrder(SearchMobCargo search);
	
	List<MobCargo> checkCargoListByBlNo(SearchMobCargo search);
}
