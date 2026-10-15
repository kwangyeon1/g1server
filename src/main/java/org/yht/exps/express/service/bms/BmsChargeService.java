package org.yht.exps.express.service.bms;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsChargeMapper;
import org.yht.exps.express.mapper.bms.BmsOutMapper;
import org.yht.exps.express.mapper.invoice.InvoiceMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.charge.*;
import org.yht.exps.express.model.bms.charge.search.SearchBmsRate;
import org.yht.exps.express.model.bms.charge.search.SearchTcrNo;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.disposal.InventoryInList;
import org.yht.exps.express.model.bms.out.*;
import org.yht.exps.express.model.bms.out.search.SearchBmsOut;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.Filters;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class BmsChargeService {

	@Autowired
	InvoiceMapper invoiceMapper;
	
	@Autowired
	CommonMapper commonMapper;

	//@Autowired
	@Autowired
	BmsOutMapper bmsOutMapper;

	@Autowired
	BmsChargeMapper bmsChargeMapper;

	@Autowired
	CommonService commonService;


	@Autowired
	BmsCommService bmsCommService ;
	/**
	 * 재고 조회 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public InventoryInList selectEstiInventoryInList(SearchBmsOut search) throws Exception {
		InventoryInList inventoryInList = new InventoryInList();
		setSearchCondition(search);

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

		List<InventoryIn> list  ;
		// excel Down 여부
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYOUT_DATE|CARRYIN_DATE|APPROVAL_DATE|ESTIMATE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|X.CUSTOMS_REPORT_NO|A.MBL_NO");
		if(null == search.getExcelDownYn()){
			list  = bmsChargeMapper.selectEstiBmsInventoryInList(search);
		}else {
			list  = bmsChargeMapper.selectEstiBmsInventoryInListExcel(search);
		}
		inventoryInList.setInventoryInList(list);
		if(list.size() > 0) {
			inventoryInList.setTotal(list.get(0).getTotal());
		}else {
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
	public BmsOutList selectEstiBmsOutList(SearchBmsOut search) throws Exception {
		BmsOutList bmsOutList = new BmsOutList();
		setSearchCondition(search);

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
		List<BmsOut> list ;
		// excel Down 여부
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|A.CARRYOUT_DATE|B.CARRYIN_DATE|A.APPROVAL_DATE|ESTIMATE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO|A.CUSTOMS_REPORT_NO|A.MBL_NO");
		if(null == search.getExcelDownYn()){
			list  = bmsChargeMapper.selectEstiBmsOutList(search);
		}else {
			list  = bmsChargeMapper.selectEstiBmsOutListExcel(search);
		}
		bmsOutList.setBmsOutList(list);
		if(list.size() > 0) {
			bmsOutList.setTotal(list.get(0).getTotal());
		}else {
			bmsOutList.setTotal(0);
		}
		return bmsOutList;
	}
	/**
	 * 반출승인/승인취하 조회 List
	 * srchDateGbn에 A.붙이기
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsOutPlanList selectEstiBmsOutPlanList(SearchBmsOut search) throws Exception {
		BmsOutPlanList bmsOutPlanList = new BmsOutPlanList();
		setSearchCondition(search);

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
		List<BmsOutPlan> list ;
		// excel Down 여부
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYOUT_DATE|CARRYIN_DATE|APPROVAL_DATE|ESTIMATE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|X.CUSTOMS_REPORT_NO|B.CUSTOMS_REPORT_NO|A.MBL_NO");
		if(null == search.getExcelDownYn()){
			list  = bmsChargeMapper.selectEstiBmsOutPlanList(search);
		}else {
			list  = bmsChargeMapper.selectEstiBmsOutPlanListExcel(search);
		}
		bmsOutPlanList.setBmsOutPlanList(list);
		if(list.size() > 0) {
			bmsOutPlanList.setTotal(list.get(0).getTotal());
		}else {
			bmsOutPlanList.setTotal(0);
		}
		return bmsOutPlanList;
	}
	public BmsEstimateList selectEstiBmsEstimateList(SearchBmsOut search) throws Exception {
		BmsEstimateList estimateList = new BmsEstimateList();
		setSearchCondition(search);

	/*	if( null != search.getSrchDateGbn() ){
			if(!"noDateChk".equals(search.getSrchDateGbn()) && !"".equals(search.getSrchDateGbn())){
				search.setSrchDateGbn("A."+search.getSrchDateGbn());
			}
		}*/

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
		List<BmsEstimate> list  ;
		// excel Down 여부
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|A.CARRYOUT_DATE|A.CARRYIN_DATE|A.APPROVAL_DATE|A.ESTIMATE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|E.CUSTOMS_REPORT_NO|B.CUSTOMS_REPORT_NO|A.MBL_NO");
		if(null == search.getExcelDownYn()){
			list  = bmsChargeMapper.selectEstiBmsEstimateList(search);
		}else {
			list  = bmsChargeMapper.selectEstiBmsEstimateListExcel(search);
		}
		estimateList.setBmsEstimatesList(list);
		if(list.size() > 0) {
			estimateList.setTotal(list.get(0).getTotal());
		}else {
			estimateList.setTotal(0);
		}
		return estimateList;
	}
	
	public BmsEstimateGroupingList getInNoAndApplyClassFromEstimate(SearchBmsOut search) throws Exception {
		BmsEstimateGroupingList bmsEstimateGroupingList = new BmsEstimateGroupingList();

		/*List<String> inNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			inNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split("\\|")));
		}*/
		
		Map<String, Object> param = new HashMap<String, Object>();
		/* param.put("inNoList", inNoList); */
		
		param.put("srchEntCode", search.getSrchEntCode());
		param.put("userLanguage", search.getUserLanguage());
		param.put("groupCode", "WAREHOUSE_RATES");
		
		param.put("startEstimateDate", search.getSrchStartDate());
		param.put("endEstimateDate", search.getSrchEndDate());

		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		List<BmsEstimate> list = bmsChargeMapper.selectEstimateListByInNoList(param);
		Map<String, List<BmsEstimate>> groupedByApplyClass = list.stream()
			    .collect(Collectors.groupingBy(BmsEstimate::getApplyClass));
		
		bmsEstimateGroupingList.setGroupedByApplyClass(groupedByApplyClass);
		return bmsEstimateGroupingList;
	}
	
	public Map getBmsOutListByMrnMsnOfCargoNo(Map<String, Object> param) throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		List<Map<String, String>> list = bmsChargeMapper.selectBmsOutListByMrnMsnOfCargoNo(param);
		result.put("data", list);
		result.put("total", list.size());
		return result;
	}
	
	public TcrNoSettingResult getTcrNo(SearchTcrNo search) throws Exception {
		TcrNoSettingResult tcrNoResult = new TcrNoSettingResult();
		
		String tcrNo = bmsChargeMapper.selectTcrNoFromEntBusOtion(search);
		
		tcrNoResult.setTcrNo(tcrNo);
		return tcrNoResult;
	}
	
	public TcrNoSettingResult updateTcrNo(SearchTcrNo search, HttpServletRequest request) throws Exception {
		TcrNoSettingResult tcrNoResult = new TcrNoSettingResult();
		
		bmsChargeMapper.updateTcrNoFromEntBusOtion(search);
		
		tcrNoResult.setResultCode("A012051"); // 저장 되었습니다.
		return tcrNoResult;
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

	private static void setSearchConditionRate(SearchBmsRate search) throws Exception {
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
	 * 상세 화면 조회 리스트 조회
	 * dupEstimateCnt: 파라미터에 각 스텝별로 0보다 크면
	 * 해당자료가 있는 경우 ABMS000006	저장할 화물관리번호가 이미 존재합니다. 추가등록하시겠습니까?
	 * @param search
	 * @return
	 */
	public EstimateDetail selectEstiDetail(SearchBmsOut search){
		EstimateDetail detail = new EstimateDetail();
		BmsEstimate bmsEsti =null;
		BmsOut bmsOut = null;

		BmsOutPlan bmsOutPlan = null;
		InventoryIn inventoryIn = null;
		List<BmsDelivery> doLst = new ArrayList<>();
		int dupEstimateCnt = 0;
		String customerCode = ""; // 계산서 표시용
		String customerName = ""; // 거래처 명
		BmsCustomerInfo info ;
		if(BmsConsts.BMS_CONST_ESTIMATE.equals(search.getSrchCarryInState())) {  //OUT, PLAN, STOCK
			bmsEsti = bmsChargeMapper.selectBmsEstimateDetail(search);
            if( bmsEsti != null){
				search.setSrchOutNo(bmsEsti.getOutNo());
				search.setSrchOutPlanNo(bmsEsti.getOutPlanNo());
				search.setSrchInNo(bmsEsti.getInNo());
				search.setSrchBlNo(bmsEsti.getBlNo());
				// 고객 정보 가져 오기용
				customerCode = bmsEsti.getCustomerCode();
				customerName = bmsEsti.getCustomerName();
			}
			bmsOut   = bmsOutMapper.selectBmsOutDetail(search);

			//
			inventoryIn =  bmsOutMapper.selectInventoryDetail(search);
			bmsOutPlan = bmsOutMapper.selectBmsOutPlanDetail(search);

			doLst = bmsOutMapper.selectDoList(search);

            // 정산서 번호 매핑 및 조회
			info = this.setOutCustomerInfo(search, customerCode, customerName);
			if( null != bmsEsti) {
				BmsOut bmsOutCus = new BmsOut();
				// 정산 번호 호출
				bmsOutCus.setBlNo(bmsEsti.getBlNo());
			//	bmsOutCus.setOdJobNo(bmsEsti.getOdJobNo());
				bmsOutCus.setOdJobNo(String.valueOf(bmsEsti.getComOdJobNo()));
				String invoiceNo = bmsChargeMapper.selectBmsInvoiceNo(bmsOutCus);
				info.setInvoiceNo(invoiceNo);
			}
            // 테스트 데이터 입력

		/*	info.setCustomerCode("1");
			info.setCustomerName("2");
			info.setInvoiceNo("3");
			info.setAttn("damdang");
			info.setTel("tel");
			info.setEmail("email");
			info.setBusinessNo("busiNo");*/

			//dupEstimateCnt = 0; 조회할 필요가 없다
		}else if(BmsConsts.BMS_CONST_OUT.equals(search.getSrchCarryInState())) {  //OUT, PLAN, STOCK
			//반출
			bmsOut   = bmsOutMapper.selectBmsOutDetail(search);
            //창고료
			bmsEsti = bmsChargeMapper.selectBmsEstimateDetail(search);

			search.setSrchInNo(bmsOut.getInNo()); // 상세 정보 조회
			inventoryIn =  bmsOutMapper.selectInventoryDetail(search);
			if(null != bmsOut) {
				search.setSrchOutPlanNo(bmsOut.getOutPlanNo());
				bmsOutPlan = bmsOutMapper.selectBmsOutPlanDetail(search);
				
				customerCode = bmsOut.getCustomerCode(); // 계산서 표시용
				customerName = bmsOut.getCustomerName(); // 거래처 명

			}
			if(null != bmsEsti) {	// 반출>창고료정산 엔트리에서 저장시 화주정보 안보이는것 수정 2025.11.03 2sowell
				customerCode = bmsEsti.getCustomerCode(); // 계산서 표시용
				customerName = bmsEsti.getCustomerName(); // 거래처 명
			}
			search.setSrchBlNo(bmsOut.getBlNo());
			doLst = bmsOutMapper.selectDoList(search);
			// dupEstimateCnt estimate 등록 여부 체크 처리
			BmsOut dupBmsOut = new BmsOut();
			dupBmsOut.setCargoReferenceNo(bmsOut.getCargoReferenceNo()); // 화물관리 번호
			dupBmsOut.setOutApprovalNo(bmsOut.getOutApprovalNo()); //  반출 근거 번호
			// 두번 입력 체크
			dupEstimateCnt = bmsChargeMapper.selectEstimateCargoAndOutApprovalCount(dupBmsOut);

			// OUT 일때 고객 정보 매핑
			info = this.setOutCustomerInfo(search, customerCode, customerName);
		}else if(BmsConsts.BMS_CONST_OUT_PLAN.equals(search.getSrchCarryInState())) {  //OUT, PLAN, STOCK  // OUT_NO로 조회 해아 한다.
			bmsOutPlan   = bmsOutMapper.selectBmsOutPlanDetail(search);
			//창고료
			bmsEsti = bmsChargeMapper.selectBmsEstimateDetail(search);

			bmsOut   = bmsOutMapper.selectBmsOutDetail(search);

			search.setSrchInNo(bmsOutPlan.getInNo()); // 상세 정보 조회
			inventoryIn =  bmsOutMapper.selectInventoryDetail(search);
			//
			search.setSrchBlNo(bmsOutPlan.getBlNo());
			doLst = bmsOutMapper.selectDoList(search);
			// dupEstimateCnt estimate 등록 여부 체크 처리
			BmsOut dupBmsOut = new BmsOut();
			dupBmsOut.setCargoReferenceNo(bmsOutPlan.getCargoReferenceNo()); // 화물관리 번호
			dupBmsOut.setOutApprovalNo(bmsOutPlan.getOutApprovalNo()); //  반출 근거 번호
			dupEstimateCnt = bmsChargeMapper.selectEstimateCargoAndOutApprovalCount(dupBmsOut);


			BmsOut bmsOutCus = new BmsOut();
			// 정산서 조회용
			bmsOutCus.setEntCode(search.getSrchEntCode());
			if(bmsEsti != null) {
				bmsOutCus.setCustomerCode(bmsEsti.getCustomerCode());
				// bmsOutCus.setCompanyBusinessNo(bmsOutPlan.getCompanyBusinessNo()); // 반출승인통보는 사업자 번호로 계산서 번호 조회
			}else {
				bmsOutCus.setCustomerCode("");
				bmsOutCus.setCompanyBusinessNo(bmsOutPlan.getCompanyBusinessNo()); // 반출승인통보는 사업자 번호로 계산서 번호 조회
			}
			
			// 계산서 정보를 위한 화주 정보 리턴
			info =  bmsChargeMapper.selectBmsCustomerInfo(bmsOutCus);
			if ( null == info){
				customerName = bmsOutPlan.getCustomerName();
				info = new BmsCustomerInfo();
				info.setCustomerCode("");
				info.setCustomerName(customerName);
			}

		}else { // STOCK
			inventoryIn =  bmsOutMapper.selectInventoryDetail(search);
			//창고료
			bmsEsti = bmsChargeMapper.selectBmsEstimateDetail(search);
			//qkscnf
			bmsOut      = bmsOutMapper.selectBmsOutDetail(search);
			if(null != bmsOut) {
				search.setSrchOutPlanNo(bmsOut.getOutPlanNo());
				bmsOutPlan = bmsOutMapper.selectBmsOutPlanDetail(search);
			}
			search.setSrchBlNo(inventoryIn.getBlNo());
			doLst = bmsOutMapper.selectDoList(search);


			// dupEstimateCnt estimate 등록 여부 체크 처리
			BmsOut dupBmsOut = new BmsOut();
			dupBmsOut.setCargoReferenceNo(inventoryIn.getCargoReferenceNo()); // 화물관리 번호
			//dupBmsOut.setOutApprovalNo(bmsOut.getOutApprovalNo()); //  반출 근거 번호 재고는 조회조건을 삭제
			dupEstimateCnt = bmsChargeMapper.selectEstimateCargoAndOutApprovalCount(dupBmsOut);

			// 계산서 정보 조회
			customerCode = inventoryIn.getCustomerCode();
			customerName = inventoryIn.getCustomerName();
			info = this.setOutCustomerInfo(search, customerCode, customerName);

		/*	info.setCustomerCode("1");
			info.setCustomerName("2");
			info.setInvoiceNo("3");
			info.setAttn("damdang");
			info.setTel("tel");
			info.setEmail("email");
			info.setBusinessNo("busiNo");*/
		}


		    detail.setDupEstimateCnt(dupEstimateCnt); //ESTIMATE 그리드가 아닌 다른곳에서 온 경우 중복 체크 메시지 표시용
 	        detail.setBmsDelivery(doLst);
	 		detail.setBmsOut(bmsOut);
			detail.setBmsOutPlan(bmsOutPlan);
			detail.setInventoryIn(inventoryIn);
			detail.setBmsEstimate(bmsEsti);
 			detail.setCustomerInfo(info);

			detail.setSrchCarryInState(search.getSrchCarryInState());
			detail.setSrchOutNo(search.getSrchOutNo());
			detail.setSrchInNo(search.getSrchInNo());
			return detail;
	}

	private BmsCustomerInfo setOutCustomerInfo(SearchBmsOut search, String customerCode, String customerName) {
		BmsCustomerInfo info;
		BmsOut bmsOutCus = new BmsOut();
		// 정산서 조회용
		bmsOutCus.setEntCode(search.getSrchEntCode());
		bmsOutCus.setCustomerCode(customerCode);
		bmsOutCus.setCompanyBusinessNo("");
		// 계산서 정보를 위한 화주 정보 리턴
		info =  bmsChargeMapper.selectBmsCustomerInfo(bmsOutCus);

		if ( null == info){
			info = new BmsCustomerInfo();
			info.setCustomerCode(customerCode);
			info.setCustomerName(customerName);
		}
		if(StringUtils.isNotBlank(customerName)){
			info.setCustomerName(customerName);
		}

		return info;
	}

	/***
	 * srchBlNo로 조회시에 보이는 값 리스트
	 * @param search
	 * @return
	 */
	public EstimateDetailList selectEstiBmsOutByBlNo(SearchBmsOut search) throws Exception {
		EstimateDetailList lst = new EstimateDetailList();

		search.setPageSize(50);
		search.setPage(1);
		search.setDefaultSort(" IN_NO DESC ");

		setSearchCondition(search);
		// CUSTOMS_REPORT_NO  에 대해서 각 단계 별로 Alias 처리 요청
		String originSrchNoType = search.getSrchNoType();

		getSearchCondiEstimateOut(search, BmsConsts.BMS_CONST_ESTIMATE , originSrchNoType);
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|A.CARRYOUT_DATE|A.CARRYIN_DATE|A.APPROVAL_DATE|A.ESTIMATE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|E.CUSTOMS_REPORT_NO|B.CUSTOMS_REPORT_NO|A.MBL_NO");
		List<BmsEstimate> bmsEstimateLst  = bmsChargeMapper.selectEstiBmsEstimateList(search);
		if(bmsEstimateLst.size() > 0) {
			lst.setTotalEstimate(bmsEstimateLst.get(0).getTotal());
		}else {
			lst.setTotalEstimate(0);
		}
		lst.setEstimateList(bmsEstimateLst);

		getSearchCondiEstimateOut(search, BmsConsts.BMS_CONST_OUT , originSrchNoType);
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|A.CARRYOUT_DATE|B.CARRYIN_DATE|A.APPROVAL_DATE|ESTIMATE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO|A.CUSTOMS_REPORT_NO|A.MBL_NO");
		List<BmsOut> bmsOutLst  = bmsChargeMapper.selectEstiBmsOutList(search);
		if(bmsOutLst.size() > 0) {
			lst.setTotal(bmsOutLst.get(0).getTotal());
		}else {
			lst.setTotal(0);
		}
		lst.setBmsOutList(bmsOutLst);
		//inventory TB_BMS_IN.A.CUSTOMS_REPORT_NO
		getSearchCondiEstimateOut(search, BmsConsts.BMS_CONST_STOCK, originSrchNoType);
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYOUT_DATE|CARRYIN_DATE|APPROVAL_DATE|ESTIMATE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|X.CUSTOMS_REPORT_NO|A.MBL_NO");
		List<InventoryIn> invenLst = bmsChargeMapper.selectEstiBmsInventoryInList(search);
		if(invenLst.size() > 0) {
			lst.setTotalIn(invenLst.get(0).getTotal());
		}else {
			lst.setTotalIn(0);
		}
		lst.setInventoryInList(invenLst);

		// Plan  PLAN 일경우 TB_BMS_IN.B.CUSTOMS_REPORT_NO
		getSearchCondiEstimateOut(search, BmsConsts.BMS_CONST_OUT_PLAN , originSrchNoType);
		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYOUT_DATE|CARRYIN_DATE|APPROVAL_DATE|ESTIMATE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|X.CUSTOMS_REPORT_NO|B.CUSTOMS_REPORT_NO|A.MBL_NO");
		List<BmsOutPlan> bmsOutPlanLst  = bmsChargeMapper.selectEstiBmsOutPlanList(search);
		if(bmsOutPlanLst.size() > 0) {
			lst.setTotalPlan(bmsOutPlanLst.get(0).getTotal());
		}else {
			lst.setTotalPlan(0);
		}
		lst.setBmsOutPlanList(bmsOutPlanLst);
		return lst;
	}
	
	/**
	 * 정산료 일부필드 update
	 * @param bmsEstimate
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult updateField(BmsEstimate bmsEstimate, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		String podRemarkStr = "";
		
		SearchBmsOut detailParam = new SearchBmsOut();
		detailParam.setSrchEstimateNo(bmsEstimate.getEstimateNo());
		BmsEstimate oldEstimateInfo = bmsChargeMapper.selectBmsEstimateDetail(detailParam);
		if(!Util.null2str(bmsEstimate.getApplyClass()).equals(oldEstimateInfo.getApplyClass())) {
			podRemarkStr = "요율구분 " + oldEstimateInfo.getApplyClass() + "에서 " + bmsEstimate.getApplyClass() + "으로 변경";
			bmsEstimate.setApplyClassChangedYn("Y");
		}
		if(!Util.null2str(bmsEstimate.getRemarks()).equals(oldEstimateInfo.getRemarks())) {
			podRemarkStr += "".equals(podRemarkStr) ? " 비고변경" : ", 비고변경";
		}
		
		int intRow = bmsChargeMapper.updateBmsEstimateField(bmsEstimate );

		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(bmsEstimate.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(bmsEstimate.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(bmsEstimate.getRegisterUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_ESTIMATE);
		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
		processLogs.setDescription(BmsConsts.DbType.BMS_ESTIMATE+" [" + podRemarkStr + " 수정 NO:"+String.valueOf(bmsEstimate.getEstimateNo())+"]");
		processLogs.setJobNo(String.valueOf(bmsEstimate.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(bmsEstimate));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(bmsEstimate.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(bmsEstimate.getLogUserName());
		processLogs.setProcessUserNameEng(bmsEstimate.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(bmsEstimate.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
				, podRemarkStr
				, bmsEstimate
		);

		// WT,CT 데이터 업데이트
		bmsOutSaveResult.setResultCode("A012051"); // 저장 되었습니다.
		return bmsOutSaveResult;
	}

	/***
	 * CUSTOMS_REPORT_NO 에 대해서 각 쿼리 마다 다른 값이 넣머 오므로 각 단계 별로
	 * setSearchType 의 값을 넘긴다.
	 * @param search
	 * @param gubun
	 */
	private static void getSearchCondiEstimateOut(SearchBmsOut search , String gubun , String originSearchType) {
		if (BmsConsts.BMS_CONST_ESTIMATE.equals(gubun)) { //// TB_BMS_ESTIMATE , A.CUSTOMS_REPORT_NO, TB_BMS_IN.B.CUSTOMS_REPORT_NO
			if ("CUSTOMS_REPORT_NO-OUT".equals(originSearchType)) {
				search.setSrchNoType("E.CUSTOMS_REPORT_NO");
			} else if ("CUSTOMS_REPORT_NO-IN".equals(originSearchType)) {
				search.setSrchNoType("B.CUSTOMS_REPORT_NO");
			}
		}else if (BmsConsts.BMS_CONST_OUT.equals(gubun)) { //// TB_BMS_OUT A.CUSTOMS_REPORT_NO, TB_BMS_IN.B.CUSTOMS_REPORT_NO
			if ("CUSTOMS_REPORT_NO-OUT".equals(originSearchType)) {
				search.setSrchNoType("A.CUSTOMS_REPORT_NO");
			} else if ("CUSTOMS_REPORT_NO-IN".equals(originSearchType)) {
				search.setSrchNoType("B.CUSTOMS_REPORT_NO");
			}
		}else if (BmsConsts.BMS_CONST_STOCK.equals(gubun)) {// inventory TB_BMS_IN.A.CUSTOMS_REPORT_NO
			if ("CUSTOMS_REPORT_NO-OUT".equals(originSearchType)) {
				search.setSrchNoType("X.CUSTOMS_REPORT_NO");  // IN에서 OUT으로 조회
			} else if ("CUSTOMS_REPORT_NO-IN".equals(originSearchType)) {
				search.setSrchNoType("A.CUSTOMS_REPORT_NO");
			}
		}else if (BmsConsts.BMS_CONST_OUT_PLAN.equals(gubun)) {  // Plan  PLAN 일경우 TB_BMS_IN.B.CUSTOMS_REPORT_NO
			if ("CUSTOMS_REPORT_NO-OUT".equals(originSearchType)) {
				search.setSrchNoType("X.CUSTOMS_REPORT_NO");
			} else if ("CUSTOMS_REPORT_NO-IN".equals(originSearchType)) {
				search.setSrchNoType("B.CUSTOMS_REPORT_NO");
			}
		}
	}

	/**
	 * 정산료 입력 - insert/update
	 * @param bmsEstimate
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult insertBmsEstimate(BmsEstimate bmsEstimate, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		//
		//	String bmsWhCode = bmsOut.getBmsWhCode();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("entCode", bmsEstimate.getEntCode());
		map.put("inNo", bmsEstimate.getInNo());
		map.put("outPlanNo", bmsEstimate.getOutPlanNo());
		map.put("outNo", bmsEstimate.getOutNo());
		Map<String, Object> bmsInWithOutCustomerInfo = bmsChargeMapper.selectCustomerOfInOut(map);
		
		/* if("".equals(bmsEstimate.getCustomerCode())) {
			if(bmsInWithOutCustomerInfo != null) {
				if(bmsInWithOutCustomerInfo.get("OUT_CUSTOMER_NAME") != null && !"".equals(bmsInWithOutCustomerInfo.get("OUT_CUSTOMER_NAME"))) {
					bmsEstimate.setCustomerCode(bmsInWithOutCustomerInfo.get("OUT_CUSTOMER_CODE").toString());
					bmsEstimate.setCustomerName(bmsInWithOutCustomerInfo.get("OUT_CUSTOMER_NAME").toString());
				}else if(bmsInWithOutCustomerInfo.get("IN_CUSTOMER_NAME") != null && !"".equals(bmsInWithOutCustomerInfo.get("IN_CUSTOMER_NAME"))) {
					bmsEstimate.setCustomerCode(bmsInWithOutCustomerInfo.get("IN_CUSTOMER_CODE").toString());
					bmsEstimate.setCustomerName(bmsInWithOutCustomerInfo.get("IN_CUSTOMER_NAME").toString());
				}
			}
		} */
		bmsChargeMapper.insertBmsEstimate(bmsEstimate);

		if( null != bmsEstimate.getOutNo() ){  // OUT_NO 존재 하지 않으면 0이 넘어 온다.
			// 계산 완료 여부를 OUT 테이블에 입력 한다.
			if(   bmsEstimate.getOutNo() > 0) {
				BmsOut bmsOut = new BmsOut();
				bmsOut.setOutNo(bmsEstimate.getOutNo());
				bmsOut.setUpdateUserId(bmsEstimate.getUpdateUserId());
				//
				bmsChargeMapper.updateBmsOutEstimateYn_Y(bmsOut);
			}
		}

		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(bmsEstimate.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(bmsEstimate.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(bmsEstimate.getRegisterUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setDbType(BmsConsts.DbType.BMS_ESTIMATE);
		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
		processLogs.setDescription(BmsConsts.DbType.BMS_ESTIMATE+" [입력 NO:"+String.valueOf(bmsEstimate.getEstimateNo())+"]");
		processLogs.setJobNo(String.valueOf(bmsEstimate.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(bmsEstimate));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(bmsEstimate.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(bmsEstimate.getLogUserName());
		processLogs.setProcessUserNameEng(bmsEstimate.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(bmsEstimate.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.INSERT /* pod Status 값 매핑 */
				, ""
				, bmsEstimate

		);

		map.put("estimateNo", Util.null2str(bmsEstimate.getEstimateNo()));
		bmsOutSaveResult.setResultData(map);
		bmsOutSaveResult.setResultCode("A012051");// 저장 되었습니다.
		return bmsOutSaveResult;
	}

	/**
	 * 세금 계산서 계산
	 * @param bmsEstimate
	 * @param request
	 * @return
	 * @throws Exception
	 */

	public BmsOutSaveResult insertCalTax(BmsEstimate bmsEstimate, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		
		if(bmsEstimate == null) {
			bmsOutSaveResult.setResultCode("A00313"); // #1#을(를) 입력바랍니다. -- 계산서정보 
			return bmsOutSaveResult;
		}
		
		String estimateDate = 	bmsEstimate.getEstimateDate(); // 정산 번호
		String gmtTime  = bmsEstimate.getGmtTime();
		String entCode  = bmsEstimate.getEntCode();
		String entBusinessCode = bmsEstimate.getEntBusinessCode();
		String userId  = bmsEstimate.getRegisterUserId();
		
		Map<String, Object> cusInfoParam = new HashMap<String, Object>();
		cusInfoParam.put("entCode", entCode);
		cusInfoParam.put("inNo", bmsEstimate.getInNo());
		cusInfoParam.put("outPlanNo", bmsEstimate.getOutPlanNo());
		cusInfoParam.put("outNo", bmsEstimate.getOutNo());
		cusInfoParam.put("estimateNo", bmsEstimate.getEstimateNo());
		Map<String, Object> bmsInWithCustomerMap = bmsChargeMapper.selectCustomerOfInOut(cusInfoParam);
		// UPS일 경우에는 BMS_IN 데이터 없어도 세금계산서 발행 가능하도록 수정 2026.04.29 2sowell
		if( bmsInWithCustomerMap == null && !(entCode.equals("UPSXK") || entCode.equals("YHTXX"))) {
			bmsOutSaveResult.setResultCode("A01054");
			return bmsOutSaveResult;
		}
		
		/* if(bmsInWithCustomerMap.get("OUT_CUSTOMER_NAME") != null && !"".equals(bmsInWithCustomerMap.get("OUT_CUSTOMER_NAME"))) {
			bmsEstimate.setCustomerCode(bmsInWithCustomerMap.get("OUT_CUSTOMER_CODE").toString());
			bmsEstimate.setCustomerName(bmsInWithCustomerMap.get("OUT_CUSTOMER_NAME").toString());
		}else if(bmsInWithCustomerMap.get("IN_CUSTOMER_NAME") != null && !"".equals(bmsInWithCustomerMap.get("IN_CUSTOMER_NAME"))) {
			bmsEstimate.setCustomerCode(bmsInWithCustomerMap.get("IN_CUSTOMER_CODE").toString());
			bmsEstimate.setCustomerName(bmsInWithCustomerMap.get("IN_CUSTOMER_NAME").toString());
		}else { */
			// bmsOutSaveResult.setResultCode("A01054");
			// return bmsOutSaveResult;
		
		if(bmsChargeMapper.checkCustomerInfo(bmsEstimate) == 0) {
			 bmsOutSaveResult.setResultCode("A01054"); // 시스템관리 -- 등록되지않은 사업자정보
			 return bmsOutSaveResult;
		}
		
		//}
		Map<String, Object> updateParam = new HashMap<String, Object>();
		updateParam.put("entCode", entCode);
		updateParam.put("customerCode", bmsEstimate.getCustomerCode());
		updateParam.put("attn", bmsEstimate.getAttn());
		updateParam.put("tel", bmsEstimate.getTel());
		updateParam.put("email", bmsEstimate.getEmail());
		bmsChargeMapper.updateCustomerInfoForTax(updateParam);
		
		bmsChargeMapper.updateBmsEstimateCustomer(bmsEstimate);
		
		BmsEstimate savedBmsEstimateData = bmsChargeMapper.selectOneEstimate(cusInfoParam);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("entCode", entCode);
		map.put("entBusinessCode", entBusinessCode);
		map.put("gmtTime", gmtTime);
		map.put("updateUserId", userId);
		map.put("stockSeqLength", 5);
		map.put("solution", BmsConsts.BMS_CONST_SOLUTION);
		
		map.put("userId", userId);
		map.put("bound", "AIR"); // 임시값 dummy
		map.put("boundType", "IMP"); // 임시값 dummy
		
		map.put("dateFormat", "23");
		
		/** TB_COM_OBL관리번호(OBL_JOB_NO) */
		Map<String, Object> oblStockMap = new HashMap<String, Object>();
		oblStockMap.putAll(map);
		oblStockMap.put("stockType", "OBL");
		oblStockMap.put("stockSign", "CBL");
		oblStockMap.put("stockSeqLength", 5);
		oblStockMap.put("stockRemark", "OBL_JOB_NO");
		oblStockMap.put("solution", "ECM");
		String oblJobNo = commonMapper.selectNextStockMaxNo(oblStockMap);
		
		//TB_COM_OBL의 OBL_JOB_NO
		map.put("oblJobNo", oblJobNo);
		
		map.put("blTypeMh", 'H');
		map.put("mblNo", savedBmsEstimateData.getMblNo());
		map.put("hblNo", savedBmsEstimateData.getBlNo());
		map.put("blNo", savedBmsEstimateData.getBlNo());
		if( bmsInWithCustomerMap != null ) {
			map.put("shipperName", bmsInWithCustomerMap.get("SHIPPER_NAME") != null ? bmsInWithCustomerMap.get("SHIPPER_NAME") : "");
			map.put("shipperAddress1", bmsInWithCustomerMap.get("SHIPPER_ADDRESS_1") != null ? bmsInWithCustomerMap.get("SHIPPER_ADDRESS_1") : "");
			map.put("shipperAddress2", bmsInWithCustomerMap.get("SHIPPER_ADDRESS_2") != null ? bmsInWithCustomerMap.get("SHIPPER_ADDRESS_2") : "");
			map.put("consigneeName", bmsInWithCustomerMap.get("CONSIGNEE_NAME") != null ? bmsInWithCustomerMap.get("CONSIGNEE_NAME") : "");
			map.put("consigneeAdress1", bmsInWithCustomerMap.get("CONSIGNEE_ADDRESS_1") != null ? bmsInWithCustomerMap.get("CONSIGNEE_ADDRESS_1") : "");
			map.put("vesselFlight", bmsInWithCustomerMap.get("VESSEL_FLIGHT") != null ? bmsInWithCustomerMap.get("VESSEL_FLIGHT") : "");
			map.put("voyage", bmsInWithCustomerMap.get("VOYAGE") != null ? bmsInWithCustomerMap.get("VOYAGE") : "");
		}else{
			map.put("shipperName", "");
			map.put("shipperAddress1","");
			map.put("shipperAddress2", "");
			map.put("consigneeName", "");
			map.put("consigneeAdress1", "");
			map.put("vesselFlight", "");
			map.put("voyage", "");
		}
		invoiceMapper.insertOblForStorage(map);
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("seqNo", 1);
		tmpMap.put("gmtTime", gmtTime);
		tmpMap.put("entCode", entCode);
		tmpMap.put("entBusinessCode", "");
		tmpMap.put("updateUserId", userId);
		tmpMap.put("solution", "ECM");
		tmpMap.put("stockType", "OD");
		tmpMap.put("stockSign", "COD");
		tmpMap.put("stockSeqLength", 8);
		tmpMap.put("dateLength", null);
		tmpMap.put("signType", null);
		tmpMap.put("stockDateFormat", null);
		tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		tmpMap.put("stockRemark", "ORDER-JOB NO");
		String ecmOdJobNo = commonMapper.selectNextStockMaxNo(tmpMap);
		//TB_ECM_OD의 OD_JOB_NO
		map.put("ecmOdJobNo", ecmOdJobNo);
		
		Map<String, Object> odStockMap = new HashMap<String, Object>();
		odStockMap.putAll(map);
		odStockMap.put("stockType", "OD");
		odStockMap.put("stockSign", "COD");
		odStockMap.put("stockRemark", "ORDER-JOB NO");
		
		String comOdJobNo = commonMapper.selectNextStockMaxNo(odStockMap);
		//TB_COM_OD의 OD_JOB_NO
		map.put("comOdJobNo", comOdJobNo);
		map.put("mblNo", savedBmsEstimateData.getMblNo());
		map.put("hblNo", savedBmsEstimateData.getBlNo());
		map.put("blTypeMh", 'H');
		map.put("customerCode", bmsEstimate.getCustomerCode());
		map.put("customerName", bmsEstimate.getCustomerName());
//		map.put("obarDate", bmsEstimate.getBlNo());
		
		map.put("attn", bmsEstimate.getAttn());
		map.put("tel", bmsEstimate.getTel());
		map.put("email", bmsEstimate.getEmail());
		
		map.put("entCode", entCode);
		map.put("entBusinessCode", entBusinessCode);
		map.put("gmtTime", gmtTime);
		map.put("updateUserId", userId);
		map.put("userId", userId);
		Util.accessControlDatabase(map, "entCode|entBusinessCode|gmtTime|updateUserId|userId", false, null, null, null, null);
		invoiceMapper.insertComOdForStorage(map);
		
		bmsEstimate.setComOdJobNo(comOdJobNo);
		bmsChargeMapper.updateBmsEstimateComOdJobNo(bmsEstimate);
		savedBmsEstimateData.setComOdJobNo(comOdJobNo);
		
		int chargeSeqNum = 1;
		String chargeSeqNo = comOdJobNo + "-" + Util.lpad(Util.null2str(chargeSeqNum++), 4, '0');
		map.put("chargeSeqNo", chargeSeqNo);
		
		map.put("localOversea", "LOCAL");
		map.put("arAp", "AR"); // AR: 매출
		
		String currencyCodeLoc = "KRW";
		String currencyCodeCur = "KRW"; // 외화도 KRW기준으로
		double exchangeRate = 1; // 어차피 외화도 KRW기준이므로 환율비율을 1로 고정
		double rateCur = 1;
		double rateLoc = 0;
		rateLoc = rateCur * exchangeRate;
		
		map.put("currencyCodeLoc", currencyCodeLoc);
		map.put("currencyCodeCur", currencyCodeCur);
		map.put("exchangeRate", exchangeRate);
		
		map.put("amountLoc", savedBmsEstimateData.getAmountLoc());
		map.put("amountVatLoc", savedBmsEstimateData.getAmountVatLoc());
		map.put("amountTotalLoc", savedBmsEstimateData.getAmountTotalLoc());
		
		map.put("amountCur", savedBmsEstimateData.getAmountLoc());
		map.put("amountVatCur", savedBmsEstimateData.getAmountVatLoc());
		map.put("amountTotalCur", savedBmsEstimateData.getAmountTotalLoc());
		
		map.put("currencyTotal", currencyCodeCur + ":" + String.valueOf(map.get("amountTotalCur")) );
		
		// 입력받는 반입 및 반출의 화주(CUSTOMER_CODE)는 공백입력이 가능하기때문에 정산관리메뉴 정산내역 및 계산서발행 조회가 안될수있다. (조회항목인 TB_COM_OD_CHG_INVOICE의 CUSTOMER_CODE가 공백일시 조회 안됨)
		map.put("accountCode", savedBmsEstimateData.getCustomerCode());
		map.put("accountName", savedBmsEstimateData.getCustomerName());  
		map.put("invoiceDate", DateTimeUtil.str2date(DateTimeUtil.getNow(), "yyyy-MM-dd"));
		
		String inVoiceJobNo  = 	this.getNextInvoiceJobNo(gmtTime,entCode,entBusinessCode,userId);
		String inVoiceNo  = 	this.getNextInvoiceNo(gmtTime,entCode,entBusinessCode,userId, estimateDate);
		map.put("invoiceJobNo", inVoiceJobNo);
		map.put("invoiceNo", inVoiceNo);
		
		map.put("vatPercent", 10);
		invoiceMapper.insertComOdChgForStorage(map);
		
		bmsChargeMapper.insertTbSasInvoiceTax(map);
		
		map.put("rateLoc", rateLoc);
		map.put("rateCur", rateCur);
		if(Util.null2str(savedBmsEstimateData.getApplyClass()).equals("6")) {	// 요율구분이 6.보수작업인 경우 세금계산서 발행 품목에 '보수작업' 이 들어가도록 수정 2026.02.25 2sowell
			map.put("chargeName", "보수작업");
		}else {
			map.put("chargeName", "창고료");
		}
		bmsChargeMapper.insertTbSasInvoiceItemTax(map);

		/* 세금 계산서 정산 계산 여부 Y로 처리 */
		bmsChargeMapper.updateBmsEstimateInvoiceYnYes(bmsEstimate);


		bmsOutSaveResult.setResultCode("A012051");// 저장 되었습니다.
		return bmsOutSaveResult;
	}



	public BmsOutSaveResult updateBmsEstimate(BmsEstimate bmsEstimate, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("entCode", bmsEstimate.getEntCode());
		map.put("inNo", bmsEstimate.getInNo());
		map.put("outPlanNo", bmsEstimate.getOutPlanNo());
		map.put("outNo", bmsEstimate.getOutNo());
		Map<String, Object> bmsInWithOutCustomerInfo = bmsChargeMapper.selectCustomerOfInOut(map);
		
		/* if("".equals(bmsEstimate.getCustomerCode())) {
			if( bmsInWithOutCustomerInfo != null ) {
				if(bmsInWithOutCustomerInfo.get("OUT_CUSTOMER_NAME") != null && !"".equals(bmsInWithOutCustomerInfo.get("OUT_CUSTOMER_NAME"))) {
					bmsEstimate.setCustomerCode(bmsInWithOutCustomerInfo.get("OUT_CUSTOMER_CODE").toString());
				 	bmsEstimate.setCustomerName(bmsInWithOutCustomerInfo.get("OUT_CUSTOMER_NAME").toString());
				}else if(bmsInWithOutCustomerInfo.get("IN_CUSTOMER_NAME") != null && !"".equals(bmsInWithOutCustomerInfo.get("IN_CUSTOMER_NAME"))) {
				 	bmsEstimate.setCustomerCode(bmsInWithOutCustomerInfo.get("IN_CUSTOMER_CODE").toString());
				 	bmsEstimate.setCustomerName(bmsInWithOutCustomerInfo.get("IN_CUSTOMER_NAME").toString());
				}
			}
		} */
		int intRow = bmsChargeMapper.updateBmsEstimate(bmsEstimate );

		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(bmsEstimate.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(bmsEstimate.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(bmsEstimate.getRegisterUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_ESTIMATE);
		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
		processLogs.setDescription(BmsConsts.DbType.BMS_ESTIMATE+" [수정 NO:"+String.valueOf(bmsEstimate.getEstimateNo())+"]");
		processLogs.setJobNo(String.valueOf(bmsEstimate.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(bmsEstimate));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(bmsEstimate.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(bmsEstimate.getLogUserName());
		processLogs.setProcessUserNameEng(bmsEstimate.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(bmsEstimate.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
				, ""
				, bmsEstimate
		);

		// WT,CT 데이터 업데이트
		bmsOutSaveResult.setResultCode("A012051"); // 저장 되었습니다.
		return bmsOutSaveResult;
	}


	/***
	 * 	정산료 항목에 대해 다건 선택후  삭제 처리
	 *
	 * 반입 처리후 이력 남김
	 * @param delete
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult deleteBmsEstimate(DeleteBmsOut delete, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();

		BmsEstimate esti ;
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		int resultCnt =0 ; //삭제 처리 카운트
		int failCnt =0; // 삭제 처리 불가 카운트
		SearchBmsOut searchBmsOut = new SearchBmsOut();

		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();

		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = null;

		String userId  = delete.getInputUserId();
		String entCode  = delete.getEntCode();
		String entBusinessCode  = delete.getEntBusinessCode();
		ProcessLogs processLogs;
		for (Map<String, Object> deleteMap : deleteList) {
			searchBmsOut.setSrchEstimateNo(Integer.parseInt(Util.null2str(deleteMap.get("estimateNo"))));
			esti =  bmsChargeMapper.selectBmsEstimateDetail(searchBmsOut);
			log.info("delete ESTIMATE estimateNo:{}", esti.getEstimateNo());
			if (null != esti) {
				if("N".equals(esti.getInvoiceYn())) {  // invoice_YN 이 N일때만 삭제 처리 가능하다.
					resultCnt++;
					bmsChargeMapper.deleteBmsEstimate(esti);

					/** 삭제후 정산여부 N으로 변경  */
					BmsOut bmsOut = new BmsOut();
					bmsOut.setOutNo(esti.getOutNo());
					bmsOut.setUpdateUserId(userId);
					bmsChargeMapper.updateBmsOutEstimateYn_N(bmsOut);



					processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(delete.getEntCode()));
					processLogs.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
					processLogs.setLogJobType("DATA");
					processLogs.setBound("COM");
					processLogs.setBoundType("IMP");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
					processLogs.setDbType(BmsConsts.DbType.BMS_ESTIMATE);
					//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
					processLogs.setDescription(BmsConsts.DbType.BMS_ESTIMATE+" [삭제 NO:"+String.valueOf(deleteMap.get("estimateNo"))+", 삭제사유:"+ delete.getReason()+"]");
					processLogs.setJobNo(String.valueOf(deleteMap.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
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
							, delete.getReason() /* blNo */
							,null
					);

					//
					/*tmpLogMap =	this.getTempLogMap(entCode, entBusinessCode,userId  );
					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(resultCnt));
					logMap = new HashMap<String, Object>();
					logMap.putAll(tmpLogMap);
					logMap.put("reference_no_id", bulkData.getId());
					logMap.put("bound", "COM");
					logMap.put("bound_type", "IMP");
					logMap.put("job_no", String.valueOf(deleteMap.get("storageRateNo")));
					logMap.put("description", "BMS_STORAGE_RATE [삭제 NO:" + String.valueOf(deleteMap.get("storageRateNo")) + ", 삭제사유:" + delete.getReason() + "]");
					logMap.put("job_log", new Gson().toJson(deleteMap));
					logMap.put("process_user_name", delete.getLogUserName());
					bulkData.setJsonMap(logMap);
					bulkList.add(bulkData);*/

				}else{
					failCnt++ ;
				}
			} else {// not null
				failCnt++ ;
			}
			//commonService.insertLogBulk(bulkList);
		}
		result.setFailCnt(failCnt);
		result.setResultCnt(resultCnt);
		return result;
	}




	/**
	 * 선반입반출신고독려통보 조회 List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsStorageRateList selectBmsStorageRateList(SearchBmsRate search) throws Exception {
		BmsStorageRateList retList = new BmsStorageRateList();
		setSearchConditionRate(search);

		Util.accessControlDatabase(search, null, true, null, null, null, null);
		List<BmsStorageRate> list  = bmsChargeMapper.selectBmsStorageRateList(search);
		retList.setBmsStorageRateList(list);

		if(list.size() > 0) {
			retList.setTotal(list.get(0).getTotal());
		} else {
			retList.setTotal(0);
		}

		return retList;
	}
	public BmsStorageRateList selectBmsStorageRateDetailList(SearchBmsRate search) throws Exception {
		BmsStorageRateList retList = new BmsStorageRateList();

		List<BmsStorageRate> list  = bmsChargeMapper.selectBmsStorageRateDetailList(search);
		retList.setBmsStorageRateList(list);

		return retList;
	}

	public BmsWorkRateList selectBmsWorkRateList(SearchBmsRate search) throws Exception {
		BmsWorkRateList retList = new BmsWorkRateList();
		setSearchConditionRate(search);

		Util.accessControlDatabase(search, null, true, null, null, null, null);
		List<BmsWorkRate> list  = bmsChargeMapper.selectBmsWorkRateList(search);
		retList.setBmsWorkRateList(list);

		if(list.size() > 0) {
			retList.setTotal(list.get(0).getTotal());
		} else {
			retList.setTotal(0);
		}

		return retList;
	}
/*	public BmsWorkRateList selectBmsWorkRateDetailList(SearchBmsRate search) throws Exception {
		BmsWorkRateList retList = new BmsWorkRateList();
		List<BmsWorkRate> list  = bmsChargeMapper.selectBmsWorkRateDetailList(search);
		retList.setBmsWorkRateList(list);

		return retList;
	}*/

	public BmsRateBase selectBmsWorkRateDetail(SearchBmsRate search) throws Exception {
		BmsRateBase base = new BmsRateBase();
		BmsWorkRate  workRate  = bmsChargeMapper.selectBmsWorkRateDetail(search);
		base.setWorkRate(workRate);
		return base;
	}

	public BmsOutSaveResult insertBmsStorageRate(BmsRateData bmsRateData, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();

		for (BmsStorageRate storageRate:  bmsRateData.getDataRate()) {
			//A00073 // 중복 체크 로직
			/*int nDupCnt = bmsChargeMapper.selectChargeStorageDupCount(storageRate);
			if(nDupCnt ==0) {*/
				int intRow = bmsChargeMapper.insertBmsStorageRate(storageRate);
			 //
			/*}else
			{   //A00073") {  //
				bmsOutSaveResult.setResultCode("A00073");// 중복되는 #1# 값이 있습니다
			}*/
		}
		bmsOutSaveResult.setResultCode("A012051");// 저장 되었습니다.


		return bmsOutSaveResult;
	}



	public BmsOutSaveResult updateBmsStorageRate(BmsRateData bmsRateData, HttpServletRequest request) throws Exception {
		  BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();

		// 컨테이너 정보는 전체 삭제후에 입력 한다.
		for (BmsStorageRate storageRate:  bmsRateData.getDataRate()) {
			if(StringUtils.isNotBlank(storageRate.getStorageRateNo())) {
				int uptRow = bmsChargeMapper.updateBmsStorageRate(storageRate);
			}else { // 다섯개중에 2개가 삭제 된 경우는 2개는 insert로 처리된다.
				int intRow = bmsChargeMapper.insertBmsStorageRate(storageRate);
			}
		}
		// WT,CT 데이터 업데이트
		bmsOutSaveResult.setResultCode("A012051"); // 저장 되었습니다.
		return bmsOutSaveResult;
	}
	//void deleteBmsStorageRate(BmsStorageRate storageRate);

	public BmsOutSaveResult deleteBmsStorageRate(DeleteEstimate delete, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();

		BmsStorageRate storageRate ;
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		int resultCnt =0 ; //삭제 처리 카운트
		int failCnt =0; // 삭제 처리 불가 카운트
		SearchBmsRate searchBmsRate;

		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();

		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = null;

		String userId  = delete.getInputUserId();
		String entCode  = delete.getEntCode();
		String entBusinessCode  = delete.getEntBusinessCode();
		for (Map<String, Object> deleteMap : deleteList) {
			searchBmsRate = new SearchBmsRate();
			searchBmsRate.setSrchStorageRateNo((Integer.parseInt(Util.null2str(deleteMap.get("storageRateNo")))));
			searchBmsRate.setSrchApplyDate(Util.null2str(deleteMap.get("applyDate")));
			searchBmsRate.setSrchApplyClass( Util.null2str(deleteMap.get("applyClass")));
			storageRate =  bmsChargeMapper.selectBmsStorageRateDetail(searchBmsRate);
			log.info("delete BMS_STORAGE_RATE storageRateNo:{}", storageRate.getStorageRateNo());
			if (null != storageRate) {
				resultCnt++;
				bmsChargeMapper.deleteBmsStorageRate(storageRate);
				//
				tmpLogMap =	this.getTempLogMap(entCode, entBusinessCode,userId  );
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(resultCnt));
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("bound", "COM");
				logMap.put("bound_type", "IMP");
				logMap.put("job_no", String.valueOf(deleteMap.get("storageRateNo")));
				logMap.put("description", "BMS_STORAGE_RATE [삭제 NO:" + String.valueOf(deleteMap.get("storageRateNo")) + ", 삭제사유:" + delete.getReason() + "]");
				logMap.put("job_log", new Gson().toJson(deleteMap));
				logMap.put("process_user_name", delete.getLogUserName());
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);


			} else {// not null
				failCnt++ ;
			}
			// 한번에 처리 한는 로직
		}
		commonService.insertLogBulk(bulkList);
		result.setFailCnt(failCnt);
		result.setResultCnt(resultCnt);
		return result;
	}


	public BmsOutSaveResult insertBmsWorkRate(BmsWorkRate workRate, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();

		int nDupCnt = bmsChargeMapper.selectChargeWorkDupCount(workRate);
		if(nDupCnt ==0) {
			int intRow = bmsChargeMapper.insertBmsWorkRate(workRate);
			// 저장후 조회할 값 리턴
			bmsOutSaveResult.setWorkRateNo(workRate.getWorkRateNo());
			bmsOutSaveResult.setResultCode("A012051");// 저장 되었습니다.
		}else
		{   //A00073") {  //
			bmsOutSaveResult.setResultCode("A00073");// 중복되는 #1# 값이 있습니다
		}
		return bmsOutSaveResult;
	}



	public BmsOutSaveResult updateBmsWorkRate(BmsWorkRate workRate, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();

		// 컨테이너 정보는 전체 삭제후에 입력 한다.


		int intRow = bmsChargeMapper.updateBmsWorkRate(workRate );

		// WT,CT 데이터 업데이트
		bmsOutSaveResult.setResultCode("A012051"); // 저장 되었습니다.
		return bmsOutSaveResult;
	}
	//void deleteBmsStorageRate(BmsStorageRate storageRate);

	public BmsOutSaveResult deleteBmsWorkRate(DeleteEstimate delete, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();

		BmsWorkRate workRate  ;
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		int resultCnt =0 ; //삭제 처리 카운트
		int failCnt =0; // 삭제 처리 불가 카운트
		SearchBmsRate searchBmsRate;
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();

		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = null;

		String userId  = delete.getInputUserId();
		String entCode  = delete.getEntCode();
		String entBusinessCode  = delete.getEntBusinessCode();

		//
		for (Map<String, Object> deleteMap : deleteList) {
			searchBmsRate = new SearchBmsRate();
			searchBmsRate.setSrchWorkRateNo((Integer.parseInt(Util.null2str(deleteMap.get("workRateNo")))));
			searchBmsRate.setSrchApplyDate(Util.null2str(deleteMap.get("applyDate")));
			searchBmsRate.setSrchApplyClass( Util.null2str(deleteMap.get("applyClass")));

			workRate =  bmsChargeMapper.selectBmsWorkRateDetail(searchBmsRate);
			log.info("delete BMS_WORK_RATE WorkRateNo:{}", workRate.getWorkRateNo());
			if (null != workRate) {
					resultCnt++;
					bmsChargeMapper.deleteBmsWorkRate(workRate);

					tmpLogMap =	this.getTempLogMap(entCode, entBusinessCode,userId  );

					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(resultCnt));
					logMap = new HashMap<String, Object>();
					logMap.putAll(tmpLogMap);
					logMap.put("reference_no_id", bulkData.getId());
					logMap.put("bound", "");
					logMap.put("bound_type", "");
					logMap.put("job_no", String.valueOf(deleteMap.get("workRateNo")));
					logMap.put("description", "BMS_WORK_RATE [삭제 NO:" + String.valueOf(deleteMap.get("workRateNo")) + ", 삭제사유:" + delete.getReason() + "]");
  					logMap.put("job_log", new Gson().toJson(deleteMap));
					logMap.put("process_user_name", delete.getLogUserName());
					bulkData.setJsonMap(logMap);
					bulkList.add(bulkData);

					//
					//userAuth가 AD인 경우 사용건수 있더라도 수정이 가능하도록 수정됨/이때 시스템로그 남기도록 추가
					// 삭제로그 남기기


			} else {// not null
				failCnt++ ;
			}
		}
		// 한번에 처리 한는 로직
		commonService.insertLogBulk(bulkList);
		result.setFailCnt(failCnt);
		result.setResultCnt(resultCnt);
		return result;
	}
	/**
	 * 요율구분에 따른 발행번호및 보관일수(기본값) 리턴
	 * 1. 요율구분선택시 발행번호는 매출일자의 요율구분에 MAX + 1 한 값 4자리 예) 0010
	 * 2. 보관일수 = 반출일자 - 반입일자
	 *    2.1 요율구분이 4. 수출일반인 경우 보관일수가 4보다 작으면 보관일수 0
	 *    2.2 요율구분이 4. 수출일반 이면서 보관일수가 4 일 인 경우
	 *         반출시간 < 반입시간 작으면 보관일수 0
	 *    2.3 2.2 가 아니면서 보관일수가 1일 인 경우
	 *         반출시간 < 반입시간 작으면 보관일수 0
	 *    2.4 요율구분이 2 수입특송이면서 면제여부가 4일 면제 인 경우
	 *         보관일수 = 보관일수 - 휴일일수체크 + 1 - 4  예) 반입일 2024-04-30 반출일 2024-05-07 인 경우 보관일수 1
	 *    2.5 보관일수가 0보다 작으면 0을 넣는다.
	 *    2.6  만으로 1일 미만일 경우만 시간체크해서 2.3적용하고, 2일 이상일 경우는 시간 체크 하지 않고 무조건 올림으로 처리
	 *    2.7 면제 여부가 Y일경우  종가료, 종량료, THC료, 공급가액, 부가세, 합계, 입금액 등은 0으로 표시 하도록 하겠습니다.
	 *    2.8  2 수입특송인 경우 (2024-07-24) 반입일과 반출일 사이에 토요일, 일요일, 공휴일 이며,  휴일데이타 (TB_SMS_HOLIDAY ) 등록된것의 합이다. 07-25 kim 추가 사항
	 * @param search
	 * @return
	 */
	public BmsRateBase selectBmsApplyNoAndWorkDaysBaseRate(SearchBmsRate search) throws Exception {
		BmsRateBase baseDays  = 	bmsChargeMapper.selectBmsApplyNoAndWorkDays(search); //공휴일 뺀 보관일수 계산 프로시저호출

		int vStorageDays = getvStorageDays(search, baseDays); //면제일때 보관일수 재계산 함수호출
		BmsRateBase base = new BmsRateBase();
		base.setApplyNo(baseDays.getApplyNo()); // 발행번호
		base.setStorageDays(vStorageDays); // 보관일수
		//
		search.setSrchStorgeDays(vStorageDays);
		base.setStorageRate(bmsChargeMapper.selectBmsStorageRateBase(search)); // 보관료 계
		base.setWorkRate(bmsChargeMapper.selectBmsWorkRateBase(search)); // 작업료 계
		return base;
		//return bmsChargeMapper.selectBmsApplyNoAndWorkDays(search);
	}

	/***
	 * 요율구분에 따른 보관일수 계산 로직 적용
	 * 계산기준 반출일자에서 매출일자로 변경 2025.11.03 2sowell
	 * 2. 보관일수 = 매출일자 - 반입일자
	 *    2.1 요율구분이 4. 수출일반인 경우 보관일수가 4보다 작으면 보관일수 0
	 *    2.2 요율구분이 4. 수출일반 이면서 보관일수가 4 일 인 경우
	 *         입력시간 < 반입시간 작으면 보관일수 0
	 *    2.3 2.2 가 아니면서 보관일수가 1일 인 경우
	 *         입력시간 < 반입시간 작으면 보관일수 0
	 *    2.4 요율구분이 2 수입특송이면서 면제여부가 4일 면제 인 경우
	 *         보관일수 = 보관일수 - 휴일일수체크 + 1 - 4  예) 반입일 2024-04-30 매출일 2024-05-07 인 경우 보관일수 1
	 *    2.5 보관일수가 0보다 작으면 0을 넣는다.
	 *    2.6  만으로 1일 미만일 경우만 시간체크해서 2.3적용하고, 2일 이상일 경우는 시간 체크 하지 않고 무조건 올림으로 처리
	 *    2.7 면제 여부가 Y일경우  종가료, 종량료, THC료, 공급가액, 부가세, 합계, 입금액 등은 0으로 표시 하도록 하겠습니다.
	 *    2.8  2 수입특송인 경우 (2024-07-24) 반입일과 반출일 사이에 토요일, 일요일, 공휴일 이며,  휴일데이타 (TB_SMS_HOLIDAY ) 등록된것의 합이다. 07-25 kim 추가 사항
	 *    2.9 0. 요율구분이 2 수입특송인 경우 (2024-07-24) 인 경우 AND 면제여부 4일면제 인경우는 따로 아래와 같이 처리 한다.
	 * 			1. 반입일과 반출일 사이에 토요일, 일요일, 공휴일를 더한다.
	 * 			2. 공휴일은 휴일데이타 (TB_SMS_HOLIDAY ) 을 통해 해당일이면 1일을 더한다.
	 * 3. 요율구분이 6.보수작업인 경우 보관일수 0로 고정 2025.11.10 2sowell
	 * @param search
	 * @param baseDays
	 * @return
	 */
	private  int getvStorageDays(SearchBmsRate search, BmsRateBase baseDays) {
		// 2.7 면제 여부가 Y일경우  종가료, 종량료, THC료, 공급가액, 부가세, 합계, 입금액 등은 0으로 표시 하도록 하겠습니다.
		int vStorageDays = 0;
		
		if("6".equals(search.getSrchApplyClass())) {	// 6.보수작업 인경우 보관일수 1로 고정 2025.11.06 2sowell
			vStorageDays = 0;
			return vStorageDays;
		}
		
		if("2".equals(search.getSrchApplyClass()) && //2.수입특송
				"C".equals(search.getSrchFreeYnc()) //4일면제
				){  //2.수입특송 이면서 4일면제 일경우  - 휴일, 공휴일 포함 계산
			vStorageDays = baseDays.getStorageDays();
		}else { // 나머지는 단순 뺀값 계산
			vStorageDays = baseDays.getStorageAllDays();
		}
		//int vStorageDays = baseDays.getStorageDays(); // 전체 계산 로직
        // search.getSrchCarryinTime() : 13:23:23 8자리인데, 5자리로 들어 오면
		String carryinTime = StringUtils.replace(search.getSrchCarryinTime(),":","");
		String carryoutTime = StringUtils.replace(search.getSrchCarryoutTime(),":","");
		String registerTime = DateTimeUtil.getShortTimeString(); // 입력일시(hhmmss) 추가 2025.11.03 2sowell
		//: 1323 8자리인데, 4자리로 들어 오면뒤에 00을 합친다 6자리 만든다음 처리 .
		if(carryinTime.length() ==4){ carryinTime = carryinTime.concat("00") ;}
		//: 1323 8자리인데, 4자리로 들어 오면뒤에 00을 합친다. 6자리 만든다음 처리 .
		if(carryoutTime.length() ==4){ carryoutTime = carryinTime.concat("00") ;}
		if(vStorageDays < 0){ //2.5 보관일수가 0보다 작으면 0을 넣는다.
			vStorageDays =0;
		}
		if("4".equals(search.getSrchApplyClass())){
			if(vStorageDays < 4){
				vStorageDays =0;
			}else if(vStorageDays == 4) { // 4일 기준으로 보관일수 계산
				//if( Integer.parseInt( carryinTime ) >  Integer.parseInt( carryoutTime )){  //2.2 요율구분이 4. 수출일반 이면서 보관일수가 4 일 인 경우
				if( Integer.parseInt( carryinTime ) >  Integer.parseInt( registerTime )){	// 계산기준 입력시간으로 변경 2025.11.03 2sowell
						 vStorageDays = 0;                                                  //*    반출시간 < 반입시간 작으면 보관일수 0
				}else{
					      vStorageDays =1;
				}
			}else { // 4일 초과 일 경우에는 보관일수 -3으로 계산 한다.
				vStorageDays = vStorageDays - 3 ; // 5일 이상부터는 -3 해서 5일이면 2일, 6일이면 3으로 계산되어야 한다.
			}
		}else { // 요율구분이 4.수출일반 아닌 경우에 처리 방법
			if (vStorageDays == 1){ //1일일 경우에 반입시간이 반출시간 보다 크면 0이고 나머지는 그대로 처리 한다.
				// 2.6  만으로 1일 미만일 경우만 시간체크해서 2.3적용하고, 2일 이상일 경우는 시간 체크 하지 않고 무조건 올림으로 처리
				//if( Integer.parseInt( carryinTime ) >  Integer.parseInt( carryoutTime )){  //2.2 요율구분이 4. 수출일반 이면서 보관일수가 4 일 인 경우
				if( Integer.parseInt( carryinTime ) >  Integer.parseInt( registerTime )){	// 계산기준 입력시간으로 변경 2025.11.03 2sowell
					vStorageDays = 0;   //*    반출시간 < 반입시간 작으면 보관일수 0
				}else{
					vStorageDays = 1;
				}
			}
		}

		if( "C".equals(search.getSrchFreeYnc())){ //4일 면제일경우
			if("2".equals(search.getSrchApplyClass())) {
				//  2.수입특송&4일면제 계산 : 보관일수 = 보관일수 - 휴일일수체크 + 1 - 4  예) 반입일 2024-04-30 반출일 2024-05-07 인 경우 보관일수 1
				vStorageDays = vStorageDays +(1 - 4);
			}else {
				vStorageDays = vStorageDays - 4; // 나머지경우 4일 면제 일경우 무조건 4를 뺀다 240514. kim test 요청 사항
			}
 			if( vStorageDays < 0){
				vStorageDays = 0;
			}
		}
		return vStorageDays;
	}
	private Map<String, Object>  getTempLogMap(String entCode,  String entBusinessCode , String userId) {
	/*	BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");*/
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", entCode);
		tmpLogMap.put("ent_business_code", entBusinessCode);
		tmpLogMap.put("solution", "BMS");
		tmpLogMap.put("process_user_id", userId);
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "DELETE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		return  tmpLogMap;
	}


	public BmsOutSaveResult saveBmsHoliday( DeleteEstimate saveData, HttpServletRequest request) throws Exception {
		BmsOutSaveResult bmsOutSaveResult = new BmsOutSaveResult();
		Integer inCnt =0;
		Integer upCnt =0;
		Integer failCnt =0;
		List<Map<String, Object>> deleteList =  saveData.getDataList();

		BmsHoliday holiday = null;
		//
		for (Map<String, Object> saveMap : deleteList) {
			holiday = new BmsHoliday();
			holiday.setHolidayDate(Util.null2str(saveMap.get("holidayDate")));
			holiday.setOldHolidayDate(Util.null2str(saveMap.get("oldHolidayDate")));
			holiday.setCrudMode(Util.null2str(saveMap.getOrDefault("crudMode","C")));
			if("C".equals(holiday.getCrudMode())) { // 입력
				inCnt++;
				int intRow = bmsChargeMapper.insertBmsHoliday(holiday);
			}else if("U".equals(holiday.getCrudMode())) { // 입력
				upCnt++;
				int intRow = bmsChargeMapper.updateBmsHoliday(holiday);
			}else {
				failCnt++;
			}
		}
		// 저장후 조회할 값 리턴
		//bmsOutSaveResult.setHolidayDate(holiday.getHolidayDate());
		bmsOutSaveResult.setInCnt(inCnt); // 입력
		bmsOutSaveResult.setUpCnt(upCnt); // 수정
		bmsOutSaveResult.setResultCnt(inCnt+upCnt);
		bmsOutSaveResult.setFailCnt(failCnt); // 실패
		bmsOutSaveResult.setResultCode("A012051");// 저장 되었습니다.
		return bmsOutSaveResult;
	}
	public BmsOutSaveResult deleteBmsHoliday(DeleteEstimate delete, HttpServletRequest request) throws Exception {
		BmsOutSaveResult result = new BmsOutSaveResult();
		List<Map<String, Object>> deleteList =  delete.getDataList();
		int resultCnt =0 ; //삭제 처리 카운트
		int failCnt =0; // 삭제 처리 불가 카운트
        String crudMode="";
		BmsHoliday bmsHoliday; // = new BmsHoliday();
		// 선택된 항목만 삭제 처리 하므로 해당 데이타가 U,R인경우만 삭제 처리 가능하다.
		for (Map<String, Object> deleteMap : deleteList) {
				bmsHoliday = new BmsHoliday();
				crudMode = Util.null2str(deleteMap.get("crudMode"));
				bmsHoliday.setOldHolidayDate(Util.null2str(deleteMap.get("oldHolidayDate")));
				if("R".equals(crudMode) || "U".equals(crudMode)) {
					String logVal = "delete TB_SMS_HOLIDAY OldHolidayDate:" + bmsHoliday.getOldHolidayDate();
					log.info(logVal);
					resultCnt++;
					bmsChargeMapper.deleteBmsHoliday(bmsHoliday);
				}else {failCnt++;}
		}
		result.setResultCnt(resultCnt);
		result.setFailCnt(failCnt);
		return  result;
	}


	public BmsWorkRateList selectBmsHolidayList(SearchBmsRate search) throws Exception {
		BmsWorkRateList retList = new BmsWorkRateList();

		Util.accessControlDatabase(search, null, true, null, null, null, null);
		List<BmsHoliday>  list  = bmsChargeMapper.selectBmsHolidayList(search);
		retList.setHolidayList(list);
		if(list.size() > 0) {
			retList.setTotal(list.get(0).getTotal());
		} else {
			retList.setTotal(0);
		}
		return retList;
	}

	/***
	 * 세금 계산시 계산시 인보이스 Job No
	 * @return
	 */
	public String getNextInvoiceJobNo(String gmtTime, String entCode, String entBusinessCode, String userId){

		//셰금계산서 계산시(TB_SAS_INVOICE.INVOICE_JOB_NO)
		Map<String,Object> paramMap = new HashMap();
		paramMap.put("seqNo", "1");
		paramMap.put("gmtTime", gmtTime ) ; // "0"
		paramMap.put("entCode", entCode ); // "YHTXX"
		paramMap.put("entBusinessCode", entBusinessCode ); // "YHTXX-KR"
		paramMap.put("updateUserId", userId); //"USER01"
		paramMap.put("solution", "COM");
		paramMap.put("stockSign", "WAR");
		paramMap.put("stockType", "ARP");
		paramMap.put("stockSeqLength", 5);
 		paramMap.put("stockRemark", "DOMESTIC AR INVOICE NO"); //AIR WAY

		return   commonMapper.selectNextStockMaxNo(paramMap) ;
	}

	public String getNextInvoiceNo(String gmtTime, String entCode, String entBusinessCode
				, String userId, String estimateDate) throws Exception {

		//셰금계산서 계산시(TB_SAS_INVOICE.INVOICE_NO)
		Map<String,Object> paramMap = new HashMap();
		paramMap.put("seqNo", "1");
		paramMap.put("gmtTime", gmtTime);
		paramMap.put("entCode", entCode);
		paramMap.put("entBusinessCode", entBusinessCode);
		paramMap.put("updateUserId", userId);
		paramMap.put("solution", "BMS");
/**
 * BOUND          AIR
   BOUND_TYPE     IMP로 되어 있음
 **/
		paramMap.put("bound", "AIR");  // 창고료 설계서에
		paramMap.put("boundType", "IMP"); // 수입
		//paramMap.put("boundType", "EXP"); // 수출

		paramMap.put("stockDate", estimateDate);  // 정산일자를 입력 , ESTIMATE_DATE: 창고료계산일자  (매출일자)
		paramMap.put("dateFormat", "23");  //

		paramMap.put("stockType", "WAR"); //
		//      tmpMap.put("stockSign", "DAR");
		paramMap.put("statusSbi", "B");
		paramMap.put("portPrefix", "");
		paramMap.put("customerPrefix", "");

		paramMap.put("stockSeqLength", 8);
		paramMap.put("dateLength", null);
		paramMap.put("signType", null);
		paramMap.put("stockDateFormat", null);
		paramMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		paramMap.put("stockRemark", "BMS INVOICE_NO NO");
		
		Util.accessControlDatabase(paramMap, "gmtTime", false, null, null, null, null);
		return  commonMapper.selectNextStockNo(paramMap);
	}

	public List<BmsWorkRate> selectChargeWorkRateCombo() {
		return bmsChargeMapper.selectChargeWorkRateCombo();
	}
	public List<BmsStorageRate> selectChargeStorageRateCombo() {
		return bmsChargeMapper.selectChargeStorageRateCombo();
	}
	
	public BmsCustomerChargeInfo selectBmsChargeCustomerDetail(BmsCustomerChargeInfo search) {
		return bmsChargeMapper.selectBmsCustomerChargeInfo(search);
	}
	
	public BmsCustomerChargeInfo insertBmsChargeCustomerDetail(BmsCustomerChargeInfo bmsCustomerChargeInfo) {
		
		// REGISTRATION_CODE 확인
		String registrationCode = bmsChargeMapper.selectBmsBusRegistrationCode(bmsCustomerChargeInfo);
		if(!Util.null2str(registrationCode).equals("")) {	// 해당 사업자번호로 BUSINESS_REG_INFO가 있는경우 조회해서 세팅
			bmsCustomerChargeInfo.setRegistrationCode(registrationCode);
		}else {	// 해당 사업자번호로 BUSINESS_REG_INFO가 없는경우 새로 생성해서 세팅
			bmsCustomerChargeInfo.setRegistrationCode(bmsChargeMapper.selectBmsCustomerRegistrationCode(bmsCustomerChargeInfo));
		}
		
		// 화주 생성
		bmsChargeMapper.insertBmsCustomerChargeInfo(bmsCustomerChargeInfo);
		
		return bmsCustomerChargeInfo;
	}
	
	public BmsCustomerChargeInfo updateBmsChargeCustomerDetail(BmsCustomerChargeInfo bmsCustomerChargeInfo) {
		// REGISTRATION_CODE 확인
		if(Util.null2str(bmsCustomerChargeInfo.getRegistrationCode()).equals("")) {	// REGISTRATION_CODE가 없는경우 
			// 해당 사업자번호로 REGISTRATION_CODE 조회
			String registrationCode = bmsChargeMapper.selectBmsBusRegistrationCode(bmsCustomerChargeInfo);
			if(!Util.null2str(registrationCode).equals("")) {	// 해당 사업자번호로 BUSINESS_REG_INFO가 있는경우 조회해서 세팅
				bmsCustomerChargeInfo.setRegistrationCode(registrationCode);
			}else {	// 해당 사업자번호로 BUSINESS_REG_INFO가 없는경우 새로 생성해서 세팅
				bmsCustomerChargeInfo.setRegistrationCode(bmsChargeMapper.selectBmsCustomerRegistrationCode(bmsCustomerChargeInfo));
			}
		}
		// 화주 정보 수정
		bmsChargeMapper.updateBmsCustomerChargeInfo(bmsCustomerChargeInfo);
		
		return bmsCustomerChargeInfo;
	}
}
