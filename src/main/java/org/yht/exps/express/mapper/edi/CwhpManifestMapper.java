package org.yht.exps.express.mapper.edi;

import org.mapstruct.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CwhpManifestMapper {

    List<Map<String, Object>> selectCwhpXrayInfoCheck(Map<String, Object> paramMap);
    List<Map<String, Object>> selectCwhpInpCheck(Map<String, Object> paramMap);
    void cwhpUpdateXrayInfo(Map<String, Object> paramMap);
    void cwhpDeleteXrayInfoBox(Map<String, Object> paramMap);
    void cwhpInsertXrayInfoBox(Map<String, Object> paramMap);
    void cwhpUpdateXrayInfoSub(Map<String, Object> paramMap);
    void cwhpDeleteContainer(Map<String, Object> paramMap);
    void cwhpInsertContainer(Map<String, Object> paramMap);
    void cwhpUpdateExpect(Map<String, Object> paramMap);
}
