package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgOrder;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWg;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWgOrderList;

@Mapper
public interface MultiWgMapper {
	
	List<MultiWgOrder> selectMultiWgList(SearchMultiWgOrderList search);
	List<MultiWgOrder> selectSubMultiWgList(SearchMultiWgOrderList search);
	
	MultiWgOrder selectOrder(SearchMultiWg search);	
	void deleteMultiWgSeq(MultiWgOrder order);	
	int deleteMultiWgOrder(MultiWgList order);	
	void insertMulti(MultiWgOrder order);	
	void updateMulti(MultiWgOrder order);	
}
