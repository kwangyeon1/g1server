package org.yht.excel;

import org.apache.poi.ss.usermodel.*;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelMappingAndSavingProgram {

//    @Test
//    public void name() throws IOException {
//        downloadDisposalExcel();
//    }
//
//    public void downloadDisposalExcel() throws IOException {
//        String excelFilePath = "templates/xls/DISPOSAL_SEA_BATCH_REG.xlsx";
//        ClassPathResource resource = new ClassPathResource(excelFilePath);
//
//        Path path = Paths.get(resource.getURI());
//        System.out.println(resource.getPath());
//        System.out.println(path);
//
//
//        try (FileInputStream inputStream = new FileInputStream(path.toAbsolutePath().toFile())) {
//            Workbook workbook = WorkbookFactory.create(inputStream);
//            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
//            Row rowLast =   sheet.getRow(sheet.getLastRowNum());
//            int lastColumn = rowLast.getLastCellNum();
//            int newRowNum = sheet.getLastRowNum() + 1;
//            Row  row = sheet.createRow(newRowNum);
//            System.out.println(newRowNum +","+ lastColumn);
//            // column set value
//            for ( int i =0 ;  i < newRowNum ; i++){
//                setCellValue(row, i, "id_"+String.valueOf(i));
//            }
//
//            // Save the modified workbook to a new file
//            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//            String outputFileName = "DISPOSAL_SEA_BATCH_REG_" + timeStamp + ".xlsx";
//
//            //workbook파일 출력
//            //FileUtil.fileWrite(wb, excelDir, saveFile);
//
//            try (FileOutputStream outputStream = new FileOutputStream(outputFileName)) {
//                workbook.write(outputStream);
//                System.out.println("Modified data saved to: " + outputFileName);
//            }
//
//            workbook.close();
//        } catch (IOException  ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private static void setCellValue(Row row, int columnIndex, String value) {
//        Cell cell = row.createCell(columnIndex);
//        cell.setCellValue(value);
//    }
}
