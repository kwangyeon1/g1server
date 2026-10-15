package org.yht.exps.express.mapper.eCommerce;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderBatchUpdate;

import java.util.List;
import java.util.Map;

@Mapper
public interface HubnOrderMapper {

    void insertInterfaceInfo(Map<String, Object> checkMap);
    List<Map<String, Object>> selectHubnBatchUpdateHoldTarget(Map batchUpdateMap);

    void deleteResetSendCustomsManifest(SearchOrderBatchUpdate search);
    void updateResetSendCustomsManifest(Map<String, Object> search);
}
