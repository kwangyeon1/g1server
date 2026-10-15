package org.yht.exps.express.controller.invoice;

import java.util.List;

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
import org.yht.exps.express.model.invoice.search.Easypay;
import org.yht.exps.express.service.invoice.PayService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Pay")
@RequestMapping("/{entCode}/pay")
public class PayController {
	
	@Resource(name = "payService")
	PayService payService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	
	@ApiOperation(value = "정산서 이지페이 거래등록", notes="정산서 이지페이 거래등록합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/easypay/registration", method = RequestMethod.POST)
	public ResponseEntity<Easypay> easypayRegistration(@PathVariable("entCode") String entCode, @RequestBody Easypay easypay, HttpServletRequest request) throws Exception {
		easypay.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, easypay, "userId", "logUserName", null, "logUserNameEng", true, false, false, false);
		return new ResponseEntity<Easypay>(payService.easypayRegistration(easypay, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "정산서 이지페이 결제 결과조회", notes="정산서 이지페이 결제 결과조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "shopOrderNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/easypay/result/get/{shopOrderNo}", method = RequestMethod.GET)
	public ResponseEntity<Easypay> getEasypayResult(@PathVariable("entCode") String entCode, @PathVariable("shopOrderNo") String shopOrderNo, Easypay easypay, HttpServletRequest request) throws Exception {
		easypay.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, easypay, null, "logUserName", null, "logUserNameEng");
		/** 2025-02-11 암호화된 shopOrderNo에 '/'가 있을경우 Bad Request오류 발생해서 추가함 */
		easypay.setShopOrderNo(shopOrderNo.replace("SVC-CSSV-062A", "/"));
		return new ResponseEntity<Easypay>(payService.getEasypayResult(easypay), HttpStatus.OK);
	}

	@ApiOperation(value = "정산서 이지페이 결제 청구서 조회", notes="정산서 이지페이 결제 청구서 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "srchBlNo", value = "비엘번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/easypay/invoice/get/{srchBlNo}", method = RequestMethod.GET)
	public ResponseEntity<List<Easypay>> getEasypayInvoice(@PathVariable("entCode") String entCode, @PathVariable("srchBlNo") String srchBlNo, Easypay easypay, HttpServletRequest request) throws Exception {
		easypay.setEntCode(entCode);
		easypay.setSrchBlNo(srchBlNo);
		Util.getSessionUserInfo(request, jwtTokenProvider, easypay, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<List<Easypay>>(payService.getEasypayInvoiceList(easypay), HttpStatus.OK);
	}
	
	@ApiOperation(value = "정산서 이지페이 노티처리", notes="정산서 이지페이 노티처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/easypay/noti", method = RequestMethod.POST)
	public ResponseEntity<String> easypayNoti(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
		return new ResponseEntity<String>(payService.easypayNoti(request), HttpStatus.OK);
	}
}
