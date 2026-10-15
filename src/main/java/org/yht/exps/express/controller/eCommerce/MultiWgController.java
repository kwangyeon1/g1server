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
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgOrder;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgOrderList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgSaveResult;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWg;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWgOrderList;
import org.yht.exps.express.service.eCommerce.MultiWgService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/multiWg")
public class MultiWgController {
	
	@Resource(name = "multiWgService")
	MultiWgService multiWgService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
		
	@ApiOperation(value = "전자상거래 멀티(중량)Order 조회", notes="멀티(중량)Order 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<MultiWgOrderList> selectMultiWgList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMultiWgOrderList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<MultiWgOrderList>(multiWgService.selectMultiWgList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 멀티(중량) Sub Order 조회", notes="멀티(중량) Sub Order 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/sub/list", method = RequestMethod.GET)
	public ResponseEntity<MultiWgOrderList> selectSubMultiWgList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchMultiWgOrderList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<MultiWgOrderList>(multiWgService.selectSubMultiWgList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 멀티(중량) Order 상세 조회", notes="전자상거래 멀티(중량) Order 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/{odJobNo}", method = RequestMethod.GET)
	public ResponseEntity<MultiWgOrder> selectOrderDetail(@PathVariable("entCode") String entCode, SearchMultiWg search) throws Exception {
		return new ResponseEntity<MultiWgOrder>(multiWgService.selectOrderDetail(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 멀티(중량) Order 삭제일괄처리", notes="전자상거래 멀티(중량) Order 일괄삭제처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/delete/seq/batch", method = RequestMethod.DELETE)
	public ResponseEntity<MultiWgSaveResult> deleteMultiWgList(@PathVariable("entCode") String entCode, @RequestBody MultiWgList multiWgList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, multiWgList, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<MultiWgSaveResult>(multiWgService.deleteMultiWgList(multiWgList, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 멀티(중량) List save", notes="전자상거래 멀티(중량) List 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/saveMultiWgList", method = RequestMethod.POST)
	public ResponseEntity<MultiWgSaveResult> saveMultiWgList(@PathVariable("entCode") String entCode, @RequestBody MultiWgList multiWgList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, multiWgList, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<MultiWgSaveResult>(multiWgService.saveMultiWgList(multiWgList, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 멀티(중량) List 인계확정 or 취소 일괄처리", notes="전자상거래 멀티(중량) List 인계확정 or 취소처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/update/batch", method = RequestMethod.PUT)
	public ResponseEntity<MultiWgSaveResult> updateMultiList(@PathVariable("entCode") String entCode, @RequestBody MultiWgList multiWgList, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, multiWgList, "registerUserId", "logUserName", null, "logUserNameEng");
		return new ResponseEntity<MultiWgSaveResult>(multiWgService.updateMultiList(multiWgList, request), HttpStatus.OK);
	}
	
	/**
	 * 엑셀 list
	 * @param entCode
	 * @param functionOrder
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "엑셀 list 조회", notes="엑셀 list 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})	
	@RequestMapping(value="/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectMultiWgListExcel(
						@PathVariable("entCode") String entCode,
						@QueryStringArgResolver SearchMultiWgOrderList search,
						HttpServletRequest request) throws Exception {
		
		search.setPage(0);
		MultiWgOrderList orderList = multiWgService.selectMultiWgList(search);
				
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", orderList.getMultiWgOrderList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("Multi_List", map), HttpStatus.OK);
	}
}
