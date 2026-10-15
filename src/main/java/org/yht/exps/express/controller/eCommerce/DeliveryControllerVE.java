package org.yht.exps.express.controller.eCommerce;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.mapper.consumerService.OrderCommonCsMapper;
import org.yht.exps.express.mapper.eCommerce.DeliveryMapper;
import org.yht.exps.express.mapper.systemMng.SystemMngMapper;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryHList;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryMList;
import org.yht.exps.express.model.eCommerce.delivery.DeliverySaveResult;
import org.yht.exps.express.model.eCommerce.delivery.UpdateDelivery;
import org.yht.exps.express.model.eCommerce.delivery.search.SearchDeliveryMList;
import org.yht.exps.express.model.eCommerce.order.OrderMulti;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.eCommerce.DeliveryService;
import org.yht.exps.express.service.eCommerce.DeliveryServiceVE;
import org.yht.exps.express.service.eCommerce.OrderService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
@Slf4j
@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/delivery/ve")
public class DeliveryControllerVE {

	@Resource(name = "deliveryServiceVE")
	DeliveryServiceVE DeliveryServiceVe;
	@Resource(name = "deliveryService")
	DeliveryService DeliveryService;
	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name = "fileService")
	FileService fileService;

	@Value("${spring.profiles.active}")
	private String profile;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	SystemMngMapper systemMngMapper;
	@Autowired
	DeliveryMapper deliveryMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	OrderCommonCsMapper orderCommonCsMapper;
	@Autowired
	OrderService orderService;


	//기본세팅 구분용(현재는 허브넷구분용)
	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public ResponseEntity<String> getProfile() {
		return ResponseEntity.ok(profile);
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

		log.debug("sendChk ->"+ send);
		//changeDelivery로 올경우 송장교체에서 온것이므로 분기처리가 필요함
		if(Util.null2str(send.getChkPage()).equals("changeDelivery")){
			List<Map<String, Object>> multiOdList = new ArrayList<>();
			//1.변경	배송사를 체크하기
			for(int i = 0;i<send.getDataList().size();i++){
				Map<String, Object> multiStockMap = new HashMap<String, Object>();
				//변경배송사가 대한통운일경우
				if(Util.null2str(send.getDataList().get(i).get("deliveryCodeChanged")).equals("KOREX")){
						//1-1 자동으로 스탁을생성
						multiStockMap.put("gmtTime", 0);
						multiStockMap.put("seqNoId", Util.null2str(send.getDataList().get(i).get("seqNoId")));
						multiStockMap.put("updateUserId", Util.null2str(send.getDataList().get(i).get("updateUserId")));
						multiStockMap.put("createStockNoCnt", "1");
						multiStockMap.put("bound", Util.null2str(send.getDataList().get(i).get("bound")));
						multiStockMap.put("boundType", Util.null2str(send.getDataList().get(i).get("boundType")));
						multiStockMap.put("odJobNo", Util.null2str(send.getDataList().get(i).get("odJobNo")));
						multiStockMap.put("PIECES", "1");
						multiStockMap.put("DELIVERY_CODE", send.getDataList().get(i).get("deliveryCodeChanged"));
						multiStockMap.put("profileName", profile);
						multiStockMap.put("ENT_CODE", entCode);
						multiStockMap.put("CUST_CODE_LV1", send.getDataList().get(i).get("custCodeLv1"));
						multiStockMap.put("CUST_CODE_LV2", send.getDataList().get(i).get("custCodeLv2"));
						multiStockMap.put("CUST_CODE_LV3", send.getDataList().get(i).get("custCodeLv3"));
						orderService.setMultiStock(multiStockMap, Util.null2int(send.getDataList().get(i).get("stockCustLv")), 1, false, false);
						if(multiStockMap.get("multiList") != null) {
							List<OrderMulti> multiList = (List<OrderMulti>) multiStockMap.get("multiList");
							multiStockMap.put("stockMutliNo", multiList.get(0).getDeliveryNo());
							log.debug("stockNoChk->"+  multiList.get(0).getDeliveryNo());
							//생성한 스탁번호를 멀티의 변경송장번호에 저장
							deliveryMapper.updateMulDate(multiStockMap);
						}
				}
				// multiOdList 구성
				Map<String, Object> multiOd = new HashMap<>();
				multiOd.put("odJobNo", send.getDataList().get(i).get("odJobNo"));
				multiOd.put("seqNoId", send.getDataList().get(i).get("seqNoId"));
				multiOdList.add(multiOd);
				//변경건에 대한 기록 삭제하는 로직 추가(경동,대한통운 모두 적용하는부분)
				multiStockMap.put("checkDelNo", Util.null2str(deliveryMapper.selectCheckNo(multiStockMap)));
				deliveryMapper.deleteChkPod(multiStockMap);
			}
			send.setMultiOdList(multiOdList.size() > 0 ? multiOdList : null);
		}else if(Util.null2str(send.getChkPage()).equals("gwDelivery")){
			//gwDelivery로 올경우 중량화물에서 온것이므로 분기처리가 필요함
			//멀티건 처리로직
			for(int i = 0;i<send.getDataList().size();i++){
				//seqNoId가 존재하며 0보다 크면 멀티가 있는건이므로 multiOdJobNo에 값을 넣어줌 -> 이후로직에서 multiOdJobNo가 있으므로 멀티조회쿼리조건으로 넘김
				Map<String, Object> multiOd = new HashMap<>();
				if(send.getDataList().get(i).get("seqNoId")!=null && Util.null2int(send.getDataList().get(i).get("seqNoId"))>0){
					send.getDataList().get(i).put("multiOdJobNo","1");
				}
			}
		}


		String korexMethod = "";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> deliveryDataMap = new HashMap<String, Object>(); // 각 배송사 전송될 데이터
		Map<String, Object> interfaceMap = new HashMap<String, Object>(); // 배송사 인터페이스 조회용
		Map<String, Object> interfaceInfoMap = new HashMap<String, Object>(); // 배송사 인터페이스 조회 결과
		Map<String, Object> korexMap = new HashMap<String, Object>(); // 대한통운 배송접수 결과 map
		Map<String, Object> hanjinMap = new HashMap<String, Object>(); // 한진택배 배송접수 결과 map
		Map<String, Object> lotteMap = new HashMap<String, Object>(); // 롯데택배 배송접수 결과 map
		Map<String, Object> kdMap = new HashMap<String, Object>(); // 경동택배 배송접수 결과 map

		List<Map<String, Object>> deliveryListEms = null;    // 우체국 으로 전송할 데이터
		List<Map<String, Object>> deliveryListKorex = null;  // 대한통운 으로 전송할 데이터
		List<Map<String, Object>> deliveryListHanjin = null; // 한진 으로 전송할 데이터
		List<Map<String, Object>> deliveryListLotte = null; // 롯데 로 전송할 데이터
		List<Map<String, Object>> deliveryListKd = null;    // 경동으로 전송할 데이터

		List<Map<String, Object>> odJobNoList = new ArrayList<Map<String, Object>>(); // 배송접수 완료된 하우스번호의 odJobNo 리스트
		List<Map<String, Object>> odJobNoListMulti = new ArrayList<Map<String, Object>>(); // 배송접수 완료된 하우스번호의 odJobNo 리스트(멀티전용)
		List<Map<String, Object>> totalErrorList = new ArrayList<Map<String, Object>>(); // 유효성오류 리스트

		int totalEmsCnt = 0; // 우체국 전체 카운트
		int totalKorexCnt = 0; // 대한통운 전체 카운트
		int totalHanjinCnt = 0; // 한진택배 전체 카운트
		int totalLotteCnt = 0; // 롯데택배 전체 카운트
		int totalKdCnt = 0; // 경동택배 전체 카운트

		interfaceMap.put("entCode", entCode);
		interfaceMap.put("bound", Util.null2str(send.getSrchBoundType()));
		interfaceMap.put("srchBound", Util.null2str(send.getSrchBoundType()));
		interfaceMap.put("srchCustomerCode", send.getSrchCustomerCode());
		//interfaceMap.put("srchCustomerCode", "100001"); // 임시

		// 1. 각 배송사 전송될 데이터 조회 후 각 배송사 map에 넣는다.
		deliveryDataMap = DeliveryServiceVe.selectSendDeliveryList(interfaceMap, send,"0"); // 각 배송사 전송될 데이터 조회
		List<Map<String, Object>> korexList = (List<Map<String, Object>>) deliveryDataMap.get("KOREX");


		if(!korexList.isEmpty()){
			Map<String, Object> deliveryInterfaceInfo = (Map<String, Object>) korexList.get(0).get("deliveryInterfaceInfo");
			korexMethod = (String) deliveryInterfaceInfo.get("KOREX_METHOD");
		}

		if(deliveryDataMap.get("errorList") != null){
			result.setErrorList((List)deliveryDataMap.get("errorList"));
//			return new ResponseEntity<DeliverySaveResult>(result, HttpStatus.OK);
		}


		deliveryListEms = (List<Map<String, Object>>) deliveryDataMap.get("EMS");
		deliveryListKorex = (List<Map<String, Object>>) deliveryDataMap.get("KOREX");
		deliveryListHanjin = (List<Map<String, Object>>) deliveryDataMap.get("HANJIN");
		deliveryListLotte = (List<Map<String, Object>>) deliveryDataMap.get("LOTTE");
		deliveryListKd = (List<Map<String, Object>>) deliveryDataMap.get("KD");

		// 우체국 배송요청
		if(deliveryListEms.size() > 0) {
			resultMap.putAll(DeliveryService.sendDeliveryEms(interfaceMap, deliveryListEms));
		}

		// 경동택배 배송요청
//		if(deliveryListKd.size() > 0) {
//			resultMap.putAll(DeliveryServiceVe.sendDeliveryKd(deliveryListKd,entCode));
//		}
		if (deliveryListKd.size() > 0) {
			Map<String, Object> kdMapChk = DeliveryServiceVe.sendDeliveryKd(deliveryListKd, entCode);
			resultMap.putAll(kdMapChk);

			// 일반 오더 상태 반영
			List<Map<String, Object>> kdOdJobNoList =
					(List<Map<String, Object>>) kdMapChk.get("successedKdOdJobNos");
			if (kdOdJobNoList != null && kdOdJobNoList.size() > 0) {
				send.setDeliveryCode("KD");
				Util.setDB(req, entCode);
				DeliveryServiceVe.updateDeliveryStatus(send, kdOdJobNoList);
			}

			// 멀티 오더 상태 반영
			List<Map<String, Object>> kdOdJobNoListMulti =
					(List<Map<String, Object>>) kdMapChk.get("successedKdOdJobNosMulti");
			if (kdOdJobNoListMulti != null && kdOdJobNoListMulti.size() > 0) {
				send.setDeliveryCode("KD");
				Util.setDB(req, entCode);
				DeliveryServiceVe.updateDeliveryStatusMulti(send, kdOdJobNoListMulti);
			}
		}


		//대한통운의경우 db방식인지아닌지 먼저 분기처리해야함
		// 대한통운, 한진택배 배송요청
		if(deliveryListKorex.size() > 0 || deliveryListHanjin.size() > 0) {
			// 대한통운
			if(deliveryListKorex.size() > 0) {

				//systemMngMapper.selectDeliveryInterface();
				if(!korexMethod.equals("REST")) {
					//DB방식
					Util.setDB(req, "KOREX");
					korexMap = DeliveryService.sendDeliveryKorex(deliveryListKorex);
				}else {
					//REST방식
					korexMap = DeliveryServiceVe.sendDeliveryKorexRest(deliveryListKorex,false);
				}

				//일반오더건
				resultMap.putAll(korexMap);
				odJobNoList.clear();
				odJobNoList.addAll((List<Map<String, Object>>)korexMap.get("successedKorexOdJobNos"));

				if(odJobNoList != null && odJobNoList.size() > 0) {
					send.setDeliveryCode("KOREX");
					Util.setDB(req, entCode);
					DeliveryServiceVe.updateDeliveryStatus(send,odJobNoList);
				}

				//멀티건
				odJobNoListMulti.clear();
				odJobNoListMulti.addAll((List<Map<String, Object>>)korexMap.get("successedKorexOdJobNosMulti"));
				if(odJobNoListMulti != null && odJobNoListMulti.size() > 0) {
					send.setDeliveryCode("KOREX");
					Util.setDB(req, entCode);
					DeliveryServiceVe.updateDeliveryStatusMulti(send,odJobNoListMulti);
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

		// 경동택배(KD)(경동은 hbl_no가 아닌 po_no기준으로 처리)
		result.setSendKdYn(Util.null2str(resultMap.get("sendKdYn")));
		result.setSuccessedKdCnt(Util.null2int(resultMap.get("successedKdCnt")));
		result.setFailKdCnt(Util.null2int(resultMap.get("failKdCnt")));
		result.setFailKdHblNos(Util.null2str(resultMap.get("failKdPoNos")));
		result.setFailKdAddrHblNos(Util.null2str(resultMap.get("failKdAddrPoNos")));
		result.setFailKdInterfaceHblNos(Util.null2str(resultMap.get("failKdInterfacePoNos")));
		result.setFailKdManHblNos(Util.null2str(resultMap.get("failKdManPoNos")));
		totalKdCnt = Util.null2int(resultMap.get("totalKdCnt"));
		if(((List<Map<String, Object>>) resultMap.get("errorListKd")) != null) {
			totalErrorList.addAll((List<Map<String, Object>>) resultMap.get("errorListKd"));
		}

		// 배송요청 각 배송사 합산 토탈 카운트
		result.setTotalCnt(totalEmsCnt+totalKorexCnt+totalHanjinCnt+totalLotteCnt+totalKdCnt);
		log.debug("totalErrorList->" + totalErrorList);
		result.setTotalErrorList(totalErrorList);


		return new ResponseEntity<DeliverySaveResult>(result, HttpStatus.OK);
	}


//	@ApiOperation(value = "전자상거래 Delivery 반품(대한통운)", notes="Delivery 반품합니다.")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
//	})
//	@RequestMapping(value="/cancelkorex/{id}", method = RequestMethod.PUT)
//	public ResponseEntity<DeliverySaveResult> cancelKorexDelivery(@PathVariable("entCode") String entCode, @RequestBody UpdateDelivery cancel, HttpServletRequest request) throws Exception {
//		Util.getSessionUserInfo(request, jwtTokenProvider, cancel, "updateUserId", "logUserName", null, "logUserNameEng");
//		cancel.setRegisterUserId(cancel.getUpdateUserId());
//		cancel.setRegisterUserName(cancel.getLogUserName());
//		cancel.setRegisterUserId(cancel.getUpdateUserId());
//		Map<String, Object> resultMap = new HashMap<String, Object>();
//		Map<String, Object> deliveryDataMap = new HashMap<String, Object>();
//		Map<String, Object> interfaceMap = new HashMap<String, Object>();
//		DeliverySaveResult result = new DeliverySaveResult();
//		Map<String, Object> korexMap = new HashMap<String, Object>();
//
//		List<Map<String, Object>> deliveryListKorex = null;
//		List<Map<String, Object>> odJobNoList = new ArrayList<Map<String, Object>>();
//		List<Map<String, Object>> odJobNoListMulti = new ArrayList<Map<String, Object>>(); //멀티전용
//		List<Map<String, Object>> totalErrorList = new ArrayList<Map<String, Object>>();
//
//		int totalKorexCnt = 0;
//		interfaceMap.put("entCode", cancel.getEntCode());
//		interfaceMap.put("bound", Util.null2str(cancel.getSrchBoundType()));
//		interfaceMap.put("srchBound", Util.null2str(cancel.getSrchBoundType()));
//		interfaceMap.put("srchCustomerCode", cancel.getSrchCustomerCode());
//
//		// 각 배송사 전송될 데이터 조회
//		deliveryDataMap = DeliveryServiceVe.selectSendDeliveryList(interfaceMap, cancel,"0");
//		String korexMethod = "";
//		List<Map<String, Object>> korexList = (List<Map<String, Object>>) deliveryDataMap.get("KOREX");
//		if(!korexList.isEmpty()){
//			Map<String, Object> deliveryInterfaceInfo = (Map<String, Object>) korexList.get(0).get("deliveryInterfaceInfo");
//			korexMethod = (String) deliveryInterfaceInfo.get("KOREX_METHOD");
//		}
//		if (deliveryDataMap.get("errorList") != null) {
//			result.setErrorList((List) deliveryDataMap.get("errorList"));
//		}
//		deliveryListKorex = (List<Map<String, Object>>) deliveryDataMap.get("KOREX");
//
//		//  대한통운 반품
//		if (deliveryListKorex.size() > 0) {
//			korexMap = DeliveryServiceVe.sendDeliveryKorexReturn(deliveryListKorex,entCode);
//			resultMap.putAll(korexMap);
//
//			odJobNoList.clear();
//			odJobNoList.addAll((List<Map<String, Object>>) korexMap.get("successedKorexOdJobNos"));
//			//  반품 성공건 POD 상태 업데이트(오더건)
//			if (odJobNoList.size() > 0) {
//				cancel.setDeliveryCode("KOREX");
//				Util.setDB(request, cancel.getEntCode());
//				Map<String, Object> tmpMap = new HashMap<String, Object>();
//				SqlSession sqlSession = null;
//				try {
//					sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
//					tmpMap.put("entCode",         cancel.getEntCode());
//					tmpMap.put("gmtTime",         cancel.getGmtTime());
//					tmpMap.put("registerUserId",  cancel.getRegisterUserId());
//					tmpMap.put("updateUserId",    cancel.getRegisterUserId());
//					tmpMap.put("entBusinessCode", "");
//					tmpMap.put("solution",        "ECM");
//					tmpMap.put("stockType",       "POD");
//					tmpMap.put("stockSign",       "POD");
//					tmpMap.put("stockSeqLength",  "8");
//					tmpMap.put("resultCode",      "FIXED_SIGN_AND_SEQ_NO");
//					tmpMap.put("stockRemark",     "ECM POD JOB NO");
//					tmpMap.put("podStatus",       "ECM504");  //  반품요청
//					tmpMap.put("deliveryCode",    "Korex");
//					tmpMap.put("podTableName",    "TB_ECM_POD_04");
//					Util.accessControlDatabase(tmpMap, "podTableName|gmtTime", false, null, null, null, null);
//					DeliveryServiceVe.batchProcessOrderPod(sqlSession, tmpMap, odJobNoList, "ECM504", String.valueOf(cancel.getGmtTime()));
//					sqlSession.flushStatements();
//					sqlSession.clearCache();
//					sqlSession.commit();
//				} catch (Exception e) {
//					throw e;
//				} finally {
//					sqlSession.close();
//				}
//
//			}
//			//멀티건
//			odJobNoListMulti.clear();
//			odJobNoListMulti.addAll((List<Map<String, Object>>) korexMap.get("successedKorexOdJobNosMulti"));
//			if (odJobNoListMulti.size() > 0) {
//				cancel.setDeliveryCode("KOREX");
//				Util.setDB(request, cancel.getEntCode());
//				Map<String, Object> tmpMap = new HashMap<String, Object>();
//				SqlSession sqlSession = null;
//				try {
//					sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
//					tmpMap.put("entCode",         cancel.getEntCode());
//					tmpMap.put("gmtTime",         cancel.getGmtTime());
//					tmpMap.put("registerUserId",  cancel.getRegisterUserId());
//					tmpMap.put("updateUserId",    cancel.getRegisterUserId());
//					tmpMap.put("entBusinessCode", "");
//					tmpMap.put("solution",        "ECM");
//					tmpMap.put("stockType",       "POD");
//					tmpMap.put("stockSign",       "POD");
//					tmpMap.put("stockSeqLength",  "8");
//					tmpMap.put("resultCode",      "FIXED_SIGN_AND_SEQ_NO");
//					tmpMap.put("stockRemark",     "ECM POD JOB NO");
//					tmpMap.put("podStatus",       "ECM504");  //  반품요청
//					tmpMap.put("deliveryCode",    "Korex");
//					tmpMap.put("podTableName",    "TB_ECM_POD_04_M");
//					Util.accessControlDatabase(tmpMap, "podTableName|gmtTime", false, null, null, null, null);
//					DeliveryServiceVe.batchProcessOrderPod(sqlSession, tmpMap, odJobNoList, "ECM504", String.valueOf(cancel.getGmtTime()));
//					sqlSession.flushStatements();
//					sqlSession.clearCache();
//					sqlSession.commit();
//				} catch (Exception e) {
//					throw e;
//				} finally {
//					sqlSession.close();
//				}
//
//			}
//
//		}
//
//		//  결과 세팅
//		result.setSendKorexYn(Util.null2str(resultMap.get("sendKorexYn")));
//		result.setSuccessedKorexCnt(Util.null2int(resultMap.get("successedKorexCnt")));
//		result.setFailKorexCnt(Util.null2int(resultMap.get("failKorexCnt")));
//		result.setFailKorexHblNos(Util.null2str(resultMap.get("failKorexHblNos")));
//		result.setFailKorexAddrHblNos(Util.null2str(resultMap.get("failKorexAddrHblNos")));
//		result.setFailKorexInterfaceHblNos(Util.null2str(resultMap.get("failKorexInterfaceHblNos")));
//		totalKorexCnt = Util.null2int(resultMap.get("totalKorexCnt"));
//		if (((List<Map<String, Object>>) resultMap.get("errorListKorex")) != null) {
//			totalErrorList.addAll((List<Map<String, Object>>) resultMap.get("errorListKorex"));
//		}
//		result.setTotalCnt(totalKorexCnt);
//		result.setTotalErrorList(totalErrorList);
//		return new ResponseEntity<DeliverySaveResult>(result, HttpStatus.OK);
//	}

	@ApiOperation(value = "전자상거래 Delivery 배송사 반품 요청", notes="Delivery 배송사 배송 요청합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/send/return/{id}", method = RequestMethod.PUT)
	public ResponseEntity<DeliverySaveResult> sendDeliveryReturn(@PathVariable("entCode") String entCode, @RequestBody UpdateDelivery send, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfo(req, jwtTokenProvider, send, "updateUserId", "logUserName", null, "logUserNameEng");
		send.setRegisterUserName(send.getLogUserName());
		send.setRegisterUserId(send.getUpdateUserId());
		send.setSrchReturnYn("Y");//반품건 배송접수유효성 안타게 하는 플래그
		send.setReturnDeliveryCodeChk("Y");//반품관리에서 보낸 건은 반품배송사코드를 바라보게하기위한 플래그
		DeliverySaveResult result = new DeliverySaveResult(); // 배송요청 결과값


		//멀티건 처리로직
		List<Map<String, Object>> multiOdList = new ArrayList<>();
		for(int i = 0;i<send.getDataList().size();i++){
			//seqNoId가 존재하며 0보다 크면 멀티가 있는건이므로 multiOdJobNo에 값을 넣어줌 -> 이후로직에서 multiOdJobNo가 있으므로 멀티조회쿼리조건으로 넘김
			Map<String, Object> multiOd = new HashMap<>();
			if(send.getDataList().get(i).get("seqNoId")!=null && Util.null2int(send.getDataList().get(i).get("seqNoId"))>0){
				send.getDataList().get(i).put("multiOdJobNo","1");
			}
		}

		String korexMethod = "";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> deliveryDataMap = new HashMap<String, Object>(); // 각 배송사 전송될 데이터
		Map<String, Object> interfaceMap = new HashMap<String, Object>(); // 배송사 인터페이스 조회용
		Map<String, Object> interfaceInfoMap = new HashMap<String, Object>(); // 배송사 인터페이스 조회 결과
		Map<String, Object> korexMap = new HashMap<String, Object>(); // 대한통운 배송접수 결과 map
		Map<String, Object> hanjinMap = new HashMap<String, Object>(); // 한진택배 배송접수 결과 map
		Map<String, Object> lotteMap = new HashMap<String, Object>(); // 롯데택배 배송접수 결과 map
		Map<String, Object> kdMap = new HashMap<String, Object>(); // 경동택배 배송접수 결과 map

		List<Map<String, Object>> deliveryListEms = null;    // 우체국 으로 전송할 데이터
		List<Map<String, Object>> deliveryListKorex = null;  // 대한통운 으로 전송할 데이터
		List<Map<String, Object>> deliveryListHanjin = null; // 한진 으로 전송할 데이터
		List<Map<String, Object>> deliveryListLotte = null; // 롯데 로 전송할 데이터
		List<Map<String, Object>> deliveryListKd = null;    // 경동으로 전송할 데이터

		List<Map<String, Object>> odJobNoList = new ArrayList<Map<String, Object>>(); // 배송접수 완료된 하우스번호의 odJobNo 리스트
		List<Map<String, Object>> odJobNoListMulti = new ArrayList<Map<String, Object>>(); // 배송접수 완료된 하우스번호의 odJobNo 리스트(멀티전용)
		List<Map<String, Object>> totalErrorList = new ArrayList<Map<String, Object>>(); // 유효성오류 리스트

		int totalEmsCnt = 0; // 우체국 전체 카운트
		int totalKorexCnt = 0; // 대한통운 전체 카운트
		int totalHanjinCnt = 0; // 한진택배 전체 카운트
		int totalLotteCnt = 0; // 롯데택배 전체 카운트
		int totalKdCnt = 0; // 경동택배 전체 카운트

		interfaceMap.put("entCode", entCode);
		interfaceMap.put("bound", Util.null2str(send.getSrchBoundType()));
		interfaceMap.put("srchBound", Util.null2str(send.getSrchBoundType()));
		interfaceMap.put("srchCustomerCode", send.getSrchCustomerCode());
		//interfaceMap.put("srchCustomerCode", "100001"); // 임시

		// 1. 각 배송사 전송될 데이터 조회 후 각 배송사 map에 넣는다.
		deliveryDataMap = DeliveryServiceVe.selectSendDeliveryList(interfaceMap, send,"0"); // 각 배송사 전송될 데이터 조회
		List<Map<String, Object>> korexList = (List<Map<String, Object>>) deliveryDataMap.get("KOREX");


		if(!korexList.isEmpty()){
			Map<String, Object> deliveryInterfaceInfo = (Map<String, Object>) korexList.get(0).get("deliveryInterfaceInfo");
			korexMethod = (String) deliveryInterfaceInfo.get("KOREX_METHOD");
		}

		if(deliveryDataMap.get("errorList") != null){
			result.setErrorList((List)deliveryDataMap.get("errorList"));
//			return new ResponseEntity<DeliverySaveResult>(result, HttpStatus.OK);
		}


		deliveryListEms = (List<Map<String, Object>>) deliveryDataMap.get("EMS");
		//1.반품배송코드 우체국건은 송장번호를 변경해서 전송(앞자리7로 변경)
		for (int i = 0; i < deliveryListEms.size(); i++) {
			//변경한송장번호를 기존맵에 덮어씌우기(이후 반품송장번호에 업데이트 됨)
			Map<String, Object> row = deliveryListEms.get(i);
			String hblNo = String.valueOf(row.get("HBL_NO"));
			String deliveryNo = String.valueOf(row.get("DELIVERY_NO"));
			if (hblNo != null && !hblNo.isEmpty() && !"null".equals(hblNo)) {
				row.put("HBL_NO", "7" + hblNo.substring(1));
			}
			if (deliveryNo != null && !deliveryNo.isEmpty() && !"null".equals(deliveryNo)) {
				row.put("DELIVERY_NO", "7" + deliveryNo.substring(1));
			}
		}



		deliveryListKorex = (List<Map<String, Object>>) deliveryDataMap.get("KOREX");
		deliveryListHanjin = (List<Map<String, Object>>) deliveryDataMap.get("HANJIN");
		deliveryListLotte = (List<Map<String, Object>>) deliveryDataMap.get("LOTTE");
		deliveryListKd = (List<Map<String, Object>>) deliveryDataMap.get("KD");

		// 우체국 반품요청
		if(deliveryListEms.size() > 0) {
			resultMap.putAll(DeliveryService.sendDeliveryEmsReturn(interfaceMap, deliveryListEms));
		}

		// 경동택배 반품요청
//		if(deliveryListKd.size() > 0) {
//			resultMap.putAll(DeliveryServiceVe.sendDeliveryKd(deliveryListKd,entCode));
//		}
		if (deliveryListKd.size() > 0) {
			Map<String, Object> kdMapChk = DeliveryServiceVe.sendDeliveryKd(deliveryListKd, entCode);
			resultMap.putAll(kdMapChk);
			// 일반 오더 상태 반영
			List<Map<String, Object>> kdOdJobNoList =
					(List<Map<String, Object>>) kdMapChk.get("successedKdOdJobNos");
			if (kdOdJobNoList != null && kdOdJobNoList.size() > 0) {
				send.setDeliveryCode("KD");
				Util.setDB(req, entCode);
				DeliveryServiceVe.updateDeliveryStatus(send, kdOdJobNoList);
			}

			// 멀티 오더 상태 반영
			List<Map<String, Object>> kdOdJobNoListMulti =
					(List<Map<String, Object>>) kdMapChk.get("successedKdOdJobNosMulti");
			if (kdOdJobNoListMulti != null && kdOdJobNoListMulti.size() > 0) {
				send.setDeliveryCode("KD");
				Util.setDB(req, entCode);
				DeliveryServiceVe.updateDeliveryStatusMulti(send, kdOdJobNoListMulti);
			}
		}


		//대한통운의경우 db방식인지아닌지 먼저 분기처리해야함
		// 대한통운, 한진택배 반품요청
		if(deliveryListKorex.size() > 0 || deliveryListHanjin.size() > 0) {
			// 대한통운
			if(deliveryListKorex.size() > 0) {

				//systemMngMapper.selectDeliveryInterface();
				if(!korexMethod.equals("REST")) {
					//DB방식
					Util.setDB(req, "KOREX");
					korexMap = DeliveryService.sendDeliveryKorex(deliveryListKorex);
				}else {
					//REST방식(true는 반품접수)
					korexMap = DeliveryServiceVe.sendDeliveryKorexRest(deliveryListKorex,true);
				}

				//일반오더건
				resultMap.putAll(korexMap);
				odJobNoList.clear();
				odJobNoList.addAll((List<Map<String, Object>>)korexMap.get("successedKorexOdJobNos"));

				if(odJobNoList != null && odJobNoList.size() > 0) {
					send.setDeliveryCode("KOREX");
					Util.setDB(req, entCode);
					DeliveryServiceVe.updateDeliveryStatus(send,odJobNoList);
				}

				//멀티건
				odJobNoListMulti.clear();
				odJobNoListMulti.addAll((List<Map<String, Object>>)korexMap.get("successedKorexOdJobNosMulti"));
				if(odJobNoListMulti != null && odJobNoListMulti.size() > 0) {
					send.setDeliveryCode("KOREX");
					Util.setDB(req, entCode);
					DeliveryServiceVe.updateDeliveryStatusMulti(send,odJobNoListMulti);
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

		// 롯데택배 반품요청
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

		// 경동택배(KD)(경동은 hbl_no가 아닌 po_no기준으로 처리)
		result.setSendKdYn(Util.null2str(resultMap.get("sendKdYn")));
		result.setSuccessedKdCnt(Util.null2int(resultMap.get("successedKdCnt")));
		result.setFailKdCnt(Util.null2int(resultMap.get("failKdCnt")));
		result.setFailKdHblNos(Util.null2str(resultMap.get("failKdPoNos")));
		result.setFailKdAddrHblNos(Util.null2str(resultMap.get("failKdAddrPoNos")));
		result.setFailKdInterfaceHblNos(Util.null2str(resultMap.get("failKdInterfacePoNos")));
		result.setFailKdManHblNos(Util.null2str(resultMap.get("failKdManPoNos")));
		totalKdCnt = Util.null2int(resultMap.get("totalKdCnt"));
		if(((List<Map<String, Object>>) resultMap.get("errorListKd")) != null) {
			totalErrorList.addAll((List<Map<String, Object>>) resultMap.get("errorListKd"));
		}

		// 배송요청 각 배송사 합산 토탈 카운트
		result.setTotalCnt(totalEmsCnt+totalKorexCnt+totalHanjinCnt+totalLotteCnt+totalKdCnt);
		log.debug("totalErrorList->" + totalErrorList);
		result.setTotalErrorList(totalErrorList);


		return new ResponseEntity<DeliverySaveResult>(result, HttpStatus.OK);
	}

	@ApiOperation(value = "전자상거래 Delivery 배송사 일괄변경", notes="Delivery 배송사 배송 요청합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/change/{id}", method = RequestMethod.PUT)
	public ResponseEntity<DeliverySaveResult> changeDelivery(@PathVariable("entCode") String entCode, @RequestBody UpdateDelivery send, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfo(req, jwtTokenProvider, send, "updateUserId", "logUserName", null, "logUserNameEng");
		send.setRegisterUserName(send.getLogUserName());
		send.setRegisterUserId(send.getUpdateUserId());
		DeliverySaveResult result = new DeliverySaveResult();
		DeliveryServiceVe.changeDelivery(send);
		return new ResponseEntity<DeliverySaveResult>(result, HttpStatus.OK);
	}


	@ApiOperation(value = "전자상거래 Delivery 반품번호 변경", notes="Delivery 반품번호를 변경합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/changereturn/{odJobNo}/{returnRequestId}/{returnDeliveryCode}/{multiDeliveryNo}/{seqNoId}", method = RequestMethod.POST)
	public ResponseEntity<DeliverySaveResult> changeReturnNo(@PathVariable("entCode") String entCode,
															 @PathVariable("odJobNo") String odJobNo,
															 @PathVariable("returnRequestId") String returnRequestId,
															 @PathVariable("returnDeliveryCode") String returnDeliveryCode,
															 @PathVariable("multiDeliveryNo") String multiDeliveryNo,
															 @PathVariable("seqNoId") String seqNoId,
															 HttpServletRequest req) throws Exception {
		log.debug("odJobNo->"+ odJobNo+ "returnRequestId->"+returnRequestId+ "returnCarrierCd->"+ returnDeliveryCode);
		Map<String,Object>saveMap =new HashMap<>();
		saveMap.put("odJobNo",Util.null2str(odJobNo));
		saveMap.put("returnRequestId",Util.null2str(returnRequestId));
		saveMap.put("returnDeliveryCode",Util.null2str(returnDeliveryCode));
		saveMap.put("multiDeliveryNo",Util.null2str(multiDeliveryNo));
		saveMap.put("seqNoId",Util.null2str(seqNoId));
		saveMap.put("entCode",Util.null2str(entCode));
		DeliverySaveResult result = DeliveryServiceVe.changeReturnNo(saveMap);
		return new ResponseEntity<DeliverySaveResult>(result, HttpStatus.OK);
	}


}