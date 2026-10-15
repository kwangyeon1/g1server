package org.yht.exps.express.service.bms;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsDisposalMapper;
import org.yht.exps.express.mapper.bms.BmsInMapper;
import org.yht.exps.express.mapper.bms.BmsOutMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsComm.BmsEcmPod;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.disposal.Disposal;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.disposal.InventoryInList;
import org.yht.exps.express.model.bms.disposal.SpCallVo;
import org.yht.exps.express.model.bms.disposal.search.SearchDisposal;
import org.yht.exps.express.model.bms.out.*;
import org.yht.exps.express.model.bms.out.search.SearchBmsInOut;
import org.yht.exps.express.model.bms.out.search.SearchBmsOut;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo.CargCsclPrgsInfoDtail;
import org.yht.exps.express.model.eCommerce.order.OrderHold;
import org.yht.exps.express.model.mobile.bms.BmsLocalInspectResult;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.Filters;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.EcmCustomer;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.bms.send.BmsSendXmlParser;
import org.yht.exps.express.service.pda.PdaTicPaliService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import javax.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class BmsOutService {

	@Autowired
	OrderMapper orderMapper;
	
	@Autowired
	CommonMapper commonMapper;

	@Autowired
	BmsOutMapper bmsOutMapper;
	
	@Autowired
	BmsInMapper bmsInMapper;
	
	@Autowired
	BmsDisposalMapper bmsDisposalMapper;

	@Autowired
	PdaTicPaliService pdaTicPaliService;
	
	@Autowired
	CommonService commonService;

	@Autowired
	BmsDisposalService bmsDisposalService;

	@Autowired
	BmsCommService bmsCommService ;

	@Autowired
	BmsSendXmlParser bmsSendXmlParser;

	@Autowired
    BmsSendUtils bmsSendUtils;
	
	/**
	 * 재고 조회 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public InventoryInList selectInventoryInList(SearchBmsOut search) throws Exception {
		InventoryInList inventoryInList = new InventoryInList();

		setSearchCondition(search);

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|A.AR_DATE|A.CARRYIN_DATE|APPROVAL_DATE|APPROVAL_CANCEL_DATE|CARRYOUT_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|X.CUSTOMS_REPORT_NO|A.MBL_NO|X.OUT_APPROVAL_NO|A.PKG_NO");
		List<InventoryIn> list = bmsOutMapper.selectInventoryInList(search);
		inventoryInList.setInventoryInList(list);

		if(list.size() > 0) {
			inventoryInList.setTotal(list.get(0).getTotal());
		} else {
			inventoryInList.setTotal(0);
		}

		return inventoryInList;
	}

	/**
	 * 반출 조회 List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsOutList selectBmsOutList(SearchBmsOut search) throws Exception {
		BmsOutList bmsOutList = new BmsOutList();

		setSearchCondition(search);

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|B.AR_DATE|B.CARRYIN_DATE|A.APPROVAL_DATE|C.APPROVAL_CANCEL_DATE|A.CARRYOUT_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO|A.CUSTOMS_REPORT_NO|A.MBL_NO|A.OUT_APPROVAL_NO|B.PKG_NO");
		List<BmsOut> list = bmsOutMapper.selectBmsOutList2(search);
		bmsOutList.setBmsOutList(list);

		if(list.size() > 0) {
			bmsOutList.setTotal(list.get(0).getTotal());
		} else {
			bmsOutList.setTotal(0);
		}

		return bmsOutList;
	}

	/**
	 * 반출승인/승인취하 조회 List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsOutPlanList selectBmsOutPlanList(SearchBmsOut search) throws Exception {
		BmsOutPlanList bmsOutPlanList = new BmsOutPlanList();

		setSearchCondition(search);

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|B.AR_DATE|B.CARRYIN_DATE|APPROVAL_DATE|APPROVAL_CANCEL_DATE|A.CARRYOUT_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO|X.CUSTOMS_REPORT_NO|A.MBL_NO|A.OUT_APPROVAL_NO|B.PKG_NO");
		List<BmsOutPlan> list = bmsOutMapper.selectBmsOutPlanList2(search);
		bmsOutPlanList.setBmsOutPlanList(list);

		if(list.size() > 0) {
			bmsOutPlanList.setTotal(list.get(0).getTotal());
		} else {
			bmsOutPlanList.setTotal(0);
		}

		return bmsOutPlanList;
	}
	
	/**
	 * 견품반출 조회 List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsOutSampleList selectBmsOutSampleList(SearchBmsOut search) throws Exception {
		BmsOutSampleList bmsOutSampleList = new BmsOutSampleList();

		setSearchCondition(search);

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|B.AR_DATE|B.CARRYIN_DATE|APPROVAL_DATE|APPROVAL_CANCEL_DATE|A.CARRYOUT_DATE|A.PERMISSION_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO|X.CUSTOMS_REPORT_NO|A.MBL_NO|X.OUT_APPROVAL_NO|B.PKG_NO");
		List<BmsOutSample> list = bmsOutMapper.selectBmsOutSampleList(search);
		bmsOutSampleList.setBmsOutSampleList(list);

		if(list.size() > 0) {
			bmsOutSampleList.setTotal(list.get(0).getTotal());
		} else {
			bmsOutSampleList.setTotal(0);
		}

		return bmsOutSampleList;
	}

	private static void setSearchCondition(SearchBmsOut search) throws Exception {
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

			Filter lstFilter = search.getFilter();
			if( lstFilter != null) {
				for (Filters filt : lstFilter.getFilters()) { // 가상필드 다른 이름으로 처리
					if (filt.getField().equals("CARGO_REFERENCE_NO_DISP")) {
						filt.setField("CARGO_REFERENCE_NO");
					}
				}
			}
		}
	}

	/**
	 * 반출 이고차수 드롭다운 List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Map getTicOutSeqListFromBmsOut(Map<String, Object> param) throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		List<Integer> list = bmsOutMapper.selectTicOutSeqListFromBmsOut(param);
		result.put("data", list);
		result.put("total", list.size());
		return result;
	}
	
	public Map updateTicOutSeqFromBmsOut(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		if(listData == null) {
			result.put("resultCode", "A00059");
			return result;
		}
		
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) listData.get("dataList");
		if(dataList == null || (dataList != null && dataList.size() == 0) ) {
			result.put("resultCode", "A00059");
			return result;
		}
		
		bmsOutMapper.updateTicOutSeqFromBmsOut(listData);
		
		result.put("resultCode", "A11254");
		return result;
	}
	
	/**
	 * 견품반출 상세 화면 조회
	 * @param search
	 * @return
	 */
	public BmsOutDetail selectBmsOutSampleDetail(SearchBmsOut search){
		BmsOutDetail detail = new BmsOutDetail();
		BmsOutSample bmsOutSample = null;
		InventoryIn inventoryIn = null;
		
		if(BmsConsts.BMS_CONST_SAMPLE.equals(search.getSrchCarryInState())) {  // SAMPLE
			bmsOutSample   = bmsOutMapper.selectBmsOutSampleDetail(search);
			search.setSrchInNo(bmsOutSample.getInNo());
			inventoryIn =  bmsOutMapper.selectInventoryDetail(search);
		}else { // STOCK
			bmsOutSample   = bmsOutMapper.selectBmsOutSampleDetail(search);
			inventoryIn =  bmsOutMapper.selectInventoryDetail(search);
		}

 		detail.setBmsOutSample(bmsOutSample);
		detail.setInventoryIn(inventoryIn);

		detail.setSrchCarryInState(search.getSrchCarryInState());
		detail.setSrchOutNo(search.getSrchOutNo());
		detail.setSrchInNo(search.getSrchInNo());
		return detail;
	}
	
	/**
	 * 상세 화면 조회 리스트 조회
	 * @param search
	 * @return
	 */
	public BmsOutDetail selectBmsOutInventoryDetail(SearchBmsOut search){
		BmsOutDetail detail = new BmsOutDetail();
		BmsOut bmsOut = null;

		BmsOutPlan bmsOutPlan = null;
		InventoryIn inventoryIn = null;
		EcmCustomer customerInfo = null;
		List<BmsDelivery> doLst = new ArrayList<>();
		if(BmsConsts.BMS_CONST_OUT.equals(search.getSrchCarryInState())) {  //OUT, PLAN, STOCK
			bmsOut   = bmsOutMapper.selectBmsOutDetail(search);
			search.setSrchInNo(bmsOut.getInNo()); // 상세 정보 조회
			inventoryIn =  bmsOutMapper.selectInventoryDetail(search);
			if(null != bmsOut) {
				search.setSrchOutPlanNo(bmsOut.getOutPlanNo());
				bmsOutPlan = bmsOutMapper.selectBmsOutPlanDetail(search);
			}
			search.setSrchBlNo(bmsOut.getBlNo());
			doLst = bmsOutMapper.selectDoList(search);
			//
			detail.setBmsContainer(bmsOutMapper.selectOutContainerList(search));
		}else if(BmsConsts.BMS_CONST_OUT_PLAN.equals(search.getSrchCarryInState())) {  //OUT, PLAN, STOCK  // OUT_NO로 조회 해아 한다.
			bmsOutPlan   = bmsOutMapper.selectBmsOutPlanDetail(search);
			bmsOut   = bmsOutMapper.selectBmsOutDetail(search);

			search.setSrchInNo(bmsOutPlan.getInNo()); // 상세 정보 조회
			inventoryIn =  bmsOutMapper.selectInventoryDetail(search);
			//
			search.setSrchBlNo(bmsOutPlan.getBlNo());
			doLst = bmsOutMapper.selectDoList(search);
			if( null != bmsOut) {
				search.setSrchOutNo(bmsOut.getOutNo());
				detail.setBmsContainer(bmsOutMapper.selectOutContainerList(search));
			}else{
				detail.setBmsContainer(bmsOutMapper.selectInContainerList(search));
			}
			// 반출승인에 납세의무자사업자번호가 있는 경우 팝업화면에 화주코드/명에 거래처코드/명 표출
			// 만약 거래처코드/명 없는 경우 반입의 화주정보를 표출 (inventoryIn에 있음)
			if(null == bmsOut && search.getSrchCompanyBusinessNo() != null && !"".equals(search.getSrchCompanyBusinessNo())) {
				search.setCargoReferenceNo(bmsOutPlan.getCargoReferenceNo());
				customerInfo = bmsOutMapper.selectCustomerInfoByBusNo(search);
			}
		}else { // STOCK
			inventoryIn =  bmsOutMapper.selectInventoryDetail(search);
			bmsOut      = bmsOutMapper.selectBmsOutDetail(search);
			if(null != bmsOut) {
				search.setSrchOutPlanNo(bmsOut.getOutPlanNo());
				bmsOutPlan = bmsOutMapper.selectBmsOutPlanDetail(search);
			}
			search.setSrchBlNo(inventoryIn.getBlNo());
			doLst = bmsOutMapper.selectDoList(search);
			if( null != bmsOut) {
				search.setSrchOutNo(bmsOut.getOutNo());
				detail.setBmsContainer(bmsOutMapper.selectOutContainerList(search));
			}else{
				detail.setBmsContainer(bmsOutMapper.selectInContainerList(search));
			}
		}


		detail.setCustomerInfo(customerInfo);
 	    detail.setBmsDelivery(doLst);
 		detail.setBmsOut(bmsOut);
		detail.setBmsOutPlan(bmsOutPlan);
		detail.setInventoryIn(inventoryIn);

		detail.setSrchCarryInState(search.getSrchCarryInState());
		detail.setSrchOutNo(search.getSrchOutNo());
		detail.setSrchInNo(search.getSrchInNo());
		return detail;
	}

	/***
	 * srchBlNo로 조회시에 보이는 값 리스트
	 * @param search
	 * @return
	 */
	public BmsOutDetailList selectBmsOutByBlNo(SearchBmsOut search) throws Exception {
		BmsOutDetailList lst = new BmsOutDetailList();

		search.setPageSize(50);
		search.setPage(1);
		search.setDefaultSort(" IN_NO DESC ");
		setSearchCondition(search);
		  // Out   // CUSTOMS_REPORT_NO-IN // CUSTOMS_REPORT_NO-OUT 처리 , srchNoLikeYn
		// TB_BMS_OUT A.CUSTOMS_REPORT_NO, TB_BMS_IN.B.CUSTOMS_REPORT_NO
		// CUSTOMS_REPORT_NO  에 대해서 각 단계 별로 Alias 처리 요청
		String originSrchNoType = search.getSrchNoType();
		getSearchCondiOut(search, BmsConsts.BMS_CONST_OUT , originSrchNoType);
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|B.AR_DATE|B.CARRYIN_DATE|A.APPROVAL_DATE|C.APPROVAL_CANCEL_DATE|A.CARRYOUT_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|B.CUSTOMS_REPORT_NO|X.CUSTOMS_REPORT_NO|(1=0)|A.MBL_NO|A.OUT_APPROVAL_NO|B.PKG_NO");
		List<BmsOut> bmsOutLst  = bmsOutMapper.selectBmsOutList(search);
		if(bmsOutLst.size() > 0) {
			lst.setTotal(bmsOutLst.get(0).getTotal());
		}else {
			lst.setTotal(0);
		}
		lst.setBmsOutList(bmsOutLst);
		//inventory TB_BMS_IN.A.CUSTOMS_REPORT_NO
		getSearchCondiOut(search, BmsConsts.BMS_CONST_STOCK, originSrchNoType);
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|AR_DATE|CARRYIN_DATE|APPROVAL_DATE|APPROVAL_CANCEL_DATE|CARRYOUT_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|B.CUSTOMS_REPORT_NO|X.CUSTOMS_REPORT_NO|(1=0)|A.MBL_NO|X.OUT_APPROVAL_NO|A.PKG_NO");
		List<InventoryIn> invenLst = bmsOutMapper.selectInventoryInList(search);
		if(invenLst.size() > 0) {
			lst.setTotalIn(invenLst.get(0).getTotal());
		}else {
			lst.setTotalIn(0);
		}
		lst.setInventoryInList(invenLst);

		// Plan  PLAN 일경우 TB_BMS_IN.B.CUSTOMS_REPORT_NO
		getSearchCondiOut(search, BmsConsts.BMS_CONST_OUT_PLAN , originSrchNoType);
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|B.AR_DATE|B.CARRYIN_DATE|APPROVAL_DATE|C.APPROVAL_CANCEL_DATE|A.CARRYOUT_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|B.CUSTOMS_REPORT_NO|X.CUSTOMS_REPORT_NO|(1=0)|A.MBL_NO|A.OUT_APPROVAL_NO|B.PKG_NO");
		List<BmsOutPlan> bmsOutPlanLst  = bmsOutMapper.selectBmsOutPlanList(search);
		if(bmsOutPlanLst.size() > 0) {
			//lst.setTotalPlan(bmsOutPlanLst.get(0).getTotal());
		}else {
			lst.setTotalPlan(0);
		}
		lst.setBmsOutPlanList(bmsOutPlanLst);
		return lst;
	}
	
	/***
	 * srchBlNo로 조회시에 보이는 값 리스트
	 * @param search
	 * @return
	 */
	public BmsOutDetailList selectBmsOutSampleByBlNo(SearchBmsOut search) throws Exception {
		BmsOutDetailList lst = new BmsOutDetailList();

		search.setPageSize(50);
		search.setPage(1);
		search.setDefaultSort(" IN_NO DESC ");
		setSearchCondition(search);
		
		// 견품반출
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|B.AR_DATE|B.CARRYIN_DATE|APPROVAL_DATE|APPROVAL_CANCEL_DATE|A.CARRYOUT_DATE|A.PERMISSION_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|PERMISSION_NO");
		List<BmsOutSample> bmsOutSampleLst  = bmsOutMapper.selectBmsOutSampleList(search);
		if(bmsOutSampleLst.size() > 0) {
			lst.setTotal(bmsOutSampleLst.get(0).getTotal());
		}else {
			lst.setTotal(0);
		}
		lst.setBmsOutSampleList(bmsOutSampleLst);

		// 반입
		String originSrchNoType = search.getSrchNoType();
		if ("PERMISSION_NO".equals(originSrchNoType)) {
			search.setSrchNoType("(1=0)");
		}
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|AR_DATE|CARRYIN_DATE|APPROVAL_DATE|APPROVAL_CANCEL_DATE|CARRYOUT_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|PERMISSION_NO|(1=0)");
		List<InventoryIn> invenLst = bmsOutMapper.selectInventoryInList(search);
		if(invenLst.size() > 0) {
			lst.setTotalIn(invenLst.get(0).getTotal());
		}else {
			lst.setTotalIn(0);
		}
		lst.setInventoryInList(invenLst);

		return lst;
	}
	
	/***
	 * 반입,출입에서 반입번호로 조회해서 각각의 신고번호와 화물관리번호를 조회한다
	 * @param search
	 * @return
	 */
	public BmsInOut selectCustomsReportNoOrCargoNoFromInOut(SearchBmsInOut search) throws Exception {
		BmsInOut bmsInOut = bmsOutMapper.selectCustomsReportNoOrCargoNoFromInOut(search);
		
		if(bmsInOut == null) {
			BmsInOut errBmsInOut = new BmsInOut();
			errBmsInOut.setResultCode("A11481"); // 해당 #1# 데이터는 존재하지 않습니다
			
			return errBmsInOut;
		}
		
		bmsInOut.setResultCode("A012051"); // 저장 되었습니다.
		return bmsInOut;
	}

	/***
	 * CUSTOMS_REPORT_NO 에 대해서 각 쿼리 마다 다른 값이 넣머 오므로 각 단계 별로
	 * setSearchType 의 값을 넘긴다.
	 * @param search
	 * @param gubun
	 */
	private static void getSearchCondiOut(SearchBmsOut search , String gubun , String originSearchType) {
		if (BmsConsts.BMS_CONST_OUT.equals(gubun)) { //// TB_BMS_OUT A.CUSTOMS_REPORT_NO, TB_BMS_IN.B.CUSTOMS_REPORT_NO
			if ("CUSTOMS_REPORT_NO-OUT".equals(originSearchType)) {
				search.setSrchNoType("A.CUSTOMS_REPORT_NO");
			} else if ("CUSTOMS_REPORT_NO-IN".equals(originSearchType)) {
				search.setSrchNoType("B.CUSTOMS_REPORT_NO");
			}
		}else if (BmsConsts.BMS_CONST_STOCK.equals(gubun)) {// inventory TB_BMS_IN.A.CUSTOMS_REPORT_NO
			if ("CUSTOMS_REPORT_NO-OUT".equals(originSearchType)) {
				search.setSrchNoType("(1=0)");
			} else if ("CUSTOMS_REPORT_NO-IN".equals(originSearchType)) {
				search.setSrchNoType("A.CUSTOMS_REPORT_NO");
			}
		}else if (BmsConsts.BMS_CONST_OUT_PLAN.equals(gubun)) {  // Plan  PLAN 일경우 TB_BMS_IN.B.CUSTOMS_REPORT_NO
			if ("CUSTOMS_REPORT_NO-OUT".equals(originSearchType)) {
				search.setSrchNoType("(1=0)");
			} else if ("CUSTOMS_REPORT_NO-IN".equals(originSearchType)) {
				search.setSrchNoType("B.CUSTOMS_REPORT_NO");
			}
		}
	}

	/**
	 * 반출신고 이고반출확인처리
	 * @param bmsOut
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult confirmTic61out(BmsOut bmsOut, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		
		Map<String, Object> outInfoParam = new HashMap<String, Object>();
		outInfoParam.put("outNo", bmsOut.getOutNo());
		
		Map<String, Object>  out = bmsOutMapper.selectBmsOutByOutNo(outInfoParam);
		if( out == null || (out != null && "Y".equals(Util.null2str(out.get("TIC_OUT_SCAN_YN")))) ) {
			bmsOutSaveResult.setResultCode("A11481"); // 해당 #1# 데이터는 존재하지 않습니다.
			return bmsOutSaveResult;
		}
		
		if("PANTK".equals(bmsOut.getEntCode()) && "61".equals(Util.null2str(out.get("OUT_CLASS"))) ){ // pantos 반출구분이 61일때
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("outNo", bmsOut.getOutNo());
			param.put("cargoReferenceNo", out.get("CARGO_REFERENCE_NO"));
			Map<String, Object> bmsOutWithBmsIn = bmsOutMapper.selectBmsOutWithBmsIn(param);
			if(bmsOutWithBmsIn != null) {
				bmsOutWithBmsIn.put("inPlanNo", bmsOutWithBmsIn.get("IN_PLAN_NO"));
				List<Map<String, Object>> pkgList = bmsOutMapper.selectPkgListByInPlanNo(bmsOutWithBmsIn);
				for(Map<String, Object> pkg : pkgList) {
					bmsOutWithBmsIn.put("PACKING_NO", pkg.get("PACKING_NO"));
					
					Map<String, Object> updateUserIdMap = new HashMap<String, Object>();
					updateUserIdMap.put("updateUserId", bmsOut.getRegisterUserId());
					pdaTicPaliService.insertTicMoveScan(bmsOutWithBmsIn, updateUserIdMap, "완료");
				}
			}
		}
		
		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(bmsOut.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(bmsOut.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(bmsOut.getRegisterUserId()));
		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setDbType(BmsConsts.DbType.BMS_TIC_PALI);
		processLogs.setDescription(BmsConsts.DbType.BMS_TIC_PALI+" [수기 이고반출대상확인 BMS_OUT NO:"+String.valueOf(bmsOut.getOutNo())+"]");
		processLogs.setJobNo(String.valueOf(bmsOut.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(bmsOut));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(bmsOut.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(bmsOut.getLogUserName());
		processLogs.setProcessUserNameEng(bmsOut.getLogUserNameEng());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.BMS_TIC_PALI_CHK /* pod Status 값 매핑 */
				, bmsOut.getReason() + "(수기 이고반출대상확인)"
				, bmsOut
		);

		bmsOutSaveResult.setResultCode("A012051");// 저장 되었습니다.
		return bmsOutSaveResult;
	}
	
	/**
	 * 특송창고 disposal - insert/update
	 * @param bmsOut
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult insertBmsOut(BmsOut bmsOut, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		Map<String, String> holdParam = new HashMap<String, String>();
		holdParam.put("srchEntCode", bmsOut.getEntCode());
		holdParam.put("srchOdJobNo", bmsOut.getOdJobNo());
		holdParam.put("dateFormat", "23");
		
		Util.accessControlDatabase(holdParam, "dateFormat", false, null, null, null, null);
		List<OrderHold> orderHoldList = orderMapper.selectHold(holdParam);
		for(OrderHold orderHold : orderHoldList) {
			if(orderHold.getCarryoutStopYn().equals("Y") && orderHold.getHoldReleaseYn().equals("N")) {
				bmsOutSaveResult.setResultCode("ABMS100036");// 저장 되었습니다.
				bmsOutSaveResult.setHoldReason(orderHold.getHoldReason());
				return bmsOutSaveResult;
			}
		}
		
		if("PANTK".equals(bmsOut.getEntCode()) && "61".equals(bmsOut.getOutClass()) && "04002001".equals(bmsOut.getOutApprovalNo())) {
			List<String> errList = new ArrayList<String>();
			Map<String, Object> out61Param = new HashMap<String, Object>();
			out61Param.put("cargoReferenceNo", bmsOut.getCargoReferenceNo());
			int cnt61Out = bmsOutMapper.selectCheckBmsOut61Count(out61Param);
			if(cnt61Out > 0) { // 화물관리번호에 해당하는 이고반출(61)이 이미 존재할때
				errList.add("수기 이고반출 저장의 경우, 화물관리번호에 해당하는  이고반출이 이미 있는 경우는 저장");
				errList.add("(반입신고의 일괄이고반출생성을 이용해주세요.)");
				bmsOutSaveResult.setErrList(errList);
				bmsOutSaveResult.setResultCode("ABMS100070");// #1# 할 수 없습니다. #2#
				return bmsOutSaveResult;
			}
			
			CarryIn carryIn = new CarryIn();
			carryIn.setCargoReferenceNo(bmsOut.getCargoReferenceNo());
			CarryIn in = bmsInMapper.selectFirstBmsInByCargoNo(carryIn); // 첫번째 반입
			
			Integer nTicInCnt = bmsInMapper.selectCheckBmsIn21Count(in); // 화물관리번호에 해당하는 재고가있는 이고반입
			
			//nTicInCnt > 0 이상이면 이고반출 생성이 됨.
			if(nTicInCnt == 0) { // 1차 이고반출시
				
				Integer carryInBigsizeCnt = bmsInMapper.checkCarryInBigsizeCount(in); // 거대, 지재권, 집중검사가 전부 N이면 count>0 인 값이 된다
				Integer nTicCnt2 = bmsInMapper.checkCarryInBatchTicCount2(in); 
				log.info("carryInBigsizeCnt ==========" + carryInBigsizeCnt);
				log.info("sumFinalBmsOutOrPlan0 ==========" + nTicCnt2);
				if( Util.null2int(nTicCnt2) == 0 && Util.null2int(carryInBigsizeCnt) > 0) {
					errList.add("이고검사를 추가하지않으면 이고반출 저장");
					errList.add("(pda검사완료 > 검사추가 혹은 거대/집중/지재 지정을 이용해주세요.)");
					bmsOutSaveResult.setErrList(errList);
					bmsOutSaveResult.setResultCode("ABMS100070");// #1# 할 수 없습니다. #2#
					return bmsOutSaveResult;
				}
				
			}else if(nTicInCnt > 0) { // 2차 이고반출시
				errList.add("수기 이고반출 저장의 경우, 화물관리번호에 해당하는  이고반출이 이미 있는 경우는 저장");
				errList.add("(반입신고의 일괄이고반출생성을 이용해주세요.)");
				bmsOutSaveResult.setErrList(errList);
				bmsOutSaveResult.setResultCode("ABMS100070");// #1# 할 수 없습니다. #2#
				return bmsOutSaveResult;
			}
		}
		
		
		BmsOutPlan checkOutPlan = bmsOutMapper.selectBmsOutPlanCheck(bmsOut);
		if(checkOutPlan != null) {
			bmsOut.setOutScanDateTime(checkOutPlan.getOutScanDateTime());
		}
		String bmsWhCode = bmsOut.getBmsWhCode();

		SpCallVo voIn = bmsDisposalService.getBmsOutCustomsReportNo(bmsWhCode);
		//3) TB_BMS_DISPOSAL 에서 TB_BMS_OUT로 데이터 입력
		bmsOut.setCustomsReportNo(voIn.getRetCustomsReportNo());
		int intRow = bmsOutMapper.insertBmsOut(bmsOut);
		// WT,CT 데이터 업데이트 STOCK_WT 계산
		bmsOutMapper.updateInCtWtCbm(bmsOut);
		// 반출승인 승인용적 업데이트 APPROVAL_CBM
		bmsOutMapper.updateOutPlanApprovalCbm(bmsOut);
		// 컨테이너 정보는 전체 삭제후에 입력 한다.
		List<BmsContainer> lstContainer = bmsOut.getContainerList();
		if(null !=  lstContainer) {
			bmsOutMapper.deleteOutContainerByNo(bmsOut);
			for (BmsContainer cont : lstContainer) {
				cont.setOutNo(bmsOut.getOutNo());
				bmsOutMapper.insertOutContainer(cont);
			}
		}
		// 반출승인자료 존재여부 확인 추가 2025.11.11 2sowell
		if(checkOutPlan != null) {
			bmsOut.setOutPlanNo(checkOutPlan.getOutPlanNo());	// 조회한 OUT_PLAN_NO 로 설정 2025.11.19
			// 반출 OUT_PLAN_NO 업데이트 추가 
			bmsOutMapper.updateBmsOutPlanNo(bmsOut);
			// 반출여부 Y로 해서 처리
			updateBmsOutYn_Y(bmsOut);
		}
		
		// 반출구분이 (53.목록통관특송물품반출, 52.B/L제시인도물품반출, 55.선용품반입검사반출, 63.일괄구매물품B/L분할반출)인 경우 접수처리로 업데이트 2026.01.02 2sowell
		String outClass = Util.null2str(bmsOut.getOutClass());
		if(outClass.equals("53") || outClass.equals("52") || outClass.equals("55") || outClass.equals("63")) {
			bmsOutMapper.updateBmsOutLastPodStatus(bmsOut);
		}
		
		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(bmsOut.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(bmsOut.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(bmsOut.getRegisterUserId()));
		//processLogs.setLogJobType(BmsConsts.DbType.BMS_OUT);
		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setDbType(BmsConsts.DbType.BMS_OUT);

		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
		processLogs.setDescription(BmsConsts.DbType.BMS_OUT+" [입력 NO:"+String.valueOf(bmsOut.getOutNo())+"]");
		processLogs.setJobNo(String.valueOf(bmsOut.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(bmsOut));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(bmsOut.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(bmsOut.getLogUserName());
		processLogs.setProcessUserNameEng(bmsOut.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(bmsOut.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.INSERT /* pod Status 값 매핑 */
				, ""
				, bmsOut
		);

		Map<String, Object> bmsOutMap = new HashMap<String, Object>();
		bmsOutMap.put("outNo", bmsOut.getOutNo());
		bmsOutMap.put("outClass", bmsOut.getOutClass());
		bmsOutSaveResult.setResultData(bmsOutMap);
		bmsOutSaveResult.setResultCode("A012051");// 저장 되었습니다.
		return bmsOutSaveResult;
	}



	public BmsOutSaveResult updateBmsOut(BmsOut bmsOut, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();

		// 컨테이너 정보는 전체 삭제후에 입력 한다.
		List<BmsContainer> lstContainer = bmsOut.getContainerList();
		if(null !=  lstContainer) {
			bmsOutMapper.deleteOutContainerByNo(bmsOut);
			for (BmsContainer cont : lstContainer) {
				cont.setOutNo(bmsOut.getOutNo());
				bmsOutMapper.insertOutContainer(cont);
			}
		}

		int intRow = bmsOutMapper.updateBmsOut(bmsOut);

		// WT,CT 데이터 업데이트
		bmsOutMapper.updateInCtWtCbm(bmsOut);
		
		// 반출승인자료 존재여부 확인후 반출승인쪽 OUT_YN 업데이트 추가 2025.11.18 2sowell
		BmsOutPlan checkOutPlan = bmsOutMapper.selectBmsOutPlanCheck(bmsOut);
		if(checkOutPlan != null) {
			bmsOut.setOutPlanNo(checkOutPlan.getOutPlanNo());	// 조회한 OUT_PLAN_NO 로 설정 2025.11.19
			// 반출 OUT_PLAN_NO 업데이트 추가 
			bmsOutMapper.updateBmsOutPlanNo(bmsOut);
			// 반출여부 Y로 해서 처리
			updateBmsOutYn_Y(bmsOut);
		}
		
		// 반출구분이 (53.목록통관특송물품반출, 52.B/L제시인도물품반출, 55.선용품반입검사반출, 63.일괄구매물품B/L분할반출)인 경우 접수처리로 업데이트 2026.01.02 2sowell
		String outClass = Util.null2str(bmsOut.getOutClass());
		if(outClass.equals("53") || outClass.equals("52") || outClass.equals("55") || outClass.equals("63")) {
			bmsOutMapper.updateBmsOutLastPodStatus(bmsOut);
		}
		
		bmsOutSaveResult.setResultCode("A012051"); // 저장 되었습니다.


		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(bmsOut.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(bmsOut.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(bmsOut.getRegisterUserId()));
		//processLogs.setLogJobType(BmsConsts.DbType.BMS_OUT);
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);

		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
		processLogs.setDescription(BmsConsts.DbType.BMS_OUT+" [수정 NO:"+String.valueOf(bmsOut.getOutNo())+"]");
		processLogs.setJobNo(String.valueOf(bmsOut.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(bmsOut));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(bmsOut.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(bmsOut.getLogUserName());
		processLogs.setProcessUserNameEng(bmsOut.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(bmsOut.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
				, ""
				, bmsOut
		);
		return bmsOutSaveResult;
	}
	
	/**
	 * 견품반출 - insert/update
	 * @param bmsOutSample
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult insertBmsOutSample(BmsOutSample bmsOutSample, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		
		Map<String, Object> bmsInFirstInParam = new HashMap<String, Object>();
    	bmsInFirstInParam.put("cargoReferenceNo", bmsOutSample.getCargoReferenceNo());
    	InventoryIn bmsInFirst = bmsOutMapper.selectBmsInFirstInByCargoNo(bmsInFirstInParam);
		
    	bmsOutSample.setInNo(bmsInFirst.getInNo());
		int intRow = bmsOutMapper.insertBmsOutSample(bmsOutSample);

		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(bmsOutSample.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(bmsOutSample.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(bmsOutSample.getRegisterUserId()));
		//processLogs.setLogJobType(BmsConsts.DbType.BMS_OUT);
		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setDbType(BmsConsts.DbType.BMS_OUT_SAMPLE);

		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
		processLogs.setDescription(BmsConsts.DbType.BMS_OUT_SAMPLE+" [입력 inNO:"+String.valueOf(bmsOutSample.getInNo())+"]");
		processLogs.setJobNo(String.valueOf(bmsOutSample.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(bmsOutSample));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(bmsOutSample.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(bmsOutSample.getLogUserName());
		processLogs.setProcessUserNameEng(bmsOutSample.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(bmsOutSample.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.INSERT /* pod Status 값 매핑 */
				, ""
				, bmsOutSample
		);

		bmsOutSaveResult.setResultCode("A012051");// 저장 되었습니다.
		return bmsOutSaveResult;
	}

	public BmsOutSaveResult updateBmsOutSample(BmsOutSample bmsOutSample, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();

		int intRow = bmsOutMapper.updateBmsOutSample(bmsOutSample);

		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(bmsOutSample.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(bmsOutSample.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(bmsOutSample.getRegisterUserId()));
		//processLogs.setLogJobType(BmsConsts.DbType.BMS_OUT);
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);

		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setDbType(BmsConsts.DbType.BMS_OUT_SAMPLE);
		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
		processLogs.setDescription(BmsConsts.DbType.BMS_OUT_SAMPLE+" [수정 inNO:"+String.valueOf(bmsOutSample.getInNo())+"]");
		processLogs.setJobNo(String.valueOf(bmsOutSample.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(bmsOutSample));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(bmsOutSample.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(bmsOutSample.getLogUserName());
		processLogs.setProcessUserNameEng(bmsOutSample.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(bmsOutSample.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
				, ""
				, bmsOutSample
		);
		
		bmsOutSaveResult.setResultCode("A012051"); // 저장 되었습니다.
		return bmsOutSaveResult;
	}
	
	public BmsOutSaveResult updateResetSampleIn(BmsOutSample bmsOutSample, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		
		//List<Map<String, Object>> currentSampleList = bmsOutMapper.selectSampleWithPkgList(bmsOutSample);
		//for(Map<String, Object> currentSample : currentSampleList) {
			//if("".equals(Util.null2str(bmsOutSample.getReturnDate())) && Util.null2int(currentSample.get("SAMPLE_IN_CT")) != 0) {
				bmsOutSample.setReturnTime("");
				bmsOutSample.setReturnDate("");
				bmsOutSample.setReturnRemarks("");
				//bmsOutSample.setReturnAcceptor("");
				
				Map<String, Object> pkgParam = new HashMap<String, Object>();
				pkgParam.put("cargoReferenceNo", bmsOutSample.getCargoReferenceNo());
				pkgParam.put("sampleInCt", 0);
				pkgParam.put("updateUserId", bmsOutSample.getRegisterUserId());
				bmsOutMapper.updatePkgSampleInfo(pkgParam);
				//break;
			//}
		//}
		
		bmsOutMapper.updateReturnDateFromSample(bmsOutSample);

		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(bmsOutSample.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(bmsOutSample.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(bmsOutSample.getRegisterUserId()));
		//processLogs.setLogJobType(BmsConsts.DbType.BMS_OUT);
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);

		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setDbType(BmsConsts.DbType.BMS_OUT_SAMPLE);
		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
		processLogs.setDescription(BmsConsts.DbType.BMS_OUT_SAMPLE+" [수정 inNO:"+String.valueOf(bmsOutSample.getInNo())+"]");
		processLogs.setJobNo(String.valueOf(bmsOutSample.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(bmsOutSample));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(bmsOutSample.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(bmsOutSample.getLogUserName());
		processLogs.setProcessUserNameEng(bmsOutSample.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(bmsOutSample.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
				, "견품반출 재입고 초기화"
				, bmsOutSample
		);
		
		bmsOutSaveResult.setResultCode("A012051"); // 저장 되었습니다.
		return bmsOutSaveResult;
	}

	public BmsOutSaveResult deleteBmsOutAndOutPlan(DeleteBmsOut delete, HttpServletRequest request) throws Exception {
		if(BmsConsts.BMS_CONST_OUT_PLAN.equals(delete.getCarryInState())) { // 반출 승인 통고
			return this.deleteBmsOutPlan(delete, request);
		}else if(BmsConsts.BMS_CONST_OUT.equals(delete.getCarryInState())) { // OUT
			return this.deleteBmsOut(delete, request);
		}else if(BmsConsts.BMS_CONST_SAMPLE.equals(delete.getCarryInState())) { // 견품반출
			return this.deleteBmsOutSample(delete, request);
		}else {  // 에러 리턴
			return new BmsOutSaveResult();
		}
	}

	/***
	 * 	반출 삭제 처리
	 * 	 반출(TB_BMS_OUT) 에 신고상태(LAST_POD_STATUS) 가 'BWH01'(입력) 이 아닌 경우  변수삭제못함COUNT 1건 추
	 * 반입 처리후 이력 남김
	 * @param delete
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult deleteBmsOut(DeleteBmsOut delete, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();

		BmsOut out ;
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		int resultCnt =0 ; //삭제 처리 카운트
		int failCnt =0; // 삭제 처리 불가 카운트
		SearchBmsOut searchBmsOut = new SearchBmsOut();
		for (Map<String, Object> deleteMap : deleteList) {
			searchBmsOut.setSrchOutNo(Integer.parseInt(Util.null2str(deleteMap.get("outNo"))));
			out = bmsOutMapper.selectBmsOutDetail(searchBmsOut);
			log.info("delete BmsOut outNo:{}", out.getOutNo());
			if (null != out) {
				if( ("BMS01".equals(out.getLastPodStatus()))  //입력
				||  ("BMS02".equals(out.getLastPodStatus()))  //송신실패
				||  ("BMS05".equals(out.getLastPodStatus())) ) {  // 오류 일때 삭제 처리 /입력, 송신실패, 오류
					resultCnt++;
					bmsOutMapper.updateInitPkgAndTic(out);
					bmsOutMapper.deleteBmsOut(out);
					// WT,CT 데이터 업데이트
					bmsOutMapper.updateInCtWtCbm(out);
					updateBmsOutYn_N(out);
					// UPDATE BMS_OUT_PLAN.OUT_YN ='N' ( 재고 )에는 Y 로 넣는다.
					//
					// 8.13 추가요청 : 반출구분이 83.체화폐기반출인 경우 UPDATE BMS_DISPOSAL.OUT_YN ='N'
					if("83".equals(out.getOutClass())) {
						updateBmsDisposalOutYn_N(out);
					}
					
					if("PANTK".equals(delete.getEntCode()) && "61".equals(out.getOutClass()) ){
						Map<String, Object> param = new HashMap<String, Object>();
						param.put("cargoReferenceNo", out.getCargoReferenceNo());
						BmsOutPlan outPlan = bmsOutMapper.selectOutPlanNotExistInNo(param);
						if(outPlan != null) {
							List<InventoryIn> bmsInList = bmsOutMapper.selectBmsInListByCargoNo(param);
							List<InventoryIn> bmsInExistStockList = bmsInList.stream().filter(bmsIn -> bmsIn.getStockCt() > 0).collect(Collectors.toList());
							InventoryIn bmsIn = bmsInExistStockList.get(0);
							
							outPlan.setInNo(bmsIn.getInNo());
							bmsOutMapper.updateInNoOfOutPlan(outPlan);
						}
					}
					
					if( ("UPSXK".equals(delete.getEntCode()) || "YHTXX".equals(delete.getEntCode()) ) && "61".equals(out.getOutClass()) ){
						Map<String, Object> param = new HashMap<String, Object>();
						param.put("cargoReferenceNo", out.getCargoReferenceNo());
						Map<String, Object>  bmsInExistStock = bmsOutMapper.selectBmsInExistStock(param);
						if(bmsInExistStock != null) {
							Map<String, Object> bmsInMinusStock = bmsOutMapper.selectBmsInMinusStock(param);
							BmsOutPlan outPlan31 = bmsOutMapper.selectOutPlan31(param);
							if(outPlan31 != null) {
								outPlan31.setInNo(Util.null2int(bmsInExistStock.get("IN_NO")));
								outPlan31.setUpdateUserId(Util.null2str(delete.getInputUserId()));
								bmsOutMapper.updateInNoOfOutPlan(outPlan31);
							}
							if(bmsInMinusStock != null) {
								bmsInMinusStock.put("inNo", bmsInMinusStock.get("IN_NO"));
								bmsInMinusStock.put("inNoOfExistStock", bmsInExistStock.get("IN_NO"));
								bmsInMinusStock.put("updateUserId", Util.null2str(delete.getInputUserId()));
								bmsOutMapper.updateInNoOfBmsOut(bmsInMinusStock);
								
								BmsOut minusStockParam = new BmsOut();
								minusStockParam.setInNo(Util.null2int(bmsInMinusStock.get("IN_NO")));
								bmsOutMapper.updateInCtWtCbm(minusStockParam);
								
								BmsOut existStockParam = new BmsOut();
								existStockParam.setInNo(Util.null2int(bmsInExistStock.get("IN_NO")));
								bmsOutMapper.updateInCtWtCbm(existStockParam);
							}
						}
					}
					
					//userAuth가 AD인 경우 사용건수 있더라도 수정이 가능하도록 수정됨/이때 시스템로그 남기도록 추가
					// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(delete.getEntCode()));
					processLogs.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
					processLogs.setLogJobType(BmsConsts.DbType.BMS_OUT);
					processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
					processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
					//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
					processLogs.setDescription(BmsConsts.DbType.BMS_OUT+"[삭제 NO:" + String.valueOf(deleteMap.get("outNo")) + ", 삭제사유:" + delete.getReason() + "]");
					processLogs.setJobNo(String.valueOf(deleteMap.get("odJobNo")));
					processLogs.setJobLog(new Gson().toJson(deleteMap));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
					processLogs.setProcessUserName(delete.getLogUserName());
					processLogs.setProcessUserNameEng(delete.getLogUserNameEng());

					String blNo = String.valueOf(deleteMap.get("blNo"));
					// 호출시 필요 항목 : BL_NO, POD_STATUS
					bmsCommService.insertLogAndPodStatus(processLogs, request
							, BmsConsts.PodStatus.DELETE /* pod Status 값 매핑 */
							, delete.getReason()
							, null
					);
				}else{
					failCnt++ ;
				}
			} else {// not null
				failCnt++ ;
			}
		}
		result.setFailCnt(failCnt);
		result.setResultCnt(resultCnt);
		return result;
	}

	/***
	 * 반출 승인 통고 데이터 삭제 처리  반출승인내역통보(TB_BMS_OUT_PLAN)에 반출처리여부(OUT_YN) 가 'Y' 경우  변수삭제못함COUNT 1건 추가
	 * @param delete
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult deleteBmsOutPlan(DeleteBmsOut delete, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();

		BmsOutPlan outPlan ;
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		int resultCnt =0 ;
		int failCnt =0; // 삭제 처리 불가 카운트
		SearchBmsOut searchBmsOut = new SearchBmsOut();
		for (Map<String, Object> deleteMap : deleteList) {
			searchBmsOut.setSrchOutPlanNo(Integer.parseInt(Util.null2str(deleteMap.get("outPlanNo"))));
			outPlan = bmsOutMapper.selectBmsOutPlanDetail(searchBmsOut);
			if (null != outPlan) {
				if("N".equals(outPlan.getOutYn())) {  // OUT_YN이 N일경우에만 승인 통고 가능함
					resultCnt++;
					log.info("delete BmsOutPlan outPlanNo:{}", outPlan.getOutPlanNo());
					bmsOutMapper.deleteBmsOutPlan(outPlan);
					//userAuth가 AD인 경우 사용건수 있더라도 수정이 가능하도록 수정됨/이때 시스템로그 남기도록 추가
					// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(delete.getEntCode()));
					processLogs.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
					processLogs.setLogJobType("DATA");
					processLogs.setBound("AIR");
					processLogs.setBoundType("IMP");
					processLogs.setDbType(BmsConsts.DbType.BMS_OUT_PLAN);
					//processLogs.setLogJobType(BmsConsts.DbType.BMS_OUT_PLAN);
					processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
					//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
					processLogs.setDescription(BmsConsts.DbType.BMS_OUT_PLAN+" [삭제 NO:" + String.valueOf(deleteMap.get("outPlanNo")) + ", 삭제사유:" + delete.getReason() + "]");
					processLogs.setJobNo(String.valueOf(deleteMap.get("odJobNo")));
					processLogs.setJobLog(new Gson().toJson(deleteMap));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
					processLogs.setProcessUserName(delete.getLogUserName());
					processLogs.setProcessUserNameEng(delete.getLogUserNameEng());

					String blNo = String.valueOf(deleteMap.get("blNo"));
					// 호출시 필요 항목 : BL_NO, POD_STATUS
					bmsCommService.insertLogAndPodStatus(processLogs, request
							, BmsConsts.PodStatus.DELETE /* pod Status 값 매핑 */
							, delete.getReason()
							, null
					);
				}else{
					failCnt++;
				}
			}else{ // null
				failCnt++;
			}
		}
		//
		result.setFailCnt(failCnt);
		result.setResultCnt(resultCnt);
		return result;
	}

	/***
	 * 견품반출 데이터 삭제 처리  견품반출(TB_BMS_SAMPLE)에 재입고일자(RETURN_DATE) 가 공백이 아닌경우 변수삭제못함COUNT 1건 추가
	 * @param delete
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult deleteBmsOutSample(DeleteBmsOut delete, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();

		BmsOutSample outSample = new BmsOutSample();
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		int resultCnt =0 ;
		int returnCnt =0; // 삭제 처리 불가 카운트(재입고건)
		SearchBmsOut searchBmsOut = new SearchBmsOut();
		for (Map<String, Object> deleteMap : deleteList) {
			searchBmsOut.setSrchInNo(Integer.parseInt(Util.null2str(deleteMap.get("inNo"))));
			outSample = bmsOutMapper.selectBmsOutSampleDetail(searchBmsOut);
			if (null != outSample) {
				if("".equals(outSample.getReturnDate())) {  // RETURN_DATE이 공백일경우에만 삭제 가능함
					resultCnt++;
					log.info("delete BmsOutSample inNo:{}", outSample.getInNo());
					bmsOutMapper.deleteBmsOutSample(outSample);
					//userAuth가 AD인 경우 사용건수 있더라도 수정이 가능하도록 수정됨/이때 시스템로그 남기도록 추가
					// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(delete.getEntCode()));
					processLogs.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
					processLogs.setLogJobType("DATA");
					processLogs.setBound("AIR");
					processLogs.setBoundType("IMP");
					processLogs.setDbType(BmsConsts.DbType.BMS_OUT_SAMPLE);
					//processLogs.setLogJobType(BmsConsts.DbType.BMS_OUT_SAMPLE);
					processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
					//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
					processLogs.setDescription(BmsConsts.DbType.BMS_OUT_SAMPLE+" [삭제 inNO:" + String.valueOf(deleteMap.get("inNo")) + ", 삭제사유:" + delete.getReason() + "]");
					processLogs.setJobNo(String.valueOf(deleteMap.get("odJobNo")));
					processLogs.setJobLog(new Gson().toJson(deleteMap));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
					processLogs.setProcessUserName(delete.getLogUserName());
					processLogs.setProcessUserNameEng(delete.getLogUserNameEng());

					String blNo = String.valueOf(deleteMap.get("blNo"));
					// 호출시 필요 항목 : BL_NO, POD_STATUS
					bmsCommService.insertLogAndPodStatus(processLogs, request
							, BmsConsts.PodStatus.DELETE /* pod Status 값 매핑 */
							, delete.getReason()
							, null
					);
				}else{ // RETURN_DATE 공백이 아니면 재입고 처리된 경우
					returnCnt++;
				}
			}else{ // null
				returnCnt++;
			}
		}
		//
		result.setReturnCnt(returnCnt);
		result.setResultCnt(resultCnt);
		return result;
	}

	/**
	 * 5) 반출 -> 반입 일괄 생성 : 3. 특송장이고반입일괄생성
	 * @param batchOut
	 * @param request
	 * @return
	 * @throws Exception
	 */
	 public BmsOutSaveResult executeBatchOutToIn(BatchBmsOut batchOut, HttpServletRequest request) throws Exception {
		 BmsOutSaveResult result = new BmsOutSaveResult();
		 String batchExecType = batchOut.getBatchExecType(); //DISP,OUT
		 String inputUserId = batchOut.getInputUserId();
		 String dateTimeFm = batchOut.getDateFormat();
		 String gmtTime = batchOut.getGmtTime();
		 String entCode = batchOut.getEntCode();
		 String entBusinessCode = batchOut.getEntBusinessCode();
		 List<String> returnLst = new ArrayList<>();
		 List<String> errorLst = new ArrayList<>();
		 Integer nResultCnt = 0;
		 // blList
		 if ("BATCH_OUT_IN".equals(batchExecType)) { // OUT" => IN  // 특송장 이고 반입 생성
			 String bmsWhCode = batchOut.getBmsWhCode();
			 int succSeq = 1;
			 
			 for (BmsOut out : batchOut.getBmsOutList()) {
				 String logVal = "batch  OUT => IN  batchOut:" + out;
				 log.info(logVal);
				 //1) TB_BMS_IN.LAST_POD_STATUS 'BWH0' 접수 리스트 확인
				 out.setRegisterUserId(inputUserId);
				 out.setUpdateUserId(inputUserId);
				 out.setEntCode(entCode);
				 out.setEntBusinessCode(entBusinessCode);
				 //  반입 (TB_BMS_IN) 검색시 화물관리번호 (CARGO_REFERENCE_NO) AND 반입구분(OUT_CLASS)  = '21' AND 반입근거번호(IN_APPROVAL_NO) = '04002001'
				 Integer nCnt = bmsOutMapper.selectOutBatchCheckInCount(out);
				 //2) 1에서 자료있으면 list에 넣어서  리스트 제외 함
				 if (nCnt > 0) {
					 returnLst.add(out.getBlNo()); // 중복 체크
				 } else {
					 if( !"61".equals(out.getOutClass()) ) { // 특송장이고 반입일괄생성시 체크 조건 추가 07.31 khs
						// returnLst.add("BL:"+out.getBlNo()+" 이고반출,근거번호04002001 확인");
						 errorLst.add(  out.getBlNo() );
						 continue;
					 }
					 BmsOut latest61Out = bmsOutMapper.selectLatest61CarryOut(out);
					 if(!Objects.equals(out.getOutNo(), latest61Out.getOutNo())) { // 참조할 반출대상 == 가장 최근 이고반출 일때만
						 log.info("대상건 outNo: {}, latest 61반출: {}", out.getOutNo(), latest61Out.getOutNo());
						 errorLst.add(  out.getBlNo() );
						 continue;
					 }
					 
						 SpCallVo voIn = bmsDisposalService.getBmsInCustomsReportNo(bmsWhCode);
						 //3) TB_BMS_DISPOSAL 에서 TB_BMS_OUT로 데이터 입력
						 out.setCustomsReportNo(voIn.getRetCustomsReportNo());
						 bmsOutMapper.insertFromOutToInBatchGen(out);


						 nResultCnt++; // 성공 카운트
						 
						// 배차예정정보(TB_BMS_ALLOCATE_CAR_NOTI) 저장
						Map<String, Object> tmpMap = new HashMap<String, Object>();
						tmpMap.put("bmsWhCode", bmsWhCode);
						tmpMap.put("registerUserId", inputUserId);
						tmpMap.put("updateUserId", inputUserId);
						bmsOutMapper.insertBmsAllocateCarNotiByInClass(tmpMap);

						 // BMS_IN 이관 로그 적용 BmsConsts.PodStatus.DELETE
						 // elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
						 ProcessLogs processLogs = new ProcessLogs();
						 processLogs.setEntCode(Util.null2str(batchOut.getEntCode()));
						 processLogs.setEntBusinessCode(Util.null2str(batchOut.getEntBusinessCode()));
						 processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
						 processLogs.setProcessUserId(Util.null2str(batchOut.getInputUserId()));
					//	 processLogs.setLogJobType(BmsConsts.DbType.BMS_IN);
						 processLogs.setJobFuncType(BmsConsts.JobFuncType.BATCH);
						 processLogs.setLogJobType("DATA");
						 processLogs.setBound("AIR");
						 processLogs.setBoundType("IMP");
						 processLogs.setDbType(BmsConsts.DbType.BMS_IN);

						 //logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
						 processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [BATCH OUT_NO:"+String.valueOf(out.getOutNo())+"]");
						 processLogs.setJobNo(String.valueOf(out.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
						 processLogs.setJobLog(new Gson().toJson(out));
						 processLogs.setResult("ok");
						 processLogs.setEntBusinessPrintName(out.getLogUserBusinessPrintName());
						 processLogs.setProcessUserName(out.getLogUserName());
						 processLogs.setProcessUserNameEng(out.getLogUserNameEng());
						 //	String podStatus = BmsConsts.PodStatus.BMS99;
						 //String blNo = String.valueOf(bmsOut.getBlNo());
						 // 호출시 필요 항목 : BL_NO, POD_STATUS
						 bmsCommService.insertLogAndPodStatus(processLogs, request
								 , BmsConsts.PodStatus.BATCH /* pod Status 값 매핑 */
								 , ""
								 , null
						 );
						 
						 if("BMSA4".equals(out.getLastPodStatus()) && "04002001".equals(out.getOutApprovalNo()) && "61".equals(out.getOutClass()) ) {
						 	Map<String, Object> updateParam = new HashMap<String, Object>();
						    updateParam.put("inputUserId", inputUserId);
				        	updateParam.put("inNo", out.getNewInNo());
				        	updateParam.put("lastPodStatus", "BMSA4");
				        	bmsOutMapper.updateLastPodStatusFrom21BmsIn(updateParam);
				        	
				        	updateParam.put("outNo", out.getOutNo());
				        	bmsOutMapper.updateBmsOutSetInNo(updateParam);
				        	
				        	BmsOut outParam = new BmsOut();
				        	outParam.setInNo(Util.null2int(updateParam.get("inNo")));
				        	// WT,CT 데이터 업데이트
							bmsOutMapper.updateInCtWtCbm(outParam);
				        	
							updateParam.put("inNo", out.getNewInNo());
							InventoryIn bmsIn21 = bmsOutMapper.selectBmsInByInNo(updateParam); // 이번에 생성된 이고반입(21)을 조회
							
				        	Map<String, Object> bmsInFirstInParam = new HashMap<String, Object>();
				        	bmsInFirstInParam.put("cargoReferenceNo", out.getCargoReferenceNo());
				        	InventoryIn bmsInBeforeTicOut = bmsOutMapper.selectBmsInFirstInByCargoNo(bmsInFirstInParam); // key.setInNo(inspectReulst.getInNo());
				        	
				        	Map<String, Object> restoreBmsInParam = new HashMap<String, Object>();
				        	restoreBmsInParam.put("inputUserId", inputUserId);
				        	restoreBmsInParam.put("inNo", bmsInBeforeTicOut.getInNo()); // 입항반입
				        	restoreBmsInParam.put("outNoticeDate", bmsIn21.getOutNoticeDate()); // 이고반입(21)의 통고일자
				        	restoreBmsInParam.put("periodOverDate", bmsIn21.getPeriodOverDate()); //이고반입(21)의 체화일자
				        	bmsOutMapper.updateBmsInRestoreBeforeBmsOut(restoreBmsInParam);
				        	
				        	Map<String, Object> param = new HashMap<String, Object>();
				        	param.put("inNo", out.getInNo());
				        	param.put("outBackInFinishYn", "Y");
				        	bmsOutMapper.updateTicOutBackInFinish(param);
				        	
							log.info("위에서 새로 생성한 이고반입의 신고상태를 수입통관이고(BMSA4)로 변경, 수입통관이고(BMSA4)이고반출의 반입번호를 수입통관이고(BMSA4)로변경한 이고반입의 반입번호로 교체, 입항 또는 보세운송반입의 재고수량은 반입수량을 넣어서 복구 ");
				        	continue;
						 }
						 
						 // unipass조사후 송신 혹은 상태 업데이트
						 CargoProgressInfo cpInfo = bmsSendUtils.getCargoProgressInfo(out.getCargoReferenceNo()); // 테스트
				         List<CargCsclPrgsInfoDtail> cargoLogList  = cpInfo.getCargCsclPrgsInfoDtails();
				         
				         if(cargoLogList == null) {
				         	log.info("유니패스 정보없음 skip");
				         	continue;
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
				         	log.info("유니패스: (" + bmsWhCode + ") 우리창고에서에서 세관검사장으로의 이고반출내역 없음 skip");
				         	continue;
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
				         	log.info("04002001에 A)이고반출(적출입작업),  B)목록통관특송물품 반출 등이 없음 skip");
				         	continue;
				         }
                         
				         // `이고반출(적출입작업) or 목록통관특송물품 반출이 있는데이터`의 `가장 최근 rlbrDttm` 구하기
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
				         	continue;
				         }
				         
				         
				         List<CargCsclPrgsInfoDtail> needUpdateCaseList = outside10SecondsLogList.stream()
				         		.filter(detail -> "목록통관특송물품 반출".equals(detail.getRlbrCn())) 
				                .collect(Collectors.toList());
				         if (needUpdateCaseList != null && !needUpdateCaseList.isEmpty()) {
					         Map<String, Object> updateParam = new HashMap<String, Object>();
					         updateParam.put("inputUserId", inputUserId);
					         
					         updateParam.put("inNo", out.getNewInNo());
					         updateParam.put("lastPodStatus", "BMSA3");
					         bmsOutMapper.updateLastPodStatusFrom21BmsIn(updateParam);
					         
					         Map<String, Object> ticParam = new HashMap<String, Object>();
					         ticParam.put("inNo", out.getInNo());
					         ticParam.put("outBackInFinishYn", "Y");
					         bmsOutMapper.updateTicOutBackInFinish(ticParam);
					         
					         log.info("04002001에 B)목록통관특송물품 반출, 반입데이터를 통관후입고(BMSA3)로 업데이트");
	                          
						     log.info("통관후입고반출(BmsOut) 생성");
						     SpCallVo voInOut = bmsDisposalService.getBmsOutCustomsReportNo(bmsWhCode); // 반출신고번호 채번
						     CargCsclPrgsInfoDtail cargoInfo = needUpdateCaseList.get(needUpdateCaseList.size() - 1);
						     
						     String outApprovalNo = "TIC" + Util.null2str(cargoInfo.getRlbrBssNo());
						     
						     BmsOut bmsOutParam = new BmsOut();
						     bmsOutParam.setInNo(out.getNewInNo());
						     bmsOutParam.setCustomsReportNo(voInOut.getRetCustomsReportNo());
						     bmsOutParam.setOutApprovalNo(outApprovalNo);
						     bmsOutParam.setRegisterUserId(inputUserId);
						     bmsOutMapper.insert53BmsOutBMSA3(bmsOutParam);
						     
							 String succReferenceNoId = "";
							 String succDataIdPrefix = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5);
						     if(bmsOutParam.getOutNo() != null) {
						    	 bmsOutMapper.updateInCtWtCbm(bmsOutParam); // 반입 재고 업데이트
							 	 /** 작업내역 로그 */
							 	 try {
							 	 	succReferenceNoId = succDataIdPrefix + Util.setSeqNo(succSeq++);
							 	 } catch (Exception e) {
							 	 	succReferenceNoId = succDataIdPrefix + "00001";
							 	 }
							 	
						         BmsEcmPod pod = new BmsEcmPod();
						         pod.setPodJobNo(succReferenceNoId);
						         pod.setEntCode(entCode);
						         pod.setOdJobNo(String.valueOf(out.getOdJobNo()));
						         pod.setPodName("목록반출");
						         pod.setRemarks("");
						         pod.setRegisterUserId(inputUserId);
							 	 bmsOutMapper.insertBmsEcmPod(pod);
							 	 
							 	 if("04077009".equals(bmsWhCode)) {
							 	    Map<String, Object> hmsOutPlanParam = new HashMap<String, Object>();
							 		hmsOutPlanParam.put("approvalClass", "31");
							 		hmsOutPlanParam.put("odJobNo", Util.null2str(out.getOdJobNo()));
							 		hmsOutPlanParam.put("blNo", out.getBlNo());
							 		hmsOutPlanParam.put("outApprovalNo", outApprovalNo);
								 	hmsOutPlanParam.put("outClass", "53"); // 목록반출
							 		bmsOutMapper.insertOutPlanTempForHms(hmsOutPlanParam);
							 	 }
					         }
					         
				         	 continue;
				         }
				         
				         log.info("unipass조회후, 처리 끝");
						  
					
				 }
			 }
		 }
		 result.setReturnList(returnLst);
		 result.setErrList(errorLst);
		 result.setResultCnt(nResultCnt);
		 return result;
	 }


	/**
	 * 반출에서 등록시 OUT_PLAN 테이블의 OUT_YN을 Y으로 변경
	 * @param bmsOut
	 */
	private void updateBmsOutYn_Y(BmsOut bmsOut) {
		 // bmsOut
		bmsOut.setOutYn("Y");
		bmsOutMapper.updateBmsPlanOutYn(bmsOut);
	}

	/**
	 * 반출에서 삭제시 OUT_PLAN 테이블의 OUT_YN을 N으로 변경
	 * @param bmsOut
	 */
	private void updateBmsOutYn_N(BmsOut bmsOut) {
		// bmsOut
		bmsOut.setOutYn("N");
		bmsOutMapper.updateBmsPlanOutYn(bmsOut);
	}
	
	/**
	 * 반출에서 삭제시 DISPOSAL 테이블의 OUT_YN을 N으로 변경
	 * @param bmsOut
	 */
	private void updateBmsDisposalOutYn_N(BmsOut bmsOut) {
		// bmsOut
		Disposal disp = null;
		SearchDisposal search = new SearchDisposal();
		search.setSrchInNo(bmsOut.getInNo());
		search.setCargoReferenceNo(bmsOut.getCargoReferenceNo());
		
		disp = bmsDisposalMapper.selectDisposalDetail(search);
		disp.setOutYn("N");
		
		bmsDisposalMapper.updateBmsDisposalOutYn(disp);
	}

	/**
	 * 선반입반출신고독려통보 조회 List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsEncourageNotiList selectBmsEncourageNotiList(SearchBmsOut search) throws Exception {
		BmsEncourageNotiList bmsEncourageNotiList = new BmsEncourageNotiList();

		setSearchCondition(search);

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYIN_DATE|APPROVAL_DATE|noDateChk", null, null);
		List<BmsEncourageNoti> list = bmsOutMapper.selectBmsEncourageNotiList(search);
		bmsEncourageNotiList.setBmsEncourageNotiList(list);

		if(list.size() > 0) {
			bmsEncourageNotiList.setTotal(list.get(0).getTotal());
		} else {
			bmsEncourageNotiList.setTotal(0);
		}

		return bmsEncourageNotiList;
	}
	
	/**
	 * 정정전원본 키 조회
	 * @param customsReportNo
	 * @return
	 * @throws Exception
	 */
	public String getChangeDelInNoByCustomsReportNo(String customsReportNo){
		String outNo = bmsOutMapper.selectChangeDelInNoByCustomsReportNo(customsReportNo);
		return StringEscapeUtils.escapeHtml4(outNo); 
	}

	public String selectErrorDesc(String srchDeclarationNo,String srchDocType){
		String errorDesc = bmsOutMapper.selectErrorDesc(srchDeclarationNo,srchDocType);
		return StringEscapeUtils.escapeHtml4(errorDesc);
	}

	/**
	 * 관세청 XML 송신(Send) - GOVCBR6NB(반출신고서)
	 * @param params
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult sendOutToXml(BatchBmsOut params, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();
		String batchExecType = params.getBatchExecType();

		if("SEND".equals(batchExecType)) {
			result = bmsSendXmlParser.callSendXmlGOVCBR6NBParserExecutor(params, request);
		}

		return result;
	}
	
	/**
	 * 택배사연계 조회 List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsOutDeliveryList selectDeliveryList(SearchBmsOut search) throws Exception {
		BmsOutDeliveryList bmsOutDeliveryList = new BmsOutDeliveryList();

		setSearchCondition(search);

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|D.REGISTER_DATE_TIME|D.DELEVERY_SEND_DATE_TIME|O.APPROVAL_DATE|I.CARRYIN_DATE|noDateChk", "srchNoType", "|O.BL_NO|D.CARGO_REFERENCE_NO|D.PACKING_NO|O.IN_NO");
		List<BmsOutDelivery> list = bmsOutMapper.selectDeliveryList(search);
		bmsOutDeliveryList.setBmsOutDeliveryList(list);

		if(list.size() > 0) {
			bmsOutDeliveryList.setTotal(list.get(0).getTotal());
		} else {
			bmsOutDeliveryList.setTotal(0);
		}

		return bmsOutDeliveryList;
	}
	
	/**
	 * 택배사연계 재전송
	 * @param listData
	 * @return
	 * @throws Exception
	 */
	public Map updateDeliverySeq(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) listData.get("dataList");
		
	    boolean isDeleverySendIfYn = false;
	    for (Map<String, Object> data : dataList) {
	        if (!"E".equals(data.get("deleverySendIfYn"))) {
	        	isDeleverySendIfYn = true;
	            break;
	        }
	    }
	    
	    // 전송상태가 E(오류)가 아닌건이 있을 경우
	    if (isDeleverySendIfYn) {
	        result.put("resultCnt", 0);
	        result.put("resultCode", "ABMS100068"); // #1#
	        return result;
	    }
		
		int resultCnt = bmsOutMapper.updateDeliverySeq(listData);
		
		result.put("resultCnt", resultCnt);
		result.put("resultCode", "ABMS100006"); // #1#건 전송되었습니다.
		return result;
	}
	
	/**
	 * 출고SCAN수기저장 update
	 * @param bmsOut
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult updateOutScan(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) listData.get("dataList");
	    Set<Integer> updatedOutNoSet = new HashSet<>();	// 업데이트된 outNo 저장
		int upCnt = 0;	// 저장건수
		int outNo = 0;
			
		for(Map<String, Object> bmsOutList : dataList) {
			String lastPodStatus = Util.null2str(bmsOutList.get("lastPodStatus"));	              // 진행상태
			String outScanDateTimeStr = Util.null2str(bmsOutList.get("outScanDateTime"));         // OUT쪽 출고스캔일시
			String outPlanScanDateTimeStr = Util.null2str(bmsOutList.get("outPlanScanDateTime")); // OUT_PLAN쪽 출고스캔일시
			
			// BMS05(오류)/BMS01(입력)/BMS02(송신실패)/BMS03(송신)이면 SKIP
			if("BMS05".equals(lastPodStatus) || "BMS01".equals(lastPodStatus) || "BMS02".equals(lastPodStatus) || "BMS03".equals(lastPodStatus)) {
				continue;
			}else {
        		// OUT쪽 출고스캔일시가 1900-01-01인 경우만 업데이트
	            if(outScanDateTimeStr.startsWith("1900-01-01")) {
	            	outNo = Util.null2int(bmsOutList.get("outNo"));

	                int outResult = bmsOutMapper.updateBmsOutScanDateTime(outNo); 
	                if(outResult > 0) {
	                    updatedOutNoSet.add(outNo);
	                }
	                
	        		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
	        		ProcessLogs processLogs = new ProcessLogs();
	        		//processLogs.setEntCode(Util.null2str(bmsOutList.get("entCode")));
	        		String entCode = Util.null2str(listData.get("entCode"));
	        		String itemEntCode = Util.null2str(bmsOutList.get("entCode"));
	                processLogs.setEntCode(itemEntCode.isEmpty() ? entCode : itemEntCode);
	        		processLogs.setEntBusinessCode(Util.null2str(bmsOutList.get("entBusinessCode")));
	        		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
	        		processLogs.setProcessUserId(Util.null2str(bmsOutList.get("registerUserId")));
	        		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
	        		processLogs.setLogJobType("DATA");
	        		processLogs.setBound("AIR");
	        		processLogs.setBoundType("IMP");
	        		processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
	        
	        		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
	        		processLogs.setDescription(BmsConsts.DbType.BMS_OUT+" [입력 NO:"+String.valueOf(bmsOutList.get("outNo"))+"]");
	        		processLogs.setJobNo(String.valueOf(bmsOutList.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
	        		processLogs.setJobLog(new Gson().toJson(bmsOutList));
	        		processLogs.setResult("ok");
	        		processLogs.setEntBusinessPrintName(Util.null2str(bmsOutList.get("logUserBusinessPrintName")));
	        		processLogs.setProcessUserName(Util.null2str(bmsOutList.get("logUserName")));
	        		processLogs.setProcessUserNameEng(Util.null2str(bmsOutList.get("logUserNameEng")));
	        		
	        		String blNo = String.valueOf(bmsOutList.get("blNo"));
	        		// 호출시 필요 항목 : BL_NO, POD_STATUS
	        		bmsCommService.insertLogAndPodStatus(processLogs, request
	        				, BmsConsts.PodStatus.OUT_SCAN /* pod Status 값 매핑 */
	        				, ""
	        				, "출고SCAN수기저장"
	        		);	        	
	                
	            }
	            
        		int outPlanNo = Util.null2int(bmsOutList.get("outPlanNo"));
        		
        		// outPlanNo가 0보다 크고 OUT_PLAN쪽 출고스캔일시가 1900-01-01인 경우만 업데이트
        		if(outPlanNo > 0) {
        			if(outPlanScanDateTimeStr.startsWith("1900-01-01")) {
        				int outPlanResult = bmsOutMapper.updateBmsOutPlanScanDateTime(outPlanNo);
        		           if(outPlanResult > 0) {
        		        	   updatedOutNoSet.add(outNo);
        		            }
        			}
        		}
			}
			upCnt = updatedOutNoSet.size();
		}
		
		bmsOutSaveResult.setUpCnt(upCnt); 
		bmsOutSaveResult.setResultCode("A012051");	// 저장되었습니다.
		return bmsOutSaveResult;
	}
	
	/**
	 * 반출 진행상태를 세관검사장(BMSA2)으로 변경
	 * 
	 * 진행상태가 접수(BMS04)인 건만 대상이며, 그 외 상태는 SKIP한다.
	 * 변경 성공건은 POD 이력(TB_ECM_POD_01~04)과 Elasticsearch 로그를 남긴다.
	 * 
	 * @param listData	dataList(반출 그리드 선택행), entCode, registerUserId, updateUserId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult updateCustomsInspect(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) listData.get("dataList");
		int upCnt = 0;	// 저장건수
		
		if(dataList == null || dataList.isEmpty()) {
			bmsOutSaveResult.setUpCnt(0);
			bmsOutSaveResult.setResultCode("A012051");	// 저장되었습니다.
			return bmsOutSaveResult;
		}
		
		String entCode = Util.null2str(listData.get("entCode"));
		String updateUserId = Util.null2str(listData.get("updateUserId"));
		String registerUserId = Util.null2str(listData.get("registerUserId"));
		
		for(Map<String, Object> bmsOutList : dataList) {
			String lastPodStatus = Util.null2str(bmsOutList.get("lastPodStatus"));	// 진행상태
			int outNo = Util.null2int(bmsOutList.get("outNo"));
			
			// 이미 세관검사장(BMSA2)이면 SKIP
			if(BmsConsts.PodStatus.BMSA2.equals(lastPodStatus)) {
				log.info("updateCustomsInspect (세관검사장변경) 이미 세관검사장 skip outNo: {}", outNo);
				continue;
			}
			
			// 접수(BMS04)이면 SKIP
			if(BmsConsts.PodStatus.BMS04.equals(lastPodStatus)) {
				log.info("updateCustomsInspect (세관검사장변경) 접수(BMS04) 이면 skip outNo: {}, lastPodStatus: {}", outNo, lastPodStatus);
				continue;
			}
			
			Map<String, Object> updateParam = new HashMap<String, Object>();
			updateParam.put("outNo", outNo);
			updateParam.put("updateUserId", updateUserId);
			
			int updateResult = bmsOutMapper.updateBmsOutCustomsInspect(updateParam);
			if(updateResult <= 0) {
				log.info("updateCustomsInspect (세관검사장변경) 업데이트 대상 없음 skip outNo: {}", outNo);
				continue;
			}
			upCnt++;
			
			// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
			ProcessLogs processLogs = new ProcessLogs();
			String itemEntCode = Util.null2str(bmsOutList.get("entCode"));
			processLogs.setEntCode(itemEntCode.isEmpty() ? entCode : itemEntCode);
			processLogs.setEntBusinessCode(Util.null2str(bmsOutList.get("entBusinessCode")));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(registerUserId);
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setLogJobType("DATA");
			processLogs.setBound("AIR");
			processLogs.setBoundType("IMP");
			processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
			
			processLogs.setDescription(BmsConsts.DbType.BMS_OUT+" [세관검사장변경 NO:"+String.valueOf(outNo)+"]");
			processLogs.setJobNo(String.valueOf(bmsOutList.get("odJobNo")));	// ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			processLogs.setJobLog(new Gson().toJson(bmsOutList));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(Util.null2str(bmsOutList.get("logUserBusinessPrintName")));
			processLogs.setProcessUserName(Util.null2str(bmsOutList.get("logUserName")));
			processLogs.setProcessUserNameEng(Util.null2str(bmsOutList.get("logUserNameEng")));
			
			// 호출시 필요 항목 : BL_NO, POD_STATUS
			// DbType이 BMS_OUT일 때의 CUSTOM_POD_NAME 분기를 사용한다. (podName은 마지막 인자 문자열이 그대로 들어감)
			bmsCommService.insertLogAndPodStatus(processLogs, request
					, BmsConsts.PodStatus.CUSTOM_POD_NAME /* pod Status 값 매핑 */
					, ""
					, "세관검사장변경"
			);
		}
		
		bmsOutSaveResult.setUpCnt(upCnt);
		bmsOutSaveResult.setResultCode("A012051");	// 저장되었습니다.
		return bmsOutSaveResult;
	}
	
	// 접수통보후반출수정 반출접수체크
	public BmsOutSaveResult checkLastPodStatus(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();
			
		BmsOut bmsOut = bmsOutMapper.selectBmsOutByOutNoOrOdJobNo(search);
		log.info("checkLastPodStatus (접수통보후반출수정 : 반출접수체크) bmsOut: {}", bmsOut);
		if(bmsOut == null) {
			result.setResultCode("A11481"); // 해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
			
		log.info("checkLastPodStatus (접수통보후반출수정 : 반출접수체크) LastPodStatus: {}", bmsOut.getLastPodStatus());
		if(!"BMS04".equals(bmsOut.getLastPodStatus())) {
			result.setResultCode("ABMS100039"); // #1#건 진행상태 접수만 가능합니다.
			return result;
		}
			
		Map<String, Object> returnMap = new HashMap<String,Object>();
		returnMap.put("blNo", bmsOut.getBlNo());
		returnMap.put("mblNo", bmsOut.getMblNo());
		returnMap.put("cargoReferenceNo", bmsOut.getCargoReferenceNo());
		returnMap.put("ct", bmsOut.getOutCt());
		returnMap.put("wt", bmsOut.getOutWt());
			
		result.setReturnMap(returnMap);
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
		
	// 접수통보후반출수정 수량,중량수정
	public BmsOutSaveResult modCtWt(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();

		log.info("modCtWt (접수통보후반출수정 : 수량,중량수정) ct: {}", search.get("ct"));
		if (!Util.isInteger(search.get("ct"))) {
			log.info("modCtWt (접수통보후반출수정 : 수량,중량수정) isInteger");
			result.setResultCode("A11449"); // #1# 의 형식이 올바르지 않습니다. 확인해주세요.
			return result;
		}
			
		log.info("modCtWt (접수통보후반출수정 : 수량,중량수정) 반입삭제 wt: {}", search.get("wt"));
		if (!Util.isDecimal(search.get("wt"))) {
			log.info("modCtWt (접수통보후반출수정 : 수량,중량수정) isDecimal");
			result.setResultCode("A11449"); // #1# 의 형식이 올바르지 않습니다. 확인해주세요.
			return result;
		}
			
		BmsOut bmsOut = bmsOutMapper.selectBmsOutByOutNoOrOdJobNo(search);
		log.info("modCtWt (접수통보후반출수정 : 수량,중량수정) bmsOut: {}", bmsOut);
		if(bmsOut == null) {
			result.setResultCode("A11481"); //해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
		search.put("inNo", Util.null2str(bmsOut.getInNo()));
		Map<String, Object> inInfo = bmsOutMapper.getInCtWtBmsOut(search);	// 반입 수량,중량
		
		// 변경전 반입쪽 수량,중량 반출쪽 수량,중량 비교
		Map<String, Object> exOutInfo = bmsOutMapper.getSumOutCtWtGroupBmsOutWithout(search);	
		if(exOutInfo == null) {	// 해당반출건만 있을경우
			int ct = Util.null2int(inInfo.get("CT"));	// 반입 수량, 중량
			BigDecimal wt = Util.obj2BigDecimalOrZero(inInfo.get("WT"));
			
			int outCt = Util.null2int(search.get("ct"));	// 변경할 반출 수량, 중량
			BigDecimal outWt = Util.obj2BigDecimalOrZero(search.get("wt"));
			
			if(ct < outCt || wt.compareTo(outWt) < 0) {	// 반입쪽 수량,중량보다 반출이 더 많을 순 없음
				result.setResultCode("ABMS100068");
				return result;
			}
		}else {	// 다른 반출건이 있을경우
			int ct = Util.null2int(inInfo.get("CT"));	// 반입 수량, 중량
			BigDecimal wt = Util.obj2BigDecimalOrZero(inInfo.get("WT"));
			
			int outCt = Util.null2int(search.get("ct")) + Util.null2int(exOutInfo.get("S_OUT_CT"));	// 변경할 반출 수량, 중량 + 다른 반출수량
			BigDecimal outWt = Util.obj2BigDecimalOrZero(search.get("wt")).add(Util.obj2BigDecimalOrZero(exOutInfo.get("S_OUT_WT")));
			
			if(ct < outCt || wt.compareTo(outWt) < 0) {	// 반입쪽 수량,중량보다 반출이 더 많을 순 없음
				result.setResultCode("ABMS100068");
				return result;
			}
		}
		
		bmsOutMapper.updateBmsOutCtWt(search);
		Map<String, Object> sumOutInfo = bmsOutMapper.getSumOutCtWtGroupBmsOut(search);
		if(sumOutInfo == null) {
			log.info("modCtWt (접수통보후반출수정 : 수량,중량수정) sumOutInfo: null");
			int ct = Util.null2int(inInfo.get("CT"));
			BigDecimal wt = Util.obj2BigDecimalOrZero(inInfo.get("WT"));
			
			log.info("sumOutInfo else문 ct: {}", ct);
			log.info("sumOutInfo else문 wt: {}", wt);
			
			Map<String, Object> param = new HashMap<String,Object>();
			param.put("inNo", search.get("inNo"));
			param.put("outNo", search.get("outNo"));
			param.put("calcCt", ct);
			param.put("calcWt", wt);
			param.put("sOutYn", "N");
			param.put("updateUserId", Util.null2str(search.get("updateUserId")));
			bmsOutMapper.updateStockAndCtWtForChange(param);
		}else {
			log.info("modCtWt (접수통보후반출수정 : 수량,중량수정) sumOutInfo: {}", sumOutInfo);
			int ct = Util.null2int(inInfo.get("CT"));
			BigDecimal wt = Util.obj2BigDecimalOrZero(inInfo.get("WT"));
				
			int sumOutCt = Util.null2int(sumOutInfo.get("S_OUT_CT"));
			BigDecimal sumOutWt = Util.obj2BigDecimalOrZero(sumOutInfo.get("S_OUT_WT"));
				
			log.info("sumOutInfo else문 ct: {}", ct);
			log.info("sumOutInfo else문 wt: {}", wt);
			log.info("sumOutInfo else문 sumOutCt: {}", sumOutCt);
			log.info("sumOutInfo else문 sumOutWt: {}", sumOutWt);
				
			int calcCt = ct - sumOutCt;
			BigDecimal calcWt = wt.subtract(sumOutWt);
				
			Map<String, Object> param = new HashMap<String,Object>();
			param.put("inNo", search.get("inNo"));
			param.put("outNo", search.get("outNo"));
			param.put("calcCt", calcCt);
			param.put("calcWt", calcWt);
			param.put("updateUserId", Util.null2str(search.get("updateUserId")));
			bmsOutMapper.updateStockAndCtWtForChange(param);
		}
			
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
		processLogs.setDescription(BmsConsts.DbType.BMS_OUT+" [접수통보후반출수정 수량,중량 업데이트]");
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobNo(bmsOut.getOdJobNo());  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		//commonService.insertLog(processLogs, request);
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
				, Util.null2str(search.get("podRemark"))
				, "반출접수후 수량,중량수정"
		);
			
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
		
	// 접수통보후반출수정 반출삭제
	public BmsOutSaveResult delOut(Map<String, Object> search, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();
			
		log.info("delOut (접수통보후반출수정 : 반출삭제) outNo: {}", search.get("outNo"));
		if(Util.null2int(search.get("outNo")) == 0) {
			result.setResultCode("A11481"); // 해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
			
		BmsOut bmsOut = bmsOutMapper.selectBmsOutByOutNoOrOdJobNo(search);
		log.info("delOut (접수통보후반출수정 : 반출삭제) bmsOut: {}", bmsOut);
		if(bmsOut == null) {
			result.setResultCode("A11481"); // 해당 #1# 데이터는 존재하지 않습니다.
			return result;
		}
		log.info("delOut (접수통보후반출수정 : 반출삭제) sumOutInfo: null");
		if(bmsOut.getOutPlanNo() > 0) {
			Map<String, Object> outPlanParam = new HashMap<String,Object>();
			outPlanParam.put("outPlanNo", bmsOut.getOutPlanNo());
			outPlanParam.put("outYn", "C");
			outPlanParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
			bmsOutMapper.updateOutYnFromOutPlan(outPlanParam);
		}
			
		Map<String, Object> outChangeDelParm = new HashMap<String,Object>();
		outChangeDelParm.put("outNo", bmsOut.getOutNo());
		outChangeDelParm.put("updateUserId", Util.null2str(search.get("updateUserId")));
		Map<String, Object> bmsOutChangeDel = bmsOutMapper.getBmsOutChangeDelByOutNo(outChangeDelParm);
		if(bmsOutChangeDel == null) {
			bmsOutMapper.insertBmsOutChangeDelByOutNo(outChangeDelParm);
		}
			
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
		processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
		processLogs.setDescription(BmsConsts.DbType.BMS_OUT+" [접수통보후반출수정 반출삭제 업데이트]");
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobNo(bmsOut.getOdJobNo());  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		//commonService.insertLog(processLogs, request);
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
				, Util.null2str(search.get("podRemark"))
				, "반출접수후 반출삭제"
				);
		
		bmsOutMapper.updateBmsOutScanResult(bmsOut);	// 출고스캔데이터 관련 업데이트
		
		bmsOutMapper.deleteBmsOut(bmsOut);	// 출고 삭제
		
		search.put("inNo", Util.null2str(bmsOut.getInNo()));
		Map<String, Object> sumOutInfo = bmsOutMapper.getSumOutCtWtGroupBmsOut(search);
		Map<String, Object> inInfo = bmsOutMapper.getInCtWtBmsOut(search);
		if(sumOutInfo == null) {
			log.info("modCtWt (접수통보후반출수정 : 수량,중량수정) sumOutInfo: null");
			int ct = Util.null2int(inInfo.get("CT"));
			BigDecimal wt = Util.obj2BigDecimalOrZero(inInfo.get("WT"));
			
			log.info("sumOutInfo else문 ct: {}", ct);
			log.info("sumOutInfo else문 wt: {}", wt);
			
			Map<String, Object> param = new HashMap<String,Object>();
			param.put("inNo", search.get("inNo"));
			param.put("outNo", search.get("outNo"));
			param.put("calcCt", ct);
			param.put("calcWt", wt);
			param.put("sOutYn", "N");
			param.put("updateUserId", Util.null2str(search.get("updateUserId")));
			bmsOutMapper.updateStockAndCtWtForChange(param);
		}else {
			log.info("modCtWt (접수통보후반출수정 : 수량,중량수정) sumOutInfo: {}", sumOutInfo);
			int ct = Util.null2int(inInfo.get("CT"));
			BigDecimal wt = Util.obj2BigDecimalOrZero(inInfo.get("WT"));
				
			int sumOutCt = Util.null2int(sumOutInfo.get("S_OUT_CT"));
			BigDecimal sumOutWt = Util.obj2BigDecimalOrZero(sumOutInfo.get("S_OUT_WT"));
				
			log.info("sumOutInfo else문 ct: {}", ct);
			log.info("sumOutInfo else문 wt: {}", wt);
			log.info("sumOutInfo else문 sumOutCt: {}", sumOutCt);
			log.info("sumOutInfo else문 sumOutWt: {}", sumOutWt);
				
			int calcCt = ct - sumOutCt;
			BigDecimal calcWt = wt.subtract(sumOutWt);
			
			Map<String, Object> param = new HashMap<String,Object>();
			param.put("inNo", search.get("inNo"));
			param.put("outNo", search.get("outNo"));
			param.put("calcCt", calcCt);
			param.put("calcWt", calcWt);
			param.put("updateUserId", Util.null2str(search.get("updateUserId")));
			bmsOutMapper.updateStockAndCtWtForChange(param);
		}
		
		if("PANTK".equals( Util.null2str(search.get("entCode")) ) && "61".equals(bmsOut.getOutClass()) ){
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cargoReferenceNo", bmsOut.getCargoReferenceNo());
			BmsOutPlan outPlan = bmsOutMapper.selectOutPlanNotExistInNo(param);
			if(outPlan != null) {
				List<InventoryIn> bmsInList = bmsOutMapper.selectBmsInListByCargoNo(param);
				List<InventoryIn> bmsInExistStockList = bmsInList.stream().filter(bmsIn -> bmsIn.getStockCt() > 0).collect(Collectors.toList());
				InventoryIn bmsIn = bmsInExistStockList.get(0);
				
				outPlan.setInNo(bmsIn.getInNo());
				bmsOutMapper.updateInNoOfOutPlan(outPlan);
			}
		}
		
		if( ( "UPSXK".equals( Util.null2str(search.get("entCode")) ) || "YHTXX".equals( Util.null2str(search.get("entCode")) ) ) && "61".equals(bmsOut.getOutClass()) ){
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cargoReferenceNo", bmsOut.getCargoReferenceNo());
			Map<String, Object>  bmsInExistStock = bmsOutMapper.selectBmsInExistStock(param);
			if(bmsInExistStock != null) {
				Map<String, Object> bmsInMinusStock = bmsOutMapper.selectBmsInMinusStock(param);
				BmsOutPlan outPlan31 = bmsOutMapper.selectOutPlan31(param);
				if(outPlan31 != null) {
					outPlan31.setInNo(Util.null2int(bmsInExistStock.get("IN_NO")));
					outPlan31.setUpdateUserId(Util.null2str(search.get("updateUserId")));
					bmsOutMapper.updateInNoOfOutPlan(outPlan31);
				}
				if(bmsInMinusStock != null) {
					bmsInMinusStock.put("inNo", bmsInMinusStock.get("IN_NO"));
					bmsInMinusStock.put("inNoOfExistStock", bmsInExistStock.get("IN_NO"));
					bmsInMinusStock.put("updateUserId", Util.null2str(search.get("updateUserId")));
					bmsOutMapper.updateInNoOfBmsOut(bmsInMinusStock);
					
					BmsOut minusStockParam = new BmsOut();
					minusStockParam.setInNo(Util.null2int(bmsInMinusStock.get("IN_NO")));
					bmsOutMapper.updateInCtWtCbm(minusStockParam);
					
					BmsOut existStockParam = new BmsOut();
					existStockParam.setInNo(Util.null2int(bmsInExistStock.get("IN_NO")));
					bmsOutMapper.updateInCtWtCbm(existStockParam);
				}
			}
		}
			
		result.setResultCode("A00165"); // 저장 되었습니다.
		return result;
	}
	
	/**
	 * 반입정보매핑 update
	 * @param bmsOut
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult updateInMapping(Map<String, Object> listData, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) listData.get("dataList");		
	    Map<String,Object> map = new HashMap<String,Object>();
			
		for(Map<String, Object> bmsOutList : dataList) {
			String cargoRefNo = Util.null2str(bmsOutList.get("cargoReferenceNo"));
			Map<String, Object> inMap = bmsOutMapper.selectCargoByinNo(cargoRefNo);  
			
			// 반입자료가 있다면
			if(inMap != null) {				
				String inClass = Util.null2str(inMap.get("IN_CLASS"));
				String inClassName = Util.null2str(inMap.get("IN_CLASS_NAME"));
				
				// 반입구분이 10.입항반입, 11.무적화물반입, 23.일괄구매물품B/L분할반입인 경우만 실행
				if("10".equals(inClass) || "11".equals(inClass) || "23".equals(inClass)) {
					Integer outPlanNo = (Integer) bmsOutList.get("outPlanNo");	
					
			        if(outPlanNo != null) {
			        	map.put("outPlanNo", outPlanNo);
			        	map.put("inNo", Util.null2str(inMap.get("IN_NO")));
			        	map.put("blNo", Util.null2str(inMap.get("BL_NO")));
			        	map.put("mblNo", Util.null2str(inMap.get("MBL_NO")));
			        	
			        	int chkCnt = bmsOutMapper.selectCargoByOutPlanNo(cargoRefNo);		
			        	
			        	// 반출승인이 31, IN_NO가 0, BL_NO가 빈 값인 경우 실행 => 매핑된 정보가 없는 경우
			        	if(chkCnt > 0) {
			        		
			        		bmsOutMapper.updateNo(map);  // 반입번호,BL,MBL 업데이트
			        		
			        		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
			        		ProcessLogs processLogs = new ProcessLogs();
			        		processLogs.setEntCode(Util.null2str(listData.get("entCode")));
			        		processLogs.setEntBusinessCode(Util.null2str(listData.get("entBusinessCode")));
			        		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			        		processLogs.setProcessUserId(Util.null2str(listData.get("registerUserId")));
			        		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			        		processLogs.setLogJobType("DATA");
			        		processLogs.setBound("AIR");
			        		processLogs.setBoundType("IMP");
			        		processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
			        		
			        		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
			        		processLogs.setDescription(BmsConsts.DbType.BMS_OUT+" [입력 NO:"+String.valueOf(listData.get("inNo"))+"]");
			        		processLogs.setJobNo(String.valueOf(inMap.get("OD_JOB_NO")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
			        		processLogs.setJobLog(new Gson().toJson(bmsOutList));
			        		processLogs.setResult("ok");
			        		processLogs.setEntBusinessPrintName(Util.null2str(listData.get("logUserBusinessPrintName")));
			        		processLogs.setProcessUserName(Util.null2str(listData.get("logUserName")));
			        		processLogs.setProcessUserNameEng(Util.null2str(listData.get("logUserNameEng")));
			        		
			        		// 호출시 필요 항목 : BL_NO, POD_STATUS
			        		bmsCommService.insertLogAndPodStatus(processLogs, request
			        				, "CUSTOM_POD_NAME" /* pod Status 값 매핑 */
			        				, ""
			        				, "반입정보매핑"
			        				);	 
			        	}else {
							bmsOutSaveResult.setResultCode("A11462"); 
					        return bmsOutSaveResult;
			        	}
			        }
			        
			        bmsOutSaveResult.setResultCode("A012051");	// 저장되었습니다.
			        
				}else {
					bmsOutSaveResult.setResultCode("ABMS100068"); // #1#
					bmsOutSaveResult.setErrorMsg(inClassName);
			        return bmsOutSaveResult;
				}
				
			}else {		
				bmsOutSaveResult.setResultCode("A11397"); // 조회된 내역이 없습니다.
		        return bmsOutSaveResult;
			}
			
		}

		return bmsOutSaveResult;
	}

}
