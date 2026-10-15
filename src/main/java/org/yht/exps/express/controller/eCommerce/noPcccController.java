package org.yht.exps.express.controller.eCommerce;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.exps.express.model.eCommerce.pccc.PodDetail;
import org.yht.exps.express.model.eCommerce.pccc.search.SearchPod;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.eCommerce.PcccService;
import org.yht.exps.express.service.sms.SmsLogsService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/nosess/pccc")
public class noPcccController {

	@Resource(name = "pcccService")
	PcccService pcccService;
	
	@Resource(name = "smsLogsService")
	SmsLogsService smsLogsService;
	
	@Resource(name = "fileService")
	FileService fileService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "전자상거래 POD상태(팝업) 조회", notes="POD상태(팝업) 조회를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/pod/detail", method = RequestMethod.GET)
	public ResponseEntity<PodDetail> selectPodDetail(@PathVariable("entCode") String entCode,  SearchPod search) throws Exception {
		search.setSrchEntCode(entCode);
		search.setNoSessPodYn("Y");
		
		Util.accessControlDatabase(search, "srchNo", false, null, null, null, null);  
		
		return new ResponseEntity<PodDetail>(pcccService.selectPodDetail(search), HttpStatus.OK);
	}
}
