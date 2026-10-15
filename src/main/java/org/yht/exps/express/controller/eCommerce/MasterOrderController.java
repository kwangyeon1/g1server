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
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrder;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderCommonList;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderDelete;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderSaveResult;
import org.yht.exps.express.model.eCommerce.masterOrder.search.SearchMasterOrder;
import org.yht.exps.express.model.eCommerce.masterOrder.search.SearchMasterOrderList;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.api.InterfaceService;
import org.yht.exps.express.service.eCommerce.MasterOrderService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/masterOrder")
public class MasterOrderController {
	
	@Resource(name = "masterOrderService")
	MasterOrderService masterOrderService;
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
	
	@ApiOperation(value = "전자상거래 MasterOrder insert", notes="MasterOrder를 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<MasterOrderSaveResult> insertMasterOrder(@PathVariable("entCode") String entCode, @RequestBody MasterOrder masterOrder, HttpServletRequest request) throws Exception {
		masterOrder.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, masterOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		masterOrder.setRegisterUserId(masterOrder.getUpdateUserId());
		return new ResponseEntity<MasterOrderSaveResult>(masterOrderService.saveMasterOrder(masterOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 MasterOrder update", notes="MasterOrder를 update합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/update/{odJobNo}", method = RequestMethod.PUT)
	public ResponseEntity<MasterOrderSaveResult> updateMasterOrder(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo, @RequestBody MasterOrder masterOrder, HttpServletRequest request) throws Exception {
		masterOrder.setEntCode(entCode);
		masterOrder.setOdJobNo(odJobNo);
		Util.getSessionUserInfo(request, jwtTokenProvider, masterOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		masterOrder.setRegisterUserId(masterOrder.getUpdateUserId());
		return new ResponseEntity<MasterOrderSaveResult>(masterOrderService.saveMasterOrder(masterOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 MasterOrder 조회", notes="MasterOrder를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/get/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<MasterOrder> selectMasterOrder(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo, SearchMasterOrder search) throws Exception {
		search.setSrchEntCode(entCode);
		search.setSrchOdJobNo(odJobNo);
		return new ResponseEntity<MasterOrder>(masterOrderService.selectMasterOrder(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Order 공통 조회", notes="Order 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/orderCommon/search/list", method = RequestMethod.GET)
	public ResponseEntity<MasterOrderCommonList> selectMasterOrderListCommon(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMasterOrderList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<MasterOrderCommonList>(masterOrderService.selectMasterOrderListCommon(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 MasterComOrder insert", notes="MasterComOrder를 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insertComOd", method = RequestMethod.POST)
	public ResponseEntity<MasterOrderSaveResult> insertMasterComOrder(@PathVariable("entCode") String entCode, @RequestBody MasterOrder masterOrder, HttpServletRequest request) throws Exception {
		masterOrder.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, masterOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		masterOrder.setRegisterUserId(masterOrder.getUpdateUserId());
		return new ResponseEntity<MasterOrderSaveResult>(masterOrderService.insertMasterComOrder(masterOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 MasterOrder 삭제", notes="MasterOrder를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete", method = RequestMethod.DELETE)
	public ResponseEntity<MasterOrderSaveResult> deleteMasterOrder(@PathVariable("entCode") String entCode, @RequestBody MasterOrderDelete orderDelete, HttpServletRequest request) throws Exception {
		orderDelete.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, orderDelete, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<MasterOrderSaveResult>(masterOrderService.deleteMasterOrder(orderDelete, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Master & House Order 삭제", notes="Master & Houser Order를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/deleteMH", method = RequestMethod.DELETE)
	public ResponseEntity<MasterOrderSaveResult> deleteMasterHouseOrder(@PathVariable("entCode") String entCode, @RequestBody MasterOrderDelete masterDelete, HttpServletRequest request) throws Exception {
		masterDelete.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, masterDelete, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<MasterOrderSaveResult>(masterOrderService.deleteMasterHouseOrder(masterDelete, request), HttpStatus.OK);
	}
}
