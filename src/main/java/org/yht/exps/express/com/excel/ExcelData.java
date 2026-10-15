package org.yht.exps.express.com.excel;

import java.util.List;
import java.util.Map;

public class ExcelData {
    private ExcelCallBack excelCallBack;
    int dataSize;

    public ExcelData(ExcelCallBack e){
        excelCallBack = e;
        dataSize = 10000;
    }
    public ExcelData(ExcelCallBack e, int s){
        excelCallBack = e;
        dataSize = s;
    }

    public List<Map<String, Object>> doSearch(Map<String, Object> search) throws Exception{
        return excelCallBack.dataSearch(search);
    }

    public int getDataSize(){
        return dataSize;
    }


}
