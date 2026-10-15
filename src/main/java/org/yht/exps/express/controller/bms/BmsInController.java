package org.yht.exps.express.controller.bms;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.mapper.bms.BmsInMapper;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.bms.carryin.*;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.bms.BmsInService;
import org.yht.exps.express.service.bms.CargoProgressInfoService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags="BmsInbm")
@RequestMapping("/{entCode}/bms/inbm")
public class BmsInController {

	@Resource(name="bmsInService")
	BmsInService bmsInService;

	@Resource(name = "commonService")
	CommonService commonService;

	@Resource(name ="cargoProgressInfoService")
	CargoProgressInfoService cargoProgressInfoService;

	@Autowired
	BmsInMapper bmsInMapper;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@ApiOperation(value = "반입신고 -  반입예정 리스트 조회", notes="반입신고 - 반입예정 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inPlan/search/list", method = RequestMethod.POST)
	public ResponseEntity<InPlanList> selectInPlanList(@PathVariable("entCode") String entCode, @RequestBody SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsInService.selectInPlanList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "반입신고 - 반입 리스트 조회", notes="반입신고 - 반입 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryIn/search/list", method = RequestMethod.POST)
	public ResponseEntity<CarryInList> selectCarryInList(@PathVariable("entCode") String entCode, @RequestBody SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsInService.selectCarryInList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반입신고 - 박스 리스트 조회", notes="반입신고 - 박스 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inPlanPkg/search/list", method = RequestMethod.POST)
	public ResponseEntity<InPlanPkgList> selectInPlanPkgList(@PathVariable("entCode") String entCode, @RequestBody SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsInService.selectInPlanPkgList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "반입신고 - 반입예정 상세 한건 조회", notes="반입신고 - 반입예정 상세 한건을 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inPlan/search/detailInPlan", method = RequestMethod.GET)
	public ResponseEntity<InPlan> selectDetailInPlan(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsInService.selectDetailInPlan(search), HttpStatus.OK);
	}

	@ApiOperation(value = "반입신고 - 반입 상세 한건 조회", notes="반입신고 - 반입 상세 한건을 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryIn/search/detailCarryIn", method = RequestMethod.GET)
	public ResponseEntity<CarryIn> selectDetailCarryIn(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsInService.selectDetailCarryIn(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반입신청  insert", notes="반입신청을 insert합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getCarryIn", method = RequestMethod.GET)
	public ResponseEntity<CarryIn> insertCarryIn(@PathVariable("entCode") String entCode, SearchInList search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<CarryIn>(bmsInService.getCarryIn(search, request), HttpStatus.OK);
	}
	
	@RequestMapping(value="/updateCarryInChangeByCustoms", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> updateCarryInChangeByCustoms(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.updateCarryInChangeByCustoms(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반입신청 BL No  조회", notes="반입신청 반입신청 BL No  상세 화면 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getCarryInByBlNo", method = RequestMethod.GET)
	public ResponseEntity<CarryInDetailList> selectCarryInByBlNo(@PathVariable("entCode") String entCode, SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsInService.selectCarryInByBlNo(search), HttpStatus.OK);
	}

	@ApiOperation(value = "반입신청 번호타입에 따라 조회", notes="반입신청 반입신청 번호타입에 따라 조회  상세 화면 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getCarryInBySrchNo", method = RequestMethod.GET)
	public ResponseEntity<CarryInDetailList> selectCarryInBySrchNo(@PathVariable("entCode") String entCode, SearchInList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsInService.selectCarryInBySrchNo(search), HttpStatus.OK);
	}

	@ApiOperation(value = "반입신청  insert", notes="반입신청을 insert합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryIn/insert", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> insertCarryIn(@PathVariable("entCode") String entCode, @RequestBody CarryIn carryIn, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryIn, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.insertCarryIn(carryIn, request), HttpStatus.OK);
	}

	@ApiOperation(value = "반입신청 update", notes="반입신청을 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
	})
	@RequestMapping(value="/carryIn/update", method = RequestMethod.PUT)
	public ResponseEntity<CarryInSaveResult> updateCarryIn(@PathVariable("entCode") String entCode, @RequestBody CarryIn carryIn, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryIn, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.updateCarryIn(carryIn, request), HttpStatus.OK);
	}

	@ApiOperation(value = "반입신고팝업- 재고 - update", notes="반입신고팝업- 재고 - update")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
	})
	@RequestMapping(value="/carryIn/updateStock", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> updateCarryInStock(@PathVariable("entCode") String entCode, @RequestBody CarryIn carryIn, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryIn, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.updateCarryInStock(carryIn, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반입신고가 됬는지 POD확인", notes="입신고가 됬는지 POD확인")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/check632isSended", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> check632isSended(@PathVariable("entCode") String entCode, CarryIn carryIn) throws Exception {
		return new ResponseEntity<>(bmsInService.check632isSended(carryIn), HttpStatus.OK);
	}

	/***
	 * 2.특송장이고반출일괄생성 처리
	 * @param entCode
	 * @param batchCarryIn
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/carryIn/exec/batch", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> execBatchCarryIn(@PathVariable("entCode") String entCode, @RequestBody BatchCarryIn batchCarryIn, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, batchCarryIn, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		batchCarryIn.setEntCode(entCode);
		return new ResponseEntity<CarryInSaveResult>(bmsInService.executeBatchCarryIn(batchCarryIn, request), HttpStatus.OK);
	}
	
	@RequestMapping(value="/carryIn70/exec/batch", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> execBatchCarryIn70(@PathVariable("entCode") String entCode, @RequestBody BatchCarryIn batchCarryIn, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, batchCarryIn, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		batchCarryIn.setEntCode(entCode);
		return new ResponseEntity<CarryInSaveResult>(bmsInService.executeBatchCarryIn70(batchCarryIn, request), HttpStatus.OK);
	}
	
	@RequestMapping(value="/checkCarryInBatchTicCount", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> checkCarryInBatchTicCount(@PathVariable("entCode") String entCode, @RequestBody BatchCarryIn batchCarryIn, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, batchCarryIn, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		batchCarryIn.setEntCode(entCode);
		return new ResponseEntity<CarryInSaveResult>(bmsInService.checkCarryInBatchTicCount(batchCarryIn, request), HttpStatus.OK);
	}
	
	@RequestMapping(value="/ipr/updateIprFromBmsInAndPlan", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> updateIprFromBmsInAndPlan(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.updateIprFromBmsInAndPlan(listData, request), HttpStatus.OK);
	}
	
	@RequestMapping(value="/bigsize/updateBigSizeYnFromBmsInAndPlan", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> updateBigSizeYnFromBmsInAndPlan(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.updateBigSizeYnFromBmsInAndPlan(listData, request), HttpStatus.OK);
	}
	
	@RequestMapping(value="/isptCustoms/updateIsptCustomsYnFromBmsInAndPlan", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> updateIsptCustomsYnFromBmsInAndPlan(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		
		return new ResponseEntity<CarryInSaveResult>(bmsInService.updateIsptCustomsYnFromBmsInAndPlan(listData, request), HttpStatus.OK);
	}
	
	/**
	 * 접수통보후반입수정 반입접수체크
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/raq/checkLastPodStatus", method = RequestMethod.GET)
	public ResponseEntity<CarryInSaveResult> checkLastPodStatus(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String, Object> search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<CarryInSaveResult>(bmsInService.checkLastPodStatus(search, request), HttpStatus.OK);
	}
	
	/**
	 * 접수통보후반입수정 수량,중량수정
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/raq/modCtWt", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> modCtWt(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.modCtWt(search, request), HttpStatus.OK);
	}
	
	/**
	 * 접수통보후반입수정 반입삭제
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/raq/delCarryIn", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> delCarryIn(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.delCarryIn(search, request), HttpStatus.OK);
	}
	@RequestMapping(value="/raq/delCarryInOutCheck", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> delCarryInOutCheck(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.delCarryInOutCheck(search, request), HttpStatus.OK);
	}

	@ApiOperation(value = "화물관리번호 check", notes="신규입력시 화물관리번호 중복체크")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/checkCargoReferenceNo", method = RequestMethod.GET)
	public ResponseEntity<Integer> checkCargoReferenceNo(@PathVariable("entCode") String entCode, String cargoReferenceNo) throws Exception {
		return new ResponseEntity<>(bmsInService.checkCargoReferenceNo(cargoReferenceNo),HttpStatus.OK);
	}

	@ApiOperation(value = "반입예정 데이터 삭제", notes="반입예정 데이터를 삭제합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inPlan/delete", method = RequestMethod.DELETE)
	public ResponseEntity<CarryInSaveResult> deleteInPlan(@PathVariable("entCode") String entCode, @RequestBody DeleteInPlan delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.deleteInPlan(delete, request), HttpStatus.OK);
	}

	@ApiOperation(value = "반입신청 삭제", notes="반입신청 삭제합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryIn/delete", method = RequestMethod.DELETE)
	public ResponseEntity<CarryInSaveResult> deleteCarryIn(@PathVariable("entCode") String entCode, @RequestBody DeleteCarryIn delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.deleteCarryIn(delete, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반입신청 삭제", notes="반입신청 삭제합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/box/delete", method = RequestMethod.DELETE)
	public ResponseEntity<CarryInSaveResult> deleteBox(@PathVariable("entCode") String entCode, @RequestBody DeleteCarryIn delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.deleteBox(delete, request), HttpStatus.OK);
	}

	@ApiOperation(value = "반입에 존재하는 데이터인지 check", notes="반입에 존재하는 데이터인지 check")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/checkExistCarryInAndInPlan", method = RequestMethod.GET)
	public ResponseEntity<CarryInDetailList> checkExistCarryInAndInPlan(@PathVariable("entCode") String entCode, int inPlanNo) throws Exception {
		return new ResponseEntity<>(bmsInService.checkExistCarryInAndInPlan(inPlanNo),HttpStatus.OK);
	}
	
	@ApiOperation(value = "정정전원본 키 조회", notes="정정전원본 키 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getChangeDelInNoByCustomsReportNo", method = RequestMethod.GET)
	public ResponseEntity<String> getChangeDelInNoByCustomsReportNo(@PathVariable("entCode") String entCode, String customsReportNo) throws Exception {
		return new ResponseEntity<String>(bmsInService.getChangeDelInNoByCustomsReportNo(customsReportNo),HttpStatus.OK);
	}

	@ApiOperation(value = "오류통보메시지", notes="오류통보 메시지를 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getErrorNotification", method = RequestMethod.GET)
	public ResponseEntity<String> selectErrorNotification(@PathVariable("entCode") String entCode, String customsReportNo) throws Exception {
		return new ResponseEntity<String>(bmsInService.selectErrorNotification(customsReportNo),HttpStatus.OK);
	}
	
	/** 관세청 XML 송신(Send) - GOVCBR632(반입신고서) */
	@ApiOperation(value = "관세청 XML 송신(Send)", notes = "GOVCBR632(반입신고서) 목록을 전송합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/exec/send/bmsCarryInList", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> sendOutToXml(@PathVariable("entCode") String entCode, @RequestBody BatchCarryIn params, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, params, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		params.setEntCode(entCode);
		return new ResponseEntity<CarryInSaveResult>(bmsInService.sendOutToXml(params, request), HttpStatus.OK);
	}

	@ApiOperation(value = "MFN(화물있으나적하목록없음)자료생성", notes="MFN(화물있으나적하목록없음)자료생성 합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert/mfn", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> insertMfn(@PathVariable("entCode") String entCode, @RequestBody CarryIn carryIn, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryIn, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.insertMfn(carryIn, request), HttpStatus.OK);
	}

	@ApiOperation(value = "MGN(적하목록있으나화물없음)자료생성", notes="MGN(적하목록있으나화물없음)자료생성")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert/mgn", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> insertMgn(@PathVariable("entCode") String entCode, @RequestBody CarryIn carryIn, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryIn, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.insertMgn(carryIn, request), HttpStatus.OK);
	}

	@ApiOperation(value = "반입예정 엑셀 list 다운로드", notes="반입예정 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inPlan/search/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectInPlanExcel(@PathVariable("entCode") String entCode, @RequestBody SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		InPlanList inPlanList  = bmsInService.selectInPlanList(search);

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", inPlanList.getInPlanList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map, "xlsx"), HttpStatus.OK);
	}

	@ApiOperation(value = "반입 엑셀 list 다운로드", notes="반입 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryIn/search/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectCarryInExcel(@PathVariable("entCode") String entCode, @RequestBody SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		CarryInList carryInList  = bmsInService.selectCarryInList(search);
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", carryInList.getCarryInList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map, "xlsx"), HttpStatus.OK);
	}

	@ApiOperation(value = "재고 엑셀 list 다운로드", notes="재고 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/stock/search/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectStockExcel(@PathVariable("entCode") String entCode, @RequestBody SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		CarryInList carryInList  = bmsInService.selectCarryInList(search);

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", carryInList.getCarryInList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map, "xlsx"), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반입예정 엑셀 list 다운로드", notes="반입예정 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inPlanPkg/search/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectInPlanPkgExcel(@PathVariable("entCode") String entCode, @RequestBody SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		InPlanPkgList inPlanPkgList  = bmsInService.selectInPlanPkgList(search);

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", inPlanPkgList.getInPlanPkgList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map, "xlsx"), HttpStatus.OK);
	}
	
	@ApiOperation(value = "XRAY판독 엑셀 list 다운로드", notes="XRAY판독 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/carryIn/xray/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectXrayExcel(@PathVariable("entCode") String entCode, @RequestBody SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		List<Map<String, Object>> xrayResultList  = bmsInService.selectXrayResultForDownload(search);

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", xrayResultList);
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map, "xlsx"), HttpStatus.OK);
	}

	@ApiOperation(value = "반입신고 - 보류리스트 조회", notes="반입신고 - 보류 리스트를 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/withholding/search/list", method = RequestMethod.GET)
	public ResponseEntity<WithHoldingCheckResult> selectWithholdingList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsInService.selectWithholdingList(search), HttpStatus.OK);
	}
	
	/***
	 * A.일괄반입자료생성 처리
	 * @param entCode
	 * @param batchCarryIn
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/inPlan/exec/batch", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> execBatchInPlan(@PathVariable("entCode") String entCode, @RequestBody BatchCarryIn batchCarryIn, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, batchCarryIn, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		batchCarryIn.setEntCode(entCode);
		return new ResponseEntity<CarryInSaveResult>(bmsInService.executeBatchInPlanToIn(batchCarryIn, request), HttpStatus.OK);
	}
	
	/**
	 * 화물속성(5TL)재전송 팝업 화물조회
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/carryIn/checkLastPodResendStatus", method = RequestMethod.GET)
	public ResponseEntity<CarryInSaveResult> checkLastPodResendStatus(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String, Object> search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<CarryInSaveResult>(bmsInService.checkLastPodResendStatus(search, request), HttpStatus.OK);
	}
	
	/**
	 * 화물속성(5TL)재전송 처리
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/carryIn/resendLastPodStatus", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> resendLastPodStatus(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.resendLastPodStatus(search, request), HttpStatus.OK);
	}
	
	/**
	 * 화물속성(5TL)재전송 팝업 화물 여러건 조회
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/carryIn/checkLastPodResendStatusList", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> checkLastPodResendStatusList(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.checkLastPodResendStatusList(search, request), HttpStatus.OK);
	}
	
	/**
	 * 화물속성(5TL)재전송 여러건 처리
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/carryIn/resendLastPodStatusList", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> resendLastPodStatusList(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.resendLastPodStatusList(search, request), HttpStatus.OK);
	}
	
	/**
	 * 타창고하기 설정
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/carryIn/updatePodStatus", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> updatePodStatus(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.updatePodStatus(search, request), HttpStatus.OK);
	}
	
	/**
	 * 타창고하기 설정 여러건
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/carryIn/updatePodStatusList", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> updatePodStatusList(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.updatePodStatusList(listData, request), HttpStatus.OK);
	}
	
	/**
	 * 타창고하기 해제
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/carryIn/updatePodStatusClear", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> updatePodStatusClear(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.updatePodStatusClear(search, request), HttpStatus.OK);
	}
	
	/**
     * 일괄홀드 설정
     * 
     * @param entCode
     * @param search
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/carryIn/updateHoldOnList", method = RequestMethod.POST)
    public ResponseEntity<CarryInSaveResult> updateHoldOnList(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
        Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
        return new ResponseEntity<CarryInSaveResult>(bmsInService.updateHoldOnList(listData, request), HttpStatus.OK);
    }
    
    /**
     * 일괄홀드해제 설정
     * 
     * @param entCode
     * @param search
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/carryIn/updateHoldOnListClear", method = RequestMethod.POST)
    public ResponseEntity<CarryInSaveResult> updateHoldOnListClear(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> listData, HttpServletRequest request) throws Exception {
        Util.getSessionUserInfoBMS(request, jwtTokenProvider, listData, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
        return new ResponseEntity<CarryInSaveResult>(bmsInService.updateHoldOnListClear(listData, request), HttpStatus.OK);
    }
    
	/**
	 * X-Ray판독수량 초기화
	 * 
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/carryIn/updateXrayCtClear", method = RequestMethod.POST)
	public ResponseEntity<CarryInSaveResult> updateXrayCtClear(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CarryInSaveResult>(bmsInService.updateXrayCtClear(search, request), HttpStatus.OK);
	}
}
