package org.yht.exps.express.service.eCommerce;

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
import org.yht.exps.express.mapper.eCommerce.HouseOrderMapper;
import org.yht.exps.express.mapper.eCommerce.MasterOrderMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.systemMng.SystemMngMapper;
import org.yht.exps.express.model.common.AddressInfo;
import org.yht.exps.express.model.common.search.SearchAddressInfo;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrder;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderCommon;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderCommonList;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderContainer;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderDelete;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderDimension;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderHts;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderLicense;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderOtherCharge;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderSaveResult;
import org.yht.exps.express.model.eCommerce.houseOrder.search.SearchHouseOrder;
import org.yht.exps.express.model.eCommerce.houseOrder.search.SearchHouseOrderList;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderContainer;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderDelete;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.OrderCommonService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class HouseOrderService {

	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name = "orderCommonService")
	OrderCommonService orderCommonService;
	
	
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	HouseOrderMapper houseOrderMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;

	
	/**
	 * 전자상거래 Order - insert/update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HouseOrderSaveResult saveHouseOrder(HouseOrder order, HttpServletRequest request) throws Exception {
		HouseOrderSaveResult result = new HouseOrderSaveResult();
		HouseOrder logOrder = order.clone();
		Util.accessControlDatabase(order, "dateFormat|gmtTime", false, null, null, null, null);

		boolean emsBlYn = false;
		String action = order.getAction();	/** insert/update구분 */
		String bound = order.getBound();
		String boundType = order.getBoundType();
		String description = null;
		String oldOrigCode = "", oldHblNo = "", oldDestCode = "";
		if(action.equals("update")){
			emsBlYn = order.getOblJobNo().endsWith("-A");
		}
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> exchangeRateMap = null;
		Map<String, Object> orderSaveMap = new HashMap<String, Object>();
		boolean hashouse = false;
		
		if(action.equals("update") && !emsBlYn){
			tmpMap.put("odJobNo", order.getOdJobNo());
			tmpMap.put("obDate", order.getObDate());
			tmpMap.put("arDate", order.getArDate());
			tmpMap.put("dateFormat", order.getDateFormat());
			tmpMap.put("selectObl", "Y");
			
			tmpMap = houseOrderMapper.selectHouseOrderChk(tmpMap);
			oldOrigCode = Util.null2str(tmpMap.get("ORIG_CODE"));
			oldHblNo = Util.null2str(tmpMap.get("HBL_NO"));
			oldDestCode = Util.null2str(tmpMap.get("DEST_CODE"));
			if(Util.null2int(tmpMap.get("HOUSE_CNT")) > 0){
				hashouse = true;
				if(!Util.null2str(tmpMap.get("CUST_CODE_LV1")).equals(Util.null2str(order.getCustCodeLv1()))){
					order.setCustCodeLv1(Util.null2str(tmpMap.get("CUST_CODE_LV1")));
				}
				if("EXP".equals(boundType) && !Util.null2str(tmpMap.get("OB_DATE")).equals(Util.null2str(tmpMap.get("INPUT_OB_DATE")))){
					exchangeRateMap = new HashMap<String, Object>();
					exchangeRateMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
					exchangeRateMap.put("srchDate", tmpMap.get("INPUT_OB_DATE"));
				}else if("IMP".equals(boundType) && !Util.null2str(tmpMap.get("AR_DATE")).equals(Util.null2str(tmpMap.get("INPUT_AR_DATE")))){
					exchangeRateMap = new HashMap<String, Object>();
					exchangeRateMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
					exchangeRateMap.put("srchDate", tmpMap.get("INPUT_AR_DATE"));
				}
			}
			if("1".equals(order.getRegisterUserLevel())){
				if("Y".equals(tmpMap.get("FINISH_YN"))){
					result.setResultCode("A11237");	/** 마감된 오더는 수정 할 수 없습니다. */
					return result;
				}
			}
		} else if(action.equals("update") && emsBlYn) {
			tmpMap.put("odJobNo", order.getOdJobNo());
			tmpMap.put("oblJobNo", order.getOblJobNo());
			tmpMap.put("obDate", order.getObDate());
			tmpMap.put("arDate", order.getArDate());
			tmpMap.put("dateFormat", order.getDateFormat());
			tmpMap.put("selectObl", "Y");

			tmpMap = houseOrderMapper.selectHouseOrderChkEms(tmpMap);
			oldOrigCode = Util.null2str(tmpMap.get("ORIG_CODE"));
			oldHblNo = Util.null2str(tmpMap.get("HBL_NO"));
			oldDestCode = Util.null2str(tmpMap.get("DEST_CODE"));
			if(Util.null2int(tmpMap.get("HOUSE_CNT")) > 0){
				hashouse = true;
				if("EXP".equals(boundType) && !Util.null2str(tmpMap.get("OB_DATE")).equals(Util.null2str(tmpMap.get("INPUT_OB_DATE")))){
					exchangeRateMap = new HashMap<String, Object>();
					exchangeRateMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
					exchangeRateMap.put("srchDate", tmpMap.get("INPUT_OB_DATE"));
				}else if("IMP".equals(boundType) && !Util.null2str(tmpMap.get("AR_DATE")).equals(Util.null2str(tmpMap.get("INPUT_AR_DATE")))){
					exchangeRateMap = new HashMap<String, Object>();
					exchangeRateMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
					exchangeRateMap.put("srchDate", tmpMap.get("INPUT_AR_DATE"));
				}
			}
			if("1".equals(order.getRegisterUserLevel())){
				if("Y".equals(tmpMap.get("FINISH_YN"))){
					result.setResultCode("A11237");	/** 마감된 오더는 수정 할 수 없습니다. */
					return result;
				}
			}
			tmpMap.clear();
			tmpMap.put("oblJobNo", order.getOblJobNo());
			tmpMap.put("origOblJobNo", order.getOblJobNo().replaceAll("-A",""));
			tmpMap.put("blTypeMh", "H");
			tmpMap.put("entCode", order.getEntCode());
			tmpMap.put("blNo", order.getHblNo());
			if(order.getOblJobNo().contains("SBL")){
				tmpMap.put("tableType","SBL");
				
			}else {
				tmpMap.put("tableType","OBL");
			}
			if(houseOrderMapper.selectChkDupBlNo(tmpMap) > 0){
				result.setResultCode("duplicateBlNo");
				return result;
			}
		}
		
		/** 비엘번호중복체크 */
		tmpMap.clear();
		tmpMap.put("odJobNo", order.getOdJobNo());
		tmpMap.put("blTypeMh", "H");
		tmpMap.put("entCode", order.getEntCode());
		tmpMap.put("blNo", order.getHblNo());
		if(commonMapper.selectChkDupBlNo(tmpMap) > 0){
			result.setResultCode("duplicateBlNo");
			return result;
		}
		
		ProcessLogs processLogs = new ProcessLogs();
		
		if(("EXP".equals(boundType) && !Util.isEmpty(order.getOrigCode()) && !order.getOrigCode().equals(oldOrigCode)) || action.equals("insert")){
			/** 관세청사서함 */
			tmpMap.clear();
			tmpMap.put("srchEntCode", order.getEntCode());
			tmpMap.put("srchEntBusinessCode", order.getEntBusinessCode());
			tmpMap.put("srchBound", bound);
			tmpMap.put("srchBoundType", boundType);
			if(boundType.equals("EXP")){
				tmpMap.put("srchEdiCityPort", order.getOrigCode());
			}else{
				tmpMap.put("srchEdiCityPort", order.getDestPort());
			}
			tmpMap = commonMapper.selectBusEdiEcmInfo(tmpMap);
			if(tmpMap != null && tmpMap.size() > 0){
				order.setEdiMailboxSeqNo(Util.null2str(tmpMap.get("SEQ_NO")));
			}else{
				tmpMap = new HashMap<String,Object>();
				if(action.equals("update")){
					order.setEdiMailboxSeqNo("null");
				}
			}
		}
		
		if(action.equals("insert")){
			tmpMap.clear();
			tmpMap.put("gmtTime", order.getGmtTime());
			tmpMap.put("entCode", order.getEntCode());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("updateUserId", order.getRegisterUserId());
			tmpMap.put("solution", "ECM");
			tmpMap.put("stockType", "OD");
			tmpMap.put("stockSign", "COD");
			tmpMap.put("stockSeqLength", 8);
			tmpMap.put("dateLength", null);
			tmpMap.put("signType", null);
			tmpMap.put("stockDateFormat", null);
			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", "ORDER-JOB NO");
			order.setOdJobNo(commonMapper.selectNextStockMaxNo(tmpMap));
			
			tmpMap.put("stockType", "CBL");
			tmpMap.put("stockSign", "CBL");
			tmpMap.put("stockRemark", "OBL-JOB NO");
			order.setOblJobNo(commonMapper.selectNextStockMaxNo(tmpMap));
			
			/** 화면에서 vender 제거해서 insert일때 사업장 기본값으로 저장 */
			tmpMap.put("entCode", order.getEntCode());
			tmpMap.put("entBusinessCode", order.getEntBusinessCode());
			tmpMap.put("solution", "ECM");
			if(bound.equals("AIR")){
				tmpMap.put("optionCode", "C02");
				Map<String, Object> map = commonMapper.selectSystemOption2(tmpMap);
				/** 항공일경우 중계사업자 설정값이 auto이면 편명의 중계사업자로 저장 */
				if("AUTO".equals(Util.null2str(map.get("SET_VALUE")).toUpperCase())){
					String venderId = "";
					if(!Util.isEmpty(order.getVesselFlight())){
						tmpMap.put("groupCode", "AIRLINE_CODE");
						tmpMap.put("srchEtcFieldOnly", "Y");
						tmpMap.put("srchEtcField", "ETC2");
						tmpMap.put("srchEtcFieldValue", order.getVesselFlight());
						tmpMap = commonMapper.getCodeDetail(tmpMap);
						if(tmpMap != null){
							if(boundType.equals("EXP")){
								venderId = Util.null2str(tmpMap.get("ETC10"));
							}else{
								venderId = Util.null2str(tmpMap.get("ETC11"));
							}
						}else {
							tmpMap = new HashMap<String, Object>();
						}
					}
					if(venderId.equals("")){
						order.setVenderId1(Util.null2str(map.get("DEFAULT_VALUE")));
					}else{
						order.setVenderId1(venderId);
					}
				}else if(Util.isEmpty(map.get("SET_VALUE"))){
					order.setVenderId1(Util.null2str(map.get("DEFAULT_VALUE")));
				}else{
					order.setVenderId1(Util.null2str(map.get("SET_VALUE")));
				}
			}else{
				tmpMap.put("optionCode", "C03");
				order.setVenderId1(commonMapper.selectSystemOption(tmpMap));
			}
			houseOrderMapper.insertHouseObl(order);
			houseOrderMapper.insertHouseOrder(order);
			houseOrderMapper.insertHouseOrderBus(order);
			
			
			if("AIR".equals(order.getBound())){
				houseOrderMapper.insertHouseOblRealDeliverer(order);
				order.setLineCode(order.getVesselFlight());
				order.setLineName(order.getEcmLineName());
				order.setVesselFlight(null);
			}
			
			houseOrderMapper.insertHouseOrderEcm(order);
			houseOrderMapper.insertHouseOrderAdditional(order);
			
			processLogs.setJobFuncType("ADD");
			description = "NEW HOUSE ORDER";
		}else if(action.equals("update")){
			houseOrderMapper.updateHouseObl(order);
			houseOrderMapper.updateHouseOrder(order);
			houseOrderMapper.updateHouseOrderBus(order);
			
			if(emsBlYn) {
				String origOdJobNo = Util.null2str(order.getOdJobNo()).replaceAll("-A", "");
				String origOblJobNo = Util.null2str(order.getOblJobNo()).replaceAll("-A", "");
				
				order.setOrigOdJobNo(origOdJobNo);
				order.setOrigOblJobNo(origOblJobNo);
				order.setEmsBl("Y");
				if(order.getOrigOblJobNo().contains("SBL")) {
					houseOrderMapper.updateSbl(order);
//					houseOrderMapper.updateSblLicense(order);
				}else {
					houseOrderMapper.updateHouseObl(order);
					houseOrderMapper.updateHouseOrder(order);
					houseOrderMapper.updateHouseOrderBus(order);
				}
			}
			/** 항공은 COM_OBL.LINE_CODE값과 ECM_OD.LINE_CODE값 다르게 저장함 */
			if("AIR".equals(order.getBound())){
				houseOrderMapper.updateHouseOblRealDeliverer(order);
				if(!oldHblNo.equals(order.getHblNo())){
					order.setLineCode(null);
					order.setLineName(null);
					if(order.getHblNo().length() > 2){
						tmpMap.clear();
						tmpMap.put("groupCode", "AIRLINE_CODE");
						tmpMap.put("entCode", order.getEntCode());
						tmpMap.put("srchCode", order.getHblNo().substring(0, 3));
						tmpMap = commonMapper.getCodeDetail(tmpMap);
						if(tmpMap!= null && tmpMap.size() > 0){
							order.setLineCode(Util.null2str(tmpMap.get("ETC2")));
							order.setLineName(Util.null2str(tmpMap.get("CODE_NAME")));
						}
					}
				}else{
					order.setLineCode("noUpdate");
				}
			}
			houseOrderMapper.updateHouseOrderEcm(order);
			houseOrderMapper.updateHouseOrderAdditional(order);
			
			processLogs.setJobFuncType("UPDATE");
			description = "EDIT HOUSE ORDER";
		}
		
		
		/** 서브테이블 */
		tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", order.getEntCode());
		tmpMap.put("odJobNo", order.getOdJobNo());
		tmpMap.put("oblJobNo", order.getOblJobNo());
		
		if(action.equals("update")){
			/** 서브테이블 delete */
			houseOrderMapper.deleteMark(tmpMap);
			if("AIR".equals(bound)){
				houseOrderMapper.deleteOtherCharge(tmpMap);
				houseOrderMapper.deleteDimension(tmpMap);
				houseOrderMapper.deleteLicense(tmpMap);
				if(order.getOblJobNo().contains("SBL") && emsBlYn) {
					tmpMap.put("origOblJobNo", order.getOrigOblJobNo());
					houseOrderMapper.deleteSblLicense(tmpMap);
				}
				houseOrderMapper.deleteHts(tmpMap);
			}else{
				houseOrderMapper.deleteContainer(tmpMap);
				houseOrderMapper.deleteCntrType(tmpMap);
				houseOrderMapper.deleteLicense(tmpMap);
			}
			/** POD테이블 비엘번호 업데이트 */
			tmpMap.put("hblNo", order.getHblNo());
			houseOrderMapper.updatePodBlNo(tmpMap);
		}
		
		/** 서브테이블 insert */
		tmpMap.put("registerUserId", order.getRegisterUserId());
		tmpMap.put("dateFormat", order.getDateFormat());
		tmpMap.put("gmtTime", order.getGmtTime());
		
		if("AIR".equals(bound)){
			/** Other Charge */
			List<HouseOrderOtherCharge> otherChargeList = order.getOtherChargeList();
			if(otherChargeList != null && otherChargeList.size() > 0) {
				tmpMap.put("otherChargeList", otherChargeList);
				houseOrderMapper.insertOtherCharge(tmpMap);
				tmpMap.remove("otherChargeList");
			}
			/** Dimensioin */
			List<HouseOrderDimension> dimensionList = order.getDimensionList();
			if(dimensionList != null && dimensionList.size() > 0) {
				tmpMap.put("dimensionList", dimensionList);
				houseOrderMapper.insertDimension(tmpMap);
				tmpMap.remove("dimensionList");
			}
			/** 면장정보 */
			List<HouseOrderLicense> licenseList = order.getLicenseList();
			if(licenseList != null && licenseList.size() > 0) {
				tmpMap.put("licenseList", licenseList);
				houseOrderMapper.insertLicense(tmpMap);
				if(order.getOblJobNo().contains("SBL") && emsBlYn) {
					tmpMap.put("origOblJobNo", order.getOrigOblJobNo());
					houseOrderMapper.insertSblLicense(tmpMap);
				}
				tmpMap.remove("licenseList");
			}
			/** HS Code */
			List<HouseOrderHts> htsList = order.getHtsList();
			if(htsList != null && htsList.size() > 0) {
				tmpMap.put("htsList", htsList);
				houseOrderMapper.insertHts(tmpMap);
				tmpMap.remove("htsList");
			}
		}else{
			/** Container */
			List<HouseOrderContainer> containerList = order.getContainerList();
			if(containerList != null && containerList.size() > 0) {
				tmpMap.put("containerList", containerList);
				houseOrderMapper.insertContainer(tmpMap);
				tmpMap.remove("containerList");
			}
			/** CntrType */
			List<HouseOrderContainer> cntrTypeList = order.getCntrTypeList();
			if(cntrTypeList != null && cntrTypeList.size() > 0) {
				tmpMap.put("cntrTypeList", cntrTypeList);
				houseOrderMapper.insertCntrType(tmpMap);
				tmpMap.remove("cntrTypeList");
			}
			/** 면장정보 */
			List<HouseOrderLicense> licenseList = order.getLicenseList();
			if(licenseList != null && licenseList.size() > 0) {
				tmpMap.put("licenseList", licenseList);
				houseOrderMapper.insertLicense(tmpMap);
				tmpMap.remove("licenseList");
			}
		}
		
		
		/** Description */
		List<Map<String, Object>> oblMarkArray = new ArrayList<Map<String, Object>>();
		String[] marksGoodsLArray = null;
		String[] marksGoodsRArray = null;
		
		if(!Util.isEmpty(order.getMarksGoodsLeft())){
			marksGoodsLArray = order.getMarksGoodsLeft().split("\r\n");
		}
		if(!Util.isEmpty(order.getMarksGoodsRight())){
			marksGoodsRArray = order.getMarksGoodsRight().split("\r\n");
		}
			
		if (marksGoodsLArray != null && marksGoodsLArray.length > 0) {
			for (int i = 0; i < marksGoodsLArray.length; i++) {
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("markTypeLr", "L");
				tmpMap.put("seqNo", i + 1);
				tmpMap.put("marksGoods", marksGoodsLArray[i]);
				
				oblMarkArray.add(tmpMap);
				tmpMap = null;
			}
		}
		
		if (marksGoodsRArray != null && marksGoodsRArray.length > 0) {
			for(int i = 0; i < marksGoodsRArray.length; i++) {
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("markTypeLr", "R");
				tmpMap.put("seqNo", i + 1);
				tmpMap.put("marksGoods", marksGoodsRArray[i]);
				
				oblMarkArray.add(tmpMap);
				tmpMap = null;
			}
		}
		
		if (oblMarkArray.size() > 0) {
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("oblJobNo", order.getOblJobNo());
			tmpMap.put("markList", oblMarkArray);
			houseOrderMapper.insertMark(tmpMap);
			tmpMap = null;
		}
		
		if(hashouse){
			if(tmpMap == null){
				tmpMap = new HashMap<String, Object>();
			}
			tmpMap.put("odJobNo", order.getOdJobNo());
			List<Order> houseList = houseOrderMapper.selectHouseOrderList(tmpMap);
			if(houseList != null && houseList.size() > 0){
				if(exchangeRateMap != null){
					exchangeRateMap.put("srchEntCode", order.getEntCode());
					exchangeRateMap.put("entBusinessCode", order.getEntBusinessCode());
					exchangeRateMap.put("srchDateFormat", "23");
					exchangeRateMap = commonMapper.selectExchangeRate(exchangeRateMap);
					
					orderSaveMap.put("exChangeYn", "Y");
					orderSaveMap.put("exrateMap", exchangeRateMap);
				}
				
				Order tmpOrder = new Order();
				tmpOrder.setMblNo(order.getMblNo());
				tmpOrder.setLineCode(order.getLineCode());
				tmpOrder.setLineName(order.getLineName());
				tmpOrder.setVesselVoyage(order.getVesselVoyage());
				if("AIR".equals(order.getBound())){
					tmpOrder.setVesselFlight(order.getViaByA());
				}else{
					tmpOrder.setVesselFlight(order.getVesselFlight());
				}
				tmpOrder.setObDate(order.getObDate());
				tmpOrder.setDestCode(order.getDestCode());
				tmpOrder.setDestName(order.getDestName());
				tmpOrder.setArDate(order.getArDate());
				tmpOrder.setDestCountry(order.getDestCountry());
				tmpOrder.setConsigneeCountryCode(order.getConsigneeCountryCode());
				tmpOrder.setOrigCode(order.getOrigCode());
				tmpOrder.setOrigName(order.getOrigName());
				tmpOrder.setOrigCountry(order.getOrigCountry());
				tmpOrder.setShipperCountryCode(order.getShipperCountryCode());
				tmpOrder.setUpdateUserId(order.getUpdateUserId());
				tmpOrder.setGmtTime(order.getGmtTime());
				tmpOrder.setBound(bound);
				tmpOrder.setBoundType(boundType);
				
				/** 항공수출일경우 DEST_CODE값에 따라 하우스의 송하인/수하인 팩스번호 변경 */
				if("AIR".equals(order.getBound()) && "EXP".equals(order.getBoundType()) && !oldDestCode.equals(order.getDestCode())){
					if(!Util.isEmpty(order.getDestCode())){
						tmpMap.clear();
						tmpMap.put("groupCode", "CITY_PORT");
						tmpMap.put("entCode", order.getEntCode());
						tmpMap.put("srchCode", order.getDestCode());
						tmpMap = commonMapper.getCodeDetail(tmpMap);
						if(tmpMap != null && tmpMap.size() > 0){
							if("CN".equals(tmpMap.get("ETC2"))){
								tmpOrder.setShipperFax("VAT NUMBER");
								tmpOrder.setConsigneeFax("USCI");
							}else if("ID".equals(tmpMap.get("ETC2"))){
								tmpOrder.setConsigneeFax("TIN");
							}else if("210".equals(tmpMap.get("ETC3"))){
								tmpOrder.setConsigneeFax("T");
							}
						}
					}
				}else{
					tmpOrder.setShipperFax("noUpdate");
				}
				
				SqlSession sqlSession = null;
				try {
					sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
					MasterOrderMapper masterOrderBatchMapper = sqlSession.getMapper(MasterOrderMapper.class);
					int cnt = 1;
					for(Order houseOrder : houseList){
						tmpOrder.setOdJobNo(houseOrder.getOdJobNo());
						if(exchangeRateMap != null){
							tmpOrder.setCurrencyCode(houseOrder.getCurrencyCode());
							tmpOrder.setAmountCur(houseOrder.getAmountCur());
							orderCommonService.orderSave(tmpOrder, "saveMaster", orderSaveMap, 0);
						}
						masterOrderBatchMapper.updateHouseOrderBatch(tmpOrder);
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
		}
		
		// 처리 로그 기록
		processLogs.setEntCode(order.getEntCode());
		processLogs.setEntBusinessCode(order.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(bound);
		processLogs.setBoundType(boundType);
		processLogs.setProcessUserId(order.getUpdateUserId());
		processLogs.setJobNo(order.getOblJobNo());
		processLogs.setLogJobType("DATA");
		processLogs.setDescription(description +"[HBL_NO : " + order.getHblNo()+"]");
		processLogs.setJobLog(new Gson().toJson(logOrder));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(order.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(order.getLogUserName());
		processLogs.setProcessUserNameEng(order.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		result.setOdJobNo(order.getOdJobNo());
		result.setOblJobNo(order.getOblJobNo());
		return result;
	}
	
	/**
	 * 전자상거래 Order 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public HouseOrder selectHouseOrder(SearchHouseOrder search) throws Exception {
		if(search.getSrchOdJobNo().endsWith("-A")) search.setSrchEmsBlYn("Y");

		HouseOrder order = houseOrderMapper.selectHouseOrder(search);
		if(order != null){
			if("AIR".equals(order.getBound())){
				order.setOtherChargeList(houseOrderMapper.selectOtherCharge(search));
				order.setDimensionList(houseOrderMapper.selectDimension(search));
				order.setLicenseList(houseOrderMapper.selectLicense(search));
				order.setHtsList(houseOrderMapper.selectHts(search));
			}else{
				order.setContainerList(houseOrderMapper.selectContainer(search));
				order.setLicenseList(houseOrderMapper.selectLicense(search));
				order.setCntrTypeList(houseOrderMapper.selectCntrType(search));
			}
		}
		return order;
	}
	
	/**
	 * MasterOrder List(공통)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HouseOrderCommonList selectHouseOrderListCommon(SearchHouseOrderList search) throws Exception {
		HouseOrderCommonList orderCommonList = new HouseOrderCommonList();
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
		String emsBlYn = search.getSrchEmsBlYn();
		if(emsBlYn.equals("Y")) {
			Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "noDateChk|obl.OB_DATE|obl.AR_DATE|cod.ORDER_DATE", "srchNoType", "obl.HBL_NO|cod.HBL_NO");
		} else {
			Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "noDateChk|od.OB_DATE|od.AR_DATE|od.PO_DATE", "srchNoType", "obl.HBL_NO|od.HBL_NO");
		}
		List<HouseOrderCommon> list  = houseOrderMapper.selectHouseOrderListCommon(search);
		orderCommonList.setOrderCommonList(list);
		if(list.size() > 0) {
			orderCommonList.setTotal(list.get(0).getTotal());
		}else {
			orderCommonList.setTotal(0);
		}
		return orderCommonList;
	}
	
	/**
	 * 전자상거래 Order - insert/update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HouseOrderSaveResult insertHouseComOrder(HouseOrder order, HttpServletRequest request) throws Exception {
		HouseOrderSaveResult result = new HouseOrderSaveResult();
		Util.accessControlDatabase(order, "gmtTime|dateFormat", false, null, null, null, null);
		
		Map<String, Object> tmpMap = houseOrderMapper.selectUserInfo(order);
		tmpMap.put("entBusinessCode", "");
		tmpMap.put("gmtTime", order.getGmtTime());
		tmpMap.put("entCode", order.getEntCode());
		tmpMap.put("updateUserId", order.getRegisterUserId());
		tmpMap.put("solution", "ECM");
		tmpMap.put("stockSeqLength", 8);
		tmpMap.put("dateLength", null);
		tmpMap.put("signType", null);
		tmpMap.put("stockDateFormat", null);
		tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		tmpMap.put("stockType", "CBL");
		tmpMap.put("stockSign", "CBL");
		tmpMap.put("stockRemark", "OBL-JOB NO");
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(order.getEntCode());
		processLogs.setEntBusinessCode(order.getRegisterUserId());
		processLogs.setSolution("ECM");
		processLogs.setBound(order.getBound());
		processLogs.setBoundType(order.getBoundType());
		processLogs.setProcessUserId(order.getRegisterUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("ADD");
		processLogs.setJobLog("");
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(order.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(order.getLogUserName());
		processLogs.setProcessUserNameEng(order.getLogUserNameEng());
		
		List<HouseOrderContainer> cntrTypeList;
		List<HouseOrderContainer> containerList;
		String errorStr = "";
		String odJobNo = null;
		String oblJobNo = null;
		List<HouseOrder> insertList = houseOrderMapper.selectEcmOrderList(order);
		Map<String, Object> remarks;
		Map<String, Object> odJobNoMap;
		List<Map<String, Object>> markList;
		List<Map<String, Object>> odJobNoList = new ArrayList<Map<String, Object>>();
		for(HouseOrder data : insertList){
			if(Util.isEmpty(data.getOblJobNo())){
				this.setDefaultData(data, tmpMap);
				if(odJobNo == null){
					odJobNo = data.getOdJobNo();
					oblJobNo = data.getOblJobNo();
				}
				if(!Util.isEmpty(data.getMasterOdJobNo())) {
					HouseOrder masterData = houseOrderMapper.selectSrNo(data);
					if(masterData != null) {
						data.setMblJobNo(masterData.getMblJobNo());
						data.setSrNo(masterData.getSrNo());
					}
				}
				houseOrderMapper.insertHouseObl(data);
				houseOrderMapper.insertHouseOrder(data);
				houseOrderMapper.insertHouseOrderBus(data);
				if("SEA".equals(order.getBound())){
					tmpMap.put("oblJobNo", data.getOblJobNo());
					
					remarks = new HashMap<String, Object>();
					remarks.put("markTypeLr", "R");
					remarks.put("seqNo", 1);
					remarks.put("marksGoods", data.getMarksGoodsRight());
					markList = new ArrayList<Map<String, Object>>();
					markList.add(remarks);
					tmpMap.put("markList", markList);
					houseOrderMapper.insertMark(tmpMap);
					tmpMap.remove("markList");
					
					cntrTypeList = data.getCntrTypeList();
					if(cntrTypeList != null && cntrTypeList.size() > 0) {
						tmpMap.put("cntrTypeList", cntrTypeList);
						houseOrderMapper.insertCntrType(tmpMap);
						tmpMap.remove("cntrTypeList");
					}
					containerList = data.getContainerList();
					if(containerList != null && containerList.size() > 0) {
						tmpMap.put("containerList", containerList);
						houseOrderMapper.insertContainer(tmpMap);
						tmpMap.remove("containerList");
					}
					tmpMap.remove("oblJobNo");
				}else{
					houseOrderMapper.insertHouseOblRealDeliverer(data);
				}
				
				processLogs.setDescription("NEW HOUSE COM ORDER[HBL_NO : " + data.getHblNo() + "]");
				processLogs.setJobNo(data.getOblJobNo());
				commonService.insertLog(processLogs, request);
			}else{
				odJobNoMap = new HashMap<String, Object>();
				if(data.getOblJobNo().endsWith("-A")) {
					odJobNoMap.put("odJobNo", data.getOrigOdJobNo());
				}else {
					odJobNoMap.put("odJobNo", data.getOdJobNo());
				}
				odJobNoMap.put("oblJobNo", data.getOblJobNo());
				odJobNoList.add(odJobNoMap);
				errorStr += ", " + data.getHblNo();
			}
		}
		
		tmpMap.clear();
		if(errorStr.length() > 0){
			tmpMap.put("odJobNoList", odJobNoList);
			result.setResultData(tmpMap);
			result.setResultCode(errorStr.substring(2));
		}else {
			if(odJobNo != null){
				tmpMap.put("odJobNo", odJobNo);
				tmpMap.put("oblJobNo", oblJobNo);
				result.setResultData(tmpMap);
			}
		}
		
		return result;
	}
	public void setDefaultData(HouseOrder order, Map<String, Object> tmpMap) throws Exception {
		order.setOblJobNo(commonMapper.selectNextStockMaxNo(tmpMap));
		order.setBlTypeSc("CS");
		order.setDvCarriage("N.V.D");
		order.setDvInsurance("NIL");
		order.setGmtTime(Util.null2str(tmpMap.get("gmtTime")));
		order.setDateFormat("23");
		order.setRegisterUserId(Util.null2str(tmpMap.get("updateUserId")));
		
		/** 하우스의 PIECES/GWT/CBM 저장 */
		tmpMap.put("odJobNo", order.getOdJobNo());
		Map<String, Object> map = houseOrderMapper.selectHouseDetail(order);
		if(map != null && map.size() > 0){
			order.setPieces(Util.null2int(map.get("PIECES")));
			order.setGwt(Util.null2dbl(map.get("GWT")));
			order.setCwtCbm(Util.null2dbl(map.get("GWT")));
		}else{
			order.setPieces(0);
			order.setGwt(0);
			order.setCwtCbm(0);
		}
		
		HouseOrder tmpOrder = new HouseOrder();
		tmpOrder.setOdJobNo(order.getOdJobNo());
		tmpOrder.setAction("selectCustLv1");
		map = houseOrderMapper.selectUserInfo(tmpOrder);
		
		/** 마스터정보 */
		Map<String, Object> masterMap = houseOrderMapper.selectMasterInfo(order);
		
		/** 26.08.06 요청 
		 1) H.B/L 생성시 송/수하인정보 적하목록 하우스의 송/수하인 정보 기준 생성
   			- 우편번호, 주코드, 국가코드는 AMS 용으로 생성
   			- 송/수하인 이름 및 주소는 적하용 이름/주소로 생성
		 */
		/** 사업장 거래처 정보 */
		String busCustomerCode = Util.null2str(tmpMap.get("CUSTOMER_CODE"));
		String addressEng = "";
		String tmpStr;
		if(!busCustomerCode.equals("")){
			addressEng = Util.setAddress(Util.null2str(tmpMap.get("ADDRESS_ENG")));
			tmpStr = Util.null2str(tmpMap.get("ADDRESS_A_ENG")).trim();
			if(!tmpStr.equals(""))	addressEng += "\n" + Util.setAddress(tmpStr);
			
			tmpStr = Util.null2str(tmpMap.get("ADDRESS_B_ENG")).trim();
			if(!tmpStr.equals(""))	addressEng += "\n" + Util.setAddress(tmpStr);
			
			tmpStr = Util.null2str(tmpMap.get("ATTN_ENG")).trim();
			if(!tmpStr.equals(""))	addressEng += "\nATTN: " + Util.setAddress(tmpStr);
			
			tmpStr = Util.null2str(tmpMap.get("TEL_ENG")).trim();
			if(!tmpStr.equals("")){
				addressEng += "\nTEL: " + Util.setAddress(tmpStr);
				tmpStr = Util.null2str(tmpMap.get("FAX_ENG")).trim();
				if(!tmpStr.equals(""))	addressEng += "   FAX: " + Util.setAddress(tmpStr);
			}else{
				tmpStr = Util.null2str(tmpMap.get("FAX_ENG")).trim();
				if(!tmpStr.equals(""))	addressEng += "\nFAX: " + Util.setAddress(tmpStr);
			}
		}
		
		/** 최상위업체 거래처 정보 */
		String lv1CustomerCode = "";
		if(map != null && map.size() > 0){
			lv1CustomerCode = Util.null2str(map.get("CUSTOMER_CODE"));
		}
		String lv1AddressEng = "";
		if(!lv1CustomerCode.equals("")){
			lv1AddressEng = Util.setAddress(Util.null2str(map.get("ADDRESS_ENG")));
			tmpStr = Util.null2str(map.get("ADDRESS_A_ENG")).trim();
			if(!tmpStr.equals(""))	lv1AddressEng += "\n" + Util.setAddress(tmpStr);
			
			tmpStr = Util.null2str(map.get("ADDRESS_B_ENG")).trim();
			if(!tmpStr.equals(""))	lv1AddressEng += "\n" + Util.setAddress(tmpStr);
			
			tmpStr = Util.null2str(map.get("ATTN_ENG")).trim();
			if(!tmpStr.equals(""))	lv1AddressEng += "\nATTN: " + Util.setAddress(tmpStr);
			
			tmpStr = Util.null2str(map.get("TEL_ENG")).trim();
			if(!tmpStr.equals("")){
				lv1AddressEng += "\nTEL: " + Util.setAddress(tmpStr);
				tmpStr = Util.null2str(map.get("FAX_ENG")).trim();
				if(!tmpStr.equals(""))	lv1AddressEng += "   FAX: " + Util.setAddress(tmpStr);
			}else{
				tmpStr = Util.null2str(map.get("FAX_ENG")).trim();
				if(!tmpStr.equals(""))	lv1AddressEng += "\nFAX: " + Util.setAddress(tmpStr);
			}
		}
		
		if("AIR".equals(order.getBound())){
			/** 2024-11-15 ECM_OD편명과 COM_OBL편명은 다른값이 저장되기때문에 주석처리함 */
			order.setIssuePlaceCode(order.getOrigCode());
			order.setIssueDate(order.getObDate());
			order.setSignAttn(Util.null2str(tmpMap.get("USER_NAME_ENG")));
			
			if(!busCustomerCode.equals("")){
				order.setIssuingCarriers(Util.null2str(tmpMap.get("CUSTOMER_NAME_ENG")));
			}
			
			if("EXP".equals(order.getBoundType())){
				order.setHandlingSpecial("EAW");
				order.setHandlingSpecialName("NOT RESTRICTED");
				order.setEdiCommodityItem("CONSOLIDATION");
				order.setViaToA(order.getDestCode());
				order.setViaByA(Util.null2str(masterMap.get("LINE_CODE")) + Util.null2str(masterMap.get("VESSEL_FLIGHT")));
				if(!busCustomerCode.equals("")){
//					order.setShipperCode(busCustomerCode);
//					order.setShipperName(Util.null2str(tmpMap.get("CUSTOMER_NAME_ENG")));
					order.setSignHisAgent(order.getShipperName());
//					order.setShipperAddress(addressEng);
//					order.setShipperZipcode(Util.null2str(tmpMap.get("AMS_ZIPCODE")));
//					order.setShipperStateCode(Util.null2str(tmpMap.get("AMS_STATE_CODE")));
//					order.setShipperCountryCode(Util.null2str(tmpMap.get("AMS_COUNTRY_CODE")));
//					order.setShipperBusinessId(Util.null2str(tmpMap.get("BUSINESS_ID")));
//					order.setShipperTel(Util.null2str(tmpMap.get("TEL_ENG")));
//					order.setShipperAttn(Util.null2str(tmpMap.get("ATTN_ENG")));
//					order.setShipperFax(Util.null2str(tmpMap.get("FAX_ENG")));
//					order.setShipperPlace(Util.null2str(tmpMap.get("AMS_PLACE")));
//					order.setShipperStreet(Util.null2str(tmpMap.get("AMS_STREET")));
//					order.setIataCode(Util.null2str(tmpMap.get("IATA_CODE")));
					
//					if(Util.isEmpty(tmpMap.get("AMS_STREET")) && Util.isEmpty(tmpMap.get("AMS_COUNTRY_CODE")) && Util.isEmpty(tmpMap.get("AMS_STATE_CODE"))){
//						SearchAddressInfo search = new SearchAddressInfo();
//						search.setSrchAddress(addressEng);
//						AddressInfo addressInfo = commonService.getAddressInfo(search);
//						order.setShipperZipcode(addressInfo.getZipcode());
//						order.setShipperStateCode(addressInfo.getStateCode());
//						order.setShipperCountryCode(addressInfo.getCountryCode());
//						order.setShipperTel(addressInfo.getTel());
//						order.setShipperAttn(addressInfo.getAttn());
//						order.setShipperFax(addressInfo.getFax());
//						order.setShipperPlace(addressInfo.getPlace());
//						order.setShipperStreet(addressInfo.getStreet());
//					}
				}
				order.setAccountInfo("\"FREIGHT PREPAID\"");
			}else{
				if(!busCustomerCode.equals("")){
					order.setConsigneeCode(busCustomerCode);
					order.setConsigneeName(Util.null2str(tmpMap.get("CUSTOMER_NAME_ENG")));
					order.setSignHisAgent(order.getConsigneeName());
					order.setConsigneeAddress(addressEng);
					order.setConsigneeZipcode(Util.null2str(tmpMap.get("AMS_ZIPCODE")));
					order.setConsigneeStateCode(Util.null2str(tmpMap.get("AMS_STATE_CODE")));
					order.setConsigneeCountryCode(Util.null2str(tmpMap.get("AMS_COUNTRY_CODE")));
					order.setConsigneeBusinessId(Util.null2str(tmpMap.get("BUSINESS_ID")));
					order.setConsigneeTel(Util.null2str(tmpMap.get("TEL_ENG")));
					order.setConsigneeAttn(Util.null2str(tmpMap.get("ATTN_ENG")));
					order.setConsigneeFax(Util.null2str(tmpMap.get("FAX_ENG")));
					order.setConsigneePlace(Util.null2str(tmpMap.get("AMS_PLACE")));
					order.setConsigneeStreet(Util.null2str(tmpMap.get("AMS_STREET")));
					
					if(Util.isEmpty(tmpMap.get("AMS_STREET")) && Util.isEmpty(tmpMap.get("AMS_COUNTRY_CODE")) && Util.isEmpty(tmpMap.get("AMS_STATE_CODE"))){
						SearchAddressInfo search = new SearchAddressInfo();
						search.setSrchAddress(addressEng);
						AddressInfo addressInfo = commonService.getAddressInfo(search);
						order.setConsigneeZipcode(addressInfo.getZipcode());
						order.setConsigneeStateCode(addressInfo.getStateCode());
						order.setConsigneeCountryCode(addressInfo.getCountryCode());
						order.setConsigneeTel(addressInfo.getTel());
						order.setConsigneeAttn(addressInfo.getAttn());
						order.setConsigneeFax(addressInfo.getFax());
						order.setConsigneePlace(addressInfo.getPlace());
						order.setConsigneeStreet(addressInfo.getStreet());
					}
				}
				
				if(!lv1CustomerCode.equals("")){
					order.setShipperCode(lv1CustomerCode);
					order.setShipperName(Util.null2str(map.get("CUSTOMER_NAME_ENG")));
					order.setShipperAddress(lv1AddressEng);
					order.setShipperZipcode(Util.null2str(map.get("AMS_ZIPCODE")));
					order.setShipperStateCode(Util.null2str(map.get("AMS_STATE_CODE")));
					order.setShipperCountryCode(Util.null2str(map.get("AMS_COUNTRY_CODE")));
					order.setShipperBusinessId(Util.null2str(map.get("BUSINESS_ID")));
					order.setShipperTel(Util.null2str(map.get("TEL_ENG")));
					order.setShipperAttn(Util.null2str(map.get("ATTN_ENG")));
					order.setShipperFax(Util.null2str(map.get("FAX_ENG")));
					order.setShipperPlace(Util.null2str(map.get("AMS_PLACE")));
					order.setShipperStreet(Util.null2str(map.get("AMS_STREET")));
					
					if(Util.isEmpty(map.get("AMS_STREET")) && Util.isEmpty(map.get("AMS_COUNTRY_CODE")) && Util.isEmpty(map.get("AMS_STATE_CODE"))){
						SearchAddressInfo search = new SearchAddressInfo();
						search.setSrchAddress(lv1AddressEng);
						AddressInfo addressInfo = commonService.getAddressInfo(search);
						order.setShipperZipcode(addressInfo.getZipcode());
						order.setShipperStateCode(addressInfo.getStateCode());
						order.setShipperCountryCode(addressInfo.getCountryCode());
						order.setShipperTel(addressInfo.getTel());
						order.setShipperAttn(addressInfo.getAttn());
						order.setShipperFax(addressInfo.getFax());
						order.setShipperPlace(addressInfo.getPlace());
						order.setShipperStreet(addressInfo.getStreet());
					}
				}
				order.setAccountInfo("\"FREIGHT COLLECT\"");
			}
			
			order.setLineCode(null);
			order.setLineName(null);
			if(order.getHblNo().length() > 2){
				Map<String, Object> lineCode = new HashMap<String, Object>();
				lineCode.put("entCode", order.getEntCode());
				lineCode.put("customerCode", order.getHblNo().substring(0, 3));
				lineCode = commonMapper.selectCustomerInfo(lineCode);
				if(lineCode!= null && lineCode.size() > 0){
					order.setLineCode(Util.null2str(lineCode.get("CUSTOMER_CODE")));
					order.setLineName(Util.null2str(lineCode.get("CUSTOMER_NAME_ENG")));
					order.setSignActingAs("AS AGENT FOR THE CARRIER : " + order.getLineName());
					if(!"1".equals(tmpMap.get("SIGN_HIS_AGENT_TYPE"))){
						order.setSignHisAgent(order.getSignActingAs());
					}
					
					order.setFwrdCode(order.getLineCode());
					order.setFwrdName(order.getLineName());
					
					addressEng = Util.setAddress(Util.null2str(lineCode.get("ADDRESS_ENG")));
					tmpStr = Util.null2str(lineCode.get("ADDRESS_A_ENG")).trim();
					if(!tmpStr.equals(""))	addressEng += "\n" + Util.setAddress(tmpStr);
					
					tmpStr = Util.null2str(lineCode.get("ADDRESS_B_ENG")).trim();
					if(!tmpStr.equals(""))	addressEng += "\n" + Util.setAddress(tmpStr);
					
					tmpStr = Util.null2str(lineCode.get("ATTN_ENG")).trim();
					if(!tmpStr.equals(""))	addressEng += "\nATTN: " + Util.setAddress(tmpStr);
					
					tmpStr = Util.null2str(lineCode.get("TEL_ENG")).trim();
					if(!tmpStr.equals("")){
						addressEng += "\nTEL: " + Util.setAddress(tmpStr);
						tmpStr = Util.null2str(lineCode.get("FAX_ENG")).trim();
						if(!tmpStr.equals(""))	addressEng += "   FAX: " + Util.setAddress(tmpStr);
					}else{
						tmpStr = Util.null2str(lineCode.get("FAX_ENG")).trim();
						if(!tmpStr.equals(""))	addressEng += "\nFAX: " + Util.setAddress(tmpStr);
					}
					
					order.setFwrdAddress(addressEng);
				}
			}
		}else{
			order.setOriginalBlYn("N");
			order.setUnitPieces("CT");
			order.setNoOfOriginal("ZERO / 0");
			order.setBlTypeLfb("F");
			order.setBlCargoCyCfs("CY/CY");
			order.setReceiptCode(order.getOrigCode());
			order.setDiscCode(order.getDestCode());
			order.setDeliCode(order.getDestCode());
			
			if(!busCustomerCode.equals("")){
				if("EXP".equals(order.getBoundType())){
//					order.setShipperCode(busCustomerCode);
//					order.setShipperName(Util.null2str(tmpMap.get("CUSTOMER_NAME_ENG")));
//					order.setShipperAddress(addressEng);
				}else{
					order.setConsigneeCode(busCustomerCode);
					order.setConsigneeName(Util.null2str(tmpMap.get("CUSTOMER_NAME_ENG")));
					order.setConsigneeAddress(addressEng);
				}
			}
			
			if(!lv1CustomerCode.equals("")){
				if("EXP".equals(order.getBoundType())){
				}else{
					order.setShipperCode(lv1CustomerCode);
					order.setShipperName(Util.null2str(map.get("CUSTOMER_NAME_ENG")));
					order.setShipperAddress(lv1AddressEng);
				}
			}
			
			/** 하우스의 컨테이너 조회해서 마스터에 저장 */
			List<HouseOrderContainer> cntrTypeList = new ArrayList<HouseOrderContainer>();
			HouseOrderContainer houseOrderContainer;
			List<Map<String, Object>> cntrList = houseOrderMapper.selectHouseContainerList(order);
			String cntr20Type = null, cntr40Type = null;
			int cntr20Pieces = 0, cntr40Pieces = 0;
			if(cntrList != null && cntrList.size() > 0){
				String cntrCode;
				List<HouseOrderContainer> containerList = new ArrayList<HouseOrderContainer>();
				HouseOrderContainer container;
				for(Map<String, Object> cntr : cntrList){
					cntrCode = Util.null2str(cntr.get("CNTR_CODE"));
					if(cntrCode.startsWith("2")){
						if(cntr20Type != null && !cntrCode.equals(cntr20Type)){
							cntr20Type = "20DR";	/** 컨테이너타입이 한가지가 아닐경우 DR로 저장 */
						}else{
							cntr20Type = cntrCode;
						}
						cntr20Pieces++;
					}else{
						if(cntr40Type != null && !cntrCode.equals(cntr40Type)){
							cntr40Type = "40DR";
						}else{
							cntr40Type = cntrCode;
						}
						cntr40Pieces++;
					}
					/** 하우스의 컨테이너번호 종류만큼 마스터 컨테이너 저장 */
					container = new HouseOrderContainer();
					container.setCntrNo(Util.null2str(cntr.get("CNTR_NO")));
					container.setCntrCode(Util.null2str(cntr.get("CNTR_CODE")));
					container.setCntrEdiCode(Util.null2str(cntr.get("CNTR_EDI_CODE")));
					container.setCntrSealNo1(Util.null2str(cntr.get("CNTR_SEAL_NO_1")));
					container.setUnitPieces("CT");
					container.setUnitWeight("KG");
					container.setCntrOwnType("2");
					container.setCntrLoadTypeEl("L");
					containerList.add(container);
				}
				/** 컨테이너타입이 22GP 등  20/40이 아닐경우 DR로 저장 */
				if(cntr20Type == null || !cntr20Type.startsWith("20")){
					cntr20Type = "20DR";
				}
				if(cntr40Type == null || !cntr40Type.startsWith("40")){
					cntr40Type = "40DR";
				}
				order.setContainerList(containerList);
			}else{
				cntr20Type = "20DR";
				cntr40Type = "40DR";
			}
			houseOrderContainer = new HouseOrderContainer();
			houseOrderContainer.setCntrCode(cntr20Type);
			houseOrderContainer.setPieces(cntr20Pieces);
			cntrTypeList.add(houseOrderContainer);
			
			houseOrderContainer = new HouseOrderContainer();
			houseOrderContainer.setCntrCode(cntr40Type);
			houseOrderContainer.setPieces(cntr40Pieces);
			cntrTypeList.add(houseOrderContainer);
			order.setCntrTypeList(cntrTypeList);
			
			if(cntr20Type.length() == 4){cntr20Type = cntr20Type.substring(2);}
			if(cntr40Type.length() == 4){cntr40Type = cntr40Type.substring(2);}
			
			String blSay = "";
			String cntrQtyEng;
			if(cntr20Pieces > 0){
				cntrQtyEng = Util.numberToWords(cntr20Pieces);
				blSay = cntrQtyEng + "(" + cntr20Pieces + ") X ";
				if (cntr20Type.equals("DR"))	{
					blSay += "20'";
				} else if (cntr20Type.equals("HT") || cntr20Type.equals("PT")) {
					blSay = "PART OF " + blSay + "20'";
				} else if (cntr20Type.equals("HP")) {
					blSay = "HC PART OF " + blSay + "20'";
				} else {
					blSay += "20'" + cntr20Type;
				}
				blSay += ", ";
			}
			if(cntr40Pieces > 0){
				cntrQtyEng = Util.numberToWords(cntr40Pieces);
				blSay += cntrQtyEng + "(" + cntr40Pieces + ") X ";
				if (cntr40Type.equals("DR"))	{
					blSay += "40'";
				} else if (cntr40Type.equals("HT") || cntr40Type.equals("PT")) {
					blSay = "PART OF " + blSay + "40'";
				} else if (cntr40Type.equals("HP")) {
					blSay = "HC PART OF " + blSay + "40'";
				} else {
					blSay += "40'" + cntr40Type;
				}
			}
			if(blSay.length() > 0){
				blSay += " CONTAINER ONLY";
			}
			if(blSay.length() > 150){
				blSay = blSay.substring(0 , 150);
			}
			order.setBlSay(blSay);
			
			
			if("EXP".equals(order.getBoundType())){
				order.setMarksGoodsRight("\"FREIGHT PREPAID\"");
			}else{
				order.setMarksGoodsRight("\"FREIGHT COLLECT\"");
			}
		}
		
		if("EXP".equals(order.getBoundType())){
			order.setCurrencyCode(Util.null2str(tmpMap.get("CURRENCY_CODE")));
			order.setFreightTerm("PP");
			order.setOtherTerm("PP");
		}else{
			order.setFreightTerm("CC");
			order.setOtherTerm("CC");
			if("SEA".equals(order.getBound())){
				order.setBlPayableAt("DESTINATION");
			}
		}
		
	}
	
	/**
	 * 전자상거래 OrderList - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HouseOrderSaveResult deleteHouseOrder(HouseOrderDelete orderDelete, HttpServletRequest request) throws Exception {
		HouseOrderSaveResult result = new HouseOrderSaveResult();
		String hblNo = "";
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("oblJobNo", orderDelete.getOblJobNo());
		tmpMap.put("checkInvoiceCnt",  "Y");
		tmpMap = houseOrderMapper.selectHouseOrderChk(tmpMap);
		hblNo = Util.null2str(tmpMap.get("HBL_NO"));
		if(Util.null2int(tmpMap.get("INVOICE_CNT")) > 0){
			result.setResultCode("A11176");	/** 정산서가 발행된 건이 있어 변경할 수 없습니다. */
			return result;
		}
		
		if("check".equals(orderDelete.getAction())){
			return result;
		}
		
		tmpMap.clear();
		tmpMap.put("oblJobNo", orderDelete.getOblJobNo());
		tmpMap.put("odJobNo", orderDelete.getOdJobNo());
		
		houseOrderMapper.deleteHouseObl(tmpMap);
		houseOrderMapper.deleteHouseOrder(tmpMap);
		houseOrderMapper.deleteHouseOrderBus(tmpMap);
		houseOrderMapper.deleteMark(tmpMap);
		if("AIR".equals(orderDelete.getBound())){
			houseOrderMapper.deleteHouseOblRealDeliverer(tmpMap);
			houseOrderMapper.deleteOtherCharge(tmpMap);
			houseOrderMapper.deleteDimension(tmpMap);
			houseOrderMapper.deleteLicense(tmpMap);
			houseOrderMapper.deleteHts(tmpMap);
		}else{
			houseOrderMapper.deleteContainer(tmpMap);
			houseOrderMapper.deleteCntrType(tmpMap);
		}
		
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(orderDelete.getEntCode());
		processLogs.setEntBusinessCode(orderDelete.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(orderDelete.getBound());
		processLogs.setBoundType(orderDelete.getBoundType());
		processLogs.setProcessUserId(orderDelete.getUpdateUserId());
		processLogs.setJobNo(orderDelete.getOblJobNo());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("DELETE");
		processLogs.setDescription("DELETE MASTER COM ORDER[HBL_NO : " + hblNo + "]");
		processLogs.setJobLog(new Gson().toJson(orderDelete));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(orderDelete.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(orderDelete.getLogUserName());
		processLogs.setProcessUserNameEng(orderDelete.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return result;
	}
	
	/**
	 * 전자상거래 OrderList - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HouseOrderSaveResult deleteHouseHouseOrder(HouseOrderDelete houseDelete, HttpServletRequest request) throws Exception {
		HouseOrderSaveResult result = new HouseOrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			SystemMngMapper systemMngBatchMapper = sqlSession.getMapper(SystemMngMapper.class);
			
			/** 콘솔체크 */
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("checkInvoiceCnt",  "Y");
			tmpMap.put("odJobNo", houseDelete.getOdJobNo());
			Util.accessControlDatabase(tmpMap, "dateFormat", false, null, "", null, "");
			tmpMap = houseOrderMapper.selectHouseOrderChk(tmpMap);
			String mblNo = Util.null2str(tmpMap.get("MBL_NO"));
			
			if("Y".equals(tmpMap.get("FINISH_YN"))){
				result.setResultCode("A11237");	/** 마감된 오더는 수정 할 수 없습니다. */
				return result;
			}
			if(Util.null2int(tmpMap.get("INVOICE_CNT")) > 0){
				result.setResultCode("A11176");	/** 정산서가 발행된 건이 있어 변경할 수 없습니다. */
				return result;
			}
			
			BulkData bulkData = null;
			String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
			Map<String, Object> logMap = null;
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			
			tmpLogMap.put("ent_code", houseDelete.getEntCode());
			tmpLogMap.put("ent_business_code", houseDelete.getUserBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("bound", houseDelete.getBound());
			tmpLogMap.put("bound_type", houseDelete.getBoundType());
			tmpLogMap.put("process_user_id", houseDelete.getUpdateUserId());
			tmpLogMap.put("log_job_type", "CONSOL");
			tmpLogMap.put("job_func_type", "DELETE");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", houseDelete.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", houseDelete.getLogUserName());
			tmpLogMap.put("process_user_name_eng", houseDelete.getLogUserNameEng());
			
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
			tmpMap.clear();
			tmpMap.put("entCode", houseDelete.getEntCode());
			tmpMap.put("odJobNo", houseDelete.getOdJobNo());
			tmpMap.put("oblJobNo", houseDelete.getOblJobNo());
			
			String bound = houseDelete.getBound();
			String boundType = houseDelete.getBoundType();
			
			/** Master Delete */
			if(!"".equals(Util.null2str(houseDelete.getOblJobNo()))) {
				houseOrderMapper.deleteHouseObl(tmpMap);
				houseOrderMapper.deleteMark(tmpMap);
				houseOrderMapper.deleteHouseOrder(tmpMap);
				houseOrderMapper.deleteHouseOrderBus(tmpMap);
				if("AIR".equals(houseDelete.getBound())){
					houseOrderMapper.deleteHouseOblRealDeliverer(tmpMap);
					houseOrderMapper.deleteOtherCharge(tmpMap);
					houseOrderMapper.deleteDimension(tmpMap);
					houseOrderMapper.deleteLicense(tmpMap);
					houseOrderMapper.deleteHts(tmpMap);
				}else{
					houseOrderMapper.deleteContainer(tmpMap);
					houseOrderMapper.deleteCntrType(tmpMap);
				}
			}
			houseOrderMapper.deleteHouseOrderEcm(tmpMap);
			houseOrderMapper.deleteHouseOrderAdditional(tmpMap);
			orderMapper.deletePod(tmpMap);
			
			/** House Delete */
			List<OrderDelete> houseList = houseOrderMapper.selectDeleteHouseList(tmpMap);
			if(houseList != null && houseList.size() > 0) {
				tmpMap.clear();
				int cnt = 1;
				int seq = 1;
				String hblNos = "";
				for(OrderDelete houseDelete1 : houseList) {
					orderBatchMapper.deleterOrder(houseDelete1);
					hblNos += "," + houseDelete1.getHblNo();
					/** 하우스 1000건씩 묶어서 하나의 로그로 남김 */
					if(cnt % 1000 == 0 || cnt == houseList.size()) {
						bulkData = new BulkData();
						bulkData.setActionType(BulkData.Type.CREATE);
						bulkData.setIndexName(index);
						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
						
						logMap = new HashMap<String, Object>();
						logMap.putAll(tmpLogMap);
						logMap.put("reference_no_id", bulkData.getId());
						logMap.put("job_no", houseDelete1.getOdJobNo());
						logMap.put("description", "CONSOL ORDER DELETE [MBL_NO : " + mblNo + ", HBL_NO : "+ hblNos.substring(1) +"]");
						bulkData.setJsonMap(logMap);
						bulkList.add(bulkData);
						hblNos = "";
					}
					cnt++;
				}	
				cnt = 1;
				/** TB_ECM_OD_ADDITIONAL */
				for(OrderDelete houseDelete1 : houseList) {
					tmpMap.put("odJobNo", houseDelete1.getOdJobNo());
					orderBatchMapper.deleterAdditional(tmpMap);
					cnt++;
				}
				/** TB_ECM_OD_ITEM */
				for(OrderDelete houseDelete1 : houseList) {
					tmpMap.put("odJobNo", houseDelete1.getOdJobNo());
					orderBatchMapper.deleteItem(tmpMap);
					cnt++;
				}
				/** TB_ECM_OD_COST */
				for(OrderDelete houseDelete1 : houseList) {
					tmpMap.put("odJobNo", houseDelete1.getOdJobNo());
					orderBatchMapper.deleteCost(tmpMap);
					cnt++;
				}
				/** TB_ECM_OD_POD */
				for(OrderDelete houseDelete1 : houseList) {
					tmpMap.put("odJobNo", houseDelete1.getOdJobNo());
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
					for(OrderDelete houseDelete1 : houseList) {
						tmpMap.put("odJobNo", houseDelete1.getOdJobNo());
						orderBatchMapper.deleteMulti(tmpMap);
						cnt++;
					}
					
					/** TB_ECM_OD_PO_INFO */
					for(OrderDelete houseDelete1 : houseList) {
						tmpMap.put("odJobNo", houseDelete1.getOdJobNo());
						orderBatchMapper.deletePoInfo(tmpMap);
						cnt++;
					}
					
					if(bound.equals("AIR")){
						/** TB_ECM_OD_SPECIAL_ITEM */
						for(OrderDelete houseDelete1 : houseList) {
							tmpMap.put("odJobNo", houseDelete1.getOdJobNo());
							orderBatchMapper.deleteSpecialItem(tmpMap);
							cnt++;
						}
						/** TB_ECM_OD_ABNORMAL */
						for(OrderDelete houseDelete1 : houseList) {
							tmpMap.put("odJobNo", houseDelete1.getOdJobNo());
							orderBatchMapper.deleteAbnormal(tmpMap);
							cnt++;
						}
					}
				}else{
					/** TB_ECM_OD_LICENSE */
					for(OrderDelete houseDelete1 : houseList) {
						tmpMap.put("odJobNo", houseDelete1.getOdJobNo());
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
					for(OrderDelete houseDelete1 : houseList) {
						tmpMap.put("odJobNo", houseDelete1.getOdJobNo());
						orderBatchMapper.deleteContainer(tmpMap);
						cnt++;
					}
				}
				/** TB_SMS_ALARM */
				for(OrderDelete houseDelete1 : houseList) {
					tmpMap.put("refJobNo", houseDelete1.getOdJobNo());
					systemMngBatchMapper.deleteAlarm(tmpMap);
				}
				
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
			}
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		result.setResultCode("A00165"); // 성공 
		
		commonService.insertLogBulk(bulkList);
		
		return result;
	}
}