package org.yht.exps.express.controller.bms;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.service.bms.BmsCargoService;
import org.yht.jwt.JwtTokenProvider;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/{entCode}/bms/cargo")
public class BmsCargoController {

	@Resource(name = "bmsCargoService")
	BmsCargoService bmsCargoService;

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
	public ResponseEntity<Map<String,Object>> selectCargoTracking(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String,Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<>(bmsCargoService.selectCargoTracking(search), HttpStatus.OK);
	}
	
	/**
	 * 박스조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "박스 조회", notes="박스 조회합니다.")
	@RequestMapping(value="/pkgList", method = RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> pkgList(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String,Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<>(bmsCargoService.pkgList(search), HttpStatus.OK);
	}
	
	/**
	 * 반출입 진행단계 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "반출입 진행단계  조회", notes="반출입 진행단계 조회합니다.")
	@RequestMapping(value="/cargoStatusList", method = RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> cargoStatusList(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String,Object> search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<>(bmsCargoService.cargoStatusList(search), HttpStatus.OK);
	}

}