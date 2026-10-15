package org.yht.exps.express.com.excel;

import java.util.List;
import java.util.Map;

public interface ExcelCallBack {
    public List<Map<String, Object>> dataSearch(Map<String, Object> search) throws Exception;
}
