package org.yht.exps.express.service.edi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.edi.AbnormalMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.edi.abnormal.Abnormal;
import org.yht.exps.express.model.edi.abnormal.AbnormalList;
import org.yht.exps.express.model.edi.abnormal.AbnormalSaveResult;
import org.yht.exps.express.model.edi.abnormal.search.SearchAbnormalList;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AbnormalService {
		
	@Autowired
	AbnormalMapper abnormalMapper;
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
		if("PANTK".equals(search.getSrchEntCode())) {
			Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "bip.AR_DATE|odAdnor.REGISTER_DATE_TIME", "srchNoType", "bip.MBL_NO|bip.BL_NO|odAdnor.MRN");
			list  = abnormalMapper.selectBmsInPlanAbnormalList(search);
		}else {
			Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "od.AR_DATE|od.OB_DATE|odAdnor.REGISTER_DATE_TIME", "srchNoType", "od.MBL_NO|od.HBL_NO|odAdnor.MRN");
			list  = abnormalMapper.selectAbnormalList(search);
		}
		
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
			abnormalList.setAbnormalList(abnormalMapper.selectBmsInPlanAbnormalEntryList(search));
		}else{
			abnormalList.setAbnormalList(abnormalMapper.selectAbnormalEntryList(search));
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
		if("PANTK".equals(search.getSrchEntCode())) {
			Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "odAdnor.SEND_DATE|bip.AR_DATE", "srchNoType", "bip.MBL_NO|bip.BL_NO|odAdnor.MRN");
			list  = abnormalMapper.selectBmsInPlanAbnormalDeclareList(search);
		}else{
			Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "odAdnor.SEND_DATE|od.AR_DATE|odAdd.UNMANAGEMENT_DATE", "srchNoType", "od.MBL_NO|od.HBL_NO|odAdnor.MRN");
			list  = abnormalMapper.selectAbnormalDeclareList(search);
		}
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
		
		List<Abnormal> list  = abnormalMapper.selectAbnormalAddPopupList(search);
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

			abnormalMapper.insertAbnormalList(ab);
		}
		
		return abnormalSaveResult;
	}
	
	/**
	 * 하기이상결과 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public AbnormalSaveResult deleteAbnormalList(AbnormalList list) throws Exception {
		AbnormalSaveResult abnormalSaveResult = new AbnormalSaveResult();
		List<Abnormal> abnormalList = list.getAbnormalList();		
		if(abnormalList == null) {
			return abnormalSaveResult;
		}
		
		String errorList = "";
		List<Map<String, Object>> chkList = abnormalMapper.selectAbnormalDeleteCheck(list);
		for(Map<String, Object> map : chkList){
			if(map.get("MGN_MFN") != null && Util.null2int(map.get("SEQ_NO_ID")) == 1){
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
		if(!"PANTK".equals(list.getEntCode())){
			for(Map<String, Object> map : chkList){
				if("MFN".equals(map.get("ABNORMAL_CODE"))){
					delSpecialItemList.add(map);
				}
			}
		}
		
		abnormalMapper.deleteAbnormalList(list);
		if(delSpecialItemList != null && delSpecialItemList.size() > 0) {
			abnormalMapper.deleteSpecialItemList(delSpecialItemList);
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
		List<Map<String, Object>> chkList = abnormalMapper.selectAbnormalDeleteCheck(list);
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
				abnormalMapper.updateAbnormalAdditional(ab);
			}
		}
		
		abnormalMapper.deleteAbnormalDeclareList(list);
		if(delSpecialItemList != null && delSpecialItemList.size() > 0) {
			abnormalMapper.deleteSpecialItemList(delSpecialItemList);
		}
		
		if(!"PANTK".equals(list.getEntCode())){
			for(int i=0;i<abnormalList.size();i++) {
				Abnormal ab = abnormalList.get(i);
				if("".equals(Util.null2str(ab.getMgnMfnSendDate()))) {
					abnormalMapper.deleteAbnormalDeclarePod(ab);
					abnormalMapper.updateLastPodMax(ab);
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
		
		Map<String, Object> map = abnormalMapper.getNoRegisterCount(abnormal);
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
//			tmpMap = abnormalMapper.selectAbnormalChk(tmpMap);
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
//			abnormalMapper.insertAbnormalOrder(abnormal);
//			abnormalMapper.insertAbnormalOrderAdditional(abnormal);
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
				noList = abnormalMapper.selectHouseListMgnMfn(abnormal);
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
				noList = abnormalMapper.selectHouseList(tmpMap);
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
				if(abnormalMapper.selectAbnormalDupChk(tmpMap) > 0){
					abnormalSaveResult.setResultCode("A11253");
					return abnormalSaveResult;
				}
			}
			Util.accessControlDatabase(abnormal, "gmtTime", false, null, null, null, null);
			if("PANTK".equals(abnormal.getEntCode())){
				abnormalMapper.insertBmsInPlanAbnormal(abnormal);
			}else{
				abnormalMapper.insertAbnormal(abnormal);
			}
			
			if("SEA".equals(abnormal.getBound())){
				abnormalMapper.updateUnmanagementDate(abnormal);
				for(int i = 0; i < listSize; i += 1000){
					if(i + 1000 < listSize) {
						tmpMap.put("dataList", noList.subList(i, i + 1000));
					}else{
						tmpMap.put("dataList", noList.subList(i, listSize));
					}
					 List<Map<String, Object>> dataList = (List<Map<String, Object>>) tmpMap.get("dataList");
				        for(Map<String, Object> data : dataList){
				            Map<String, Object> podMap = new HashMap<>();
				            podMap.put("entCode", abnormal.getEntCode());
				            podMap.put("gmtTime", abnormal.getGmtTime());
				            podMap.put("updateUserId", abnormal.getRegisterUserId());
				            podMap.put("odJobNo", data.get("odJobNo"));
				            podMap.put("blNo", data.get("hblNo"));
				            podMap.put("podStatus", "ECM160");
				            podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
				            podMap.put("podTableName", "TB_ECM_POD_02");
				            orderMapper.deleteOrderPod(podMap);
				            orderMapper.insertOrderPod(podMap);
				        }
				}
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
						abnormalMapper.insertSpecialManageItem(ab);
						
						//2.후착화물
						ab.put("MANAGE_TYPE", "3");
						ab.put("FLIGHT_NO", abnormal.getVesselFlight());
						ab.put("AR_DATE", abnormal.getArDate());
						ab.put("GWT", abnormal.getGwt());
						ab.put("PIECES", abnormal.getPieces());
						abnormalMapper.insertSpecialManageItem(ab);
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
					abnormalMapper.insertSpecialManageItem(ab);
					
					//2.후착화물
					ab.put("MANAGE_TYPE", "3");
					ab.put("GWT", abnormal.getGwt());
					ab.put("PIECES", abnormal.getPieces());
					abnormalMapper.insertSpecialManageItem(ab);
				}
			}
		}else{
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				AbnormalMapper abnormalBatchMapper = sqlSession.getMapper(AbnormalMapper.class);
				
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
//				abnormalMapper.insertAbnormal(ab);				
//				abnormalMapper.insertSpecialItem(ab);
//			}
//			else if("SHT".equals(abCd) || "OVR".equals(abCd)) {
//				abnormalMapper.insertSpecialItem(ab);
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
					tmpMap = abnormalMapper.selectAbnormalCntrInfo(tmpMap);
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
					List<Map<String, Object>> abnormalCntrList = abnormalMapper.selectAbnormalCntrInfoList(tmpMap);
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
								cntrNoChk = abnormalMapper.selectAbnormalCntrInfo(tmpMap);
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
						cntrNoChk = abnormalMapper.selectAbnormalCntrInfo(tmpMap);
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
			tmpMap = abnormalMapper.selectAbnormalCntrInfo(tmpMap);
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
				/** UPS는 정정일련번호 101부터 생성 (다른 수입부에서 KTNET사이트에서 직접 정정하고 있어서 번호 겹치지 않게 하기 위함) */
				if(list.getEntCode().equals("YHTXX") || list.getEntCode().equals("UPSXK")) {
					modSerialNo = "0101";
				}else {
					modSerialNo = "0001";
				}
			}
		}else{
			/** UPS는 정정일련번호 101부터 생성 (다른 수입부에서 KTNET사이트에서 직접 정정하고 있어서 번호 겹치지 않게 하기 위함) */
			if(list.getEntCode().equals("YHTXX") || list.getEntCode().equals("UPSXK")) {
				modSerialNo = "0101";
			}else {
				modSerialNo = "0001";
			}
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
				modCnt = (abnormalMapper.getIsModContainerData(list)).intValue();
			}else{
				modCnt = (abnormalMapper.getIsModData(list)).intValue();
			}
			if(modCnt > 0) {
				abnormalSaveResult.setResultCode("A11260");	/** 적하정정건 이미 존재합니다. */
				return abnormalSaveResult;
			}
		}
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			AbnormalMapper abnormalBatchMapper = sqlSession.getMapper(AbnormalMapper.class);
			
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
				abnormalList = abnormalMapper.selectAbnormalHouseList(tmpMap);
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
					abnormalList = abnormalMapper.selectAbnormalHouseList(tmpMap);
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
	public String insertModList(List<Abnormal> abnormalList, AbnormalList list, Abnormal tmpAbnormal, List<String> singleModCodeList, List<Map<String, Object>> containerList, AbnormalMapper abnormalBatchMapper, 
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
			abnormalMapper.insertMblCorrectByAb(tmpMap);
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
				seqIdNoMap = abnormalMapper.getSeqNoIdByAb(tmpAbnormal);
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
						seqIdNoMap = abnormalMapper.getSeqNoIdByAb(tmpAbnormal);
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
//					for(String cntrNo : containerUpdateMap.keySet()){
//						for(Map<String, Object> cntrMap : containerUpdateMap.get(cntrNo)){
//							abnormalBatchMapper.updateOrderContainer(cntrMap);
//						}
//					}
					for(String cntrNo : containerUpdateMap.keySet()){
						for(Map<String, Object> cntrMap : containerUpdateMap.get(cntrNo)){
							abnormalBatchMapper.insertContainerCorrectByAb(cntrMap);
						}
					}
					tmpMap.clear();
					tmpMap.put("modJobNo", modJobNo);
//					for(String cntrNo : containerUpdateMap.keySet()){
//						if(updateCntrNoChk2.get(cntrNo) != null){
//							for(Map<String, Object> cntrMap : containerUpdateMap.get(cntrNo)){
//								tmpMap.put("seqNoId", cntrMap.get("seqNoId"));
//								tmpMap.put("beforeCntrNo", cntrNo);
//								tmpMap.put("afterCntrNo", updateCntrNoChk2.get(cntrNo));
//								abnormalBatchMapper.updateModContainerNo(tmpMap);
//							}
//						}
//					}
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
					seqIdNoMap = abnormalMapper.getSeqNoIdByAb(tmpAbnormal);
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
			abnormalMapper.updateMblCorrectByAb(tmpMap);
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
	//				Map<String, Object> rstHbl = abnormalMapper.selectHblValidate(resultData);
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
		if(dataList == null) {
			return abnormalSaveResult;
		}
		
		Map<String, Object> chkMap;
		StringBuffer chkErr = new StringBuffer();
		Abnormal abnormal = null;
		List<Abnormal> orderInsertList = new ArrayList<Abnormal>();
		String hblNo;
		String action = Util.null2str(abnormalList.getAction());
		
		if(action.equals("abnormalAdd") || action.equals("abnormalUpdate")){
			for(Map<String, Object> data : dataList){
				if(Util.null2str(data.get("AR_DATE")).equals("") || (!"PANTK".equals(abnormalList.getEntCode()) && Util.null2str(data.get("DEST_CODE")).equals("")) || Util.null2str(data.get("MRN")).equals("") || Util.null2str(data.get("MSN")).equals("")
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
		}
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			AbnormalMapper abnormalMapperBatch = sqlSession.getMapper(AbnormalMapper.class);
			
			int cnt = 1;
			if(action.equals("abnormalUpdate")){
				for(Map<String, Object> ab : dataList) {
					ab.put("registerUserId", abnormalList.getRegisterUserId());
					ab.put("gmtTime", abnormalList.getGmtTime());
					abnormalMapperBatch.updateAbnormal(ab);
					if((cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}else{
				int hsn;
				for(Map<String, Object> data : dataList){
					hblNo = Util.null2str(data.get("HBL_NO"));
					data.put("action", action);
					if("PANTK".equals(abnormalList.getEntCode())){
						chkMap = abnormalMapper.selectBmsInPlanAbnormalChk(data);
					}else{
						chkMap = abnormalMapper.selectAbnormalChk(data);
					}
					if(chkMap != null && chkMap.size() > 0){
						if(!Util.null2str(chkMap.get("ABNORMAL_CODE_DUP")).equals("")){
							chkErr.append("MAWB No : " + Util.null2str(data.get("MBL_NO")) + ", HAWB No : " + hblNo + " 하기이상코드가 이미 등록되어 있습니다.<br />");
						}
						/** 사업장에 저장된 값 이외의 값을 사용자가 선택해서 저장할수 있도록 수정함 */
		//				if(Util.null2str(chkMap.get("EDI_ASSIGNMENT_WH_CODE")).equals("") || !Util.null2str(chkMap.get("EDI_ASSIGNMENT_WH_CODE")).equals(Util.null2str(data.get("DISC_WH_CODE")))){
		//					chkErr.append("MAWB No : " + Util.null2str(data.get("MBL_NO")) + ", HAWB No : " + hblNo + " 배정창고코드가 일치하지 않습니다.<br />");
		//				}
						if(Util.null2str(data.get("ABNORMAL_CODE")).equals("MFN") && action.equals("") && !"PANTK".equals(abnormalList.getEntCode())){
							if(Util.null2str(chkMap.get("HBL_NO")).equals("")){
								hsn = Util.null2int(chkMap.get("MAX_HSN")) + 1;
								if(hsn > 9999){
									abnormalSaveResult.setResultCode("A11287");	/** 사용가능한 HSN이 없습니다. */
									return abnormalSaveResult;
								}
								abnormal = new Abnormal();
								abnormal.setHblNo(hblNo);
								abnormal.setPieces(Util.null2int(data.get("PIECES")));
								abnormal.setGwt(Util.null2int(data.get("GWT")));
								abnormal.setMasterOdJobNo(Util.null2str(chkMap.get("MASTER_OD_JOB_NO")));
								abnormal.setHsn(Util.lpad(Util.null2str(hsn), 4, '0'));
								abnormal.setCdType("N");
								abnormal.setCdUseType("1");
								abnormal.setEcmType("0");
								orderInsertList.add(abnormal);
							}
						}else{
							if(Util.null2str(chkMap.get("HBL_NO")).equals("")){
								chkErr.append("HAWB No " + hblNo + " 데이터가 존재하지 않습니다.<br />");
							}
						}
						data.put("OD_JOB_NO", chkMap.get("OD_JOB_NO"));
					}else{
						if("PANTK".equals(abnormalList.getEntCode())){
							chkErr.append("HAWB No " + Util.null2str(data.get("HBL_NO")) + " 데이터가 존재하지 않습니다.<br />");
						}else{
							chkErr.append("MAWB No " + Util.null2str(data.get("MBL_NO")) + " 데이터가 존재하지 않습니다.<br />");
						}
					}
					if(chkErr.length() > 1000){
						break;
					}
				}
				
				if(chkErr.length() > 0){
					chkMap = new HashMap<String, Object>();
					chkMap.put("errorStr", chkErr.toString());
					abnormalSaveResult.setResultData(chkMap);
					abnormalSaveResult.setResultCode("abnormalChkError");
					return abnormalSaveResult;
				}
				
				chkMap = new HashMap<String, Object>();
				chkMap.put("gmtTime", abnormalList.getGmtTime());
				chkMap.put("entCode", abnormalList.getEntCode());
				chkMap.put("entBusinessCode", "");
				chkMap.put("registerUserId", abnormalList.getRegisterUserId());
				chkMap.put("updateUserId", abnormalList.getRegisterUserId());
				chkMap.put("solution", "ECM");
				chkMap.put("stockType", "OD");
				chkMap.put("stockSign", "COD");
				chkMap.put("stockSeqLength", 8);
				chkMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				chkMap.put("stockRemark", "ORDER-JOB NO");
				
				Map<String, Object> packingMap = new HashMap<String, Object>();
//				int packingCnt;
				
				for(Abnormal orderInsert : orderInsertList){
					orderInsert.setOdJobNo(commonMapper.selectNextStockMaxNo(chkMap));
					orderInsert.setGmtTime(abnormalList.getGmtTime());
					orderInsert.setRegisterUserId(abnormalList.getRegisterUserId());
					orderInsert.setEntCode(abnormalList.getEntCode());
					
//					if("YHTXX".equals(abnormalList.getEntCode()) || "UPSXK".equals(abnormalList.getEntCode())){
//						packingCnt = 0;
//						Order order = new Order();
//						order.setOdJobNo(orderInsert.getOdJobNo());
//						order.setHblNo(orderInsert.getHblNo().trim().replace("\t", ""));
//						if(Util.null2int(orderMapper.updatePackingOdJobNo(order)) > 0){
//							packingMap.put("odJobNo", order.getOdJobNo());
//							packingMap.put("hblNo", order.getHblNo());
//							packingCnt = orderMapper.selectPackingCount(packingMap);
//						}
//						if(packingCnt != orderInsert.getPieces()){
//							orderInsert.setPackingErrorYn("Y");
//						}else{
//							orderInsert.setPackingErrorYn("N");
//						}
//					}
					
					abnormalMapper.insertAbnormalOrder(orderInsert);
					abnormalMapper.insertAbnormalOrderAdditional(orderInsert);
					
					if("YHTXX".equals(abnormalList.getEntCode()) || "UPSXK".equals(abnormalList.getEntCode())){
						Order order = new Order();
						order.setOdJobNo(orderInsert.getOdJobNo());
						orderMapper.updatePackingOdJobNo(order);
						packingMap = new HashMap<String,Object>();
						packingMap.put("odJobNo", order.getOdJobNo());
						commonMapper.updatePackingErrorYn(packingMap);
					}
				}
				
				cnt = 1;	
				for(int i=0;i<dataList.size();i++) {
					Map<String, Object> ab = dataList.get(i);
					
					ab.put("registerUserId", abnormalList.getRegisterUserId());
					ab.put("gmtTime", abnormalList.getGmtTime());
					
					if("PANTK".equals(abnormalList.getEntCode())){
						abnormalMapperBatch.insertBmsInPlanExcelAbnormal(ab);
					}else{
						abnormalMapperBatch.insertExcelAbnormal(ab);
					}
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
		
		/* 정상인 경우에는 select 하지 않는다..2022-02-24.hyuk
		abnormalSaveResult.setOdList(abnormalMapper.selectOdList(abnormalList));
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
	
}