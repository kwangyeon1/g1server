package org.yht.exps.express.mapper.pda;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.model.mobile.bms.MobBwlo;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwlo;

@Mapper
public interface BmsMobBwloMapper {
	public List<Map<String, Object>> selectLocationStock(SearchInList search);
	
	public List<Map<String, Object>> selectLocationInfoDetail(SearchInList search);

	public MobBwlo selectLocationCarryInDetail(SearchMobBwlo search);
	
	public int updateLocationCarryIn(SearchMobBwlo search);
}
