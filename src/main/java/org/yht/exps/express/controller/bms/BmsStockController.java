package org.yht.exps.express.controller.bms;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.bms.bmsStock.BmsStockList;
import org.yht.exps.express.model.bms.bmsStock.BmsStockSaveResult;
import org.yht.exps.express.model.bms.bmsStock.ExcelUploadCustomsStock;
import org.yht.exps.express.model.bms.bmsStock.search.SearchStock;
import org.yht.exps.express.model.bms.statistics.BmsStatisticsList;
import org.yht.exps.express.model.bms.statistics.search.SearchStatistics;
import org.yht.exps.express.service.bms.BmsStockService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@Api(tags="BmsStoc")
@RequestMapping("/{entCode}/bms/stoc")
public class BmsStockController {

    @Resource(name = "bmsStockService")
    BmsStockService bmsStockService;

    @ApiOperation(value = "재고조회", notes="재고조회 - 재고 리스트를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/stock/search/list", method = RequestMethod.GET)
    public ResponseEntity<BmsStockList> selectStockList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchStock search, HttpServletRequest request) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsStockService.selectStockList(search), HttpStatus.OK);
    }
    @ApiOperation(value = "재고실사", notes="재고실사조회 - 재고실사 리스트를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/stockInspection/search/list", method = RequestMethod.GET)
    public ResponseEntity<BmsStockList> selectStockInspectionList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchStock search, HttpServletRequest request) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsStockService.selectStockInspectionList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "재고조회 엑셀 list 다운로드", notes="재고조회 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/stock/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectStockExcel(@PathVariable("entCode") String entCode, @RequestBody SearchStock search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        search.setPage(0);
        BmsStockList bmsStockList = bmsStockService.selectStockList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", bmsStockList.getBmsStockList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }

    @ApiOperation(value = "재고실사 엑셀 list 다운로드", notes="재고실사 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/stockInspection/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectStockInspectionExcel(@PathVariable("entCode") String entCode, @RequestBody SearchStock search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        search.setPage(0);
        BmsStockList bmsStockList = bmsStockService.selectStockInspectionList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", bmsStockList.getBmsStockList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }

    @ApiOperation(value = "재고실사 ExcelUpload", notes="재고실사 ExcelUpload합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "excelUploadType", value = "엑셀타입", required = true, dataType = "string", paramType = "path", defaultValue = "bmsStock")
    })
    @RequestMapping(value="/excelUpload/check/{excelUploadType}", method = RequestMethod.POST)
    public ResponseEntity<BmsStockSaveResult> checkExcelUploadCheck(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
        return new ResponseEntity<BmsStockSaveResult>(bmsStockService.checkExcelUpload(uploadExcel, entCode, excelUploadType), HttpStatus.OK);
    }

    @ApiOperation(value = "재고실사 Excel 저장", notes="재고실사 Excel 저장합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/excelUpload/save/{excelUploadType}", method = RequestMethod.POST)
    public ResponseEntity<BmsStockSaveResult> saveExcelUpload(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestBody ExcelUploadCustomsStock excelUploadCustomsStock, HttpServletRequest request) throws Exception {
        return new ResponseEntity<BmsStockSaveResult>(bmsStockService.saveExcelUpload(excelUploadCustomsStock, request, excelUploadType), HttpStatus.OK);
    }
}
