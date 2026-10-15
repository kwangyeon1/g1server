
package org.yht.exps.express.controller.eCommerce;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
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
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.eCommerce.order.ManifestFile;
import org.yht.exps.express.model.eCommerce.order.ManifestFileList;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchManifestFileList;
import org.yht.exps.express.model.edi.ManifestInList;
import org.yht.exps.express.model.edi.search.SearchManifestInList;
import org.yht.exps.express.service.eCommerce.ManifestFileService;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/manifestFile")
public class ManifestFileController {
	
	@Resource(name = "manifestFileService")
	ManifestFileService manifestFileService;
	@Value("${spring.profiles.active}")
	private String profile;
	
	@ApiOperation(value = "전자상거래 적하목록 파일업로드 배제 조회", notes="적하목록 파일업로드 배제 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/exclusion/search/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestFileList> selectExclusionList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<ManifestFileList>(manifestFileService.selectExclusionList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 적하목록 파일업로드 배제 엑셀파일생성", notes="적하목록 파일업로드 배제 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/exclusion/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectExclusionListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestFileService.selectExclusionList(search).getManifestFileList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("ExclusionList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 적하목록 배제 파일업로드", notes="전자상거래 적하목록 배제 파일업로드 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "userId", value = "등록자ID", required = true, dataType = "string", paramType = "path", defaultValue = "201808-001"),
		@ApiImplicitParam(name = "gmtTime", value = "GMT", required = true, dataType = "string", paramType = "path", defaultValue = "0")
	})
	@RequestMapping(value = "/uploadExclusion/upload/{userId}/{gmtTime}", method = RequestMethod.POST)
    public FileUploadResponse uploadExclusion(
    		@PathVariable("entCode") String entCode,
    		@PathVariable("userId") String userId,
    		@PathVariable("gmtTime") String gmtTime,
    		@RequestParam("fileExclusion") MultipartFile file) throws Exception {
		
		return manifestFileService.uploadCdBInfo(entCode, userId, gmtTime, "exclusion", file);
    }
	
	@ApiOperation(value = "전자상거래 UPS 기초코드 파일업로드", notes="전자상거래 UPS 기초코드 파일업로드 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "userId", value = "등록자ID", required = true, dataType = "string", paramType = "path", defaultValue = "201808-001"),
		@ApiImplicitParam(name = "gmtTime", value = "GMT", required = true, dataType = "string", paramType = "path", defaultValue = "0")
	})
	@RequestMapping(value = "/uploadUpsBasic/upload/{uploadType}/{userId}/{gmtTime}", method = RequestMethod.POST)
    public FileUploadResponse uploadUpsBasic(
    		@PathVariable("entCode") String entCode,
    		@PathVariable("uploadType") String uploadType,
    		@PathVariable("userId") String userId,
    		@PathVariable("gmtTime") String gmtTime,
    		@RequestParam("fileExclusion") MultipartFile file) throws Exception {
		
		return manifestFileService.uploadUpsBasic(entCode, userId, gmtTime, uploadType, file);
    }
	
	@ApiOperation(value = "전자상거래 적하목록 배제 삭제", notes="전자상거래 적하목록 배제 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/exclusion/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteExclusion(@PathVariable("entCode") String entCode, @RequestBody SearchManifestFileList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<OrderSaveResult>(manifestFileService.deleteExclusion(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 수입적하목록 파일업로드", notes="전자상거래 수입적하목록 파일업로드 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value = "/iFile/upload", method = RequestMethod.POST)
    public FileUploadResponse uploadIFileImp(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search, @RequestParam("iFile") MultipartFile file) throws Exception {
		if("ups1".equals(profile) || "bms1".equals(profile) || "bms3".equals(profile)) {
			return manifestFileService.uploadIFile(entCode, search, file);
		}else {
			return new FileUploadResponse(FileUploadResponse.RESULT_FAIL);
		}
    }
	
	@ApiOperation(value = "전자상거래 수입적하목록 파일업로드 저장", notes="전자상거래 수입적하목록 파일업로드 저장 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value = "/iFile/save", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> saveIFileImp(@PathVariable("entCode") String entCode, @RequestBody SearchManifestFileList search, HttpServletRequest req) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		try {
			orderSaveResult = manifestFileService.saveIFile(entCode, search, req);
		}catch (TransactionSystemException e){
		    Throwable root = e.getRootCause();
		    if(root instanceof SQLException) {
		        if (root.getMessage().toLowerCase().contains("connection is closed")) {
		        	Util.getPrintStacTraceString(e);
	            	orderSaveResult.setResultCode("timeout");
		        }
		    }
		}
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
    }
	
	@ApiOperation(value = "전자상거래 수출적하목록 파일업로드", notes="전자상거래 수출적하목록 파일업로드 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value = "/eFile/upload", method = RequestMethod.POST)
    public FileUploadResponse uploadIFileExp(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search, @RequestParam("eFile") MultipartFile file) throws Exception {
		if("ups1".equals(profile) || "bms1".equals(profile) || "bms3".equals(profile)) {
			return manifestFileService.uploadEFile(entCode, search, file);
		}else {
			FileUploadResponse result = new FileUploadResponse(FileUploadResponse.RESULT_OK);
			result.setResult(FileUploadResponse.RESULT_FAIL);
			result.setResultText("It does not support uploading.");
			return result;
		}
    }
	
	@ApiOperation(value = "전자상거래 수출적하목록 파일업로드 저장", notes="전자상거래 수출적하목록 파일업로드 저장 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value = "/eFile/save", method = RequestMethod.PUT)
    public ResponseEntity<OrderSaveResult> saveIFileExp(@PathVariable("entCode") String entCode, @RequestBody SearchManifestFileList search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveEFile(entCode, search, req), HttpStatus.OK);
    }
	
	@ApiOperation(value = "전자상거래 임시마스터 조회", notes="임시마스터 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/masterTemp/search/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestFileList> selectMasterTempList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<ManifestFileList>(manifestFileService.selectMasterTempList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 임시마스터 엑셀파일생성", notes="임시마스터 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/masterTemp/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectMasterTempListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestFileService.selectMasterTempList(search).getManifestFileList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("masterTempList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 eFile 하우스 엑셀파일생성", notes="임시마스터 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/house/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectHouseListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchManifestFileList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", search.getManifestFileList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("houseList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 임시마스터 insert", notes="임시마스터를 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/masterTemp/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertMasterTemp(@PathVariable("entCode") String entCode, @RequestBody ManifestFile manifestFile) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveMasterTemp(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 임시마스터 update", notes="임시마스터를 update합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "seqNoId", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/masterTemp/update/{seqNoId}", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateMasterTemp(@PathVariable("entCode") String entCode, @PathVariable("seqNoId") String seqNoId, @RequestBody ManifestFile manifestFile) throws Exception {
		manifestFile.setSeqNoId(seqNoId);
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveMasterTemp(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 임시마스터 삭제", notes="임시마스터를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/masterTemp/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteMasterTemp(@PathVariable("entCode") String entCode, @RequestBody ManifestFile manifestFile) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveMasterTemp(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 임시마스터 그룹명 조회", notes="임시마스터 그룹명을 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/masterTempGroupName/search/list", method = RequestMethod.GET)
	public ResponseEntity<List<String>> selectMasterTempGroupName(@PathVariable("entCode") String entCode, SearchManifestFileList search) throws Exception {
		return new ResponseEntity<List<String>>(manifestFileService.selectMasterTempGroupName(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 UPS기초코드 조회", notes="UPS기초코드 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/upsBasic/search/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestFileList> selectUpsBasicList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<ManifestFileList>(manifestFileService.selectUpsBasicList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 UPS기초코드 엑셀파일생성", notes="UPS기초코드 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/upsBasic/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectUpsBasicListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestFileService.selectUpsBasicList(search).getManifestFileList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("upsBasicList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 UPS기초코드 insert", notes="UPS기초코드를 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/upsBasic/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertUpsBasic(@PathVariable("entCode") String entCode, @RequestBody ManifestFile manifestFile) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveUpsBasic(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 UPS기초코드 update", notes="UPS기초코드를 update합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "seqNoId", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/upsBasic/update/{seqNoId}", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateUpsBasic(@PathVariable("entCode") String entCode, @PathVariable("seqNoId") String seqNoId, @RequestBody ManifestFile manifestFile) throws Exception {
		manifestFile.setSeqNoId(seqNoId);
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveUpsBasic(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 UPS기초코드 삭제", notes="UPS기초코드를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/upsBasic/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteUpsBasic(@PathVariable("entCode") String entCode, @RequestBody ManifestFile manifestFile) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveUpsBasic(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 UPS기초코드 조회", notes="UPS기초코드를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "seqNoId", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/upsBasic/get/{seqNoId}", method = RequestMethod.GET)
	public ResponseEntity<ManifestFile> selectUpsBasicEntry(@PathVariable("entCode") String entCode, @PathVariable("seqNoId") String seqNoId, ManifestFile manifestFile) throws Exception {
		manifestFile.setSeqNoId(seqNoId);
		return new ResponseEntity<ManifestFile>(manifestFileService.selectUpsBasicEntry(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 UPS그룹코드 조회", notes="UPS그룹코드 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/upsGroup/search/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestFileList> selectUpsGroupList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<ManifestFileList>(manifestFileService.selectUpsGroupList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 UPS그룹코드 엑셀파일생성", notes="UPS그룹코드 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/upsGroup/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectUpsGroupListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestFileService.selectUpsGroupList(search).getManifestFileList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("upsGroupList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 UPS그룹코드 insert", notes="UPS그룹코드를 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/upsGroup/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertUpsGroup(@PathVariable("entCode") String entCode, @RequestBody ManifestFile manifestFile) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveUpsGroup(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 UPS그룹코드 update", notes="UPS그룹코드를 update합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "seqNoId", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/upsGroup/update/{seqNoId}", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateUpsGroup(@PathVariable("entCode") String entCode, @PathVariable("seqNoId") String seqNoId, @RequestBody ManifestFile manifestFile) throws Exception {
		manifestFile.setSeqNoId(seqNoId);
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveUpsGroup(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 UPS그룹코드 삭제", notes="UPS그룹코드를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/upsGroup/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteUpsGroup(@PathVariable("entCode") String entCode, @RequestBody ManifestFile manifestFile) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveUpsGroup(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 UPS그룹코드 조회", notes="UPS그룹코드를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "seqNoId", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/upsGroup/get/{seqNoId}", method = RequestMethod.GET)
	public ResponseEntity<ManifestFile> selectUpsGroupEntry(@PathVariable("entCode") String entCode, @PathVariable("seqNoId") String seqNoId, ManifestFile manifestFile) throws Exception {
		manifestFile.setSeqNoId(seqNoId);
		return new ResponseEntity<ManifestFile>(manifestFileService.selectUpsGroupEntry(manifestFile), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 적하목록 개인통관부호 파일업로드", notes="전자상거래 적하목록 개인통관부호 파일업로드 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "userId", value = "등록자ID", required = true, dataType = "string", paramType = "path", defaultValue = "201808-001"),
		@ApiImplicitParam(name = "gmtTime", value = "GMT", required = true, dataType = "string", paramType = "path", defaultValue = "0")
	})
	@RequestMapping(value = "/customsId/upload/{userId}/{gmtTime}", method = RequestMethod.POST)
    public FileUploadResponse uploadCustomsId(
    		@PathVariable("entCode") String entCode,
    		@PathVariable("userId") String userId,
    		@PathVariable("gmtTime") String gmtTime,
    		@RequestParam("upsCustomsIdExcelUploadFile") MultipartFile file) throws Exception {
		
		return manifestFileService.uploadCdBInfo(entCode, userId, gmtTime, "customsId", file);
    }
	
	@ApiOperation(value = "UPS : 항공수입 T/S HAWB 가져오기 - 항공수입 T/S HAWB 조회", notes="항공수입 T/S HAWB 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/ts/in/search/house/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestFileList> selectTsHouseInList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestFileList search) throws Exception {
		return new ResponseEntity<ManifestFileList>(manifestFileService.selectTsHouseInList(search), HttpStatus.OK);
	}
    
    @ApiOperation(value = "UPS : 항공수입 T/S HAWB 가져오기 - 2.항공수출 HAWB 생성", notes="항공수출 HAWB 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/ts/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertTsHouse(@PathVariable("entCode") String entCode, @RequestBody SearchManifestFileList search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveTsHouse(entCode, search, request), HttpStatus.OK);
	}
    
    @ApiOperation(value = "UPS : 항공수입 T/S HAWB 가져오기 - MAWB 번호 일괄적용", notes="항공수출 MAWB 번호 일괄적용합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/ts/update", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> updateTsHouse(@PathVariable("entCode") String entCode, @RequestBody SearchManifestFileList search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestFileService.saveTsHouse(entCode, search, request), HttpStatus.OK);
	}
}
