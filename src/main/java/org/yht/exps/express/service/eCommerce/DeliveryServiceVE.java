package org.yht.exps.express.service.eCommerce;

import java.net.URI;

import net.minidev.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.*;

import net.minidev.json.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.api.InterfaceMapperVE;
import org.yht.exps.express.mapper.eCommerce.DeliveryMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.api.ResultLotte;
import org.yht.exps.express.model.eCommerce.delivery.*;
import org.yht.exps.express.model.eCommerce.delivery.search.SearchDeliveryMList;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.User;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.api.InterfaceServiceVE;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.model.AuthenticationToken;

@Slf4j
@Service
@Transactional
public class DeliveryServiceVE {

    @Autowired
    CustomProperties customProperties;

    @Autowired
    CommonMapper commonMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    DeliveryMapper deliveryMapper;
    @Autowired
    InterfaceMapperVE interfaceMapperVE;
    @Autowired
    EnterpriseMapper enterpriseMapper;

    @Autowired
    SmsUserService smsUserService;

    @Autowired
    CommonService commonService;
    @Resource(name = "interfaceServiceVE")
    InterfaceServiceVE interfaceServiceVE;
    @Autowired
    RestHighLevelClient client;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;

	@Value("${spring.profiles.active}")
	private String profile;
	
    /**
     * Delivery 각 배송사에 해당하는 데이터 조회
     *
     * @param UpdateDelivery
     * @return Map
     * @throws Exception
     */
    public Map<String, Object> selectSendDeliveryList(Map<String, Object> map, UpdateDelivery send, String singleFlag) throws Exception {
    	send.setProfileName(profile);
        //singleFlag는 모바일의 건별 배송사 변경시 사용하는경우의 플래그(0->사용x, 1-> 오더건 ,2 -> 멀티건)
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Map<String, Object> deliveryInterfaceInfo = null;
        Map<String, Object> commonDeliveryInterfaceInfo = null;
        List<Map<String, Object>> ems = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> korex = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> hanjin = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> lotte = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> kd = new ArrayList<Map<String, Object>>();
        String customerCode = "";
        String logisticsCode = "";
        List<Map<String, Object>> deliveryList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();
        Map<String, Object> errorMap;
        String errorStr, errorResult, deliveryCode, consigneeAddress, shipperAddress;

        //0은 모바일 단건화면과 상관없는 일반기능
        if (singleFlag.equals("0")) {

        	List<Map<String,Object>> multiOdList = new ArrayList<Map<String,Object>>();
        	List<Map<String,Object>> hblOdList = new ArrayList<Map<String,Object>>();

            //changeDelivery는 송장교체에서 온것이므로 별도로 setMultiOdList를 세팅함
            if(send.getDataList() != null && send.getDataList().size() > 0
                    && !Util.null2str(send.getChkPage()).equals("changeDelivery")) {
                for(Map<String,Object> dataMap : send.getDataList()) {
                    if(dataMap.get("multiOdJobNo") != null) {
                        multiOdList.add(dataMap);
                    }else {
                        hblOdList.add(dataMap);
                    }
                }
                send.setMultiOdList(multiOdList);
                send.setHblOdList(hblOdList);
            }
        	log.debug("sendChk ->"+ send);
            deliveryList = deliveryMapper.selectSendDeliveryListToMulti(send);
        } else if(singleFlag.equals("1")){
            //오더(모바일용)
            deliveryList = deliveryMapper.selectSendDeliveryListToOdSingle(send);
        }else if(singleFlag.equals("2")){
            //멀티(모바일용)
            deliveryList = deliveryMapper.selectSendDeliveryListToMultiSingle(send);
        }

        for (Map<String, Object> data : deliveryList) {
            if (!customerCode.equals(Util.null2str(data.get("AGENT_CODE"))) || !logisticsCode.equals(Util.null2str(data.get("DEST_PORT")))) {
                customerCode = Util.null2str(data.get("AGENT_CODE"));
                logisticsCode = Util.null2str(data.get("BOUND")); // 2025.01.03 목적항으로 집화점 조회
                map.put("srchCustomerCode", customerCode);
                map.put("srchLogisticsCode", logisticsCode);
                deliveryInterfaceInfo = deliveryMapper.selectDeliveryInterface(map);
                if (deliveryInterfaceInfo == null) {
                    /** 오더의 CUST_CODE_LV1코드로 등록된 TB_SMS_DELIVERY_COMP_INFO가 없을경우 COMMON으로 등록된 데이터 사용 */
                    deliveryInterfaceInfo = new HashMap<String, Object>();
//					if(commonDeliveryInterfaceInfo == null){
                    map.put("srchCustomerCode", "COMMON");
                    map.put("srchLogisticsCode", null);
                    commonDeliveryInterfaceInfo = deliveryMapper.selectDeliveryInterface(map);
                    if (commonDeliveryInterfaceInfo == null) {
                        commonDeliveryInterfaceInfo = new HashMap<String, Object>();
                    }
//					}
                    deliveryInterfaceInfo.putAll(commonDeliveryInterfaceInfo);
                }
            }
            log.debug("deliveryInterfaceInfo->"+ deliveryInterfaceInfo);
            //멀티인경우는 멀티의 deliverycode 즉 변경배송사를 기준으로 먼저 체크해야함
            if (!(Util.null2str(deliveryInterfaceInfo.get("KOREX_METHOD")).equals("DB"))) {
                Map<String, Object> chkMultCount = new HashMap<>();
                chkMultCount.put("odJobNo", Util.null2str(data.get("OD_JOB_NO")));
                if (deliveryMapper.countMulti(chkMultCount) > 0) {
                    deliveryCode = Util.null2str(data.get("DELIVERY_CODE_MULTI")).equals("") ? Util.null2str(data.get("DELIVERY_CODE"))
                            : Util.null2str(data.get("DELIVERY_CODE_MULTI"));
                } else {
                    deliveryCode = Util.null2str(data.get("DELIVERY_CODE"));
                }
            } else {
                deliveryCode = Util.null2str(data.get("DELIVERY_CODE"));
            }

            switch (deliveryCode) {
                case "EMS":
                    data.put("EMS_ID", deliveryInterfaceInfo.get("EMS_ID"));
                    ems.add(data);
                    break;
                case "KOREX":
                    data.put("deliveryInterfaceInfo", deliveryInterfaceInfo);
                    korex.add(data);
                    break;
                case "HANJIN":
                    data.put("deliveryInterfaceInfo", deliveryInterfaceInfo);
                    hanjin.add(data);
                    break;
                case "LOTTE":
                    data.put("deliveryInterfaceInfo", deliveryInterfaceInfo);
                    lotte.add(data);
                    break;
                case "KD":
                    data.put("deliveryInterfaceInfo", deliveryInterfaceInfo);
                    kd.add(data);
                    break;
            }

            /** 필수값체크 */
            errorStr = "";
            errorResult = "";
            consigneeAddress = Util.null2str(data.get("consigneeAddressLocConvert")).replace("\r", " ").replace("\n", " ");
            shipperAddress = Util.null2str(deliveryInterfaceInfo.get("LOTTE_ADDR")).replace("\r", " ").replace("\n", " ");

            if (Util.null2str(deliveryCode).equals("KOREX") && Util.null2str(data.get("DELIVERY_NO")).length() > 20) {
                errorStr += ", 멀티 운송장 번호";
                errorResult += ", " + Util.null2str(data.get("DELIVERY_NO"));
            }
            if (Util.null2str(data.get("consigneeNameLocConvert")).equals("")) {
                errorStr += ", 수하인명";
            }
            if (Util.null2str(data.get("CONSIGNEE_ZIPCODE")).equals("")) {
                errorStr += ", 수하인 우편번호";
            }
            if (Util.null2str(data.get("CONSIGNEE_TEL")).equals("")) {
                errorStr += ", 수하인 전화번호";
            }
            if (deliveryCode.equals("EMS")) {
                if (Util.null2str(data.get("commodityItem1")).equals("")) {
                    errorStr += ", 상품명";
                }
                if (consigneeAddress.equals("")) {
                    errorStr += ", 수하인 주소";
                }
            } else if (deliveryCode.equals("LOTTE")) {
                if (Util.null2str(data.get("CONSIGNEE_TEL")).equals("")) {
                    errorStr += ", 수하인 전화번호";
                }
                if (Util.null2str(data.get("commodityItemLocConvert")).equals("")) {
                    errorStr += ", 상품명";
                }
                if (consigneeAddress.equals("")) {
                    errorStr += ", 수하인 주소";
                }
//				if(Util.null2str(deliveryInterfaceInfo.get("LOTTE_NAME")).equals("")) {
                if (Util.null2str(data.get("shipperNameConvert")).equals("")) {
                    errorStr += ", 송하인명";
                }
                if (Util.null2str(deliveryInterfaceInfo.get("LOTTE_TEL")).equals("")) {
                    errorStr += ", 송하인 전화번호";
                }
                if (Util.null2str(deliveryInterfaceInfo.get("LOTTE_ZIPCODE")).equals("")) {
                    errorStr += ", 송하인 우편번호";
                }
                if (shipperAddress.equals("")) {
                    errorStr += ", 송하인 주소";
                }
            } else {
                if (Util.null2str(data.get("commodityItemLocConvert")).equals("")) {
                    errorStr += ", 상품명";
                }
                if (consigneeAddress.equals("") || (consigneeAddress.trim()).split(" ").length < 4) {
                    errorResult += ", " + consigneeAddress;
                    errorStr += ", 수하인 주소";
                }
                if (deliveryCode.equals("KOREX")) {
                    if (Util.null2str(data.get("shipperNameConvert")).equals("")) {
                        errorStr += ", 송하인명";
                    }
                }
            }

//			if(!errorStr.equals("")){
//				errorMap = new HashMap<String,Object>();
//				errorMap.put("messageType", "필수값오류");
//				errorMap.put("hsn", data.get("HSN"));
//				errorMap.put("blNo", data.get("HBL_NO"));
//				errorMap.put("errorStr", errorStr.substring(2));
//				if(!errorResult.equals("")) errorMap.put("errorResult", errorResult.substring(2));
//				errorList.add(errorMap);
            // 오류 건은 제외하고 전송하기 위해 리스트에서 삭제한다. - 2024.06.13
            //		data.clear();
//				switch (deliveryCode) {
//				case "EMS":
//					ems.remove(ems.size()-1);
//					break;
//				case "KOREX" :
//					korex.remove(korex.size()-1);
//					break;
//				case "HANJIN":
//					hanjin.remove(hanjin.size()-1);
//					break;
//				case "KD":
//					kd.remove(kd.size()-1);
//					break;
//				}
//			}
        }

        returnMap.put("EMS", ems);
        returnMap.put("KOREX", korex);
        returnMap.put("HANJIN", hanjin);
        returnMap.put("LOTTE", lotte);
        returnMap.put("KD", kd);
        if (errorList.size() > 0) {
            returnMap.put("errorList", errorList);
        }

        return returnMap;
    }

    //경동택배 전송
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendDeliveryKd(List<Map<String, Object>> deliveryListKd, String entCode) throws Exception {

        Map<String, Object> returnMap = new HashMap<>();
        List<Map<String, Object>> errorList = new ArrayList<>();
        List<Map<String, Object>> successedKdOdJobNos = new ArrayList<>();
        List<Map<String, Object>> successedKdOdJobNosMulti = new ArrayList<>();
        List<String> failKdPoNos = new ArrayList<>();
        List<String> failKdAddrPoNos = new ArrayList<>();
        List<String> failKdManPoNos = new ArrayList<>();
        List<String> failKdInterfacePoNos = new ArrayList<>();
        String sendYn = "N";

        int totalCnt = 0;
        int failedCnt = 0;
        int sendCnt = 0;

        SqlSession sqlSession = null;

        try {
            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
            log.info("###### 경동택배 인터페이스 시작 ##########");
            totalCnt = deliveryListKd.size();

            for (Map<String, Object> deliveryMapKd : deliveryListKd) {

                String poNo = Util.null2str(deliveryMapKd.get("CHK_PO_NO"));
                String odJobNo = Util.null2str(deliveryMapKd.get("OD_JOB_NO"));
                String consigneeAddress = Util.null2str(deliveryMapKd.get("consigneeAddressLocConvert"))
                        .replace("\r", " ").replace("\n", " ");

                List<String> itemErrors = new ArrayList<>(); // 해당 건의 오류 목록
                // 품목명
                String commodityItem = Util.null2str(deliveryMapKd.get("commodityItemLocConvert"));
                if (commodityItem.isEmpty()) {
                    itemErrors.add("[경동택배]배송요청 필수값 오류 (품목명)");
                    failKdManPoNos.add(poNo + "(오더부분품목명누락)");
                    log.info("경동택배 유효성오류 [품목명 누락] PO_NO={}", poNo);
                } else if (commodityItem.length() > 60) {
                    itemErrors.add("[경동택배]배송요청 길이초과 오류 (품목명 최대 60자)");
                    failKdManPoNos.add(poNo + "(오더부분품목명길이초과)");
                    log.info("경동택배 유효성오류 [품목명 길이초과] PO_NO={}", poNo);
                }
                //품목명멀티
                if (isMultiDelivery(deliveryMapKd)) {
                    if (Util.null2str(deliveryMapKd.get("commodityItemLocConvertMulti")).isEmpty()) {
                        itemErrors.add("[경동택배]배송요청 필수값 오류 (멀티품목명)");
                        failKdManPoNos.add(poNo + "(멀티부분품목명누락)");
                        log.info("경동택배 유효성오류(멀티) [품목명 누락] PO_NO={}", poNo);
                    } else if (Util.null2str(deliveryMapKd.get("commodityItemLocConvertMulti")).length() > 60) {
                        itemErrors.add("[경동택배]배송요청 길이초과 오류 (멀티품목명 최대 60자)");
                        failKdManPoNos.add(poNo + "(멀티부분품목길이초과");
                        log.info("경동택배 유효성오류(멀티) [품목명 길이초과] PO_NO={}", poNo);
                    }
                }
                // 고객사 주문번호
                if (Util.null2str(deliveryMapKd.get("PO_NO")).isEmpty()) {
                    itemErrors.add("[경동택배]배송요청 필수값 오류 (오더번호)");
                    failKdManPoNos.add(poNo + "오더번호누락");
                    log.info("경동택배 유효성오류 [오더번호누락 누락] PO_NO={}", poNo);
                }
//				// HBL이 존재하는건
//				if (!Util.null2str(deliveryMapKd.get("HBL_NO")).isEmpty()) {
//					itemErrors.add("[경동택배]배송요청 필수값 오류 (HBL존재하는 오더)");
//					failKdManPoNos.add(poNo+"HBL번호가 존재하는오더");
//					log.info("경동택배 유효성오류 [HBL번호가 존재하는오더] PO_NO={}", poNo);
//				}
                // 치수/무게 (멀티 요청건은 멀티 값만 검사, 단일 요청건은 오더 본체 값만 검사)
                if (isMultiDelivery(deliveryMapKd)) {
                    if (isDimOrWeightInvalidMulti(deliveryMapKd)) {
                        itemErrors.add("[경동택배]배송요청 필수값 오류(멀티) (가로/세로/높이/무게는 0보다 큰 값)");
                        failKdManPoNos.add(poNo + "(멀티부분가로,세로,높이,무게는0보다커야함)");
                        log.info("경동택배 유효성오류(멀티) [치수/무게 오류] PO_NO={}", poNo);
                    }
                } else {
                    if (isDimOrWeightInvalid(deliveryMapKd)) {
                        itemErrors.add("[경동택배]배송요청 필수값 오류 (가로/세로/높이/무게는 0보다 큰 값)");
                        failKdManPoNos.add(poNo + "(가로,세로,높이,무게는0보다커야함)");
                        log.info("경동택배 유효성오류 [치수/무게 오류] PO_NO={}", poNo);
                    }
                }
                // 수하인명
                String consigneeName = Util.null2str(deliveryMapKd.get("consigneeNameLocConvert"));
                if (consigneeName.isEmpty()) {
                    itemErrors.add("[경동택배]배송요청 필수값 오류 (수하인명)");
                    failKdManPoNos.add(poNo + "(수하인명누락)");
                    log.info("경동택배 유효성오류 [수하인명 누락] PO_NO={}", poNo + "(오더부분수하인명)");
                } else if (consigneeName.length() > 30) {
                    itemErrors.add("[경동택배]배송요청 길이초과 오류 (수하인명 최대 30자)");
                    failKdManPoNos.add(poNo + "(수하인길이초과)");
                    log.info("경동택배 유효성오류 [수하인명 길이초과] PO_NO={}", poNo + "(오더부분수하인명길이초과)");
                }

                // 물품금액
//				if ("".equals(Util.null2str(deliveryMapKd.get("AMOUNT_LOC"))) ||
//						Util.null2dbl(deliveryMapKd.get("AMOUNT_LOC")) <= 0) {
//					itemErrors.add("[경동택배]배송요청 필수값 오류 (물품금액은 0보다 큰 값)");
//					failKdManPoNos.add(poNo);
//					log.info("경동택배 유효성오류 [물품금액 오류] PO_NO={}", poNo);
//				}

                // 운임구분 (멀티 요청건은 멀티 운임만 검사, 단일 요청건은 오더 본체 운임만 검사)
                if (isMultiDelivery(deliveryMapKd)) {
                    if (Util.null2str(deliveryMapKd.get("DELIVERY_PAYMENT_TYPE_MULTI")).isEmpty()) {
                        itemErrors.add("[경동택배]배송요청 필수값 오류(멀티) (운임구분)");
                        failKdManPoNos.add(poNo + "(멀티부분운임)");
                        log.info("경동택배 유효성오류(멀티) [운임구분 누락] PO_NO={}", poNo);
                    }
                } else {
                    if (Util.null2str(deliveryMapKd.get("DELIVERY_PAYMENT_TYPE")).isEmpty()) {
                        itemErrors.add("[경동택배]배송요청 필수값 오류 (운임구분)");
                        failKdManPoNos.add(poNo + "(오더부분운임)");
                        log.info("경동택배 유효성오류 [운임구분 누락] PO_NO={}", poNo + "(오더부분운임)");
                    }
                }
                // 수하인 연락처
                if (Util.null2str(deliveryMapKd.get("consigneeTelConvert")).isEmpty()
                        && Util.null2str(deliveryMapKd.get("consigneeHpConvert")).isEmpty()) {
                    itemErrors.add("[경동택배]배송요청 필수값 오류 (수하인연락처)");
                    failKdManPoNos.add(poNo + "수하인연락처누락");
                    log.info("경동택배 유효성오류 [수하인연락처 누락] PO_NO={}", poNo);
                }
                // 수하인 우편번호
                String zipcode = Util.null2str(deliveryMapKd.get("consigneeZipcodeLocConvert"));
                if (zipcode.isEmpty()) {
                    itemErrors.add("[경동택배]배송요청 필수값 오류 (수하인우편번호)");
                    failKdManPoNos.add(poNo + "우편번호누락");
                    log.info("경동택배 유효성오류 [수하인우편번호 누락] PO_NO={}", poNo);
                } else if (!zipcode.matches("\\d{5}")) {
                    itemErrors.add("[경동택배]배송요청 형식 오류 (수하인우편번호 5자리 숫자)");
                    failKdManPoNos.add(poNo + "우편번호5자리형식");
                    log.info("경동택배 유효성오류 [수하인우편번호 형식오류] PO_NO={}", poNo);
                }
                // 수하인 주소 길이
                if (consigneeAddress.length() > 100) {
                    itemErrors.add("[경동택배]배송요청 길이초과 오류 (수하인주소 최대 100자)");
                    failKdAddrPoNos.add(poNo + "주소길이초과");
                    log.info("경동택배 유효성오류 [수하인주소 길이초과] PO_NO={} / 주소길이={}", poNo, consigneeAddress.length());
                }

                // 공통 에러 처리
                if (!itemErrors.isEmpty()) {
                    failedCnt++;
                    deliveryMapKd.put("sendStatus", "FAIL");

                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("messageType", "유효성오류");
                    errorMap.put("hsn", deliveryMapKd.get("HSN"));
                    errorMap.put("blNo", poNo);
                    errorMap.put("errorStr", String.join(" / ", itemErrors)); // ← 오류 한번에 표시
                    errorMap.put("errorResult", poNo);
                    errorList.add(errorMap);

                } else {
                    // 정상 전송 대상
                    sendCnt++;


                    String key = "E-COMMERCE_DELIVERY_RECEIPT_" + entCode;
                    JSONObject json = new JSONObject();
                    ListOperations<String, String> listOperations = redisTemplate.opsForList();

                    // 멀티 정보
                    json.put("DELIVERY_PAYMENT_TYPE_MULTI", deliveryMapKd.get("DELIVERY_PAYMENT_TYPE_MULTI"));
                    json.put("commodityItemLocConvertMulti", deliveryMapKd.get("commodityItemLocConvertMulti"));
                    json.put("DIM_WIDTH_MULTI", deliveryMapKd.get("DIM_WIDTH_MULTI"));
                    json.put("DIM_LENGTH_MULTI", deliveryMapKd.get("DIM_LENGTH_MULTI"));
                    json.put("DIM_HEIGHT_MULTI", deliveryMapKd.get("DIM_HEIGHT_MULTI"));
                    json.put("DIM_WIDTH", deliveryMapKd.get("DIM_WIDTH"));
                    json.put("DIM_LENGTH", deliveryMapKd.get("DIM_LENGTH"));
                    json.put("DIM_HEIGHT", deliveryMapKd.get("DIM_HEIGHT"));
                    json.put("VWT", deliveryMapKd.get("VWT"));
                    json.put("VWT_MULTI", deliveryMapKd.get("VWT_MULTI"));
                    json.put("DELIVERY_NO", deliveryMapKd.get("DELIVERY_NO"));
                    json.put("SEQ_NO_ID", deliveryMapKd.get("SEQ_NO_ID"));
                    json.put("MULTI_COUNT", deliveryMapKd.get("MULTI_COUNT"));
                    json.put("DELIVERY_CODE_MULTI", deliveryMapKd.get("DELIVERY_CODE_MULTI"));

                    // POD 관련
                    json.put("ECM360_POD_JOB_NO_MULTI", deliveryMapKd.get("ECM360_POD_JOB_NO_MULTI"));
                    json.put("ECM360_POD_JOB_NO", deliveryMapKd.get("ECM360_POD_JOB_NO"));
                    json.put("ECM410_POD_JOB_NO_MULTI", deliveryMapKd.get("ECM410_POD_JOB_NO_MULTI"));
                    json.put("ECM410_POD_JOB_NO", deliveryMapKd.get("ECM410_POD_JOB_NO"));
                    json.put("ECM340_POD_JOB_NO_MULTI", deliveryMapKd.get("ECM340_POD_JOB_NO_MULTI"));
                    json.put("ECM340_POD_JOB_NO", deliveryMapKd.get("ECM340_POD_JOB_NO"));
                    json.put("ECM504_POD_JOB_NO", deliveryMapKd.get("ECM504_POD_JOB_NO"));
                    json.put("ECM504_POD_JOB_NO_MULTI", deliveryMapKd.get("ECM504_POD_JOB_NO_MULTI"));


                    // 오더 기본 정보
                    json.put("HBL_NO", deliveryMapKd.get("HBL_NO"));
                    json.put("OD_JOB_NO", deliveryMapKd.get("OD_JOB_NO"));
                    json.put("HSN", deliveryMapKd.get("HSN"));
                    json.put("AGENT_CODE", deliveryMapKd.get("AGENT_CODE"));
                    json.put("DEST_PORT", deliveryMapKd.get("DEST_PORT"));
                    json.put("LAST_POD_STATUS", deliveryMapKd.get("LAST_POD_STATUS"));
                    json.put("VESSEL_FLIGHT", deliveryMapKd.get("VESSEL_FLIGHT"));
                    json.put("CHK_PO_NO", deliveryMapKd.get("CHK_PO_NO"));
                    json.put("PO_NO", deliveryMapKd.get("PO_NO"));
                    json.put("TR_CODE", deliveryMapKd.get("TR_CODE"));
                    json.put("DELIVERY_CODE", deliveryMapKd.get("DELIVERY_CODE"));
                    json.put("MBL_NO", deliveryMapKd.get("MBL_NO"));
                    json.put("DELIVERY_PAYMENT_TYPE", deliveryMapKd.get("DELIVERY_PAYMENT_TYPE"));
                    json.put("DELIVERY_NOTE", deliveryMapKd.get("DELIVERY_NOTE"));
                    json.put("BOUND", deliveryMapKd.get("BOUND"));
                    json.put("BOUND_TYPE", deliveryMapKd.get("BOUND_TYPE"));

                    // 수하인(Consignee) 정보
                    json.put("CONSIGNEE_NAME_LOC", deliveryMapKd.get("CONSIGNEE_NAME_LOC"));
                    json.put("CONSIGNEE_NAME", deliveryMapKd.get("CONSIGNEE_NAME"));
                    json.put("CONSIGNEE_ZIPCODE", deliveryMapKd.get("CONSIGNEE_ZIPCODE"));
                    json.put("CONSIGNEE_ADDRESS", deliveryMapKd.get("CONSIGNEE_ADDRESS"));
                    json.put("CONSIGNEE_ADDRESS_A", deliveryMapKd.get("CONSIGNEE_ADDRESS_A"));
                    json.put("CONSIGNEE_ADDRESS_B", deliveryMapKd.get("CONSIGNEE_ADDRESS_B"));
                    json.put("CONSIGNEE_TEL", deliveryMapKd.get("CONSIGNEE_TEL"));
                    json.put("CONSIGNEE_HP", deliveryMapKd.get("CONSIGNEE_HP"));
                    json.put("consigneeNameLocConvert", deliveryMapKd.get("consigneeNameLocConvert"));
                    json.put("consigneeZipcodeLocConvert", deliveryMapKd.get("consigneeZipcodeLocConvert"));
                    json.put("consigneeAddressLocConvert", deliveryMapKd.get("consigneeAddressLocConvert"));
                    json.put("consigneeTelConvert", deliveryMapKd.get("consigneeTelConvert"));
                    json.put("consigneeHpConvert", deliveryMapKd.get("consigneeHpConvert"));

                    // 화주(Shipper) 정보
                    json.put("shipperNameConvert", deliveryMapKd.get("shipperNameConvert"));
                    json.put("shipperZipcodeConvert", deliveryMapKd.get("shipperZipcodeConvert"));
                    json.put("shipperAddressLocConvert", deliveryMapKd.get("shipperAddressLocConvert"));
                    json.put("shipperTelConvert", deliveryMapKd.get("shipperTelConvert"));
                    json.put("shipperMobileConvert", deliveryMapKd.get("shipperMobileConvert"));

                    // 화물 치수/무게
                    json.put("PIECES", deliveryMapKd.get("PIECES"));
                    json.put("PIECES_REAL", deliveryMapKd.get("PIECES_REAL"));
                    json.put("CBM", deliveryMapKd.get("CBM"));
                    json.put("VWT", deliveryMapKd.get("VWT"));

                    // 금액
                    json.put("AMOUNT_CUR", deliveryMapKd.get("AMOUNT_CUR"));

                    // 품목 정보
                    json.put("commodityItemLocConvert", deliveryMapKd.get("commodityItemLocConvert"));
                    json.put("commodityItem1", deliveryMapKd.get("commodityItem1"));
                    json.put("commodityItem2", deliveryMapKd.get("commodityItem2"));
                    json.put("commodityItem3", deliveryMapKd.get("commodityItem3"));
                    json.put("commodityItem4", deliveryMapKd.get("commodityItem4"));
                    json.put("commodityItem5", deliveryMapKd.get("commodityItem5"));

                    // 등록자 정보
                    json.put("registerUserId", deliveryMapKd.get("registerUserId"));
                    json.put("registerUserName", deliveryMapKd.get("registerUserName"));
                    json.put("gmtTime", deliveryMapKd.get("gmtTime"));

                    //  Redis List에 Push
                    listOperations.rightPush(key, json.toString());
                    log.info("경동택배 전송 데이터 추가 PO_NO={} / 누적건수={}", poNo, sendCnt);
                    log.info("경동택배 Redis 저장 완료 PO_NO={} / KEY={}", poNo, key);

                    //일반 오더 성공 목록 (멀티 없는 순수 오더 또는 멀티 첫 시퀀스)
                    if (!isMultiDelivery(deliveryMapKd)
                            || Util.null2str(deliveryMapKd.get("SEQ_NO_ID")).equals("1")) {
                        Map<String, Object> successedKdOdJobNo = new HashMap<>();
                        successedKdOdJobNo.put("odJobNo", odJobNo);
                        successedKdOdJobNo.put("hblNo", Util.null2str(deliveryMapKd.get("HBL_NO")));
                        successedKdOdJobNo.put("podJobNo", deliveryMapKd.get("ECM340_POD_JOB_NO"));
                        successedKdOdJobNos.add(successedKdOdJobNo);
                        // 시퀀스1번 상태 오더add에 반영
                        Map<String, Object> updateMap = new HashMap<>();
                        updateMap.put("poNo", poNo);
                        updateMap.put("odJobNo", odJobNo);
                        orderMapper.updateOrderKdInfo(updateMap);
                    }
                    //멀티 오더 성공 목록
                    if (isMultiDelivery(deliveryMapKd)) {
                        Map<String, Object> successedKdOdJobNoMulti = new HashMap<>();
                        successedKdOdJobNoMulti.put("odJobNo", odJobNo);
                        successedKdOdJobNoMulti.put("hblNo", Util.null2str(deliveryMapKd.get("DELIVERY_NO")));
                        successedKdOdJobNoMulti.put("podJobNo", deliveryMapKd.get("ECM340_POD_JOB_NO_MULTI"));
                        successedKdOdJobNosMulti.add(successedKdOdJobNoMulti);
                    }
                }
            }
            sqlSession.flushStatements();
            sqlSession.commit();

        } catch (Exception e) {
            log.error(Util.getPrintStacTraceString(e));
        } finally {
            if (sqlSession != null) sqlSession.close();
        }

        log.info("###### 경동택배 인터페이스 종료 ##########");

        returnMap.put("sendKdYn", sendYn);
        returnMap.put("totalKdCnt", totalCnt);
        returnMap.put("successedKdCnt", sendCnt);
        returnMap.put("failKdCnt", failedCnt);
        returnMap.put("failKdPoNos", String.join(",", failKdPoNos));
        returnMap.put("failKdAddrPoNos", String.join(",", failKdAddrPoNos));
        returnMap.put("failKdManPoNos", String.join(",", failKdManPoNos));
        returnMap.put("failKdInterfacePoNos", String.join(",", failKdInterfacePoNos));
        returnMap.put("successedKdOdJobNos", successedKdOdJobNos);
        returnMap.put("successedKdOdJobNosMulti", successedKdOdJobNosMulti);
        returnMap.put("errorListKd", errorList);
        return returnMap;
    }

    //  멀티 전송 건 여부 (SEQ_NO_ID로 판단. 비어있으면 멀티 행이 있어도 그냥 오더 행)
    private boolean isMultiDelivery(Map<String, Object> map) {
        return Util.null2int(map.get("SEQ_NO_ID")) >= 1;
    }

    //  치수/무게 유효성 검사
    private boolean isDimOrWeightInvalid(Map<String, Object> map) {
        return Util.null2str(map.get("DIM_WIDTH")).isEmpty() || Util.null2dbl(map.get("DIM_WIDTH")) <= 0 ||
                Util.null2str(map.get("DIM_LENGTH")).isEmpty() || Util.null2dbl(map.get("DIM_LENGTH")) <= 0 ||
                Util.null2str(map.get("DIM_HEIGHT")).isEmpty() || Util.null2dbl(map.get("DIM_HEIGHT")) <= 0 ||
                Util.null2str(map.get("VWT")).isEmpty() || Util.null2dbl(map.get("VWT")) <= 0;
    }


    //  치수/무게 유효성 검사(멀티)
    private boolean isDimOrWeightInvalidMulti(Map<String, Object> map) {
        return Util.null2str(map.get("DIM_WIDTH_MULTI")).isEmpty() || Util.null2dbl(map.get("DIM_WIDTH_MULTI")) <= 0 ||
                Util.null2str(map.get("DIM_LENGTH_MULTI")).isEmpty() || Util.null2dbl(map.get("DIM_LENGTH_MULTI")) <= 0 ||
                Util.null2str(map.get("DIM_HEIGHT_MULTI")).isEmpty() || Util.null2dbl(map.get("DIM_HEIGHT_MULTI")) <= 0 ||
                Util.null2str(map.get("VWT_MULTI")).isEmpty() || Util.null2dbl(map.get("VWT_MULTI")) <= 0;
    }


    /**
     * Delivery 배송 요청 후 상태값 업데이트
     *
     * @param entCode
     * @param deliveryMap
     * @return
     * @throws Exception
     */
    public void updateDeliveryStatus(UpdateDelivery send, List<Map<String, Object>> odJobNoList) throws Exception {

        Map<String, Object> tmpMap = new HashMap<String, Object>();
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String podJobNo = "";
        log.debug("odJobNoList체크->"+ odJobNoList);
        SqlSession sqlSession = null;

        try {

            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
            OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);

            int updateCnt = 1;

            // POD_JOB_NO 채번
            tmpMap.put("entCode", send.getEntCode());
            tmpMap.put("gmtTime", send.getGmtTime());
            tmpMap.put("registerUserId", send.getRegisterUserId());
            tmpMap.put("updateUserId", send.getRegisterUserId());
            tmpMap.put("entBusinessCode", "");
            tmpMap.put("solution", "ECM");
            tmpMap.put("stockType", "POD");
            tmpMap.put("stockSign", "POD");
            tmpMap.put("stockSeqLength", "8");
            tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
            tmpMap.put("stockRemark", "ECM POD JOB NO");
            tmpMap.put("podStatus", "ECM340");
            tmpMap.put("deliveryCode", send.getDeliveryCode());
            tmpMap.put("podTableName", "TB_ECM_POD_04");
            /** 취약성점검 SQL Injection 방지 */
            Util.accessControlDatabase(tmpMap, "podTableName|gmtTime", false, null, null, null, null);

            List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
            Map<String, Object> tmpInsertPodMap = null;
            int seq = 1;
            for (Map<String, Object> odJobNoMap : odJobNoList) {
                dataMap.put("odJobNo", odJobNoMap.get("odJobNo"));
                dataMap.put("podStatus", "ECM340");
                dataMap.put("gmtTime", send.getGmtTime());
                dataMap.put("noCheckLastPodStatus", "Y");
                dataMap.put("podDate", LocalDate.now().toString());
                orderBatchMapper.updateOrderLastPod(dataMap);


                tmpInsertPodMap = new HashMap<String, Object>();
                if (odJobNoMap.get("podJobNo") != null) {
                    tmpInsertPodMap.put("podJobNo", odJobNoMap.get("podJobNo"));
                    updatePodList.add(tmpInsertPodMap);
                } else {
                    tmpInsertPodMap.put("odJobNo", odJobNoMap.get("odJobNo"));
                    tmpInsertPodMap.put("blNo", odJobNoMap.get("hblNo"));
                    tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
                    insertPodList.add(tmpInsertPodMap);
                }

                //deliveryMapper.updateDeliveryStatus(dataMap);

                if ((updateCnt++ % 1000) == 0) {
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }

            updateCnt = 1;
            for (Map<String, Object> insertPodMap : insertPodList) {
                tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
                tmpMap.put("blNo", insertPodMap.get("blNo"));
                tmpMap.put("podJobNo", insertPodMap.get("podJobNo"));
                orderBatchMapper.insertOrderPod(tmpMap);
                if ((updateCnt++ % 1000) == 0) {
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }
            updateCnt = 1;
            for (Map<String, Object> updatePodMap : updatePodList) {
                tmpMap.put("podJobNo", updatePodMap.get("podJobNo"));
                orderBatchMapper.updateOrderPod(tmpMap);
                if ((updateCnt++ % 1000) == 0) {
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }

            /** 배송요청시 미집하 POD삭제 */
            tmpMap.put("podStatus", "ECM350");
            for (Map<String, Object> odJobNoMap : odJobNoList) {
                tmpMap.put("odJobNo", odJobNoMap.get("odJobNo"));
                orderBatchMapper.deleteOrderPod(tmpMap);
                if ((updateCnt++ % 1000) == 0) {
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }

            sqlSession.flushStatements();
            sqlSession.clearCache();
            sqlSession.commit();


        } catch (Exception e) {
            throw e;
        } finally {
            sqlSession.close();
        }
    }


    //전화번호 분리유틸
    private String[] splitPhoneNumber(String phone, String type) {
        final String[] DEFAULT = {"000", "0000", "0000"};

        if (phone == null || phone.trim().isEmpty()) return DEFAULT;

        int len = phone.length();

        if ("1".equals(type) && !(len == 9 || len == 10 || len == 11 || len == 12)) return DEFAULT;
        if ("2".equals(type) && !(len == 9 || len == 10 || len == 11)) return DEFAULT;
        if ("3".equals(type) && !(len == 10 || len == 11)) return DEFAULT;

        switch (len) {
            case 9:
                return new String[]{phone.substring(0, 2), phone.substring(2, 5), phone.substring(5, 9)};
            case 10:
                return new String[]{phone.substring(0, 3), phone.substring(3, 6), phone.substring(6, 10)};
            case 11:
                return new String[]{phone.substring(0, 3), phone.substring(3, 7), phone.substring(7, 11)};
            case 12:
                return new String[]{phone.substring(0, 4), phone.substring(4, 8), phone.substring(8, 12)};
            default:
                return DEFAULT;
        }
    }

    /**
     * Delivery 대한통운 배송요청(oracle)(REST전송방식)
     *
     * @param entCode
     * @param deliveryMap
     * @return
     * @throws Exception
     */
    public Map<String, Object> sendDeliveryKorexRest(List<Map<String, Object>> deliveryListKorex, boolean recpFlag) throws Exception {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> interfaceInfoMap;
        Map<String, Object> tmpMap = new HashMap<String, Object>();
        Map<String, Object> errorMap = new HashMap<String, Object>();
        List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();

        String sendYn = "N";   // 배송사 배송요청 완료 여부
        String deliveryResult = null; // 배송사 배송요청 결과값
        String hblno = null;
        String odJobNo = "";
        String consigneeAddress = null;
        String failKorexHblNos = "";  // 대한통운 배송요청 실패 하우스번호
        String failKorexAddrHblNos = ""; // 대한통운 배송요청 주소 오류로 인한 실패 하우스번호
        String failKorexInterfaceHblNos = "";  // 배송인터페이스정보오류 하우스번호
        String errorStr = "";
        String errorResult = "";

        String logId = "";            // ES 로그용 아이디
        String index = "";            // ES 로그용 index
        String if_status = "";        // 처리상태값

        IndexApi indexApi = null;
        DocumentApi documentApi = null;

        List<Map<String, Object>> successedKorexOdJobNos = new ArrayList<Map<String, Object>>(); // 대한 배송요청 성공 하우스번호
        Map<String, Object> successedKorexOdJobNo = null;

        List<Map<String, Object>> successedKorexOdJobNosMulti = new ArrayList<Map<String, Object>>(); // 대한 배송요청 성공 하우스번호(멀티용)
        Map<String, Object> successedKorexOdJobNoMulti = null;

        int totalCnt = 0;
        int successedCnt = 0;
        int failedCnt = 0;

        boolean checkKor = false;

        log.info("###### 대한통운 인터페이스 시작(REST) ##########");
        totalCnt += deliveryListKorex.size();
        for (Map<String, Object> deliveryMapKorex : deliveryListKorex) {

            interfaceInfoMap = (Map<String, Object>) deliveryMapKorex.get("deliveryInterfaceInfo");
            checkKor = false;
            deliveryResult = "";
            errorStr = "";
            errorResult = "";

            hblno = Util.null2str(deliveryMapKorex.get("HBL_NO"));
            consigneeAddress = Util.null2str(deliveryMapKorex.get("consigneeAddressLocConvert")).replace("\r", " ").replace("\n", " ");
            odJobNo = Util.null2str(deliveryMapKorex.get("OD_JOB_NO"));

            // 한글 포함여부 체크
            checkKor = consigneeAddress.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");

            if (checkKor) {
                if (consigneeAddress.split(" ").length < 4) {
                    if ("".equals(failKorexAddrHblNos))
                        failKorexAddrHblNos = hblno;
                    else
                        failKorexAddrHblNos += "," + hblno;

                    failedCnt++;
                    deliveryResult = "N";
                    if_status = "FAIL";
                    errorStr = "[대한통운]배송요청 수하인주소 오류";
                    errorResult = consigneeAddress;
                } else {
                    //전송부분
//                    deliveryResult = interfaceToDeliveryKorexRest(interfaceInfoMap, deliveryMapKorex,recpFlag);
                    try {
                        //     String redisKey = "E-COMMERCE_DELIVERY_RETURN_" + interfaceInfoMap.get("ENT_CODE");//반품테스트용
                        String redisKey = "";
                        if(recpFlag==true){
                            redisKey =   "E-COMMERCE_DELIVERY_RETURN_" + interfaceInfoMap.get("ENT_CODE");
                        }else {
                            redisKey =   "E-COMMERCE_DELIVERY_RECEIPT_" + interfaceInfoMap.get("ENT_CODE");
                        }

                        Long pushResult = redisTemplate.opsForList().rightPush(redisKey, new ObjectMapper().writeValueAsString(deliveryMapKorex));
                        if (pushResult != null && pushResult > 0) {
                            deliveryResult = "D010";
                            log.info("[대한통운REST][{}] Redis 적재 완료 HBL={}, KEY={}", (recpFlag ? "반품" : "배송접수"), hblno, redisKey);
                        } else {
                            deliveryResult = "D030";
                            log.warn("[대한통운REST][{}] Redis 적재 실패(결과값 이상) HBL={}, result={}", (recpFlag ? "반품" : "배송접수"), hblno, pushResult);
                        }
                    } catch (Exception e) {
                        log.error("[대한통운REST] Redis push 오류 HBL={} : {}", hblno, e.getMessage(), e);
                        deliveryResult = "D030";
                    }

                    if ("D010".equals(deliveryResult)) { // 정상적으로 요청완료
                        //해당 건 자체가 멀티행인지로 판정(오더에 멀티가 존재하는지가 아니라 접수중인 건이 멀티행인지)
                        //멀티행은 SEQ_NO_ID가 1이상, 오더본체는 SEQ_NO_ID가 널/빈문자
                        boolean multiChk = false;
                        String seqNoIdStr = Util.null2str(deliveryMapKorex.get("SEQ_NO_ID"));
                        if (!seqNoIdStr.isEmpty() && Util.null2int(seqNoIdStr) >= 1) {
                            multiChk = true;
                        }
                        //일반오더건처리
                        if (multiChk) {
                            if (Util.null2str(deliveryMapKorex.get("SEQ_NO_ID")).equals("1")) {
                                successedKorexOdJobNo = new HashMap<String, Object>();
                                successedKorexOdJobNo.put("odJobNo", odJobNo);
                                successedKorexOdJobNo.put("hblNo", hblno);
                                successedKorexOdJobNo.put("podJobNo", deliveryMapKorex.get("ECM340_POD_JOB_NO"));
                                successedKorexOdJobNos.add(successedKorexOdJobNo);
                            }
                        } else {
                            successedKorexOdJobNo = new HashMap<String, Object>();
                            successedKorexOdJobNo.put("odJobNo", odJobNo);
                            successedKorexOdJobNo.put("hblNo", hblno);
                            successedKorexOdJobNo.put("podJobNo", deliveryMapKorex.get("ECM340_POD_JOB_NO"));
                            successedKorexOdJobNos.add(successedKorexOdJobNo);
                        }
                        //멀티건처리
                        if (multiChk) {
                            successedKorexOdJobNoMulti = new HashMap<String, Object>();
                            successedKorexOdJobNoMulti.put("odJobNo", odJobNo);
                            successedKorexOdJobNoMulti.put("podJobNo", deliveryMapKorex.get("ECM340_POD_JOB_NO_MULTI"));
                            successedKorexOdJobNoMulti.put("hblNo", deliveryMapKorex.get("DELIVERY_NO"));
                            successedKorexOdJobNosMulti.add(successedKorexOdJobNoMulti);
                        }
                        successedCnt++;
                        if_status = "SEND";
                    } else if ("D020".equals(deliveryResult)) {    /** 배송사 인터페이스에 배송사 계정 정보 없음 */
                        if ("".equals(failKorexInterfaceHblNos))
                            failKorexInterfaceHblNos = hblno;
                        else
                            failKorexInterfaceHblNos += "," + hblno;

                        failedCnt++;
                        if_status = "FAIL";
                        errorStr = "[대한통운]배송요청 interface정보 오류";
                    } else if ("D031".equals(deliveryResult)) {    /** 수하인상세주소 200BYTE 초과 */
                        if ("".equals(failKorexAddrHblNos))
                            failKorexAddrHblNos = hblno;
                        else
                            failKorexAddrHblNos += "," + hblno;

                        failedCnt++;
                        if_status = "FAIL";
                        errorStr = "[대한통운]배송요청 수하인주소 오류";
                        errorResult = consigneeAddress;
                    } else if ("D035".equals(deliveryResult)) {    /** 수하인 우편번호 6BYTE 초과 */
                        if ("".equals(failKorexAddrHblNos))
                            failKorexAddrHblNos = hblno;
                        else
                            failKorexAddrHblNos += "," + hblno;

                        failedCnt++;
                        if_status = "FAIL";
                        errorStr = "[대한통운]배송요청 수하인 우편번호 오류";
                        errorResult = consigneeAddress;
                    } else if ("D030".equals(deliveryResult)) {    /** 대한통운 인터페이스 DB2DB Insert Error */
                        if ("".equals(failKorexHblNos))
                            failKorexHblNos = hblno;
                        else
                            failKorexHblNos += "," + hblno;

                        failedCnt++;
                        if_status = "FAIL";
                        errorStr = "[대한통운]배송요청 오류";
                    }
                }
            } else {
                if ("".equals(failKorexAddrHblNos))
                    failKorexAddrHblNos = hblno;
                else
                    failKorexAddrHblNos += "," + hblno;

                failedCnt++;
                deliveryResult = "D034";// 수하인 주소가 한글이 X
                errorStr = "[대한통운]배송요청 수하인주소 오류";
                errorResult = consigneeAddress;
            }

            if (!"".equals(errorStr)) {
                errorMap = new HashMap<String, Object>();
                errorMap.put("messageType", "유효성오류");
                errorMap.put("hsn", deliveryMapKorex.get("HSN"));
                errorMap.put("blNo", hblno);
                errorMap.put("errorStr", errorStr);
                errorMap.put("errorResult", errorResult);
                errorList.add(errorMap);
            }


            // history 등록
            tmpMap = new HashMap<String, Object>();
            logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
            index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");

            // 처리결과에 따른 로그 기록 history
            tmpMap.clear();
            tmpMap.put("ent_code", interfaceInfoMap.get("ENT_CODE"));
            tmpMap.put("customer_code", interfaceInfoMap.get("CUSTOMER_CODE"));         // 고객코드
            tmpMap.put("solution", "ECM");                                     // SOLUTION
            tmpMap.put("bound", deliveryMapKorex.get("BOUND"));                    // 바운드
            tmpMap.put("bound_type", deliveryMapKorex.get("BOUND_TYPE"));            // 바운드타입
            tmpMap.put("hbl_no", deliveryMapKorex.get("HBL_NO"));                     // HBL_NO
            tmpMap.put("if_service_code", "KOREX");
            tmpMap.put("if_doc_code", "SEND_COURIER");
            tmpMap.put("if_doc_name", "택배사전송");                         // 문서분류명 (AIR ORDER, SEA ORDER)
            tmpMap.put("if_doc_reference_no", deliveryMapKorex.get("MBL_NO"));         // 문서번호
            tmpMap.put("if_vender", deliveryMapKorex.get("AGENT_CODE"));            // 중개사업자
            tmpMap.put("if_send_recv_type", "S");                                 // 송수신타입(S:SEND, R:RECV)
            tmpMap.put("if_send_recv_method", "DB to DB");                                 // 송수신방법
            tmpMap.put("if_file_path", "");                                         // 파일경로
            tmpMap.put("if_file_name", "");                                 // 파일명
            tmpMap.put("if_status", if_status);                                     // 처리상태
            tmpMap.put("if_status_info", "");                                     // 송수신상태_설명
            tmpMap.put("if_sender_id", deliveryMapKorex.get("AGENT_CODE"));                                     // 송신식별자
            tmpMap.put("if_sender_name", "");                                     // 송신자명
            tmpMap.put("if_receiver_id", "");                                     // 수신식별자
            tmpMap.put("if_receiver_name", "");                                     // 수신자명
            tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));                 // DATE
            tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));             // DATE_TIME
            tmpMap.put("if_remark", "KOREX");                                         // 비고( 에러로그 등)
            tmpMap.put("register_user_id", deliveryMapKorex.get("registerUserId"));
            tmpMap.put("register_user_name", deliveryMapKorex.get("registerUserName"));
            tmpMap.put("register_date_time", System.currentTimeMillis());
            tmpMap.put("es_job_no", logId);

            indexApi = new IndexApi(client);
            if (!indexApi.existIndex(index)) indexApi.createIndex(index);

            documentApi = new DocumentApi(client);
            documentApi.createDocument(index, logId, tmpMap);

            if ("D010".equals(deliveryResult)) { // 정상적으로 요청완료
                //status = 1;
                sendYn = "Y";
            } else {
                sendYn = deliveryResult;
            }

        }

        log.info("###### 대한통운 인터페이스 종료(REST) - 전체:{} / 성공:{} / 실패:{} / 오더성공:{}건 / 멀티성공:{}건 ##########",
                totalCnt, successedCnt, failedCnt, successedKorexOdJobNos.size(), successedKorexOdJobNosMulti.size());

        resultMap.put("sendKorexYn", sendYn);
        resultMap.put("totalKorexCnt", totalCnt);
        resultMap.put("successedKorexCnt", successedCnt);
        resultMap.put("successedKorexOdJobNos", successedKorexOdJobNos);
        resultMap.put("successedKorexOdJobNosMulti", successedKorexOdJobNosMulti);
        resultMap.put("failKorexCnt", failedCnt);
        resultMap.put("failKorexHblNos", failKorexHblNos);
        resultMap.put("failKorexAddrHblNos", failKorexAddrHblNos);
        resultMap.put("failKorexInterfaceHblNos", failKorexInterfaceHblNos);
        resultMap.put("errorListKorex", errorList);

        return resultMap;
    }


    /**
     * Delivery 배송 요청 후 상태값 업데이트 (멀티건처리용)
     *
     * @param entCode
     * @param deliveryMap
     * @return
     * @throws Exception
     */
    public void updateDeliveryStatusMulti(UpdateDelivery send, List<Map<String, Object>> odJobNoList) throws Exception {

        Map<String, Object> tmpMap = new HashMap<String, Object>();
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String podJobNo = "";

        SqlSession sqlSession = null;

        long procStart = System.currentTimeMillis();
        log.info("[배송접수-멀티상태반영] START - 대상 멀티건수={}, 배송사={}", (odJobNoList == null ? 0 : odJobNoList.size()), send.getDeliveryCode());

        try {

            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
            OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);

            int updateCnt = 1;

            // POD_JOB_NO 채번
            tmpMap.put("entCode", send.getEntCode());
            tmpMap.put("gmtTime", send.getGmtTime());
            tmpMap.put("registerUserId", send.getRegisterUserId());
            tmpMap.put("updateUserId", send.getRegisterUserId());
            tmpMap.put("entBusinessCode", "");
            tmpMap.put("solution", "ECM");
            tmpMap.put("stockType", "POD");
            tmpMap.put("stockSign", "POD");
            tmpMap.put("stockSeqLength", "8");
            tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
            tmpMap.put("stockRemark", "ECM POD JOB NO");
            tmpMap.put("podStatus", "ECM340");
            tmpMap.put("deliveryCode", send.getDeliveryCode());
            tmpMap.put("podTableName", "TB_ECM_POD_04_M");
            /** 취약성점검 SQL Injection 방지 */
            Util.accessControlDatabase(tmpMap, "podTableName|gmtTime", false, null, null, null, null);

            List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
            Map<String, Object> tmpInsertPodMap = null;
            int seq = 1;
            for (Map<String, Object> odJobNoMap : odJobNoList) {
                dataMap.put("odJobNo", odJobNoMap.get("odJobNo"));
                dataMap.put("podStatus", "ECM340");
                dataMap.put("gmtTime", send.getGmtTime());
                dataMap.put("noCheckLastPodStatus", "Y");
                orderBatchMapper.updateOrderLastPod(dataMap);

                // 멀티건 배송상태(RETURN_DELIVERY_STATUS)를 ECM340으로 반영
                Map<String, Object> multiStatusMap = new HashMap<String, Object>();
                multiStatusMap.put("odJobNo", odJobNoMap.get("odJobNo"));
                multiStatusMap.put("deliveryNo", odJobNoMap.get("hblNo"));
                multiStatusMap.put("podStatus", "ECM340");
                orderBatchMapper.updateOdMultiStatus(multiStatusMap);
                log.info("[배송접수-멀티상태반영] ECM340 반영 odJobNo={}, deliveryNo={}", odJobNoMap.get("odJobNo"), odJobNoMap.get("hblNo"));

//                dataMap.put("deliveryNo", odJobNoMap.get("hblNo"));
//                dataMap.put("statusType", "DELIVERY_REG");
//                dataMap.put("podDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                orderBatchMapper.updateOdMultiDate(dataMap);

                tmpInsertPodMap = new HashMap<String, Object>();
                if (odJobNoMap.get("podJobNo") != null) {
                    tmpInsertPodMap.put("podJobNo", odJobNoMap.get("podJobNo"));
                    updatePodList.add(tmpInsertPodMap);
                } else {
                    tmpInsertPodMap.put("odJobNo", odJobNoMap.get("odJobNo"));
                    tmpInsertPodMap.put("blNo", odJobNoMap.get("hblNo"));
                    tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
                    insertPodList.add(tmpInsertPodMap);
                }

                //deliveryMapper.updateDeliveryStatus(dataMap);

                if ((updateCnt++ % 1000) == 0) {
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }

            updateCnt = 1;
            for (Map<String, Object> insertPodMap : insertPodList) {
                tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
                tmpMap.put("blNo", insertPodMap.get("blNo"));
                tmpMap.put("podJobNo", insertPodMap.get("podJobNo"));
                orderBatchMapper.insertOrderPod(tmpMap);
                if ((updateCnt++ % 1000) == 0) {
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }
            updateCnt = 1;
            for (Map<String, Object> updatePodMap : updatePodList) {
                tmpMap.put("podJobNo", updatePodMap.get("podJobNo"));
                orderBatchMapper.updateOrderPod(tmpMap);
                if ((updateCnt++ % 1000) == 0) {
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }

            /** 배송요청시 미집하 POD삭제 */
            tmpMap.put("podStatus", "ECM350");
            for (Map<String, Object> odJobNoMap : odJobNoList) {
                tmpMap.put("odJobNo", odJobNoMap.get("odJobNo"));
                orderBatchMapper.deleteOrderPod(tmpMap);
                if ((updateCnt++ % 1000) == 0) {
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }

            sqlSession.flushStatements();
            sqlSession.clearCache();
            sqlSession.commit();

            log.info("[배송접수-멀티상태반영] END - 처리완료 멀티건수={}, 소요시간={}ms",
                    (odJobNoList == null ? 0 : odJobNoList.size()), (System.currentTimeMillis() - procStart));

        } catch (Exception e) {
            log.error("[배송접수-멀티상태반영] 처리 중 오류 발생 : {}", e.getMessage(), e);
            throw e;
        } finally {
            sqlSession.close();
        }
    }

//    /**
//     * Delivery 반품요청전송
//     *
//     * @param entCode
//     * @param deliveryMap
//     * @return
//     * @throws Exception
//     */
//    public Map<String, Object> sendDeliveryKorexReturn(List<Map<String, Object>> deliveryListKorex, String entCode) throws Exception {
//
//        Map<String, Object> resultMap = new HashMap<>();
//        Map<String, Object> tmpMap;
//        Map<String, Object> interfaceInfoMap;
//
//        String hblno;
//        String odJobNo;
//        String logId;
//        String index;
//
//        List<Map<String, Object>> successedKorexOdJobNos = new ArrayList<Map<String, Object>>();
//        Map<String, Object> successedKorexOdJobNo = null;
//        List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();
//        List<Map<String, Object>> successedKorexOdJobNosMulti = new ArrayList<Map<String, Object>>(); // 멀티용
//        Map<String, Object> successedKorexOdJobNoMulti = null;// 멀티용
//        int totalCnt = deliveryListKorex.size();
//        int successedCnt = 0;
//        int failedCnt = 0;
//
//        IndexApi indexApi;
//        DocumentApi documentApi;
//
//        log.info("###### 대한통운 반품요청 시작 ##########");
//
//        for (Map<String, Object> deliveryMapKorex : deliveryListKorex) {
//            interfaceInfoMap = (Map<String, Object>) deliveryMapKorex.get("deliveryInterfaceInfo");
//
//            hblno = Util.null2str(deliveryMapKorex.get("HBL_NO"));
//            odJobNo = Util.null2str(deliveryMapKorex.get("OD_JOB_NO"));
//            //멀티여부 확인
//            //해당 건 자체가 멀티행인지로 판정(오더에 멀티가 존재하는지가 아니라 접수중인 건이 멀티행인지)
//            //멀티행은 SEQ_NO_ID가 1이상, 오더본체는 SEQ_NO_ID가 널/빈문자
//            boolean multiChk = false;
//            String seqNoIdStr = Util.null2str(deliveryMapKorex.get("SEQ_NO_ID"));
//            if (!seqNoIdStr.isEmpty() && Util.null2int(seqNoIdStr) >= 1) {
//                multiChk = true;
//            }
//            String ifStatus = "SEND";
//            String errorMsg = "";
//
//            //  Redis Push
//            try {
//                String key = "E-COMMERCE_DELIVERY_RETURN_" + entCode;
//                JSONObject json = new JSONObject();
//
//                // 멀티 정보
//                json.put("DELIVERY_PAYMENT_TYPE_MULTI", deliveryMapKorex.get("DELIVERY_PAYMENT_TYPE_MULTI"));
//                json.put("commodityItemLocConvertMulti", deliveryMapKorex.get("commodityItemLocConvertMulti"));
//                json.put("DIM_WIDTH_MULTI", deliveryMapKorex.get("DIM_WIDTH_MULTI"));
//                json.put("DIM_LENGTH_MULTI", deliveryMapKorex.get("DIM_LENGTH_MULTI"));
//                json.put("DIM_HEIGHT_MULTI", deliveryMapKorex.get("DIM_HEIGHT_MULTI"));
//                json.put("DIM_WIDTH", deliveryMapKorex.get("DIM_WIDTH"));
//                json.put("DIM_LENGTH", deliveryMapKorex.get("DIM_LENGTH"));
//                json.put("DIM_HEIGHT", deliveryMapKorex.get("DIM_HEIGHT"));
//                json.put("GWT_MULTI",                    deliveryMapKorex.get("GWT_MULTI"));
//                json.put("VWT", deliveryMapKorex.get("VWT"));
//                json.put("VWT_MULTI", deliveryMapKorex.get("VWT_MULTI"));
//                json.put("DELIVERY_NO", deliveryMapKorex.get("DELIVERY_NO"));
//                json.put("SEQ_NO_ID", deliveryMapKorex.get("SEQ_NO_ID"));
//                json.put("MULTI_COUNT", deliveryMapKorex.get("MULTI_COUNT"));
//
//                // POD 관련
//                json.put("ECM360_POD_JOB_NO_MULTI", deliveryMapKorex.get("ECM360_POD_JOB_NO_MULTI"));
//                json.put("ECM360_POD_JOB_NO", deliveryMapKorex.get("ECM360_POD_JOB_NO"));
//                json.put("ECM410_POD_JOB_NO_MULTI", deliveryMapKorex.get("ECM410_POD_JOB_NO_MULTI"));
//                json.put("ECM410_POD_JOB_NO", deliveryMapKorex.get("ECM410_POD_JOB_NO"));
//                json.put("ECM340_POD_JOB_NO_MULTI", deliveryMapKorex.get("ECM340_POD_JOB_NO_MULTI"));
//                json.put("ECM340_POD_JOB_NO", deliveryMapKorex.get("ECM340_POD_JOB_NO"));
//                json.put("ECM504_POD_JOB_NO_MULTI", deliveryMapKorex.get("ECM504_POD_JOB_NO_MULTI"));
//                json.put("ECM504_POD_JOB_NO", deliveryMapKorex.get("ECM504_POD_JOB_NO"));
//
//                // 오더 기본 정보
//                json.put("HBL_NO", deliveryMapKorex.get("HBL_NO"));
//                json.put("OD_JOB_NO", deliveryMapKorex.get("OD_JOB_NO"));
//                json.put("HSN", deliveryMapKorex.get("HSN"));
//                json.put("AGENT_CODE", deliveryMapKorex.get("AGENT_CODE"));
//                json.put("DEST_PORT", deliveryMapKorex.get("DEST_PORT"));
//                json.put("LAST_POD_STATUS", deliveryMapKorex.get("LAST_POD_STATUS"));
//                json.put("VESSEL_FLIGHT", deliveryMapKorex.get("VESSEL_FLIGHT"));
//                json.put("CHK_PO_NO", deliveryMapKorex.get("CHK_PO_NO"));
//                json.put("PO_NO", deliveryMapKorex.get("PO_NO"));
//                json.put("TR_CODE", deliveryMapKorex.get("TR_CODE"));
//                json.put("DELIVERY_CODE", deliveryMapKorex.get("DELIVERY_CODE"));
//                json.put("MBL_NO", deliveryMapKorex.get("MBL_NO"));
//                json.put("DELIVERY_PAYMENT_TYPE", deliveryMapKorex.get("DELIVERY_PAYMENT_TYPE"));
//                json.put("DELIVERY_NOTE", deliveryMapKorex.get("DELIVERY_NOTE"));
//                json.put("BOUND", deliveryMapKorex.get("BOUND"));
//                json.put("BOUND_TYPE", deliveryMapKorex.get("BOUND_TYPE"));
//
//                // 수하인 정보
//                json.put("CONSIGNEE_NAME_LOC", deliveryMapKorex.get("CONSIGNEE_NAME_LOC"));
//                json.put("CONSIGNEE_NAME", deliveryMapKorex.get("CONSIGNEE_NAME"));
//                json.put("CONSIGNEE_ZIPCODE", deliveryMapKorex.get("CONSIGNEE_ZIPCODE"));
//                json.put("CONSIGNEE_ADDRESS", deliveryMapKorex.get("CONSIGNEE_ADDRESS"));
//                json.put("CONSIGNEE_ADDRESS_A", deliveryMapKorex.get("CONSIGNEE_ADDRESS_A"));
//                json.put("CONSIGNEE_ADDRESS_B", deliveryMapKorex.get("CONSIGNEE_ADDRESS_B"));
//                json.put("CONSIGNEE_TEL", deliveryMapKorex.get("CONSIGNEE_TEL"));
//                json.put("CONSIGNEE_HP", deliveryMapKorex.get("CONSIGNEE_HP"));
//                json.put("consigneeNameLocConvert", deliveryMapKorex.get("consigneeNameLocConvert"));
//                json.put("consigneeZipcodeLocConvert", deliveryMapKorex.get("consigneeZipcodeLocConvert"));
//                json.put("consigneeAddressLocConvert", deliveryMapKorex.get("consigneeAddressLocConvert"));
//                json.put("consigneeTelConvert", deliveryMapKorex.get("consigneeTelConvert"));
//                json.put("consigneeHpConvert", deliveryMapKorex.get("consigneeHpConvert"));
//
//                // 화주 정보
//                json.put("shipperNameConvert", deliveryMapKorex.get("shipperNameConvert"));
//                json.put("shipperZipcodeConvert", deliveryMapKorex.get("shipperZipcodeConvert"));
//                json.put("shipperAddressLocConvert", deliveryMapKorex.get("shipperAddressLocConvert"));
//                json.put("shipperTelConvert", deliveryMapKorex.get("shipperTelConvert"));
//                json.put("shipperMobileConvert", deliveryMapKorex.get("shipperMobileConvert"));
//
//                // 화물 치수/무게
//                json.put("PIECES", deliveryMapKorex.get("PIECES"));
//                json.put("PIECES_REAL", deliveryMapKorex.get("PIECES_REAL"));
//                json.put("CBM", deliveryMapKorex.get("CBM"));
//                json.put("VWT", deliveryMapKorex.get("VWT"));
//
//                // 금액
//                json.put("AMOUNT_CUR", deliveryMapKorex.get("AMOUNT_CUR"));
//
//                // 품목 정보
//                json.put("commodityItemLocConvert", deliveryMapKorex.get("commodityItemLocConvert"));
//                json.put("commodityItem1", deliveryMapKorex.get("commodityItem1"));
//                json.put("commodityItem2", deliveryMapKorex.get("commodityItem2"));
//                json.put("commodityItem3", deliveryMapKorex.get("commodityItem3"));
//                json.put("commodityItem4", deliveryMapKorex.get("commodityItem4"));
//                json.put("commodityItem5", deliveryMapKorex.get("commodityItem5"));
//
//                // 등록자 정보
//                json.put("registerUserId", deliveryMapKorex.get("registerUserId"));
//                json.put("registerUserName", deliveryMapKorex.get("registerUserName"));
//                json.put("gmtTime", deliveryMapKorex.get("gmtTime"));
//
//                Long pushResult = redisTemplate.opsForList().rightPush(key, json.toString());
//                if (pushResult != null && pushResult > 0) {
//                    log.info("Redis 저장 완료 PO_NO={} / KEY={}", hblno, key);
//
//                    // 성공 목록 누적
//                    if (multiChk) {
//                        if (Util.null2str(deliveryMapKorex.get("SEQ_NO_ID")).equals("1")) {
//                            successedKorexOdJobNo = new HashMap<String, Object>();
//                            successedKorexOdJobNo.put("odJobNo", odJobNo);
//                            successedKorexOdJobNo.put("hblNo", hblno);
//                            successedKorexOdJobNo.put("podJobNo", deliveryMapKorex.get("ECM504_POD_JOB_NO"));
//                            successedKorexOdJobNos.add(successedKorexOdJobNo);
//                        }
//                    } else {
//                        successedKorexOdJobNo = new HashMap<String, Object>();
//                        successedKorexOdJobNo.put("odJobNo", odJobNo);
//                        successedKorexOdJobNo.put("hblNo", hblno);
//                        successedKorexOdJobNo.put("podJobNo", deliveryMapKorex.get("ECM504_POD_JOB_NO"));
//                        successedKorexOdJobNos.add(successedKorexOdJobNo);
//                    }
//                    //멀티건처리
//                    if (multiChk) {
//                        successedKorexOdJobNoMulti = new HashMap<String, Object>();
//                        successedKorexOdJobNoMulti.put("odJobNo", odJobNo);
//                        successedKorexOdJobNoMulti.put("podJobNo", deliveryMapKorex.get("ECM504_POD_JOB_NO_MULTI"));
//                        successedKorexOdJobNoMulti.put("hblNo", deliveryMapKorex.get("DELIVERY_NO"));
//                        successedKorexOdJobNosMulti.add(successedKorexOdJobNoMulti);
//                    }
//                    //Redis push 성공 건 카운트(기존에 누락되어 successedKorexCnt가 항상 0이던 문제 수정)
//                    successedCnt++;
//
//                } else {
//                    ifStatus = "ERROR";
//                    errorMsg = "Redis rightPush 반환값 이상 (result=" + pushResult + ")";
//                    log.error("Redis 저장 실패 PO_NO={} / KEY={} / {}", hblno, key, errorMsg);
//
//                    Map<String, Object> errorMap = new HashMap<>();
//                    errorMap.put("odJobNo", odJobNo);
//                    errorMap.put("hblNo", hblno);
//                    errorMap.put("errorMsg", errorMsg);
//                    errorList.add(errorMap);
//                    failedCnt++;
//                }
//
//            } catch (Exception e) {
//                ifStatus = "ERROR";
//                errorMsg = "Redis 저장 예외: " + e.getMessage();
//                log.error("Redis 저장 예외 PO_NO={} / ERROR={}", hblno, e.getMessage(), e);
//
//                Map<String, Object> errorMap = new HashMap<>();
//                errorMap.put("odJobNo", odJobNo);
//                errorMap.put("hblNo", hblno);
//                errorMap.put("errorMsg", errorMsg);
//                errorList.add(errorMap);
//                failedCnt++;
//            }
//
//            //  ES 로그 기록 (Redis 성공/실패 무관하게 항상 기록)─
//            try {
//                logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
//                index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//
//                tmpMap = new HashMap<>();
//                tmpMap.put("ent_code", interfaceInfoMap.get("ENT_CODE"));
//                tmpMap.put("customer_code", interfaceInfoMap.get("CUSTOMER_CODE"));
//                tmpMap.put("solution", "ECM");
//                tmpMap.put("bound", deliveryMapKorex.get("BOUND"));
//                tmpMap.put("bound_type", deliveryMapKorex.get("BOUND_TYPE"));
//                tmpMap.put("hbl_no", deliveryMapKorex.get("HBL_NO"));
//                tmpMap.put("if_service_code", "KOREX");
//                tmpMap.put("if_doc_code", "SEND_COURIER");
//                tmpMap.put("if_doc_name", "택배사전송");
//                tmpMap.put("if_doc_reference_no", deliveryMapKorex.get("MBL_NO"));
//                tmpMap.put("if_vender", deliveryMapKorex.get("AGENT_CODE"));
//                tmpMap.put("if_send_recv_type", "S");
//                tmpMap.put("if_send_recv_method", "DB to DB");
//                tmpMap.put("if_file_path", "");
//                tmpMap.put("if_file_name", "");
//                tmpMap.put("if_status", ifStatus);   // SEND or ERROR
//                tmpMap.put("if_status_info", errorMsg);   // 에러면 사유, 정상이면 ""
//                tmpMap.put("if_sender_id", deliveryMapKorex.get("AGENT_CODE"));
//                tmpMap.put("if_sender_name", "");
//                tmpMap.put("if_receiver_id", "");
//                tmpMap.put("if_receiver_name", "");
//                tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
//                tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));
//                tmpMap.put("if_remark", "KOREX");
//                tmpMap.put("register_user_id", deliveryMapKorex.get("registerUserId"));
//                tmpMap.put("register_user_name", deliveryMapKorex.get("registerUserName"));
//                tmpMap.put("register_date_time", System.currentTimeMillis());
//                tmpMap.put("es_job_no", logId);
//
//                indexApi = new IndexApi(client);
//                if (!indexApi.existIndex(index)) indexApi.createIndex(index);
//
//                documentApi = new DocumentApi(client);
//                documentApi.createDocument(index, logId, tmpMap);
//
//            } catch (Exception e) {
//                log.error("ES 로그 기록 실패 PO_NO={} / ERROR={}", hblno, e.getMessage(), e);
//                // ES 로그 실패는 비즈니스 처리에 영향 없으므로 계속 진행
//            }
//        }
//        log.info("###### 대한통운 반품요청 종료 - 전체:{} / 성공:{} / 실패:{} ##########", totalCnt, successedCnt, failedCnt);
//        resultMap.put("sendKorexYn", failedCnt == 0 ? "Y" : "N");
//        resultMap.put("totalKorexCnt", totalCnt);
//        resultMap.put("successedKorexCnt", successedCnt);
//        resultMap.put("successedKorexOdJobNos", successedKorexOdJobNos);
//        resultMap.put("successedKorexOdJobNosMulti", successedKorexOdJobNosMulti);
//        resultMap.put("failKorexCnt", failedCnt);
//        resultMap.put("errorListKorex", errorList);
//
//        return resultMap;
//    }

    /**
     * POD 이력 배치 insert/update 처리(반품처리용)
     */
    public void batchProcessOrderPod(SqlSession sqlSession, Map<String, Object> tmpMap,
                                     List<Map<String, Object>> odJobNoList, String podStatus, String gmtTime) throws Exception {

        OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
        Map<String, Object> dataMap = new HashMap<String, Object>();
        List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
        Map<String, Object> tmpInsertPodMap = null;
        int updateCnt = 1;
        int seq = 1;

        for (Map<String, Object> odJobNoMap : odJobNoList) {
            dataMap.put("odJobNo", odJobNoMap.get("odJobNo"));
            dataMap.put("podStatus", podStatus);
            dataMap.put("gmtTime", gmtTime);
            dataMap.put("noCheckLastPodStatus", "Y");
            orderBatchMapper.updateOrderLastPod(dataMap);
            tmpInsertPodMap = new HashMap<String, Object>();
            if (odJobNoMap.get("podJobNo") != null) {
                tmpInsertPodMap.put("podJobNo", odJobNoMap.get("podJobNo"));
                updatePodList.add(tmpInsertPodMap);
            } else {
                tmpInsertPodMap.put("odJobNo", odJobNoMap.get("odJobNo"));
                tmpInsertPodMap.put("blNo", odJobNoMap.get("hblNo"));
                tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
                insertPodList.add(tmpInsertPodMap);
            }

            if ((updateCnt++ % 1000) == 0) {
                sqlSession.flushStatements();
                sqlSession.clearCache();
            }
        }

        updateCnt = 1;
        for (Map<String, Object> insertPodMap : insertPodList) {
            tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
            tmpMap.put("blNo", insertPodMap.get("blNo"));
            tmpMap.put("podJobNo", insertPodMap.get("podJobNo"));
            orderBatchMapper.insertOrderPod(tmpMap);
            if ((updateCnt++ % 1000) == 0) {
                sqlSession.flushStatements();
                sqlSession.clearCache();
            }
        }

        updateCnt = 1;
        for (Map<String, Object> updatePodMap : updatePodList) {
            tmpMap.put("podJobNo", updatePodMap.get("podJobNo"));
            orderBatchMapper.updateOrderPod(tmpMap);
            if ((updateCnt++ % 1000) == 0) {
                sqlSession.flushStatements();
                sqlSession.clearCache();
            }
        }
    }

    /**
     * 전자상거래 Delivery 배송사 일괄변경
     *
     * @param entCode
     * @param deliveryMap
     * @return
     * @throws Exception
     */
    public DeliverySaveResult changeDelivery(UpdateDelivery send) throws Exception {
        DeliverySaveResult result = new DeliverySaveResult();
        int totalCnt = send.getDataList().size();
        int successCnt = 0;
        List<Map<String, Object>> errorList = new ArrayList<>();
        for (int i = 0; i < send.getDataList().size(); i++) {
            Map<String, Object> row = send.getDataList().get(i);
            try {
                Map<String, Object> chkMap = new HashMap<>();
                chkMap.put("odJobNo", row.get("odJobNo"));
                chkMap.put("seqNoId", row.get("seqNoId"));
                chkMap.put("deliveryCode", send.getDeliveryCode());
                int updated = deliveryMapper.updateChangeDelivery(chkMap);
                if (updated > 0) {
                    successCnt++;
                } else {
                    Map<String, Object> err = new HashMap<>();
                    err.put("odJobNo", row.get("odJobNo"));
                    err.put("seqNoId", row.get("seqNoId"));
                    err.put("message", "업데이트 대상이 없습니다.");
                    errorList.add(err);
                }
            } catch (Exception e) {
                Map<String, Object> err = new HashMap<>();
                err.put("odJobNo", row.get("odJobNo"));
                err.put("seqNoId", row.get("seqNoId"));
                err.put("message", e.getMessage());
                errorList.add(err);
            }
        }
        log.debug("changeDelivery successCnt -> "+successCnt);
        result.setTotalCnt(totalCnt);
        result.setErrorList(errorList);
        result.setResultCode(errorList.isEmpty() ? "S" : "F");
        return result;
    }


    /**
     * 전자상거래 Delivery 반품번호 변경
     *
     * @param entCode
     * @param deliveryMap
     * @return
     * @throws Exception
     */
    public DeliverySaveResult changeReturnNo(Map<String, Object> saveMap) throws Exception {
        DeliverySaveResult result = new DeliverySaveResult();
        result.setTotalCnt(1);
        try {
            Map<String, Object> returnOrderInfo = interfaceMapperVE.selectReturnOrderInfoPop(saveMap);
            //멀티추가 고려한 맵
            Map<String, Object> chkReturnMultiMap = new HashMap<>();
            chkReturnMultiMap.put("odJobNo",returnOrderInfo.get("OD_JOB_NO"));
            chkReturnMultiMap.put("deliveryNo",returnOrderInfo.get("HBL_NO"));
            chkReturnMultiMap.put("deliveryCode",returnOrderInfo.get("DELIVERY_CODE"));
            chkReturnMultiMap.put("customerCode",returnOrderInfo.get("DELIVERY_CODE"));//배송사와같음
            chkReturnMultiMap.put("paymentGubn",returnOrderInfo.get("DELIVERY_PAYMENT_TYPE"));
            chkReturnMultiMap.put("deliveryFee",returnOrderInfo.get("DELIVERY_FEE"));
            chkReturnMultiMap.put("pieces",returnOrderInfo.get("PIECES"));
            chkReturnMultiMap.put("dimWidth",returnOrderInfo.get("DIM_WIDTH"));
            chkReturnMultiMap.put("dimLength",returnOrderInfo.get("DIM_LENGTH"));
            chkReturnMultiMap.put("dimHeight",returnOrderInfo.get("DIM_HEIGHT"));
            chkReturnMultiMap.put("vwt",returnOrderInfo.get("VWT"));
            chkReturnMultiMap.put("recipientName",returnOrderInfo.get("CONSIGNEE_NAME"));
            chkReturnMultiMap.put("remarks","");
            chkReturnMultiMap.put("requestId",saveMap.get("returnRequestId"));
            chkReturnMultiMap.put("returnDeliveryCode",saveMap.get("returnDeliveryCode"));
            chkReturnMultiMap.put("itemName",returnOrderInfo.get("COMMODITY_ITEM"));
            chkReturnMultiMap.put("registerUserId","API");
            chkReturnMultiMap.put("multiDeliveryNo",saveMap.get("multiDeliveryNo"));
            chkReturnMultiMap.put("seqNoId",saveMap.get("seqNoId"));

            //멀티로 추가는 하되 반품요청은 이전 기록에서 동일한 RETURN_REQUEST_ID가 없어야 진행이 되도록함
            boolean chkAdFlag = true;
            List<Map<String, Object>> chkReturnMapInfo = interfaceMapperVE.selectReturnChk(chkReturnMultiMap);
            if(chkReturnMapInfo.size()>0){
                //같은게 1건이라도 있으면 로직이 진행되지 않게하기위해 for문을 돌림
                for(int i=0;i<chkReturnMapInfo.size();i++){
                    //맵자체에 대한 널방지
                    if(chkReturnMapInfo.get(i) == null) continue;
                    if(Util.null2str(chkReturnMapInfo.get(i).get("RETURN_REQUEST_ID")).equals(Util.null2str(saveMap.get("returnRequestId")))){
                        chkAdFlag = false;
                    }
                }
            }
            
            if("".equals(Util.null2str(saveMap.get("multiDeliveryNo")))) { //멀티번호를 선택하고 반품번호를 넣은경우 업데이트하고 반품처리하기
            	//0.무조건 추가까지는 가능하게함
                interfaceMapperVE.insertReturnMulti(chkReturnMultiMap);
            }else {
            	interfaceMapperVE.updateReturnMulti(chkReturnMultiMap);
            }
            //반품접수를위해 flush
            sqlSessionTemplate.flushStatements();
            sqlSessionTemplate.clearCache();

            //0-1.반품관리 조회용 RESEND_INFO 등록
            //반품관리 List 쿼리(selectReturnMngList)가 TB_ECM_OD_RESEND_INFO 와 INNER JOIN 되어있어
            //방금 등록한 멀티건 SEQ_NO_ID 로 RESEND_INFO 행을 만들어줘야 반품관리 화면에 조회됨
            String returnSeqNoId = interfaceMapperVE.selectReturnMultiSeqNoId(chkReturnMultiMap);
            if (returnSeqNoId != null && !"".equals(returnSeqNoId)) {
                Map<String, Object> resendInfoMap = new HashMap<>();
                resendInfoMap.put("odJobNo", saveMap.get("odJobNo"));
                resendInfoMap.put("seqNoId", returnSeqNoId);
                resendInfoMap.put("registerUserId", "API");
                interfaceMapperVE.insertReturnResendInfo(resendInfoMap);
                sqlSessionTemplate.flushStatements();
                sqlSessionTemplate.clearCache();
            }
            //멀티건이 없는경우는 무조건 반품전송하면되며 일치하는건이 없을때도 반품전송 하면됨(기본값 true)
            if(chkAdFlag==true){

                //1-0오더기준값 최신화
                interfaceMapperVE.updateReturnOrderJob(saveMap);//반품저장
                interfaceMapperVE.updateReturnOrderAdJob(saveMap);//requestId,반품배송사코드저장

                //1-1.변경된건을 고려해 반품관련데이터 초기화해야함(pod4,pod4_m을 초기화)
                interfaceMapperVE.deleteChkPod(chkReturnMultiMap);

                    //#######반품요청 로직###############
                    //레디스세팅
                    UpdateDelivery send = new UpdateDelivery();
                    Map<String, Object> multiMap = new HashMap<>();;
                    //멀티건은 orderInvoiceNumber을 받아 hbl과 매핑(현재는 무조건 멀티)
                    multiMap.put("odJobNo",returnOrderInfo.get("OD_JOB_NO"));
                    if(deliveryMapper.countMulti(multiMap)>0){
                        log.debug("mulchk->"+ deliveryMapper.countMulti(multiMap));
                        send.setMultiChk("multi");
                        send.setSrchHblNo(Util.null2str(returnOrderInfo.get("HBL_NO")));
                    }
                    send.setRegisterUserId("API");
                    send.setRegisterUserName("API");
                    send.setGmtTime(9);
                    send.setSrchReturnYn("Y");//해당플래그로 반품이 아닌 배송요청 쿼리조건을 스킵
                    send.setCryptographyYn("N");
                    send.setBlTypeMh("H");
                    Map<String, Object> item = new HashMap<>();
                    item.put("odJobNo", saveMap.get("odJobNo"));
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    dataList.add(item);
                    send.setDataList(dataList);
                    //레디스에 넣을 셀렉 정보(멀티여부는 위에 세팅한 setMultiChk로 동적쿼리셀렉)
                    List<Map<String, Object>> resultDelivery = deliveryMapper.selectSendDeliveryListToMultiVE(send);
                    log.debug("resultDelivery->"+ resultDelivery);
                    Map<String, Object>deliveryMapChk = new HashMap<>();
                    String key = "E-COMMERCE_DELIVERY_RETURN_HUBNK";

                    // es로그세팅
                    String ifStatusRedis = "SEND";
                    String errorMsg = "";
                    int totalCnt = resultDelivery.size();
                    int successedCnt = 0;
                    int failedCnt = 0;

                    //1건이상 나와야 진행
                    if(resultDelivery.size()>0) {
                        deliveryMapChk = resultDelivery.get(0);
                        try {
                            //반품정보테이블에 저장
                            Map<String, Object> insertOrderMap = new HashMap<>();
                            insertOrderMap.put("od_job_no", saveMap.get("odJobNo"));
                            insertOrderMap.put("requestId", Util.null2str(saveMap.get("returnRequestId")));
                            insertOrderMap.put("senderZipcode", deliveryMapChk.get("shipperZipcodeConvert"));
                            insertOrderMap.put("senderName", deliveryMapChk.get("shipperNameConvert"));
                            insertOrderMap.put("senderTel",deliveryMapChk.get("shipperTelConvert"));
                            insertOrderMap.put("senderHp",  deliveryMapChk.get("shipperMobileConvert"));
                            insertOrderMap.put("senderAddress", deliveryMapChk.get("shipperAddressLocConvert"));
                            insertOrderMap.put("senderAddressDetail",  deliveryMapChk.get("shipperAddressLocConvert"));
                            insertOrderMap.put("receiverZipcode",  deliveryMapChk.get("consigneeZipcodeLocConvert"));
                            insertOrderMap.put("receiverName", deliveryMapChk.get("CONSIGNEE_NAME"));
                            insertOrderMap.put("receiverTel",deliveryMapChk.get("consigneeTelConvert"));
                            insertOrderMap.put("receiverHp", deliveryMapChk.get("consigneeHpConvert"));
                            insertOrderMap.put("receiverAddress", deliveryMapChk.get("consigneeAddressLocConvert"));
                            insertOrderMap.put("receiverAddressDetail", deliveryMapChk.get("consigneeAddressLocConvert"));
                            //삭제하고 인서트하는로직(변경건의 중복키방지)
                            interfaceMapperVE.deleteReturninfo(insertOrderMap);
                            interfaceMapperVE.insertReturninfo(insertOrderMap);


                            // 멀티 정보 레디스세팅
                            JSONObject json = new JSONObject();
                            json.put("DELIVERY_PAYMENT_TYPE_MULTI", deliveryMapChk.get("DELIVERY_PAYMENT_TYPE_MULTI"));
                            json.put("commodityItemLocConvertMulti", deliveryMapChk.get("commodityItemLocConvertMulti"));
                            json.put("DIM_WIDTH_MULTI", deliveryMapChk.get("DIM_WIDTH_MULTI"));
                            json.put("DIM_LENGTH_MULTI", deliveryMapChk.get("DIM_LENGTH_MULTI"));
                            json.put("DIM_HEIGHT_MULTI", deliveryMapChk.get("DIM_HEIGHT_MULTI"));
                            json.put("DIM_WIDTH", deliveryMapChk.get("DIM_WIDTH"));
                            json.put("DIM_LENGTH", deliveryMapChk.get("DIM_LENGTH"));
                            json.put("DIM_HEIGHT", deliveryMapChk.get("DIM_HEIGHT"));
                            json.put("GWT_MULTI", deliveryMapChk.get("GWT_MULTI"));
                            json.put("VWT", deliveryMapChk.get("VWT"));
                            json.put("VWT_MULTI", deliveryMapChk.get("VWT_MULTI"));
                            json.put("DELIVERY_NO", deliveryMapChk.get("DELIVERY_NO"));
                            json.put("SEQ_NO_ID", deliveryMapChk.get("SEQ_NO_ID"));
                            json.put("MULTI_COUNT", deliveryMapChk.get("MULTI_COUNT"));
                            json.put("DELIVERY_CODE_MULTI", deliveryMapChk.get("DELIVERY_CODE_MULTI"));

                            // POD 관련
                            json.put("ECM360_POD_JOB_NO_MULTI", deliveryMapChk.get("ECM360_POD_JOB_NO_MULTI"));
                            json.put("ECM360_POD_JOB_NO", deliveryMapChk.get("ECM360_POD_JOB_NO"));
                            json.put("ECM410_POD_JOB_NO_MULTI", deliveryMapChk.get("ECM410_POD_JOB_NO_MULTI"));
                            json.put("ECM340_POD_JOB_NO_MULTI", deliveryMapChk.get("ECM340_POD_JOB_NO_MULTI"));
                            json.put("ECM340_POD_JOB_NO", deliveryMapChk.get("ECM340_POD_JOB_NO"));
                            json.put("ECM504_POD_JOB_NO_MULTI", deliveryMapChk.get("ECM504_POD_JOB_NO_MULTI"));
                            json.put("ECM504_POD_JOB_NO", deliveryMapChk.get("ECM504_POD_JOB_NO"));

                            // 오더 기본 정보
                            json.put("HBL_NO", deliveryMapChk.get("HBL_NO"));
                            json.put("OD_JOB_NO", deliveryMapChk.get("OD_JOB_NO"));
                            json.put("HSN", deliveryMapChk.get("HSN"));
                            json.put("AGENT_CODE", deliveryMapChk.get("AGENT_CODE"));
                            json.put("DEST_PORT", deliveryMapChk.get("DEST_PORT"));
                            json.put("LAST_POD_STATUS", deliveryMapChk.get("LAST_POD_STATUS"));
                            json.put("VESSEL_FLIGHT", deliveryMapChk.get("VESSEL_FLIGHT"));
                            json.put("CHK_PO_NO", deliveryMapChk.get("CHK_PO_NO"));
                            json.put("PO_NO", deliveryMapChk.get("PO_NO"));
                            json.put("TR_CODE", deliveryMapChk.get("TR_CODE"));
                            json.put("DELIVERY_CODE", deliveryMapChk.get("DELIVERY_CODE"));
                            json.put("MBL_NO", deliveryMapChk.get("MBL_NO"));
                            json.put("DELIVERY_PAYMENT_TYPE", deliveryMapChk.get("DELIVERY_PAYMENT_TYPE"));
                            json.put("DELIVERY_NOTE", deliveryMapChk.get("DELIVERY_NOTE"));
                            json.put("BOUND", deliveryMapChk.get("BOUND"));
                            json.put("BOUND_TYPE", deliveryMapChk.get("BOUND_TYPE"));

                            // 수하인 정보
                            json.put("CONSIGNEE_NAME_LOC", deliveryMapChk.get("CONSIGNEE_NAME_LOC"));
                            json.put("CONSIGNEE_NAME", deliveryMapChk.get("CONSIGNEE_NAME"));
                            json.put("CONSIGNEE_ZIPCODE", deliveryMapChk.get("CONSIGNEE_ZIPCODE"));
                            json.put("CONSIGNEE_ADDRESS", deliveryMapChk.get("CONSIGNEE_ADDRESS"));
                            json.put("CONSIGNEE_ADDRESS_A", deliveryMapChk.get("CONSIGNEE_ADDRESS_A"));
                            json.put("CONSIGNEE_ADDRESS_B", deliveryMapChk.get("CONSIGNEE_ADDRESS_B"));
                            json.put("CONSIGNEE_TEL", deliveryMapChk.get("CONSIGNEE_TEL"));
                            json.put("CONSIGNEE_HP", deliveryMapChk.get("CONSIGNEE_HP"));
                            json.put("consigneeNameLocConvert", deliveryMapChk.get("consigneeNameLocConvert"));
                            json.put("consigneeZipcodeLocConvert", deliveryMapChk.get("consigneeZipcodeLocConvert"));
                            json.put("consigneeAddressLocConvert", deliveryMapChk.get("consigneeAddressLocConvert"));
                            json.put("consigneeTelConvert", deliveryMapChk.get("consigneeTelConvert"));
                            json.put("consigneeHpConvert", deliveryMapChk.get("consigneeHpConvert"));

                            // 화주 정보
                            json.put("shipperNameConvert", deliveryMapChk.get("shipperNameConvert"));
                            json.put("shipperZipcodeConvert", deliveryMapChk.get("shipperZipcodeConvert"));
                            json.put("shipperAddressLocConvert", deliveryMapChk.get("shipperAddressLocConvert"));
                            json.put("shipperTelConvert", deliveryMapChk.get("shipperTelConvert"));
                            json.put("shipperMobileConvert", deliveryMapChk.get("shipperMobileConvert"));

                            // 화물 치수/무게
                            json.put("PIECES", deliveryMapChk.get("PIECES"));
                            json.put("PIECES_REAL", deliveryMapChk.get("PIECES_REAL"));
                            json.put("CBM", deliveryMapChk.get("CBM"));
                            json.put("GWT", deliveryMapChk.get("GWT"));

                            // 금액
                            json.put("AMOUNT_CUR", deliveryMapChk.get("AMOUNT_CUR"));

                            // 품목 정보
                            json.put("commodityItemLocConvert", deliveryMapChk.get("commodityItemLocConvert"));
                            json.put("commodityItem1", deliveryMapChk.get("commodityItem1"));
                            json.put("commodityItem2", deliveryMapChk.get("commodityItem2"));
                            json.put("commodityItem3", deliveryMapChk.get("commodityItem3"));
                            json.put("commodityItem4", deliveryMapChk.get("commodityItem4"));
                            json.put("commodityItem5", deliveryMapChk.get("commodityItem5"));

                            // 등록자 정보
                            json.put("registerUserId", deliveryMapChk.get("registerUserId"));
                            json.put("registerUserName", deliveryMapChk.get("registerUserName"));
                            json.put("gmtTime", deliveryMapChk.get("gmtTime"));

                            Long pushResult = redisTemplate.opsForList().rightPush(key, json.toString());
                            if (pushResult != null && pushResult > 0) {
                                log.info("Redis 저장 완료 OD_JOB_NO={} / KEY={}", deliveryMapChk.get("OD_JOB_NO"), key);

                                //반품요청시간 추가
                                Map<String, Object> dataMap = new HashMap<String, Object>();
                                dataMap.put("deliveryNo", deliveryMapChk.get("DELIVERY_NO"));
                                dataMap.put("statusType", "RETURN_DLV_REQ_DATE");
                                dataMap.put("podDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                                orderMapper.updateOdMultiDate(dataMap);

                                // 대한통운 반품 POD반영
                                // 오더건 POD 업데이트 (TB_ECM_POD_04)
                                boolean multiChk = "multi".equals(send.getMultiChk());
                                String seqNoIdChk = Util.null2str(deliveryMapChk.get("SEQ_NO_ID"));
                                //멀티가 아니거나 멀티시퀀스 1번일경우에만 같이 pod반영
                                if (!multiChk || "1".equals(seqNoIdChk)) {

                                    //오더에도 반품요청시간 추가
                                    Map<String, Object> updateOdMap = new HashMap<>();
                                    updateOdMap.put("odJobNo", saveMap.get("odJobNo"));
                                    updateOdMap.put("statusType", "RETURN_DLV_REQ_DATE");
                                    updateOdMap.put("podDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                                    orderMapper.updateOdDate(updateOdMap);

                                    List<Map<String, Object>> podList = new ArrayList<>();
                                    Map<String, Object> podMap = new HashMap<>();
                                    podMap.put("odJobNo", saveMap.get("odJobNo"));
                                    podMap.put("hblNo", deliveryMapChk.get("HBL_NO"));
                                    podMap.put("podJobNo", deliveryMapChk.get("ECM504_POD_JOB_NO"));
                                    podList.add(podMap);
                                    SqlSession sqlSessionOd = null;
                                    try {
                                        sqlSessionOd = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
                                        Map<String, Object> podBatchMap = new HashMap<>();
                                        podBatchMap.put("entCode", "HUBNK");
                                        podBatchMap.put("gmtTime", 9);
                                        podBatchMap.put("registerUserId", "API");
                                        podBatchMap.put("updateUserId", "API");
                                        podBatchMap.put("entBusinessCode", "");
                                        podBatchMap.put("solution", "ECM");
                                        podBatchMap.put("stockType", "POD");
                                        podBatchMap.put("stockSign", "POD");
                                        podBatchMap.put("stockSeqLength", "8");
                                        podBatchMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
                                        podBatchMap.put("stockRemark", "ECM POD JOB NO");
                                        podBatchMap.put("podStatus", "ECM504");
                                        podBatchMap.put("deliveryCode", "KOREX");
                                        podBatchMap.put("podTableName", "TB_ECM_POD_04");
                                        Util.accessControlDatabase(podBatchMap, "podTableName|gmtTime", false, null, null, null, null);
                                        interfaceServiceVE.batchProcessOrderPod(sqlSessionOd, podBatchMap, podList, "ECM504", String.valueOf(podBatchMap.get("gmtTime")));
                                        sqlSessionOd.flushStatements();
                                        sqlSessionOd.clearCache();
                                       // sqlSessionOd.commit();
                                    } catch (Exception e) {
                                        throw e;
                                    } finally {
                                        if (sqlSessionOd != null) sqlSessionOd.close();
                                    }
                                }
                                //  멀티건 POD 업데이트 (TB_ECM_POD_04_M)
                                if (multiChk) {
                                    List<Map<String, Object>> podListMulti = new ArrayList<>();
                                    Map<String, Object> podMapMulti = new HashMap<>();
                                    podMapMulti.put("odJobNo", saveMap.get("odJobNo"));
                                    podMapMulti.put("podJobNo", deliveryMapChk.get("ECM504_POD_JOB_NO_MULTI"));
                                    podMapMulti.put("hblNo", deliveryMapChk.get("DELIVERY_NO"));
                                    podListMulti.add(podMapMulti);
                                    SqlSession sqlSessionMulti = null;
                                    try {
                                        sqlSessionMulti = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
                                        Map<String, Object> podBatchMapMulti = new HashMap<>();
                                        podBatchMapMulti.put("entCode", "HUBNK");
                                        podBatchMapMulti.put("gmtTime", 9);
                                        podBatchMapMulti.put("registerUserId", "API");
                                        podBatchMapMulti.put("updateUserId", "API");
                                        podBatchMapMulti.put("entBusinessCode", "");
                                        podBatchMapMulti.put("solution", "ECM");
                                        podBatchMapMulti.put("stockType", "POD");
                                        podBatchMapMulti.put("stockSign", "POD");
                                        podBatchMapMulti.put("stockSeqLength", "8");
                                        podBatchMapMulti.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
                                        podBatchMapMulti.put("stockRemark", "ECM POD JOB NO");
                                        podBatchMapMulti.put("podStatus", "ECM504");
                                        podBatchMapMulti.put("deliveryCode", "Korex");
                                        podBatchMapMulti.put("podTableName", "TB_ECM_POD_04_M");
                                        Util.accessControlDatabase(podBatchMapMulti, "podTableName|gmtTime", false, null, null, null, null);
                                        interfaceServiceVE.batchProcessOrderPod(sqlSessionMulti, podBatchMapMulti, podListMulti, "ECM504", String.valueOf(podBatchMapMulti.get("gmtTime")));
                                        sqlSessionMulti.flushStatements();
                                        sqlSessionMulti.clearCache();
                                       // sqlSessionMulti.commit();
                                    } catch (Exception e) {
                                        throw e;
                                    } finally {
                                        if (sqlSessionMulti != null) sqlSessionMulti.close();
                                    }

                                    //멀티건 배송상태(RETURN_DELIVERY_STATUS)를 ECM504(반품요청)로 반영
                                    Map<String, Object> multiStatusMap = new HashMap<>();
                                    multiStatusMap.put("odJobNo", saveMap.get("odJobNo"));
                                    multiStatusMap.put("deliveryNo", deliveryMapChk.get("DELIVERY_NO"));
                                    multiStatusMap.put("podStatus", "ECM504");
                                    orderMapper.updateOdMultiStatus(multiStatusMap);
                                }
                                log.debug("Redis save job->" + Util.null2str(deliveryMapChk.get("OD_JOB_NO")));
                                successedCnt++;
                            } else {
                                ifStatusRedis = "ERROR";
                                errorMsg = "Redis rightPush 반환값 이상 (result=" + pushResult + ")";
                                log.error("Redis 저장 실패 OD_JOB_NO={} / KEY={} / {}", deliveryMapChk.get("OD_JOB_NO"), key, errorMsg);

                                Map<String, Object> errorMap = new HashMap<>();
                                errorMap.put("odJobNo", saveMap.get("odJobNo"));
                                errorMap.put("hblNo", deliveryMapChk.get("HBL_NO"));
                                errorMap.put("errorMsg", errorMsg);
                                failedCnt++;
                            }

                        } catch (Exception e) {
                            ifStatusRedis = "ERROR";
                            errorMsg = "Redis 저장 예외: " + e.getMessage();
                            log.error("Redis 저장 예외 OD_JOB_NO={} / ERROR={}", saveMap.get("odJobNo"), e.getMessage(), e);

                            Map<String, Object> errorMap = new HashMap<>();
                            errorMap.put("odJobNo", saveMap.get("odJobNo"));
                            errorMap.put("hblNo", deliveryMapChk.get("HBL_NO"));
                            errorMap.put("errorMsg", errorMsg);
                            failedCnt++;
                        }
                        // ES 로그 기록(레디스기록용)
                        try {
                            String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
                            String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
                            Map<String, Object> tmpMap = new HashMap<>();
                            tmpMap.put("ent_code", "HUBNK");
                            tmpMap.put("customer_code", "");
                            tmpMap.put("solution", "ECM");
                            tmpMap.put("bound", deliveryMapChk.get("BOUND"));
                            tmpMap.put("bound_type", deliveryMapChk.get("BOUND_TYPE"));
                            tmpMap.put("hbl_no", deliveryMapChk.get("HBL_NO"));
                            tmpMap.put("if_service_code", "KOREX");
                            tmpMap.put("if_doc_code", "SEND_RETURN");
                            tmpMap.put("if_doc_name", "배송사요청");
                            tmpMap.put("if_doc_reference_no", deliveryMapChk.get("MBL_NO"));
                            tmpMap.put("if_vender", deliveryMapChk.get("AGENT_CODE"));
                            tmpMap.put("if_send_recv_type", "S");
                            tmpMap.put("if_send_recv_method", "redis");
                            tmpMap.put("if_file_path", "");
                            tmpMap.put("if_file_name", "");
                            tmpMap.put("if_status", ifStatusRedis);
                            tmpMap.put("if_status_info", errorMsg);
                            tmpMap.put("if_sender_id", deliveryMapChk.get("AGENT_CODE"));
                            tmpMap.put("if_sender_name", "");
                            tmpMap.put("if_receiver_id", "");
                            tmpMap.put("if_receiver_name", "");
                            tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
                            tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));
                            tmpMap.put("if_remark", "KOREX");
                            tmpMap.put("register_user_id", deliveryMapChk.get("registerUserId"));
                            tmpMap.put("register_user_name", deliveryMapChk.get("registerUserName"));
                            tmpMap.put("register_date_time", System.currentTimeMillis());
                            tmpMap.put("es_job_no", logId);

                            DocumentApi documentApi = null;
                            IndexApi indexApi = new IndexApi(client);
                            if (!indexApi.existIndex(index)) indexApi.createIndex(index);

                            documentApi = new DocumentApi(client);
                            documentApi.createDocument(index, logId, tmpMap);

                        } catch (Exception e) {
                            log.error("ES 로그 기록 실패 OD_JOB_NO={} / ERROR={}", saveMap.get("odJobNo"), e.getMessage(), e);
                        }
                        log.info("###### 대한통운 반품요청 종료 - 전체:{} / 성공:{} / 실패:{} ##########", totalCnt, successedCnt, failedCnt);
                    }
                //반영(허브넷용)
//                    sqlSession.flushStatements();
//                    sqlSession.clearCache();
            }
            result.setResultCode("S");
        } catch (Exception e) {
            log.error("반품번호 변경 실패 odJobNo={} / returnRequestId={}",
                    saveMap.get("odJobNo"), saveMap.get("returnRequestId"), e);
            throw e;
        }
        return result;
    }

}
