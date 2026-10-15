package org.yht.exps.express.service.pda;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.pda.PdaInspectPaisMapper;
import org.yht.exps.express.mapper.pda.PdaLocationPaloMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsPkgNo.search.SearchBmsPkgNoList;
import org.yht.exps.express.model.bms.carryin.BmsInPlanPkgResult;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.model.mobile.bms.MobPalo;
import org.yht.exps.express.model.mobile.bms.MobPaloList;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwlo;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.util.Util;

import com.google.gson.Gson;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PdaLocationPaloNewService {
	
	@Autowired
	PdaLocationPaloMapper pdaLocationPaloMapper;
	@Autowired
	PdaInspectPaisMapper pdaInspectPaisMapper;
	
	@Autowired
	BmsCommService bmsCommService;
	
	
	/**
	 * 로케이션적치 - 현재 로케이션 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobPaloList getPkgListByCurrentLocation13F(SearchMobBwlo search, HttpServletRequest request) throws Exception {
		MobPaloList mobPaloList = new MobPaloList();
		
		Map<String, Object> currentLoactionParam = new HashMap<String, Object>();
		currentLoactionParam.put("locationPlace", search.getLocationPlace());
		Map<String, Object> locationInfo = pdaLocationPaloMapper.selectLocationInfoDetail(currentLoactionParam);
		if(locationInfo == null) {
			mobPaloList.setResultCode("A11481");
			return mobPaloList;
		}
		
		currentLoactionParam.put("printLocationScanYn", Util.null2str(search.getPrintLocationScanYn())); // 로케이션 스캔정보 DB 조회를 포함할지 여부
		List<MobPalo> list = pdaLocationPaloMapper.getPkgPaloListByCurrentLocation(currentLoactionParam);
			
		mobPaloList.setMobPaloList(list);
		mobPaloList.setResultCode("A012051");
		
		return mobPaloList;
	}
	
	/**
	 * 로케이션적치 - 현재 로케이션에서 변경할 로케이션으로 pkg 일괄 update
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public MobPaloList batchLocationFromCurrentToTarget13F(Map<String, Object> search, HttpServletRequest request) throws Exception {
		MobPaloList mobPaloList = new MobPaloList();
		
		if("".equals(Util.null2str(search.get("locationPlace")))) {
			mobPaloList.setResultCode("A00313");
			return mobPaloList;
		}
		
		if("".equals(Util.null2str(search.get("srchCurrentLocation")))) {
			mobPaloList.setResultCode("A00313");
			return mobPaloList;
		}
		
		Map<String, Object> targetLocationParam = new HashMap<String, Object>();
		targetLocationParam.put("locationPlace", search.get("locationPlace"));
		Map<String, Object> targetLocationInfo = pdaLocationPaloMapper.selectLocationInfoDetail(targetLocationParam);
		if(targetLocationInfo == null) {
			mobPaloList.setResultCode("A11481");
			return mobPaloList;
		}
		
		Map<String, Object> currentLocationParam = new HashMap<String, Object>();
		currentLocationParam.put("locationPlace", search.get("srchCurrentLocation"));
		Map<String, Object> currentLocationInfo = pdaLocationPaloMapper.selectLocationInfoDetail(currentLocationParam);
		if(currentLocationInfo == null) {
			mobPaloList.setResultCode("A11481");
			return mobPaloList;
		}
		List<MobPalo> updatePkglist = pdaLocationPaloMapper.getPkgPaloListByCurrentLocation(currentLocationParam);// 변경할 로케이션의 현재 박스 리스트
		mobPaloList.setTotal(Util.null2long(updatePkglist.size()));
		
		List<MobPalo> updatePkgListExceptOutScan = updatePkglist.stream()
			.filter(palo -> palo.getOutCt() == 0)
			.collect(Collectors.toList());
		if(updatePkgListExceptOutScan.size() == 0) {
			mobPaloList.setFailCnt(mobPaloList.getTotal());
			mobPaloList.setResultCode("A11481");
			return mobPaloList;
		}
		
		search.put("seqList", updatePkgListExceptOutScan);
		int succCnt = pdaLocationPaloMapper.updatePkgLocationInSeqList(search);
		
		mobPaloList.setSuccCnt(succCnt);
		mobPaloList.setFailCnt(updatePkglist.size() - succCnt);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("locationPlace", Util.null2str(search.get("locationPlace")));
		paramMap.put("locationStatus", "L");
    	paramMap.put("registerUserId", Util.null2str(search.get("updateUserId")));
    	paramMap.put("updateUserId", Util.null2str(search.get("updateUserId")));
		for(MobPalo loc : updatePkglist) {
			if(loc.getOutCt() == 1) {
				loc.setScanResult("이미출고됨");
				continue;
			}
			paramMap.put("packingNo", loc.getPackingNo());
			paramMap.put("blNo", loc.getBlNo());
			paramMap.put("storageDays", loc.getStorageDays());
			paramMap.put("inspectionYn", loc.getInspection());
			paramMap.put("holdHistory", loc.getHoldHistory());
			paramMap.put("scanCt", loc.getCt());
			paramMap.put("approvalDate", loc.getApprovalDate());
			paramMap.put("inspectionYn", loc.getInspection());
    		if(loc.getInNo() != 0) {
    		} else { // 반입예정
    			//loc.setScanResult("미반입");
    		}
			paramMap.put("result", "완료");
    		pdaLocationPaloMapper.insertLocationScanLog(paramMap);
    		
    		paramMap.put("cargoReferenceNo", loc.getCargoReferenceNo());
        	pdaLocationPaloMapper.updateBmsInLocationInfo(paramMap);
    		
        	/** pod 저장 */
        	CarryIn carryIn = new CarryIn();
    		carryIn.setPdaYn("Y");
    		
    		ProcessLogs processLogs = new ProcessLogs();
    		processLogs.setEntCode(Util.null2str(search.get("srchEntCode")));
    		processLogs.setEntBusinessCode(Util.null2str(search.get("srchEntBusinessCode")));
    		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
    		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
    		processLogs.setLogJobType("DATA");
    		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
    		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
    		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [PDA 현재로케이션 -> 변경로케이션 일괄적치]");
    		processLogs.setJobLog(new Gson().toJson(search));
    		processLogs.setJobNo(loc.getBlNo());
    		processLogs.setResult("ok");
    		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
    		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
    		bmsCommService.insertLogAndPodStatus(processLogs, request
    				, BmsConsts.PodStatus.LOCATION_SAVE /* pod Status 값 매핑 */
    				, Util.null2str("박스: " + loc.getPackingNo() + " , "+currentLocationInfo.get("NAME")) +"(" + Util.null2str(currentLocationInfo.get("CODE")) +")에서 " + Util.null2str(targetLocationInfo.get("NAME")) +"(" + Util.null2str(targetLocationInfo.get("CODE")) +")" +"으로 일괄적치"
    				, carryIn
    		);
    		
    		loc.setScanResult("완료");
		}
		
		mobPaloList.setMobPaloList(updatePkglist);
		mobPaloList.setResultCode("A012051");
		return mobPaloList;
	}
	
	/**
	 * 로케이션적치 - 박스번호조회 NEW
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public MobPaloList selectPkgNoInfoNew13F(SearchMobBwlo search, HttpServletRequest request) throws Exception {
		MobPaloList mobPaloList = new MobPaloList();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		paramMap.put("locationPlace", Util.null2str(search.getLocationPlaceCode()));
		paramMap.put("locationStatus", "L");
		paramMap.put("blNo", "");
		paramMap.put("storageDays", 0);
		paramMap.put("inspectionYn", "N");
		paramMap.put("holdHistory", "");
		paramMap.put("scanCt", 0);
		paramMap.put("approvalDate", "");
    	paramMap.put("packingNo", Util.null2str(search.getSrchNo()));
    	paramMap.put("registerUserId", Util.null2str(search.getUpdateUserId()));
    	paramMap.put("updateUserId", Util.null2str(search.getUpdateUserId()));
		
		if( "Y".equals(Util.null2str(search.getUnManifested())) ) {
			mobPaloList.setMobPaloList(new ArrayList<MobPalo>());
			mobPaloList.setScanResult("무적");
			// 스캔로그 저장
			paramMap.put("result", mobPaloList.getScanResult());
			pdaLocationPaloMapper.insertLocationScanLog(paramMap);
			return mobPaloList;
		}
		
		MobPalo loc = pdaLocationPaloMapper.selectPkgNoInfoNew13F(search);
		if(loc != null && "Y".equals(loc.getDeleteYn())) {
			mobPaloList.setScanResult("삭제된박스");
		}else if(loc != null) {
			List<MobPalo> list = new ArrayList<>();
			if("반입".equals(loc.getDGbn())) {
				list.add(loc);
				list.add(loc);
			}else {
				list.add(loc);
			}
			mobPaloList.setMobPaloList(list);
			paramMap.put("blNo", loc.getBlNo());
			paramMap.put("storageDays", loc.getStorageDays());
			paramMap.put("holdHistory", loc.getHoldHistory());
			paramMap.put("scanCt", loc.getCt());
			paramMap.put("approvalDate", loc.getApprovalDate());
			if("Y".equals(search.getLocationChangeYn())) { // 기존 로케이션에서 다른걸로 변경수행 여부 (Y:변경, N:변경안함)
				mobPaloList.setScanResult("완료");
				
	    		paramMap.put("inNo", loc.getInNo());
				paramMap.put("inPlanNo", loc.getInPlanNo());
				paramMap.put("odJobNo", loc.getOdJobNo());
				paramMap.put("inPlanNo", loc.getInPlanNo());
	    		
	    		boolean notExistInspection = true;
				for(MobPalo cargo : list) {
					if("N".equals(cargo.getBigSizeYn()) && "N".equals(cargo.getInspectionCustomsYn()) && "N".equals(cargo.getIntellectualPropYn()) &&
					"N".equals(cargo.getCrimeSelectionYn()) && StringUtils.isEmpty(cargo.getSeInspectionCode()) && StringUtils.isEmpty(cargo.getXrayInspectionCode()) &&
					StringUtils.isEmpty(cargo.getInspectionType()) && StringUtils.isEmpty(cargo.getInspectionTypeA()) && StringUtils.isEmpty(cargo.getInspectionTypeB()) &&
					StringUtils.isEmpty(cargo.getPaln5emInspectionCode())) {
						if(notExistInspection) { // 화물리스트에 한번도 검사항목이 존재한적이 없을때
							notExistInspection = true;
						}
					}else {
						notExistInspection = false;
					}
					
					cargo.setScanResult(mobPaloList.getScanResult());
				}
	    		
	    		
	    		if(notExistInspection) {
	    			loc.setInspection("N");
	    		}else {
	    			boolean isAllFinished = true;
	    			//검사 항목이 1개라도 있고
	    			if(!("N".equals(loc.getBigSizeYn()) && "N".equals(loc.getInspectionCustomsYn()) && "N".equals(loc.getIntellectualPropYn()) &&
	    					"N".equals(loc.getCrimeSelectionYn()) && StringUtils.isEmpty(loc.getSeInspectionCode()) && StringUtils.isEmpty(loc.getXrayInspectionCode()) &&
	    					StringUtils.isEmpty(loc.getInspectionType()) && StringUtils.isEmpty(loc.getInspectionTypeA()) && StringUtils.isEmpty(loc.getInspectionTypeB()) &&
	    					StringUtils.isEmpty(loc.getPaln5emInspectionCode()) )
					) {
						if("N".equals(Util.null2str(loc.getAllInspectionFinishYn()))) { // TB_BMS_IN의 ALL_INSPECTION_FINISH_YN(전체검사완료)가 N일때
							isAllFinished = false;
						}
					}
	    			
	    			if(isAllFinished) {
	    				loc.setInspection("N");
	    				//mobPaloList.setScanResult("완료");
	    			}else {
	    				loc.setInspection("Y");
	    				//mobPaloList.setScanResult("검사중");
	    			}
	    		}
	    		if("".equals(loc.getOrigLocationPlace())) {
		    		loc.setOrigLocationPlace(Util.null2str(paramMap.get("locationPlace")));
		    	}
		    	if("Y".equals(Util.null2str(search.getApplyAllLoactionYn()))) {
		    		loc.setOrigLocationPlace(Util.null2str(paramMap.get("locationPlace")));
		    	}
		    	if(loc.getOutCt() == 1) {
		    		loc.setScanResult("이미출고됨");
		    		if("반입".equals(loc.getDGbn())) {
						list.add(loc);
						list.add(loc);
					}else {
						list.add(loc);
					}
					mobPaloList.setMobPaloList(list);
					mobPaloList.setScanResult("이미출고됨");
		    		return mobPaloList;
		    	}
	    		paramMap.put("inspectionYn", loc.getInspection());
	    		
	    		paramMap.put("applyAllLoactionYn", search.getApplyAllLoactionYn());
	    		if(!"".equals(Util.null2str(search.getSrchOrigLocation()))) {
		    		Map<String, Object> checkParam = new HashMap<String, Object>();
		    		checkParam.put("locationPlace", search.getSrchOrigLocation());
		    		Map<String, Object> checkInfo = pdaLocationPaloMapper.selectLocationInfoDetail(checkParam);
		    		if(checkInfo == null) {
		    			mobPaloList.setResultCode("A11481");
		    			return mobPaloList;
		    		}
	    			paramMap.put("srchOrigLocation", search.getSrchOrigLocation());
	    		}
	    		
		    	paramMap.put("locationPlace", Util.null2str(search.getLocationPlaceCode()));
		    	paramMap.put("packingNo", Util.null2str(search.getSrchNo()));
		    	paramMap.put("updateUserId", Util.null2str(search.getUpdateUserId()));
		    	pdaLocationPaloMapper.updatePkgLocationAndOrigLocation(paramMap);
		    	
		    	paramMap.put("cargoReferenceNo", loc.getCargoReferenceNo());
		    	pdaLocationPaloMapper.updateBmsInLocationInfo(paramMap);
		    	
		    	//if(!"".equals(Util.null2str(search.getSrchOrigLocation()))) {
		    	//	Map<String, Object> origLocParam = new HashMap<String, Object>();
		    	//	origLocParam.put("applyAllLoactionYn", search.getApplyAllLoactionYn());
		    	//	origLocParam.put("srchInPlanNo", search.getSrchInPlanNo());
		    	//	origLocParam.put("srchOrigLocation", search.getSrchOrigLocation());
		    	//	origLocParam.put("updateUserId", Util.null2str(search.getUpdateUserId()));
		    	//	int cnt = pdaLocationPaloMapper.updateOrigLocation(origLocParam);
		    	//	if(cnt > 0) {
		    	//		loc.setOrigLocationPlace(Util.null2str(search.getSrchOrigLocation()));
		    	//	}
		    	//}
		    	
		    	/** pod 저장 */
		    	CarryIn carryIn = new CarryIn();
				carryIn.setPdaYn("Y");
				
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(search.getEntCode()));
				processLogs.setEntBusinessCode(Util.null2str(search.getEntBusinessCode()));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(search.getUpdateUserId()));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [PDA 로케이션 적치]");
				processLogs.setJobLog(new Gson().toJson(search));
				processLogs.setJobNo(Util.null2str(paramMap.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setResult("ok");
				processLogs.setProcessUserName(Util.null2str(search.getLogUserName()));
				processLogs.setProcessUserNameEng(Util.null2str(search.getLogUserNameEng()));
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.LOCATION_SAVE /* pod Status 값 매핑 */
						, Util.null2str(search.getLocationPlaceName() + "("+ search.getLocationPlaceCode() +")" + "(" + paramMap.get("packingNo") + ")") // 비고= "D-01-00"(로케이션코드) (# 박스번호)
						, carryIn
				);
				
				mobPaloList.setMobPaloList(list);
			} else {
				mobPaloList.setScanResult("미완료");
			}
			
		}
		
		// 스캔로그 저장
		paramMap.put("result", mobPaloList.getScanResult());
		pdaLocationPaloMapper.insertLocationScanLog(paramMap);
		
		//List<MobPalo> list = new ArrayList<>();
		//MobPalo mobPalo = pdaLocationPaloMapper.getPkgPaloByPkgNo(paramMap);
		//if("반입".equals(mobPalo.getDGbn())) {
		//	list.add(mobPalo);
		//	list.add(mobPalo);
		//}else {
		//	list.add(mobPalo);
		//}
		//
		//mobPaloList.setMobPaloList(list);
		return mobPaloList;
	}
	
	/**
	 * 로케이션적치 - 박스번호조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobPaloList batchSaveLocation13F(SearchMobBwlo search, HttpServletRequest request) throws Exception {
		MobPaloList mobPaloList = new MobPaloList();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		paramMap.put("locationPlace", Util.null2str(search.getLocationPlaceCode()));
		paramMap.put("locationStatus", "L");
		paramMap.put("blNo", "");
		paramMap.put("storageDays", 0);
		paramMap.put("inspectionYn", "N");
		paramMap.put("holdHistory", "");
		paramMap.put("scanCt", 0);
		paramMap.put("approvalDate", "");
    	paramMap.put("packingNo", Util.null2str(search.getSrchNo()));
    	paramMap.put("registerUserId", Util.null2str(search.getUpdateUserId()));
    	paramMap.put("updateUserId", Util.null2str(search.getUpdateUserId()));
		
		if( "Y".equals(Util.null2str(search.getUnManifested())) ) {
			mobPaloList.setMobPaloList(new ArrayList<MobPalo>());
			mobPaloList.setTotal(0);
			mobPaloList.setScanResult("무적");
			// 스캔로그 저장
			paramMap.put("result", mobPaloList.getScanResult());
			pdaLocationPaloMapper.insertLocationScanLog(paramMap);
			return mobPaloList;
		}
		
		//search.setSrchNotLocationStatus("L");
		List<MobPalo> list = pdaLocationPaloMapper.selectPkgNoByPlanNoAndBlNo13F(search);
		mobPaloList.setMobPaloList(list);
		
    	if("Y".equals(search.getLocationChangeYn())) { // 기존 로케이션에서 다른걸로 변경수행 여부 (Y:변경, N:변경안함)
    		mobPaloList.setScanResult("완료");
    		if(list != null && list.size()>0) {
    			Map<String, Object> inspectResultParam = new HashMap<String, Object>();
    			inspectResultParam.put("inPlanNo", search.getSrchInPlanNo());
    			List<Map<String, Object>> rstList = pdaInspectPaisMapper.selectNotFinishedInspectResultByInPlanNo(inspectResultParam);
    			
    			int existedOutCtCnt = 0;
	    		for(MobPalo loc : list) {
	    			loc.setScanResult("완료");
					paramMap.put("blNo", loc.getBlNo());
					paramMap.put("storageDays", loc.getStorageDays());
					paramMap.put("inspectionYn", loc.getInspection());
					paramMap.put("holdHistory", loc.getHoldHistory());
					paramMap.put("scanCt", loc.getCt());
					paramMap.put("approvalDate", loc.getApprovalDate());
	        		if(loc.getInNo() != 0) {
	        			paramMap.put("inNo", loc.getInNo());
						if(rstList != null && rstList.size()>0) { // 검사중 인것만 파악 이코드로는 검사완료는 알수없음
							paramMap.put("inspectionYn","Y"); // 'Y'는 검사중 표시 검사중 이외에는 전부 'N'
							loc.setInspection("Y");
						}
						
	        		} else { // 반입예정
	        			loc.setScanResult("미반입");
	        			mobPaloList.setScanResult("미반입");
	        		}
	        		
	        		if(loc.getOutCt() == 1) {
	        			existedOutCtCnt++;
	    				loc.setScanResult("이미출고됨");
	    				continue;
	    			}
	        		paramMap.put("result", mobPaloList.getScanResult());
	        		pdaLocationPaloMapper.insertLocationScanLog(paramMap);
	        		
	        		/** pod 저장 */
		        	CarryIn carryIn = new CarryIn();
		    		carryIn.setPdaYn("Y");
		    		
		    		ProcessLogs processLogs = new ProcessLogs();
		    		processLogs.setEntCode(Util.null2str(search.getEntCode()));
		    		processLogs.setEntBusinessCode(Util.null2str(search.getEntBusinessCode()));
		    		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		    		processLogs.setProcessUserId(Util.null2str(search.getUpdateUserId()));
		    		processLogs.setLogJobType("DATA");
		    		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		    		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		    		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [PDA 로케이션 적치]");
		    		processLogs.setJobLog(new Gson().toJson(search));
		    		processLogs.setJobNo(list.get(0).getOdJobNo());  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		    		processLogs.setResult("ok");
		    		processLogs.setProcessUserName(Util.null2str(search.getLogUserName()));
		    		processLogs.setProcessUserNameEng(Util.null2str(search.getLogUserNameEng()));
		    		bmsCommService.insertLogAndPodStatus(processLogs, request
		    				, BmsConsts.PodStatus.LOCATION_SAVE /* pod Status 값 매핑 */
		    				, Util.null2str(search.getLocationPlaceName() + "("+ search.getLocationPlaceCode() +")" + "(" + paramMap.get("packingNo") + ") 일괄적치") // 비고= "D-01-00"(로케이션코드) (# 박스번호)
		    				, carryIn
		    		);
	        	}
	    		
	    		if(existedOutCtCnt < list.get(0).getOutCt()) {
		    		paramMap.put("srchBlNo	", Util.null2str(search.getSrchBlNo()));
		    		paramMap.put("inPlanNo", search.getSrchInPlanNo());
		        	paramMap.put("locationPlace", Util.null2str(search.getLocationPlaceCode()));
		        	paramMap.put("updateUserId", Util.null2str(search.getUpdateUserId()));
		        	pdaLocationPaloMapper.updateLocationFromInPlanPkg(paramMap);
	    		}else {
	    			mobPaloList.setScanResult("이미출고됨");
	    		}
	    		
	    		mobPaloList.setMobPaloList(list);
    		}else {
    			mobPaloList.setScanResult("이미적치");
        		// 스캔로그 저장
    			paramMap.put("blNo", search.getSrchBlNo());
        		paramMap.put("result", mobPaloList.getScanResult());
        		pdaLocationPaloMapper.insertLocationScanLog(paramMap);
    		}
    	} else {
    		mobPaloList.setScanResult("미완료");
    		// 스캔로그 저장
    		paramMap.put("result", mobPaloList.getScanResult());
    		pdaLocationPaloMapper.insertLocationScanLog(paramMap);
    	}
		
		return mobPaloList;
	}
	
	
	//------------ 14A----------------------//
	
	/**
	 * 로케이션적치 - 팝업 단일박스 최초로케이션 변경
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobPaloList selectPkgNoInfoNew14A(SearchMobBwlo search, HttpServletRequest request) throws Exception {
		MobPaloList mobPaloList = new MobPaloList();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		mobPaloList.setResultCode("A11481");
		
		if( "Y".equals(Util.null2str(search.getUnManifested())) ) {
			mobPaloList.setMobPaloList(new ArrayList<MobPalo>());
			mobPaloList.setResultCode("A11481");
			return mobPaloList;
		}
		
		MobPalo loc = pdaLocationPaloMapper.selectPkgNoInfoNew13F(search);
		if(loc != null && "Y".equals(loc.getDeleteYn())) {
			mobPaloList.setScanResult("삭제된박스");
		}else if(loc != null) {
			mobPaloList.setResultCode("A012051");
			
    		paramMap.put("inNo", loc.getInNo());
			paramMap.put("inPlanNo", loc.getInPlanNo());
			paramMap.put("odJobNo", loc.getOdJobNo());
			
	    	if(loc.getOutCt() == 1) {
				mobPaloList.setScanResult("이미출고됨");
	    		return mobPaloList;
	    	}
    		
	    	paramMap.put("locationPlace", Util.null2str(search.getLocationPlaceCode()));
	    	paramMap.put("packingNo", Util.null2str(search.getSrchNo()));
	    	paramMap.put("updateUserId", Util.null2str(search.getUpdateUserId()));
	    	pdaLocationPaloMapper.updateOnlyOrigLocation(paramMap);
	    	
	    	/** pod 저장 */
	    	CarryIn carryIn = new CarryIn();
			carryIn.setPdaYn("Y");
			
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(search.getEntCode()));
			processLogs.setEntBusinessCode(Util.null2str(search.getEntBusinessCode()));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(search.getUpdateUserId()));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_IN);
			processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [PDA 로케이션 적치]");
			processLogs.setJobLog(new Gson().toJson(search));
			processLogs.setJobNo(Util.null2str(paramMap.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(search.getLogUserName()));
			processLogs.setProcessUserNameEng(Util.null2str(search.getLogUserNameEng()));
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, BmsConsts.PodStatus.LOCATION_SAVE /* pod Status 값 매핑 */
					, "최초로케이션 변경: "+ Util.null2str(search.getLocationPlaceName() + "("+ search.getLocationPlaceCode() +")" + "(" + paramMap.get("packingNo") + ")")
					, carryIn
			);
		}
		
		return mobPaloList;
	}
	
	/**
	 * 로케이션적치 - 팝업 단일박스 최초로케이션 변경
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public MobPaloList updatePkgSameOrigLoaction(Map<String, Object> search, HttpServletRequest request) throws Exception {
		MobPaloList mobPaloList = new MobPaloList();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		mobPaloList.setResultCode("A11481");
		
		if( "Y".equals(Util.null2str(search.get("unManifested"))) ) {
			mobPaloList.setMobPaloList(new ArrayList<MobPalo>());
			mobPaloList.setResultCode("A11481");
			return mobPaloList;
		}
		
		SearchMobBwlo scanParam = new SearchMobBwlo();
		scanParam.setSrchNo(Util.null2str(search.get("srchNo")));
		scanParam.setSrchInPlanNo(Util.null2str(search.get("srchInPlanNo")));
		scanParam.setSrchBlNo(Util.null2str(search.get("srchBlNo")));
		MobPalo scanPkgInfo = pdaLocationPaloMapper.selectPkgNoInfoNew13F(scanParam);
		
		Map<String, Object> origLocationParam = new HashMap<String, Object>();
		origLocationParam.put("locationPlace", scanPkgInfo.getOrigLocationPlace());
		List<MobPalo> updatePkglist = pdaLocationPaloMapper.getPkgPaloListByOrigLocation(origLocationParam);// 변경할 로케이션의 현재 박스 리스트
		
		mobPaloList.setTotal(Util.null2long(updatePkglist.size()));
		
		List<MobPalo> updatePkgListExceptOutScan = updatePkglist.stream()
			.filter(palo -> palo.getOutCt() == 0)
			.collect(Collectors.toList());
		if(updatePkgListExceptOutScan.size() == 0) {
			mobPaloList.setResultCode("A11481");
			return mobPaloList;
		}
		
		search.put("seqList", updatePkgListExceptOutScan);
		search.put("origLocationPlace", search.get("targetOrigLocationPlace"));
		int succCnt = pdaLocationPaloMapper.updatePkgOrigLocationInSeqList(search);
		
		mobPaloList.setSuccCnt(succCnt);
		mobPaloList.setFailCnt(updatePkglist.size() - succCnt);
		
		for(MobPalo loc : updatePkgListExceptOutScan) {
    		
        	/** pod 저장 */
        	CarryIn carryIn = new CarryIn();
    		carryIn.setPdaYn("Y");
    		
    		ProcessLogs processLogs = new ProcessLogs();
    		processLogs.setEntCode(Util.null2str(search.get("srchEntCode")));
    		processLogs.setEntBusinessCode(Util.null2str(search.get("srchEntBusinessCode")));
    		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
    		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
    		processLogs.setLogJobType("DATA");
    		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
    		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
    		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [PDA 현재로케이션 -> 변경로케이션 일괄적치]");
    		processLogs.setJobLog(new Gson().toJson(search));
    		processLogs.setJobNo(loc.getBlNo());
    		processLogs.setResult("ok");
    		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
    		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
    		bmsCommService.insertLogAndPodStatus(processLogs, request
    				, BmsConsts.PodStatus.LOCATION_SAVE /* pod Status 값 매핑 */
    				, "최초로케이션을 " + Util.null2str(search.get("origLocationPlace"))+"으로 변경"
    				, carryIn
    		);
    		
    		loc.setScanResult("완료");
		}
		
		mobPaloList.setResultCode("A012051");
		return mobPaloList;
	}
}
