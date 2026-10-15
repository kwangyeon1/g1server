package org.yht.exps.express.service.selectMng;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.MailSmtpSender;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.selectMng.SelectMngMapper;
import org.yht.exps.express.model.eCommerce.order.OrderItem;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrder;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.selectMng.DeleteSelfSelection;
import org.yht.exps.express.model.selectMng.GoodsSelectionInfo;
import org.yht.exps.express.model.selectMng.GoodsSelectionInfoList;
import org.yht.exps.express.model.selectMng.GoodsSelectionMail;
import org.yht.exps.express.model.selectMng.GoodsSelectionMailList;
import org.yht.exps.express.model.selectMng.SaveGoodsSelectionInfo;
import org.yht.exps.express.model.selectMng.Selection;
import org.yht.exps.express.model.selectMng.SelectionItemList;
import org.yht.exps.express.model.selectMng.SelectionList;
import org.yht.exps.express.model.selectMng.SelfSelection;
import org.yht.exps.express.model.selectMng.SelfSelectionList;
import org.yht.exps.express.model.selectMng.UpdateSelectionList;
import org.yht.exps.express.model.selectMng.search.SearchGoodsSelectionInfo;
import org.yht.exps.express.model.selectMng.search.SearchSelectionItemList;
import org.yht.exps.express.model.selectMng.search.SearchSelectionList;
import org.yht.exps.express.model.selectMng.search.SearchSelfSelection;
import org.yht.exps.express.model.selectMng.search.SearchSelfSelectionList;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.eCommerce.OrderService;
import org.yht.util.Converter;
import org.yht.util.DateTime;
import org.yht.util.EncryptionUtils;
import org.yht.util.Util;

import com.google.gson.Gson;

import ch.qos.logback.classic.Logger;

@Service
@Transactional
public class SelectMngService {

	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name = "orderService")
	OrderService orderService;
	
	
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	SelectMngMapper selectMngMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	CustomProperties customProperties;
	
	@Value("${spring.mail.host}")
	private String host;
	@Value("${spring.mail.port}")
	private int port;
	@Value("${spring.mail.username}")
	private String username;
	@Value("${spring.mail.password}")
	private String password;
	@Value("${spring.mail.rawKey}")
	private String rawKey;
	@Value("${spring.profiles.active}")
	private String profile;
	
	/**
	 * 목록선별 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public SelectionList selectSelectionList(SearchSelectionList search, HttpServletRequest request) throws Exception {
		SelectionList selectionList = new SelectionList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<Selection> list;
		if(search.getSrchSelectionOption() != null && search.getSrchSelectionOption().size() > 0){
			search.setPage(0);
			if(search.getSrchInspectionOption() != null && search.getSrchInspectionOption().size() == 0){search.setSrchInspectionOption(null);}
			Util.accessControlDatabase(search, "gmtTime|srchRatio", true, null, "", null, "");
			list = selectMngMapper.selectSelectionList(search);
			List<Map<String, Object>> chkItemNameList = selectMngMapper.selectChkItemNameList(search);		/** 세부품목과 비교할 내용(품목명 or HsCode) */
			List<Map<String, Object>> chkItemNameOtherList = selectMngMapper.selectChkOtherList(search);	/** 오더항목과 비교할 내용 */
			List<Map<String, Object>> chkHsCodeList = selectMngMapper.selectChkHsCodeList(search);			/** HsCode 업데이트할 내용 */
			
			List<GoodsSelectionInfo> unSelectionItemList = new ArrayList<GoodsSelectionInfo>();
			Map<String, Object> itemNameChkMap = new HashMap<String, Object>();	/** 품명중복체크 */
			List<String> selfSelectionChkList = new ArrayList<String>();	/** 자체선별체크된 odJobNoList */
			List<String> goodsSelectionBList = new ArrayList<String>();	/** 자동선별 배제 체크된 odJobNoList */
			boolean selfSelectionChkFlag;
			boolean goodsSelectionBFlag;
			
			String srchSelectionType = Util.null2str(search.getSrchSelectionType());
			
			String selectionType = "";
			String autoSelectionYn = "";
			
			if(srchSelectionType.equals("auto")){	/** 통관목록자동선별 */
				autoSelectionYn = "Y";
			}else{
				autoSelectionYn = "N";
			}
			
			List<GoodsSelectionInfo> itemNameList;	/** 세부품목 */
//			List<GoodsSelectionInfo> selectionHsList = new ArrayList<GoodsSelectionInfo>();	/** hsCode 업데이트 해야하는 세부품목 리스트 */
			List<Selection> insertTempSelectionList = new ArrayList<Selection>();	/** 오더임시저장해야할 리스트 */
			
			String origCdType, operator, checkField, itemName, hsCode, compareItemName, tmpUsdCdType;
			boolean changeFlag, gridChkFlag, inspectionCrimeFlag;
			boolean chkObFlag = false;
			String cdTypeDesc, inspectionYnDesc, crimeAreaYnDesc, hsCodeDesc;
			
			Class<?> selectionClass = Class.forName("org.yht.exps.express.model.selectMng.Selection");
	        Method method = null;
	        
			for(int i = 0; i < search.getSrchSelectionOption().size(); i++){	/** USD금액체크는 선별대상-통관 일경우에 처리 */
				if(search.getSrchSelectionOption().get(i).equals("OB")){
					chkObFlag = true;
				}
				if(i == 0){
					selectionType = search.getSrchSelectionOption().get(i);
				}else{
					selectionType += "|" + search.getSrchSelectionOption().get(i);
				}
			}
			for(Selection selection : list){
				origCdType = selection.getCdType();
				/** 2025-05-16 선별 대상 목록건으로 수정 */
				if(!"Y".equals(origCdType)){
					continue;
				}
				selfSelectionChkFlag = false;
				gridChkFlag = false;
				tmpUsdCdType = "";
				cdTypeDesc = "";inspectionYnDesc = "";crimeAreaYnDesc = "";hsCodeDesc = "";
				inspectionCrimeFlag = false;
				goodsSelectionBFlag = false;
				selection.setSelectionType(srchSelectionType);
				selection.setSrchRatio(search.getSrchRatio());
				itemNameList = selectMngMapper.selectItenName(selection);
				if(chkObFlag){
					if(!"US".equals(selection.getOrigCountryCode()) && !"PR".equals(selection.getOrigCountryCode())){
						if(selection.getAmountUsd() > 150 && !origCdType.equals("N")){
							selection.setCdType("N");
							gridChkFlag = true;selfSelectionChkFlag = true;
							cdTypeDesc = ", 일반($150초과)";
						}else if(selection.getAmountUsd() <= 150 && !origCdType.equals("Y")){
							/** 2022-07-05 금액이 150$(200$) 이하 데이터 목록으로 변경하지 않도록 수정 */
							tmpUsdCdType = "Y";
						}
					}else{
						if(selection.getAmountUsd() > 200 && !origCdType.equals("N")){
							selection.setCdType("N");
							gridChkFlag = true;selfSelectionChkFlag = true;
							cdTypeDesc = ", 일반($200초과)";
						}else if(selection.getAmountUsd() <= 200 && !origCdType.equals("Y")){
							tmpUsdCdType = "Y";
						}
					}
				}
				
				/** 오더항목과 비교할 내용 */
				if(chkItemNameOtherList != null && chkItemNameOtherList.size() > 0){
					for(Map<String, Object> chkItemNameOther : chkItemNameOtherList){
						if(Util.null2str(selection.getDocumentYn()).equals(chkItemNameOther.get("DOCUMENT_YN"))){
							changeFlag = false;
							operator = Util.null2str(chkItemNameOther.get("OPERATOR"));
							checkField = Util.null2str(chkItemNameOther.get("CHECK_FIELD"));
							method = selectionClass.getMethod("get" + checkField);
							if(operator.equals("0")){
								if(Util.null2str(method.invoke(selection)).toUpperCase().equals(Util.null2str(chkItemNameOther.get("KEYWORD")).toUpperCase())){
									changeFlag = true;
								}
							}else if(operator.equals("1")){
								if(Util.null2str(method.invoke(selection)).toUpperCase().indexOf(Util.null2str(chkItemNameOther.get("KEYWORD")).toUpperCase()) > -1){
									changeFlag = true;
								}
							}else if(operator.equals("2")){
								if(Util.null2dbl(method.invoke(selection)) >= Util.null2dbl(chkItemNameOther.get("KEYWORD"))){
									changeFlag = true;
								}
							}else if(operator.equals("3")){
								if(Util.null2dbl(method.invoke(selection)) <= Util.null2dbl(chkItemNameOther.get("KEYWORD"))){
									changeFlag = true;
								}
							}
							if(changeFlag){
								/** OB:통관(배제) / ON:통관(일반) / 3Y:검사 / ZX:우범 */
								if(Util.null2str(chkItemNameOther.get("SEARCH_TYPE")).equals("OB")){
									if(!origCdType.equals("B")){
										selection.setCdType("B");
										gridChkFlag = true;selfSelectionChkFlag = true;
										cdTypeDesc = ", 배제(" + checkField + ") : " + Util.null2str(chkItemNameOther.get("KEYWORD"));
									}
								}else if(Util.null2str(chkItemNameOther.get("SEARCH_TYPE")).equals("ON")){
									if(!origCdType.equals("N")){
										selection.setCdType("N");
										gridChkFlag = true;selfSelectionChkFlag = true;
										cdTypeDesc = ", 일반(" + checkField + ") : " + Util.null2str(chkItemNameOther.get("KEYWORD"));
									}
								}else if(Util.null2str(chkItemNameOther.get("SEARCH_TYPE")).equals("3Y") && !"Y".equals(selection.getInspectionYn())){
									/** 목록구분이 목록(Y)이 아닐경우 검사Y 입력불가   하단에서 목록인건만 검사Y로 처리 */
									inspectionYnDesc = ", 검사요청(" + checkField + ") : " + Util.null2str(chkItemNameOther.get("KEYWORD"));
								}
								/** 20241024 우범국가일경우 검사Y처리 추가 */
								else if(Util.null2str(chkItemNameOther.get("SEARCH_TYPE")).equals("ZX")){
									if(!selection.getCrimeAreaYn().equals("Y") && !"Y".equals(selection.getInspectionYn())){
										selection.setCrimeAreaYn("Y");
										crimeAreaYnDesc = ", 우범대상(" + checkField + ") : " + Util.null2str(chkItemNameOther.get("KEYWORD"));
										inspectionCrimeFlag = true;
									}else if(!selection.getCrimeAreaYn().equals("Y")){
										selection.setCrimeAreaYn("Y");
										crimeAreaYnDesc = ", 우범대상(" + checkField + ") : " + Util.null2str(chkItemNameOther.get("KEYWORD"));
									}else if(!"Y".equals(selection.getInspectionYn())){
										inspectionYnDesc = ", 우범대상(" + checkField + ") : " + Util.null2str(chkItemNameOther.get("KEYWORD"));
									}
								}
							}
						}
					}
				}
				
				/** 자동선별 그리드에 특수문자 제거한 품명 표현 */
				if(Util.null2str(search.getSrchType()).equals("autoSelection")) {
					selection.setCommodityItem(Converter.changeSpeialChar(Util.null2str(selection.getCommodityItem()), ""));
				}
				
				if(itemNameList != null && itemNameList.size() > 0){
					String tmpHsCode;
					List<Selection> odJobNoSelectionList;
					Selection odJobNoSelection;
					for(GoodsSelectionInfo itemData : itemNameList){
						itemName = Util.null2str(itemData.getOdItemName()).toUpperCase();
						hsCode = Util.null2str(itemData.getOdHsCode());
						
						/** 세부품목과 비교할 내용(품목명 or HsCode) */
						for(Map<String, Object> chkItemName : chkItemNameList){
							if(Util.null2str(selection.getDocumentYn()).equals(chkItemName.get("DOCUMENT_YN"))){
								changeFlag = false;
								operator = Util.null2str(chkItemName.get("OPERATOR"));
								compareItemName = Util.null2str(chkItemName.get("KEYWORD")).toUpperCase();
								checkField = Util.null2str(chkItemName.get("CHECK_FIELD"));
								if("DetailItemName".equals(checkField)){
									if((itemName.equals(compareItemName) && operator.equals("0")) || (itemName.contains(compareItemName) && operator.equals("1"))){
										changeFlag = true;
									}
								}else{
									if((hsCode.equals(compareItemName) && operator.equals("0"))){
										changeFlag = true;
									}
								}
								
								if(changeFlag){
									if(Util.null2str(chkItemName.get("SEARCH_TYPE")).equals("OB")){
										if(!origCdType.equals("B")) {
											selection.setCdType("B");
											gridChkFlag = true;selfSelectionChkFlag = true;
											cdTypeDesc = ", 배제(" + checkField + ") : " + Util.null2str(chkItemName.get("KEYWORD"));
										}
									}else if(Util.null2str(chkItemName.get("SEARCH_TYPE")).equals("ON")){
										if(!origCdType.equals("N")) {
											selection.setCdType("N");
											gridChkFlag = true;selfSelectionChkFlag = true;
											cdTypeDesc = ", 일반(" + checkField + ") : " + Util.null2str(chkItemName.get("KEYWORD"));
										}
									}else if(Util.null2str(chkItemName.get("SEARCH_TYPE")).equals("3Y") && !selection.getInspectionYn().equals("Y")){
										inspectionYnDesc = ", 검사요청(" + checkField + ") : " + Util.null2str(chkItemName.get("KEYWORD"));
									}else if(Util.null2str(chkItemName.get("SEARCH_TYPE")).equals("ZX") && (!selection.getCrimeAreaYn().equals("Y") || !"Y".equals(selection.getInspectionYn()))){
										if(!selection.getCrimeAreaYn().equals("Y") && !"Y".equals(selection.getInspectionYn())){
											selection.setCrimeAreaYn("Y");
											crimeAreaYnDesc = ", 우범대상(" + checkField + ") : " + Util.null2str(chkItemName.get("KEYWORD"));
											inspectionCrimeFlag = true;
										}else if(!selection.getCrimeAreaYn().equals("Y")){
											selection.setCrimeAreaYn("Y");
											crimeAreaYnDesc = ", 우범대상(" + checkField + ") : " + Util.null2str(chkItemName.get("KEYWORD"));
										}else if(!"Y".equals(selection.getInspectionYn())){
											inspectionYnDesc = ", 우범대상(" + checkField + ") : " + Util.null2str(chkItemName.get("KEYWORD"));
										}
									}
								}
							}
						}
						
						/** 2025-05-15 영문,숫자 외 모두 제거하고 쿼리에서 조회함 */
//						/** 품명 특수문자 삭제하여 기초 데이터 재조회 2025.04.16 */
//						String tmpItemName = Converter.changeSpeialChar(itemName, "");
//						if(!itemName.equals(tmpItemName)) {
//							itemName = tmpItemName;
//							SearchGoodsSelectionInfo tmpSearch = new SearchGoodsSelectionInfo();
//							tmpSearch.setSrchItemName(tmpItemName);
//							tmpSearch.setDefaultSort("ITEM_NAME");
//							List<GoodsSelectionInfo> tmpList = selectMngMapper.selectGoodsSelectionList(tmpSearch);
//							if(tmpList != null && tmpList.size() > 0) {
//								itemData.setItemName(tmpList.get(0).getItemName());
//								itemData.setHsCode(tmpList.get(0).getHsCode());
//								itemData.setCdYtypeYn(tmpList.get(0).getCdYtypeYn());
//								itemData.setCdBtypeYn(tmpList.get(0).getCdBtypeYn());
//							}
//						}
						
						/** 2025-05-16 충돌문제로 중복 작성함   머지후 2번째if문 제거 */
						if(Util.null2str(selection.getAllowedItemCode()).equals("")){
							if(!Util.isEmpty(itemData.getHsCode())){
								selection.setAllowedItemCode(itemData.getHsCode());
								selection.setSelectionItemName(itemName);
								gridChkFlag = true;
								hsCodeDesc = ", Backup file(품목코드)";
							}else{
								tmpHsCode = "";
								/** HsCode 업데이트할 내용 */
								for(Map<String, Object> chkHsCode : chkHsCodeList){
									if(Util.null2str(chkHsCode.get("DOCUMENT_YN")).equals(selection.getDocumentYn())){
										operator = Util.null2str(chkHsCode.get("OPERATOR"));
										if(operator.equals("0")){
											if(itemName.equals(Util.null2str(chkHsCode.get("KEYWORD")).toUpperCase())){
												if(!hsCode.equals(Util.null2str(chkHsCode.get("HS_CODE")))){
													selection.setAllowedItemCode(Util.null2str(chkHsCode.get("HS_CODE")));
													selection.setSelectionItemName(itemName);
													gridChkFlag = true;
													hsCodeDesc = ", 품목코드(DetailItemName) : " + Util.null2str(chkHsCode.get("KEYWORD"));
													break;
												}
											}
										}else if(operator.equals("1")){
											if(itemName.contains(Util.null2str(chkHsCode.get("KEYWORD")).toUpperCase())){
												if(!hsCode.equals(Util.null2str(chkHsCode.get("HS_CODE")))){
													selection.setAllowedItemCode(Util.null2str(chkHsCode.get("HS_CODE")));
													selection.setSelectionItemName(itemName);
													gridChkFlag = true;
													hsCodeDesc = ", 품목코드(DetailItemName) : " + Util.null2str(chkHsCode.get("KEYWORD"));
													break;
												}
											}
										}
									}
								}
								if("".equals(hsCodeDesc) && !Util.isEmpty(itemData.getOdHsCode())){
									selection.setAllowedItemCode(itemData.getOdHsCode());
									gridChkFlag = true;
									hsCodeDesc = ", 품목코드(세부품목)";
								}
							}
						}
						/** 2025-05-16 충돌문제로 중복 작성함   머지후 제거 */
						if(Util.null2str(selection.getAllowedItemCode()).equals("")) {
							selection.setAllowedItemCode(!Util.null2str(itemData.getOdHsCode()).equals("") ? itemData.getOdHsCode() : Util.null2str(itemData.getHsCode()));
						}
						
						/** GOODS_SELECTION_INFO */
						if(!Util.null2str(itemData.getItemName()).equals("")){
							if(Util.null2str(itemData.getCdBtypeYn()).equals("Y")){
								if(!origCdType.equals("B")) {
									selection.setCdType("B");
									gridChkFlag = true; goodsSelectionBFlag = true;
									cdTypeDesc = ", Backup file(배제)";
								}
							}
							/** 2025-05-16 자동선별 대상 목록건으로 수정    목록으로 변경처리 하지 않음 */
//							else if("Y".equals(itemData.getCdYtypeYn()) && tmpUsdCdType.equals("Y")){	/** USD금액이 기준 이하일때만 적용 */
//								if(!origCdType.equals("Y")) {
//									selection.setCdType("Y");
//									gridChkFlag = true;
//									cdTypeDesc = ", Backup file(목록)";
//								}
//							}
							
//							if(!Util.null2str(itemData.getHsCode()).equals("") && !hsCode.equals(itemData.getHsCode())){
//								itemData.setItemName(itemData.getOdItemName());
//								selectionHsList.add(itemData);
//								tmpHsCode = "";
//							}
						}else if(srchSelectionType.equals("auto")){
							/** 품목검토 그리드 추가 */
							odJobNoSelectionList = (List)itemNameChkMap.get(itemName);
							if(odJobNoSelectionList == null){
								odJobNoSelectionList = new ArrayList<Selection>();
							}
							odJobNoSelection = new Selection();
							odJobNoSelection.setOdJobNo(itemData.getOdJobNo());
							odJobNoSelection.setAmountUsd(itemData.getAmountUsd());
							odJobNoSelection.setMasterOdJobNo(search.getSrchMasterOdJobNo());
							odJobNoSelection.setAllowedItemCode(hsCode);
							odJobNoSelection.setOrigCountryCode(selection.getOrigCountryCode());
							if("Y".equals(origCdType) && "Y".equals(selection.getCdType())){
								odJobNoSelection.setCdType("Y");	/** 마감시 USD 150(200)초과 체크해야하는지 여부 */
							}
							odJobNoSelectionList.add(odJobNoSelection);
							itemNameChkMap.put(itemName, odJobNoSelectionList);
						}
						
//						/** 기초1보다 기초2를 우선적용함    기초2에서 HS Code업데이트가 없을경우 기초1의 HS Code로 업데이트 */
//						if(!tmpHsCode.equals("")){
//							itemData.setHsCode(tmpHsCode);
//							itemData.setItemName(itemData.getOdItemName());
//							selectionHsList.add(itemData);
//						}
					}
				}
					
				
				if("Y".equals(selection.getCdType()) && (inspectionYnDesc.length() > 0 || inspectionCrimeFlag)){
					selection.setInspectionYn("Y");
					gridChkFlag = true;
				}else{
					inspectionYnDesc = "";
				}
				
				if(!cdTypeDesc.equals("") || !inspectionYnDesc.equals("") || !crimeAreaYnDesc.equals("")  || !hsCodeDesc.equals("")){
					selection.setCheckInfo((cdTypeDesc + inspectionYnDesc + crimeAreaYnDesc + hsCodeDesc).substring(2));
				}
				if(gridChkFlag || !crimeAreaYnDesc.equals("")){
					if(gridChkFlag){
						selection.setDataChangeYn("Y");
					}
					if(selfSelectionChkFlag){
						selfSelectionChkList.add(selection.getOdJobNo());
					}
					if(goodsSelectionBFlag) {
						goodsSelectionBList.add(selection.getOdJobNo());
					}
					insertTempSelectionList.add(selection);
				}
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("masterOdJobNo", search.getSrchMasterOdJobNo());
			selectMngMapper.deleteTempSelection(map);
			selectMngMapper.deleteTempSelectionItem(map);
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				SelectMngMapper selectBatchMngMapper = sqlSession.getMapper(SelectMngMapper.class);
				int cnt = 1;
				if(insertTempSelectionList.size() > 0){
					for(Selection selection : insertTempSelectionList){
						selection.setSelectionType(selectionType);
						selection.setAutoSelectionYn(autoSelectionYn);
						selection.setRegisterUserId(search.getUserId());
						selection.setGmtTime(search.getGmtTime());
						selectBatchMngMapper.insertTempSelection(selection);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
				if(srchSelectionType.equals("auto")){
					GoodsSelectionInfo unSelectionItem;
					int seqNoId = 1;
					boolean addNameFlag;	/** 품명검토에 추가될 데이터가 자체선별에서 통관목록이 일반or배제로 변경된 오더에밖에 없을경우 품명검토에 추가하지않음 */
					boolean removeFlag;
					Map<String, List<Selection>> updateItemNameSelection = new HashMap<String, List<Selection>>();
					List<Selection> itemNameSelectionList;
					String tmpHsCode = "";
					for(String chkItemName : itemNameChkMap.keySet()){
						addNameFlag = false; 
						tmpHsCode = "";
						itemNameSelectionList = (List)itemNameChkMap.get(chkItemName);
						for(int i = itemNameSelectionList.size() -1; i >= 0; i--) {
							removeFlag = false;
							tmpHsCode = Util.null2str(itemNameSelectionList.get(i).getAllowedItemCode());
							for(int j = selfSelectionChkList.size() -1; j >= 0; j--) {
								if(!selfSelectionChkList.get(j).equals(itemNameSelectionList.get(i).getOdJobNo())){
									addNameFlag = true;
								}else{
//									itemNameSelectionList.remove(i);
									removeFlag = true;
									break;
								}
							}
							/** 기초에 배제로 등록되어 있는 품명이 하나라도 있으면 하우스 전체 품명을 품명검토에 추가하지않음 2025.04.16  */
							if(!removeFlag) {
								int chkCnt = 0;
								for(int j = goodsSelectionBList.size() -1; j >= 0; j--) {
									if(goodsSelectionBList.get(j).equals(itemNameSelectionList.get(i).getOdJobNo())){
										chkCnt++;
									}
								}
								if(chkCnt > 0) {
//								itemNameSelectionList.remove(i);
									removeFlag = true;
								}else {
									addNameFlag = true;
								}
							}
							
							if(removeFlag) {
								itemNameSelectionList.remove(i);
							}
						}
						
						if(addNameFlag) {
							unSelectionItem = new GoodsSelectionInfo();
							unSelectionItem.setMasterOdJobNo(search.getSrchMasterOdJobNo());
							unSelectionItem.setSeqNoId(seqNoId);
							unSelectionItem.setItemName(chkItemName);
							unSelectionItem.setHsCode(tmpHsCode);
							unSelectionItem.setCdYtypeYn("N");
							unSelectionItem.setCdBtypeYn("N");
							unSelectionItem.setInspectionYn("N");
							unSelectionItem.setReviewYn("N");
							unSelectionItem.setUpdateUserId(search.getUserId());
							unSelectionItem.setGmtTime(search.getGmtTime());
							unSelectionItem.setOdJobNoSelectionList(itemNameSelectionList);
							selectBatchMngMapper.insertTempSelectionItem(unSelectionItem);
							unSelectionItemList.add(unSelectionItem);
							updateItemNameSelection.put(Util.null2str(seqNoId++), itemNameSelectionList);
							if( (cnt++ % 1000) == 0){
								sqlSession.flushStatements();
								sqlSession.clearCache();
							}
						}
						
					}
					Map<String, String> chkOdJobNo;
					for(String seqNo : updateItemNameSelection.keySet()){
						itemNameSelectionList = updateItemNameSelection.get(seqNo);
						chkOdJobNo = new HashMap<String, String>();
						for(Selection selection : itemNameSelectionList){
							
							/** 하나의오더에 같은품명이 여러게있을경우 중복저장되지 않도록 처리 */
							if(chkOdJobNo.get(selection.getOdJobNo()) == null){
								chkOdJobNo.put(selection.getOdJobNo(), "Y");
							}else{
								continue;
							}
							
							selection.setSeqNoId(seqNo);
							selectBatchMngMapper.insertTempSelectionItemHbl(selection);
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
			selectionList.setUnSelectionItemList(unSelectionItemList);
//			selectionList.setSelectionHsList(selectionHsList);
			
			if(search.getPageSize() > list.size()){
				selectionList.setSelectionList(list);
			}else{
				selectionList.setSelectionList(list.subList(0, search.getPageSize()));
			}
			
			// 처리 로그 기록
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(search.getSrchEntCode());
			processLogs.setEntBusinessCode(search.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(search.getBound());
			processLogs.setBoundType(search.getBoundType());
			processLogs.setProcessUserId(search.getUserId());
			processLogs.setLogJobType("UPDATE");
			processLogs.setJobFuncType("UPDATE");
			processLogs.setDescription("selection temp update");
			processLogs.setJobLog(new Gson().toJson(search));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(search.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(search.getLogUserName());
			processLogs.setProcessUserNameEng(search.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
			
		}else if(search.getSrchOdJobNoList() != null && !Util.null2str(search.getSrchMode()).equals("excel")){	/** 자동선별 g4Grid */
			Util.accessControlDatabase(search, null, true, null, "", null, "");
			list = selectMngMapper.selectSelectionList(search);
			for(Selection selection : list) {
				selection.setCommodityItem(Converter.changeSpeialChar(Util.null2str(selection.getCommodityItem()), ""));
			}
			selectionList.setSelectionList(list);
		}else if("autoSelectionClose".equals(search.getSrchType())){	/** 자동선별 마감된 데이터 조회 */
			Util.accessControlDatabase(search, "gmtTime", true, null, "", null, "");
			list = selectMngMapper.selectSelectionList(search);
			selectionList.setSelectionList(list);
		}else{
			Util.accessControlDatabase(search, null, true, null, "", null, "");
			list = selectMngMapper.selectTempSelectionList(search);
			selectionList.setSelectionList(list);
			if(Util.null2str(search.getSrchSelectionType()).equals("auto")){
				/** 품명, 품명에 속해있는 오더 임시저장데이터 조회 */
				List<GoodsSelectionInfo> selectionItemList = new ArrayList<GoodsSelectionInfo>();	/** 품명임시저장 */
				List<Selection> odJobNoSelectionList = null;	/** 품명오더임시저장 */
				List<GoodsSelectionInfo> tmpList = selectMngMapper.selectTempSelectionItemList(search);
				int seqNoId = 0;
				GoodsSelectionInfo goodsSelectionInfo;
				Selection selection;
				for(GoodsSelectionInfo tmpData : tmpList){
					if(seqNoId != tmpData.getSeqNoId()){
						seqNoId = tmpData.getSeqNoId();
						odJobNoSelectionList = new ArrayList<Selection>();
						goodsSelectionInfo = new GoodsSelectionInfo();
						selectionItemList.add(goodsSelectionInfo);
						goodsSelectionInfo.setOdJobNoSelectionList(odJobNoSelectionList);
						goodsSelectionInfo.setSeqNoId(tmpData.getSeqNoId());
						goodsSelectionInfo.setItemName(tmpData.getItemName());
						goodsSelectionInfo.setHsCode(tmpData.getHsCode());
						goodsSelectionInfo.setCdYtypeYn(tmpData.getCdYtypeYn());
						goodsSelectionInfo.setCdBtypeYn(tmpData.getCdBtypeYn());
						goodsSelectionInfo.setInspectionYn(tmpData.getInspectionYn());
						goodsSelectionInfo.setReviewYn(tmpData.getReviewYn());
						goodsSelectionInfo.setMasterOdJobNo(tmpData.getMasterOdJobNo());
					}
					selection = new Selection();
					selection.setOdJobNo(tmpData.getOdJobNo());
					selection.setAmountUsd(tmpData.getAmountUsd());
					selection.setMasterOdJobNo(tmpData.getMasterOdJobNo());
					selection.setOrigCountryCode(tmpData.getOrigCountryCode());
					if("Y".equals(tmpData.getCdType()) && "Y".equals(tmpData.getCdYtypeYn())){
						selection.setCdType("Y");	/** 마감시 USD 150(200)초과 체크해야하는지 여부 */
					}
					odJobNoSelectionList.add(selection);
				}
				
				selectionList.setUnSelectionItemList(selectionItemList);
			}else if("excel".equals(Util.null2str(search.getSrchMode()))) { /** 자체선별 엑셀 다운로드 - 아이템 조회 */
				SearchOrder searchOrder = new SearchOrder();
				List<Selection> tmpSelectionList = new ArrayList<Selection>();
				Selection tmpSelection;
				List<OrderItem> tmpItemList = new ArrayList<OrderItem>();
				int cnt = 0;
				for(Selection selection : list) {
					searchOrder.setSrchOdJobNo(selection.getOdJobNo());
					tmpItemList = orderMapper.selectItem(searchOrder);
					if(tmpItemList.size() > 0) {
						for(OrderItem item : tmpItemList) {
							tmpSelection = selection.clone(); 
							tmpSelection.setNum(++cnt);
							tmpSelection.setSelectionItem(item);
							tmpSelectionList.add(tmpSelection);
						}
					}else {
						selection.setNum(++cnt);
						tmpSelectionList.add(selection);
					}
				}
				if(tmpSelectionList.size() > 0) {
					selectionList.setSelectionList(tmpSelectionList);
				}
			}
		}
		
		if(list.size() > 0) {
			selectionList.setTotal(list.get(0).getTotal());
		}else {
			selectionList.setTotal(0);
		}
		return selectionList;
	}
	
	/**
	 * 목록선별 세부품목 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public SelectionItemList selectSelectionItemList(SearchSelectionItemList search) throws Exception {
		SelectionItemList selectionItemList = new SelectionItemList();
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		SearchOrder searchOrder = new SearchOrder();
		searchOrder.setSrchOdJobNo(search.getSrchOdJobNo());
		selectionItemList.setSelectionItemList(orderMapper.selectItem(searchOrder));
		return selectionItemList;
	}
	
	/**
	 * 목록선별 임시저장 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateTempSelectionList(UpdateSelectionList search, HttpServletRequest request) throws Exception {
		Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("masterOdJobNo", search.getMasterOdJobNo());
		map.put("autoSelectionYn", search.getAutoSelectionYn());
		
		if("Y".equals(search.getDeleteOtherSelectionYn())){
			selectMngMapper.deleteOtherTempSelection(map);
			if("Y".equals(search.getAutoSelectionYn())){
				
			}
		}
		
		map.put("deleteOdJobNoList", search.getDeleteOdJobNoList());
		selectMngMapper.deleteTempSelection(map);
		
		List<Selection> selectionList = search.getSelectionList();
		if(selectionList != null && selectionList.size() > 0){
			SqlSession sqlSession = null;
			try {
				int cnt = 1;
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				SelectMngMapper selectBatchMngMapper = sqlSession.getMapper(SelectMngMapper.class);
				for(Selection selection : selectionList){
					selection.setMasterOdJobNo(search.getMasterOdJobNo());
					selection.setSelectionType(search.getSelectionType());
					selection.setAutoSelectionYn(search.getAutoSelectionYn());
					selection.setDataChangeYn("Y");
					selection.setRegisterUserId(search.getUpdateUserId());
					selection.setGmtTime(search.getGmtTime());
					selectBatchMngMapper.insertTempSelection(selection);
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
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(search.getEntCode());
		processLogs.setEntBusinessCode(search.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(search.getBound());
		processLogs.setBoundType(search.getBoundType());
		processLogs.setProcessUserId(search.getUpdateUserId());
		processLogs.setJobNo(search.getMasterOdJobNo());
		processLogs.setLogJobType("UPDATE");
		processLogs.setJobFuncType("UPDATE");
		processLogs.setDescription("selection temp update");
//		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobLog("");
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(search.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(search.getLogUserName());
		processLogs.setProcessUserNameEng(search.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return orderSaveResult;
	}
	
	/**
	 * 목록선별품명 임시저장 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateTempSelectionItemList(UpdateSelectionList search, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		List<GoodsSelectionInfo> selectionItemList = search.getSelectionItemList();
		if(selectionItemList != null && selectionItemList.size() > 0){
			SqlSession sqlSession = null;
			try {
				int cnt = 1;
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				SelectMngMapper selectBatchMngMapper = sqlSession.getMapper(SelectMngMapper.class);
				for(GoodsSelectionInfo goodsSelection : selectionItemList){
					selectBatchMngMapper.updateTempSelectionItem(goodsSelection);
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
			
			// 처리 로그 기록
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(search.getEntCode());
			processLogs.setEntBusinessCode(search.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(search.getBound());
			processLogs.setBoundType(search.getBoundType());
			processLogs.setProcessUserId(search.getUpdateUserId());
			processLogs.setJobNo(search.getMasterOdJobNo());
			processLogs.setLogJobType("UPDATE");
			processLogs.setJobFuncType("UPDATE");
			processLogs.setDescription("selection item temp update");
//			processLogs.setJobLog(new Gson().toJson(search));
			processLogs.setJobLog("");
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(search.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(search.getLogUserName());
			processLogs.setProcessUserNameEng(search.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}
		
		
		return orderSaveResult;
	}
	
	/**
	 * 목록선별 리스트 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateSelectionList(UpdateSelectionList search, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		if(Util.null2str(search.getSelectionYn()).equals("Y")){
			if(Util.null2str(selectMngMapper.selectSelectionYn(search)).equals("Y")){
				orderSaveResult.setResultCode("A00112");	/** 마감이 완료된 자료 입니다. */
				return orderSaveResult;
			}
			
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("masterOdJobNo", search.getMasterOdJobNo());
			tmpMap = commonMapper.selectSimpleDocSendYn(tmpMap);
			if("Y".equals(tmpMap.get("SEND_YN"))){
				orderSaveResult.setResultCode("A11333");	/** 통관목록이 전송되어서 처리할 수 없습니다. */
				return orderSaveResult;
			}
		}
		selectMngMapper.updateSelectionYn(search);
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
				
		if(Util.null2str(search.getSelectionYn()).equals("Y")){
			List<GoodsSelectionInfo> selectionHsList = new ArrayList<GoodsSelectionInfo>();	/** 기초1,기초2에서 선별된 HS Code 업데이트해야할 리스트 */
			
			SearchSelectionList searchSelectionList = new SearchSelectionList();
			searchSelectionList.setSrchMasterOdJobNo(search.getMasterOdJobNo());
			searchSelectionList.setSrchEntCode(search.getEntCode());
			searchSelectionList.setSrchType("updateHsCode");
			searchSelectionList.setCryptographyYn(search.getCryptographyYn());
			
			List<Map<String, Object>> chkHsCodeList = selectMngMapper.selectChkHsCodeList(searchSelectionList);			/** HsCode 업데이트할 내용 */
			Util.accessControlDatabase(searchSelectionList, null, true, null, "", null, "");
			List<Selection> list = selectMngMapper.selectSelectionList(searchSelectionList);
			List<GoodsSelectionInfo> itemNameList;	/** 세부품목 */
			String srchSelectionType, operator, itemName, hsCode;
			if(Util.null2str(search.getAutoSelectionYn()).equals("Y")){
				srchSelectionType = "auto";
			}else{
				srchSelectionType = "self";
			}
			
			List<Selection> selectionList = search.getSelectionList();	/** 오더그리드에서 체크한 리스트 */
			boolean gridChkFlag = false;
			if(selectionList != null && selectionList.size() > 0){
				gridChkFlag = true;
			}
			
			List<Selection> selectionOdList = selectMngMapper.selectSelectionOdList(search);
			/** 2025-05-16 자동선별은 마감시 hsCode 기초에서 체크하지 않음 */
			if("auto".equals(srchSelectionType)){
				
				for(int i = selectionOdList.size() - 1; i >= 0; i--){
					Selection selection = selectionOdList.get(i);
					if(!Util.isEmpty(selection.getSelectionItemName())){
						GoodsSelectionInfo itemData = new GoodsSelectionInfo();
						itemData.setHsCode(selection.getAllowedItemCode());
						itemData.setOdJobNo(selection.getOdJobNo());
						itemData.setItemName(selection.getSelectionItemName());
						selectionHsList.add(itemData);
					}
					if(gridChkFlag){
						for(Selection tmp : selectionList){
							if(tmp.getOdJobNo().equals(selection.getOdJobNo())){
								selectionOdList.remove(i);
								break;
							}
						}
					}
				}
			}else{
				for(Selection selection : list){
					selection.setSelectionType(srchSelectionType);
					itemNameList = selectMngMapper.selectItenName(selection);
					if(itemNameList != null && itemNameList.size() > 0){
						String tmpHsCode;
						for(GoodsSelectionInfo itemData : itemNameList){
							itemName = Util.null2str(itemData.getOdItemName()).toUpperCase();
							hsCode = Util.null2str(itemData.getOdHsCode());
							tmpHsCode = "";
							/** HsCode 업데이트할 내용 */
							for(Map<String, Object> chkHsCode : chkHsCodeList){
								if(Util.null2str(chkHsCode.get("DOCUMENT_YN")).equals(selection.getDocumentYn())){
									operator = Util.null2str(chkHsCode.get("OPERATOR"));
									if(operator.equals("0")){
										if(itemName.equals(Util.null2str(chkHsCode.get("KEYWORD")).toUpperCase())){
											if(!hsCode.equals(Util.null2str(chkHsCode.get("HS_CODE")))){
												tmpHsCode = Util.null2str(chkHsCode.get("HS_CODE"));
												break;
											}
										}
									}else if(operator.equals("1")){
										if(itemName.contains(Util.null2str(chkHsCode.get("KEYWORD")).toUpperCase())){
											if(!hsCode.equals(Util.null2str(chkHsCode.get("HS_CODE")))){
												tmpHsCode = Util.null2str(chkHsCode.get("HS_CODE"));
												break;
											}
										}
									}
								}
							}
							if(!tmpHsCode.equals("")){
								itemData.setHsCode(tmpHsCode);
								itemData.setItemName(itemData.getOdItemName());
								selectionHsList.add(itemData);
							}
						}
					}
				}
			}
			
			if(gridChkFlag){
				selectionList.addAll(selectionOdList);
			}else{
				selectionList = selectionOdList;
			}
			
			List<GoodsSelectionInfo> selectionItemList = search.getSelectionItemList();	/** 품명검토 완료한 그리드 리스트 */
			List<Selection> gridInspectionList = new ArrayList<Selection>();	/** 품명검토 완료 그리드 중 검사Y인 데이터 */
			if(selectionItemList != null && selectionItemList.size() > 0){
				GoodsSelectionInfo goodsSelectionInfo;
				Selection selection;
				List<Selection> odJobNoList;
				Map<String, Selection> chkMap = new HashMap<String, Selection>();
				/** 하나의 오더에 품명검토완료내용이 2건이상있고 서로 업데이트해야할 값이 다를경우 통관여부는 배제, 검사YN은 Y로 업데이트 */
				for(GoodsSelectionInfo selectionItem : selectionItemList){
					odJobNoList = selectionItem.getOdJobNoSelectionList();
					for(Selection odJobNoSelection : odJobNoList){
						selection = chkMap.get(odJobNoSelection.getOdJobNo());
						if(selection == null){
							selection = new Selection();
							selection.setOdJobNo(odJobNoSelection.getOdJobNo());
							if("Y".equals(selectionItem.getCdYtypeYn())){
								selection.setCdType("Y");
							}else if("Y".equals(selectionItem.getCdBtypeYn())){
								selection.setCdType("B");
							}else{
								selection.setCdType("");
							}
							selection.setInspectionYn(selectionItem.getInspectionYn());
							chkMap.put(odJobNoSelection.getOdJobNo(), selection);
						}else{
							if("Y".equals(selectionItem.getCdBtypeYn())){
								selection.setCdType("B");
							}else if("Y".equals(selectionItem.getCdYtypeYn()) && !"B".equals(selection.getCdType()) && !"N".equals(selection.getCdType())){
								selection.setCdType("Y");
							}
							if("Y".equals(selectionItem.getInspectionYn())){
								selection.setInspectionYn("Y");
							}
						}
						if("Y".equals(selection.getCdType()) || ("".equals(selection.getCdType()) && "Y".equals(odJobNoSelection.getCdType()))){
							/** cdType(품목검토완료그리드목록구분값)이 목록이거나 cdType이 null이고 선별한 목록구분값이 목록일경우) USD체크 */
							if(!"US".equals(odJobNoSelection.getOrigCountryCode()) && !"PR".equals(odJobNoSelection.getOrigCountryCode())){
								if(odJobNoSelection.getAmountUsd() > 150){
									selection.setCdType("N");
								}
							}else{
								if(odJobNoSelection.getAmountUsd() > 200){
									selection.setCdType("N");
								}
							}
						}
						
						/** HS Code는 기초선별, 품목검토완료그리드 양쪽에 다 있을경우 품목검토완료그리드 내용으로 업데이트(기초선별업데이트 => 품목검토완료그리드업데이트) */
						if(!Util.null2str(selectionItem.getHsCode()).equals("")){
							goodsSelectionInfo = new GoodsSelectionInfo();
							goodsSelectionInfo.setOdJobNo(odJobNoSelection.getOdJobNo());
							goodsSelectionInfo.setItemName(selectionItem.getItemName());
							goodsSelectionInfo.setHsCode(selectionItem.getHsCode());
							selectionHsList.add(goodsSelectionInfo);
						}
					}
				}
				
				for(String odJobNo : chkMap.keySet()){
					selection = chkMap.get(odJobNo);
					/** 목록구분은 오더그리드, 품목검토완료그리드 양쪽에 다 있을경우 품목검토완료그리드 내용으로 업데이트(오더그리드업데이트 => 품목검토완료그리드업데이트) */
					if(!"".equals(selection.getCdType())){
						selection.setSelectionType("gridCdType");
						selectionList.add(selection);
					}
					/** 검사는 오더그리드, 품목검토완료그리드 양쪽에 다 있을경우 오더그리드 내용으로 업데이트(품목검토완료그리드업데이트 => 오더그리드업데이트) */
					if("Y".equals(selection.getInspectionYn())){
						gridInspectionList.add(selection);
					}
				}
			}
			
			List<Map<String, Object>> updateCdTypeList = new ArrayList<Map<String, Object>>();
			Map<String, Object> updateCdTypeMap = new HashMap<String, Object>();
			if(selectionList != null && selectionList.size() > 0){
				for(Selection selection : selectionList){
					updateCdTypeMap.put("odJobNo", selection.getOdJobNo());
					selection.setOrderData(commonMapper.selectBmsCdType(updateCdTypeMap));
				}
			}
			
			SqlSession sqlSession = null;
			try {
				int cnt = 1;
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				SelectMngMapper selectMngMapper2 = sqlSession.getMapper(SelectMngMapper.class);
				CommonMapper CommonBatchMapper = sqlSession.getMapper(CommonMapper.class);
				if(selectionHsList != null && selectionHsList.size() > 0){
					for(GoodsSelectionInfo selectionItem : selectionHsList){
						selectMngMapper2.updateSelectionHsList(selectionItem);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
				cnt = 1;
				if(gridInspectionList.size() > 0){
					for(Selection selection : gridInspectionList){
						selectMngMapper2.updateInspectionList(selection);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
				
				if(selectionList != null && selectionList.size() > 0){
					cnt = 1;
					for(Selection selection : selectionList){
						
						if(!"gridCdType".equals(selection.getSelectionType())){
							if("Y".equals(selection.getCdType())){
								if(!"US".equals(selection.getOrigCountryCode()) && !"PR".equals(selection.getOrigCountryCode())){
									if(selection.getAmountUsd() > 150){
										selection.setCdType("N");
									}
								}else{
									if(selection.getAmountUsd() > 200){
										selection.setCdType("N");
									}
								}
							}
						}
						
						selectMngMapper2.updateSelectionList(selection);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
					cnt = 1;
					for(Selection selection : selectionList){
						if(!"Y".equals(selection.getCdType())){
							selectMngMapper2.updateSelectionClearanceStatus(selection);
						}
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
					cnt = 1;
					for(Selection selection : selectionList){
						updateCdTypeMap = selection.getOrderData();
						if(!Util.null2str(updateCdTypeMap.get("CD_TYPE")).equals(selection.getCdType())){
							updateCdTypeMap.put("odJobNo", selection.getOdJobNo());
							updateCdTypeMap.put("cdType", selection.getCdType());
							CommonBatchMapper.updateBmsCdType(updateCdTypeMap);
							updateCdTypeList.add(updateCdTypeMap);
						}
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
					
					if(updateCdTypeList.size() > 0){
						Map<String, Object> resultData = new HashMap<String, Object>();
						resultData.put("updateCdTypeList", updateCdTypeList);
						orderSaveResult.setResultCode("updateBmsCdType");
						orderSaveResult.setResultData(resultData);
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
			processLogs.setJobFuncType("UPDATE");
			processLogs.setDescription("selection close");
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("masterOdJobNo", search.getMasterOdJobNo());
			selectMngMapper.deleteTempSelection(map);
			selectMngMapper.deleteTempSelectionItem(map);
			 
			/** 선별마감 마친 후 관세사 E-MAIL 전송 (자동선별) */
			if("Y".equals(Util.null2str(search.getEmailYn()))) {
				List<Map<String, Object>> mailList = selectMngMapper.selectSelectionMailList(search);
				String receiver = "";
				String subject = "선별마감 E-MAIL 안내"; 
				String sendMsg = "";
				String custName = "";
				if(mailList != null && mailList.size() > 0) {
					for(Map<String, Object> mailMap : mailList) {
						if(!"".equals(Util.null2str(mailMap.get("CUST_CODE_LV2")))) {
							custName = Util.null2str(mailMap.get("CUST_CODE_LV2"));
						}else {
							custName = Util.null2str(mailMap.get("CUST_CODE_LV1"));
						}
						
						// 메일 내용 세팅
						sendMsg = "";
						sendMsg += "안녕하세요<br/>";
						sendMsg += search.getEntName() + "입니다.<br/><br/>";
						sendMsg += custName + "의 선별마감 되었습니다.<br/><br/>";
						sendMsg += "입항일자: " + Util.null2str(mailMap.get("AR_DATE")) + "<br/>";
						sendMsg += "마스터번호: " + Util.null2str(mailMap.get("MBL_NO")) + " / 편명: " + Util.null2str(mailMap.get("VESSEL_FLIGHT")) + "<br/>";
						sendMsg += "일반건: " + Util.null2str(mailMap.get("CD_TYPE_N_CNT")) + " 건<br/>";
						sendMsg += "배제건: " + Util.null2str(mailMap.get("CD_TYPE_B_CNT")) + " 건<br/>";
						sendMsg += "일반+배제: " + Util.null2str(mailMap.get("CD_TYPE_NB_CNT")) + " 건<br/><br/>";
						sendMsg += "G1-Express.com 확인하시어 수입신고 부탁드립니다.<br/><br/>감사합니다.";
						
						// 메일 전송
						if(!"".equals(Util.null2str(mailMap.get("CB_EMAIL"))) && !"".equals(Util.null2str(mailMap.get("CB_NAME")))) {
							receiver = Util.null2str(mailMap.get("CB_EMAIL"));
							MailSmtpSender sender = new MailSmtpSender(host, port, username, EncryptionUtils.decryptAES(password, rawKey));
							String senderName = profile.toUpperCase();
							if(senderName.contains("PROD")){
								senderName = "G1Express";
							}
							try {
								//TODO 테스트중 주석
								sender.sendSmtpMail("GONE@YHT.CO.KR", "G1Express", new String[] {receiver}, subject, sendMsg);
							}catch(Exception e){
								String sendStatusRemark = e.getMessage();
								if (sendStatusRemark.length() > 500) {
									sendStatusRemark = sendStatusRemark.substring(0, 480);
								}
								orderSaveResult.setResultCode("A00375");
							}
						}
					}
				}
			}
		}else{
			processLogs.setJobFuncType("UPDATE");
			processLogs.setDescription("selection release");
		}
		
		search.setSelectionList(null);
		search.setSelectionItemList(null);
		processLogs.setEntCode(search.getEntCode());
		processLogs.setEntBusinessCode(search.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(search.getBound());
		processLogs.setBoundType(search.getBoundType());
		processLogs.setProcessUserId(search.getUpdateUserId());
		processLogs.setLogJobType("UPDATE");
		processLogs.setJobNo(search.getMasterOdJobNo());
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(search.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(search.getLogUserName());
		processLogs.setProcessUserNameEng(search.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return orderSaveResult;
	}
	/**
	 * 목록선별 리스트 제출번호 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateSelectionSimpleDocNo(UpdateSelectionList search, HttpServletRequest request) throws Exception {
		
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		String searchType = "simpleDocNo";
		String aeDocNo = "";
		String adDocNo = "";
		List<String> hblList = new ArrayList<>();
		Map<String, Object> hblNoMap = new HashMap<>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		if(Util.null2str(search.getSelectionYn()).equals("Y")){
			tmpMap.put("masterOdJobNo", search.getMasterOdJobNo());
			tmpMap.put("searchType", searchType);
			tmpMap = commonMapper.selectSimpleDocSendYn(tmpMap);
			if("Y".equals(tmpMap.get("SEND_YN"))){
				orderSaveResult.setResultCode("A11333");	/** 통관목록이 전송되어서 처리할 수 없습니다. */
				return orderSaveResult;
			}
			
			if(Util.null2int(tmpMap.get("AE_DOC_CNT")) > 1 || Util.null2int(tmpMap.get("AD_DOC_CNT")) > 1 ) {
				orderSaveResult.setResultCode("A11625");	/** 1개 이상의 제출번호가 존재합니다. */
				return orderSaveResult;
			}
			
			if(Util.null2int(tmpMap.get("AE_DOC_CNT")) == 0 && Util.null2int(tmpMap.get("AD_DOC_CNT")) == 0 ) {
				orderSaveResult.setResultCode("A11274");	/** 제출번호가 없는건이 포함되어 있습니다..*/
				return orderSaveResult;
			}
			
		}
		aeDocNo = Util.null2str(tmpMap.get("AE_DOC_NO"));
		adDocNo = Util.null2str(tmpMap.get("AD_DOC_NO"));
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
				
		if(Util.null2str(search.getSelectionYn()).equals("Y")){
			
			SearchSelectionList searchSelectionList = new SearchSelectionList();
			searchSelectionList.setSrchMasterOdJobNo(search.getMasterOdJobNo());
			searchSelectionList.setSrchEntCode(search.getEntCode());
			searchSelectionList.setSrchType("updateHsCode");
			searchSelectionList.setCryptographyYn(search.getCryptographyYn());
			searchSelectionList.setSearchType(searchType);
			
			Util.accessControlDatabase(searchSelectionList, null, true, null, "", null, "");
			List<Selection> list = selectMngMapper.selectSelectionList(searchSelectionList);
			
			SqlSession sqlSession = null;
			try {
				int cnt = 1;
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				SelectMngMapper selectMngMapper2 = sqlSession.getMapper(SelectMngMapper.class);
				if(list != null && list.size() > 0){
					for(Selection selection : list){
						//1.	개인통관고유부호 존재 , 제출번호 없는 건 생성
						//2.	용도구분이 회사인 경우 제출번호 없는 건 생성.  - 26.03.26
						if((!Util.isEmpty(selection.getCustomsId()) && Util.isEmpty(selection.getSimpleDocNo())) || 
								("2".equals(selection.getCdUseType())) && Util.isEmpty(selection.getSimpleDocNo())) {
							if("Y".equals(selection.getDocumentYn())) {
								if(!"".equals(adDocNo)) {
									selection.setSimpleDocNo(adDocNo);
								}
							}else {
								if(!"".equals(aeDocNo)) {
									selection.setSimpleDocNo(aeDocNo);
								}
							}
							
							if(!"".equals(selection.getSimpleDocNo()) && selection.getSimpleDocNo() != null) {
								selectMngMapper2.updateSelectionSimpleDocNo(selection);
								hblList.add(selection.getHblNo());
							}
						}
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
		search.setSelectionList(null);
		search.setSelectionItemList(null);
		processLogs.setEntCode(search.getEntCode());
		processLogs.setEntBusinessCode(search.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(search.getBound());
		processLogs.setBoundType(search.getBoundType());
		processLogs.setProcessUserId(search.getUpdateUserId());
		processLogs.setLogJobType("UPDATE");
		processLogs.setJobNo(search.getMasterOdJobNo());
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setResult("simpleDocNo Update");
		processLogs.setEntBusinessPrintName(search.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(search.getLogUserName());
		processLogs.setProcessUserNameEng(search.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		if(hblList.size() > 0) {
			hblNoMap.put("hblList", hblList.size());
			orderSaveResult.setResultData(hblNoMap);
			orderSaveResult.setResultCode("A11299"); /** 총 #1#건 변경되었습니다. */
		}
		return orderSaveResult;
	}
	
	/**
	 * 통관목록 자체선별(기초) 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult insertSelfSelection(SelfSelection selfSelection) throws Exception {
		Util.accessControlDatabase(selfSelection, "gmtTime", false, null, null, null, null);
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		selectMngMapper.insertSelfSelection(selfSelection);
		orderSaveResult.setResultCode(selfSelection.getJobNo());
		return orderSaveResult;
	}
	
	/**
	 * 통관목록 자체선별(기초) 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateSelfSelection(SelfSelection selfSelection, HttpServletRequest request) throws Exception {
		Util.accessControlDatabase(selfSelection, "gmtTime", false, null, "", null, "");
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		SelfSelection oldSelfSelection = selectMngMapper.selectSelfSelection(selfSelection);
		boolean insertLog = false;
		if(oldSelfSelection != null){
			if(!oldSelfSelection.getSelectionTarget().equals(selfSelection.getSelectionTarget())
				|| !oldSelfSelection.getDataField().equals(selfSelection.getDataField())
				|| !oldSelfSelection.getKeyword().equals(selfSelection.getKeyword())
				|| !oldSelfSelection.getOperator().equals(selfSelection.getOperator())
				|| !oldSelfSelection.getDocumentYn().equals(selfSelection.getDocumentYn())){
				insertLog = true;
			}
		}
		
		selectMngMapper.updateSelfSelection(selfSelection);
		orderSaveResult.setResultCode(selfSelection.getJobNo());
		if(insertLog){
			// 처리 로그 기록
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(selfSelection.getEntCode());
			processLogs.setEntBusinessCode(selfSelection.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(selfSelection.getBound());
			processLogs.setBoundType(selfSelection.getBoundType());
			processLogs.setProcessUserId(selfSelection.getUpdateUserId());
			processLogs.setLogJobType("UPDATE");
			processLogs.setJobFuncType("UPDATE");
			processLogs.setDescription("self selection info update");
			processLogs.setJobLog(new Gson().toJson(selfSelection));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(selfSelection.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(selfSelection.getLogUserName());
			processLogs.setProcessUserNameEng(selfSelection.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}
		
		return orderSaveResult;
	}
	
	/**
	 * 통관목록 자체선별(기초) 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult deleteSelfSelection(DeleteSelfSelection delete, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		selectMngMapper.deleteSelfSelection(delete);
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		delete.setJobNoList(null);
		processLogs.setEntCode(delete.getEntCode());
		processLogs.setEntBusinessCode(delete.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(delete.getBound());
		processLogs.setBoundType(delete.getBoundType());
		processLogs.setProcessUserId(delete.getUpdateUserId());
		processLogs.setLogJobType("DELETE");
		processLogs.setJobFuncType("UPDATE");
		processLogs.setDescription("goods selection info delete");
		processLogs.setJobLog(new Gson().toJson(delete));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(delete.getLogUserName());
		processLogs.setProcessUserNameEng(delete.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		return orderSaveResult;
	}
	
	/**
	 * 검사선별관리 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public SelectionList selectInspectionList(SearchSelectionList search) throws Exception {
		SelectionList selectionList = new SelectionList();
		List<String> optionList = search.getSrchInspectionOption();
		if(optionList.size() == 0){
			selectionList.setSelectionList(new ArrayList<Selection>());
			selectionList.setTotal(0);
			return selectionList;
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		Map<String, Object> map = BeanUtils.describe(search);
		for(String option : optionList){
			map.put(option, "Y");
		}
		map.put("page", null);
		map.put("sort", search.getSort());
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		if(searchNoList.size() > 1){
			map.put("searchType", "multi");
			map.put("searchNoList", searchNoList);
		}else{
			map.put("searchType", "");
			map.put("srchNo", Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}
		
		List<Selection> list = selectMngMapper.selectInspectionList(map);
		selectionList.setSelectionList(list);
		if(list.size() > 0) {
			selectionList.setTotal(list.get(0).getTotal());
		}else {
			selectionList.setTotal(0);
		}
		return selectionList;
	}
	
	/**
	 * 검사선별관리 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateInspectionList(SaveGoodsSelectionInfo goodsSelection, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		SqlSession sqlSession = null;
		try {
			int cnt = 1;
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			SelectMngMapper selectMngMapper2 = sqlSession.getMapper(SelectMngMapper.class);
			Selection selection = new Selection();
			selection.setInspectionYn("Y");
			for(String odJobNo : goodsSelection.getOdJobNoList()){
				selection.setOdJobNo(odJobNo);
				selectMngMapper2.updateInspectionList(selection);
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
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(goodsSelection.getEntCode());
		processLogs.setEntBusinessCode(goodsSelection.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(goodsSelection.getBound());
		processLogs.setBoundType("IMP");
		processLogs.setProcessUserId(goodsSelection.getUpdateUserId());
		processLogs.setLogJobType("UPDATE");
		processLogs.setJobFuncType("UPDATE");
		processLogs.setDescription("Update Inspection List");
		processLogs.setJobLog(new Gson().toJson(goodsSelection));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(goodsSelection.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(goodsSelection.getLogUserName());
		processLogs.setProcessUserNameEng(goodsSelection.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		return orderSaveResult;
	}
	
	/**
	 * 통관목록 자체선별(기초) 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public SelfSelectionList selectSelfSelectionList(SearchSelfSelectionList search) throws Exception {
		SelfSelectionList selfSelectionList = new SelfSelectionList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		Util.accessControlDatabase(search, "srchDateGbn|srchDateFormat", true, null, "", null, "");
		List<SelfSelection> list = selectMngMapper.selectSelfSelectionList(search);
		selfSelectionList.setSelfSelectionList(list);
		if(list.size() > 0) {
			selfSelectionList.setTotal(list.get(0).getTotal());
		}else {
			selfSelectionList.setTotal(0);
		}
		return selfSelectionList;
	}
	
	/**
	 * 통관목록 자체선별(기초) 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public SelfSelection selectSelfSelection(SearchSelfSelection search) throws Exception {
		return selectMngMapper.selectSelfSelection(search);
	}
	
	/**
	 * 통관목록 자체선별(기초) 엑셀업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult selfSelectionExcelUpload(String entCode, String updateUserId, String gmtTime, File file, String userBusinessCode, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		if(file != null){
			InputStream is = null;
			try {
				String[] fieldName =	new String[]{"selectionTarget",	"operator",	"dataField",	"keyword",	"remarks",	"documentYn"};	
				String[] fieldTitle =	new String[]{"선별종류",		"검색방법",	"대상항목",		"적용내용",	"비고",		"서류여부"};
				String[] fieldLength =	new String[]{"100",				"100",		"100",			"100",		"100",		"1"};
				String[] fieldType =	new String[]{"S",				"S",		"S",			"S",		"S",		"S"};
				
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
				boolean nullChkFlag;
				
				List<Map<String, Object>> chkList = selectMngMapper.selectSelfSelectionListExcelUploadChk();
				String selectionTargetStr = null, operatorStr = null, dataFieldStr = null, dataFieldStr3y;
				if(chkList == null || chkList.size() != 3){
					orderSaveResult.setResultCode("ERROR");
					return orderSaveResult;
				}else{
					for(Map<String, Object> chkMap : chkList) {
						if(Util.null2str(chkMap.get("COL_TITLE")).equals("SEARCH_TYPE")){
							selectionTargetStr = "|" + Util.null2str(chkMap.get("COL_TYPE_VALUE")) + "|";
						}else if(Util.null2str(chkMap.get("COL_TITLE")).equals("OPERATOR")){
							operatorStr = "|" + Util.null2str(chkMap.get("COL_TYPE_VALUE")) + "|";
						}else if(Util.null2str(chkMap.get("COL_TITLE")).equals("CHECK_FIELD")){
							dataFieldStr = "|" + Util.null2str(chkMap.get("COL_TYPE_VALUE")) + "|";
						}
					}
					if("UPSXK".equals(entCode) || "YHTXX".equals(entCode)){
						dataFieldStr3y = "|Pieces|DestCountry|EcmType|CommodityItem|ShipperBusinessId|ShipperName|ShipperAddress|ShipperTel|ConsigneeName|ConsigneeAddress|ConsigneeTel|";
					}else{
						dataFieldStr3y = dataFieldStr + "|Pieces|DestCountry|EcmType|ShipperBusinessId|";
					}
				}
				
				if ("xls".equals(extension)) {
					HSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						nullChkFlag = false;
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}else{
								if(!fieldName[j].equals("remarks")){
									nullChkFlag = true;
								}
							}
						}
						
						dataMap.put("entCode", entCode);
						this.selfSelectionExcelUploadErrorCheck(dataMap, i, nullChkFlag, selectionTargetStr, operatorStr, dataFieldStr, dataFieldStr3y, errMsg);
						dataMap.put("updateUserId", updateUserId);
						dataMap.put("gmtTime", gmtTime);
						dataList.add(dataMap);
					}
				}else{
					XSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						nullChkFlag = false;
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}else{
								if(!fieldName[j].equals("remarks")){
									nullChkFlag = true;
								}
							}
						}
						
						dataMap.put("entCode", entCode);
						this.selfSelectionExcelUploadErrorCheck(dataMap, i, nullChkFlag, selectionTargetStr, operatorStr, dataFieldStr, dataFieldStr3y, errMsg);
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
				
				//TODO 파일삭제?
				if(dataList.size() > 0){
					
					SqlSession sqlSession = null;
					try {
						sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
						SelectMngMapper selectMngMapperBatch = sqlSession.getMapper(SelectMngMapper.class);
						int cnt = 1;
						for(Map<String, Object> tmpMap : dataList){
							selectMngMapperBatch.insertSelfSelectionExcelUpload(tmpMap);
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
	public void selfSelectionExcelUploadErrorCheck(Map<String, Object> dataMap, int idx, boolean nullChkFlag, String selectionTargetStr, String operatorStr, String dataFieldStr, String dataFieldStr3y, StringBuffer errMsg){
		String selectionTarget = Util.null2str(dataMap.get("selectionTarget"));
		String dataField = Util.null2str(dataMap.get("dataField"));
		String operator = Util.null2str(dataMap.get("operator"));
		String keyword = Util.null2str(dataMap.get("keyword"));
		String entCode = Util.null2str(dataMap.get("entCode"));

		if("JK".equals(selectionTarget)){
			if(!"DetailItemName".equals(dataField)){
				errMsg.append(idx + "행 : 선별종류가 'JK'일 경우 대상항목은 'DetailItemName'만 입력 가능합니다.<br />");
			}
			if(!"0".equals(operator)){
				errMsg.append(idx + "행 : 선별종류가 'JK'일 경우 검색방법은 '0'만 입력 가능합니다.<br />");
			}
		}else if(selectionTargetStr.indexOf("|" + selectionTarget + "|") == -1){
			errMsg.append(idx + "행의 선별종류를 확인하십시오.<br />");
		}
		
		if(("UPSXK".equals(entCode) || "YHTXX".equals(entCode)) && "|ShipperName|ShipperAddress|ShipperTel|ConsigneeName|ConsigneeAddress|ConsigneeTel|".indexOf("|" + dataField + "|") > -1){
			if(!"0".equals(operator) && !"1".equals(operator)){
				errMsg.append(idx + "행 : 대상항목이 '" + dataField + "'일 경우 검색방법은 '0', '1' 만 입력 가능합니다.<br />");
			}
		}else if("|HsCode|CustomsId|DestCountry|ShipperBusinessId|EcmType|".indexOf("|" + dataField + "|") > -1){
			if(!"0".equals(operator)){
				errMsg.append(idx + "행 : 대상항목이 '" + dataField + "'일 경우 검색방법은 '0'만 입력 가능합니다.<br />");
			}
			if("EcmType".equals(dataField) && !"D".equals(keyword)){
				errMsg.append(idx + "행 : 대상항목이 '" + dataField + "'일 경우 적용내용은 'D'만 입력 가능합니다.<br />");
			}
		}else if("Pieces".equals(dataField)){
			if(!"2".equals(operator)){
				errMsg.append(idx + "행 : 대상항목이 'Pieces'일 경우 검색방법은 '2'만 입력 가능합니다.<br />");
			}
			if(!"2".equals(keyword)){
				errMsg.append(idx + "행 : 대상항목이 'Pieces'일 경우 적용내용은 '2'만 입력 가능합니다.<br />");
			}
		}else if("CommodityItem".equals(dataField)){
			if(!"1".equals(operator)){
				errMsg.append(idx + "행 : 대상항목이 'CommodityItem'일 경우 검색방법은 '1'만 입력 가능합니다.<br />");
			}
		}else if("ActualCstName".equals(dataField)){
			if(!"OB".equals(selectionTarget)){
				errMsg.append(idx + "행 : 대상항목이 'ActualCstName'일 경우 선별종류는 'OB'만 입력 가능합니다.<br />");
			}
		}else{
			if(operatorStr.indexOf("|" + operator + "|") == -1){
				errMsg.append(idx + "행의 검색방법을 확인하십시오.<br />");
			}else{
				if(operator.equals("2") || operator.equals("3")){
					try{
						Util.null2dbl(keyword);
					}catch(Exception e){
						errMsg.append(idx + "행 : 검색방법이 '2', '3'일경우 적용내용은 숫자만 입력 가능합니다.<br />");
					}
				}
			}
		}
		if("WT".equals(dataField)){
			if(!"2".equals(operator)){
				errMsg.append(idx + "행 : 대상항목이 'WT'일 경우 검색방법은 '2'만 입력 가능합니다.<br />");
			}
		}
		
		if(!"ActualCstName".equals(dataField)){
			if("3Y".equals(selectionTarget)){
				if(dataFieldStr3y.indexOf("|" + dataField + "|") == -1){
					if(dataFieldStr.indexOf("|" + dataField + "|") > -1){
						errMsg.append(idx + "행 : 선별종류 '3Y'가 아닐경우에만 대상항목 '" + dataField + "' 사용 가능합니다.<br />");
					}else{
						errMsg.append(idx + "행의 대상항목을 확인하십시오.<br />");
					}
				}
			}else{
				if(dataFieldStr.indexOf("|" + dataField + "|") == -1){
					if(dataFieldStr3y.indexOf("|" + dataField + "|") > -1){
						errMsg.append(idx + "행 : 선별종류 '3Y'일 경우에만 대상항목 '" + dataField + "' 사용 가능합니다.<br />");
					}else{
						errMsg.append(idx + "행의 대상항목을 확인하십시오.<br />");
					}
				}
			}
		}
		
		
		if(nullChkFlag){
			errMsg.append(idx + "행의 필수값이 없습니다.<br />");
		}
	}
	
	/**
	 * 통관목록 자동선별(기초) 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult insertGoodsSelectionInfo(SaveGoodsSelectionInfo goodsSelectionInfo) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		SearchGoodsSelectionInfo searchGoodsSelectionInfo = new SearchGoodsSelectionInfo();
		searchGoodsSelectionInfo.setSrchItemName(goodsSelectionInfo.getGoodsSelectionInfoList().get(0).getItemName());
		searchGoodsSelectionInfo.setDefaultSort("ITEM_NAME");
		Util.accessControlDatabase(searchGoodsSelectionInfo, "srchDateGbn|srchDateFormat", true, null, "", null, "");
		List<GoodsSelectionInfo> list = selectMngMapper.selectGoodsSelectionList(searchGoodsSelectionInfo);
		if(list != null && list.size() > 0){
			orderSaveResult.setResultCode("A00611");	/** 중복되는 명칭이 있습니다. */
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("appendStr", list.get(0).getItemName() + " : ");
			orderSaveResult.setResultData(resultData);
		}else{
			Util.accessControlDatabase(goodsSelectionInfo, "gmtTime", false, null, null, null, null);
			orderSaveResult.setResultData(selectMngMapper.insertGoodsSelectionInfo(goodsSelectionInfo.getGoodsSelectionInfoList().get(0)));
		}
		return orderSaveResult;
	}
	
	/**
	 * 통관목록 자동선별(기초) Backup File 업데이트
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult insertGoodsSelectionInfoBackupFile(SaveGoodsSelectionInfo goodsSelectionInfo) throws Exception {
		Util.accessControlDatabase(goodsSelectionInfo, "gmtTime", false, null, null, null, null);
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		/** 품명중복체크 */
		Map<String, Object> chkMap = new HashMap<String, Object>();
		chkMap.put("insertType","backupFileUpdate");
		Map<String, Object> errMap = null;
		String dupItemNameStr;
		List<GoodsSelectionInfo> itemNameList = goodsSelectionInfo.getGoodsSelectionInfoList();
		int listCnt = itemNameList.size();
		if(listCnt > 1000){
			StringBuffer dupItemName = new StringBuffer();
			for(int i = 0; i < listCnt; i+=1000){
				if(i + 1000 < listCnt) {
					chkMap.put("itemNameList", itemNameList.subList(i, i + 1000));
				}else{
					chkMap.put("itemNameList", itemNameList.subList(i, listCnt));
				}
				dupItemNameStr = selectMngMapper.selectGoodsSelectionDupChk(chkMap);
				if(dupItemNameStr != null) {
					dupItemName.append(dupItemNameStr + ", ");
				}
			}
			if(dupItemName.length() > 0){
				errMap = new HashMap<String, Object>();
				errMap.put("itemNameStr", dupItemName.toString().substring(0, dupItemName.length() - 2));
			}
		}else{
			chkMap.put("itemNameList", itemNameList);
			dupItemNameStr = selectMngMapper.selectGoodsSelectionDupChk(chkMap);
			if(dupItemNameStr != null){
				errMap = new HashMap<String, Object>();
				errMap.put("itemNameStr", dupItemNameStr);
			}
		}
		
		if(errMap != null){
			orderSaveResult.setResultCode("A00611");	/** 중복되는 명칭이 있습니다. */
			orderSaveResult.setResultData(errMap);
			return orderSaveResult;
		}
		
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			SelectMngMapper selectMngMapperBatch = sqlSession.getMapper(SelectMngMapper.class);
			int cnt = 1;
			for(GoodsSelectionInfo selectionInfo : itemNameList){
				selectionInfo.setUpdateUserId(goodsSelectionInfo.getUpdateUserId());
				selectionInfo.setGmtTime(goodsSelectionInfo.getGmtTime());
				selectMngMapperBatch.insertGoodsSelectionInfoBackupFile(selectionInfo);
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
		
		return orderSaveResult;
	}
	
	/**
	 * 통관목록 자동선별(기초) 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult updateGoodsSelectionInfo(SaveGoodsSelectionInfo goodsSelectionInfo, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		SearchGoodsSelectionInfo searchGoodsSelectionInfo = new SearchGoodsSelectionInfo();
		searchGoodsSelectionInfo.setSrchItemName(goodsSelectionInfo.getGoodsSelectionInfoList().get(0).getItemName());
		searchGoodsSelectionInfo.setDefaultSort("ITEM_NAME");
		Util.accessControlDatabase(searchGoodsSelectionInfo, "srchDateGbn|srchDateFormat", true, null, "", null, "");
		List<GoodsSelectionInfo> list = selectMngMapper.selectGoodsSelectionList(searchGoodsSelectionInfo);
		GoodsSelectionInfo newData = goodsSelectionInfo.getGoodsSelectionInfoList().get(0);
		boolean insertLog = false;
		if(list != null && list.size() > 0){
			GoodsSelectionInfo oldData = list.get(0);
			if(!Util.null2str(oldData.getCdBtypeYn()).equals(newData.getCdBtypeYn()) || !Util.null2str(oldData.getCdYtypeYn()).equals(newData.getCdYtypeYn()) || !Util.null2str(oldData.getHsCode()).equals(newData.getHsCode())){
				insertLog = true;
			}
		}
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("hsCode", newData.getHsCode());
		tmpMap.put("cdYtypeYn", newData.getCdYtypeYn());
		tmpMap.put("cdBtypeYn", newData.getCdBtypeYn());
		tmpMap.put("itemName", newData.getItemName());
		tmpMap.put("updateUserId", goodsSelectionInfo.getUpdateUserId());
		tmpMap.put("gmtTime", goodsSelectionInfo.getGmtTime());
		Util.accessControlDatabase(tmpMap, "gmtTime", false, null, "", null, "");
		selectMngMapper.updateGoodsSelectionInfo(tmpMap);
		
		if(insertLog){
			// 처리 로그 기록
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(goodsSelectionInfo.getEntCode());
			processLogs.setEntBusinessCode(goodsSelectionInfo.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(goodsSelectionInfo.getBound());
			processLogs.setBoundType(goodsSelectionInfo.getBoundType());
			processLogs.setProcessUserId(goodsSelectionInfo.getUpdateUserId());
			processLogs.setLogJobType("UPDATE");
			processLogs.setJobFuncType("UPDATE");
			processLogs.setDescription("goods selection info update");
			processLogs.setJobLog(new Gson().toJson(goodsSelectionInfo));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(goodsSelectionInfo.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(goodsSelectionInfo.getLogUserName());
			processLogs.setProcessUserNameEng(goodsSelectionInfo.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}
		
		return orderSaveResult;
	}
	
	/**
	 * 통관목록 자동선별(기초) 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult deleteGoodsSelectionInfo(SaveGoodsSelectionInfo goodsSelectionInfo, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		selectMngMapper.deleteGoodsSelectionInfo(goodsSelectionInfo);
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		goodsSelectionInfo.setGoodsSelectionInfoList(null);
		processLogs.setEntCode(goodsSelectionInfo.getEntCode());
		processLogs.setEntBusinessCode(goodsSelectionInfo.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(goodsSelectionInfo.getBound());
		processLogs.setBoundType(goodsSelectionInfo.getBoundType());
		processLogs.setProcessUserId(goodsSelectionInfo.getUpdateUserId());
		processLogs.setLogJobType("DELETE");
		processLogs.setJobFuncType("UPDATE");
		processLogs.setDescription("goods selection info delete");
		processLogs.setJobLog(new Gson().toJson(goodsSelectionInfo));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(goodsSelectionInfo.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(goodsSelectionInfo.getLogUserName());
		processLogs.setProcessUserNameEng(goodsSelectionInfo.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		return orderSaveResult;
	}
	
	
	/**
	 * 통관목록 자동선별(기초) 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public GoodsSelectionInfoList selectGoodsSelectionInfoList(SearchGoodsSelectionInfo search) throws Exception {
		GoodsSelectionInfoList goodsSelectionInfoList = new GoodsSelectionInfoList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		Util.accessControlDatabase(search, "srchDateGbn|srchDateFormat", true, null, "", null, "");
		List<GoodsSelectionInfo> list = selectMngMapper.selectGoodsSelectionList(search);
		goodsSelectionInfoList.setGoodsSelectionInfoList(list);
		if(list.size() > 0) {
			goodsSelectionInfoList.setTotal(list.get(0).getTotal());
		}else {
			goodsSelectionInfoList.setTotal(0);
		}
		return goodsSelectionInfoList;
	}
	
	/**
	 * 통관목록 자동선별(기초) 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public GoodsSelectionInfo selectGoodsSelectionInfo(String entCode, String itemName) throws Exception {
		SearchGoodsSelectionInfo search = new SearchGoodsSelectionInfo();
		search.setSrchEntCode(entCode);
		search.setSrchItemName(itemName);
		search.setDefaultSort("ITEM_NAME");
		Util.accessControlDatabase(search, null, true, null, "", null, "");
		return selectMngMapper.selectGoodsSelectionList(search).get(0);
	}
	
	/**
	 * 통관목록 자동선별(기초) 엑셀업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult goodsSelectionInfoExcelUpload(String entCode, String updateUserId, String gmtTime, File file, String userBusinessCode, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		if(file != null){
			InputStream is = null;
			try {
				String[] fieldName =	new String[]{"ITEM_NAME",	"HS_CODE",		"CD_YTYPE_YN",	"CD_BTYPE_YN"};	
				String[] fieldTitle =	new String[]{"품명",		"허용물품코드",	"목록",			"배제"};
				String[] fieldLength =	new String[]{"pass",		"10",			"1",			"1"};
				String[] fieldType =	new String[]{"S",			"S",			"S",			"S"};
				
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
				
				List<String> itemNameList = new ArrayList<String>();
				Map<String, Object> dataMap = null;
				
				Map<String, String> nameChkMap = new HashMap<String, String>();
				List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
				StringBuffer errMsg = new StringBuffer();
				String itemName;
				
				if ("xls".equals(extension)) {
					HSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						
						itemName = Converter.changeSpeialChar(Util.null2str(dataMap.get("ITEM_NAME")), "").replaceAll(" ", "");
						if(nameChkMap.get(itemName) != null){
							continue;
						}else{
							nameChkMap.put(itemName, "Y");
						}
						
						if((Util.null2str(dataMap.get("CD_YTYPE_YN")).equals("Y") && Util.null2str(dataMap.get("CD_BTYPE_YN")).equals("Y")) || (!Util.null2str(dataMap.get("CD_YTYPE_YN")).equals("Y") && !Util.null2str(dataMap.get("CD_BTYPE_YN")).equals("Y"))){
							errMsg.append(i + "행 : 목록/배제 중 하나가 Y여야 합니다.<br />");
						}
						if(!itemName.equals("")){
							dataMap.put("entCode", entCode);
							dataMap.put("updateUserId", updateUserId);
							dataMap.put("gmtTime", gmtTime);
							if(itemName.length() > 200){
								itemName = itemName.substring(0, 200);
							}
							dataMap.put("ITEM_NAME", itemName);
							dataList.add(dataMap);
							itemNameList.add(itemName);
						}else{
							errMsg.append(i + "행의 필수값이 없습니다.<br />");
						}
					}
				}else{
					XSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						
						itemName = Converter.changeSpeialChar(Util.null2str(dataMap.get("ITEM_NAME")), "").replaceAll(" ", "");
						if(nameChkMap.get(itemName) != null){
							continue;
						}else{
							nameChkMap.put(itemName, "Y");
						}
						
						if((Util.null2str(dataMap.get("CD_YTYPE_YN")).equals("Y") && Util.null2str(dataMap.get("CD_BTYPE_YN")).equals("Y")) || (!Util.null2str(dataMap.get("CD_YTYPE_YN")).equals("Y") && !Util.null2str(dataMap.get("CD_BTYPE_YN")).equals("Y"))){
							errMsg.append(i + "행 : 목록/배제 중 하나가 Y여야 합니다.<br />");
						}
						if(!itemName.equals("")){
							dataMap.put("entCode", entCode);
							dataMap.put("updateUserId", updateUserId);
							dataMap.put("gmtTime", gmtTime);
							if(itemName.length() > 200){
								itemName = itemName.substring(0, 200);
							}
							dataMap.put("ITEM_NAME", itemName);
							dataList.add(dataMap);
							itemNameList.add(itemName);
						}else{
							errMsg.append(i + "행의 필수값이 없습니다.<br />");
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
				if(dataList.size() > 0){
					
					/** 품명중복체크 */
					Map<String, Object> chkMap = new HashMap<String, Object>();
					Map<String, Object> errMap = null;
					String dupItemNameStr;
					int listCnt = itemNameList.size();
					if(listCnt > 1000){
						StringBuffer dupItemName = new StringBuffer();
						for(int i = 0; i < listCnt; i+=1000){
							if(i + 1000 < listCnt) {
								chkMap.put("itemNameList", itemNameList.subList(i, i + 1000));
							}else{
								chkMap.put("itemNameList", itemNameList.subList(i, listCnt));
							}
							dupItemNameStr = selectMngMapper.selectGoodsSelectionDupChk(chkMap);
							if(dupItemNameStr != null) {
								dupItemName.append(dupItemNameStr + ", ");
							}
						}
						if(dupItemName.length() > 0){
							errMap = new HashMap<String, Object>();
							errMap.put("itemNameStr", dupItemName.toString().substring(0, dupItemName.length() - 2));
						}
					}else{
						chkMap.put("itemNameList", itemNameList);
						dupItemNameStr = selectMngMapper.selectGoodsSelectionDupChk(chkMap);
						if(dupItemNameStr != null){
							errMap = new HashMap<String, Object>();
							errMap.put("itemNameStr", dupItemNameStr);
						}
					}
					
					if(errMap != null){
						String[] arr = Util.null2str(errMap.get("itemNameStr")).replaceAll(" ", "").split(",");
						for(String tmpStr : arr) {
							for(int i=0; i<dataList.size(); i++) {
								if(tmpStr.equals(dataList.get(i).get("ITEM_NAME"))) {
									dataList.remove(i);
								}
							}
						}
//						orderSaveResult.setResultCode("A00611");	/** 중복되는 명칭이 있습니다. */
//						orderSaveResult.setResultData(errMap);
//						return orderSaveResult;
					}
					
					
					SqlSession sqlSession = null;
					try {
						sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
						SelectMngMapper selectMngMapperBatch = sqlSession.getMapper(SelectMngMapper.class);
						int cnt = 1;
						for(Map<String, Object> tmpMap : dataList){
							selectMngMapperBatch.insertGoodsSelectionInfoBatch(tmpMap);
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
	 * 통관목록 자동선별 선별마감 여부 조회
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult checkSelectionYn(UpdateSelectionList search, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		if(!Util.null2str(selectMngMapper.selectSelectionYn(search)).equals("Y")){
			orderSaveResult.setResultCode("A11334");	/** 마감된 마스터가 아닙니다. */
		}
		return orderSaveResult;
	}
	
	/**
	 * 목록선별 리스트 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public GoodsSelectionMailList selectGoodsSelectionMailList(SearchSelectionList search, HttpServletRequest request) throws Exception {
		List<GoodsSelectionMail> mailList = selectMngMapper.selectGoodsSelectionMailList(search);
		GoodsSelectionMailList goodsSelectionMailList = new GoodsSelectionMailList();
		goodsSelectionMailList.setGoodsSelectionMailList(mailList);
		return goodsSelectionMailList;
	}
	
	/**
	 * 목록선별 리스트 수정
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult sendGoodsSelectionMail(GoodsSelectionMailList search, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		UpdateSelectionList updateSelectionList = new UpdateSelectionList();
		Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);
		updateSelectionList.setMasterOdJobNo(search.getMasterOdJobNo());
		if(!Util.null2str(selectMngMapper.selectSelectionYn(updateSelectionList)).equals("Y")){
			orderSaveResult.setResultCode("A11334");	/** 마감된 마스터가 아닙니다. */
			return orderSaveResult;
		}
		
		// 처리 로그 기록
		List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
		Map<String, Object> logMap = new HashMap<String, Object>();
		logMap.put("entCode", search.getEntCode());
		logMap.put("entBusinessCode", search.getUserBusinessCode());
		logMap.put("solution", "ECM");
		logMap.put("bound", search.getBound());
		logMap.put("boundType", search.getBoundType());
		logMap.put("sendType", "EMAIL");
		logMap.put("mailCodeRef", search.getMasterOdJobNo());
		logMap.put("MAIL_SENDER_ID", "GONE@YHT.CO.KR");
		logMap.put("MAIL_SENDER_NAME", search.getUpdateUserId());
		logMap.put("gmtTime", search.getGmtTime());
		logMap.put("userId", search.getUpdateUserId());
				
		String receiver = "";
		String subject = "선별마감 E-MAIL 안내"; 
		String sendMsg = "";
		List<GoodsSelectionMail> mailList = search.getGoodsSelectionMailList();
		
		if(mailList != null && mailList.size() > 0) {
			for(GoodsSelectionMail mail : mailList) {
				// 메일 내용 세팅
				sendMsg = "";
				sendMsg += "안녕하세요<br/>";
				sendMsg += search.getEntName() + "입니다.<br/><br/>";
				sendMsg += mail.getCustCode() + "의 선별마감 되었습니다.<br/><br/>";
				sendMsg += "입항일자: " + Util.null2str(mail.getArDate()) + "<br/>";
				sendMsg += "마스터번호: " + Util.null2str(mail.getMblNo()) + " / 편명: " + Util.null2str(mail.getVesselFlight()) + "<br/>";
				sendMsg += "일반건: " + Util.null2int(mail.getCdNtypeCnt()) + " 건<br/>";
				sendMsg += "배제건: " + Util.null2int(mail.getCdBtypeCnt()) + " 건<br/>";
				sendMsg += "일반+배제: " + (Util.null2int(mail.getCdNtypeCnt()) + Util.null2int(mail.getCdBtypeCnt())) + " 건<br/><br/>";
				sendMsg += "G1-Express.com 확인하시어 수입신고 부탁드립니다.<br/><br/>감사합니다.";
				
				// 메일 전송
				if(!"".equals(Util.null2str(mail.getCbEmail())) && !"".equals(Util.null2str(mail.getCbName()))) {
					receiver = mail.getCbEmail();
					MailSmtpSender sender = new MailSmtpSender(host, port, username, EncryptionUtils.decryptAES(password, rawKey));
					String senderName = profile.toUpperCase();
					if(senderName.contains("PROD")){
						senderName = "G1Express";
					}
					try {
						sender.sendSmtpMail("GONE@YHT.CO.KR", "G1Express", new String[] {receiver}, subject, sendMsg);
						logMap.put("sendYn", "Y");
						logMap.put("sendStatus", "SEND");
						orderSaveResult.setResultCode("A00320");
					}catch(Exception e){
						String sendStatusRemark = e.getMessage();
						if (sendStatusRemark.length() > 500) {
							sendStatusRemark = sendStatusRemark.substring(0, 480);
						}
						logMap.put("sendYn", "N");
						logMap.put("sendStatus", "FAIL");
						logMap.put("sendStatusRemark", sendStatusRemark);
						orderSaveResult.setResultCode("A00375");
					}
					logMap.put("MAIL_SUBJECT", subject);
					logMap.put("MAIL_CONTENTS", sendMsg);
					logMap.put("MAIL_RECEIVER_ID", mail.getCbEmail());
					logList.add(logMap);
				}
			}
		}
		
		// 로그
		if(logList.size() > 0) {
			int seq = 0;
			for(Map<String, Object> map : logList) {
				map.put("seqNoId", commonMapper.selectMailSeqNoId(seq++));
				commonMapper.insertMailLog(map);
			}
		}
		
		return orderSaveResult;
	}
}
