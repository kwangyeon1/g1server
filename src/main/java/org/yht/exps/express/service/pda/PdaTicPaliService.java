package org.yht.exps.express.service.pda;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.pda.PdaTicPaliMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.BmsInspectResultRst;
import org.yht.exps.express.model.bms.carryin.BmsTicMoveScan;
import org.yht.exps.express.model.bms.carryin.CarryInRst;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.exps.express.service.bms.BmsSendUtils;
import org.yht.exps.express.service.bms.send.BmsSendXmlParser;
import org.yht.util.Util;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.out.BatchBmsOut;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutSaveResult;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo.CargCsclPrgsInfoDtail;
import org.yht.exps.express.service.bms.BmsOutFtpSendService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PdaTicPaliService {

	@Autowired
	CommonMapper commonMapper;

	@Autowired
	PdaTicPaliMapper pdaTicPaliMapper;

	@Autowired
	BmsCommService bmsCommService;
	
	@Autowired
	BmsOutFtpSendService bmsOutFtpSendService;
	
	@Autowired
    BmsSendUtils bmsSendUtils;

	@Autowired
	BmsSendXmlParser bmsSendXmlParser;

	public BmsInspectResultRst selectBmsInRelatedTicBmsOut(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst carryInRst = new BmsInspectResultRst();
		
		List<BmsInspectResult> bmsInspectResultList = pdaTicPaliMapper.selectBmsInRelatedTicBmsOut(search);
		for(BmsInspectResult bmsInspectResult : bmsInspectResultList) {
			String ticOutStatus = "";
			
			// 반출조회시 LAST_POD_STATUS가 "BMSA4" OR TIC_OUT_APPROVAL_NO_50가 공백이 아닌인경우, TIC_OUT_STATUS는 "실물이고대상" 출력
			BmsOut bmsOut =pdaTicPaliMapper.selectBmsOutByOutNo(bmsInspectResult.getOutNo());
			if("BMSA4".equals(bmsOut.getLastPodStatus())){
				ticOutStatus = "실물이고대상";
				bmsInspectResult.setTicOutStatus(ticOutStatus);
				continue;
			}
			
			//unipass 목록통관완료 TIC_OUT_STATUS는 "목록반출" OR "이고반출" OR "실물이고대상" 출력
			ticOutStatus = getUnipassTicOutResultStr(bmsInspectResult.getCargoReferenceNo(), Util.null2str(search.get("bmsWhCode")) );
			if(!"".equals(ticOutStatus)) {
				bmsInspectResult.setTicOutStatus(ticOutStatus);
				continue;
			}
			
			// 위에 전부 해당하지 않는경우는 TIC_OUT_STATUS는 "미이고반출" 출력
			ticOutStatus = "미이고반출";
			bmsInspectResult.setTicOutStatus(ticOutStatus);
		}
		carryInRst.setBmsInspectResultList(bmsInspectResultList);
		
		carryInRst.setResultCode("A012051");
		carryInRst.setReason("완료");
		
		return carryInRst;
	}
	
	public BmsInspectResultRst selectBoxRelatedTicBmsOut(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst carryInRst = new BmsInspectResultRst();
		
		List<BmsInspectResult> bmsInspectResultList = pdaTicPaliMapper.selectBoxRelatedTicBmsOut(search);
		for(BmsInspectResult bmsInspectResult : bmsInspectResultList) {
			String ticOutStatus = "";
			
			if(0 != Util.null2int(bmsInspectResult.getBmsOutCt()) && Util.null2int(bmsInspectResult.getCt()) != Util.null2int(bmsInspectResult.getBmsOutCt())) {
				bmsInspectResult.setOtherOutPlan("분할승인");
			}
			// 반출조회시 LAST_POD_STATUS가 "BMSA4" OR TIC_OUT_APPROVAL_NO_50가 공백이 아닌인경우, TIC_OUT_STATUS는 "실물이고대상" 출력
			BmsOut bmsOut =pdaTicPaliMapper.selectBmsOutByOutNo(bmsInspectResult.getOutNo());
			if("BMSA4".equals(bmsOut.getLastPodStatus())){
				ticOutStatus = "실물이고대상";
				bmsInspectResult.setTicOutStatus(ticOutStatus);
				continue;
			}
			
			//unipass 목록통관완료 TIC_OUT_STATUS는 "목록반출" OR "이고반출" OR "실물이고대상" 출력
//			ticOutStatus = getUnipassTicOutResultStr(bmsInspectResult.getCargoReferenceNo(), Util.null2str(search.get("bmsWhCode")) );
//			if(!"".equals(ticOutStatus)) {
//				bmsInspectResult.setTicOutStatus(ticOutStatus);
//				continue;
//			}
//			
//			// 위에 전부 해당하지 않는경우는 TIC_OUT_STATUS는 "미이고반출" 출력
//			ticOutStatus = "미이고반출";
			bmsInspectResult.setTicOutStatus(ticOutStatus);
		}
		carryInRst.setBmsInspectResultList(bmsInspectResultList);
		
		carryInRst.setResultCode("A012051");
		carryInRst.setReason("완료");
		
		return carryInRst;
	}
	
	/** 이고반출확정 */
	public CarryInRst selectBmsOutTicOutScanPicking(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInRst carryInRst = new CarryInRst();
		
		List<InPlanPkg> inPlanPkgList = pdaTicPaliMapper.selectBmsOutTicOutScanPicking(search);
		carryInRst.setInPlanPkgList(inPlanPkgList);
		
		return carryInRst;
	}
	
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public CarryInRst updateBmsOutTicOutScanStatus(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInRst carryInRst = new CarryInRst();
		Map<String, Object> logMap = new HashMap<String, Object>();
		List<BmsOut> outLst = new ArrayList<>();
		
		logMap.put("scanNo", Util.null2str(search.get("srchPackingNo")));
		logMap.put("outBackUserId", Util.null2str(search.get("registerUserId")));
		logMap.put("scanDate", Util.null2str(search.get("currentDate")));
		logMap.put("outBackInFinishYn", "N");
		logMap.put("registerUserId", Util.null2str(search.get("registerUserId")));
		
		if("Y".equals(Util.null2str(search.get("unManifested")))) {
			carryInRst.setReason("무적");
			logMap.put("outNo", 0);
			logMap.put("inNo", 0);
			logMap.put("blNo", "");
			logMap.put("outCt", 0);
			logMap.put("outBackStatus", "");
			logMap.put("scanResult", Util.null2str(carryInRst.getReason()));
			pdaTicPaliMapper.insertBmsTicMoveScanLog(logMap);
			return carryInRst;
		} else {
			List<InPlanPkg> bmsTicOutList = pdaTicPaliMapper.selectBmsOutTicOutScanPicking(search);
			
			if(bmsTicOutList.size() > 0) { /** 반출대상이면 */
				carryInRst.setInPlanPkgList(bmsTicOutList);
				// 스캔로그 파라미터 set
				logMap.put("outNo", bmsTicOutList.get(0).getOutNo());
				logMap.put("inNo", bmsTicOutList.get(0).getInNo());
				logMap.put("blNo", bmsTicOutList.get(0).getBlNo());
				logMap.put("outCt", bmsTicOutList.get(0).getOutCt());
				
				if(bmsTicOutList.get(0).getMoveInCt() == 1) {
					carryInRst.setReason("이미이고반입됨");
					logMap.put("outBackStatus", "");
					logMap.put("scanResult", Util.null2str(carryInRst.getReason()));
					pdaTicPaliMapper.insertBmsTicMoveScanLog(logMap);
					return carryInRst;
				}
				if(bmsTicOutList.get(0).getMoveOutCt() == 1) {
					carryInRst.setReason("이미이고반출됨");
					logMap.put("outBackStatus", "");
					logMap.put("scanResult", Util.null2str(carryInRst.getReason()));
					pdaTicPaliMapper.insertBmsTicMoveScanLog(logMap);
					return carryInRst;
				}
				// 반출신고진행상태가 접수, 수입통관이고인 경우 -> 결과 완료
				if("BMS04".equals(bmsTicOutList.get(0).getLastPodStatus()) || "BMSA4".equals(bmsTicOutList.get(0).getLastPodStatus())) {
					carryInRst.setReason("완료");

					Map<String, Object> pkgCheckScanParam = new HashMap<String, Object>();
					pkgCheckScanParam.put("inPlanNo", Util.null2int(search.get("srchInPlanNo")));
					pkgCheckScanParam.put("packingNo", Util.null2str(search.get("srchPackingNo")));
					pkgCheckScanParam.put("blNo", Util.null2str(search.get("srchBlNo")));
					pkgCheckScanParam.put("ticStatus", "U");
					pkgCheckScanParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
					pdaTicPaliMapper.updatePkgTicStatus(pkgCheckScanParam);

					/** 스캔로그 저장 */
					if(bmsTicOutList.get(0).getOutCt() == 1) {
						logMap.put("outBackStatus", "U"); // 이고반출구분 U(상차)
					} else if(bmsTicOutList.get(0).getOutCt() > 1) {
						logMap.put("outBackStatus", "T"); // 이고반출구분 T(상차대기)
					}
					logMap.put("scanResult", Util.null2str(carryInRst.getReason()));
					int dupCnt = pdaTicPaliMapper.countDuplicatedTicMoveScanLog(logMap);
					if(dupCnt == 0) {
						pdaTicPaliMapper.insertBmsTicMoveScanLog(logMap);
					}
					
					/** 스캔로그 저장후 진행 */
					if(bmsTicOutList.get(0).getOutCt() == 1) {
						carryInRst.setResultCode("A012051");
						
					} else if(bmsTicOutList.get(0).getOutCt() > 1) {
						// TB_BMS_TIC_MOVE_SCAN 다시 읽음
						search.put("srchOutBackStatus", "T");
						Integer nCnt = pdaTicPaliMapper.checkBmsPkgAndTicMoveScanCount(search);
						// 위 수량과 CT가 같은경우
						if(nCnt == bmsTicOutList.get(0).getOutCt()) {
							search.put("outBackStatus", "U"); // 이고반출구분 U(상차)로 update
							pdaTicPaliMapper.updateBmsTicMoveOutScanStatus(search);
							carryInRst.setResultCode("A012051");
						}
						// CT가 같지않은경우 화면변화없음
					}
					
					if("A012051".equals(carryInRst.getResultCode())) {
						// unipass 조회해서 반출에 수입신고번호 저장
	            		CargoProgressInfo cpInfo = bmsSendUtils.getCargoProgressInfo(bmsTicOutList.get(0).getCargoReferenceNo());
	            		if(cpInfo != null && cpInfo.getCargCsclPrgsInfoDtails() != null) {
	            			Map<String, Object> outParamMap = new HashMap<String, Object>();
	            			if(cpInfo.getCargCsclPrgsInfoDtails().size() > 0) {
	            				for(CargCsclPrgsInfoDtail ccpid : cpInfo.getCargCsclPrgsInfoDtails()) {
	            					// 보세구역번호가 일치하고, 관세청 api 처리구분이 수입신고인 경우
	            					if(Util.null2str(search.get("bmsWhCode")).equals(ccpid.getShedSgn()) && "수입신고".equals(ccpid.getCargTrcnRelaBsopTpcd())) {
	            						outParamMap.put("outNo", bmsTicOutList.get(0).getOutNo());
	            						outParamMap.put("dclrNo", ccpid.getDclrNo()); // 수입신고번호
	            						pdaTicPaliMapper.updateTicOutApprovalNo50FromBmsOut(outParamMap);
	            					}
	            				}
	            			}
	            		}
						
	            		//이고반출수량 MOVE OUT CT update
						pdaTicPaliMapper.updateBmsInPlanPkgMoveOutCt(search);
						
						Map<String, Object> outScanParam = new HashMap<String, Object>();
						outScanParam.put("outNo", bmsTicOutList.get(0).getOutNo());
						pdaTicPaliMapper.updateOutScanDateFromBmsOut(outScanParam);
						
						/** TIC_OUT_FTP_SEND_YN (TIC이고반출FTP전송여부) 가 'N' 인경우 FTP 예정정보 송신 */
						if("N".equals(bmsTicOutList.get(0).getTicOutFtpSendYn())) {
							BatchBmsOut batchBmsOut = new BatchBmsOut();
							batchBmsOut.setEntCode(Util.null2str(search.get("entCode")));
							batchBmsOut.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
							batchBmsOut.setUserLanguage(Util.null2str(search.get("userLanguage")));
							batchBmsOut.setInputUserId(Util.null2str(search.get("inputUserId")));
							BmsOut out = new BmsOut();
							out.setOutNo(bmsTicOutList.get(0).getOutNo());
							outLst.add(out);
							batchBmsOut.setBmsOutList(outLst);
							
							List<String> errList = new ArrayList<String>();

							/* 관세청 반출 전송 */
							if( "BMS01".equals(bmsTicOutList.get(0).getLastPodStatus()) ) { // 입력일 경우 관세청 반출 전송
								//BmsOutSaveResult bosr6NB = bmsSendXmlParser.callSendXmlGOVCBR6NBParserExecutor(batchBmsOut, request);
								//errList.add(bosr6NB.getErrorMsg());
							} else if( "BMSA4".equals(bmsTicOutList.get(0).getLastPodStatus()) ) { // 수입통관이고일 경우 데이터 생성 구분 'D'
								batchBmsOut.setTicOutDataCreateGbn("D");
							}

							BmsOutSaveResult bosr = bmsOutFtpSendService.sendOutFtpData(batchBmsOut);
							for(String err :bosr.getErrList()) {
								errList.add(err);
							}
							carryInRst.setResultCnt(bosr.getResultCnt());
							carryInRst.setErrList(errList);
						}
					}
					
					/** pod 저장 */
					CarryIn carryIn = new CarryIn();
					carryIn.setPdaYn("Y");
					String remark = "("+ Util.null2str(bmsTicOutList.get(0).getPackingNo()) +")";
					
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(search.get("entCode")));
					processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
					processLogs.setLogJobType("DATA");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
					processLogs.setDbType(BmsConsts.DbType.BMS_TIC_PALI);
					processLogs.setDescription(BmsConsts.DbType.BMS_TIC_PALI+" [PDA 이고반출확정 업데이트]");
					processLogs.setJobLog(new Gson().toJson(search));
					processLogs.setJobNo(Util.null2str(bmsTicOutList.get(0).getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
					processLogs.setResult("ok");
					processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
					processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
					bmsCommService.insertLogAndPodStatus(processLogs, request
							, BmsConsts.PodStatus.BMS_TIC_PALI_CFM /* pod Status 값 매핑 */
							, remark // 비고 (# 박스번호)
							, carryIn
							);
					
				} else if("BMS01".equals(bmsTicOutList.get(0).getLastPodStatus())) { // 입력인경우 -> 미전송
					carryInRst.setReason("미전송");
				} else if("BMS02".equals(bmsTicOutList.get(0).getLastPodStatus())) { // 송신실패인경우 -> 송신실패
					carryInRst.setReason("송신실패");
				} else if("BMS03".equals(bmsTicOutList.get(0).getLastPodStatus())) { // 송신인경우 -> 송신후대기
					carryInRst.setReason("송신후대기");
				} else if("BMS05".equals(bmsTicOutList.get(0).getLastPodStatus())) { // 오류인경우 -> 오류통보
					carryInRst.setReason("오류통보");
				} else if("BMSA3".equals(bmsTicOutList.get(0).getLastPodStatus())) { // 통관후입고인경우 -> 확인필요
					carryInRst.setReason("확인필요");
				}
				
				// 완료의 경우에는 위에서 이미 스캔로그저장
				if(!"완료".equals(carryInRst.getReason())) {
					logMap.put("outBackStatus", "");
					logMap.put("scanResult", Util.null2str(carryInRst.getReason()));
					pdaTicPaliMapper.insertBmsTicMoveScanLog(logMap);
				}
				
			} else {
				int completedOutScanCount = pdaTicPaliMapper.selectCompletedOutScanCountByPackingNo(search);
				if(completedOutScanCount > 0) {
					carryInRst.setReason("이미출고됨");
				}else {
					Map<String, Object> ticStatusMap = pdaTicPaliMapper.selectOneBmsInForNoOutStatus(search);
					if("U".equals(Util.null2str(ticStatusMap.get("TIC_STATUS")))) {
						carryInRst.setReason("반출확정완료");
					}
				}
				logMap.put("outNo", 0);
				logMap.put("outCt", 0);
				logMap.put("outBackStatus", "");
				logMap.put("scanResult", Util.null2str(carryInRst.getReason()));
				
				// 반출없는경우 박스번호로 반입 조회
				List<InPlanPkg> inPlanPkgList = pdaTicPaliMapper.selectOneBmsInForNoOut(search);
				if(inPlanPkgList.size() > 0) {
					logMap.put("inNo", inPlanPkgList.get(0).getInNo());
					logMap.put("blNo", inPlanPkgList.get(0).getBlNo());
					carryInRst.setInPlanPkgList(inPlanPkgList);
				} else {
					logMap.put("inNo", 0);
					logMap.put("blNo", "");
				}
				pdaTicPaliMapper.insertBmsTicMoveScanLog(logMap);
			}
		}
		
		if(carryInRst.getReason() == null) carryInRst.setReason("");
		
		return carryInRst;
	}


	/**
	 * scan: 이고반입확인 - 박스에대한 로그 생성 (이고 로그-하차_대기 생성)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInspectResultRst updateFinishTicMoveScan(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInspectResultRst = new BmsInspectResultRst();
		
		bmsInspectResultRst.setResultCode("A012051");
		String reason = "";
		
		if( "Y".equals(Util.null2str(search.get("unManifested"))) ) {
			reason = "무적";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("inNo", 0);
			logParamg.put("outNo", 0);
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("blNo", "");
			logParamg.put("outCt", 0);
			logParamg.put("result", reason);
			logParamg.put("updateUserId", Util.null2str(search.get("updateUserId")));
			pdaTicPaliMapper.insertTicMoveScanLog(logParamg);
			
			bmsInspectResultRst.setReason(reason);
			return bmsInspectResultRst;
		}
		
		BmsInspectResult bmsInspectResult = pdaTicPaliMapper.selectBmsInRelatedTicBmsOutByPackingNo(search);
		if(bmsInspectResult == null) {
			BmsInspectResult ticBmsOutspectResult = pdaTicPaliMapper.selectTicBmsOutRelatedByPackingNo(search);
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("inNo", 0);
			logParamg.put("outNo", 0);
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("blNo", "");
			logParamg.put("outCt", 0);
			logParamg.put("updateUserId", Util.null2str(search.get("updateUserId")));
			if(ticBmsOutspectResult == null) {
				reason = "이고반입대상아님";
			}else {
				reason = "이고반출출고처리확인";
				logParamg.put("inNo", ticBmsOutspectResult.getInNo());
				logParamg.put("outNo", ticBmsOutspectResult.getOutNo());
				logParamg.put("blNo", ticBmsOutspectResult.getBlNo());
				logParamg.put("outCt", ticBmsOutspectResult.getBmsOutCt());
			}
			logParamg.put("result", reason);
			
			pdaTicPaliMapper.insertTicMoveScanLog(logParamg);
			
			bmsInspectResultRst.setReason(reason);
			return bmsInspectResultRst;
		}
		
		if(bmsInspectResult.getCt() == 1) {
			reason = "완료";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("inNo", bmsInspectResult.getInNo());
			logParamg.put("outNo", bmsInspectResult.getOutNo());
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("blNo", bmsInspectResult.getBlNo());
			logParamg.put("outCt", bmsInspectResult.getBmsOutCt());
			logParamg.put("outBackStatus", "C"); // 하차 입고
			logParamg.put("result", reason);
			logParamg.put("updateUserId", Util.null2str(search.get("updateUserId")));
			pdaTicPaliMapper.insertTicMoveScanLog(logParamg);
			
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(search.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_TIC_MOVE_SCAN);
			processLogs.setDescription(BmsConsts.DbType.BMS_TIC_MOVE_SCAN+" [PDA 이고반입 확인 업데이트]");
			processLogs.setJobLog(new Gson().toJson(search));
			processLogs.setJobNo(Util.null2str(bmsInspectResult.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, BmsConsts.PodStatus.PDA_TIC_IN_CHECK /* pod Status 값 매핑 */
					, ""
					, null
			);
			
			bmsInspectResultRst.setBmsInspectResult(bmsInspectResult);
			bmsInspectResultRst.setReason(reason);
			bmsInspectResultRst.setResultAction("검사완료");
			return bmsInspectResultRst;
		}
		
		reason = "완료";
		
		Map<String, Object> waitLogParam = new HashMap<String, Object>();
		waitLogParam.put("inNo", bmsInspectResult.getInNo());
		waitLogParam.put("blNo", bmsInspectResult.getBlNo());
		waitLogParam.put("packingNo", Util.null2str(search.get("scanPackingNo")));
		Map<String, Object> ticMoveScanLog = pdaTicPaliMapper.selectWaitPkgFromTicMoveScanLog(waitLogParam);
		if(ticMoveScanLog == null) { // 중복저장을 방지하기위해
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("inNo", bmsInspectResult.getInNo());
			logParamg.put("outNo", bmsInspectResult.getOutNo());
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("blNo", bmsInspectResult.getBlNo());
			logParamg.put("outCt", bmsInspectResult.getBmsOutCt());
			logParamg.put("outBackStatus", "C"); // 하차 입고
			logParamg.put("result", reason);
			logParamg.put("updateUserId", Util.null2str(search.get("updateUserId")));
			pdaTicPaliMapper.insertTicMoveScanLog(logParamg);
			
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(search.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_TIC_MOVE_SCAN);
			processLogs.setDescription(BmsConsts.DbType.BMS_TIC_MOVE_SCAN+" [PDA 이고반입 확인 업데이트]");
			processLogs.setJobLog(new Gson().toJson(search));
			processLogs.setJobNo(Util.null2str(bmsInspectResult.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, BmsConsts.PodStatus.PDA_TIC_IN_CHECK /* pod Status 값 매핑 */
					, ""
					, null
			);
		}
		
		search.put("inNo", bmsInspectResult.getInNo());
		int waitPkgCnt = pdaTicPaliMapper.countWaitPkgFromTicMoveScanLog(search);
		if(bmsInspectResult.getCt().intValue() !=  waitPkgCnt) {
			bmsInspectResultRst.setBmsInspectResult(bmsInspectResult);
			bmsInspectResultRst.setReason(reason);
			return bmsInspectResultRst;
		}
		
		
		reason = "완료";
		
//		Map<String, Object> finishUpdateParam = new HashMap<String, Object>();
//		finishUpdateParam.put("inNo", bmsInspectResult.getInNo());
//		finishUpdateParam.put("result", reason);
//		finishUpdateParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
//		finishUpdateParam.put("outBackStatus", "D"); // 하차 입고
//		finishUpdateParam.put("outBackInFinishYn", "S"); // 이고반입생성 대기
//		pdaTicPaliMapper.updateFinishTicMoveScanLogByInNo(finishUpdateParam);
//		
//		Map<String, Object> pkgUpdateParam = new HashMap<String, Object>();
//		pkgUpdateParam.put("inPlanNo", bmsInspectResult.getInPlanNo());
//		pkgUpdateParam.put("moveInCt", 1);
//		pkgUpdateParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
//		pdaTicPaliMapper.updatePkgTicMoveInByInPlanNo(pkgUpdateParam);
		
		bmsInspectResultRst.setBmsInspectResult(bmsInspectResult);
		bmsInspectResultRst.setReason(reason);
		bmsInspectResultRst.setResultAction("검사완료");
		return bmsInspectResultRst;
	}
	
	/**
	 * scan: (박스단위)이고반입확인 - 박스에대한 로그 생성 (이고 로그-하차_대기 생성)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInspectResultRst updateBoxFinishTicMoveScan(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInspectResultRst = new BmsInspectResultRst();
		
		bmsInspectResultRst.setResultCode("A012051");
		String reason = "";
		
		if( "Y".equals(Util.null2str(search.get("unManifested"))) ) {
			reason = "무적";
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("inNo", 0);
			logParamg.put("outNo", 0);
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("blNo", "");
			logParamg.put("outCt", 0);
			logParamg.put("result", reason);
			logParamg.put("updateUserId", Util.null2str(search.get("updateUserId")));
			pdaTicPaliMapper.insertTicMoveScanLog(logParamg);
			
			bmsInspectResultRst.setReason(reason);
			return bmsInspectResultRst;
		}
		
		BmsInspectResult bmsInspectResult = pdaTicPaliMapper.selectBoxRelatedTicBmsOutByPackingNo(search);
		if(bmsInspectResult == null) {
			BmsInspectResult ticBmsOutspectResult = pdaTicPaliMapper.selectTicBmsOutRelatedByPackingNo(search);
			
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("inNo", 0);
			logParamg.put("outNo", 0);
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("blNo", "");
			logParamg.put("outCt", 0);
			logParamg.put("updateUserId", Util.null2str(search.get("updateUserId")));
			if(ticBmsOutspectResult == null) {
				reason = "이고반입대상아님";
			}else {
				reason = "이고반출출고처리확인";
				logParamg.put("inNo", ticBmsOutspectResult.getInNo());
				logParamg.put("outNo", ticBmsOutspectResult.getOutNo());
				logParamg.put("blNo", ticBmsOutspectResult.getBlNo());
				logParamg.put("outCt", ticBmsOutspectResult.getBmsOutCt());
			}
			logParamg.put("result", reason);
			
			pdaTicPaliMapper.insertTicMoveScanLog(logParamg);
			
			bmsInspectResultRst.setReason(reason);
			return bmsInspectResultRst;
		}
		
		reason = "완료";
		
		Map<String, Object> waitLogParam = new HashMap<String, Object>();
		waitLogParam.put("inNo", bmsInspectResult.getInNo());
		waitLogParam.put("blNo", bmsInspectResult.getBlNo());
		waitLogParam.put("packingNo", Util.null2str(search.get("scanPackingNo")));
		Map<String, Object> ticMoveScanLog = pdaTicPaliMapper.selectWaitPkgFromTicMoveScanLog(waitLogParam);
		if(ticMoveScanLog == null) { // 중복저장을 방지하기위해
			Map<String, Object> logParamg = new HashMap<String, Object>();
			logParamg.put("inNo", bmsInspectResult.getInNo());
			logParamg.put("outNo", bmsInspectResult.getOutNo());
			logParamg.put("packingNo", Util.null2str(search.get("scanPackingNo")));
			logParamg.put("blNo", bmsInspectResult.getBlNo());
			logParamg.put("outCt", bmsInspectResult.getBmsOutCt());
			logParamg.put("outBackStatus", "C"); // 하차 입고
			logParamg.put("result", reason);
			logParamg.put("updateUserId", Util.null2str(search.get("updateUserId")));
			pdaTicPaliMapper.insertTicMoveScanLog(logParamg);
			
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(search.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_TIC_MOVE_SCAN);
			processLogs.setDescription(BmsConsts.DbType.BMS_TIC_MOVE_SCAN+" [PDA 이고반입 확인 업데이트]");
			processLogs.setJobLog(new Gson().toJson(search));
			processLogs.setJobNo(Util.null2str(bmsInspectResult.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, BmsConsts.PodStatus.PDA_TIC_IN_CHECK /* pod Status 값 매핑 */
					, ""
					, null
			);
		}
		
		Map<String, Object> pkgCheckScanParam = new HashMap<String, Object>();
		pkgCheckScanParam.put("inPlanNo", bmsInspectResult.getInPlanNo());
		pkgCheckScanParam.put("packingNo", bmsInspectResult.getPackingNo());
		pkgCheckScanParam.put("blNo", bmsInspectResult.getBlNo());
		pkgCheckScanParam.put("ticStatus", "P");
		pkgCheckScanParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
		pdaTicPaliMapper.updatePkgTicStatus(pkgCheckScanParam);
		
		List<InPlanPkg> pkgList = pdaTicPaliMapper.selectBoxPkgWhereTicStatus(search);
		if(pkgList != null && bmsInspectResult.getCt() == pkgList.size()) {
			reason = "완료";
			
			Map<String, Object> bmsInUpdateParam = new HashMap<String, Object>();
			bmsInUpdateParam.put("inNo", bmsInspectResult.getInNo());
			bmsInUpdateParam.put("inspectionFinishYn", "Y");
			bmsInUpdateParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
			bmsInUpdateParam.put("remarks", Util.null2str(search.get("inspectResultFinishRemarks")));
			pdaTicPaliMapper.updateAllTicInspectResultFinish(bmsInUpdateParam);
			
			int cnt = 0;
			for(InPlanPkg pkg : pkgList) {
				cnt++;
				
				Map<String, Object> outBackInParam = new HashMap<String, Object>();
				outBackInParam.put("inNo", bmsInspectResult.getInNo());
				outBackInParam.put("packingNo", pkg.getPackingNo());
				if(cnt == pkgList.size()) {
					outBackInParam.put("reason", reason);
				}else {
					outBackInParam.put("reason", "입고");
				}
				outBackInParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				outBackInParam.put("outBackStatus", "D"); // 하차 입고
				outBackInParam.put("outBackInFinishYn", "S"); // 이고반입생성 대기
				pdaTicPaliMapper.updateTicMoveScanLogByC(outBackInParam);
				
				Map<String, Object> pkgMoveInParam = new HashMap<String, Object>();
				pkgMoveInParam.put("inPlanNo", pkg.getInPlanNo());
				pkgMoveInParam.put("packingNo", pkg.getPackingNo());
				pkgMoveInParam.put("blNo", pkg.getBlNo());
				pkgMoveInParam.put("moveInCt", 1);
				pkgMoveInParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				pdaTicPaliMapper.updatePkgTicMoveInByInPlanNo(pkgMoveInParam);
			}
			
			Map<String, Object> bmsInAllFinishParam = new HashMap<String, Object>();
			bmsInAllFinishParam.put("inNo", bmsInspectResult.getInNo());
			List<BmsInspectResult> notFinishedAllResultList = pdaTicPaliMapper.selectAllInspectResultNotFinished(bmsInAllFinishParam);
			if(notFinishedAllResultList == null || (notFinishedAllResultList != null && notFinishedAllResultList.size() == 0)) { // TB_BMS_INSPECT_RESULT의 IN_NO를 그룹으로 했을때 그그룹의 모든 검사가 완료된경우
				bmsInAllFinishParam.put("allInspectionFinishYn", "Y");
				bmsInAllFinishParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				pdaTicPaliMapper.updateBmsInAllInspectionFinishYn(bmsInAllFinishParam);
			}
			
			bmsInspectResultRst.setBmsInspectResult(bmsInspectResult);
			bmsInspectResultRst.setReason(reason);
			bmsInspectResultRst.setResultAction("검사완료");
			return bmsInspectResultRst;
		}
		
		search.put("inNo", bmsInspectResult.getInNo());
		int waitPkgCnt = pdaTicPaliMapper.countWaitPkgFromTicMoveScanLog(search);
		if(bmsInspectResult.getCt().intValue() !=  waitPkgCnt) {
			bmsInspectResultRst.setBmsInspectResult(bmsInspectResult);
			bmsInspectResultRst.setReason(reason);
			return bmsInspectResultRst;
		}
		
		bmsInspectResultRst.setBmsInspectResult(bmsInspectResult);
		bmsInspectResultRst.setReason(reason);
		bmsInspectResultRst.setResultAction("검사완료");
		return bmsInspectResultRst;
	}
	

	/**
	 * 이고반입 확인 - 일괄검사완료 ( 이고 로그-하차_입고 업데이트)
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
		
		// 1. inNo로 그룹핑
		Map<String, List<Map<String, Object>>> groupedMap = dataList.stream()
		    .filter(m -> m.get("inNo") != null)
		    .collect(Collectors.groupingBy(m -> m.get("inNo").toString()));

		// 2. ArrayList로 변환
		List<List<Map<String, Object>>> inNoGroupListOfList = new ArrayList<>(groupedMap.values());
		
		// 3. 각 그룹에서 첫 번째 요소만 남기기
		List<Map<String, Object>> inNoGroupList = inNoGroupListOfList.stream()
		    .map(list -> list.get(0)) // 각 그룹에서 첫 번째 요소만 가져옴
		    .collect(Collectors.toList());
		
		for(Map<String, Object> inNoGroupData :inNoGroupList) {
			
			/////////////////////전체 박스 체크및 업데이트///////////////////////
			int inNo = Util.null2int(inNoGroupData.get("inNo"));
			
			Map<String, Object> bmsInUpdateParam = new HashMap<String, Object>();
			bmsInUpdateParam.put("inNo", inNo);
			for(Map<String, Object> data: dataList) {
				if( inNo == Util.null2int(data.get("inNo")) ) {
					bmsInUpdateParam.put("inNo", inNo);
					bmsInUpdateParam.put("inspectionResultType", Util.null2str(data.get("inspectionResultType")));
					bmsInUpdateParam.put("inspectionFinishYn", "Y");
					bmsInUpdateParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
					bmsInUpdateParam.put("remarks", Util.null2str(data.get("reason")));
					pdaTicPaliMapper.updateInspectResultFinishByInspectionResultType(bmsInUpdateParam);
				}
			}
			
			bmsInspectResultRst.setBmsInMap(inNoGroupData); // 일괄처리 응답데이터 : 일부분만 처리된것들
			List<BmsInspectResult> notFinishedResultList = pdaTicPaliMapper.selectInspectResultNotFinished(bmsInUpdateParam);
			if(notFinishedResultList != null && notFinishedResultList.size() > 0) { // TB_BMS_INSPECT_RESULT의 IN_NO를 그룹으로 했을때 그그룹의 모든 검사가 완료안됬을 경우 Skip (continue;키워드)
				continue;
			}
			bmsInspectResultRst.setBmsInMap(inNoGroupData); // 일괄처리 응답데이터 : 모두 처리된것들
			// int waitPkgCnt = pdaTicPaliMapper.countWaitPkgFromTicMoveScanLog(bmsInUpdateParam);
			// if(Util.null2int(inNoGroupData.get("ct")) !=  waitPkgCnt + 1) {  // TB_BMS_TIC_MOVE_SCAN의 IN_NO를 그룹으로 했을때 그그룹의 하차대기(C)갯수와 반입의 CT수량이 일치하지않는경우 Skip (continue;키워드)
			//	continue;
			// }
			
			List<InPlanPkg> pkgList = pdaTicPaliMapper.selectPkgList(inNoGroupData);
			for(InPlanPkg pkg : pkgList) {
				Map<String, Object> paramTicLogParam = new HashMap<String, Object>();
				paramTicLogParam.put("inNo", inNoGroupData.get("inNo"));
				paramTicLogParam.put("blNo", inNoGroupData.get("blNo"));
				paramTicLogParam.put("packingNo", pkg.getPackingNo());
				Map<String, Object> ticMoveScanLog = pdaTicPaliMapper.selectWaitPkgFromTicMoveScanLog(paramTicLogParam);
				if(ticMoveScanLog == null) { // 중복저장을 방지하기위해
					Map<String, Object> logParamg = new HashMap<String, Object>();
					logParamg.put("inNo", Util.null2str(inNoGroupData.get("inNo")));
					logParamg.put("outNo", Util.null2str(inNoGroupData.get("outNo")));
					logParamg.put("packingNo", Util.null2str(pkg.getPackingNo()));
					logParamg.put("blNo", Util.null2str(inNoGroupData.get("blNo")));
					logParamg.put("outCt", Util.null2str(inNoGroupData.get("outCt")));
					logParamg.put("outBackStatus", "C"); // 하차 입고
					logParamg.put("result", "완료");
					logParamg.put("updateUserId", Util.null2str(search.get("updateUserId")));
					pdaTicPaliMapper.insertTicMoveScanLog(logParamg);
					
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(search.get("entCode")));
					processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
					processLogs.setLogJobType("DATA");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
					processLogs.setDbType(BmsConsts.DbType.BMS_TIC_MOVE_SCAN);
					processLogs.setDescription(BmsConsts.DbType.BMS_TIC_MOVE_SCAN+" [PDA 이고반입 확인 업데이트]");
					processLogs.setJobLog(new Gson().toJson(search));
					processLogs.setJobNo(Util.null2str(inNoGroupData.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
					processLogs.setResult("ok");
					processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
					processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
					bmsCommService.insertLogAndPodStatus(processLogs, request
							, BmsConsts.PodStatus.PDA_TIC_IN_CHECK /* pod Status 값 매핑 */
							, ""
							, null
					);
				}
			}
			
			for(InPlanPkg pkg : pkgList) {
				Map<String, Object> finishUpdateParam = new HashMap<String, Object>();
				finishUpdateParam.put("inNo", inNo);
				finishUpdateParam.put("packingNo", pkg.getPackingNo());
				finishUpdateParam.put("reason", "완료");
				finishUpdateParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				finishUpdateParam.put("outBackStatus", "D"); // 하차 입고
				finishUpdateParam.put("outBackInFinishYn", "S"); // 이고반입생성 대기
				pdaTicPaliMapper.updateFinishTicMoveScanLogByInNo(finishUpdateParam);
				
				Map<String, Object> pkgUpdateParam = new HashMap<String, Object>();
				pkgUpdateParam.put("inPlanNo", pkg.getInPlanNo());
				pkgUpdateParam.put("packingNo", pkg.getPackingNo());
				pkgUpdateParam.put("blNo", Util.null2str(inNoGroupData.get("blNo")));
				pkgUpdateParam.put("moveInCt", 1);
				pkgUpdateParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				pdaTicPaliMapper.updatePkgTicMoveInByInPlanNo(pkgUpdateParam);
			}
			
			Map<String, Object> bmsInAllFinishParam = new HashMap<String, Object>();
			bmsInAllFinishParam.put("inNo", inNo);
			List<BmsInspectResult> notFinishedAllResultList = pdaTicPaliMapper.selectAllInspectResultNotFinished(bmsInAllFinishParam);
			if(notFinishedAllResultList == null || (notFinishedAllResultList != null && notFinishedAllResultList.size() == 0)) { // TB_BMS_INSPECT_RESULT의 IN_NO를 그룹으로 했을때 그그룹의 모든 검사가 완료된경우
				bmsInAllFinishParam.put("allInspectionFinishYn", "Y");
				bmsInAllFinishParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				pdaTicPaliMapper.updateBmsInAllInspectionFinishYn(bmsInAllFinishParam);
			}
		}
		
		reason = "일괄완료";
		bmsInspectResultRst.setReason(reason);
		
		return bmsInspectResultRst;
	}
	
	/**
	 * 이고반입 확인 - 일괄검사완료 ( 이고 로그-하차_입고 업데이트)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInspectResultRst batchBoxFinish(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsInspectResultRst bmsInspectResultRst = new BmsInspectResultRst();
		
		bmsInspectResultRst.setResultCode("A012051");
		String reason = "";
		
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) search.get("dataList");
		if(dataList == null || (dataList != null && dataList.size() == 0)) {
			bmsInspectResultRst.setResultCode("A11481"); //해당 #1# 데이터는 존재하지 않습니다.
			return bmsInspectResultRst;
		}
		
		// 1. inNo로 그룹핑
		Map<String, List<Map<String, Object>>> groupedMap = dataList.stream()
		    .filter(m -> m.get("inNo") != null)
		    .collect(Collectors.groupingBy(m -> m.get("inNo").toString()));

		// 2. ArrayList로 변환
		List<List<Map<String, Object>>> inNoGroupListOfList = new ArrayList<>(groupedMap.values());
		
		// 3. 각 그룹에서 첫 번째 요소만 남기기
		List<Map<String, Object>> inNoGroupList = inNoGroupListOfList.stream()
		    .map(list -> list.get(0)) // 각 그룹에서 첫 번째 요소만 가져옴
		    .collect(Collectors.toList());
		
		for(Map<String, Object> inNoGroupData :inNoGroupList) {
			
			/////////////////////전체 박스 체크및 업데이트///////////////////////
			int inNoGroupDataInNo = Util.null2int(inNoGroupData.get("inNo"));
			
			Map<String, Object> bmsInUpdateParam = new HashMap<String, Object>();
			bmsInUpdateParam.put("inNo", inNoGroupDataInNo);
			for(Map<String, Object> data: dataList) {
				if( inNoGroupDataInNo == Util.null2int(data.get("inNo")) ) {
					bmsInUpdateParam.put("inNo", inNoGroupDataInNo);
					bmsInUpdateParam.put("inspectionFinishYn", "Y");
					bmsInUpdateParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
					bmsInUpdateParam.put("remarks", Util.null2str(data.get("reason")));
					pdaTicPaliMapper.updateAllTicInspectResultFinish(bmsInUpdateParam);
				}
			}
			
			bmsInspectResultRst.setBmsInMap(inNoGroupData); // 일괄처리 응답데이터 : 일부분만 처리된것들
			List<BmsInspectResult> notFinishedResultList = pdaTicPaliMapper.selectInspectResultNotFinished(bmsInUpdateParam);
			if(notFinishedResultList != null && notFinishedResultList.size() > 0) { // TB_BMS_INSPECT_RESULT의 IN_NO를 그룹으로 했을때 그그룹의 모든 검사가 완료안됬을 경우 Skip (continue;키워드)
				continue;
			}
			bmsInspectResultRst.setBmsInMap(inNoGroupData); // 일괄처리 응답데이터 : 모두 처리된것들
			// int waitPkgCnt = pdaTicPaliMapper.countWaitPkgFromTicMoveScanLog(bmsInUpdateParam);
			// if(Util.null2int(inNoGroupData.get("ct")) !=  waitPkgCnt + 1) {  // TB_BMS_TIC_MOVE_SCAN의 IN_NO를 그룹으로 했을때 그그룹의 하차대기(C)갯수와 반입의 CT수량이 일치하지않는경우 Skip (continue;키워드)
			//	continue;
			// }
			
			List<InPlanPkg> pkgList = pdaTicPaliMapper.selectPkgList(inNoGroupData);
			int cnt = 0;
			for(InPlanPkg pkg : pkgList) {
				cnt++;
				
				Map<String, Object> waitLogParam = new HashMap<String, Object>();
				waitLogParam.put("inNo", inNoGroupDataInNo);
				waitLogParam.put("blNo", pkg.getBlNo());
				waitLogParam.put("packingNo", pkg.getPackingNo());
				Map<String, Object> ticMoveScanLog = pdaTicPaliMapper.selectWaitPkgFromTicMoveScanLog(waitLogParam);
				if(ticMoveScanLog == null) { // 중복저장을 방지하기위해
					Map<String, Object> logParamg = new HashMap<String, Object>();
					logParamg.put("inNo", inNoGroupDataInNo);
					logParamg.put("outNo", inNoGroupData.get("outNo"));
					logParamg.put("packingNo", pkg.getPackingNo());
					logParamg.put("blNo", pkg.getBlNo());
					logParamg.put("outCt", inNoGroupData.get("bmsOutCt"));
					logParamg.put("outBackStatus", "C"); // 하차 입고
					logParamg.put("result", "완료");
					logParamg.put("updateUserId", Util.null2str(search.get("updateUserId")));
					pdaTicPaliMapper.insertTicMoveScanLog(logParamg);
					
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(search.get("entCode")));
					processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
					processLogs.setLogJobType("DATA");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
					processLogs.setDbType(BmsConsts.DbType.BMS_TIC_MOVE_SCAN);
					processLogs.setDescription(BmsConsts.DbType.BMS_TIC_MOVE_SCAN+" [PDA 이고반입 확인 업데이트]");
					processLogs.setJobLog(new Gson().toJson(search));
					processLogs.setJobNo(Util.null2str(inNoGroupData.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
					processLogs.setResult("ok");
					processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
					processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
					bmsCommService.insertLogAndPodStatus(processLogs, request
							, BmsConsts.PodStatus.PDA_TIC_IN_CHECK /* pod Status 값 매핑 */
							, ""
							, null
					);
				}
				
				Map<String, Object> outBackInParam = new HashMap<String, Object>();
				outBackInParam.put("inNo", inNoGroupDataInNo);
				outBackInParam.put("packingNo", pkg.getPackingNo());
				if(cnt == pkgList.size()) {
					outBackInParam.put("reason", "완료");
				}else {
					outBackInParam.put("reason", "입고");
				}
				outBackInParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				outBackInParam.put("outBackStatus", "D"); // 하차 입고
				outBackInParam.put("outBackInFinishYn", "S"); // 이고반입생성 대기
				pdaTicPaliMapper.updateTicMoveScanLogByC(outBackInParam);
				
				Map<String, Object> pkgMoveInParam = new HashMap<String, Object>();
				pkgMoveInParam.put("inPlanNo", pkg.getInPlanNo());
				pkgMoveInParam.put("packingNo", pkg.getPackingNo());
				pkgMoveInParam.put("blNo", pkg.getBlNo());
				pkgMoveInParam.put("moveInCt", 1);
				pkgMoveInParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				pdaTicPaliMapper.updatePkgTicMoveInByInPlanNo(pkgMoveInParam);
				
				Map<String, Object> pkgCheckScanParam = new HashMap<String, Object>();
				pkgCheckScanParam.put("inPlanNo", pkg.getInPlanNo());
				pkgCheckScanParam.put("packingNo", pkg.getPackingNo());
				pkgCheckScanParam.put("blNo", pkg.getBlNo());
				pkgCheckScanParam.put("ticStatus", "P");
				pkgCheckScanParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				pdaTicPaliMapper.updatePkgTicStatus(pkgCheckScanParam);
			}
			
			Map<String, Object> bmsInAllFinishParam = new HashMap<String, Object>();
			bmsInAllFinishParam.put("inNo", inNoGroupDataInNo);
			List<BmsInspectResult> notFinishedAllResultList = pdaTicPaliMapper.selectAllInspectResultNotFinished(bmsInAllFinishParam);
			if(notFinishedAllResultList == null || (notFinishedAllResultList != null && notFinishedAllResultList.size() == 0)) { // TB_BMS_INSPECT_RESULT의 IN_NO를 그룹으로 했을때 그그룹의 모든 검사가 완료된경우
				bmsInAllFinishParam.put("allInspectionFinishYn", "Y");
				bmsInAllFinishParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
				pdaTicPaliMapper.updateBmsInAllInspectionFinishYn(bmsInAllFinishParam);
			}
		}
		
		reason = "일괄완료";
		bmsInspectResultRst.setReason(reason);
		
		return bmsInspectResultRst;
	}

	/**
	 * 이고반출대상확인
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CarryInRst selectBmsInWithInspectResult(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInRst bmsInPlanPkgResult = new CarryInRst();
		List<Map<String, Object>> inspectResultList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> srchInspectResult = new ArrayList<Map<String, Object>>();
		boolean isUnManifested = false;
		String reason = "";

		if("Y".equals(Util.null2str(search.get("unManifested")))) {
			isUnManifested = true;
			reason = "무적";
		}

		/* Get Data Inspect Result */
		inspectResultList = pdaTicPaliMapper.selectBmsInWithInspectResult(search);

		/* Set Inspect Result */
		for( Map<String, Object> resultMap : inspectResultList ) {
			resultMap.put("INSPECTION_TYPE_NAME", convertInspectResult(resultMap));
		}

		/* Search Data Inspect Result */
		if( !isUnManifested && StringUtils.isNotEmpty((String) search.get("packingNo")) ) {
			search.put("srchPackingNo", search.get("packingNo"));
			search.put("srchPackingSeq", search.get("srchSeq"));
			srchInspectResult = pdaTicPaliMapper.selectBmsInWithInspectResult(search);

			if( ! srchInspectResult.isEmpty() ) {
				CarryIn carryIn = new CarryIn();
				carryIn.setPdaYn("Y");

				/** 작업내역 로그시작 */
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(search.get("entCode")));
				processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
				processLogs.setDbType(BmsConsts.DbType.BMS_TIC_PALI);
				processLogs.setDescription(BmsConsts.DbType.BMS_TIC_PALI+" [이고반출대상확인(PDA]");
				processLogs.setJobLog(new Gson().toJson(search));
				processLogs.setJobNo(Util.null2str(srchInspectResult.get(0).get("OD_JOB_NO")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setResult("ok");
				processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
				processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.BMS_TIC_PALI_CHK /* pod Status 값 매핑 */
						, "(" + search.get("packingNo") + ")"
						, carryIn
				);
			} else {
				Map<String, Object> srchInPlanPkg = pdaTicPaliMapper.selectSrchPkgInOutCt(search);
				if( "1".equals((String) srchInPlanPkg.get("MOVE_IN_CT")) ) {
					reason = "이미이고반입됨";
				} else if( "1".equals((String) srchInPlanPkg.get("MOVE_OUT_CT")) ) {
					reason = "이미이고반출됨";
				} else {
					String ticOutScanYn = pdaTicPaliMapper.selectTicOutScanYn(search);
					if( "Y".equals(ticOutScanYn) ) {
						reason = "이고반출준비중";
					} else {
						reason = "이고대상아님";
					}
				}
			}
		}

		if( StringUtils.isNotEmpty((String) search.get("packingNo")) ) {
			bmsInPlanPkgResult.setDeleteRowFlag( insertTicMoveScan(srchInspectResult.isEmpty() ? new HashMap<String, Object>() : srchInspectResult.get(0), search, reason) );
		}

		bmsInPlanPkgResult.setReason(reason);
		bmsInPlanPkgResult.setInspectResultList(inspectResultList);
		return bmsInPlanPkgResult;
	}

	public String insertTicMoveScan(Map<String, Object> srchInspectResult, Map<String, Object> search, String reason) throws Exception {
		String result = StringUtils.EMPTY;
		BmsTicMoveScan bmsTicMoveScan = new BmsTicMoveScan();
		bmsTicMoveScan.setScanInfo(srchInspectResult, search, StringUtils.isEmpty(reason) ? "완료" : reason);
		pdaTicPaliMapper.insertTicMoveScan(bmsTicMoveScan);

		if( !srchInspectResult.isEmpty() && ((Integer) srchInspectResult.get("OUT_CT")) == 1 ) {
			pdaTicPaliMapper.updateBmsOutTicOutScan(srchInspectResult);
		} else if( !srchInspectResult.isEmpty() && ((Integer) srchInspectResult.get("OUT_CT")) > 1 ) {
			int ticMoveScanCnt = pdaTicPaliMapper.selectBmsTicMoveScanCount(srchInspectResult);

			if( ((Integer) srchInspectResult.get("OUT_CT")) == ticMoveScanCnt ) {
				pdaTicPaliMapper.updateTicMoveScan(bmsTicMoveScan);
				pdaTicPaliMapper.updateBmsOutTicOutScan(srchInspectResult);
			}
		}

		Map<String, Object> pkgCheckScanParam = new HashMap<String, Object>();
		pkgCheckScanParam.put("inPlanNo", srchInspectResult.get("IN_PLAN_NO"));
		pkgCheckScanParam.put("packingNo", srchInspectResult.get("PACKING_NO"));
		pkgCheckScanParam.put("blNo", srchInspectResult.get("BL_NO"));
		pkgCheckScanParam.put("ticStatus", "T");
		pkgCheckScanParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
		pdaTicPaliMapper.updatePkgTicStatus(pkgCheckScanParam);
		result = "T";

		return result;
	}

	private String convertInspectResult(Map<String, Object> resultMap) throws Exception {
		StringBuilder inspectResult = new StringBuilder();

		// 거대
		if( StringUtils.isNotEmpty((String) resultMap.get("BIG_SIZE_YN_NAME")) ) 
			inspectResult.append((String) resultMap.get("BIG_SIZE_YN_NAME") + ", ");
		// 집중
		if( StringUtils.isNotEmpty((String) resultMap.get("INSPECTION_CUSTOMS_YN_NAME")) ) 
			inspectResult.append((String) resultMap.get("INSPECTION_CUSTOMS_YN_NAME") + ", ");
		// XRAY
		if( StringUtils.isNotEmpty((String) resultMap.get("XRAY_INSPECTION_NAME")) ) 
			inspectResult.append((String) resultMap.get("XRAY_INSPECTION_NAME") + ", ");
		// 선별
		if( StringUtils.isNotEmpty((String) resultMap.get("SE_INSPECTION_CODE_NAME")) )
			inspectResult.append((String) resultMap.get("SE_INSPECTION_CODE_NAME") + ", ");
		// 통관
		if( StringUtils.isNotEmpty((String) resultMap.get("PALN5EM_INSPECTION_NAME")) ) 
			inspectResult.append((String) resultMap.get("PALN5EM_INSPECTION_NAME") + ", ");
		// 우범
		if( StringUtils.isNotEmpty((String) resultMap.get("CRIME_SELECTION_YN_NAME")) )
			inspectResult.append((String) resultMap.get("CRIME_SELECTION_YN_NAME") + ", ");
		// 지재
		if( StringUtils.isNotEmpty((String) resultMap.get("INTELLECTUAL_PROP_YN_NAME")) ) 
			inspectResult.append((String) resultMap.get("INTELLECTUAL_PROP_YN_NAME") + ", ");

		return StringUtils.isEmpty(inspectResult.toString()) ? inspectResult.toString() : inspectResult.toString().substring(0, inspectResult.toString().length() -2);
	}

	public List<Map<String, Object>> selectScanLogInfo(Map<String, Object> search) {
		List<Map<String, Object>> scanDataList = pdaTicPaliMapper.selectScanLogInfo(search);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // KST(한국 시간) 설정

		for (Map<String, Object> map : scanDataList) {
		    if (map.get("REGISTER_DATE_TIME") instanceof Date) { 
		        map.put("REGISTER_DATE_TIME", sdf.format(map.get("REGISTER_DATE_TIME"))); // String 변환
		    }
		}
		return scanDataList;
	}
	
	public String getUnipassTicOutResultStr(String cargoReferenceNo, String bmsWhCode) throws Exception {
		List<Map<String, Object>> scanDataList = null;
		
		CargoProgressInfo cpInfo = bmsSendUtils.getCargoProgressInfo(cargoReferenceNo);
	    List<CargCsclPrgsInfoDtail> cargoLogList  = cpInfo.getCargCsclPrgsInfoDtails();
	        
        if(cargoLogList == null) {
        	log.info("유니패스 정보없음 skip");
        	return "";
        }
        List<CargCsclPrgsInfoDtail> latestTicStartList = cargoLogList.stream()
                // 조건 필터링
                .filter(detail -> detail.getRlbrDttmDateTime() != null &&
                                  bmsWhCode.equals(detail.getShedSgn()) && // 장치장 부호
                                  "04002001".equals(detail.getRlbrBssNo()) && // 반출입 근거 번호
                                  "이고반출(적출입작업)".equals(detail.getRlbrCn()) ) // 반출입 내용
                // 정렬
                .sorted(Comparator.comparing(
                        CargCsclPrgsInfoDtail::getRlbrDttmDateTime, 
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
        if(latestTicStartList.size() == 0) {
        	String resultCheck = getImportOutCheckFromUnipassList(cpInfo, bmsWhCode);
            if(!"".equals(resultCheck)) {
            	return resultCheck;
            }
            
        	log.info("유니패스: 우리창고에서 세관검사장으로의 이고반출내역 없음 skip");
        	return "";
        }
        LocalDateTime latestTicStartDay = latestTicStartList.get(0).getRlbrDttmDateTime();
        log.info("latestTicStartDay: "+ latestTicStartDay);
        
        List<CargCsclPrgsInfoDtail> filteredLogList = cargoLogList.stream()
                // 조건 필터링
                .filter(detail -> detail.getRlbrDttmDateTime() != null &&
                                  detail.getRlbrDttmDateTime().isAfter(latestTicStartDay) && // 전날 00:00:00 이후
                                  "04002001".equals(detail.getShedSgn())) // 장치장 부호
                // rlbrCn 조건 A), B), C) 구분
                .filter(detail -> ( "이고반출(적출입작업)".equals(detail.getRlbrCn()) && bmsWhCode.equals(detail.getRlbrBssNo()) ) ||
                                  "목록통관특송물품 반출".equals(detail.getRlbrCn())
                                  ) 
                // 정렬
                .sorted(Comparator.comparing(
                        CargCsclPrgsInfoDtail::getRlbrDttmDateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());

        if (filteredLogList == null || filteredLogList.isEmpty()) {
        	String resultCheck = getImportOutCheckFromUnipassList(cpInfo, bmsWhCode);
            if(!"".equals(resultCheck)) {
            	return resultCheck;
            }
            
        	log.info("04002001에 A)이고반출(적출입작업),  B)목록통관특송물품 반출 skip");
        	return "";
        }

        // `위 3가지 문구가 있는데이터`의 `가장 최근 rlbrDttm` 구하기
        LocalDateTime mostRecentRlbrDttm = filteredLogList.get(0).getRlbrDttmDateTime();
        log.info("LocalDateTime mostRecentRlbrDttm: " + mostRecentRlbrDttm);

        // `전체데이터`에서 `가장 최근 rlbrDttm`날짜의 ±10초 차이가 나는 데이터 필터링
        List<CargCsclPrgsInfoDtail> outside10SecondsLogList = cargoLogList.stream()
                .filter(detail -> {
                	if(!"04002001".equals(detail.getShedSgn())) return false;
                	if(detail.getRlbrDttmDateTime() == null) return false;
                	return Math.abs(ChronoUnit.SECONDS.between(mostRecentRlbrDttm, detail.getRlbrDttmDateTime())) <= 10;
                })
                .collect(Collectors.toList());
      
        List<CargCsclPrgsInfoDtail> notNeedUpdateCaseList = outside10SecondsLogList.stream() // 송신, 통관후입고 업데이트 등을 할 필요가 없는 경우
		        .filter(detail -> 
		        	"반출신고 취소".equals(detail.getCargTrcnRelaBsopTpcd())
		        )
				.collect(Collectors.toList());
        
        if (notNeedUpdateCaseList != null && !notNeedUpdateCaseList.isEmpty()) {
        	log.info("04002001에 A)이고반출(적출입작업) --> 취소 or  B)목록통관특송물품 반출 --> 취소 skip");
        	return "";
        }
        
        List<CargCsclPrgsInfoDtail> needSendXmlCaseList = outside10SecondsLogList.stream()
        		.filter(detail -> "이고반출(적출입작업)".equals(detail.getRlbrCn())) 
                .collect(Collectors.toList());
        if (needSendXmlCaseList != null && !needSendXmlCaseList.isEmpty()) {
        	
        	log.info("04002001에서 A)이고반출(적출입작업)");
        	return "이고반출";
        }
        
        List<CargCsclPrgsInfoDtail> needUpdateCaseList = outside10SecondsLogList.stream()
        		.filter(detail -> "목록통관특송물품 반출".equals(detail.getRlbrCn())) 
                .collect(Collectors.toList());
        if (needUpdateCaseList != null && !needUpdateCaseList.isEmpty()) {
        	
        	
        	log.info("04002001에서 B)목록통관특송물품 반출");
	        
        	return "목록반출";
        }
        
        String resultCheck = getImportOutCheckFromUnipassList(cpInfo, bmsWhCode);
        if(!"".equals(resultCheck)) {
        	return resultCheck;
        }
        
        log.info("unipass조회후, 처리 끝");
		return "";
	}
	
	public String getImportOutCheckFromUnipassList(CargoProgressInfo cpInfo, String bmsWhCode) {
        List<CargCsclPrgsInfoDtail> cargoLogList = cpInfo.getCargCsclPrgsInfoDtails();
        if (cargoLogList == null) {
            log.info("유니패스 정보없음 skip");
            return "";  // 필터링 대상에서 제외
        }
        
        List<CargCsclPrgsInfoDtail> latestImportList = cargoLogList.stream()
	                .filter(detail ->
	                        bmsWhCode.equals(detail.getShedSgn())
	                        && "수입신고".equals(detail.getCargTrcnRelaBsopTpcd()))
	                .sorted(Comparator.nullsLast(
	                		Comparator.comparingLong( (CargCsclPrgsInfoDtail detail) -> Util.str2longOrZero(detail.getPrcsDttm()) ) 
	                		).reversed()) // 내림차순 정렬
	                .collect(Collectors.toList());
	        
        if (latestImportList.isEmpty()) {
            log.info("유니패스: (" + bmsWhCode + ") 우리창고에서 수입신고내역 없음 skip");
            return "";
        }
        long latestImportTime = Util.str2longOrZero(latestImportList.get(0).getPrcsDttm());
      
        List<CargCsclPrgsInfoDtail> filteredLogList = cargoLogList.stream()
                // 조건 필터링
                .filter(detail -> Util.str2longOrZero(detail.getPrcsDttm()) >= latestImportTime &&
                                  bmsWhCode.equals(detail.getShedSgn())) // 장치장 부호
                .collect(Collectors.toList());
	        
        List<CargCsclPrgsInfoDtail> latestImportCompleteList = filteredLogList.stream()
                .filter(detail ->
                        bmsWhCode.equals(detail.getShedSgn())
                        && "수입신고수리".equals(detail.getCargTrcnRelaBsopTpcd()))
                .collect(Collectors.toList());
        
        if (latestImportCompleteList.isEmpty()) { //수입신고수리 취소내역이 있으면
        	log.info("유니패스: (" + bmsWhCode + ") 우리창고에서 수입신고수리내역 없음 skip");
            return "";
        }
	        
        List<CargCsclPrgsInfoDtail> latestCancelList = filteredLogList.stream()
                .filter(detail ->
                        bmsWhCode.equals(detail.getShedSgn())
                        && "수입신고수리 취소".equals(detail.getCargTrcnRelaBsopTpcd()))
                .collect(Collectors.toList());
        
        if (!latestCancelList.isEmpty()) { //수입신고수리 취소내역이 있으면
	      	CargCsclPrgsInfoDtail latestCancel = latestCancelList.get(0);
	      	CargCsclPrgsInfoDtail latestComplete = latestImportCompleteList.get(0);
      	  
	      	long cancelTime = Util.str2longOrZero(latestCancel.getPrcsDttm());
	      	long completeTime = Util.str2longOrZero(latestComplete.getPrcsDttm());
	      	log.info("cancelTime: " + cancelTime + ", completeTime: " + completeTime);
	      	if(cancelTime > completeTime || (cancelTime == completeTime) ) {
	      		log.info("유니패스: (" + bmsWhCode + ") 우리창고에서 수입신고수리 취소 skip");
	      		return "";
	      	}
        }

        log.info("유니패스: (" + bmsWhCode + ") 우리창고에서 수입신고수리내역 있음 ");
        return "실물이고대상";
	}
}
