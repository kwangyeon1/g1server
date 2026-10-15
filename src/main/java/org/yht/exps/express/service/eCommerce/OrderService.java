package org.yht.exps.express.service.eCommerce;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.time.DateUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.api.SearchApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.elasticsearch.client.data.SortData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.api.InterfaceMapperVE;
import org.yht.exps.express.mapper.bms.BmsInMapper;
import org.yht.exps.express.mapper.consumerService.OrderCommonCsMapper;
import org.yht.exps.express.mapper.consumerService.OrderCustomerMapper;
import org.yht.exps.express.mapper.eCommerce.ConsolMapper;
import org.yht.exps.express.mapper.eCommerce.DeliveryMapper;
import org.yht.exps.express.mapper.eCommerce.HubnOrderMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.eCommerce.PcccMapper;
import org.yht.exps.express.mapper.edi.DeliveryChgMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.mapper.systemMng.SystemMngMapper;
import org.yht.exps.express.mapper.systemMng.SystemMngMapperVE;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList;
import org.yht.exps.express.model.eCommerce.delivery.UpdateDelivery;
import org.yht.exps.express.model.eCommerce.order.CustomsVerification;
import org.yht.exps.express.model.eCommerce.order.CustomsVerificationResult;
import org.yht.exps.express.model.eCommerce.order.CustomsVerificationResultList;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadOdChg;
import org.yht.exps.express.model.eCommerce.order.HblNo;
import org.yht.exps.express.model.eCommerce.order.HblNoList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderCSum;
import org.yht.exps.express.model.eCommerce.order.OrderCSumList;
import org.yht.exps.express.model.eCommerce.order.OrderCombinedTax;
import org.yht.exps.express.model.eCommerce.order.OrderCombinedTaxList;
import org.yht.exps.express.model.eCommerce.order.OrderCommon;
import org.yht.exps.express.model.eCommerce.order.OrderCommonList;
import org.yht.exps.express.model.eCommerce.order.OrderContainer;
import org.yht.exps.express.model.eCommerce.order.OrderCost;
import org.yht.exps.express.model.eCommerce.order.OrderCostList;
import org.yht.exps.express.model.eCommerce.order.OrderCostUpdate;
import org.yht.exps.express.model.eCommerce.order.OrderCs;
import org.yht.exps.express.model.eCommerce.order.OrderCsDelete;
import org.yht.exps.express.model.eCommerce.order.OrderCus;
import org.yht.exps.express.model.eCommerce.order.OrderCusList;
import org.yht.exps.express.model.eCommerce.order.OrderDashboard;
import org.yht.exps.express.model.eCommerce.order.OrderDay;
import org.yht.exps.express.model.eCommerce.order.OrderDayHouse;
import org.yht.exps.express.model.eCommerce.order.OrderDayHouseList;
import org.yht.exps.express.model.eCommerce.order.OrderDayList;
import org.yht.exps.express.model.eCommerce.order.OrderDelete;
import org.yht.exps.express.model.eCommerce.order.OrderDet;
import org.yht.exps.express.model.eCommerce.order.OrderDetList;
import org.yht.exps.express.model.eCommerce.order.OrderExclusion;
import org.yht.exps.express.model.eCommerce.order.OrderHold;
import org.yht.exps.express.model.eCommerce.order.OrderHouseDup;
import org.yht.exps.express.model.eCommerce.order.OrderHouseDupList;
import org.yht.exps.express.model.eCommerce.order.OrderItem;
import org.yht.exps.express.model.eCommerce.order.OrderLicense;
import org.yht.exps.express.model.eCommerce.order.OrderListDelete;
import org.yht.exps.express.model.eCommerce.order.OrderMasterDup;
import org.yht.exps.express.model.eCommerce.order.OrderMasterDupList;
import org.yht.exps.express.model.eCommerce.order.OrderMulti;
import org.yht.exps.express.model.eCommerce.order.OrderPacking;
import org.yht.exps.express.model.eCommerce.order.OrderPod;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.OrderSpecialItem;
import org.yht.exps.express.model.eCommerce.order.OrderSumHouse;
import org.yht.exps.express.model.eCommerce.order.OrderSumHouseList;
import org.yht.exps.express.model.eCommerce.order.OrderSumMaster;
import org.yht.exps.express.model.eCommerce.order.OrderSumMasterList;
import org.yht.exps.express.model.eCommerce.order.TrademarkInfringement;
import org.yht.exps.express.model.eCommerce.order.ZipCodeChange;
import org.yht.exps.express.model.eCommerce.order.search.SearchCustomsVerification;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrder;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderBatchUpdate;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderBatchUpdateCntr;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderCombinedTaxList;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderCostList;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderPodUpdate;
import org.yht.exps.express.model.eCommerce.order.search.SearchPrintOut;
import org.yht.exps.express.model.edi.search.SearchManifestCustoms;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.User;
import org.yht.exps.express.model.systemMng.DeliveryInterface;
import org.yht.exps.express.model.systemMng.QnAVE;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.OrderCommonService;
import org.yht.exps.express.service.api.InterfaceServiceVE;
import org.yht.exps.express.service.edi.EdiCommonService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.Converter;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.ExchangeKorEng;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;
import com.ubireport.viewer.report.preview.UbiViewer;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@Slf4j
@Service
@Transactional
public class OrderService {

	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name = "consolService")
	ConsolService consolService;
	@Resource(name = "fileService")
	FileService fileService;
	@Resource(name = "orderCommonService")
	OrderCommonService orderCommonService;
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	@Resource(name="deliveryServiceVE")
	DeliveryServiceVE deliveryServiceVE;

	@Resource(name = "trackingOrderDeliveryParser")
	TrackingOrderDeliveryParser trackingOrderDeliveryParser;
	//로컬,테스트,운영 구분
	@Value("${spring.profiles.active}")
	private String profile;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	BmsInMapper bmsInMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	ConsolMapper consolMapper;
	@Autowired
	SystemMngMapper systemMngMapper;
	@Autowired
	DeliveryChgMapper deliveryChgMapper;
	@Autowired
	InterfaceMapperVE interfaceMapperVE;
	@Autowired
	DeliveryMapper deliveryMapper;
	@Autowired
	OrderCustomerMapper orderCustomerMapper;
	@Autowired
    EnterpriseMapper enterpriseMapper;
	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	PcccMapper pcccMapper;
	@Autowired
	OrderCommonCsMapper orderCommonCsMapper;
	@Autowired
	SystemMngMapperVE systemMngMapperVE;
	@Autowired
    SmsUserService smsUserService;
    @Autowired
    HubnOrderMapper hubnOrderMapper;
	@Autowired
	ZipcodeConvert zipcodeConvert;
	@Autowired
	CustomProperties customProperties;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	StringRedisTemplate redisTemplate;
	@Autowired
	RestHighLevelClient restClient;
	
	@Value("${custom.elasticsearch-scroll-page}")
	private int elasticsearchScrollPage_;
	
	private Logger orderParamsLog = LoggerFactory.getLogger("orderParams");

 
	/**
	 * Order List(Detail)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderDetList selectOrderListDet(SearchOrderList search) throws Exception {
		String logVal = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> OrderController.selectOrderList!! " + search.getSrchEntCode() + " / "
			+ search.getLoginId() + "(" + search.getUserName() + ") / " + search.getSrchDateGbn() + " / " + search.getSrchStartDate() + " ~ " + search.getSrchEndDate();
		orderParamsLog.info(logVal);
		search.setProfileName(profile);
		
		OrderDetList orderDetList = new OrderDetList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		if(search.getSort() != null && search.getSort().size() > 0 && Util.null2str(search.getSort().get(0).getField()).equals("REMARKS")) search.getSort().get(0).setField("A.REMARKS");
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}
		

		if("AO".equals(search.getSrchBoundType()) || "SO".equals(search.getSrchBoundType())){
			if("Y".equals(search.getSrchRvsBoundType())){
				search.setSrchBoundType2("IMP");
			}else{
				search.setSrchBoundType2("EXP");
			}
		}else{
			if("Y".equals(search.getSrchRvsBoundType())){
				search.setSrchBoundType2("EXP");
			}else{
				search.setSrchBoundType2("IMP");
			}
		}
		
		if("HUBNK".equals(search.getSrchEntCode()) && "ALL".equals(search.getSrchBoundType())) {
			search.setSrchBoundType2(null);
		}
		
		Util.getCryptographyYn(search);
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchCusType", true, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE|od.REGISTER_DATE_TIME", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM"
				+ "|multi.DELIVERY_NO|od.RETURN_DELIVERY_NO|od.SHIPMENT_BOX_ID|ad.RETURN_REQUEST_ID");
		List<OrderDet> list  = orderMapper.selectOrderListDet(search);
		orderDetList.setOrderDetList(list);
		
		if(list.size() > 0) {
			orderDetList.setTotal(list.get(0).getTotal());
		}else {
			orderDetList.setTotal(0);
		}
		return orderDetList;
	}
	
	/**
	 * H.B/L No 리스트
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HblNoList selectHblNo(SearchOrderList search) throws Exception {
		HblNoList hblNoList = new HblNoList();
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

		Util.accessControlDatabase(search, null, true, null, null, "srchNoType", "od.HBL_NO|od.HSN");
		List<HblNo> list  = orderMapper.selectHblNoList(search);
		hblNoList.setHblNoList(list);
		if(list.size() > 0) {
			hblNoList.setTotal(list.get(0).getTotal());
		}else {
			hblNoList.setTotal(0);
		}
		return hblNoList;
	}
	
	/**
	 * Order List(일일보고)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderDayList selectOrderListDay(SearchOrderList search) throws Exception {
		OrderDayList orderDayList = new OrderDayList();
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
		Util.accessControlDatabase(search, "srchDateFormat|srchCusType", true, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		List<OrderDay> list  = orderMapper.selectOrderListDay(search);
		orderDayList.setOrderDayList(list);
		if(list.size() > 0) {
			orderDayList.setTotal(list.get(0).getTotal());
		}else {
			orderDayList.setTotal(0);
		}
		return orderDayList;
	}
	/**
	 * Order List(일일보고 하우스)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderDayHouseList selectOrderListDayHouse(SearchOrderList search) throws Exception {
		OrderDayHouseList orderDayHouseList = new OrderDayHouseList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchCusType", true, "srchDateGbn", "|noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "|od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		List<OrderDayHouse> list  = orderMapper.selectOrderListDayHouse(search);
		orderDayHouseList.setOrderDayHouseList(list);
		if(list.size() > 0) {
			orderDayHouseList.setTotal(list.get(0).getTotal());
		}else {
			orderDayHouseList.setTotal(0);
		}
		return orderDayHouseList;
	}
	
	/**
	 * Order List(Summary - Master)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSumMasterList selectOrderListSumMaster(SearchOrderList search) throws Exception {
		OrderSumMasterList orderSumMasterList = new OrderSumMasterList();
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
		Util.accessControlDatabase(search, "srchDateFormat|srchCusType", true, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		List<OrderSumMaster> list  = orderMapper.selectOrderListSumMaster(search);
		orderSumMasterList.setOrderSumMasterList(list);
		
		
		if(list.size() > 0) {
			orderSumMasterList.setTotal(list.get(0).getTotal());
		}else {
			orderSumMasterList.setTotal(0);
		}
		return orderSumMasterList;
	}
	
	/**
	 * Order List(Summary - House)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSumHouseList selectOrderListSumHouse(SearchOrderList search) throws Exception {
		OrderSumHouseList orderSumHouseList = new OrderSumHouseList();
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

		Util.accessControlDatabase(search, "srchDateFormat|srchCusType", true, "srchDateGbn", "|noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "|od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		List<OrderSumHouse> list  = orderMapper.selectOrderListSumHouse(search);
		orderSumHouseList.setOrderSumHouseList(list);
		
		if(list.size() > 0) {
			orderSumHouseList.setTotal(list.get(0).getTotal());
		}else {
			orderSumHouseList.setTotal(0);
		}
		return orderSumHouseList;
	}
	
	/**
	 * Order List(통관목록현황집계)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCSumList selectOrderListCSum(SearchOrderList search) throws Exception {
		OrderCSumList orderCSumList = new OrderCSumList();
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
		//
		
		List<OrderCSum> list  = orderMapper.selectOrderListCSum(search);
		orderCSumList.setOrderCSumList(list);
		
		
		if(list.size() > 0) {
			orderCSumList.setTotal(list.get(0).getTotal());
		}else {
			orderCSumList.setTotal(0);
		}
		return orderCSumList;
	}
	
	public OrderSaveResult insertClsFilterCodeExcel(String entCode, String updateUserId, String gmtTime, File file, String userBusinessCode, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		if(file != null) {
			
			String[] fieldName = null;
			String[] fieldTitle = null;
			String[] fieldLength = null;
			String[] fieldType = null;

			fieldName = 	new String[]{"code","etc1","etc2","etc3"}; // 컬럼명
			fieldTitle =	new String[]{"코드","판매자구분","전자상거래주소","거래유형"}; 		 // 타이틀
			fieldLength =	new String[]{"6","20","100","1"}; 	// 길이
			fieldType =		new String[]{"S","S","S","S"}; 		// 타입
			
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				Sheet sheet = wb.getSheetAt(0);
				Row row;
				int lastRow = sheet.getLastRowNum();
				DataFormatter df = new DataFormatter();
				
				int fieldSize = fieldName.length;
				String extension;
				String fieldValue = "";
				
				if(file.getName().indexOf(".xlsx") > -1){
					extension = "xlsx";
				}else{
					extension = "xls";
				}
				
				Map<String, Object> dataMap = null;
				List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
				StringBuffer errMsg = new StringBuffer();
				
				if("xls".equals(extension)) {
					HSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){ // 0은 헤더이므로 1부터 시작 
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							
							//필수값 체크
							if(j == 0) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[코드] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//데이터 유효성 체크
							if(j == 3) {
								if(!StringUtils.isEmpty(fieldValue) && !fieldValue.equals("A") && !fieldValue.equals("B") 
										&& !fieldValue.equals("C") && !fieldValue.equals("D") && !fieldValue.equals("Z")) {
									errMsg.append("[거래유형] 항목은 A, B, C, D, Z만 입력 가능합니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//중복체크
							if(j == 0) {
								Map<String, Object> checkMap = new HashMap<String, Object>();
								checkMap.put("entCode", entCode);
								checkMap.put("code", fieldValue);
								int cnt = orderMapper.selectClsFilterCodeDupCheck(checkMap);
								if(cnt > 0) {
									errMsg.append("입력하신 [코드]는 이미 사용중 입니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						dataMap.put("entCode", entCode);
						dataMap.put("updateUserId", updateUserId);
						dataMap.put("gmtTime", gmtTime);
						dataList.add(dataMap);
					}
				}else {
					XSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){ // 0은 헤더이므로 1부터 시작 
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							
							//필수값 체크
							if(j == 0) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[코드] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//데이터 유효성 체크
							if(j == 3) {
								if(!StringUtils.isEmpty(fieldValue) && !fieldValue.equals("A") && !fieldValue.equals("B") 
										&& !fieldValue.equals("C") && !fieldValue.equals("D") && !fieldValue.equals("Z")) {
									errMsg.append("[거래유형] 항목은 A, B, C, D, Z만 입력 가능합니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							//중복체크
							if(j == 0) {
								Map<String, Object> checkMap = new HashMap<String, Object>();
								checkMap.put("entCode", entCode);
								checkMap.put("code", fieldValue);
								int cnt = orderMapper.selectClsFilterCodeDupCheck(checkMap);
								if(cnt > 0) {
									errMsg.append("입력하신 [코드] 이미 사용중 입니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						dataMap.put("entCode", entCode);
						dataMap.put("updateUserId", updateUserId);
						dataMap.put("gmtTime", gmtTime);
						dataList.add(dataMap);
					}
				}
				
				if(errMsg.length() != 0){
					orderSaveResult.setResultCode("dataErr");
					Map<String, Object> errMap = new HashMap<String, Object>();
					errMap.put("errStr", errMsg);
					orderSaveResult.setResultData(errMap);
					return orderSaveResult;
				}
				
				if(dataList.size() > 0) {
					try {
						for(Map<String, Object> data : dataList) {
							orderMapper.insertClsFilterExcelUpload(data);
						}
					}catch(Exception e) {
						throw e;
					}finally{
						wb.close();
					}
				}
				
				String backupPath = customProperties.getBackupDir() + "excel/" + entCode + "/" + DateTime.getDate("yyyy-MM");
				File dir = new File(backupPath);
				if (dir.exists() == false)	dir.mkdirs();
				String backupFilePath = backupPath + "/" + file.getName();
				file.renameTo(new File(backupFilePath));
			}catch(Exception e) {
				throw e;
			}finally{
				if (is != null) is.close();
			}
		}else{
			orderSaveResult.setResultCode("ERROR");
		}
		
		return orderSaveResult;
	}
	
	/**
	 * Order List(고객상담관리)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCusList selectOrderListCus(SearchOrderList search) throws Exception {
		OrderCusList orderCusList = new OrderCusList();
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
		Util.accessControlDatabase(search, "srchDateFormat|srchCusType", true, "srchDateGbn", "noDateChk|cs.REGISTER_DATE_TIME|od.AR_DATE|od.OB_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		List<OrderCus> list  = orderMapper.selectOrderListCus(search);
		orderCusList.setOrderCusList(list);
		if(list.size() > 0) {
			orderCusList.setTotal(list.get(0).getTotal());
		}else {
			orderCusList.setTotal(0);
		}
		return orderCusList;
	}
	
	/**
	 * Order List(추가비용)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCostList selectOrderListCost(SearchOrderCostList search) throws Exception {
		OrderCostList orderCostList = new OrderCostList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, null, true, null, null, null, null);
		List<OrderCost> list  = orderMapper.selectOrderListCusCost(search);
		orderCostList.setOrderCostList(list);
		if(list.size() > 0) {
			orderCostList.setTotal(list.get(0).getTotal());
		}else {
			orderCostList.setTotal(0);
		}
		return orderCostList;
	}
	
	/**
	 * Order List(공통)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCommonList selectOrderListCommon(SearchOrderList search) throws Exception {
		OrderCommonList orderCommonList = new OrderCommonList();
		
		if(("BOUND").equals(search.getRequiredType()) && (search.getSrchBoundType() == null || search.getSrchBoundType() == "")){
			throw new Exception("매개변수 호출에 실패하였습니다.");
		}

		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		String srchBlTypeMh = Util.null2str(search.getSrchBlTypeMh());
		List<OrderCommon> list  = null;
		
		if(!Util.null2str(search.getNotUseMultiYn()).equals("Y")){
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
		}
		Util.accessControlDatabase(search, "srchDateFormat|srchCusType|userLanguage", true, "srchDateGbn", "|noDateChk|od.OB_DATE|od.AR_DATE|od.PO_DATE", "srchNoType", "|obl.MBL_NO|od.MBL_NO|od.HBL_NO|OD.HBL_NO|od.PO_NO");
		//컨테이너인 경우
		if("C".equals(srchBlTypeMh)) {
			search.setSrchBlTypeMh("H");
			list  = orderMapper.selectContainerListCommon(search);
		}else  if("MU".equals(srchBlTypeMh)) {
			search.setSrchBlTypeMh("H");
			list  = orderMapper.selectMultiListCommon(search);
		}else {
			if("O".equals(srchBlTypeMh)) {
				search.setSrchBlTypeMh("H");
			}
			list  = orderMapper.selectOrderListCommon(search);
			// 적하목록정정해상 > B/L추가 > 컨테이너 조회
			if("Y".equals(Util.null2str(search.getSrchContainerYn()))) {
				orderCommonList.setOrderCntrList(orderMapper.selectContainerListCommon(search));
			}
		}
		orderCommonList.setOrderCommonList(list);
		if(list.size() > 0) {
			orderCommonList.setTotal(list.get(0).getTotal());
		}else {
			orderCommonList.setTotal(0);
		}
		return orderCommonList;
	}
	
	/**
	 * 전자상거래 OrderList - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult deleteOrderList(OrderListDelete orderListDelete, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
//		ProcessLogs processLogs = new ProcessLogs();
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			SystemMngMapper systemMngBatchMapper = sqlSession.getMapper(SystemMngMapper.class);
			
			List<OrderDelete> orderDeleteList = orderListDelete.getOrderDeleteList();
			List<String> checkedPackingList = new ArrayList<String>();
			
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			/** 콘솔체크 */
			tmpMap.put("orderDeleteList", orderDeleteList);
			tmpMap.put("action", orderListDelete.getAction());
			tmpMap.put("chkInvoiceYn", "Y");
			if( "UPSXK".equals(orderListDelete.getEntCode()) ) { // || "YHTXX".equals(orderListDelete.getEntCode())
				checkedPackingList = orderBatchMapper.checkPackingRegisterIdAPI(tmpMap);
			}
			tmpMap = orderBatchMapper.selectOrderSaveChk(tmpMap);
			if(tmpMap != null && tmpMap.size() > 0){
				if(Util.null2int(tmpMap.get("CONSOL_YN_CNT")) > 0 && !"duplicateHbl".equals(orderListDelete.getAction())){
					orderSaveResult.setResultCode("A11215");	/** 이미 콘솔된 데이터가 존재합니다. */
					return orderSaveResult;
				}
				if(Util.null2int(tmpMap.get("EDI_SEND_CNT")) > 0){
					orderSaveResult.setResultCode("A11333");	/** 통관목록이 전송되어서 처리할 수 없습니다. */
					return orderSaveResult;
				}
				if(Util.null2int(tmpMap.get("INVOICE_CNT")) > 0){
					orderSaveResult.setResultCode("A11176");	/** 정산서가 발행된 건이 있어 변경할 수 없습니다. */
					return orderSaveResult;
				}
			}
			/** 오더삭제전 Packing 체크 */
			if( "UPSXK".equals(orderListDelete.getEntCode()) ) { // || "YHTXX".equals(orderListDelete.getEntCode())
				if(checkedPackingList != null && checkedPackingList.size() > 0) {
					tmpMap.put("checkedPackingList", checkedPackingList);
					tmpMap.put("entCode", orderListDelete.getEntCode());
					orderSaveResult.setResultData(tmpMap);
					return orderSaveResult;
				}
			}
			
			BulkData bulkData = null;
			String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
			Map<String, Object> logMap = null;
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", orderListDelete.getEntCode());
			tmpLogMap.put("ent_business_code", orderListDelete.getUserBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("bound", orderListDelete.getBound());
			tmpLogMap.put("bound_type", orderListDelete.getBoundType());
			tmpLogMap.put("process_user_id", orderListDelete.getUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "DELETE");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			
			tmpLogMap.put("ent_business_print_name", orderListDelete.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", orderListDelete.getLogUserName());
			tmpLogMap.put("process_user_name_eng", orderListDelete.getLogUserNameEng());
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
//			processLogs.setEntBusinessPrintName(orderListDelete.getLogUserBusinessPrintName());
//			processLogs.setProcessUserName(orderListDelete.getLogUserName());
//			processLogs.setProcessUserNameEng(orderListDelete.getLogUserNameEng());
			
			
			tmpMap.clear();
			tmpMap.put("entCode", orderListDelete.getEntCode());
			
			String bound = orderListDelete.getBound();
			String boundType = orderListDelete.getBoundType();
			
			int cnt = 1;
			int seq = 1;
			for(OrderDelete orderDelete : orderDeleteList) {
				orderBatchMapper.deleterOrder(orderDelete);
				
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", orderDelete.getOdJobNo());
				logMap.put("description", "ORDER DELETE [HBL_NO : "+orderDelete.getHblNo() +"]");
//				tmpLogMap.put("job_log", new Gson().toJson(orderDelete));
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
			}
			/** TB_ECM_OD_ADDITIONAL */
			for(OrderDelete orderDelete : orderDeleteList) {
				tmpMap.put("odJobNo", orderDelete.getOdJobNo());
				orderBatchMapper.deleterAdditional(tmpMap);
				cnt++;
			}
			/** TB_ECM_OD_ITEM */
			for(OrderDelete orderDelete : orderDeleteList) {
				tmpMap.put("odJobNo", orderDelete.getOdJobNo());
				orderBatchMapper.deleteItem(tmpMap);
				cnt++;
			}
			/** TB_ECM_OD_COST */
			for(OrderDelete orderDelete : orderDeleteList) {
				tmpMap.put("odJobNo", orderDelete.getOdJobNo());
				orderBatchMapper.deleteCost(tmpMap);
				cnt++;
			}
			/** TB_ECM_OD_POD */
			for(OrderDelete orderDelete : orderDeleteList) {
				tmpMap.put("odJobNo", orderDelete.getOdJobNo());
				orderBatchMapper.deletePod(tmpMap);
				cnt++;
			}
			if(cnt > 1000){
				sqlSession.flushStatements();
				sqlSession.clearCache();
				cnt = 1;
			}
			if(boundType.equals("IMP")){
				/** TB_ECM_OD_MULTI */
				for(OrderDelete orderDelete : orderDeleteList) {
					tmpMap.put("odJobNo", orderDelete.getOdJobNo());
					orderBatchMapper.deleteMulti(tmpMap);
					cnt++;
				}
				
				/** TB_ECM_OD_PACKING */
				for(OrderDelete orderDelete : orderDeleteList) {
					tmpMap.put("odJobNo", orderDelete.getOdJobNo());
					orderBatchMapper.deletePacking(tmpMap);
					cnt++;
				}
				
				/** TB_ECM_OD_PO_INFO */
				for(OrderDelete orderDelete : orderDeleteList) {
					tmpMap.put("odJobNo", orderDelete.getOdJobNo());
					orderBatchMapper.deletePoInfo(tmpMap);
					cnt++;
				}
				
				if(bound.equals("AIR")){
					/** TB_ECM_OD_SPECIAL_ITEM */
					for(OrderDelete orderDelete : orderDeleteList) {
						tmpMap.put("odJobNo", orderDelete.getOdJobNo());
						orderBatchMapper.deleteSpecialItem(tmpMap);
						cnt++;
					}
					/** TB_ECM_OD_ABNORMAL */
					for(OrderDelete orderDelete : orderDeleteList) {
						tmpMap.put("odJobNo", orderDelete.getOdJobNo());
						orderBatchMapper.deleteAbnormal(tmpMap);
						cnt++;
					}
				}
				
				/** TB_ECM_OD_EMS_INFO */
				for(OrderDelete orderDelete : orderDeleteList) {
					tmpMap.put("odJobNo", orderDelete.getOdJobNo());
					orderBatchMapper.deleteEmsInfo(tmpMap);
					cnt++;
				}
				
			}else{
				/** TB_ECM_OD_LICENSE */
				for(OrderDelete orderDelete : orderDeleteList) {
					tmpMap.put("odJobNo", orderDelete.getOdJobNo());
					orderBatchMapper.deleteLicense(tmpMap);
					cnt++;
				}
			}
			if(cnt > 1000){
				sqlSession.flushStatements();
				sqlSession.clearCache();
				cnt = 1;
			}
			if(bound.equals("SEA")){
				/** TB_ECM_OD_CONTAINER */
				for(OrderDelete orderDelete : orderDeleteList) {
					tmpMap.put("odJobNo", orderDelete.getOdJobNo());
					orderBatchMapper.deleteContainer(tmpMap);
					cnt++;
				}
			}
			/** TB_SMS_ALARM */
			for(OrderDelete orderDelete : orderDeleteList) {
				tmpMap.put("refJobNo", orderDelete.getOdJobNo());
				systemMngBatchMapper.deleteAlarm(tmpMap);
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
		
		// 처리 로그 기록
//		processLogs.setEntCode(orderListDelete.getEntCode());
//		processLogs.setEntBusinessCode(orderListDelete.getUserBusinessCode());
//		processLogs.setSolution("ECM");
//		processLogs.setBound(orderListDelete.getBound());
//		processLogs.setBoundType(orderListDelete.getBoundType());
//		processLogs.setProcessUserId(orderListDelete.getUserId());
////		processLogs.setJobNo(orderListDelete.getOdJobNo());
//		processLogs.setJobNo("");
//		processLogs.setLogJobType("DATA");
//		processLogs.setJobFuncType("DELETE");
//		processLogs.setDescription("ORDER DELETE");
////		processLogs.setJobLog(new Gson().toJson(orderListDelete));
//		processLogs.setJobLog("");
//		processLogs.setResult("ok");
//		processLogs.setProcessDateTime(System.currentTimeMillis());
//		commonService.insertLog(processLogs, request);
		
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 Order - insert/update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveOrder(Order order, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Order logOrder = order.clone();
		Util.accessControlDatabase(order, "dateFormat|gmtTime", false, null, null, null, null);
		String action = order.getAction();	/** insert/update구분 */
		String bound = order.getBound();
		String boundType = order.getBoundType();
		String oldMasterOdJobNo = "";
		String oldCdType = "";
		String oldSimpleDocNo = "";
		int oldMasterSimpleEdiSendCnt = 0;
		int oldMasterHblCnt = 0;
		String masterOdJobNo = Util.null2str(order.getMasterOdJobNo());
		String masterFinishYn = "";
		String masterConsolYn = "";
		String description = null;
		String clearanceViolationReason = "";
		String lastPodStatus = "";
		String lastPodDate = "";
		String chkRequest = "";
		String simpleEdiSendYn = "N";
		int masterMaxHsn = 0;
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> orderSaveMap = new HashMap<String, Object>();
		order.setProfileName(profile);
		//쿠팡확인용 커밋전 반품번호 확인로직
		if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")) {
			Map<String, Object> chkMap = new HashMap<>();
			chkMap.put("odJobNo", order.getOdJobNo());
			chkRequest = orderMapper.chkRequestId(chkMap);
		}
		
		if(action.equals("update")){
			tmpMap.put("odJobNo", order.getOdJobNo());
			tmpMap.put("masterOdJobNo", order.getMasterOdJobNo());
			tmpMap.put("checkClearanceViolationReason", "Y");
			tmpMap = orderMapper.selectOrderChk(tmpMap);
			order.setFinishYn(Util.null2str(tmpMap.get("FINISH_YN")));
			if("Y".equals(order.getFinishYn()) && !Util.null2str(tmpMap.get("DOCUMENT_YN")).equals(order.getDocumentYn())){	/** 마감된오더 서류여부 수정불가 */
				orderSaveResult.setResultCode("A11237");	/** 마감된 오더는 수정 할 수 없습니다. */
				tmpMap.clear();
				tmpMap.put("appendString", " - DOC");
				tmpMap.put("appendDirection", "R");
				orderSaveResult.setResultData(tmpMap);
				return orderSaveResult;
			}
			oldCdType = Util.null2str(tmpMap.get("CD_TYPE"));
			oldSimpleDocNo = Util.null2str(tmpMap.get("SIMPLE_DOC_NO"));
			clearanceViolationReason = Util.null2str(tmpMap.get("CLEARANCE_VIOLATION_REASON"));
			lastPodStatus = Util.null2str(tmpMap.get("LAST_POD_STATUS"));
			lastPodDate = Util.null2str(tmpMap.get("LAST_POD_DATE"));
			if("".equals(Util.null2str(order.getHsn()))){
				order.setHsn(Util.null2str(tmpMap.get("HSN")));
			}
			simpleEdiSendYn = Util.null2str(tmpMap.get("SIMPLE_EDI_SEND_YN"));
			oldMasterOdJobNo = Util.null2str(tmpMap.get("MASTER_OD_JOB_NO"));
//			oldMasterSimpleEdiSendCnt = Util.null2int(tmpMap.get("SIMPLE_EDI_SEND_CNT"));
			oldMasterHblCnt = Util.null2int(tmpMap.get("HBL_CNT"));
			orderSaveMap.put("CLEARANCE_STATUS", tmpMap.get("CLEARANCE_STATUS"));	/** 공통 - 통관상태 */
			orderSaveMap.put("CB_CODE", tmpMap.get("CB_CODE"));	/** 공통 - 관세사 */
			orderSaveMap.put("DROP_YN", tmpMap.get("DROP_YN")); /** 공통 - 취하여부 */
			orderSaveMap.put("chkEditOrder", "Y");
			orderSaveMap.put("chkEditOrderTypeEnt", "Y");
		}else{
			orderSaveMap.put("createOdJobNo", "Y");	/** 공통 - odJobNo생성 */
		}
		/** 마감된건 수정가능 */
//		if(!Util.isEmpty(order.getMasterOdJobNo()) || !Util.isEmpty(order.getOdJobNo())){
//			orderSaveMap.put("chkOrderClose", "Y");	/** 공통 - 마감체크 */
//		}
		tmpMap.clear();
		
		if(order.getBoundType().equals("IMP") && Util.isEmpty(order.getMasterOdJobNo())){
			orderSaveMap.put("setOrigCountry", "Y");
		}
		
		orderSaveMap.put("action", action);
		orderSaveMap.put("chkDupBlNo", "Y");	/** 공통 - 비엘번호중복체크 */
		
		if(Util.null2str(order.getCurrencyCode()).equals("USD")){
			order.setAmountUsd(order.getAmountCur());
		}else if(Util.null2dbl(order.getAmountLoc()) != 0 && !Util.null2str(order.getMblNo()).equals("")){
			orderSaveMap.put("exChangeYn", "Y");	/** 공통 - 환율처리 */
		}
		
		orderSaveMap.put("setAssignmentWhCode", "Y");
		
		if(!masterOdJobNo.equals("") && (!masterOdJobNo.equals(oldMasterOdJobNo) || (Util.null2str(order.getCdType()).equals("Y") && !oldCdType.equals("Y")))){
			tmpMap.put("masterOdJobNo", masterOdJobNo);
			tmpMap = orderMapper.selectMasterInfo(tmpMap);

			if(tmpMap != null && tmpMap.size() > 0){
				masterFinishYn = Util.null2str(tmpMap.get("FINISH_YN"));
				masterConsolYn = Util.null2str(tmpMap.get("CONSOL_YN"));
				masterMaxHsn = Util.null2int(tmpMap.get("MAX_HSN"));
			}else{
				order.setMblNo(null);
				order.setMasterOdJobNo(null);
				orderSaveResult.setResultCode("notMasterBlNo");
				return orderSaveResult;
			}
		}
		
		/** 통관목록체크 => 마감체크로 변경 */
//		if(!masterOdJobNo.equals("") && !masterOdJobNo.equals(oldMasterOdJobNo)){	/** 새로운 마스터와 콘솔되는경우 */
//			if(tmpMap != null && tmpMap.size() > 0){
//				if(Util.null2int(tmpMap.get("SIMPLE_EDI_SEND_CNT")) > 0){	/** 신규콘솔마스터의 통관목록신고여부 체크 */
//					orderSaveResult.setResultCode("A11325");	/** 통관목록신고후에는 콘솔/콘솔해제 할 수 없습니다. */
//					return orderSaveResult;
//				}
//			}
//		}
//		if(!oldMasterOdJobNo.equals("") && !masterOdJobNo.equals(oldMasterOdJobNo)){
//			if(oldMasterSimpleEdiSendCnt > 0){	/** 기존콘솔마스터의 통관목록신고여부 체크 */
//				orderSaveResult.setResultCode("A11325");	/** 통관목록신고후에는 콘솔/콘솔해제 할 수 없습니다. */
//				return orderSaveResult;
//			}
//		}
		
		if(!masterOdJobNo.equals("") && !masterOdJobNo.equals(oldMasterOdJobNo)){	/** 새로운 마스터와 콘솔되는경우 */
			if(tmpMap != null && tmpMap.size() > 0){
				if(Util.null2str(tmpMap.get("FINISH_YN")).equals("Y")){	/** 신규콘솔마스터 마감체크 */
					/** COPY(insert)이면서 통관구분이 일반(N)/배제(B)인 경우에만 마감체크 건너뜀. MBL변경 재콘솔(update)은 기존 로직 유지 */
					boolean skipFinishChkForCopy = "insert".equals(action)
							&& ("N".equals(Util.null2str(order.getCdType())) || "B".equals(Util.null2str(order.getCdType())));
					if(!skipFinishChkForCopy){
						orderSaveResult.setResultCode("A11237");	/** 마감된 오더는 수정 할 수 없습니다. */
						return orderSaveResult;
					}
				}
			}
		}
		if(!oldMasterOdJobNo.equals("") && !masterOdJobNo.equals(oldMasterOdJobNo)){
			if(Util.null2str(order.getFinishYn()).equals("Y")){	/** 기존콘솔마스터 마감체크 */
				orderSaveResult.setResultCode("A11237");	/** 마감된 오더는 수정 할 수 없습니다. */
				return orderSaveResult;
			}
		}
		
		/** 2025-06-12 통관목록전송건 통관구분 수정불가 */
		if(!"AD".equals(order.getUserAuth()) && "Y".equals(simpleEdiSendYn) && "Y".equals(oldCdType) && !"Y".equals(order.getCdType())){
			orderSaveResult.setResultCode("A11333");	/** 통관목록이 전송되어서 처리할 수 없습니다. */
			return orderSaveResult;
		}
		
		/** 마감오더 콘솔/콘솔해제시 HSN업데이트 */
//		if(masterOdJobNo.equals("") || (!masterOdJobNo.equals("") && !masterOdJobNo.equals(oldMasterOdJobNo) && !masterFinishYn.equals("Y"))){
//			order.setHsn(null);
//			order.setFinishYn("N");
//		}else if(!masterOdJobNo.equals("") && !masterOdJobNo.equals(oldMasterOdJobNo) && masterFinishYn.equals("Y")){
//			if(masterMaxHsn + 1 > 9999){
//				orderSaveResult.setResultCode("A11287");	/** 사용가능한 HSN이 없습니다. */
//				return orderSaveResult;
//			}
//			order.setHsn(Util.lpad(Util.null2str(masterMaxHsn + 1), 4, '0'));
//			order.setFinishYn("Y");
//		}
		
		if(!orderCommonService.orderSave(order, "entOrder", orderSaveMap, 1)){
			/** 통관목록신고할때 체크하도록 수정 */
//			if(orderSaveMap.get("busEdiEcmNull") != null){	/** 특송업체부호 미설정 */
//				orderSaveResult.setResultCode("busEdiEcmNull");
//			}else 
			if(orderSaveMap.get("duplicateBlNo") != null){	/** 비엘번호중복 */
				orderSaveResult.setResultCode("duplicateBlNo");
			}else if(orderSaveMap.get("closeOrder") != null){	/** 마감된오더 */
				orderSaveResult.setResultCode("A11237");	/** 마감된 오더는 수정 할 수 없습니다. */
			}else if(orderSaveMap.get("resultCode") != null){
				orderSaveResult.setResultCode(Util.null2str(orderSaveMap.get("resultCode")));
			}
			return orderSaveResult;
		}
		
		/** 미통관사유값 유무에 따라 ECM251(미통관)POD 저장/삭제 */
		String inputLastPodStatus = null;
		boolean insertEcm251Pod = false;
		boolean deleteEcm251Pod = false;
		/** COPY(insert) + 하우스(H) + 마스터 콘솔 + 통관구분 일반(N)/배제(B)인 경우 진행상태를 원본 하우스의 진행상태로 따라감 */
		boolean copyConsolClose = action.equals("insert") && "H".equals(Util.null2str(order.getBlTypeMh()))
				&& !masterOdJobNo.equals("")
				&& ("N".equals(Util.null2str(order.getCdType())) || "B".equals(Util.null2str(order.getCdType())))
				&& !Util.isEmpty(order.getCopyLastPodStatus());
		if(action.equals("insert")){
			if(!Util.isEmpty(order.getClearanceViolationReason())){
				inputLastPodStatus = "ECM251";
				insertEcm251Pod = true;
			}else if(copyConsolClose){
				inputLastPodStatus = order.getCopyLastPodStatus();	/** 원본 하우스 진행상태 따라감 */
			}else{
				inputLastPodStatus = "ECM010";
			}
		}else{
			if(!Util.isEmpty(order.getClearanceViolationReason()) && !clearanceViolationReason.equals(order.getClearanceViolationReason())){
				insertEcm251Pod = true;
				if(Util.isEmpty(lastPodStatus) || Util.null2int(lastPodStatus.substring(3)) <= 251){
					inputLastPodStatus = "ECM251";
					lastPodDate = null;
				}
			}else if(Util.isEmpty(order.getClearanceViolationReason()) && !Util.isEmpty(clearanceViolationReason)){
				deleteEcm251Pod = true;
				inputLastPodStatus = lastPodStatus;
			}
		}
		order.setLastPodStatus(inputLastPodStatus);
		order.setLastPodDate(lastPodDate);
		
		ProcessLogs processLogs = new ProcessLogs();
		tmpMap.clear();
		tmpMap.put("entCode", order.getEntCode());
		tmpMap.put("entBusinessCode", "");
		tmpMap.put("gmtTime", order.getGmtTime());
		tmpMap.put("solution", "ECM");
		tmpMap.put("updateUserId", order.getUpdateUserId());
		tmpMap.put("stockType", "POD");
		tmpMap.put("stockSign", "POD");
		tmpMap.put("stockSeqLength", 8);
		tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		tmpMap.put("stockRemark", "ECM POD JOB NO");
		tmpMap.put("odJobNo", order.getOdJobNo());
		/** COPY(insert) + 마스터 콘솔 + 일반(N)/배제(B)인 경우 POD도 원본 하우스 진행상태로 기록 */
		tmpMap.put("podStatus", copyConsolClose ? order.getCopyLastPodStatus() : "ECM010");
		tmpMap.put("blNo", order.getHblNo());
		tmpMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
		tmpMap.put("podTableName", 	"TB_ECM_POD_01");
		if(action.equals("insert")){
			orderMapper.insertOrder(order);
			orderMapper.insertAdditional(order);
			orderMapper.insertOrderPod(tmpMap);
			if("Y".equals(order.getEmsYn())) {
				orderMapper.insertOrderEmsInfo(order);
			}
			processLogs.setJobFuncType("ADD");
			description = "NEW ORDER";
		}else if(action.equals("update")){
			/** 2025-05-27 통관구분이 목록으로 변경되었을때 하우스의 제출번호가 이미 통관목록 전송되었다면 제출번호 제거 */
			if("IMP".equals(boundType) && !"Y".equals(oldCdType) && "Y".equals(order.getCdType()) && !Util.isEmpty(order.getSimpleDocNo())){
				orderSaveMap.clear();
				orderSaveMap.put("simpleDocNo", order.getSimpleDocNo());
				orderSaveMap.put("srchType", "sendCnt");
				if(commonMapper.selectSimpleDocNoCount(orderSaveMap) > 0){
					order.setSimpleDocNo(null);
				}
			}
			orderMapper.updateOrder(order);
			orderMapper.updateAdditional(order);
			
			if("Y".equals(order.getEmsYn())) {
				orderMapper.updateOrderEmsInfo(order);
			}
			
			processLogs.setJobFuncType("UPDATE");
			description = "EDIT ORDER";
		}
		
		
		
		if(insertEcm251Pod || deleteEcm251Pod){
			tmpMap.put("podTableName", "TB_ECM_POD_03");
			tmpMap.put("podStatus", "ECM251");
			if(!action.equals("insert")){
				orderMapper.deleteOrderPod(tmpMap);
			}
			if(insertEcm251Pod){
				tmpMap.put("remarks", order.getClearanceViolationReason());
				tmpMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00002");
				orderMapper.insertOrderPod(tmpMap);
			}
		}
		
		/** 제출번호 제거시 마스터의 제출번호와 같은 제출번호의 하우스가 없을경우 마스터 제출번호 제거 */
		if(action.equals("update") && !oldSimpleDocNo.equals("") && Util.null2str(order.getSimpleDocNo()).equals("")){
			tmpMap.clear();
			tmpMap.put("masterOdJobNo", oldMasterOdJobNo);
			tmpMap.put("simpleDocNo", oldSimpleDocNo);
			tmpMap = orderMapper.selectSimpleDocNoCnt(tmpMap);
			if(tmpMap != null && tmpMap.size() > 0 && Util.null2int(tmpMap.get("HOUSE_COUNT")) == 0){
				orderMapper.updateSimpleDocNoNull(tmpMap);
			}
		}
		
		/** 서브테이블 */
		if(tmpMap == null) tmpMap = new HashMap<String, Object>();
		else tmpMap.clear();
		tmpMap.put("entCode", order.getEntCode());
		tmpMap.put("odJobNo", order.getOdJobNo());
		
		if(action.equals("update")){
			/** 서브테이블 delete */
			orderMapper.deleteItem(tmpMap);
//			orderMapper.deleteCost(tmpMap);
			
			if(boundType.equals("IMP")){
				orderMapper.deleteMulti(tmpMap);
				orderMapper.deletePacking(tmpMap);
				orderMapper.deletePoInfo(tmpMap);
				if(bound.equals("AIR")){
					orderMapper.deleteSpecialItem(tmpMap);
//					orderMapper.deleteAbnormal(tmpMap);
				}
			}else{
				orderMapper.deleteLicense(tmpMap);
			}
			if(bound.equals("SEA")){
				orderMapper.deleteContainer(tmpMap);
			}
			
			tmpMap.put("hblNo", order.getHblNo());
			orderMapper.updatePodBlNo(tmpMap);
		}
		
		/** 서브테이블 insert */
		tmpMap.put("registerUserId", order.getRegisterUserId());
		tmpMap.put("dateFormat", order.getDateFormat());
		tmpMap.put("gmtTime", order.getGmtTime());
		tmpMap.put("profileName", profile);
		
		/** 세부품목 */
		List<OrderItem> itemList = order.getItemList();
		if(itemList != null && itemList.size() > 0) {
//			tmpMap.put("itemList", itemList);
//			orderMapper.insertItem(tmpMap);
//			tmpMap.remove("itemList");
			
			/** 세부품목 100건 넘게 입력하는 경우 발생해서 수정 */
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
				int seqNoId = 1;
				
				DateTimeFormatter inputFormatter  = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
				for(OrderItem orderItem : itemList){
					orderItem.setOdJobNo(order.getOdJobNo());
					orderItem.setSeqNoId(seqNoId++);
					
					if("IMP".equals(order.getBoundType())){
                        if(!"".equals(Util.null2str(orderItem.getOrderEndDatetime()))) {
                            String formatted = LocalDateTime.parse(orderItem.getOrderEndDatetime(), inputFormatter)
                                    .format(outputFormatter);
                            orderItem.setOrderEndDatetime(formatted);
                        }else{
                            orderItem.setOrderEndDatetime(null);
                        }
                    }
					orderItem.setProfileName(profile);
					orderBatchMapper.insertItemBatch(orderItem);
				}

				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
			}catch(Exception e) {
				throw e;
			}finally{
				sqlSession.close();
			}
		}
		
		
		if(action.equals("insert")){	/** Copy로 신규입력시 저장 */
			/** 추가비용화물 */
			List<OrderCost> costList = order.getCostList();
			if(costList != null && costList.size() > 0) {
				tmpMap.put("costList", costList);
				orderMapper.insertCost(tmpMap);
				tmpMap.remove("costList");
			}
		}
		
		if(boundType.equals("IMP")){
			/** 멀티(중량)화물 */
			List<OrderMulti> multiList = order.getMultiList();
			if(multiList != null && multiList.size() > 0) {
				tmpMap.put("multiList", multiList);
				orderMapper.insertMulti(tmpMap);
				//전자상거래 Delivery 반품번호 변경
				for(OrderMulti orderMulti : multiList) {
					int seqNo = 1;
					if(!"".equals(Util.null2str(orderMulti.getReturnRequestId())) && "".equals(Util.null2str(orderMulti.getReturnRegDate()))) {
						Map<String, Object> returnMap = new HashMap<String, Object>();
						returnMap.put("odJobNo",Util.null2str(orderMulti.getOdJobNo()));
						returnMap.put("returnRequestId",Util.null2str(orderMulti.getReturnRequestId()));
						returnMap.put("returnDeliveryCode",Util.null2str(orderMulti.getReturnDeliveryNo()));
						returnMap.put("multiDeliveryNo",Util.null2str(orderMulti.getDeliveryNo()));
						returnMap.put("seqNoId",Util.null2str(seqNo));
						returnMap.put("entCode",Util.null2str(order.getEntCode()));
						deliveryServiceVE.changeReturnNo(returnMap);
					}
					seqNo++;
				}
				
				tmpMap.remove("multiList");
			}
			
			/** PACKING */
			List<OrderPacking> packingList = order.getPackingList();
			if(packingList != null && packingList.size() > 0) {
				tmpMap.put("packingList",packingList);
				orderMapper.insertPacking(tmpMap);
				tmpMap.remove("packingList");
			}

			/** 오더 PACKING의 오더관리번호 업데이트 */
			if("UPSXK".equals(order.getEntCode())) { // || "YHTXX".equals(order.getEntCode())
				orderMapper.updatePackingOdJobNo(order);
			}
			
			if(bound.equals("AIR")){
				/** 별도관리물품상세내역 */
				List<OrderSpecialItem> specialItemList = order.getSpecialItemList();
				if(specialItemList != null && specialItemList.size() > 0) {
					tmpMap.put("specialItemList", specialItemList);
					orderMapper.insertSpecialItem(tmpMap);
					tmpMap.remove("specialItemList");
				}
				/** 하기이상결과보고 */
//				List<OrderAbnormal> abnormalList = order.getAbnormalList();
//				if(abnormalList != null && abnormalList.size() > 0) {
//					tmpMap.put("abnormalList", abnormalList);
//					orderMapper.insertAbnormal(tmpMap);
//					tmpMap.remove("abnormalList");
//				}
			}
			List<String> poInfoList = order.getPoInfoList();
			if(poInfoList != null && poInfoList.size() > 0) {
				tmpMap.put("poInfoList", poInfoList);
				orderMapper.insertPoInfo(tmpMap);
				tmpMap.remove("poInfoList");
			}
			
			if("YHTXX".equals(order.getEntCode()) || "UPSXK".equals(order.getEntCode())){
				orderMapper.updatePackingOdJobNo(order);
				commonMapper.updatePackingErrorYn(tmpMap);
//				int packingCnt = 0;
//				String hblNo = order.getHblNo().trim().replace("\t", "");
//				Map<String, Object> packingMap = new HashMap<String, Object>();
//				packingMap.put("odJobNo", order.getOdJobNo());
//				if(Util.null2int(orderMapper.updatePackingOdJobNo(order)) > 0){
//					packingMap.put("hblNo", hblNo);
//					packingCnt = orderMapper.selectPackingCount(packingMap);
//				}else{
//					String packingNo;
//					for(OrderPacking packing : packingList){
//						packingNo = Util.null2str(packing.getPackingNo());
//						if(packingNo.length() == 18 || packingNo.equals(hblNo)){
//							packingCnt++;
//						}
//					}
//				}
//				
//				if(packingCnt != Util.null2int(order.getPieces())){
//					packingMap.put("packingErrorYn", "Y");
//				}else{
//					packingMap.put("packingErrorYn", "N");
//				}
//				orderMapper.updatePackingErrorYn(packingMap);
			}
		}else {
			/** 면장정보 */
			List<OrderLicense> licenseList = order.getLicenseList();
			if(licenseList != null && licenseList.size() > 0) {
				tmpMap.put("licenseList", licenseList);
				tmpMap.put("gmtTime", order.getGmtTime());
				tmpMap.put("registerUserId", order.getRegisterUserId());
				tmpMap.put("updateUserId", order.getUpdateUserId());
				orderMapper.insertLicense(tmpMap);
				tmpMap.remove("licenseList");
			}
		}
		if(bound.equals("SEA")){
			/** 컨테이너정보 */
			List<OrderContainer> containerList = order.getContainerList();
			if(containerList != null && containerList.size() > 0) {
				tmpMap.put("containerList", containerList);
				orderMapper.insertContainer(tmpMap);
				tmpMap.remove("containerList");
			}
		}
		
		
		// 처리 로그 기록
		processLogs.setEntCode(order.getEntCode());
		processLogs.setEntBusinessCode(order.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(order.getBound());
		processLogs.setBoundType(order.getBoundType());
		processLogs.setProcessUserId(order.getUpdateUserId());
		processLogs.setJobNo(order.getOdJobNo());
		processLogs.setLogJobType("DATA");
		processLogs.setDescription(description +"[HBL_NO : " + order.getHblNo()+"]");
		processLogs.setJobLog(new Gson().toJson(logOrder));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(order.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(order.getLogUserName());
		processLogs.setProcessUserNameEng(order.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		orderSaveResult.setOdJobNo(order.getOdJobNo());
		
		if(action.equals("update") && !oldCdType.equals(order.getCdType())){
			Map<String, Object> updateCdTypeMap = new HashMap<String, Object>();
			updateCdTypeMap.put("odJobNo", order.getOdJobNo());
			updateCdTypeMap.put("cdType", order.getCdType());
			commonMapper.updateBmsCdType(updateCdTypeMap);
			
			tmpMap.put("cdType", order.getCdType());
			commonService.insertBmsCargoProgress(tmpMap);
		}
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 OrderCost - update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateCost(OrderCostUpdate orderCostUpdate, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		if(Util.null2str(orderCostUpdate.getOdJobNo()).equals("") || Util.null2str(orderCostUpdate.getUpdateType()).equals("")){
			orderSaveResult.setResultCode("A00001");
			return orderSaveResult;
		}
		
		Util.accessControlDatabase(orderCostUpdate, "dateFormat|gmtTime", false, null, null, null, null);
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		List<OrderCost> invoiceCheckList = new ArrayList<OrderCost>();
		List<OrderCost> orderCostDelList = orderCostUpdate.getOrderCostDelList();	/** 그리드에서 삭제한 추가비용 */
		List<OrderCost> costList = orderCostUpdate.getOrderCostList();				/** 그리드 추가비용 */
		boolean changeFlag = false;
		
		for(OrderCost cost : orderCostDelList){
			invoiceCheckList.add(cost);
		}
		
		for(OrderCost cost : costList){
			if("U".equals(cost.getMode()) || "I".equals(cost.getMode())){
				changeFlag = true;
				if("U".equals(cost.getMode())){
					invoiceCheckList.add(cost);
				}
			}
		}
		
		if(invoiceCheckList.size() > 0){
			tmpMap.put("arAp", orderCostUpdate.getArAp());
			tmpMap.put("invoiceCheckList", invoiceCheckList);
			if(orderMapper.selectInvoiceConfirmCheck(tmpMap) > 0){
				orderSaveResult.setResultCode("A11176");	/** 정산서가 발행된 건이 있어 변경할 수 없습니다. */
				return orderSaveResult;
			}
		}
		
		tmpMap.clear();
		tmpMap.put("odJobNo", orderCostUpdate.getOdJobNo());
		tmpMap.put("updateType", orderCostUpdate.getUpdateType());
		tmpMap.put("entCode", orderCostUpdate.getEntCode());
		tmpMap.put("gmtTime", orderCostUpdate.getGmtTime());
		tmpMap.put("dateFormat", orderCostUpdate.getDateFormat());
		tmpMap.put("registerUserId", orderCostUpdate.getUpdateUserId());
		if(orderCostDelList.size() > 0){
			tmpMap.put("orderCostDelList", orderCostDelList);
			orderMapper.deleteCost(tmpMap);
		}
		
		if(changeFlag) {
			tmpMap.put("costList", costList);
			orderMapper.updateCost(tmpMap);
		}
		
		if(orderCostDelList.size() > 0 || changeFlag){
			double tariff = 0;
			double deliveryFee = 0;
			String clearancePaymentType = "";
			String deliveryPaymentType = "";
			String cbCode = "";
			String deliveryCode = "";
			
			String chgType;
			for(OrderCost cost : costList){
				chgType = Util.null2str(cost.getChgType());
				if(chgType.equals("BRK")){
					tariff += cost.getArAmount();
					if(clearancePaymentType.equals("") && !Util.null2str(cost.getPaymentGubn()).equals("")){
						clearancePaymentType = cost.getPaymentGubn();
					}
					if(cbCode.equals("") && !Util.null2str(cost.getAccountCode()).equals("")){
						cbCode = cost.getAccountCode();
					}
				}else if(chgType.equals("DLV")){
					deliveryFee += cost.getArAmount();
					if(deliveryPaymentType.equals("") && !Util.null2str(cost.getPaymentGubn()).equals("")){
						deliveryPaymentType = cost.getPaymentGubn();
					}
					if(deliveryCode.equals("") && !Util.null2str(cost.getAccountCode()).equals("")){
						deliveryCode = cost.getAccountCode();
					}
				}
			}
			tmpMap.clear();
			tmpMap.put("odJobNo", orderCostUpdate.getOdJobNo());
			tmpMap.put("updateUserId", orderCostUpdate.getUpdateUserId());
			tmpMap.put("gmtTime", orderCostUpdate.getGmtTime());
			if(!orderCostUpdate.getUpdateType().equals("DLV")){
				tmpMap.put("updateTariffYn", "Y");
				tmpMap.put("tariff", tariff);
				if(!clearancePaymentType.equals("")){
					tmpMap.put("clearancePaymentType", clearancePaymentType);
				}
				if(!cbCode.equals("")){
					tmpMap.put("cbCode", cbCode);
				}
			}
			if(!orderCostUpdate.getUpdateType().equals("BRK")){
				tmpMap.put("updateDeliveryFeeYn", "Y");
				tmpMap.put("deliveryFee", deliveryFee);
				if(!deliveryPaymentType.equals("")){
					tmpMap.put("deliveryPaymentType", deliveryPaymentType);
				}
				if(!deliveryCode.equals("")){
					tmpMap.put("deliveryCode", deliveryCode);
				}
			}
			orderMapper.updateCostInfo(tmpMap);
			
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setJobFuncType("UPDATE");
			processLogs.setEntCode(orderCostUpdate.getEntCode());
			processLogs.setEntBusinessCode(orderCostUpdate.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(orderCostUpdate.getBound());
			processLogs.setBoundType(orderCostUpdate.getBoundType());
			processLogs.setProcessUserId(orderCostUpdate.getUpdateUserId());
			processLogs.setLogJobType("DATA");
			processLogs.setDescription("OrderCostUpdate(" + orderCostUpdate.getUpdateType() + ")");
			processLogs.setJobLog(new Gson().toJson(orderCostUpdate));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(orderCostUpdate.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(orderCostUpdate.getLogUserName());
			processLogs.setProcessUserNameEng(orderCostUpdate.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
			orderSaveResult.setResultData(tmpMap);
		}else{
			orderSaveResult.setResultCode("noUpdate");
		}
		
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 OdChgInv - update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateOdChgInv(HashMap<String, Object> map, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		if(Util.null2str(map.get("odJobNo")).equals("") || Util.null2str(map.get("updateType")).equals("")){
			orderSaveResult.setResultCode("A00001");
			return orderSaveResult;
		}
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		List<Map<String, Object>> invoiceCheckList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> odChgInvDelList = (List<Map<String, Object>>) map.get("odChgInvDelList");	/** 그리드에서 삭제한 추가비용 */
		List<Map<String, Object>> odChgInvList = (List<Map<String, Object>>) map.get("odChgInvList");		/** 그리드 추가비용 */
		
		int chargeSeqNo = 1;
		if(odChgInvList != null){
			for(Map<String, Object> odChgInv : odChgInvList){
				invoiceCheckList.add(odChgInv);
				odChgInv.put("CHARGE_SEQ_NO", Util.null2str(map.get("odJobNo")) + "-" + Util.lpad(Util.null2str(chargeSeqNo++), 4, '0'));
			}
		}
		
		tmpMap.put("odJobNo", map.get("odJobNo"));
		tmpMap.put("updateType", map.get("updateType"));
		tmpMap.put("entCode", map.get("entCode")); 
		tmpMap.put("solution", map.get("solution"));
		tmpMap.put("entBusinessCode", map.get("entBusinessCode"));
		tmpMap.put("gmtTime", map.get("gmtTime"));
		tmpMap.put("dateFormat", map.get("dateFormat")); 
		tmpMap.put("registerUserId", map.get("updateUserId")); 
		orderMapper.deleteOdChgInv(tmpMap);
		
		if(odChgInvList != null && odChgInvList.size() > 0) { 
			tmpMap.put("odChgInvList", odChgInvList);
			orderMapper.insertOdChgInv(tmpMap);
		}
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setJobFuncType("UPDATE");
		processLogs.setEntCode((String) map.get("entCode"));
		processLogs.setEntBusinessCode((String) map.get("entBusinessCode"));
		processLogs.setSolution("ECM");
		processLogs.setBound((String) map.get("bound"));
		processLogs.setBoundType((String) map.get("boundType"));
		processLogs.setProcessUserId((String) map.get("updateUserId"));
		processLogs.setLogJobType("DATA");
		processLogs.setDescription("map(" + map.get("updateType") + ")");
		processLogs.setJobLog(new Gson().toJson(map));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName((String) map.get("logUserBusinessPrintName"));
		processLogs.setProcessUserName((String) map.get("logUserName"));
		processLogs.setProcessUserNameEng((String) map.get("logUserNameEng"));
		commonService.insertLog(processLogs, request);
		orderSaveResult.setResultData(tmpMap);
		return orderSaveResult;
		
	}
    
	/**
	 * 전자상거래 Order 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Order selectOrder(SearchOrder search) throws Exception {
		String bound = search.getSrchBound();
		String boundType = search.getSrchBoundType();
		
		Util.accessControlDatabase(search, "dateFormat|userLanguage", false, null, null, null, null);
		search.setProfileName(profile);
		Order order = orderMapper.selectOrder(search);
		if(order != null){
			
			order.setItemList(orderMapper.selectItem(search));
			order.setCostList(orderMapper.selectCost(search));
			order.setPodList(orderMapper.selectPod(search));
			
			if(boundType.equals("IMP")){
				order.setMultiList(orderMapper.selectMulti(search));
				order.setPackingList(orderMapper.selectPacking(search));
				order.setPoInfoList(orderMapper.selectPoInfo(search));
				if(bound.equals("AIR")){
					order.setSpecialItemList(orderMapper.selectSpecialItem(search));
					order.setAbnormalList(orderMapper.selectAbnormal(search));
				}
			}else{
				order.setLicenseList(orderMapper.selectLicense(search));
			}
			if(bound.equals("SEA")){
				order.setContainerList(orderMapper.selectContainer(search));
			}
			
			List<OrderCost> costList = order.getCostList();
			double tariff = 0;
			double deliveryFee = 0;
			String chgCodeSystem;
			if(costList != null && costList.size() > 0){
				for(OrderCost cost : costList){
					chgCodeSystem = Util.null2str(cost.getChgCodeSystem());
					if(chgCodeSystem.equals("BRK")){
						tariff += Util.null2dbl(cost.getArAmount());
					}else if(chgCodeSystem.equals("DLV")){
						deliveryFee += Util.null2dbl(cost.getArAmount());
					}
				}
			}
			order.setTariff(tariff);
			order.setDeliveryFee(deliveryFee);
			
			Map tmpMap = new HashMap();
			tmpMap.put("entCode", search.getSrchEntCode());
			tmpMap.put("refJobNo", search.getSrchOdJobNo());
			tmpMap.put("userLanguage", search.getUserLanguage());
			order.setAlarmList(systemMngMapper.selectAlarm(tmpMap));
			order.setCsList(orderMapper.selectCs(tmpMap));
		}
		return order;
	}
	
	/**
	 * 수출오더 제출번호 생성
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult selectSimpleDocNo(SearchOrder search) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);
		
		Map<String, Object> orderMap = orderMapper.selectSimpleDocNoData(search);
		if(orderMap != null && orderMap.size() > 0) {
			String ediStatus = Util.null2str(orderMap.get("SIMPLE_EDI_STATUS"));
			String cdType =Util.null2str(search.getSrchCdType());
			if(!Util.null2str(orderMap.get("FINISH_YN")).equals("Y") || (!ediStatus.equals("") && !ediStatus.equals("ERROR")) || (!cdType.equals("Y") && !cdType.equals("E"))){
				orderSaveResult.setResultCode("A11323");	/** 제출번호를 생성할수 없는 상태입니다. */
				return orderSaveResult;
			}else if(Util.null2str(orderMap.get("EDI_EXPRESS_SIGN")).equals("")){
				orderSaveResult.setResultCode("A11271");	/** 관세청 EDI정보가 없습니다. */
				return orderSaveResult;
			}
			
			orderMap.put("entCode", search.getSrchEntCode());
			orderMap.put("gmtTime", search.getGmtTime());
			orderMap.put("entBusinessCode", "");
			orderMap.put("updateUserId", search.getUserId());
			orderMap.put("solution", "ECM");
			orderMap.put("stockType", "OD");
			orderMap.put("stockSign", orderMap.get("EDI_EXPRESS_SIGN"));
			orderMap.put("stockSeqLength", "8");
			orderMap.put("signType", "noHyphen");
			orderMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			orderMap.put("stockRemark", "ORDER SIMPLE DOC NO");
			if(cdType.equals("Y")){
				orderMap.put("dateLength", "4");
				orderMap.put("stockDateFormat", "112");
			}else if(cdType.equals("E")){
				orderMap.put("dateLength", "2");
				orderMap.put("stockDateFormat", "12");
			}
			orderSaveResult.setResultCode(commonMapper.selectNextStockMaxNo(orderMap));
		}
		
		return orderSaveResult;
	}
	/**
	 * Order List(Detail) - 합산과세자료조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCombinedTaxList selectCombinedTaxList(SearchOrderCombinedTaxList search) throws Exception {
		
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		OrderCombinedTaxList orderCombinedTaxList = new OrderCombinedTaxList();
		if(Util.null2str(search.getSrchTaxType()).equals("returnNull")){
			orderCombinedTaxList.setOrderCombinedTaxList(new ArrayList<OrderCombinedTax>());
			orderCombinedTaxList.setTotal(0);
			return orderCombinedTaxList;
		}
		Util.accessControlDatabase(search, "srchTaxType|srchDateFormat|level", true, "srchDateGbn", "od.AR_DATE|od.OB_DATE|od.PO_DATE", null, null);
		
		Map<String, Object> map = BeanUtils.describe(search);
		/* 환율 계산부분 삭제 
		List<Map<String, Object>> exchangeRateNullList = orderMapper.selectExchangeRateNullList(map);
		if(exchangeRateNullList.size() > 0){
			map.put("exrateType", "EXRATE_TYPE_AI");
			Map<String, Object> exrateType = orderMapper.selectExrateType(map);
			if(exrateType != null && !Util.null2str(exrateType.get("EXRATE_TYPE")).equals("")){
				map.put("exrateType", exrateType.get("EXRATE_TYPE"));
			}else{
				map.put("exrateType", "EXRATE_DEFAULT");
			}
			Map<String, Object> exchangeRateMap = orderMapper.selectExchangeRate(map);
			double amountLoc = 0;
			double exchangeRate = 0;
			double exchangeRateUsd = Util.null2dbl(exchangeRateMap.get("EXCHANGE_RATE_USD"));
			for(Map<String, Object> exchangeRateNullMap : exchangeRateNullList){
				exchangeRate = Util.null2dbl(exchangeRateNullMap.get("EXCHANGE_RATE"));
				if(exchangeRate == 0){
					exchangeRate = Util.null2dbl(exchangeRateMap.get("EXCHANGE_RATE_" + Util.null2str(exchangeRateNullMap.get("CURRENCY_CODE"))));
				}
				if(exchangeRate == 0){
					amountLoc = 0;
				}else{
					exchangeRateNullMap.put("EXCHANGE_RATE", exchangeRate);
					amountLoc = Util.null2dbl(exchangeRate * Util.null2dbl(exchangeRateNullMap.get("AMOUNT_CUR")), 2, "U");
				}
				exchangeRateNullMap.put("AMOUNT_LOC", amountLoc);
				if(Util.null2str(exchangeRateNullMap.get("CURRENCY_CODE")).equals("USD")){
					exchangeRateNullMap.put("AMOUNT_USD", exchangeRateNullMap.get("AMOUNT_CUR"));
				}else if(amountLoc != 0 && amountLoc != 0 && exchangeRateUsd!= 0){
					exchangeRateNullMap.put("AMOUNT_USD", Util.null2dbl(amountLoc / exchangeRateUsd, 2, "U"));
				}else{
					exchangeRateNullMap.put("AMOUNT_USD", 0);
				}
			}
			
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				OrderMapper orderMapper2 = sqlSession.getMapper(OrderMapper.class);
				int cnt = 1;
				for(Map<String, Object> exchangeRateUpdateMap : exchangeRateNullList){
					orderMapper2.updateExchangeRate(exchangeRateUpdateMap);
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
			
		}
		*/
		
		String srchTaxType = Util.null2str(map.get("srchTaxType"));
		int idx = srchTaxType.indexOf(".");
		map.put("srchTaxType2",  srchTaxType.substring(idx+1));
		
		Util.getCryptographyYn(map, "srchTaxType");
		
		List<String> odJobNoStrList = orderMapper.selectCombinedTaxParams(map);
		List<String> odJobNoList = new ArrayList<String>();
		String[] tmpOdJobNo;
		for(String odJobNoStr : odJobNoStrList){
			tmpOdJobNo = odJobNoStr.split(",");
			for(String odJobNo : tmpOdJobNo){
				odJobNoList.add(odJobNo);
			}
		}
		if(odJobNoList.size() > 0){
			if(search.getPage() != 0 && search.getPageSize() != 0){
				search.setStartNum(search.getPageSize() * (search.getPage() - 1));
			}
			List<OrderCombinedTax> list = null;
			int houseCnt = odJobNoList.size();
			Map srchMap = BeanUtils.describe(search);
			
			//합산과세 팝업 - 정렬 오류 나서 추가
			if(search.getSort() != null && search.getSort().size() > 0) {
				List<Sort> sortList = new ArrayList<Sort>();
				Sort sort = new Sort();
				sort.setField(search.getSort().get(0).getField());
				sort.setDir(search.getSort().get(0).getDir());
				sortList.add(sort);
				srchMap.put("sort", sortList);
			}
			
			//합산과세 팝업 - 페이징 없이 2000건까지 조회되는 것으로 수정됨
//			if(houseCnt > 1000){
			if(houseCnt > 2000) {
//				list = new ArrayList<OrderCombinedTax>();
//				for(int i = 0; i < houseCnt; i+=1000){
//					if(i + 1000 < houseCnt) {
//						srchMap.put("srchOdJobNoList", odJobNoList.subList(i, i + 1000));
//					}else{
//						srchMap.put("srchOdJobNoList", odJobNoList.subList(i, houseCnt));
//					}
//					list.addAll(orderMapper.selectCombinedTaxList(srchMap));
//				}
				
				srchMap.put("srchOdJobNoList", odJobNoList.subList(0, 2000));
				list  = orderMapper.selectCombinedTaxList(srchMap);
				
			}else{
				srchMap.put("srchOdJobNoList", odJobNoList);
				list  = orderMapper.selectCombinedTaxList(srchMap);
			}
			
//			srchMap.put("srchOdJobNoList", odJobNoList);
//			list  = orderMapper.selectCombinedTaxList(srchMap);
			
			orderCombinedTaxList.setOrderCombinedTaxList(list);
			if(list.size() > 0) {
				orderCombinedTaxList.setTotal(list.get(0).getTotal());
			}else {
				orderCombinedTaxList.setTotal(0);
			}
			return orderCombinedTaxList;
		}else{
			orderCombinedTaxList.setOrderCombinedTaxList(new ArrayList<OrderCombinedTax>());
			orderCombinedTaxList.setTotal(0);
			return orderCombinedTaxList;
		}
	}
	
	/**
	 * 전자상거래 Order 배제처리
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateExclusion(OrderExclusion exclusion, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		/** 마감체크 */
		consolService.closeChkList(orderSaveResult, exclusion.getOdJobNoList(), "odJobNoList");
		if(!Util.null2str(orderSaveResult.getResultCode()).equals("")){
			return orderSaveResult;
		}
		Util.accessControlDatabase(exclusion, "gmtTime", false, null, null, null, null);
		orderMapper.updateExclusion(exclusion);
		
		Map<String, Object> updateCdTypeMap = new HashMap<String, Object>();
		updateCdTypeMap.put("odJobNoList", exclusion.getOdJobNoList());
		updateCdTypeMap.put("cdType", "B");
		updateCdTypeMap.put("action", "updateExclusion");
		commonService.insertBmsCargoProgress(commonMapper.selectBmsCdTypeList(updateCdTypeMap));
		
		commonMapper.updateBmsCdType(updateCdTypeMap);

		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		exclusion.setOdJobNoList(null);
		processLogs.setEntCode(exclusion.getEntCode());
		processLogs.setEntBusinessCode(exclusion.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(exclusion.getBound());
		processLogs.setBoundType(exclusion.getBoundType());
		processLogs.setProcessUserId(exclusion.getUpdateUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("UPDATE");
		processLogs.setDescription("order exclusion");
		processLogs.setJobLog(new Gson().toJson(exclusion));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(exclusion.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(exclusion.getLogUserName());
		processLogs.setProcessUserNameEng(exclusion.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		return orderSaveResult;
	}
	
	/**
	 * 엑셀업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult excelUploadCheck(MultipartFile uploadExcel, String entCode, String excelUploadType) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
			orderSaveResult.setResultCode("extError");
			return orderSaveResult;
		}
		
		FileUploadResponse result = fileService.storeFile(uploadExcel, entCode, true);
		
		Map<String, Object> rstHbl = null;
		
		if(Util.null2str(result.getResult()).equals("ok")){
			File file = fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile();
			String extension = null;
			if(file.getName().toLowerCase().indexOf(".xlsx") > -1){
				extension = "xlsx";
			}else{
				extension = "xls";
			}
			
			String[] fieldName = null;
			String[] fieldTitle = null;
			String[] fieldLength = null;
			String[] fieldType = null;
			
			String[] fieldNameItem = null;
			String[] fieldTitleItem = null;
			String[] fieldLengthItem = null;
			String[] fieldTypeItem = null;
			
			String[] fieldNameCntr = null;
			String[] fieldTitleCntr = null;
			String[] fieldLengthCntr = null;
			String[] fieldTypeCntr = null;
			if(excelUploadType.equals("odChg")){
				fieldName =		new String[]{"No",	"MBL_NO",	"HBL_NO",	"PIECES",	"GWT",	"CBM",	"CURRENCY_CODE",	"AMOUNT_CUR",	"ALLOWED_ITEM_CODE",	"COMMODITY_ITEM",	"CD_TYPE",	"CUSTOMS_ID_TYPE",	"CUSTOMS_ID",	"SHIPPER_NAME",	"SHIPPER_ADDRESS",	"CONSIGNEE_NAME",	"CONSIGNEE_NAME_LOC",	"CONSIGNEE_ADDRESS",	"CONSIGNEE_ADDRESS_LOC",	"CONSIGNEE_TEL",	"CONSIGNEE_HP",		"CONSIGNEE_ZIPCODE",	"URL",	"REMARKS"};
				fieldTitle =	new String[]{"No",	"M.B/L No",	"H.B/L No",	"수량",		"중량",	"CBM",	"통화단위",			"금액(외화)",	"HS Code",				"품명",				"통관구분",	"개인통관부호코드",	"개인통관부호",	"송하인",		"송하인주소",		"수하인명",			"수하인명(L)",			"수하인주소",			"수하인주소(L)",			"수하인전화번호",	"수하인휴대전화",	"우편번호",				"URL",	"비고"};
				fieldLength =	new String[]{"10",	"20",		"20",		"5",		"12.3",	"12.3",	"3",				"17.4",			"2",					"200",				"2",		"1",				"50",			"100",			"300",				"100",				"100",					"300",					"300",						"50",				"50",				"10",					"200",	"420"};
				fieldType =		new String[]{"S",	"S",		"S",		"N",		"DBL",	"DBL",	"S",				"DBL",			"S",					"SN",				"S",		"S",				"S",			"SN",			"SN",				"SN",				"SN",					"SN",					"SN",						"S",				"S",				"S",					"S",	"SN"};
			}else if(excelUploadType.equals("carryinChg")){
				fieldName =		new String[]{"MBL_NO","HSN_STRT","HSN_END","CARRYIN_NO"};
				fieldTitle =	new String[]{"M.B/L No","HSN 시작","HSN 끝","반입근거번호"};
				fieldLength =	new String[]{"20","4","4","20"};
				fieldType =		new String[]{"S","S","S","S"};
			}else if(excelUploadType.equals("insertPod")){
				fieldName =		new String[]{"MBL_NO"	,"HBL_NO",		"ECM010",	"ECM030",		"ECM050",		"ECM060",	"ECM070",	"ECM120",	"ECM210",		"ECM290",		"ECM240",	"ECM360",	"ECM420",	"ECM400"};
				fieldTitle =	new String[]{"M.B/L No"	,"H.B/L No",	"오더접수",	"출발지반입",	"출발지반출",	"출항",		"입항",		"통관접수",	"도착지반입",	"도착지반출",	"통관완료",	"배송접수",	"배송완료",	"미배달"};
				fieldLength =	new String[]{"20",		"20",		"16","16","16","16","16","16","16","16","16","16","16","16"};
				fieldType =		new String[]{"S","S","S","S","S","S","S","S","S","S","S","S","S","S"};
			}else if(excelUploadType.equals("insertEmsTs")){
				fieldName =		new String[]{"NO",	"PO_NO",	"PO_DATE",		"CUST_CODE",	"HBL_NO",	"MBL_NO",	"VESSEL_FLIGHT",	"OB_DATE",	"AR_DATE",	"ORIG_CODE",	"DEST_CODE",	"MAIL_TYPE",	"CONTRACT_CODE",	"INTER_EMAIL_TYPE",	"ITEM_TYPE",	"INSURANCE_YN",	"INSURANCE_AMOUNT",	"SHIPPER_NAME",			"SHIPPER_ADDRESS",		"SHIPPER_ADDRESS_A",	"SHIPPER_ADDRESS_B",	"SHIPPER_ADDRESS_C",	"ORIG_COUNTRY",				"SHIPPER_TEL",	"SHIPPER_TEL_A",			"SHIPPER_TEL_B",			"SHIPPER_TEL_C",		"SHIPPER_TEL_D",			"SHIPPER_HP",			"SHIPPER_HP_A",			"SHIPPER_HP_B",			"SHIPPER_HP_C",			"SHIPPER_ZIPCODE",		"SHIPPER_EMAIL",		"URL",		"CONSIGNEE_NAME",		"CONSIGNEE_ADDRESS",		"CONSIGNEE_ADDRESS_C",		"CONSIGNEE_ADDRESS_B",		"CONSIGNEE_ADDRESS_A",			"CONSIGNEE_TEL",		"CONSIGNEE_TEL_A",			"CONSIGNEE_TEL_B",			"CONSIGNEE_TEL_C",			"CONSIGNEE_TEL_D",			"CONSIGNEE_ZIPCODE",		"CONSIGNEE_HP",		"CONSIGNEE_EMAIL",	"TAX_ID_NO",			"DEST_COUNTRY",					"CONSIGNEE_NAME_LOC",			"CONSIGNEE_ADDRESS_LOC",		"CONSIGNEE_STATE",				"CONSIGNEE_PLACE",				"CONSIGNEE_STREET",					"COMMODITY_ITEM",		"COMMODITY_ITEM_LOC",		"PIECES",	"GWT",		"CBM",		"DIM_WIDTH",	"DIM_LENGTH",	"DIM_HEIGHT",	"VOLUMN_EXCLUDE_YN",		"VAT_IDT_NO",		"CURRENCY_CODE",	"AMOUNT_CUR",	"DELIVERY_CENTER_CODE",	"BAG_NO",	"DELIVERY_AMOUNT",		"GROUP_SHIPPING_DATE",	"GROUP_AGENT_CODE",		"GROUP_EXCHANGE_OFFICE",	"GROUP_BAG_NUMBER",		"GROUP_BAG_WEIGHT",		"GROUP_AMOUNT"};
				fieldTitle =	new String[]{"No",	"Order No",	"Order Date",	"AgentCode",	"BL No",	"M.B/L No",	"Vessel",			"ETD",		"ETA",		"Origin Code",	"Dest Code",	"EMS TYPE",		"환적계약구분코드",	"EMSKind",			"EMSDetailDiv",	"InsuranceYN",	"Insurance Amount",	"Shipper Name(ENG)",	"Shipper Address(ENG)",	"EMS Shipper Address1",	"EMS Shipper Address2",	"EMS Shipper Address3",	"EMS Shipper Country Code",	"Shipper TEL",	"EMS Shipper TEL (국가)",	"EMS Shipper TEL (지역)",	"EMS Shipper TEL (국)",	"EMS Shipper TEL (번호)",	"EMS Shipper Mobile",	"EMS Shipper Mobile-1",	"EMS Shipper Mobile-2",	"EMS Shipper Mobile-3",	"EMS Shipper ZIP Code",	"EMS Shipper E-Mail",	"URL",		"Consignee Name(ENG)",	"Consignee Address(ENG)",	"EMS Consignee State(ENG)",	"EMS Consignee Place(ENG)",	"EMS Consignee Street(ENG)",	"EMS Consignee TEL",	"EMS Consignee(국가)- TEL",	"EMS Consignee(지역)- TEL",	"EMS Consignee(국)- TEL",	"EMS Consignee(번호)- TEL",	"EMS Consignee Zip Code",	"Consignee Mobile",	"Consignee E-mail",	"EMS Tax ID Number",	"EMS Consignee Contry Code",	"EMS Consignee Name(LOCAL)",	"EMS ConsigneeAddress(LOCAL)",	"EMS Consignee State(LOCAL)",	"EMS Consignee Place(LOCAL)",	"EMS Consignee District(LOCAL)",	"Commondity Name(ENG)",	"Commondity Name(LOCAL)",	"Box Qty",	"Total WT",	"CBM",		"가로",			"세로",			"높이",			"부피중량 적용제외신청",	"부가세식별번호",	"Currency Code",	"Total Amount",	"Delivery Center Code",	"Bag No",	"EMS Delivery Payment",	"묵음번호용 발송일자",	"묵음번호용 업체코드",	"묵음번호용 교환국",		"묵음번호용 자루순번",	"묵음번호용 자루중량",	"묵음번호용 우편물수"};
				fieldLength =	new String[]{"10",	"35",		"8",			"10",			"20",		"20",		"30",				"8",		"8",		"5",			"5",			"1",			"2",				"3",				"2",			"1",			"12.2",				"100",					"300",					"200",					"50",					"50",					"2",						"35",			"4",						"4",						"4",					"4",						"20",					"4",					"4",					"4",					"10",					"40",					"300",		"100",					"300",						"50",						"50",						"200",							"35",					"4",						"4",						"4",						"4",						"10",						"20",				"50",				"100",					"2",							"60",							"300",							"60",							"60",							"60",								"300",					"300",						"10",		"12.3",		"12.3",		"6.2",			"6.2",			"6.2",			"1",						"50",				"3",				"12.2",			"3",					"10",		"12.3",					"6",					"2",					"3",						"4",					"6",					"3"};
				fieldType =		new String[]{"S",	"S",		"S",			"S",			"S",		"S",		"S",				"S",		"S",		"S",			"S",			"S",			"S",				"S",				"S",			"S",			"DBL",				"S",					"S",					"S",					"S",					"S",					"S",						"S",			"S",						"S",						"S",					"S",						"S",					"S",					"S",					"S",					"S",					"S",					"S",		"S",					"S",						"S",						"S",						"S",							"S",					"S",						"S",						"S",						"S",						"S",						"S",				"S",				"S",					"S",							"S",							"S",							"S",							"S",							"S",								"S",					"S",						"N",		"DBL",		"DBL",		"DBL",			"DBL",			"DBL",			"S",						"S",				"S",				"DBL",			"S",					"S",		"DBL",					"S",					"S",					"S",						"N",					"N",					"N"};
				
				fieldNameItem =		new String[]{"DETAIL_ITEM_NAME",	"DETAIL_ITEM_NAME_LOC",	"PIECES",			"UNIT_PIECES",		"GWT",				"VWT",						"AMOUNT",			"CURRENCY_CODE",		"HS_CODE",	"ORIGIN_COUNTRY",		"REMARKS"};
				fieldTitleItem =	new String[]{"ITEM Name(ENG)",		"ITEM NAME(Local)",		"ITEM QYT",			"ITEM QTY UNIT",	"ITEM TOTAL WT",	"ITEM TOTAL Net Weight",	"ITEM TOTAL Value",	"ITEM Currency Code",	"HS Code",	"Country Of Origin",	"Remark"};
				fieldLengthItem =	new String[]{"200",					"200",					"10",				"3",				"12.3",				"12.3",						"12.2",				"3",					"10",		"2",					"100"};
				fieldTypeItem =		new String[]{"S",					"S",					"N",				"S",				"DBL",				"DBL",						"DBL",				"S",					"S",		"S",					"S"};
				
				fieldNameCntr =		new String[]{"CNTR_NO",				"CNTR_CODE",			"CNTR_SEAL_NO_1"};
				fieldTitleCntr =	new String[]{"Container No",		"Container Type",		"SEAL No"};
				fieldLengthCntr =	new String[]{"11",					"4",					"20"};
				fieldTypeCntr =		new String[]{"S",					"S",					"S"};
			}else if(excelUploadType.equals("updateLicense")){
				fieldName =		new String[]{"HBL_NO"	,"LICENSE_NO",		"PIECES",	"GWT",		"PARTIAL_YN",		"PARTIAL_SEQ",	"COMBINE_YN",	"COMBINE_TYPE_A_Z", "COMBINE_PIECES"};
				fieldTitle =	new String[]{"H.B/L No"	,"수출면장번호",	"수량",	"중량",	"분할여부(Y/N)",	"분할차수",		"동시포장여부(Y/N)",		"동시포장코드(A-Z)", "동시포장개수"};
				fieldLength =	new String[]{"20",		"20",		"4","9.3","1","1","1","1","4"};
				fieldType =		new String[]{"S","S","N","DBL","S","N","S","S","N"};
			}
			
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> dataListSub = null;
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				Sheet sheet = wb.getSheetAt(0);
				DataFormatter df = new DataFormatter();
				int fieldSize = fieldName.length;
				int fieldSizeItem = 0;
				int fieldSizeCntr = 0;
				if(fieldNameItem != null) fieldSizeItem = fieldNameItem.length;
				if(fieldNameCntr != null) fieldSizeCntr = fieldNameCntr.length;
				StringBuffer errMsg = new StringBuffer();
				Map<String, Object> dataMap = null;
				Map<String, Object> dataMapSub = null;
				String fieldValue = "";
				String firstMblNo = null;
				boolean chkMblNo = false;
				// 데이트 포맷
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				if(extension.equals("xlsx")){
					XSSFCell myCell = null;
					for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
						if(sheet.getRow(i) == null){
							orderSaveResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							orderSaveResult.setResultData(resultData);
							return orderSaveResult;
						}
						
						dataMap = new HashMap<String, Object>();
						//셀 셋팅
						for(int j = 0; j < fieldSize; j++){
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i, false);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						
						if(excelUploadType.equals("odChg")) {
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("")){
								if(Util.null2str(dataMap.get("MBL_NO")).equals("")){
									dataMap.put("MBL_NO","");
								}
								dataList.add(dataMap);
							}
						}else if(excelUploadType.equals("carryinChg")) {
							if(!Util.null2str(dataMap.get("MBL_NO")).equals("") && Util.null2str(dataMap.get("HSN_STRT")).length() == 4 && Util.null2str(dataMap.get("HSN_END")).length() == 4 && !Util.isEmpty(dataMap.get("CARRYIN_NO"))){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else if(excelUploadType.equals("insertEmsTs")){
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("")){
								
								/** 세부품목/컨테이너 각각 3번까지 반복가능 */
								int startIdx, fieldSeq, pieces;
								dataListSub = new ArrayList<Map<String, Object>>();
								for(int k = 0; k < 3; k++){
									startIdx = fieldSize + (k * (fieldSizeItem + fieldSizeCntr));
									dataMapSub = new HashMap<String, Object>();
									fieldSeq = 0;
									for(int l = startIdx; l < startIdx + fieldSizeItem; l++){
										myCell = (XSSFCell)sheet.getRow(i).getCell(l, MissingCellPolicy.CREATE_NULL_AS_BLANK);
										fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitleItem[fieldSeq], fieldLengthItem[fieldSeq], fieldTypeItem[fieldSeq], errMsg, i, false);
										if(!Util.null2str(fieldValue).equals("")){
											dataMapSub.put(fieldNameItem[fieldSeq], fieldValue);
										}
										fieldSeq++;
									}
									if(!Util.isEmpty(dataMapSub.get("DETAIL_ITEM_NAME"))){
										pieces = Util.null2int(dataMapSub.get("PIECES"));
										if(pieces > 0){
											dataMapSub.put("UNIT_PRICE", Util.null2dbl(dataMapSub.get("AMOUNT")) / pieces);
										}
										dataListSub.add(dataMapSub);
									}
								}
								if(dataListSub.size() > 0){
									dataMap.put("itemList", dataListSub);
									dataListSub = new ArrayList<Map<String, Object>>();
								}
								
								for(int k = 0; k < 3; k++){
									startIdx = fieldSize + ((k + 1) * fieldSizeItem) + (k * fieldSizeCntr);
									dataMapSub = new HashMap<String, Object>();
									fieldSeq = 0;
									for(int l = startIdx; l < startIdx + fieldSizeCntr; l++){
										myCell = (XSSFCell)sheet.getRow(i).getCell(l, MissingCellPolicy.CREATE_NULL_AS_BLANK);
										fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitleCntr[fieldSeq], fieldLengthCntr[fieldSeq], fieldTypeCntr[fieldSeq], errMsg, i, false);
										if(!Util.null2str(fieldValue).equals("")){
											dataMapSub.put(fieldNameCntr[fieldSeq], fieldValue);
										}
										fieldSeq++;
									}
									if(!Util.isEmpty(dataMapSub.get("CNTR_NO"))){
										dataListSub.add(dataMapSub);
									}
								}
								if(dataListSub.size() > 0){
									dataMap.put("cntrList", dataListSub);
								}
								if(!chkMblNo){	/** 한 엑셀파일에 마스터는 한종류만 입력가능함  첫번째데이터의 MBL_NO로 모든 데이터 처리 */
									firstMblNo = (String)dataMap.get("MBL_NO");
									chkMblNo = true;
								}
								if(!Util.isEmpty(firstMblNo)){
									firstMblNo = firstMblNo.replace("	", "").trim();
								}
								dataMap.put("MBL_NO", firstMblNo);
								dataMap.put("HBL_NO", Util.null2str(dataMap.get("HBL_NO")).replace("	", "").trim());
								dataList.add(dataMap);
							}
						}else if(excelUploadType.equals("updateLicense")) {
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("") && !Util.null2str(dataMap.get("LICENSE_NO")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else{
							dataList.add(dataMap);
						}
					}
				}else{
					HSSFCell myCell = null;
					for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
						if(sheet.getRow(i) == null){
							orderSaveResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							orderSaveResult.setResultData(resultData);
							return orderSaveResult;
						}
						
						dataMap = new HashMap<String, Object>();
						//셀 셋팅
						for(int j = 0; j < fieldSize; j++){
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i, false);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						
						if(excelUploadType.equals("odChg")) {
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("")){
								if(Util.null2str(dataMap.get("MBL_NO")).equals("")){
									dataMap.put("MBL_NO","");
								}
								dataList.add(dataMap);
							}
						}else if(excelUploadType.equals("carryinChg")) {
							if(!Util.null2str(dataMap.get("MBL_NO")).equals("") && !Util.null2str(dataMap.get("HSN_STRT")).equals("") && !Util.null2str(dataMap.get("HSN_END")).equals("")){
								dataList.add(dataMap);
							}
						}else if(excelUploadType.equals("insertEmsTs")){
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("")){
								
								/** 세부품목/컨테이너 각각 3번까지 반복가능 */
								int startIdx, fieldSeq, pieces;
								dataListSub = new ArrayList<Map<String, Object>>();
								for(int k = 0; k < 3; k++){
									startIdx = fieldSize + (k * (fieldSizeItem + fieldSizeCntr));
									dataMapSub = new HashMap<String, Object>();
									fieldSeq = 0;
									for(int l = startIdx; l < startIdx + fieldSizeItem; l++){
										myCell = (HSSFCell)sheet.getRow(i).getCell(l, MissingCellPolicy.CREATE_NULL_AS_BLANK);
										fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitleItem[fieldSeq], fieldLengthItem[fieldSeq], fieldTypeItem[fieldSeq], errMsg, i, false);
										if(!Util.null2str(fieldValue).equals("")){
											dataMapSub.put(fieldNameItem[fieldSeq], fieldValue);
										}
										fieldSeq++;
									}
									if(!Util.isEmpty(dataMapSub.get("DETAIL_ITEM_NAME"))){
										pieces = Util.null2int(dataMapSub.get("PIECES"));
										if(pieces > 0){
											dataMapSub.put("UNIT_PRICE", Util.null2dbl(dataMapSub.get("AMOUNT")) / pieces);
										}
										dataListSub.add(dataMapSub);
									}
								}
								
								if(dataListSub.size() > 0){
									dataMap.put("itemList", dataListSub);
									dataListSub = new ArrayList<Map<String, Object>>();
								}
								for(int k = 0; k < 3; k++){
									startIdx = fieldSize + ((k + 1) * fieldSizeItem) + (k * fieldSizeCntr);
									dataMapSub = new HashMap<String, Object>();
									fieldSeq = 0;
									for(int l = startIdx; l < startIdx + fieldSizeCntr; l++){
										myCell = (HSSFCell)sheet.getRow(i).getCell(l, MissingCellPolicy.CREATE_NULL_AS_BLANK);
										fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitleCntr[fieldSeq], fieldLengthCntr[fieldSeq], fieldTypeCntr[fieldSeq], errMsg, i, false);
										if(!Util.null2str(fieldValue).equals("")){
											dataMapSub.put(fieldNameCntr[fieldSeq], fieldValue);
										}
										fieldSeq++;
									}
									if(!Util.isEmpty(dataMapSub.get("CNTR_NO"))){
										dataListSub.add(dataMapSub);
									}
								}
								if(dataListSub.size() > 0){
									dataMap.put("cntrList", dataListSub);
								}
								if(!chkMblNo){	/** 한 엑셀파일에 마스터는 한종류만 입력가능함  첫번째데이터의 MBL_NO로 모든 데이터 처리 */
									firstMblNo = (String)dataMap.get("MBL_NO");
									chkMblNo = true;
								}
								if(!Util.isEmpty(firstMblNo)){
									firstMblNo = firstMblNo.replace("	", "").trim();
								}
								dataMap.put("MBL_NO", firstMblNo);
								dataMap.put("HBL_NO", Util.null2str(dataMap.get("HBL_NO")).replace("	", "").trim());
								dataList.add(dataMap);
							}
						}else if(excelUploadType.equals("updateLicense")) {
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("") && !Util.null2str(dataMap.get("LICENSE_NO")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else{
							dataList.add(dataMap);
						}
					}
				}
				
				if(errMsg.length() != 0){
					orderSaveResult.setResultCode("dataErr");
					Map<String, Object> errMap = new HashMap<String, Object>();
					errMap.put("errStr", errMsg);
					orderSaveResult.setResultData(errMap);
					return orderSaveResult;
				}
				//TODO 파일삭제?
				
				resultData.put("entCode", entCode);
				resultData.put("excelUploadType", excelUploadType);
				
				//hbl 유효성 체크
				if("insertPod".equals(excelUploadType)){
					rstHbl = new HashMap<String, Object>();
					
					String podDate = null, podTime = null, tmpPodDate, lastPodStatus, mblNo;
					SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyyMMdd");
					int excelLastPodStatus;
					int tmpLastPodStatus;
					int notBlCnt = 0;
					int idx = 0;
					boolean podAddFlag;
					List<String> podStatusList;
					List<Map<String, Object>> podList = new ArrayList<Map<String, Object>>();
					Map<String, Object> podMap;
					StringBuffer notHblList = new StringBuffer();
					
					List<Map<String, Object>> valiList = new ArrayList<Map<String, Object>>();
					
					int houseCnt = dataList.size();
					for(int i = 0; i < houseCnt; i+=500){
						if(i + 500 < houseCnt) {
							resultData.put("dataList", dataList.subList(i, i + 500));
						}else{
							resultData.put("dataList", dataList.subList(i, houseCnt));
						}
						valiList.addAll(deliveryChgMapper.selectHblValidate2(resultData));
					}
					resultData.put("dataList", dataList);
					
					
					Map<String, String> mblChkMap = new HashMap<String, String>();
					
					for(Map<String, Object> vali : valiList){
						if(Util.isEmpty(vali.get("OD_JOB_NO"))){
							notBlCnt++;
							notHblList.append(", " + Util.null2str(vali.get("HBL_NO")));
						}else if(notBlCnt == 0){
							podStatusList = new ArrayList<String>();
							/** POD날짜 있는POD 리스트 생성 */
							for(String mapKey : dataList.get(idx).keySet()){
								if(!"MBL_NO".equals(mapKey) && !"HBL_NO".equals(mapKey)){
									if(!Util.isEmpty(dataList.get(idx).get(mapKey))){
										podStatusList.add(mapKey);
									}
								}
							}
							
							excelLastPodStatus = 0;
							for(String podStatus : podStatusList){
								podAddFlag = false;
								/** 마지막 POD 찾기 */
								tmpLastPodStatus = Util.null2int(podStatus.replace("ECM", ""));
								if(excelLastPodStatus < tmpLastPodStatus) {
									excelLastPodStatus = tmpLastPodStatus;
								}
								
								podMap = new HashMap<String, Object>();
								/** 날짜형식체크   format : 2023-11-02 13:50(엑셀형식 텍스트만 처리함) */
								tmpPodDate = Util.null2str(dataList.get(idx).get(podStatus)).replaceAll("[-./\\s]", "");
								if(tmpPodDate.length() == 13){
									try {
										podDate = sdFormat.format(sdFormat2.parse(tmpPodDate.substring(0, 8)));
										podMap.put("podDate", podDate);
									}catch(Exception e){
										errMsg.append("[" + Util.null2str(vali.get("HBL_NO")) + "] 날짜형식 오류 <br />");
										break;
									}
									podTime = tmpPodDate.substring(8);
									if(Pattern.matches("^([01][0-9]|2[0-3]):([0-5][0-9])$", podTime)) {
									    podMap.put("podTime", podTime);
									}else{
										errMsg.append("[" + Util.null2str(vali.get("HBL_NO")) + "] 날짜형식 오류 <br />");
										break;
									}
								}else if(tmpPodDate.length() != 0){
									errMsg.append("[" + Util.null2str(vali.get("HBL_NO")) + "] 날짜형식 오류 <br />");
									break;
								}
								
								vali.put(podStatus, podDate + " " + podTime);
								
								podMap.put("podStatus", podStatus);
								if("ECM060".equals(podStatus) || "ECM070".equals(podStatus)){
									mblNo = Util.null2str(vali.get("MBL_NO"));
									if(mblChkMap.get(mblNo + "|" + podStatus) == null){
										podMap.put("blNo", mblNo);
										podMap.put("odJobNo", vali.get("MASTER_OD_JOB_NO"));
										mblChkMap.put(mblNo + "|" + podStatus, "Y");
										podAddFlag = true;
									}
								}else{
									podMap.put("blNo", vali.get("HBL_NO"));
									podMap.put("odJobNo", vali.get("OD_JOB_NO"));
									podAddFlag = true;
								}
								
								if(podAddFlag){
									switch(podStatus) {
										case "ECM010" :	case "ECM030" :	case "ECM050" :	case "ECM060" :	case "ECM070" :
											podMap.put("podTableName", 	"TB_ECM_POD_01");
											break;
										case "ECM120" :
											podMap.put("podTableName", 	"TB_ECM_POD_02");
											break;
										case "ECM210" :	case "ECM240" :	case "ECM290" :
											podMap.put("podTableName", 	"TB_ECM_POD_03");
											break;
										case "ECM360" :	case "ECM400" :	case "ECM420" :
											podMap.put("podTableName", 	"TB_ECM_POD_04");
											break;
									}
									podList.add(podMap);
								}
							}
							/** DB의 lastPodStatus보다 엑셀의 lastPodStatus가 더 클경우 업데이트하기위한 데이터 추가 */
							if(excelLastPodStatus >= Util.null2int(Util.null2str(vali.get("LAST_POD_STATUS")).replace("ECM", ""))){
								lastPodStatus = "ECM" + Util.setSeqNo(excelLastPodStatus, 3);
								podList.get(podList.size() - 1).put("lastPodOdJobNo", vali.get("OD_JOB_NO"));
								podList.get(podList.size() - 1).put("lastPodStatus", lastPodStatus);
								podList.get(podList.size() - 1).put("lastPodDateTime", vali.get(lastPodStatus));
							}
						}
						idx++;
					}
					
					if(errMsg.length() != 0){
						orderSaveResult.setResultCode("dataErr");
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMsg.append("정상날짜형식 ex. 2023-11-02 13:50");
						errMap.put("errStr", errMsg);
						orderSaveResult.setResultData(errMap);
						return orderSaveResult;
					}
					
					resultData.put("NOT_BL_CNT", notBlCnt);
					if(notBlCnt > 0){
						resultData.put("NOT_HBL_LIST", notHblList.toString().substring(2));
						resultData.remove("dataList");
					}else{
						resultData.put("dataList", podList);
					}
				}else if(excelUploadType.equals("carryinChg")){
					resultData.put("dataList", dataList);
				}else if(excelUploadType.equals("insertEmsTs")){
					if(dataList.size() > 0 && !Util.isEmpty(Util.null2str(dataList.get(0).get("MBL_NO")))){
						dataMap = new HashMap<String, Object>();
						dataMap.put("mblNo", dataList.get(0).get("MBL_NO"));
						resultData.put("mblData", orderMapper.selectMasterOrderEmsTs(dataMap));
					}
					resultData.put("dataList", dataList);
				}else{
					if(excelUploadType.equals("updateLicense")){
						Set<String> licenseNoList = new HashSet<>();
						Set<String> dupNoList = new HashSet<>();

						for (Map<String, Object> row : dataList) {
						    String licenseNo = (String) row.get("LICENSE_NO");
						    if (!licenseNoList.add(licenseNo)) {
						    	dupNoList.add(licenseNo);
						    }
						}
						if(!dupNoList.isEmpty()) {
							resultData.put("licenseNo", dupNoList);
							orderSaveResult.setResultData(resultData);
							return orderSaveResult;
						}
					}
						
					resultData.put("dataList", dataList);
					rstHbl = deliveryChgMapper.selectHblValidate(resultData);
					
					if(rstHbl!=null) {
						//유효하지않은 BL
						resultData.put("NOT_BL_CNT", rstHbl.get("NOT_BL_CNT"));
						resultData.put("NOT_HBL_LIST", rstHbl.get("NOT_HBL_LIST"));
						resultData.remove("dataList");
					}else {
						resultData.put("NOT_BL_CNT", "0");
						resultData.put("NOT_HBL_LIST", "");
					}
				}
				
				resultData.put("dataSize", dataList.size());
				
				
				
				orderSaveResult.setResultData(resultData);
				orderSaveResult.setResultCode("200");
				
				String backupPath = customProperties.getBackupDir() + "excel/" + entCode + "/" + DateTime.getDate("yyyy-MM");
				File dir = new File(backupPath);
				if (dir.exists() == false)	dir.mkdirs();
				String backupFilePath = backupPath + "/" + file.getName();
				file.renameTo(new File(backupFilePath));
			}catch(Exception e) {
				throw e;
			}finally{
				if (is != null) is.close();
			}
		}else{
			orderSaveResult.setResultCode("ERROR");
		}
		return orderSaveResult;
	}
	
	/**
	 * Excel 오더수정 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult excelUpload(ExcelUploadOdChg excelUploadOdChg, HttpServletRequest request, String excelUploadType) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		if(excelUploadOdChg.getDataList() == null) {
			return orderSaveResult;
		}
		Util.accessControlDatabase(excelUploadOdChg, "gmtTime", false, null, null, null, null);
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
			int cnt = 1;
			int seq = 1;
			
			List<Map<String, Object>> updateLastPodList = new ArrayList<Map<String, Object>>();
			
			/** 배치쿼리실행시 테이블명별로 처리하지않으면 배치처리안됨 */
			List<Map<String, Object>> podList01 = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> podList02 = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> podList03 = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> podList04 = new ArrayList<Map<String, Object>>();
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				excelOdChg.put("entCode", excelUploadOdChg.getEntCode());
				excelOdChg.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				excelOdChg.put("gmtTime", excelUploadOdChg.getGmtTime());
				excelOdChg.put("dateFormat", "23");
				if("TB_ECM_POD_01".equals(excelOdChg.get("podTableName"))){
					podList01.add(excelOdChg);
				}else if("TB_ECM_POD_02".equals(excelOdChg.get("podTableName"))){
					podList02.add(excelOdChg);
				}else if("TB_ECM_POD_03".equals(excelOdChg.get("podTableName"))){
					podList03.add(excelOdChg);
				}else if("TB_ECM_POD_04".equals(excelOdChg.get("podTableName"))){
					podList04.add(excelOdChg);
				}
			}
			
			seq = this.excelUploadInsertPod(podList01, orderBatchMapper, commonBatchMapper, sqlSession, updateLastPodList, seq);
			seq = this.excelUploadInsertPod(podList02, orderBatchMapper, commonBatchMapper, sqlSession, updateLastPodList, seq);
			seq = this.excelUploadInsertPod(podList03, orderBatchMapper, commonBatchMapper, sqlSession, updateLastPodList, seq);
			seq = this.excelUploadInsertPod(podList04, orderBatchMapper, commonBatchMapper, sqlSession, updateLastPodList, seq);
			
			for(Map<String, Object> excelOdChg : updateLastPodList){
				excelOdChg.put("podStatus", excelOdChg.get("lastPodStatus"));
				excelOdChg.put("podDate", excelOdChg.get("lastPodDateTime"));
				excelOdChg.put("odJobNo", excelOdChg.get("lastPodOdJobNo"));
				excelOdChg.put("noCheckLastPodStatus", "Y");
				orderBatchMapper.updateOrderLastPod(excelOdChg);
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
		
		return orderSaveResult;
	}
	
	public int excelUploadInsertPod(List<Map<String, Object>> podList, OrderMapper orderBatchMapper, CommonMapper commonBatchMapper, SqlSession sqlSession, List<Map<String, Object>> updateLastPodList
			,int seq) throws Exception{
		int cnt = 1;
		for(Map<String, Object> excelOdChg : podList){
			orderBatchMapper.deleteOrderPod(excelOdChg);
			if( (cnt++ % 1000) == 0){
				sqlSession.flushStatements();
				sqlSession.clearCache();
			}
		}
		
		cnt = 1;
		for(Map<String, Object> excelOdChg : podList){
			excelOdChg.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			commonBatchMapper.insertPod(excelOdChg);
			if(!Util.isEmpty(excelOdChg.get("lastPodStatus"))){
				updateLastPodList.add(excelOdChg);
			}
			if( (cnt++ % 1000) == 0){
				sqlSession.flushStatements();
				sqlSession.clearCache();
			}
		}
		return seq;
	}
	
	public OrderSaveResult excelUploadEmsTs(ExcelUploadOdChg excelUploadOdChg, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		if(excelUploadOdChg.getDataList() == null) {
			return orderSaveResult;
		}
		
		Map<String, Object> tmpMap;
		Map<String, List<String>> chkMap = new HashMap<String, List<String>>();
		List<String> chkList = new ArrayList<String>();
		List<Map<String, Object>> cntrList;
		Map<String, Object> dataMap = excelUploadOdChg.getDataMap();
		List<Map<String, Object>> dataList = excelUploadOdChg.getDataList();
		String userCustCode = Util.null2str(dataMap.get("ecmCustomerCode"));
		String custCodeLv1 = Util.null2str(dataMap.get("CUST_CODE_LV1"));
		String headEntBusCode = Util.null2str(dataMap.get("headEntBusinessCode"));
		String entCode = excelUploadOdChg.getEntCode();
		int custLv = Util.null2int(dataMap.get("ecmCustomerLevel"));
		int masterMaxHsn = Util.null2int(dataMap.get("MAX_HSN"));
		String custCode;
		boolean mbl = false, finish = false;
		if(!Util.isEmpty(dataMap.get("OD_JOB_NO"))){
			mbl = true;
		}else{
			tmpMap = dataList.get(0);
			dataMap.put("MBL_NO", tmpMap.get("MBL_NO"));
			dataMap.put("VESSEL_FLIGHT", tmpMap.get("VESSEL_FLIGHT"));
			dataMap.put("OB_DATE", tmpMap.get("OB_DATE"));
			dataMap.put("AR_DATE", tmpMap.get("AR_DATE"));
			dataMap.put("ORIG_CODE", tmpMap.get("ORIG_CODE"));
			dataMap.put("DEST_CODE", tmpMap.get("DEST_CODE"));
		}
		
		if(Util.isEmpty(dataMap.get("DEST_PORT"))){
			dataMap.put("DEST_PORT", dataMap.get("DEST_CODE"));
		}
		
		String mblNo = Util.null2str(dataMap.get("MBL_NO"));
		StringBuffer errMsg = new StringBuffer();
		
		if(masterMaxHsn + dataList.size() > 9999){
			errMsg.append("최대 콘솔 가능 건수(9,999)를 초과하였습니다.<br />");
		}
		if(Util.null2int(dataMap.get("SIMPLE_EDI_SEND_CNT")) > 0){
			errMsg.append(mblNo + " : 통관목록전송이력이 존재합니다.<br />");
		}
		
		for(int i = 0; i < dataList.size(); i += 1000){
			if(i + 1000 < dataList.size()){
				dataMap.put("chkList", dataList.subList(i, i + 1000));
			}else{
				dataMap.put("chkList", dataList.subList(i, dataList.size()));
			}
			chkList.addAll(orderMapper.selectHouseOrderEmsTs(dataMap));
		}
		for(String hblNo : chkList){
			errMsg.append(hblNo + " : 중복되는 데이터가 있습니다.<br />");
		}
		dataMap.remove("chkList");
		
		String cntrNo1, cntrNo2, hblNo;
		tmpMap = new HashMap<String, Object>();
		for(Map<String, Object> map : dataList){
			custCode = Util.null2str(map.get("CUST_CODE"), userCustCode);
			if(custCode.equals("")){
				errMsg.append(Util.null2str(map.get("HBL_NO")) + " : AgentCode를 입력하세요.<br />");
				continue;
			}else{
				hblNo = Util.null2str(map.get("HBL_NO"));
				if(tmpMap.get(hblNo) == null){
					tmpMap.put(hblNo, "");
				}else{
					errMsg.append(Util.null2str(map.get("HBL_NO")) + " : 엑셀에 중복되는 BL No가 있습니다.<br />");
				}
				chkList = chkMap.get(custCode);
				if(chkList == null){
					map.put("entCode", entCode);
					map.put("customerCode", custCode);
					chkList = commonMapper.selectCustCode(map);
					if(custLv != 0){
						if(chkList.size() < custLv || !chkList.get(custLv - 1).equals(userCustCode)){
							chkList = new ArrayList<String>();
						}
					}
				}
				if(chkList.size() == 0){
					errMsg.append(Util.null2str(map.get("HBL_NO")) + " : 권한이 없는 AgentCode입니다.<br />");
					continue;
				}
				if(mbl && !chkList.get(0).equals(custCodeLv1)){
					errMsg.append(Util.null2str(map.get("HBL_NO")) + " : 마스터의 최상위 업체와 다른 AgentCode입니다.<br />");
					continue;
				}
			}
			
			cntrNo1 = null;
			cntrNo2 = null;
			cntrList = (List<Map<String, Object>>)map.get("cntrList");
			if(cntrList != null){
				for(Map<String, Object> cntr : cntrList){
					if(cntrNo1 != null){
						if(cntrNo1.equals(cntr.get("CNTR_NO"))){
							errMsg.append(Util.null2str(map.get("HBL_NO")) + " : Container No가 중복되었습니다.<br />");
							break;
						}
						if(cntrNo2 != null){
							if(cntrNo2.equals(cntr.get("CNTR_NO"))){
								errMsg.append(Util.null2str(map.get("HBL_NO")) + " : Container No가 중복되었습니다.<br />");
							}
						}else{
							cntrNo2 = Util.null2str(cntr.get("CNTR_NO"));
						}
					}else{
						cntrNo1 = Util.null2str(cntr.get("CNTR_NO"));
					}
				}
			}
			
			if(errMsg.length() == 0){
				for(int i = 0; i < chkList.size(); i++){
					map.put("CUST_CODE_LV" + (i + 1), chkList.get(i));
				}
			}
		}
		
		if(errMsg.length() != 0){
			orderSaveResult.setResultCode("dataErr");
			Map<String, Object> errMap = new HashMap<String, Object>();
			errMap.put("errStr", errMsg);
			orderSaveResult.setResultData(errMap);
			return orderSaveResult;
		}
		
		dataMap.put("gmtTime", excelUploadOdChg.getGmtTime());
		dataMap.put("entCode", entCode);
		dataMap.put("entBusinessCode", "");
		dataMap.put("updateUserId", excelUploadOdChg.getUpdateUserId());
		dataMap.put("solution", "ECM");
		dataMap.put("stockType", "OD");
		dataMap.put("stockSeqLength", 8);
		dataMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		dataMap.put("srchEntCode", entCode);
		dataMap.put("srchEntBusinessCode", headEntBusCode);
		dataMap.put("srchBound", dataMap.get("BOUND"));
		dataMap.put("srchEdiCityPort", dataMap.get("DEST_PORT"));
		
		Util.accessControlDatabase(dataMap, "gmtTime|stockDateFormat", false, null, null, null, null);
		
		if("Y".equals(dataMap.get("FINISH_YN"))){
			finish = true;
			/** 제출번호 */
			if(!Util.isEmpty(dataMap.get("SIMPLE_DOC_NO"))){
				dataMap.put("simpleDocNo", dataMap.get("SIMPLE_DOC_NO"));
			}else if(Util.null2str(dataMap.get("SIMPLE_DOC_NO_MASTER")).startsWith("AE")){
				dataMap.put("simpleDocNo", dataMap.get("SIMPLE_DOC_NO_MASTER"));
			}else{
				Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(dataMap);
				if(busEdiEcmMap != null && !Util.isEmpty(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"))){
					dataMap.put("stockSign", busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"));
					dataMap.put("signType", "noHyphen");
					dataMap.put("stockRemark", "ORDER SIMPLE DOC NO");
					dataMap.put("dateLength", "2");
					dataMap.put("stockDateFormat", "12");
					dataMap.put("simpleDocNo", commonMapper.selectNextStockMaxNo(dataMap));
				}
			}
			dataMap.put("lastPodStatus", "ECM055");
		}else{
			finish = false;
			dataMap.put("FINISH_YN", "N");
			dataMap.put("lastPodStatus", "ECM010");
		}
		
		/** 환율계산 */
		dataMap.put("srchDateFormat", "23");
		dataMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
		if(!Util.isEmpty(dataMap.get("AR_DATE"))){
			dataMap.put("srchDate", dataMap.get("AR_DATE"));
		}else if(!Util.isEmpty(dataMap.get("PO_DATE"))){
			dataMap.put("srchDate", dataMap.get("PO_DATE"));
		}else{
			dataMap.put("srchDate", Util.getNow("yyyy-MM-dd"));
		}
		dataMap.put("entBusinessCode", headEntBusCode);
		Util.accessControlDatabase(dataMap, "exrateType|srchDateFormat|gmtTime|dateFormat", false, null, null, null, null);
		Map<String, Object> exrateMap = commonMapper.selectExchangeRate(dataMap);
		dataMap.put("entBusinessCode", "");
		
		if("".equals(mblNo)){
			dataMap.put("consolYn", "N");
		}else{
			dataMap.put("consolYn", "Y");
		}
		
		dataMap.put("stockSign", "COD");
		dataMap.put("signType", null);
		dataMap.put("stockRemark", "ORDER-JOB NO");
		dataMap.put("dateLength", null);
		dataMap.put("stockDateFormat", null);
		dataMap.put("dateFormat", "23");
		
		int seqNo = 1;
		
		if(!mbl && !Util.isEmpty(dataMap.get("MBL_NO"))){	/** 신규마스터생성 */
			/** 중계사업자 */
			dataMap.put("entBusinessCode", headEntBusCode);
			dataMap.put("solution", "ECM");
			if("AIR".equals(dataMap.get("BOUND"))){
				dataMap.put("optionCode", "C02");
				tmpMap = commonMapper.selectSystemOption2(dataMap);
				/** 항공일경우 중계사업자 설정값이 auto이면 편명의 중계사업자로 저장 */
				if("AUTO".equals(Util.null2str(tmpMap.get("SET_VALUE")).toUpperCase())){
					String venderId = "";
					if(!Util.isEmpty(dataMap.get("LINE_CODE"))){
						dataMap.put("groupCode", "AIRLINE_CODE");
						dataMap.put("srchEtcFieldOnly", "Y");
						dataMap.put("srchEtcField", "ETC2");
						dataMap.put("srchEtcFieldValue", dataMap.get("LINE_CODE"));
						tmpMap = commonMapper.getCodeDetail(dataMap);
						if(tmpMap != null){
							venderId = Util.null2str(tmpMap.get("ETC11"));
						}
					}
					if(venderId.equals("")){
						dataMap.put("VENDER_ID_1", Util.null2str(tmpMap.get("DEFAULT_VALUE")));
					}else{
						dataMap.put("VENDER_ID_1", venderId);
					}
				}else if(Util.isEmpty(tmpMap.get("SET_VALUE"))){
					dataMap.put("VENDER_ID_1", tmpMap.get("DEFAULT_VALUE"));
				}else{
					dataMap.put("VENDER_ID_1", tmpMap.get("SET_VALUE"));
				}
			}else{
				dataMap.put("optionCode", "C03");
				dataMap.put("VENDER_ID_1", commonMapper.selectSystemOption(dataMap));
			}
			
			/** 관세청사서함 */
			dataMap.put("srchBoundType", "IMP");
			tmpMap = commonMapper.selectBusEdiEcmInfo(dataMap);
			if(tmpMap != null && tmpMap.size() > 0){
				dataMap.put("EDI_MAILBOX_SEQ_NO", tmpMap.get("SEQ_NO"));
			}
			
			/** 세관/과, 하기장소, 보세창고 */
			SearchManifestCustoms searchCustoms = new SearchManifestCustoms();
			searchCustoms.setBoundType("IMP");
			searchCustoms.setSrchBusEdiCodeYn("Y");
			searchCustoms.setEntCode(entCode);
			searchCustoms.setEntBusinessCode(headEntBusCode);
			searchCustoms.setBound(Util.null2str(dataMap.get("BOUND")));
			searchCustoms.setPortCode(Util.null2str(dataMap.get("DEST_PORT")));
			tmpMap = manifestMapper.getCustomsCode(searchCustoms);
			dataMap.put("CUSTOMS_CODE", tmpMap.get("CUSTOMS_CODE"));
			dataMap.put("CUSTOMS_DPT_CODE", tmpMap.get("CUSTOMS_DPT_CODE"));
			if("AIR".equals(dataMap.get("BOUND"))){
				dataMap.put("DISC_WH_CODE", tmpMap.get("EDI_DISC_WH_CODE"));
				dataMap.put("ASSIGNMENT_WH_CODE", tmpMap.get("EDI_ASSIGNMENT_WH_CODE"));
			}else{
				dataMap.put("ASSIGNMENT_WH_CODE", tmpMap.get("EDI_WH_CODE"));
			}
			
			/** 국가코드 */
			dataMap.put("groupCode", "CITY_PORT");
			dataMap.put("userLanguage", "ENG");
			if(!Util.isEmpty(dataMap.get("ORIG_CODE"))){
				dataMap.put("srchCode", dataMap.get("ORIG_CODE"));
				tmpMap = commonMapper.getCodeDetail(dataMap);
				if(tmpMap != null){
					dataMap.put("ORIG_NAME", tmpMap.get("CODE_NAME"));
					dataMap.put("ORIG_COUNTRY_OD", tmpMap.get("ETC2"));
				}
			}
			if(!Util.isEmpty(dataMap.get("DEST_CODE"))){
				dataMap.put("srchCode", dataMap.get("DEST_CODE"));
				tmpMap = commonMapper.getCodeDetail(dataMap);
				if(tmpMap != null){
					dataMap.put("DEST_NAME", tmpMap.get("CODE_NAME"));
					dataMap.put("DEST_COUNTRY_OD", tmpMap.get("ETC2"));
				}
			}
			dataMap.remove("userLanguage");
			
			dataMap.put("entBusinessCode", "");
			dataMap.put("odJobNo", commonMapper.selectNextStockMaxNo(dataMap));
			dataMap.put("MASTER_OD_JOB_NO", dataMap.get("odJobNo"));
			dataMap.put("blTypeMh", "M");
			dataMap.put("CUST_CODE_LV1", dataList.get(0).get("CUST_CODE_LV1"));
			orderMapper.insertOrderEmsTsExcel(dataMap);
		}
		
		/** POD */
		Map<String, Object> podMap = new HashMap<String, Object>();
		podMap.put("entCode", entCode);
		podMap.put("entBusinessCode", "");
		podMap.put("gmtTime", dataMap.get("gmtTime"));
		podMap.put("solution", "ECM");
		podMap.put("updateUserId", dataMap.get("updateUserId"));
		podMap.put("stockType", "POD");
		podMap.put("stockSign", "POD");
		podMap.put("stockSeqLength", 8);
		podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		podMap.put("stockRemark", "ECM POD JOB NO");
		podMap.put("podTableName", "TB_ECM_POD_01");
		podMap.put("podStatus", "ECM010");
		
		dataMap.put("blTypeMh", "H");
		
		String currencyCode;
		double exrateUsd, exchangeRate, amountCur, amountLoc, gwt;
		tmpMap = new HashMap<String, Object>();
		Map<String, Object> cntrMap = new HashMap<String, Object>();
		String cntrCode;
		dataMap.put("groupCode", "CONTAINER");
		for(Map<String, Object> map : dataList){
			map.putAll(dataMap);
			if(finish){
				map.put("hsn", Util.lpad(Util.null2str(masterMaxHsn + seqNo), 4, '0'));
			}
			currencyCode = Util.null2str(map.get("CURRENCY_CODE"));
			if(exrateMap != null && !currencyCode.equals("")) {
				exrateUsd = Util.null2dbl(exrateMap.get("EXCHANGE_RATE_USD"));
				exchangeRate = Util.null2dbl(exrateMap.get("EXCHANGE_RATE_" + currencyCode));
				amountCur = Util.null2dbl(map.get("AMOUNT_CUR"));
				amountLoc = amountCur * exchangeRate;
				map.put("exchangeRate", exchangeRate);
				map.put("amountLoc", amountLoc);
				if(currencyCode.equals("USD")){
					map.put("amountUsd", amountCur);
				}else if(exrateUsd!= 0){
					map.put("amountUsd", Util.null2dbl(amountLoc / exrateUsd, 2, "U"));
				}else{
					map.put("amountUsd", 0);
				}
			}else{
				map.put("exchangeRate", 0);
				map.put("amountLoc", 0);
				map.put("amountUsd", 0);
			}
			
			gwt = Util.null2dbl(map.get("GWT"));
			map.put("gwtLbs", gwt * 2.20462262);
			map.put("gwtEms", gwt * 1000);
			map.put("emsBundleNo", Util.null2str(map.get("GROUP_SHIPPING_DATE")) + Util.null2str(map.get("GROUP_AGENT_CODE")) + Util.null2str(map.get("GROUP_EXCHANGE_OFFICE")) + Util.null2str(map.get("GROUP_BAG_NUMBER")) + Util.null2str(map.get("GROUP_BAG_WEIGHT")) + Util.null2str(map.get("GROUP_AMOUNT")));
			
			/** CNTR_EDI_CODE */
			cntrList = (List<Map<String, Object>>)map.get("cntrList");
			if(cntrList != null){
				for(Map<String, Object> cntr : cntrList){
					cntrCode = Util.null2str(cntr.get("CNTR_CODE"));
					if(tmpMap.get(cntrCode) == null){
						dataMap.put("srchCode", cntrCode);
						cntrMap = commonMapper.getCodeDetail(dataMap);
						if(cntrMap != null){
							tmpMap.put(cntrCode, Util.null2str(cntrMap.get("ETC3")));
						}else{
							tmpMap.put(cntrCode, "");
						}
					}
					cntr.put("cntrEdiCode", tmpMap.get(cntrCode));
				}
			}
			
			dataMap.put("seqNo", seqNo++);
			map.put("odJobNo", commonMapper.selectNextStockMaxNo(dataMap));
		}
		
		SqlSession sqlSession = null;
		int cnt = 1;
		int seq = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			for(Map<String, Object> map : dataList){
				map.put("entCode", entCode);
				map.put("entBusinessCode", headEntBusCode);
				
				orderBatchMapper.insertOrderEmsTsExcel(map);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			sqlSession.flushStatements();
			sqlSession.clearCache();
			
			cnt = 1;
			for(Map<String, Object> map : dataList){
				podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				podMap.put("odJobNo", map.get("odJobNo"));
				podMap.put("blNo", map.get("HBL_NO"));
				orderBatchMapper.insertOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			if(finish){
				cnt = 1;
				podMap.put("podStatus", "ECM055");
				for(Map<String, Object> map : dataList){
					podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					podMap.put("odJobNo", map.get("odJobNo"));
					podMap.put("blNo", map.get("HBL_NO"));
					orderBatchMapper.insertOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
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
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("job_func_type", "ADD");
		tmpLogMap.put("ent_code", entCode);
		tmpLogMap.put("ent_business_code", excelUploadOdChg.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("bound", dataMap.get("BOUND"));
		tmpLogMap.put("bound_type", "IMP");
		tmpLogMap.put("process_user_id", excelUploadOdChg.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("ent_business_print_name", excelUploadOdChg.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", excelUploadOdChg.getLogUserName());
		tmpLogMap.put("process_user_name_eng", excelUploadOdChg.getLogUserNameEng());		
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		seq = 1;
		for(Map<String, Object> map : dataList){
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("job_no", map.get("odJobNo"));
			logMap.put("job_log", new Gson().toJson(map));
			logMap.put("description","NEW ORDER(EMS EXCEL) [HBL_NO : "+ Util.null2str(map.get("HBL_NO")) +"]");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}
				
		commonService.insertLogBulk(bulkList);
		
		return orderSaveResult;
	}
	
	public OrderSaveResult excelUploadUpdateLicense(ExcelUploadOdChg excelUploadOdChg, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		List<String> odJobNoList = new ArrayList<>();
		Map<String, Object> odJobNo = new HashMap<String, Object>();
		List<String> odJobNoDataList = new ArrayList<>();
		
		if(excelUploadOdChg.getDataList() == null) {
			return orderSaveResult;
		}
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Util.accessControlDatabase(excelUploadOdChg, "gmtTime", false, null, null, null, null);
		SqlSession sqlSession = null;
		int cnt = 1;
		int seq = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", excelUploadOdChg.getEntCode());
			tmpLogMap.put("ent_business_code", excelUploadOdChg.getEntBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", excelUploadOdChg.getUpdateUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "EXCEL");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", excelUploadOdChg.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", excelUploadOdChg.getLogUserName());
			tmpLogMap.put("process_user_name_eng", excelUploadOdChg.getLogUserNameEng());
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				Map<String, Object> selectOdJobNo = new HashMap<String, Object>();
				selectOdJobNo.put("entCode", excelUploadOdChg.getEntCode());
				selectOdJobNo.put("hblNo", excelOdChg.get("HBL_NO"));
				odJobNo = orderBatchMapper.selectOdJobNoByHblNo(selectOdJobNo);
				odJobNoList.add(Util.null2str(odJobNo.get("OD_JOB_NO")));
				excelOdChg.put("odJobNo", Util.null2str(odJobNo.get("OD_JOB_NO")));
			}
			
			if(odJobNoList != null && odJobNoList.size() > 0) {
				odJobNoDataList = odJobNoList.stream().distinct().collect(Collectors.toList());
				for(String deleteLicense : odJobNoDataList) {
					Map<String, Object> deleteLicenseNo = new HashMap<String, Object>();
					deleteLicenseNo.put("odJobNo", deleteLicense);
					orderBatchMapper.deleteLicense(deleteLicenseNo);
					if((cnt++ % 100) == 0) {  // 100건 단위
						sqlSession.flushStatements();
				        sqlSession.clearCache();
					}
				}
				sqlSession.flushStatements();
			}
			
			cnt = 1;
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				excelOdChg.put("entCode", excelUploadOdChg.getEntCode());
				excelOdChg.put("hblNo", excelOdChg.get("HBL_NO"));
				excelOdChg.put("odJobNoList", odJobNoDataList);
				excelOdChg.put("odJobNo", excelOdChg.get("odJobNo"));
				excelOdChg.put("licenseNo", excelOdChg.get("LICENSE_NO"));
				excelOdChg.put("pieces", excelOdChg.get("PIECES"));
				excelOdChg.put("gwt", excelOdChg.get("GWT"));
				excelOdChg.put("combineYn", excelOdChg.get("COMBINE_YN"));
				excelOdChg.put("combineTypeAZ", excelOdChg.get("COMBINE_TYPE_A_Z"));
				excelOdChg.put("combinePieces", excelOdChg.get("COMBINE_PIECES"));
				excelOdChg.put("partialYn", excelOdChg.get("PARTIAL_YN"));
				excelOdChg.put("partialSeq", excelOdChg.get("PARTIAL_SEQ"));
				excelOdChg.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				excelOdChg.put("gmtTime", excelUploadOdChg.getGmtTime());
				
				orderBatchMapper.insertLicenseExcel(excelOdChg);
				
				// 처리 로그 기록
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("bound", excelUploadOdChg.getBound());
				logMap.put("bound_type", "EXP");
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", "");
				logMap.put("job_log", "");
				logMap.put("description", "엑셀 이행신고 수정 [HBL NO : " + excelOdChg.get("HBL_NO") + "]");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
			// 처리 로그 기록
			commonService.insertLogBulk(bulkList);
			
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
			
		return orderSaveResult;
		
	}
	
	public OrderSaveResult excelUploadCarryIn(ExcelUploadOdChg excelUploadOdChg, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		List<Order> logList = new ArrayList<Order>();
		
		if(excelUploadOdChg.getDataList() == null) {
			return orderSaveResult;
		}
		Util.accessControlDatabase(excelUploadOdChg, "gmtTime", false, null, null, null, null);
		SqlSession sqlSession = null;
		int cnt = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				excelOdChg.put("entCode", excelUploadOdChg.getEntCode());
				excelOdChg.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				excelOdChg.put("gmtTime", excelUploadOdChg.getGmtTime());
				excelOdChg.put("bound", excelUploadOdChg.getBound());
				excelOdChg.put("boundType", excelUploadOdChg.getBoundType());
				orderBatchMapper.updateExcelUploadCarryin(excelOdChg);
				logList.addAll(orderMapper.selectCarryinList(excelOdChg));
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
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", excelUploadOdChg.getEntCode());
		tmpLogMap.put("ent_business_code", excelUploadOdChg.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", excelUploadOdChg.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		
		// 사업장명 가져오기
		if (!"".equals(Util.null2str(excelUploadOdChg.getEntBusinessCode()))) {
			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(excelUploadOdChg.getEntBusinessCode());
			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
		}
		
		// 사용자명 가져오기
		if (!"".equals(Util.null2str(excelUploadOdChg.getUpdateUserId()))) {
			User user = smsUserService.getUser(excelUploadOdChg.getUpdateUserId());
			tmpLogMap.put("process_user_name", user.getUserName());
			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
		}
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		int seq = 1;
		for(Order od : logList) {
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("bound", od.getBound());
			logMap.put("bound_type", od.getBoundType());
			logMap.put("job_no", od.getOdJobNo());
//			logMap.put("job_log", new Gson().toJson(excelUploadOdChg));
			logMap.put("job_log", "");
			logMap.put("description","Order carryinNo change [MBL_NO : "+ od.getMblNo() + "]"+" [HBL_NO : "+ od.getHblNo() +"]");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}
				
		commonService.insertLogBulk(bulkList);
		
		return orderSaveResult;
	}
	
	/**
	 * 일괄변경 대상 건수조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult batchTargetCount(SearchOrderBatchUpdate search, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		if(search.getCheckDataList() != null){
			Map<String, Object> map = new HashMap<String, Object>();
			if("Y".equals(search.getCheckMfSubGridYn())){
				map.put("checkMfSubGridYn", "Y");
				map.put("checkMfSubGridDataList", search.getCheckMfSubGridDataList());
			}else{
				map.put("checkDataList", search.getCheckDataList());
			}
			orderSaveResult.setResultCode(orderMapper.selectBatchTargetCountChk(map));
		}else{
			//등록,발송 화면에서 호출했을 경우 orderCustomerMapper
			if(Util.null2str(search.getScreen()).equals("ORDER_CUSTOMER")) {
				SearchOrderCustomerList searchOrderList = search.getSearchOrderCustomerList();
				/* N개 검색(멀티타입) */
				List<String> searchNoList = new ArrayList<String>();
				if(!"".equals(Util.null2str(searchOrderList.getSrchNo()))) {
					searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(searchOrderList.getSrchNo())).split(",")));
				}
				
				if(searchNoList.size() > 1){
					searchOrderList.setSearchType("multi");
					searchOrderList.setSearchNoList(searchNoList);
				}else{
					searchOrderList.setSearchType("");
					searchOrderList.setSrchNo(Util.null2str(searchOrderList.getSrchNo()).trim().replace(",", ""));
				}
				
				Util.getCryptographyYn(searchOrderList);
				Util.accessControlDatabase(searchOrderList, "loginCustomerLev|srchDateFormat|loginCustomerUpperLev|loginCustomerLowLev", false, "srchDateGbn", "od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.CONSIGNEE_HP|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
				orderSaveResult.setResultCode(orderCustomerMapper.selectBatchTargetCount(searchOrderList));
			//그 외 화면에서 호출했을 경우 orderMapper
			}else {
				SearchOrderList searchOrderList = search.getSearchOrderList();
				/* N개 검색(멀티타입) */
				List<String> searchNoList = new ArrayList<String>();
				if(!"".equals(Util.null2str(searchOrderList.getSrchNo()))) {
					searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(searchOrderList.getSrchNo())).split(",")));
				}
				
				if(searchNoList.size() > 1){
					searchOrderList.setSearchType("multi");
					searchOrderList.setSearchNoList(searchNoList);
				}else{
					searchOrderList.setSearchType("");
					searchOrderList.setSrchNo(Util.null2str(searchOrderList.getSrchNo()).trim().replace(",", ""));
				}
				
				String srchQueryType = Util.null2str(searchOrderList.getSrchQueryType());
				if(srchQueryType.equals("ORDER")){
					Util.getCryptographyYn(searchOrderList);
					Util.accessControlDatabase(searchOrderList, "srchDateFormat|srchCusType", false, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|od.REGISTER_DATE_TIME|ad.SIMPLE_DOC_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO|od.RETURN_DELIVERY_NO|od.SHIPMENT_BOX_ID|ad.RETURN_REQUEST_ID");
				}else if(srchQueryType.equals("MANIFEST_IMP")){
					Util.accessControlDatabase(searchOrderList, "srchDateFormat", false, "srchDateGbn", "noDateChk|ad.SIMPLE_DOC_DATE|od.AR_DATE|od.OB_DATE|ad.MF_DATE", "srchNoType", "od.HBL_NO|od.MBL_NO|od.MRN|ad.SIMPLE_DOC_NO|od.MASTER_OD_JOB_NO");
				}else if(srchQueryType.equals("MANIFEST_EXP")){
					Util.accessControlDatabase(searchOrderList, "srchDateFormat", false, "srchDateGbn", "od.AR_DATE|od.OB_DATE", "srchNoType", "od.HBL_NO|od.MBL_NO|od.MASTER_OD_JOB_NO");
				}else if(srchQueryType.equals("IN_OUT")){
					Util.getCryptographyYn(searchOrderList);
					Util.accessControlDatabase(searchOrderList, "srchCusType|srchOrigDestType|srchDateFormat", false, "srchDateGbn", "od.AR_DATE|CARRYIN_DATE|CARRYOUT_DATE|STOCK_DATE_TIME|WH_CARRYOUT_DATE", "srchNoType", "PO_NO|HBL_NO|MBL_NO|CONSIGNEE_NAME|CONSIGNEE_NAME_LOC|CONSIGNEE_TEL|SHIPPER_NAME|SHIPPER_TEL|COMMODITY_ITEM");
				}else if(srchQueryType.equals("MANIFEST_IMP_031")){
					Util.accessControlDatabase(searchOrderList, "srchDateFormat", false, "srchDateGbn", "noDateChk|ad.SIMPLE_DOC_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.HBL_NO|od.MBL_NO|od.MRN|ad.SIMPLE_DOC_NO");
				}
				orderSaveResult.setResultCode(orderMapper.selectBatchTargetCount(searchOrderList));
			}
		}
		
		
		
		return orderSaveResult;
	}
	
	/**
	 * Order List(Detail) - 일괄변경
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public OrderSaveResult batchUpdate(SearchOrderBatchUpdate search, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		String targetData = Util.null2str(search.getTargetData());
		Map batchUpdateMap = new HashMap<String,Object>();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Util.accessControlDatabase(search, "updateField|updateField2|gmtTime", false, null, null, null, null);
		
		if(targetData.equals("srchData")){
			SearchOrderList searchOrderList = search.getSearchOrderList();
			/* N개 검색(멀티타입) */
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(Util.null2str(searchOrderList.getSrchNo()))) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(searchOrderList.getSrchNo())).split(",")));
			}
			
			if(searchNoList.size() > 1){
				searchOrderList.setSearchType("multi");
				searchOrderList.setSearchNoList(searchNoList);
			}else{
				searchOrderList.setSearchType("");
				searchOrderList.setSrchNo(Util.null2str(searchOrderList.getSrchNo()).trim().replace(",", ""));
			}
			
			batchUpdateMap = BeanUtils.describe(searchOrderList);
			batchUpdateMap.put("updateField", search.getUpdateField());
			batchUpdateMap.put("updateValue", search.getUpdateValue());
			batchUpdateMap.put("updateField2", search.getUpdateField2());
			batchUpdateMap.put("updateValue2", search.getUpdateValue2());
			batchUpdateMap.put("gmtTime", search.getGmtTime());
			batchUpdateMap.put("updateUserId", search.getUpdateUserId());
			batchUpdateMap.put("srchCdTypeList", searchOrderList.getSrchCdTypeList());
			batchUpdateMap.put("srchClearanceStatusList", searchOrderList.getSrchClearanceStatusList());
			batchUpdateMap.put("srchLastPodStatusList", searchOrderList.getSrchLastPodStatusList());
			batchUpdateMap.put("srchCustomsIdVerifyList", searchOrderList.getSrchCustomsIdVerifyList());
			batchUpdateMap.put("srchCarryList", searchOrderList.getSrchCarryList());
			batchUpdateMap.put("srchHoldMap", search.getHoldMap());
			if(searchOrderList.getSearchType().equals("multi")){
				batchUpdateMap.put("searchNoList", searchOrderList.getSearchNoList());
			}
		}else {
			batchUpdateMap.put("updateField", search.getUpdateField());
			batchUpdateMap.put("checkDataTypeMh", search.getCheckDataTypeMh());
			if("Y".equals(search.getCheckMfSubGridYn())){
				batchUpdateMap.put("checkMfSubGridYn", "Y");
				batchUpdateMap.put("dataList", search.getCheckMfSubGridDataList());
			}else{
				batchUpdateMap.put("dataList", search.getCheckDataList());
			}
		}
		batchUpdateMap.put("updateType", search.getUpdateType());
		batchUpdateMap.put("boundType", search.getBoundType());
		
		Util.getCryptographyYn(search, "updateField", "updateCryptographyYn");
		batchUpdateMap.put("updateCryptographyYn", search.getUpdateCryptographyYn());
		batchUpdateMap.put("cryptographyYn", search.getCryptographyYn());
		
		if(batchUpdateMap.get("dataList") != null || targetData.equals("srchData")){
			String srchQueryType = Util.null2str(batchUpdateMap.get("srchQueryType"));
			if(srchQueryType.equals("ORDER")){
				Util.getCryptographyYn(search.getSearchOrderList());
				batchUpdateMap.put("dynamicCryptographyYn", search.getSearchOrderList().getDynamicCryptographyYn());
				Util.accessControlDatabase(batchUpdateMap, "srchDateFormat|srchCusType", false, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|od.REGISTER_DATE_TIME|ad.SIMPLE_DOC_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO|od.RETURN_DELIVERY_NO|od.SHIPMENT_BOX_ID|ad.RETURN_REQUEST_ID");
			}else if(srchQueryType.equals("MANIFEST_IMP")){
				Util.accessControlDatabase(batchUpdateMap, "srchDateFormat", false, "srchDateGbn", "noDateChk|ad.SIMPLE_DOC_DATE|od.AR_DATE|od.OB_DATE|ad.MF_DATE", "srchNoType", "od.HBL_NO|od.MBL_NO|od.MRN|ad.SIMPLE_DOC_NO|od.MASTER_OD_JOB_NO");
			}else if(srchQueryType.equals("MANIFEST_EXP")){
				Util.accessControlDatabase(batchUpdateMap, "srchDateFormat", false, "srchDateGbn", "od.AR_DATE|od.OB_DATE", "srchNoType", "od.HBL_NO|od.MBL_NO|od.MASTER_OD_JOB_NO");
			}else if(srchQueryType.equals("IN_OUT")){
				Util.getCryptographyYn(search.getSearchOrderList());
				batchUpdateMap.put("dynamicCryptographyYn", search.getSearchOrderList().getDynamicCryptographyYn());
				Util.accessControlDatabase(batchUpdateMap, "srchCusType|srchOrigDestType|srchDateFormat", false, "srchDateGbn", "od.AR_DATE|CARRYIN_DATE|CARRYOUT_DATE|STOCK_DATE_TIME|WH_CARRYOUT_DATE", "srchNoType", "PO_NO|HBL_NO|MBL_NO|CONSIGNEE_NAME|CONSIGNEE_NAME_LOC|CONSIGNEE_TEL|SHIPPER_NAME|SHIPPER_TEL|COMMODITY_ITEM");
			}else if(srchQueryType.equals("MANIFEST_IMP_031")){
				Util.accessControlDatabase(batchUpdateMap, "srchDateFormat", false, "srchDateGbn", "noDateChk|ad.SIMPLE_DOC_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.HBL_NO|od.MBL_NO|od.MRN|ad.SIMPLE_DOC_NO");
			}
		}
		
		/** 2025-04-18 목록 => 일반 변경시 수입화물진행정보실행 */
		if("CD_TYPE".equals(search.getUpdateField()) && "N".equals(search.getUpdateValue())){
			batchUpdateMap.put("srchCargoProgress", "Y");
		}
		List<Order> logList = orderMapper.selectOrderListForLog(batchUpdateMap);
		
		if(!"".equals(Util.null2str(search.getUserAuth())) && !"AD".equals(search.getUserAuth())){
			if("CFA_HOLD_YN".equals(search.getUpdateField())){
				for(Order order : logList){
					if(!Util.isEmpty(order.getCfaDate())){
						Map<String, Object> resultData = new HashMap<String, Object>();
						orderSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
						resultData.put("appendMsgCode", "EXPS003681");	/** 지정창고 */
						orderSaveResult.setResultData(resultData);
						return orderSaveResult;
					}
				}
			/** 2025-06-12 통관목록전송건 통관구분 수정불가 */	
//			}else if(!"CARRYIN_NO".equals(search.getUpdateField()) && !"CD_TYPE".equals(search.getUpdateField()) && !"container".equals(search.getUpdateType())){
			}else if(!"CARRYIN_NO".equals(search.getUpdateField()) && !"container".equals(search.getUpdateType())){
				String simpleEdiStatus;
				for(Order order : logList){
					simpleEdiStatus = Util.null2str(order.getSimpleEdiStatus());
					if(!simpleEdiStatus.equals("") && !simpleEdiStatus.equals("ERROR")){
						orderSaveResult.setResultCode("A11333");	/** 통관목록이 전송되어서 처리할 수 없습니다. */
						return orderSaveResult;
					}
				}
			}
		}
		
		if(Util.null2str(search.getUpdateType()).equals("container")){
			boolean divideYn = search.isContainerDivideYn();
			if(divideYn && (targetData.equals("srchData") || search.getCheckDataList().size() > 1 )) {
				orderSaveResult.setResultCode("A11269");	/** 1개의 마스터만 선택해 주세요 */
				return orderSaveResult;
			}
			if(targetData.equals("srchData")){
				orderMapper.deleteContainerBatchBySrchData(batchUpdateMap);
			}else {
				orderMapper.deleteContainerBatch(search);
			}
			if(!Util.null2str(search.getUpdateField()).equals("deleteContainer")){
				boolean piecesYn = search.isContainerPiecesYn();
				if(targetData.equals("srchData") || piecesYn || divideYn){
					List<String> batchUpdateList = null;
					if(targetData.equals("srchData")) {
						if(piecesYn) {
							batchUpdateList = orderMapper.selectBatchUpdateTarget2(search.getSearchOrderList());	/** 검색으로 저장 + 하우스 수량 적용일 경우 대상건 + 수량 + 수량단위 조회 */
						}else {
							batchUpdateList = orderMapper.selectBatchUpdateTarget(search.getSearchOrderList());		/** 검색으로 저장일경우 대상건 조회 */
						}
					}else {
						batchUpdateList = orderMapper.selectBatchUpdatePiecesList(search);		/** 선택건 저장 + 하우스 수량 적용일 경우 수량 + 수량단위 조회 */
					}
					if(batchUpdateList.size() > 0){
						SqlSession sqlSession = null;
						try {
							sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
							OrderMapper orderMapper2 = sqlSession.getMapper(OrderMapper.class);
							List<SearchOrderBatchUpdateCntr> cntrList = search.getContainerList();
							int cnt = 1, startCnt = 0;
							if(divideYn) {
								int cntrCnt = cntrList.size();
								int batchUpdateCnt = batchUpdateList.size();
								int divideCnt = batchUpdateCnt / cntrCnt;
								int remainCnt = batchUpdateCnt % cntrCnt;
								for(int i = 0; i < cntrList.size(); i++) {
									int currentDivideCnt = divideCnt + (i < remainCnt ? 1 : 0);
							        int endCnt = startCnt + currentDivideCnt;
									for(int j = startCnt; j < endCnt; j++){
										if(piecesYn) {
											cntrList.get(i).setOdJobNo(batchUpdateList.get(j).split("\\|")[0]);
											cntrList.get(i).setPieces(Integer.parseInt(batchUpdateList.get(j).split("\\|")[1]));
											cntrList.get(i).setUnitPieces(batchUpdateList.get(j).split("\\|")[2]);
										}else {
											cntrList.get(i).setOdJobNo(batchUpdateList.get(j));
										}
										orderMapper2.insertContainerBatchBySrchData(cntrList.get(i));
										startCnt++;
										if( (cnt++ % 1000) == 0){
											sqlSession.flushStatements();
											sqlSession.clearCache();
										}
									}
									startCnt = endCnt;
								}
							}else {
								for(int i = 0; i < batchUpdateList.size(); i++){
									for(int j = 0; j < cntrList.size(); j++) {
										if(piecesYn) {
											cntrList.get(j).setOdJobNo(batchUpdateList.get(i).split("\\|")[0]);
											cntrList.get(j).setPieces(Integer.parseInt(batchUpdateList.get(i).split("\\|")[1]));
											cntrList.get(j).setUnitPieces(batchUpdateList.get(i).split("\\|")[2]);
										}else {
											cntrList.get(j).setOdJobNo(batchUpdateList.get(i));
										}
										orderMapper2.insertContainerBatchBySrchData(cntrList.get(j));
										if( (cnt++ % 1000) == 0){
											sqlSession.flushStatements();
											sqlSession.clearCache();
										}
									}
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
						/**
						 * order 10000 / container 4 / flushStatements 1000  / 모두실행후 commit : 2308(로그제거)
						 * order 10000 / container 4 / flushStatements 10000 / 모두실행후 commit : 8545
						 * order 10000 / container 4 / flushStatements 1000  / 모두실행후 commit : 8261
						 * order 10000 / container 4 / flushStatements 100   / 모두실행후 commit : 14507
						 * order 10000 / container 4 / flushStatements 1000  / commit 1000 : 13968
						 */
					}
				}else{
					orderMapper.insertContainerBatch(search);		/** 선택건 저장일 경우 */
				}
			}
		}else if(Util.null2str(search.getUpdateField()).equals("poInfoList")){
			if(targetData.equals("srchData")){
				orderMapper.deletePoInfoBatchBySrchData(batchUpdateMap);
			}else {
				orderMapper.deletePoInfoBatch(search);
			}
			
			if(targetData.equals("srchData")){
				List<String> batchUpdateList = orderMapper.selectBatchUpdateTarget(search.getSearchOrderList());		/** 검색으로 저장일경우 대상건 조회 */
				if(batchUpdateList.size() > 0){
					SqlSession sqlSession = null;
					try {
						sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
						OrderMapper orderMapper2 = sqlSession.getMapper(OrderMapper.class);
						Map<String, Object> poInfo = new HashMap<String, Object>();
						poInfo.put("poInfoList", search.getPoInfoList());
						int cnt = 1;
						for(int i = 0; i < batchUpdateList.size(); i++){
							poInfo.put("odJobNo", batchUpdateList.get(i));
							orderMapper2.insertPoInfo(poInfo);
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
				}
			}else{
				orderMapper.insertPoInfoBatch(search);
			}
		}else if(Util.null2str(search.getUpdateType()).equals("licenseNo")){
			if(targetData.equals("srchData")){
				orderMapper.deleteLiceNoInfoBatchBySrchData(batchUpdateMap);
			}else {
				orderMapper.deleteLiceNoInfoBatch(search);
			}
			
			if(targetData.equals("srchData")){
				List<String> batchUpdateList = new ArrayList<String>(); 
				if("clearGener".equals(search.getUpdateField())) {
					batchUpdateList = orderMapper.selectBatchUpdateLiceTarget1(search.getSearchOrderList());		/** 검색으로 저장일경우 대상건 조회 */
				}else {
					batchUpdateList = orderMapper.selectBatchUpdateLiceTarget2(search.getSearchOrderList());		/** 검색으로 저장일경우 대상건 조회 */
				}
				if(batchUpdateList.size() > 0){
					SqlSession sqlSession = null;
					try {
						sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
						OrderMapper orderMapper2 = sqlSession.getMapper(OrderMapper.class);
						Map<String, Object> liceInfo = new HashMap<String, Object>();
						int cnt = 1;
						for(int i = 0; i < batchUpdateList.size(); i++){
							liceInfo.put("odJobNo", batchUpdateList.get(i));
							orderMapper2.insertLicenseInfo(liceInfo);
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
				}
			}else{
				orderMapper.insertLiceInfoBatch(search);
			}
		}else if("deleteSimpleDocNo".equals(search.getUpdateType())){
			
			String simpleEdiStatus;
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			for(Order order : logList){
				simpleEdiStatus = Util.null2str(order.getSimpleEdiStatus());
				if(!simpleEdiStatus.equals("") && !simpleEdiStatus.equals("ERROR")){
					orderSaveResult.setResultCode("A11301");	/** 통관목록신고후에는 제출번호를 변경할 수 없습니다. */
					return orderSaveResult;
				}
				tmpMap.put(order.getSimpleDocNo(), "");
			}
			if(targetData.equals("srchData")){
				batchUpdateMap.put("srchBlTypeMh", "notCheck");
				orderMapper.updateOrderAdditionalBatchBySrchData(batchUpdateMap);
			}else{
				orderMapper.updateOrderAdditionalBatch(search);
				
				/** 제출번호 제거시 마스터의 제출번호와 같은 제출번호의 하우스가 없을경우 마스터 제출번호 제거 */
				if("H".equals(search.getCheckDataTypeMh()) && logList.size() > 0){
					List<String> simpleDocNoList = new ArrayList<String>();
					for(String simpleDocNo : tmpMap.keySet()){
						simpleDocNoList.add(simpleDocNo);
					}
					tmpMap.clear();
					tmpMap.put("masterOdJobNo", logList.get(0).getMasterOdJobNo());
					tmpMap.put("simpleDocNoList", simpleDocNoList);
					tmpMap = orderMapper.selectSimpleDocNoCnt(tmpMap);
					if(tmpMap != null && tmpMap.size() > 0 && Util.null2int(tmpMap.get("HOUSE_COUNT")) == 0){
						orderMapper.updateSimpleDocNoNull(tmpMap);
					}
				}
			}
			
		}else if("KorToEngTranslation".equals(search.getUpdateField())){
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
				int cnt = 1;
				for(Order order : logList){
					order.setConsigneeName(Util.getFileWriteDataWithRtrim(ExchangeKorEng.convert(order.getConsigneeNameLoc()), "String", "100"));
					order.setConsigneeAddress(ExchangeKorEng.convert(order.getConsigneeAddressLoc()));
					orderBatchMapper.updateConsigneeEng(order);
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
		}else if("hold".equals(Util.null2str(search.getUpdateType()))) {	/** 홀드 */
			Map<String, Object> tmpMap = new HashMap<String, Object>();	// 히스토리 기록용 맵
			Map<String, Object> holdMap = search.getHoldMap();
			holdMap.put("updateUserId", search.getUpdateUserId());
			holdMap.put("gmtTime", search.getGmtTime());
			batchUpdateMap.put("holdCode", holdMap.get("holdCode"));
			
			List<Map<String, Object>> checkList = new ArrayList<Map<String, Object>>();
            if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")){
                checkList = hubnOrderMapper.selectHubnBatchUpdateHoldTarget(batchUpdateMap);
            }else{
                checkList = orderMapper.selectBatchUpdateHoldTarget(batchUpdateMap);
            }

			if(checkList.size() > 0){
				for(Map<String, Object> checkMap : checkList){
					if(Util.null2str(checkMap.get("HOLD_CODE")).equals(holdMap.get("holdCode")) && Util.null2str(checkMap.get("HOLD_RELEASE_YN")).equals("Y")) {
						orderSaveResult.setResultCode("A11476");	/** 해당 홀드코드를 처리한 건이 있습니다. */
						return orderSaveResult;
					}
					
					//허브넷은 홀드 처리시 MBL 필수. 퀵요청, 기타일경우에는 예외
					if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")){
						if(!"04".equals(holdMap.get("holdCode")) && !"06".equals(holdMap.get("holdCode"))) {
							if("".equals(Util.null2str(checkMap.get("MBL_NO")))) {
								orderSaveResult.setResultCode("A11114");	/** M.BL은 필수입니다. */
								return orderSaveResult;
							}
						}
					}
				}
			}
			
			if(targetData.equals("srchData")){
				orderMapper.deleteHoldBatchBySrchData(batchUpdateMap);
			}else {
				orderMapper.deleteHoldBatch(search);
			}
			
			if(targetData.equals("srchData")){
				if(checkList.size() > 0){
					SqlSession sqlSession = null;
					try {
						sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
						OrderMapper orderMapper2 = sqlSession.getMapper(OrderMapper.class);
						OrderCommonCsMapper orderCommonCsMapper = sqlSession.getMapper(OrderCommonCsMapper.class);
//						SystemMngMapperVE systemMngMapperVE = sqlSession.getMapper(SystemMngMapperVE.class);
						Map<String, Object> liceInfo = new HashMap<String, Object>();
						int cnt = 1;
						for(Map<String, Object> checkMap : checkList){
								holdMap.put("odJobNo", checkMap.get("OD_JOB_NO"));
								holdMap.put("inputMethod", "Hold");
								orderMapper.insertHoldInfo(holdMap);
								if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")){
									if("01".equals(Util.null2str(holdMap.get("holdCode"))) || "02".equals(Util.null2str(holdMap.get("holdCode")))) {
										QnAVE qnaVe = new QnAVE();
										if("01".equals(Util.null2str(holdMap.get("holdCode")))) { //송장교체
											holdMap.put("deliveryNameChanged", checkMap.get("CONSIGNEE_NAME_LOC"));
											holdMap.put("deliveryZipcodeChanged", checkMap.get("CONSIGNEE_ZIPCODE"));
											holdMap.put("deliveryTelChanged", checkMap.get("CONSIGNEE_HP"));
											holdMap.put("deliveryCodeChanged", checkMap.get("DELIVERY_CODE"));
											holdMap.put("deliveryAddressChanged", holdMap.get("holdReason"));
											holdMap.put("odJobNoSeqNoId", orderCommonCsMapper.selectMaxMultiSeqNo(holdMap) + 1);
											//멀티데이터 생성 
											Map<String, Object> multiStockMap = new HashMap<String, Object>();
											multiStockMap.putAll(checkMap);
											multiStockMap.put("gmtTime", search.getGmtTime());
											multiStockMap.put("updateUserId", search.getUpdateUserId());
											multiStockMap.put("createStockNoCnt", "1");
											multiStockMap.put("bound", checkMap.get("BOUND"));
											multiStockMap.put("boundType", checkMap.get("BOUND_TYPE"));
											multiStockMap.put("odJobNo", checkMap.get("OD_JOB_NO"));
											multiStockMap.put("hblNo", checkMap.get("HBL_NO"));
											multiStockMap.put("deliveryCode", checkMap.get("DELIVERY_CODE"));
											multiStockMap.put("PIECES", "1");
											multiStockMap.put("profileName", profile);
											multiStockMap.put("seqNoId", holdMap.get("odJobNoSeqNoId"));
											if("KOREX".equals(checkMap.get("DELIVERY_CODE"))) {
												this.setMultiStock(multiStockMap, Util.null2int(checkMap.get("STOCK_CUST_LV")), 1, false, false);
												if(multiStockMap.get("multiList") != null) {
													List<OrderMulti> multiList = (List<OrderMulti>) multiStockMap.get("multiList");
													multiStockMap.put("stockMutliNo", multiList.get(0).getDeliveryNo());
													holdMap.put("deliveryNoChanged", multiList.get(0).getDeliveryNo());
												}
											}
											orderCommonCsMapper.insertMultiDelivery(multiStockMap);
											orderCommonCsMapper.insertChangeDelivery(holdMap);
											qnaVe.setQuestionType("Q5");
										}else if("02".equals(Util.null2str(holdMap.get("holdCode")))) { // 퀵요청
											holdMap.put("remark", holdMap.get("holdReason"));
											orderCommonCsMapper.insertQuickRequest(holdMap);
											qnaVe.setQuestionType("Q6");
										}
										// QnA등록
										String entCode = search.getEntCode();
										qnaVe.setEntCode(entCode);
										qnaVe.setCustomerCode(entCode.substring(0, entCode.length() - 1)); //도입사 거래처는 4자리이므로 마지막자리수 제외 
										qnaVe.setHblNo(Util.null2str(checkMap.get("HBL_NO")));
										qnaVe.setPoNo(Util.null2str(checkMap.get("PO_NO")));
										qnaVe.setQuestionUserType("S");
										qnaVe.setQandaType("Q");
										qnaVe.setCompleteYnh("N");
										if(!"".equals(holdMap.get("odJobNoSeqNoId"))) {
											qnaVe.setSeqNoId(Util.null2int(holdMap.get("odJobNoSeqNoId")));
										}
										qnaVe.setTitle(Util.null2str(holdMap.get("holdReason")));
										qnaVe.setContents(Util.null2str(holdMap.get("holdReason"))); 
										qnaVe.setGmtTime(search.getGmtTime());
										qnaVe.setUpdateUserId(search.getUpdateUserId());
										systemMngMapperVE.insertQnA(qnaVe);
									}
								}
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
				}
			}else{
				orderMapper.insertHoldInfoBatch(search);
				for(Map<String, Object> checkMap : checkList){
					holdMap.put("odJobNo", checkMap.get("OD_JOB_NO"));
					holdMap.put("inputMethod", "Hold");
					if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")){
						if("01".equals(Util.null2str(holdMap.get("holdCode"))) || "02".equals(Util.null2str(holdMap.get("holdCode")))) {
							QnAVE qnaVe = new QnAVE();
							if("01".equals(Util.null2str(holdMap.get("holdCode")))) { //송장교체
								holdMap.put("deliveryNameChanged", checkMap.get("CONSIGNEE_NAME_LOC"));
								holdMap.put("deliveryZipcodeChanged", checkMap.get("CONSIGNEE_ZIPCODE"));
								holdMap.put("deliveryTelChanged", checkMap.get("CONSIGNEE_HP"));
								holdMap.put("deliveryCodeChanged", checkMap.get("DELIVERY_CODE"));
								holdMap.put("deliveryAddressChanged", holdMap.get("holdReason"));
								holdMap.put("odJobNoSeqNoId", orderCommonCsMapper.selectMaxMultiSeqNo(holdMap) + 1);
								//멀티데이터 생성 
								Map<String, Object> multiStockMap = new HashMap<String, Object>();
								multiStockMap.putAll(checkMap);
								multiStockMap.put("gmtTime", search.getGmtTime());
								multiStockMap.put("updateUserId", search.getUpdateUserId());
								multiStockMap.put("createStockNoCnt", "1");
								multiStockMap.put("bound", checkMap.get("BOUND"));
								multiStockMap.put("boundType", checkMap.get("BOUND_TYPE"));
								multiStockMap.put("odJobNo", checkMap.get("OD_JOB_NO"));
								multiStockMap.put("hblNo", checkMap.get("HBL_NO"));
								multiStockMap.put("deliveryCode", checkMap.get("DELIVERY_CODE"));
								multiStockMap.put("PIECES", "1");
								multiStockMap.put("profileName", profile);
								multiStockMap.put("seqNoId", holdMap.get("odJobNoSeqNoId"));
								if("KOREX".equals(checkMap.get("DELIVERY_CODE"))) {
									this.setMultiStock(multiStockMap, Util.null2int(checkMap.get("STOCK_CUST_LV")), 1, false, false);
									if(multiStockMap.get("multiList") != null) {
										List<OrderMulti> multiList = (List<OrderMulti>) multiStockMap.get("multiList");
										multiStockMap.put("stockMutliNo", multiList.get(0).getDeliveryNo());
										holdMap.put("deliveryNoChanged", multiList.get(0).getDeliveryNo());
									}
								}
								orderCommonCsMapper.insertMultiDelivery(multiStockMap);
								orderCommonCsMapper.insertChangeDelivery(holdMap);
								qnaVe.setQuestionType("Q5");
							}else if("02".equals(Util.null2str(holdMap.get("holdCode")))) { // 퀵요청
								holdMap.put("remark", holdMap.get("holdReason"));
								orderCommonCsMapper.insertQuickRequest(holdMap);
								qnaVe.setQuestionType("Q6");
							}
						
							// QnA등록
							String entCode = search.getEntCode();
							qnaVe.setEntCode(entCode);
							qnaVe.setCustomerCode(entCode.substring(0, entCode.length() - 1)); //도입사 거래처는 4자리이므로 마지막자리수 제외 
							qnaVe.setHblNo(Util.null2str(checkMap.get("HBL_NO")));
							qnaVe.setPoNo(Util.null2str(checkMap.get("PO_NO")));
							qnaVe.setQuestionUserType("S");
							qnaVe.setQandaType("Q");
							qnaVe.setCompleteYnh("N");
							if(!"".equals(holdMap.get("odJobNoSeqNoId"))) {
								qnaVe.setSeqNoId(Util.null2int(holdMap.get("odJobNoSeqNoId")));
							}
							qnaVe.setTitle(Util.null2str(holdMap.get("holdReason")));
							qnaVe.setContents(Util.null2str(holdMap.get("holdReason"))); 
							qnaVe.setGmtTime(search.getGmtTime());
							qnaVe.setUpdateUserId(search.getUpdateUserId());
							systemMngMapperVE.insertQnA(qnaVe);
						}
					}
				}
			}
			
			// 히스토리 기록
			if(checkList.size() > 0){
				for(Map<String, Object> checkMap : checkList) {
					tmpMap.clear();
					tmpMap.put("entCode", search.getEntCode());
					tmpMap.put("entBusinessCode", "");
					tmpMap.put("gmtTime", search.getGmtTime());
					tmpMap.put("solution", "ECM");
					tmpMap.put("updateUserId", search.getUpdateUserId());
					tmpMap.put("stockType", "POD");
					tmpMap.put("stockSign", "POD");
					tmpMap.put("stockSeqLength", 8);
					tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
					tmpMap.put("stockRemark", "ECM POD JOB NO");
					tmpMap.put("odJobNo", checkMap.get("OD_JOB_NO"));
					tmpMap.put("podStatus", "ECM286");
					tmpMap.put("podName", "HOLD");
					tmpMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
					tmpMap.put("podTableName", 	"TB_ECM_POD_05");
					tmpMap.put("remarks", "BATCH UPDATE [HBL_NO : "+checkMap.get("HBL_NO")+", HOLD_CODE : "+holdMap.get("holdCode")+"]");
					orderMapper.insertHoldHistory(tmpMap);
				}
			}

            //허브넷인 경우에는 인터페이스 테이블에 홀드 데이터를 저장해줘야함.
            if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")){
            	if("IMP".equals(search.getBoundType())) {
            		for(Map<String, Object> checkMap : checkList) {
            			checkMap.put("BOUND", search.getBound());
            			checkMap.put("BOUND_TYPE", search.getBoundType());
            			checkMap.put("STATUS_CODE_ETC1","");
            			checkMap.put("STATUS_CODE","H01");
            			checkMap.put("HOLD_CODE", holdMap.get("holdCode"));
//                    hubnOrderMapper.insertInterfaceInfo(checkMap);
            		}
            		
            		Map<String,Object> returnMap = new HashMap<String, Object>();
            		returnMap.put("hubnHoldList", checkList);
            		orderSaveResult.setResultData(returnMap);
            	}
            }
			
		}else if("holdCancel".equals(Util.null2str(search.getUpdateType()))) {	/** 홀드취소 */
			Map<String, Object> tmpMap = new HashMap<String, Object>();	// 히스토리 기록용 맵
			List<Map<String, Object>> checkList = new ArrayList<Map<String, Object>>();
            if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")){
                checkList = hubnOrderMapper.selectHubnBatchUpdateHoldTarget(batchUpdateMap);
            }else{
                checkList = orderMapper.selectBatchUpdateHoldTarget(batchUpdateMap);
            }
			
			if(targetData.equals("srchData")){
				orderMapper.deleteHoldBatchBySrchData(batchUpdateMap);
			}else {
				orderMapper.deleteHoldBatch(search);
			}
			
			// 히스토리 기록
			if(checkList.size() > 0){
				for(Map<String, Object> checkMap : checkList) {
					tmpMap.clear();
					tmpMap.put("entCode", search.getEntCode());
					tmpMap.put("entBusinessCode", "");
					tmpMap.put("gmtTime", search.getGmtTime());
					tmpMap.put("solution", "ECM");
					tmpMap.put("updateUserId", search.getUpdateUserId());
					tmpMap.put("stockType", "POD");
					tmpMap.put("stockSign", "POD");
					tmpMap.put("stockSeqLength", 8);
					tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
					tmpMap.put("stockRemark", "ECM POD JOB NO");
					tmpMap.put("odJobNo", checkMap.get("OD_JOB_NO"));
					tmpMap.put("podStatus", "ECM286");
					tmpMap.put("podName", "HOLD");
					tmpMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
					tmpMap.put("podTableName", 	"TB_ECM_POD_05");
					tmpMap.put("remarks", "CANCEL [HBL_NO : "+checkMap.get("HBL_NO")+"]");
					orderMapper.insertHoldHistory(tmpMap);
				}
			}

            //허브넷인 경우에는 인터페이스 테이블에 홀드 데이터를 저장해줘야함.
            if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")){
                for(Map<String, Object> checkMap : checkList) {
                    if (!"".equals(Util.null2str(checkMap.get("HOLD_CODE")))) {
                    	
                    	checkMap.put("BOUND", search.getBound());
            			checkMap.put("BOUND_TYPE", search.getBoundType());
                        checkMap.put("STATUS_CODE_ETC1","C");
                        checkMap.put("STATUS_CODE","H01");
//                        hubnOrderMapper.insertInterfaceInfo(checkMap);
                    }
                }
                
        		Map<String,Object> returnMap = new HashMap<String, Object>();
        		returnMap.put("hubnHoldList", checkList);
        		orderSaveResult.setResultData(returnMap);
            }

		}else if("holdRelease".equals(Util.null2str(search.getUpdateType()))) {	/** 홀드(처리) */
			Map<String, Object> tmpMap = new HashMap<String, Object>();	// 히스토리 기록용 맵
			Map<String, Object> holdMap = search.getHoldMap();
			holdMap.put("updateUserId", search.getUpdateUserId());
			holdMap.put("gmtTime", search.getGmtTime());
			batchUpdateMap.put("holdCode", holdMap.get("holdCode"));
			batchUpdateMap.put("holdReason", holdMap.get("holdReason"));
			
			List<Map<String, Object>> checkList = new ArrayList<Map<String, Object>>();
            checkList = orderMapper.selectBatchUpdateHoldTarget(batchUpdateMap);

			if(checkList.size() > 0){
				for(Map<String, Object> checkMap : checkList){
					if(Util.null2str(checkMap.get("HOLD_CODE")).equals(holdMap.get("holdCode")) && Util.null2str(checkMap.get("HOLD_RELEASE_YN")).equals("Y")) {
						orderSaveResult.setResultCode("A11476");	/** 해당 홀드코드를 처리한 건이 있습니다. */
						return orderSaveResult;
					}
				}
			}
			
			if(targetData.equals("srchData")){
				orderMapper.updateHoldBatchBySrchData(batchUpdateMap);
			}else {
				orderMapper.updateHoldBatch(search);
			}
			
			// 히스토리 기록
			if(checkList.size() > 0){
				for(Map<String, Object> checkMap : checkList) {
					tmpMap.clear();
					tmpMap.put("entCode", search.getEntCode());
					tmpMap.put("entBusinessCode", "");
					tmpMap.put("gmtTime", search.getGmtTime());
					tmpMap.put("solution", "ECM");
					tmpMap.put("updateUserId", search.getUpdateUserId());
					tmpMap.put("stockType", "POD");
					tmpMap.put("stockSign", "POD");
					tmpMap.put("stockSeqLength", 8);
					tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
					tmpMap.put("stockRemark", "ECM POD JOB NO");
					tmpMap.put("odJobNo", checkMap.get("OD_JOB_NO"));
					tmpMap.put("podStatus", "ECM286");
					tmpMap.put("podName", "HOLD");
					tmpMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
					tmpMap.put("podTableName", 	"TB_ECM_POD_05");
					tmpMap.put("remarks", "BATCH RELEASE [HBL_NO : "+checkMap.get("HBL_NO")+", HOLD_CODE : "+holdMap.get("holdCode")+"]");
					orderMapper.insertHoldHistory(tmpMap);
				}
			}

		}else if(Util.null2str(search.getUpdateField()).equals("CBM") || Util.null2str(search.getUpdateField()).equals("GWT")){ /** CBM or GWT */
			// House CBM 안분 - 소수점 아래 3자리까지 안분하고 나머지는 맨 마지막 하우스에 반영
			if(search.isDivideFlag()) {
				double totalVal = Util.null2dbl(search.getUpdateValue());
				double val = Util.null2dbl(totalVal / logList.size(), 3, "D");
				double lastVal = val + Util.null2dbl(totalVal - Util.null2dbl(val * logList.size(), 3, "D"), 3, "D");
				
				if(targetData.equals("srchData")){ // 검색
					batchUpdateMap.put("updateValue", String.valueOf(val));
					orderMapper.updateOrderBatchBySrchData(batchUpdateMap);
				}else{ // 선택 
					search.setUpdateValue(String.valueOf(val));
					orderMapper.updateOrderBatch(search);
				}
				
				// 마지막 하우스
				Order lastOrder = logList.get(logList.size()-1);
				List<String> checkDataList = new ArrayList<String>();
				checkDataList.add(lastOrder.getOdJobNo());
				search.setCheckDataList(checkDataList);
				search.setCheckDataTypeMh("H");
				search.setUpdateValue(String.valueOf(lastVal));
				orderMapper.updateOrderBatch(search);
			}else {
				if(targetData.equals("srchData")){ // 검색
					orderMapper.updateOrderBatchBySrchData(batchUpdateMap);
				}else{ // 선택 
					orderMapper.updateOrderBatch(search);
				}
			}
		}else if("unusual".equals(Util.null2str(search.getUpdateType()))) {	/** 이례사항 */
			Map<String, Object> tmpMap = new HashMap<String, Object>();	// 히스토리 기록용 맵
			Map<String, Object> unusualMap = search.getUnusualMap();
			unusualMap.put("updateUserId", search.getUpdateUserId());
			unusualMap.put("gmtTime", search.getGmtTime());
			batchUpdateMap.put("unusualCode", unusualMap.get("unusualCode"));
			
			List<Map<String, Object>> checkList = new ArrayList<Map<String, Object>>();
			checkList = orderMapper.selectBatchUpdateUnusualTarget(batchUpdateMap);
			if(checkList.size() > 0){
				for(Map<String, Object> checkMap : checkList){
					if(Util.null2str(checkMap.get("UNUSUAL_CODE")).equals(unusualMap.get("unusualCode"))) {
						orderSaveResult.setResultCode("A11635");	/** 해당 이례코드를 처리한 건이 있습니다. */
						return orderSaveResult;
					}
				}
			}
			if(targetData.equals("srchData")){
				if(checkList.size() > 0){
					SqlSession sqlSession = null;
					try {
						sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
						OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
						int cnt = 1;
						for(Map<String, Object> checkMap : checkList){
							unusualMap.put("odJobNo", checkMap.get("OD_JOB_NO"));
							orderMapper.updateUnusualInfo(unusualMap);
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
				}
			}else{
				orderMapper.updateUnusualInfoBatch(search);
			}
			
			// 히스토리 기록
//			if(checkList.size() > 0){
//				for(Map<String, Object> checkMap : checkList) {
//					tmpMap.clear();
//					tmpMap.put("entCode", search.getEntCode());
//					tmpMap.put("entBusinessCode", "");
//					tmpMap.put("gmtTime", search.getGmtTime());
//					tmpMap.put("solution", "ECM");
//					tmpMap.put("updateUserId", search.getUpdateUserId());
//					tmpMap.put("stockType", "POD");
//					tmpMap.put("stockSign", "POD");
//					tmpMap.put("stockSeqLength", 8);
//					tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
//					tmpMap.put("stockRemark", "ECM POD JOB NO");
//					tmpMap.put("odJobNo", checkMap.get("OD_JOB_NO"));
//					tmpMap.put("podStatus", "ECM286");
//					tmpMap.put("podName", "HOLD");
//					tmpMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
//					tmpMap.put("podTableName", 	"TB_ECM_POD_05");
//					tmpMap.put("remarks", "BATCH UPDATE [HBL_NO : "+checkMap.get("HBL_NO")+", HOLD_CODE : "+holdMap.get("holdCode")+"]");
//					orderMapper.insertHoldHistory(tmpMap);
//				}
//			}
			
		}else if("unusualCancel".equals(Util.null2str(search.getUpdateType()))) {	/** 이례사항 취소 */
			Map<String, Object> tmpMap = new HashMap<String, Object>();	// 히스토리 기록용 맵
			List<Map<String, Object>> checkList = new ArrayList<Map<String, Object>>();
			checkList = orderMapper.selectBatchUpdateUnusualTarget(batchUpdateMap);
			
			if(targetData.equals("srchData")){
				orderMapper.updateUnusualBatchBySrchData(batchUpdateMap);
			}else {
				orderMapper.updateUnusualBatch(search);
			}
			
			// 히스토리 기록
//			if(checkList.size() > 0){
//				for(Map<String, Object> checkMap : checkList) {
//					tmpMap.clear();
//					tmpMap.put("entCode", search.getEntCode());
//					tmpMap.put("entBusinessCode", "");
//					tmpMap.put("gmtTime", search.getGmtTime());
//					tmpMap.put("solution", "ECM");
//					tmpMap.put("updateUserId", search.getUpdateUserId());
//					tmpMap.put("stockType", "POD");
//					tmpMap.put("stockSign", "POD");
//					tmpMap.put("stockSeqLength", 8);
//					tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
//					tmpMap.put("stockRemark", "ECM POD JOB NO");
//					tmpMap.put("odJobNo", checkMap.get("OD_JOB_NO"));
//					tmpMap.put("podStatus", "ECM286");
//					tmpMap.put("podName", "HOLD");
//					tmpMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
//					tmpMap.put("podTableName", 	"TB_ECM_POD_05");
//					tmpMap.put("remarks", "CANCEL [HBL_NO : "+checkMap.get("HBL_NO")+"]");
//					orderMapper.insertHoldHistory(tmpMap);
//				}
//			}
		}
//		else if(Util.null2str(search.getUpdateField()).equals("DELIVERY_CODE")){
//			if(targetData.equals("srchData")){ // 검색 
//				batchUpdateMap.put("targetData", "srchData");
//				orderMapper.insertOrderMultiBatch(batchUpdateMap); 
//			}else{
//				orderMapper.insertOrderMultiBatch(search); // 선택 
//			}
//		}
		else{
			
			if(targetData.equals("srchData")){
				if("WH_LOCATION".equals(search.getUpdateField()) || "CFA_HOLD_YN".equals(search.getUpdateField()) || "AMS_YN".equals(search.getUpdateField()) || "WH_CARRYOUT_DATE".equals(search.getUpdateField()) || "CARRYOUT_CODE".equals(search.getUpdateField()) ){
					orderMapper.updateOrderAdditionalBatchBySrchData(batchUpdateMap);
				}else{
					orderMapper.updateOrderBatchBySrchData(batchUpdateMap);
				}
			}else{
				if("WH_LOCATION".equals(search.getUpdateField()) || "CFA_HOLD_YN".equals(search.getUpdateField()) || "AMS_YN".equals(search.getUpdateField()) || "WH_CARRYOUT_DATE".equals(search.getUpdateField()) || "CARRYOUT_CODE".equals(search.getUpdateField()) ){
					orderMapper.updateOrderAdditionalBatch(search);
				}else{
					orderMapper.updateOrderBatch(search);
				}
			}
		}
		
		if("CD_TYPE".equals(search.getUpdateField())){
			if("EXP".equals(batchUpdateMap.get("boundType"))){
				String updateCdType = Util.null2str(search.getUpdateValue());
				Map<String, Object> simpleDocNoMap = null;
				if(updateCdType.equals("Y") || updateCdType.equals("E")){
					simpleDocNoMap = new HashMap<String, Object>();
					simpleDocNoMap.put("entCode", search.getEntCode());
					simpleDocNoMap.put("gmtTime", search.getGmtTime());
					simpleDocNoMap.put("entBusinessCode", "");
					simpleDocNoMap.put("updateUserId", search.getUpdateUserId());
					simpleDocNoMap.put("solution", "ECM");
					simpleDocNoMap.put("stockType", "OD");
					simpleDocNoMap.put("stockSeqLength", "8");
					simpleDocNoMap.put("signType", "noHyphen");
					simpleDocNoMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
					simpleDocNoMap.put("stockRemark", "ORDER SIMPLE DOC NO");
					if(updateCdType.equals("Y")){
						simpleDocNoMap.put("dateLength", "4");
						simpleDocNoMap.put("stockDateFormat", "112");
					}else if(updateCdType.equals("E")){
						simpleDocNoMap.put("dateLength", "2");
						simpleDocNoMap.put("stockDateFormat", "12");
					}
				}
				
				
				SqlSession sqlSession = null;
				try {
					sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
					ConsolMapper consolBatchMapper = sqlSession.getMapper(ConsolMapper.class);
					int seq = 1;
					for(Order od : logList) {
						if(!"".equals(od.getEdiExpressSign()) && "Y".equals(od.getFinishYn()) && !updateCdType.equals(od.getCdType())){
							if(simpleDocNoMap != null){
								simpleDocNoMap.put("odJobNo", od.getOdJobNo());
								simpleDocNoMap.put("stockSign", od.getEdiExpressSign());
								simpleDocNoMap.put("seqNo", seq);
								consolBatchMapper.updateSimpleDocNoExp(simpleDocNoMap);
							}else{
								od.setSimpleDocNo(null);
								consolBatchMapper.updateSimpleDocNo(od);
							}
							if( (seq++ % 1000) == 0){
								sqlSession.flushStatements();
								sqlSession.clearCache();
							}
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
			}else{
				/** 2025-05-27 통관구분이 목록으로 변경되었을때 하우스의 제출번호가 이미 통관목록 전송되었다면 제출번호 제거 */
				if("Y".equals(search.getUpdateValue())){
					Map<String, Object> tmpMap = new HashMap<String, Object>();
					Map<String, Object> chkMap = new HashMap<String, Object>();
					tmpMap.put("srchType", "sendCnt");
					for(Order od : logList) {
						if(!"Y".equals(od.getCdType()) && !Util.isEmpty(od.getSimpleDocNo())){
							if(chkMap.get(od.getSimpleDocNo()) == null){
								tmpMap.put("simpleDocNo", od.getSimpleDocNo());
								if(commonMapper.selectSimpleDocNoCount(tmpMap) > 0){
									chkMap.put(od.getSimpleDocNo(), "DEL");
									tmpMap.put("OD_JOB_NO", od.getOdJobNo());
									orderMapper.updateSimpleDocNoNull(tmpMap);
								}else{
									chkMap.put(od.getSimpleDocNo(), "N");
								}
							}else if("DEL".equals(chkMap.get(od.getSimpleDocNo()))){
								tmpMap.put("OD_JOB_NO", od.getOdJobNo());
								orderMapper.updateSimpleDocNoNull(tmpMap);
							}
						}
					}
				}
			}
		}
		
		if("Y".equals(batchUpdateMap.get("srchCargoProgress"))){
			String key = "E-COMMERCE_CARGO_PROGRESS_INSERT";
			ListOperations<String, String> listOperations = redisTemplate.opsForList();
			JSONObject json = new JSONObject();
			json.put("entCode", search.getEntCode());
			json.put("entBusinessCode", search.getUserBusinessCode());
			json.put("bound", search.getBound());
			json.put("boundType", search.getBoundType());
			for(Order od : logList) {
				if("Y".equals(od.getCdType()) && !Util.isEmpty(od.getMblNo())){
					json.put("customs", od.getCustomsCode());
					json.put("status", od.getLastPodStatus());
					json.put("odJobNo", od.getOdJobNo());
					json.put("masterOdJobNo", od.getMasterOdJobNo());
					json.put("mblNo", od.getMblNo());
					json.put("hblNo", od.getHblNo());
					json.put("arDate", od.getArDate());
					json.put("dropYn", od.getDropYn());
					json.put("cdType", od.getCdType());
					json.put("destCode", od.getDestCode());
					json.put("destPort", od.getDestPort());
					if(!Util.null2str(od.getDestCode()).equals(Util.null2str(od.getDestPort()))){
						json.put("bondedCargoYn", "Y");
					}else{
						json.put("bondedCargoYn", "N");
					}
					json.put("action", "cdTypeBatchUpdate");
					json.put("mrn", od.getMrn());
					listOperations.rightPush(key, json.toString());
				}
			}
		}
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", search.getEntCode());
		tmpLogMap.put("ent_business_code", search.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", search.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", search.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", search.getLogUserName());
		tmpLogMap.put("process_user_name_eng", search.getLogUserNameEng());
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		int seq = 1;
		String descriptionStr = "Order batch update > " + Util.null2str(search.getUpdateType()) + " - " + Util.null2str(search.getUpdateField()) + " - " + Util.null2str(search.getUpdateValue());
		for(Order od : logList) {
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("bound", od.getBound());
			logMap.put("bound_type", od.getBoundType());
			logMap.put("job_no", od.getOdJobNo());
			if(targetData.equals("srchData")) {	//검색건
				search.setSearchOrderList(null);
				search.setContainerList(null);
				search.setPoInfoList(null);
				logMap.put("job_log", new Gson().toJson(search));
			}else {	//선택건
				logMap.put("job_log", "");
			}
			
			
			
			if(Util.null2str(od.getHblNo()).equals("")) {
				logMap.put("description",descriptionStr +" [MBL_NO : "+ od.getMblNo() + "]");
			}else if(!Util.null2str(od.getMblNo()).equals("")) {
				logMap.put("description",descriptionStr +" [MBL_NO : "+ od.getMblNo() + "]" +" [HBL_NO : "+ od.getHblNo() +"]");
			}else {
				logMap.put("description",descriptionStr +" [HBL_NO : "+ od.getHblNo() +"]");
			}
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}
		
		commonService.insertLogBulk(bulkList);
		
		return orderSaveResult;
	}
	
	public Map<String, Object> batchUpdateTest(SearchOrderBatchUpdate search) throws Exception{
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Util.accessControlDatabase(search, "updateField|updateField2|gmtTime", false, null, null, null, null);
		String targetData = Util.null2str(search.getTargetData());
		Map batchUpdateMap = null;
		if(targetData.equals("srchData")){
			batchUpdateMap = BeanUtils.describe(search.getSearchOrderList());
			batchUpdateMap.put("updateField", search.getUpdateField());
			batchUpdateMap.put("updateValue", search.getUpdateValue());
		}
		if(Util.null2str(search.getUpdateType()).equals("container")){
			if(targetData.equals("srchData")){
				orderMapper.deleteContainerBatchBySrchData(batchUpdateMap);
			}else {
				orderMapper.deleteContainerBatch(search);
			}
			if(!Util.null2str(search.getUpdateField()).equals("delete")){
				if(targetData.equals("srchData")){
					List<String> batchUpdateList = orderMapper.selectBatchUpdateTarget(search.getSearchOrderList());		/** 검색으로 저장일경우 대상건 조회 */
					if(batchUpdateList.size() > 0){
						returnMap.put("cntrInsert", "Y");
						returnMap.put("batchUpdateList", batchUpdateList);
						returnMap.put("cntrList", search.getContainerList());
					}
				}else{
					orderMapper.insertContainerBatch(search);
				}
			}
		}else{
			if(targetData.equals("srchData")){
				orderMapper.updateOrderBatchBySrchData(batchUpdateMap);
			}else{
				orderMapper.updateOrderBatch(search);
			}
		}
		return returnMap;
	}
	
	public void batchUpdateContainer(Map<String, Object> map) throws Exception{
		
		List<String> batchUpdateList = (List<String>)map.get("batchUpdateList");
		List<SearchOrderBatchUpdateCntr> cntrList = (List<SearchOrderBatchUpdateCntr>)map.get("cntrList");
		
		Connection con = null;
		PreparedStatement pstmt = null ;
		String sql = "INSERT INTO TB_ECM_OD_CONTAINER(OD_JOB_NO,CNTR_NO,CNTR_CODE,PIECES,CNTR_SEAL_NO_1,CNTR_SEAL_NO_2,CNTR_SEAL_NO_3)VALUES(?,?,?,?,?,?,?)" ;
		try{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection("jdbc:sqlserver://10.3.1.20:1433;DatabaseName=exps", "exps", "test12!@");
			con.setAutoCommit(false);
			pstmt = con.prepareStatement(sql) ;
			
			int cnt = 1;
			for(int i = 0; i < batchUpdateList.size(); i++){
				for(int j = 0; j < cntrList.size(); j++) {
					pstmt.setString(1, batchUpdateList.get(i));
					pstmt.setString(2, cntrList.get(j).getCntrNo());
					pstmt.setString(3, cntrList.get(j).getCntrCode());
					pstmt.setInt(4, cntrList.get(j).getPieces());
					pstmt.setString(5, cntrList.get(j).getCntrSealNo1());
					pstmt.setString(6, cntrList.get(j).getCntrSealNo2());
					pstmt.setString(7, cntrList.get(j).getCntrSealNo3());
					
					// addBatch에 담기
					pstmt.addBatch();
					// 파라미터 Clear
					pstmt.clearParameters();
					// OutOfMemory를 고려하여 만건 단위로 커밋
					if( (cnt++ % 1000) == 0){
						// Batch 실행
						pstmt.executeBatch();
						// Batch 초기화
						pstmt.clearBatch();
						// 커밋
//						con.commit() ;
					}
				}
			}
			// 커밋되지 못한 나머지 구문에 대하여 커밋
			
			pstmt.executeBatch();
			con.commit();
		}catch(Exception e){
			try {
				con.rollback();
			} catch (SQLException e1) {
				throw e1;
			}
			throw e;
		}finally{
			if (pstmt != null) try {pstmt.close();pstmt = null;} catch(SQLException ex){log.error(ex.getMessage());}
			if (con != null) try {con.close();con = null;} catch(SQLException ex){log.error(ex.getMessage());}
		}
		/**
		 * order 10000 / container 4 / executeBatch 1000  / 모두실행후 commit : 1831
		 * order 10000 / container 4 / executeBatch 10000 / commit 10000 : 4936
		 * order 10000 / container 4 / executeBatch 1000  / commit 1000  : 5106
		 * order 10000 / container 4 / executeBatch 100   / commit 100   : 7686
		 */
	}
	
	/**
	 * 전자상거래 Order 수화인우편번호변환
	 * @param map
	 * @return
	 * @throws Exception
	 */
//	@SuppressWarnings("unchecked")
//	public OrderSaveResult updateConsigneeZipCode(ZipCodeChange zipCodeChange, HttpServletRequest request) throws Exception{
//		OrderSaveResult orderSaveResult = new OrderSaveResult();
//		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
//		
//		String blTypeMh = Util.null2str(zipCodeChange.getBlTypeMh());
//		/** 마감체크 */
//		//마스터 번호인경우
////		if("M".equals(blTypeMh)) {
////			consolService.closeChkList(orderSaveResult, zipCodeChange.getOdJobNoList(), "masterOdJobNoList");
////		}
////		else {
////			consolService.closeChkList(orderSaveResult, zipCodeChange.getOdJobNoList(), "odJobNoList");
////		}
//		
////		if(!Util.null2str(orderSaveResult.getResultCode()).equals("")){
////			return orderSaveResult;
////		}
//		
////		int nSkip_OverCount = 0;
////		int nSkip_NoCount = 0;
//		
//		
//		Map srchMap = new HashMap();
//		if(zipCodeChange.getTargetData().equals("srchData")){
//			//등록,발송 화면에서 호출했을 경우 orderCustomerMapper
//			if(Util.null2str(zipCodeChange.getScreen()).equals("ORDER_CUSTOMER")) {
//				SearchOrderCustomerList searchOrderList = zipCodeChange.getSearchOrderCustomerList();
//				/* N개 검색(멀티타입) */
//				List<String> searchNoList = new ArrayList<String>();
//				if(!"".equals(Util.null2str(searchOrderList.getSrchNo()))) {
//					searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(searchOrderList.getSrchNo())).split(",")));
//				}
//				
//				if(searchNoList.size() > 1){
//					searchOrderList.setSearchType("multi");
//					searchOrderList.setSearchNoList(searchNoList);
//				}else{
//					searchOrderList.setSearchType("");
//					searchOrderList.setSrchNo(Util.null2str(searchOrderList.getSrchNo()).trim().replace(",", ""));
//				}
//				srchMap = BeanUtils.describe(searchOrderList);
//				srchMap.put("srchStatusList", searchOrderList.getSrchStatusList());
//				srchMap.put("srchCdTypeList", searchOrderList.getSrchCdTypeList());
//				srchMap.put("srchCusIdVerifyList", searchOrderList.getSrchCusIdVerifyList());
//				srchMap.put("srchCarryList", searchOrderList.getSrchCarryList());
//				
//				if(searchOrderList.getSearchType().equals("multi")){
//					srchMap.put("searchNoList", searchOrderList.getSearchNoList());
//				}
//				String boundType = Util.null2str(srchMap.get("srchBoundType"));
//				if(boundType.equals("AI")) {
//					zipCodeChange.setBound("AIR");
//				}else{
//					zipCodeChange.setBound("SEA");
//				}
//				
//			}
//			//그 외 화면에서 호출했을 경우 orderMapper
//			else {
//				SearchOrderList searchOrderList = zipCodeChange.getSearchOrderList();
//				/* N개 검색(멀티타입) */
//				List<String> searchNoList = new ArrayList<String>();
//				if(!"".equals(Util.null2str(searchOrderList.getSrchNo()))) {
//					searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(searchOrderList.getSrchNo())).split(",")));
//				}
//				
//				if(searchNoList.size() > 1){
//					searchOrderList.setSearchType("multi");
//					searchOrderList.setSearchNoList(searchNoList);
//				}else{
//					searchOrderList.setSearchType("");
//					searchOrderList.setSrchNo(Util.null2str(searchOrderList.getSrchNo()).trim().replace(",", ""));
//				}
//				srchMap = BeanUtils.describe(searchOrderList);
//				
//				srchMap.put("srchCdTypeList", searchOrderList.getSrchCdTypeList());
//				srchMap.put("srchClearanceStatusList", searchOrderList.getSrchClearanceStatusList());
//				srchMap.put("srchLastPodStatusList", searchOrderList.getSrchLastPodStatusList());
//				srchMap.put("srchCustomsIdVerifyList", searchOrderList.getSrchCustomsIdVerifyList());
//				srchMap.put("srchCarryList", searchOrderList.getSrchCarryList());
//				
//				if(searchOrderList.getSearchType().equals("multi")){
//					srchMap.put("searchNoList", searchOrderList.getSearchNoList());
//				}
//				String boundType = Util.null2str(srchMap.get("srchBoundType"));
//				if(boundType.equals("AI")) {
//					zipCodeChange.setBound("AIR");
//				}else{
//					zipCodeChange.setBound("SEA");
//				}
//			}
//		}else{
//			srchMap.put("blTypeMh", blTypeMh);
//			
//			if("Y".equals(zipCodeChange.getCheckMfSubGridYn())){
//				srchMap.put("checkMfSubGridYn", "Y");
//				srchMap.put("checkMfSubGridDataList", zipCodeChange.getCheckMfSubGridDataList());
//			}else{
//				srchMap.put("odJobNoList", zipCodeChange.getOdJobNoList());
//			}
//			
//		}
//		srchMap.put("targetData", zipCodeChange.getTargetData());
//		List<Map<String, Object>> consigneeAddrList = null;
//		if(Util.null2str(zipCodeChange.getScreen()).equals("ORDER_CUSTOMER")) {
//			consigneeAddrList = orderCustomerMapper.selectOrderCustomerChkList(srchMap);
//		}else{
//			consigneeAddrList =  orderMapper.selectOrderChkList(srchMap);
//		}
//		
//		Map<String,Object> returnMap = new HashMap<String,Object>();
//		int dataSize = consigneeAddrList.size();
//		
//		BulkData bulkData = null;
//		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
//		Map<String, Object> logMap = null;
//		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
//		tmpLogMap.put("ent_code", zipCodeChange.getEntCode());
//		tmpLogMap.put("ent_business_code", zipCodeChange.getUserBusinessCode());
//		tmpLogMap.put("solution", "ECM");
//		tmpLogMap.put("bound", zipCodeChange.getBound());
//		tmpLogMap.put("bound_type", "IMP");
//		tmpLogMap.put("process_user_id", zipCodeChange.getUpdateUserId());
//		tmpLogMap.put("log_job_type", "DATA");
//		tmpLogMap.put("job_func_type", "UPDATE");
//		tmpLogMap.put("result", "ok");
//		tmpLogMap.put("process_date_time", System.currentTimeMillis());
//		tmpLogMap.put("ent_business_print_name", zipCodeChange.getLogUserBusinessPrintName());
//		tmpLogMap.put("process_user_name", zipCodeChange.getLogUserName());
//		tmpLogMap.put("process_user_name_eng", zipCodeChange.getLogUserNameEng());
//		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
//		
//		if(dataSize > 0){
//			int threadIndex = 0;//쓰레드 번호
//			int startIndex = 0; 
//			int endIndex = 0;
//			int threadCnt = 30; //쓰레드당 처리하는 데이터 개수 
//			int threadMax = 0; // 스레드 수 
//			int threadFormatLength = 0;
//			int idx = 0;
//			Map<String,Thread> threadMap = new HashMap<String,Thread>();
//			
//			threadMax = (dataSize / threadCnt);
//			if(dataSize % threadCnt > 0){
//				threadMax++;
//			}
//			threadFormatLength = String.valueOf(threadMax).length();
//			
//			for(Map<String, Object> consigneeAddrMap : consigneeAddrList){
//				if(( (dataSize > 1 && idx > 0) || (dataSize == 1)) && (idx % threadCnt == 0 || idx == dataSize-1)){
//					startIndex = endIndex;
//					endIndex = idx;
//					if(idx == dataSize-1){
//						endIndex++;
//					}
//					
//					ZipcodeConvertThreadEX threadex = new ZipcodeConvertThreadEX(consigneeAddrList, startIndex, endIndex, returnMap);
//					Thread thread = new Thread(threadex, "ZipcodeConvert_"+zipCodeChange.getEntCode()+"_"+zipCodeChange.getUpdateUserId()+"_"+String.format("%0"+threadFormatLength+"d", threadIndex));
//					thread.start();
//					
//					threadMap.put("ZipcodeConvert_"+zipCodeChange.getEntCode()+"_"+zipCodeChange.getUpdateUserId()+"_"+String.format("%0"+threadFormatLength+"d", threadIndex), thread);
//					//threadList.add(threadMap);
//					threadIndex++;
//				}
//				idx++;
//			}
//			
//			while(threadMap!= null && !threadMap.isEmpty()){
//				for(Entry<String, Thread> entry : threadMap.entrySet()) {
//					if(threadMap.get(entry.getKey()) != null && !threadMap.get(entry.getKey()).isAlive()) {
//						threadMap.remove(entry.getKey());
//						break;
//					}
//				}
//			}
//			
//			SqlSession sqlSession = null;
//			try {
//				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
//				OrderMapper orderMapper2 = sqlSession.getMapper(OrderMapper.class);
//				int cnt = 1;
//				int seq = 1;
//				for(Map<String, Object> consigneeAddrMap : consigneeAddrList){
//					if(!Util.null2str(consigneeAddrMap.get("CONSIGNEE_ZIPCODE")).equals("")){
//						orderMapper2.updateConsigneeZipCode(consigneeAddrMap);
//						
//						bulkData = new BulkData();
//						bulkData.setActionType(BulkData.Type.CREATE);
//						bulkData.setIndexName(index);
//						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//						logMap = new HashMap<String, Object>();
//						logMap.putAll(tmpLogMap);
//						logMap.put("reference_no_id", bulkData.getId());
//						logMap.put("description", "consignee zipcode update [OD_JOB_NO : "+consigneeAddrMap.get("OD_JOB_NO") +"]");
//						if(zipCodeChange.getTargetData().equals("srchData")){
//							logMap.put("job_log", new Gson().toJson(zipCodeChange));
//						}else {
//							logMap.put("job_log", "");
//						}
//						bulkData.setJsonMap(logMap);
//						bulkList.add(bulkData);
//						
//						if((cnt++ % 1000) == 0){
//							sqlSession.flushStatements();
//							sqlSession.clearCache();
//						}
//					}
//				}
//				sqlSession.flushStatements();
//				sqlSession.clearCache();
//				sqlSession.commit();
//			}catch(Exception e) {
//				throw e;
//			}finally{
//				sqlSession.close();
//			}
//		}
//		
//		//우편번호검증 오류 건은 1000건씩 돌며 ad.ZIPCODE_ERROR_YN 값을 Y로 업데이트함
//		String failOdJobNoStr = Util.null2str(returnMap.get("failOdJobNoStr"));
//		List<String> failOdJobNoList = new ArrayList<String>();
//		failOdJobNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(failOdJobNoStr)).split(",")));
//		SqlSession sqlSession = null;
//		try {
//			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
//			OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
//			int cnt = 1;
//			int seq = 1;
//			for(String failOdJobNo : failOdJobNoList){
//				orderMapper.updateZipCodeErrorYn(failOdJobNo);
//				
//				bulkData = new BulkData();
//				bulkData.setActionType(BulkData.Type.CREATE);
//				bulkData.setIndexName(index);
//				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//				logMap = new HashMap<String, Object>();
//				logMap.putAll(tmpLogMap);
//				logMap.put("reference_no_id", bulkData.getId());
//				logMap.put("description", "consignee zipcode error [OD_JOB_NO : "+failOdJobNo +"]");
//				if(zipCodeChange.getTargetData().equals("srchData")){
//					logMap.put("job_log", new Gson().toJson(zipCodeChange));
//				}else {
//					logMap.put("job_log", "");
//				}
//				bulkData.setJsonMap(logMap);
//				bulkList.add(bulkData);
//				
//				if((cnt++ % 1000) == 0){
//					sqlSession.flushStatements();
//					sqlSession.clearCache();
//				}
//			}
//			sqlSession.flushStatements();
//			sqlSession.clearCache();
//			sqlSession.commit();
//		}catch(Exception e) {
//			throw e;
//		}finally{
//			sqlSession.close();
//		}
//		
//		srchMap.clear();
//		String strFailRefNo = Util.null2str(returnMap.get("strFailRefNo"));
//		if(!strFailRefNo.equals("")){
//			String[] failHblNoArr = strFailRefNo.split(",");
//			StringBuffer failHblNo = new StringBuffer();
//			for(int i = 0; i < failHblNoArr.length; i++){
//				failHblNo.append(failHblNoArr[i]);
//				if(i != failHblNoArr.length - 1){
//					failHblNo.append(",");
//					if(i % 5 == 4){
//						failHblNo.append("\n");
//					}
//				}
//			}
//			
//			orderSaveResult.setResultCode("someFail");
//			srchMap.put("reqCnt", dataSize);
//			srchMap.put("failCnt", returnMap.get("nFail"));
//			srchMap.put("successCnt", returnMap.get("nSuccess"));
//			srchMap.put("failStr", failHblNo.toString());
//			orderSaveResult.setResultData(srchMap);
//		}else{
//			srchMap.put("reqCnt", dataSize);
//			srchMap.put("failCnt", "0");
//			srchMap.put("successCnt", dataSize);
//			orderSaveResult.setResultData(srchMap);
//		}
//		
//		// 처리 로그 기록
////		ProcessLogs processLogs = new ProcessLogs();
////		processLogs.setEntCode(zipCodeChange.getEntCode());
////		processLogs.setEntBusinessCode(zipCodeChange.getUserBusinessCode());
////		processLogs.setSolution("ECM");
////		processLogs.setBound(zipCodeChange.getBound());
////		processLogs.setBoundType("IMP");
////		processLogs.setProcessUserId(zipCodeChange.getUpdateUserId());
////		processLogs.setLogJobType("DATA");
////		processLogs.setJobFuncType("UPDATE");
////		processLogs.setDescription("consignee zipcode update");
////		processLogs.setJobLog(new Gson().toJson(zipCodeChange));
////		processLogs.setResult("ok");
////		processLogs.setEntBusinessPrintName(zipCodeChange.getLogUserBusinessPrintName());
////		processLogs.setProcessUserName(zipCodeChange.getLogUserName());
////		processLogs.setProcessUserNameEng(zipCodeChange.getLogUserNameEng());
////		commonService.insertLog(processLogs, request);
//		//건별 로그 남기도록 수정
//		commonService.insertLogBulk(bulkList);
//		return orderSaveResult;
//	}
	public OrderSaveResult updateConsigneeZipCode(ZipCodeChange zipCodeChange, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		String blTypeMh = Util.null2str(zipCodeChange.getBlTypeMh());
		
		Map srchMap = new HashMap();
		if(zipCodeChange.getTargetData().equals("srchData")){
			//등록,발송 화면에서 호출했을 경우 orderCustomerMapper
			if(Util.null2str(zipCodeChange.getScreen()).equals("ORDER_CUSTOMER")) {
				SearchOrderCustomerList searchOrderList = zipCodeChange.getSearchOrderCustomerList();
				/* N개 검색(멀티타입) */
				List<String> searchNoList = new ArrayList<String>();
				if(!"".equals(Util.null2str(searchOrderList.getSrchNo()))) {
					searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(searchOrderList.getSrchNo())).split(",")));
				}
				
				if(searchNoList.size() > 1){
					searchOrderList.setSearchType("multi");
					searchOrderList.setSearchNoList(searchNoList);
				}else{
					searchOrderList.setSearchType("");
					searchOrderList.setSrchNo(Util.null2str(searchOrderList.getSrchNo()).trim().replace(",", ""));
				}
				srchMap = BeanUtils.describe(searchOrderList);
				srchMap.put("srchStatusList", searchOrderList.getSrchStatusList());
				srchMap.put("srchCdTypeList", searchOrderList.getSrchCdTypeList());
				srchMap.put("srchCusIdVerifyList", searchOrderList.getSrchCusIdVerifyList());
				srchMap.put("srchCarryList", searchOrderList.getSrchCarryList());
				
				if(searchOrderList.getSearchType().equals("multi")){
					srchMap.put("searchNoList", searchOrderList.getSearchNoList());
				}
				String boundType = Util.null2str(srchMap.get("srchBoundType"));
				if(boundType.equals("AI")) {
					zipCodeChange.setBound("AIR");
				}else{
					zipCodeChange.setBound("SEA");
				}
				
			}
			//그 외 화면에서 호출했을 경우 orderMapper
			else {
				SearchOrderList searchOrderList = zipCodeChange.getSearchOrderList();
				/* N개 검색(멀티타입) */
				List<String> searchNoList = new ArrayList<String>();
				if(!"".equals(Util.null2str(searchOrderList.getSrchNo()))) {
					searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(searchOrderList.getSrchNo())).split(",")));
				}
				
				if(searchNoList.size() > 1){
					searchOrderList.setSearchType("multi");
					searchOrderList.setSearchNoList(searchNoList);
				}else{
					searchOrderList.setSearchType("");
					searchOrderList.setSrchNo(Util.null2str(searchOrderList.getSrchNo()).trim().replace(",", ""));
				}
				srchMap = BeanUtils.describe(searchOrderList);
				
				srchMap.put("srchCdTypeList", searchOrderList.getSrchCdTypeList());
				srchMap.put("srchClearanceStatusList", searchOrderList.getSrchClearanceStatusList());
				srchMap.put("srchLastPodStatusList", searchOrderList.getSrchLastPodStatusList());
				srchMap.put("srchCustomsIdVerifyList", searchOrderList.getSrchCustomsIdVerifyList());
				srchMap.put("srchCarryList", searchOrderList.getSrchCarryList());
				
				if(searchOrderList.getSearchType().equals("multi")){
					srchMap.put("searchNoList", searchOrderList.getSearchNoList());
				}
				String boundType = Util.null2str(srchMap.get("srchBoundType"));
				if(boundType.equals("AI")) {
					zipCodeChange.setBound("AIR");
				}else{
					zipCodeChange.setBound("SEA");
				}
			}
		}else{
			srchMap.put("blTypeMh", blTypeMh);
			
			if("Y".equals(zipCodeChange.getCheckMfSubGridYn())){
				srchMap.put("checkMfSubGridYn", "Y");
				srchMap.put("checkMfSubGridDataList", zipCodeChange.getCheckMfSubGridDataList());
			}else{
				srchMap.put("odJobNoList", zipCodeChange.getOdJobNoList());
			}
			
		}
		srchMap.put("targetData", zipCodeChange.getTargetData());
		List<Map<String, Object>> consigneeAddrList = null;
		if(Util.null2str(zipCodeChange.getScreen()).equals("ORDER_CUSTOMER")) {
			Util.getCryptographyYn(srchMap);
			Util.accessControlDatabase(srchMap, "loginCustomerLev|srchDateFormat|loginCustomerUpperLev|loginCustomerLowLev", false, "srchDateGbn", "od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.CONSIGNEE_HP|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
			consigneeAddrList = orderCustomerMapper.selectOrderCustomerChkList(srchMap);
		}else{
			if("srchData".equals(zipCodeChange.getTargetData())){
				String srchQueryType = Util.null2str(srchMap.get("srchQueryType"));
				if(srchQueryType.equals("ORDER")){
					Util.getCryptographyYn(srchMap);
					Util.accessControlDatabase(srchMap, "srchDateFormat|srchCusType", false, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
				}else if(srchQueryType.equals("MANIFEST_IMP")){
					Util.accessControlDatabase(srchMap, "srchDateFormat", false, "srchDateGbn", "noDateChk|ad.SIMPLE_DOC_DATE|od.AR_DATE|od.OB_DATE|ad.MF_DATE", "srchNoType", "od.HBL_NO|od.MBL_NO|od.MRN|ad.SIMPLE_DOC_NO|od.MASTER_OD_JOB_NO");
				}else if(srchQueryType.equals("MANIFEST_IMP_031")){
					Util.accessControlDatabase(srchMap, "srchDateFormat", false, "srchDateGbn", "noDateChk|ad.SIMPLE_DOC_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.HBL_NO|od.MBL_NO|od.MRN|ad.SIMPLE_DOC_NO");
				}
			}
			consigneeAddrList =  orderMapper.selectOrderChkList(srchMap);
		}
		
		Map<String,Object> returnMap = new HashMap<String,Object>();
		int dataSize = consigneeAddrList.size();
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", zipCodeChange.getEntCode());
		tmpLogMap.put("ent_business_code", zipCodeChange.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("bound", zipCodeChange.getBound());
		tmpLogMap.put("bound_type", "IMP");
		tmpLogMap.put("process_user_id", zipCodeChange.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", zipCodeChange.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", zipCodeChange.getLogUserName());
		tmpLogMap.put("process_user_name_eng", zipCodeChange.getLogUserNameEng());
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		String failOdJobNoStr = "", strFailRefNo = "";
		int nFail = 0, nSuccess = 0;
		
		if(dataSize > 0){
			List<CompletableFuture<Boolean>> result = new ArrayList<>();
			CompletableFuture<Boolean> asyncFuture;
			List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
			for(int i = 0; i < dataSize; i+=30){
				if(i + 30 < dataSize){
					asyncFuture = zipcodeConvert.getZipcode(consigneeAddrList.subList(i, i + 30), resultList);
				}else{
					asyncFuture = zipcodeConvert.getZipcode(consigneeAddrList.subList(i, dataSize), resultList);
				}
				result.add(asyncFuture);
			}
			CompletableFuture.allOf(result.toArray(new CompletableFuture[result.size()])).join();
			
			for(Map<String, Object> map : resultList){
				failOdJobNoStr += Util.null2str(map.get("failOdJobNoStr"));
				strFailRefNo += Util.null2str(map.get("strFailRefNo"));
				nFail += Util.null2int(map.get("nFail"));
				nSuccess += Util.null2int(map.get("nSuccess"));
			}
			
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				OrderMapper orderMapper2 = sqlSession.getMapper(OrderMapper.class);
				int cnt = 1;
				int seq = 1;
				for(Map<String, Object> consigneeAddrMap : consigneeAddrList){
					if(!Util.null2str(consigneeAddrMap.get("CONSIGNEE_ZIPCODE")).equals("")){
						orderMapper2.updateConsigneeZipCode(consigneeAddrMap);
						
//						bulkData = new BulkData();
//						bulkData.setActionType(BulkData.Type.CREATE);
//						bulkData.setIndexName(index);
//						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//						logMap = new HashMap<String, Object>();
//						logMap.putAll(tmpLogMap);
//						logMap.put("reference_no_id", bulkData.getId());
//						logMap.put("description", "consignee zipcode update [OD_JOB_NO : "+consigneeAddrMap.get("OD_JOB_NO") +"]");
//						if(zipCodeChange.getTargetData().equals("srchData")){
//							logMap.put("job_log", new Gson().toJson(zipCodeChange));
//						}else {
//							logMap.put("job_log", "");
//						}
//						bulkData.setJsonMap(logMap);
//						bulkList.add(bulkData);
						
						if((cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
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
		}
		
		//우편번호검증 오류 건은 1000건씩 돌며 ad.ZIPCODE_ERROR_YN 값을 Y로 업데이트함
		List<String> failOdJobNoList = new ArrayList<String>();
		failOdJobNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(failOdJobNoStr)).split(",")));
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
			int cnt = 1;
			int seq = 1;
			for(String failOdJobNo : failOdJobNoList){
				if(Util.isEmpty(failOdJobNo)){
					continue;
				}
				orderMapper.updateZipCodeErrorYn(failOdJobNo);
				
//				bulkData = new BulkData();
//				bulkData.setActionType(BulkData.Type.CREATE);
//				bulkData.setIndexName(index);
//				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//				logMap = new HashMap<String, Object>();
//				logMap.putAll(tmpLogMap);
//				logMap.put("reference_no_id", bulkData.getId());
//				logMap.put("description", "consignee zipcode error [OD_JOB_NO : "+failOdJobNo +"]");
//				if(zipCodeChange.getTargetData().equals("srchData")){
//					logMap.put("job_log", new Gson().toJson(zipCodeChange));
//				}else {
//					logMap.put("job_log", "");
//				}
//				bulkData.setJsonMap(logMap);
//				bulkList.add(bulkData);
				
				if((cnt++ % 1000) == 0){
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
		
		srchMap.clear();
		if(!strFailRefNo.equals("")){
			String[] failHblNoArr = strFailRefNo.split(",");
			StringBuffer failHblNo = new StringBuffer();
			for(int i = 0; i < failHblNoArr.length; i++){
				failHblNo.append(failHblNoArr[i]);
				if(i != failHblNoArr.length - 1){
					failHblNo.append(",");
					if(i % 5 == 4){
						failHblNo.append("\n");
					}
				}
			}
			
			orderSaveResult.setResultCode("someFail");
			srchMap.put("reqCnt", dataSize);
			srchMap.put("failCnt", nFail);
			srchMap.put("successCnt", nSuccess);
			srchMap.put("failStr", failHblNo.toString());
			orderSaveResult.setResultData(srchMap);
		}else{
			srchMap.put("reqCnt", dataSize);
			srchMap.put("failCnt", "0");
			srchMap.put("successCnt", dataSize);
			orderSaveResult.setResultData(srchMap);
		}
		
		// 처리 로그 기록
		
		bulkData = new BulkData();
		bulkData.setActionType(BulkData.Type.CREATE);
		bulkData.setIndexName(index);
		bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(1));
		logMap = new HashMap<String, Object>();
		logMap.putAll(tmpLogMap);
		logMap.put("reference_no_id", bulkData.getId());
		logMap.put("description", "consignee zipcode update [successCnt : " + nSuccess +", failCnt : " + nFail + "]");
		if(zipCodeChange.getTargetData().equals("srchData")){
			logMap.put("job_log", new Gson().toJson(zipCodeChange));
		}else {
			logMap.put("job_log", "");
		}
		bulkData.setJsonMap(logMap);
		bulkList.add(bulkData);
		
		commonService.insertLogBulk(bulkList);
		return orderSaveResult;
	}
	
	/**
	 * MULTI STOCK생성
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult insertMultiStock(OrderExclusion search, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Util.accessControlDatabase(search, "dateFormat|gmtTime", false, null, null, null, null);
		
		List<Map<String, Object>> dataList = orderMapper.selectMultiStockList(search);
		if(dataList != null && dataList.size() > 0){
			Map<String, Object> resultData = new HashMap<String, Object>();
			String errHblNoStr = "";
			for(Map<String, Object> data : dataList){
				if(Util.isEmpty(data.get("DELIVERY_CODE")) || Util.null2int(data.get("PIECES")) < 2 || "Y".equals(data.get("MULTI_YN"))){
					errHblNoStr += ", " + Util.null2str(data.get("HBL_NO"));
				}
			}
			
			if(errHblNoStr.length() > 2){
				orderSaveResult.setResultCode("A11337");	/** 처리할 수 없는 데이터가 있습니다. 확인 후 다시 처리해주세요. */
				resultData.put("errHblNoStr", errHblNoStr.substring(2));
				orderSaveResult.setResultData(resultData);
				return orderSaveResult;
			}
			
			BulkData bulkData = null;
			ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
			String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
			Map<String, Object> logMap = null;
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", search.getEntCode());
			tmpLogMap.put("ent_business_code", search.getUserBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("bound", search.getBound());
			tmpLogMap.put("bound_type", search.getBoundType());
			tmpLogMap.put("process_user_id", search.getUpdateUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "UPDATE");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			
			tmpLogMap.put("ent_business_print_name", search.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", search.getLogUserName());
			tmpLogMap.put("process_user_name_eng", search.getLogUserNameEng());
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
			
			List<String> checkDataList = new ArrayList<String>();
			SearchOrderBatchUpdate updateData = new SearchOrderBatchUpdate();
			updateData.setUpdateField("MULTI_YN");
			updateData.setUpdateValue("Y");
			updateData.setUpdateUserId(search.getUpdateUserId());
			updateData.setGmtTime(search.getGmtTime());
			
			int failCnt = 0;
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
				int seq = 1;
				boolean useHblNo;
				if("multiStockSplit".equals(search.getProcessType())){
					useHblNo = true;
				}else{
					useHblNo = false;
				}
				for(Map<String, Object> data : dataList){
					data.put("gmtTime", search.getGmtTime());
					data.put("dateFormat", search.getDateFormat());
					data.put("registerUserId", search.getUpdateUserId());
					data.put("createStockNoCnt", data.get("PIECES"));
					data.put("bound", data.get("BOUND"));
					data.put("boundType", data.get("BOUND_TYPE"));
					data.put("odJobNo", data.get("OD_JOB_NO"));
					data.put("profileName", profile);
					this.setMultiStock(data, Util.null2int(data.get("STOCK_CUST_LV")), 1, true, useHblNo);
					
					if(data.get("multiList") != null){
						orderBatchMapper.insertMulti(data);	/** TB_ECM_OD_MULTI Insert */
						checkDataList.add(Util.null2str(data.get("OD_JOB_NO")));
						
						bulkData = new BulkData();
						bulkData.setActionType(BulkData.Type.CREATE);
						bulkData.setIndexName(index);
						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
						logMap = new HashMap<String, Object>();
						logMap.putAll(tmpLogMap);
						logMap.put("reference_no_id", bulkData.getId());
						logMap.put("job_no", data.get("OD_JOB_NO"));
						logMap.put("description", "INSERT MULTI STOCK [HBL_NO : " + Util.null2str(data.get("HBL_NO")) +"]");
//						tmpLogMap.put("job_log", new Gson().toJson(orderDelete));
						bulkData.setJsonMap(logMap);
						bulkList.add(bulkData);
					}else{
						failCnt++;
						errHblNoStr += ", " + Util.null2str(data.get("HBL_NO"));
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
			
			if(errHblNoStr.length() > 2){
				orderSaveResult.setResultCode("A11238");	/** 총 #1#건 중 #2#건 처리성공하고 #3#건 처리실패했습니다. */
				resultData.put("errHblNoStr", errHblNoStr.substring(2));
				resultData.put("reqCnt", dataList.size());
				resultData.put("successCnt", checkDataList.size());
				resultData.put("failCnt", failCnt);
				orderSaveResult.setResultData(resultData);
			}
			
			if(checkDataList.size() > 0){
				updateData.setCheckDataList(checkDataList);
				orderMapper.updateOrderBatch(updateData);	/** MULTI_YN = 'Y' Update */
			}
			
			commonService.insertLogBulk(bulkList);	/** 처리 로그 기록 */
		}
		
		return orderSaveResult;
	}
	
	/** 멀티스탁생성 */
	public void setMultiStock(Map<String, Object> data, int custCodeLv, int seqNoId, boolean multiFlag, boolean useHblNo) throws Exception {
		String currentNo = null;
		String deliveryCode = Util.null2str(data.get("DELIVERY_CODE"));
		int createStockNoCnt = Util.null2int(data.get("createStockNoCnt"));
		if(seqNoId == 1 && multiFlag && !"KOREX".equals(deliveryCode) && !"EMS".equals(deliveryCode)){
			multiFlag = false;
		}
		if(useHblNo && createStockNoCnt == Util.null2int(data.get("PIECES"))){
			currentNo = Util.null2str(data.get("HBL_NO"));
			data.put("deliveryCode", deliveryCode);
		}else{
			String agentCode = null;
			if (custCodeLv == 0) {
				agentCode = Util.null2str(data.get("ENT_CODE"));
			}else{
				agentCode = Util.null2str(data.get("CUST_CODE_LV" + Util.null2str(custCodeLv)));
			}
			
			if(multiFlag){
				data.put("deliveryCode", deliveryCode + "MUL");
			}else{
				data.put("deliveryCode", deliveryCode);
			}
			data.put("agentCode", agentCode);
			data.put("createCnt", seqNoId);
			Util.accessControlDatabase(data, "odJobNo|createCnt", false, null, null, null, null);
			Map<String, Object> hblStockMap = commonMapper.selectHblNoStock(data);
			currentNo = Util.null2str(hblStockMap.get("CURRENT_NO"));
		}
		
		
		if(!Util.isEmpty(currentNo)){
			seqNoId++;
			OrderMulti odMulti = new OrderMulti();
			odMulti.setDeliveryNo(currentNo);
			odMulti.setDeliveryCode(Util.null2str(data.get("deliveryCode")));
			odMulti.setPieces(1);
			odMulti.setPaymentGubn("01");
			List<OrderMulti> odMultiList;
			if (data.get("multiList") == null){
				odMultiList = new ArrayList<OrderMulti>();
			} else {
				odMultiList = (List<OrderMulti>)data.get("multiList");
			}
			odMultiList.add(odMulti);
			data.put("multiList", odMultiList);
			
			if(--createStockNoCnt > 0) {
				data.put("createStockNoCnt", createStockNoCnt);
				this.setMultiStock(data, custCodeLv, seqNoId, multiFlag, useHblNo);
			}
		}else{	/** stockNo를 생성하지못하면 상위레벨로 다시 생성, custCodeLv0(도입사)로 stockNo생성할수없으면 종료 */
			if(custCodeLv > 0){
				 this.setMultiStock(data, --custCodeLv, seqNoId, multiFlag, useHblNo);
			 }else{
				 if(multiFlag){
					 this.setMultiStock(data, Util.null2int(data.get("STOCK_CUST_LV")), seqNoId, false, useHblNo);	/** 대한통운멀티,우체국멀티 stock이 없을경우 대한통운,우체국으로 처리 */
				 }else{
					 data.put("multiList", null);
				 }
			 }
		}
	}
	
	/**
	 * 고객관리 insert
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List<OrderCs> insertCs(OrderCs cs, HttpServletRequest request) throws Exception {
		Util.accessControlDatabase(cs, "gmtTime|userLanguage", false, null, null, null, null);
		orderMapper.insertCs(cs);
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", cs.getEntCode());
		tmpMap.put("refJobNo", cs.getRefJobNo());
		tmpMap.put("userLanguage", cs.getUserLanguage());
		
		List<OrderCs> csList = orderMapper.selectCs(tmpMap);
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(cs.getEntCode());
		processLogs.setEntBusinessCode(cs.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(cs.getBound());
		processLogs.setBoundType(cs.getBoundType());
		processLogs.setProcessUserId(cs.getUpdateUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("ADD");
		processLogs.setDescription("CS INSERT");
		processLogs.setJobLog(new Gson().toJson(cs));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(cs.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(cs.getLogUserName());
		processLogs.setProcessUserNameEng(cs.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return csList;
	}
	
	/**
	 * 고객관리 update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List<OrderCs> updateCs(OrderCs cs, HttpServletRequest request) throws Exception {
		Util.accessControlDatabase(cs, "userLanguage", false, null, null, null, null);
		orderMapper.updateCs(cs);
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", cs.getEntCode());
		tmpMap.put("refJobNo", cs.getRefJobNo());
		tmpMap.put("userLanguage", cs.getUserLanguage());
		List<OrderCs> csList = orderMapper.selectCs(tmpMap);
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(cs.getEntCode());
		processLogs.setEntBusinessCode(cs.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(cs.getBound());
		processLogs.setBoundType(cs.getBoundType());
		processLogs.setProcessUserId(cs.getUpdateUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("ADD");
		processLogs.setDescription("CS INSERT");
		processLogs.setJobLog(new Gson().toJson(cs));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(cs.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(cs.getLogUserName());
		processLogs.setProcessUserNameEng(cs.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return csList;
	}
	
	/**
	 * 고객관리 delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List<OrderCs> deleteCs(OrderCsDelete csDelete, HttpServletRequest request) throws Exception {
		Util.accessControlDatabase(csDelete, "userLanguage", false, null, null, null, null);
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", csDelete.getEntCode());
		tmpMap.put("seqNoId", csDelete.getSeqNoId());
		orderMapper.deleteCs(tmpMap);
		tmpMap.remove("seqNoId");
		tmpMap.put("refJobNo", csDelete.getRefJobNo());
		tmpMap.put("userLanguage", csDelete.getUserLanguage());
		List<OrderCs> csList = orderMapper.selectCs(tmpMap);
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(csDelete.getEntCode());
		processLogs.setEntBusinessCode(csDelete.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(csDelete.getBound());
		processLogs.setBoundType(csDelete.getBoundType());
		processLogs.setProcessUserId(csDelete.getUpdateUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("ADD");
		processLogs.setDescription("CS INSERT");
		processLogs.setJobLog(new Gson().toJson(csDelete));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(csDelete.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(csDelete.getLogUserName());
		processLogs.setProcessUserNameEng(csDelete.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return csList;
	}
	
	
	
	
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row) throws Exception {
		return this.dataChk(data, name, length, type, errMsg, row, true);
	}
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row, boolean null2zero) throws Exception {
		//TODO 엑셀 셀타입 텍스트로 고정시킬수있는지?  없다면 추가개발필요
		if(type.equals("N")){
			if (Util.null2str(data).equals("")){
				if(null2zero){
					data = "0";
				}else{
					data = null;
				}
			} else {
				try {
//					int idx = data.indexOf("E");	/** 8자리가 넘어가면 123456789.123 => 1.23456789123E8 로 표현됨 */
//					if(idx > -1){
//						int k = Util.null2int(data.substring(idx + 1)) + 1;
//						data = data.substring(0, idx).replace(".", "");
//						data = data.substring(0, k) + "." + data.substring(k);
//					}
					int dotIndex = data.indexOf(".");
					if (dotIndex > 0) {
						data = data.substring(0, dotIndex);
					}
					if(data.length() > Util.null2int(length)){
						errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
					}
					Integer.parseInt(data);	/** 숫자인지 체크 */
				}catch (Exception e){
					errMsg.append("[" + name + "] DATA TYPE 오류(행 : " + row + ", 입력값 : " + data + ") <br />");
				}
			}
		}else if(type.equals("DATE")){
			if (Util.null2str(data).equals("")) {
				data = null;
			}else{
				try{
//					if(!data.startsWith("2")){
//						String[] tmpData = data.split("/");
//						data = "20" + tmpData[2] + (tmpData[0].length() == 1 ? "0" + tmpData[0] : tmpData[0]) + tmpData[1];
//					}else{
						SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyyMMdd");
						data = data.replaceAll("[-./]", "").substring(0, 8);
						data = sdFormat.format(sdFormat2.parse(data));
//					}
				}catch (Exception e){
					errMsg.append("[" + name + "] DATA TYPE 오류(행 : " + row + ", 입력값 : " + data + ") <br />");
				}
			}
		}else if("DBL".equals(type)){
			if (Util.null2str(data).equals("")) {
				if(null2zero){
					data = "0";
				}else{
					data = null;
				}
			} else {
				try {
					Double.parseDouble(data);
				} catch (Exception e) {
					errMsg.append("[" + name + "] DATA TYPE 오류(행 : " + row + ", 입력값 : " + data + ") <br />");
				}
			}
//			int idx = data.indexOf("E");	/** 8자리가 넘어가면 123456789.123 => 1.23456789123E8 로 표현됨 */
//			if(idx > -1){
//				int k = Util.null2int(data.substring(idx + 1)) + 1;
//				data = data.substring(0, idx).replace(".", "");
//				data = data.substring(0, k) + "." + data.substring(k);
//			}
			if(data != null) {
				String[] lengthCnt = Util.null2str(length).split("\\.");
				String[] lengthData = data.split("\\.");
				int j = 0;
				if(lengthCnt.length > 1){
					j = Util.null2int(lengthCnt[1]);
				}
				if(lengthData[0].length() > Util.null2int(lengthCnt[0]) - j) {
					errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + ") <br />");
				} else if(lengthData.length > 1 && lengthData[1].length() > j) {
					errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + ") <br />");
				}
			}
		}else if("S".equals(type)){
			if(Util.null2str(data).equals("")){
				data = null;
			}else{
				if(!"pass".equals(length) && Util.getKor2ByteLength(data) > Util.null2int(length)) {
					errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
				}
			}
		}else if("SN".equals(type)){
			if(Util.null2str(data).equals("")){
				data = null;
			}else{
				if(!"pass".equals(length) && data.length() > Util.null2int(length)) {
					errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
				}
			}
		}else{
			if(Util.null2str(data).equals("")){
				data = null;
			}else{
				if(data.length() > Util.null2int(length)) {
					errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
				}
			}
		}
		return data;
	}
	
	/**
	 * 개인통관부호 검증을 위한 데이터 저장
	 * @param index
	 * @param entCode
	 * @param customsVerificationList
	 * @throws Exception 
	 */
	public void setCustomsVerification(String index, String entCode, List<CustomsVerification> customsVerificationList, Map<String, Object> map) throws Exception {
		IndexApi indexApi = new IndexApi(restClient);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
	
		List<JSONObject> redisList = new ArrayList<JSONObject>();
		
		String id = DateTime.getDate("yyyyMMddhhmmssSSS") + "-" + entCode;
		DocumentApi documentApi = new DocumentApi(restClient);
		Map<String, Object> esMap = new HashMap<String, Object>();
		esMap.put("ent_code", entCode);
		esMap.put("id", id);
		esMap.put("order", "");
		esMap.put("status", "wait");
		esMap.put("total_count", 0);
		esMap.put("fail_count", 0);
		esMap.put("register_date_time", DateUtils.nowWithMillisResolution());
		
		String userId = Util.null2str(map.get("USER_ID"));
		String userName = Util.null2str(map.get("USER_NAME"));
		
		if(customsVerificationList.size() > 0){
			if(userId.equals("")){
				userId = customsVerificationList.get(0).getUserId();
			}
			if(userName.equals("")) {
				userName = customsVerificationList.get(0).getUserName();
			}
		}
		esMap.put("register_user_id", userId);
		esMap.put("register_user_name", userName);
		
		for (int i = 0; i < customsVerificationList.size(); i++) {
			CustomsVerification customsVerification = customsVerificationList.get(i);
			JSONObject json = new JSONObject();
			json.put("resultIndex", index);
			json.put("resultId", id);
			json.put("entCode", customsVerification.getEntCode());
			json.put("entBusinessCode", customsVerification.getEntBusinessCode());
			json.put("userId", customsVerification.getUserId());
			json.put("userName", customsVerification.getUserName());
			json.put("odJobNo", customsVerification.getOdJobNo());
			json.put("masterOdJobNo", customsVerification.getMasterOdJobNo());
			json.put("mablNo", customsVerification.getMblNo());
			json.put("hblNo", customsVerification.getHblNo());
			json.put("nameType", customsVerification.getNameType());
			
			redisList.add(json);
		}
		
		// 결과 조회를 위해 ES에 저장
		documentApi.createDocument(index, id, esMap);
		
		// Batch 작업을 위해 Redis에 저장
    	String key = "E-COMMERCE_CUSTOMS_CHECK_" + entCode;
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        listOperations.rightPush(key, redisList.toString());
	}
	
	/**
	 * 개인통관부호 검증 결과 리스트 조회
	 * @param index
	 * @return
	 * @throws Exception
	 */
	/**
	 * 
	 * @param searchCustomsVerificationResult
	 * @return
	 * @throws Exception
	 */
	public CustomsVerificationResultList selectCutstomsVerificationList(SearchCustomsVerification searchCustomsVerification) throws Exception {
		CustomsVerificationResultList customsVerificationResultList = new CustomsVerificationResultList();

        try {
			// 검색할 Indices
	        String[] indices = new String[1];
	        indices[0] = "ecm-customs-verification-result-*";
	        
	        // Sort 조건 처리
	        List<SortData> sortList = new ArrayList<SortData>();

	        if (searchCustomsVerification.getSort() == null || searchCustomsVerification.getSort().size() == 0) {
		        SortData sortData = new SortData();
		        sortData.setField("register_date_time");
		        sortData.setType(SortOrder.DESC);
		        sortList.add(sortData);
	        } else {
	        	for (int i = 0; i < searchCustomsVerification.getSort().size(); i++) {
	        		Sort data = searchCustomsVerification.getSort().get(i);
	    	        SortData sortData = new SortData();
			        sortData.setField(Converter.camelToSnake(data.getField()));
			        if ("asc".equals(data.getDir()))	sortData.setType(SortOrder.ASC);
			        else								sortData.setType(SortOrder.DESC);
			        sortList.add(sortData);
	        	}
	        }
	        
	        // Parameter 처리
			int size = searchCustomsVerification.getPageSize();
	        int from = searchCustomsVerification.getPage() > 0 ? (searchCustomsVerification.getPage() - 1) * size : 0;
	        long startDate = searchCustomsVerification.getSrchStartDate();
	        long endDate = searchCustomsVerification.getSrchEndDate();

	        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
	        boolQueryBuilder.filter(QueryBuilders.matchQuery("ent_code", searchCustomsVerification.getSrchEntCode()));
        	boolQueryBuilder.filter(QueryBuilders.rangeQuery(searchCustomsVerification.getSrchDateGbn()).gte(startDate).lte(endDate));

	        
	        SearchApi searchApi = new SearchApi(restClient);
	        SearchResponse response;
	        if (size == 0)	response = searchApi.searchScroll(boolQueryBuilder, elasticsearchScrollPage_, indices, searchCustomsVerification.getSrchScrollId(), sortList);
        	else			response = searchApi.search(boolQueryBuilder, from, size, 0, indices, sortList);
	        
	        if (response != null) {
	        	// 대기순번 검색을 위한 Redis 데이터 리스트
	        	JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
				String key = "E-COMMERCE_CUSTOMS_CHECK_" + searchCustomsVerification.getSrchEntCode();
				ListOperations<String, String> listOperations = redisTemplate.opsForList();
				Long redisListSize = listOperations.size(key);
				
		        String scrollId = response.getScrollId();
		    	SearchHits hits = response.getHits();
		    	customsVerificationResultList.setScrollId(scrollId);

				final ObjectMapper mapper = new ObjectMapper();
				mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				mapper.setSerializationInclusion(Include.NON_NULL);
				
		    	SearchHit[] resultHits = hits.getHits();
		    	if (size == 0)	customsVerificationResultList.setTotal(resultHits.length);
		    	else			customsVerificationResultList.setTotal(hits.getTotalHits().value);
		    	
		    	List<CustomsVerificationResult> list = new ArrayList<CustomsVerificationResult>();
		    	if (resultHits.length > 0) {
		    		for (SearchHit hit : resultHits) {
		    			CustomsVerificationResult customsVerificationResult = mapper.convertValue(hit.getSourceAsMap(), CustomsVerificationResult.class);
		    			customsVerificationResult.setIndexName(hit.getIndex());
						// 대기순번 세팅
						int order = 0;
						if (redisListSize > 0) {
							for (int i = 0; i < redisListSize; i++) {
								String result = listOperations.index(key, i);
								Object obj = parser.parse(result);
								JSONArray jsonArray = (JSONArray) obj;
								order++;
								if (((JSONObject) jsonArray.get(0)).get("resultId").equals(customsVerificationResult.getId())) {
									customsVerificationResult.setOrder(order);
									break;
								}
							}
						}
						
						list.add(customsVerificationResult);
					}
					
		    	} else {
		    		// 메모리 확보를 위해 ScrollId를 ElasticSearch에서 삭제한다.
		    		if (!"".equals(Util.null2str(searchCustomsVerification.getSrchScrollId()))) {
		    			searchApi.clearScollRequest(searchCustomsVerification.getSrchScrollId());
		    		}
		    	}
				
		    	customsVerificationResultList.setCustomsVerificatitonResult(list);
	        }
			
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		} finally {}
        
		return customsVerificationResultList;
	}
	
	/** 대한통운 주소오류 체크 */
	public OrderSaveResult korexAddressCheck(SearchPrintOut search) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		List<Map<String, Object>> dataList = this.selectDeliveryPrintout(search);
		String address;
		Map<String, Object> checkMap = new HashMap<String, Object>();
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			for (Map<String, Object> dataMap : dataList) {
				address = Util.null2str(dataMap.get("CONSIGNEE_ADDRESS_LOC"));
				/** 2025.11.21 주소에 괄호가 있을 경우 괄호 안의 내용은 제외하고 중복 체크 */
				if(address.indexOf("(") > -1 && address.indexOf(")") > -1){
					address = address.substring(0, address.indexOf("(")) + address.substring(address.indexOf(")") + 1, address.length());
				}
				checkMap.clear();
				for(String word : address.split(" ")){
					if(word.length() > 1){
						if(checkMap.get(word) == null){
							checkMap.put(word, "Y");
						}else{
							orderBatchMapper.updatePrintOutError(Util.null2str(dataMap.get("OD_JOB_NO")));
						}
					}
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
		return orderSaveResult;
	}
	
	/**
	 * 출력물에 들어갈 주소,우편번호 조회
	 * @param SearchPrintOut
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectDeliveryPrintout(SearchPrintOut search) throws Exception {
		return orderMapper.selectDeliveryPrintout(search);
	}		
	
	/**
	 * 우체국 출력물 생성 전에 집배코드 체크
	 * @param SearchPrintOut
	 * @throws Exception
	 */
	public Map<String, Object> printOutConvertEms(List<Map<String, Object>> dataList) throws Exception {
		Map<String, Object> zipMap = null; // 우체국 집배코드 결과
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		returnMap.put("resultCode", "Y"); // 우체국 zipCode 데이터가 존재 유무와 상관없이 출력요청
		
		if(dataList.size() > 0){
			Map<String, String> tmpMap = new HashMap<String, String>();
			String emsDelivAreaKey = "";
			tmpMap.put("entCode", Util.null2str(dataList.get(0).get("ENT_CODE")));
			List<DeliveryInterface> deliveryInterface = new ArrayList<>();
			if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")){
				deliveryInterface = orderMapper.selectDeliveryTrackingYnVe(tmpMap);
			}else {
				deliveryInterface = orderMapper.selectDeliveryTrackingYn(tmpMap);
			}
			if(deliveryInterface != null && deliveryInterface.size() > 0){
				emsDelivAreaKey = Util.null2str(deliveryInterface.get(0).getEpostDelivAreaKey());
			}
			
			if(emsDelivAreaKey.equals("")){
				return returnMap;
			}
		
			String zipcode = "";
			String address = "";
			String delivAreaCd = "";
	
			HttpURLConnection conn = null;
			URL url = null;
			Document doc = null;
			SAXBuilder builder = new SAXBuilder();
//			String epost = "http://biz.epost.go.kr/KpostPortal/openapi?regkey=da18ce74cbb747fb01706146577200&target=delivArea&zip=";
			String epost = "http://biz.epost.go.kr/KpostPortal/openapi?regkey=" + URLEncoder.encode(emsDelivAreaKey, "EUC-KR") + "&target=delivArea&zip=";
			
			
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
				for (Map<String, Object> dataMap : dataList) {				
					
					zipcode = Util.null2str(dataMap.get("CONSIGNEE_ZIPCODE"));
					address = Util.null2str(dataMap.get("CONSIGNEE_ADDRESS_LOC"));
					
					url = new URL(epost + URLEncoder.encode(zipcode, "EUC-KR") + "&addr=" + URLEncoder.encode(address, "EUC-KR"));
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestProperty("accept-language","ko");
					doc = builder.build(conn.getInputStream());
					delivAreaCd = Util.null2str(doc.getRootElement().getChildText("delivAreaCd"));
					if(!delivAreaCd.equals("")){
						zipMap = new HashMap<String, Object>();
						zipMap.put("odJobNo",dataMap.get("OD_JOB_NO"));
						zipMap.put("registerUserId",dataMap.get("registerUserId"));
						if(delivAreaCd.length() > 1){
							zipMap.put("CENTRAL_STATION_NO", delivAreaCd.substring(0, 2));
						}
						zipMap.put("CENTRAL_STATION", doc.getRootElement().getChildText("arrCnpoNm"));
						if(delivAreaCd.length() > 4){
							zipMap.put("DELIVERY_NO", delivAreaCd.substring(2, 5));
						}
						zipMap.put("DELIVERY", doc.getRootElement().getChildText("delivPoNm"));
						if(delivAreaCd.length() > 5){
							zipMap.put("DELIVERY_TEAM_NO", delivAreaCd.substring(5, 7));
						}
						if(delivAreaCd.length() > 8){
							zipMap.put("DELIVERY_PEOPLE_NO", delivAreaCd.substring(7, 9));
						}
						zipMap.put("GUBUN", doc.getRootElement().getChildText("courseNo"));
						orderBatchMapper.insertZipCodeInfo(zipMap);
					}
					
				}
			
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
			} catch (Exception e) {
				returnMap.put("resultCode", "N");
				returnMap.put("returnMessage", "EXCEPTION[" + e.getMessage() + "]");
				log.error(Util.getPrintStacTraceString(e));
				return returnMap;
			} finally {
				sqlSession.close();
			}
		}

		return returnMap;
	}	
	
	
	/**
	 * 배송사 인터페이스 정보 조회
	 * @param UpdateDelivery
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> selectDeliveryInterface(Map<String, Object> map) throws Exception {
		
		/** 2022-05-30 출력은 거래처코드 COMMON으로 고정 */
//		//거래처인 경우
//		if(!"".equals(Util.null2str(map.get("srchCustomerCode")))) {
//			//LV1 추출후 srchCustomerCode에 넣어 배송사 Interface 정보 조회
//			Map<String,Object> paramMap = new HashMap<String,Object>();
//			paramMap.put("entCode", map.get("entCode"));
//			paramMap.put("userId", map.get("userId"));
//			Map<String,Object> lvlMap = orderCustomerMapper.selectCusLvl(paramMap);
//			map.put("srchCustomerCode", Util.null2str(lvlMap.get("CUST_CODE_LV1")));
//		}
		

		return deliveryMapper.selectDeliveryInterface(map);
	}
	
	
	/**
	 * 대한통운  집배코드 정보 INSERT
	 * @param map
	 * @throws Exception
	 */
	public void insertZipCodeInfo(Map<String, Object> map) throws Exception {
		orderMapper.insertZipCodeInfo(map);
	}		
	
	/**
	 * 출력물 출력여부 및 출력날짜 업데이트
	 * @param SearchPrintOut
	 * @throws Exception
	 */	
	public void updatePrintOutDate(SearchPrintOut search) throws Exception {
		orderMapper.updatePrintOutDate(search);
	}

	/**
	 * 대한통운(KOREX),우체국(EMS),한진택배(HANJIN) 트래킹 수집 여부 조회
	 * @param entCode
	 * @throws Exception
	 */
	public List<DeliveryInterface> selectDeliveryTrackingYn(String entCode) throws Exception {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("entCode", entCode);
		if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")){
			return orderMapper.selectDeliveryTrackingYnVe(tmpMap);
		}else {
			return orderMapper.selectDeliveryTrackingYn(tmpMap);
		}
	}
	
	/**
	 * 대한통운(KOREX),우체국(EMS) 출력물 생성후 출력물 데이터 삭제
	 * @param SearchPrintOut
	 * @throws Exception
	 */	
	public OrderSaveResult deletePrintoutData(SearchPrintOut search) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		orderMapper.deletePrintoutData(search);
		return orderSaveResult;
	}	

	public Order selectOrderPodStatusList(SearchOrderList search) {
		Order order = new Order();
		order.setPodList(orderMapper.selectOrderPodStatusList(search));
		
		if(Util.null2str(search.getBmsYn()).equals("Y")) {
			List<OrderPod> podList = order.getPodList();
			if(podList.size() > 0) {
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("srchOdJobNo", search.getSrchOdJobNo());
				List<CarryIn> carryInList = bmsInMapper.selectNoInfoCarryInWithPlan(param);
				
				OrderPod orderPod = podList.get(0);
				
				for(CarryIn carryIn : carryInList) {
					if(!Util.isEmpty(carryIn.getOdJobNo())) {
						orderPod.setShipmentNo(carryIn.getCargoReferenceNo());
						orderPod.setHblNo(carryIn.getBlNo());
						orderPod.setMblNo(carryIn.getMblNo());
					}
				}
			}
		}
		return order;
	}

	public List<Order> checkDeliveryPrintYn(SearchPrintOut search) {
		return orderMapper.checkDeliveryPrintYn(search);
	}	
	
	/**
	 * 물품소재지정보 조회
	 * @param map
	 * @throws Exception
	 */
	public Order getGoodsLocInfo(Map<String, String> map) throws Exception {
		Util.accessControlDatabase(map, "userLanguage", false, null, null, null, null);
		return orderMapper.getGoodsLocInfo(map);
	}
	
	/**
	 * Order 수량, 중량 (Master 기준)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Order selectOrderGwtPieces(SearchOrderList search) throws Exception {
		return orderMapper.selectOrderGwtPieces(search);
	}
	
	/**
	 * 홀드사유 조회
	 * @param map
	 * @throws Exception
	 */
	public List<OrderHold> selectHold(Map<String, String> map) throws Exception {
		Util.accessControlDatabase(map, "dateFormat", false, null, null, null, null);
		return orderMapper.selectHold(map);
	}
	
	/**
	 * 홀드 저장
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveHold(Order order) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Util.accessControlDatabase(order, "gmtTime", false, null, null, null, null);
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", order.getEntCode());
		tmpMap.put("odJobNo", order.getOdJobNo());
		tmpMap.put("registerUserId", order.getRegisterUserId());
		tmpMap.put("dateFormat", order.getDateFormat());
		tmpMap.put("gmtTime", order.getGmtTime());
		
		String action = order.getAction(); /** insert/update구분 */
        List<OrderHold> holdList = order.getHoldList();
        List<OrderHold> cancelHoldList = new ArrayList<>();

        if(action.equals("update")) {
            if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")) {
                //허브넷은 홀드취소 인 데이터를 인터페이스 테이블에 저장해야함.
                Map<String, String> holdMap = new HashMap<>();
                holdMap.put("srchOdJobNo", order.getOdJobNo());
                holdMap.put("srchEntCode", order.getEntCode());
                holdMap.put("dateFormat", order.getDateFormat());
                List<OrderHold> delHoldList = orderMapper.selectHold(holdMap);

                for (OrderHold holdData1 : delHoldList) {
                    boolean found = false;

                    for (OrderHold holdData2 : holdList) {
                        if(holdData1.getHoldCode().equals(holdData2.getHoldCode())){
                            found = true;
                            break;
                        }
                    }

                    if(!found){
                        holdData1.setHoldGubn("C");
                        cancelHoldList.add(holdData1);
                    }
                }
            }
            orderMapper.deleteHold(tmpMap);
        }

        if(holdList != null && holdList.size() > 0) {
			tmpMap.put("holdList", holdList);
			tmpMap.put("userLanguage", order.getUserLanguage());
			orderMapper.insertHold(tmpMap);
			
			// 히스토리 기록
			for(OrderHold hold : holdList) {
				tmpMap.put("entBusinessCode", "");
				tmpMap.put("solution", "ECM");
				tmpMap.put("updateUserId", order.getRegisterUserId());
				tmpMap.put("stockType", "POD");
				tmpMap.put("stockSign", "POD");
				tmpMap.put("stockSeqLength", 8);
				tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				tmpMap.put("stockRemark", "ECM POD JOB NO");
				tmpMap.put("podStatus", "ECM286");
				tmpMap.put("podName", "HOLD");
				tmpMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
				tmpMap.put("podTableName", 	"TB_ECM_POD_05");
				tmpMap.put("remarks", "POPUP SAVE [HBL_NO : "+order.getHblNo()+", HOLD_CODE : "+hold.getHoldCode()+"]");
				orderMapper.insertHoldHistory(tmpMap);
			}
		}
        
        if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")) {
        	 if(cancelHoldList != null && cancelHoldList.size() > 0) {
        		// 히스토리 기록
     			for(OrderHold hold : cancelHoldList) {
     				tmpMap.put("entBusinessCode", "");
     				tmpMap.put("solution", "ECM");
     				tmpMap.put("updateUserId", order.getRegisterUserId());
     				tmpMap.put("stockType", "POD");
     				tmpMap.put("stockSign", "POD");
     				tmpMap.put("stockSeqLength", 8);
     				tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
     				tmpMap.put("stockRemark", "ECM POD JOB NO");
     				tmpMap.put("podStatus", "ECM286");
     				tmpMap.put("podName", "HOLD");
     				tmpMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
     				tmpMap.put("podTableName", 	"TB_ECM_POD_05");
     				tmpMap.put("remarks", "POPUP CANCEL [HBL_NO : "+order.getHblNo()+", HOLD_CODE : "+hold.getHoldCode()+"]");
     				orderMapper.insertHoldHistory(tmpMap);
     			}
        	 }
        }

        //허브넷인 경우에는 인터페이스 테이블에 홀드 데이터를 저장해줘야함.
        // 홀드 List
        // 홀드 처리인 경우에는 인터페이스 테이블에 저장 X
        // 홀드 취소 List
        if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")){
        	List<Map<String,Object>> checkList = new ArrayList<Map<String,Object>>();
            holdList.addAll(cancelHoldList);
            if(holdList != null && holdList.size() > 0) {
                for (OrderHold hold : holdList) {
                    if ("C".equals(Util.null2str(hold.getHoldGubn())) || (!"C".equals(Util.null2str(hold.getHoldGubn())) && !"Y".equals(hold.getHoldReleaseYn()))) {
                        SearchOrder searchOrder = new SearchOrder();
                        searchOrder.setSrchOdJobNo(order.getOdJobNo());
                        searchOrder.setSrchEntCode(order.getEntCode());
                        searchOrder.setDateFormat(order.getDateFormat());
                        searchOrder.setUserLanguage(order.getUserLanguage());
                        Order orderInfo = orderMapper.selectOrder(searchOrder);

                        Map<String, Object> checkMap = new HashMap<>();
                        if (orderInfo != null && !"".equals(Util.null2str(orderInfo.getMblNo())) && !"".equals(Util.null2str(orderInfo.getHblNo()))) {
                            checkMap.put("MBL_NO", orderInfo.getMblNo());
                            checkMap.put("HBL_NO", orderInfo.getHblNo());
                            checkMap.put("HOLD_CODE", hold.getHoldCode());
                            if ("C".equals(hold.getHoldGubn())) {
                                checkMap.put("STATUS_CODE_ETC1", "C");
                            } else {
                                checkMap.put("STATUS_CODE_ETC1", "");
                            }
                            checkMap.put("STATUS_CODE", "H01");
                            checkMap.put("BOUND_TYPE",orderInfo.getBoundType());
                            checkMap.put("BOUND",orderInfo.getBound());
                            checkList.add(checkMap);
                        }
                    }
                }
                Map<String,Object> returnMap = new HashMap<String, Object>();
        		returnMap.put("hubnHoldList", checkList);
        		orderSaveResult.setResultData(returnMap);
            }
        }

		orderSaveResult.setOdJobNo(order.getOdJobNo());
		return orderSaveResult;
	}
	
	/**
	 * UBI 출력물 생성
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public boolean getReportResultFile(Map<String,Object> params){
		Map<String,Object> result = new HashMap<String,Object>();
//		String jrf_dir = customProperties.getReportJrfTopLoc() + Util.null2str(params.get("report_FileDir")) + "/" ; 			// 리포트 파일 위치.
		String jrf_dir = customProperties.getReportJrfTopLoc(); 			// 리포트 파일 위치.
		
		log.info("reportFileDir========================"+Util.null2str(params.get("report_FileDir")));
		
		String entCode = Util.null2str(params.get("entCode"));
		String jrfFileName = Util.null2str(params.get("report_FileName")) + ".jrf";												// 리포트 파일명.
		//String ds	= "dataSource_yhtxx"; // 테스트 
		//String ds = "jdbc/exps_yhtxx"; //  운영
		String ds = customProperties.getReportDs() + entCode.toLowerCase();														// WAS에 설정된 DataSource 명. ubl/UbiService/ ubiservice.xml
		String file_url = customProperties.getReportUrl();																		// 리포트에서 사용되는 이미지 또는 공통 아이템 정보를 가져오기위한 정보.
		String ubi_serverUrl = customProperties.getReportDomain() 	+ "/UbiServer";												// 리포트에서 사용되는 이미지 또는 공통 아이템 정보를 가져오기위한 정보.
		String servlet_url1 = customProperties.getReportDomain() 	+ "/UbiForm";												//"/UbiForm";							// Form 서블릿 URL.
		String servlet_url2 = customProperties.getReportDomain() 	+ "/UbiData";												//"/UbiData";							// Data 서블릿 URL.
		String args = Util.null2str(params.get("ubi_arg"));					    												// 아규먼트 정보.
		String odJobNo = Util.null2str(params.get("odJobNo"));
		String poDate = Util.null2str(params.get("poDate"));
		String datePath = "/"+poDate.substring(0,4)+"/"+poDate.substring(5,7)+"/"+poDate.substring(8,10)+"/";
		String export_dir = customProperties.getLabelDir()+Util.null2str(params.get("deliveryCode")).toLowerCase()+datePath;
		String export_file = Util.null2str(params.get("odJobNo")) + "_label.pdf";
		String ext = "ecm";
		boolean isSuccess = false;
		
		log.info("FileUtil.isExists========================"+FileUtil.isExists(export_dir));
		if(!FileUtil.isExists(export_dir)){
			FileUtil.createDirectorys(export_dir);
		}
		
		try {
			UbiViewer ubiviewer = new UbiViewer(false, false);	// 객체 생성
			
			log.info("file_url========================"+file_url);
			log.info("ubiServerURL===================="+ubi_serverUrl);
			log.info("jrf_dir========================="+jrf_dir);
			log.info("jrfFileName====================="+jrfFileName);
			log.info("args============================"+args);
			log.info("ds=============================="+ds);
			log.info("export_dir======================"+export_dir);
			log.info("export_file====================="+export_file);
			
			ubiviewer.fileURL 		= file_url;
			ubiviewer.ubiServerURL 	= ubi_serverUrl;
			ubiviewer.isLocalFile	= true;
			ubiviewer.jrfFileDir 	= jrf_dir;
			ubiviewer.jrfFileName 	= jrfFileName;
			ubiviewer.arg 			= args;
			ubiviewer.dataSource 	= ds;
			ubiviewer.setExportParams("PDF", export_dir + export_file);
			ubiviewer.exectype 		= "TYPE6";
			ubiviewer.fontRevision 	= true;
			ubiviewer.setFontPath(customProperties.getReportFontLoc());		//WAS가 설치된 서버의 OS가 Non-Windows인 경우 폰트 정보를 가져오는 부분을 설정.
			ubiviewer.isDebug 		= true;
			log.info("["+odJobNo+"] loadReport Start");
			isSuccess = ubiviewer.loadReport(); // 파일 저장 수행.
			log.info("["+odJobNo+"] loadReport End / isSuccess: " + isSuccess);
		}catch(Exception e) {
			e.printStackTrace();
			isSuccess = false;
		}
		
		return isSuccess;
	 }

	/**
	 * 출력물 저장 
	 * @param order
	 */
	public void savePrintInfo(Order order) {
		
			LocalDate today = LocalDate.now();
	        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			
			// 송장 파일 생성 (UBI -> PDF)
			Map<String, Object> ubiParams = new HashMap<String, Object>();
			ubiParams.put("ubi_arg", "odJobNo#"+Util.null2str(order.getOdJobNo())+"#srchEntBusinessCode#"+Util.null2str(order.getEntBusinessCode())+"#srchEntCode#"+order.getEntCode());
			ubiParams.put("report_FileDir", "ecm");
			if("K".equals(order.getMailType())) {
				ubiParams.put("report_FileName", "ECM-EMSKP-001");
				ubiParams.put("deliveryCode", "EMS-KP");
			}else if("E".equals(order.getMailType())) {
				ubiParams.put("report_FileName", "ECM-EMSTS-001");
				ubiParams.put("deliveryCode", "EMS-TS");
			}
			ubiParams.put("odJobNo", Util.null2str(order.getOdJobNo()));
			ubiParams.put("entCode", Util.null2str(order.getEntCode()));
			ubiParams.put("poDate", formattedDate);
			
			getReportResultFile(ubiParams);
	}
	

	/**
	 * 상표권 침해 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public TrademarkInfringement trademark(SearchOrderList search) throws Exception {
		return orderMapper.getTrademark(search);
	}
	
	public OrderSaveResult updateKorexTracking(SearchOrderList search) throws Exception {
		
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		for(String odJobNo : search.getDataList()) {
			Map<String,Object>searchDeliveryMap = new HashMap<>();
			
			String deliveryCode = "";
//			searchDeliveryMap.put("srchEntCode",Util.null2str(pod.getEntCode()));
			searchDeliveryMap.put("odJobNo",odJobNo);
//			searchDeliveryMap.put("hblNo",Util.null2str(pod.getHblNo()));
//			searchDeliveryMap.put("deliveryCode",Util.null2str(pod.getDeliveryCode()));
			
			List<Map<String,Object>> multiList = pcccMapper.selectOrderMultiList(searchDeliveryMap);

			if(multiList != null && multiList.size() > 0) {
				for(Map<String,Object> multiMap : multiList) {
					
					deliveryCode = "".equals(Util.null2str(multiMap.get("multiChangeDeliveryCode"))) ? Util.null2str(multiMap.get("deliveryCode")) : Util.null2str(multiMap.get("multiChangeDeliveryCode")); 
					
					multiMap.put("entCode", search.getSrchEntCode());
					
					if("KOREX".equals(deliveryCode)) {
						long result = trackingOrderDeliveryParser.trackingProcessKorexPlus(multiMap,search.getSrchEntCode());
					}
				}
			}
		}
		
		
		return orderSaveResult;
	}

	public OrderDetList selectMultiOrderList(SearchOrderList search) throws Exception {
		OrderDetList orderDetList = new OrderDetList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<OrderDet> list  = orderMapper.selectMultiOrderList(search);
		orderDetList.setOrderDetList(list);
		
		if(list.size() > 0) {
			orderDetList.setTotal(list.get(0).getTotal());
		}else {
			orderDetList.setTotal(0);
		}
		return orderDetList;
	}
	
	public OrderMasterDupList selectOrderMasterDupList(SearchOrderList search) throws Exception {
		OrderMasterDupList orderMasterDupList = new OrderMasterDupList();
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
		//
		
		List<OrderMasterDup> list  = orderMapper.selectOrderMasterDupList(search);
		orderMasterDupList.setOrderMasterDupList(list);
		
		if(list.size() > 0) {
			orderMasterDupList.setTotal(list.get(0).getTotal());
		}else {
			orderMasterDupList.setTotal(0);
		}
		
		return orderMasterDupList;
	}
	
	public OrderHouseDupList selectOrderHouseDupList(SearchOrderList search) throws Exception {
		OrderHouseDupList orderHouseDupList = new OrderHouseDupList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<OrderHouseDup> list  = orderMapper.selectOrderHouseDupList(search);
		orderHouseDupList.setOrderHouseDupList(list);
		
		if(list.size() > 0) {
			orderHouseDupList.setTotal(list.get(0).getTotal());
		}else {
			orderHouseDupList.setTotal(0);
		}
		
		return orderHouseDupList;
	}

	/**
	 * Order List(Detail) - 일괄변경
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult batchSelectorUpdate(SearchOrderBatchUpdate search, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		String targetData = Util.null2str(search.getTargetData());
		Map<String,Object> batchUpdateMap = new HashMap<String,Object>();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Util.accessControlDatabase(search, "updateField|updateField2|gmtTime", false, null, null, null, null);
		
		if(targetData.equals("srchData")){
			SearchOrderList searchOrderList = search.getSearchOrderList();
			/* N개 검색(멀티타입) */
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(Util.null2str(searchOrderList.getSrchNo()))) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(searchOrderList.getSrchNo())).split(",")));
			}
			
			if(searchNoList.size() > 1){
				searchOrderList.setSearchType("multi");
				searchOrderList.setSearchNoList(searchNoList);
			}else{
				searchOrderList.setSearchType("");
				searchOrderList.setSrchNo(Util.null2str(searchOrderList.getSrchNo()).trim().replace(",", ""));
			}
			
			batchUpdateMap = BeanUtils.describe(searchOrderList);
			batchUpdateMap.put("selectorUserId", search.getSelectorUserId());
			batchUpdateMap.put("selectorUserName", search.getSelectorUserName());
			batchUpdateMap.put("confirmerUserId", search.getConfirmerUserId());
			batchUpdateMap.put("confirmerUserName", search.getConfirmerUserName());
			batchUpdateMap.put("xrayReader1Name", search.getXrayReader1Name());
			batchUpdateMap.put("xrayReader2Name", search.getXrayReader2Name());
			batchUpdateMap.put("gmtTime", search.getGmtTime());
			batchUpdateMap.put("updateUserId", search.getUpdateUserId());
			batchUpdateMap.put("srchCdTypeList", searchOrderList.getSrchCdTypeList());
			batchUpdateMap.put("srchClearanceStatusList", searchOrderList.getSrchClearanceStatusList());
			batchUpdateMap.put("srchLastPodStatusList", searchOrderList.getSrchLastPodStatusList());
			batchUpdateMap.put("srchCustomsIdVerifyList", searchOrderList.getSrchCustomsIdVerifyList());
			batchUpdateMap.put("srchCarryList", searchOrderList.getSrchCarryList());
			batchUpdateMap.put("srchHoldMap", search.getHoldMap());
			if(searchOrderList.getSearchType().equals("multi")){
				batchUpdateMap.put("searchNoList", searchOrderList.getSearchNoList());
			}
		}else {
			batchUpdateMap.put("selectorUserId", search.getSelectorUserId());
			batchUpdateMap.put("selectorUserName", search.getSelectorUserName());
			batchUpdateMap.put("confirmerUserId", search.getConfirmerUserId());
			batchUpdateMap.put("confirmerUserName", search.getConfirmerUserName());
			batchUpdateMap.put("xrayReader1Name", search.getXrayReader1Name());
			batchUpdateMap.put("xrayReader2Name", search.getXrayReader2Name());
			batchUpdateMap.put("checkDataTypeMh", search.getCheckDataTypeMh());
			if("Y".equals(search.getCheckMfSubGridYn())){
				batchUpdateMap.put("checkMfSubGridYn", "Y");
				batchUpdateMap.put("dataList", search.getCheckMfSubGridDataList());
			}else{
				batchUpdateMap.put("dataList", search.getCheckDataList());
			}
		}
		
		List<Order> logList = orderMapper.selectOrderListForLog(batchUpdateMap);
		
		if(targetData.equals("srchData")){
			orderMapper.updateOrderSelectorBatchBySrchData(batchUpdateMap);
		}else{
			orderMapper.updateOrderSelectorBatch(search);
		}
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", search.getEntCode());
		tmpLogMap.put("ent_business_code", search.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", search.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", search.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", search.getLogUserName());
		tmpLogMap.put("process_user_name_eng", search.getLogUserNameEng());
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		int seq = 1;
		String descriptionStr = "Order batch Selector update > ";
		if(!"".equals(Util.null2str(search.getSelectorUserId()))) {
			descriptionStr += "선택자 ID : " + search.getSelectorUserId();
		}
		if(!"".equals(Util.null2str(search.getSelectorUserName()))) {
			descriptionStr += " 선택자 이름 : " + search.getSelectorUserName();
		}
		if(!"".equals(Util.null2str(search.getConfirmerUserId()))) {
			descriptionStr += " 확인자 ID : " + search.getConfirmerUserId();
		}
		if(!"".equals(Util.null2str(search.getConfirmerUserName()))) {
			descriptionStr += " XRAY판독자1 이름 : " + search.getConfirmerUserName();
		}
		if(!"".equals(Util.null2str(search.getXrayReader1Name()))) {
			descriptionStr += " XRAY판독자2 이름 : " + search.getXrayReader1Name();
		}
		for(Order od : logList) {
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("bound", od.getBound());
			logMap.put("bound_type", od.getBoundType());
			logMap.put("job_no", od.getOdJobNo());
			if(targetData.equals("srchData")) {	//검색건
				search.setSearchOrderList(null);
				search.setContainerList(null);
				search.setPoInfoList(null);
				logMap.put("job_log", new Gson().toJson(search));
			}else {	//선택건
				logMap.put("job_log", "");
			}
			
			
			
			if(Util.null2str(od.getHblNo()).equals("")) {
				logMap.put("description",descriptionStr +" [MBL_NO : "+ od.getMblNo() + "]");
			}else if(!Util.null2str(od.getMblNo()).equals("")) {
				logMap.put("description",descriptionStr +" [MBL_NO : "+ od.getMblNo() + "]" +" [HBL_NO : "+ od.getHblNo() +"]");
			}else {
				logMap.put("description",descriptionStr +" [HBL_NO : "+ od.getHblNo() +"]");
			}
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}
		
		commonService.insertLogBulk(bulkList);
		
		return orderSaveResult;
	}

	/**
	 * 오더 EDI상태 초기화
	 * @param search
	 * @param request
	 * @return
	 */
	public OrderSaveResult orderResetUpdate(SearchOrderBatchUpdate search, HttpServletRequest request) {
		
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("entCode",search.getEntCode());
		map.put("hblNo", search.getUpdateValue());
		map = orderMapper.selectOdJobNoByHblNo(map);
		orderMapper.orderResetUpdate(map);
		return orderSaveResult;
	}
	
	/**
	 * Order List(KREAM 수출통관용 엑셀 오더정보)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderDetList selectOrderListCustomsExp(SearchOrderList search) throws Exception {
		OrderDetList orderDetList = new OrderDetList();
		
		/** 환율계산 */
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("srchDateFormat", "23");
		dataMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
		dataMap.put("srchDate", Util.getNow("yyyy-MM-dd"));
		dataMap.put("srchEntCode", search.getSrchEntCode());
		dataMap.put("entBusinessCode", search.getSrchEntBusinessCode());
		Util.accessControlDatabase(dataMap, "exrateType|srchDateFormat|gmtTime|dateFormat", false, null, null, null, null);
		Map<String, Object> exrateMap = commonMapper.selectExchangeRate(dataMap);
		search.setExchangeRateUsd(Util.null2dbl(exrateMap.get("EXCHANGE_RATE_USD")));
		search.setExchangeRateJpy(Util.null2dbl(exrateMap.get("EXCHANGE_RATE_JPY")));
		
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		if(search.getSort() != null && search.getSort().size() > 0 && Util.null2str(search.getSort().get(0).getField()).equals("REMARKS")) search.getSort().get(0).setField("A.REMARKS");
		
		Util.getCryptographyYn(search);
		//Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchCusType", true, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		List<OrderDet> list  = orderMapper.selectOrderListCustomsExp(search);
		orderDetList.setOrderDetList(list);
		
		if(list.size() > 0) {
			orderDetList.setTotal(list.get(0).getTotal());
		}else {
			orderDetList.setTotal(0);
		}
		return orderDetList;
	}
	
	public OrderDashboard selectOrderDashboard(SearchOrderList search) throws Exception {
		OrderDashboard orderDashboard = new OrderDashboard();
		Util.accessControlDatabase(search, "srchDateFormat", false, null, null, null, null);
		
		List<OrderDashboard> list = orderMapper.selectOrderDashboardList(search);
		List<OrderDashboard> diffList = orderMapper.selectOrderDashboardDiff(search);
		list.addAll(diffList);
		orderDashboard.setDashboardList(list);
		
		/* 환율조회 */
		List<Map<String, Object>> rateList = new ArrayList<Map<String, Object>>();
		String[] strList = {"EXRATE_SELLING_CUSTOMS", "EXRATE_BUYING_CUSTOMS"};
		for(String str:strList) {
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("srchDateFormat", "23");
			dataMap.put("exrateType", str);
			dataMap.put("srchDate", Util.getNow("yyyy-MM-dd"));
			dataMap.put("srchEntCode", search.getSrchEntCode());
			dataMap.put("entBusinessCode", search.getSrchEntBusinessCode());
			Util.accessControlDatabase(dataMap, "exrateType|srchDateFormat|gmtTime|dateFormat", false, null, null, null, null);
			
			Map<String, Object> exrateMap = orderMapper.selectExchangeRate(dataMap);
			rateList.add(exrateMap);
		}
		if(rateList != null && rateList.size() > 0) orderDashboard.setRateList(rateList);
		
		orderDashboard.setCsQanda(orderMapper.selectCSQanda(search));
		orderDashboard.setResendCount(orderMapper.selectResendCount(search));
		
		return orderDashboard;
	}
	
	/**
	 * 트래킹 POD 업데이트
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult trackingPodUpdate(SearchOrderPodUpdate search, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		String targetData = Util.null2str(search.getTargetData());
		Map batchUpdateMap = new HashMap<String,Object>();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Util.accessControlDatabase(search, "trackingCode|gmtTime", false, null, null, null, null);
		
		if(targetData.equals("srchData")){
			SearchOrderList searchOrderList = search.getSearchOrderList();
			/* N개 검색(멀티타입) */
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(Util.null2str(searchOrderList.getSrchNo()))) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(searchOrderList.getSrchNo())).split(",")));
			}
			
			if(searchNoList.size() > 1){
				searchOrderList.setSearchType("multi");
				searchOrderList.setSearchNoList(searchNoList);
			}else{
				searchOrderList.setSearchType("");
				searchOrderList.setSrchNo(Util.null2str(searchOrderList.getSrchNo()).trim().replace(",", ""));
			}
			
			batchUpdateMap = BeanUtils.describe(searchOrderList);
			batchUpdateMap.put("gmtTime", search.getGmtTime());
			batchUpdateMap.put("updateUserId", search.getUpdateUserId());
			batchUpdateMap.put("srchCdTypeList", searchOrderList.getSrchCdTypeList());
			batchUpdateMap.put("srchClearanceStatusList", searchOrderList.getSrchClearanceStatusList());
			batchUpdateMap.put("srchLastPodStatusList", searchOrderList.getSrchLastPodStatusList());
			batchUpdateMap.put("srchCustomsIdVerifyList", searchOrderList.getSrchCustomsIdVerifyList());
			batchUpdateMap.put("srchCarryList", searchOrderList.getSrchCarryList());
			batchUpdateMap.put("srchHoldMap", search.getHoldMap());
			if(searchOrderList.getSearchType().equals("multi")){
				batchUpdateMap.put("searchNoList", searchOrderList.getSearchNoList());
			}
		}else {
			batchUpdateMap.put("checkDataTypeMh", search.getCheckDataTypeMh());
			batchUpdateMap.put("dataList", search.getCheckDataList());
		}
		batchUpdateMap.put("updateType", search.getUpdateType());
		batchUpdateMap.put("boundType", search.getBoundType());
		
		List<Map<String,Object>> orderList = orderMapper.selectTrackingPodOrderList(batchUpdateMap);
		
		String podTable = "";
		String podCode = Util.null2str(search.getTrackingCode());
		
		if("ECM010".equals(podCode) || "ECM005".equals(podCode) || "ECM007".equals(podCode)
				|| "ECM030".equals(podCode) || "ECM050".equals(podCode) || "ECM060".equals(podCode) || "ECM070".equals(podCode)) {
			podTable = "TB_ECM_POD_01";
		}else if("ECM120".equals(podCode)) {
			podTable = "TB_ECM_POD_02";
		}else if("ECM210".equals(podCode) || "ECM290".equals(podCode) || "ECM240".equals(podCode)) {
			podTable = "TB_ECM_POD_03";
		}else if("ECM360".equals(podCode) || "ECM420".equals(podCode) || "ECM400".equals(podCode)) {
			podTable = "TB_ECM_POD_04";
		}
				
		SqlSession sqlSession = null;
		
		try {
			
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			int cnt = 1;

			for(Map<String,Object> odMap : orderList) {
				
				String odPodCode = podCode;
				// 오더의 통관타입에 따라 POD가 달라지는 경우
				// 통관완료 목록: ECM240 , 일반,배제,취하 ECM280
				// 통관접수 목록: ECM120, 일반,배제,취하 ECM250
				
				if(!"Y".equals(Util.null2str(odMap.get("CD_TYPE")))) {
					if("ECM240".equals(podCode)) {
						odPodCode = "ECM280";
					}
					if("ECM120".equals(podCode)) {
						odPodCode = "ECM250";
					}
				}
				
				
				odMap.put("podTable", podTable);
				odMap.put("podStatus", odPodCode);
				String result = String.format("%05d", cnt);
				odMap.put("POD_JOB_NO", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + result);
				odMap.put("updateUserId", search.getUpdateUserId());
				odMap.put("POD_DATE", search.getTrcDate());
				odMap.put("POD_TIME", search.getTrcTime());

				orderBatchMapper.updateOrderTrackingPod(odMap);
				
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
				
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
			
			BulkData bulkData = null;
			String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
			Map<String, Object> logMap = null;
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", search.getEntCode());
			tmpLogMap.put("ent_business_code", search.getUserBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", search.getUpdateUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "UPDATE");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", search.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", search.getLogUserName());
			tmpLogMap.put("process_user_name_eng", search.getLogUserNameEng());
			
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			int seq = 1;
			String descriptionStr = "Order Tracking Pod update > " + Util.null2str(search.getTrackingCode());
			for(Map<String,Object> odMap : orderList) {
				
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("bound", odMap.get("BOUND"));
				logMap.put("bound_type", odMap.get("BOUND_TYPE"));
				logMap.put("job_no", odMap.get("OD_JOB_NO"));
				if(targetData.equals("srchData")) {	//검색건
					search.setSearchOrderList(null);
					search.setContainerList(null);
					search.setPoInfoList(null);
					logMap.put("job_log", new Gson().toJson(search));
				}else {	//선택건
					logMap.put("job_log", "");
				}
				
				
				logMap.put("description",descriptionStr +" [HBL_NO : "+ odMap.get("HBL_NO") + "]");
				
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
			}
			
			commonService.insertLogBulk(bulkList);
			
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		
		return orderSaveResult;
	}

	/**
	 * 허브넷 인터페이스 테이블 데이터 저장
	 * @param paramMap
	 */
	public void updateHubnHoldList(Map<String,Object> paramMap) {
		
		List<Map<String, Object>> checkList = (List<Map<String, Object>>) paramMap.get("hubnHoldList");
		 for(Map<String, Object> checkMap : checkList) {
			 if("AIR".equals(checkMap.get("BOUND"))) {
				 checkMap.put("boundTable", "IF_TEDAI_STATUS");
			 }else {
				 checkMap.put("boundTable", "IF_TEDOI_STATUS");
			 }
             hubnOrderMapper.insertInterfaceInfo(checkMap);
         }
		 
	}

	/**
	 * 신규제출번호생성(이고반출)
	 * @param search
	 * @param request
	 * @return
	 */
    public OrderSaveResult saveHubnSimpleDocNo(SearchOrderBatchUpdate search, HttpServletRequest request) {
        OrderSaveResult orderSaveResult = new OrderSaveResult();
        Map<String, Object> tmpMap = new HashMap<>();
        Map<String, Object> podMap = new HashMap<String, Object>();
        Map<String, Object> simpleDocNoMap = new HashMap<String, Object>();
        Map<String, Object> updateSimpleDocNoMap = new HashMap<String, Object>();
        SqlSession sqlSession = null;
        int cnt = 1;

        simpleDocNoMap.put("entCode", search.getEntCode());
        simpleDocNoMap.put("gmtTime", search.getGmtTime());
        simpleDocNoMap.put("entBusinessCode", "");
        simpleDocNoMap.put("updateUserId", search.getUpdateUserId());
        simpleDocNoMap.put("solution", "ECM");
        simpleDocNoMap.put("stockType", "OD");
        simpleDocNoMap.put("stockSeqLength", "8");
        simpleDocNoMap.put("signType", "noHyphen");
        simpleDocNoMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
        simpleDocNoMap.put("stockRemark", "ORDER SIMPLE DOC NO");
        simpleDocNoMap.put("dateLength", "2");
        simpleDocNoMap.put("stockDateFormat", "12");

        /** 제출번호에 사용되는 특송부호 조회 */
        tmpMap.put("srchEntCode", search.getEntCode());
        tmpMap.put("srchEntBusinessCode", search.getUserBusinessCode());
        tmpMap.put("srchBound", "SEA"); //해상 고정
        tmpMap.put("srchEdiCityPort", search.getUpdateField());
        Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmpMap);

        if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
            updateSimpleDocNoMap.put("ediExpressSign", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN")));
            updateSimpleDocNoMap.put("ediExpressSignDocumentN", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N")));
        }

        //신규 제출번호 생성
        simpleDocNoMap.put("stockSign", updateSimpleDocNoMap.get("ediExpressSignDocumentN"));
        String updateSimpleDocNo = commonMapper.selectNextStockMaxNo(simpleDocNoMap);

        tmpMap.clear();

        try{
            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
            HubnOrderMapper hubnOrderBatchMapper = sqlSession.getMapper(HubnOrderMapper.class);

            hubnOrderMapper.deleteResetSendCustomsManifest(search);
            for(String odJobNo : search.getCheckDataList()){
                Map<String,Object> resetMap = new HashMap<>();
                resetMap.put("OD_JOB_NO", odJobNo);
                tmpMap = commonMapper.selectLastPodInfo(resetMap);
                if(tmpMap != null && tmpMap.size() > 0){
                    podMap.put("lastPodStatus", tmpMap.get("POD_STATUS"));
                    podMap.put("lastPodDate", tmpMap.get("POD_DATE_TIME"));
                }
                tmpMap.putAll(podMap);
                tmpMap.put("odJobNo", odJobNo);
                tmpMap.put("simpleDocNo", updateSimpleDocNo);

                hubnOrderBatchMapper.updateResetSendCustomsManifest(tmpMap);
                if( (cnt++ % 300) == 0){
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }
            sqlSession.flushStatements();
            sqlSession.clearCache();
            sqlSession.commit();

        }catch(Exception e) {
            throw e;
        }finally {
            sqlSession.close();
        }


        return orderSaveResult;
    }
}