package org.yht.exps.express.service.bms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.UpsNewbillingMapper;
import org.yht.exps.express.mapper.eCommerce.ManifestFileMapper;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.bms.UpsNewbilling;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class UpsNewbillingService {

	@Autowired
	private CustomProperties customProperties;
	@Autowired
	UpsNewbillingMapper upsNewbillingMapper;
	
	@Autowired
	ManifestFileMapper manifestFileMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	CommonMapper commonMapper;
	
	
	
	/**
	 * UPS : 1.적하목록 정보 뉴빌링 업데이트 대상건조회
	 * @param newbilling
	 * @return
	 * @throws Exception
	 */
	public void selectManifestToNewbilling(UpsNewbilling newbilling) throws Exception {
		/** uFms소스  EDAL_UPS_Interface.cs - AI_ManifestToNewbilling */
		List<Map<String,Object>> list = upsNewbillingMapper.selectManifestToNewbilling(newbilling);
		
		/** 각 데이터 타입 길이를 초과하는 경우 문자열 자르기 **/
		for(Map<String,Object> dataMap : list) {
			dataMap.put("hblNo",getSubStringK(Util.null2str(dataMap.get("hblNo")),0,20));
			dataMap.put("customsId",getSubStringK(Util.null2str(dataMap.get("customsId")),0,13));
			dataMap.put("consigneeAddressLoc",getSubStringK(Util.null2str(dataMap.get("consigneeAddressLoc")),0,200));
			dataMap.put("consigneeNameLoc",getSubStringK(Util.null2str(dataMap.get("consigneeNameLoc")),0,100));
			dataMap.put("bankName",getSubStringK(Util.null2str(dataMap.get("bankName")),0,16));
			dataMap.put("consigneeName",getSubStringK(Util.null2str(dataMap.get("consigneeName")),0,100));
			dataMap.put("consigneeTel",getSubStringK(Util.null2str(dataMap.get("consigneeTel")),0,20));
			dataMap.put("commodityItem",getSubStringK(Util.null2str(dataMap.get("commodityItem")),0,200));
			dataMap.put("consigneeAddress",getSubStringK(Util.null2str(dataMap.get("consigneeAddress")),0,200));
			dataMap.put("lcNo",getSubStringK(Util.null2str(dataMap.get("lcNo")),0,19));
		}
		
		newbilling.setDataList(list);
//		newbilling.setDataList(upsNewbillingMapper.selectManifestToNewbilling(newbilling));
	}
	
	/**
	 * UPS : 2.목록이관시 목록건 뉴빌링 업데이트 대상건조회
	 * @param newbilling
	 * @return
	 * @throws Exception
	 */
	public void selectManifestToNewbillingCustomsTransfer(UpsNewbilling newbilling) throws Exception {
		/** newbilling.getCdType()값   자체선별화면 - 선별마감 = 'P',  적하목록화면 - 목록이관시 목록건 뉴빌링 업데이트 = 'Y' */
		/** uFms소스  EDAL_UPS_Interface.cs - AI_ManifestToNewbilling_CustomsTransfer(string strMasterRefNo, string strHouseRefNo, bool bPCustomerYN(newbilling.getCdType()이 'P'이면 true 아니면 flase)) */
		if(!"customsUpdate".equals(newbilling.getAction())) {
			newbilling.setDataList(upsNewbillingMapper.selectManifestToNewbillingCustomsTransfer(newbilling));
		}else{	/** uFms소스 AI_ManifestClearanceList.cs - Get_pCustomerNumber, EDAL_UPS_Interface.cs - AI_LocalNewbillingPCustomsUpdate */
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			Map<String, Object> customsMap = null;
			
			String nowDate = Util.getNow("yyyy-MM-dd");
			tmpMap.put("nowDate", nowDate);
			tmpMap.put("beforeOneYear", DateTime.getAddOrBeforeDay(nowDate, -365));
			
			List<Map<String, Object>> customsUpdateList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> newbillingUpdateList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> list = upsNewbillingMapper.selectLocalNewbillingPCustomsUpdate(newbilling);
			for(Map<String, Object> map : list){
				
				/** 각 데이터 타입 길이를 초과하는 경우 문자열 자르기 **/
				map.put("hblNo",getSubStringK(Util.null2str(map.get("hblNo")),0,20));
				map.put("consigneeAddressLoc",getSubStringK(Util.null2str(map.get("consigneeAddressLoc")),0,200));
				map.put("consigneeNameLoc",getSubStringK(Util.null2str(map.get("consigneeNameLoc")),0,100));
				
				if(!Util.isEmpty(map.get("CONSIGNEE_NAME_CHK")) && !Util.isEmpty(map.get("CONSIGNEE_TEL_CHK"))){
					/** 선별마감시 수하인정보로 CUSTOMS_ID, LC_NO 업데이트 */
					tmpMap.put("consigneeName", map.get("CONSIGNEE_NAME_CHK"));
					tmpMap.put("consigneeTel", map.get("CONSIGNEE_TEL_CHK"));
					tmpMap.put("consigneeHp", map.get("CONSIGNEE_HP_CHK"));
					customsMap = manifestFileMapper.selectOrderCustomsId(tmpMap);	/** AI_ManifestClearanceList.cs - Get_pCustomerNumber */
					if(customsMap != null && customsMap.size() > 0){
						customsMap.put("odJobNo", map.get("OD_JOB_NO"));
						customsUpdateList.add(customsMap);
					}else{
						newbillingUpdateList.add(map);
					}
				}else{
					newbillingUpdateList.add(map);
				}
			}
			
			if(customsUpdateList.size() > 0){
				SqlSession sqlSession = null;
				try{
					sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
					UpsNewbillingMapper upsNewbillingBatchMapper = sqlSession.getMapper(UpsNewbillingMapper.class);
					
					int cnt = 1;
					for(Map<String, Object> data : customsUpdateList){
						data.put("cryptographyYn", newbilling.getCryptographyYn());
						upsNewbillingBatchMapper.updateCustomsInfo(data);
						if((cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
					
					sqlSession.flushStatements();
					sqlSession.clearCache();
					sqlSession.commit();
				}catch(Exception e){
					throw e;
				}finally{
					sqlSession.close();
				}
			}
			
			newbilling.setDataList(newbillingUpdateList);	/** EDAL_UPS_Interface.cs - AI_LocalNewbillingPCustomsUpdate */
		}
	}
	
	/**
	 * UPS : 통관예정자료 파일생성(eCIS, TXT)
	 * @param newbilling
	 * @return
	 * @throws Exception
	 */
	public DownloadFileInfo eCISProgramCreate(UpsNewbilling newbilling) throws Exception {
//		OrderSaveResult orderSaveResult = new OrderSaveResult();
//		Map<String, Object> resultData = new HashMap<String, Object>();
		
		/** uFms소스  EDAL_UPS_Interface.cs - AI_eCISProgram_Create */
		List<Map<String, Object>> list = newbilling.getDataList();
		
		/** 2025-04-29 뉴빌링DB접근하지않음  체크X, 내용공백처리 */
//		List<Map<String, Object>> checkList = new ArrayList<Map<String, Object>>();
//		int listSize = list.size();
//		if(listSize > 0){
//			Map<String, Object> tmpMap = new HashMap<String, Object>();
//			for(int i = 0; i < listSize; i+=1000){
//				if(i + 1000 < listSize) {
//					tmpMap.put("list", list.subList(i, i + 1000));
//				}else{
//					tmpMap.put("list", list.subList(i, listSize));
//				}
//				checkList.addAll(upsNewbillingMapper.selecteCISProgramCreateCheck(tmpMap));
//			}
//			
//			StringBuffer hblStr = new StringBuffer();
//			if(checkList.size() != listSize){
//				if(checkList.size() == 0){
//					for(Map<String, Object> map : list){
//						hblStr.append(", " + Util.null2str(map.get("HBL_NO")));
//					}
//				}else{
//					boolean chk;
//					for(Map<String, Object> map : list){
//						chk = true;
//						for(Map<String, Object> checkMap : checkList){
//							if(Util.null2str(checkMap.get("ShipmentNumber")).trim().equals(map.get("HBL_NO"))){
//								chk = false;
//								break;
//							}
//						}
//						if(chk){
//							hblStr.append(", " + Util.null2str(map.get("HBL_NO")));
//						}
//					}
//				}
//				
//				if(hblStr.length() > 0){
//					resultData.put("errorStr", "로컬트랜스퍼를 확인 후 진행하시길 바랍니다.<br />" + hblStr.toString().substring(2) + " : Waybill 테이블에 해당 BL이 존재 하지 않습니다.");
//					orderSaveResult.setResultData(resultData);
//					orderSaveResult.setResultCode("error");
//					return orderSaveResult;
//				}
//			}else{
//				for(Map<String, Object> checkMap : checkList){
//					if(Util.isEmpty(checkMap.get("Orig"))){
//						hblStr.append(", " + Util.null2str(checkMap.get("ShipmentNumber")));
//					}
//				}
//				if(hblStr.length() > 0){
//					resultData.put("errorStr", "로컬트랜스퍼를 확인 후 진행하시길 바랍니다.<br />" + hblStr.toString().substring(2) + " 적출국 데이터가 없는 BL번호 입니다.");
//					orderSaveResult.setResultData(resultData);
//					orderSaveResult.setResultCode("error");
//					return orderSaveResult;
//				}
//			}
//		}
		
		
		File file = null;
		BufferedWriter bw = null;
        OutputStreamWriter osw = null;
        FileOutputStream fos = null;
        StringBuffer dataSb = new StringBuffer();
		
		String dir = customProperties.getExcelDownload() + "/";
		String saveFile = "CISProgram_" + DateTimeUtil.getDate("yyyyMMddhhmmss") + ".txt";
		
		try {
			
			file = new File(dir + saveFile);
			fos = new FileOutputStream(file,true); //  true로 지정되면 해당 파일에 이어쓰기
			osw = new OutputStreamWriter(fos, "MS949"); // "euc-kr" , "MS949"
			bw = new BufferedWriter(osw);
			int row = 1;
			String whCode; 
			for(Map<String, Object> map : list){
//				map.putAll(upsNewbillingMapper.selecteCISProgramCreateWaybill(map));
				dataSb.setLength(0);
				
				dataSb.append(Util.rpad2(map.get("HBL_NO"), 20, true));			// 1 ,  20 'HOUSE B/L
				dataSb.append(Util.rpad2("", 14));								// 21,  14 '사업자등록번호
				dataSb.append(Util.rpad2(map.get("SHIPPER_NAME"), 30, true));	// 35,  30 '수출자상호
				dataSb.append(Util.rpad2(map.get("AR_DATE"), 8, true));			// 65,  8  '도착예정일(입항일자)
				dataSb.append(Util.rpad2(map.get("LINE_CODE"), 2, true));		// 73,  2  'Carrier
				dataSb.append(Util.rpad2(map.get("VESSEL_FLIGHT"), 20, true));	// 75,  20 '선박명(편명)
				dataSb.append(Util.rpad2("", 6));								// 95,  6  'Filler
				dataSb.append(Util.rpad2(map.get("ORIG"), 2, true));			// 101, 2  '적출국코드  *UPS 테이블?
				dataSb.append(Util.rpad2(Util.null2str(newbilling.getCarryinDate()).replaceAll("[^0-9]", ""), 8));	// 103, 8  '입항예정일(반입일자)  *UPS 테이블?
				whCode = Util.null2str(map.get("ASSIGNMENT_WH_CODE")).trim().toUpperCase();
				if(whCode.equals("DG") || whCode.equals("SJD") || whCode.equals("SID")){ // CUSTBROK=배정코드
					dataSb.append(Util.rpad2("DGXXX", 10));						// 111, 10 '장치장소(장치확인번호)  *UPS 테이블?
				}else{
					dataSb.append(Util.rpad2("PKG/F", 10));						// 111, 10 '장치장소(장치확인번호)  *UPS 테이블?
				}
				dataSb.append(Util.rpad2(map.get("MRN"), 11, true));			// 121, 11 '화물관리번호(MRN)
				dataSb.append(Util.rpad2(map.get("MSN"), 4, true));				// 132, 4  'MSN
				dataSb.append(Util.rpad2(map.get("HSN"), 4, true));				// 136, 4  'HSN
				dataSb.append(Util.rpad2("", 3));								// 140, 3  '인도조건(FOB, CIF등...)
				dataSb.append("USD");											// 143, 3  '통화기호
				dataSb.append(Util.lpad(Util.null2str(map.get("AMOUNT_CUR")).replace(".", ""), 16, '0'));	// 146, 16 '결제금액	   '신고가격  "뒤4자리가 소숫자리임"
				dataSb.append(Util.lpad(Util.null2str(map.get("PIECES")), 8, '0'));							// 162, 8  '총포장갯수
				dataSb.append("CT");											// 170, 2  '총포장단위 "CT"
				dataSb.append(Util.lpad(Util.null2str(map.get("GWT")).replace(".", ""), 14, '0'));			// 172, 14 '총중량 "뒤1자리가 소숫자리임"       소수3자리?
				dataSb.append("KG");											// 186, 2  '총중량단위
				dataSb.append(Util.rpad2("", 14));								// 188, 14 'Filler
				dataSb.append(Util.rpad2(map.get("MBL_NO"), 11, true));			// 202, 11 'MASTER NUMBER
				dataSb.append(Util.rpad2("", 29));								// 213, 29 'Filler
				dataSb.append(Util.rpad2("", 2));								// 242, 2  '선기국코드
				dataSb.append(Util.rpad2("", 3));								// 244, 3  'Ugent유무(긴급건인지 유무)
				dataSb.append(Util.rpad2("", 1));								// 247, 1  'Filler
				if(Util.null2str(map.get("BILLING_TERM")).trim().toUpperCase().equals("F/D")){
					dataSb.append("Y");											// 248, 1  'ddp유무(Y/N)(운임+관세를 수출자가 지불했는지유무)
				}else{
					dataSb.append("N");											// 248, 1  'ddp유무(Y/N)(운임+관세를 수출자가 지불했는지유무)
				}
				dataSb.append(Util.rpad2(map.get("ExcWho"), 8, true));			// 249, 8  'UPS담당자
				dataSb.append("1");												// 257, 1  '화주구분(UPS/일우)
				dataSb.append(Util.rpad2(map.get("DEST_CODE"), 3, true));		// 258, 3  'Destination(도착도시)
				if(Util.isEmpty(map.get("BILLING_TERM")) || Util.isEmpty(map.get("TRANS_TYPE")) || Util.isEmpty(map.get("DUTIES"))){
					dataSb.append(Util.rpad2("", 10));	// 261, 10 'Billing Term
				}else{
					dataSb.append(Util.rpad2(Util.null2str(map.get("BILLING_TERM")).trim().toUpperCase() + " - " + Util.null2str(map.get("TRANS_TYPE")).trim().toUpperCase() + " " + Util.null2str(map.get("DUTIES")).trim().toUpperCase(), 10));	// 261, 10 'Billing Term
				}
				//dataSb.append(Util.rpad2("", 20));							// 271, 20 'SPACE
				dataSb.append(Util.rpad2("", 11));								// 271, 11 'SPACE
				dataSb.append("\r\n");
				bw.write(String.valueOf(dataSb));
				if (row++ % 100 == 0) bw.flush();
			}
			
			
			bw.flush();
			bw.close();
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
			bw.close();
			throw e;
		}
		
//		resultData.put("saveFile", saveFile);
//		orderSaveResult.setResultData(resultData);
//		orderSaveResult.setResultCode("succ");
		
		DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
		downloadFileInfo.setSaveFile(saveFile);
		downloadFileInfo.setOriginFile(saveFile);
		return downloadFileInfo;
	}
	
	/**
	 * UPS : 통관예정자료 파일생성(eCIS, TXT) 대상건조회
	 * @param newbilling
	 * @return
	 * @throws Exception
	 */
	public void selecteECISProgramCreate(UpsNewbilling newbilling) throws Exception {
		/** uFms소스  EDAL_UPS_Interface.cs - AI_eCISProgram_Create */
		newbilling.setDataList(upsNewbillingMapper.selecteCISProgramCreate(newbilling));
	}
	
	
	public OrderSaveResult setNewbillingError() throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> resultData = new HashMap<String, Object>();
		resultData.put("errorStr", "[항공수입] 뉴빌링 업데이트 중 오류가 발생 되었습니다. 전산담당자에 문의 바랍니다.");
		orderSaveResult.setResultData(resultData);
		orderSaveResult.setResultCode("error");
		return orderSaveResult;
	}
	

    
    
    
    
    
    
    
//  김일호 =====================================================================================================

	/**
	 * 오류통보삭제 뉴빌링 DB 접근 작업
	 * @param req 
	 * @param newbilling
	 * @param entCode 
	 * @return
	 */
	public OrderSaveResult newbillingCustomsIdErrorUpdate(HttpServletRequest req, UpsNewbilling newbilling, String entCode) {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		List<Map<String, Object>> dataList = newbilling.getDataList();
		
		// 뉴빌링 DB 접근
		for(int i=0; i<dataList.size(); i++) {
			
			Map<String , Object> dataMap = dataList.get(i);
			
			//BatShipmentM 삭제
			upsNewbillingMapper.deleteBatShipmentMInfo(dataMap);
			
			//WaybillExceptHot 수정
			dataMap.put("lowToHigh", "P");
			upsNewbillingMapper.updateLowToHighWaybillExceptHot(dataMap);
			
			//WaybillMFCSDN 수정
			dataMap.put("Perror", "Y");
			upsNewbillingMapper.updatePerrorWaybillMFCSDN(dataMap);
			
			//WaybillExceptOne 저장
			dataMap.put("excCode", "PS");
			dataMap.put("excMemo", "개인통관고유부호 필요(오류통보)");
			upsNewbillingMapper.saveWaybillExceptOneInfo(dataMap);

			dataMap.put("excCode", "F6");
			dataMap.put("excMemo", "사용불가 - 화주정보의 통관번호");
			dataMap.put("excTimeGbn", "SECOND");
			dataMap.put("excTime", 10);
			
			//WaybillExceptOne 저장
			upsNewbillingMapper.saveWaybillExceptOneInfo(dataMap);
			
		}
		
		return orderSaveResult;
	}

    
//  이슬기 =====================================================================================================
	/**
	 * 통관번호,제출번호 삭제 작업
	 * @param newbilling
	 * @param entCode
	 * @return
	 */
	public OrderSaveResult removeCustomsId(UpsNewbilling newbilling, String entCode) {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		List<Map<String, Object>> dataList = newbilling.getDataList();
		
		Map<String,Object> srchMap = new HashMap<String, Object>();
		srchMap.put("entCode", entCode);
		srchMap.put("entBusinessCode", newbilling.getSrchEntBusinessCode());
		srchMap.put("solution", "ECM");
		srchMap.put("optionCode", "C10");
		
		String optionVal = commonMapper.selectSystemOption(srchMap);
		
		// DB 업데이트
		for(int i=0; i<dataList.size(); i++) {
			Map<String , Object> dataMap = dataList.get(i);
			
				// TB_ECM_OD 테이블 DELIVERY_NOTE에 개통부호 저장
				dataMap.put("cryptographyYn", newbilling.getCryptographyYn());
				upsNewbillingMapper.updateDeliveryNoteOd(dataMap);
				
				// TB_ECM_OD 테이블 개통부호삭제
				upsNewbillingMapper.updateCustomsIdOd(dataMap);
				
				// TB_CD_B_INFO 테이블 개통부호 삭제
				upsNewbillingMapper.updateCustomsIdCdBinfo(dataMap);
			
			if("N".equals(Util.null2str(optionVal))) {
				// TB_ECM_OD_ADD 테이블 제출번호 삭제 및 LAST POD 적하전송으로 변경
				upsNewbillingMapper.updateSimpleDocNoInfo(dataMap);
			}
		}
		
		return orderSaveResult;
	}
	/**
	 * UPS 성명불일치 뉴빌링 업데이트
	 * @param newbilling
	 * @return
	 * @throws Exception
	 */
	public boolean localNewbillingConsigneeInCorrect(UpsNewbilling newbilling) throws Exception {
		try{
			for(Map<String, Object> dataMap : newbilling.getDataList()) {
				upsNewbillingMapper.deleteBatShipmentMInfo(dataMap);
				
				dataMap.put("lowToHigh", "P");
				upsNewbillingMapper.updateLowToHighWaybillExceptHot(dataMap);
				
				dataMap.put("Perror", "S");
				upsNewbillingMapper.updatePerrorWaybillMFCSDN(dataMap);
				
				dataMap.put("excCode", "PS");
				dataMap.put("excMemo", "발급자명,휴대전화번호 불일치(오류통보)");
				upsNewbillingMapper.saveWaybillExceptOneInfo(dataMap);
				
				dataMap.put("excTimeGbn", "SECOND");
				dataMap.put("excTime", 10);
				dataMap.put("excMemo", "(고객)유니패스,모바일관세청앱에서 확인요망");
				upsNewbillingMapper.saveWaybillExceptOneInfo(dataMap);
				
				dataMap.put("excTime", 20);
				dataMap.put("excCode", "F6");
				dataMap.put("excMemo", "개인통관고유부호 - " + Util.null2str(dataMap.get("customsId")));
				upsNewbillingMapper.saveWaybillExceptOneInfo(dataMap);
			}
			
			return true;
			
		}catch (Exception ex) {
			log.error(Util.getPrintStacTraceString(ex));
			return false;
		}
	}
	
	/**
	 * 로컬미결등록 뉴빌링 업데이트 대상건조회
	 * @param newbilling
	 * @throws Exception
	 */
	public void selectLocalNewbillingApply(UpsNewbilling newbilling) throws Exception {
		newbilling.setDataList(upsNewbillingMapper.selectLocalNewbillingApply(newbilling));
	}
	
	/**
	 * 로컬미결등록 뉴빌링 업데이트
	 * @param newbilling
	 * @param entCode
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult localNewbillingApply(UpsNewbilling newbilling) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		try {
			List<Map<String, Object>> dataList = newbilling.getDataList();
			for(Map<String, Object> dataMap : dataList) {
				dataMap.put("excCode", "B5");
				dataMap.put("excMemo", "목록미결");
				upsNewbillingMapper.saveWaybillExceptOneInfo(dataMap);
				
				upsNewbillingMapper.updateLocalNewbillingApply(dataMap);
			}
			return orderSaveResult;
		}catch (Exception ex) {
			orderSaveResult.setResultCode("error");
			return orderSaveResult;
		}
	}
	
	/**
	 * 
	 * @param newbilling
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> selectToNewbillingCustomsTransferExcelMblInfo(UpsNewbilling newbilling) throws Exception {
		return upsNewbillingMapper.selectToNewbillingCustomsTransferExcelMblInfo(newbilling);
	}
    
    
    
	public static String getSubStringK(String str, int start, int end) throws Exception {
		// 시작위치 >= 끝 또는 문자열 < 시작위치
		if(start >= end || str.length() < start) {
			return "";
		}
		
		return Util.getFileReadData(str, "String", Integer.toString(end - start), start, end);
	}
    
    
    
   
}
