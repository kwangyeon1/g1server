package org.yht.exps.express.controller.bms;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.bms.disposal.*;
import org.yht.exps.express.model.bms.disposal.search.SearchDisposal;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.bms.BmsDisposalService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags="BMS")
@RequestMapping("/{entCode}/bms/disp")
public class BmsDisposalController {

	@Autowired
	private ResourceLoader resourceLoader;

	@Resource(name="bmsDisposalService")
	BmsDisposalService bmsDisposalService;

	@Autowired
	private CustomProperties customProperties;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@ApiOperation(value = "폐기신청 - 재고 리스트 조회", notes="폐기신청 - 재고 리스트 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/inventory/search/list", method = RequestMethod.GET)
	public ResponseEntity<InventoryInList> selectInventoryInList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchDisposal search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<InventoryInList>(bmsDisposalService.selectInventoryInList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "폐기신청 - 폐기 리스트 조회", notes="폐기신청 - 폐기 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/disposal/search/list", method = RequestMethod.GET)
	public ResponseEntity<DisposalList> selectDisposalList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchDisposal search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<DisposalList>(bmsDisposalService.selectDisposalList(search), HttpStatus.OK);
	}

	/***
	 * 폐기신청 그리드, 재고 그리드에서 상세 호출시에 사용하는 API로
	 *
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "폐기신청 상세 한건 조회", notes="폐기 신청 상세 화면 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get", method = RequestMethod.GET)
	public ResponseEntity<DisposalDetail> selectDisposalInventoryDetail(@PathVariable("entCode") String entCode, SearchDisposal search) throws Exception {
		//search.setSrchEntCode(entCode);
		//search.setSrchOdJobNo(odJobNo);
		 return new ResponseEntity<>(bmsDisposalService.selectDisposalInventoryDetail(search), HttpStatus.OK);
	}
	@ApiOperation(value = "폐기신청 BL No  조회", notes="폐기 신청 폐기신청 BL No  상세 화면 조회")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getDisposalByBlNo", method = RequestMethod.GET)
	public ResponseEntity<DisposalDetailList> selectDisposalByBlNo(@PathVariable("entCode") String entCode,  SearchDisposal search) throws Exception {
		//search.setSrchEntCode(entCode);
		return new ResponseEntity<>(bmsDisposalService.selectDisposalByBlNo(search), HttpStatus.OK);
	}

	@ApiOperation(value = "폐기신청  insert", notes="폐기신청을 insert합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/disposal/insert", method = RequestMethod.POST)
	public ResponseEntity<DisposalSaveResult> insertDisposal(@PathVariable("entCode") String entCode, @RequestBody Disposal disposal, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, disposal, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//disposal.setEntCode(entCode);
		return new ResponseEntity<DisposalSaveResult>(bmsDisposalService.insertDisposal(disposal, request), HttpStatus.OK);
	}
	@ApiOperation(value = "폐기신청 update", notes="폐기신청을 update합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
			//@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/disposal/update", method = RequestMethod.PUT)
	public ResponseEntity<DisposalSaveResult> updateDisposal(@PathVariable("entCode") String entCode,  @RequestBody Disposal disposal, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, disposal, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		//disposal.setEntCode(entCode);
		//disposal.setOdJobNo(odJobNo);
		return new ResponseEntity<DisposalSaveResult>(bmsDisposalService.updateDisposal(disposal, request), HttpStatus.OK);
	}
	@ApiOperation(value = "폐기신청  삭제", notes="폐기신청 삭제합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/disposal/delete", method = RequestMethod.DELETE)
	public ResponseEntity<DisposalSaveResult> deleteDisposal(@PathVariable("entCode") String entCode, @RequestBody DeleteDisposal delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<DisposalSaveResult>(bmsDisposalService.deleteDisposal(delete, request), HttpStatus.OK);
	}

	@ApiOperation(value = "폐기신청 - 폐기 리스트 조회", notes="폐기신청 - 폐기 리스트 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})

	@RequestMapping(value="/disposal/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectDisposalListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchDisposal search, HttpServletRequest request) throws Exception {
		//search.setEntCode(entCode);

		if("1".equals(search.getSrchImportExportClass())) { // 1.해상 헤더가 2개다
			List<Map<String,Object>> list = bmsDisposalService.selectDisposalListExcel(search);

			BmsExcelUtils bmUtil = new BmsExcelUtils();

			DownloadFileInfo  down = bmUtil.downloadDisposalExcel(this.resourceLoader, search.getMainTitle(),list , customProperties);
			return new ResponseEntity<DownloadFileInfo>(down,  HttpStatus.OK);

		}else{
			List<Map<String,Object>> list = bmsDisposalService.selectDisposalListExcel(search);

			BmsExcelUtils bmUtil = new BmsExcelUtils();

			DownloadFileInfo  down = bmUtil.downloadDisposalAirExcel(this.resourceLoader, search.getMainTitle(),list , customProperties);
			return new ResponseEntity<DownloadFileInfo>(down,  HttpStatus.OK);

			/*ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
			map.put("data", bmsDisposalService.selectDisposalListExcel(search));
			return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getMainTitle(), map), HttpStatus.OK);*/
		}

	}
	//## batch controller make
  // executeBatchDisposal
	@ApiOperation(value = "폐기일괄신청, 폐기반출일괄반영", notes="폐기일괄신청, 폐기반출일괄반영 처리")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/disposal/exec/batch", method = RequestMethod.POST)
	public ResponseEntity<DisposalSaveResult> execBatchDisposal(@PathVariable("entCode") String entCode, @RequestBody BatchDisposal  batchDisposal, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, batchDisposal, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		
		batchDisposal.setEntCode(entCode);
		return new ResponseEntity<DisposalSaveResult>(bmsDisposalService.executeBatchDisposal(batchDisposal, request), HttpStatus.OK);
	}

	@ApiOperation(value = "폐기화물 ExcelUpload", notes="폐기화물 ExcelUpload합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
			@ApiImplicitParam(name = "excelUploadType", value = "엑셀타입", required = true, dataType = "string", paramType = "path", defaultValue = "bmsDisposal")
	})
	@RequestMapping(value="/excelUpload/check/{excelUploadType}", method = RequestMethod.POST)
	public ResponseEntity<DisposalSaveResult> checkExcelUpload(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
		return new ResponseEntity<DisposalSaveResult>(bmsDisposalService.checkExcelUpload(uploadExcel, entCode, excelUploadType), HttpStatus.OK);
	}

	@ApiOperation(value = "폐기화물 Excel 저장", notes="폐기화물 Excel 저장합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload/save/{excelUploadType}", method = RequestMethod.POST)
	public ResponseEntity<DisposalSaveResult> saveExcelUpload(@PathVariable("entCode") String entCode, @PathVariable("excelUploadType") String excelUploadType, @RequestBody ExcelUploadDisposal excelUploadDisposal, HttpServletRequest request) throws Exception {
		return new ResponseEntity<DisposalSaveResult>(bmsDisposalService.saveExcelUpload(excelUploadDisposal, request, excelUploadType), HttpStatus.OK);
	}

	@ApiOperation(value = "재고 엑셀 list 다운로드", notes="재고 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/stock/search/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectInPlanExcel(@PathVariable("entCode") String entCode, @RequestBody SearchDisposal search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		InventoryInList inventoryInList  = bmsDisposalService.selectInventoryInList(search);

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", inventoryInList.getInventoryInList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}

	@ApiOperation(value = "폐기 엑셀 list 다운로드", notes="폐기 엑셀 list 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/disp/search/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectCarryInExcel(@PathVariable("entCode") String entCode, @RequestBody SearchDisposal search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		search.setPage(0);
		DisposalList disposalList  = bmsDisposalService.selectDisposalList(search);

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", disposalList.getDisposalList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}
	
	@ApiOperation(value = "폐기스캔 insert", notes="폐기스캔을 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/disp/discard/insert", method = RequestMethod.POST)
	public ResponseEntity<DisposalSaveResult> insertDiscard(@PathVariable("entCode") String entCode, @RequestBody Disposal disposal, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, disposal, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<DisposalSaveResult>(bmsDisposalService.insertDiscard(disposal, request), HttpStatus.OK);
	}
	
}
