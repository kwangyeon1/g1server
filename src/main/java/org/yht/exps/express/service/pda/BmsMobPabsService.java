package org.yht.exps.express.service.pda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsInMapper;
import org.yht.exps.express.mapper.pda.BmsMobPabsMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsPkgNo.search.SearchBmsPkgNoList;
import org.yht.exps.express.model.bms.carryin.BatchCarryIn;
import org.yht.exps.express.model.bms.carryin.BmsInPlanPkgResult;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.bms.carryin.InPlanPkgList;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.disposal.SpCallVo;
import org.yht.exps.express.model.mobile.bms.BmsBigSizeScan;
import org.yht.exps.express.model.mobile.bms.BmsBigSizeScanList;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBigSizeScan;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.exps.express.service.bms.BmsInService;
import org.yht.exps.express.service.bms.send.BmsSendXmlParser;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsMobPabsService {
	
	@Autowired
	BmsMobPabsMapper bmsMobPabsMapper;
	
	@Autowired
	BmsInMapper bmsInMapper;
	
	@Autowired
	BmsCommService bmsCommService;
	@Autowired
	BmsInService bmsInService;
	
	@Autowired
	BmsSendXmlParser bmsSendXmlParser;
	
	/**
	 * 거대화물지정 - 박스번호조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public BmsInPlanPkgResult updatedBmsInBigsize(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInPlanPkgResult bmsInPlanPkgResult = new BmsInPlanPkgResult();
		BmsBigSizeScan bmsBigSizeScan = new BmsBigSizeScan();
		bmsBigSizeScan.setBigSizeYnInput("Y");
		bmsBigSizeScan.setRegisterUserId(Util.null2str(search.get("updateUserId")) );
		
		bmsInPlanPkgResult.setResultCode("A012051");
		
		InPlanPkg inPlanPkg = bmsMobPabsMapper.selectOneInPlanPkg(search);
		if(inPlanPkg == null) {
			bmsBigSizeScan.setPackingNo(Util.null2str(search.get("srchNo")));
			bmsBigSizeScan.setBlNo("");
			bmsBigSizeScan.setScanResult("무적");
			bmsMobPabsMapper.insertBigSizeScanLog(bmsBigSizeScan);
			
			bmsInPlanPkgResult.setReason("무적");
			return bmsInPlanPkgResult;
		}
		
		search.put("packingNo", inPlanPkg.getPackingNo());
		search.put("inPlanNo", inPlanPkg.getInPlanNo());
		// search.put("inCt", inPlanPkg.getInCt()); // IN_CT는 업데이트 대상이기때문에 업데이트 전 IN_CT를 입력용으로 사용 하지 않는다
		search.put("cargoReferenceNo", inPlanPkg.getCargoReferenceNo());
		search.put("inYn", inPlanPkg.getInYn());
		search.put("bigSizeYn", inPlanPkg.getBigSizeYn());
		
		bmsBigSizeScan.setPackingNo(inPlanPkg.getPackingNo());
		bmsBigSizeScan.setBlNo(inPlanPkg.getBlNo());
		
		String reason = "";
		if(inPlanPkg.getInCt() == 1  && "Y".equals(inPlanPkg.getBigSizeYn()) ) {
			bmsInPlanPkgResult.setInPlanPkg(inPlanPkg);
			
			reason = "이미지정됨";
			bmsBigSizeScan.setScanResult(reason);
			bmsMobPabsMapper.insertBigSizeScanLog(bmsBigSizeScan);
			
			bmsInPlanPkgResult.setReason(reason);
			return bmsInPlanPkgResult;
		}
		
		boolean isExistPodBigSize = false;
		
		if(inPlanPkg.getExCt() == 1 && "N".equals(inPlanPkg.getInYn()) ) {
			log.info("11111111111111PPPPPPPPPPPPP");
			if(inPlanPkg.getInCt() == 0) {
				search.put("inCt", 1);
				bmsMobPabsMapper.updateInPlanPkg(search);
				inPlanPkg = bmsMobPabsMapper.selectOneInPlanPkg(search);
			}
			
			search.put("bigSizeYn", "Y");
			bmsMobPabsMapper.updateBigSizeYnFromBmsInAndInPlan(search);
			
			inPlanPkg.setBigSizeYn("Y");
			isExistPodBigSize = true;
			
			//반입생성
			search.put("odJobNo", Util.null2str(inPlanPkg.getOdJobNo()));
			//if("N".equals(inPlanPkg.getInYn())) {
			//	search = this.insertBmsInFromInPlan(Util.decimal2int(search.get("inPlanNo")), Util.null2str(search.get("bmsWhCode")), Util.null2str(search.get("entCode")), Util.null2str(search.get("entBusinessCode")), Util.null2str(search.get("updateUserId")), search);
			//}
			//if(search.get("inNo") != null) {
			//	//반입예정 반입여부 Y
			//	search.put("inYn", "Y");
			//	bmsMobPabsMapper.updateBmsInPlanInYn(search);
			//	
			//	//반입신고
			//	InventoryIn bmsIn = new InventoryIn();
			//	bmsIn.setInNo(Util.decimal2int(search.get("inNo")));
			//	List<InventoryIn> bmsInList = new ArrayList<InventoryIn>();
			//	bmsInList.add(bmsIn);
			//	
			//	BatchCarryIn batchCarryIn = new BatchCarryIn();
			//	batchCarryIn.setEntCode(Util.null2str(search.get("entCode")));
			//	batchCarryIn.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
			//	batchCarryIn.setLogUserBusinessPrintName(Util.null2str(search.get("logUserBusinessPrintName")));
			//	batchCarryIn.setLogUserName(Util.null2str(search.get("logUserName")));
			//	batchCarryIn.setLogUserNameEng(Util.null2str(search.get("logUserNameEng")));
			//	batchCarryIn.setBmsCarryInList(bmsInList);
			//	bmsSendXmlParser.callSendXmlGOVCBR632ParserExecutor(batchCarryIn, request);
			//}
			
			reason = "완료";
			bmsInPlanPkgResult.setReason(reason);
		}else if(inPlanPkg.getExCt() >= 2 && "N".equals(inPlanPkg.getInYn()) ) {
			log.info("222222222222PPPPPPPPPPPPP");
			if(inPlanPkg.getInCt() == 0) {
				search.put("inCt", 1);
				bmsMobPabsMapper.updateInPlanPkg(search);
				inPlanPkg = bmsMobPabsMapper.selectOneInPlanPkg(search);
			}
			
			isExistPodBigSize = setBigSize(search, inPlanPkg);
			
			if(inPlanPkg.getExCt() == inPlanPkg.getInCtSum()) {
				
				//반입생성
				search.put("odJobNo", Util.null2str(inPlanPkg.getOdJobNo()));
				//if("N".equals(inPlanPkg.getInYn())) {
				//	search = this.insertBmsInFromInPlan(Util.decimal2int(search.get("inPlanNo")), Util.null2str(search.get("bmsWhCode")), Util.null2str(search.get("entCode")), Util.null2str(search.get("entBusinessCode")), Util.null2str(search.get("updateUserId")), search);
				//}	
				//if(search.get("inNo") != null) {
				//	//반입예정 반입여부 Y
				//	search.put("inYn", "Y");
				//	bmsMobPabsMapper.updateBmsInPlanInYn(search);
				//	
				//	//반입신고
				//	InventoryIn bmsIn = new InventoryIn();
				//	bmsIn.setInNo(Util.decimal2int(search.get("inNo")));
				//	List<InventoryIn> bmsInList = new ArrayList<InventoryIn>();
				//	bmsInList.add(bmsIn);
				//	
				//	BatchCarryIn batchCarryIn = new BatchCarryIn();
				//	batchCarryIn.setEntCode(Util.null2str(search.get("entCode")));
				//	batchCarryIn.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
				//	batchCarryIn.setLogUserBusinessPrintName(Util.null2str(search.get("logUserBusinessPrintName")));
				//	batchCarryIn.setLogUserName(Util.null2str(search.get("logUserName")));
				//	batchCarryIn.setLogUserNameEng(Util.null2str(search.get("logUserNameEng")));
				//	batchCarryIn.setBmsCarryInList(bmsInList);
				//	bmsSendXmlParser.callSendXmlGOVCBR632ParserExecutor(batchCarryIn, request);
				//}
			}
			
			reason = "완료";
			bmsInPlanPkgResult.setReason(reason);
		}else if(inPlanPkg.getExCt() >= 1 && "Y".equals(inPlanPkg.getInYn()) ) {
			log.info("3333333333PPPPPPPPPPPPP");
			if(inPlanPkg.getInCt() == 0) {
				search.put("inCt", 1);
				bmsMobPabsMapper.updateInPlanPkg(search);
				inPlanPkg = bmsMobPabsMapper.selectOneInPlanPkg(search);
			}
			
			isExistPodBigSize = setBigSize(search, inPlanPkg);
			setBigSizeInspectionResult(search, inPlanPkg);
			
			if(inPlanPkg.getExCt() == inPlanPkg.getInCtSum()) {
				
				//if("BMS01".equals(inPlanPkg.getLastPodStatus()) ) {
				//	//반입신고
				//	InventoryIn bmsIn = new InventoryIn();
				//	bmsIn.setInNo(inPlanPkg.getInNo()); // inPlanPkg.getInYn() Y인경우 대부분 IN_NO값을 가져올수있다
				//	List<InventoryIn> bmsInList = new ArrayList<InventoryIn>();
				//	bmsInList.add(bmsIn);
				//	
				//	BatchCarryIn batchCarryIn = new BatchCarryIn();
				//	batchCarryIn.setEntCode(Util.null2str(search.get("entCode")));
				//	batchCarryIn.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
				//	batchCarryIn.setLogUserBusinessPrintName(Util.null2str(search.get("logUserBusinessPrintName")));
				//	batchCarryIn.setLogUserName(Util.null2str(search.get("logUserName")));
				//	batchCarryIn.setLogUserNameEng(Util.null2str(search.get("logUserNameEng")));
				//	batchCarryIn.setBmsCarryInList(bmsInList);
				//	bmsSendXmlParser.callSendXmlGOVCBR632ParserExecutor(batchCarryIn, request);
				//}
			}
			
			reason = "완료";
			bmsInPlanPkgResult.setReason(reason);
		}else if(inPlanPkg.getInCt() == 0) {
			log.info("4444444444PPPPPPPPPPPPP");
			search.put("inCt", 1);
			search.put("bcrTimeUpdateYn", "Y");
			bmsMobPabsMapper.updateInPlanPkg(search);
			inPlanPkg = bmsMobPabsMapper.selectOneInPlanPkg(search);
			
			isExistPodBigSize = setBigSize(search, inPlanPkg);
			
			if(inPlanPkg.getExCt() == inPlanPkg.getInCtSum()) {
				
				//반입생성
				search.put("odJobNo", Util.null2str(inPlanPkg.getOdJobNo()));
				//if("N".equals(inPlanPkg.getInYn())) {
				//	search = this.insertBmsInFromInPlan(Util.decimal2int(search.get("inPlanNo")), Util.null2str(search.get("bmsWhCode")), Util.null2str(search.get("entCode")), Util.null2str(search.get("entBusinessCode")), Util.null2str(search.get("updateUserId")), search);
				//}
				//if(search.get("inNo") != null) {
				//	//반입예정 반입여부 Y
				//	search.put("inYn", "Y");
				//	bmsMobPabsMapper.updateBmsInPlanInYn(search);
				//	
				//	//반입신고
				//	InventoryIn bmsIn = new InventoryIn();
				//	bmsIn.setInNo(Util.decimal2int(search.get("inNo")));
				//	List<InventoryIn> bmsInList = new ArrayList<InventoryIn>();
				//	bmsInList.add(bmsIn);
				//	
				//	BatchCarryIn batchCarryIn = new BatchCarryIn();
				//	batchCarryIn.setEntCode(Util.null2str(search.get("entCode")));
				//	batchCarryIn.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
				//	batchCarryIn.setLogUserBusinessPrintName(Util.null2str(search.get("logUserBusinessPrintName")));
				//	batchCarryIn.setLogUserName(Util.null2str(search.get("logUserName")));
				//	batchCarryIn.setLogUserNameEng(Util.null2str(search.get("logUserNameEng")));
				//	batchCarryIn.setBmsCarryInList(bmsInList);
				//	bmsSendXmlParser.callSendXmlGOVCBR632ParserExecutor(batchCarryIn, request);
				//}
			}
			
			reason = "완료";
			bmsInPlanPkgResult.setReason(reason);
		}
		
		if(isExistPodBigSize) {
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(search.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_IN_PLAN);
			processLogs.setDescription(BmsConsts.DbType.BMS_IN_PLAN+" [PDA 거대여부 업데이트]");
			processLogs.setJobLog(new Gson().toJson(inPlanPkg));
			processLogs.setJobNo(Util.null2str(inPlanPkg.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, BmsConsts.PodStatus.BIGSIZE_UPDATE_Y /* pod Status 값 매핑 */
					, Util.null2str(reason)
					, null
			);
		}
		bmsBigSizeScan.setScanResult(reason);
		bmsMobPabsMapper.insertBigSizeScanLog(bmsBigSizeScan);
		
		bmsInPlanPkgResult.setInPlanPkg(inPlanPkg);
		return bmsInPlanPkgResult;
	}
	
	
	public Map<String, Object> insertBmsInFromInPlan(int inPlanNo, String bmsWhCode, String entCode, String entBusinessCode, String updateUserId, Map<String, Object> search) throws Exception {
		Map<String, Object> param = new HashMap<String, Object>();
		
		//1)  이미 반입자료 있는지 확인
		param.put("inPlanNo", inPlanNo);
		Integer nCnt = bmsMobPabsMapper.selectCountBmsInByInPlan(param);
		//2) 1에서 자료있으면 반입처리 안함
		if(nCnt >0){ 
			return param;
		} 
		
		param.put("bmsWhCode", bmsWhCode);
		param.put("entCode", entCode);
		param.put("entBusinessCode", entBusinessCode);
		param.put("updateUserId", updateUserId);
		
		SpCallVo voIn = bmsInService.getBmsInCustomsReportNo(Util.null2str(param.get("bmsWhCode")));
		param.put("customsReportNo", voIn.getRetCustomsReportNo());
		param.put("optionCodeForLocation", "A02");
		param.put("solution", "BMS");
		param.put("optionCodeForDate", "A01");
		param.put("optionCodeForLocation", "A02");
		param.put("customerCode", "");
		param.put("customerName", "");
		param.put("fwdCode", "");
		param.put("fwdName", "");
		bmsMobPabsMapper.batchInsertBmsInFromInPlan(param);
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(param.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(param.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(param.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [PDA 거대여부지정후 반입]");
		processLogs.setJobLog(new Gson().toJson(param));
		processLogs.setJobNo(Util.null2str(search.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		bmsCommService.insertLogAndPodStatus(processLogs, null
				, BmsConsts.PodStatus.BIGSIZE_UPDATE_Y /* pod Status 값 매핑 */
				, "PDA거대반입"
				, null
		);
		
		/** 거대 검사결과추가 */
		BmsInspectResult inspectResult = bmsMobPabsMapper.selectInspectResultByTypeIsBigSize(Util.decimal2int(param.get("inNo")));
		if(inspectResult == null) {
			param.put("cargoReferenceNo", Util.null2str(search.get("cargoReferenceNo")));
			bmsMobPabsMapper.insertInspectResult(param);
		}
		param.put("inspectionFinishYn", "N");
		param.put("inspectionFinishRemarks", "");
		param.put("inspectionFinishUserId", "");
		param.put("inspectionFinishDateTime", "");
		bmsMobPabsMapper.updateInspectResultByTypeIsBigSize(param);
		return param;
	}
	
	public boolean setBigSize(Map<String, Object> search, InPlanPkg inPlanPkg) {
		log.info(search.toString());
		if("".equals(Util.null2str(search.get("updateUserId")))) return false;
		if(Util.null2int(search.get("inPlanNo")) == 0) return false;
		
		search.put("bigSizeYn", "Y");
		bmsMobPabsMapper.updateBigSizeYnFromBmsInAndInPlan(search);
		
		inPlanPkg.setBigSizeYn("Y");
		
		return true;
	}
	
	public void setBigSizeInspectionResult(Map<String, Object> search, InPlanPkg inPlanPkg) {
		log.info(search.toString());
		if("".equals(Util.null2str(search.get("updateUserId")))) return;
		if("".equals(Util.null2str(search.get("cargoReferenceNo")))) return;
		if(inPlanPkg == null) return;
		if(inPlanPkg.getInNo() == null || inPlanPkg.getInNo() == 0) return;
		
		/** 거대 검사결과추가 */
		BmsInspectResult inspectResult = bmsMobPabsMapper.selectInspectResultByTypeIsBigSize(Util.decimal2int(inPlanPkg.getInNo()));
		search.put("inNo", Util.decimal2int(inPlanPkg.getInNo()));
		if(inPlanPkg.getInNo() > 0 && inspectResult == null) {
			bmsMobPabsMapper.insertInspectResult(search);
		}
		
		search.put("inspectionFinishYn", "N");
		search.put("inspectionFinishRemarks", "");
		search.put("inspectionFinishUserId", "");
		search.put("inspectionFinishDateTime", "");
		bmsMobPabsMapper.updateInspectResultByTypeIsBigSize(search);
	}
	
	/**
	 * 거대화물지정취소 - 박스번호조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInPlanPkgResult selectPkgNoInfo(SearchBmsPkgNoList search) throws Exception {
		BmsInPlanPkgResult bmsInPlanPkgResult = new BmsInPlanPkgResult();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<InPlanPkg> list = bmsMobPabsMapper.selectPkgNoInfo(search);
		bmsInPlanPkgResult.setInPlanPkgList(list);
		
		// 스캔로그를 근거로 이미 거대지정취소 여부를 체크
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("srchNo", search.getSrchNo());
		param.put("srchInPlanNo", search.getSrchInPlanNo());
		param.put("srchBlNo", search.getSrchBlNo());
		param.put("srchBigSizeYnInput", "N"); // 지정취소는 스캔로그저장시 N으로 insert되고 있음
		param.put("srchScanResult", "완료");
		Integer nCnt = bmsMobPabsMapper.selectCountBigSizeYnFromScanLog(param);
		if(nCnt > 0) {
			bmsInPlanPkgResult.setReason("이미지정취소됨");
		}
		
		return bmsInPlanPkgResult;
	}
	
	/**
	 * 거대화물지정취소 - 반입예정,반입 거대지정취소
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInPlanPkgResult updateBigSizeYn(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		BmsInPlanPkgResult result = new BmsInPlanPkgResult();
		int inPlanCnt = bmsInMapper.updateBigSizeYnFromBmsInPlan(listData);
		int inCnt = bmsInMapper.updateBigSizeYnBmsInWhereHasBmsInPlan(listData);
		int cnt = 0;
		if(inPlanCnt > 0 || inCnt > 0) {
			cnt = inPlanCnt;
		}
		
		/** 거대 검사결과추가 */
		Map<String, Object> bmsIn = bmsMobPabsMapper.selectBmsInByInPlanNo(listData);
		if(bmsIn != null) {
			Map<String, Object> updateParam = new HashMap<>();
			updateParam.put("inNo", Util.decimal2int(bmsIn.get("IN_NO")));
			updateParam.put("inspectionFinishUserId", Util.null2str(listData.get("updateUserId")));
			updateParam.put("inspectionFinishYn", "Y");
			updateParam.put("inspectionFinishRemarks", "거대지정취소로인한완료처리(PDA)");
			bmsMobPabsMapper.updateInspectResultByTypeIsBigSize(updateParam);
		}
		
		CarryIn carryIn = new CarryIn();
		carryIn.setBigSizeYn(Util.null2str(listData.get("yn")));
		carryIn.setPdaYn("Y");
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(listData.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(listData.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(listData.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN_PLAN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN_PLAN+" [PDA 거대여부 업데이트]");
		processLogs.setJobLog(new Gson().toJson(listData));
		processLogs.setJobNo(Util.null2str(listData.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(listData.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(listData.get("logUserNameEng")));
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.BIGSIZE_UPDATE_N /* pod Status 값 매핑 */
				, Util.null2str(listData.get("reason"))
				, carryIn
		);
		
		result.setSuccCnt(cnt);
		return result;
	}
	
	/**
	 * 거대화물지정취소 - 거대화물스캔로그 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsBigSizeScanList selectScanLogInfo(SearchMobBigSizeScan search) throws Exception {
		BmsBigSizeScanList bmsBigSizeScanList = new BmsBigSizeScanList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<BmsBigSizeScan> list = bmsMobPabsMapper.selectScanLogInfo(search);
		bmsBigSizeScanList.setBmsBigSizeScanList(list);
		if(list.size() > 0) {
			bmsBigSizeScanList.setTotal(list.get(0).getTotal());
		}else {
			bmsBigSizeScanList.setTotal(0);
		}
		return bmsBigSizeScanList;
	}
	
	/**
	 * 거대화물지정취소 - 거대화물스캔로그 저장
	 * @param carryIn
	 * @param request
	 * @return
	 */
	public BmsInPlanPkgResult insertBigSizeScanLog(BmsBigSizeScan bmsBigSizeScan, HttpServletRequest request) throws Exception {
		BmsInPlanPkgResult result = new BmsInPlanPkgResult();
		if(bmsMobPabsMapper.insertBigSizeScanLog(bmsBigSizeScan) > 0) {
			result.setResultCode("S");
		}else {
			result.setResultCode("F");
		}
		
		return result;
	}
	
}
