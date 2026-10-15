package org.yht.exps.express.controller.bms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.bms.bmsBcrNoCargo.BmsBcrNoCargoList;
import org.yht.exps.express.model.bms.bmsBcrNoCargo.search.SearchBmsBcrNoCargoList;
import org.yht.exps.express.service.bms.BmsBcrNoCargoService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "bms")
@RequestMapping("/{entCode}/bms/bmsBcrNoCargo")
public class BmsBcrNoCargoController {

	@Resource(name = "bmsBcrNoCargoService")
	BmsBcrNoCargoService bmsBcrNoCargoService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@ApiOperation(value = "BCR 무적 조회", notes="BCR 무적 리스트를 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<BmsBcrNoCargoList> selectBmsBcrNoCargoList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsBcrNoCargoList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);

		String profile = System.getProperty("spring.profiles.active");

		/* Check BCR No Cargo Date */
		String maxScanDateTime = bmsBcrNoCargoService.selectMaxScanDateTime();
		if( ! "local".equals(profile) ) Util.setDB(request, "XRAY");

		Util.getSessionUserInfoBMS(request, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");

		/* Get Transfer Data BCR Cargo */
		List<Map<String, Object>> bcrTransferList = bmsBcrNoCargoService.selectBcrCargoTransferList(maxScanDateTime, entCode);

		Util.setDB(request, search.getSrchEntCode());
		return new ResponseEntity<BmsBcrNoCargoList>(bmsBcrNoCargoService.selectBcrNoCargoList(search, bcrTransferList, request), HttpStatus.OK);
	}

	@ApiOperation(value = "일괄체크완료 팝업 update", notes="일괄체크완료 팝업 update합니다.")
	@RequestMapping(value="/update", method = RequestMethod.PUT)
	public void updateBmsBcrNoCargo(@RequestBody Map<String, Object> map, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfoBMS(request, jwtTokenProvider, map, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		bmsBcrNoCargoService.updateBcrNoCargo(map);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "일괄체크완료 리스트 엑셀 다운로드", notes="일괄체크완료 리스트를 엑셀로 다운로드합니다.")
	@RequestMapping(value="/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectBmsBcrNoCargoExcelList(@RequestBody SearchBmsBcrNoCargoList search, HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", bmsBcrNoCargoService.selectBcrNoCargoList(search, new ArrayList<Map<String, Object>>(), request).getBmsBcrNoCargoList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList(search.getExcelTitle(), map, "xlsx"), HttpStatus.OK);
	}
}
