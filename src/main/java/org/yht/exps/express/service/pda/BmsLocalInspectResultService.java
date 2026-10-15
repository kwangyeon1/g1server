package org.yht.exps.express.service.pda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsInMapper;
import org.yht.exps.express.mapper.pda.BmsLocalInspectResultMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.carryin.BatchCarryIn;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.CarryInSaveResult;
import org.yht.exps.express.model.bms.carryin.WithHolding;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.model.bms.disposal.SpCallVo;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.exps.express.model.mobile.bms.BmsInForPda;
import org.yht.exps.express.model.mobile.bms.BmsLocalInspectResult;
import org.yht.exps.express.model.mobile.bms.BmsLocalInspectResultList;
import org.yht.exps.express.model.mobile.bms.LocalInspectRstSaveResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMblFromInspectResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMobInspectResult;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.exps.express.service.bms.send.BmsSendXmlParser;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsLocalInspectResultService {
	
	@Autowired
	BmsInMapper bmsInMapper;
	
	@Autowired
	BmsLocalInspectResultMapper bmsLocalInspectResultMapper;

	@Autowired
	CommonService commonService;
	
	@Autowired
	BmsCommService bmsCommService ;

	@Autowired
	BmsSendXmlParser bmsSendXmlParser;

	public LocalInspectRstSaveResult updateTlcOutOfInspectResultByBlNo(SearchMobInspectResult search, HttpServletRequest request) throws Exception {
		LocalInspectRstSaveResult result = new LocalInspectRstSaveResult();
		
		List<BmsOut> bmsOutList = bmsLocalInspectResultMapper.selectBmsOutByBlNo(search);
		if(bmsOutList == null || bmsOutList.isEmpty()){
			result.setResultCode("A11481");
			return result;
		}
		
		BmsOut bmsOut = bmsOutList.get(0);
		search.setInNo(String.valueOf(bmsOut.getInNo()));
		BmsLocalInspectResult localInspect = bmsLocalInspectResultMapper.selectBmsLocalInspectResultByBlNo(search);
		
		result.setBlNo(search.getBlNo());
		result.setCarryoutDate(bmsOut.getCarryoutDate());
		result.setOutCt(String.valueOf(bmsOut.getOutCt()));
		result.setUpdateYn("N");
		result.setResultCode("A012051");
		if(localInspect == null) {
			result.setReason("이고대상아님");
			return result;
		}
		
		if(localInspect != null && !"BMS04".equals(bmsOut.getLastPodStatus())) {
			result.setReason(bmsOut.getLastPodStatus());
			return result;
		}
		
		result.setBlNo(search.getBlNo());
		result.setCarryoutDate(bmsOut.getCarryoutDate());
		result.setOutCt(String.valueOf(bmsOut.getOutCt()));
		result.setUpdateYn("Y");
		result.setResultCode("A012051");
		if(!"".equals(localInspect.getTicOutScanUserId())) {
			return result;
		}
		bmsLocalInspectResultMapper.updateBmsLocalInspectResult(search);
		localInspect.setTicOutScanUserId(search.getUpdateUserId()); // 해당값의 유무로 podName 종류를 구분하므로 update값을 set해준다
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(search.getUserBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.getUpdateUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_LOCAL_INSPECT_RESULT);
		processLogs.setDescription(BmsConsts.DbType.BMS_LOCAL_INSPECT_RESULT+" [입력 BL NO:"+ search.getBlNo() +"]");
		processLogs.setJobNo(String.valueOf(bmsOut.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setResult("ok");
		processLogs.setProcessUserName(search.getLogUserName());
		processLogs.setProcessUserNameEng(search.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
				, ""
				, localInspect
		);
		
		return result;
	}
	
	public LocalInspectRstSaveResult updateTicComeBackOfInspectResultByBlNo(SearchMobInspectResult search, HttpServletRequest request) throws Exception {
		LocalInspectRstSaveResult result = new LocalInspectRstSaveResult();
		
		BmsLocalInspectResult localInspect = bmsLocalInspectResultMapper.selectTicData(search);
		
		result.setBlNo(search.getBlNo());
		result.setUpdateYn("N");
		result.setResultCode("A012051");
		if(localInspect == null || localInspect.getCt() == 0) {			
			result.setReason("이고대상아님");
			return result;
		}
		
		result.setCarryinDate(localInspect.getCarryinDate());
		result.setCt(localInspect.getCt());
		result.setUpdateYn("Y");
		result.setResultCode("A012051");
		if(!"".equals(localInspect.getTicComeBackScanUserId())) {
			return result;
		}
		bmsLocalInspectResultMapper.updateBmsTicComebackLocalInspectResult(search);
		localInspect.setTicComeBackScanUserId(search.getUpdateUserId()); // 해당값의 유무로 podName 종류를 구분하므로 update값을 set해준다
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(search.getUserBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.getUpdateUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_LOCAL_INSPECT_RESULT);
		processLogs.setDescription(BmsConsts.DbType.BMS_LOCAL_INSPECT_RESULT+" [입력 BL NO:"+ search.getBlNo() +"]");
		processLogs.setJobNo(String.valueOf(localInspect.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setResult("ok");
		processLogs.setProcessUserName(search.getLogUserName());
		processLogs.setProcessUserNameEng(search.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
				, ""
				, localInspect
		);
		
		return result;
	}

	public LocalInspectRstSaveResult updateOutScanDate(SearchMobInspectResult search, HttpServletRequest request) throws Exception {
		LocalInspectRstSaveResult result = new LocalInspectRstSaveResult();
		
		BmsInForPda bmsInForPda = bmsLocalInspectResultMapper.selectBmsInDetailByBlNo(search);
		result.setBlNo(search.getBlNo());
		result.setUpdateYn("N");
		result.setResultCode("A012051");
		if( bmsInForPda == null) {			
			result.setReason("미반입");
			return result;
		}
		
		BmsOutPlan bmsOutPlan = bmsLocalInspectResultMapper.selectOneBmsOutPlan(search);
		if( bmsOutPlan == null) {			
			result.setReason("미승인");
			return result;
		}
		
		if( !"".equals(bmsOutPlan.getOutScanDateTime()) && !"1900-01-01 00:00:00.0".equals(bmsOutPlan.getOutScanDateTime())) {
			result.setDeliveryName(bmsInForPda.getDeliveryName());
			result.setApprovalCt(bmsOutPlan.getApprovalCt());
			result.setUpdateYn("Y");
			result.setReason("출고됨");
			return result;
		}
		
		/*if( bmsInForPda.getStockCt() == 0) {			
			result.setReason("재고없음");
			return result;
		}*/
		
		if("Y".equals(bmsInForPda.getCarryoutStopYn()) && "N".equals(bmsInForPda.getHoldReleaseYn()) ) {
			result.setReason("홀드");
			return result;
		}
		
		if( "1".equals(bmsOutPlan.getApprovalClass())) {			
			result.setReason("승인취하");
			return result;
		}
		
		BmsLocalInspectResult localInspect = bmsLocalInspectResultMapper.selectBmsLocalInspectResultByBlNo(search);
		if( localInspect != null && !"Y".equals(localInspect.getInspectionFinishYn())) {			
			result.setReason("미검사");
			return result;
		}
		
		result.setDeliveryName(bmsInForPda.getDeliveryName());
		result.setApprovalCt(bmsOutPlan.getApprovalCt());
		result.setUpdateYn("Y");
		
		bmsLocalInspectResultMapper.updateScanDateBmsOutPlan(bmsOutPlan.getOutPlanNo());
		bmsOutPlan.setOutScanDateTime(DateTimeUtil.getNow()); // 해당값의 유무로 podName 종류를 구분하므로 update값을 set해준다
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(search.getUserBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.getUpdateUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_OUT_PLAN);
		processLogs.setDescription(BmsConsts.DbType.BMS_OUT_PLAN+" [입력 BL_NO :" + search.getBlNo() + ", IN_NO :"+ search.getInNo() +"]");
		processLogs.setJobNo(String.valueOf(bmsInForPda.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setResult("ok");
		processLogs.setProcessUserName(search.getLogUserName());
		processLogs.setProcessUserNameEng(search.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
				, ""
				, bmsOutPlan
		);
		
		return result;
	}
	
	public BmsLocalInspectResultList selectMblList() throws Exception {
		BmsLocalInspectResultList resultList = new BmsLocalInspectResultList();
		List<BmsLocalInspectResult> list = bmsLocalInspectResultMapper.selectMblList();
		if (list == null || list.isEmpty()) {
			return resultList;
		}
		
		Set<String> seen = new HashSet<>();
        list.removeIf(result -> !seen.add(result.getMblNo()));
        
		resultList.setBmsLocalInspectResultList(list);
		
		return resultList;
	}
	
	public BmsLocalInspectResultList selectInspectResultFilteredByCodeGroup(SearchMblFromInspectResult search) throws Exception {
		BmsLocalInspectResultList resultList = new BmsLocalInspectResultList();
		resultList.setBmsLocalInspectResultList(bmsLocalInspectResultMapper.selectInspectResultFilteredByCodeGroup(search));
		
		return resultList;
	}
	
	public LocalInspectRstSaveResult updateInspectResultSetFinish(SearchMblFromInspectResult search, HttpServletRequest request) throws Exception {
		LocalInspectRstSaveResult result = new LocalInspectRstSaveResult();
		
		List<BmsLocalInspectResult> list = search.getUpdateInfoList();
		if (list == null || list.isEmpty()) {
			return result;
		}
		
		// 이미 검사한것들은 검사목록 list에서 제외한다
		list.removeIf(inspectResult -> "Y".equals(inspectResult.getInspectionYn()));
		if (list == null || list.isEmpty()) {
			return result;
		}
		
		for(BmsLocalInspectResult inspectResult : list) {
			inspectResult.setFinishUserId(search.getUpdateUserId());
			bmsLocalInspectResultMapper.updateInspectResultByInspectionKeyword(inspectResult);
		}
		
		List<BmsLocalInspectResult> resultList = bmsLocalInspectResultMapper.getSelectedInspectResultList(list);
		
		for(BmsLocalInspectResult inspectResult : resultList) {
			if(inspectResult.isSuccess()) {
				Map<String, Object> paramWithInNoAndYn = new HashMap<String, Object>();
				paramWithInNoAndYn.put("inNo", inspectResult.getInNo());
				paramWithInNoAndYn.put("yn", "Y");
				bmsLocalInspectResultMapper.updateInspectionFinishedYn(paramWithInNoAndYn);
				inspectResult.setInspectionFinishYn("Y"); // 해당값의 유무로 podName 종류를 구분하므로 update값을 set해준다
				
				CarryIn bmsIn = bmsLocalInspectResultMapper.selectBmsInByInNo(paramWithInNoAndYn);
				
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(search.getEntCode()));
				processLogs.setEntBusinessCode(Util.null2str(search.getUserBusinessCode()));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(search.getUpdateUserId()));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
				processLogs.setDbType(BmsConsts.DbType.BMS_LOCAL_INSPECT_RESULT);
				processLogs.setDescription(BmsConsts.DbType.BMS_LOCAL_INSPECT_RESULT+" [ IN_NO :"+ inspectResult.getInNo() +"]");
				processLogs.setJobNo(Util.null2str(bmsIn.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setJobLog(new Gson().toJson(search));
				processLogs.setResult("ok");
				processLogs.setProcessUserName(search.getLogUserName());
				processLogs.setProcessUserNameEng(search.getLogUserNameEng());
				commonService.insertLog(processLogs, request);
				
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
						, ""
						, inspectResult
				);
			}
		}
		
		result.setResultCode("A012051");
		return result;
	}
	
	/**
	 * 관세청 XML 송신(Send) - GOVCBR632(반입신고서)
	 * @param params
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CarryInSaveResult sendOutToXml(BatchCarryIn params, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		String batchExecType = params.getBatchExecType();

		if("SEND".equals(batchExecType)) {
			result = bmsSendXmlParser.callSendXmlGOVCBR632ParserExecutor(params, request);
		}

		return result;
	}
	/**
	 * 반입신청 - 반입신고번호 생성
	 * @param bmsWhCode
	 * @return
	 */
	public SpCallVo getBmsInCustomsReportNo(String bmsWhCode) {
		SpCallVo voIn = new SpCallVo();
		voIn.setMode("M"); // 	M: Manual A: Auto
		voIn.setBmsWhCode(bmsWhCode);
		voIn.setInOut("I"); // I.TB_BMS_IN, O.TB_BMS_OUT
		// 0 18자리 CUSTOMS_REPORT_NO 시퀀스 작성
		bmsInMapper.callProcBMSInSeq(voIn);
		return voIn;
	}


	public List<WithHolding> selectWithholdingList(SearchInList search) {
		List<WithHolding> list = new ArrayList<>();
		list =bmsInMapper.selectWithholdingList(search);
		return list;
	}
}
