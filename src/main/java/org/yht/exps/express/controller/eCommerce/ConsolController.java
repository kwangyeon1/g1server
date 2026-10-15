package org.yht.exps.express.controller.eCommerce;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.eCommerce.consol.ConsolClose;
import org.yht.exps.express.model.eCommerce.consol.ConsolHouseOrderList;
import org.yht.exps.express.model.eCommerce.consol.ConsolMasterOrderList;
import org.yht.exps.express.model.eCommerce.consol.ConsolOrder;
import org.yht.exps.express.model.eCommerce.consol.ConsolRelease;
import org.yht.exps.express.model.eCommerce.consol.search.SearchConsolOrderList;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.eCommerce.ConsolService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/consol")
public class ConsolController {
	
	@Resource(name = "consolService")
	ConsolService consolService;
	
	@Resource(name = "fileService")
	FileService fileService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@ApiOperation(value = "전자상거래 Consolidation MasterOrder 조회", notes="MasterOrder 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/masterOrder/search/list", method = RequestMethod.GET)
	public ResponseEntity<ConsolMasterOrderList> selectMasterOrderList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchConsolOrderList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", "logUserName", null, null, true, false, false, false);
		return new ResponseEntity<ConsolMasterOrderList>(consolService.selectMasterOrderList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 Consolidation MasterOrder 조회", notes="MasterOrder 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/masterOrder/search/list/post", method = RequestMethod.POST)
	public ResponseEntity<ConsolMasterOrderList> selectMasterOrderListPost(@PathVariable("entCode") String entCode, @RequestBody SearchConsolOrderList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", "logUserName", null, null, true, false, false, false);
		return new ResponseEntity<ConsolMasterOrderList>(consolService.selectMasterOrderList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 Consolidation HouseOrder 조회", notes="HouseOrder 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/houseOrder/search/list", method = RequestMethod.GET)
	public ResponseEntity<ConsolHouseOrderList> selectHouseOrderList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchConsolOrderList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", "logUserName", null, null, true, false, false, false);
		return new ResponseEntity<ConsolHouseOrderList>(consolService.selectHouseOrderList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation 수입 Master 조회", notes="수입 Master 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/impMaster/search/list", method = RequestMethod.GET)
	public ResponseEntity<ConsolMasterOrderList> selectImpMasterList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchConsolOrderList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, null);
		return new ResponseEntity<ConsolMasterOrderList>(consolService.selectImpMasterList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation 수입 House 조회", notes="수입 House 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/impHouse/search/list", method = RequestMethod.GET)
	public ResponseEntity<ConsolHouseOrderList> selectImpHouseList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchConsolOrderList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, null);
		return new ResponseEntity<ConsolHouseOrderList>(consolService.selectImpHouseList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation 수입 House 콘솔건 조회", notes="수입 House 콘솔건 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/impHouseConsol/search/list", method = RequestMethod.GET)
	public ResponseEntity<ConsolHouseOrderList> selectImpHouseConsolList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchConsolOrderList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, null);
		return new ResponseEntity<ConsolHouseOrderList>(consolService.selectImpHouseConsolList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation 수출 Master 조회", notes="수출 Master 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/expMaster/search/list", method = RequestMethod.GET)
	public ResponseEntity<ConsolMasterOrderList> selectExpMasterList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchConsolOrderList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, null);
		return new ResponseEntity<ConsolMasterOrderList>(consolService.selectExpMasterList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation 수출 House 조회", notes="수출 House 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/expHouse/search/list", method = RequestMethod.GET)
	public ResponseEntity<ConsolHouseOrderList> selectExpHouseList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchConsolOrderList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, null);
		return new ResponseEntity<ConsolHouseOrderList>(consolService.selectExpHouseList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation 수출 House 콘솔건 조회", notes="수출 House 콘솔건 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/expHouseConsol/search/list", method = RequestMethod.GET)
	public ResponseEntity<ConsolHouseOrderList> selectExpHouseConsolList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchConsolOrderList search, HttpServletRequest request) throws Exception {
		search.setSrchEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, search, null, "logUserName", null, null);
		return new ResponseEntity<ConsolHouseOrderList>(consolService.selectExpHouseConsolList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation Consol", notes="MasterOrder에 HouseOrder를 콘솔합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/consol/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> consol(@PathVariable("entCode") String entCode, @RequestBody ConsolOrder consolOrder, HttpServletRequest request) throws Exception {
		consolOrder.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, consolOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(consolService.consol(consolOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation Consol", notes="MasterOrder에 HouseOrder를 콘솔합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/autoConsol/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> autoConsol(@PathVariable("entCode") String entCode, @RequestBody ConsolOrder consolOrder, HttpServletRequest request) throws Exception {
		consolOrder.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, consolOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(consolService.autoConsol(consolOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation Deconsol", notes="MasterOrder에 HouseOrder를 디콘솔합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/autoDeconsol/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> autoDeconsol(@PathVariable("entCode") String entCode, @RequestBody ConsolRelease consolRelease, HttpServletRequest request) throws Exception {
		consolRelease.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, consolRelease, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(consolService.autoDeconsol(consolRelease, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation Consol", notes="마감된 MasterOrder에 HouseOrder를 콘솔합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/closeMasterConsol/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> closeMasterConsol(@PathVariable("entCode") String entCode, @RequestBody ConsolOrder consolOrder, HttpServletRequest request) throws Exception {
		consolOrder.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, consolOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(consolService.closeMasterConsol(consolOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation Deconsol", notes="마감된 MasterOrder에 HouseOrder를 디콘솔합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/closeMasterDeconsol/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> closeMasterDeconsol(@PathVariable("entCode") String entCode, @RequestBody ConsolOrder consolOrder, HttpServletRequest request) throws Exception {
		consolOrder.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, consolOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(consolService.closeMasterDeconsol(consolOrder, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 MasterOrder save", notes="Master Order를 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertMasterOrder(@PathVariable("entCode") String entCode, @RequestBody ConsolOrder consolOrder, HttpServletRequest request) throws Exception {
		consolOrder.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, consolOrder, "updateUserId", "logUserName", null, "logUserNameEng");
		consolOrder.setRegisterUserId(consolOrder.getUpdateUserId());
		return new ResponseEntity<OrderSaveResult>(consolService.insertMasterOrder(consolOrder, request), HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "전자상거래 Consolidation ExcelUpload", notes="엑셀파일의 하우스번호리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload/search/list", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> excelUpload(@PathVariable("entCode") String entCode, @RequestParam("consolHouseExcel") MultipartFile consolHouseExcel) throws Exception {
		return new ResponseEntity<OrderSaveResult>(consolService.excelUpload(consolHouseExcel, entCode), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation Deconsol", notes="MasterOrder에서 HouseOrder를 콘솔해제합니다.(1건)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/deconsol/update/{odJobNo}", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> deconsol(@PathVariable("entCode") String entCode, @PathVariable("odJobNo") String odJobNo, @RequestBody ConsolRelease consolRelease, HttpServletRequest request) throws Exception {
		consolRelease.setEntCode(entCode);
		consolRelease.setOdJobNo(odJobNo);
		Util.getSessionUserInfo(request, jwtTokenProvider, consolRelease, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(consolService.deconsol(consolRelease, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation Deconsol", notes="MasterOrder에서 HouseOrder를 콘솔해제합니다.(복수)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "odJobNo", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/deconsol/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> deconsol(@PathVariable("entCode") String entCode, @RequestBody ConsolRelease consolRelease, HttpServletRequest request) throws Exception {
		consolRelease.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, consolRelease, null, "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(consolService.deconsol(consolRelease, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation 마감", notes="MasterOrder를 마감/해제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/close/update", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> close(@PathVariable("entCode") String entCode, @RequestBody ConsolClose consolClose, HttpServletRequest request) throws Exception {
		consolClose.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, consolClose, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(consolService.close(consolClose, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Consolidation 삭제", notes="MasterOrder를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> delete(@PathVariable("entCode") String entCode, @RequestBody ConsolClose consolClose, HttpServletRequest request) throws Exception {
		consolClose.setEntCode(entCode);
		Util.getSessionUserInfo(request, jwtTokenProvider, consolClose, "updateUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<OrderSaveResult>(consolService.delete(consolClose, request), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "전자상거래 Consolidation 삭제", notes="MasterOrder를 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/houseOrder/search/list/cargoManifest", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectCargoManifest(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchConsolOrderList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", "logUserName", null, null, true, false, false, false);
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", consolService.selectCargoManifest(search).getConsolHouseOrderList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Manifest", map), HttpStatus.OK);
	}
}
