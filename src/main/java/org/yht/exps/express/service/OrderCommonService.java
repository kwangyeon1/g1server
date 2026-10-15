package org.yht.exps.express.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderItem;
import org.yht.util.ExchangeKorEng;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderCommonService {
    
	@Autowired
	CommonMapper commonMapper;
	//로컬,테스트,운영 구분
	@Value("${spring.profiles.active}")
	private String profile;
	/** 
	 * 오더저장공통
	 * saveType : entOrder-도입사오더, cusOrder-고객서비스오더, excelUpload-엑셀업로드(배치), excelUploadItem-엑셀업로드세부품목(배치), consol-콘솔/마스터저장, excelOrderError-엑셀업로드처리내역(하우스),
	 * 			  excelItemError-엑셀업로드처리내역(세부품목수정오더), excelMasterError-엑셀업로드처리내역(마스터) 
	 */
	public boolean orderSave(Order order, String saveType, Map<String, Object> map, int seqNo) throws Exception {
		Util.accessControlDatabase(order, "gmtTime|dateFormat", false, null, null, null, null);
		Map<String, Object> tmpMap = new HashMap<String, Object>(); 
		/** 비엘번호 중복체크 */
		if(map.get("chkDupBlNo") != null){
			tmpMap.put("odJobNo", order.getOdJobNo());
			tmpMap.put("masterOdJobNo", order.getMasterOdJobNo());
			tmpMap.put("blTypeMh", order.getBlTypeMh());
			tmpMap.put("entCode", order.getEntCode());
			if(order.getBlTypeMh().equals("H")){
				tmpMap.put("blNo", order.getHblNo());
			}else if(order.getBlTypeMh().equals("M")){
				tmpMap.put("blNo", order.getMblNo());
			}
			/** COPY(insert)이면서 통관구분이 일반(N)/배제(B)인 경우, 같은 하우스번호라도 HSN이 다르면 중복으로 보지않음 */
			if("H".equals(order.getBlTypeMh()) && "insert".equals(Util.null2str(map.get("action")))
					&& ("N".equals(Util.null2str(order.getCdType())) || "B".equals(Util.null2str(order.getCdType())))
					&& !Util.isEmpty(order.getHsn())){
				tmpMap.put("chkHsn", "Y");
				tmpMap.put("hsn", order.getHsn());
			}
			if(commonMapper.selectChkDupBlNo(tmpMap) > 0){
				map.put("duplicateBlNo", "Y");
				return false;
			}
		}
		
		/** 수정/삭제 가능여부 체크 */
		if(map.get("chkEditOrder") != null){
			tmpMap.clear();
			tmpMap.put("odJobNo", order.getOdJobNo());
			tmpMap = commonMapper.selectOrderChk(tmpMap);
			if(tmpMap != null && tmpMap.size() > 0){
				if(Util.null2int(order.getRegisterUserLevel()) > 1){
					if(Util.null2str(tmpMap.get("CONSOL_YN")).equals("Y")){	/** 하위업체는 콘솔된건 수정불가 */
						map.put("resultCode", "A11247");	/** #1# 콘솔되어 등록할 수 없습니다. */
						return false;
					}
				}else if(Util.null2int(order.getRegisterUserLevel()) == 1){
					if(profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")) {
						if(Util.null2str(tmpMap.get("FINISH_YN")).equals("Y")){    /** 상위업체 마감건, 단 발송수정여부 Y면 허용 */
							map.put("resultCode", "A11237");
							return false;
						}
					}else {
						if(Util.null2str(tmpMap.get("FINISH_YN")).equals("Y")
								&& !"Y".equals(order.getSendModifyYn())){    /** 상위업체 마감건, 단 발송수정여부 Y면 허용 */
							map.put("resultCode", "A11237");
							return false;
						}
					}


				}else if(!"AD".equals(order.getUserAuth()) && !"Y".equals(map.get("chkEditOrderTypeEnt"))){
					if("Y".equals(tmpMap.get("SIMPLE_EDI_SEND_YN")) && "Y".equals(tmpMap.get("CD_TYPE"))){	/** 도입사는 통관목록전송된 목록건 수정불가 */
						map.put("resultCode", "A11333");	/** 통관목록이 전송되어서 처리할 수 없습니다. */
						return false;
					}
				}
				map.put("dbCdType", tmpMap.get("CD_TYPE"));
				map.put("CD_TYPE", tmpMap.get("CD_TYPE"));
				map.put("SIMPLE_DOC_NO", tmpMap.get("SIMPLE_DOC_NO"));
			}
		}
		
		/** 마감체크 */
		if(map.get("chkOrderClose") != null) {
			tmpMap.clear();
			tmpMap.put("masterOdJobNo", order.getMasterOdJobNo());
			tmpMap.put("odJobNo", order.getOdJobNo());
			if(commonMapper.selectChkOrderClose(tmpMap) > 0){
				map.put("closeOrder", "Y");
				return false;
			}
		}
		
		/** 통관목록제출번호 */
		if(map.get("setSimpleDocNo") != null){
			order.setSimpleDocNo(Util.null2str(Util.null2str(map.get("setSimpleDocNo"))));
		}else if(map.get("createSimpleDocNo") != null){
			String ediExpressSign = Util.null2str(map.get("ediExpressSign"));
			
			/** ediExpressSign값이 없는경우 사용하지않고있음    사용하려면 서류여부에 따른 처리 필요함 */
//			if(ediExpressSign.equals("")){
//				/** 제출번호에 사용되는 특송부호 조회 */
//				tmpMap.clear();
//				if(order.getBoundType().equals("EXP")){
//					tmpMap.put("srchEdiCityPort", order.getOrigCode());
//				}else{
//					tmpMap.put("srchEdiCityPort", order.getDestPort());
//				}				
//				tmpMap.put("srchEntCode", order.getEntCode());
//				tmpMap.put("srchEntBusinessCode", order.getEntBusinessCode());
//				tmpMap.put("srchBound", order.getBound());
//				Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmpMap);
//				if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
//					ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN"));
//				}else{
//					if(saveType.equals("entOrder")){
//						map.put("busEdiEcmNull", "Y");
//						return false;
//					}
//				}
//			}
			
			tmpMap.put("seqNo", seqNo);
			tmpMap.put("entCode", order.getEntCode());
			tmpMap.put("gmtTime", order.getGmtTime());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("updateUserId", order.getUpdateUserId());
			tmpMap.put("solution", "ECM");
			tmpMap.put("stockType", "OD");
			tmpMap.put("stockSign", ediExpressSign);
			tmpMap.put("stockSeqLength", "8");
			tmpMap.put("signType", "noHyphen");
			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", "ORDER SIMPLE DOC NO");
			if(order.getBoundType().equals("EXP")){
				if("E".equals(order.getCdType())){
					tmpMap.put("dateLength", "2");
					tmpMap.put("stockDateFormat", "12");
				}else{
					tmpMap.put("dateLength", "4");
					tmpMap.put("stockDateFormat", "112");
				}
			}else{
				tmpMap.put("dateLength", "2");
				tmpMap.put("stockDateFormat", "12");
			}
			order.setSimpleDocNo(commonMapper.selectNextStockMaxNo(tmpMap));
		}
		
		/** origCountry */
		if(map.get("setOrigCountry") != null){
			tmpMap.clear();
			tmpMap.put("entCode", order.getEntCode());
			tmpMap.put("customerCode", order.getCustCodeLv1());
			tmpMap = commonMapper.selectCustomerInfo(tmpMap);
			if(tmpMap != null){
				order.setOrigCountry(Util.null2str(tmpMap.get("AMS_COUNTRY_CODE")));
			}else {
				tmpMap = new HashMap<String, Object>();;
			}
		}
		
		if(saveType.equals("excelUpload") || saveType.equals("excelOrderError")){
			if(Util.isEmpty(order.getShipperCountryCode())){
				order.setShipperCountryCode(order.getOrigCountry());
			}
			if(Util.isEmpty(order.getConsigneeCountryCode())){
				order.setConsigneeCountryCode(order.getDestCountry());
			}
			if(Util.isEmpty(order.getGoodsLocInspectYn()) && order.getBoundType().equals("EXP")){
				order.setGoodsLocInspectYn("N");
			}
		}
		
		/** odJobNo생성 */
		if(map.get("createOdJobNo") != null){
			tmpMap.clear();
			tmpMap.put("seqNo", seqNo);
			tmpMap.put("gmtTime", order.getGmtTime());
			tmpMap.put("entCode", order.getEntCode());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("updateUserId", order.getUpdateUserId());
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
		}
		
		/** 통관타입 값이 없을경우 목록으로 입력 */
		if(Util.isEmpty(order.getCdType()) && !saveType.equals("excelUploadItem")){
			order.setCdType("Y");
		}
		
		/** 수하인 한글명/한글주소 영문변환 */
		if(map.get("setConsigneeAddress") != null){
			String engStr = null;
			if(Util.isEmpty(order.getConsigneeAddress()) && Util.isEmpty(order.getConsigneeAddressAdd())){
				engStr = ExchangeKorEng.convert(order.getConsigneeAddressLoc()) + ExchangeKorEng.convert(order.getConsigneeAddressLocAdd());
				if(engStr.length() > 300){
					engStr = engStr.substring(0, 300);
				}
				order.setConsigneeAddress(engStr);
			}
			if(Util.isEmpty(order.getConsigneeName())){
				engStr = ExchangeKorEng.convert(order.getConsigneeNameLoc());
				if(engStr.length() > 100){
					engStr = engStr.substring(0, 100);
				}
				order.setConsigneeName(engStr);
			}
		}
		/** 항공수출 AMS 정보입력 */
		if((saveType.equals("excelUpload") || saveType.equals("excelOrderError")) && Util.null2str(order.getBound()).equals("AIR") && Util.null2str(order.getBound()).equals("EXP")){
			if(!Util.isEmpty(order.getShipperAddress())){
				if(order.getShipperAddress().length() > 35){
					order.setShipperStreet(order.getShipperAddress().substring(0, 35));
					order.setShipperPlace(order.getShipperAddress().substring(35));
					if(order.getShipperPlace().length() > 60){
						order.setShipperPlace(order.getShipperPlace().substring(0, 60));
					}
				}else{
					order.setShipperStreet(order.getShipperAddress());
					order.setShipperPlace("SHIPPER_ADDRESS");
				}
			}
			if(!Util.isEmpty(order.getConsigneeAddress()) || !Util.isEmpty(order.getConsigneeAddressAdd())){
				String consigneeAddress = Util.null2str(order.getConsigneeAddress()) + Util.null2str(order.getConsigneeAddressAdd());
				if(consigneeAddress.length() > 35){
					order.setConsigneeStreet(consigneeAddress.substring(0, 35));
					order.setConsigneePlace(consigneeAddress.substring(35));
					if(order.getConsigneePlace().length() > 60){
						order.setConsigneePlace(order.getConsigneePlace().substring(0, 60));
					}
				}else{
					order.setConsigneeStreet(consigneeAddress);
					order.setConsigneePlace("CONSIGNEE_ADDRESS");
				}
			}
		}
		
		if(!saveType.equals("excelItemError") && !saveType.equals("excelMasterError") && !saveType.equals("consol")){
			
			/** 홀드처리담당자,처리일자값에따라 홀드처리여부처리 */
//			if(Util.null2str(order.getHoldReleaseAttn()).equals("") && Util.null2str(order.getHoldReleaseDate()).equals("")){
//				order.setHoldReleaseYn("N");
//			}else{
//				order.setHoldReleaseYn("Y");
//			}
			
			/** 통관상태 */
			if(saveType.equals("entOrder") || saveType.equals("cusOrder")){
				String clearanceStatus = "";
				if(Util.null2str(order.getCdType()).equals("N") || Util.null2str(order.getCdType()).equals("B") || 
						(Util.null2str(order.getCdType()).equals("Y") && Util.null2str(order.getDropYn()).equals("Y")) ){
					if(!Util.null2str(order.getCbCode()).equals("")){
						clearanceStatus = "CLS030";	/** 일반/배제건이거나 목록&취하건인 경우 관세사에 값이 있다면 신고의뢰(CLS030) */
					}else {
						clearanceStatus = "CLS010";	/** 일반/배제건이거나 목록&취하건인 경우 관세사에 값이 없다면 신고전(CLS010) */
					}
				}
				if(Util.null2str(map.get("action")).equals("update")){
					String oldClearanceStatus = Util.null2str(map.get("CLEARANCE_STATUS"));
					String oldCbCode = Util.null2str(map.get("CB_CODE"));
					if(clearanceStatus.equals("") && oldClearanceStatus.equals("CLS010")){
						order.setClearanceStatus("");
					}else if(clearanceStatus.equals("CLS030") && (oldClearanceStatus.equals("") || oldClearanceStatus.equals("CLS010"))){	/** 기존 통관상태가 없거나 신고전(CLS010)일경우만 신고의뢰(CLS030)로 저장 */
						order.setClearanceStatus(clearanceStatus);
					}else if(clearanceStatus.equals("CLS010") && oldClearanceStatus.equals("")){	/** 기존 통관상태값이 없을경우만 신고전(CLS010)으로 저장 */
						order.setClearanceStatus(clearanceStatus);
					}else if(!oldCbCode.equals("") && Util.null2str(order.getCbCode()).equals("") && oldClearanceStatus.equals("CLS030")){	/** 관세사에값이 지워질경우는 신고전(CLS010)으로 저장(통관상태가 신고의뢰(CLS030)일경우만) */
						order.setClearanceStatus(clearanceStatus);
					}
				}else{
					order.setClearanceStatus(clearanceStatus);
				}
			}
		}
		if(saveType.equals("excelUpload") || saveType.equals("excelItemError") || saveType.equals("excelOrderError") || saveType.equals("excelUploadItem")){
			/** 세부품목 단가/수량/금액처리 */
			List<OrderItem> itemList = order.getItemList();
			if(itemList != null && itemList.size() > 0){
				double unitPrice;
				double amount;
				int pieces;
				if(saveType.equals("excelUpload") || saveType.equals("excelUploadItem")){
					for(OrderItem orderItem : itemList){
						unitPrice = Util.null2dbl(orderItem.getUnitPrice());
						amount = Util.null2dbl(orderItem.getAmount());
						pieces = Util.null2int(orderItem.getPieces());
						if(unitPrice == 0 && pieces != 0 && amount != 0){	/** 단가X/수량O/금액O : 금액 / 수량 = 단가 */
							orderItem.setUnitPrice(amount / pieces);
						}else if(unitPrice != 0 && pieces != 0 && amount == 0){	/** 단가O/수량O/금액X : 단가 * 수량 = 금액 */
							orderItem.setAmount(unitPrice * pieces);
						}else if(unitPrice != 0 && pieces == 0 && amount != 0){	/** 단가O/수량X/금액O : 금액 / 단가 = 수량 */
							orderItem.setPieces(Util.null2int(amount / unitPrice));
						}else if(unitPrice == 0 && pieces == 0 && amount != 0){	/** 단가X/수량X/금액O : 금액 = 단가, 수량1 */
							orderItem.setUnitPrice(amount);
							orderItem.setPieces(1);
						}else if(unitPrice != 0 && pieces == 0 && amount == 0){	/** 단가O/수량X/금액X  : 단가 = 금액, 수량1 */
							orderItem.setAmount(unitPrice);
							orderItem.setPieces(1);
						}
					}
				}
			}
			if(!saveType.equals("excelItemError") && !saveType.equals("excelUploadItem")){
				/** CUSTOMS_ID_TYPE값이 없을경우 CUSTOMS_ID, CD_USE_TYPE값에 따라 자동입력 */
				if("EXP".equals(order.getBoundType())){
					if(Util.isEmpty(order.getCustomsIdType()) || order.getCustomsIdType().length() < 2 || "01|02|03|04|05".indexOf(order.getCustomsIdType()) == -1){
						String shipperBusinessId = Util.null2str(order.getShipperBusinessId());
						if(shipperBusinessId.length() == 13){
							if("1234".contains(shipperBusinessId.substring(6,7))){
								order.setCustomsIdType("01");	/** 주민등록번호 */
							}else if("5678".contains(shipperBusinessId.substring(6,7))){
								order.setCustomsIdType("03");	/** 외국인등록번호 */
							}else if((shipperBusinessId.startsWith("P") || shipperBusinessId.startsWith("p") || shipperBusinessId.startsWith("O") || shipperBusinessId.startsWith("o"))){
								order.setCustomsIdType("A");	/** 개인통관고유부호 */
							}else{
								order.setCustomsIdType("02");	/** 여권번호 */
							}
						}else if(shipperBusinessId.length() == 10){
							order.setCustomsIdType("04");	/** 사업자번호 */
						}else{
							order.setCustomsIdType("02");	/** 여권번호 */
						}
					}
				}else{
					if(Util.isEmpty(order.getCustomsIdType()) || order.getCustomsIdType().length() != 1 || "ABCDEF".indexOf(Util.null2str(order.getCustomsIdType())) == -1){
						String customsId = Util.null2str(order.getCustomsId());
						/** 2022-05-20메일내용으로 수정 */
						if(customsId.length() == 8 && StringUtils.isNumeric(customsId)){
							order.setCustomsIdType("B");	/** 생년월일 */
						}else if(customsId.length() == 13){
							if((customsId.startsWith("P") || customsId.startsWith("p") || customsId.startsWith("o") || customsId.startsWith("O")) && StringUtils.isNumeric(customsId.substring(1))){
								order.setCustomsIdType("A");	/** 개인통관고유부호 */
							}else if(StringUtils.isNumeric(customsId) && "5678".contains(customsId.substring(6,7))){
								order.setCustomsIdType("E");	/** 외국인등록번호 */
							}
						}else if(customsId.length() == 10 && StringUtils.isNumeric(customsId)){
							order.setCustomsIdType("D");	/** 사업자번호 */
						}else if(customsId.length() == 11 && StringUtils.isNumeric(customsId.substring(4)) && customsId.substring(0, 4).matches("[ㄱ-ㅎ가-힣*]*$")){
							order.setCustomsIdType("C");	/** 통관고유부호 */
						// 2024.09.11 요청자 : 장순문
						}else if(!Util.isEmpty(order.getCustomsId())) {
							order.setCustomsIdType("F");	/** 위의 조건에 해당하지 않고 개인통관고유부호 공란이 아닐 경우 F.여권 번호 으로 분류 */
						}else {
							order.setCustomsIdType("Z");	/** 개인통관고유부호 공란일 경우 Z.데이터 없음 으로 분류 */
						}
					}
				}
			}
		}
		
		/** 세부품목내용 오더에 저장 */
		if(!saveType.equals("excelMasterError") && !saveType.equals("consol")){
			List<OrderItem> itemList = order.getItemList();
			
			if(itemList != null && itemList.size() > 0){
//				double volSum = 0;
				double gwt = 0;
				double amountCur = 0;
				int pieces = 0;
				StringBuffer itemStr = new StringBuffer();
				StringBuffer itemLocStr = new StringBuffer();
				String hsCode = null;
				String currencyCode = null;
				String unitPieces = null;
				for(OrderItem orderItem : itemList){
					if(!Util.isEmpty(order.getCurrencyCode())){
						orderItem.setCurrencyCode(order.getCurrencyCode());
					}
					if(!Util.isEmpty(order.getAllowedItemCode()) && Util.isEmpty(orderItem.getHsCode())){
						orderItem.setHsCode(order.getAllowedItemCode());
					}
					if(!saveType.equals("cusOrder")){
//						volSum += Util.null2dbl(orderItem.getVwt());
						if(!saveType.equals("entOrder")){
							if("2".equals(order.getGwtType()) && (saveType.equals("excelUpload") || saveType.equals("excelItemError") || saveType.equals("excelOrderError"))){
								orderItem.setGwt(Util.null2dbl(orderItem.getGwt()) * 0.45359237);
							}
							gwt += Util.null2dbl(orderItem.getGwt());
							pieces += Util.null2int(orderItem.getPieces());
							amountCur += Util.null2dbl(orderItem.getAmount());
							
							if(Util.null2str(map.get("ecmItemNameAllYn")).equals("Y")){
								if(itemStr.length() < 201){
									itemStr.append("," + Util.null2str(orderItem.getDetailItemName()));
								}
								if(itemLocStr.length() < 201){
									itemLocStr.append("," + Util.null2str(orderItem.getDetailItemNameLoc()));
								}
							}else{
								if(itemStr.length() == 0) {
									itemStr.append(Util.null2str(orderItem.getDetailItemName()));
								}
								if(itemLocStr.length() == 0) {
									itemLocStr.append(Util.null2str(orderItem.getDetailItemNameLoc()));
								}
							}
						}
					}
					if(hsCode == null){
						hsCode = Util.null2str(orderItem.getHsCode());
					}
					if(currencyCode == null){
						currencyCode = Util.null2str(orderItem.getCurrencyCode());
					}
					if(unitPieces == null){
						unitPieces = Util.null2str(orderItem.getUnitPieces());
					}
				}
				
				if(!saveType.equals("cusOrder")){
//					if(volSum > 999999999.999){
//						volSum = 999999999.999;
//					}
//					order.setVwt(volSum);
					
					if(!saveType.equals("entOrder")){
						if(Util.null2dbl(order.getGwt()) == 0){
							if(gwt > 999999999.9999) {
								gwt = 999999999.9999;
							}
							order.setGwt(gwt);
							if(saveType.equals("excelUpload") || saveType.equals("excelItemError") || saveType.equals("excelOrderError")){
								if(order.getBound().equals("AIR")){
									gwt = Util.null2dbl(order.getGwt()) * 2.20462262;
									if(gwt > 999999999.9999) {
										gwt = 999999999.9999;
									}
									order.setGwtLbs(gwt);
								}
							}
						}else{
							if(saveType.equals("excelUpload") || saveType.equals("excelItemError") || saveType.equals("excelOrderError")){
								if("2".equals(order.getGwtType())){	/** GWT입력란에 GWT_LBS입력한경우 */
									if(order.getBound().equals("AIR")){
										order.setGwtLbs(order.getGwt());
									}
									order.setGwt(Util.null2dbl(order.getGwt()) * 0.45359237);
								}else{
									if(order.getBound().equals("AIR")){
										gwt = Util.null2dbl(order.getGwt()) * 2.20462262;
										if(gwt > 999999999.9999) {
											gwt = 999999999.9999;
										}
										order.setGwtLbs(gwt);
									}
								}
							}
						}
						if(Util.null2dbl(order.getPiecesReal()) == 0 || saveType.equals("excelItemError")){
							if(pieces > 999999) {
								pieces = 999999;
							}
							order.setPiecesReal(pieces);
						}
						if(amountCur > 9999999999999.9999){
							amountCur = 9999999999999.9999;
						}
						
						if(Util.null2dbl(order.getAmountCur()) == 0 || saveType.equals("excelItemError")){
							order.setAmountCur(amountCur);
						}
						
						if(Util.isEmpty(order.getCommodityItem()) || saveType.equals("excelItemError")){
							if(Util.null2str(map.get("ecmItemNameAllYn")).equals("Y")){
								if(itemStr.length() > 201){
									order.setCommodityItem(itemStr.substring(1, 201));
								}else{
									order.setCommodityItem(itemStr.substring(1));
								}
							}else{
								order.setCommodityItem(itemStr.toString());
							}
						}
						if(Util.isEmpty(order.getCommodityItemLoc()) || saveType.equals("excelItemError")){
							if(Util.null2str(map.get("ecmItemNameAllYn")).equals("Y")){
								if(itemLocStr.length() > 201){
									order.setCommodityItemLoc(itemLocStr.substring(1, 201));
								}else{
									order.setCommodityItemLoc(itemLocStr.substring(1));
								}
							}else{
								order.setCommodityItemLoc(itemLocStr.toString());
							}
						}
						
						if(Util.isEmpty(order.getAllowedItemCode())){
							order.setAllowedItemCode(hsCode);
						}
						if(Util.isEmpty(order.getCurrencyCode())){
							order.setCurrencyCode(currencyCode);
						}
						if(Util.isEmpty(order.getUnitPieces())){
							order.setUnitPieces(unitPieces);
						}
					}
				}
			}
		}
		
		if(saveType.equals("excelUpload") || saveType.equals("excelItemError") || saveType.equals("excelOrderError")){
			if(order.getBound().equals("AIR")){
				double gwtLbs = Util.null2dbl(order.getGwt()) * 2.20462262;
				if(gwtLbs > 999999999.9999) {
					gwtLbs = 999999999.9999;
				}
				order.setGwtLbs(gwtLbs);
			}
			/** 오더 VWT */
			if(Util.null2dbl(map.get("divideValue")) != 0){
				double vwt = Util.null2dbl(order.getDimWidth()) * Util.null2dbl(order.getDimLength()) * Util.null2dbl(order.getDimHeight()) * Util.null2dbl(order.getPieces()) / Util.null2dbl(map.get("divideValue"));
				if(vwt > 999999999.999){
					vwt = 999999999.999;
				}
				order.setVwt(vwt);
			}
			
			if(Util.null2dbl(order.getCbm()) == 0){
				if(Util.null2dbl(order.getGwt()) > Util.null2dbl(order.getVwt())){
					order.setCbm(Util.null2dbl(order.getGwt()));
				}else{
					order.setCbm(Util.null2dbl(order.getVwt()));
				}
			}
		}
		
		/** 환율적용 */
		if(map.get("exChangeYn") != null){
			Map<String, Object> exrateMap = null;
			if(map.get("exrateMap") != null){
				exrateMap = (Map<String, Object>)map.get("exrateMap");
				if(exrateMap != null && Util.null2int(exrateMap.get("CNT")) == 0){
					exrateMap = null;
				}
			}else{
				/** 환율정보조회 */
				tmpMap.clear();
				tmpMap.put("srchEntCode", order.getEntCode());
				tmpMap.put("entBusinessCode", order.getEntBusinessCode());
				tmpMap.put("srchDateFormat", order.getDateFormat());
				if(Util.null2str(order.getBoundType()).equals("EXP")){
					tmpMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
					tmpMap.put("srchDate", Util.null2str(order.getObDate()));
				}else{
					tmpMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
					tmpMap.put("srchDate", Util.null2str(order.getArDate()));
				}
				/** 출항일자(입항일자)가 없을경우 오더일자로 환율 조회 2023-09-14 */
				if(Util.null2str(tmpMap.get("srchDate")).equals("")) {
					tmpMap.put("srchDate", Util.null2str(order.getPoDate(), Util.getNow("yyyy-MM-dd")));
				}
				exrateMap = commonMapper.selectExchangeRate(tmpMap);
				if(Util.null2int(exrateMap.get("CNT")) == 0){
					exrateMap = null;
				}
			}
			
			if(exrateMap != null) {
				double exrateUsd = Util.null2dbl(exrateMap.get("EXCHANGE_RATE_USD"));
				
				if(!saveType.equals("entOrder")){
					order.setExchangeRate(Util.null2dbl(exrateMap.get("EXCHANGE_RATE_" + order.getCurrencyCode())));
					order.setAmountLoc(Util.null2dbl(order.getAmountCur()) * order.getExchangeRate());
				}
				
				if(Util.null2str(order.getCurrencyCode()).equals("USD")){
					order.setAmountUsd(Util.null2dbl(order.getAmountCur()));
				}else if(exrateUsd!= 0){
					order.setAmountUsd(Util.null2dbl(order.getAmountLoc() / exrateUsd, 2, "U"));
				}else{
					order.setAmountUsd(0.0);
				}
			}else{
				if(!saveType.equals("entOrder")){
					order.setExchangeRate(0.0);
					order.setAmountLoc(0.0);
				}
				order.setAmountUsd(0.0);
			}
		}
		
		/** 통화단위가USD일경우 AMOUNT_USD = AMOUNT_CUR */
		if(Util.null2str(order.getCurrencyCode()).equals("USD") && !saveType.equals("excelUploadItem")){
			order.setAmountUsd(order.getAmountCur());
		}
		
		/** 통화단위가KRW일경우 환율 1, AMOUNT_LOC = AMOUNT_CUR 2023-09-14 */
		if(Util.null2str(order.getCurrencyCode()).equals("KRW") && !saveType.equals("entOrder") && !saveType.equals("excelUploadItem")) {
			order.setExchangeRate(1.0);
			order.setAmountLoc(order.getAmountCur());
		}
		
		if(saveType.equals("excelUpload") || saveType.equals("excelItemError") || saveType.equals("excelOrderError") || saveType.equals("excelMasterError")){
			/** 목록/일반 구분이 목록일경우에만 금액기준으로 값 입력 */
			if(Util.null2str(order.getCdType()).equals("Y")){
				if(Util.null2str(order.getBoundType()).equals("EXP")){
					if(Util.null2dbl(order.getAmountLoc()) > 2000000){
						order.setCdType("N");
					}
				}else{
					int chkUsd;
					if(Util.null2str(order.getOrigCountry()).equals("US") || Util.null2str(order.getOrigCountry()).equals("PR")){
						chkUsd = 200;
					}else{
						chkUsd = 150;
					}
					if(Util.null2dbl(order.getAmountUsd()) > chkUsd){
						order.setCdType("N");
					}
				}
			}
			
			if((Util.null2str(order.getCdType()).equals("N") || Util.null2str(order.getCdType()).equals("B"))){
				if(!Util.null2str(order.getCbCode()).equals("")){
					order.setClearanceStatus("CLS030");
				}else {
					order.setClearanceStatus("CLS010");
				}
			}
		}
		
		return true;
	}
	
	/** 오더대문자변환 */
	public void setOrderUpperCase(Order od) throws Exception {
		if(!Util.isEmpty(od.getShipperName())){
			od.setShipperName(od.getShipperName().toUpperCase());
		}
		if(!Util.isEmpty(od.getShipperAddress())){
			od.setShipperAddress(od.getShipperAddress().toUpperCase());
		}
		if(!Util.isEmpty(od.getConsigneeName())){
			od.setConsigneeName(od.getConsigneeName().toUpperCase());
		}
		if(!Util.isEmpty(od.getConsigneeAddress())){
			od.setConsigneeAddress(od.getConsigneeAddress().toUpperCase());
		}
		if(!Util.isEmpty(od.getCustomsId())){
			od.setCustomsId(od.getCustomsId().toUpperCase());
		}
	}
	
}