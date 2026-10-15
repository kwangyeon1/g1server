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
import org.yht.exps.express.model.bms.bmsAbnormal.BatchBmsAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalDetailList;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalList;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalSaveResult;
import org.yht.exps.express.model.bms.bmsAbnormal.DeleteCarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormal.search.SearchCarryInAbnormal;
import org.yht.exps.express.model.bms.carryin.InPlanList;
import org.yht.exps.express.service.bms.BmsAbnormalService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Arrays;
import java.util.Map;

@RestController
@Api(tags="BmsInab")
@RequestMapping("/{entCode}/bms/inab")
public class BmsAbnormalController {

    @Resource(name = "bmsAbnormalService")
    BmsAbnormalService bmsAbnormalService;
    
    @Autowired
	JwtTokenProvider jwtTokenProvider;

    @ApiOperation(value = "반입물품이상신고 -  반입예정 리스트 조회", notes="반입물품이상신고 - 반입예정 리스트 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/inPlan/search/list", method = RequestMethod.GET)
    public ResponseEntity<InPlanList> selectInPlanList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCarryInAbnormal search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsAbnormalService.selectInPlanList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입물품이상신고 - 반입이상 리스트 조회", notes="반입물품이상신고 - 반입이상 리스트 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormal/search/list", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormalList> selectAbnormalList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCarryInAbnormal search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsAbnormalService.selectAbnormalList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입물품이상신고 삭제", notes="반입물품이상신고 삭제합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormal/delete", method = RequestMethod.DELETE)
    public ResponseEntity<CarryInAbnormalSaveResult> deleteAbnormal(@PathVariable("entCode") String entCode, @RequestBody DeleteCarryInAbnormal delete, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, delete, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
        return new ResponseEntity<>(bmsAbnormalService.deleteAbnormal(delete, request), HttpStatus.OK);
    }

    @ApiOperation(value = "반입이상에 존재하는 데이터인지 check", notes="반입이상에 존재하는 데이터인지 check")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/checkExistAbnormal", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormalDetailList> checkExistAbnormal(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCarryInAbnormal search) throws Exception {
        return new ResponseEntity<>(bmsAbnormalService.checkExistAbnormal(search),HttpStatus.OK);
    }

    @ApiOperation(value = "반입이상 상세 한건 조회", notes="반입이상 상세 한건을 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormal/search/detailAbnormal", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormal> selectDetailAbnormal(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCarryInAbnormal search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsAbnormalService.selectDetailAbnormal(search), HttpStatus.OK);
    }

    @ApiOperation(value = "HOUSE 정보(HBL 리스트)를 번호타입에 따라 조회", notes="HOUSE 정보(HBL 리스트)를 번호타입에 따라 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/getAbnormalListPopup", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormalDetailList> selectAbnormalListPopup(@PathVariable("entCode") String entCode, SearchCarryInAbnormal search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsAbnormalService.selectAbnormalListPopup(search), HttpStatus.OK);
    }

    @ApiOperation(value = "HOUSE 정보(HBL 리스트)를 번호타입에 따라 조회", notes="HOUSE 정보(HBL 리스트)를 번호타입에 따라 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/getAbnormalHblList", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormalDetailList> selectAbnormalHblList(@PathVariable("entCode") String entCode, SearchCarryInAbnormal search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsAbnormalService.selectAbnormalHblList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "그리드에 등록되지 않은 BL번호 조회", notes="그리드에 등록되지 않은 BL번호 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/getAbnormalHblNotInList", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormalDetailList> selectAbnormalHblNotInList(@PathVariable("entCode") String entCode, SearchCarryInAbnormal search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsAbnormalService.selectAbnormalHblNotInList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "HOUSE 정보(HBL 리스트)를 MBL에 따라 조회", notes="HOUSE 정보(HBL 리스트)를 MBL에 따라 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/getAbnormalHblListByMBL", method = RequestMethod.GET)
    public ResponseEntity<CarryInAbnormalDetailList> selectAbnormalHblListByMBL(@PathVariable("entCode") String entCode, SearchCarryInAbnormal search) throws Exception {
        search.setSrchEntCode(entCode);
        return new ResponseEntity<>(bmsAbnormalService.selectAbnormalHblListByMBL(search), HttpStatus.OK);
    }

    @ApiOperation(value = "반입물품이상신고  insert", notes="반입물품이상신고를 insert합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormal/insert", method = RequestMethod.POST)
    public ResponseEntity<CarryInAbnormalSaveResult> insertAbnormal(@PathVariable("entCode") String entCode, @RequestBody CarryInAbnormal carryInAbnormal, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryInAbnormal, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
    	return new ResponseEntity<CarryInAbnormalSaveResult>(bmsAbnormalService.insertAbnormal(carryInAbnormal, request), HttpStatus.OK);
    }

    @ApiOperation(value = "반입물품이상신고 update", notes="반입물품이상신고를 update합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
    })
    @RequestMapping(value="/abnormal/update", method = RequestMethod.PUT)
    public ResponseEntity<CarryInAbnormalSaveResult> updateAbnormal(@PathVariable("entCode") String entCode, @RequestBody CarryInAbnormal carryInAbnormal, HttpServletRequest request) throws Exception {
    	Util.getSessionUserInfoBMS(request, jwtTokenProvider, carryInAbnormal, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
        return new ResponseEntity<CarryInAbnormalSaveResult>(bmsAbnormalService.updateAbnormal(carryInAbnormal, request), HttpStatus.OK);
    }

	/** 관세청 XML 송신(Send) - GOVCBRDKO(특송화물 반입물품 이상보고서) */
	@ApiOperation(value = "관세청 XML 송신(Send)", notes = "GOVCBRDKO(특송화물 반입물품 이상보고서) 목록을 전송합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/exec/send/carryInAbnormalList", method = RequestMethod.POST)
	public ResponseEntity<CarryInAbnormalSaveResult> sendOutToXml(@PathVariable("entCode") String entCode, @RequestBody BatchBmsAbnormal params, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, params, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		params.setEntCode(entCode);
		return new ResponseEntity<CarryInAbnormalSaveResult>(bmsAbnormalService.sendOutToXml(params, request), HttpStatus.OK);
	}

    @ApiOperation(value = "반입예정 엑셀 list 다운로드", notes="반입예정 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/inPlan/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectInPlanExcel(@PathVariable("entCode") String entCode, @RequestBody SearchCarryInAbnormal search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        search.setPage(0);
        InPlanList inPlanList  = bmsAbnormalService.selectInPlanList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", inPlanList.getInPlanList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }
    @ApiOperation(value = "반입이상 엑셀 list 다운로드", notes="반입이상 엑셀 list 반환합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/abnormal/search/excel", method = RequestMethod.POST)
    public ResponseEntity<DownloadFileInfo> selectAbnormalExcel(@PathVariable("entCode") String entCode, @RequestBody SearchCarryInAbnormal search, HttpServletRequest req) throws Exception {
    	search.setSrchEntCode(entCode);
        search.setPage(0);
        CarryInAbnormalList carryInAbnormalList  = bmsAbnormalService.selectAbnormalList(search);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
        map.put("data", carryInAbnormalList.getCarryInAbnormalList());
        return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getDocumentName(), map,"xlsx"), HttpStatus.OK);
    }

}
