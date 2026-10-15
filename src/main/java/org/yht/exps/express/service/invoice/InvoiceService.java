package org.yht.exps.express.service.invoice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.invoice.InvoiceMapper;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.invoice.BrokerCharge;
import org.yht.exps.express.model.invoice.BrokerChargeList;
import org.yht.exps.express.model.invoice.CourierCharge;
import org.yht.exps.express.model.invoice.CourierChargeList;
import org.yht.exps.express.model.invoice.Invoice;
import org.yht.exps.express.model.invoice.PartnerDetailCharge;
import org.yht.exps.express.model.invoice.PartnerDetailChargeList;
import org.yht.exps.express.model.invoice.PartnerSummaryCharge;
import org.yht.exps.express.model.invoice.PartnerSummaryChargeList;
import org.yht.exps.express.model.invoice.WarehouseCharge;
import org.yht.exps.express.model.invoice.WarehouseChargeList;
import org.yht.exps.express.model.invoice.WorkFeeCharge;
import org.yht.exps.express.model.invoice.WorkFeeChargeList;
import org.yht.exps.express.model.invoice.WorkFeeRate;
import org.yht.exps.express.model.invoice.search.SearchBatchDeleteCost;
import org.yht.exps.express.model.invoice.search.SearchInvoiceList;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class InvoiceService {

	@Autowired
	InvoiceMapper invoiceMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	@Resource(name = "commonService")
	CommonService commonService;

	
	/**
	 * 전자상거래 정산관리 창고료 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public WorkFeeChargeList selectWorkFeeChargeList(SearchInvoiceList search) throws Exception {
		WorkFeeChargeList workFeeChargeList = new WorkFeeChargeList();
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
		
		Util.getCryptographyYn(search);
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|"
				+ "od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
		List<WorkFeeCharge> list  = invoiceMapper.selectWorkFeeChargeList(search);
		workFeeChargeList.setWorkFeeChargeList(list);
		
		if(list.size() > 0) {
			workFeeChargeList.setTotal(list.get(0).getTotal());
		}else {
			workFeeChargeList.setTotal(0);
		}
		return workFeeChargeList;
	}
	
	/**
	 * 전자상거래 정산관리 창고료 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public WarehouseChargeList selectWarehouseChargeList(SearchInvoiceList search) throws Exception {
		WarehouseChargeList warehouseChargeList = new WarehouseChargeList();
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
		
		Util.getCryptographyYn(search);
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|"
				+ "od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
		List<WarehouseCharge> list  = invoiceMapper.selectWarehouseChargeList(search);
		warehouseChargeList.setWarehouseChargeList(list);
		
		if(list.size() > 0) {
			warehouseChargeList.setTotal(list.get(0).getTotal());
		}else {
			warehouseChargeList.setTotal(0);
		}
		return warehouseChargeList;
	}
	
	/**
	 * 전자상거래 정산관리 택배사 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public CourierChargeList selectCourierChargeList(SearchInvoiceList search) throws Exception {
		CourierChargeList courierChargeList = new CourierChargeList();
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
		
		Util.getCryptographyYn(search);
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|"
				+ "od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
		List<CourierCharge> list  = invoiceMapper.selectCourierChargeList(search);
		courierChargeList.setCourierChargeList(list);
		
		if(list.size() > 0) {
			courierChargeList.setTotal(list.get(0).getTotal());
		}else {
			courierChargeList.setTotal(0);
		}
		return courierChargeList;
	}
	
	/**
	 * 전자상거래 정산관리 관세사 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public BrokerChargeList selectBrokerChargeList(SearchInvoiceList search) throws Exception {
		BrokerChargeList brokerChargeList = new BrokerChargeList();
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
		
		Util.getCryptographyYn(search);
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|"
				+ "od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
		List<BrokerCharge> list  = invoiceMapper.selectBrokerChargeList(search);
		brokerChargeList.setBrokerChargeList(list);
		
		if(list.size() > 0) {
			brokerChargeList.setTotal(list.get(0).getTotal());
		}else {
			brokerChargeList.setTotal(0);
		}
		return brokerChargeList;
	}
	
	/**
	 * 전자상거래 정산관리 파트너세부 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public PartnerDetailChargeList selectPartnerDetailChargeList(SearchInvoiceList search) throws Exception {
		PartnerDetailChargeList partnerDetailChargeList = new PartnerDetailChargeList();
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
		
		Util.getCryptographyYn(search);
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE|cost.AP_INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|"
				+ "od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
		List<PartnerDetailCharge> list  = invoiceMapper.selectPartnerDetailChargeList(search);
		partnerDetailChargeList.setPartnerDetailChargeList(list);
		
		if(list.size() > 0) {
			partnerDetailChargeList.setTotal(list.get(0).getTotal());
		}else {
			partnerDetailChargeList.setTotal(0);
		}
		return partnerDetailChargeList;
	}
	
	/**
	 * 전자상거래 정산관리 파트너집계 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public PartnerSummaryChargeList selectPartnerSummaryChargeList(SearchInvoiceList search) throws Exception {
		PartnerSummaryChargeList partnerSummaryChargeList = new PartnerSummaryChargeList();
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
		
		Util.getCryptographyYn(search);
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|"
				+ "od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
		List<PartnerSummaryCharge> list  = invoiceMapper.selectPartnerSummaryChargeList(search);
		partnerSummaryChargeList.setPartnerSummaryChargeList(list);
		
		if(list.size() > 0) {
			partnerSummaryChargeList.setTotal(list.get(0).getTotal());
		}else {
			partnerSummaryChargeList.setTotal(0);
		}
		return partnerSummaryChargeList;
	}
	
	/**
	 * 전자상거래 추가비용 삭제
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult deleteCost(Invoice invoice, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		if(invoiceMapper.deleteOrderCost(invoice) == 0){
			orderSaveResult.setResultCode("A11176");	/** 정산서가 발행된 건이 있어 변경할 수 없습니다. */
			return orderSaveResult;
		}
		
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		invoice.setOdJobNoList(null);
		processLogs.setJobFuncType("DELETE");
		processLogs.setEntCode(invoice.getEntCode());
		processLogs.setEntBusinessCode(invoice.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(invoice.getBound());
		processLogs.setBoundType(invoice.getBoundType());
		processLogs.setProcessUserId(invoice.getRegisterUserId());
		processLogs.setJobNo("");
		processLogs.setLogJobType("DATA");
		processLogs.setDescription("DELETE ECM ORDER COST");
		processLogs.setJobLog(new Gson().toJson(invoice));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(invoice.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(invoice.getLogUserName());
		processLogs.setProcessUserNameEng(invoice.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 추가비용 삭제
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult batchDeleteCost(SearchBatchDeleteCost search, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		String targetData = Util.null2str(search.getTargetData());
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		if(targetData.equals("srchData")){
			SearchInvoiceList searchInvoiceList = search.getSearchInvoiceList();
			/* N개 검색(멀티타입) */
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(Util.null2str(searchInvoiceList.getSrchNo()))) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(searchInvoiceList.getSrchNo())).split(",")));
			}
			
			if(searchNoList.size() > 1){
				searchInvoiceList.setSearchType("multi");
				searchInvoiceList.setSearchNoList(searchNoList);
			}else{
				searchInvoiceList.setSearchType("");
				searchInvoiceList.setSrchNo(Util.null2str(searchInvoiceList.getSrchNo()).trim().replace(",", ""));
			}
			search.setCheckDataList(null);
			
			Util.getCryptographyYn(searchInvoiceList);
			Util.accessControlDatabase(searchInvoiceList, "invoiceAccountType|srchDateFormat|srchChgCodeSystemIn|srchArAp", false, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO"
					+ "|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
		}else{
			search.setSearchInvoiceList(null);
		}
		
		Util.accessControlDatabase(search, "invoiceAccountType", false, null, "", null, "");
		List<Map<String, Object>> dataList = invoiceMapper.selectCostBatchDelete(search);
		
		if(dataList != null && dataList.size() > 0){
			BulkData bulkData = null;
			String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
			Map<String, Object> logMap = null;
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", search.getEntCode());
			tmpLogMap.put("ent_business_code", search.getUserBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", search.getUpdateUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "DELETE");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", search.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", search.getLogUserName());
			tmpLogMap.put("process_user_name_eng", search.getLogUserNameEng());
			
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			int seq = 1;
			int cnt = 1;
			String odJobNo = null;
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				InvoiceMapper invoiceBatchMapper = sqlSession.getMapper(InvoiceMapper.class);
				
				for(Map<String, Object> data : dataList) {
					if(data.get("AR_INVOICE_JOB_NO") != null || data.get("AP_INVOICE_JOB_NO") != null){
						orderSaveResult.setResultCode("A11176");	/** 정산서가 발행된 건이 있어 변경할 수 없습니다. */
						return orderSaveResult;
					}
					if(odJobNo == null || !Util.null2str(data.get("OD_JOB_NO")).equals(odJobNo)){
						bulkData = new BulkData();
						bulkData.setActionType(BulkData.Type.CREATE);
						bulkData.setIndexName(index);
						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
						logMap = new HashMap<String, Object>();
						logMap.putAll(tmpLogMap);
						logMap.put("reference_no_id", bulkData.getId());
						logMap.put("bound", search.getBound());
						logMap.put("bound_type", search.getBoundType());
						logMap.put("job_no", data.get("OD_JOB_NO"));
						logMap.put("job_log", new Gson().toJson(search));
						logMap.put("description","Cost batch delete [MBL_NO : "+ Util.null2str(data.get("MBL_NO")) + "][HBL_NO : " + Util.null2str(data.get("MBL_NO")) + "][INVOICE_TYPE : " + search.getInvoiceType() + "]");
						bulkData.setJsonMap(logMap);
						bulkList.add(bulkData);
					}
					
					data.put("entCode", search.getEntCode());
					data.put("invoiceType", search.getInvoiceType());
					data.put("invoiceAccountType", search.getInvoiceAccountType());
					
					Util.accessControlDatabase(data, "invoiceAccountType", false, null, "", null, "");
					invoiceBatchMapper.deleteCostBatch(data);
					
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
			}catch(Exception e) {
				throw e;
			}finally{
				sqlSession.close();
			}
			
			commonService.insertLogBulk(bulkList);
		}else{
			orderSaveResult.setResultCode("A00965");	/** 조회된 내역이 없습니다. */
		}
		
		return orderSaveResult;
	}
	
	/**
	 * 정산배치처리 건수조회
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult batchDeleteCostCount(SearchBatchDeleteCost search, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Util.accessControlDatabase(search, "invoiceAccountType", false, null, "", null, "");
		if(search.getSearchInvoiceList() != null) {
			Util.getCryptographyYn(search.getSearchInvoiceList());
			Util.accessControlDatabase(search.getSearchInvoiceList(), "invoiceAccountType|srchDateFormat|srchChgCodeSystemIn|srchArAp", false, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO"
					+ "|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
		}
		orderSaveResult.setResultCode(Util.null2str(invoiceMapper.selectCostBatchDelete(search).size()));
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 정산서 생성
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult insertInvoice(Invoice invoice, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		String ecmOdJobNo;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("entCode", invoice.getEntCode());
		map.put("entBusinessCode", invoice.getEntBusinessCode());
		
		List<Map<String, Object>> odCostList = null;
		List<Map<String, Object>> ecmOdChgList = null;
		// 일반정산일 경우 TB_ECM_OD_COST 조회 X
		if(!Util.null2str(invoice.getEcmOdChgYn()).equals("Y")) {
			if(invoice.getOdJobNoList() == null){
				Util.accessControlDatabase(invoice.getSearchInvoiceList(), "srchArAp", false, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO"
						+ "|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
				odCostList = invoiceMapper.selectOdCostSum(invoice.getSearchInvoiceList());
			}else{
				Util.accessControlDatabase(invoice.getSearchInvoiceList(), "arAp", false, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO"
						+ "|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
				odCostList = invoiceMapper.selectOdCostSumSelectGrid(invoice);
			}
			if(odCostList == null || odCostList.size() == 0){
				orderSaveResult.setResultCode("A00163");	/** 정산처리할 수 있는 자료가 없습니다. */
				return orderSaveResult;
			}else{
				invoice.setObarDate(Util.null2str(odCostList.get(0).get("OBAR_DATE")));
				invoice.setMblNo(Util.null2str(odCostList.get(0).get("MBL_NO")));
				invoice.setHblNo(Util.null2str(odCostList.get(0).get("HBL_NO")));
			}
			ecmOdJobNo = Util.null2str(odCostList.get(0).get("OD_JOB_NO"));
		}else {
			ecmOdChgList = (List<Map<String, Object>>) invoice.getEcmOdChgList();
			for(Map<String, Object> ecmOdChg : ecmOdChgList) {
				if(Util.null2str(ecmOdChg.get("INVOICE_CONFIRM_YN")).equals("")) {
					orderSaveResult.setResultCode("A11176"); /** 정산서가 발행된 건이 있어 변경할 수 없습니다. */
					return orderSaveResult;
				}
			}
			ecmOdJobNo = invoice.getOdJobNoList().get(0);
		}
		Util.accessControlDatabase(invoice, "gmtTime", false, null, null, null, null);
		map.put("gmtTime", invoice.getGmtTime());
		map.put("updateUserId", invoice.getRegisterUserId());
		map.put("stockSeqLength", 5);
		map.put("userId", invoice.getRegisterUserId());
		map.put("solution", "ECM");
		map.put("bound", invoice.getBound());
		map.put("boundType", invoice.getBoundType());
		map.put("dateFormat", invoice.getDateFormat());
		
		/** 정산서관리번호(INVOICE_JOB_NO) */
		Map<String, Object> invoiceStockMap = new HashMap<String, Object>();
		invoiceStockMap.putAll(map);
		invoiceStockMap.put("stockType", "ARP");
		invoiceStockMap.put("solution", "COM");
		/** 정산번호(INVOICE_NO) */
		Map<String, Object> invoiceNoStockMap = new HashMap<String, Object>();
		invoiceNoStockMap.putAll(map);
		invoiceNoStockMap.put("stockType", "ARP");
		invoiceNoStockMap.put("solution", "EMS");
		invoiceNoStockMap.put("statusSbi", "B");
		invoiceNoStockMap.put("portPrefix", "");
		invoiceNoStockMap.put("customerPrefix", "");
		invoiceNoStockMap.put("saveYn", "Y");
		invoiceNoStockMap.put("inputUserId", invoice.getRegisterUserId());
		invoiceNoStockMap.put("stockDate", invoice.getInvoiceDate());
		/** TB_COM_OD관리번호(OD_JOB_NO) */
		Map<String, Object> odStockMap = new HashMap<String, Object>();
		odStockMap.putAll(map);
		odStockMap.put("stockType", "OD");
		odStockMap.put("stockSign", "COD");
		odStockMap.put("stockRemark", "ORDER-JOB NO");
		
		if("OVERSEA".equals(invoice.getLocalOversea())){
			invoiceStockMap.put("stockSign", "ODC");
			invoiceStockMap.put("stockRemark", "OVERSEAS DR/CR INVOICE NO");
			invoiceNoStockMap.put("stockType", "ODC");
		}else{
			if("AR".equals(invoice.getArAp())){
				invoiceStockMap.put("stockSign", "DAR");
				invoiceStockMap.put("stockRemark", "DOMESTIC AR INVOICE NO");
				invoiceNoStockMap.put("stockType", "DAR");
			}else{
				invoiceStockMap.put("stockSign", "DAP");
				invoiceStockMap.put("stockRemark", "DOMESTIC AP INVOICE NO");
				invoiceNoStockMap.put("stockType", "DAP");
			}
		}
		/** TB_COM_OBL관리번호(OBL_JOB_NO) */
		Map<String, Object> oblStockMap = new HashMap<String, Object>();
		/** 결제시 TB_COM_OBL조회 */
//		if(Util.null2str(invoice.getEcmOdChgYn()).equals("Y")) {
			oblStockMap.putAll(map);
			oblStockMap.put("stockType", "OBL");
			oblStockMap.put("stockSign", "CBL");
			oblStockMap.put("stockSeqLength", 5);
			oblStockMap.put("stockRemark", "OBL_JOB_NO");
			oblStockMap.put("solution", "ECM");
//		}
		
		map.put("invoiceNoCustomer", invoice.getInvoiceNoCustomer());
		map.put("accountCode", invoice.getAccountCode());
		map.put("localOversea", invoice.getLocalOversea());
		map.put("arAp", invoice.getArAp());
		map.put("srchDateFormat", invoice.getDateFormat());
		map.put("accountName", invoice.getAccountName());
		map.put("accountAttnId", invoice.getAccountAttnId());
		map.put("accountAttn", invoice.getAccountAttn());
		map.put("accountTel", invoice.getAccountTel());
		map.put("accountFax", invoice.getAccountFax());
		map.put("accountAttnEbillYn", invoice.getAccountAttnEbillYn());
		map.put("remarks", invoice.getRemarks());
		map.put("exrateDate", invoice.getInvoiceDate());
		map.put("ecmOdChgYn", invoice.getEcmOdChgYn());
		
		/** 환율조회 */
		Map<String, Object> exrateMap = new HashMap<String, Object>();
		if(invoice.getBoundType().equals("EXP")){
			exrateMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
			map.put("exrateType", "SELLING_CUSTOMS");
		}else{
			exrateMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
			map.put("exrateType", "BUYING_CUSTOMS");
		}
		exrateMap.put("srchEntCode", invoice.getEntCode());
		exrateMap.put("entBusinessCode", invoice.getEntBusinessCode());
		exrateMap.put("srchDate", invoice.getInvoiceDate());
		exrateMap.put("srchDateFormat", invoice.getDateFormat());
		Util.accessControlDatabase(exrateMap, "srchDateFormat|exrateType", false, null, "", null, "");
		exrateMap = commonMapper.selectExchangeRate(exrateMap);
		
		/** TB_COM_OD insert */
		map.put("comOdJobNo", commonMapper.selectNextStockMaxNo(odStockMap));
		
		/** TB_COM_OBL insert */
		/** 결제시 TB_COM_OBL조회 */
//		if(Util.null2str(invoice.getEcmOdChgYn()).equals("Y")) {
			map.put("oblJobNo", commonMapper.selectNextStockMaxNo(oblStockMap));
			map.put("ecmOdJobNo", ecmOdJobNo);
			Util.accessControlDatabase(map, "gmtTime", false, null, "", null, "");
			invoiceMapper.insertObl(map);
//		}
		map.put("obarDate", invoice.getObarDate());
		map.put("mblNo", invoice.getMblNo());
		map.put("hblNo", invoice.getHblNo());
		invoiceMapper.insertComOd(map);
		
		
		
		
		invoice.setInvoiceJobNo(commonMapper.selectNextStockMaxNo(invoiceStockMap));
		map.put("invoiceJobNo", invoice.getInvoiceJobNo());
		Util.accessControlDatabase(invoiceNoStockMap, "dateFormat|gmtTime", false, null, null, null, null);
		map.put("invoiceNo", commonMapper.selectNextStockNo(invoiceNoStockMap));
		map.put("invoiceDate", invoice.getInvoiceDate());

		String currencyCode = null;
		double sasAmountCur = 0,sasAmountVatCur = 0,sasAmountTotalCur = 0,sasAmountLoc = 0,sasAmountVatLoc = 0,sasAmountTotalLoc = 0,sasAmountTotalUsd = 0;
		
		int chargeSeqNo = 1;
		/** TB_COM_OD_CHG_INVOICE */
		if(!Util.null2str(invoice.getEcmOdChgYn()).equals("Y")){
			
			for(Map<String, Object> odCost : odCostList){
				double totalAmount = Util.null2dbl(odCost.get("TOTAL_AMOUNT"));
				double vatAmount = Util.null2dbl(odCost.get("VAT_AMOUNT"));
				double amount = 0, totalAmountLoc = 0, vatAmountLoc = 0, amountLoc = 0, exchangeRate = 0;
				currencyCode = Util.null2str(odCost.get("CURRENCY_CODE"));
				odCost.putAll(map);
				odCost.put("seqNoId", odCost.get("SEQ_NO_ID"));
				odCost.put("chargeCode", odCost.get("CHARGE_CODE"));
				odCost.put("chargeName", odCost.get("CHARGE_NAME"));
				odCost.put("currencyCodeCur", currencyCode);
				odCost.put("chargeSeqNo", Util.null2str(odCost.get("comOdJobNo")) + "-" + Util.lpad(Util.null2str(chargeSeqNo++), 4, '0'));
				odCost.put("vatPercent", 0);
				
				/** CUR금액 */
				if(vatAmount != 0){
					odCost.put("vatYnx", "Y");
					amount = totalAmount - vatAmount;
				}else{
					odCost.put("vatYnx", "N");
					amount = totalAmount;
				}
				odCost.put("amountCur", amount);
				odCost.put("amountVatCur", vatAmount);
				odCost.put("amountTotalCur", totalAmount);
				
				/** LOC(KRW)금액 */
				if(currencyCode.equals("KRW")){
					odCost.put("exchangeRate", "1");
					amountLoc = amount;
					vatAmountLoc = vatAmount;
					totalAmountLoc = totalAmount;
				}else if(exrateMap != null){
					exchangeRate = Util.null2dbl(exrateMap.get("EXCHANGE_RATE_" + currencyCode));
					odCost.put("exchangeRate", exchangeRate);
					amountLoc = amount * exchangeRate;
					vatAmountLoc = vatAmount * exchangeRate;
					totalAmountLoc = totalAmount * exchangeRate;
				}
				odCost.put("amountLoc", amountLoc);
				odCost.put("amountVatLoc", vatAmountLoc);
				odCost.put("amountTotalLoc", totalAmountLoc);
				
				/** USD금액 */
				if(exrateMap != null){
					exchangeRate = Util.null2dbl(exrateMap.get("EXCHANGE_RATE_USD"));
					if(exchangeRate != 0){
						odCost.put("exrateUsd", exchangeRate);
						odCost.put("amountUsd", amountLoc / exchangeRate);
						odCost.put("amountVatUsd", vatAmountLoc / exchangeRate);
						odCost.put("amountTotalUsd", totalAmountLoc / exchangeRate);
					}
				}
				if(odCost.get("amountUsd") == null){
					odCost.put("exrateUsd", 0);
					odCost.put("amountUsd", 0);
					odCost.put("amountVatUsd", 0);
					odCost.put("amountTotalUsd", 0);
				}
				
				sasAmountCur += amount;
				sasAmountVatCur += vatAmount;
				sasAmountTotalCur += totalAmount; 
				sasAmountLoc += amountLoc;
				sasAmountVatLoc += vatAmountLoc;
				sasAmountTotalLoc += totalAmountLoc;
				sasAmountTotalUsd += Util.null2dbl(odCost.get("amountTotalUsd"));
			}
			invoiceMapper.insertComOdChg(odCostList);
		}else {
			for(Map<String, Object> ecmOdChg : ecmOdChgList){
				double amount = 0, vatAmount = 0, totalAmount = 0, totalAmountLoc = 0, vatAmountLoc = 0, amountLoc = 0, exchangeRate = 0;
				currencyCode = Util.null2str(ecmOdChg.get("CURRENCY_CODE_CUR"));
				
				ecmOdChg.putAll(map);
				ecmOdChg.put("seqNoId", ecmOdChg.get("SEQ_NO_ID"));
				ecmOdChg.put("chargeSeqNo", Util.null2str(ecmOdChg.get("comOdJobNo")) + "-" + Util.lpad(Util.null2str(chargeSeqNo++), 4, '0'));
				ecmOdChg.put("chargeCode", ecmOdChg.get("CHARGE_CODE"));
				ecmOdChg.put("chargeName", ecmOdChg.get("CHARGE_NAME"));
				ecmOdChg.put("currencyCodeCur", currencyCode);
				ecmOdChg.put("vatPercent", ecmOdChg.get("VAT_PERCENT"));
				ecmOdChg.put("exchangeRate", Util.null2dbl(ecmOdChg.get("EXCHANGE_RATE")));
//				ecmOdChg.put("ecmOdChgYn", invoice.getEcmOdChgYn());
				
				/** CUR금액 */
				amount 		= Util.null2dbl(ecmOdChg.get("AMOUNT_CUR"));
				vatAmount 	= Util.null2dbl(ecmOdChg.get("AMOUNT_VAT_CUR"));
				totalAmount = Util.null2dbl(ecmOdChg.get("AMOUNT_TOTAL_CUR"));
				ecmOdChg.put("amountCur", amount);
				ecmOdChg.put("amountVatCur", vatAmount);
				ecmOdChg.put("amountTotalCur", totalAmount);
				if(vatAmount != 0)  ecmOdChg.put("vatYnx", "Y");
				else 				ecmOdChg.put("vatYnx", "N");
				
				/** LOC(KRW)금액 */
				amountLoc	   = Util.null2dbl(ecmOdChg.get("AMOUNT_LOC"));
				vatAmountLoc   = Util.null2dbl(ecmOdChg.get("AMOUNT_VAT_LOC"));
				totalAmountLoc = Util.null2dbl(ecmOdChg.get("AMOUNT_TOTAL_LOC"));
				ecmOdChg.put("amountLoc", amountLoc);
				ecmOdChg.put("amountVatLoc", vatAmountLoc);
				ecmOdChg.put("amountTotalLoc", totalAmountLoc);
				
				/** USD금액 */
				if(exrateMap != null){
					exchangeRate = Util.null2dbl(exrateMap.get("EXCHANGE_RATE_USD"));
					if(exchangeRate != 0){
						ecmOdChg.put("exrateUsd"	, exchangeRate);
						ecmOdChg.put("amountUsd"	, amountLoc / exchangeRate);
						ecmOdChg.put("amountVatUsd" , vatAmountLoc / exchangeRate);
						ecmOdChg.put("amountTotalUsd", totalAmountLoc / exchangeRate);
					}
				}
				if(ecmOdChg.get("amountUsd") == null){
					ecmOdChg.put("exrateUsd", 0);
					ecmOdChg.put("amountUsd", 0);
					ecmOdChg.put("amountVatUsd", 0);
					ecmOdChg.put("amountTotalUsd", 0);
				}
				
				sasAmountCur += amount;
				sasAmountVatCur += vatAmount;
				sasAmountTotalCur += totalAmount;
				sasAmountLoc += amountLoc;
				sasAmountVatLoc += vatAmountLoc;
				sasAmountTotalLoc += totalAmountLoc;
				sasAmountTotalUsd += Util.null2dbl(ecmOdChg.get("amountTotalUsd"));
			}
			invoiceMapper.insertComOdChg(ecmOdChgList);
			invoice.setInvoiceNo(Util.null2str(map.get("invoiceNo")));
			
			/** TB_ECM_OD_CHG_INVOICE update */
			invoiceMapper.updateEcmOdChg(invoice);
		}
		
		/** TB_SAS_INVOICE insert */
		if(!Util.null2str(invoice.getEcmOdChgYn()).equals("Y")) {
			map.put("currencyCodeCur", odCostList.get(0).get("currencyCodeCur"));
			map.put("exchangeRate", odCostList.get(0).get("exchangeRate"));
			map.put("exrateUsd", odCostList.get(0).get("exrateUsd"));
		}else {
			map.put("currencyCodeCur", ecmOdChgList.get(0).get("currencyCodeCur"));
			map.put("exchangeRate", ecmOdChgList.get(0).get("exchangeRate"));
			map.put("exrateUsd", ecmOdChgList.get(0).get("exrateUsd"));
		}
		map.put("amountCur", sasAmountCur);
		map.put("amountVatCur", sasAmountVatCur);
		map.put("amountTotalCur", sasAmountTotalCur);
		map.put("amountLoc", sasAmountLoc);
		map.put("amountVatLoc", sasAmountVatLoc);
		map.put("amountTotalLoc", sasAmountTotalLoc);
		map.put("amountTotalUsd", sasAmountTotalUsd);
		map.put("crAmountTotalCur", 0);
		map.put("crAmountTotalLoc", 0);
		map.put("drAmountTotalCur", 0);
		map.put("drAmountTotalLoc", 0);
		if("OVERSEA".equals(invoice.getLocalOversea())){
			if("AR".equals(invoice.getArAp())) {
				map.put("drAmountTotalCur", sasAmountTotalCur);
				map.put("drAmountTotalLoc", sasAmountTotalLoc);
			}else{
				map.put("crAmountTotalCur", sasAmountTotalCur);
				map.put("crAmountTotalLoc", sasAmountTotalLoc);
			}
		}
		map.put("currencyTotal", currencyCode + ":" + sasAmountTotalCur);
		Util.accessControlDatabase(map, "dateFormat|gmtTime", false, null, "", null, "");
		invoiceMapper.insertInvoice(map);
		
		/** TB_SAS_INVOICE_ITEM insert */
		invoiceMapper.insertInvoiceItem(map);
		/** TB_ECM_OD_COST update */
		if(!Util.null2str(invoice.getEcmOdChgYn()).equals("Y")) {
			if(invoice.getOdJobNoList() == null){
				invoice.getSearchInvoiceList().setInvoiceJobNo(invoice.getInvoiceJobNo());
				invoice.getSearchInvoiceList().setInvoiceDate(invoice.getInvoiceDate());
				Util.accessControlDatabase(invoice.getSearchInvoiceList(), "srchArAp|srchDateFormat", false, null, "", null, "");
				invoiceMapper.updateOdCost(invoice.getSearchInvoiceList());
			}else{
				Util.accessControlDatabase(invoice, "arAp|dateFormat", false, null, "", null, "");
				invoiceMapper.updateOdCostSelectGrid(invoice);
			}
		}
		
		/** TB_COM_OD_CHG_INVOICE 저장하려는 invoiceNo가 남아있는 데이터의 invoiceNo 제거 */
//		invoiceMapper.updateOdChgInvoiceNoNull(map);
		
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("invoiceJobNo", invoice.getInvoiceJobNo());
		orderSaveResult.setResultData(resultMap);
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		invoice.setOdJobNoList(null);
		processLogs.setJobFuncType("ADD");
		processLogs.setEntCode(invoice.getEntCode());
		processLogs.setEntBusinessCode(invoice.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(invoice.getBound());
		processLogs.setBoundType(invoice.getBoundType());
		processLogs.setProcessUserId(invoice.getRegisterUserId());
		processLogs.setJobNo(invoice.getInvoiceJobNo());
		processLogs.setLogJobType("DATA");
		processLogs.setDescription("CREATE ECM INVOICE[INVOICE_NO : " + Util.null2str(map.get("invoiceNo")) + "]");
		processLogs.setJobLog(new Gson().toJson(invoice));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(invoice.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(invoice.getLogUserName());
		processLogs.setProcessUserNameEng(invoice.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		return orderSaveResult;
	}
	/** 파트너는 통화단위별로 정산서 생성 */
	public OrderSaveResult insertInvoicePartner(Invoice invoice, HttpServletRequest request) throws Exception {
		Invoice logData = invoice.clone();
		logData.setOdJobNoList(null);
		
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		String ecmOdJobNo;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("entCode", invoice.getEntCode());
		map.put("entBusinessCode", invoice.getEntBusinessCode());
		
		List<Map<String, Object>> odCostList = null;
		
		if(invoice.getOdJobNoList() == null){
			Util.accessControlDatabase(invoice.getSearchInvoiceList(), "srchArAp", false, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO"
					+ "|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
			invoice.getSearchInvoiceList().setInvoiceType(invoice.getInvoiceType());
			odCostList = invoiceMapper.selectOdCostSum(invoice.getSearchInvoiceList());
		}else{
			Util.accessControlDatabase(invoice.getSearchInvoiceList(), "arAp", false, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO"
					+ "|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
			odCostList = invoiceMapper.selectOdCostSumSelectGrid(invoice);
		}
		if(odCostList == null || odCostList.size() == 0){
			orderSaveResult.setResultCode("A00163");	/** 정산처리할 수 있는 자료가 없습니다. */
			return orderSaveResult;
		}else{
			invoice.setObarDate(Util.null2str(odCostList.get(0).get("OBAR_DATE")));
			invoice.setMblNo(Util.null2str(odCostList.get(0).get("MBL_NO")));
			invoice.setHblNo(Util.null2str(odCostList.get(0).get("HBL_NO")));
		}
		ecmOdJobNo = Util.null2str(odCostList.get(0).get("OD_JOB_NO"));
		
		Util.accessControlDatabase(invoice, "gmtTime", false, null, null, null, null);
		map.put("invoiceType", invoice.getInvoiceType());
		map.put("gmtTime", invoice.getGmtTime());
		map.put("updateUserId", invoice.getRegisterUserId());
		map.put("stockSeqLength", 5);
		map.put("userId", invoice.getRegisterUserId());
		map.put("solution", "ECM");
		map.put("bound", invoice.getBound());
		map.put("boundType", invoice.getBoundType());
		map.put("dateFormat", invoice.getDateFormat());
		
		/** 정산서관리번호(INVOICE_JOB_NO) */
		Map<String, Object> invoiceStockMap = new HashMap<String, Object>();
		invoiceStockMap.putAll(map);
		invoiceStockMap.put("stockType", "ARP");
		invoiceStockMap.put("solution", "COM");
		/** 정산번호(INVOICE_NO) */
		Map<String, Object> invoiceNoStockMap = new HashMap<String, Object>();
		invoiceNoStockMap.putAll(map);
		invoiceNoStockMap.put("stockType", "ARP");
		invoiceNoStockMap.put("solution", "EMS");
		invoiceNoStockMap.put("statusSbi", "B");
		invoiceNoStockMap.put("portPrefix", "");
		invoiceNoStockMap.put("customerPrefix", "");
		invoiceNoStockMap.put("saveYn", "Y");
		invoiceNoStockMap.put("inputUserId", invoice.getRegisterUserId());
		invoiceNoStockMap.put("stockDate", invoice.getInvoiceDate());
		/** TB_COM_OD관리번호(OD_JOB_NO) */
		Map<String, Object> odStockMap = new HashMap<String, Object>();
		odStockMap.putAll(map);
		odStockMap.put("stockType", "OD");
		odStockMap.put("stockSign", "COD");
		odStockMap.put("stockRemark", "ORDER-JOB NO");
		
		invoiceStockMap.put("stockSign", "ODC");
		invoiceStockMap.put("stockRemark", "OVERSEAS DR/CR INVOICE NO");
		invoiceNoStockMap.put("stockType", "ODC");
		
		/** TB_COM_OBL관리번호(OBL_JOB_NO) */
		Map<String, Object> oblStockMap = new HashMap<String, Object>();
		oblStockMap.putAll(map);
		oblStockMap.put("stockType", "OBL");
		oblStockMap.put("stockSign", "CBL");
		oblStockMap.put("stockSeqLength", 5);
		oblStockMap.put("stockRemark", "OBL_JOB_NO");
		oblStockMap.put("solution", "ECM");
		
		map.put("invoiceNoCustomer", invoice.getInvoiceNoCustomer());
		map.put("accountCode", invoice.getAccountCode());
		map.put("localOversea", invoice.getLocalOversea());
		map.put("arAp", invoice.getArAp());
		map.put("srchDateFormat", invoice.getDateFormat());
		map.put("accountName", invoice.getAccountName());
		map.put("accountAttnId", invoice.getAccountAttnId());
		map.put("accountAttn", invoice.getAccountAttn());
		map.put("accountTel", invoice.getAccountTel());
		map.put("accountFax", invoice.getAccountFax());
		map.put("accountAttnEbillYn", invoice.getAccountAttnEbillYn());
		map.put("remarks", invoice.getRemarks());
		map.put("exrateDate", invoice.getInvoiceDate());
		map.put("ecmOdChgYn", invoice.getEcmOdChgYn());
		
		/** 환율조회 */
		Map<String, Object> exrateMap = new HashMap<String, Object>();
		if(invoice.getBoundType().equals("EXP")){
			exrateMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
			map.put("exrateType", "SELLING_CUSTOMS");
		}else{
			exrateMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
			map.put("exrateType", "BUYING_CUSTOMS");
		}
		exrateMap.put("srchEntCode", invoice.getEntCode());
		exrateMap.put("entBusinessCode", invoice.getEntBusinessCode());
		exrateMap.put("srchDate", invoice.getInvoiceDate());
		exrateMap.put("srchDateFormat", invoice.getDateFormat());
		Util.accessControlDatabase(exrateMap, "srchDateFormat|exrateType", false, null, "", null, "");
		exrateMap = commonMapper.selectExchangeRate(exrateMap);
		
		/** TB_COM_OD insert */
		map.put("comOdJobNo", commonMapper.selectNextStockMaxNo(odStockMap));
		
		/** TB_COM_OBL insert */
		map.put("oblJobNo", commonMapper.selectNextStockMaxNo(oblStockMap));
		map.put("ecmOdJobNo", ecmOdJobNo);
		Util.accessControlDatabase(map, "gmtTime", false, null, "", null, "");
		invoiceMapper.insertObl(map);
		
		map.put("obarDate", invoice.getObarDate());
		map.put("mblNo", invoice.getMblNo());
		map.put("hblNo", invoice.getHblNo());
		invoiceMapper.insertComOd(map);
		
		double sasAmountTotalCur = 0, sasAmountTotalLoc = 0, sasAmountTotalUsd = 0, exchangeRate = 0, exchangeRateUsd = 0, totalAmount = 0, totalAmountCur = 0, amountTotalUsd;
		
		int chargeSeqNo = 1;
		String currencyCode = Util.null2str(odCostList.get(0).get("CURRENCY_CODE"));
		Map<String, Object> invoiceMap = new HashMap<String, Object>(map);
		invoiceMap.put("invoiceDate", invoice.getInvoiceDate());
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setJobFuncType("ADD");
		processLogs.setEntCode(logData.getEntCode());
		processLogs.setEntBusinessCode(logData.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(logData.getBound());
		processLogs.setBoundType(logData.getBoundType());
		processLogs.setProcessUserId(logData.getRegisterUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobLog(new Gson().toJson(logData));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(logData.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(logData.getLogUserName());
		processLogs.setProcessUserNameEng(logData.getLogUserNameEng());
		
		List<Map<String, Object>> insertCostList = new ArrayList<Map<String, Object>>();
		
		
		invoiceStockMap.put("seqNo", chargeSeqNo);
		invoiceNoStockMap.put("seqNo", chargeSeqNo);
		invoice.setInvoiceJobNo(commonMapper.selectNextStockMaxNo(invoiceStockMap));
		invoiceMap.put("invoiceJobNo", invoice.getInvoiceJobNo());
		Util.accessControlDatabase(invoiceNoStockMap, "dateFormat|gmtTime", false, null, null, null, null);
		invoiceMap.put("invoiceNo", commonMapper.selectNextStockNo(invoiceNoStockMap));
		for(Map<String, Object> odCost : odCostList){
			if(!currencyCode.equals(odCost.get("CURRENCY_CODE"))){
				/** TB_COM_OD_CHG_INVOICE */
				invoiceMapper.insertComOdChg(insertCostList);
				insertCostList = new ArrayList<Map<String, Object>>();
				
				/** TB_SAS_INVOICE insert */
				invoiceMap.put("currencyCodeCur", currencyCode);
				invoiceMap.put("exchangeRate", exchangeRate);
				invoiceMap.put("exrateUsd", exchangeRateUsd);
				invoiceMap.put("amountCur", sasAmountTotalCur);
				invoiceMap.put("amountVatCur", 0);
				invoiceMap.put("amountTotalCur", sasAmountTotalCur);
				invoiceMap.put("amountLoc", sasAmountTotalLoc);
				invoiceMap.put("amountVatLoc", 0);
				invoiceMap.put("amountTotalLoc", sasAmountTotalLoc);
				invoiceMap.put("amountTotalUsd", sasAmountTotalUsd);
				invoiceMap.put("crAmountTotalCur", 0);
				invoiceMap.put("crAmountTotalLoc", 0);
				invoiceMap.put("drAmountTotalCur", 0);
				invoiceMap.put("drAmountTotalLoc", 0);
				invoiceMap.put("drAmountTotalCur", sasAmountTotalCur);
				invoiceMap.put("drAmountTotalLoc", sasAmountTotalLoc);
				invoiceMap.put("currencyTotal", currencyCode + ":" + sasAmountTotalCur);
				Util.accessControlDatabase(invoiceMap, "dateFormat|gmtTime", false, null, "", null, "");
				invoiceMapper.insertInvoice(invoiceMap);
				
				/** TB_SAS_INVOICE_ITEM insert */
				invoiceMapper.insertInvoiceItem(invoiceMap);
				
				/** TB_ECM_OD_COST update */
				invoice.getSearchInvoiceList().setSrchCurrencyCode(currencyCode);
				if(invoice.getOdJobNoList() == null){
					invoice.getSearchInvoiceList().setInvoiceJobNo(invoice.getInvoiceJobNo());
					invoice.getSearchInvoiceList().setInvoiceDate(invoice.getInvoiceDate());
					Util.accessControlDatabase(invoice.getSearchInvoiceList(), "srchArAp|srchDateFormat", false, null, "", null, "");
					invoiceMapper.updateOdCost(invoice.getSearchInvoiceList());
				}else{
					Util.accessControlDatabase(invoice, "arAp|dateFormat", false, null, "", null, "");
					invoiceMapper.updateOdCostSelectGrid(invoice);
				}
				
				processLogs.setDescription("CREATE ECM INVOICE[INVOICE_NO : " + Util.null2str(invoiceMap.get("invoiceNo")) + "]");
				processLogs.setJobNo(invoice.getInvoiceJobNo());
				commonService.insertLog(processLogs, request);
				
				sasAmountTotalCur = 0;sasAmountTotalLoc = 0;sasAmountTotalUsd = 0;
				
				
				invoiceStockMap.put("seqNo", chargeSeqNo);
				invoiceNoStockMap.put("seqNo", chargeSeqNo);
				invoice.setInvoiceJobNo(commonMapper.selectNextStockMaxNo(invoiceStockMap));
				invoiceMap.put("invoiceJobNo", invoice.getInvoiceJobNo());
				Util.accessControlDatabase(invoiceNoStockMap, "dateFormat|gmtTime", false, null, null, null, null);
				invoiceMap.put("invoiceNo", commonMapper.selectNextStockNo(invoiceNoStockMap));
			}
			
			totalAmount = Util.null2dbl(odCost.get("TOTAL_AMOUNT"));
			totalAmountCur = Util.null2dbl(odCost.get("TOTAL_AMOUNT_CUR"));
			currencyCode = Util.null2str(odCost.get("CURRENCY_CODE"));
			odCost.putAll(invoiceMap);
			odCost.put("chargeCode", odCost.get("CHARGE_CODE"));
			odCost.put("chargeName", odCost.get("CHARGE_NAME"));
			odCost.put("currencyCodeCur", currencyCode);
			odCost.put("chargeSeqNo", Util.null2str(odCost.get("comOdJobNo")) + "-" + Util.lpad(Util.null2str(chargeSeqNo++), 4, '0'));
			odCost.put("vatPercent", 0);
			
			/** CUR금액 */
			odCost.put("vatYnx", "N");
			odCost.put("amountCur", totalAmountCur);
			odCost.put("amountVatCur", 0);
			odCost.put("amountTotalCur", totalAmountCur);
			
			/** LOC(KRW)금액 */
			if(currencyCode.equals("KRW")){
				exchangeRate = 1;
			}else if(exrateMap != null){
				exchangeRate = Util.null2dbl(exrateMap.get("EXCHANGE_RATE_" + currencyCode));
			}
			odCost.put("exchangeRate", exchangeRate);
			
			odCost.put("amountLoc", totalAmount);
			odCost.put("amountVatLoc", 0);
			odCost.put("amountTotalLoc", totalAmount);
			
			/** USD금액 */
			exchangeRateUsd = 0;
			amountTotalUsd = 0;
			if(exrateMap != null){
				exchangeRateUsd = Util.null2dbl(exrateMap.get("EXCHANGE_RATE_USD"));
				if(exchangeRateUsd != 0){
					odCost.put("exrateUsd", exchangeRateUsd);
					odCost.put("amountUsd", totalAmount / exchangeRateUsd);
					odCost.put("amountVatUsd", 0);
					amountTotalUsd = totalAmount / exchangeRateUsd;
					odCost.put("amountTotalUsd", amountTotalUsd);
				}
			}
			if(odCost.get("amountUsd") == null){
				odCost.put("exrateUsd", 0);
				odCost.put("amountUsd", 0);
				odCost.put("amountVatUsd", 0);
				odCost.put("amountTotalUsd", 0);
			}
			insertCostList.add(odCost);
			
			sasAmountTotalCur += totalAmountCur; 
			sasAmountTotalLoc += totalAmount;
			sasAmountTotalUsd += amountTotalUsd;
		}
		/** TB_COM_OD_CHG_INVOICE */
		invoiceMapper.insertComOdChg(insertCostList);
		
		/** TB_SAS_INVOICE insert */
		invoiceMap.put("currencyCodeCur", currencyCode);
		invoiceMap.put("exchangeRate", exchangeRate);
		invoiceMap.put("exrateUsd", exchangeRateUsd);
		invoiceMap.put("amountCur", sasAmountTotalCur);
		invoiceMap.put("amountVatCur", 0);
		invoiceMap.put("amountTotalCur", sasAmountTotalCur);
		invoiceMap.put("amountLoc", sasAmountTotalLoc);
		invoiceMap.put("amountVatLoc", 0);
		invoiceMap.put("amountTotalLoc", sasAmountTotalLoc);
		invoiceMap.put("amountTotalUsd", sasAmountTotalUsd);
		invoiceMap.put("crAmountTotalCur", 0);
		invoiceMap.put("crAmountTotalLoc", 0);
		invoiceMap.put("drAmountTotalCur", 0);
		invoiceMap.put("drAmountTotalLoc", 0);
		invoiceMap.put("drAmountTotalCur", sasAmountTotalCur);
		invoiceMap.put("drAmountTotalLoc", sasAmountTotalLoc);
		invoiceMap.put("currencyTotal", currencyCode + ":" + sasAmountTotalCur);
		Util.accessControlDatabase(invoiceMap, "dateFormat|gmtTime", false, null, "", null, "");
		invoiceMapper.insertInvoice(invoiceMap);
		
		/** TB_SAS_INVOICE_ITEM insert */
		invoiceMapper.insertInvoiceItem(invoiceMap);
		
		/** TB_ECM_OD_COST update */
		invoice.getSearchInvoiceList().setSrchCurrencyCode(currencyCode);
		if(invoice.getOdJobNoList() == null){
			invoice.getSearchInvoiceList().setInvoiceJobNo(invoice.getInvoiceJobNo());
			invoice.getSearchInvoiceList().setInvoiceDate(invoice.getInvoiceDate());
			Util.accessControlDatabase(invoice.getSearchInvoiceList(), "srchArAp|srchDateFormat", false, null, "", null, "");
			invoiceMapper.updateOdCost(invoice.getSearchInvoiceList());
		}else{
			Util.accessControlDatabase(invoice, "arAp|dateFormat", false, null, "", null, "");
			invoiceMapper.updateOdCostSelectGrid(invoice);
		}
		
		processLogs.setDescription("CREATE ECM INVOICE[INVOICE_NO : " + Util.null2str(invoiceMap.get("invoiceNo")) + "]");
		processLogs.setJobNo(invoice.getInvoiceJobNo());
		commonService.insertLog(processLogs, request);
		
		
		
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("invoiceJobNo", invoice.getInvoiceJobNo());
		orderSaveResult.setResultData(resultMap);
		
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 작업료 요율계산
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult workFeeRateUpdate(WorkFeeRate rate, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Util.accessControlDatabase(rate, "gmtTime|dateFormat", false, null, "", null, "");
		
		SqlSession sqlSession = null;
		List<WorkFeeRate> treeList = null; // 하위업체 리스트
		int cnt = 1;
		boolean return0 = false;
		boolean saveData = false;
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setJobFuncType("ADD");
		processLogs.setEntCode(rate.getEntCode());
		processLogs.setEntBusinessCode(rate.getEntBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound("COM");
		processLogs.setBoundType("IMP");
		processLogs.setProcessUserId(rate.getUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(rate.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(rate.getLogUserName());
		processLogs.setProcessUserNameEng(rate.getLogUserNameEng());
		
		
		/** 환율조회 */
		Map<String, Object> exrateMap = new HashMap<String, Object>();
		Map<String, Object> exrateMapExp = new HashMap<String, Object>();
		exrateMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
		exrateMap.put("srchEntCode", rate.getEntCode());
		exrateMap.put("entBusinessCode", rate.getEntBusinessCode());
		exrateMap.put("srchDate", DateTime.getToday());
		exrateMap.put("srchDateFormat", "23");
		exrateMapExp = commonMapper.selectExchangeRate(exrateMap);
		
		exrateMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
		exrateMap = commonMapper.selectExchangeRate(exrateMap);
		double exchangeRate, amountCur, amount;
		long amountMax = 9999999999999L;
		
		List<Map<String, Object>> insertWorkFeeList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
		Map<String, Object> logMap = null;
		
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			InvoiceMapper invoiceBatchMapper = sqlSession.getMapper(InvoiceMapper.class);
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			
			List<String> custList = new ArrayList<String>();
			if(Util.isEmpty(rate.getCustomerCode())){
				tmpMap.put("entCode", rate.getEntCode());
				tmpMap.put("customerLevel", "1");
				for(Map<String,Object> custMap : commonMapper.selectCustomerInfoList(tmpMap)){
					custList.add(Util.null2str(custMap.get("CUSTOMER_CODE")));
				}
			}else{
				custList.add(rate.getCustomerCode());
				return0 = true;
			}
			
			for(String custCode : custList){
				rate.setCustomerCode(custCode);
				tmpMap.put("entCode", rate.getEntCode());
				tmpMap.put("customerCode", rate.getCustomerCode());
				Map<String, Object> custMap = commonMapper.selectCustomerInfo(tmpMap);
				rate.setCustomerLv(Util.null2str(custMap.get("CUSTOMER_LEVEL")));
				
				/** 정산여부 N인 상위업체일 경우 하위업체 작업료를 각각의 하위업체 요율로 계산 */
				if("1".equals(Util.null2str(custMap.get("CUSTOMER_LEVEL"))) && "N".equals(Util.null2str(custMap.get("ECM_INVOICE_YN")))) {
					treeList = new ArrayList<WorkFeeRate>();
					treeList.add(rate);
					treeList.addAll(invoiceMapper.selectCustomerTreeList(rate));
					for(WorkFeeRate tmpRate : treeList){
						tmpRate.setEntCode(rate.getEntCode());
						tmpRate.setEntBusinessCode(rate.getEntBusinessCode());
						tmpRate.setStartDate(rate.getStartDate());
						tmpRate.setEndDate(rate.getEndDate());
						tmpRate.setDateFormat(rate.getDateFormat());
						tmpRate.setEcmInvoiceYn(Util.null2str(custMap.get("ECM_INVOICE_YN")));
						tmpRate.setCustomerLowLv(String.valueOf(Util.null2int(tmpRate.getCustomerLv()) + 1));
						Util.accessControlDatabase(tmpRate, "customerLv|customerLowLv|dateFormat", false, null, "", null, "");
						List<Map<String, Object>> list = invoiceMapper.selectWorkFeeRate(tmpRate);
						saveData = false;
						for(Map<String, Object> map : list){
							if(map.get("INVOICE_JOB_NO") != null){
								continue;
							}
							if("EXP".equals(map.get("BOUND_TYPE"))){
								exchangeRate = Util.null2dbl(exrateMapExp.get("EXCHANGE_RATE_" + map.get("CURRENCY_CODE")));
							}else{
								exchangeRate = Util.null2dbl(exrateMap.get("EXCHANGE_RATE_" + map.get("CURRENCY_CODE")));
							}
							if(exchangeRate == 0){
								orderSaveResult.setResultCode("3");
								return orderSaveResult;
							}
							
							if("BL".equals(map.get("RATE_TYPE_APPLY"))){
								amountCur = Util.null2dbl(map.get("RATE_BASE"));	/** 기본단가 */
							}else{
								/** 기본단가 + 올림처리{ (오더중량 - 기본중량) / 증가단위 } * 증가금액 */
								amountCur = Util.null2dbl(map.get("RATE_BASE")) + Math.ceil((Util.null2dbl(map.get("WORK_WEIGHT")) - Util.null2dbl(map.get("WEIGHT_BASE"))) / Util.null2dbl(map.get("WEIGHT_INC"))) * Util.null2dbl(map.get("RATE_INC"));
							}
							if(amountCur > amountMax){
								amountCur = amountMax;
							}
							amount = Util.null2dbl(amountCur * exchangeRate, 0, "U");
							if(amount > amountMax){
								amount = amountMax;
							}
							
							map.put("amountCur", amountCur);
							map.put("amount", amount);
							map.put("userId", rate.getUserId());
							map.put("gmtTime", rate.getGmtTime());
							map.put("customerLv", tmpRate.getCustomerLv());
							insertWorkFeeList.add(new HashMap<String, Object>(map));
							saveData = true;
//							invoiceBatchMapper.insertWorkFee(map);
//							if( (cnt++ % 1000) == 0){
//								sqlSession.flushStatements();
//								sqlSession.clearCache();
//							}
						}
						
						if(saveData){
							logMap = new HashMap<String, Object>();
							logMap.put("jobNo", tmpRate.getCustomerCode());
							logMap.put("description", "CREATE ECM WORK FEE[CUSTOMER_CODE : " + tmpRate.getCustomerCode() +"]");
							logMap.put("jobLog", new Gson().toJson(tmpRate));
							logList.add(logMap);
							
//							processLogs.setJobNo(tmpRate.getCustomerCode());
//							processLogs.setDescription("CREATE ECM WORK FEE[CUSTOMER_CODE : " + tmpRate.getCustomerCode() +"]");
//							processLogs.setJobLog(new Gson().toJson(tmpRate));
//							commonService.insertLog(processLogs, request);
						}
					}
				/** 정산여부 Y인 상위업체 또는 하위업체 */
				}else if(!"".equals(Util.null2str(custMap.get("CUSTOMER_LEVEL")))) {
					Util.accessControlDatabase(rate, "customerLv|customerLowLv|dateFormat", false, null, "", null, "");
					List<Map<String, Object>> list = invoiceMapper.selectWorkFeeRate(rate);
					saveData = false;
					for(Map<String, Object> map : list){
						if(map.get("INVOICE_JOB_NO") != null){
							continue;
						}
						if("EXP".equals(map.get("BOUND_TYPE"))){
							exchangeRate = Util.null2dbl(exrateMapExp.get("EXCHANGE_RATE_" + map.get("CURRENCY_CODE")));
						}else{
							exchangeRate = Util.null2dbl(exrateMap.get("EXCHANGE_RATE_" + map.get("CURRENCY_CODE")));
						}
						if(exchangeRate == 0){
							orderSaveResult.setResultCode("3");
							return orderSaveResult;
						}
						
						if("BL".equals(map.get("RATE_TYPE_APPLY"))){
							amountCur = Util.null2dbl(map.get("RATE_BASE"));	/** 기본단가 */
						}else{
							/** 기본단가 + 올림처리{ (오더중량 - 기본중량) / 증가단위 } * 증가금액 */
							amountCur = Util.null2dbl(map.get("RATE_BASE")) + Math.ceil((Util.null2dbl(map.get("WORK_WEIGHT")) - Util.null2dbl(map.get("WEIGHT_BASE"))) / Util.null2dbl(map.get("WEIGHT_INC"))) * Util.null2dbl(map.get("RATE_INC"));
						}
						if(amountCur > amountMax){
							amountCur = amountMax;
						}
						amount = Util.null2dbl(amountCur * exchangeRate, 0, "U");
						if(amount > amountMax){
							amount = amountMax;
						}
						map.put("amountCur", amountCur);
						map.put("amount", amount);
						map.put("userId", rate.getUserId());
						map.put("gmtTime", rate.getGmtTime());
						map.put("customerLv", rate.getCustomerLv());
						insertWorkFeeList.add(new HashMap<String, Object>(map));
						saveData = true;
//						invoiceBatchMapper.insertWorkFee(map);
//						if( (cnt++ % 1000) == 0){
//							sqlSession.flushStatements();
//							sqlSession.clearCache();
//						}
					}
					
					if(saveData){
						logMap = new HashMap<String, Object>();
						logMap.put("jobNo", rate.getCustomerCode());
						logMap.put("description", "CREATE ECM WORK FEE[CUSTOMER_CODE : " + rate.getCustomerCode() +"]");
						logMap.put("jobLog", new Gson().toJson(rate));
						logList.add(logMap);
						
//						processLogs.setJobNo(rate.getCustomerCode());
//						processLogs.setDescription("CREATE ECM WORK FEE[CUSTOMER_CODE : " + rate.getCustomerCode() +"]");
//						processLogs.setJobLog(new Gson().toJson(rate));
//						commonService.insertLog(processLogs, request);
					}
				/** 상하위 업체가 매핑되지 않은 경우 */
				}else if(return0){
					orderSaveResult.setResultCode("0");
					return orderSaveResult;
				}
			}
			
			cnt = 1;
			for(Map<String, Object> map : insertWorkFeeList){
				invoiceBatchMapper.insertWorkFee(map);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			for(Map<String, Object> map : logList){
				processLogs.setJobNo(Util.null2str(map.get("jobNo")));
				processLogs.setDescription(Util.null2str(map.get("description")));
				processLogs.setJobLog(Util.null2str(map.get("jobLog")));
				commonService.insertLog(processLogs, request);
			}
			
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		if(logList.size() > 0){
			orderSaveResult.setResultCode("2");
		}else{
			orderSaveResult.setResultCode("1");
		}
		
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 작업료마감
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateWorkFeeClose(Invoice invoice, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		int updateCnt = 0;
		Util.getCryptographyYn(invoice.getSearchInvoiceList());
		Util.accessControlDatabase(invoice.getSearchInvoiceList(), "srchDateFormat|srchArAp", false, "srchDateGbn", "od.AR_DATE|cost.REGISTER_DATE_TIME|inv.INVOICE_DATE", "srchNoType", "od.PO_NO|od.HBL_NO"
				+ "|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM");
		if(invoice.getOdJobNoList() == null){
			updateCnt = invoiceMapper.updateWorkFeeClose(invoice.getSearchInvoiceList());
		}else{
			updateCnt = invoiceMapper.updateWorkFeeCloseSelectGrid(invoice);
		}
		if(updateCnt > 0){
			// 처리 로그 기록
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setJobFuncType("UPDATE");
			processLogs.setEntCode(invoice.getEntCode());
			processLogs.setEntBusinessCode(invoice.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(invoice.getBound());
			processLogs.setBoundType(invoice.getBoundType());
			processLogs.setProcessUserId(invoice.getRegisterUserId());
			processLogs.setJobNo("");
			processLogs.setLogJobType("DATA");
			processLogs.setDescription("ECM WORK FEE CLOSE");
			if(invoice.getOdJobNoList() != null) {	//검색
				processLogs.setJobLog(new Gson().toJson(invoice));
			}else {	//선택
				processLogs.setJobLog("");
			}
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(invoice.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(invoice.getLogUserName());
			processLogs.setProcessUserNameEng(invoice.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}
		
		return orderSaveResult;
	}
}