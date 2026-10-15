package org.yht.exps.express.service.bms;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsCommMapper;
import org.yht.exps.express.mapper.bms.BmsInMapper;
import org.yht.exps.express.mapper.bms.BmsOutMapper;
import org.yht.exps.express.mapper.bms.BmsPkgNoMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsComm.BmsEcmPod;
import org.yht.exps.express.model.bms.carryin.BatchCarryIn;
import org.yht.exps.express.model.bms.carryin.BmsInspectResult;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.CarryInDetailList;
import org.yht.exps.express.model.bms.carryin.CarryInList;
import org.yht.exps.express.model.bms.carryin.CarryInSaveResult;
import org.yht.exps.express.model.bms.carryin.Container;
import org.yht.exps.express.model.bms.carryin.DeleteCarryIn;
import org.yht.exps.express.model.bms.carryin.DeleteInPlan;
import org.yht.exps.express.model.bms.carryin.InPlan;
import org.yht.exps.express.model.bms.carryin.InPlanCheckResult;
import org.yht.exps.express.model.bms.carryin.InPlanList;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.bms.carryin.InPlanPkgList;
import org.yht.exps.express.model.bms.carryin.OdAbnormal;
import org.yht.exps.express.model.bms.carryin.WithHolding;
import org.yht.exps.express.model.bms.carryin.WithHoldingCheckResult;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.disposal.SpCallVo;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo.CargCsclPrgsInfoDtail;
import org.yht.exps.express.model.eCommerce.order.OrderHold;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.bms.send.BmsSendXmlParser;
import org.yht.exps.express.service.pda.PdaInspectPaisService;
import org.yht.util.Util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsInService {

	@Autowired
	OrderMapper orderMapper;
	
	@Autowired
	CommonMapper commonMapper;
	
	@Autowired
	BmsCommMapper bmsCommMapper;

	@Autowired
	BmsInMapper bmsInMapper;
	
	@Autowired
	BmsOutMapper bmsOutMapper;

	@Autowired
	BmsPkgNoMapper bmsPkgNoMapper;

	@Autowired
	BmsDisposalService bmsDisposalService;
	
	@Autowired
	PdaInspectPaisService pdaInspectPaisService;

	@Autowired
	CommonService commonService;

	@Autowired
	BmsSendXmlParser bmsSendXmlParser;

	@Autowired
	BmsCommService bmsCommService ;
	
	@Autowired
    BmsSendUtils bmsSendUtils;

	/**
	 * InPlanList(Detail)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public InPlanList selectInPlanList(SearchInList search) throws Exception {
		InPlanList inPlanList = new InPlanList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|AR_DATE|CARRYIN_DATE|OUT_NOTICE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO|PKG_NO");
		List<InPlan> list  = bmsInMapper.selectInPlanList2(search);
		inPlanList.setInPlanList(list);
		if(list.size() > 0) {
			inPlanList.setTotal(list.get(0).getTotal());
		}else {
			inPlanList.setTotal(0);
		}
		return inPlanList;
	}
	
	/**
	 * CarryInList(Detail)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public CarryInList selectCarryInList(SearchInList search) throws Exception {
		CarryInList carryInList = new CarryInList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|AR_DATE|CARRYIN_DATE|OUT_NOTICE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO|PKG_NO");
		List<CarryIn> list  = bmsInMapper.selectCarryInList3(search);
		carryInList.setCarryInList(list);
		if(list.size() > 0) {
			carryInList.setTotal(list.get(0).getTotal());
		}else {
			carryInList.setTotal(0);
		}
		return carryInList;
	}
	
	/**
	 * InPlanPkgList(Detail)
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public InPlanPkgList selectInPlanPkgList(SearchInList search) throws Exception {
		InPlanPkgList carryInList = new InPlanPkgList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|AR_DATE|CARRYIN_DATE|OUT_NOTICE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO|PKG_NO");
		List<InPlanPkg> list  = bmsInMapper.selectInPlanPkgList2(search);
		carryInList.setInPlanPkgList(list);
		if(list.size() > 0) {
			carryInList.setTotal(list.get(0).getTotal());
		}else {
			carryInList.setTotal(0);
		}
		return carryInList;
	}

	/**
	 * XRAY판독 엑셀 list 조회
	 * @param search
	 * @return
	 */
	public List<Map<String, Object>> selectXrayResultForDownload(SearchInList search) throws Exception {
		Util.accessControlDatabase(search, "userLanguage", true, "srchDateGbn", "|AR_DATE|CARRYIN_DATE|OUT_NOTICE_DATE",  null, null);
		return bmsInMapper.selectXrayResultForDownload(search);
	}
	
	/**
	 * 반입예정 개별 한건 조회
	 * @param search
	 * @return
	 */
	public InPlan selectDetailInPlan(SearchInList search){
		List<Container> containerList = bmsInMapper.selectContainerListByInPlanNo(search.getInPlanNo());
		InPlan inPlan = bmsInMapper.selectDetailInPlan(search);
		inPlan.setContainerList(containerList);
		return inPlan;
	}

	/**
	 * 반입 개별 한건 조회
	 * @param search
	 * @return
	 */
	public CarryIn selectDetailCarryIn(SearchInList search) {
		String logVal = "반입번호::: " + search.getInNo();
		log.info(logVal);
		List<Container> containerList = bmsInMapper.selectContainerListByInNo(search.getInNo());
		CarryIn carryIn = bmsInMapper.selectDetailCarryIn(search);
		logVal = "콘테이너리스트: " + containerList.toString();
		log.info(logVal);
		if(containerList.size() > 0) {
			carryIn.setContainerList(containerList);
		}
		return carryIn;
	}
	
	/**
	 * 반입 개별 한건 조회
	 * @param search
	 * @return
	 */
	public CarryIn getCarryIn(SearchInList search, HttpServletRequest request) {
		CarryIn carryIn = bmsInMapper.selectDetailCarryIn(search);
		return carryIn;
	}
	
	public CarryInSaveResult updateCarryInChangeByCustoms(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();

		SearchInList searchInList = new SearchInList();
		searchInList.setInNo(Util.null2str(listData.get("inNo")));
		CarryIn carryIn = bmsInMapper.selectDetailCarryIn(searchInList);
		
		boolean isExistUnipassCarryInChange = this.getCarryInChangeFromUnipassList(carryIn.getCargoReferenceNo(), Util.null2str(listData.get("bmsWhCode")));
		if(!isExistUnipassCarryInChange) {
			result.setResultCode("A11481");  //해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
			
		bmsInMapper.updateCtWtByInNo(listData);
		
		// WT,CT 데이터 업데이트 STOCK_WT 계산
		BmsOut bmsOut = new BmsOut();
		bmsOut.setInNo(carryIn.getInNo());
		bmsOutMapper.updateInCtWtCbm(bmsOut);
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(listData.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(listData.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(listData.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [집중검사 업데이트]");
		processLogs.setJobLog(new Gson().toJson(listData));
		processLogs.setJobNo(String.valueOf(carryIn.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(listData.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(listData.get("logUserNameEng")));
		//commonService.insertLog(processLogs, request);
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.BMS_IN_CHANGE_CUSTOMS /* pod Status 값 매핑 */
				, "직권정정"
				, carryIn
		);
		
		result.setResultCode("A012051"); // 저장 되었습니다.
		return result;
	}

	public boolean getCarryInChangeFromUnipassList(String cargoReferenceNo, String bmsWhCode) throws Exception {
		CargoProgressInfo cpInfo = bmsSendUtils.getCargoProgressInfo(cargoReferenceNo);
	    List<CargCsclPrgsInfoDtail> cargoLogList  = cpInfo.getCargCsclPrgsInfoDtails();
        if (cargoLogList == null) {
            log.info("유니패스 정보없음 skip");
            return false;  // 필터링 대상에서 제외
        }
        
        List<CargCsclPrgsInfoDtail> latestImportList = cargoLogList.stream()
	                .filter(detail ->
	                        bmsWhCode.equals(detail.getShedSgn())
	                        && "반입신고 정정 승인신청 접수".equals(detail.getCargTrcnRelaBsopTpcd()))
	                .sorted(Comparator.nullsLast(
	                		Comparator.comparingLong( (CargCsclPrgsInfoDtail detail) -> Util.str2longOrZero(detail.getPrcsDttm()) ) 
	                		).reversed()) // 내림차순 정렬
	                .collect(Collectors.toList());
	        
        if (latestImportList.isEmpty()) {
            log.info("유니패스: (" + bmsWhCode + ") 우리창고에서 '반입신고 정정 승인신청 접수' 없음 skip");
            return false;
        }
        

        log.info("유니패스: (" + bmsWhCode + ") 우리창고에서 '반입신고 정정 승인신청 접수' 있음 ");
        return true;
	}
	
	/**
	 * 반입신청 - insert
	 * @param carryIn
	 * @param request
	 * @return
	 */
	public CarryInSaveResult insertCarryIn(CarryIn carryIn, HttpServletRequest request) throws Exception {

		CarryInSaveResult carryInSaveResult = new CarryInSaveResult();
		
		Map<String, String> holdParam = new HashMap<String, String>();
		holdParam.put("srchEntCode", carryIn.getEntCode());
		holdParam.put("srchOdJobNo", carryIn.getOdJobNo());
		holdParam.put("dateFormat", "23");
		
		Util.accessControlDatabase(holdParam, "dateFormat", false, null, null, null, null);
		List<OrderHold> orderHoldList = orderMapper.selectHold(holdParam);
		for(OrderHold orderHold : orderHoldList) {
			if(orderHold.getCarryinStopYn().equals("Y") && orderHold.getHoldReleaseYn().equals("N")) {
				carryInSaveResult.setResultCode("ABMS100036");// 저장 되었습니다.
				carryInSaveResult.setHoldReason(orderHold.getHoldReason());
				return carryInSaveResult;
			}
		}
		
//		/** 반입예정으로 반입 신규생성일경우 해당반입예정번호로 생성된 반입데이터 있는지 확인 */
//		if(!Util.isEmpty(carryIn.getInPlanNo())){
//			if(bmsInMapper.checkExistCarryIn(carryIn.getInPlanNo()) > 0){
//				carryInSaveResult.setResultCode("A11253");	/** 이미 생성된 건이 존재합니다. */
//				return carryInSaveResult;
//			}
//		}
		
		String bmsWhCode = carryIn.getBmsWhCode();
		SpCallVo voIn = getBmsInCustomsReportNo(bmsWhCode);
		log.info("반입신고번호: {}", voIn.getRetCustomsReportNo());
		carryIn.setCustomsReportNo(voIn.getRetCustomsReportNo());

		log.info("반입번호: {}",carryIn.getInNo());
		log.info("반입예정번호: {}",carryIn.getInPlanNo());
		if(carryIn.getInNo() == null && (carryIn.getInPlanNo() == null || carryIn.getInPlanNo() == 0) ) {
			Util.accessControlDatabase(carryIn, "dateFormat", false, null, null, null, null);
			int intRow2 = bmsInMapper.insertHandInputCarryIn(carryIn);
			
			if("PANTK".equals(carryIn.getEntCode()) && "21".equals(carryIn.getInClass())) {
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("cargoReferenceNo", carryIn.getCargoReferenceNo());
				BmsOutPlan outPlan = bmsOutMapper.selectOutPlanNotExistInNo(param);
				if(outPlan != null) {
					List<InventoryIn> bmsInList = bmsOutMapper.selectBmsInListByCargoNo(param);
					List<InventoryIn> bmsInExistStockList = bmsInList.stream().filter(bmsIn -> bmsIn.getStockCt() > 0).collect(Collectors.toList());
					InventoryIn bmsIn = bmsInExistStockList.get(0);
					
					outPlan.setInNo(bmsIn.getInNo());
					bmsOutMapper.updateInNoOfOutPlan(outPlan);
				}
				
				int origInPlanNo = 0;
				CarryIn firstBmsIn = bmsInMapper.selectFirstBmsInByCargoNo(carryIn);
				if(firstBmsIn.getInPlanNo() > 0) {
					origInPlanNo =  firstBmsIn.getInPlanNo();
					carryIn.setOrigInPlanNo(origInPlanNo);
					bmsInMapper.updateOrigInPlanNoOfBmsInByInNo(carryIn);
					
					Map<String, Object> pkgParam = new HashMap<String, Object>();
					pkgParam.put("inPlanNo", origInPlanNo); 
					pkgParam.put("moveInCt", 1);
					pkgParam.put("updateUserId", carryIn.getUpdateUserId());
					bmsInMapper.updatePkgTicMoveInByInPlanNo(pkgParam);
				}
				
				Map<String, Object> ticInitParam = new HashMap<>();
				ticInitParam.put("blNo", carryIn.getBlNo());
				bmsInMapper.updateTicLogFinishedY(ticInitParam);
				
				Map<String, Object> inspectUpdateParam = new HashMap<>();
				inspectUpdateParam.put("inNo", firstBmsIn.getInNo());
				inspectUpdateParam.put("updateUserId", carryIn.getUpdateUserId());
				pdaInspectPaisService.updateInspectionTypeByTicInspectResultCode(inspectUpdateParam); // TB_BMS_INSPECT_RESULT  Y처리
				pdaInspectPaisService.updateBmsInAllInspectionFinishYn(inspectUpdateParam); // TB_BMS_IN의  ALL_INSPECTION_FINISH_YN   Y처리
				
				carryInSaveResult.setReferenceInPlanNo(origInPlanNo);
			}
		}else {
			Util.accessControlDatabase(carryIn, "dateFormat", false, null, null, null, null);
			int intRow = bmsInMapper.insertCarryIn(carryIn);
		}

		//화물정보 저장
		List<Container> containerList = new ArrayList<>();
		Container container1 = new Container();
		Container container2 = new Container();
		Container container3 = new Container();

		container1.setCustomsReportNo(carryIn.getCustomsReportNo());
		container1.setCntrNo(carryIn.getCntrNo1());
		container1.setCntrEdiCode(carryIn.getCntrEdiCode1());
		container1.setSealNo1(carryIn.getSealNo1A());
		container1.setSealNo2(carryIn.getSealNo1B());

		container2.setCustomsReportNo(carryIn.getCustomsReportNo());
		container2.setCntrNo(carryIn.getCntrNo2());
		container2.setCntrEdiCode(carryIn.getCntrEdiCode2());
		container2.setSealNo1(carryIn.getSealNo2A());
		container2.setSealNo2(carryIn.getSealNo2B());

		container3.setCustomsReportNo(carryIn.getCustomsReportNo());
		container3.setCntrNo(carryIn.getCntrNo3());
		container3.setCntrEdiCode(carryIn.getCntrEdiCode3());
		container3.setSealNo1(carryIn.getSealNo3A());
		container3.setSealNo2(carryIn.getSealNo3B());

		if(carryIn.getCntrNo1() != "") {
			containerList.add(container1);
		}
		if(carryIn.getCntrNo2() != "") {
			containerList.add(container2);
		}
		if(carryIn.getCntrNo3() != "") {
			containerList.add(container3);
		}

		for (Container container: containerList) {
			bmsInMapper.insertContainerInfo(container);
		}
		if(carryIn.getInPlanNo() != null) {
			if(!carryIn.getInPlanNo().equals(0)) {
				// 반입예정테이블의 IN_YN(반입등록여부) Y로 함
				updateBmsInPlanInYn(carryIn);
			}
		}

		String inNo = bmsInMapper.selectInNoByCustomsReportNo(carryIn.getCustomsReportNo());
		carryInSaveResult.setInNo(inNo);

		/**작업내역 로그시작*/
		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(carryIn.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(carryIn.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(carryIn.getRegisterUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [입력 BL_NO:"+String.valueOf(carryIn.getBlNo())+"]");
		if("".equals(Util.null2str(carryIn.getOdJobNo()))){
			processLogs.setJobNo(String.valueOf(carryIn.getBlNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다. (odJobNo 없을경우 bl번호 입력)
		}else {
			processLogs.setJobNo(String.valueOf(carryIn.getOdJobNo()));
		}
		processLogs.setJobLog(new Gson().toJson(carryIn));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(carryIn.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(carryIn.getLogUserName());
		processLogs.setProcessUserNameEng(carryIn.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(carryIn.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.INSERT /* pod Status 값 매핑 */
				, ""
				, carryIn
		);
		/**작업내역 로그끝*/

		carryInSaveResult.setResultCode("A012051");// 저장 되었습니다.
		return carryInSaveResult;
	}

	/**
	 * 반입신청 - update
	 * @param carryIn
	 * @param request
	 * @return
	 */
	public CarryInSaveResult updateCarryIn(CarryIn carryIn, HttpServletRequest request) throws Exception {
		CarryInSaveResult carryInSaveResult = new CarryInSaveResult();
		String podRemarks = "";
		if("Y".equals(carryIn.getChangeStatus03Yn())) {
			podRemarks = "전송에서 입력으로 변경";
		}
		int intRow = bmsInMapper.updateCarryIn(carryIn);
		if(!"BMS04".equals(carryIn.getLastPodStatus())) {
			// WT,CT 데이터 업데이트 STOCK_WT 계산
			BmsOut bmsOut = new BmsOut();
			bmsOut.setInNo(carryIn.getInNo());
			bmsOutMapper.updateInCtWtCbm(bmsOut);
		}
		
		//컨테이너 정보 삭제
		int deletedRow = bmsInMapper.deleteContainerInfo(carryIn);
		//컨테이너정보 저장
		List<Container> containerList = new ArrayList<>();
		Container container1 = new Container();
		Container container2 = new Container();
		Container container3 = new Container();

		container1.setCustomsReportNo(carryIn.getCustomsReportNo());
		container1.setCntrNo(carryIn.getCntrNo1());
		container1.setCntrEdiCode(carryIn.getCntrEdiCode1());
		container1.setSealNo1(carryIn.getSealNo1A());
		container1.setSealNo2(carryIn.getSealNo1B());

		container2.setCustomsReportNo(carryIn.getCustomsReportNo());
		container2.setCntrNo(carryIn.getCntrNo2());
		container2.setCntrEdiCode(carryIn.getCntrEdiCode2());
		container2.setSealNo1(carryIn.getSealNo2A());
		container2.setSealNo2(carryIn.getSealNo2B());

		container3.setCustomsReportNo(carryIn.getCustomsReportNo());
		container3.setCntrNo(carryIn.getCntrNo3());
		container3.setCntrEdiCode(carryIn.getCntrEdiCode3());
		container3.setSealNo1(carryIn.getSealNo3A());
		container3.setSealNo2(carryIn.getSealNo3B());

		if(carryIn.getCntrNo1() != "") {
			containerList.add(container1);
		}
		if(carryIn.getCntrNo2() != "") {
			containerList.add(container2);
		}
		if(carryIn.getCntrNo3() != "") {
			containerList.add(container3);
		}

		for (Container container: containerList) {
			bmsInMapper.insertContainerInfo(container);
		}
		
		// 10.입항반입인 경우 실행
		// 11.무적화물반입 , 23.일괄구매물품B/L분할반입 추가 2026.02.11 2sowell
		if("10".equals(carryIn.getInClass()) || "11".equals(carryIn.getInClass()) || "23".equals(carryIn.getInClass())) {
			Map<String, Object> outPlanNoMap = bmsInMapper.selectCargoByOutPlanNo(carryIn);  // 반출승인자료
			
			// 반출승인자료가 있다면
			if(outPlanNoMap != null) {
				Integer outPlanNo = (Integer) outPlanNoMap.get("OUT_PLAN_NO");	            
		        if(outPlanNo != null) {
		            carryIn.setOutPlanNo(outPlanNo);
		            bmsInMapper.updateNo(carryIn);  // 반입번호,BL,MBL 업데이트
		            carryIn.setIsPrecUpdated(true); 
		        }
			}
		}

		/**작업내역 로그시작*/
		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(carryIn.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(carryIn.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(carryIn.getRegisterUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [수정 IN_NO:"+String.valueOf(carryIn.getInNo())+"]");
		processLogs.setJobNo(String.valueOf(carryIn.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(carryIn));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(carryIn.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(carryIn.getLogUserName());
		processLogs.setProcessUserNameEng(carryIn.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(carryIn.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
				, podRemarks
				, carryIn
		);
		/**작업내역 로그끝*/

		carryInSaveResult.setResultCode("A012051"); // 저장 되었습니다.
		return carryInSaveResult;
	}

	/**
	 * 반입신고팝업- 재고 - update
	 * @param carryIn
	 * @param request
	 * @return
	 */
	public CarryInSaveResult updateCarryInStock(CarryIn carryIn, HttpServletRequest request) throws Exception {
		CarryInSaveResult carryInSaveResult = new CarryInSaveResult();
		
		// WT,CT 데이터 업데이트 STOCK_WT 계산
		BmsOut bmsOut = new BmsOut();
		bmsOut.setInNo(carryIn.getInNo());
		bmsOutMapper.updateInCtWtCbm(bmsOut);
		
		/**작업내역 로그시작*/
		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(carryIn.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(carryIn.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(carryIn.getRegisterUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [수정 IN_NO:"+String.valueOf(carryIn.getInNo())+"]");
		processLogs.setJobNo(String.valueOf(carryIn.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(carryIn));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(carryIn.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(carryIn.getLogUserName());
		processLogs.setProcessUserNameEng(carryIn.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(carryIn.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
				, "재고수량 갱신"
				, carryIn
		);
		/**작업내역 로그끝*/

		carryInSaveResult.setResultCode("A012051"); // 저장 되었습니다.
		return carryInSaveResult;
	}


	/**
	 * 반입신청 BL번호로 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public CarryInDetailList selectCarryInByBlNo(SearchInList search) throws Exception {
		CarryInDetailList list = new CarryInDetailList();

		search.setPageSize(50);
		search.setPage(1);
		search.setDefaultSort(" BL_NO DESC ");
		setSearchCondition(search);

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|AR_DATE|CARRYIN_DATE|OUT_NOTICE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO|PKG_NO");
		List<CarryIn> carryInList  = bmsInMapper.selectCarryInList(search);
		if(carryInList.size() > 0) {
			list.setTotal(carryInList.get(0).getTotal());
		}else {
			list.setTotal(0);
		}
		list.setCarryInList(carryInList);

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|AR_DATE|CARRYIN_DATE|OUT_NOTICE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO|PKG_NO");
		List<InPlan> inPlanList = bmsInMapper.selectInPlanList(search);
		if(inPlanList.size() > 0) {
			list.setTotalInPlan(inPlanList.get(0).getTotal());
		}else {
			list.setTotal(0);
		}
		list.setInPlanList(inPlanList);
		return list;
	}

	public CarryInDetailList selectCarryInBySrchNo(SearchInList search) throws Exception {
		CarryInDetailList list = new CarryInDetailList();
		search.setPageSize(50);
		search.setPage(1);
		search.setDefaultSort(search.getSrchNoType());
		setSearchCondition(search);

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|AR_DATE|CARRYIN_DATE|OUT_NOTICE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO|PKG_NO");
		List<CarryIn> carryInList  = bmsInMapper.selectCarryInList(search);
		if(carryInList.size() > 0){
			list.setTotal(carryInList.get(0).getTotal());
			list.setCarryInList(carryInList);
			
			list.setTotalInPlan(0);
		}else {
			list.setTotal(0);
			
			//엔트리에서 반입신고번호로 조회시 반입예정 자료는 검색하지 않음
			if(!search.getSrchNoType().equals("CUSTOMS_REPORT_NO")) {
				List<InPlan> inPlanList = bmsInMapper.selectInPlanList(search);
				if(inPlanList.size() > 0) {
					list.setTotalInPlan(inPlanList.get(0).getTotal());
					list.setInPlanList(inPlanList);
				}else {
					list.setTotalInPlan(0);
				}
			}else{
				list.setTotalInPlan(0);
			}
		}

		return list;
	}
	
	/**
	 * 반입신청 - update
	 * @param carryIn
	 * @param request
	 * @return
	 */
	public Map<String, Object> check632isSended(CarryIn carryIn) throws Exception {
		Map<String, Object> res = new HashMap<String, Object>();
		if(!"10".equals(carryIn.getInClass())) {
			res.put("sendYn", "N");
			return res;
		}
			
		Map<String, Object> podParam = new HashMap<String, Object>();
		podParam.put("odJobNo", carryIn.getOdJobNo());
		List<BmsEcmPod> podList = bmsInMapper.selectPodSelect(podParam);
		boolean isSended = podList.stream()
				.anyMatch(pod -> carryIn != null && Util.null2str(pod.getRemarks()).equals(carryIn.getCustomsReportNo()) && Util.null2str(pod.getPodName()).endsWith("/C200"));
	
		res.put("sendYn", isSended ? "Y" : "N");
		return res;
	}

	private static void setSearchCondition(SearchInList search) throws Exception {
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){
			// 가상 필드 다른 이름으로 매핑 처리 cargoReferenceNoDisp,
			List<Sort> lst = search.getSort();
			if( lst != null) {
				for (Sort sort : lst) { // 가상필드 다른 이름으로 처리
					if (sort.getField().equals("cargoReferenceNoDisp")) {
						sort.setField("cargoReferenceNo");
					}
				}
			}
			Util.camelCaseToUnderscore(search);
		}
	}

	private void updateBmsInPlanInYn(CarryIn carryIn) {
		InPlan inPlan = new InPlan();
		inPlan.setInPlanNo(carryIn.getInPlanNo());
		inPlan.setInYn("Y");
		inPlan.setUpdateUserId(carryIn.getUpdateUserId());
		inPlan.setGmtTime(carryIn.getGmtTime());
		inPlan.setDateFormat(carryIn.getDateFormat());
		bmsInMapper.updateBmsInPlanInYn(inPlan);
	}

	public int checkCargoReferenceNo(String cargoReferenceNo) {
		int cnt = bmsInMapper.checkCargoReferenceNo(cargoReferenceNo);
		return cnt;
	}

	public CarryInSaveResult deleteInPlan(DeleteInPlan delete, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();

		InPlan inPlan;
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		int notDeletedCnt = 0;
		int deletedCnt = 0;
		for (Map<String, Object> deleteMap : deleteList) {
			//삭제전 체크
			InPlanCheckResult inPlanCheckResult = bmsInMapper.checkInPlanBeforeDelete(Util.null2str(deleteMap.get("inPlanNo")));
			String inYn = inPlanCheckResult.getInYn();
			int xrayResultCount = inPlanCheckResult.getXrayResultCount();
			int existsOutPlanCount = inPlanCheckResult.getExistsOutPlanCount();
			if(inYn.equals("Y") || xrayResultCount > 0 || existsOutPlanCount > 0) {
				notDeletedCnt++;
			}else {
				inPlan = new InPlan();
				inPlan.setInPlanNo(Integer.parseInt(Util.null2str(deleteMap.get("inPlanNo"))));

				bmsInMapper.deleteInPlan(inPlan);
				bmsInMapper.deleteInPlanPkg(inPlan);
				
				String odJobNo = Util.null2str(deleteMap.get("odJobNo"));
				if(!"".equals(odJobNo) && !odJobNo.equals( Util.null2str(deleteMap.get("blNo")) )) {
					Map<String, Object> odUpdateParam = new HashMap<String, Object>();
					odUpdateParam.put("odJobNo", odJobNo);
					odUpdateParam.put("bmsInPlanYn", "N");
					bmsInMapper.updateBmsInPlanYn(odUpdateParam);
				}
				
				//deletedCnt(변수삭제카운트) count++
				deletedCnt++;

				/**작업내역 로그시작*/
				// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(delete.getEntCode()));
				processLogs.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
				processLogs.setLogJobType("DATA");
				processLogs.setBound("AIR");
				processLogs.setBoundType("IMP");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN_PLAN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN_PLAN+"[삭제 IN_PLAN_NO:" + String.valueOf(deleteMap.get("inPlanNo")) + ", 삭제사유:" + delete.getReason() + "]");
				processLogs.setJobNo(String.valueOf(deleteMap.get("odJobNo")));
				processLogs.setJobLog(new Gson().toJson(deleteMap));
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(delete.getLogUserName());
				processLogs.setProcessUserNameEng(delete.getLogUserNameEng());
				//	String podStatus = BmsConsts.PodStatus.BMS99;
				String blNo = String.valueOf(deleteMap.get("blNo"));
				// 호출시 필요 항목 : BL_NO, POD_STATUS
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.DELETE /* pod Status 값 매핑 */
						, delete.getReason()
						, null
				);
			}
		}
		log.info("delete cnt:{}", deletedCnt);
		log.info("not delete cnt:{}", notDeletedCnt);
		result.setNotDeletedCnt(notDeletedCnt);
		result.setDeletedCnt(deletedCnt);
		return result;
	}

	public CarryInSaveResult deleteCarryIn(DeleteCarryIn delete, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();

		CarryIn carryIn;
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		int notDeletedCnt = 0;
		int deletedCnt = 0;
		SearchInList searchInList = new SearchInList();
		searchInList.setSrchType("carryInDel");
		for (Map<String, Object> deleteMap : deleteList) {
			searchInList.setInNo(Util.null2str(deleteMap.get("inNo")));
			carryIn = bmsInMapper.selectDetailCarryIn(searchInList);
			log.info("ParaminNo: {}",Integer.parseInt(Util.null2str(deleteMap.get("inNo"))));
			if(null != carryIn) {
				carryIn.setInPlanNo(Integer.parseInt(Util.null2str(deleteMap.get("inPlanNo"))));
				carryIn.setInNo(Integer.parseInt(Util.null2str(deleteMap.get("inNo"))));
				if(("BMS01".equals(carryIn.getLastPodStatus()) || "BMS02".equals(carryIn.getLastPodStatus()) || "BMS05".equals(carryIn.getLastPodStatus())) 
					&& Util.null2int(carryIn.getXrayScanCt()) == 0 && Util.null2int(carryIn.getXrayImageCt()) == 0 && Util.null2dbl(carryIn.getStockWt()) != 0 && Util.null2int(carryIn.getOutNo()) == 0) {
					log.info("delete carryIn:{}", carryIn);
					bmsInMapper.deleteCarryIn(carryIn);
					bmsInMapper.deleteContainerInfo(carryIn);
					deletedCnt++;

					//삭제 후 반입예정의 IN_YN 'C' 로 변경
					if(carryIn.getInPlanNo() != 0) {
						// N말고 C로 업데이트하는것으로 수정 2025.11.21 2sowell (반입계속 생성되는 문제를 막고 EC에서 반입예정 삭제로 내려서 삭제하려는 용도)
						// 판토스 전용 분기 제거하고 UPS(04077009) 포함 공통으로 C 업데이트 2026.09.22 (UPS도 반입삭제후 데몬이 반입을 재생성/재신고하는 문제 발생)
						String updateUserIdBackup = carryIn.getUpdateUserId();
						carryIn.setUpdateUserId(delete.getInputUserId());
						bmsInMapper.updateInPlanInYnC(carryIn);		// IN_YN 'C'로 업데이트
						
						carryIn.setUpdateUserId(updateUserIdBackup);
					}
					
					if("PANTK".equals(Util.null2str(delete.getEntCode())) && "21".equals(carryIn.getInClass())) {
						CarryIn firstBmsIn = bmsInMapper.selectFirstBmsInByCargoNo(carryIn);
						
						Map<String, Object> pkgParam = new HashMap<String, Object>();
						pkgParam.put("inPlanNo", firstBmsIn.getInPlanNo()); 
						pkgParam.put("moveInCt", 0);
						pkgParam.put("updateUserId", delete.getInputUserId());
						bmsInMapper.updatePkgTicMoveInByInPlanNo(pkgParam);
					}

					/**작업내역 로그시작*/
					// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(delete.getEntCode()));
					processLogs.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
					processLogs.setLogJobType("DATA");
					processLogs.setDbType(BmsConsts.DbType.BMS_IN);
					processLogs.setBound("AIR");
					processLogs.setBoundType("IMP");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
					processLogs.setDescription(BmsConsts.DbType.BMS_IN+"[삭제 IN_NO:" + String.valueOf(deleteMap.get("inNo")) + ", 삭제사유:" + delete.getReason() + "]");
					processLogs.setJobNo(String.valueOf(deleteMap.get("odJobNo")));
					processLogs.setJobLog(new Gson().toJson(deleteMap));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
					processLogs.setProcessUserName(delete.getLogUserName());
					processLogs.setProcessUserNameEng(delete.getLogUserNameEng());
					//	String podStatus = BmsConsts.PodStatus.BMS99;
					String blNo = String.valueOf(deleteMap.get("blNo"));
					// 호출시 필요 항목 : BL_NO, POD_STATUS
					bmsCommService.insertLogAndPodStatus(processLogs, request
							, BmsConsts.PodStatus.DELETE /* pod Status 값 매핑 */
							, delete.getReason()
							, null
					);
					/**작업내역 로그끝*/
				}else {
					notDeletedCnt++;
				}
			}else {
				notDeletedCnt++;
			}
		}
		log.info("delete cnt:{}", deletedCnt);
		log.info("not delete cnt:{}", notDeletedCnt);
		result.setNotDeletedCnt(notDeletedCnt);
		result.setDeletedCnt(deletedCnt);
		return result;
	}

	public CarryInSaveResult deleteBox(DeleteCarryIn delete, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();

		int deletedCnt = 0;
		int notDeletedCnt = 0;
		
		/** 삭제불가 사유별 박스번호 (박스조회 팝업 박스추가/박스변경 조건 참고) */
		List<String> alreadyDeletedPkgList = new ArrayList<String>();	// 삭제처리된 박스번호
		List<String> deliveryScanPkgList = new ArrayList<String>();		// 이미 택배스캔된 박스번호 (판토스)
		List<String> alreadyInPkgList = new ArrayList<String>();		// 이미 입고된 박스번호 (IN_CT > 0)
		List<String> notExistPkgList = new ArrayList<String>();			// 존재하지 않는 박스번호
		
		List<Map<String, Object>> deleteList = delete.getDataList();
		for (Map<String, Object> deleteMap : deleteList) {
			/** 삭제전 체크 1) 존재하지 않는 박스 (박스추가/박스변경 : 존재하지않는 BL 혹은 박스입니다.) */
			InPlanPkg inPlanPkg = null;
			if (Util.null2int(deleteMap.get("seq")) > 0) {
				inPlanPkg = bmsInMapper.selectInPlanPkgBySeq(deleteMap);
			}
			if (inPlanPkg == null) {
				notExistPkgList.add(Util.null2str(deleteMap.get("packingNo")));
				notDeletedCnt++;
				continue;
			}
			String packingNo = Util.null2str(inPlanPkg.getPackingNo());
			
			/** 삭제전 체크 2) 삭제처리된 박스 (박스변경 : 삭제처리된 박스번호는 삭제할 수 없습니다.) */
			if ("Y".equals(Util.null2str(inPlanPkg.getDeleteYn()))) {
				alreadyDeletedPkgList.add(packingNo);
				notDeletedCnt++;
				continue;
			}
			
			/** 삭제전 체크 3) 판토스 택배스캔된 박스 (박스변경 : 이미 택배스캔된 박스번호는 삭제할 수 없습니다.) */
			if ("PANTK".equals(Util.null2str(delete.getEntCode()))) {
				Map<String, Object> chkMap = new HashMap<String, Object>();
				chkMap.put("packingNo", packingNo);
				if (bmsPkgNoMapper.checkBoxDeliveryOutScanByPackingNo(chkMap) > 0) {
					deliveryScanPkgList.add(packingNo);
					notDeletedCnt++;
					continue;
				}
			}
			
			/** 삭제전 체크 4) 입고된 박스 (IN_CT가 1인 입고된 박스번호는 삭제할 수 없습니다.) */
			if (Util.null2int(inPlanPkg.getInCt()) > 0) {
				alreadyInPkgList.add(packingNo);
				notDeletedCnt++;
				continue;
			}
			
			deleteMap.put("updateUserId", delete.getInputUserId());
			int updatedRows = bmsInMapper.softDeleteInPlanPkg(deleteMap);
			if (updatedRows == 0) {
				notExistPkgList.add(packingNo);
				notDeletedCnt++;
				continue;
			}
			deletedCnt++;
			
			/**작업내역 로그시작*/
			// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(delete.getEntCode()));
			processLogs.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
			processLogs.setLogJobType("DATA");
			processLogs.setDbType(BmsConsts.DbType.BMS_IN_PLAN_PKG);
			processLogs.setBound("AIR");
			processLogs.setBoundType("IMP");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
			processLogs.setDescription(BmsConsts.DbType.BMS_IN_PLAN_PKG+"[삭제 TB_BMS_IN_PLAN_PKG SEQ :" + String.valueOf(deleteMap.get("seq")) + ", 삭제사유:" + delete.getReason() + "]");
			processLogs.setJobNo(String.valueOf(deleteMap.get("odJobNo")));
			processLogs.setJobLog(new Gson().toJson(deleteMap));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(delete.getLogUserName());
			processLogs.setProcessUserNameEng(delete.getLogUserNameEng());
			//	String podStatus = BmsConsts.PodStatus.BMS99;
			String blNo = String.valueOf(deleteMap.get("blNo"));
			// 호출시 필요 항목 : BL_NO, POD_STATUS
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, BmsConsts.PodStatus.DELETE /* pod Status 값 매핑 */
					, "("+Util.null2str(deleteMap.get("packingNo"))+") " + delete.getReason()
					, null
			);
		}
		log.info("delete cnt:{}", deletedCnt);
		log.info("not delete cnt:{}", notDeletedCnt);
		
		/** 삭제불가 사유 (프론트 알림용) */
		List<String> notDeletedReasonList = new ArrayList<String>();
		addBoxNotDeletedReason(notDeletedReasonList, "삭제처리된 박스번호", alreadyDeletedPkgList);
		addBoxNotDeletedReason(notDeletedReasonList, "이미 택배스캔된 박스번호", deliveryScanPkgList);
		addBoxNotDeletedReason(notDeletedReasonList, "이미 입고된 박스번호", alreadyInPkgList);
		addBoxNotDeletedReason(notDeletedReasonList, "존재하지 않는 박스번호", notExistPkgList);
		result.setNotDeletedReasonList(notDeletedReasonList);
		
		result.setNotDeletedCnt(notDeletedCnt);
		result.setDeletedCnt(deletedCnt);
		return result;
	}
	
	/** 박스삭제 불가 사유 문구 생성 ("- 사유 : 박스번호, 박스번호"), 박스번호는 HTML 이스케이프 */
	private void addBoxNotDeletedReason(List<String> reasonList, String reason, List<String> packingNoList) {
		if (packingNoList.isEmpty()) {
			return;
		}
		String packingNos = packingNoList.stream()
				.map(StringEscapeUtils::escapeHtml4)
				.collect(Collectors.joining(", "));
		reasonList.add("- " + reason + " : " + packingNos);
	}
	
	public Map<String, Object> checkIsXrayTarget(Map<String, Object> grid) {
		List<Map<String, Object>> xrayHistoryList = bmsInMapper.getXrayHistoryList(grid);
		List<Map<String, Object>> inList = (List<Map<String, Object>>) grid.get("inList");
		List<Map<String, Object>> bmsInInspectionCodeList = bmsInMapper.getBmsInWithTicInspectionCodeFromInList(grid);
		
		/* 그리드 select해서 최신화 */
		for (Map<String, Object> _in : inList) {
			for(Map<String, Object> bmsInInspectionCode : bmsInInspectionCodeList) {
				long inPlanNo = Util.obj2longOrZero(_in.get("inPlanNo"));
				if( inPlanNo != 0 && inPlanNo == Util.obj2longOrZero(bmsInInspectionCode.get("IN_PLAN_NO")) ) {
					_in.put("seInspectionCode", Util.null2str(bmsInInspectionCode.get("SE_INSPECTION_CODE")));
					_in.put("inspectionType", Util.null2str(bmsInInspectionCode.get("INSPECTION_TYPE")));
					_in.put("inspectionTypeA", Util.null2str(bmsInInspectionCode.get("INSPECTION_TYPE_A")));
					_in.put("inspectionTypeB", Util.null2str(bmsInInspectionCode.get("INSPECTION_TYPE_B")));
					_in.put("xrayInspectionCode", Util.null2str(bmsInInspectionCode.get("XRAY_INSPECTION_CODE")));
					_in.put("paln5emInspectionCode", Util.null2str(bmsInInspectionCode.get("PALN5EM_INSPECTION_CODE")));
					_in.put("bigSizeYn", Util.null2str(bmsInInspectionCode.get("BIG_SIZE_YN")));
					_in.put("crimeSelectionYn", Util.null2str(bmsInInspectionCode.get("CRIME_SELECTION_YN")));
					_in.put("intellectualPropYn", Util.null2str(bmsInInspectionCode.get("INTELLECTUAL_PROP_YN")));
					_in.put("inspectionCustomsYn", Util.null2str(bmsInInspectionCode.get("INSPECTION_CUSTOMS_YN")));
				}
			}
		}
		
		for (Map<String, Object> _in : inList) {
			for(Map<String, Object> xrayHistory : xrayHistoryList) {
				long inPlanNo = Util.obj2longOrZero(_in.get("inPlanNo"));
				if( inPlanNo != 0 && inPlanNo == Util.obj2longOrZero(xrayHistory.get("IN_PLAN_NO")) ) {
					_in.put("xrayPassTime", Util.null2str(xrayHistory.get("XRAY_PASS_TIME")));
				}
			}
		}
		
		/* 판독일시 */
		
		for (Map<String, Object> _in : inList) {
			if( StringUtils.isEmpty(Util.null2str(_in.get("xrayInspectionCode"))) ) {
				if(StringUtils.isEmpty(Util.null2str(_in.get("xrayPassTime")))) {
					if( "Y".equals(Util.null2str(_in.get("bigSizeYn"))) ){
						_in.put("xrayPassResult", "판독대상아님");
					}else {
						_in.put("xrayPassResult", "미판독");
					}
				}else {
					_in.put("xrayPassResult", "이상없음");
				}
			}
		}
		
		grid.put("inList", inList);
		return grid;
	}
	
	public CarryInSaveResult checkCarryInBatchTicCount(BatchCarryIn batchCarryIn, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		List<Integer> inNoList = batchCarryIn.getInList().stream()
							.map(CarryIn::getInNo)
							.collect(Collectors.toList());
		
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("inNoList", inNoList);
		Integer nTicCnt = 0;
		if(inNoList != null && inNoList.size() > 0) {
			nTicCnt = bmsInMapper.checkCarryInBatchTicCount(paramMap); //이고대상이 아닌 건수
			// 주석처리 2026-01-09 yjkim
//			for(Integer inNo : inNoList) {
//				
//				Map<String, Object> param = new HashMap<>();
//				param.put("inNo", inNo);
//				Integer nTicCnt2 = bmsInMapper.checkCarryInBatchTicCount2(param); 
//				
//				if(nTicCnt2 == 0) { //이고 검사건이 완료된건 OR 이고검사건이 없는건이 있으면 이고대상건이 아닌 건수에 추가  2026-01-09 yjkim
//					nTicCnt ++;
//				}
//			}
		}
		
		String bmsWhCode = batchCarryIn.getBmsWhCode();
		if(!"04077009".equals(bmsWhCode)) {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> batchCarryInMap = objectMapper.convertValue(batchCarryIn, Map.class);
			Map<String, Object> returnMap = checkIsXrayTarget(batchCarryInMap);
			result.setReturnMap(returnMap);
		}
		
		
		
		result.setResultCnt(nTicCnt);
		return result;
	}

	public CarryInSaveResult  executeBatchCarryIn(BatchCarryIn batchCarryIn, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		String batchExecType = batchCarryIn.getBatchExecType(); //DISP,OUT
		String inputUserId = batchCarryIn.getInputUserId();
		String dateTimeFm =  batchCarryIn.getDateFormat();
		String gmtTime     = batchCarryIn.getGmtTime();
		List<String> returnLst = new ArrayList<>();
		List<String> outLst = new ArrayList<>();
		List<String> stockLst = new ArrayList<>();
		Integer nResultCnt = 0;
		int nextTicOutSeq = 0;
		// blList
		if( "OUT".equals(batchExecType)) { // IN => OUT 반입 => 반출 처리 특송장 이고 반출입 생성
			String bmsWhCode = batchCarryIn.getBmsWhCode();
			
			//오늘, 반출에서 이고차수 최대값가져오기
			int maxTicOutSeq = bmsInMapper.selectTodayMaxTicOutSeqFromBmsOut();
			nextTicOutSeq = maxTicOutSeq + 1; // 이고반출에 저장할 이고차수 = 반출에있는 최대 이고차수 + 1
			
			for (CarryIn in : batchCarryIn.getInList()) {
				String logVal = "batch  IN => OUT  batchCarryIn:" + in;
				log.info(logVal);
				// 그리드 값이 - 가 표시될 가능성이 있으므로 정리
				in.setCargoReferenceNo(in.getCargoReferenceNo().replaceAll("-",""));
				//1) TB_BMS_IN.LAST_POD_STATUS 'BWH0' 접수 리스트 확인
				in.setRegisterUserId(inputUserId);
				in.setUpdateUserId(inputUserId);
				Integer nCnt =  bmsInMapper.selectCarryInBatchCheckReceiptCount(in);
				Integer nOutCnt = bmsInMapper.selectCarryInBatchCheckOutCount(in);
				Integer nTicInCnt = 0; // 이고반입확인cnt
				Integer sumFinalBmsOutOrPlan = 1; // 완전출고sum: 결과가 NULL이면 이고반출가능, 결고가 NULL이 아니면 완전출고상태
				
				boolean willCheckStock = true; // 재고 체크할지 여부
				if("04077248".equals(bmsWhCode)) { // pantos 만
					nTicInCnt = bmsInMapper.selectCheckBmsIn21Count(in); // 화물관리번호에 해당하는 재고가있는 이고반입
					
					//nTicInCnt > 0 이상이면 이고반출 생성이 됨.
					if(nTicInCnt == 0) { // 1차 이고반출시
						
						Integer carryInBigsizeCnt = bmsInMapper.checkCarryInBigsizeCount(in); // 거대, 지재권, 집중검사가 전부 N이면 count>0 인 값이 된다
						Integer nTicCnt2 = bmsInMapper.checkCarryInBatchTicCount2(in); 
						log.info("carryInBigsizeCnt ==========" + carryInBigsizeCnt);
						log.info("sumFinalBmsOutOrPlan0 ==========" + nTicCnt2);
						if( Util.null2int(nTicCnt2) == 0 && Util.null2int(carryInBigsizeCnt) > 0) {
							nOutCnt = 1;
							nTicInCnt = 0;
						}
						
					}else if(nTicInCnt > 0) { // 2차 이고반출시
						Integer nTicCnt2 = bmsInMapper.checkCarryInBatchTicCount2(in); 
						log.info("sumFinalBmsOutOrPlan0 ==========" + nTicCnt2);
						if( Util.null2int(nTicCnt2) == 0) {
							nOutCnt = 1;
							nTicInCnt = 0;
						}
					}
					
					Map<String, Object> inParam = new HashMap<>();
					inParam.put("cargoReferenceNo", in.getCargoReferenceNo());
					sumFinalBmsOutOrPlan = bmsInMapper.sumWhenFinalBmsOutOrPlan(inParam); // 화물관리번호에 해당하는 완전출고된것들을 COUNT (하지만 이고혹은 부분출고승인 혹은 부분출고는 no count)
					log.info("sumFinalBmsOutOrPlan {}", sumFinalBmsOutOrPlan);
					if(sumFinalBmsOutOrPlan == null) {
						sumFinalBmsOutOrPlan = 0;
					}
					log.info("sumFinalBmsOutOrPlan2 ==========" + nTicInCnt);
					log.info("sumFinalBmsOutOrPlan ==========" + sumFinalBmsOutOrPlan);
					if(nTicInCnt > 0 && sumFinalBmsOutOrPlan == 0) { // 재고가있는 21이고반입이 존재하고 화물관리번호에 해당하는 완전출고된것들 존재하지않을 때
						
						nOutCnt = 0; // 이고반출 중복체크값을 0으로해서 이고반출 중복체크 X
						willCheckStock = false; // 재고 재고체크 X
					}
					
				}
				
				//2) 1에서 자료있으면 list에 넣어서  리스트 제외 함
				if(nCnt >0){ // BL 번호로 존재 하면 // 카운트에서 제외 처리
					returnLst.add(in.getBlNo()); // 중복 체크
				}else if(nOutCnt >0) {
					outLst.add(in.getBlNo()); // 중복 체크
				}else if(willCheckStock && bmsInMapper.selectCarryInBatchCheckStockCount(in) > 0){
					stockLst.add(in.getBlNo());
				}
				else {
					SpCallVo voIn = bmsDisposalService.getBmsOutCustomsReportNo(bmsWhCode);
					//3) TB_BMS_DISPOSAL 에서 TB_BMS_OUT로 데이터 입력
					in.setCustomsReportNo(voIn.getRetCustomsReportNo());
					in.setNextTicOutSeq(nextTicOutSeq);
					
					if("04077248".equals(bmsWhCode)) { // pantos 만 (이고관련 반입/반출 백업및 이고상태 초기화)
						
						
						if(nTicInCnt > 0 && sumFinalBmsOutOrPlan == 0) {  // 재고가있는 21이고반입이 존재하고 완전출고된것들 존재하지않을때 (BMSA3등의 출고는 완전출고임)
							Map<String, Object> ticParam = new HashMap<>();
							ticParam.put("cargoReferenceNo", in.getCargoReferenceNo());
							
							bmsInMapper.insertTicInByCargoNo(ticParam); // 화물관리번호에 해당하는 21번 이고반입을 history에 insert
							
							bmsInMapper.insertTicOutByCargoNo(ticParam); // 화물관리번호에 해당하는 61번 이고반출을 history에 insert
							
							Map<String, Object> ticPkgInitParam = new HashMap<>();
							ticPkgInitParam.put("inPlanNo", in.getInPlanNo());
							ticPkgInitParam.put("updateUserId", inputUserId);
							bmsInMapper.updateTicPkgInit(ticPkgInitParam); // IN_PLAN_NO에 해당하는 박스의 이고관련 칼럼 초기화 update
							
							Map<String, Object> ticInitParam = new HashMap<>();
							ticInitParam.put("inNo", in.getInNo());
							ticInitParam.put("blNo", in.getBlNo());
							ticInitParam.put("updateUserId", inputUserId);
							
							//검사결과 테이블 초기화 안함 2026-01-09 yjkimm
							//bmsInMapper.deleteOnlyTicInspectResult(ticInitParam); // 반입에 해당하는 이고관련 검사결과만 전부 삭제
							//this.insertAllTicInspectResultByBmsIn(in, inputUserId); //반입에 등록된 이고검사건 TB_BMS_INSPECT_RESULT N으로 INSERT
							bmsInMapper.updateTicLogFinishedY(ticInitParam);
							
							bmsInMapper.updateTicInInit(ticInitParam); // 반입번호에 해당하는 반입 이고관련 칼럼 초기화 TB_BMS_IN UPDATE
							
							Map<String, Object> calcStockParam = new HashMap<>();
							calcStockParam.put("inNo", in.getInNo());
							calcStockParam.put("updateUserId", inputUserId);
							bmsInMapper.deleteTicIn(ticParam); // 화물관리번호에 해당하는 21번 이고반입 삭제 DELETE
							bmsInMapper.deleteTicOut(ticParam); // 화물관리번호에 해당하는 61번 이고반출 삭제 DELETE
							
							// 반입번호로 반출데이터를 SUM해서 반입 관련 STOCK UPDATE
							bmsInMapper.updateCalcBmsInStockWithBmsOut(calcStockParam);
						}
						
						bmsInMapper.insertCarryInOutBatchGenPantk(in);
					}else {
						bmsInMapper.insertCarryInOutBatchGen(in);
					}
					
					// 3.5) BMS_OUT IN_NO group 하여 BMS_IN에 SUM(OUT_CT), SUM(OUT_WT), SUM(OUT_CBM)
					bmsInMapper.updateCarryInCtWtCbm(in);
					nResultCnt++; // 성공 카운트

					// 배차예정정보(TB_BMS_ALLOCATE_CAR_NOTI) 저장
					Map<String, Object> tmpMap = new HashMap<String, Object>();
					tmpMap.put("bmsWhCode", bmsWhCode);
					tmpMap.put("registerUserId", inputUserId);
					tmpMap.put("updateUserId", inputUserId);
					bmsInMapper.insertBmsAllocateCarNotiByOutClass(tmpMap);

					// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(batchCarryIn.getEntCode()));
					processLogs.setEntBusinessCode(Util.null2str(batchCarryIn.getEntBusinessCode()));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(inputUserId));
					processLogs.setLogJobType("DATA");
					processLogs.setBound("AIR");
					processLogs.setBoundType("IMP");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
					processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
					processLogs.setDescription(BmsConsts.DbType.BMS_OUT+" [입력 CARGO_REFERENCE_NO:"+ in.getCargoReferenceNo() +"]");
					processLogs.setJobNo(String.valueOf(in.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
					processLogs.setJobLog(new Gson().toJson(in));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(in.getLogUserBusinessPrintName());
					processLogs.setProcessUserName(in.getLogUserName());
					processLogs.setProcessUserNameEng(in.getLogUserNameEng());
					//	String podStatus = BmsConsts.PodStatus.BMS99;
					// 호출시 필요 항목 : BL_NO, POD_STATUS
					bmsCommService.insertLogAndPodStatus(processLogs, request
							, BmsConsts.PodStatus.BATCH /* pod Status 값 매핑 61로만 입력되어 들어 온다.  */
							, ""
							, null
					);

				}
			}
		}
		result.setReturnList(returnLst);
		result.setReturnOutList(outLst);
		result.setReturnStockList(stockLst);
		result.setResultCnt(nResultCnt);
		result.setNextTicOutSeq(nextTicOutSeq);
		return result;
	}
	
	public void insertAllTicInspectResultByBmsIn(CarryIn bmsIn, String userId) {
		List<String> ticInspectionTypeList = bmsCommMapper.selectInspectionTypeByTicInspectResultCode(); // 검사결과TB_BMS_INSPECT_RESULT 코드타입 (이고) 리스트
		String[] fields = { // 이고가 존재할수있는 검사코드만
    		    !"".equals(Util.null2str(bmsIn.getSeInspectionCode())) ? "S" + bmsIn.getSeInspectionCode() : "",
    		    !"".equals(Util.null2str(bmsIn.getPaln5emInspectionCode())) ? "P" + bmsIn.getPaln5emInspectionCode() : "",
    		    !"".equals(Util.null2str(bmsIn.getXrayInspectionCode())) ? "X" + bmsIn.getXrayInspectionCode() : "",
    		    //"Y".equals(Util.null2str(bmsIn.getCrimeSelectionYn())) ? "C" : "",
    		    "Y".equals(Util.null2str(bmsIn.getIntellectualPropYn())) ? "T" : "",
    		    "Y".equals(Util.null2str(bmsIn.getInspectionCustomsYn())) ? "A" : "",
    		    "Y".equals(Util.null2str(bmsIn.getBigSizeYn())) ? "B" : ""
    		};
        	
		List<String> inspectionOfbmsInList  = Arrays.stream(fields)
		                               .filter(inspectionOfbmsIn -> !inspectionOfbmsIn.isEmpty())
		                               .filter(inspectionOfbmsIn -> ticInspectionTypeList.stream().anyMatch( ticInspectionType -> ticInspectionType.equals(Util.null2str(inspectionOfbmsIn)) ) ) // 이고검사건만
		                               .collect(Collectors.toList());
		
		Map<String, Object> insertParam = new HashMap<String, Object>();
    	insertParam.put("inNo", bmsIn.getInNo());
    	insertParam.put("cargoReferenceNo", bmsIn.getCargoReferenceNo());
    	insertParam.put("inspectionFinishUserId", userId); // REGISTER_USER_ID항목이랑 맴핑됨
		for(String inspectionOfbmsIn : inspectionOfbmsInList ) {
        	insertParam.put("inspectionType", inspectionOfbmsIn);
        	bmsInMapper.insertInspectResult(insertParam); // TB_BMS_INSPECT_RESULT
		}
	}
	
	public CarryInSaveResult  executeBatchCarryIn70(BatchCarryIn batchCarryIn, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		String inputUserId = batchCarryIn.getInputUserId();
		List<String> returnLst = new ArrayList<>();
		List<String> outLst = new ArrayList<>();
		List<String> stockLst = new ArrayList<>();
		Integer nResultCnt = 0;
		// blList
		String bmsWhCode = batchCarryIn.getBmsWhCode();
		for (CarryIn in : batchCarryIn.getInList()) {
			log.info("batch  IN => OUT  batchCarryIn:{}", in);
			// 그리드 값이 - 가 표시될 가능성이 있으므로 정리
			in.setCargoReferenceNo(in.getCargoReferenceNo().replaceAll("-",""));
			//1) TB_BMS_IN.LAST_POD_STATUS 'BWH0' 접수 리스트 확인
			in.setRegisterUserId(inputUserId);
			in.setUpdateUserId(inputUserId);
			
			Integer nOutCnt = bmsInMapper.selectBatchCheckOutCountByOutClass(in);
			//2) 1에서 자료있으면 list에 넣어서  리스트 제외 함
			if(nOutCnt >0) {
				outLst.add(in.getBlNo()); // 중복 체크
			}else if(bmsInMapper.selectCarryInBatchCheckStockCount(in) > 0){
				stockLst.add(in.getBlNo());
			}
			else {
				SpCallVo voIn = bmsDisposalService.getBmsOutCustomsReportNo(bmsWhCode);
				//3) TB_BMS_DISPOSAL 에서 TB_BMS_OUT로 데이터 입력
				in.setCustomsReportNo(voIn.getRetCustomsReportNo());
				//

				bmsInMapper.insertCarryOut70BatchGen(in);
				// 3.5) BMS_OUT IN_NO group 하여 BMS_IN에 SUM(OUT_CT), SUM(OUT_WT), SUM(OUT_CBM)
				bmsInMapper.updateCarryInCtWtCbm(in);
				nResultCnt++; // 성공 카운트

				// POD에 정상적으로 저장되기위해 BmsOut객체에 반출구분을 70으로 셋팅하면 정상적으로 podName이 통과반출(일괄생성) 이된다
				BmsOut out = new BmsOut();
				out.setOutClass("70");

				// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(batchCarryIn.getEntCode()));
				processLogs.setEntBusinessCode(Util.null2str(batchCarryIn.getEntBusinessCode()));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(inputUserId));
				processLogs.setLogJobType("DATA");
				processLogs.setBound("AIR");
				processLogs.setBoundType("IMP");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
				processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
				processLogs.setDescription(BmsConsts.DbType.BMS_OUT+" [입력 CARGO_REFERENCE_NO:"+ in.getCargoReferenceNo() +"]");
				processLogs.setJobNo(String.valueOf(in.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setJobLog(new Gson().toJson(in));
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(in.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(in.getLogUserName());
				processLogs.setProcessUserNameEng(in.getLogUserNameEng());
				//	String podStatus = BmsConsts.PodStatus.BMS99;
				// 호출시 필요 항목 : BL_NO, POD_STATUS
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.BATCH /* pod Status 값 매핑 61로만 입력되어 들어 온다.  */
						, ""
						, out
				);

			}
		}
		result.setReturnList(returnLst);
		result.setReturnOutList(outLst);
		result.setReturnStockList(stockLst);
		result.setResultCnt(nResultCnt);
		return result;
	}
	
	public CarryInSaveResult updateIprFromBmsInAndPlan(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();

		bmsInMapper.updateIprFromBmsIn(listData);
		bmsInMapper.updateIprBmsInPlanWhereHasBmsIn(listData);
		bmsInMapper.updateIprFromBmsInPlan(listData);
		bmsInMapper.updateIprBmsInWhereHasBmsInPlan(listData);
		
		ObjectMapper objectMapper = new ObjectMapper();
		List<Map<String, Object>> inList = objectMapper.convertValue(listData.get("dataList"), new TypeReference<List<Map<String, Object>>>() {});
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("inspectionFinishUserId", Util.null2str(listData.get("updateUserId")));
		paramMap.put("inspectionType", "T");
		
		if(inList != null) {
			for(Map<String, Object> in : inList) {
				if(Util.null2int(in.get("inNo")) > 0) {
					/** 지정시 지재권 검사결과추가 */
					if("Y".equals(listData.get("intellectualPropYn"))) {
						paramMap.put("inNo", Util.null2int(in.get("inNo")));
						paramMap.put("cargoReferenceNo", Util.null2str(in.get("cargoReferenceNo")));
						BmsInspectResult inspectResult = bmsInMapper.selectInspectResultByInNoAndType(paramMap);
						if(inspectResult == null) {
							bmsInMapper.insertInspectResult(paramMap);
						}
						paramMap.put("inspectionFinishYn", "N");
						paramMap.put("inspectionFinishRemarks", "");
						paramMap.put("inspectionFinishUserId", "");
						paramMap.put("inspectionFinishDateTime", "");
						bmsInMapper.updateInspectResultByInNoAndType(paramMap);
					} else {
						/** 지정취소시 지재권 검사결과 완료처리 */
						paramMap.put("inNo", Util.null2int(in.get("inNo")));
						paramMap.put("inspectionFinishYn", "Y");
						paramMap.put("inspectionFinishRemarks", "지재권지정취소로인한완료처리");
						bmsInMapper.updateInspectResultByInNoAndType(paramMap);
					}
				}
			}
		}
		
		List<String> obJobNoList = (List<String>) listData.get("odJobNoList");
		if(obJobNoList != null) {
			CarryIn carryIn = new CarryIn();
			carryIn.setIntellectualPropYn(Util.null2str(listData.get("intellectualPropYn")));
			for(String obJobNo : obJobNoList) {
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(listData.get("entCode")));
				processLogs.setEntBusinessCode(Util.null2str(listData.get("entBusinessCode")));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(listData.get("updateUserId")));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [지재권 업데이트]");
				processLogs.setJobLog(new Gson().toJson(listData));
				processLogs.setJobNo(String.valueOf(obJobNo));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setResult("ok");
				processLogs.setProcessUserName(Util.null2str(listData.get("logUserName")));
				processLogs.setProcessUserNameEng(Util.null2str(listData.get("logUserNameEng")));
				//commonService.insertLog(processLogs, request);
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.IPR_UPDATE /* pod Status 값 매핑 */
						, Util.null2str(listData.get("reason"))
						, carryIn
				);
			}
			
		}
		
		result.setResultCode("A012051"); // 저장 되었습니다.
		return result;
	}

	public CarryInSaveResult updateBigSizeYnFromBmsInAndPlan(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();

		bmsInMapper.updateBigSizeYnFromBmsIn(listData);
		bmsInMapper.updateBigSizeYnBmsInPlanWhereHasBmsIn(listData);
		bmsInMapper.updateBigSizeYnFromBmsInPlan(listData);
		bmsInMapper.updateBigSizeYnBmsInWhereHasBmsInPlan(listData);
		
		ObjectMapper objectMapper = new ObjectMapper();
		List<Map<String, Object>> inList = objectMapper.convertValue(listData.get("dataList"), new TypeReference<List<Map<String, Object>>>() {});
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("inspectionFinishUserId", Util.null2str(listData.get("updateUserId")));
		paramMap.put("inspectionType", "B");
		
		if(inList != null) {
			for(Map<String, Object> in : inList) {
				if(Util.null2int(in.get("inNo")) > 0) {
					/** 지정시 거대 검사결과추가 */
					if("Y".equals(listData.get("yn"))) {
						paramMap.put("inNo", Util.null2int(in.get("inNo")));
						paramMap.put("cargoReferenceNo", Util.null2str(in.get("cargoReferenceNo")));
						BmsInspectResult inspectResult = bmsInMapper.selectInspectResultByInNoAndType(paramMap);
						if(inspectResult == null) {
							bmsInMapper.insertInspectResult(paramMap);
						}
						paramMap.put("inspectionFinishYn", "N");
						paramMap.put("inspectionFinishRemarks", "");
						paramMap.put("inspectionFinishUserId", "");
						paramMap.put("inspectionFinishDateTime", "");
						bmsInMapper.updateInspectResultByInNoAndType(paramMap);
					} else {
						/** 지정취소시 거대 검사결과 완료처리 */
						paramMap.put("inNo", Util.null2int(in.get("inNo")));
						paramMap.put("inspectionFinishYn", "Y");
						paramMap.put("inspectionFinishRemarks", "거대지정취소로인한완료처리");
						bmsInMapper.updateInspectResultByInNoAndType(paramMap);
					}
				}
			}
		}
		
		List<String> obJobNoList = (List<String>) listData.get("odJobNoList");
		if(obJobNoList != null) {
			CarryIn carryIn = new CarryIn();
			carryIn.setBigSizeYn(Util.null2str(listData.get("yn")));
			for(String obJobNo : obJobNoList) {
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(listData.get("entCode")));
				processLogs.setEntBusinessCode(Util.null2str(listData.get("entBusinessCode")));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(listData.get("updateUserId")));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [거대여부 업데이트]");
				processLogs.setJobLog(new Gson().toJson(listData));
				processLogs.setJobNo(String.valueOf(obJobNo));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setResult("ok");
				processLogs.setProcessUserName(Util.null2str(listData.get("logUserName")));
				processLogs.setProcessUserNameEng(Util.null2str(listData.get("logUserNameEng")));
				//commonService.insertLog(processLogs, request);
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.BIGSIZE_UPDATE /* pod Status 값 매핑 */
						, Util.null2str(listData.get("reason"))
						, carryIn
				);
			}
			
		}
		
		result.setResultCode("A012051"); // 저장 되었습니다.
		return result;
	}
	
	public CarryInSaveResult updateIsptCustomsYnFromBmsInAndPlan(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();

		bmsInMapper.updateIsptCustomsYnFromBmsIn(listData);
		bmsInMapper.updateIsptCustomsYnBmsInPlanWhereHasBmsIn(listData);
		bmsInMapper.updateIsptCustomsYnFromBmsInPlan(listData);
		bmsInMapper.updateIsptCustomsYnBmsInWhereHasBmsInPlan(listData);
		
		ObjectMapper objectMapper = new ObjectMapper();
		List<Map<String, Object>> inList = objectMapper.convertValue(listData.get("dataList"), new TypeReference<List<Map<String, Object>>>() {});
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("inspectionFinishUserId", Util.null2str(listData.get("updateUserId")));
		paramMap.put("inspectionType", "A");
		
		if(inList != null) {
			for(Map<String, Object> in : inList) {
				if(Util.null2int(in.get("inNo")) > 0) {
					/** 지정시 집중검사 검사결과추가 */
					if("Y".equals(listData.get("yn"))) {
						paramMap.put("inNo", Util.null2int(in.get("inNo")));
						paramMap.put("cargoReferenceNo", Util.null2str(in.get("cargoReferenceNo")));
						BmsInspectResult inspectResult = bmsInMapper.selectInspectResultByInNoAndType(paramMap);
						if(inspectResult == null) {
							bmsInMapper.insertInspectResult(paramMap);
						}
						paramMap.put("inspectionFinishYn", "N");
						paramMap.put("inspectionFinishRemarks", "");
						paramMap.put("inspectionFinishUserId", "");
						paramMap.put("inspectionFinishDateTime", "");
						bmsInMapper.updateInspectResultByInNoAndType(paramMap);
					} else {
						/** 지정취소시 집중검사 검사결과 완료처리 */
						paramMap.put("inNo", Util.null2int(in.get("inNo")));
						paramMap.put("inspectionFinishYn", "Y");
						paramMap.put("inspectionFinishRemarks", "집중검사지정취소로인한완료처리");
						bmsInMapper.updateInspectResultByInNoAndType(paramMap);
					}
				}
			}
		}
		
		List<String> obJobNoList = (List<String>) listData.get("odJobNoList");
		if(obJobNoList != null) {
			CarryIn carryIn = new CarryIn();
			carryIn.setInspectionCustomsYn(Util.null2str(listData.get("yn")));
			for(String obJobNo : obJobNoList) {
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(listData.get("entCode")));
				processLogs.setEntBusinessCode(Util.null2str(listData.get("entBusinessCode")));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(listData.get("updateUserId")));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [집중검사 업데이트]");
				processLogs.setJobLog(new Gson().toJson(listData));
				processLogs.setJobNo(String.valueOf(obJobNo));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setResult("ok");
				processLogs.setProcessUserName(Util.null2str(listData.get("logUserName")));
				processLogs.setProcessUserNameEng(Util.null2str(listData.get("logUserNameEng")));
				//commonService.insertLog(processLogs, request);
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.ISPTCUSTOMS_UPDATE /* pod Status 값 매핑 */
						, Util.null2str(listData.get("reason"))
						, carryIn
				);
			}
			
		}
		
		result.setResultCode("A012051"); // 저장 되었습니다.
		return result;
	}
	
	// 접수통보후반입수정 반입접수체크
	public CarryInSaveResult checkLastPodStatus(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		
		CarryIn carryIn = bmsInMapper.selectCarryInByInNoOrOdJobNoOrPackingNo(search);
		log.info("checkLastPodStatus (접수통보후반입수정 : 반입접수체크) carryIn: {}", carryIn);
		if(carryIn == null) {
			result.setResultCode("A11481"); // 해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
		
		log.info("checkLastPodStatus (접수통보후반입수정 : 반입접수체크) LastPodStatus: {}", carryIn.getLastPodStatus());
		if(!"BMS04".equals(carryIn.getLastPodStatus())) {
			result.setResultCode("ABMS100039"); // #1#건 진행상태 접수만 가능합니다.
			return result;
		}
		
		Map<String, Object> returnMap = new HashMap<String,Object>();
		returnMap.put("blNo", carryIn.getBlNo());
		returnMap.put("mblNo", carryIn.getMblNo());
		returnMap.put("cargoReferenceNo", carryIn.getCargoReferenceNo());
		returnMap.put("ct", carryIn.getCt());
		returnMap.put("wt", carryIn.getWt());
		
		result.setReturnMap(returnMap);
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
	
	// 접수통보후반입수정 수량,중량수정
	public CarryInSaveResult modCtWt(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();

		log.info("modCtWt (접수통보후반입수정 : 수량,중량수정) ct: {}", search.get("ct"));
		if (!Util.isInteger(search.get("ct"))) {
			log.info("modCtWt (접수통보후반입수정 : 수량,중량수정) isInteger");
			result.setResultCode("A11449"); // #1# 의 형식이 올바르지 않습니다. 확인해주세요.
			return result;
		}
		
		log.info("modCtWt (접수통보후반입수정 : 수량,중량수정) 반입삭제 wt: {}", search.get("wt"));
		if (!Util.isDecimal(search.get("wt"))) {
			log.info("modCtWt (접수통보후반입수정 : 수량,중량수정) isDecimal");
			result.setResultCode("A11449"); // #1# 의 형식이 올바르지 않습니다. 확인해주세요.
			return result;
		}
		
		CarryIn carryIn = bmsInMapper.selectCarryInByInNoOrOdJobNoOrPackingNo(search);
		log.info("modCtWt (접수통보후반입수정 : 수량,중량수정) carryIn: {}", carryIn);
		if(carryIn == null) {
			result.setResultCode("A11481"); //해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
		
		Map<String, Object> sumOutInfo = bmsInMapper.getSumOutCtWtGroupBmsOut(search);
		if(sumOutInfo == null) {
			log.info("modCtWt (접수통보후반입수정 : 수량,중량수정) sumOutInfo: null");
			
			Map<String, Object> param = new HashMap<String,Object>();
			param.put("inNo", search.get("inNo"));
			param.put("ct", search.get("ct"));
			param.put("wt", search.get("wt"));
			param.put("stockCt", search.get("ct"));
			param.put("stockWt", search.get("wt"));
			param.put("updateUserId", Util.null2str(search.get("updateUserId")));
			bmsInMapper.updateStockAndCtWtForChange(param);
		}else {
			log.info("modCtWt (접수통보후반입수정 : 수량,중량수정) sumOutInfo: {}", sumOutInfo);
			int ct = Util.null2int(search.get("ct"));
			BigDecimal wt = Util.obj2BigDecimalOrZero(search.get("wt"));
			
			int sumOutCt = Util.null2int(sumOutInfo.get("S_OUT_CT"));
			BigDecimal sumOutWt = Util.obj2BigDecimalOrZero(sumOutInfo.get("S_OUT_WT"));
			
			log.info("sumOutInfo else문 ct: {}", ct);
			log.info("sumOutInfo else문 wt: {}", wt);
			log.info("sumOutInfo else문 sumOutCt: {}", sumOutCt);
			log.info("sumOutInfo else문 sumOutWt: {}", sumOutWt);
			
			int calcCt = ct - sumOutCt;
			BigDecimal calcWt = wt.subtract(sumOutWt);
			
			//if (calcCt < 0) {
			//	log.info("sumOutInfo else문 CT calcCt < 0");
			//	result.setResultCode("ABMS100067"); // #1#  0과 같거나 0보다 커야합니다
			//	return result;
			//}
			//
			//if (calcWt.compareTo(BigDecimal.ZERO) < 0) {
			//	log.info("sumOutInfo else문 WT calcWt < 0");
			//	result.setResultCode("ABMS100067"); // #1#  0과 같거나 0보다 커야합니다
			//	return result;
			//}
			
			Map<String, Object> param = new HashMap<String,Object>();
			param.put("inNo", search.get("inNo"));
			param.put("ct", ct);
			param.put("wt", wt);
			param.put("stockCt", calcCt);
			param.put("stockWt", calcWt);
			param.put("updateUserId", Util.null2str(search.get("updateUserId")));
			bmsInMapper.updateStockAndCtWtForChange(param);
		}
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [접수통보후반입수정 수량,중량 업데이트]");
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobNo(carryIn.getOdJobNo());  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		//commonService.insertLog(processLogs, request);
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
				, Util.null2str(search.get("podRemark"))
				, "반입접수후 수량,중량수정"
		);
		
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
	
	// 접수통보후반입수정 반입삭제
	public CarryInSaveResult delCarryIn(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		
		log.info("delCarryIn (접수통보후반입수정 : 반입삭제) inNo: {}", search.get("inNo"));
		if(Util.null2int(search.get("inNo")) == 0) {
			result.setResultCode("A11481"); // 해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
		
		CarryIn carryIn = bmsInMapper.selectCarryInByInNoOrOdJobNoOrPackingNo(search);
		log.info("delCarryIn (접수통보후반입수정 : 반입삭제) carryIn: {}", carryIn);
		if(carryIn == null) {
			result.setResultCode("A11481"); // 해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
		
		log.info("delCarryIn (접수통보후반입수정 : 반입삭제) sumOutInfo: null");
		if(carryIn.getInPlanNo() > 0) {
			Map<String, Object> inPlanParam = new HashMap<String,Object>();
			inPlanParam.put("inPlanNo", carryIn.getInPlanNo());
			inPlanParam.put("inYn", "C");
			inPlanParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
			bmsInMapper.updateInYnFromInPlan(inPlanParam);
		}
		
		Map<String, Object> inChangeDelParm = new HashMap<String,Object>();
		inChangeDelParm.put("inNo", carryIn.getInNo());
		inChangeDelParm.put("updateUserId", Util.null2str(search.get("updateUserId")));
		Map<String, Object> bmsInChangeDel = bmsInMapper.getBmsInChangeDelByInNo(inChangeDelParm);
		if(bmsInChangeDel == null) {
			bmsInMapper.insertBmsInChangeDelByInNo(inChangeDelParm);
		}
		if(!"N".equals(Util.null2str(search.get("deleteOutYn")))) {	// 반출 삭제 여부 파라미터 추가(N:삭제x) 2026.04.08 2sowell
			List<BmsOut> bmsOutList = bmsInMapper.getOutListByInNo(search);
			for(BmsOut bmsOut : bmsOutList) {
				log.info("delCarryIn (접수통보후반입수정 : 반입삭제) delCarryIn foreach bmsOutList의 bmsOut: {}", bmsOut);
				Map<String, Object> outChangeDelParm = new HashMap<String,Object>();
				outChangeDelParm.put("outNo", bmsOut.getOutNo());
				outChangeDelParm.put("outPlanNo", bmsOut.getOutPlanNo());
				outChangeDelParm.put("updateUserId", Util.null2str(search.get("updateUserId")));
				Map<String, Object> bmsOutChangeDel = bmsInMapper.getBmsOutChangeDelByOutNo(outChangeDelParm);
				if(bmsOutChangeDel == null) {
					bmsInMapper.insertBmsOutChangeDelByOutNo(outChangeDelParm);
				}
				bmsInMapper.deleteBmsOut(outChangeDelParm);
				bmsInMapper.deleteBmsOutPlan(outChangeDelParm);
			}
		}
		Map<String, Object> delPkgParam = new HashMap<String,Object>();
		delPkgParam.put("inPlanNo", carryIn.getInPlanNo());
		delPkgParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
		bmsInMapper.softDeleteAllInPlanPkgByInPlanNo(delPkgParam);
		bmsInMapper.deleteCarryIn(carryIn);
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [접수통보후반입수정 반입삭제 업데이트]");
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobNo(carryIn.getOdJobNo());  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		//commonService.insertLog(processLogs, request);
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
				, Util.null2str(search.get("podRemark"))
				, "반입접수후 반입삭제"
		);
		
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
	
	// 접수통보후반입수정 반입삭제 전 체크
	public CarryInSaveResult delCarryInOutCheck(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
			
		log.info("delCarryIn (접수통보후반입수정 : 반입삭제) inNo: {}", search.get("inNo"));
		if(Util.null2int(search.get("inNo")) == 0) {
			result.setResultCode("A11481"); // 해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
			
		CarryIn carryIn = bmsInMapper.selectCarryInByInNoOrOdJobNoOrPackingNo(search);
		log.info("delCarryIn (접수통보후반입수정 : 반입삭제) carryIn: {}", carryIn);
		if(carryIn == null) {
			result.setResultCode("A11481"); // 해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
		
		Map<String, Object> bmsOutMap = bmsInMapper.getOutListByInNoOutCheck(search);
		if(bmsOutMap != null) {
			if(Util.null2int(bmsOutMap.get("CNT_61")) > 0) {	// 이고반출 개수
				result.setResultCode("A11499");
				return result;
			}else if(Util.null2int(bmsOutMap.get("CNT_NOT61")) > 0) {	// 일반반출 개수
				result.setResultCode("A11498");
				return result;
			}
		}
		
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
		
	/**
	 * 정정전원본 키 조회
	 * @param customsReportNo
	 * @return
	 * @throws Exception
	 */
	public String getChangeDelInNoByCustomsReportNo(String customsReportNo){
		String inNo = bmsInMapper.selectChangeDelInNoByCustomsReportNo(customsReportNo);
		return StringEscapeUtils.escapeHtml4(inNo);
	}

	public CarryInDetailList checkExistCarryInAndInPlan(int inPlanNo) {
		CarryInDetailList carryInDetailList = new CarryInDetailList();
		int carryInCnt = bmsInMapper.checkExistCarryIn(inPlanNo);
		int inPlanCnt = bmsInMapper.checkExistInPlan(inPlanNo);
		carryInDetailList.setTotalInPlan(inPlanCnt);
		carryInDetailList.setTotal(carryInCnt);
		return carryInDetailList;
	}

	public String selectErrorNotification(String customsReportNo) {
		String errorNotification = bmsInMapper.selectErrorNotification(customsReportNo);
		return StringEscapeUtils.escapeHtml4(errorNotification);
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

	/** MFN(화물있으나적하목록없음)자료생성 */
	public CarryInSaveResult insertMfn(CarryIn carryIn, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		List<Map<String, Object>> dataList = carryIn.getDataList();
		List<String> existCargoNoList = new ArrayList<>();
		int succCnt = 0;
		int failCnt = 0;
		for (Map<String, Object> dataMap : dataList) {
			String cargoRefNo = (String) dataMap.get("cargoReferenceNo");
			String cargoRefNoDisp = (String) dataMap.get("cargoReferenceNoDisp");
			String logVal = "cargoRefNo ::: " + cargoRefNo;
			log.info(logVal);
			carryIn.setCargoReferenceNo(cargoRefNo);
			//MFN(화물있으나적하목록없음) 자료, TB_ECM_OD_ABNORMAL 테이블에 존재 여부 체크
			int checkMfnCnt = bmsInMapper.checkExistMfn(carryIn);
			log.info("checkMfnCnt:: {}",checkMfnCnt);
			//하기이상정보(TB_ECM_OD_ABNORMAL)에 중복되는 MFN 자료가 없는경우 자료생성, MFN_ISSUE_YN 'Y'로 업데이트
			if(checkMfnCnt == 0) {
				//화면에서 선택한 반입예정 or 반입정보를 하기이상정보로 생성
				OdAbnormal odAbnormal = new OdAbnormal();

				String mrn = "";
				String msn = "";
				String hsn = "";
				if(cargoRefNo.length() > 15) {
					mrn = cargoRefNo.substring(0,11);
					msn = cargoRefNo.substring(11,15);
					hsn = cargoRefNo.substring(15);
				}else {
					mrn = cargoRefNo.substring(0,11);
					msn = cargoRefNo.substring(11,15);
				}

				odAbnormal.setOdJobNo((String) dataMap.get("odJobNo"));
				odAbnormal.setAbnormalCode("MFN");
				odAbnormal.setMrn(mrn);
				odAbnormal.setMsn(msn);
				odAbnormal.setHsn(hsn);
				odAbnormal.setVesselFlight((String) dataMap.get("vesselFlight"));
				odAbnormal.setDiscWhCode(carryIn.getBmsWhCode());
				odAbnormal.setArDate((String) dataMap.get("arDate"));
				odAbnormal.setPieces((Integer) dataMap.get("exCt"));
				double exWt = Double.parseDouble(String.valueOf(dataMap.get("exWt")));
				odAbnormal.setGwt(exWt);
				odAbnormal.setCnt((Integer) dataMap.get("exCt"));
				odAbnormal.setRegisterUserId(carryIn.getRegisterUserId());
				odAbnormal.setUpdateUserId(carryIn.getUpdateUserId());
				//MFN 자료 생성(insert)
				int insertedCnt = bmsInMapper.insertOdAbnormal(odAbnormal);
				if(insertedCnt > 0) {
					/*
					//반입에서 선택했을 경우
					if(carryIn.getCarryInState().equals("CARRY_IN")) {
						bmsInMapper.updateMfnIssueYn((Integer)dataMap.get("inNo"));
					}
					*/
					/*
					//반입예정에서 선택했을 경우
					else if(carryIn.getCarryInState().equals("PRE_CARRY_IN")) {
						bmsInMapper.updateMfnIssueYnByInPlan((Integer)dataMap.get("inPlanNo"));
					}*/
					succCnt++;
					result.setRetSuccMsgId("ABMS100020"); //#1# 건 생성되었습니다.
				}

				/**작업내역 로그시작*/
				// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(carryIn.getEntCode()));
				processLogs.setEntBusinessCode(Util.null2str(carryIn.getEntBusinessCode()));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(carryIn.getRegisterUserId()));
				processLogs.setLogJobType("DATA");
				processLogs.setBound("AIR");
				processLogs.setBoundType("IMP");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [입력 IN_NO:"+String.valueOf(dataMap.get("inNo"))+"]");
				processLogs.setJobNo(String.valueOf(dataMap.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setJobLog(new Gson().toJson(dataMap));
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(carryIn.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(carryIn.getLogUserName());
				processLogs.setProcessUserNameEng(carryIn.getLogUserNameEng());
				//	String podStatus = BmsConsts.PodStatus.BMS99;
				String blNo = String.valueOf(dataMap.get("blNo"));
				// 호출시 필요 항목 : BL_NO, POD_STATUS
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.MFN_INSERT /* pod Status 값 매핑 */
						, ""
						, carryIn
				);
				/**작업내역 로그끝*/
			}else {
				failCnt++;
				result.setRetFailMsgId("A11253"); //이미 생성된 건이 존재합니다.
				existCargoNoList.add(cargoRefNoDisp);
				result.setExistCargoNoList(existCargoNoList);
			}
		}
		result.setInsertSuccCnt(succCnt);
		result.setInsertFailCnt(failCnt);
		return result;
	}

	/** MGN(적하목록있으나화물없음)자료생성 */
	public CarryInSaveResult insertMgn(CarryIn carryIn, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		List<Map<String, Object>> dataList = carryIn.getDataList();
		List<String> existCargoNoList = new ArrayList<>();
		int succCnt = 0;
		int failCnt = 0;
		for (Map<String, Object> dataMap : dataList) {
			String cargoRefNo = (String) dataMap.get("cargoReferenceNo");
			String cargoRefNoDisp = (String) dataMap.get("cargoReferenceNoDisp");
			String logVal = "화물번호: " + cargoRefNo;
			log.info(logVal);
			carryIn.setCargoReferenceNo(cargoRefNo);
			//MGN(적하목록있으나화물없음) 자료, TB_ECM_OD_ABNORMAL 테이블에 존재 여부 체크
			int checkMfnCnt = bmsInMapper.checkExistMgn(carryIn);
			//하기이상정보(TB_ECM_OD_ABNORMAL)에 중복되는 MGN 자료가 없는경우 자료생성, MGN_ISSUE_YN 'Y'로 업데이트
			if(checkMfnCnt == 0) {
				//화면에서 선택한 반입예정 or 반입정보를 하기이상정보로 생성
				OdAbnormal odAbnormal = new OdAbnormal();

				String mrn = "";
				String msn = "";
				String hsn = "";
				if(cargoRefNo.length() > 15) {
					mrn = cargoRefNo.substring(0,11);
					msn = cargoRefNo.substring(11,15);
					hsn = cargoRefNo.substring(15);
				}else {
					mrn = cargoRefNo.substring(0,11);
					msn = cargoRefNo.substring(11,15);
				}

				odAbnormal.setOdJobNo((String) dataMap.get("odJobNo"));
				odAbnormal.setAbnormalCode("MGN");
				odAbnormal.setMrn(mrn);
				odAbnormal.setMsn(msn);
				odAbnormal.setHsn(hsn);
				odAbnormal.setVesselFlight((String) dataMap.get("vesselFlight"));
				odAbnormal.setDiscWhCode(carryIn.getBmsWhCode());
				odAbnormal.setArDate((String) dataMap.get("arDate"));
				odAbnormal.setPieces((Integer) dataMap.get("exCt"));
				double exWt = Double.parseDouble(String.valueOf(dataMap.get("exWt")));
				odAbnormal.setGwt(exWt);
				odAbnormal.setCnt((Integer) dataMap.get("exCt"));
				odAbnormal.setRegisterUserId(carryIn.getRegisterUserId());
				odAbnormal.setUpdateUserId(carryIn.getUpdateUserId());
				//MGN 자료 생성(insert)
				int insertedCnt = bmsInMapper.insertOdAbnormal(odAbnormal);
				if(insertedCnt > 0) {
					//반입에서 선택했을 경우
					if(carryIn.getCarryInState().equals("CARRY_IN")) {
						bmsInMapper.updateMgnIssueYn((Integer)dataMap.get("inNo"));
					}
					//반입예정에서 선택했을 경우
					else if(carryIn.getCarryInState().equals("PRE_CARRY_IN")) {
						bmsInMapper.updateMgnIssueYnByInPlan((Integer)dataMap.get("inPlanNo"));
					}
					succCnt++;
					result.setRetSuccMsgId("ABMS100020"); //#1# 건 생성되었습니다.
				}

				/**작업내역 로그시작*/
				// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(carryIn.getEntCode()));
				processLogs.setEntBusinessCode(Util.null2str(carryIn.getEntBusinessCode()));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(carryIn.getRegisterUserId()));
				processLogs.setLogJobType("DATA");
				processLogs.setBound("AIR");
				processLogs.setBoundType("IMP");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [입력 IN_NO:"+String.valueOf(dataMap.get("inNo"))+"]");
				processLogs.setJobNo(String.valueOf(dataMap.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setJobLog(new Gson().toJson(dataMap));
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(carryIn.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(carryIn.getLogUserName());
				processLogs.setProcessUserNameEng(carryIn.getLogUserNameEng());
				//	String podStatus = BmsConsts.PodStatus.BMS99;
				String blNo = String.valueOf(dataMap.get("blNo"));
				// 호출시 필요 항목 : BL_NO, POD_STATUS
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.MGN_INSERT /* pod Status 값 매핑 */
						, ""
						, carryIn
				);
				/**작업내역 로그끝*/
			}else {
				failCnt++;
				result.setRetFailMsgId("A11253"); //이미 생성된 건이 존재합니다.
				existCargoNoList.add(cargoRefNoDisp);
				result.setExistCargoNoList(existCargoNoList);
			}
		}
		result.setInsertSuccCnt(succCnt);
		result.setInsertFailCnt(failCnt);
		return result;
	}

	public WithHoldingCheckResult selectWithholdingList(SearchInList search) {
		WithHoldingCheckResult result = new WithHoldingCheckResult();
		List<WithHolding> list = new ArrayList<>();
		Map<String,Object> paramMap = new HashMap<String,Object>();
		
		/** 옵션 A07이 Y인경우 반입예정 보류리스트 콤보 설정 */
		paramMap.put("entCode", search.getSrchEntCode());
		paramMap.put("entBusinessCode", search.getSrchEntBusinessCode());
		paramMap.put("solution", "BMS");
		paramMap.put("optionCode", "A07");
		String setValue = commonMapper.selectSystemOption(paramMap);
		
		if("Y".equals(setValue)) {
			list =bmsInMapper.selectWithholdingList(search);
		}
		result.setWithHoldingYn(setValue);
		result.setWithHoldingList(list);
		return result;
	}
	
	/**
	 * 일괄반입자료생성 - 반입구분(inClass) : 반입근거번호(IN_APPROVAL_NO) 자리수가 20이면 "20.보세운송반입" 아니면 "10.입항반입"
	 * @param bmsWhCode
	 * @return
	 */
	public String getBmsInClass(String inApprovalNo) {
		String inClass = "10";
		if(inApprovalNo.length() == 20) {
			inClass = "20";
		}
		return inClass;
	}
	/**
	 * 일괄반입자료생성 - 수출입구분(importExportClass) : 선기명(편명) 3번째 자리수가 숫자이면 항공수입, 아닌 해상수입
	 * @param bmsWhCode
	 * @return
	 */
	public String getBmsImportExportClass(String vesselFlight) {
		String importExportClass = "1";
		
		// 문자열이 최소 3자리 이상인지 확인
        if (vesselFlight.length() < 3) {
            return importExportClass;  // 3번째 문자가 없으면 1(해상수입) 반환
        }

        // 3번째 자리(인덱스 2)의 문자가 숫자인지 확인
        char thirdChar = vesselFlight.charAt(2);
        if(Character.isDigit(thirdChar)) {
        	importExportClass = "3"; //항공수입
        } else {
        	importExportClass = "1"; //해상수입
        }
		
		return importExportClass;
	}
	public CarryInSaveResult  executeBatchInPlanToIn(BatchCarryIn batchCarryIn, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		String batchExecType = batchCarryIn.getBatchExecType(); //IN
		String inputUserId = batchCarryIn.getInputUserId();
		String dateTimeFm =  batchCarryIn.getDateFormat();
		String gmtTime     = batchCarryIn.getGmtTime();
		List<String> returnLst = new ArrayList<>();
		Integer nResultCnt = 0;
		Util.accessControlDatabase(batchCarryIn, "dateFormat", false, null, null, null, null);
		// blList
		if( "IN".equals(batchExecType)) { // IN_PLAN => IN 반입예정 => 반입 처리 일괄반입자료 생성
			String bmsWhCode = batchCarryIn.getBmsWhCode();
			String carryinDate = batchCarryIn.getCarryinDate();
			String carryinTime = batchCarryIn.getCarryinTime();
			String periodOverDate = batchCarryIn.getPeriodOverDate();
			String outNoticeDate = batchCarryIn.getOutNoticeDate();
			String locationPlace = batchCarryIn.getLocationPlace();
			
			for (CarryIn in : batchCarryIn.getInList()) {
				log.info("batch  IN_PLAN => IN  batchCarryIn:{}", in);
				// 그리드 값이 - 가 표시될 가능성이 있으므로 정리
				in.setCargoReferenceNo(in.getCargoReferenceNo().replaceAll("-",""));
				in.setRegisterUserId(inputUserId);
				in.setUpdateUserId(inputUserId);
				in.setCarryinDate(carryinDate);
				in.setCarryinTime(carryinTime);
				in.setPeriodOverDate(periodOverDate);
				in.setOutNoticeDate(outNoticeDate);
				// in.setLocationPlace(locationPlace);
				//1) TB_BMS_IN_PLAN.IN_YN 'Y' 이미 반입자료 있는지 확인
				Integer nCnt =  bmsInMapper.selectCarryInBatchCheckInPlanCount(in);
				//2) 1에서 자료있으면 list에 넣어서  리스트 제외 함
				if(nCnt >0){ // BL 번호로 존재 하면 // 카운트에서 제외 처리
					returnLst.add(in.getBlNo()); // 중복 체크
				} else {
					
					if(in.getInPlanNo() != null) {
						if(!in.getInPlanNo().equals(0)) {
							SpCallVo voIn = bmsDisposalService.getBmsInCustomsReportNo(bmsWhCode);
							in.setCustomsReportNo(voIn.getRetCustomsReportNo());
							
							//String inClass = getBmsInClass(in.getInApprovalNo()); // 반입구분 설정
							//in.setInClass(inClass);
							in.setInClass("15");	// 반입구분 무조건 15.일괄보세운송반입으로 설정 2026.04.27
							
							String importExportClass = getBmsImportExportClass(in.getVesselFlight()); // 수출입구분 설정
							in.setImportExportClass(importExportClass);
							
							//3) TB_BMS_IN_PLAN 에서 TB_BMS_IN로 데이터 입력
							bmsInMapper.insertFromInPlanToInBatchGen(in);
							// 3.5) 반입예정테이블의 IN_YN(반입등록여부) Y로 함
							updateBmsInPlanInYn(in);
							
							nResultCnt++; // 성공 카운트
							
							// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
							ProcessLogs processLogs = new ProcessLogs();
							processLogs.setEntCode(Util.null2str(batchCarryIn.getEntCode()));
							processLogs.setEntBusinessCode(Util.null2str(batchCarryIn.getEntBusinessCode()));
							processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
							processLogs.setProcessUserId(Util.null2str(inputUserId));
							processLogs.setLogJobType("DATA");
							processLogs.setBound("AIR");
							processLogs.setBoundType("IMP");
							processLogs.setJobFuncType(BmsConsts.JobFuncType.BATCH);
							processLogs.setDbType(BmsConsts.DbType.BMS_IN);
							processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [입력 CARGO_REFERENCE_NO:"+ in.getCargoReferenceNo() +", 입력 IN_PLAN_NO:"+ in.getInPlanNo() +"]");
							processLogs.setJobNo(String.valueOf(in.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
							processLogs.setJobLog(new Gson().toJson(in));
							processLogs.setResult("ok");
							processLogs.setEntBusinessPrintName(in.getLogUserBusinessPrintName());
							processLogs.setProcessUserName(in.getLogUserName());
							processLogs.setProcessUserNameEng(in.getLogUserNameEng());
							//	String podStatus = BmsConsts.PodStatus.BMS99;
							// 호출시 필요 항목 : BL_NO, POD_STATUS
							bmsCommService.insertLogAndPodStatus(processLogs, request
									, BmsConsts.PodStatus.INSERT /* pod Status 값 매핑 61로만 입력되어 들어 온다.  */
									, ""
									, in
							);
						}
					}

				}
			}
		}
		result.setReturnList(returnLst);
		result.setResultCnt(nResultCnt);
		return result;
	}
	
	// 접수통보후반입수정 반입접수체크
	public CarryInSaveResult checkLastPodResendStatus(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
			
		CarryIn carryIn = bmsInMapper.selectBmsInLastPodResendCheck(search);
		log.info("checkLastPodResendStatus (화물속성(5TL)재전송 : 반입접수체크) carryIn: {}", carryIn);
		if(carryIn == null) {
			result.setResultCode("A11481");		// 존재하지 않을경우 해당 코드 설정
			return result;
		}
			
		log.info("checkLastPodResendStatus (화물속성(5TL)재전송 : 반입접수체크) LastPodStatus: {}", carryIn.getLastPodStatus());
		if(!"BMS04".equals(carryIn.getLastPodStatus())) {
			result.setResultCode("ABMS100039"); // 접수통보(BMS04)가 아닐경우 해당 코드 설정
			return result;
		}
			
		Map<String, Object> returnMap = new HashMap<String,Object>();
		returnMap.put("inNo", carryIn.getInNo());
		returnMap.put("inPlanNo", carryIn.getInPlanNo());
		returnMap.put("odJobNo", carryIn.getOdJobNo());
		returnMap.put("blNo", carryIn.getBlNo());
		returnMap.put("mblNo", carryIn.getMblNo());
		returnMap.put("cargoReferenceNo", carryIn.getCargoReferenceNo());
		returnMap.put("ct", carryIn.getCt());
		returnMap.put("bigSizeYn", carryIn.getBigSizeYn());
		returnMap.put("xrayImageCt", carryIn.getXrayImageCt());
		returnMap.put("xrayInspectionCode", carryIn.getXrayInspectionCode());
		
		result.setReturnMap(returnMap);
		result.setResultCode("A00165"); 	// 정상조회된 경우 해당 코드 설정
		return result;
	}
	
	// 화물속성(5TL)재전송 처리
	public CarryInSaveResult resendLastPodStatus(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		String podRemark = Util.null2str(search.get("podRemark"));
		String podRemarkStr = "";
		log.info("resendLastPodStatus (화물속성(5TL)재전송 처리) 사유: {}", podRemark);
		
		/* 사유(podRemark) - 01:수기반입등록후전송, 02:관계사요구로재전송, 03:거대수입신고이고검사 */
		if(podRemark.equals("01") || podRemark.equals("02")) {	// '수기반입등록후전송' 또는 '관계사요구로재전송'인 경우
			int ct = Util.null2int(search.get("ct"));
			int xrayImageCt = Util.null2int(search.get("xrayImageCt"));
			if(!"04077009".equals(Util.null2str(search.get("bmsWhCode")))) {	// UPS가 아닌경우 기존 로직 2025.12.03 2sowell
				// 이미지수량이 0이거나 이미지수량<수량 인 경우
				if(xrayImageCt == 0 || xrayImageCt < ct) {
					log.info("resendLastPodStatus (화물속성(5TL)재전송 : 수량) ct:"+ct+", xrayImageCt:"+xrayImageCt);
					result.setErrorMsg("이미지수량이 반입수량보다 작습니다.");
					result.setResultCode("A11501");
					return result;
				}
			}else {	// UPS인경우 백분율 비교 추가 2025.12.03 2sowell
				// 이미지수량이 0 인 경우
				if(xrayImageCt == 0) {
					log.info("resendLastPodStatus (화물속성(5TL)재전송 : 이미지수량) xrayImageCt:"+xrayImageCt);
					result.setErrorMsg("이미지수량이 0입니다.");
					result.setResultCode("A11501");
					return result;
				}
				if(ct > 0) {	// 반입량이 0보다 클경우
					// 반입량과 이미지수량 백분율 X = 이미지수량 * 100 / 반입량
					double ctnPerX = ((double) xrayImageCt / ct ) * 100;
					DecimalFormat df = new DecimalFormat("#.##");
					if(ctnPerX < 80) {
						log.info("resendLastPodStatus (화물속성(5TL)재전송 : 수량 백분율) x:"+ctnPerX);
						result.setErrorMsg("이미지수량이 "+df.format(ctnPerX)+"% 입니다(80% 이상이어야 가능).");
						result.setResultCode("A11501");
						return result;
					}
				}else {
					log.info("resendLastPodStatus (화물속성(5TL)재전송 : 수량) ct:"+ct);
					result.setErrorMsg("반입량이 0보다 커야합니다.");
					result.setResultCode("A11501");
					return result;
				}
				
			}
			
		}else if(podRemark.equals("03")) {	// '거대수입신고이고검사'인 경우
			// 거대여부 확인
			if(Util.null2str(search.get("bigSizeYn")).equals("N")) {
				log.info("resendLastPodStatus (화물속성(5TL)재전송 : 거대여부) N");
				result.setErrorMsg("거대이면서 이고반출된 경우만 가능합니다.");
				result.setResultCode("A11501");
				return result;
			}
			// 해당 화물관리번호로 반출구분이 '61'인 이고반출자료가 있는지 확인
			Integer checkOutCnt = bmsInMapper.selectBmsOutLastPodCheck(search);
			if(checkOutCnt < 1) {	// 조회된 데이터가 없는경우
				result.setErrorMsg("거대이면서 이고반출된 경우만 가능합니다.");
				result.setResultCode("A11501");
				return result;
			}
		}else {	// 그 외에 경우 오류처리
			log.info("resendLastPodStatus (화물속성(5TL)재전송 : 사유) podRemark:"+podRemark);
			result.setErrorMsg("사유가 올바르지 않습니다.");
			result.setResultCode("A11501");
			return result;
		}
		
		// 업데이트하기전 XRAY_RESULT 데이터 확인 체크
		Map<String,Object> xrayMap = bmsInMapper.selectBmsXrayResultCheck(search);
		if(xrayMap == null || xrayMap.isEmpty()) {
			log.info("resendLastPodStatus (화물속성(5TL)재전송 : XRAY) 데이터 없음");
			result.setErrorMsg("X-RAY 데이터가 없습니다.");
			result.setResultCode("A11501");
			return result;
		}
		// 거대수입신고이고검사 이면서 선택한 검사구분이 '이상없음'이고, 기존 검사구분에 코드가 있는경우, 코드 선택 필수
		if(podRemark.equals("03") && Util.null2str(search.get("xrayInspectionCode")).equals("") && !Util.null2str(xrayMap.get("XRAY_INSPECTION_CODE")).equals("")) {
			log.info("resendLastPodStatus (화물속성(5TL)재전송 : 검사구분) 구분 선택 필요");
			result.setErrorMsg("이상없음을 제외한 검사구분 값을 선택하십시오.");
			result.setResultCode("A11501");
			return result;
		}
		
		// TB_BMS_XRAY_RESULT 업데이트
		bmsInMapper.updateXrayResultResend(search);
		
		// POD05 저장
		if(podRemark.equals("01")) {
			podRemarkStr = "수기반입등록후전송";
		}else if(podRemark.equals("02")) {
			podRemarkStr = "관계사요구로재전송";
		}else if(podRemark.equals("03")) {
			podRemarkStr = "거대수입신고이고검사";
		}
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [화물속성(5TL)재전송]");
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobNo(Util.null2str(search.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		//commonService.insertLog(processLogs, request);
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
				, podRemarkStr
				, "화물속성(5TL)재전송"
		);
			
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
	
	// 접수통보후반입수정 반입접수 여러건 체크
	public CarryInSaveResult checkLastPodResendStatusList(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		List<Map<String,Object>> inNoList = (List<Map<String, Object>>) search.get("inNoList");
		List<Map<String,Object>> carryInList = bmsInMapper.selectBmsInLastPodResendCheckList(search);
		if(carryInList != null && carryInList.size() > 0) {
			Map<String,Object> carryIn = carryInList.get(0);
			log.info("checkLastPodResendStatus (화물속성(5TL)재전송 : 반입접수체크) carryIn: {}", carryIn.get("inNo") +"외" + (Util.null2int(carryIn.get("cnt"))-1) + "건");
			if(Util.null2int(carryIn.get("cnt")) != inNoList.size()) {
				result.setResultCode("A11481");		// 존재하지 않을경우 해당 코드 설정
				return result;
			}
			
			log.info("checkLastPodResendStatus (화물속성(5TL)재전송 : 반입접수체크) LastPodStatus: {}", carryIn.get("inNo") +"외" + (Util.null2int(carryIn.get("cnt"))-1) + "건");
			if(Util.null2int(carryIn.get("lastPodCnt")) != inNoList.size()) {
				result.setResultCode("ABMS100039"); // 접수통보(BMS04)가 아닐경우 해당 코드 설정
				return result;
			}
			
			Map<String, Object> returnMap = new HashMap<String,Object>();
			returnMap.put("inNo", carryIn.get("inNo"));
			returnMap.put("blNo", carryIn.get("blNo"));
			returnMap.put("mblNo", carryIn.get("mblNo"));
			returnMap.put("cargoReferenceNo", carryIn.get("cargoReferenceNo"));
			returnMap.put("bigSizeCnt", carryIn.get("bigSizeCnt"));
			returnMap.put("xrayInspectionCode", carryIn.get("xrayInspectionCode"));
			returnMap.put("ct", carryIn.get("ctSum"));
			returnMap.put("xrayImageCt", carryIn.get("xrayImageCtSum"));
			returnMap.put("inNoListResult", carryInList);
			result.setReturnMap(returnMap);
			result.setResultCode("A00165"); 	// 정상조회된 경우 해당 코드 설정
		}else {
			result.setResultCode("A11481");		// 존재하지 않을경우 해당 코드 설정
			return result;
		}
				
		return result;
	}
		
	// 화물속성(5TL)재전송 여러건 처리
	public CarryInSaveResult resendLastPodStatusList(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		List<Map<String,Object>> inNoListResult = (List<Map<String, Object>>) search.get("inNoListResult");
		String podRemark = Util.null2str(search.get("podRemark"));
		log.info("resendLastPodStatus (화물속성(5TL)재전송 처리) 사유: {}", podRemark);
			
		/* 사유(podRemark) - 01:수기반입등록후전송, 02:관계사요구로재전송, 03:거대수입신고이고검사 */
		if(podRemark.equals("01") || podRemark.equals("02")) {	// '수기반입등록후전송' 또는 '관계사요구로재전송'인 경우
			result.setErrorMsg("거대수입신고이고검사만 여러건 전송할 수 있습니다.");
			result.setResultCode("A11501");
			return result;
		}else if(podRemark.equals("03")) {	// '거대수입신고이고검사'인 경우
			// 거대여부 확인
			if(Util.null2str(search.get("bigSizeCnt")).equals("N")) {
				log.info("resendLastPodStatus (화물속성(5TL)재전송 : 거대여부) N");
				result.setErrorMsg("거대이면서 이고반출된 경우만 가능합니다.");
				result.setResultCode("A11501");
				return result;
			}
			// 해당 화물관리번호로 반출구분이 '61'인 이고반출자료가 있는지 확인
			Integer checkOutCnt = bmsInMapper.selectBmsOutLastPodCheckList(search);
			if(checkOutCnt < inNoListResult.size()) {	// 조회된 데이터가 없는경우
				result.setErrorMsg("거대이면서 이고반출된 경우만 가능합니다.");
				result.setResultCode("A11501");
				return result;
			}
		}else {	// 그 외에 경우 오류처리
			log.info("resendLastPodStatus (화물속성(5TL)재전송 : 사유) podRemark:"+podRemark);
			result.setErrorMsg("사유가 올바르지 않습니다.");
			result.setResultCode("A11501");
			return result;
		}
			
		// 업데이트하기전 XRAY_RESULT 데이터 확인 체크
		Map<String,Object> xrayMap = bmsInMapper.selectBmsXrayResultCheckList(search);
		if(xrayMap == null || xrayMap.isEmpty()) {
			log.info("resendLastPodStatus (화물속성(5TL)재전송 : XRAY) 데이터 없음");
			result.setErrorMsg("X-RAY 데이터가 없습니다.");
			result.setResultCode("A11501");
			return result;
		}else {
			Integer xrayCnt = Util.null2int(xrayMap.get("CNT"));
			if(xrayCnt < inNoListResult.size()) {
				result.setErrorMsg("X-RAY 데이터가 없습니다.");
				result.setResultCode("A11501");
				return result;
			}
		}
		// 거대수입신고이고검사 이면서 선택한 검사구분이 '이상없음'이고, 기존 검사구분에 코드가 있는경우, 코드 선택 필수
		if(podRemark.equals("03") && Util.null2str(search.get("xrayInspectionCode")).equals("") && !Util.null2str(xrayMap.get("XRAY_INSPECTION_CODE")).equals("")) {
			log.info("resendLastPodStatus (화물속성(5TL)재전송 : 검사구분) 구분 선택 필요");
			result.setErrorMsg("이상없음을 제외한 검사구분 값을 선택하십시오.");
			result.setResultCode("A11501");
			return result;
		}
		for(Map inNoMap : inNoListResult) {
			// TB_BMS_XRAY_RESULT 업데이트
			inNoMap.put("podRemark", podRemark);
			inNoMap.put("xrayInspectionCode", Util.null2str(search.get("xrayInspectionCode")));
			inNoMap.put("inspectorId", Util.null2str(search.get("inspectorId")));
			inNoMap.put("bmsWhCode", Util.null2str(search.get("bmsWhCode")));
			bmsInMapper.updateXrayResultResend(inNoMap);
		
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(search.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_IN);
			processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [화물속성(5TL)재전송]");
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		
		
			processLogs.setJobLog(new Gson().toJson(inNoMap));
			processLogs.setJobNo(Util.null2str(inNoMap.get("odJobNo")));	// ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
					, "거대수입신고이고검사"
					, "화물속성(5TL)재전송"
					);
		}
		
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
	
	// 타창고하기 설정
	public CarryInSaveResult updatePodStatus(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		String lastPodStatus = Util.null2str(search.get("lastPodStatus"));
		int inNo = Util.null2int(search.get("inNo"));

		// LAST_POD_STATUS(진행상태)가 BMS01(입력), BMS05(오류)인 경우만 진행
		if("BMS01".equals(lastPodStatus) || "BMS05".equals(lastPodStatus)) {
			bmsInMapper.updatePodStatus(inNo);	// 반입 쪽 진행상태 업데이트
			
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(search.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_IN);
			processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [타창고하기수기저장]");
			processLogs.setJobLog(new Gson().toJson(search));
			processLogs.setJobNo(Util.null2str(search.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
			//commonService.insertLog(processLogs, request);
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
					, "타창고하기설정"
					, "타창고하기수기저장"
			);
			
			Map<String, Object> outMap = bmsInMapper.selectLastPodStatusByInNo(inNo);	// 반출 쪽 조회
			if(outMap != null) {
				String outLastPodStatus = Util.null2str(outMap.get("LAST_POD_STATUS"));
				int outNo = Util.null2int(outMap.get("OUT_NO"));
				
				// 반출 쪽 진행상태가 입력 또는 오류인 경우 진행
				if("BMS01".equals(outLastPodStatus) || "BMS05".equals(outLastPodStatus)) {
					bmsInMapper.updateOutPodStatus(outNo);	// 반출 쪽 진행상태 업데이트
				}
			}

		}else {
			result.setErrorMsg("입력 또는 오류일때만 사용가능합니다.");
			result.setResultCode("ABMS100068");
			return result;
		}
		
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
	
	// 타창고하기 설정 여러건
	public CarryInSaveResult updatePodStatusList(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) listData.get("dataList");
		
	    // 오류 검증 : LAST_POD_STATUS(진행상태)가 BMS01(입력), BMS05(오류)인 경우만 진행
	    for (Map<String, Object> bmsInList : dataList) {
	        String lastPodStatus = Util.null2str(bmsInList.get("lastPodStatus"));
	        if (!"BMS01".equals(lastPodStatus) && !"BMS05".equals(lastPodStatus)) {
	            result.setErrorMsg("입력 또는 오류일때만 사용가능합니다.");
	            result.setResultCode("ABMS100068");
	            return result;
	        }
	    }

		for(Map<String, Object> bmsInList : dataList) {
			int inNo = Util.null2int(bmsInList.get("inNo"));			

			bmsInMapper.updatePodStatus(inNo);	// 반입 쪽 진행상태 업데이트
			
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(listData.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(listData.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(bmsInList.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_IN);
			processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [타창고하기수기저장]");
			processLogs.setJobLog(new Gson().toJson(bmsInList));
			processLogs.setJobNo(Util.null2str(bmsInList.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(bmsInList.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(bmsInList.get("logUserNameEng")));
			//commonService.insertLog(processLogs, request);
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
						, "타창고하기설정"
						, "타창고하기수기저장"
				);
			
			Map<String, Object> outMap = bmsInMapper.selectLastPodStatusByInNo(inNo);	// 반출 쪽 조회

			if(outMap != null) {
				String outLastPodStatus = Util.null2str(outMap.get("LAST_POD_STATUS"));
				int outNo = Util.null2int(outMap.get("OUT_NO"));

				// 반출 쪽 진행상태가 입력 또는 오류인 경우 진행
				if("BMS01".equals(outLastPodStatus) || "BMS05".equals(outLastPodStatus)) {
					bmsInMapper.updateOutPodStatus(outNo);	// 반출 쪽 진행상태 업데이트
				}
			}
		}	
		
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
	
	
	// 타창고하기 해제
	public CarryInSaveResult updatePodStatusClear(Map<String, Object> search, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		String lastPodStatus = Util.null2str(search.get("lastPodStatus"));
		int inNo = Util.null2int(search.get("inNo"));

		// LAST_POD_STATUS(진행상태)가 BMSA1(타창고하기)인 경우만 진행
		if("BMSA1".equals(lastPodStatus)) {
			bmsInMapper.updatePodStatusClear(inNo);	// 반입 쪽 진행상태 업데이트
				
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(search.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_IN);
			processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [타창고하기수기해제]");
			processLogs.setJobLog(new Gson().toJson(search));
			processLogs.setJobNo(Util.null2str(search.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setResult("ok");
			processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
			//commonService.insertLog(processLogs, request);
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
					, "타창고하기해제"
					, "타창고하기수기해제"
			);
				
			Map<String, Object> outMap = bmsInMapper.selectLastPodStatusByInNo(inNo);	// 반출 쪽 조회
			if(outMap != null) {
				String outLastPodStatus = Util.null2str(outMap.get("LAST_POD_STATUS"));
				int outNo = Util.null2int(outMap.get("OUT_NO"));
					
				// 반출 쪽 진행상태가 타창고하기인 경우 진행
				if("BMSA1".equals(outLastPodStatus)) {
					bmsInMapper.updateOutPodStatusClear(outNo);	// 반출 쪽 진행상태 업데이트
				}
			}
		}else {
			result.setErrorMsg("타창고하기인 경우에만 사용가능합니다.");
			result.setResultCode("ABMS100068");
			return result;
		}
			
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
	
	// 일괄홀드 처리 설정
		public CarryInSaveResult updateHoldOnList(Map<String, Object> listData, HttpServletRequest request) throws Exception {
			CarryInSaveResult result = new CarryInSaveResult();
			List<Map<String, Object>> dataList = (List<Map<String, Object>>) listData.get("dataList");
			String holdCode = "";
			
			if("PANTK".equals(Util.null2str(listData.get("entCode")))) {
				holdCode = "98";	// 판토스인경우 98.G1홀드
			}else {
				holdCode = "4";		// UPS인경우 4.기타
			}
				
			// 오류 검증 : 홀드처리가 되어있는 경우인지 확인
			for (Map<String, Object> bmsInList : dataList) {
				bmsInList.put("srchHoldCode", holdCode);
				Map<String, Object> holdMap = bmsInMapper.selectHoldStatusByOdJobNo(bmsInList);
				if (holdMap != null && ("Y".equals(Util.null2str(holdMap.get("CARRYOUT_STOP_YN"))) && "N".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN"))))) {
					result.setErrorMsg("이미 홀드된 데이터가 있습니다.");
					result.setResultCode("ABMS100068");
					return result;
				}
			}

			for(Map<String, Object> bmsInList : dataList) {
				bmsInList.put("srchHoldCode", holdCode);
				bmsInList.put("updateUserId", listData.get("updateUserId"));
				bmsInMapper.insertHoldOn(bmsInList);	// 홀드 데이터 입력
				
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(listData.get("entCode")));
				processLogs.setEntBusinessCode(Util.null2str(listData.get("entBusinessCode")));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(bmsInList.get("updateUserId")));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [일괄홀드처리]");
				processLogs.setJobLog(new Gson().toJson(bmsInList));
				processLogs.setJobNo(Util.null2str(bmsInList.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setResult("ok");
				processLogs.setProcessUserName(Util.null2str(bmsInList.get("logUserName")));
				processLogs.setProcessUserNameEng(Util.null2str(bmsInList.get("logUserNameEng")));
				//commonService.insertLog(processLogs, request);
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
						, "일괄홀드"
						, "일괄홀드처리"
				);
					
			}	
				
			result.setResultCode("A00165"); // 저장 되었습니다.
			return result;
		}
		
		
		// 일괄홀드 해제처리 설정
		public CarryInSaveResult updateHoldOnListClear(Map<String, Object> listData, HttpServletRequest request) throws Exception {
			CarryInSaveResult result = new CarryInSaveResult();
			List<Map<String, Object>> dataList = (List<Map<String, Object>>) listData.get("dataList");
			String holdCode = "";
			
			if("PANTK".equals(Util.null2str(listData.get("entCode")))) {
				holdCode = "98";	// 판토스인경우 98.G1홀드
			}else {
				holdCode = "4";		// UPS인경우 4.기타
			}
			
			// 오류 검증 : 홀드처리가 되어있는 경우인지 확인
			for (Map<String, Object> bmsInList : dataList) {
				bmsInList.put("srchHoldCode", holdCode);
				Map<String, Object> holdMap = bmsInMapper.selectHoldStatusByOdJobNo(bmsInList);
				if(holdMap == null) {
					result.setErrorMsg("홀드가 안된 데이터가 있습니다.");
					result.setResultCode("ABMS100068");
					return result;
				}else if("N".equals(Util.null2str(holdMap.get("CARRYOUT_STOP_YN"))) && "Y".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))) {
					result.setErrorMsg("이미 홀드해제된 데이터가 있습니다.");
					result.setResultCode("ABMS100068");
					return result;
				}
			}

			for(Map<String, Object> bmsInList : dataList) {
				bmsInMapper.updateHoldOnClear(bmsInList);	// 홀드 데이터 입력
					
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(listData.get("entCode")));
				processLogs.setEntBusinessCode(Util.null2str(listData.get("entBusinessCode")));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(bmsInList.get("updateUserId")));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [일괄홀드해제처리]");
				processLogs.setJobLog(new Gson().toJson(bmsInList));
				processLogs.setJobNo(Util.null2str(bmsInList.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setResult("ok");
				processLogs.setProcessUserName(Util.null2str(bmsInList.get("logUserName")));
				processLogs.setProcessUserNameEng(Util.null2str(bmsInList.get("logUserNameEng")));
				//commonService.insertLog(processLogs, request);
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
						, "일괄홀드해제"
						, "일괄홀드해제처리"
				);
						
			}	
					
			result.setResultCode("A00165"); // 저장 되었습니다.
			return result;
		}
		
		// X-Ray판독수량 초기화
		public CarryInSaveResult updateXrayCtClear(Map<String, Object> search, HttpServletRequest request)
				throws Exception {
			CarryInSaveResult result = new CarryInSaveResult();
			int inPlanNo = Util.null2int(search.get("inPlanNo"));
			String inYn = Util.null2str(search.get("inYn"));
			String carryInState = Util.null2str(search.get("srchCarryInState"));
			
			// 반입전 또는 반입 조회인 경우 진행
			if ("N".equals(inYn) || "CARRY_IN".equals(carryInState)) {
				if ("CARRY_IN".equals(carryInState)) {
					bmsInMapper.updateCarryInXrayCtClear(search);
				} else {
					bmsInMapper.updateXrayCtClear(inPlanNo); // X-ray 판독, 이미지 수량 0으로 업데이트
				}
		
				ProcessLogs processLogs = new ProcessLogs();
				String blNo = Util.null2str(search.get("blNo"));
				String remarks = "BL_NO : " + blNo;
				processLogs.setEntCode(Util.null2str(search.get("entCode")));
				processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
				processLogs.setDbType(BmsConsts.DbType.BMS_IN);
				processLogs.setDescription(BmsConsts.DbType.BMS_IN + " [X-Ray판독수량 초기화]");
				processLogs.setJobLog(new Gson().toJson(search));
				processLogs.setJobNo(Util.null2str(search.get("odJobNo"))); // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setResult("ok");
				processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
				processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
				// commonService.insertLog(processLogs, request);
				bmsCommService.insertLogAndPodStatus(processLogs, request, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
						,remarks , "X-Ray판독수량 초기화");		 
			}else {
				result.setErrorMsg("반입전인 경우만 사용가능합니다.");
				result.setResultCode("ABMS100068");
				return result;
			}
			
			result.setResultCode("A00165"); // 저장 되었습니다.
			return result;
		}
	
}
