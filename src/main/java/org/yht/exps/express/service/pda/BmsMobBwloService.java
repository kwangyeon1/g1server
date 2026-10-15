package org.yht.exps.express.service.pda;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.pda.BmsMobBwloMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.model.mobile.bms.MobBwlo;
import org.yht.exps.express.model.mobile.bms.MobBwloSaveResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwlo;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsMobBwloService {
	
	@Autowired
	BmsMobBwloMapper bmsMobBwloMapper;
	
	@Autowired
	BmsCommService bmsCommService;
	
	/**
	 * 로케이션재고확인 - 재고수량 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectLocationStock(SearchInList search) throws Exception {
		return bmsMobBwloMapper.selectLocationStock(search);
	}
	
	/**
	 * 로케이션적치 - 로케이션정보 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectLocationInfoDetail(SearchInList search) throws Exception {
		return bmsMobBwloMapper.selectLocationInfoDetail(search);
	}
	
	/**
	 * 로케이션적치 - 반입 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobBwlo selectLocationCarryInDetail(SearchMobBwlo search) throws Exception {
		return bmsMobBwloMapper.selectLocationCarryInDetail(search);
	}

	/**
	 * 로케이션적치 - 장치위치 업데이트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobBwloSaveResult updateLocationCarryIn(SearchMobBwlo search, HttpServletRequest request) throws Exception {
		MobBwloSaveResult result = new MobBwloSaveResult();
		if(bmsMobBwloMapper.updateLocationCarryIn(search) > 0) {
			result.setResultCode("S");
		}else {
			result.setResultCode("F");
		}
		
		/**작업내역 로그시작*/
		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(search.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.getUpdateUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [입력 BL NO:"+String.valueOf(search.getBlNo())+"]");
		processLogs.setJobNo(String.valueOf(search.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setResult("ok");
		processLogs.setProcessUserName(search.getLogUserName());
		processLogs.setProcessUserNameEng(search.getLogUserNameEng());
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.LOCATION_SAVE /* pod Status 값 매핑 */
				, ""
				, search
		);
		/**작업내역 로그끝*/
		
		return result;
	}
}
