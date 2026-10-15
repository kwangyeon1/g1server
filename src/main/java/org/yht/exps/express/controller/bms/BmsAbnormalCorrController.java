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
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalList;
import org.yht.exps.express.model.bms.bmsAbnormal.search.SearchCarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.BatchBmsAbnormalCorr;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalCorr;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalCorrDetailList;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalCorrList;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalCorrSaveResult;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.DeleteCarryInAbnormalCorr;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.search.SearchCarryInAbnormalCorr;
import org.yht.exps.express.service.bms.BmsAbnormalCorrService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Arrays;
import java.util.Map;

@RestController
@Api(tags="BmsInabcr")
@RequestMapping("/{entCode}/bms/inab/cr")
public class BmsAbnormalCorrController {

    @Resource(name = "bmsAbnormalCorrService")
    BmsAbnormalCorrService bmsAbnormalCorrService;
    
    @Autowired
	JwtTokenProvider jwtTokenProvider;

    @ApiOperation(value = "반입물품이상신고정정 - 반입이상 리스트 조회", notes = "반입물품이상신고정정 - 반입이상 리스트를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/abnormal/search/list")
    public ResponseEntity<CarryInAbnormalList> selectAbnormalList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCarryInAbnormalCorr search, HttpServletRequest request) throws Exception{
        return new ResponseEntity<>(bmsAbnormalCorrService.selectAbnormalList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입물품이상신고정정 - 반입물품이상신고정정 리스트 조회", notes = "반입물품이상신고정정 - 반입물품이상신고정정 리스트를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/abnormalCorr/search/list")
    public ResponseEntity<CarryInAbnormalCorrList> selectAbnormalCorrList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCarryInAbnormalCorr search, HttpServletRequest request) throws Exception{
        return new ResponseEntity<>(bmsAbnormalCorrService.selectAbnormalCorrList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입물품이상신고정정 삭제", notes="반입물품이상신고정정 삭제합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormalCorr/delete", method = RequestMethod.DELETE)
    public ResponseEntity<CarryInAbnormalCorrSaveResult> deleteAbnormalCorr(@PathVariable("entCode") String entCode, @RequestBody DeleteCarryInAbnormalCorr delete, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
    	return new ResponseEntity<>(bmsAbnormalCorrService.deleteAbnormalCorr(delete, request), HttpStatus.OK);
    }

    @ApiOperation(value = "반입이상 또는 반입이상정정에 존재하는 데이터인지 check", notes="반입이상 또는 반입이상정정에 존재하는 데이터인지 check")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/checkExistAbnormal", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormalCorrDetailList> checkExistAbnormal(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCarryInAbnormalCorr search) throws Exception {
        return new ResponseEntity<>(bmsAbnormalCorrService.checkExistAbnormal(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입물품이상신고정정  insert", notes="반입물품이상신고정정을 insert합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormalCorr/insert", method = RequestMethod.POST)
    public ResponseEntity<CarryInAbnormalCorrSaveResult> insertAbnormalCorr(@PathVariable("entCode") String entCode, @RequestBody CarryInAbnormalCorr carryInAbnormalCorr, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryInAbnormalCorr, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
    	return new ResponseEntity<CarryInAbnormalCorrSaveResult>(bmsAbnormalCorrService.insertAbnormalCorr(carryInAbnormalCorr, request), HttpStatus.OK);
    }

    @ApiOperation(value = "반입물품이상신고정정  insert", notes="반입물품이상신고정정을 insert합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormalCorr/update", method = RequestMethod.PUT)
    public ResponseEntity<CarryInAbnormalCorrSaveResult> updateAbnormalCorr(@PathVariable("entCode") String entCode, @RequestBody CarryInAbnormalCorr carryInAbnormalCorr, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryInAbnormalCorr, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
    	return new ResponseEntity<CarryInAbnormalCorrSaveResult>(bmsAbnormalCorrService.updateAbnormalCorr(carryInAbnormalCorr, request), HttpStatus.OK);
    }

    @ApiOperation(value = "반입이상신고정정 상세 한건 조회", notes="반입이상신고정정 상세 한건을 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormalCorr/search/detailAbnormalCorr", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormalCorr> selectDetailAbnormalCorr(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCarryInAbnormalCorr search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsAbnormalCorrService.selectDetailAbnormalCorr(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입이상정정 HOUSE 정보(HBL 리스트) 조회", notes="반입이상정정 HOUSE 정보(HBL 리스트) 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/getAbnormalHblCorrList", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormalCorrDetailList> selectAbnormalHblCorrList(@PathVariable("entCode") String entCode, SearchCarryInAbnormalCorr search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsAbnormalCorrService.selectAbnormalHblCorrList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입물품이상정정 조회 팝업", notes="반입물품이상정정 조회 팝업")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/getAbnormalCorrListPopup", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormalCorrDetailList> selectAbnormalCorrListPopup(@PathVariable("entCode") String entCode, SearchCarryInAbnormalCorr search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsAbnormalCorrService.selectAbnormalCorrListPopup(search), HttpStatus.OK);
    }

    @ApiOperation(value = "정정차수 조회", notes="정정차수 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/getMaxChangeCount", method = RequestMethod.GET)
    public int selectMaxChangeCount(@PathVariable("entCode") String entCode, SearchCarryInAbnormalCorr search) throws Exception {
        search.setSrchEntCode(entCode);
        return bmsAbnormalCorrService.selectMaxChangeCount(search);
    }

	/** 관세청 XML 송신(Send) - GOVCBRDKP(특송화물 반입물품 이상보고 정정신청서) */
	@ApiOperation(value = "관세청 XML 송신(Send)", notes = "GOVCBRDKP(특송화물 반입물품 이상보고 정정신청서) 목록을 전송합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/exec/send/carryInAbnormalCorrList", method = RequestMethod.POST)
	public ResponseEntity<CarryInAbnormalCorrSaveResult> sendOutToXml(@PathVariable("entCode") String entCode, @RequestBody BatchBmsAbnormalCorr params, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, params, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		params.setEntCode(entCode);
		return new ResponseEntity<CarryInAbnormalCorrSaveResult>(bmsAbnormalCorrService.sendOutToXml(params, request), HttpStatus.OK);
	}

    @ApiOperation(value = "반입이상정정 엑셀 list 다운로드", notes="반입이상정정 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormalCorr/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectAbnormalCorrExcel(@PathVariable("entCode") String entCode, @RequestBody SearchCarryInAbnormalCorr search, HttpServletRequest req) throws Exception {
    	search.setSrchEntCode(entCode);
        search.setPage(0);
        CarryInAbnormalCorrList carryInAbnormalCorrList  = bmsAbnormalCorrService.selectAbnormalCorrList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", carryInAbnormalCorrList.getCarryInAbnormalCorrList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }
    
    @ApiOperation(value = "반입이상 엑셀 list 다운로드", notes="반입이상 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormal/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectAbnormalExcel(@PathVariable("entCode") String entCode, @RequestBody SearchCarryInAbnormalCorr search, HttpServletRequest req) throws Exception {
    	search.setSrchEntCode(entCode);
        search.setPage(0);
        CarryInAbnormalList carryInAbnormalList  = bmsAbnormalCorrService.selectAbnormalList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", carryInAbnormalList.getCarryInAbnormalList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }
}
