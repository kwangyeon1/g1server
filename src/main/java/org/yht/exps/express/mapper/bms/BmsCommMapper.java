package org.yht.exps.express.mapper.bms;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsComm.BmsEcmPod;

@Mapper
public interface BmsCommMapper {

	/**
	 *  TB_ECM_POD 01~04 테이블 입력
	 * @param ecmPod
	 * @return
	 */
	void insertBmsEcmPod(BmsEcmPod ecmPod);
	
	List<String> selectInspectionTypeByTicInspectResultCode(); // 검사결과TB_BMS_INSPECT_RESULT 코드타입 (이고) 리스트
}
