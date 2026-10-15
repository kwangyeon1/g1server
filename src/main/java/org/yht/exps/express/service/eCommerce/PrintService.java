package org.yht.exps.express.service.eCommerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.eCommerce.PrintMapper;
import org.yht.exps.express.model.eCommerce.print.PrintInfo;
import org.yht.exps.express.model.eCommerce.print.SearchPrintInfo;
import org.yht.util.Prop;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PrintService {

	@Autowired
	PrintMapper printMapper;
	
	/**
	 * B/L 라벨 출력정보 조회
	 * @param SearchPrintInfo
	 * @return List<PrintInfo>
	 * @throws Exception
	 */
	public List<PrintInfo> selectPrintInfoLabel(SearchPrintInfo search) throws Exception {
//		Util.accessControlDatabase(search, "srchRptFileName", false, null, null, null, null);
		List<PrintInfo> printInfo = printMapper.selectPrintInfoLabel(search);
		return printInfo;
	}
	
	/**
	 * Barcode 출력 임시 테이블 저장
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> saveBarcodeFromUbi(SearchPrintInfo search) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> gridList = null; 
		Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);
		
		if(!"".equals(Util.null2str(search.getPrintFormCode()))){
			// 기존 저장된 인쇄정보 삭제(User ID, Print Form Code 기준)
			printMapper.deletePrintFormUbi(search);
			Map<String, Object> oblData = new HashMap<String, Object>();
						
			gridList = search.getGrid();
			
			if(gridList != null && gridList.size() > 0){
				
			}else{
				gridList = new ArrayList<Map<String, Object>>(); 
				gridList.add((Map<String, Object>) search);
			}
			
			List<Map<String, Object>> printFormItemList = printMapper.getPrintFormItemList(search);
			
			if(printFormItemList != null && printFormItemList.size() > 0) {
				int tmpDocCount = 0;
				int printFormItemcount = Util.null2int(printFormItemList.size());
				int count = 0;
				
				if(gridList != null && gridList.size() > 0){					
					for(Map<String, Object> gridMap : gridList){
						gridMap.put("bound", search.getBound());
						gridMap.put("dbDateFormat", search.getDbDateFormat());
						gridMap.put("entBusinessCode", search.getUserEntBusinessCode());
						gridMap.put("entCode", search.getEntCode());
						gridMap.put("gmtTime", search.getGmtTime());
						gridMap.put("printFormCode", search.getPrintFormCode());
						gridMap.put("solution", search.getSolution());
						gridMap.put("srchLangType", search.getSrchLangType());
						gridMap.put("userId", search.getUserId());
						gridMap.put("title", search.getTitle());
						gridMap.put("cryptographyYn", search.getCryptographyYn());
					
						oblData = printMapper.selectManualBarcodeList(gridMap);
						oblData.put("FROM_DOC_PRINT_NO", 1);
						oblData.put("TO_DOC_PRINT_NO", oblData.get("PIECES"));
						oblData.put("DOC_PRINT_COUNT", oblData.get("PIECES"));
						
						//label-1건출력 체크한 경우 수량대로 출력하지 않고 1장만 출력할 수 있도록 추가
						if("Y".equals(Util.null2str(search.getPrintOnePage()))){
							oblData.put("TO_DOC_PRINT_NO", 1);
							oblData.put("DOC_PRINT_COUNT", 1);
						}

						if(oblData != null && oblData.size() > 0){
							
							//출력 아이템
							for(int j = 0; j < printFormItemcount; j++) {
								Map<String, Object> printFormItemData = printFormItemList.get(j);
								
								printFormItemData.put("ENT_CODE", Util.null2str(gridMap.get("entCode")));
								printFormItemData.put("ENT_BUSINESS_CODE", Util.null2str(gridMap.get("entBusinessCode")));
								printFormItemData.put("USER_ID", Util.null2str(gridMap.get("userId")));
								printFormItemData.put("HBL_NO_BARCODE_VAL", Util.null2str(oblData.get("HBL_NO")));
								printFormItemData.put("GMT_TIME",gridMap.get("gmtTime"));
								printFormItemData.put("REFERENCE_NO", (tmpDocCount+1));
								printFormItemData.put("DOC_PRINT_COUNT", Util.null2str(oblData.get("DOC_PRINT_COUNT")));
								printFormItemData.put("SEQ_NO", count++ );
								printFormItemData.put("PRINT_NO_FROM", Util.null2int(oblData.get("FROM_DOC_PRINT_NO")));
								printFormItemData.put("PRINT_NO_TO", Util.null2int(oblData.get("TO_DOC_PRINT_NO")));
								
								String controlId = Util.null2str(printFormItemData.get("CONTROL_ID")).toUpperCase();
								String controlIdMapping = Util.null2str(Prop.getMessage(controlId));
								String controlValue = "";
								String fixedValue = Util.null2str(printFormItemData.get("FIXED_VALUE"));
														
								if(!"".equals(Util.null2str(fixedValue))){
									controlValue = fixedValue;
								}else{
									//현재는 ECM_MANUAL_BC_AIR_H_UMAC, ECM_MANUAL_BC_AIR_H_NO만 출력 가능하므로 사용x 부분
									//전자상거래에 인쇄양식 추가돼 필요한 경우 수정해야함
//									if(!"".equals(controlIdMapping)) {
//										if(controlIdMapping.startsWith("F_")) {
//												
//											switch(Util.null2str(controlId)) {
//												case "HBL_NO" :
//													controlValue = Util.null2str(oblData.get("HBL_NO"));
//													break;
//												case "HBL_PCS" :
//													controlValue = Util.null2str(oblData.get("PIECES"));
//													break;
//												case "HBL_WT" :
//													controlValue = Util.null2str(oblData.get("GWT"));
//													break;
//												case "VAL_BL_NO_BARCODE" :
//													controlValue = Util.null2str(oblData.get("HBL_NO"));
//													break;
//												case "ORIG_CODE" :
//													controlValue = Util.null2str(oblData.get("ORIG_CODE"));
//													break;
//												case "DEST_CODE" :
//													controlValue = Util.null2str(oblData.get("DEST_CODE"));
//													break;
//												case "DEST_NAME" :
//													controlValue = Util.null2str(oblData.get("DEST_NAME"));
//													break;
//												case "HBL_PAGE_PCS" :
//													if("EMPTY".equals(search.getPageMode())){
//														controlValue =  "#:PRINT_NO#/" + Util.null2str(oblData.get("DOC_PRINT_COUNT"));
//													}else{
//														controlValue =  "#:PRINT_NO#/" + Util.null2str(oblData.get("PIECES"));
//													}
//													break;
//												case "HBL_PAGE_PCS_ENG" :
//													controlValue =  "#:PRINT_NO#";
//													break;
//												case "SHIPPER_NAME" :
//													controlValue = Util.null2str(oblData.get("SHIPPER_NAME"));
//													break;
//												case "CONSIGNEE_NAME" :
//													controlValue = Util.null2str(oblData.get("CONSIGNEE_NAME"));
//													break;
//												case "CONSIGNEE_ADDR" : 
//													controlValue = Util.null2str(oblData.get("CONSIGNEE_ADDRESS"));
//													break;
//												case "CONSIGNEE_ADDR_A" : 
//													controlValue = Util.null2str(oblData.get("CONSIGNEE_ADDRESS_A"));
//													break;
//												case "CONSIGNEE_HP" : 
//													controlValue = Util.null2str(oblData.get("CONSIGNEE_HP"));
//													break;	
//												case "SHIPPER_ADDR" : 
//													controlValue = Util.null2str(oblData.get("SHIPPER_ADDRESS"));
//													break;
//												case "SHIPPER_ADDR_A" : 
//													controlValue = Util.null2str(oblData.get("SHIPPER_ADDRESS_A"));
//													break;	
//												case "PAYMENT" : 
//													controlValue = Util.null2str(oblData.get("PAYMENT"));
//												case "ITEM_NAME" : 
//													controlValue = Util.null2str(oblData.get("ITEM_NAME"));
//													break;	
//												case "ENT_BUSINESS_NAME" : 
//													controlValue = Util.null2str(oblData.get("ENT_BUSINESS_NAME"));
//													break;	
//												case "Issuing_Carrier" : 
//													controlValue = "";
//													break;
//												case "SHIPPER_TEL" : 
//													controlValue = Util.null2str(oblData.get("SHIPPER_TEL"));
//													break;
//												case "SHIPPER_ATTN" : 
//													controlValue = Util.null2str(oblData.get("SHIPPER_ATTN"));
//													break;
//												case "CONSIGNEE_TEL" : 
//													controlValue = Util.null2str(oblData.get("CONSIGNEE_TEL"));
//													break;
//												case "CONSIGNEE_ATTN" : 
//													controlValue = Util.null2str(oblData.get("CONSIGNEE_ATTN"));
//													break;
//												case "CONSIGNEE_CODE" : 
//													controlValue = Util.null2str(oblData.get("CONSIGNEE_CODE"));
//													break;
//												case "SHIPPER_CODE" : 
//													controlValue = Util.null2str(oblData.get("SHIPPER_CODE"));
//													break;
//												case "BAG_NO" : 
//													controlValue = Util.null2str(oblData.get("BAG_NO"));
//													break;
//												case "BOUND" : 
//													controlValue = Util.null2str(oblData.get("BOUND"));
//													break;
//												case "DELIVERY_NO" : 
//													controlValue = Util.null2str(oblData.get("DELIVERY_NO"));
//													break;
//											}
//											
//											
//										} else if(controlIdMapping.startsWith("FIX_")) {
//											
//											String fixVal = controlIdMapping.replace("FIX_", "");
//											
//											controlValue = fixVal;
//											
//										}else if(controlIdMapping.startsWith("LIST_")) {
//											String[] listArray = controlIdMapping.replace("LIST_", "").split(",");
//											
//											for(int x = 0; x < listArray.length; x++) {
//												controlValue += Util.null2strNoTrim(oblData.get(listArray[x]));
//											}
//										}
//									}else{
										
										switch(Util.null2str(controlId)) {
											case "HBL_NO" :
												controlValue = Util.null2str(oblData.get("HBL_NO"));
												break;
											case "HBL_PCS" :
												controlValue = Util.null2str(oblData.get("PIECES"));
												break;
											case "HBL_WT" :
												controlValue = Util.null2str(oblData.get("GWT"));
												break;
											case "VAL_BL_NO_BARCODE" :
												controlValue = Util.null2str(oblData.get("HBL_NO"));
												break;
											case "ORIG_CODE" :
												controlValue = Util.null2str(oblData.get("ORIG_CODE"));
												break;
											case "DEST_CODE" :
												controlValue = Util.null2str(oblData.get("DEST_CODE"));
												break;
											case "DEST_NAME" :
												controlValue = Util.null2str(oblData.get("DEST_NAME"));
												break;
											case "HBL_PAGE_PCS" :
												if("EMPTY".equals(search.getPageMode())){
													controlValue =  "#:PRINT_NO#/" + Util.null2str(oblData.get("DOC_PRINT_COUNT"));
												}else{
													controlValue =  "#:PRINT_NO#/" + Util.null2str(oblData.get("PIECES"));
												}
												break;
											case "HBL_PAGE_PCS_ENG" :
												controlValue =  "#:PRINT_NO#";
												break;
											case "SHIPPER_NAME" :
												controlValue = Util.null2str(oblData.get("SHIPPER_NAME"));
												break;
											case "CONSIGNEE_NAME" :
												controlValue = Util.null2str(oblData.get("CONSIGNEE_NAME"));
												break;
											case "CONSIGNEE_ADDR" : 
												controlValue = Util.null2str(oblData.get("CONSIGNEE_ADDRESS"));
												break;
											case "CONSIGNEE_ADDR_A" : 
												controlValue = Util.null2str(oblData.get("CONSIGNEE_ADDRESS_A"));
												break;
											case "CONSIGNEE_HP" : 
												controlValue = Util.null2str(oblData.get("CONSIGNEE_HP"));
												break;
											case "SHIPPER_ADDR" : 
												controlValue = Util.null2str(oblData.get("SHIPPER_ADDRESS"));
												break;
											case "SHIPPER_ADDR_A" : 
												controlValue = Util.null2str(oblData.get("SHIPPER_ADDRESS_A"));
												break;	
											case "PAYMENT" : 
												controlValue = Util.null2str(oblData.get("PAYMENT"));
												break;
											case "ITEM_NAME" : 
												controlValue = Util.null2str(oblData.get("ITEM_NAME"));
												break;	
											case "ENT_BUSINESS_NAME" : 
												controlValue = Util.null2str(oblData.get("ENT_BUSINESS_NAME"));
												break;	
											case "OB_DATE" : 
												controlValue = Util.null2str(oblData.get("OB_DATE"));
												break;	
											case "ITEM_GUBN" : 
												controlValue = Util.null2str(oblData.get("ITEM_GUBN"));
												break;	
											case "VESSEL_VOYAGE_M" : 
												controlValue = Util.null2str(oblData.get("VESSEL_VOYAGE_M"));
												break;	
											case "RECV_DATE" : 
												controlValue = Util.null2str(oblData.get("RECV_DATE"));
												break;	
											case "Issuing_Carrier" : 
												controlValue = "";
												break;
											case "SHIPPER_TEL" : 
												controlValue = Util.null2str(oblData.get("SHIPPER_TEL"));
												break;
											case "SHIPPER_ATTN" : 
												controlValue = Util.null2str(oblData.get("SHIPPER_ATTN"));
												break;
											case "CONSIGNEE_TEL" : 
												controlValue = Util.null2str(oblData.get("CONSIGNEE_TEL"));
												break;
											case "CONSIGNEE_ATTN" : 
												controlValue = Util.null2str(oblData.get("CONSIGNEE_ATTN"));
												break;
											case "CONSIGNEE_CODE" : 
												controlValue = Util.null2str(oblData.get("CONSIGNEE_CODE"));
												break;
											case "SHIPPER_CODE" : 
												controlValue = Util.null2str(oblData.get("SHIPPER_CODE"));
												break;
											case "BAG_NO" : 
												controlValue = Util.null2str(oblData.get("BAG_NO"));
												break;
											case "BOUND" : 
												controlValue = Util.null2str(oblData.get("BOUND"));
												break;
											case "DELIVERY_NO" : 
												controlValue = Util.null2str(oblData.get("DELIVERY_NO"));
												break;
										}
										
									}
//								}
								printFormItemData.put("CONTROL_VALUE", controlValue);
								printFormItemList.set(j, printFormItemData);
							}// for printFormItemList끝
							
							//grid 저장
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("list", printFormItemList);
							// 인쇄정보 생성
							printMapper.insertPrintFormUbi(map);
							result.put("result", "success");	
							
							search.setReferenceNo((tmpDocCount+1));
							search.setPrintNo(Util.null2int(oblData.get("FROM_DOC_PRINT_NO")));
							search.setDocPrintCount(Util.null2str(search.getDocPrintCount()));
							
							// 프로시져 count 만큼 
							//awbPrintDao.pComSetBarcode(reqMap);
							
							//tmpDocCount += Util.null2int(oblData.get("DOC_PRINT_COUNT"));
							tmpDocCount ++;
						
						
					}
				}
			
			}
		}
			
			
			
		}
		return result;
	}
	
}