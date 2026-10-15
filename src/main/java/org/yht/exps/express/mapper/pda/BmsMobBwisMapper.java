package org.yht.exps.express.mapper.pda;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.mobile.bms.MobBwis;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwis;

@Mapper
public interface BmsMobBwisMapper {
	public MobBwis selectInspectResult(SearchMobBwis search);
	public int updateSeInspectStart(SearchMobBwis search);
}
