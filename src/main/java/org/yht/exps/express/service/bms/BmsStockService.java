package org.yht.exps.express.service.bms;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsStockInspectionMapper;
import org.yht.exps.express.mapper.bms.BmsStockMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.bms.bmsStock.*;
import org.yht.exps.express.model.bms.bmsStock.search.SearchStock;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.eCommerce.OrderService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@Transactional
public class BmsStockService {

    @Resource(name = "commonService")
    CommonService commonService;
    @Resource(name = "fileService")
    FileService fileService;

    @Resource(name = "orderService")
    OrderService orderService;

    @Autowired
    BmsStockMapper bmsStockMapper;

    @Autowired
    BmsStockInspectionMapper bmsStockInspectionMapper;

    @Autowired
    CustomProperties customProperties;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    public BmsStockList selectStockList(SearchStock search) throws Exception {
        BmsStockList BmsStockList = new BmsStockList();
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
        Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYIN_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO|PKG_NO");
        List<BmsStock> list = bmsStockMapper.selectStockList2(search);

        BmsStockList.setBmsStockList(list);
        if(list.size() > 0) {
            BmsStockList.setTotal(list.get(0).getTotal());
        }else {
            BmsStockList.setTotal(0);
        }

        return BmsStockList;
    }

    public BmsStockList selectStockInspectionList(SearchStock search) throws Exception {
        BmsStockList BmsStockList = new BmsStockList();
        if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
        if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

        /* N개 검색(멀티타입) */
        List<String> searchNoList = new ArrayList<String>();
        if(!"".equals(Util.null2str(search.getSrchNo()))) {
            searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
        }
        /* N개 검색(멀티타입) */
        List<String> srchLocationPlaceList = new ArrayList<String>();
        if(!"".equals(Util.null2str(search.getSrchLocationPlace()))) {
        	srchLocationPlaceList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchLocationPlace())).split(",")));
        }

        if(searchNoList.size() > 1){
            search.setSearchType("multi");
            search.setSearchNoList(searchNoList);
        }else{
            search.setSearchType("");
            search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
        }
        
        if(srchLocationPlaceList.size() > 1){
            search.setSearchLocationType("multi");
            search.setSrchLocationPlaceList(srchLocationPlaceList);
        }else{
            search.setSearchLocationType("");
            search.setSrchLocationPlace(Util.null2str(search.getSrchLocationPlace()).trim().replace(",", ""));
        }
        
        String tmp = search.getDefaultSort();
        if("ISNULL(CARRYIN_DATE,'') + ISNULL(CUSTOMS_CARRYIN_DATE,'') + ISNULL(SCAN_DATE,''), ISNULL(CARRYIN_DATE,'') + ISNULL(CUSTOMS_CARRYIN_DATE,''), ISNULL(IN_PLAN_NO,0) DESC".equals(tmp) ) {
        	search.setDefaultSort("");
        }
        Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYIN_DATE|AR_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO|PKG_NO");
        search.setDefaultSort(tmp);
        
        //if(!"04077009".equals(search.getBmswhCode())) {
	    //    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    //
	    //    LocalDate date = LocalDate.parse(search.getSrchStartDate(), formatter);
	    //    LocalDate scanDate = LocalDate.parse(search.getSrchScanStartDate(), formatter);
	    //
	    //    // 과거 날짜 선택
	    //    LocalDate scanStartDate = scanDate.isBefore(date) ? scanDate : date;
	    //    search.setSrchStartDate(scanStartDate.format(formatter));
	    //    
	    //    date = LocalDate.parse(search.getSrchEndDate(), formatter);
	    //    scanDate = LocalDate.parse(search.getSrchScanEndDate(), formatter);
	    //
	    //    // 미래 날짜 선택
	    //    LocalDate scanEndDate = scanDate.isAfter(date) ? scanDate : date;
	    //    search.setSrchEndDate(scanEndDate.format(formatter));
        //}
        
        List<BmsStock> list = new ArrayList<>();
        if("04077009".equals(search.getBmswhCode())) {
        	list = bmsStockInspectionMapper.selectStockInspectionList(search);
        }else {
        	list = bmsStockInspectionMapper.selectStockInspectionListForPantk(search);
        }
        
        if(!"04077009".equals(search.getBmswhCode())) {
        	//if("WAREHOUSE_BASE".equals(Util.null2str(search.getSrchBaseState()))) {
        		for(BmsStock bmsStock : list) {
    	        	Map<String,Object> cargoStatusInfo = bmsStockInspectionMapper.selectCargoStatus(bmsStock);
    	        	if(cargoStatusInfo != null) {
    	        		String outLastPodStatus = Util.null2str(cargoStatusInfo.get("OUT_LAST_POD_STATUS"));
    	        		String approvalClass = Util.null2str(cargoStatusInfo.get("APPROVAL_CLASS"));
    	        		String inLastPodStatus = Util.null2str(cargoStatusInfo.get("IN_LAST_POD_STATUS"));
    	        		if(!"".equals(outLastPodStatus)) {
    	        			if("BMS04".equals(inLastPodStatus)) {
    	        				bmsStock.setLastPodStatus("반출접수");
    	        			}else {
    	        				bmsStock.setLastPodStatus("반출미신고");
    	        			}
    	        		}else if(!"".equals(approvalClass)) {
    	        			if("1".equals(approvalClass)) {
    	        				bmsStock.setLastPodStatus("반출취하");
    	        			}else if("31".equals(approvalClass)){
    	        				bmsStock.setLastPodStatus("반출승인");
    	        			}
    	        		}else if(!"".equals(inLastPodStatus)) {
    	        			if("BMS04".equals(inLastPodStatus)) {
    	        				bmsStock.setLastPodStatus("반입접수");
    	        			}else {
    	        				bmsStock.setLastPodStatus("반입미신고");
    	        			}
    	        		}else {
    	        			bmsStock.setLastPodStatus("미반입");
    	        		}
    	        	}
    	        }
        	//}
        }

        BmsStockList.setBmsStockList(list);
        if(list.size() > 0) {
            BmsStockList.setTotal(list.get(0).getTotal());
        }else {
            BmsStockList.setTotal(0);
        }

        return BmsStockList;
    }

    /**
     * 재고실사 엑셀업로드(엑셀업로드 체크)
     * */
    public BmsStockSaveResult checkExcelUpload(MultipartFile uploadExcel, String entCode, String excelUploadType) throws Exception {
        BmsStockSaveResult bmsStockSaveResult = new BmsStockSaveResult();
        Map<String,Object> resultData = new HashMap<>();
        
        // 파일 확장자 유효성 검사
 		if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
 			bmsStockSaveResult.setResultCode("A00036");
 			return bmsStockSaveResult;
 		}
        
        FileUploadResponse result = fileService.storeFile(uploadExcel, entCode, true);

        //Map<String, Object> rstHbl = null;

        if(Util.null2str(result.getResult()).equals("ok")) {
            File file = fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile();
            String extension = null;
            if(file.getName().toLowerCase().indexOf(".xlsx") > -1) {
                extension = "xlsx";
            }else {
                extension = "xls";
            }

            String[] fieldName = null;
            String[] fieldTitle = null;
            String[] fieldLength = null;
            String[] fieldType = null;

            if(excelUploadType.equals("bmsStock")) {
                fieldName = new String[] {
                        "no",      "cargoReferenceNo", "blNo",              "consigneeName", "consigneeAddress1", "consigneeTel", "commodityItemLoc",
                        "ct",      "wt",               "sOutCt",            "sOutWt",        "stockCt",           "stockWt",      "carryinDate",
                        "cdType",  "notifyName",       "notifyAddress1",    "notifyTel"
                };
                fieldTitle = new String[] {
                        "No",      "화물관리번호",       "B/L번호",           "수하인상호",     "수하인주소",         "수하인 전화번호", "품명",
                        "반입개수", "반입중량(KG)",      "반출개수",           "반출중량(KG)",    "재고개수",          "재고중량(KG)",    "반입일자",
                        "화물구분", "통지처 상호",       "통지처 주소",        "통지처 전화번호"
                };
                fieldLength = new String[] {
                        "10",       "21",               "20",               "100",            "150",                "50",             "200",
                        "4" ,       "12",                "4",               "12",              "4",                 "12",              "10",
                        "20",       "100",              "150",              "50"
                };
                fieldType = new String[] {
                        "S",        "S",                "S",                "SN",             "SN",                 "S",              "SN",
                        "N",        "S",                "N",                "S",              "N",                  "S",              "S",
                        "S",       "SN",                "SN",               "S"
                };

            }

            List<Map<String, Object>> dataList = new ArrayList<>();
            InputStream is = null;
            try{
                is = new FileInputStream(file);
                Workbook wb = WorkbookFactory.create(is);
                Sheet sheet = wb.getSheetAt(0);
                DataFormatter df = new DataFormatter();
                int fieldSize = fieldName.length;
                StringBuffer errMsg = new StringBuffer();
                Map<String, Object> dataMap = null;
                String fieldValue = "";
                //데이터포맷
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                //데이터 형식체크(세관재고 업로드 파일 형식의 컬럼명이 모두 일치해야함)
                String cell1 = String.valueOf(sheet.getRow(0).getCell(0));
                String cell2 = String.valueOf(sheet.getRow(0).getCell(1));
                String cell3 = String.valueOf(sheet.getRow(0).getCell(2));
                String cell4 = String.valueOf(sheet.getRow(0).getCell(3));
                String cell5 = String.valueOf(sheet.getRow(0).getCell(4));
                String cell6 = String.valueOf(sheet.getRow(0).getCell(5));
                String cell7 = String.valueOf(sheet.getRow(0).getCell(6));
                String cell8 = String.valueOf(sheet.getRow(0).getCell(7));
                String cell9 = String.valueOf(sheet.getRow(0).getCell(8));
                String cell10 = String.valueOf(sheet.getRow(0).getCell(9));
                String cell11 = String.valueOf(sheet.getRow(0).getCell(10));
                String cell12 = String.valueOf(sheet.getRow(0).getCell(11));
                String cell13 = String.valueOf(sheet.getRow(0).getCell(12));
                String cell14 = String.valueOf(sheet.getRow(0).getCell(13));
                String cell15 = String.valueOf(sheet.getRow(0).getCell(14));
                String cell16 = String.valueOf(sheet.getRow(0).getCell(15));
                String cell17 = String.valueOf(sheet.getRow(0).getCell(16));
                String cell18 = String.valueOf(sheet.getRow(0).getCell(17));

                if(!cell1.equals("No")  || !cell2.equals("화물관리번호")  || !cell3.equals("B/L번호") || !cell4.equals("수하인 상호") || !cell5.equals("수하인주소") ||
                        !cell6.equals("수하인 전화번호") || !cell7.equals("품명") || !cell8.equals("반입개수") || !cell9.equals("반입중량(KG)") || !cell10.equals("반출개수") ||
                        !cell11.equals("반출중량(KG)") || !cell12.equals("재고개수") || !cell13.equals("재고중량(KG)") || !cell14.equals("반입일자") ||
                        !cell15.equals("화물구분") || !cell16.equals("통지처 상호")  || !cell17.equals("통지처 주소")  || !cell18.equals("통지처 전화번호")) {

                    log.info("형식오류: {}",cell11.equals("No"));
                    bmsStockSaveResult.setResultCode("A00036");
                    return bmsStockSaveResult;
                }

                if(extension.equals("xlsx")) {
                    XSSFCell myCell = null;
                    for (int i = 1; i <sheet.getLastRowNum() + 1; i++) {
                        if(sheet.getRow(i) == null) {
                            bmsStockSaveResult.setResultCode("A11233");     /** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
                            resultData.put("nullLine", i + 1);
                            resultData.put("lastLine", sheet.getLastRowNum() + 1);
                            bmsStockSaveResult.setResultData(resultData);
                            return bmsStockSaveResult;
                        }

                        dataMap= new HashMap<String, Object>();
                        //셀 세팅
                        for (int j = 0; j < fieldSize; j++) {
                            myCell = (XSSFCell) sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            if("DATE".equals(fieldType[j])) {
                                String value = null;
                                if(myCell != null && myCell.getDateCellValue() != null) {
                                    value = sdf.format(myCell.getDateCellValue());
                                }
                                fieldValue = orderService.dataChk(value, fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
                            }else {
                                fieldValue = orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
                                //log.info("fieldValue:::: {}", fieldValue);
                            }
                            if(!Util.null2str(fieldValue).equals("")) {
                                dataMap.put(fieldName[j], fieldValue);
                            }
                        }

                        dataList.add(dataMap);
                    }
                }else {
                    HSSFCell myCell = null;
                    for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                        if(sheet.getRow(i) == null) {
                            bmsStockSaveResult.setResultCode("A11233");     /** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
                            resultData.put("nullLine", i + 1);
                            resultData.put("lastLine", sheet.getLastRowNum() + 1);
                            bmsStockSaveResult.setResultData(resultData);
                            return bmsStockSaveResult;
                        }

                        dataMap= new HashMap<String, Object>();
                        //셀 세팅
                        for (int j = 0; j < fieldSize; j++) {
                            myCell = (HSSFCell) sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            if("DATE".equals(fieldType[j])) {
                                String value = null;
                                if(myCell != null && myCell.getDateCellValue() != null) {
                                    value = sdf.format(myCell.getDateCellValue());
                                }
                                fieldValue = orderService.dataChk(value, fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
                            }else {
                                fieldValue = orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
                            }
                            if(!Util.null2str(fieldValue).equals("")) {
                                dataMap.put(fieldName[j], fieldValue);
                            }
                        }
                        dataList.add(dataMap);
                    }
                }

                if(errMsg.length() != 0) {
                    bmsStockSaveResult.setResultCode("dataErr");
                    Map<String, Object> errMap = new HashMap<>();
                    errMap.put("errStr", errMsg);
                    bmsStockSaveResult.setResultData(errMap);
                    return bmsStockSaveResult;
                }

                resultData.put("entCode", entCode);
                resultData.put("excelUploadType", excelUploadType);
                resultData.put("dataList", dataList);
                resultData.put("dataSize", dataList.size());

                bmsStockSaveResult.setResultData(resultData);
                bmsStockSaveResult.setResultCode("200");

                String backupPath = customProperties.getBackupDir() + "excel/" + entCode + "/" + DateTime.getDate("yyyy-MM");
                File dir = new File(backupPath);
                if(dir.exists() == false){
                    dir.mkdirs();
                }
                String backupFilePath = backupPath + "/" + file.getName();
                file.renameTo(new File(backupFilePath));
            }catch (Exception e) {
                bmsStockSaveResult.setResultCode("A00063");
            }finally {
                if (is != null) is.close();
            }

        }else {
            bmsStockSaveResult.setResultCode("ERROR");
        }
        //log.info("bmsStockSaveResult: {}", bmsStockSaveResult.toString());
        return bmsStockSaveResult;
    }


    public BmsStockSaveResult saveExcelUpload(ExcelUploadCustomsStock excelUploadCustomsStock, HttpServletRequest request, String excelUploadType) throws Exception {
        BmsStockSaveResult bmsStockSaveResult = new BmsStockSaveResult();
        ArrayList<BulkData> bulkList = new ArrayList<>();

        if(excelUploadCustomsStock.getDataList() == null) {
            return bmsStockSaveResult;
        }

        BulkData bulkData = null;
        String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
        Map<String, Object> logMap = null;

        SqlSession sqlSession = null;
        ProcessLogs processLogs = new ProcessLogs();

        try {
            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
            BmsStockInspectionMapper stockBatchMapper = sqlSession.getMapper(BmsStockInspectionMapper.class);
            CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);

            int cnt = 1;
            int seq = 1;

            Map<String, Object> tmpLogMap = new HashMap<>();
            tmpLogMap.put("ent_code", excelUploadCustomsStock.getEntCode());
            tmpLogMap.put("ent_business_code", excelUploadCustomsStock.getEntBusinessCode());
            tmpLogMap.put("solution", "BMS");
            tmpLogMap.put("process_user_id", excelUploadCustomsStock.getRegisterUserId());
            tmpLogMap.put("log_job_type", "DATA");
            tmpLogMap.put("job_func_type", "EXCEL");
            tmpLogMap.put("result", "ok");
            tmpLogMap.put("process_date_time", System.currentTimeMillis());
            tmpLogMap.put("ent_business_print_name", excelUploadCustomsStock.getLogUserBusinessPrintName());
            tmpLogMap.put("process_user_name", excelUploadCustomsStock.getLogUserName());
            tmpLogMap.put("process_user_name_eng", excelUploadCustomsStock.getLogUserNameEng());

            if(request != null) {
                tmpLogMap.put("access_ip", Util.getClientIP(request));
            }

            //TB_BMS_STOCK_TEMP 데이터 전부 삭제
            stockBatchMapper.deleteStockInspectionTemp();

            for (Map<String, Object> excelStock : excelUploadCustomsStock.getDataList()) {
                excelStock.put("entCode", excelUploadCustomsStock.getEntCode());
                excelStock.put("gmtTime", excelUploadCustomsStock.getGmtTime());
                excelStock.put("registerUserId", excelUploadCustomsStock.getRegisterUserId());

                String cargoReferenceNo = Util.null2str(excelStock.get("cargoReferenceNo")).replaceAll("-","");

                excelStock.put("no", Util.null2str(excelStock.get("no")));
                excelStock.put("cargoReferenceNo", cargoReferenceNo);
                excelStock.put("blNo", Util.null2str(excelStock.get("blNo")));
                excelStock.put("commodityItemLoc", Util.null2str(excelStock.get("commodityItemLoc")));
                excelStock.put("consigneeName", Util.null2str(excelStock.get("consigneeName")));
                excelStock.put("consigneeAddress1", Util.null2str(excelStock.get("consigneeAddress1")));
                excelStock.put("consigneeTel", Util.null2str(excelStock.get("consigneeTel")));
                excelStock.put("notifyName", Util.null2str(excelStock.get("notifyName")));
                excelStock.put("notifyAddress1", Util.null2str(excelStock.get("notifyAddress1")));
                excelStock.put("notifyTel", Util.null2str(excelStock.get("notifyTel")));
                excelStock.put("carryinDate", Util.null2str(excelStock.get("carryinDate")));
                excelStock.put("stockCt", Util.null2int(excelStock.get("stockCt")));
                excelStock.put("stockWt", Util.null2dbl(excelStock.get("stockWt")));

                //재고 데이터 입력
                stockBatchMapper.insertExcelUploadStock(excelStock);

                if(cnt > 1000){
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                    cnt = 1;
                }
            }
            sqlSession.flushStatements();
            sqlSession.clearCache();
            sqlSession.commit();
        }catch (Exception e){
            throw e;
        }finally {
            sqlSession.close();
        }

        // 처리 로그 기록
        processLogs.setEntCode(excelUploadCustomsStock.getEntCode());
        processLogs.setEntBusinessCode(excelUploadCustomsStock.getEntBusinessCode());
        processLogs.setSolution("BMS");
        processLogs.setBound(excelUploadCustomsStock.getBound());
        processLogs.setBoundType(excelUploadCustomsStock.getBoundType());
        processLogs.setProcessUserId(excelUploadCustomsStock.getUpdateUserId());
        processLogs.setJobNo("");
        processLogs.setLogJobType("EXCEL");
        processLogs.setDescription("Upload Excel");
        processLogs.setJobLog("");
        processLogs.setResult("ok");
        processLogs.setEntBusinessPrintName(excelUploadCustomsStock.getLogUserBusinessPrintName());
        processLogs.setProcessUserName(excelUploadCustomsStock.getLogUserName());
        processLogs.setProcessUserNameEng(excelUploadCustomsStock.getLogUserNameEng());
        commonService.insertLog(processLogs, request);

        return bmsStockSaveResult;
    }
}
