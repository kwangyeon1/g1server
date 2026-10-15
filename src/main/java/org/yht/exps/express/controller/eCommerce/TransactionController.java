package org.yht.exps.express.controller.eCommerce;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.eCommerce.transaction.TransactionOdList;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.eCommerce.transaction.TransactionSaveResult;
import org.yht.exps.express.model.eCommerce.transaction.search.SearchTransactionList;
import org.yht.exps.express.service.eCommerce.TransactionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/trans")
public class TransactionController {

	@Resource(name = "transactionService")
	TransactionService transactionService;

	@Value("${spring.profiles.active}")
	private String profile;

	@ApiOperation(value = "거래정보 Order 조회", notes="거래정보 Order 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/order/search/list", method = RequestMethod.GET)
	public ResponseEntity<TransactionOdList> selectTransOdList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchTransactionList search, HttpServletRequest req) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<TransactionOdList>(transactionService.selectTransOdList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "거래정보 상세 리스트 조회", notes="거래정보 상세 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/detail/search/list", method = RequestMethod.GET)
	public ResponseEntity<TransactionOdList> selectTransDetailList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchTransactionList search, HttpServletRequest req) throws Exception {
		return new ResponseEntity<TransactionOdList>(transactionService.selectTransDetailList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "거래정보 삭제", notes="거래정보를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete/transInfo", method = RequestMethod.DELETE)
	public ResponseEntity<TransactionSaveResult> deleteTransactionInfo(@PathVariable("entCode") String entCode, @RequestBody SearchTransactionList search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<TransactionSaveResult>(transactionService.deleteTransactionInfo(search, request), HttpStatus.OK);
	}

	@ApiOperation(value = "거래정보 전송", notes="거래정보를 전송합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/send/transInfo", method = RequestMethod.POST)
	public ResponseEntity<TransactionSaveResult> sendTransInfo(@PathVariable("entCode") String entCode, @RequestBody SearchTransactionList search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<TransactionSaveResult>(transactionService.sendTransInfo(search), HttpStatus.OK);
	}

	@ApiOperation(value = "거래정보 개인통관부호 검증", notes="거래정보 개인통관부호 검증합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/check/customsId", method = RequestMethod.POST)
	public ResponseEntity<TransactionSaveResult> checkTransCustomsId(@PathVariable("entCode") String entCode, @RequestBody SearchTransactionList search, HttpServletRequest request) throws Exception {
		return new ResponseEntity<TransactionSaveResult>(transactionService.checkTransCustomsId(search), HttpStatus.OK);
	}

	@ApiOperation(value = "거래정보 리스트 엑셀파일생성", notes="거래정보 리스트 엑셀파일생성")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectTransOdListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchTransactionList search, HttpServletRequest req) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", transactionService.selectTransOdListExcel(search));
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("eCommerce_gov", map), HttpStatus.OK);
	}
}
