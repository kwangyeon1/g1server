package org.yht.exps.express.controller.eCommerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryHList;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryMList;
import org.yht.exps.express.model.eCommerce.delivery.DeliverySaveResult;
import org.yht.exps.express.model.eCommerce.delivery.UpdateDelivery;
import org.yht.exps.express.model.eCommerce.delivery.search.SearchDeliveryMList;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.eCommerce.DeliveryService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/delivery")
public class DeliveryController {
 
	@Resource(name = "deliveryService")
	DeliveryService DeliveryService;
	@Resource(name = "commonService")
	CommonService commonService;	
	@Resource(name = "fileService")
	FileService fileService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "전자상거래 Delivery-M List(main) 조회", notes="Delivery-M 리스트(main)를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/deliveryM/search/list", method = RequestMethod.GET)
	public ResponseEntity<DeliveryMList> selectDeliveryMList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchDeliveryMList search) throws Exception {
		return new ResponseEntity<DeliveryMList>(DeliveryService.selectDeliveryMList(search), HttpStatus.OK);
	}	
	
	@ApiOperation(value = "전자상거래 Delivery-H List(sub) 조회", notes="Delivery-H 리스트(sub)를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/deliveryM/item/search/list", method = RequestMethod.GET)
	public ResponseEntity<DeliveryHList> selectDeliveryHList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchDeliveryMList search) throws Exception {
		return new ResponseEntity<DeliveryHList>(DeliveryService.selectDeliveryHList(search), HttpStatus.OK);
	} 		
	
	@ApiOperation(value = "전자상거래 Delivery 배송사 지정", notes="Delivery 배송사 지정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/assign", method = RequestMethod.PUT)
	public ResponseEntity<DeliverySaveResult> assignDelivery(HttpServletRequest request, @PathVariable("entCode") String entCode, @RequestBody UpdateDelivery assign) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, assign, "updateUserId", "logUserName", null, "logUserNameEng");
		assign.setRegisterUserId(assign.getUpdateUserId());
		return new ResponseEntity<DeliverySaveResult>(DeliveryService.assignDelivery(request ,assign), HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "전자상거래 Delivery 배송사 지정(엑셀)", notes="Delivery 배송사 엑셀파일을 업로드하여 지정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "deliveryCode", value = "배송사", required = true, dataType = "string", paramType = "path", defaultValue = "EMS"),
		@ApiImplicitParam(name = "updateUserId", value = "수정자", required = true, dataType = "string", paramType = "path", defaultValue = "201909-007"),
		@ApiImplicitParam(name = "gmtTime", value = "GMT", required = true, dataType = "string", paramType = "path", defaultValue = "0"),
		@ApiImplicitParam(name = "userBusinessCode", value = "작업자사업장코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX-KR")	
	})
	@RequestMapping(value="/assignExcel/{updateUserId}/{gmtTime}/{userBusinessCode}", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> assignDeliveryExcel(HttpServletRequest request
			, @PathVariable("entCode") String entCode
			, @PathVariable("updateUserId") String updateUserId
			, @PathVariable("gmtTime") String gmtTime
			, @PathVariable("userBusinessCode") String userBusinessCode) throws Exception {
		updateUserId = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"), updateUserId);
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
		MultipartFile file = multipartRequest.getFile("deliveryFile");
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(file, new String[]{"xlsx", "xls"})) {
			OrderSaveResult orderSaveResult = new OrderSaveResult();
			orderSaveResult.setResultCode("extError");
			return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
		}
		
		FileUploadResponse result = fileService.storeFile(file, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")){
			return new ResponseEntity<OrderSaveResult>(DeliveryService.assignDeliveryExcel(entCode, updateUserId, gmtTime, fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile(), userBusinessCode, request), HttpStatus.OK);
		}else {
			return new ResponseEntity<OrderSaveResult>(DeliveryService.assignDeliveryExcel(entCode, updateUserId, gmtTime,  null, null, null), HttpStatus.OK);
		}
		
		
	}	
	 
	@ApiOperation(value = "전자상거래 Delivery 배송지 정보변경", notes="Delivery 배송지 정보를 변경합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/{id}", method = RequestMethod.PUT)
	public ResponseEntity<DeliverySaveResult> updateDelivery(@PathVariable("entCode") String entCode, @RequestBody UpdateDelivery update, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, update, "updateUserId", "logUserName", null, "logUserNameEng");
		update.setRegisterUserId(update.getUpdateUserId());
		return new ResponseEntity<DeliverySaveResult>(DeliveryService.updateDelivery(update, request), HttpStatus.OK);
	}		
	
	@ApiOperation(value = "전자상거래 Delivery 배송취소, 보류", notes="Delivery 배송취소, 보류합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/cancel", method = RequestMethod.PUT)
	public ResponseEntity<DeliverySaveResult> cancelHoldDelivery(@PathVariable("entCode") String entCode, @RequestBody UpdateDelivery cancel, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, cancel, "updateUserId", "logUserName", null, "logUserNameEng");
		cancel.setRegisterUserId(cancel.getUpdateUserId());
		return new ResponseEntity<DeliverySaveResult>(DeliveryService.cancelDelivery(cancel, request), HttpStatus.OK);
	}		

	@ApiOperation(value = "전자상거래 Delivery 보류(Hold)", notes="Delivery 보류(ECM425) 처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/hold", method = RequestMethod.PUT)
	public ResponseEntity<DeliverySaveResult> holdDelivery(@PathVariable("entCode") String entCode, @RequestBody UpdateDelivery hold, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, hold, "updateUserId", "logUserName", null, "logUserNameEng");
		hold.setEntCode(entCode);
		hold.setRegisterUserId(hold.getUpdateUserId());
		return new ResponseEntity<DeliverySaveResult>(DeliveryService.holdDelivery(hold, request), HttpStatus.OK);
	}		
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "전자상거래 Delivery 배송사 배송 요청", notes="Delivery 배송사 배송 요청합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/send/{id}", method = RequestMethod.PUT)
	public ResponseEntity<DeliverySaveResult> sendDelivery(@PathVariable("entCode") String entCode, @RequestBody UpdateDelivery send, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfo(req, jwtTokenProvider, send, "updateUserId", "logUserName", null, "logUserNameEng");
		send.setRegisterUserName(send.getLogUserName());
		send.setRegisterUserId(send.getUpdateUserId());
		DeliverySaveResult result = new DeliverySaveResult(); // 배송요청 결과값
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> deliveryDataMap = new HashMap<String, Object>(); // 각 배송사 전송될 데이터 
		Map<String, Object> interfaceMap = new HashMap<String, Object>(); // 배송사 인터페이스 조회용
		Map<String, Object> interfaceInfoMap = new HashMap<String, Object>(); // 배송사 인터페이스 조회 결과
		Map<String, Object> korexMap = new HashMap<String, Object>(); // 대한통운 배송접수 결과 map 
		Map<String, Object> hanjinMap = new HashMap<String, Object>(); // 한진택배 배송접수 결과 map
		Map<String, Object> lotteMap = new HashMap<String, Object>(); // 롯데택배 배송접수 결과 map
		
		List<Map<String, Object>> deliveryListEms = null;    // 우체국 으로 전송할 데이터
		List<Map<String, Object>> deliveryListKorex = null;  // 대한통운 으로 전송할 데이터
		List<Map<String, Object>> deliveryListHanjin = null; // 한진 으로 전송할 데이터
		List<Map<String, Object>> deliveryListLotte = null; // 롯데 로 전송할 데이터
		List<Map<String, Object>> odJobNoList = new ArrayList<Map<String, Object>>(); // 배송접수 완료된 하우스번호의 odJobNo 리스트
		List<Map<String, Object>> totalErrorList = new ArrayList<Map<String, Object>>(); // 유효성오류 리스트
		
		int totalEmsCnt = 0; // 우체국 전체 카운트
		int totalKorexCnt = 0; // 대한통운 전체 카운트
		int totalHanjinCnt = 0; // 한진택배 전체 카운트
		int totalLotteCnt = 0; // 롯데택배 전체 카운트
		
		interfaceMap.put("entCode", entCode);
		interfaceMap.put("bound", Util.null2str(send.getSrchBoundType()));
		interfaceMap.put("srchBound", Util.null2str(send.getSrchBoundType()));
		interfaceMap.put("srchCustomerCode", send.getSrchCustomerCode());
		//interfaceMap.put("srchCustomerCode", "100001"); // 임시
		
		// 1. 각 배송사 전송될 데이터 조회 후 각 배송사 map에 넣는다.
		deliveryDataMap = DeliveryService.selectSendDeliveryList(interfaceMap, send); // 각 배송사 전송될 데이터 조회
		if(deliveryDataMap.get("errorList") != null){
			result.setErrorList((List)deliveryDataMap.get("errorList"));
//			return new ResponseEntity<DeliverySaveResult>(result, HttpStatus.OK);
		}
		
		deliveryListEms = (List<Map<String, Object>>) deliveryDataMap.get("EMS");
		deliveryListKorex = (List<Map<String, Object>>) deliveryDataMap.get("KOREX"); 
		deliveryListHanjin = (List<Map<String, Object>>) deliveryDataMap.get("HANJIN");
		deliveryListLotte = (List<Map<String, Object>>) deliveryDataMap.get("LOTTE");
		
		// 우체국 배송요청
		if(deliveryListEms.size() > 0) { 
			resultMap.putAll(DeliveryService.sendDeliveryEms(interfaceMap, deliveryListEms));
		}

		
		// 대한통운, 한진택배 배송요청
		if(deliveryListKorex.size() > 0 || deliveryListHanjin.size() > 0) {
			// 대한통운
			if(deliveryListKorex.size() > 0) {
				Util.setDB(req, "KOREX");
				korexMap = DeliveryService.sendDeliveryKorex(deliveryListKorex);
				resultMap.putAll(korexMap);
				odJobNoList.clear();
				odJobNoList.addAll((List<Map<String, Object>>)korexMap.get("successedKorexOdJobNos"));
				
				if(odJobNoList.size() > 0) {
					send.setDeliveryCode("KOREX");
					Util.setDB(req, entCode);
					DeliveryService.updateDeliveryStatus(send,odJobNoList);
				}
			}
			
			// 한진
			if(deliveryListHanjin.size() > 0) {
				Util.setDB(req, "HANJIN");
				hanjinMap = DeliveryService.sendDeliveryHanjin(deliveryListHanjin);
				resultMap.putAll(hanjinMap);
				odJobNoList.clear();
				odJobNoList.addAll((List<Map<String, Object>>)hanjinMap.get("successedHanjinOdJobNos"));
				
				if(odJobNoList.size() > 0) {// successedHanjinOdJobNos이 있을때
					send.setDeliveryCode("HANJIN");
					Util.setDB(req, entCode);
					DeliveryService.updateDeliveryStatus(send,odJobNoList);
				}
			}
			
		}
		
		
		// 롯데택배 배송요청
		if(deliveryListLotte.size() > 0) {
			resultMap.putAll(DeliveryService.sendDeliveryLotte(interfaceMap, deliveryListLotte));
		}
		
//		if(Util.null2str(send.getBlTypeMh()).equals("M")){
//			Util.setDB(req, entCode);
//			DeliveryService.insertMasterPod(send);
//		}

		// 3. 각 배송사별 service에서 처리한 결과값 리턴 
		/*
				D010	정상적인 전송완료
				D020	배송사 인터페이스에 조회 가능 => 해당 배송사 미사용 업체로 요청에 필요한 정보가 등록되어 있지 않음 
				D030	배송요청 중 파일쓰기 에러, INSERT 에러, API 전송 에러 발생
				D031	수하인상세주소 200BYTE 초과
				D032	수하인 한글이름 항목이 존재하지 않음
				D033	수하인우편번호 항목이 존재하지 않음
				D034	수하인 주소가 한글로 되어있지 않음
				D035	수하인 우편번호 문자열 초과
				D060	배송사 인터페이스에 조회 불가 => 배송사 인터페이스 정보가 없음  
		*/
		
		
		// 우체국(EMS)
		result.setSendEmsYn(Util.null2str(resultMap.get("sendEmsYn")));
		result.setSuccessedEmsCnt(Util.null2int(resultMap.get("successedEmsCnt")));
		result.setFailEmsCnt(Util.null2int(resultMap.get("failEmsCnt")));
		result.setFailEmsHblNos(Util.null2str(resultMap.get("failEmsHblNos")));
		result.setFailEmsAddrHblNos(Util.null2str(resultMap.get("failEmsAddrHblNos")));
		result.setFailEmsInterfaceHblNos(Util.null2str(resultMap.get("failEmsInterfaceHblNos")));
		result.setFailEmsTelHblNos(Util.null2str(resultMap.get("failEmsTelHblNos")));
		totalEmsCnt = Util.null2int(resultMap.get("totalEmsCnt"));
		if(((List<Map<String, Object>>) resultMap.get("errorListEms")) != null) {
			totalErrorList.addAll((List<Map<String, Object>>) resultMap.get("errorListEms"));
		}
		// 대한통운(KOREX)
		result.setSendKorexYn(Util.null2str(resultMap.get("sendKorexYn")));
		result.setSuccessedKorexCnt(Util.null2int(resultMap.get("successedKorexCnt")));
		result.setFailKorexCnt(Util.null2int(resultMap.get("failKorexCnt")));
		result.setFailKorexHblNos(Util.null2str(resultMap.get("failKorexHblNos")));
		result.setFailKorexAddrHblNos(Util.null2str(resultMap.get("failKorexAddrHblNos")));
		result.setFailKorexInterfaceHblNos(Util.null2str(resultMap.get("failKorexInterfaceHblNos")));
		totalKorexCnt = Util.null2int(resultMap.get("totalKorexCnt"));
		if(((List<Map<String, Object>>) resultMap.get("errorListKorex")) != null) {
			totalErrorList.addAll((List<Map<String, Object>>) resultMap.get("errorListKorex"));
		}
		
		// 한진택배(HANJIN)
		result.setSendHanjinYn(Util.null2str(resultMap.get("sendHanjinYn")));
		result.setSuccessedHanjinCnt(Util.null2int(resultMap.get("successedHanjinCnt")));
		result.setFailHanjinCnt(Util.null2int(resultMap.get("failHanjinCnt")));
		result.setFailHanjinHblNos(Util.null2str(resultMap.get("failHanjinHblNos")));
		result.setFailHanjinAddrHblNos(Util.null2str(resultMap.get("failHanjinAddrHblNos")));
		result.setFailHanjinInterfaceHblNos(Util.null2str(resultMap.get("failHanjinInterfaceHblNos")));
		result.setFailHanjinNameHblNos(Util.null2str(resultMap.get("failHanjinNameHblNos")));
		result.setFailHanjinZipcodeHblNos(Util.null2str(resultMap.get("failHanjinZipcodeHblNos")));
		totalHanjinCnt = Util.null2int(resultMap.get("totalHanjinCnt"));
		if(((List<Map<String, Object>>) resultMap.get("errorListHanjin")) != null) {
			totalErrorList.addAll((List<Map<String, Object>>) resultMap.get("errorListHanjin"));
		}
		
		// 롯데택배(LOTTE)
		result.setSendLotteYn(Util.null2str(resultMap.get("sendLotteYn")));
		result.setSuccessedLotteCnt(Util.null2int(resultMap.get("successedLotteCnt")));
		result.setFailLotteCnt(Util.null2int(resultMap.get("failLotteCnt")));
		result.setFailLotteHblNos(Util.null2str(resultMap.get("failLotteHblNos")));
		result.setFailLotteAddrHblNos(Util.null2str(resultMap.get("failLotteAddrHblNos")));
		result.setFailLotteInterfaceHblNos(Util.null2str(resultMap.get("failLotteInterfaceHblNos")));
		result.setFailLotteManHblNos(Util.null2str(resultMap.get("failLotteManHblNos")));
		totalLotteCnt = Util.null2int(resultMap.get("totalLotteCnt"));
		if(((List<Map<String, Object>>) resultMap.get("errorListLotte")) != null) {
			totalErrorList.addAll((List<Map<String, Object>>) resultMap.get("errorListLotte"));
		}
		
		
		
		// 배송요청 각 배송사 합산 토탈 카운트
		result.setTotalCnt(totalEmsCnt+totalKorexCnt+totalHanjinCnt+totalLotteCnt);
		result.setTotalErrorList(totalErrorList);

		
		return new ResponseEntity<DeliverySaveResult>(result, HttpStatus.OK);
	}		
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "기본 엑셀 list 조회", notes="기본 엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/item/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectBrokerHouseExcel(@PathVariable("entCode") String entCode,@QueryStringArgResolver SearchDeliveryMList search,HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		DeliveryHList orderList = DeliveryService.selectDeliveryHList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getDeliveryHList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Delivery_List", map), HttpStatus.OK);
	}
	
	

}
