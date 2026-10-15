package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.statistics.BmsStatistics;
import org.yht.exps.express.model.bms.statistics.search.SearchStatistics;

import java.util.List;

@Mapper
public interface BmsStatisticsMapper {
    List<BmsStatistics> selectCarryInStatisticsList(SearchStatistics search);

    List<BmsStatistics> selectCarryOutStatisticsList(SearchStatistics search);

    List<BmsStatistics> selectStockStatisticsList(SearchStatistics search);
    
    List<BmsStatistics> selectXrayCarryInStatisticsList(SearchStatistics search);
}
