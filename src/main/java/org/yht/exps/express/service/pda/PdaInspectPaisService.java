package org.yht.exps.express.service.pda;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.pda.PdaInspectPaisMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.BmsInspectResultRst;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PdaInspectPaisService {
	
	/** POD 비고 최대 길이(글자 수) - TB_ECM_POD_05.REMARKS NVARCHAR(200) */
	private static final int POD_REMARKS_MAX_LENGTH = 200;
	
	/** 검사추가 POD 비고의 검사 구분명 (검사코드 접두어 → 구분명, 검사사유관리 메시지의 선별/XRAY/통관/검역 표기와 동일) */
	private static final Map<String, String> INSPECTION_PREFIX_LABEL_MAP = Map.of("S", "선별", "X", "XRAY", "P", "통관", "I", "검역");
	
	@Autowired
	PdaInspectPaisMapper pdaInspectPaisMapper;
	
	@Autowired
	BmsCommService bmsCommService;
	
	@Autowired
	CommonMapper commonMapper;
	
	
	public BmsInspectResultRst selectBmsInWithInspectResult(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInPlanPkgResult = new BmsInspectResultRst();
		
		List<BmsInspectResult> bmsInspectResultList = pdaInspectPaisMapper.selectBmsInWithInspectResult(search);
		bmsInPlanPkgResult.setBmsInspectResultList(bmsInspectResultList);
		
		bmsInPlanPkgResult.setResultCode("A012051");
		bmsInPlanPkgResult.setReason("완료");
		
		return bmsInPlanPkgResult;
	}
	
	public BmsInspectResultRst selectBmsInWithInspectResultV2(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInPlanPkgResult = new BmsInspectResultRst();
		
		List<BmsInspectResult> bmsInspectResultList = pdaInspectPaisMapper.selectBmsInWithInspectResultV2(search);
		bmsInPlanPkgResult.setBmsInspectResultList(bmsInspectResultList);
		
		bmsInPlanPkgResult.setResultCode("A012051");
		bmsInPlanPkgResult.setReason("완료");
		
		return bmsInPlanPkgResult;
	}
	
	public BmsInspectResultRst selectBmsInspectResultPicking(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInspectResultRst = new BmsInspectResultRst();
		
		List<BmsInspectResult> bmsInspectResultList = pdaInspectPaisMapper.selectBmsInspectResultPicking(search);
		bmsInspectResultRst.setBmsInspectResultList(bmsInspectResultList);
		
		return bmsInspectResultRst;
	}
	
	public BmsInspectResultRst updateBmsInspectResultPicking(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInspectResultRst = new BmsInspectResultRst();
		Map<String, Object> logMap = new HashMap<String, Object>();
		
		if("Y".equals(Util.null2str(search.get("unManifested")))) {
			bmsInspectResultRst.setReason("무적");
			logMap.put("blNo", "");
		} else {
			List<BmsInspectResult> bmsInspectResultList = pdaInspectPaisMapper.selectBmsInspectResultPicking(search);
			bmsInspectResultRst.setUpdateYn("N");
			
			if(bmsInspectResultList.size() > 0) { /** 검사대상이면 */
				bmsInspectResultRst.setReason("완료");
				bmsInspectResultRst.setBmsInspectResultList(bmsInspectResultList);
				logMap.put("blNo", bmsInspectResultList.get(0).getBlNo()); // 스캔로그 파라미터 set
				
				search.put("inspectStatus", "P"); // P: 피킹, E: 완료
				pdaInspectPaisMapper.updateBmsInPlanPkgInspectStatus(search);
				
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("inPlanNo", bmsInspectResultList.get(0).getInPlanNo());
				paramMap.put("packingNo", null);
				paramMap.put("entCode", Util.null2str(search.get("entCode")));
				paramMap.put("srchCdType", Util.null2str(search.get("srchCdType")));
				paramMap.put("defaultSort", Util.null2str(search.get("defaultSort")));
				paramMap.put("inspectStatus", Util.null2str(search.get("inspectStatus"))); // P: 피킹, E: 완료
				List<BmsInspectResult> bmsInspectNotPickingList = pdaInspectPaisMapper.selectBmsInspectResultPicking(paramMap);
				
				if(bmsInspectNotPickingList.size() == 0) { // BL단위로 박스를 다찍어야 처리
					paramMap.put("registerUserId", Util.null2str(search.get("registerUserId")));
					paramMap.put("inNo", bmsInspectResultList.get(0).getInNo());
					pdaInspectPaisMapper.updateBmsInspectResultStartInfo(paramMap);
					bmsInspectResultRst.setUpdateYn("Y");
					bmsInspectResultRst.setReason("검사피킹");
				}
				
				/** pod 저장 */
				CarryIn carryIn = new CarryIn();
				carryIn.setPdaYn("Y");
				String remark = Util.null2str(bmsInspectResultList.get(0).getBmsInspectionType()) + "("+ bmsInspectResultList.get(0).getPackingNo() +")";
				
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(search.get("entCode")));
				processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [PDA 검사피킹]");
				processLogs.setJobLog(new Gson().toJson(search));
				processLogs.setJobNo(Util.null2str(bmsInspectResultList.get(0).getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setResult("ok");
				processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
				processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.INSPECT_PICKING /* pod Status 값 매핑 */
						, remark // 비고 "기타" (# 박스번호) - (검사유형)
						, carryIn
						);
				
			} else {
				bmsInspectResultRst.setReason("검사대상아님");
				// 검사추가를 위해 반입,박스에서 조회
				List<InPlanPkg> inPlanPkgList = pdaInspectPaisMapper.selectBmsInForAddInspectResult(search);
				if(inPlanPkgList.size() > 0) {
					logMap.put("blNo", inPlanPkgList.get(0).getBlNo());
					bmsInspectResultRst.setInPlanPkgList(inPlanPkgList);
				} else {
					logMap.put("blNo", "");
					bmsInspectResultRst.setReason("미반입");
				}
			}
		}
		
		// 검사스캔로그 저장
		logMap.put("packingNo", Util.null2str(search.get("packingNo")));
		logMap.put("inspectStatus", "P");
		logMap.put("result", bmsInspectResultRst.getReason());
		logMap.put("registerUserId", Util.null2str(search.get("registerUserId")));
		pdaInspectPaisMapper.insertBmsInspectScanLog(logMap);
		
		return bmsInspectResultRst;
	}
	
	/**
	 * 검사스캔로그 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectBmsInspectScanLogInfo(Map<String, Object> search) throws Exception {
		List<Map<String, Object>> list = pdaInspectPaisMapper.selectBmsInspectScanLogInfo(search);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // KST(한국 시간) 설정

		for (Map<String, Object> map : list) {
		    if (map.get("REGISTER_DATE_TIME") instanceof Date) { 
		        map.put("REGISTER_DATE_TIME", sdf.format(map.get("REGISTER_DATE_TIME"))); // String 변환
		    }
		}
		return list;
	}
	
	/**
	 * scan: 검사완료 - 박스 검사상태설정 (박스 검사상태 업데이트)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInspectResultRst updateBmsInPkgInspectionStatusE(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInspectResultRst = new BmsInspectResultRst();
		
		bmsInspectResultRst.setResultCode("A012051");
		String reason = "";
		
		if( "Y".equals(Util.null2str(search.get("unManifested"))) ) {
			reason = "무적";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", "");
			logParamg.put("result", reason);
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			bmsInspectResultRst.setReason(reason);
			return bmsInspectResultRst;
		}
		
		Map<String, Object> scanBmsIn = pdaInspectPaisMapper.selectOneBmsInByPackingNo(search);
		if(scanBmsIn == null) {
			reason = "검사대상아님";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", "");
			logParamg.put("result", reason);
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			bmsInspectResultRst.setReason(reason);
			return bmsInspectResultRst;
		}
		
		InPlanPkg scanInPlanPkg = pdaInspectPaisMapper.selectPkgByPackingNo(search);
		if("E".equals(scanInPlanPkg.getInspectStatus())) {
			reason = "이미찍음";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", scanInPlanPkg.getBlNo());
			logParamg.put("result", reason);
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			bmsInspectResultRst.setReason(reason);
		}
		
		if("".equals(scanInPlanPkg.getInspectStatus()) || "P".equals(scanInPlanPkg.getInspectStatus())) {
			reason = "찍음";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", scanInPlanPkg.getBlNo());
			logParamg.put("result", reason);
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			search.put("inspectStatus", "E");
			pdaInspectPaisMapper.updateInspectStatusOfPkg(search);
			
			bmsInspectResultRst.setReason(reason);
		}
		
		search.put("inPlanNo", scanInPlanPkg.getInPlanNo());
		int notInspectPkgCnt = pdaInspectPaisMapper.checkAllPkgNotInspectE(search);
		if(notInspectPkgCnt>0) {
			return bmsInspectResultRst;
		}
		
		reason = "검사완료";
		
		scanBmsIn.put("inPlanNo", scanBmsIn.get("IN_PLAN_NO"));
		bmsInspectResultRst.setBmsInMap(scanBmsIn);
		bmsInspectResultRst.setReason(reason);
		return bmsInspectResultRst;
	}
	
	/**
	 * scan: 검사완료 - 박스 검사상태설정 (박스 검사상태 업데이트)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInspectResultRst updateBmsInPkgInspectionStatusEV2(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInspectResultRst = new BmsInspectResultRst();
		
		bmsInspectResultRst.setResultCode("A012051");
		String reason = "";
		
		if( "Y".equals(Util.null2str(search.get("unManifested"))) ) {
			reason = "무적";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", "");
			logParamg.put("result", reason);
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			bmsInspectResultRst.setReason(reason);
			return bmsInspectResultRst;
		}
		
		Map<String, Object> scanBmsIn = pdaInspectPaisMapper.selectOneBmsInByPackingNoV2(search);
		if(scanBmsIn == null) {
			reason = "검사대상아님";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", "");
			logParamg.put("result", reason);
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			bmsInspectResultRst.setReason(reason);
			return bmsInspectResultRst;
		}
		
		InPlanPkg scanInPlanPkg = pdaInspectPaisMapper.selectPkgByPackingNo(search);
		if("E".equals(scanInPlanPkg.getInspectStatus())) {
			reason = "이미찍음";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", scanInPlanPkg.getBlNo());
			logParamg.put("result", reason);
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			bmsInspectResultRst.setReason(reason);
		}
		
		if("".equals(scanInPlanPkg.getInspectStatus()) || "P".equals(scanInPlanPkg.getInspectStatus())) {
			reason = "찍음";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", scanInPlanPkg.getBlNo());
			logParamg.put("result", reason);
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			search.put("inspectStatus", "E");
			pdaInspectPaisMapper.updateInspectStatusOfPkg(search);
			
			bmsInspectResultRst.setReason(reason);
		}
		
		search.put("inPlanNo", scanInPlanPkg.getInPlanNo());
		int notInspectPkgCnt = pdaInspectPaisMapper.checkAllPkgNotInspectE(search);
		if(notInspectPkgCnt>0) {
			return bmsInspectResultRst;
		}
		
		reason = "검사완료";
		
		scanBmsIn.put("inPlanNo", scanBmsIn.get("IN_PLAN_NO"));
		bmsInspectResultRst.setBmsInMap(scanBmsIn);
		bmsInspectResultRst.setReason(reason);
		return bmsInspectResultRst;
	}
	
	/**
	 * 검사완료 - 일괄검사완료 (검사완료 업데이트)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInspectResultRst batchInspectResultFinish(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInspectResultRst = new BmsInspectResultRst();
		
		bmsInspectResultRst.setResultCode("A012051");
		String reason = "";
		
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) search.get("dataList");
		if(dataList == null || (dataList != null && dataList.size() == 0)) {
			bmsInspectResultRst.setResultCode("A11481"); //해당 #1# 데이터는 존재하지 않습니다.
			return bmsInspectResultRst;
		}
		
		List<Integer> inPlanNoList = dataList.stream().map(m -> (Integer) Integer.valueOf(m.get("inPlanNo").toString())).collect(Collectors.toList());
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("inPlanNoList", inPlanNoList);
		param.put("cdType", Util.null2str(search.get("cdType")));
		List<InPlanPkg> inPlanPkgList = pdaInspectPaisMapper.selectPkgBmsInByInPlanNo(param);
		/*if(inPlanPkgList == null || (inPlanPkgList != null && inPlanPkgList.size() == 0)) {
			bmsInspectResultRst.setResultCode("A11481"); //해당 #1# 데이터는 존재하지 않습니다.
			return bmsInspectResultRst;
		}*/

		/** 일괄검사완료 처리시 dataList에 최대값 박스번호만 저장되기 때문에 박스번호를 조회하여 리스트에 덮어씀 */
		// key는 "IN_PLAN_NO_BL_NO" 문자열
		// value는 해당 key에 해당하는 박스번호 값들을 순서대로 담은 Queue
		Map<String, Queue<String>> packingMap = new HashMap<>();
		for (InPlanPkg pkg : inPlanPkgList) {
		    String key = pkg.getInPlanNo() + "_" + pkg.getBlNo();

		    // key가 존재하지 않으면 새 Queue 생성 후 add
		    // 존재하면 기존 Queue에 add
		    packingMap
		        .computeIfAbsent(key, k -> new LinkedList<>()) // 없으면 새 Queue 생성
		        .add(pkg.getPackingNo()); // 박스번호를 Queue에 넣음
		}
		
		// dataList를 순회하면서 inPlanNoList에서 가져온 박스번호로 덮어쓰기
		for(Map<String, Object> dataMap : dataList) {
			String key = dataMap.get("inPlanNo") + "_" + dataMap.get("blNo");
		    Queue<String> packingQueue = packingMap.get(key);

		    // 해당 key가 존재하고, Queue에 값이 남아 있다면
		    if (packingQueue != null && !packingQueue.isEmpty()) {
		    	String newPackingNo = packingQueue.poll(); // Queue에서 값을 꺼내고 제거 (FIFO)
		        dataMap.put("packingNo", newPackingNo); // 박스번호 값 업데이트
		    }
		}
		
		reason = "일괄완료";
		String beforePackingNo = "";
		for(Map<String, Object> data : dataList) {
			Map<String, Object> finishParm = new HashMap<String, Object>();
			finishParm.put("inNo", data.get("inNo"));
			finishParm.put("inspectionResultType", data.get("inspectionResultType"));
			finishParm.put("inspectionFinishYn", "Y");
			finishParm.put("remarks", data.get("reason"));
			finishParm.put("updateUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.updateInspectResultFinishByInspectionTypeList(finishParm);
			
			String packingNo = Util.null2str(data.get("packingNo"));
			if(!beforePackingNo.equals(packingNo)) {
				beforePackingNo = packingNo;
				
				Map<String, Object> statusParam = new HashMap<String, Object>();
				statusParam.put("srchInPlanNo", Util.null2str(data.get("inPlanNo")));
				statusParam.put("srchBlNo", Util.null2str(data.get("blNo")));
				statusParam.put("scanPackingNo", Util.null2str(data.get("packingNo")));
				statusParam.put("inspectStatus", "E");
				statusParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				pdaInspectPaisMapper.updateInspectStatusOfPkg(statusParam);
				
				Map<String, Object> logParamg = new HashMap<String, Object>();
				logParamg.put("packingNo", Util.null2str(data.get("packingNo")));
				logParamg.put("inspectionStatus", "E");
				logParamg.put("blNo", Util.null2str(data.get("blNo")));
				logParamg.put("result", reason);
				logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
				pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			}
		}
		
		int beforeInNo = -1;
		for(Map<String, Object> data : dataList) {
			int inNo = Util.null2int(data.get("inNo"));
			
			if(beforeInNo != inNo) { // 반입번호 1개당 1번씩만 실행 (이전 반입번호가 현재 반입번호가 일치하지않을때)
				beforeInNo = inNo;
				Map<String, Object> bmsInUpdateParam = new HashMap<String, Object>();
				bmsInUpdateParam.put("inNo", inNo);
				List<BmsInspectResult> notFinishedResultList = pdaInspectPaisMapper.selectInspectResultNotFinished(bmsInUpdateParam);
				if(notFinishedResultList != null && notFinishedResultList.size() > 0) { // TB_BMS_INSPECT_RESULT의 IN_NO를 그룹으로 했을때 그그룹의 모든 검사가 완료안됬을 경우 Skip (continue;키워드)
					continue;
				}
				
				bmsInUpdateParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				bmsInUpdateParam.put("allInspectionFinishYn", "Y");
				pdaInspectPaisMapper.updateBmsInAllInpectionFinishYnByInNoList(bmsInUpdateParam);
				
				List<Map<String, Object>> grouped = dataList.stream()
						.filter(map -> inNo == Util.null2int(map.get("inNo")))
						.collect(Collectors.toList());
				
				String podRemarks = "일괄 검사완료(" + Util.null2str(data.get("packingNo")) + ")";
				if(grouped.size() - 1 > 0) {
					podRemarks = "일괄 검사완료(" + Util.null2str(data.get("packingNo")) + " 외 " + (grouped.size() - 1) + ")";
				}
				
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(search.get("entCode")));
				processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN_PLAN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN_PLAN+" [PDA 검사완료 업데이트]");
				processLogs.setJobLog(new Gson().toJson(search));
				processLogs.setJobNo(Util.null2str(data.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setResult("ok");
				processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
				processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.INSPECTION_FINISH_YN /* pod Status 값 매핑 */
						, podRemarks
						, "일괄 검사완료"
				);
			}
		}
		
		bmsInspectResultRst.setReason(reason);
		
		return bmsInspectResultRst;
	}
	
	/**
	 * 모든 이고 검사항목 완료(Y)처리
	 * @param bmsIn
	 * @param search
	 * @return boolean
	 * @throws Exception
	 */
	public boolean updateInspectionTypeByTicInspectResultCode(Map<String, Object> search) throws Exception {
		Map<String, Object> inspectResultParam = new HashMap<String, Object>();
		List<String> ticInspectionTypeList = pdaInspectPaisMapper.selectInspectionTypeByTicInspectResultCode(); // 이고검사 INSPECTION_TYPE 문자열 리스트
		
		if(Util.null2int(search.get("IN_NO")) > 0 || Util.null2int(search.get("inNo")) > 0) {
			int _inNo = Util.null2int(search.get("IN_NO")) > 0 ? Util.null2int(search.get("IN_NO")) : Util.null2int(search.get("inNo"));
			inspectResultParam.put("inNo", _inNo);
		}
		
		inspectResultParam.put("typeList", ticInspectionTypeList);
		inspectResultParam.put("inspectionFinishYn", "Y");
		inspectResultParam.put("remarks", "이상없음");
		inspectResultParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
		int updatedCnt = pdaInspectPaisMapper.updateInspectResultFinishByTicType(inspectResultParam);
		
		return updatedCnt > 0;
	}
	
	/**
	 * 반입 모든 검사항목 PDA 완료여부 체크후, 반입의 all inspection yn 완료(Y)처리
	 * @param bmsIn
	 * @param search
	 * @return boolean
	 * @throws Exception
	 */
	public boolean updateBmsInAllInspectionFinishYn(Map<String, Object> search) throws Exception {
		search.put("allInspectionFinishYn", "N");
		Map<String, Object> bmsIn = new HashMap<String, Object>();
		
		Map<String, Object> bmsInParam = new HashMap<String, Object>();
		if(Util.null2int(search.get("IN_NO")) > 0 || Util.null2int(search.get("inNo")) > 0) {
			int _inNo = Util.null2int(search.get("IN_NO")) > 0 ? Util.null2int(search.get("IN_NO")) : Util.null2int(search.get("inNo"));
			bmsInParam.put("inNo", _inNo);
			bmsIn = pdaInspectPaisMapper.selectBmsInByInNoOrInPlanNo(bmsInParam);
		}else if(!"".equals(Util.null2str(search.get("CARGO_REFERENCE_NO"))) || !"".equals(Util.null2str(search.get("cargoReferenceNo")))) {
			String _cargoReferenceNo = !"".equals(Util.null2str(search.get("CARGO_REFERENCE_NO"))) ? Util.null2str(search.get("CARGO_REFERENCE_NO")) : Util.null2str(search.get("cargoReferenceNo"));
			bmsInParam.put("cargoReferenceNo", _cargoReferenceNo);
			bmsIn = pdaInspectPaisMapper.selectBmsInByCargoNo(bmsInParam);
		}else if(Util.null2int(search.get("IN_PLAN_NO")) > 0 || Util.null2int(search.get("inPlanNo")) > 0) {
			int _inPlanNo = Util.null2int(search.get("IN_PLAN_NO")) > 0 ? Util.null2int(search.get("IN_PLAN_NO")) : Util.null2int(search.get("inPlanNo"));
			bmsInParam.put("inPlanNo", _inPlanNo);
			bmsIn = pdaInspectPaisMapper.selectBmsInByInNoOrInPlanNo(bmsInParam);
		}
		
		Map<String, Object> inspectResultParam = new HashMap<String, Object>();
		inspectResultParam.put("inNo", bmsIn.get("IN_NO"));
		List<BmsInspectResult> notFinishedResultList = pdaInspectPaisMapper.selectInspectResultNotFinished(inspectResultParam);
		if(notFinishedResultList == null || notFinishedResultList != null && notFinishedResultList.size() == 0) { // TB_BMS_INSPECT_RESULT의 IN_NO에 대한 모든 검사가 완료된경우
			
			if( !( "N".equals(Util.null2str(bmsIn.get("BIG_SIZE_YN"))) && "N".equals(Util.null2str(bmsIn.get("INSPECTION_CUSTOMS_YN"))) && "N".equals(Util.null2str(bmsIn.get("INTELLECTUAL_PROP_YN"))) &&
					"N".equals(Util.null2str(bmsIn.get("CRIME_SELECTION_YN"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("SE_INSPECTION_CODE"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("XRAY_INSPECTION_CODE"))) &&
					StringUtils.isEmpty(Util.null2str(bmsIn.get("INSPECTION_TYPE"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("INSPECTION_TYPE_A"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("INSPECTION_TYPE_B"))) &&
					StringUtils.isEmpty(Util.null2str(bmsIn.get("PALN5EM_INSPECTION_CODE"))) ) ) { //검사 항목이 1개라도 있으면
				search.put("allInspectionFinishYn", "Y");
			}
		}
		
		search.put("inNo", bmsIn.get("IN_NO"));
		int updatedCnt = pdaInspectPaisMapper.updateBmsInAllInpectionFinishYnByInNoList(search);
		
		return "Y".equals(Util.null2str(search.get("allInspectionFinishYn"))) && updatedCnt > 0;
	}
	
	public BmsInspectResultRst selectInspectResultByInNo(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInPlanPkgResult = new BmsInspectResultRst();
		
		if(search == null) {
			bmsInPlanPkgResult.setResultCode("A11481");
			return bmsInPlanPkgResult;
		}
		
		if("".equals(Util.null2str(search.get("entCode")))) {
			bmsInPlanPkgResult.setResultCode("A11481");
			return bmsInPlanPkgResult;
		}
		
		if("".equals(Util.null2str(search.get("inPlanNo"))) && "".equals(Util.null2str(search.get("inNo")))) {
			bmsInPlanPkgResult.setResultCode("A11481");
			return bmsInPlanPkgResult;
		}
		
		List<BmsInspectResult> bmsInspectResultList = pdaInspectPaisMapper.selectInspectResultCodeValueByInPlanNoOrInNo(search);
		bmsInPlanPkgResult.setBmsInspectResultList(bmsInspectResultList);
		
		if(bmsInspectResultList == null) {
			bmsInPlanPkgResult.setResultCode("A11481");
			return bmsInPlanPkgResult;
		}
		
		bmsInPlanPkgResult.setResultCode("A012051");
		bmsInPlanPkgResult.setReason("완료");
		
		return bmsInPlanPkgResult;
	}
	
	/**
	 * 검사결과 추가
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInspectResultRst addInspectResult(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInspectResultRst = new BmsInspectResultRst();
		
		bmsInspectResultRst.setResultCode("A012051");
		String reason = "";
		
		Map<String, Object> bmsIn = pdaInspectPaisMapper.selectBmsInByInPlanNo(search);
		if(bmsIn == null) {
			return bmsInspectResultRst;
		}
		
		String[] fields = {
		    !"".equals(Util.null2str(search.get("seInspectionCode"))) ? "S" + Util.null2str(search.get("seInspectionCode")): "",
		    !"".equals(Util.null2str(search.get("inspectionType"))) ? "I" + Util.null2str(search.get("inspectionType")): "",
		    !"".equals(Util.null2str(search.get("paln5emInspectionCode"))) ? "P" + Util.null2str(search.get("paln5emInspectionCode")): "",
		    !"".equals(Util.null2str(search.get("xrayInspectionCode"))) ? "X" + Util.null2str(search.get("xrayInspectionCode")): ""
		};
		
		List<String> inspectionByRequestList = Arrays.stream(fields)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
		if(inspectionByRequestList.size() == 0) {
			return bmsInspectResultRst;
		}
		
		List<String> ticInspectionTypeList = pdaInspectPaisMapper.selectInspectionTypeByTicInspectResultCode();
		
		Map<String, Object> inNoInspectResultParam = new HashMap<String, Object>();
		inNoInspectResultParam.put("inNo", bmsIn.get("IN_NO"));
		List<Map<String, Object>> rstList = pdaInspectPaisMapper.selectInspectResultByInNo(inNoInspectResultParam); // TB_BMS_INSPECT_RESULT의 IN_NO에 해당하는 INSPECTION_TYPE(검사종류)리스트
		
		List<String> dbInspectionList = rstList.stream()
				.filter(rst -> ticInspectionTypeList.stream().anyMatch( ticInspectionType -> ticInspectionType.equals(Util.null2str(rst.get("INSPECTION_TYPE"))) ) ) // 이고검사건만
				.filter(rst -> !"검사변경으로인한완료처리".equals(Util.null2str(rst.get("INSPECTION_FINISH_REMARKS"))) ) // "검사변경으로인한완료처리" 는 제외함
				.map(rst -> Util.null2str(rst.get("INSPECTION_TYPE"))).collect(Collectors.toList()); // INSPECTION_TYPE값들을 모아서 List<String>형태로 리스트화
		for(String inspectionByRequest : inspectionByRequestList) {
			for(String dbInspection : dbInspectionList) {// 반입의 모든 이고검사
				if(inspectionByRequest.substring(0, 1).equals(dbInspection.substring(0, 1)) // DB등록된 이고 검사유형(S, X등)와 req요청 검사유형(S, X등)가 같은경우
						&& !ticInspectionTypeList.stream().anyMatch(ticInspectionType -> ticInspectionType.equals(inspectionByRequest)) ) { // and 일반검사코드인경우( req요청 검사코드가 이고검사코드가 아닌경우 )
					String ticName = Stream.of(
					        Map.entry("S", "선별"),
					        Map.entry("P", "통관"),
					        Map.entry("X", "xray")
					).filter(e -> inspectionByRequest.startsWith(e.getKey())).map(Map.Entry::getValue).findFirst().orElse("");
					
					bmsInspectResultRst.setResultCode("ABMS100068"); // #1#
					bmsInspectResultRst.setReason(ticName + "은(는) 이고검사기록이 있기 때문에 추가할 수 없습니다.");
					return bmsInspectResultRst;
				}
			}
		}
		
		
		// 반입(TB_BMS_IN) UPDATE, 반입예정(TB_BMS_IN_PLAN) UPDATE
		pdaInspectPaisMapper.updateBmsInAndPlanAboutInspection(search);
		
		List<String> typeList = inspectionByRequestList.stream()
				.map(inspectionType -> inspectionType.substring(0, 1))
				.collect(Collectors.toList());
		Map<String, Object> finishParam = new HashMap<String, Object>();
		finishParam.put("inNo", bmsIn.get("IN_NO"));
		finishParam.put("typeList", typeList);
		finishParam.put("inspectionFinishYn", "Y");
		finishParam.put("remarks", "검사변경으로인한완료처리");
		finishParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
		pdaInspectPaisMapper.updateInspectResultFinishWhenExistType(finishParam);
		
		Set<String> addedInspectionTypeSet = new LinkedHashSet<String>(); // 이번 검사추가로 추가 처리된 검사코드 (POD 비고용, 요청 순서 유지/중복 제거)
		for(String inspectionByRequest : inspectionByRequestList) {
			if(!"delete".equals(inspectionByRequest.substring(1).trim())) {
				Map<String, Object> insertInspectResult = new HashMap<String, Object>();
				insertInspectResult.put("inNo", bmsIn.get("IN_NO"));
				insertInspectResult.put("inspectionType", inspectionByRequest);
				insertInspectResult.put("cargoReferenceNo", bmsIn.get("CARGO_REFERENCE_NO"));
				insertInspectResult.put("updateUserId", Util.null2str(search.get("updateUserId")));
				int result = pdaInspectPaisMapper.updateInspectResultStatusInit(insertInspectResult);
				if(result == 0) pdaInspectPaisMapper.insertInspectResult(insertInspectResult);
				addedInspectionTypeSet.add(inspectionByRequest.trim()); // TB_BMS_INSPECT_RESULT.INSPECTION_TYPE 값
			}
		}
		List<String> addedInspectionNameList = new ArrayList<String>(); // 추가된 검사의 비고 항목 "구분:한글명" (POD 비고용, 요청 순서 유지)
		for(String addedInspectionType : addedInspectionTypeSet) {
			addedInspectionNameList.add(selectInspectionTypeRemarkItem(Util.null2str(search.get("entCode")), addedInspectionType));
		}
		
		search.put("allInspectionFinishYn", "N");

		Map<String, Object> inspectResultParam = new HashMap<String, Object>();
		inspectResultParam.put("inNo", bmsIn.get("IN_NO"));
		List<BmsInspectResult> notFinishedResultList = pdaInspectPaisMapper.selectInspectResultNotFinished(inspectResultParam);
		if(notFinishedResultList == null || notFinishedResultList != null && notFinishedResultList.size() == 0) { // TB_BMS_INSPECT_RESULT의 IN_NO에 대한 모든 검사가 완료된경우
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("packingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", Util.null2str(search.get("blNo")));
			logParamg.put("result", "검사추가(All Y)");
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			if( !( "N".equals(Util.null2str(bmsIn.get("BIG_SIZE_YN"))) && "N".equals(Util.null2str(bmsIn.get("INSPECTION_CUSTOMS_YN"))) && "N".equals(Util.null2str(bmsIn.get("INTELLECTUAL_PROP_YN"))) &&
					"N".equals(Util.null2str(bmsIn.get("CRIME_SELECTION_YN"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("SE_INSPECTION_CODE"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("XRAY_INSPECTION_CODE"))) &&
					StringUtils.isEmpty(Util.null2str(bmsIn.get("INSPECTION_TYPE"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("INSPECTION_TYPE_A"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("INSPECTION_TYPE_B"))) &&
					StringUtils.isEmpty(Util.null2str(bmsIn.get("PALN5EM_INSPECTION_CODE"))) ) ) { //검사 항목이 1개라도 있으면
				search.put("allInspectionFinishYn", "Y");
			}
			
			bmsInspectResultRst.setResultCode("A012051");
		}

		/* 사유 저장 */
		search.put("inNo", bmsIn.get("IN_NO"));
		pdaInspectPaisMapper.updateBmsInAllInpectionFinishYnByInNoList(search);
		
		reason = "완료";
			
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN_PLAN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN_PLAN+" [PDA 검사추가 생성]");
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobNo(Util.null2str(search.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.INSPECTION_FINISH_YN /* pod Status 값 매핑 */
				, buildAddInspectPodRemarks(Util.null2str(search.get("podRemarks")), addedInspectionNameList) // 비고: 사유 + 추가된 검사 "구분:한글명"(" | " 구분, 200자 이내)
				, "검사추가"
		);
		
		bmsInspectResultRst.setReason(reason);
		
		return bmsInspectResultRst;
	}
	
	/**
	 * 검사추가 POD(TB_ECM_POD_05) 비고 생성
	 * - 기존 비고 뒤에 공백 한 칸을 두고 이번에 추가된 검사의 "구분:한글명" 항목을 " | "로 연결한다. (예: "이상없음 선별:검역 | XRAY:식품,의약품")
	 * - 한글명에 콤마("식품,의약품")나 슬래시("검사(일반/간이)")가 들어간 코드가 있어 항목 구분자는 " | "를 쓴다.
	 * - 기존 비고가 비어 있으면 항목 목록만 넣는다. (예: "선별:검역 | XRAY:식품,의약품")
	 * - TB_ECM_POD_05.REMARKS NVARCHAR(200)를 넘지 않도록 항목은 자르지 않고 들어가는 항목까지만 넣는다.
	 * - 기존 비고만으로 200자를 넘으면 기존 비고를 200자로 자르고 항목은 넣지 않는다.
	 * - 추가된 검사코드가 없으면 기존 비고를 그대로 반환한다. (기존 동작 유지)
	 * - insertLogAndPodStatus / BmsCommMapper.insertBmsEcmPod 는 REMARKS 에 문자열을 덧붙이지 않으므로 이 반환값이 최종 저장값이다.
	 * @param baseRemarks 기존 비고 (요청 podRemarks)
	 * @param addedInspectionNames 이번 요청에서 추가 처리된 검사의 비고 항목 (selectInspectionTypeRemarkItem 변환값, 요청 순서)
	 * @return TB_ECM_POD_05.REMARKS 저장값
	 */
	private static String buildAddInspectPodRemarks(String baseRemarks, List<String> addedInspectionNames) {
		String remarks = Util.null2str(baseRemarks);
		if(addedInspectionNames == null || addedInspectionNames.isEmpty()) {
			return remarks; // 추가된 검사코드 없음 → 기존 동작 그대로
		}
		if(remarks.length() > POD_REMARKS_MAX_LENGTH) {
			return remarks.substring(0, POD_REMARKS_MAX_LENGTH); // 기존 비고만으로 초과 → 200자로 자르고 항목은 넣지 않음
		}
		
		StringBuilder remarksBuilder = new StringBuilder(remarks);
		boolean isFirstName = true;
		for(String inspectionName : addedInspectionNames) {
			String separator = isFirstName ? ("".equals(remarks) ? "" : " ") : " | "; // 항목 구분자
			if(remarksBuilder.length() + separator.length() + inspectionName.length() > POD_REMARKS_MAX_LENGTH) {
				break; // 다음 항목이 구분자 포함 남은 길이를 넘으면 중단 (항목은 중간에 자르지 않음)
			}
			remarksBuilder.append(separator).append(inspectionName);
			isFirstName = false;
		}
		return remarksBuilder.toString();
	}
	
	/**
	 * 검사코드(TB_BMS_INSPECT_RESULT.INSPECTION_TYPE)를 "구분:한글명" 비고 항목으로 변환 (검사추가 POD 비고용)
	 * - 구분: 접두어 S(선별), X(XRAY), P(통관), I(검역) → INSPECTION_PREFIX_LABEL_MAP
	 * - 한글명: S/X/P는 SE_INSPECTION_CODE, I는 INSPECTION_TYPE 그룹의 NAME_DEFAULT (selectInspectResult 조회와 같은 기준)
	 * - 한글명이 없으면 코드값을 그대로 쓴다. (예: 선별:08)
	 * @param entCode 도입사코드
	 * @param inspectionType 검사코드 (예: S03, X04, I10)
	 * @return 비고 항목 (예: 선별:검역, XRAY:식품,의약품, 검역:검역(식품검역))
	 */
	private String selectInspectionTypeRemarkItem(String entCode, String inspectionType) {
		String prefix = inspectionType.substring(0, 1);
		String code = inspectionType.substring(1);
		
		Map<String, Object> codeNameParam = new HashMap<String, Object>();
		codeNameParam.put("userLanguage", "DEFAULT"); // CommonMapper.selectCodeName 의 NAME_${userLanguage} → NAME_DEFAULT
		codeNameParam.put("entCode", entCode);
		codeNameParam.put("srchGroupCode", "I".equals(prefix) ? "INSPECTION_TYPE" : "SE_INSPECTION_CODE");
		codeNameParam.put("srchCode", code);
		Map<String, Object> codeNameMap = commonMapper.selectCodeName(codeNameParam);
		String codeName = codeNameMap == null ? "" : Util.null2str(codeNameMap.get("CODE_NAME"));
		if("".equals(codeName)) codeName = code; // 한글명이 없으면 코드값
		
		String prefixLabel = INSPECTION_PREFIX_LABEL_MAP.getOrDefault(prefix, "");
		return "".equals(prefixLabel) ? codeName : prefixLabel + ":" + codeName;
	}

	public BmsInspectResult selectInspectResult(Map<String, Object> search) throws Exception {
		BmsInspectResult inspectResult = new BmsInspectResult();
		List<BmsInspectResult> rstList = pdaInspectPaisMapper.selectInspectResult(search);

		if(rstList.size() > 0) {
			inspectResult.setCt(rstList.get(0).getCt());
			inspectResult.setInspectRemark(rstList.get(0).getInspectRemark());

			for(BmsInspectResult rst : rstList) {
				inspectResult.setSeInspectionCodeName( StringUtils.isNotEmpty(rst.getSeInspectionCodeName()) ? rst.getSeInspectionCodeName() : inspectResult.getSeInspectionCodeName());
				inspectResult.setXrayInspectionCodeName( StringUtils.isNotEmpty(rst.getXrayInspectionCodeName()) ? rst.getXrayInspectionCodeName() : inspectResult.getXrayInspectionCodeName());
				inspectResult.setPaln5emInspectionCodeName( StringUtils.isNotEmpty(rst.getPaln5emInspectionCodeName()) ? rst.getPaln5emInspectionCodeName() : inspectResult.getPaln5emInspectionCodeName());
				inspectResult.setInspectionTypeName( StringUtils.isNotEmpty(rst.getInspectionTypeName()) ? rst.getInspectionTypeName() : inspectResult.getInspectionTypeName());
				
				inspectResult.setBigSizeYnName( StringUtils.isNotEmpty(rst.getBigSizeYnName()) ? rst.getBigSizeYnName() : inspectResult.getBigSizeYnName());
				inspectResult.setIntellectualPropYnName( StringUtils.isNotEmpty(rst.getIntellectualPropYnName()) ? rst.getIntellectualPropYnName() : inspectResult.getIntellectualPropYnName());
				inspectResult.setInspectionCustomsYnName( StringUtils.isNotEmpty(rst.getInspectionCustomsYnName()) ? rst.getInspectionCustomsYnName() : inspectResult.getInspectionCustomsYnName());
				
				inspectResult.setSeInspectionCodeType( StringUtils.isNotEmpty(rst.getSeInspectionCodeType()) ? rst.getSeInspectionCodeType() : inspectResult.getSeInspectionCodeType());
				inspectResult.setXrayInspectionCodeType( StringUtils.isNotEmpty(rst.getXrayInspectionCodeType()) ? rst.getXrayInspectionCodeType() : inspectResult.getXrayInspectionCodeType());
				inspectResult.setPaln5emInspectionCodeType( StringUtils.isNotEmpty(rst.getPaln5emInspectionCodeType()) ? rst.getPaln5emInspectionCodeType() : inspectResult.getPaln5emInspectionCodeType());
				inspectResult.setInspectionTypeType( StringUtils.isNotEmpty(rst.getInspectionTypeType()) ? rst.getInspectionTypeType() : inspectResult.getInspectionTypeType());

				inspectResult.setSeInspectionRemark( StringUtils.isNotEmpty(rst.getSeInspectionRemark()) ? rst.getSeInspectionRemark() : inspectResult.getSeInspectionRemark());
				inspectResult.setXrayInspectionRemark( StringUtils.isNotEmpty(rst.getXrayInspectionRemark()) ? rst.getXrayInspectionRemark() : inspectResult.getXrayInspectionRemark());
				inspectResult.setPaln5emInspectionRemark( StringUtils.isNotEmpty(rst.getPaln5emInspectionRemark()) ? rst.getPaln5emInspectionRemark() : inspectResult.getPaln5emInspectionRemark());
				inspectResult.setInspectionTypeRemark( StringUtils.isNotEmpty(rst.getInspectionTypeRemark()) ? rst.getInspectionTypeRemark() : inspectResult.getInspectionTypeRemark());
			
				inspectResult.setInNo(rst.getInNo() != null ? rst.getInNo() : inspectResult.getInNo());
				inspectResult.setCargoReferenceNo( StringUtils.isNotEmpty(rst.getCargoReferenceNo()) ? rst.getCargoReferenceNo() : inspectResult.getCargoReferenceNo());				
				inspectResult.setSeInspectionFinishYn( StringUtils.isNotEmpty(rst.getSeInspectionFinishYn()) ? rst.getSeInspectionFinishYn() : inspectResult.getSeInspectionFinishYn());				
				inspectResult.setXrayInspectionFinishYn( StringUtils.isNotEmpty(rst.getXrayInspectionFinishYn()) ? rst.getXrayInspectionFinishYn() : inspectResult.getXrayInspectionFinishYn());				
				inspectResult.setPaln5emInspectionFinishYn( StringUtils.isNotEmpty(rst.getPaln5emInspectionFinishYn()) ? rst.getPaln5emInspectionFinishYn() : inspectResult.getPaln5emInspectionFinishYn());				
			}
		}
		return inspectResult;
	}

	public BmsInspectResultRst updateInspectResult(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInPlanPkgResult = new BmsInspectResultRst();
		String reason = "";
		String inspectionStrFinished = "";
		Map<String, Object> bmsIn = pdaInspectPaisMapper.selectBmsInByInPlanNo(search);
		search.put("inNo", bmsIn.get("IN_NO"));
		
		List<String> ticInspectionTypeList = pdaInspectPaisMapper.selectInspectionTypeByTicInspectResultCode();
		List<Map<String, Object>> rstList = pdaInspectPaisMapper.selectInspectResultByInNo(search);
		/* 선별 */
		if( !"".equals(Util.null2str(search.get("seInspectionCodeType"))) ) {
			
			String inspectTypeCode = Util.null2str(search.get("seInspectionCodeType"));
			if( !ticInspectionTypeList.stream().anyMatch(inspectionType -> inspectionType.equals(inspectTypeCode)) ) { // 이고 검사건이 아닐때만
				
				Map<String, Object> inspectResult = rstList.stream().filter( rst -> inspectTypeCode.equals(Util.null2str(rst.get("INSPECTION_TYPE"))) ).findFirst().orElse(null);
				String orginYn = Util.null2str(inspectResult.get("INSPECTION_FINISH_YN"));
				String orginRemark = Util.null2str(inspectResult.get("INSPECTION_FINISH_REMARKS"));
				
				Map<String, Object> updateInspRstParam = new HashMap<String, Object>();
				updateInspRstParam.put("inNo", bmsIn.get("IN_NO"));
				updateInspRstParam.put("updateUserId", search.get("updateUserId"));
				if( "".equals(Util.null2str(search.get("seInspectionRemark"))) && !"".equals(orginRemark) ) { // 1. 입력remark가 공백이고 2. 기존 리마크가 공백이 아닐때
					
					if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
					inspectionStrFinished += "선별완료취소";
					
					updateInspRstParam.put("inspectionFinishYn", "N");
					updateInspRstParam.put("updateUserId", "");
					
					updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
					updateInspRstParam.put("inspectResultRemark", "");
					pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
				}else if( !"".equals(Util.null2str(search.get("seInspectionRemark"))) ) { // 1. 입력remark가 공백이 아닐때
					if( "Y".equals(orginYn)) {
						if( !orginRemark.equals(Util.null2str(search.get("seInspectionRemark"))) ) {
							
							if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
							inspectionStrFinished += "선별";
							
							updateInspRstParam.put("inspectionFinishYn", "Y");
							
							updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
							updateInspRstParam.put("inspectResultRemark", Util.null2str(search.get("seInspectionRemark")) );
							pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
						}
					}else { // 기존 YN이 N 인경우는 무족건업데이트
						
						if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
						inspectionStrFinished += "선별";
						
						updateInspRstParam.put("inspectionFinishYn", "Y");
						
						updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
						updateInspRstParam.put("inspectResultRemark", Util.null2str(search.get("seInspectionRemark")) );
						pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
					}
				}
				
			}
		}

		/* X-RAY */
		if( StringUtils.isNotEmpty((String) search.get("xrayInspectionCodeType")) ) {
			
			String inspectTypeCode = Util.null2str(search.get("xrayInspectionCodeType"));
			if( !ticInspectionTypeList.stream().anyMatch(inspectionType -> inspectionType.equals(inspectTypeCode)) ) { // 이고 검사건이 아닐때만
				
				Map<String, Object> inspectResult = rstList.stream().filter( rst -> inspectTypeCode.equals(Util.null2str(rst.get("INSPECTION_TYPE"))) ).findFirst().orElse(null);
				String orginYn = Util.null2str(inspectResult.get("INSPECTION_FINISH_YN"));
				String orginRemark = Util.null2str(inspectResult.get("INSPECTION_FINISH_REMARKS"));
				
				Map<String, Object> updateInspRstParam = new HashMap<String, Object>();
				updateInspRstParam.put("inNo", bmsIn.get("IN_NO"));
				updateInspRstParam.put("updateUserId", search.get("updateUserId"));
				if( "".equals(Util.null2str(search.get("xrayInspectionRemark"))) && !"".equals(orginRemark) ) { // 1. 입력remark가 공백이고 2. 기존 리마크가 공백이 아닐때
					
					if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
					inspectionStrFinished += "XRAY완료취소";
					
					updateInspRstParam.put("inspectionFinishYn", "N");
					updateInspRstParam.put("updateUserId", "");
					
					updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
					updateInspRstParam.put("inspectResultRemark", "");
					pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
				}else if( !"".equals(Util.null2str(search.get("xrayInspectionRemark"))) ) { // 1. 입력remark가 공백이 아닐때
					if( "Y".equals(orginYn)) {
						if( !orginRemark.equals(Util.null2str(search.get("xrayInspectionRemark"))) ) {
							
							if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
							inspectionStrFinished += "XRAY";
							
							updateInspRstParam.put("inspectionFinishYn", "Y");
							
							updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
							updateInspRstParam.put("inspectResultRemark", Util.null2str(search.get("xrayInspectionRemark")) );
							pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
						}
					}else { // 기존 YN이 N 인경우는 무족건업데이트
						
						if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
						inspectionStrFinished += "XRAY";
						
						updateInspRstParam.put("inspectionFinishYn", "Y");
						
						updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
						updateInspRstParam.put("inspectResultRemark", Util.null2str(search.get("xrayInspectionRemark")) );
						pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
					}
				}
				
			}
		}

		/* 통관검사계획 */
		if( StringUtils.isNotEmpty((String) search.get("paln5emInspectionCodeType")) ) {
			
			String inspectTypeCode = Util.null2str(search.get("paln5emInspectionCodeType"));
			if( !ticInspectionTypeList.stream().anyMatch(inspectionType -> inspectionType.equals(inspectTypeCode)) ) { // 이고 검사건이 아닐때만
				
				Map<String, Object> inspectResult = rstList.stream().filter( rst -> inspectTypeCode.equals(Util.null2str(rst.get("INSPECTION_TYPE"))) ).findFirst().orElse(null);
				String orginYn = Util.null2str(inspectResult.get("INSPECTION_FINISH_YN"));
				String orginRemark = Util.null2str(inspectResult.get("INSPECTION_FINISH_REMARKS"));
				
				Map<String, Object> updateInspRstParam = new HashMap<String, Object>();
				updateInspRstParam.put("inNo", bmsIn.get("IN_NO"));
				updateInspRstParam.put("updateUserId", search.get("updateUserId"));
				if( "".equals(Util.null2str(search.get("paln5emInspectionRemark"))) && !"".equals(orginRemark) ) { // 1. 입력remark가 공백이고 2. 기존 리마크가 공백이 아닐때
					
					if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
					inspectionStrFinished += "통관완료취소";
					
					updateInspRstParam.put("inspectionFinishYn", "N");
					updateInspRstParam.put("updateUserId", "");
					
					updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
					updateInspRstParam.put("inspectResultRemark", "");
					pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
				}else if( !"".equals(Util.null2str(search.get("paln5emInspectionRemark"))) ) { // 1. 입력remark가 공백이 아닐때
					if( "Y".equals(orginYn)) {
						if( !orginRemark.equals(Util.null2str(search.get("paln5emInspectionRemark"))) ) {
							
							if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
							inspectionStrFinished += "통관";
							
							updateInspRstParam.put("inspectionFinishYn", "Y");
							
							updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
							updateInspRstParam.put("inspectResultRemark", Util.null2str(search.get("paln5emInspectionRemark")) );
							pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
						}
					}else { // 기존 YN이 N 인경우는 무족건업데이트
						
						if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
						inspectionStrFinished += "통관";
						
						updateInspRstParam.put("inspectionFinishYn", "Y");
						
						updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
						updateInspRstParam.put("inspectResultRemark", Util.null2str(search.get("paln5emInspectionRemark")) );
						pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
					}
				}
				
			}
		}

		/* 검역검사 */
		if( StringUtils.isNotEmpty((String) search.get("inspectionTypeType")) ) {
			
			String inspectTypeCode = Util.null2str(search.get("inspectionTypeType"));
			if( !ticInspectionTypeList.stream().anyMatch(inspectionType -> inspectionType.equals(inspectTypeCode)) ) { // 이고 검사건이 아닐때만
				
				Map<String, Object> inspectResult = rstList.stream().filter( rst -> inspectTypeCode.equals(Util.null2str(rst.get("INSPECTION_TYPE"))) ).findFirst().orElse(null);
				String orginYn = Util.null2str(inspectResult.get("INSPECTION_FINISH_YN"));
				String orginRemark = Util.null2str(inspectResult.get("INSPECTION_FINISH_REMARKS"));
				
				Map<String, Object> updateInspRstParam = new HashMap<String, Object>();
				updateInspRstParam.put("inNo", bmsIn.get("IN_NO"));
				updateInspRstParam.put("updateUserId", search.get("updateUserId"));
				if( "".equals(Util.null2str(search.get("inspectionTypeRemark"))) && !"".equals(orginRemark) ) { // 1. 입력remark가 공백이고 2. 기존 리마크가 공백이 아닐때
					
					if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
					inspectionStrFinished += "검역완료취소";
					
					updateInspRstParam.put("inspectionFinishYn", "N");
					updateInspRstParam.put("updateUserId", "");
					
					updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
					updateInspRstParam.put("inspectResultRemark", "");
					pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
				}else if( !"".equals(Util.null2str(search.get("inspectionTypeRemark"))) ) { // 1. 입력remark가 공백이 아닐때
					if( "Y".equals(orginYn)) {
						if( !orginRemark.equals(Util.null2str(search.get("inspectionTypeRemark"))) ) {
							
							if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
							inspectionStrFinished += "검역";
							
							updateInspRstParam.put("inspectionFinishYn", "Y");
							
							updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
							updateInspRstParam.put("inspectResultRemark", Util.null2str(search.get("inspectionTypeRemark")) );
							pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
						}
					}else { // 기존 YN이 N 인경우는 무족건업데이트
						
						if(!"".equals(inspectionStrFinished)) inspectionStrFinished += ", ";
						inspectionStrFinished += "검역";
						
						updateInspRstParam.put("inspectionFinishYn", "Y");
						
						updateInspRstParam.put("inspectionTypeCode", inspectTypeCode);
						updateInspRstParam.put("inspectResultRemark", Util.null2str(search.get("inspectionTypeRemark")) );
						pdaInspectPaisMapper.updateInspectResultRemarkByKey(updateInspRstParam);
					}
				}
				
			}
		}

		Map<String, Object> inspectResultParam = new HashMap<String, Object>();
		inspectResultParam.put("inNo", bmsIn.get("IN_NO"));
		List<BmsInspectResult> notFinishedResultList = pdaInspectPaisMapper.selectInspectResultNotFinished(inspectResultParam);
		if(notFinishedResultList != null && notFinishedResultList.size() > 0) { // TB_BMS_INSPECT_RESULT의 IN_NO에 대한 모든 검사가 완료안됐을 경우
			if(!"".equals(inspectionStrFinished)) {
				reason = "[" + inspectionStrFinished + "]이(가) 저장 되었습니다. <br>이고 건이 아직 검사중입니다.";
			}else {
				reason = "저장 되었습니다. <br>이고 건이 아직 검사중입니다.";
			}
			
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("packingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", Util.null2str(search.get("blNo")));
			logParamg.put("result", inspectionStrFinished+" 완료, 미검사있음");
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			bmsInPlanPkgResult.setResultCode("ABMS100068"); // #1#
		}else {
			reason = "완료";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("packingNo", Util.null2str(search.get("packingNo")));
			logParamg.put("inspectionStatus", "E");
			logParamg.put("blNo", Util.null2str(search.get("blNo")));
			logParamg.put("result", inspectionStrFinished + " 완료");
			logParamg.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaInspectPaisMapper.insertInspectScanLog(logParamg);
			
			if( !( "N".equals(Util.null2str(bmsIn.get("BIG_SIZE_YN"))) && "N".equals(Util.null2str(bmsIn.get("INSPECTION_CUSTOMS_YN"))) && "N".equals(Util.null2str(bmsIn.get("INTELLECTUAL_PROP_YN"))) &&
					"N".equals(Util.null2str(bmsIn.get("CRIME_SELECTION_YN"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("SE_INSPECTION_CODE"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("XRAY_INSPECTION_CODE"))) &&
					StringUtils.isEmpty(Util.null2str(bmsIn.get("INSPECTION_TYPE"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("INSPECTION_TYPE_A"))) && StringUtils.isEmpty(Util.null2str(bmsIn.get("INSPECTION_TYPE_B"))) &&
					StringUtils.isEmpty(Util.null2str(bmsIn.get("PALN5EM_INSPECTION_CODE"))) ) ) { //검사 항목이 1개라도 있으면
				search.put("allInspectionFinishYn", "Y");
			}
			
			bmsInPlanPkgResult.setResultCode("A012051");
		}

		/* 사유 저장 */
		search.put("inNo", bmsIn.get("IN_NO"));
		pdaInspectPaisMapper.updateBmsInAllInspectionFinishYn(search);

		/* POD 저장 */
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_INSPECT_RESULT);
		processLogs.setDescription(BmsConsts.DbType.BMS_INSPECT_RESULT+" [PDA 검사사유관리 저장]");
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobNo(Util.null2str(search.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.INSPECT_RESULT /* pod Status 값 매핑 */
				, Util.null2str(search.get("podRemarks"))
				, "검사사유관리"
		);

		bmsInPlanPkgResult.setReason(reason);
		return bmsInPlanPkgResult;
	}
	
	public BmsInspectResultRst updateInspectComplete(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInPlanPkgResult = new BmsInspectResultRst();
		
		// IN_NO가 빈 값일 경우 종료
		if("".equals(search.get("inNo"))) {
			return bmsInPlanPkgResult;
		}
		 
		Map<String, Object> bmsIn = pdaInspectPaisMapper.selectBmsInsInNo(search);	 // 이고반입 데이터 조회		
		Map<String, Object> bmsOut = pdaInspectPaisMapper.selectBmsInsOutNo(search); // 이고반출 데이터 조회		

		List<String> inspectionTypeList = (List<String>) search.get("inspectionType"); 
		String seInspectionFinishYn = (String) search.get("seInspectionFinishYn");
		String xrayInspectionFinishYn = (String) search.get("xrayInspectionFinishYn");
		String paln5emInspectionFinishYn = (String) search.get("paln5emInspectionFinishYn");
		
		Map<String, Object> updateInsParam = new HashMap<String, Object>();
		
		// 이고반입이 있거나 이고반입과 이고반출이 모두 없을 경우 검사 완료로 업데이트 
		if( (!"".equals(Util.null2str(bmsIn))) || ("".equals(Util.null2str(bmsIn)) && "".equals(Util.null2str(bmsOut))) ) {
			
			// 모든 이고검사 건이 완료인 경우 종료
			if(("Y".equals(Util.null2str(seInspectionFinishYn)) || seInspectionFinishYn == null)
					&& ("Y".equals(Util.null2str(xrayInspectionFinishYn)) || xrayInspectionFinishYn == null)
					&& ("Y".equals(Util.null2str(paln5emInspectionFinishYn)) || paln5emInspectionFinishYn == null)) {
				bmsInPlanPkgResult.setReason("모든 이고검사(목록선별, X-RAY검사, 통관검사계획)건이 검사완료되어 이고검사완료 처리할 수 없습니다.");
				bmsInPlanPkgResult.setResultCode("ABMS100068"); // #1#
				return bmsInPlanPkgResult;
			}
			
			updateInsParam.put("inNo", search.get("inNo"));
			updateInsParam.put("blNo", search.get("blNo"));
			updateInsParam.put("updateUserId", search.get("updateUserId"));
			updateInsParam.put("inspectionType", inspectionTypeList);
			pdaInspectPaisMapper.updateInsComplete(updateInsParam);	// 검사 완료 업데이트
			bmsInPlanPkgResult.setResultCode("A012051");
		}else {
			bmsInPlanPkgResult.setReason("이고 반입이 안된 건은 검사완료를 할 수 없습니다. 이고반입확인 메뉴에서 검사완료를 하십시오.");
			bmsInPlanPkgResult.setResultCode("ABMS100068"); // #1#
			return bmsInPlanPkgResult;
		}
		
		List<BmsInspectResult> bmsInsCompleteYnList = pdaInspectPaisMapper.selectBmsInsCompleteYn(search); // 해당 IN_NO의 모든 검사 건 완료여부 조회 		
		boolean allFinished = true;  // 전체 완료 여부
	
		for (BmsInspectResult item : bmsInsCompleteYnList) {
		    if (!"Y".equals(item.getInspectionFinishYn())) {
		        allFinished = false;
		        break; // 하나라도 N이면 바로 중단
		    }
		}
		
		// 전부 검사가 끝난 경우 업데이트
		if (allFinished && !bmsInsCompleteYnList.isEmpty()) {
		    pdaInspectPaisMapper.updateInsCompleteYn(updateInsParam);
		}
	
		return bmsInPlanPkgResult;
	}
}
