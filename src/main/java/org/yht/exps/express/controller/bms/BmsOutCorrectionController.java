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
import org.yht.exps.express.model.bms.bmsOutCorrection.BatchBmsOutCorrection;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChange;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChangeDetail;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChangeDetailList;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChangeList;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChangeSaveResult;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutList;
import org.yht.exps.express.model.bms.bmsOutCorrection.DeleteCarryOutChange;
import org.yht.exps.express.model.bms.bmsOutCorrection.search.SearchOutCorrection;
import org.yht.exps.express.service.bms.BmsOutCorrectionService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Arrays;
import java.util.Map;

@RestController
@Api(tags="BmsOtcr")
@RequestMapping("/{entCode}/bms/otcr")
public class BmsOutCorrectionController {

    @Resource(name="bmsOutCorrectionService")
    BmsOutCorrectionService bmsOutCorrectionService;
    
    @Autowired
	JwtTokenProvider jwtTokenProvider;

    @ApiOperation(value = "반출정정신고 -  반츨 리스트 조회", notes="반출정정신고 -  반츨 리스트 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryOut/search/list", method = RequestMethod.GET)
    public ResponseEntity<CarryOutList> selectCarryOutList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOutCorrection search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsOutCorrectionService.selectCarryOutList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반출정정신고 - 반출정정 리스트 조회", notes = "반출정정신고 - 반출정정 리스트를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/carryOutChange/search/list")
    public ResponseEntity<CarryOutChangeList> selectCarryOutChangeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOutCorrection search, HttpServletRequest request) throws Exception{
        return new ResponseEntity<>(bmsOutCorrectionService.selectCarryOutChangeList(search),HttpStatus.OK);
    }

    @ApiOperation(value = "반출정정신고 상세 한건 조회", notes="반출정정신고 상세 한건 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/get", method = RequestMethod.GET)
    public ResponseEntity<CarryOutChangeDetail> selectCarryOutAndCarryOutChangeDetail(@PathVariable("entCode") String entCode, SearchOutCorrection search) throws Exception {
        return new ResponseEntity<>(bmsOutCorrectionService.selectCarryOutAndCarryOutChangeDetail(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반출정정신청 번호타입에 따라 조회", notes="반출정정신청 번호타입에 따라 조회  상세 화면 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/getCarryOutChangeBySrchNo", method = RequestMethod.GET)
    public ResponseEntity<CarryOutChangeDetailList> selectCarryOutChangeBySrchNo(@PathVariable("entCode") String entCode, SearchOutCorrection search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsOutCorrectionService.selectCarryOutChangeBySrchNo(search), HttpStatus.OK);
    }
    @ApiOperation(value = "반입정정신청  insert", notes="반입정정신청을 insert합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryOutChange/insert", method = RequestMethod.POST)
    public ResponseEntity<CarryOutChangeSaveResult> insertCarryOutChange(@PathVariable("entCode") String entCode, @RequestBody CarryOutChange carryOutChange, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryOutChange, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
    	return new ResponseEntity<CarryOutChangeSaveResult>(bmsOutCorrectionService.insertCarryOutChange(carryOutChange, request), HttpStatus.OK);
    }

    @ApiOperation(value = "반출정정신청 update", notes="반출정정신청 update합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
    })
    @RequestMapping(value="/carryOutChange/update", method = RequestMethod.PUT)
    public ResponseEntity<CarryOutChangeSaveResult> updateCarryOutChange(@PathVariable("entCode") String entCode, @RequestBody CarryOutChange carryOutChange, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryOutChange, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
    	return new ResponseEntity<CarryOutChangeSaveResult>(bmsOutCorrectionService.updateCarryOutChange(carryOutChange, request), HttpStatus.OK);
    }

    @ApiOperation(value = "반출정정 데이터 삭제", notes="반출정정 데이터를 삭제합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value = "/carryOutChange/delete", method = RequestMethod.DELETE)
    public ResponseEntity<CarryOutChangeSaveResult> deleteCarryOutChange(@PathVariable("entCode") String entCode, @RequestBody DeleteCarryOutChange delete, HttpServletRequest request) throws Exception{
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
    	return new ResponseEntity<>(bmsOutCorrectionService.deleteCarryOutChange(delete, request),HttpStatus.OK);
    }

    /** 관세청 XML 송신(Send) - GOVCBR5LD(반출신고정정신청서) */
	@ApiOperation(value = "관세청 XML 송신(Send)", notes = "GOVCBR5LD(반출신고정정신청서) 목록을 전송합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/exec/send/bmsCarryOutChangeList", method = RequestMethod.POST)
	public ResponseEntity<CarryOutChangeSaveResult> sendOutToXml(@PathVariable("entCode") String entCode, @RequestBody BatchBmsOutCorrection params, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, params, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		params.setEntCode(entCode);
		return new ResponseEntity<CarryOutChangeSaveResult>(bmsOutCorrectionService.sendOutToXml(params, request), HttpStatus.OK);
	}

    @ApiOperation(value = "반출 엑셀 list 다운로드", notes="반출 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryOut/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectCarryOutExcel(@PathVariable("entCode") String entCode, @RequestBody SearchOutCorrection search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        search.setPage(0);
        CarryOutList carryOutList  = bmsOutCorrectionService.selectCarryOutList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", carryOutList.getCarryOutList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map, "xlsx"), HttpStatus.OK);
    }

    @ApiOperation(value = "반출 엑셀 list 다운로드", notes="반출 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/carryOutChange/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectCarryOutChangeExcel(@PathVariable("entCode") String entCode, @RequestBody SearchOutCorrection search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        search.setPage(0);
        CarryOutChangeList carryOutChangeList  = bmsOutCorrectionService.selectCarryOutChangeList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", carryOutChangeList.getCarryOutChangeList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map, "xlsx"), HttpStatus.OK);
    }

}
