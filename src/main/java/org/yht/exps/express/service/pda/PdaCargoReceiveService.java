package org.yht.exps.express.service.pda;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.pda.PdaCargoReceiveMapper;
import org.yht.exps.express.model.bms.bmsCargo.BmsMasterDest;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo.CargCsclPrgsInfoDtail;
import org.yht.exps.express.service.bms.BmsSendUtils;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PdaCargoReceiveService {

	@Autowired
	PdaCargoReceiveMapper pdaCargoReceiveMapper;

	@Autowired
    BmsSendUtils bmsSendUtils;
	
//	public List<BmsMasterDest> selectMasterScanList(Map<String, Object> search) throws Exception {
//		return pdaCargoReceiveMapper.selectMasterDest(search);
//	}

	public Map<String, Object> selectExistMasterScan(Map<String, Object> search) throws Exception {
		Map<String, Object> inPlan = pdaCargoReceiveMapper.selectExistMasterScan(search);
		return pdaCargoReceiveMapper.selectExistMasterScan(search);
	}

	
	public List<BmsMasterDest> controlBatchMasterDest(Map<String, Object> search) throws Exception {
		List<Map<String, Object>> masterChkList = (List<Map<String, Object>>) search.get("checkedList");
		log.debug("[controlBatchMasterDest] typeMode : {}, editMode : {}", search.get("typeMode"), search.get("editMode"));
		for(Map<String, Object> dataMap : masterChkList) {
			dataMap.put("updateUserId", search.get("updateUserId"));
			if( (String.valueOf(dataMap.get("seq"))).indexOf("C_") != -1 && !"D".equals(search.get("typeMode")) ) {
				setArdateAfterCheckUnipass(dataMap);
				pdaCargoReceiveMapper.insertMasterDest(dataMap);
				log.debug("[controlBatchMasterDest] - Insert Dest : {}", dataMap);
			} else if( "U".equals(search.get("typeMode")) ) {
				if( "Y".equals(Util.null2str(search.get("editMode"))) ) dataMap.put("editDestDate", "20" + dataMap.get("destDateTime"));
				setArdateAfterCheckUnipass(dataMap);
				pdaCargoReceiveMapper.updateMasterDest(dataMap);
				log.debug("[controlBatchMasterDest] - Update Dest : {}", dataMap);
			} else {
				if( (String.valueOf(dataMap.get("seq"))).indexOf("C_") == -1 ) {
					pdaCargoReceiveMapper.deleteMasterDest(dataMap);
				}
				log.debug("[controlBatchMasterDest] - Delete Dest : {}", dataMap);
				//return null;
			}
		}
		search.put("listSize", masterChkList.size());
		return pdaCargoReceiveMapper.selectMasterDest(search);
	}
	
	
	public List<BmsMasterDest> controlBatchMasterDestWeb(Map<String, Object> search) throws Exception {
	    List<Map<String, Object>> masterChkList = (List<Map<String, Object>>) search.get("checkedList");	    
		log.debug("[controlBatchMasterDest] typeMode : {}, editMode : {}", search.get("typeMode"), search.get("editMode"));	    
	    for(Map<String, Object> dataMap : masterChkList) {
	    	String seq = String.valueOf(dataMap.get("seq"));
	    	boolean isNewRow = "null".equals(seq) || seq == null || seq.isEmpty();
	        dataMap.put("updateUserId", search.get("updateUserId"));	     
	       
	        // 삭제 
	        if((String.valueOf(dataMap.get("seq"))).indexOf("C_") == -1 && "D".equals(search.get("typeMode"))) {	
	            if(!isNewRow) {
	                pdaCargoReceiveMapper.deleteMasterDest(dataMap);
	            }
	            log.debug("[controlBatchMasterDest] - Delete Dest : {}", dataMap);	        
	        // 신규 
	        } else if(isNewRow) {
	        	if(!"D".equals(search.get("typeMode"))) {		          
		            dataMap.put("registerUserId", search.get("registerUserId"));
	                dataMap.put("editDestDate", "20" + dataMap.get("destDateTime"));
		            setArdateAfterCheckUnipass(dataMap);
		            pdaCargoReceiveMapper.insertMasterDest(dataMap);
		            log.debug("[controlBatchMasterDest] - Insert Dest : {}", dataMap);
	        	}
	        // 업데이트
	        } else {
	            if( "U".equals(search.get("typeMode"))) {
	            	if( "Y".equals(Util.null2str(search.get("editMode"))) ) dataMap.put("editDestDate", "20" + dataMap.get("destDateTime"));
	                setArdateAfterCheckUnipass(dataMap);
		            pdaCargoReceiveMapper.updateMasterDest(dataMap);
		            log.debug("[controlBatchMasterDest] - Update Dest : {}", dataMap);
	            }	
	        }
	    }
	    
	    search.put("listSize", masterChkList.size());
	    return pdaCargoReceiveMapper.selectMasterDest(search);
	}
	
	public boolean setArdateAfterCheckUnipass(Map<String, Object> dataMap) throws Exception {
			String arDate = Util.null2str(dataMap.get("arDate"));
			String cargoReferenceNo = Util.null2str(dataMap.get("cargoReferenceNo"));
			String mblNo = Util.null2str(dataMap.get("mblNo"));
			String hblNo = Util.null2str(dataMap.get("hblNo"));
	        
			CargoProgressInfo cpInfo = bmsSendUtils.getCargoProgressInfoByParam(cargoReferenceNo, arDate, hblNo, mblNo);
            if(cpInfo == null) {
            	log.info("입항화물 Unipass NULL 조회실패 {}", dataMap);
				return false;
			}
            
            List<CargCsclPrgsInfoDtail> cargoLogList = cpInfo.getCargCsclPrgsInfoDtails();
            
            if (cargoLogList == null) {
                log.info("입항화물 Unipass 정보없음 skip {}", dataMap);
                return false;  // 필터링 대상에서 제외
            }
        	
            List<CargCsclPrgsInfoDtail> latestOutList = cargoLogList.stream()
  	                .filter(detail ->
  	                        "입항보고 수리".equals(detail.getCargTrcnRelaBsopTpcd()))
  	                .sorted(Comparator.nullsLast(
  	                		Comparator.comparingLong( (CargCsclPrgsInfoDtail detail) -> Util.str2longOrZero(detail.getPrcsDttm()) ) 
  	                		).reversed()) // 내림차순 정렬
  	                .collect(Collectors.toList());
            
            if(latestOutList.size() == 0) {
            	log.info("입항화물 Unipass 입항보고 수리 없음 {}", dataMap);
            	return false;
            }
            
            String dt = latestOutList.get(0).getPrcsDttm();
            if(Util.null2str(dt).length() < 12) {
            	log.info("입항화물 Unipass 입항보고 처리날짜가 12자리가 아님 {}", dataMap);
            	return false;
            }
            
            String date = dt.substring(0, 4) + "-" 
            			+ dt.substring(4, 6) + "-" 
            			+ dt.substring(6, 8);

	        String time = dt.substring(8, 10) + ":" 
	                    + dt.substring(10, 12);
	        
	        dataMap.put("arDate", date);
	        dataMap.put("arTime", time);
	        
	        return true;
	}
}