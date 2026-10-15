package org.yht.exps.express.model.bms;

import com.google.common.base.CaseFormat;
import org.apache.ibatis.type.Alias;

import java.util.HashMap;

@Alias("camelHashMap")
public class CamelHashMap extends HashMap {

    public Object put(Object key, Object value){
        return super.put(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, (String)key),  value);
    }
}
