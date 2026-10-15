package org.yht.exps.express.mapper.bms;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsErrorDesc.BmsErrorDesc;
import org.yht.exps.express.model.bms.bmsErrorDesc.search.SearchBmsErrorDescList;

@Mapper
public interface BmsErrorDescMapper {

	/** 오류통보 리스트 조회 */
	List<BmsErrorDesc> selectBms05ErrorDescList(SearchBmsErrorDescList search); 

	/** 결과통보 리스트 조회 */
	List<BmsErrorDesc> selectBms09ErrorDescList(SearchBmsErrorDescList search);

}
