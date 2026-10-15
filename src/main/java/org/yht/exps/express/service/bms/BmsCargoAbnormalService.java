package org.yht.exps.express.service.bms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsCargoAbnormalMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.carryin.InPlanList;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.edi.SendDelivery;
import org.yht.exps.express.model.edi.abnormal.Abnormal;
import org.yht.exps.express.model.edi.abnormal.AbnormalList;
import org.yht.exps.express.model.edi.abnormal.AbnormalSaveResult;
import org.yht.exps.express.model.edi.abnormal.search.SearchAbnormalList;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.bms.send.BmsSendXmlParser;
import org.yht.exps.express.service.edi.EdiCommonService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsCargoAbnormalService {
		
	@Autowired
	BmsCargoAbnormalMapper bmsCargoAbnormalMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	CustomProperties customProperties;
	@Autowired
    EnterpriseMapper enterpriseMapper;
	@Autowired
	SmsUserService smsUserService;
	@Autowired
    CommonService commonService;
	@Autowired
	EdiCommonService ediCommonService;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	PlatformTransactionManager transactionManager;
	@Autowired
	BmsSendXmlParser bmsSendXmlParser;
	@Autowired
	BmsCommService bmsCommService;
	
	@Resource(name = "fileService")
	FileService fileService;
		
	/**
	 * ECM 하기이상결과보고신고 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public AbnormalList selectAbnormalList(SearchAbnormalList search) throws Exception {
		AbnormalList abnormalList = new AbnormalList();
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
		
		List<Abnormal> list = null;
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "bip.AR_DATE|odAdnor.REGISTER_DATE_TIME|od.AR_DATE|od.OB_DATE", "srchNoType", "bip.MBL_NO|bip.BL_NO|SUBSTRING(bip.CARGO_REFERENCE_NO, 1, 11)|od.MBL_NO|od.HBL_NO|odAdnor.MRN");
		list  = bmsCargoAbnormalMapper.selectBmsInPlanAbnormalList(search);
		
		abnormalList.setAbnormalList(list);
		if(list != null && list.size() > 0) {
			abnormalList.setTotal(list.get(0).getTotal());
		}else {
			abnormalList.setTotal(0);
		}
		
		return abnormalList;
	}
	
	/**
	 * ECM 하기이상결과보고신고 엔트리 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public AbnormalList selectAbnormalEntryList(SearchAbnormalList search) throws Exception {
		AbnormalList abnormalList = new AbnormalList();
		if("PANTK".equals(search.getSrchEntCode())) {
			abnormalList.setAbnormalList(bmsCargoAbnormalMapper.selectBmsInPlanAbnormalEntryList(search));
		}else{
			abnormalList.setAbnormalList(bmsCargoAbnormalMapper.selectAbnormalEntryList(search));
		}
		return abnormalList;
	}
		
	/**
	 * ECM 하기이상결과보고신고 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public AbnormalList selectAbnormalDeclareList(SearchAbnormalList search) throws Exception {
		AbnormalList abnormalList = new AbnormalList();
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

		List<Abnormal> list = null;
		// 신고일자 SEND_DATE 에서 DECLARATION_DATE로 변경 2025.12.12 2sowell
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "A.DECLARATION_DATE|A.AR_DATE", "srchNoType", "A.MBL_NO|A.HBL_NO|A.MRN");
		list  = bmsCargoAbnormalMapper.selectBmsAbnormalDeclareList(search);

		abnormalList.setAbnormalList(list);
		if(list != null && list.size() > 0) {
			abnormalList.setTotal(list.get(0).getTotal());
		}else {
			abnormalList.setTotal(0);
		}
		
		return abnormalList;
	}
	
	/**
	 * ECM 하기이상결과보고 추가팝업 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public AbnormalList selectAbnormalAddPopupList(SearchAbnormalList search) throws Exception {
		AbnormalList abnormalList = new AbnormalList();
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<Abnormal> list  = bmsCargoAbnormalMapper.selectAbnormalAddPopupList(search);
		abnormalList.setAbnormalList(list);
		if(list != null && list.size() > 0) {
			abnormalList.setTotal(list.get(0).getTotal());
		}else {
			abnormalList.setTotal(0);
		}
		return abnormalList;
	}
	
	/**
	 * 하기이상결과 생성
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public AbnormalSaveResult insertAbnormalList(AbnormalList list) throws Exception {
		AbnormalSaveResult abnormalSaveResult = new AbnormalSaveResult();
		
		List<Abnormal> abnormalList = list.getAbnormalList();		
		if(abnormalList == null) {
			return abnormalSaveResult;
		}
		Util.accessControlDatabase(list, "gmtTime", false, null, null, null, null);
		for(int i=0;i<abnormalList.size();i++) {
			Abnormal ab = abnormalList.get(i);
			ab.setRegisterUserId(list.getRegisterUserId());
			ab.setGmtTime(list.getGmtTime());

			bmsCargoAbnormalMapper.insertAbnormalList(ab);
		}
		
		return abnormalSaveResult;
	}
	
	/**
	 * 하기이상결과보고신고 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public AbnormalSaveResult deleteAbnormalDeclareList(AbnormalList list) throws Exception {
		AbnormalSaveResult abnormalSaveResult = new AbnormalSaveResult();
		List<Abnormal> abnormalList = list.getAbnormalList();		
		if(abnormalList == null) {
			return abnormalSaveResult;
		}
		
		String errorList = "";
		list.setAction("sendDel");
		List<Map<String, Object>> chkList = bmsCargoAbnormalMapper.selectAbnormalDeleteCheck(list);
		/** MGN=>MFN생성하고 MGN만 신고 후 삭제할경우 삭제불가    MGN,MFN둘다 신고 후 삭제할경우 둘다 삭제 */
		for(Map<String, Object> map : chkList){
			if("N".equals(map.get("MGN_MFN")) && Util.null2int(map.get("SEQ_NO_ID")) == 1){
				errorList += ", " + Util.null2str(map.get("HBL_NO"));
			}
		}
		
		if(errorList.length() > 2){
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("errorList", errorList.substring(2));
			abnormalSaveResult.setResultData(resultData);
			abnormalSaveResult.setResultCode("ABMS100004");	/** 삭제할 수 없습니다.(#1#) */
			return abnormalSaveResult;
		}
		
		List<Map<String, Object>> delSpecialItemList = new ArrayList<Map<String, Object>>();
		String abnormalCode;
		if(!"PANTK".equals(list.getEntCode())){
			for(Map<String, Object> map : chkList){
				abnormalCode = Util.null2str(map.get("ABNORMAL_CODE"));
				if("MFN".equals(abnormalCode) || (("MGN".equals(abnormalCode) || "SHT".equals(abnormalCode)) && map.get("MGN_MFN") != null)){
					delSpecialItemList.add(map);
				}
			}
		}
		
		
		for(int i=0;i<abnormalList.size();i++) {
			Abnormal ab = abnormalList.get(i);
			/** MGN(SHT),MFN 둘다 신고 후 첫번째로 삭제할 경우 MFN만 삭제, 두번째 삭제 시 MGN(SHT) 삭제 */
			if(!"".equals(Util.null2str(ab.getMgnMfnSendDate()))) {
				ab.setSeqNoId(2);
			}
			if(!"PANTK".equals(list.getEntCode())){
				bmsCargoAbnormalMapper.updateAbnormalAdditional(ab);
			}
		}
		
		bmsCargoAbnormalMapper.deleteAbnormalDeclareList(list);
		if(delSpecialItemList != null && delSpecialItemList.size() > 0) {
			bmsCargoAbnormalMapper.deleteSpecialItemList(delSpecialItemList);
		}
		
		if(!"PANTK".equals(list.getEntCode())){
			for(int i=0;i<abnormalList.size();i++) {
				Abnormal ab = abnormalList.get(i);
				if("".equals(Util.null2str(ab.getMgnMfnSendDate()))) {
					bmsCargoAbnormalMapper.deleteAbnormalDeclarePod(ab);
					bmsCargoAbnormalMapper.updateLastPodMax(ab);
				}
			}
		}
		
		return abnormalSaveResult;
	}
	
	/**
	 * 하기코드 존재하는 하우스 건수 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getNoRegisterCount(Abnormal abnormal, HttpServletRequest request) throws Exception {
		
		/* N개 검색(멀티타입) */
		String srchNo = Util.null2str(abnormal.getSrchNo());
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(srchNo)) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(srchNo).split(",")));
		}
		if(searchNoList.size() > 1){
			abnormal.setSearchType("multi");
			abnormal.setSearchNoList(searchNoList);
		}else{
			srchNo = srchNo.trim().replace(",", "");
			abnormal.setSearchType("");
			abnormal.setSrchNo(srchNo);
		}
		
		Map<String, Object> map = bmsCargoAbnormalMapper.getNoRegisterCount(abnormal);
		int mgnCnt = Util.null2int(map.get("MGN_CNT"));
		int mgnMfnCnt = Util.null2int(map.get("MGN_MFN_CNT"));
		int otherCnt = Util.null2int(map.get("OTHER_CNT"));
		if(mgnCnt > 0 && mgnCnt != mgnMfnCnt){
			map.put("noRegisterCnt", 1);
			return map;
		}else if(otherCnt > 0){
			map.put("noRegisterCnt", 2);
			return map;
		}
		map.put("noRegisterCnt", 0);
		return map;
	}
	
	/**
	 * 무적화물생성 생성
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public AbnormalSaveResult insertNoRegister(Abnormal abnormal, HttpServletRequest request) throws Exception {
		AbnormalSaveResult abnormalSaveResult = new AbnormalSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		//적하/목록신고(in)화면에서 하우스 여러 건 선택해 무적화물 생성할 경우 각 하우스에 대해 로그 남기기 위해 선택한 하우스 리스트의 hblNo, odJobNo 함께 전달
		Map<String, String> map = new HashMap<String, String>();
		List<Map> noList = new ArrayList<Map>();
		int dupCnt = 0;
		
		tmpMap.put("srchEntCode", abnormal.getEntCode());
		tmpMap.put("srchEntBusinessCode", abnormal.getEntBusinessCode());
		tmpMap.put("srchBound", abnormal.getBound());
		tmpMap.put("srchEdiCityPort", abnormal.getDestPort());
		
		tmpMap = commonMapper.selectBusEdiCodeInfo(tmpMap);
		if(tmpMap != null){
			abnormal.setDiscWhCode(Util.null2str(tmpMap.get("EDI_DISC_WH_CODE")));
		}else{
		 	tmpMap = new HashMap<String, Object>();
		}
//		if(abnormal.getDiscWhCode().equals("")){
//			abnormalSaveResult.setResultCode("A11271");	/** 관세청 EDI정보가 없습니다. */
//			return abnormalSaveResult;
//		}
		
		
				
		if(!Util.null2str(abnormal.getMasterOdJobNo()).equals("")){
			/** 비엘번호 중복체크 */ /** 마스터의 모든 하우스 일괄 처리하도록 변경하면서 새로운 하우스 생성하는 부분 주석처리 */
//			tmpMap.clear();
//			tmpMap.put("odJobNo", abnormal.getOdJobNo());
//			tmpMap.put("blTypeMh", "H");
//			tmpMap.put("entCode", abnormal.getEntCode());
//			tmpMap.put("blNo", abnormal.getHblNo());
//			Map<String, Object> blNoChkMap = orderMapper.selectOrderByBlNo(tmpMap);
//			if(blNoChkMap != null && blNoChkMap.size() > 0){
//				abnormalSaveResult.setResultCode("duplicateBlNo");
//				return abnormalSaveResult;
//			}
//			tmpMap.clear();
//			tmpMap.put("odJobNo", abnormal.getMasterOdJobNo());
//			tmpMap = bmsCargoAbnormalMapper.selectAbnormalChk(tmpMap);
//			int hsn = Util.null2int(tmpMap.get("MAX_HSN")) + 1;
//			if(hsn > 9999){
//				abnormalSaveResult.setResultCode("A11287");	/** 사용가능한 HSN이 없습니다. */
//				return abnormalSaveResult;
//			}
//			abnormal.setHsn(Util.lpad(Util.null2str(hsn), 4, '0'));
//			abnormal.setCdType("N");
//			abnormal.setCdUseType("1");
//			abnormal.setEcmType("0");
//			
//			tmpMap.clear();
//			tmpMap.put("gmtTime", abnormal.getGmtTime());
//			tmpMap.put("entCode", abnormal.getEntCode());
//			tmpMap.put("entBusinessCode", "");
//			tmpMap.put("registerUserId", abnormal.getRegisterUserId());
//			tmpMap.put("updateUserId", abnormal.getRegisterUserId());
//			tmpMap.put("solution", "ECM");
//			tmpMap.put("stockType", "OD");
//			tmpMap.put("stockSign", "COD");
//			tmpMap.put("stockSeqLength", 8);
//			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
//			tmpMap.put("stockRemark", "ORDER-JOB NO");
//			abnormal.setOdJobNo(commonMapper.selectNextStockMaxNo(tmpMap));
//			
//			bmsCargoAbnormalMapper.insertAbnormalOrder(abnormal);
//			bmsCargoAbnormalMapper.insertAbnormalOrderAdditional(abnormal);
//			List<String> odJobNoList = new ArrayList<String>();
//			odJobNoList.add(abnormal.getOdJobNo());
//			abnormal.setOdJobNoList(odJobNoList);
//			
//			//1. 적하목록신고 화면에서 M 1개를 선택한 경우
//			map.put("odJobNo", abnormal.getOdJobNo());
//			map.put("hblNo", abnormal.getHblNo());
//			noList.add(map);
			
			
			/* N개 검색(멀티타입) */
			String srchNo = Util.null2str(abnormal.getSrchNo());
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(srchNo)) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(srchNo).split(",")));
			}
			if(searchNoList.size() > 1){
				abnormal.setSearchType("multi");
				abnormal.setSearchNoList(searchNoList);
			}else{
				srchNo = srchNo.trim().replace(",", "");
				abnormal.setSearchType("");
				abnormal.setSrchNo(srchNo);
			}
			
			//1. 적하목록신고 화면에서 M 1개를 선택한 경우 마스터로 조회한 hblNoList 전달
			if("masterMgnMfn".equals(abnormal.getModType())){
				noList = bmsCargoAbnormalMapper.selectHouseListMgnMfn(abnormal);
			}else {
				tmpMap.clear();
				tmpMap.put("odJobNo", abnormal.getMasterOdJobNo());
				tmpMap.put("srchCarryList", abnormal.getSrchCarryList());
				tmpMap.put("srchCdTypeList", abnormal.getSrchCdTypeList());
				tmpMap.put("srchCustomsIdVerifyList", abnormal.getSrchCustomsIdVerifyList());
				tmpMap.put("srchEcmTypeList", abnormal.getSrchEcmTypeList());
				tmpMap.put("srchEcmBuyTypeList", abnormal.getSrchEcmBuyTypeList());
				tmpMap.put("srchNo", abnormal.getSrchNo());
				tmpMap.put("srchNoType", abnormal.getSrchNoType());
				tmpMap.put("srchEtcYn", abnormal.getSrchEtcYn());
				tmpMap.put("srchInspectionYn", abnormal.getSrchInspectionYn());
				tmpMap.put("srchSeInspectionYn", abnormal.getSrchSeInspectionYn());
				tmpMap.put("srchXrayYn", abnormal.getSrchXrayYn());
				tmpMap.put("srchDropYn", abnormal.getSrchDropYn());
				tmpMap.put("srchAbnormalYn", abnormal.getSrchAbnormalYn());
				tmpMap.put("srchSimpleEdiStatus", abnormal.getSrchSimpleEdiStatus());
				tmpMap.put("srchSimpleStatementYn", abnormal.getSrchSimpleStatementYn());
				tmpMap.put("srchCriminalYn", abnormal.getSrchCriminalYn());
				tmpMap.put("searchType", abnormal.getSearchType());
				tmpMap.put("searchNoList", abnormal.getSearchNoList());
				noList = bmsCargoAbnormalMapper.selectHouseList(tmpMap);
			}
		}
		//하우스 별로 로그 남기기 위함
		else if(abnormal.getOdJobNoList() != null && abnormal.getOdJobNoList().size() > 0) {
			//2. 적하목록신고 화면에서 여러 하우스 선택한 경우 hblNoList 전달
			//	ex) [{odJobNo=COD220429-00000001, hblNo=20220428_H2, SEQ=0}, {odJobNo=COD220429-00000002, hblNo=20220428_H3, SEQ=1}]
			noList = abnormal.getNoList();
		}else {
			//3. 하기이상결과보고 화면에서 하우스 1개 선택한 경우
			map.put("odJobNo", abnormal.getOdJobNo());
			map.put("hblNo", abnormal.getHblNo());
			noList.add(map);
		}
		
		
		int listSize = noList.size();
		if(!"masterMgnMfn".equals(abnormal.getModType())){
			tmpMap.clear();
			tmpMap.put("seqNoId", abnormal.getSeqNoId());
			for(int i = 0; i < listSize; i += 1000){
				if(i + 1000 < listSize) {
					tmpMap.put("dataList", noList.subList(i, i + 1000));
				}else{
					tmpMap.put("dataList", noList.subList(i, listSize));
				}
				if(bmsCargoAbnormalMapper.selectAbnormalDupChk(tmpMap) > 0){
					abnormalSaveResult.setResultCode("A11253");
					return abnormalSaveResult;
				}
			}
			Util.accessControlDatabase(abnormal, "gmtTime", false, null, null, null, null);
			if("PANTK".equals(abnormal.getEntCode())){
				bmsCargoAbnormalMapper.insertBmsInPlanAbnormal(abnormal);
			}else{
				bmsCargoAbnormalMapper.insertAbnormal(abnormal);
			}
			
			// MFN 신고시에 별도관리물품 데이터 생성 
			// 1.선착화물
			// 2.후착화물
			if("MFN".equals(abnormal.getAbnormalCode()) && !"PANTK".equals(abnormal.getEntCode())) {
				
				// HAWB로 선택해서 MFN 신고하는 경우
				if(abnormal.getMfnOdJobNoList() != null) {
					
					for(int i=0; i<abnormal.getMfnOdJobNoList().size(); i++) {
						
						Map<String, Object> ab = new HashMap<>();
						
						//1.선착화물
						ab.put("MANAGE_TYPE", "2");
						ab.put("OD_JOB_NO", abnormal.getMfnOdJobNoList().get(i).get("odJobNo"));
						ab.put("FLIGHT_NO", abnormal.getVesselOriFlight());
						ab.put("DEST_PORT", abnormal.getDestPort());
						ab.put("AR_DATE", abnormal.getArOriDate());
						ab.put("ITEM_DESC", abnormal.getMfnOdJobNoList().get(i).get("commodityItem"));
						ab.put("GWT", abnormal.getMfnOdJobNoList().get(i).get("abnormalGwt"));
						ab.put("PIECES", abnormal.getMfnOdJobNoList().get(i).get("abnormalPieces"));
						bmsCargoAbnormalMapper.insertSpecialManageItem(ab);
						
						//2.후착화물
						ab.put("MANAGE_TYPE", "3");
						ab.put("FLIGHT_NO", abnormal.getVesselFlight());
						ab.put("AR_DATE", abnormal.getArDate());
						ab.put("GWT", abnormal.getGwt());
						ab.put("PIECES", abnormal.getPieces());
						bmsCargoAbnormalMapper.insertSpecialManageItem(ab);
					}
				}
				//MAWB로 선택한 경우
				if(!Util.null2str(abnormal.getMasterOdJobNo()).equals("")){
					
					Map<String, Object> ab = new HashMap<>();
					
					//1.선착화물
					ab.put("MANAGE_TYPE", "2");
//					ab.put("OD_JOB_NO", abnormal.getOdJobNo());
					ab.put("MASTER_OD_JOB_NO", abnormal.getMasterOdJobNo());
					ab.put("FLIGHT_NO", abnormal.getVesselFlight());
					ab.put("DEST_PORT", abnormal.getDestPort());
					ab.put("AR_DATE", abnormal.getArDate());
					ab.put("ITEM_DESC", "");
					ab.put("GWT", 0);
					ab.put("PIECES", 0);
					ab.put("srchCarryList", abnormal.getSrchCarryList());
					ab.put("srchCdTypeList", abnormal.getSrchCdTypeList());
					ab.put("srchCustomsIdVerifyList", abnormal.getSrchCustomsIdVerifyList());
					ab.put("srchEcmTypeList", abnormal.getSrchEcmTypeList());
					ab.put("srchEcmBuyTypeList", abnormal.getSrchEcmBuyTypeList());
					ab.put("srchNo", abnormal.getSrchNo());
					ab.put("srchNoType", abnormal.getSrchNoType());
					ab.put("srchEtcYn", abnormal.getSrchEtcYn());
					ab.put("srchInspectionYn", abnormal.getSrchInspectionYn());
					ab.put("srchSeInspectionYn", abnormal.getSrchSeInspectionYn());
					ab.put("srchXrayYn", abnormal.getSrchXrayYn());
					ab.put("srchDropYn", abnormal.getSrchDropYn());
					ab.put("srchAbnormalYn", abnormal.getSrchAbnormalYn());
					ab.put("srchSimpleEdiStatus", abnormal.getSrchSimpleEdiStatus());
					ab.put("srchSimpleStatementYn", abnormal.getSrchSimpleStatementYn());
					ab.put("srchCriminalYn", abnormal.getSrchCriminalYn());
					ab.put("searchType", abnormal.getSearchType());
					ab.put("searchNoList", abnormal.getSearchNoList());
					bmsCargoAbnormalMapper.insertSpecialManageItem(ab);
					
					//2.후착화물
					ab.put("MANAGE_TYPE", "3");
					ab.put("GWT", abnormal.getGwt());
					ab.put("PIECES", abnormal.getPieces());
					bmsCargoAbnormalMapper.insertSpecialManageItem(ab);
				}
			}
		}else{
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				BmsCargoAbnormalMapper abnormalBatchMapper = sqlSession.getMapper(BmsCargoAbnormalMapper.class);
				
				int cnt = 1;
				Abnormal tmpAbnormal = abnormal.clone();
				tmpAbnormal.setSeqNoId(2);
				Util.accessControlDatabase(tmpAbnormal, "gmtTime", false, null, null, null, null);
				for(Map noMap : noList) {
					tmpAbnormal.setOdJobNo(Util.null2str(noMap.get("odJobNo")));
					tmpAbnormal.setHblPieces(Util.null2int(noMap.get("pieces")));
					abnormalBatchMapper.insertAbnormalMgnMfn(tmpAbnormal);
					if((cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				
				if("MFN".equals(abnormal.getAbnormalCode())) {
					cnt = 1;
					tmpMap.clear();
					tmpMap.put("FLIGHT_NO", abnormal.getVesselFlight());
					tmpMap.put("DEST_PORT", abnormal.getDestPort());
					tmpMap.put("AR_DATE", abnormal.getArDate());
					tmpMap.put("ITEM_DESC", "");
					for(Map noMap : noList) {
						tmpMap.put("OD_JOB_NO", noMap.get("odJobNo"));
						tmpMap.put("MANAGE_TYPE", "2");
						tmpMap.put("GWT", 0);
						tmpMap.put("PIECES", 0);
						abnormalBatchMapper.insertSpecialManageItemMgnMfn(tmpMap);
						tmpMap.put("MANAGE_TYPE", "3");
						tmpMap.put("GWT", abnormal.getGwt());
						tmpMap.put("PIECES", abnormal.getPieces());
						abnormalBatchMapper.insertSpecialManageItemMgnMfn(tmpMap);
						if((cnt++ % 500) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
					
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
				
				sqlSession.commit();
			}catch(Exception e) {
				throw e;
			}finally{
				sqlSession.close();
			}
		}

		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", abnormal.getEntCode());
		tmpLogMap.put("ent_business_code", abnormal.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("bound", "AIR");
		tmpLogMap.put("bound_type", "IMP");
		tmpLogMap.put("process_user_id", abnormal.getRegisterUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", abnormal.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", abnormal.getLogUserName());
		tmpLogMap.put("process_user_name_eng", abnormal.getLogUserNameEng());
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		for(int i=0; i<noList.size(); i++) {
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(i));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("job_no", Util.null2str(noList.get(i).get("odJobNo")));
			logMap.put("description",(abnormal.getOdJobNoList() != null ? "적하/목록신고" : "하기이상결과보고") + " > 무적화물생성" +" [MBL_NO : " + abnormal.getMblNo() + " HBL_NO : "+ noList.get(i).get("hblNo") +"]");
//			logMap.put("job_log", new Gson().toJson(abnormal));
			logMap.put("job_log", "");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}
		commonService.insertLogBulk(bulkList);
		
		
//		List<Abnormal> abnormalList = list.getAbnormalList();		
//		if(abnormalList == null) {
//			return abnormalSaveResult;
//		}
//		
//		//이상코드 MGN 인경우 => MFN 생성 && 별도관리물품 데이터 생성
//		//이상코드 SHT or OVR 인경우 => 별도관리물품 데이터 생성
//		for(int i=0;i<abnormalList.size();i++) {
//			Abnormal ab = abnormalList.get(i);
//			ab.setMfnOdJobNo(list.getMfnOdJobNo());
//			ab.setRegisterUserId(list.getRegisterUserId());
//			ab.setGmtTime(list.getGmtTime());
//
//			String abCd = Util.null2str(ab.getAbnormalCode());
//			
//			if("MGN".equals(abCd)) {
//				ab.setAbnormalCode("MFN");				
//				bmsCargoAbnormalMapper.insertAbnormal(ab);				
//				bmsCargoAbnormalMapper.insertSpecialItem(ab);
//			}
//			else if("SHT".equals(abCd) || "OVR".equals(abCd)) {
//				bmsCargoAbnormalMapper.insertSpecialItem(ab);
//			}
//		}
		
		return abnormalSaveResult;
	}
	
	/**
	 * 하기이상결과 적하정정 생성
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public AbnormalSaveResult insertEdiModList(AbnormalList list) throws Exception {
		AbnormalSaveResult abnormalSaveResult = new AbnormalSaveResult();
		
		List<Abnormal> abnormalList = list.getAbnormalList();		
		if(abnormalList == null || abnormalList.size() == 0) {
			abnormalSaveResult.setResultCode("A00049");	/** 자료를 선택바랍니다. */
			return abnormalSaveResult;
		}
		
		Abnormal tmpAbnormal = abnormalList.get(0).clone();
		String modType = Util.null2str(list.getModType());
		String bound = Util.null2str(list.getBound());
		
		if(Util.null2str(tmpAbnormal.getMrn()).length() != 11){
			abnormalSaveResult.setResultCode("A11275");	/** #1#을 #2#자리로 입력하십시오. */
			return abnormalSaveResult;
		}
		
		String modJobNo = "";
		String modSerialNo = "";
		String b2gForwarderSign = null;
		int hblCnt = 0;
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", list.getEntCode());
		tmpMap.put("entBusinessCode", list.getEntBusinessCode());
		Map<String, Object> fwSendAccInfo = manifestMapper.getFwSendAccInfo(tmpMap);
		if(fwSendAccInfo == null || fwSendAccInfo.size() == 0 || Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).equals("") || Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).length() != 4){
			abnormalSaveResult.setResultCode("A11296");	/** 신청인부호를 4자리로 입력하십시오. */
			return abnormalSaveResult;
		}else{
			b2gForwarderSign = Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN"));
		}
		
		int insertCnt = 1;
		String cntrModType = null;
		String cntrNo = null;
		Map<String, List<Map<String, Object>>> containerNoMap = null;
		List<String> singleModCodeList = null;
		List<Map<String, Object>> containerList = null;
		if(modType.equals("05") && Util.null2str(list.getSingleModCode()).equals("")){
			singleModCodeList = list.getSingleModCodeList();
			insertCnt = singleModCodeList.size();
		}else if(modType.equals("99")){
			containerList = list.getContainerList();
			cntrModType = list.getCntrModType();
			cntrNo = Util.null2str(containerList.get(0).get("cntrNo"));
		}else if(modType.equals("05") || bound.equals("SEA")) {
			containerList = list.getContainerList();
		}
		
		/** 컨테이너번호 중복체크 / 컨테이너EDI코드 처리 */
		/** 2025-05-07 컨테이너번호 중복체크는 하우스단위로 처리하도록 수정 */
		Map<String, Object> updateCntrNoChk2 = new HashMap<String, Object>();
		if("99".equals(modType) && !"D".equals(cntrModType)){
			List<String> updateCntrNoBeforeList = new ArrayList<String>();
			List<String> insertCntrNoList = new ArrayList<String>();
			if("U".equals(cntrModType)){
				Map<String, Object> cntrEdiCodeMap = new HashMap<String, Object>();
				String beforeCntrNo, afterCntrNo;
				for(Map<String, Object> containerMap : containerList){
					if("EH".equals(containerMap.get("cntrModCode"))){	/** EH : 컨테이너번호정정 */
						beforeCntrNo = Util.null2str(containerMap.get("cntrNo"));		/** 변경 전 컨테이너번호 */
						afterCntrNo = Util.null2str(containerMap.get("afterValue"));	/** 변경 후 컨테이너번호 */
						if(updateCntrNoChk2.get(beforeCntrNo) == null){
							updateCntrNoChk2.put(beforeCntrNo, afterCntrNo);
							updateCntrNoBeforeList.add(beforeCntrNo);
						}
					}else if("EA".equals(containerMap.get("cntrModCode"))){	/** EA : 컨테이너 SIZE/TYPE     정정내용에 사용자가 입력하는 내용은 CNTR_EDI_CODE */
						afterCntrNo = Util.null2str(containerMap.get("afterValue"));
						if(cntrEdiCodeMap.get(afterCntrNo) == null){
							tmpMap.put("groupCode", "CONTAINER");
							tmpMap.put("srchEtcFieldOnly", "Y");
							tmpMap.put("srchEtcField", "ETC3");
							tmpMap.put("srchEtcFieldValue", afterCntrNo);
							Util.accessControlDatabase(tmpMap, "srchEtcField", false, null, null, null, null);
							tmpMap = commonMapper.getCodeDetail(tmpMap);
							if(tmpMap == null){
								abnormalSaveResult.setResultCode("A00062");	/** #1# Code가 없습니다. */
								return abnormalSaveResult;
							}
							cntrEdiCodeMap.put(afterCntrNo, tmpMap.get("CODE"));
						}
						containerMap.put("cntrCode", cntrEdiCodeMap.get(afterCntrNo));
					}
				}
				
				/** 쿼리 배치처리하기위해서 컨테이너번호별로 처리 */
				List<Map<String, Object>> containerChkList;
				containerNoMap = new HashMap<String, List<Map<String, Object>>>();
				String cntrNoChk;
				
				for(Map<String, Object> containerMap : containerList){
					cntrNoChk = Util.null2str(containerMap.get("cntrNo"));
					if(containerNoMap.get(cntrNoChk) == null){
						containerChkList = new ArrayList<Map<String, Object>>();
					}else{
						containerChkList = containerNoMap.get(cntrNoChk);
					}
					containerChkList.add(containerMap);
					containerNoMap.put(cntrNoChk, containerChkList);
				}
			}else {
				for(Map<String, Object> containerMap : containerList){
					insertCntrNoList.add(Util.null2str(containerMap.get("cntrNo")));
				}
			}
			
			tmpMap.put("action", list.getAction());
			tmpMap.put("masterOdJobNo", tmpAbnormal.getOdJobNo());
			tmpMap.put("modType", modType);
			tmpMap.put("cntrModType", cntrModType);
			if(updateCntrNoBeforeList.size() > 0 || "A".equals(cntrModType)){
				tmpMap.put("abnormalList", list.getAbnormalList());
				if("A".equals(cntrModType)){
					// 2024.11.01 컨테이너 추가 : 1개 -> N개 등록
//					tmpMap.put("cntrNo", containerList.get(0).get("cntrNo"));
					tmpMap.put("insertCntrNoList", insertCntrNoList);
					tmpMap = bmsCargoAbnormalMapper.selectAbnormalCntrInfo(tmpMap);
					if(tmpMap != null){
						if(Util.null2int(tmpMap.get("CNTR_CNT")) > 0){
							abnormalSaveResult.setResultCode("A11297");	/** 이미 사용된 컨테이너번호입니다. */
							return abnormalSaveResult;
						}
					}else{
						tmpMap = new HashMap<String, Object>();
					}
				}else{
					/** 2025-05-07 컨테이너번호 업데이트 중복처리 */
					tmpMap.put("updateCntrNoList", updateCntrNoBeforeList);
					List<Map<String, Object>> abnormalCntrList = bmsCargoAbnormalMapper.selectAbnormalCntrInfoList(tmpMap);
					if(abnormalCntrList != null && abnormalCntrList.size() > 0) {
						String chkOdJobNo = null;
						String chkUpdateCntrNo = null;
						List<String> updateCntrNoCheckList = new ArrayList<String>();
						tmpMap.clear();
						Map<String, Object> cntrNoChk = new HashMap<String, Object>();
						for(Map<String, Object> cntrMap : abnormalCntrList){
							if(chkOdJobNo != null && !chkOdJobNo.equals(cntrMap.get("OD_JOB_NO"))){
								tmpMap.put("odJobNo", chkOdJobNo);
								tmpMap.put("updateCntrNoList", updateCntrNoCheckList);
								cntrNoChk = bmsCargoAbnormalMapper.selectAbnormalCntrInfo(tmpMap);
								if(cntrNoChk != null){	/** 해당 하우스에 이미 등록되어있는 컨테이너번호로 변경 */
									abnormalSaveResult.setResultCode("A11297");	/** 이미 사용된 컨테이너번호입니다. */
									return abnormalSaveResult;
								}
								
								cntrNoChk = new HashMap<String, Object>();
								updateCntrNoCheckList = new ArrayList<String>();
							}
							chkOdJobNo = Util.null2str(cntrMap.get("OD_JOB_NO"));
							chkUpdateCntrNo = Util.null2str(updateCntrNoChk2.get(Util.null2str(cntrMap.get("CNTR_NO"))));
							if(cntrNoChk.get(chkUpdateCntrNo) == null){
								cntrNoChk.put(chkUpdateCntrNo, true);
							}else{	/** A => C, B => C 와 같이 변경 */
								abnormalSaveResult.setResultCode("A11297");	/** 이미 사용된 컨테이너번호입니다. */
								return abnormalSaveResult;
							}
							updateCntrNoCheckList.add(chkUpdateCntrNo);
						}
						
						tmpMap.put("odJobNo", chkOdJobNo);
						tmpMap.put("updateCntrNoList", updateCntrNoCheckList);
						cntrNoChk = bmsCargoAbnormalMapper.selectAbnormalCntrInfo(tmpMap);
						if(cntrNoChk != null){	/** 해당 하우스에 이미 등록되어있는 컨테이너번호로 변경 */
							abnormalSaveResult.setResultCode("A11297");	/** 이미 사용된 컨테이너번호입니다. */
							return abnormalSaveResult;
						}
					}
				}
			}
		}else if("06".equals(modType) && containerList != null) {
			List<String> insertCntrNoList = new ArrayList<String>();
			for(Map<String, Object> containerMap : containerList){
				insertCntrNoList.add(Util.null2str(containerMap.get("cntrNo")));
			}
			
			tmpMap.put("action", list.getAction());
			tmpMap.put("masterOdJobNo", tmpAbnormal.getOdJobNo());
			tmpMap.put("modType", modType);
			tmpMap.put("abnormalList", list.getAbnormalList());
			tmpMap.put("insertCntrNoList", insertCntrNoList);
			tmpMap = bmsCargoAbnormalMapper.selectAbnormalCntrInfo(tmpMap);
			if(tmpMap != null){
				if(Util.null2int(tmpMap.get("CNTR_CNT")) > 0){
					abnormalSaveResult.setResultCode("A11297");	/** 이미 사용된 컨테이너번호입니다. */
					return abnormalSaveResult;
				}
			}else{
				tmpMap = new HashMap<String, Object>();
			}
		}
		
		/** MRN, MBL_NO 기준으로 순번처리함 */
		tmpMap.clear();
		tmpMap.put("entCode", list.getEntCode());
		tmpMap.put("mrn", tmpAbnormal.getMrn());
		tmpMap.put("mblNo", tmpAbnormal.getMblNo());
		tmpMap.put("solution", "ECM");
		/** 마스터선택 저장시 기존 전송하지않은 데이터가 있어도 신규순번 생성 */
		if("house".equals(list.getAction()) || "modHouse".equals(list.getAction()) || !Util.isEmpty(list.getModJobNo())){
			tmpMap.put("modType", modType);
		}
		tmpMap = manifestMapper.selectLastMblInfo(tmpMap);
		if(tmpMap != null && tmpMap.size() > 0){
			modJobNo = Util.null2str(tmpMap.get("MOD_JOB_NO_TYPE"));
			hblCnt = Util.null2int(tmpMap.get("HBL_CNT_TYPE"));
			if(!Util.null2str(tmpMap.get("MOD_SERIAL_NO")).equals("")){
				modSerialNo = Util.lpad(Util.null2str(Util.null2int(tmpMap.get("MOD_SERIAL_NO")) + 1), 4, '0');
			}else{
				modSerialNo = "0001";
			}
		}else{
			modSerialNo = "0001";
			tmpMap = new HashMap<String, Object>();
		}
		
		if(!modJobNo.equals("")){
			//적하정정 존재여부 추출
			list.setModJobNo(modJobNo);
			int modCnt = 0;
			if(!"06".equals(modType) && containerList != null){
				if(!"U".equals(cntrModType) && !"A".equals(cntrModType)) {
					list.setCntrNo(cntrNo);
				}
				modCnt = (bmsCargoAbnormalMapper.getIsModContainerData(list)).intValue();
			}else{
				modCnt = (bmsCargoAbnormalMapper.getIsModData(list)).intValue();
			}
			if(modCnt > 0) {
				abnormalSaveResult.setResultCode("A11260");	/** 적하정정건 이미 존재합니다. */
				return abnormalSaveResult;
			}
		}
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			BmsCargoAbnormalMapper abnormalBatchMapper = sqlSession.getMapper(BmsCargoAbnormalMapper.class);
			
			tmpMap.clear();
			tmpMap.put("cryptographyYn", list.getCryptographyYn());
			if("master".equals(list.getAction())){
				tmpMap.put("masterOdJobNo", tmpAbnormal.getOdJobNo());
				tmpMap.put("action", "master");
				tmpMap.put("modType", modType);
				if("99".equals(modType)){
					tmpMap.put("cntrModType", cntrModType);
					if("D".equals(cntrModType)){
						tmpMap.put("cntrNo", cntrNo);
					}else if("U".equals(cntrModType)){
						tmpMap.put("containerList", containerList);
					}
				}
				abnormalList = bmsCargoAbnormalMapper.selectAbnormalHouseList(tmpMap);
				if(abnormalList.size() == 0) {
					return abnormalSaveResult;
				}
				int listSize;
				if("06".equals(modType)){
					listSize = 200;
				}else{
					listSize = 500;
				}
				int houseCnt = abnormalList.size();
				List<Abnormal> tmpAbnormalList;
				for(int i = 0; i < houseCnt; i += listSize){
					if(i + listSize < houseCnt) {
						tmpAbnormalList = abnormalList.subList(i, i + listSize);
					}else{
						tmpAbnormalList = abnormalList.subList(i, houseCnt);
					}
					if(i != 0){
						modSerialNo = Util.lpad(Util.null2str(Util.null2int(modSerialNo) + 1), 4, '0');
					}
					if(tmpAbnormalList.size() > 0){
						modJobNo = this.insertModList(tmpAbnormalList, list, tmpAbnormal, singleModCodeList, containerList, abnormalBatchMapper, containerNoMap, updateCntrNoChk2, b2gForwarderSign, "", modSerialNo, modType, cntrModType, hblCnt, insertCnt);
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}else{
				if( ("99".equals(modType) && !"A".equals(cntrModType)) || "05".equals(modType) || ("06".equals(modType) && containerList == null) ){
					tmpMap.put("modType", modType);
					tmpMap.put("abnormalList", abnormalList);
					tmpMap.put("containerList", containerList);
					if("99".equals(modType)){
						tmpMap.put("cntrModType", cntrModType);
						tmpMap.put("cntrNo", cntrNo);
						
					}
					abnormalList = bmsCargoAbnormalMapper.selectAbnormalHouseList(tmpMap);
				}
				if(abnormalList.size() > 0) {
					modJobNo = this.insertModList(abnormalList, list, tmpAbnormal, singleModCodeList, containerList, abnormalBatchMapper, containerNoMap, updateCntrNoChk2, b2gForwarderSign, modJobNo, modSerialNo, modType, cntrModType, hblCnt, insertCnt);
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}else{
					return abnormalSaveResult;
				}
			}
		
			sqlSession.commit();
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		abnormalSaveResult.setEdiJobNo(modJobNo);
		return abnormalSaveResult;
	}
	public String insertModList(List<Abnormal> abnormalList, AbnormalList list, Abnormal tmpAbnormal, List<String> singleModCodeList, List<Map<String, Object>> containerList, BmsCargoAbnormalMapper abnormalBatchMapper, 
			Map<String, List<Map<String, Object>>> containerNoMap, Map<String, Object> updateCntrNoChk2, String b2gForwarderSign, String modJobNo,
			String modSerialNo, String modType, String cntrModType, int hblCnt, int insertCnt) throws Exception{
		
		Map<String, List<Map<String, Object>>> containerUpdateMap = null;
		if(containerNoMap != null) {
			containerUpdateMap = new HashMap<String, List<Map<String, Object>>>();
			for(String cntrNoChk : containerNoMap.keySet()){
				containerUpdateMap.put(cntrNoChk, new ArrayList<Map<String, Object>>());
			}
		}
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> map = null;
		Map<String, Object> tmpContainerMap = new HashMap<String, Object>();
		Map<String, Object> tmpCntrMap = new HashMap<String, Object>();
		List<Map<String, Object>> tmpContainerList = null;
		String[] cntrNoArr;
		boolean isNew;
		
		Util.accessControlDatabase(list, "gmtTime", false, null, null, null, null);
		
		/** 항공 - 추가  일경우 하기장소/하기물품코드 업데이트 */
		if("AIR".equals(tmpAbnormal.getBound()) && modType.equals("06")){
			tmpMap.put("discWhCode", list.getDiscWhCode());
			tmpMap.put("discItemCode", list.getDiscItemCode());
			for(Abnormal ab : abnormalList){
				tmpMap.put("odJobNo",ab.getOdJobNo());
				abnormalBatchMapper.updateDiscWhCode(tmpMap);
			}
			for(Abnormal ab : abnormalList){
				tmpMap.put("odJobNo",ab.getOdJobNo());
				abnormalBatchMapper.updateDiscItemCode(tmpMap);
			}
		}
		
		tmpMap.clear();
		tmpMap.put("gmtTime", list.getGmtTime());
		if(modJobNo.equals("")){
			/** 관리번호 */
			hblCnt += abnormalList.size() * insertCnt;
			tmpMap.put("hblCnt", hblCnt);
			tmpMap.put("entCode", list.getEntCode());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("registerUserId", list.getRegisterUserId());
			tmpMap.put("updateUserId", list.getRegisterUserId());
			tmpMap.put("solution", "ECM");
			tmpMap.put("stockType", "MOD");
			tmpMap.put("stockSign", "MOD");
			tmpMap.put("stockSeqLength", 7);
			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", "MOD-JOB NO");
			modJobNo = commonMapper.selectNextStockMaxNo(tmpMap);
			tmpMap.put("resultCode", "FIXED_SIGN_DATE_SEQ_NO");
			tmpMap.put("stockDateStr", b2gForwarderSign);
			tmpMap.put("stockSign", tmpAbnormal.getMrn());
			tmpMap.put("stockSeqLength", 4);
			/** 정정신청번호 끝 네자리는 정정번호(modSerialNo) - 2024.06.03*/
//			tmpMap.put("modReqNo", commonMapper.selectNextStockMaxNo(tmpMap));
			tmpMap.put("modReqNo", tmpAbnormal.getMrn() + b2gForwarderSign + modSerialNo);
			tmpMap.put("modJobNo", modJobNo);
			tmpMap.put("modSerialNo", modSerialNo);
			tmpMap.put("mblNo", tmpAbnormal.getMblNo());
			tmpMap.put("modType", modType);
			bmsCargoAbnormalMapper.insertMblCorrectByAb(tmpMap);
			isNew = true;
		}else{
			tmpMap.put("modJobNo", modJobNo);
			isNew = false;
		}
		
		Map<String, Object> seqIdNoMap;
		int seqIdNo = 0, hblSeqNoId = 0, cntrSeqNoId = 0;
		tmpAbnormal.setModJobNo(modJobNo);
		if(singleModCodeList != null){
			if(isNew){
				seqIdNo = 1;
			}else{
				seqIdNoMap = bmsCargoAbnormalMapper.getSeqNoIdByAb(tmpAbnormal);
				seqIdNo = Util.null2int(seqIdNoMap.get("SEQ_NO_ID"));
			}
			
			for(String singleModCode : singleModCodeList){
				for(int i=0;i<abnormalList.size();i++) {
					Abnormal ab = abnormalList.get(i);
					ab.setRegisterUserId(list.getRegisterUserId());
					ab.setGmtTime(list.getGmtTime());
					ab.setEntCode(list.getEntCode());
					ab.setEntBusinessCode(list.getEntBusinessCode());
					ab.setModJobNo(modJobNo);
					ab.setSeqNoId(seqIdNo++);
					ab.setModType(modType);
					ab.setModReason(list.getModReason());
					ab.setSingleModCode(singleModCode);
					ab.setSingleModReasonCode(list.getSingleModReasonCode());
					if("05".equals(modType)){
						this.setOrderData(ab);
					}
					
					abnormalBatchMapper.insertHblCorrectByAb(ab);			
				}
			}
		}else{
			if(containerList != null){
				List<Abnormal> insertHblCorrectByAbList = new ArrayList<Abnormal>();
				List<Map<String, Object>> insertContainerCorrectByAbList = new ArrayList<Map<String, Object>>();
				tmpAbnormal.setModType(modType);
				
				for(int i=0;i<abnormalList.size();i++) {
					Abnormal ab = abnormalList.get(i);
					
					if(isNew){
						if(i == 0){
							seqIdNo = 1;
							hblSeqNoId = 0;
						}
						cntrSeqNoId = 1;
					}else{
						tmpAbnormal.setOdJobNo(ab.getOdJobNo());
						seqIdNoMap = bmsCargoAbnormalMapper.getSeqNoIdByAb(tmpAbnormal);
						hblSeqNoId = Util.null2int(seqIdNoMap.get("HBL_SEQ_NO_ID"));
						cntrSeqNoId = Util.null2int(seqIdNoMap.get("CNTR_SEQ_NO_ID"));
						if(i == 0){
							seqIdNo = Util.null2int(seqIdNoMap.get("SEQ_NO_ID"));
						}
					}
					
					ab.setModJobNo(modJobNo);
					if(hblSeqNoId == 0){
						ab.setRegisterUserId(list.getRegisterUserId());
						ab.setGmtTime(list.getGmtTime());
						ab.setEntCode(list.getEntCode());
						ab.setEntBusinessCode(list.getEntBusinessCode());
						ab.setSeqNoId(seqIdNo++);
						ab.setModType(modType);
						ab.setModReason(list.getModReason());
						hblCnt++;
						insertHblCorrectByAbList.add(ab);
					}else{
						ab.setSeqNoId(hblSeqNoId);
					}
					
					map = new HashMap<String, Object>();
					for(Map<String, Object> cntrMap : containerList){
						cntrMap.put("modJobNo", modJobNo);
						cntrMap.put("seqNoId", ab.getSeqNoId());
						cntrMap.put("cntrModType", list.getCntrModType());
						cntrMap.put("odJobNo", ab.getOdJobNo());
						map.putAll(cntrMap);
					}
					
					if("06".equals(list.getModType())) {
						for(Map<String, Object> cntrMap : containerList) {
							tmpCntrMap = new HashMap<String, Object>();
							tmpCntrMap.put("modJobNo", modJobNo);
							tmpCntrMap.put("seqNoId", ab.getSeqNoId());
							tmpCntrMap.put("cntrModType", list.getCntrModType());
							tmpCntrMap.put("odJobNo", ab.getOdJobNo());
							tmpCntrMap.put("cntrNo", cntrMap.get("cntrNo"));
							tmpCntrMap.put("cntrCode", cntrMap.get("cntrCode"));
							tmpCntrMap.put("cntrEdiCode", cntrMap.get("cntrEdiCode"));
							tmpCntrMap.put("cntrSealNo1", cntrMap.get("cntrSealNo1"));
							tmpCntrMap.put("cntrSealNo2", cntrMap.get("cntrSealNo2"));
							tmpCntrMap.put("cntrSealNo3", cntrMap.get("cntrSealNo3"));
							tmpCntrMap.put("pieces", cntrMap.get("pieces"));
							tmpCntrMap.put("unitPieces", cntrMap.get("unitPieces"));
							insertContainerCorrectByAbList.add(tmpCntrMap);
						}
					}else if("U".equals(list.getCntrModType())){
						cntrNoArr = Util.null2str(ab.getCntrNoList()).split("\\|");
						for(String cntrNo : cntrNoArr){
							tmpContainerList = new ArrayList<Map<String, Object>>();
							for(Map<String, Object> cntrMap : (List<Map<String, Object>>)containerNoMap.get(cntrNo)){
								tmpContainerMap = new HashMap<String, Object>();
								tmpContainerMap.put("cntrModCode", cntrMap.get("cntrModCode"));
								tmpContainerMap.put("beforeValue", cntrMap.get("beforeValue"));
								tmpContainerMap.put("afterValue", cntrMap.get("afterValue"));
								tmpContainerMap.put("cntrSeqNoId", cntrSeqNoId++);
								tmpContainerMap.put("cntrCode", cntrMap.get("cntrCode"));
								tmpContainerList.add(tmpContainerMap);
							}
							tmpCntrMap = new HashMap<String, Object>();
							tmpCntrMap.put("modJobNo", modJobNo);
							tmpCntrMap.put("seqNoId", ab.getSeqNoId());
							tmpCntrMap.put("cntrModType", list.getCntrModType());
							tmpCntrMap.put("odJobNo", ab.getOdJobNo());
							tmpCntrMap.put("cntrNo", cntrNo);
							tmpCntrMap.put("cntrModList", tmpContainerList);
							containerUpdateMap.get(cntrNo).add(tmpCntrMap);
						}
					}else {
						for(Map<String, Object> cntrMap : containerList) {
							tmpContainerList = new ArrayList<Map<String, Object>>();
							tmpContainerMap = new HashMap<String, Object>();
							tmpContainerMap.put("cntrModCode", cntrMap.get("cntrModCode"));
							tmpContainerMap.put("cntrSeqNoId", cntrSeqNoId++);
							tmpContainerList.add(tmpContainerMap);
							
							tmpCntrMap = new HashMap<String, Object>();
							tmpCntrMap.put("modJobNo", modJobNo);
							tmpCntrMap.put("seqNoId", ab.getSeqNoId());
							tmpCntrMap.put("cntrModType", list.getCntrModType());
							tmpCntrMap.put("odJobNo", ab.getOdJobNo());
							tmpCntrMap.put("cntrNo", cntrMap.get("cntrNo"));
							tmpCntrMap.put("cntrCode", cntrMap.get("cntrCode"));
							tmpCntrMap.put("cntrEdiCode", cntrMap.get("cntrEdiCode"));
							tmpCntrMap.put("cntrSealNo1", cntrMap.get("cntrSealNo1"));
							tmpCntrMap.put("cntrSealNo2", cntrMap.get("cntrSealNo2"));
							tmpCntrMap.put("cntrSealNo3", cntrMap.get("cntrSealNo3"));
							tmpCntrMap.put("pieces", cntrMap.get("pieces"));
							tmpCntrMap.put("unitPieces", cntrMap.get("unitPieces"));
							tmpCntrMap.put("cntrModList", tmpContainerList);
							insertContainerCorrectByAbList.add(tmpCntrMap);
						}
					}
				}
				
				for(int i=0;i<insertHblCorrectByAbList.size();i++){
					Abnormal ab = insertHblCorrectByAbList.get(i);
					abnormalBatchMapper.insertHblCorrectByAb(ab);
				}
				
				if("06".equals(list.getModType())) {
					for(Map<String, Object> cntrMap : insertContainerCorrectByAbList){
						abnormalBatchMapper.insertOrderContainer(cntrMap);
					}
				}else if("U".equals(list.getCntrModType())){
					for(String cntrNo : containerUpdateMap.keySet()){
						for(Map<String, Object> cntrMap : containerUpdateMap.get(cntrNo)){
							abnormalBatchMapper.updateOrderContainer(cntrMap);
						}
					}
					for(String cntrNo : containerUpdateMap.keySet()){
						for(Map<String, Object> cntrMap : containerUpdateMap.get(cntrNo)){
							abnormalBatchMapper.insertContainerCorrectByAb(cntrMap);
						}
					}
					tmpMap.clear();
					tmpMap.put("modJobNo", modJobNo);
					for(String cntrNo : containerUpdateMap.keySet()){
						if(updateCntrNoChk2.get(cntrNo) != null){
							for(Map<String, Object> cntrMap : containerUpdateMap.get(cntrNo)){
								tmpMap.put("seqNoId", cntrMap.get("seqNoId"));
								tmpMap.put("beforeCntrNo", cntrNo);
								tmpMap.put("afterCntrNo", updateCntrNoChk2.get(cntrNo));
								abnormalBatchMapper.updateModContainerNo(tmpMap);
							}
						}
					}
				}else{
					if("A".equals(list.getCntrModType())){
						for(Map<String, Object> cntrMap : insertContainerCorrectByAbList){
							abnormalBatchMapper.insertOrderContainer(cntrMap);
						}
					}else{
						for(Map<String, Object> cntrMap : insertContainerCorrectByAbList){
							abnormalBatchMapper.deleteOrderContainer(cntrMap);
						}
					}
					
					for(Map<String, Object> cntrMap : insertContainerCorrectByAbList){
						abnormalBatchMapper.insertContainerCorrectByAb(cntrMap);
					}
				}
				
			}else{
				if(isNew){
					seqIdNo = 1;
				}else{
					seqIdNoMap = bmsCargoAbnormalMapper.getSeqNoIdByAb(tmpAbnormal);
					seqIdNo = Util.null2int(seqIdNoMap.get("SEQ_NO_ID"));
				}
				for(int i=0;i<abnormalList.size();i++) {
					Abnormal ab = abnormalList.get(i);
					ab.setRegisterUserId(list.getRegisterUserId());
					ab.setGmtTime(list.getGmtTime());
					ab.setEntCode(list.getEntCode());
					ab.setEntBusinessCode(list.getEntBusinessCode());
					ab.setModJobNo(modJobNo);
					ab.setSeqNoId(seqIdNo++);
					ab.setModType(modType);
					ab.setModReason(list.getModReason());
					ab.setSingleModCode(list.getSingleModCode());
					ab.setSingleModReasonCode(list.getSingleModReasonCode());
					abnormalBatchMapper.insertHblCorrectByAb(ab);
				}
			}
		}
		
		if(!isNew){
			if(containerList == null){
				hblCnt += abnormalList.size() * insertCnt;
			}
			tmpMap.clear();
			tmpMap.put("gmtTime", list.getGmtTime());
			tmpMap.put("modJobNo", modJobNo);
			tmpMap.put("hblCnt", hblCnt);
			bmsCargoAbnormalMapper.updateMblCorrectByAb(tmpMap);
		}
		
		return modJobNo;
	}
	
	public void setOrderData(Abnormal ab) throws Exception{
		String beforeValue = "";
		switch(ab.getSingleModCode()) {
			case "AA" :
				beforeValue = ab.getVesselFlight();
				break;
			case "AB" :
				beforeValue = ab.getVoyage();
				break;
			case "AE" :
				beforeValue = ab.getDestCode();
				break;
			case "AI" :
				beforeValue = ab.getArDate();
				break;
			case "BC" :
				beforeValue = ab.getShipperName();
				break;
			case "BC1" :
				beforeValue = ab.getShipperAddress();
				break;
			case "BC2" :
				beforeValue = ab.getShipperTel();
				break;
			case "BD" :
				beforeValue = ab.getConsigneeName();
				break;
			case "BD1" :
				beforeValue = ab.getConsigneeAddress();
				break;
			case "BD2" :
				beforeValue = ab.getConsigneeTel();
				break;
			case "BD5" :
				beforeValue = ab.getConsigneeBusinessId();
				break;
			case "BF" :
				beforeValue = ab.getCommodityItem();
				break;
			case "BG" :
				beforeValue = ab.getUnitPieces();
				break;
			case "DB" :
				beforeValue = Util.null2str(ab.getGwt());
				break;
			case "DK" :
				beforeValue = ab.getHblNo();
				break;
		}
		ab.setBeforeValue(beforeValue);
	}
	
		
	/**
	 * 엑셀업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public AbnormalSaveResult excelUploadCheck(MultipartFile uploadExcel, String entCode) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		AbnormalSaveResult abnormalSaveResult = new AbnormalSaveResult();
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
			abnormalSaveResult.setResultCode("extError");
			return abnormalSaveResult;
		}
		
		try {
			
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
				fieldName =		new String[]{"AR_DATE","VESSEL_FLIGHT","DEST_CODE","MRN","MSN","MBL_NO","HBL_NO","HSN","H_PIECES","H_GWT","PIECES","GWT","ABNORMAL_CODE","CNT","DISC_WH_CODE"};	
				fieldTitle =	new String[]{"입항일자","편명","도착항","MRN","MSN","MBLNO","HBLNo","HSN","적하목록 PCS","적하목록 WT","하기이상 PCS","하기이상 WT","이상 코드","사고 개수","배정창고"};
				fieldLength =	new String[]{"10","50","5","20","4","20","20","4","4","9.3","4","9.3","3","4","10"};
				fieldType =		new String[]{"DATE","S","S","S","S","S","S","S","DBL","DBL","N","DBL","S","N","S"};
				
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
						for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
							if(sheet.getRow(i) == null){
								abnormalSaveResult.setResultCode("A11216");
								resultData.put("nullLine", i + 1);
								resultData.put("lastLine", sheet.getLastRowNum() + 1);
								abnormalSaveResult.setResultData(resultData);
								return abnormalSaveResult;
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
								
								dataMap.put(fieldName[j], fieldValue);
							}
							dataList.add(dataMap);
						}
					}else{
						HSSFCell myCell = null;
						for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
							if(sheet.getRow(i) == null){
								abnormalSaveResult.setResultCode("A11216");
								resultData.put("nullLine", i + 1);
								resultData.put("lastLine", sheet.getLastRowNum() + 1);
								abnormalSaveResult.setResultData(resultData);
								return abnormalSaveResult;
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
		
								dataMap.put(fieldName[j], fieldValue);
							}
							dataList.add(dataMap);
						}
					}
					
					if(errMsg.length() != 0){
						abnormalSaveResult.setResultCode("dataErr");
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("errStr", errMsg);
						abnormalSaveResult.setResultData(errMap);
						return abnormalSaveResult;
					}
					//TODO 파일삭제?
					
					for(Map<String, Object> data : dataList){
						if(Util.null2str(data.get("AR_DATE")).equals("") || (!"PANTK".equals(entCode) && Util.null2str(data.get("DEST_CODE")).equals("")) || Util.null2str(data.get("MRN")).equals("") || Util.null2str(data.get("MSN")).equals("")
							|| Util.null2str(data.get("MBL_NO")).equals("") || Util.null2str(data.get("HBL_NO")).equals("") || Util.null2str(data.get("HSN")).equals("") || Util.null2str(data.get("ABNORMAL_CODE")).equals("")
							|| Util.null2str(data.get("DISC_WH_CODE")).equals("")){
							abnormalSaveResult.setResultCode("dataNull");
							return abnormalSaveResult;
						}
						if(Util.null2str(data.get("ABNORMAL_CODE")).equals("MGN")){
							if(Util.null2dbl(data.get("PIECES")) != 0 || Util.null2dbl(data.get("GWT")) != 0){
								abnormalSaveResult.setResultCode("A11307");	/** MGN은 하기이상PCS, 하기이상WT를 입력할 수 없습니다. */
								return abnormalSaveResult;
							}
						}else{
							if(Util.null2dbl(data.get("PIECES")) == 0 || Util.null2dbl(data.get("GWT")) == 0){
								abnormalSaveResult.setResultCode("A11308");	/** MGN이 아닐경우 하기이상PCS, 하기이상WT를 입력바랍니다. */
								return abnormalSaveResult;
							}
						}
					}
					
					resultData.put("dataList", dataList);
	//				resultData.put("entCode", entCode);
	//				
	//				//hbl 유효성 체크
	//				Map<String, Object> rstHbl = bmsCargoAbnormalMapper.selectHblValidate(resultData);
	//				
	//				resultData.put("dataSize", dataList.size());
	//				
	//				if(rstHbl!=null) {
	//					//유효하지않은 BL
	//					resultData.put("NOT_BL_CNT", rstHbl.get("NOT_BL_CNT"));
	//					resultData.put("NOT_HBL_LIST", rstHbl.get("NOT_HBL_LIST"));
	//				}
	//				else {
	//					resultData.put("NOT_BL_CNT", "0");
	//					resultData.put("NOT_HBL_LIST", "");				
	//				}
					
					abnormalSaveResult.setResultData(resultData);
					abnormalSaveResult.setResultCode("200");
					
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
				abnormalSaveResult.setResultCode("ERROR");
			}
		
		}catch(Exception e) {
			abnormalSaveResult.setResultCode("A00063");
			log.error("exception Message :: " + e.getMessage());
		}finally{
		}
		
		return abnormalSaveResult;
	}
	
	/**
	 * 하기이상결과보고 Excel 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public AbnormalSaveResult excelUpload(AbnormalList abnormalList) throws Exception {
		AbnormalSaveResult abnormalSaveResult = new AbnormalSaveResult();
		Util.accessControlDatabase(abnormalList, "gmtTime", false, null, null, null, null);
		List<Map<String, Object>> dataList = abnormalList.getDataList();	
		List<Map<String, Object>> mrnList = abnormalList.getMrnList();	

		if(dataList == null) return abnormalSaveResult;
		
		Map<String, Object> chkMap;
		StringBuffer chkErr = new StringBuffer();
		String notNoCargoYn;
		String hblNo;
		String action = Util.null2str(abnormalList.getAction());
		String maxAbnormalBlCd = "";
		boolean isNoCargo = "Y".equals(Util.null2str(mrnList.get(0).get("noCargoYn")).equals("")?Util.null2str(dataList.get(0).get("noCargoYn")):Util.null2str(mrnList.get(0).get("noCargoYn")));
		boolean mfnAdd = false;		// MFN edit건인지 확인 2026.04.27 2sowell
		boolean mgnEdit = false;	// 기존에 MGN 데이터가 있는지 확인 2026.04.27 2sowell
		if( action.equals("abnormalAdd") || action.equals("abnormalUpdate") ) {
			if( StringUtils.isBlank(Util.null2str(mrnList.get(0).get("mrn"))) || StringUtils.isBlank(Util.null2str(mrnList.get(0).get("vesselFlight"))) || StringUtils.isBlank(Util.null2str(mrnList.get(0).get("arDate"))) ) {
				abnormalSaveResult.setResultCode("dataNull");
				return abnormalSaveResult;
			}

			for(Map<String, Object> data : dataList) {
				if(Util.null2str(data.get("arDate")).equals("") || Util.null2str(data.get("mrn")).equals("") || Util.null2str(data.get("abnormalCode")).equals("")
						|| Util.null2str(data.get("mblNo")).equals("") || Util.null2str(data.get("hblNo")).equals("") ) {
					abnormalSaveResult.setResultCode("dataNull");
					return abnormalSaveResult;
				}
				if(Util.null2str(data.get("abnormalCode")).equals("MGN")) {
					if(Util.null2dbl(data.get("pieces")) != 0 || Util.null2dbl(data.get("gwt")) != 0){
						abnormalSaveResult.setResultCode("A11307");	/** MGN은 하기이상PCS, 하기이상WT를 입력할 수 없습니다. */
						return abnormalSaveResult;
					}
				} else {
					if(Util.null2dbl(data.get("pieces")) == 0 || Util.null2dbl(data.get("gwt")) == 0) {
						abnormalSaveResult.setResultCode("A11308");	/** MGN이 아닐경우 하기이상PCS, 하기이상WT를 입력바랍니다. */
						return abnormalSaveResult;
					}
				}
				
				// 새로추가된 데이터가 MFN인 경우 2026.04.27 2sowell
				if("".equals(Util.null2str(data.get("lastPodStatus"))) && "MFN".equals(Util.null2str(data.get("abnormalCode")))) {
					mfnAdd = true;
				}
				// 기존에 있던 데이터가 MGN인지 확인
				if(!"".equals(Util.null2str(data.get("lastPodStatus"))) && "MGN".equals(Util.null2str(data.get("abnormalCode")))) {
					mgnEdit = true;
				}
			}
		}

		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			BmsCargoAbnormalMapper bmsCargoAbnormalMapperBatch = sqlSession.getMapper(BmsCargoAbnormalMapper.class);

			int cnt = 1;
			int remarkCnt = 1;
			for(Map<String, Object> data : dataList) {
				hblNo = Util.null2str(data.get("hblNo"));
				data.put("action", action);
				data.put("entCode", abnormalList.getEntCode());
				data.put("seqId", bmsCargoAbnormalMapper.selectBmsSeqNoId(data));

				if( "Y".equals(Util.null2str(abnormalList.getNoCargoMfnYn())) ) {
					data.put("remarks", (bmsCargoAbnormalMapper.selectMaxRemarksNo(data) + remarkCnt));
					remarkCnt++;
				}

				if(!isNoCargo) {
					chkMap = bmsCargoAbnormalMapper.selectBmsInPlanAbnormalChk(data);
					notNoCargoYn = bmsCargoAbnormalMapper.selectNoCargoYn(data);

					if(chkMap != null && chkMap.size() > 0) {
//						if(!Util.null2str(chkMap.get("ABNORMAL_CODE_DUP")).equals("")){
//							chkErr.append("MAWB No : " + Util.null2str(data.get("mblNo")) + ", HAWB No : " + hblNo + " 하기이상코드가 이미 등록되어 있습니다.<br />");
//						}

						if(Util.null2str(chkMap.get("HBL_NO")).equals("") && Util.null2str(notNoCargoYn).equals("")){
							chkErr.append("HAWB No " + hblNo + " 데이터가 존재하지 않습니다.<br />");
						}

						data.put("odJobNo", chkMap.get("OD_JOB_NO"));
						data.put("msn", chkMap.get("MSN"));
						data.put("hsn", chkMap.get("HSN"));
						data.put("origCountry", chkMap.get("ORIG_COUNTRY"));
						data.put("hblPieces", chkMap.get("EX_CT"));
						data.put("hblGwt", chkMap.get("EX_WT"));
					}else if( Util.null2str(notNoCargoYn).equals("") ) {
						if("PANTK".equals(abnormalList.getEntCode())){
							chkErr.append("HAWB No " + Util.null2str(data.get("hblNo")) + " 데이터가 존재하지 않습니다.<br />");
						}else{
							chkErr.append("MAWB No " + Util.null2str(data.get("mblNo")) + " 데이터가 존재하지 않습니다.<br />");
						}
					}
					if(chkErr.length() > 1000){
						break;
					}
				} else {
					if( Util.null2str(data.get("hblNo")).length() > 20 ) {
						chkErr.append("HAWB No " + Util.null2str(data.get("hblNo")) + " 데이터가 올바르지 않습니다.<br />");
					}
					maxAbnormalBlCd = bmsCargoAbnormalMapper.selectMaxAbnormalBlCnt(data);
					data.put("odJobNo", data.get("hblNo") + String.valueOf(data.get("arDate")).substring(2, 4) + maxAbnormalBlCd);
				}
			}

			if(chkErr.length() > 0) {
				chkMap = new HashMap<String, Object>();
				chkMap.put("errorStr", chkErr.toString());
				abnormalSaveResult.setResultData(chkMap);
				abnormalSaveResult.setResultCode("abnormalChkError");
				return abnormalSaveResult;
			}
			cnt = 1;

			for(int i=0;i<dataList.size();i++) {
				Map<String, Object> ab = dataList.get(i);
				ab.put("registerUserId", abnormalList.getRegisterUserId());
				ab.put("gmtTime", abnormalList.getGmtTime());
				
				// MFN 추가이고, 기존 데이터에 MGN건이 있으며 삭제한것이 없으면 기존 데이터 상태 업데이트 안함 2026.04.27 2sowell
				if(mfnAdd && mgnEdit && abnormalList.getEditDelList().size() == 0) {
					ab.put("updateStatus", "N");
				}else {
					ab.put("updateStatus", "Y");
				}

				bmsCargoAbnormalMapperBatch.insertBmsInPlanExcelAbnormal(ab);
				if((cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}

			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();

			if(abnormalList.getEditDelList().size() > 0) bmsCargoAbnormalMapper.deleteAbnormalList(abnormalList);
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		/* 정상인 경우에는 select 하지 않는다..2022-02-24.hyuk
		abnormalSaveResult.setOdList(bmsCargoAbnormalMapper.selectOdList(abnormalList));
		*/
		
		return abnormalSaveResult;
	}
	
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row) throws Exception {
		//TODO 엑셀 셀타입 텍스트로 고정시킬수있는지?  없다면 추가개발필요
		if(type.equals("N")){
			if (Util.null2str(data).equals("")){
				data = "0";
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
				data = "0";
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
			
		}else if("S".equals(type)){
			if(Util.null2str(data).equals("")){
				data = null;
			}else{
				if(data.getBytes("euc-kr").length > Util.null2int(length)) {
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
	 * 하기이상결과보고 적하 삭제 여부 Update
	 * @param abnormalList
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public AbnormalSaveResult deleteManifest(AbnormalList abnormalList, HttpServletRequest request) throws Exception {
		AbnormalSaveResult abnormalSaveResult = new AbnormalSaveResult();
		StringBuffer chkErr = new StringBuffer();

		log.debug("[deleteManifest] deleteManifestList Size : {}", abnormalList.getDataList().size());
		for(Map<String, Object> data : abnormalList.getDataList()) {
			log.debug("deleteManifest OD_JOB_NO : {}", data.get("odJobNo"));
			if( "N".equals(Util.null2str(data.get("manifestDelYn"))) ) {
				bmsCargoAbnormalMapper.updateManifestDelYn(String.valueOf(data.get("odJobNo")));
			} else {
				chkErr.append("MAWB No : " + Util.null2str(data.get("mblNo")) + ", HAWB No : " + Util.null2str(data.get("hblNo")) + " 적하 삭제가 이미 등록되어 있습니다.<br />");
			}
		}

		if(chkErr.length() > 0) {
			Map<String, Object> chkMap = new HashMap<String, Object>();
			chkMap.put("errorStr", chkErr.toString());
			abnormalSaveResult.setResultData(chkMap);
			abnormalSaveResult.setResultCode("duplicateDelError");
			return abnormalSaveResult;
		}

		return abnormalSaveResult;
	}

	/**
	 * 하기결과이상보고
	 * @param sendDelivery
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult sendAbnormalitiesReport(SendDelivery sendDelivery, HttpServletRequest request) throws java.lang.Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> docSend = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();
		StringBuffer processMessage = new StringBuffer();
		
		docSend.put("entCode", sendDelivery.getEntCode());
		docSend.put("entBusinessCode", sendDelivery.getEntBusinessCode());
		docSend.put("solution", sendDelivery.getSolution());
		docSend.put("bound", sendDelivery.getBound());
		docSend.put("boundType", sendDelivery.getBoundType());
		docSend.put("userLanguage", sendDelivery.getUserLanguage());
		docSend.put("gmtTime", sendDelivery.getGmtTime());
		docSend.put("docType", "MANIFEST-ABNORMAL-PANTOS");
		docSend.put("registerUserId", sendDelivery.getRegisterUserId());
		docSend.put("registerUserName", sendDelivery.getRegisterUserName());
		docSend.put("srchEntCode", sendDelivery.getEntCode());
		docSend.put("srchEntBusinessCode", sendDelivery.getEntBusinessCode());
		docSend.put("srchSolution", sendDelivery.getSolution());
		docSend.put("srchBound", sendDelivery.getBound());
		docSend.put("srchBoundType", sendDelivery.getBoundType());
		docSend.put("ediList", sendDelivery.getEdiList());
		
		List<Map<String, Object>> tempDataList = bmsCargoAbnormalMapper.selectSendBmsInPlanAbnormalities(sendDelivery);

		List<String> errList = new ArrayList<String>();
		if(tempDataList != null && tempDataList.size() > 0) {
			docSend.put("ediVender", tempDataList.get(0).get("EDI_VENDER"));
			
			Map<String, Object> fwSendAccInfo = null;
			String ifServiceCode = null;
			
			processMessage.append("<li>M.B/L No.");
			// M.B/L 오더정보 조회
			
			// 서비스 코드 세팅
			ifServiceCode = ediCommonService.getServiceCode(Util.null2str(docSend.get("solution")), Util.null2str(docSend.get("bound")), Util.null2str(docSend.get("boundType")), Util.null2str(docSend.get("docType")), Util.null2str(docSend.get("ediVender")));
			docSend.put("ifServiceCode", ifServiceCode);
			
			// 사업장정보 가져오기( 포워더 송/수신 계정정보, TB_SMS_ENT_BUS_FMS )
			fwSendAccInfo = manifestMapper.getFwSendAccInfo(docSend);
			this.setAbnormalitiesReport(docSend, tempDataList, fwSendAccInfo, errList, request);
		} else {
			errList.add("처리할 자료가 존재하지 않습니다.");
		}
		
		if(errList.size() > 0){
			orderSaveResult.setResultCode("error");
			result.put("errorList", errList);
			orderSaveResult.setResultData(result);;
		}
		return orderSaveResult;
	}
	
	public boolean setAbnormalitiesReport(Map<String, Object> docSend, List<Map<String, Object>> tempDataList, Map<String, Object> fwSendAccInfo, List<String> errList, HttpServletRequest request) throws java.lang.Exception {
		Map<String, Object> ediHostSendInfo = null;
		
		Map<String, Object> tempSearchData = null;
		String message = null;
		
		// 에러 메시지 가져오기 조건 세팅
		tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		tempSearchData.put("srchEntCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("srchGroupCode", "EDI_VERIFY_ERROR_CODE");
		
		//////////////////////////////////////////
		// 서비스코드 및 중계사업자 체크
		//////////////////////////////////////////
		if("".equals(Util.null2str(docSend.get("ifServiceCode"))) && !"CUSTOMS".equals(Util.null2str(docSend.get("ediVender")))) {
			errList.add("현재 지원되지 않는 인터페이스 스펙(서비스코드) 입니다.");
			return false;
		}
		
		//////////////////////////////////////////
		// 본/지사 정보 가져오기( 중개사업자별 문서 및 송/수신 정보, TB_SMS_ENT_OFFICE_INTERFACE )
		//////////////////////////////////////////
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "ifServiceCodeIn", false, null, null, null, null);
		if(!"CUSTOMS".equals(Util.null2str(docSend.get("ediVender")))) {
			ediHostSendInfo = manifestMapper.getEdiInterfaceInfo(docSend);
			if(ediHostSendInfo == null || ediHostSendInfo.size() == 0) {
				errList.add("중계사업자정보가 존재하지 않습니다.");
				return false;
			}
		}
		//////////////////////////////////////////
		// 오류검증 및 파일 생성후 전송
		//////////////////////////////////////////
		if(!this.getAbnormalitiesReport(docSend, tempDataList, fwSendAccInfo, ediHostSendInfo, errList, request)) {
			return false;
		}
		
		return true;
	}
	
	public boolean getAbnormalitiesReport(Map<String, Object> docSend, List<Map<String, Object>> tempDataList, Map<String, Object> fwSendAccInfo
			, Map<String, Object> ediHostSendInfo, List<String> errList, HttpServletRequest request) throws java.lang.Exception {
		
		String fileCreateRetVal[] = null;									// 파일 생성후 결과값, [0]:파일명, [1]:로컬파일경로, [2]:파일내용
		Set<String> duplicateMblkey = new HashSet<>();
		
		/** 처리 로그 기록 */
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		IndexApi indexApi = new IndexApi(client);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		DocumentApi documentApi = new DocumentApi(client);
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "gmtTime", false, null, null, null, null);
		
		tmpMap.put("ent_code", docSend.get("entCode"));
		tmpMap.put("customer_code", "");
		tmpMap.put("solution", "ECM");
		tmpMap.put("bound", docSend.get("bound"));
		tmpMap.put("bound_type", docSend.get("boundType"));
		tmpMap.put("hbl_no", "");
		tmpMap.put("if_service_code", "MANIFEST-ABNORMAL");
		tmpMap.put("if_doc_code", "ABNORMAL");
		tmpMap.put("if_doc_name", "하기이상신고");
		tmpMap.put("if_vender", docSend.get("ediVender"));
		tmpMap.put("if_send_recv_type", "S");
		tmpMap.put("if_status", "SEND");
		tmpMap.put("if_status_info", "");
		if(ediHostSendInfo != null) {
			tmpMap.put("if_send_recv_method", ediHostSendInfo.get("IF_S_R_TYPE"));
			tmpMap.put("if_sender_id", ediHostSendInfo.get("IF_SENDER_ID"));
			tmpMap.put("if_receiver_id", ediHostSendInfo.get("IF_RECEIVER_ID"));
		}
		tmpMap.put("if_remark", "");
		tmpMap.put("register_user_id", docSend.get("registerUserId"));
		tmpMap.put("register_user_name", docSend.get("registerUserName"));
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> tempErrorDataList = new ArrayList<Map<String, Object>>();
		
		if(Util.null2str(docSend.get("ediVender")).equals("KCNET")) {
			// 오류 검증
			if(!this.getAbnormalitiesReportValidateKCNET(docSend, tempDataList, fwSendAccInfo, ediHostSendInfo, errList)) {
				return false;
			}
			int seq = 1;
			for(Map<String, Object> mblData : tempDataList) {
				String dataKey = Util.null2str(mblData.get("OD_JOB_NO"));

				if( StringUtils.isNoneBlank(dataKey) && duplicateMblkey.add(dataKey) && (List<Map<String, Object>>)mblData.get("tempHblDataList") != null) {
				    // 파일 생성
				    fileCreateRetVal = this.getSendDataFileCreateAbnormalitiesReportKCNET(docSend, mblData, fwSendAccInfo, ediHostSendInfo);
				
//					if ("Y".equals(customProperties.getEdiSendYn())) {
//						try {
//							
//							log.info("getAbnormalitiesReport HttpClient start "+ docSend.get("entCode"));
//							
//							//테스트 http://121.138.113.7:12101 리얼 http://222.122.233.22:12101
//							String httpPost = Util.null2str("http://" + ediHostSendInfo.get("IF_SVR_IP")) + ":" + Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")) + "/WebServices/services/XmlDataService";
//		//					HttpPost blPost = new HttpPost(httpPost);
//		//					blPost.setHeader("Content-type", "text/xml;charset=utf-8");
//							HttpClient client = HttpClientBuilder.create().build();
//							
//							
//							HttpPost post = new HttpPost(httpPost);
//							post.setHeader("SOAPAction", "");
//					        post.setEntity(new StringEntity(fileCreateRetVal[1], StandardCharsets.UTF_8));
//					        HttpResponse responseBl = client.execute(post);
//					        
//					        int responseCode = 0;
//					        if(responseBl != null) {
//					        	responseCode = responseBl.getStatusLine().getStatusCode();
//					        	
//					        	if(responseCode != 200 ) {
//					        		errList.add("파일전송에 실패하였습니다.");
//					        	}
//					        	
//					        	HttpEntity entity = responseBl.getEntity();
//				                if (entity != null) {
//				                   String result = EntityUtils.toString(entity);
//				                   log.info("KCNET 하기이상 전송 결과 Response: " + result);
//				                }
//					        }
//					        
//					        log.info("getAbnormalitiesReport HttpClient end "+ docSend.get("entCode") + " responseCode :" + responseCode);
//					        
//						}catch(Exception e) {
//							log.error(e.getMessage());
//							throw e;
//						}
//	//					if(!ediCommonService.ftpFileSend(docSend, mblData, fileCreateRetVal, ediHostSendInfo, resultList, resultMap)) {
//	//						errList.add("파일전송에 실패하였습니다.");
//	//					}
//					}

					if ("Y".equals(customProperties.getEdiSendYn())) {
						if(!ediCommonService.ftpFileSend(docSend, mblData, fileCreateRetVal, ediHostSendInfo, resultList, resultMap)) {
							errList.add("파일전송에 실패하였습니다.");
						}
					}

					// 원본파일 이동
					String outFileName = ediCommonService.moveFile(fileCreateRetVal[0], fileCreateRetVal[0], fileCreateRetVal);

					String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
					BulkData bulkData = new BulkData();	// 가져온 데이터를 ES에 입력하기 위해 Bulk List 생성
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(logId);

					tmpMap.put("if_file_path", fileCreateRetVal[3]);
					tmpMap.put("if_file_name", outFileName);
					tmpMap.put("if_doc_reference_no", fileCreateRetVal[5]);
					tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
					tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
					tmpMap.put("register_date_time", System.currentTimeMillis());
					tmpMap.put("es_job_no", logId);

					bulkData.setJsonMap(tmpMap);
					bulkList.add(bulkData);
				}
			}
			
			// ES에 입력
			if (bulkList != null && bulkList.size() > 0) {
				// Index 확인 후 Index 생성
				indexApi = new IndexApi(client);
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				
				documentApi = new DocumentApi(client);
				documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
				
			}
		}else if(Util.null2str(docSend.get("ediVender")).equals("CUSTOMS")) {	// 관세청 송신 추가 2026.01.13 2sowell
		
			// 오류 검증
			if(!this.getAbnormalitiesReportValidateKCNET(docSend, tempDataList, fwSendAccInfo, ediHostSendInfo, errList)) {
				return false;
			}
			
			ProcessLogs processLogs = new ProcessLogs();
			// 처리 로그 기록
	        processLogs.setEntCode(Util.null2str(docSend.get("entCode")));
	        processLogs.setEntBusinessCode(Util.null2str(docSend.get("entBusinessCode")));
	        processLogs.setSolution("BMS");
	        processLogs.setBound(Util.null2str(docSend.get("bound")));
	        processLogs.setBoundType(Util.null2str(docSend.get("boundType")));
	        processLogs.setProcessUserId(Util.null2str(docSend.get("registerUserId")));
	        processLogs.setJobNo("하기이상");
	        processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType("TB_ECM_OD_PANTOS_ABNORMAL");
			processLogs.setDescription("하기이상신고 [SEND] TB_ECM_OD_PANTOS_ABNORMAL 검색태그: 하기이상보고 하기이상");
	        processLogs.setJobLog(new Gson().toJson(docSend));
	        processLogs.setResult("ok");
	        processLogs.setEntBusinessPrintName("SEL");
	        processLogs.setProcessUserName(Util.null2str(docSend.get("registerUserName")));
	        processLogs.setProcessUserNameEng(Util.null2str(docSend.get("registerUserName")));
	        commonService.insertLog(processLogs, request);
			
			/* callSendXmlGOVCBR5IGParserExecutor(String entCode, String entBusinessCode, Map<String,Object> tmpMap, List<Map<String,Object>> abnormalList) */
			tmpMap.put("entBusinessCode", Util.null2str(docSend.get("entBusinessCode")));
			resultMap = bmsSendXmlParser.callSendXmlGOVCBR5IGParserExecutor(docSend, tempDataList, fwSendAccInfo);
			
			tempDataList = (List<Map<String, Object>>) resultMap.get("succList");	// MRS 전송 성공한 데이터 리스트
			tempErrorDataList = (List<Map<String, Object>>) resultMap.get("errorList");	// MRS 전송 실패한 데이터 리스트
			docSend.put("ediList",(List<Map<String, Object>>) resultMap.get("succList"));
			
			// 송신하기전에 POD 입력하는걸로 변경 2026.04.13 2sowell
			
//			List<Map<String,Object>> masterList = (List<Map<String, Object>>) resultMap.get("masterList");
//			for(Map<String, Object> mrnData : masterList) {
//				if(!Util.isEmpty(mrnData.get("MRN"))) {
//					// 전송 성공 건들 POD 저장
//					ProcessLogs processLogs = new ProcessLogs();
//					processLogs.setEntCode(Util.null2str(docSend.get("entCode")));
//					processLogs.setEntBusinessCode(Util.null2str(docSend.get("entBusinessCode")));
//					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
//					processLogs.setProcessUserId(Util.null2str(docSend.get("registerUserId")));
//					processLogs.setLogJobType("DATA");
//					processLogs.setBound(Util.null2str(docSend.get("bound")));
//					processLogs.setBoundType(Util.null2str(docSend.get("boundType")));
//					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
//					processLogs.setDbType(BmsConsts.DbType.BMS_ABNORMAL_RESULT);
//					processLogs.setDescription(BmsConsts.DbType.BMS_ABNORMAL_RESULT+" [SEND MRN:]" + mrnData.get("MRN"));
//					processLogs.setJobLog(new Gson().toJson(mrnData.get("hblList")));
//					processLogs.setJobNo(String.valueOf(mrnData.get("MRN")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
//					processLogs.setResult("ok");
//					processLogs.setProcessUserName(Util.null2str(docSend.get("registerUserName")));
//					processLogs.setProcessUserNameEng(Util.null2str(docSend.get("registerUserName")));
//					//commonService.insertLog(processLogs, request);
//					bmsCommService.insertLogAndPodStatus(processLogs, request
//							, BmsConsts.PodStatus.CUSTOM_POD_NAME /* pod Status 값 매핑 */
//							, "하기이상결과보고(관세청송신)"
//							, null
//							);
//				}
//			}
		} else{
			// 오류 검증
			if(!this.getAbnormalitiesReportValidate(docSend, tempDataList, fwSendAccInfo, ediHostSendInfo, errList)) {
				return false;
			}
			// 파일 생성
			fileCreateRetVal = this.getSendDataFileCreateAbnormalitiesReport(docSend, tempDataList, fwSendAccInfo, ediHostSendInfo);
			
			if ("Y".equals(customProperties.getEdiSendYn())) {
				if(!ediCommonService.ftpFileSend(docSend, tempDataList.get(0), fileCreateRetVal, ediHostSendInfo, resultList, resultMap)) {
					errList.add("파일전송에 실패하였습니다.");
				}
			}
			
			// 원본파일 이동
			String outFileName = ediCommonService.moveFile(fileCreateRetVal[0], fileCreateRetVal[0], fileCreateRetVal);
			
			String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
//			tmpMap.put("if_doc_code", fileCreateRetVal[4]);
			tmpMap.put("if_doc_reference_no", fileCreateRetVal[5]);
			tmpMap.put("if_file_path", fileCreateRetVal[3]);
			tmpMap.put("if_file_name", outFileName);
			tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
			tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			documentApi.createDocument(index, logId, tmpMap);
		}
	
		if(resultMap.get("errorData") != null) {
			resultList = (List<Map<String, Object>>)resultMap.get("errorData");
			for(Map<String, Object> resultData : resultList){
				errList.add(Util.null2str(resultData.get("REFERANCE")) + " : " + Util.null2str(resultData.get("ERROR_MESSAGE")));
			}
		}
		
		//////////////////////////////////////////
		// 정정 M.B/L 상태값 업데이트
		//////////////////////////////////////////
		docSend.put("tempDataList", tempDataList);
		/* TB_ECM_OD_ABNORMAL 테이블은 특송쪽에서 사용하는 테이블로 BMS에선 사용하지 않아서 주석처리한다. 2026.01.14 2sowell */
//		if("PANTK".equals(docSend.get("entCode"))){
//			bmsCargoAbnormalMapper.updateSendBmsInPlanAbnormalitiesReportd(docSend);
//		}else{
//			bmsCargoAbnormalMapper.updateSendAbnormalitiesReportd(docSend);
//		}
		
		if(Util.null2str(docSend.get("ediVender")).equals("CUSTOMS")) {
			for(Map<String, Object> houseData : tempDataList){
				if(!Util.isEmpty(houseData.get("OD_JOB_NO"))) {
					houseData.put("lastPodStatus", "BMS03");
					houseData.put("gmtTime", docSend.get("gmtTime"));
					houseData.put("succYn", "Y");
					bmsCargoAbnormalMapper.updateAbnormalSendDate(houseData);
				}
			}
			
			for(Map<String, Object> houseData : tempErrorDataList){
				if(!Util.isEmpty(houseData.get("OD_JOB_NO"))) {
					houseData.put("lastPodStatus", "BMS02");
					houseData.put("gmtTime", docSend.get("gmtTime"));
					houseData.put("succYn", "E");
					bmsCargoAbnormalMapper.updateAbnormalSendDate(houseData);
				}
			}
			
		}else {
			
			/** MGN->MFN or SHT->MFN일경우  MFN신고시 하기이상 완료여부 Y 업데이트 */
			List<Map<String, Object>> tempHblDataList = null;
			for(Map<String, Object> masterData : tempDataList){
				tempHblDataList = (List<Map<String, Object>>)masterData.get("tempHblDataList");
				if(tempHblDataList == null) continue;
				for(Map<String, Object> houseData : tempHblDataList){
					
					// 2025.11.17 모든 하기이상코드 신고 가능하도록 수정 요청
//					if("MFN".equals(houseData.get("ABNORMAL_CODE")) ) {
						if(!Util.isEmpty(houseData.get("OD_JOB_NO"))) {
							/** MFN(or SHT)전송하지 않고 해당건의 MFN만 전송할경우 전송데이터 리스트에서 조회하기위해 MGN의 전송상태값 업데이트 */
							houseData.put("lastPodStatus", "BMS03");
							houseData.put("gmtTime", docSend.get("gmtTime"));
							houseData.put("succYn", "Y");
							bmsCargoAbnormalMapper.updateAbnormalSendDate(houseData);
						}
//					}
				}
			}
		
		}
		return true;
	}

	public boolean getAbnormalitiesReportValidateKCNET(Map<String, Object> docSend, List<Map<String, Object>> tempDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<String> errList) throws java.lang.Exception {
		List<Map<String, Object>> tempHblDataList = null;
		String mrn = null;
		String mblNo = null;
		String hblNo = null;
		String ediVender = null;
		for(Map<String, Object> tempMblData : tempDataList){
			mblNo = Util.null2str(tempMblData.get("MBL_NO"));
			if( mrn == null ) {
				mrn = Util.null2str(tempMblData.get("MRN"));
			} else {
				if(mrn.equals(tempMblData.get("MRN"))) continue;
				else mrn = Util.null2str(tempMblData.get("MRN"));
			}

			if(ediVender == null){
				ediVender = Util.null2str(tempMblData.get("EDI_VENDER"));
			}else if(!ediVender.equals(Util.null2str(tempMblData.get("EDI_VENDER")))){
				errList.add("M.B/L No : " + mblNo + " 중계사업자가 같은데이터로 전송하시기 바랍니다.");
			}
			if ("".equals(Util.null2str(tempMblData.get("CUSTOMS_CODE")).trim()) || "".equals(Util.null2str(tempMblData.get("CUSTOMS_DPT_CODE")).trim())) {
				errList.add("M.B/L No : " + mblNo + " 세관/과부호를 기재하기 바랍니다.");
			}
			if ((Util.null2str(tempMblData.get("ABNORMAL_CODE")).equals("MFN") && Util.null2str(tempMblData.get("MRN_ABNORMAL")).equals(""))
					|| (!Util.null2str(tempMblData.get("ABNORMAL_CODE")).equals("MFN") && Util.null2str(tempMblData.get("MRN")).equals(""))) {
				errList.add("M.B/L No : " + mblNo + " 제출번호를 기재하기 바랍니다.");
			}
			if (!Util.null2str(tempMblData.get("ABNORMAL_CODE")).equals("MFN") && Util.null2str(tempMblData.get("AR_DATE")).equals("")) {
				errList.add("M.B/L No : " + mblNo + " 입항일자를 기재하기 바랍니다.");
			}
			if ("".equals(Util.null2str(tempMblData.get("AR_DATE_ABNORMAL")))) {
				errList.add("M.B/L No : " + mblNo + " 하기완료일자를 기재하기 바랍니다.");
			}
			if ("".equals(Util.null2str(tempMblData.get("LINE_CODE")).trim()) || "".equals(Util.null2str(tempMblData.get("VESSEL_FLIGHT")).trim())) {
				errList.add("M.B/L No : " + mblNo + " 항공사/편명을 기재하기 바랍니다.");
			}
			docSend.put("masterOdJobNo", tempMblData.get("OD_JOB_NO"));
			docSend.put("masterMrn", tempMblData.get("MRN"));
			if( "PANTK".equals(docSend.get("entCode")) ) {
				tempHblDataList = bmsCargoAbnormalMapper.selectSendBmsInPlanAbnormalitiesAbnormal(docSend);
			}else{
				tempHblDataList = bmsCargoAbnormalMapper.selectSendAbnormalitiesAbnormal(docSend);
			}
			
			for(Map<String, Object> tempHblData : tempHblDataList) {
				hblNo = Util.null2str(tempHblData.get("HBL_NO"));
				if ("".equals(Util.null2str(tempHblData.get("ABNORMAL_CODE")))) {
					errList.add("H.B/L No : " + hblNo + " 사고/상이 내역부호 번호를 기재하기 바랍니다.");
				}
				
				if(!Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MGN")){
					if (Util.null2dbl(tempHblData.get("GWT_ABNORMAL")) == 0) {
						errList.add("H.B/L No : " + hblNo + " 하기결과중량을 기재하기 바랍니다.");
					}
					if (Util.null2int(tempHblData.get("PIECES_ABNORMAL")) == 0) {
						errList.add("H.B/L No : " + hblNo + " 개수(하기 결과 포장)를 기재하기 바랍니다.");
					}
				}
				
				// 2025.11.17 적하 수량 or 중량 Validation 체크 제외 요청 
//				if(!Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MFN") && Util.null2dbl(tempHblData.get("PIECES")) == 0){
//					errList.add("H.B/L No : " + hblNo + " 개수(포장)를 기재하기 바랍니다.");
//				}
//				if(!Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MFN") && Util.null2dbl(tempHblData.get("GWT")) == 0){
//					errList.add("H.B/L No : " + hblNo + " 중량을 기재하기 바랍니다.");
//				}
			}
			tempMblData.put("tempHblDataList", tempHblDataList);
		}

		if (errList.size() > 0) {
			return false;
		}
		return true;
	}

	public boolean getAbnormalitiesReportValidate(Map<String, Object> docSend, List<Map<String, Object>> tempDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<String> errList) throws java.lang.Exception {
		List<Map<String, Object>> tempHblDataList = null;
		String hblNo = null;
		String ediVender = null;
		for(Map<String, Object> mblData : tempDataList) {
			if(ediVender == null){
				ediVender = Util.null2str(mblData.get("EDI_VENDER"));
			}else if(!ediVender.equals(Util.null2str(mblData.get("EDI_VENDER")))){
				errList.add("M.B/L No : " + Util.null2str(mblData.get("MBL_NO")) + " 중계사업자가 같은데이터로 전송하시기 바랍니다.");
			}
			docSend.put("masterOdJobNo", mblData.get("OD_JOB_NO"));
			if("PANTK".equals(docSend.get("entCode"))){
				tempHblDataList = bmsCargoAbnormalMapper.selectSendBmsInPlanAbnormalitiesAbnormal(docSend);
			}else{
				tempHblDataList = bmsCargoAbnormalMapper.selectSendAbnormalitiesAbnormal(docSend);
			}
			for(Map<String, Object> tempHblData : tempHblDataList) {
				hblNo = Util.null2str(tempHblData.get("HBL_NO"));
				tempHblData.put("LINE_CODE", mblData.get("LINE_CODE"));
				hblNo = Util.null2str(tempHblData.get("HBL_NO"));
				if(Util.null2str(tempHblData.get("")).equals("MFN")){
					if ("".equals(Util.null2str(tempHblData.get("AR_DATE_ABNORMAL")))) {
						errList.add("H.B/L No : " + hblNo + " 입항일자를 기재하기 바랍니다.");
					}
				}else{
					if ("".equals(Util.null2str(tempHblData.get("MRN")).trim())) {
						errList.add("H.B/L No : " + hblNo + " 적하목록관리번호를 기재하기 바랍니다.");
					}
					if ("".equals(Util.null2str(tempHblData.get("AR_DATE")).trim())) {
						errList.add("H.B/L No : " + hblNo + " 입항일자를 기재하기 바랍니다.");
					}
				}
				if ("".equals(Util.null2str(tempHblData.get("LINE_CODE")).trim()) || "".equals(Util.null2str(tempHblData.get("VESSEL_FLIGHT")).trim())) {
					errList.add("H.B/L No : " + hblNo + " 항공사/편명을 기재하기 바랍니다.");
				}
				if ("".equals(Util.null2str(tempHblData.get("ABNORMAL_CODE")))) {
					errList.add("H.B/L No : " + hblNo + " 사고/상이 내역 부호를 기재하기 바랍니다.");
				}
				if (!"PANTK".equals(docSend.get("entCode")) && "".equals(Util.null2str(tempHblData.get("DEST_CODE")))) {
					errList.add("H.B/L No : " + hblNo + " Discharging Port를 기재하기 바랍니다.");
				}
			}
			mblData.put("tempHblDataList", tempHblDataList);
		}
		
		// 오류내역이 있을 경우
		if (errList.size() > 0) {
			return false;
		}
		
		return true;
	}

	public Element createElement(Document document, Element parent, Element ele, String eleName, String eleContent) throws Exception {
		return createElement(document, parent, ele, eleName, eleContent, null);
	}

	public Element createElement(Document document, Element parent, Element ele, String eleName, String eleContent, Map<String, String> attribute) throws Exception {
		ele = document.createElement(eleName);
		if(eleContent != null){
			ele.setTextContent(eleContent);
		}
		ele.setTextContent(eleContent);
		if(attribute != null){
			ele.setAttribute(Util.null2str(attribute.get("attributeName")), Util.null2str(attribute.get("attributeContent")));
		}
		parent.appendChild(ele);
		return ele;
	}

	public String[] getSendDataFileCreateAbnormalitiesReportKCNET(Map<String, Object> docSend, Map<String, Object> mblData, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo) throws Exception {
		
		String writeData = null;
		String retData[] = new String[6];
//		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(mblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(mblData.get("OD_JOB_NO")).trim()).append(".txt").toString();	// 전송 파일명
        String ediVender = Util.null2str(docSend.get("ediVender"));
		String dirPath[] = ediCommonService.getSendFilePath(ediVender, "edi/abnormal", docSend);	// 로컬폴더 경로 가져오기
		
		String docCode = "GOVCBR5IG";
		String declarationId = null;
		
		List<Map<String, Object>> tempHblDataList = null;
		int dPieces = 0;
		/*문서쓰기방식 변경하여 추가*/
		String dirPath0 = dirPath[0];
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("userLanguage", docSend.get("userLanguage"));
			podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
			podMap.put("srchCode", "ECM160");
			podMap.put("entCode", docSend.get("entCode"));
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", docSend.get("gmtTime"));
			podMap.put("solution", "ECM");
			podMap.put("updateUserId", docSend.get("registerUserId"));
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			podMap.put("podStatus", "ECM160");
			podMap.put("podTableName", 	"TB_ECM_POD_02");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
			
			/** 마스터 POD 저장안함 */
//			podMap.put("odJobNo", mblData.get("OD_JOB_NO"));
//			podMap.put("blNo", mblData.get("MBL_NO"));
//			podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5));
//			orderBatchMapper.insertOrderPod(podMap);
			
			ByteArrayOutputStream out = null;
			DOMSource source = null;
			StreamResult streamResult = null;
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//DB저장시 제거?
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			
			Document document = null;
			Element root = null;
			Element lv1 = null;
			Element lv2 = null;
			Element lv3 = null;
			Element lv4 = null;
			document = documentBuilder.newDocument();
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
			
			root = document.createElement("wco:Declaration");
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0");
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0 ./schema4G/kcs/data/standard/KCS_DeclarationOfCAG_5IGSchemaModule_1.0_standard.xsd");
			document.appendChild(root);
			
			/** 세관/과부호 */
			/** 2025-04-09 KCNET 일때 세관,세관과 040D8로 고정 요청 **/
//			this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(mblData.get("CUSTOMS_CODE")) + Util.null2str(mblData.get("CUSTOMS_DPT_CODE")));
			this.createElement(document, root, lv1, "wco:DeclarationOfficeID", "040D8");
			/** 전자문서의 기능 */
			this.createElement(document, root, lv1, "wco:FunctionCode", "9");
			/** 제출번호 */
			if(Util.null2str(mblData.get("ABNORMAL_CODE")).equals("MFN")){
				declarationId = Util.null2str(mblData.get("MRN_ABNORMAL")) + Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN"));
				this.createElement(document, root, lv1, "wco:ID", declarationId);
			}else{
				declarationId = Util.null2str(mblData.get("MRN")) + Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN"));
				this.createElement(document, root, lv1, "wco:ID", declarationId);
			}
			/** 제출일자 */
			this.createElement(document, root, lv1, "wco:IssueDateTime", Util.null2str(mblData.get("ISSUE_DATE")));
			/** 문서형태구분 */
			this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBR5IG");
			
			/** wco:BorderTransportMeans 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
			/** 입항일자 */
			if(Util.null2str(mblData.get("ABNORMAL_CODE")).equals("MFN")){
				this.createElement(document, lv1, lv2, "wco:ArrivalDateTime", Util.null2str(mblData.get("AR_DATE_ABNORMAL")));
			}else{
				this.createElement(document, lv1, lv2, "wco:ArrivalDateTime", Util.null2str(mblData.get("AR_DATE")));
			}
			/** 하기완료일자 */
			this.createElement(document, lv1, lv2, "wco:DischargeCompletedDateTime", Util.null2str(mblData.get("AR_DATE_ABNORMAL")));
			/** 편명 */
//			this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(mblData.get("LINE_CODE")) + Util.null2str(mblData.get("VESSEL_FLIGHT")));
			this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(mblData.get("VESSEL_FLIGHT")));	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
			/** 하기결과이상보고자 */
			lv1 = this.createElement(document, root, lv1, "wco:LoadingProofParty", null);
			this.createElement(document, lv1, lv2, "wco:Name", Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")));
			/** wco:BorderTransportMeans 끝 */
			
			/** wco:Consignment 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
			/** Master AWB 번호 */
			lv2 = this.createElement(document, lv1, lv2, "wco:AssociatedTransportDocument", null);
			this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.getSubStringK(Util.null2str(mblData.get("MBL_NO")).trim(), 0, 11));
			
			int cnt = 1;
			int seq = 1;
			String odJobNo = "";
			tempHblDataList = (List<Map<String, Object>>)mblData.get("tempHblDataList");
			for(Map<String, Object> tempHblData : tempHblDataList){
				/** wco:ConsignmentItem 시작 */
				lv2 = this.createElement(document, lv1, lv2, "wco:ConsignmentItem", null);
				/** 사고/상이 내역부호 번호 */
				lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
				this.createElement(document, lv3, lv4, "wco:StatementCode", Util.null2str(tempHblData.get("ABNORMAL_CODE")));
				/** 배정창고 부호 */
				lv3 = this.createElement(document, lv2, lv3, "wco:GoodsConsignedPlace", null);
				this.createElement(document, lv3, lv4, "wco:ID", ediCommonService.getSubStringK(Util.null2str(tempHblData.get("DISC_WH_CODE")), 0, 8));
				
				/** wco:GoodsMeasure 시작 */
				lv3 = this.createElement(document, lv2, lv3, "wco:GoodsMeasure", null);
				/** 총중량 */
				if(Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MFN")){
					this.createElement(document, lv3, lv4, "wco:GrossMassMeasure", "0");
				}else{
					this.createElement(document, lv3, lv4, "wco:GrossMassMeasure", ("0.000".equals(Util.null2str(tempHblData.get("GWT"))) ? "0" : Util.null2str(tempHblData.get("GWT"))) );
				}
				/** 하기결과중량 */
				this.createElement(document, lv3, lv4, "wco:NetNetWeightMeasure", ("0.000".equals(Util.null2str(tempHblData.get("GWT_ABNORMAL"))) ? "0" : Util.null2str(tempHblData.get("GWT_ABNORMAL"))) );
				/** wco:GoodsMeasure 끝 */
				
				/** wco:Packaging 시작 */
				lv3 = this.createElement(document, lv2, lv3, "wco:Packaging", null);
				/** 개수(포장) */
				if(Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MFN")){
					this.createElement(document, lv3, lv4, "wco:QuantityQuantity", "0");
				}else {
					this.createElement(document, lv3, lv4, "wco:QuantityQuantity", Util.null2str(tempHblData.get("PIECES")));
				}
				/** 개수(하기 결과 포장) */
				this.createElement(document, lv3, lv4, "wco:MarksNumbersID", Util.null2str(tempHblData.get("PIECES_ABNORMAL")));
				/** 개수(사고 포장) */
				dPieces = Math.abs(Util.null2int(tempHblData.get("PIECES")) - Util.null2int(tempHblData.get("PIECES_ABNORMAL")));
				if(dPieces > 999){
					dPieces = 999;
				}
				this.createElement(document, lv3, lv4, "kcs:ConcurrentPackagingID", Util.null2str(dPieces));
				/** wco:Packaging 끝 */
				
				/** wco:TransportContractDocument 시작 */
				lv3 = this.createElement(document, lv2, lv3, "wco:TransportContractDocument", null);
				/** House AWB 번호 */
				this.createElement(document, lv3, lv4, "wco:ID", ediCommonService.getSubStringK(Util.null2str(tempHblData.get("HBL_NO")), 0 , 20));
				/** HSN */
				if(Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MFN")){
					this.createElement(document, lv3, lv4, "kcs:HouseBLSequenceID", "");
				}else{
					this.createElement(document, lv3, lv4, "kcs:HouseBLSequenceID", ediCommonService.getSubStringK(Util.null2str(tempHblData.get("HSN")), 0 , 4));
				}
				/** wco:TransportContractDocument 끝 */
				/** wco:ConsignmentItem 끝 */
				if(!"PANTK".equals(docSend.get("entCode")) && !odJobNo.equals(Util.null2str(tempHblData.get("OD_JOB_NO")))){
					podMap.put("odJobNo", tempHblData.get("OD_JOB_NO"));
					podMap.put("blNo", tempHblData.get("HBL_NO"));
					if(tempHblData.get("ECM160_POD_JOB_NO") != null){
						podMap.put("podJobNo", tempHblData.get("ECM160_POD_JOB_NO"));
						orderBatchMapper.updateOrderPod(podMap);
					}else{
						podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
						orderBatchMapper.insertOrderPod(podMap);
					}
					odJobNo = Util.null2str(tempHblData.get("OD_JOB_NO"));
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}
			
			/** MSN */
			if( !Util.null2str(mblData.get("ABNORMAL_CODE")).equals("MFN")) {
				lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
				this.createElement(document, lv2, lv3, "kcs:MasterBLSequenceID", ediCommonService.getSubStringK(Util.null2str(mblData.get("MSN")), 0 , 4));
			}
//			lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
//			if(Util.null2str(mblData.get("ABNORMAL_CODE")).equals("MFN")){
//				this.createElement(document, lv2, lv3, "kcs:MasterBLSequenceID", "");
//			}else{
//				this.createElement(document, lv2, lv3, "kcs:MasterBLSequenceID", ediCommonService.getSubStringK(Util.null2str(mblData.get("MSN")), 0 , 4));
//			}
			/** wco:Consignment 끝 */
			/** 업체관리번호 */
			lv1 = this.createElement(document, root, lv1, "wco:UCR", null);
			this.createElement(document, lv1, lv2, "wco:TraderAssignedReferenceID", Util.null2str(mblData.get("LINE_CODE")) + "0000");
			/** 하역업자 부호 */
			lv1 = this.createElement(document, root, lv1, "wco:Stevedore", null);
			this.createElement(document, lv1, lv2, "wco:ID", "");
			
			out = new ByteArrayOutputStream();
			source = new DOMSource(document);
			streamResult = new StreamResult(out);
			transformer.transform(source, streamResult);
			writeData = new String(out.toByteArray(), StandardCharsets.UTF_8);
			
//			StringBuffer soapData = new StringBuffer();
//			soapData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//			soapData.append("<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
//			soapData.append("<soapenv:Header>");
//			soapData.append("<kcn:Kcnet_Header xmlns:kcn=\"http://www.kcnet.co.kr/kcn\" mustUnderstand=\"0\">");
//			soapData.append("<kcn:SenderId mustUnderstand=\"0\">").append(Util.getFileWriteDataWithRtrim(ediCommonService.getDeleteCharStringFwb(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim()), "String", "4")).append("</kcn:SenderId>");
//			soapData.append("<kcn:ReceiverId mustUnderstand=\"0\">KCNECP</kcn:ReceiverId>");
//			soapData.append("<kcn:DocName mustUnderstand=\"0\">CUSRGD</kcn:DocName>");
//			soapData.append("<kcn:DocCode mustUnderstand=\"0\">5IG</kcn:DocCode>");
//			soapData.append("<kcn:MessageId mustUnderstand=\"0\">").append(Util.null2str(mblData.get("MBL_NO"))).append("</kcn:MessageId>");
//			soapData.append("<kcn:VenderId mustUnderstand=\"0\">YHT</kcn:VenderId>");
//			soapData.append("<kcn:ID mustUnderstand=\"0\">").append(Util.null2str(ediHostSendInfo.get("IF_WEB_ID"))).append("</kcn:ID>");
//			soapData.append("<kcn:Password mustUnderstand=\"0\">").append(Util.null2str(ediHostSendInfo.get("IF_WEB_PWD"))).append("</kcn:Password>");
//			soapData.append("</kcn:Kcnet_Header>");
//			soapData.append("</soapenv:Header>");
//			soapData.append("<soapenv:Body>");
			
//			writeData = soapData.toString() + writeData.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "") + "</soapenv:Body></soapenv:Envelope>";
			writeData = writeData.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
			
	        FileUtil.fileWrite(writeData, dirPath0, fileName, "euc-kr");
	        out.close();
	        
	        sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		} catch(Exception e) {
			throw e;
		} finally {
			sqlSession.close();
		}
		
		retData[0] = fileName;	// 파일명
		retData[1] = writeData;	// 파일 내용
		retData[2] = dirPath[0];	// 원본 로컬 경로
		retData[3] = dirPath[1];	// 백업 로컬 경로
		
		retData[4] = docCode;
		retData[5] = declarationId;
		
		return retData;
	}

	public String[] getSendDataFileCreateAbnormalitiesReport(Map<String, Object> docSend, List<Map<String, Object>> tempDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo) throws Exception {
		
		/*주석처리 문서쓰기 변경
		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		*/
		List<Map<String, Object>> tempHblDataList = null;
		StringBuffer writeData = null;
		String retData[] = new String[6];
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(tempDataList.get(0).get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
        String ediVender = Util.null2str(docSend.get("ediVender"));
		String dirPath[] = ediCommonService.getSendFilePath(ediVender, "edi/abnormal", docSend);	// 로컬폴더 경로 가져오기
		
		String docCode = "AIRIRE";
		String declarationId = null;
		
		/*문서쓰기방식 변경하여 추가*/
		String dirPath0 = dirPath[0];
		
		SqlSession sqlSession = null;
		// 파일 내용 쓰기
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", docSend.get("entCode"));
			podMap.put("userLanguage", docSend.get("userLanguage"));
			podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
			podMap.put("srchCode", "ECM160");
			podMap.put("entCode", docSend.get("entCode"));
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", docSend.get("gmtTime"));
			podMap.put("solution", "ECM");
			podMap.put("updateUserId", docSend.get("registerUserId"));
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			podMap.put("podStatus", "ECM160");
			podMap.put("podTableName", 	"TB_ECM_POD_02");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
			int cnt = 1;
			String odJobNo = "";
			
			writeData = new StringBuffer();
			
			String strFWDCode			= Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim();
            
            int nSegmentCount = 0;
            int dPieces = 0;
            
            //전자문서 참조번호
            String strFwd_SenderID = Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim();
            
            /** 줄바꿈하지않고 전송하도록 수정  */
            writeData.append("UNB+KECA:1+").append(strFwd_SenderID).append(":57+KTNETMFCSA4G:57+");
            writeData.append(DateTime.getDate("yyMMdd:HHmm")).append("+");
            writeData.append("001");
            writeData.append("'");
//            writeData.append("\r\n");
            
            //UNH
            writeData.append("UNH+1+AIRIRE:2.0'");
//            writeData.append("\r\n");
            nSegmentCount++;
            int seq = 1;
            for(Map<String, Object> mblData : tempDataList) {
            	
            	/** 마스터 POD저장안함 */
//    			podMap.put("odJobNo", mblData.get("OD_JOB_NO"));
//    			podMap.put("blNo", mblData.get("MBL_NO"));
//    			podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5));
//    			orderBatchMapper.insertOrderPod(podMap);
    			
            	tempHblDataList = (List<Map<String, Object>>)mblData.get("tempHblDataList");
				for(Map<String, Object> tmpHblData : tempHblDataList){
					/** Version [20] */
	                writeData.append("[20]    ");
	                /** 적하목록관리번호 - MRN 기재 */
	                if(Util.null2str(tmpHblData.get("ABNORMAL_CODE")).equals("MFN")){
	                	declarationId = Util.getFileWriteData(Util.null2str(tmpHblData.get("MRN_ABNORMAL")), "String", "11");
	                	writeData.append(declarationId);
	                }else{
	                	declarationId = Util.getFileWriteData(Util.null2str(tmpHblData.get("MRN")), "String", "11");
	                	writeData.append(declarationId);
	                }
	                /** Master AWB 번호 또는 신용장 번호 */
	                writeData.append(Util.getFileWriteData(ediCommonService.getSubStringK(Util.null2str(tmpHblData.get("MBL_NO")), 0, 11), "String", "20"));
	                /** MSN */
	                if(Util.null2str(tmpHblData.get("ABNORMAL_CODE")).equals("MFN")){
	                	writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("MSN_ABNORMAL")), "String", "4"));
	                }else{
	                	writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("MSN")), "String", "4"));
	                }
	                /** Deconsol Code - 포워더 세관등록부호(4자리) */
	                writeData.append(Util.getFileWriteData(strFWDCode, "String", "4"));
	                /** 편명 */
//	                writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("LINE_CODE")) + Util.null2str(tmpHblData.get("VESSEL_FLIGHT")), "String", "7"));
	                writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("VESSEL_FLIGHT")), "String", "7"));	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
	                
	                if(Util.null2str(tmpHblData.get("ABNORMAL_CODE")).equals("MFN")){
	                	/** 입항일자 */
	                	writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("AR_DATE_ABNORMAL")), "String", "8"));
	                	/** HSN */
	                    writeData.append(Util.getFileWriteData("", "String", "4"));
	                }else{
	                	/** 입항일자 */
	                	writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("AR_DATE")), "String", "8"));
	                	/** HSN */
	                    writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("HSN")), "String", "4"));
	                }
	                
	                /** House AWB 번호 또는 신용장 번호 */
	                writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("HBL_NO")), "String", "20"));
	                /** 사고/상이 내역 부호 */
//	                    MFN 화물은 있으나 적하목록 없음 SHT 포장갯수 부족
//	                    MGN 적하목록은 있으나 화물없음 OVW 중량 초과
//	                    BKI 포장단위내 물품파손 SHW 중량 부족
//	                    BRK 포장파손 WET 비에 젖음
//	                    CND 컨테이너 번호 다름 AWP 생물이 죽었음
//	                    CNN 컨테이너 번호 없음 SLN 봉인번호 다름
//	                    PER 부패 SLW 봉인번호 파손
//	                    OVR 포장갯수 과다 ETC 기타
	                writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("ABNORMAL_CODE")), "String", "3"));
	                /** 배정창고 부호 */
	                writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("DISC_WH_CODE")), "String", "8"));
	                if(Util.null2str(tmpHblData.get("ABNORMAL_CODE")).equals("MFN")){
	                	/** 개수(포장) */
	                    writeData.append(Util.getFileWriteData2("0", "Number", "10"));
	                    /** 총중량 */
	                    writeData.append(Util.getFileWriteData2("0", "Number", "17,3"));
	                }else{
	                	/** 개수(포장) */
	                    writeData.append(Util.getFileWriteData2(Util.null2str(tmpHblData.get("PIECES")), "Number", "10"));
	                    /** 총중량 */
	                    writeData.append(Util.getFileWriteData2(Util.null2str(tmpHblData.get("GWT")), "Number", "17,3"));	
	                }
	                
	                /** 개수(하기결과 포장) */
	                writeData.append(Util.getFileWriteData2(Util.null2str(tmpHblData.get("PIECES_ABNORMAL")), "Number", "10"));
	                /** 하기결과중량 */
	                writeData.append(Util.getFileWriteData2(Util.null2str(tmpHblData.get("GWT_ABNORMAL")), "Number", "17,3"));
	                /** 개수(사고포장) */
	                dPieces = Math.abs(Util.null2int(tmpHblData.get("PIECES")) - Util.null2int(tmpHblData.get("PIECES_ABNORMAL")));
	                writeData.append(Util.getFileWriteData2(dPieces == 0 ? "1" : Util.null2str(dPieces), "Number", "10"));
	                /** B/L Type - (C:CONSOL / X:EXRESS 등),  'B/L Type'이 ‘X’인 경우는 'Discharging port'(양륙항)를 필수로 입력함*/
	                writeData.append("X");
	                /** Discharging Port - 양륙항 기재 */
	                writeData.append(Util.getFileWriteData(Util.null2str(tmpHblData.get("DEST_CODE")), "String", "3"));
	                /** 하역업자부호 - 관세청에서 부여한 하역회사 부호를 기재*/
	                writeData.append(Util.getFileWriteData("", "String", "9"));
	                /** Transfer Agent - (A:항공사, E:특송사 직접 제출)   'B/L Type'이 ‘X’인 경우는 'Transfer Agent'를 필수로 입력함 */
	                writeData.append("E");
	                writeData.append("'");
//	                writeData.append("\r\n");
	                nSegmentCount++;
	                
	                if(!"PANTK".equals(docSend.get("entCode")) && !odJobNo.equals(Util.null2str(tmpHblData.get("OD_JOB_NO")))){
						podMap.put("odJobNo", tmpHblData.get("OD_JOB_NO"));
						podMap.put("blNo", tmpHblData.get("HBL_NO"));
						if(tmpHblData.get("ECM160_POD_JOB_NO") != null){
							podMap.put("podJobNo", tmpHblData.get("ECM160_POD_JOB_NO"));
							orderBatchMapper.updateOrderPod(podMap);
						}else{
							podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
							orderBatchMapper.insertOrderPod(podMap);
						}
						odJobNo = Util.null2str(tmpHblData.get("OD_JOB_NO"));
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
            }

            
            //UNT+세그먼트 갯수(UNH부터 UNT까지 세그먼트 갯수)+1'
            writeData.append("UNT+").append(String.valueOf(++nSegmentCount)).append("+1");
            writeData.append("'");
//            writeData.append("\r\n");
            writeData.append("UNZ+1+001'");
            
            FileUtil.fileWrite(writeData.toString(), dirPath0, fileName, "euc-kr");
            
            sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		} catch(Exception e) {
			throw e;
		} finally {
			sqlSession.close();
		}
		
		retData[0] = fileName;	// 파일명
		retData[1] = writeData.toString();	// 파일 내용
		retData[2] = dirPath[0];	// 원본 로컬 경로
		retData[3] = dirPath[1];	// 백업 로컬 경로
		
		retData[4] = docCode;
		retData[5] = declarationId;
		
		return retData;
	}

	/**
	 * 하기결과이상보고 > 추가 팝업 조회
	 * @param abnormalList
	 * @return
	 * @throws Exception
	 */
	public AbnormalList selectAddPopupSearchList(AbnormalList srchAbnormalList) throws Exception {
		Abnormal abnormalData = new Abnormal();
		AbnormalList abnormalList = new AbnormalList();
		Set<String> duplicateHousekey = new HashSet<>();
		Map<String, Object> masterMap = new HashMap<String, Object>();
		Map<String, Object> mrnMap = new HashMap<String, Object>();
		List<Map<String, Object>> masterMapList = new ArrayList<Map<String, Object>>();
		List<Abnormal> abnormalDataList = new ArrayList<Abnormal>();
		List<Abnormal> houseList = new ArrayList<Abnormal>();
		List<Abnormal> totalAlrAbnormalList = new ArrayList<Abnormal>();
		boolean isNoCargoFlag = false;
		boolean islateCargoFlag = false;
		boolean isFirstFlag = false;
		String firstMrn = "";
		String action = Util.null2str(srchAbnormalList.getAction());
		String masterMrn = Util.null2str(srchAbnormalList.getMasterMrn());
		String masterMrnsYn = Util.null2str(srchAbnormalList.getMasterMrnsYn());
		log.debug("[selectAddPopupSearchList] Action : {}", action);

		if( action.equals("abnormalUpdate") ) {
			if( srchAbnormalList.getDataList() != null && srchAbnormalList.getDataList().size() > 0 ) {
				for(Map<String, Object> data : srchAbnormalList.getDataList()) {
					masterMap = new HashMap<String, Object>();
					abnormalDataList = new ArrayList<Abnormal>();
					abnormalDataList = bmsCargoAbnormalMapper.selectEditPopupSearchList(data);
					log.debug("[{}]abnormalDataList : {}", action, abnormalDataList);
					abnormalData.setAbnormalCode(srchAbnormalList.getApplyAbnormalCode());
					masterMrn = StringUtils.isNotEmpty(masterMrn) ? masterMrn : String.valueOf(srchAbnormalList.getDataList().get(0).get("mrn"));
					if(abnormalDataList.size() > 0) {
					if( ! isFirstFlag && masterMrn.equals(abnormalDataList.get(0).getMrn()) && !masterMrnsYn.equals("Y") ) {
						totalAlrAbnormalList.addAll(abnormalDataList);
						isFirstFlag = true;
					}else {
						if( ! isFirstFlag && masterMrnsYn.equals("Y")) {
							totalAlrAbnormalList.addAll(abnormalDataList);
							isFirstFlag = true;
							firstMrn = abnormalDataList.get(0).getMrn();
						}else if(masterMrnsYn.equals("Y")) {	// 여러 MRN 검색일경우 추가 2026.02.06 2sowell
							if(!firstMrn.equals(abnormalDataList.get(0).getMrn())) {
								firstMrn = StringUtils.isNotEmpty(abnormalDataList.get(0).getMrn()) ? abnormalDataList.get(0).getMrn() : firstMrn;
								totalAlrAbnormalList.addAll(abnormalDataList);	// 조회한 MRN의 하기이상 데이터 모두 넣기
							}
						}
						if( firstMrn.equals(abnormalData.getMrn()) ) {
							setHoustList(duplicateHousekey, houseList, abnormalData, false); // 중복 제외
						}
					}


					
					for(Abnormal abnormalInfo : abnormalDataList) {
						if( StringUtils.isNoneEmpty(abnormalInfo.getMrn()) ) {
							abnormalInfo.setNoCargoYn("N");
							
							masterMap.put("mrn", abnormalInfo.getMrn());
							masterMap.put("mblNo", abnormalInfo.getMblNo());
							masterMap.put("vesselFlight", abnormalInfo.getVesselFlight());
							masterMap.put("arDate", abnormalInfo.getArDate());
							masterMap.put("origCountry", abnormalInfo.getOrigCountry());
							masterMap.put("noCargoYn", "N");
							masterMap.put("lastPodStatus", abnormalInfo.getLastPodStatus());
							masterMapList.add(masterMap);
						}
					}
					}
				}
			}
		} else {
			if( srchAbnormalList.getDataList() != null && srchAbnormalList.getDataList().size() > 0 ) {
				for(Map<String, Object> data : srchAbnormalList.getDataList()) {
					masterMap = new HashMap<String, Object>();
					abnormalDataList = new ArrayList<Abnormal>();
					abnormalData = bmsCargoAbnormalMapper.selectAddPopupSearchList(data);
					if( StringUtils.isNotEmpty(masterMrn) ) data.put("mrn", masterMrn);
					if(masterMrnsYn.equals("Y")) {	// 여러 MRN 검색일경우 추가 2026.02.09 2sowell
						data.put("mrn", abnormalData.getMrn());
					}
					abnormalDataList = bmsCargoAbnormalMapper.selectEditPopupSearchList(data);
					log.debug("[{}]abnormalData : {}", action, abnormalData);
					log.debug("[{}]abnormalDataList : {}", action, abnormalDataList);
					abnormalData.setAbnormalCode(srchAbnormalList.getApplyAbnormalCode());

					if( StringUtils.isNoneEmpty(abnormalData.getMrn()) ) {
						abnormalData.setNoCargoYn("N");

						masterMap.put("mrn", abnormalData.getMrn());
						masterMap.put("mblNo", abnormalData.getMblNo());
						masterMap.put("vesselFlight", abnormalData.getVesselFlight());
						masterMap.put("arDate", abnormalData.getArDate());
						masterMap.put("origCountry", abnormalData.getOrigCountry());
						masterMap.put("noCargoYn", "N");
						masterMap.put("lastPodStatus", abnormalData.getLastPodStatus());
						masterMapList.add(masterMap);
					}

					if( ! isFirstFlag ) {
						totalAlrAbnormalList.addAll(abnormalDataList);
						isFirstFlag = true;
						firstMrn = StringUtils.isNotEmpty(masterMrn) ? masterMrn : abnormalData.getMrn();
						
					}else if(masterMrnsYn.equals("Y")) {	// 여러 MRN 검색일경우 추가 2026.02.06 2sowell
						if(!firstMrn.equals(abnormalData.getMrn())) {
							firstMrn = StringUtils.isNotEmpty(abnormalData.getMrn()) ? abnormalData.getMrn() : firstMrn;
							totalAlrAbnormalList.addAll(abnormalDataList);	// 조회한 MRN의 하기이상 데이터 모두 넣기
						}
					}

					if( firstMrn.equals(abnormalData.getMrn()) ) {
						setHoustList(duplicateHousekey, houseList, abnormalData, false); // 중복 제외
					}
				}
			} else if( srchAbnormalList.getBlNoList() != null && srchAbnormalList.getBlNoList().size() > 0 ) {
				for( String blNo : srchAbnormalList.getBlNoList() ) {
					masterMap = new HashMap<String, Object>();
					mrnMap = new HashMap<String, Object>();
					abnormalDataList = new ArrayList<Abnormal>();
					if( StringUtils.isEmpty(blNo.trim()) ) continue;
					abnormalData = bmsCargoAbnormalMapper.selectAddPopupSrchBlNoList(blNo);
					log.debug("[abnormalAddHblNo]abnormalData : {}", abnormalData);

					if( abnormalData == null ) {
						isNoCargoFlag = true;
						abnormalData = new Abnormal();
						abnormalData.setNoCargoYn("Y");
						abnormalData.setHblNo(blNo);

						masterMap.put("noCargoYn", "Y");
						masterMapList.add(masterMap);

						abnormalData.setAbnormalCode(srchAbnormalList.getApplyAbnormalCode());
						setHoustList(duplicateHousekey, houseList, abnormalData, false); // 중복 제외
					} else {
						islateCargoFlag = true;
						abnormalData.setNoCargoYn("N");

						masterMap.put("mrn", abnormalData.getMrn());
						masterMap.put("mblNo", abnormalData.getMrn());
						masterMap.put("vesselFlight", abnormalData.getVesselFlight());
						masterMap.put("arDate", abnormalData.getArDate());
						masterMap.put("origCountry", abnormalData.getOrigCountry());
						masterMap.put("noCargoYn", "N");
						masterMap.put("lastPodStatus", abnormalData.getLastPodStatus());
						masterMapList.add(masterMap);

						mrnMap.put("mrn", StringUtils.isNotEmpty(masterMrn) ? masterMrn : abnormalData.getMrn());
						abnormalDataList = bmsCargoAbnormalMapper.selectEditPopupSearchList(mrnMap);

						if( ! isFirstFlag ) {
							if(abnormalDataList.size() > 0) totalAlrAbnormalList.addAll(abnormalDataList);
							isFirstFlag = true;
							firstMrn = StringUtils.isNotEmpty(masterMrn) ? masterMrn : abnormalData.getMrn();
						}

						if( StringUtils.isNotEmpty(masterMrn) && ! masterMrn.equals(abnormalData.getMrn()) ) {
							abnormalList.setErrResultMsg("A11390"); /** #1# 건을 #2# 할 수 없습니다. */
						} else if( firstMrn.equals(abnormalData.getMrn()) ) {
							abnormalData.setAbnormalCode(srchAbnormalList.getApplyAbnormalCode());
							setHoustList(duplicateHousekey, houseList, abnormalData, false); // 중복 제외
						}
					}
				}
			}
		}

		List<Map<String, Object>> masterList = masterMapList.stream().distinct().collect(Collectors.toList());
		
		abnormalList.setMrnList(masterList);
		abnormalList.setAbnormalList(houseList);
		abnormalList.setAlrAbnormalList(totalAlrAbnormalList);
		if( isNoCargoFlag && islateCargoFlag ) abnormalList.setErrResultMsg("ERR_ALL");
		log.debug("MrnList Size : {}, AbnormalList Size : {}, AlrAbnormalList Size : {}, errResultMsg : {}", masterList.size(), houseList.size(), totalAlrAbnormalList.size(), abnormalList.getErrResultMsg());
		return abnormalList;
	}

	private void setHoustList(Set<String> duplicateHousekey, List<Abnormal> houseList, Abnormal abnormalData, boolean isEdit) throws Exception {
		String key = abnormalData.getMrn() + "|" + abnormalData.getMblNo() + "|" + abnormalData.getHblNo();
		key = isEdit ? key + "|" + abnormalData.getAbnormalCode() : key;

		if (duplicateHousekey.add(key)) {
		    houseList.add(abnormalData);
		}
	}

	/**
	 * 하기이상결과보고신고 조회 > 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public AbnormalSaveResult deleteAbnormalDeclare(AbnormalList list) throws Exception {
		AbnormalSaveResult abnormalSaveResult = new AbnormalSaveResult();
		List<Abnormal> abnormalList = list.getAbnormalList();
		if(abnormalList == null) {
			return abnormalSaveResult;
		}
		
		String errorList = "";
		List<String> errDelList = new ArrayList<String>();
		List<Map<String, Object>> chkList = bmsCargoAbnormalMapper.selectAbnormalDeleteCheck(list);

		/** MGN=>MFN생성하고 MGN만 신고 후 삭제할경우 삭제불가    MGN,MFN둘다 신고 후 삭제할경우 둘다 삭제 */
		for(Map<String, Object> map : chkList){
			if("N".equals(map.get("MGN_MFN")) && Util.null2int(map.get("SEQ_NO_ID")) == 1){
				errorList += ", " + Util.null2str(map.get("HBL_NO"));
			}
			
			if( !"sendDel".equals(list.getAction()) && !"".equals(Util.null2str(map.get("SEND_DATE"))) ) {
				errDelList.add(Util.null2str(map.get("MRN")));
			}
		}

		Set<String> errDelSet = new HashSet<>(errDelList);
		Map<String, Object> resultData = new HashMap<String, Object>();
		if(errorList.length() > 2) {
			resultData.put("errorList", errorList.substring(2));
			abnormalSaveResult.setResultData(resultData);
			abnormalSaveResult.setResultCode("ABMS100004");	/** 삭제할 수 없습니다.(#1#) */
			return abnormalSaveResult;
		}

		if( !errDelSet.isEmpty() ) {
			resultData.put("errDelList", String.join(", ", errDelSet));
			abnormalSaveResult.setResultData(resultData);
			abnormalSaveResult.setResultCode("ABMS100004");	/** 삭제할 수 없습니다.(#1#) */
			return abnormalSaveResult;
		}

		bmsCargoAbnormalMapper.deleteAbnormalList(list);
		return abnormalSaveResult;
	}

	/**
	 * ECM 하기이상결과보고 결과 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public AbnormalList selectAbnormalResultList(SearchAbnormalList search) throws Exception {
		AbnormalList abnormalList = new AbnormalList();
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

		List<Abnormal> list = null;
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "A.DECLARATION_DATE", "srchNoType", "A.HBL_NO");
		list = bmsCargoAbnormalMapper.selectAbnormalResultList(search);

		abnormalList.setAbnormalList(list);
		if(list != null && list.size() > 0) {
			abnormalList.setTotal(list.get(0).getTotal());
		}else {
			abnormalList.setTotal(0);
		}

		return abnormalList;
	}

	/**
	 * 하기결과이상보고 > 무적MFN 팝업 MRN 조회
	 * @param abnormalList
	 * @return
	 * @throws Exception
	 */
	public AbnormalList selectAddPopupMrnSearchList(SearchAbnormalList search) throws Exception {
		AbnormalList abnormalList = new AbnormalList();
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

		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "AR_DATE", "srchNoType", "");
		List<Map<String, Object>> mrnList = bmsCargoAbnormalMapper.selectAddPopupMrnSearchList(search);
		abnormalList.setMrnList(mrnList);

		if(mrnList != null && mrnList.size() > 0) abnormalList.setTotal(mrnList.size());
		else abnormalList.setTotal(0);

		return abnormalList;
	}
	
	/**
	 * 하기결과이상보고 > 무적 MFN 추가 팝업 조회
	 * @param abnormalList
	 * @return
	 * @throws Exception
	 */
	public AbnormalList selectAddPopupNoCargoSearchList(AbnormalList srchAbnormalList) throws Exception {
		Abnormal abnormalData = new Abnormal();
		AbnormalList abnormalList = new AbnormalList();
		List<Abnormal> abnormalDataList = new ArrayList<Abnormal>();
//		List<Abnormal> totalAlrAbnormalList = new ArrayList<Abnormal>();

		if( srchAbnormalList.getDataList() != null && srchAbnormalList.getDataList().size() > 0 ) {
			for( Map<String, Object> dataMap : srchAbnormalList.getDataList() ) {
				abnormalData = bmsCargoAbnormalMapper.selectAddPopupNoCargoData(dataMap);
				if( abnormalData == null ) {
					abnormalData = new Abnormal();
					abnormalData.setMrn( String.valueOf(dataMap.get("mrn")) );
					abnormalData.setMblNo( String.valueOf(dataMap.get("mblNo")) );
					abnormalData.setVesselFlight( String.valueOf(dataMap.get("vesselFlight")) );
					abnormalData.setArDate( String.valueOf(dataMap.get("arDate")) );
					abnormalData.setOrigCountry( String.valueOf(dataMap.get("origCountry")) );
					abnormalData.setHblNo( String.valueOf(dataMap.get("hblNo")) );
					abnormalData.setAbnormalCode( String.valueOf(dataMap.get("abnormalCode")) );
					abnormalData.setHblPieces( 0 );
					abnormalData.setHblGwt( 0 );
					abnormalData.setPieces( 0 );
					abnormalData.setGwt( 0 );
					abnormalData.setCnt( 0 );
				}

				abnormalDataList.add(abnormalData);
			}
		}

		abnormalList.setAbnormalList(abnormalDataList);
//		abnormalList.setAlrAbnormalList(totalAlrAbnormalList);
		log.debug("AbnormalList Size : {}", abnormalDataList.size());
		return abnormalList;
	}
	
	
public String getSendDataFileCreateAbnormalitiesReportString(Map<String, Object> docSend, Map<String, Object> mblData, Map<String, Object> fwSendAccInfo) throws Exception {
		
		String writeData = null;		// xml 송신 파일 내용
		String declarationId = null;
		
		List<Map<String, Object>> tempHblDataList = null;
		int dPieces = 0;
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			ByteArrayOutputStream out = null;
			DOMSource source = null;
			StreamResult streamResult = null;
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//DB저장시 제거?
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			
			Document document = null;
			Element root = null;
			Element lv1 = null;
			Element lv2 = null;
			Element lv3 = null;
			Element lv4 = null;
			document = documentBuilder.newDocument();
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
			
			root = document.createElement("wco:Declaration");
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0");
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0 ./schema4G/kcs/data/standard/KCS_DeclarationOfCAG_5IGSchemaModule_1.0_standard.xsd");
			document.appendChild(root);
			
			/** 세관/과부호 */
			/** 2025-04-09 KCNET 일때 세관,세관과 040D8로 고정 요청 **/
			this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(mblData.get("CUSTOMS_CODE")) + Util.null2str(mblData.get("CUSTOMS_DPT_CODE")));
			//this.createElement(document, root, lv1, "wco:DeclarationOfficeID", "040D8");
			/** 전자문서의 기능 */
			this.createElement(document, root, lv1, "wco:FunctionCode", "9");
			/** 제출번호 */
			if(Util.null2str(mblData.get("ABNORMAL_CODE")).equals("MFN")){
				declarationId = Util.null2str(mblData.get("MRN_ABNORMAL")) + Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN"));
				this.createElement(document, root, lv1, "wco:ID", declarationId);
			}else{
				declarationId = Util.null2str(mblData.get("MRN")) + Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN"));
				this.createElement(document, root, lv1, "wco:ID", declarationId);
			}
			/** 제출일자 */
			this.createElement(document, root, lv1, "wco:IssueDateTime", Util.null2str(mblData.get("ISSUE_DATE")));
			/** 문서형태구분 */
			this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBR5IG");
			
			/** wco:BorderTransportMeans 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
			/** 입항일자 */
			if(Util.null2str(mblData.get("ABNORMAL_CODE")).equals("MFN")){
				this.createElement(document, lv1, lv2, "wco:ArrivalDateTime", Util.null2str(mblData.get("AR_DATE_ABNORMAL")));
			}else{
				this.createElement(document, lv1, lv2, "wco:ArrivalDateTime", Util.null2str(mblData.get("AR_DATE")));
			}
			/** 하기완료일자 */
			this.createElement(document, lv1, lv2, "wco:DischargeCompletedDateTime", Util.null2str(mblData.get("AR_DATE_ABNORMAL")));
			/** 편명 */
			this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(mblData.get("VESSEL_FLIGHT")));	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
			/** 하기결과이상보고자 */
			lv1 = this.createElement(document, root, lv1, "wco:LoadingProofParty", null);
			this.createElement(document, lv1, lv2, "wco:Name", Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")));
			/** wco:BorderTransportMeans 끝 */
			
			int cnt = 1;
			int seq = 1;
			String odJobNo = "";
			tempHblDataList = (List<Map<String, Object>>)mblData.get("tempHblDataList");
			
			// BL리스트들을 MSN과 MBL_NO로 그룹바이함 2026.04.13 2sowell
			Map<String, List<Map<String, Object>>> groupedHblDataList = tempHblDataList.stream()
				    .collect(Collectors.groupingBy(m -> Util.null2str(m.get("MSN")) + "_" + Util.null2str(m.get("MBL_NO"))));
			
			/** Consignment 반복 - MSN(MBL)마다 2026.04.13 2sowell */
			for (List<Map<String, Object>> hblDataList : groupedHblDataList.values()) {
				Map<String, Object> groupedHblMap = hblDataList.get(0);
				
				/** wco:Consignment 시작 */
				lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
				/** Master AWB 번호 */
				lv2 = this.createElement(document, lv1, lv2, "wco:AssociatedTransportDocument", null);
				this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.getSubStringK(Util.null2str(groupedHblMap.get("MBL_NO")).trim(), 0, 11));
				
				for(Map<String, Object> tempHblData : hblDataList){
					/** wco:ConsignmentItem 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:ConsignmentItem", null);
					/** 사고/상이 내역부호 번호 */
					lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
					this.createElement(document, lv3, lv4, "wco:StatementCode", Util.null2str(tempHblData.get("ABNORMAL_CODE")));
					/** 배정창고 부호 */
					lv3 = this.createElement(document, lv2, lv3, "wco:GoodsConsignedPlace", null);
					this.createElement(document, lv3, lv4, "wco:ID", ediCommonService.getSubStringK(Util.null2str(tempHblData.get("DISC_WH_CODE")), 0, 8));
					
					/** wco:GoodsMeasure 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:GoodsMeasure", null);
					/** 총중량 */
					if(Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MFN")){
						this.createElement(document, lv3, lv4, "wco:GrossMassMeasure", "0");
					}else{
						this.createElement(document, lv3, lv4, "wco:GrossMassMeasure", ("0.000".equals(Util.null2str(tempHblData.get("GWT"))) ? "0" : Util.null2str(tempHblData.get("GWT"))) );
					}
					/** 하기결과중량 */
					this.createElement(document, lv3, lv4, "wco:NetNetWeightMeasure", ("0.000".equals(Util.null2str(tempHblData.get("GWT_ABNORMAL"))) ? "0" : Util.null2str(tempHblData.get("GWT_ABNORMAL"))) );
					/** wco:GoodsMeasure 끝 */
					
					/** wco:Packaging 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:Packaging", null);
					/** 개수(포장) */
					if(Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MFN")){
						this.createElement(document, lv3, lv4, "wco:QuantityQuantity", "0");
					}else {
						this.createElement(document, lv3, lv4, "wco:QuantityQuantity", Util.null2str(tempHblData.get("PIECES")));
					}
					/** 개수(하기 결과 포장) */
					this.createElement(document, lv3, lv4, "wco:MarksNumbersID", Util.null2str(tempHblData.get("PIECES_ABNORMAL")));
					/** 개수(사고 포장) */
					dPieces = Math.abs(Util.null2int(tempHblData.get("PIECES")) - Util.null2int(tempHblData.get("PIECES_ABNORMAL")));
					if(dPieces > 999){
						dPieces = 999;
					}
					this.createElement(document, lv3, lv4, "kcs:ConcurrentPackagingID", Util.null2str(dPieces));
					/** wco:Packaging 끝 */
					
					/** wco:TransportContractDocument 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:TransportContractDocument", null);
					/** House AWB 번호 */
					this.createElement(document, lv3, lv4, "wco:ID", ediCommonService.getSubStringK(Util.null2str(tempHblData.get("HBL_NO")), 0 , 20));
					/** HSN */
					if(Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MFN")){
						this.createElement(document, lv3, lv4, "kcs:HouseBLSequenceID", "");
					}else{
						this.createElement(document, lv3, lv4, "kcs:HouseBLSequenceID", ediCommonService.getSubStringK(Util.null2str(tempHblData.get("HSN")), 0 , 4));
					}
					/** wco:TransportContractDocument 끝 */
					/** wco:ConsignmentItem 끝 */
				}
				
				String msn = "";
				boolean onlyMfn = true; // 해당 MRN에 MFN만 있는지여부( default: MFN만 있음)

				for (Map<String, Object> map : hblDataList) {
				    Object abnormalCode = map.get("ABNORMAL_CODE");

				    if (!"MFN".equals(abnormalCode)) {
				        onlyMfn = false;

				        Object msnObj = map.get("MSN");
				        if (msnObj != null) {
				            msn = String.valueOf(msnObj);
				        }

				        break;
				    }
				}

				if (onlyMfn) { // MFN만 있는경우 msn을 ""공백처리
				    msn = "";
				}
				
				/** MSN */
				if(!"".equals(msn)) { // msn이 있는경우 (MFN만 있는경우: "",MBL번호 쌍)(MFN이외의 데이터도 있는경우: MSN번호,MBL번호 ※MSN이 반드시 존재)
					lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
					this.createElement(document, lv2, lv3, "kcs:MasterBLSequenceID", ediCommonService.getSubStringK(msn, 0 , 4));
				}
				//if( !Util.null2str(mblData.get("ABNORMAL_CODE")).equals("MFN")) {
				//	lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
				//	this.createElement(document, lv2, lv3, "kcs:MasterBLSequenceID", ediCommonService.getSubStringK(msn, 0 , 4));
				//}
			}
			/** 업체관리번호 */
			lv1 = this.createElement(document, root, lv1, "wco:UCR", null);
			this.createElement(document, lv1, lv2, "wco:TraderAssignedReferenceID", Util.null2str(mblData.get("LINE_CODE")) + "0000");
			/** 하역업자 부호 */
			lv1 = this.createElement(document, root, lv1, "wco:Stevedore", null);
			this.createElement(document, lv1, lv2, "wco:ID", "");
			
			out = new ByteArrayOutputStream();
			source = new DOMSource(document);
			streamResult = new StreamResult(out);
			transformer.transform(source, streamResult);
			writeData = new String(out.toByteArray(), StandardCharsets.UTF_8);	// 송신 XML 파일 내용 String으로 변환
			writeData = writeData.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
	
	     
	        out.close();
	        
	        sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		} catch(Exception e) {
			throw e;
		} finally {
			sqlSession.close();
		}
		
		return writeData;
	}


	/**
	 * 하기결과이상보고 > 무적 Add HBL 다중조회 리스트
	 * @param abnormalList
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> selectAddPopupNoCargoHblSearchList(AbnormalList srchAbnormalList) throws Exception {
		List<String> blNoList = srchAbnormalList.getBlNoList();	// 다중입력 HBL 리스트
		List<Map<String,Object>> returnList = null;
		
		if(blNoList != null) {
			returnList = bmsCargoAbnormalMapper.selectHblInPlanInfoList(blNoList);
		}
		return returnList;
	}
}