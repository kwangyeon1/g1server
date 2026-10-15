package org.yht.exps.express.controller.bms;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.bms.UpsNewbilling;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.service.bms.UpsNewbillingService;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags="Bms")
@RequestMapping("/{entCode}/bms/ups/newbilling")
public class UpsNewbillingController {

    @Resource(name = "upsNewbillingService")
    UpsNewbillingService upsNewbillingService;

    @ApiOperation(value = "UPS : 1.적하목록 정보 뉴빌링 업데이트", notes="UPS : 1.적하목록 정보 뉴빌링 업데이트합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/manifestToNewbilling", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> manifestToNewbilling(@PathVariable("entCode") String entCode, @QueryStringArgResolver UpsNewbilling newbilling, HttpServletRequest req) throws Exception {
    	upsNewbillingService.selectManifestToNewbilling(newbilling);
    	
    	try{
    		// 뉴빌링 업데이트에 필요한 항목 엑셀 다운로드
    		List<Map<String, Object>> dataList = newbilling.getDataList();
			String now = Util.getNow();
			for(Map<String, Object> dataMap : dataList) {
				dataMap.put("jobType", "4");
				dataMap.put("logUserName", newbilling.getLogUserName());
				dataMap.put("logDateTime", now);
			}
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(newbilling, Map.class);
			map.put("data", dataList);
			map.put("mainTitle", "UpsNewbilling");
			return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("UpsNewbilling", map), HttpStatus.OK);
    	}catch(Exception e){
    		return new ResponseEntity<DownloadFileInfo>(new DownloadFileInfo(), HttpStatus.OK);
    	}
    }
    
    @ApiOperation(value = "UPS : 2.목록이관시 목록건 뉴빌링 업데이트", notes="UPS : 2.목록이관시 목록건 뉴빌링 업데이트합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/manifestToNewbillingCustomsTransfer", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> manifestToNewbillingCustomsTransfer(@PathVariable("entCode") String entCode, @QueryStringArgResolver UpsNewbilling newbilling, HttpServletRequest req) throws Exception {
    	upsNewbillingService.selectManifestToNewbillingCustomsTransfer(newbilling);
    	
    	try{
    		// 뉴빌링 업데이트에 필요한 항목 엑셀 다운로드
    		List<Map<String, Object>> dataList = newbilling.getDataList();
			String now = Util.getNow();
			String jobType = null;
			if(!"customsUpdate".equals(newbilling.getAction())){
				if("P".equals(newbilling.getCdType())){
					jobType = "7";
				}else{
					jobType = "5";
				}
			}else{
				jobType = "8";
			}
			if("8".equals(jobType)){ /** 업무구분 8 : 상업서류생성 - 안 쓰는 기능으로 다운로드하지 않게 수정 */
				DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
				downloadFileInfo.setResultCode("pass");
				return new ResponseEntity<DownloadFileInfo>(downloadFileInfo, HttpStatus.OK);
			}else{
				String title = "UpsNewbilling";
				if("7".equals(jobType) && !"ECM-L0188".equals(Util.null2str(newbilling.getMenuId()))) {
					Map<String, Object> tmpMap = upsNewbillingService.selectToNewbillingCustomsTransferExcelMblInfo(newbilling);
					if(tmpMap != null) {
						title = "UpsNewbilling_자체선별_" + Util.null2str(tmpMap.get("VESSEL_FLIGHT")) + "_" + Util.null2str(tmpMap.get("MBL_NO"));
					}
				}
				for(Map<String, Object> dataMap : dataList) {
					dataMap.put("jobType", jobType);
					dataMap.put("logUserName", newbilling.getLogUserName());
					dataMap.put("logDateTime", now);
				}
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(newbilling, Map.class);
				map.put("data", dataList);
				map.put("mainTitle", "UpsNewbilling");
				return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(title, map), HttpStatus.OK);
			}
    	}catch(Exception e){
    		return new ResponseEntity<DownloadFileInfo>(new DownloadFileInfo(), HttpStatus.OK);
    	}
    }
    
    @ApiOperation(value = "UPS : 통관예정자료 파일생성(eCIS, TXT)", notes="UPS : 통관예정자료 파일생성(eCIS, TXT)합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/eCISProgramCreate", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> eCISProgramCreate(@PathVariable("entCode") String entCode, @QueryStringArgResolver UpsNewbilling newbilling, HttpServletRequest req) throws Exception {
    	upsNewbillingService.selecteECISProgramCreate(newbilling);
    	
//    	Util.setDB(req, "NEWBILLING");
    	return new ResponseEntity<DownloadFileInfo>(upsNewbillingService.eCISProgramCreate(newbilling), HttpStatus.OK);
    }
    
//    @ApiOperation(value = "UPS : 통관예정자료 파일생성(eCIS, TXT)", notes="UPS : 통관예정자료 파일생성(eCIS, TXT)합니다.")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
//    })
//    @RequestMapping(value="/eCISProgramCreate", method = RequestMethod.GET)
//    public ResponseEntity<DownloadFileInfo> eCISProgramCreate(@PathVariable("entCode") String entCode, @QueryStringArgResolver UpsNewbilling newbilling, HttpServletRequest req) throws Exception {
//    	upsNewbillingService.selecteECISProgramCreate(newbilling);
//    	
//    	try{
//    		// 뉴빌링 업데이트에 필요한 항목 엑셀 다운로드
//    		List<Map<String, Object>> dataList = newbilling.getDataList();
//			String now = Util.getNow();
//			for(Map<String, Object> dataMap : dataList) {
//				dataMap.put("jobType", "6");
//				dataMap.put("logUserName", newbilling.getLogUserName());
//				dataMap.put("logDateTime", now);
//			}
//			ObjectMapper objectMapper = new ObjectMapper();
//			Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(newbilling, Map.class);
//			map.put("data", dataList);
//			map.put("mainTitle", "UpsNewbilling");
//			return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("UpsNewbilling", map), HttpStatus.OK);
//    	}catch(Exception e){
//    		return new ResponseEntity<DownloadFileInfo>(new DownloadFileInfo(), HttpStatus.OK);
//    	}
//    }

    
    
    
    
    
    
    
//  김일호 =====================================================================================================
    @ApiOperation(value = "오류통보삭제 뉴빌링 업데이트", notes="목록이관시 목록건 뉴빌링 업데이트합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/customsIdError/Update", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> newbillingCustomsIdErrorUpdate(@PathVariable("entCode") String entCode, @QueryStringArgResolver UpsNewbilling newbilling) throws Exception {
    	DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
    	try{
    		// 1. DB 업데이트 - 통관목록 오류건 재전송 화면에서는 생략
    		if(!"ECM-L0188".equals(Util.null2str(newbilling.getMenuId()))){
    			upsNewbillingService.removeCustomsId(newbilling, entCode);
    			downloadFileInfo.setResultCode("pass");
    		}else {
    			// 2. 뉴빌링 업데이트에 필요한 항목 엑셀 다운로드 - 적하목록수입 화면에서는 생략
    			List<Map<String, Object>> dataList = newbilling.getDataList();
    			String now = Util.getNow();
    			for(Map<String, Object> dataMap : dataList) {
    				dataMap.put("jobType", "1");
    				dataMap.put("logUserName", newbilling.getLogUserName());
    				dataMap.put("logDateTime", now);
    				// 개인통관부호 DELIVERY_NOTE 값으로 표현
    				dataMap.put("customsId", Util.null2str(dataMap.get("deliveryNote")));
    			}
    			ObjectMapper objectMapper = new ObjectMapper();
    			Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(newbilling, Map.class);
    			map.put("data", dataList);
    			map.put("mainTitle", "UpsNewbilling");
    			downloadFileInfo = Common.getJsonForExcelList("UpsNewbilling", map);
    		}
    		return new ResponseEntity<DownloadFileInfo>(downloadFileInfo, HttpStatus.OK);
    	}catch(Exception e){
			return new ResponseEntity<DownloadFileInfo>(downloadFileInfo, HttpStatus.OK);
    	}
    }
    
    
    
    
    
//  이슬기 =====================================================================================================
    @ApiOperation(value = "성명불일치 뉴빌링 업데이트", notes="성명불일치 뉴빌링 업데이트합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/localNewbillingConsigneeInCorrect", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> localNewbillingConsigneeInCorrect(@PathVariable("entCode") String entCode, @RequestBody UpsNewbilling newbilling, HttpServletRequest request) throws Exception {
    	DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
    	try{
    		// 1. DB 업데이트 - 통관목록 오류건 재전송 화면에서는 생략
    		if(!"ECM-L0188".equals(Util.null2str(newbilling.getMenuId()))){
    			upsNewbillingService.removeCustomsId(newbilling, entCode);
    			downloadFileInfo.setResultCode("pass");
    		}else {
    			// 2. 뉴빌링 업데이트에 필요한 항목 엑셀 다운로드 - 적하목록수입 화면에서는 생략
    			List<Map<String, Object>> dataList = newbilling.getDataList();
    			String now = Util.getNow();
    			for(Map<String, Object> dataMap : dataList) {
    				dataMap.put("jobType", "2");
    				dataMap.put("logUserName", newbilling.getLogUserName());
    				dataMap.put("logDateTime", now);
    				// 개인통관부호 DELIVERY_NOTE 값으로 표현
    				dataMap.put("customsId", Util.null2str(dataMap.get("deliveryNote")));
    			}
    			ObjectMapper objectMapper = new ObjectMapper();
    			Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(newbilling, Map.class);
    			map.put("data", dataList);
    			map.put("mainTitle", "UpsNewbilling");
    			downloadFileInfo = Common.getJsonForExcelList("UpsNewbilling", map);
    		}
    		return new ResponseEntity<DownloadFileInfo>(downloadFileInfo, HttpStatus.OK);
    	}catch(Exception e){
			return new ResponseEntity<DownloadFileInfo>(downloadFileInfo, HttpStatus.OK);
    	}
    }
    
    @ApiOperation(value = "로컬미결등록 뉴빌링 업데이트", notes="로컬미결등록 뉴빌링 업데이트합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/localNewbillingApply", method = RequestMethod.GET)
    public ResponseEntity<DownloadFileInfo> localNewbillingApply(@PathVariable("entCode") String entCode, @QueryStringArgResolver  UpsNewbilling newbilling) throws Exception {    	
    	upsNewbillingService.selectLocalNewbillingApply(newbilling);
    	try{
    		// 뉴빌링 업데이트에 필요한 항목 엑셀 다운로드
    		List<Map<String, Object>> dataList = newbilling.getDataList();
			String now = Util.getNow();
			for(Map<String, Object> dataMap : dataList) {
				dataMap.put("jobType", "3");
				dataMap.put("logUserName", newbilling.getLogUserName());
				dataMap.put("logDateTime", now);
			}
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(newbilling, Map.class);
			map.put("data", dataList);
			map.put("mainTitle", "UpsNewbilling");
			return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("UpsNewbilling", map), HttpStatus.OK);
    	}catch(Exception e){
    		return new ResponseEntity<DownloadFileInfo>(new DownloadFileInfo(), HttpStatus.OK);
    	}
    }
    
    
    
    
    
    
    
    
    
}
