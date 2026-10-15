package org.yht.exps.express.controller.bms;

import java.util.Arrays;
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
import org.yht.exps.express.model.bms.bmsPkgNo.BmsPkgNoList;
import org.yht.exps.express.model.bms.bmsPkgNo.search.SearchBmsPkgNoList;
import org.yht.exps.express.model.bms.carryin.BmsInspectResultRst;
import org.yht.exps.express.service.bms.BmsPkgNoService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "bms")
@RequestMapping("/{entCode}/bms/pkgNo")
public class BmsPkgNoController {

	@Resource(name = "bmsPkgNoService")
	BmsPkgNoService bmsPkgNoService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	/**
	 * 반입신고 반입예정 박스번호조회 리스트
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "박스번호조회 리스트", notes="박스번호조회 리스트를 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/get/list", method = RequestMethod.GET)
	public ResponseEntity<BmsPkgNoList> selectBmsPkgNoList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPkgNoList search) throws Exception {
		search.setSrchEntCode(entCode); 
		return new ResponseEntity<BmsPkgNoList>(bmsPkgNoService.selectBmsPkgNoList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "박스번호 저장", notes="박스번호 저장")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<BmsPkgNoList> updateBmsInspectResultPicking(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsPkgNoList>(bmsPkgNoService.insert(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "박스번호 저장", notes="박스번호 저장")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/blSplitPkgInsert", method = RequestMethod.POST)
	public ResponseEntity<BmsPkgNoList> blSplitPkgInsert(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsPkgNoList>(bmsPkgNoService.blSplitPkgInsert(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "분할된박스 신규화물관리번호 저장", notes="박스번호 저장")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/saveNewCargoNo", method = RequestMethod.POST)
	public ResponseEntity<BmsPkgNoList> saveNewCargoNo(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsPkgNoList>(bmsPkgNoService.saveNewCargoNo(search, req), HttpStatus.OK);
	}
	
	@ApiOperation(value = "박스번호 변경", notes="박스번호 변경")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update", method = RequestMethod.POST)
	public ResponseEntity<BmsPkgNoList> updateBmsInPkgPackingNo(@PathVariable("entCode") String entCode,  @RequestBody Map<String, Object> search, HttpServletRequest req) throws Exception {
		Util.getSessionUserInfoBMS(req, jwtTokenProvider, search, Arrays.asList("registerUserId", "updateUserId","inputUserId"), "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BmsPkgNoList>(bmsPkgNoService.update(search, req), HttpStatus.OK);
	}

}
