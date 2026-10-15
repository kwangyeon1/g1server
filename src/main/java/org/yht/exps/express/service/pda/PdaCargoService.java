package org.yht.exps.express.service.pda;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.pda.PdaCargoMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsCargo.BmsCargo;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.disposal.SpCallVo;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.exps.express.service.bms.BmsInService;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import io.micrometer.core.instrument.util.StringUtils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PdaCargoService {

	@Autowired
	BmsCommService bmsCommService;

	@Autowired
	BmsInService bmsInService;

	@Autowired
	PdaCargoMapper pdaCargoMapper;

	public BmsCargo selectCargoTracking(Map<String, Object> search) throws Exception {
		BmsCargo bmsCargo = new BmsCargo();
		Map<String, Object> cargoMap = pdaCargoMapper.selectPkgInfo(search);
		List<BmsCargo> cargoList = pdaCargoMapper.selectCargoTracking(cargoMap);
		boolean isUnManifested = "Y".equals(Util.null2str(search.get("unManifested")));
		int inRestockCnt = 0;
		int outRestockCnt = 0;
		String holdReason = StringUtil.EMPTY_STRING;

		bmsCargo.setBlNo(isUnManifested ? "무적" : StringUtils.isNotEmpty((String) cargoMap.get("BL_NO")) ? (String) cargoMap.get("BL_NO") : (String) search.get("blNo"));

		if( isUnManifested ) return bmsCargo;

		Map<String, Object> inspectPrintNo = pdaCargoMapper.selectCargoPrintNo(cargoMap);
		if(inspectPrintNo != null) bmsCargo.setPrintNo(Util.null2str(inspectPrintNo.get("PRINT_NO")));
		
		for(BmsCargo cargo : cargoList) {
			// 반출관련 일시들 다 보이도록 수정 2025.11.18 2sowell
			//if( ("반입".equals(cargo.getDataType()) && inRestockCnt > 0) || ("반출".equals(cargo.getDataType()) && outRestockCnt > 0) ) continue;
			if( ("반입".equals(cargo.getDataType()) && inRestockCnt > 0) ) continue;

			if( StringUtils.isNotEmpty(cargo.getCtPrint()) && !"0".equals(cargo.getCtPrint()) ) {
				bmsCargo.setCtPrint(cargo.getCtPrint());
			}
			
			getOrSetValue(bmsCargo::getCt, bmsCargo::setCt, cargo.getCt());
			getOrSetValue(bmsCargo::getWt, bmsCargo::setWt, cargo.getWt());
			getOrSetValue(bmsCargo::getLocationPlace, bmsCargo::setLocationPlace, cargo.getLocationPlace());
			getOrSetValue(bmsCargo::getCdType, bmsCargo::setCdType, cargo.getCdType());
			getOrSetValue(bmsCargo::getBigSizeYn, bmsCargo::setBigSizeYn, cargo.getBigSizeYn());
			getOrSetValue(bmsCargo::getInspectionCustomsYn, bmsCargo::setInspectionCustomsYn, cargo.getInspectionCustomsYn());
			getOrSetValue(bmsCargo::getIntellectualPropYn, bmsCargo::setIntellectualPropYn, cargo.getIntellectualPropYn());
			getOrSetValue(bmsCargo::getCrimeSelectionYn, bmsCargo::setCrimeSelectionYn, cargo.getCrimeSelectionYn());
			getOrSetValue(bmsCargo::getMgnIssueYn, bmsCargo::setMgnIssueYn, cargo.getMgnIssueYn());
			getOrSetValue(bmsCargo::getDivideClass, bmsCargo::setDivideClass, cargo.getDivideClass());
			getOrSetValue(bmsCargo::getArDate, bmsCargo::setArDate, cargo.getArDate());
			getOrSetValue(bmsCargo::getVesselFlight, bmsCargo::setVesselFlight, cargo.getVesselFlight());
			getOrSetValue(bmsCargo::getMblNo, bmsCargo::setMblNo, cargo.getMblNo());
			getOrSetValue(bmsCargo::getHoldReason, bmsCargo::setHoldReason, cargo.getHoldReason());
			getOrSetValue(bmsCargo::getHoldReleaseYn, bmsCargo::setHoldReleaseYn, cargo.getHoldReleaseYn());
			getOrSetValue(bmsCargo::getSeInspectionCode, bmsCargo::setSeInspectionCode, cargo.getSeInspectionCode());
			getOrSetValue(bmsCargo::getInspectionType, bmsCargo::setInspectionType, cargo.getInspectionType());
			getOrSetValue(bmsCargo::getXrayInspectionCode, bmsCargo::setXrayInspectionCode, cargo.getXrayInspectionCode());
			getOrSetValue(bmsCargo::getAllInspectionFinishYn, bmsCargo::setAllInspectionFinishYn, cargo.getAllInspectionFinishYn());
			getOrSetValue(bmsCargo::getPaln5emInspectionCode, bmsCargo::setPaln5emInspectionCode, cargo.getPaln5emInspectionCode());
			getOrSetValue(bmsCargo::getLastPod5tlStatus, bmsCargo::setLastPod5tlStatus, cargo.getLastPod5tlStatus());
			getOrSetValue(bmsCargo::getLastPodDate, bmsCargo::setLastPodDate, cargo.getLastPodDate());
			getOrSetValue(bmsCargo::getApprovalDate, bmsCargo::setApprovalDate, cargo.getApprovalDate());
			getOrSetValue(bmsCargo::getApprovalCancelDate, bmsCargo::setApprovalCancelDate, cargo.getApprovalCancelDate());
			getOrSetValue(bmsCargo::getMoveCarryInDate, bmsCargo::setMoveCarryInDate, cargo.getMoveCarryInDate());
			getOrSetValue(bmsCargo::getMoveCarryOutDate, bmsCargo::setMoveCarryOutDate, cargo.getMoveCarryOutDate());
			getOrSetValue(bmsCargo::getOutScanDateTime, bmsCargo::setOutScanDateTime, cargo.getOutScanDateTime());
			getOrSetValue(bmsCargo::getDeliveryName, bmsCargo::setDeliveryName, cargo.getDeliveryName());
			getOrSetValue(bmsCargo::getCommodityItemLoc, bmsCargo::setCommodityItemLoc, cargo.getCommodityItemLoc());
			getOrSetValue(bmsCargo::getConsigneeName, bmsCargo::setConsigneeName, cargo.getConsigneeName());

			if("반입".equals(cargo.getDataType())) {
				getOrSetValue(bmsCargo::getCarryInDate, bmsCargo::setCarryInDate, cargo.getCarryInDate());
				getOrSetValue(bmsCargo::getCarryInTime, bmsCargo::setCarryInTime, cargo.getCarryInTime());
				getOrSetValue(bmsCargo::getInLastPodStatus, bmsCargo::setInLastPodStatus, cargo.getLastPodStatus());
				if( "통관후입고".equals(cargo.getLastPodStatus()) ) inRestockCnt++;
			} else if("반출".equals(cargo.getDataType())) {
				getOrSetValue(bmsCargo::getCarryOutDate, bmsCargo::setCarryOutDate, cargo.getCarryOutDate());
				getOrSetValue(bmsCargo::getCarryOutTime, bmsCargo::setCarryOutTime, cargo.getCarryOutTime());
				getOrSetValue(bmsCargo::getOutLastPodStatus, bmsCargo::setOutLastPodStatus, cargo.getLastPodStatus());
				//if( "통관후입고".equals(cargo.getLastPodStatus()) ) outRestockCnt++;
			}
		}
		
		for(BmsCargo cargo : cargoList) {
			if("Y".equals(cargo.getBigSizeYn())) {
				bmsCargo.setBigSizeYn("Y");
			}
			if("Y".equals(cargo.getInspectionCustomsYn())) {
				bmsCargo.setInspectionCustomsYn("Y");
			}
			if("Y".equals(cargo.getIntellectualPropYn())) {
				bmsCargo.setIntellectualPropYn("Y");
			}
			if("Y".equals(cargo.getCrimeSelectionYn())) {
				bmsCargo.setCrimeSelectionYn("Y");
			}

			if( "BOX".equals(cargo.getDataType()) && StringUtils.isNotEmpty(cargo.getLocationPlace()) ) {
				bmsCargo.setLocationPlace(cargo.getLocationPlace());
			}
		}
		
		boolean isExistBmsIn = false;
		for(BmsCargo cargo : cargoList) {
			if("반입".equals(cargo.getDataType())){
				isExistBmsIn = true;
				if( "미부호".equals(cargo.getReferenceNo()) ) bmsCargo.setNotSubmitYn("Y");
			}
		}
		
		if(!isExistBmsIn) bmsCargo.setLocationPlace("반입예정상태");

		if(inRestockCnt > 0) {
			bmsCargo.setArDate(StringUtil.EMPTY_STRING);
			bmsCargo.setCarryInDate(StringUtil.EMPTY_STRING);
			bmsCargo.setCarryInTime(StringUtil.EMPTY_STRING);
			bmsCargo.setMoveCarryInDate(StringUtil.EMPTY_STRING);
		}
		
		// 반출관련 일시들 다 보이도록 수정 2025.11.18 2sowell
		/*
		 * if(outRestockCnt > 0) { bmsCargo.setApprovalDate(StringUtil.EMPTY_STRING);
		 * bmsCargo.setApprovalCancelDate(StringUtil.EMPTY_STRING);
		 * bmsCargo.setCarryOutDate(StringUtil.EMPTY_STRING);
		 * bmsCargo.setCarryOutTime(StringUtil.EMPTY_STRING);
		 * bmsCargo.setMoveCarryOutDate(StringUtil.EMPTY_STRING);
		 * bmsCargo.setOutScanDateTime(StringUtil.EMPTY_STRING); }
		 */

		/* Get Hold & DIVIDE_CLASS */
		if( Objects.nonNull(cargoMap) ) {
			cargoMap.put("srchEntCode", search.get("entCode"));

			List<Map<String, Object>> otherInfoMapList = pdaCargoMapper.selectHoldInfo(cargoMap);

			if( !otherInfoMapList.isEmpty() ) {
				for(Map<String, Object> otherInfoMap : otherInfoMapList) {
					holdReason = StringUtils.isNotEmpty(Util.null2str(otherInfoMap.get("HOLD_REASON"))) ? Util.null2str(otherInfoMap.get("HOLD_CODE_NAME")) : Util.null2str(otherInfoMap.get("HOLD_REASON"));
					if( "Y".equals(Util.null2str(otherInfoMap.get("HOLD_RELEASE_YN"))) && StringUtils.isEmpty(bmsCargo.getHoldReleaseYn()) ) {
						bmsCargo.setHoldReason(holdReason);
						bmsCargo.setHoldReleaseYn(Util.null2str(otherInfoMap.get("HOLD_RELEASE_YN")));
					} else {
						getOrSetValue(bmsCargo::getHoldReason, bmsCargo::setHoldReason, holdReason);
						getOrSetValue(bmsCargo::getHoldReleaseYn, bmsCargo::setHoldReleaseYn, Util.null2str(otherInfoMap.get("HOLD_RELEASE_YN")));
					}
				}
			}

			bmsCargo.setDivideClass(pdaCargoMapper.selectExistDivideCount(cargoMap) > 0 ? "Y" : StringUtil.EMPTY_STRING);
			bmsCargo.setCargoReferenceNo(Util.null2str(cargoMap.get("CARGO_REFERENCE_NO")));
		}

		boolean notExistInspection = true;
		List<BmsCargo> bmsInCargoList =  cargoList.stream().filter(cargo -> "반입".equals(cargo.getDataType())).collect(Collectors.toList());
		if(bmsInCargoList != null && bmsInCargoList.size() >0 ) {
			for(BmsCargo cargo : bmsInCargoList) {
				if("반입".equals(cargo.getDataType())) {
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
			}
		} else {
			for(BmsCargo cargo : cargoList) {
				if("반입예정".equals(cargo.getDataType())) {
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
			}
		}
		
		
		if(notExistInspection) {
			bmsCargo.setAllInspectionFinishYn("");
		}else {
			//반입이 존재하지않으면 무족건 검사중(false)
			boolean isAllFinished = cargoList.stream().anyMatch(cargo -> "반입".equals(cargo.getDataType())) ? true : false;
			for(BmsCargo cargo : cargoList) {
				if("반입".equals(cargo.getDataType())) {
					if(!("N".equals(cargo.getBigSizeYn()) && "N".equals(cargo.getInspectionCustomsYn()) && "N".equals(cargo.getIntellectualPropYn()) &&
					"N".equals(cargo.getCrimeSelectionYn()) && StringUtils.isEmpty(cargo.getSeInspectionCode()) && StringUtils.isEmpty(cargo.getXrayInspectionCode()) &&
					StringUtils.isEmpty(cargo.getInspectionType()) && StringUtils.isEmpty(cargo.getInspectionTypeA()) && StringUtils.isEmpty(cargo.getInspectionTypeB()) &&
					StringUtils.isEmpty(cargo.getPaln5emInspectionCode()) )
					) {
						if("N".equals(Util.null2str(cargo.getAllInspectionFinishYn()))) { // 화물리스트에 한번도  검사중(false)이 존재한적 적없을때
							isAllFinished = false;
						}
					}
				}
			}
			
			if(isAllFinished) {
				bmsCargo.setAllInspectionFinishYn("검사완료");
			}else {
				bmsCargo.setAllInspectionFinishYn("검사중");
			}
		}
		
		/* Set State All Inspection Finish */

		bmsCargo.setArDate(Util.null2str(bmsCargo.getArDate()) + " " + Util.null2str(bmsCargo.getArTime()));
		bmsCargo.setCarryInDate(Util.null2str(bmsCargo.getCarryInDate()) + " " + Util.null2str(bmsCargo.getCarryInTime()));
		bmsCargo.setCarryOutDate(Util.null2str(bmsCargo.getCarryOutDate()) + " " + Util.null2str(bmsCargo.getCarryOutTime()));
		if( ! Objects.isNull(cargoMap) ) bmsCargo.setOdJobNo(Util.null2str(cargoMap.get("OD_JOB_NO")));
		
		cargoMap.put("entCode", search.get("entCode"));
		List<Map<String, Object>> inspectResultList = pdaCargoMapper.selectInspectResultNotFinished(cargoMap);
		String inspectionResultType = inspectResultList.stream()
			    .map(map -> (String) map.get("BMS_INSPECTION_TYPE"))
			    .filter(Objects::nonNull)
			    .collect(Collectors.joining(","));
		bmsCargo.setInspectionResultType(inspectionResultType);

		/* 분류 메세지 */
		bmsCargo.setRsltMsg(pdaCargoMapper.selectSortHistoryMsg(search));

		/* 판독일시 */
		String xrayPassTime = pdaCargoMapper.selectXrayPassTime(search);
		bmsCargo.setXrayPassTime(xrayPassTime);
		if( StringUtils.isEmpty(bmsCargo.getXrayInspectionCode()) ) {
			bmsCargo.setXrayInspectionCode( StringUtils.isEmpty(xrayPassTime) ? ("Y".equals(bmsCargo.getBigSizeYn()) ? "판독대상아님" : "미판독") : "이상없음");
		}
		
		/* BCR 일시 */
		bmsCargo.setBcrTime((String) cargoMap.get("BCR_TIME"));

		/* 업무 권고 */
		bmsCargo.setAdviceWork(pdaCargoMapper.selectAdviceWork(search));

		bmsCargo.setBigSizeYn("Y".equals(bmsCargo.getBigSizeYn()) ? "거대" : "");
		bmsCargo.setInspectionCustomsYn("Y".equals(bmsCargo.getInspectionCustomsYn()) ? "집중" : "");
		bmsCargo.setIntellectualPropYn("Y".equals(bmsCargo.getIntellectualPropYn()) ? "지재권" : "");

		return bmsCargo;
	}

	private String isAllInspectionFinish(BmsCargo bmsCargo) {
		boolean isAllFinish = "Y".equals(Util.null2str(bmsCargo.getAllInspectionFinishYn()));
		boolean isInspection = false;

		if( "Y".equals(Util.null2str(bmsCargo.getBigSizeYn())) ) isInspection = true;
		else if( "Y".equals(Util.null2str(bmsCargo.getInspectionCustomsYn())) ) isInspection = true;
		else if( "Y".equals(Util.null2str(bmsCargo.getIntellectualPropYn())) ) isInspection = true;
		else if( "Y".equals(Util.null2str(bmsCargo.getCrimeSelectionYn())) ) isInspection = true;
		else if( StringUtils.isNotEmpty(bmsCargo.getSeInspectionCode()) ) isInspection = true;
		else if( StringUtils.isNotEmpty(bmsCargo.getInspectionType()) ) isInspection = true;
		else if( StringUtils.isNotEmpty(bmsCargo.getInspectionTypeA()) ) isInspection = true;
		else if( StringUtils.isNotEmpty(bmsCargo.getInspectionTypeB()) ) isInspection = true;
		else if( StringUtils.isNotEmpty(bmsCargo.getXrayInspectionCode()) ) isInspection = true;
		else if( StringUtils.isNotEmpty(bmsCargo.getPaln5emInspectionCode()) ) isInspection = true;

		return isInspection ? (isAllFinish ? "완료" : "검사중") : StringUtil.EMPTY_STRING;
	}

	public List<Map<String, Object>> selectCargoTrackingBox(Map<String, Object> search) {
		return pdaCargoMapper.selectCargoTrackingBox(search);
	}
	
	public Map<String, Object> getHoldList(Map<String, Object> search) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		search.put("OD_JOB_NO", Util.null2str(search.get("odJobNo")));
		List<Map<String, Object>> otherInfoMapList = pdaCargoMapper.selectHoldInfo(search);
		
		String holdCodeNameAll = otherInfoMapList.stream()
			    .map(m -> String.valueOf(m.get("HOLD_REASON")))
			    .filter(name -> name != null && !name.isEmpty())
			    .collect(Collectors.joining(","));
		
		resultMap.put("holdList", otherInfoMapList);
		resultMap.put("holdCodeNameAll", holdCodeNameAll);
		return resultMap;
	}
	
	public Map<String, Object> updateHold(Map<String, Object> search, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		int holdCnt = pdaCargoMapper.selectHoldInfoCnt(search);
		
		if(holdCnt > 0) {
			resultMap.put("errorMag", "fail");
		}else { 
			pdaCargoMapper.updateHold(search);
			
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("entCode", Util.null2str(search.get("entCode")));
			tmpMap.put("updateUserId", Util.null2str(search.get("updateUserId")));
			tmpMap.put("odJobNo", Util.null2str(search.get("odJobNo")));
			tmpMap.put("stockRemark", "ECM POD JOB NO");
			tmpMap.put("podStatus", "ECM286");
			tmpMap.put("podName", "HOLD");
			tmpMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
			tmpMap.put("podTableName", 	"TB_ECM_POD_05");
			tmpMap.put("remarks", "PDA 홀드건 출고허용");
			pdaCargoMapper.insertPodHistory(tmpMap);
			
			resultMap.put("errorMag", "ok");
		}
		
		return resultMap;
	}

    public static <T> T getOrSetValue(Supplier<T> getter, Consumer<T> setter, T defaultValue) {
        T value = getter.get();
        boolean shouldSet = false;

        if (value == null) {
            shouldSet = true;
        } else if (value instanceof String) {
            String strVal = ((String) value).trim();
            String defVal = StringUtils.isNotEmpty((String) defaultValue) ? ((String) defaultValue).trim() : null;
            if ( strVal.isEmpty() || (!strVal.isEmpty() && defVal != null && !strVal.equals(defVal)) ) {
                shouldSet = true;
            }
        } else if (value instanceof Number && defaultValue instanceof Number) {
            double valNum = ((Number) value).doubleValue();
            double defNum = ((Number) defaultValue).doubleValue();
            if ( valNum == 0.0 || (valNum != 0.0 && defNum != 0.0 && valNum != defNum) ) {
                shouldSet = true;
            }
        }

        if (shouldSet) {
            setter.accept(defaultValue);
            return defaultValue;
        }

        return value;
    }

	public void insertCargoRestock(Map<String, Object> map, HttpServletRequest request) throws Exception {
		CarryIn initBmsIn = pdaCargoMapper.selectInitBmsInInfo(map);
		BmsOut initBmsOut = pdaCargoMapper.selectInitBmsOutInfo(map);
		SpCallVo voIn = bmsInService.getBmsInCustomsReportNo(Util.null2str(map.get("bmsWhCode")));
		log.debug("[화물조회 - 출고 후 재입고] 반입신고번호 : {}", voIn.getRetCustomsReportNo());
		initBmsIn.setCustomsReportNo(voIn.getRetCustomsReportNo());
		initBmsIn.setRegisterUserId(Util.null2str(map.get("registerUserId")));
		initBmsIn.setUpdateUserId(Util.null2str(map.get("updateUserId")));
		initBmsOut.setRegisterUserId(Util.null2str(map.get("registerUserId")));
		initBmsOut.setUpdateUserId(Util.null2str(map.get("updateUserId")));

		/* 반출통고/체화일자 조회 */
		Map<String, Object> dateMap = pdaCargoMapper.selectEntBusOptionDate(map);
		initBmsIn.setOutNoticeDate(Util.null2str(dateMap.get("OUT_NOTICE_DATE")));
		initBmsIn.setPeriodOverDate(Util.null2str(dateMap.get("PERIOD_OVER_DATE")));

		pdaCargoMapper.insertReStockInData(initBmsIn);
		pdaCargoMapper.insertReStockOutData(initBmsOut);

		/** 작업내역 로그시작 */
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(map.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(map.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(map.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setDbType(BmsConsts.DbType.BMS_CARGO);
		processLogs.setDescription(BmsConsts.DbType.BMS_CARGO+" [출고후재입고(PDA)]");
		processLogs.setJobLog(new Gson().toJson(map));
		processLogs.setJobNo(Util.null2str(map.get("odJobNo")));
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(map.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(map.get("logUserNameEng")));
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.BMS_CARGO_RESTOCK
				, "(" + Util.null2str(map.get("packingNo")) + ")"
				, null
		);
	}
}