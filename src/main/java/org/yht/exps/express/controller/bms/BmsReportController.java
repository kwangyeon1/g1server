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
import org.yht.exps.express.model.bms.dailyReport.*;
import org.yht.exps.express.model.bms.dailyReport.search.SearchDailyReport;
import org.yht.exps.express.model.sms.user.EcmCustomerList;
import org.yht.exps.express.service.bms.BmsReportService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@Api(tags = "BmsRprt")
@RequestMapping("/{entCode}/bms/rprt")
public class BmsReportController {

    @Resource(name = "bmsReportService")
    BmsReportService bmsReportService;

    @ApiOperation(value = "근무일지 list 조회", notes="근무일지 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/workReport", method = RequestMethod.GET)
    public ResponseEntity<WorkReportList> selectWorkReport(@PathVariable("entCode") String entCode, SearchDailyReport search) throws Exception{
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsReportService.selectWorkReport(search), HttpStatus.OK);
    }
    @ApiOperation(value = "반입보고 list 조회", notes="반입보고 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/carryInReport", method = RequestMethod.GET)
    public ResponseEntity<CarryInReportList> selectCarryInReport(@PathVariable("entCode") String entCode, SearchDailyReport search) throws Exception{
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsReportService.selectCarryInReport(search), HttpStatus.OK);
    }
    @ApiOperation(value = "반출보고 list 조회", notes="반출보고 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/carryOutReport", method = RequestMethod.GET)
    public ResponseEntity<CarryOutReportList> selectCarryOutReport(@PathVariable("entCode") String entCode, SearchDailyReport search) throws Exception{
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsReportService.selectCarryOutReport(search), HttpStatus.OK);
    }

    @ApiOperation(value = "기간별반출입 list 조회", notes="반출보고 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/periodReport", method = RequestMethod.GET)
    public ResponseEntity<PeriodReportList> selectPeriodReport(@PathVariable("entCode") String entCode, SearchDailyReport search) throws Exception{
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsReportService.selectPeriodReport(search), HttpStatus.OK);
    }

    @ApiOperation(value = "근무일지 엑셀 list 조회", notes="근무일지 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/workReport/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectWorkReportExcel(@PathVariable("entCode") String entCode, @RequestBody SearchDailyReport search, HttpServletRequest request) throws Exception {

        search.setPage(0);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", search.getWorkReportList());
        map.put("reportType",search.getReportType());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }

    @ApiOperation(value = "반입보고 엑셀 list 조회", notes="반입보고 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryInReport/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectCarryInReportExcel(@PathVariable("entCode") String entCode, @RequestBody SearchDailyReport search, HttpServletRequest request) throws Exception {

        search.setPage(0);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", search.getCarryInReportList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }

    @ApiOperation(value = "반출보고 엑셀 list 조회", notes="반출보고 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryOutReport/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectCarryOutReportExcel(@PathVariable("entCode") String entCode, @RequestBody SearchDailyReport search, HttpServletRequest request) throws Exception {

        search.setPage(0);
        
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data",  search.getCarryOutReportList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }

    @ApiOperation(value = "기간별반출입 엑셀 list 조회", notes="기간별반출입 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/periodReport/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectPeriodReportExcel(@PathVariable("entCode") String entCode, @RequestBody SearchDailyReport search, HttpServletRequest request) throws Exception {

        search.setPage(0);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", search.getPeriodReportList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }
}
