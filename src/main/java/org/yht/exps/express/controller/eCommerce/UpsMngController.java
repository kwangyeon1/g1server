package org.yht.exps.express.controller.eCommerce;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.eCommerce.UpsMngService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@Api(tags = "UpsGroup")
@RequestMapping("/{entCode}/ecm/eCommerce/ups")
public class UpsMngController {
	
	@Resource(name = "upsMngService")
	UpsMngService upsMngService;
	@Value("${spring.profiles.active}")
	private String profile;

	@Resource(name = "fileService")
	FileService fileService;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@ApiOperation(value = "전자상거래 UPS그룹코드 등록 엑셀 업로드", notes="전자상거래 UPS그룹코드 엑셀 업로드 저장합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/upsGroup/insert/Excel/{userId}/{gmtTime}", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertUpsGroupCodeExcel(HttpServletRequest request
			, @PathVariable("entCode") String entCode
			, @PathVariable("userId") String userId
			, @PathVariable("gmtTime") String gmtTime) throws Exception {

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
		MultipartFile file = multipartRequest.getFile("excelUpload_file");
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(file, new String[]{"xlsx", "xls"})) {
			OrderSaveResult orderSaveResult = new OrderSaveResult();
			orderSaveResult.setResultCode("extError");
			return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
		}

		userId = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"), userId);

		FileUploadResponse result = fileService.storeFile(file, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")){
			return new ResponseEntity<OrderSaveResult>(upsMngService.insertUpsGroupCodeExcel(entCode, userId, gmtTime, fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile()), HttpStatus.OK);
		}else {
			return new ResponseEntity<OrderSaveResult>(upsMngService.insertUpsGroupCodeExcel(entCode, userId, gmtTime,  null), HttpStatus.OK);
		}
	}

}
