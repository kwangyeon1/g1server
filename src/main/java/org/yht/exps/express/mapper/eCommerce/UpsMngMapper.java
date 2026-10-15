package org.yht.exps.express.mapper.eCommerce;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.order.ManifestFile;

@Mapper
public interface UpsMngMapper {
    int selectUpsGroupDup(ManifestFile manifestFile);
}
