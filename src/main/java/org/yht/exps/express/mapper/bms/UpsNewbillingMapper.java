package org.yht.exps.express.mapper.bms;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.UpsNewbilling;

@Mapper
public interface UpsNewbillingMapper {
	
	List<Map<String, Object>> selectManifestToNewbilling(UpsNewbilling newbilling);
	
	List<Map<String, Object>> selectManifestToNewbillingCustomsTransfer(UpsNewbilling newbilling);
	List<Map<String, Object>> selectLocalNewbillingPCustomsUpdate(UpsNewbilling newbilling);
	void updateCustomsInfo(Map<String, Object> search);
	
//	Map<String, Object> selecteCISProgramCreateWaybill(Map<String, Object> search);
//	List<Map<String, Object>> selecteCISProgramCreateCheck(Map<String, Object> search);
	List<Map<String, Object>> selecteCISProgramCreate(UpsNewbilling newbilling);
	

    
    
    
    
    
    
    
//  김일호 =====================================================================================================
	
    
    
    
    
    
    
    
//  이슬기 =====================================================================================================
	void deleteBatShipmentMInfo(Map<String, Object> dataMap);
	void updateLowToHighWaybillExceptHot(Map<String, Object> dataMap);
	void updatePerrorWaybillMFCSDN(Map<String, Object> dataMap);
	void saveWaybillExceptOneInfo(Map<String, Object> dataMap);
	void updateCustomsIdOd(Map<String, Object> dataMap);
	void updateSimpleDocNoInfo(Map<String, Object> dataMap);
	void updateCustomsIdCdBinfo(Map<String, Object> dataMap);
	void updateDeliveryNoteOd(Map<String, Object> dataMap);
	List<Map<String, Object>> selectLocalNewbillingApply(UpsNewbilling newbilling);
    void updateLocalNewbillingApply(Map<String, Object> dataMap);
    
    Map<String, Object> selectToNewbillingCustomsTransferExcelMblInfo(UpsNewbilling newbilling);
    
    
    
    
    
   
}
