package org.yht.exps.express.controller.pda;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.bms.bmsCargo.BmsCargo;
import org.yht.exps.express.service.pda.PdaCargoService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/pda/pa/ct")
public class PdaCargoController {

	@Resource(name = "pdaCargoService")
	PdaCargoService pdaCargoService;

	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	/**
	 * 화물조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "화물 조회", notes="화물 상세 조회합니다.")
	@RequestMapping(value="/selectCargoTracking", method = RequestMethod.GET)
	public ResponseEntity<BmsCargo> selectCargoTracking(@QueryStringArgResolver Map<String,Object> search) throws Exception {
		return new ResponseEntity<BmsCargo>(pdaCargoService.selectCargoTracking(search), HttpStatus.OK);
	}

	/**
	 * 화물조회 - 박스 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "화물조회 - 화물 박스 조회", notes="화물조회 - 화물 박스 조회합니다.")
	@RequestMapping(value="/search/box", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> selectCargoTrackingBox(@QueryStringArgResolver Map<String, Object> search) throws Exception {
		return new ResponseEntity<List<Map<String, Object>>>(pdaCargoService.selectCargoTrackingBox(search), HttpStatus.OK);
	}

	/**
	 * 화물조회 - 박스 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "홀드 조회", notes="")
	@RequestMapping(value="/hold/get", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getHoldList(@QueryStringArgResolver Map<String, Object> search) throws Exception {
		return new ResponseEntity<Map<String, Object>>(pdaCargoService.getHoldList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "hold 완료처리", notes="hold 완료처리")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/hold/update", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> saveHold(@RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<Map<String, Object>>(pdaCargoService.updateHold(search, req), HttpStatus.OK);
	}
	
	/**
	 * 화물조회 - 출고 후 재입고
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "화물조회 - 출고 후 재입고", notes="화물조회 - 출고 후 재입고 생성합니다.")
	@RequestMapping(value="/insertCargoRestock", method = RequestMethod.POST)
	public void insertCargoRestock(@RequestBody Map<String, Object> map, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, req, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		pdaCargoService.insertCargoRestock(map, req);
	}
}