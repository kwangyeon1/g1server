package org.yht.exps.express.service.bms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsCargoMapper;
import org.yht.exps.express.model.bms.bmsCargo.BmsCargoHistory;
import org.yht.exps.express.model.bms.bmsCargo.BmsCargoInfo;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.exps.express.service.pda.PdaCargoService;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsCargoService {

	@Autowired
	BmsCargoMapper bmsCargoMapper;
	
	public Map<String, Object> selectCargoTracking(Map<String, Object> search) throws Exception {
		Map<String, Object> res = new HashMap<String, Object>();
		// 아무값도 없는경우
		if("".equals(Util.null2str(search.get("blNo")).trim()) && "".equals(Util.null2str(search.get("packingNo")).trim()) && "".equals(Util.null2str(search.get("cargoReferenceNo")).trim())){
			res.put("cargoInfo", null);
			return res;
		}
		
		if(!"".equals(Util.null2str(search.get("packingNo")).trim())) {
			InPlanPkg pkg = bmsCargoMapper.selectPkgByPkgNo(search);
			//BL번호와 화물관리번호 둘다 없는경우면 inPlanNo설정  
			if(pkg != null && "".equals(Util.null2str(search.get("blNo")).trim()) && "".equals(Util.null2str(search.get("cargoReferenceNo")).trim())) {
				search.put("inPlanNo", pkg.getInPlanNo());
			}
		}
		// 박스번호에 해당하는 박스도 없는경우(= inPlanNo가 없는경우)
		if(Util.null2int(search.get("inPlanNo")) == 0 && "".equals(Util.null2str(search.get("blNo")).trim()) && "".equals(Util.null2str(search.get("cargoReferenceNo")).trim())) {
			res.put("cargoInfo", null);
			return res;
		}
		
		BmsCargoInfo inCargo = bmsCargoMapper.selectCarryIn(search);
		if(inCargo == null) {
			res.put("cargoInfo", null);
			return res;
		}
		
		if(Util.null2int(inCargo.getInNo()) > 0) {
			inCargo.setOutCt(inCargo.getCt() - inCargo.getStockCt());
			
			BigDecimal wt = BigDecimal.valueOf(inCargo.getWt());
			BigDecimal stockWt = BigDecimal.valueOf(inCargo.getStockWt());
			BigDecimal outWt = wt.subtract(stockWt); // wt - outWt
			inCargo.setOutWt(outWt.doubleValue());
		}
		
		search.put("inNo", inCargo.getInNo());
		List<BmsCargoInfo> outCargoList = bmsCargoMapper.selectCarryOut(search);
		for(BmsCargoInfo outCargo : outCargoList) {
			//inCargo.setVesselFlight(outCargo.getVesselFlight());
			inCargo.setBrokerName(outCargo.getBrokerName());
			inCargo.setCarryOutDateTime(outCargo.getCarryOutDateTime());
		}
		
		search.put("odJobNo", inCargo.getOdJobNo());
		Map<String, Object> hold = bmsCargoMapper.selectHoldInfo(search);
		if(hold != null) {
			inCargo.setHoldCode(Util.null2str(hold.get("HOLD_CODE")));
			inCargo.setHoldReason(Util.null2str(hold.get("HOLD_REASON")));
			inCargo.setHoldReleaseYn(Util.null2str(hold.get("HOLD_RELEASE_YN")));
		}
		
		search.put("inPlanNo", inCargo.getInPlanNo());
		Map<String, Object> xray = bmsCargoMapper.selectXray(search);
		if(Util.null2int(inCargo.getXrayImageCt()) <= 0) {
			if(xray != null) {
				inCargo.setXrayImageCt(Util.null2int(xray.get("XRAY_IMAGE_CT")));
			}
		}
		if(Util.null2int(inCargo.getXrayScanCt()) <= 0) {
			if(xray != null) {
				inCargo.setXrayScanCt(Util.null2int(xray.get("XRAY_SCAN_CT")));
			}
		}
		if("".equals(Util.null2str(inCargo.getXrayLineNo()))) {
			if(xray != null) {
				inCargo.setXrayLineNo(Util.null2str(xray.get("XRAY_LINE_NO")));
			}
		}
		
		//search.put("inPlanNo", inCargo.getInPlanNo());
		String xrayPassTime = bmsCargoMapper.selectXrayPassTimeByInPlanNo(search);
		inCargo.setXrayPassTime(Util.null2str(xrayPassTime));
		
		search.put("blNo", inCargo.getBlNo());
		String infoLastClass = bmsCargoMapper.selectSortHistoryMsg(search);
		inCargo.setInfoLastClass(Util.null2str(infoLastClass));
		
		List<BmsInspectResult> inspectRstNotCompleteList = bmsCargoMapper.selectInspectResultNotCompleteByInNo(search); // 미 완료 검사리스트
		
		List<Map<String, Object>> inspectionList = new ArrayList<>();
		if(!"".equals(Util.null2str(inCargo.getXrayInspectionCode()))) {
			String insCode = inCargo.getXrayInspectionCode();
			BmsInspectResult inspectRst = inspectRstNotCompleteList.stream()
				.filter(ins ->  "X".equals(Util.null2str(ins.getInspectionType()).substring(0, 1)) 
						&& insCode.equals( Util.null2str(ins.getInspectionType()).substring(1)))
				.findFirst().orElse(null);
			
			Map<String, Object> inspectionRes = new HashMap<>();
			inspectionRes.put("bmsInspectionType", "xray - "+ inCargo.getXrayInspectionCodeStr());
			if(inspectRst != null) {
				inspectionRes.put("inspectionFinishYn", "N");
				inspectionRes.put("inspectionFinishRemarks", inspectRst.getInspectionFinishRemarks());
			}else {
				Map<String, Object> insParam = new HashMap<String, Object>();
				insParam.put("inNo", search.get("inNo"));
				insParam.put("inspectionType", "X");
				BmsInspectResult inspectRstComplete = bmsCargoMapper.selectInspectResultCompleteByInNoAndType(insParam); // 검사 완료 검사리스트
				
				if(inspectRstComplete != null) {
					inspectionRes.put("inspectionFinishYn", "Y");
					inspectionRes.put("inspectionFinishRemarks", inspectRstComplete.getInspectionFinishRemarks());
				} else {
					inspectionRes.put("inspectionFinishYn", "N");
				}
			}
			
			inspectionList.add(inspectionRes);
		}
		if(!"".equals(Util.null2str(inCargo.getSeInspectionCode()))) {
			String insCode = inCargo.getSeInspectionCode();
			BmsInspectResult inspectRst = inspectRstNotCompleteList.stream()
				.filter(ins ->  "S".equals(Util.null2str(ins.getInspectionType()).substring(0, 1)) 
						&& insCode.equals( Util.null2str(ins.getInspectionType()).substring(1)))
				.findFirst().orElse(null);
			
			Map<String, Object> inspectionRes = new HashMap<>();
			inspectionRes.put("bmsInspectionType", "선별 - "+ inCargo.getSeInspectionCodeStr());
			if(inspectRst != null) {
				inspectionRes.put("inspectionFinishYn", "N");
				inspectionRes.put("inspectionFinishRemarks", inspectRst.getInspectionFinishRemarks());
			}else {
				Map<String, Object> insParam = new HashMap<String, Object>();
				insParam.put("inNo", search.get("inNo"));
				insParam.put("inspectionType", "S");
				BmsInspectResult inspectRstComplete = bmsCargoMapper.selectInspectResultCompleteByInNoAndType(insParam); // 검사 완료 검사리스트
				
				if(inspectRstComplete != null) {
					inspectionRes.put("inspectionFinishYn", "Y");
					inspectionRes.put("inspectionFinishRemarks", inspectRstComplete.getInspectionFinishRemarks());
				} else {
					inspectionRes.put("inspectionFinishYn", "N");
				}
			}
			
			inspectionList.add(inspectionRes);
		}
		if(!"".equals(Util.null2str(inCargo.getPaln5emInspectionCode()))) {
			String insCode = inCargo.getPaln5emInspectionCode();
			BmsInspectResult inspectRst = inspectRstNotCompleteList.stream()
				.filter(ins ->  "P".equals(Util.null2str(ins.getInspectionType()).substring(0, 1)) 
						&& insCode.equals( Util.null2str(ins.getInspectionType()).substring(1)))
				.findFirst().orElse(null);
			
			Map<String, Object> inspectionRes = new HashMap<>();
			inspectionRes.put("bmsInspectionType", "통관 - "+ inCargo.getPaln5emInspectionCodeStr());
			if(inspectRst != null) {
				inspectionRes.put("inspectionFinishYn", "N");
				inspectionRes.put("inspectionFinishRemarks", inspectRst.getInspectionFinishRemarks());
			}else {
				Map<String, Object> insParam = new HashMap<String, Object>();
				insParam.put("inNo", search.get("inNo"));
				insParam.put("inspectionType", "P");
				BmsInspectResult inspectRstComplete = bmsCargoMapper.selectInspectResultCompleteByInNoAndType(insParam); // 검사 완료 검사리스트
				
				if(inspectRstComplete != null) {
					inspectionRes.put("inspectionFinishYn", "Y");
					inspectionRes.put("inspectionFinishRemarks", inspectRstComplete.getInspectionFinishRemarks());
				} else {
					inspectionRes.put("inspectionFinishYn", "N");
				}
			}
			
			inspectionList.add(inspectionRes);
		}
		if(!"".equals(Util.null2str(inCargo.getInspectionType()))) {
			String insCode = inCargo.getInspectionType();
			BmsInspectResult inspectRst = inspectRstNotCompleteList.stream()
				.filter(ins ->  "I".equals(Util.null2str(ins.getInspectionType()).substring(0, 1)) 
						&& insCode.equals( Util.null2str(ins.getInspectionType()).substring(1)))
				.findFirst().orElse(null);
			
			Map<String, Object> inspectionRes = new HashMap<>();
			inspectionRes.put("bmsInspectionType", "검역 - "+ inCargo.getInspectionTypeStr());
			if(inspectRst != null) {
				inspectionRes.put("inspectionFinishYn", "N");
				inspectionRes.put("inspectionFinishRemarks", inspectRst.getInspectionFinishRemarks());
			}else {
				Map<String, Object> insParam = new HashMap<String, Object>();
				insParam.put("inNo", search.get("inNo"));
				insParam.put("inspectionType", "I");
				BmsInspectResult inspectRstComplete = bmsCargoMapper.selectInspectResultCompleteByInNoAndType(insParam); // 검사 완료 검사리스트
				
				if(inspectRstComplete != null) {
					inspectionRes.put("inspectionFinishYn", "Y");
					inspectionRes.put("inspectionFinishRemarks", inspectRstComplete.getInspectionFinishRemarks());
				} else {
					inspectionRes.put("inspectionFinishYn", "N");
				}
			}
			
			inspectionList.add(inspectionRes);
		}
		if("Y".equals(Util.null2str(inCargo.getBigSizeYn()))) {
			String insCode = inCargo.getBigSizeYn();
			BmsInspectResult inspectRst = inspectRstNotCompleteList.stream()
				.filter(ins ->  "B".equals(Util.null2str(ins.getInspectionType()).substring(0, 1)) 
						)
				.findFirst().orElse(null);
			
			Map<String, Object> inspectionRes = new HashMap<>();
			inspectionRes.put("bmsInspectionType", "거대");
			if(inspectRst != null) {
				inspectionRes.put("inspectionFinishYn", "N");
				inspectionRes.put("inspectionFinishRemarks", inspectRst.getInspectionFinishRemarks());
			}else {
				Map<String, Object> insParam = new HashMap<String, Object>();
				insParam.put("inNo", search.get("inNo"));
				insParam.put("inspectionType", "B");
				BmsInspectResult inspectRstComplete = bmsCargoMapper.selectInspectResultCompleteByInNoAndType(insParam); // 검사 완료 검사리스트
				
				if(inspectRstComplete != null) {
					inspectionRes.put("inspectionFinishYn", "Y");
					inspectionRes.put("inspectionFinishRemarks", inspectRstComplete.getInspectionFinishRemarks());
				} else {
					inspectionRes.put("inspectionFinishYn", "N");
				}
			}
			
			inspectionList.add(inspectionRes);
		}
		if("Y".equals(Util.null2str(inCargo.getIntellectualPropYn()))) {
			String insCode = inCargo.getIntellectualPropYn();
			BmsInspectResult inspectRst = inspectRstNotCompleteList.stream()
				.filter(ins ->  "T".equals(Util.null2str(ins.getInspectionType()).substring(0, 1)) 
						)
				.findFirst().orElse(null);
			
			Map<String, Object> inspectionRes = new HashMap<>();
			inspectionRes.put("bmsInspectionType", "지재권");
			if(inspectRst != null) {
				inspectionRes.put("inspectionFinishYn", "N");
				inspectionRes.put("inspectionFinishRemarks", inspectRst.getInspectionFinishRemarks());
			}else {
				Map<String, Object> insParam = new HashMap<String, Object>();
				insParam.put("inNo", search.get("inNo"));
				insParam.put("inspectionType", "T");
				BmsInspectResult inspectRstComplete = bmsCargoMapper.selectInspectResultCompleteByInNoAndType(insParam); // 검사 완료 검사리스트
				
				if(inspectRstComplete != null) {
					inspectionRes.put("inspectionFinishYn", "Y");
					inspectionRes.put("inspectionFinishRemarks", inspectRstComplete.getInspectionFinishRemarks());
				} else {
					inspectionRes.put("inspectionFinishYn", "N");
				}
			}
			
			inspectionList.add(inspectionRes);
		}
		if("Y".equals(Util.null2str(inCargo.getInspectionCustomsYn()))) {
			String insCode = inCargo.getIntellectualPropYn();
			BmsInspectResult inspectRst = inspectRstNotCompleteList.stream()
				.filter(ins ->  "A".equals(Util.null2str(ins.getInspectionType()).substring(0, 1)) 
						)
				.findFirst().orElse(null);
			
			Map<String, Object> inspectionRes = new HashMap<>();
			inspectionRes.put("bmsInspectionType", "집중");
			if(inspectRst != null) {
				inspectionRes.put("inspectionFinishYn", "N");
				inspectionRes.put("inspectionFinishRemarks", inspectRst.getInspectionFinishRemarks());
			}else {
				Map<String, Object> insParam = new HashMap<String, Object>();
				insParam.put("inNo", search.get("inNo"));
				insParam.put("inspectionType", "A");
				BmsInspectResult inspectRstComplete = bmsCargoMapper.selectInspectResultCompleteByInNoAndType(insParam); // 검사 완료 검사리스트
				
				if(inspectRstComplete != null) {
					inspectionRes.put("inspectionFinishYn", "Y");
					inspectionRes.put("inspectionFinishRemarks", inspectRstComplete.getInspectionFinishRemarks());
				} else {
					inspectionRes.put("inspectionFinishYn", "N");
				}
			}
			
			inspectionList.add(inspectionRes);
		}
		inCargo.setInspectionList(inspectionList);
		
		//search.put("inPlanNo", inCargo.getInPlanNo());
		
		res.put("cargoInfo", inCargo);
		return res;
	}
	
	public Map<String, Object> pkgList(Map<String, Object> search) throws Exception {
		Map<String, Object> res = new HashMap<String, Object>();
		
		if("".equals(Util.null2str(search.get("blNo")).trim()) && "".equals(Util.null2str(search.get("packingNo")).trim()) && "".equals(Util.null2str(search.get("cargoReferenceNo")).trim())){
			res.put("pkgList", new ArrayList<>());
			return res;
		}
		
		if(Util.null2int(search.get("page")) != 0 && Util.null2int(search.get("pageSize")) != 0){search.put("startNum",(Util.null2int(search.get("pageSize")) * (Util.null2int(search.get("page")) - 1)));}
		if(Util.null2str(search.get("changeField")).equals("Y")){Util.camelCaseToUnderscoreMap(search);}
		
		List<InPlanPkg> pkgList = bmsCargoMapper.selectPkg(search);
		
		res.put("pkgList", pkgList);
		if(pkgList.size() > 0) {
			res.put("total", pkgList.get(0).getTotal());
		}else {
			res.put("total", 0);
		}
		return res;
	}

	public Map<String, Object> cargoStatusList(Map<String, Object> search) throws Exception {
		Map<String, Object> res = new HashMap<String, Object>();
		
		if("".equals(Util.null2str(search.get("blNo")).trim()) && "".equals(Util.null2str(search.get("packingNo")).trim()) && "".equals(Util.null2str(search.get("cargoReferenceNo")).trim())){
			res.put("cargoStatusList", new ArrayList<>());
			return res;
		}
		
		//if(Util.null2int(search.get("page")) != 0 && Util.null2int(search.get("pageSize")) != 0){search.put("startNum",(Util.null2int(search.get("pageSize")) * (Util.null2int(search.get("page")) - 1)));}
		//if(Util.null2str(search.get("changeField")).equals("Y")){Util.camelCaseToUnderscoreMap(search);}
		
		List<BmsCargoHistory> cargoHistoryList = bmsCargoMapper.selectCargoInOutHistory(search);
		
		res.put("cargoStatusList", cargoHistoryList);
		//if(cargoHistoryList.size() > 0) {
		//	res.put("total", cargoHistoryList.get(0).getTotal());
		//}else {
		//	res.put("total", 0);
		//}
		return res;
	}
	
//	public Map<String, Object> cargoStatusList(Map<String, Object> search) throws Exception {
//		Map<String, Object> res = new HashMap<String, Object>();
//		
//		if(Util.null2int(search.get("page")) != 0 && Util.null2int(search.get("pageSize")) != 0){search.put("startNum",(Util.null2int(search.get("pageSize")) * (Util.null2int(search.get("page")) - 1)));}
//		if(Util.null2str(search.get("changeField")).equals("Y")){Util.camelCaseToUnderscoreMap(search);}
//		
//		BmsCargoInfo inCargo = bmsCargoMapper.selectCarryIn(search); // 반입예정 or 반입 정보
//		
//		search.put("cargoReferenceNo", inCargo.getCargoReferenceNo());
//		List<CarryIn> inList = bmsCargoMapper.selectInList(search); // 화물관리번호에대한 모든 반입리스트
//		List<CarryIn> inFilteredList = inList.stream()
//				.filter(in -> in.getInNo() != inCargo.getInNo()).collect(Collectors.toList()); // 최초반입은 제외한 반입들
//
//		List<Integer> inNoList = new ArrayList<>();
//		if(!inFilteredList.isEmpty()) { // 최초반입외에 다른것들도 존재한다면
//			inNoList = inFilteredList.stream()
//					.map(in -> in.getInNo()).collect(Collectors.toList());
//		}
//		inNoList.add(inCargo.getInNo()); // 최초반입번호 추가
//		
//		search.put("inNoList", inNoList);
//		List<BmsOutPlan> outPlanList = bmsCargoMapper.selectOutPlanList(search);
//		List<BmsOut> outList = bmsCargoMapper.selectOutList(search);
//		
//		res.put("cargoList", outList);
//		if(outList.size() > 0) {
//			res.put("total", outList.get(0).getTotal());
//		}else {
//			res.put("total", 0);
//		}
//		return res;
//	}
}
