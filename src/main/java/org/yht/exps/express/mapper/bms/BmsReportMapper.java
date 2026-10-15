package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.dailyReport.*;
import org.yht.exps.express.model.bms.dailyReport.search.SearchDailyReport;

import java.util.List;

@Mapper
public interface BmsReportMapper {
    List<WorkReport> selectWorkReport(SearchDailyReport search);

    List<CarryInReport> selectCarryInReport(SearchDailyReport search);

    List<CarryOutReport> selectCarryOutReport(SearchDailyReport search);

    List<PeriodReport> selectPeriodReport(SearchDailyReport search);

    int selectInOutDiff(SearchDailyReport search);
}
