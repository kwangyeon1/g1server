package org.yht.exps.express.controller.eCommerce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import org.yht.exps.express.mapper.eCommerce.CoioMapper;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.eCommerce.coio.CoioDetList;
import org.yht.exps.express.model.eCommerce.coio.CoioLocList;
import org.yht.exps.express.model.eCommerce.coio.CoioLocSubList;
import org.yht.exps.express.model.eCommerce.coio.CoioSumList;
import org.yht.exps.express.model.eCommerce.coio.search.SearchCoioList;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.eCommerce.CoioService;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "ECommerce")
@RequestMapping("/{entCode}/ecm/eCommerce/coio")
public class CoioController {
	
	@Resource(name = "coioService")
	CoioService coioService;
	@Resource(name = "commonService")
	CommonService commonService;	
	
	@Autowired
	CoioMapper coioMapper;
	
	@ApiOperation(value = "반입/반출(Detail) List 조회", notes="반입/반출(Detail) 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/detail/list", method = RequestMethod.GET)
	public ResponseEntity<CoioDetList> selectCoioDetailList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCoioList search) throws Exception {
		return new ResponseEntity<CoioDetList>(coioService.selectCoioDetailList(search), HttpStatus.OK);
	}		
	@ApiOperation(value = "반입/반출(Detail) List 조회 엑셀파일생성", notes="반입/반출(Detail) List 조회 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/detail/list/excel", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectCoioDetailListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchCoioList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}
		
		search.setSrchMode("listCount");
		search.setPage(1);

		Util.getCryptographyYn(search);
		
		return new ResponseEntity<DownloadFileInfo>(commonService.makeExcelDownload(map, search, coioMapper, "selectCoioDetailList", "org.yht.exps.express.model.eCommerce.coio.search.SearchCoioList", false), HttpStatus.OK);
		
	}
	
	
	@ApiOperation(value = "반입/반출(location) Summary List 조회", notes="반입/반출(location) Summary 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/summury/list", method = RequestMethod.GET)
	public ResponseEntity<CoioSumList> selectCoioSummaryList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCoioList search) throws Exception {
		return new ResponseEntity<CoioSumList>(coioService.selectCoioSummaryList(search), HttpStatus.OK);
	}	
	@ApiOperation(value = "반입/반출(Detail) List Summary 조회 엑셀파일생성", notes="반입/반출(Detail) Summary List 조회 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/summary/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectCoioSummaryListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCoioList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", coioService.selectCoioSummaryList(search).getCoioSumList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("OrderCusList", map), HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "반입/반출(location) List 조회", notes="반입/반출(location) 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/location/list", method = RequestMethod.GET)
	public ResponseEntity<CoioLocList> selectCoioLocationList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCoioList search) throws Exception {
		return new ResponseEntity<CoioLocList>(coioService.selectCoioLocationList(search), HttpStatus.OK);
	}		
	
	@ApiOperation(value = "반입/반출(location) Sub List 조회", notes="반입/반출(location) Sub 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/item/location/list", method = RequestMethod.GET)
	public ResponseEntity<CoioLocSubList> selectCoioLocationSubList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCoioList search) throws Exception {
		return new ResponseEntity<CoioLocSubList>(coioService.selectCoioLocationSubList(search), HttpStatus.OK);
	}	
	@ApiOperation(value = "반입/반출(location) Loc Sub List 조회 엑셀파일생성", notes="반입/반출(location) Loc Sub 리스트 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/item/location/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectCoioLocationSubListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCoioList search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		map.put("data", coioService.selectCoioLocationSubList(search).getCoioLocSubList());
		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("OrderCusList", map), HttpStatus.OK);
	}
	
	
}
