package org.yht.exps.express.mapper.sms.holy;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.sms.holy.Holyday;
import org.yht.exps.express.model.sms.holy.HolydayList;
import org.yht.exps.express.model.sms.holy.search.SearchHolydayList;

@Mapper
public interface HolydayMapper {
	List<Holyday> selectHolyDayList(SearchHolydayList search);

	void insertHolyDay(Holyday holyday);
	void deleteHolyDay(HolydayList holydayList);
}
