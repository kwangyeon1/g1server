package org.yht.exps.express.controller.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.constant.BranchEnum;
import org.yht.exps.express.model.api.CoupangOrderInterface;
import org.yht.exps.express.model.api.CoupangPccc;
import org.yht.exps.express.model.api.CoupangReturn;
import org.yht.exps.express.model.api.InterfaceReturnDataPcccVE;
import org.yht.exps.express.model.api.InterfaceReturnDataVE;
import org.yht.exps.express.service.api.InterfaceServiceVE;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "Api")
@RequestMapping("/if/ve")
public class InterfaceControllerVE {
	@Resource(name = "interfaceServiceVE")
	InterfaceServiceVE interfaceServiceVE;
	
	//로컬,테스트,운영 구분
	@Value("${spring.profiles.active}")
	private String profile;
	
	//개발
	private final  String receiverDev = "YHTXX";
	//운영
	private final  String receiverProd = "HUBNK";
	
	@ApiOperation(value = "쿠팡 오더 생성 API", notes = "쿠팡 오더를 생성합니다.")
	@RequestMapping(value = {"/coupang/order/registOrderInfo"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<InterfaceReturnDataVE> registCoupangOrderInfo(@RequestBody CoupangOrderInterface order, HttpServletRequest req) throws Exception{
		//System.out.println(req.getrequ);
		// URL => http://localhost:8090/YHTXX/if/ve/coupang/order/registOrderInfo
		String entCode = null;
		try {
			// 운영 구분
			String receiver = "";
			log.debug("interface:data::>"+order.toString());
			String senderName = profile.toUpperCase();
			log.debug("interface:data::>"+senderName);
			if(senderName.contains("BMS12") || senderName.contains("HUBN2")){
	    		receiver = receiverProd;
	    	}else {
	    		receiver = receiverDev;
	    	}
			
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, receiver));
		}catch (Exception e) {
//			InterfaceResultVE interfaceResultVE = new InterfaceResultVE();
			InterfaceReturnDataVE returnData = new InterfaceReturnDataVE();
			returnData.setCode("FAIL");
			returnData.setMessage("Not a Service Provider");
			return new ResponseEntity<InterfaceReturnDataVE>(returnData, HttpStatus.OK);
		}
		
		Util.setDB(req, entCode);
		return new ResponseEntity<InterfaceReturnDataVE>(interfaceServiceVE.registCoupangOrderInfo(order, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "쿠팡 오더 반품처리 API", notes = "쿠팡 반품 처리합니다.")
	@RequestMapping(value = {"/coupang/order/returnOrderInfo"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<InterfaceReturnDataVE> returnCoupangOrderInfo(@RequestBody CoupangReturn order, HttpServletRequest req) throws Exception{
		// URL => http://localhost:8090/YHTXX/if/ve/coupang/order/returnOrderInfo
		String entCode = null;
		try {
			// 운영 구분
			String receiver = "";
			log.debug("interface:data::>"+order.toString());
			String senderName = profile.toUpperCase();
			log.debug("interface:data::>"+senderName);
			if(senderName.contains("BMS12") || senderName.contains("HUBN2")){
	    		receiver = receiverProd;
	    	}else {
	    		receiver = receiverDev;
	    	}
			
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, receiver));
		}catch (Exception e) {
//			InterfaceResultVE interfaceResultVE = new InterfaceResultVE();
			InterfaceReturnDataVE returnData = new InterfaceReturnDataVE();
			returnData.setCode("FAIL");
			returnData.setMessage("Not a Service Provider");
			return new ResponseEntity<InterfaceReturnDataVE>(returnData, HttpStatus.OK);
		}
		
		Util.setDB(req, entCode);
		return new ResponseEntity<InterfaceReturnDataVE>(interfaceServiceVE.returnCoupangOrderInfo(order, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "쿠팡 PCCC수신 API", notes = "쿠팡 PCCC수신 처리합니다.")
	@RequestMapping(value = {"/Coupang/CoupangRequest/pccc/error/send"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<InterfaceReturnDataPcccVE> coupangOrderPcccInfo(@RequestBody CoupangPccc pccc, HttpServletRequest req) throws Exception{
		// URL => http://localhost:8090/YHTXX/if/ve/Coupang/CoupangRequest/pccc/error/send
		String entCode = null;
		Map<String, Object> recvPccc = new HashMap<String, Object>();
		try {
			// 운영 구분
			String receiver = "";
			log.debug("interface:data::>"+pccc.toString());
			String senderName = profile.toUpperCase();
			log.debug("interface:data::>"+senderName);
			if(senderName.contains("BMS12") || senderName.contains("HUBN2")){
	    		receiver = receiverProd;
	    	}else {
	    		receiver = receiverDev;
	    	}
			
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, receiver));
			recvPccc.put("gubun", "");
			recvPccc.put("pccc", pccc);
		}catch (Exception e) {
//			InterfaceResultVE interfaceResultVE = new InterfaceResultVE();
			InterfaceReturnDataPcccVE returnData = new InterfaceReturnDataPcccVE();
			returnData.setCode("FAIL");
			returnData.setMessage("Not a Service Provider");
			return new ResponseEntity<InterfaceReturnDataPcccVE>(returnData, HttpStatus.OK);
		}
		
		Util.setDB(req, entCode);
		return new ResponseEntity<InterfaceReturnDataPcccVE>(interfaceServiceVE.coupangOrderPcccInfo(recvPccc, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "쿠팡 PCCC 취소 수신 API", notes = "쿠팡 PCCC 취소 수신 처리합니다.")
	@RequestMapping(value = {"/Coupang/CoupangRequest/pccc/error/cancel"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<InterfaceReturnDataPcccVE> coupangOrderPcccCancelInfo(HttpServletRequest req) throws Exception{
		// URL => http://localhost:8090/YHTXX/if/ve/Coupang/CoupangRequest/pccc/error/cancel?requestId=xxxx
		CoupangPccc pccc = new CoupangPccc();
		String entCode = null;
		Map<String, Object> recvPccc = new HashMap<String, Object>();
		try {
			// 운영 구분
			String receiver = "";
			String requestId = req.getParameter("requestId");
			log.debug("interface:data::>"+requestId);
			String senderName = profile.toUpperCase();
			log.debug("interface:data::>"+senderName);
			if(senderName.contains("BMS12") || senderName.contains("HUBN2")){
	    		receiver = receiverProd;
	    	}else {
	    		receiver = receiverDev;
	    	}
			
			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, receiver));
			
//			entCode = Util.null2str(Enum.valueOf(BranchEnum.class, "YHTXX"));
			pccc.setRequestId(requestId);
			recvPccc.put("gubun", "cancel");
			recvPccc.put("pccc", pccc);
		}catch (Exception e) {
//			InterfaceResultVE interfaceResultVE = new InterfaceResultVE();
			InterfaceReturnDataPcccVE returnData = new InterfaceReturnDataPcccVE();
			returnData.setCode("FAIL");
			returnData.setMessage("Not a Service Provider");
			return new ResponseEntity<InterfaceReturnDataPcccVE>(returnData, HttpStatus.OK);
		}
		
		Util.setDB(req, entCode);
		return new ResponseEntity<InterfaceReturnDataPcccVE>(interfaceServiceVE.coupangOrderPcccInfo(recvPccc, req), HttpStatus.OK);
	}
}
