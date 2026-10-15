package org.yht.exps.express.mapper.bms;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsInspection.BmsInspection;
import org.yht.exps.express.model.bms.bmsInspection.search.SearchBmsInspectionList;

@Mapper
public interface BmsInspectionMapper {

	/**
	 * 검사계획통보 조회
	 * @param search
	 * @return
	 */
	List<BmsInspection> selectBmsInspectionList(SearchBmsInspectionList search);

}
