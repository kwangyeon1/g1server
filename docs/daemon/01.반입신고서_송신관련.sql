/***
  반입 신고서 송신 소스 SQL문
  GOVCBR632:
  1. 반입(TB_BMS_IN) 자료를 읽어서 XML 생성
    2. 반입(TB_BMS_IN)
       신고일자(LAST_POD_DATE) 현재일자
       신고상태(LAST_POD_STATUS) BWH03 (송신)
       수정저장
 */
 --  1. 반입(TB_BMS_IN) 자료를 읽어서 XML 생성
SELECT
    'GOVCBR632' docNm
     , A.IN_NO
     , A.CUSTOMS_REPORT_NO
     , '9' AS FUNCTION_CODE
     , CARRYIN_DATE
     , CARRYIN_TIME
     , REPLACE(CARRYIN_DATE,'-','')+REPLACE(CARRYIN_TIME,':','')  AS carry_IN_14
     , LOCATION_PLACE
     , IN_CLASS
     , 'A' AS BUNHAL
     , CARGO_REFERENCE_NO
     , IN_APPROVAL_NO
     , XRAY_INSPECTION_CODE
     , CT
     , PKG_TYPE
     , ACCIDENT_CLASS
     , WT
     , ACCIDENT_WT
     , ACCIDENT_CT
FROM TB_BMS_IN A
WHERE A.LAST_POD_STATUS ='BMS01'
/*
    2. 반입(TB_BMS_IN)
       신고일자(LAST_POD_DATE) 현재일자
       신고상태(LAST_POD_STATUS) BWH03 (송신)
 */
 UPDATE
     TB_BMS_IN
  SET LAST_POD_DATE = GETDATE() /* DateTime 형식 */
    , LAST_POD_STATUS='BMS03' /*송신*/
WHERE
    IN_NO = #{inNo}


#참고 소스 정리 ====================================####
1) TB_SMS_ENT_BUS_EDI_ECM  에서 EDI_PORTAL_MAILBOX_ID 컬럼 조회 해서 ftp 접속 ID 준비 한다.
2) MRS 서버의 웹서비스로 전송 준비 한다.
2) 메소드로 전송 처리 한다.  웹으로 전송 처리
ediCommonService.sendEbms(Util.null2str(resultData.get("ediPortalMailboxId")), "GOVCBR936", jsonObject, sendType);

<select id="selectBusEdiEcmInfo" parameterType="HashMap" resultType="HashMap">
/* CommonMapper.selectBusEdiEcmInfo : TB_SMS_ENT_BUS_EDI_ECM 조회 */
SELECT TOP 1
    ENT_CODE
           ,ENT_BUSINESS_CODE
           ,SEQ_NO
           ,BOUND
           ,BOUND_TYPE
           ,EDI_CITY_PORT
           ,EDI_EXPRESS_SIGN
           ,EDI_PORTAL_LOGIN_ID
           ,EDI_PORTAL_LOGIN_PW
           ,EDI_PORTAL_MAILBOX_ID
           ,EDI_CERTIFICATE_PW
FROM TB_SMS_ENT_BUS_EDI_ECM
WHERE ENT_CODE = #{srchEntCode}
			<choose>
			<when test ="srchSeqNo !=null and srchSeqNo !=''">
			 AND SEQ_NO = #{srchSeqNo}
			</when>
			<otherwise>
			AND ENT_BUSINESS_CODE = #{srchEntBusinessCode}
			AND BOUND = #{srchBound}
			AND BOUND_TYPE = #{srchBoundType}
			AND EDI_CITY_PORT = #{srchEdiCityPort}
			</otherwise>
			</choose>
	</select>


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


			public JSONObject sendEbms(String ediPortalMailboxId, String doucmentCode, JSONObject sendData, String sendType, String token) throws Exception {
				JSONObject jsonObject = null;
if("local".equals(profile)){
    ediPortalMailboxId = "VC116814146901";
}
	/**
	 * 수입목록신고 xml 생성
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
				public OrderSaveResult setCustomsManifestImpData(SendManifest sendManifest, String cusSendType) throws Exception {


					IndexApi indexApi = new IndexApi(client);
        DocumentApi documentApi = null;
        String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
if (!indexApi.existIndex(index))	indexApi.createIndex(index);
documentApi = new DocumentApi(client);

        logMap.put("if_status", "READY");
        logMap.put("if_remark", "M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "(" + houseCnt + "건)");
        documentApi.createDocument(index, DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "99999", logMap);

if(xmlByte > 1024 * 1024 * 19){
    result.put("sendType", "FILE");
}

    // -- setCustomsManifestExp
    jsonArray = jsonArrayList.get(i);
houseOdJobNoList = odJobNoArrayList.get(i);
sendData = new JSONObject();
sendData.put("dataList", jsonArray);
jsonObject = ediCommonService.sendEbms(ediPortalMailboxId, "GOVCBR833", sendData, "LIST", token);
if(token == null){
    token = Util.null2str(jsonObject.get("token"));
}

    //


    			restData.put(ediPortalMailboxId + "_ORIG_CODE", origCode);	/** REST전송시 사서함별로 전송/수신받고 수신받은값 처리시 사서함별로 업데이트하기위한 값으로 ORIG_CODE사용 */
        restData.put(ediPortalMailboxId + "_LOG_LIST", logList);