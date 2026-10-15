package org.yht.exps.express.service.pda;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.pda.BmsMobBwstMapper;
import org.yht.exps.express.mapper.pda.PdaInspectPaisMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsStock.BmsStockPkgScan;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.exps.express.model.mobile.bms.MobBwst;
import org.yht.exps.express.model.mobile.bms.MobBwstSaveResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwst;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.util.Util;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import io.micrometer.core.instrument.util.StringUtils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsMobBwstService {

	@Autowired
	BmsMobBwstMapper bmsMobBwstMapper;
	
	@Autowired
	PdaInspectPaisMapper pdaInspectPaisMapper;
	
	@Autowired
	BmsCommService bmsCommService;

	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	/**
	 * 재고실사 - 재고PKG SCAN 조회
	 * @return String
	 * @throws Exception
	 */
	public Map<String, Object> selectInitStockScanInfo() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String status = bmsMobBwstMapper.selectInitStockScanInfo();
		String statusNm = "E".equals(status) ? "마감" : "C".equals(status) ? "실사중" : "시작";
		map.put("statusNm", statusNm);
		map.put("statusVal", StringUtils.isNotEmpty(status) ? status : "S");
		return map;
	}


	/**
	 * 재고실사 - 재고PKG SCAN 시작종료 갱신
	 * @param map
	 * @throws Exception 
	 */
	public void insertStockScanInfo(Map<String, Object> map, HttpServletRequest request) throws Exception {
		try {
			
		String podStatus = StringUtil.EMPTY_STRING;
		if( "S".equals(Util.null2str(map.get("statusFlag"))) ) {
			bmsMobBwstMapper.insertStockScanInfo(map);
			podStatus = BmsConsts.PodStatus.BMS_STOCK_SCAN_START;
		} else {
			int checkScanned = bmsMobBwstMapper.checkScannedByScanDate();
			if(checkScanned > 0) {
				Integer stockScanSeq = bmsMobBwstMapper.selectCurrentStockScanSeq();
				map.put("stockScanSeq", stockScanSeq != null ? stockScanSeq : 1);
			}
			
			bmsMobBwstMapper.insertStockPkgScanBySortResult(map);
			bmsMobBwstMapper.updateStockScanInfo(map);
			podStatus = BmsConsts.PodStatus.BMS_STOCK_SCAN_END;
		}

		insertStockScanPodLog(map, request, podStatus);
		
		} catch(Exception e) {
        	log.error("재고실사 insert / update TB_BMS_STOCK_SCAN_START_END / SP_CR_CHKINV : {}", Util.getPrintStacTraceString(e));
		} 
	}

	/**
	 * 재고실사 - 재고실사 PKG 조회
	 * @param search
	 * @param req
	 * @return BmsStockPkgScan
	 * @throws Exception
	 */
	public BmsStockPkgScan selectStockInfo(Map<String, Object> search, HttpServletRequest req) throws Exception {
		BmsStockPkgScan stockPkgScan = new BmsStockPkgScan();
		String scanResult = "완료";

		if( "Y".equals(Util.null2str(search.get("unManifested"))) ) {
			// scanResult = "무적";
			scanResult = "반입예정정보없음";
		} else {
			stockPkgScan = bmsMobBwstMapper.selectStockInfo(search);
		}

		stockPkgScan.setRegisterUserId(Util.null2str(search.get("registerUserId")));
		stockPkgScan.setScanResult(scanResult);
		search.put("odJobNo", Util.null2str(stockPkgScan.getOdJobNo()));

		Integer stockScanSeq = bmsMobBwstMapper.selectCurrentStockScanSeq();
		stockPkgScan.setStockScanSeq(stockScanSeq != null ? stockScanSeq : 1);
		/* Insert Stock Scan Log */
		bmsMobBwstMapper.insertStockPkgScan(stockPkgScan);
		insertStockScanPodLog(search, req, BmsConsts.PodStatus.BMS_STOCK_SCAN);

		return stockPkgScan;
	}

	/**
	 * 재고실사 - 재고PKG SCAN 로그 조회
	 * @param srchScanDate
	 * @return List
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectScanLogInfo(Map<String, Object> search) throws Exception {
		Map<String, Object> scanStartEndInfo = bmsMobBwstMapper.getStockScanStartEnd();
		
		String scanDate = scanStartEndInfo != null ? Util.null2str(scanStartEndInfo.get("SCAN_DATE")).trim() : "";
		String scanStartTime = scanStartEndInfo != null ? Util.null2str(scanStartEndInfo.get("SCAN_START_TIME")).trim() : "";
		search.put("scanDate", scanDate);
		search.put("scanStartTime", scanStartTime);
		search.put("scanStartDateTime", scanDate + " " + scanStartTime);
		
		// 오늘 날짜(yyyy-MM-dd)만 추출할 포맷 생성 및 동일한 TimeZone 설정
        SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
        dateOnly.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // 이 부분도 KST로!

        // 오늘 날짜 문자열 생성 (예: "2025-10-31")
        String todayDate = dateOnly.format(new Date());
		if("".equals(scanDate) || "".equals(scanStartTime)) {
	        // "오늘 날짜 + 00:00:00" 문자열을 Date로 변환
	        String todayFrom0Hour = todayDate + " 00:00:00";
			search.put("scanStartDateTime", todayFrom0Hour);
		}else if(scanDate.equals(todayDate)) {
			String todayFrom0Hour = todayDate + " 00:00:00";
			search.put("scanStartDateTime", todayFrom0Hour);
		}
		List<Map<String, Object>> scanDataList = bmsMobBwstMapper.selectScanLogInfo(search);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // KST(한국 시간) 설정
		for (Map<String, Object> map : scanDataList) {
		    if (map.get("REGISTER_DATE_TIME") instanceof Date) { 
		        map.put("REGISTER_DATE_TIME", sdf.format(map.get("REGISTER_DATE_TIME"))); // String 변환
		    }
		}
		return scanDataList;
	}

	private void insertStockScanPodLog(Map<String, Object> map, HttpServletRequest request, String podStatus, String additionRemark) throws Exception {
		map.put("additionRemark", additionRemark);
		insertStockScanPodLog(map, request, podStatus);
	}
	/**
	 * Pod Log 생성
	 * @param map
	 * @param request
	 * @param podStatus
	 * @throws Exception
	 */
	private void insertStockScanPodLog(Map<String, Object> map, HttpServletRequest request, String podStatus) throws Exception {
		String remark = podStatus.equals(BmsConsts.PodStatus.BMS_STOCK_SCAN) ?  Util.null2str(map.get("additionRemark")) + " (" + map.get("packingNo") + ")" : StringUtil.EMPTY_STRING;
		
		/** 작업내역 로그시작 */
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(map.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(map.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(map.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setDbType(BmsConsts.DbType.BMS_STOCK_SCAN);
		processLogs.setDescription(BmsConsts.DbType.BMS_STOCK_SCAN+" [재고실사(PDA)]");
		processLogs.setJobLog(new Gson().toJson(map));
		processLogs.setJobNo(Util.null2str(map.get("odJobNo")));
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(map.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(map.get("logUserNameEng")));
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, podStatus /* pod Status 값 매핑 */
				, remark
				, null
		);
	}

	/**
	 * 재고실사처리 - 반입 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobBwst selectRealStockCarryInDetail(SearchMobBwst search) throws Exception {
		return bmsMobBwstMapper.selectRealStockCarryInDetail(search);
	}
	
	/**
	 * 재고실사처리 - 재고스캔 insert
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobBwstSaveResult insertRealStockScan(SearchMobBwst search, HttpServletRequest request) throws Exception {
		MobBwstSaveResult result = new MobBwstSaveResult();
		if(bmsMobBwstMapper.insertRealStockScan(search) > 0) {
			result.setResultCode("S");
		}else {
			result.setResultCode("F");
		}
		
		/**작업내역 로그시작*/
		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
//		ProcessLogs processLogs = new ProcessLogs();
//		processLogs.setEntCode(Util.null2str(search.getEntCode()));
//		processLogs.setEntBusinessCode(Util.null2str(search.getEntBusinessCode()));
//		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
//		processLogs.setProcessUserId(Util.null2str(search.getUpdateUserId()));
//		processLogs.setLogJobType("DATA");
//		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
//		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
//		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [입력 BL NO:"+String.valueOf(search.getBlNo())+"]");
//		processLogs.setJobNo(String.valueOf(search.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
//		processLogs.setJobLog(new Gson().toJson(search));
//		processLogs.setResult("ok");
//		processLogs.setProcessUserName(search.getLogUserName());
//		processLogs.setProcessUserNameEng(search.getLogUserNameEng());
//		bmsCommService.insertLogAndPodStatus(processLogs, request
//				, BmsConsts.PodStatus.LOCATION_SAVE /* pod Status 값 매핑 */
//				, ""
//				, search
//		);
		/**작업내역 로그끝*/
		
		return result;
	}
	
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public Map<String, Object> selectPkgWithBmsInByLocation(Map<String, Object> search, HttpServletRequest request) throws Exception {
		Map<String, Object> bmsInPlanPkgResult = new HashMap<String, Object>();
		
		try {
			
			Map<String, Object> locationInfo = bmsMobBwstMapper.selectLocationInfoDetail(search);
			bmsInPlanPkgResult.put("locationInfo", locationInfo);
			
			if(locationInfo == null) {
				return bmsInPlanPkgResult;
			}
			List<BmsStockPkgScan> scanDataList = bmsMobBwstMapper.selectScanLogInfoByLocation(search);
			
			SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory()
				        .openSession(ExecutorType.BATCH, false); // autoCommit=false
			
			try {
				BmsMobBwstMapper batchMapper = sqlSession.getMapper(BmsMobBwstMapper.class);
				int batchSize = 100;
				List<List<BmsStockPkgScan>> chunks = Lists.partition(scanDataList, batchSize);
				
				for (List<BmsStockPkgScan> chunk : chunks) {
					for(BmsStockPkgScan scan : chunk) {
						if(scan.getInPlanNo() == null || scan.getInPlanNo() == 0) {
							batchMapper.insertStockPkgScan(scan);
							continue;
						}
						
						String scanResult = "";
						if("Y".equals(Util.null2str(scan.getDeletedYn()))) {
							scanResult = "삭제된 박스";
						}else if(Util.null2int(scan.getInNo()) == 0) {
							scanResult = "미반입";
						}else if("P".equals(Util.null2str(scan.getLocationStatus()))) {
							scanResult = "로케이션피킹됨";
						}else if("L".equals(scan.getLocationStatus()) && !Util.null2str(search.get("locationPlace")).equals(scan.getLocationPlace())){
							scanResult = scan.getLocationPlace();
						}else{
							scan.setApprovalClassStr(scan.getPkgOutCt()==1 ? "(출고)" : "");
							scanResult = "완료";
							log.info("scanResult : {}", scanResult);
						}
						scan.setScanResult(scanResult);
						
						scan.setRegisterUserId(Util.null2str(search.get("registerUserId")));
		
						//Integer stockScanSeq = bmsMobBwstMapper.selectCurrentStockScanSeq();
						//scan.setStockScanSeq(stockScanSeq != null ? stockScanSeq : 1);
						/* Insert Stock Scan Log */
						batchMapper.insertStockPkgScan(scan);
						
						String odJobNo = scan.getOdJobNo();
							
						Map<String, Object> podParam = new HashMap<String, Object>();
						podParam.put("entCode", Util.null2str(search.get("entCode")));
						podParam.put("odJobNo", odJobNo);
						podParam.put("podName", "일괄 재고실사(PDA)");
						podParam.put("remarks", scanResult +"_("+scan.getPackingNo()+")");
						podParam.put("registerUserId", Util.null2str(search.get("registerUserId")));
						batchMapper.insertBmsEcmPodForBatch(podParam);
					}
					
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			    sqlSession.commit();
			    
			} catch (Exception e) {
			    sqlSession.rollback();
			    throw e;
			} finally {
			    sqlSession.close();
			}
			
			String locationPlace = Util.null2str(search.get("locationPlace"));
			/** 작업내역 로그시작 */
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(search.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
			processLogs.setDbType(BmsConsts.DbType.BMS_STOCK_SCAN);
			processLogs.setDescription(BmsConsts.DbType.BMS_STOCK_SCAN+" [ 일괄 재고실사(PDA)]");
			processLogs.setJobLog(new Gson().toJson(search));
			processLogs.setJobNo(locationPlace);
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
			bmsCommService.insertLog(processLogs, request);
		
			bmsInPlanPkgResult.put("pkgScanList", scanDataList);
		
		} catch(Exception e) {
        	log.error("재고실사 insert PKG selectPkgWithBmsInByLocation : {}", Util.getPrintStacTraceString(e));
		} 
		
		bmsInPlanPkgResult.put("resultCode", "A012051");
		
		return bmsInPlanPkgResult;
	}
	
	/**
	 * 재고실사 - 재고실사 PKG 조회
	 * @param search
	 * @param req
	 * @return BmsStockPkgScan
	 * @throws Exception
	 */
	public BmsStockPkgScan selectStockInfoByLocation(Map<String, Object> search, HttpServletRequest req) throws Exception {
		BmsStockPkgScan stockPkgScan = new BmsStockPkgScan();
		String scanResult = "";
		String podAdditionRemark = "";
		try {
		log.info("로케이션없이 박스스캔 req : {}", search);
		if( "Y".equals(Util.null2str(search.get("unManifested"))) ) {
			// scanResult = "무적";
			stockPkgScan.setPackingNo(Util.null2str(search.get("packingNo")));
			scanResult = "반입예정정보없음";
		} else {
			if("".equals(Util.null2str(search.get("locationPlace"))) ){ // 로케이션 정보 스캔을 안한 경우만 체크
				stockPkgScan = bmsMobBwstMapper.selectStockInfoByLocation(search);
				
				Integer hasBmsInCount = bmsMobBwstMapper.checkPkgHasBmsIn(search);
				if(hasBmsInCount > 0) {
					scanResult = "완료";
				}else {
					scanResult = "미반입";
				}
				
				if("Y".equals(Util.null2str(stockPkgScan.getDeletedYn()))) {
					scanResult = "삭제된 박스";
				}
			}else{
				stockPkgScan = bmsMobBwstMapper.selectStockInfoByLocation(search);
				if("Y".equals(Util.null2str(stockPkgScan.getDeletedYn()))) {
					scanResult = "삭제된 박스";
				}else {
					if("L".equals(stockPkgScan.getLocationStatus()) && !Util.null2str(search.get("locationPlace")).equals(stockPkgScan.getLocationPlace())){
						podAdditionRemark = "로케이션다름 ※" + Util.null2str(search.get("locationPlace")) + " ->"; //로케이션다름 ※1029->1030(582890563211)
						scanResult = stockPkgScan.getLocationPlace();
					}else if("".equals(stockPkgScan.getLocationPlace()) || "L".equals(stockPkgScan.getLocationStatus()) && Util.null2str(search.get("locationPlace")).equals(stockPkgScan.getLocationPlace())) {
						Integer hasBmsInCount = bmsMobBwstMapper.checkPkgHasBmsIn(search);
						if(hasBmsInCount > 0) {
							scanResult = "완료";
						}else {
							scanResult = "미반입";
						}
					}else if("P".equals(stockPkgScan.getLocationStatus())){
						scanResult = "로케이션피킹됨";
					}
				}
			}
		}

		podAdditionRemark = podAdditionRemark + scanResult;
		
		stockPkgScan.setRegisterUserId(Util.null2str(search.get("registerUserId")));
		stockPkgScan.setScanResult(scanResult);
		search.put("odJobNo", Util.null2str(stockPkgScan.getOdJobNo()));

		Integer stockScanSeq = bmsMobBwstMapper.selectCurrentStockScanSeq();
		stockPkgScan.setStockScanSeq(stockScanSeq != null ? stockScanSeq : 1);
		/* Insert Stock Scan Log */
		bmsMobBwstMapper.insertStockPkgScan(stockPkgScan);
		insertStockScanPodLog(search, req, BmsConsts.PodStatus.BMS_STOCK_SCAN, podAdditionRemark);
		
		BmsOutPlan inAndOutPlan = bmsMobBwstMapper.selectOutPlanInfoByPkgNo(search);
		//if( inAndOutPlan != null && !"".equals(Util.null2str(inAndOutPlan.getApprovalClass())) ) {
		if( inAndOutPlan != null && Util.null2int(inAndOutPlan.getInNo()) > 0 ) {
			String approvalClass = Util.null2str(inAndOutPlan.getApprovalClass());
			stockPkgScan.setApprovalClassStr("31".equals(approvalClass) && !"80".equals(Util.null2str(inAndOutPlan.getOutClass())) ? "반출승인" : "");
			
			if("".equals(stockPkgScan.getApprovalClassStr()) && inAndOutPlan.getInNo() > 0) {
				Map<String, Object> inspectResultParam = new HashMap<String, Object>();
				inspectResultParam.put("inNo", inAndOutPlan.getInNo());
				List<BmsInspectResult> notFinishedResultList = pdaInspectPaisMapper.selectInspectResultNotFinished(inspectResultParam);
				if(notFinishedResultList != null && notFinishedResultList.size() > 0) { // TB_BMS_INSPECT_RESULT의 IN_NO에 대한 모든 검사가 완료안됐을 경우
					stockPkgScan.setApprovalClassStr("검사중");
				}
			}
		}
		
		} catch(Exception e) {
        	log.error("재고실사 insert PKG selectStockInfoByLocation : {}", Util.getPrintStacTraceString(e));
		} 
		
		return stockPkgScan;
	}
	
	public Map<String, Object> selectPkgWithLogByLocation(Map<String, Object> search, HttpServletRequest request) throws Exception {
		Map<String, Object> bmsInPlanPkgResult = new HashMap<String, Object>();
		
		Map<String, Object> locationInfo = bmsMobBwstMapper.selectLocationInfoDetail(search);
		bmsInPlanPkgResult.put("locationInfo", locationInfo);
		
		if(locationInfo == null) {
			return bmsInPlanPkgResult;
		}
		List<BmsStockPkgScan> scanLogByLocationList = bmsMobBwstMapper.selectPkgWithLogByLocation(search);
		List<BmsStockPkgScan> scanDataList = scanLogByLocationList.stream()
				.filter(scanData -> !"Y".equals(scanData.getDeletedYn()))
				.map(scanData -> {
					scanData.setApprovalClassStr(scanData.getPkgOutCt()==1 ? "(출고)" : "");
					return scanData;
				})
				.collect(Collectors.toList());
		
		bmsInPlanPkgResult.put("pkgScanList", scanDataList);
		
		bmsInPlanPkgResult.put("resultCode", "A012051");
		
		return bmsInPlanPkgResult;
	}
	
	public Map<String, Object> getStockScanStartEnd() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> map = bmsMobBwstMapper.getStockScanStartEnd();
		
		String scanDate = map != null ? Util.null2str(map.get("SCAN_DATE")) : "";
		String scanStartTime = map != null ? Util.null2str(map.get("SCAN_START_TIME")) : "";
		result.put("scanDate", scanDate);
		result.put("scanStartTime", scanStartTime);
		result.put("scanStartDateTime", scanDate + " " + scanStartTime);
		return result;
	}
}
