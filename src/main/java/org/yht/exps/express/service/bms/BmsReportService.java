package org.yht.exps.express.service.bms;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsReportMapper;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.CarryInList;
import org.yht.exps.express.model.bms.carryin.InPlan;
import org.yht.exps.express.model.bms.dailyReport.*;
import org.yht.exps.express.model.bms.dailyReport.search.SearchDailyReport;
import org.yht.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@Transactional
public class BmsReportService {

    @Autowired
    BmsReportMapper bmsReportMapper;

    /**
     * 일일보고 - 근무일지 조회
     * */
    public WorkReportList selectWorkReport(SearchDailyReport search) {

        List<WorkReport> list = bmsReportMapper.selectWorkReport(search);
        
        int inOutDiff = 0;
        if(list.size()>0) {
        	WorkReport endRow = list.get(list.size()-1);
        	inOutDiff = endRow.getTodayStockCount() - (endRow.getYesterdayStockCount() + endRow.getTodayCarryinCount() - endRow.getTodayCarryoutCount());
        }
        
        WorkReportList workReportList = new WorkReportList();
        workReportList.setWorkReportList(list);
        workReportList.setInOutDiff(inOutDiff);

        return workReportList;
    }

    /**
     * 일일보고 - 반입보고 조회
     * */
    public CarryInReportList selectCarryInReport(SearchDailyReport search) {
        List<CarryInReport> list = bmsReportMapper.selectCarryInReport(search);
        CarryInReportList carryInReportList = new CarryInReportList();
        carryInReportList.setCarryInReportList(list);
        return carryInReportList;
    }

    public CarryOutReportList selectCarryOutReport(SearchDailyReport search) {
        List<CarryOutReport> list = bmsReportMapper.selectCarryOutReport(search);
        CarryOutReportList carryOutReportList = new CarryOutReportList();
        carryOutReportList.setCarryOutReportList(list);
        return carryOutReportList;
    }

    public PeriodReportList selectPeriodReport(SearchDailyReport search) throws Exception {
        PeriodReportList periodReportList = new PeriodReportList();
        if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
        if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

        /* N개 검색(멀티타입) */
        List<String> searchNoList = new ArrayList<String>();
        if(!"".equals(Util.null2str(search.getSrchNo()))) {
            searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
        }

        if(searchNoList.size() > 1){
            search.setSearchType("multi");
            search.setSearchNoList(searchNoList);
        }else{
            search.setSearchType("");
            search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
        }

        List<PeriodReport> list  = bmsReportMapper.selectPeriodReport(search);
        int inOutDiff = 0;
        for (int i = 0; i < list.size(); i++) {
        	if(list.get(i).getDeviceClassification().equals("합계")) {
        		inOutDiff += list.get(i).getTodayStockCount() - (list.get(i).getYesterdayStockCount() + list.get(i).getCarryinTotal() - list.get(i).getCarryoutTotal());
        	}
    	}
        
        periodReportList.setPeriodReportList(list);
        if(list.size() > 0) {
            periodReportList.setTotal(list.get(0).getTotal());
            periodReportList.setInOutDiff(inOutDiff);
        }else {
            periodReportList.setTotal(0);
        }
        return periodReportList;
    }
}
