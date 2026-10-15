package org.yht.exps.express.mapper.eCommerce;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.coio.CoioDet;
import org.yht.exps.express.model.eCommerce.coio.CoioLoc;
import org.yht.exps.express.model.eCommerce.coio.CoioLocSub;
import org.yht.exps.express.model.eCommerce.coio.CoioSum;
import org.yht.exps.express.model.eCommerce.coio.search.SearchCoioList;

@Mapper
public interface CoioMapper {

	List<CoioDet> selectCoioDetailList(SearchCoioList search);
	List<CoioSum> selectCoioSummaryList(SearchCoioList search);
	List<CoioLoc> selectCoioLocationList(SearchCoioList search);
	List<CoioLocSub> selectCoioLocationSubList(SearchCoioList search);
	
	
}
