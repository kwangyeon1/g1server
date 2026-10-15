package org.yht.exps.express.service.edi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
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
import org.yht.exps.express.mapper.edi.AbnormalMapper;
import org.yht.exps.express.mapper.edi.CustomsMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.edi.SendDelivery;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Slf4j
@Service
@Transactional
public class CustomsService {
		
	@Autowired
	CustomsMapper customsMapper;
	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	AbnormalMapper abnormalMapper;
	@Autowired
	ConsolMapper consolMapper;
	@Autowired
	private CustomProperties customProperties;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	@Resource(name = "commonService")
	CommonService commonService;
	
	
	/**
	 * 배송지변경신고
	 * @param sendDelivery
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult setSendDeliveryModifyData(SendDelivery sendDelivery, String cusSendType) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> sendDeliveryMaster = new HashMap<String, Object>();
		
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendDelivery.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendDelivery.getUserLanguage()));
		
		int seq = 1;
		List<Map<String, Object>> ediList = sendDelivery.getEdiList();	//EDI신고서 리스트
		
		/** 2023-01-12  사서함이 서로 다를수 있어서 한건씩 전송가능하도록 수정   ediList.size() == 1 */
		List<Map<String, Object>> sendDeliveryList = customsMapper.selectSendDelivery(ediList.get(0).get("ediDocNo"));
		
		if(sendDeliveryList != null && sendDeliveryList.size() > 0){
			
			sendDeliveryMaster = sendDeliveryList.get(0);
			
			List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
			
			if (this.getSendDeliveryModifyValidate(sendDeliveryMaster, sendDeliveryList, tempSearchData, errList, result)){
				
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
				Map<String, Object> insertData = new HashMap<String, Object>();
				String shipperType = null;
				
				document = documentBuilder.newDocument();
				document.setXmlVersion("1.0");
				document.setXmlStandalone(true);
				root = document.createElement("wco:Declaration");
				
				root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_109SchemaModule:1:0");
				root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_109SchemaModule:1:0");
				root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_109SchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfSPC_109SchemaModule_1.0_standard.xsd");
				document.appendChild(root);
				
				/** 배송내역제출번호 */
				this.createElement(document, root, lv1, "wco:ID", Util.null2str(sendDeliveryMaster.get("DELIVERY_DOC_NO")));
				/** 제출일자 */
				this.createElement(document, root, lv1, "wco:IssueDateTime", Util.null2str(sendDeliveryMaster.get("ISSUE_DATE")));
				/** 문서형태구분 */
				this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBR109");
				/** 제출대상년월 */
				lv1 = this.createElement(document, root, lv1, "wco:AdditionalInformation", null);
				this.createElement(document, lv1, lv2, "wco:LimitDateTime", Util.null2str(sendDeliveryMaster.get("LIMIT_DATE")));
				/** 특송업체부호 */
				lv1 = this.createElement(document, root, lv1, "wco:Carrier", null);
				this.createElement(document, lv1, lv2, "wco:ID", ediCommonService.getSubStringK(Util.null2str(sendDeliveryMaster.get("EDI_EXPRESS_SIGN")), 0, 6));
				
				int cnt = 0;
				
				for(Map<String, Object> sendDeliveryMap : sendDeliveryList){
					/** wco:GoodsShipment 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:GoodsShipment", null);
					
					/** wco:AdditionalDocument 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalDocument", null);
					/** 배송내역일련번호 */
					sendDeliveryMap.put("deliveryChangedSeqNo", Util.lpad(Util.null2str(++cnt), 8, '0'));
					this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(sendDeliveryMap.get("deliveryChangedSeqNo")));
					/** 통관목록제출일자 */
					/** SIMPLE_REPORT_DATE데이터 저장   2024-01-17 데몬 반영함   데이터 쌓일동안 기존소스로 전송 */
//					this.createElement(document, lv2, lv3, "wco:IssueDateTime", Util.null2str(sendDeliveryMap.get("SIMPLE_REPORT_DATE")));
					//24.08.14 수정 (목록 : 통관목록접수일자 / 일반, 배제, 취하 : 수입신고수리일자)
					if(Util.null2str(sendDeliveryMap.get("CD_TYPE")).equals("Y") && Util.null2str(sendDeliveryMap.get("DROP_YN"), "N").equals("N")){
						this.createElement(document, lv2, lv3, "wco:IssueDateTime", Util.null2str(sendDeliveryMap.get("SIMPLE_REPORT_DATE")));
					}else{
						this.createElement(document, lv2, lv3, "wco:IssueDateTime", Util.null2str(sendDeliveryMap.get("CLEARANCE_DATE")));
					}
					
					/** wco:AdditionalDocument 끝 */
					
					/** 변경사유 */
					lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalInformation", null);
					this.createElement(document, lv2, lv3, "wco:Content", Util.null2str(sendDeliveryMap.get("DELIVERY_REASON_CHANGED")));
					/** 최초제출 수하인주소 */
					lv2 = this.createElement(document, lv1, lv2, "wco:Consignee", null);
					lv3 = this.createElement(document, lv2, lv3, "wco:Address", null);
					this.createElement(document, lv3, lv4, "wco:Line", ediCommonService.getSubStringK(Util.null2str(sendDeliveryMap.get("CONSIGNEE_ADDRESS")), 0, 150));
					
					/** wco:Consignment 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:Consignment", null);
					//일반,목록 구분(18.06.11 추가)
					/** 배송정보제출신고구분코드 */
					//24.05.14 수정 (목록 : L / 일반, 배제, 취하 : I)
					lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalDocument", null);
					if(Util.null2str(sendDeliveryMap.get("CD_TYPE")).equals("Y") && Util.null2str(sendDeliveryMap.get("DROP_YN"), "N").equals("N")){
						this.createElement(document, lv3, lv4, "wco:TypeCode", "L");	//통관목록신고건
					}else{
						this.createElement(document, lv3, lv4, "wco:TypeCode", "I");	//수입신고제출건
					}
					
					/** wco:BorderTransportMeans 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:BorderTransportMeans", null);
					/** 배송일자 */
					this.createElement(document, lv3, lv4, "wco:ArrivalDateTime", Util.null2str(sendDeliveryMap.get("DELIVERY_DATE")));
					/** 항차항공편명 */
					if(Util.null2str(sendDeliveryMap.get("BOUND")).equals("AIR")){
//							this.createElement(document, lv3, lv4, "wco:ScheduledConveyanceID", Util.null2str(sendDeliveryMap.get("LINE_CODE"))+Util.null2str(sendDeliveryMap.get("VESSEL_FLIGHT")));
						this.createElement(document, lv3, lv4, "wco:ScheduledConveyanceID", Util.null2str(sendDeliveryMap.get("VESSEL_FLIGHT")));	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
					}else{
						this.createElement(document, lv3, lv4, "wco:ScheduledConveyanceID", Util.null2str(sendDeliveryMap.get("VOYAGE")));
					}
					/** wco:BorderTransportMeans 끝 */
					
					/** 배송업체명 */
					lv3 = this.createElement(document, lv2, lv3, "wco:Carrier", null);
					this.createElement(document, lv3, lv4, "wco:Name", Util.null2str(sendDeliveryMap.get("DELIVERY_NAME_CHANGED")));
					/** 적출항구코드 */
					lv3 = this.createElement(document, lv2, lv3, "wco:LoadingLocation", null);
					this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(sendDeliveryMap.get("ORIG_CODE")).trim());
					/** HBL번호 */
					lv3 = this.createElement(document, lv2, lv3, "wco:TransportContractDocument", null);
					this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(sendDeliveryMap.get("HBL_NO")).trim());
					/** wco:Consignment 끝 */
					
					/** 실제배송 수하인주소 */
					lv2 = this.createElement(document, lv1, lv2, "wco:DeliveryDestination", null);
					lv3 = this.createElement(document, lv2, lv3, "wco:Address", null);
					this.createElement(document, lv3, lv4, "wco:Line", ediCommonService.getSubStringK(Util.null2str(sendDeliveryMap.get("DELIVERY_ADDRESS_CHANGED")), 0, 150));
					/** 품명 */
					lv2 = this.createElement(document, lv1, lv2, "wco:GovernmentAgencyGoodsItem", null);
					lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
					String limitCommodityItem = Util.null2str(sendDeliveryMap.get("COMMODITY_ITEM"));
					this.createElement(document, lv3, lv4, "wco:CargoDescription", Util.getKOR2bytePassing(limitCommodityItem, 195));
					/** 화물관리번호 */
					lv2 = this.createElement(document, lv1, lv2, "wco:UCR", null);
					this.createElement(document, lv2, lv3, "wco:CustomsAssignedReferenceID", Util.null2str(sendDeliveryMap.get("MRN")).trim() + Util.null2str(sendDeliveryMap.get("MSN")).trim() + Util.null2str(sendDeliveryMap.get("HSN")).trim());
					/** wco:GoodsShipment 끝 */
				}
				
				SqlSession sqlSession = null;
				try {
					sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
					CustomsMapper customsBatchMapper = sqlSession.getMapper(CustomsMapper.class);
					int updateCnt = 1;
					for(Map<String, Object> sendDeliveryMap : sendDeliveryList){
						customsBatchMapper.updateDeliveryChangedSeqNo(sendDeliveryMap);
						if( (updateCnt++ % 1000) == 0){
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
				
				out = new ByteArrayOutputStream();
				source = new DOMSource(document);
				streamResult = new StreamResult(out);
				transformer.transform(source, streamResult);
				
				if("REST".equals(cusSendType)){
					JSONObject jsonObject = new JSONObject();
					String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
					jsonObject.put("customsReportNo", Util.null2str(sendDeliveryMaster.get("DELIVERY_DOC_NO")));
					jsonObject.put("xml", xml);
					resultData.put("REST", jsonObject);
					resultData.put("ediPortalMailboxId", sendDeliveryMaster.get("EDI_PORTAL_MAILBOX_ID"));
					
					String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
					
					// ES Index
					String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
					
					// ES Bulk List
					Map<String, Object> tmpMap = new HashMap<String, Object>();
					String filePath = customProperties.getBackupDir() + "edi/customs/" + sendDelivery.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
					String fileName = dataId + "GOVCBR109.xml";
					FileUtil.fileWrite(xml, filePath, fileName);
					
					
					tmpMap.put("if_file_path", filePath);
					tmpMap.put("if_file_name", fileName);
					tmpMap.put("ent_code", sendDelivery.getEntCode());
					tmpMap.put("customer_code", "");
					tmpMap.put("solution", "ECM");
					tmpMap.put("bound", sendDelivery.getBound());
					tmpMap.put("bound_type", "IMP");
					tmpMap.put("hbl_no", "");
					tmpMap.put("if_service_code", "");
					tmpMap.put("if_doc_reference_no", sendDeliveryMaster.get("DELIVERY_DOC_NO"));
					tmpMap.put("if_vender", "GOV");
					tmpMap.put("if_send_recv_type", "S");
					tmpMap.put("if_send_recv_method", "");
					tmpMap.put("if_status", "SEND");
					tmpMap.put("if_status_info", "");
					tmpMap.put("if_sender_id", "");
					tmpMap.put("if_receiver_id", "");
					tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
					tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
					tmpMap.put("if_remark", "");
					tmpMap.put("register_user_id", sendDelivery.getRegisterUserId());
					tmpMap.put("register_user_name", sendDelivery.getRegisterUserName());
					tmpMap.put("register_date_time", System.currentTimeMillis());
					tmpMap.put("es_job_no", dataId);
					tmpMap.put("if_doc_code", "CHANGE_DELIVERY_ADDR");
					tmpMap.put("if_doc_name", "배송지변경신고");
					
					resultData.put("index", index);
					resultData.put("dataId", dataId);
					resultData.put("logMap", tmpMap);
				}else{
					insertData.put("entCode", sendDelivery.getEntCode());
					insertData.put("typeCode", "GOVCBR109");
					insertData.put("declarationId", Util.null2str(sendDeliveryMaster.get("DELIVERY_DOC_NO")));
					insertData.put("ediDesc", out.toByteArray());
					insertData.put("registerUserId", sendDelivery.getRegisterUserId());
					insertData.put("gmtTime", sendDelivery.getGmtTime());
					insertData.put("sendStatus", "N");
					
					insertData.put("entBusinessCode", "");
					insertData.put("updateUserId", sendDelivery.getRegisterUserId());
					insertData.put("solution", "ECM");
					insertData.put("stockType", "EDI");
					insertData.put("stockSign", "EDI");
					insertData.put("stockSeqLength", "8");
					insertData.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
					insertData.put("stockRemark", "sendDeliveryModify(GOVCBR109)");
					insertData.put("ediPortalMailboxId", sendDeliveryMaster.get("EDI_PORTAL_MAILBOX_ID"));
					resultData.put("DB", insertData);
				}
				
				sendDeliveryMaster.put("gmtTime", sendDelivery.getGmtTime());
				
				resultData.put("updateEidLinkedData", sendDeliveryMaster);
				out.close();
				
			}
		
		// 하우스 없는 Edi 신고건의 경우
		}else{
			orderSaveResult.setResultCode("A11316");	/** 처리할 하우스 리스트가 없습니다. */
		}
		
		if(result.size() > 0) {
			orderSaveResult.setResultCode("ERROR");
			resultData.put("result", result);
		}else {
			resultData.put("result", null);
		}
		
		orderSaveResult.setResultData(resultData);
		
		return orderSaveResult;
	}
	public void sendDeliveryModify(SendDelivery sendDelivery, String cusSendType, OrderSaveResult orderSaveResult) throws Exception {
		Map<String, Object> resultData = orderSaveResult.getResultData();
		String customsErrCode = "";
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(sendDelivery, "gmtTime", false, null, null, null, null);
		
		if("REST".equals(cusSendType)){
			JSONObject jsonObject = (JSONObject)resultData.get("REST");
			jsonObject = ediCommonService.sendEbms(Util.null2str(resultData.get("ediPortalMailboxId")), "GOVCBR109", jsonObject);
			
			Map<String, Object> logMap = (Map)resultData.get("logMap");
			customsErrCode = Util.null2str(jsonObject.get("customsErrCode"));
			logMap.put("customs_err_desc", jsonObject.get("customsErrDesc"));
			logMap.put("customs_err_desc_additional", jsonObject.get("body"));
			String index = Util.null2str(resultData.get("index"));
			IndexApi indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			DocumentApi documentApi = new DocumentApi(client);
			documentApi.createDocument(index, Util.null2str(resultData.get("dataId")), logMap);
			
		}else{
			ediCommonService.insertGovSend(commonMapper, (Map)resultData.get("DB"));
		}
		
		Map<String, Object> updateEidLinkedData = (Map)resultData.get("updateEidLinkedData");
		updateEidLinkedData.put("customsErrCode", customsErrCode);
		Util.accessControlDatabase(updateEidLinkedData, "gmtTime", false, null, null, null, null);
		customsMapper.updateEidLinked(updateEidLinkedData);
	}
	
	/**
	 * 별도관리물품 해제 신청서
	 * @param sendDelivery
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult sendUnmanagementItemData(SendDelivery sendDelivery, String cusSendType) throws Exception {
		int seq = 1;
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> masterOrder = new HashMap<String, Object>();
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendDelivery.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendDelivery.getUserLanguage()));
		String message = null;
		List<List<Map<String, Object>>> simpleDocNoHouseList = new ArrayList<List<Map<String, Object>>>();
		Util.accessControlDatabase(sendDelivery, "gmtTime", false, null, null, null, null);
		
		List<Map<String, Object>> tmpHouseOrderList;
		for(Map<String, Object> ediData : sendDelivery.getEdiList()){
			tmpHouseOrderList = customsMapper.selectSendUnmanagementHouse(ediData);
			if(tmpHouseOrderList != null && tmpHouseOrderList.size() > 0){
				this.getSendUnmanagementItemValidate(tmpHouseOrderList, tempSearchData, errList, result);
				simpleDocNoHouseList.add(tmpHouseOrderList);
			}
		}
		
		if(simpleDocNoHouseList.size() == 0){
			result.put("processCode", "0000");
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "Master Order 자료가 존재하지 않습니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			result.put("processMessage", message);
			orderSaveResult.setResultData(result);
			return orderSaveResult;
		}
		
		if(errList.size() > 0){
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("BOUND", masterOrder.get("BOUND"));
			ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
			orderSaveResult.setResultData(result);
			orderSaveResult.setResultCode("ERROR");
			return orderSaveResult;
		}
		
		List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tmpInsertData = null;
		List<Map<String, Object>> insertGovSendList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> updateEidLinkedList = new ArrayList<Map<String, Object>>();
		Map<String, Object> updateEidLinked;
		
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		JSONObject tmpJsonObject = null;
		String simpleDocNo;
		for(List<Map<String, Object>> houseOrderList : simpleDocNoHouseList){
			masterOrder = houseOrderList.get(0);
			simpleDocNo = Util.null2str(masterOrder.get("EDI_DOC_NO"));
				
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
			Map<String, Object> insertData = new HashMap<String, Object>();
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			String shipperType = null;
			
			document = documentBuilder.newDocument();
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
			root = document.createElement("wco:Declaration");
			
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_DAXSchemaModule:1:0");
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_DAXSchemaModule:1:0");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_DAXSchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfCAG_DAXSchemaModule_1.0_standard.xsd");
			document.appendChild(root);
			
			/** 신청세관 */
			this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(masterOrder.get("CUSTOMS_CODE")));
			/** 신청번호 */
			this.createElement(document, root, lv1, "wco:ID", Util.null2str(masterOrder.get("SPECIAL_ITEM_DOC_NO")));
			/** 신청일자 */
			this.createElement(document, root, lv1, "wco:IssueDateTime", Util.null2str(masterOrder.get("ISSUE_DATE")));
			/** 문서형태구분 */
			this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBRDAX");
			
			/** wco:BorderTransportMeans 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
			/** 입항일자 */
			this.createElement(document, lv1, lv2, "wco:ArrivalDateTime", Util.null2str(masterOrder.get("AR_DATE")).trim());
			/** 편명 */
			this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", Util.cutLength(masterOrder.get("VESSEL_FLIGHT"), 7));
			/** wco:BorderTransportMeans 끝 */
			
			
			for(Map<String, Object> houseOrder : houseOrderList){
				/** wco:Consignment 시작 */
				lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
				
				/** wco:AssociatedTransportDocument 시작 */
				lv2 = this.createElement(document, lv1, lv2, "wco:AssociatedTransportDocument", null);
				/** H AWB NO */
				this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(houseOrder.get("HBL_NO")).trim());
				/** HSN */
				this.createElement(document, lv2, lv3, "kcs:HouseBLSequenceID", Util.null2str(houseOrder.get("HSN")).trim());
				/** wco:AssociatedTransportDocument 끝 */
			
				List<Map<String, Object>> sendUnmanagementItemList = (List<Map<String, Object>>)houseOrder.get("sendUnmanagementItemList");
				for(Map<String, Object> sendUnmanagementItem : sendUnmanagementItemList){
					/** wco:ConsignmentItem 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:ConsignmentItem", null);
					/** 별도관리물품대상구분(별도관리 선후착 화물내역에서는 "2:선착화물, 3:후착화물" 만 사용하고, 선착화물내역은 하나만 존재하여야 함.) */
					lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
					this.createElement(document, lv3, lv4, "wco:StatementCode", Util.null2str(sendUnmanagementItem.get("MANAGE_TYPE")));
					
					/** wco:BorderTransportMeans 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:BorderTransportMeans", null);
					/** 입항일자 */
					this.createElement(document, lv3, lv4, "wco:ArrivalDateTime", Util.null2str(sendUnmanagementItem.get("AR_DATE")));
					/** 편명 */
					this.createElement(document, lv3, lv4, "wco:ScheduledConveyanceID", Util.null2str(sendUnmanagementItem.get("FLIGHT_NO")));
					/** wco:BorderTransportMeans 끝 */
					
					/** wco:Commodity 시작 */
					lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
					/** 품명 */
					this.createElement(document, lv3, lv4, "wco:CargoDescription", Util.null2str(sendUnmanagementItem.get("ITEM_DESC")));
					/** 중량 */
					this.createElement(document, lv3, lv5, "wco:SizeMeasure", Util.null2str(sendUnmanagementItem.get("GWT")));
					/** wco:Commodity 끝 */
					
					/** 적재항 */
					lv3 = this.createElement(document, lv2, lv3, "wco:GoodsConsignedPlace", null);
					this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(sendUnmanagementItem.get("DEST_COUNTRY")) + Util.null2str(sendUnmanagementItem.get("DEST_PORT")));
					/** 포장개수 */
					lv3 = this.createElement(document, lv2, lv3, "wco:Packaging", null);
					this.createElement(document, lv3, lv4, "wco:QuantityQuantity", Util.null2str(sendUnmanagementItem.get("PIECES")));
					/** wco:ConsignmentItem 끝 */
				}
				
				/** wco:Commodity 시작 */
				lv2 = this.createElement(document, lv1, lv2, "wco:Commodity", null);
				/** 품명 */
				this.createElement(document, lv2, lv3, "wco:CargoDescription", Util.null2str(houseOrder.get("COMMODITY_ITEM")).trim());
				/** 중량 */
				this.createElement(document, lv2, lv3, "wco:SizeMeasure", Util.null2str(houseOrder.get("GWT")).trim());
				/** wco:Commodity 끝 */
				
				/** 포장개수 */
				lv2 = this.createElement(document, lv1, lv2, "wco:Packaging", null);
				this.createElement(document, lv2, lv3, "wco:QuantityQuantity", Util.null2str(houseOrder.get("PIECES")).trim());
				
				/** wco:TransportContractDocument 시작 */
				lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
				/** M AWB NO */
				this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(houseOrder.get("MBL_NO")).trim());
				/** MSN */
				this.createElement(document, lv2, lv3, "kcs:MasterBLSequenceID", Util.null2str(houseOrder.get("MSN")).trim());
				/** wco:TransportContractDocument 끝 */
				/** wco:Consignment 끝 */
			}
			
			
			/** 적재항 */
			lv1 = this.createElement(document, root, lv1, "wco:LoadingLocation", null);
			this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("DEST_COUNTRY")) + Util.null2str(masterOrder.get("DEST_PORT")));
			/** MRN */
			lv1 = this.createElement(document, root, lv1, "wco:PreviousDocument", null);
			this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(masterOrder.get("MRN")).trim());
			
			
			out = new ByteArrayOutputStream();
			source = new DOMSource(document);
			streamResult = new StreamResult(out);
			transformer.transform(source, streamResult);
			
			if("REST".equals(cusSendType)){
				tmpJsonObject = new JSONObject();
				String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
				tmpJsonObject.put("customsReportNo", simpleDocNo);
				tmpJsonObject.put("xml", xml);
				jsonArray.add(tmpJsonObject);
				
				String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
				String filePath = customProperties.getBackupDir() + "edi/customs/" + sendDelivery.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
				String fileName = dataId + "GOVCBRDAX.xml";
				FileUtil.fileWrite(xml, filePath, fileName);
				
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("if_file_path", filePath);
				tmpMap.put("if_file_name", fileName);
				tmpMap.put("if_doc_reference_no", simpleDocNo);
				tmpMap.put("es_job_no", dataId);
				logList.add(tmpMap);
			}else{
				tmpInsertData = new HashMap<String, Object>();
				tmpInsertData.put("ediDesc", out.toByteArray());
				tmpInsertData.put("declarationId", simpleDocNo);
				insertGovSendList.add(tmpInsertData);
			}
			out.close();
			
			updateEidLinked = new HashMap<String, Object>();
			updateEidLinked.put("gmtTime", sendDelivery.getGmtTime());
			updateEidLinked.put("EDI_JOB_NO", masterOrder.get("EDI_JOB_NO"));
			updateEidLinked.put("ediDocNo", simpleDocNo);
			updateEidLinked.put("issueDate", masterOrder.get("ISSUE_DATE"));
			updateEidLinkedList.add(updateEidLinked);
		}
		
		result.put("updateEidLinkedList", updateEidLinkedList);
		if("REST".equals(cusSendType)){
			jsonObject.put("dataList", jsonArray);
			result.put("REST", jsonObject);
			result.put("logList", logList);
			result.put("ediPortalMailboxId", masterOrder.get("EDI_PORTAL_MAILBOX_ID"));
		}else{
			result.put("DB", insertGovSendList);
		}
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	public void sendUnmanagementItem(SendDelivery sendDelivery, String cusSendType ,OrderSaveResult orderSaveResult) throws Exception {
		Map<String, Object> resultData = orderSaveResult.getResultData();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(sendDelivery, "gmtTime", false, null, null, null, null);
		
		SqlSession sqlSession = null;
		int cnt = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			CustomsMapper customsBatchMapper = sqlSession.getMapper(CustomsMapper.class);
			String customsErrCode = "";
			
			if("REST".equals(cusSendType)){
				JSONObject jsonObject = (JSONObject)resultData.get("REST");
				jsonObject = ediCommonService.sendEbms(Util.null2str(resultData.get("ediPortalMailboxId")), "GOVCBRDAX", jsonObject, "LIST");
				
				String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
				ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
				BulkData bulkData = null;
				Map<String, Object> logMap = new HashMap<String, Object>();
				logMap.put("ent_code", sendDelivery.getEntCode());
				logMap.put("customer_code", "");
				logMap.put("solution", "ECM");
				logMap.put("bound", "AIR");
				logMap.put("bound_type", "IMP");
				logMap.put("hbl_no", "");
				logMap.put("if_service_code", "");
				logMap.put("if_vender", "GOV");
				logMap.put("if_send_recv_type", "S");
				logMap.put("if_send_recv_method", "");
				logMap.put("if_status", "SEND");
				logMap.put("if_status_info", "");
				logMap.put("if_sender_id", "");
				logMap.put("if_receiver_id", "");
				logMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
				logMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
				logMap.put("if_remark", "");
				logMap.put("register_user_id", sendDelivery.getRegisterUserId());
				logMap.put("register_user_name", sendDelivery.getRegisterUserName());
				logMap.put("register_date_time", System.currentTimeMillis());
				logMap.put("if_doc_code", "SEPARATELY_MANAGED");
				logMap.put("if_doc_name", "별도관리물품 해제 신청서");
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
				
				if (bulkList != null && bulkList.size() > 0) {
					IndexApi indexApi = new IndexApi(client);
					if (!indexApi.existIndex(index))	indexApi.createIndex(index);
					
					DocumentApi documentApi = new DocumentApi(client);
					documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
				}
			}else{
				Map<String, Object> insertGovSendMap = new HashMap<String, Object>();
				insertGovSendMap.put("entCode", sendDelivery.getEntCode());
				insertGovSendMap.put("typeCode", "GOVCBRDAX");
				insertGovSendMap.put("registerUserId", sendDelivery.getRegisterUserId());
				insertGovSendMap.put("gmtTime", sendDelivery.getGmtTime());
				insertGovSendMap.put("sendStatus", "N");
				insertGovSendMap.put("entBusinessCode", "");
				insertGovSendMap.put("updateUserId", sendDelivery.getRegisterUserId());
				insertGovSendMap.put("solution", "ECM");
				insertGovSendMap.put("stockType", "EDI");
				insertGovSendMap.put("stockSign", "EDI");
				insertGovSendMap.put("stockSeqLength", "8");
				insertGovSendMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				insertGovSendMap.put("stockRemark", "sendUnmanagementItem(GOVCBRDAX)");
				insertGovSendMap.put("ediPortalMailboxId", resultData.get("ediPortalMailboxId"));
				
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
			
			cnt = 1;
			List<Map<String, Object>> updateEidLinkedList = (List)resultData.get("updateEidLinkedList");
			for(Map<String, Object> updateEidLinked : updateEidLinkedList){
				updateEidLinked.put("customsErrCode", customsErrCode);
				customsBatchMapper.updateEidLinked(updateEidLinked);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			cnt = 1;
			for(Map<String, Object> updateEidLinked : updateEidLinkedList){
				customsBatchMapper.updateUnmanagementDate(updateEidLinked);
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
	
	/**
	 * 수출이행신고
	 * @param sendDelivery
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult setSendExportFulfillmentNotificationData(SendDelivery sendDelivery, String index) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendDelivery.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendDelivery.getUserLanguage()));
		String message = null;
		Util.accessControlDatabase(sendDelivery, "gmtTime", false, null, null, null, null);
		
		List<Map<String, Object>> sendFulfillmentList = customsMapper.selectSendFulfillment(sendDelivery);
		if(sendFulfillmentList != null && sendFulfillmentList.size() > 0){
			if (this.getSendfulFillmentValidate(sendFulfillmentList, tempSearchData, errList, result)){
				
				String podStatus = null;
				if("01".equals(sendDelivery.getSendType())){
					podStatus = "ECM121";
				}else if("02".equals(sendDelivery.getSendType())){
					podStatus = "ECM127";
				}else if("03".equals(sendDelivery.getSendType())){
					podStatus = "ECM125";
				}
				
				/** 건수가 많을경우 전송시간이 오래 걸려서 전송전에 상태변경/로그저장함  */
				Map<String, Object> updateEdiStatusMap = new HashMap<String, Object>();
				updateEdiStatusMap.put("odJobNo", sendDelivery.getOdJobNo());
				updateEdiStatusMap.put("ediList", sendDelivery.getEdiList());
				updateEdiStatusMap.put("gmtTime", sendDelivery.getGmtTime());
				updateEdiStatusMap.put("sendType", sendDelivery.getSendType());
				updateEdiStatusMap.put("podStatus", podStatus);
				customsMapper.updateEdiStatus(updateEdiStatusMap);
				
				IndexApi indexApi = new IndexApi(client);
				DocumentApi documentApi = null;
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				documentApi = new DocumentApi(client);
				
				String mblNo = Util.null2str(sendFulfillmentList.get(0).get("MBL_NO"));
				Map<String, Object> logMap = new HashMap<String, Object>();
				logMap.put("ent_code", sendDelivery.getEntCode());
				logMap.put("customer_code", "");
				logMap.put("solution", "ECM");
				logMap.put("bound", sendDelivery.getBound());
				logMap.put("bound_type", sendDelivery.getBoundType());
				logMap.put("if_service_code", "");
				logMap.put("if_vender", "GOV");
				logMap.put("if_send_recv_type", "S");
				logMap.put("if_send_recv_method", "");
				logMap.put("if_status_info", "");
				logMap.put("if_sender_id", "");
				logMap.put("if_receiver_id", "");
				logMap.put("register_user_id", sendDelivery.getRegisterUserId());
				logMap.put("register_user_name", sendDelivery.getRegisterUserName());
				logMap.put("if_doc_code", "FULFILLMENT_NOTIFICATION");
				logMap.put("if_doc_name", "이행신고");
				logMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
				logMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
				logMap.put("register_date_time", System.currentTimeMillis());
				logMap.put("if_status", "READY");
				logMap.put("if_doc_reference_no", mblNo);
				logMap.put("if_remark", "M.B/L No : " + Util.null2str(mblNo) + "(" + sendFulfillmentList.size() + "건)");
				logMap.put("es_job_no", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "99999");
				
				documentApi.createDocument(index, Util.null2str(logMap.get("es_job_no")), logMap);
				
				result.put("houseOrderList", sendFulfillmentList);
			}else{
				orderSaveResult.setResultCode("ERROR");
			}
		}else{
			orderSaveResult.setResultCode("ERROR");
			result.put("processCode", "0000");
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "전송할 자료가 존재하지 않습니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			result.put("processMessage", message);
		}
		
		orderSaveResult.setResultData(result);
		
		
		return orderSaveResult;
	}
	public void sendExportFulfillmentNotification(SendDelivery sendDelivery, List<Map<String, Object>> sendFulfillmentList, String ediPortalMailboxId, String index) throws Exception {
		
		List<Map<String, Object>> insertOrderPodList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> updateOrderPodList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tmpPodMap = null;
		int seq = 1;
		
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
		
		Map<String, Object> tmpMap = null;
		List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
		
		String ediDocNo;
		JSONArray jsonArray = new JSONArray();
		JSONObject tmpJsonObject = null;
		String origCode = null;
		int houseCnt = 0;
		Map<String, Object> restData = new HashMap<String, Object>();
		List<String> houseOdJobNoList = new ArrayList<String>();
		Util.accessControlDatabase(sendDelivery, "gmtTime", false, null, null, null, null);
		
		for(Map<String, Object> sendFulfillment : sendFulfillmentList){
			ediDocNo = Util.null2str(sendFulfillment.get("SIMPLE_DOC_NO"));
			document = documentBuilder.newDocument();
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
			root = document.createElement("wco:Declaration");
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_DJRSchemaModule:1:0");
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_DJRSchemaModule:1:0");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_DJRSchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfEXP_DJRSchemaModule_1.0_standard.xsd");
			document.appendChild(root);
			
			/** 전송구분(01:등록,02:수정,03:삭제) */
			this.createElement(document, root, lv1, "wco:FunctionCode", sendDelivery.getSendType());
			/** 신고번호 */
			this.createElement(document, root, lv1, "wco:ID", ediDocNo);
			/** 문서형태구분 */
			this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBRDJR");
			/** 신고구분(01:인터넷신고(ebXml),03:BlockChain) */
			this.createElement(document, root, lv1, "wco:TransactionNatureCode", "01");
			/** 주문번호 */
			lv1 = this.createElement(document, root, lv1, "wco:AdditionalDocument", null);
			this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(sendFulfillment.get("EFN_PO_NO")));
			
			/** wco:BorderTransportMeans 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
			/** 출항일시(년월일시분(CCYYMMDDHHMM)) */
			this.createElement(document, lv1, lv2, "wco:DepartureDateTime", Util.null2str(sendFulfillment.get("OB_DATE")) + Util.null2str(sendFulfillment.get("OB_TIME")));
			if(Util.null2str(sendFulfillment.get("BOUND")).equals("AIR")){
				/** 선박/항공편명 */
//				this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", ediCommonService.getSubStringK(Util.null2str(sendFulfillment.get("LINE_CODE")) + Util.null2str(sendFulfillment.get("VESSEL_FLIGHT")), 0, 5));
				this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", ediCommonService.getSubStringK(Util.null2str(sendFulfillment.get("VESSEL_FLIGHT")), 0, 5));	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
				/** 해상/항공 구분(10 : 해상, 40 : 항공) */
				this.createElement(document, lv1, lv2, "wco:TypeCode", "40");
			}else{
				/** 선박/항공편명 */
				this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", ediCommonService.getSubStringK(Util.null2str(sendFulfillment.get("VESSEL_FLIGHT")), 0, 10));
				/** 해상/항공 구분(10 : 해상, 40 : 항공) */
				this.createElement(document, lv1, lv2, "wco:TypeCode", "10");
			}
			/** wco:BorderTransportMeans 끝 */
			
			/** wco:Consignment 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
			lv2 = this.createElement(document, lv1, lv2, "wco:PreviousDocument", null);
			lv3 = this.createElement(document, lv2, lv3, "wco:Packaging", null);
			/** 수출동시포장여부 */
			this.createElement(document, lv3, lv4, "kcs:ConcurrentPackagingCode", Util.null2str(sendFulfillment.get("COMBINE_YN"), "N"));
			/** wco:Consignment 끝 */
			
			/** 수출화주 신고인 부호 */
			lv1 = this.createElement(document, root, lv1, "wco:Exporter", null);
			this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(sendFulfillment.get("EFN_SENDER_CODE")));
			
			/** wco:GoodsShipment 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:GoodsShipment", null);
			/** 배송신고번호 */
			lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalDocument", null);
			this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(sendFulfillment.get("EFN_DELIVERY_NO")));
			/** 목적국 */
			lv2 = this.createElement(document, lv1, lv2, "wco:Destination", null);
			this.createElement(document, lv2, lv3, "wco:CountryCode", Util.null2str(sendFulfillment.get("CONSIGNEE_COUNTRY_CODE")));

			/** wco:GovernmentAgencyGoodsItem 시작 */
			/** wco:Commodity 시작 */
			lv2 = this.createElement(document, lv1, lv2, "wco:GovernmentAgencyGoodsItem", null);
			lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
			/** 이행수량 */
			this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(sendFulfillment.get("PIECES")));
			/** 이행중량 */
			this.createElement(document, lv3, lv4, "wco:SizeMeasure", Util.null2str(sendFulfillment.get("GWT")));
			/** wco:GovernmentAgencyGoodsItem 끝 */
			/** wco:Commodity 끝 */
			/** wco:GoodsShipment 끝 */
			
			/** 전자상거래수출신고번호 */
			lv1 = this.createElement(document, root, lv1, "wco:PreviousDocument", null);
			this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(sendFulfillment.get("LICENSE_NO")));
			
			out = new ByteArrayOutputStream();
			source = new DOMSource(document);
			streamResult = new StreamResult(out);
			transformer.transform(source, streamResult);
			
			tmpJsonObject = new JSONObject();
			String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
			tmpJsonObject.put("customsReportNo", ediDocNo);
			tmpJsonObject.put("xml", xml);
			jsonArray.add(tmpJsonObject);
			houseOdJobNoList.add(Util.null2str(sendFulfillment.get("OD_JOB_NO")));
			
			String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
			String filePath = customProperties.getBackupDir() + "edi/customs/" + sendDelivery.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
			String fileName = dataId + "GOVCBRDJR.xml";
			FileUtil.fileWrite(xml, filePath, fileName);
			
			out.close();
			
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("if_file_path", filePath);
			tmpMap.put("if_file_name", fileName);
			tmpMap.put("hbl_no", sendFulfillment.get("HBL_NO"));
			tmpMap.put("if_doc_reference_no", sendFulfillment.get("SIMPLE_DOC_NO"));
			tmpMap.put("es_job_no", dataId);
			logList.add(tmpMap);
			
			tmpPodMap = new HashMap<String, Object>();
			tmpPodMap.put("odJobNo", sendFulfillment.get("OD_JOB_NO"));
			tmpPodMap.put("blNo", sendFulfillment.get("HBL_NO"));
			if(sendFulfillment.get("SEND_POD_JOB_NO") != null){
				tmpPodMap.put("SEND_POD_JOB_NO", sendFulfillment.get("SEND_POD_JOB_NO"));
				updateOrderPodList.add(tmpPodMap);
			}else{
				tmpPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
				insertOrderPodList.add(tmpPodMap);
			}
		}	/** sendFulfillmentList for End */
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("dataList", jsonArray);
		
		log.info("[수출이행신고] EBMS 전송 요청 - entCode={}, 사서함ID={}, 문서수={}, houseOdJobNoList={}",
				sendDelivery.getEntCode(), ediPortalMailboxId, jsonArray.size(), houseOdJobNoList);

		jsonObject = ediCommonService.sendEbms(ediPortalMailboxId, "GOVCBRDJR", jsonObject, "LIST");

		String customsErrCode = Util.null2str(jsonObject.get("customsErrCode"));
		String customsErrDesc = Util.null2str(jsonObject.get("customsErrDesc"));
		if("C200".equals(customsErrCode)){
			log.info("[수출이행신고] EBMS 전송 성공 - entCode={}, 사서함ID={}, 문서수={}, customsErrCode={}, customsErrDesc={}",
					sendDelivery.getEntCode(), ediPortalMailboxId, jsonArray.size(), customsErrCode, customsErrDesc);
		}else{
			log.error("[수출이행신고] EBMS 전송 실패 - entCode={}, 사서함ID={}, 문서수={}, customsErrCode={}, customsErrDesc={}, body={}",
					sendDelivery.getEntCode(), ediPortalMailboxId, jsonArray.size(), customsErrCode, customsErrDesc, Util.null2str(jsonObject.get("body")));
		}

		String podStatus = null;
		if("01".equals(sendDelivery.getSendType())){
			podStatus = "ECM121";
		}else if("02".equals(sendDelivery.getSendType())){
			podStatus = "ECM127";
		}else if("03".equals(sendDelivery.getSendType())){
			podStatus = "ECM125";
		}
		
		Map<String, Object> updateEdiStatusMap = new HashMap<String, Object>();
		updateEdiStatusMap.put("gmtTime", sendDelivery.getGmtTime());
		updateEdiStatusMap.put("sendType", sendDelivery.getSendType());
		updateEdiStatusMap.put("podStatus", podStatus);
		updateEdiStatusMap.put("customsErrCode", jsonObject.get("customsErrCode"));
		updateEdiStatusMap.put("houseOdJobNoList", houseOdJobNoList);
		customsMapper.updateEdiStatus(updateEdiStatusMap);
		
		if(sendDelivery.getAction() == null) {
			String masterOdJobNo = Util.null2str(sendFulfillmentList.get(0).get("MASTER_OD_JOB_NO"));
			tmpPodMap = new HashMap<String, Object>();
			tmpPodMap.put("podTableName", "TB_ECM_POD_02");
			tmpPodMap.put("odJobNo", masterOdJobNo);
			tmpPodMap.put("podStatus", podStatus);
			tmpPodMap = consolMapper.selectPodInfo(tmpPodMap);
			if(tmpPodMap == null || tmpPodMap.size() == 0){
				tmpPodMap = new HashMap<String, Object>();
				tmpPodMap.put("odJobNo", masterOdJobNo);
				tmpPodMap.put("blNo", sendFulfillmentList.get(0).get("MBL_NO"));
				tmpPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(++seq));
				insertOrderPodList.add(tmpPodMap);
			}
			
			Map<String, Object> masterLastPodMap = new HashMap<String, Object>();
			masterLastPodMap.put("lastPodStatus", podStatus);
			masterLastPodMap.put("gmtTime", sendDelivery.getGmtTime());
			masterLastPodMap.put("updateType", "odJobNo");
			masterLastPodMap.put("odJobNo", masterOdJobNo);
			commonMapper.updateLastPodStatus(masterLastPodMap);
		}
		sendDelivery.setAction("masterPodChk");
		
		Map<String, Object> logMap = new HashMap<String, Object>();
		logMap.put("ent_code", sendDelivery.getEntCode());
		logMap.put("customer_code", "");
		logMap.put("solution", "ECM");
		logMap.put("bound", sendDelivery.getBound());
		logMap.put("bound_type", sendDelivery.getBoundType());
		logMap.put("if_service_code", "");
		logMap.put("if_vender", "GOV");
		logMap.put("if_send_recv_type", "S");
		logMap.put("if_send_recv_method", "");
		logMap.put("if_status_info", "");
		logMap.put("if_sender_id", "");
		logMap.put("if_receiver_id", "");
		logMap.put("register_user_id", sendDelivery.getRegisterUserId());
		logMap.put("register_user_name", sendDelivery.getRegisterUserName());
		logMap.put("if_doc_code", "FULFILLMENT_NOTIFICATION");
		logMap.put("if_doc_name", "이행신고");
		logMap.put("customs_err_desc", jsonObject.get("customsErrDesc"));
		logMap.put("customs_err_desc_additional", jsonObject.get("body"));
		logMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
		logMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
		logMap.put("register_date_time", System.currentTimeMillis());
		logMap.put("if_status", "SEND");
		logMap.put("if_remark", "");
		BulkData bulkData;
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		for(Map<String, Object> logData : logList){
			logData.putAll(logMap);
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(Util.null2str(logData.get("es_job_no")));
			bulkData.setJsonMap(logData);
			bulkList.add(bulkData);
		}
		
		if (bulkList.size() > 0) {
			DocumentApi documentApi = new DocumentApi(client);
			documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
		}
		
		SqlSession sqlSession = null;
		int cnt = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("podTableName", 	"TB_ECM_POD_02");
			podMap.put("entCode", sendDelivery.getEntCode());
			podMap.put("podStatus", podStatus);
			podMap.put("gmtTime", sendDelivery.getGmtTime());
			podMap.put("updateUserId", sendDelivery.getUpdateUserId());
			
			
			for(Map<String, Object> insertOrderPodMap : insertOrderPodList){
				podMap.putAll(insertOrderPodMap);
				orderBatchMapper.insertOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			cnt = 1;
			for(Map<String, Object> updateOrderPodMap : updateOrderPodList){
				updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("SEND_POD_JOB_NO"));
				podMap.putAll(updateOrderPodMap);
				orderBatchMapper.updateOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			/** 수출이행신고 전송시 오류통보POD삭제 */
			cnt = 1;
			podMap.put("podStatus", "ECM122");
			for(Map<String, Object> deleteOrderPodMap : insertOrderPodList){
				podMap.putAll(deleteOrderPodMap);
				orderBatchMapper.deleteOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			for(Map<String, Object> deleteOrderPodMap : updateOrderPodList){
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
	public OrderSaveResult setSendExportFulfillmentNotificationDataOld(SendDelivery sendDelivery) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("entCode", Util.null2str(sendDelivery.getEntCode()));
		tempSearchData.put("userLanguage", Util.null2str(sendDelivery.getUserLanguage()));
		String message = null;
		Util.accessControlDatabase(sendDelivery, "gmtTime", false, null, null, null, null);
		
		List<Map<String, Object>> sendFulfillmentList = customsMapper.selectSendFulfillment(sendDelivery);
		if(sendFulfillmentList != null && sendFulfillmentList.size() > 0){
			if (this.getSendfulFillmentValidate(sendFulfillmentList, tempSearchData, errList, result)){
				List<Map<String, Object>> insertOrderPodList = new ArrayList<Map<String, Object>>();
				Map<String, Object> tmpPodMap = null;
				int seq = 1;
				Map<String, Object> logMap = new HashMap<String, Object>();
				logMap.put("ent_code", sendDelivery.getEntCode());
				logMap.put("customer_code", "");
				logMap.put("solution", "ECM");
				logMap.put("bound", sendDelivery.getBound());
				logMap.put("bound_type", sendDelivery.getBoundType());
				logMap.put("if_service_code", "");
				logMap.put("if_vender", "GOV");
				logMap.put("if_send_recv_type", "S");
				logMap.put("if_send_recv_method", "");
				logMap.put("if_status", "SEND");
				logMap.put("if_status_info", "");
				logMap.put("if_sender_id", "");
				logMap.put("if_receiver_id", "");
				logMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
				logMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
				logMap.put("if_remark", "");
				logMap.put("register_user_id", sendDelivery.getRegisterUserId());
				logMap.put("register_user_name", sendDelivery.getRegisterUserName());
				logMap.put("register_date_time", System.currentTimeMillis());
				logMap.put("if_doc_code", "FULFILLMENT_NOTIFICATION");
				logMap.put("if_doc_name", "이행신고");
				
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
				
//				List<Map<String, Object>> updateEdiStatusList = new ArrayList<Map<String, Object>>();
				
				Map<String, Object> tmpMap = null;
				List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
				
				String ediDocNo;
				int cnt = 1;
				JSONObject jsonObject = new JSONObject();
				JSONArray jsonArray = new JSONArray();
				JSONObject tmpJsonObject = null;
				String ediPortalMailboxId = "";
				String origCode = null;
				int houseCnt = 0;
				Map<String, Object> restData = new HashMap<String, Object>();
				List<String> odJobNoArray = new ArrayList<String>();
				List<JSONArray> jsonArrayList = new ArrayList<JSONArray>();	/** 수출일경우 하우스200건씩 전송 */
				List<List<String>> odJobNoArrayList = new ArrayList<List<String>>();
				
				for(Map<String, Object> sendFulfillment : sendFulfillmentList){
					if(!ediPortalMailboxId.equals("") && !ediPortalMailboxId.equals(sendFulfillment.get("EDI_PORTAL_MAILBOX_ID"))){
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
					}
					ediPortalMailboxId = Util.null2str(sendFulfillment.get("EDI_PORTAL_MAILBOX_ID"));
					origCode = Util.null2str(sendFulfillment.get("ORIG_CODE"), "noData");
					
					
					ediDocNo = Util.null2str(sendFulfillment.get("SIMPLE_DOC_NO"));
					
					document = documentBuilder.newDocument();
					document.setXmlVersion("1.0");
					document.setXmlStandalone(true);
					root = document.createElement("wco:Declaration");
					root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_DJRSchemaModule:1:0");
					root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_DJRSchemaModule:1:0");
					root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
					root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfEXP_DJRSchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfEXP_DJRSchemaModule_1.0_standard.xsd");
					document.appendChild(root);
					
					/** 전송구분(01:등록,02:수정,03:삭제) */
					this.createElement(document, root, lv1, "wco:FunctionCode", sendDelivery.getSendType());
					/** 신고번호 */
					this.createElement(document, root, lv1, "wco:ID", ediDocNo);
					/** 문서형태구분 */
					this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBRDJR");
					/** 신고구분(01:인터넷신고(ebXml),03:BlockChain) */
					this.createElement(document, root, lv1, "wco:TransactionNatureCode", "01");
					/** 주문번호 */
					lv1 = this.createElement(document, root, lv1, "wco:AdditionalDocument", null);
					this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(sendFulfillment.get("EFN_PO_NO")));
					
					/** wco:BorderTransportMeans 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:BorderTransportMeans", null);
					/** 출항일시(년월일시분(CCYYMMDDHHMM)) */
					this.createElement(document, lv1, lv2, "wco:DepartureDateTime", Util.null2str(sendFulfillment.get("OB_DATE")) + Util.null2str(sendFulfillment.get("OB_TIME")));
					if(Util.null2str(sendFulfillment.get("BOUND")).equals("AIR")){
						/** 선박/항공편명 */
//						this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", ediCommonService.getSubStringK(Util.null2str(sendFulfillment.get("LINE_CODE")) + Util.null2str(sendFulfillment.get("VESSEL_FLIGHT")), 0, 5));
						this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", ediCommonService.getSubStringK(Util.null2str(sendFulfillment.get("VESSEL_FLIGHT")), 0, 5));	/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
						/** 해상/항공 구분(10 : 해상, 40 : 항공) */
						this.createElement(document, lv1, lv2, "wco:TypeCode", "40");
					}else{
						/** 선박/항공편명 */
						this.createElement(document, lv1, lv2, "wco:ScheduledConveyanceID", ediCommonService.getSubStringK(Util.null2str(sendFulfillment.get("VESSEL_FLIGHT")), 0, 10));
						/** 해상/항공 구분(10 : 해상, 40 : 항공) */
						this.createElement(document, lv1, lv2, "wco:TypeCode", "10");
					}
					/** wco:BorderTransportMeans 끝 */
					
					/** 수출화주 신고인 부호 */
					lv1 = this.createElement(document, root, lv1, "wco:Exporter", null);
					this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(sendFulfillment.get("EFN_SENDER_CODE")));
					
					/** wco:GoodsShipment 시작 */
					lv1 = this.createElement(document, root, lv1, "wco:GoodsShipment", null);
					/** 배송신고번호 */
					lv2 = this.createElement(document, lv1, lv2, "wco:AdditionalDocument", null);
					this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(sendFulfillment.get("EFN_DELIVERY_NO")));
					/** 목적국 */
					lv2 = this.createElement(document, lv1, lv2, "wco:Destination", null);
					this.createElement(document, lv2, lv3, "wco:CountryCode", Util.null2str(sendFulfillment.get("CONSIGNEE_COUNTRY_CODE")));

					/** wco:GovernmentAgencyGoodsItem 시작 */
					/** wco:Commodity 시작 */
					lv2 = this.createElement(document, lv1, lv2, "wco:GovernmentAgencyGoodsItem", null);
					lv3 = this.createElement(document, lv2, lv3, "wco:Commodity", null);
					/** 이행수량 */
					this.createElement(document, lv3, lv4, "wco:CountQuantity", Util.null2str(sendFulfillment.get("PIECES")));
					/** 이행중량 */
					this.createElement(document, lv3, lv4, "wco:SizeMeasure", Util.null2str(sendFulfillment.get("GWT")));
					/** wco:GovernmentAgencyGoodsItem 끝 */
					/** wco:Commodity 끝 */
					/** wco:GoodsShipment 끝 */
					
					/** 전자상거래수출신고번호 */
					lv1 = this.createElement(document, root, lv1, "wco:PreviousDocument", null);
					this.createElement(document, lv1, lv2, "wco:ID", Util.null2str(sendFulfillment.get("LICENSE_NO")));

					out = new ByteArrayOutputStream();
					source = new DOMSource(document);
					streamResult = new StreamResult(out);
					transformer.transform(source, streamResult);
					
					tmpJsonObject = new JSONObject();
					String xml = new String(out.toByteArray(), StandardCharsets.UTF_8);
					tmpJsonObject.put("customsReportNo", ediDocNo);
					tmpJsonObject.put("xml", xml);
					jsonArray.add(tmpJsonObject);
					odJobNoArray.add(Util.null2str(sendFulfillment.get("OD_JOB_NO")));
					houseCnt++;
					/** 2025-03-05 MRS전송시 200건씩 전속되도록 수정 */
					if(houseCnt == 200){
						jsonArrayList.add(jsonArray);
						odJobNoArrayList.add(odJobNoArray);
						jsonArray = new JSONArray();
						odJobNoArray = new ArrayList<String>();
						houseCnt = 0;
					}
					
					String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(cnt++);
					String filePath = customProperties.getBackupDir() + "edi/customs/" + sendDelivery.getEntCode() + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
					String fileName = dataId + "GOVCBRDJR.xml";
					FileUtil.fileWrite(xml, filePath, fileName);
					
					
					tmpMap = new HashMap<String, Object>();
					tmpMap.put("if_file_path", filePath);
					tmpMap.put("if_file_name", fileName);
					tmpMap.put("hbl_no", sendFulfillment.get("HBL_NO"));
					tmpMap.put("if_doc_reference_no", sendFulfillment.get("SIMPLE_DOC_NO"));
					tmpMap.put("es_job_no", dataId);
					logList.add(tmpMap);
					
					
					tmpPodMap = new HashMap<String, Object>();
					tmpPodMap.put("odJobNo", sendFulfillment.get("OD_JOB_NO"));
					tmpPodMap.put("blNo", sendFulfillment.get("HBL_NO"));
					if(sendFulfillment.get("SEND_POD_JOB_NO") != null){
						tmpPodMap.put("SEND_POD_JOB_NO", sendFulfillment.get("SEND_POD_JOB_NO"));
					}else{
						tmpPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					}
					insertOrderPodList.add(tmpPodMap);
					
					out.close();
				}
				
				if(houseCnt != 0){
					jsonArrayList.add(jsonArray);
					odJobNoArrayList.add(odJobNoArray);
				}
				
				String podStatus = null;
				if("01".equals(sendDelivery.getSendType())){
					podStatus = "ECM121";
				}else if("02".equals(sendDelivery.getSendType())){
					podStatus = "ECM127";
				}else if("03".equals(sendDelivery.getSendType())){
					podStatus = "ECM125";
				};
				String masterOdJobNo = Util.null2str(sendFulfillmentList.get(0).get("MASTER_OD_JOB_NO"));
				tmpPodMap = new HashMap<String, Object>();
				tmpPodMap.put("podTableName", "TB_ECM_POD_02");
				tmpPodMap.put("odJobNo", masterOdJobNo);
				tmpPodMap.put("podStatus", podStatus);
				tmpPodMap = consolMapper.selectPodInfo(tmpPodMap);
				if(tmpPodMap == null || tmpPodMap.size() == 0){
					tmpPodMap = new HashMap<String, Object>();
					tmpPodMap.put("odJobNo", masterOdJobNo);
					tmpPodMap.put("blNo", sendFulfillmentList.get(0).get("MBL_NO"));
					tmpPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					insertOrderPodList.add(tmpPodMap);
				}
				
				result.put("insertOrderPodList", insertOrderPodList);
				result.put("masterOdJobNo", masterOdJobNo);
				
				
				jsonObject = new JSONObject();
				jsonObject.put("dataList", jsonArrayList);
				jsonObject.put("odJobNoList", odJobNoArrayList);
				restData.put(ediPortalMailboxId, jsonObject);
				restData.put(ediPortalMailboxId + "_ORIG_CODE", origCode);
				restData.put(ediPortalMailboxId + "_LOG_LIST", logList);
				
				result.put("REST", restData);
				result.put("logMap", logMap);
				
				/** 건수가 많을경우 전송시간이 오래 걸려서 전송전에 상태변경/로그저장함  */
				Map<String, Object> updateEdiStatusMap = new HashMap<String, Object>();
				updateEdiStatusMap.put("odJobNo", sendDelivery.getOdJobNo());
				updateEdiStatusMap.put("ediList", sendDelivery.getEdiList());
				updateEdiStatusMap.put("gmtTime", sendDelivery.getGmtTime());
				updateEdiStatusMap.put("sendType", sendDelivery.getSendType());
				updateEdiStatusMap.put("podStatus", podStatus);
				customsMapper.updateEdiStatus(updateEdiStatusMap);
			}else{
				orderSaveResult.setResultCode("ERROR");
			}
		}else{
			orderSaveResult.setResultCode("ERROR");
			result.put("processCode", "0000");
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "전송할 자료가 존재하지 않습니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			result.put("processMessage", message);
		}
		
		orderSaveResult.setResultData(result);
		
		
		return orderSaveResult;
	}
	public void sendExportFulfillmentNotificationMrs(String ediPortalMailboxId, JSONObject sendData, Map<String, Object> logMap, Map<String, Object> updateEdiStatusMap, List<String> houseOdJobNoList) throws Exception {
		try{
			JSONObject jsonObject = ediCommonService.sendEbms(ediPortalMailboxId, "GOVCBRDJR", sendData, "LIST");
			logMap.put("customs_err_desc", jsonObject.get("customsErrDesc"));
			logMap.put("customs_err_desc_additional", jsonObject.get("body"));
			updateEdiStatusMap.put("customsErrCode", jsonObject.get("customsErrCode"));
			updateEdiStatusMap.put("houseOdJobNoList", houseOdJobNoList);
			customsMapper.updateEdiStatus(updateEdiStatusMap);
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			try{
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("houseOdJobNoList", houseOdJobNoList);
				ediCommonService.asyncErrorSendEmail("CustomsService.sendExportFulfillmentNotificationMrs", map, e);
			}catch(Exception ee){
				log.error(Util.getPrintStacTraceString(ee));
			}
		}
	}
	public void sendExportFulfillmentNotificationOld(SendDelivery sendDelivery, OrderSaveResult orderSaveResult, ArrayList<BulkData> bulkList, String index, String podStatus) throws Exception {
		Map<String, Object> resultData = orderSaveResult.getResultData();
		SqlSession sqlSession = null;
		int cnt = 1;
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(sendDelivery, "gmtTime", false, null, null, null, null);
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			if (bulkList != null && bulkList.size() > 0) {
				IndexApi indexApi = new IndexApi(client);
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				
				DocumentApi documentApi = new DocumentApi(client);
				documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
			}
			
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("podTableName", 	"TB_ECM_POD_02");
			podMap.put("entCode", sendDelivery.getEntCode());
			podMap.put("podStatus", podStatus);
			podMap.put("gmtTime", sendDelivery.getGmtTime());
			podMap.put("updateUserId", sendDelivery.getUpdateUserId());
			
			Map<String, Object> masterLastPodMap = new HashMap<String, Object>();
			masterLastPodMap.put("lastPodStatus", podStatus);
			masterLastPodMap.put("gmtTime", sendDelivery.getGmtTime());
			masterLastPodMap.put("updateType", "odJobNo");
			masterLastPodMap.put("odJobNo", resultData.get("masterOdJobNo"));
			commonMapper.updateLastPodStatus(masterLastPodMap);
			
			List<Map<String, Object>> insertOrderPodList = (List)resultData.get("insertOrderPodList");
			List<Map<String, Object>> updateOrderPodList = new ArrayList<Map<String, Object>>();
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
			cnt = 1;
			for(Map<String, Object> insertOrderPodMap : insertOrderPodList){
				if(insertOrderPodMap.get("SEND_POD_JOB_NO") != null){
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
				updateOrderPodMap.put("podJobNo", updateOrderPodMap.get("SEND_POD_JOB_NO"));
				podMap.putAll(updateOrderPodMap);
				orderBatchMapper.updateOrderPod(podMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			/** 수출이행신고 전송시 오류통보POD삭제 */
			cnt = 1;
			podMap.put("podStatus", "ECM122");
			for(Map<String, Object> deleteOrderPodMap : insertOrderPodList){
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
	
	/**
	 * 배송지변경신고 오류검증
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean getSendDeliveryModifyValidate(Map<String, Object> sendDeliveryMaster, List<Map<String, Object>> sendDeliveryList, Map<String, Object> tempSearchData, List<Map<String, Object>> errList, Map<String, Object> result) throws java.lang.Exception {
		String message = null;
		String hblNo = null;
		String mblNo = Util.null2str(sendDeliveryMaster.get("MBL_NO"));
		
		if ("".equals(Util.null2str(sendDeliveryMaster.get("DELIVERY_DOC_NO")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "배송내역제출번호를 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(sendDeliveryMaster.get("ISSUE_DATE")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "제출일자를 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(sendDeliveryMaster.get("LIMIT_DATE")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "제출대상년월을 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(sendDeliveryMaster.get("EDI_EXPRESS_SIGN")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "특송업체부호를 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(sendDeliveryMaster.get("EDI_PORTAL_MAILBOX_ID")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "관세청사서함을 설정 후 전송하십시오.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		for(Map<String, Object> sendDelivery : sendDeliveryList) {
			hblNo = Util.null2str(sendDelivery.get("HBL_NO"));
			/** 2024-12-16  전송값   통관목록제출일자(wco:GoodsShipment > wco:AdditionalDocument > wco:IssueDateTime) 유무로 검증내용 수정 */
//			//통관구분값이 “목록”, “배제”, “간이” 인경우 통관목록을 전송합니다.
//			/** SIMPLE_REPORT_DATE데이터 저장   2024-01-17 데몬 반영함   데이터 쌓일동안 기존소스로 전송 */
////			if ("".equals(Util.null2str(sendDelivery.get("SIMPLE_REPORT_DATE")))) {
//			if ("Y".equals(Util.null2str(sendDelivery.get("CD_TYPE"))) && "".equals(Util.null2str(sendDelivery.get("HOLD_CODE"))) && "".equals(Util.null2str(sendDelivery.get("SIMPLE_REPORT_DATE")))) {
//				tempSearchData.put("srchCode", "");
//				tempSearchData.put("message", "목록 전송이력이 없는 경우 배송지신고를 할 수 없습니다.");
//				message = ediCommonService.getErrorCodeName(tempSearchData);
//				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//			}
//			if (!"Y".equals(Util.null2str(sendDelivery.get("CD_TYPE"))) && "".equals(Util.null2str(sendDelivery.get("CLEARANCE_DATE")))) {
//				tempSearchData.put("srchCode", "");
//				tempSearchData.put("message", "수입신고수리 이력이 없는 경우 배송지신고를 할 수 없습니다.");
//				message = ediCommonService.getErrorCodeName(tempSearchData);
//				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//			}
			
			if(Util.null2str(sendDelivery.get("CD_TYPE")).equals("Y") && Util.null2str(sendDelivery.get("DROP_YN"), "N").equals("N")){
				if("".equals(Util.null2str(sendDelivery.get("SIMPLE_REPORT_DATE")))){
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "목록 전송이력이 없는 경우 배송지신고를 할 수 없습니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));					
				}
			}else{
				if("".equals(Util.null2str(sendDelivery.get("CLEARANCE_DATE")))){
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "수입신고수리 이력이 없는 경우 배송지신고를 할 수 없습니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
				}
			}
			
			
			//통관구분값이  “일반” 경우 통관목록을 전송하지 않습니다. 
			/** 2023-08-02 일반/홀드도 전송 가능하도록 수정 */
//			if ("N".equals(Util.null2str(sendDelivery.get("CD_TYPE"))) || !"".equals(Util.null2str(sendDelivery.get("HOLD_CODE")))) {
//				tempSearchData.put("srchCode", "");
//				tempSearchData.put("message", "통관목록 구분값이 일반이거나 홀드건인 경우 배송지변경신고를 할 수 없습니다.");
//				message = ediCommonService.getErrorCodeName(tempSearchData);
//				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//			}
			if ("".equals(Util.null2str(sendDelivery.get("CONSIGNEE_ADDRESS")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "최초제출 수하인주소를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendDelivery.get("DELIVERY_DATE")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "배송일자를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if (("".equals(Util.null2str(sendDelivery.get("VESSEL_FLIGHT")).trim()) || "".equals(Util.null2str(sendDelivery.get("LINE_CODE"))))&& "AIR".equals(Util.null2str(sendDelivery.get("BOUND")))) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "항공편을 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendDelivery.get("VOYAGE")).trim()) && "SEA".equals(Util.null2str(sendDelivery.get("BOUND")))) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "항차를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendDelivery.get("DELIVERY_NAME_CHANGED")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "배송업체명을 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendDelivery.get("ORIG_CODE")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "적출항구코드를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendDelivery.get("DELIVERY_ADDRESS_CHANGED")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "실제배송 수하인주소를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendDelivery.get("COMMODITY_ITEM")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "품명을 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendDelivery.get("MRN")).trim()) || "".equals(Util.null2str(sendDelivery.get("MSN")).trim()) || "".equals(Util.null2str(sendDelivery.get("HSN")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "화물관리번호를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
		}
		
		// 오류내역이 있을 경우
		if (errList.size() > 0) {
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("BOUND", sendDeliveryMaster.get("BOUND"));
			ediCommonService.setValidateMessageDel(tmpMap, sendDeliveryMaster, errList, result);
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * 별도관리물품 해제 신청서 오류검증
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean getSendUnmanagementItemValidate(List<Map<String, Object>> houseOrderList, Map<String, Object> tempSearchData, List<Map<String, Object>> errList
			, Map<String, Object> result) throws java.lang.Exception {
		
		Map<String, Object> masterOrder = houseOrderList.get(0);
		SendDelivery sendDelivery = new SendDelivery();
		String message = null;
		String itemNo = null;
		String hblNo = null;
		String mblNo = Util.null2str(masterOrder.get("MBL_NO"));
		
		if ("".equals(Util.null2str(masterOrder.get("CUSTOMS_CODE")))) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "신청세관을 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("SPECIAL_ITEM_DOC_NO")))) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "신청번호를 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("ISSUE_DATE")))) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "신청일자를 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("AR_DATE")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "입항일자를 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("VESSEL_FLIGHT")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "항공편명을 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("DEST_COUNTRY")).trim()) || "".equals(Util.null2str(masterOrder.get("DEST_PORT")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "적재항을 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("MBL_NO")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "M AWB NO를 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("MRN")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "MRN을 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(masterOrder.get("MSN")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "MSN을 기재하기 바랍니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		if ("".equals(Util.null2str(masterOrder.get("EDI_PORTAL_MAILBOX_ID")).trim())) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "관세청사서함을 설정 후 전송하십시오.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		
		for(Map<String, Object> houseOrder : houseOrderList){
			hblNo = Util.null2str(houseOrder.get("HBL_NO"));
			if ("".equals(Util.null2str(houseOrder.get("COMMODITY_ITEM")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "품명을 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if (Util.null2dbl(houseOrder.get("GWT")) == 0) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "중량을 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if (Util.null2dbl(houseOrder.get("PIECES")) == 0) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "포장개수를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			
			sendDelivery.setOdJobNo(Util.null2str(houseOrder.get("OD_JOB_NO")));
			sendDelivery.setEntCode(Util.null2str(houseOrder.get("ENT_CODE")));
			List<Map<String, Object>> sendUnmanagementItemList = customsMapper.selectSendUnmanagementItem(sendDelivery);
			for(Map<String, Object> sendUnmanagementItem : sendUnmanagementItemList){
				itemNo = Util.null2str(sendUnmanagementItem.get("SEQ_NO_ID"));
				if ("".equals(Util.null2str(sendUnmanagementItem.get("MANAGE_TYPE")))) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "별도관리물품대상구분을 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L - " + itemNo + " : " + message));
				}
				
				if ("".equals(Util.null2str(sendUnmanagementItem.get("AR_DATE")))) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "입항일자를 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L - " + itemNo + " : " + message));
				}
				if ("".equals(Util.null2str(sendUnmanagementItem.get("FLIGHT_NO")).trim())) {
					tempSearchData.put("srchCode", "");
					tempSearchData.put("message", "편명을 기재하기 바랍니다.");
					message = ediCommonService.getErrorCodeName(tempSearchData);
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L - " + itemNo + " : " + message));
				}
				if("3".equals(Util.null2str(sendUnmanagementItem.get("MANAGE_TYPE")))){
					if (Util.null2dbl(sendUnmanagementItem.get("GWT")) == 0) {
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "중량을 기재하기 바랍니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L - " + itemNo + " : " + message));
					}
					if (Util.null2dbl(sendUnmanagementItem.get("PIECES")) == 0) {
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "포장개수를 기재하기 바랍니다.");
						message = ediCommonService.getErrorCodeName(tempSearchData);
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L - " + itemNo + " : " + message));
					}
				}
			}
			houseOrder.put("sendUnmanagementItemList", sendUnmanagementItemList);
		}
		
		
		// 오류내역이 있을 경우
		if (errList.size() > 0) {
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("BOUND", masterOrder.get("BOUND"));
			ediCommonService.setValidateMessage(tmpMap, masterOrder, errList, result);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 수출이행신고 오류검증
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean getSendfulFillmentValidate(List<Map<String, Object>> sendFillmentList, Map<String, Object> tempSearchData, List<Map<String, Object>> errList, Map<String, Object> result) throws java.lang.Exception {
		String message = null;
		String hblNo = null;
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		
		for(Map<String, Object> sendFillment : sendFillmentList) {
			hblNo = Util.null2str(sendFillment.get("HBL_NO"));
			if ("".equals(Util.null2str(sendFillment.get("OB_DATE"))) || "".equals(Util.null2str(sendFillment.get("OB_TIME")))) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "출항일시를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendFillment.get("LINE_CODE"))) && "AIR".equals(Util.null2str(sendFillment.get("BOUND")))) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "항공사코드를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendFillment.get("VESSEL_FLIGHT")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "선박/항공편명을 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendFillment.get("EFN_SENDER_CODE")))) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "수출화주 신고인 부호를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendFillment.get("CONSIGNEE_COUNTRY_CODE")).trim())) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "목적국을 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if (Util.null2dbl(sendFillment.get("PIECES")) == 0) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "이행수량을 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if (Util.null2dbl(sendFillment.get("GWT")) == 0) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "이행중량을 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendFillment.get("LICENSE_NO")))) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "전자상거래수출신고번호를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendFillment.get("EFN_DELIVERY_NO")))) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "운송번호를 기재하기 바랍니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendFillment.get("EDI_PORTAL_MAILBOX_ID")))) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "관세청사서함을 설정 후 전송하십시오.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
			if ("".equals(Util.null2str(sendFillment.get("EDI_EXPRESS_SIGN")))) {
				tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "특송업체부호를 설정 후 전송하십시오.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
			}
		}
		
		// 오류내역이 있을 경우
		if (errList.size() > 0) {
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("BOUND", sendFillmentList.get(0).get("BOUND"));
			ediCommonService.setValidateMessage(tmpMap, sendFillmentList.get(0), errList, result);
			return false;
		}
		
		return true;
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
		docSend.put("docType", "MANIFEST-ABNORMAL");
		docSend.put("registerUserId", sendDelivery.getRegisterUserId());
		docSend.put("registerUserName", sendDelivery.getRegisterUserName());
		docSend.put("srchEntCode", sendDelivery.getEntCode());
		docSend.put("srchEntBusinessCode", sendDelivery.getEntBusinessCode());
		docSend.put("srchSolution", sendDelivery.getSolution());
		docSend.put("srchBound", sendDelivery.getBound());
		docSend.put("srchBoundType", sendDelivery.getBoundType());
		docSend.put("ediList", sendDelivery.getEdiList());
		
		List<Map<String, Object>> tempDataList = null;
		if("PANTK".equals(sendDelivery.getEntCode())){
			tempDataList = customsMapper.selectSendBmsInPlanAbnormalities(sendDelivery);
		}else{
			tempDataList = customsMapper.selectSendAbnormalities(sendDelivery);
		}
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
		if("".equals(Util.null2str(docSend.get("ifServiceCode")))) {
			errList.add("현재 지원되지 않는 인터페이스 스펙(서비스코드) 입니다.");
			return false;
		}
		
		//////////////////////////////////////////
		// 본/지사 정보 가져오기( 중개사업자별 문서 및 송/수신 정보, TB_SMS_ENT_OFFICE_INTERFACE )
		//////////////////////////////////////////
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "ifServiceCodeIn", false, null, null, null, null);
		ediHostSendInfo = manifestMapper.getEdiInterfaceInfo(docSend);
		if(ediHostSendInfo == null || ediHostSendInfo.size() == 0) {
			errList.add("중계사업자정보가 존재하지 않습니다.");
			return false;
		}
		
		//////////////////////////////////////////
		// 오류검증 및 파일 생성후 전송
		//////////////////////////////////////////
		if(!this.getAbnormalitiesReport(docSend, tempDataList, fwSendAccInfo, ediHostSendInfo, errList)) {
			return false;
		}
		
		return true;
	}
	
	public boolean getAbnormalitiesReport(Map<String, Object> docSend, List<Map<String, Object>> tempDataList, Map<String, Object> fwSendAccInfo
			, Map<String, Object> ediHostSendInfo, List<String> errList) throws java.lang.Exception {
		
		String fileCreateRetVal[] = null;									// 파일 생성후 결과값, [0]:파일명, [1]:로컬파일경로, [2]:파일내용
		
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
		tmpMap.put("if_send_recv_method", ediHostSendInfo.get("IF_S_R_TYPE"));
		tmpMap.put("if_status", "SEND");
		tmpMap.put("if_status_info", "");
		tmpMap.put("if_sender_id", ediHostSendInfo.get("IF_SENDER_ID"));
		tmpMap.put("if_receiver_id", ediHostSendInfo.get("IF_RECEIVER_ID"));
		tmpMap.put("if_remark", "");
		tmpMap.put("register_user_id", docSend.get("registerUserId"));
		tmpMap.put("register_user_name", docSend.get("registerUserName"));
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		
		if(Util.null2str(docSend.get("ediVender")).equals("KCNET")){
			// 오류 검증
			if(!this.getAbnormalitiesReportValidateKCNET(docSend, tempDataList, fwSendAccInfo, ediHostSendInfo, errList)) {
				return false;
			}
			int seq = 1;
			for(Map<String, Object> mblData : tempDataList){
				
				// 파일 생성
				fileCreateRetVal = this.getSendDataFileCreateAbnormalitiesReportKCNET(docSend, mblData, fwSendAccInfo, ediHostSendInfo);
				
				if ("Y".equals(customProperties.getEdiSendYn())) {
					try {
						
						log.info("getAbnormalitiesReport HttpClient start "+ docSend.get("entCode"));
						
						//테스트 http://121.138.113.7:12101 리얼 http://222.122.233.22:12101
						String httpPost = Util.null2str("http://" + ediHostSendInfo.get("IF_SVR_IP")) + ":" + Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")) + "/WebServices/services/XmlDataService";
	//					HttpPost blPost = new HttpPost(httpPost);
	//					blPost.setHeader("Content-type", "text/xml;charset=utf-8");
						HttpClient client = HttpClientBuilder.create().build();
						
						
						HttpPost post = new HttpPost(httpPost);
						post.setHeader("SOAPAction", "");
				        post.setEntity(new StringEntity(fileCreateRetVal[1], StandardCharsets.UTF_8));
				        HttpResponse responseBl = client.execute(post);
				        
				        int responseCode = 0;
				        if(responseBl != null) {
				        	responseCode = responseBl.getStatusLine().getStatusCode();
				        	
				        	if(responseCode != 200 ) {
				        		errList.add("파일전송에 실패하였습니다.");
				        	}
				        	
				        	HttpEntity entity = responseBl.getEntity();
			                if (entity != null) {
			                   String result = EntityUtils.toString(entity);
			                   log.info("KCNET 하기이상 전송 결과 Response: " + result);
			                }
				        }
				        
				        log.info("getAbnormalitiesReport HttpClient end "+ docSend.get("entCode") + " responseCode :" + responseCode);
				        
					}catch(Exception e) {
						log.error(e.getMessage());
						throw e;
					}
//					if(!ediCommonService.ftpFileSend(docSend, mblData, fileCreateRetVal, ediHostSendInfo, resultList, resultMap)) {
//						errList.add("파일전송에 실패하였습니다.");
//					}
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
			
			// ES에 입력
			if (bulkList != null && bulkList.size() > 0) {
				// Index 확인 후 Index 생성
				indexApi = new IndexApi(client);
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				
				documentApi = new DocumentApi(client);
				documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
				
			}
		}else{
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
		
		if(errList.size() > 0) {
			return false;
		}
		
		//////////////////////////////////////////
		// 정정 M.B/L 상태값 업데이트
		//////////////////////////////////////////
		docSend.put("tempDataList", tempDataList);
		if("PANTK".equals(docSend.get("entCode"))){
			customsMapper.updateSendBmsInPlanAbnormalitiesReportd(docSend);
		}else{
			customsMapper.updateSendAbnormalitiesReportd(docSend);
		}
		
		/** MGN->MFN or SHT->MFN일경우  MFN신고시 하기이상 완료여부 Y 업데이트 */
		List<Map<String, Object>> tempHblDataList = null;
		for(Map<String, Object> masterData : tempDataList){
			tempHblDataList = (List<Map<String, Object>>)masterData.get("tempHblDataList");
			for(Map<String, Object> houseData : tempHblDataList){
				if("MFN".equals(houseData.get("ABNORMAL_CODE")) && Util.null2str(houseData.get("SEQ_NO_ID")).equals("2")){
					if(!"PANTK".equals(docSend.get("entCode"))){
						abnormalMapper.updateAbnormalFinishYn(houseData);
					}
					if(!Util.isEmpty(houseData.get("UPDATE_OD_JOB_NO"))){
						/** MFN(or SHT)전송하지 않고 해당건의 MFN만 전송할경우 전송데이터 리스트에서 조회하기위해 MGN의 전송상태값 업데이트 */
						houseData.put("gmtTime", docSend.get("gmtTime"));
						abnormalMapper.updateAbnormalSendDate(houseData);
					}
				}
			}
		}
		
		return true;
	}
	
	public boolean getAbnormalitiesReportValidateKCNET(Map<String, Object> docSend, List<Map<String, Object>> tempDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<String> errList) throws java.lang.Exception {
		List<Map<String, Object>> tempHblDataList = null;
		String mblNo = null;
		String hblNo = null;
		String ediVender = null;
		for(Map<String, Object> tempMblData : tempDataList){
			mblNo = Util.null2str(tempMblData.get("MBL_NO"));
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
			if("PANTK".equals(docSend.get("entCode"))){
				tempHblDataList = customsMapper.selectSendBmsInPlanAbnormalitiesAbnormal(docSend);
			}else{
				tempHblDataList = customsMapper.selectSendAbnormalitiesAbnormal(docSend);
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
				
				if(!Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MFN") && Util.null2dbl(tempHblData.get("PIECES")) == 0){
					errList.add("H.B/L No : " + hblNo + " 개수(포장)를 기재하기 바랍니다.");
				}
				if(!Util.null2str(tempHblData.get("ABNORMAL_CODE")).equals("MFN") && Util.null2dbl(tempHblData.get("GWT")) == 0){
					errList.add("H.B/L No : " + hblNo + " 중량을 기재하기 바랍니다.");
				}
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
				tempHblDataList = customsMapper.selectSendBmsInPlanAbnormalitiesAbnormal(docSend);
			}else{
				tempHblDataList = customsMapper.selectSendAbnormalitiesAbnormal(docSend);
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
	
	public String[] getSendDataFileCreateAbnormalitiesReportKCNET_old(Map<String, Object> docSend, Map<String, Object> mblData, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo) throws Exception {
		String writeData = null;
		String retData[] = new String[6];
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(mblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
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
			Map<String, String> attribute = new HashMap<String, String>();
			document = documentBuilder.newDocument();
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
			
			Element soapenvBody = null;
			Element soapenv = document.createElement("soapenv:Envelope");
//			soapenv.setAttribute("xmlns:soapenv","http://schemas.xmlsoap.org/soap/envelope");
			soapenv.setAttribute("xmlns:soapenv","http://www.w3.org/2003/05/soap-envelope");	//KCNET 전송시 버전 오류나서 수정
			soapenv.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
			soapenv.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			
			document.appendChild(soapenv);
			
			/** soapenvHeader */
			attribute.put("attributeName", "xmlns:kcn");
			attribute.put("attributeContent", "http://www.kcnet.co.kr/kcn");
			lv1 = this.createElement(document, soapenv, lv1, "soapenv:Header", null, attribute);
			
			lv2 = this.createElement(document, lv1, lv2, "kcn:Kcnet_Header", null);	//스펙에 있는 내용 누락되어 추가함
			
			/** 송신아이디 */
			this.createElement(document, lv2, lv3, "kcn:SenderId", Util.getFileWriteDataWithRtrim(ediCommonService.getDeleteCharStringFwb(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim()), "String", "4"));
			/** 수신아이디 */
			this.createElement(document, lv2, lv3, "kcn:ReceiverId", "KCNECP");
			/** 문서명 */
			this.createElement(document, lv2, lv3, "kcn:DocName", "KCNREQ");
			/** 문서코드 */
			this.createElement(document, lv2, lv3, "kcn:DocCode", "SWH");
			/** 메세지아이디 */
			this.createElement(document, lv2, lv3, "kcn:MessageId", Util.null2str(mblData.get("MBL_NO")));
			/** 중개업체 */
			this.createElement(document, lv2, lv3, "kcn:VenderId", "YHT");
			/** KCNETID */
			this.createElement(document, lv1, lv2, "kcn:ID", Util.null2str(ediHostSendInfo.get("IF_WEB_ID")));
			/** KCNETPW */
			this.createElement(document, lv1, lv2, "kcn:Password", Util.null2str(ediHostSendInfo.get("IF_WEB_PWD")));
			
//			soapenvBody = this.createElement(document, lv1, soapenvBody, "soapenv:Body", null);
			soapenvBody = this.createElement(document, soapenv, soapenvBody, "soapenv:Body", null);
			
			root = document.createElement("wco:Declaration");
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:1");
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0 ./schema4G/kcs/data/standard/KCS_DeclarationOfCAG_5IGSchemaModule_1.0_standard.xsd");
			soapenvBody.appendChild(root);
			
			/** 세관/과부호 */
			this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(mblData.get("CUSTOMS_CODE")) + Util.null2str(mblData.get("CUSTOMS_DPT_CODE")));
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
					this.createElement(document, lv3, lv4, "wco:GrossMassMeasure", "0.000");
				}else{
					this.createElement(document, lv3, lv4, "wco:GrossMassMeasure", Util.null2str(tempHblData.get("GWT")));
				}
				/** 하기결과중량 */
				this.createElement(document, lv3, lv4, "wco:NetNetWeightMeasure", Util.null2str(tempHblData.get("GWT_ABNORMAL")));
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
				if(!odJobNo.equals(Util.null2str(tempHblData.get("OD_JOB_NO")))){
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
			lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
			if(Util.null2str(mblData.get("ABNORMAL_CODE")).equals("MFN")){
				this.createElement(document, lv2, lv3, "kcs:MasterBLSequenceID", "");
			}else{
				this.createElement(document, lv2, lv3, "kcs:MasterBLSequenceID", ediCommonService.getSubStringK(Util.null2str(mblData.get("MSN")), 0 , 4));
			}
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
	public String[] getSendDataFileCreateAbnormalitiesReportKCNET(Map<String, Object> docSend, Map<String, Object> mblData, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo) throws Exception {
		
		String writeData = null;
		String retData[] = new String[6];
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(mblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
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
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:1");
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0");
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
					this.createElement(document, lv3, lv4, "wco:GrossMassMeasure", "0.000");
				}else{
					this.createElement(document, lv3, lv4, "wco:GrossMassMeasure", Util.null2str(tempHblData.get("GWT")));
				}
				/** 하기결과중량 */
				this.createElement(document, lv3, lv4, "wco:NetNetWeightMeasure", Util.null2str(tempHblData.get("GWT_ABNORMAL")));
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
			lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
			if(Util.null2str(mblData.get("ABNORMAL_CODE")).equals("MFN")){
				this.createElement(document, lv2, lv3, "kcs:MasterBLSequenceID", "");
			}else{
				this.createElement(document, lv2, lv3, "kcs:MasterBLSequenceID", ediCommonService.getSubStringK(Util.null2str(mblData.get("MSN")), 0 , 4));
			}
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
			
			StringBuffer soapData = new StringBuffer();
			soapData.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			soapData.append("<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
			soapData.append("<soapenv:Header>");
			soapData.append("<kcn:Kcnet_Header xmlns:kcn=\"http://www.kcnet.co.kr/kcn\" mustUnderstand=\"0\">");
			soapData.append("<kcn:SenderId mustUnderstand=\"0\">").append(Util.getFileWriteDataWithRtrim(ediCommonService.getDeleteCharStringFwb(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim()), "String", "4")).append("</kcn:SenderId>");
			soapData.append("<kcn:ReceiverId mustUnderstand=\"0\">KCNECP</kcn:ReceiverId>");
			soapData.append("<kcn:DocName mustUnderstand=\"0\">CUSRGD</kcn:DocName>");
			soapData.append("<kcn:DocCode mustUnderstand=\"0\">5IG</kcn:DocCode>");
			soapData.append("<kcn:MessageId mustUnderstand=\"0\">").append(Util.null2str(mblData.get("MBL_NO"))).append("</kcn:MessageId>");
			soapData.append("<kcn:VenderId mustUnderstand=\"0\">YHT</kcn:VenderId>");
			soapData.append("<kcn:ID mustUnderstand=\"0\">").append(Util.null2str(ediHostSendInfo.get("IF_WEB_ID"))).append("</kcn:ID>");
			soapData.append("<kcn:Password mustUnderstand=\"0\">").append(Util.null2str(ediHostSendInfo.get("IF_WEB_PWD"))).append("</kcn:Password>");
			soapData.append("</kcn:Kcnet_Header>");
			soapData.append("</soapenv:Header>");
			soapData.append("<soapenv:Body>");
			
			writeData = soapData.toString() + writeData.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "") + "</soapenv:Body></soapenv:Envelope>";
			
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
}