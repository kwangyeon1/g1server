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
public class PdaLocationPaloService {
	
	@Autowired
	PdaLocationPaloMapper pdaLocationPaloMapper;
	@Autowired
	PdaInspectPaisMapper pdaInspectPaisMapper;
	
	@Autowired
	BmsCommService bmsCommService;
	
	/**
	 * 로케이션적치 - 로케이션정보 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectLocationPlace(SearchInList search) throws Exception {
		return pdaLocationPaloMapper.selectLocationPlace(search);
	}
	
	/**
	 * 로케이션적치 - 원본로케이션 업데이트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public Map<String, Object> updateOrigLocation(Map<String, Object> search, HttpServletRequest request) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put("resultCode", "A012051");
		
		String locationCode = Util.null2str(search.get("srchOrigLocation"));
		if("".equals(locationCode)) {
			result.put("resultCode", "A00313");
			return result;
		}
		if(!locationCode.matches("^[a-zA-Z0-9_-]+$")) {
			result.put("resultCode", "영문자,숫자,_,-만입력가능합니다.");
			return result;
		}
		
		if( "Y".equals(Util.null2str(search.get("applyAllLoactionYn"))) && Util.null2int(search.get("srchInPlanNo")) <= 0 ) {
			result.put("resultCode", "EXPS003188");
			return result;
		}
		
		Map<String, Object> checkParam = new HashMap<String, Object>();
		checkParam.put("locationPlace", search.get("srchOrigLocation"));
		Map<String, Object> checkInfo = pdaLocationPaloMapper.selectLocationInfoDetail(checkParam);
		if(checkInfo == null) {
			result.put("resultCode", "A11481");
			return result;
		}
		
		int cnt = pdaLocationPaloMapper.updateOrigLocation(search);
		if(cnt == 0) {
			result.put("resultCode", "EXPS003188");
			return result;
		}
		
		/** pod 저장 */
    	CarryIn carryIn = new CarryIn();
		carryIn.setPdaYn("Y");
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.get("srchEntCode")));
		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [PDA 로케이션 원본 로케이션]");
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobNo(Util.null2str(search.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.LOCATION_SAVE /* pod Status 값 매핑 */
				, Util.null2str(search.get("srchOrigLocation") + "-최초로케이션(" + search.get("packingNo") + ")") // 수정 후_최초로케이션코드 (# 박스번호)
				, carryIn
		);
		
		return result;
	}

	/**
	 * 로케이션적치 - 박스번호조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobPaloList selectPkgNoInfo(SearchMobBwlo search, HttpServletRequest request) throws Exception {
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
			mobPaloList.setTotal(0);
			mobPaloList.setScanResult("무적");
			// 스캔로그 저장
			paramMap.put("result", mobPaloList.getScanResult());
			pdaLocationPaloMapper.insertLocationScanLog(paramMap);
			return mobPaloList;
		}
		
		List<MobPalo> list = pdaLocationPaloMapper.selectPkgNoInfo(search);
		mobPaloList.setMobPaloList(list);
		
		if(list.size() > 0) {
			mobPaloList.setTotal(list.get(0).getTotal());
			for(MobPalo loc : list) {
				paramMap.put("blNo", loc.getBlNo());
				paramMap.put("storageDays", loc.getStorageDays());
				paramMap.put("inspectionYn", loc.getInspection());
				paramMap.put("holdHistory", loc.getHoldHistory());
				paramMap.put("scanCt", loc.getCt());
				paramMap.put("approvalDate", loc.getApprovalDate());
        	}
			
			if (list.stream().noneMatch(loc -> "반입".equals(loc.getDGbn()))) { // "반입" 레코드가 없는경우
				mobPaloList.setScanResult("미반입");
				
	        } else { // 2개 레코드인 경우
	        	if("Y".equals(search.getLocationChangeYn())) { // 기존 로케이션에서 다른걸로 변경수행 여부 (Y:변경, N:변경안함)
	        		mobPaloList.setScanResult("완료");
	        		for(MobPalo loc : list) {
		        		if("반입".equals(loc.getDGbn())) {
		        			//현재 멀티건을 1개만 찍었을때도 반입(TB_BMS_IN)의 LOCATION_PLACE를 업데이트해버린다
		        			//전부 찍었을때 업데이트하도록 변경할 필요가 있다
	    					paramMap.put("inNo", loc.getInNo());
	    					paramMap.put("inPlanNo", loc.getInPlanNo());
	    					paramMap.put("odJobNo", loc.getOdJobNo());
	    					// 검사인데 검사완료가 아닌 경우 "검사미완료"
	    					if("Y".equals(loc.getInspection()) && !"Y".equals(loc.getInspectionFinishYn())) {
	    						mobPaloList.setScanResult("검사미완료");
	    					}
	    					
		        		} else { // 반입예정
		        			paramMap.put("inPlanNo", loc.getInPlanNo());
		        		}
		        		paramMap.put("cargoReferenceNo", loc.getCargoReferenceNo());
		        	}
		        	
		        	paramMap.put("locationPlace", Util.null2str(search.getLocationPlaceCode()));
		        	paramMap.put("packingNo", Util.null2str(search.getSrchNo()));
		        	paramMap.put("updateUserId", Util.null2str(search.getUpdateUserId()));
		        	pdaLocationPaloMapper.updateBmsInAndInPlanPkgLocationInfo(paramMap);
		        	pdaLocationPaloMapper.updateBmsInLocationInfo(paramMap);
		        	
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
	        	} else {
	        		mobPaloList.setScanResult("미완료");
	        	}
	        }
		}else { // 0개 레코드인 경우
			int cntPkgAlreadyOut = pdaLocationPaloMapper.getCntPkgOutCt(search);
			if(cntPkgAlreadyOut>0) {
				mobPaloList.setTotal(0);
				mobPaloList.setScanResult("이미출고됨");
			}else {
				mobPaloList.setTotal(0);
				mobPaloList.setScanResult("삭제된박스");
			}
			
		}
		
		// 스캔로그 저장
		paramMap.put("result", mobPaloList.getScanResult());
		pdaLocationPaloMapper.insertLocationScanLog(paramMap);
		
		return mobPaloList;
	}
	
	/**
	 * 로케이션적치 - 박스번호조회 NEW
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public MobPaloList selectPkgNoInfoNew(SearchMobBwlo search, HttpServletRequest request) throws Exception {
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
		
		MobPalo loc = pdaLocationPaloMapper.selectPkgNoInfoNew(search);
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
		    	pdaLocationPaloMapper.updateBmsInAndInPlanPkgLocationInfo2(paramMap);
		    	
		    	paramMap.put("cargoReferenceNo", loc.getCargoReferenceNo());
		    	pdaLocationPaloMapper.updateBmsInLocationInfo(paramMap);
		    	
		    	if("".equals(loc.getOrigLocationPlace())) {
		    		loc.setOrigLocationPlace(Util.null2str(paramMap.get("locationPlace")));
		    	}
		    	
		    	if(!"".equals(Util.null2str(search.getSrchOrigLocation()))) {
		    	//	Map<String, Object> origLocParam = new HashMap<String, Object>();
		    	//	origLocParam.put("applyAllLoactionYn", search.getApplyAllLoactionYn());
		    	//	origLocParam.put("srchInPlanNo", search.getSrchInPlanNo());
		    	//	origLocParam.put("srchOrigLocation", search.getSrchOrigLocation());
		    	//	origLocParam.put("updateUserId", Util.null2str(search.getUpdateUserId()));
		    	//	int cnt = pdaLocationPaloMapper.updateOrigLocation(origLocParam);
		    	//	if(cnt > 0) {
		    			loc.setOrigLocationPlace(Util.null2str(search.getSrchOrigLocation()));
		    	//	}
		    	}
		    	
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
			} else {
				mobPaloList.setScanResult("미완료");
			}
			
			int cntPkgAlreadyOut = pdaLocationPaloMapper.getCntOutDateTimeFromBmsOut(search);
			if(cntPkgAlreadyOut>0) {
				mobPaloList.setScanResult("이미출고됨");
			}
			
		}
		
		// 스캔로그 저장
		paramMap.put("result", mobPaloList.getScanResult());
		pdaLocationPaloMapper.insertLocationScanLog(paramMap);
		
		return mobPaloList;
	}
	
	/**
	 * 로케이션적치 - 박스번호조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobPaloList batchSaveLocation(SearchMobBwlo search, HttpServletRequest request) throws Exception {
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
		List<MobPalo> list = pdaLocationPaloMapper.selectPkgNoByPlanNoAndBlNo(search);
		mobPaloList.setMobPaloList(list);
		
    	if("Y".equals(search.getLocationChangeYn())) { // 기존 로케이션에서 다른걸로 변경수행 여부 (Y:변경, N:변경안함)
    		mobPaloList.setScanResult("완료");
    		if(list != null && list.size()>0) {
    			Map<String, Object> inspectResultParam = new HashMap<String, Object>();
    			inspectResultParam.put("inPlanNo", search.getSrchInPlanNo());
    			List<Map<String, Object>> rstList = pdaInspectPaisMapper.selectNotFinishedInspectResultByInPlanNo(inspectResultParam);
    			
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
	        		paramMap.put("result", mobPaloList.getScanResult());
	        		pdaLocationPaloMapper.insertLocationScanLog(paramMap);
	        	}
	    		
	    		paramMap.put("srchBlNo	", Util.null2str(search.getSrchBlNo()));
	    		paramMap.put("inPlanNo", search.getSrchInPlanNo());
	        	paramMap.put("locationPlace", Util.null2str(search.getLocationPlaceCode()));
	        	paramMap.put("updateUserId", Util.null2str(search.getUpdateUserId()));
	        	pdaLocationPaloMapper.updateLocationFromInPlanPkg(paramMap);
	        	
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
	
	/**
	 * 로케이션적치 - 공로케이션조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobPaloList selectEmptyLocation(SearchBmsPkgNoList search) throws Exception {
		MobPaloList mobPaloList = new MobPaloList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<MobPalo> list = pdaLocationPaloMapper.selectEmptyLocation(search);
		mobPaloList.setMobPaloList(list);
		if(list.size() > 0) {
			mobPaloList.setTotal(list.get(0).getTotal());
		}else {
			mobPaloList.setTotal(0);
		}
		return mobPaloList;
	}
	
	/**
	 * 로케이션적치 - 로케이션 스캔로그조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectScanLogInfo(Map<String, Object> search) throws Exception {
		List<Map<String, Object>> list = pdaLocationPaloMapper.selectScanLogInfo(search);
		
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
	 * 로케이션적치 - 홀드조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> selectBmsInHoldInfo(SearchBmsPkgNoList search) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		result = pdaLocationPaloMapper.selectBmsInHoldInfo(search);
		return result;
	}
	
	/**
	 * 로케이션적치 - 검사,검역조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> selectBmsInInspectionInfo(SearchBmsPkgNoList search) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		result = pdaLocationPaloMapper.selectBmsInInspectionInfo(search);
		return result;
	}
	
	/**
	 * 로케이션 추가
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addLocation(Map<String, Object> search, HttpServletRequest request) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put("resultCode", "A012051");
		
		String location = Util.null2str(search.get("location"));
		if("".equals(location)) {
			result.put("resultCode", "A00313");
			return result;
		}
		if(!location.matches("^[a-zA-Z0-9_-]+$")) {
			result.put("resultCode", "영문자,숫자,_,-만입력가능합니다.");
			return result;
		}
		
		String locationName = Util.null2str(search.get("locationName"));
		if("".equals(locationName)) {
			result.put("resultCode", "A00313");
			return result;
		}
		
		Map<String, Object> checkParam = new HashMap<String, Object>();
		checkParam.put("locationPlace", search.get("location"));
		Map<String, Object> checkInfo = pdaLocationPaloMapper.selectLocationInfoDetail(checkParam);
		if(checkInfo != null) {
			result.put("resultCode", "A11470");
			return result;
		}
		
		search.put("remarks", "로케이션값 추가(PDA)");
		int cnt = pdaLocationPaloMapper.insertLocation(search);
		if(cnt == 0) {
			result.put("resultCode", "EXPS003188");
		}
		
		return result;
	}
	
	public BmsInPlanPkgResult selectPkgWithBmsInByLocation(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInPlanPkgResult bmsInPlanPkgResult = new BmsInPlanPkgResult();
		
		Map<String, Object> locationInfo = pdaLocationPaloMapper.selectLocationInfoDetail(search);
		bmsInPlanPkgResult.setLocationInfo(locationInfo);
		
		List<InPlanPkg> inPlanPkgList = pdaLocationPaloMapper.selectPkgWithBmsInByLocation(search);
		for(InPlanPkg pkgInfo : inPlanPkgList) {
			if("Y".equals(pkgInfo.getInspectionType()) && !"Y".equals(pkgInfo.getInspectionFinishYn())) {
				pkgInfo.setScanResult("검사미완료");
			}
		}
		
		bmsInPlanPkgResult.setInPlanPkgList(inPlanPkgList);
		
		bmsInPlanPkgResult.setResultCode("A012051");
		bmsInPlanPkgResult.setReason("완료");
		
		return bmsInPlanPkgResult;
	}
	
	/**
	 * scan: 로케이션피킹 - 박스조회를 통한 로케이션 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInPlanPkgResult selectLocationFromPkg(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInPlanPkgResult bmsInPlanPkgResult = new BmsInPlanPkgResult();
		
		InPlanPkg inPlanPkg = pdaLocationPaloMapper.selectLocationFromPkg(search);
		bmsInPlanPkgResult.setInPlanPkg(inPlanPkg);
		
		bmsInPlanPkgResult.setResultCode("A012051");
		bmsInPlanPkgResult.setReason("완료");
		
		return bmsInPlanPkgResult;
	}
	
	/**
	 * scan: 로케이션피킹 - 박스조회를 통한 로케이션 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInPlanPkgResult selectLocationFromAllPkgByScan(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInPlanPkgResult bmsInPlanPkgResult = new BmsInPlanPkgResult();
		
		List<InPlanPkg> inPlanPkgList = pdaLocationPaloMapper.selectLocationFromAllPkgByScan(search);
		bmsInPlanPkgResult.setInPlanPkgList(inPlanPkgList);
		
		bmsInPlanPkgResult.setResultCode("A012051");
		bmsInPlanPkgResult.setReason("완료");
		
		return bmsInPlanPkgResult;
	}
	
	/**
	 * scan: 로케이션피킹 - 박스 피킹설정 (박스 로케이션 업데이트)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInPlanPkgResult updateBmsInPkgLocationPicking(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInPlanPkgResult bmsInPlanPkgResult = new BmsInPlanPkgResult();
		
		bmsInPlanPkgResult.setResultCode("A012051");
		String reason = "";
		
		if( "Y".equals(Util.null2str(search.get("unManifested"))) ) {
			reason = "무적";
			
			Map<String, Object> scanlogParam = new HashMap<String, Object>();
			scanlogParam.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			scanlogParam.put("locationStatus", "P");
			scanlogParam.put("locationPlace", "");
			scanlogParam.put("blNo", "");
			scanlogParam.put("storageDays", "");
			scanlogParam.put("inspectionYn", "");
			scanlogParam.put("holdHistory", "");
			scanlogParam.put("result", reason);
			scanlogParam.put("scanCt", 0);
			scanlogParam.put("approvalDate", "");
			scanlogParam.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaLocationPaloMapper.insertLocationScanLog(scanlogParam);
			
			bmsInPlanPkgResult.setReason(reason);
			return bmsInPlanPkgResult;
		}
		
		List<Map<String, Object>> allPackingNoList = (List<Map<String, Object>>) search.get("allPackingNoList");
		if(allPackingNoList == null) {
			bmsInPlanPkgResult.setResultCode("A11481"); //해당 #1# 데이터는 존재하지 않습니다.
			return bmsInPlanPkgResult;
		}
		Map<String, Object> scanParam = new HashMap<String, Object>();
		scanParam.put("scanPackingNo", Util.null2str(search.get("scanPackingNo")));
		scanParam.put("srchInPlanNo", Util.null2str(search.get("srchInPlanNo")));
		scanParam.put("srchBlNo", Util.null2str(search.get("srchBlNo")));
		InPlanPkg scanInPlanPkg = pdaLocationPaloMapper.selectOnePkgWithBmsInByPackingNo(scanParam);
		if(scanInPlanPkg == null) {
			reason = "피킹대상아님";
			
			Map<String, Object> logParamg2 = new HashMap<String, Object>();
			logParamg2.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg2.put("locationStatus", "P");
			logParamg2.put("locationPlace", "");
			logParamg2.put("blNo", "");
			logParamg2.put("storageDays", "");
			logParamg2.put("inspectionYn", "");
			logParamg2.put("holdHistory", "");
			logParamg2.put("result", reason);
			logParamg2.put("scanCt", 0);
			logParamg2.put("approvalDate", "");
			logParamg2.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaLocationPaloMapper.insertLocationScanLog(logParamg2);
			
			bmsInPlanPkgResult.setReason(reason);
			return bmsInPlanPkgResult;
		}
		
		Map<String, Object> param = new HashMap<String, Object>();
		String[] parts = Util.null2str(search.get("locationInfoStr")).split(":");
		String srchLocationPlace = parts[0].trim();
		param.put("locationPlace", srchLocationPlace);
		param.put("packingNo", allPackingNoList);
		if(allPackingNoList.size() == 0) {
			Map<String, Object> pkgMap = new HashMap<>();
			pkgMap.put("packingNo", scanInPlanPkg.getPackingNo());
			pkgMap.put("inPlanNo", scanInPlanPkg.getInPlanNo());
			pkgMap.put("blNo", scanInPlanPkg.getBlNo());
			allPackingNoList.add(pkgMap);
		}
		List<InPlanPkg> inPlanPkgList = pdaLocationPaloMapper.selectPkgWithBmsInByPackingNoList(param);
		
		InPlanPkg matchedPkg = null;
		boolean isMatchedbox = false;
		for(InPlanPkg pkg : inPlanPkgList) {
			if( pkg.getPackingNo().equals(scanInPlanPkg.getPackingNo()) ) {
				matchedPkg = pkg;
				isMatchedbox = true;
				if( "L".equals(pkg.getLocationStatus()) ) {
					reason = "완료";
					
					/** pod 저장 */
		        	CarryIn carryIn = new CarryIn();
		    		carryIn.setPdaYn("Y");
		    		
		    		String remark = Util.null2str(search.get("locationInfoStr")) + "|"+ pkg.getPackingNo();
		    		
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(search.get("entCode")));
					processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		    		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		    		processLogs.setLogJobType("DATA");
		    		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		    		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		    		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [PDA 로케이션 피킹]");
		    		processLogs.setJobLog(new Gson().toJson(pkg));
		    		processLogs.setJobNo(Util.null2str(pkg.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		    		processLogs.setResult("ok");
		    		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
					processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		    		bmsCommService.insertLogAndPodStatus(processLogs, request
		    				, BmsConsts.PodStatus.LOCATION_PICKING /* pod Status 값 매핑 */
		    				, remark // 비고 "D-01-00" (로케이션코드)
		    				, carryIn
		    		);
				}else if( "P".equals(pkg.getLocationStatus()) ) {
					reason = "이미피킹됨";
				}
				
				Map<String, Object> logParam = new HashMap<String, Object>();
				logParam.put("packingNo", pkg.getPackingNo());
				logParam.put("locationStatus", "P");
				logParam.put("locationPlace", pkg.getLocationPlace());
				logParam.put("inPlanNo", pkg.getInPlanNo());
				logParam.put("blNo", pkg.getBlNo());
				logParam.put("storageDays", pkg.getStorageDays());
				logParam.put("inspectionYn", Util.null2str(pkg.getInspectionType()));
				logParam.put("holdHistory", Util.null2str(pkg.getHoldHistory()));
				logParam.put("result", reason);
				logParam.put("scanCt", pkg.getInCt());
				logParam.put("approvalDate", Util.null2str(pkg.getApprovalDate()));
				logParam.put("registerUserId", Util.null2str(search.get("updateUserId")));
				pdaLocationPaloMapper.insertLocationScanLog(logParam);
				
				if( "L".equals(pkg.getLocationStatus()) ) {
					logParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
					pdaLocationPaloMapper.updateInPlanPkgLocationInfo(logParam);
					pdaLocationPaloMapper.updateInLocationInfo(logParam);
				}
			}
		}
		
		if(!isMatchedbox) {
			if("L".equals(scanInPlanPkg.getLocationStatus())) {
				reason = "로케이션다름";
			}else if("P".equals(scanInPlanPkg.getLocationStatus())){
				reason = "이미피킹됨";
			}else if("".equals(scanInPlanPkg.getLocationStatus())){
				reason = "로케이션정보없음";
			}
			
			Map<String, Object> logParamg2 = new HashMap<String, Object>();
			logParamg2.put("packingNo", scanInPlanPkg.getPackingNo());
			logParamg2.put("locationStatus", "P");
			logParamg2.put("locationPlace", scanInPlanPkg.getLocationPlace());
			logParamg2.put("blNo", scanInPlanPkg.getBlNo());
			logParamg2.put("storageDays", scanInPlanPkg.getStorageDays());
			logParamg2.put("inspectionYn", Util.null2str(scanInPlanPkg.getInspectionType()));
			logParamg2.put("holdHistory", Util.null2str(scanInPlanPkg.getHoldHistory()));
			logParamg2.put("result", reason);
			logParamg2.put("scanCt", scanInPlanPkg.getInCt());
			logParamg2.put("approvalDate", Util.null2str(scanInPlanPkg.getApprovalDate()));
			logParamg2.put("registerUserId", Util.null2str(search.get("updateUserId")));
			pdaLocationPaloMapper.insertLocationScanLog(logParamg2);
			
			bmsInPlanPkgResult.setReason(reason);
			return bmsInPlanPkgResult;
		}
		
		search.put("locationPlace", matchedPkg.getLocationPlace());
		bmsInPlanPkgResult = this.selectPkgWithBmsInByLocation(search, request);
		
		List<InPlanPkg> printInPlanPkgList = new ArrayList<InPlanPkg>();
		List<InPlanPkg> searchedInPlanPkgList = bmsInPlanPkgResult.getInPlanPkgList();
		for(InPlanPkg pkg : searchedInPlanPkgList) {
			if(pkg.getPackingNo().equals(matchedPkg.getPackingNo())) {
				printInPlanPkgList.add(pkg);
			}
		}
		/*if(searchedInPlanPkgList != null && searchedInPlanPkgList.size() == 0 && "완료".equals(reason)) {
			printInPlanPkgList.add(matchedPkg);
		}*/
		
		for(InPlanPkg pkg : searchedInPlanPkgList) {
			if(!pkg.getPackingNo().equals(matchedPkg.getPackingNo())) {
				printInPlanPkgList.add(pkg);
			}
		}
		
		bmsInPlanPkgResult.setInPlanPkgList(printInPlanPkgList);
		bmsInPlanPkgResult.setInPlanPkg(scanInPlanPkg);
		bmsInPlanPkgResult.setReason(reason);
		return bmsInPlanPkgResult;
	}
	
	/**
	 * 로케이션피킹 - 박스 일괄피킹 (박스 로케이션 업데이트)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInPlanPkgResult batchBmsInPkgLocationPicking(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInPlanPkgResult bmsInPlanPkgResult = new BmsInPlanPkgResult();
		
		bmsInPlanPkgResult.setResultCode("A012051");
		String reason = "";
		
		List<Map<String, Object>> packingNoList = (List<Map<String, Object>>) search.get("packingNoList");
		if(packingNoList == null || (packingNoList != null && packingNoList.size() == 0)) {
			bmsInPlanPkgResult.setResultCode("A11481"); //해당 #1# 데이터는 존재하지 않습니다.
			return bmsInPlanPkgResult;
		}
		
		Map<String, Object> param = new HashMap<String, Object>();
		String[] parts = Util.null2str(search.get("locationInfoStr")).split(":");
		String srchLocationPlace = parts[0].trim();
		param.put("locationPlace", srchLocationPlace);
		param.put("packingNo", packingNoList);
		List<InPlanPkg> inPlanPkgList = pdaLocationPaloMapper.selectPkgWithBmsInByPackingNoList(param);
		if(inPlanPkgList == null || (inPlanPkgList != null && inPlanPkgList.size() == 0)) {
			bmsInPlanPkgResult.setResultCode("A11481"); //해당 #1# 데이터는 존재하지 않습니다.
			return bmsInPlanPkgResult;
		}
		
		List<InPlanPkg> inPlanPkgNotPickingList = inPlanPkgList.stream().filter( _pkg -> "L".equals(_pkg.getLocationStatus()) ).collect(Collectors.toList());
		if(inPlanPkgNotPickingList == null || (inPlanPkgNotPickingList != null && inPlanPkgNotPickingList.size() == 0)) {
			bmsInPlanPkgResult.setResultCode("ALREADY_PICKED");
			return bmsInPlanPkgResult;
		}
		if(inPlanPkgNotPickingList.size() != inPlanPkgList.size()) {
			bmsInPlanPkgResult.setResultCode("ALREADY_PICKED");
			return bmsInPlanPkgResult;
		}
		
		reason = "완료";
		
		for(InPlanPkg pkg : inPlanPkgNotPickingList) {
			Map<String, Object> logParam = new HashMap<String, Object>();
			logParam.put("packingNo", pkg.getPackingNo());
			logParam.put("locationStatus", "P");
			logParam.put("locationPlace", pkg.getLocationPlace());
			logParam.put("blNo", pkg.getBlNo());
			logParam.put("storageDays", pkg.getStorageDays());
			logParam.put("inspectionYn", Util.null2str(pkg.getInspectionType()));
			logParam.put("holdHistory", Util.null2str(pkg.getHoldHistory()));
			logParam.put("result", reason);
			logParam.put("scanCt", pkg.getInCt());
			logParam.put("approvalDate", Util.null2str(pkg.getApprovalDate()));
			logParam.put("registerUserId", Util.null2str(search.get("updateUserId")));
			logParam.put("inPlanNo", Util.null2str(pkg.getInPlanNo()));
			pdaLocationPaloMapper.insertLocationScanLog(logParam);
			pdaLocationPaloMapper.updateInLocationInfo(logParam);
		}
		
		List<String> pkgNoLandingList = inPlanPkgNotPickingList.stream().map(_pkg -> _pkg.getPackingNo()).collect(Collectors.toList());
		
		Map<String, Object> batchParam = new HashMap<String, Object>();
		batchParam.put("packingNo", inPlanPkgNotPickingList);
		batchParam.put("locationStatus", "P");
		batchParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
		pdaLocationPaloMapper.batchUpdateInPlanPkgLocationInfo(batchParam);
		
		/** pod 저장 */
    	CarryIn carryIn = new CarryIn();
		carryIn.setPdaYn("Y");
		
		String remark = Util.null2str(search.get("locationInfoStr")) + "|"+  String.join(",", pkgNoLandingList);
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [PDA 로케이션 피킹]");
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobNo(Util.null2str(inPlanPkgNotPickingList.get(0).getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.LOCATION_PICKING_BATCH /* pod Status 값 매핑 */
				, remark // 비고 "D-01-00" (로케이션코드)
				, carryIn
		);
	
				
		search.put("locationPlace", srchLocationPlace);
		bmsInPlanPkgResult = this.selectPkgWithBmsInByLocation(search, request);
		
		bmsInPlanPkgResult.setReason(reason);
		return bmsInPlanPkgResult;
	}
}
