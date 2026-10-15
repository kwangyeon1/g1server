package org.yht.exps.express.service.pda;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.pda.BmsMobPasmMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.mobile.bms.BmsSampleScan;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.util.Util;

import com.google.gson.Gson;
import com.microsoft.sqlserver.jdbc.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsMobPasmService {

	@Autowired
	BmsCommService bmsCommService;

	@Autowired
	BmsMobPasmMapper bmsMobPasmMapper;

	@Autowired
	CommonMapper commonMapper;

	/**
	 * 견품반출 - 박스 번호 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> selectTakeOutSample(Map<String, Object> search) throws Exception {
		Map<String, Object> sampleData = bmsMobPasmMapper.selectTakeOutSample(search);
		boolean isEmptySample = sampleData == null;
		String result = isEmptySample ? "견품반출 대상아님" : "완료";

		if( !isEmptySample && !Util.isEmpty(sampleData.get("RETURN_DATE")) ) result = "재반입(회수됨)";
		else if( !isEmptySample && !Util.isEmpty(sampleData.get("CARRYOUT_TIME")) ) result = "이미반출";

		search.put("scanResult", result);
		insertSampleScanLog(search, false);
		return bmsMobPasmMapper.selectTakeOutSample(search);
	}

	/**
	 * 견품반출 - 견품반출 서명 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public void updateTakeOutSample(Map<String, Object> map, HttpServletRequest req) throws Exception {
		log.debug("[updateTakeOutSample] srchNo : {}, bl_no : {}", map.get("srchNo"), map.get("blNo"));
		bmsMobPasmMapper.updateSampleInOutCt(map);
		bmsMobPasmMapper.updateSampleData(map);

		/** 작업내역 로그시작 */
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(map.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(map.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(map.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_OUT_SAMPLE);
		processLogs.setDescription(BmsConsts.DbType.BMS_OUT_SAMPLE+" [견품반출 서명 저장]");
		processLogs.setJobLog(new Gson().toJson(map));
		processLogs.setJobNo(Util.null2str(map.get("odJobNo")));
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(map.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(map.get("logUserNameEng")));
		bmsCommService.insertLogAndPodStatus(processLogs, req
				, BmsConsts.PodStatus.SAMPLE_SIGNATURE_SAVE /* pod Status 값 매핑 */
				, Util.null2str(  "("+ map.get("srchNo") + ")") // (# 박스번호)
				, StringUtils.EMPTY
		);
	}

	/**
	 * 견품반출 후 입고 - 박스 번호 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectSampleTakingOutList(Map<String, Object> search) throws Exception {
		List<Map<String, Object>> sampleTakingList = bmsMobPasmMapper.selectSampleTakingOutList(search);

		if( ! Util.isEmpty(search.get("srchNo")) ) {
			search.put("scanResult", sampleTakingList.size() > 0 ? "완료" : "무적");
			insertSampleScanLog(search, true);
		}

		return sampleTakingList;
	}

	/**
	 * 견품반출 후 입고 - 견품반출 후 입고 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void updateSampleTakingOutList(Map<String, Object> map, HttpServletRequest req) throws Exception {
		Map<String, Object> paramMap = new HashMap<>();
		for(Map<String, Object> data : (List<Map<String, Object>>) map.get("takingOutList")) {
			paramMap.put("userLanguage", map.get("userLanguage"));
			paramMap.put("entCode", map.get("entCode"));
			paramMap.put("srchGroupCode", "SAMPLE_RESULT");
			paramMap.put("srchCode", Util.null2str(data.get("returnRemarks"), "01"));
			data.put("returnRemarks", commonMapper.selectCodeName(paramMap).get("CODE_NAME"));
			
			bmsMobPasmMapper.updateSampleInOutCt(data);
			bmsMobPasmMapper.updateSampleData(data);
			log.debug("[updateSampleTakingOutList] srchNo : {}, bl_no : {}, sample_remarks : {}", data.get("srchNo"), data.get("blNo"), data.get("SAMPLE_REMARKS"));

			/** 작업내역 로그시작 */
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(map.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(map.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(map.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_OUT_SAMPLE);
			processLogs.setDescription(BmsConsts.DbType.BMS_OUT_SAMPLE+" [견품반출 후 입고 저장]");
			processLogs.setJobLog(new Gson().toJson(map));
			processLogs.setJobNo(Util.null2str(data.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(map.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(map.get("logUserNameEng")));
			bmsCommService.insertLogAndPodStatus(processLogs, req
					, BmsConsts.PodStatus.SAMPLE_TAKING_OUT_IN /* pod Status 값 매핑 */
					, Util.null2str(  "("+ data.get("srchNo") +")") // (# 박스번호)
					, StringUtils.EMPTY
			);
		}
	}

	/**
	 * 견품반출 - 견품반출 스캔 로그 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectScanLogInfo(Map<String, Object> search) throws Exception {
		List<Map<String, Object>> scanDataList = bmsMobPasmMapper.selectScanLogInfo(search);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // KST(한국 시간) 설정

		for (Map<String, Object> map : scanDataList) {
		    if (map.get("REGISTER_DATE_TIME") instanceof Date) { 
		        map.put("REGISTER_DATE_TIME", sdf.format(map.get("REGISTER_DATE_TIME"))); // String 변환
		    }
		}
		return scanDataList;
	}

	/**
	 * 견품반출 - 견품반출 스캔 로그 생성
	 * @param map
	 * @param isInCtMode
	 * @throws Exception
	 */
	private void insertSampleScanLog(Map<String, Object> map, boolean isInCtMode) throws Exception {
		BmsSampleScan scanLog = new BmsSampleScan();
		scanLog.setPackingNo(Util.null2str(map.get("srchNo")));
		scanLog.setBlNo(Util.null2str(map.get("blNo")));
		scanLog.setScanResult(Util.null2str(map.get("scanResult")));
		scanLog.setInOutStatus(isInCtMode ? "I" : "O");
		scanLog.setRegisterUserId(Util.null2str(map.get("updateUserId")));
		bmsMobPasmMapper.insertSampleScanLog(scanLog);
	}
}
