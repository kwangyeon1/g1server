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
import org.yht.exps.express.model.bms.bmsCargo.BmsMasterDest;
import org.yht.exps.express.service.pda.PdaCargoReceiveService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/pda/pa/in")
public class PdaCargoReceiveController {

	@Resource(name = "pdaCargoReceiveService")
	PdaCargoReceiveService pdaCargoReceiveService;

	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	/**
	 * 입항화물도착 MBL 조회
	 * @param search
	 * @return String
	 * @throws Exception
	 */
//	@ApiOperation(value = "입항화물도착", notes="입항화물도착 MBL 조회")
//	@RequestMapping(value="/init/selectMasterScanList", method = RequestMethod.GET)
//	public ResponseEntity<List<BmsMasterDest>> selectMasterScanList(@QueryStringArgResolver Map<String, Object> search) throws Exception {
//		return new ResponseEntity<List<BmsMasterDest>>(pdaCargoReceiveService.selectMasterScanList(search), HttpStatus.OK);
//	}

	/**
	 * 입항화물도착 MBL 조회
	 * @param search
	 * @return String
	 * @throws Exception
	 */
	@ApiOperation(value = "입항화물도착", notes="입항화물도착 MBL 조회")
	@RequestMapping(value="/search/selectExistMasterScan", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> selectExistMasterScan(@QueryStringArgResolver Map<String, Object> search) throws Exception {
		return new ResponseEntity<Map<String, Object>>(pdaCargoReceiveService.selectExistMasterScan(search), HttpStatus.OK);
	}

	/**
	 * 입항화물도착 Master Batch Update/Delete
	 * @param search
	 * @param req
	 * @return BmsMasterDest
	 * @throws Exception
	 */
	@ApiOperation(value = "입항화물도착", notes="입항화물도착 Master Batch Update/Delete")
	@RequestMapping(value="/controlBatchMasterDest", method = RequestMethod.POST)
	public ResponseEntity<List<BmsMasterDest>> controlBatchMasterDest(@RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<List<BmsMasterDest>>(pdaCargoReceiveService.controlBatchMasterDest(search), HttpStatus.OK);
	}
	
	/**
	 * 입항화물도착 Master Batch Update/Delete
	 * @param search
	 * @param req
	 * @return BmsMasterDest
	 * @throws Exception
	 */
	@ApiOperation(value = "입항화물도착", notes="웹입항화물도착 Master Batch Update/Delete")
	@RequestMapping(value="/controlBatchMasterDestWeb", method = RequestMethod.POST)
	public ResponseEntity<List<BmsMasterDest>> controlBatchMasterDestWeb(@RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<List<BmsMasterDest>>(pdaCargoReceiveService.controlBatchMasterDestWeb(search), HttpStatus.OK);
	}
}