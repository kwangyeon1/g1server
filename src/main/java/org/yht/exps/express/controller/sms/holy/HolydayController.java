package org.yht.exps.express.controller.sms.holy;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.mapper.sms.holy.HolydayMapper;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.sms.holy.HolydayList;
import org.yht.exps.express.model.sms.holy.HolydayResult;
import org.yht.exps.express.model.sms.holy.search.SearchHolydayList;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.sms.holy.HolydayService;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Holyday")
@RequestMapping("/{entCode}/sms/holyday")
public class HolydayController {
	@Resource(name = "holydayService")
	HolydayService holydayService;
	
	@Resource(name = "commonService")
	CommonService commonService;
	
	@Autowired
	HolydayMapper holydayMapper;
	
	@ApiOperation(value = "공휴일관리 리스트 조회", notes="공휴일관리 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list", method = RequestMethod.GET)
	public ResponseEntity<HolydayList> selectHolydayList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchHolydayList search) throws Exception {
		return new ResponseEntity<HolydayList>(holydayService.selectHolydayList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Order insert", notes="전자상거래 Order 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/insert", method = RequestMethod.POST)
	public ResponseEntity<HolydayResult> insertOrder(HttpServletRequest request, @PathVariable("entCode") String entCode, @RequestBody HolydayList holydayList) throws Exception {
		return new ResponseEntity<HolydayResult>(holydayService.saveHolyday(request, holydayList), HttpStatus.OK);
	}
	
	@ApiOperation(value = "공휴일관리 리스트 조회", notes="공휴일관리 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/excelPost", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> selectHolydayListExcel(@PathVariable("entCode") String entCode, @RequestBody SearchHolydayList search,HttpServletRequest request) throws Exception {
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

		Util.getCryptographyYn(search);
		
		search.setSrchMode("listCount");
		search.setPage(1);
		return new ResponseEntity<DownloadFileInfo>(commonService.makeExcelDownload(map, search, holydayMapper, "selectHolyDayList", "org.yht.exps.express.model.sms.holy.search.SearchHolydayList", false), HttpStatus.OK);
	}
	
	@ApiOperation(value = "공휴일 정보 ExcelUpload", notes="공휴일 정보 ExcelUpload 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/excelUpload/search/list", method = RequestMethod.POST)
	public ResponseEntity<HolydayResult> excelUpload(@PathVariable("entCode") String entCode, @RequestParam("holydayExcel") MultipartFile holydayExcel) throws Exception {
		return new ResponseEntity<HolydayResult>(holydayService.excelUpload(holydayExcel, entCode), HttpStatus.OK);
	}
}
