package org.yht.exps.express.mapper.bms;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsAirDO.BmsAirDO;
import org.yht.exps.express.model.bms.bmsAirDO.search.SearchBmsAirDOList;

@Mapper
public interface BmsAirDOMapper {

	/**
	 * 항공D/O조회
	 * @param search
	 * @return
	 */
	List<BmsAirDO> selectBmsAirDOList(SearchBmsAirDOList search);
}
