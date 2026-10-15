package org.yht.exps.express.service.pda;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.pda.PdaOutReleasePaotMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.carryin.CarryInRst;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.exps.express.model.mobile.bms.BmsOutReleaseResult;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.util.Util;

import com.google.gson.Gson;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PdaOutReleasePaotService {
	
	@Autowired
	PdaOutReleasePaotMapper pdaOutReleasePaotMapper;
	
	@Autowired
	BmsCommService bmsCommService;
	
	/**
	 * 박스스캔 출고 최종처리 및 출고처리(OUT_SCAN_DATE_TIME, OUT_CT) 업데이트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public CarryInRst updateFinishOutRelease(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInRst carryInRst = new CarryInRst();
		Map<String, Object> logMap = new HashMap<String, Object>();
		List<String> cargoReferenceNoList = new ArrayList<>();
		List<String> odJobNoList = new ArrayList<>();
		List<Integer> outNoList = new ArrayList<>(); 
		List<BmsOutReleaseResult> returnList = new ArrayList<>();
		String xrayResult = "";
		logMap.put("scanNo", Util.null2str(search.get("srchPackingNo")));
		logMap.put("outLineNo", "P"); // 출고호기 PDA인 경우 P
		logMap.put("registerUserId", Util.null2str(search.get("registerUserId")));
		
		if( "Y".equals(Util.null2str(search.get("unManifested"))) ) {
			carryInRst.setReason("무적");
			// 스캔로그 저장
			logMap.put("outNo", "");
			logMap.put("blNo", "");
			logMap.put("cargoReferenceNo", "");
			logMap.put("result", carryInRst.getReason());
			pdaOutReleasePaotMapper.insertBmsOutScanLog(logMap);
			return carryInRst;
		}
		
		cargoReferenceNoList.add(Util.null2str(search.get("srchCargoReferenceNo")));
		odJobNoList.add(Util.null2str(search.get("srchOdJobNo")));
		
		// bl분할 존재여부 조회하여 해당 분할된 화물관리번호, 오더job번호를 추가하여 조회
		List<BmsOutReleaseResult> blDivideList = pdaOutReleasePaotMapper.selectExistBlDivideByBms5HF(search);
		for(BmsOutReleaseResult rst : blDivideList) {
			if(!"".equals(rst.getCargoReferenceNo())) {
				cargoReferenceNoList.add(rst.getCargoReferenceNo());
			}
			if(!"".equals(rst.getOdJobNo())) {
				odJobNoList.add(rst.getOdJobNo());
			}
		}
		
		search.put("cargoReferenceNoList", cargoReferenceNoList);
		search.put("odJobNoList", odJobNoList);
		List<BmsOutReleaseResult> list = pdaOutReleasePaotMapper.selectAllResultForOutRelease(search);
		
		if(list.size() > 0) {
			Optional<BmsOutReleaseResult> holdRow = list.stream().filter(obj -> "HOLD".equals(obj.getDGbn())).findFirst();
			Optional<BmsOutReleaseResult> bmsInRow = list.stream().filter(obj -> "반입".equals(obj.getDGbn())).findFirst();
			Optional<BmsOutReleaseResult> bmsOutPlanRow = list.stream().filter(obj -> "반출승인".equals(obj.getDGbn())).findFirst();
			Optional<BmsOutReleaseResult> bmsOutRow = list.stream().filter(obj -> "반출".equals(obj.getDGbn())).findFirst();
			Optional<BmsOutReleaseResult> pkgRow = list.stream().filter(obj -> "BOX".equals(obj.getDGbn())).findFirst();
			
			returnList = bmsInRow
				    .map(Collections::singletonList)  // Optional이 비어있지 않다면 List로 감쌈
				    .orElse(Collections.emptyList()); // 비어있다면 빈 리스트 반환
			
			String newCargoReferenceNo = "";
			// "반입" 레코드가 없으면(미반입) "BOX" 레코드를 return 
			if(returnList.isEmpty()) {
				returnList = pkgRow.map(Collections::singletonList).orElse(Collections.emptyList());
			}else {
				newCargoReferenceNo = pdaOutReleasePaotMapper.selectNewCargoReferenceNoFromPkg(search);
				search.put("cargoReferenceNo", !"".equals(Util.null2str(newCargoReferenceNo)) ? newCargoReferenceNo : returnList.get(0).getCargoReferenceNo());
			}
			
			// XRAY검사 설정 추가 2026.04.15 2sowell
			Map<String, Object> param = new HashMap<String, Object>();
			for(BmsOutReleaseResult result : returnList) {
				String xrayPassTime = pdaOutReleasePaotMapper.selectXrayPassTime(search);
				if( StringUtils.isEmpty(result.getXrayInspectionCodeName()) ) {
					result.setXrayInspectionCodeName( StringUtils.isEmpty(xrayPassTime) ? ("Y".equals(result.getBigSizeYn()) ? "판독대상아님" : "미판독") : "이상없음");
				}
				if("미판독".equals(Util.null2str(result.getXrayInspectionCodeName()))) {
					xrayResult = "미판독";
				}
			}
			carryInRst.setOutReleaseResultList(returnList);
			
			if(holdRow.isPresent()) { // "HOLD" 레코드가 있는경우
				carryInRst.setReason("홀드");
				carryInRst.setHoldReason(holdRow.get().getHoldReason());
			} else {
				if (!bmsInRow.isPresent()) { // "반입" 레코드가 없는경우
					carryInRst.setReason("반입전");
				} else {
					if(list.stream().anyMatch(obj -> "반입".equals(obj.getDGbn()) && "BMS01".equals(obj.getInLastPodStatus().trim())) ) { //"반입" 중에 반입신고상태가 BMS01.입력인 경우
						carryInRst.setReason("반입미전송");
					} else {
						if(list.stream().anyMatch(obj -> "반입".equals(obj.getDGbn()) && "BMS05".equals(obj.getInLastPodStatus().trim())) ) { //"반입" 중에 반입신고상태가 BMS05.오류인 경우
							carryInRst.setReason("반입오류통보");
						} else {
							if(list.stream().anyMatch(obj -> "반입".equals(obj.getDGbn()) // "반입" 중에 
									&& obj.getInPlanNo() > 0 // BL분할(23) 반입의 경우, 박스추가생성(새로운 IN_PLAN_NO)이 아닌이상 원본의 검사 결과에 따른다
									&& "N".equals(obj.getAllInspectionFinishYn()) // 검사완료여부가 N이면서
									&& (!"".equals(obj.getXrayInspectionCode().trim()) || // XRAY판독구분코드가 공백이 아니거나
										!"".equals(obj.getSeInspectionCode().trim()) ||  // 선별결과코드가 공백이 아니거나
										!"".equals(obj.getPaln5emInspectionCode().trim()) ||  // 통관검사계획코드가 공백이 아니거나
										!"".equals(obj.getInspectionType().trim()) ||  // 검역구분코드가 공백이 아니거나
										!"".equals(obj.getInspectionTypeA().trim()) ||  // 검역구분코드A가 공백이 아니거나
										!"".equals(obj.getInspectionTypeB().trim()) ||  // 검역구분코드B가 공백이 아니거나
										//"Y".equals(obj.getCrimeSelectionYn().trim()) || // 우범화물이거나
										"Y".equals(obj.getIntellectualPropYn().trim()) || // 지재권이거나
										"Y".equals(obj.getInspectionCustomsYn().trim()) || // 집중검사이거나
										"Y".equals(obj.getBigSizeYn().trim()) ) // 거대화물인 경우
									)) {
								carryInRst.setReason("검사미완료");
							} else {
								if(list.stream().sorted(Comparator.comparing(
											BmsOutReleaseResult::getNotifyDateTime, 
						                    Comparator.nullsLast(Comparator.reverseOrder())
						                )).limit(1).anyMatch(obj -> "반출승인".equals(obj.getDGbn()) && "1".equals(obj.getApprovalClass())) ) { // "반출승인" 중에 승인통보구분이 1.취하인경우
									carryInRst.setReason("반출승인취하");
								} else {
									if( list.stream().anyMatch(obj -> "반출".equals(obj.getDGbn()) && "61".equals(obj.getOutClass()) && "04002001".equals(obj.getOutApprovalNo())) // "반출" 중에 반출구분이 61.이고반출이 있는데 + 근거번호(04002001)
											&& list.stream().noneMatch(obj -> "반입".equals(obj.getDGbn()) && "21".equals(obj.getInClass())) ) { // "반입"에 반입구분이 21.이고반입인 데이터가 없는경우
										carryInRst.setReason("이고검사중");
									} else {
										String menuType = Util.null2str(search.get("menuType")); // 메뉴타입 - 통관반출출고: 50, 보세운송반출출고: 60, 멸각반출출고: 80
										
										if( "50".equals(menuType) && list.stream().anyMatch(obj -> "반출승인".equals(obj.getDGbn())) // "반출승인" 레코드가 있는데
												&& list.stream().noneMatch(obj -> "반출승인".equals(obj.getDGbn()) && ("50".equals(obj.getOutClass()) || "53".equals(obj.getOutClass()))) ) { // 반출구분이 53.목록통관특송물품반출, 50.수입신고수리후반출인 데이터가 없는경우
											carryInRst.setReason("미통관반출");
										} else if( "60".equals(menuType) && list.stream().anyMatch(obj -> "반출승인".equals(obj.getDGbn())) // "반출승인" 레코드가 있는데
												&& list.stream().noneMatch(obj -> "반출승인".equals(obj.getDGbn()) && "60".equals(obj.getOutClass())) ) { // 반출구분이 60.보세운송반출인 데이터가 없는경우
											carryInRst.setReason("미보세운송");
										} else if( "80".equals(menuType) && list.stream().anyMatch(obj -> "반출승인".equals(obj.getDGbn())) // "반출승인" 레코드가 있는데
												&& list.stream().noneMatch(obj -> "반출승인".equals(obj.getDGbn()) && "80".equals(obj.getOutClass())) ) { // 반출구분이 80.멸각폐기반출인 데이터가 없는경우
											carryInRst.setReason("미멸각폐기");
										} else {
											if(!bmsOutPlanRow.isPresent()) { // "반출승인" 레코드가 없는경우
												carryInRst.setReason("반출미승인");
											} else {
												if(list.stream().anyMatch(obj -> "BOX".equals(obj.getDGbn()) && obj.getOutCt() == 1) ) { // "BOX" 중에 출고수량이 1인경우
													carryInRst.setReason("이미출고됨");	
												} else {
													// "반입"에 수량,중량이 "반출승인"에 수량,중량과 같지 않을 경우
													if( "N".equals(Util.null2str(search.get("isDivideYn"))) && list.stream().filter(d1 -> "반입".equals(d1.getDGbn())).noneMatch(d1 -> 
														list.stream().filter(d2 -> "반출승인".equals(d2.getDGbn())).anyMatch(d2 -> d1.getCt() == d2.getCt() && Double.compare(d1.getWt(), d2.getWt()) == 0)) ) {
														carryInRst.setReason("분할승인"); 
													} else {
														int classApprovalCt = 0; // 반출구분 승인수량
														int outPlanScanCt = 0; // 반출구분 SCAN수량
														int outPlanNo = 0;
														
														List<String> outClassList = new ArrayList<>();
														outClassList.add(menuType);
														if("50".equals(menuType)) {  //PDA 통관반출출고는 53번도 해당
															outClassList.add("53");
														}
														search.put("outClassList", outClassList);
														List<Map<String, Object>> outPlanCtInfo = pdaOutReleasePaotMapper.selectOutPlanListCtByCargoNoAndOutClass(search); // 반출구분 승인수량
														if(outPlanCtInfo == null || (outPlanCtInfo != null && outPlanCtInfo.size() == 0)) {
															classApprovalCt = 0;
															outPlanScanCt = 0;
														}else {
															outPlanCtInfo.removeIf(row -> Util.null2str(row.get("OUT_APPROVAL_NO")).startsWith("TIC") && !"53".equals(Util.null2str(row.get("OUT_CLASS")))); // 이고에서 생성된 TIC목록 반출승인은 제거
															if(outPlanCtInfo.size() == 0) {
																classApprovalCt = 0;
																outPlanScanCt = 0;
															}else {
																Map<String, Object> outPlan = outPlanCtInfo.get(0);
																classApprovalCt = Util.null2int(outPlan.get("APPROVAL_CT"));
																outPlanScanCt = Util.null2int(outPlan.get("OUT_SCAN_CT"));
																outPlanNo = Util.null2int(outPlan.get("OUT_PLAN_NO"));
																logMap.put("outClass", Util.null2str(outPlan.get("OUT_CLASS"))); 
															}
															
														}
														
														log.debug("반출구분 승인수량: " + classApprovalCt + ", 반출구분 SCAN수량: " + outPlanScanCt);
														if(pdaOutReleasePaotMapper.selectExistsScanOutCount(search) > 0) {
															carryInRst.setReason("이미출고됨");
														}else if(outPlanCtInfo == null) {
															carryInRst.setReason("50".equals(menuType) ? "미통관반출" : "60".equals(menuType) ? "미보세운송" : "80".equals(menuType) ? "미멸각폐기" : "");
														}else if(classApprovalCt <= outPlanScanCt) {
															carryInRst.setReason("승인수량출고됨");
														}else {
															carryInRst.setReason("완료"); // 위 조건에 모두 맞지 않는 건이 완료 = 출고대상
															
															// (outPlanNo과)반출승인과 일치하는 반출을 찾아서 update해야함 
															//if("Y".equals(Util.null2str(search.get("isDivideYn")))) {
															outNoList = list.stream()
																    .filter(d -> "반출".equals(d.getDGbn()))
																    .map(BmsOutReleaseResult::getKeyNo)
																    .collect(Collectors.toList());
															search.put("outNoList", outNoList);
															// 반출출고일시 업데이트
															search.put("outPlanNo", outPlanNo);
															pdaOutReleasePaotMapper.updateOutScanDtFromBmsOutAndOutPlan2(search);
															//}else {
															//outNoList = list.stream()
															//	    .filter(d -> "반출".equals(d.getDGbn()))
															//	    .map(BmsOutReleaseResult::getKeyNo)
															//	    .collect(Collectors.toList());
															//
															//search.put("outPlanNoList", outPlanNoList);
															//search.put("outNoList", outNoList);
															//search.put("outCt", 1);
															//search.put("outScanDateTime", "");
															//
															//// 반출출고일시 업데이트
															//pdaOutReleasePaotMapper.updateOutScanDtFromBmsOutAndOutPlan(search);
															//}
															search.put("outCt", 1);
															pdaOutReleasePaotMapper.updateOutScanCtFromOutPlan(search); // 반출승인 SCAN수량 업데이트 + pkg OUT_CT 업데이트
															
															logMap.put("outNo", outNoList.isEmpty() ? "" : outNoList.get(outNoList.size()-1));
															logMap.put("blNo", returnList.get(0).getBlNo());
															logMap.put("cargoReferenceNo", returnList.get(0).getCargoReferenceNo());
															logMap.put("result", carryInRst.getReason());
															pdaOutReleasePaotMapper.insertBmsOutScanLogSelectPkg(logMap); // INSERT TB_BMS_OUT_SCAN select pkg where out_ct = 0
															
															//search.put("outCt", 1);
															// 반출출고수량 업데이트
															//pdaOutReleasePaotMapper.updateOutCtFromBmsInPlanPkg(search);
															
															// DELETE TB_BMS_STOCK_PKG_SCAN
															pdaOutReleasePaotMapper.deleteStockPkgScan(logMap);
															
															/** pod 저장 */
															BmsOutPlan bmsOutPlan = new BmsOutPlan();
															String remark = "("+ Util.null2str(search.get("srchPackingNo")) +")";
															
															ProcessLogs processLogs = new ProcessLogs();
															processLogs.setEntCode(Util.null2str(search.get("entCode")));
															processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
															processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
															processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
															processLogs.setLogJobType("DATA");
															processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
															processLogs.setDbType(BmsConsts.DbType.BMS_OUT_PLAN);
															processLogs.setDescription(BmsConsts.DbType.BMS_OUT_PLAN+" [PDA 반출출고 업데이트]");
															processLogs.setJobLog(new Gson().toJson(search));
															processLogs.setJobNo(Util.null2str(search.get("srchOdJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
															processLogs.setResult("ok");
															processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
															processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
															bmsCommService.insertLogAndPodStatus(processLogs, request
																	, BmsConsts.PodStatus.BMS_OUT_RELEASE /* pod Status 값 매핑 */
																	, remark // 비고 (# 박스번호)
																	, bmsOutPlan
																	);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		// 스캔로그 저장
		if(returnList.isEmpty()) {
			logMap.put("outNo", "");
			logMap.put("blNo", "");
			logMap.put("cargoReferenceNo", "");
		} else {
			logMap.put("outNo", outNoList.isEmpty() ? "" : outNoList.get(outNoList.size()-1));
			logMap.put("blNo", returnList.get(0).getBlNo());
			logMap.put("cargoReferenceNo", returnList.get(0).getCargoReferenceNo());
		}
		logMap.put("result", carryInRst.getReason());

		if(!"완료".equals(logMap.get("result")) ) {
			// INSERT TB_BMS_OUT_SCAN
			pdaOutReleasePaotMapper.insertBmsOutScanLog(logMap);
		}
		
		if(xrayResult.equals("미판독")) {
			xrayResult = xrayResult + "\n" + carryInRst.getReason();
			carryInRst.setReason(xrayResult);
		}
		
		return carryInRst;
	}
	
	public List<Map<String, Object>> selectScanLogInfo(Map<String, Object> search) {
		List<Map<String, Object>> scanDataList = pdaOutReleasePaotMapper.selectScanLogInfo(search);

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
	 * 박스스캔 출고 최종처리 및 출고처리(OUT_SCAN_DATE_TIME, OUT_CT) 취소
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public void updateFinishOutCancle(Map<String, Object> search, HttpServletRequest request) throws Exception {
		List<String> cargoReferenceNoList = new ArrayList<>();
		List<String> odJobNoList = new ArrayList<>();
		Map<String, Object> logMap = new HashMap<String, Object>();

		cargoReferenceNoList.add(Util.null2str(search.get("srchCargoReferenceNo")));
		odJobNoList.add(Util.null2str(search.get("srchOdJobNo")));

		// bl분할 존재여부 조회하여 해당 분할된 화물관리번호, 오더job번호를 추가하여 조회
		List<BmsOutReleaseResult> blDivideList = pdaOutReleasePaotMapper.selectExistBlDivideByBms5HF(search);
		for(BmsOutReleaseResult rst : blDivideList) {
			if(!"".equals(rst.getCargoReferenceNo())) {
				cargoReferenceNoList.add(rst.getCargoReferenceNo());
			}
			if(!"".equals(rst.getOdJobNo())) {
				odJobNoList.add(rst.getOdJobNo());
			}
		}

		search.put("cargoReferenceNoList", cargoReferenceNoList);
		search.put("odJobNoList", odJobNoList);
		List<BmsOutReleaseResult> list = pdaOutReleasePaotMapper.selectAllResultForOutRelease(search);

		if(list.size() > 0) {
			//'반출승인'중에 OUT_PLAN_NO 로 조회 OUT_SCAN_DATE_TIME 가 비워있는 경우만 OUT_SCAN_DATE_TIME <- '1900-01-01 00:00:00.000' 저장
			List<Integer> outPlanNoList = list.stream()
					.filter(d -> "반출승인".equals(d.getDGbn()))
					.map(BmsOutReleaseResult::getKeyNo)
					.collect(Collectors.toList());

			List<Integer> outNoList = list.stream()
					.filter(d -> "반출".equals(d.getDGbn()))
					.map(BmsOutReleaseResult::getKeyNo)
					.collect(Collectors.toList());

			search.put("outPlanNoList", outPlanNoList);
			search.put("outNoList", outNoList);
			search.put("outCt", 0);
			search.put("outScanDateTime", "1900-01-01 00:00:00.000");
			search.put("isCancleYn", "Y");

			String newCargoReferenceNo = pdaOutReleasePaotMapper.selectNewCargoReferenceNoFromPkg(search);
			search.put("cargoReferenceNo", !"".equals(Util.null2str(newCargoReferenceNo)) ? newCargoReferenceNo : cargoReferenceNoList.get(0));
			
			int outPlanNo = 0;
			
			String menuType = Util.null2str(search.get("menuType"));
			List<String> outClassList = new ArrayList<>();
			outClassList.add(menuType);
			if("50".equals(menuType)) { //PDA 통관반출출고는 53번도 해당
				outClassList.add("53");
			}
			search.put("outClassList", outClassList);
			List<Map<String, Object>> outPlanCtInfo = pdaOutReleasePaotMapper.selectOutPlanListCtByCargoNoAndOutClass(search); // 반출구분 승인수량
			if(outPlanCtInfo != null && outPlanCtInfo.size() > 0) {
				outPlanCtInfo.removeIf(row -> Util.null2str(row.get("OUT_APPROVAL_NO")).startsWith("TIC") && !"53".equals(Util.null2str(row.get("OUT_CLASS")))); // 이고에서 생성된 TIC목록 반출승인은 제거
				if(outPlanCtInfo.size() > 0) {
					Map<String, Object> outPlan = outPlanCtInfo.get(0);
					outPlanNo = Util.null2int(outPlan.get("OUT_PLAN_NO"));
				}
			}
			search.put("outPlanNo", outPlanNo);
			
			// 반출출고일시 업데이트
			pdaOutReleasePaotMapper.updateOutScanDtFromBmsOutAndOutPlan2(search);
			
			pdaOutReleasePaotMapper.updateOutScanCtFromOutPlan(search); // 반출승인 SCAN수량 업데이트
			// 반출출고수량 업데이트
			pdaOutReleasePaotMapper.updateOutCtFromBmsInPlanPkg(search);

			/** pod 저장 */
			BmsOutPlan bmsOutPlan = new BmsOutPlan();
			String remark = (String) search.get("reason");

			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(search.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_OUT_PLAN);
			processLogs.setDescription(BmsConsts.DbType.BMS_OUT_PLAN+" [PDA 반출출고 업데이트]");
			processLogs.setJobLog(new Gson().toJson(search));
			processLogs.setJobNo(Util.null2str(search.get("srchOdJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, BmsConsts.PodStatus.BMS_OUT_CANCLE /* pod Status 값 매핑 */
					, remark // 비고 (# 박스번호)
					, bmsOutPlan
			);

			logMap.put("srchPackingNo", Util.null2str(search.get("srchPackingNo")));
			pdaOutReleasePaotMapper.deleteBmsOutScanLog(logMap);
		}
	}
}
