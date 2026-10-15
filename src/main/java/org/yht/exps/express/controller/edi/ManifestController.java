package org.yht.exps.express.controller.edi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.yht.config.QueryStringArgResolver;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderBatchUpdate;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.edi.CarryinInList;
import org.yht.exps.express.model.edi.CarryoutInList;
import org.yht.exps.express.model.edi.CustomsManifestResList;
import org.yht.exps.express.model.edi.ManifestAggregate;
import org.yht.exps.express.model.edi.ManifestAggregateList;
import org.yht.exps.express.model.edi.ManifestCorrect;
import org.yht.exps.express.model.edi.ManifestCorrectDelete;
import org.yht.exps.express.model.edi.ManifestCorrectList;
import org.yht.exps.express.model.edi.ManifestCorrectMaster;
import org.yht.exps.express.model.edi.ManifestCorrectRecvResultList;
import org.yht.exps.express.model.edi.ManifestCorrectSaveResult;
import org.yht.exps.express.model.edi.ManifestInHouse;
import org.yht.exps.express.model.edi.ManifestInList;
import org.yht.exps.express.model.edi.ManifestOrder;
import org.yht.exps.express.model.edi.ManifestOrderSaveResult;
import org.yht.exps.express.model.edi.ManifestOutList;
import org.yht.exps.express.model.edi.ManifestSeparationResult;
import org.yht.exps.express.model.edi.SendDelivery;
import org.yht.exps.express.model.edi.SendManifest;
import org.yht.exps.express.model.edi.search.SearchCarryoutInList;
import org.yht.exps.express.model.edi.search.SearchManifest;
import org.yht.exps.express.model.edi.search.SearchManifestCorrectList;
import org.yht.exps.express.model.edi.search.SearchManifestCustoms;
import org.yht.exps.express.model.edi.search.SearchManifestInList;
import org.yht.exps.express.model.edi.search.SearchManifestOutList;
import org.yht.exps.express.model.edi.search.SearchManifestSimpleDocNo;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.edi.EdiCommonService;
import org.yht.exps.express.service.edi.EdiCustomsManifestSendService;
import org.yht.exps.express.service.edi.EdiManifestGwSendService;
import org.yht.exps.express.service.edi.EdiMfModifyExpSendService;
import org.yht.exps.express.service.edi.EdiMfModifyImpSendService;
import org.yht.exps.express.service.edi.ManifestService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.DateTime;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Slf4j
@RestController
@Api(tags = "EDI")
@RequestMapping("/{entCode}/ecm/edi/manifest")
public class ManifestController {
	
	@Resource(name = "manifestService")
	ManifestService manifestService;
	@Resource(name = "ediMfModifyExpSendService")
	EdiMfModifyExpSendService ediMfModifyExpSendService;
	@Resource(name = "ediMfModifyImpSendService")
	EdiMfModifyImpSendService ediMfModifyImpSendService;
	@Resource(name = "ediCustomsManifestSendService")
	EdiCustomsManifestSendService ediCustomsManifestSendService;
	@Resource(name = "ediManifestGwSendService")
	EdiManifestGwSendService ediManifestGwSendService;
	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	private CustomProperties customProperties;
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	
	@ApiOperation(value = "ECM 적하목록 수입 마스터 리스트 조회", notes="ECM 적하목록 수입 마스터 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/in/search/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestInList> selectMasterInList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestInList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		ManifestInList manifestInList = new ManifestInList();
		try {
			manifestInList  = manifestService.selectMasterInList(search);
		}catch(Exception e) {
			if((entCode.equals("TYGCK") || entCode.equals("TPCNK")) && e.getMessage().contains("SQLException")) {
				// 10~15초 사이의 랜덤한 시간 뒤 재조회
				int sec = (int)new Random().nextInt(6)+10;
				Thread.sleep(sec * 1000);
				manifestInList  = manifestService.selectMasterInList(search);
			}else {
				throw e;
			}
		}
		return new ResponseEntity<ManifestInList>(manifestInList, HttpStatus.OK);
	}
	@ApiOperation(value = "ECM 적하목록 수입 마스터 리스트 조회 POST ", notes="ECM 적하목록 수입 마스터 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/in/search/list/post", method = RequestMethod.POST)
	public ResponseEntity<ManifestInList> selectMasterInListPost(@PathVariable("entCode") String entCode, @RequestBody SearchManifestInList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		ManifestInList manifestInList = new ManifestInList();
		try {
			manifestInList  = manifestService.selectMasterInList(search);
		}catch(Exception e) {
			if((entCode.equals("TYGCK") || entCode.equals("TPCNK")) && e.getMessage().contains("SQLException")) {
				// 10~15초 사이의 랜덤한 시간 뒤 재조회
				int sec = (int)new Random().nextInt(6)+10;
				Thread.sleep(sec * 1000);
				manifestInList  = manifestService.selectMasterInList(search);
			}else {
				throw e;
			}
		}
		return new ResponseEntity<ManifestInList>(manifestInList, HttpStatus.OK);
	}
	@ApiOperation(value = "ECM 적하목록 수입 마스터 리스트 엑셀파일생성", notes="ECM 적하목록 수입 마스터 리스트 엑셀파일생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/in/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectMasterInListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestInList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestService.selectMasterInList(search).getManifestInMasterList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("ManifestMasterList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록 수입 하우스 리스트 조회", notes="ECM 적하목록 수입 하우스 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/in/search/house/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestInList> selectHouseInList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestInList search) throws Exception {
		return new ResponseEntity<ManifestInList>(manifestService.selectHouseInList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "ECM 적하목록 수입 하우스 리스트 조회(POST)", notes="ECM 적하목록 수입 하우스 리스트 조회합니다. (대량 검색조건 대응 POST 방식)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/in/search/house/list/post", method = RequestMethod.POST)
	public ResponseEntity<ManifestInList> selectHouseInListPost(@PathVariable("entCode") String entCode, @RequestBody SearchManifestInList search) throws Exception {
		return new ResponseEntity<ManifestInList>(manifestService.selectHouseInList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록 수입 하우스 리스트 엑셀파일생성", notes="ECM 적하목록 수입 하우스 리스트 엑셀파일생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/in/search/house/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectHouseInListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestInList search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestService.selectHouseInList(search).getManifestInHouseList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("ManifestHouseList", map), HttpStatus.OK);
	}
	@ApiOperation(value = "ECM 적하목록 수입 하우스 리스트 엑셀파일생성", notes="ECM 적하목록 수입 하우스 리스트 엑셀파일생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/in/search/house/list/excelPost", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectHouseInListExcelPost(@PathVariable("entCode") String entCode, @RequestBody SearchManifestInList search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestService.selectHouseInList(search).getManifestInHouseList());
		if(((List<Object>)map.get("data")).size() > 0) {
			String logVal = "ManifestHouseList EXCEL DOWNLOAD!!! " + entCode + " / " + ((List<ManifestInHouse>)map.get("data")).get(0).getMblNo();
			log.info(logVal);
		}
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("ManifestHouseList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록 수출 마스터 리스트 조회", notes="ECM 적하목록 수출 마스터 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/out/search/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestOutList> selectMasterOutList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestOutList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		return new ResponseEntity<ManifestOutList>(manifestService.selectMasterOutList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "ECM 적하목록 수출 마스터 리스트 조회 POST", notes="ECM 적하목록 수출 마스터 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/out/search/list/post", method = RequestMethod.POST)
	public ResponseEntity<ManifestOutList> selectMasterOutListPost(@PathVariable("entCode") String entCode, @RequestBody SearchManifestOutList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		return new ResponseEntity<ManifestOutList>(manifestService.selectMasterOutList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "ECM 적하목록 수출 마스터 리스트 엑셀파일생성", notes="ECM 적하목록 수출 마스터 리스트 엑셀파일생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/out/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectMasterOutListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestOutList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestService.selectMasterOutList(search).getManifestOutMasterList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("ManifestMasterList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록 수출 하우스 리스트 조회", notes="ECM 적하목록 수출 하우스 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/out/search/house/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestOutList> selectHouseOutList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestOutList search) throws Exception {
		return new ResponseEntity<ManifestOutList>(manifestService.selectHouseOutList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "ECM 적하목록 수출 하우스 리스트 조회 POST", notes="ECM 적하목록 수출 하우스 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/out/search/house/list/post", method = RequestMethod.POST)
	public ResponseEntity<ManifestOutList> selectHouseOutListPost(@PathVariable("entCode") String entCode, @RequestBody SearchManifestOutList search) throws Exception {
		return new ResponseEntity<ManifestOutList>(manifestService.selectHouseOutList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "ECM 적하목록 수출 하우스 리스트 엑셀파일생성", notes="ECM 적하목록 수출 하우스 리스트 엑셀파일생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/out/search/house/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectHouseOutListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestOutList search) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestService.selectHouseOutList(search).getManifestOutHouseList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("ManifestHouseList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Master Order 상세 조회", notes="Master Order 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<ManifestOrder> selectOrderDetail(@PathVariable("entCode") String entCode, SearchManifest search) throws Exception {
		return new ResponseEntity<ManifestOrder>(manifestService.selectOrderDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Master Order update", notes="Master Order 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/{odJobNo}", method = RequestMethod.PUT)
	public ResponseEntity<ManifestOrderSaveResult> updateOrder(@PathVariable("entCode") String entCode, @RequestBody Order order, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, order, "updateUserId", "logUserName", null, "logUserNameEng");
		order.setRegisterUserId(order.getUpdateUserId());
		return new ResponseEntity<ManifestOrderSaveResult>(manifestService.saveOrder(order, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Master Order delete", notes="Master Order 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete", method = RequestMethod.DELETE)
	public ResponseEntity<ManifestOrderSaveResult> deleteOrder(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		return new ResponseEntity<ManifestOrderSaveResult>(manifestService.deleteOrder(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Convert Ams Address", notes="AMS 주소 변환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/convertAmsAddress", method = RequestMethod.PUT)
	public ResponseEntity<ManifestOrderSaveResult> convertAmsAddress(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ManifestOrderSaveResult>(manifestService.convertAmsAddress(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "BMS반입예정생성", notes="BMS반입예정생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insertBmsInPlan", method = RequestMethod.POST)
	public ResponseEntity<ManifestOrderSaveResult> insertBmsInPlan(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		return new ResponseEntity<ManifestOrderSaveResult>(manifestService.insertBmsInPlan(sendManifest), HttpStatus.OK);
	}

    @ApiOperation(value = "허브넷반입예정생성(항공)", notes="허브넷반입예정생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/insertHubnInPlanAir", method = RequestMethod.POST)
    public ResponseEntity<ManifestOrderSaveResult> insertHubnInPlanAir(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
        //반입예정 검증
        ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
        manifestOrderSaveResult = manifestService.verifyHubnInPlanAir(sendManifest);
        sendManifest.setBound("AIR");
        
        //CWHP 반입 예정 생성
        if("".equals(Util.null2str(manifestOrderSaveResult.getResultCode())) && sendManifest.getBmsInPlanList().size() > 0){
            Util.setDB(request, "CWHP");
            manifestOrderSaveResult = manifestService.insertHubnInPlanAir(request,sendManifest);
        }

        // G1 반입 예정 업데이트
        if("".equals(Util.null2str(manifestOrderSaveResult.getResultCode())) && sendManifest.getBmsInPlanList().size() > 0){
            Util.setDB(request, entCode);
            manifestOrderSaveResult = manifestService.updateHubnInPlan(request,sendManifest);
        }

        return new ResponseEntity<ManifestOrderSaveResult>(manifestOrderSaveResult, HttpStatus.OK);
    }

    @ApiOperation(value = "허브넷반입예정생성(해상)", notes="허브넷반입예정생성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/insertHubnInPlanSea", method = RequestMethod.POST)
    public ResponseEntity<ManifestOrderSaveResult> insertHubnInPlanSea(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {

        ManifestOrderSaveResult manifestOrderSaveResult = new ManifestOrderSaveResult();
        manifestOrderSaveResult = manifestService.verifyHubnInPlanSea(sendManifest);
        sendManifest.setBound("SEA");

        //CWHP 반입 예정 생성
        if("".equals(Util.null2str(manifestOrderSaveResult.getResultCode())) && sendManifest.getBmsInPlanList().size() > 0) {
            Util.setDB(request, "CWHPS");
            manifestOrderSaveResult = manifestService.insertHubnInPlanSea(request, sendManifest);
        }

        // G1 반입 예정 업데이트
        if("".equals(Util.null2str(manifestOrderSaveResult.getResultCode())) && sendManifest.getBmsInPlanList().size() > 0){
            Util.setDB(request, entCode);
            manifestOrderSaveResult = manifestService.updateHubnInPlan(request,sendManifest);
        }

        return new ResponseEntity<ManifestOrderSaveResult>(manifestOrderSaveResult, HttpStatus.OK);
    }
	
	@ApiOperation(value = "BMS반입예정삭제", notes="BMS반입예정삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/deleteBmsInPlan", method = RequestMethod.DELETE)
	public ResponseEntity<ManifestOrderSaveResult> deleteBmsInPlan(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		return new ResponseEntity<ManifestOrderSaveResult>(manifestService.deleteBmsInPlan(sendManifest), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 통관목록 오류건 자료생성", notes="전자상거래 통관목록 오류건 자료생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/simpleError/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateSimpleError(@PathVariable("entCode") String entCode, @RequestBody SearchOrderBatchUpdate search, HttpServletRequest request) throws Exception {
		search.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(manifestService.updateSimpleError(search, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "EMS환적 문서송신", notes="EMS환적 문서송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendEms", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendEms(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(manifestService.sendEms(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "해운 적하목록신고 문서송신(4세대)", notes="해운 적하목록신고문서(4세대)를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendManifestSea4th", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendManifestSea4th(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		return new ResponseEntity<OrderSaveResult>(manifestService.sendManifestSea4th(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "수출목록신고문서 송신", notes="수출목록신고문서를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendCustomsManifestExpOld", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendCustomsManifestExpOld(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, null, "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		String cusSendType = "REST";
		OrderSaveResult orderSaveResult = ediCustomsManifestSendService.setCustomsManifestExpDataOld(sendManifest, cusSendType);
		if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(!ediCommonService.isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
				orderSaveResult.setResultCode("ebmsRestConnectionError");
				
				OrderSaveResult returnObj = new OrderSaveResult();
				returnObj.setResultCode(orderSaveResult.getResultCode());
				if(orderSaveResult.getResultData() != null){
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
					dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
					dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
					returnObj.setResultData(dataMap);
				}
				return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
			}
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try{
						Map<String, Object> resultData = orderSaveResult.getResultData();
						int logStartIdx, logEndIdx, seq;
						Map<String, Object> restData = (Map)resultData.get("REST");
						JSONObject jsonObject;
						JSONObject sendData;
						JSONArray jsonArray;
						List<String> houseOdJobNoList;
						List<Map<String, Object>> logList;
						List<Map<String, Object>> upsList;
						List<List<String>> odJobNoArrayList;
						List<JSONArray> jsonArrayList;
						Map<String, Object> logData;
						Map<String, Object> logMap = (Map)resultData.get("logMap");
						SendManifest updateCustomsManifestData = (SendManifest)resultData.get("updateCustomsManifestData");
						updateCustomsManifestData.setMasterOdJobNo(null);
						ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
						ArrayList<BulkData> bulkList2 = new ArrayList<BulkData>();	// UPS bulkList
						BulkData bulkData = null;
						String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
						String index2 = "ecm-ups-tracking-" + entCode.toLowerCase();
						int listSize;
						
						for(String ediPortalMailboxId : restData.keySet()){
							if(ediPortalMailboxId.indexOf("_ORIG_CODE") == -1 && ediPortalMailboxId.indexOf("_LOG_LIST") == -1 && ediPortalMailboxId.indexOf("_UPS_LIST") == -1){
								logStartIdx = 0;
								logEndIdx = 0;
								seq = 1;
								jsonObject = (JSONObject)restData.get(ediPortalMailboxId);
								logList = (List)restData.get(ediPortalMailboxId + "_LOG_LIST");
								upsList = (List)restData.get(ediPortalMailboxId + "_UPS_LIST");
								jsonArrayList = (List<JSONArray>)jsonObject.get("dataList");
								odJobNoArrayList = (List<List<String>>)jsonObject.get("odJobNoList");
								listSize = jsonArrayList.size();
								for(int i = 0; i < listSize; i++){
									jsonArray = jsonArrayList.get(i);
									houseOdJobNoList = odJobNoArrayList.get(i);
									sendData = new JSONObject();
									sendData.put("dataList", jsonArray);
									
									ediCustomsManifestSendService.setCustomsManifestExpMrn(ediPortalMailboxId, sendData, logMap, updateCustomsManifestData, restData, houseOdJobNoList);
									
									logEndIdx = logStartIdx + jsonArray.size();
									for(int j = logStartIdx; j < logEndIdx; j++){
										logData = logList.get(j);
										logData.putAll(logMap);
										bulkData = new BulkData();
										bulkData.setActionType(BulkData.Type.CREATE);
										bulkData.setIndexName(index);
										bulkData.setId(Util.null2str(logData.get("es_job_no")));
										bulkData.setJsonMap(logData);
										bulkList.add(bulkData);
									}
									logStartIdx = logEndIdx;
									
									/** UPS Tracking Send */
									if(upsList != null) {
										long trackingDateTime = System.currentTimeMillis();
										for(Map<String, Object> upsData : upsList) {
											upsData.put("DEMTrackingDateTime", trackingDateTime);
											bulkData = new BulkData();
											bulkData.setActionType(BulkData.Type.CREATE);
											bulkData.setIndexName(index2);
											bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
											bulkData.setJsonMap(upsData);
											bulkList2.add(bulkData);
										}
									}
									if(listSize - 1 != i){
										Thread.sleep(60000);
									}
								}
							}
						}
						
						ediCustomsManifestSendService.setCustomsManifestExp(sendManifest, cusSendType, orderSaveResult, bulkList, bulkList2, index);
					}catch(Exception e){
						log.error(Util.getPrintStacTraceString(e));
						try{
							ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestExp", BeanUtils.describe(sendManifest), e);
						}catch(Exception ee){
							log.error(Util.getPrintStacTraceString(ee));
						}
					}
				}
			});
			thread.start();
			
			orderSaveResult.setResultCode("ebmsRest");
		}
		
		OrderSaveResult returnObj = new OrderSaveResult();
		returnObj.setResultCode(orderSaveResult.getResultCode());
		if(orderSaveResult.getResultData() != null){
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
			dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
			dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
			returnObj.setResultData(dataMap);
		}
		return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
	}
	
	@ApiOperation(value = "수출목록신고문서 송신", notes="수출목록신고문서를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendCustomsManifestExp", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendCustomsManifestExp(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		OrderSaveResult orderSaveResult = ediCustomsManifestSendService.setCustomsManifestExpData(sendManifest, index);
		if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(!ediCommonService.isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
				orderSaveResult.setResultCode("ebmsRestConnectionError");
				OrderSaveResult returnObj = new OrderSaveResult();
				returnObj.setResultCode(orderSaveResult.getResultCode());
				if(orderSaveResult.getResultData() != null){
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
					dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
					dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
					returnObj.setResultData(dataMap);
				}
				return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
			}

			// STTDK, HKLXK 도입사는 대량건 처리 시 세션(30분) 만료로 DataSource 라우팅 키가 사라지는 문제가 있어
			//  백그라운드 스레드에 요청 컨텍스트(db_key=entCode)를 그대로 넘겨준다.
			// dbKeyForThread: entCode가 "STTDK" 또는 "HKLXK"면 그 값을, 아니면 null.
			// null이면 reqAttrs도 null → 아래 스레드에서 기존(세션) 방식 그대로 동작.
			final String dbKeyForThread =
					("STTDK".equalsIgnoreCase(entCode) || "HKLXK".equalsIgnoreCase(entCode)) ? entCode : null;

			// reqAttrs: 백그라운드 스레드에 심어줄 "가짜 요청 컨텍스트".
			// MyRoutingDataSource가 db_key를 물어보면 세션 대신 이 객체가 답한다.
			final RequestAttributes reqAttrs =
					(dbKeyForThread == null) ? null : new RequestAttributes() {
						// db_key 값을 담아둘 자체 저장소. 실제 HttpSession을 안 쓰고 이 Map만 사용 → 세션 만료와 무관.
						private final Map<String, Object> store = new HashMap<String, Object>();
						// 인스턴스 초기화 블록: 이 객체가 만들어질 때 store에 db_key = 도입사코드(STTDK/HKLXK)를 미리 넣어둔다.
						{ store.put("db_key", dbKeyForThread); }
						// 이 한 줄 덕분에 라우팅 키가 항상 해당 도입사코드로 결정된다.
						@Override public Object getAttribute(String name, int scope) { return store.get(name); }
						// 속성 저장 요청 시 store에 넣음.
						@Override public void setAttribute(String name, Object value, int scope) { store.put(name, value); }
						// 속성 삭제 요청 시 store에서 제거. (형식적 구현)
						@Override public void removeAttribute(String name, int scope) { store.remove(name); }
						// 저장된 속성 이름 목록 반환. (형식적 구현)
						@Override public String[] getAttributeNames(int scope) { return store.keySet().toArray(new String[0]); }
						// 객체 소멸 시 실행할 콜백 등록. 쓸 일 없으므로 아무것도 안 함.
						@Override public void registerDestructionCallback(String name, Runnable callback, int scope) { }
						// 특정 키에 대한 참조(예: 실제 request/session 객체) 반환. 우리는 실제 객체가 없으므로 null.
						@Override public Object resolveReference(String key) { return null; }
						// 세션 ID 반환. 실제 세션이 없으니 식별용 임의 문자열("bg-STTDK") 반환.
						@Override public String getSessionId() { return "bg-" + dbKeyForThread; }
						// 세션 동기화용 잠금 객체 반환. 별도 객체가 없으니 자기 자신을 반환.
						@Override public Object getSessionMutex() { return this; }
					};

			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					boolean routingBound = false;
					//STTDK, HKLXK일경우에 타임아웃 문제없는 위 세팅객체를 적용
					if(reqAttrs != null){
						org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(reqAttrs, true);
						routingBound = true;
						log.info("[수출목록신고] 백그라운드 라우팅 컨텍스트 바인딩 - entCode={}, db_key={}", entCode, dbKeyForThread);
					}
					try{
						Map<String, Object> result = orderSaveResult.getResultData();
						Map<String, Object> masterOrder = (Map<String, Object>)result.get("masterOrder");
						List<Map<String, Object>> houseOrderList = (List<Map<String, Object>>)result.get("houseOrderList");
						int totalHouseCnt = (houseOrderList == null) ? 0 : houseOrderList.size();
						log.info("[수출목록신고] 전송 시작 - entCode={}, dbKeyForThread={}, index={}, 전체건수={}",
								entCode, dbKeyForThread, index, totalHouseCnt);

						List<Map<String, Object>> sendHouseOrderList = new ArrayList<Map<String, Object>>();
						String ediPortalMailboxId = "";
						int houseCnt = 0;
						int batchNo = 0;			/** 배치 순번(로그 추적용) */
						int sentCnt = 0;			/** 실제 전송 완료 건수 누적 */
						for(Map<String, Object> houseOrder : houseOrderList){
							if(houseCnt == 500 || !ediPortalMailboxId.equals(houseOrder.get("EDI_PORTAL_MAILBOX_ID"))){	/** 사서함별 500건씩 나눠서 전송 */
								if(houseCnt != 0){
									batchNo++;
									log.info("[수출목록신고] 배치 전송 - entCode={}, 배치번호={}, 사서함ID={}, 건수={}",
											entCode, batchNo, ediPortalMailboxId, houseCnt);
									ediCustomsManifestSendService.sendCustomsManifestExp(sendManifest, masterOrder, sendHouseOrderList, ediPortalMailboxId, index);
									sentCnt += houseCnt;
									log.info("[수출목록신고] 배치 전송 완료 - entCode={}, 배치번호={}, 사서함ID={}, 누적전송={}/{}",
											entCode, batchNo, ediPortalMailboxId, sentCnt, totalHouseCnt);
									Thread.sleep(60000);	/** 관세청에 200건씩 전송시 1분 대기해야함 */
									sendHouseOrderList = new ArrayList<Map<String, Object>>();
									houseCnt = 0;
								}
								ediPortalMailboxId = Util.null2str(houseOrder.get("EDI_PORTAL_MAILBOX_ID"));
							}
							
							sendHouseOrderList.add(houseOrder);
							houseCnt++;
						}
						if(houseCnt != 0){
							batchNo++;
							log.info("[수출목록신고] 배치 전송(마지막) - entCode={}, 배치번호={}, 사서함ID={}, 건수={}",
									entCode, batchNo, ediPortalMailboxId, houseCnt);
							ediCustomsManifestSendService.sendCustomsManifestExp(sendManifest, masterOrder, sendHouseOrderList, ediPortalMailboxId, index);
							sentCnt += houseCnt;
						}
						log.info("[수출목록신고] 전송 완료 - entCode={}, 총배치={}, 총전송건수={}/{}",
								entCode, batchNo, sentCnt, totalHouseCnt);
						
//						Map<String, Object> tmpMap = new HashMap<String, Object>();
//						tmpMap.put("entCode", entCode);
//						tmpMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
//						tmpMap.put("gmtTime", sendManifest.getGmtTime());
//						tmpMap.put("userId", sendManifest.getUpdateUserId());
//						tmpMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
//						tmpMap.put("bound", masterOrder.get("BOUND"));
//						tmpMap.put("boundType", masterOrder.get("BOUND_TYPE"));
//						tmpMap.put("blNo", masterOrder.get("MBL_NO"));
//						commonService.sendKakaoAlarmGroup(tmpMap, "GOVCBR936");
					}catch(Exception e){
						log.error("[수출목록신고] 전송 실패 - entCode={}, index={}, msg={}", entCode, index, e.getMessage(), e);
						try{
							ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestExp", BeanUtils.describe(sendManifest), e);
						}catch(Exception ee){
							log.error(Util.getPrintStacTraceString(ee));
						}
					}finally{
						//자원초기화
						if(routingBound){
							org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
						}
					}
				}
			});
			thread.start();
			orderSaveResult.setResultCode("ebmsRest");
		}
		
		OrderSaveResult returnObj = new OrderSaveResult();
		returnObj.setResultCode(orderSaveResult.getResultCode());
		if(orderSaveResult.getResultData() != null){
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
			dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
			dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
			returnObj.setResultData(dataMap);
		}
		return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
	}
	
	@ApiOperation(value = "수출목록신고문서 송신", notes="수출목록신고문서를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendCustomsManifestExpTest", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendCustomsManifestExpTest(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		log.info("sendCustomsManifestExpTest");
		OrderSaveResult orderSaveResult = ediCustomsManifestSendService.setCustomsManifestExpDataTest(sendManifest);
		if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(!ediCommonService.isOnline("211.43.13.136", Util.null2int(customProperties.getEbmsPort()))) {
				orderSaveResult.setResultCode("ebmsRestConnectionError");
				
				OrderSaveResult returnObj = new OrderSaveResult();
				returnObj.setResultCode(orderSaveResult.getResultCode());
				if(orderSaveResult.getResultData() != null){
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
					dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
					dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
					returnObj.setResultData(dataMap);
				}
				return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
			}
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try{
						ediCustomsManifestSendService.setCustomsManifestExpTest(sendManifest, orderSaveResult);
					}catch(Exception e){
						log.error(Util.getPrintStacTraceString(e));
						try{
							ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestExp", BeanUtils.describe(sendManifest), e);
						}catch(Exception ee){
							log.error(Util.getPrintStacTraceString(ee));
						}
					}
				}
			});
			thread.start();
			
			orderSaveResult.setResultCode("ebmsRest");
		}
		
		OrderSaveResult returnObj = new OrderSaveResult();
		returnObj.setResultCode(orderSaveResult.getResultCode());
		if(orderSaveResult.getResultData() != null){
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
			dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
			dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
			returnObj.setResultData(dataMap);
		}
		return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
	}
	
	@ApiOperation(value = "수입목록신고문서 송신", notes="수입목록신고문서를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendCustomsManifestImp", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendCustomsManifestImp(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		Map<String, Object> enterpriseMap = commonMapper.selectEnterpriseInfo(Util.null2str(sendManifest.getEntCode()));
		String cusSendType = Util.null2str(enterpriseMap.get("CUS_SEND_TYPE"));
		
		OrderSaveResult orderSaveResult = ediCustomsManifestSendService.setCustomsManifestImpData(sendManifest, cusSendType);
		if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(cusSendType.equals("REST")){
				if(!ediCommonService.isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
					orderSaveResult.setResultCode("ebmsRestConnectionError");
					
					OrderSaveResult returnObj = new OrderSaveResult();
					returnObj.setResultCode(orderSaveResult.getResultCode());
					if(orderSaveResult.getResultData() != null){
						Map<String, Object> dataMap = new HashMap<String, Object>();
						dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
						dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
						dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
						returnObj.setResultData(dataMap);
					}
					
					try{
						log.error("ebmsRestConnectionError!!!  [ entCode : " + entCode + " ,  masterOdJobNo : " + Util.null2str(sendManifest.getMasterOdJobNo()) + " ]");
						throw new Exception("ebmsRestConnectionError!!!");
					}catch(Exception e){
						ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestImp", BeanUtils.describe(sendManifest), e);
					}
					return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
				}
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try{
							ediCustomsManifestSendService.setCustomsManifestImp(sendManifest, cusSendType, orderSaveResult);
							
//							Map<String, Object> result = orderSaveResult.getResultData();
//							Map<String, Object> masterOrder = (Map<String, Object>)result.get("masterOrder");
//							Map<String, Object> tmpMap = new HashMap<String, Object>();
//							tmpMap.put("entCode", entCode);
//							tmpMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
//							tmpMap.put("gmtTime", sendManifest.getGmtTime());
//							tmpMap.put("userId", sendManifest.getUpdateUserId());
//							tmpMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
//							tmpMap.put("bound", masterOrder.get("BOUND"));
//							tmpMap.put("boundType", masterOrder.get("BOUND_TYPE"));
//							tmpMap.put("blNo", masterOrder.get("MBL_NO"));
//							commonService.sendKakaoAlarmGroup(tmpMap, "GOVCBR936");
						}catch(Exception e){
							log.error(Util.getPrintStacTraceString(e));
							try{
								ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestImp", BeanUtils.describe(sendManifest), e);
							}catch(Exception ee){
								log.error(Util.getPrintStacTraceString(ee));
							}
						}
					}
				});
				thread.start();
				
				orderSaveResult.setResultCode("ebmsRest");
			}else{
				ediCustomsManifestSendService.setCustomsManifestImp(sendManifest, cusSendType, orderSaveResult);
			}
		}
		
		OrderSaveResult returnObj = new OrderSaveResult();
		returnObj.setResultCode(orderSaveResult.getResultCode());
		if(orderSaveResult.getResultData() != null){
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
			dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
			dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
			returnObj.setResultData(dataMap);
		}
		return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
	}
	@ApiOperation(value = "수입목록신고문서검증", notes="수입목록신고문서를 검증합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendCustomsManifestImpVali", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendCustomsManifestImpVali(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		Map<String, Object> enterpriseMap = commonMapper.selectEnterpriseInfo(Util.null2str(sendManifest.getEntCode()));
		String cusSendType = Util.null2str(enterpriseMap.get("CUS_SEND_TYPE"));
		OrderSaveResult orderSaveResult = ediCustomsManifestSendService.setCustomsManifestImpDataVali(sendManifest, cusSendType);
		OrderSaveResult returnObj = new OrderSaveResult();
		returnObj.setResultCode(orderSaveResult.getResultCode());
		if(orderSaveResult.getResultData() != null){
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
			dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
			dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
			returnObj.setResultData(dataMap);
		}
		return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
	}
	
	/** UPS 통관목록오류건재전송 마스터n건 전송용 */
	@ApiOperation(value = "수입목록신고문서 송신", notes="수입목록신고문서를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendCustomsManifestImpUps", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendCustomsManifestImpUps(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		Map<String, Object> enterpriseMap = commonMapper.selectEnterpriseInfo(Util.null2str(sendManifest.getEntCode()));
		String cusSendType = Util.null2str(enterpriseMap.get("CUS_SEND_TYPE"));
		
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		OrderSaveResult tmpOrderSaveResult = new OrderSaveResult();
		List<Map<String,Object>> errorList = new ArrayList<Map<String, Object>>();
		List<String> simpleDocNoList = new ArrayList<String>();
		
		for(String masterOdJobNo : sendManifest.getMasterInfo().keySet()) {
			Map<String, Object> masterMap = (Map<String, Object>) sendManifest.getMasterInfo().get(masterOdJobNo);
			String mblNo = Util.null2str(masterMap.get("mblNo"));
			sendManifest.setOdJobNo(null);
			sendManifest.setMasterOdJobNo(masterOdJobNo);
			sendManifest.setMblNo(mblNo);
			sendManifest.setSimpleDocNoList((List<String>)masterMap.get("simpleDocNoList"));
			tmpOrderSaveResult = ediCustomsManifestSendService.setCustomsManifestImpData(sendManifest, cusSendType);
			if(Util.null2str(tmpOrderSaveResult.getResultCode()).equals("ERROR")) {
				orderSaveResult.setResultCode("ERROR");
				if(tmpOrderSaveResult.getResultData() != null){
					if("Y".equals(Util.null2str(tmpOrderSaveResult.getResultData().get("validateErrorYn")))){
						errorList.addAll((List)tmpOrderSaveResult.getResultData().get("errorData"));
					}
					if(!"".equals(Util.null2str(tmpOrderSaveResult.getResultData().get("processMessage")))) {
						Map<String, Object> tmpMap = new HashMap<String, Object>();
						tmpMap.put("REFERANCE", mblNo);
						tmpMap.put("ERROR_MESSAGE", "M.B/L : " + Util.null2str(tmpOrderSaveResult.getResultData().get("processMessage")));
						errorList.add(tmpMap);
					}
				}
			}
			
			if(Util.null2str(tmpOrderSaveResult.getResultCode()).equals("")){
				orderSaveResult.setResultData(tmpOrderSaveResult.getResultData());
				if(cusSendType.equals("REST")){
					if(!ediCommonService.isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
						tmpOrderSaveResult.setResultCode("ebmsRestConnectionError");
						
						OrderSaveResult returnObj = new OrderSaveResult();
						returnObj.setResultCode(tmpOrderSaveResult.getResultCode());
						if(errorList.size() > 0) {
							Map<String, Object> dataMap = new HashMap<String, Object>();
							dataMap.put("errorList", errorList);
							returnObj.setResultData(dataMap);
						}
						
						try{
							log.error("ebmsRestConnectionError!!!  [ entCode : " + entCode + " ,  masterOdJobNo : " + Util.null2str(sendManifest.getMasterOdJobNo()) + " ]");
							throw new Exception("ebmsRestConnectionError!!!");
						}catch(Exception e){
							ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestImp", BeanUtils.describe(sendManifest), e);
						}
						return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
					}
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							try{
								ediCustomsManifestSendService.setCustomsManifestImp(sendManifest, cusSendType, orderSaveResult);
								
//									Map<String, Object> result = orderSaveResult.getResultData();
//									Map<String, Object> masterOrder = (Map<String, Object>)result.get("masterOrder");
//									Map<String, Object> tmpMap = new HashMap<String, Object>();
//									tmpMap.put("entCode", entCode);
//									tmpMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
//									tmpMap.put("gmtTime", sendManifest.getGmtTime());
//									tmpMap.put("userId", sendManifest.getUpdateUserId());
//									tmpMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
//									tmpMap.put("bound", masterOrder.get("BOUND"));
//									tmpMap.put("boundType", masterOrder.get("BOUND_TYPE"));
//									tmpMap.put("blNo", masterOrder.get("MBL_NO"));
//									commonService.sendKakaoAlarmGroup(tmpMap, "GOVCBR936");
							}catch(Exception e){
								log.error(Util.getPrintStacTraceString(e));
								try{
									ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestImp", BeanUtils.describe(sendManifest), e);
								}catch(Exception ee){
									log.error(Util.getPrintStacTraceString(ee));
								}
							}
						}
					});
					thread.start();
					if(!Util.null2str(orderSaveResult.getResultCode()).equals("ERROR")) {
						orderSaveResult.setResultCode("ebmsRest");
					}
				}else{
					ediCustomsManifestSendService.setCustomsManifestImp(sendManifest, cusSendType, orderSaveResult);
				}
			}
		}
		
		OrderSaveResult returnObj = new OrderSaveResult();
		returnObj.setResultCode(orderSaveResult.getResultCode());
		if(errorList.size() > 0) {
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("errorList", errorList);
			returnObj.setResultData(dataMap);
		}
		return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
	}
	
	@ApiOperation(value = "DKX수입목록신고문서 송신", notes="수입목록신고문서를 송신합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/sendDkxCustomsManifestImp", method = RequestMethod.POST)
    public ResponseEntity<OrderSaveResult> sendDkxCustomsManifestImp(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
        Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
        sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
        sendManifest.setRegisterUserName(sendManifest.getLogUserName());
        sendManifest.setManifestDkxYn("Y");

        Map<String, Object> enterpriseMap = commonMapper.selectEnterpriseInfo(Util.null2str(sendManifest.getEntCode()));
        String cusSendType = Util.null2str(enterpriseMap.get("CUS_SEND_TYPE"));

        OrderSaveResult orderSaveResult = ediCustomsManifestSendService.setDkxCustomsManifestImpData(sendManifest, cusSendType);
//        orderSaveResult.setResultCode("TEST");
        if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
            if(cusSendType.equals("REST")){
                if(!ediCommonService.isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
                    orderSaveResult.setResultCode("ebmsRestConnectionError");

                    OrderSaveResult returnObj = new OrderSaveResult();
                    returnObj.setResultCode(orderSaveResult.getResultCode());
                    if(orderSaveResult.getResultData() != null){
                        Map<String, Object> dataMap = new HashMap<String, Object>();
                        dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
                        dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
                        dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
                        returnObj.setResultData(dataMap);
                    }

                    try{
                        log.error("ebmsRestConnectionError!!!  [ entCode : " + entCode + " ,  masterOdJobNo : " + Util.null2str(sendManifest.getMasterOdJobNo()) + " ]");
                        throw new Exception("ebmsRestConnectionError!!!");
                    }catch(Exception e){
                        ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestImp", BeanUtils.describe(sendManifest), e);
                    }
                    return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
                }
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            ediCustomsManifestSendService.setDkxCustomsManifestImp(sendManifest, cusSendType, orderSaveResult);

//							Map<String, Object> result = orderSaveResult.getResultData();
//							Map<String, Object> masterOrder = (Map<String, Object>)result.get("masterOrder");
//							Map<String, Object> tmpMap = new HashMap<String, Object>();
//							tmpMap.put("entCode", entCode);
//							tmpMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
//							tmpMap.put("gmtTime", sendManifest.getGmtTime());
//							tmpMap.put("userId", sendManifest.getUpdateUserId());
//							tmpMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
//							tmpMap.put("bound", masterOrder.get("BOUND"));
//							tmpMap.put("boundType", masterOrder.get("BOUND_TYPE"));
//							tmpMap.put("blNo", masterOrder.get("MBL_NO"));
//							commonService.sendKakaoAlarmGroup(tmpMap, "GOVCBR936");
                        }catch(Exception e){
                            log.error(Util.getPrintStacTraceString(e));
                            try{
                                ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestImp", BeanUtils.describe(sendManifest), e);
                            }catch(Exception ee){
                                log.error(Util.getPrintStacTraceString(ee));
                            }
                        }
                    }
                });
                thread.start();

                orderSaveResult.setResultCode("ebmsRest");
            }else{
                ediCustomsManifestSendService.setCustomsManifestImp(sendManifest, cusSendType, orderSaveResult);
            }
        }

        OrderSaveResult returnObj = new OrderSaveResult();
        returnObj.setResultCode(orderSaveResult.getResultCode());
        if(orderSaveResult.getResultData() != null){
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
            dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
            dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
            returnObj.setResultData(dataMap);
        }
        return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
    }
	
	/** 임시 MRS테스트서버전송용 */
	@ApiOperation(value = "수입목록신고문서 송신", notes="수입목록신고문서를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendCustomsManifestImpTest", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendCustomsManifestImpTest(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		log.info("sendCustomsManifestImpTest");
		OrderSaveResult orderSaveResult = ediCustomsManifestSendService.setCustomsManifestImpDataTest(sendManifest);
		if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(!ediCommonService.isOnline("211.43.13.136", Util.null2int(customProperties.getEbmsPort()))) {
				orderSaveResult.setResultCode("ebmsRestConnectionError");
				
				OrderSaveResult returnObj = new OrderSaveResult();
				returnObj.setResultCode(orderSaveResult.getResultCode());
				if(orderSaveResult.getResultData() != null){
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
					dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
					dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
					returnObj.setResultData(dataMap);
				}
				
				try{
					log.error("ebmsRestConnectionError!!!  [ entCode : " + entCode + " ,  masterOdJobNo : " + Util.null2str(sendManifest.getMasterOdJobNo()) + " ]");
					throw new Exception("ebmsRestConnectionError!!!");
				}catch(Exception e){
					ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestImp", BeanUtils.describe(sendManifest), e);
				}
				return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
			}
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try{
						ediCustomsManifestSendService.setCustomsManifestImpTest(sendManifest, orderSaveResult);
					}catch(Exception e){
						log.error(Util.getPrintStacTraceString(e));
						try{
							ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestImp", BeanUtils.describe(sendManifest), e);
						}catch(Exception ee){
							log.error(Util.getPrintStacTraceString(ee));
						}
					}
				}
			});
			thread.start();
			
			orderSaveResult.setResultCode("ebmsRest");
		}
		
		OrderSaveResult returnObj = new OrderSaveResult();
		returnObj.setResultCode(orderSaveResult.getResultCode());
		if(orderSaveResult.getResultData() != null){
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
			dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
			dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
			returnObj.setResultData(dataMap);
		}
		return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
	}
	
	@ApiOperation(value = "제출번호 초기화", notes="제출번호를 초기화합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/resetSimpleDocNo", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> resetSimpleDocNo(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(ediCustomsManifestSendService.resetSimpleDocNo(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록전송 초기화", notes="통관목록전송문서를 초기화합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/resetSendCustomsManifest", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> resetSendCustomsManifest(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(ediCustomsManifestSendService.resetSendCustomsManifest(sendManifest), HttpStatus.OK);
	}
	
	@ApiOperation(value = "지정창고전송 초기화", notes="지정창고전송문서를 초기화합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/resetSendManifestGw", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> resetSendManifestGw(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		return new ResponseEntity<OrderSaveResult>(ediManifestGwSendService.resetSendManifestGw(sendManifest), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록결과조회", notes="통관목록 결과리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/customsManifestRes/list", method = RequestMethod.GET)
	public ResponseEntity<CustomsManifestResList> customsManifestResList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifest search) throws Exception {
		return new ResponseEntity<CustomsManifestResList>(manifestService.customsManifestResList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "통관목록결과리스트 엑셀파일생성", notes="통관목록결과리스트 엑셀파일생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/customsManifestRes/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> customsManifestResListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifest search, HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestService.customsManifestResList(search).getCustomsManifestResList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("customsManifestResList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록정정신고 이력삭제", notes="적하목록정정신고 이력삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/modSendReset", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> modSendReset(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrect, "registerUserId", "logUserName", null, "logUserNameEng");
		manifestCorrect.setRegisterUserName(manifestCorrect.getLogUserName());
		return new ResponseEntity<OrderSaveResult>(ediMfModifyExpSendService.modSendReset(manifestCorrect), HttpStatus.OK);
	}
	
	@ApiOperation(value = "수출 적하목록정정신고 문서송신(4세대)", notes="수출 적하목록정정신고문서(4세대)를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendManifestModifyExp4th", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendManifestModifyExp4th(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "registerUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		return new ResponseEntity<OrderSaveResult>(ediMfModifyExpSendService.sendManifestModifyExp4th(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "수입 적하목록정정신고 문서송신(4세대)", notes="수입 적하목록정정신고문서(4세대)를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendManifestModifyImp4th", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendManifestModifyImp4th(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "registerUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		return new ResponseEntity<OrderSaveResult>(ediMfModifyImpSendService.sendManifestModifyImp4th(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "항공 적하목록신고 문서송신(4세대)", notes="항공 적하목록신고문서(4세대)를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendManifestAir4th", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendManifestAir4th(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		return new ResponseEntity<OrderSaveResult>(manifestService.sendManifestAir4th(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "항공 적하목록신고검증(4세대)", notes="항공 적하목록신고문서(4세대)를 검증합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendManifestAir4thVali", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendManifestAir4thVali(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		return new ResponseEntity<OrderSaveResult>(manifestService.sendManifestAir4thVali(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "관우회 문서송신", notes="관우회문서를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendManifestGw", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendManifestGw(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		/** stockMax에서 lock발생해서 transaction 분리 */
		orderSaveResult = ediManifestGwSendService.sendManifestGwSeqNo(sendManifest);
		
		if(!Util.null2str(sendManifest.getConfirmYn()).equals("Y") || orderSaveResult.getResultData() != null){
			return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK); 
		}else{
			Map<String, Object> returnMap = orderSaveResult.getReturnMap();
			/** 150건단위로 롤백되게 하기 위해서 수정 */
//			return new ResponseEntity<OrderSaveResult>(ediManifestGwSendService.sendManifestGw(sendManifest, request, returnMap), HttpStatus.OK);
			
			OrderSaveResult orderSaveResult2 = new OrderSaveResult();
			Map<String, Object> result = new HashMap<String, Object>();
			Map<String, Object> docSend = (Map<String, Object>)returnMap.get("docSend");
			List<Map<String, Object>> masterOrderList = (List<Map<String, Object>>)returnMap.get("masterOrderList");
			List<Map<String, Object>> houseOrderList = null;
			List<String> errList = new ArrayList<String>();
			int houseCnt = 0;
			
			String stockStr = Util.null2str(returnMap.get("stockStr"));
			docSend.put("yyyy", stockStr.substring(0, 4));
			docSend.put("stockStartNo", Util.null2int(stockStr.substring(4)));
			
			
			if(sendManifest.getGwTest() != null && "Y".equals(sendManifest.getGwTest().get("custom"))){
				docSend.put("custom", sendManifest.getGwTest().get("custom"));
				docSend.put("mode", sendManifest.getGwTest().get("mode"));
				docSend.put("fileTransferMode", sendManifest.getGwTest().get("fileTransferMode"));
				docSend.put("storeFileStream", sendManifest.getGwTest().get("storeFileStream"));
			}
			
			
			int fileSeq = 1;
			int masterCnt = 10001;
			
			int createOrderCnt = 5000;
			
			if("sendGw50".equals(sendManifest.getAction()) || "sendGwCancel50".equals(sendManifest.getAction())){
				createOrderCnt = 50;
			}
			
			/** jbs.g1에만 반영     전송실패해도 로그남기기위해 임시처리 */
//			int createOrderCnt = 10000;
			
			for(Map<String, Object> masterData : masterOrderList){
				houseOrderList = (List<Map<String, Object>>)masterData.get("houseOrderList");
				
				houseCnt = houseOrderList.size();
				for(int i = 0; i < houseCnt; i+=createOrderCnt){
					if(i + createOrderCnt < houseCnt) {
						ediManifestGwSendService.getManifestGwSend(docSend, masterData, houseOrderList.subList(i, i + createOrderCnt), (Map<String, Object>)masterData.get("kctdiInfo"), errList, i, masterCnt++, fileSeq);
					}else{
						ediManifestGwSendService.getManifestGwSend(docSend, masterData, houseOrderList.subList(i, houseCnt), (Map<String, Object>)masterData.get("kctdiInfo"), errList, i, masterCnt++, fileSeq);
					}
					fileSeq++;
				}
			}
			
			if(errList.size() > 0){
				result.put("errList", errList);
			}
			
			orderSaveResult2.setResultData(result);
			return new ResponseEntity<OrderSaveResult>(orderSaveResult2, HttpStatus.OK);
		}
	}
	
	@ApiOperation(value = "수입화물진행정보 배치실행데이터 저장", notes="수입화물진행정보 배치실행데이터 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insertCargoProgress", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertCargoProgress(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		return new ResponseEntity<OrderSaveResult>(ediManifestGwSendService.insertCargoProgress(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "특송오더생성", notes="특송오더생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insertEms", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertEms(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(manifestService.insertEms(sendManifest, request), HttpStatus.OK);
	}

	//적하목록 정정
	@ApiOperation(value = "ECM 적하목록  정정 마스터 리스트 조회", notes="ECM 적하목록  정정 마스터 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/search/master/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestCorrectList> selectCorrectMasterList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestCorrectList search) throws Exception {
		return new ResponseEntity<ManifestCorrectList>(manifestService.selectCorrectMasterList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록 정정 하우스 리스트 조회", notes="ECM 적하목록 정정 하우스 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/search/house/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestCorrectList> selectCorrectHouseList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestCorrectList search) throws Exception {
		return new ResponseEntity<ManifestCorrectList>(manifestService.selectCorrectHouseList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록 정정 컨테이너 리스트 조회", notes="ECM 적하목록 정정 컨테이너 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/search/container/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestCorrectList> selectCorrectContainerList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestCorrectList search) throws Exception {
		return new ResponseEntity<ManifestCorrectList>(manifestService.selectCorrectContainerList(search), HttpStatus.OK);
	}
	
	//적하목록 정정번호 추출
	@ApiOperation(value = "ECM 적하목록  정정번호 조회", notes="ECM 적하목록 정정번호 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/get/serialNo", method = RequestMethod.GET)
	public ResponseEntity<ManifestCorrectMaster> getSerialNo(@PathVariable("entCode") String entCode, SearchManifestCorrectList search) throws Exception {
		return new ResponseEntity<ManifestCorrectMaster>(manifestService.getSerialNo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록  정정정보 조회", notes="ECM 적하목록 정정정보 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/get/modInfo", method = RequestMethod.GET)
	public ResponseEntity<ManifestCorrectMaster> getModInfo(@PathVariable("entCode") String entCode, SearchManifestCorrectList search) throws Exception {
		return new ResponseEntity<ManifestCorrectMaster>(manifestService.getModInfo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 정정 insert", notes="적하목록 정정 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/insert", method = RequestMethod.POST)
	public ResponseEntity<ManifestCorrectSaveResult> insertCorrect(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrect, "updateUserId", "logUserName", null, "logUserNameEng");
		manifestCorrect.setRegisterUserId(manifestCorrect.getUpdateUserId());
		manifestCorrect.setRegisterUserName(manifestCorrect.getLogUserName());
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.insertCorrect(manifestCorrect), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 정정 삭제", notes="적하목록 정정을 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/delete/batch", method = RequestMethod.DELETE)
	public ResponseEntity<ManifestCorrectSaveResult> deleteCorrect(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrectDelete manifestCorrectDelete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrectDelete, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.deleteCorrect(manifestCorrectDelete), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 정정 삭제", notes="적하목록 정정을 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/delete/mbl", method = RequestMethod.DELETE)
	public ResponseEntity<ManifestCorrectSaveResult> deleteCorrectMbl(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrectDelete manifestCorrectDelete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrectDelete, "userId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.deleteCorrectMbl(manifestCorrectDelete, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 정정사유 수정", notes="적하목록 정정사유 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/modReason/update", method = RequestMethod.POST)
	public ResponseEntity<ManifestCorrectSaveResult> updateModReason(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrect, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.updateModReason(manifestCorrect, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 마스터정보 수정", notes="적하목록 마스터정보 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/modMbl/update", method = RequestMethod.POST)
	public ResponseEntity<ManifestCorrectSaveResult> updateModMbl(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrect, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.updateModMbl(manifestCorrect, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 컨테이너정정 정보수정", notes="적하목록 컨테이너추가 정보수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/container/update", method = RequestMethod.POST)
	public ResponseEntity<ManifestCorrectSaveResult> updateContainer(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrect, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.updateContainer(manifestCorrect, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록 정정 수신결과리스트 조회", notes="ECM 적하목록 정정 수신결과리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/search/recvResult/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestCorrectRecvResultList> selectManifestCorrectRecvResultList(@PathVariable("entCode") String entCode, SearchManifestCorrectList search) throws Exception {
		return new ResponseEntity<ManifestCorrectRecvResultList>(manifestService.selectManifestCorrectRecvResultList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록  정정 팝업 정정 마스터 리스트 조회", notes="ECM 적하목록  정정 팝업 마스터 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/popup/search/master/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestCorrectList> selectPopupCorrectMasterList(@PathVariable("entCode") String entCode, SearchManifestCorrectList search) throws Exception {
		return new ResponseEntity<ManifestCorrectList>(manifestService.selectPopupCorrectMasterList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 정정 copy", notes="적하목록 정정 copy합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/copy", method = RequestMethod.POST)
	public ResponseEntity<ManifestCorrectSaveResult> copyCorrect(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrect, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.copyCorrect(manifestCorrect), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 정정 결과수신요청", notes="적하목록 정정 결과수신요청합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/resultRequest", method = RequestMethod.POST)
	public ResponseEntity<ManifestCorrectSaveResult> correctResultRequest(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrect, "registerUserId", "logUserName", null, "logUserNameEng");
		manifestCorrect.setRegisterUserName(manifestCorrect.getLogUserName());
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.correctResultRequest(manifestCorrect), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 항목정정 오더 업데이트", notes="적하목록 항목정정 오더 업데이트합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/correct/update", method = RequestMethod.POST)
	public ResponseEntity<ManifestCorrectSaveResult> correctUpdate(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrect, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.correctUpdate(entCode, manifestCorrect), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하정정 ExcelUpload Check", notes="적하정정 ExcelUpload Check합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload/{gubun}/{modJobNo}/check", method = RequestMethod.POST)
	public ResponseEntity<ManifestCorrectSaveResult> excelUploadCheck(@PathVariable("entCode") String entCode, @PathVariable("gubun") String gubun, @PathVariable("modJobNo") String modJobNo,@RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.excelUploadCheck(uploadExcel,gubun, entCode, modJobNo), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하정정 ExcelUpload 수정", notes="적하정정 Excel내용으로 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excel/update", method = RequestMethod.POST)
	public ResponseEntity<ManifestCorrectSaveResult> excelUploadUpdate(@PathVariable("entCode") String entCode, @RequestBody ManifestCorrect manifestCorrect, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, manifestCorrect, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.excelUploadUpdate(manifestCorrect), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 수입 마스터 신고세관/과 조회", notes="적하목록 수입 마스터 신고세관/과 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getCustomsCode", method = RequestMethod.POST)
	public ResponseEntity<ManifestCorrectSaveResult> getCustomsCode(@PathVariable("entCode") String entCode, @RequestBody SearchManifestCustoms searchCustoms) throws Exception {
		return new ResponseEntity<ManifestCorrectSaveResult>(manifestService.getCustomsCode(entCode, searchCustoms), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록 목록신고제출번호 조회", notes="적하목록 목록신고제출번호 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/getSimpleDocNo", method = RequestMethod.POST)
	public ResponseEntity<ManifestOrderSaveResult> getSimpleDocNo(@PathVariable("entCode") String entCode, @RequestBody SearchManifestSimpleDocNo search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", null, null);
		return new ResponseEntity<ManifestOrderSaveResult>(manifestService.getSimpleDocNo(search), HttpStatus.OK);
	}
	@ApiOperation(value = "적하목록수입 목록신고제출번호 수정", notes="적하목록 목록신고제출번호 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/simpleDocNo/update", method = RequestMethod.POST)
	/** 사용안함 */
	public ResponseEntity<ManifestOrderSaveResult> updateSimpleDocNo(@PathVariable("entCode") String entCode, @RequestBody SearchManifestSimpleDocNo search) throws Exception {
		return new ResponseEntity<ManifestOrderSaveResult>(manifestService.updateSimpleDocNo(search), HttpStatus.OK);
	}
	
//	/**
//	 * 엑셀 list
//	 * @param entCode
//	 * @param functionOrder
//	 * @param request
//	 * @return
//	 * @throws Exception
//	 */
//	@SuppressWarnings("unchecked")
//	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
//	@ApiImplicitParams({
//		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
//	})	
//	@RequestMapping(value="/in/search/list/excel", method = RequestMethod.GET)
//	public ResponseEntity<DownloadFileInfo> selectManifestInExcel(
//						@PathVariable("entCode") String entCode,
//						@QueryStringArgResolver SearchManifestInList search,
//						HttpServletRequest request) throws Exception {
//		
//		search.setPage(0);
//		ManifestInList orderList = manifestService.selectHouseInList(search);
//				
//		ObjectMapper objectMapper = new ObjectMapper();
//		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
//		map.put("data", orderList.getManifestInHouseList());
//		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Manifest_List", map), HttpStatus.OK);
//	}
	
	@ApiOperation(value = "업무마감", notes="업무마감 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/businessFinish", method = RequestMethod.POST)
	public ResponseEntity<ManifestOrderSaveResult> businessFinish(@PathVariable("entCode") String entCode, @RequestBody SendDelivery sendDelivery, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendDelivery, "registerUserId", null, null);
		return new ResponseEntity<ManifestOrderSaveResult>(manifestService.businessFinish(sendDelivery), HttpStatus.OK);
	}
	
	/**
	 * 엑셀 list - 지정창고반출요청(해상/군산항)
	 * @param entCode
	 * @param functionOrder
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "지정창고반출요청(해상/군산항) 엑셀 list 조회", notes="지정창고반출요청(해상/군산항) 엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/in/search/list/carryoutExcel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectManifestInExcel2(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchOrderList search,
						HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "registerUserId", null, null);
		search.setPage(0);
		CarryoutInList orderList = manifestService.selectCarryoutInList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getManifestInHouseList());
		String fileTitle = Util.null2str(search.getExcelFileName()).equals("carryout") ? "Carryout_List" : "Carryout_Gunsan_List";
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(fileTitle, map), HttpStatus.OK);
	}
	
	/**
	 * 엑셀 list - 일일반입예상
	 * @param entCode
	 * @param functionOrder
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "일일반입예상 엑셀 list 조회", notes="일일반입예상 엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/in/search/list/carryinExcel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectManifestInExcel3(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchCarryoutInList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		CarryinInList orderList = manifestService.selectCarryinInList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getManifestInHouseList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Carryin_List", map), HttpStatus.OK);
	}
	
	/**
	 * 엑셀 list - 적하목록정보 (UPS)
	 * @param entCode
	 * @param functionOrder
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "적하목록정보 엑셀 list 조회 (UPS)", notes="적하목록정보 엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/in/search/list/excel/ups", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectManifestInExcel4(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchManifestInList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		ManifestInList orderList = manifestService.selectManifestInList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getManifestInHouseList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Ups_Manifest_List", map, "xlsx"), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록정보 업데이트 ExcelUpload 유효성체크 (UPS)", notes="적하목록정보 업데이트 ExcelUpload 유효성체크합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/ups/excelUpload/check", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> upsExcelUploadCheck(@PathVariable("entCode") String entCode, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestService.upsExcelUploadCheck(uploadExcel, entCode), HttpStatus.OK);
	}
	@ApiOperation(value = "tracking 업데이트 ExcelUpload 유효성체크 (UPS)", notes="tracking 업데이트 ExcelUpload 유효성체크합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/ups/excelUploadTracking/check", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> upsExcelUploadTracking(@PathVariable("entCode") String entCode, @RequestParam("uploadExcel") MultipartFile uploadExcel) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestService.upsExcelUploadCheckTracking(uploadExcel, entCode), HttpStatus.OK);
	}
	
	@ApiOperation(value = "적하목록정보 업데이트 ExcelUpload", notes="적하목록정보 Excel내용으로 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/ups/excelUpload", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> upsExcelUploadUpdate(@PathVariable("entCode") String entCode, @RequestBody ExcelUploadList excelUploadList) throws Exception {
		
		OrderSaveResult orderSaveResult = manifestService.upsExcelUpload(excelUploadList);
		if("updateBmsCdType".equals(orderSaveResult.getResultCode())){
			List<Map<String, Object>> updateCdTypeList = (List<Map<String, Object>>)orderSaveResult.getResultData().get("updateCdTypeList");
			commonService.updateBmsCdType(updateCdTypeList);
			orderSaveResult.setResultCode(null);
			orderSaveResult.setResultData(null);
		}
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
	
	@ApiOperation(value = "tracking 업데이트 ExcelUpload", notes="tracking Excel내용으로 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/ups/excelUploadTracking", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> upsExcelUploadTracking(@PathVariable("entCode") String entCode, @RequestBody ExcelUploadList excelUploadList) throws Exception {
		return new ResponseEntity<OrderSaveResult>(manifestService.upsExcelUploadTracking(excelUploadList), HttpStatus.OK);
	}
	
//	/**
//	 * 엑셀 list
//	 * @param entCode
//	 * @param functionOrder
//	 * @param request
//	 * @return
//	 * @throws Exception
//	 */
//	@SuppressWarnings("unchecked")
//	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
//	@ApiImplicitParams({
//		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
//	})	
//	@RequestMapping(value="/out/search/list/excel", method = RequestMethod.GET)
//	public ResponseEntity<DownloadFileInfo> selectManifestOutExcel(
//						@PathVariable("entCode") String entCode,
//						@QueryStringArgResolver SearchManifestOutList search,
//						HttpServletRequest request) throws Exception {
//		
//		search.setPage(0);
//		ManifestOutList orderList = manifestService.selectHouseOutList(search);
//				
//		ObjectMapper objectMapper = new ObjectMapper();
//		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
//		map.put("data", orderList.getManifestOutHouseList());
//		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Manifest_List", map), HttpStatus.OK);
//	}
	
	/**
	 * 엑셀 list
	 * @param entCode
	 * @param functionOrder
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/correct/search/house/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectCorrectHouseExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchManifestCorrectList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		ManifestCorrectList orderList = manifestService.selectCorrectHouseList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getManifestCorrectHouseList());
		String fileName = "";
		if("upload".equals(search.getAction())){
			fileName = "Correction_Upload_List";
		}else{
			fileName = "Correction_List";
		}
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(fileName, map), HttpStatus.OK);
	}
	
	
	
	
	@ApiOperation(value = "개인통관오류건 제출번호 분리", notes="개인통관오류건 제출번호 분리")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/customsErrSeparation", method = RequestMethod.POST)
	public ResponseEntity<ManifestSeparationResult> customsErrSeparation(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		return new ResponseEntity<ManifestSeparationResult>(manifestService.customsErrSeparation(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "오더 5000건 이상 제출번호 분리", notes="개인통관오류건 제출번호 분리")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/separation", method = RequestMethod.POST)
	public ResponseEntity<ManifestSeparationResult> separationSimpleDocNo(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		return new ResponseEntity<ManifestSeparationResult>(manifestService.separationSimpleDocNo(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "HSN 재생성", notes="HSN 재생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/reHsn", method = RequestMethod.POST)
	public ResponseEntity<ManifestOrderSaveResult> reHsn(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		return new ResponseEntity<ManifestOrderSaveResult>(manifestService.reHsn(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "MRN/MSN 수동 업데이트", notes="MRN/MSN 수동 업데이트합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/mrnMsn/ManualUpdate", method = RequestMethod.POST)
	public ResponseEntity<ManifestOrderSaveResult> mrnMsnManualUpdate(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		return new ResponseEntity<ManifestOrderSaveResult>(manifestService.mrnMsnManualUpdate(sendManifest, request), HttpStatus.OK);
	}
	
    @ApiOperation(value = "목록건 수하인정보 매칭처리", notes="목록건 수하인정보 매칭처리 업데이트합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/manifestConsigneeMatching", method = RequestMethod.POST)
    public ResponseEntity<ManifestOrderSaveResult> manifestConsigneeMatching(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {    	
    	return new ResponseEntity<ManifestOrderSaveResult>(manifestService.manifestConsigneeMatching(sendManifest, request), HttpStatus.OK);
    }
    

	@ApiOperation(value = "수입화물진행정보 수동 저장", notes="수입화물진행정보를 수동 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/manualInsertCargoProgress", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> manualInsertCargoProgress(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		return new ResponseEntity<OrderSaveResult>(manifestService.manualInsertCargoProgress(sendManifest, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "수입목록취하문서 송신", notes="수입목록취하문서를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendCustomsManifestCancelImp", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendCustomsManifestCancelImp(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, "updateUserId", "logUserName", null, "logUserNameEng");
		sendManifest.setRegisterUserId(sendManifest.getUpdateUserId());
		sendManifest.setRegisterUserName(sendManifest.getLogUserName());
		Map<String, Object> enterpriseMap = commonMapper.selectEnterpriseInfo(Util.null2str(sendManifest.getEntCode()));
		String cusSendType = Util.null2str(enterpriseMap.get("CUS_SEND_TYPE"));
		
		OrderSaveResult orderSaveResult = ediCustomsManifestSendService.setCustomsManifestCancelImpData(sendManifest, cusSendType);
		
		
		if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(cusSendType.equals("REST")){
				if(!ediCommonService.isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
					orderSaveResult.setResultCode("ebmsRestConnectionError");
					
					OrderSaveResult returnObj = new OrderSaveResult();
					returnObj.setResultCode(orderSaveResult.getResultCode());
					if(orderSaveResult.getResultData() != null){
						Map<String, Object> dataMap = new HashMap<String, Object>();
						dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
						dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
						dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
						returnObj.setResultData(dataMap);
					}
					
					try{
						log.error("ebmsRestConnectionError!!!  [ entCode : " + entCode + " ,  masterOdJobNo : " + Util.null2str(sendManifest.getMasterOdJobNo()) + " ]");
						throw new Exception("ebmsRestConnectionError!!!");
					}catch(Exception e){
						ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestCancelImp", BeanUtils.describe(sendManifest), e);
					}
					return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
				}
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try{
							ediCustomsManifestSendService.setCustomsManifestCancelImp(sendManifest, cusSendType, orderSaveResult);
							
//							Map<String, Object> result = orderSaveResult.getResultData();
//							Map<String, Object> masterOrder = (Map<String, Object>)result.get("masterOrder");
//							Map<String, Object> tmpMap = new HashMap<String, Object>();
//							tmpMap.put("entCode", entCode);
//							tmpMap.put("entBusinessCode", sendManifest.getEntBusinessCode());
//							tmpMap.put("gmtTime", sendManifest.getGmtTime());
//							tmpMap.put("userId", sendManifest.getUpdateUserId());
//							tmpMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
//							tmpMap.put("bound", masterOrder.get("BOUND"));
//							tmpMap.put("boundType", masterOrder.get("BOUND_TYPE"));
//							tmpMap.put("blNo", masterOrder.get("MBL_NO"));
//							commonService.sendKakaoAlarmGroup(tmpMap, "GOVCBR936");
						}catch(Exception e){
							log.error(Util.getPrintStacTraceString(e));
							try{
								ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestCancelImp", BeanUtils.describe(sendManifest), e);
							}catch(Exception ee){
								log.error(Util.getPrintStacTraceString(ee));
							}
						}
					}
				});
				thread.start();
				
				orderSaveResult.setResultCode("ebmsRest");
			}else{
				ediCustomsManifestSendService.setCustomsManifestCancelImp(sendManifest, cusSendType, orderSaveResult);
			}
		}
		
		OrderSaveResult returnObj = new OrderSaveResult();
		returnObj.setResultCode(orderSaveResult.getResultCode());
		if(orderSaveResult.getResultData() != null){
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
			dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
			dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
			returnObj.setResultData(dataMap);
		}
		return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
	}
	
	@ApiOperation(value = "취하이력삭제", notes="하우스 체크건의 취하여부(DROP_YN)를 N으로 변경합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/deleteDropHist", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> deleteDropHist(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(ediCustomsManifestSendService.deleteDropHist(sendManifest), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관목록전송 초기화", notes="통관목록전송문서를 초기화합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/resetSendCustomsManifestCancel", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> resetSendCustomsManifestCancel(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendManifest, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(ediCustomsManifestSendService.resetSendCustomsManifestCancel(sendManifest), HttpStatus.OK);
	}
	
	@ApiOperation(value = "ECM 적하목록 수입 특송생성데이터 조회", notes="ECM 적하목록 수입 특송생성데이터 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/selectInsertEms/list", method = RequestMethod.GET)
	public ResponseEntity<ManifestInList> selectInsertEms(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchManifestInList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "srchLoginId", null, null);
		ManifestInList manifestInList = new ManifestInList();
		try {
			manifestInList  = manifestService.selectInsertEms(search);
		}catch(Exception e) {
			throw e;
		}
		return new ResponseEntity<ManifestInList>(manifestInList, HttpStatus.OK);
	}
	
	@ApiOperation(value = "UPS 통관목록 출력물 전송 대상 건 조회", notes="UPS 통관목록 출력물 전송 대상 건 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/selectExpEmailList", method = RequestMethod.POST)
    public ResponseEntity<ManifestOrderSaveResult> selectExpEmailList(@PathVariable("entCode") String entCode, @RequestBody SendManifest sendManifest, HttpServletRequest request) throws Exception {    	
    	return new ResponseEntity<ManifestOrderSaveResult>(manifestService.selectExpEmailList(sendManifest, request), HttpStatus.OK);
    }
	
	@ApiOperation(value = "UPS 통관목록 현황 집계 조회", notes="UPS 통관목록 현황 집계 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
	@RequestMapping(value="/manifestAggregate", method = RequestMethod.GET)
	public ResponseEntity<ManifestAggregateList> selectManifestAggregateList(@PathVariable("entCode") String entCode,@QueryStringArgResolver SearchManifestInList search ) throws Exception{
		ManifestAggregateList manifestAggregateList = new ManifestAggregateList();
		manifestAggregateList = manifestService.selectManifestAggregateList(search);
		search.setPageSize(100000);
		return new ResponseEntity<ManifestAggregateList>(manifestAggregateList, HttpStatus.OK); 
	}
	
	@ApiOperation(value = "UPS 통관목록 현황 집계 조회 Excel 저장", notes="UPS 통관목록 현황 집계 조회 Excel 저장")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
	@RequestMapping(value="/manifestAggregate/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectManifestAggregateListExcel(@PathVariable("entCode") String entCode,@RequestBody SearchManifestInList search ) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", manifestService.selectManifestAggregateList(search).getManifestAggregateList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("manifestAggregate_list", map), HttpStatus.OK);
	}
	
	
}
