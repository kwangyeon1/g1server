package org.yht.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yht.exps.express.model.api.CoupangAddressInfo;
import org.yht.exps.express.model.api.CoupangOrderInterface;
import org.yht.exps.express.model.api.CoupangPccc;
import org.yht.exps.express.model.api.CoupangReturn;
import org.yht.exps.express.model.api.ItemInfoVE;

public class OrderValidateVE {
	public static String validateOrderInterface(CoupangOrderInterface vo) {
		StringBuilder errors = new StringBuilder();
		for(Field field : vo.getClass().getDeclaredFields()){
			field.setAccessible(true);
			String fieldName = field.getName();
			try {
				Object value = field.get(vo);
				System.out.println("검사 중: field = " + fieldName + ", value = " + value + ", type = " + (value != null ? value.getClass().getName() : "null"));
				switch (fieldName) {
					case "mblNumber":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /mblNumber= 필수입니다.\n");
							continue;
						}
						continue;
					case "courierCode":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /courierCode= 필수입니다.\n");
							continue;
						}
						continue;
					case "originCode":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /originCode= 필수입니다.\n");
							continue;
						}
						continue;
				}
			} catch (Exception e) {
				errors.append("필드 처리 중 오류 발생: ").append(fieldName).append("\n");
			}
		}
		return errors.toString();
	}
	
	public static String validateOrderHblInterface(Map<String, Object> vo) {
		StringBuilder errors = new StringBuilder();
		List<String> keyList = new ArrayList<String>(vo.keySet());
		for(String fieldName : keyList) {
			try {
				Object value = vo.get(fieldName);
				System.out.println("검사 중: field = " + fieldName + ", value = " + value + ", type = " + (value != null ? value.getClass().getName() : "null"));
				
				switch (fieldName) {
					case "hblNumber":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /hblNumber= 필수입니다.\n");
							continue;
						}
						continue;
					case "billingTerm":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /billingTerm= 필수입니다.\n");
							continue;
						}
						continue;
					case "clearanceType":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /clearanceType= 필수입니다.\n");
							continue;
						}
						continue;
					case "currency":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /oricurrencyginCode= 필수입니다.\n");
							continue;
						}
						continue;
					case "ecommerceType":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /ecommerceType= 필수입니다.\n");
							continue;
						}
						continue;
					case "hsCodeOfMainItem":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /hsCodeOfMainItem= 필수입니다.\n");
							continue;
						}
						continue;
					case "numberOfPackages":
						if (value == null) {
							errors.append(" /numberOfPackages= 필수입니다.\n");
							continue;
						}
						continue;
					case "orderNumber":
						if (value == null) {
							errors.append(" /orderNumber= 필수입니다.\n");
							continue;
						}
						continue;
					case "origin":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /origin= 필수입니다.\n");
							continue;
						}
						continue;
					case "originCountry":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /originCountry= 필수입니다.\n");
							continue;
						}
						continue;
					case "shipmentDescription":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /shipmentDescription= 필수입니다.\n");
							continue;
						}
						continue;
					case "usageType": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /usageType= 필수입니다.\n");
							continue;
						}
						continue;
					case "weight":
						if (value == null) {
							errors.append(" /weight= 필수입니다.\n");
							continue;
						}
						continue;
					case "shipperName": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /shipperName= 필수입니다.\n");
							continue;
						}
						continue;
					case "shipperAddress": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /shipperAddress= 필수입니다.\n");
							continue;
						}
						continue;
					case "shipperContactNumber": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /shipperContactNumber= 필수입니다.\n");
							continue;
						}
						continue;
						
					//CustomerInfo
					case "consigneeTel": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /consigneeTel= 필수입니다.\n");
							continue;
						}
						continue;
					case "consigneeAddress": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /consigneeAddress= 필수입니다.\n");
							continue;
						}
						continue;
					case "consigneeName": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /consigneeName= 필수입니다.\n");
							continue;
						}
						continue;
					case "consigneeAddressLoc": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /consigneeAddressLoc= 필수입니다.\n");
							continue;
						}
						continue;
					case "consigneeNameLoc": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /consigneeNameLoc= 필수입니다.\n");
							continue;
						}
						continue;
					case "customsId": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /customsId= 필수입니다.\n");
							continue;
						}
						continue;
					case "consigneeHp":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /consigneeHp= 필수입니다.\n");
							continue;
						}
						continue;
					case "consigneeZipcode":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /consigneeZipcode= 필수입니다.\n");
							continue;
						}
						continue;
					//hblProperties
					case "shipmentBoxId":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /shipmentBoxId= 필수입니다.\n");
							continue;
						}
						continue;
				}
			} catch (Exception e) {
				errors.append("필드 처리 중 오류 발생: ").append(fieldName).append("\n");
			}
		}
		return errors.toString();
	}
	
	public static String validateItemInterface(Map<String, Object> vo) {
		StringBuilder errors = new StringBuilder();
		List<String> keyList = new ArrayList<String>(vo.keySet());
		for(String fieldName : keyList) {
			try {
				Object value = vo.get(fieldName);
				System.out.println("검사 중: field = " + fieldName + ", value = " + value + ", type = " + (value != null ? value.getClass().getName() : "null"));
				switch (fieldName) {
					case "hsCode":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /hsCode= 필수입니다.\n");
							continue;
						}
						continue;
					case "name": 
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /name= 필수입니다.\n");
							continue;
						}
						continue;
					case "amount":
						if (value == null) {
							errors.append(" /amount= 필수입니다.\n");
							continue;
						}
						continue;
					case "quantity": 
						if (value == null) {
							errors.append(" /quantity= 필수입니다.\n");
							continue;
						}
						continue;
					case "unitPrice":
						if (value == null) {
							errors.append(" /unitPrice= 필수입니다.\n");
							continue;
						}
						continue;
					//ItemProperties
//					case "invoiceItemNameLocal":
//						if (value == null || ((String) value).trim().isEmpty()) {
//							errors.append(" /invoiceItemNameLocal= 필수입니다.\n");
//							continue;
//						}
//						continue;
//					case "grossWeight":
//						if (value == null || ((String) value).trim().isEmpty()) {
//							errors.append(" /grossWeight= 필수입니다.\n");
//							continue;
//						}
//						continue;
					case "netWeight":
						if (value == null) {
							errors.append(" /netWeight= 필수입니다.\n");
							continue;
						}
						continue;
//					case "coupangRegDate":
//						if (value == null || ((String) value).trim().isEmpty()) {
//							errors.append(" /coupangRegDate= 필수입니다.\n");
//							continue;
//						}
//						continue;
				}
			} catch (Exception e) {
				errors.append("필드 처리 중 오류 발생: ").append(fieldName).append("\n");
			}
		}
		return errors.toString();
	}

	public static String validateReturnOrderInterface(CoupangReturn vo) {
		StringBuilder errors = new StringBuilder();
		for(Field field : vo.getClass().getDeclaredFields()){
			field.setAccessible(true);
			String fieldName = field.getName();
			try {
				Object value = field.get(vo);
				System.out.println("검사 중: field = " + fieldName + ", value = " + value + ", type = " + (value != null ? value.getClass().getName() : "null"));
				switch (fieldName) {
					case "orderType":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /orderType= 필수입니다.\n");
							continue;
						}
						continue;
					case "requestId":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /requestId= 필수입니다.\n");
							continue;
						}
						continue;
					case "orderId":
						if (value == null) {
							errors.append(" /orderId= 필수입니다.\n");
							continue;
						}
						continue;
					case "orderInvoiceNumber":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /orderInvoiceNumber= 필수입니다.\n");
							continue;
						}
						continue;
					case "vendorType":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /vendorType= 필수입니다.\n");
							continue;
						}
						continue;
					case "shippingCompanyCode":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /shippingCompanyCode= 필수입니다.\n");
							continue;
						}
						continue;
				}
			} catch (Exception e) {
				errors.append("필드 처리 중 오류 발생: ").append(fieldName).append("\n");
			}
		}
		return errors.toString();
	}

	public static String validateReturnAddInfoInterface(CoupangAddressInfo vo) {
		StringBuilder errors = new StringBuilder();
		for(Field field : vo.getClass().getDeclaredFields()){
			field.setAccessible(true);
			String fieldName = field.getName();
			try {
				Object value = field.get(vo);
				System.out.println("검사 중: field = " + fieldName + ", value = " + value + ", type = " + (value != null ? value.getClass().getName() : "null"));
				switch (fieldName) {
					case "name":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /name= 필수입니다.\n");
							continue;
						}
						continue;
					case "zipCode":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /zipCode= 필수입니다.\n");
							continue;
						}
						continue;
					case "address":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /address= 필수입니다.\n");
							continue;
						}
						continue;
					case "contactNumber":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /contactNumber= 필수입니다.\n");
							continue;
						}
						continue;
				}
			} catch (Exception e) {
				errors.append("필드 처리 중 오류 발생: ").append(fieldName).append("\n");
			}
		}
		return errors.toString();
	}

	public static String validateReturnItemInfoInterface(ItemInfoVE vo) {
		StringBuilder errors = new StringBuilder();
		for(Field field : vo.getClass().getDeclaredFields()){
			field.setAccessible(true);
			String fieldName = field.getName();
			try {
				Object value = field.get(vo);
				System.out.println("검사 중: field = " + fieldName + ", value = " + value + ", type = " + (value != null ? value.getClass().getName() : "null"));
				switch (fieldName) {
					case "id":
						if (value == null) {
							errors.append(" /id= 필수입니다.\n");
							continue;
						}
						continue;
					case "name":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /name= 필수입니다.\n");
							continue;
						}
						continue;
					case "quantity":
						if (value == null) {
							errors.append(" /quantity= 필수입니다.\n");
							continue;
						}
						continue;
					case "price":
						if (value == null) {
							errors.append(" /price= 필수입니다.\n");
							continue;
						}
						continue;
				}
			} catch (Exception e) {
				errors.append("필드 처리 중 오류 발생: ").append(fieldName).append("\n");
			}
		}
		return errors.toString();
	}

	public static String validatePcccInterface(CoupangPccc vo) {
		StringBuilder errors = new StringBuilder();
		for(Field field : vo.getClass().getDeclaredFields()){
			field.setAccessible(true);
			String fieldName = field.getName();
			try {
				Object value = field.get(vo);
				System.out.println("검사 중: field = " + fieldName + ", value = " + value + ", type = " + (value != null ? value.getClass().getName() : "null"));
				switch (fieldName) {
					case "requestId":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /requestId= 필수입니다.\n");
							continue;
						}
						continue;
					case "coupangOrderId":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /coupangOrderId= 필수입니다.\n");
							continue;
						}
						continue;
					case "orderedAt":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /orderedAt= 필수입니다.\n");
							continue;
						}
						continue;
					case "pccc":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /pccc= 필수입니다.\n");
							continue;
						}
						continue;
					case "receiverPhoneNumber":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /receiverPhoneNumber= 필수입니다.\n");
							continue;
						}
						continue;
					case "receiverName":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /receiverName= 필수입니다.\n");
							continue;
						}
						continue;
					case "zipcode":
						if (value == null || ((String) value).trim().isEmpty()) {
							errors.append(" /zipcode= 필수입니다.\n");
							continue;
						}
						continue;
				}
			} catch (Exception e) {
				errors.append("필드 처리 중 오류 발생: ").append(fieldName).append("\n");
			}
		}
		return errors.toString();
	}
}
