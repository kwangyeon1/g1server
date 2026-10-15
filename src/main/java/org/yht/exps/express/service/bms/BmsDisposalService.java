package org.yht.exps.express.service.bms;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsDisposalMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.disposal.*;
import org.yht.exps.express.model.bms.disposal.search.SearchDisposal;
import org.yht.exps.express.model.eCommerce.order.OrderHold;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.Filters;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class BmsDisposalService {

	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	@Autowired
	OrderMapper orderMapper;
	
	@Autowired
	CommonMapper commonMapper;

	@Autowired
	BmsDisposalMapper bmsDisposalMapper;
	@Autowired
	BmsCommService bmsCommService;
	
	@Autowired
	CommonService commonService;

	@Resource(name = "fileService")
	FileService fileService;

	@Autowired
	CustomProperties customProperties;

/*	@Autowired
	RestHighLevelClient client;*/
	/**
	 * 재고 조회 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public InventoryInList selectInventoryInList(SearchDisposal search) throws Exception {
		InventoryInList inventoryInList = new InventoryInList();
		setSearchCondition(search);
		// BMS_IN에만 CUSTOMS_REPORT_NO 가 있다.
		if("CUSTOMS_REPORT_NO".equals(search.getSrchNoType())) {
			search.setSrchNoType("A.CUSTOMS_REPORT_NO");
		}
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

		List<InventoryIn> list ;
 		// excel Down 여부
		if(null == search.getExcelDownYn()){
			Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "|CARRYIN_DATE|DISPOSAL_ISSUE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|A.MBL_NO");
			list  = bmsDisposalMapper.selectInventoryInList(search);
		}else {
			Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYIN_DATE|DISPOSAL_ISSUE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|A.MBL_NO");
			list  = bmsDisposalMapper.selectInventoryInListExcel(search);
		}


		inventoryInList.setInventoryInList(list);
		if(list.size() > 0) {
			inventoryInList.setTotal(list.get(0).getTotal());
		}else {
			inventoryInList.setTotal(0);
		}
		return inventoryInList;
	}
	/**
	 * 폐기 조회 List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public DisposalList selectDisposalList(SearchDisposal search) throws Exception {
		DisposalList disposalList = new DisposalList();
		setSearchCondition(search);
		//Server 에서 반입 신고 번호 Alias 변경 처리
		if("CUSTOMS_REPORT_NO".equals(search.getSrchNoType())) {
			search.setSrchNoType("B.CUSTOMS_REPORT_NO");
		}
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
		List<Disposal> list;
		// excel Down 여부
		if(null == search.getExcelDownYn()){
			Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "|CARRYIN_DATE|DISPOSAL_ISSUE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO|A.MBL_NO");
			list  = bmsDisposalMapper.selectDisposalList(search);
		}else {
			Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYIN_DATE|DISPOSAL_ISSUE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO|A.MBL_NO");
			list  = bmsDisposalMapper.selectDisposalListExcel(search);
		}
		disposalList.setDisposalList(list);
		if(list.size() > 0) {
			disposalList.setTotal(list.get(0).getTotal());
		}else {
			disposalList.setTotal(0);
		}
		return disposalList;
	}

	private static void setSearchCondition(SearchDisposal search) throws Exception {
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){
			// 가상 필드 다른 이름으로 매핑 처리 cargoReferenceNoDisp,
			List<Sort> lst = search.getSort();
			if( lst != null) {
				for (Sort sort : lst) { // 가상필드 다른 이름으로 처리
					if (sort.getField().equals("cargoReferenceNoDisp")) {
						sort.setField("cargoReferenceNo");
					}
				}
			}
			Util.camelCaseToUnderscore(search);

			Filter lstFilter = search.getFilter();
			if( lstFilter != null) {
				for (Filters filt : lstFilter.getFilters()) { // 가상필드 다른 이름으로 처리
					if (filt.getField().equals("CARGO_REFERENCE_NO_DISP")) {
						filt.setField("CARGO_REFERENCE_NO");
					}
				}
			}
		}
	}

	/**
	 * 상세 화면 조회 리스트 조회
	 * @param search
	 * @return
	 */
	public DisposalDetail selectDisposalInventoryDetail(SearchDisposal search){
		DisposalDetail detail = new DisposalDetail();
		Disposal disposal = null;
		Disposal dispoCustomer = null;
		InventoryIn inventoryIn = null;
		if(BmsConsts.BMS_CONST_DISPOSAL.equals(search.getSrchCarryInState())) {  //DISPOSAL, STOCK

			 disposal   = bmsDisposalMapper.selectDisposalDetail(search);
			search.setSrchInNo(disposal.getInNo()); // 상세 정보 조회
			inventoryIn =  bmsDisposalMapper.selectInventoryDetail(search);
		}else {
			disposal    = bmsDisposalMapper.selectDisposalDetail(search);
			inventoryIn =  bmsDisposalMapper.selectInventoryDetail(search);
			dispoCustomer = bmsDisposalMapper.selectRecentDisposalData(search.getSrchEntCode());

			detail.setDispoCustomer(dispoCustomer);
		}
 		detail.setDisposal(disposal);
		detail.setInventoryIn(inventoryIn);

		detail.setSrchCarryInState(search.getSrchCarryInState());
		detail.setSrchDisposalNo(search.getSrchDisposalNo());
		detail.setSrchInNo(search.getSrchInNo());
		return detail;
	}

	/***
	 * srchBlNo로 조회시에 보이는 값 리스트
	 * @param search
	 * @return
	 */
	public DisposalDetailList selectDisposalByBlNo(SearchDisposal search) throws Exception {
		DisposalDetailList lst = new DisposalDetailList();

		search.setPageSize(50);
		search.setPage(1);
		search.setDefaultSort(" IN_NO DESC ");
		setSearchCondition(search);
		  // search에
		String originSrchNoType =search.getSrchNoType();
		getSearchCondiDisposal( search , BmsConsts.BMS_CONST_DISPOSAL ,  originSrchNoType);
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "|CARRYIN_DATE|DISPOSAL_ISSUE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO|A.MBL_NO");
		List<Disposal> dispLst  = bmsDisposalMapper.selectDisposalList(search);
		lst.setDisposalList(dispLst);
		if(dispLst.size() > 0) {
			lst.setTotal(dispLst.get(0).getTotal());
		}else {
			lst.setTotal(0);
		}
		getSearchCondiDisposal( search , BmsConsts.BMS_CONST_STOCK ,  originSrchNoType);
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "|CARRYIN_DATE|DISPOSAL_ISSUE_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|A.MBL_NO");
		List<InventoryIn> invenLst = bmsDisposalMapper.selectInventoryInList(search);
		if(invenLst.size() > 0) {
			lst.setTotalIn(invenLst.get(0).getTotal());
		}else {
			lst.setTotalIn(0);
		}
		lst.setInventoryInList(invenLst);
		return lst;
	}

	/**
	 * 특송창고 disposal - insert/update
	 * @param disposal
	 * @return
	 * @throws Exception
	 */
	public DisposalSaveResult insertDisposal(Disposal disposal, HttpServletRequest request) throws Exception {
		DisposalSaveResult disposalSaveResult = new DisposalSaveResult();
		Util.accessControlDatabase(disposal, "dateFormat", false, null, null, null, null);
		int intRow = bmsDisposalMapper.insertDisposal(disposal);

		// 폐기 신청 Y로 함
		updateBmsInDisposalYn_Y(disposal);

		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(disposal.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(disposal.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(disposal.getRegisterUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setDbType(BmsConsts.DbType.BMS_DISPOSAL);
		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
		processLogs.setDescription("DISPOSAL [입력 NO:"+String.valueOf(disposal.getDisposalNo())+"]");
		processLogs.setJobNo(String.valueOf(disposal.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(disposal));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(disposal.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(disposal.getLogUserName());
		processLogs.setProcessUserNameEng(disposal.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(disposal.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.INSERT /* pod Status 값 매핑 */
				, ""
				, disposal
		);

	/*	에러 리턴 값
	if(disposalSaveResult.get("duplicateBlNo") != null){	*//** 비엘번호중복 *//*
			disposalSaveResult.setResultCode("duplicateBlNo");
		}else if(disposalSaveResult.get("closeOrder") != null){	*//** 마감된오더 *//*
			disposalSaveResult.setResultCode("A11237");	*//** 마감된 오더는 수정 할 수 없습니다. *//*
		}else if(disposalSaveResult.get("resultCode") != null){
			disposalSaveResult.setResultCode(Util.null2str(orderSaveMap.get("resultCode")));
		}*/
		disposalSaveResult.setResultCode("A012051");// 저장 되었습니다.
		return disposalSaveResult;
	}



	public DisposalSaveResult updateDisposal(Disposal disposal, HttpServletRequest request) throws Exception {
		DisposalSaveResult disposalSaveResult = new DisposalSaveResult();
		int intRow = bmsDisposalMapper.updateDisposal(disposal);


		// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(disposal.getEntCode()));
		processLogs.setEntBusinessCode(Util.null2str(disposal.getEntBusinessCode()));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(disposal.getRegisterUserId()));
		processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_DISPOSAL);
		//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
		processLogs.setDescription("DISPOSAL [수정 NO:"+String.valueOf(disposal.getDisposalNo())+"]");
		processLogs.setJobNo(String.valueOf(disposal.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setJobLog(new Gson().toJson(disposal));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(disposal.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(disposal.getLogUserName());
		processLogs.setProcessUserNameEng(disposal.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
		String blNo = String.valueOf(disposal.getBlNo());
		// 호출시 필요 항목 : BL_NO, POD_STATUS
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 수정이나 입력 값이 변경이 안됨 */
		  	, ""
				, disposal
		);
		disposalSaveResult.setResultCode("A012051"); // 저장 되었습니다.
		return disposalSaveResult;
	}

	/***
	 * 폐기처리 삭제 처리
	 * 반입 처리후 이력 남김
	 * @param delete
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public DisposalSaveResult deleteDisposal(DeleteDisposal delete, HttpServletRequest request) throws Exception {
		DisposalSaveResult result = new DisposalSaveResult();

		 Disposal disp ;
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		
		List<Disposal> list = bmsDisposalMapper.getAlreadyOutFromDisposal(deleteList);
		if(list.size()>0) {
			result.setResultCode("ABMS000007");
			return result;
		}
		
		int resultCnt =0 ;
		ProcessLogs processLogs;
		for (Map<String, Object> deleteMap : deleteList) {
			resultCnt ++;
			disp = new Disposal();
			disp.setDisposalNo(Integer.parseInt(Util.null2str(deleteMap.get("disposalNo"))));

			log.info("delete disp:{}", disp);
			bmsDisposalMapper.deleteDisposal(disp);
			// UPDATE BMS_IN.DISPOSAL_YN ='N' ( 폐기 신청시에는 Y 로 넣는다.
			updateBmsInDisposalYn_N(delete, deleteMap);
			//
			//userAuth가 AD인 경우 사용건수 있더라도 수정이 가능하도록 수정됨/이때 시스템로그 남기도록 추가
			//elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
			processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(delete.getEntCode()));
			processLogs.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
			processLogs.setLogJobType("DATA");
			processLogs.setBound("AIR");
			processLogs.setBoundType("IMP");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
			processLogs.setDbType(BmsConsts.DbType.BMS_DISPOSAL);
			//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
			processLogs.setDescription("DISPOSAL [삭제 NO:"+String.valueOf(deleteMap.get("disposalNo"))+", 삭제사유:"+ delete.getReason()+"]");
 			processLogs.setJobNo(String.valueOf(deleteMap.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
 			processLogs.setJobLog(new Gson().toJson(deleteMap));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(delete.getLogUserName());
			processLogs.setProcessUserNameEng(delete.getLogUserNameEng());
		//	String podStatus = BmsConsts.PodStatus.BMS99;
			String blNo = String.valueOf(deleteMap.get("blNo"));
			// 호출시 필요 항목 : BL_NO, POD_STATUS
			bmsCommService.insertLogAndPodStatus(processLogs, request
					 , BmsConsts.PodStatus.DELETE /* pod Status 값 매핑 */
					 ,  delete.getReason() /* 삭제 사유 */
					 , null
			);

		}
		result.setResultCnt(resultCnt);
		return result;
	}

	private void updateBmsInDisposalYn_N(DeleteDisposal delete, Map<String, Object> map) {
		InventoryIn in = new InventoryIn();
		in.setInNo( (Integer) map.get("inNo") );
		in.setDisposalYn("N"); // 폐기에서 => 재고로 변경
		in.setUpdateUserId(delete.getInputUserId());
		in.setGmtTime(delete.getGmtTime());
		in.setDateFormat(delete.getDateFormat());
		bmsDisposalMapper.updateBmsInDisposalYn(in);
	}

	private void updateBmsInDisposalYn_Y(Disposal disposal) {
		InventoryIn in = new InventoryIn();
		in.setInNo( disposal.getInNo());
		in.setDisposalYn("Y"); // 재고에서 폐기로 변경
		in.setUpdateUserId(disposal.getUpdateUserId());
		in.setGmtTime(disposal.getGmtTime());
		in.setDateFormat(disposal.getDateFormat());
		bmsDisposalMapper.updateBmsInDisposalYn(in);
	}

	private void fib(int b){
		if(b==0) return;
		fib(b-1);
		fib(b-2);
		log.debug(Integer.toString(b));
	}

	/**
	 * 폐기화물 엑셀업로드(엑셀업로드 체크)
	 */
	public DisposalSaveResult checkExcelUpload(MultipartFile uploadExcel, String entCode, String excelUploadType) throws Exception {
		DisposalSaveResult disposalSaveResult = new DisposalSaveResult();
		Map<String, Object> resultData = new HashMap<>();

		FileUploadResponse result = fileService.storeFile(uploadExcel, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")) {
			File file = fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile();
			String[] fieldName = null;

			if(excelUploadType.equals("bmsDisposal")) {
				fieldName = new String[] {
						null, "cargoReferenceNo", null, "blNo", null,
						null, null, null, null, "disposalReason",
						"disposalCt", "pkgType", "disposalWt", null, null,
						null, null, null, "carryinCompanyName"
				};
			}

			List<Map<String, Object>> dataList = new ArrayList<>();
			try(InputStream is = new FileInputStream(file); Workbook wb = WorkbookFactory.create(is)) {
				Sheet sheet = wb.getSheetAt(0);
				DataFormatter df = new DataFormatter();
				int fieldSize = fieldName.length;

				for(int i = 1; i < sheet.getLastRowNum() + 1; i++) {
					Row row = sheet.getRow(i);
					if(row == null) {
						continue;
					}

					Map<String, Object> dataMap = new HashMap<>();
					for(int j = 0; j < fieldSize; j++) {
						if(fieldName[j] == null) {
							continue;
						}
						Cell cell = row.getCell(j);
						String fieldValue = df.formatCellValue(cell);
						if(!Util.null2str(fieldValue).equals("")) {
							dataMap.put(fieldName[j], fieldValue);
						}
					}
					dataList.add(dataMap);
				}

				resultData.put("entCode", entCode);
				resultData.put("excelUploadType", excelUploadType);
				resultData.put("dataList", dataList);
				resultData.put("dataSize", dataList.size());

				disposalSaveResult.setResultData(resultData);
				disposalSaveResult.setResultCode("200");
			}catch(Exception e) {
				disposalSaveResult.setResultCode("A00063");
				return disposalSaveResult;
			}

			String backupPath = customProperties.getBackupDir() + "excel/" + entCode + "/" + DateTime.getDate("yyyy-MM");
			File dir = new File(backupPath);
			if(dir.exists() == false) {
				dir.mkdirs();
			}
			String backupFilePath = backupPath + "/" + file.getName();
			file.renameTo(new File(backupFilePath));
		}else {
			disposalSaveResult.setResultCode("ERROR");
		}

		return disposalSaveResult;
	}

	public DisposalSaveResult saveExcelUpload(ExcelUploadDisposal excelUploadDisposal, HttpServletRequest request, String excelUploadType) throws Exception {
		DisposalSaveResult result = new DisposalSaveResult();
		List<String> duplicateBlList = new ArrayList<>();
		Integer resultCnt = 0;

		if(excelUploadDisposal.getDataList() == null) {
			result.setReturnList(duplicateBlList);
			result.setResultCnt(resultCnt);
			return result;
		}

		List<Map<String, Object>> insertDisposalMapList = new ArrayList<>();
		
		Disposal recentDisposal = bmsDisposalMapper.selectRecentDisposalData(excelUploadDisposal.getEntCode());
		for(Map<String, Object> excelDisposal : excelUploadDisposal.getDataList()) {
			String cargoReferenceNo = Util.null2str(excelDisposal.get("cargoReferenceNo")).replaceAll("-", "").toUpperCase();
			Integer count = bmsDisposalMapper.selectDisposalExcelUploadCheckCount(cargoReferenceNo);

			if(count > 0) {
				duplicateBlList.add(Util.null2str(excelDisposal.get("blNo")));
			}else {
				InventoryIn in = bmsDisposalMapper.selectInventoryForDisposalExcelUpload(cargoReferenceNo);
				if(in == null) {
					duplicateBlList.add(0, cargoReferenceNo + "(반입없음)");
					continue;
				}
				if (in.getStockWt() == null || in.getStockWt() <= 0) {
					duplicateBlList.add(0, cargoReferenceNo + "(재고없음)");
					continue;
				}
				if(!"N".equals(in.getDisposalYn()) ) {
					duplicateBlList.add(0, cargoReferenceNo + "(폐기있음)");
					continue;
				}
				excelDisposal.put("inNo", in.getInNo());
				excelDisposal.put("odJobNo", in.getOdJobNo());
				excelDisposal.put("cargoReferenceNo", cargoReferenceNo);
				insertDisposalMapList.add(excelDisposal);
				resultCnt++;
			}
		}
		
		int batchCnt = 0;
		SqlSession sqlSession    = null;

		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			BmsDisposalMapper batchBmsDisposalMapper = sqlSession.getMapper(BmsDisposalMapper.class);
			
			for(Map<String, Object> excelDisposal : insertDisposalMapList) {
				String cargoReferenceNo = Util.null2str(excelDisposal.get("cargoReferenceNo"));
				
				Disposal disposal = new Disposal();
				disposal.setCargoReferenceNo(cargoReferenceNo);
				disposal.setBlNo(Util.null2str(excelDisposal.get("blNo")));
				disposal.setDisposalReason(Util.null2str(excelDisposal.get("disposalReason")));
				disposal.setDisposalCode(recentDisposal.getDisposalCode());
				disposal.setDisposalName(recentDisposal.getDisposalName());
				disposal.setCustomerName(Util.null2str(excelDisposal.get("carryinCompanyName")));
				disposal.setDisposalLoc(recentDisposal.getDisposalLoc());
				disposal.setDisposalExpDate(recentDisposal.getDisposalExpDate());
				disposal.setDisposalMethod(recentDisposal.getDisposalMethod());
				disposal.setPkgType(Util.null2str(excelDisposal.get("pkgType")));
				disposal.setDisposalCt(Util.null2int(excelDisposal.get("disposalCt")));
				disposal.setDisposalWt(Util.null2dbl(excelDisposal.get("disposalWt")));
				disposal.setGmtTime(excelUploadDisposal.getGmtTime());
				disposal.setDateFormat(excelUploadDisposal.getDateFormat());
				disposal.setRegisterUserId(excelUploadDisposal.getRegisterUserId());
				disposal.setUpdateUserId(excelUploadDisposal.getRegisterUserId());
				disposal.setInNo(Util.null2int(excelDisposal.get("inNo")));
				/* pod용 */
				disposal.setEntCode(Util.null2str(excelUploadDisposal.getEntCode()));
				disposal.setOdJobNo(Util.null2str(excelDisposal.get("odJobNo")));
				disposal.setPodName("폐기저장(업로드)");
				disposal.setPodRemarks("");
				Util.accessControlDatabase(disposal, "dateFormat", false, null, null, null, null);
				batchBmsDisposalMapper.insertDisposalBatchGenWithExcelData(disposal);
	
				
				InventoryIn in = new InventoryIn();
				in.setInNo(Util.null2int(excelDisposal.get("inNo")));
				in.setDisposalYn("Y");
				in.setUpdateUserId(excelUploadDisposal.getRegisterUserId());
				in.setGmtTime(excelUploadDisposal.getGmtTime());
				in.setDateFormat(excelUploadDisposal.getDateFormat());
				batchBmsDisposalMapper.updateBmsInDisposalYn(in);
				
				batchCnt++;
				if(batchCnt % 100 == 0) {
	                sqlSession.flushStatements();
	                sqlSession.clearCache();
	            }
			}
			
			sqlSession.flushStatements();
	        sqlSession.clearCache();
	        sqlSession.commit();

	    } catch (Exception e) {
	        if (sqlSession != null) {
	            sqlSession.rollback();
	        }
	        throw e;

	    } finally {
	        if (sqlSession != null) {
	            sqlSession.close();
	        }
	    }
		
		for(Map<String, Object> excelDisposal : insertDisposalMapList) {
			ProcessLogs processLogs = new ProcessLogs();
			// 처리 로그 기록
			processLogs.setEntCode(Util.null2str(excelUploadDisposal.getEntCode()));
			processLogs.setEntBusinessCode(Util.null2str(excelUploadDisposal.getEntBusinessCode()));
	        processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
	        processLogs.setBound("AIR");
	        processLogs.setBoundType("IMP");
	        processLogs.setProcessUserId(Util.null2str(excelUploadDisposal.getRegisterUserId()));
	        processLogs.setJobNo(Util.null2str(excelDisposal.get("odJobNo")));
	        processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
			processLogs.setDbType(BmsConsts.DbType.BMS_DISPOSAL);
			processLogs.setDescription(BmsConsts.DbType.BMS_DISPOSAL + " [입력 CARGO_REFERENCE_NO:" + Util.null2str(excelDisposal.get("cargoReferenceNo")) + "]");
	        processLogs.setJobLog(new Gson().toJson(excelDisposal));
	        processLogs.setResult("ok");
	        processLogs.setEntBusinessPrintName(excelUploadDisposal.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(excelUploadDisposal.getLogUserName());
			processLogs.setProcessUserNameEng(excelUploadDisposal.getLogUserNameEng());
	        commonService.insertLog(processLogs, request);
		}

		result.setReturnList(duplicateBlList);
		result.setResultCnt(resultCnt);
		return result;
	}

	/**
	 * 1) 반입 -> 폐기일괄 생성 : 폐기화물일괄생성
	 * 2) 폐기 -> 반출 생성  : 폐기화물일괄반출생성
	 * @param batchDisp
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public DisposalSaveResult executeBatchDisposal(BatchDisposal batchDisp, HttpServletRequest request) throws Exception {
		DisposalSaveResult result = new DisposalSaveResult();
		String batchExecType = batchDisp.getBatchExecType(); //DISP,OUT
		String inputUserId = batchDisp.getInputUserId();
		String dateTimeFm =  batchDisp.getDateFormat();
		String gmtTime     = batchDisp.getGmtTime();
		String entCode     = batchDisp.getEntCode();
		List<String> lst = new ArrayList<>();
		Integer nResultCnt = 0;
		// blList
		if( "DISP".equals(batchExecType)) { // IN => DISP 반입 => 폐기 처리 DISP
			// 최신 데이터 6개 가져오기
			Disposal recentDisposal = bmsDisposalMapper.selectRecentDisposalData(batchDisp.getEntCode());
			for (InventoryIn in : batchDisp.getInList()) {
				//in.setEntCode(batchDisp.getEntCode());
				String logVal = "batch  IN => DISP  InventoryIn:" + in;
				log.info(logVal);
				//1) TB_BMS_DISPOSAL.  cargoReferenceNo 데이터 확인 같은 반입 번호 있으면 처리 부분에서 제외후 list에 넣기
				Integer nCnt = bmsDisposalMapper.selectDisposalBatchCheckCount(in);
				if(nCnt >0){ // BL 번호로 존재 하면 // 카운트에서 제외 처리
					lst.add(in.getBlNo()); // 중복 체크
				}else {
					Disposal disp = new Disposal();
					//2) TB_BMS_IN 에서 TB_BMS_DISPOSAL 로 데이터 하나 넣기
					// 최근 Disposal 참조 컬럼 객체 넣기
					disp.setDisposalReason(recentDisposal.getDisposalReason());
					disp.setDisposalCode(recentDisposal.getDisposalCode());
					disp.setDisposalName(recentDisposal.getDisposalName());
					disp.setDisposalLoc(recentDisposal.getDisposalLoc());
					disp.setDisposalExpDate(recentDisposal.getDisposalExpDate());
					disp.setDisposalMethod(recentDisposal.getDisposalMethod());
                    //
					disp.setGmtTime(gmtTime);
					disp.setDateFormat(dateTimeFm);
					disp.setRegisterUserId(inputUserId);
					disp.setUpdateUserId(inputUserId);
					disp.setInNo(in.getInNo());
					Util.accessControlDatabase(disp, "dateFormat", false, null, null, null, null);
					bmsDisposalMapper.insertDisposalBatchGen(disp);

					 // 폐기 입력 Y 로
				//3) TB_BMS_IN.DISPOSAL_YN ='Y' 로 업데이트
					this.updateBmsInDisposalYn_Y(disp);
					nResultCnt++; // 성공 카운트
					
					// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(batchDisp.getEntCode()));
					processLogs.setEntBusinessCode(Util.null2str(batchDisp.getEntBusinessCode()));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(inputUserId));
					processLogs.setLogJobType("DATA");
					processLogs.setBound("AIR");
					processLogs.setBoundType("IMP");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
					processLogs.setDbType(BmsConsts.DbType.BMS_DISPOSAL);
					processLogs.setDescription(BmsConsts.DbType.BMS_DISPOSAL+" [입력 CARGO_REFERENCE_NO:"+ in.getCargoReferenceNo() +"]");
					processLogs.setJobNo(String.valueOf(in.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
					processLogs.setJobLog(new Gson().toJson(in));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(in.getLogUserBusinessPrintName());
					processLogs.setProcessUserName(in.getLogUserName());
					processLogs.setProcessUserNameEng(in.getLogUserNameEng());
					//	String podStatus = BmsConsts.PodStatus.BMS99;
					// 호출시 필요 항목 : BL_NO, POD_STATUS
					bmsCommService.insertLogAndPodStatus(processLogs, request
							, BmsConsts.PodStatus.BATCH /* pod Status 값 매핑 61로만 입력되어 들어 온다.  */
							, ""
							, null
					);
				}
			}
		}else if( "OUT".equals(batchExecType)) { // DISP => OUT: 폐기 반출 처리
			List<Disposal> dispList = batchDisp.getDispList();
			Map<String, Object> paramForDisp = new HashMap<String, Object>();
			paramForDisp.put("dispList", batchDisp.getDispList());
			List<InventoryIn> dispMapList = bmsDisposalMapper.selectOdJobNoListFromDisp(paramForDisp);
			
			if(dispMapList.size() > 0) {
				Map<String, Object> holdParam = new HashMap<String, Object>();
				holdParam.put("srchEntCode", batchDisp.getEntCode());
				holdParam.put("dateFormat", "23");
				holdParam.put("whereInList", dispMapList);
				
				List<OrderHold> orderHoldList = orderMapper.selectHoldWhereInOdJobNos(holdParam);
				for(OrderHold orderHold : orderHoldList) {
					if(orderHold.getCarryoutStopYn().equals("Y") && orderHold.getHoldReleaseYn().equals("N")) {
						result.setResultCode("ABMS100036");// 저장 되었습니다.
						String blNo = "";
						for(InventoryIn disp : dispMapList) {
							if(disp.getOdJobNo().equals(orderHold.getOdJobNo())) {
								blNo = disp.getBlNo();
							}
						}
						result.setHoldReason(blNo + " " + orderHold.getHoldReason());
						return result;
					}
				}
			}
			String bmsWhCode = batchDisp.getBmsWhCode();
			for (Disposal disp : batchDisp.getDispList()) {
				disp.setEntCode(entCode);
				String logVal = "batch DISP => OUT Disposal:" + disp;
				log.info(logVal);
				//1) TB_BMS_OUT.CARGO_REFERENCE_NO 와 OUT_CLASS='83' 자료 있는지 확인
				Integer nCnt =  bmsDisposalMapper.selectOutBatchCheckCount(disp);
				//2) 1에서 자료있으면 list에 넣어서  리스트 제외 함
				if(nCnt >0){ // BL 번호로 존재 하면 // 카운트에서 제외 처리
					lst.add(disp.getBlNo()); // 중복 체크
				}else {
					SpCallVo voIn = getBmsOutCustomsReportNo(bmsWhCode);
					//3) TB_BMS_DISPOSAL 에서 TB_BMS_OUT로 데이터 입력
				    disp.setCustomsReportNo(voIn.getRetCustomsReportNo());
					//
					bmsDisposalMapper.insertOutBatchGen(disp);
					// 3.5) BMS_OUT IN_NO group 하여 BMS_IN에 SUM(OUT_CT), SUM(OUT_WT), SUM(OUT_CBM)
					bmsDisposalMapper.updateInCtWtCbm(disp);
					//4) TB_BMS_DISPOSAL.OUT_YN='Y' 로 업데이트
					disp.setOutYn("Y");
					bmsDisposalMapper.updateBmsDisposalOutYn(disp);
					nResultCnt++; // 성공 카운트
				}


			}
		}
	/*	lst.add("7V1A128CQT3") ;
		lst.add("316Y9VHZTPF") ;*/
		result.setReturnList(lst);
		result.setResultCnt(nResultCnt);
			return result;
	}

	public SpCallVo getBmsOutCustomsReportNo(String bmsWhCode) {
		SpCallVo voIn = new SpCallVo();
		voIn.setMode("M"); // 	M.Manual
		voIn.setBmsWhCode(bmsWhCode);
		voIn.setInOut("O"); //  I.TB_BMS_IN, O.TB_BMS_OUT
		// 0 18자리 CUSTOMS_REPORT_NO 시퀀스 작성
		bmsDisposalMapper.callProcBMSInOutSeq(voIn);
		return voIn;
	}
	public SpCallVo getBmsInCustomsReportNo(String bmsWhCode) {
		SpCallVo voIn = new SpCallVo();
		voIn.setMode("M"); // 	M.Manual
		voIn.setBmsWhCode(bmsWhCode);
		voIn.setInOut("I"); // I.TB_BMS_IN, O.TB_BMS_OUT
		// 0 18자리 CUSTOMS_REPORT_NO 시퀀스 작성
		bmsDisposalMapper.callProcBMSInOutSeq(voIn);
		return voIn;
	}

	/**
	 * 해상및 항공운송 Excel 자료 데이터 내리기
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> selectDisposalListExcel(SearchDisposal search) throws Exception {
		DisposalList disposalList = new DisposalList();
		//setSearchCondition(search);

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

		List<Map<String,Object>> list = new ArrayList<>();
		//srchImportExportClass 1.해상, 3.항공
		if("1".equals(search.getSrchImportExportClass())) { // 1.해상 헤더가 2개다
			search.setSrchImportExportClass(null);
			Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "|CARRYIN_DATE|DISPOSAL_ISSUE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO");
			list = bmsDisposalMapper.selectDisposalExcelSeaList(search);
		}else{
			search.setSrchImportExportClass(null);
			Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "|CARRYIN_DATE|DISPOSAL_ISSUE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO");
			list = bmsDisposalMapper.selectDisposalExcelAirList(search);
		}

		return list;
	}
	/***
	 * CUSTOMS_REPORT_NO 에 대해서 각 쿼리 마다 다른 값이 넣머 오므로 각 단계 별로
	 * setSearchType 의 값을 넘긴다.
	 * @param search
	 * @param gubun
	 */
	private static void getSearchCondiDisposal(SearchDisposal search , String gubun , String originSrchNoType) {
		if (BmsConsts.BMS_CONST_STOCK.equals(gubun)) { //// TB_BMS_OUT A.CUSTOMS_REPORT_NO, TB_BMS_IN.B.CUSTOMS_REPORT_NO
			if ("CUSTOMS_REPORT_NO".equals(originSrchNoType)) {
				search.setSrchNoType("A.CUSTOMS_REPORT_NO");
			}
		} else if (BmsConsts.BMS_CONST_DISPOSAL.equals(gubun)) {// inventory TB_BMS_IN.A.CUSTOMS_REPORT_NO
			if ("CUSTOMS_REPORT_NO".equals(originSrchNoType)) {
				search.setSrchNoType("B.CUSTOMS_REPORT_NO");
			}
		}
	}
	
	/**
	 * 폐기스캔
	 * @param disposal
	 * @return
	 * @throws Exception
	 */
	public DisposalSaveResult insertDiscard(Disposal disposal, HttpServletRequest request) throws Exception {
		DisposalSaveResult disposalSaveResult = new DisposalSaveResult();
        List<Disposal> discardScan = bmsDisposalMapper.selectDiscardScan(disposal.getBlNo()); // 자료 조회
		int selectCnt = discardScan.size(); // 자료 조회 건수
		
	    Map<String, Object> paramMap = new HashMap<>();  // insert/update Map
	    Map<String, Object> returnMap = new HashMap<>();
	    
	    paramMap.put("locationPlace", disposal.getLocationPlace());
	    paramMap.put("blNo", disposal.getBlNo());
	    paramMap.put("registerUserId", disposal.getRegisterUserId());
	    paramMap.put("updateUserId", disposal.getUpdateUserId());
	    
		// B/L또는박스번호가 빈 값인 경우 종료
	    if("".equals(disposal.getBlNo())) {
		    paramMap.put("scanResult", "무적"); 
	        returnMap.put("scanResult", paramMap.get("scanResult"));		
		    disposalSaveResult.setReturnMap(returnMap);
			disposalSaveResult.setSelectCnt(selectCnt);
	        return disposalSaveResult;
	    }

	    // 건수가 1개 있는 경우
		if(discardScan.size() == 1) {
			
	        String disposalReason = Util.null2str(discardScan.get(0).getDisposalReason());
			if(disposalReason.contains("모조")) {
			    paramMap.put("scanResult", "모조류");		    
			}else {
				paramMap.put("scanResult", "완료");		    				
			}
		    
	        String blNo = discardScan.get(0).getBlNo();
	        String boxNo = discardScan.get(0).getPackingNo();
	        String cargoReferenceNo = discardScan.get(0).getCargoReferenceNo();

	        returnMap.put("blNo", blNo);
	        returnMap.put("boxNo", boxNo);
		    paramMap.put("cargoReferenceNo", cargoReferenceNo); 

		    bmsDisposalMapper.insertDiscardScanLog(paramMap); // 로그 저장
		    bmsDisposalMapper.updateDiscardScan(paramMap);    // 폐기자료 업데이트

		// 건수가 2개 이상 있는 경우
		}else if(discardScan.size() > 1) {
		    paramMap.put("scanResult", "중복"); 
		    bmsDisposalMapper.insertDiscardScanLog(paramMap); // 로그 저장
		    
		// 건수가 없는 경우   
		}else if(discardScan.size() == 0) {
		    paramMap.put("scanResult", "무적"); 
		    bmsDisposalMapper.insertDiscardScanLog(paramMap); // 로그 저장
		}
        returnMap.put("scanResult", paramMap.get("scanResult"));		
	    
	    disposalSaveResult.setReturnMap(returnMap);
		disposalSaveResult.setSelectCnt(selectCnt);
		disposalSaveResult.setResultCode("A012051");// 저장 되었습니다.

		return disposalSaveResult;
	}
}
