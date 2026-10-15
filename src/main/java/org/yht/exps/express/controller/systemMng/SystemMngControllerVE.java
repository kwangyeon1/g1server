package org.yht.exps.express.controller.systemMng;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.systemMng.HubNoticeList;
import org.yht.exps.express.model.systemMng.QnADeleteVE;
import org.yht.exps.express.model.systemMng.QnAListVE;
import org.yht.exps.express.model.systemMng.QnASaveResultVE;
import org.yht.exps.express.model.systemMng.QnAVE;
import org.yht.exps.express.model.systemMng.SubQnAListVE;
import org.yht.exps.express.model.systemMng.search.SearchHubMainNoticeList;
import org.yht.exps.express.model.systemMng.search.SearchQnADetailVE;
import org.yht.exps.express.model.systemMng.search.SearchQnAListVE;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.systemMng.HubSystemMngService;
import org.yht.exps.express.service.systemMng.SystemMngServiceVE;
import org.yht.util.FileDown;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "SystemMngVE")
@RequestMapping("/{entCode}/sms/systemMng/ve")
public class SystemMngControllerVE {
	@Autowired
	CustomProperties customProperties;
	
	@Resource(name = "systemMngServiceVE")
	SystemMngServiceVE systemMngServiceVE;
	
	@Resource(name = "fileService")
	FileService fileService;
	
	@Resource(name = "hubSystemMngService")
	HubSystemMngService hubSystemMngService;
	
	@ApiOperation(value = "문의사항(Q&A) 리스트 조회", notes="문의사항(Q&A) 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/QnA/search/list", method = RequestMethod.GET)
	public ResponseEntity<QnAListVE> selectQnAList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchQnAListVE search) throws Exception {
		return new ResponseEntity<QnAListVE>(systemMngServiceVE.selectQnAList(search), HttpStatus.OK);
	}

	@ApiOperation(value = "문의사항(Q&A) 리스트 조회", notes="문의사항(Q&A) 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/subQnA/search/list", method = RequestMethod.GET)
	public ResponseEntity<SubQnAListVE> selectSubQnAList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchQnAListVE search) throws Exception {
		return new ResponseEntity<SubQnAListVE>(systemMngServiceVE.selectSubQnAList(search), HttpStatus.OK);
	}
	
	/**
	 * Q&A > Entry > 파일 다운로드
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/QnA/fileDownload", method = RequestMethod.POST)
	public void selectSmsComg032FileDown(@RequestBody Map<String, Object> QnAMap, HttpServletResponse response) throws Exception {
		
		String downloadDir 		= customProperties.getFileQnADir();	// 파일 생성 기본 폴더 : 도입사/사업장/QnA/;
		String fileTempName = Util.null2str(QnAMap.get("fileTempName"));
		String fileOriginalName = Util.null2str(QnAMap.get("fileOriginalName")); 
		
		File file = new File(downloadDir + fileTempName);
		System.out.println("path : " + downloadDir + fileTempName);
		System.out.println("존재: " + file.exists());
		System.out.println("읽기 가능: " + file.canRead());
		System.out.println("크기: " + file.length());
		
		if(file.isFile()){
			System.out.println("===============================================");
			FileDown.down(response, file, fileOriginalName);
		} else {
			response.setContentType("application/x-json;  charset=UTF-8");
	        response.getWriter().print("");
		}
	}
	
	/**
	 * 문의사항(Q&A) Save
	 * @param entCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "문의사항(Q&A) save", notes="문의사항(Q&A) 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/QnA/save", method = RequestMethod.POST)
	public QnASaveResultVE saveQnA(@PathVariable("entCode") String entCode,HttpServletRequest request,HttpServletResponse response) throws Exception {
		QnAVE qna = new ObjectMapper().readValue(request.getParameter("params"), QnAVE.class);
		// PathVariable로 받은 값 세팅
	    qna.setEntCode(entCode);
	    
	    QnASaveResultVE result = systemMngServiceVE.saveQnA(qna, request);

	    return result;	
	  }
	
	/**
	 * 문의사항(Q&A) subGrid Save
	 * @param entCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "문의사항(Q&A) save", notes="문의사항(Q&A) 서브그리드 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/subQnA/save", method = RequestMethod.POST)
	public QnASaveResultVE saveSubQnA(@PathVariable("entCode") String entCode,HttpServletRequest request,HttpServletResponse response) throws Exception {
		QnAVE qna = new ObjectMapper().readValue(request.getParameter("params"), QnAVE.class);
		// PathVariable로 받은 값 세팅
		qna.setEntCode(entCode);
		
		QnASaveResultVE result = systemMngServiceVE.saveSubQnA(qna, request);
		
		return result;	
	}
	
	/**
	 * 문의사항(Q&A) - 삭제
	 * @param entCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "문의사항(Q&A) 삭제처리", notes="문의사항(Q&A) 삭제처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/QnA/delete", method = RequestMethod.DELETE)
	public ResponseEntity<QnASaveResultVE> deleteQnA(@PathVariable("entCode") String entCode, @RequestBody QnADeleteVE qnADelete, HttpServletRequest request) throws Exception {
		qnADelete.setEntCode(entCode);		
		return new ResponseEntity<QnASaveResultVE>(systemMngServiceVE.deleteQnA(qnADelete, request), HttpStatus.OK);
	}
	
	/**
	 * 문의사항(Q&A) - subGrid 삭제
	 * @param entCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "문의사항(Q&A) 삭제처리", notes="문의사항(Q&A) 삭제처리합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/subQnA/delete", method = RequestMethod.DELETE)
	public ResponseEntity<QnASaveResultVE> deleteSubQnA(@PathVariable("entCode") String entCode, @RequestBody QnADeleteVE qnADelete, HttpServletRequest request) throws Exception {
		qnADelete.setEntCode(entCode);		
		return new ResponseEntity<QnASaveResultVE>(systemMngServiceVE.deleteSubQnA(qnADelete, request), HttpStatus.OK);
	}
	
	/**
	 * 문의사항(Q&A) - 상세조회
	 * @param entCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "문의사항(Q&A) 상세 조회", notes="문의사항(Q&A) 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/QnA/get/{referenceNo}", method = RequestMethod.GET)
	public ResponseEntity<QnAVE> selectQnADetail(@PathVariable("entCode") String entCode, SearchQnADetailVE search) throws Exception {
		return new ResponseEntity<QnAVE>(systemMngServiceVE.selectQnADetail(search), HttpStatus.OK);
	}
	
	/**
	 * 문의사항(Q&A) subGrid - 상세조회
	 * @param entCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "문의사항(Q&A) 상세 조회", notes="문의사항(Q&A) 상세 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/subQnA/get/{referenceNo}", method = RequestMethod.GET)
	public ResponseEntity<QnAVE> selectSubQnADetail(@PathVariable("entCode") String entCode, SearchQnADetailVE search) throws Exception {
		return new ResponseEntity<QnAVE>(systemMngServiceVE.selectSubQnADetail(search), HttpStatus.OK);
	}

	@ApiOperation(value = "문의사항(Q&A) 엑셀파일생성", notes="문의사항(Q&A) 엑셀파일을 생성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/QnA/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> selectHubListExcel(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchQnAListVE search,HttpServletRequest request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class);
		String[] columnsArr = {"num","customerName","questionTypeName","completeYnh","poNo","hblNo","title","contents","registerUserName","registerDateTime","answerDateTime"};
		String[] columnsWidthArr = {"60","120","100","100","130","130","130","130","130","150","150"};
		Integer[] numFormatsArr = {0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,};
		String[] excelTypesArr = {"center","center","center","center","center","center","center","center","center","center","center"};
		String[] titlesArr = {"번호","BUSINESS TYPE","문의유형","진행상태","ORDER No","H.B/L No","제목","내용","작성자","작성일자","답변일자"};

		List<String> columns = new ArrayList<String>();
		List<String> columnsWidth = new ArrayList<String>();
		List<Integer> numFormats = new ArrayList<Integer>();
		List<String> excelTypes = new ArrayList<String>();
		List<String> titles = new ArrayList<String>();
		for(int i = 0; i < columnsArr.length; i++){
			columns.add(columnsArr[i]);
			columnsWidth.add(columnsWidthArr[i]);
			numFormats.add(numFormatsArr[i]);
			excelTypes.add(excelTypesArr[i]);
			titles.add(titlesArr[i]);
		}
		map.put("columns", columns);
		map.put("columnsWidth", columnsWidth);
		map.put("numFormats", numFormats);
		map.put("excelTypes", excelTypes);
		map.put("titles", titles);
		
		map.put("data", systemMngServiceVE.selectQnAList(search).getQnAList());
 		return new ResponseEntity<DownloadFileInfo>(Common.getJsonForExcelList("qnAList", map), HttpStatus.OK);
	}
	
	@ApiOperation(value = "문의사항(Q&A) 이력 리스트 조회", notes="문의사항(Q&A) 이력 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/QnA/search/history/list", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String,Object>>> selectQnAHistoryList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchQnAListVE search) throws Exception {
		return new ResponseEntity<List<Map<String,Object>>>(systemMngServiceVE.selectQnAHistoryList(search), HttpStatus.OK);
	}
	
	
	/**
	 * eHub 메인 공지사항 리스트 조회
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "eHub메인 공지사항 리스트 조회", notes="eHub 메인 공지사항 리스트 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/mainNotice/search/list", method = RequestMethod.GET)
	public ResponseEntity<HubNoticeList> selectOmsMainNoticeList(@PathVariable("entCode") String entCode, SearchHubMainNoticeList search) throws Exception {
		return new ResponseEntity<HubNoticeList>(hubSystemMngService.selectHubMainNoticeList(search), HttpStatus.OK);
	}
}
