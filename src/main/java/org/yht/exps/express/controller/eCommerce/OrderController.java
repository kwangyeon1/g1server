//
package org.yht.exps.express.controller.eCommerce;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.mapper.api.InterfaceMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.eCommerce.PrintMapper;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList;
import org.yht.exps.express.model.eCommerce.order.CustomsVerification;
import org.yht.exps.express.model.eCommerce.order.CustomsVerificationResultList;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadOdChg;
import org.yht.exps.express.model.eCommerce.order.HblNoList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderCSumList;
import org.yht.exps.express.model.eCommerce.order.OrderCombinedTaxList;
import org.yht.exps.express.model.eCommerce.order.OrderCommonList;
import org.yht.exps.express.model.eCommerce.order.OrderCostList;
import org.yht.exps.express.model.eCommerce.order.OrderCostUpdate;
import org.yht.exps.express.model.eCommerce.order.OrderCs;
import org.yht.exps.express.model.eCommerce.order.OrderCsDelete;
import org.yht.exps.express.model.eCommerce.order.OrderCusList;
import org.yht.exps.express.model.eCommerce.order.OrderDashboard;
import org.yht.exps.express.model.eCommerce.order.OrderDayHouseList;
import org.yht.exps.express.model.eCommerce.order.OrderDayList;
import org.yht.exps.express.model.eCommerce.order.OrderDetList;
import org.yht.exps.express.model.eCommerce.order.OrderExclusion;
import org.yht.exps.express.model.eCommerce.order.OrderHold;
import org.yht.exps.express.model.eCommerce.order.OrderHouseDupList;
import org.yht.exps.express.model.eCommerce.order.OrderListDelete;
import org.yht.exps.express.model.eCommerce.order.OrderMasterDupList;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.OrderSumHouseList;
import org.yht.exps.express.model.eCommerce.order.OrderSumMasterList;
import org.yht.exps.express.model.eCommerce.order.TrademarkInfringement;
import org.yht.exps.express.model.eCommerce.order.ZipCodeChange;
import org.yht.exps.express.model.eCommerce.order.search.SearchCustomsVerification;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrder;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderBatchUpdate;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderCombinedTaxList;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderCostList;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderPodUpdate;
import org.yht.exps.express.model.eCommerce.order.search.SearchPrintOut;
import org.yht.exps.express.model.eCommerce.print.SearchPrintInfo;
import org.yht.exps.express.model.systemMng.DeliveryInterface;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.api.InterfaceService;
import org.yht.exps.express.service.eCommerce.OrderService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.DateTime;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/order")
public class OrderController {

    @Autowired
    OrderMapper odMapper;
    @Resource(name = "orderService")
    OrderService orderService;
    @Resource(name = "commonService")
    CommonService commonService;
    @Resource(name = "fileService")
    FileService fileService;
    @Resource(name = "interfaceService")
    InterfaceService interfaceService;

    @Value("${spring.profiles.active}")
    private String profile;
    @Autowired
    PrintMapper printMapper;
    @Autowired
    OrderMapper orderMapper;

    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @ApiOperation(value = "전자상거래 Order Detail 조회", notes = "Order 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderDet/search/list", method = RequestMethod.GET)
    public ResponseEntity<OrderDetList> selectOrderList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        Util.getSessionUserInfo(req, jwtTokenProvider, search, null, "userName", "loginId", null, false, true, false, false);
        OrderDetList orderDetList = new OrderDetList();
        try {
            orderDetList = orderService.selectOrderListDet(search);
        } catch (Exception e) {
            if ((entCode.equals("TYGCK") || entCode.equals("TPCNK")) && e.getMessage().contains("SQLException")) {
                // 10~15초 사이의 랜덤한 시간 뒤 재조회
                int sec = (int) new Random().nextInt(6) + 10;
                Thread.sleep(sec * 1000);
                orderDetList = orderService.selectOrderListDet(search);
            } else {
                throw e;
            }
        }
        return new ResponseEntity<OrderDetList>(orderDetList, HttpStatus.OK);
    }
	@ApiOperation(value = "전자상거래 Order Detail 조회", notes="Order 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/orderDet/search/list/post", method = RequestMethod.POST)
	public ResponseEntity<OrderDetList> selectOrderListPost(@PathVariable("entCode") String entCode, @RequestBody SearchOrderList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(req, jwtTokenProvider, search, null, "userName", "loginId", null, false, true, false, false);
		OrderDetList orderDetList = new OrderDetList();
		try {
			orderDetList  = orderService.selectOrderListDet(search);
		}catch(Exception e) {
			if((entCode.equals("TYGCK") || entCode.equals("TPCNK")) && e.getMessage().contains("SQLException")) {
				// 10~15초 사이의 랜덤한 시간 뒤 재조회
				int sec = (int)new Random().nextInt(6)+10;
				Thread.sleep(sec * 1000);
				orderDetList  = orderService.selectOrderListDet(search);
			}else {
				throw e;
			}
		}
		return new ResponseEntity<OrderDetList>(orderDetList, HttpStatus.OK);
	}
	
    @ApiOperation(value = "전자상거래 Order Detail 엑셀파일생성", notes = "전자상거래 Order Detail 엑셀파일을 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderDet/search/list/excel", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> selectOrderListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest request) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);

        String[] columnsArr = {"num", "lastPodStatus", "custCodeLv1", "custCodeLv2", "poDate", "obDate", "arDate", "poNo", "hblNo", "mblNo", "cdType", "cdUseType", "ecmType", "inspectionYn", "seInspectionYn", "dropYn", "xrayYn", "geInspectionYn", "inspectionType", "holdYn", "pieces", "piecesReal", "licensePieces", "gwt", "licenseGwt", "cbm", "currencyCode", "exchangeRate", "amountCur", "amountLoc", "allowedItemCode", "consigneeName", "consigneeNameLoc", "consigneeAddressLoc", "consigneeZipcode", "consigneeBusinessId", "customsId", "customsIdType", "consigneeTel", "consigneeHp", "url", "clearanceYn", "inspectionResultYn", "dropCode", "dropReason", "abnormalDate", "abnormalCode", "mfCorrectionDate", "modType", "vesselFlight", "origCode", "origCountry", "destCode", "destCountry", "shipperName", "shipperAddress", "shipperTel", "shipperBusinessId", "carryinDate", "carryoutDate", "clearanceDate", "deliveryChangeYn", "cbName", "clearancePaymentType", "clearanceReceiptDate", "clearanceViolationCode", "clearanceViolationReason", "splitWaistYn", "deliveryCode", "deliveryPaymentType", "deliveryStatus", "deliveryRecvDate", "deliveryNameChanged", "deliveryDate", "penaltyYn", "ftaYn", "ftaRemarks"};
        String[] columnsWidthArr = {"63", "100", "100", "100", "120", "120", "120", "100", "170", "170", "100", "100", "100", "55", "55", "55", "65", "55", "55", "55", "55", "65", "80", "55", "80", "65", "55", "55", "90", "55", "80", "100", "100", "100", "115", "125", "125", "100", "110", "110", "115", "80", "100", "120", "100", "125", "100", "125", "100", "100", "65", "80", "65", "80", "100", "100", "115", "125", "120", "120", "120", "80", "100", "100", "120", "100", "100", "120", "100", "100", "100", "120", "100", "120", "65", "55", "125"};
        Integer[] numFormatsArr = {0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, 2, 3, -1, 4, 2, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        String[] excelTypesArr = {"center", "left", "left", "left", "center", "center", "center", "left", "left", "left", "left", "left", "left", "center", "center", "center", "center", "center", "center", "center", "number", "number", "number", "double", "double", "double", "center", "double", "double", "double", "center", "left", "left", "left", "center", "center", "left", "left", "left", "left", "left", "center", "left", "center", "left", "center", "left", "center", "center", "left", "center", "center", "center", "center", "left", "left", "left", "center", "center", "center", "center", "center", "left", "left", "center", "left", "left", "center", "left", "left", "left", "center", "left", "center", "center", "center", "left"};
        String[] titlesArr = {"번호", "진행상태", "상위업체", "하위업체", "오더일자", "출항일자", "입항일자", "오더번호", "H.B/L번호", "M.B/L번호", "목록구분", "용도구분", "거래구분", "검사", "선별", "취하", "X-Ray", "일검", "검역", "홀드", "수량", "실수량", "면장수량", "중량", "면장중량", "CBM", "환종", "환율", "금액(외화)", "금액", "허용품목", "수하인명", "수하인명(L)", "수하인주소(L)", "수하인우편번호", "수하인사업자번호", "개인통관고유부호", "통관부호구분", "수하인전화번호", "수하인휴대전화", "전자상거래주소", "통관여부", "검사결과", "취하사유코드", "취하사유", "하기이상신고일자", "하기이상코드", "적하목록정정일자", "정정구분", "편명(선명)", "출발지", "출발국가", "도착지", "도착국가", "송하인명", "송하인주소", "송하인전화번호", "송하인사업자번호", "반입일자", "반출일자", "수입신고수리일", "배송변경", "관세사명", "통관결제", "통관접수일", "미통관코드", "미통관사유", "분할/폐기여부", "배송사", "배송비결재", "배송상태", "배송접수일", "배송사변경", "배송완료일", "과태료", "FTA", "자유무역협정비고"};

//		String[] columnsArr = {"num","fileShare","consolYn","lastPodStatus","blStatus","custCodeLv1","custCodeLv2","poDate","obDate","arDate","poNo","hblNo","mblNo","cdType","cdUseType","ecmType","inspectionYn","seInspectionYn","dropYn","xrayYn","geInspectionYn","inspectionType","holdYn","pieces","piecesReal"/* 25 */,"num","fileShare","consolYn","lastPodStatus","blStatus","custCodeLv1","custCodeLv2","poDate","obDate","arDate","poNo","hblNo","mblNo","cdType","cdUseType","ecmType","inspectionYn","seInspectionYn","dropYn","xrayYn","geInspectionYn","inspectionType","holdYn","pieces","piecesReal"/* 50 */,"num","fileShare","consolYn","lastPodStatus","blStatus","custCodeLv1","custCodeLv2","poDate","obDate","arDate","poNo","hblNo","mblNo","cdType","cdUseType","ecmType","inspectionYn","seInspectionYn","dropYn","xrayYn","geInspectionYn","inspectionType","holdYn","pieces","piecesReal"/* 75 */,"xrayYn","geInspectionYn","inspectionType","holdYn","pieces"/*,"piecesReal"*//* 81 */};
//		String[] columnsWidthArr = {"63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63"/* 25 */, "63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63"/* 50 */, "63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63","63"/* 75 */,"63","63","63","63","63"/*,"63"*//* 81 */};
//		Integer[] numFormatsArr = {-1,-1,-1,-1,2,2,3,-1,4,2,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 /* 25 */, -1,-1,-1,-1,2,2,3,-1,4,2,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1/* 50 */, -1,-1,-1,-1,2,2,3,-1,4,2,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1/* 75 */,-1,-1,-1,-1,-1/*,-1*//* 81 */};
//		String[] excelTypesArr = {"center","center","center","left","center","left","left","center","center","center","left","left","left","left","left","left","center","center","center","center","center","center","center","center","center"/* 25 */,"center","center","center","left","center","left","left","center","center","center","left","left","left","left","left","left","center","center","center","center","center","center","center","center","center" /* 50 */,"center","center","center","left","center","left","left","center","center","center","left","left","left","left","left","left","center","center","center","center","center","center","center","center","center" /* 75 */,"center","center","center","center","center"/*,"center"*/ /* 81 */};
//		String[] titlesArr = {"번호","F#","Consol","진행상태","관세청","상위업체","하위업체","오더일자","출항일자","입항일자","오더번호","H.B/L번호","M.B/L번호","목록구분","용도구분","거래구분","검사","선별","취하","X-Ray","일검","검역","홀드","수량","실수량"/* 25 */,"번호","F#","Consol","진행상태","관세청","상위업체","하위업체","오더일자","출항일자","입항일자","오더번호","H.B/L번호","M.B/L번호","목록구분","용도구분","거래구분","검사","선별","취하","X-Ray","일검","검역","홀드","수량","실수량"/* 50 *//* 25 */,"번호","F#","Consol","진행상태","관세청","상위업체","하위업체","오더일자","출항일자","입항일자","오더번호","H.B/L번호","M.B/L번호","목록구분","용도구분","거래구분","검사","선별","취하","X-Ray","일검","검역","홀드","수량","실수량"/* 75 */,"X-Ray","일검","검역","홀드","수량"/*,"실수량"*//* 81 */};

        List<String> columns = new ArrayList<String>();
        List<String> columnsWidth = new ArrayList<String>();
        List<Integer> numFormats = new ArrayList<Integer>();
        List<String> excelTypes = new ArrayList<String>();
        List<String> titles = new ArrayList<String>();
        for (int i = 0; i < columnsArr.length; i++) {
            columns.add(columnsArr[i]);
            columnsWidth.add(columnsWidthArr[i]);
            numFormats.add(numFormatsArr[i]);
            excelTypes.add(excelTypesArr[i]);
            titles.add(titlesArr[i]);
        }
        map.put("columns", columns);
        map.put("columnsWidth", columnsWidth);
        map.put("numFormats", numFormats);
        map.put("excelTypes", excelTypes);
        map.put("titles", titles);

        map.put("data", orderService.selectOrderListDet(search).getOrderDetList());
        if (Util.null2str(map.get("documentCode")).equals("")) {
            return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("OrderDetList", map), HttpStatus.OK);
        } else {
            map.put("excelItemList", commonService.selectExcelItemList(map));
            return new ResponseEntity<DownloadFileInfo>(Common.getCustomizeExcelList(map), HttpStatus.OK);
        }
    }

    @ApiOperation(value = "전자상거래 Order Detail 엑셀파일생성", notes = "전자상거래 Order Detail 엑셀파일을 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderDet/search/list/excelPost", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectOrderListExcelPost(@PathVariable("entCode") String entCode, @RequestBody SearchOrderList search, HttpServletRequest request) throws Exception {

        Util.getSessionUserInfo(request, jwtTokenProvider, search, "registerUserId", null, null);

        Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchCusType", true, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE|od.REGISTER_DATE_TIME", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO|od.RETURN_DELIVERY_NO"
                + "|od.RETURN_DELIVERY_NO|od.SHIPMENT_BOX_ID|ad.RETURN_REQUEST_ID");
        Util.getCryptographyYn(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);

        if (Util.null2str(search.getChangeField()).equals("Y")) {
            Util.camelCaseToUnderscore(search);
        }
        /* N개 검색(멀티타입) */
        List<String> searchNoList = new ArrayList<String>();
        if (!"".equals(Util.null2str(search.getSrchNo()))) {
            searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
        }

        if (searchNoList.size() > 1) {
            search.setSearchType("multi");
            search.setSearchNoList(searchNoList);
        } else {
            search.setSearchType("");
            search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
        }

        if (!"ALL".equals(search.getSrchBoundType())) {
            if ("AO".equals(search.getSrchBoundType()) || "SO".equals(search.getSrchBoundType())) {
                if ("Y".equals(search.getSrchRvsBoundType())) {
                    search.setSrchBoundType2("IMP");
                } else {
                    search.setSrchBoundType2("EXP");
                }
            } else {
                search.setSrchBoundType2("IMP");
                if ("Y".equals(search.getSrchRvsBoundType())) {
                    search.setSrchBoundType2("EXP");
                } else {
                    search.setSrchBoundType2("IMP");
                }
            }
        }


        /** 전체 엑셀 다운로드 */
        if (Util.null2str(search.getSrchDateGbn()).equals("noDateChk") ||
                (Util.null2str(search.getSrchNoType()).equals("od.HBL_NO") && !Util.null2str(search.getSrchNo()).equals(""))
        ) {
            search.setSrchMode("listCount");
            search.setPage(1);
            return new ResponseEntity<DownloadFileInfo>(commonService.makeExcelDownload(map, search, orderMapper, "selectOrderListDet", "org.yht.exps.express.model.eCommerce.order.search.SearchOrderList"), HttpStatus.OK);

            /** 일별로 엑셀 다운로드 */
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            long diffDate = Util.diffDate(search.getSrchEndDate(), search.getSrchStartDate());

            Date strDate = Util.strToDate(search.getSrchStartDate(), "yyyy-MM-dd");
            Date startDate = new Date();
            Date endDate = new Date();

            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();

            start.setTime(strDate);
            end.setTime(strDate);

            List<DownloadFileInfo> downloadList = new ArrayList<DownloadFileInfo>();
            DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
            downloadFileInfo.setSeq(0); // 일별로 엑셀 다운로드시 NUM 항목값이 날짜별 ROW NUMBER로 들어가서 시퀀스로 대체
            for (int i = 0; i < diffDate + 1; i++) {
                if (i > 0) {
                    start.add(Calendar.DATE, 1);
                    end.add(Calendar.DATE, 1);
                }
                startDate = new Date(start.getTimeInMillis());
                endDate = new Date(end.getTimeInMillis());
                search.setSrchStartDate(sdf.format(startDate));
                search.setSrchEndDate(sdf.format(endDate));
                search.setSrchMode("listCount");
                search.setPage(1);
                downloadFileInfo = commonService.makeExcelDownload(map, search, orderMapper, "selectOrderListDet", "org.yht.exps.express.model.eCommerce.order.search.SearchOrderList");
                if (downloadFileInfo.getSeq() > 0) {
                    map.put("seq", downloadFileInfo.getSeq());
                }
            }
            return new ResponseEntity<DownloadFileInfo>(downloadFileInfo, HttpStatus.OK);

        }
//		map.put("data", orderService.selectOrderListDet(search).getOrderDetList());
//		if(Util.null2str(map.get("documentCode")).equals("")) {
//			return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("OrderDetList", map), HttpStatus.OK);
//		}else {
//			map.put("excelItemList", commonService.selectExcelItemList(map));
//			return new ResponseEntity<DownloadFileInfo>(Common.getCustomizeExcelList(map), HttpStatus.OK);
//		}
    }

    @ApiOperation(value = "전자상거래 H.B/L No 리스트 조회", notes = "Order H.B/L No 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/hblNo/search/list", method = RequestMethod.GET)
    public ResponseEntity<HblNoList> selectHblNo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<HblNoList>(orderService.selectHblNo(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 일일보고 조회", notes = "Order 일일보고 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderDay/search/list", method = RequestMethod.GET)
    public ResponseEntity<OrderDayList> selectOrderListDay(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderDayList>(orderService.selectOrderListDay(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 일일보고 엑셀파일생성", notes = "Order 일일보고 엑셀파일을 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderDay/search/list/excel", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> selectOrderListDayExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest request) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);
        map.put("data", orderService.selectOrderListDay(search).getOrderDayList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("OrderDayList", map), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 일일보고 하우스 조회", notes = "Order 일일보고 하우스 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderDayHouse/search/list", method = RequestMethod.GET)
    public ResponseEntity<OrderDayHouseList> selectOrderListDayHouse(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderDayHouseList>(orderService.selectOrderListDayHouse(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 일일보고 하우스 엑셀파일생성", notes = "Order 일일보고 하우스 엑셀파일을 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderDayHouse/search/list/excel", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> selectOrderListDayHouseExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest request) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);
        map.put("data", orderService.selectOrderListDayHouse(search).getOrderDayHouseList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("OrderDayHouseList", map), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order Summary Master 조회", notes = "Order Summary Master 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderSumMaster/search/list", method = RequestMethod.GET)
    public ResponseEntity<OrderSumMasterList> selectOrderListSumMaster(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderSumMasterList>(orderService.selectOrderListSumMaster(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order Summary Master 엑셀파일생성", notes = "Order Summary Master 엑셀파일을 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderSumMaster/search/list/excel", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> selectOrderListSumMasterExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest request) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);
        map.put("data", orderService.selectOrderListSumMaster(search).getOrderSumMasterList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("OrderSummaryMasterList", map), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order Summary House 조회", notes = "Order 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderSumHouse/search/list", method = RequestMethod.GET)
    public ResponseEntity<OrderSumHouseList> selectOrderListSumHouse(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderSumHouseList>(orderService.selectOrderListSumHouse(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order Summary House 엑셀파일생성", notes = "Order Summary House 엑셀파일을 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderSumHouse/search/list/excel", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> selectOrderListSumHouseExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest request) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);
        map.put("data", orderService.selectOrderListSumHouse(search).getOrderSumHouseList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("OrderSummaryHouseList", map), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 통관목록현황집계", notes = "Order 통관목록현황집계 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderCSum/search/list", method = RequestMethod.GET)
    public ResponseEntity<OrderCSumList> selectOrderListCSum(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderCSumList>(orderService.selectOrderListCSum(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 통관목록현황집계 엑셀파일생성", notes = "Order 통관목록현황집계 엑셀파일을 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderCSum/search/list/excel", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> selectOrderListCSumExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest request) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);
        map.put("data", orderService.selectOrderListCSum(search).getOrderCSumList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("OrderCSummaryList", map), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 통관목록현황집계 필터 코드 엑셀 업로드", notes = "Order 통관목록현황집계 필터 코드를 엑셀 업로드합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/clsFlt/excelUpload/{updateUserId}/{gmtTime}/{userBusinessCode}", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> insertClsFilterCodeExcel(HttpServletRequest request
            , @PathVariable("entCode") String entCode
            , @PathVariable("updateUserId") String updateUserId
            , @PathVariable("gmtTime") String gmtTime
            , @PathVariable("userBusinessCode") String userBusinessCode) throws Exception {

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("excelUpload_file");

        FileUploadResponse result = fileService.storeFile(file, entCode, true);
        if (Util.null2str(result.getResult()).equals("ok")) {
            return new ResponseEntity<OrderSaveResult>(orderService.insertClsFilterCodeExcel(entCode, updateUserId, gmtTime, fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile(), userBusinessCode, request), HttpStatus.OK);
        } else {
            return new ResponseEntity<OrderSaveResult>(orderService.insertClsFilterCodeExcel(entCode, updateUserId, gmtTime, null, null, null), HttpStatus.OK);
        }
    }

    @ApiOperation(value = "전자상거래 Order 고객상담관리 조회", notes = "Order 고객상담관리 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderCus/search/list", method = RequestMethod.GET)
    public ResponseEntity<OrderCusList> selectOrderListCus(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderCusList>(orderService.selectOrderListCus(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 고객상담관리 엑셀파일생성", notes = "Order 고객상담관리 엑셀파일을 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderCus/search/list/excel", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> selectOrderListCusExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest request) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);
        map.put("data", orderService.selectOrderListCus(search).getOrderCusList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("OrderCusList", map), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 추가비용리스트 조회", notes = "Order 추가비용리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderCost/search/list", method = RequestMethod.GET)
    public ResponseEntity<OrderCostList> selectOrderListCost(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderCostList search) throws Exception {
        return new ResponseEntity<OrderCostList>(orderService.selectOrderListCost(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 공통 조회", notes = "Order 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderCommon/search/list", method = RequestMethod.GET)
    public ResponseEntity<OrderCommonList> selectOrderListCommon(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderCommonList>(orderService.selectOrderListCommon(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 OrderList 삭제", notes = "OrderList를 삭제합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public ResponseEntity<OrderSaveResult> deleteOrderList(@PathVariable("entCode") String entCode, @RequestBody OrderListDelete orderListDelete, HttpServletRequest request) throws Exception {
        orderListDelete.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, orderListDelete, "userId", "logUserName", null, "logUserNameEng");
        orderListDelete.setUserName(orderListDelete.getLogUserName());
        return new ResponseEntity<OrderSaveResult>(orderService.deleteOrderList(orderListDelete, request), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order insert", notes = "Order를 insert합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> insertOrder(@PathVariable("entCode") String entCode, @RequestBody Order order, HttpServletRequest request) throws Exception {
        order.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, order, "updateUserId", "logUserName", null, "logUserNameEng");
        order.setRegisterUserId(order.getUpdateUserId());
        OrderSaveResult result = new OrderSaveResult();
        result = orderService.saveOrder(order, request);

        /**
         * EMS환적 오더 저장시 출력물 생성
         */
        if ("Y".equals(order.getEmsYn())) {
            orderService.savePrintInfo(order);
        }

        return new ResponseEntity<OrderSaveResult>(result, HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order update", notes = "Order를 update합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
    })
    @RequestMapping(value = "/update/{odJobNo}", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> updateOrder(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo, @RequestBody Order order, HttpServletRequest request) throws Exception {
        order.setEntCode(entCode);
        order.setOdJobNo(odJobNo);
        Util.getSessionUserInfo(request, jwtTokenProvider, order, "updateUserId", "logUserName", null, "logUserNameEng");
        order.setRegisterUserId(order.getUpdateUserId());
        return new ResponseEntity<OrderSaveResult>(orderService.saveOrder(order, request), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 OrderCost update", notes = "Order추가비용을 update합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
    })
    @RequestMapping(value = "/updateCost/{odJobNo}", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> updateCost(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo, @RequestBody OrderCostUpdate orderCostUpdate, HttpServletRequest request) throws Exception {
        orderCostUpdate.setEntCode(entCode);
        orderCostUpdate.setOdJobNo(odJobNo);
        Util.getSessionUserInfo(request, jwtTokenProvider, orderCostUpdate, "updateUserId", "logUserName", null, "logUserNameEng");
        return new ResponseEntity<OrderSaveResult>(orderService.updateCost(orderCostUpdate, request), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 OdChgInv update", notes = "OdChgInv를 update합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
    })
    @RequestMapping(value = "/updateOdChgInv/{odJobNo}", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> updateOdChgInv(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo, @RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {
        map.put("entCode", entCode);
        map.put("odJobNo", odJobNo);
        Map<String, Object> userMap = Util.getSessionUserInfo(request, jwtTokenProvider);
        if (!Util.isEmpty(userMap.get("USER_ID"))) {
            map.put("updateUserId", userMap.get("USER_ID"));
            map.put("logUserName", userMap.get("USER_NAME"));
            map.put("logUserNameEng", userMap.get("USER_NAME_ENG"));
        }

        return new ResponseEntity<OrderSaveResult>(orderService.updateOdChgInv(map, request), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 조회", notes = "Order를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
    })
    @RequestMapping(value = "/get/{odJobNo}", method = RequestMethod.GET)
    public ResponseEntity<Order> selectOrder(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo, SearchOrder search) throws Exception {
        search.setSrchEntCode(entCode);
        search.setSrchOdJobNo(odJobNo);
        return new ResponseEntity<Order>(orderService.selectOrder(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 수출오더 제출번호 생성", notes = "수출오더 제출번호를 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
    })
    @RequestMapping(value = "/simpleDocNo/get/{odJobNo}", method = RequestMethod.GET)
    public ResponseEntity<OrderSaveResult> selectSimpleDocNo(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo, SearchOrder search, HttpServletRequest request) throws Exception {
        search.setSrchEntCode(entCode);
        search.setSrchOdJobNo(odJobNo);
        Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", null, null);
        return new ResponseEntity<OrderSaveResult>(orderService.selectSimpleDocNo(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 일괄변경", notes = "전자상거래 Order를 일괄변경합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/batch/targetCount", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> batchTargetCount(@PathVariable("entCode") String entCode, @RequestBody SearchOrderBatchUpdate search, HttpServletRequest request) throws Exception {
        search.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
        String userId = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"));
        if (!userId.equals("")) {
            SearchOrderCustomerList searchOrderCustomerList = search.getSearchOrderCustomerList();
            if (searchOrderCustomerList != null) {
                searchOrderCustomerList.setSrchLoginId(userId);
            }
            SearchOrderList searchOrderList = search.getSearchOrderList();
            if (searchOrderList != null) {
                searchOrderList.setUserId(userId);
            }
        }
        return new ResponseEntity<OrderSaveResult>(orderService.batchTargetCount(search, request), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 일괄변경", notes = "전자상거래 Order를 일괄변경합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/batch/Update", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> batchUpdate(@PathVariable("entCode") String entCode, @RequestBody SearchOrderBatchUpdate search, HttpServletRequest request) throws Exception {
        search.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
        SearchOrderList searchOrderList = search.getSearchOrderList();
        if (searchOrderList != null) {
            searchOrderList.setUserId(search.getUpdateUserId());
        }
        OrderSaveResult orderSaveResult = orderService.batchUpdate(search, request);
        if ("updateBmsCdType".equals(orderSaveResult.getResultCode())) {
            List<Map<String, Object>> updateCdTypeList = (List<Map<String, Object>>) orderSaveResult.getResultData().get("updateCdTypeList");
            commonService.updateBmsCdType(updateCdTypeList);
            orderSaveResult.setResultCode(null);
            orderSaveResult.setResultData(null);
        }

        if (profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")) {
			Util.setDB(request, "HUBNK_IF");
        	if(orderSaveResult.getResultCode() == null) {
        		//퀵요청, 기타인경우에는 인터페이스 테이블에 반영 X
        		if ("hold".equals(search.getUpdateType())) {
        			if(!"04".equals(search.getHoldMap().get("holdCode")) && !"06".equals(search.getHoldMap().get("holdCode"))) {
        				Map<String, Object> paramMap = (Map<String, Object>) orderSaveResult.getResultData();
        				orderService.updateHubnHoldList(paramMap);
        			}
        		}
        	}
        }

        return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);

//		OrderSaveResult orderSaveResult = new OrderSaveResult();
//		Map<String, Object> returnMap = orderService.batchUpdateTest(search);
//		if(Util.null2str(returnMap.get("cntrInsert")).equals("Y")){
//			orderService.batchUpdateContainer(returnMap);
//		}
//		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 선택자 일괄변경", notes = "전자상거래 Order 선택자 를 일괄변경합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/batch/Selector/Update", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> batchSelectorUpdate(@PathVariable("entCode") String entCode, @RequestBody SearchOrderBatchUpdate search, HttpServletRequest request) throws Exception {
        search.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
        SearchOrderList searchOrderList = search.getSearchOrderList();
        if (searchOrderList != null) {
            searchOrderList.setUserId(search.getUpdateUserId());
        }
        OrderSaveResult orderSaveResult = orderService.batchSelectorUpdate(search, request);
        return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
    }

    @ApiOperation(value = "오더변경 ExcelUpload", notes = "오더변경 ExcelUpload합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "excelUploadType", value = "엑셀타입", required = true, dataType = "string", paramType = "path", defaultValue = "odChg")
    })
    @RequestMapping(value = "/excelUpload/check/{excelUploadType}", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> excelUploadCheck(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
        return new ResponseEntity<OrderSaveResult>(orderService.excelUploadCheck(uploadExcel, entCode, excelUploadType), HttpStatus.OK);
    }

    @ApiOperation(value = "오더변경 Excel 저장", notes = "오더변경 Excel 저장합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/excelUpload/{excelUploadType}", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> excelUpload(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestBody ExcelUploadOdChg excelUploadOdChg, HttpServletRequest request) throws Exception {
        Util.getSessionUserInfo(request, jwtTokenProvider, excelUploadOdChg, "updateUserId", "logUserName", null, "logUserNameEng");
        if ("insertPod".equals(excelUploadType)) {
            return new ResponseEntity<OrderSaveResult>(orderService.excelUpload(excelUploadOdChg, request, excelUploadType), HttpStatus.OK);
        } else if ("insertEmsTs".equals(excelUploadType)) {
            return new ResponseEntity<OrderSaveResult>(orderService.excelUploadEmsTs(excelUploadOdChg, request), HttpStatus.OK);
        } else if ("updateLicense".equals(excelUploadType)) {
            return new ResponseEntity<OrderSaveResult>(orderService.excelUploadUpdateLicense(excelUploadOdChg, request), HttpStatus.OK);
        } else { // "carryinChg".equals(excelUploadType)
            return new ResponseEntity<OrderSaveResult>(orderService.excelUploadCarryIn(excelUploadOdChg, request), HttpStatus.OK);
        }
    }

    @ApiOperation(value = "전자상거래 Order 수화인우편번호변환", notes = "전자상거래 Order 수화인우편번호변환처리합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/consigneeZipCode/Update", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> updateConsigneeZipCode(@PathVariable("entCode") String entCode, @RequestBody ZipCodeChange zipCodeChange, HttpServletRequest request) throws Exception {
        zipCodeChange.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, zipCodeChange, "updateUserId", "logUserName", null, "logUserNameEng");
        SearchOrderCustomerList searchOrderCustomerList = zipCodeChange.getSearchOrderCustomerList();
        if (searchOrderCustomerList != null) {
            searchOrderCustomerList.setSrchLoginId(zipCodeChange.getUpdateUserId());
        }
        SearchOrderList searchOrderList = zipCodeChange.getSearchOrderList();
        if (searchOrderList != null) {
            searchOrderList.setUserId(Util.null2str(zipCodeChange.getUpdateUserId()));
        }
        return new ResponseEntity<OrderSaveResult>(orderService.updateConsigneeZipCode(zipCodeChange, request), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 합산과세자료조회", notes = "전자상거래 합산과세자료를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/selectCombinedTaxList", method = RequestMethod.GET)
    public ResponseEntity<OrderCombinedTaxList> selectCombinedTaxList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderCombinedTaxList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderCombinedTaxList>(orderService.selectCombinedTaxList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 배제처리", notes = "전자상거래 Order를 배제처리합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/exclusion/Update", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> updateExclusion(@PathVariable("entCode") String entCode, @RequestBody OrderExclusion exclusion, HttpServletRequest request) throws Exception {
        Util.getSessionUserInfo(request, jwtTokenProvider, exclusion, "updateUserId", "logUserName", null, "logUserNameEng");
        return new ResponseEntity<OrderSaveResult>(orderService.updateExclusion(exclusion, request), HttpStatus.OK);
    }

    @ApiOperation(value = "MULTI STOCK생성", notes = "MULTI STOCK생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/multiStock/insert", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> insertMultiStock(@PathVariable("entCode") String entCode, @RequestBody OrderExclusion search, HttpServletRequest request) throws Exception {
        search.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
        return new ResponseEntity<OrderSaveResult>(orderService.insertMultiStock(search, request), HttpStatus.OK);
    }

    @ApiOperation(value = "고객관리 insert", notes = "고객관리정보를 insert후 관리번호로 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/cs/insert", method = RequestMethod.POST)
    public List<OrderCs> insertCs(@PathVariable("entCode") String entCode, @RequestBody OrderCs cs, HttpServletRequest request) throws Exception {
        cs.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, cs, "updateUserId", "logUserName", null, "logUserNameEng");
        cs.setRegisterUserId(cs.getUpdateUserId());
        return orderService.insertCs(cs, request);
    }

    @ApiOperation(value = "고객관리 update", notes = "고객관리정보를 update후 관리번호로 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "seqNoId", value = "일련번호", required = true, dataType = "int", paramType = "path")
    })
    @RequestMapping(value = "/cs/update/{seqNoId}", method = RequestMethod.PUT)
    public List<OrderCs> updateCs(@PathVariable("entCode") String entCode, @PathVariable("seqNoId") String seqNoId, @RequestBody OrderCs cs, HttpServletRequest request) throws Exception {
        cs.setEntCode(entCode);
        cs.setSeqNoId(seqNoId);
        Util.getSessionUserInfo(request, jwtTokenProvider, cs, "updateUserId", "logUserName", null, "logUserNameEng");
        cs.setRegisterUserId(cs.getUpdateUserId());
        return orderService.updateCs(cs, request);
    }

    @ApiOperation(value = "고객관리 delete", notes = "고객관리정보를 delete후 관리번호로 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "seqNoId", value = "일련번호", required = true, dataType = "string", paramType = "path")
    })
    @RequestMapping(value = "/cs/delete/{seqNoId}", method = RequestMethod.DELETE)
    public List<OrderCs> deleteCs(@PathVariable("entCode") String entCode, @PathVariable("seqNoId") String seqNoId, @RequestBody OrderCsDelete csDelete, HttpServletRequest request) throws Exception {
        csDelete.setEntCode(entCode);
        csDelete.setSeqNoId(seqNoId);
        Util.getSessionUserInfo(request, jwtTokenProvider, csDelete, "updateUserId", "logUserName", null, "logUserNameEng");
        return orderService.deleteCs(csDelete, request);
    }

    /**
     * 개인통관부호 검증을 위한 데이터 저장
     *
     * @param entCode
     * @param customsVerificationList
     * @param request
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "개인통관부호 검증", notes = "개인통관부호 검증을 위한 데이터를 Redis에 저장합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
    })
    @RequestMapping(value = "/customs/verification", method = RequestMethod.POST)
    public ResponseEntity<String> insertOrderList(
            @PathVariable("entCode") String entCode,
            @RequestBody List<CustomsVerification> customsVerificationList,
            HttpServletRequest request) throws Exception {
        // 검증 상태 체크를 위해 ElasticSearh 및 Redis에 내역 저장
        String resultIndex = "ecm-customs-verification-result-" + DateTime.getDate("yyyy.MM.dd");
        orderService.setCustomsVerification(resultIndex, entCode, customsVerificationList, Util.getSessionUserInfo(request, jwtTokenProvider));

        return new ResponseEntity<String>("ok", HttpStatus.OK);
    }

    /**
     * 개인통관부호 검증 결과 List
     *
     * @param entCode
     * @param searchCustomsVerificationResult
     * @param request
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "개인통관부호 검증 결과 리스트 조회", notes = "검증 결과 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/search/customs/verification/result/list", method = RequestMethod.GET)
    public ResponseEntity<CustomsVerificationResultList> selectCutstomsVerificationList(
            @PathVariable("entCode") String entCode,
            @QueryStringArgResolver SearchCustomsVerification searchCustomsVerification,
            HttpServletRequest request) throws Exception {

        return new ResponseEntity<CustomsVerificationResultList>(orderService.selectCutstomsVerificationList(searchCustomsVerification), HttpStatus.OK);
    }

    @ApiOperation(value = "대한통운(KOREX),우체국(EMS),한진택배(HANJIN),롯데택배(LOTTE) 트래킹 수집 여부 조회", notes = "전자상거래 대한통운, 우체국, 한진택배 트래킹 수집 여부를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "String", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/delivery/trackingYn", method = RequestMethod.GET)
    public ResponseEntity<List<DeliveryInterface>> selectDeliveryTrackingYn(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
        return new ResponseEntity<List<DeliveryInterface>>(orderService.selectDeliveryTrackingYn(entCode), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 대한통운 주소오류 체크", notes = "전자상거래 대한통운 주소오류 체크합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/printout/korexAddressCheck", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> korexAddressCheck(@PathVariable("entCode") String entCode, @RequestBody SearchPrintOut search) throws Exception {
        return new ResponseEntity<OrderSaveResult>(orderService.korexAddressCheck(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 대한통운(KOREX),우체국(EMS),한진택배(HANJIN),롯데택배(LOTTE),경동(KD) 출력물 생성을 위한 데이터 세팅", notes = "전자상거래 대한통운(KOREX),우체국(EMS),한진택배(HANJIN),롯데택배(LOTTE) 출력물 생성을 위한 데이터 세팅합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/printout/delivery", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> selectDeliveryPrintout(@PathVariable("entCode") String entCode, @RequestBody SearchPrintOut search, HttpServletRequest req) throws Exception {
        Util.getSessionUserInfo(req, jwtTokenProvider, search, "registerUserId", null, null);
        OrderSaveResult result = new OrderSaveResult();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        SearchPrintInfo searchPrint;
        Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);

        if ("Y".equals(search.getPrintCheckYn())) {
            //중복 체크
            List<Order> checkPrintList = orderService.checkDeliveryPrintYn(search);
            if (checkPrintList != null && checkPrintList.size() > 0) {
                returnMap.put("checkPrintList", checkPrintList);
                result.setReturnMap(returnMap);
                result.setResultCode("Y");
                return new ResponseEntity<OrderSaveResult>(result, HttpStatus.OK);
            }
        }

        Map<String, Object> deliveryInfo = null; // 업체 배송정보 조회
        Map<String, Object> korexPramMap = new HashMap<String, Object>(); // 대한통운 집배코드 조회하기 위한 파라미터
        Map<String, Object> postCode = null; // 대한통운 API 우편번호 조회
        Map<String, Object> zipMap = new HashMap<String, Object>(); // 대한통운 집배코드 정보

        Map<String, Object> hanjinParamMap = new HashMap<String, Object>();    //한진 집배코드 정보
        Map<String, Object> lotteParamMap = new HashMap<String, Object>();    //롯데 집배코드 정보

        String branchCode = ""; // 대한통운 집배코드
        String zipcode = ""; // 우편번호 원본

        //출력물에 들어갈 주소,우편번호 조회
        List<Map<String, Object>> dataList = orderService.selectDeliveryPrintout(search);
        orderService.deletePrintoutData(search);    // 로그인 아이디로 등록된 대한통운(KOREX),우체국(EMS) 출력물 데이터 전체 삭제

        if ("EMS".equals(Util.null2str(search.getSrchDeliveryCode()))) {
            returnMap = orderService.printOutConvertEms(dataList);

//			if(!"".equals(Util.null2str(returnMap.get("errorNos")))) {
//				returnMap.put("resultCode", "N");
//				returnMap.put("returnMessage", "우체국 집배코드 정보 없음");
//			}

        } else if ("KOREX".equals(Util.null2str(search.getSrchDeliveryCode()))) {
            /** 대한통운 집배코드조회 프로시저 호출시 오류발생해서     주소를 공백으로 분리한 후 2글자이상일경우 해당 문자가 중복되면 오류처리함 */
            String address;
            Map<String, Object> checkMap = new HashMap<String, Object>();
            for (Map<String, Object> dataMap : dataList) {
                address = Util.null2str(dataMap.get("CONSIGNEE_ADDRESS_LOC"));
                /** 2025.11.21 주소에 괄호가 있을 경우 괄호 안의 내용은 제외하고 중복 체크 */
                if (address.indexOf("(") > -1 && address.indexOf(")") > -1) {
                    address = address.substring(0, address.indexOf("(")) + address.substring(address.indexOf(")") + 1, address.length());
                }
                checkMap.clear();
                for (String word : address.split(" ")) {
                    if (word.length() > 1) {
                        if (checkMap.get(word) == null) {
                            checkMap.put(word, "Y");
                        } else {
                            returnMap.put("returnMessage", "The address is incorrect.(" + address + ")");
                            result.setReturnMap(returnMap);
                            return new ResponseEntity<OrderSaveResult>(result, HttpStatus.OK);
                        }
                    }
                }
            }
			
			/*
				업체코드,비즈니스 코드로 대한통운 등록 정보 가져와서 korexParamMap에 가져온 정보를 넣고 
				대한통운 db 프로시저 호출해서 집배코드 가져와서 해당 데이터 TB_ECM_OD_DELIVERY_INFO 테이블에 넣어야함 
			*/
            String custId = "";

            Map<String, Object> tmpMap = new HashMap<String, Object>();
            tmpMap.put("entCode", search.getSrchEntCode());
            tmpMap.put("srchBoundType", Util.null2str(search.getSrchBoundType()));
//			tmpMap.put("srchCustomerCode", search.getSrchCustomerCode());
            tmpMap.put("srchCustomerCode", "COMMON");    /** 2022-05-30 출력은 거래처코드 COMMON으로 고정 */
            tmpMap.put("userId", search.getRegisterUserId());

            deliveryInfo = orderService.selectDeliveryInterface(tmpMap);

            if (deliveryInfo != null) {
                custId = Util.null2str(deliveryInfo.get("KOREX_ID"));

                for (Map<String, Object> dataMap : dataList) {
                    zipMap.clear();
                    korexPramMap.put("P_CLNTNUM", custId);
                    korexPramMap.put("P_CLNTMGMCUSTCD", custId);
                    korexPramMap.put("P_PRNGDIVCD", "01");
                    korexPramMap.put("P_CGOSTS", "91");
                    korexPramMap.put("P_ADDRESS", Util.null2str(dataMap.get("CONSIGNEE_ADDRESS_LOC")));


                    if (profile.equals("hubn1") || profile.equals("local") || profile.equals("bms11")) {
                        //대한통운rest,db여부 조회 및 인터페이스 정보 가져오기
                        Map<String, Object> srchDeliveryApiMap = new HashMap<String, Object>();
                        Map<String, Object> deliveryApiMap = new HashMap<String, Object>();
                        Map<String, Object> deliveryApiAllMap = new HashMap<String, Object>();
                        srchDeliveryApiMap.put("entCode", search.getSrchEntCode());
                        srchDeliveryApiMap.put("deliveryCode", "KOREX_ADDR");
                        srchDeliveryApiMap.put("korexId", custId);
                        deliveryApiMap = odMapper.selectDeliveryApiInfo(srchDeliveryApiMap);// 중개업체id,url
                        deliveryApiAllMap = odMapper.selectDeliveryApiInfoAllMapping(srchDeliveryApiMap);//사업자번호,업체코드,토큰
                        //없을경우 COMMON적용
                        if (deliveryApiAllMap == null) {
                            deliveryApiAllMap = odMapper.selectDeliveryApiInfoAll(srchDeliveryApiMap);//사업자번호,업체코드
                        }
                        if (deliveryApiAllMap == null || deliveryApiAllMap.isEmpty() || !Util.null2str(deliveryApiAllMap.get("KOREX_METHOD")).equals("REST")) {

                            //기존
                            Util.setDB(req, "KOREXADD");
                            interfaceService.getKorexAddressCode(korexPramMap); // 대한통운 프로시저로 집배코드 정보 조회

                            // 집배코드 변경 프로시저 호출해서 얻은값이 존재 O
                            if (!"".equals(Util.null2str(korexPramMap.get("P_CLSFCD")))) {
                                zipMap.put("KOREX_DSF_ADDR", Util.null2str(korexPramMap.get("P_CLSFADDR")));            // KOREX_DSF_ADDR => 해당값 대한통운 우측상단(프로시저 호출로만 획득가능)
                                zipMap.put("KOREX_BRANS_NM", Util.null2str(korexPramMap.get("P_CLLDLCBRANSHORTNM")));    // KOREX_BRANS_NM
                                zipMap.put("KOREX_EMP_NM", Util.null2str(korexPramMap.get("P_CLLDLVEMPNM")));            // KOREX_EMP_NM
                                zipMap.put("KOREX_NICK_NM", Util.null2str(korexPramMap.get("P_CLLDLVEMPNICKNM")));        // KOREX_NICK_NM
                                branchCode = Util.null2str(korexPramMap.get("P_CLSFCD")) + Util.null2str(korexPramMap.get("P_SUBCLSFCD"));
                            } else { // 프로시저 호출해서 얻은값이 존재 X  => 대한통운 DB에서 조회
                                zipcode = Util.null2str(dataMap.get("CONSIGNEE_ZIPCODE"));
                                if (!"".equals(zipcode)) {
                                    Util.setDB(req, "KOREX");
                                    postCode = interfaceService.checkKorexPostCode(zipcode); // 대한통운 TB_POST010 테이블에서 집배코드 정보 조회
                                    if (postCode != null) {
                                        if (Util.null2str(postCode.get("MAN_BRAN_NM")).length() > 4) {
                                            zipMap.put("KOREX_BRANS_NM", Util.null2str(postCode.get("MAN_BRAN_NM")).substring(1, 4)); // KOREX_BRANS_NM
                                        } else {
                                            zipMap.put("KOREX_BRANS_NM", Util.null2str(postCode.get("MAN_BRAN_NM"))); // KOREX_BRANS_NM
                                        }
                                        zipMap.put("KOREX_EMP_NM", Util.null2str(postCode.get("CLDV_EMP_NM"))); // KOREX_EMP_NM
                                        branchCode = Util.null2str(postCode.get("END_NO")) + Util.null2str(postCode.get("SUB_END_NO"));
                                    }
                                }

                            }

                        } else {
                            // 추가된 REST 방식
                            HttpHeaders cjHeaders = new HttpHeaders();
                            cjHeaders.setContentType(MediaType.APPLICATION_JSON);

                            cjHeaders.set("CJ-Gateway-APIKey", Util.null2str(deliveryApiAllMap.get("KOREX_TOKEN_NUM")));

                            Map<String, Object> cjData = new HashMap<String, Object>();
                            cjData.put("CLNTNUM", custId);//고객id
                            cjData.put("CLNTMGMCUSTCD", custId);//협력사id
                            cjData.put("USER_ID", deliveryApiMap.get("RECV_API_ID")); // 중개업체id
                            cjData.put("ADDRESS", korexPramMap.get("P_ADDRESS")); // 괄호 제거 전 원본 주소
                            cjData.put("TOKEN_NUM", deliveryApiAllMap.get("KOREX_TOKEN_NUM"));

                            Map<String, Object> cjReqBody = new HashMap<String, Object>();
                            cjReqBody.put("DATA", cjData);

                            log.debug("대한통운 요청 Body : {}", new Gson().toJson(cjReqBody));

                            HttpEntity<Map<String, Object>> cjEntity = new HttpEntity<>(cjReqBody, cjHeaders);
                            RestTemplate restTemplate = new RestTemplate();

                            ResponseEntity<Map> cjRes = restTemplate.exchange(
                                    Util.null2str(deliveryApiMap.get("RECV_API_URL")),
                                    HttpMethod.POST,
                                    cjEntity,
                                    Map.class
                            );
                            // REST 응답
                            if (cjRes.getBody() != null) {
                                log.debug("대한통운 응답 : {}", new Gson().toJson(cjRes.getBody()));
                                Map<String, Object> cjResult = (Map<String, Object>) cjRes.getBody();
                                String resultCd = Util.null2str(cjResult.get("RESULT_CD"));
                                if ("S".equals(resultCd)) {
                                    Map<String, Object> getData = (Map<String, Object>) cjResult.get("DATA");
                                    korexPramMap.put("P_CLSFCD", Util.null2str(getData.get("CLSFCD")));
                                    korexPramMap.put("P_SUBCLSFCD", Util.null2str(getData.get("SUBCLSFCD")));
                                    korexPramMap.put("P_CLSFADDR", Util.null2str(getData.get("CLSFADDR")));
                                    korexPramMap.put("P_CLLDLCBRANSHORTNM", Util.null2str(getData.get("CLLDLVBRANNM")));
                                    korexPramMap.put("P_CLLDLVEMPNM", Util.null2str(getData.get("CLLDLVEMPNM")));
                                    korexPramMap.put("P_CLLDLVEMPNICKNM", Util.null2str(getData.get("CLLDLVEMPNICKNM")));
                                } else {
//									accessFlag    = "F";
                                    String returnMessage = Util.null2str(cjResult.get("RESULT_DETAIL")); // CJ 오류메시지
                                    log.debug("대한통운 응답 : {}", returnMessage);
                                }
                            } else {
//								accessFlag    = "F";
                                String returnMessage = "대한통운 API 응답 없음";
                                log.debug("대한통운 응답 : {}", returnMessage);
                            }
                            // REST도 동일한 파싱 로직
                            if (!"".equals(Util.null2str(korexPramMap.get("P_CLSFCD")))) {
                                zipMap.put("KOREX_DSF_ADDR", Util.null2str(korexPramMap.get("P_CLSFADDR")));
                                zipMap.put("KOREX_BRANS_NM", Util.null2str(korexPramMap.get("P_CLLDLCBRANSHORTNM")));
                                zipMap.put("KOREX_EMP_NM", Util.null2str(korexPramMap.get("P_CLLDLVEMPNM")));
                                zipMap.put("KOREX_NICK_NM", Util.null2str(korexPramMap.get("P_CLLDLVEMPNICKNM")));
                                branchCode = Util.null2str(korexPramMap.get("P_CLSFCD")) + Util.null2str(korexPramMap.get("P_SUBCLSFCD"));
                            }
                        }

                    } else {
                        Util.setDB(req, "KOREXADD");
                        interfaceService.getKorexAddressCode(korexPramMap); // 대한통운 프로시저로 집배코드 정보 조회

                        // 집배코드 변경 프로시저 호출해서 얻은값이 존재 O
                        if (!"".equals(Util.null2str(korexPramMap.get("P_CLSFCD")))) {
                            zipMap.put("KOREX_DSF_ADDR", Util.null2str(korexPramMap.get("P_CLSFADDR")));            // KOREX_DSF_ADDR => 해당값 대한통운 우측상단(프로시저 호출로만 획득가능)
                            zipMap.put("KOREX_BRANS_NM", Util.null2str(korexPramMap.get("P_CLLDLCBRANSHORTNM")));    // KOREX_BRANS_NM
                            zipMap.put("KOREX_EMP_NM", Util.null2str(korexPramMap.get("P_CLLDLVEMPNM")));            // KOREX_EMP_NM
                            zipMap.put("KOREX_NICK_NM", Util.null2str(korexPramMap.get("P_CLLDLVEMPNICKNM")));        // KOREX_NICK_NM
                            branchCode = Util.null2str(korexPramMap.get("P_CLSFCD")) + Util.null2str(korexPramMap.get("P_SUBCLSFCD"));
                        } else { // 프로시저 호출해서 얻은값이 존재 X  => 대한통운 DB에서 조회
                            zipcode = Util.null2str(dataMap.get("CONSIGNEE_ZIPCODE"));
                            if (!"".equals(zipcode)) {
                                Util.setDB(req, "KOREX");
                                postCode = interfaceService.checkKorexPostCode(zipcode); // 대한통운 TB_POST010 테이블에서 집배코드 정보 조회
                                if (postCode != null) {
                                    if (Util.null2str(postCode.get("MAN_BRAN_NM")).length() > 4) {
                                        zipMap.put("KOREX_BRANS_NM", Util.null2str(postCode.get("MAN_BRAN_NM")).substring(1, 4)); // KOREX_BRANS_NM
                                    } else {
                                        zipMap.put("KOREX_BRANS_NM", Util.null2str(postCode.get("MAN_BRAN_NM"))); // KOREX_BRANS_NM
                                    }
                                    zipMap.put("KOREX_EMP_NM", Util.null2str(postCode.get("CLDV_EMP_NM"))); // KOREX_EMP_NM
                                    branchCode = Util.null2str(postCode.get("END_NO")) + Util.null2str(postCode.get("SUB_END_NO"));
                                }
                            }

                        }
                    }

                    if (branchCode.length() > 2) {
                        zipMap.put("KOREX_NO1", branchCode.substring(0, 1));
                        if (branchCode.length() == 5) {
                            zipMap.put("KOREX_NO2", branchCode.substring(1, branchCode.length() - 1));
                            zipMap.put("KOREX_NO3", branchCode.substring(branchCode.length() - 1));
                        } else {
                            zipMap.put("KOREX_NO2", branchCode.substring(1, branchCode.length() - 2));
                            zipMap.put("KOREX_NO3", branchCode.substring(branchCode.length() - 2));
                        }
                    }


                    Util.setDB(req, entCode);
                    if (zipMap != null && zipMap.size() > 0) {
                        zipMap.put("odJobNo", dataMap.get("OD_JOB_NO"));
                        zipMap.put("registerUserId", dataMap.get("registerUserId"));
                        orderService.insertZipCodeInfo(zipMap); // 각 하우스에 해당하는 각 배송사(우체국,대한통운) 집배코드 정보 INSERT
                        //returnMap.put("resultCode","Y");
                    }
                    returnMap.put("resultCode", "Y"); // 우체국 zipCode 데이터가 존재 유무와 상관없이 출력요청

                } // for end

            } else {
                returnMap.put("resultCode", "N");
                returnMap.put("returnMessage", "대한통운 배송정보 없음");
            }

            //한진 운송장 추가 - 인쇄양식설정에서 등록 후 사용 > 일반 출력물로 변경됨
        } else if ("HANJIN".equals(Util.null2str(search.getSrchDeliveryCode()))) {

            returnMap.put("resultCode", "Y");

            String clientId = "";        //hanjinClientId
            String authKey = "";        //hanjinApiCode
            String hanjinZipcode = "";    //hanjinZipcode

            //배송사 IF에서 등록한 배송사 데이터 조회
            Map<String, Object> tmpMap = new HashMap<String, Object>();
            tmpMap.put("entCode", search.getSrchEntCode());
            tmpMap.put("srchBoundType", Util.null2str(search.getSrchBoundType()));
            tmpMap.put("srchCustomerCode", "COMMON");    /** 2022-05-30 출력은 거래처코드 COMMON으로 고정 */
            tmpMap.put("userId", search.getRegisterUserId());

            deliveryInfo = orderService.selectDeliveryInterface(tmpMap);

            if (deliveryInfo != null) {

                clientId = Util.null2str(deliveryInfo.get("HANJIN_CLIENT_ID"));
                authKey = Util.null2str(deliveryInfo.get("HANJIN_API_CODE"));
                hanjinZipcode = Util.null2str(deliveryInfo.get("HANJIN_ZIPCODE"));

                if (clientId.equals("") || authKey.equals("")) {
                    returnMap.put("returnMessage", "배송사 IF - 한진택배 Api ClientId, AuthKey 정보가 없습니다.");
                    result.setResultCode("N");
                    result.setReturnMap(returnMap);
                    return new ResponseEntity<OrderSaveResult>(result, HttpStatus.OK);
                }

                //한진 운송장 출력정보 조회
//				searchPrint = new SearchPrintInfo();
//				searchPrint.setPrintFormCode(search.getSrchPrintFormCode());
//				List<Map<String, Object>> printInfoList = printMapper.getPrintFormItemList(searchPrint);

                Map insertMap = null;
                List insertDataList = new ArrayList();

                JSONObject hanjinObj = new JSONObject();

                SqlSession sqlSession = null;
                try {
                    sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
                    InterfaceMapper interfaceMapper = sqlSession.getMapper(InterfaceMapper.class);
                    int cnt = 0;
                    for (Map<String, Object> dataMap : dataList) {
                        hanjinObj.clear();
                        hanjinObj.put("api_key", authKey);
                        hanjinObj.put("client_id", clientId);
                        hanjinObj.put("address", Util.null2str(dataMap.get("CONSIGNEE_ADDRESS_LOC")));
                        hanjinObj.put("snd_zip", hanjinZipcode);
                        hanjinObj.put("rcv_zip", Util.null2str(dataMap.get("CONSIGNEE_ZIPCODE")));
                        hanjinObj.put("msg_key", Util.null2str(dataMap.get("OD_JOB_NO")));

                        //API로 한진 집배코드 정보 가져옴
                        hanjinParamMap = interfaceService.connectHanjinApi(hanjinObj);

                        //API 처리 결과가 유효하지 않은 경우 - 출력 x (일단 한 건이라도 유효하지 않다면 아예 출력 안 되게 막아놓음)
                        if (!"OK".equals(hanjinParamMap.get("result_code"))) {
                            returnMap.put("resultCode", "N");
                            returnMap.put("returnMessage", hanjinParamMap.get("result_message"));
                            //API 처리 결과가 정상인 경우
                        } else if (hanjinParamMap != null) {

                            insertMap = new HashMap();

                            String domRgn = "";
                            String controlValue = "";
                            String deliveryPaymentType = "";
                            String consigneeName = "";
                            StringBuffer nameTemp = new StringBuffer();
                            String consigneeTel = "";
                            String consigneeHp = "";
                            StringBuffer telTemp = new StringBuffer();
                            StringBuffer hpTemp = new StringBuffer();

                            insertMap.put("HANJIN_HUB_COD", Util.null2str(hanjinParamMap.get("hub_cod")));
                            insertMap.put("HANJIN_TML_COD", Util.null2str(hanjinParamMap.get("tml_cod")));
                            insertMap.put("HANJIN_DOM_MID", Util.null2str(hanjinParamMap.get("dom_mid")));
                            insertMap.put("HANJIN_CEN_COD", Util.null2str(hanjinParamMap.get("cen_cod")));
                            insertMap.put("HANJIN_CEN_NAM", Util.null2str(hanjinParamMap.get("cen_nam")));
                            insertMap.put("HANJIN_S_TML_COD", Util.null2str(hanjinParamMap.get("s_tml_cod")));
                            insertMap.put("HANJIN_S_TML_NAM", Util.null2str(hanjinParamMap.get("s_tml_nam")));
                            insertMap.put("HANJIN_GRP_RNK", Util.null2str(hanjinParamMap.get("grp_rnk")));
                            insertMap.put("HANJIN_ES_NAM", Util.null2str(hanjinParamMap.get("es_nam")));
                            insertMap.put("HANJIN_PRT_ADD", Util.null2str(hanjinParamMap.get("prt_add")));
                            insertMap.put("HANJIN_ES_COD", Util.null2str(hanjinParamMap.get("es_cod")));
                            insertMap.put("HANJIN_DOM_RGN", Util.null2str(hanjinParamMap.get("dom_rgn")));

                            insertMap.put("OD_JOB_NO", Util.null2str(dataMap.get("OD_JOB_NO")));
                            insertMap.put("registerUserId", Util.null2str(search.getRegisterUserId()));
                            insertMap.put("gmtTime", Util.null2str(search.getGmtTime()));

                            printMapper.insertHanjinPrintData(insertMap);

                            sqlSession.flushStatements();
                            sqlSession.clearCache();
                            sqlSession.commit();
                        }

                    }
                } finally {
                    sqlSession.close();
                }
            } else {
                returnMap.put("resultCode", "N");
                returnMap.put("returnMessage", "한진 배송정보 없음");
            }
        } else if ("LOTTE".equals(Util.null2str(search.getSrchDeliveryCode()))) {
            returnMap.put("resultCode", "Y");

            String clientId = "";         //lotteClientId
            String authKey = "";         //lotteApiCode

            //배송사 IF에서 등록한 배송사 데이터 조회
            Map<String, Object> tmpMap = new HashMap<String, Object>();
            tmpMap.put("entCode", search.getSrchEntCode());
            tmpMap.put("srchBoundType", Util.null2str(search.getSrchBoundType()));
            tmpMap.put("srchCustomerCode", "COMMON");    /** 2022-05-30 출력은 거래처코드 COMMON으로 고정 */
            tmpMap.put("userId", search.getRegisterUserId());

            deliveryInfo = orderService.selectDeliveryInterface(tmpMap);

            if (deliveryInfo != null) {
                clientId = Util.null2str(deliveryInfo.get("LOTTE_CUST_ID"));
                authKey = Util.null2str(deliveryInfo.get("LOTTE_AUTH_KEY"));

                if (clientId.equals("") || authKey.equals("")) {
                    returnMap.put("returnMessage", "배송사 IF - 롯데택배 Api ClientId, AuthKey 정보가 없습니다.");
                    result.setResultCode("N");
                    result.setReturnMap(returnMap);
                    return new ResponseEntity<OrderSaveResult>(result, HttpStatus.OK);
                }

                //롯데 운송장 출력정보 조회
                /*
                 * searchPrint = new SearchPrintInfo();
                 * searchPrint.setPrintFormCode(search.getSrchPrintFormCode()); List<Map<String,
                 * Object>> printInfoList = printMapper.getPrintFormItemList(searchPrint);
                 */
                Map<String, Object> insertMap = null;
                JSONObject lotteObj = new JSONObject();

                SqlSession sqlSession = null;

                try {
                    sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
                    InterfaceMapper interfaceMapper = sqlSession.getMapper(InterfaceMapper.class);
                    int cnt = 0;
                    for (Map<String, Object> dataMap : dataList) {
                        lotteObj.clear();
                        lotteObj.put("api_key", Util.null2str(deliveryInfo.get("LOTTE_AUTH_KEY")));
                        lotteObj.put("id", Util.null2str(deliveryInfo.get("LOTTE_CUST_ID")));                //거래처코드(6자리 택배코드) 또는 별도부여 ID
                        //2023.11.30 박세진 부장님 요청 : 00으로 고정
                        lotteObj.put("network", Util.null2str("00"));                                        //필수, 00:일반,01:특화
                        lotteObj.put("area_no", Util.null2str(dataMap.get("CONSIGNEE_ZIPCODE")));            //필수, 도착지 새 우편번호
                        lotteObj.put("zip_no", Util.null2str(""));                                            //옵션, 도착지 구 우편번호
                        lotteObj.put("address", Util.null2str(dataMap.get("CONSIGNEE_ADDRESS_LOC")));        //필수, 도착지 주소
                        lotteObj.put("pick_area_no", Util.null2str(deliveryInfo.get("LOTTE_ZIPCODE")));        //필수, 출발지 새 우편번호
                        lotteObj.put("pick_zip_no", Util.null2str(""));                                        //옵션, 출반지 구 우편번호
//						lotteObj.put("pick_address",StringUtill.nullToString(deliveryInfo.get("LOTTE_ADDR")));	//필수, 출발지 주소 -> 박세진 부장님 : 노선 구분 사용X므로 보낼 필요X

                        lotteParamMap = interfaceService.connectLotteAPI(lotteObj);
						
						/*if(!"success".equals(lotteParamMap.get("result"))) {
							returnMap.put("resultCode","N");
							returnMap.put("returnMessage",Util.null2str(lotteParamMap.get("message")));
						}else*/
                        if (lotteParamMap != null) {
                            insertMap = new HashMap();
                            insertMap.put("odJobNo", dataMap.get("OD_JOB_NO"));
                            insertMap.put("registerUserId", dataMap.get("registerUserId"));
                            insertMap.put("LOTTE_TML_NM", Util.null2str(lotteParamMap.get("tml_nm")));                // 분류 코드
                            insertMap.put("LOTTE_CITY_GUN_GU", Util.null2str(lotteParamMap.get("city_gun_gu")));    // 주소(시군구)
                            insertMap.put("LOTTE_DONG", Util.null2str(lotteParamMap.get("dong")));                    // 주소(동)
                            insertMap.put("LOTTE_FILT_CD", Util.null2str(lotteParamMap.get("filt_cd")));            // 도착지 코드
                            insertMap.put("LOTTE_BRNSHP_NM", Util.null2str(lotteParamMap.get("brnshp_nm")));        // 배달 영업소
                            insertMap.put("LOTTE_EMP_NM", Util.null2str(lotteParamMap.get("emp_nm")));                // 배송 사원명

                            orderService.insertZipCodeInfo(insertMap); // 각 하우스에 해당하는 각 배송사(롯데택배) 집배코드 정보 INSERT
                            //returnMap.put("resultCode","Y");
                        }
                        sqlSession.flushStatements();
                        sqlSession.clearCache();
                        sqlSession.commit();
                    }

                    returnMap.put("resultCode", "Y"); // 우체국 zipCode 데이터가 존재 유무와 상관없이 출력요청

                } finally {
                    sqlSession.close();
                }
            } else {
                returnMap.put("resultCode", "N");
                returnMap.put("returnMessage", "롯데 배송정보 없음");
            }
        } else if ("KD".equals(Util.null2str(search.getSrchDeliveryCode()))) {
            // 경동택배는 별도 API 없이 바로 출력
            returnMap.put("resultCode", "Y");
        } else if ("KREAM".equals(Util.null2str(search.getSrchDeliveryCode()))) {
            // 크림은 별도 API 없이 바로 출력
            returnMap.put("resultCode", "Y");
        }

        // returnMap 특정한 값일때만 업데이트 실패면 js로 실패코드 전달
        if ("Y".equals(Util.null2str(returnMap.get("resultCode")))) {
            orderService.updatePrintOutDate(search); // 출력물 출력여부 및 출력날짜 업데이트
            result.setResultCode("Y");
        } else {
            result.setResultCode("N");
            result.setReturnMap(returnMap);
        }

        return new ResponseEntity<OrderSaveResult>(result, HttpStatus.OK);
    }

    @ApiOperation(value = "pod list", notes = "pod 상태 리스트 반환")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
    })
    @RequestMapping(value = "/podStatus/search", method = RequestMethod.GET)
    public ResponseEntity<Order> selectOrderPodStatusList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<Order>(orderService.selectOrderPodStatusList(search), HttpStatus.OK);
    }

    /**
     * 합산과세 list
     *
     * @param entCode
     * @param functionOrder
     * @param request
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @ApiOperation(value = "합산과세 list 조회", notes = "합산과세 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/selectCombinedTaxList/excel", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> selectOrderExcel(
            @PathVariable("entCode") String entCode,
            @QueryStringArgResolver SearchOrderCombinedTaxList search,
            HttpServletRequest request) throws Exception {

        search.setPage(0);
        OrderCombinedTaxList list = orderService.selectCombinedTaxList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);
        map.put("data", list.getOrderCombinedTaxList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("CombinedTax_List", map), HttpStatus.OK);
    }

    /**
     * 물품소재지정보 조회
     *
     * @param entCode
     * @param entBusinessCode
     * @param request
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @ApiOperation(value = "사업장으로 물품소재지정보 조회", notes = "사업장으로 물품소재지정보를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/goodsLocInfo", method = RequestMethod.GET)
    public ResponseEntity<Order> getGoodsLocInfo(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("entCode", entCode);
        map.put("entBusinessCode", request.getParameter("entBusinessCode"));
        map.put("userLanguage", request.getParameter("userLanguage"));
        return new ResponseEntity<Order>(orderService.getGoodsLocInfo(map), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order Master 기준 수량,중량 조회", notes = "전자상거래 Order Master 기준 수량,중량 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
    })
    @RequestMapping(value = "/search/gwtPieces", method = RequestMethod.GET)
    public ResponseEntity<Order> selectOrderGwtPieces(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<Order>(orderService.selectOrderGwtPieces(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 홀드사유 조회", notes = "Order 홀드사유 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/hold/list", method = RequestMethod.GET)
    public ResponseEntity<List<OrderHold>> selectHold(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("srchEntCode", entCode);
        map.put("srchOdJobNo", request.getParameter("srchOdJobNo"));
        map.put("dateFormat", request.getParameter("dateFormat"));
        return new ResponseEntity<List<OrderHold>>(orderService.selectHold(map), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order insert", notes = "Order를 insert합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/hold/save", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> saveHold(@PathVariable("entCode") String entCode, @RequestBody Order order, HttpServletRequest request) throws Exception {
        order.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, order, "registerUserId", "logUserName", null, "logUserNameEng");
        OrderSaveResult orderSaveResult = new OrderSaveResult();
        orderSaveResult = orderService.saveHold(order);

        if (profile.equals("bms11") || profile.contains("hubn") || profile.contains("local")) {
			Util.setDB(request, "HUBNK_IF");
            Map<String, Object> paramMap = (Map<String, Object>) orderSaveResult.getResultData();
            orderService.updateHubnHoldList(paramMap);
        }

        return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 상표권침해물품 조회", notes = "상표권침해물품 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/trademark/search", method = RequestMethod.GET)
    public ResponseEntity<TrademarkInfringement> trademark(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest request) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<TrademarkInfringement>(orderService.trademark(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 대한통운 트래킹 조회", notes = "Order 대한통운 트래킹 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/update/korexTracking", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> updateKorexTracking(@PathVariable("entCode") String entCode, @RequestBody SearchOrderList search, HttpServletRequest request) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderSaveResult>(orderService.updateKorexTracking(search), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order multi 조회", notes = "Order multi 리스트를 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/multi/search/list", method = RequestMethod.GET)
    public ResponseEntity<OrderDetList> selectMultiOrderList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderDetList>(orderService.selectMultiOrderList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "중복HAWB 조회 - Master list 조회", notes = "중복HAWB 조회 - Master 리스트를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/search/master/list", method = RequestMethod.GET)
    public ResponseEntity<OrderMasterDupList> selectOrderMasterDupList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderMasterDupList>(orderService.selectOrderMasterDupList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "중복HAWB 조회 - House list 조회", notes = "중복HAWB 조회 - House 리스트를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/search/house/list", method = RequestMethod.GET)
    public ResponseEntity<OrderHouseDupList> selectOrderHouseDupList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<OrderHouseDupList>(orderService.selectOrderHouseDupList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "중복HAWB 조회 - 엑셀파일생성", notes = "중복HAWB 조회 엑셀파일을 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/search/house/list/excel", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> selectOrderHouseDupExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search, HttpServletRequest req) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);
        map.put("data", orderService.selectOrderHouseDupList(search).getOrderHouseDupList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Duplicate_House_List", map), HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 Order 선택자 일괄변경", notes = "전자상거래 Order 선택자 를 일괄변경합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderReset/Update", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> orderResetUpdate(@PathVariable("entCode") String entCode, @RequestBody SearchOrderBatchUpdate search, HttpServletRequest request) throws Exception {
        search.setEntCode(entCode);
        OrderSaveResult orderSaveResult = orderService.orderResetUpdate(search, request);
        return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
    }

    @ApiOperation(value = "전자상거래 수출 통관용 엑셀파일생성", notes = "전자상거래 수출 통관용 엑셀파일을 생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/orderDet/search/list/customsExp", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectOrderListCnExcelPost(@PathVariable("entCode") String entCode, @RequestBody SearchOrderList search, HttpServletRequest request) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(search, Map.class);

        String documentCode = Util.null2str(search.getDocumentCode());

        String[] columnsArr = {"num", "poNo", "hblNo", "gwt", "cnt", "cbm", "hsCode", "commodityItem", "pieces", "unitPrice", "totalAmount"};
        String[] columnsWidthArr = {"63", "100", "100", "80", "80", "80", "80", "150", "80", "80", "100"};
        Integer[] numFormatsArr = {0, -1, -1, 2, 0, 2, -1, -1, 0, 2, 2};
        String[] excelTypesArr = {"center", "left", "left", "double", "number", "double", "left", "left", "number", "double", "double"};
        String[] titlesArr = {"번호", "오더번호", "하우스번호", "중량", "건수", "CBM", "HSCODE", "중문품명", "수량", "단가", "총금액"};

        if ("gridJp".equals(documentCode)) {
            columnsArr = new String[]{"num", "hblNo", "", "", "", "duty", "", "commodityItem", "pieces", "gwt", "currencyCode", "invValue", "", "", "", "", "", "", "shipperName", "", "shipperAddress", "", "shipperTel", "", "", "", "", "", "", "", "destCode", "", "", "", "", "", "", "", "", "", ""};
            columnsWidthArr = new String[]{"63", "100", "70", "50", "50", "50", "70", "150", "80", "80", "80", "80", "70", "70", "70", "100", "70", "70", "100", "70", "200", "70", "100", "70", "70", "70", "70", "70", "70", "70", "100", "70", "70", "70", "70", "70", "70", "70", "70", "70", "70"};
            numFormatsArr = new Integer[]{0, -1, -1, -1, -1, -1, -1, -1, 0, 2, -1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
            excelTypesArr = new String[]{"center", "center", "center", "center", "center", "center", "center", "center", "number", "double", "center", "double", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center", "center"};
            titlesArr = new String[]{"No.", "HAWB", "MAOHONG番号", "R/A", "NOTE", "Duty", "AirbillNo", "ITEM", "No. OF PKG", "WEIGHT", "CRCY CODE", "INV VALUE", "CR CODE", "CNEE", "ADD", "Ｐ．Ｉ．Ｃ", "CNEE'S TEL", "EXPTER'S CODE", "SHPR", "請求先", "ADD", "P.I.C", "SHP'S TEL", "インボイス価格区分コード", "インボイス価格条件コード", "インボイス通貨コード", "インボイス価格", "Bag No", "予定日", "備考", "最終仕向地コード", "処理種別", "貨物識別", "貨物種別", "配達先会社名", "ADD", "P.I.C", "配達先 TEL.", "申告価格", "関税", "増値税"};
            search.setSrchOrigCountry("JP");
        }else {
        	search.setSrchOrigCountry("CN");
        }

        List<String> columns = new ArrayList<String>();
        List<String> columnsWidth = new ArrayList<String>();
        List<Integer> numFormats = new ArrayList<Integer>();
        List<String> excelTypes = new ArrayList<String>();
        List<String> titles = new ArrayList<String>();
        for (int i = 0; i < columnsArr.length; i++) {
            columns.add(columnsArr[i]);
            columnsWidth.add(columnsWidthArr[i]);
            numFormats.add(numFormatsArr[i]);
            excelTypes.add(excelTypesArr[i]);
            titles.add(titlesArr[i]);
        }
        map.put("columns", columns);
        map.put("columnsWidth", columnsWidth);
        map.put("numFormats", numFormats);
        map.put("excelTypes", excelTypes);
        map.put("titles", titles);

        map.put("data", orderService.selectOrderListCustomsExp(search).getOrderDetList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(Util.null2str(search.getDocumentName()), map, "xlsx"), HttpStatus.OK);
    }

    @ApiOperation(value = "Main 주문상태 대시보드 조회", notes = "Main 주문상태 대시보드 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public ResponseEntity<OrderDashboard> selectOrderDashboard(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOrderList search) throws Exception {
        if (search == null || search.getSrchEntCode() == null) {
            return new ResponseEntity<OrderDashboard>(new OrderDashboard(), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<OrderDashboard>(orderService.selectOrderDashboard(search), HttpStatus.OK);
        }
    }

    @ApiOperation(value = "전자상거래 Order 일괄변경", notes = "전자상거래 Order를 일괄변경합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/trackingPod/Update", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> trackingPodUpdate(@PathVariable("entCode") String entCode, @RequestBody SearchOrderPodUpdate search, HttpServletRequest request) throws Exception {
        search.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
        SearchOrderList searchOrderList = search.getSearchOrderList();
        if (searchOrderList != null) {
            searchOrderList.setUserId(search.getUpdateUserId());
        }
        return new ResponseEntity<OrderSaveResult>(orderService.trackingPodUpdate(search, request), HttpStatus.OK);

    }

    @ApiOperation(value = "신규제출번호생성(이고반출)", notes = "신규제출번호생성(이고반출)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/simpleDocNo/save", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> saveHubnSimpleDocNo(@PathVariable("entCode") String entCode, @RequestBody SearchOrderBatchUpdate search, HttpServletRequest request) throws Exception {
        search.setEntCode(entCode);
        Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
        SearchOrderList searchOrderList = search.getSearchOrderList();
        if (searchOrderList != null) {
            searchOrderList.setUserId(search.getUpdateUserId());
        }
        return new ResponseEntity<OrderSaveResult>(orderService.saveHubnSimpleDocNo(search, request), HttpStatus.OK);
    }
}