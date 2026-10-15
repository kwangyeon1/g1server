package org.yht.exps.express.controller.api;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.text.StringEscapeUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.yht.constant.BranchEnum;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.model.api.AirInterface;
import org.yht.exps.express.model.api.CustomsInterface;
import org.yht.exps.express.model.api.EcmRequestBLnterface;
import org.yht.exps.express.model.api.EcmResult;
import org.yht.exps.express.model.api.EcmResult.EcmReturn;
import org.yht.exps.express.model.api.EmsInterface;
import org.yht.exps.express.model.api.EmsRequestBLnterface;
import org.yht.exps.express.model.api.EmsResult;
import org.yht.exps.express.model.api.EmsResult.EmsReturn;
import org.yht.exps.express.model.api.HblInterface;
import org.yht.exps.express.model.api.HblTrackingInterface;
import org.yht.exps.express.model.api.HblTrackingResult;
import org.yht.exps.express.model.api.InterfaceResult;
import org.yht.exps.express.model.api.InterfaceResultJson;
import org.yht.exps.express.model.api.InterfaceResultJson.InterfaceResultDataJson;
import org.yht.exps.express.model.api.InterfaceReturnData;
import org.yht.exps.express.model.api.InterfaceReturnDataJson;
import org.yht.exps.express.model.api.InterfaceTracking;
import org.yht.exps.express.model.api.KorexConvertInterface;
import org.yht.exps.express.model.api.KorexConvertResult;
import org.yht.exps.express.model.api.KorexConvertReturnData;
import org.yht.exps.express.model.api.LxpXrayIntf;
import org.yht.exps.express.model.api.OceanInterface;
import org.yht.exps.express.model.api.OrderInterfaceJson;
import org.yht.exps.express.model.api.OrderInterfaceJsonDKX;
import org.yht.exps.express.model.api.TrackingInterfaceJson;
import org.yht.exps.express.model.api.TrackingResult;
import org.yht.exps.express.model.api.TrackingResultJson;
import org.yht.exps.express.model.api.Tracking_interface;
import org.yht.exps.express.model.api.hms.Hms;
import org.yht.exps.express.model.api.hms.HmsResult;
import org.yht.exps.express.model.api.hms.HmsToken;
import org.yht.exps.express.model.api.hms.HmsTokenResult;
import org.yht.exps.express.model.properties.JwtProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.api.InterfaceService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.exps.express.util.HttpUtil;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
@RestController
@Api(tags = "Api")
@RequestMapping("/if")
public class InterfaceController {

	@Resource(name = "interfaceService")
	InterfaceService interfaceService;
	
	@Resource(name = "commonService")
	CommonService commonService;	
	@Resource(name = "smsUserService")
	SmsUserService smsUserService;
	
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	JwtProperties jwtProperties;
	@Autowired
    JwtTokenProvider jwtTokenProvider;
	@Autowired
	RestHighLevelClient client;	
	@Autowired
	OrderMapper odMapper;
	@Value("${spring.profiles.active}")
	private String profile;
	
	@ApiOperation(value = "ECM 항공(AI/AO) 오더 생성 API", notes="ECM 항공(AI/AO) 오더를 생성합니다.")
	@RequestMapping(value={"/order/registAIOrderInfo","/order/registAOOrderInfo"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<InterfaceResult> registAirOrderInfo(@RequestBody String xmlData, HttpServletRequest req, HttpServletResponse res) throws Exception {
		// URL => http://localhost:8090/YHTXX/if/order/registAIOrderInfo (or registAOOrderInfo)		
		String entCode = null;
		xmlData = Util.escapeStr(xmlData);
		AirInterface order = null;
		try{
			JAXBContext jaxbContext = JAXBContext.newInstance(AirInterface.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			order = (AirInterface) unmarshaller.unmarshal(new StringReader(xmlData));
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, order.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
		}catch(Exception e){
			InterfaceResult interfaceResult = new InterfaceResult();
			InterfaceReturnData returnData = new InterfaceReturnData();
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("Not a Service Provider");
			interfaceResult.setReturnData(returnData);
			return new ResponseEntity<InterfaceResult>(interfaceResult, HttpStatus.OK);
		}
		Util.setDB(req, entCode);
		return new ResponseEntity<InterfaceResult>(interfaceService.registAirOrderInfo(order, req, null), HttpStatus.OK);
	}	
	
	@ApiOperation(value = "ECM 해상(OI/OO) 오더 생성 API", notes="ECM 해상(OI/OO) 오더를 생성합니다.")
	@RequestMapping(value={"/order/registOIOrderInfo","/order/registOOOrderInfo"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
		public ResponseEntity<InterfaceResult> registOceanOrderInfo(@RequestBody String xmlData, HttpServletRequest req, HttpServletResponse res) throws Exception {
		// URL => http://localhost:8090/YHTXX/if/order/registOIOrderInfo or registOOOrderInfo
		String entCode = null;
		xmlData = Util.escapeStr(xmlData);
		OceanInterface order = null;
		
		try{
			JAXBContext jaxbContext = JAXBContext.newInstance(OceanInterface.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			order = (OceanInterface) unmarshaller.unmarshal(new StringReader(xmlData));
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, order.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
		}catch(Exception e){
			InterfaceResult interfaceResult = new InterfaceResult();
			InterfaceReturnData returnData = new InterfaceReturnData();
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("Not a Service Provider");
			interfaceResult.setReturnData(returnData);
			return new ResponseEntity<InterfaceResult>(interfaceResult, HttpStatus.OK);
		}
		Util.setDB(req, entCode);
		return new ResponseEntity<InterfaceResult>(interfaceService.registOceanOrderInfo(order, req), HttpStatus.OK);
	}

	@ApiOperation(value = "UPS ECM 항공(AI/AO) 오더 생성 API - 사용자 인증", notes="UPS ECM 항공(AI/AO) 오더를 생성합니다.")
	@RequestMapping(value={"/order/registUAIOrderInfo","/order/registUAOOrderInfo"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<InterfaceResult> registUAirOrderInfo(@RequestBody AirInterface order, HttpServletRequest req) throws Exception {
		// URL => http://localhost:8090/if/order/registUAIOrderInfo (or registUAOOrderInfo)
		String entCode = null;
		if("ups2".equals(profile)){
			entCode = "UPSXK";
		}else{
			entCode = "YHTXX";
		}
//		try{
//			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, order.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
//		}catch(Exception e){
//			e.printStackTrace();
//			InterfaceResult interfaceResult = new InterfaceResult();
//			InterfaceReturnData returnData = new InterfaceReturnData();
//			returnData.setReturnCode("FAIL");
//			returnData.setReturnMessage("Not a Service Provider");
//			interfaceResult.setReturnData(returnData);
//			return new ResponseEntity<InterfaceResult>(interfaceResult, HttpStatus.OK);
//		}
		
		Util.setDB(req, entCode);
		String errMsg = Util.null2str(interfaceService.checkAccessIdentifierUps(order.getAccessIdentifier(), "U"));
		if(!"".equals(errMsg)) {
			InterfaceResult interfaceResult = new InterfaceResult();
			InterfaceReturnData returnData = new InterfaceReturnData();
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage(errMsg);
			interfaceResult.setReturnData(returnData);
			return new ResponseEntity<InterfaceResult>(interfaceResult, HttpStatus.OK);
		}
		return new ResponseEntity<InterfaceResult>(interfaceService.registAirOrderInfo(order, req, null), HttpStatus.OK);
	}
	
	@ApiOperation(value = "UPS ECM 항공(AI/AO) 오더 생성 API - 토큰 인증", notes="UPS ECM 항공(AI/AO) 오더를 생성합니다.")
	@RequestMapping(value={"/order/registUTAIOrderInfo","/order/registUTAOOrderInfo"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<InterfaceResult> registUTAirOrderInfo(@RequestBody AirInterface order, HttpServletRequest req) throws Exception {
		// URL => http://localhost:8090/if/order/registUTAIOrderInfo (or registUTAOOrderInfo)
		String entCode = null;
		if("ups2".equals(profile)){
			entCode = "UPSXK";
		}else{
			entCode = "YHTXX";
		}
		
		Util.setDB(req, entCode);
		String errMsg = Util.null2str(interfaceService.checkAccessIdentifierUps(order.getAccessIdentifier(), "T"));
		if(!"".equals(errMsg)) {
			InterfaceResult interfaceResult = new InterfaceResult();
			InterfaceReturnData returnData = new InterfaceReturnData();
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage(errMsg);
			interfaceResult.setReturnData(returnData);
			return new ResponseEntity<InterfaceResult>(interfaceResult, HttpStatus.OK);
		}
		return new ResponseEntity<InterfaceResult>(interfaceService.registAirOrderInfo(order, req, entCode), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 수입(항공&해상) 관세사 비용 적용 API", notes="ECM 수입(항공&해상) 관세사 비용 적용합니다.")
	@RequestMapping(value="/registCustomsInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<InterfaceResult> registCustomsInfo(@RequestBody CustomsInterface customs, HttpServletRequest req) throws Exception {
		// URL => http://localhost:8090/YHTXX/if/registCustomsInfo  => 11/09 관세사 API, 배치 보류
		// 관세사비용적용 API, 배치 => 개발보류
		String entCode = null;
		try{
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, customs.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
		}catch(Exception e){
			InterfaceResult interfaceResult = new InterfaceResult();
			InterfaceReturnData returnData = new InterfaceReturnData();
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("Not a Service Provider");
			interfaceResult.setReturnData(returnData);
			return new ResponseEntity<InterfaceResult>(interfaceResult, HttpStatus.OK);
		}
		Util.setDB(req, entCode);
		return new ResponseEntity<InterfaceResult>(interfaceService.registCustomsInfo(customs, req), HttpStatus.OK);
		
	}	
	
	@ApiOperation(value = "ECM 수입(항공&해상) 트레이킹 조회 API", notes="ECM 수입(항공&해상) 트레이킹 조회합니다.")
	@RequestMapping(value="/selectTrackingInfo", method = RequestMethod.GET)
	public void selectTrackingInfo(InterfaceTracking tracking, HttpServletRequest req, HttpServletResponse res) throws Exception {
		// URL => http://localhost:8090/#{endCode}/if/selectTrackingInfo?fwdCode=#{FWD}&hblNo=#{HBLNo}&bound=${bound}
		// URL => http://localhost:8090/YHTXX/if/selectTrackingInfo?entCode=YHTXX&hblNo=20211101_H1&bound=AI
		// 네이버 굿스플로,스윗트래커 연동
		JSONObject rtnJson = new JSONObject(); // 웹화면에 json으로 출력해주기 위함 
		 
		boolean resFlag = false;
		String entCode = null;
		try{
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, tracking.getFwdCode().toUpperCase()));
		}catch(Exception e){
			Map<String, Object> rootMap = new HashMap<String, Object>();
			rootMap.put("mArray", new ArrayList<Map<String, Object>>());
			rtnJson.put("resultJson", rootMap);
			res.setHeader("Content-Type", "application/json;charset=UTF-8");
			res.getWriter().write(rtnJson.toString());
			
			resFlag = true;
		}
		if(!resFlag){
			try {
				Util.setDB(req, entCode);
				Map<String, Object> rootMap = interfaceService.selectTrackingInfo(tracking, req);
				rtnJson.put("resultJson", rootMap);
			}catch(Exception e){
				log.error(Util.getPrintStacTraceString(e));
			}finally{
				// 웹화면에 조회한 결과를 json 포맷으로 출력
				res.setHeader("Content-Type", "application/json;charset=UTF-8");
				res.getWriter().write(rtnJson.toString());
			}
		}
	}	
	
	

	@ApiOperation(value = "ECM 수입(항공(AI/AO), 해상(OI/OO)) 스탁 자동 채번 API", notes="ECM 수입(항공(AI/AO), 해상(OI/OO)) 스탁 자동 채번 후 출력합니다.")
	@RequestMapping(value="/createStock", method = RequestMethod.POST)
	public void createStock(HttpServletRequest req, HttpServletResponse res) throws Exception {
		JSONObject rtnJson = new JSONObject(); // 웹화면에 json으로 출력해주기 위함 
		 
		String body = HttpUtil.getBodyDataByRequest(req);
		JSONObject reqJson = null;
		String entCode = null;
		
		
		boolean resFlag = false;
		try{
			if(body != null) {
				reqJson =  new JSONObject().fromObject(body);
				entCode = Util.null2str(reqJson.get("ReceiverIdentifier"));
			}
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, entCode.toUpperCase()));
		}catch(Exception e){
			rtnJson.put("Success", false);
			rtnJson.put("ErrorMsg", StringEscapeUtils.escapeHtml4("AccessKey가 등록되어 있지 않습니다. 관리자에게 문의하시기 바랍니다."));
			
			res.setHeader("Content-Type", "application/json;charset=UTF-8");
			res.getWriter().write(rtnJson.toString());
			
			resFlag = true;
		}
		
		if(!resFlag) {
			try {
				Util.setDB(req, entCode);
				int stockCnt = Util.null2int(reqJson.get("StockCount"));
				List<String> hblNoList = new ArrayList<String>();
				Map<String,Object> paramMap = new HashMap<String,Object>();
				//검증
				rtnJson = interfaceService.createStockCheck(body,paramMap);
				if((boolean)rtnJson.get("Success")) {
					for(int i=0; i<stockCnt; i++) {
						// STOCK 생성
						rtnJson = interfaceService.createStock(paramMap,hblNoList);
						rtnJson.put("HBLNos", hblNoList);
					}
				}
				
				//로그저장
				interfaceService.createStockLog(rtnJson,body,paramMap);
			}catch(Exception e){
				log.error(Util.getPrintStacTraceString(e));
			}finally{
				// 웹화면에 조회한 결과를 json 포맷으로 출력
				res.setHeader("Content-Type", "application/json;charset=UTF-8");
				res.getWriter().write(rtnJson.toString());
			}
		}
	}	
	
	@ApiOperation(value = "ECM 수입(항공(AI/AO), 해상(OI/OO)) 스탁 자동 채번 API", notes="ECM 수입(항공(AI/AO), 해상(OI/OO)) 스탁 자동 채번 후 출력합니다.")
	@RequestMapping(value="/createStockEms", method = RequestMethod.POST)
	public void createStockEms(HttpServletRequest req, HttpServletResponse res) throws Exception {
		JSONObject rtnJson = new JSONObject(); // 웹화면에 json으로 출력해주기 위함 
		 
		String body = HttpUtil.getBodyDataByRequest(req);
		JSONObject reqJson = null;
		String entCode = null;
		
		
		boolean resFlag = false;
		try{
			if(body != null) {
				reqJson =  new JSONObject().fromObject(body);
				entCode = Util.null2str(reqJson.get("ReceiverIdentifier"));
			}
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, entCode.toUpperCase()));
		}catch(Exception e){
			rtnJson.put("Success", false);
			rtnJson.put("ErrorMsg", StringEscapeUtils.escapeHtml4("AccessKey가 등록되어 있지 않습니다. 관리자에게 문의하시기 바랍니다."));
			
			res.setHeader("Content-Type", "application/json;charset=UTF-8");
			res.getWriter().write(rtnJson.toString());
			
			resFlag = true;
		}
		
		if(!resFlag) {
			try {
				Util.setDB(req, entCode);
				int stockCnt = Util.null2int(reqJson.get("StockCount"));
				List<String> hblNoList = new ArrayList<String>();
				Map<String,Object> paramMap = new HashMap<String,Object>();
				//검증
				rtnJson = interfaceService.createStockCheckEms(body,paramMap);
				if((boolean)rtnJson.get("Success")) {
					for(int i=0; i<stockCnt; i++) {
						// STOCK 생성
						rtnJson = interfaceService.createStockEms(paramMap,hblNoList);
						rtnJson.put("HBLNos", hblNoList);
					}
				}
				
				//로그저장
				interfaceService.createStockLog(rtnJson,body,paramMap);
			}catch(Exception e){
				log.error(Util.getPrintStacTraceString(e));
			}finally{
				// 웹화면에 조회한 결과를 json 포맷으로 출력
				res.setHeader("Content-Type", "application/json;charset=UTF-8");
				res.getWriter().write(rtnJson.toString());
			}
		}
	}
	
	@SuppressWarnings("finally")
	@ApiOperation(value = "ECM 대한통운 집배코드 조회 API", notes="ECM 대한통운 집배코드 조회합니다.")
	@RequestMapping(value="/korexConvert", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<KorexConvertResult> korexConvert(@RequestBody KorexConvertInterface korexIf, HttpServletRequest req) throws Exception {
		// URL => http://localhost:8090/YHTXX/if/korexConvert
		 
		KorexConvertResult korexConvertResult = new KorexConvertResult();
        KorexConvertReturnData  korexConvertReturnData = new KorexConvertReturnData();
        
        Map<String, Object> tmpMap = null;
        Map<String, Object> postCode = null; // 대한통운 API 우편번호 조회
        Map<String, Object> deliveryInfo = null;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Map<String, Object> korexPramMap = new HashMap<String, Object>();
        
		String clientIP = req.getRemoteAddr();
		String accessKey = "";
		String sender = "";
		String receiver = "";
		String clntNum = "";
		String clntMgmCustCd = "";
		String prngDivCd = "";
		String cgosts = "";
		String address = "";
		String zipcode = "";
		String returnCode = "";
		String returnMessage = "";
		String accessFlag = "N";
		String korexNo1 = "";
		String korexNo2 = "";
		String korexNo3 = "";
		String korexDsfAddr = "";
		String korexBransNm = "";
		String korexEmpNm = "";
		String korexNickNm = "";
		String pClsfce = "";
		String pSubClsfce = "";
//		String branchName = "";
		String branchCode = "";
		String logId = ""; 			// ES 로그용 아이디
		String index = ""; 			// ES 로그용 index
		String if_status = "RS";	// 처리상태값
		String if_status_info = "Receive Start"; // 처리상태설명
		
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		
		try{
			// XML 파일 파싱
			receiver = (Util.null2str(korexIf.getReceiverIdentifier())).toUpperCase();
			
			try{
				receiver = Util.null2str(Enum.valueOf(BranchEnum.class, receiver.toUpperCase()));
			}catch(Exception e){
				korexConvertReturnData.setReturnCode("FAIL");
				korexConvertReturnData.setReturnMessage("Not a Service Provider");
				korexConvertResult.setReturnData(korexConvertReturnData);
				return new ResponseEntity<KorexConvertResult>(korexConvertResult, HttpStatus.OK);
			}
			
			accessKey = (Util.null2str(korexIf.getAccessKey())).toUpperCase();
			sender = (Util.null2str(korexIf.getSenderIdentifier())).toUpperCase();
			clntNum = (Util.null2str(korexIf.getClntNum())).toUpperCase();
			clntMgmCustCd = (Util.null2str(korexIf.getClntMgmCustCd())).toUpperCase();
			prngDivCd = (Util.null2str(korexIf.getPrngDivCd())).toUpperCase();
			cgosts = (Util.null2str(korexIf.getCgosts())).toUpperCase();
			address = (Util.null2str(korexIf.getAddress())).toUpperCase();
			zipcode = (Util.null2str(korexIf.getZipcode())).toUpperCase();
	
	 		// interface 인증키 검증
	 		paramMap.put("ReceiverId", receiver);
	 		paramMap.put("SenderId", sender);
	 		paramMap.put("UseYN", "Y");
			
			Util.setDB(req, receiver);
			deliveryInfo = interfaceService.selectInterfaceInfo(paramMap);
			
			if(deliveryInfo != null){
				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
					accessFlag = "Y";
				}
			}else{
				accessFlag = "X";
			}
				
			// 인증키 검증 후 대한통운 인터페이스
			if("Y".equals(accessFlag)){
				
				// 대한통운 주소정제 API 사용량 측정을 위해 DELIVERY_CODE_CNT 업데이트 
		 		paramMap.put("Type", "korexConvert"); 			// Type : 대한통운 집배코드 (주소정제) => korexConvert , 오더생성 => order
//				interfaceService.updateInterfaceCnt(paramMap);	// 220622 주석처리
				
				korexPramMap.put("P_CLNTNUM", clntNum);
				korexPramMap.put("P_CLNTMGMCUSTCD", clntMgmCustCd);
				korexPramMap.put("P_PRNGDIVCD", prngDivCd);
				korexPramMap.put("P_CGOSTS", cgosts);
				korexPramMap.put("P_ADDRESS", address);
				
				/** 대한통운 집배코드조회 프로시저 호출시 오류발생해서     주소를 공백으로 분리한 후 2글자이상일경우 해당 문자가 중복되면 오류처리함 */
				/** 2024.12.23 주소에 괄호가 있을 경우 괄호 안의 내용은 제외하고 중복 체크 */
				if(address.indexOf("(") > -1 && address.indexOf(")") > -1){
					// aaa(bb, cc) -> aaa
					address = address.substring(0, address.indexOf("(")) + address.substring(address.indexOf(")") + 1, address.length());
				}
				Map<String, Object> checkMap = new HashMap<String, Object>();
				for(String word : address.split(" ")){
					if(word.length() > 1){
						if(checkMap.get(word) == null){
							checkMap.put(word, "Y");
						}else{
							korexConvertReturnData.setReturnCode("FAIL");
							korexConvertReturnData.setReturnMessage("The address is incorrect.(" + address + ")");
							korexConvertResult.setReturnData(korexConvertReturnData);
							return new ResponseEntity<KorexConvertResult>(korexConvertResult, HttpStatus.OK);
						}
					}
				}
				
				//허브넷분리(추후공통이될수 있어서 내부에서도 기존로직분기처리유지)
				if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")){
					//대한통운rest,db여부 조회 및 인터페이스 정보 가져오기
					Map<String, Object> srchDeliveryApiMap = new HashMap<String, Object>();
					Map<String, Object> deliveryApiMap = new HashMap<String, Object>();
					Map<String, Object> deliveryApiAllMap = new HashMap<String, Object>();
					srchDeliveryApiMap.put("entCode", receiver);
					srchDeliveryApiMap.put("deliveryCode", "KOREX_ADDR");
					srchDeliveryApiMap.put("korexId", clntNum);
					deliveryApiMap = odMapper.selectDeliveryApiInfo(srchDeliveryApiMap);// 중개업체id,url
					deliveryApiAllMap = odMapper.selectDeliveryApiInfoAllMapping(srchDeliveryApiMap);//사업자번호,업체코드,토큰
					//없을경우 COMMON적용
					if(deliveryApiAllMap == null) {
						deliveryApiAllMap = odMapper.selectDeliveryApiInfoAll(srchDeliveryApiMap);//사업자번호,업체코드
					}
					if(deliveryApiAllMap == null || deliveryApiAllMap.isEmpty() || !Util.null2str(deliveryApiAllMap.get("KOREX_METHOD")).equals("REST")){

						//기존
						Util.setDB(req, "KOREXADD");
						interfaceService.getKorexAddressCode(korexPramMap); // ECM 대한통운 집배코드 조회

						// 집배코드 파싱
						if(!"".equals(Util.null2str(korexPramMap.get("P_CLSFCD")))){
							korexDsfAddr = Util.null2str(korexPramMap.get("P_CLSFADDR"));
							korexBransNm = Util.null2str(korexPramMap.get("P_CLLDLCBRANSHORTNM"));
							korexEmpNm = Util.null2str(korexPramMap.get("P_CLLDLVEMPNM"));
							korexNickNm = Util.null2str(korexPramMap.get("P_CLLDLVEMPNICKNM"));
							pClsfce = Util.null2str(korexPramMap.get("P_CLSFCD"));
							pSubClsfce = Util.null2str(korexPramMap.get("P_SUBCLSFCD"));
							//branchName =  Util.null2str(korexPramMap.get("P_CLSFNM"));
							branchCode = pClsfce+pSubClsfce;
						}else{

							//기존
							Util.setDB(req, "KOREXADD");
							interfaceService.getKorexAddressCode(korexPramMap); // ECM 대한통운 집배코드 조회
							// 집배코드 파싱
							if(!"".equals(Util.null2str(korexPramMap.get("P_CLSFCD")))){
								korexDsfAddr = Util.null2str(korexPramMap.get("P_CLSFADDR"));
								korexBransNm = Util.null2str(korexPramMap.get("P_CLLDLCBRANSHORTNM"));
								korexEmpNm = Util.null2str(korexPramMap.get("P_CLLDLVEMPNM"));
								korexNickNm = Util.null2str(korexPramMap.get("P_CLLDLVEMPNICKNM"));
								pClsfce = Util.null2str(korexPramMap.get("P_CLSFCD"));
								pSubClsfce = Util.null2str(korexPramMap.get("P_SUBCLSFCD"));
								//branchName =  Util.null2str(korexPramMap.get("P_CLSFNM"));
								branchCode = pClsfce+pSubClsfce;
							}else{
								Util.setDB(req, "KOREX");
								postCode = interfaceService.checkKorexPostCode(zipcode);

								if(postCode != null){
									if(Util.null2str(postCode.get("MAN_BRAN_NM")).length() > 4) {
										korexBransNm = Util.null2str(postCode.get("MAN_BRAN_NM")).substring(1,4);
									} else {
										korexBransNm = Util.null2str(postCode.get("MAN_BRAN_NM"));
									}
									korexEmpNm = Util.null2str(postCode.get("CLDV_EMP_NM"));
									//korexNickNm = Util.null2str(postCode.get("P_CLLDLVEMPNICKNM")); //TB_POST는 없음
									pClsfce = Util.null2str(postCode.get("END_NO"));
									pSubClsfce = Util.null2str(postCode.get("SUB_END_NO"));
									//branchName =  Util.null2str(postCode.get("MAN_BRAN_NM"));
									branchCode = pClsfce + pSubClsfce;
								}
							}
						}

					}else{
						// 추가된 REST 방식
						HttpHeaders cjHeaders = new HttpHeaders();
						cjHeaders.setContentType(MediaType.APPLICATION_JSON);

						cjHeaders.set("CJ-Gateway-APIKey", Util.null2str(deliveryApiAllMap.get("KOREX_TOKEN_NUM")));

						Map<String, Object> cjData = new HashMap<String, Object>();
						cjData.put("CLNTNUM",       clntNum);//고객id
						cjData.put("CLNTMGMCUSTCD", clntMgmCustCd);//협력사id
						cjData.put("USER_ID",       deliveryApiMap.get("RECV_API_ID")); // 중개업체id
						cjData.put("ADDRESS",       korexPramMap.get("P_ADDRESS")); // 괄호 제거 전 원본 주소
						cjData.put("TOKEN_NUM",     deliveryApiAllMap.get("KOREX_TOKEN_NUM"));

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
						if(cjRes.getBody() != null){
							log.debug("대한통운 응답 : {}", new Gson().toJson(cjRes.getBody()));
							Map<String, Object> cjResult = (Map<String, Object>) cjRes.getBody();
							String resultCd = Util.null2str(cjResult.get("RESULT_CD"));
							if("S".equals(resultCd)){
								Map<String, Object> getData = (Map<String, Object>) cjResult.get("DATA");
								korexPramMap.put("P_CLSFCD",            Util.null2str(getData.get("CLSFCD")));
								korexPramMap.put("P_SUBCLSFCD",         Util.null2str(getData.get("SUBCLSFCD")));
								korexPramMap.put("P_CLSFADDR",          Util.null2str(getData.get("CLSFADDR")));
								korexPramMap.put("P_CLLDLCBRANSHORTNM", Util.null2str(getData.get("CLLDLVBRANNM")));
								korexPramMap.put("P_CLLDLVEMPNM",       Util.null2str(getData.get("CLLDLVEMPNM")));
								korexPramMap.put("P_CLLDLVEMPNICKNM",   Util.null2str(getData.get("CLLDLVEMPNICKNM")));
							}else{
								accessFlag    = "F";
								returnMessage = Util.null2str(cjResult.get("RESULT_DETAIL")); // CJ 오류메시지
							}
						}else{
							accessFlag    = "F";
							returnMessage = "대한통운 API 응답 없음";// CJ 오류메시지
						}
						// REST도 동일한 파싱 로직
						if(!"".equals(Util.null2str(korexPramMap.get("P_CLSFCD")))){
							korexDsfAddr = Util.null2str(korexPramMap.get("P_CLSFADDR"));
							korexBransNm = Util.null2str(korexPramMap.get("P_CLLDLCBRANSHORTNM"));
							korexEmpNm   = Util.null2str(korexPramMap.get("P_CLLDLVEMPNM"));
							korexNickNm  = Util.null2str(korexPramMap.get("P_CLLDLVEMPNICKNM"));
							pClsfce      = Util.null2str(korexPramMap.get("P_CLSFCD"));
							pSubClsfce   = Util.null2str(korexPramMap.get("P_SUBCLSFCD"));
							branchCode   = pClsfce + pSubClsfce;
						}
					}

				}else {
					
					Util.setDB(req, "KOREX");
					postCode = interfaceService.checkKorexPostCode(zipcode);

							
					if(postCode != null){
						if(Util.null2str(postCode.get("MAN_BRAN_NM")).length() > 4) {
							korexBransNm = Util.null2str(postCode.get("MAN_BRAN_NM")).substring(1,4);
						} else {
							korexBransNm = Util.null2str(postCode.get("MAN_BRAN_NM"));
						}
						korexEmpNm = Util.null2str(postCode.get("CLDV_EMP_NM"));
						//korexNickNm = Util.null2str(postCode.get("P_CLLDLVEMPNICKNM")); //TB_POST는 없음
						pClsfce = Util.null2str(postCode.get("END_NO"));
						pSubClsfce = Util.null2str(postCode.get("SUB_END_NO"));
						//branchName =  Util.null2str(postCode.get("MAN_BRAN_NM"));
						branchCode = pClsfce + pSubClsfce;
					}
				}
				
				if(branchCode.length() > 2) {
			        korexNo1 = branchCode.substring(0, 1);
			        if(branchCode.length() == 5){
			               korexNo2 = branchCode.substring(1, branchCode.length() - 1);
			               korexNo3 = branchCode.substring(branchCode.length() - 1);
			        }else{
			               korexNo2 = branchCode.substring(1, branchCode.length() - 2);
			               korexNo3 = branchCode.substring(branchCode.length() - 2);
			        }
				}else {
					accessFlag = "F";
					returnMessage = "해당 주소 또는 우편번호가 매칭되는 집배코드가 존재하지 않습니다.";
				}
			}
			
			if("Y".equals(accessFlag)){
	 	        returnCode = "SUCCESS";
	 			returnMessage = "";
				if_status = "SUCCESS";
	 			if_status_info = "Receive Complete"; 
	 		}else if("N".equals(accessFlag)){
				returnCode = "FAIL";
	 			returnMessage = "Wrong AccessKey";
				if_status = "ERROR";
	 			if_status_info = "Receive Fail"; 
	 		}else if("X".equals(accessFlag)){
				returnCode = "FAIL";
	 			returnMessage = "Not a service provider";
				if_status = "ERROR";
	 			if_status_info = "Receive Fail"; 
	 		}else if("F".equals(accessFlag)){
	 			returnCode = "FAIL";
				if_status = "ERROR";
	 			if_status_info = "Receive Fail"; 
	 		}
				
			korexConvertReturnData.setReturnCode(returnCode);
			korexConvertReturnData.setReturnMessage(returnMessage);
			
			if("Y".equals(accessFlag)){
				korexConvertReturnData.setDsfAddr(korexDsfAddr);
				korexConvertReturnData.setBransNm(korexBransNm);
//				korexConvertReturnData.setEmpNm(korexEmpNm);
				korexConvertReturnData.setEmpNm("");	//SM명 삭제되어 API에서도 "" 리턴해주도록 수정
				korexConvertReturnData.setNickNm(korexNickNm);
				korexConvertReturnData.setKorexNo1(korexNo1);
				korexConvertReturnData.setKorexNo2(korexNo2);
				korexConvertReturnData.setKorexNo3(korexNo3);
			}
			
			korexConvertResult.setReturnData(korexConvertReturnData);
			
		}catch(Exception e){
			returnCode = "FAIL";
			if_status = "ERROR";
 			if_status_info = "Receive Fail"; 
 			returnMessage = returnMessage + " EXCEPTION[" + e.getMessage() + "]";
	
			korexConvertReturnData.setReturnCode(returnCode);
			korexConvertReturnData.setReturnMessage(returnMessage);
			
			
			if("Y".equals(accessFlag)){
				korexConvertReturnData.setDsfAddr(korexDsfAddr);
				korexConvertReturnData.setBransNm(korexBransNm);
//				korexConvertReturnData.setEmpNm(korexEmpNm);
				korexConvertReturnData.setEmpNm("");
				korexConvertReturnData.setNickNm(korexNickNm);
				korexConvertReturnData.setKorexNo1(korexNo1);
				korexConvertReturnData.setKorexNo2(korexNo2);
				korexConvertReturnData.setKorexNo3(korexNo3);
			}
			
			korexConvertResult.setReturnData(korexConvertReturnData);
			
		}finally {
			
			Util.setDB(req, receiver);
			
			// history 등록 
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			
			// 처리결과에 따른 로그 기록 history
			tmpMap.clear();
			tmpMap.put("ent_code", receiver); 		 							 // 도입사코드
			tmpMap.put("customer_code", sender); 		 							 // 고객코드
			tmpMap.put("solution", "ECM");      		 						 // SOLUTION
			tmpMap.put("bound", "");		 	 							 	 // 바운드
			tmpMap.put("bound_type", "");	 							 		 // 바운드타입
			tmpMap.put("hbl_no", "");				 							 // HBL_NO
			tmpMap.put("if_service_code", "KOREX");
			tmpMap.put("if_doc_code", "DELIVERY_CODE_API");
			tmpMap.put("if_doc_name", "집배코드API"); 			 			 // 문서분류명 (AIR ORDER, SEA ORDER)
			tmpMap.put("if_doc_reference_no", "");	 							 // 문서번호 
			tmpMap.put("if_vender", sender);										 // 중개사업자
			tmpMap.put("if_send_recv_type", "");			 					 // 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "");		 				 		 // 송수신방법
			tmpMap.put("if_file_path", "");			 						 	 // 파일경로
			tmpMap.put("if_file_name", clientIP);			 				 	 // 파일명
			tmpMap.put("if_status", if_status);			 						 // 처리상태
			tmpMap.put("if_status_info", if_status_info);		 				 // 송수신상태_설명
			tmpMap.put("if_sender_id", sender);									 // 송신식별자
			tmpMap.put("if_sender_name", "");		 							 // 송신자명
			tmpMap.put("if_receiver_id", "");		 							 // 수신식별자
			tmpMap.put("if_receiver_name", "");	     							 // 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
			tmpMap.put("if_remark", returnMessage);			 					 // 비고( 에러로그 등)
			tmpMap.put("register_user_id", "API");
			tmpMap.put("register_user_name", "API");
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);	
					
			return new ResponseEntity<KorexConvertResult>(korexConvertResult, HttpStatus.OK);
		}
			


	}	

	/*
	 * 2022.12.21 강신정 과장님 요청
	 * 한진 집배코드 조회 API 사용 안 함.
	 * */
//	@SuppressWarnings("finally")
//	@ApiOperation(value = "ECM 한진 집배코드 조회 API", notes="ECM 한진 집배코드 조회합니다.")
//	@RequestMapping(value="/hanjinConvert", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
//	public ResponseEntity<HanjinConvertResult> hanjinConvert(@RequestBody HanjinConvertInterface hanjinIf, HttpServletRequest req) throws Exception {
//		// URL => http://localhost:8090/YHTXX/if/hanjinConvert
//
//		HanjinConvertResult hanjinConvertResult = new HanjinConvertResult();
//		HanjinReturnConvertData  hanjinReturnConvertData = new HanjinReturnConvertData();
//		
//		Map<String, Object> tmpMap = null;
//        Map<String, Object> deliveryInfo = null;
//        Map<String, Object> paramMap = new HashMap<String, Object>();
//        Map<String, Object> hanjinPramMap = new HashMap<String, Object>();
//        
//		String clientIP = req.getRemoteAddr();
//		String accessKey = "";
//		String sender = "";
//		String receiver = "";
//		String sZipcode = "";
//		String zipcode = "";
//		String returnCode = "";
//		String returnMessage = "";
//		String accessFlag = "N";
//		String HUB_COD = ""; //허브코드
//		String TML_COD = ""; //수하인 터미널코드
//		String TML_NAM = ""; //수하인 터미널명
//		String DOM_MID = ""; //중분류 코드 
//		String CEN_COD = ""; //영업소코드
//		String DOM_PDZ = ""; //소분류코드
//		String CEN_NAM = ""; //영업소명
//		String ES_NAM = ""; //ES 명
//		String STML_COD = ""; //송하인 터미널코드
//		String STML_NAM = ""; //송하인 터미널명
//		String PDZ_NAM = ""; //집배권역명
//		String DOM_RGN = ""; //제주/도서 구분
//		
//		String logId = ""; // ES 로그용 아이디
//		String index = ""; // ES 로그용 index
//		String if_status = "RS"; // 처리상태값
//		String if_status_info = "Receive Start"; // 처리상태설명
//		
//		IndexApi indexApi = null;
//		DocumentApi documentApi = null;		
//        
//		try{
//			
//			// XML 파일 파싱
//			accessKey = (Util.null2str(hanjinIf.getAccessKey())).toUpperCase();
//			sender = (Util.null2str(hanjinIf.getSenderIdentifier())).toUpperCase();
//			receiver = (Util.null2str(hanjinIf.getReceiverIdentifier())).toUpperCase();
//			sZipcode = (Util.null2str(hanjinIf.getSZipcode())).toUpperCase();
//			zipcode = (Util.null2str(hanjinIf.getZipcode())).toUpperCase();     
//			
//			paramMap.put("ReceiverId", receiver);
//	 		paramMap.put("SenderId", sender);
//	 		paramMap.put("UseYN", "Y");
//	 		
//			Util.setDB(req, receiver);
//			deliveryInfo = interfaceService.selectInterfaceInfo(paramMap);
//			
//			if(deliveryInfo != null){
//				if(accessKey.equals(deliveryInfo.get("ACCESS_KEY").toString())){
//					accessFlag = "Y";
//				}
//			}else{
//				accessFlag = "X";
//			}
//			
//			
//			// 인증키 검증 후 한진택배 인터페이스
//			if("Y".equals(accessFlag)){
//				hanjinPramMap.put("sZipcode", sZipcode);
//				hanjinPramMap.put("zipcode", zipcode);
//				
//				Util.setDB(req, "HANJIN");
//				hanjinPramMap = interfaceService.getHanjinAddressCode(hanjinPramMap); // ECM 한진 집배코드 조회
//				
//				// 집배코드 파싱
//				if(!"".equals(Util.null2str(hanjinPramMap.get("TML_COD")))) {
//					TML_COD = Util.null2str(hanjinPramMap.get("TML_COD")); 
//					HUB_COD = Util.null2str(hanjinPramMap.get("HUB_COD")); 
//					TML_NAM = Util.null2str(hanjinPramMap.get("TML_NAM")); 
//					DOM_MID = Util.null2str(hanjinPramMap.get("DOM_MID")); 
//					CEN_COD = Util.null2str(hanjinPramMap.get("CEN_COD")); 
//					DOM_PDZ = Util.null2str(hanjinPramMap.get("DOM_PDZ")); 
//					CEN_NAM = Util.null2str(hanjinPramMap.get("CEN_NAM")); 
//					ES_NAM = Util.null2str(hanjinPramMap.get("ES_NAM")); 
//					STML_COD = Util.null2str(hanjinPramMap.get("STML_COD")); 
//					STML_NAM = Util.null2str(hanjinPramMap.get("STML_NAM")); 
//					PDZ_NAM = Util.null2str(hanjinPramMap.get("PDZ_NAM")); 
//					DOM_RGN = Util.null2str(hanjinPramMap.get("DOM_RGN")); 
//				}else {
//					accessFlag = "F";
//					returnMessage = "해당 우편번호에 집배코드가 존재하지 않습니다.";
//				}
//				
//			}
//			
//			if("Y".equals(accessFlag)){
//	 	        returnCode = "SUCCESS";
//	 			returnMessage = "";
//	 			if_status = "SUCCESS";
//	 			if_status_info = "Receive Complete"; 
//	 		}else if("N".equals(accessFlag)){
//				returnCode = "FAIL";
//	 			returnMessage = "Wrong AccessKey";
//	 			if_status = "ERROR";
//	 			if_status_info = "Receive Fail";
//	 		}else if("X".equals(accessFlag)){
//				returnCode = "FAIL";
//	 			returnMessage = "Not a service provider";
//	 			if_status = "ERROR";
//	 			if_status_info = "Receive Fail";
//	 		}else if("F".equals(accessFlag)){
//	 			returnCode = "FAIL";
//	 			if_status = "ERROR";
//	 			if_status_info = "Receive Fail"; 
//	 		}
//			
//			
//			hanjinReturnConvertData.setReturnCode(returnCode);
//			hanjinReturnConvertData.setReturnMessage(returnMessage);
//			
//			if("Y".equals(accessFlag)){
//				//return data set
//				hanjinReturnConvertData.setHUB_COD(HUB_COD);
//				hanjinReturnConvertData.setTML_COD(TML_COD);
//				hanjinReturnConvertData.setTML_NAM(TML_NAM);
//				hanjinReturnConvertData.setDOM_MID(DOM_MID);
//				hanjinReturnConvertData.setCEN_COD(CEN_COD);
//				hanjinReturnConvertData.setDOM_PDZ(DOM_PDZ);
//				hanjinReturnConvertData.setCEN_NAM(CEN_NAM);
//				hanjinReturnConvertData.setES_NAM(ES_NAM);
//				hanjinReturnConvertData.setSTML_COD(STML_COD);
//				hanjinReturnConvertData.setSTML_NAM(STML_NAM);
//				hanjinReturnConvertData.setPDZ_NAM(PDZ_NAM);
//				hanjinReturnConvertData.setDOM_RGN(DOM_RGN);
//				
//			}
//			
//			hanjinConvertResult.setReturnData(hanjinReturnConvertData);
//			
//		}catch(Exception e){
//			
//			returnCode = "FAIL";
//			if_status = "ERROR";
// 			if_status_info = "Receive Fail"; 
//			
//			hanjinReturnConvertData.setReturnCode(returnCode);
//			hanjinReturnConvertData.setReturnMessage(returnMessage);
//			
//			returnMessage = returnMessage + " EXCEPTION[" + e.getMessage() + "]";
//			
//			
//			if("Y".equals(accessFlag)){
//				hanjinReturnConvertData.setHUB_COD(HUB_COD);
//				hanjinReturnConvertData.setTML_COD(TML_COD);
//				hanjinReturnConvertData.setTML_NAM(TML_NAM);
//				hanjinReturnConvertData.setDOM_MID(DOM_MID);
//				hanjinReturnConvertData.setCEN_COD(CEN_COD);
//				hanjinReturnConvertData.setDOM_PDZ(DOM_PDZ);
//				hanjinReturnConvertData.setCEN_NAM(CEN_NAM);
//				hanjinReturnConvertData.setES_NAM(ES_NAM);
//				hanjinReturnConvertData.setSTML_COD(STML_COD);
//				hanjinReturnConvertData.setSTML_NAM(STML_NAM);
//				hanjinReturnConvertData.setPDZ_NAM(PDZ_NAM);
//				hanjinReturnConvertData.setDOM_RGN(DOM_RGN);
//			}
//			 
//			hanjinConvertResult.setReturnData(hanjinReturnConvertData);
//			
//			
//		}finally {
//			
//			Util.setDB(req, receiver);
//			// history 등록 
//			tmpMap = new HashMap<String, Object>();
//			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
//			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//			
//			// 처리결과에 따른 로그 기록 history
//			tmpMap.clear();
//			tmpMap.put("ent_code", receiver); 		 							 // 도입사코드
//			tmpMap.put("customer_code", ""); 		 							 // 고객코드
//			tmpMap.put("solution", "ECM");      		 							 // SOLUTION
//			tmpMap.put("bound", "");		 	 							 	 // 바운드
//			tmpMap.put("bound_type", "");	 							 		 // 바운드타입
//			tmpMap.put("hbl_no", "");				 							 // HBL_NO
//			tmpMap.put("if_service_code", "HANJIN");
//			tmpMap.put("if_doc_code", "DELIVERY_CODE_API");
//			tmpMap.put("if_doc_name", "집배코드API");
//			tmpMap.put("if_doc_reference_no", "");	 							 // 문서번호 
//			tmpMap.put("if_vender", "");										 // 중개사업자
//			tmpMap.put("if_send_recv_type", "");								 // 송수신타입(S:SEND, R:RECV)
//			tmpMap.put("if_send_recv_method", "");		 				 		 // 송수신방법
//			tmpMap.put("if_file_path", "");			 						 	 // 파일경로
//			tmpMap.put("if_file_name", clientIP);			 				 	 // 파일명
//			tmpMap.put("if_status", if_status);			 						 // 처리상태
//			tmpMap.put("if_status_info", if_status_info);		 				 // 송수신상태_설명
//			tmpMap.put("if_sender_id", sender);									 // 송신식별자
//			tmpMap.put("if_sender_name", "");		 							 // 송신자명
//			tmpMap.put("if_receiver_id", "");		 							 // 수신식별자
//			tmpMap.put("if_receiver_name", "");	     							 // 수신자명
//			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
//			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
//			tmpMap.put("if_remark", returnMessage);			 					 // 비고( 에러로그 등)
//			tmpMap.put("register_user_id", "API");
//			tmpMap.put("register_date_time", System.currentTimeMillis());
//			tmpMap.put("es_job_no", logId);
//			
//			indexApi = new IndexApi(client);
//			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//			
//			documentApi = new DocumentApi(client);
//			documentApi.createDocument(index, logId, tmpMap);							
//			
//			return new ResponseEntity<HanjinConvertResult>(hanjinConvertResult, HttpStatus.OK);
//		}
//		
//	}		
	
	
	/**
	 * HMS 토큰 발행
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "UPS 토큰 발행", notes="인증(ID 존재 확인) 후 인증 토큰값을 반환합니다.")
	@RequestMapping(value = "/get/token/ups", method = RequestMethod.POST)
	public ResponseEntity<HmsTokenResult> getTokenUps(@RequestBody HmsToken hmsToken, HttpServletRequest req) throws Exception {
		String entCode = null;
		if("ups2".equals(profile)){
			entCode = "UPSXK";
		}else{
			entCode = "YHTXX";
		}
		Util.setDB(req, entCode);
		HmsTokenResult tokenResult = new HmsTokenResult();
		try{
			Map<String, Object> map = null;
			if(!Util.isEmpty(hmsToken.getClient_id()) && !Util.isEmpty(hmsToken.getClient_secret())){
				map = new HashMap<String, Object>();
				map.put("entCode", entCode);
				map.put("deliveryCode", "hms");
				map.put("recvApiId", hmsToken.getClient_id());
				map.put("recvApiPw", hmsToken.getClient_secret());
				map = interfaceService.selectDeliveryInterfaceApi(map);
			}
			if(map == null){
				return new ResponseEntity<HmsTokenResult>(tokenResult, HttpStatus.UNAUTHORIZED);
			}
			
			final List<String> roles = new ArrayList<>();
			final UserDetails details = smsUserService.loadUserByUsername(hmsToken.getClient_id());
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);

			
			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			for (GrantedAuthority authority : details.getAuthorities()) {
				roles.add(authority.toString());
				authorities.add(new SimpleGrantedAuthority(authority.toString()));
			}
			tokenResult.setAccess_token(jwtTokenProvider.createToken4(details.getUsername(), roles, false));
			tokenResult.setToken_type("Bearer");
			//TODO HMS 유효시간확인
			tokenResult.setExpires_in(jwtProperties.getValidityInMs());
			
			return new ResponseEntity<HmsTokenResult>(tokenResult, HttpStatus.OK);
		}catch(Exception e){
			log.error(e.getMessage());
			return new ResponseEntity<HmsTokenResult>(tokenResult, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@ApiOperation(value = "shipment info before packages arrive at HUB", notes="shipment info before packages arrive at HUB")
	@RequestMapping(value="/hms/registShipmentInfo", method = RequestMethod.POST)
	public ResponseEntity<HmsResult> hmsRegistShipmentInfo(@RequestBody Hms hms, HttpServletRequest req) throws Exception {
		log.info("============================== hmsRegistShipmentInfo START : " + Util.null2str(hms.getPayload().getShipmentNo()) + " ==============================");
		String entCode = "";
		if("ups2".equals(profile)){
			Util.setDB(req, "UPSXK");
			entCode = "UPSXK";
		}else{
			Util.setDB(req, "YHTXX");
			entCode = "YHTXX";
		}
		HmsResult result = interfaceService.hmsRegistShipmentInfo(hms,entCode);
		log.info("============================== hmsRegistShipmentInfo END : " + Util.null2str(hms.getPayload().getShipmentNo()) + " ==============================");
		if(result.getResponse() == null || "FAIL".equals(result.getResponse().getReturnCode())){
			return new ResponseEntity<HmsResult>(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}else if("400".equals(result.getResponse().getReturnCode())){
			result.getResponse().setReturnCode("FAIL");
			return new ResponseEntity<HmsResult>(result, HttpStatus.BAD_REQUEST);
		}else{
			return new ResponseEntity<HmsResult>(result, HttpStatus.OK);
		}
	}
	
	@ApiOperation(value = "Send GSSI outbound scan data when package left hub", notes="Send GSSI outbound scan data when package left hub")
	@RequestMapping(value="/hms/registPackageScanInfo", method = RequestMethod.POST)
	public ResponseEntity<HmsResult> hmsRegistPackageScanInfo(@RequestBody Hms hms, HttpServletRequest req) throws Exception {
		log.info("============================== hmsRegistPackageScanInfo START : " + Util.null2str(hms.getPayload().getShipmentNo()) + " ==============================");
		String entCode = "";
		if("ups2".equals(profile)){
			Util.setDB(req, "UPSXK");
			entCode = "UPSXK";
		}else{
			Util.setDB(req, "YHTXX");
			entCode = "YHTXX";
		}
		HmsResult result = interfaceService.hmsRegistPackageScanInfo(hms,entCode);
		log.info("============================== hmsRegistPackageScanInfo END : " + Util.null2str(hms.getPayload().getShipmentNo()) + " ==============================");
		if(result.getResponse() == null || "FAIL".equals(result.getResponse().getReturnCode())){
			return new ResponseEntity<HmsResult>(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}else if("400".equals(result.getResponse().getReturnCode())){
			result.getResponse().setReturnCode("FAIL");
			return new ResponseEntity<HmsResult>(result, HttpStatus.BAD_REQUEST);
		}else{
			return new ResponseEntity<HmsResult>(result, HttpStatus.OK);
		}
	}
	
	@ApiOperation(value = "특송 BL & Pickup 생성 API", notes="특송 BL & Pickup을 생성합니다.")
	@RequestMapping(value="/ems/registblInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<InterfaceResult> registHblInfo(@RequestBody HblInterface order, HttpServletRequest req) throws Exception {
		// URL => http://localhost:8090/ems/registblInfo
		String entCode = null;
		try{
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, order.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
		}catch(Exception e){
			InterfaceResult interfaceResult = new InterfaceResult();
			InterfaceReturnData returnData = new InterfaceReturnData();
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("Not a Service Provider");
			interfaceResult.setReturnData(returnData);
			return new ResponseEntity<InterfaceResult>(interfaceResult, HttpStatus.OK);
		}
		Util.setDB(req, entCode);
		return new ResponseEntity<InterfaceResult>(interfaceService.registHblInfo(order), HttpStatus.OK);
	}
	
	@ApiOperation(value = "특송 BL & Pickup 트레이킹 조회 API", notes="특송 BL & Pickup 트레이킹 조회합니다.")
	@RequestMapping(value="/ems/blTracking", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<HblTrackingResult> hblTracking(@RequestBody HblTrackingInterface tracking, HttpServletRequest req) throws Exception {
		// URL => http://localhost:8090/ems/registblInfo
		String entCode = null;
		try{
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, tracking.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
		}catch(Exception e){
			HblTrackingResult interfaceResult = new HblTrackingResult();
			InterfaceReturnData returnData = new InterfaceReturnData();
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("Not a Service Provider");
			interfaceResult.setReturnData(returnData);
			return new ResponseEntity<HblTrackingResult>(interfaceResult, HttpStatus.OK);
		}
		Util.setDB(req, entCode);
		
		return new ResponseEntity<HblTrackingResult>(interfaceService.hblTracking(tracking, entCode), HttpStatus.OK);
	}
	
	@ApiOperation(value = "POD 조회 API", notes="오더번호, 하우스번호로 트래킹조회")
	/*특송 항공 수입 interface Tracking*/
	@RequestMapping(value = {"/order/selectAOOrderTracking","/order/selectAIOrderTracking","/order/selectOIOrderTracking","/order/selectOOOrderTracking"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<TrackingResult> selectAIOrderTracking(@RequestBody Tracking_interface track, HttpServletRequest req) throws Exception{
		
		String receiver = null;
		
		try{
			receiver = Util.null2str(Enum.valueOf(BranchEnum.class, track.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
			if (track.getOrderTracking() == null) {
				TrackingResult trackingResult = new TrackingResult();
				InterfaceReturnData returnData = new InterfaceReturnData();
				returnData.setReturnCode("FAIL");
				returnData.setReturnMessage("OrdId or HBLNo is required");
				trackingResult.setReturnData(returnData);
				StringWriter writer = new StringWriter();
				JAXBContext jaxbContext1 = JAXBContext.newInstance(TrackingResult.class);
				Marshaller jaxbMarshaller1 = jaxbContext1.createMarshaller();
				jaxbMarshaller1.marshal(trackingResult, writer);
				return new ResponseEntity<TrackingResult>(trackingResult, HttpStatus.OK);
			}
		}catch(Exception e){
			
			TrackingResult trackingResult = new TrackingResult();
			InterfaceReturnData returnData = new InterfaceReturnData();
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("Not a Service Provider");
			trackingResult.setReturnData(returnData);
			
			StringWriter writer = new StringWriter();
			JAXBContext jaxbContext1 = JAXBContext.newInstance(TrackingResult.class);
	        Marshaller jaxbMarshaller1 = jaxbContext1.createMarshaller();
	        jaxbMarshaller1.marshal(trackingResult, writer);

			return new ResponseEntity<TrackingResult>(trackingResult, HttpStatus.OK);
		}
		
		Util.setDB(req, receiver);
		List<Map<String, Object>> list = null;


		if(req.getRequestURI().equals("/if/order/selectAOOrderTracking"))list = interfaceService.selectOrderTrackingList(req, track,"AIR","EXP");
		else if(req.getRequestURI().equals("/if/order/selectAIOrderTracking"))list = interfaceService.selectOrderTrackingList(req, track,"AIR","IMP");
		else if(req.getRequestURI().equals("/if/order/selectOOOrderTracking"))list = interfaceService.selectOrderTrackingList(req, track,"SEA","EXP");
		else list = interfaceService.selectOrderTrackingList(req, track,"SEA","IMP");
		
		return new ResponseEntity<TrackingResult>(interfaceService.convertPodReturnData(list), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EMS 환적 수입(AI/OI) 오더 생성 API", notes="EMS 환적 수입(AI/OI) 오더를 생성합니다.")
	@RequestMapping(value={"/order/ems/registAIOrderInfo","/order/ems/registOIOrderInfo"},  method = RequestMethod.POST)
	public void emsRegistOrderInfo(@RequestBody EmsInterface emsInterface, HttpServletRequest req, HttpServletResponse res) throws Exception {
		// URL => http://localhost:8090/YHTXX/if/order/registEMSOIOrderInfo	
		JSONObject rtnJson = new JSONObject(); // 웹화면에 json으로 출력해주기 위함 
		EmsReturn emsReturn = new EmsReturn();
		String msg = "";
		
		Util.setDB(req, "MASTER");
		String entCode = null;
//		System.out.println(emsInterface);
		if(emsInterface.getAccessIdentifier() != null && emsInterface.getAccessIdentifier().getReceiverIdentifier() != null) {
			try{
				entCode = Util.null2str(Enum.valueOf(BranchEnum.class, emsInterface.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
			}catch(Exception e){
				msg = "Not a Service Provider";
			}
			// 도입사 환적사용여부 조회(EMS_YN)
			if(msg.equals("") && "N".equals(Util.null2str(commonMapper.selectEnterPriseEmsYn(entCode), "N"))) {
				msg = "UnAuthorized";
			}
		}else {
			msg = "Not a Service Provider";
		}
		if(!msg.equals("")) {
			emsReturn.setReturnCode("FAIL");
			emsReturn.setReturnMessage(msg);
			rtnJson.put("Return", emsReturn);
			res.setHeader("Content-Type", "application/json;charset=UTF-8");
			res.getWriter().write(rtnJson.toString());
		}else {
			Util.setDB(req, entCode);
			emsReturn = interfaceService.emsRegistOrderInfo(emsInterface, req);
		}
		Gson gson = new Gson();
	    String jsonString = gson.toJson(emsReturn);
		res.setHeader("Content-Type", "application/json;charset=UTF-8");
		res.getWriter().write(jsonString);
	}
	
	@ApiOperation(value = "EMS 환적 송장 호출 API", notes="EMS 환적 송장을 호출합니다.")
	@RequestMapping(value={"/order/ems/selectRequestBL"},  method = RequestMethod.POST)
	public void emsSelectRequestBL(@RequestBody EmsRequestBLnterface reqBl, HttpServletRequest req, HttpServletResponse res) throws Exception {
		// URL => http://localhost:8090/YHTXX/if/order/ems/selectRequestBL	
		JSONObject rtnJson = new JSONObject(); // 웹화면에 json으로 출력해주기 위함 
		EmsResult emsResult = new EmsResult();
		String msg = "";
		
		Util.setDB(req, "MASTER");
		String entCode = null;
//		System.out.println(reqBl);
		if(reqBl.getAccessIdentifier() != null && reqBl.getAccessIdentifier().getReceiverIdentifier() != null) {
			try{
				entCode = Util.null2str(Enum.valueOf(BranchEnum.class, reqBl.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
			}catch(Exception e){
				msg = "Not a Service Provider";
			}
			// 도입사 환적사용여부 조회(EMS_YN)
			if(msg.equals("") && "N".equals(Util.null2str(commonMapper.selectEnterPriseEmsYn(entCode), "N"))) {
				msg = "UnAuthorized";
			}
		}else {
			msg = "Not a Service Provider";
		}
		if(!msg.equals("")) {
			EmsReturn emsReturn = new EmsReturn();
			emsReturn.setReturnCode("FAIL");
			emsReturn.setReturnMessage(msg);
			emsResult.setReturn(emsReturn);
		}else {
			Util.setDB(req, entCode);
			emsResult = interfaceService.emsSelectRequestBL(req, reqBl);
		}
		Gson gson = new Gson();
	    String jsonString = gson.toJson(emsResult);
		res.setHeader("Content-Type", "application/json;charset=UTF-8");
		res.getWriter().write(jsonString);
	}
	
	@ApiOperation(value = "ECM 오더 생성 API", notes="ECM 오더를 생성합니다.")
	@RequestMapping(value={"/order/registOrderInfo"},  method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void registOrderInfo(@RequestBody OrderInterfaceJson orderInterface, HttpServletRequest req, HttpServletResponse res) throws Exception {
		// URL => http://localhost:8090/YHTXX/if/order/registEMSOIOrderInfo	
		InterfaceResultJson orderReturn = new InterfaceResultJson();
		String msg = "";
		String entCode = null;

		if(orderInterface.getAccessIdentifier() != null && orderInterface.getAccessIdentifier().getReceiverIdentifier() != null) {
			try{
				entCode = Util.null2str(Enum.valueOf(BranchEnum.class, orderInterface.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
			}catch(Exception e){
				msg = "Not a Service Provider";
			}
		}else {
			msg = "Not a Service Provider";
		}
		if(!msg.equals("")) {
			InterfaceResultDataJson resultData = new InterfaceResultDataJson();
			resultData.setReturnCode("FAIL");
			resultData.setReturnMessage(msg);
			orderReturn.setReturnData(resultData);
		}else {
			Util.setDB(req, entCode);
			orderReturn = interfaceService.registOrderInfo(orderInterface, req);
		}
		ObjectMapper mapper = new ObjectMapper();
		String jsonBody = mapper.writeValueAsString(orderReturn);
		res.setHeader("Content-Type", "application/json;charset=UTF-8");
		res.getWriter().write(jsonBody);
	}
	
	@ApiOperation(value = "ECM 송장 호출 API", notes="ECM 송장을 호출합니다.")
	@RequestMapping(value={"/order/selectRequestBL"},  method = RequestMethod.POST)
	public void ecmSelectRequestBL(@RequestBody EcmRequestBLnterface reqBl, HttpServletRequest req, HttpServletResponse res) throws Exception {
		// URL => http://localhost:8090/YHTXX/if/order/ecm/selectRequestBL	
		JSONObject rtnJson = new JSONObject(); // 웹화면에 json으로 출력해주기 위함 
		EcmResult ecmResult = new EcmResult();
		String msg = "";
		
		Util.setDB(req, "MASTER");
		String entCode = null;
		if(reqBl.getAccessIdentifier() != null && reqBl.getAccessIdentifier().getReceiverIdentifier() != null) {
			try{
				entCode = Util.null2str(Enum.valueOf(BranchEnum.class, reqBl.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
			}catch(Exception e){
				msg = "Not a Service Provider";
			}
		}else {
			msg = "Not a Service Provider";
		}
		if(!msg.equals("")) {
			EcmReturn ecmReturn = new EcmReturn();
			ecmReturn.setReturnCode("FAIL");
			ecmReturn.setReturnMessage(msg);
			ecmResult.setReturn(ecmReturn);
		}else {
			Util.setDB(req, entCode);
			ecmResult = interfaceService.ecmSelectRequestBL(req, reqBl);
		}
		Gson gson = new Gson();
	    String jsonString = gson.toJson(ecmResult);
		res.setHeader("Content-Type", "application/json;charset=UTF-8");
		res.getWriter().write(jsonString);
	}
	
	@ApiOperation(value = "POD 조회 API", notes="오더번호, 하우스번호로 트래킹조회(json)")
	/*특송 항공 수입 interface Tracking*/
	@RequestMapping(value = {"/order/selectOrderTracking"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void selectOrderTrackingJson(@RequestBody TrackingInterfaceJson track, HttpServletRequest req, HttpServletResponse res) throws Exception{
		TrackingResultJson trackingResult = new TrackingResultJson();
		String msg = "";
		String receiver = null;
		
		try{
			receiver = Util.null2str(Enum.valueOf(BranchEnum.class, track.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
		}catch(Exception e){
			msg = "Not a Service Provider";
		}
		
		if(!msg.equals("")) {
			InterfaceReturnDataJson returnData = new InterfaceReturnDataJson();
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("Not a Service Provider");
			trackingResult.setReturnData(returnData);
		}else {
			Util.setDB(req, receiver);
			
			List<Map<String, Object>> list = interfaceService.selectOrderTrackingJsonList(req, track);
			trackingResult = interfaceService.convertPodReturnDataJson(list);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonBody = mapper.writeValueAsString(trackingResult);
		res.setHeader("Content-Type", "application/json;charset=UTF-8");
		res.getWriter().write(jsonBody);
	}
	
	@ApiOperation(value = "ECM 오더(DKX) 생성 API", notes="ECM 오더(DKX)를 생성합니다.")
	@RequestMapping(value={"/order/registOrderInfoDKX"},  method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void registOrderInfoDKX(@RequestBody OrderInterfaceJsonDKX orderInterface, HttpServletRequest req, HttpServletResponse res) throws Exception {
		// URL => http://localhost:8090/YHTXX/if/order/registEMSOIOrderInfo	
		InterfaceResultJson orderReturn = new InterfaceResultJson();
		String msg = "";
		String entCode = null;

		if(orderInterface.getAccessIdentifier() != null && orderInterface.getAccessIdentifier().getReceiverIdentifier() != null) {
			try{
				entCode = Util.null2str(Enum.valueOf(BranchEnum.class, orderInterface.getAccessIdentifier().getReceiverIdentifier().toUpperCase()));
			}catch(Exception e){
				msg = "Not a Service Provider";
			}
		}else {
			msg = "Not a Service Provider";
		}
		if(!msg.equals("")) {
			InterfaceResultDataJson resultData = new InterfaceResultDataJson();
			resultData.setReturnCode("FAIL");
			resultData.setReturnMessage(msg);
			orderReturn.setReturnData(resultData);
		}else {
			Util.setDB(req, entCode);
			orderReturn = interfaceService.registOrderInfoDKX(orderInterface, req);
		}
		ObjectMapper mapper = new ObjectMapper();
		String jsonBody = mapper.writeValueAsString(orderReturn);
		res.setHeader("Content-Type", "application/json;charset=UTF-8");
		res.getWriter().write(jsonBody);
	}
	
	@ApiOperation(value = "PANTOS X-RAY 분배", notes="X-RAY 분배")
	/* X-RAY 분배 동시 구현 DB 프로시저 호출 */
	@RequestMapping(value = {"/workstationinterconnection/v1/imagenotification/"}, method = RequestMethod.POST, produces = "application/json;charset=utf8")
	public ResponseEntity<InterfaceResult> ImageDisplayInfo(@RequestBody LxpXrayIntf xray, HttpServletRequest req) throws Exception{
		
		String entCode = "";
//		if("pantos2".equals(profile)){
			Util.setDB(req, "PANTK");
			entCode = "PANTK";
//		}else{
//			Util.setDB(req, "YHTXX");
//			entCode = "YHTXX";
//		}
		
		xray.setEntCode(entCode);
		
		InterfaceResult result = interfaceService.imageDisplayInfo(xray, req);
		if("FAIL".equals(result.getReturnData().getReturnCode())) {
			return new ResponseEntity<InterfaceResult>(result, HttpStatus.BAD_REQUEST);
		}else {
			return new ResponseEntity<InterfaceResult>(result, HttpStatus.OK);
		}
	}
	
	@ApiOperation(value = "인터페이스 서버 상태 확인", notes="인터페이스 서버 상태 확인")
	/* 인터페이스 서버 상태 확인용 */
	@RequestMapping(value = {"/check/interfaceServer"}, method = RequestMethod.GET, produces = "application/json;charset=utf8")
	public ResponseEntity<InterfaceResult> checkServerStatus(HttpServletRequest req,HttpServletResponse res) throws Exception{
		
		InterfaceResult result = new InterfaceResult();
		InterfaceReturnData returnData = new InterfaceReturnData();
		returnData.setReturnCode("Success");
		returnData.setReturnMessage("ok");
		result.setReturnData(returnData);
		log.debug("interfaceServer Check!!!!!");
		
		return new ResponseEntity<InterfaceResult>(result, HttpStatus.OK);
	}
}
