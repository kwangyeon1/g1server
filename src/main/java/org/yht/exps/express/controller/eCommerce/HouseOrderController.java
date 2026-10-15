package org.yht.exps.express.controller.eCommerce;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.mapper.eCommerce.PrintMapper;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrder;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderCommonList;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderDelete;
import org.yht.exps.express.model.eCommerce.houseOrder.HouseOrderSaveResult;
import org.yht.exps.express.model.eCommerce.houseOrder.search.SearchHouseOrder;
import org.yht.exps.express.model.eCommerce.houseOrder.search.SearchHouseOrderList;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.api.InterfaceService;
import org.yht.exps.express.service.eCommerce.HouseOrderService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/houseOrder")
public class HouseOrderController {
	
	@Resource(name = "houseOrderService")
	HouseOrderService houseOrderService;
	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name = "fileService")
	FileService fileService;
	@Resource(name = "interfaceService")
	InterfaceService interfaceService;
	
	@Autowired
	PrintMapper printMapper;
	
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "전자상거래 HouseOrder insert", notes="HouseOrder를 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<HouseOrderSaveResult> insertHouseOrder(@PathVariable("entCode") String entCode, @RequestBody HouseOrder houseOrder, HttpServletRequest request) throws Exception {
		houseOrder.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, houseOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		houseOrder.setRegisterUserId(houseOrder.getUpdateUserId());
		return new ResponseEntity<HouseOrderSaveResult>(houseOrderService.saveHouseOrder(houseOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 HouseOrder update", notes="HouseOrder를 update합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/update/{odJobNo}", method = RequestMethod.PUT)
	public ResponseEntity<HouseOrderSaveResult> updateHouseOrder(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo, @RequestBody HouseOrder houseOrder, HttpServletRequest request) throws Exception {
		houseOrder.setEntCode(entCode);
		houseOrder.setOdJobNo(odJobNo);
		Util.getSessionUserInfo(request, jwtTokenProvider, houseOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		houseOrder.setRegisterUserId(houseOrder.getUpdateUserId());
		return new ResponseEntity<HouseOrderSaveResult>(houseOrderService.saveHouseOrder(houseOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 HouseOrder 조회", notes="HouseOrder를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/get/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<HouseOrder> selectHouseOrder(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo, SearchHouseOrder search) throws Exception {
		search.setSrchEntCode(entCode);
		search.setSrchOdJobNo(odJobNo);
		return new ResponseEntity<HouseOrder>(houseOrderService.selectHouseOrder(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Order 공통 조회", notes="Order 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/orderCommon/search/list", method = RequestMethod.GET)
	public ResponseEntity<HouseOrderCommonList> selectHouseOrderListCommon(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchHouseOrderList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<HouseOrderCommonList>(houseOrderService.selectHouseOrderListCommon(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 HouseComOrder insert", notes="HouseComOrder를 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insertComOdHouse", method = RequestMethod.POST)
	public ResponseEntity<HouseOrderSaveResult> insertHouseComOrder(@PathVariable("entCode") String entCode, @RequestBody HouseOrder houseOrder, HttpServletRequest request) throws Exception {
		houseOrder.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, houseOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		houseOrder.setRegisterUserId(houseOrder.getUpdateUserId());
		return new ResponseEntity<HouseOrderSaveResult>(houseOrderService.insertHouseComOrder(houseOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 HouseOrder 삭제", notes="HouseOrder를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HouseOrderSaveResult> deleteHouseOrder(@PathVariable("entCode") String entCode, @RequestBody HouseOrderDelete orderDelete, HttpServletRequest request) throws Exception {
		orderDelete.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, orderDelete, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<HouseOrderSaveResult>(houseOrderService.deleteHouseOrder(orderDelete, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Master & House Order 삭제", notes="Master & Houser Order를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/deleteMH", method = RequestMethod.DELETE)
	public ResponseEntity<HouseOrderSaveResult> deleteHouseHouseOrder(@PathVariable("entCode") String entCode, @RequestBody HouseOrderDelete houseDelete, HttpServletRequest request) throws Exception {
		houseDelete.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, houseDelete, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<HouseOrderSaveResult>(houseOrderService.deleteHouseHouseOrder(houseDelete, request), HttpStatus.OK);
	}
}
