package org.yht.exps.express.controller.bms;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BmsExcelUtils  {
    /***
     * 폐기신청_해상_엑셀템플릿 기준 데이터 매핑 처리
     * @param lst
     * @return
     * @throws IOException
     */
    public DownloadFileInfo downloadDisposalExcel(ResourceLoader resourceLoader,  String fileName, List<Map<String, Object>> lst, CustomProperties customProperties) throws IOException {
        String excelFilePath = "classpath:templates/xls/DISPOSAL_SEA_BATCH_REG.xlsx";
       // System.out.println(excelFilePath);
        // 리소스 로드
        Resource resource = resourceLoader.getResource(excelFilePath);
        String excelDir = customProperties.getExcelDownload();
        DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
        if (resource.exists()) {
            try (InputStream is = resource.getInputStream()) {
                Workbook workbook = WorkbookFactory.create(is);
                Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
                int newRowNum = 2; //sheet.getLastRowNum() ;
                Row row = null;
                String cargoReference = "", cargoReference_0 = "", cargoReference_1 = "", cargoReference_2 = "";
                String aCargo[];
                // column set value
                int i = 0;
                for (Map dMap : lst) {
                    newRowNum++;
                    row = sheet.createRow(newRowNum);
                    cargoReference = (String) dMap.getOrDefault("cargoReferenceNo", "");
                    aCargo = cargoReference.split("-");
                    if (aCargo.length == 3) {
                        cargoReference_0 = aCargo[0];
                        cargoReference_1 = aCargo[1];
                        cargoReference_2 = aCargo[2];
                    } else if (aCargo.length == 2) {
                        cargoReference_0 = aCargo[0];
                        cargoReference_1 = aCargo[1];
                        cargoReference_2 = "";
                    } else {
                        cargoReference_0 = aCargo[0];
                        cargoReference_1 = "";
                        cargoReference_2 = "";
                    }
                    i = 0;
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("num", "")));
                    setCellValue(row, i++, cargoReference_0);
                    setCellValue(row, i++, cargoReference_1);
                    setCellValue(row, i++, cargoReference_2);
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("blNo", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("customerName", "")));

                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("businessPresident", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("customerNameByLogin", ""))); /** fromBusinessName */
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("customerTel", ""))); /** tel */
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("customerAddress", ""))); /** fromAddress */
                    //10
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalIssueDate", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalName", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("businessSubRegNo", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalAddr", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("bmsWhCode", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalLoc", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalExpDate", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalMethod", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalReason", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalCt", "")));
                    //20
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalWt", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalAmount", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("dispOutDate", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalFinishDate", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("commodityItemLoc", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("itemSpec", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("stockCt", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("stockWt", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("amountUsd", "")));
                }

                // Save the modified workbook to a new file
                //  String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String timeStamp = new SimpleDateFormat("yyyy-MM").format(new Date());
                // String saveFile = "DISPOSAL_SEA_BATCH_REG_" + timeStamp + ".xlsx";
               /*   UID로 변경됨 if(Common.checkSaveFilenameForExcel(fileName)) {
                    String uuid = UUID.randomUUID().toString();
                    fileName = uuid  ;
                }*/
                String saveFile = fileName + "_" + timeStamp + ".xlsx";


                //workbook파일 출력
                FileUtil.fileWrite(workbook, excelDir, saveFile);


                downloadFileInfo.setSaveFile(saveFile);
                downloadFileInfo.setOriginFile(saveFile);


                //workbook파일 출력
                //FileUtil.fileWrite(wb, excelDir, saveFile);
                /*outPut.put("saveFile",outputFileName);
                try (FileOutputStream outputStream = new FileOutputStream(outputFileName)) {
                    workbook.write(outputStream);
                    System.out.println("Modified data saved to: " + outputFileName);
                }
    */
                workbook.close();
            } catch (IOException ex) {
                log.error(Util.getPrintStacTraceString(ex));
            } finally {
            }
            return downloadFileInfo;
        }else {
            return downloadFileInfo;
        }
     }
    private static void setCellValue(Row row, int columnIndex, String value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
    }

    /**
     * 항공용 가져 오기, 적용 엑셀 파일명 : 22년 3-2차 장치기간 경과 수입(특송)화물 폐기결정목록(성원글로벌).xlsx (07.31)
     * @param resourceLoader
     * @param fileName
     * @param lst
     * @param customProperties
     * @return
     * @throws IOException
     */
    public DownloadFileInfo downloadDisposalAirExcel(ResourceLoader resourceLoader,  String fileName, List<Map<String, Object>> lst, CustomProperties customProperties) throws IOException {
        String excelFilePath = "classpath:templates/xls/DISPOSAL_AIR_BATCH_REG.xlsx";
        // System.out.println(excelFilePath);
        // 리소스 로드
        Resource resource = resourceLoader.getResource(excelFilePath);
        String excelDir = customProperties.getExcelDownload();
        DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
        if (resource.exists()) {
            try (InputStream is = resource.getInputStream()) {
                Workbook workbook = WorkbookFactory.create(is);
                Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
                int newRowNum = 0; //sheet.getLastRowNum() ;
                Row row = null;
                String cargoReference = "", cargoReference_0 = "", cargoReference_1 = "", cargoReference_2 = "";
                String aCargo[];
                // column set value
                int i = 0;
                for (Map dMap : lst) {
                    newRowNum++;
                    row = sheet.createRow(newRowNum);
                    cargoReference = (String) dMap.getOrDefault("cargoReferenceNo", "");
                    aCargo = cargoReference.split("-");
                    if (aCargo.length == 3) {
                        cargoReference_0 = aCargo[0];
                        cargoReference_1 = aCargo[1];
                        cargoReference_2 = aCargo[2];
                    } else if (aCargo.length == 2) {
                        cargoReference_0 = aCargo[0];
                        cargoReference_1 = aCargo[1];
                        cargoReference_2 = "";
                    } else {
                        cargoReference_0 = aCargo[0];
                        cargoReference_1 = "";
                        cargoReference_2 = "";
                    }
                    i = 0;
                     setCellValue(row, i++, String.valueOf(dMap.getOrDefault("num", "")));  //A
                    setCellValue(row, i++, String.valueOf(" ")); // 모조품 공백처리 B
                /*    setCellValue(row, i++, cargoReference_0);
                    setCellValue(row, i++, cargoReference_1);
                    setCellValue(row, i++, cargoReference_2);*/
                    setCellValue(row, i++, cargoReference ); //C
                    setCellValue(row, i++, String.valueOf(" ")); // 모조품 공백처리 D
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("blNo", ""))); //E
                    setCellValue(row, i++, String.valueOf(" ")); // 모조품 공백처리 F

                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("commodityItemLoc", ""))); //G
                    setCellValue(row, i++, String.valueOf(" ")); // 모조품 공백처리 H
                    setCellValue(row, i++, String.valueOf(" ")); // 모조품 공백처리 I
                    setCellValue(row, i++, String.valueOf(" ")); // 모조품 공백처리 J
                    setCellValue(row, i++, String.valueOf(" ")); // 모조품 공백처리 K
                    setCellValue(row, i++, String.valueOf(" ")); // 모조품 공백처리 L

                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalReason", "")));//M
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalCt", "")));//N
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("pkgType", ""))); //O
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("disposalWt", "")));//P
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("kg", "")));//Q
                    //10
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("bmsWhCode", "")));//R
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("customerNameByLogin", ""))); /** fromBusinessName */ //S
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("carryinDate", ""))); //T
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("customerNameByLogin", ""))); /** inBusinessName */   //Uß

              /*      setCellValue(row, i++, String.valueOf(dMap.getOrDefault("totLan", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("itemSpec", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("dispYy", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("dispCha", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("dispHs", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("dispReasonCd", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("fAmt", "")));



                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("customerName", "")));
                    setCellValue(row, i++, String.valueOf(dMap.getOrDefault("status", "")));*/

                }

                // Save the modified workbook to a new file
                //  String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String timeStamp = new SimpleDateFormat("yyyy-MM").format(new Date());
                // String saveFile = "DISPOSAL_SEA_BATCH_REG_" + timeStamp + ".xlsx";

                /*   UID로 변경됨 if(Common.checkSaveFilenameForExcel(fileName)) {
                    String uuid = UUID.randomUUID().toString();
                    fileName = uuid  ;
                }*/
                String saveFile = fileName + "_" + timeStamp + ".xlsx";


                //workbook파일 출력
                FileUtil.fileWrite(workbook, excelDir, saveFile);


                downloadFileInfo.setSaveFile(saveFile);
                downloadFileInfo.setOriginFile(saveFile);


                //workbook파일 출력
                //FileUtil.fileWrite(wb, excelDir, saveFile);
                /*outPut.put("saveFile",outputFileName);
                try (FileOutputStream outputStream = new FileOutputStream(outputFileName)) {
                    workbook.write(outputStream);
                    System.out.println("Modified data saved to: " + outputFileName);
                }
    */
                workbook.close();
            } catch (IOException ex) {
                log.error(Util.getPrintStacTraceString(ex));
            } finally {
            }
            return downloadFileInfo;
        }else {
            return downloadFileInfo;
        }
    }
}
