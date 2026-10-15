package org.yht.exps.express.service.bms.send;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.yht.CatchMailSend;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.BmsMembers;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.EdiBmsSendMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.MrsRet;
import org.yht.exps.express.model.bms.UMrsSendData;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalSaveResult;
import org.yht.exps.express.model.bms.bmsAbnormal.search.SearchCarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalCorr;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalCorrSaveResult;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.search.SearchCarryInAbnormalCorr;
import org.yht.exps.express.model.bms.bmsInCorrection.BatchBmsInCorrection;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChange;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChangeSaveResult;
import org.yht.exps.express.model.bms.bmsInCorrection.search.SearchInCorrection;
import org.yht.exps.express.model.bms.bmsOutCorrection.BatchBmsOutCorrection;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChange;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChangeSaveResult;
import org.yht.exps.express.model.bms.bmsOutCorrection.search.SearchOutCorrection;
import org.yht.exps.express.model.bms.bmsOutNotice.BatchBmsOutNotice;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNotice;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticeSaveResult;
import org.yht.exps.express.model.bms.bmsOutNotice.search.SearchBmsOutNotice;
import org.yht.exps.express.model.bms.carryin.BatchCarryIn;
import org.yht.exps.express.model.bms.carryin.CarryInSaveResult;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.out.BatchBmsOut;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutSaveResult;
import org.yht.exps.express.model.bms.out.search.SearchBmsOut;
import org.yht.exps.express.model.bms.send.xml.GOVCBR5II.OutListGOVCBR5II;
import org.yht.exps.express.model.bms.send.xml.GOVCBR5LC.InGOVCBR5LC;
import org.yht.exps.express.model.bms.send.xml.GOVCBR5LD.OutGOVCBR5LD;
import org.yht.exps.express.model.bms.send.xml.GOVCBR632.InDecrGOVCBR632;
import org.yht.exps.express.model.bms.send.xml.GOVCBR6NB.OutDecrGOVCBR6NB;
import org.yht.exps.express.model.bms.send.xml.GOVCBRDKO.DeclGOVCBRDKO;
import org.yht.exps.express.model.bms.send.xml.GOVCBRDKP.DeclGOVCBRDKP;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.bms.BmsCargoAbnormalService;
import org.yht.exps.express.service.bms.BmsCommService;
import org.yht.exps.express.service.bms.BmsSendUtils;
import org.yht.util.DateTime;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BmsSendXmlParser {

	@Autowired
	SqlSessionTemplate sqlSessionTemplate;

	@Autowired
	PlatformTransactionManager transactionManager;

	@Autowired
	private CustomProperties customProperties;

	@Autowired
	RestHighLevelClient client;

	@Autowired
	EdiBmsSendMapper ediBmsSendMapper;

    @Autowired
    CommonMapper commonMapper;

	@Autowired
	BmsSendUtils sendUtils;

    @Autowired
	CatchMailSend catchMailSend;

	@Autowired
	BmsCommService bmsCommService;
	
	@Autowired
	BmsCargoAbnormalService bmsCargoAbnormalService;

	private static BmsMembers bmsMembers = new BmsMembers(); /** Thread 사용 사업장 리스트 정적 변수 */
	private int batchContInsertSize = 500;

    /**
     * 01.GOVCBR632 MRS 서버 전송및 송신후 결과 표시 XML Parser 호출
     * @param entCode
     * @param entBusinessCode
     * @param inNoList
     * @return
     * @throws Exception
     */
	public CarryInSaveResult callSendXmlGOVCBR632ParserExecutor(BatchCarryIn params, HttpServletRequest request) throws Exception {
    	String entCode = params.getEntCode();
		String entBusinessCode = params.getEntBusinessCode();
		String logUserBusinessPrintName = params.getLogUserBusinessPrintName();
		String logUserName = params.getLogUserName();
		String logUserNameEng = params.getLogUserNameEng();
		List<InventoryIn> bmsCarryInList = params.getBmsCarryInList();
		CarryInSaveResult result = new CarryInSaveResult();

		Integer nSuccCnt = 0;
		Integer nFailCnt = 0;
		String mrsSendErrorMsg = "";

		try {
			SqlSession sqlSession    = null;
			TransactionStatus status = null;

			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
				status = transactionManager.getTransaction(new DefaultTransactionDefinition());
				EdiBmsSendMapper bmsSendMapper = sqlSession.getMapper(EdiBmsSendMapper.class);

				// 토큰값 얻어오기
				String ediPortalMailboxId = sendUtils.getEdiPortalMailboxId(entCode, entBusinessCode);
				if(!sendUtils.isOnline(customProperties.getEbmsIp(), Integer.parseInt(customProperties.getEbmsPort()))) {
					log.warn("###################################### Failed to access EDI Portal ######################################");
					result.setErrorMsg("관세청 연결 오류가 발생하였습니다.");
					return result;
				}

				// XML 마샬링용 변수
				String sendXml = "";
				String jaxbSchemaLocation="urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_632SchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfCAG_632SchemaModule_1.0_standard.xsd";
				String strkcs = "xmlns:kcs=\"urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_632SchemaModule:1:0\"";
				String token = sendUtils.getMrsToken(ediPortalMailboxId);

				SearchInList search = new SearchInList();
				List<Integer> inNoList = new ArrayList<Integer>();

				for(InventoryIn in : bmsCarryInList) inNoList.add(in.getInNo());
				search.setInNoList(inNoList);
				List<InventoryIn>  lst = ediBmsSendMapper.selectEdiBmsInSendList(search);

				UMrsSendData sendData;
				InDecrGOVCBR632 in632Vo = new InDecrGOVCBR632();
				List<UMrsSendData> sndList = new ArrayList<UMrsSendData>();

				Map<String, Object> succDbUpdateMap = null;
				
				// 마샬링
				for(InventoryIn in : lst) {
					if(in.getLastPodStatus().equals("BMS01") || in.getLastPodStatus().equals("BMS02")) {
						in632Vo = sendUtils.getBmsInToGOVCBR632(in);
						sendXml = sendUtils.sendVoToXmlMarshal(in632Vo, jaxbSchemaLocation, strkcs);
						sendData = new UMrsSendData();
						sendData.setKeyId(in.getInNo());
						sendData.setErrDescKey(in.getBlNo()); // 에러메시지용 key
						sendData.setEdiPortalMailboxId(ediPortalMailboxId);
						sendData.setToken(token);
						sendData.setCustomsReportNo(in.getCustomsReportNo()); // 반입신고번호
						sendData.setDoucmentCode(in.getTypeCode());
						sendData.setXml(sendXml);
						sendData.setRegisterUserId(in.getRegisterUserId()); // 내부 로그용
						sendData.setOdJobNo(in.getOdJobNo()); // 작업내역 조회용 key
						sendData.setEsJsonJobLogList(in);  // ES JOB LOG
						sndList.add(sendData);
						
						//전송전에 BMS03(전송)을 업데이트 함. BMS04 접수가 먼저 수신 받을 수 있음 2025-10-03 yjkim
						succDbUpdateMap = new HashMap<>();
						succDbUpdateMap.put("inNo", in.getInNo());
						succDbUpdateMap.put("updateUserId", in.getRegisterUserId());
						bmsSendMapper.batchUpdateEdiBmsInSendSucc(succDbUpdateMap);
						
					} else {
						nFailCnt++;
					}
				}

				// MRS 서버로 1건씩 XML 전송 처리
				List<Map<String, Object>> succMrsSendArray = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> errMrsSendArray = new ArrayList<Map<String, Object>>();
				Map<String, Object> succMrsSendMap;
				Map<String, Object> errMrsSendMap;

				for(UMrsSendData send: sndList) {
					MrsRet mrsRet = sendUtils.sendMrsServerByHttp(send);
					send.setCustomsErrCode(mrsRet.getCustomsErrCode());
					send.setCustomsErrDesc(mrsRet.getCustomsErrDesc());
					send.setCustomsErrDescAdditional(mrsRet.getBody()); // 추가 Error 코드

					succMrsSendMap = new HashMap<String, Object>();
					errMrsSendMap = new HashMap<String, Object>();

					if("C200".equals(send.getCustomsErrCode())) {
						nSuccCnt++;
						succMrsSendMap.put("inNo", send.getKeyId()); // 송신성공(BMS03) UPDATE 키값 수집
						succMrsSendMap.put("customsReportNo", send.getCustomsReportNo());
						succMrsSendMap.put("odJobNo", send.getOdJobNo());
						succMrsSendMap.put("podName", "/성공/" + Util.null2str(mrsRet.getCustomsErrCode()));
						succMrsSendMap.put("remarks", Util.null2str(send.getCustomsReportNo()));
						succMrsSendMap.put("esJsonJobLogList", send.getEsJsonJobLogList());
						succMrsSendMap.put("updateUserId", send.getRegisterUserId());
						succMrsSendArray.add(succMrsSendMap);
					} else {
						nFailCnt++;
						errMrsSendMap.put("inNo", send.getKeyId()); // 송신실패(BMS02) UPDATE 키값 수집
						errMrsSendMap.put("customsReportNo", send.getCustomsReportNo());
						errMrsSendMap.put("odJobNo", send.getOdJobNo());
						errMrsSendMap.put("podName", "반입전송(수기)/실패/" + Util.null2str(mrsRet.getCustomsErrCode()));
						errMrsSendMap.put("remarks", Util.null2str(send.getCustomsReportNo()));
						errMrsSendMap.put("esJsonJobLogList", send.getEsJsonJobLogList());
						errMrsSendMap.put("updateUserId", send.getRegisterUserId());
						errMrsSendArray.add(errMrsSendMap);
						log.error("############################### [반입신고 GOVCBR632] UMRS SEND FAILED (BMS02): {}", send); // SEND FAILED 로그
						mrsSendErrorMsg += "[BL번호 " + send.getErrDescKey() + "] " + send.getCustomsErrDescAdditional() + " (Error Code: " + send.getCustomsErrCode() + ")<br/>";
					}
				}

				// DB Batch Update
				int succCnt = 0;
//				Map<String, Object> succDbUpdateMap;

				for(Map<String, Object> succMap : succMrsSendArray) {
					succCnt++;
//					succDbUpdateMap = new HashMap<>();
//					succDbUpdateMap.put("inNo", succMap.get("inNo"));
//					succDbUpdateMap.put("updateUserId", succMap.get("updateUserId"));
//					bmsSendMapper.batchUpdateEdiBmsInSendSucc(succDbUpdateMap);

					/** 작업내역 로그 */
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(entCode);
					processLogs.setEntBusinessCode(entBusinessCode);
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(succMap.get("updateUserId")));
					processLogs.setLogJobType("DATA");
		    		processLogs.setBound("AIR");
		    		processLogs.setBoundType("IMP");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
					processLogs.setDbType(BmsConsts.DbType.BMS_IN);
					processLogs.setDescription(BmsConsts.DbType.BMS_IN + " [SEND 신고번호: " + String.valueOf(succMap.get("customsReportNo")) + "]");
					processLogs.setJobNo(String.valueOf(succMap.get("odJobNo")));
					processLogs.setJobLog(new Gson().toJson(succMap.get("esJsonJobLogList")));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(logUserBusinessPrintName);
					processLogs.setProcessUserName(logUserName);
					processLogs.setProcessUserNameEng(logUserNameEng);
					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.SEND, Util.null2str(succMap.get("customsReportNo")), succMap);

					if(succCnt % batchContInsertSize == 0 || succCnt == succMrsSendArray.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}

				int errCnt = 0;
				Map<String, Object> errDbUpdateMap;

				for(Map<String, Object> errMap : errMrsSendArray) {
					errCnt++;
					errDbUpdateMap = new HashMap<>();
					errDbUpdateMap.put("inNo", errMap.get("inNo"));
					errDbUpdateMap.put("updateUserId", errMap.get("updateUserId"));
					bmsSendMapper.batchUpdateEdiBmsInSendErr(errDbUpdateMap);

					errMap.put("entCode", entCode);
					bmsCommService.insertPod5(errMap);
					
//					/** 작업내역 로그 -> 2024-08-01, 전송 실패건은 POD 업데이트 하지 않음 */
//					ProcessLogs processLogs = new ProcessLogs();
//					processLogs.setEntCode(entCode);
//					processLogs.setEntBusinessCode(entBusinessCode);
//					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
//					processLogs.setProcessUserId(Util.null2str(errMap.get("updateUserId")));
//					processLogs.setLogJobType("DATA");
//		    		processLogs.setBound("AIR");
//		    		processLogs.setBoundType("IMP");
//					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
//					processLogs.setDbType(BmsConsts.DbType.BMS_IN);
//					processLogs.setDescription(BmsConsts.DbType.BMS_IN + " [SEND 신고번호: " + String.valueOf(errMap.get("customsReportNo")) + "]");
//					processLogs.setJobNo(String.valueOf(errMap.get("odJobNo")));
//					processLogs.setJobLog(new Gson().toJson(errMap.get("esJsonJobLogList")));
//					processLogs.setResult("ok");
//					processLogs.setEntBusinessPrintName(logUserBusinessPrintName);
//					processLogs.setProcessUserName(logUserName);
//					processLogs.setProcessUserNameEng(logUserNameEng);
//					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.SEND, "", errMap);

					if(errCnt % batchContInsertSize == 0 || errCnt == errMrsSendArray.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				transactionManager.commit(status);
			} catch (Exception e) {
				if(transactionManager != null) transactionManager.rollback(status);
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			} finally {
				if(sqlSession != null) sqlSession.close();
			}
		} catch(Exception e) {
			log.error(e.getMessage());
			String stuff = new Object(){}.getClass().getName() + new Object(){}.getClass().getEnclosingMethod().getName();
			result.setErrorMsg("System Error.");
		} finally {
			bmsMembers.removeSendXmlGOVCBR632ParserUser(entCode);
			if (bmsMembers.getSendXmlGOVCBR632ParserUser(entCode) != null) {
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR632ParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)");
				bmsMembers.removeSendXmlGOVCBR632ParserUser(entCode);
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR632ParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)2");
			}
		}

		result.setSuccCnt(nSuccCnt);
		result.setFailCnt(nFailCnt);
		result.setMrsSendErrorMsg(mrsSendErrorMsg);

		return result;
	}

	/**
	 * GOVCBR6NB(반출신고서) MRS 서버 전송및 송신후 결과 표시 XML Parser 호출
	 * @param entCode
	 * @param entBusinessCode
	 * @param outNoList
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult callSendXmlGOVCBR6NBParserExecutor(BatchBmsOut params, HttpServletRequest request) throws Exception {
		String entCode = params.getEntCode();
		String entBusinessCode = params.getEntBusinessCode();
		String logUserBusinessPrintName = params.getLogUserBusinessPrintName();
		String logUserName = params.getLogUserName();
		String logUserNameEng = params.getLogUserNameEng();
		String bmsWhCode = params.getBmsWhCode();
		List<BmsOut> bmsOutList = params.getBmsOutList();
		BmsOutSaveResult result = new BmsOutSaveResult();

		Integer nSuccCnt = 0;
		Integer nFailCnt = 0;
		Integer mFailCnt = 0;
		String mrsSendErrorMsg = "";

		try {
			SqlSession sqlSession = null;
			TransactionStatus status = null;

			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
				status = transactionManager.getTransaction(new DefaultTransactionDefinition());
				EdiBmsSendMapper bmsSendMapper = sqlSession.getMapper(EdiBmsSendMapper.class);

				// 토큰값 얻어오기
				String ediPortalMailboxId = sendUtils.getEdiPortalMailboxId(entCode, entBusinessCode);
				if(!sendUtils.isOnline(customProperties.getEbmsIp(), Integer.parseInt(customProperties.getEbmsPort()))) {
					log.warn("###################################### Failed to access EDI Portal ######################################");
					result.setErrorMsg("관세청 연결 오류가 발생하였습니다.");
					return result;
				}

				// XML 마샬링용 변수
				String sendXml = "";
				String jaxbSchemaLocation="urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_6NBSchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfCAG_6NBSchemaModule_1.0_standard.xsd";
				String strkcs = "xmlns:kcs=\"urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_6NBSchemaModule:1:0\"";
				if(ediPortalMailboxId.equals("TEST_DEFAULT") || ediPortalMailboxId.equals("")) {
					result.setErrorMsg("관세청 전송중 시스템 오류가 발생하였습니다.");
					return result;
				}
				String token = sendUtils.getMrsToken(ediPortalMailboxId);

				SearchBmsOut search = new SearchBmsOut();
				List<Integer> outNoList = new ArrayList<Integer>();

				for(BmsOut out : bmsOutList) outNoList.add(out.getOutNo());
				search.setOutNoList(outNoList);
				List<BmsOut> lst = ediBmsSendMapper.selectEdiBmsOutSendList(search);

				UMrsSendData sendData;
				OutDecrGOVCBR6NB out6nbVo = new OutDecrGOVCBR6NB();
				List<UMrsSendData> sndList = new ArrayList<UMrsSendData>();
				Map<String, Object> succDbUpdateMap = null;
				
				// 마샬링
				for(BmsOut out : lst) {
					if( ! "04077009".equals(bmsWhCode) && "61".equals(out.getOutClass()) && "04002001".equals(out.getOutApprovalNo()) && "N".equals(out.getTicOutScanYn()) ) {
						result.setMoveOutFailYn("Y");
						mFailCnt++;
					} else if(out.getLastPodStatus().equals("BMS01") || out.getLastPodStatus().equals("BMS02")) {
						out6nbVo = sendUtils.getBmsOutToGOVCBR6NB(out);
						sendXml = sendUtils.sendVoToXmlMarshal(out6nbVo, jaxbSchemaLocation, strkcs);
						sendData = new UMrsSendData();
						sendData.setKeyId(out.getOutNo());
						sendData.setErrDescKey(out.getBlNo()); // 에러메시지용 key
						sendData.setEdiPortalMailboxId(ediPortalMailboxId);
						sendData.setToken(token);
						sendData.setCustomsReportNo(out.getCustomsReportNo()); // 반출신고번호
						sendData.setDoucmentCode(out.getTypeCode());
						sendData.setXml(sendXml);
						sendData.setRegisterUserId(out.getRegisterUserId()); // 내부 로그용
						sendData.setOdJobNo(out.getOdJobNo()); // 작업내역 조회용 key
						sendData.setEsJsonJobLogList(out);  // ES JOB LOG
						sndList.add(sendData);
						
						//전송 상태를 mrs 전송전에 업데이트 한다. 접수 수신이 먼저 될 수 있음. 2025-10-04 yjkim
						succDbUpdateMap = new HashMap<>();
						succDbUpdateMap.put("outNo", out.getOutNo());
						succDbUpdateMap.put("updateUserId", out.getRegisterUserId());
						bmsSendMapper.batchUpdateEdiBmsOutSendSucc(succDbUpdateMap);
					} else {
						nFailCnt++;
					}
				}

				// MRS 서버로 1건씩 XML 전송 처리
				List<Map<String, Object>> succMrsSendArray = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> errMrsSendArray = new ArrayList<Map<String, Object>>();
				Map<String, Object> succMrsSendMap;
				Map<String, Object> errMrsSendMap;

				for(UMrsSendData send: sndList) {
					MrsRet mrsRet = sendUtils.sendMrsServerByHttp(send);
					send.setCustomsErrCode(mrsRet.getCustomsErrCode());
					send.setCustomsErrDesc(mrsRet.getCustomsErrDesc());
					send.setCustomsErrDescAdditional(mrsRet.getBody()); // 추가 Error 코드

					succMrsSendMap = new HashMap<String, Object>();
					errMrsSendMap = new HashMap<String, Object>();

					if ("C200".equals(send.getCustomsErrCode())) {
						nSuccCnt++;
						succMrsSendMap.put("outNo", send.getKeyId()); // 송신성공(BMS03) UPDATE 키값 수집
						succMrsSendMap.put("customsReportNo", send.getCustomsReportNo());
						succMrsSendMap.put("odJobNo", send.getOdJobNo());
						succMrsSendMap.put("esJsonJobLogList", send.getEsJsonJobLogList());
						succMrsSendMap.put("updateUserId", send.getRegisterUserId());
						succMrsSendArray.add(succMrsSendMap);
					} else {
						nFailCnt++;
						errMrsSendMap.put("outNo", send.getKeyId()); // 송신실패(BMS02) UPDATE 키값 수집
						errMrsSendMap.put("customsReportNo", send.getCustomsReportNo());
						errMrsSendMap.put("odJobNo", send.getOdJobNo());
						errMrsSendMap.put("esJsonJobLogList", send.getEsJsonJobLogList());
						errMrsSendMap.put("updateUserId", send.getRegisterUserId());
						errMrsSendArray.add(errMrsSendMap);
						log.error("############################### [반출통고신고 GOVCBR6NB] UMRS SEND FAILED (BMS02): {}", send); // SEND FAILED 로그
						mrsSendErrorMsg += "[BL번호 " + send.getErrDescKey() + "] " + send.getCustomsErrDescAdditional() + " (Error Code: " + send.getCustomsErrCode() + ")<br/>";
					}
				}

				// DB Batch Update
				int succCnt = 0;
//				Map<String, Object> succDbUpdateMap;

				for(Map<String, Object> succMap : succMrsSendArray) {
					succCnt++;
//					succDbUpdateMap = new HashMap<>();
//					succDbUpdateMap.put("outNo", succMap.get("outNo"));
//					succDbUpdateMap.put("updateUserId", succMap.get("updateUserId"));
//					bmsSendMapper.batchUpdateEdiBmsOutSendSucc(succDbUpdateMap);

					/** 작업내역 로그 */
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(entCode);
					processLogs.setEntBusinessCode(entBusinessCode);
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(succMap.get("updateUserId")));
					processLogs.setLogJobType("DATA");
		    		processLogs.setBound("AIR");
		    		processLogs.setBoundType("IMP");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
					processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
					processLogs.setDescription(BmsConsts.DbType.BMS_OUT + " [SEND 신고번호: " + String.valueOf(succMap.get("customsReportNo")) + "]");
					processLogs.setJobNo(String.valueOf(succMap.get("odJobNo")));
					processLogs.setJobLog(new Gson().toJson(succMap.get("esJsonJobLogList")));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(logUserBusinessPrintName);
					processLogs.setProcessUserName(logUserName);
					processLogs.setProcessUserNameEng(logUserNameEng);
					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.SEND, "", succMap);

					if(succCnt % batchContInsertSize == 0 || succCnt == succMrsSendArray.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}

				int errCnt = 0;
				Map<String, Object> errDbUpdateMap;

				for(Map<String, Object> errMap : errMrsSendArray) {
					errCnt++;
					errDbUpdateMap = new HashMap<>();
					errDbUpdateMap.put("outNo", errMap.get("outNo"));
					errDbUpdateMap.put("updateUserId", errMap.get("updateUserId"));
					bmsSendMapper.batchUpdateEdiBmsOutSendErr(errDbUpdateMap);

//					/** 작업내역 로그 -> 2024-08-01, 전송 실패건은 POD 업데이트 하지 않음 */
//					ProcessLogs processLogs = new ProcessLogs();
//					processLogs.setEntCode(entCode);
//					processLogs.setEntBusinessCode(entBusinessCode);
//					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
//					processLogs.setProcessUserId(Util.null2str(errMap.get("updateUserId")));
//					processLogs.setLogJobType("DATA");
//		    		processLogs.setBound("AIR");
//		    		processLogs.setBoundType("IMP");
//					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
//					processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
//					processLogs.setDescription(BmsConsts.DbType.BMS_OUT + " [SEND 신고번호: " + String.valueOf(errMap.get("customsReportNo")) + "]");
//					processLogs.setJobNo(String.valueOf(errMap.get("odJobNo")));
//					processLogs.setJobLog(new Gson().toJson(errMap.get("esJsonJobLogList")));
//					processLogs.setResult("ok");
//					processLogs.setEntBusinessPrintName(logUserBusinessPrintName);
//					processLogs.setProcessUserName(logUserName);
//					processLogs.setProcessUserNameEng(logUserNameEng);
//					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.SEND, "", errMap);

					if(errCnt % batchContInsertSize == 0 || errCnt == errMrsSendArray.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				transactionManager.commit(status);
			} catch (Exception e) {
				if(transactionManager != null)	transactionManager.rollback(status);
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			} finally {
				if(sqlSession != null)	sqlSession.close();
			}
		} catch(Exception e) {
			log.error(e.getMessage());
			String stuff = new Object(){}.getClass().getName() + new Object(){}.getClass().getEnclosingMethod().getName();
			result.setErrorMsg("관세청 전송중 시스템 오류가 발생하였습니다.");
		} finally {
			bmsMembers.removeSendXmlGOVCBR6NBParserUser(entCode);
			if(bmsMembers.getSendXmlGOVCBR6NBParserUser(entCode) != null) {
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR6NBParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)");
				bmsMembers.removeSendXmlGOVCBR6NBParserUser(entCode);
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR6NBParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)2");
			}
		}

		result.setResultCnt(nSuccCnt);
		result.setFailCnt(nFailCnt);
		result.setMoveFailCnt(mFailCnt);
		result.setMrsSendErrorMsg(mrsSendErrorMsg);

		return result;
	}

	/**
	 * GOVCBR5II(반출통고목록보고서) MRS 서버 전송및 송신후 결과 표시 XML Parser 호출
	 * @param entCode
	 * @param entBusinessCode
	 * @param outNoticeNoList
	 * @return
	 */
	public BmsOutNoticeSaveResult callSendXmlGOVCBR5IIParserExecutor(BatchBmsOutNotice params, HttpServletRequest request) throws Exception {
		String entCode = params.getEntCode();
		String entBusinessCode = params.getEntBusinessCode();
		String logUserBusinessPrintName = params.getLogUserBusinessPrintName();
		String logUserName = params.getLogUserName();
		String logUserNameEng = params.getLogUserNameEng();
		List<BmsOutNotice> bmsOutNoticeList = params.getBmsOutNoticeList();
		BmsOutNoticeSaveResult result = new BmsOutNoticeSaveResult();

		Integer nSuccCnt = 0;
		Integer nFailCnt = 0;
		String mrsSendErrorMsg = "";

		try {
			SqlSession sqlSession = null;
			TransactionStatus status = null;

			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
				status = transactionManager.getTransaction(new DefaultTransactionDefinition());
				EdiBmsSendMapper bmsSendMapper = sqlSession.getMapper(EdiBmsSendMapper.class);

				// 토큰값 얻어오기
				String ediPortalMailboxId = sendUtils.getEdiPortalMailboxId(entCode, entBusinessCode);
				if(!sendUtils.isOnline(customProperties.getEbmsIp(), Integer.parseInt(customProperties.getEbmsPort()))) {
					log.warn("###################################### Failed to access EDI Portal ######################################");
					result.setErrorMsg("관세청 연결 오류가 발생하였습니다.");
					return result;
				}

				// XML 마샬링용 변수
				String sendXml = "";
				String jaxbSchemaLocation="urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IISchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfCAG_5IISchemaModule_1.0_standard.xsd";
				String strkcs = "xmlns:kcs=\"urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IISchemaModule:1:0\"";
				String token = sendUtils.getMrsToken(ediPortalMailboxId);

				SearchBmsOutNotice search = new SearchBmsOutNotice();
				List<Integer> outNoticeNoList = new ArrayList<Integer>();

				for(BmsOutNotice outNotice : bmsOutNoticeList) outNoticeNoList.add(outNotice.getOutNoticeNo());
				search.setOutNoticeNoList(outNoticeNoList);
				search.setEntBusinessCode(entBusinessCode); // 세관과코드 조회용
				List<BmsOutNotice> lst = ediBmsSendMapper.selectEdiBmsOutNoticeSendList(search);

				UMrsSendData sendData;
				OutListGOVCBR5II out5II = new OutListGOVCBR5II();
				List<UMrsSendData> sndList = new ArrayList<UMrsSendData>();

				Map<String, Object> succDbUpdateMap = null;

				// 마샬링
				for(BmsOutNotice outNotice : lst) {
					if(outNotice.getLastPodStatus().equals("BMS01") || outNotice.getLastPodStatus().equals("BMS02")) {
						out5II = sendUtils.getBmsOutNoticeToGOVCBR5II(outNotice);
						sendXml = sendUtils.sendVoToXmlMarshal(out5II, jaxbSchemaLocation, strkcs);
						sendData = new UMrsSendData();
						sendData.setKeyId(outNotice.getOutNoticeNo());
						sendData.setErrDescKey(outNotice.getBlNo()); // 에러메시지용 key
						sendData.setEdiPortalMailboxId(ediPortalMailboxId);
						sendData.setToken(token);
						sendData.setCustomsReportNo(outNotice.getCargoReferenceNo()); // 목록보고서는 Declaration/ID가 화물관리번호
						sendData.setDoucmentCode(outNotice.getTypeCode());
						sendData.setXml(sendXml);
						sendData.setRegisterUserId(outNotice.getRegisterUserId()); // 내부 로그용
						sendData.setOdJobNo(outNotice.getOdJobNo()); // 작업내역 조회용 key
						sendData.setEsJsonJobLogList(outNotice); // ES JOB LOG
						sndList.add(sendData);
						
						//접수 후 송신상태로 업데이트 되는 문제로 전송전에 송신업데이트 하는 걸로 수정함. 2025-10-13 yjkim
						succDbUpdateMap = new HashMap<>();
						succDbUpdateMap.put("outNoticeNo", outNotice.getOutNoticeNo());
						succDbUpdateMap.put("updateUserId", outNotice.getRegisterUserId());
						bmsSendMapper.batchUpdateEdiBmsOutNoticeSendSucc(succDbUpdateMap);
						
					} else {
						nFailCnt++;
					}
				}

				// MRS 서버로 1건씩 XML 전송 처리
				List<Map<String, Object>> succMrsSendArray = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> errMrsSendArray = new ArrayList<Map<String, Object>>();
				Map<String, Object> succMrsSendMap;
				Map<String, Object> errMrsSendMap;

				for (UMrsSendData send: sndList) {
					MrsRet mrsRet = sendUtils.sendMrsServerByHttp(send);
					send.setCustomsErrCode(mrsRet.getCustomsErrCode());
					send.setCustomsErrDesc(mrsRet.getCustomsErrDesc());
					send.setCustomsErrDescAdditional(mrsRet.getBody()); // 추가 Error 코드

					succMrsSendMap = new HashMap<String, Object>();
					errMrsSendMap = new HashMap<String, Object>();

					if ("C200".equals(send.getCustomsErrCode())) {
						nSuccCnt++;
						succMrsSendMap.put("outNoticeNo", send.getKeyId()); // 송신성공(BMS03) UPDATE 키값 수집
						succMrsSendMap.put("customsReportNo", send.getCustomsReportNo());
						succMrsSendMap.put("odJobNo", send.getOdJobNo());
						succMrsSendMap.put("esJsonJobLogList", send.getEsJsonJobLogList());
						succMrsSendMap.put("updateUserId", send.getRegisterUserId());
						succMrsSendArray.add(succMrsSendMap);
					} else {
						nFailCnt++;
						errMrsSendMap.put("outNoticeNo", send.getKeyId()); // 송신실패(BMS02) UPDATE 키값 수집
						errMrsSendMap.put("customsReportNo", send.getCustomsReportNo());
						errMrsSendMap.put("odJobNo", send.getOdJobNo());
						errMrsSendMap.put("esJsonJobLogList", send.getEsJsonJobLogList());
						errMrsSendMap.put("updateUserId", send.getRegisterUserId());
						errMrsSendArray.add(errMrsSendMap);
						log.error("############################### [반출통고신고 GOVCBR5II] UMRS SEND FAILED (BMS02): {}", send); // SEND FAILED 로그
						mrsSendErrorMsg += "[BL번호 " + send.getErrDescKey() + "] " + send.getCustomsErrDescAdditional() + " (Error Code: " + send.getCustomsErrCode() + ")<br/>";
					}
				}

				// DB Batch Update
				int succCnt = 0;
//				Map<String, Object> succDbUpdateMap;

				for(Map<String, Object> succMap : succMrsSendArray) {
					succCnt++;
//					succDbUpdateMap = new HashMap<>();
//					succDbUpdateMap.put("outNoticeNo", succMap.get("outNoticeNo"));
//					succDbUpdateMap.put("updateUserId", succMap.get("updateUserId"));
//					bmsSendMapper.batchUpdateEdiBmsOutNoticeSendSucc(succDbUpdateMap);

					/** 작업내역 로그 */
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(entCode);
					processLogs.setEntBusinessCode(entBusinessCode);
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(succMap.get("updateUserId")));
					processLogs.setLogJobType("DATA");
		    		processLogs.setBound("AIR");
		    		processLogs.setBoundType("IMP");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
					processLogs.setDbType(BmsConsts.DbType.BMS_OUT_NOTICE);
					processLogs.setDescription(BmsConsts.DbType.BMS_OUT_NOTICE + " [SEND 신고번호: " + String.valueOf(succMap.get("customsReportNo")) + "]"); // 반출통고는 신고번호가 화물관리번호
					processLogs.setJobNo(String.valueOf(succMap.get("odJobNo")));
					processLogs.setJobLog(new Gson().toJson(succMap.get("esJsonJobLogList")));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(logUserBusinessPrintName);
					processLogs.setProcessUserName(logUserName);
					processLogs.setProcessUserNameEng(logUserNameEng);
					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.SEND, "", succMap);

					if(succCnt % batchContInsertSize == 0 || succCnt == succMrsSendArray.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}

				int errCnt = 0;
				Map<String, Object> errDbUpdateMap;

				for(Map<String, Object> errMap : errMrsSendArray) {
					errCnt++;
					errDbUpdateMap = new HashMap<>();
					errDbUpdateMap.put("outNoticeNo", errMap.get("outNoticeNo"));
					errDbUpdateMap.put("updateUserId", errMap.get("updateUserId"));
					bmsSendMapper.batchUpdateEdiBmsOutNoticeSendErr(errDbUpdateMap);

//					/** 작업내역 로그 -> 2024-08-01, 전송 실패건은 POD 업데이트 하지 않음 */
//					ProcessLogs processLogs = new ProcessLogs();
//					processLogs.setEntCode(entCode);
//					processLogs.setEntBusinessCode(entBusinessCode);
//					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
//					processLogs.setProcessUserId(Util.null2str(errMap.get("updateUserId")));
//					processLogs.setLogJobType("DATA");
//		    		processLogs.setBound("AIR");
//		    		processLogs.setBoundType("IMP");
//					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
//					processLogs.setDbType(BmsConsts.DbType.BMS_OUT_NOTICE);
//					processLogs.setDescription(BmsConsts.DbType.BMS_OUT_NOTICE + " [SEND 신고번호: " + String.valueOf(errMap.get("customsReportNo")) + "]"); // 반출통고는 신고번호가 화물관리번호
//					processLogs.setJobNo(String.valueOf(errMap.get("odJobNo")));
//					processLogs.setJobLog(new Gson().toJson(errMap.get("esJsonJobLogList")));
//					processLogs.setResult("ok");
//					processLogs.setEntBusinessPrintName(logUserBusinessPrintName);
//					processLogs.setProcessUserName(logUserName);
//					processLogs.setProcessUserNameEng(logUserNameEng);
//					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.SEND, "", errMap);

					if(errCnt % batchContInsertSize == 0 || errCnt == errMrsSendArray.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				transactionManager.commit(status);
			} catch (Exception e) {
				if(transactionManager != null)	transactionManager.rollback(status);
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			} finally {
				if(sqlSession != null)	sqlSession.close();
			}
		} catch(Exception e) {
			log.error(e.getMessage());
			String stuff = new Object(){}.getClass().getName() + new Object(){}.getClass().getEnclosingMethod().getName();
			result.setErrorMsg("System Error.");
		} finally {
			bmsMembers.removeSendXmlGOVCBR5IIParserUser(entCode);
			if(bmsMembers.getSendXmlGOVCBR5IIParserUser(entCode) != null) {
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR5IIParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)");
				bmsMembers.removeSendXmlGOVCBR5IIParserUser(entCode);
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR5IIParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)2");
			}
		}

		result.setSuccCnt(nSuccCnt);
		result.setFailCnt(nFailCnt);
		result.setMrsSendErrorMsg(mrsSendErrorMsg);

		return result;
	}

	/**
	 * GOVCBR5LC(반입신고정정신청서) MRS 서버 전송및 송신후 결과 표시 XML Parser 호출
	 * @param entCode
	 * @param entBusinessCode
	 * @param changeNoList
	 * @return
	 */
	public CarryInChangeSaveResult callSendXmlGOVCBR5LCParserExecutor(BatchBmsInCorrection params, HttpServletRequest request) throws Exception {
		String entCode = params.getEntCode();
		String entBusinessCode = params.getEntBusinessCode();
		String logUserBusinessPrintName = params.getLogUserBusinessPrintName();
		String logUserName = params.getLogUserName();
		String logUserNameEng = params.getLogUserNameEng();
		List<CarryInChange> bmsCarryInChangeList = params.getBmsCarryInChangeList();
		CarryInChangeSaveResult result = new CarryInChangeSaveResult();

		Integer nSuccCnt = 0;
		Integer nFailCnt = 0;
		String mrsSendErrorMsg = "";

		try {
			SqlSession sqlSession = null;
			TransactionStatus status = null;

			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
				status = transactionManager.getTransaction(new DefaultTransactionDefinition());
				EdiBmsSendMapper bmsSendMapper = sqlSession.getMapper(EdiBmsSendMapper.class);

				// 토큰값 얻어오기
				String ediPortalMailboxId = sendUtils.getEdiPortalMailboxId(entCode, entBusinessCode);
				if(!sendUtils.isOnline(customProperties.getEbmsIp(), Integer.parseInt(customProperties.getEbmsPort()))) {
					log.warn("###################################### Failed to access EDI Portal ######################################");
					result.setErrorMsg("관세청 연결 오류가 발생하였습니다.");
					return result;
				}

				// XML 마샬링용 변수
				String sendXml = "";
				String jaxbSchemaLocation="urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LCSchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfCAG_5LCSchemaModule_1.0_standard.xsd";
				String strkcs = "xmlns:kcs=\"urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LCSchemaModule:1:0\"";
				String token = sendUtils.getMrsToken(ediPortalMailboxId);

				SearchInCorrection search = new SearchInCorrection();
				List<Integer> changeNoList = new ArrayList<Integer>();

				for(CarryInChange change : bmsCarryInChangeList) changeNoList.add(change.getChangeNo());
				search.setChangeNoList(changeNoList);
				List<CarryInChange> lst = ediBmsSendMapper.selectCarryInChangeList(search);

				UMrsSendData sendData;
				InGOVCBR5LC in5LC = new InGOVCBR5LC();
				List<UMrsSendData> sndList = new ArrayList<UMrsSendData>();

				Map<String, Object> succDbUpdateMap = null;
				
				// 마샬링
				for(CarryInChange change : lst) {
					if(change.getLastPodStatus().equals("BMS01") || change.getLastPodStatus().equals("BMS02")) {
						in5LC = sendUtils.getBmsChangeToGOVCBR5LC(change);
						sendXml = sendUtils.sendVoToXmlMarshal(in5LC, jaxbSchemaLocation, strkcs);
						sendData = new UMrsSendData();
						sendData.setKeyId(change.getChangeNo());
						sendData.setErrDescKey(change.getCustomsReportNo()); // 에러메시지용 key
						sendData.setEdiPortalMailboxId(ediPortalMailboxId);
						sendData.setToken(token);
						sendData.setCustomsReportNo(change.getCustomsReportNo()); // 반입신고번호
						sendData.setDoucmentCode(change.getTypeCode());
						sendData.setXml(sendXml);
						sendData.setRegisterUserId(change.getRegisterUserId()); // 내부 로그용
						sendData.setOdJobNo(change.getOdJobNo()); // 작업내역 조회용 key
						sendData.setEsJsonJobLogList(change);  // ES JOB LOG
						sndList.add(sendData);
						
						//접수후 전송상태로 업데이트 문제로 전송전에 상태값 업데이트 하는 걸로 수정함. 2025-10-13 yjkim
						succDbUpdateMap = new HashMap<>();
						succDbUpdateMap.put("changeNo", change.getChangeNo());
						succDbUpdateMap.put("updateUserId", change.getRegisterUserId());
						bmsSendMapper.batchUpdateEdiBmsChangeSendSucc(succDbUpdateMap);
					} else {
						nFailCnt++;
					}
				}

				// MRS 서버로 1건씩 XML 전송 처리
				List<Map<String, Object>> succMrsSendArray = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> errMrsSendArray = new ArrayList<Map<String, Object>>();
				Map<String, Object> succMrsSendMap;
				Map<String, Object> errMrsSendMap;

				for(UMrsSendData send: sndList) {
					MrsRet mrsRet = sendUtils.sendMrsServerByHttp(send);
					send.setCustomsErrCode(mrsRet.getCustomsErrCode());
					send.setCustomsErrDesc(mrsRet.getCustomsErrDesc());
					send.setCustomsErrDescAdditional(mrsRet.getBody()); // 추가 Error 코드

					succMrsSendMap = new HashMap<String, Object>();
					errMrsSendMap = new HashMap<String, Object>();

					if("C200".equals(send.getCustomsErrCode())) {
						nSuccCnt++;
						succMrsSendMap.put("changeNo", send.getKeyId()); // 송신성공(BMS03) UPDATE 키값 수집
						succMrsSendMap.put("customsReportNo", send.getCustomsReportNo());
						succMrsSendMap.put("odJobNo", send.getOdJobNo());
						succMrsSendMap.put("esJsonJobLogList", send.getEsJsonJobLogList());
						succMrsSendMap.put("updateUserId", send.getRegisterUserId());
						succMrsSendArray.add(succMrsSendMap);
					} else {
						nFailCnt++;
						errMrsSendMap.put("changeNo", send.getKeyId()); // 송신실패(BMS02) UPDATE 키값 수집
						errMrsSendMap.put("customsReportNo", send.getCustomsReportNo());
						errMrsSendMap.put("odJobNo", send.getOdJobNo());
						errMrsSendMap.put("esJsonJobLogList", send.getEsJsonJobLogList());
						errMrsSendMap.put("updateUserId", send.getRegisterUserId());
						errMrsSendArray.add(errMrsSendMap);
						log.error("############################### [반입정정 GOVCBR5LC] UMRS SEND FAILED (BMS02): {}", send); // SEND FAILED 로그
						mrsSendErrorMsg += "[반입신고번호 " + send.getErrDescKey() + "] " + send.getCustomsErrDescAdditional() + " (Error Code: " + send.getCustomsErrCode() + ")<br/>";
					}
				}

				// DB Batch Update
				int succCnt = 0;
//				Map<String, Object> succDbUpdateMap;

				for(Map<String, Object> succMap : succMrsSendArray) {
					succCnt++;
//					succDbUpdateMap = new HashMap<>();
//					succDbUpdateMap.put("changeNo", succMap.get("changeNo"));
//					succDbUpdateMap.put("updateUserId", succMap.get("updateUserId"));
//					bmsSendMapper.batchUpdateEdiBmsChangeSendSucc(succDbUpdateMap);

					/** 작업내역 로그 */
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(entCode);
					processLogs.setEntBusinessCode(entBusinessCode);
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(succMap.get("updateUserId")));
					processLogs.setLogJobType("DATA");
		    		processLogs.setBound("AIR");
		    		processLogs.setBoundType("IMP");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
					processLogs.setDbType(BmsConsts.DbType.BMS_CHANGE_IN);
					processLogs.setDescription(BmsConsts.DbType.BMS_CHANGE_IN + " [SEND 신고번호: " + String.valueOf(succMap.get("customsReportNo")) + "]");
					processLogs.setJobNo(String.valueOf(succMap.get("odJobNo")));
					processLogs.setJobLog(new Gson().toJson(succMap.get("esJsonJobLogList")));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(logUserBusinessPrintName);
					processLogs.setProcessUserName(logUserName);
					processLogs.setProcessUserNameEng(logUserNameEng);
					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.SEND, "", succMap);

					if(succCnt % batchContInsertSize == 0 || succCnt == succMrsSendArray.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}

				int errCnt = 0;
				Map<String, Object> errDbUpdateMap;

				for(Map<String, Object> errMap : errMrsSendArray) {
					errCnt++;
					errDbUpdateMap = new HashMap<>();
					errDbUpdateMap.put("changeNo", errMap.get("changeNo"));
					errDbUpdateMap.put("updateUserId", errMap.get("updateUserId"));
					bmsSendMapper.batchUpdateEdiBmsChangeSendErr(errDbUpdateMap);

//					/** 작업내역 로그 -> 2024-08-01, 전송 실패건은 POD 업데이트 하지 않음 */
//					ProcessLogs processLogs = new ProcessLogs();
//					processLogs.setEntCode(entCode);
//					processLogs.setEntBusinessCode(entBusinessCode);
//					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
//					processLogs.setProcessUserId(Util.null2str(errMap.get("updateUserId")));
//					processLogs.setLogJobType("DATA");
//		    		processLogs.setBound("AIR");
//		    		processLogs.setBoundType("IMP");
//					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
//					processLogs.setDbType(BmsConsts.DbType.BMS_CHANGE_IN);
//					processLogs.setDescription(BmsConsts.DbType.BMS_CHANGE_IN + " [SEND 신고번호: " + String.valueOf(errMap.get("customsReportNo")) + "]");
//					processLogs.setJobNo(String.valueOf(errMap.get("odJobNo")));
//					processLogs.setJobLog(new Gson().toJson(errMap.get("esJsonJobLogList")));
//					processLogs.setResult("ok");
//					processLogs.setEntBusinessPrintName(logUserBusinessPrintName);
//					processLogs.setProcessUserName(logUserName);
//					processLogs.setProcessUserNameEng(logUserNameEng);
//					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.SEND, "", errMap);

					if(errCnt % batchContInsertSize == 0 || errCnt == errMrsSendArray.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				transactionManager.commit(status);
			} catch (Exception e) {
				if(transactionManager != null)	transactionManager.rollback(status);
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			} finally {
				if(sqlSession != null)	sqlSession.close();
			}
		} catch(Exception e) {
			log.error(e.getMessage());
			String stuff = new Object(){}.getClass().getName() + new Object(){}.getClass().getEnclosingMethod().getName();
			result.setErrorMsg("System Error.");
		} finally {
			bmsMembers.removeSendXmlGOVCBR5LCParserUser(entCode);
			if (bmsMembers.getSendXmlGOVCBR5LCParserUser(entCode) != null) {
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR5LCParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)");
				bmsMembers.removeSendXmlGOVCBR5LCParserUser(entCode);
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR5LCParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)2");
			}
		}

		result.setSuccCnt(nSuccCnt);
		result.setFailCnt(nFailCnt);
		result.setMrsSendErrorMsg(mrsSendErrorMsg);

		return result;
	}

	/**
	 * GOVCBR5LD(반출신고정정신청서) MRS 서버 전송및 송신후 결과 표시 XML Parser 호출
	 * @param entCode
	 * @param entBusinessCode
	 * @param changeNoList
	 * @return
	 */
	public CarryOutChangeSaveResult callSendXmlGOVCBR5LDParserExecutor(BatchBmsOutCorrection params, HttpServletRequest request) throws Exception {
		String entCode = params.getEntCode();
		String entBusinessCode = params.getEntBusinessCode();
		String logUserBusinessPrintName = params.getLogUserBusinessPrintName();
		String logUserName = params.getLogUserName();
		String logUserNameEng = params.getLogUserNameEng();
		List<CarryOutChange> bmsCarryOutChangeList = params.getBmsCarryOutChangeList();
		CarryOutChangeSaveResult result = new CarryOutChangeSaveResult();

		Integer nSuccCnt = 0;
		Integer nFailCnt = 0;
		String mrsSendErrorMsg = "";

		try {
			SqlSession sqlSession = null;
			TransactionStatus status = null;

			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
				status = transactionManager.getTransaction(new DefaultTransactionDefinition());
				EdiBmsSendMapper bmsSendMapper = sqlSession.getMapper(EdiBmsSendMapper.class);

				// 토큰값 얻어오기
				String ediPortalMailboxId = sendUtils.getEdiPortalMailboxId(entCode, entBusinessCode);
				if(!sendUtils.isOnline(customProperties.getEbmsIp(), Integer.parseInt(customProperties.getEbmsPort()))) {
					log.warn("###################################### Failed to access EDI Portal ######################################");
					result.setErrorMsg("관세청 연결 오류가 발생하였습니다.");
					return result;
				}

				// XML 마샬링용 변수
				String sendXml = "";
				String jaxbSchemaLocation="urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LDSchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfCAG_5LDSchemaModule_1.0_standard.xsd";
				String strkcs = "xmlns:kcs=\"urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LDSchemaModule:1:0\"";
				String token = sendUtils.getMrsToken(ediPortalMailboxId);

				SearchOutCorrection search = new SearchOutCorrection();
				List<Integer> changeNoList = new ArrayList<Integer>();

				for(CarryOutChange change : bmsCarryOutChangeList) changeNoList.add(change.getChangeNo());
				search.setChangeNoList(changeNoList);
				List<CarryOutChange> lst = ediBmsSendMapper.selectCarryOutChangeList(search);

				UMrsSendData sendData;
				OutGOVCBR5LD out5LD = new OutGOVCBR5LD();
				List<UMrsSendData> sndList = new ArrayList<UMrsSendData>();

				Map<String, Object> succDbUpdateMap = null;
				
				// 마샬링
				for(CarryOutChange change : lst) {
					if(change.getLastPodStatus().equals("BMS01") || change.getLastPodStatus().equals("BMS02")) {
						out5LD = sendUtils.getBmsChangeToGOVCBR5LD(change);
						sendXml = sendUtils.sendVoToXmlMarshal(out5LD, jaxbSchemaLocation, strkcs);
						sendData = new UMrsSendData();
						sendData.setKeyId(change.getChangeNo());
						sendData.setErrDescKey(change.getCustomsReportNo()); // 에러메시지용 key
						sendData.setEdiPortalMailboxId(ediPortalMailboxId);
						sendData.setToken(token);
						sendData.setCustomsReportNo(change.getCustomsReportNo()); // 반출신고번호
						sendData.setDoucmentCode(change.getTypeCode());
						sendData.setXml(sendXml);
						sendData.setRegisterUserId(change.getRegisterUserId()); // 내부 로그용
						sendData.setOdJobNo(change.getOdJobNo()); // 작업내역 조회용 key
						sendData.setEsJsonJobLogList(change);  // ES JOB LOG
						sndList.add(sendData);
						
						succDbUpdateMap = new HashMap<>();
						succDbUpdateMap.put("changeNo", change.getChangeNo());
						succDbUpdateMap.put("updateUserId", change.getRegisterUserId());
						bmsSendMapper.batchUpdateEdiBmsChangeSendSucc(succDbUpdateMap);
					} else {
						nFailCnt++;
					}
				}

				// MRS 서버로 1건씩 XML 전송 처리
				List<Map<String, Object>> succMrsSendArray = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> errMrsSendArray = new ArrayList<Map<String, Object>>();
				Map<String, Object> succMrsSendMap;
				Map<String, Object> errMrsSendMap;

				for(UMrsSendData send: sndList) {
					MrsRet mrsRet = sendUtils.sendMrsServerByHttp(send);
					send.setCustomsErrCode(mrsRet.getCustomsErrCode());
					send.setCustomsErrDesc(mrsRet.getCustomsErrDesc());
					send.setCustomsErrDescAdditional(mrsRet.getBody()); // 추가 Error 코드

					succMrsSendMap = new HashMap<String, Object>();
					errMrsSendMap = new HashMap<String, Object>();

					if("C200".equals(send.getCustomsErrCode())) {
						nSuccCnt++;
						succMrsSendMap.put("changeNo", send.getKeyId()); // 송신성공(BMS03) UPDATE 키값 수집
						succMrsSendMap.put("customsReportNo", send.getCustomsReportNo());
						succMrsSendMap.put("odJobNo", send.getOdJobNo());
						succMrsSendMap.put("esJsonJobLogList", send.getEsJsonJobLogList());
						succMrsSendMap.put("updateUserId", send.getRegisterUserId());
						succMrsSendArray.add(succMrsSendMap);
					} else {
						nFailCnt++;
						errMrsSendMap.put("changeNo", send.getKeyId()); // 송신실패(BMS02) UPDATE 키값 수집
						errMrsSendMap.put("customsReportNo", send.getCustomsReportNo());
						errMrsSendMap.put("odJobNo", send.getOdJobNo());
						errMrsSendMap.put("esJsonJobLogList", send.getEsJsonJobLogList());
						errMrsSendMap.put("updateUserId", send.getRegisterUserId());
						errMrsSendArray.add(errMrsSendMap);
						log.error("############################### [반출정정 GOVCBR5LD] UMRS SEND FAILED (BMS02): {}", send); // SEND FAILED 로그
						mrsSendErrorMsg += "[반출신고번호 " + send.getErrDescKey() + "] " + send.getCustomsErrDescAdditional() + " (Error Code: " + send.getCustomsErrCode() + ")<br/>";
					}
				}

				// DB Batch Update
				int succCnt = 0;
//				Map<String, Object> succDbUpdateMap;

				for(Map<String, Object> succMap : succMrsSendArray) {
					succCnt++;
//					succDbUpdateMap = new HashMap<>();
//					succDbUpdateMap.put("changeNo", succMap.get("changeNo"));
//					succDbUpdateMap.put("updateUserId", succMap.get("updateUserId"));
//					bmsSendMapper.batchUpdateEdiBmsChangeSendSucc(succDbUpdateMap);

					/** 작업내역 로그 */
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(entCode);
					processLogs.setEntBusinessCode(entBusinessCode);
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(succMap.get("updateUserId")));
					processLogs.setLogJobType("DATA");
		    		processLogs.setBound("AIR");
		    		processLogs.setBoundType("IMP");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
					processLogs.setDbType(BmsConsts.DbType.BMS_CHANGE_OUT);
					processLogs.setDescription(BmsConsts.DbType.BMS_CHANGE_OUT + " [SEND 신고번호: " + String.valueOf(succMap.get("customsReportNo")) + "]");
					processLogs.setJobNo(String.valueOf(succMap.get("odJobNo")));
					processLogs.setJobLog(new Gson().toJson(succMap.get("esJsonJobLogList")));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(logUserBusinessPrintName);
					processLogs.setProcessUserName(logUserName);
					processLogs.setProcessUserNameEng(logUserNameEng);
					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.SEND, "", succMap);

					if(succCnt % batchContInsertSize == 0 || succCnt == succMrsSendArray.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}

				int errCnt = 0;
				Map<String, Object> errDbUpdateMap;

				for(Map<String, Object> errMap : errMrsSendArray) {
					errCnt++;
					errDbUpdateMap = new HashMap<>();
					errDbUpdateMap.put("changeNo", errMap.get("changeNo"));
					errDbUpdateMap.put("updateUserId", errMap.get("updateUserId"));
					bmsSendMapper.batchUpdateEdiBmsChangeSendErr(errDbUpdateMap);

//					/** 작업내역 로그 -> 2024-08-01, 전송 실패건은 POD 업데이트 하지 않음 */
//					ProcessLogs processLogs = new ProcessLogs();
//					processLogs.setEntCode(entCode);
//					processLogs.setEntBusinessCode(entBusinessCode);
//					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
//					processLogs.setProcessUserId(Util.null2str(errMap.get("updateUserId")));
//					processLogs.setLogJobType("DATA");
//		    		processLogs.setBound("AIR");
//		    		processLogs.setBoundType("IMP");
//					processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
//					processLogs.setDbType(BmsConsts.DbType.BMS_CHANGE_OUT);
//					processLogs.setDescription(BmsConsts.DbType.BMS_CHANGE_OUT + " [SEND 신고번호: " + String.valueOf(errMap.get("customsReportNo")) + "]");
//					processLogs.setJobNo(String.valueOf(errMap.get("odJobNo")));
//					processLogs.setJobLog(new Gson().toJson(errMap.get("esJsonJobLogList")));
//					processLogs.setResult("ok");
//					processLogs.setEntBusinessPrintName(logUserBusinessPrintName);
//					processLogs.setProcessUserName(logUserName);
//					processLogs.setProcessUserNameEng(logUserNameEng);
//					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.SEND, "", errMap);

					if(errCnt % batchContInsertSize == 0 || errCnt == errMrsSendArray.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				transactionManager.commit(status);
			} catch (Exception e) {
				if(transactionManager != null)	transactionManager.rollback(status);
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			} finally {
				if(sqlSession != null)	sqlSession.close();
			}
		} catch(Exception e) {
			log.error(e.getMessage());
			String stuff = new Object(){}.getClass().getName() + new Object(){}.getClass().getEnclosingMethod().getName();
			result.setErrorMsg("System Error.");
//			catchMailSend.exceptionHandle(e, stuff); // 예외 메일 전송
		} finally {
			bmsMembers.removeSendXmlGOVCBR5LDParserUser(entCode);
			if (bmsMembers.getSendXmlGOVCBR5LDParserUser(entCode) != null) {
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR5LDParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)");
				bmsMembers.removeSendXmlGOVCBR5LDParserUser(entCode);
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR5LDParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)2");
			}
		}

		result.setSuccCnt(nSuccCnt);
		result.setFailCnt(nFailCnt);
		result.setMrsSendErrorMsg(mrsSendErrorMsg);

		return result;
	}

	/**
	 * GOVCBRDKO(특송화물 반입물품 이상보고서) MRS 서버 전송및 송신후 결과 표시 XML Parser 호출
	 * @param entCode
	 * @param entBusinessCode
	 * @param abnNoList
	 * @return
	 */
	public CarryInAbnormalSaveResult callSendXmlGOVCBRDKOParserExecutor(String entCode, String entBusinessCode, List<Integer> abnNoList) {
		CarryInAbnormalSaveResult result = new CarryInAbnormalSaveResult();
		Integer nSuccCnt = 0;
		Integer nFailCnt = 0;
		String mrsSendErrorMsg = "";

		try {
//			BranchContextHolder.setBranchContext(Enum.valueOf(BranchEnum.class, entCode.toUpperCase()));
			SqlSession sqlSession = null;
			TransactionStatus status = null;

			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
				status = transactionManager.getTransaction(new DefaultTransactionDefinition());
				EdiBmsSendMapper bmsSendMapper = sqlSession.getMapper(EdiBmsSendMapper.class);

				// 토큰값 얻어오기
				String ediPortalMailboxId = sendUtils.getEdiPortalMailboxId(entCode, entBusinessCode);
				if(!sendUtils.isOnline(customProperties.getEbmsIp(), Integer.parseInt(customProperties.getEbmsPort()))) {
					log.warn("###################################### Failed to access EDI Portal ######################################");
					result.setErrorMsg("관세청 연결 오류가 발생하였습니다.");
					return result;
				}

				// XML 마샬링용 변수
				String sendXml = "";
				String jaxbSchemaLocation="urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_DKOSchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfCAG_DKOSchemaModule_1.0_standard.xsd";
				String strkcs = "xmlns:kcs=\"urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_DKOSchemaModule:1:0\"";
				String token = sendUtils.getMrsToken(ediPortalMailboxId);

				SearchCarryInAbnormal search = new SearchCarryInAbnormal();
				search.setEntBusinessCode(entBusinessCode); // 세관과코드 조회용
				search.setAbnNoList(abnNoList);
				List<CarryInAbnormal> lst = ediBmsSendMapper.selectEdiBmsAbnormalSendList(search);

				UMrsSendData sendData;
				DeclGOVCBRDKO decrDKO = new DeclGOVCBRDKO();
				List<UMrsSendData> sndList = new ArrayList<UMrsSendData>();

				Map<String, Object> abnMap = null;
				
				// 마샬링
				for(CarryInAbnormal abn : lst) {
					if(abn.getLastPodStatus().equals("BMS01")) {
						decrDKO = sendUtils.getBmsAbnormalToGOVCBRDKO(abn);
						sendXml = sendUtils.sendVoToXmlMarshal(decrDKO, jaxbSchemaLocation, strkcs);
						sendData = new UMrsSendData();
						sendData.setKeyId(abn.getAbnNo());
						sendData.setErrDescKey(abn.getMblNo()); // 에러메시지용 key
						sendData.setEdiPortalMailboxId(ediPortalMailboxId);
						sendData.setToken(token);
						sendData.setCustomsReportNo(abn.getCustomsReportNo());
						sendData.setDoucmentCode(abn.getTypeCode());
						sendData.setXml(sendXml);
						sendData.setRegisterUserId(abn.getRegisterUserId()); // 내부 로그용
						sndList.add(sendData);
						
						abnMap = new HashMap<>();
						abnMap.put("abnNo", abn.getAbnNo());
						abnMap.put("updateUserId", abn.getRegisterUserId());
						bmsSendMapper.batchUpdateEdiBmsAbnormalSend(abnMap);
					} else {
						nFailCnt++;
					}
				}

				// MRS 서버로 1건씩 XML 전송 처리
				List<Integer> succAbnNos = new ArrayList<Integer>();
				for(UMrsSendData send: sndList) {
					MrsRet mrsRet = sendUtils.sendMrsServerByHttp(send);
					send.setCustomsErrCode(mrsRet.getCustomsErrCode());
					send.setCustomsErrDesc(mrsRet.getCustomsErrDesc());
					send.setCustomsErrDescAdditional(mrsRet.getBody()); // 추가 Error 코드

					if ("C200".equals(send.getCustomsErrCode())) {
						nSuccCnt++;
						succAbnNos.add(send.getKeyId()); // 전송 완료 DB Update 처리를 위한 키값 수집
					} else {
						nFailCnt++;
						log.error("send mrs error GOVCBRDKO: {}", send); // SEND ERROR 처리
						mrsSendErrorMsg += "[MBL번호 " + send.getErrDescKey() + "] " + send.getCustomsErrDescAdditional() + " (Error Code: " + send.getCustomsErrCode() + ")<br/>";
					}
				}

				// DB Batch Update
				int cnt = 0;
//				Map<String, Object> abnMap;

				for(Integer abnNo : succAbnNos) {
					cnt++;
//					abnMap = new HashMap<>();
//					abnMap.put("abnNo", abnNo);
//					bmsSendMapper.batchUpdateEdiBmsAbnormalSend(abnMap);

					if(cnt % batchContInsertSize == 0 || cnt == succAbnNos.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}

				// log elastic search에 전송 처리
				String index = "bms-interface-logs-" + DateTime.getDate("yyyyMMdd");
				ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
				BulkData bulkData = null;
				String dataIdPrefix = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5);
				String dataId = "";
				int seq = 1;
				String docCode = "GOVCBRDKO";
				String docName = "특송화물반입물품이상보고서";
				Map<String, Object> logMap;
				for (UMrsSendData send: sndList) {
					logMap = sendUtils.getSendLogMap(send, entCode, docCode, docName);
					try {
						dataId = dataIdPrefix + Util.setSeqNo(seq++);
					} catch (Exception e) {
						dataId = dataIdPrefix + "00001";
					}

					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(Util.null2str(dataId));
					bulkData.setJsonMap(logMap);
					bulkList.add(bulkData);
				}

				if (bulkList != null && bulkList.size() > 0) {
					IndexApi indexApi = new IndexApi(client);
					if (!indexApi.existIndex(index)) indexApi.createIndex(index);
					DocumentApi documentApi = new DocumentApi(client);
					documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
				}
				transactionManager.commit(status);
			} catch (Exception e) {
				if(transactionManager != null) transactionManager.rollback(status);
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			} finally {
				if(sqlSession != null) sqlSession.close();
			}
		} catch(Exception e) {
			log.error(e.getMessage());
			String stuff = new Object(){}.getClass().getName() + new Object(){}.getClass().getEnclosingMethod().getName();
			result.setErrorMsg("System Error.");
//			catchMailSend.exceptionHandle(e, stuff); //예외 메일 전송
		} finally {
			bmsMembers.removeSendXmlGOVCBRDKOParserUser(entCode);
			if(bmsMembers.getSendXmlGOVCBRDKOParserUser(entCode) != null) {
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBRDKOParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)");
				bmsMembers.removeSendXmlGOVCBRDKOParserUser(entCode);
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBRDKOParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)2");
			}
		}

		result.setSuccCnt(nSuccCnt);
		result.setFailCnt(nFailCnt);
		result.setMrsSendErrorMsg(mrsSendErrorMsg);

		return result;
	}

	/**
	 * GOVCBRDKP(특송화물 반입물품 이상보고 정정신청서) MRS 서버 전송및 송신후 결과 표시 XML Parser 호출
	 * @param entCode
	 * @param entBusinessCode
	 * @param abnChangeNoList
	 * @return
	 */
	public CarryInAbnormalCorrSaveResult callSendXmlGOVCBRDKPParserExecutor(String entCode, String entBusinessCode, List<Integer> abnChangeNoList) {
		CarryInAbnormalCorrSaveResult result = new CarryInAbnormalCorrSaveResult();
		Integer nSuccCnt = 0;
		Integer nFailCnt = 0;
		String mrsSendErrorMsg = "";

		try {
//			BranchContextHolder.setBranchContext(Enum.valueOf(BranchEnum.class, entCode.toUpperCase()));
			SqlSession sqlSession = null;
			TransactionStatus status = null;

			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
				status = transactionManager.getTransaction(new DefaultTransactionDefinition());
				EdiBmsSendMapper bmsSendMapper = sqlSession.getMapper(EdiBmsSendMapper.class);

				// 토큰값 얻어오기
				String ediPortalMailboxId = sendUtils.getEdiPortalMailboxId(entCode, entBusinessCode);
				if(!sendUtils.isOnline(customProperties.getEbmsIp(), Integer.parseInt(customProperties.getEbmsPort()))) {
					log.warn("###################################### Failed to access EDI Portal ######################################");
					result.setErrorMsg("관세청 연결 오류가 발생하였습니다.");
					return result;
				}

				// XML 마샬링용 변수
				String sendXml = "";
				String jaxbSchemaLocation="urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_DKPSchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfCAG_DKPSchemaModule_1.0_standard.xsd";
				String strkcs = "xmlns:kcs=\"urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_DKPSchemaModule:1:0\"";
				String token = sendUtils.getMrsToken(ediPortalMailboxId);

				SearchCarryInAbnormalCorr search = new SearchCarryInAbnormalCorr();
				search.setEntBusinessCode(entBusinessCode); // 세관과코드 조회용
				search.setAbnChangeNoList(abnChangeNoList);
				List<CarryInAbnormalCorr> lst = ediBmsSendMapper.selectEdiBmsAbnormalCorrSendList(search);

				UMrsSendData sendData;
				DeclGOVCBRDKP decrDKP = new DeclGOVCBRDKP();
				List<UMrsSendData> sndList = new ArrayList<UMrsSendData>();

				Map<String, Object> abnChangeMap = null;
				
				// 마샬링
				for(CarryInAbnormalCorr abnChange : lst) {
					if(abnChange.getLastPodStatus().equals("BMS01")) {
						decrDKP = sendUtils.getBmsAbnormalCorrToGOVCBRDKP(abnChange);
						sendXml = sendUtils.sendVoToXmlMarshal(decrDKP, jaxbSchemaLocation, strkcs);
						sendData = new UMrsSendData();
						sendData.setKeyId(abnChange.getAbnChangeNo());
						sendData.setErrDescKey(abnChange.getMblNo()); // 에러메시지용 key
						sendData.setEdiPortalMailboxId(ediPortalMailboxId);
						sendData.setToken(token);
						sendData.setCustomsReportNo(abnChange.getCustomsReportNo());
						sendData.setDoucmentCode(abnChange.getTypeCode());
						sendData.setXml(sendXml);
						sendData.setRegisterUserId(abnChange.getRegisterUserId()); // 내부 로그용
						sndList.add(sendData);
						
						abnChangeMap = new HashMap<>();
						abnChangeMap.put("abnChangeNo", abnChange.getAbnChangeNo());
						abnChangeMap.put("updateUserId", abnChange.getRegisterUserId());
						bmsSendMapper.batchUpdateEdiBmsAbnormalCorrSend(abnChangeMap);
					} else {
						nFailCnt++;
					}
				}

				// MRS 서버로 1건씩 XML 전송 처리
				List<Integer> succAbnChangeNos = new ArrayList<Integer>();
				for(UMrsSendData send: sndList) {
					MrsRet mrsRet = sendUtils.sendMrsServerByHttp(send);
					send.setCustomsErrCode(mrsRet.getCustomsErrCode());
					send.setCustomsErrDesc(mrsRet.getCustomsErrDesc());
					send.setCustomsErrDescAdditional(mrsRet.getBody()); // 추가 Error 코드

					if ("C200".equals(send.getCustomsErrCode())) {
						nSuccCnt++;
						succAbnChangeNos.add(send.getKeyId()); // 전송 완료 DB Update 처리를 위한 키값 수집
					} else {
						nFailCnt++;
						log.error("send mrs error GOVCBRDKP: {}", send); // SEND ERROR 처리
						mrsSendErrorMsg += "[MBL번호 " + send.getErrDescKey() + "] " + send.getCustomsErrDescAdditional() + " (Error Code: " + send.getCustomsErrCode() + ")<br/>";
					}
				}

				// DB Batch Update
				int cnt = 0;
//				Map<String, Object> abnChangeMap;

				for(Integer abnChangeNo : succAbnChangeNos) {
					cnt++;
//					abnChangeMap = new HashMap<>();
//					abnChangeMap.put("abnChangeNo", abnChangeNo);
//					bmsSendMapper.batchUpdateEdiBmsAbnormalCorrSend(abnChangeMap);

					if(cnt % batchContInsertSize == 0 || cnt == succAbnChangeNos.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}

				// log elastic search에 전송 처리
				String index = "bms-interface-logs-" + DateTime.getDate("yyyyMMdd");
				ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
				BulkData bulkData = null;
				String dataIdPrefix = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5);
				String dataId = "";
				int seq = 1;
				String docCode = "GOVCBRDKP";
				String docName = "특송화물반입물품이상보고정정신청서";
				Map<String, Object> logMap;
				for (UMrsSendData send: sndList) {
					logMap = sendUtils.getSendLogMap(send, entCode, docCode, docName);
					try {
						dataId = dataIdPrefix + Util.setSeqNo(seq++);
					} catch (Exception e) {
						dataId = dataIdPrefix + "00001";
					}

					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(Util.null2str(dataId));
					bulkData.setJsonMap(logMap);
					bulkList.add(bulkData);
				}

				if (bulkList != null && bulkList.size() > 0) {
					IndexApi indexApi = new IndexApi(client);
					if (!indexApi.existIndex(index)) indexApi.createIndex(index);
					DocumentApi documentApi = new DocumentApi(client);
					documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
				}
				transactionManager.commit(status);
			} catch (Exception e) {
				if(transactionManager != null) transactionManager.rollback(status);
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			} finally {
				if(sqlSession != null) sqlSession.close();
			}
		} catch(Exception e) {
			log.error(e.getMessage());
			String stuff = new Object(){}.getClass().getName() + new Object(){}.getClass().getEnclosingMethod().getName();
			result.setErrorMsg("System Error.");
//			catchMailSend.exceptionHandle(e, stuff); //예외 메일 전송
		} finally {
			bmsMembers.removeSendXmlGOVCBRDKPParserUser(entCode);
			if(bmsMembers.getSendXmlGOVCBRDKPParserUser(entCode) != null) {
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBRDKPParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)");
				bmsMembers.removeSendXmlGOVCBRDKPParserUser(entCode);
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBRDKPParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)2");
			}
		}

		result.setSuccCnt(nSuccCnt);
		result.setFailCnt(nFailCnt);
		result.setMrsSendErrorMsg(mrsSendErrorMsg);

		return result;
	}
	
	
	/**
	 * GOVCBR5IG(하기결과이상보고) MRS 서버 전송및 송신후 결과 표시 XML Parser 호출
	 * @param docSend
	 * @param abnormalList
	 * @param fwSendAccInfo
	 * @return
	 */
	public Map<String, Object> callSendXmlGOVCBR5IGParserExecutor(Map<String,Object> docSend, List<Map<String,Object>> abnormalList, Map<String,Object> fwSendAccInfo) {
		Map result = new HashMap<String, Object>();
		Integer nSuccCnt = 0;
		Integer nFailCnt = 0;
		String mrsSendErrorMsg = "";
		List<Map<String,Object>> masterAbnormalNos = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> succAbnormalNos = new ArrayList<Map<String,Object>>();		// 전송 성공 리스트
		List<Map<String,Object>> failAbnormalNos = new ArrayList<Map<String,Object>>();		// 전송 실패 리스트
		List<Map<String,Object>> failAbnormalNosMap = new ArrayList<Map<String,Object>>();	// 전송 실패 대표리스트 맵
		
		String entCode = Util.null2str(docSend.get("entCode"));
		String entBusinessCode = Util.null2str(docSend.get("entBusinessCode"));
		String bound = "AIR";
		String boundType = "IMP";
		String ediCityPort = "ICN";
		try {
			SqlSession sqlSession = null;
			TransactionStatus status = null;

			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
				status = transactionManager.getTransaction(new DefaultTransactionDefinition());
				EdiBmsSendMapper bmsSendMapper = sqlSession.getMapper(EdiBmsSendMapper.class);

				// 토큰값 얻어오기
				String ediPortalMailboxId = sendUtils.getEdiPortalMailboxIdSrch(entCode, entBusinessCode, bound, boundType, ediCityPort);
				if(!sendUtils.isOnline(customProperties.getEbmsIp(), Integer.parseInt(customProperties.getEbmsPort()))) {
					log.warn("###################################### Failed to access EDI Portal ######################################");
					result.put("errorMsg","시스템 오류가 발생하였습니다. Failed to access EDI Portal");
					//result.setErrorMsg("시스템 오류가 발생하였습니다.<br>Failed to access EDI Portal");
					return result;
				}
				
				
				String sendXml = "";
				String jaxbSchemaLocation="urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0 ../../schema4G/kcs/data/standard/KCS_DeclarationOfCAG_5IGSchemaModule_1.0_standard.xsd";
				String strkcs = "xmlns:kcs=\"urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IGSchemaModule:1:0\"";
				String token = sendUtils.getMrsToken(ediPortalMailboxId);

				UMrsSendData sendData;
				List<UMrsSendData> sndList = new ArrayList<UMrsSendData>();
				List<Map<String, Object>> sendMapList = new ArrayList<Map<String,Object>>();
				for(Map<String,Object> abnormal : abnormalList) {
					sendData = new UMrsSendData();
					if( !"".equals(Util.null2str(abnormal.get("OD_JOB_NO")))  && (List<Map<String, Object>>)abnormal.get("tempHblDataList") != null ) {
						sendXml = bmsCargoAbnormalService.getSendDataFileCreateAbnormalitiesReportString(docSend, abnormal, fwSendAccInfo);
						sendData.setKeyId(Util.null2int(abnormal.get("SEQ_NO_ID")));		// 전송측키? Integer
						sendData.setErrDescKey(Util.null2str(abnormal.get("OD_JOB_NO")));	// 에러메시지용 key
						sendData.setEdiPortalMailboxId(ediPortalMailboxId);
						sendData.setToken(token);
						sendData.setCustomsReportNo(Util.null2str(abnormal.get("MRN")));
						sendData.setDoucmentCode("GOVCBR5IG");	// 문서코드
						sendData.setXml(sendXml);	// XML 스트링
						sendData.setRegisterUserId(Util.null2str(docSend.get("registerUserId"))); // 내부 로그용
						sndList.add(sendData);
						sendMapList.add(abnormal);
					}
				}
				

				// MRS 서버로 1건씩 XML 전송 처리
				int idx = 0;
				for(UMrsSendData send: sndList) {
					Map<String, Object> hblMap = sendMapList.get(idx);
					List<Map<String, Object>> hblList = (List<Map<String, Object>>) hblMap.get("tempHblDataList");
					
					// 보내기전 전송 처리 2026.04.13 2sowell
					if(!Util.isEmpty(send.getCustomsReportNo())) {
						// 전송 성공 건들 POD 저장
						ProcessLogs processLogs = new ProcessLogs();
						processLogs.setEntCode(Util.null2str(docSend.get("entCode")));
						processLogs.setEntBusinessCode(Util.null2str(docSend.get("entBusinessCode")));
						processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
						processLogs.setProcessUserId(Util.null2str(docSend.get("registerUserId")));
						processLogs.setLogJobType("DATA");
						processLogs.setBound(Util.null2str(docSend.get("bound")));
						processLogs.setBoundType(Util.null2str(docSend.get("boundType")));
						processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
						processLogs.setDbType(BmsConsts.DbType.BMS_ABNORMAL_RESULT);
						processLogs.setDescription(BmsConsts.DbType.BMS_ABNORMAL_RESULT+" [SEND MRN:]" + send.getCustomsReportNo());
						processLogs.setJobLog(new Gson().toJson(hblList));
						processLogs.setJobNo(String.valueOf(send.getCustomsReportNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
						processLogs.setResult("ok");
						processLogs.setProcessUserName(Util.null2str(docSend.get("registerUserName")));
						processLogs.setProcessUserNameEng(Util.null2str(docSend.get("registerUserName")));
						//commonService.insertLog(processLogs, request);
						bmsCommService.insertLogAndPodStatus(processLogs, null
								, BmsConsts.PodStatus.CUSTOM_POD_NAME /* pod Status 값 매핑 */
								, "하기이상결과보고(관세청송신)"
								, null
								);
					}
					
					
					MrsRet mrsRet = sendUtils.sendMrsServerByHttp(send);
					send.setCustomsErrCode(mrsRet.getCustomsErrCode());
					send.setCustomsErrDesc(mrsRet.getCustomsErrDesc());
					send.setCustomsErrDescAdditional(mrsRet.getBody()); // 추가 Error 코드

					if ("C200".equals(send.getCustomsErrCode())) {
						Map<String,Object> master = new HashMap<String,Object>();
						master.put("MRN", send.getCustomsReportNo());
						master.put("hblList", hblList);
						masterAbnormalNos.add(master);
						for(Map<String,Object> hbl : hblList) {
							Map succMap = new HashMap<String, Object>();
							succMap.put("OD_JOB_NO", hbl.get("OD_JOB_NO"));
							succMap.put("SEQ_NO_ID", hbl.get("SEQ_NO_ID"));
							succMap.put("odJobNo", hbl.get("OD_JOB_NO"));
							succMap.put("seqNoId", hbl.get("SEQ_NO_ID"));
							succAbnormalNos.add(succMap);
						}
						nSuccCnt++;
						
					} else {
						log.error("send mrs error GOVCBR5IG: {}", send); // SEND ERROR 처리
						Map failMap = new HashMap<String, Object>();
						failMap.put("OD_JOB_NO", send.getErrDescKey());
						failMap.put("SEQ_NO_ID", send.getKeyId());
						failMap.put("REFERANCE", "[OD_JOB_NO:"+send.getErrDescKey()+", SEQ_NO_ID:"+send.getKeyId());
						failMap.put("ERROR_MESSAGE", send.getCustomsErrCode());
						for(Map<String,Object> hbl : hblList) {
							Map failNoMap = new HashMap<String, Object>();
							failNoMap.put("OD_JOB_NO", hbl.get("OD_JOB_NO"));
							failNoMap.put("SEQ_NO_ID", hbl.get("SEQ_NO_ID"));
							failNoMap.put("odJobNo", hbl.get("OD_JOB_NO"));
							failNoMap.put("seqNoId", hbl.get("SEQ_NO_ID"));
							failAbnormalNos.add(failNoMap);
						}
						nFailCnt++;
						failAbnormalNosMap.add(failMap);
						mrsSendErrorMsg += "[OD_JOB_NO : " + send.getErrDescKey() + ", SEQ_NO_ID : " + send.getKeyId() + "] " + send.getCustomsErrDescAdditional() + " (Error Code: " + send.getCustomsErrCode() + ")<br/>";
					}
					idx ++;
				}

				// DB Batch Update
				int cnt = 0;

				for(Map<String, Object> abnChangeNo : succAbnormalNos) {
					cnt++;
					
					if(cnt % batchContInsertSize == 0 || cnt == succAbnormalNos.size()) {
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				
				transactionManager.commit(status);
			} catch (Exception e) {
				if(transactionManager != null) transactionManager.rollback(status);
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			} finally {
				if(sqlSession != null) sqlSession.close();
			}
		} catch(Exception e) {
			log.error(e.getMessage());
			String stuff = new Object(){}.getClass().getName() + new Object(){}.getClass().getEnclosingMethod().getName();
			result.put("errorMsg","System Error.");
		} finally {
			bmsMembers.removeSendXmlGOVCBR5IGParserUser(entCode);
			if(bmsMembers.getSendXmlGOVCBR5IGParserUser(entCode) != null) {
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR5IGParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)");
				bmsMembers.removeSendXmlGOVCBR5IGParserUser(entCode);
				log.error("[" + entCode + "][" + bmsMembers.getSendXmlGOVCBR5IGParserUser(entCode) + "] bmsMembers.getOrderXmlParserProcessUser(entCode)2");
			}
		}
		
		result.put("nSuccCnt", nSuccCnt);
		result.put("nFailCnt", nFailCnt);
		result.put("mrsSendErrorMsg", mrsSendErrorMsg);
		result.put("masterList", masterAbnormalNos);
		result.put("succList", succAbnormalNos);
		result.put("errorList", failAbnormalNos);
		result.put("errorData", failAbnormalNosMap);
		return result;
	}

}
