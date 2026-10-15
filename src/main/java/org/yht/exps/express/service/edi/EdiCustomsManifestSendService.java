package org.yht.exps.express.service.edi;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.ConsolMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrder;
import org.yht.exps.express.model.edi.SendManifest;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Slf4j
@Service
@Transactional
public class EdiCustomsManifestSendService {
		
	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	private CustomProperties customProperties;
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	@Resource(name = "commonService")
	CommonService commonService;
	/**
	 * 수출목록신고 xml 생성
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public OrderSaveResult setCustomsManifestExpDataOld(SendManifest sendManifest, String cusSendType) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendManifest.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		String message = null;
		
		if(Util.null2str(sendManifest.getMasterOdJobNo()).equals("")){
			sendManifest.setOdJobNo(sendManifest.getHouseOdJobNoList().get(0));
		}
		Util.accessControlDatabase(sendManifest, "action", false, null, null, null, null);
		Map<String, Object> masterOrder = manifestMapper.selectCustomsManifestMaster(sendManifest);
		if(masterOrder != null){
			List<Map<String, Object>> houseOrderList = manifestMapper.selectCustomsManifestHouse(sendManifest);
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			Map<String, Object> logMap = new HashMap<String, Object>();
			Map<String, Object> insertData = new HashMap<String, Object>();
			
			Map<String, Object> tmpPodMap = null;
			List<Map<String, Object>> insertOrderPodList = new ArrayList<Map<String, Object>>();
			
			Map<String, Object> tmpMap = null;
			
			Map<String, Object> tmpInsertData = null;
			List<Map<String, Object>> insertGovSendList = new ArrayList<Map<String, Object>>();
			
			podMap.put("entCode", sendManifest.getEntCode());
			podMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
			podMap.put("solution", "ECM");
			podMap.put("optionCode", "C04");
//			String consigneeNameType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
			podMap.put("optionCode", "C05");
//			String consigneeAddressType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
			
			if (this.getCustomsManifestValidate(masterOrder, houseOrderList, tempSearchData, errList, result, "", "") && !Util.null2str(sendManifest.getPenaltyYn()).equals("Y")){
				
				if("errorCheck".equals(sendManifest.getAction())){
					orderSaveResult.setResultCode("errorCheck");
					return orderSaveResult;
				}
				
				String today = DateTime.getToday();
				sendManifest.setPodStatus("ECM110");
				sendManifest.setSimpleDocDate(today);
				result.put("updateCustomsManifestData", sendManifest);
				
				podMap.put("userLanguage", sendManifest.getUserLanguage());
				podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
				podMap.put("srchCode", "ECM110");
				podMap.put("entCode", sendManifest.getEntCode());
				podMap.put("entBusinessCode", "");
				podMap.put("gmtTime", sendManifest.getGmtTime());
				podMap.put("updateUserId", sendManifest.getUpdateUserId());
				podMap.put("stockType", "POD");
				podMap.put("stockSign", "POD");
				podMap.put("stockSeqLength", 8);
				podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				podMap.put("stockRemark", "ECM POD JOB NO");
				podMap.put("podStatus", "ECM110");
				podMap.put("podTableName", 	"TB_ECM_POD_02");
				
				logMap.put("ent_code", sendManifest.getEntCode());
				logMap.put("customer_code", "");
				logMap.put("solution", "ECM");
				logMap.put("bound", sendManifest.getBound());
				logMap.put("bound_type", "EXP");
				logMap.put("if_service_code", "");
				logMap.put("if_vender", "GOV");
				logMap.put("if_send_recv_type", "S");
				logMap.put("if_send_recv_method", "");
				logMap.put("if_status_info", "");
				logMap.put("if_sender_id", "");
				logMap.put("if_receiver_id", "");
				logMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
				logMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
				logMap.put("register_user_id", sendManifest.getRegisterUserId());
				logMap.put("register_user_name", sendManifest.getRegisterUserName());
				logMap.put("register_date_time", System.currentTimeMillis());
				logMap.put("if_doc_code", "CUSTOMS_CLEARANCE");
				logMap.put("if_doc_name", "통관목록");
				
				insertData.put("entCode", sendManifest.getEntCode());
				insertData.put("typeCode", "GOVCBR833");
				insertData.put("registerUserId", sendManifest.getRegisterUserId());
				insertData.put("gmtTime", sendManifest.getGmtTime());
				insertData.put("sendStatus", "N");
				insertData.put("entBusinessCode", sendManifest.getEntBusinessCode());
				insertData.put("updateUserId", sendManifest.getRegisterUserId());
				insertData.put("solution", "ECM");
				insertData.put("stockType", "EDI");
				insertData.put("stockSign", "EDI");
				insertData.put("stockSeqLength", "8");
				insertData.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				insertData.put("stockRemark", "sendCustomsManifestExp(GOVCBR833)");
//				insertData.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));

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
				Element lv5 = null;
				Map<String, String> attribute = new HashMap<String, String>();
				String shipperType = null;
				String ediPortalMailboxId = "";
				String origCode = null;
				String carryoutCode = null;
				
				int seq = 1;
				int houseCnt = 0;
				Map<String, Object> restData = new HashMap<String, Object>();
				List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> upsList = new ArrayList<Map<String, Object>>();
				JSONObject jsonObject = null;
				JSONArray jsonArray = new JSONArray();
				List<String> odJobNoArray = new ArrayList<String>();
				JSONObject tmpJsonObject = null;
				List<JSONArray> jsonArrayList = new ArrayList<JSONArray>();	/** 수출일경우 하우스200건씩 전송 */
				List<List<String>> odJobNoArrayList = new ArrayList<List<String>>();
				
				for(Map<String, Object> houseOrder : houseOrderList){
					if("REST".equals(cusSendType)){
						if(!ediPortalMailboxId.equals("") && !ediPortalMailboxId.equals(houseOrder.get("EDI_PORTAL_MAILBOX_ID"))){
							jsonObject = new JSONObject();
							if(houseCnt != 0) {
								jsonArrayList.add(jsonArray);
								odJobNoArrayList.add(odJobNoArray);
							}
							jsonObject.put("dataList", jsonArrayList);
							jsonObject.put("odJobNoList", odJobNoArrayList);
							restData.put(ediPortalMailboxId, jsonObject);
							restData.put(ediPortalMailboxId + "_ORIG_CODE", origCode);	/** REST전송시 사서함별로 전송/수신받고 수신받은값 처리시 사서함별로 업데이트하기위한 값으로 ORIG_CODE사용 */
							restData.put(ediPortalMailboxId + "_LOG_LIST", logList);
							if("UPSXK".equals(sendManifest.getEntCode()) && "AIR".equals(sendManifest.getBound())) {
								restData.put(ediPortalMailboxId + "_UPS_LIST", upsList);
							}
							jsonArray = new JSONArray();
							odJobNoArray = new ArrayList<String>();
							jsonArrayList = new ArrayList<JSONArray>();
							odJobNoArrayList = new ArrayList<List<String>>();
							houseCnt = 0;
							logList = new ArrayList<Map<String, Object>>();
							upsList = new ArrayList<Map<String, Object>>();
						}
						ediPortalMailboxId = Util.null2str(houseOrder.get("EDI_PORTAL_MAILBOX_ID"));
						origCode = Util.null2str(houseOrder.get("ORIG_CODE"), "noData");
					}
					
					document = documentBuilder.newDocument();
					document.setXmlVersion("1.0");
					document.setXmlStandalone(true);
					root = document.createElement("wco:Declaration");
					
					root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_833SchemaModule:1:0");
					root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_833SchemaModule:1:0");
					root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
					root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_833SchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfEXP_833SchemaModule_1.0_standard.xsd");
					document.appendChild(root);
					/** 제출세관/과 */
					this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(houseOrder.get("CUSTOMS_CODE")).trim() + Util.null2str(houseOrder.get("CUSTOMS_DPT_CODE")).trim());
					/** 신고번호 */
					this.createElement(document, root, lv1, "wco:ID", Util.null2str(houseOrder.get("SIMPLE_DOC_NO")).trim());
					/** 가격 */
					this.createElement(document, root, lv1, "wco:InvoiceAmount", ediCommonService.getNumberFormat(Util.null2str(Util.null2dbl(houseOrder.get("AMOUNT_LOC"))), "##0", 0));
					/** 총중량(단위) */
					attribute.put("attributeName", "kcsUnitCode");
					attribute.put("attributeContent", "KG");
					this.createElement(document, root, lv1, "wco:TotalGrossMassMeasure", Util.null2str(Util.null2dbl(houseOrder.get("GWT"))), attribute);
					/** 포장개수(단위) */
					attribute.put("attributeContent", "CT");
					this.createElement(document, root, lv1, "wco:TotalPackageQuantity", Util.null2str(Util.null2int(houseOrder.get("PIECES"))), attribute);
					/** 문서형태구분 */
					this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBR833");
					
					carryoutCode = Util.null2str(masterOrder.get("CARRYOUT_CODE"));
					/** 신고구분 */
					if(carryoutCode.equals("FF") || carryoutCode.equals("GG")){
						this.createElement(document, root, lv1, "kcs:SubTypeCode", "02");
					}else {
						this.createElement(document, root, lv1, "kcs:SubTypeCode", "01");
					}
					/** 반출사유 */
					this.createElement(document, root, lv1, "kcs:Reason", carryoutCode);
					/** Master AWB(B/L) 번호 */
					lv1 = this.createElement(document, root, lv1, "wco:AssociatedTransportDocument", null);
					if(Util.null2str(sendManifest.getBound()).equals("AIR")){
						this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.getSubStringK(Util.null2str(masterOrder.get("MBL_NO")).trim(), 0, 11));
					}else{
						this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.GetBLNo_ZSeqDelete(Util.null2str(masterOrder.get("MBL_NO")).trim()));
					}
					
					/** wco:BorderTransportMeans 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
					/** 출항일시(CCYYMMDDHHMM) */
					this.createElement(document, lv1, lv2, "wco:DepartureDateTime", Util.null2str(masterOrder.get("OB_DATE")).replace("-", "") + Util.null2str(masterOrder.get("OB_TIME")).replace(":", ""));
					/** 항공편명/항차 */
					if(Util.null2str(sendManifest.getBound()).equals("AIR")){
//							this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("LINE_CODE")).trim() + Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim());
						this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim());	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
					}else{
						this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("MRN")).trim());
					}
					/** 해상/항공 구분(해상:10, 항공:40) */
					if(Util.null2str(sendManifest.getBound()).equals("AIR")){this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "40");}else{this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "10");}
					/** wco:BorderTransportMeans 끝 */
					
					/** 간이수출거래구분코드(01 : 전자상거래, 02 : 기업견품, 99 : 기타 ) */
					lv1 = this.createElement(document, root, lv1, "wco:CustomsProcedure", null);
					this.createElement(document, lv1, lv2, "kcs:ProcessTypeCode", Util.null2str(houseOrder.get("ECM_TYPE")).trim());
					
					/** wco:Consignment 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
					lv2 = this.createElement(document, lv1, lv2, "wco:ConsignmentItem", null);
					lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
					/** 품명/규격 */
					this.createElement(document, lv3, lv4, "wco:CargoDescription", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("COMMODITY_ITEM")).trim(), "1"));
					
					/** 수량단위(단위) */
					if(Util.null2str(sendManifest.getBound()).equals("AIR")){
						attribute.put("attributeContent", "EA");
					}else{
						attribute.put("attributeContent", "CT");
					}
					
					if(Util.null2dbl(houseOrder.get("PIECES_REAL")) == 0){
						this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(Util.null2int(houseOrder.get("PIECES"))), attribute);
					}else{
						this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(Util.null2int(houseOrder.get("PIECES_REAL"))), attribute);
					}
//					/** 간이수출HS10단위부호 */
//					lv4 = this.createElement(document, lv3, lv4, "wco:Classification", null);
//					this.createElement(document, lv4, lv5, "wco:ID", "");
					/** wco:Consignment 끝 */
					
					/** wco:Exporter 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:Exporter", null);
					
					/** 간이수출사업자등록번호 */
					/** 간이수출사업자등록번호를 SHIPPER_BUSINESS_ID입력값으로 구분하지않고 웹에서 직접 입력하도록 수정 */
					shipperType = Util.null2str(houseOrder.get("CUSTOMS_ID_TYPE"));
					if(!Util.null2str(houseOrder.get("SHIPPER_BUSINESS_ID")).equals("")){
						attribute.put("attributeName", "schemeAgencyID");
//						if(shipperType.equals("B")){shipperType = "01";}
//						else if(shipperType.equals("F")){shipperType = "02";}
//						else if(shipperType.equals("E")){shipperType = "03";}
//						else if(shipperType.equals("D")){shipperType = "04";}
//						else if(shipperType.equals("A")){shipperType = "05";}
						switch (shipperType){
							case "01":
								attribute.put("attributeContent", "KAS");
								break;
							case "02":
								attribute.put("attributeContent", "MFA");
								break;
							case "03":
								attribute.put("attributeContent", "KIS");
								break;
							case "04":
								attribute.put("attributeContent", "KTX");
								break;
							case "05":
								attribute.put("attributeContent", "AHP");
								break;
							default:
								attribute.put("attributeContent", "KTX");
								break;
						}
						this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(houseOrder.get("SHIPPER_BUSINESS_ID")).trim(), attribute);
					}
					/** 수출화주 성명 또는 상호 */
					this.createElement(document, lv1, lv2, "wco:Name", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_NAME")).trim(), "1"), 0, 28));
					
					/** 간이수출사업자등록번호구분부호 */
					if(!shipperType.equals("")){
						this.createElement(document, lv1, lv2, "wco:RoleCode", shipperType);
					}
					
					/** wco:Address 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:Address", null);
					
//					/** 수출화주도로명코드 */
//					this.createElement(document, lv2, lv3, "wco:CountrySubDivisionID", "");	//공백처리
//					/** 수출화주상세주소 */
//					this.createElement(document, lv2, lv3, "wco:Line", "");	//공백처리
//					/** 수출화주우편번호 */
//					this.createElement(document, lv2, lv3, "wco:PostcodeID", "");	//공백처리
//					/** 수출화주건물관리번호 */
//					this.createElement(document, lv2, lv3, "kcs:BuildingNumber", "");	//공백처리
					/** 수출화주기본주소 */
					this.createElement(document, lv2, lv3, "kcs:Description", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")).trim(), "1"), 0, 150));
					/** wco:Address 끝 */
					/** wco:Exporter 끝 */
					
					/** wco:GoodsShipment 시작 */
					if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_REASON")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_YN")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_CODE"))
							|| !Util.isEmpty(houseOrder.get("GOODS_LOC_ADDR")) || !Util.isEmpty(houseOrder.get("BWH_NAME"))){
						lv1 = this.createElement(document, root, lv1, "wco:GoodsShipment", null);
						/** wco:AdditionalInformation 시작 */
						if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_REASON")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_YN"))){
							lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalInformation", null);
							/** 물품소재지검사신청사유 */
							if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_REASON"))){
								this.createElement(document, lv2, lv3, "wco:Content", Util.null2str(houseOrder.get("GOODS_LOC_INSPECT_REASON")));
							}
							/** 물품소재지검사신청여부 */
							if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_YN"))){
								this.createElement(document, lv2, lv3, "wco:StatementCode", Util.null2str(houseOrder.get("GOODS_LOC_INSPECT_YN")));
							}
						}
						/** wco:AdditionalInformation 끝 */
						
						/** wco:Consignment 시작 */
						if(!Util.isEmpty(houseOrder.get("GOODS_LOC_CODE")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_ADDR"))){
							lv2 = this.createElement(document, lv1, lv2, "wco:Consignment", null);
							/** wco:GoodsLocation 시작 */
							lv3 = this.createElement(document, lv2, lv3, "wco:GoodsLocation", null);
							/** 물품소재지 부호 */
							if(!Util.isEmpty(houseOrder.get("GOODS_LOC_CODE"))){
								this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(houseOrder.get("GOODS_LOC_CODE")));
							}
							/** 물품소재지 주소 */
							if(!Util.isEmpty(houseOrder.get("GOODS_LOC_ADDR"))){
								lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
								this.createElement(document, lv4, lv5, "kcs:Description", Util.null2str(houseOrder.get("GOODS_LOC_ADDR")));
							}
							/** wco:GoodsLocation 끝 */
						}
						/** wco:Consignment 끝 */
						
						/** 장치장소명 */
						if(!Util.isEmpty(houseOrder.get("BWH_NAME"))){
							lv2 = this.createElement(document, lv1, lv2, "wco:Warehouse", null);
							this.createElement(document, lv2, lv3, "wco:Name", Util.null2str(houseOrder.get("BWH_NAME")));
						}
					}
					/** wco:GoodsShipment 끝 */
					
					/** wco:Importer 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:Importer", null);
					/** 수취인 성명 또는 상호 */
					this.createElement(document, lv1, lv2, "wco:Name", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim(), "1"), 0, 60));
					
					/** wco:Address 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:Address", null);
					/** 주소 */
					this.createElement(document, lv2, lv3, "wco:Line", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")).trim(), "1"), 0, 150));
					/** wco:Address 끝 */
					/** wco:Importer 끝 */

					/** 적재항 */
					lv1 = this.createElement(document, root, lv1, "wco:LoadingLocation", null);
					this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(houseOrder.get("ORIG_CODE")));
					
					/** House AWB 번호 */
					lv1 = this.createElement(document, root, lv1, "wco:TransportContractDocument", null);
					this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.DeleteCharString_Loop(Util.null2str(houseOrder.get("HBL_NO")), " |-", "|"));
					
					/** 목적국(국가코드(2), 도시코드(3)을 입력함) */
					lv1 = this.createElement(document, root, lv1, "wco:TransitDestination", null);
					if(Util.null2str(sendManifest.getBound()).equals("AIR")){
						this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("DEST_COUNTRY")).trim() + Util.null2str(masterOrder.get("DEST_CODE")).trim());
					}else{
						this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("DEST_CODE")).trim());
					}
					
					out = new ByteArrayOutputStream();
					source = new DOMSource(document);
					streamResult = new StreamResult(out);
					transformer.transform(source, streamResult);
					
					if("REST".equals(cusSendType)){
						tmpJsonObject = new JSONObject();
						String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
						tmpJsonObject.put("customsReportNo", Util.null2str(houseOrder.get("SIMPLE_DOC_NO")));
						tmpJsonObject.put("xml", xml);
						jsonArray.add(tmpJsonObject);
						odJobNoArray.add(Util.null2str(houseOrder.get("OD_JOB_NO")));
						houseCnt++;
						/** 2025-03-05 MRS전송시 200건씩 전속되도록 수정 */
						if(houseCnt == 200){
							jsonArrayList.add(jsonArray);
							odJobNoArrayList.add(odJobNoArray);
							jsonArray = new JSONArray();
							odJobNoArray = new ArrayList<String>();
							houseCnt = 0;
						}
						
						String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
						String filePath = customProperties.getBackupDir() + "edi/customs/" + sendManifest.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
						String fileName = dataId + "GOVCBR109.xml";
						FileUtil.fileWrite(xml, filePath, fileName);
						
						tmpMap = new HashMap<String, Object>();
						tmpMap.put("if_file_path", filePath);
						tmpMap.put("if_file_name", fileName);
						tmpMap.put("hbl_no", houseOrder.get("HBL_NO"));
//						tmpMap.put("if_doc_reference_no", houseOrder.get("SIMPLE_DOC_NO"));
						tmpMap.put("if_doc_reference_no", houseOrder.get("HBL_NO"));
						tmpMap.put("es_job_no", dataId);
						logList.add(tmpMap);
						
						/** UPS Tracking Send */
						if(("YHTXX".equals(sendManifest.getEntCode()) || "UPSXK".equals(sendManifest.getEntCode())) && "AIR".equals(sendManifest.getBound())){
							tmpMap = new HashMap<String, Object>();
							tmpMap.put("OrdId", houseOrder.get("PO_NO"));
							tmpMap.put("MBLNo", masterOrder.get("MBL_NO"));
							tmpMap.put("SubmissionNo", houseOrder.get("SIMPLE_DOC_NO"));
							tmpMap.put("HBLNo", houseOrder.get("HBL_NO"));
							tmpMap.put("HSN", houseOrder.get("HSN"));
							tmpMap.put("DEMTrackingCode", "SNT");
							tmpMap.put("TrackingRemark", "");
							tmpMap.put("ShipperBusinessId", houseOrder.get("SHIPPER_BUSINESS_ID"));
							tmpMap.put("BoundType", "EXP");
							upsList.add(tmpMap);
						}
					}else{
						tmpInsertData = new HashMap<String, Object>();
						tmpInsertData.put("ediDesc", out.toByteArray());
						tmpInsertData.put("declarationId", Util.null2str(houseOrder.get("SIMPLE_DOC_NO")).trim());
						tmpInsertData.put("ediPortalMailboxId", houseOrder.get("EDI_PORTAL_MAILBOX_ID"));
						insertGovSendList.add(tmpInsertData);
						seq++;
					}

					
					tmpPodMap = new HashMap<String, Object>();
					tmpPodMap.put("odJobNo", houseOrder.get("OD_JOB_NO"));
					tmpPodMap.put("blNo", houseOrder.get("HBL_NO"));
					
					if(houseOrder.get("ECM110_POD_JOB_NO") != null){
						tmpPodMap.put("ECM110_POD_JOB_NO", houseOrder.get("ECM110_POD_JOB_NO"));
					}else{
						tmpPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
					}
					insertOrderPodList.add(tmpPodMap);
					
					out.close();
					
				}
				
				if(houseCnt != 0){
					jsonArrayList.add(jsonArray);
					odJobNoArrayList.add(odJobNoArray);
				}
				
				if(masterOrder.get("ECM110_POD_JOB_NO") != null){
					tmpPodMap = new HashMap<String, Object>();
					tmpPodMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
					tmpPodMap.put("blNo", masterOrder.get("MBL_NO"));
					tmpPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(++seq));
					insertOrderPodList.add(tmpPodMap);
				}
				
				result.put("podMap", podMap);
				result.put("insertOrderPodList", insertOrderPodList);
				
				if("REST".equals(cusSendType)){
					jsonObject = new JSONObject();
					jsonObject.put("dataList", jsonArrayList);
					jsonObject.put("odJobNoList", odJobNoArrayList);
					restData.put(ediPortalMailboxId, jsonObject);
					restData.put(ediPortalMailboxId + "_ORIG_CODE", origCode);
					restData.put(ediPortalMailboxId + "_LOG_LIST", logList);
					if(("YHTXX".equals(sendManifest.getEntCode()) || "UPSXK".equals(sendManifest.getEntCode())) && "AIR".equals(sendManifest.getBound())) {
						restData.put(ediPortalMailboxId + "_UPS_LIST", upsList);
					}
					
					result.put("REST", restData);
//					result.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));
					
					/** 건수가 많을경우 전송시간이 오래 걸려서 전송전에 상태변경/로그저장함  */
					manifestMapper.updateCustomsManifest(sendManifest);
					manifestMapper.updateSimpleErrorDescNull(sendManifest);
					
					IndexApi indexApi = new IndexApi(client);
					DocumentApi documentApi = null;
					String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
					if (!indexApi.existIndex(index))	indexApi.createIndex(index);
					documentApi = new DocumentApi(client);
					
					logMap.put("if_status", "READY");
					logMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
					logMap.put("if_remark", "M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "(" + houseOrderList.size() + "건)");
					logMap.put("es_job_no", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "99999");
					
					documentApi.createDocument(index, Util.null2str(logMap.get("es_job_no")), logMap);
					logMap.put("if_status", "SEND");
					logMap.put("if_remark", "");
					logMap.remove("es_job_no");
					
					result.put("logMap", logMap);
				}else{
					result.put("DB", insertGovSendList);
					result.put("insertGovSendMap", insertData);
				}
				result.put("masterOdJobNo", masterOrder.get("OD_JOB_NO"));
			}else{
				orderSaveResult.setResultCode("ERROR");
			}
		}else{
			orderSaveResult.setResultCode("ERROR");
			result.put("processCode", "0000");
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "통관목록신고 가능한 Master Order 자료가 존재하지 않습니다. 입력내용을 확인하십시오.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			result.put("processMessage", message);
		}
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	/**
	 * 수출목록신고 xml 생성
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public OrderSaveResult setCustomsManifestExpData(SendManifest sendManifest, String index) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendManifest.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		
		if(Util.null2str(sendManifest.getMasterOdJobNo()).equals("")){
			sendManifest.setOdJobNo(sendManifest.getHouseOdJobNoList().get(0));
		}
		Util.accessControlDatabase(sendManifest, "action", false, null, null, null, null);
		Map<String, Object> masterOrder = manifestMapper.selectCustomsManifestMaster(sendManifest);
		if(masterOrder != null){
			List<Map<String, Object>> houseOrderList = manifestMapper.selectCustomsManifestHouse(sendManifest);
			if (this.getCustomsManifestValidate(masterOrder, houseOrderList, tempSearchData, errList, result, "", "")){
				if("errorCheck".equals(sendManifest.getAction())){
					orderSaveResult.setResultCode("errorCheck");
					return orderSaveResult;
				}
				
				sendManifest.setPodStatus("ECM110");
				sendManifest.setSimpleDocDate(DateTime.getToday());
				
				/** 건수가 많을경우 전송시간이 오래 걸려서 전송전에 상태변경/로그저장함  */
				manifestMapper.updateCustomsManifestExp(sendManifest);
				manifestMapper.updateSimpleErrorDescNull(sendManifest);
				
				IndexApi indexApi = new IndexApi(client);
				DocumentApi documentApi = null;
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				documentApi = new DocumentApi(client);
				
				Map<String, Object> logMap = new HashMap<String, Object>();
				logMap.put("ent_code", sendManifest.getEntCode());
				logMap.put("customer_code", "");
				logMap.put("solution", "ECM");
				logMap.put("bound", sendManifest.getBound());
				logMap.put("bound_type", "EXP");
				logMap.put("if_service_code", "");
				logMap.put("if_vender", "GOV");
				logMap.put("if_send_recv_type", "S");
				logMap.put("if_send_recv_method", "");
				logMap.put("if_status_info", "");
				logMap.put("if_sender_id", "");
				logMap.put("if_receiver_id", "");
				logMap.put("register_user_id", sendManifest.getRegisterUserId());
				logMap.put("register_user_name", sendManifest.getRegisterUserName());
				logMap.put("if_doc_code", "CUSTOMS_CLEARANCE");
				logMap.put("if_doc_name", "통관목록");
				logMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
				logMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
				logMap.put("register_date_time", System.currentTimeMillis());
				logMap.put("if_status", "READY");
				logMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
				logMap.put("if_remark", "M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "(" + houseOrderList.size() + "건)");
				logMap.put("es_job_no", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "99999");
				
				documentApi.createDocument(index, Util.null2str(logMap.get("es_job_no")), logMap);
				
				if("UPSXK".equals(sendManifest.getEntCode()) && "AIR".equals(sendManifest.getBound())){
					String index2 = "ecm-ups-tracking-upsxk";
					if (!indexApi.existIndex(index2))	indexApi.createIndex(index2);
				}
				
				result.put("masterOrder", masterOrder);
				result.put("houseOrderList", houseOrderList);
			}else{
				orderSaveResult.setResultCode("ERROR");
			}
		}else{
			orderSaveResult.setResultCode("ERROR");
			result.put("processCode", "0000");
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "통관목록신고 가능한 Master Order 자료가 존재하지 않습니다. 입력내용을 확인하십시오.");
			String message = ediCommonService.getErrorCodeName(tempSearchData);
			result.put("processMessage", message);
		}
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	
	public void sendCustomsManifestExp(SendManifest sendManifest, Map<String, Object> masterOrder, List<Map<String, Object>> houseOrderList, String ediPortalMailboxId, String index) throws Exception {
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
		Element lv5 = null;
		Map<String, String> attribute = new HashMap<String, String>();
		String shipperType = null;
		String carryoutCode = null;
		
		int seq = 1;
		List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> upsList = new ArrayList<Map<String, Object>>();
		JSONArray jsonArray = new JSONArray();
		List<String> houseOdJobNoList = new ArrayList<String>();
		JSONObject tmpJsonObject = null;
		Map<String, Object> tmpMap;
		Map<String, Object> tmpPodMap = null;
		List<Map<String, Object>> insertOrderPodList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> updateOrderPodList = new ArrayList<Map<String, Object>>();
		
		for(Map<String, Object> houseOrder : houseOrderList){
			document = documentBuilder.newDocument();
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
			root = document.createElement("wco:Declaration");
			
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_833SchemaModule:1:0");
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_833SchemaModule:1:0");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_833SchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfEXP_833SchemaModule_1.0_standard.xsd");
			document.appendChild(root);
			/** 제출세관/과 */
			this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(houseOrder.get("CUSTOMS_CODE")).trim() + Util.null2str(houseOrder.get("CUSTOMS_DPT_CODE")).trim());
			/** 신고번호 */
			this.createElement(document, root, lv1, "wco:ID", Util.null2str(houseOrder.get("SIMPLE_DOC_NO")).trim());
			/** 가격 */
			this.createElement(document, root, lv1, "wco:InvoiceAmount", ediCommonService.getNumberFormat(Util.null2str(Util.null2dbl(houseOrder.get("AMOUNT_LOC"))), "##0", 0));
			/** 총중량(단위) */
			attribute.put("attributeName", "kcsUnitCode");
			attribute.put("attributeContent", "KG");
			this.createElement(document, root, lv1, "wco:TotalGrossMassMeasure", Util.null2str(Util.null2dbl(houseOrder.get("GWT"))), attribute);
			/** 포장개수(단위) */
			attribute.put("attributeContent", "CT");
			this.createElement(document, root, lv1, "wco:TotalPackageQuantity", Util.null2str(Util.null2int(houseOrder.get("PIECES"))), attribute);
			/** 문서형태구분 */
			this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBR833");
			
			carryoutCode = Util.null2str(masterOrder.get("CARRYOUT_CODE"));
			/** 신고구분 */
			if(carryoutCode.equals("FF") || carryoutCode.equals("GG")){
				this.createElement(document, root, lv1, "kcs:SubTypeCode", "02");
			}else {
				this.createElement(document, root, lv1, "kcs:SubTypeCode", "01");
			}
			/** 반출사유 */
			this.createElement(document, root, lv1, "kcs:Reason", carryoutCode);
			/** Master AWB(B/L) 번호 */
			lv1 = this.createElement(document, root, lv1, "wco:AssociatedTransportDocument", null);
			if(Util.null2str(sendManifest.getBound()).equals("AIR")){
				this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.getSubStringK(Util.null2str(masterOrder.get("MBL_NO")).trim(), 0, 11));
			}else{
				this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.GetBLNo_ZSeqDelete(Util.null2str(masterOrder.get("MBL_NO")).trim()));
			}
			
			/** wco:BorderTransportMeans 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
			/** 출항일시(CCYYMMDDHHMM) */
			this.createElement(document, lv1, lv2, "wco:DepartureDateTime", Util.null2str(masterOrder.get("OB_DATE")).replace("-", "") + Util.null2str(masterOrder.get("OB_TIME")).replace(":", ""));
			/** 항공편명/항차 */
			if(Util.null2str(sendManifest.getBound()).equals("AIR")){
//					this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("LINE_CODE")).trim() + Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim());
				this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim());	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
			}else{
				this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("MRN")).trim());
			}
			/** 해상/항공 구분(해상:10, 항공:40) */
			if(Util.null2str(sendManifest.getBound()).equals("AIR")){this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "40");}else{this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "10");}
			/** wco:BorderTransportMeans 끝 */
			
			/** 간이수출거래구분코드(01 : 전자상거래, 02 : 기업견품, 99 : 기타 ) */
			lv1 = this.createElement(document, root, lv1, "wco:CustomsProcedure", null);
			this.createElement(document, lv1, lv2, "kcs:ProcessTypeCode", Util.null2str(houseOrder.get("ECM_TYPE")).trim());
			
			/** wco:Consignment 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
			lv2 = this.createElement(document, lv1, lv2, "wco:ConsignmentItem", null);
			lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
			/** 품명/규격 */
			this.createElement(document, lv3, lv4, "wco:CargoDescription", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("COMMODITY_ITEM")).trim(), "1"));
			
			/** 수량단위(단위) */
			if(Util.null2str(sendManifest.getBound()).equals("AIR")){
				attribute.put("attributeContent", "EA");
			}else{
				attribute.put("attributeContent", "CT");
			}
			
			if(Util.null2dbl(houseOrder.get("PIECES_REAL")) == 0){
				this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(Util.null2int(houseOrder.get("PIECES"))), attribute);
			}else{
				this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(Util.null2int(houseOrder.get("PIECES_REAL"))), attribute);
			}
			/** 간이수출HS10단위부호 */
			lv4 = this.createElement(document, lv3, lv4, "wco:Classification", null);
			this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(houseOrder.get("ALLOWED_ITEM_CODE")));
			/** wco:Consignment 끝 */
			
			/** wco:Exporter 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:Exporter", null);
			
			/** 간이수출사업자등록번호 */
			/** 간이수출사업자등록번호를 SHIPPER_BUSINESS_ID입력값으로 구분하지않고 웹에서 직접 입력하도록 수정 */
			shipperType = Util.null2str(houseOrder.get("CUSTOMS_ID_TYPE"));
			if(!Util.null2str(houseOrder.get("SHIPPER_BUSINESS_ID")).equals("")){
				attribute.put("attributeName", "schemeAgencyID");
				switch (shipperType){
					case "01":
						attribute.put("attributeContent", "KAS");
						break;
					case "02":
						attribute.put("attributeContent", "MFA");
						break;
					case "03":
						attribute.put("attributeContent", "KIS");
						break;
					case "04":
						attribute.put("attributeContent", "KTX");
						break;
					case "05":
						attribute.put("attributeContent", "AHP");
						break;
					default:
						attribute.put("attributeContent", "KTX");
						break;
				}
				this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(houseOrder.get("SHIPPER_BUSINESS_ID")).trim(), attribute);
			}
			/** 수출화주 성명 또는 상호 */
			this.createElement(document, lv1, lv2, "wco:Name", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_NAME")).trim(), "1"), 0, 28));
			
			/** 간이수출사업자등록번호구분부호 */
			if(!shipperType.equals("")){
				this.createElement(document, lv1, lv2, "wco:RoleCode", shipperType);
			}
			
			/** wco:Address 시작 */
			lv2 = this.createElement(document, lv1, lv2, "wco:Address", null);
			
//			/** 수출화주도로명코드 */
//			this.createElement(document, lv2, lv3, "wco:CountrySubDivisionID", "");	//공백처리
//			/** 수출화주상세주소 */
//			this.createElement(document, lv2, lv3, "wco:Line", "");	//공백처리
//			/** 수출화주우편번호 */
//			this.createElement(document, lv2, lv3, "wco:PostcodeID", "");	//공백처리
//			/** 수출화주건물관리번호 */
//			this.createElement(document, lv2, lv3, "kcs:BuildingNumber", "");	//공백처리
			/** 수출화주기본주소 */
			this.createElement(document, lv2, lv3, "kcs:Description", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")).trim(), "1"), 0, 150));
			/** wco:Address 끝 */
			/** wco:Exporter 끝 */
			
			/** wco:GoodsShipment 시작 */
			if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_REASON")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_YN")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_CODE"))
					|| !Util.isEmpty(houseOrder.get("GOODS_LOC_ADDR")) || !Util.isEmpty(houseOrder.get("BWH_NAME"))){
				lv1 = this.createElement(document, root, lv1, "wco:GoodsShipment", null);
				/** wco:AdditionalInformation 시작 */
				if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_REASON")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_YN"))){
					lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalInformation", null);
					/** 물품소재지검사신청사유 */
					if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_REASON"))){
						this.createElement(document, lv2, lv3, "wco:Content", Util.null2str(houseOrder.get("GOODS_LOC_INSPECT_REASON")));
					}
					/** 물품소재지검사신청여부 */
					if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_YN"))){
						this.createElement(document, lv2, lv3, "wco:StatementCode", Util.null2str(houseOrder.get("GOODS_LOC_INSPECT_YN")));
					}
				}
				/** wco:AdditionalInformation 끝 */
				
				/** wco:Consignment 시작 */
				if(!Util.isEmpty(houseOrder.get("GOODS_LOC_CODE")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_ADDR"))){
					lv2 = this.createElement(document, lv1, lv2, "wco:Consignment", null);
					/** wco:GoodsLocation 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:GoodsLocation", null);
					/** 물품소재지 부호 */
					if(!Util.isEmpty(houseOrder.get("GOODS_LOC_CODE"))){
						this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(houseOrder.get("GOODS_LOC_CODE")));
					}
					/** 물품소재지 주소 */
					if(!Util.isEmpty(houseOrder.get("GOODS_LOC_ADDR"))){
						lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
						this.createElement(document, lv4, lv5, "kcs:Description", Util.null2str(houseOrder.get("GOODS_LOC_ADDR")));
					}
					/** wco:GoodsLocation 끝 */
				}
				/** wco:Consignment 끝 */
				
				/** 장치장소명 */
				if(!Util.isEmpty(houseOrder.get("BWH_NAME"))){
					lv2 = this.createElement(document, lv1, lv2, "wco:Warehouse", null);
					this.createElement(document, lv2, lv3, "wco:Name", Util.null2str(houseOrder.get("BWH_NAME")));
				}
			}
			/** wco:GoodsShipment 끝 */
			
			/** wco:Importer 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:Importer", null);
			/** 수취인 성명 또는 상호 */
			this.createElement(document, lv1, lv2, "wco:Name", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim(), "1"), 0, 60));
			
			/** wco:Address 시작 */
			lv2 = this.createElement(document, lv1, lv2, "wco:Address", null);
			/** 주소 */
			// 2026-03-05 주소 값에 (,) 괄호가 들어가있으면 빈값으로 대체 
			String consigneeAddress = Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")).replaceAll("[()]", "");
			this.createElement(document, lv2, lv3, "wco:Line", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(consigneeAddress.trim(), "1"), 0, 150));
			/** wco:Address 끝 */
			/** wco:Importer 끝 */

			/** 적재항 */
			lv1 = this.createElement(document, root, lv1, "wco:LoadingLocation", null);
			this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(houseOrder.get("ORIG_CODE")));
			
			/** House AWB 번호 */
			lv1 = this.createElement(document, root, lv1, "wco:TransportContractDocument", null);
			this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.DeleteCharString_Loop(Util.null2str(houseOrder.get("HBL_NO")), " |-", "|"));
			
			/** 목적국(국가코드(2), 도시코드(3)을 입력함) */
			lv1 = this.createElement(document, root, lv1, "wco:TransitDestination", null);
			if(Util.null2str(sendManifest.getBound()).equals("AIR")){
				this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("DEST_COUNTRY")).trim() + Util.null2str(masterOrder.get("DEST_CODE")).trim());
			}else{
				this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("DEST_CODE")).trim());
			}
			
			out = new ByteArrayOutputStream();
			source = new DOMSource(document);
			streamResult = new StreamResult(out);
			transformer.transform(source, streamResult);
			
			
			
			tmpJsonObject = new JSONObject();
			String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
			tmpJsonObject.put("customsReportNo", Util.null2str(houseOrder.get("SIMPLE_DOC_NO")));
			tmpJsonObject.put("xml", xml);
			jsonArray.add(tmpJsonObject);
			houseOdJobNoList.add(Util.null2str(houseOrder.get("OD_JOB_NO")));
			
			String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
			String filePath = customProperties.getBackupDir() + "edi/customs/" + sendManifest.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
			String fileName = dataId + "GOVCBR109.xml";
			FileUtil.fileWrite(xml, filePath, fileName);
			
			out.close();
			
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("if_file_path", filePath);
			tmpMap.put("if_file_name", fileName);
			tmpMap.put("hbl_no", houseOrder.get("HBL_NO"));
			tmpMap.put("if_doc_reference_no", houseOrder.get("SIMPLE_DOC_NO"));
			tmpMap.put("es_job_no", dataId);
			logList.add(tmpMap);
			
			/** UPS Tracking Send */
			if(("YHTXX".equals(sendManifest.getEntCode()) || "UPSXK".equals(sendManifest.getEntCode())) && "AIR".equals(sendManifest.getBound())){
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("OrdId", houseOrder.get("PO_NO"));
				tmpMap.put("MBLNo", masterOrder.get("MBL_NO"));
				tmpMap.put("SubmissionNo", houseOrder.get("SIMPLE_DOC_NO"));
				tmpMap.put("HBLNo", houseOrder.get("HBL_NO"));
				tmpMap.put("HSN", houseOrder.get("HSN"));
				tmpMap.put("DEMTrackingCode", "SNT");
				tmpMap.put("TrackingRemark", "");
				tmpMap.put("ShipperBusinessId", houseOrder.get("SHIPPER_BUSINESS_ID"));
				tmpMap.put("BoundType", "EXP");
				tmpMap.put("es_job_no", dataId);
				upsList.add(tmpMap);
			}

			
			tmpPodMap = new HashMap<String, Object>();
			tmpPodMap.put("odJobNo", houseOrder.get("OD_JOB_NO"));
			tmpPodMap.put("blNo", houseOrder.get("HBL_NO"));
			if(houseOrder.get("ECM111_POD_JOB_NO") != null) {
				tmpPodMap.put("ECM111_POD_JOB_NO", houseOrder.get("ECM111_POD_JOB_NO"));
				updateOrderPodList.add(tmpPodMap);
			}
			if(houseOrder.get("ECM110_POD_JOB_NO") != null){
				tmpPodMap.put("ECM110_POD_JOB_NO", houseOrder.get("ECM110_POD_JOB_NO"));
				updateOrderPodList.add(tmpPodMap);
			}else{ 
				if(houseOrder.get("ECM111_POD_JOB_NO") != null) {
					tmpPodMap.put("ECM111_POD_JOB_NO", houseOrder.get("ECM111_POD_JOB_NO"));
				}
				tmpPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
				insertOrderPodList.add(tmpPodMap);
			}
		}	/** houseOrderList for End */
		
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("dataList", jsonArray);
		
		jsonObject = ediCommonService.sendEbms(ediPortalMailboxId, "GOVCBR833", jsonObject, "LIST");
		boolean success = true;	// 목록전송성공여부
		String podStatus = "ECM110";
		
		if(!Util.null2str(jsonObject.get("customsErrCode")).equals("") && !Util.null2str(jsonObject.get("customsErrCode")).equals("C200")) {
			success = false;
			podStatus = "ECM111";
		}
		sendManifest.setCustomsErrCode(Util.null2str(jsonObject.get("customsErrCode")));
		sendManifest.setHouseOdJobNoList(houseOdJobNoList);
		sendManifest.setAction("REST");
		sendManifest.setPodStatus(podStatus);
		manifestMapper.updateCustomsManifestExp(sendManifest);
		
		// n번에 걸쳐 전송하는 경우 한 번이라도 전송 실패하면 마스터 POD "목록전송실패"
		if(masterOrder.get(podStatus+"_POD_JOB_NO") == null && !(success && "Y".equals(masterOrder.get("ECM111_POD_JOB_NO")))){
			tmpPodMap = new HashMap<String, Object>();
			tmpPodMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
			tmpPodMap.put("blNo", masterOrder.get("MBL_NO"));
			tmpPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(++seq));
			insertOrderPodList.add(tmpPodMap);
		}
		
		if(!"Y".equals(masterOrder.get(podStatus+"_POD_JOB_NO")) && !(success && "Y".equals(masterOrder.get("ECM111_POD_JOB_NO")))){
			Map<String, Object> masterLastPodMap = new HashMap<String, Object>();
			masterLastPodMap.put("lastPodStatus", podStatus);
			masterLastPodMap.put("gmtTime", sendManifest.getGmtTime());
			masterLastPodMap.put("updateType", "odJobNo");
			masterLastPodMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
			commonMapper.updateLastPodStatus(masterLastPodMap);
		}
		masterOrder.put(podStatus+"_POD_JOB_NO", "Y");
		
		Map<String, Object> logMap = new HashMap<String, Object>();
		logMap.put("ent_code", sendManifest.getEntCode());
		logMap.put("customer_code", "");
		logMap.put("solution", "ECM");
		logMap.put("bound", sendManifest.getBound());
		logMap.put("bound_type", "EXP");
		logMap.put("if_service_code", "");
		logMap.put("if_vender", "GOV");
		logMap.put("if_send_recv_type", "S");
		logMap.put("if_send_recv_method", "");
		logMap.put("if_status_info", "");
		logMap.put("if_sender_id", "");
		logMap.put("if_receiver_id", "");
		logMap.put("register_user_id", sendManifest.getRegisterUserId());
		logMap.put("register_user_name", sendManifest.getRegisterUserName());
		logMap.put("if_doc_code", "CUSTOMS_CLEARANCE");
		logMap.put("if_doc_name", "통관목록");
		logMap.put("customs_err_desc", jsonObject.get("customsErrDesc"));
		logMap.put("customs_err_desc_additional", jsonObject.get("body"));
		logMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
		logMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
		logMap.put("register_date_time", System.currentTimeMillis());
		logMap.put("if_status", "SEND");
		logMap.put("if_remark", "");
		BulkData bulkData;
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		ArrayList<BulkData> bulkList2 = new ArrayList<BulkData>();
		String index2 = "ecm-ups-tracking-" + Util.null2str(sendManifest.getEntCode()).toLowerCase();
		
		for(Map<String, Object> logData : logList){
			logData.putAll(logMap);
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(Util.null2str(logData.get("es_job_no")));
			bulkData.setJsonMap(logData);
			bulkList.add(bulkData);
		}
		
		if(success && upsList.size() > 0){
			long trackingDateTime = System.currentTimeMillis();
			for(Map<String, Object> upsData : upsList) {
				upsData.put("TrackingDateTime", trackingDateTime);
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index2);
				bulkData.setId(Util.null2str(upsData.get("es_job_no")));
				bulkData.setJsonMap(upsData);
				bulkList2.add(bulkData);
			}
		}
		
		if (bulkList.size() > 0) {
			DocumentApi documentApi = new DocumentApi(client);
			documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
		}
		if (bulkList2.size() > 0) {
			DocumentApi documentApi = new DocumentApi(client);
			documentApi.bulkDocumentWithBulkProcessor(bulkList2, 10000);
		}
		
		SqlSession sqlSession = null;
		int cnt = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", sendManifest.getEntCode());
			podMap.put("gmtTime", sendManifest.getGmtTime());
			podMap.put("updateUserId", sendManifest.getUpdateUserId());
			podMap.put("podStatus", podStatus);
			podMap.put("podTableName", 	"TB_ECM_POD_02");
			for(Map<String, Object> insertOrderPodMap : insertOrderPodList){
				// 전송 실패 > 재전송 실패 시 기존의 목록전송실패 POD를 UPDATE하고, INSERT는 되지 않게 막음
				if((!success && !Util.null2str(insertOrderPodMap.get("ECM111_POD_JOB_NO")).equals(""))
				) {
					continue;
				}
				podMap.putAll(insertOrderPodMap);
				orderBatchMapper.insertOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			cnt = 1;
			for(Map<String, Object> updateOrderPodMap : updateOrderPodList){
				if("".equals(Util.null2str(updateOrderPodMap.get(podStatus+"_POD_JOB_NO")))) {
					continue;
				}
				updateOrderPodMap.put("podJobNo", updateOrderPodMap.get(podStatus+"_POD_JOB_NO"));
				podMap.putAll(updateOrderPodMap);
				orderBatchMapper.updateOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			/** 통관목록전송시 오류통보&목록전송실패POD삭제 / 통관목록실패시 오류통보&목록전송POD삭제 */
			cnt = 1;
			podMap.put("podStatus", "ECM115");
			for(Map<String, Object> deleteOrderPodMap : insertOrderPodList){
				podMap.putAll(deleteOrderPodMap);
				orderBatchMapper.deleteOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			for(Map<String, Object> deleteOrderPodMap : updateOrderPodList){
				if("".equals(Util.null2str(deleteOrderPodMap.get(podStatus+"_POD_JOB_NO")))) {
					continue;
				}
				podMap.putAll(deleteOrderPodMap);
				orderBatchMapper.deleteOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			podMap.put("podStatus", success ? "ECM111" : "ECM110");
			for(Map<String, Object> deleteOrderPodMap : insertOrderPodList){
				if(deleteOrderPodMap.get("odJobNo").equals(masterOrder.get("OD_JOB_NO")) 
						&& (success && !Util.null2str(deleteOrderPodMap.get("ECM111_POD_JOB_NO")).equals(""))
				) {
					continue;
				}
				podMap.putAll(deleteOrderPodMap);
				orderBatchMapper.deleteOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			for(Map<String, Object> deleteOrderPodMap : updateOrderPodList){
				if("".equals(Util.null2str(deleteOrderPodMap.get(podStatus+"_POD_JOB_NO")))) {
					continue;
				}
				podMap.putAll(deleteOrderPodMap);
				orderBatchMapper.deleteOrderPod(podMap);
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
	
	public OrderSaveResult setCustomsManifestExpDataTest(SendManifest sendManifest) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendManifest.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		String message = null;
		
		if(Util.null2str(sendManifest.getMasterOdJobNo()).equals("")){
			sendManifest.setOdJobNo(sendManifest.getHouseOdJobNoList().get(0));
		}
		Util.accessControlDatabase(sendManifest, "action", false, null, null, null, null);
		Map<String, Object> masterOrder = manifestMapper.selectCustomsManifestMaster(sendManifest);
		if(masterOrder != null){
			List<Map<String, Object>> houseOrderList = manifestMapper.selectCustomsManifestHouse(sendManifest);
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			Map<String, Object> logMap = new HashMap<String, Object>();
			Map<String, Object> insertData = new HashMap<String, Object>();
			
			Map<String, Object> tmpPodMap = null;
			List<Map<String, Object>> insertOrderPodList = new ArrayList<Map<String, Object>>();
			
			Map<String, Object> tmpMap = null;
			
			Map<String, Object> tmpInsertData = null;
			List<Map<String, Object>> insertGovSendList = new ArrayList<Map<String, Object>>();
			
			podMap.put("entCode", sendManifest.getEntCode());
			podMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
			podMap.put("solution", "ECM");
			podMap.put("optionCode", "C04");
//			String consigneeNameType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
			podMap.put("optionCode", "C05");
//			String consigneeAddressType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
			
			if (this.getCustomsManifestValidate(masterOrder, houseOrderList, tempSearchData, errList, result, "", "") && !Util.null2str(sendManifest.getPenaltyYn()).equals("Y")){
				
				if("errorCheck".equals(sendManifest.getAction())){
					orderSaveResult.setResultCode("errorCheck");
					return orderSaveResult;
				}
				
				String today = DateTime.getToday();
				sendManifest.setPodStatus("ECM110");
				sendManifest.setSimpleDocDate(today);
				result.put("updateCustomsManifestData", sendManifest);
				
				podMap.put("userLanguage", sendManifest.getUserLanguage());
				podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
				podMap.put("srchCode", "ECM110");
				podMap.put("entCode", sendManifest.getEntCode());
				podMap.put("entBusinessCode", "");
				podMap.put("gmtTime", sendManifest.getGmtTime());
				podMap.put("updateUserId", sendManifest.getUpdateUserId());
				podMap.put("stockType", "POD");
				podMap.put("stockSign", "POD");
				podMap.put("stockSeqLength", 8);
				podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				podMap.put("stockRemark", "ECM POD JOB NO");
				podMap.put("podStatus", "ECM110");
				podMap.put("podTableName", 	"TB_ECM_POD_02");
				
				logMap.put("ent_code", sendManifest.getEntCode());
				logMap.put("customer_code", "");
				logMap.put("solution", "ECM");
				logMap.put("bound", sendManifest.getBound());
				logMap.put("bound_type", "EXP");
				logMap.put("if_service_code", "");
				logMap.put("if_vender", "GOV");
				logMap.put("if_send_recv_type", "S");
				logMap.put("if_send_recv_method", "");
				logMap.put("if_status_info", "");
				logMap.put("if_sender_id", "");
				logMap.put("if_receiver_id", "");
				logMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
				logMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
				logMap.put("register_user_id", sendManifest.getRegisterUserId());
				logMap.put("register_user_name", sendManifest.getRegisterUserName());
				logMap.put("register_date_time", System.currentTimeMillis());
				logMap.put("if_doc_code", "CUSTOMS_CLEARANCE");
				logMap.put("if_doc_name", "통관목록");
				
				insertData.put("entCode", sendManifest.getEntCode());
				insertData.put("typeCode", "GOVCBR833");
				insertData.put("registerUserId", sendManifest.getRegisterUserId());
				insertData.put("gmtTime", sendManifest.getGmtTime());
				insertData.put("sendStatus", "N");
				insertData.put("entBusinessCode", sendManifest.getEntBusinessCode());
				insertData.put("updateUserId", sendManifest.getRegisterUserId());
				insertData.put("solution", "ECM");
				insertData.put("stockType", "EDI");
				insertData.put("stockSign", "EDI");
				insertData.put("stockSeqLength", "8");
				insertData.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				insertData.put("stockRemark", "sendCustomsManifestExp(GOVCBR833)");
//				insertData.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));

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
				Element lv5 = null;
				Map<String, String> attribute = new HashMap<String, String>();
				String shipperType = null;
				String ediPortalMailboxId = "";
				String origCode = null;
				String carryoutCode = null;
				
				int seq = 1;
				int houseCnt = 0;
				Map<String, Object> restData = new HashMap<String, Object>();
				List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> upsList = new ArrayList<Map<String, Object>>();
				JSONObject jsonObject = null;
				JSONArray jsonArray = new JSONArray();
				List<String> odJobNoArray = new ArrayList<String>();
				JSONObject tmpJsonObject = null;
				List<JSONArray> jsonArrayList = new ArrayList<JSONArray>();	/** 수출일경우 하우스300건씩 전송 */
				List<List<String>> odJobNoArrayList = new ArrayList<List<String>>();
				
				for(Map<String, Object> houseOrder : houseOrderList){
					if(!ediPortalMailboxId.equals("") && !ediPortalMailboxId.equals(houseOrder.get("EDI_PORTAL_MAILBOX_ID"))){
						jsonObject = new JSONObject();
						if(houseCnt != 0) {
							jsonArrayList.add(jsonArray);
							odJobNoArrayList.add(odJobNoArray);
						}
						jsonObject.put("dataList", jsonArrayList);
						jsonObject.put("odJobNoList", odJobNoArrayList);
						restData.put(ediPortalMailboxId, jsonObject);
						restData.put(ediPortalMailboxId + "_ORIG_CODE", origCode);	/** REST전송시 사서함별로 전송/수신받고 수신받은값 처리시 사서함별로 업데이트하기위한 값으로 ORIG_CODE사용 */
						restData.put(ediPortalMailboxId + "_LOG_LIST", logList);
						jsonArray = new JSONArray();
						odJobNoArray = new ArrayList<String>();
						jsonArrayList = new ArrayList<JSONArray>();
						odJobNoArrayList = new ArrayList<List<String>>();
						houseCnt = 0;
						logList = new ArrayList<Map<String, Object>>();
						upsList = new ArrayList<Map<String, Object>>();
					}
					ediPortalMailboxId = Util.null2str(houseOrder.get("EDI_PORTAL_MAILBOX_ID"));
					origCode = Util.null2str(houseOrder.get("ORIG_CODE"), "noData");
					
					document = documentBuilder.newDocument();
					document.setXmlVersion("1.0");
					document.setXmlStandalone(true);
					root = document.createElement("wco:Declaration");
					
					root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_833SchemaModule:1:0");
					root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_833SchemaModule:1:0");
					root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
					root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_833SchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfEXP_833SchemaModule_1.0_standard.xsd");
					document.appendChild(root);
					/** 제출세관/과 */
					this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(houseOrder.get("CUSTOMS_CODE")).trim() + Util.null2str(houseOrder.get("CUSTOMS_DPT_CODE")).trim());
					/** 신고번호 */
					this.createElement(document, root, lv1, "wco:ID", Util.null2str(houseOrder.get("SIMPLE_DOC_NO")).trim());
					/** 가격 */
					this.createElement(document, root, lv1, "wco:InvoiceAmount", ediCommonService.getNumberFormat(Util.null2str(Util.null2dbl(houseOrder.get("AMOUNT_LOC"))), "##0", 0));
					/** 총중량(단위) */
					attribute.put("attributeName", "kcsUnitCode");
					attribute.put("attributeContent", "KG");
					this.createElement(document, root, lv1, "wco:TotalGrossMassMeasure", Util.null2str(Util.null2dbl(houseOrder.get("GWT"))), attribute);
					/** 포장개수(단위) */
					attribute.put("attributeContent", "CT");
					this.createElement(document, root, lv1, "wco:TotalPackageQuantity", Util.null2str(Util.null2int(houseOrder.get("PIECES"))), attribute);
					/** 문서형태구분 */
					this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBR833");
					
					carryoutCode = Util.null2str(masterOrder.get("CARRYOUT_CODE"));
					/** 신고구분 */
					if(carryoutCode.equals("FF") || carryoutCode.equals("GG")){
						this.createElement(document, root, lv1, "kcs:SubTypeCode", "02");
					}else {
						this.createElement(document, root, lv1, "kcs:SubTypeCode", "01");
					}
					/** 반출사유 */
					this.createElement(document, root, lv1, "kcs:Reason", carryoutCode);
					/** Master AWB(B/L) 번호 */
					lv1 = this.createElement(document, root, lv1, "wco:AssociatedTransportDocument", null);
					if(Util.null2str(sendManifest.getBound()).equals("AIR")){
						this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.getSubStringK(Util.null2str(masterOrder.get("MBL_NO")).trim(), 0, 11));
					}else{
						this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.GetBLNo_ZSeqDelete(Util.null2str(masterOrder.get("MBL_NO")).trim()));
					}
					
					/** wco:BorderTransportMeans 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
					/** 출항일시(CCYYMMDDHHMM) */
					this.createElement(document, lv1, lv2, "wco:DepartureDateTime", Util.null2str(masterOrder.get("OB_DATE")).replace("-", "") + Util.null2str(masterOrder.get("OB_TIME")).replace(":", ""));
					/** 항공편명/항차 */
					if(Util.null2str(sendManifest.getBound()).equals("AIR")){
						this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim());	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
					}else{
						this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("MRN")).trim());
					}
					/** 해상/항공 구분(해상:10, 항공:40) */
					if(Util.null2str(sendManifest.getBound()).equals("AIR")){this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "40");}else{this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "10");}
					/** wco:BorderTransportMeans 끝 */
					
					/** 간이수출거래구분코드(01 : 전자상거래, 02 : 기업견품, 99 : 기타 ) */
					lv1 = this.createElement(document, root, lv1, "wco:CustomsProcedure", null);
					this.createElement(document, lv1, lv2, "kcs:ProcessTypeCode", Util.null2str(houseOrder.get("ECM_TYPE")).trim());
					
					/** wco:Consignment 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
					lv2 = this.createElement(document, lv1, lv2, "wco:ConsignmentItem", null);
					lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
					/** 품명/규격 */
					this.createElement(document, lv3, lv4, "wco:CargoDescription", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("COMMODITY_ITEM")).trim(), "1"));
					
					/** 수량단위(단위) */
					if(Util.null2str(sendManifest.getBound()).equals("AIR")){
						attribute.put("attributeContent", "EA");
					}else{
						attribute.put("attributeContent", "CT");
					}
					
					if(Util.null2dbl(houseOrder.get("PIECES_REAL")) == 0){
						this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(Util.null2int(houseOrder.get("PIECES"))), attribute);
					}else{
						this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(Util.null2int(houseOrder.get("PIECES_REAL"))), attribute);
					}
					/** wco:Consignment 끝 */
					
					/** wco:Exporter 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:Exporter", null);
					
					/** 간이수출사업자등록번호 */
					/** 간이수출사업자등록번호를 SHIPPER_BUSINESS_ID입력값으로 구분하지않고 웹에서 직접 입력하도록 수정 */
					shipperType = Util.null2str(houseOrder.get("CUSTOMS_ID_TYPE"));
					if(!Util.null2str(houseOrder.get("SHIPPER_BUSINESS_ID")).equals("")){
						attribute.put("attributeName", "schemeAgencyID");
						switch (shipperType){
							case "01":
								attribute.put("attributeContent", "KAS");
								break;
							case "02":
								attribute.put("attributeContent", "MFA");
								break;
							case "03":
								attribute.put("attributeContent", "KIS");
								break;
							case "04":
								attribute.put("attributeContent", "KTX");
								break;
							case "05":
								attribute.put("attributeContent", "AHP");
								break;
							default:
								attribute.put("attributeContent", "KTX");
								break;
						}
						this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(houseOrder.get("SHIPPER_BUSINESS_ID")).trim(), attribute);
					}
					/** 수출화주 성명 또는 상호 */
					this.createElement(document, lv1, lv2, "wco:Name", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_NAME")).trim(), "1"), 0, 28));
					
					/** 간이수출사업자등록번호구분부호 */
					if(!shipperType.equals("")){
						this.createElement(document, lv1, lv2, "wco:RoleCode", shipperType);
					}
					
					/** wco:Address 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:Address", null);
					
					/** 수출화주기본주소 */
					this.createElement(document, lv2, lv3, "kcs:Description", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")).trim(), "1"), 0, 150));
					/** wco:Address 끝 */
					/** wco:Exporter 끝 */
					
					/** wco:GoodsShipment 시작 */
					if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_REASON")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_YN")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_CODE"))
							|| !Util.isEmpty(houseOrder.get("GOODS_LOC_ADDR")) || !Util.isEmpty(houseOrder.get("BWH_NAME"))){
						lv1 = this.createElement(document, root, lv1, "wco:GoodsShipment", null);
						/** wco:AdditionalInformation 시작 */
						if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_REASON")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_YN"))){
							lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalInformation", null);
							/** 물품소재지검사신청사유 */
							if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_REASON"))){
								this.createElement(document, lv2, lv3, "wco:Content", Util.null2str(houseOrder.get("GOODS_LOC_INSPECT_REASON")));
							}
							/** 물품소재지검사신청여부 */
							if(!Util.isEmpty(houseOrder.get("GOODS_LOC_INSPECT_YN"))){
								this.createElement(document, lv2, lv3, "wco:StatementCode", Util.null2str(houseOrder.get("GOODS_LOC_INSPECT_YN")));
							}
						}
						/** wco:AdditionalInformation 끝 */
						
						/** wco:Consignment 시작 */
						if(!Util.isEmpty(houseOrder.get("GOODS_LOC_CODE")) || !Util.isEmpty(houseOrder.get("GOODS_LOC_ADDR"))){
							lv2 = this.createElement(document, lv1, lv2, "wco:Consignment", null);
							/** wco:GoodsLocation 시작 */
							lv3 = this.createElement(document, lv2, lv3, "wco:GoodsLocation", null);
							/** 물품소재지 부호 */
							if(!Util.isEmpty(houseOrder.get("GOODS_LOC_CODE"))){
								this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(houseOrder.get("GOODS_LOC_CODE")));
							}
							/** 물품소재지 주소 */
							if(!Util.isEmpty(houseOrder.get("GOODS_LOC_ADDR"))){
								lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
								this.createElement(document, lv4, lv5, "kcs:Description", Util.null2str(houseOrder.get("GOODS_LOC_ADDR")));
							}
							/** wco:GoodsLocation 끝 */
						}
						/** wco:Consignment 끝 */
						
						/** 장치장소명 */
						if(!Util.isEmpty(houseOrder.get("BWH_NAME"))){
							lv2 = this.createElement(document, lv1, lv2, "wco:Warehouse", null);
							this.createElement(document, lv2, lv3, "wco:Name", Util.null2str(houseOrder.get("BWH_NAME")));
						}
					}
					/** wco:GoodsShipment 끝 */
					
					/** wco:Importer 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:Importer", null);
					/** 수취인 성명 또는 상호 */
					this.createElement(document, lv1, lv2, "wco:Name", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim(), "1"), 0, 60));
					
					/** wco:Address 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:Address", null);
					/** 주소 */
					this.createElement(document, lv2, lv3, "wco:Line", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")).trim(), "1"), 0, 150));
					/** wco:Address 끝 */
					/** wco:Importer 끝 */

					/** 적재항 */
					lv1 = this.createElement(document, root, lv1, "wco:LoadingLocation", null);
					this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(houseOrder.get("ORIG_CODE")));
					
					/** House AWB 번호 */
					lv1 = this.createElement(document, root, lv1, "wco:TransportContractDocument", null);
					this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.DeleteCharString_Loop(Util.null2str(houseOrder.get("HBL_NO")), " |-", "|"));
					
					/** 목적국(국가코드(2), 도시코드(3)을 입력함) */
					lv1 = this.createElement(document, root, lv1, "wco:TransitDestination", null);
					if(Util.null2str(sendManifest.getBound()).equals("AIR")){
						this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("DEST_COUNTRY")).trim() + Util.null2str(masterOrder.get("DEST_CODE")).trim());
					}else{
						this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("DEST_CODE")).trim());
					}
					
					out = new ByteArrayOutputStream();
					source = new DOMSource(document);
					streamResult = new StreamResult(out);
					transformer.transform(source, streamResult);
					
					tmpJsonObject = new JSONObject();
					String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
					tmpJsonObject.put("customsReportNo", Util.null2str(houseOrder.get("SIMPLE_DOC_NO")));
					tmpJsonObject.put("xml", xml);
					jsonArray.add(tmpJsonObject);
					odJobNoArray.add(Util.null2str(houseOrder.get("OD_JOB_NO")));
					houseCnt++;
					/** 2025-03-05 MRS전송시 200건씩 전속되도록 수정 */
					if(houseCnt == 200){
						jsonArrayList.add(jsonArray);
						odJobNoArrayList.add(odJobNoArray);
						jsonArray = new JSONArray();
						odJobNoArray = new ArrayList<String>();
						houseCnt = 0;
					}
					
					String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
					String filePath = customProperties.getBackupDir() + "edi/customs/" + sendManifest.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
					String fileName = dataId + "GOVCBR109.xml";
					FileUtil.fileWrite(xml, filePath, fileName);
					
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("if_file_path", filePath);
					tmpMap.put("if_file_name", fileName);
					tmpMap.put("hbl_no", houseOrder.get("HBL_NO"));
					tmpMap.put("if_doc_reference_no", houseOrder.get("HBL_NO"));
					tmpMap.put("es_job_no", dataId);
					logList.add(tmpMap);
					
					tmpPodMap = new HashMap<String, Object>();
					tmpPodMap.put("odJobNo", houseOrder.get("OD_JOB_NO"));
					tmpPodMap.put("blNo", houseOrder.get("HBL_NO"));
					if(houseOrder.get("ECM110_POD_JOB_NO") != null){
						tmpPodMap.put("ECM110_POD_JOB_NO", houseOrder.get("ECM110_POD_JOB_NO"));
					}else{
						tmpPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
					}
					insertOrderPodList.add(tmpPodMap);
					
					out.close();
					
				}
				
				if(houseCnt != 0){
					jsonArrayList.add(jsonArray);
					odJobNoArrayList.add(odJobNoArray);
				}
				
				if(masterOrder.get("ECM110_POD_JOB_NO") != null){
					tmpPodMap = new HashMap<String, Object>();
					tmpPodMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
					tmpPodMap.put("blNo", masterOrder.get("MBL_NO"));
					tmpPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(++seq));
					insertOrderPodList.add(tmpPodMap);
				}
				
				result.put("podMap", podMap);
				result.put("insertOrderPodList", insertOrderPodList);
				
				jsonObject = new JSONObject();
				jsonObject.put("dataList", jsonArrayList);
				jsonObject.put("odJobNoList", odJobNoArrayList);
				restData.put(ediPortalMailboxId, jsonObject);
				restData.put(ediPortalMailboxId + "_ORIG_CODE", origCode);
				restData.put(ediPortalMailboxId + "_LOG_LIST", logList);
				
				result.put("REST", restData);
				
				/** 건수가 많을경우 전송시간이 오래 걸려서 전송전에 상태변경/로그저장함  */
//				manifestMapper.updateCustomsManifest(sendManifest);
//				manifestMapper.updateSimpleErrorDescNull(sendManifest);
				
//				IndexApi indexApi = new IndexApi(client);
//				DocumentApi documentApi = null;
//				String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//				documentApi = new DocumentApi(client);
				
				logMap.put("if_status", "READY");
				logMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
				logMap.put("if_remark", "M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "(" + houseOrderList.size() + "건)");
				logMap.put("es_job_no", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "99999");
				
//				documentApi.createDocument(index, Util.null2str(logMap.get("es_job_no")), logMap);
				logMap.put("if_status", "SEND");
				logMap.put("if_remark", "");
				logMap.remove("es_job_no");
				
				result.put("logMap", logMap);
				result.put("masterOdJobNo", masterOrder.get("OD_JOB_NO"));
			}else{
				orderSaveResult.setResultCode("ERROR");
			}
		}else{
			orderSaveResult.setResultCode("ERROR");
			result.put("processCode", "0000");
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "통관목록신고 가능한 Master Order 자료가 존재하지 않습니다. 입력내용을 확인하십시오.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			result.put("processMessage", message);
		}
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	
	public void setCustomsManifestExpMrn(String ediPortalMailboxId, JSONObject sendData, Map<String, Object> logMap, SendManifest updateCustomsManifestData, Map<String, Object> restData, List<String> houseOdJobNoList) throws Exception {
		try{
			JSONObject jsonObject = ediCommonService.sendEbms(ediPortalMailboxId, "GOVCBR833", sendData, "LIST");
			logMap.put("customs_err_desc", jsonObject.get("customsErrDesc"));
			logMap.put("customs_err_desc_additional", jsonObject.get("body"));
			updateCustomsManifestData.setCustomsErrCode(Util.null2str(jsonObject.get("customsErrCode")));
			updateCustomsManifestData.setOrigCode(Util.null2str(restData.get(ediPortalMailboxId + "_ORIG_CODE")));
			updateCustomsManifestData.setHouseOdJobNoList(houseOdJobNoList);
			updateCustomsManifestData.setAction("REST");
			manifestMapper.updateCustomsManifest(updateCustomsManifestData);
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			try{
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("houseOdJobNoList", houseOdJobNoList);
				ediCommonService.asyncErrorSendEmail("EdiCustomsManifestSendService.setCustomsManifestExpMrn", map, e);
			}catch(Exception ee){
				log.error(Util.getPrintStacTraceString(ee));
			}
		}
	}
	public void setCustomsManifestExp(SendManifest sendManifest, String cusSendType, OrderSaveResult orderSaveResult, ArrayList<BulkData> bulkList, ArrayList<BulkData> bulkList2, String index) throws Exception {
		Map<String, Object> resultData = orderSaveResult.getResultData();
		String index2 = "ecm-ups-tracking-" + Util.null2str(sendManifest.getEntCode()).toLowerCase();
		SqlSession sqlSession = null;
		int cnt = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			if (bulkList != null && bulkList.size() > 0) {
				IndexApi indexApi = new IndexApi(client);
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				
				DocumentApi documentApi = new DocumentApi(client);
				documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
			}
			if (bulkList2 != null && bulkList2.size() > 0) {
				IndexApi indexApi = new IndexApi(client);
				if (!indexApi.existIndex(index2))	indexApi.createIndex(index2);
				
				DocumentApi documentApi = new DocumentApi(client);
				documentApi.bulkDocumentWithBulkProcessor(bulkList2, 10000);
			}
			
			Map<String, Object> masterLastPodMap = new HashMap<String, Object>();
			masterLastPodMap.put("lastPodStatus", "ECM110");
			masterLastPodMap.put("gmtTime", sendManifest.getGmtTime());
			masterLastPodMap.put("updateType", "odJobNo");
			masterLastPodMap.put("odJobNo", resultData.get("masterOdJobNo"));
			commonMapper.updateLastPodStatus(masterLastPodMap);
			
			Map<String, Object> podMap = (Map)resultData.get("podMap");
			List<Map<String, Object>> insertOrderPodList = (List)resultData.get("insertOrderPodList");
			List<Map<String, Object>> updateOrderPodList = new ArrayList<Map<String, Object>>();
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
			cnt = 1;
			for(Map<String, Object> insertOrderPodMap : insertOrderPodList){
				if(insertOrderPodMap.get("ECM110_POD_JOB_NO") != null){
					updateOrderPodList.add(insertOrderPodMap);
				}else{
					podMap.putAll(insertOrderPodMap);
					orderBatchMapper.insertOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}
			cnt = 1;
			for(Map<String, Object> updateOrderPodMap : updateOrderPodList){
				updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("ECM110_POD_JOB_NO"));
				podMap.putAll(updateOrderPodMap);
				orderBatchMapper.updateOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			/** 통관목록전송시 오류통보POD삭제 */
			cnt = 1;
			for(Map<String, Object> deleteOrderPodMap : insertOrderPodList){
				podMap.putAll(deleteOrderPodMap);
				podMap.put("podStatus", "ECM115");
				orderBatchMapper.deleteOrderPod(podMap);
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
	public void setCustomsManifestExpTest(SendManifest sendManifest, OrderSaveResult orderSaveResult) throws Exception {
		Map<String, Object> resultData = orderSaveResult.getResultData();
		
//		SqlSession sqlSession = null;
//		int cnt = 1;
		try {
//			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
//			CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
//			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			SendManifest updateCustomsManifestData = (SendManifest)resultData.get("updateCustomsManifestData");
			String customsErrCode = "";
			
			updateCustomsManifestData.setMasterOdJobNo(null);
//			String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//			String index2 = "ecm-ups-tracking-upsxk";
//			ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
//			ArrayList<BulkData> bulkList2 = new ArrayList<BulkData>();	// UPS bulkList
//			BulkData bulkData = null;
			Map<String, Object> logMap = (Map)resultData.get("logMap");
			
			JSONObject jsonObject;
			JSONObject sendData;
			JSONArray jsonArray;
			List<String> houseOdJobNoList;
			List<Map<String, Object>> logList;
			List<List<String>> odJobNoArrayList;
			List<JSONArray> jsonArrayList;
			Map<String, Object> logData;
			int logStartIdx, logEndIdx, seq;
			Map<String, Object> restData = (Map)resultData.get("REST");
			String token = null;
			for(String ediPortalMailboxId : restData.keySet()){
				if(ediPortalMailboxId.indexOf("_ORIG_CODE") == -1 && ediPortalMailboxId.indexOf("_LOG_LIST") == -1){
					logStartIdx = 0;
					logEndIdx = 0;
					seq = 1;
					jsonObject = (JSONObject)restData.get(ediPortalMailboxId);
					logList = (List)restData.get(ediPortalMailboxId + "_LOG_LIST");
					jsonArrayList = (List<JSONArray>)jsonObject.get("dataList");
					odJobNoArrayList = (List<List<String>>)jsonObject.get("odJobNoList");
					for(int i = 0; i < jsonArrayList.size(); i++){
						jsonArray = jsonArrayList.get(i);
						houseOdJobNoList = odJobNoArrayList.get(i);
						sendData = new JSONObject();
						sendData.put("dataList", jsonArray);
						jsonObject = ediCommonService.sendEbmsTest(ediPortalMailboxId, "GOVCBR833", sendData, "LIST", token);
						if(token == null){
							token = Util.null2str(jsonObject.get("token"));
						}
						
						logMap.put("customs_err_desc", jsonObject.get("customsErrDesc"));
						logMap.put("customs_err_desc_additional", jsonObject.get("body"));
						customsErrCode = Util.null2str(jsonObject.get("customsErrCode"));
						
						logEndIdx = logStartIdx + jsonArray.size();
						for(int j = logStartIdx; j < logEndIdx; j++){
							logData = logList.get(j);
							logData.putAll(logMap);
//							bulkData = new BulkData();
//							bulkData.setActionType(BulkData.Type.CREATE);
//							bulkData.setIndexName(index);
//							bulkData.setId(Util.null2str(logData.get("es_job_no")));
//							bulkData.setJsonMap(logData);
//							bulkList.add(bulkData);
						}
						logStartIdx = logEndIdx;
						
						updateCustomsManifestData.setCustomsErrCode(customsErrCode);
						updateCustomsManifestData.setOrigCode(Util.null2str(restData.get(ediPortalMailboxId + "_ORIG_CODE")));
						updateCustomsManifestData.setHouseOdJobNoList(houseOdJobNoList);
						updateCustomsManifestData.setAction("REST");
//						manifestMapper.updateCustomsManifest(updateCustomsManifestData);
					}
				}
			}
			
//			if (bulkList != null && bulkList.size() > 0) {
//				IndexApi indexApi = new IndexApi(client);
//				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//				
//				DocumentApi documentApi = new DocumentApi(client);
//				documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
//			}
//			if (bulkList2 != null && bulkList2.size() > 0) {
//				IndexApi indexApi = new IndexApi(client);
//				if (!indexApi.existIndex(index2))	indexApi.createIndex(index2);
//				
//				DocumentApi documentApi = new DocumentApi(client);
//				documentApi.bulkDocumentWithBulkProcessor(bulkList2, 10000);
//			}
			
			Map<String, Object> masterLastPodMap = new HashMap<String, Object>();
			masterLastPodMap.put("lastPodStatus", "ECM110");
			masterLastPodMap.put("gmtTime", sendManifest.getGmtTime());
			masterLastPodMap.put("updateType", "odJobNo");
			masterLastPodMap.put("odJobNo", resultData.get("masterOdJobNo"));
//			commonMapper.updateLastPodStatus(masterLastPodMap);
			
			Map<String, Object> podMap = (Map)resultData.get("podMap");
			List<Map<String, Object>> insertOrderPodList = (List)resultData.get("insertOrderPodList");
			List<Map<String, Object>> updateOrderPodList = new ArrayList<Map<String, Object>>();
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
//			cnt = 1;
//			for(Map<String, Object> insertOrderPodMap : insertOrderPodList){
//				if(insertOrderPodMap.get("ECM110_POD_JOB_NO") != null){
//					updateOrderPodList.add(insertOrderPodMap);
//				}else{
//					podMap.putAll(insertOrderPodMap);
//					orderBatchMapper.insertOrderPod(podMap);
//					if( (cnt++ % 1000) == 0){
//						sqlSession.flushStatements();
//						sqlSession.clearCache();
//					}
//				}
//			}
			
//			cnt = 1;
//			for(Map<String, Object> updateOrderPodMap : updateOrderPodList){
//				updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("ECM110_POD_JOB_NO"));
//				podMap.putAll(updateOrderPodMap);
//				orderBatchMapper.updateOrderPod(podMap);
//				if( (cnt++ % 1000) == 0){
//					sqlSession.flushStatements();
//					sqlSession.clearCache();
//				}
//			}
			
//			/** 통관목록전송시 오류통보POD삭제 */
//			cnt = 1;
//			for(Map<String, Object> deleteOrderPodMap : insertOrderPodList){
//				podMap.putAll(deleteOrderPodMap);
//				podMap.put("podStatus", "ECM115");
//				orderBatchMapper.deleteOrderPod(podMap);
//				if( (cnt++ % 1000) == 0){
//					sqlSession.flushStatements();
//					sqlSession.clearCache();
//				}
//			}
			
//			sqlSession.flushStatements();
//			sqlSession.clearCache();
//			sqlSession.commit();
		}catch(Exception e) {
			throw e;
		}finally{
//			sqlSession.close();
		}
	}
	
	/**
	 * 수입목록신고 xml 생성
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public OrderSaveResult setCustomsManifestImpData(SendManifest sendManifest, String cusSendType) throws Exception {
		int seq = 1;
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendManifest.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		String message = null;
		String bound = Util.null2str(sendManifest.getBound());
		Map<String, Object> masterOrder = null;
		Util.accessControlDatabase(sendManifest, "action", false, null, null, null, null);
		if(Util.null2str(sendManifest.getMasterOdJobNo()).equals("")){	/** 하우스그리드선택 */
			sendManifest.setOdJobNo(sendManifest.getHouseOdJobNoList().get(0));	/** 하우스관리번호로 마스터 조회 */
			masterOrder = manifestMapper.selectCustomsManifestMaster(sendManifest);
			if(sendManifest.getHouseOdJobNoList().size() != Util.null2int(masterOrder.get("SIMPLE_DOC_NO_CNT"))){	/** 해당 제출번호의 데이터건수와 그리드선택건수가 같지않음 */
				result.put("processMessage", "제출번호가 같지만 선택되지않은 오더가 있습니다.");
				orderSaveResult.setResultData(result);
				orderSaveResult.setResultCode("ERROR");
				return orderSaveResult;
			}
		}else{
			/** 마스터그리드선택일경우 해당 마스터에 전송가능한 제출번호  */
			//서브그리드 선택한 경우 해당 마스터 + 선택한 제출번호 중 전송가능한 제출번호 가져옴
			masterOrder = manifestMapper.selectCustomsManifestMaster(sendManifest);
		}
		
		if(masterOrder != null){
			if(Util.null2str(masterOrder.get("SIMPLE_DOC_NO_LIST")).equals("")){
				tempSearchData.put("srchCode", "MF-43");	// 해당 M.B/L에 H.B/L이 한 건도 없습니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : " + message));
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("BOUND", masterOrder.get("BOUND"));
				ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
				orderSaveResult.setResultData(result);
				orderSaveResult.setResultCode("ERROR");
				return orderSaveResult;
			}
			String[] simpleDocStrList = Util.null2str(masterOrder.get("SIMPLE_DOC_NO_LIST")).split(",");	/** 해당마스터에 전송해야할 제출번호리스트 */
			List<List<Map<String, Object>>> simpleDocNoHouseList = new ArrayList<List<Map<String, Object>>>();
			List<Map<String, Object>> tmpHouseOrderList = null;
			List<Map<String, Object>> tmpHouseOrderListSimpleDocNo = null;
			String simpleDocNo = null;
			String tmpStr, tmpStr2;
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", sendManifest.getEntCode());
			podMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
			podMap.put("solution", "ECM");
			podMap.put("optionCode", "C04");
			String consigneeNameType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
			podMap.put("optionCode", "C05");
			String consigneeAddressType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
			podMap.put("optionCode", "C09");
			String updateSimpleDocDateYn = Util.null2str(commonMapper.selectSystemOption(podMap), "N");
			masterOrder.put("updateSimpleDocDateYn", updateSimpleDocDateYn);
			
			int startNum, countCheck;
			sendManifest.setHouseCount(100);
			for(String simpleDocStr : simpleDocStrList){
				sendManifest.setSimpleDocNo(simpleDocStr);
				
				/** 데이터건수체크(두개의 브라우저에서 한번에 같은건을 전송했을경우 manifestMapper.updateCustomsManifest에서 상태업데이트 되면서 100건씩 조회하는데이터가 더이상 조회되지않고 종료됨) */
				sendManifest.setStartNum(999999);
				countCheck = Util.null2int(manifestMapper.selectCustomsManifestHouseItem(sendManifest).get(0).get("COUNT_CHECK"));
				
				boolean chkData = true;
				startNum = 0;
				tmpHouseOrderListSimpleDocNo = new ArrayList<Map<String, Object>>();
				
				while(chkData){
					sendManifest.setStartNum(startNum);
					tmpHouseOrderList = manifestMapper.selectCustomsManifestHouseItem(sendManifest);
					if(startNum != 0 && (tmpHouseOrderList == null || tmpHouseOrderList.size() == 0)){
						chkData = false;
						break;
					}
					this.getCustomsManifestValidate(masterOrder, tmpHouseOrderList, tempSearchData, errList, result, consigneeNameType, consigneeAddressType);
					startNum += 100;
					if(tmpHouseOrderList.size() != 100) {
						chkData = false;
					}
					tmpHouseOrderListSimpleDocNo.addAll(tmpHouseOrderList);
				}
				
				if(countCheck == tmpHouseOrderListSimpleDocNo.size()){
					simpleDocNoHouseList.add(tmpHouseOrderListSimpleDocNo);
				}else{
					errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : 전송중 변경된 데이터가 있습니다. 확인 후 처리 바랍니다."));
				}
			}
			
			if(errList.size() > 0 || Util.null2str(sendManifest.getPenaltyYn()).equals("Y")){
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("BOUND", masterOrder.get("BOUND"));
				ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
				orderSaveResult.setResultData(result);
				orderSaveResult.setResultCode("ERROR");
				return orderSaveResult;
			}
			
			if("errorCheck".equals(sendManifest.getAction())){
				orderSaveResult.setResultCode("errorCheck");
				return orderSaveResult;
			}
			
			podMap.put("userLanguage", sendManifest.getUserLanguage());
			podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
			podMap.put("srchCode", "ECM110");
			podMap.put("entCode", sendManifest.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", sendManifest.getGmtTime());
			podMap.put("updateUserId", sendManifest.getUpdateUserId());
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			podMap.put("podStatus", "ECM110");
			podMap.put("podTableName", 	"TB_ECM_POD_02");
			
			
			Map<String, Object> tmpMap = null;
			List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> upsList = new ArrayList<Map<String, Object>>();
			Map<String, Object> logMap = new HashMap<String, Object>();
			logMap.put("ent_code", sendManifest.getEntCode());
			logMap.put("customer_code", "");
			logMap.put("solution", "ECM");
			logMap.put("bound", bound);
			logMap.put("bound_type", "IMP");
			logMap.put("hbl_no", "");
			logMap.put("if_service_code", "");
			logMap.put("if_vender", "GOV");
			logMap.put("if_send_recv_type", "S");
			logMap.put("if_send_recv_method", "");
			logMap.put("if_status_info", "");
			logMap.put("if_sender_id", "");
			logMap.put("if_receiver_id", "");
			logMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
			logMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
			logMap.put("register_user_id", sendManifest.getRegisterUserId());
			logMap.put("register_user_name", sendManifest.getRegisterUserName());
			logMap.put("register_date_time", System.currentTimeMillis());
			logMap.put("if_doc_code", "CUSTOMS_CLEARANCE");
			logMap.put("if_doc_name", "통관목록");
			
			Map<String, Object> tmpInsertData = null;
			List<Map<String, Object>> insertGovSendList = new ArrayList<Map<String, Object>>();
			Map<String, Object> insertData = new HashMap<String, Object>();
			insertData.put("entCode", sendManifest.getEntCode());
			insertData.put("typeCode", "GOVCBR936");
			insertData.put("registerUserId", sendManifest.getRegisterUserId());
			insertData.put("gmtTime", sendManifest.getGmtTime());
			insertData.put("sendStatus", "N");
			insertData.put("entBusinessCode", "");
			insertData.put("updateUserId", sendManifest.getRegisterUserId());
			insertData.put("solution", "ECM");
			insertData.put("stockType", "EDI");
			insertData.put("stockSign", "EDI");
			insertData.put("stockSeqLength", "8");
			insertData.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			insertData.put("stockRemark", "sendCustomsManifestImp(GOVCBR936)");
			insertData.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));
			
			List<SendManifest> updateCustomsManifestList = new ArrayList<SendManifest>();
			SendManifest tmpSendManifest = null;
			
			List<Map<String, Object>> insertOrderPodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpPodData = null;
			
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			JSONObject tmpJsonObject = null;
			
			String today = DateTime.getToday();
			int xmlByte = 0;
			int houseCnt = 0;
			for(List<Map<String, Object>> houseOrderList : simpleDocNoHouseList){
				simpleDocNo = Util.null2str(houseOrderList.get(0).get("SIMPLE_DOC_NO"));
				houseCnt += houseOrderList.size();
				
				tmpSendManifest = new SendManifest();
				tmpSendManifest.setPodStatus("ECM110");
				tmpSendManifest.setGmtTime(sendManifest.getGmtTime());
				tmpSendManifest.setSimpleDocDate(today);
				tmpSendManifest.setMasterOdJobNo(sendManifest.getMasterOdJobNo());
				tmpSendManifest.setHouseOdJobNoList(sendManifest.getHouseOdJobNoList());
				tmpSendManifest.setSimpleDocNo(simpleDocNo);
				updateCustomsManifestList.add(tmpSendManifest);
				
				
				
				List<Map<String, Object>> houseItemList = null;
				List<String> poInfoList = null;
						
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
				Element lv5 = null;
				Map<String, String> attribute = new HashMap<String, String>();
				
				document = documentBuilder.newDocument();
				document.setXmlVersion("1.0");
				document.setXmlStandalone(true);
				root = document.createElement("wco:Declaration");
				
				root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_936SchemaModule:1:0");
				root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_936SchemaModule:1:0");
				root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_936SchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfSPC_936SchemaModule_1.0_standard.xsd");
				document.appendChild(root);
				
				/** 수입통관목록전송 내용 변경시 배치의 수입통관목록전송소스도 수정해야함(CargoProgressCheck.sendCustomsManifestImp) */
				
				/** 제출세관/과 */
				this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(masterOrder.get("CUSTOMS_CODE")).trim() + Util.null2str(masterOrder.get("CUSTOMS_DPT_CODE")).trim());
				/** 제출번호 */
				this.createElement(document, root, lv1, "wco:ID", simpleDocNo);
				/** 제출일자(CCYYMMDD) */
				this.createElement(document, root, lv1, "wco:IssueDateTime", today.replace("-", ""));
				/** 통관목록 화물건수 */
				this.createElement(document, root, lv1, "wco:LoadingListQuantity", Util.null2str(houseOrderList.size()));
				/** 문서형태구분 */
				this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBR936");
				/** 적하목록 관리 번호 */
				lv1 = this.createElement(document, root, lv1, "wco:AdditionalDocument", null);
				this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("MRN")).trim().toUpperCase());
				
				/** 스팩내용으로 수정함 */
//				/** wco:AdditionalInformation(목록선별자) 시작 */
//				lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
//				/** 목록선별자/확인자/X-RAY판독자 */
//				if(Util.null2str(masterOrder.get("SELECTOR_USER_NAME")).trim().equals("1")){
//					this.createElement(document, lv1, lv2, "wco:Content", "목록선별자");
//				}else if(Util.null2str(masterOrder.get("SELECTOR_USER_NAME")).trim().equals("2")){
//					this.createElement(document, lv1, lv2, "wco:Content", "확인자");
//				}else{
//					this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("SELECTOR_USER_NAME")).trim());
//				}
//				/** 1:목록선별자, 2: 확인자, 3:판독자1, 4: 판독자2, 5:판독자3, 6:판독자4 */
//				this.createElement(document, lv1, lv2, "wco:StatementCode", "1");
//				/** wco:AdditionalInformation(목록선별자) 끝 */
//				
//				/** wco:AdditionalInformation(확인자) 시작 */
//				lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
//				/** 목록선별자/확인자/X-RAY판독자 */
//				this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("CONFIRMER_USER_NAME")).trim());
//				/** 1:목록선별자, 2: 확인자, 3:판독자1, 4: 판독자2, 5:판독자3, 6:판독자4 */
//				this.createElement(document, lv1, lv2, "wco:StatementCode", "2");
//				/** wco:AdditionalInformation(확인자) 끝 */
				String selectorUserName = "";
				String confirmerUserName = "";
				/** UPS는 목록선별자,확인자 하우스 기준으로 한다. 2025-11-07 **/
				if("UPSXK".equals(sendManifest.getEntCode()) || "YHTXX".equals(sendManifest.getEntCode())) {
					selectorUserName = Util.null2str(houseOrderList.get(0).get("SELECTOR_USER_NAME")).trim();
					confirmerUserName = Util.null2str(houseOrderList.get(0).get("CONFIRMER_USER_NAME")).trim();
				}else {
					selectorUserName = Util.null2str(masterOrder.get("SELECTOR_USER_NAME")).trim();
					confirmerUserName = Util.null2str(masterOrder.get("CONFIRMER_USER_NAME")).trim();
				}
				lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
				this.createElement(document, lv1, lv2, "wco:Content", selectorUserName);
				this.createElement(document, lv1, lv2, "wco:StatementCode", "1");
				lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
				this.createElement(document, lv1, lv2, "wco:Content", confirmerUserName);
				this.createElement(document, lv1, lv2, "wco:StatementCode", "2");
				
				if(!"".equals(Util.null2str(masterOrder.get("XRAY_READER1_NAME")))) {
					lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
					this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("XRAY_READER1_NAME")).trim());
					this.createElement(document, lv1, lv2, "wco:StatementCode", "3");
				}
				if(!"".equals(Util.null2str(masterOrder.get("XRAY_READER2_NAME")))) {
					lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
					this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("XRAY_READER2_NAME")).trim());
					this.createElement(document, lv1, lv2, "wco:StatementCode", "4");
				}	
				
				
				//2021-09-02 해당값 전송하지않음
//				if((bound.equals("AIR")/* && zCompanyInfo.htCompanyInfo["XRayYN"].ToString() == "Y"*/) || (bound.equals("SEA"))/* && zCompanyInfo.htCompanyInfo["XRayYN_Ocean"].ToString() == "Y"*/){
//					if(!Util.null2str(masterOrder.get("TEDAI_MBL.XRayAttnA")).trim().equals("")){
//						/** wco:AdditionalInformation(판독자1) 시작 */
//						lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
//						/** 목록선별자/확인자/X-RAY판독자 */
//						this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("TEDAI_MBL.XRayAttnA")).trim());
//						/** 1:목록선별자, 2: 확인자, 3:판독자1, 4: 판독자2, 5:판독자3, 6:판독자4 */
//						this.createElement(document, lv1, lv2, "wco:StatementCode", "3");
//						/** wco:AdditionalInformation(판독자1) 끝 */
//					}
//					
//					if(!Util.null2str(masterOrder.get("TEDAI_MBL.XRayAttnB")).trim().equals("")){
//						/** wco:AdditionalInformation(판독자2) 시작 */
//						lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
//						/** 목록선별자/확인자/X-RAY판독자 */
//						this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("TEDAI_MBL.XRayAttnB")).trim());
//						/** 1:목록선별자, 2: 확인자, 3:판독자1, 4: 판독자2, 5:판독자3, 6:판독자4 */
//						this.createElement(document, lv1, lv2, "wco:StatementCode", "4");
//						/** wco:AdditionalInformation(판독자2) 끝 */
//					}
//					
//					if(!Util.null2str(masterOrder.get("TEDAI_MBL.XRayAttnC")).trim().equals("")){
//						/** wco:AdditionalInformation(판독자3) 시작 */
//						lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
//						/** 목록선별자/확인자/X-RAY판독자 */
//						this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("TEDAI_MBL.XRayAttnC")).trim());
//						/** 1:목록선별자, 2: 확인자, 3:판독자1, 4: 판독자2, 5:판독자3, 6:판독자4 */
//						this.createElement(document, lv1, lv2, "wco:StatementCode", "5");
//						/** wco:AdditionalInformation(판독자3) 끝 */
//					}
//					
//					if(!Util.null2str(masterOrder.get("TEDAI_MBL.XRayAttnD")).trim().equals("")){
//						/** wco:AdditionalInformation(판독자4) 시작 */
//						lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
//						/** 목록선별자/확인자/X-RAY판독자 */
//						this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("TEDAI_MBL.XRayAttnD")).trim());
//						/** 1:목록선별자, 2: 확인자, 3:판독자1, 4: 판독자2, 5:판독자3, 6:판독자4 */
//						this.createElement(document, lv1, lv2, "wco:StatementCode", "6");
//						/** wco:AdditionalInformation(판독자4) 끝 */
//					}
//				}
				
				/** wco:BorderTransportMeans 시작 */
				lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
				/** 입항일자(CCYYMMDD) */
				this.createElement(document, lv1, lv2, "wco:ArrivalDateTime", Util.null2str(masterOrder.get("AR_DATE")).trim().replace("-", ""));
				/** 항공편명/항차 */
				if(bound.equals("AIR")){
//						this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("LINE_CODE")).trim() + Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim());
					this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim());	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
					this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "40");
				}else{
					this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("VOYAGE")).trim());
					this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "10");
				}
				/** wco:BorderTransportMeans 끝 */
				
				String consigneeAddress = null;
//				String strpCustomsNumber_A = null;
				String customsIdType = null;
				int itemSeq = 0;
				int poInfoSeq = 0;
				
				String assignmentWhCodeHbl = Util.null2str(houseOrderList.get(0).get("ASSIGNMENT_WH_CODE"));
				String assignmentWhCodeEtcHbl = Util.null2str(houseOrderList.get(0).get("ASSIGNMENT_WH_CODE_ETC"));
				
				for(Map<String, Object> houseOrder : houseOrderList){
				
					if(consigneeAddressType.equals("ENG")){
						if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")).trim())){
							consigneeAddress = ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 0, 150);
						}else{
							consigneeAddress = ediCommonService.getSubStringK(ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS_LOC")), false), 0, 150);
						}
					}else{
						if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS_LOC")).trim())){
							consigneeAddress = ediCommonService.getSubStringK(ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS_LOC")), false), 0, 150);
						}else{
							consigneeAddress = ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 0, 150);
						}
					}
						
					/** wco:Consignment 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
					/** 검사요청대상 */
					this.createElement(document, lv1, lv2, "wco:GoodsStatusCode", Util.null2str(houseOrder.get("INSPECTION_YN"), "N"));
					/** 총중량 */
					this.createElement(document, lv1, lv2, "wco:GrossVolumeMeasure", Util.null2str(houseOrder.get("GWT")));
					/** 포장개수 */
					this.createElement(document, lv1, lv2, "wco:TotalPackageQuantity", Util.null2str(houseOrder.get("PIECES")));
					/** 물품가격(미화) */
					attribute.put("attributeName", "currencyID");
					attribute.put("attributeContent", "USD");
					this.createElement(document, lv1, lv2, "wco:ValueAmount",String.format("%.2f",Util.null2dbl(houseOrder.get("AMOUNT_USD"))), attribute);
					
					/** 수하인 식별부호 */
					/** 2024.09.11 - Z(데이터 없음)일 때는 항목 전송하지 않음 */
					customsIdType = Util.null2str(houseOrder.get("CUSTOMS_ID_TYPE"));
					if(!Util.isEmpty2(customsIdType) && !"Z".equals(customsIdType)){
						this.createElement(document, lv1, lv2, "kcs:TypeID", customsIdType);
					}
//					strpCustomsNumber_A = dsSub.Tables[0].Rows[nRowIndex]["pCustomsNumber"].ToString().Trim().ToUpper().Replace("-", "").Replace("/", "").Replace(".", "").Replace(" ", "");
//					if(Util.null2str(houseOrder.get("TEDAI_HBL.CustomsUseType")).equals("2")){
//						if(strpCustomsNumber_A.length() == 10){
//							this.createElement(document, lv1, lv2, "kcs:TypeID", "D");	/** 사업자등록번호 */
//						}else if (strpCustomsNumber_A.length() > 10 && ediCommonService.getDeleteCharStringManifest(strpCustomsNumber_A) != strpCustomsNumber_A){	//EtcLibrary.DeleteAlphabetChar(strpCustomsNumber_A) != strpCustomsNumber_A
//							this.createElement(document, lv1, lv2, "kcs:TypeID", "C");	/** 통관고유부호 */
//						}else{
//							this.createElement(document, lv1, lv2, "kcs:TypeID", "");
//						}
//					}else{
//						if(ediCommonService.getDeleteCharStringManifest(strpCustomsNumber_A) == strpCustomsNumber_A && strpCustomsNumber_A.length() == 8 && (ediCommonService.getSubStringK(strpCustomsNumber_A, 0, 2) == "19" || ediCommonService.getSubStringK(strpCustomsNumber_A, 0, 2) == "20")){	//EtcLibrary.DeleteAlphabetChar(strpCustomsNumber_A) == strpCustomsNumber_A && strpCustomsNumber_A.Length == 8 && (EtcLibrary.getSubStringK(strpCustomsNumber_A,0,2) == "19" || EtcLibrary.getSubStringK(strpCustomsNumber_A,0,2) == "20")
//							this.createElement(document, lv1, lv2, "kcs:TypeID", "B");	/** 생년월일 */
//						}else{
//							if (ediCommonService.getDeleteCharStringManifest(strpCustomsNumber_A) == strpCustomsNumber_A && strpCustomsNumber_A.length() == 13){	//EtcLibrary.DeleteAlphabetChar(strpCustomsNumber_A) == strpCustomsNumber_A && strpCustomsNumber_A.Length == 13
//								this.createElement(document, lv1, lv2, "kcs:TypeID", "E");	/** 외국인등록번호 */
//							}else{
//								if (ediCommonService.getSubStringK(strpCustomsNumber_A, 0, 1).toUpperCase() == "P" && ediCommonService.getDeleteCharStringManifest(strpCustomsNumber_A).length() == 12){	//EtcLibrary.getSubStringK(strpCustomsNumber_A, 0, 1).ToUpper() == "P" && EtcLibrary.DeleteAlphabetChar(strpCustomsNumber_A).Length == 12
//									this.createElement(document, lv1, lv2, "kcs:TypeID", "A");	/** 개인통관고유부호 */
//								}else{
//									this.createElement(document, lv1, lv2, "kcs:TypeID", "F");	/** 여권번호 */
//								}
//							}
//						}
//					}
					
					/** 상세물품 수 */
					this.createElement(document, lv1, lv2, "kcs:DetailCountQuantity", Util.null2str(houseOrder.get("ITEM_CNT")));
					/** 내포장개수 */
					this.createElement(document, lv1, lv2, "kcs:InternalPackageQuantity", Util.null2str(houseOrder.get("PIECES_REAL")));
					
					/** 특별통관대상업체지정번호 */
//					lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalDocument", null);
//					this.createElement(document, lv2, lv3, "wco:ID", "");	//공백처리
					
					/** wco:AdditionalInformation 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalInformation", null);
					/** House Air Way Bill 품명 */
					this.createElement(document, lv2, lv3, "wco:Content", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("COMMODITY_ITEM")).trim(), "1"), 0, 190));
					/** 용도구분(1:개인,2:회사) */
					this.createElement(document, lv2, lv3, "wco:StatementCode", Util.null2str(houseOrder.get("CD_USE_TYPE")));
					/** 허용품목코드(1 : 화장지, 주방용기류 (48류), 2 : 서적, 인쇄물류 (49류), 3 : 의류 (61~63류), 4 : 신발류 (64류), 5 : 가구/조명기구류 (94류), 6 : 음악/영화용 CD (85류), 7 : 허용 비대상 품목) */
					this.createElement(document, lv2, lv3, "wco:StatementTypeCode", Util.null2str(houseOrder.get("ALLOWED_ITEM_CODE")).trim());
					/** wco:AdditionalInformation 끝 */
					
					/** wco:Agent 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:Agent", null);
					/** 화물운송주선업자부호 */
					tmpStr = Util.null2str(masterOrder.get("B2G_FORWARDER_SIGN")).trim().toUpperCase();
					if(!Util.isEmpty2(tmpStr)){
						this.createElement(document, lv2, lv3, "wco:ID", tmpStr);
					}
					/** 거래유형코드(A:전자상거래물품, D:개인 일반물품, E:상용견품, F:상업서류) */
					this.createElement(document, lv2, lv3, "kcs:TypeOfBusiness", Util.null2str(houseOrder.get("ECM_TYPE")));
					/** wco:Agent 끝 */
					
//					if(Util.null2str(masterOrder.get("B2G_FORWARDER_SIGN")).equals("")){
//						/** 상업서류송달업체부호 */
//						lv2 = this.createElement(document, lv1, lv2, "wco:Carrier", null);
//						this.createElement(document, lv2, lv3, "wco:ID", "");	//2021-09-02 해당값 전송하지않음
//					}
					
					/** wco:Consignee 시작 */
					tmpStr = Util.null2str(houseOrder.get("SELLER_REG_NO"));
					tmpStr2 = Util.null2str(houseOrder.get("SELLER_NAME"));
					if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)){
						lv2 = this.createElement(document, lv1, lv2, "wco:Consignee", null);
						/** 해외판매자부호 */
						if(!Util.isEmpty2(tmpStr)){
							this.createElement(document, lv2, lv3, "wco:ID", tmpStr);
						}
						/** 해외판매자명 */
						if(!Util.isEmpty2(tmpStr2)){
							this.createElement(document, lv2, lv3, "wco:Name", tmpStr2);
						}
					}
					/** wco:Consignee 끝 */
					
					/** wco:ConsignmentItem 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:ConsignmentItem", null);
					
					/** wco:AdditionalInformation 시작 */
					tmpStr = ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("URL")).trim(), "2");
					tmpStr2 = Util.null2str(houseOrder.get("ECM_BUY_TYPE"));
					if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)){
						lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
						/** 전자상거래사이트주소 */
						if(!Util.isEmpty2(tmpStr)){
							this.createElement(document, lv3, lv4, "wco:Content", ediCommonService.getSubStringK(tmpStr, 0, 300));
						}
						/** 전자상거래유형코드 */
						if(!Util.isEmpty2(tmpStr2)){
							this.createElement(document, lv3, lv4, "wco:StatementTypeCode", tmpStr2);
						}
					}
					/** wco:AdditionalInformation 끝 */
					
					/** wco:Agent 시작 */
					tmpStr = Util.null2str(houseOrder.get("AGENT_REG_NO"));
					tmpStr2 = Util.null2str(houseOrder.get("AGENT_NAME"));
					if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)){
						lv3 = this.createElement(document, lv2, lv3, "wco:Agent", null);
						/** 구매대행업자부호 */
						if(!Util.isEmpty2(tmpStr)){
							this.createElement(document, lv3, lv4, "wco:ID", tmpStr);
						}
						/** 구매대행업자명 */
						if(!Util.isEmpty2(tmpStr2)){
							this.createElement(document, lv3, lv4, "wco:Name", tmpStr2);
						}
					}
					/** wco:Agent 끝 */
					
					/** wco:Buyer 시작 */
					tmpStr = Util.null2str(houseOrder.get("SALES_BROKER_REG_NO"));
					tmpStr2 = Util.null2str(houseOrder.get("SALES_BROKER_NAME"));
					if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)){
						lv3 = this.createElement(document, lv2, lv3, "wco:Buyer", null);
						/** 판매중개자부호 */
						if(!Util.isEmpty2(tmpStr)){
							this.createElement(document, lv3, lv4, "wco:ID", tmpStr);
						}
						/** 판매중개자명 */
						if(!Util.isEmpty2(tmpStr2)){
							this.createElement(document, lv3, lv4, "wco:Name", tmpStr2);
						}
					}
					/** wco:Buyer 끝 */
					
					
					
					sendManifest.setOdJobNo(Util.null2str(houseOrder.get("OD_JOB_NO")));
					houseItemList = (List) houseOrder.get("houseItemList");
					attribute.put("attributeName", "kcsUnitCode");
					if(houseItemList == null || houseItemList.size() == 0){
						/** wco:Commodity 시작 */
						lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
						/** 일련번호 */
						this.createElement(document, lv3, lv4, "wco:SequenceNumeric", "1");
						/** 품명 */
						this.createElement(document, lv3, lv4, "wco:CargoDescription", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("COMMODITY_ITEM")), "1"));
						/** 수량(단위) */
						attribute.put("attributeContent", "EA");
						this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(houseOrder.get("PIECES")), attribute);
						/** 규격 */
//						this.createElement(document, lv3, lv4, "wco:Description", "");
						/** 금액 */
						if(Util.null2dbl(houseOrder.get("AMOUNT_USD")) > 0){
							this.createElement(document, lv3, lv4, "wco:ValueAmount",  String.format("%.2f",Util.null2dbl(houseOrder.get("AMOUNT_USD"))));
						}
						/** wco:Commodity 끝 */
					}else{
						itemSeq = 1;
						for(Map<String, Object> houseItem : houseItemList){
							/** wco:Commodity 시작 */
							lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
							/** 일련번호 */
							this.createElement(document, lv3, lv4, "wco:SequenceNumeric", Util.null2str(itemSeq++));	//Convert.ToInt16(i + 1)
							/** 품명 */
							this.createElement(document, lv3, lv4, "wco:CargoDescription", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseItem.get("DETAIL_ITEM_NAME")), "1"));
							/** 수량(단위) */
							attribute.put("attributeContent", Util.null2str(houseItem.get("UNIT_PIECES"), "EA"));
							this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(houseItem.get("PIECES")), attribute);
							/** 규격 */
							this.createElement(document, lv3, lv4, "wco:Description", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseItem.get("BRAND")), "1"));
							/** 금액 */
							this.createElement(document, lv3, lv4, "wco:ValueAmount", String.format("%.2f",Util.null2dbl(houseItem.get("AMOUNT"))));
						}
					}
					
					/** wco:Consignee 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:Consignee", null);
					/** 수하인 성명 */
					if(consigneeNameType.equals("ENG")){
						if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim())){
							this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim(), "1"));
						}else{
							this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_NAME_LOC")).trim(), false));
						}
					}else{
						if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_NAME_LOC")).trim())){
							this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_NAME_LOC")).trim(), false));
						}else{
							this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim(), "1"));
						}
					}
					/** 수하인 고유부호 */
					/** 2024.09.11 - 수하인 식별부호가 Z(데이터 없음)일 때는 항목 전송하지 않음 */
					tmpStr = Util.null2str(houseOrder.get("CUSTOMS_ID")).trim().toUpperCase().replace("-", "").replace("/", "").replace(".", "").replace(" ", "");
					if(!Util.isEmpty2(tmpStr) && !"Z".equals(Util.isEmpty2(customsIdType))){
						this.createElement(document, lv3, lv4, "wco:ID", tmpStr);
					}
					
//					if(Util.null2str(houseOrder.get("TEDAI_HBL.CustomsUseType")).equals("2")){
//						if(strpCustomsNumber_A.length() == 10 || (strpCustomsNumber_A.length() > 10 && ediCommonService.getDeleteCharStringManifest(strpCustomsNumber_A) != strpCustomsNumber_A)){	//EtcLibrary.DeleteAlphabetChar(strpCustomsNumber_A) != strpCustomsNumber_A
//							this.createElement(document, lv3, lv4, "wco:ID", strpCustomsNumber_A.toUpperCase());
//						}else{
//							this.createElement(document, lv3, lv4, "wco:ID", "");
//						}
//					}else{
//						this.createElement(document, lv3, lv4, "wco:ID", strpCustomsNumber_A.toUpperCase());
//					}
					
					/** wco:Address 시작 */
					lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
					/** 수하인 주소 */
					this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(consigneeAddress, 0, 150));
					/** 수하인 우편번호 */
					/** 우편번호값 없을경우 00000으로 전송되지 않도록 수정    0으로 시작하는 우편번호에 대한 처리 무시 */
//					this.createElement(document, lv4, lv5, "wco:PostcodeID", ediCommonService.getSubStringK(Util.getFileWriteData(Util.null2str(houseOrder.get("CONSIGNEE_ZIPCODE")).replace(" ", "").replace("-", ""), "Number", "5"), 0, 6));	//0으로 시작하는 우편번호일 때 4자리만 저장된 경우 있어서 수정, 맨 앞자리 0추가 ex.5750 -> 05750
					tmpStr = Util.null2str(houseOrder.get("CONSIGNEE_ZIPCODE")).replace(" ", "").replace("-", "");
					if(!Util.isEmpty2(tmpStr)){
						this.createElement(document, lv4, lv5, "wco:PostcodeID", ediCommonService.getSubStringK(tmpStr, 0, 6));
					}
					/** wco:Address 끝 */
					
					/** 수하인 전화번호 */
					lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
					this.createElement(document, lv4, lv5, "wco:ID", ediCommonService.EDI_Numeric_Select(Util.null2str(houseOrder.get("CONSIGNEE_TEL"))));
					/** wco:Consignee 끝 */
					
					/** wco:Consignor 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:Consignor", null);
					/** 송하인 성명 */
					this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_NAME")), "1"));
					
					/** wco:Address 시작 */
					lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
					/** 송하인 주소 */
					tmpStr = ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")), "1");
					if(!Util.isEmpty2(tmpStr)){
						this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(tmpStr, 0, 150));
					}
					/** 발송국가 */
                    /** 2025-12-24 UPS일때는 ORIG_COUNTRY 사용 **/
                    String origCountry = "";
                    if("UPSXK".equals(Util.null2str(sendManifest.getEntCode()))){
                        origCountry = Util.null2str(houseOrder.get("ORIG_COUNTRY"));
                    }else {
                        origCountry = Util.null2str(houseOrder.get("SHIPPER_COUNTRY_CODE"));
                    }
					this.createElement(document, lv4, lv5, "wco:CountryCode", origCountry.trim());
					/** wco:Address 끝 */
					
					/** 송하인 전화번호 */
					tmpStr = ediCommonService.EDI_Numeric_Select(Util.null2str(houseOrder.get("SHIPPER_TEL")));
					if(!Util.isEmpty2(tmpStr)){
						lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
						this.createElement(document, lv4, lv5, "wco:ID", tmpStr);
					}
					/** wco:Consignor 끝 */
					/** wco:ConsignmentItem 끝 */
					
					poInfoList = (List) houseOrder.get("poInfoList");
					if(poInfoList != null && poInfoList.size() > 0){
						poInfoSeq = 1;
						for(String poNo : poInfoList){
							/** wco:Commodity 시작 */
							lv2 = this.createElement(document, lv1, lv2, "wco:Commodity", null);
							/** 일련번호 */
							this.createElement(document, lv2, lv3, "kcs:SequenceNumeric", Util.null2str(poInfoSeq++));
							/** 주문번호 */
							this.createElement(document, lv2, lv3, "wco:ID", poNo);
							/** wco:Commodity 끝 */
						}
					}
					
					/** wco:TransportContractDocument 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
					/** House B/L 번호 */
					this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.DeleteCharString_Loop(Util.null2str(houseOrder.get("HBL_NO")).trim().toUpperCase(), " |-", "|"));
					/** House B/L 일련번호 */
					this.createElement(document, lv2, lv3, "kcs:HouseBLSequenceID", Util.null2str(houseOrder.get("HSN")).trim());
					/** wco:TransportContractDocument 끝 */
					/** wco:Consignment 끝 */
					
					tmpPodData = new HashMap<String, Object>();
					tmpPodData.put("odJobNo", houseOrder.get("OD_JOB_NO"));
					tmpPodData.put("blNo", houseOrder.get("HBL_NO"));
					if(houseOrder.get("ECM111_POD_JOB_NO") != null) {
						tmpPodData.put("ECM111_POD_JOB_NO", houseOrder.get("ECM111_POD_JOB_NO"));
					}
					if(houseOrder.get("ECM110_POD_JOB_NO") != null){
						tmpPodData.put("ECM110_POD_JOB_NO", houseOrder.get("ECM110_POD_JOB_NO"));
					}else{
						tmpPodData.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					}
					insertOrderPodList.add(tmpPodData);
				}
				
				/** wco:GoodsShipment 시작 */
				lv1 = this.createElement(document, root, lv1, "wco:GoodsShipment", null);
				/** 보세구역부호 */
				lv2 = this.createElement(document, lv1, lv2, "wco:Warehouse", null);
//				if("UPSXK".equals(sendManifest.getEntCode())) {
//					this.createElement(document, lv2, lv3, "wco:ID", "UX");	/** UPS일경우 UX고정 */
//				}else {
					/** 허브넷은 해상일때 하우스 값으로 창고 코드 전송 **/
					if("HUBNK".equals(sendManifest.getEntCode()) && "SEA".equals(bound)) {
						if(Util.null2str(assignmentWhCodeHbl).trim().length() == 8){
							this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(assignmentWhCodeHbl).trim());
						}else{
							this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.getSubStringK(Util.null2str(assignmentWhCodeEtcHbl).trim(), 0, 8));
						}
					}else {
						if(Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE")).trim().length() == 8){
							this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE")).trim());
						}else{
							this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.getSubStringK(Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE_ETC")).trim(), 0, 8));
						}
					}
//				}
				/** wco:GoodsShipment 끝 */
				
				/** 적출국(항) */
				lv1 = this.createElement(document, root, lv1, "wco:LoadingLocation", null);
				this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("ORIG_CODE")).trim());
				
				/** wco:TransportContractDocument 시작 */
				lv1 = this.createElement(document, root, lv1, "wco:TransportContractDocument", null);
				/** Master B/L번호 */
				if(bound.equals("AIR")){
					this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.getSubStringK(Util.null2str(masterOrder.get("MBL_NO")).trim(), 0, 11));
				}else{
					this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.GetBLNo_ZSeqDelete(Util.null2str(masterOrder.get("MBL_NO")).trim().toUpperCase()));
				}
				/** Master B/L 일련번호 */
				this.createElement(document, lv1, lv2, "kcs:MasterBLSequenceID", Util.null2str(masterOrder.get("MSN")));
				/** wco:TransportContractDocument 끝 */

				out = new ByteArrayOutputStream();
				source = new DOMSource(document);
				streamResult = new StreamResult(out);
				transformer.transform(source, streamResult);
				
				if("REST".equals(cusSendType)){
					tmpJsonObject = new JSONObject();
					String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
					String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
					String filePath = customProperties.getBackupDir() + "edi/customs/" + sendManifest.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
					String fileName = dataId + "GOVCBR936.xml";
					FileUtil.fileWrite(xml, filePath, fileName);
					
					tmpJsonObject.put("customsReportNo", simpleDocNo);
					tmpJsonObject.put("xml", xml);
					tmpJsonObject.put("filePath", filePath);
					tmpJsonObject.put("fileName", fileName);
					jsonArray.add(tmpJsonObject);
					
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("if_file_path", filePath);
					tmpMap.put("if_file_name", fileName);
//					tmpMap.put("if_doc_reference_no", simpleDocNo);
					tmpMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
					tmpMap.put("es_job_no", dataId);
					logList.add(tmpMap);
					
					/** UPS Tracking Send */
					if(("YHTXX".equals(sendManifest.getEntCode()) || "UPSXK".equals(sendManifest.getEntCode())) && "AIR".equals(sendManifest.getBound())){
						for(Map<String, Object> houseOrder : houseOrderList){
							ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
							BulkData bulkData = null;
							tmpMap = new HashMap<String, Object>();
							tmpMap.put("MBLNo", masterOrder.get("MBL_NO"));
							tmpMap.put("SubmissionNo", houseOrder.get("SIMPLE_DOC_NO"));
							tmpMap.put("OrdId", houseOrder.get("PO_NO"));
							tmpMap.put("HBLNo", houseOrder.get("HBL_NO"));
							tmpMap.put("HSN", houseOrder.get("HSN"));
							tmpMap.put("CustomsClearance", Util.null2str(houseOrder.get("DROP_YN")).equals("Y") ? "W" : "Y");
							tmpMap.put("BoundType", "IMP");
							tmpMap.put("DEMTrackingCode", "SNT");
							upsList.add(tmpMap);
						}
					}
					
					xmlByte += xml.getBytes().length;
				}else{
					tmpInsertData = new HashMap<String, Object>();
					tmpInsertData.put("ediDesc", out.toByteArray());
					tmpInsertData.put("declarationId", simpleDocNo);
					insertGovSendList.add(tmpInsertData);
					
				}
				out.close();
			}
			
			tmpPodData = new HashMap<String, Object>();
			tmpPodData.put("odJobNo", masterOrder.get("OD_JOB_NO"));
			tmpPodData.put("blNo", masterOrder.get("MBL_NO"));
			if(masterOrder.get("ECM111_POD_JOB_NO") != null){
				tmpPodData.put("ECM111_POD_JOB_NO", masterOrder.get("ECM111_POD_JOB_NO"));
			}
			if(masterOrder.get("ECM110_POD_JOB_NO") != null){
				tmpPodData.put("ECM110_POD_JOB_NO", masterOrder.get("ECM110_POD_JOB_NO"));
			}else{
				tmpPodData.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			}
			insertOrderPodList.add(tmpPodData);
			
			result.put("masterOdJobNo", masterOrder.get("OD_JOB_NO"));
			result.put("masterOrder", masterOrder);
			
			result.put("podMap", podMap);
			result.put("insertOrderPodList", insertOrderPodList);
			result.put("updateCustomsManifestList", updateCustomsManifestList);
			
			if("REST".equals(cusSendType)){
				jsonObject.put("dataList", jsonArray);
				result.put("REST", jsonObject);
				result.put("logList", logList);
				if(("YHTXX".equals(sendManifest.getEntCode()) || "UPSXK".equals(sendManifest.getEntCode())) && "AIR".equals(sendManifest.getBound())) {
					result.put("upsList", upsList);
				}
				result.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));
				if(xmlByte > 1024 * 1024 * 19){
					result.put("sendType", "FILE");
				}
				
				/** 건수가 많을경우 전송시간이 오래 걸려서 전송전에 상태변경/로그저장함  */
				for(SendManifest updateSendManifest : updateCustomsManifestList){
					manifestMapper.updateCustomsManifest(updateSendManifest);
					manifestMapper.updateSimpleErrorDescNull(updateSendManifest);
				}
				
				IndexApi indexApi = new IndexApi(client);
				DocumentApi documentApi = null;
				String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				documentApi = new DocumentApi(client);
				
				logMap.put("if_status", "READY");
				logMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
				logMap.put("if_remark", "M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "(" + houseCnt + "건)");
				logMap.put("es_job_no", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "99999");
				
				documentApi.createDocument(index, Util.null2str(logMap.get("es_job_no")), logMap);
				logMap.put("if_status", "SEND");
				logMap.put("if_remark", "");
				logMap.remove("es_job_no");
				
				result.put("logMap", logMap);
			}else{
				result.put("DB", insertGovSendList);
				result.put("insertGovSendMap", insertData);
			}
		}else{
			orderSaveResult.setResultCode("ERROR");
			result.put("processCode", "0000");
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "Master Order 자료가 존재하지 않습니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			result.put("processMessage", message);
		}
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	
	/**
	 * 수입목록신고 검증용
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public OrderSaveResult setCustomsManifestImpDataVali(SendManifest sendManifest, String cusSendType) throws Exception {
		int seq = 1;
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendManifest.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		String message = null;
		List<String> masterOdJobNoList = new ArrayList<>();
		if("Y".equals(sendManifest.getSubGridYn())){
			masterOdJobNoList.add(sendManifest.getMasterOdJobNo());
		}else {
			masterOdJobNoList = sendManifest.getMasterOdJobNoList();
		}
		for(int i =0; i<masterOdJobNoList.size(); i++) {
			if("valiMaster".equals(sendManifest.getAction())) {
				String[] splitData = masterOdJobNoList.get(i).split(":");
				sendManifest.setMasterOdJobNo(splitData[0]);
				sendManifest.setSimpleDocNo(splitData[1]);
			}else {
				sendManifest.setMasterOdJobNo(masterOdJobNoList.get(i));
			}
			
			Map<String, Object> masterOrder = null;
			
			/** 마스터그리드선택일경우 해당 마스터에 전송가능한 제출번호  */
			//서브그리드 선택한 경우 해당 마스터 + 선택한 제출번호 중 전송가능한 제출번호 가져옴
			masterOrder = manifestMapper.selectCustomsManifestMaster(sendManifest);
			
			if(masterOrder != null){
				if(Util.null2str(masterOrder.get("SIMPLE_DOC_NO_LIST")).equals("")){
					tempSearchData.put("srchCode", "MF-43");	// 해당 M.B/L에 H.B/L이 한 건도 없습니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : " + message));
					Map<String, Object> tmpMap = new HashMap<String, Object>();
					tmpMap.put("BOUND", masterOrder.get("BOUND"));
					ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
					continue;
				}
				String[] simpleDocStrList = Util.null2str(masterOrder.get("SIMPLE_DOC_NO_LIST")).split(",");	/** 해당마스터에 전송해야할 제출번호리스트 */
				List<List<Map<String, Object>>> simpleDocNoHouseList = new ArrayList<List<Map<String, Object>>>();
				List<Map<String, Object>> tmpHouseOrderList = null;
				List<Map<String, Object>> tmpHouseOrderListSimpleDocNo = null;
				
				Map<String, Object> podMap = new HashMap<String, Object>();
				podMap.put("entCode", sendManifest.getEntCode());
				podMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
				podMap.put("solution", "ECM");
				podMap.put("optionCode", "C04");
				String consigneeNameType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
				podMap.put("optionCode", "C05");
				String consigneeAddressType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
				podMap.put("optionCode", "C09");
				String updateSimpleDocDateYn = Util.null2str(commonMapper.selectSystemOption(podMap), "N");
				masterOrder.put("updateSimpleDocDateYn", updateSimpleDocDateYn);
				
				int startNum, countCheck;
				sendManifest.setHouseCount(100);
				for(String simpleDocStr : simpleDocStrList){
					log.info("6666666");
					sendManifest.setSimpleDocNo(simpleDocStr);
					
					/** 데이터건수체크(두개의 브라우저에서 한번에 같은건을 전송했을경우 manifestMapper.updateCustomsManifest에서 상태업데이트 되면서 100건씩 조회하는데이터가 더이상 조회되지않고 종료됨) */
					sendManifest.setStartNum(999999);
					countCheck = Util.null2int(manifestMapper.selectCustomsManifestHouseItem(sendManifest).get(0).get("COUNT_CHECK"));
					
					boolean chkData = true;
					startNum = 0;
					tmpHouseOrderListSimpleDocNo = new ArrayList<Map<String, Object>>();
					
					while(chkData){
						sendManifest.setStartNum(startNum);
						tmpHouseOrderList = manifestMapper.selectCustomsManifestHouseItem(sendManifest);
						if(startNum != 0 && (tmpHouseOrderList == null || tmpHouseOrderList.size() == 0)){
							chkData = false;
							break;
						}
						this.getCustomsManifestValidate(masterOrder, tmpHouseOrderList, tempSearchData, errList, result, consigneeNameType, consigneeAddressType);
						startNum += 100;
						if(tmpHouseOrderList.size() != 100) {
							chkData = false;
						}
						tmpHouseOrderListSimpleDocNo.addAll(tmpHouseOrderList);
					}
					
					if(countCheck == tmpHouseOrderListSimpleDocNo.size()){
						simpleDocNoHouseList.add(tmpHouseOrderListSimpleDocNo);
					}else{
						errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : 전송중 변경된 데이터가 있습니다. 확인 후 처리 바랍니다."));
					}
				}
			}
			if(errList.size() > 0){
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("BOUND", masterOrder.get("BOUND"));
				ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
				log.info(result.toString());
				orderSaveResult.setResultData(result);
				orderSaveResult.setResultCode("ERROR");
			}else {
				orderSaveResult.setResultCode("Success");
			}
		}
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	
	public OrderSaveResult setCustomsManifestImpDataTest(SendManifest sendManifest) throws Exception {
		int seq = 1;
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendManifest.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		String message = null;
		String bound = Util.null2str(sendManifest.getBound());
		Map<String, Object> masterOrder = null;
		Util.accessControlDatabase(sendManifest, "action", false, null, null, null, null);
		if(Util.null2str(sendManifest.getMasterOdJobNo()).equals("")){	/** 하우스그리드선택 */
			sendManifest.setOdJobNo(sendManifest.getHouseOdJobNoList().get(0));	/** 하우스관리번호로 마스터 조회 */
			masterOrder = manifestMapper.selectCustomsManifestMaster(sendManifest);
			if(sendManifest.getHouseOdJobNoList().size() != Util.null2int(masterOrder.get("SIMPLE_DOC_NO_CNT"))){	/** 해당 제출번호의 데이터건수와 그리드선택건수가 같지않음 */
				result.put("processMessage", "제출번호가 같지만 선택되지않은 오더가 있습니다.");
				orderSaveResult.setResultData(result);
				orderSaveResult.setResultCode("ERROR");
				return orderSaveResult;
			}
		}else{
			/** 마스터그리드선택일경우 해당 마스터에 전송가능한 제출번호  */
			//서브그리드 선택한 경우 해당 마스터 + 선택한 제출번호 중 전송가능한 제출번호 가져옴
			masterOrder = manifestMapper.selectCustomsManifestMaster(sendManifest);
		}
		
		if(masterOrder != null){
			if(Util.null2str(masterOrder.get("SIMPLE_DOC_NO_LIST")).equals("")){
				tempSearchData.put("srchCode", "MF-43");	// 해당 M.B/L에 H.B/L이 한 건도 없습니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : " + message));
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("BOUND", masterOrder.get("BOUND"));
				ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
				orderSaveResult.setResultData(result);
				orderSaveResult.setResultCode("ERROR");
				return orderSaveResult;
			}
			String[] simpleDocStrList = Util.null2str(masterOrder.get("SIMPLE_DOC_NO_LIST")).split(",");	/** 해당마스터에 전송해야할 제출번호리스트 */
			List<List<Map<String, Object>>> simpleDocNoHouseList = new ArrayList<List<Map<String, Object>>>();
			List<Map<String, Object>> tmpHouseOrderList = null;
			List<Map<String, Object>> tmpHouseOrderListSimpleDocNo = null;
			String simpleDocNo = null;
			String tmpStr, tmpStr2;
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", sendManifest.getEntCode());
			podMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
			podMap.put("solution", "ECM");
			podMap.put("optionCode", "C04");
			String consigneeNameType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
			podMap.put("optionCode", "C05");
			String consigneeAddressType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
			podMap.put("optionCode", "C09");
			String updateSimpleDocDateYn = Util.null2str(commonMapper.selectSystemOption(podMap), "N");
			masterOrder.put("updateSimpleDocDateYn", updateSimpleDocDateYn);
			
			int startNum, countCheck;
			sendManifest.setHouseCount(100);
			for(String simpleDocStr : simpleDocStrList){
				sendManifest.setSimpleDocNo(simpleDocStr);
				
				/** 데이터건수체크(두개의 브라우저에서 한번에 같은건을 전송했을경우 manifestMapper.updateCustomsManifest에서 상태업데이트 되면서 100건씩 조회하는데이터가 더이상 조회되지않고 종료됨) */
				sendManifest.setStartNum(999999);
				countCheck = Util.null2int(manifestMapper.selectCustomsManifestHouseItem(sendManifest).get(0).get("COUNT_CHECK"));
				
				boolean chkData = true;
				startNum = 0;
				tmpHouseOrderListSimpleDocNo = new ArrayList<Map<String, Object>>();
				
				while(chkData){
					sendManifest.setStartNum(startNum);
					tmpHouseOrderList = manifestMapper.selectCustomsManifestHouseItem(sendManifest);
					if(startNum != 0 && (tmpHouseOrderList == null || tmpHouseOrderList.size() == 0)){
						chkData = false;
						break;
					}
					this.getCustomsManifestValidate(masterOrder, tmpHouseOrderList, tempSearchData, errList, result, consigneeNameType, consigneeAddressType);
					startNum += 100;
					if(tmpHouseOrderList.size() != 100) {
						chkData = false;
					}
					tmpHouseOrderListSimpleDocNo.addAll(tmpHouseOrderList);
				}
				
				if(countCheck == tmpHouseOrderListSimpleDocNo.size()){
					simpleDocNoHouseList.add(tmpHouseOrderListSimpleDocNo);
				}else{
					errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : 전송중 변경된 데이터가 있습니다. 확인 후 처리 바랍니다."));
				}
			}
			
			if(errList.size() > 0 || Util.null2str(sendManifest.getPenaltyYn()).equals("Y")){
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("BOUND", masterOrder.get("BOUND"));
				ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
				orderSaveResult.setResultData(result);
				orderSaveResult.setResultCode("ERROR");
				return orderSaveResult;
			}
			
			if("errorCheck".equals(sendManifest.getAction())){
				orderSaveResult.setResultCode("errorCheck");
				return orderSaveResult;
			}
			
			podMap.put("userLanguage", sendManifest.getUserLanguage());
			podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
			podMap.put("srchCode", "ECM110");
			podMap.put("entCode", sendManifest.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", sendManifest.getGmtTime());
			podMap.put("updateUserId", sendManifest.getUpdateUserId());
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			podMap.put("podStatus", "ECM110");
			podMap.put("podTableName", 	"TB_ECM_POD_02");
			
			
			Map<String, Object> tmpMap = null;
			List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> upsList = new ArrayList<Map<String, Object>>();
			Map<String, Object> logMap = new HashMap<String, Object>();
			logMap.put("ent_code", sendManifest.getEntCode());
			logMap.put("customer_code", "");
			logMap.put("solution", "ECM");
			logMap.put("bound", bound);
			logMap.put("bound_type", "IMP");
			logMap.put("hbl_no", "");
			logMap.put("if_service_code", "");
			logMap.put("if_vender", "GOV");
			logMap.put("if_send_recv_type", "S");
			logMap.put("if_send_recv_method", "");
			logMap.put("if_status_info", "");
			logMap.put("if_sender_id", "");
			logMap.put("if_receiver_id", "");
			logMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
			logMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
			logMap.put("register_user_id", sendManifest.getRegisterUserId());
			logMap.put("register_user_name", sendManifest.getRegisterUserName());
			logMap.put("register_date_time", System.currentTimeMillis());
			logMap.put("if_doc_code", "CUSTOMS_CLEARANCE");
			logMap.put("if_doc_name", "통관목록");
			
			Map<String, Object> tmpInsertData = null;
			List<Map<String, Object>> insertGovSendList = new ArrayList<Map<String, Object>>();
			Map<String, Object> insertData = new HashMap<String, Object>();
			insertData.put("entCode", sendManifest.getEntCode());
			insertData.put("typeCode", "GOVCBR936");
			insertData.put("registerUserId", sendManifest.getRegisterUserId());
			insertData.put("gmtTime", sendManifest.getGmtTime());
			insertData.put("sendStatus", "N");
			insertData.put("entBusinessCode", "");
			insertData.put("updateUserId", sendManifest.getRegisterUserId());
			insertData.put("solution", "ECM");
			insertData.put("stockType", "EDI");
			insertData.put("stockSign", "EDI");
			insertData.put("stockSeqLength", "8");
			insertData.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			insertData.put("stockRemark", "sendCustomsManifestImp(GOVCBR936)");
			insertData.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));
			
			List<SendManifest> updateCustomsManifestList = new ArrayList<SendManifest>();
			SendManifest tmpSendManifest = null;
			
			List<Map<String, Object>> insertOrderPodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpPodData = null;
			
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			JSONObject tmpJsonObject = null;
			
			String today = DateTime.getToday();
			int xmlByte = 0;
			int houseCnt = 0;
			for(List<Map<String, Object>> houseOrderList : simpleDocNoHouseList){
				simpleDocNo = Util.null2str(houseOrderList.get(0).get("SIMPLE_DOC_NO"));
				houseCnt += houseOrderList.size();
				
				tmpSendManifest = new SendManifest();
				tmpSendManifest.setPodStatus("ECM110");
				tmpSendManifest.setGmtTime(sendManifest.getGmtTime());
				tmpSendManifest.setSimpleDocDate(today);
				tmpSendManifest.setMasterOdJobNo(sendManifest.getMasterOdJobNo());
				tmpSendManifest.setHouseOdJobNoList(sendManifest.getHouseOdJobNoList());
				tmpSendManifest.setSimpleDocNo(simpleDocNo);
				updateCustomsManifestList.add(tmpSendManifest);
				
				
				
				List<Map<String, Object>> houseItemList = null;
				List<String> poInfoList = null;
						
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
				Element lv5 = null;
				Map<String, String> attribute = new HashMap<String, String>();
				
				document = documentBuilder.newDocument();
				document.setXmlVersion("1.0");
				document.setXmlStandalone(true);
				root = document.createElement("wco:Declaration");
				
				root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_936SchemaModule:1:0");
				root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_936SchemaModule:1:0");
				root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_936SchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfSPC_936SchemaModule_1.0_standard.xsd");
				document.appendChild(root);
				
				/** 수입통관목록전송 내용 변경시 배치의 수입통관목록전송소스도 수정해야함(CargoProgressCheck.sendCustomsManifestImp) */
				
				/** 제출세관/과 */
				this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(masterOrder.get("CUSTOMS_CODE")).trim() + Util.null2str(masterOrder.get("CUSTOMS_DPT_CODE")).trim());
				/** 제출번호 */
				this.createElement(document, root, lv1, "wco:ID", simpleDocNo);
				/** 제출일자(CCYYMMDD) */
				this.createElement(document, root, lv1, "wco:IssueDateTime", today.replace("-", ""));
				/** 통관목록 화물건수 */
				this.createElement(document, root, lv1, "wco:LoadingListQuantity", Util.null2str(houseOrderList.size()));
				/** 문서형태구분 */
				this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBR936");
				/** 적하목록 관리 번호 */
				lv1 = this.createElement(document, root, lv1, "wco:AdditionalDocument", null);
				this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("MRN")).trim().toUpperCase());
				
				lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
				this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("SELECTOR_USER_NAME")).trim());
				this.createElement(document, lv1, lv2, "wco:StatementCode", "1");
				lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
				this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("CONFIRMER_USER_NAME")).trim());
				this.createElement(document, lv1, lv2, "wco:StatementCode", "2");
				
				/** wco:BorderTransportMeans 시작 */
				lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
				/** 입항일자(CCYYMMDD) */
				this.createElement(document, lv1, lv2, "wco:ArrivalDateTime", Util.null2str(masterOrder.get("AR_DATE")).trim().replace("-", ""));
				/** 항공편명/항차 */
				if(bound.equals("AIR")){
					this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim());	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
					this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "40");
				}else{
					this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("VOYAGE")).trim());
					this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "10");
				}
				/** wco:BorderTransportMeans 끝 */
				
				String consigneeAddress = null;
				String customsIdType = null;
				int itemSeq = 0;
				int poInfoSeq = 0;
				
				for(Map<String, Object> houseOrder : houseOrderList){
				
					if(consigneeAddressType.equals("ENG")){
						if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")).trim())){
							consigneeAddress = ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 0, 150);
						}else{
							consigneeAddress = ediCommonService.getSubStringK(ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS_LOC")), false), 0, 150);
						}
					}else{
						if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS_LOC")).trim())){
							consigneeAddress = ediCommonService.getSubStringK(ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS_LOC")), false), 0, 150);
						}else{
							consigneeAddress = ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 0, 150);
						}
					}
						
					/** wco:Consignment 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
					/** 검사요청대상 */
					this.createElement(document, lv1, lv2, "wco:GoodsStatusCode", Util.null2str(houseOrder.get("INSPECTION_YN"), "N"));
					/** 총중량 */
					this.createElement(document, lv1, lv2, "wco:GrossVolumeMeasure", Util.null2str(houseOrder.get("GWT")));
					/** 포장개수 */
					this.createElement(document, lv1, lv2, "wco:TotalPackageQuantity", Util.null2str(houseOrder.get("PIECES")));
					/** 물품가격(미화) */
					attribute.put("attributeName", "currencyID");
					attribute.put("attributeContent", "USD");
					this.createElement(document, lv1, lv2, "wco:ValueAmount",String.format("%.2f",Util.null2dbl(houseOrder.get("AMOUNT_USD"))), attribute);
					
					/** 수하인 식별부호 */
					/** 2024.09.11 - Z(데이터 없음)일 때는 항목 전송하지 않음 */
					customsIdType = Util.null2str(houseOrder.get("CUSTOMS_ID_TYPE"));
					if(!Util.isEmpty2(customsIdType) && !"Z".equals(customsIdType)){
						this.createElement(document, lv1, lv2, "kcs:TypeID", customsIdType);
					}
					
					/** 상세물품 수 */
					this.createElement(document, lv1, lv2, "kcs:DetailCountQuantity", Util.null2str(houseOrder.get("ITEM_CNT")));
					/** 내포장개수 */
					this.createElement(document, lv1, lv2, "kcs:InternalPackageQuantity", Util.null2str(houseOrder.get("PIECES_REAL")));
					
					/** wco:AdditionalInformation 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalInformation", null);
					/** House Air Way Bill 품명 */
					this.createElement(document, lv2, lv3, "wco:Content", ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("COMMODITY_ITEM")).trim(), "1"), 0, 190));
					/** 용도구분(1:개인,2:회사) */
					this.createElement(document, lv2, lv3, "wco:StatementCode", Util.null2str(houseOrder.get("CD_USE_TYPE")));
					/** 허용품목코드(1 : 화장지, 주방용기류 (48류), 2 : 서적, 인쇄물류 (49류), 3 : 의류 (61~63류), 4 : 신발류 (64류), 5 : 가구/조명기구류 (94류), 6 : 음악/영화용 CD (85류), 7 : 허용 비대상 품목) */
					this.createElement(document, lv2, lv3, "wco:StatementTypeCode", Util.null2str(houseOrder.get("ALLOWED_ITEM_CODE")).trim());
					/** wco:AdditionalInformation 끝 */
					
					/** wco:Agent 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:Agent", null);
					/** 화물운송주선업자부호 */
					tmpStr = Util.null2str(masterOrder.get("B2G_FORWARDER_SIGN")).trim().toUpperCase();
					if(!Util.isEmpty2(tmpStr)){
						this.createElement(document, lv2, lv3, "wco:ID", tmpStr);
					}
					/** 거래유형코드(A:전자상거래물품, D:개인 일반물품, E:상용견품, F:상업서류) */
					this.createElement(document, lv2, lv3, "kcs:TypeOfBusiness", Util.null2str(houseOrder.get("ECM_TYPE")));
					/** wco:Agent 끝 */
					
					/** wco:Consignee 시작 */
					tmpStr = Util.null2str(houseOrder.get("SELLER_REG_NO"));
					tmpStr2 = Util.null2str(houseOrder.get("SELLER_NAME"));
					if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)){
						lv2 = this.createElement(document, lv1, lv2, "wco:Consignee", null);
						/** 해외판매자부호 */
						if(!Util.isEmpty2(tmpStr)){
							this.createElement(document, lv2, lv3, "wco:ID", tmpStr);
						}
						/** 해외판매자명 */
						if(!Util.isEmpty2(tmpStr2)){
							this.createElement(document, lv2, lv3, "wco:Name", tmpStr2);
						}
					}
					/** wco:Consignee 끝 */
					
					/** wco:ConsignmentItem 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:ConsignmentItem", null);
					
					/** wco:AdditionalInformation 시작 */
					tmpStr = ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("URL")).trim(), "2");
					tmpStr2 = Util.null2str(houseOrder.get("ECM_BUY_TYPE"));
					if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)){
						lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
						/** 전자상거래사이트주소 */
						if(!Util.isEmpty2(tmpStr)){
							this.createElement(document, lv3, lv4, "wco:Content", ediCommonService.getSubStringK(tmpStr, 0, 300));
						}
						/** 전자상거래유형코드 */
						if(!Util.isEmpty2(tmpStr2)){
							this.createElement(document, lv3, lv4, "wco:StatementTypeCode", tmpStr2);
						}
					}
					/** wco:AdditionalInformation 끝 */
					
					/** wco:Agent 시작 */
					tmpStr = Util.null2str(houseOrder.get("AGENT_REG_NO"));
					tmpStr2 = Util.null2str(houseOrder.get("AGENT_NAME"));
					if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)){
						lv3 = this.createElement(document, lv2, lv3, "wco:Agent", null);
						/** 구매대행업자부호 */
						if(!Util.isEmpty2(tmpStr)){
							this.createElement(document, lv3, lv4, "wco:ID", tmpStr);
						}
						/** 구매대행업자명 */
						if(!Util.isEmpty2(tmpStr2)){
							this.createElement(document, lv3, lv4, "wco:Name", tmpStr2);
						}
					}
					/** wco:Agent 끝 */
					
					/** wco:Buyer 시작 */
					tmpStr = Util.null2str(houseOrder.get("SALES_BROKER_REG_NO"));
					tmpStr2 = Util.null2str(houseOrder.get("SALES_BROKER_NAME"));
					if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)){
						lv3 = this.createElement(document, lv2, lv3, "wco:Buyer", null);
						/** 판매중개자부호 */
						if(!Util.isEmpty2(tmpStr)){
							this.createElement(document, lv3, lv4, "wco:ID", tmpStr);
						}
						/** 판매중개자명 */
						if(!Util.isEmpty2(tmpStr2)){
							this.createElement(document, lv3, lv4, "wco:Name", tmpStr2);
						}
					}
					/** wco:Buyer 끝 */
					
					
					
					sendManifest.setOdJobNo(Util.null2str(houseOrder.get("OD_JOB_NO")));
					houseItemList = (List) houseOrder.get("houseItemList");
					attribute.put("attributeName", "kcsUnitCode");
					if(houseItemList == null || houseItemList.size() == 0){
						/** wco:Commodity 시작 */
						lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
						/** 일련번호 */
						this.createElement(document, lv3, lv4, "wco:SequenceNumeric", "1");
						/** 품명 */
						this.createElement(document, lv3, lv4, "wco:CargoDescription", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("COMMODITY_ITEM")), "1"));
						/** 수량(단위) */
						attribute.put("attributeContent", "EA");
						this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(houseOrder.get("PIECES")), attribute);
						/** 금액 */
						if(Util.null2dbl(houseOrder.get("AMOUNT_USD")) > 0){
							this.createElement(document, lv3, lv4, "wco:ValueAmount",  String.format("%.2f",Util.null2dbl(houseOrder.get("AMOUNT_USD"))));
						}
						/** wco:Commodity 끝 */
					}else{
						itemSeq = 1;
						for(Map<String, Object> houseItem : houseItemList){
							/** wco:Commodity 시작 */
							lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
							/** 일련번호 */
							this.createElement(document, lv3, lv4, "wco:SequenceNumeric", Util.null2str(itemSeq++));	//Convert.ToInt16(i + 1)
							/** 품명 */
							this.createElement(document, lv3, lv4, "wco:CargoDescription", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseItem.get("DETAIL_ITEM_NAME")), "1"));
							/** 수량(단위) */
							attribute.put("attributeContent", Util.null2str(houseItem.get("UNIT_PIECES"), "EA"));
							this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(houseItem.get("PIECES")), attribute);
							/** 규격 */
							this.createElement(document, lv3, lv4, "wco:Description", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseItem.get("BRAND")), "1"));
							/** 금액 */
							this.createElement(document, lv3, lv4, "wco:ValueAmount", String.format("%.2f",Util.null2dbl(houseItem.get("AMOUNT"))));
						}
					}
					
					/** wco:Consignee 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:Consignee", null);
					/** 수하인 성명 */
					if(consigneeNameType.equals("ENG")){
						if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim())){
							this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim(), "1"));
						}else{
							this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_NAME_LOC")).trim(), false));
						}
					}else{
						if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_NAME_LOC")).trim())){
							this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_NAME_LOC")).trim(), false));
						}else{
							this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim(), "1"));
						}
					}
					/** 수하인 고유부호 */
					/** 2024.09.11 - 수하인 식별부호가 Z(데이터 없음)일 때는 항목 전송하지 않음 */
					tmpStr = Util.null2str(houseOrder.get("CUSTOMS_ID")).trim().toUpperCase().replace("-", "").replace("/", "").replace(".", "").replace(" ", "");
					if(!Util.isEmpty2(tmpStr) && !"Z".equals(Util.isEmpty2(customsIdType))){
						this.createElement(document, lv3, lv4, "wco:ID", tmpStr);
					}
					
					/** wco:Address 시작 */
					lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
					/** 수하인 주소 */
					this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(consigneeAddress, 0, 150));
					/** 수하인 우편번호 */
					tmpStr = Util.null2str(houseOrder.get("CONSIGNEE_ZIPCODE")).replace(" ", "").replace("-", "");
					if(!Util.isEmpty2(tmpStr)){
						this.createElement(document, lv4, lv5, "wco:PostcodeID", ediCommonService.getSubStringK(tmpStr, 0, 6));
					}
					/** wco:Address 끝 */
					
					/** 수하인 전화번호 */
					lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
					this.createElement(document, lv4, lv5, "wco:ID", ediCommonService.EDI_Numeric_Select(Util.null2str(houseOrder.get("CONSIGNEE_TEL"))));
					/** wco:Consignee 끝 */
					
					/** wco:Consignor 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:Consignor", null);
					/** 송하인 성명 */
					this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_NAME")), "1"));
					
					/** wco:Address 시작 */
					lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
					/** 송하인 주소 */
					tmpStr = ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")), "1");
					if(!Util.isEmpty2(tmpStr)){
						this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(tmpStr, 0, 150));
					}
					/** 발송국가 */
					this.createElement(document, lv4, lv5, "wco:CountryCode", Util.null2str(houseOrder.get("SHIPPER_COUNTRY_CODE")).trim());
					/** wco:Address 끝 */
					
					/** 송하인 전화번호 */
					tmpStr = ediCommonService.EDI_Numeric_Select(Util.null2str(houseOrder.get("SHIPPER_TEL")));
					if(!Util.isEmpty2(tmpStr)){
						lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
						this.createElement(document, lv4, lv5, "wco:ID", tmpStr);
					}
					/** wco:Consignor 끝 */
					/** wco:ConsignmentItem 끝 */
					
					poInfoList = (List) houseOrder.get("poInfoList");
					if(poInfoList != null && poInfoList.size() > 0){
						poInfoSeq = 1;
						for(String poNo : poInfoList){
							/** wco:Commodity 시작 */
							lv2 = this.createElement(document, lv1, lv2, "wco:Commodity", null);
							/** 일련번호 */
							this.createElement(document, lv2, lv3, "kcs:SequenceNumeric", Util.null2str(poInfoSeq++));
							/** 주문번호 */
							this.createElement(document, lv2, lv3, "wco:ID", poNo);
							/** wco:Commodity 끝 */
						}
					}
					
					/** wco:TransportContractDocument 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
					/** House B/L 번호 */
					this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.DeleteCharString_Loop(Util.null2str(houseOrder.get("HBL_NO")).trim().toUpperCase(), " |-", "|"));
					/** House B/L 일련번호 */
					this.createElement(document, lv2, lv3, "kcs:HouseBLSequenceID", Util.null2str(houseOrder.get("HSN")).trim());
					/** wco:TransportContractDocument 끝 */
					/** wco:Consignment 끝 */
					
					tmpPodData = new HashMap<String, Object>();
					tmpPodData.put("odJobNo", houseOrder.get("OD_JOB_NO"));
					tmpPodData.put("blNo", houseOrder.get("HBL_NO"));
					if(houseOrder.get("ECM110_POD_JOB_NO") != null){
						tmpPodData.put("ECM110_POD_JOB_NO", houseOrder.get("ECM110_POD_JOB_NO"));
					}else{
						tmpPodData.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					}
					insertOrderPodList.add(tmpPodData);
				}
				
				/** wco:GoodsShipment 시작 */
				lv1 = this.createElement(document, root, lv1, "wco:GoodsShipment", null);
				/** 보세구역부호 */
				lv2 = this.createElement(document, lv1, lv2, "wco:Warehouse", null);
				if(Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE")).trim().length() == 8){
					this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE")).trim());
				}else{
					this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.getSubStringK(Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE_ETC")).trim(), 0, 8));
				}
				/** wco:GoodsShipment 끝 */
				
				/** 적출국(항) */
				lv1 = this.createElement(document, root, lv1, "wco:LoadingLocation", null);
				this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("ORIG_CODE")).trim());
				
				/** wco:TransportContractDocument 시작 */
				lv1 = this.createElement(document, root, lv1, "wco:TransportContractDocument", null);
				/** Master B/L번호 */
				if(bound.equals("AIR")){
					this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.getSubStringK(Util.null2str(masterOrder.get("MBL_NO")).trim(), 0, 11));
				}else{
					this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.GetBLNo_ZSeqDelete(Util.null2str(masterOrder.get("MBL_NO")).trim().toUpperCase()));
				}
				/** Master B/L 일련번호 */
				this.createElement(document, lv1, lv2, "kcs:MasterBLSequenceID", Util.null2str(masterOrder.get("MSN")));
				/** wco:TransportContractDocument 끝 */

				out = new ByteArrayOutputStream();
				source = new DOMSource(document);
				streamResult = new StreamResult(out);
				transformer.transform(source, streamResult);
				
				tmpJsonObject = new JSONObject();
				String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
				String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
				String filePath = customProperties.getBackupDir() + "edi/customs/" + sendManifest.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
				String fileName = dataId + "GOVCBR936.xml";
				FileUtil.fileWrite(xml, filePath, fileName);
				
				tmpJsonObject.put("customsReportNo", simpleDocNo);
				tmpJsonObject.put("xml", xml);
				tmpJsonObject.put("filePath", filePath);
				tmpJsonObject.put("fileName", fileName);
				jsonArray.add(tmpJsonObject);
				
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("if_file_path", filePath);
				tmpMap.put("if_file_name", fileName);
				tmpMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
				tmpMap.put("es_job_no", dataId);
				logList.add(tmpMap);
				
				xmlByte += xml.getBytes().length;
				out.close();
			}
			
			tmpPodData = new HashMap<String, Object>();
			tmpPodData.put("odJobNo", masterOrder.get("OD_JOB_NO"));
			tmpPodData.put("blNo", masterOrder.get("MBL_NO"));
			if(masterOrder.get("ECM110_POD_JOB_NO") != null){
				tmpPodData.put("ECM110_POD_JOB_NO", masterOrder.get("ECM110_POD_JOB_NO"));
			}else{
				tmpPodData.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			}
			insertOrderPodList.add(tmpPodData);
			
			result.put("masterOdJobNo", masterOrder.get("OD_JOB_NO"));
			
			result.put("podMap", podMap);
			result.put("insertOrderPodList", insertOrderPodList);
			result.put("updateCustomsManifestList", updateCustomsManifestList);
			
			jsonObject.put("dataList", jsonArray);
			result.put("REST", jsonObject);
			result.put("logList", logList);
			result.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));
			if(xmlByte > 1024 * 1024 * 19){
				result.put("sendType", "FILE");
			}
			
			/** 건수가 많을경우 전송시간이 오래 걸려서 전송전에 상태변경/로그저장함  */
//			for(SendManifest updateSendManifest : updateCustomsManifestList){
//				manifestMapper.updateCustomsManifest(updateSendManifest);
//				manifestMapper.updateSimpleErrorDescNull(updateSendManifest);
//			}
			
//			IndexApi indexApi = new IndexApi(client);
//			DocumentApi documentApi = null;
//			String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//			documentApi = new DocumentApi(client);
			
			logMap.put("if_status", "READY");
			logMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
			logMap.put("if_remark", "M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "(" + houseCnt + "건)");
			logMap.put("es_job_no", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "99999");
			
//			documentApi.createDocument(index, Util.null2str(logMap.get("es_job_no")), logMap);
			logMap.put("if_status", "SEND");
			logMap.put("if_remark", "");
			logMap.remove("es_job_no");
			
			result.put("logMap", logMap);
		}else{
			orderSaveResult.setResultCode("ERROR");
			result.put("processCode", "0000");
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "Master Order 자료가 존재하지 않습니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			result.put("processMessage", message);
		}
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	public void setCustomsManifestImp(SendManifest sendManifest, String cusSendType ,OrderSaveResult orderSaveResult) throws Exception {
		Map<String, Object> resultData = orderSaveResult.getResultData();
		
		SqlSession sqlSession = null;
		int cnt = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
			String customsErrCode = "";
			boolean success = true;	// 목록전송성공여부
			String token = null;
			
			if("REST".equals(cusSendType)){
				JSONObject jsonObject = (JSONObject)resultData.get("REST");
				String sendType = Util.null2str(resultData.get("sendType"), "LIST");
				jsonObject = ediCommonService.sendEbms(Util.null2str(resultData.get("ediPortalMailboxId")), "GOVCBR936", jsonObject, sendType);
				
				String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
				String index2 = "ecm-ups-tracking-" + Util.null2str(sendManifest.getEntCode()).toLowerCase();
				ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
				ArrayList<BulkData> bulkList2 = new ArrayList<BulkData>();	// UPS bulkList
				BulkData bulkData = null;
				int seq = 1;
				Map<String, Object> logMap = (Map)resultData.get("logMap");
				logMap.put("customs_err_desc", jsonObject.get("customsErrDesc"));
				logMap.put("customs_err_desc_additional", jsonObject.get("body"));
				customsErrCode = Util.null2str(jsonObject.get("customsErrCode"));
				if(!customsErrCode.equals("") && !customsErrCode.equals("C200")) {
					success = false; // 목록전송실패
				}
				
				List<Map<String, Object>> logList = (List)resultData.get("logList");
				for(Map<String, Object> logData : logList){
					logData.putAll(logMap);
					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(Util.null2str(logData.get("es_job_no")));
					bulkData.setJsonMap(logData);
					bulkList.add(bulkData);
				}
				
				if (bulkList != null && bulkList.size() > 0) {
					IndexApi indexApi = new IndexApi(client);
					if (!indexApi.existIndex(index))	indexApi.createIndex(index);
					
					DocumentApi documentApi = new DocumentApi(client);
					documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
				}
				
				List<Map<String, Object>> upsList = (List)resultData.get("upsList");
				if(success && upsList != null) {
					long trackingDateTime = System.currentTimeMillis();
					for(Map<String, Object> upsData : upsList) {
						upsData.put("DEMTrackingDateTime", trackingDateTime);
						bulkData = new BulkData();
						bulkData.setActionType(BulkData.Type.CREATE);
						bulkData.setIndexName(index2);
						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
						bulkData.setJsonMap(upsData);
						bulkList2.add(bulkData);
					}
					
					if (bulkList2 != null && bulkList2.size() > 0) {
						IndexApi indexApi = new IndexApi(client);
						if (!indexApi.existIndex(index2))	indexApi.createIndex(index2);
						
						DocumentApi documentApi = new DocumentApi(client);
						documentApi.bulkDocumentWithBulkProcessor(bulkList2, 10000);
					}
				}
				
			}else{
				Map<String, Object> insertGovSendMap = (Map)resultData.get("insertGovSendMap");
				List<Map<String, Object>> insertGovSendList = (List)resultData.get("DB");
				for(Map<String, Object> insertGovSendData : insertGovSendList){
					insertGovSendMap.putAll(insertGovSendData);
					ediCommonService.insertGovSend(commonBatchMapper, insertGovSendMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}
			
			Map<String, Object> masterLastPodMap = new HashMap<String, Object>();
			if(!success) {
				masterLastPodMap.put("lastPodStatus", "ECM111");
			}else {
				masterLastPodMap.put("lastPodStatus", "ECM110");
			}
			masterLastPodMap.put("gmtTime", sendManifest.getGmtTime());
			masterLastPodMap.put("updateType", "odJobNo");
			masterLastPodMap.put("odJobNo", resultData.get("masterOdJobNo"));
			commonMapper.updateLastPodStatus(masterLastPodMap);
			
			cnt = 1;
			List<SendManifest> updateCustomsManifestList = (List)resultData.get("updateCustomsManifestList");
			for(SendManifest updateSendManifest : updateCustomsManifestList){
				if(!success) {
					updateSendManifest.setPodStatus("ECM111");
				}
				updateSendManifest.setAction(cusSendType);
				updateSendManifest.setCustomsErrCode(customsErrCode);
				manifestBatchMapper.updateCustomsManifest(updateSendManifest);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			cnt = 1;
			Map<String, Object> podMap = (Map)resultData.get("podMap");
			List<Map<String, Object>> insertOrderPodList = (List)resultData.get("insertOrderPodList");
			List<Map<String, Object>> updateOrderPodList = new ArrayList<Map<String, Object>>();
			if(!success) {
				podMap.put("podStatus", "ECM111");
			}
			
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
			for(Map<String, Object> insertOrderPodMap : insertOrderPodList){
				if((success && insertOrderPodMap.get("ECM110_POD_JOB_NO") != null) || 	// 전송 성공했고 목록전송	POD가 이미 있는 경우 또는
					(!success && insertOrderPodMap.get("ECM111_POD_JOB_NO") != null)	// 전송 실패했고 목록전송실패	POD가 이미 있는 경우
				){
					updateOrderPodList.add(insertOrderPodMap);
				}else{
					podMap.putAll(insertOrderPodMap);
					orderBatchMapper.insertOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}
			cnt = 1;
			for(Map<String, Object> updateOrderPodMap : updateOrderPodList){
				if(!success) {
					updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("ECM111_POD_JOB_NO"));
				}else {
					updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("ECM110_POD_JOB_NO"));
				}
				podMap.putAll(updateOrderPodMap);
				orderBatchMapper.updateOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			/** 통관목록전송시 오류통보&목록전송실패POD삭제 */
			if(success) {
				cnt = 1;
				for(Map<String, Object> deleteOrderPodMap : insertOrderPodList){
					podMap.putAll(deleteOrderPodMap);
					podMap.put("podStatus", "ECM115");
					orderBatchMapper.deleteOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
					
					podMap.put("podStatus", "ECM111");
					orderBatchMapper.deleteOrderPod(podMap);
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
	public void setCustomsManifestImpTest(SendManifest sendManifest, OrderSaveResult orderSaveResult) throws Exception {
		Map<String, Object> resultData = orderSaveResult.getResultData();
		
//		SqlSession sqlSession = null;
//		int cnt = 1;
		try {
//			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
//			CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
//			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
//			ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
			String customsErrCode = "";
			String token = null;
			
			JSONObject jsonObject = (JSONObject)resultData.get("REST");
			String sendType = Util.null2str(resultData.get("sendType"), "LIST");
			jsonObject = ediCommonService.sendEbmsTest(Util.null2str(resultData.get("ediPortalMailboxId")), "GOVCBR936", jsonObject, sendType, null);
			
			String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			String index2 = "ecm-ups-tracking-upsxk";
			ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
			ArrayList<BulkData> bulkList2 = new ArrayList<BulkData>();	// UPS bulkList
			BulkData bulkData = null;
			int seq = 1;
			Map<String, Object> logMap = (Map)resultData.get("logMap");
			logMap.put("customs_err_desc", jsonObject.get("customsErrDesc"));
			logMap.put("customs_err_desc_additional", jsonObject.get("body"));
			customsErrCode = Util.null2str(jsonObject.get("customsErrCode"));
			
			List<Map<String, Object>> logList = (List)resultData.get("logList");
			for(Map<String, Object> logData : logList){
				logData.putAll(logMap);
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(Util.null2str(logData.get("es_job_no")));
				bulkData.setJsonMap(logData);
				bulkList.add(bulkData);
			}
			
//			if (bulkList != null && bulkList.size() > 0) {
//				IndexApi indexApi = new IndexApi(client);
//				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//				
//				DocumentApi documentApi = new DocumentApi(client);
//				documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
//			}
			
//			Map<String, Object> masterLastPodMap = new HashMap<String, Object>();
//			masterLastPodMap.put("lastPodStatus", "ECM110");
//			masterLastPodMap.put("gmtTime", sendManifest.getGmtTime());
//			masterLastPodMap.put("updateType", "odJobNo");
//			masterLastPodMap.put("odJobNo", resultData.get("masterOdJobNo"));
//			commonMapper.updateLastPodStatus(masterLastPodMap);
			
//			cnt = 1;
//			List<SendManifest> updateCustomsManifestList = (List)resultData.get("updateCustomsManifestList");
//			for(SendManifest updateSendManifest : updateCustomsManifestList){
//				updateSendManifest.setAction(cusSendType);
//				updateSendManifest.setCustomsErrCode(customsErrCode);
//				manifestBatchMapper.updateCustomsManifest(updateSendManifest);
//				if( (cnt++ % 1000) == 0){
//					sqlSession.flushStatements();
//					sqlSession.clearCache();
//				}
//			}
			
//			cnt = 1;
//			Map<String, Object> podMap = (Map)resultData.get("podMap");
//			List<Map<String, Object>> insertOrderPodList = (List)resultData.get("insertOrderPodList");
//			List<Map<String, Object>> updateOrderPodList = new ArrayList<Map<String, Object>>();
//			/** 취약성점검 SQL Injection 방지 */
//			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
//			for(Map<String, Object> insertOrderPodMap : insertOrderPodList){
//				if(insertOrderPodMap.get("ECM110_POD_JOB_NO") != null){
//					updateOrderPodList.add(insertOrderPodMap);
//				}else{
//					podMap.putAll(insertOrderPodMap);
//					orderBatchMapper.insertOrderPod(podMap);
//					if( (cnt++ % 1000) == 0){
//						sqlSession.flushStatements();
//						sqlSession.clearCache();
//					}
//				}
//			}
			
//			cnt = 1;
//			for(Map<String, Object> updateOrderPodMap : updateOrderPodList){
//				updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("ECM110_POD_JOB_NO"));
//				podMap.putAll(updateOrderPodMap);
//				orderBatchMapper.updateOrderPod(podMap);
//				if( (cnt++ % 1000) == 0){
//					sqlSession.flushStatements();
//					sqlSession.clearCache();
//				}
//			}
			
			/** 통관목록전송시 오류통보POD삭제 */
//			cnt = 1;
//			for(Map<String, Object> deleteOrderPodMap : insertOrderPodList){
//				podMap.putAll(deleteOrderPodMap);
//				podMap.put("podStatus", "ECM115");
//				orderBatchMapper.deleteOrderPod(podMap);
//				if( (cnt++ % 1000) == 0){
//					sqlSession.flushStatements();
//					sqlSession.clearCache();
//				}
//			}
//			sqlSession.flushStatements();
//			sqlSession.clearCache();
//			sqlSession.commit();
		}catch(Exception e) {
			throw e;
		}finally{
//			sqlSession.close();
		}
	}
	public OrderSaveResult resetSendCustomsManifest(SendManifest sendManifest) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> tmpMap;
		Map<String, Object> podMap = new HashMap<String, Object>();
		SqlSession sqlSession = null;
		int cnt = 1;
		
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
			
			/*
			 * masterOdJobNo 가 없는 경우(houseOdJobNoList 기반) IN 절 파라미터 개수가 SQL Server 최대값(2100)을
			 * 초과하는 문제가 있어, houseOdJobNoList 를 300개씩 잘라 반복 처리한다.
			 * masterOdJobNo 가 있는 경우에는 리스트를 사용하지 않으므로 한 번만 처리한다.
			 */
			final int CHUNK_SIZE = 300;
			String masterOdJobNo = Util.null2str(sendManifest.getMasterOdJobNo());
			List<String> houseOdJobNoList = sendManifest.getHouseOdJobNoList();
			
			List<List<String>> chunkList = new ArrayList<List<String>>();
			if(masterOdJobNo.isEmpty() && houseOdJobNoList != null && !houseOdJobNoList.isEmpty()){
				for(int i = 0; i < houseOdJobNoList.size(); i += CHUNK_SIZE){
					chunkList.add(houseOdJobNoList.subList(i, Math.min(i + CHUNK_SIZE, houseOdJobNoList.size())));
				}
			}else{
				// masterOdJobNo 기반 처리(리스트 미사용) : 원본 그대로 1회 처리
				chunkList.add(null);
			}
			
			for(List<String> chunk : chunkList){
				// 청크별 파라미터 : 원본을 얕은 복사한 뒤 houseOdJobNoList 만 서브리스트로 교체
				SendManifest chunkParam = new SendManifest();
				BeanUtils.copyProperties(chunkParam, sendManifest);
				if(chunk != null){
					chunkParam.setHouseOdJobNoList(chunk);
				}
				
				manifestMapper.deleteResetSendCustomsManifest(chunkParam);
				List<Map<String, Object>> resetDataList = manifestMapper.selectResetSendCustomsManifest(chunkParam);
				for(Map<String, Object> resetData : resetDataList){
					tmpMap = commonMapper.selectManifestLastPodInfo(resetData);
					if(tmpMap != null && tmpMap.size() > 0){
						if("H".equals(resetData.get("BL_TYPE_MH"))){
							podMap.put("lastPodStatus", tmpMap.get("POD_STATUS"));
							podMap.put("lastPodDate", tmpMap.get("POD_DATE_TIME"));
						}else{
							podMap.put("lastPodStatusMaster", tmpMap.get("POD_STATUS"));
							podMap.put("lastPodDateMaster", tmpMap.get("POD_DATE_TIME"));
						}
					}
					
					tmpMap = BeanUtils.describe(chunkParam);
					tmpMap.put("odJobNo", resetData.get("OD_JOB_NO"));
					tmpMap.putAll(podMap);
					manifestBatchMapper.updateResetSendCustomsManifestInfo(tmpMap);
					if( (cnt++ % 300) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
					
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
				
				if("resetSendCustomsManifest".equals(sendManifest.getAction())){
					tmpMap = BeanUtils.describe(chunkParam);
					tmpMap.putAll(podMap);
					tmpMap.put("houseOdJobNoList", chunkParam.getHouseOdJobNoList());
					tmpMap.put("simpleDocNoList", chunkParam.getSimpleDocNoList());
			//		manifestMapper.updateResetSendCustomsManifest(tmpMap);
					manifestMapper.updateResetSendCustomsManifest2(tmpMap);
				}
			}
		}catch(Exception e) {
			throw e;
		}finally {
			sqlSession.close();
		}
		
		return orderSaveResult;
	}
	public OrderSaveResult resetSimpleDocNo(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		String bound = "COM";
		int cnt = 0;
		List<Map<String, Object>> list = manifestMapper.selectResetSimpleDocNoList(sendManifest);
		if(list.size() > 0){
			bound = Util.null2str(list.get(0).get("BOUND"));
			String ediExpressSign = null, ediExpressSignDocumentY = "", ediExpressSignDocumentN = "";
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("srchEntCode", list.get(0).get("ENT_CODE"));
			tmpMap.put("srchEntBusinessCode", list.get(0).get("ENT_BUSINESS_CODE"));
			tmpMap.put("srchEdiCityPort", list.get(0).get("ORIG_CODE"));
			tmpMap.put("srchBound", bound);
			Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmpMap);
			if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
				ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN"));
				ediExpressSignDocumentY = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
				ediExpressSignDocumentN = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"));
			}
			
			if(!Util.isEmpty(ediExpressSign)) {
				Map<String, Object> simpleDocNoMap = new HashMap<String, Object>();
				simpleDocNoMap.put("entCode", tmpMap.get("srchEntCode"));
				simpleDocNoMap.put("gmtTime", sendManifest.getGmtTime());
				simpleDocNoMap.put("entBusinessCode", "");
				simpleDocNoMap.put("updateUserId", sendManifest.getUpdateUserId());
				simpleDocNoMap.put("solution", "ECM");
				simpleDocNoMap.put("stockType", "OD");
				simpleDocNoMap.put("stockSeqLength", "8");
				simpleDocNoMap.put("signType", "noHyphen");
				simpleDocNoMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				simpleDocNoMap.put("stockRemark", "ORDER SIMPLE DOC NO");
				
				SqlSession sqlSession = null;
				try {
					sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
					ConsolMapper consolBatchMapper = sqlSession.getMapper(ConsolMapper.class);
					for(Map<String, Object> map : list){
						if("E".equals(map.get("CD_TYPE"))){
							simpleDocNoMap.put("dateLength", "2");
							simpleDocNoMap.put("stockDateFormat", "12");
						}else{
							simpleDocNoMap.put("dateLength", "4");
							simpleDocNoMap.put("stockDateFormat", "112");
						}
						
						if("Y".equals(map.get("DOCUMENT_YN")) && !"".equals(ediExpressSignDocumentY)){
							simpleDocNoMap.put("odJobNo", map.get("OD_JOB_NO"));
							simpleDocNoMap.put("stockSign", ediExpressSignDocumentY);
							simpleDocNoMap.put("seqNo", cnt);
							consolBatchMapper.updateSimpleDocNoExp(simpleDocNoMap);
							cnt++;
						}else if("N".equals(map.get("DOCUMENT_YN")) && !"".equals(ediExpressSignDocumentN)){
							simpleDocNoMap.put("odJobNo", map.get("OD_JOB_NO"));
							simpleDocNoMap.put("stockSign", ediExpressSignDocumentN);
							simpleDocNoMap.put("seqNo", cnt);
							consolBatchMapper.updateSimpleDocNoExp(simpleDocNoMap);
							cnt++;
						}
						if( (cnt % 1000) == 0){
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
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setJobFuncType("UPDATE");
		processLogs.setEntCode(sendManifest.getEntCode());
		processLogs.setEntBusinessCode(sendManifest.getEntBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(bound);
		processLogs.setBoundType("EXP");
		processLogs.setProcessUserId(sendManifest.getUpdateUserId());
		processLogs.setJobNo("");
		processLogs.setLogJobType("DATA");
		if("resetSimpleDocNoY".equals(sendManifest.getAction())){
			processLogs.setDescription("통관목록신고 제출번호 초기화[처리건수 : " + cnt +"]");
		}else{
			processLogs.setDescription("이행신고 제출번호 초기화[처리건수 : " + cnt +"]");
		}
		processLogs.setJobLog(new Gson().toJson(sendManifest));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(sendManifest.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(sendManifest.getLogUserName());
		processLogs.setProcessUserNameEng(sendManifest.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return orderSaveResult;
	}
	
	/**
	 * 목록신고 오류검증
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean getCustomsManifestValidate(Map<String, Object> masterOrder, List<Map<String, Object>> houseOrder, Map<String, Object> tempSearchData, List<Map<String, Object>> errList, Map<String, Object> result
			, String consigneeNameType, String consigneeAddressType) throws java.lang.Exception {
		String message = null;
		String ecmBuyType = null;
		List<Map<String, Object>>  houseItemList = null;
        Map<String, Object> tempHblRow = null;
        Map<String, Object> tmpMap = new HashMap<String, Object>();
        SendManifest sendManifest = new SendManifest();
        SearchOrder searchOrder = new SearchOrder();
		
        String mblNo = Util.null2str(masterOrder.get("MBL_NO"));
        String hblNo = null;
        String hsn = null;
		String bound = Util.null2str(masterOrder.get("BOUND"));
		String allowedItemCode = null;
		String ecmType = null;
		
		LocalDate today = LocalDate.now();
		// 1월 1일 ~ 1월 5일 여부
		boolean isBlockedPeriod =
		        today.getMonthValue() == 1 &&
		        today.getDayOfMonth() >= 1 &&
		        today.getDayOfMonth() <= 5;
		        
		if (houseOrder.size() <= 0) {
			tempSearchData.put("srchCode", "MF-43");	// 해당 M.B/L에 통관목록신고 가능한 H.B/L이 한 건도 없습니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : " + message));
		}
		
		if ("".equals(Util.null2str(masterOrder.get("MBL_NO")).trim())) {
			tempSearchData.put("srchCode", "MF-70");	// M.B/L 번호를 기재 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("LINE_CODE")).trim()) && bound.equals("AIR")) {
			tempSearchData.put("srchCode", "MF-06");	//항공사를 기재하기 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("MRN")).trim()) && !(Util.null2str(masterOrder.get("BOUND")).equals("AIR") && Util.null2str(masterOrder.get("BOUND_TYPE")).equals("EXP"))) {
			tempSearchData.put("srchCode", "MF-65");	//적하목록관리번호(MRN)를 기재 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if(Util.null2str(masterOrder.get("BOUND_TYPE")).equals("EXP")){
			if ("".equals(Util.null2str(masterOrder.get("OB_DATE")).trim())) {
				tempSearchData.put("srchCode", "MF-08");	//출항일을 확인하십시오.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			if ("".equals(Util.null2str(masterOrder.get("OB_TIME")).trim())) {
				tempSearchData.put("srchCode", "MF-108");	//yyMMdd:HHmm 형식의 일시 자료가 없습니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			if ("".equals(Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim())) {
				tempSearchData.put("srchCode", "MF-07");	//편명은 최소 3자리 이상이어야만 합니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			if ("".equals(Util.null2str(masterOrder.get("DEST_COUNTRY")).trim())) {
				tempSearchData.put("srchCode", "MF-79");	//도착지를 기재하기 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			if ("".equals(Util.null2str(masterOrder.get("DEST_CODE")).trim())) {
				tempSearchData.put("srchCode", "MF-79");	//도착지를 기재하기 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			if ("".equals(Util.null2str(masterOrder.get("CARRYOUT_CODE")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "반출사유를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
		}else{
			if ("".equals(Util.null2str(masterOrder.get("CUSTOMS_CODE")).trim())) {
				tempSearchData.put("srchCode", "MF-58");	//세관(관세청) 등록부호를 기재하기 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			if ("".equals(Util.null2str(masterOrder.get("CUSTOMS_DPT_CODE")).trim())) {
				tempSearchData.put("srchCode", "MF-58");	//세관(관세청) 등록부호를 기재하기 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			/** 전송시 일자를 제출일자로 사용 */
//			if ("".equals(Util.null2str(masterOrder.get("SIMPLE_DOC_DATE")).trim())) {
//				tempSearchData.put("srchCode", "");
//				tempSearchData.put("message", "제출일자를 기재하기 바랍니다.");
//				message = ediCommonService.getErrorCodeName(tempSearchData);
//				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
//			}
			if ("".equals(Util.null2str(masterOrder.get("MSN")).trim())) {
				tempSearchData.put("srchCode", "MF-111");	//MSN를 기재 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			if ("".equals(Util.null2str(masterOrder.get("AR_DATE")).trim())) {
				tempSearchData.put("srchCode", "MF-112");	//입항일자를 기재 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			if ("".equals(Util.null2str(masterOrder.get("ORIG_CODE")).trim())) {
				tempSearchData.put("srchCode", "MF-77");	//출발지를 기재하기 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			/** 2021-12-01 마스터한건에 제출번호 여러종류전송이 가능하고 제출번호 없는건은 전송하지 않으므로 주석처리 */
//				if ("".equals(Util.null2str(masterOrder.get("SIMPLE_DOC_NO")).trim())) {
//					tempSearchData.put("srchCode", "");
//					tempSearchData.put("message", "제출번호를 기재하기 바랍니다.");
//					message = ediCommonService.getErrorCodeName(tempSearchData);
//					errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
//				}
			if(!"UPSXK".equals(sendManifest.getEntCode())) {
				if ("".equals(Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE")).trim()) && "".equals(Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE_ETC")).trim())) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "보세구역부호를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
				}
			}
			if ("".equals(Util.null2str(masterOrder.get("SELECTOR_USER_NAME")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "목록선별자를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			if ("".equals(Util.null2str(masterOrder.get("CONFIRMER_USER_NAME")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "확인자를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
			if(bound.equals("AIR")){
				if ("".equals(Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim())) {
					tempSearchData.put("srchCode", "MF-07");	//편명은 최소 3자리 이상이어야만 합니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
				}
			}else{
				if ("".equals(Util.null2str(masterOrder.get("VOYAGE")).trim())) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "항차를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
				}
			}
			/** 관세청사서함 */
			if(Util.null2str(masterOrder.get("EDI_PORTAL_MAILBOX_ID")).equals("")){
				tmpMap.put("srchEntCode", masterOrder.get("ENT_CODE"));
				tmpMap.put("srchEntBusinessCode", masterOrder.get("ENT_BUSINESS_CODE"));
				tmpMap.put("srchBound", masterOrder.get("BOUND"));
				tmpMap.put("srchSeqNo", masterOrder.get("EDI_MAILBOX_SEQ_NO"));
				if(Util.null2str(masterOrder.get("BOUND_TYPE")).equals("EXP")){
					tmpMap.put("srchEdiCityPort", masterOrder.get("ORIG_CODE"));
				}else{
					tmpMap.put("srchEdiCityPort", masterOrder.get("DEST_PORT"));
				}
				Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiEcmInfo(tmpMap);
				if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
					masterOrder.put("EDI_PORTAL_MAILBOX_ID", busEdiEcmMap.get("EDI_PORTAL_MAILBOX_ID"));
				}else{
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "관세청사서함을 설정 후 전송하십시오.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
				}
			}
		}
		
		String nowDate = null;
		if("Y".equals(masterOrder.get("updateSimpleDocDateYn"))){
			nowDate = DateTimeUtil.getNow("yyyy-MM-dd");
		}
		
		boolean mrnMsnEqualFlag = false;
		boolean mrnMsnNullFlag = false;
		for (int i = 0; i < houseOrder.size(); ++i) {
			tempHblRow = houseOrder.get(i);
			hblNo = Util.null2str(tempHblRow.get("HBL_NO")).trim();
			hsn = Util.null2str(tempHblRow.get("HSN")).trim();
			allowedItemCode = Util.null2str(tempHblRow.get("ALLOWED_ITEM_CODE"));
			ecmType = Util.null2str(tempHblRow.get("ECM_TYPE"));
			
			if ("".equals(Util.null2str(tempHblRow.get("COMMODITY_ITEM")).trim())) {
				tempSearchData.put("srchCode", "MF-20");	//품명을 기재하기 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
			}
			if (Util.null2dbl(tempHblRow.get("GWT")) == 0) {
				tempSearchData.put("srchCode", "MF-17");	//HOUSE 중량을 기재하기 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
			}
			if (Util.null2dbl(tempHblRow.get("PIECES")) == 0) {
				tempSearchData.put("srchCode", "MF-16");	//HOUSE 수량을 기재하기 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
			}
			if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_NAME")).trim())) {
				tempSearchData.put("srchCode", "MF-21");	//송하인 상호를 기재하기 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
			}
			if(Util.null2str(masterOrder.get("BOUND_TYPE")).equals("EXP")){
				if ("".equals(Util.null2str(tempHblRow.get("CUSTOMS_CODE")).trim())) {
					tempSearchData.put("srchCode", "MF-58");	//세관(관세청) 등록부호를 기재하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("CUSTOMS_DPT_CODE")).trim())) {
					tempSearchData.put("srchCode", "MF-58");	//세관(관세청) 등록부호를 기재하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("EDI_PORTAL_MAILBOX_ID")).trim())) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "관세청사서함을 설정 후 전송하십시오.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				
				if(allowedItemCode.length() < 2){
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "세번부호(HS)를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if(ecmType.equals("01") && (!"04".equals(tempHblRow.get("CUSTOMS_ID_TYPE")) || Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).trim().equals(""))){
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "송하인 사업자번호를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				
				if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_ADDRESS")).trim())) {
					tempSearchData.put("srchCode", "MF-86");	//송하인 주소를 기재하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim())) {
					tempSearchData.put("srchCode", "MF-26");	//수하인 상호를 기재하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ADDRESS")).trim())) {
					tempSearchData.put("srchCode", "MF-89");	//수하인 주소를 기재하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("HBL_NO")).trim())) {
					tempSearchData.put("srchCode", "MF-74");	// House B/L 번호를 기재하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(ecmType)) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "간이수출거래구분코드를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("SIMPLE_DOC_NO")).trim())) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "신고번호를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if (Util.null2dbl(tempHblRow.get("AMOUNT_LOC")) == 0) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "가격을 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("Y".equals(tempHblRow.get("GOODS_LOC_INSPECT_YN")) && "".equals(Util.null2str(tempHblRow.get("GOODS_LOC_INSPECT_REASON")))) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "물품소재지검사신청여부가 Y이면 사유를 입력해야 합니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
			}else{
				if(nowDate != null && !Util.null2str(tempHblRow.get("SIMPLE_DOC_DATE")).equals(nowDate)){
					errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : 신고일자가 잘못 입력 되어 있습니다."));
					nowDate = null;
				}
				if(ecmType.equals("A")){
					if(allowedItemCode.length() != 6){
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + "거래구분이 A:전자상거래이면 품목코드는 6자리로 입력해야 합니다.", hblNo, hsn));
					}
				}else if(allowedItemCode.length() != 2 && allowedItemCode.length() != 4 && allowedItemCode.length() != 6){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + "품목코드는 2자리, 4자리, 6자리로 입력해야 합니다.", hblNo, hsn));
				}
				
				if(allowedItemCode.length() > 2){
					allowedItemCode = allowedItemCode.substring(0, 2);
				}
				/** 00~22, 24, 29, 30, 77, 98, 99일경우 전송불가로 변경 */
//				if("00|12|16|17|18|19|20|21|22|24|30|77|98|99".indexOf(allowedItemCode) > -1){
				if("24|29|30|77|98|99".indexOf(allowedItemCode) > -1 || !Util.isNumber(allowedItemCode) || Util.null2int(allowedItemCode) < 23){
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + "품목코드 " + allowedItemCode + "는 일반 건으로 신고해야 합니다.", hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("INSPECTION_YN")).trim())) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "검사요청대상을 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_COUNTRY_CODE")).trim())) {
					tempSearchData.put("srchCode", "MF-22");	//송하인 국적을 기재하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_TEL")).trim())) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "수하인전화번호를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("ALLOWED_ITEM_CODE")).trim())) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "허용품목코드를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if (Util.null2dbl(tempHblRow.get("AMOUNT_USD")) == 0) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "물품가격(미화)를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(ecmType)) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "거래유형코드를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if(!"Y".equals(Util.null2str(tempSearchData.get("dkxYn")))){
					if ("".equals(Util.null2str(tempHblRow.get("CD_USE_TYPE")).trim())) {
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "용도구분을 기재하기 바랍니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
				}
				if (Util.null2dbl(tempHblRow.get("ITEM_CNT")) == 0) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "상세물품을 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if("UPSXK".equals(Util.null2str(tempSearchData.get("entCode")))) {
					if ("".equals(Util.null2str(tempHblRow.get("MF_DATE")).trim())) {
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "적하목록 전송 이력이 없습니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
				}
				/** 사업장에서 수하인설정값과 관계없이 한글,영문 중 하나라도 값이 있으면 오류처리하지않음*/
//				if(consigneeNameType.equals("ENG")){
//					if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim())) {
//						tempSearchData.put("srchCode", "MF-26");	//수하인 상호를 기재하기 바랍니다.
//						message = ediCommonService.getErrorCodeName(tempSearchData);
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//					}
//				}else{
//					if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_NAME_LOC")).trim())) {
//						tempSearchData.put("srchCode", "");
//						tempSearchData.put("message", "수하인명(한글)을 기재하기 바랍니다.");
//						message = ediCommonService.getErrorCodeName(tempSearchData);
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//					}
//				}
//				if(consigneeAddressType.equals("ENG")){
//					if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ADDRESS")).trim())) {
//						tempSearchData.put("srchCode", "AM-38");	//수하인 주소를 기재하기 바랍니다.
//						message = ediCommonService.getErrorCodeName(tempSearchData);
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//					}
//				}else{
//					if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ADDRESS_LOC")).trim())) {
//						tempSearchData.put("srchCode", "");
//						tempSearchData.put("message", "수하인주소(한글)을 기재하기 바랍니다.");
//						message = ediCommonService.getErrorCodeName(tempSearchData);
//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//					}
//				}
				if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim()) && "".equals(Util.null2str(tempHblRow.get("CONSIGNEE_NAME_LOC")).trim())) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "수하인명을 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ADDRESS")).trim()) && "".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ADDRESS_LOC")).trim())){
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "수하인주소를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				
				if(!"Y".equals(Util.null2str(tempSearchData.get("dkxYn")))){
					if(!ecmType.equals("A") && !Util.null2str(tempHblRow.get("URL")).equals("")){
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "거래구분 : D일반물품, E.상업견품, F.상업서류 이면,  전자상거래 URL 주소를 기재하면 안됩니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					if(!ecmType.equals("A") && !Util.null2str(tempHblRow.get("ECM_BUY_TYPE")).equals("")) {
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "거래유형 A(전자상거래물품)인 경우만 전자상거래유형코드 입력이(가) 가능합니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					
					if(Util.null2str(tempHblRow.get("CD_USE_TYPE")).equals("1")){
						if(ecmType.equals("A") && !"AEF".contains(Util.null2str(tempHblRow.get("CUSTOMS_ID_TYPE")))){
							tempSearchData.put("srchCode", "");
							tempSearchData.put("message", "용도구분 : 개인, 거래구분 : 전자상거래(개인)일경우 개인통관식별자는 A/E/F만 입력 가능합니다.");
							message = ediCommonService.getErrorCodeName(tempSearchData);
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}else if(ecmType.equals("D") && !"ABEF".contains(Util.null2str(tempHblRow.get("CUSTOMS_ID_TYPE")))){
							tempSearchData.put("srchCode", "");
							tempSearchData.put("message", "용도구분 : 개인, 거래구분 : 개인(일반)일경우 개인통관식별자는 A/B/E/F만 입력 가능합니다.");
							message = ediCommonService.getErrorCodeName(tempSearchData);
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}
						if(ecmType.equals("E") || ecmType.equals("F")){
							tempSearchData.put("srchCode", "");
							tempSearchData.put("message", "용도구분 1(개인)이면, 거래구분 A.전자상거래,D.일반물품만 입력 가능합니다.");
							message = ediCommonService.getErrorCodeName(tempSearchData);
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}else if("".equals(Util.null2str(tempHblRow.get("CUSTOMS_ID")).trim())){		//용도구분이 1(개인)이고 거래유형코드가 A.전자상거래, D.일반물품인 경우
							tempSearchData.put("srchCode", "");
							tempSearchData.put("message", "수하인 식별번호를 기재하기 바랍니다.");
							message = ediCommonService.getErrorCodeName(tempSearchData);
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}
					}else if(Util.null2str(tempHblRow.get("CD_USE_TYPE")).equals("2")){
						if(ecmType.equals("A") || ecmType.equals("D")){
							tempSearchData.put("srchCode", "");
							tempSearchData.put("message", "용도구분 2(회사)이면, 거래구분 E.상용견품, F.상업서류만 입력 가능합니다.");
							message = ediCommonService.getErrorCodeName(tempSearchData);
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}
						/** 2024.08.19 용도구분 값이 2(회사)이고 개인통관부호가 NULL인 경우 전송가능하게 변경 */
	//					else if("".equals(Util.null2str(tempHblRow.get("CUSTOMS_ID")).trim())){		//용도구분이 2(회사)이고 거래유형코드가 E.상용견품, F.상업서류인 경우
	//						tempSearchData.put("srchCode", "");
	//						tempSearchData.put("message", "수하인 식별번호를 기재하기 바랍니다.");
	//						message = ediCommonService.getErrorCodeName(tempSearchData);
	//						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	//					}
					}
				}
				ecmBuyType = Util.null2str(tempHblRow.get("ECM_BUY_TYPE"));
				if(ecmBuyType.equals("A") || ecmBuyType.equals("B") || ecmBuyType.equals("C")){
					/** DKX 목록신고 일때는 전자상거래 유형 검증 다름 **/
                    if("Y".equals(Util.null2str(tempSearchData.get("dkxYn")))){
                        if(ecmBuyType.equals("A")){
                            if(Util.null2str(tempHblRow.get("SALES_BROKER_REG_NO")).equals("") || Util.null2str(tempHblRow.get("SALES_BROKER_NAME")).equals("")){
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "전자상거래 유형이 A이면, 판매중개자(코드/이름) 항목을 필수 입력해야 합니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }
                        }
                        if(ecmBuyType.equals("B")){
                            if(Util.null2str(tempHblRow.get("AGENT_REG_NO")).equals("") || Util.null2str(tempHblRow.get("AGENT_NAME")).equals("")){
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "전자상거래 유형이 B이면, 구매대행업자(코드/이름) 항목을 필수 입력해야 합니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }
                            if(Util.null2str(tempHblRow.get("SALES_BROKER_REG_NO")).equals("") || Util.null2str(tempHblRow.get("SALES_BROKER_NAME")).equals("")) {
                            	tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "전자상거래 유형이 B이면, 판매중개자(코드/이름) 항목을 필수 입력해야 합니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }
                        }
                        if(ecmBuyType.equals("C")){
                            if(Util.null2str(tempHblRow.get("FORWARD_AGENT_CODE")).equals("") || Util.null2str(tempHblRow.get("FORWARD_AGENT_NAME")).equals("")){
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "전자상거래 유형이 C이면, 배송대행업자(코드/이름) 항목을 필수 입력해야 합니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }
                        }
                    }else{
                        if(Util.null2str(tempHblRow.get("SELLER_REG_NO")).equals("") && Util.null2str(tempHblRow.get("SELLER_NAME")).equals("")){
                            tempSearchData.put("srchCode", "");
                            tempSearchData.put("message", "전자상거래 유형이 A, B, C이면, 해외판매자(코드/이름) 항목을 필수 입력해야 합니다.");
                            message = ediCommonService.getErrorCodeName(tempSearchData);
                            errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                        }
                        if((ecmBuyType.equals("B") || ecmBuyType.equals("C")) && (Util.null2str(tempHblRow.get("AGENT_REG_NO")).equals("") && Util.null2str(tempHblRow.get("AGENT_NAME")).equals(""))){
                            tempSearchData.put("srchCode", "");
                            tempSearchData.put("message", "전자상거래 유형이 B,C이면, 구매,배송대행업자(코드/이름) 항목을 필수 입력해야 합니다.");
                            message = ediCommonService.getErrorCodeName(tempSearchData);
                            errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                        }
                    }
				}
				if(!"Y".equals(Util.null2str(tempSearchData.get("dkxYn")))){
					if (Util.null2str(tempHblRow.get("CUSTOMS_ID_TYPE")).equals("A") && Util.null2str(tempHblRow.get("CUSTOMS_ID")).equals("")) {
						tempSearchData.put("srchCode", "");
						if(ecmType.equals("A")) tempSearchData.put("message", " 거래유형이 A (개인) 이면, 개인통관부호(A) 입력을 해야 합니다.");
						else if(ecmType.equals("D"))tempSearchData.put("message", " 거래유형이 D (개인일반물품) 이면, 개인통관부호(A) 입력을 해야 합니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					
					if(Util.null2str(tempHblRow.get("CUSTOMS_ID_TYPE")).equals("Z") && !Util.null2str(tempHblRow.get("CUSTOMS_ID")).equals("")) {
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "개인통관식별자가 Z(데이터 없음) 이면, 수하인 식별번호를 기재하면 안됩니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
				}
				if (Util.null2dbl(tempHblRow.get("PIECES_REAL")) == 0) {
					tempSearchData.put("srchCode", "MF-59");	//House 실제 수량을 기재하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				
				if ("ECM131".equals(Util.null2str(tempHblRow.get("LAST_POD_STATUS"))) || "ECM132".equals(Util.null2str(tempHblRow.get("LAST_POD_STATUS")))) {
					tempSearchData.put("srchCode", "MF-147");	//취하전송,접수된 데이터는 전송 불가능 합니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				
				if("UPSXK".equals(Util.null2str(masterOrder.get("ENT_CODE"))) || "YHTXX".equals(Util.null2str(masterOrder.get("ENT_CODE")))) {
					if (Util.null2str(tempHblRow.get("SELECTOR_USER_NAME")).equals("")) {
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "목록선별자를 기재하기 바랍니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					
					if (Util.null2str(tempHblRow.get("CONFIRMER_USER_NAME")).equals("")) {
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "확인자를 기재하기 바랍니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
				}
				
                // DKX목록신고 검증
                if("Y".equals(Util.null2str(tempSearchData.get("dkxYn")))){

                    if ("".equals(Util.null2str(tempHblRow.get("ORDER_NAME")).trim())) {
                        tempSearchData.put("srchCode", "MF-144");	//주문자 성명을 기재하기 바랍니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }

                    if ("".equals(Util.null2str(tempHblRow.get("DELIVERY_NAME")).trim())) {
                        tempSearchData.put("srchCode", "MF-145");	//배송업체명을 기재하기 바랍니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }

                    if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ZIPCODE")).trim())) {
                        tempSearchData.put("srchCode", "MF-146");	//수하인 우편번호를 기재하기 바랍니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                }
                
                // DKX 목록신고 전자상거래 여뷰 체크 
                if("Y".equals(Util.null2str(tempSearchData.get("dkxYn")))){
                	if (!"Y".equals(Util.null2str(tempHblRow.get("ECM_YN")).trim())) {
                        tempSearchData.put("srchCode", "MF-149");	// 전자상거래 여부를 확인하시기 바랍니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                }else {
                	if ("Y".equals(Util.null2str(tempHblRow.get("ECM_YN")).trim())) {
                        tempSearchData.put("srchCode", "MF-149");	// 전자상거래 여부를 확인하시기 바랍니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                }
                
				/** 유예기간 주석처리 */
//				if(ecmType.equals("A") && ecmBuyType.equals("")){
//					tempSearchData.put("srchCode", "");
//					tempSearchData.put("message", "거래구분 : A전자상거래이면 전자상거래 유형은 NONE이어야 합니다.");
//					message = ediCommonService.getErrorCodeName(tempSearchData);
//					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//				}else if(!ecmType.equals("A") && !ecmBuyType.equals("")) {
//					tempSearchData.put("srchCode", "");
//					tempSearchData.put("message", "거래구분 : D일반물품, E.상업견품, F.상업서류 이면, 전자상거래 유형은 A/B/C/Z를 필수 입력해야 합니다.");
//					message = ediCommonService.getErrorCodeName(tempSearchData);
//					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//				}
				
				sendManifest.setOdJobNo(Util.null2str(tempHblRow.get("OD_JOB_NO")));
//				houseItemList = manifestMapper.selectCustomsManifestItem(sendManifest);
				
				String itemsInfo = Util.null2str(tempHblRow.get("itemsInfo"));
				if(!"".equals(itemsInfo)){
					org.json.JSONArray jArr = new org.json.JSONArray(itemsInfo.replace("\n", "").replace("&#x0D;", ""));
					houseItemList = (List<Map<String, Object>>)Util.JSONArrayToList(jArr);
				}else{
					houseItemList = null;
				}
				
				if(houseItemList != null) {
					if(houseItemList.size() > 50){
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "상세물품은 50건까지 입력 가능합니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					for(Map<String, Object> houseItem : houseItemList){
						if (Util.null2dbl(houseItem.get("PIECES")) == 0) {
							tempSearchData.put("srchCode", "");
							tempSearchData.put("message", "상세물품수량을 기재하기 바랍니다.");
							message = ediCommonService.getErrorCodeName(tempSearchData);
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}
						if ("".equals(Util.null2str(houseItem.get("DETAIL_ITEM_NAME")).trim())) {
							tempSearchData.put("srchCode", "");
							tempSearchData.put("message", "상세물품명을 기재하기 바랍니다.");
							message = ediCommonService.getErrorCodeName(tempSearchData);
							errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}
						
						// DKX목론신고 검증
                        if("Y".equals(Util.null2str(tempSearchData.get("dkxYn")))){

                            if ("".equals(Util.null2str(houseItem.get("ORDER_NO")).trim())) {
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "상품 주문번호를 기재하기 바랍니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }

                            if ("".equals(Util.null2str(houseItem.get("ITEM_NO")).trim())) {
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "상품 일련번호를 기재하기 바랍니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }

                            if ("".equals(Util.null2str(houseItem.get("CATEGORY_NAME_1")).trim())) {
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "상품 분류명 1단계를 기재하기 바랍니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }

                            if ("".equals(Util.null2str(houseItem.get("CATEGORY_NAME_2")).trim())) {
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "상품 분류명 2단계를 기재하기 바랍니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }

                            if ("".equals(Util.null2str(houseItem.get("CUSTOMS_TEMP_NO")).trim())) {
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "일회용인증번호를 기재하기 바랍니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }
                            
                            if ("".equals(Util.null2str(houseItem.get("CUSTOMS_ID")).trim())) {
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "개인통관고유부호를 기재하기 바랍니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }

//                            if ("".equals(Util.null2str(houseItem.get("ITEM_ID")).trim())) {
//                                tempSearchData.put("srchCode", "");
//                                tempSearchData.put("message", "상품아이디를 기재하기 바랍니다.");
//                                message = ediCommonService.getErrorCodeName(tempSearchData);
//                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//                            }

//                            if ("".equals(Util.null2str(houseItem.get("ITEM_MAF_NAME")).trim())) {
//                                tempSearchData.put("srchCode", "");
//                                tempSearchData.put("message", "상품 제조사명을 기재하기 바랍니다.");
//                                message = ediCommonService.getErrorCodeName(tempSearchData);
//                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//                            }

//                            if ("".equals(Util.null2str(houseItem.get("ITEM_MAF_MODEL_NO")).trim())) {
//                                tempSearchData.put("srchCode", "");
//                                tempSearchData.put("message", "상품 제조사 모델번호를 기재하기 바랍니다.");
//                                message = ediCommonService.getErrorCodeName(tempSearchData);
//                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//                            }

//                            if ("".equals(Util.null2str(houseItem.get("ITEM_BRAND_NAME")).trim())) {
//                                tempSearchData.put("srchCode", "");
//                                tempSearchData.put("message", "상품 브랜드명을 기재하기 바랍니다.");
//                                message = ediCommonService.getErrorCodeName(tempSearchData);
//                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//                            }

                            if ("".equals(Util.null2str(houseItem.get("HS_CODE")).trim())) {
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "상품 HS코드를 기재하기 바랍니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }

                            if (!"".equals(Util.null2str(houseItem.get("HS_CODE")).trim())) {
                            	if(Util.null2str(houseItem.get("HS_CODE")).length() < 6) {
                            		tempSearchData.put("srchCode", "");
                            		tempSearchData.put("message", "상품 HS코드는 6자리 입니다.");
                            		message = ediCommonService.getErrorCodeName(tempSearchData);
                            		errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            	}
                            }
                            
                            if ("".equals(Util.null2str(houseItem.get("ECM_URL")).trim())) {
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "상품 URL을 기재하기 바랍니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }

                            if ("".equals(Util.null2str(houseItem.get("SELLER_NAME")).trim())) {
                                tempSearchData.put("srchCode", "");
                                tempSearchData.put("message", "판매업체명을 기재하기 바랍니다.");
                                message = ediCommonService.getErrorCodeName(tempSearchData);
                                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                            }
                        }
					}
					tempHblRow.put("houseItemList", houseItemList);
				}
				
				
				/** 주문번호리스트조회 */
				searchOrder.setSrchOdJobNo(Util.null2str(tempHblRow.get("OD_JOB_NO")));
//				tempHblRow.put("poInfoList", orderMapper.selectPoInfo(searchOrder));
				String poInfo = Util.null2str(tempHblRow.get("poInfo"));
				if(!"".equals(poInfo)){
					org.json.JSONArray jArr = new org.json.JSONArray(poInfo.replace("\n", "").replace("&#x0D;", ""));
					tempHblRow.put("poInfoList", Util.JSONArrayToList(jArr));
				}else{
					tempHblRow.put("poInfoList", null);
				}
				
				/** DKX 주문번호리스트조회 */
				if("Y".equals(Util.null2str(tempSearchData.get("dkxYn")))){
					String orderInfo = Util.null2str(tempHblRow.get("orderInfo"));
					if(!"".equals(orderInfo)) {
						org.json.JSONArray jArr = new org.json.JSONArray(orderInfo.replace("\n", "").replace("&#x0D;", ""));
						tempHblRow.put("orderInfoList", Util.JSONArrayToList(jArr));
					}else {
						tempHblRow.put("orderInfoList", null);
					}
				}
				
				//2026-02-03 UPS일때 수입 목록신고 전에 관세청 화물진행정보 API를 통해 MRN,MSN 값을 리턴 받아서 
				//오더의 MRN,MSN 값을 비교한다. 
				//최대 10번을 체크해서 1번이라도 화물관리번호가 있거나 입력된 화물관리번호와 일치 하는 경우에는 오류검증에 걸리지 않도록 한다
				//매년 1월 1일 ~ 1월 5일은 이 로직이 실행 되지 않게 한다.
				if(!isBlockedPeriod && ("UPSXK".equals(masterOrder.get("ENT_CODE")) || "YHTXX".equals(masterOrder.get("ENT_CODE")))) {
					if(Util.null2str(masterOrder.get("BOUND_TYPE")).equals("IMP")){
						if(!"".equals(Util.null2str(tempHblRow.get("AR_DATE"))) && !"".equals(Util.null2str(tempHblRow.get("MRN"))) && !"".equals(Util.null2str(tempHblRow.get("MSN")))) {
							if(i<9 && (!mrnMsnNullFlag || !mrnMsnEqualFlag)) {
								String blYy = Util.dateStrToStr(Util.null2str(tempHblRow.get("AR_DATE")), "yyyy");
								String mrnMsn = ediCommonService.unipassGetMrnMsn(mblNo, hblNo, blYy);
								
								if(!"".equals(mrnMsn)) {
									mrnMsnNullFlag = true;
								}
								if(!"".equals(mrnMsn) && mrnMsn.length() == 15) {
									String uniMrn = mrnMsn.substring(0,11);
									String uniMsn = mrnMsn.substring(11,15); 
									if(uniMrn.equals(tempHblRow.get("MRN")) && uniMsn.equals(tempHblRow.get("MSN"))){
										mrnMsnEqualFlag = true;
									}
								}
							}
						}
					}
				}
			}
		}	// end of for-loop.
		
		//매년 1월 1일 ~ 1월 5일은 이 로직이 실행 되지 않게 한다.
		if(!isBlockedPeriod && ("UPSXK".equals(Util.null2str(masterOrder.get("ENT_CODE"))) || "YHTXX".equals(Util.null2str(masterOrder.get("ENT_CODE"))))) {
			if(Util.null2str(masterOrder.get("BOUND_TYPE")).equals("IMP")){
				if(!mrnMsnNullFlag) {
					tempSearchData.put("srchCode", "MF-139");	// 적하전송을 하지 않은 건 이므로 MRN, MSN 번호가 관세청에 등록되어 있지 않습니다. 확인 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : " + message));
				}
				
				if(!mrnMsnEqualFlag) {
					tempSearchData.put("srchCode", "MF-140");	// MRN, MSN 번호를 확인하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : " + message));
				}
			}
		}
		
		// 오류내역이 있을 경우
		if(errList.size() > 0 && Util.null2str(masterOrder.get("BOUND_TYPE")).equals("EXP")){
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("BOUND", masterOrder.get("BOUND"));
			ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
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
		if(attribute != null){
			ele.setAttribute(Util.null2str(attribute.get("attributeName")), Util.null2str(attribute.get("attributeContent")));
		}
		parent.appendChild(ele);
		return ele;
	}
	
	/**
	 * 수입목록취하 xml 생성
	 * @param sendManifest
	 * @param cusSendType
	 * @return
	 */
	public OrderSaveResult setCustomsManifestCancelImpData(SendManifest sendManifest, String cusSendType) throws Exception {
		int seq = 1;
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendManifest.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		String message = null;
		String bound = Util.null2str(sendManifest.getBound());
		Map<String, Object> masterOrder = null;
		Util.accessControlDatabase(sendManifest, "action", false, null, null, null, null);
		if(Util.null2str(sendManifest.getMasterOdJobNo()).equals("")){	/** 하우스그리드선택 */
			sendManifest.setOdJobNo(sendManifest.getHouseOdJobNoList().get(0));	/** 하우스관리번호로 마스터 조회 */
			masterOrder = manifestMapper.selectCustomsManifestCancelMaster(sendManifest);
		}else{
			/** 마스터그리드선택일경우 해당 마스터에 전송가능한 제출번호  */
			//서브그리드 선택한 경우 해당 마스터 + 선택한 제출번호 중 전송가능한 제출번호 가져옴
			masterOrder = manifestMapper.selectCustomsManifestCancelMaster(sendManifest);
		}
		
		if(masterOrder != null){
			if(Util.null2str(masterOrder.get("SIMPLE_DOC_NO_LIST")).equals("")){
				tempSearchData.put("srchCode", "MF-43");	// 해당 M.B/L에 H.B/L이 한 건도 없습니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : " + message));
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("BOUND", masterOrder.get("BOUND"));
				ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
				orderSaveResult.setResultData(result);
				orderSaveResult.setResultCode("ERROR");
				return orderSaveResult;
			}
			
			List<List<Map<String, Object>>> simpleDocNoHouseList = new ArrayList<List<Map<String, Object>>>();
			List<Map<String, Object>> tmpHouseOrderList = null;
			List<Map<String, Object>> tmpHouseOrderListSimpleDocNo = null;
			String simpleDocNo = null;
			String tmpStr, tmpStr2;
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", sendManifest.getEntCode());
			podMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
			podMap.put("solution", "ECM");
			

			int startNum, countCheck;
			sendManifest.setHouseCount(100);
			
			/** 데이터건수체크(두개의 브라우저에서 한번에 같은건을 전송했을경우 manifestMapper.updateCustomsManifest에서 상태업데이트 되면서 100건씩 조회하는데이터가 더이상 조회되지않고 종료됨) */
			sendManifest.setStartNum(999999);
			countCheck = Util.null2int(manifestMapper.selectCustomsManifestCancelHouseItem(sendManifest).get(0).get("COUNT_CHECK"));
			
			boolean chkData = true;
			startNum = 0;
			tmpHouseOrderListSimpleDocNo = new ArrayList<Map<String, Object>>();
			
			while(chkData){
				sendManifest.setStartNum(startNum);
				tmpHouseOrderList = manifestMapper.selectCustomsManifestCancelHouseItem(sendManifest);
				if(startNum != 0 && (tmpHouseOrderList == null || tmpHouseOrderList.size() == 0)){
					chkData = false;
					break;
				}
				this.getCustomsManifestCancelValidate(masterOrder, tmpHouseOrderList, tempSearchData, errList, result);
				startNum += 100;
				if(tmpHouseOrderList.size() != 100) {
					chkData = false;
				}
				tmpHouseOrderListSimpleDocNo.addAll(tmpHouseOrderList);
			}
			
			if(countCheck == 0 && tmpHouseOrderListSimpleDocNo.size() == 0) {
				errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : 전송할 데이터가 없습니다. 확인 후 처리 바랍니다."));
			}else if(countCheck == tmpHouseOrderListSimpleDocNo.size()){
				simpleDocNoHouseList.add(tmpHouseOrderListSimpleDocNo);
			}else{
				errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : 전송중 변경된 데이터가 있습니다. 확인 후 처리 바랍니다."));
			}
			
			if(errList.size() > 0 || Util.null2str(sendManifest.getPenaltyYn()).equals("Y")){
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("BOUND", masterOrder.get("BOUND"));
				ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
				orderSaveResult.setResultData(result);
				orderSaveResult.setResultCode("ERROR");
				return orderSaveResult;
			}
			
			if("errorCheck".equals(sendManifest.getAction())){
				orderSaveResult.setResultCode("errorCheck");
				return orderSaveResult;
			}
			
			podMap.put("userLanguage", sendManifest.getUserLanguage());
			podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
			podMap.put("srchCode", "ECM131");
			podMap.put("entCode", sendManifest.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", sendManifest.getGmtTime());
			podMap.put("updateUserId", sendManifest.getUpdateUserId());
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			podMap.put("podStatus", "ECM131");
			podMap.put("podTableName", 	"TB_ECM_POD_02");
			
			Map<String, Object> tmpMap = null;
			List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> upsList = new ArrayList<Map<String, Object>>();
			Map<String, Object> logMap = new HashMap<String, Object>();
			logMap.put("ent_code", sendManifest.getEntCode());
			logMap.put("customer_code", "");
			logMap.put("solution", "ECM");
			logMap.put("bound", bound);
			logMap.put("bound_type", "IMP");
			logMap.put("hbl_no", "");
			logMap.put("if_service_code", "");
			logMap.put("if_vender", "GOV");
			logMap.put("if_send_recv_type", "S");
			logMap.put("if_send_recv_method", "");
			logMap.put("if_status_info", "");
			logMap.put("if_sender_id", "");
			logMap.put("if_receiver_id", "");
			logMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
			logMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
			logMap.put("register_user_id", sendManifest.getRegisterUserId());
			logMap.put("register_user_name", sendManifest.getRegisterUserName());
			logMap.put("register_date_time", System.currentTimeMillis());
			logMap.put("if_doc_code", "CUSTOMS_CLEARANCE_DROP");
			logMap.put("if_doc_name", "통관목록취하");
			
			Map<String, Object> tmpInsertData = null;
			List<Map<String, Object>> insertGovSendList = new ArrayList<Map<String, Object>>();
			Map<String, Object> insertData = new HashMap<String, Object>();
			insertData.put("entCode", sendManifest.getEntCode());
			insertData.put("typeCode", "GOVCBRDL6");
			insertData.put("registerUserId", sendManifest.getRegisterUserId());
			insertData.put("gmtTime", sendManifest.getGmtTime());
			insertData.put("sendStatus", "N");
			insertData.put("entBusinessCode", "");
			insertData.put("updateUserId", sendManifest.getRegisterUserId());
			insertData.put("solution", "ECM");
			insertData.put("stockType", "EDI");
			insertData.put("stockSign", "EDI");
			insertData.put("stockSeqLength", "8");
			insertData.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			insertData.put("stockRemark", "sendCustomsManifestImp(GOVCBRDL6)");
			insertData.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));
			
			List<SendManifest> updateCustomsManifestList = new ArrayList<SendManifest>();
			SendManifest tmpSendManifest = null;
			
			List<Map<String, Object>> insertOrderPodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpPodData = null;
			
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			JSONObject tmpJsonObject = null;
			
			String today = DateTime.getToday();
			int xmlByte = 0;
			int houseCnt = 0;
			
			for(List<Map<String, Object>> houseOrderList : simpleDocNoHouseList){
				
				simpleDocNo = Util.null2str(houseOrderList.get(0).get("SIMPLE_DOC_NO"));
				houseCnt += houseOrderList.size();
				
				tmpSendManifest = new SendManifest();
				tmpSendManifest.setPodStatus("ECM131");
				tmpSendManifest.setGmtTime(sendManifest.getGmtTime());
				tmpSendManifest.setSimpleDocDate(today);
				tmpSendManifest.setMasterOdJobNo(sendManifest.getMasterOdJobNo());
				tmpSendManifest.setHouseOdJobNoList(sendManifest.getHouseOdJobNoList());
				tmpSendManifest.setSimpleDocNo(simpleDocNo);
				tmpSendManifest.setSimpleDropCode(sendManifest.getSimpleDropCode());
				tmpSendManifest.setSimpleDropCnt(sendManifest.getSimpleDropCnt());
				tmpSendManifest.setSimpleDropEtc(sendManifest.getSimpleDropEtc());
				tmpSendManifest.setSimpleDropTime(sendManifest.getSimpleDropTime());
				
				updateCustomsManifestList.add(tmpSendManifest);
				
				List<Map<String, Object>> houseItemList = null;
				List<String> poInfoList = null;
						
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
				Element lv5 = null;
				Map<String, String> attribute = new HashMap<String, String>();
				
				document = documentBuilder.newDocument();
				document.setXmlVersion("1.0");
				document.setXmlStandalone(true);
				root = document.createElement("wco:Declaration");
				
				root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_DL6SchemaModule:1:0");
				root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_DL6SchemaModule:1:0");
				root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_DL6SchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfSPC_DL6SchemaModule_1.0_standard.xsd");
				document.appendChild(root);
				
				/** 제출세관부호 */
				this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(masterOrder.get("CUSTOMS_CODE")).trim());
				/** 제출번호 */
				this.createElement(document, root, lv1, "wco:ID", simpleDocNo);
				/** 제출일자(CCYYMMDD) */
				this.createElement(document, root, lv1, "wco:IssueDateTime", today.replace("-", ""));
				/** 제출세관과코드 */
				this.createElement(document, root, lv1, "SubsequentDeclarationOfficeID", Util.null2str(masterOrder.get("CUSTOMS_DPT_CODE")).trim());
				/** 문서형태구분 */
				this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBRDL6");
				/** 취하신청차수 */
				this.createElement(document, root, lv1, "wco:VersionID", Util.null2str(sendManifest.getSimpleDropCnt()));
				/** 취하신청구분코드 */
				this.createElement(document, root, lv1, "wco:SubTypeCode", "B");
				lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
				/** wco:AdditionalDocument 시작 */
				lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalDocument", null);
				/** 적하목록 관리 번호 */
				this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(masterOrder.get("MRN")).trim().toUpperCase());
				/** wco:TransportContractDocument 시작 */
				lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
				
				/** Master B/L번호 */
				if(bound.equals("AIR")){
					this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.getSubStringK(Util.null2str(masterOrder.get("MBL_NO")).trim(), 0, 11));
				}else{
					this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.GetBLNo_ZSeqDelete(Util.null2str(masterOrder.get("MBL_NO")).trim().toUpperCase()));
				}
				/** Master B/L 일련번호 */
				this.createElement(document, lv2, lv3, "wco:MasterBLSequenceID", Util.null2str(masterOrder.get("MSN")).trim().toUpperCase());
				/** wco:TransportContractDocument 끝 */
				
				int hblSeq = 1;
				for(Map<String, Object> houseOrder : houseOrderList){
					/** wco:HouseConsignment 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:HouseConsignment", null);
					
					/** 취하사유코드 */
					this.createElement(document, lv2, lv3, "wco:RequestOverrideCode", Util.null2str(sendManifest.getSimpleDropCode()));
					/** 취하사유상세기타내용 */
					tmpStr = Util.null2str(sendManifest.getSimpleDropEtc());
					if(!"".equals(tmpStr)) {
						this.createElement(document, lv2, lv3, "wco:StatementDescription", tmpStr);
					}
					lv3 = this.createElement(document, lv2, lv3, "wco:TransportContractDocument", null);
					/** House B/L 번호 */
					this.createElement(document, lv3, lv4, "wco:ID", ediCommonService.DeleteCharString_Loop(Util.null2str(houseOrder.get("HBL_NO")).trim().toUpperCase(), " |-", "|"));
					/** House B/L 일련번호 */
					this.createElement(document, lv3, lv4, "wco:HouseBLSequenceID", Util.null2str(houseOrder.get("HSN")));
					/** wco:HouseConsignment 끝 */
					
					
					tmpPodData = new HashMap<String, Object>();
					tmpPodData.put("odJobNo", houseOrder.get("OD_JOB_NO"));
					tmpPodData.put("blNo", houseOrder.get("HBL_NO"));
					if(houseOrder.get("ECM134_POD_JOB_NO") != null) {
						tmpPodData.put("ECM134_POD_JOB_NO", houseOrder.get("ECM134_POD_JOB_NO"));
					}
					if(houseOrder.get("ECM131_POD_JOB_NO") != null){
						tmpPodData.put("ECM131_POD_JOB_NO", houseOrder.get("ECM131_POD_JOB_NO"));
					}else{
						tmpPodData.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					}
					insertOrderPodList.add(tmpPodData);
				}
				
				out = new ByteArrayOutputStream();
				source = new DOMSource(document);
				streamResult = new StreamResult(out);
				transformer.transform(source, streamResult);
				
				if("REST".equals(cusSendType)){
					tmpJsonObject = new JSONObject();
					String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
					String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
					String filePath = customProperties.getBackupDir() + "edi/customs/" + sendManifest.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
					String fileName = dataId + "GOVCBRDL6.xml";
					FileUtil.fileWrite(xml, filePath, fileName);
					
					tmpJsonObject.put("customsReportNo", simpleDocNo);
					tmpJsonObject.put("xml", xml);
					tmpJsonObject.put("filePath", filePath);
					tmpJsonObject.put("fileName", fileName);
					jsonArray.add(tmpJsonObject);
					
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("if_file_path", filePath);
					tmpMap.put("if_file_name", fileName);
//					tmpMap.put("if_doc_reference_no", simpleDocNo);
					tmpMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
					tmpMap.put("es_job_no", dataId);
					logList.add(tmpMap);
					
					xmlByte += xml.getBytes().length;
				}else{
					tmpInsertData = new HashMap<String, Object>();
					tmpInsertData.put("ediDesc", out.toByteArray());
					tmpInsertData.put("declarationId", simpleDocNo);
					insertGovSendList.add(tmpInsertData);
					
				}
				out.close();
			}
			
			tmpPodData = new HashMap<String, Object>();
			tmpPodData.put("odJobNo", masterOrder.get("OD_JOB_NO"));
			tmpPodData.put("blNo", masterOrder.get("MBL_NO"));
			if(masterOrder.get("ECM134_POD_JOB_NO") != null){
				tmpPodData.put("ECM134_POD_JOB_NO", masterOrder.get("ECM134_POD_JOB_NO"));
			}
			if(masterOrder.get("ECM131_POD_JOB_NO") != null){
				tmpPodData.put("ECM131_POD_JOB_NO", masterOrder.get("ECM131_POD_JOB_NO"));
			}else{
				tmpPodData.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			}
			insertOrderPodList.add(tmpPodData);
			
			result.put("masterOdJobNo", masterOrder.get("OD_JOB_NO"));
			result.put("masterOrder", masterOrder);
			
			result.put("podMap", podMap);
			result.put("insertOrderPodList", insertOrderPodList);
			result.put("updateCustomsManifestList", updateCustomsManifestList);
			
			if("REST".equals(cusSendType)){
				jsonObject.put("dataList", jsonArray);
				result.put("REST", jsonObject);
				result.put("logList", logList);
				if("UPSXK".equals(sendManifest.getEntCode()) && "AIR".equals(sendManifest.getBound())) {
					result.put("upsList", upsList);
				}
				result.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));
				if(xmlByte > 1024 * 1024 * 19){
					result.put("sendType", "FILE");
				}
				
				/** 건수가 많을경우 전송시간이 오래 걸려서 전송전에 상태변경/로그저장함  */
				for(SendManifest updateSendManifest : updateCustomsManifestList){
					manifestMapper.updateCustomsManifestCancel(updateSendManifest);
					manifestMapper.updateSimpleCancelErrorDescNull(updateSendManifest);
				}
				
				IndexApi indexApi = new IndexApi(client);
				DocumentApi documentApi = null;
				String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				documentApi = new DocumentApi(client);
				
				logMap.put("if_status", "READY");
				logMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
				logMap.put("if_remark", "M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "(" + houseCnt + "건)");
				logMap.put("es_job_no", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "99999");
				
				documentApi.createDocument(index, Util.null2str(logMap.get("es_job_no")), logMap);
				logMap.put("if_status", "SEND");
				logMap.put("if_remark", "");
				logMap.remove("es_job_no");
				
				result.put("logMap", logMap);
			}else{
				result.put("DB", insertGovSendList);
				result.put("insertGovSendMap", insertData);
			}
			
		}else{
			orderSaveResult.setResultCode("ERROR");
			result.put("processCode", "0000");
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "Master Order 자료가 존재하지 않습니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			result.put("processMessage", message);
		}
		
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}

	/**
	 * 목록취하 데이터 검증
	 * @param masterOrder
	 * @param houseOrder
	 * @param tempSearchData
	 * @param errList
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean getCustomsManifestCancelValidate(Map<String, Object> masterOrder, List<Map<String, Object>> houseOrder, Map<String, Object> tempSearchData, List<Map<String, Object>> errList, Map<String, Object> result
		) throws java.lang.Exception {

		String message = null;
        Map<String, Object> tempHblRow = null;
        Map<String, Object> tmpMap = new HashMap<String, Object>();
        SendManifest sendManifest = new SendManifest();
		
        String mblNo = Util.null2str(masterOrder.get("MBL_NO"));
        String hblNo = null;
        String hsn = null;
		String bound = Util.null2str(masterOrder.get("BOUND"));
		
		if (houseOrder.size() <= 0) {
			tempSearchData.put("srchCode", "MF-43");	// 해당 M.B/L에 통관목로신고 가능한 H.B/L이 한 건도 없습니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : " + message));
		}
		
		if ("".equals(Util.null2str(masterOrder.get("MBL_NO")).trim())) {
			tempSearchData.put("srchCode", "MF-70");	// M.B/L 번호를 기재 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("MRN")).trim()) && !(Util.null2str(masterOrder.get("BOUND")).equals("AIR") && Util.null2str(masterOrder.get("BOUND_TYPE")).equals("EXP"))) {
			tempSearchData.put("srchCode", "MF-65");	//적하목록관리번호(MRN)를 기재 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}

		if ("".equals(Util.null2str(masterOrder.get("CUSTOMS_CODE")).trim())) {
			tempSearchData.put("srchCode", "MF-58");	//세관(관세청) 등록부호를 기재하기 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("CUSTOMS_DPT_CODE")).trim())) {
			tempSearchData.put("srchCode", "MF-58");	//세관(관세청) 등록부호를 기재하기 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		/** 전송시 일자를 제출일자로 사용 */
//			if ("".equals(Util.null2str(masterOrder.get("SIMPLE_DOC_DATE")).trim())) {
//				tempSearchData.put("srchCode", "");
//				tempSearchData.put("message", "제출일자를 기재하기 바랍니다.");
//				message = ediCommonService.getErrorCodeName(tempSearchData);
//				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
//			}
		if ("".equals(Util.null2str(masterOrder.get("MSN")).trim())) {
			tempSearchData.put("srchCode", "MF-111");	//MSN를 기재 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		/** 관세청사서함 */
		if(Util.null2str(masterOrder.get("EDI_PORTAL_MAILBOX_ID")).equals("")){
			tmpMap.put("srchEntCode", masterOrder.get("ENT_CODE"));
			tmpMap.put("srchEntBusinessCode", masterOrder.get("ENT_BUSINESS_CODE"));
			tmpMap.put("srchBound", masterOrder.get("BOUND"));
			tmpMap.put("srchSeqNo", masterOrder.get("EDI_MAILBOX_SEQ_NO"));
			if(Util.null2str(masterOrder.get("BOUND_TYPE")).equals("EXP")){
				tmpMap.put("srchEdiCityPort", masterOrder.get("ORIG_CODE"));
			}else{
				tmpMap.put("srchEdiCityPort", masterOrder.get("DEST_PORT"));
			}
			Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiEcmInfo(tmpMap);
			if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
				masterOrder.put("EDI_PORTAL_MAILBOX_ID", busEdiEcmMap.get("EDI_PORTAL_MAILBOX_ID"));
			}else{
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "관세청사서함을 설정 후 전송하십시오.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
		}
		
		
		String nowDate = null;
		if("Y".equals(masterOrder.get("updateSimpleDocDateYn"))){
			nowDate = DateTimeUtil.getNow("yyyy-MM-dd");
		}
		
		for (int i = 0; i < houseOrder.size(); ++i) {
			tempHblRow = houseOrder.get(i);
			hblNo = Util.null2str(tempHblRow.get("HBL_NO")).trim();
			hsn = Util.null2str(tempHblRow.get("HSN")).trim();
			
				if(nowDate != null && !Util.null2str(tempHblRow.get("SIMPLE_DOC_DATE")).equals(nowDate)){
					errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : 신고일자가 잘못 입력 되어 있습니다."));
					nowDate = null;
				}
				
				if (Util.null2int(Util.null2str(tempHblRow.get("LAST_POD_STATUS")).substring(3)) < 120) {
					tempSearchData.put("srchCode", "MF-148");	//목록접수 이전 상태인 데이터는 목록취하전송 불가능합니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
				
				sendManifest.setOdJobNo(Util.null2str(tempHblRow.get("OD_JOB_NO")));
				
		}	// end of for-loop.
		
		return true;
	
	}
	
	/**
	 * 목록취하 데이터 전송 및 상태처리
	 * @param sendManifest
	 * @param cusSendType
	 * @param orderSaveResult
	 * @throws Exception
	 */
	public void setCustomsManifestCancelImp(SendManifest sendManifest, String cusSendType,OrderSaveResult orderSaveResult) throws Exception {
		
		Map<String, Object> resultData = orderSaveResult.getResultData();
		
		SqlSession sqlSession = null;
		int cnt = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
			String customsErrCode = "";
			boolean success = true;	// 목록전송성공여부
			String token = null;
			
			if("REST".equals(cusSendType)){
				JSONObject jsonObject = (JSONObject)resultData.get("REST");
				String sendType = Util.null2str(resultData.get("sendType"), "LIST");
				jsonObject = ediCommonService.sendEbms(Util.null2str(resultData.get("ediPortalMailboxId")), "GOVCBRDL6", jsonObject, sendType);
				
				String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
				ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
				ArrayList<BulkData> bulkList2 = new ArrayList<BulkData>();	// UPS bulkList
				BulkData bulkData = null;
				int seq = 1;
				Map<String, Object> logMap = (Map)resultData.get("logMap");
				logMap.put("customs_err_desc", jsonObject.get("customsErrDesc"));
				logMap.put("customs_err_desc_additional", jsonObject.get("body"));
				customsErrCode = Util.null2str(jsonObject.get("customsErrCode"));
				if(!customsErrCode.equals("") && !customsErrCode.equals("C200")) {
					success = false; // 취하전송실패
				}
				
				List<Map<String, Object>> logList = (List)resultData.get("logList");
				for(Map<String, Object> logData : logList){
					logData.putAll(logMap);
					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(Util.null2str(logData.get("es_job_no")));
					bulkData.setJsonMap(logData);
					bulkList.add(bulkData);
				}
				
				if (bulkList != null && bulkList.size() > 0) {
					IndexApi indexApi = new IndexApi(client);
					if (!indexApi.existIndex(index))	indexApi.createIndex(index);
					
					DocumentApi documentApi = new DocumentApi(client);
					documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
				}
				
			}else{
				Map<String, Object> insertGovSendMap = (Map)resultData.get("insertGovSendMap");
				List<Map<String, Object>> insertGovSendList = (List)resultData.get("DB");
				for(Map<String, Object> insertGovSendData : insertGovSendList){
					insertGovSendMap.putAll(insertGovSendData);
					ediCommonService.insertGovSend(commonBatchMapper, insertGovSendMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}
			
			Map<String, Object> masterLastPodMap = new HashMap<String, Object>();
			if(!success) {
				masterLastPodMap.put("lastPodStatus", "ECM134");
			}else {
				masterLastPodMap.put("lastPodStatus", "ECM131");
			}
			masterLastPodMap.put("gmtTime", sendManifest.getGmtTime());
			masterLastPodMap.put("updateType", "odJobNo");
			masterLastPodMap.put("odJobNo", resultData.get("masterOdJobNo"));
			commonMapper.updateLastPodStatus(masterLastPodMap);
			
			cnt = 1;
			List<SendManifest> updateCustomsManifestList = (List)resultData.get("updateCustomsManifestList");
			for(SendManifest updateSendManifest : updateCustomsManifestList){
				if(!success) {
					updateSendManifest.setPodStatus("ECM134");
				}
				updateSendManifest.setAction(cusSendType);
				updateSendManifest.setCustomsErrCode(customsErrCode);
				manifestBatchMapper.updateCustomsManifestCancel(updateSendManifest);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			cnt = 1;
			Map<String, Object> podMap = (Map)resultData.get("podMap");
			List<Map<String, Object>> insertOrderPodList = (List)resultData.get("insertOrderPodList");
			List<Map<String, Object>> updateOrderPodList = new ArrayList<Map<String, Object>>();
			if(!success) {
				podMap.put("podStatus", "ECM134");
			}
			
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
			for(Map<String, Object> insertOrderPodMap : insertOrderPodList){
				if((success && insertOrderPodMap.get("ECM131_POD_JOB_NO") != null) || 	// 전송 성공했고 목록전송	POD가 이미 있는 경우 또는
					(!success && insertOrderPodMap.get("ECM134_POD_JOB_NO") != null)	// 전송 실패했고 목록전송실패	POD가 이미 있는 경우
				){
					updateOrderPodList.add(insertOrderPodMap);
				}else{
					podMap.putAll(insertOrderPodMap);
					orderBatchMapper.insertOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}
			cnt = 1;
			for(Map<String, Object> updateOrderPodMap : updateOrderPodList){
				if(!success) {
					updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("ECM134_POD_JOB_NO"));
				}else {
					updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("ECM131_POD_JOB_NO"));
				}
				podMap.putAll(updateOrderPodMap);
				orderBatchMapper.updateOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			/** 통관목록취하전송시 취하오류통보&취하전송실패POD삭제 */
			if(success) {
				cnt = 1;
				for(Map<String, Object> deleteOrderPodMap : insertOrderPodList){
					podMap.putAll(deleteOrderPodMap);
					podMap.put("podStatus", "ECM135");
					orderBatchMapper.deleteOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
					
					podMap.put("podStatus", "ECM134");
					orderBatchMapper.deleteOrderPod(podMap);
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
	
	/**
	 * 취하이력삭제 (하우스 체크건의 DROP_YN을 N으로 변경)
	 * @param sendManifest
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult deleteDropHist(SendManifest sendManifest) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		manifestMapper.deleteDropHist(sendManifest);
		return orderSaveResult;
	}
	
	/**
	 * 목록취하 초기화
	 * @param sendManifest
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult resetSendCustomsManifestCancel(SendManifest sendManifest) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> tmpMap;
		Map<String, Object> podMap = new HashMap<String, Object>();
		SqlSession sqlSession = null;
		int cnt = 1;
		
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
			
			manifestMapper.deleteResetSendCustomsManifestCancel(sendManifest);
			List<Map<String, Object>> resetDataList = manifestMapper.selectResetSendCustomsManifestCancel(sendManifest); 
			for(Map<String, Object> resetData : resetDataList){
				tmpMap = commonMapper.selectManifestLastPodInfo(resetData);
				if(tmpMap != null && tmpMap.size() > 0){
					if("H".equals(resetData.get("BL_TYPE_MH"))){
						podMap.put("lastPodStatus", tmpMap.get("POD_STATUS"));
						podMap.put("lastPodDate", tmpMap.get("POD_DATE_TIME"));
					}else{
						podMap.put("lastPodStatusMaster", tmpMap.get("POD_STATUS"));
						podMap.put("lastPodDateMaster", tmpMap.get("POD_DATE_TIME"));
					}
				}
				
				tmpMap = BeanUtils.describe(sendManifest);
				tmpMap.put("odJobNo", resetData.get("OD_JOB_NO"));
				tmpMap.putAll(podMap);
				manifestBatchMapper.updateResetSendCustomsManifestCancelInfo(tmpMap);
				if( (cnt++ % 300) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
				
			}
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
			tmpMap = BeanUtils.describe(sendManifest);
			tmpMap.putAll(podMap);
			tmpMap.put("houseOdJobNoList", sendManifest.getHouseOdJobNoList());
			tmpMap.put("simpleDocNoList", sendManifest.getSimpleDocNoList());
	//		manifestMapper.updateResetSendCustomsManifest(tmpMap);
			if("resetSendCustomsManifest".equals(sendManifest.getAction())){
				manifestMapper.updateResetSendCustomsManifest2(tmpMap);
			}
		}catch(Exception e) {
			throw e;
		}finally {
			sqlSession.close();
		}
		
		return orderSaveResult;
	}
	
	 public OrderSaveResult setDkxCustomsManifestImpData(SendManifest sendManifest, String cusSendType) throws Exception {
	        int seq = 1;
	        OrderSaveResult orderSaveResult = new OrderSaveResult();
	        Map<String, Object> result = new HashMap<String, Object>();
	        List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
	        Map<String, Object> tempSearchData = new HashMap<String, Object>();
	        tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
	        tempSearchData.put("entCode", Util.null2str(sendManifest.getEntCode()));
	        tempSearchData.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
	        tempSearchData.put("dkxYn", "Y");
	        String message = null;
	        String bound = Util.null2str(sendManifest.getBound());
	        Map<String, Object> masterOrder = null;
	        Util.accessControlDatabase(sendManifest, "action", false, null, null, null, null);
	        if(Util.null2str(sendManifest.getMasterOdJobNo()).equals("")){	/** 하우스그리드선택 */
	            sendManifest.setOdJobNo(sendManifest.getHouseOdJobNoList().get(0));	/** 하우스관리번호로 마스터 조회 */
	            masterOrder = manifestMapper.selectCustomsManifestMaster(sendManifest);
	            if(sendManifest.getHouseOdJobNoList().size() != Util.null2int(masterOrder.get("SIMPLE_DOC_NO_CNT"))){	/** 해당 제출번호의 데이터건수와 그리드선택건수가 같지않음 */
	                result.put("processMessage", "제출번호가 같지만 선택되지않은 오더가 있습니다.");
	                orderSaveResult.setResultData(result);
	                orderSaveResult.setResultCode("ERROR");
	                return orderSaveResult;
	            }
	        }else{
	            /** 마스터그리드선택일경우 해당 마스터에 전송가능한 제출번호  */
	            //서브그리드 선택한 경우 해당 마스터 + 선택한 제출번호 중 전송가능한 제출번호 가져옴
	            masterOrder = manifestMapper.selectCustomsManifestMaster(sendManifest);
	        }

	        if (masterOrder != null) {
	            if (Util.null2str(masterOrder.get("SIMPLE_DOC_NO_LIST")).equals("")) {
	                tempSearchData.put("srchCode", "MF-43");    // 해당 M.B/L에 H.B/L이 한 건도 없습니다.
	                message = ediCommonService.getErrorCodeName(tempSearchData);
	                errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : " + message));
	                Map<String, Object> tmpMap = new HashMap<String, Object>();
	                tmpMap.put("BOUND", masterOrder.get("BOUND"));
	                ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
	                orderSaveResult.setResultData(result);
	                orderSaveResult.setResultCode("ERROR");
	                return orderSaveResult;
	            }
	            String[] simpleDocStrList = Util.null2str(masterOrder.get("SIMPLE_DOC_NO_LIST")).split(",");    /** 해당마스터에 전송해야할 제출번호리스트 */
	            List<List<Map<String, Object>>> simpleDocNoHouseList = new ArrayList<List<Map<String, Object>>>();
	            List<Map<String, Object>> tmpHouseOrderList = null;
	            List<Map<String, Object>> tmpHouseOrderListSimpleDocNo = null;
	            String simpleDocNo = null;
	            String tmpStr, tmpStr2;

	            Map<String, Object> podMap = new HashMap<String, Object>();
	            podMap.put("entCode", sendManifest.getEntCode());
	            podMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
	            podMap.put("solution", "ECM");
	            podMap.put("optionCode", "C04");
	            String consigneeNameType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
	            podMap.put("optionCode", "C05");
	            String consigneeAddressType = Util.null2str(commonMapper.selectSystemOption(podMap), "ENG");
	            podMap.put("optionCode", "C09");
	            String updateSimpleDocDateYn = Util.null2str(commonMapper.selectSystemOption(podMap), "N");
	            masterOrder.put("updateSimpleDocDateYn", updateSimpleDocDateYn);

	            int startNum, countCheck;
	            sendManifest.setHouseCount(100);
	            for (String simpleDocStr : simpleDocStrList) {
	                sendManifest.setSimpleDocNo(simpleDocStr);

	                /** 데이터건수체크(두개의 브라우저에서 한번에 같은건을 전송했을경우 manifestMapper.updateCustomsManifest에서 상태업데이트 되면서 100건씩 조회하는데이터가 더이상 조회되지않고 종료됨) */
	                sendManifest.setStartNum(999999);
	                countCheck = Util.null2int(manifestMapper.selectCustomsManifestHouseItem(sendManifest).get(0).get("COUNT_CHECK"));

	                boolean chkData = true;
	                startNum = 0;
	                tmpHouseOrderListSimpleDocNo = new ArrayList<Map<String, Object>>();

	                while (chkData) {
	                    sendManifest.setStartNum(startNum);
	                    tmpHouseOrderList = manifestMapper.selectCustomsManifestHouseItem(sendManifest);
	                    if (startNum != 0 && (tmpHouseOrderList == null || tmpHouseOrderList.size() == 0)) {
	                        chkData = false;
	                        break;
	                    }
	                    this.getCustomsManifestValidate(masterOrder, tmpHouseOrderList, tempSearchData, errList, result, consigneeNameType, consigneeAddressType);
	                    startNum += 100;
	                    if (tmpHouseOrderList.size() != 100) {
	                        chkData = false;
	                    }
	                    tmpHouseOrderListSimpleDocNo.addAll(tmpHouseOrderList);
	                }

	                if (countCheck == tmpHouseOrderListSimpleDocNo.size()) {
	                    simpleDocNoHouseList.add(tmpHouseOrderListSimpleDocNo);
	                } else {
	                    errList.add(ediCommonService.getErrorMessage(Util.null2str(masterOrder.get("MBL_NO")), "M.B/L : 전송중 변경된 데이터가 있습니다. 확인 후 처리 바랍니다."));
	                }
	            }

	            if (errList.size() > 0 || Util.null2str(sendManifest.getPenaltyYn()).equals("Y")) {
	                Map<String, Object> tmpMap = new HashMap<String, Object>();
	                tmpMap.put("BOUND", masterOrder.get("BOUND"));
	                ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
	                orderSaveResult.setResultData(result);
	                orderSaveResult.setResultCode("ERROR");
	                return orderSaveResult;
	            }

	            if ("errorCheck".equals(sendManifest.getAction())) {
	                orderSaveResult.setResultCode("errorCheck");
	                return orderSaveResult;
	            }

	            podMap.put("userLanguage", sendManifest.getUserLanguage());
	            podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
	            podMap.put("srchCode", "ECM110");
	            podMap.put("entCode", sendManifest.getEntCode());
	            podMap.put("entBusinessCode", "");
	            podMap.put("gmtTime", sendManifest.getGmtTime());
	            podMap.put("updateUserId", sendManifest.getUpdateUserId());
	            podMap.put("stockType", "POD");
	            podMap.put("stockSign", "POD");
	            podMap.put("stockSeqLength", 8);
	            podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
	            podMap.put("stockRemark", "ECM POD JOB NO");
	            podMap.put("podStatus", "ECM110");
	            podMap.put("podTableName", "TB_ECM_POD_02");


	            Map<String, Object> tmpMap = null;
	            List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
	            List<Map<String, Object>> upsList = new ArrayList<Map<String, Object>>();
	            Map<String, Object> logMap = new HashMap<String, Object>();
	            logMap.put("ent_code", sendManifest.getEntCode());
	            logMap.put("customer_code", "");
	            logMap.put("solution", "ECM");
	            logMap.put("bound", bound);
	            logMap.put("bound_type", "IMP");
	            logMap.put("hbl_no", "");
	            logMap.put("if_service_code", "");
	            logMap.put("if_vender", "GOV");
	            logMap.put("if_send_recv_type", "S");
	            logMap.put("if_send_recv_method", "");
	            logMap.put("if_status_info", "");
	            logMap.put("if_sender_id", "");
	            logMap.put("if_receiver_id", "");
	            logMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
	            logMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
	            logMap.put("register_user_id", sendManifest.getRegisterUserId());
	            logMap.put("register_user_name", sendManifest.getRegisterUserName());
	            logMap.put("register_date_time", System.currentTimeMillis());
	            logMap.put("if_doc_code", "CUSTOMS_CLEARANCE");
	            logMap.put("if_doc_name", "통관목록DKX");

	            Map<String, Object> tmpInsertData = null;
	            List<Map<String, Object>> insertGovSendList = new ArrayList<Map<String, Object>>();
	            Map<String, Object> insertData = new HashMap<String, Object>();
	            insertData.put("entCode", sendManifest.getEntCode());
	            insertData.put("typeCode", "GOVCBR936");
	            insertData.put("registerUserId", sendManifest.getRegisterUserId());
	            insertData.put("gmtTime", sendManifest.getGmtTime());
	            insertData.put("sendStatus", "N");
	            insertData.put("entBusinessCode", "");
	            insertData.put("updateUserId", sendManifest.getRegisterUserId());
	            insertData.put("solution", "ECM");
	            insertData.put("stockType", "EDI");
	            insertData.put("stockSign", "EDI");
	            insertData.put("stockSeqLength", "8");
	            insertData.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
	            insertData.put("stockRemark", "sendCustomsManifestImp(GOVCBR936)");
	            insertData.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));

	            List<SendManifest> updateCustomsManifestList = new ArrayList<SendManifest>();
	            SendManifest tmpSendManifest = null;

	            List<Map<String, Object>> insertOrderPodList = new ArrayList<Map<String, Object>>();
	            Map<String, Object> tmpPodData = null;

	            JSONObject jsonObject = new JSONObject();
	            JSONArray jsonArray = new JSONArray();
	            JSONObject tmpJsonObject = null;

	            String today = DateTime.getToday();
	            int xmlByte = 0;
	            int houseCnt = 0;
	            for (List<Map<String, Object>> houseOrderList : simpleDocNoHouseList) {
	                simpleDocNo = Util.null2str(houseOrderList.get(0).get("SIMPLE_DOC_NO"));
	                houseCnt += houseOrderList.size();

	                tmpSendManifest = new SendManifest();
	                tmpSendManifest.setPodStatus("ECM110");
	                tmpSendManifest.setGmtTime(sendManifest.getGmtTime());
	                tmpSendManifest.setSimpleDocDate(today);
	                tmpSendManifest.setMasterOdJobNo(sendManifest.getMasterOdJobNo());
	                tmpSendManifest.setHouseOdJobNoList(sendManifest.getHouseOdJobNoList());
	                tmpSendManifest.setSimpleDocNo(simpleDocNo);
	                updateCustomsManifestList.add(tmpSendManifest);

	                List<Map<String, Object>> houseItemList = null;
	                List<Map<String, Object>> orderInfoList = null;
	                List<String> poInfoList = null;

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
	                Element lv5 = null;
	                Element lv6 = null;
	                Map<String, String> attribute = new HashMap<String, String>();

	                document = documentBuilder.newDocument();
	                document.setXmlVersion("1.0");
	                document.setXmlStandalone(true);
	                root = document.createElement("wco:Declaration");


	                root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_DKXSchemaModule:1:0");
	                root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_DKXSchemaModule:1:0");
	                root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	                root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_DKXSchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfSPC_DKXSchemaModule_1.0_standard.xsd");
	                document.appendChild(root);

	                /** 제출세관/과 */
	                this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(masterOrder.get("CUSTOMS_CODE")).trim() + Util.null2str(masterOrder.get("CUSTOMS_DPT_CODE")).trim());
	                /** 제출번호 */
	                this.createElement(document, root, lv1, "wco:ID", simpleDocNo);
	                /** 제출일자(CCYYMMDD) */
	                this.createElement(document, root, lv1, "wco:IssueDateTime", today.replace("-", ""));
	                /** 통관목록 화물건수 */
	                this.createElement(document, root, lv1, "wco:LoadingListQuantity", Util.null2str(houseOrderList.size()));
	                /** 문서형태구분 */
	                this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBRDKX");
	                /** 적하목록 관리 번호 */
	                lv1 = this.createElement(document, root, lv1, "wco:AdditionalDocument", null);
	                this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("MRN")).trim().toUpperCase());

	                //				/** wco:AdditionalInformation(확인자) 시작 */
//					lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
//					/** 목록선별자/확인자/X-RAY판독자 */
//					this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("CONFIRMER_USER_NAME")).trim());
//					/** 1:목록선별자, 2: 확인자, 3:판독자1, 4: 판독자2, 5:판독자3, 6:판독자4 */
//					this.createElement(document, lv1, lv2, "wco:StatementCode", "2");
//					/** wco:AdditionalInformation(확인자) 끝 */
	                lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
	                this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("SELECTOR_USER_NAME")).trim());
	                this.createElement(document, lv1, lv2, "wco:StatementCode", "1");
	                lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
	                this.createElement(document, lv1, lv2, "wco:Content", Util.null2str(masterOrder.get("CONFIRMER_USER_NAME")).trim());
	                this.createElement(document, lv1, lv2, "wco:StatementCode", "2");

	                /** wco:BorderTransportMeans 시작 */
	                lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
	                /** 입항일자(CCYYMMDD) */
	                this.createElement(document, lv1, lv2, "wco:ArrivalDateTime", Util.null2str(masterOrder.get("AR_DATE")).trim().replace("-", ""));
	                /** 항공편명/항차 */
	                if(bound.equals("AIR")){
//							this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("LINE_CODE")).trim() + Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim());
	                    this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim());	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
	                    this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "40");
	                }else{
	                    this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.null2str(masterOrder.get("VOYAGE")).trim());
	                    this.createElement(document, lv1, lv2, "kcs:ModeTypeCode", "10");
	                }
	                /** wco:BorderTransportMeans 끝 */

	                String consigneeAddress = null;
//					String strpCustomsNumber_A = null;
	                String customsIdType = null;
	                int itemSeq = 0;
	                int poInfoSeq = 0;
	                int orderSeq = 0;

	                for(Map<String, Object> houseOrder : houseOrderList){

	                    if(consigneeAddressType.equals("ENG")){
	                        if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")).trim())){
	                            consigneeAddress = ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 0, 150);
	                        }else{
	                            consigneeAddress = ediCommonService.getSubStringK(ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS_LOC")), false), 0, 150);
	                        }
	                    }else{
	                        if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS_LOC")).trim())){
	                            consigneeAddress = ediCommonService.getSubStringK(ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS_LOC")), false), 0, 150);
	                        }else{
	                            consigneeAddress = ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 0, 150);
	                        }
	                    }

	                    /** wco:Consignment 시작 */
	                    lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
	                    /** 검사요청대상 */
	                    this.createElement(document, lv1, lv2, "wco:GoodsStatusCode", Util.null2str(houseOrder.get("INSPECTION_YN"), "N"));
	                    /** 총중량 */
	                    this.createElement(document, lv1, lv2, "wco:GrossVolumeMeasure", Util.null2str(houseOrder.get("GWT")));
	                    /** 포장개수 */
	                    this.createElement(document, lv1, lv2, "wco:TotalPackageQuantity", Util.null2str(houseOrder.get("PIECES")));
	                    /** 물품가격(미화) */
	                    attribute.put("attributeName", "currencyID");
	                    attribute.put("attributeContent", "USD");
	                    this.createElement(document, lv1, lv2, "wco:ValueAmount",String.format("%.2f",Util.null2dbl(houseOrder.get("AMOUNT_USD"))), attribute);

//	                    /** 수하인 식별부호 */
//	                    /** 2024.09.11 - Z(데이터 없음)일 때는 항목 전송하지 않음 */
//	                    customsIdType = Util.null2str(houseOrder.get("CUSTOMS_ID_TYPE"));
//	                    if(!Util.isEmpty2(customsIdType) && !"Z".equals(customsIdType)){
//	                        this.createElement(document, lv1, lv2, "kcs:TypeID", customsIdType);
//	                    }

//						strpCustomsNumber_A = dsSub.Tables[0].Rows[nRowIndex]["pCustomsNumber"].ToString().Trim().ToUpper().Replace("-", "").Replace("/", "").Replace(".", "").Replace(" ", "");
//						if(Util.null2str(houseOrder.get("TEDAI_HBL.CustomsUseType")).equals("2")){
//							if(strpCustomsNumber_A.length() == 10){
//								this.createElement(document, lv1, lv2, "kcs:TypeID", "D");	/** 사업자등록번호 */
//							}else if (strpCustomsNumber_A.length() > 10 && ediCommonService.getDeleteCharStringManifest(strpCustomsNumber_A) != strpCustomsNumber_A){	//EtcLibrary.DeleteAlphabetChar(strpCustomsNumber_A) != strpCustomsNumber_A
//								this.createElement(document, lv1, lv2, "kcs:TypeID", "C");	/** 통관고유부호 */
//							}else{
//								this.createElement(document, lv1, lv2, "kcs:TypeID", "");
//							}
//						}else{
//							if(ediCommonService.getDeleteCharStringManifest(strpCustomsNumber_A) == strpCustomsNumber_A && strpCustomsNumber_A.length() == 8 && (ediCommonService.getSubStringK(strpCustomsNumber_A, 0, 2) == "19" || ediCommonService.getSubStringK(strpCustomsNumber_A, 0, 2) == "20")){	//EtcLibrary.DeleteAlphabetChar(strpCustomsNumber_A) == strpCustomsNumber_A && strpCustomsNumber_A.Length == 8 && (EtcLibrary.getSubStringK(strpCustomsNumber_A,0,2) == "19" || EtcLibrary.getSubStringK(strpCustomsNumber_A,0,2) == "20")
//								this.createElement(document, lv1, lv2, "kcs:TypeID", "B");	/** 생년월일 */
//							}else{
//								if (ediCommonService.getDeleteCharStringManifest(strpCustomsNumber_A) == strpCustomsNumber_A && strpCustomsNumber_A.length() == 13){	//EtcLibrary.DeleteAlphabetChar(strpCustomsNumber_A) == strpCustomsNumber_A && strpCustomsNumber_A.Length == 13
//									this.createElement(document, lv1, lv2, "kcs:TypeID", "E");	/** 외국인등록번호 */
//								}else{
//									if (ediCommonService.getSubStringK(strpCustomsNumber_A, 0, 1).toUpperCase() == "P" && ediCommonService.getDeleteCharStringManifest(strpCustomsNumber_A).length() == 12){	//EtcLibrary.getSubStringK(strpCustomsNumber_A, 0, 1).ToUpper() == "P" && EtcLibrary.DeleteAlphabetChar(strpCustomsNumber_A).Length == 12
//										this.createElement(document, lv1, lv2, "kcs:TypeID", "A");	/** 개인통관고유부호 */
//									}else{
//										this.createElement(document, lv1, lv2, "kcs:TypeID", "F");	/** 여권번호 */
//									}
//								}
//							}
//						}

//	                    /** 상세물품 수 */
//	                    this.createElement(document, lv1, lv2, "kcs:DetailCountQuantity", Util.null2str(houseOrder.get("ITEM_CNT")));
	                    /** 내포장개수 */
	                    this.createElement(document, lv1, lv2, "kcs:InternalPackageQuantity", Util.null2str(houseOrder.get("PIECES_REAL")));

	                    /** 특별통관대상업체지정번호 */
//						lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalDocument", null);
//						this.createElement(document, lv2, lv3, "wco:ID", "");	//공백처리

//	                    /** wco:AdditionalInformation 시작 */
//	                    lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalInformation", null);
//	                    /** House Air Way Bill 품명 */
//	                    this.createElement(document, lv2, lv3, "wco:Content", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("COMMODITY_ITEM")).trim(), "1"));
//	                    /** 용도구분(1:개인,2:회사) */
//	                    this.createElement(document, lv2, lv3, "wco:StatementCode", Util.null2str(houseOrder.get("CD_USE_TYPE")));
//	                    /** 허용품목코드(1 : 화장지, 주방용기류 (48류), 2 : 서적, 인쇄물류 (49류), 3 : 의류 (61~63류), 4 : 신발류 (64류), 5 : 가구/조명기구류 (94류), 6 : 음악/영화용 CD (85류), 7 : 허용 비대상 품목) */
//	                    this.createElement(document, lv2, lv3, "wco:StatementTypeCode", Util.null2str(houseOrder.get("ALLOWED_ITEM_CODE")).trim());
//	                    /** wco:AdditionalInformation 끝 */

	                    /** wco:Agent 시작 */
	                    lv2 = this.createElement(document, lv1, lv2, "wco:Agent", null);
	                    /** 화물운송주선업자부호 */
	                    tmpStr = Util.null2str(masterOrder.get("B2G_FORWARDER_SIGN")).trim().toUpperCase();
	                    if(!Util.isEmpty2(tmpStr)){
	                        this.createElement(document, lv2, lv3, "wco:ID", tmpStr);
	                    }
//	                    /** 거래유형코드(A:전자상거래물품, D:개인 일반물품, E:상용견품, F:상업서류) */
//	                    this.createElement(document, lv2, lv3, "kcs:TypeOfBusiness", Util.null2str(houseOrder.get("ECM_TYPE")));
	                    /** 국내배송업체명 */
	                    this.createElement(document, lv2, lv3, "wco:Name", Util.null2str(houseOrder.get("DELIVERY_NAME")));
	                    /** wco:Agent 끝 */

//						if(Util.null2str(masterOrder.get("B2G_FORWARDER_SIGN")).equals("")){
//							/** 상업서류송달업체부호 */
//							lv2 = this.createElement(document, lv1, lv2, "wco:Carrier", null);
//							this.createElement(document, lv2, lv3, "wco:ID", "");	//2021-09-02 해당값 전송하지않음
//						}


	                    /** wco:Buyer 시작 */
	                    tmpStr = Util.null2str(houseOrder.get("ORDER_ACCOUNT"));
	                    tmpStr2 = Util.null2str(houseOrder.get("ORDER_TEL"));
	                    lv2 = this.createElement(document, lv1, lv2, "wco:Buyer", null);
	                    /** 주문자 계정 */
	                    if(!Util.isEmpty2(tmpStr)) {
	                        this.createElement(document, lv2, lv3, "wco:ID", tmpStr);
	                    }
	                    /** 주문자 성명 */
	                    this.createElement(document, lv2, lv3, "wco:Name", Util.null2str(houseOrder.get("ORDER_NAME")));
	                    /** 주문자 전화번호 **/
	                    if(!Util.isEmpty2(tmpStr2)) {
	                        lv3 = this.createElement(document, lv2, lv3, "wco:Communication", null);
	                        this.createElement(document, lv3, lv4, "wco:ID", tmpStr2);
	                    }
	                    /** wco:Buyer 끝 */

	                    /** wco:Carrier 시작 */
	                    tmpStr = Util.null2str(houseOrder.get("FORWARD_AGENT_CODE"));
	                    tmpStr2 = Util.null2str(houseOrder.get("FORWARD_AGENT_NAME"));
	                    /** 전자상거래 유형이 C일때 배송대행업체 필수 나머지는 선택 **/
	                    if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)) {
	                        lv2 = this.createElement(document, lv1, lv2, "wco:Carrier", null);
	                        /** 배송대행업체부호 */
	                        if(!Util.isEmpty2(tmpStr)) {
	                            this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(houseOrder.get("FORWARD_AGENT_CODE")));
	                        }
	                        /** 배송대행업체명 */
	                        if(!Util.isEmpty2(tmpStr2)) {
	                            this.createElement(document, lv2, lv3, "wco:Name", Util.null2str(houseOrder.get("FORWARD_AGENT_NAME")));
	                        }
	                    }
	                    /** wco:Carrier 끝 */

//	                    /** wco:Consignee 시작 */
//	                    tmpStr = Util.null2str(houseOrder.get("SELLER_REG_NO"));
//	                    tmpStr2 = Util.null2str(houseOrder.get("SELLER_NAME"));
//	                    if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)){
//	                        lv2 = this.createElement(document, lv1, lv2, "wco:Consignee", null);
//	                        /** 해외판매자부호 */
//	                        if(!Util.isEmpty2(tmpStr)){
//	                            this.createElement(document, lv2, lv3, "wco:ID", tmpStr);
//	                        }
//	                        /** 해외판매자명 */
//	                        if(!Util.isEmpty2(tmpStr2)){
//	                            this.createElement(document, lv2, lv3, "wco:Name", tmpStr2);
//	                        }
//	                    }
//	                    /** wco:Consignee 끝 */

	                    /** wco:ConsignmentItem 시작 */
	                    lv2 = this.createElement(document, lv1, lv2, "wco:ConsignmentItem", null);

	                    /** wco:AdditionalInformation 시작 */
	                    tmpStr = Util.null2str(houseOrder.get("URL")).trim();
	                    tmpStr2 = Util.null2str(houseOrder.get("ECM_BUY_TYPE"));
	                    lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
	                    /** 전자상거래사이트주소 */
	                    if(!Util.isEmpty2(tmpStr)){
	                        this.createElement(document, lv3, lv4, "wco:Content", ediCommonService.getSubStringK(tmpStr, 0, 300));
	                    }
	                    /** 전자상거래유형코드 */
	                    this.createElement(document, lv3, lv4, "wco:StatementTypeCode", tmpStr2);
	                    /** wco:AdditionalInformation 끝 */

	                    /** wco:Agent 시작 */
	                    tmpStr = Util.null2str(houseOrder.get("AGENT_REG_NO"));
	                    tmpStr2 = Util.null2str(houseOrder.get("AGENT_NAME"));
	                    if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)){
	                        /** 전자상거래 유형이 B일때 구매대행업자 필수 나머지는 선택 **/
	                        lv3 = this.createElement(document, lv2, lv3, "wco:Agent", null);
	                        /** 구매대행업자부호 */
	                        if(!Util.isEmpty2(tmpStr)){
	                            this.createElement(document, lv3, lv4, "wco:ID", tmpStr);
	                        }
	                        /** 구매대행업자명 */
	                        if(!Util.isEmpty2(tmpStr2)){
	                            this.createElement(document, lv3, lv4, "wco:Name", tmpStr2);
	                        }
	                    }
	                    /** wco:Agent 끝 */

	                    /** wco:Buyer 시작 */
	                    tmpStr = Util.null2str(houseOrder.get("SALES_BROKER_REG_NO"));
	                    tmpStr2 = Util.null2str(houseOrder.get("SALES_BROKER_NAME"));
	                    if(!Util.isEmpty2(tmpStr) || !Util.isEmpty2(tmpStr2)) {
	                        /** 전자상거래 유형이 A일때 구매대행업자 필수 나머지는 선택 **/
	                        lv3 = this.createElement(document, lv2, lv3, "wco:Buyer", null);
	                        /** 판매중개자부호 */
	                        this.createElement(document, lv3, lv4, "wco:ID", tmpStr);
	                        /** 판매중개자명 */
	                        this.createElement(document, lv3, lv4, "wco:Name", tmpStr2);
	                    }
	                    /** wco:Buyer 끝 */

	                    /** wco:Consignee 시작 */
	                    lv3 = this.createElement(document, lv2, lv3, "wco:Consignee", null);
	                    /** 수하인 성명 */
	                    if(consigneeNameType.equals("ENG")){
	                        if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim())){
	                            this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim(), "1"));
	                        }else{
	                            this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_NAME_LOC")).trim(), false));
	                        }
	                    }else{
	                        if(!"".equals(Util.null2str(houseOrder.get("CONSIGNEE_NAME_LOC")).trim())){
	                            this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getRemove2bitChar(Util.null2str(houseOrder.get("CONSIGNEE_NAME_LOC")).trim(), false));
	                        }else{
	                            this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).trim(), "1"));
	                        }
	                    }

	                    /** wco:Address 시작 */
	                    lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
	                    /** 수하인 상세주소 */
	                    this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(consigneeAddress, 150, 300));
	                    /** 수하인 우편번호 */
	                    /** 우편번호값 없을경우 00000으로 전송되지 않도록 수정    0으로 시작하는 우편번호에 대한 처리 무시 */
//						this.createElement(document, lv4, lv5, "wco:PostcodeID", ediCommonService.getSubStringK(Util.getFileWriteData(Util.null2str(houseOrder.get("CONSIGNEE_ZIPCODE")).replace(" ", "").replace("-", ""), "Number", "5"), 0, 6));	//0으로 시작하는 우편번호일 때 4자리만 저장된 경우 있어서 수정, 맨 앞자리 0추가 ex.5750 -> 05750
	                    tmpStr = Util.null2str(houseOrder.get("CONSIGNEE_ZIPCODE")).replace(" ", "").replace("-", "");
	                    if(!Util.isEmpty2(tmpStr)){
	                        this.createElement(document, lv4, lv5, "wco:PostcodeID", ediCommonService.getSubStringK(tmpStr, 0, 6));
	                    }
	                    /** 수하인 기본주소 */
	                    this.createElement(document, lv4, lv5, "kcs:Description", ediCommonService.getSubStringK(consigneeAddress, 0, 150));
	                    /** wco:Address 끝 */

	                    /** 수하인 전화번호 */
	                    lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
	                    this.createElement(document, lv4, lv5, "wco:ID", ediCommonService.EDI_Numeric_Select(Util.null2str(houseOrder.get("CONSIGNEE_TEL"))));
	                    /** wco:Consignee 끝 */

	                    /** wco:Consignor 시작 */
	                    lv3 = this.createElement(document, lv2, lv3, "wco:Consignor", null);
	                    /** 송하인 성명 */
	                    this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_NAME")), "1"));

	                    /** wco:Address 시작 */
	                    lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
	                    /** 송하인 주소 */
	                    tmpStr = ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")), "1");
	                    if(!Util.isEmpty2(tmpStr)){
	                        this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(tmpStr, 0, 150));
	                    }
	                    /** 발송국가 */
	                    this.createElement(document, lv4, lv5, "wco:CountryCode", Util.null2str(houseOrder.get("SHIPPER_COUNTRY_CODE")).trim());
	                    /** wco:Address 끝 */

	                    /** 송하인 전화번호 */
	                    tmpStr = ediCommonService.EDI_Numeric_Select(Util.null2str(houseOrder.get("SHIPPER_TEL")));
	                    if(!Util.isEmpty2(tmpStr)){
	                        lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
	                        this.createElement(document, lv4, lv5, "wco:ID", tmpStr);
	                    }
	                    /** wco:Consignor 끝 */

	                    sendManifest.setOdJobNo(Util.null2str(houseOrder.get("OD_JOB_NO")));
	                    houseItemList = (List) houseOrder.get("houseItemList");
	                    attribute.put("attributeName", "kcsUnitCode");
	                    if(houseItemList == null || houseItemList.size() == 0){
	                        /** wco:Commodity 시작 */
	                        lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
	                        /** 일련번호 */
	                        this.createElement(document, lv3, lv4, "wco:SequenceNumeric", "1");
	                        /** 품명 */
	                        this.createElement(document, lv3, lv4, "wco:CargoDescription", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseOrder.get("COMMODITY_ITEM")), "1"));
	                        /** 수량(단위) */
	                        attribute.put("attributeContent", "EA");
	                        this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(houseOrder.get("PIECES")), attribute);
	                        /** 규격 */
//							this.createElement(document, lv3, lv4, "wco:Description", "");
	                        /** 금액 */
	                        if(Util.null2dbl(houseOrder.get("AMOUNT_USD")) > 0){
	                            this.createElement(document, lv3, lv4, "wco:ValueAmount",  String.format("%.2f",Util.null2dbl(houseOrder.get("AMOUNT_USD"))));
	                        }
	                        /** wco:Commodity 끝 */
	                    }else{
	                        itemSeq = 1;
	                        for(Map<String, Object> houseItem : houseItemList){
	                            /** wco:ElectronicCommerceCommodity 시작 */
	                            lv3 = this.createElement(document, lv2, lv3, "wco:ElectronicCommerceCommodity", null);
	                            /** 상품 일련번호 */
	                            this.createElement(document, lv3, lv4, "wco:SequenceNumeric", Util.null2str(houseItem.get("ITEM_NO")));	//Convert.ToInt16(i + 1)

	                            /** wco:AdditionalInformation 시작 */
	                            lv4 = this.createElement(document, lv3, lv4, "wco:AdditionalInformation", null);
	                            /** 상품판매 페이지 주소 */
	                            String ecmUrl = Util.null2str(houseItem.get("ECM_URL")).replace("&amp;", "&");
	                            this.createElement(document, lv4, lv5, "wco:Content", ecmUrl);
	                            /** 선택옵션명 */
	                            this.createElement(document, lv4, lv5, "wco:StatementDescription", Util.null2str(houseItem.get("OPTION_NAME")));
	                            /** 주문번호 */
	                            this.createElement(document, lv4, lv5, "kcs:OrderID", Util.null2str(houseItem.get("ORDER_NO")));
	                            tmpStr = Util.null2str(houseItem.get("SELLER_ID"));
	                            if(!"".equals(tmpStr)){
	                                /** 판매업체 아이디 */
	                                this.createElement(document, lv4, lv5, "wco:SellerID", Util.null2str(houseItem.get("SELLER_ID")));
	                            }
	                            /** wco:AdditionalInformation 끝 */

	                            /** wco:DetailedCommodity 시작 */
	                            lv4 = this.createElement(document, lv3, lv4, "wco:DetailedCommodity", null);
	                            /** 일련번호 */
	                            this.createElement(document, lv4, lv5, "wco:SequenceNumeric", Util.null2str(itemSeq++));
	                            tmpStr = Util.null2str(houseItem.get("BRAND"));
	                            if(!"".equals(tmpStr)) {
	                                /** 규격명 */
	                                this.createElement(document, lv4, lv5, "wco:CargoDescription", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseItem.get("BRAND")), "1"));
	                            }
	                            /** 수량(단위) */
	                            attribute.put("attributeContent", Util.null2str(houseItem.get("UNIT_PIECES"), "EA"));
	                            this.createElement(document, lv4, lv5, "wco:CountQuantity", Util.null2str(houseItem.get("PIECES")), attribute);
	                            /** 상품명 */
	                            this.createElement(document, lv4, lv5, "wco:Description", ediCommonService.GetReplaceETCCharToSpace_A(Util.null2str(houseItem.get("DETAIL_ITEM_NAME")), "1"));
	                            /** 상품 제조사 모델 번호 */
	                            this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(houseItem.get("ITEM_MAF_MODEL_NO")));
	                            /** 상품 아이디 */
	                            this.createElement(document, lv4, lv5, "wco:IdentityQualifierCode", Util.null2str(houseItem.get("ITEM_ID")));
	                            /** 상품 브랜드명 */
	                            this.createElement(document, lv4, lv5, "wco:Name", Util.null2str(houseItem.get("ITEM_BRAND_NAME")));
	                            /** 금액 */
	                            this.createElement(document, lv4, lv5, "wco:ValueAmount", String.format("%.2f",Util.null2dbl(houseItem.get("AMOUNT"))));
	                            /** 상품단가 */
	                            this.createElement(document, lv4, lv5, "wco:UnitPriceAmount", String.format("%.2f",Util.null2dbl(houseItem.get("UNIT_PRICE"))));

	                            lv5 = this.createElement(document, lv4, lv5, "wco:AdditionalInformation", null);
	                            /** 상품 분류명 1단계 코드 */
	                            this.createElement(document, lv5, lv6, "wco:StatementCode", "1");
	                            this.createElement(document, lv5, lv6, "wco:StatementDescription", Util.null2str(houseItem.get("CATEGORY_NAME_1")));

	                            lv5 = this.createElement(document, lv4, lv5, "wco:AdditionalInformation", null);
	                            /** 상품 분류명 2단계 코드 */
	                            this.createElement(document, lv5, lv6, "wco:StatementCode", "2");
	                            this.createElement(document, lv5, lv6, "wco:StatementDescription", Util.null2str(houseItem.get("CATEGORY_NAME_2")));

	                            tmpStr = Util.null2str(houseItem.get("CATEGORY_NAME_3"));
	                            if(!"".equals(tmpStr)){
	                                lv5 = this.createElement(document, lv4, lv5, "wco:AdditionalInformation", null);
	                                /** 상품 분류명 3단계 코드 */
	                                this.createElement(document, lv5, lv6, "wco:StatementCode", "3");
	                                this.createElement(document, lv5, lv6, "wco:StatementDescription", Util.null2str(houseItem.get("CATEGORY_NAME_3")));
	                            }

	                            tmpStr = Util.null2str(houseItem.get("CATEGORY_NAME_4"));
	                            if(!"".equals(tmpStr)){
	                                lv5 = this.createElement(document, lv4, lv5, "wco:AdditionalInformation", null);
	                                /** 상품 분류명 4단계 코드 */
	                                this.createElement(document, lv5, lv6, "wco:StatementCode", "4");
	                                this.createElement(document, lv5, lv6, "wco:StatementDescription", Util.null2str(houseItem.get("CATEGORY_NAME_4")));
	                            }

	                            tmpStr = Util.null2str(houseItem.get("CATEGORY_NAME_5"));
	                            if(!"".equals(tmpStr)){
	                                lv5 = this.createElement(document, lv4, lv5, "wco:AdditionalInformation", null);
	                                /** 상품 분류명 5단계 코드 */
	                                this.createElement(document, lv5, lv6, "wco:StatementCode", "5");
	                                this.createElement(document, lv5, lv6, "wco:StatementDescription", Util.null2str(houseItem.get("CATEGORY_NAME_5")));
	                            }

	                            tmpStr = Util.null2str(houseItem.get("CATEGORY_NAME_6"));
	                            if(!"".equals(tmpStr)){
	                                lv5 = this.createElement(document, lv4, lv5, "wco:AdditionalInformation", null);
	                                /** 상품 분류명 6단계 코드 */
	                                this.createElement(document, lv5, lv6, "wco:StatementCode", "6");
	                                this.createElement(document, lv5, lv6, "wco:StatementDescription", Util.null2str(houseItem.get("CATEGORY_NAME_6")));
	                            }

	                            tmpStr = Util.null2str(houseItem.get("CATEGORY_NAME_7"));
	                            if(!"".equals(tmpStr)){
	                                lv5 = this.createElement(document, lv4, lv5, "wco:AdditionalInformation", null);
	                                /** 상품 분류명 7단계 코드 */
	                                this.createElement(document, lv5, lv6, "wco:StatementCode", "7");
	                                this.createElement(document, lv5, lv6, "wco:StatementDescription", Util.null2str(houseItem.get("CATEGORY_NAME_7")));
	                            }

	                            tmpStr = Util.null2str(houseItem.get("CATEGORY_NAME_8"));
	                            if(!"".equals(tmpStr)){
	                                lv5 = this.createElement(document, lv4, lv5, "wco:AdditionalInformation", null);
	                                /** 상품 분류명 8단계 코드 */
	                                this.createElement(document, lv5, lv6, "wco:StatementCode", "8");
	                                this.createElement(document, lv5, lv6, "wco:StatementDescription", Util.null2str(houseItem.get("CATEGORY_NAME_8")));
	                            }

	                            tmpStr = Util.null2str(houseItem.get("CATEGORY_NAME_9"));
	                            if(!"".equals(tmpStr)){
	                                lv5 = this.createElement(document, lv4, lv5, "wco:AdditionalInformation", null);
	                                /** 상품 분류명 9단계 코드 */
	                                this.createElement(document, lv5, lv6, "wco:StatementCode", "9");
	                                this.createElement(document, lv5, lv6, "wco:StatementDescription", Util.null2str(houseItem.get("CATEGORY_NAME_9")));
	                            }

	                            /** wco:Classification 시작 */
	                            lv5 = this.createElement(document, lv4, lv5, "wco:Classification", null);
	                            /** 상세물품 HS코드 */
	                            this.createElement(document, lv5, lv6, "wco:ID", Util.null2str(houseItem.get("HS_CODE")).length() > 6 ? Util.null2str(houseItem.get("HS_CODE")).substring(0,6) : Util.null2str(houseItem.get("HS_CODE")));
	                            /** wco:Classification 끝 */
	                            /** wco:DetailedCommodity 끝 */

	                            /** wco:Seller 시작 */
	                            lv4 = this.createElement(document, lv3, lv4, "wco:Seller", null);
	                            tmpStr = Util.null2str(houseItem.get("SELLER_CODE"));
	                            if(!"".equals(tmpStr)) {
	                                /** 판매업체 부호 */
	                                this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(houseItem.get("SELLER_CODE")));
	                            }
	                            tmpStr = Util.null2str(houseItem.get("SELLER_NAME"));
	                            if(!"".equals(tmpStr)){
	                                /** 판매업체 상호 */
	                                this.createElement(document, lv4, lv5, "wco:Name", Util.null2str(houseItem.get("SELLER_NAME")));
	                            }
	                            /** wco:Seller 끝 */
	                            
	                            /** wco:Manufacturer 시작 */
	                            lv4 = this.createElement(document, lv3, lv4, "wco:Manufacturer", null);
	                            /** 상품 제조사명 */
	                            this.createElement(document, lv4, lv5, "wco:Name", Util.null2str(itemSeq));
	                            /** wco:Manufacturer 끝 */

	                            /** wco:ElectronicCommerceCommodity 끝 */
	                        }
	                    }
	                    /** wco:ConsignmentItem 끝 */

	                    /** wco:PreviousDocument 시작 */
	                    orderSeq = 1;
	                    orderInfoList = (List) houseOrder.get("orderInfoList");
	                    for(Map<String, Object> houseItem : orderInfoList){
	                        lv2 = this.createElement(document, lv1, lv2, "wco:PreviousDocument", null);
	                        /** 주문번호 */
	                        this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(houseItem.get("ORDER_NO")));
	                        /** 주문번호 일련번호 */
	                        this.createElement(document, lv2, lv3, "kcs:SequenceNumeric", Util.null2str(orderSeq++));
	                        /** wco:AdditionalInformation 시작 */
	                        lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
	                        /** 개인통관고유부호 */
	                        this.createElement(document, lv3, lv4, "kcs:Content", Util.null2str(houseItem.get("CUSTOMS_ID")));
	                        /** 일회용인증번호 */
	                        tmpStr = !"".equals(Util.null2str(houseItem.get("CUSTOMS_TEMP_NO"))) ? Util.null2str(houseItem.get("CUSTOMS_TEMP_NO")) : Util.null2str(houseOrder.get("CUSTOMS_ID"));
	                        this.createElement(document, lv3, lv4, "kcs:StatementDescription", Util.null2str(houseItem.get("CUSTOMS_TEMP_NO")));
	                        /** 주문완료일시 */
	                        this.createElement(document, lv3, lv4, "kcs:OrderCompletionDateTime", Util.null2str(houseItem.get("ORDER_END_DATETIME")));
	                        /** wco:AdditionalInformation 끝 */
	                    }
	                    /** wco:PreviousDocument 끝 */

//	                    poInfoList = (List) houseOrder.get("poInfoList");
//	                    if(poInfoList != null && poInfoList.size() > 0){
//	                        poInfoSeq = 1;
//	                        for(String poNo : poInfoList){
//	                            /** wco:PreviousDocument 시작 */
//	                            lv2 = this.createElement(document, lv1, lv2, "wco:PreviousDocument", null);
//	                            /** 주문번호 */
//	                            this.createElement(document, lv2, lv3, "wco:ID", poNo);
//	                            /** 일련번호 */
//	                            this.createElement(document, lv2, lv3, "kcs:SequenceNumeric", Util.null2str(poInfoSeq++));
//	                            /** wco:AdditionalInformation 시작 */
//	                            lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
//	                            /** 개인통관고유부호 */
//	                            this.createElement(document, lv2, lv3, "kcs:Content", Util.null2str(houseOrder.get("CUSTOMS_ID")));
//	                            /** 일회용인증번호 */
//	                            this.createElement(document, lv2, lv3, "kcs:StatementDescription", Util.null2str(houseOrder.get("ONE_TIME_CODE")));
//	                            /** 주문완료일시 */
//	                            this.createElement(document, lv2, lv3, "kcs:OrderCompletionDateTime", Util.null2str(houseOrder.get("ORDER_DATE")));
//	                            /** wco:AdditionalInformation 끝 */
//	                            /** wco:PreviousDocument 끝 */
//	                        }
//	                    }

	                    /** wco:TransportContractDocument 시작 */
	                    lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
	                    /** House B/L 번호 */
	                    this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.DeleteCharString_Loop(Util.null2str(houseOrder.get("HBL_NO")).trim().toUpperCase(), " |-", "|"));
	                    /** House B/L 일련번호 */
	                    this.createElement(document, lv2, lv3, "kcs:HouseBLSequenceID", Util.null2str(houseOrder.get("HSN")).trim());
	                    /** wco:TransportContractDocument 끝 */
	                    /** wco:Consignment 끝 */

	                    tmpPodData = new HashMap<String, Object>();
	                    tmpPodData.put("odJobNo", houseOrder.get("OD_JOB_NO"));
	                    tmpPodData.put("blNo", houseOrder.get("HBL_NO"));
	                    if(houseOrder.get("ECM111_POD_JOB_NO") != null) {
	                        tmpPodData.put("ECM111_POD_JOB_NO", houseOrder.get("ECM111_POD_JOB_NO"));
	                    }
	                    if(houseOrder.get("ECM110_POD_JOB_NO") != null){
	                        tmpPodData.put("ECM110_POD_JOB_NO", houseOrder.get("ECM110_POD_JOB_NO"));
	                    }else{
	                        tmpPodData.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
	                    }
	                    insertOrderPodList.add(tmpPodData);
	                }

	                /** wco:GoodsShipment 시작 */
	                lv1 = this.createElement(document, root, lv1, "wco:GoodsShipment", null);
	                /** 보세구역부호 */
	                lv2 = this.createElement(document, lv1, lv2, "wco:Warehouse", null);
	                if(Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE")).trim().length() == 8){
	                    this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE")).trim());
	                }else{
	                    this.createElement(document, lv2, lv3, "wco:ID", ediCommonService.getSubStringK(Util.null2str(masterOrder.get("ASSIGNMENT_WH_CODE_ETC")).trim(), 0, 8));
	                }
	                /** wco:GoodsShipment 끝 */

	                /** 적출국(항) */
	                lv1 = this.createElement(document, root, lv1, "wco:LoadingLocation", null);
	                this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("ORIG_CODE")).trim());

	                /** wco:TransportContractDocument 시작 */
	                lv1 = this.createElement(document, root, lv1, "wco:TransportContractDocument", null);
	                /** Master B/L번호 */
	                if(bound.equals("AIR")){
	                    this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.getSubStringK(Util.null2str(masterOrder.get("MBL_NO")).trim(), 0, 11));
	                }else{
	                    this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.GetBLNo_ZSeqDelete(Util.null2str(masterOrder.get("MBL_NO")).trim().toUpperCase()));
	                }
	                /** Master B/L 일련번호 */
	                this.createElement(document, lv1, lv2, "kcs:MasterBLSequenceID", Util.null2str(masterOrder.get("MSN")));
	                /** wco:TransportContractDocument 끝 */

	                out = new ByteArrayOutputStream();
	                source = new DOMSource(document);
	                streamResult = new StreamResult(out);
	                transformer.transform(source, streamResult);

	                tmpJsonObject = new JSONObject();
	                String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
	                String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
	                String filePath = customProperties.getBackupDir() + "edi/customs/" + sendManifest.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
	                String fileName = dataId + "GOVCBRDKX.xml";
	                FileUtil.fileWrite(xml, filePath, fileName);

	                tmpJsonObject.put("customsReportNo", simpleDocNo);
	                tmpJsonObject.put("xml", xml);
	                tmpJsonObject.put("filePath", filePath);
	                tmpJsonObject.put("fileName", fileName);
	                jsonArray.add(tmpJsonObject);

	                tmpMap = new HashMap<String, Object>();
	                tmpMap.put("if_file_path", filePath);
	                tmpMap.put("if_file_name", fileName);
	                tmpMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
	                tmpMap.put("es_job_no", dataId);
	                logList.add(tmpMap);

	                xmlByte += xml.getBytes().length;
	                out.close();

	            }

	            tmpPodData = new HashMap<String, Object>();
				tmpPodData.put("odJobNo", masterOrder.get("OD_JOB_NO"));
				tmpPodData.put("blNo", masterOrder.get("MBL_NO"));
				if(masterOrder.get("ECM111_POD_JOB_NO") != null){
					tmpPodData.put("ECM111_POD_JOB_NO", masterOrder.get("ECM111_POD_JOB_NO"));
				}
				if(masterOrder.get("ECM110_POD_JOB_NO") != null){
					tmpPodData.put("ECM110_POD_JOB_NO", masterOrder.get("ECM110_POD_JOB_NO"));
				}else{
					tmpPodData.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				}
				insertOrderPodList.add(tmpPodData);
				
				result.put("masterOdJobNo", masterOrder.get("OD_JOB_NO"));
				result.put("masterOrder", masterOrder);
				
				result.put("podMap", podMap);
				result.put("insertOrderPodList", insertOrderPodList);
				result.put("updateCustomsManifestList", updateCustomsManifestList);
				
				if("REST".equals(cusSendType)){
					jsonObject.put("dataList", jsonArray);
					result.put("REST", jsonObject);
					result.put("logList", logList);
					if("UPSXK".equals(sendManifest.getEntCode()) && "AIR".equals(sendManifest.getBound())) {
						result.put("upsList", upsList);
					}
					result.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));
					if(xmlByte > 1024 * 1024 * 19){
						result.put("sendType", "FILE");
					}
					
					/** 건수가 많을경우 전송시간이 오래 걸려서 전송전에 상태변경/로그저장함  */
					for(SendManifest updateSendManifest : updateCustomsManifestList){
						manifestMapper.updateCustomsManifest(updateSendManifest);
						manifestMapper.updateSimpleErrorDescNull(updateSendManifest);
					}
					
					IndexApi indexApi = new IndexApi(client);
					DocumentApi documentApi = null;
					String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
					if (!indexApi.existIndex(index))	indexApi.createIndex(index);
					documentApi = new DocumentApi(client);
					
					logMap.put("if_status", "READY");
					logMap.put("if_doc_reference_no", masterOrder.get("MBL_NO"));
					logMap.put("if_remark", "M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "(" + houseCnt + "건)");
					logMap.put("es_job_no", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "99999");
					
					documentApi.createDocument(index, Util.null2str(logMap.get("es_job_no")), logMap);
					logMap.put("if_status", "SEND");
					logMap.put("if_remark", "");
					logMap.remove("es_job_no");
					
					result.put("logMap", logMap);
				}else{
					result.put("DB", insertGovSendList);
					result.put("insertGovSendMap", insertData);
				}
				
	        }else{
	            orderSaveResult.setResultCode("ERROR");
	            result.put("processCode", "0000");
	            tempSearchData.put("srchCode", "");
	            tempSearchData.put("message", "Master Order 자료가 존재하지 않습니다.");
	            message = ediCommonService.getErrorCodeName(tempSearchData);
	            result.put("processMessage", message);
	        }

	        orderSaveResult.setResultData(result);
	        return orderSaveResult;
	    }
	 
		public void setDkxCustomsManifestImp(SendManifest sendManifest, String cusSendType, OrderSaveResult orderSaveResult) throws Exception {
			
			Map<String, Object> resultData = orderSaveResult.getResultData();
			
			SqlSession sqlSession = null;
			int cnt = 1;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
				OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
				ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
				String customsErrCode = "";
				boolean success = true;	// 목록전송성공여부
				String token = null;
				
				if("REST".equals(cusSendType)){
					JSONObject jsonObject = (JSONObject)resultData.get("REST");
					String sendType = Util.null2str(resultData.get("sendType"), "LIST");
					jsonObject = ediCommonService.sendEbms(Util.null2str(resultData.get("ediPortalMailboxId")), "GOVCBRDKX", jsonObject, sendType);
					
					String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
					String index2 = "ecm-ups-tracking-upsxk";
					ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
					ArrayList<BulkData> bulkList2 = new ArrayList<BulkData>();	// UPS bulkList
					BulkData bulkData = null;
					int seq = 1;
					Map<String, Object> logMap = (Map)resultData.get("logMap");
					logMap.put("customs_err_desc", jsonObject.get("customsErrDesc"));
					logMap.put("customs_err_desc_additional", jsonObject.get("body"));
					customsErrCode = Util.null2str(jsonObject.get("customsErrCode"));
					if(!customsErrCode.equals("") && !customsErrCode.equals("C200")) {
						success = false; // 목록전송실패
					}
					
					List<Map<String, Object>> logList = (List)resultData.get("logList");
					for(Map<String, Object> logData : logList){
						logData.putAll(logMap);
						bulkData = new BulkData();
						bulkData.setActionType(BulkData.Type.CREATE);
						bulkData.setIndexName(index);
						bulkData.setId(Util.null2str(logData.get("es_job_no")));
						bulkData.setJsonMap(logData);
						bulkList.add(bulkData);
					}
					
					if (bulkList != null && bulkList.size() > 0) {
						IndexApi indexApi = new IndexApi(client);
						if (!indexApi.existIndex(index))	indexApi.createIndex(index);
						
						DocumentApi documentApi = new DocumentApi(client);
						documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
					}
					
					List<Map<String, Object>> upsList = (List)resultData.get("upsList");
					if(success && upsList != null) {
						long trackingDateTime = System.currentTimeMillis();
						for(Map<String, Object> upsData : upsList) {
							upsData.put("DEMTrackingDateTime", trackingDateTime);
							bulkData = new BulkData();
							bulkData.setActionType(BulkData.Type.CREATE);
							bulkData.setIndexName(index2);
							bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
							bulkData.setJsonMap(upsData);
							bulkList2.add(bulkData);
						}
						
						if (bulkList2 != null && bulkList2.size() > 0) {
							IndexApi indexApi = new IndexApi(client);
							if (!indexApi.existIndex(index2))	indexApi.createIndex(index2);
							
							DocumentApi documentApi = new DocumentApi(client);
							documentApi.bulkDocumentWithBulkProcessor(bulkList2, 10000);
						}
					}
					
				}else{
					Map<String, Object> insertGovSendMap = (Map)resultData.get("insertGovSendMap");
					List<Map<String, Object>> insertGovSendList = (List)resultData.get("DB");
					for(Map<String, Object> insertGovSendData : insertGovSendList){
						insertGovSendMap.putAll(insertGovSendData);
						ediCommonService.insertGovSend(commonBatchMapper, insertGovSendMap);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
				
				Map<String, Object> masterLastPodMap = new HashMap<String, Object>();
				if(!success) {
					masterLastPodMap.put("lastPodStatus", "ECM111");
				}else {
					masterLastPodMap.put("lastPodStatus", "ECM110");
				}
				masterLastPodMap.put("gmtTime", sendManifest.getGmtTime());
				masterLastPodMap.put("updateType", "odJobNo");
				masterLastPodMap.put("odJobNo", resultData.get("masterOdJobNo"));
				commonMapper.updateLastPodStatus(masterLastPodMap);
				
				cnt = 1;
				List<SendManifest> updateCustomsManifestList = (List)resultData.get("updateCustomsManifestList");
				for(SendManifest updateSendManifest : updateCustomsManifestList){
					if(!success) {
						updateSendManifest.setPodStatus("ECM111");
					}
					updateSendManifest.setAction(cusSendType);
					updateSendManifest.setCustomsErrCode(customsErrCode);
					manifestBatchMapper.updateCustomsManifest(updateSendManifest);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				cnt = 1;
				Map<String, Object> podMap = (Map)resultData.get("podMap");
				List<Map<String, Object>> insertOrderPodList = (List)resultData.get("insertOrderPodList");
				List<Map<String, Object>> updateOrderPodList = new ArrayList<Map<String, Object>>();
				if(!success) {
					podMap.put("podStatus", "ECM111");
				}
				
				/** 취약성점검 SQL Injection 방지 */
				Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
				for(Map<String, Object> insertOrderPodMap : insertOrderPodList){
					if((success && insertOrderPodMap.get("ECM110_POD_JOB_NO") != null) || 	// 전송 성공했고 목록전송	POD가 이미 있는 경우 또는
						(!success && insertOrderPodMap.get("ECM111_POD_JOB_NO") != null)	// 전송 실패했고 목록전송실패	POD가 이미 있는 경우
					){
						updateOrderPodList.add(insertOrderPodMap);
					}else{
						podMap.putAll(insertOrderPodMap);
						orderBatchMapper.insertOrderPod(podMap);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
				cnt = 1;
				for(Map<String, Object> updateOrderPodMap : updateOrderPodList){
					if(!success) {
						updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("ECM111_POD_JOB_NO"));
					}else {
						updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("ECM110_POD_JOB_NO"));
					}
					podMap.putAll(updateOrderPodMap);
					orderBatchMapper.updateOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				
				/** 통관목록전송시 오류통보&목록전송실패POD삭제 */
				if(success) {
					cnt = 1;
					for(Map<String, Object> deleteOrderPodMap : insertOrderPodList){
						podMap.putAll(deleteOrderPodMap);
						podMap.put("podStatus", "ECM115");
						orderBatchMapper.deleteOrderPod(podMap);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
						
						podMap.put("podStatus", "ECM111");
						orderBatchMapper.deleteOrderPod(podMap);
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
}