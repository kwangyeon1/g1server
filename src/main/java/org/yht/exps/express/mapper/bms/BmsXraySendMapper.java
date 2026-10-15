package org.yht.exps.express.mapper.bms;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsXraySend.XraySend;
import org.yht.exps.express.model.bms.bmsXraySend.XraySendBl;
import org.yht.exps.express.model.bms.bmsXraySend.search.SearchXraySend;

@Mapper
public interface BmsXraySendMapper {
	List<XraySend> selectXraySendList(SearchXraySend searchXraySend);
	List<XraySend> selectXraySendTypeList(SearchXraySend search);
	void updateXraySend(SearchXraySend searchXraySend);
	void updateReleaseArrovalSend(SearchXraySend searchXraySend);
	List<XraySendBl> selectXraySendBl(SearchXraySend searchXraySend);
	void insertXraySend(XraySendBl xraySendBl);
	void insertReleaseArrovalSend(XraySendBl xraySendBl);
}
