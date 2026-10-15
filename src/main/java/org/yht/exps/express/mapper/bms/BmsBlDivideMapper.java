package org.yht.exps.express.mapper.bms;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsBlDivide.BmsBlDivide;
import org.yht.exps.express.model.bms.bmsBlDivide.search.SearchBmsBlDivideList;

@Mapper
public interface BmsBlDivideMapper {

	/**
	 * 보수&BL분할 조회
	 * @param search
	 * @return
	 */
	List<BmsBlDivide> selectBmsBlDivideList(SearchBmsBlDivideList search);
	
}
