package org.yht.exps.express.controller.bms;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.bms.carryin.CarryInList;
import org.yht.exps.express.model.bms.xrayImageViewer.XrayImageDown;
import org.yht.exps.express.model.bms.xrayImageViewer.XrayImageList;
import org.yht.exps.express.model.bms.xrayImageViewer.search.SearchXrayImage;
import org.yht.exps.express.service.bms.XrayImageViewerService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags="xrayImageViewer")
@RequestMapping("/{entCode}/bms/xray")
public class XrayImageViewerController {

    @Resource(name="xrayImageViewerService")
    XrayImageViewerService xrayImageViewerService;
    
    @Autowired
	JwtTokenProvider jwtTokenProvider;

    @ApiOperation(value = "XRAY VIEWER - POS_TOP 리스트 조회", notes="XRAY VIEWER - POS_TOP 리스트 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/topImage/search/list", method = RequestMethod.GET)
    public ResponseEntity<XrayImageList> selectPosTopImageList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchXrayImage search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        Util.setDB(req, "XRAY");
        return new ResponseEntity<>(xrayImageViewerService.selectPosTopImageList(search), HttpStatus.OK); 
    }

    @ApiOperation(value = "XRAY VIEWER - POS_SIDE 리스트 조회", notes="XRAY VIEWER - POS_SIDE 리스트 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/sideImage/search/list", method = RequestMethod.GET)
    public ResponseEntity<XrayImageList> selectPosSideImageList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchXrayImage search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        Util.setDB(req, "XRAY");
        return new ResponseEntity<>(xrayImageViewerService.selectPosSideImageList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "XRAY VIEWER - 이미지 리스트 조회", notes="XRAY VIEWER - 이미지 리스트 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/imagePath/list", method = RequestMethod.GET)
    public ResponseEntity<XrayImageList> selectXrayImagePathList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchXrayImage search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        Util.setDB(req, "XRAY");
        return new ResponseEntity<>(xrayImageViewerService.selectXrayImagePathList(search), HttpStatus.OK);
    }

    @ApiOperation(value = "XRAY VIEWER - 화물정보조회 POPUP", notes="XRAY VIEWER - 화물정보를 POPUP 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/imageInfo/search", method = RequestMethod.GET)
    public ResponseEntity<CarryInList> selectXrayImageInfoList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchXrayImage search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        Util.setDB(req, "XRAY");
        return new ResponseEntity<>(xrayImageViewerService.selectXrayImageInfoList(search), HttpStatus.OK);
    }
    
    @ApiOperation(value = "XRAY VIEWER - 이미지 데이터 조회", notes="XRAY VIEWER - 이미지 데이터 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/get/imageData", method = RequestMethod.POST)
    public ResponseEntity<XrayImageList> selectXrayImageData(@PathVariable("entCode") String entCode, @RequestBody SearchXrayImage search, HttpServletRequest req) throws Exception {
    	Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
        search.setSrchEntCode(entCode);
        Util.setDB(req, "XRAY");
        return new ResponseEntity<>(xrayImageViewerService.selectXrayImageData(search), HttpStatus.OK);
    }
    
    @ApiOperation(value = "XRAY VIEWER - 이미지 데이터 다운로드", notes="XRAY VIEWER - 이미지 데이터 다운로드합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/download/allImageData", method = RequestMethod.GET)
    public ResponseEntity<List<XrayImageDown>> selectAllXrayImageData(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchXrayImage search, HttpServletRequest req) throws Exception {
        search.setSrchEntCode(entCode);
        Util.setDB(req, "XRAY");
        return new ResponseEntity<>(xrayImageViewerService.selectAllXrayImageData(search), HttpStatus.OK);
    }

}
