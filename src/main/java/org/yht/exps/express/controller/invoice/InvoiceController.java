package org.yht.exps.express.controller.invoice;

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
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.invoice.BrokerChargeList;
import org.yht.exps.express.model.invoice.CourierChargeList;
import org.yht.exps.express.model.invoice.Invoice;
import org.yht.exps.express.model.invoice.PartnerDetailChargeList;
import org.yht.exps.express.model.invoice.PartnerSummaryChargeList;
import org.yht.exps.express.model.invoice.WarehouseChargeList;
import org.yht.exps.express.model.invoice.WorkFeeChargeList;
import org.yht.exps.express.model.invoice.WorkFeeRate;
import org.yht.exps.express.model.invoice.search.SearchBatchDeleteCost;
import org.yht.exps.express.model.invoice.search.SearchInvoiceList;
import org.yht.exps.express.service.invoice.InvoiceService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Invoice")
@RequestMapping("/{entCode}/ecm/invoice")
public class InvoiceController {
	
	@Resource(name = "invoiceService")
	InvoiceService invoiceService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	
	@ApiOperation(value = "전자상거래 정산관리 작업료 리스트 조회", notes="정산관리 작업료 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/workFeeCharge/search/list", method = RequestMethod.GET)
	public ResponseEntity<WorkFeeChargeList> selectWorkFeeChargeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<WorkFeeChargeList>(invoiceService.selectWorkFeeChargeList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 정산관리 창고료 엑셀파일생성", notes="전자상거래 정산관리 창고료 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/workFeeCharge/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectWorkFeeChargeListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", invoiceService.selectWorkFeeChargeList(search).getWorkFeeChargeList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("workFeeChargeList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 정산관리 창고료 리스트 조회", notes="정산관리 창고료 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/warehouseCharge/search/list", method = RequestMethod.GET)
	public ResponseEntity<WarehouseChargeList> selectWarehouseChargeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<WarehouseChargeList>(invoiceService.selectWarehouseChargeList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 정산관리 창고료 엑셀파일생성", notes="전자상거래 정산관리 창고료 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/warehouseCharge/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectWarehouseChargeListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", invoiceService.selectWarehouseChargeList(search).getWarehouseChargeList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("warehouseChargeList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 정산관리 택배사 리스트 조회", notes="정산관리 택배사 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/courierCharge/search/list", method = RequestMethod.GET)
	public ResponseEntity<CourierChargeList> selectCourierChargeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<CourierChargeList>(invoiceService.selectCourierChargeList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 정산관리 창고료 엑셀파일생성", notes="전자상거래 정산관리 창고료 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/courierCharge/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectCourierChargeListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", invoiceService.selectCourierChargeList(search).getCourierChargeList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("courierChargeList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 정산관리 관세사 리스트 조회", notes="정산관리 관세사 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/brokerCharge/search/list", method = RequestMethod.GET)
	public ResponseEntity<BrokerChargeList> selectBrokerChargeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<BrokerChargeList>(invoiceService.selectBrokerChargeList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 정산관리 창고료 엑셀파일생성", notes="전자상거래 정산관리 창고료 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/brokerCharge/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectBrokerChargeListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", invoiceService.selectBrokerChargeList(search).getBrokerChargeList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("brokerChargeList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 정산관리 파트너세부 리스트 조회", notes="정산관리 파트너세부 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/partnerDetailCharge/search/list", method = RequestMethod.GET)
	public ResponseEntity<PartnerDetailChargeList> selectPartnerDetailChargeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<PartnerDetailChargeList>(invoiceService.selectPartnerDetailChargeList(search), HttpStatus.OK);
	}
	@RequestMapping(value="/partnerDetailCharge/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectpartnerDetailChargeListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", invoiceService.selectPartnerDetailChargeList(search).getPartnerDetailChargeList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("partnerDetailCharge", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 정산관리 파트너집계 리스트 조회", notes="정산관리 파트너집계 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/partnerSummaryCharge/search/list", method = RequestMethod.GET)
	public ResponseEntity<PartnerSummaryChargeList> selectPartnerSummaryChargeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<PartnerSummaryChargeList>(invoiceService.selectPartnerSummaryChargeList(search), HttpStatus.OK);
	}
	@RequestMapping(value="/partnerSummaryCharge/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectpartnerSummaryChargeListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInvoiceList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", invoiceService.selectPartnerSummaryChargeList(search).getPartnerSummaryChargeList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("partnerSummaryCharge", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 추가비용 삭제", notes="추가비용을 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/cost/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteCost(@PathVariable("entCode") String entCode, @RequestBody Invoice invoice, HttpServletRequest request) throws Exception {
		invoice.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, invoice, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(invoiceService.deleteCost(invoice, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 추가비용 삭제", notes="추가비용을 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/cost/batchDelete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> batchDeleteCost(@PathVariable("entCode") String entCode, @RequestBody SearchBatchDeleteCost search, HttpServletRequest request) throws Exception {
		search.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(invoiceService.batchDeleteCost(search, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 정산배치처리 건수조회", notes="정산배치처리 건수조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/cost/batchDeleteCount", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> batchDeleteCostCount(@PathVariable("entCode") String entCode, @RequestBody SearchBatchDeleteCost search, HttpServletRequest request) throws Exception {
		search.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(invoiceService.batchDeleteCostCount(search, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 정산서 생성", notes="정산서를 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertInvoice(@PathVariable("entCode") String entCode, @RequestBody Invoice invoice, HttpServletRequest request) throws Exception {
		invoice.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, invoice, "registerUserId", "logUserName", null, "logUserNameEng");
		if("PTN".equals(invoice.getInvoiceType())){
			return new ResponseEntity<OrderSaveResult>(invoiceService.insertInvoicePartner(invoice, request), HttpStatus.OK);
		}else{
			return new ResponseEntity<OrderSaveResult>(invoiceService.insertInvoice(invoice, request), HttpStatus.OK);
		}
	}
	
	@ApiOperation(value = "전자상거래 작업료 요율계산", notes="작업료 요율계산을 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/workFee/rateUpdate", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> workFeeRateUpdate(@PathVariable("entCode") String entCode, @RequestBody WorkFeeRate rate, HttpServletRequest request) throws Exception {
		rate.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, rate, "userId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(invoiceService.workFeeRateUpdate(rate, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 작업료마감", notes="작업료를 마감합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/workFee/closeUpdate", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateWorkFeeClose(@PathVariable("entCode") String entCode, @RequestBody Invoice invoice, HttpServletRequest request) throws Exception {
		invoice.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, invoice, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(invoiceService.updateWorkFeeClose(invoice, request), HttpStatus.OK);
	}
}
