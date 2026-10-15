package org.yht.exps.express.controller.bms;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.bms.bmsOutNotice.BatchBmsOutNotice;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNotice;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticeDetail;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticeDetailList;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticeList;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticePlanList;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticeSaveResult;
import org.yht.exps.express.model.bms.bmsOutNotice.DeleteBmsOutNotice;
import org.yht.exps.express.model.bms.bmsOutNotice.search.SearchBmsOutNotice;
import org.yht.exps.express.model.bms.bmsStock.ExcelUploadCustomsStock;
import org.yht.exps.express.model.bms.disposal.InventoryInList;
import org.yht.exps.express.service.bms.BmsOutNoticeService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags="BMS")
@RequestMapping("/{entCode}/bms/outNotice")
public class BmsOutNoticeController {

	@Resource(name="bmsOutNoticeService")
	BmsOutNoticeService bmsOutNoticeService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	/** 반출통고 리스트 조회 */
	@ApiOperation(value = "반출통고 리스트 조회", notes = "반출통고 리스트를 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/list/notice", method = RequestMethod.GET)
	public ResponseEntity<BmsOutNoticeList> selectBmsOutNoticeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsOutNotice search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsOutNoticeList>(bmsOutNoticeService.selectBmsOutNoticeList(search), HttpStatus.OK);
	}

	/** 미승인재고 리스트 조회 */
	@ApiOperation(value = "미승인재고 리스트 조회", notes = "미승인재고 리스트를 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/list/stock", method = RequestMethod.GET)
	public ResponseEntity<InventoryInList> selectInventoryList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsOutNotice search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<InventoryInList>(bmsOutNoticeService.selectInventoryList(search), HttpStatus.OK);
	}

	/** 누락통보 리스트 조회 */
	@ApiOperation(value = "누락통보 리스트 조회", notes = "누락통보 리스트를 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/list/omitted", method = RequestMethod.GET)
	public ResponseEntity<BmsOutNoticePlanList> selectBmsOutNoticeOmittedList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsOutNotice search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsOutNoticePlanList>(bmsOutNoticeService.selectBmsOutNoticeOmittedList(search), HttpStatus.OK);
	}

	/** 등록 ENTRY - 상세 조회 */
	@ApiOperation(value = "등록 ENTRY - 상세 조회", notes="등록 ENTRY - 상세내역을 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/detail", method = RequestMethod.GET)
	public ResponseEntity<BmsOutNoticeDetail> selectBmsOutNoticeDetail(@PathVariable("entCode") String entCode, SearchBmsOutNotice search) throws Exception {
	   return new ResponseEntity<>(bmsOutNoticeService.selectBmsOutNoticeDetail(search), HttpStatus.OK);
	}

	/**	등록 ENTRY - 오류내역 조회 */
	@ApiOperation(value = "등록 ENTRY - 오류내역 조회", notes="등록 ENTRY - 오류내역을 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/get/errorDesc", method = RequestMethod.GET)
	public ResponseEntity<String> selectErrorDesc(@PathVariable("entCode") String entCode, String srchDeclarationNo) throws Exception {
		return new ResponseEntity<String>(bmsOutNoticeService.selectErrorDesc(srchDeclarationNo), HttpStatus.OK);
	}

	/** POPUP - 조회번호, 타입에 따라 데이터 조회 */
	@ApiOperation(value = "POPUP - 조회번호, 타입에 따라 데이터 조회", notes="POPUP - 조회번호, 타입에 따라 데이터를 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/list/bySrchNo", method = RequestMethod.GET)
	public ResponseEntity<BmsOutNoticeDetailList> selectListBySrchNo(@PathVariable("entCode") String entCode, SearchBmsOutNotice search) throws Exception {
		return new ResponseEntity<>(bmsOutNoticeService.selectListBySrchNo(search), HttpStatus.OK);
	}

	/** 등록 ENTRY - 반출통고 등록 or 수정 */
	@ApiOperation(value = "등록 ENTRY - 반출통고 등록 or 수정", notes="등록 ENTRY - 반출통고 데이터를 등록 or 수정합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/save", method = RequestMethod.PUT)
	public ResponseEntity<BmsOutNoticeSaveResult> saveBmsOutNotice(@PathVariable("entCode") String entCode, @RequestBody BmsOutNotice bmsOutNotice, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsOutNotice, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutNoticeSaveResult>(bmsOutNoticeService.saveBmsOutNotice(bmsOutNotice, request), HttpStatus.OK);
	}
	
	/** 반출통고 출력여부 수정 */
	@ApiOperation(value = "반출통고 출력여부 수정", notes="반출통고 출력여부 수정합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/updatePrintYn", method = RequestMethod.PUT)
	public ResponseEntity<BmsOutNoticeSaveResult> updatePrintYn(@PathVariable("entCode") String entCode, @RequestBody BmsOutNoticeList bmsOutNoticeList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsOutNoticeList, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutNoticeSaveResult>(bmsOutNoticeService.updatePrintYn(bmsOutNoticeList), HttpStatus.OK);
	}

	/** 반출통고 삭제 */
	@ApiOperation(value = "반출통고 삭제", notes="반출통고 데이터를 삭제합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/delete", method = RequestMethod.DELETE)
	public ResponseEntity<BmsOutNoticeSaveResult> deleteBmsOutNotice(@PathVariable("entCode") String entCode, @RequestBody DeleteBmsOutNotice delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsOutNoticeSaveResult>(bmsOutNoticeService.deleteBmsOutNotice(delete, request), HttpStatus.OK);
	}
	
    @ApiOperation(value = "반출통고 오류 일괄해제", notes="반출통고 오류 일괄해제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/updatePodStatus", method = RequestMethod.POST)
    public ResponseEntity<BmsOutNoticeSaveResult> updatePodStatus(@PathVariable("entCode") String entCode, @RequestBody BmsOutNoticeList bmsOutNoticeList, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, bmsOutNoticeList, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
        return new ResponseEntity<BmsOutNoticeSaveResult>(bmsOutNoticeService.updatePodStatusFromBmsOutNotice(bmsOutNoticeList, request), HttpStatus.OK);
    }

	/** 관세청 XML 송신(Send) - GOVCBR5II(반출통고목록보고서) */
	@ApiOperation(value = "관세청 XML 송신(Send)", notes = "GOVCBR5II(목록보고서) 목록을 전송합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/exec/send/bmsOutNoticeList", method = RequestMethod.POST)
	public ResponseEntity<BmsOutNoticeSaveResult> sendOutToXml(@PathVariable("entCode") String entCode, @RequestBody BatchBmsOutNotice params, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, params, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		params.setEntCode(entCode);
		return new ResponseEntity<BmsOutNoticeSaveResult>(bmsOutNoticeService.sendOutToXml(params, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "반출통고 ExcelUpload", notes="반출통고 ExcelUpload합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "excelUploadType", value = "엑셀타입", required = true, dataType = "string", paramType = "path", defaultValue = "bmsOutNotice")
    })
    @RequestMapping(value="/excelUpload/check/{excelUploadType}", method = RequestMethod.POST)
    public ResponseEntity<BmsOutNoticeSaveResult> checkExcelUploadCheck(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
        return new ResponseEntity<BmsOutNoticeSaveResult>(bmsOutNoticeService.checkExcelUpload(uploadExcel, entCode, excelUploadType), HttpStatus.OK);
    }

    @ApiOperation(value = "반출통고 Excel 저장", notes="반출통고 Excel 저장합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/excelUpload/save/{excelUploadType}", method = RequestMethod.POST)
    public ResponseEntity<BmsOutNoticeSaveResult> saveExcelUpload(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestBody ExcelUploadCustomsStock excelUploadCustomsStock, HttpServletRequest request) throws Exception {
        return new ResponseEntity<BmsOutNoticeSaveResult>(bmsOutNoticeService.saveExcelUpload(excelUploadCustomsStock, request, excelUploadType), HttpStatus.OK);
    }

	/**
	 * 엑셀 다운로드
	 * @param entCode
	 * @param search
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "반출통고 리스트 엑셀 다운로드", notes = "반출통고 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/list/notice/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBmsOutNoticeExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOutNotice search, HttpServletRequest req) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutNoticeService.selectBmsOutNoticeList(search).getBmsOutNoticeList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "미승인재고 리스트 엑셀 다운로드", notes = "미승인재고 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/list/stock/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectInventoryExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOutNotice search, HttpServletRequest req) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutNoticeService.selectInventoryList(search).getInventoryInList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "누락통보 리스트 엑셀 다운로드", notes = "누락통보 리스트를 엑셀로 다운로드합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/list/omitted/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBmsOutNoticeOmittedExcelList(@PathVariable("entCode") String entCode, @RequestBody SearchBmsOutNotice search, HttpServletRequest req) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsOutNoticeService.selectBmsOutNoticeOmittedList(search).getBmsOutNoticePlanList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

}