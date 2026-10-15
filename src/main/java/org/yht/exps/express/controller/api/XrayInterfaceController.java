package org.yht.exps.express.controller.api;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.exps.express.model.api.InterfaceResult;
import org.yht.exps.express.model.api.LxpXrayIntf;
import org.yht.exps.express.service.api.InterfaceService;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "Api")
public class XrayInterfaceController {
	
	@Resource(name = "interfaceService")
	InterfaceService interfaceService;
	
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
}
