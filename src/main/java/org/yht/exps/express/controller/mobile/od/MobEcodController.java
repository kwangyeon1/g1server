package org.yht.exps.express.controller.mobile.od;


import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.exps.express.model.mobile.od.MobEcod;
import org.yht.exps.express.model.mobile.od.MobEcodList;
import org.yht.exps.express.model.mobile.od.MobEcodMulti;
import org.yht.exps.express.model.mobile.od.MobSaveResult;
import org.yht.exps.express.model.mobile.od.search.SearchMobEcod;
import org.yht.exps.express.service.mobile.od.MobEcodService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "mobile")
@RequestMapping("/{entCode}/ecm/mob/order")
public class MobEcodController {
	@Value("${spring.profiles.active}")
	private String profile;
	@Resource(name = "mobEcodService")
	MobEcodService mobEcodService;	
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "전자상거래 Order 상세 조회", notes="전자상거래  Order 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/{hblNo}", method = RequestMethod.GET)
	public ResponseEntity<MobEcod> selectOrderDetail(@PathVariable("entCode") String entCode, SearchMobEcod search) throws Exception {
		return new ResponseEntity<MobEcod>(mobEcodService.selectOrderDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Order 출고처리 조회", notes="전자상거래 Order 출고처리 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getCarryout/{hblNo}", method = RequestMethod.GET)
	public ResponseEntity<MobEcod> selectOrderDetail2(@PathVariable("entCode") String entCode, SearchMobEcod search) throws Exception {
		return new ResponseEntity<MobEcod>(mobEcodService.selectOrderDetail2(search), HttpStatus.OK);
	}

	
	@ApiOperation(value = "전자상거래 Order 상세 조회", notes="전자상거래  Order 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getByList/{hblNo}", method = RequestMethod.GET)
	public ResponseEntity<MobEcod> selectOrderDetailByList(@PathVariable("entCode") String entCode, SearchMobEcod search) throws Exception {
		return new ResponseEntity<MobEcod>(mobEcodService.selectOrderDetailByList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Order 출고처리 list 조회", notes="전자상거래  Order 출고처리 list 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/ByCarryoutList", method = RequestMethod.GET)
	public ResponseEntity<MobEcodList> selectOrderDetailByCarryoutList(@PathVariable("entCode") String entCode, SearchMobEcod search) throws Exception {
		return new ResponseEntity<MobEcodList>(mobEcodService.selectOrderDetailByCarryoutList(search), HttpStatus.OK);
	}
		
	@ApiOperation(value = "전자상거래 Order 출고처리", notes="Order 출고처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryout/update", method = RequestMethod.PUT)
	public ResponseEntity<MobSaveResult> carryoutSave(@PathVariable("entCode") String entCode, @RequestBody MobEcod mobEcod) throws Exception {
		return new ResponseEntity<MobSaveResult>(mobEcodService.carryoutSave(mobEcod), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Order 출고처리 save", notes="Order 출고처리 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryout/batch/update", method = RequestMethod.PUT)
	public ResponseEntity<MobSaveResult> carryoutBatchSave(@PathVariable("entCode") String entCode, @RequestBody MobEcod mobEcod, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, mobEcod, "carryoutUserId", null, null);
		return new ResponseEntity<MobSaveResult>(mobEcodService.carryoutBatchSave(mobEcod), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 로케이션 재고 list 조회", notes="로케이션 재고 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/inventory/list", method = RequestMethod.GET)
	public ResponseEntity<MobEcodList> selectInventoryList(@PathVariable("entCode") String entCode, SearchMobEcod search) throws Exception {
		return new ResponseEntity<MobEcodList>(mobEcodService.selectInventoryList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Order 재고확인", notes="Order 재고확인합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inventory/check", method = RequestMethod.PUT)
	public ResponseEntity<MobEcod> inventoryCheck(@PathVariable("entCode") String entCode, @RequestBody MobEcod mobEcod, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, mobEcod, "stockUserId", null, null);
		return new ResponseEntity<MobEcod>(mobEcodService.inventoryCheck(mobEcod), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Order 재고등록", notes = "Order 재고등록합니다.")
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value = "/inventory/update", method = RequestMethod.PUT)
	public ResponseEntity<List<MobEcod>> inventorySave(@PathVariable("entCode") String entCode, @RequestBody MobEcod mobEcod, HttpServletRequest request) throws Exception {
		if(!Util.isEmpty(mobEcod.getStockUserId())){
			Util.getSessionUserInfo(request, jwtTokenProvider, mobEcod, "stockUserId", null, null);
		}
	    List<MobEcod> result = mobEcodService.inventorySave(mobEcod); // inventorySave 메서드 수정하여 배열 처리
	    
	    return new ResponseEntity<List<MobEcod>>(result, HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래  중량화물 Order Multi 상세 조회", notes="전자상거래  중량화물 Order Multi 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getByMulti/{hblNo}", method = RequestMethod.GET)
	public ResponseEntity<MobEcod> selectOrderDetailByMulti(@PathVariable("entCode") String entCode, SearchMobEcod search) throws Exception {
		return new ResponseEntity<MobEcod>(mobEcodService.selectOrderDetailByMulti(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 멀티(중량) List save", notes="전자상거래 멀티(중량) List 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/saveMultiWgList", method = RequestMethod.POST)
	public ResponseEntity<MobSaveResult> saveMultiWgList(@PathVariable("entCode") String entCode, @RequestBody MobEcod mobEcod) throws Exception {
		return new ResponseEntity<MobSaveResult>(mobEcodService.saveMultiWgList(mobEcod), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 멀티(중량) List delete", notes="전자상거래 멀티(중량) List 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multi/delete", method = RequestMethod.POST)
	public ResponseEntity<MobSaveResult> deleteOrderMulti(@PathVariable("entCode") String entCode, @RequestBody MobEcod mobEcod) throws Exception {
		return new ResponseEntity<MobSaveResult>(mobEcodService.deleteOrderMulti(mobEcod), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 멀티(중량) 인계정보 delete", notes="전자상거래 멀티(중량) 인계정보 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multi/receipt/update", method = RequestMethod.POST)
	public ResponseEntity<MobSaveResult> updateOrderReceiptMulti(@PathVariable("entCode") String entCode, @RequestBody MobEcod mobEcod, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, mobEcod, "recipientUserId", null, null);
		return new ResponseEntity<MobSaveResult>(mobEcodService.updateOrderReceiptMulti(mobEcod), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 멀티(중량) 일괄 List save", notes="전자상거래 멀티(중량) List 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multi/batch/update", method = RequestMethod.POST)
	public ResponseEntity<MobSaveResult> multiBatchSave(@PathVariable("entCode") String entCode, @RequestBody MobEcod mobEcod) throws Exception {
		return new ResponseEntity<MobSaveResult>(mobEcodService.multiBatchSave(mobEcod), HttpStatus.OK);
	}
		
	@ApiOperation(value = "전자상거래 멀티(중량) List 인계  or 취소 save", notes="전자상거래 멀티(중량) List 인계  or 취소 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multi/saveReceiptList", method = RequestMethod.POST)
	public ResponseEntity<MobSaveResult> saveReceiptList(@PathVariable("entCode") String entCode, @RequestBody MobEcod mobEcod, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, mobEcod, "recipientUserId", null, null);
		return new ResponseEntity<MobSaveResult>(mobEcodService.saveReceiptList(mobEcod), HttpStatus.OK);
	}
	
	
	/** 멀티(중량) 일괄 등록 화면 조회 */
	@ApiOperation(value = "전자상거래 일괄 등록 화면 조회 detail", notes="전자상거래 일괄 등록 List 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multi/get/{hblNo}", method = RequestMethod.GET)
	public ResponseEntity<MobEcod> selectOrderListByMulti(@PathVariable("entCode") String entCode, SearchMobEcod search) throws Exception {
		return new ResponseEntity<MobEcod>(mobEcodService.selectOrderListByMulti(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 일괄 등록 List save", notes="전자상거래 일괄 등록 List 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multi/saveMultiList", method = RequestMethod.POST)
	public ResponseEntity<MobSaveResult> saveMultiList(@PathVariable("entCode") String entCode, @RequestBody MobEcod mobEcod, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, mobEcod, "registerUserId", null, null);
		return new ResponseEntity<MobSaveResult>(mobEcodService.saveMultiList(mobEcod), HttpStatus.OK);
	}
	
	/** 멀티(중량) 건별 등록 화면 조회 */
	@ApiOperation(value = "전자상거래 건별 등록 화면 조회 detail", notes="전자상거래 건별 등록 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/case/get/{hblNo}", method = RequestMethod.GET)
	public ResponseEntity<MobEcod> selectCaseOrderDetail(@PathVariable("entCode") String entCode, SearchMobEcod search) throws Exception {
		return new ResponseEntity<MobEcod>(mobEcodService.selectCaseOrderDetail(search), HttpStatus.OK);
	}
	
	/**
	 * 건별 등록 화면 divide value 조회
	 * @param map
	 * @throws Exception
	 */
	@ApiOperation(value = "전자상거래 건별 등록 화면 divide value 조회", notes="전자상거래 건별 등록 divide value")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/case/selectDivideValue", method = RequestMethod.POST)
	public String selectDivideValue(@PathVariable("entCode") String entCode,   @RequestBody SearchMobEcod search) throws Exception {
		return mobEcodService.selectDivideValue(search);
	}
	
	@ApiOperation(value = "전자상거래 건별 등록 List save", notes="전자상거래 건별 등록 List 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/multi/saveCaseList", method = RequestMethod.POST)
	public ResponseEntity<MobSaveResult> saveCaseList(@PathVariable("entCode") String entCode, @RequestBody MobEcodMulti mobEcodMulti, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, mobEcodMulti, "registerUserId", null, null);

		//변경배송사로 배송접수하는 로직 추가(현재는 경동만 적용)
		MobSaveResult result = mobEcodService.saveCaseList(mobEcodMulti);

		// 서비스가 null을 반환한 경우(multiList null 등) 방어
		if (result == null) {
			result = new MobSaveResult();
			result.setResultCode("FAIL");
			result.setChkContent("저장할 데이터가 없습니다.");
			return new ResponseEntity<MobSaveResult>(result, HttpStatus.OK);
		}
		//프린트용pdf
		if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")){
			if("SUCCESS".equals(result.getResultCode())){
			mobEcodService.savePrintInfo(result.getPrintParamMap(),mobEcodMulti.getChkPrintPoint());
			}
		}
		return new ResponseEntity<MobSaveResult>(result, HttpStatus.OK);
	}
}
