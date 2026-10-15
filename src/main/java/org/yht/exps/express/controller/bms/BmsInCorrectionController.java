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
import org.yht.exps.express.model.bms.bmsInCorrection.BatchBmsInCorrection;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChange;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChangeDetail;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChangeDetailList;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChangeList;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChangeSaveResult;
import org.yht.exps.express.model.bms.bmsInCorrection.DeleteCarryInChange;
import org.yht.exps.express.model.bms.bmsInCorrection.search.SearchInCorrection;
import org.yht.exps.express.model.bms.carryin.CarryInList;
import org.yht.exps.express.model.bms.carryin.search.SearchInList;
import org.yht.exps.express.service.bms.BmsInCorrectionService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Arrays;
import java.util.Map;

@RestController
@Api(tags="BmsIncr")
@RequestMapping("/{entCode}/bms/incr")
public class BmsInCorrectionController {

    @Resource(name = "bmsInCorrectionService")
    BmsInCorrectionService bmsInCorrectionService;
    
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @ApiOperation(value = "반입정정신고 -  반입 리스트 조회", notes="반입정정신고 -  반입 리스트 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryIn/search/list", method = RequestMethod.GET)
    public ResponseEntity<CarryInList> selectCarryInList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInCorrection search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsInCorrectionService.selectCarryInList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입정정신고 - 반입정정 리스트 조회", notes = "반입정정신고 - 반입정정 리스트를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/carryInChange/search/list")
    public ResponseEntity<CarryInChangeList> selectCarryInChangeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInCorrection search, HttpServletRequest request) throws Exception{
        return new ResponseEntity<>(bmsInCorrectionService.selectCarryInChangeList(search),HttpStatus.OK);
    }

    @ApiOperation(value = "반입정정신고 상세 한건 조회", notes="반입정정신고 상세 한건 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/get", method = RequestMethod.GET)
    public ResponseEntity<CarryInChangeDetail> selectCarryInAndCarryInChangeDetail(@PathVariable("entCode") String entCode, SearchInCorrection search) throws Exception {
        return new ResponseEntity<>(bmsInCorrectionService.selectCarryInAndCarryInChangeDetail(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입정정신청 번호타입에 따라 조회", notes="반입정정신청 번호타입에 따라 조회  상세 화면 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/getCarryInChangeBySrchNo", method = RequestMethod.GET)
    public ResponseEntity<CarryInChangeDetailList> selectCarryInChangeBySrchNo(@PathVariable("entCode") String entCode, SearchInCorrection search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsInCorrectionService.selectCarryInChangeBySrchNo(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입정정신고 - 반입정정 상세 한건 조회", notes="반입정정신고 - 반입정정상세 한건을 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryInChangeDetail", method = RequestMethod.GET)
    public ResponseEntity<CarryInChange> selectDetailCarryInChange(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInCorrection search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsInCorrectionService.selectCarryInChangeDetail(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입정정신청  insert", notes="반입정정신청을 insert합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryInChange/insert", method = RequestMethod.POST)
    public ResponseEntity<CarryInChangeSaveResult> insertCarryInChange(@PathVariable("entCode") String entCode, @RequestBody CarryInChange carryInChange, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryInChange, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");

    	return new ResponseEntity<CarryInChangeSaveResult>(bmsInCorrectionService.insertCarryInChange(carryInChange, request), HttpStatus.OK);
    }

    @ApiOperation(value = "반입정정신청 update", notes="반입정정신청 update합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
    })
    @RequestMapping(value="/carryInChange/update", method = RequestMethod.PUT)
    public ResponseEntity<CarryInChangeSaveResult> updateCarryInChange(@PathVariable("entCode") String entCode, @RequestBody CarryInChange carryInChange, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryInChange, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
    	
    	return new ResponseEntity<CarryInChangeSaveResult>(bmsInCorrectionService.updateCarryInChange(carryInChange, request), HttpStatus.OK);
    }

    @ApiOperation(value = "반입정정 데이터 삭제", notes="반입정정 데이터를 삭제합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/carryInChange/delete", method = RequestMethod.DELETE)
    public ResponseEntity<CarryInChangeSaveResult> deleteCarryInChange(@PathVariable("entCode") String entCode, @RequestBody DeleteCarryInChange delete, HttpServletRequest request) throws Exception{
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
    	return new ResponseEntity<>(bmsInCorrectionService.deleteCarryInChange(delete, request),HttpStatus.OK);
    }

    @ApiOperation(value = "통보메시지", notes="통보 메시지를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/getNotificationMsg", method = RequestMethod.GET)
    public ResponseEntity<String> selectNotificationMsg(@PathVariable("entCode") String entCode, String customsReportNo, String changeCount, String lastPodStatus) throws Exception {
        return new ResponseEntity<String>(bmsInCorrectionService.selectNotificationMsg(customsReportNo, changeCount, lastPodStatus),HttpStatus.OK);
    }

    /** 관세청 XML 송신(Send) - GOVCBR5LC(반입신고정정신청서) */
	@ApiOperation(value = "관세청 XML 송신(Send)", notes = "GOVCBR5LC(반입신고정정신청서) 목록을 전송합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/exec/send/bmsCarryInChangeList", method = RequestMethod.POST)
	public ResponseEntity<CarryInChangeSaveResult> sendOutToXml(@PathVariable("entCode") String entCode, @RequestBody BatchBmsInCorrection params, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, params, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		
		params.setEntCode(entCode);
		return new ResponseEntity<CarryInChangeSaveResult>(bmsInCorrectionService.sendOutToXml(params, request), HttpStatus.OK);
	}

    @ApiOperation(value = "반입 엑셀 list 다운로드", notes="반입 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryIn/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectCarryInExcel(@PathVariable("entCode") String entCode, @RequestBody SearchInCorrection search, HttpServletRequest req) throws Exception {
    	search.setSrchEntCode(entCode);
        search.setPage(0);
        CarryInList carryInList  = bmsInCorrectionService.selectCarryInList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", carryInList.getCarryInList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map, "xlsx"), HttpStatus.OK);
    }

    @ApiOperation(value = "반입정정 엑셀 list 다운로드", notes="반입정정 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryInChange/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectCarryInChangeExcel(@PathVariable("entCode") String entCode, @RequestBody SearchInCorrection search, HttpServletRequest req) throws Exception {
    	search.setSrchEntCode(entCode);
        search.setPage(0);
        CarryInChangeList carryInChangeList  = bmsInCorrectionService.selectCarryInChangeList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", carryInChangeList.getCarryInChangeList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map, "xlsx"), HttpStatus.OK);
    }

}
