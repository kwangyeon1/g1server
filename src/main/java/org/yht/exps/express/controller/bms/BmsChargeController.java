package org.yht.exps.express.controller.bms;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.bms.charge.*;
import org.yht.exps.express.model.bms.charge.search.SearchBmsRate;
import org.yht.exps.express.model.bms.charge.search.SearchTcrNo;
import org.yht.exps.express.model.bms.disposal.InventoryInList;
import org.yht.exps.express.model.bms.out.BmsOutList;
import org.yht.exps.express.model.bms.out.BmsOutPlanList;
import org.yht.exps.express.model.bms.out.BmsOutSaveResult;
import org.yht.exps.express.model.bms.out.DeleteBmsOut;
import org.yht.exps.express.model.bms.out.search.SearchBmsOut;
import org.yht.exps.express.service.bms.BmsChargeService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags="BMS")
@RequestMapping("/{entCode}/bms/charge")
public class BmsChargeController {

	@Resource(name="bmsChargeService")
	BmsChargeService bmsChargeService;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@ApiOperation(value = "정산료 계산 - 재고 리스트 조회", notes="정산료 계산 - 재고 리스트 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inventory/search/list", method = RequestMethod.GET)
	public ResponseEntity<InventoryInList> selectInventoryInList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<InventoryInList>(bmsChargeService.selectEstiInventoryInList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "정산료 계산 - 반출 리스트 조회", notes="정산료 계산 - 반출 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryout/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsOutList> selectBmsOutList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsOutList>(bmsChargeService.selectEstiBmsOutList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "정산료 계산 - 반출승인/승인취하 리스트 조회", notes="정산료 계산 - 반출승인/승인취하 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/approvalYn/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsOutPlanList> selectBmsOutPlanList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsOutPlanList>(bmsChargeService.selectEstiBmsOutPlanList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "정산료 계산 - 반출승인/승인취하 리스트 조회", notes="정산료 계산 - 반출승인/승인취하 리스트 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/esti/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsEstimateList> selectBmsEstimateList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsEstimateList>(bmsChargeService.selectEstiBmsEstimateList(search), HttpStatus.OK);
	}

	@RequestMapping(value="/esti/getInNoAndApplyClassFromEstimate", method = RequestMethod.GET)
	public ResponseEntity<BmsEstimateGroupingList> getInNoAndApplyClassFromEstimate(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsEstimateGroupingList>(bmsChargeService.getInNoAndApplyClassFromEstimate(search), HttpStatus.OK);
	}
	
	@RequestMapping(value="/getBmsOutListByMrnMsnOfCargoNo", method = RequestMethod.GET)
	public ResponseEntity<Map> getBmsOutListByMrnMsnOfCargoNo(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String, Object> param, HttpServletRequest req) throws Exception {
		return new ResponseEntity<>(bmsChargeService.getBmsOutListByMrnMsnOfCargoNo(param), HttpStatus.OK);
	}
	
	@RequestMapping(value="/esti/getTcrNo", method = RequestMethod.GET)
	public ResponseEntity<TcrNoSettingResult> getTcrNo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchTcrNo search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<TcrNoSettingResult>(bmsChargeService.getTcrNo(search), HttpStatus.OK);
	}
	
	@RequestMapping(value="/esti/updateTcrNo", method = RequestMethod.PUT)
	public ResponseEntity<TcrNoSettingResult> updateTcrNo(@PathVariable("entCode") String entCode, @RequestBody SearchTcrNo search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		
		search.setSrchEntCode(entCode);
		return new ResponseEntity<TcrNoSettingResult>(bmsChargeService.updateTcrNo(search, request), HttpStatus.OK);
	}
	/***
	 * 정산료 계산 그리드, 재고 그리드에서 상세 호출시에 사용하는 API로
	 *
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "정산료 계산 상세 한건 조회", notes="폐기 신청 상세 화면 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get", method = RequestMethod.GET)
	public ResponseEntity<EstimateDetail> selectBmsOutInventoryDetail(@PathVariable("entCode") String entCode, SearchBmsOut search) throws Exception {
	   return new ResponseEntity<>(bmsChargeService.selectEstiDetail(search), HttpStatus.OK);
	}
	@ApiOperation(value = "정산료 계산 BL No  조회", notes="폐기 신청 정산료 계산 BL No  상세 화면 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getBmsEstimateByBlNo", method = RequestMethod.GET)
	public ResponseEntity<EstimateDetailList> getBmsOutByBlNo(@PathVariable("entCode") String entCode, SearchBmsOut search) throws Exception {
		//search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectEstiBmsOutByBlNo(search), HttpStatus.OK);
	}

	@ApiOperation(value = "정산료 계산시 사용하는 휴일 계산 _ 기상청 API 사용", notes="휴일 계산 _ 기상청 API 사용 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getHodiday", method = RequestMethod.GET)
	public ResponseEntity<EstimateDetailList> getHodiday(@PathVariable("entCode") String entCode, SearchBmsOut search) throws Exception {
		//search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectEstiBmsOutByBlNo(search), HttpStatus.OK);
	}
	@ApiOperation(value = "정산료 계산  insert", notes="정산료 계산을 insert합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/esti/insert", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> insertBmsEstimate(@PathVariable("entCode") String entCode, @RequestBody BmsEstimate bmsEstimate, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsEstimate, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.insertBmsEstimate(bmsEstimate, request), HttpStatus.OK);
	}
	@RequestMapping(value="/esti/caltax", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> calTax(@PathVariable("entCode") String entCode, @RequestBody BmsEstimate bmsEstimate, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsEstimate, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.insertCalTax(bmsEstimate, request), HttpStatus.OK);
	}

	@ApiOperation(value = "정산료 계산 update", notes="정산료 계산을 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
			//@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/esti/update", method = RequestMethod.PUT)
	public ResponseEntity<BmsOutSaveResult> updateDisposal(@PathVariable("entCode") String entCode, @RequestBody BmsEstimate bmsEstimate, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsEstimate, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		//bmsOut.setOdJobNo(odJobNo);
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.updateBmsEstimate(bmsEstimate, request), HttpStatus.OK);
	}
	@ApiOperation(value = "정산료 계산 삭제  삭제", notes="정산료 계산 삭제합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/esti/delete", method = RequestMethod.DELETE)
	public ResponseEntity<BmsOutSaveResult> deleteBmsEstimate(@PathVariable("entCode") String entCode, @RequestBody DeleteBmsOut delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.deleteBmsEstimate(delete, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "정산료 일부필드 update", notes="정산료 일부필드를 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/esti/updateField", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> updateField(@PathVariable("entCode") String entCode, @RequestBody BmsEstimate bmsEstimate, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsEstimate, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.updateField(bmsEstimate, request), HttpStatus.OK);
	}

	@ApiOperation(value = "창고료  조회", notes = "창고료  -  리스트 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/storagerate/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsStorageRateList> selectBmsStorageRateList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsRate search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectBmsStorageRateList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "창고료  조회(팝업)", notes = "창고료  -  리스트 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/storagerate/search/get", method = RequestMethod.GET)
	public ResponseEntity<BmsStorageRateList> selectBmsStorageRateDetailList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsRate search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectBmsStorageRateDetailList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "보관료 계산  insert", notes="보관료 계산을 insert합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/storagerate/insert", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> insertBmsStorageRate(@PathVariable("entCode") String entCode, @RequestBody BmsRateData bmsRateData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsRateData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.insertBmsStorageRate(bmsRateData, request), HttpStatus.OK);
	}
	@ApiOperation(value = "보관료 계산 update", notes="보관료 계산을 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
			//@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/storagerate/update", method = RequestMethod.PUT)
	public ResponseEntity<BmsOutSaveResult> updateBmsStorageRate(@PathVariable("entCode") String entCode, @RequestBody BmsRateData bmsRateData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsRateData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");

		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.updateBmsStorageRate(bmsRateData, request), HttpStatus.OK);
	}
	@ApiOperation(value = "보관료 계산 삭제  삭제", notes="보관료 계산 삭제합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/storagerate/delete", method = RequestMethod.DELETE)
	public ResponseEntity<BmsOutSaveResult> deleteBmsStorageRate(@PathVariable("entCode") String entCode, @RequestBody DeleteEstimate delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.deleteBmsStorageRate(delete, request), HttpStatus.OK);
	}

	@ApiOperation(value = "작업료 조회", notes = "작업료 리스트 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/workrate/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsWorkRateList> selectBmsWorkRateList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsRate search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectBmsWorkRateList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "작업료 조회(팝업)", notes = "작업료 리스트 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/workrate/search/get", method = RequestMethod.GET)
	public ResponseEntity<BmsRateBase> selectBmsWorkRateDetail(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsRate search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectBmsWorkRateDetail(search), HttpStatus.OK);
	}
	@ApiOperation(value = "작업료 계산  insert", notes="작업료 계산을 insert합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/workrate/insert", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> insertBmsWorkRate(@PathVariable("entCode") String entCode, @RequestBody BmsWorkRate bmsWorkRate, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsWorkRate, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.insertBmsWorkRate(bmsWorkRate, request), HttpStatus.OK);
	}
	@ApiOperation(value = "작업료 계산 update", notes="작업료 계산을 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
			//@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/workrate/update", method = RequestMethod.PUT)
	public ResponseEntity<BmsOutSaveResult> updateDisposal(@PathVariable("entCode") String entCode, @RequestBody BmsWorkRate bmsWorkRate, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsWorkRate, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		//bmsOut.setOdJobNo(odJobNo);
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.updateBmsWorkRate(bmsWorkRate, request), HttpStatus.OK);
	}
	@ApiOperation(value = "작업료 계산   삭제", notes="작업료 계산 삭제합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/workrate/delete", method = RequestMethod.DELETE)
	public ResponseEntity<BmsOutSaveResult> deleteBmsWorkRate(@PathVariable("entCode") String entCode, @RequestBody DeleteEstimate delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.deleteBmsWorkRate(delete, request), HttpStatus.OK);
	}


	/**
	 * 요율구분에 따른 발행번호및 보관일수(기본값) 리턴
	 * @param search
	 * @return
	 */
	@ApiOperation(value = "요율구분에 따른 발행번호및 보관일수 작업요율, 보관료율(기본값) ", notes = "요율구분에 따른 발행번호및 보관일수(기본값) 리턴.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/base/applyNoAndWorkDaysBaseRate", method = RequestMethod.GET)
	public ResponseEntity<BmsRateBase> selectBmsApplyNoAndWorkDaysBaseRate(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsRate search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectBmsApplyNoAndWorkDaysBaseRate(search), HttpStatus.OK);
	}


	@ApiOperation(value = "휴일 조회(팝업)", notes = "휴일 리스트 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/holiday/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsWorkRateList> selectBmsHoliday(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsRate search, HttpServletRequest req) throws Exception {
		//search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectBmsHolidayList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "휴일 계산  insert", notes="휴일 계산을 save(Insert,update)합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/holiday/save", method = RequestMethod.POST)
	public ResponseEntity<BmsOutSaveResult> saveHoliday(@PathVariable("entCode") String entCode, @RequestBody DeleteEstimate saveData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, saveData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//bmsOut.setEntCode(entCode);
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.saveBmsHoliday(saveData, request), HttpStatus.OK);
	}
	@ApiOperation(value = "휴일 삭제", notes="휴일  delete합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
			//@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/holiday/delete", method = RequestMethod.DELETE)
	public ResponseEntity<BmsOutSaveResult> deleteBmsHoliday(@PathVariable("entCode") String entCode, @RequestBody DeleteEstimate delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutSaveResult>(bmsChargeService.deleteBmsHoliday(delete, request), HttpStatus.OK);
	}



	@ApiOperation(value = "반출 엑셀 list 다운로드", notes="반출 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/out/search/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectOutExcel(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		BmsOutList bmsOutList  = bmsChargeService.selectEstiBmsOutList(search);

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutList.getBmsOutList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	@ApiOperation(value = "반출승인 엑셀 list 다운로드", notes="반출승인 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/outplan/search/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectOutPlanExcel(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		BmsOutPlanList bmsOutPlanList  = bmsChargeService.selectEstiBmsOutPlanList(search);

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutPlanList.getBmsOutPlanList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}
	@ApiOperation(value = "재고 엑셀 list 다운로드", notes="재고 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/stock/search/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectStockExcel(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		InventoryInList inventoryInList  = bmsChargeService.selectEstiInventoryInList(search);

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", inventoryInList.getInventoryInList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	@ApiOperation(value = "창고료 엑셀 list 다운로드", notes="창고료 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/esti/search/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectCarryInExcel(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOut search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		BmsEstimateList bmsEstimateList  = bmsChargeService.selectEstiBmsEstimateList(search);

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsEstimateList.getBmsEstimatesList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	@ApiOperation(value = "창고료  날짜 콤보조회", notes = "창고료  날짜 콤보조회  조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/storagerate/search/dateCombo", method = RequestMethod.GET)
	public ResponseEntity<List<BmsStorageRate>> selectChargeStorageRateCombo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsRate search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectChargeStorageRateCombo(), HttpStatus.OK);
	}

	@ApiOperation(value = "작업료  날짜 콤보조회", notes = "작업료  날짜 콤보조회  조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/workrate/search/dateCombo", method = RequestMethod.GET)
	public ResponseEntity<List<BmsWorkRate>> selectChargeWorkRateCombo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsRate search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectChargeWorkRateCombo(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "화주 조회(팝업)", notes = "창고료정산팝업 - 화주팝업 - 화주를 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/getCustomerInfo", method = RequestMethod.GET)
	public ResponseEntity<BmsCustomerChargeInfo> selectBmsChargeCustomerDetail(@PathVariable("entCode") String entCode, @QueryStringArgResolver BmsCustomerChargeInfo search, HttpServletRequest req) throws Exception {
		search.setEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.selectBmsChargeCustomerDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "화주 저장(팝업)", notes = "창고료정산팝업 - 화주팝업 - 화주를 신규입력합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/customerInfo/insert", method = RequestMethod.POST)
	public ResponseEntity<BmsCustomerChargeInfo> insertBmsChargeCustomerDetail(@PathVariable("entCode") String entCode, @RequestBody BmsCustomerChargeInfo search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		
		search.setEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.insertBmsChargeCustomerDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "화주 수정(팝업)", notes = "창고료정산팝업 - 화주팝업 - 화주정보를 수정합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/customerInfo/update", method = RequestMethod.PUT)
	public ResponseEntity<BmsCustomerChargeInfo> updateBmsChargeCustomerDetail(@PathVariable("entCode") String entCode, @RequestBody BmsCustomerChargeInfo search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		search.setEntCode(entCode);
		return new ResponseEntity<>(bmsChargeService.updateBmsChargeCustomerDetail(search), HttpStatus.OK);
	}
}
