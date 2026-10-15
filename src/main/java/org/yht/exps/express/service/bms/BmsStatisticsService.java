package org.yht.exps.express.service.bms;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsStatisticsMapper;
import org.yht.exps.express.model.bms.statistics.BmsStatistics;
import org.yht.exps.express.model.bms.statistics.BmsStatisticsList;
import org.yht.exps.express.model.bms.statistics.search.SearchStatistics;
import org.yht.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@Transactional
public class BmsStatisticsService {

    @Autowired
    BmsStatisticsMapper bmsStatisticsMapper;

    public BmsStatisticsList selectCarryInStatisticsList(SearchStatistics search) throws Exception {
        BmsStatisticsList bmsStatisticsList = new BmsStatisticsList();
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
        List<String> srchInGroupCodes = new ArrayList<>();
        String srchInGroupCode1 = search.getSrchInGroupCode1();
        String srchInGroupCode2 = search.getSrchInGroupCode2();
        String srchInGroupCode3 = search.getSrchInGroupCode3();
        String srchInGroupCode4 = search.getSrchInGroupCode4();
        String srchInGroupCode5 = search.getSrchInGroupCode5();
        String srchInGroupCode6 = search.getSrchInGroupCode6();


        if (srchInGroupCode1 != null && !srchInGroupCode1.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode1);
        }
        if (srchInGroupCode2 != null && !srchInGroupCode2.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode2);
        }
        if (srchInGroupCode3 != null && !srchInGroupCode3.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode3);
        }
        if (srchInGroupCode4 != null && !srchInGroupCode4.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode4);
        }
        if (srchInGroupCode5 != null && !srchInGroupCode5.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode5);
        }
        if (srchInGroupCode6 != null && !srchInGroupCode6.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode6);
        }

        search.setSrchInGroupCodes(srchInGroupCodes);

        Util.accessControlDatabase(search, "userLanguage", true, null, null, null, null);
        List<BmsStatistics> list;
        if(Util.null2str(srchInGroupCode1).equals("XRAY_TIME")) {
        	list  = bmsStatisticsMapper.selectXrayCarryInStatisticsList(search);
        }else {
        	list  = bmsStatisticsMapper.selectCarryInStatisticsList(search);
        }
        bmsStatisticsList.setBmsStatisticsList(list);
        if(list.size() > 0) {
            bmsStatisticsList.setTotal(list.get(0).getTotal());
        }else {
            bmsStatisticsList.setTotal(0);
        }
        return bmsStatisticsList;
    }

    public BmsStatisticsList selectCarryOutStatisticsList(SearchStatistics search) throws Exception {

        BmsStatisticsList bmsStatisticsList = new BmsStatisticsList();
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
        List<String> srchOutGroupCodes = new ArrayList<>();
        String srchOutGroupCode1 = search.getSrchOutGroupCode1();
        String srchOutGroupCode2 = search.getSrchOutGroupCode2();
        String srchOutGroupCode3 = search.getSrchOutGroupCode3();
        String srchOutGroupCode4 = search.getSrchOutGroupCode4();
        String srchOutGroupCode5 = search.getSrchOutGroupCode5();
        String srchOutGroupCode6 = search.getSrchOutGroupCode6();

        if (srchOutGroupCode1 != null && !srchOutGroupCode1.isEmpty()) {
            srchOutGroupCodes.add(srchOutGroupCode1);
        }
        if (srchOutGroupCode2 != null && !srchOutGroupCode2.isEmpty()) {
            srchOutGroupCodes.add(srchOutGroupCode2);
        }
        if (srchOutGroupCode3 != null && !srchOutGroupCode3.isEmpty()) {
            srchOutGroupCodes.add(srchOutGroupCode3);
        }
        if (srchOutGroupCode4 != null && !srchOutGroupCode4.isEmpty()) {
            srchOutGroupCodes.add(srchOutGroupCode4);
        }
        if (srchOutGroupCode5 != null && !srchOutGroupCode5.isEmpty()) {
            srchOutGroupCodes.add(srchOutGroupCode5);
        }
        if (srchOutGroupCode6 != null && !srchOutGroupCode6.isEmpty()) {
            srchOutGroupCodes.add(srchOutGroupCode6);
        }

        search.setSrchOutGroupCodes(srchOutGroupCodes);

        Util.accessControlDatabase(search, "userLanguage", true, null, null, null, null);
        List<BmsStatistics> list  = bmsStatisticsMapper.selectCarryOutStatisticsList(search);
        bmsStatisticsList.setBmsStatisticsList(list);
        if(list.size() > 0) {
            bmsStatisticsList.setTotal(list.get(0).getTotal());
        }else {
            bmsStatisticsList.setTotal(0);
        }
        return bmsStatisticsList;
    }

    public BmsStatisticsList selectStockStatisticsList(SearchStatistics search) throws Exception {
        BmsStatisticsList bmsStatisticsList = new BmsStatisticsList();
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
        List<String> srchInGroupCodes = new ArrayList<>();
        String srchInGroupCode1 = search.getSrchInGroupCode1();
        String srchInGroupCode2 = search.getSrchInGroupCode2();
        String srchInGroupCode3 = search.getSrchInGroupCode3();
        String srchInGroupCode4 = search.getSrchInGroupCode4();
        String srchInGroupCode5 = search.getSrchInGroupCode5();
        String srchInGroupCode6 = search.getSrchInGroupCode6();


        if (srchInGroupCode1 != null && !srchInGroupCode1.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode1);
        }
        if (srchInGroupCode2 != null && !srchInGroupCode2.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode2);
        }
        if (srchInGroupCode3 != null && !srchInGroupCode3.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode3);
        }
        if (srchInGroupCode4 != null && !srchInGroupCode4.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode4);
        }
        if (srchInGroupCode5 != null && !srchInGroupCode5.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode5);
        }
        if (srchInGroupCode6 != null && !srchInGroupCode6.isEmpty()) {
            srchInGroupCodes.add(srchInGroupCode6);
        }

        search.setSrchInGroupCodes(srchInGroupCodes);

        Util.accessControlDatabase(search, "userLanguage", true, null, null, null, null);
        List<BmsStatistics> list  = bmsStatisticsMapper.selectStockStatisticsList(search);
        bmsStatisticsList.setBmsStatisticsList(list);
        if(list.size() > 0) {
            bmsStatisticsList.setTotal(list.get(0).getTotal());
        }else {
            bmsStatisticsList.setTotal(0);
        }
        return bmsStatisticsList;
    }
}
