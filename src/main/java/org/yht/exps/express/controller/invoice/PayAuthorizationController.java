package org.yht.exps.express.controller.invoice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.yht.exps.express.service.invoice.PayService;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Controller
@Api(tags = "PayAuthorization")
@RequestMapping("/{entCode}/payAuthorization")
public class PayAuthorizationController {
	
	@Resource(name = "payService")
	PayService payService;
	
	
	@ApiOperation(value = "정산서 이지페이 승인요청", notes="정산서 이지페이 승인요청합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/easypay", method = RequestMethod.POST)
	public String easypayApproval(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
		return payService.easypayApproval(entCode, request);
	}
}
