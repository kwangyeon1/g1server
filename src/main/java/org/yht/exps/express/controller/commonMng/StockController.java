package org.yht.exps.express.controller.commonMng;


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
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.commonMng.stock.DeleteStock;
import org.yht.exps.express.model.commonMng.stock.InsertStock;
import org.yht.exps.express.model.commonMng.stock.RegrantStock;
import org.yht.exps.express.model.commonMng.stock.Stock;
import org.yht.exps.express.model.commonMng.stock.StockList;
import org.yht.exps.express.model.commonMng.stock.StockSaveResult;
import org.yht.exps.express.model.commonMng.stock.VoidStockList;
import org.yht.exps.express.model.commonMng.stock.search.SearchStockDetail;
import org.yht.exps.express.model.commonMng.stock.search.SearchStockList;
import org.yht.exps.express.model.commonMng.stock.search.SearchVoidStockList;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.commonMng.StockService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Stock")
@RequestMapping("/{entCode}/ecm/commonMng/stock")
public class StockController {

	@Resource(name = "stockService")
	StockService stockService;
	@Resource(name = "commonService")
	CommonService commonService;
	
	@Autowired
	CustomProperties customProperties;
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	

	@ApiOperation(value = "전자상거래 Stock List 조회", notes="Stock 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<StockList> selectStockList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchStockList search) throws Exception {
		return new ResponseEntity<StockList>(stockService.selectStockList(search), HttpStatus.OK);
	}		
	
	 
	@ApiOperation(value = "전자상거래 Stock 등록", notes="Stock 데이터 등록.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<StockSaveResult> insertStock(@PathVariable("entCode") String entCode, @RequestBody InsertStock insert, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, insert, "inputUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<StockSaveResult>(stockService.insertStock(insert, request), HttpStatus.OK);
	}	
	
	@ApiOperation(value = "전자상거래 Stock 사용여부변경", notes="전자상거래 Stock 사용여부를 변경합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/updateUseYn", method = RequestMethod.PUT)
	public ResponseEntity<StockSaveResult> updateUseYn(@PathVariable("entCode") String entCode, @RequestBody InsertStock stock, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, stock, "inputUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<StockSaveResult>(stockService.updateUseYn(stock, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "StockNo 생성후 엑셀다운로드", notes="StockNo 생성후 엑셀다운로드합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/stockNoExcel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> stockNoExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver InsertStock stock, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, stock, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<DownloadFileInfo>((DownloadFileInfo)new ObjectMapper().convertValue(stockService.stockNoExcel(stock), DownloadFileInfo.class), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Stock 삭제", notes="Order를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete", method = RequestMethod.DELETE)
	public ResponseEntity<StockSaveResult> deleteStock(@PathVariable("entCode") String entCode, @RequestBody DeleteStock delete, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, delete, "inputUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<StockSaveResult>(stockService.deleteStock(delete, request), HttpStatus.OK);
	}		
	

	@ApiOperation(value = "전자상거래 Stock 데이터 디테일", notes="Stock 디테일을 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/{id}", method = RequestMethod.POST)
	public ResponseEntity<Stock> selectStockDetail(@PathVariable("entCode") String entCode, @RequestBody SearchStockDetail search, @PathVariable("id") String id) throws Exception {
		return new ResponseEntity<Stock>(stockService.selectStockDetail(search), HttpStatus.OK);
	}	
	
	
	@ApiOperation(value = "전자상거래 Void Stock List 조회", notes="Void Stock 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/item/search/list", method = RequestMethod.GET)
	public ResponseEntity<VoidStockList> selectVoidStockList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchVoidStockList search) throws Exception {
		return new ResponseEntity<VoidStockList>(stockService.selectVoidStockList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Stock 재부여", notes="Stock 데이터 재부여.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/{id}", method = RequestMethod.POST)
	public ResponseEntity<StockSaveResult> regrantStock(@PathVariable("entCode") String entCode, @RequestBody RegrantStock regrant, @PathVariable("id") String id, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, regrant, "inputUserId", "logUserName", null, "logUserNameEng");
		regrant.setUpdateUserId(regrant.getInputUserId());
		return new ResponseEntity<StockSaveResult>(stockService.regrantStock(regrant, request), HttpStatus.OK);
	}	
	
	@ApiOperation(value = "전자상거래 Stock 중복 체크", notes="Stock 중복 체크.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/check/{id}", method = RequestMethod.POST)
	public ResponseEntity<Integer> checkStockDuplication(@PathVariable("entCode") String entCode, @RequestBody RegrantStock regrant, @PathVariable("id") String id, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, regrant, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<Integer>(stockService.checkStockDuplication(regrant), HttpStatus.OK);
	}		
	
	
	
	
	
	@ApiOperation(value = "전자상거래 Stock List 조회", notes="Stock 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/v2", method = RequestMethod.GET)
	public ResponseEntity<StockList> selectStockListV2(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchStockList search) throws Exception {
		return new ResponseEntity<StockList>(stockService.selectStockListV2(search), HttpStatus.OK);
	}
}

