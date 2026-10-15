package org.yht.exps.express.service.edi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.com.ftp.FTPConnector;
import org.yht.exps.express.com.ftp.SFTPConnector;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.ConsolMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.edi.AbnormalMapper;
import org.yht.exps.express.mapper.edi.CwhpManifestMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.common.AddressInfo;
import org.yht.exps.express.model.common.search.SearchAddressInfo;
import org.yht.exps.express.model.eCommerce.consol.ConsolOrder;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderContainer;
import org.yht.exps.express.model.eCommerce.order.OrderPodUpdate;
import org.yht.exps.express.model.eCommerce.order.OrderPodUpdate.Pod;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderBatchUpdate;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.edi.CarryinInHouse;
import org.yht.exps.express.model.edi.CarryinInList;
import org.yht.exps.express.model.edi.CarryoutInHouse;
import org.yht.exps.express.model.edi.CarryoutInList;
import org.yht.exps.express.model.edi.CustomsManifestRes;
import org.yht.exps.express.model.edi.CustomsManifestResList;
import org.yht.exps.express.model.edi.ManifestAggregate;
import org.yht.exps.express.model.edi.ManifestAggregateList;
import org.yht.exps.express.model.edi.ManifestCorrect;
import org.yht.exps.express.model.edi.ManifestCorrectDelete;
import org.yht.exps.express.model.edi.ManifestCorrectList;
import org.yht.exps.express.model.edi.ManifestCorrectMaster;
import org.yht.exps.express.model.edi.ManifestCorrectRecvResultList;
import org.yht.exps.express.model.edi.ManifestCorrectSaveResult;
import org.yht.exps.express.model.edi.ManifestInHouse;
import org.yht.exps.express.model.edi.ManifestInList;
import org.yht.exps.express.model.edi.ManifestInMaster;
import org.yht.exps.express.model.edi.ManifestOrder;
import org.yht.exps.express.model.edi.ManifestOrderSaveResult;
import org.yht.exps.express.model.edi.ManifestOutHouse;
import org.yht.exps.express.model.edi.ManifestOutList;
import org.yht.exps.express.model.edi.ManifestOutMaster;
import org.yht.exps.express.model.edi.ManifestSeparationResult;
import org.yht.exps.express.model.edi.SendDelivery;
import org.yht.exps.express.model.edi.SendManifest;
import org.yht.exps.express.model.edi.abnormal.Abnormal;
import org.yht.exps.express.model.edi.search.SearchCarryoutInList;
import org.yht.exps.express.model.edi.search.SearchManifest;
import org.yht.exps.express.model.edi.search.SearchManifestCorrectList;
import org.yht.exps.express.model.edi.search.SearchManifestCustoms;
import org.yht.exps.express.model.edi.search.SearchManifestInList;
import org.yht.exps.express.model.edi.search.SearchManifestOutList;
import org.yht.exps.express.model.edi.search.SearchManifestSimpleDocNo;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.User;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.OrderCommonService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Service
@Transactional
public class ManifestService {
		
	@Autowired
	private CustomProperties customProperties;
	@Autowired
	ManifestMapper manifestMapper;
	
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	@Resource(name = "orderCommonService")
	OrderCommonService orderCommonService;
	@Resource(name="ediManifestAirExpSendService")
	EdiManifestAirExpSendService ediManifestAirExpSendService;
	@Resource(name="ediManifestAirImpSendService")
	EdiManifestAirImpSendService ediManifestAirImpSendService;
	@Resource(name="ediManifestSeaExpSendService")
	EdiManifestSeaExpSendService ediManifestSeaExpSendService;
	@Resource(name="ediManifestSeaImpSendService")
	EdiManifestSeaImpSendService ediManifestSeaImpSendService;
	@Resource(name="cargoProgressParser")
	CargoProgressParser cargoProgressParser;
	
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	ConsolMapper consolMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	AbnormalMapper abnormalMapper;
    @Autowired
    CwhpManifestMapper cwhpManifestMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	StringRedisTemplate redisTemplate;
	
	@Autowired
    CommonService commonService;
	@Autowired
    EnterpriseMapper enterpriseMapper;
	@Autowired
	SmsUserService smsUserService;
	
	@Resource(name = "fileService")
	FileService fileService;
	
	@Value("${spring.profiles.active}")
	private String profile;
	
	/**
	 * ECM 적하목록 마스터 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestInList selectMasterInList(SearchManifestInList search) throws Exception {
		ManifestInList manifestInList = new ManifestInList();
		
		if(("").equals(search.getSrchStartDate()) || ("").equals(search.getSrchEndDate())){
			throw new Exception("매개변수 호출에 실패하였습니다.");
		}
		
		if(search.getSrchResultYn() != null) {
			if(search.getSrchResultYn().equals("N")) {
				List<ManifestInMaster> list  = new ArrayList<ManifestInMaster>();
				manifestInList.setManifestInMasterList(list);
				manifestInList.setTotal(0);
				return manifestInList;
			}
		}
		
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
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
		
		
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE|ad.MF_DATE|noDateChk", "srchNoType", "|od.MASTER_OD_JOB_NO|od.MBL_NO|od.HBL_NO|od.MRN|ad.SIMPLE_DOC_NO|od.VESSEL_FLIGHT|pack.PACKING_NO|");
		if(!"Y".equals(search.getSubGridYn())){
			/** 제출번호로 조회시 해당 제출번호의 masterOdJobNo조회 */
			if("ad.SIMPLE_DOC_NO".equals(search.getSrchNoType()) && (searchNoList.size() > 1 || !"".equals(Util.null2str(search.getSrchNo())))){
				List<String> masterOdJobNoList = manifestMapper.selectMasterOdJobNoList(search);
				if(masterOdJobNoList.size() > 1){
					search.setSearchType("multi");
					search.setSearchNoList(masterOdJobNoList);
				}else if(masterOdJobNoList.size() == 1 && masterOdJobNoList.get(0) != null){
					search.setSearchType("");
					search.setSrchNo(masterOdJobNoList.get(0));
				}else{
					search.setSearchType("");
					search.setSrchNo("null");
				}
				search.setSrchNoType("od.MASTER_OD_JOB_NO");
			}
		}
		List<ManifestInMaster> list  = manifestMapper.selectMasterInList(search);
		manifestInList.setManifestInMasterList(list);
		if(list != null && list.size() > 0) {
			manifestInList.setTotal(list.get(0).getTotal());
		}else {
			manifestInList.setTotal(0);
		}
		
		return manifestInList;
	}
	
	/**
	 * ECM 적하목록 하우스 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestInList selectHouseInList(SearchManifestInList search) throws Exception {
		ManifestInList manifestInList = new ManifestInList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		if("ECM-EIMF-031F".equals(search.getSrchMenuId())){
			search.setSrchNoType(Util.null2str(search.getSrchHNoType()));
		}
		
		/* N개 검색(멀티타입) */
		String srchNo = null;
		srchNo = Util.null2str(search.getSrchHNo());
		
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(srchNo)) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(srchNo).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			srchNo = srchNo.trim().replace(",", "");
			search.setSearchType("");
			if("ECM-EIMF-031F".equals(search.getSrchMenuId())){
				search.setSrchNo(srchNo);
			}else{
				search.setSrchHNo(srchNo);
			}
		}
		
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchNoType", true, "srchDateGbn", "|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE|ad.MF_DATE|noDateChk", "srchHNoType", "od.OD_JOB_NO|od.HBL_NO|od.HSN|od.MBL_NO|od.MRN|ad.SIMPLE_DOC_NO|pack.PACKING_NO|od.ASSIGNMENT_WH_CODE");
		List<ManifestInHouse> list  = manifestMapper.selectHouseInList(search);
		manifestInList.setManifestInHouseList(list);
		if(list != null && list.size() > 0) {
			manifestInList.setTotal(list.get(0).getTotal());
		}else {
			manifestInList.setTotal(0);
		}
		
		return manifestInList;
	}
	
	/**
	 * ECM 지정창고 반출요청 - 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public CarryoutInList selectCarryoutInList(SearchOrderList search) throws Exception {
		
		CarryoutInList manifestInList = new CarryoutInList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		
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
		Util.accessControlDatabase(search, "srchDateFormat|srchCusType", true, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE|ad.MF_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO|ad.SIMPLE_DOC_NO|od.MASTER_OD_JOB_NO");
		List<CarryoutInHouse> list  = manifestMapper.selectCarryoutInList(search);
		
		manifestInList.setManifestInHouseList(list);
		if(list != null && list.size() > 0) {
			manifestInList.setTotal(list.get(0).getTotal());
		}else {
			manifestInList.setTotal(0);
		}
		
		return manifestInList;
		
	}
	
	/**
	 * ECM 일일반입예상 - 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public CarryinInList selectCarryinInList(SearchCarryoutInList search) throws Exception {
		
		CarryinInList manifestInList = new CarryinInList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		
		Util.accessControlDatabase(search, "srchDateFormat|srchCusType", true, "srchDateGbn", "od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE|ad.MF_DATE", "srchNoType", "od.MBL_NO|od.HBL_NO|od.MRN|ad.SIMPLE_DOC_NO|od.MASTER_OD_JOB_NO");
		List<CarryinInHouse> list  = manifestMapper.selectCarryinInList(search);
		
		manifestInList.setManifestInHouseList(list);
		if(list != null && list.size() > 0) {
			manifestInList.setTotal(list.get(0).getTotal());
		}else {
			manifestInList.setTotal(0);
		}
		
		return manifestInList;
		
	}
	
	/**
	 * ECM 적하목록정보 - 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestInList selectManifestInList(SearchManifestInList search) throws Exception {
		ManifestInList manifestInList = new ManifestInList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchMasterOdJobNoListStr()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchMasterOdJobNoListStr())).split(",")));
		}
		if(searchNoList.size() > 0) {
			search.setSrchMasterOdJobNoList(searchNoList);
		}
		
		List<Map<String, Object>> srchOdJobNoDocNo  = new ArrayList<>();
		if(!"".equals(Util.null2str(search.getSrchOdJobNoAndDocNoList()))) {
			for(String item : search.getSrchOdJobNoAndDocNoList()) {
				Map<String, Object> srchMap = new HashMap<>();
				String[] srchNo = item.split("\\|", -1);
				srchMap.put("srchOdJobNo", srchNo[0]);
				srchMap.put("srchSimpleDocNo", srchNo[1]);
				srchOdJobNoDocNo.add(srchMap);
			}
		}
		if(srchOdJobNoDocNo.size() > 0) {
			search.setSrchOdJobNoAndDocNoListSql(srchOdJobNoDocNo);
		}
//		Util.accessControlDatabase(search, "srchMasterOdJobNoListStr", false, null, null, null, null);
		List<ManifestInHouse> list  = manifestMapper.selectManifestInList(search);
		List<Map<String, Object>> chkList = manifestMapper.selectChkConsigneeList(search);	/** 통관목록 자체선별 기초 리스트 */
		List<Map<String, Object>> blackList = manifestMapper.selectBlacklistList(search);	/** 블랙리스트 리스트 */
		manifestInList.setManifestInHouseList(list);
		if(list != null && list.size() > 0) {
			manifestInList.setTotal(list.get(0).getTotal());
			/** 
			 * 오류검증
			 * 1. 중량이 20kg를 초과하면 [무게] 로 표시
			 * 2. 통관금액이 0이면 [화폐] 로 표시
			 * 3. 통관금액이 1이하면 [금액] 으로 표시
			 * 4. 수하인 이름에 MR, MISS, MRS, DR 이 포함되어 있으면 [상호] 로 표시
			 * 5. 수하인 전화번호 길이가 7자리 미만이거나 연속된 숫자가 6자리이면 [전번] 표시
			 * 6. 수하인 주소에 숫자가 없거나 20자리 미만이면 [주소] 로 표시
			 * 7. TB_ECM_OD 테이블의 REMARKS 컬럼 앞 3자리가 819이거나 앞 3자리가 숫자가 아닌 경우 [더미] 로 표시
			 * 8. 통관목록 선별 기초에 수하인 이름 또는 수하인 전화번호가 있는 경우 [배제] 로 표시
			 * 9. 거래구분이 A인 경우 허용품목이 오류품목기초코드에 있거나 허용품목코드 자릿수가 6자리 미만인 경우 [세번] 으로 표시
			 * 10. 블랙리스트에 등록된 송하인상호, 송하인 국가코드, 수하인상호, 품명을 포함할 경우 [블랙리스트] 로 표시
			 * 11. 오류품목기초코드에 있으면 [HSCODE] 로 표시
			 */
			Pattern patternAddress = Pattern.compile(".*\\d.*"); // 숫자
			Matcher matcher = null;
			Class<?> selectionClass = Class.forName("org.yht.exps.express.model.edi.ManifestInHouse");
	        Method method = null;
			boolean changeFlag = false;
			boolean blackFlag = false;
			String operator, checkField = "";
			for(ManifestInHouse house : list) {
				// 오류검증
				String errorCheck = "";
				boolean obFlag = false, jkFlag = false;
				if(Util.null2dbl(house.getGwt()) > 20) {
					errorCheck += ",무게";
				}
				if(Util.null2dbl(house.getAmountCur()) == 0) {
					errorCheck += ",화폐";
				}
				if(Util.null2dbl(house.getAmountCur()) <= 1) {
					errorCheck += ",금액";
				}
				String consigneeName = Util.null2str(house.getConsigneeName());
				/** 2025-05-19 상호 조건추가    7자리이하 / 숫자2자리이상 */
				if(consigneeName.length() <= 7 || consigneeName.replaceAll("[^0-9]", "").length() >= 2 || consigneeName.indexOf("MR") > -1 || consigneeName.indexOf("MISS") > -1 || consigneeName.indexOf("MRS") > -1 || consigneeName.indexOf("DR") > -1) {
					errorCheck += ",상호";
				}
				/** 2025-05-19 전번 = 같은번호6자리 / 연속번호6자리 일경우     789012|890123|901234 는 허용?   */
				String consigneeTel = Util.null2str(house.getConsigneeTel());
				if(consigneeTel.length() < 7 || Pattern.matches(".*(1{6}|2{6}|3{6}|4{6}|5{6}|6{6}|7{6}|8{6}|9{6}|0{6}).*", consigneeTel) || Pattern.matches(".*(012345|123456|234567|345678|456789|567890|678901).*", consigneeTel)) {
					errorCheck += ",전번";
				}
//				else {
//					/** 전화번호 연속된 숫자 6개 체크 - 111111(X), 123456(O) */
//					int pre = 0, now = 0, cnt = 1;
//					for(int i=0; i<consigneeTel.length(); i++){
//						try { 
//							now = Integer.parseInt(consigneeTel.substring(i, i+1));
//						}catch(NumberFormatException e) {
//							cnt = 1;
//							continue;
//						}
//						if(i > 0){
//							if(pre + 1 == now){
//								cnt++;
//							}else{
//								cnt = 1;
//							}
//						}
//						pre = now;
//						if(cnt == 6){ /** 6자리 연속 */
//							errorCheck += ",전번";
//							break;
//						}
//					}
//				}
				String consigneeAddress = Util.null2str(house.getConsigneeAddress());
				matcher = patternAddress.matcher(consigneeAddress);
				if(!matcher.find() || consigneeAddress.length() < 20) {
					errorCheck += ",주소";
				}
				String remarks = Util.null2str(house.getRemarks());
				if(!"".equals(remarks)){
					/** 2025-05-07  819로 시작하고 6자리가 모두 숫자인 경우로 수정 */
//					if((remarks.length() > 5 && Util.isNumber(remarks.substring(3, 6))) || (remarks.length() > 2 && "819".equals(remarks.substring(0, 3)))) {
					if(remarks.length() > 5 && Util.isNumber(remarks.substring(0, 6)) && "819".equals(remarks.substring(0, 3))){
						errorCheck += ",더미";
					}
				}
				
				if(blackList != null && blackList.size() > 0) {
					for(Map<String, Object> chk : blackList) {
						checkField = Util.null2str(chk.get("ETC2"));
						method = selectionClass.getMethod("get" + checkField);
						if("ShipperCountryCode".equals(checkField)) {
							if(Util.null2str(method.invoke(house)).toUpperCase().equals(Util.null2str(chk.get("ETC1")).toUpperCase())){
								blackFlag = true;
								break;
							}
						}else {
							if(Util.null2str(method.invoke(house)).toUpperCase().indexOf(Util.null2str(chk.get("ETC1")).toUpperCase()) > -1){
								blackFlag = true;
								break;
							}
						}
					}
				}
				
				if(blackFlag) {
					errorCheck += ",블랙리스트";
				}
				
				if(chkList != null && chkList.size() > 0) {
					for(Map<String, Object> chk : chkList) {
						if(Util.null2str(house.getDocumentYn()).equals(chk.get("DOCUMENT_YN"))){
							changeFlag = false;
							operator = Util.null2str(chk.get("OPERATOR"));
							checkField = Util.null2str(chk.get("CHECK_FIELD"));
							method = selectionClass.getMethod("get" + checkField);
							if(operator.equals("0")){
								if(Util.null2str(method.invoke(house)).toUpperCase().equals(Util.null2str(chk.get("KEYWORD")).toUpperCase())){
									changeFlag = true;
								}
							}else if(operator.equals("1")){
								if(Util.null2str(method.invoke(house)).toUpperCase().indexOf(Util.null2str(chk.get("KEYWORD")).toUpperCase()) > -1){
									changeFlag = true;
								}
							}else if(operator.equals("2")){
								if(Util.null2dbl(method.invoke(house)) >= Util.null2dbl(chk.get("KEYWORD"))){
									changeFlag = true;
								}
							}else if(operator.equals("3")){
								if(Util.null2dbl(method.invoke(house)) <= Util.null2dbl(chk.get("KEYWORD"))){
									changeFlag = true;
								}
							}
							if(changeFlag){
								/** OB:배제 / JK:HSCODE 오류 */
								if(Util.null2str(chk.get("SEARCH_TYPE")).equals("OB")){
									obFlag = true;
								}else if(Util.null2str(chk.get("SEARCH_TYPE")).equals("JK")){
									jkFlag = true;
								}
							}
						}
					}
				}
				if(obFlag) {
					errorCheck += ",배제";
				}
				
				if(jkFlag) {
					errorCheck += ",HSCODE";
				}
				
				if(house.getEcmType().equals("A") && (jkFlag || Util.null2str(house.getAllowedItemCode()).length() < 6)) {
					errorCheck += ",세번";
					
				}
				if(!"".equals(errorCheck)) {
					errorCheck = errorCheck.substring(1);
					house.setErrorCheck(errorCheck);
				}
				
				// 수하인과 다른 경우
				if(!"".equals(Util.null2str(house.getConsigneeName())) && !"".equals(Util.null2str(house.getActualCstName()))) {
					if(!house.getConsigneeName().equals(house.getActualCstName())) {
						house.setDiffYn("Y");
					}else {
						house.setDiffYn("N");
					}
				}
			}
		}else {
			manifestInList.setTotal(0);
		}
		
		return manifestInList;
	}
	
	/**
	 * 엑셀업로드체크
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult upsExcelUploadCheck(MultipartFile uploadExcel, String entCode) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
			orderSaveResult.setResultCode("extError");
			return orderSaveResult;
		}
		
		FileUploadResponse result = fileService.storeFile(uploadExcel, entCode, true);
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
			
			fieldName =		new String[]{"planYn", 	"errorCheck",	"hblNo",			"commodityItemTemp",	"commodityItem",		"consigneeName",	"consigneeTel",	"shipperCountryCode",	"amountCurTemp",	"gwt",	"shipperName",	"consigneeAddress",	"consigneeAddress",	"arDate",	"vesselFlight",	"allowedItemCode",	"etc1",	"url",		"etc2",	"etc3",	"amountCur",	"etc4",	"mblNo",	"customsId",	"cdUseType"};
			fieldTitle =	new String[]{"PlanY/N", "오류검증", 		"ShipmentNumber", 	"품명",					"수정품명",				"수하인상호",			"수하인전화번호",		"발송국가",				"금액",				"무게",	"송하인이름",		"수하인주소",			"수정주소",			"입항일자",	"편명",			"품명코드",			"Etc1",	"전자상거래주소",	"Etc2",	"Etc3",	"총금액",			"Etc4",	"MBLNo",	"개인통관고유부호",	"용도구분(개인,회사)"};
			fieldLength =	new String[]{"1",		"pass", 		"20", 				"pass", 				"300",					"100", 				"50", 			"2",		 			"17.4", 			"12.3",	"100", 			"300", 				"300", 				"10", 		"50",			"10", 				"80", 	"300", 		"80", 	"80",	"17.4", 		"80", 	"20", 		"50", 			"2"};
			fieldType =		new String[]{"X",		"S",			"S",				"S",					"SN",					"SN",				"S",			"X",					"DBL",				"X",	"X",			"X",				"SN",				"X",		"X",			"S",				"X",	"S",		"X",	"X",	"DBL",			"X",	"S",		"S",			"SN"};
			
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				Sheet sheet = wb.getSheetAt(0);
				DataFormatter df = new DataFormatter();
				int fieldSize = fieldName.length;
				StringBuffer errMsg = new StringBuffer();
				Map<String, Object> dataMap = null;
				String fieldValue = "";
				
				// 데이트 포맷
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				if(extension.equals("xlsx")){
					XSSFCell myCell = null;
					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
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
							if("DATE".equals(fieldType[j])) {
								String value = null;
								try {
									if(myCell!=null && myCell.getDateCellValue() !=null) {
										value = sdf.format(myCell.getDateCellValue());
									}
								}catch(Exception e) {
									value = df.formatCellValue(myCell);
								}
								fieldValue = this.dataChk(value, fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							else if("X".equals(fieldType[j])) { // fieldType이 없는 항목은 셋팅하지 않음
								continue;
							}
							else {
								if(myCell.getCellType() == CellType.FORMULA) {
									fieldValue = this.dataChk(myCell.getStringCellValue(), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i, false);
								}else {
									fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i, false);
								}
							}
							
							//필수값 체크
							if(j == 2) {
								if(StringUtils.isEmpty(fieldValue) || Util.null2str(fieldValue).trim().equals("")) {
									errMsg.append("[ShipmentNumber] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							dataMap.put(fieldName[j], fieldValue);
						}
						
						//하우스정보 셋팅
						dataList.add(dataMap);
					}
				}else{
					HSSFCell myCell = null;
					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
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
							if("DATE".equals(fieldType[j])) {
								String value = null;
								try {
									if(myCell!=null && myCell.getDateCellValue() !=null) {
										value = sdf.format(myCell.getDateCellValue());
									}
								}catch(Exception e) {
									value = df.formatCellValue(myCell);
								}
								fieldValue = this.dataChk(value, fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							else if("X".equals(fieldType[j])) { // fieldType이 없는 항목은 셋팅하지 않음 (업데이트 시 사용하지 않는 항목)
								continue;
							}
							else {
								if(myCell.getCellType() == CellType.FORMULA) {
									fieldValue = this.dataChk(myCell.getStringCellValue(), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i, false);
								}else {
									fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i, false);
								}
							}
							
							//필수값 체크
							if(j == 2) {
								if(StringUtils.isEmpty(fieldValue) || Util.null2str(fieldValue).trim().equals("")) {
									errMsg.append("[ShipmentNumber] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
		
							dataMap.put(fieldName[j], fieldValue);
						}
						
						//하우스정보 셋팅
						dataList.add(dataMap);				
						
					}
				}
				
				if(dataList.size() ==0){
					orderSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
					return orderSaveResult;
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
				
				resultData.put("dataList", dataList);
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
	 * tracking 엑셀업로드체크
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult upsExcelUploadCheckTracking(MultipartFile uploadExcel, String entCode) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
			orderSaveResult.setResultCode("extError");
			return orderSaveResult;
		}
		
		FileUploadResponse result = fileService.storeFile(uploadExcel, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")){
			File file = fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile();
			String extension = null;
			if(file.getName().toLowerCase().indexOf(".xlsx") > -1){
				extension = "xlsx";
			}else{
				extension = "xls";
			}
			
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> tmpDataList = new ArrayList<Map<String, Object>>();
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				Sheet sheet = wb.getSheetAt(0);
				DataFormatter df = new DataFormatter();
				StringBuffer errMsg = new StringBuffer();
				Map<String, Object> dataMap = null;
				String fieldValue = "";
				
				if(extension.equals("xlsx")){
					XSSFCell myCell = null;
					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
						if(sheet.getRow(i) == null){
							orderSaveResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							orderSaveResult.setResultData(resultData);
							return orderSaveResult;
						}
											
						dataMap = new HashMap<String, Object>();
						myCell = (XSSFCell)sheet.getRow(i).getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK);
						fieldValue = this.dataChk(df.formatCellValue(myCell), "MAWB No", "30", "S", errMsg, i);
						if(Util.isEmpty(fieldValue)){
							continue;
						}
						dataMap.put("mblNo", fieldValue);
						
						myCell = (XSSFCell)sheet.getRow(i).getCell(1, MissingCellPolicy.CREATE_NULL_AS_BLANK);
						fieldValue = this.dataChk(df.formatCellValue(myCell), "Shipment", "30", "S", errMsg, i);
						if(Util.isEmpty(fieldValue)){
							continue;
						}
						dataMap.put("hblNo", fieldValue);
						
						myCell = (XSSFCell)sheet.getRow(i).getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK);
						fieldValue = this.dataChk(df.formatCellValue(myCell), "Tracking", "30", "S", errMsg, i);
						if(Util.isEmpty(fieldValue)){
							continue;	
						}
						dataMap.put("packingNo", fieldValue);
						
						//하우스정보 셋팅
						dataList.add(dataMap);
					}
				}else{
					HSSFCell myCell = null;
					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
						if(sheet.getRow(i) == null){
							orderSaveResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							orderSaveResult.setResultData(resultData);
							return orderSaveResult;
						}
						
						dataMap = new HashMap<String, Object>();
						myCell = (HSSFCell)sheet.getRow(i).getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK);
						fieldValue = this.dataChk(df.formatCellValue(myCell), "MAWB No", "30", "S", errMsg, i);
						if(Util.isEmpty(fieldValue)){
							continue;
						}
						dataMap.put("mblNo", fieldValue);
						
						dataMap = new HashMap<String, Object>();
						myCell = (HSSFCell)sheet.getRow(i).getCell(1, MissingCellPolicy.CREATE_NULL_AS_BLANK);
						fieldValue = this.dataChk(df.formatCellValue(myCell), "Shipment", "30", "S", errMsg, i);
						if(Util.isEmpty(fieldValue)){
							continue;
						}
						dataMap.put("hblNo", fieldValue);
						
						myCell = (HSSFCell)sheet.getRow(i).getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK);
						fieldValue = this.dataChk(df.formatCellValue(myCell), "Tracking", "30", "S", errMsg, i);
						if(Util.isEmpty(fieldValue)){
							continue;
						}
						dataMap.put("packingNo", fieldValue);
						
						//하우스정보 셋팅
						dataList.add(dataMap);				
						
					}
				}
				
				if(dataList.size() ==0){
					orderSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
					return orderSaveResult;
				}else {
					Map<String, List<String>> tmpDataMap = new HashMap<String, List<String>>();
					for(Map<String, Object> data : dataList) {
						String keyStr = Util.null2str(data.get("mblNo")) + "|" + Util.null2str(data.get("hblNo"));
						if(Util.isEmpty((tmpDataMap.get(keyStr)))){
							tmpDataMap.put(keyStr, new ArrayList<String>());
						}
						tmpDataMap.get(keyStr).add(Util.null2str(data.get("packingNo")));
					}
					for(String key : tmpDataMap.keySet()) {
						String[] keyArr = key.split("\\|");
						Map<String, Object> tmpMap = new HashMap<String, Object>();
						tmpMap.put("mblNo", keyArr[0]);
						tmpMap.put("hblNo", keyArr[1]);
						tmpMap.put("packingNoList", tmpDataMap.get(key));
						tmpDataList.add(tmpMap);
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
				
				resultData.put("dataList", tmpDataList);
				resultData.put("dataSize", tmpDataList.size());
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
	 * 적하목록정보 업데이트 ExcelUpload
	 * @param excelUploadList
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult upsExcelUpload(ExcelUploadList excelUploadList) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		List<Map<String, Object>> dataList = excelUploadList.getDataList();
		List<Map<String, Object>> dataListAd = new ArrayList<Map<String, Object>>();
		if(dataList == null) {
			return orderSaveResult;
		}
		
		Map<String, Object> tmpMap;
		List<Map<String, Object>> updateCdTypeList = new ArrayList<Map<String, Object>>();
		Map<String, Object> updateCdTypeMap;
		
		for(Map<String, Object> map : dataList) {
			tmpMap = manifestMapper.selectExcelUploadTrackingChk(map);
			if(tmpMap != null){
				map.put("odJobNo", tmpMap.get("OD_JOB_NO"));
				map.put("bound", tmpMap.get("BOUND"));
				if("1".equals(map.get("errorCheck"))){
					map.put("cdType", "B");
					if(!"B".equals(tmpMap.get("CD_TYPE"))){
						tmpMap.put("odJobNo", map.get("odJobNo"));
						tmpMap.put("cdType", "B");
						updateCdTypeList.add(tmpMap);
					}
				}else if("2".equals(map.get("errorCheck"))){
					map.put("cdType", "Y");
					if(!"Y".equals(tmpMap.get("CD_TYPE"))){
						updateCdTypeMap = new HashMap<String, Object>();
						updateCdTypeMap.put("odJobNo", map.get("odJobNo"));
						updateCdTypeMap.put("cdType", "Y");
						updateCdTypeList.add(updateCdTypeMap);
					}
				}
				
				if("개인".equals(map.get("cdUseType"))){
					map.put("cdUseType", "1");
				}else if("회사".equals(map.get("cdUseType"))){
					map.put("cdUseType", "2");
				}
				
				if("1".equals(map.get("cdUseType")) && "2".equals(tmpMap.get("CD_USE_TYPE"))){
					if(Util.isEmpty(map.get("url"))){
						map.put("ecmType", "D");
					}else{
						map.put("ecmType", "A");
					}
					if("".equals(Util.null2str(tmpMap.get("SIMPLE_EDI_STATUS"))) || "ERROR".equals(tmpMap.get("SIMPLE_EDI_STATUS"))){
						dataListAd.add(map);
					}
				}else if("2".equals(map.get("cdUseType")) && "1".equals(tmpMap.get("CD_USE_TYPE"))){
					if(Util.null2dbl(map.get("amountCurTemp")) == 1 && "DOCUMENTS".equals(map.get("commodityItemTemp"))){
						map.put("ecmType", "F");
					}else{
						map.put("ecmType", "E");
					}
				}
			}
		}
		if(updateCdTypeList.size() > 0){
			commonService.insertBmsCargoProgress(updateCdTypeList);
			
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("updateCdTypeList", updateCdTypeList);
			orderSaveResult.setResultCode("updateBmsCdType");
			orderSaveResult.setResultData(resultData);
		}
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", excelUploadList.getEntCode());
		tmpLogMap.put("ent_business_code", excelUploadList.getUserEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("bound_type", "IMP");
		tmpLogMap.put("process_user_id", excelUploadList.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "EXCEL");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", excelUploadList.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", excelUploadList.getLogUserName());
		tmpLogMap.put("process_user_name_eng", excelUploadList.getLogUserNameEng());
		
		int cnt = 1;
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
			Util.accessControlDatabase(excelUploadList, "gmtTime", false, null, null, null, null);
			
			for(Map<String, Object> map : dataList){
				if(!Util.isEmpty(map.get("odJobNo"))){
					map.put("updateUserId", excelUploadList.getUpdateUserId());
					map.put("gmtTime", excelUploadList.getGmtTime());
					if(!"".equals(Util.null2str(map.get("customsId")))) {
						String customsId = Util.null2str(map.get("customsId"));
						// 개인통관부호 P로 시작할 경우 통관ID구분 값을 A(개인통관부호)로 업데이트
						if(customsId.startsWith("P")) {
							map.put("customsIdType", "A");
						}
					}
					manifestBatchMapper.updateExcelUploadUps(map);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
					
					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(cnt));
					logMap = new HashMap<String, Object>();
					logMap.putAll(tmpLogMap);
					logMap.put("bound", map.get("bound"));
					logMap.put("reference_no_id", bulkData.getId());
					logMap.put("job_no", map.get("odJobNo"));
					logMap.put("description", "UPDATE EXCEL UPLOAD MANIFEST UPS[HBL_NO : " + map.get("hblNo") +"]");
					logMap.put("job_log", new Gson().toJson(map));
					bulkData.setJsonMap(logMap);
					bulkList.add(bulkData);
				}
			}
			cnt = 1;
			for(Map<String, Object> map : dataListAd){
				manifestBatchMapper.updateExcelUploadUpsAd(map);
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
		return orderSaveResult;
	}
	
	/**
	 * tracking 업데이트 ExcelUpload
	 * @param excelUploadList
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult upsExcelUploadTracking(ExcelUploadList excelUploadList) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		List<Map<String, Object>> dataList = excelUploadList.getDataList();
		
		if(dataList == null) {
			return orderSaveResult;
		}
		
		Map<String, Object> tmpMap;
		for(Map<String, Object> map : dataList){
			tmpMap = manifestMapper.selectExcelUploadTrackingChk(map);
			if(tmpMap != null){
				map.put("odJobNo", tmpMap.get("OD_JOB_NO"));
				map.put("bound", tmpMap.get("BOUND"));
				map.put("pieces", tmpMap.get("PIECES"));
				map.put("hblNo", tmpMap.get("HBL_NO"));
			}
		}
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", excelUploadList.getEntCode());
		tmpLogMap.put("ent_business_code", excelUploadList.getUserEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("bound_type", "IMP");
		tmpLogMap.put("process_user_id", excelUploadList.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "EXCEL");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", excelUploadList.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", excelUploadList.getLogUserName());
		tmpLogMap.put("process_user_name_eng", excelUploadList.getLogUserNameEng());
		
		int cnt = 1;
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
			Util.accessControlDatabase(excelUploadList, "gmtTime", false, null, null, null, null);
//			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			for(Map<String, Object> map : dataList){
				if(!Util.isEmpty(map.get("odJobNo"))){
					map.put("userId", excelUploadList.getUpdateUserId());
					map.put("gmtTime", excelUploadList.getGmtTime());
					manifestBatchMapper.updateExcelUploadTracking(map);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
					
					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(cnt));
					logMap = new HashMap<String, Object>();
					logMap.putAll(tmpLogMap);
					logMap.put("bound", map.get("bound"));
					logMap.put("reference_no_id", bulkData.getId());
					logMap.put("job_no", map.get("odJobNo"));
					logMap.put("description", "UPDATE EXCEL UPLOAD TRACKING[HBL_NO : " + map.get("hblNo") +"]");
					logMap.put("job_log", new Gson().toJson(map));
					bulkData.setJsonMap(logMap);
					bulkList.add(bulkData);
				}
			}
//			cnt = 1;
//			tmpMap = new HashMap<String, Object>();
//			List<String> packingNoList;
//			int packingCnt = 0;
//			for(Map<String, Object> map : dataList){
//				if(!Util.isEmpty(map.get("odJobNo"))){
//					tmpMap.put("odJobNo", map.get("odJobNo"));
//					packingNoList = (List<String>)map.get("packingNoList");
//					packingCnt = 0;
//					for(String packingNo : packingNoList){
//						if(packingNo.length() == 18 || packingNo.equals(Util.null2str(map.get("hblNo")))){
//							packingCnt++;
//						}
//					}
//					if(packingCnt != Util.null2int(map.get("pieces"))){
//						tmpMap.put("packingErrorYn", "Y");
//					}else{
//						tmpMap.put("packingErrorYn", "N");
//					}
//					orderBatchMapper.updatePackingErrorYn(tmpMap);
//					
//					if( (cnt++ % 1000) == 0){
//						sqlSession.flushStatements();
//						sqlSession.clearCache();
//					}
//				}
//			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		
		commonService.insertLogBulk(bulkList);
			
		return orderSaveResult;
	}
	
	/**
	 * ECM 적하목록 마스터 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestOutList selectMasterOutList(SearchManifestOutList search) throws Exception {
		ManifestOutList manifestOutList = new ManifestOutList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
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
		
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "od.OB_DATE|od.AR_DATE", "srchNoType", "od.MBL_NO|od.HBL_NO|od.MASTER_OD_JOB_NO");
		search.setProfileName(profile);
		List<ManifestOutMaster> list  = manifestMapper.selectMasterOutList(search);
		manifestOutList.setManifestOutMasterList(list);
		if(list != null && list.size() > 0) {
			manifestOutList.setTotal(list.get(0).getTotal());
		}else {
			manifestOutList.setTotal(0);
		}
		
		return manifestOutList;
	}
	
	/**
	 * ECM 적하목록 하우스 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestOutList selectHouseOutList(SearchManifestOutList search) throws Exception {
		ManifestOutList manifestOutList = new ManifestOutList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchHNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchHNo())).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchHNo()).trim().replace(",", ""));
		}
		
		Util.accessControlDatabase(search, "userLanguage", true, null, null, "srchHNoType", "od.HBL_NO|od.HSN|odAdd.SIMPLE_DOC_NO");
		List<ManifestOutHouse> list  = manifestMapper.selectHouseOutList(search);
		manifestOutList.setManifestOutHouseList(list);
		if(list != null && list.size() > 0) {
			manifestOutList.setTotal(list.get(0).getTotal());
		}else {
			manifestOutList.setTotal(0);
		}
		
		return manifestOutList;
	}
	
	/**
	 * Order Detail
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestOrder selectOrderDetail(SearchManifest search) throws Exception {		
		
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		ManifestOrder order  = manifestMapper.selectOrder(search);
		
		return order;
	}
	
	/**
	 * Master Order - update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestOrderSaveResult saveOrder(Order order, HttpServletRequest request) throws Exception {
		ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
		String ediExpressSign = null, ediExpressSignDocumentY = null, ediExpressSignDocumentN = null;
		String stockIssueYymm = null;
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		boolean updateCustomsData = true;
		if("srchMfDateUpdate".equals(order.getAction())){
			updateCustomsData = false;
		}
		
		Util.accessControlDatabase(order, "gmtTime|dateFormat", false, null, null, null, null);
		
		/** 통관목록제출번호수정 */
		boolean updateSimpleDocNoFlag = false;
		Map<String, Object> busEdiEcmMap = null;
		ConsolOrder oldOrder = new ConsolOrder();
		oldOrder.setOdJobNo(order.getOdJobNo());
		if(order.getBoundType().equals("IMP")){
			oldOrder.setAction("saveManifest");
			oldOrder.setOldSimpleDocNo(order.getOldSimpleDocNo());
		}
		oldOrder = consolMapper.selectConsolMasterInfo(oldOrder);
		if(!oldOrder.getMblNo().equals(order.getMblNo())){
			if(Util.null2int(oldOrder.getSimpleEdiSendCnt()) > 0){
				manifestOrderSaveResult.setResultCode("simpleDocSend");
				return manifestOrderSaveResult;
			}else{
				tmpMap.put("entCode", order.getEntCode());
				tmpMap.put("blTypeMh", "M");
				tmpMap.put("blNo", order.getMblNo());
				Map<String, Object> blNoChkMap = orderMapper.selectOrderByBlNo(tmpMap);
				if(blNoChkMap != null && blNoChkMap.size() > 0){
					manifestOrderSaveResult.setResultCode("duplicateBlNo");
					return manifestOrderSaveResult;
				}
			}
		}
		tmpMap.clear();
		if((order.getBoundType().equals("EXP") && !order.getOrigCode().equals(oldOrder.getOrigCode())) || (order.getBoundType().equals("IMP") && !order.getDestPort().equals(oldOrder.getDestPort())) || !Util.isEmpty(order.getSimpleDocNoSeqNo())){
			updateSimpleDocNoFlag = true;
			if(Util.isEmpty(order.getSimpleDocNoStockNo())){	/** 수입화면에서 S버튼으로 스탁번호생성하지 않은 경우 */
				tmpMap.put("srchEntCode", order.getEntCode());
				tmpMap.put("srchEntBusinessCode", order.getEntBusinessCode());
				tmpMap.put("srchBound", order.getBound());
				if(order.getBoundType().equals("EXP")){
					tmpMap.put("srchEdiCityPort", order.getOrigCode());
				}else{
					tmpMap.put("srchEdiCityPort", order.getDestPort());
					if(!Util.isEmpty(order.getSimpleDocNoSeqNo())){
						tmpMap.put("dateLength", "2");
						tmpMap.put("stockDateFormat", "12");
					}
				}
				/** 제출번호에 사용되는 특송부호 조회 */
				busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmpMap);
				if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
					ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN"));
					ediExpressSignDocumentY = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
					ediExpressSignDocumentN = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"));
					stockIssueYymm = Util.null2str(busEdiEcmMap.get("STOCK_ISSUE_YYMM"));
				}else{
					order.setSimpleDocNo(null);

					/** 통관목록전송시체크 */
//					manifestOrderSaveResult.setResultCode("busEdiEcmNull");
//					return manifestOrderSaveResult;
				}
			}
		}
		
		String tmpSimpleDocNo = null;
		if(order.getBoundType().equals("IMP") && busEdiEcmMap != null){
			if(oldOrder.getDocumentYn().indexOf("N") > -1){	/** 서류N or 서류YN(제출번호가 없을경우 서류Y건과 서류N건이 같이 있을수 있음) */
				order.setDocumentYn("N");
				ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"));
			}else{
				order.setDocumentYn("Y");
				ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
			}
			
			tmpMap.put("entCode", order.getEntCode());
			tmpMap.put("gmtTime", order.getGmtTime());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("updateUserId", order.getUpdateUserId());
			tmpMap.put("solution", "ECM");
			tmpMap.put("stockType", "OD");
			tmpMap.put("stockSeqLength", "8");
			tmpMap.put("signType", "noHyphen");
			tmpMap.put("dateLength", "2");
			tmpMap.put("stockDateFormat", "12");
			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", "ORDER SIMPLE DOC NO");
			
			if(!Util.isEmpty(order.getSimpleDocNoSeqNo())){
				tmpMap.put("updateMaxNo", order.getSimpleDocNoSeqNo());
				if(!Util.isEmpty(ediExpressSign)){
					tmpMap.put("stockSign", ediExpressSign);
					if(updateCustomsData){
						if(commonMapper.updateNextStockMaxNo(tmpMap) == 0) {
							if(commonMapper.selectStockMaxNo(tmpMap) == null){
								commonMapper.insertStockMaxNo(tmpMap);
							}else{
								manifestOrderSaveResult.setResultCode("A11331");	/** 마지막 제출번호보다 작은번호는 사용할 수 없습니다. */
								return manifestOrderSaveResult;
							}
						}
					}
					order.setSimpleDocNo(ediExpressSign + stockIssueYymm + Util.lpad(Util.null2str(order.getSimpleDocNoSeqNo()), 8, '0'));
				}
				
				if("YN".equals(oldOrder.getDocumentYn())){	/** 해당 마스터에 서류Y,N이 같이 있을경우 서류Y의 제출번호 추가 생성    생성불가할경우 업데이트하지않음 */
					ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
					if(!Util.isEmpty(ediExpressSign)){
						tmpMap.put("stockSign", ediExpressSign);
						if(updateCustomsData){
							if(commonMapper.updateNextStockMaxNo(tmpMap) == 0) {
								if(commonMapper.selectStockMaxNo(tmpMap) == null){
									commonMapper.insertStockMaxNo(tmpMap);
								}else{
									tmpSimpleDocNo = "N";
								}
							}
						}
						if("N".equals(tmpSimpleDocNo)){
							tmpSimpleDocNo = null;
						}else{
							tmpSimpleDocNo = ediExpressSign + stockIssueYymm + Util.lpad(Util.null2str(order.getSimpleDocNoSeqNo()), 8, '0');
						}
					}
				}
			}else{
				if(!Util.isEmpty(ediExpressSign)){
					tmpMap.put("stockSign", ediExpressSign);
					if(updateCustomsData){
						order.setSimpleDocNo(commonMapper.selectNextStockMaxNo(tmpMap));
					}
				}
				if("YN".equals(oldOrder.getDocumentYn())){
					ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
					if(!Util.isEmpty(ediExpressSign)){
						tmpMap.put("stockSign", ediExpressSign);
						if(updateCustomsData){
							tmpSimpleDocNo = commonMapper.selectNextStockMaxNo(tmpMap);
						}
					}
				}
			}
		}
		
		//update 처리
		order.setBlTypeMh("M");
		if(updateCustomsData){
			order.setAction("updateSelectorUserId");
		}
		manifestMapper.updateOrder(order);
		
		/** MASTER 수정 LOG */
		ProcessLogs processMblLogs = new ProcessLogs();
		processMblLogs.setJobFuncType("UPDATE");
		processMblLogs.setEntCode(order.getEntCode());
		processMblLogs.setEntBusinessCode(order.getEntBusinessCode());
		processMblLogs.setSolution("ECM");
		processMblLogs.setBound(order.getBound());
		processMblLogs.setBoundType(order.getBoundType());
		processMblLogs.setProcessUserId(order.getUpdateUserId());
		processMblLogs.setJobNo(order.getOdJobNo());
		processMblLogs.setLogJobType("DATA");
		processMblLogs.setDescription("MASTER UPDATE[MBL_NO : " + order.getMblNo()+"]");
		processMblLogs.setJobLog(new Gson().toJson(order));
		processMblLogs.setResult("ok");
		processMblLogs.setEntBusinessPrintName(order.getLogUserBusinessPrintName());
		processMblLogs.setProcessUserName(order.getLogUserName());
		processMblLogs.setProcessUserNameEng(order.getLogUserNameEng());
		commonService.insertLog(processMblLogs, request);
		
		/** TB_COM_OD수정 */
		if(!Util.isEmpty(oldOrder.getOblJobNo())){
			order.setOblJobNo(oldOrder.getOblJobNo());
			manifestMapper.updateComObl(order);
			manifestMapper.updateComOrder(order);
			manifestMapper.updateComOrderBus(order);
			
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setJobFuncType("UPDATE");
			processLogs.setEntCode(order.getEntCode());
			processLogs.setEntBusinessCode(order.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(order.getBound());
			processLogs.setBoundType(order.getBoundType());
			processLogs.setProcessUserId(order.getUpdateUserId());
			processLogs.setJobNo(order.getOblJobNo());
			processLogs.setLogJobType("DATA");
			processLogs.setDescription("MANIFEST UPDATE[MBL_NO : " + order.getMblNo()+"]");
			processLogs.setJobLog(new Gson().toJson(order));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(order.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(order.getLogUserName());
			processLogs.setProcessUserNameEng(order.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}

		ConsolOrder consolOrder = new ConsolOrder();
		consolOrder.setOdJobNo(order.getOdJobNo());
		consolOrder.setCarryoutCode(order.getCarryoutCode());
		consolOrder.setAction("manifestUpdate");
		List<Map<String, Object>> consolHouseList = consolMapper.selectConsolTarget(consolOrder);
		
		if(order.getBoundType().equals("IMP")){
			String simpleDocNo = null;
			if(!Util.isEmpty(order.getSimpleDocNoStockNo())){	/** S버튼(제출번호생성) 사용했을경우 */
				if("updateDocumentYn".equals(order.getSimpleDocNoStockNo())){	/** 마스터에 서류Y/서류N이 모두 포함되어 있을때 */
					simpleDocNo = Util.null2str(order.getSimpleDocNo());
					
					order.setDocumentYn("N");
					order.setSimpleDocNo(order.getSimpleDocNoDocumentN());
					if(Util.isEmpty(oldOrder.getSimpleDocNo()) && !Util.isEmpty(order.getSimpleDocNo())){
						order.setAction("updateMaster");
					}
					if(updateCustomsData){
						manifestMapper.updateSimpleDocNo(order);
					}
					
					order.setDocumentYn("Y");
					order.setSimpleDocNo(order.getSimpleDocNoDocumentY());
					if(Util.isEmpty(oldOrder.getSimpleDocNo()) && !Util.isEmpty(order.getSimpleDocNo())){
						order.setAction("updateMaster");
					}
					if(updateCustomsData){
						manifestMapper.updateSimpleDocNo(order);
					}
					
					order.setSimpleDocNo(simpleDocNo);
				}else{
					if(Util.isEmpty(oldOrder.getSimpleDocNo()) && !Util.isEmpty(order.getSimpleDocNo())) {
						order.setAction("updateMaster");
					}
					order.setDocumentYn(oldOrder.getDocumentYn().substring(0, 1));
					if(updateCustomsData){
						manifestMapper.updateSimpleDocNo(order);
					}
				}
			}else if(updateSimpleDocNoFlag){
				if(Util.isEmpty(oldOrder.getSimpleDocNo()) && !Util.isEmpty(order.getSimpleDocNo())){
					order.setAction("updateMaster");
					simpleDocNo = order.getSimpleDocNo();
				}
				if(updateCustomsData){
					manifestMapper.updateSimpleDocNo(order);
				}
				if("YN".equals(oldOrder.getDocumentYn())){
					if(Util.isEmpty(oldOrder.getSimpleDocNo()) && Util.isEmpty(order.getSimpleDocNo())){
						order.setAction("updateMaster");
						simpleDocNo = tmpSimpleDocNo;
					}else{
						order.setAction("");
					}
					order.setDocumentYn("Y");
					order.setSimpleDocNo(tmpSimpleDocNo);
					if(updateCustomsData){
						manifestMapper.updateSimpleDocNo(order);
					}
					order.setSimpleDocNo(simpleDocNo);
				}
			}
		}
		String odJobNo = order.getOdJobNo();
		
		boolean changeExrateFlag = false;
		if(order.getBoundType().equals("EXP") && !oldOrder.getObDate().equals(order.getObDate()) || order.getBoundType().equals("IMP") && !oldOrder.getArDate().equals(order.getArDate())){
			changeExrateFlag = true;
		}
		
		if(consolHouseList != null && consolHouseList.size() > 0){
			SqlSession sqlSession = null;
			int cnt = 1;
			tmpMap.clear();
			try{
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
				
				Map<String, Object> exchangeRateMap = null;
				if(changeExrateFlag) {
					tmpMap.put("srchEntCode", order.getEntCode());
					tmpMap.put("entBusinessCode", order.getEntBusinessCode());
					tmpMap.put("srchDateFormat", "23");
					if(Util.null2str(order.getBoundType()).equals("EXP")){
						tmpMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
						tmpMap.put("srchDate", Util.null2str(order.getObDate()));
					}else{
						tmpMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
						tmpMap.put("srchDate", Util.null2str(order.getArDate()));
					}
					
					exchangeRateMap = commonMapper.selectExchangeRate(tmpMap);
				}
				
				Map<String, Object> orderSaveMap = new HashMap<String, Object>();
				
				order.setBlTypeMh("H");
				
				List<Order> updateOrderList = new ArrayList<Order>();
				Order tmpUpdateOrder = null;
				int seqNo = 1;
				
				/** 항공수출일경우 DEST_CODE값에 따라 하우스의 송하인/수하인 팩스번호 변경 */
				if("AIR".equals(order.getBound()) && "EXP".equals(order.getBoundType()) && !Util.null2str(oldOrder.getDestCode()).equals(order.getDestCode())){
					if(!Util.isEmpty(order.getDestCode())){
						tmpMap.clear();
						tmpMap.put("groupCode", "CITY_PORT");
						tmpMap.put("entCode", order.getEntCode());
						tmpMap.put("srchCode", order.getDestCode());
						tmpMap = commonMapper.getCodeDetail(tmpMap);
						if(tmpMap != null && tmpMap.size() > 0){
							if("CN".equals(tmpMap.get("ETC2"))){
								order.setShipperFax("VAT NUMBER");
								order.setConsigneeFax("USCI");
							}else if("ID".equals(tmpMap.get("ETC2"))){
								order.setConsigneeFax("TIN");
							}else if("210".equals(tmpMap.get("ETC3"))){
								order.setConsigneeFax("T");
							}
						}
					}
				}else{
					order.setShipperFax("noUpdate");
				}
				String masterAssignmentWhCode = Util.null2str(order.getAssignmentWhCode());
				String masterEmsApprovalNo = Util.null2str(order.getEmsApprovalNo());
				for(Map<String, Object> consol : consolHouseList){
					order.setOdJobNo(Util.null2str(consol.get("OD_JOB_NO")));
					order.setCurrencyCode(Util.null2str(consol.get("CURRENCY_CODE")));
					order.setAmountCur(Util.null2dbl(consol.get("AMOUNT_CUR")));
					order.setCdType(Util.null2str(consol.get("CD_TYPE")));
					
					if(order.getBoundType().equals("EXP")){
						if(updateSimpleDocNoFlag){
							order.setSimpleDocNo("null");	/** "null"일경우 SIMPLE_DOC_NO = NULL로 업데이트 */
						}else{
							order.setSimpleDocNo(null);	/** null일경우 SIMPLE_DOC_NO 업데이트 하지않음 */
						}
					}else{
						if(updateCustomsData){
							if(Util.null2str(consol.get("SIMPLE_DOC_NO")).equals(Util.null2str(order.getOldSimpleDocNo()))){
								order.setAction("updateSelectorUserId");
							}else{
								order.setAction(null);
							}
						}else{
							order.setAction("srchMfDateUpdate");
						}
					}
					
					orderSaveMap.clear();
					if(changeExrateFlag) {
						/** 공통 - 환율처리 */
						orderSaveMap.put("exChangeYn", "Y");
						orderSaveMap.put("exrateMap", exchangeRateMap);
					}else{
						order.setAmountLoc(Util.null2dbl(consol.get("AMOUNT_LOC")));
						order.setAmountUsd(Util.null2dbl(consol.get("AMOUNT_USD")));
						order.setExchangeRate(Util.null2dbl(consol.get("EXCHANGE_RATE")));
					}
					/** 2025-06-20 하우스의 반출사유값이 없을경우만 마스터 정보로 업데이트 */
					if(Util.isEmpty(consol.get("CARRYOUT_CODE"))){
						order.setCarryoutCode(consolOrder.getCarryoutCode());
					}else{
						order.setCarryoutCode(null);
					}
					if(order.getBoundType().equals("EXP") && ediExpressSign != null && ("Y".equals(order.getCdType()) || "E".equals(order.getCdType()))){
						/** 제출번호 생성할경우 orderCommonService.orderSave에서 셀렉트 쿼리 실행하기때문에 배치쿼리 따로 처리 */
						if("Y".equals(consol.get("DOCUMENT_YN")) && !Util.isEmpty(ediExpressSignDocumentY)){
							orderSaveMap.put("createSimpleDocNo", "Y");	/** 제출번호생성 */
							orderSaveMap.put("ediExpressSign", ediExpressSignDocumentY);
						}else if("N".equals(consol.get("DOCUMENT_YN")) && !Util.isEmpty(ediExpressSignDocumentN)){
							orderSaveMap.put("createSimpleDocNo", "Y");	/** 제출번호생성 */
							orderSaveMap.put("ediExpressSign", ediExpressSignDocumentN);
						}
						orderCommonService.orderSave(order, "consol", orderSaveMap, seqNo++);
						tmpUpdateOrder = order.clone();
						updateOrderList.add(tmpUpdateOrder);
					}else{
						orderCommonService.orderSave(order, "consol", orderSaveMap, seqNo++);
						if(Util.isEmpty(consol.get("ASSIGNMENT_WH_CODE"))){
							order.setAssignmentWhCode(masterAssignmentWhCode);
						}else{
							order.setAssignmentWhCode(null);
						}
						if(Util.isEmpty(consol.get("EMS_APPROVAL_NO"))){
							order.setEmsApprovalNo(masterEmsApprovalNo);
						}else{
							order.setEmsApprovalNo(null);
						}
						
						manifestBatchMapper.updateOrder(order);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
				if(order.getBoundType().equals("EXP") && ediExpressSign != null){
					cnt = 1;
					for(Order updateOrder : updateOrderList){
						manifestBatchMapper.updateOrder(updateOrder);
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
		}
		
		//오더번호 셋팅
		manifestOrderSaveResult.setOdJobNo(odJobNo);
		manifestOrderSaveResult.setResultCode(order.getSimpleDocNo());
		
		return manifestOrderSaveResult;
	}
	
	/**
	 * Master Order - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestOrderSaveResult deleteOrder(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
		
		Map<String, Object> tmpMap = manifestMapper.selectDeleteMasterOrderCheck(sendManifest);
		if(Util.null2int(tmpMap.get("CNT")) > 0){
			manifestOrderSaveResult.setResultCode("A11333");	/**	통관목록이 전송되어서 처리할 수 없습니다. */
			return manifestOrderSaveResult;
		}
		
		tmpMap = new HashMap<String, Object>();
		tmpMap.put("masterOdJobNo", sendManifest.getMasterOdJobNo());
		tmpMap.put("houseOdJobNoList", sendManifest.getHouseOdJobNoList());
		tmpMap.put("delAlias", "delTable");
		
		if("EXP".equals(sendManifest.getBoundType())){
			tmpMap.put("delTable", "TB_ECM_OD_LICENSE");
			manifestMapper.deleteMasterOrder(tmpMap);
		}else{
			tmpMap.put("delTable", "TB_ECM_OD_MULTI");
			manifestMapper.deleteMasterOrder(tmpMap);
			tmpMap.put("delTable", "TB_ECM_OD_PACKING");
			manifestMapper.deleteMasterOrder(tmpMap);
			tmpMap.put("delTable", "TB_ECM_OD_PO_INFO");
			manifestMapper.deleteMasterOrder(tmpMap);
			if("AIR".equals(sendManifest.getBound())){
				tmpMap.put("delTable", "TB_ECM_OD_SPECIAL_ITEM");
				manifestMapper.deleteMasterOrder(tmpMap);
				tmpMap.put("delTable", "TB_ECM_OD_ABNORMAL");
				manifestMapper.deleteMasterOrder(tmpMap);
			}
		}
		if("SEA".equals(sendManifest.getBound())){
			tmpMap.put("delTable", "TB_ECM_OD_CONTAINER");
			manifestMapper.deleteMasterOrder(tmpMap);
		}
		tmpMap.put("delTable", "TB_SMS_ALARM");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_OD_ITEM");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_OD_COST");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_POD_01");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_POD_02");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_POD_03");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_POD_04");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_POD_05");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_OD_SELECTION_ITEM_HBL");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_OD_SELECTION_ITEM");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_OD_SELECTION");
		manifestMapper.deleteMasterOrder(tmpMap);
		tmpMap.put("delTable", "TB_ECM_OD_ADDITIONAL");
		manifestMapper.deleteMasterOrder(tmpMap);
		if(sendManifest.getHouseOdJobNoList() == null){
			tmpMap.remove("delTable");
			tmpMap.put("delAlias", "od");
		}else{
			tmpMap.put("delTable", "TB_ECM_OD");
		}
		manifestMapper.deleteMasterOrder(tmpMap);
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setJobFuncType("DELETE");
		processLogs.setEntCode(sendManifest.getEntCode());
		processLogs.setEntBusinessCode(sendManifest.getEntBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(sendManifest.getBound());
		processLogs.setBoundType(sendManifest.getBoundType());
		processLogs.setProcessUserId(sendManifest.getUpdateUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobLog(new Gson().toJson(sendManifest));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(sendManifest.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(sendManifest.getLogUserName());
		processLogs.setProcessUserNameEng(sendManifest.getLogUserNameEng());
		if(sendManifest.getHouseOdJobNoList() == null){
			processLogs.setJobNo(sendManifest.getMasterOdJobNo());
			processLogs.setDescription("MASTER ORDER DELETE[MBL_NO : " + sendManifest.getMblNo()+"]");
		}else{
			processLogs.setJobNo("");
			processLogs.setDescription("MANIFEST ORDER DELETE[MBL_NO : " + sendManifest.getMblNo()+", DELETE HOUSE COUNT: " + sendManifest.getHouseOdJobNoList().size() + "]");
		}
		commonService.insertLog(processLogs, request);
		
		return manifestOrderSaveResult;
	}
	
	/**
	 * Convert Ams Address
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestOrderSaveResult convertAmsAddress(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
		
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", sendManifest.getEntCode());
		tmpLogMap.put("ent_business_code", sendManifest.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", sendManifest.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", sendManifest.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", sendManifest.getLogUserName());
		tmpLogMap.put("process_user_name_eng", sendManifest.getLogUserNameEng());
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		AddressInfo addressInfo;
		SearchAddressInfo search = new SearchAddressInfo();
		search.setSrchEntCode(sendManifest.getEntCode());
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
			
			int cnt = 1;
			List<Map<String, Object>> dataList = manifestBatchMapper.convertAmsAddressList(sendManifest);
			for(Map<String, Object> data : dataList){
				search.setSrchAddress(Util.null2str(data.get("SHIPPER_ADDRESS")));
				addressInfo = commonService.getAddressInfo(search);
				data.put("SHIPPER_TEL", addressInfo.getTel());
				data.put("SHIPPER_STREET", ediCommonService.getSubStringK(Util.null2str(addressInfo.getStreet()), 0, 35));
				data.put("SHIPPER_PLACE", addressInfo.getPlace());
				data.put("SHIPPER_STATE_CODE", addressInfo.getStateCode());
				data.put("SHIPPER_COUNTRY_CODE", addressInfo.getCountryCode());
				data.put("SHIPPER_ZIPCODE", addressInfo.getZipcode());
				search.setSrchAddress(Util.null2str(data.get("CONSIGNEE_ADDRESS")));
				addressInfo = commonService.getAddressInfo(search);
				data.put("CONSIGNEE_TEL", addressInfo.getTel());
				data.put("CONSIGNEE_STREET", ediCommonService.getSubStringK(Util.null2str(addressInfo.getStreet()), 0, 35));
				data.put("CONSIGNEE_PLACE", addressInfo.getPlace());
				data.put("CONSIGNEE_STATE_CODE", addressInfo.getStateCode());
				data.put("CONSIGNEE_COUNTRY_CODE", addressInfo.getCountryCode());
				data.put("CONSIGNEE_ZIPCODE", addressInfo.getZipcode());
			}
			
			for(Map<String, Object> data : dataList){
				data.put("cryptographyYn", sendManifest.getCryptographyYn());
				manifestBatchMapper.updateAmsAddress(data);
				if((cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(cnt++));
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("bound", data.get("BOUND"));
				logMap.put("bound_type", data.get("BOUND_TYPE"));
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", data.get("OD_JOB_NO"));
				logMap.put("job_log", "");
				logMap.put("description", "AMS 주소변환 [H.B/L : " + data.get("HBL_NO") + "]");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
			}
			
			if(bulkList.size() > 0) {
				commonService.insertLogBulk(bulkList);
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		return manifestOrderSaveResult;
	}
	
	/**
	 * BMS반입예정생성
	 * @return
	 * @throws Exception
	 */
	public ManifestOrderSaveResult insertBmsInPlan(SendManifest sendManifest) throws Exception {
		ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
		Map<String, Object> resultData = new HashMap<String, Object>();
		Util.accessControlDatabase(sendManifest, "gmtTime", false, null, null, null, null);
//		if(manifestMapper.selectBmsInPlanInsertCheck(sendManifest) == 0) {
//			resultData.put("errorStr", "통관목록 결과통보 수신 후 반입예정생성 가능합니다.");
//			manifestOrderSaveResult.setResultCode("ERROR");
//			manifestOrderSaveResult.setResultData(resultData);
//			return manifestOrderSaveResult;
//		}
		
		String bmsCustomsCode = null, bmsCustomsDptCode = null, b2gForwarderSign = null;
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", sendManifest.getEntCode());
		tmpMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
		tmpMap = manifestMapper.getFwSendAccInfo(tmpMap);
		if(tmpMap != null){
			bmsCustomsCode = Util.null2str(tmpMap.get("BMS_CUSTOMS_CODE"));
			bmsCustomsDptCode = Util.null2str(tmpMap.get("BMS_CUSTOMS_DPT_CODE"));
			b2gForwarderSign = Util.null2str(tmpMap.get("B2G_FORWARDER_SIGN"));
		}
		
		int size = 0;
		List<Map<String, Object>> list = manifestMapper.selectBmsInPlanInsertTarget(sendManifest);
		if(list != null && list.size() > 0) {

            Map<String, Object> resultMap = new HashMap<String, Object>();
            String iertHblNo = "";
            for (Map<String, Object> validMap : list) {
                if ("".equals(Util.null2str(validMap.get("MRN"))) || "".equals(Util.null2str(validMap.get("HSN"))) || "".equals(Util.null2str(validMap.get("MSN")))) {
                    iertHblNo += validMap.get("HBL_NO") + "</br>";
                }
            }
            if(!iertHblNo.isEmpty()){
                resultMap.put("returnMessage", iertHblNo);
                manifestOrderSaveResult.setResultCode("mrnIsNull");
                manifestOrderSaveResult.setResultData(resultMap);
                return manifestOrderSaveResult;
            }

			SqlSession sqlSession = null;
			try{
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
				
				/** 2025-03-13 반입예정생성/결과(선별)통보/취하통보 시 일반/배제/취하 수입화물진행정보실행 */
				ListOperations<String, String> listOperations = redisTemplate.opsForList();
				String key = "E-COMMERCE_CARGO_PROGRESS_INSERT";
				JSONObject json = new JSONObject();
				Map<String, Object> order = list.get(0);
				json.put("action", "sendGw");
				json.put("entCode", sendManifest.getEntCode());
				json.put("entBusinessCode", sendManifest.getEntBusinessCode());
				json.put("bound", sendManifest.getBound());
				json.put("boundType", "IMP");
				json.put("customs", order.get("CUSTOMS_CODE"));
				json.put("masterOdJobNo", order.get("MASTER_OD_JOB_NO"));
				json.put("mblNo", order.get("MBL_NO"));
				json.put("arDate", Util.null2str(order.get("AR_DATE_STR")));
				json.put("destCode", order.get("DEST_CODE"));
				json.put("destPort", order.get("DEST_PORT"));
				json.put("mrn", order.get("MRN"));
				if(!Util.null2str(order.get("DEST_CODE")).equals(Util.null2str(order.get("DEST_PORT")))){
					json.put("bondedCargoYn", "Y");
				}else{
					json.put("bondedCargoYn", "N");
				}
				
				String podJobNo = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5);
				int cnt = 0;
				size = list.size();
				for(Map<String, Object> map : list) {
					cnt++;
					map.put("bmsInPlanYn", "Y");
					manifestBatchMapper.updateBmsInPlanYn(map);
					if (cnt % 1000 == 0 || cnt == size) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
					
					if(!"Y".equals(map.get("CD_TYPE")) || "Y".equals(map.get("DROP_YN"))){
						json.put("status", map.get("LAST_POD_STATUS"));
						json.put("odJobNo", map.get("OD_JOB_NO"));
						json.put("hblNo", map.get("HBL_NO"));
						json.put("dropYn", map.get("DROP_YN"));
						json.put("cdType", map.get("CD_TYPE"));
						listOperations.rightPush(key, json.toString());
					}
				}
				
				cnt=0;
				for(Map<String, Object> map : list) {
					cnt++;
					map.put("bmsCustomsCode", bmsCustomsCode);
					map.put("bmsCustomsDptCode", bmsCustomsDptCode);
					map.put("b2gForwarderSign", b2gForwarderSign);
					map.put("entCode", sendManifest.getEntCode());
					map.put("gmtTime", sendManifest.getGmtTime());
					map.put("userId", sendManifest.getRegisterUserId());
					map.put("podJobNo", podJobNo + Util.setSeqNo(cnt));
					manifestBatchMapper.insertBmsInPlan(map);
					if (cnt % 1000 == 0 || cnt == size) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				
				sqlSession.commit();
			}catch(Exception e) {
				throw e;
			}finally{
				sqlSession.close();
			}
		}
		
		manifestOrderSaveResult.setResultCode(Util.null2str(size));
		return manifestOrderSaveResult;
	}
	
	/**
	 * BMS반입예정삭제
	 * @return
	 * @throws Exception
	 */
	public ManifestOrderSaveResult deleteBmsInPlan(SendManifest sendManifest) throws Exception {
		ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
		Map<String, Object> resultData = new HashMap<String, Object>();
		if(manifestMapper.selectBmsInPlanDeleteCheck(sendManifest) == 0) {
			resultData.put("errorStr", "반입예정생성건이 없습니다.");
			manifestOrderSaveResult.setResultCode("ERROR");
			manifestOrderSaveResult.setResultData(resultData);
			return manifestOrderSaveResult;
		}
		
		int size = 0;
		List<Map<String, Object>> list = manifestMapper.selectBmsInPlanDeleteTarget(sendManifest);
		if(list != null && list.size() > 0) {
			SqlSession sqlSession = null;
			try{
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
				Util.accessControlDatabase(sendManifest, "gmtTime", false, null, null, null, null);
				
				Map<String, Object> delMap = new HashMap<String, Object>();
				delMap.put("entCode", sendManifest.getEntCode());
				delMap.put("gmtTime", sendManifest.getGmtTime());
				delMap.put("userId", sendManifest.getRegisterUserId());
				String podJobNo = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5);
				
				int cnt = 0;
				for(Map<String, Object> map : list) {
					if(map.get("XRAY_NO") == null && map.get("IN_NO") == null){
						cnt++;
						size++;
						map.put("bmsInPlanYn", "N");
						manifestBatchMapper.updateBmsInPlanYn(map);
					}
					if (cnt % 1000 == 0) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				
				cnt=0;
				for(Map<String, Object> map : list) {
					if(map.get("XRAY_NO") == null && map.get("IN_NO") == null){
						cnt++;
						delMap.put("podJobNo", podJobNo + Util.setSeqNo(cnt));
						delMap.put("odJobNo", map.get("OD_JOB_NO"));
						delMap.put("inPlanNo", map.get("IN_PLAN_NO"));
						manifestBatchMapper.deleteBmsInPlan(delMap);
					}
					if (cnt % 1000 == 0) {
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
		
		manifestOrderSaveResult.setResultCode(Util.null2str(size));
		return manifestOrderSaveResult;
	}
	
	/**
	 * 통관목록 오류건 자료생성
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateSimpleError(SearchOrderBatchUpdate search, HttpServletRequest request) throws Exception{
		OrderSaveResult result = new OrderSaveResult();
		String targetData = Util.null2str(search.getTargetData());
		Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);
		
		SearchOrderList searchOrderList = null;
		if(targetData.equals("srchData")){
			searchOrderList = search.getSearchOrderList();
			
			/* N개 검색(멀티타입) */
			String srchNo = Util.null2str(searchOrderList.getSrchNo());
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(srchNo)) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(srchNo).split(",")));
			}
			
			if(searchNoList.size() > 1){
				searchOrderList.setSearchType("multi");
				searchOrderList.setSearchNoList(searchNoList);
			}else{
				srchNo = srchNo.trim().replace(",", "");
				searchOrderList.setSearchType("");
				searchOrderList.setSrchNo(srchNo);
			}
		}else{
			searchOrderList = new SearchOrderList();
			searchOrderList.setSrchEntCode(search.getEntCode());
			searchOrderList.setGmtTime(search.getGmtTime());
			searchOrderList.setSrchNo("Y");
			searchOrderList.setSearchType("multi");
			searchOrderList.setSearchNoList(search.getCheckDataList());
			searchOrderList.setSrchNoType("od.OD_JOB_NO");
		}
		
		Util.accessControlDatabase(searchOrderList, "gmtTime|srchDateFormat", false, "srchDateGbn", "|noDateChk|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE", "srchNoType", "od.OD_JOB_NO|od.MBL_NO|od.HBL_NO|od.MRN|ad.SIMPLE_DOC_NO");
		
		String errorStr = "";
		List<Map<String, Object>> masterList = manifestMapper.selectSimpleError(searchOrderList);
		for(Map<String, Object> master : masterList){
			if("Y".equals(master.get("DOCUMENT_YN_MAX")) && Util.isEmpty(master.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"))){
				errorStr += ", Port : " + Util.null2str(master.get("DEST_PORT")) + ", DOC : Y의 특송부호가 등록되어있지 않습니다.";
			}
			if(!"Y".equals(master.get("DOCUMENT_YN_MIN")) && Util.isEmpty(master.get("EDI_EXPRESS_SIGN_DOCUMENT_N"))){
				errorStr += ", Port : " + Util.null2str(master.get("DEST_PORT")) + ", DOC : N의 특송부호가 등록되어있지 않습니다.";
			}
		}
		
		if(errorStr.length() > 0){
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("errorStr", errorStr.substring(2));
			result.setResultCode("error");
			result.setResultData(resultData);
			return result;
		}
		
		Map<String, Object> stockMap = new HashMap<String, Object>();
		stockMap.put("entCode", search.getEntCode());
		stockMap.put("entBusinessCode","");
		stockMap.put("gmtTime",search.getGmtTime());
		stockMap.put("updateUserId",search.getUpdateUserId());
		stockMap.put("solution", "ECM");
		stockMap.put("stockType", "OD");
		stockMap.put("stockSeqLength", "8");
		stockMap.put("signType", "noHyphen");
		stockMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		stockMap.put("stockRemark", "ORDER SIMPLE DOC NO");
		stockMap.put("dateLength", "2");
		stockMap.put("stockDateFormat", "12");
		int stockSeq = 1;
		Order order = new Order();
		String ecmYnChk = "";
		for(Map<String, Object> master : masterList){
			
			searchOrderList.setSrchMasterOdJobNo(Util.null2str(master.get("MASTER_OD_JOB_NO")));
			// 해당 데이터의 전자상거래가 Y/N이 섞여있는지 단일인지 체크한다.
			// 섞여있다면 제출번호를 Y인것끼리 N인것끼리 분리 시켜야한다.
			Map<String, Object> ecmMap = manifestMapper.selectEcmYnCheck(searchOrderList);
			ecmYnChk = Util.null2str(ecmMap.get("ECM_YN_CHK"));
			
			String ecmYn = Util.null2str(ecmMap.get("ECM_YN"));
			
			searchOrderList.setSimpleDocNoY(null);
			searchOrderList.setSimpleDocNoN(null);
			if("Y".equals(master.get("DOCUMENT_YN_MAX"))){
				stockMap.put("stockSign", Util.null2str(master.get("EDI_EXPRESS_SIGN_DOCUMENT_Y")));
				stockMap.put("seqNo", stockSeq++);
				searchOrderList.setSimpleDocNoY(commonMapper.selectNextStockMaxNo(stockMap));
			}
			if(!"Y".equals(master.get("DOCUMENT_YN_MIN"))){
				stockMap.put("stockSign", Util.null2str(master.get("EDI_EXPRESS_SIGN_DOCUMENT_N")));
				stockMap.put("seqNo", stockSeq++);
				searchOrderList.setSimpleDocNoN(commonMapper.selectNextStockMaxNo(stockMap));
			}
//			searchOrderList.setSrchMasterOdJobNo(Util.null2str(master.get("MASTER_OD_JOB_NO")));
			if(Util.isEmpty(master.get("SIMPLE_DOC_NO"))){
				order.setSimpleDocNo(Util.null2str(searchOrderList.getSimpleDocNoN(), searchOrderList.getSimpleDocNoY()));
				order.setOdJobNo(Util.null2str(master.get("MASTER_OD_JOB_NO")));
				consolMapper.updateSimpleDocNo(order);
			}
			
			// 전자상거래여부 ECM_YN 해당하는 값으로 업데이트한다.
			searchOrderList.setSrchEcmYn(ecmYn);
			manifestMapper.updateSimpleError(searchOrderList);
			
			if("Y".equals(ecmYnChk)) {
				
				searchOrderList.setSimpleDocNoY(null);
				searchOrderList.setSimpleDocNoN(null);
				if("Y".equals(master.get("DOCUMENT_YN_MAX"))){
					stockMap.put("stockSign", Util.null2str(master.get("EDI_EXPRESS_SIGN_DOCUMENT_Y")));
					stockMap.put("seqNo", stockSeq++);
					searchOrderList.setSimpleDocNoY(commonMapper.selectNextStockMaxNo(stockMap));
				}
				if(!"Y".equals(master.get("DOCUMENT_YN_MIN"))){
					stockMap.put("stockSign", Util.null2str(master.get("EDI_EXPRESS_SIGN_DOCUMENT_N")));
					stockMap.put("seqNo", stockSeq++);
					searchOrderList.setSimpleDocNoN(commonMapper.selectNextStockMaxNo(stockMap));
				}
				searchOrderList.setSrchMasterOdJobNo(Util.null2str(master.get("MASTER_OD_JOB_NO")));
				if(Util.isEmpty(master.get("SIMPLE_DOC_NO"))){
					order.setSimpleDocNo(Util.null2str(searchOrderList.getSimpleDocNoN(), searchOrderList.getSimpleDocNoY()));
					order.setOdJobNo(Util.null2str(master.get("MASTER_OD_JOB_NO")));
					order.setEcmYnChk("Y");
				}
				
				// 2번의 제출번호를 STOCK 생성한다는 것은 위에서 ECM_YN이 Y인값을 업데이트 하기 때문에 
				// 여기서는 ECM_YN이 N인값을 업데이트 해야함
				searchOrderList.setSrchEcmYn("N");
				manifestMapper.updateSimpleError(searchOrderList);
			}
			
		}
		
		return result;
	}
	
	/**
	 * EMS환적 문서송신
	 * @param sendManifest
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult sendEms(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> emsMap = manifestMapper.selectEmsInfo(sendManifest);
		if(emsMap == null){
			resultMap.put("errorStr", "우체국 고객번호를 입력하십시오.");
			orderSaveResult.setResultData(resultMap);
			return orderSaveResult;
		}
		
		
		List<Map<String, Object>> list = manifestMapper.selectSendEmsOrder(sendManifest);
		if(list != null && list.size() > 0){
			List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
			
			Map<String, Object> orderMap = list.get(0);
			orderMap.putAll(emsMap);
			
			orderMap.put("entCode", sendManifest.getEntCode());
			orderMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
			orderMap.put("userId", sendManifest.getRegisterUserId());
			orderMap.put("userName", sendManifest.getRegisterUserName());
			orderMap.put("userLanguage", sendManifest.getUserLanguage());
			
			Map<String, Object> fwSendAccInfo = ediCommonService.getFwSendAccInfo(orderMap);
			if(fwSendAccInfo == null || fwSendAccInfo.size() == 0 || Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).equals("") || Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).length() != 4){
				orderSaveResult.setResultCode("A11296");	/** 신청인부호를 4자리로 입력하십시오. */
				return orderSaveResult;
			}
			
			orderMap.put("B2G_FORWARDER_SIGN", fwSendAccInfo.get("B2G_FORWARDER_SIGN"));
			
			boolean flag = false;
			if("AIR".equals(orderMap.get("BOUND"))){
				orderMap.put("ifServiceCode", ediCommonService.getServiceCode("ECM", "AIR", "IMP", "EMS", "KTNET"));
				orderMap.put("ediVender", "KTNET");
				orderMap.put("EDI_VENDER", "KTNET");
				flag = true;
			}else{
				orderMap.put("ifServiceCode", ediCommonService.getServiceCode("ECM", "SEA", "IMP", "EMS", "KLNET"));
				orderMap.put("ediVender", "KLNET");
				orderMap.put("EDI_VENDER", "KLNET");
				flag = false;
			}
			
			Util.accessControlDatabase(orderMap, "ifServiceCodeIn", false, null, null, null, null);
			Map<String, Object> ediHostSendInfo = manifestMapper.getEdiInterfaceInfo(orderMap);
			
			orderMap.put("docType", "MANIFEST");
			orderMap.put("bound", orderMap.get("BOUND"));
			orderMap.put("boundType", "IMP");
			orderMap.put("action", sendManifest.getAction());
			if (!ediCommonService.getSendHostAndBaseDataValidate(orderMap, orderMap, fwSendAccInfo, ediHostSendInfo, errList, resultMap)) {
				resultMap.put("errorData", errList);
				orderSaveResult.setResultData(resultMap);
				return orderSaveResult;
			}
			
			if (!this.emsSendDataValidate(list, orderMap, errList, flag)) {
				resultMap.put("errorData", errList);
				orderSaveResult.setResultData(resultMap);
				return orderSaveResult;
			}
			if(!this.createEms(list, orderMap, ediHostSendInfo, errList, resultMap, flag)){
				resultMap.put("errorData", errList);
				orderSaveResult.setResultData(resultMap);
			}
		}else{
			orderSaveResult.setResultCode("A00338");	/** 선택 된 자료가 없습니다. */
		}
		
		return orderSaveResult;
	}
	
	public boolean emsSendDataValidate(List<Map<String, Object>> list, Map<String, Object> orderMap, List<Map<String, Object>> errList, boolean ktnet) throws Exception{
		//TODO 화면 개발내용 확인해서 검증 추가 여부 체크
		String hblNo = null, hsn = null, tmp = null;
		boolean chkHbl = false;
		for(Map<String, Object> map : list){
			if(Util.null2str(map.get("HBL_NO")).equals(hblNo)){
				chkHbl = false;
			}else{
				chkHbl = true;
			}
			
			if(ktnet){
				if("01302001".equals(map.get("ASSIGNMENT_WH_CODE"))){
					map.put("CARRYIN_CODE", "10");
				}else {
					map.put("CARRYIN_CODE", "  ");
				}
			}else{
				if("01302001".equals(map.get("ASSIGNMENT_WH_CODE"))){
					map.put("CARRYIN_CODE", "15");
				}else if("01302004".equals(map.get("ASSIGNMENT_WH_CODE"))){
					map.put("CARRYIN_CODE", "20");
				}else {
					map.put("CARRYIN_CODE", "  ");
				}
			}
			
			if(chkHbl){
				hblNo = Util.null2str(map.get("HBL_NO"));
				hsn = Util.null2str(map.get("HSN"));
				
				/** 4.연계업체 상품주문번호 */
				if(Util.isEmpty(map.get("PO_NO"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 연계업체 상품주문번호를 입력하십시오.", hblNo, hsn));
				}
				/** 6.발송인 우편번호 */
				if(Util.isEmpty(map.get("SHIPPER_ZIPCODE")) || !Util.isNumber(Util.null2str(map.get("SHIPPER_ZIPCODE")))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 발송인 우편번호를 숫자로 입력하십시오.", hblNo, hsn));
				}
				/** 7,8,74.발송인 주소 */
				if(Util.isEmpty(map.get("SHIPPER_ADDRESS_A")) || Util.isEmpty(map.get("SHIPPER_ADDRESS_B")) || Util.isEmpty(map.get("SHIPPER_ADDRESS_C"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 발송인 주소를 입력하십시오.", hblNo, hsn));
				}
				/** 9.발송인명 */
				if(Util.isEmpty(map.get("SHIPPER_NAME"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 발송인명을 입력하십시오.", hblNo, hsn));
				}
				/** 10~13.발송인전화번호 */
				if(Util.isEmpty(map.get("SHIPPER_TEL_A")) || Util.isEmpty(map.get("SHIPPER_TEL_B")) || Util.isEmpty(map.get("SHIPPER_TEL_C")) || Util.isEmpty(map.get("SHIPPER_TEL_D"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 발송인 전화번호를 입력하십시오.", hblNo, hsn));
				}
				if(!Util.isNumber(Util.null2str(map.get("SHIPPER_TEL_A")) + Util.null2str(map.get("SHIPPER_TEL_B")) + Util.null2str(map.get("SHIPPER_TEL_C")) + Util.null2str(map.get("SHIPPER_TEL_D")))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 발송인 전화번호를 숫자로 입력하십시오.", hblNo, hsn));
				}
				/** 14~16.발송인휴대전화 */
//				if(!Util.isEmpty(map.get("AAAAA")) || !Util.isEmpty(map.get("AAAAA")) || !Util.isEmpty(map.get("AAAAA"))){
//					if(!Util.isNumber(Util.null2str(map.get("AAAAA")) + Util.null2str(map.get("AAAAA")) + Util.null2str(map.get("AAAAA")))){
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 발송인휴대전화를 숫자로 입력하십시오.", hblNo, hsn));
//					}
//				}
				/** 18.수취인명 */
				tmp = Util.null2str(map.get("CONSIGNEE_NAME"));
				if(tmp.equals("")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인명을 입력하십시오.", hblNo, hsn));
				}else{
					if("P".equals(map.get("MAIL_TYPE")) && "US".equals(map.get("ORIG_COUNTRY"))){
						if(tmp.length() > 35){
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 미국행 및 EMS프리미엄인 경우 수취인명은 35자까지 입력 가능합니다.", hblNo, hsn));
						}
					}else{
						if(tmp.length() > 95){
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인명은 95자까지 입력 가능합니다.", hblNo, hsn));
						}
					}
				}
				/** 20~23,49.수취인전화번호 */
				if((Util.isEmpty(map.get("CONSIGNEE_TEL_A")) || Util.isEmpty(map.get("CONSIGNEE_TEL_B")) || Util.isEmpty(map.get("CONSIGNEE_TEL_C")) || Util.isEmpty(map.get("CONSIGNEE_TEL_D"))) && Util.isEmpty(map.get("CONSIGNEE_TEL"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인전화번호를 입력하십시오.", hblNo, hsn));
				}
				/** 24.국제우편물종류코드 */
				if(!Util.null2str(map.get("INTER_EMAIL_TYPE")).matches("300|301|311|312|104|111|112|113")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 국제우편물종류코드는 300 301 311 312 104 111 112 113만 입력 가능합니다.", hblNo, hsn));
				}
				/** 25.수취인국가코드 */
				if(Util.isEmpty(map.get("DEST_COUNTRY"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인국가코드를 입력하십시오.", hblNo, hsn));
				}
				/** 27.수취인우편번호 */
				if(Util.isEmpty(map.get("CONSIGNEE_ZIPCODE"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인우편번호를 입력하십시오.", hblNo, hsn));
				}
				/** 28~30.수취인주소 */
				if(Util.isEmpty(map.get("CONSIGNEE_ADDRESS_A")) || Util.isEmpty(map.get("CONSIGNEE_ADDRESS_B")) || Util.isEmpty(map.get("CONSIGNEE_ADDRESS_C"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인주소를 입력하십시오.", hblNo, hsn));
				}
				tmp = Util.null2str(map.get("CONSIGNEE_ADDRESS_A")) + Util.null2str(map.get("CONSIGNEE_ADDRESS_B")) + Util.null2str(map.get("CONSIGNEE_ADDRESS_C"));
				if(!tmp.matches("^[a-zA-Z0-9\\s\\,\\.\\/\\-\\_\\(\\)\\#\\~\\:\\;\\&\\<\\>\\@]*$")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인주소는 영문, 숫자, 특수문자(, . / - _ ( ) # ~ : ; & < > @)만 입력 가능합니다.", hblNo, hsn));
				}
			}
			
			/** 31.총중량 (g) */
			map.put("GWT", this.dblCheck(Util.null2dbl(map.get("GWT")), 7, "총중량을", errList, hblNo, hsn));
			/** 33.개수 */
			map.put("PIECES", this.dblCheck(Util.null2dbl(map.get("PIECES")), 7, "개수를", errList, hblNo, hsn));
			/** 34.순중량 (g) */
			map.put("ITEM_GWT", this.dblCheck(Util.null2dbl(map.get("ITEM_GWT")), 10, "순중량을", errList, hblNo, hsn));
			/** 35.가격 ($) */
			map.put("AMOUNT", this.dblCheck2(Util.null2dbl(map.get("AMOUNT")), 9999.99, "가격을", errList, hblNo, hsn, !"300".equals(map.get("INTER_EMAIL_TYPE"))));
			/** 36.HSCODE */
			tmp = Util.null2str(map.get("HS_CODE"));
			if("".equals(tmp)){
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : HSCODE를 입력하십시오.", hblNo, hsn));
			}else{
				if(!Util.isNumber(tmp)){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : HSCODE는 숫자로 입력하십시오.", hblNo, hsn));
				}
				if(tmp.length() > 10 || tmp.length() < 6){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : HSCODE는 6~10자리로 입력하십시오.", hblNo, hsn));
				}
				if("300".equals(map.get("INTER_EMAIL_TYPE")) && !tmp.startsWith("49")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : HSCODE는 내용품 유형이 서류일 경우 49로 시작해야 합니다.", hblNo, hsn));
				}
			}
			/** 37.생산지 */
			tmp = Util.null2str(map.get("ORIGIN_COUNTRY"));
			if("".equals(tmp)){
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 생산지를 입력하십시오.", hblNo, hsn));
			}
//			else{
//				if(tmp.length() != 2){
//					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 생산지는 2자리로 입력하십시오.", hblNo, hsn));
//				}
//				if(!tmp.matches("^[a-zA-Z]*$")){
//					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 생산지는 영문만 입력 가능합니다.", hblNo, hsn));
//				}
//			}
			if(chkHbl){
				/** 38.상품구분 */
				if(!Util.null2str(map.get("ITEM_TYPE")).matches("1|31|32|91")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 상품구분은 1 31 32 91만 입력 가능합니다.", hblNo, hsn));
				}
				/** 39.보험가입여부 */
				if(!Util.null2str(map.get("INSURANCE_YN")).matches("Y|N")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 보험가입여부는 Y N만 입력 가능합니다.", hblNo, hsn));
				}
				/** 41.우편물구분 */
				if(!Util.null2str(map.get("MAIL_TYPE")).matches("P|E|K")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 우편물구분은 P E K만 입력 가능합니다.", hblNo, hsn));
				}
//				/** 43.연계업체구분코드 */
//				if(Util.isEmpty(map.get("AAAAA"))){
//					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 연계업체구분코드를 입력하십시오.", hblNo, hsn));
//				}
				/** 44.승인번호 */
				if(Util.isEmpty(map.get("EMS_APPROVAL_NO"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 승인번호를 입력하십시오.", hblNo, hsn));
				}
				/** 45.환적계약구분코드 */
				if(Util.isEmpty(map.get("CONTRACT_CODE"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 환적계약구분코드를 입력하십시오.", hblNo, hsn));
				}
				/** 46.등기번호(EMS번호) */
				if(hblNo.equals("")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 등기번호를 입력하십시오.", hblNo, hsn));
				}else if(hblNo.length() > 13){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 등기번호를 13자리 이하로 입력하십시오.", hblNo, hsn));
				}
				/** 47.화물관리번호 */
				if(Util.isEmpty(map.get("MRN")) || Util.isEmpty(map.get("MSN")) || Util.isEmpty(map.get("HSN"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 화물관리번호를 입력하십시오.", hblNo, hsn));
				}
				
				/** 48번부터 KTNET과 KLNET스펙 순번이 다름 */
				
				/** 48.원산지부호 */
				if(Util.isEmpty(map.get("ORIG_COUNTRY"))){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 원산지부호를 입력하십시오.", hblNo, hsn));
				}
				/** 50.묶음번호 */
				if(Util.isEmpty(map.get("EMS_BUNDLE_NO")) && ("113".equals(map.get("INTER_EMAIL_TYPE")) || "15".equals(map.get("CARRYIN_CODE")) || "20".equals(map.get("CARRYIN_CODE")))){
					/** 묶음번호 필수조건일때 값이 없으면 MBL_NO로 묶음번호 사용 */
//					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 묶음번호를 입력하십시오.", hblNo, hsn));
					map.put("EMS_BUNDLE_NO", map.get("MBL_NO"));
				}
//				if(!Util.isEmpty(map.get("BBBBB"))){
//					/** 56.전량분할발송여부1 */
//					if(!Util.null2str(map.get("AAAAA")).matches("Y|N")){
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수출신고번호1 작성 시 전량분할발송여부1은 Y N만 입력 가능합니다.", hblNo, hsn));
//					}
//					/** 60.선기적포장개수1 */
//					if(Util.isEmpty(map.get("AAAAA"))){
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수출신고번호1 작성 시 선기적포장개수1을 입력하십시오.", hblNo, hsn));
//					}
//				}
//				if(!Util.isEmpty(map.get("BBBBB"))){
//					/** 57.전량분할발송여부2 */
//					if(!Util.null2str(map.get("AAAAA")).matches("Y|N")){
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수출신고번호2 작성 시 전량분할발송여부2는 Y N만 입력 가능합니다.", hblNo, hsn));
//					}
//					/** 61.선기적포장개수2 */
//					if(Util.isEmpty(map.get("AAAAA"))){
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수출신고번호2 작성 시 선기적포장개수2를 입력하십시오.", hblNo, hsn));
//					}
//				}
//				if(!Util.isEmpty(map.get("BBBBB"))){
//					/** 58.전량분할발송여부3 */
//					if(!Util.null2str(map.get("AAAAA")).matches("Y|N")){
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수출신고번호3 작성 시 전량분할발송여부3은 Y N만 입력 가능합니다.", hblNo, hsn));
//					}
//					/** 62.선기적포장개수3 */
//					if(Util.isEmpty(map.get("AAAAA"))){
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수출신고번호3 작성 시 선기적포장개수3을 입력하십시오.", hblNo, hsn));
//					}
//				}
//				if(!Util.isEmpty(map.get("BBBBB"))){
//					/** 59.전량분할발송여부4 */
//					if(!Util.null2str(map.get("AAAAA")).matches("Y|N")){
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수출신고번호4 작성 시 전량분할발송여부4는 Y N만 입력 가능합니다.", hblNo, hsn));
//					}
//					/** 63.선기적포장개수4 */
//					if(Util.isEmpty(map.get("AAAAA"))){
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수출신고번호4 작성 시 선기적포장개수4를 입력하십시오.", hblNo, hsn));
//					}
//				}
				/** 67.세관신고서 비고 */
				if("BR".equals(map.get("ORIG_COUNTRY"))){
					tmp = Util.null2str(map.get("VAT_IDT_NO"));	/** 브라질 세금식별번호는 부가세식별번호 사용함 */
					if(!Util.isNumber(tmp) || (tmp.length() != 11 && tmp.length() != 14)){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인국가코드가 BR일 경우 부가세식별번호를 CPF(개인):숫자만11자리 / CNPJ(기업):숫자만14자리로 입력하십시오.", hblNo, hsn));
					}else{
						map.put("TAX_IDT_NO", tmp);
					}
				}
				/** 68.우편물(포장상자) 세로길이 */
				map.put("DIM_LENGTH", this.dblCheck2(Util.null2dbl(map.get("DIM_LENGTH")), 9999.99, "우편물 세로길이를", errList, hblNo, hsn, true));
				/** 69.우편물(포장상자) 가로길이  */
				map.put("DIM_WIDTH", this.dblCheck2(Util.null2dbl(map.get("DIM_WIDTH")), 9999.99, "우편물 가로길이를", errList, hblNo, hsn, true));
				/** 70.우편물(포장상자) 높이 */
				map.put("DIM_HEIGHT", this.dblCheck2(Util.null2dbl(map.get("DIM_HEIGHT")), 9999.99, "우편물 높이를", errList, hblNo, hsn, true));
				/** 72.부가세식별번호 */
				tmp = Util.null2str(map.get("VAT_IDT_NO"));
				if(!tmp.equals("")){
					if("210".equals(map.get("ORIG_AREA_CODE")) && (tmp.length() != 12 || !tmp.startsWith("IM") || !Util.isNumber(tmp.substring(2)))){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 유럽행의 경우 'IM' + 숫자10자리 형태 총 12자리로 부가세식별번호를 입력하십시오.", hblNo, hsn));
					}else if("GB".equals(map.get("ORIG_COUNTRY")) && (tmp.length() != 14 || !tmp.startsWith("GB") || !Util.isNumber(tmp.substring(2)))){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 영국행의 경우 'GB' + 숫자9자리 + '000' 형태 총 14자리로 부가세식별번호를 입력하십시오.", hblNo, hsn));
					}
				}
				/** 73.통화단위 */
				tmp = Util.null2str(map.get("CURRENCY_CODE"));
				if(tmp.equals("")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 통화단위를 입력하십시오.", hblNo, hsn));
				}else if("210".equals(map.get("ORIG_AREA_CODE")) && !"USD".equals(tmp) && !"EUR".equals(tmp)){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 유럽행의 경우 통화단위는 USD EUR만 입력 가능합니다.", hblNo, hsn));
				}else if(!"USD".equals(tmp)){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 유럽외 국가행의 통화단위는 USD만 입력 가능합니다.", hblNo, hsn));
				}
			}
			
			
			if(ktnet){
				if(chkHbl){
					/** 3.우체국 고객번호 */
					if(Util.isEmpty(orderMap.get("EMS_CUS_NO_AIR_IPO"))){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 우체국 고객번호를 입력하십시오.", hblNo, hsn));
					}else{
						orderMap.put("EMS_CUS_NO", orderMap.get("EMS_CUS_NO_AIR_IPO"));
					}
					/** 7,8,74.발송인 주소 */
					tmp = Util.null2str(map.get("SHIPPER_ADDRESS_A")) + Util.null2str(map.get("SHIPPER_ADDRESS_B")) + Util.null2str(map.get("SHIPPER_ADDRESS_C"));
					if(!tmp.equals(tmp.replaceAll("'|\\+|:|\\?|\\|", ""))){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 발송인 주소에 특수문자(' + : ? |)를 입력할 수 없습니다.", hblNo, hsn));
					}
					/** 9.발송인명 */
					tmp = Util.null2str(map.get("SHIPPER_NAME"));
					if(!tmp.equals(tmp.replaceAll("'|\\+|:|\\?|\\|", ""))){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 발송인명에 특수문자(' + : ? |)를 입력할 수 없습니다.", hblNo, hsn));
					}
					/** 17.발송인 이메일 */
					tmp = Util.null2str(map.get("SHIPPER_EMAIL"));
					if(!tmp.equals("")){
						if(!tmp.equals(tmp.replaceAll("'|\\+|:|\\?|\\|", ""))){
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 발송인 이메일에 특수문자(' + : ? |)를 입력할 수 없습니다.", hblNo, hsn));
						}
					}
					/** 18.수취인명 */
					tmp = Util.null2str(map.get("CONSIGNEE_NAME"));
					if("P".equals(map.get("MAIL_TYPE")) && "US".equals(map.get("DEST_COUNTRY"))){
						if(!tmp.matches("^[a-zA-Z\\s]*$")){
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 미국행 및 EMS프리미엄인 경우 수취인명은 영문만 입력 가능합니다.", hblNo, hsn));
						}
					}
					if(Util.isNumber(tmp)){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인명은 숫자만 입력할 수 없습니다.", hblNo, hsn));
					}
					if(!tmp.equals(tmp.replaceAll("'|\\+|:|\\?|\\|", ""))){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인명에 특수문자(' + : ? |)를 입력할 수 없습니다.", hblNo, hsn));
					}
					/** 19.수취인 이메일 */
					tmp = Util.null2str(map.get("CONSIGNEE_EMAIL"));
					if(!tmp.equals("")){
						if(!tmp.equals(tmp.replaceAll("'|\\+|:|\\?|\\|", ""))){
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수취인 이메일에 특수문자(' + : ? |)를 입력할 수 없습니다.", hblNo, hsn));
						}
					}
					/** 72.부가세식별번호 */
					if(Util.isEmpty(map.get("VAT_IDT_NO"))){
						if("NO".equals(map.get("ORIG_COUNTRY"))){
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 노르웨이는 VOEC번호를 부가세식별번호에 입력하십시오.", hblNo, hsn));
						}else if("MY".equals(map.get("ORIG_COUNTRY"))){
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 말레이시아는 LVG번호를 부가세식별번호에 입력하십시오.", hblNo, hsn));
						}else if("P".equals(map.get("MAIL_TYPE")) && "301".equals(map.get("INTER_EMAIL_TYPE")) && "MX".equals(map.get("ORIG_COUNTRY"))){
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : EMS프리미엄/비서류 멕시코행의 경우 부가세식별번호를 입력하십시오.", hblNo, hsn));
						}
					}
				}
				/** 32.내용품명 */
				if(!Util.isEmpty(map.get("DETAIL_ITEM_NAME")) && !Util.null2str(map.get("DETAIL_ITEM_NAME")).matches("^[a-zA-Z0-9\\s\\,\\.\\/\\-\\_\\(\\)\\#\\~\\;\\&\\<\\>\\@]*$")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 내용품명은 영문, 숫자, 특수문자(, . / - _ ( ) # ~ ; & < > @)만 입력 가능합니다.", hblNo, hsn));
				}
			}else{
				if(chkHbl){
					/** 3.우체국 고객번호 */
					if(hblNo.startsWith("EG") || hblNo.startsWith("EZ") || hblNo.startsWith("EW")){
						if("01302004".equals(map.get("ASSIGNMENT_WH_CODE"))){
							orderMap.put("EMS_CUS_NO", orderMap.get("EMS_CUS_NO_SEA"));
						}else{
							orderMap.put("EMS_CUS_NO", orderMap.get("EMS_CUS_NO_SEA_IPO"));
						}
					}else{
						orderMap.put("EMS_CUS_NO", orderMap.get("EMS_CUS_NO_SEA_ALIBABA"));
					}
					if(Util.isEmpty(orderMap.get("EMS_CUS_NO"))){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 우체국 고객번호를 입력하십시오.", hblNo, hsn));
					}
					/** 9.발송인명 */
					tmp = Util.null2str(map.get("SHIPPER_NAME"));
					if(tmp.length() > 35){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 발송인명은 영문35자까지 입력 가능합니다.", hblNo, hsn));
					}
					/** 65.반입근거번호 */
					if(Util.isEmpty(map.get("CARRYIN_NO")) && "01302004".equals(map.get("ASSIGNMENT_WH_CODE"))){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 보세창고가 01302004일 경우 반입근거번호를 입력하십시오.", hblNo, hsn));
					}
				}
				/** 32.내용품명 */
				if(!Util.isEmpty(map.get("DETAIL_ITEM_NAME")) && !Util.null2str(map.get("DETAIL_ITEM_NAME")).matches("^[a-zA-Z0-9\\s\\,\\.\\/\\-\\_\\(\\)\\#\\~\\:\\;\\&\\<\\>\\@]*$")){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 내용품명은 영문, 숫자, 특수문자(, . / - _ ( ) # ~ : ; & < > @)만 입력 가능합니다.", hblNo, hsn));
				}
			}
		}
		
		if(errList.size() > 0){
			return false;
		}
		return true;
	}
	
	public boolean createEms(List<Map<String, Object>> list, Map<String, Object> orderMap, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result, boolean ktnet) throws Exception{
		String fileName = new StringBuffer(DateTimeUtil.getDate("yyyyMMddHHmmss") + "_" + Util.null2str(orderMap.get("userId"))).append(".txt").toString();	// 전송 파일명
		String[] dirPath = ediCommonService.getSendFilePath(Util.null2str(orderMap.get("ediVender")), "edi/ems", orderMap);	// 로컬폴더 경로 가져오기
		String mrn = Util.null2str(orderMap.get("MRN"));
		String forwarderSign = Util.getFileWriteData(Util.null2str(orderMap.get("B2G_FORWARDER_SIGN")), "String", "25");
		String now = DateTimeUtil.getDate("yyyyMMdd:HHmm");
		String entCode = Util.null2str(orderMap.get("entCode"));
		String seqNo = Util.getFileWriteData(DateTimeUtil.getDate("yyMMddHHmmssSSS") + entCode, "String", "50");
		String sendType = Util.null2str(orderMap.get("action"));
		String emsApprovalNo = (Util.getFileWriteData(Util.null2str(orderMap.get("EMS_APPROVAL_NO")), "String", "10"));
		String tmp;
		int singleQuote = 2;
		BufferedWriter bfw = null;
		StringBuffer writeData = new StringBuffer();
		try {
			bfw = ediCommonService.getFileWriterOpen(fileName, dirPath);
			
			if(ktnet){
				writeData.append("UNB+KECA:4+");
				writeData.append(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim());
				writeData.append(":57+");
				writeData.append(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim());
				writeData.append(":57+");
				writeData.append(now);
				/** KTNET샘플에 +001'UNH+로 되어있어서 동일하게 처리 */
//				writeData.append("+01");
//				writeData.append(mblNo);
				writeData.append("+001");
				writeData.append("'UNH+1+AIREMS:11'");
			}else{
				writeData.append("UNB+KECA:1+");
	            writeData.append(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim());
				writeData.append(":KL+");
	            writeData.append(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim());
				writeData.append(":KL+");
	            writeData.append(now);
	            writeData.append("+");
	            writeData.append(mrn);
	            writeData.append("'UNH+1+POTSAP:1'");
			}
			bfw.write(writeData.toString());
			
			now = now.substring(0, 8);
			for(Map<String, Object> map : list){
				writeData.setLength(0);
				if(!ktnet){
					writeData.append("PTAP");	/** KLNET일경우 'PTAP'로 시작 */
				}
				writeData.append(now);	/** 1.전송일자 */
				writeData.append(forwarderSign);	/** 2.연계업체 고객번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("EMS_CUS_NO")), "String", "10"));	/** 3.우체국 고객번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("PO_NO")), "String", "50"));	/** 4.연계업체 상품주문번호 */
				writeData.append(seqNo);	/** 5.연계업체 고유일련번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("SHIPPER_ZIPCODE")), "String", "6"));	/** 6.발송인 우편번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("SHIPPER_ADDRESS_A")), "String", "200"));	/** 7.발송인 주소1(상세) */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("SHIPPER_ADDRESS_B")), "String", "50"));	/** 8.발송인 주소2(시/군/구) */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("SHIPPER_NAME")), "String", this.setEmsLen(ktnet, "35", "40")));	/** 9.발송인명 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("SHIPPER_TEL_A")), "String", "4"));	/** 10.발송인전화국가번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("SHIPPER_TEL_B")), "String", "4"));	/** 11.발송인전화지역번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("SHIPPER_TEL_C")), "String", "4"));	/** 12.발송인전화국번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("SHIPPER_TEL_D")), "String", "4"));	/** 13.발송인 전화번호 */
				writeData.append("    ");	/** 14.발송인휴대전화1 */
				writeData.append("    ");	/** 15.발송인휴대전화2 */
				writeData.append("    ");	/** 16.발송인휴대전화3 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("SHIPPER_EMAIL")), "String", "40"));	/** 17.발송인 이메일 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_NAME")), "String", this.setEmsLen(ktnet, "95", "100")));	/** 18.수취인명 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_EMAIL")), "String", "60"));	/** 19.수취인 이메일 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_TEL_A")), "String", "4"));	/** 20.수취인전화국가번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_TEL_B")), "String", "4"));	/** 21.수취인전화지역번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_TEL_C")), "String", "4"));	/** 22.수취인전화국번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_TEL_D")), "String", "4"));	/** 23.수취인전화번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("INTER_EMAIL_TYPE")), "String", "3"));	/** 24.국제우편물종류코드 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("DEST_COUNTRY")), "String", "2"));	/** 25.수취인국가코드 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("DEST_COUNTRY_NAME")), "String", "40"));	/** 26.수취인국가명 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_ZIPCODE")), "String", "20"));	/** 27.수취인우편번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_ADDRESS_C")), "String", "50"));	/** 28.수취인주소1(주/도) */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_ADDRESS_B")), "String", "50"));	/** 29.수취인주소2(시/군) */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_ADDRESS_A")), "String", "200"));	/** 30.수취인주소3(상세) */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("GWT")), "String", "7"));	/** 31.총중량(g) */
				/** TB_ECM_OD_ITEM 시작 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("DETAIL_ITEM_NAME")), "String", "70"));	/** 32.내용품명 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("PIECES")), "String", "7"));	/** 33.개수 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("ITEM_GWT")), "String", "10"));	/** 34.순중량(g) */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("AMOUNT")), "String", this.setEmsLen(ktnet, "7", "12")));	/** 35.가격($) */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("HS_CODE")), "String", "20"));	/** 36.HSCODE */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("ORIGIN_COUNTRY")), "String", "20"));	/** 37.생산지 */
				/** TB_ECM_OD_ITEM 끝 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("ITEM_TYPE")), "String", "2"));	/** 38.상품구분 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("INSURANCE_YN")), "String", "1"));	/** 39.보험가입여부 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("INSURANCE_AMOUNT")), "String", "15"));	/** 40.보험가입금액($) */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("MAIL_TYPE")), "String", "1"));	/** 41.우편물구분 */
				if(Util.null2int(map.get("ITEM_CNT")) > 1){
					writeData.append(Util.getFileWriteData(Util.null2str(map.get("HBL_NO")), "String", "20"));	/** 42.합포장키 */
				}else{
					writeData.append("N                   ");	/** 42.합포장키 */
				}
				if(ktnet){
					writeData.append("0016      ");	/** 43.연계업체구분코드 */
				}else{
					writeData.append("0042      ");	/** 43.연계업체구분코드 */
				}
				writeData.append(emsApprovalNo);	/** 44.승인번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONTRACT_CODE")), "String", "2"));	/** 45.환적계약구분코드 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("HBL_NO")), "String", "13"));	/** 46.등기번호(EMS번호) */
				writeData.append(Util.getFileWriteData(mrn + Util.null2str(map.get("MSN")) + Util.null2str(map.get("HSN")), "String", "19"));	/** 47.화물관리번호 */
				if(!ktnet){
					/** 48번부터 KTNET과 KLNET스펙 순번이 다름 */
					writeData.append(Util.getFileWriteData(Util.null2str(map.get("AR_DATE")), "String", "8"));	/** 48.입항일자 */					
				}
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("ORIG_COUNTRY")), "String", "2"));	/** 48.원산지부호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CONSIGNEE_TEL")), "String", "40"));	/** 49.수취인 전체 전화번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("EMS_BUNDLE_NO")), "String", "30"));	/** 50.묶음번호 */
				writeData.append(" ");	/** 51.수출이행등록여부 */
				writeData.append("               ");	/** 52.수출신고번호1 */
				writeData.append("               ");	/** 53.수출신고번호2 */
				writeData.append("               ");	/** 54.수출신고번호3 */
				writeData.append("               ");	/** 55.수출신고번호4 */
				writeData.append(" ");	/** 56.전량분할발송여부1 */
				writeData.append(" ");	/** 57.전량분할발송여부2 */
				writeData.append(" ");	/** 58.전량분할발송여부3 */
				writeData.append(" ");	/** 59.전량분할발송여부4 */
				writeData.append("     ");	/** 60.선기적포장개수1 */
				writeData.append("     ");	/** 61.선기적포장개수2 */
				writeData.append("     ");	/** 62.선기적포장개수3 */
				writeData.append("     ");	/** 63.선기적포장개수4 */
				writeData.append(Util.null2str(map.get("CARRYIN_CODE")));	/** 64.반입유형코드 */
				if("20".equals(map.get("CARRYIN_CODE"))){
					writeData.append(Util.getFileWriteData(Util.null2str(map.get("CARRYIN_NO")), "String", "20"));	/** 65.반입근거번호 */
				}else{
					writeData.append("                    ");	/** 65.반입근거번호 */
				}
				writeData.append(Util.getFileWriteData(sendType, "String", "1"));	/** 66.전송구분 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("TAX_IDT_NO")), "String", "100"));	/** 67.세관신고서 비고 */
				/** KLNET스펙  'SEA EXPRESS는 9자리'     KLNET샘플파일 받은내용엔 7자리로 되어있어서 7자리로 처리함  */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("DIM_LENGTH")), "String", "7"));	/** 68.우편물(포장상자) 세로길이 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("DIM_WIDTH")), "String", "7"));	/** 69.우편물(포장상자) 가로길이 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("DIM_HEIGHT")), "String", "7"));	/** 70.우편물(포장상자) 높이 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("VOLUMN_EXCLUDE_YN")), "String", "1"));	/** 71.부피중량 적용제외 신청 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("VAT_IDT_NO")), "String", this.setEmsLen(ktnet, "25", "50")));	/** 72.부가세식별번호 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("CURRENCY_CODE")), "String", "3"));	/** 73.통화단위 */
				writeData.append(Util.getFileWriteData(Util.null2str(map.get("SHIPPER_ADDRESS_C")), "String", "50"));	/** 74.발송인 주소3(도/시) */
				writeData.append("'");
				bfw.write(writeData.toString());
				singleQuote++;
			}
			writeData.setLength(0);
			writeData.append("UNT+");
			writeData.append(singleQuote);
			if(ktnet){
				/** KTNET샘플에 +001'로 되어있어서 동일하게 처리 */
//				writeData.append("+1'UNZ+1+01");
//				writeData.append(mblNo);
				writeData.append("+1'UNZ+1+001");
			}else{
				writeData.append("+1'UNZ+1+");
				writeData.append(mrn);
			}
			writeData.append("'");
			bfw.write(writeData.toString());
		}catch(Exception e) {
			throw e;
		}finally {
			if (bfw != null) {
				try{ bfw.close(); }catch(Exception e){log.error(Util.getPrintStacTraceString(e));}
			}
		}
		
		String fileCreateRetVal[] = new String[4];
		fileCreateRetVal[0] = fileName;
		fileCreateRetVal[1] = "";
		fileCreateRetVal[2] = dirPath[0];
		fileCreateRetVal[3] = dirPath[1];
		
		String fileSendYn = "N";
		if ("Y".equals(customProperties.getEdiSendYn())){ 
			if(ediCommonService.ftpFileSend(orderMap, orderMap, fileCreateRetVal, ediHostSendInfo, errList, result)) {
				fileSendYn = "Y";
			}
		}else{
			fileSendYn = "Y";
		}
		
		String outFileName = ediCommonService.moveFile(fileCreateRetVal[0], fileCreateRetVal[0], fileCreateRetVal);
		
		if(fileSendYn.equals("N")){
			return false;
		}
		
		/** 처리 로그 기록 */
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		
		tmpMap.clear();
		tmpMap.put("ent_code", orderMap.get("entCode"));
		tmpMap.put("customer_code", "");
		tmpMap.put("solution", "ECM");
		tmpMap.put("bound", orderMap.get("BOUND"));
		tmpMap.put("bound_type", "IMP");
		tmpMap.put("hbl_no", "");
		tmpMap.put("if_service_code", ediHostSendInfo.get("IF_SERVICE_CODE"));
		tmpMap.put("if_doc_code", "EMS_TS");
		tmpMap.put("if_doc_name", "EMS환적");
		tmpMap.put("if_doc_reference_no", orderMap.get("MBL_NO"));
		tmpMap.put("if_vender", orderMap.get("ediVender"));
		tmpMap.put("if_send_recv_type", "S");
		tmpMap.put("if_send_recv_method", ediHostSendInfo.get("IF_S_R_TYPE"));
		tmpMap.put("if_file_path", fileCreateRetVal[3]);
		tmpMap.put("if_file_name", outFileName);
		tmpMap.put("if_status", "SEND");
		tmpMap.put("if_status_info", "");
		tmpMap.put("if_sender_id", ediHostSendInfo.get("IF_SENDER_ID"));
		tmpMap.put("if_receiver_id", ediHostSendInfo.get("IF_RECEIVER_ID"));
		tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
		tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
		tmpMap.put("if_remark", "");
		tmpMap.put("register_user_id", orderMap.get("userId"));
		tmpMap.put("register_user_name", orderMap.get("userName"));
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", logId);
			
		IndexApi indexApi = new IndexApi(client);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		
		DocumentApi documentApi = new DocumentApi(client);
		documentApi.createDocument(index, logId, tmpMap);
		
		return true;
	}
	public String setEmsLen(boolean ktnet, String ktnetLen, String klnetLen){
		if(ktnet){
			return ktnetLen;
		}else{
			return klnetLen;
		}
	}
	public String dblCheck(double dbl, int len, String desc, List<Map<String, Object>> errList, String hblNo, String hsn) throws Exception{
		String tmp = "0";
		if(dbl == 0){
			errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + desc + " 입력하십시오.", hblNo, hsn));
		}else{
			tmp = Util.null2str(dbl);
			if(tmp.endsWith(".0")){
				tmp = tmp.replace(".0", "");
			}
			if(tmp.length() > len){
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + desc + " " + len + "자리 이하로 입력하십시오.", hblNo, hsn));
			}
		}
		return tmp;
	}
	public String dblCheck2(double dbl, double max, String desc, List<Map<String, Object>> errList, String hblNo, String hsn, boolean condition) throws Exception{
		String tmp = "0";
		if(dbl == 0 && condition){
			errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + desc + " 입력하십시오.", hblNo, hsn));
		}else if(dbl > max){
			errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + desc + " " + max + "이하로 입력하십시오.", hblNo, hsn));
		}else{
			String[] tmpArr = Util.null2str(dbl).split("\\.");
			if(tmpArr.length > 1 && tmpArr[1].length() > 2){
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + desc + " 소수 2자리만 입력하십시오.", hblNo, hsn));
			}else{
				if(tmpArr.length > 1 && tmpArr[1].length() == 1){
					tmp = tmpArr[0] + "." + tmpArr[1] + "0";
				}else{
					tmp = Util.null2str(dbl);
				}
			}
		}
		return tmp;
	}
	
	/**
	 * 해운 적하목록신고 문서송신(4세대)
	 * @param sendManifest
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult sendManifestSea4th(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> docSend = new HashMap<String, Object>();
		
		/** 그리드 데이터 담기 */
		Map<String, Object> tempBlData = null;			// M.B/L 정보
		Map<String, Object> fwSendAccInfo = null;		// 사업장정보 가져오기( 포워더 송/수신 계정정보, TB_SMS_ENT_BUS_FMS )
		
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();	// 에러내역 리스트
		
		StringBuffer processMessage = new StringBuffer();
		String message = null;
		String ifServiceCode = null;
		
		// 조회 조건 세팅
		docSend.put("entCode", Util.null2str(sendManifest.getEntCode()));
		docSend.put("entBusinessCode", Util.null2str(sendManifest.getEntBusinessCode()));
		docSend.put("solution", Util.null2str(sendManifest.getSolution()));
		docSend.put("bound", Util.null2str(sendManifest.getBound()));
		docSend.put("boundType", Util.null2str(sendManifest.getBoundType()));
		docSend.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		docSend.put("registerUserId", Util.null2str(sendManifest.getRegisterUserId()));
		docSend.put("registerUserName", sendManifest.getRegisterUserName());
		docSend.put("updateUserId", Util.null2str(sendManifest.getUpdateUserId()));
		docSend.put("gmtTime", Util.null2str(sendManifest.getGmtTime()));
		docSend.put("contractDate", Util.null2str(sendManifest.getContractDate()));
		docSend.put("penaltyYn", Util.null2str(sendManifest.getPenaltyYn()));
		docSend.put("docType", "MANIFEST");
		docSend.put("cryptographyYn", sendManifest.getCryptographyYn());
        
		// 에러 메시지 가져오기 조건 세팅
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
//		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
//		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
//		tempSearchData.put("srchCode", "MF-60");	// 적하 목록 송신이 완료 되었습니다. 
//		tempSearchData.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
//		message = ediCommonService.getErrorCodeName(tempSearchData);
		
//		// 마스터 일경우
//		if ("M".equals(Util.null2str(sendManifest.getBlTypeMh()))){
//			docSend.put("odJobNo", Util.null2str(sendManifest.getOdJobNo())); // 마스터 Job번호
//			tempBlData = ediCommonService.getEdiMblOdInfo(docSend);
//		// 하우스 일경우
//		} else {
//			docSend.put("masterOdJobNo", Util.null2str(sendManifest.getMasterOdJobNo()));	// S/R 번호
//			// M.B/L 오더정보 조회
//			tempBlData = ediCommonService.getOrderEdiMblOdInfoForManifest(docSend);
//		}
		
		List<String> houseOdJobNoList = sendManifest.getHouseOdJobNoList();
		if(houseOdJobNoList != null && houseOdJobNoList.size() > 0){
			docSend.put("houseOdJobNoList", houseOdJobNoList);
		}
		docSend.put("odJobNo", Util.null2str(sendManifest.getMasterOdJobNo()));
		tempBlData = ediCommonService.getEdiMblOdInfo(docSend);
		
		if (tempBlData != null) {	// 실제 DB 데이터가 있는지 재확인
			if(!Util.null2str(tempBlData.get("EDI_VENDER")).equals("KLNET")){
				docSend.put("docType", "MANIFEST-SFTP");
			}
			
			processMessage.append("M.B/L No.");
			
			// 조회조건 세팅
			docSend.put("odJobNo", Util.null2str(tempBlData.get("OD_JOB_NO"))); // 마스터 Job번호
			docSend.put("masterOdJobNo", Util.null2str(tempBlData.get("MASTER_OD_JOB_NO")));
			docSend.put("mblNo", Util.null2str(tempBlData.get("MBL_NO")));
			docSend.put("action", sendManifest.getAction());
			
			// 서비스 코드 세팅
			ifServiceCode = ediCommonService.getServiceCode(Util.null2str(docSend.get("solution")), Util.null2str(docSend.get("bound")), Util.null2str(docSend.get("boundType")), Util.null2str(docSend.get("docType")), Util.null2str(tempBlData.get("EDI_VENDER")).trim());
			docSend.put("ifServiceCode", ifServiceCode);
			docSend.put("ediVender", Util.null2str(tempBlData.get("EDI_VENDER")).trim());
			docSend.put("mblNo", Util.null2str(tempBlData.get("MBL_NO")).trim());
			
			// 사업장정보 가져오기( 포워더 송/수신 계정정보, TB_SMS_ENT_BUS_FMS )
			fwSendAccInfo = ediCommonService.getFwSendAccInfo(docSend);
			
			if ("EXP".equals(Util.null2str(docSend.get("boundType")))) {
				if (!ediManifestSeaExpSendService.setManifestSeaExpSend4th(docSend, tempBlData, fwSendAccInfo, errList, result, request)) {
					if ("".equals(Util.null2str(result.get("processCode"))))	result.put("processCode", "A00001");
				}
				
			} else {
				if (!ediManifestSeaImpSendService.setManifestSeaImpSend4th(docSend, tempBlData, fwSendAccInfo, errList, result, request)) {
					if ("".equals(Util.null2str(result.get("processCode")))) {
						if(!Util.null2str(sendManifest.getPenaltyYn()).equals("Y")) {
							result.put("processCode", "A00001");
						}
					}
				}
			}
			
			processMessage.append(" : " + Util.null2str(Util.null2str(tempBlData.get("MBL_NO"))));
			
		}else{
			result.put("processCode", "0000");
			
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "M.B/L 자료가 존재하지 않습니다.");
			tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
			tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
			message = ediCommonService.getErrorCodeName(tempSearchData);
		}
		
		if(!"A00001".equals(Util.null2str(result.get("processCode")))){
			processMessage.append(" " + message);
		}
		
		if(!Util.null2str(message).equals("")){
			result.put("processMessage", processMessage.toString());
		}

//		return result;
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	
	/**
	 * 항공 적하목록신고 문서송신(4세대)
	 * @param sendManifest
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult sendManifestAir4th(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> docSend = new HashMap<String, Object>();
		
		Map<String, Object> tempBlData = null;			// M.B/L 정보
		Map<String, Object> fwSendAccInfo = null;		// 사업장정보 가져오기( 포워더 송/수신 계정정보, TB_SMS_ENT_BUS_FMS )
		
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();	// 에러내역 리스트
		
		StringBuffer processMessage = new StringBuffer();
		String message = null;
		String ifServiceCode = null;
		
		// 조회 조건 세팅
		docSend.put("entCode", Util.null2str(sendManifest.getEntCode()));
		docSend.put("entBusinessCode", Util.null2str(sendManifest.getEntBusinessCode()));
		docSend.put("solution", Util.null2str(sendManifest.getSolution()));
		docSend.put("bound", Util.null2str(sendManifest.getBound()));
		docSend.put("boundType", Util.null2str(sendManifest.getBoundType()));
		docSend.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		docSend.put("registerUserId", Util.null2str(sendManifest.getRegisterUserId()));
		docSend.put("registerUserName", Util.null2str(sendManifest.getRegisterUserName()));
		docSend.put("updateUserId", Util.null2str(sendManifest.getUpdateUserId()));
		docSend.put("contractDate", Util.null2str(sendManifest.getContractDate()));
		docSend.put("gmtTime", Util.null2str(sendManifest.getGmtTime()));
		docSend.put("penaltyYn", Util.null2str(sendManifest.getPenaltyYn()));
		docSend.put("docType", "MANIFEST");
		docSend.put("cryptographyYn", sendManifest.getCryptographyYn());
        
		// 에러 메시지 가져오기 조건 세팅
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
//		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
//		tempSearchData.put("srchCode", "MF-60");	// 적하 목록 송신이 완료 되었습니다.
//		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
//		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
//		message = ediCommonService.getErrorCodeName(tempSearchData);
		
//		// 마스터 일경우
//		if ("M".equals(Util.null2str(sendManifest.getBlTypeMh()))) {
//			docSend.put("odJobNo", Util.null2str(docSend.get("odJobNo"))); // 마스터 Job번호
//			tempBlData = ediCommonService.getEdiMblOdInfo(docSend);
//
//		// 하우스 일경우
//		} else {
//			docSend.put("masterOdJobNo", Util.null2str(Util.null2str(sendManifest.getMasterOdJobNo())));	// S/R 번호
//			// M.B/L 오더정보 조회
//			tempBlData = ediCommonService.getOrderEdiMblOdInfoForManifest(docSend);
//		}
		List<String> houseOdJobNoList = sendManifest.getHouseOdJobNoList();
		if(houseOdJobNoList != null && houseOdJobNoList.size() > 0){
			docSend.put("houseOdJobNoList", houseOdJobNoList);
		}
		docSend.put("odJobNo", Util.null2str(sendManifest.getMasterOdJobNo()));
		tempBlData = ediCommonService.getEdiMblOdInfo(docSend);
		
		if (tempBlData != null) {	// 실제 DB 데이터가 있는지 재확인
			processMessage.append("M.B/L No.");
			
			// 조회조건 세팅
			docSend.put("odJobNo", Util.null2str(tempBlData.get("OD_JOB_NO"))); // 마스터 Job번호
			docSend.put("masterOdJobNo", Util.null2str(tempBlData.get("MASTER_OD_JOB_NO")));
			docSend.put("mblNo", Util.null2str(tempBlData.get("MBL_NO")));
			docSend.put("action", sendManifest.getAction());
			
			// 서비스 코드 세팅
			ifServiceCode = ediCommonService.getServiceCode(Util.null2str(docSend.get("solution")), Util.null2str(docSend.get("bound")), Util.null2str(docSend.get("boundType")), Util.null2str(docSend.get("docType")), Util.null2str(tempBlData.get("EDI_VENDER")).trim());
			docSend.put("ifServiceCode", ifServiceCode);
			docSend.put("ediVender", Util.null2str(tempBlData.get("EDI_VENDER")).trim());
			docSend.put("mblNo", Util.null2str(tempBlData.get("MBL_NO")).trim());
			
			// 사업장정보 가져오기( 포워더 송/수신 계정정보, TB_SMS_ENT_BUS_FMS )
			fwSendAccInfo = ediCommonService.getFwSendAccInfo(docSend);
			
			if ("EXP".equals(Util.null2str(docSend.get("boundType")))) {
				// 항공수출 적하목록신고 문서송신
				if (!ediManifestAirExpSendService.setManifestAirExpSend4th(docSend, tempBlData, fwSendAccInfo, errList, result, request)) {
					if ("".equals(Util.null2str(result.get("processCode"))))	result.put("processCode", "A00001");
				}
				
			} else {
				// 항공수입 적하목록신고 문서송신
				if (!ediManifestAirImpSendService.setManifestAirImpSend4th(docSend, tempBlData, fwSendAccInfo, errList, result, request)) {
					if ("".equals(Util.null2str(result.get("processCode")))) {
						if(!Util.null2str(sendManifest.getPenaltyYn()).equals("Y")) {
							result.put("processCode", "A00001");
						}
					}
				}
			}
			
			
			
			processMessage.append(" : " + Util.null2str(Util.null2str(tempBlData.get("MBL_NO"))));
			
		} else {
			result.put("processCode", "0000");
			
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "M.B/L 자료가 존재하지 않습니다.");
			tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
			tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
			message = ediCommonService.getErrorCodeName(tempSearchData);
		}
		
		processMessage.append(" " + message);
		
		if(!Util.null2str(message).equals("")){
			result.put("processMessage", processMessage.toString());
		}
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	
	/**
	 * 항공 적하목록신고검증(4세대)
	 * @param sendManifest
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult sendManifestAir4thVali(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> docSend = new HashMap<String, Object>();
		
		Map<String, Object> tempBlData = null;			// M.B/L 정보
		Map<String, Object> fwSendAccInfo = null;		// 사업장정보 가져오기( 포워더 송/수신 계정정보, TB_SMS_ENT_BUS_FMS )
		
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();	// 에러내역 리스트
		
		StringBuffer processMessage = new StringBuffer();
		String message = null;
		String ifServiceCode = null;
		
		// 조회 조건 세팅
		docSend.put("entCode", Util.null2str(sendManifest.getEntCode()));
		docSend.put("entBusinessCode", Util.null2str(sendManifest.getEntBusinessCode()));
		docSend.put("solution", Util.null2str(sendManifest.getSolution()));
		docSend.put("bound", Util.null2str(sendManifest.getBound()));
		docSend.put("boundType", Util.null2str(sendManifest.getBoundType()));
		docSend.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		docSend.put("registerUserId", Util.null2str(sendManifest.getRegisterUserId()));
		docSend.put("registerUserName", Util.null2str(sendManifest.getRegisterUserName()));
		docSend.put("updateUserId", Util.null2str(sendManifest.getUpdateUserId()));
		docSend.put("contractDate", Util.null2str(sendManifest.getContractDate()));
		docSend.put("gmtTime", Util.null2str(sendManifest.getGmtTime()));
		docSend.put("penaltyYn", Util.null2str(sendManifest.getPenaltyYn()));
		docSend.put("docType", "MANIFEST");
		docSend.put("cryptographyYn", sendManifest.getCryptographyYn());
		// 에러 메시지 가져오기 조건 세팅
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		
		List<String> masterOdJobNoList = sendManifest.getMasterOdJobNoList();
		if(masterOdJobNoList != null && masterOdJobNoList.size() > 0){
			docSend.put("masterOdJobNo", masterOdJobNoList);
		}
		
		for(int i=0; i<masterOdJobNoList.size(); i++) {
			docSend.put("odJobNo", masterOdJobNoList.get(i));
			
			tempBlData = ediCommonService.getEdiMblOdInfo(docSend);
			
			if (tempBlData != null) {	// 실제 DB 데이터가 있는지 재확인
				
					processMessage.append("M.B/L No.");
					
					// 조회조건 세팅
					docSend.put("odJobNo", Util.null2str(tempBlData.get("OD_JOB_NO"))); // 마스터 Job번호
					docSend.put("masterOdJobNo", Util.null2str(tempBlData.get("MASTER_OD_JOB_NO")));
					docSend.put("mblNo", Util.null2str(tempBlData.get("MBL_NO")));
					docSend.put("action", sendManifest.getAction());
					
					// 서비스 코드 세팅
					ifServiceCode = ediCommonService.getServiceCode(Util.null2str(docSend.get("solution")), Util.null2str(docSend.get("bound")), Util.null2str(docSend.get("boundType")), Util.null2str(docSend.get("docType")), Util.null2str(tempBlData.get("EDI_VENDER")).trim());
					docSend.put("ifServiceCode", ifServiceCode);
					docSend.put("ediVender", Util.null2str(tempBlData.get("EDI_VENDER")).trim());
					docSend.put("mblNo", Util.null2str(tempBlData.get("MBL_NO")).trim());
					
					// 사업장정보 가져오기( 포워더 송/수신 계정정보, TB_SMS_ENT_BUS_FMS )
					fwSendAccInfo = ediCommonService.getFwSendAccInfo(docSend);
					
					if ("IMP".equals(Util.null2str(docSend.get("boundType")))) {
						// 항공수입 적하목록신고 문서송신
						if (!ediManifestAirImpSendService.setManifestAirImpSend4th(docSend, tempBlData, fwSendAccInfo, errList, result, request)) {
							if ("".equals(Util.null2str(result.get("processCode"))))	result.put("processCode", "A00001");
						}
						
					}
					processMessage.append(" : " + Util.null2str(Util.null2str(tempBlData.get("MBL_NO"))));
				
				
			} else {
				result.put("processCode", "0000");
				
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "M.B/L 자료가 존재하지 않습니다.");
				tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
				tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
				message = ediCommonService.getErrorCodeName(tempSearchData);
			}
			
			processMessage.append(" " + message);
		}
		if(!Util.null2str(message).equals("")){
			result.put("processMessage", processMessage.toString());
		}
		orderSaveResult.setResultData(result);
		log.info(orderSaveResult.toString());
		return orderSaveResult;
	}
	/**
	 * 통관목록결과조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public CustomsManifestResList customsManifestResList(SearchManifest search) throws Exception {
		CustomsManifestResList customsManifestResList = new CustomsManifestResList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, "userLanguage", true, null, null, null, null);
		List<CustomsManifestRes> list  = manifestMapper.customsManifestResList(search);
		customsManifestResList.setCustomsManifestResList(list);
		if(list != null && list.size() > 0) {
			customsManifestResList.setTotal(list.get(0).getTotal());
		}else {
			customsManifestResList.setTotal(0);
		}
		
		return customsManifestResList;
	}
	

	/**
	 * ECM 적하목록  정정 마스터 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectList selectCorrectMasterList(SearchManifestCorrectList search) throws Exception {
		ManifestCorrectList manifestCorrectList = new ManifestCorrectList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
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
		
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, null, null, "srchNoType", "MBL_NO|HBL_NO|MRN");
		List<ManifestCorrectMaster> list  = manifestMapper.selectCorrectMasterList(search);
		manifestCorrectList.setManifestCorrectMasterList(list);
		if(list != null && list.size() > 0) {
			manifestCorrectList.setTotal(list.get(0).getTotal());
		}else {
			manifestCorrectList.setTotal(0);
		}
		
		return manifestCorrectList;
	}
	
	/**
	 * ECM 적하목록 정정 HBL 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectList selectCorrectHouseList(SearchManifestCorrectList search) throws Exception {
		ManifestCorrectList manifestCorrectList = new ManifestCorrectList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
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
		List<String> srchModJobNoListArr = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchModJobNoListStr()))) {
			srchModJobNoListArr.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchModJobNoListStr())).replace("'","").split(",")));
		}
		if(srchModJobNoListArr.size() > 0){
			search.setSrchModJobNoListArr(srchModJobNoListArr);
		}else {
			search.setSrchModJobNoListArr(null);
		}
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, null, null, "srchNoType", "|HBL_NO|HSN|MBL_NO|MRN");
		List<ManifestCorrect> list  = manifestMapper.selectCorrectHouseList(search);
		manifestCorrectList.setManifestCorrectHouseList(list);  
		if(list != null && list.size() > 0) {
			manifestCorrectList.setTotal(list.get(0).getTotal());
		}else {
			manifestCorrectList.setTotal(0);
		}
		
		return manifestCorrectList;
	}	
	
	/**
	 * ECM 적하목록 정정 HBL 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectList selectCorrectContainerList(SearchManifestCorrectList search) throws Exception {
		ManifestCorrectList manifestCorrectList = new ManifestCorrectList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<String> cntrModTypeInList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getCntrModTypeIn()))) {
			cntrModTypeInList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getCntrModTypeIn())).replace("'","").split(",")));
		}
		if(cntrModTypeInList.size() > 0){
			search.setCntrModTypeInList(cntrModTypeInList);
		}else {
			search.setCntrModTypeInList(null);
		}
		
		Util.accessControlDatabase(search, "srchNoType|seqNoId|cntrSeqNoId", true, null, null, null, null);
		List<ManifestCorrect> list  = manifestMapper.selectCorrectContainerList(search);
		manifestCorrectList.setManifestCorrectContainerList(list);  
		if(list != null && list.size() > 0) {
			manifestCorrectList.setTotal(list.get(0).getTotal());
		}else {
			manifestCorrectList.setTotal(0);
		}
		
		return manifestCorrectList;
	}
	
	/**
	 * ECM 적하목록  정정번호 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectMaster getSerialNo(SearchManifestCorrectList search) throws Exception {
		ManifestCorrectMaster master  = manifestMapper.getSerialNo(search);		
		return master;
	}
	
	/**
	 * ECM 적하목록  정정정보 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectMaster getModInfo(SearchManifestCorrectList search) throws Exception {
		ManifestCorrectMaster master  = new ManifestCorrectMaster();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", search.getSrchEntCode());
		tmpMap.put("mrn", search.getMrn());
		tmpMap.put("modType", search.getModType());
		tmpMap = manifestMapper.selectLastMblInfo(tmpMap);
		if(tmpMap != null && tmpMap.size() > 0){
			master.setModReqDate(Util.null2str(tmpMap.get("MOD_REQ_DATE_TYPE")));
			master.setRegisterUserName(Util.null2str(tmpMap.get("REGISTER_USER_NAME_TYPE")));
			if(!Util.null2str(tmpMap.get("MOD_SERIAL_NO_TYPE")).equals("")){
				master.setModSerialNo(Util.null2str(tmpMap.get("MOD_SERIAL_NO_TYPE")));
			}else if(!Util.null2str(tmpMap.get("MOD_SERIAL_NO")).equals("")){
				master.setModSerialNo(Util.lpad(Util.null2str(Util.null2int(tmpMap.get("MOD_SERIAL_NO")) + 1), 4, '0'));
			}else{
				master.setModSerialNo("0001");
			}
		}
		return master;
	}
	
	/**
	 * 특송오더생성
	 * @param sendManifest
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult insertEms(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		OrderSaveResult result = new OrderSaveResult();
		boolean exchangeRate = false;
		String oblJobNo = null, hblNo = null, consigneeName = null, consigneeAddress = null, entBusinessCode = null, shipmentGubn = null, chargeCode = null, chargeName = null;
		String currencyCode = null, currencyCodeLoc = null, exrateTypeBus = null, exrateTypeCus = null;
		Map<String, Object> tmpMap;
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> exrate = new HashMap<String, Object>();
		Util.accessControlDatabase(sendManifest, "gmtTime", false, null, null, null, null);
		map.put("entCode", sendManifest.getEntCode());
		map.put("masterOdJobNo", sendManifest.getMasterOdJobNo());
		
		List<Map<String, Object>> insertOblList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> insertSblList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> list = manifestMapper.selectEmsTargetList(map);
		if(list != null && list.size() > 0) {
			String customerCode = null;
			String yymmdd = DateTimeUtil.getDate("yyMMdd");
			int pieces = 0, seqNo = 1;
			double gwt = 0;
			double cwt = 0;
			map.putAll(list.get(0));
			entBusinessCode = Util.null2str(map.get("ENT_BUSINESS_CODE"));
			if("AIR".equals(Util.null2str(map.get("BOUND")))){
				shipmentGubn = "98";
				chargeCode = "AFC";
				chargeName = "항공운임";
			}else{
				shipmentGubn = "99";
				chargeCode = "OFC";
				chargeName = "해상운임";
			}
			map.put("shipmentGubn", shipmentGubn);
			
			map.put("blTypeMh", "M");
			tmpMap = manifestMapper.selectEmsOblCheck(map);
			map.put("blTypeMh", "H");
			if(tmpMap != null && tmpMap.size() > 0){
				map.put("mblJobNo", tmpMap.get("OBL_JOB_NO"));
			}else{
				exchangeRate = true;
			}
			
			map.put("customerCode", map.get("CUSTOMER_CODE"));
			tmpMap = manifestMapper.selectEmsOblCheck(map);
			if(tmpMap != null && tmpMap.size() > 0){
				consigneeName = Util.null2str(tmpMap.get("CUSTOMER_NAME_ENG"));
				consigneeAddress = Util.null2str(tmpMap.get("CUSTOMER_ADDRESS"));
				exrateTypeCus = Util.null2str(tmpMap.get("EXRATE_TYPE"));
				if(!Util.isEmpty(tmpMap.get("OBL_JOB_NO"))){
					oblJobNo = Util.null2str(tmpMap.get("OBL_JOB_NO"));
					pieces = Util.null2int(tmpMap.get("PIECES"));
					gwt = Util.null2dbl(tmpMap.get("GWT"));
				}else{
					exchangeRate = true;
					tmpMap.put("srchHblNo", Util.null2str(map.get("customerCode")) + yymmdd);
					hblNo = manifestMapper.selectEmsHblNoMax(tmpMap);
					if(hblNo == null){
						hblNo = "001";
					}else{
						hblNo = Util.lpad(hblNo, 3, '0');
					}
					hblNo = Util.null2str(tmpMap.get("srchHblNo")) + hblNo;
				}
			}else{
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("appendString", Util.null2str(map.get("customerCode")));
				result.setResultCode("A11166");	/** #1#(은)는 삭제된 거래처 입니다. */
				result.setResultData(tmpMap);
				return result;
			}
			
			
			
			for(Map<String, Object> data : list){
				if(customerCode != null){
					if(!customerCode.equals(data.get("CUSTOMER_CODE"))){
						tmpMap = new HashMap<String, Object>(insertSblList.get(0));
						tmpMap.put("oblJobNo", oblJobNo);
						tmpMap.put("hblNo", hblNo);
						tmpMap.put("insertSblList", insertSblList);
						tmpMap.put("consigneeCode", customerCode);
						tmpMap.put("consigneeName", consigneeName);
						tmpMap.put("consigneeAddress", consigneeAddress);
						tmpMap.put("exrateTypeCus", exrateTypeCus);
						tmpMap.put("pieces", pieces);
						tmpMap.put("gwt", gwt);
						for (Map<String, Object> customerCwt : sendManifest.getDataList()) {
							 if(customerCode.equals(Util.null2str(customerCwt.get("customerCode")))) {
								 cwt = Util.null2dbl(customerCwt.get("houseCwt"));
								 break;
							 }
						}
						tmpMap.put("cwt", cwt);
						
						tmpMap.put("arExrateDate", tmpMap.get("AR_DATE"));
						tmpMap.put("shipmentGubn", shipmentGubn);
						insertOblList.add(tmpMap);
						
						
						insertSblList = new ArrayList<Map<String, Object>>();
						customerCode = Util.null2str(data.get("CUSTOMER_CODE"));
						map.put("customerCode", customerCode);
						tmpMap = manifestMapper.selectEmsOblCheck(map);
						if(tmpMap != null && tmpMap.size() > 0){
							consigneeName = Util.null2str(tmpMap.get("CUSTOMER_NAME_ENG"));
							consigneeAddress = Util.null2str(tmpMap.get("CUSTOMER_ADDRESS"));
							exrateTypeCus = Util.null2str(tmpMap.get("EXRATE_TYPE"));
							if(!Util.isEmpty(tmpMap.get("OBL_JOB_NO"))){
								oblJobNo = Util.null2str(tmpMap.get("OBL_JOB_NO"));
								pieces = Util.null2int(tmpMap.get("PIECES"));
								gwt = Util.null2dbl(tmpMap.get("GWT"));
							}else{
								exchangeRate = true;
								oblJobNo = null;
								pieces = 0;
								gwt = 0;
								tmpMap.put("srchHblNo", Util.null2str(map.get("customerCode")) + yymmdd);
								hblNo = manifestMapper.selectEmsHblNoMax(tmpMap);
								if(hblNo == null){
									hblNo = "001";
								}else{
									hblNo = Util.lpad(hblNo, 3, '0');
								}
								hblNo = Util.null2str(tmpMap.get("srchHblNo")) + hblNo;
							}
						}else{
							tmpMap = new HashMap<String, Object>();
							tmpMap.put("appendString", Util.null2str(map.get("customerCode")));
							result.setResultCode("A11166");	/** #1#(은)는 삭제된 거래처 입니다. */
							result.setResultData(tmpMap);
							return result;
						}
					}
				}else{
					customerCode = Util.null2str(data.get("CUSTOMER_CODE"));
				}
				
				pieces += Util.null2int(data.get("PIECES"));
				gwt += Util.null2int(data.get("GWT"));
				insertSblList.add(data);
			}
			
			tmpMap = new HashMap<String, Object>(insertSblList.get(0));
			tmpMap.put("oblJobNo", oblJobNo);
			tmpMap.put("hblNo", hblNo);
			tmpMap.put("insertSblList", insertSblList);
			tmpMap.put("consigneeCode", customerCode);
			tmpMap.put("consigneeName", consigneeName);
			tmpMap.put("consigneeAddress", consigneeAddress);
			tmpMap.put("exrateTypeCus", exrateTypeCus);
			tmpMap.put("pieces", pieces);
			tmpMap.put("gwt", gwt);
			for (Map<String, Object> customerCwt : sendManifest.getDataList()) {
				 if(customerCode.equals(Util.null2str(customerCwt.get("customerCode")))) {
					 cwt = Util.null2dbl(customerCwt.get("houseCwt"));
					 break;
				 }
			}
			tmpMap.put("cwt", cwt);
			tmpMap.put("arExrateDate", tmpMap.get("AR_DATE"));
			tmpMap.put("shipmentGubn", shipmentGubn);
			insertOblList.add(tmpMap);
			
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(sendManifest.getEntCode());
			processLogs.setEntBusinessCode(entBusinessCode);
			processLogs.setSolution("EMS");
			processLogs.setBound(Util.null2str(map.get("BOUND")));
			processLogs.setBoundType(Util.null2str(map.get("BOUND_TYPE")));
			processLogs.setProcessUserId(sendManifest.getUpdateUserId());
			processLogs.setLogJobType("DATA");
			processLogs.setDescription("EMS B/L MODIFY");
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(sendManifest.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(sendManifest.getLogUserName());
			processLogs.setProcessUserNameEng(sendManifest.getLogUserNameEng());
			
			map.put("entBusinessCode", entBusinessCode);
			if(exchangeRate){
				tmpMap = manifestMapper.selectExchangeRateType(map);
				currencyCode = Util.null2str(tmpMap.get("CURRENCY_CODE"));
				currencyCodeLoc = Util.null2str(tmpMap.get("CURRENCY_CODE_LOC"));
				exrateTypeBus = Util.null2str(tmpMap.get("EXRATE_TYPE"));
			}
			
			map.put("gmtTime", sendManifest.getGmtTime());
			map.put("updateUserId", sendManifest.getUpdateUserId());
			map.put("solution", "EMS");
			map.put("stockSeqLength", 5);
			
			if(map.get("mblJobNo") == null){
				tmpMap.put("exrateType", this.setExrateType(exrateTypeBus));
				tmpMap.put("srchEntCode", map.get("entCode"));
				tmpMap.put("entBusinessCode", entBusinessCode);
				tmpMap.put("srchDate", map.get("AR_DATE"));
				tmpMap.put("srchDateFormat", "23");
				tmpMap = commonMapper.selectExchangeRate(tmpMap);
				if(tmpMap != null && tmpMap.size() > 0){
					exrate.put(exrateTypeBus, Util.null2dbl(tmpMap.get("EXCHANGE_RATE_" + currencyCode)));
					exrate.put(exrateTypeBus + "_USD", Util.null2dbl(tmpMap.get("EXCHANGE_RATE_USD")));
				}else{
					exrate.put(exrateTypeBus, 0);
					exrate.put(exrateTypeBus + "_USD", 0);
				}
				map.put("arExrate", exrate.get(exrateTypeBus));
				map.put("exrateUsd", exrate.get(exrateTypeBus + "_USD"));
				
				map.put("BL_TYPE_MH", "M");
				map.put("arCurrencyCode", currencyCode);
				map.put("currencyCodeLoc", currencyCodeLoc);
				map.put("stockType", "OBL");
				map.put("stockSign", "EBL");
				map.put("stockRemark", "OBL_JOB_NO");
				map.put("seqNo", seqNo++);
				map.put("mblJobNo", commonMapper.selectNextStockMaxNo(map));
				map.put("oblJobNo", map.get("mblJobNo"));
				manifestMapper.insertEmsObl(map);
				
				map.put("stockType", "OD");
				map.put("stockSign", "EOD");
				map.put("stockRemark", "ORDER-JOB NO");
				map.put("seqNo", seqNo++);
				map.put("odJobNo", commonMapper.selectNextStockMaxNo(map));
				manifestMapper.insertEmsOd(map);
				manifestMapper.insertEmsOdBus(map);
				map.put("chargeCode", chargeCode);
				map.put("chargeName", chargeName);
				map.put("blNo", map.get("MBL_NO"));
				manifestMapper.insertEmsOdChg(map);
				
				processLogs.setJobFuncType("ADD");
				processLogs.setJobNo(Util.null2str(map.get("oblJobNo")));
				processLogs.setJobLog(new Gson().toJson(map));
				commonService.insertLog(processLogs, request);
			}
			
			for(Map<String, Object> data : insertOblList){
				if(Util.isEmpty(data.get("oblJobNo"))){
					exrateTypeCus = Util.null2str(data.get("exrateTypeCus"), exrateTypeBus);
					if(exrate.get(exrateTypeCus) == null){
						tmpMap.put("exrateType", this.setExrateType(exrateTypeCus));
						tmpMap.put("srchEntCode", map.get("entCode"));
						tmpMap.put("entBusinessCode", entBusinessCode);
						tmpMap.put("srchDate", map.get("AR_DATE"));
						tmpMap.put("srchDateFormat", "23");
						/** 취약성점검 SQL Injection 방지 */
						Util.accessControlDatabase(tmpMap, "exrateType", false, null, null, null, null);
						tmpMap = commonMapper.selectExchangeRate(tmpMap);
						if(tmpMap != null && tmpMap.size() > 0){
							exrate.put(exrateTypeCus, Util.null2dbl(tmpMap.get("EXCHANGE_RATE_" + currencyCode)));
							exrate.put(exrateTypeCus + "_USD", Util.null2dbl(tmpMap.get("EXCHANGE_RATE_USD")));
						}else{
							exrate.put(exrateTypeCus, 0);
							exrate.put(exrateTypeCus + "_USD", 0);
						}
					}
					
					data.put("arCurrencyCode", currencyCode);
					data.put("currencyCodeLoc", currencyCodeLoc);
					data.put("arExrate", exrate.get(exrateTypeCus));
					data.put("exrateUsd", exrate.get(exrateTypeCus + "_USD"));
					map.put("stockType", "OBL");
					map.put("stockSign", "EBL");
					map.put("stockRemark", "OBL_JOB_NO");
					map.put("seqNo", seqNo++);
					data.put("oblJobNo", commonMapper.selectNextStockMaxNo(map));
					data.put("entCode", sendManifest.getEntCode());
					data.put("entBusinessCode", entBusinessCode);
					data.put("mblJobNo", map.get("mblJobNo"));
					data.put("gmtTime", sendManifest.getGmtTime());
					data.put("updateUserId", sendManifest.getUpdateUserId());
					manifestMapper.insertEmsObl(data);
					
					
					map.put("stockType", "OD");
					map.put("stockSign", "EOD");
					map.put("stockRemark", "ORDER-JOB NO");
					map.put("seqNo", seqNo++);
					data.put("odJobNo", commonMapper.selectNextStockMaxNo(map));
					data.put("gmtTime", sendManifest.getGmtTime());
					data.put("updateUserId", sendManifest.getUpdateUserId());
					manifestMapper.insertEmsOd(data);
					manifestMapper.insertEmsOdBus(data);
					data.put("chargeCode", chargeCode);
					data.put("chargeName", chargeName);
					data.put("blNo", map.get("hblNo"));
					manifestMapper.insertEmsOdChg(data);
					
					processLogs.setJobFuncType("ADD");
					processLogs.setJobNo(Util.null2str(data.get("oblJobNo")));
					processLogs.setJobLog(new Gson().toJson(data));
					commonService.insertLog(processLogs, request);
				}else{
					manifestMapper.updateEmsObl(data);
					
					processLogs.setJobFuncType("UPDATE");
					processLogs.setJobNo(Util.null2str(data.get("oblJobNo")));
					processLogs.setJobLog(new Gson().toJson(data));
					commonService.insertLog(processLogs, request);
				}
			}
			
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
				int cnt = 1;
				for(Map<String, Object> hblData : insertOblList){
					insertSblList = (List<Map<String, Object>>)hblData.get("insertSblList");
					for(Map<String, Object> data : insertSblList){
						data.put("stockType", "SBL");
						data.put("stockSign", "SBL");
						data.put("stockRemark", "SBL_JOB_NO");
						data.put("entCode", sendManifest.getEntCode());
						data.put("entBusinessCode", entBusinessCode);
						data.put("solution", "EMS");
						data.put("stockSeqLength", 5);
						data.put("seqNo", seqNo++);
						data.put("oblJobNo", hblData.get("oblJobNo"));
						data.put("gmtTime", sendManifest.getGmtTime());
						data.put("updateUserId", sendManifest.getUpdateUserId());
						manifestBatchMapper.insertEmsSbl(data);
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
		}
		return result;
	}
	public String setExrateType(String exrateType) throws Exception {
		exrateType = "EXRATE_" + exrateType;
		if(exrateType.equals("EXRATE_CASH_BUYING")){
			exrateType = "EXRATE_BUYING_CASH";
		}else if(exrateType.equals("EXRATE_CASH_SELLING")){
			exrateType = "EXRATE_SELLING_CASH";
		}else if(exrateType.equals("EXRATE_CUSTOMS_BUYING")){
			exrateType = "EXRATE_BUYING_CUSTOMS";
		}else if(exrateType.equals("EXRATE_CUSTOMS_SELLING")){
			exrateType = "EXRATE_SELLING_CUSTOMS";
		}
		return exrateType;
	}
	/**
	 * 적하목록 정정 - insert
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult insertCorrect(ManifestCorrect manifestCorrect) throws Exception {
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		Util.accessControlDatabase(manifestCorrect, "gmtTime|dateFormat", false, null, null, null, null);
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		String gubun = Util.null2str(manifestCorrect.getGubun());
		String modType = Util.null2str(manifestCorrect.getModType());		
		String cntrModType = Util.null2str(manifestCorrect.getCntrModType());
		
		List<Map<String, Object>> containerList = manifestCorrect.getContainerList();
		
		String cntrNo = null;
		if("99".equals(modType) && !"A".equals(cntrModType)){
			boolean updateCntrNo = false;
			if("U".equals(cntrModType)){
				for(Map<String, Object> containerMap : containerList){
					if("EH".equals(containerMap.get("cntrModCode"))){
						updateCntrNo = true;
						break;
					}
				}
			}
			
			if(updateCntrNo){
				tmpMap.put("odJobNoList", manifestCorrect.getOdJobNoList());
			}else {
				tmpMap.put("odJobNo", manifestCorrect.getOdJobNoList().get(0));
			}
			tmpMap = abnormalMapper.selectAbnormalCntrInfo(tmpMap);
			if(tmpMap != null){
				cntrNo = Util.null2str(tmpMap.get("CNTR_NO"));
				if(updateCntrNo && Util.null2int(tmpMap.get("CNTR_CNT")) > 1){
					manifestCorrectSaveResult.setResultCode("A00061");	/** 두 건 이상의 데이터가 검색되었습니다. */
					return manifestCorrectSaveResult;
				}
			}else{
				manifestCorrectSaveResult.setResultCode("A00997");	/** #1#(를)을 입력하여 주십시요. */
				return manifestCorrectSaveResult;
			}
		}
		
		if(!Util.isEmpty(manifestCorrect.getModJobNo())){
			//hbl 추가/삭제인 경우 데이터 존재하는지 체크
			int modCnt = 0;
			if(containerList != null && !"orderUpdate".equals(modType)) {
				modCnt = (manifestMapper.getIsModContainerData(manifestCorrect)).intValue();
			}else {
				modCnt = (manifestMapper.getIsModData(manifestCorrect)).intValue();
			}
			if(modCnt > 0) {
				manifestCorrectSaveResult.setResultCode("A11260");	/** 적하정정건 이미 존재합니다. */
				return manifestCorrectSaveResult;
			}
		}else{
			manifestCorrect.setModJobNo("null");	/** modJobNo없을경우 신규저장하기위해 null입력 */
		}
		
		Map<String, Object> fwSendAccInfo = null;
		String insertMasterYn = null;
		
		if(!"orderUpdate".equals(modType) && !"modUpdate".equals(modType)) {
			if(Util.null2str(manifestCorrect.getMrn()).length() != 11){
				manifestCorrectSaveResult.setResultCode("A11275");	/** #1#을 #2#자리로 입력하십시오. */
				return manifestCorrectSaveResult;
			}
			
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("entCode", manifestCorrect.getEntCode());
			tmpMap.put("entBusinessCode", manifestCorrect.getEntBusinessCode());
			fwSendAccInfo = manifestMapper.getFwSendAccInfo(tmpMap);
			if(fwSendAccInfo == null || fwSendAccInfo.size() == 0 || Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).equals("") || Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).length() != 4){
				manifestCorrectSaveResult.setResultCode("A11296");	/** 신청인부호를 4자리로 입력하십시오. */
				return manifestCorrectSaveResult;
			}
			
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("mrn", manifestCorrect.getMrn());
			tmpMap.put("entCode", manifestCorrect.getEntCode());
			tmpMap.put("modType", manifestCorrect.getModType());
			tmpMap.put("solution", manifestCorrect.getSolution());
			tmpMap.put("modJobNo", manifestCorrect.getModJobNo());
			tmpMap = manifestMapper.selectLastMblInfo(tmpMap);
			if(tmpMap != null && tmpMap.size() > 0){
//				if(!Util.null2str(tmpMap.get("SEND_STATUS")).equals("")){
//					manifestCorrectSaveResult.setResultCode("A11251");	/** 선택한 정정건이 이미 송신된 상태입니다. 신규로 등록해주세요. */
//					return manifestCorrectSaveResult;
//				}
				if(!Util.null2str(tmpMap.get("MOD_JOB_NO_TYPE")).equals("")){
					manifestCorrect.setModJobNo(Util.null2str(tmpMap.get("MOD_JOB_NO_TYPE")));
				}else{
					insertMasterYn = "Y";
					manifestCorrect.setModSerialNo(Util.lpad(Util.null2str(Util.null2int(tmpMap.get("MOD_SERIAL_NO")) + 1), 4, '0'));
					manifestCorrect.setModJobNo(null);
				}
			}else {
				insertMasterYn = "Y";
				/** UPS는 정정일련번호 101부터 생성 (다른 수입부에서 KTNET사이트에서 직접 정정하고 있어서 번호 겹치지 않게 하기 위함) */
				if(manifestCorrect.getEntCode().equals("YHTXX") || manifestCorrect.getEntCode().equals("UPSXK")) {
					manifestCorrect.setModSerialNo("0101");
				}else {
					manifestCorrect.setModSerialNo("0001");
				}
				manifestCorrect.setModJobNo(null);
			}
		}
		
		/** B/L추가 - 오더데이터 저장시 정정사유 업데이트 */
		if("orderUpdate".equals(modType) || "modUpdate".equals(modType)){
			manifestCorrectSaveResult = this.updateModReason(manifestCorrect, null);
			if(!Util.isEmpty(manifestCorrectSaveResult.getResultCode())){
				return manifestCorrectSaveResult;
			}
		}
		
		//hbl이 존재하는 체크 
		Map<String, Object> hblMap = null;
		Integer seqIdNo = null;
		
		//hbl 추가
		if("orderUpdate".equals(modType) || ("06".equals(modType) && manifestCorrect.getOdJobNoList() != null)) {	/** 비엘추가의 오더내용수정시 modType코드 orderUpdate로 호출 */
			//Hbl 추가인 경우 Order내용변경된거에 따라 Order 까지 데이터 업데이트해줘야 함.
			boolean updateAd = false;
			if("orderUpdate".equals(modType) && "AIR".equals(manifestCorrect.getBound())){
				manifestCorrect.setModType("airOrderUpdate");
				updateAd = true;
			}
//			if("IMP".equals(manifestCorrect.getBoundType()) && ("YHTXX".equals(manifestCorrect.getEntCode()) || "UPSXK".equals(manifestCorrect.getEntCode()))){
//				tmpMap = new HashMap<String,Object>();
//				tmpMap.put("odJobNo", manifestCorrect.getOdJobNo());
//				tmpMap.put("selectPackingCnt", "Y");
//				tmpMap = orderMapper.selectOrderChk(tmpMap);
//				if(tmpMap != null && Util.null2int(tmpMap.get("PACKING_CNT")) != manifestCorrect.getPieces()){
//					manifestCorrect.setPackingErrorYn("Y");
//				}else{
//					manifestCorrect.setPackingErrorYn("N");
//				}
//				updateAd = true;
//			}
			if(updateAd) {
				manifestMapper.updateOdByCorrect1(manifestCorrect);
			}
			
			
			manifestMapper.updateOdByCorrect(manifestCorrect);
			
			if("IMP".equals(manifestCorrect.getBoundType()) && ("YHTXX".equals(manifestCorrect.getEntCode()) || "UPSXK".equals(manifestCorrect.getEntCode()))){
				tmpMap = new HashMap<String,Object>();
				tmpMap.put("odJobNo", manifestCorrect.getOdJobNo());
				commonMapper.updatePackingErrorYn(tmpMap);
			}
			
			if("orderUpdate".equals(modType) && containerList != null){
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("odJobNo", manifestCorrect.getOdJobNo());
				orderMapper.deleteContainer(tmpMap);
				List<OrderContainer> cntrList = new ArrayList<OrderContainer>();
				for(Map<String, Object> container : containerList) {
					OrderContainer orderContainer = new OrderContainer();
					orderContainer.setOdJobNo(Util.null2str(container.get("odJobNo")));
					orderContainer.setCntrNo(Util.null2str(container.get("cntrNo")));
					orderContainer.setCntrSealNo1(Util.null2str(container.get("cntrSealNo1")));
					orderContainer.setCntrSealNo2(Util.null2str(container.get("cntrSealNo2")));
					orderContainer.setCntrSealNo3(Util.null2str(container.get("cntrSealNo3")));
					orderContainer.setCntrCode(Util.null2str(container.get("cntrCode")));
					orderContainer.setCntrEdiCode(Util.null2str(container.get("cntrEdiCode")));
					orderContainer.setPieces(Util.null2int(container.get("pieces")));
					orderContainer.setUnitPieces(Util.null2str(container.get("unitPieces")));
					orderContainer.setGwt(Util.null2int(container.get("gwt")));
					orderContainer.setCbm(Util.null2int(container.get("cbm")));
					cntrList.add(orderContainer);
				}
				orderMapper.insertContainerExcelError(cntrList);
			}
		}else if("modUpdate".equals(modType)){	/** 항목정정의 정정내용수정시 modType코드 modUpdate로 호출 */
			manifestMapper.updateHblCorrect(manifestCorrect);
		}
//		else if ("99".equals(modType)){
//			String cntrModCode = Util.null2str(manifestCorrect.getCntrModCode());
//			if(insertMasterYn == null){	/** MOD존재 */
//				//hbl이 존재하는 체크 
//				hblMap = manifestMapper.getIsHbl(manifestCorrect);
//				if(hblMap != null) {
//					seqIdNo = (Integer)hblMap.get("SEQ_NO_ID");
//					if(seqIdNo != null) {
//						manifestCorrect.setSeqNoId(seqIdNo);
//						/** 컨테이너번호 중복체크(정정/신규/삭제) */
//						if(!this.cntrNoDupChk(manifestCorrect, cntrModType, cntrModCode)){
//							manifestCorrectSaveResult.setResultCode("A11297");	/** 이미 사용된 컨테이너번호입니다. */
//							return manifestCorrectSaveResult;
//						}
//					}
//				}
//			}
//			
//			if("A".equals(cntrModType) || "X".equals(cntrModType) || ("U".equals(cntrModType) && cntrModCode.equals("EH"))){	/** 신규/컨테이너번호정정일경우 컨테이너번호중복체크 */
//				tmpMap = manifestMapper.selectOrderContainer(manifestCorrect);
//				if(tmpMap != null && tmpMap.size() > 0){
//					manifestCorrectSaveResult.setResultCode("A00623");	/** 중복되는 번호가 있습니다. */
//					return manifestCorrectSaveResult;
//				}
//			}
//			if("A".equals(cntrModType)) {
//				manifestMapper.insertOrderContainer(manifestCorrect);
//			}else if("D".equals(cntrModType)){
//				manifestMapper.deleteOrderContainer(manifestCorrect);
//			}else if("U".equals(cntrModType) || "X".equals(cntrModType)){
//				tmpMap = new HashMap<String, Object>();
//				tmpMap.put("entCode", manifestCorrect.getEntCode());
//				tmpMap.put("odJobNo", manifestCorrect.getOdJobNo());
//				tmpMap.put("cntrNo", manifestCorrect.getCntrNo());
//				tmpMap.put("updateValue", manifestCorrect.getAfterValue());
//				if(cntrModCode.equals("EA")){
//					tmpMap.put("updateField", "CNTR_CODE");
//				}else if(cntrModCode.equals("EB")){
//					tmpMap.put("updateField", "CNTR_SEAL_NO_1");
//				}else if(cntrModCode.equals("EC")){
//					tmpMap.put("updateField", "CNTR_SEAL_NO_2");
//				}else if(cntrModCode.equals("EH")){
//					tmpMap.put("updateField", "CNTR_NO");
//				}else if(cntrModCode.equals("EI")){
//					tmpMap.put("updateField", "CNTR_SEAL_NO_3");
//				}else if(cntrModCode.equals("EJ")){
//					tmpMap.put("updateField", "PIECES");
//				}
//				manifestMapper.updateContainerByCorrect(tmpMap);
//			}
//		}
		
		if("orderUpdate".equals(modType) || "modUpdate".equals(modType)) {
			return manifestCorrectSaveResult;
		}
		
		if("Y".equals(insertMasterYn)){
			/** 관리번호 */
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("gmtTime", manifestCorrect.getGmtTime());
			tmpMap.put("entCode", manifestCorrect.getEntCode());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("registerUserId", manifestCorrect.getRegisterUserId());
			tmpMap.put("updateUserId", manifestCorrect.getRegisterUserId());
			tmpMap.put("solution", "ECM");
			tmpMap.put("stockType", "MOD");
			tmpMap.put("stockSign", "MOD");
			tmpMap.put("stockSeqLength", 7);
			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", "MOD-JOB NO");
			manifestCorrect.setModJobNo(commonMapper.selectNextStockMaxNo(tmpMap));
			
			tmpMap.put("resultCode", "FIXED_SIGN_DATE_SEQ_NO");
			tmpMap.put("stockDateStr", fwSendAccInfo.get("B2G_FORWARDER_SIGN"));
			tmpMap.put("stockSign", manifestCorrect.getMrn());
			tmpMap.put("stockSeqLength", 4);
			tmpMap.put("stockRemark", "MOD-REQ NO");
			/** 정정신청번호 끝 네자리는 정정번호(modSerialNo) - 2024.06.03*/
//			tmpMap.put("modReqNo", commonMapper.selectNextStockMaxNo(tmpMap));
			tmpMap.put("modReqNo", manifestCorrect.getMrn() + fwSendAccInfo.get("B2G_FORWARDER_SIGN") + manifestCorrect.getModSerialNo());
			//적하정정 데이터 insert 처리
			manifestMapper.insertMblCorrect(manifestCorrect);
			manifestCorrect.setModReqDate(null);
		}
				
		//컨테이너 정정인 경우
		if ("99".equals(modType)) {
//			//hbl이 없는 경우 insert
//			if(seqIdNo == null) {
//				seqIdNo = manifestMapper.getSeqNoId(manifestCorrect);
//				manifestCorrect.setSeqNoId(seqIdNo);
//				manifestMapper.insertHblCorrect(manifestCorrect);
//			}
//			manifestMapper.insertContainerCorrect(manifestCorrect);
			
			int hblSeqNoId = 0, cntrSeqNoId = 0;
			Map<String, Object> seqIdNoMap, map, tmpContainerMap;
			List<Map<String, Object>> tmpContainerList;
			List<String> odJobNoList = new ArrayList<String>(manifestCorrect.getOdJobNoList());
			List<String> tmpOdJobNoList;
			
			Abnormal tmpAbnormal = new Abnormal();
			tmpAbnormal.setModType("99");
			tmpAbnormal.setModJobNo(manifestCorrect.getModJobNo());
			
			tmpMap = new HashMap<String, Object>();
			
			
			for(int i=0;i<odJobNoList.size();i++) {
				
				if("Y".equals(insertMasterYn)){
					if(i == 0){
						seqIdNo = 1;
						hblSeqNoId = 0;
					}
					cntrSeqNoId = 1;
				}else{
					tmpAbnormal.setOdJobNo(odJobNoList.get(i));
					seqIdNoMap = abnormalMapper.getSeqNoIdByAb(tmpAbnormal);
					hblSeqNoId = Util.null2int(seqIdNoMap.get("HBL_SEQ_NO_ID"));
					cntrSeqNoId = Util.null2int(seqIdNoMap.get("CNTR_SEQ_NO_ID"));
					if(i == 0){
						seqIdNo = Util.null2int(seqIdNoMap.get("SEQ_NO_ID"));
					}
				}
				
				if(hblSeqNoId == 0){
					tmpOdJobNoList = new ArrayList<String>();
					tmpOdJobNoList.add(odJobNoList.get(i));
					manifestCorrect.setOdJobNoList(tmpOdJobNoList);
					manifestCorrect.setGubun("containerInsert");
					manifestCorrect.setSeqNoId(seqIdNo++);
					manifestMapper.insertHblCorrect(manifestCorrect);
				}else{
					manifestCorrect.setSeqNoId(hblSeqNoId);
				}
				
				map = null;
				tmpContainerList = new ArrayList<Map<String, Object>>();
				for(Map<String, Object> cntrMap : containerList){
					cntrMap.put("modJobNo", manifestCorrect.getModJobNo());
					cntrMap.put("seqNoId", manifestCorrect.getSeqNoId());
					cntrMap.put("cntrSeqNoId", cntrSeqNoId++);
					cntrMap.put("cntrNo", Util.null2str(cntrMap.get("cntrNo"), cntrNo));
					cntrMap.put("cntrModType", cntrModType);
					cntrMap.put("odJobNo", odJobNoList.get(i));
					if(map == null){
						map = new HashMap<String, Object>(cntrMap);
					}
					tmpContainerMap = new HashMap<String, Object>();
					tmpContainerMap.put("cntrModCode", cntrMap.get("cntrModCode"));
					tmpContainerMap.put("beforeValue", cntrMap.get("beforeValue"));
					tmpContainerMap.put("afterValue", cntrMap.get("afterValue"));
					tmpContainerList.add(tmpContainerMap);
				}
				map.put("cntrModList", tmpContainerList);
				
				if("A".equals(cntrModType)){
					abnormalMapper.insertOrderContainer(map);
				}else if("U".equals(cntrModType)){
					abnormalMapper.updateOrderContainer(map);
				}else{
					abnormalMapper.deleteOrderContainer(map);
				}
				
				abnormalMapper.insertContainerCorrectByAb(map);
			}
		}else if(manifestCorrect.getSingleModCodeList() != null && (manifestCorrect.getSingleModCodeList().size() > 0)){
			for(String singleModCode : manifestCorrect.getSingleModCodeList()){
				manifestCorrect.setSingleModCode(singleModCode);
				manifestMapper.insertHblCorrect(manifestCorrect);
			}
		}else {
			manifestMapper.insertHblCorrect(manifestCorrect);
		}
		
		if("06".equals(modType)){
			manifestCorrect.setModReqDate(null);
		}
		//hbl 카운트 업데이트
		manifestMapper.updateMblCorrect(manifestCorrect);
		
		//관리번호 셋팅
		manifestCorrectSaveResult.setModJobNo(manifestCorrect.getModJobNo());
		
		return manifestCorrectSaveResult;
	}
	
	/**
	 * 적하정정 삭제 - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult deleteCorrect(ManifestCorrectDelete manifestCorrectDelete) throws Exception {
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		//delete 처리
		ManifestCorrect manifestCorrect = manifestCorrectDelete.getManifestCorrectList().get(0);
		Util.accessControlDatabase(manifestCorrect, "dateFormat", false, null, null, null, null);
		
		manifestCorrectDelete.setModJobNo(manifestCorrect.getModJobNo());
		int result = manifestMapper.deleteCorrect(manifestCorrectDelete);
		//hbl 카운트 업데이트
		if(result > 0) {
			manifestCorrect.setModReqDate(null);
			manifestMapper.updateMblCorrect(manifestCorrect);
		}else {
			manifestMapper.deleteMblCorrect(manifestCorrect);
		}
		return manifestCorrectSaveResult;
	}
	public ManifestCorrectSaveResult deleteCorrectMbl(ManifestCorrectDelete manifestCorrectDelete, HttpServletRequest request) throws Exception {
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		
		int sendCnt = manifestMapper.deleteCorrectMblCheck(manifestCorrectDelete);
		if(sendCnt > 0){
			manifestCorrectSaveResult.setResultCode("A11252");	/** 전송된건은 삭제 할수 없습니다. */
			return manifestCorrectSaveResult;
		}
		manifestMapper.deleteAllCorrect(manifestCorrectDelete);
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(manifestCorrectDelete.getEntCode());
		processLogs.setEntBusinessCode(manifestCorrectDelete.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(manifestCorrectDelete.getBound());
		processLogs.setBoundType(manifestCorrectDelete.getBoundType());
		processLogs.setProcessUserId(manifestCorrectDelete.getUserId());
		processLogs.setJobNo(manifestCorrectDelete.getModJobNo());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("DELETE");
		processLogs.setDescription("DELETE MANIFEST MOD[MBL_NO : " + manifestCorrectDelete.getMblNo()+"]");
		processLogs.setJobLog(new Gson().toJson(manifestCorrectDelete));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(manifestCorrectDelete.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(manifestCorrectDelete.getLogUserName());
		processLogs.setProcessUserNameEng(manifestCorrectDelete.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		return manifestCorrectSaveResult;
	}
	
	/**
	 * 적하정정사유 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult updateModReason(ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		
		Map<String, Object> map = manifestMapper.selectModMblInfo(manifestCorrect);
		if(map != null && map.size() > 0){
			if(Util.null2str(map.get("SEND_STATUS")).equals("Y")){
				manifestCorrectSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
				return manifestCorrectSaveResult;
			}
		}else{
			manifestCorrectSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
			return manifestCorrectSaveResult;
		}
		
		manifestMapper.updateModReason(manifestCorrect);
		
		/** B/L추가 - 오더데이터 저장에서 정정사유 업데이트시 request == null */
		if(request != null) {
			// 처리 로그 기록
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setJobFuncType("ADD");
			processLogs.setEntCode(manifestCorrect.getEntCode());
			processLogs.setEntBusinessCode(manifestCorrect.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(manifestCorrect.getBound());
			processLogs.setBoundType(manifestCorrect.getBoundType());
			processLogs.setProcessUserId(manifestCorrect.getUpdateUserId());
			processLogs.setJobNo(manifestCorrect.getOdJobNo());
			processLogs.setLogJobType("DATA");
			processLogs.setDescription("updateModReason[HBL_NO : " + manifestCorrect.getHblNo()+"]");
			processLogs.setJobLog(new Gson().toJson(manifestCorrect));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(manifestCorrect.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(manifestCorrect.getLogUserName());
			processLogs.setProcessUserNameEng(manifestCorrect.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}
		
				
		return manifestCorrectSaveResult;
	}
	
	/**
	 * 적하목록정정 마스터 정보 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult updateModMbl(ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		
		Map<String, Object> map = manifestMapper.selectModMblInfo(manifestCorrect);
		if(map != null && map.size() > 0){
			if(Util.null2str(map.get("SEND_STATUS")).equals("Y")){
				manifestCorrectSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
				return manifestCorrectSaveResult;
			}
		}else{
			manifestCorrectSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
			return manifestCorrectSaveResult;
		}
		
		manifestMapper.updateModMbl(manifestCorrect);
		manifestCorrectSaveResult.setResultCode("A012051");	/** 저장되었습니다. */
		
		return manifestCorrectSaveResult;
	}
	
	/**
	 * 적하목록 컨테이너정정 정보수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult updateContainer(ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		
		if("U".equals(manifestCorrect.getCntrModType()) && "EH".equals(manifestCorrect.getCntrModCode())){
			manifestCorrect.setCntrNo(manifestCorrect.getAfterValue());
		}
		Map<String, Object> map = manifestMapper.selectModMblInfo(manifestCorrect);
		if(map != null && map.size() > 0){
			if(Util.null2str(map.get("SEND_STATUS")).equals("Y")){
				manifestCorrectSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
				return manifestCorrectSaveResult;
			}
			if(Util.null2int(map.get("CNTR_MOD_CODE_CNT")) > 0){
				manifestCorrectSaveResult.setResultCode("A11260");	/** 적하정정건 이미 존재합니다. */
				return manifestCorrectSaveResult;
			}
			if(Util.null2int(map.get("CNTR_NO_CNT")) > 0){
				manifestCorrectSaveResult.setResultCode("A00623");	/** 중복되는 번호가 있습니다. */
				return manifestCorrectSaveResult;
			}
		}else{
			manifestCorrectSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
			return manifestCorrectSaveResult;
		}
		
		manifestMapper.updateModContainerInfo(manifestCorrect);
		manifestMapper.updateOrderContainerInfo(manifestCorrect);
		if(("U".equals(manifestCorrect.getCntrModType()) && "EH".equals(manifestCorrect.getCntrModCode())) || "A".equals(manifestCorrect.getCntrModType())){
			manifestCorrect.setCntrModType("updateCntrNo");
			manifestMapper.updateModContainerInfo(manifestCorrect);
		}
				
		return manifestCorrectSaveResult;
	}
	
	/**
	 * ECM 적하목록 정정 팝업 마스터 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectList selectPopupCorrectMasterList(SearchManifestCorrectList search) throws Exception {
		ManifestCorrectList manifestCorrectList = new ManifestCorrectList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchDateGbn", true, null, null, null, null);
		List<ManifestCorrectMaster> list  = manifestMapper.selectPopupCorrectMasterList(search);
		manifestCorrectList.setManifestCorrectMasterList(list);  
		if(list != null && list.size() > 0) {
			manifestCorrectList.setTotal(list.get(0).getTotal());
		}else {
			manifestCorrectList.setTotal(0);
		}
		
		return manifestCorrectList;
	}
	
	/**
	 * ECM 적하목록 정정 수신결과리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectRecvResultList selectManifestCorrectRecvResultList(SearchManifestCorrectList search) throws Exception {
		ManifestCorrectRecvResultList manifestCorrectRecvResultList = new ManifestCorrectRecvResultList();
		manifestCorrectRecvResultList.setManifestCorrectRecvResultList(manifestMapper.selectManifestCorrectRecvResultList(search));  
		return manifestCorrectRecvResultList;
	}
	
	/**
	 * 적하목록 정정 - copy
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult copyCorrect(ManifestCorrect manifestCorrect) throws Exception {
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		Util.accessControlDatabase(manifestCorrect, "gmtTime", false, null, null, null, null);
		
		//정정번호 채번
		SearchManifestCorrectList search = new SearchManifestCorrectList();
		search.setMrn(manifestCorrect.getMrn());
		ManifestCorrectMaster master  = manifestMapper.getSerialNo(search);	
		manifestCorrect.setModSerialNo(master.getModSerialNo());
		
		//카피 관리번호 셋팅
		manifestCorrect.setCopyModJobNo(manifestCorrect.getModJobNo());		
		String modType = Util.null2str(manifestCorrect.getModType());		
					
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		/** 관리번호 */
		tmpMap.put("gmtTime", manifestCorrect.getGmtTime());
		tmpMap.put("entCode", manifestCorrect.getEntCode());
		tmpMap.put("entBusinessCode", "");
		tmpMap.put("registerUserId", manifestCorrect.getRegisterUserId());
		tmpMap.put("updateUserId", manifestCorrect.getRegisterUserId());
		tmpMap.put("solution", "ECM");
		tmpMap.put("stockType", "MOD");
		tmpMap.put("stockSign", "MOD");
		tmpMap.put("stockSeqLength", 7);
		tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		tmpMap.put("stockRemark", "MOD-JOB NO");
		
		manifestCorrect.setModJobNo(commonMapper.selectNextStockMaxNo(tmpMap));
		//적하정정 데이터 insert 처리
		manifestMapper.insertCopyMblCorrect(manifestCorrect);
		manifestMapper.insertCopyHblCorrect(manifestCorrect);					
		//컨테이너 정정인 경우
		if ("99".equals(modType)) {
			manifestMapper.insertCopyContainerCorrect(manifestCorrect);
		}
				
		//관리번호 셋팅
		manifestCorrectSaveResult.setModJobNo(manifestCorrect.getModJobNo());
		
		return manifestCorrectSaveResult;
	}
	
	/**
	 * 적하목록 정정 결과수신요청
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult correctResultRequest(ManifestCorrect manifestCorrect) throws Exception {
ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		
		String fileName, ftpIP, ftpPassiveYN, ftpID, ftpPassword, ftpSvrRootRecv, ftpSRType, ftpSvrRoot;
		String dirPath[], tempRootPath[], tempSendReceivePath[];
		String messageCode = null, recvFileName;
		int ftpPort;
		boolean isFileReceive, recvDataFlag = false;
		boolean interfaceInfoFlag = false;
		boolean interfaceIncorrectFlag = false;
		List<File> receivefileList = new ArrayList<File>();
		List<String> errorList = new ArrayList<String>();
		File file, succFile;
		FTPConnector ftpConnector = null;
		SFTPConnector sftpConnector = null;
		
		/** 적하정정수신로그는 데몬에서만 처리 */
		/** 처리 로그 기록 */
//		Map<String, Object> tmpMap = new HashMap<String, Object>();
//		String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
//		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//		tmpMap.put("ent_code", manifestCorrect.getEntCode());
//		tmpMap.put("customer_code", "");
//		tmpMap.put("solution", "ECM");
//		tmpMap.put("bound", "COM");
//		tmpMap.put("bound_type", "COM");
//		tmpMap.put("hbl_no", "");
//		tmpMap.put("if_service_code", "MANIFEST-MOD-RECV");
//		tmpMap.put("if_doc_code", "MANIFEST_MODIFY");
//		tmpMap.put("if_doc_name", "적하목록정정");
//		tmpMap.put("if_doc_reference_no", "");
//		tmpMap.put("if_send_recv_type", "R");
//		tmpMap.put("if_status", "RECV_REQ");
//		tmpMap.put("if_status_info", "");
//		tmpMap.put("if_sender_id", "");
//		tmpMap.put("if_receiver_id", "");
//		tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
//		tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
//		tmpMap.put("if_remark", "");
//		tmpMap.put("register_user_id", manifestCorrect.getRegisterUserId());
//		tmpMap.put("register_user_name", manifestCorrect.getRegisterUserName());
//		tmpMap.put("register_date_time", System.currentTimeMillis());
//		tmpMap.put("es_job_no", logId);
//		IndexApi indexApi = new IndexApi(client);
//		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//		DocumentApi documentApi = new DocumentApi(client);
		
		Map<String, Object> interfaceMap = null;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("entCode", manifestCorrect.getEntCode());
		map.put("entBusinessCode", manifestCorrect.getEntBusinessCode());
		map.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		map.put("userLanguage", manifestCorrect.getUserLanguage());
		
		/** KTNET수신 */
		if("AIR".equals(manifestCorrect.getBound())) {
			List<String> ifServiceCodeList = Arrays.asList( "KTNET-ACPS-AIR-EXP-FHL", "KTNET-ACPS-AIR-IMP-FHL");
			map.put("ifServiceCodeIn", ifServiceCodeList);
		}else{
			List<String> ifServiceCodeList = Arrays.asList( "KTNET-MFCS-SEA-EXP-TXT-SFTP", "KTNET-MFCS-SEA-IMP-TXT-SFTP");
			map.put("ifServiceCodeIn", ifServiceCodeList);
		}
		map.put("srchEdiInterfaceUseYn", "Y");
		
		interfaceMap = manifestMapper.getEdiInterfaceInfo(map);
		if(interfaceMap != null && interfaceMap.size() > 0){
			interfaceInfoFlag = true;
			ftpIP = Util.null2str(interfaceMap.get("IF_SVR_IP"));
			if(ftpIP.indexOf("//", 0) != -1) {
				ftpIP = ftpIP.substring(ftpIP.indexOf("//", 0) + 2, ftpIP.length());
			}
			ftpPort = Util.null2int(interfaceMap.get("IF_SVR_IP_PORT"));
			ftpPassiveYN =  Util.null2str(interfaceMap.get("IF_SVR_IP_PASSIVE_YN"));			
			ftpID =  Util.null2str(interfaceMap.get("IF_SVR_ID"));			
			ftpPassword =  Util.null2str(interfaceMap.get("IF_SVR_PW"));
			ftpSvrRoot = Util.null2str(interfaceMap.get("IF_SVR_ROOT"));
			ftpSvrRootRecv =  Util.null2str(interfaceMap.get("IF_SVR_ROOT_RECV"));
			ftpSRType = Util.null2str(interfaceMap.get("IF_S_R_TYPE"));
			dirPath = ediCommonService.getSendFilePath("KTNET", "edi/manifestMod", map);
			if ("Y".equals(customProperties.getEdiSendYn())){
				try{
					if("SFTP".equals(ftpSRType)){
						sftpConnector = new SFTPConnector();
						messageCode = sftpConnector.connectSFTPForKTNET(ftpPort, ftpIP, ftpID, ftpPassword, "SA31");
					}else {
						ftpConnector = new FTPConnector();
						messageCode = ftpConnector.connectFTPForKTNET(21, ftpIP, ftpID, ftpPassword, ftpPassiveYN, "FL31");
					}
					if(!Util.null2str(messageCode).equals("")){
						map.put("srchCode", messageCode);
						errorList.add("KTNET : " + ediCommonService.getErrorCodeName(map));
					}else {
						if(!"".equals(Util.null2str(ftpSvrRoot))) {
							tempRootPath = ftpSvrRoot.split("/");
							ftpSvrRoot = "";
							for(int i = 0; i < tempRootPath.length; ++i) {
								if(!"".equals(tempRootPath[i])) {
									ftpSvrRoot = ftpSvrRoot + "/" + tempRootPath[i];
								}
							}
						}
						if(!"".equals(Util.null2str(ftpSvrRootRecv))) {
							tempSendReceivePath = ftpSvrRootRecv.split("/");
							ftpSvrRootRecv = "";
							for(int i = 0; i < tempSendReceivePath.length; ++i) {
								if(!"".equals(tempSendReceivePath[i])) {
									ftpSvrRootRecv = ftpSvrRootRecv + "/" + tempSendReceivePath[i];
								}
							}
						}
						
						if("SFTP".equals(ftpSRType)){
							if(sftpConnector.sftpMultiFileReceiveForKTnetNew(dirPath[2], ftpSvrRoot, ftpSvrRootRecv, receivefileList)) {
								isFileReceive = true;
							} else {
								isFileReceive = false;
							}
						}else {
							if(ftpConnector.ftpMultiFileReceiveForKTnet(dirPath[2], ftpSvrRoot, ftpSvrRootRecv, receivefileList, manifestCorrect.getRegisterUserId(), true)){
								isFileReceive = true;
							} else {
								isFileReceive = false;
							}
						}
						if(receivefileList.size() > 0){
							recvDataFlag = true;
//							tmpMap.put("if_vender", "KLNET");
//							tmpMap.put("if_send_recv_method", ftpSRType);
							for(File recvFile : receivefileList){
								recvFileName = recvFile.getName();
								if(recvFileName.endsWith(".xml")){
									recvFileName = recvFileName.replace(".xml", "_finish.xml");
								}else{
									recvFileName = recvFileName + "_finish.xml";
								}
								succFile = new File(dirPath[2], recvFileName);
								recvFile.renameTo(succFile);
								
//								documentApi.createDocument(index, logId, tmpMap);
							}
						}
						if(!isFileReceive){
							errorList.add("KTNET : FTP 파일 수신 실패");
						}
					}
					
					if(ftpConnector != null && ftpConnector.getFtpInfo() != null && ftpConnector.getFtpInfo().isConnected()){
						ftpConnector.disconnectFTP();
					}
					
					if(sftpConnector != null && sftpConnector.getConn() != null){
						sftpConnector.disConnectSFTPForKTNET();
					}
					
//					HttpServletRequest request = null;
//					ftpConnector.downloadFtpFileKTnet(ftpSvrRootRecv, fileName, request, dirPath[2]);
//					file = new File(dirPath[2] + fileName);
//					if(file.exists()){
//						succFile = new File(dirPath[2] + fileName.replace(".recv", ".dat"));
//						file.renameTo(succFile);
//					}
				}catch(Exception e){
					throw e;
				}finally{
					if(ftpConnector != null && ftpConnector.getFtpInfo() != null && ftpConnector.getFtpInfo().isConnected()){
						ftpConnector.disconnectFTP();
					}
					if(sftpConnector != null && sftpConnector.getConn() != null){
						messageCode = sftpConnector.disConnectSFTPForKTNET();
					}
					if(!Util.null2str(messageCode).equals("")) {
						interfaceIncorrectFlag = true;
					}
				}
			}
		}
		
		/** KLNET수신 */
		if("SEA".equals(manifestCorrect.getBound())) {
			List<String> ifServiceCodeList = Arrays.asList( "KLNET-PRISM-SEA-EXP-TXT", "KLNET-PRISM-SEA-IMP-TXT", "KLNET-PRISM-SEA-EXP-CUS", "KLNET-PRISM-SEA-IMP-CUS");
			map.put("ifServiceCodeIn", ifServiceCodeList);
			interfaceMap = manifestMapper.getEdiInterfaceInfo(map);
			if(interfaceMap != null && interfaceMap.size() > 0){
				interfaceInfoFlag = true;
				ftpIP = Util.null2str(interfaceMap.get("IF_SVR_IP"));
				if(ftpIP.indexOf("//", 0) != -1) {
					ftpIP = ftpIP.substring(ftpIP.indexOf("//", 0) + 2, ftpIP.length());
				}
				ftpPort = Util.null2int(interfaceMap.get("IF_SVR_IP_PORT"));
				ftpPassiveYN =  Util.null2str(interfaceMap.get("IF_SVR_IP_PASSIVE_YN"));			
				ftpID =  Util.null2str(interfaceMap.get("IF_SVR_ID"));			
				ftpPassword =  Util.null2str(interfaceMap.get("IF_SVR_PW"));
				ftpSvrRoot = Util.null2str(interfaceMap.get("IF_SVR_ROOT"));
				ftpSvrRootRecv =  Util.null2str(interfaceMap.get("IF_SVR_ROOT_RECV"));
				ftpSRType = Util.null2str(interfaceMap.get("IF_S_R_TYPE"));
				dirPath = ediCommonService.getSendFilePath("KLNET", "edi/manifestMod", map);
				if ("Y".equals(customProperties.getEdiSendYn())){
					try{
						if(ftpConnector == null){
							ftpConnector = new FTPConnector();
						}
						messageCode = ftpConnector.connectFTPForEDI(ftpPort, ftpIP, ftpID, ftpPassword, ftpPassiveYN);
						if(!Util.null2str(messageCode).equals("")){
							map.put("srchCode", messageCode);
							errorList.add("KLNET : " + ediCommonService.getErrorCodeName(map));
						}else {
							if(!"".equals(Util.null2str(ftpSvrRoot))) {
								tempRootPath = ftpSvrRoot.split("/");
								ftpSvrRoot = "";
								for(int i = 0; i < tempRootPath.length; ++i) {
									if(!"".equals(tempRootPath[i])) {
										ftpSvrRoot = ftpSvrRoot + "/" + tempRootPath[i];
									}
								}
							}
							if(!"".equals(Util.null2str(ftpSvrRootRecv))) {
								tempSendReceivePath = ftpSvrRootRecv.split("/");
								ftpSvrRootRecv = "";
								for(int i = 0; i < tempSendReceivePath.length; ++i) {
									if(!"".equals(tempSendReceivePath[i])) {
										ftpSvrRootRecv = ftpSvrRootRecv + "/" + tempSendReceivePath[i];
									}
								}
							}
							
							
							if(ftpConnector.ftpMultiFileReceiveForKLnet(dirPath[2], ftpSvrRoot, ftpSvrRootRecv, receivefileList, manifestCorrect.getRegisterUserId(), true)){
								isFileReceive = true;
							} else {
								isFileReceive = false;
							}
							
							if(receivefileList.size() > 0){
								recvDataFlag = true;
								for(File recvFile : receivefileList){
									recvFileName = recvFile.getName();
									if(recvFileName.endsWith(".xml")){
										recvFileName = recvFileName.replace(".xml", "_finish.xml");
									}else{
										recvFileName = recvFileName + "_finish.xml";
									}
									succFile = new File(dirPath[2], recvFileName);
									recvFile.renameTo(succFile);
								}
							}
							if(!isFileReceive){
								errorList.add("KLNET : FTP 파일 수신 실패");
							}
							
						}
						
						if(ftpConnector != null && ftpConnector.getFtpInfo() != null && ftpConnector.getFtpInfo().isConnected()){
							ftpConnector.disconnectFTP();
						}
					}catch(Exception e){
						throw e;
					}finally{
						if(ftpConnector != null && ftpConnector.getFtpInfo() != null && ftpConnector.getFtpInfo().isConnected()){
							ftpConnector.disconnectFTP();
						}
					}
				}
			}
		}
		if(!interfaceInfoFlag){	/** KTNET/KLNET interface관리 등록 안되어있을경우 */
			errorList.add("중계사업자정보가 없습니다.");
		}else if(interfaceIncorrectFlag) { /** KTNET interface관리에 등록되어 있는 정보가 부정확할 경우  */
			errorList.add("중계사업자정보를 확인해 주세요.");
		}else if(!recvDataFlag){
			errorList.add("수신내역이 없습니다.");
		}
		
		if(errorList.size() > 0){
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("errorList", errorList);
			manifestCorrectSaveResult.setResultData(resultData);
		}
		
		return manifestCorrectSaveResult;
	}
	
	/**
	 * 적하목록 항목정정 오더 업데이트
	 * @param entCode
	 * @param manifestCorrect
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult correctUpdate(String entCode, ManifestCorrect manifestCorrect) throws Exception {
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		Util.accessControlDatabase(manifestCorrect, "gmtTime", false, null, null, null, null);
		
		List<Map<String, Object>> updateList = manifestMapper.selectCorrectOrderList(manifestCorrect);
		for(Map<String, Object> updateMap : updateList) {
			if("".equals(Util.null2str(updateMap.get("MOD_NAME")))) {
				manifestCorrectSaveResult.setResultCode("E0046"); /** 변경할 자료가 없습니다. */
				return manifestCorrectSaveResult;
			}
		}
		for(Map<String, Object> updateMap : updateList) {
			updateMap.put("updateUserId", manifestCorrect.getUpdateUserId());
			updateMap.put("gmtTime", manifestCorrect.getGmtTime());
			updateMap.put("cryptographyYn", manifestCorrect.getCryptographyYn());
			manifestMapper.updateCorrectOrder(updateMap);
			// HBL 수정일 경우 추가적으로 HBL 업데이트해야할 테이블 작업 
			if("HBL_NO".equals(updateMap.get("MOD_NAME"))) {
				manifestMapper.updateHblInfo(updateMap);
				updateMap.put("hblNo", updateMap.get("AFTER_VALUE"));
				updateMap.put("odJobNo", updateMap.get("OD_JOB_NO"));
				orderMapper.updatePodBlNo(updateMap);
			}
		}
		return manifestCorrectSaveResult;
	}
	
	/**
	 * 엑셀업로드체크
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult excelUploadCheck(MultipartFile uploadExcel,String gubun, String entCode, String modJobNo) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
			manifestCorrectSaveResult.setResultCode("extError");
			return manifestCorrectSaveResult;
		}
		
		FileUploadResponse result = fileService.storeFile(uploadExcel, entCode, true);
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
			
//			if("DEL".equals(gubun)) {
//				fieldName =		new String[]{"hsn","hblNo","modType","modReason"};	
//				fieldTitle =	new String[]{"HSN","HBLNo","정정구분","정정사유"};
//				fieldLength =	new String[]{"4","20","2","200"};
//				fieldType =		new String[]{"S","S","S","S"};
//				modType = "G";
//			}else if("ADD".equals(gubun)) {
//				fieldName =		new String[]{"hsn","hblNo","modType","modReason","pieces","unitPieces","gwt","blTypeIert","commodityItem","shipperName","shipperAddress","consigneeName","consigneeAddress"};	
//				fieldTitle =	new String[]{"HSN","HBLNo","정정구분","정정사유","수량","포장단위","중량","화물구분","품명","송하인이름","송하인주소","수하인이름","수하인주소"};
//				fieldLength =	new String[]{"4","20","2","200","4","5","9","1","200","100","240","100","240"};
//				fieldType =		new String[]{"S","S","S","S","DBL","S","DBL","S","S","S","S","S","S"};
//				modType = "F";
//			}else if("UPD".equals(gubun)) {
			/** 항목정정만 엑셀 업로드 사용 */
				fieldName =		new String[]{"hblNo",	"singleModCode",	"beforeValue",	"afterValue"};	
				fieldTitle =	new String[]{"HBLNo",	"정정코드",				"정정전 내용",		"정정 내용"};
				fieldLength =	new String[]{"20",		"3",				"200",			"200"};
				fieldType =		new String[]{"S",		"S",				"S",			"S"};
//			}
			
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				Sheet sheet = wb.getSheetAt(0);
				DataFormatter df = new DataFormatter();
				int fieldSize = fieldName.length;
				StringBuffer errMsg = new StringBuffer();
				Map<String, Object> dataMap = null;
				String fieldValue = "";
				
				// 데이트 포맷
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				if(extension.equals("xlsx")){
					XSSFCell myCell = null;
					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
						if(sheet.getRow(i) == null){
							manifestCorrectSaveResult.setResultCode("A11216");	/** 항공오더에 컨테이너를 저장 할 수 없습니다. */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							manifestCorrectSaveResult.setResultData(resultData);
							return manifestCorrectSaveResult;
						}
											
						dataMap = new HashMap<String, Object>();
											
						//셀 셋팅
						for(int j = 0; j < fieldSize; j++){
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							if("DATE".equals(fieldType[j])) {
								String value = null;
								if(myCell!=null && myCell.getDateCellValue() !=null) {
									value = sdf.format(myCell.getDateCellValue());
								}
								fieldValue = this.dataChk(value, fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							else {
								fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							
							//필수값 체크
							if(j == 0) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[H.B/L No] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							else if(j == 1) {
								//정정코드 체크
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[정정코드] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							dataMap.put(fieldName[j], fieldValue);
						}
						
						//마스터정보 셋팅
						dataMap.put("modJobNo", modJobNo);
						dataList.add(dataMap);
					}
				}else{
					HSSFCell myCell = null;
					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
						if(sheet.getRow(i) == null){
							manifestCorrectSaveResult.setResultCode("A11216");	/** 항공오더에 컨테이너를 저장 할 수 없습니다. */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							manifestCorrectSaveResult.setResultData(resultData);
							return manifestCorrectSaveResult;
						}
						
						dataMap = new HashMap<String, Object>();
						
						//셀 셋팅
						for(int j = 0; j < fieldSize; j++){
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							if("DATE".equals(fieldType[j])) {
								String value = null;
								if(myCell!=null && myCell.getDateCellValue() !=null) {
									value = sdf.format(myCell.getDateCellValue());
								}
								fieldValue = this.dataChk(value, fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							else {
								fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							
							//필수값 체크
							if(j == 1) {
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[H.B/L No] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							else if(j == 2) {
								//정정코드 체크
								if(StringUtils.isEmpty(fieldValue)) {
									errMsg.append("[정정코드] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
		
							dataMap.put(fieldName[j], fieldValue);
						}
						
						//마스터정보 셋팅
						dataMap.put("modJobNo", modJobNo);
						dataList.add(dataMap);				
						
					}
				}
				
				if(dataList.size() ==0){
					manifestCorrectSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
					return manifestCorrectSaveResult;
				}
				
				if(errMsg.length() != 0){
					manifestCorrectSaveResult.setResultCode("dataErr");
					Map<String, Object> errMap = new HashMap<String, Object>();
					errMap.put("errStr", errMsg);
					manifestCorrectSaveResult.setResultData(errMap);
					return manifestCorrectSaveResult;
				}
				//TODO 파일삭제?
				
//				resultData.put("dataList", dataList);
				resultData.put("entCode", entCode);
				resultData.put("modJobNo", modJobNo);
//				//hbl 유효성 체크
//				Map<String, Object> rstHbl = manifestMapper.selectModHblValidate(resultData);
				
//				resultData.put("dataSize", dataList.size());
				
//				if(rstHbl!=null) {
//					//유효하지않은 BL
//					resultData.put("NOT_BL_CNT", rstHbl.get("NOT_BL_CNT"));
//					resultData.put("NOT_HBL_LIST", rstHbl.get("NOT_HBL_LIST"));
//				}
//				else {
//					resultData.put("NOT_BL_CNT", "0");
//					resultData.put("NOT_HBL_LIST", "");				
//				}

				//hbl 유효성 체크 - 최대 매개변수 오류로 인해 100건씩 체크
				int notBlCnt = 0; 
				String notHblList = "";
				List<Map<String, Object>> tmpList = new ArrayList<Map<String, Object>>();
				
				for(int i=0; i<dataList.size(); i++) {
					tmpList.add(dataList.get(i));
					
					if(tmpList.size() % 100 == 0 || i == dataList.size() - 1) {
						resultData.put("dataList", tmpList);
						Map<String, Object> rstHbl = manifestMapper.selectModHblValidate(resultData);
						
						if(rstHbl != null) {
							notBlCnt += (int)rstHbl.get("NOT_BL_CNT");
							notHblList += (String) rstHbl.get("NOT_HBL_LIST");
							rstHbl.clear();
						}
						tmpList.clear();
					}
				}
				
				resultData.put("dataList", dataList);
				resultData.put("dataSize", dataList.size());
				
				resultData.put("NOT_BL_CNT", notBlCnt);
				resultData.put("NOT_HBL_LIST", !notHblList.equals("") ? notHblList.substring(3) : notHblList);
				
				manifestCorrectSaveResult.setResultData(resultData);
				manifestCorrectSaveResult.setResultCode("200");
				
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
			manifestCorrectSaveResult.setResultCode("ERROR");
		}
		return manifestCorrectSaveResult;
	}

	/**
	 * 엑셀업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult excelUploadUpdate(ManifestCorrect manifestCorrect) throws Exception{
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		// 최대 매개변수 오류로 인해 100건씩 처리
		List<ManifestOrder> odList = manifestCorrect.getOdList();
		List<ManifestOrder> tmpOdList = new ArrayList<ManifestOrder>();
		for(int i=0; i<odList.size(); i++) {
			tmpOdList.add(odList.get(i));
			if(tmpOdList.size() % 100 == 0 || i == odList.size() - 1) {
				manifestCorrect.setOdList(tmpOdList);
				manifestMapper.updateModUpdateExcel(manifestCorrect);
				tmpOdList.clear();
			}
		}
		manifestCorrect.setOdList(odList);
		return manifestCorrectSaveResult;
	}
	/**
	 * 엑셀 타입 체크
	 * @param data
	 * @param name
	 * @param length
	 * @param type
	 * @param errMsg
	 * @param row
	 * @return
	 * @throws Exception
	 */
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
	//				int idx = data.indexOf("E");	/** 8자리가 넘어가면 123456789.123 => 1.23456789123E8 로 표현됨 */
	//				if(idx > -1){
	//					int k = Util.null2int(data.substring(idx + 1)) + 1;
	//					data = data.substring(0, idx).replace(".", "");
	//					data = data.substring(0, k) + "." + data.substring(k);
	//				}
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
	//				if(!data.startsWith("2")){
	//					String[] tmpData = data.split("/");
	//					data = "20" + tmpData[2] + (tmpData[0].length() == 1 ? "0" + tmpData[0] : tmpData[0]) + tmpData[1];
	//				}else{
						SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyyMMdd");
						data = data.replaceAll("[-./]", "").substring(0, 8);
						data = sdFormat.format(sdFormat2.parse(data));
	//				}
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
	//		int idx = data.indexOf("E");	/** 8자리가 넘어가면 123456789.123 => 1.23456789123E8 로 표현됨 */
	//		if(idx > -1){
	//			int k = Util.null2int(data.substring(idx + 1)) + 1;
	//			data = data.substring(0, idx).replace(".", "");
	//			data = data.substring(0, k) + "." + data.substring(k);
	//		}
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
				if(!"pass".equals(length) && data.getBytes("euc-kr").length > Util.null2int(length)) {
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
	
	
    public boolean cntrNoDupChk(ManifestCorrect manifestCorrect, String cntrModType, String cntrModCode) throws Exception {
    	List<Map<String, Object>> dupChkList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		if(Util.null2str(manifestCorrect.getBoundType()).equals("EXP")){
			if(cntrModType.equals("A")){
				//(save)신규컨테이너번호 / (old)정정전컨테이너번호
				tmpMap.put("chkValue", manifestCorrect.getCntrNo());
				tmpMap.put("chkField", "BEFORE_VALUE");
				tmpMap.put("chkCntrModType", "X");
				dupChkList.add(tmpMap);
				//(save)신규컨테이너번호 / (old)삭제컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getCntrNo());
				tmpMap.put("chkField", "CNTR_NO");
				tmpMap.put("chkCntrModType", "D");
				dupChkList.add(tmpMap);
			}else if(cntrModType.equals("X")){
				//(save)정정전컨테이너번호 / (old)정정후컨테이너번호
				tmpMap.put("chkValue", manifestCorrect.getBeforeValue());
				tmpMap.put("chkField", "AFTER_VALUE");
				tmpMap.put("chkCntrModType", "X");
				dupChkList.add(tmpMap);
				//(save)정정후컨테이너번호 / (old)정정전컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getAfterValue());
				tmpMap.put("chkField", "BEFORE_VALUE");
				tmpMap.put("chkCntrModType", "X");
				dupChkList.add(tmpMap);
				//(save)정정전컨테이너번호 / (old)정정전컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getBeforeValue());
				tmpMap.put("chkField", "BEFORE_VALUE");
				tmpMap.put("chkCntrModType", "X");
				dupChkList.add(tmpMap);
				//(save)정정전컨테이너번호 / (old)신규컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getBeforeValue());
				tmpMap.put("chkField", "CNTR_NO");
				tmpMap.put("chkCntrModType", "A");
				dupChkList.add(tmpMap);
				//(save)정정전컨테이너번호 / (old)삭제컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getBeforeValue());
				tmpMap.put("chkField", "CNTR_NO");
				tmpMap.put("chkCntrModType", "D");
				dupChkList.add(tmpMap);
				//(save)정정후컨테이너번호 / (old)삭제컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getAfterValue());
				tmpMap.put("chkField", "CNTR_NO");
				tmpMap.put("chkCntrModType", "D");
				dupChkList.add(tmpMap);
			}else if(cntrModType.equals("D")){
				//(save)삭제컨테이너번호 / (old)정정전컨테이너번호
				tmpMap.put("chkValue", manifestCorrect.getCntrNo());
				tmpMap.put("chkField", "BEFORE_VALUE");
				tmpMap.put("chkCntrModType", "X");
				dupChkList.add(tmpMap);
				//(save)삭제컨테이너번호 / (old)정정후컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getCntrNo());
				tmpMap.put("chkField", "AFTER_VALUE");
				tmpMap.put("chkCntrModType", "X");
				dupChkList.add(tmpMap);
				//(save)삭제컨테이너번호 / (old)신규컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getCntrNo());
				tmpMap.put("chkField", "CNTR_NO");
				tmpMap.put("chkCntrModType", "A");
				dupChkList.add(tmpMap);
			}
		}else{
			if(cntrModType.equals("A")){
				//(save)신규컨테이너번호 / (old)정정전컨테이너번호(컨테이너번호정정 이외의 항목도포함)
				tmpMap.put("chkValue", manifestCorrect.getCntrNo());
				tmpMap.put("chkField", "CNTR_NO");
				tmpMap.put("chkCntrModType", "U");
				dupChkList.add(tmpMap);
				//(save)신규컨테이너번호 / (old)삭제컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getCntrNo());
				tmpMap.put("chkField", "CNTR_NO");
				tmpMap.put("chkCntrModType", "D");
				dupChkList.add(tmpMap);
			}else if(cntrModType.equals("U")){
				if(cntrModCode.equals("EH")){
					//(save)정정전컨테이너번호 / (old)정정후컨테이너번호
					tmpMap.put("chkValue", manifestCorrect.getCntrNo());
					tmpMap.put("chkField", "AFTER_VALUE");
					tmpMap.put("chkCntrModType", "U");
					tmpMap.put("chkCntrModCode", "EH");
					dupChkList.add(tmpMap);
					//(save)정정후컨테이너번호 / (old)정정전컨테이너번호(컨테이너번호정정 이외의 항목도포함)
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("chkValue", manifestCorrect.getAfterValue());
					tmpMap.put("chkField", "CNTR_NO");
					tmpMap.put("chkCntrModType", "U");
					dupChkList.add(tmpMap);
					//(save)정정전컨테이너번호 / (old)정정전컨테이너번호
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("chkValue", manifestCorrect.getCntrNo());
					tmpMap.put("chkField", "CNTR_NO");
					tmpMap.put("chkCntrModType", "U");
					tmpMap.put("chkCntrModCode", "EH");
					dupChkList.add(tmpMap);
					//(save)정정전컨테이너번호 / (old)신규컨테이너번호
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("chkValue", manifestCorrect.getCntrNo());
					tmpMap.put("chkField", "CNTR_NO");
					tmpMap.put("chkCntrModType", "A");
					dupChkList.add(tmpMap);
					//(save)정정전컨테이너번호 / (old)삭제컨테이너번호
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("chkValue", manifestCorrect.getCntrNo());
					tmpMap.put("chkField", "CNTR_NO");
					tmpMap.put("chkCntrModType", "D");
					dupChkList.add(tmpMap);
					//(save)정정후컨테이너번호 / (old)삭제컨테이너번호
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("chkValue", manifestCorrect.getAfterValue());
					tmpMap.put("chkField", "CNTR_NO");
					tmpMap.put("chkCntrModType", "D");
					dupChkList.add(tmpMap);
				}else{
					//(save)선택컨테이너번호 / (old)신규컨테이너번호
					tmpMap.put("chkValue", manifestCorrect.getCntrNo());
					tmpMap.put("chkField", "CNTR_NO");
					tmpMap.put("chkCntrModType", "A");
					dupChkList.add(tmpMap);
					//(save)선택컨테이너번호 / (old)삭제컨테이너번호
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("chkValue", manifestCorrect.getCntrNo());
					tmpMap.put("chkField", "CNTR_NO");
					tmpMap.put("chkCntrModType", "D");
					dupChkList.add(tmpMap);
					//(save)선택컨테이너번호 / (old)정정전컨테이너번호
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("chkValue", manifestCorrect.getCntrNo());
					tmpMap.put("chkField", "CNTR_NO");
					tmpMap.put("chkCntrModType", "U");
					tmpMap.put("chkCntrModCode", "EH");
					dupChkList.add(tmpMap);
					//(save)선택컨테이너번호 / (old)정정후컨테이너번호
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("chkValue", manifestCorrect.getCntrNo());
					tmpMap.put("chkField", "AFTER_VALUE");
					tmpMap.put("chkCntrModType", "U");
					tmpMap.put("chkCntrModCode", "EH");
					dupChkList.add(tmpMap);
				}
			}else if(cntrModType.equals("D")){
				//(save)삭제컨테이너번호 / (old)정정전컨테이너번호(컨테이너번호정정 이외의 항목도포함)
				tmpMap.put("chkValue", manifestCorrect.getCntrNo());
				tmpMap.put("chkField", "CNTR_NO");
				tmpMap.put("chkCntrModType", "U");
				dupChkList.add(tmpMap);
				//(save)삭제컨테이너번호 / (old)정정후컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getCntrNo());
				tmpMap.put("chkField", "AFTER_VALUE");
				tmpMap.put("chkCntrModType", "U");
				tmpMap.put("chkCntrModCode", "EH");
				dupChkList.add(tmpMap);
				//(save)삭제컨테이너번호 / (old)신규컨테이너번호
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("chkValue", manifestCorrect.getCntrNo());
				tmpMap.put("chkField", "CNTR_NO");
				tmpMap.put("chkCntrModType", "A");
				dupChkList.add(tmpMap);
			}
		}
		tmpMap = new HashMap<String, Object>();
		tmpMap.put("modJobNo", manifestCorrect.getModJobNo());
		tmpMap.put("seqNoId", manifestCorrect.getSeqNoId());
		tmpMap.put("dupChkList", dupChkList);
		tmpMap = manifestMapper.selectModCntrDupChk(tmpMap);
		if(tmpMap != null && tmpMap.size() > 0){
			return false;
		}
    	return true;
    }
    
    /**
	 * 적하목록 목록신고제출번호 조회
	 * @param destPort
	 * @return
	 * @throws Exception
	 */
	public ManifestOrderSaveResult getSimpleDocNo(SearchManifestSimpleDocNo search) throws Exception {
		ManifestOrderSaveResult result = new ManifestOrderSaveResult();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> dataMap = null;
		Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);
		
		if(!Util.isEmpty(search.getOdJobNo())){
			tmpMap.put("masterOdJobNo", search.getOdJobNo());
			tmpMap.put("simpleDocNo", search.getSimpleDocNo());
			dataMap = commonMapper.selectSimpleDocNoInfo(tmpMap);
			if(dataMap != null) {
				if(Util.null2str(dataMap.get("SIMPLE_DOC_NO_SEND_YN")).equals("Y")){
					result.setResultCode("A11333");	/** 통관목록이 전송되어서 처리할 수 없습니다. */
					return result;
				}
			}
		}
		tmpMap.clear();			
		
		if(search.getBoundType().equals("EXP")){
			tmpMap.put("srchEdiCityPort", search.getOrigCode());
			if(!Util.isEmpty(search.getSimpleDocNoSeqNo())){
				tmpMap.put("dateLength", "4");
				tmpMap.put("stockDateFormat", "112");
			}
		}else{
			tmpMap.put("srchEdiCityPort", search.getDestPort());
			if(!Util.isEmpty(search.getSimpleDocNoSeqNo())){
				tmpMap.put("dateLength", "2");
				tmpMap.put("stockDateFormat", "12");
			}
		}				
		tmpMap.put("srchEntCode", search.getEntCode());
		tmpMap.put("srchEntBusinessCode", search.getEntBusinessCode());
		tmpMap.put("srchBound", search.getBound());
		Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmpMap);
		if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
			String documentYn = null, tmpDocumentYn = null, ediExpressSign = null;
			if("N".equals(dataMap.get("DOCUMENT_YN_MAX"))){
				documentYn = "N";
				tmpDocumentYn = "N";
				ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"));
			}else if("Y".equals(dataMap.get("DOCUMENT_YN_MIN"))){
				documentYn = "Y";
				tmpDocumentYn = "Y";
				ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
			}else{
				documentYn = "YN";
				tmpDocumentYn = "N";
				ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"), Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y")));
			}
			if(Util.isEmpty(ediExpressSign)){
				result.setResultCode("busEdiEcmNull");
			}else{
				if(!Util.isEmpty(search.getSimpleDocNoSeqNo())){
					result.setResultCode(ediExpressSign + Util.null2str(busEdiEcmMap.get("STOCK_ISSUE_YYMM")) + Util.lpad(Util.null2str(search.getSimpleDocNoSeqNo()), 8, '0'));
				}else{
					tmpMap.put("entCode", search.getEntCode());
					tmpMap.put("gmtTime", search.getGmtTime());
					tmpMap.put("entBusinessCode", "");
					tmpMap.put("updateUserId", search.getUserId());
					tmpMap.put("solution", "ECM");
					tmpMap.put("stockType", "OD");
					tmpMap.put("stockSign", ediExpressSign);
					tmpMap.put("stockSeqLength", "8");
					tmpMap.put("signType", "noHyphen");
					tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
					tmpMap.put("stockRemark", "ORDER SIMPLE DOC NO");
					if(search.getBoundType().equals("EXP")){
						tmpMap.put("dateLength", "4");
						tmpMap.put("stockDateFormat", "112");
					}else{
						tmpMap.put("dateLength", "2");
						tmpMap.put("stockDateFormat", "12");
					}
					
					if("YN".equals(documentYn)){
						result.setResultCode("updateDocumentYn");
						Map<String, Object> resultData = new HashMap<String, Object>();
						resultData.put("simpleDocNo", commonMapper.selectNextStockMaxNo(tmpMap));
						if(!Util.isEmpty(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"))){
							resultData.put("simpleDocNoDocumentN", resultData.get("simpleDocNo"));
							if(!Util.isEmpty(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"))){
								tmpMap.put("stockSign", busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
								resultData.put("simpleDocNoDocumentY", commonMapper.selectNextStockMaxNo(tmpMap));
							}
						}else{
							resultData.put("simpleDocNoDocumentY", resultData.get("simpleDocNo"));
						}
						result.setResultData(resultData);
					}else{
						result.setResultCode(commonMapper.selectNextStockMaxNo(tmpMap));
					}
				}
			}
		}else{
			result.setResultCode("busEdiEcmNull");
		}
		return result;
	}
	
	/**
	 * 적하목록수입 목록신고제출번호 수정
	 * @param destPort
	 * @return
	 * @throws Exception
	 */
	/** 사용안함 */
	public ManifestOrderSaveResult updateSimpleDocNo(SearchManifestSimpleDocNo search) throws Exception {
		ManifestOrderSaveResult result = new ManifestOrderSaveResult();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Util.accessControlDatabase(search, "gmtTime|dateFormat", false, null, null, null, null);
		tmpMap.put("srchEdiCityPort", search.getDestPort());
		tmpMap.put("srchEntCode", search.getEntCode());
		tmpMap.put("srchEntBusinessCode", search.getEntBusinessCode());
		tmpMap.put("srchBound", search.getBound());
		
		tmpMap.put("stockDateFormat", "12");
		tmpMap.put("dateLength", "2");
		Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmpMap);
		if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
			tmpMap.put("entCode", search.getEntCode());
			tmpMap.put("gmtTime", search.getGmtTime());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("updateUserId", search.getUserId());
			tmpMap.put("solution", "ECM");
			tmpMap.put("stockType", "OD");
			tmpMap.put("stockSign", busEdiEcmMap.get("EDI_EXPRESS_SIGN"));
			tmpMap.put("stockSeqLength", "8");
			tmpMap.put("signType", "noHyphen");
			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", "ORDER SIMPLE DOC NO");
			tmpMap.put("updateMaxNo", search.getSimpleDocNoSeqNo());
			
			
			if(commonMapper.updateNextStockMaxNo(tmpMap) == 0) {
				if(commonMapper.selectStockMaxNo(tmpMap) == null){
					commonMapper.insertStockMaxNo(tmpMap);
				}else{
					result.setResultCode("A11331");	/** 마지막 제출번호보다 작은번호는 사용할 수 없습니다. */
				}
			}
			if(Util.null2str(result.getResultCode()).equals("")){
				Order order = new Order();
				order.setSimpleDocNo(Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN")) + Util.null2str(busEdiEcmMap.get("STOCK_ISSUE_YYMM")) + Util.lpad(Util.null2str(search.getSimpleDocNoSeqNo()), 8, '0'));
				order.setOldSimpleDocNo(search.getSimpleDocNo());
				order.setOdJobNo(search.getOdJobNo());
				manifestMapper.updateSimpleDocNo(order);
			}
		}else{
			result.setResultCode("A11271");	/** 관세청 EDI정보가 없습니다. */
		}
		return result;
	}
	
	/**
	 * 적하목록수입 목록신고제출번호 수정
	 * @param destPort
	 * @return
	 * @throws Exception
	 */
	public ManifestOrderSaveResult businessFinish(SendDelivery sendDelivery) throws Exception {
		ManifestOrderSaveResult result = new ManifestOrderSaveResult();
		Util.accessControlDatabase(sendDelivery, "gmtTime", false, null, null, null, null);
		manifestMapper.updateBusinessFinish(sendDelivery);
		return result;
	}
	
	/**
	 * 신고세관/과
	 * @param destPort
	 * @return
	 * @throws Exception
	 */
	public ManifestCorrectSaveResult getCustomsCode(String entCode, SearchManifestCustoms searchCustoms) throws Exception {
		ManifestCorrectSaveResult manifestCorrectSaveResult = new ManifestCorrectSaveResult();
		
		Util.accessControlDatabase(searchCustoms, "userLanguage", false, null, null, null, null);
		Map<String, Object> res = manifestMapper.getCustomsCode(searchCustoms);
		
		manifestCorrectSaveResult.setResultData(res);
		
		return manifestCorrectSaveResult;
	}
	
	
	/**
	 * 개인통과오류건 제출번호 분리
	 * @param Order
	 * @return
	 * @throws Exception
	 */
	public ManifestSeparationResult customsErrSeparation(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		ManifestSeparationResult manifestSeparationResult = new ManifestSeparationResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> result = new HashMap<String, Object>();
		Util.accessControlDatabase(sendManifest, "gmtTime", false, null, null, null, null);
		
		/** 2024-09-23 제출번호 없는건 선택시 실행불가하도록 요청      해당내용 변경없으면 문서여부별 처리내용 불필요 */
		if(Util.isEmpty(sendManifest.getSimpleDocNo())){
			manifestSeparationResult.setResultCode("useExistingNo");
			return manifestSeparationResult;
		}
		
		//새로운 제출번호 값 조회하기 위한 파라미터 세팅
		Map<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("srchEntCode",sendManifest.getEntCode());
		tmp.put("srchEntBusinessCode",sendManifest.getEntBusinessCode());
		tmp.put("srchBound",sendManifest.getBound());
		tmp.put("srchEdiCityPort", sendManifest.getDestPort());
		Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmp);
		if(busEdiEcmMap == null){
			manifestSeparationResult.setResultCode("noEdiData");
			return manifestSeparationResult;
		}
		
		boolean documentY = false, documentN = false;
		Map<String, Object> separationInfo = manifestMapper.getSeparationInfo(sendManifest);
		if(Util.null2int(separationInfo.get("DOCUMENT_Y_ERROR_CNT")) == 0 && Util.null2int(separationInfo.get("DOCUMENT_N_ERROR_CNT")) == 0){
			//제출번호 분리시킬 하우스가 없는 경우 에러 메시지 반환
			manifestSeparationResult.setResultCode("noList");
		}else{
			if(Util.null2int(separationInfo.get("DOCUMENT_Y_ERROR_CNT")) > 0){
				if(Util.null2int(separationInfo.get("DOCUMENT_Y_CNT")) == Util.null2int(separationInfo.get("DOCUMENT_Y_ERROR_CNT"))){
					//하우스 전체가 분리조건에 해당할 때는 제출번호 분리시키지 않고 기존 제출번호로 남아있어야 함.
					manifestSeparationResult.setResultCode("useExistingNo");
				}else if(Util.isEmpty(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"))){
					manifestSeparationResult.setResultCode("noEdiData");
				}else{
					documentY = true;
				}
			}
			if(Util.null2int(separationInfo.get("DOCUMENT_N_ERROR_CNT")) > 0){
				if(Util.null2int(separationInfo.get("DOCUMENT_N_CNT")) == Util.null2int(separationInfo.get("DOCUMENT_N_ERROR_CNT"))){
					//하우스 전체가 분리조건에 해당할 때는 제출번호 분리시키지 않고 기존 제출번호로 남아있어야 함.
					manifestSeparationResult.setResultCode("useExistingNo");
				}else if(Util.isEmpty(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"))){
					manifestSeparationResult.setResultCode("noEdiData");
				}else{
					documentN = true;
				}
			}
		}
		
		if(!documentY && !documentN){
			return manifestSeparationResult;
		}
		
		//제출번호 분리할 하우스 리스트 조회
		List<Map<String,String>> list = manifestMapper.getSeparationList(sendManifest);
		
		//22.04.14 장부장님 요청으로 하우스 선택 후 제출번호 분리 막음
		
		/** 2025-04-18 사업장 시스템 옵션에 따라 제출번호 생성/삭제 설정추가 */
		tmp.clear();
		tmp.put("entCode", sendManifest.getEntCode());
		tmp.put("entBusinessCode", sendManifest.getEntBusinessCode());
		tmp.put("solution", "ECM");
		tmp.put("optionCode", "C14");
		String delSimpleDocNoYn = commonMapper.selectSystemOption(tmp);
		
		
		/** 업데이트할 제출번호값 조회 */
		tmp.clear();
		tmp.put("entCode", sendManifest.getEntCode());
//		tmp.put("entBusinessCode",sendManifest.getEntBusinessCode());
		tmp.put("entBusinessCode","");
		tmp.put("gmtTime",sendManifest.getGmtTime());
		tmp.put("updateUserId",sendManifest.getUpdateUserId());
		tmp.put("solution", "ECM");
		tmp.put("stockType", "OD");
		tmp.put("stockSeqLength", "8");
		tmp.put("signType", "noHyphen");
		tmp.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		tmp.put("stockRemark", "ORDER SIMPLE DOC NO");
		tmp.put("dateLength", "2");
		tmp.put("stockDateFormat", "12");
		
		int resCnt = 0;
		if(documentY){
			if("Y".equals(delSimpleDocNoYn)){
				sendManifest.setNewSimpleDocNo(null);
			}else{
				tmp.put("stockSign", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y")));
				//새로운 제출번호 값 조회
				sendManifest.setNewSimpleDocNo(commonMapper.selectNextStockMaxNo(tmp));
			}
			sendManifest.setDocumentYn("Y");
			if(Util.isEmpty(separationInfo.get("SIMPLE_DOC_NO")) && !documentN){
				sendManifest.setAction("updateMaster");
				resCnt--;
			}
			resCnt += manifestMapper.customsErrSeparation(sendManifest);
		}
		if(documentN){
			if("Y".equals(delSimpleDocNoYn)){
				sendManifest.setNewSimpleDocNo(null);
			}else{
				tmp.put("stockSign", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N")));
				//새로운 제출번호 값 조회
				sendManifest.setNewSimpleDocNo(commonMapper.selectNextStockMaxNo(tmp));
			}
			sendManifest.setDocumentYn("N");
			if(Util.isEmpty(separationInfo.get("SIMPLE_DOC_NO"))){
				sendManifest.setAction("updateMaster");
				resCnt--;
			}
			resCnt += manifestMapper.customsErrSeparation(sendManifest);
		}
		result.put("count", resCnt);
		manifestSeparationResult.setResultData(result);
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", sendManifest.getEntCode());
		tmpLogMap.put("ent_business_code", sendManifest.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", sendManifest.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		
		// 사업장명 가져오기
		if (!"".equals(Util.null2str(sendManifest.getEntBusinessCode()))) {
			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(sendManifest.getEntBusinessCode());
			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
		}
		
		// 사용자명 가져오기
		if (!"".equals(Util.null2str(sendManifest.getUpdateUserId()))) {
			User user = smsUserService.getUser(sendManifest.getRegisterUserId());
			tmpLogMap.put("process_user_name", user.getUserName());
			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
		}
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		for(int i=0; i<list.size(); i++) {
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(i));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("bound", sendManifest.getBound());
			logMap.put("bound_type", sendManifest.getBoundType());
			logMap.put("job_no", Util.null2str(list.get(i).get("OD_JOB_NO")));
			logMap.put("description", "적하/목록신고 > 개인통관 오류건 제출번호분리 " +" [MBL_NO : " + sendManifest.getMblNo() + " HBL_NO : "+ list.get(i).get("HBL_NO") +"]");
//			logMap.put("job_log", new Gson().toJson(sendManifest));
			logMap.put("job_log", "");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
			
		}
		
		commonService.insertLogBulk(bulkList);
		return manifestSeparationResult;
	}

	/**
	 * 제출번호 분리
	 * @param sendManifest
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public ManifestSeparationResult separationSimpleDocNo(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		ManifestSeparationResult manifestSeparationResult = new ManifestSeparationResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> result = new HashMap<String, Object>();
		Util.accessControlDatabase(sendManifest, "gmtTime", false, null, null, null, null);
		
		int resCnt = 0;
		
		if(Util.isEmpty(sendManifest.getSimpleDocNo())){
			manifestSeparationResult.setResultCode("useExistingNo");
			return manifestSeparationResult;
		}
		
		//새로운 제출번호 값 조회하기 위한 파라미터 세팅
		Map<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("srchEntCode",sendManifest.getEntCode());
		tmp.put("srchEntBusinessCode",sendManifest.getEntBusinessCode());
		tmp.put("srchBound",sendManifest.getBound());
		tmp.put("srchEdiCityPort", sendManifest.getDestPort());
		Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmp);
		if(busEdiEcmMap == null){
			manifestSeparationResult.setResultCode("noEdiData");
			return manifestSeparationResult;
		}
		
		//새로운 제출번호 값 조회하기 위한 파라미터 세팅
		tmp.put("entCode", sendManifest.getEntCode());
//		tmp.put("entBusinessCode",sendManifest.getEntBusinessCode());
		tmp.put("entBusinessCode","");
		tmp.put("gmtTime",sendManifest.getGmtTime());
		tmp.put("updateUserId",sendManifest.getUpdateUserId());
		tmp.put("solution", "ECM");
		tmp.put("stockType", "OD");
		tmp.put("stockSeqLength", "8");
		tmp.put("signType", "noHyphen");
		tmp.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		tmp.put("stockRemark", "ORDER SIMPLE DOC NO");
		tmp.put("dateLength", "2");
		tmp.put("stockDateFormat", "12");
		
		if(!Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y")).equals("")){
			tmp.put("stockSign", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y")));
		}else if(!Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N")).equals("")){
			tmp.put("stockSign", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N")));
		}
		
		//제출번호 분리할 하우스 리스트 조회
		List<Map<String,String>> list = manifestMapper.selectSparationSimpleDocNoList(sendManifest);
		
		sendManifest.setNewSimpleDocNo(commonMapper.selectNextStockMaxNo(tmp));
		
		resCnt = manifestMapper.updateSimpleDocNoList(sendManifest);
		result.put("count", resCnt);
		manifestSeparationResult.setResultData(result);
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", sendManifest.getEntCode());
		tmpLogMap.put("ent_business_code", sendManifest.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", sendManifest.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		
		// 사업장명 가져오기
		if (!"".equals(Util.null2str(sendManifest.getEntBusinessCode()))) {
			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(sendManifest.getEntBusinessCode());
			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
		}
		
		// 사용자명 가져오기
		if (!"".equals(Util.null2str(sendManifest.getUpdateUserId()))) {
			User user = smsUserService.getUser(sendManifest.getRegisterUserId());
			tmpLogMap.put("process_user_name", user.getUserName());
			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
		}
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		for(int i=0; i<list.size(); i++) {
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(i));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("bound", sendManifest.getBound());
			logMap.put("bound_type", sendManifest.getBoundType());
			logMap.put("job_no", Util.null2str(list.get(i).get("OD_JOB_NO")));
			logMap.put("description", "적하/목록신고 > 하우스 5000건 이상 제출번호분리 " +" [MBL_NO : " + sendManifest.getMblNo() + " HBL_NO : "+ list.get(i).get("HBL_NO") +"]");
//			logMap.put("job_log", new Gson().toJson(sendManifest));
			logMap.put("job_log", "");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
			
		}
		
		commonService.insertLogBulk(bulkList);
		return manifestSeparationResult;
	}
	
	/**
	 * HSN 재생성
	 * @param sendManifest
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public ManifestOrderSaveResult reHsn(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
		
		if(manifestMapper.selectReHsnInfo(sendManifest) > 0){
			manifestOrderSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
			return manifestOrderSaveResult;
		}
		
		consolMapper.updateHsnOrderClose(Util.null2str(sendManifest.getMasterOdJobNo()));
		
		ProcessLogs processMblLogs = new ProcessLogs();
		processMblLogs.setJobFuncType("UPDATE");
		processMblLogs.setEntCode(sendManifest.getEntCode());
		processMblLogs.setEntBusinessCode(sendManifest.getEntBusinessCode());
		processMblLogs.setSolution("ECM");
		processMblLogs.setBound(sendManifest.getBound());
		processMblLogs.setBoundType(sendManifest.getBoundType());
		processMblLogs.setProcessUserId(sendManifest.getUpdateUserId());
		processMblLogs.setJobNo(sendManifest.getMasterOdJobNo());
		processMblLogs.setLogJobType("DATA");
		processMblLogs.setDescription("RE-HSN[MBL_NO : " + sendManifest.getMblNo()+"]");
		processMblLogs.setJobLog(new Gson().toJson(sendManifest));
		processMblLogs.setResult("ok");
		processMblLogs.setEntBusinessPrintName(sendManifest.getLogUserBusinessPrintName());
		processMblLogs.setProcessUserName(sendManifest.getLogUserName());
		processMblLogs.setProcessUserNameEng(sendManifest.getLogUserNameEng());
		commonService.insertLog(processMblLogs, request);
		
		return manifestOrderSaveResult;
	}
	
	/**
	 * MRN/MSN 수동 업데이트
	 * MRN_RESULT_TYPE 'M'으로 변경하여 먼저 배치 돌게 만듦
	 * @param sendManifest
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public ManifestOrderSaveResult mrnMsnManualUpdate(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
		manifestMapper.updateMrnResultTypeManual(sendManifest);
		return manifestOrderSaveResult;
	}

	/**
	 * 목록건 수하인정보 매칭처리
	 * @param sendManifest
	 * @param request
	 * @return
	 */
	public ManifestOrderSaveResult manifestConsigneeMatching(SendManifest sendManifest, HttpServletRequest request) {
		ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
		
		// 목록이면서 개인통관부호가 없는 데이터 조회
		SqlSession sqlSession = null;
		Map<String,Object> srchMap = new HashMap<String, Object>();
		List<Map<String,Object>> updateList = new ArrayList<Map<String,Object>>();
		
		if(sendManifest.getMasterOdJobNo() != null) {
			srchMap.put("masterOdJobNo", sendManifest.getMasterOdJobNo());
		}else if(sendManifest.getHouseOdJobNoList() != null) {
			srchMap.put("houseOdJobNoList", sendManifest.getHouseOdJobNoList());
		}else {
			Map<String, Object> resultData = new HashMap<String, Object>();
			manifestOrderSaveResult.setResultCode("error");
			resultData.put("errorStr", "조회할 데이터가 없습니다.");
			manifestOrderSaveResult.setResultData(resultData);
			return manifestOrderSaveResult;
		}
		
		LocalDate today = LocalDate.now();
        // 1년 전 날짜
        LocalDate oneYearAgo = today.minusYears(1);
        // 원하는 포맷 설정 (예: yyyy-MM-dd)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String endDate = today.format(formatter);
        String startDate = oneYearAgo.format(formatter);
        
        // 데이터 조회
		List<Map<String,Object>> mblList = manifestMapper.selectConsigneeMatchingList(srchMap);
		
		
//		// 재사용할 데이터를 찾는다.
		if(mblList != null) {
			
			for(int i=0; i<mblList.size(); i++) {
				Map<String,Object> hblMap = new HashMap<String, Object>();
				hblMap = mblList.get(i);
				
				hblMap.put("srchStartDate", startDate);
				hblMap.put("srchEndDate", endDate);
		        String consigneeTel = Util.null2str(hblMap.get("CONSIGNEE_TEL"));
		        String consigneeName = Util.null2str(hblMap.get("CONSIGNEE_NAME"));
		        hblMap.put("CONSIGNEE_TEL", consigneeTel.length() > 8 ? consigneeTel.substring(consigneeTel.length()-8) : consigneeTel);
		        hblMap.put("CONSIGNEE_NAME", consigneeName.replaceAll("\\s+", ""));
		        
				Map<String,Object> matchMap = manifestMapper.selectConsigneeMatchingInfo(hblMap);
				if(matchMap != null) {
					matchMap.put("OD_JOB_NO", hblMap.get("OD_JOB_NO"));
					updateList.add(matchMap);
				}
			}
			
		}
		
		// 선택한 데이터 업데이트
		int cnt = 1;
		if(updateList != null && updateList.size() > 0) {
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
				
				for(int y=0; y<updateList.size(); y++) {
					
					Map<String,Object> updateMap = updateList.get(y);
					String lcNo = Util.null2str(updateMap.get("MRN"))+ Util.null2str(updateMap.get("MSN"))+Util.null2str(updateMap.get("HSN"));
					updateMap.put("LC_NO",lcNo);
					
					manifestBatchMapper.updateCustomsIdInfo(updateMap);
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
		
		return manifestOrderSaveResult;
	}
	
	/**
	 * 수입화물진행정보 수동 저장
	 * @param sendManifest
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult manualInsertCargoProgress(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		try {
			OrderSaveResult orderSaveResult = new OrderSaveResult();
			
			Map<String, Object> result = new HashMap<String, Object>();
			
			Map<String, Object> masterOrder = null;
			List<Map<String, Object>> houseOrderList = null;
			List<OrderPodUpdate> dataList = new ArrayList<OrderPodUpdate>();
			
			SqlSession sqlSession = null;
			TransactionStatus status = null;
			
			/** 하우스오더조회 */
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("orderDetYn", "Y");
			tmpMap.put("houseOdJobNoList", sendManifest.getHouseOdJobNoList());
			tmpMap.put("srchType", "insertCargoProgress");
			houseOrderList = manifestMapper.selectGwSendHouseOrderList(tmpMap);
			
			/** 화물진행정보 Status 체크 */
			for(Map<String, Object> houseOrder : houseOrderList){
				//UNIPASS에서 받은 정보 Parsing 및 데이터 세팅
				cargoProgressParser.getCargoProgressInfo(houseOrder, sendManifest.getEntBusinessCode());
			}
			
			/** 화물진행정보 수신 데이터 DB에 업데이트 */
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
				
				Map<String, Object> tempPodMap;
				Map<String, Object> podMap = new HashMap<String, Object>();
				podMap.put("entCode", sendManifest.getEntCode());
				podMap.put("entBusinessCode", "");
				podMap.put("gmtTime", "0");
				podMap.put("solution", "ECM");
				podMap.put("updateUserId", "DAEMON"); // TODO DAEMON? 사용자아이디?
				podMap.put("stockType", "POD");
				podMap.put("stockSign", "POD");
				podMap.put("stockSeqLength", 8);
				podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				podMap.put("stockRemark", "ECM POD JOB NO");
				podMap.put("podTableName", 	"TB_ECM_POD_03");
				int seq = 1;
				int cnt=0;
			
				for(Map<String, Object> houseOrder : houseOrderList) {
					if(houseOrder.get("orderPodUpdate") == null) {
						continue;
					}
					OrderPodUpdate data = (OrderPodUpdate)houseOrder.get("orderPodUpdate");
					
					/** 취하일 때는 목록심사완료로부터 수리일자를 업데이트하지 않음 (수입신고수리로부터만 업데이트) */
					boolean ecm240Flag = false; // 통관목록심사완료 유무
					boolean ecm280Flag = false; // 수입신고수리 유무
					for(Pod pod : data.getPodList()) {
						if("ECM240".equals(pod.getProgressStatus()))  ecm240Flag = true;
						if("ECM280".equals(pod.getProgressStatus()))  ecm280Flag = true;
					}
					
					String dropYn = Util.null2str(houseOrder.get("DROP_YN"));
					if("Y".equals(dropYn)) {
						if(ecm240Flag && !ecm280Flag) data.setClearanceDate(null);
						if(data.isEcm290Flag() && "".equals(Util.null2str(data.getCustomsHoldDateTime()))) data.setCarryOutDateTime(null);
					}
					manifestBatchMapper.batchUpdatePodStatus(data);
					
					/** 일반/배제/목록취하건이라면 수입신고수리 통관상태에 반영 */
					String cdType = Util.null2str(data.getCdType());
					if(ecm280Flag && (cdType.equals("N") || cdType.equals("B") || (cdType.equals("Y") && !dropYn.equals("")) )) {
						manifestBatchMapper.updateClearanceStatus(data);
					}
					data.setCdType(Util.null2str(houseOrder.get("CD_TYPE")));
					data.setDropYn(dropYn);
					data.setCustCodeLv1(Util.null2str(houseOrder.get("CUST_CODE_LV1")));
					dataList.add(data);
				}
				
				sqlSession.flushStatements();
				sqlSession.clearCache();
				
				cnt=0;
				for(OrderPodUpdate data : dataList) {
					podMap.put("odJobNo", data.getSrchOdJobNo());
					podMap.put("blNo", data.getHblNo());
					for(Pod pod : data.getPodList()) {
						cnt++;
						podMap.put("podStatus", pod.getProgressStatus());
						manifestBatchMapper.batchDeletePod(podMap);
//						if (cnt % batchInsertSize_ == 0){
//							sqlSession.flushStatements();
//							sqlSession.clearCache();
//						}
					}
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				
				cnt=0;
				for(OrderPodUpdate data : dataList) {
					for(Pod pod : data.getPodList()) {
						// 목록심사완료, 반출, 반출취소
						if(("ECM240".equals(pod.getProgressStatus()) || "ECM290".equals(pod.getProgressStatus()) || "ECM300".equals(pod.getProgressStatus()))) {
							// 취하건이면
							if("Y".equals(data.getDropYn())) {
								switch(pod.getProgressStatus()) {
								case "ECM240" : // 목록심사완료 INSERT X
									pod.setSaveYn("N"); 
									break;
								case "ECM290" :	// 목록보류 이후 반출만 INSERT
									if(data.isEcm290Flag() && "".equals(Util.null2str(data.getCustomsHoldDateTime()))) {
										pod.setSaveYn("N");
									}
									break;
								case "ECM300" : // 반출취소 INSERT X
									pod.setSaveYn("N");
									break;
								}
							}
						}
					}
				}
				for(OrderPodUpdate data : dataList) {
					podMap.put("odJobNo", data.getSrchOdJobNo());
					podMap.put("blNo", data.getHblNo());
					for(Pod pod : data.getPodList()) {
						if(!"N".equals(pod.getSaveYn())) {
							cnt++;
							podMap.put("podStatus", pod.getProgressStatus());
							podMap.put("podDateTime", pod.getProcessDateTime());
							podMap.put("podJobNo", DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
							manifestBatchMapper.batchInsertPod(podMap);
//							if (cnt % batchInsertSize_ == 0){
//								sqlSession.flushStatements();
//								sqlSession.clearCache();
//							}
						}
					}
				}
				
				//쿠팡트래킹POD Insert
				if("HUBNK".equals(sendManifest.getEntCode())) {
					cnt=0;
					for(OrderPodUpdate data : dataList) {
						if("COUPANG".contains(data.getCustCodeLv1())) {
							String cdType = data.getCdType();
							podMap.put("odJobNo", data.getSrchOdJobNo());
							podMap.put("blNo", data.getHblNo());
							for(Pod pod : data.getPodList()) {
								boolean traking210Flag = false;
								boolean traking240Flag = false;
								boolean traking250Flag = false;
								boolean traking280Flag = false;
								String podStatus = pod.getProgressStatus();
								//통관구분이 일반/배제/취하 일때
								if("N".equals(cdType) || "B".equals(cdType) || !"".equals(data.getDropYn())) {
									if("ECM250".equals(podStatus)){ //통관시작(수입신고)
										traking250Flag = true;
									}else if("ECM280".equals(podStatus)) { //통관완료(수입신고수리)
										traking280Flag = true;
									}
								}
								//통관구분이 목록일때
								if("Y".equals(cdType)) {
									if("ECM210".equals(podStatus)){ //통관시작(반입)
										traking210Flag = true;
									}else if("ECM240".equals(podStatus)) { //통관완료(목록심사완료)
										traking240Flag = true;
									}
								}
								
								if(traking250Flag || traking280Flag || traking240Flag || traking210Flag) {
									cnt++;
									podMap.put("podStatus", pod.getProgressStatus());
									podMap.put("podDateTime", pod.getProcessDateTime());
									podMap.put("trackingJobNo", DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
									manifestBatchMapper.batchInsertCoupangTrackingPod(podMap);
								}
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
			
			orderSaveResult.setResultData(result);
			return orderSaveResult;
			
		}catch(Exception e) {
			throw e;
		}finally {
			
		}
	}
	
	/**
	 * ECM 적하목록 수입 특송생성데이터 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestInList selectInsertEms(SearchManifestInList search) throws Exception {
		ManifestInList manifestInList = new ManifestInList();
		List<ManifestInMaster> list  = manifestMapper.selectInsertEms(search);
		manifestInList.setManifestInMasterList(list);
		return manifestInList;
	}
	
	/**
	 * UPS 통관목록 출력물 전송 대상 건 조회
	 * @param sendManifest
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public ManifestOrderSaveResult selectExpEmailList(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
		// UPS기초관리 > 수출이메일업체에서 전송 정보 조회
		List<Map<String, Object>> dataList = manifestMapper.selectExpEmailList(sendManifest);
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("dataList", dataList);
		manifestOrderSaveResult.setResultData(dataMap);
		return manifestOrderSaveResult;
	}

	/**
	 * UPS 통관목록 현황 집계
	 * @param search
	 * @return
	 */
	public ManifestAggregateList selectManifestAggregateList(SearchManifestInList search) throws Exception {
		ManifestAggregateList aggregateList = new ManifestAggregateList();
		List<ManifestAggregate> list = manifestMapper.selectManifestAggregateList(search);
		aggregateList.setManifestAggregateList(list);
		return aggregateList;
	}

    /**
     * 허브넷 반입예정 저장(항공)
     * @param sendManifest
     * @return
     */
    public ManifestOrderSaveResult insertHubnInPlanAir(SendManifest sendManifest) throws Exception {

        //허브넷 반입 예정 과정 정리

        //HSN 중복 체크
        //HSN 없는지 체크
        //MRN MSN 없는지 체크

        //CWHP의 XRAY_INFO 테이블 SELECT
        //반입진행중인 데이터인지 확인

        // XRAY_INFO 테이블에서 데이터가 조회됐다면
        // CWHP의 INP 테이블 SELECT
        // INP 테이블에 조회된 결과가 있으면 LOG 기록

        // CWHP의 EXPECT 업데이트
        // 업데이트 건이 없으면 => INSERT

        // CWHP의 XRAY_INFO 업데이트
        // 업데이트 건이 없으면 => INSERT

        // CWHP XRAY_INFO_BOX 삭제
        // CWHP XRAY_INFO_BOX INSERT

        // CWHP XRAY_INFO_SUB 업데이트
        // 업데이트 건이 없으면 => INSERT

        //반입 예정 정보 이력 업데이트 G1

        ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
        //대상건 조회
        List<Map<String, Object>> list = manifestMapper.selectHubnInPlanInsertTarget(sendManifest);
        int size = 0;

        //HSN 중복 체크
        //HSN 없는지 체크
        //MRN MSN 없는지 체크
        if (!list.isEmpty()) {
            size = list.size();

            Map<String, Object> resultMap = new HashMap<String, Object>();
            String iertHblNo = "";
            for (Map<String, Object> validMap : list) {
                if ("".equals(Util.null2str(validMap.get("MRN"))) || "".equals(Util.null2str(validMap.get("HSN"))) || "".equals(Util.null2str(validMap.get("MSN")))) {
                    iertHblNo += validMap.get("HBL_NO") + "</br>";
                }
            }
            if (!iertHblNo.isEmpty()) {
                resultMap.put("returnMessage", iertHblNo);
                manifestOrderSaveResult.setResultCode("mrnIsNull");
                manifestOrderSaveResult.setResultData(resultMap);
                return manifestOrderSaveResult;
            }

            int batchSize = 100;
            for (int i = 0; i < list.size(); i += batchSize) {

                int end = Math.min(i + batchSize, list.size());

                List<Map<String, Object>> subList = list.subList(i, end);
                Map<String, Object> paramMap = subList.get(i);
                paramMap.put("subList", subList);

                //CWHP의 XRAY_INFO 테이블 SELECT
                //반입진행중인 데이터인지 확인
                iertHblNo = "";
                List<Map<String, Object>> cwhpXrayList = cwhpManifestMapper.selectCwhpXrayInfoCheck(paramMap);
                if (!cwhpXrayList.isEmpty()) {

                    //XRAY_INFO 값에 존재하면 반입진행중인건이 존재하므로 return
                    for (Map<String, Object> validMap : cwhpXrayList) {
                            iertHblNo += validMap.get("MRN").toString() + validMap.get("MSN").toString() + validMap.get("HSN").toString() + "</br>";
                    }

                    if (!iertHblNo.isEmpty()) {
                        resultMap.put("returnMessage", iertHblNo);
                        manifestOrderSaveResult.setResultCode("xrayInfoError");
                        manifestOrderSaveResult.setResultData(resultMap);
                        return manifestOrderSaveResult;
                    }
                }else{

                    // CWHP의 INP 테이블 SELECT
                    // INP 테이블에 조회된 결과가 있으면 LOG 기록
                    iertHblNo = "";
                    List<Map<String, Object>> cwhpInpList = cwhpManifestMapper.selectCwhpInpCheck(paramMap);

                    // 로그저장
                    if(!cwhpInpList.isEmpty()){

//                        /** 처리 로그 기록 */
//                        Map<String, Object> tmpMap = new HashMap<String, Object>();
//                        String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
//                        String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//
//                        tmpMap.clear();
//                        tmpMap.put("ent_code", paramMap.get("entCode"));
//                        tmpMap.put("customer_code", "");
//                        tmpMap.put("solution", "ECM");
//                        tmpMap.put("bound", paramMap.get("BOUND"));
//                        tmpMap.put("bound_type", "IMP");
//                        tmpMap.put("hbl_no", "");
//                        tmpMap.put("if_service_code", "");
//                        tmpMap.put("if_doc_code", "BMS_IN_PLAN");
//                        tmpMap.put("if_doc_name", "반입예정정보");
//                        tmpMap.put("if_doc_reference_no", paramMap.get("MBL_NO"));
//                        tmpMap.put("if_vender", paramMap.get("ediVender"));
//                        tmpMap.put("if_send_recv_type", "S");
//                        tmpMap.put("if_send_recv_method", "");
//                        tmpMap.put("if_file_path", "");
//                        tmpMap.put("if_file_name", "");
//                        tmpMap.put("if_status", "SEND");
//                        tmpMap.put("if_status_info", "");
//                        tmpMap.put("if_sender_id", "");
//                        tmpMap.put("if_receiver_id", "");
//                        tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
//                        tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
//                        tmpMap.put("if_remark", "");
//                        tmpMap.put("register_user_id", "");
//                        tmpMap.put("register_user_name", "");
//                        tmpMap.put("register_date_time", System.currentTimeMillis());
//                        tmpMap.put("es_job_no", logId);
//
//                        IndexApi indexApi = new IndexApi(client);
//                        if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//
//                        DocumentApi documentApi = new DocumentApi(client);
//                        documentApi.createDocument(index, logId, tmpMap);

                        //XRAY_INFO 값에 존재하면 반입진행중인건이 존재하므로 return
                        for (Map<String, Object> validMap : cwhpInpList) {
                            iertHblNo += validMap.get("BL") + "</br>";
                        }

                        if (!iertHblNo.isEmpty()) {
                            resultMap.put("returnMessage", iertHblNo);
                            manifestOrderSaveResult.setResultCode("inpError");
                            manifestOrderSaveResult.setResultData(resultMap);
                            return manifestOrderSaveResult;
                        }
                    }

                }

            }
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> paramMap = list.get(i);

                // CWHP의 EXPECT 업데이트
                // 업데이트 건이 없으면 => INSERT
                paramMap.put("UNIT_PIECES","CT");
                paramMap.put("CON","");
                paramMap.put("CON_CNT",0);
                paramMap.put("srchBound", "AIR");
                cwhpManifestMapper.cwhpUpdateExpect(paramMap);

                // CWHP의 XRAY_INFO 업데이트
                //업데이트 건이 없으면 => INSERT
                cwhpManifestMapper.cwhpUpdateXrayInfo(paramMap);

                // CWHP XRAY_INFO_BOX 삭제
                // CWHP XRAY_INFO_BOX INSERT
                // 하우스의 pieces 의 갯수만큼 반복
                cwhpManifestMapper.cwhpDeleteXrayInfoBox(paramMap);
                int pieces = Integer.parseInt(paramMap.get("PIECES").toString());
                for (int y = 0; y < pieces; y++) {
                    if(pieces == 1){
                        paramMap.put("BOX_NO",paramMap.get("HBL_NO"));
                    }else{
                        paramMap.put("BOX_NO",paramMap.get("HBL_NO")+"00");
                    }
                    cwhpManifestMapper.cwhpInsertXrayInfoBox(paramMap);
                }

                // CWHP XRAY_INFO_SUB 업데이트
                // 업데이트 건이 없으면 => INSERT
                cwhpManifestMapper.cwhpUpdateXrayInfoSub(paramMap);

                //반입 예정 정보 이력 업데이트 G1
                manifestMapper.updateHubnBmsInInfo(paramMap);
            }
        }

        manifestOrderSaveResult.setResultCode(Util.null2str(size));
        return manifestOrderSaveResult;
    }

    /**
     * 허브넷 반입예정 저장(해상)
     * @param sendManifest
     * @return
     */
    public ManifestOrderSaveResult insertHubnInPlanSea(SendManifest sendManifest) throws Exception {

        //허브넷 반입 예정 과정 정리

        //HSN 중복 체크
        //HSN 없는지 체크
        //MRN MSN 없는지 체크

        //CWHP의 XRAY_INFO 테이블 SELECT
        //반입진행중인 데이터인지 확인

        // XRAY_INFO 테이블에서 데이터가 조회됐다면
        // CWHP의 INP 테이블 SELECT
        // INP 테이블에 조회된 결과가 있으면 LOG 기록

        // CWHP의 XRAY_INFO 업데이트
        // 업데이트 건이 없으면 => INSERT

        // CWHP 의 EXPECTContainer 삭제
        // CWHP EXPECTContainer 테이블 INSERT

        //반입 예정 정보 이력 업데이트 G1

        ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
        //대상건 조회
        List<Map<String, Object>> list = manifestMapper.selectHubnInPlanInsertTarget(sendManifest);
        int size = 0;

        //HSN 중복 체크
        //HSN 없는지 체크
        //MRN MSN 없는지 체크
        if (!list.isEmpty()) {
            size = list.size();

            Map<String, Object> resultMap = new HashMap<String, Object>();
            String iertHblNo = "";
            for (Map<String, Object> validMap : list) {
                if ("".equals(Util.null2str(validMap.get("MRN"))) || "".equals(Util.null2str(validMap.get("HSN"))) || "".equals(Util.null2str(validMap.get("MSN")))) {
                    iertHblNo += validMap.get("HBL_NO") + "</br>";
                }
            }
            if (!iertHblNo.isEmpty()) {
                resultMap.put("returnMessage", iertHblNo);
                manifestOrderSaveResult.setResultCode("mrnIsNull");
                manifestOrderSaveResult.setResultData(resultMap);
                return manifestOrderSaveResult;
            }

            int batchSize = 100;
            for (int i = 0; i < list.size(); i += batchSize) {

                int end = Math.min(i + batchSize, list.size());

                List<Map<String, Object>> subList = list.subList(i, end);
                Map<String, Object> paramMap = subList.get(i);
                paramMap.put("subList", subList);

                //CWHP의 XRAY_INFO 테이블 SELECT
                //반입진행중인 데이터인지 확인
                iertHblNo = "";
                List<Map<String, Object>> cwhpXrayList = cwhpManifestMapper.selectCwhpXrayInfoCheck(paramMap);
                if (!cwhpXrayList.isEmpty()) {

                    //XRAY_INFO 값에 존재하면 반입진행중인건이 존재하므로 return
                    for (Map<String, Object> validMap : cwhpXrayList) {
                        iertHblNo += validMap.get("MRN").toString() + validMap.get("MSN").toString() + validMap.get("HSN").toString() + "</br>";
                    }

                    if (!iertHblNo.isEmpty()) {
                        resultMap.put("returnMessage", iertHblNo);
                        manifestOrderSaveResult.setResultCode("xrayInfoError");
                        manifestOrderSaveResult.setResultData(resultMap);
                        return manifestOrderSaveResult;
                    }
                }else{

                    // CWHP의 INP 테이블 SELECT
                    // INP 테이블에 조회된 결과가 있으면 LOG 기록
                    iertHblNo = "";
                    List<Map<String, Object>> cwhpInpList = cwhpManifestMapper.selectCwhpInpCheck(paramMap);

                    // 로그저장
                    if(!cwhpInpList.isEmpty()){

//                        /** 처리 로그 기록 */
//                        Map<String, Object> tmpMap = new HashMap<String, Object>();
//                        String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
//                        String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//
//                        tmpMap.clear();
//                        tmpMap.put("ent_code", paramMap.get("entCode"));
//                        tmpMap.put("customer_code", "");
//                        tmpMap.put("solution", "ECM");
//                        tmpMap.put("bound", paramMap.get("BOUND"));
//                        tmpMap.put("bound_type", "IMP");
//                        tmpMap.put("hbl_no", "");
//                        tmpMap.put("if_service_code", "");
//                        tmpMap.put("if_doc_code", "BMS_IN_PLAN");
//                        tmpMap.put("if_doc_name", "반입예정정보");
//                        tmpMap.put("if_doc_reference_no", paramMap.get("MBL_NO"));
//                        tmpMap.put("if_vender", paramMap.get("ediVender"));
//                        tmpMap.put("if_send_recv_type", "S");
//                        tmpMap.put("if_send_recv_method", "");
//                        tmpMap.put("if_file_path", "");
//                        tmpMap.put("if_file_name", "");
//                        tmpMap.put("if_status", "SEND");
//                        tmpMap.put("if_status_info", "");
//                        tmpMap.put("if_sender_id", "");
//                        tmpMap.put("if_receiver_id", "");
//                        tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
//                        tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
//                        tmpMap.put("if_remark", "");
//                        tmpMap.put("register_user_id", "");
//                        tmpMap.put("register_user_name", "");
//                        tmpMap.put("register_date_time", System.currentTimeMillis());
//                        tmpMap.put("es_job_no", logId);
//
//                        IndexApi indexApi = new IndexApi(client);
//                        if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//
//                        DocumentApi documentApi = new DocumentApi(client);
//                        documentApi.createDocument(index, logId, tmpMap);

                        //XRAY_INFO 값에 존재하면 반입진행중인건이 존재하므로 return
                        for (Map<String, Object> validMap : cwhpInpList) {
                            iertHblNo += validMap.get("BL") + "</br>";
                        }

                        if (!iertHblNo.isEmpty()) {
                            resultMap.put("returnMessage", iertHblNo);
                            manifestOrderSaveResult.setResultCode("inpError");
                            manifestOrderSaveResult.setResultData(resultMap);
                            return manifestOrderSaveResult;
                        }
                    }

                }
            }
            
            //반입예정정보 이관
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> paramMap = list.get(i);

                // 오더 해상 컨테이너 리스트 조회
                // 컨테이너 값이 필요해서 먼저 SELECT 함.
                List<Map<String,Object>> containerMapList = manifestMapper.selectOrderContainerList(paramMap);

                // CWHP의 EXPECT 업데이트
                // 업데이트 건이 없으면 => INSERT
                paramMap.put("srchBound", "SEA");
                paramMap.put("UNIT_PIECES","GT");
                paramMap.put("CON", containerMapList.get(0).get("CNTR_NO"));
                paramMap.put("CON_CNT",containerMapList.size());
                paramMap.put("DOC_BONDED","");
                double pid = Util.null2dbl(paramMap.get("DIM_WIDTH")) * Util.null2dbl(paramMap.get("DIM_LENGTH")) * Util.null2dbl(paramMap.get("DIM_HEIGHT"));
                paramMap.put("PID",pid);
                cwhpManifestMapper.cwhpUpdateExpect(paramMap);

                // CWHP의 XRAY_INFO 업데이트
                //업데이트 건이 없으면 => INSERT
                cwhpManifestMapper.cwhpUpdateXrayInfo(paramMap);

                for(Map<String,Object> containerMap : containerMapList){
                    containerMap.put("MRN", paramMap.get("MRN"));
                    containerMap.put("MSN", paramMap.get("MSN"));
                    containerMap.put("HSN", paramMap.get("HSN"));

                    // 반입예정 컨테이너 EXPECTContainer Delete
                    cwhpManifestMapper.cwhpDeleteContainer(containerMap);
                    // EXPECTContainer INSERT
                    cwhpManifestMapper.cwhpInsertContainer(containerMap);
                }

                //반입 예정 정보 이력 업데이트 G1
                manifestMapper.updateHubnBmsInInfo(paramMap);
            }
        }

        manifestOrderSaveResult.setResultCode(Util.null2str(size));
        return manifestOrderSaveResult;
    }

    /**
     * 허브넷 반입예정 저장(항공)
     *
     * @param sendManifest
     * @return
     */
    public ManifestOrderSaveResult insertHubnInPlanAir(HttpServletRequest req, SendManifest sendManifest) throws Exception {

        //허브넷 반입 예정 과정 정리

        //HSN 중복 체크
        //HSN 없는지 체크
        //MRN MSN 없는지 체크

        //CWHP의 XRAY_INFO 테이블 SELECT
        //반입진행중인 데이터인지 확인

        // XRAY_INFO 테이블에서 데이터가 조회됐다면
        // CWHP의 INP 테이블 SELECT
        // INP 테이블에 조회된 결과가 있으면 LOG 기록

        // CWHP의 EXPECT 업데이트
        // 업데이트 건이 없으면 => INSERT

        // CWHP의 XRAY_INFO 업데이트
        // 업데이트 건이 없으면 => INSERT

        // CWHP XRAY_INFO_BOX 삭제
        // CWHP XRAY_INFO_BOX INSERT

        // CWHP XRAY_INFO_SUB 업데이트
        // 업데이트 건이 없으면 => INSERT

        //반입 예정 정보 이력 업데이트 G1

        ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
        List<Map<String, Object>> list = sendManifest.getBmsInPlanList();
        int size = 0;

        if (!list.isEmpty()) {
            size = list.size();

            Map<String, Object> resultMap = new HashMap<String, Object>();
            String iertHblNo = "";
            SqlSession sqlSession = null;

            int batchSize = 100;
            for (int i = 0; i < list.size(); i += batchSize) {

                int end = Math.min(i + batchSize, list.size());

                List<Map<String, Object>> subList = list.subList(i, end);
                Map<String, Object> paramMap = subList.get(0);
                paramMap.put("subList", subList);

                //CWHP의 XRAY_INFO 테이블 SELECT
                //반입진행중인 데이터인지 확인
                iertHblNo = "";
                List<Map<String, Object>> cwhpXrayList = cwhpManifestMapper.selectCwhpXrayInfoCheck(paramMap);
                if (!cwhpXrayList.isEmpty()) {

                    //XRAY_INFO 값에 존재하면 반입진행중인건이 존재하므로 return
                    for (Map<String, Object> validMap : cwhpXrayList) {
                        iertHblNo += validMap.get("MRN").toString() + validMap.get("MSN").toString() + validMap.get("HSN").toString() + "</br>";
                    }

                    if (!iertHblNo.isEmpty()) {
                        resultMap.put("returnMessage", iertHblNo);
                        manifestOrderSaveResult.setResultCode("xrayInfoError");
                        manifestOrderSaveResult.setResultData(resultMap);
                        return manifestOrderSaveResult;
                    }
                } else {

                    // CWHP의 INP 테이블 SELECT
                    // INP 테이블에 조회된 결과가 있으면 LOG 기록
                    iertHblNo = "";
                    List<Map<String, Object>> cwhpInpList = cwhpManifestMapper.selectCwhpInpCheck(paramMap);

                    // 로그저장
                    if (!cwhpInpList.isEmpty()) {

//                        /** 처리 로그 기록 */
//                        Map<String, Object> tmpMap = new HashMap<String, Object>();
//                        String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
//                        String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//
//                        tmpMap.clear();
//                        tmpMap.put("ent_code", paramMap.get("entCode"));
//                        tmpMap.put("customer_code", "");
//                        tmpMap.put("solution", "ECM");
//                        tmpMap.put("bound", paramMap.get("BOUND"));
//                        tmpMap.put("bound_type", "IMP");
//                        tmpMap.put("hbl_no", "");
//                        tmpMap.put("if_service_code", "");
//                        tmpMap.put("if_doc_code", "BMS_IN_PLAN");
//                        tmpMap.put("if_doc_name", "반입예정정보");
//                        tmpMap.put("if_doc_reference_no", paramMap.get("MBL_NO"));
//                        tmpMap.put("if_vender", paramMap.get("ediVender"));
//                        tmpMap.put("if_send_recv_type", "S");
//                        tmpMap.put("if_send_recv_method", "");
//                        tmpMap.put("if_file_path", "");
//                        tmpMap.put("if_file_name", "");
//                        tmpMap.put("if_status", "SEND");
//                        tmpMap.put("if_status_info", "");
//                        tmpMap.put("if_sender_id", "");
//                        tmpMap.put("if_receiver_id", "");
//                        tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
//                        tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
//                        tmpMap.put("if_remark", "");
//                        tmpMap.put("register_user_id", "");
//                        tmpMap.put("register_user_name", "");
//                        tmpMap.put("register_date_time", System.currentTimeMillis());
//                        tmpMap.put("es_job_no", logId);
//
//                        IndexApi indexApi = new IndexApi(client);
//                        if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//
//                        DocumentApi documentApi = new DocumentApi(client);
//                        documentApi.createDocument(index, logId, tmpMap);

                        //XRAY_INFO 값에 존재하면 반입진행중인건이 존재하므로 return
                        for (Map<String, Object> validMap : cwhpInpList) {
                            iertHblNo += validMap.get("BL") + "</br>";
                        }

                        if (!iertHblNo.isEmpty()) {
                            resultMap.put("returnMessage", iertHblNo);
                            manifestOrderSaveResult.setResultCode("inpError");
                            manifestOrderSaveResult.setResultData(resultMap);
                            return manifestOrderSaveResult;
                        }
                    }

                }
            }

            try {
                sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
                CwhpManifestMapper cwhpManifestBatchMapper = sqlSession.getMapper(CwhpManifestMapper.class);
                int cnt = 0;
                for (int i = 0; i < list.size(); i++) {
                    cnt++;
                    Map<String, Object> paramMap = list.get(i);

                    // CWHP의 EXPECT 업데이트
                    // 업데이트 건이 없으면 => INSERT
                    paramMap.put("UNIT_PIECES","CT");
                    paramMap.put("CON","");
                    paramMap.put("CON_CNT",0);
                    paramMap.put("srchBound", "AIR");
                    cwhpManifestBatchMapper.cwhpUpdateExpect(paramMap);

                    // CWHP의 XRAY_INFO 업데이트
                    //업데이트 건이 없으면 => INSERT
                    cwhpManifestBatchMapper.cwhpUpdateXrayInfo(paramMap);

                    // CWHP XRAY_INFO_BOX 삭제
                    // CWHP XRAY_INFO_BOX INSERT
                    // 하우스의 pieces 의 갯수만큼 반복
                    cwhpManifestBatchMapper.cwhpDeleteXrayInfoBox(paramMap);
                    int pieces = Integer.parseInt(paramMap.get("PIECES").toString());
                    int bBoxCount = 0;
                    for (int y = 0; y < pieces; y++) {
                        bBoxCount++;
                        if(pieces == 1){
                            paramMap.put("BOX_NO",paramMap.get("HBL_NO"));
                        }else{
                            paramMap.put("BOX_NO",paramMap.get("HBL_NO")+String.format("%02d", bBoxCount));
                        }
                        cwhpManifestBatchMapper.cwhpInsertXrayInfoBox(paramMap);
                    }

                    // CWHP XRAY_INFO_SUB 업데이트
                    // 업데이트 건이 없으면 => INSERT
                    cwhpManifestBatchMapper.cwhpUpdateXrayInfoSub(paramMap);

                    if (cnt % 1000 == 0 || cnt == size) {
                        sqlSession.flushStatements();
                        sqlSession.clearCache();
                    }
                }
                sqlSession.commit();
            }catch (Exception e){
                sqlSession.rollback();
                throw e;
            }finally {
                sqlSession.close();
            }
        }
        return manifestOrderSaveResult;
    }

    /**
     * 허브넷 반입예정 저장(해상)
     *
     * @param sendManifest
     * @return
     */
    public ManifestOrderSaveResult insertHubnInPlanSea(HttpServletRequest req, SendManifest sendManifest) throws Exception {

        //허브넷 반입 예정 과정 정리

        //HSN 중복 체크
        //HSN 없는지 체크
        //MRN MSN 없는지 체크

        //CWHP의 XRAY_INFO 테이블 SELECT
        //반입진행중인 데이터인지 확인

        // XRAY_INFO 테이블에서 데이터가 조회됐다면
        // CWHP의 INP 테이블 SELECT
        // INP 테이블에 조회된 결과가 있으면 LOG 기록

        // CWHP의 XRAY_INFO 업데이트
        // 업데이트 건이 없으면 => INSERT

        // CWHP 의 EXPECTContainer 삭제
        // CWHP EXPECTContainer 테이블 INSERT

        //반입 예정 정보 이력 업데이트 G1

        ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
        //대상건 조회
        List<Map<String, Object>> list = sendManifest.getBmsInPlanList();
        int size = 0;

        if (!list.isEmpty()) {
            size = list.size();

            Map<String, Object> resultMap = new HashMap<String, Object>();
            String iertHblNo = "";
            SqlSession sqlSession = null;

            int batchSize = 100;
            for (int i = 0; i < list.size(); i += batchSize) {

                int end = Math.min(i + batchSize, list.size());

                List<Map<String, Object>> subList = list.subList(i, end);
                Map<String, Object> paramMap = subList.get(0);
                paramMap.put("subList", subList);

                //CWHP의 XRAY_INFO 테이블 SELECT
                //반입진행중인 데이터인지 확인
                iertHblNo = "";
                List<Map<String, Object>> cwhpXrayList = cwhpManifestMapper.selectCwhpXrayInfoCheck(paramMap);
                if (!cwhpXrayList.isEmpty()) {

                    //XRAY_INFO 값에 존재하면 반입진행중인건이 존재하므로 return
                    for (Map<String, Object> validMap : cwhpXrayList) {
                        iertHblNo += validMap.get("MRN").toString() + validMap.get("MSN").toString() + validMap.get("HSN").toString() + "</br>";
                    }

                    if (!iertHblNo.isEmpty()) {
                        resultMap.put("returnMessage", iertHblNo);
                        manifestOrderSaveResult.setResultCode("xrayInfoError");
                        manifestOrderSaveResult.setResultData(resultMap);
                        return manifestOrderSaveResult;
                    }
                } else {

                    // CWHP의 INP 테이블 SELECT
                    // INP 테이블에 조회된 결과가 있으면 LOG 기록
                    iertHblNo = "";
                    List<Map<String, Object>> cwhpInpList = cwhpManifestMapper.selectCwhpInpCheck(paramMap);

                    // 로그저장
                    if (!cwhpInpList.isEmpty()) {

//                        /** 처리 로그 기록 */
//                        Map<String, Object> tmpMap = new HashMap<String, Object>();
//                        String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
//                        String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//
//                        tmpMap.clear();
//                        tmpMap.put("ent_code", paramMap.get("entCode"));
//                        tmpMap.put("customer_code", "");
//                        tmpMap.put("solution", "ECM");
//                        tmpMap.put("bound", paramMap.get("BOUND"));
//                        tmpMap.put("bound_type", "IMP");
//                        tmpMap.put("hbl_no", "");
//                        tmpMap.put("if_service_code", "");
//                        tmpMap.put("if_doc_code", "BMS_IN_PLAN");
//                        tmpMap.put("if_doc_name", "반입예정정보");
//                        tmpMap.put("if_doc_reference_no", paramMap.get("MBL_NO"));
//                        tmpMap.put("if_vender", paramMap.get("ediVender"));
//                        tmpMap.put("if_send_recv_type", "S");
//                        tmpMap.put("if_send_recv_method", "");
//                        tmpMap.put("if_file_path", "");
//                        tmpMap.put("if_file_name", "");
//                        tmpMap.put("if_status", "SEND");
//                        tmpMap.put("if_status_info", "");
//                        tmpMap.put("if_sender_id", "");
//                        tmpMap.put("if_receiver_id", "");
//                        tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
//                        tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
//                        tmpMap.put("if_remark", "");
//                        tmpMap.put("register_user_id", "");
//                        tmpMap.put("register_user_name", "");
//                        tmpMap.put("register_date_time", System.currentTimeMillis());
//                        tmpMap.put("es_job_no", logId);
//
//                        IndexApi indexApi = new IndexApi(client);
//                        if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//
//                        DocumentApi documentApi = new DocumentApi(client);
//                        documentApi.createDocument(index, logId, tmpMap);

                        //XRAY_INFO 값에 존재하면 반입진행중인건이 존재하므로 return
                        for (Map<String, Object> validMap : cwhpInpList) {
                            iertHblNo += validMap.get("BL") + "</br>";
                        }

                        if (!iertHblNo.isEmpty()) {
                            resultMap.put("returnMessage", iertHblNo);
                            manifestOrderSaveResult.setResultCode("inpError");
                            manifestOrderSaveResult.setResultData(resultMap);
                            return manifestOrderSaveResult;
                        }
                    }

                }
            }

            try{
                sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
                CwhpManifestMapper cwhpManifestBatchMapper = sqlSession.getMapper(CwhpManifestMapper.class);
                int cnt = 0;

                //반입예정정보 이관
                for (int i = 0; i < list.size(); i++) {
                    cnt++;
                    Map<String, Object> paramMap = list.get(i);

                    // 오더 해상 컨테이너 리스트 조회
                    List<Map<String, Object>> containerMapList = (List<Map<String, Object>>) paramMap.get("containerMapList");

                    // CWHP의 EXPECT 업데이트
                    // 업데이트 건이 없으면 => INSERT
                    paramMap.put("srchBound", "SEA");
                    paramMap.put("UNIT_PIECES", "GT");
                    paramMap.put("CON", containerMapList.get(0).get("CNTR_NO"));
                    paramMap.put("CON_CNT", containerMapList.size());
                    paramMap.put("DOC_BONDED", "");
                    double pid = Util.null2dbl(paramMap.get("DIM_WIDTH")) * Util.null2dbl(paramMap.get("DIM_LENGTH")) * Util.null2dbl(paramMap.get("DIM_HEIGHT"));
                    paramMap.put("PID", pid);
                    cwhpManifestBatchMapper.cwhpUpdateExpect(paramMap);

                    // CWHP의 XRAY_INFO 업데이트
                    //업데이트 건이 없으면 => INSERT
                    cwhpManifestBatchMapper.cwhpUpdateXrayInfo(paramMap);

                    // 반입예정 컨테이너 EXPECTContainer
                    for (Map<String, Object> containerMap : containerMapList) {
                        containerMap.put("MRN", paramMap.get("MRN"));
                        containerMap.put("MSN", paramMap.get("MSN"));
                        containerMap.put("HSN", paramMap.get("HSN"));

                        // 반입예정 컨테이너 EXPECTContainer Delete
                        cwhpManifestBatchMapper.cwhpDeleteContainer(containerMap);
                        // EXPECTContainer INSERT
                        cwhpManifestBatchMapper.cwhpInsertContainer(containerMap);
                    }

                    if (cnt % 1000 == 0 || cnt == size) {
                        sqlSession.flushStatements();
                        sqlSession.clearCache();
                    }
                }
                sqlSession.commit();
            }catch (Exception e){
                sqlSession.rollback();
                throw e;
            }finally {
                sqlSession.close();
            }
        }
        return manifestOrderSaveResult;
    }

    /**
     * 허브넷 반입 예정 항공 검증
     * @param sendManifest
     * @return
     */
    public ManifestOrderSaveResult verifyHubnInPlanAir(SendManifest sendManifest) {
        ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
        //대상건 조회
        List<Map<String, Object>> list = manifestMapper.selectHubnInPlanInsertTarget(sendManifest);
        int size = 0;

        //HSN 중복 체크
        //HSN 없는지 체크
        //MRN MSN 없는지 체크
        if (!list.isEmpty()) {
            size = list.size();

            Map<String, Object> resultMap = new HashMap<String, Object>();
            String iertHblNo = "";
            for (Map<String, Object> validMap : list) {
                if ("".equals(Util.null2str(validMap.get("MRN"))) || "".equals(Util.null2str(validMap.get("HSN"))) || "".equals(Util.null2str(validMap.get("MSN")))) {
                    iertHblNo += validMap.get("HBL_NO") + "</br>";
                }

                // CD_TYPE 통관타입 값 변환
                String cdType = Util.null2str(validMap.get("CD_TYPE"));
                if("Y".equals(cdType)) {
                	validMap.put("CD_TYPE", "1");
                }else if("N".equals(cdType)) {
                	validMap.put("CD_TYPE", "0");
                }else if("Z".equals(cdType)) {
                	validMap.put("CD_TYPE", "4");
                }else if("B".equals(cdType)) {
                	validMap.put("CD_TYPE", "5");
                }
                
                // SE_INSPECTION_CODE 값에는 01,02,03 형식이라 0을 없애고 1글자만 보내야함
                String inspectionCode = Util.null2str(validMap.get("SE_INSPECTION_CODE"));
                if(inspectionCode.length() > 1) {
                	inspectionCode = inspectionCode.substring(1);
                	validMap.put("SE_INSPECTION_CODE",inspectionCode);
                }else if(inspectionCode.length() == 1){
                	if("C".equals(inspectionCode)) {
                		validMap.put("SE_INSPECTION_CODE","0");
                	}
                }
                
                //HOLD List 조회
                List<Map<String,Object>> holdList = manifestMapper.selectHubnOdHoldList(validMap);
                if(holdList != null && holdList.size() > 0){
                    for(Map<String,Object> holdMap : holdList){
                        // 01 : 송장교체
                        // 02 : 퀵요청
                        // 03: 출고보류
                        // 04 : VIP화물
                        // 05 : 세관집중검사
                    	// 07 : 도착지변경
                        if("01".equals(Util.null2str(holdMap.get("HOLD_CODE")))){
                            if("Y".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_1","2");
                            }else if("N".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_1","1");
                            }
                        }else if("02".equals(Util.null2str(holdMap.get("HOLD_CODE")))){
                            if("Y".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_2","2");
                            }else if("N".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_2","1");
                            }
                        }else if("03".equals(Util.null2str(holdMap.get("HOLD_CODE")))){
                            if("Y".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_3","2");
                            }else if("N".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_3","1");
                            }
                        }else if("04".equals(Util.null2str(holdMap.get("HOLD_CODE")))){
                            if("Y".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_4","2");
                            }else if("N".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_4","1");
                            }
                        }
                    }
                }

            }
            if (!iertHblNo.isEmpty()) {
                resultMap.put("returnMessage", iertHblNo);
                manifestOrderSaveResult.setResultCode("mrnIsNull");
                manifestOrderSaveResult.setResultData(resultMap);
                return manifestOrderSaveResult;
            }


        }else{
            manifestOrderSaveResult.setResultCode("0");
        }
        sendManifest.setBmsInPlanList(list);
        return manifestOrderSaveResult;
    }

    /**
     * 허브넷 반입예정 해상 검증
     * @param sendManifest
     * @return
     */
    public ManifestOrderSaveResult verifyHubnInPlanSea(SendManifest sendManifest) {

        ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
        //대상건 조회
        List<Map<String, Object>> list = manifestMapper.selectHubnInPlanInsertTarget(sendManifest);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        String iertHblNo = "";

        if(!list.isEmpty()){
            for (Map<String, Object> validMap : list) {
                if ("".equals(Util.null2str(validMap.get("MRN"))) || "".equals(Util.null2str(validMap.get("HSN"))) || "".equals(Util.null2str(validMap.get("MSN")))) {
                    iertHblNo += validMap.get("HBL_NO") + "</br>";
                }
                
                // CD_TYPE 통관타입 값 변환
                String cdType = Util.null2str(validMap.get("CD_TYPE"));
                if("Y".equals(cdType)) {
                	validMap.put("CD_TYPE", "1");
                }else if("N".equals(cdType)) {
                	validMap.put("CD_TYPE", "0");
                }else if("Z".equals(cdType)) {
                	validMap.put("CD_TYPE", "4");
                }else if("B".equals(cdType)) {
                	validMap.put("CD_TYPE", "5");
                }
                
                // SE_INSPECTION_CODE 값에는 01,02,03 형식이라 0을 없애고 1글자만 보내야함
                String inspectionCode = Util.null2str(validMap.get("SE_INSPECTION_CODE"));
                if(inspectionCode.length() > 1) {
                	inspectionCode = inspectionCode.substring(1);
                	validMap.put("SE_INSPECTION_CODE",inspectionCode);
                }else if(inspectionCode.length() == 1){
                	if("C".equals(inspectionCode)) {
                		validMap.put("SE_INSPECTION_CODE","0");
                	}
                }
                
                //DIM 계산
                double dimLength = Util.null2dbl(validMap.get("DIM_LENGTH"));
                double dimHeight = Util.null2dbl(validMap.get("DIM_HEIGHT"));
                double dimWidth = Util.null2dbl(validMap.get("DIM_WIDTH"));
                validMap.put("PID", Util.null2str(dimLength+dimHeight+dimWidth));
                
                //HOLD List 조회
                List<Map<String,Object>> holdList = manifestMapper.selectHubnOdHoldList(validMap);
                if(holdList != null && holdList.size() > 0){
                    for(Map<String,Object> holdMap : holdList){
                        // 01 : 송장교체
                        // 02 : 퀵요청
                        // 03: 출고보류
                        // 04 : VIP화물
                        // 05 : 세관집중검사
                    	// 07 : 도착지변경
                        if("01".equals(Util.null2str(holdMap.get("HOLD_CODE")))){
                            if("Y".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_1","2");
                            }else if("N".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_1","1");
                            }
                        }else if("02".equals(Util.null2str(holdMap.get("HOLD_CODE")))){
                            if("Y".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_2","2");
                            }else if("N".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_2","1");
                            }
                        }else if("03".equals(Util.null2str(holdMap.get("HOLD_CODE")))){
                            if("Y".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_3","2");
                            }else if("N".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_3","1");
                            }
                        }else if("04".equals(Util.null2str(holdMap.get("HOLD_CODE")))){
                            if("Y".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_4","2");
                            }else if("N".equals(Util.null2str(holdMap.get("HOLD_RELEASE_YN")))){
                                validMap.put("USER_REQ_4","1");
                            }
                        }
                    }
                }
                
            }
            if (!iertHblNo.isEmpty()) {
                resultMap.put("returnMessage", iertHblNo);
                manifestOrderSaveResult.setResultCode("mrnIsNull");
                manifestOrderSaveResult.setResultData(resultMap);
                return manifestOrderSaveResult;
            }

            for (Map<String, Object> validMap : list) {
                List<Map<String, Object>> containerMapList = manifestMapper.selectOrderContainerList(validMap);
                if(containerMapList != null && containerMapList.size() > 0){
                    validMap.put("containerMapList",containerMapList);
                }
            }

            sendManifest.setBmsInPlanList(list);
        }else{
            manifestOrderSaveResult.setResultCode("0");
        }

        return manifestOrderSaveResult;
    }

    /**
     * 허브넷 반입 예정 오더 업데이트
     * @param request
     * @param sendManifest
     * @return
     */
    public ManifestOrderSaveResult updateHubnInPlan(HttpServletRequest request, SendManifest sendManifest) throws Exception{

        ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
        List<Map<String,Object>> orderList = sendManifest.getBmsInPlanList();
        List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
        
        int tmpSeq = 1;
        for(Map<String, Object> houseOrder : orderList) {
        	
        	Map<String, Object> tmpInsertPodMap = new HashMap<String, Object>();
        	
        	tmpInsertPodMap = new HashMap<String, Object>();
			tmpInsertPodMap.put("odJobNo", houseOrder.get("OD_JOB_NO"));
			tmpInsertPodMap.put("blNo", houseOrder.get("HBL_NO"));
			tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(tmpSeq++));
			tmpInsertPodMap.put("updateBlTypeMh", "H");
			insertPodList.add(tmpInsertPodMap);
        }
        
        SqlSession sqlSession = null;

		IndexApi indexApi = new IndexApi(client);
		DocumentApi documentApi = new DocumentApi(client);
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		String outFileName;
		
		String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + 1;
		
		String mblOdJobNo = Util.null2str(orderList.get(0).get("MASTER_OD_JOB_NO"));
		
        try {

            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
            ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
            OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
            int cnt =0;

            Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("userLanguage", sendManifest.getUserLanguage());
			podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
			podMap.put("srchCode", "ECM140");
			podMap.put("entCode", sendManifest.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", sendManifest.getGmtTime());
			podMap.put("solution", "ECM");
			podMap.put("updateUserId", sendManifest.getRegisterUserId());
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			podMap.put("podStatus", "ECM140");
			podMap.put("podTableName", 	"TB_ECM_POD_02");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
			
			Map<String, Object> tmpInsertPodMap = new HashMap<String, Object>();
			tmpInsertPodMap.put("odJobNo", mblOdJobNo);
			tmpInsertPodMap.put("blNo", orderList.get(0).get("MBL_NO"));
			tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(1));
			tmpInsertPodMap.put("updateBlTypeMh", "M");
			insertPodList.add(tmpInsertPodMap);
			
			if("Y".equals(sendManifest.getCancelYn())){
				for(Map<String, Object> insertPodMap : insertPodList){
					podMap.put("odJobNo", insertPodMap.get("odJobNo"));
					orderBatchMapper.deleteOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}else{
				/** 하우스선택 후 지정창고 전송시 해당 마스터 지정창고전송POD삭제     아래 반복문에서 다시 저장됨 */
				podMap.put("odJobNo", tmpInsertPodMap.get("odJobNo"));
				orderBatchMapper.deleteOrderPod(podMap);
				
				for(Map<String, Object> insertPodMap : insertPodList){
					podMap.put("odJobNo", insertPodMap.get("odJobNo"));
					podMap.put("blNo", insertPodMap.get("blNo"));
					podMap.put("podJobNo", insertPodMap.get("podJobNo"));
					orderBatchMapper.insertOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}

			/** 관우회전송정보업데이트 */
			cnt = 1;
			for(Map<String, Object> insertPodMap : insertPodList){
				insertPodMap.put("cancelYn", sendManifest.getCancelYn());
				insertPodMap.put("gmtTime", sendManifest.getGmtTime());
				insertPodMap.put("updateUserId", sendManifest.getRegisterUserId());
				insertPodMap.put("masterOdJobNo", mblOdJobNo);
				insertPodMap.put("esJobNo", logId);
				manifestBatchMapper.updateManifestGwSend(insertPodMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			if(!"Y".equals(sendManifest.getCancelYn())){
				List<Map<String, Object>> updateInspectionSeqNoList = manifestBatchMapper.selectInspectionSeqNo(podMap);
				if(updateInspectionSeqNoList != null && updateInspectionSeqNoList.size() > 0){
					cnt = 1;
					for(Map<String, Object> map : updateInspectionSeqNoList){
						manifestBatchMapper.updateInspectionSeqNo(map);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
			}
			
            for (Map<String, Object> paramMap : orderList) {
                cnt++;
                //반입 예정 정보 이력 업데이트 G1
                manifestBatchMapper.updateHubnBmsInInfo(paramMap);
                if (cnt % 1000 == 0 || cnt == orderList.size()) {
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }

			sqlSession.flushStatements();
			sqlSession.clearCache();
            sqlSession.commit();
        }catch (Exception e){
            sqlSession.close();
            throw e;
        } finally {
        	sqlSession.close();
        }

		String key = "";
		if("Y".equals(sendManifest.getCancelYn())){
			key = "E-COMMERCE_CARGO_PROGRESS_DELETE";
		}else{
			key = "E-COMMERCE_CARGO_PROGRESS_INSERT";
		}
		ListOperations<String, String> listOperations = redisTemplate.opsForList();
		JSONObject json = new JSONObject();
		for(Map<String, Object> houseOrder : orderList){
			// 2024-11-18 통관타입이 목록이면서, 제출번호가 없는 경우에도 redis에 넣도록 수정
			// 2025-02-13 통관타입이 목록이면서, 제출번호가 있으면서 목록신고 이력이 없는 경우 redis에 넣도록 수정
			if(!"Y".equals(houseOrder.get("CD_TYPE")) || ("Y".equals(houseOrder.get("CD_TYPE")) && "".equals(Util.null2str(houseOrder.get("SIMPLE_DOC_NO"))))
						|| ("Y".equals(houseOrder.get("CD_TYPE")) && !"".equals(Util.null2str(houseOrder.get("SIMPLE_DOC_NO"))) && "".equals(Util.null2str(houseOrder.get("SIMPLE_EDI_STATUS"))))){
				
				String destCode = Util.null2str(houseOrder.get("DEST_CODE"));
				String destPort = Util.null2str(houseOrder.get("DEST_PORT"));
				
				json.put("entCode", houseOrder.get("ENT_CODE"));
				json.put("entBusinessCode", houseOrder.get("ENT_BUSINESS_CODE"));
				json.put("bound", houseOrder.get("BOUND"));
				json.put("boundType", houseOrder.get("BOUND_TYPE"));
				json.put("customs", houseOrder.get("CUSTOMS_CODE"));
				json.put("status", houseOrder.get("LAST_POD_STATUS"));
				json.put("odJobNo", houseOrder.get("OD_JOB_NO"));
				json.put("masterOdJobNo", houseOrder.get("MASTER_OD_JOB_NO"));
				json.put("mblNo", houseOrder.get("MBL_NO"));
				json.put("hblNo", houseOrder.get("HBL_NO"));
				json.put("arDate", Util.null2str(houseOrder.get("AR_DATE")));
				json.put("dropYn", houseOrder.get("DROP_YN"));
				json.put("cdType", houseOrder.get("CD_TYPE"));
				json.put("destCode", houseOrder.get("DEST_CODE"));
				json.put("destPort", houseOrder.get("DEST_PORT"));
				json.put("bondedCargoYn", destCode.equals(destPort) ? "Y" : "N");
				json.put("action", "sendGw");
				json.put("mrn", houseOrder.get("MRN"));
				listOperations.rightPush(key, json.toString());
			}
		}
        
		/** 처리 로그 기록 */
		tmpMap.put("ent_code", sendManifest.getEntCode());
		tmpMap.put("customer_code", "");
		tmpMap.put("solution", "ECM");
		tmpMap.put("bound", sendManifest.getBound());
		tmpMap.put("bound_type", "IMP");
		tmpMap.put("hbl_no", "");
		tmpMap.put("if_service_code", "GW");
//		tmpMap.put("if_doc_code", fileCreateRetVal[4]);
		tmpMap.put("if_doc_code", "DESIGNATED_WAREHOUSE");
		tmpMap.put("if_doc_name", "지정장치장");
		tmpMap.put("if_doc_reference_no", "");
		tmpMap.put("if_vender", "GW");
		tmpMap.put("if_send_recv_type", "S");
		tmpMap.put("if_send_recv_method", "DB TO DB");
		tmpMap.put("if_file_path", "");
		tmpMap.put("if_file_name", "");
		tmpMap.put("if_status", "SEND");
		tmpMap.put("if_status_info", "");
		tmpMap.put("if_sender_id", "");
		tmpMap.put("if_receiver_id", "");
		tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
		tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
		if("Y".equals(sendManifest.getCancelYn())){
			tmpMap.put("if_remark", "지정창고 반입취소전송");
		}else{
			tmpMap.put("if_remark", "지정창고 반입전송");
		}
		tmpMap.put("register_user_id", sendManifest.getRegisterUserId());
		tmpMap.put("register_user_name", sendManifest.getRegisterUserName());
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", logId);
			
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		documentApi.createDocument(index, logId, tmpMap);
		
        manifestOrderSaveResult.setResultCode(Util.null2str(orderList.size()));
        return manifestOrderSaveResult;
    }
}