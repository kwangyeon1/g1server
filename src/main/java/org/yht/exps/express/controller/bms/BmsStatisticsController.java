package org.yht.exps.express.controller.bms;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;

import org.yht.exps.express.model.bms.statistics.BmsStatisticsList;
import org.yht.exps.express.model.bms.statistics.search.SearchStatistics;
import org.yht.exps.express.service.bms.BmsStatisticsService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@Api(tags="BmsStat")
@RequestMapping("/{entCode}/bms/stat")
public class BmsStatisticsController {

    @Resource(name = "bmsStatisticsService")
    BmsStatisticsService bmsStatisticsService;

    @ApiOperation(value = "통계조회 - 반입 리스트 조회", notes="통계조회 - 반입 리스트 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryIn/search/list", method = RequestMethod.GET)
    public ResponseEntity<BmsStatisticsList> selectCarryInStatisticsList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchStatistics search, HttpServletRequest req) throws Exception {
        //search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsStatisticsService.selectCarryInStatisticsList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "통계조회 - 반출 리스트 조회", notes="통계조회 - 반출 리스트 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryOut/search/list", method = RequestMethod.GET)
    public ResponseEntity<BmsStatisticsList> selectCarryOutStatisticsList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchStatistics search, HttpServletRequest req) throws Exception {
        //search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsStatisticsService.selectCarryOutStatisticsList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "통계조회 - 재고 리스트 조회", notes="통계조회 - 재고 리스트 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/stock/search/list", method = RequestMethod.GET)
    public ResponseEntity<BmsStatisticsList> selectStockStatisticsList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchStatistics search, HttpServletRequest req) throws Exception {
        //search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsStatisticsService.selectStockStatisticsList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입통계 엑셀 list 조회", notes="반입통계 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryIn/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectCarryInStatisticsExcel(@PathVariable("entCode") String entCode, @RequestBody SearchStatistics search, HttpServletRequest req) throws Exception {
        //search.setSrchEntCode(entCode);
        search.setPage(0);
        BmsStatisticsList bmsStatisticsList = bmsStatisticsService.selectCarryInStatisticsList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", bmsStatisticsList.getBmsStatisticsList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }

    @ApiOperation(value = "반출통계 엑셀 list 조회", notes="반출통계 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryOut/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectCarryOutStatisticsExcel(@PathVariable("entCode") String entCode, @RequestBody SearchStatistics search, HttpServletRequest req) throws Exception {
        //search.setSrchEntCode(entCode);
        search.setPage(0);
        BmsStatisticsList bmsStatisticsList = bmsStatisticsService.selectCarryOutStatisticsList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", bmsStatisticsList.getBmsStatisticsList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }

    @ApiOperation(value = "재고통계 엑셀 list 조회", notes="재고통계 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/stock/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectStockStatisticsExcel(@PathVariable("entCode") String entCode, @RequestBody SearchStatistics search, HttpServletRequest req) throws Exception {
        //search.setSrchEntCode(entCode);
        search.setPage(0);
        BmsStatisticsList bmsStatisticsList = bmsStatisticsService.selectStockStatisticsList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", bmsStatisticsList.getBmsStatisticsList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }
}
