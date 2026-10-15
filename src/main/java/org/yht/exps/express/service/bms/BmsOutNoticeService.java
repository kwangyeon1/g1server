package org.yht.exps.express.service.bms;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsOutNoticeMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsOutNotice.BatchBmsOutNotice;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNotice;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticeDetail;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticeDetailList;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticeList;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticePlan;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticePlanList;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNoticeSaveResult;
import org.yht.exps.express.model.bms.bmsOutNotice.DeleteBmsOutNotice;
import org.yht.exps.express.model.bms.bmsOutNotice.search.SearchBmsOutNotice;
import org.yht.exps.express.model.bms.bmsStock.ExcelUploadCustomsStock;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.disposal.InventoryInList;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.Filters;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.bms.send.BmsSendXmlParser;
import org.yht.exps.express.service.eCommerce.OrderService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
 
@Slf4j
@Service
@Transactional
public class BmsOutNoticeService {

	@Autowired
	CommonMapper commonMapper;

	@Autowired
	BmsOutNoticeMapper bmsOutNoticeMapper;

	@Autowired
	CommonService commonService;

	@Autowired
	BmsSendXmlParser bmsSendXmlParser;

	@Autowired
	BmsCommService bmsCommService;
	
	@Autowired
    CustomProperties customProperties;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;
	
	@Resource(name = "fileService")
    FileService fileService;

    @Resource(name = "orderService")
    OrderService orderService;

	/**
	 * Paging, Sort, Filter 관련 처리
	 * @param search
	 * @throws Exception
	 */
	private static void setSearchCondition(SearchBmsOutNotice search) throws Exception {
		if(search.getPage() != 0 && search.getPageSize() != 0) {
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}

		/** 화물관리번호(cargoReferenceNo) 매핑 처리 */
		if(Util.null2str(search.getChangeField()).equals("Y")) {
			List<Sort> sortList = search.getSort();

			if(sortList != null) {
				for(Sort sort : sortList) {
					if(sort.getField().equals("cargoReferenceNoDisp")) {
						sort.setField("cargoReferenceNo");
					}
				}
			}
			Util.camelCaseToUnderscore(search);

			Filter filterList = search.getFilter();
			if(filterList != null) {
				for(Filters filter : filterList.getFilters()) {
					if (filter.getField().equals("CARGO_REFERENCE_NO_DISP")) {
						filter.setField("CARGO_REFERENCE_NO");
					}
				}
			}
		}
	}

	/**
	 * 반출통고 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsOutNoticeList selectBmsOutNoticeList(SearchBmsOutNotice search) throws Exception {
		BmsOutNoticeList bmsOutNoticeList = new BmsOutNoticeList();

		setSearchCondition(search);
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|B.OUT_NOTICE_DATE|A.CARRYIN_DATE|B.PERIOD_OVER_DATE|noDateChk", "srchNoType", "|B.BL_NO|B.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|B.MBL_NO");
		List<BmsOutNotice> list = bmsOutNoticeMapper.selectBmsOutNoticeList(search);
		bmsOutNoticeList.setBmsOutNoticeList(list);

		if(list.size() > 0) {
			bmsOutNoticeList.setTotal(list.get(0).getTotal());
		} else {
			bmsOutNoticeList.setTotal(0);
		}

		return bmsOutNoticeList;
	}

	/**
	 * 미승인재고 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public InventoryInList selectInventoryList(SearchBmsOutNotice search) throws Exception {
		InventoryInList inventoryInList = new InventoryInList();

		setSearchCondition(search);
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|A.OUT_NOTICE_DATE|A.CARRYIN_DATE|A.PERIOD_OVER_DATE|noDateChk", "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|A.MBL_NO");
		List<InventoryIn> list = bmsOutNoticeMapper.selectInventoryList(search);
		inventoryInList.setInventoryInList(list);

		if(list.size() > 0) {
			inventoryInList.setTotal(list.get(0).getTotal());
		} else {
			inventoryInList.setTotal(0);
		}

		return inventoryInList;
	}

	/**
	 * 누락통보 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsOutNoticePlanList selectBmsOutNoticeOmittedList(SearchBmsOutNotice search) throws Exception {
		BmsOutNoticePlanList bmsOutNoticePlanList = new BmsOutNoticePlanList();

		setSearchCondition(search);
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|A.OUT_NOTICE_DATE|A.CARRYIN_DATE|B.PERIOD_OVER_DATE|noDateChk", "srchNoType", "|B.BL_NO|B.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO|B.MBL_NO");
		List<BmsOutNoticePlan> list = bmsOutNoticeMapper.selectBmsOutNoticeOmittedList(search);
		bmsOutNoticePlanList.setBmsOutNoticePlanList(list);

		if(list.size() > 0) {
			bmsOutNoticePlanList.setTotal(list.get(0).getTotal());
		} else {
			bmsOutNoticePlanList.setTotal(0);
		}

		return bmsOutNoticePlanList;
	}

	/**
	 * 등록 ENTRY - 상세 조회
	 * @param search
	 * @return
	 */
	public BmsOutNoticeDetail selectBmsOutNoticeDetail(SearchBmsOutNotice search) {
		BmsOutNoticeDetail detail = new BmsOutNoticeDetail();

		BmsOutNotice bmsOutNotice = null;
		InventoryIn inventoryIn = null;

		bmsOutNotice = bmsOutNoticeMapper.selectBmsOutNoticeDetail(search);
		inventoryIn = bmsOutNoticeMapper.selectInventoryDetail(search);

		detail.setBmsOutNotice(bmsOutNotice);
		detail.setInventoryIn(inventoryIn);

//		detail.setSrchGbn(search.getSrchGbn());
//		detail.setSrchCargoReferenceNo(search.getSrchCargoReferenceNo());

		return detail;
	}

	/**
	 * 등록 ENTRY - 오류내역 조회
	 * @param srchDeclarationNo
	 * @return
	 */
	public String selectErrorDesc(String srchDeclarationNo) {
		String msg = bmsOutNoticeMapper.selectErrorDesc(srchDeclarationNo);
		return StringEscapeUtils.escapeHtml4(msg);
	}

	/**
	 * POPUP - 조회번호, 타입에 따라 데이터 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsOutNoticeDetailList selectListBySrchNo(SearchBmsOutNotice search) throws Exception {
		BmsOutNoticeDetailList detailList = new BmsOutNoticeDetailList();

		search.setPageSize(50);
		search.setPage(1);
		search.setDefaultSort("IN_NO DESC");
		setSearchCondition(search);

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|OUT_NOTICE_DATE|CARRYIN_DATE|PERIOD_OVER_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO");
		/** 반출통고(TB_BMS_OUT_NOTICE) 조회 */
		List<BmsOutNotice> bmsOutNoticeList = bmsOutNoticeMapper.selectBmsOutNoticeListBySrchNo(search);

		if(bmsOutNoticeList.size() > 0) {
			detailList.setTotal(bmsOutNoticeList.get(0).getTotal());
		} else {
			detailList.setTotal(0);
		}

		detailList.setBmsOutNoticeList(bmsOutNoticeList);

		Util.accessControlDatabase(search, "srchDateFormat", true, null, null, "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO|MBL_NO");
		/** 반출통고에 데이터가 없을 경우, 반입(TB_BMS_IN) 조회 */
		List<InventoryIn> inventoryInList = bmsOutNoticeMapper.selectInventoryListBySrchNo(search);

		if(inventoryInList.size() > 0) {
			detailList.setTotalIn(inventoryInList.get(0).getTotal());
		} else {
			detailList.setTotal(0);
		}

		detailList.setInventoryInList(inventoryInList);

		return detailList;
	}

	/**
	 * 등록 ENTRY - 반출통고 등록 or 수정
	 * @param bmsOutNotice
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutNoticeSaveResult saveBmsOutNotice(BmsOutNotice bmsOutNotice, HttpServletRequest request) throws Exception {
		BmsOutNoticeSaveResult bmsOutNoticeSaveResult = new BmsOutNoticeSaveResult();
		InventoryIn bmsInInfoWhenInsert = null;
		log.info("########################## inNo: " + bmsOutNotice.getInNo() + " / outNoticeNo: " + bmsOutNotice.getOutNoticeNo() + " / outNoticePlanNo: " + bmsOutNotice.getOutNoticePlanNo() + "##########################");

		/** 반출통고(TB_BMS_OUT_NOTICE) 데이터가 있을 경우 반출통고 데이터 UPDATE  */
		if(bmsOutNotice.getOutNoticeNo() != null) {
			bmsOutNoticeMapper.updateBmsOutNotice(bmsOutNotice);

			/** 작업내역 로그 */
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(bmsOutNotice.getEntCode()));
			processLogs.setEntBusinessCode(Util.null2str(bmsOutNotice.getEntBusinessCode()));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(bmsOutNotice.getRegisterUserId()));
			processLogs.setLogJobType("DATA");
    		processLogs.setBound("AIR");
    		processLogs.setBoundType("IMP");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_OUT_NOTICE);
			processLogs.setDescription(BmsConsts.DbType.BMS_OUT_NOTICE + " [UPDATE CARGE_REFERENCE_NO: " + String.valueOf(bmsOutNotice.getCargoReferenceNo()) + "]");
			processLogs.setJobNo(String.valueOf(bmsOutNotice.getOdJobNo()));
			processLogs.setJobLog(new Gson().toJson(bmsOutNotice));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(bmsOutNotice.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(bmsOutNotice.getLogUserName());
			processLogs.setProcessUserNameEng(bmsOutNotice.getLogUserNameEng());
			bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.UPDATE, "", bmsOutNotice);
		}
		/**
		 *  반출통고(TB_BMS_OUT_NOTICE) 데이터가 없을 경우
		 *  1) 반출통고 INSERT
		 *  2) 반입 통고일자, 체화일자 UPDATE
		 *  3) 반출통고누락 데이터가 있을 경우, 반출통고등록여부(OUT_NOTICE_YN) Y로 UPDATE
		 */
		else {
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("entCode", bmsOutNotice.getEntCode());
			paramMap.put("entBusinessCode", bmsOutNotice.getEntBusinessCode());
			paramMap.put("solution", "BMS");
			paramMap.put("optionCode", "A01");
			String optionValueForNoticeDate = commonMapper.selectSystemOption(paramMap);
			
			Map<String,Object> bmsInParam = new HashMap<String,Object>();
			bmsInParam.put("inNo", bmsOutNotice.getInNo());
			bmsInParam.put("optionValue", optionValueForNoticeDate);
			bmsInInfoWhenInsert = bmsOutNoticeMapper.selectBmsInCalcOutNoticeDateByInNo(bmsInParam); // 반입번호에 해당하는 반입(재계산된 통고일자,체화일자 포함) 조회
			
			bmsOutNotice.setOutNoticeDate(bmsInInfoWhenInsert.getOutNoticeDate());
			bmsOutNotice.setPeriodOverDate(bmsInInfoWhenInsert.getPeriodOverDate());
			
			bmsOutNoticeMapper.insertBmsOutNotice(bmsOutNotice);

			/** 작업내역 로그 */
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(bmsOutNotice.getEntCode()));
			processLogs.setEntBusinessCode(Util.null2str(bmsOutNotice.getEntBusinessCode()));
			processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
			processLogs.setProcessUserId(Util.null2str(bmsOutNotice.getRegisterUserId()));
			processLogs.setLogJobType("DATA");
    		processLogs.setBound("AIR");
    		processLogs.setBoundType("IMP");
			processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
			processLogs.setDbType(BmsConsts.DbType.BMS_OUT_NOTICE);
			processLogs.setDescription(BmsConsts.DbType.BMS_OUT_NOTICE + " [INSERT CARGE_REFERENCE_NO: " + String.valueOf(bmsOutNotice.getCargoReferenceNo()) + "]");
			processLogs.setJobNo(String.valueOf(bmsOutNotice.getOdJobNo()));
			processLogs.setJobLog(new Gson().toJson(bmsOutNotice));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(bmsOutNotice.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(bmsOutNotice.getLogUserName());
			processLogs.setProcessUserNameEng(bmsOutNotice.getLogUserNameEng());
			bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.INSERT, "", bmsOutNotice);

			if(bmsOutNotice.getOutNoticePlanNo() != null) {
				bmsOutNotice.setOutNoticeYn("Y");
				bmsOutNoticeMapper.updateOutNoticeYn(bmsOutNotice);
			}
		}

		bmsOutNoticeMapper.updateBmsInDate(bmsOutNotice);
		
		bmsOutNoticeSaveResult.setResultCode("A012051"); /** 저장 되었습니다. */

		return bmsOutNoticeSaveResult;
	}
	
	/**
	 * 반출통고 출력여부 수정
	 * @param bmsOutNotice
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutNoticeSaveResult updatePrintYn(BmsOutNoticeList bmsOutNoticeList) throws Exception {
		BmsOutNoticeSaveResult bmsOutNoticeSaveResult = new BmsOutNoticeSaveResult();
		bmsOutNoticeMapper.updateOutNoticePrintYn(bmsOutNoticeList);
		return bmsOutNoticeSaveResult;
	}

	/**
	 * 반출통고 삭제
	 * @param delete
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutNoticeSaveResult deleteBmsOutNotice(DeleteBmsOutNotice delete, HttpServletRequest request) throws Exception {
		BmsOutNoticeSaveResult result = new BmsOutNoticeSaveResult();

		int succCnt = 0;
		int failCnt = 0;
		BmsOutNotice bmsOutNotice;
		SearchBmsOutNotice searchBmsOutNotice = new SearchBmsOutNotice();
		List<Map<String, Object>> deleteList = delete.getDataList();

		for (Map<String, Object> deleteMap : deleteList) {
			/** 반출통고(TB_BMS_OUT_NOTICE) 재조회 */
			searchBmsOutNotice.setSrchCargoReferenceNo(Util.null2str(deleteMap.get("cargoReferenceNo")));
			searchBmsOutNotice.setSrchInNo(Util.null2str(deleteMap.get("inNo")));
			bmsOutNotice = bmsOutNoticeMapper.selectBmsOutNoticeDetail(searchBmsOutNotice);
			log.info("########################## outNoticeNo: {}", bmsOutNotice.getOutNoticeNo());

			if(bmsOutNotice != null) {
				/** LAST_POD_STATUS가 BMS01(입력), BMS02(송신실패), BMS05(오류)일 경우 */
				if("BMS01".equals(bmsOutNotice.getLastPodStatus()) || "BMS02".equals(bmsOutNotice.getLastPodStatus()) || "BMS05".equals(bmsOutNotice.getLastPodStatus())) {
					/** 해당 반출통고 데이터 삭제 + 누락통보 테이블의 반출통고등록여부(OUT_NOTICE_YN) N으로 UPDATE */
					succCnt++;
					bmsOutNoticeMapper.deleteBmsOutNotice(bmsOutNotice);
					bmsOutNotice.setOutNoticeYn("N");
					bmsOutNoticeMapper.updateOutNoticeYn(bmsOutNotice);

					/** 작업내역 로그 */
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setEntCode(Util.null2str(delete.getEntCode()));
					processLogs.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
					processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
					processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
					processLogs.setLogJobType("DATA");
            		processLogs.setBound("AIR");
            		processLogs.setBoundType("IMP");
					processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
					processLogs.setDbType(BmsConsts.DbType.BMS_OUT_NOTICE);
					processLogs.setDescription(BmsConsts.DbType.BMS_OUT_NOTICE + " [DELETE CARGE_REFERENCE_NO: " + String.valueOf(bmsOutNotice.getCargoReferenceNo()) + "]");
					processLogs.setJobNo(String.valueOf(deleteMap.get("odJobNo")));
					processLogs.setJobLog(new Gson().toJson(delete));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
					processLogs.setProcessUserName(delete.getLogUserName());
					processLogs.setProcessUserNameEng(delete.getLogUserNameEng());
					bmsCommService.insertLogAndPodStatus(processLogs, request, BmsConsts.PodStatus.DELETE, delete.getReason(), delete);
				} else {
					failCnt++ ;
				}
			}
		}

		result.setFailCnt(failCnt);
		result.setSuccCnt(succCnt);

		return result;
	}
	
	/**
	 * 반출통고 삭제
	 * @param delete
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public BmsOutNoticeSaveResult updatePodStatusFromBmsOutNotice(BmsOutNoticeList srchBmsOutNoticeList, HttpServletRequest request) throws Exception {
		BmsOutNoticeSaveResult result = new BmsOutNoticeSaveResult();

	    List<BmsOutNotice> outNoticeDataList = srchBmsOutNoticeList.getBmsOutNoticeList();

	    SqlSession sqlSession = null;

	    int succCnt = 0;
	    int failCnt = 0;
	    int batchCnt = 0;

	    try {
	        sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
	        BmsOutNoticeMapper bmsOutNoticeBatchMapper = sqlSession.getMapper(BmsOutNoticeMapper.class);

	        for (BmsOutNotice bmsOutNotice : outNoticeDataList) {

	            /*
	             * 로그/API 실행 대상 초기화
	             */
	            bmsOutNotice.setBatchUpdateTargetYn("N");

	            if (!"BMS05".equals(bmsOutNotice.getLastPodStatus())) {
	                continue;
	            }

	            bmsOutNotice.setBatchUpdateTargetYn("Y");
	            bmsOutNotice.setLastPodStatus("BMS01");
	            bmsOutNoticeBatchMapper.updatePodStatusFromBmsOutNotice(bmsOutNotice);

	            batchCnt++;

	            if (batchCnt % 100 == 0) {
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

	    /*
         * Java 변수 List내 각 data마다 batchUpdateTargetYn = 'Y'인걸 필터링,
         * batch commit후이기때문에 필터링된 data들의 OUT_NOTICE_NO들에 BMS01 조건을 추가해서 select조회한후,
         * 조회된 값만 pod DB insert와 process log를 남기면된다
         */
	    List<BmsOutNotice> outNoticeFilteredList = outNoticeDataList.stream().filter(data -> "Y".equals(data.getBatchUpdateTargetYn())).collect(Collectors.toList());
	    
	    Map<String,Object> paramMap = new HashMap<String,Object>();
	    paramMap.put("bmsOutNoticeList", outNoticeFilteredList);
	    List<BmsOutNotice> dataListForLog = bmsOutNoticeMapper.selectOutNoticeInNoticeNoWithBms01(paramMap); //  
	    
	    for (BmsOutNotice bmsOutNotice : dataListForLog) {

	        /** 작업내역 로그 */
	        ProcessLogs processLogs = new ProcessLogs();
	        processLogs.setEntCode(Util.null2str(srchBmsOutNoticeList.getEntCode()));
	        processLogs.setEntBusinessCode(Util.null2str(srchBmsOutNoticeList.getEntBusinessCode()));
	        processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
	        processLogs.setProcessUserId(Util.null2str(srchBmsOutNoticeList.getInputUserId()));
	        processLogs.setLogJobType("DATA");
	        processLogs.setBound("AIR");
	        processLogs.setBoundType("IMP");
	        processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
	        processLogs.setDbType(BmsConsts.DbType.BMS_OUT_NOTICE);
	        processLogs.setDescription(
	                BmsConsts.DbType.BMS_OUT_NOTICE + " 반출통고 일괄 오류해제 [ OUT_NOTICE_NO: " + String.valueOf(bmsOutNotice.getOutNoticeNo()) + "] 검색태그: 반출통고 " 
	                + bmsOutNotice.getCargoReferenceNo() + " "
	                + bmsOutNotice.getBlNo()
	        );
	        processLogs.setJobNo(!"".equals(Util.null2str(bmsOutNotice.getOdJobNo())) ? bmsOutNotice.getOdJobNo() : bmsOutNotice.getBlNo());
	        processLogs.setJobLog(new Gson().toJson(srchBmsOutNoticeList));
	        processLogs.setResult("ok");
	        processLogs.setEntBusinessPrintName(srchBmsOutNoticeList.getLogUserBusinessPrintName());
	        processLogs.setProcessUserName(srchBmsOutNoticeList.getLogUserName());
	        processLogs.setProcessUserNameEng(srchBmsOutNoticeList.getLogUserNameEng());

	        bmsCommService.insertLogAndPodStatus(
	                processLogs,
	                request,
	                BmsConsts.PodStatus.UPDATE,
	                "반출통고 일괄 오류해제",
	                bmsOutNotice
	        );
	    }

	    
	    /*
	     * 성공/실패 카운트 보정
	     *
	     * succCnt는 실제로 BMS01 입력 상태로 UPDATE된 row COUNT
	     * failCnt는 전체 grid Row COUNT - succCnt
	     */
	    succCnt = dataListForLog.size();
	    failCnt = outNoticeDataList.size() - succCnt;
	    result.setFailCnt(failCnt);
	    result.setSuccCnt(succCnt);

	    return result;
	}

	/**
	 * 관세청 XML 송신(Send) - GOVCBR5II(반출통고목록보고서)
	 * @param params
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsOutNoticeSaveResult sendOutToXml(BatchBmsOutNotice params, HttpServletRequest request) throws Exception {
		BmsOutNoticeSaveResult result = new BmsOutNoticeSaveResult();
		String batchExecType = params.getBatchExecType();

		if("SEND".equals(batchExecType)) {
			result = bmsSendXmlParser.callSendXmlGOVCBR5IIParserExecutor(params, request);
		}

		return result;
	}

	/**
     * 반출통고 엑셀업로드(엑셀업로드 체크)
     * */
    public BmsOutNoticeSaveResult checkExcelUpload(MultipartFile uploadExcel, String entCode, String excelUploadType) throws Exception {
    	BmsOutNoticeSaveResult bmsOutNoticeSaveResult = new BmsOutNoticeSaveResult();
        Map<String,Object> resultData = new HashMap<>();
        
        // 파일 확장자 유효성 검사
 		if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
 			bmsOutNoticeSaveResult.setResultCode("A00036");
 			return bmsOutNoticeSaveResult;
 		}
        
        FileUploadResponse result = fileService.storeFile(uploadExcel, entCode, true);

        //Map<String, Object> rstHbl = null;

        if(Util.null2str(result.getResult()).equals("ok")) {
            File file = fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile();
            String extension = null;
            if(file.getName().toLowerCase().indexOf(".xlsx") > -1) {
                extension = "xlsx";
            }else {
                extension = "xls";
            }

            String[] fieldName = null;
            String[] fieldTitle = null;
            String[] fieldLength = null;
            String[] fieldType = null;

            if(excelUploadType.equals("bmsOutNotice")) {
                fieldName = new String[] {
                	// 2025.10.17 엑셀 업로드 양식 변경	
                	/* "no",			"fileShare",	"lastPodStatus",    "blStatus", 		"carryinDate", 		"outNoticeDate", 	"periodOverDate",
                		"blNo",			"mblNo", 		"cargoReferenceNo", "customsReportNo", 	"cdType",			"cdUseType",		"pkgType",
                        "noticeCt",		"noticeWt", 	"customerCode",      "customerName", 	"customerZipcode", 	"customerAddress1", "customerAddress2",
                        "customerTel",	"customerRemarks",	"shipperCode",   "shipperName",     "consigneeCode",    "consigneeName",    "notifyCode",
                        "notifyName",	"commodityItemLoc",  "locationPlace",  "printYn",       "registerUserName", "registerDateTime", "updateUserName",
                        "updateDateTime" */
                        "cargoReferenceNo",	"customerName", "customerZipcode",	"customerAddress1",	"customerAddress2",	"customerTel"
                };
                fieldTitle = new String[] {
                	/*	"번호",			"F#",	"진행상태",    "관세청", 		"반입일자", 		"반출통고일자", 	"체화일자",
                		"BL번호",			"MBL번호", 		"화물관리번호", "반입신고번호", 	"화물구분",			"용도구분",		"포장종류",
                        "수량",		"중량", 	"화주코드",      "화주명", 	"화주우편번호", 	"기본주소", "상세주소",
                        "전화번호",	"비고",	"송하인코드",   "송하인명",     "수하인코드",    "수하인명",    "통지처코드",
                        "통지처명",	"영문품명",  "장치위치",  "통고서출력여부",       "입력자", "입력일시", "수정자",
                        "수정일시" */
                		"화물관리번호(필수)", "화주명(필수)", "우편번호(필수)", "기본주소(필수)", "상세주소", "전화번호"
                };
                fieldLength = new String[] {
                	/*	"10",			"pass",	"6",    "pass", 		"10", 		"10", 	"10",
                		"20",			"20", 		"19", "18", 	"1",			"1",		"2",
                        "4",		"16.3", 	"10",      "100", 	"10", 	"150", "150",
                        "50",	"150",	"10",   "100",     "10",    "100",    "10",
                        "100",	"300",  "10",  "1",       "10", "pass", "10",
                        "pass" */
                        "19", "100", "10", "150", "150", "50" 
                };
                fieldType = new String[] {
                	/*	"X",			"X",	"S",    "X", 		"X", 		"S", 	"S",
                		"S",			"S", 		"S", "X", 	"X",			"X",		"S",
                        "N",		"DBL", 	"S",      "SN", 	"S", 	"SN", "SN",
                        "S",	"SN",	"X",   "X",     "X",    "X",    "X",
                        "X",	"X",  "X",  "S",       "X", "X", "X",
                        "X" */
                        "S", "SN", "S", "SN", "SN", "S"
                };
            }

            List<Map<String, Object>> dataList = new ArrayList<>();
            InputStream is = null;
            try{
                is = new FileInputStream(file);
                Workbook wb = WorkbookFactory.create(is);
                Sheet sheet = wb.getSheetAt(0);
                DataFormatter df = new DataFormatter();
                int fieldSize = fieldName.length;
                StringBuffer errMsg = new StringBuffer();
                Map<String, Object> dataMap = null;
                String fieldValue = "";
                //데이터포맷
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                //엑셀업로드 양식체크(반출통고 엑셀업로드 양식인지 체크)
                //String excelType = String.valueOf(sheet.getRow(1).getCell(1));
                String excelType = String.valueOf(sheet.getRow(0).getCell(0));     
                StringBuilder allErrMsg = new StringBuilder();	// 전체 에러메시지               

                if(!excelType.equals("반출통고신고(반출통고)")) {
                    bmsOutNoticeSaveResult.setResultCode("A00036");
                    return bmsOutNoticeSaveResult;
                }
                if(extension.equals("xlsx")) {
                    XSSFCell myCell = null;
                    //for (int i = 7; i <sheet.getLastRowNum() + 1; i++) {
                    for (int i = 3; i <sheet.getLastRowNum() + 1; i++) {
                    	// 2025.10.17 엑셀 양식 변경으로 인한 TOTAL 부분 의미없음
                    	// 마지막행 TOTAL 부분은 대상에서 제외
//                    	if(excelUploadType.equals("bmsOutNotice") && i == sheet.getLastRowNum()) {
//                    		break;
//                    	}
                        if(sheet.getRow(i) == null) {
//                            bmsOutNoticeSaveResult.setResultCode("A11233");     /** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
//                            resultData.put("nullLine", i + 1);
//                            resultData.put("lastLine", sheet.getLastRowNum() + 1);
//                            bmsOutNoticeSaveResult.setResultData(resultData);
//                            return bmsOutNoticeSaveResult;                        	
                            allErrMsg.append("line ").append(i + 1).append(" : 행이 비어있습니다.<br />");
                        }

                        dataMap= new HashMap<String, Object>();
                        boolean hasError = false;	// 길이오류여부 체크
                        //셀 세팅
                        for (int j = 0; j < fieldSize; j++) {
                            myCell = (XSSFCell) sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            if("DATE".equals(fieldType[j])) {
                                String value = null;
                                try {
	                                if(myCell != null && myCell.getDateCellValue() != null) {
	                                    value = sdf.format(myCell.getDateCellValue());
	                                }
                                }catch(Exception e) {
                                	value = df.formatCellValue(myCell);
                                }
                                fieldValue = orderService.dataChk(value, fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i+1);
                            }
                            else if("X".equals(fieldType[j])) { // fieldType이 없는 항목은 셋팅하지 않음
								continue;
							}
                            else {
                            	if(j == 0) { // 화물관리번호 cell인 경우 자릿수 체크를 위해 - 제거
                            		myCell.setCellValue(df.formatCellValue(myCell).replaceAll("-", ""));
                            	}
                                fieldValue = orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i+1);
                                //log.info("fieldValue:::: {}", fieldValue);
                            }
                            
                            // 길이 오류가 있는 경우 전체 에러메시지에 추가
                            if (errMsg.length() > 0) {
                            	hasError = true;
                                allErrMsg.append(errMsg.toString()).append("");
                                errMsg.setLength(0); 
                            }
                            
                            if(!Util.null2str(fieldValue).equals("")) {
                                dataMap.put(fieldName[j], fieldValue);
                            }
                        }

                        //log.info("row번호:::: {}", i+1);
                        //System.out.println(dataMap);
                        // 길이 오류가 없는 경우만 진행
                        if (!hasError) {
	                        if(excelUploadType.equals("bmsOutNotice")) {
	                        	if(!Util.null2str(dataMap.get("cargoReferenceNo")).equals("") && !Util.null2str(dataMap.get("customerName")).equals("") && !Util.null2str(dataMap.get("customerZipcode")).equals("") && !Util.isEmpty(dataMap.get("customerAddress1"))){
									if(!Util.null2str(dataMap.get("customerZipcode")).matches("^[0-9]{5}$")) {
	//									bmsOutNoticeSaveResult.setResultCode("errorZipCode");	/** 우편번호가 올바른지 확인해주세요.(숫자 5자리) */
	//									return bmsOutNoticeSaveResult;	
						                allErrMsg.append("line ").append(i + 1).append(" : 우편번호가 올바른지 확인해주세요.(숫자 5자리)<br />");
									} else {
										dataList.add(dataMap);
									}
								}else{
	//								bmsOutNoticeSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
	//								resultData.put("errorLine", i + 1 );
	//								bmsOutNoticeSaveResult.setResultData(resultData);
	//								return bmsOutNoticeSaveResult;									
									allErrMsg.append("line ").append(i + 1).append(" : 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다.<br />");			                 
								}
	                        } else {
	                        	dataList.add(dataMap);
	                        }
                        }
                    }
                }else {
                    HSSFCell myCell = null;
                    //for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                    for (int i = 3; i < sheet.getLastRowNum() + 1; i++) {
                    	// 2025.10.17 엑셀 양식 변경으로 인한 TOTAL 부분 의미없음
                    	// 마지막행 TOTAL 부분은 대상에서 제외
//                    	if(excelUploadType.equals("bmsOutNotice") && i == sheet.getLastRowNum()) {
//                    		break;
//                    	}
                    	if(sheet.getRow(i) == null) {
//                            bmsOutNoticeSaveResult.setResultCode("A11233");     /** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
//                            resultData.put("nullLine", i + 1);
//                            resultData.put("lastLine", sheet.getLastRowNum() + 1);
//                            bmsOutNoticeSaveResult.setResultData(resultData);
//                            return bmsOutNoticeSaveResult;                    		
                            allErrMsg.append("line ").append(i + 1).append(" : 행이 비어있습니다.<br />");
                        }

                        dataMap= new HashMap<String, Object>();
                        boolean hasError = false;	// 길이오류여부 체크
                        //셀 세팅
                        for (int j = 0; j < fieldSize; j++) {
                            myCell = (HSSFCell) sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            if("DATE".equals(fieldType[j])) {
                                String value = null;
                                try {
                                	if(myCell != null && myCell.getDateCellValue() != null) {
                                		value = sdf.format(myCell.getDateCellValue());
                                	}
                                }catch(Exception e) {
                                	value = df.formatCellValue(myCell);
                                }
                                fieldValue = orderService.dataChk(value, fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i+1);
                            }
                            else if("X".equals(fieldType[j])) { // fieldType이 없는 항목은 셋팅하지 않음
								continue;
							}
                            else {
                            	if(j == 0) { // 화물관리번호 cell인 경우 자릿수 체크를 위해 - 제거
                            		myCell.setCellValue(df.formatCellValue(myCell).replaceAll("-", ""));
                            	}
                                fieldValue = orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i+1);
                            }
                            
                            // 길이 오류가 있는 경우 전체 에러메시지에 추가
                            if (errMsg.length() > 0) {
                            	hasError = true;
                                allErrMsg.append(errMsg.toString()).append("");
                                errMsg.setLength(0); 
                            }
                            
                            if(!Util.null2str(fieldValue).equals("")) {
                                dataMap.put(fieldName[j], fieldValue);
                            }
                        }
                        
                        // 길이 오류가 없는 경우만 진행
                        if (!hasError) {
	                        if(excelUploadType.equals("bmsOutNotice")) {
	                        	if(!Util.null2str(dataMap.get("cargoReferenceNo")).equals("") && !Util.null2str(dataMap.get("customerName")).equals("") && !Util.null2str(dataMap.get("customerZipcode")).equals("") && !Util.isEmpty(dataMap.get("customerAddress1"))){
	                        		if(!Util.null2str(dataMap.get("customerZipcode")).matches("^[0-9]{5}$")) {
//										bmsOutNoticeSaveResult.setResultCode("errorZipCode");	/** 우편번호가 올바른지 확인해주세요.(숫자 5자리) */
//										return bmsOutNoticeSaveResult;	                        			
						                allErrMsg.append("line ").append(i + 1).append(" : 우편번호가 올바른지 확인해주세요.(숫자 5자리)<br />");
									} else {
										dataList.add(dataMap);
									}
								}else{
//									bmsOutNoticeSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
//									resultData.put("errorLine", i + 1);
//									bmsOutNoticeSaveResult.setResultData(resultData);
//									return bmsOutNoticeSaveResult;									
									allErrMsg.append("line ").append(i + 1).append(" : 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다.<br />");			                 
								}
	                        } else {
	                        	dataList.add(dataMap);
	                        }
                        }
                    }
                }
                
             /* if(errMsg.length() != 0) {
                    bmsOutNoticeSaveResult.setResultCode("dataErr");
                    Map<String, Object> errMap = new HashMap<>();
                    errMap.put("errStr", errMsg);
                    bmsOutNoticeSaveResult.setResultData(errMap);
                    return bmsOutNoticeSaveResult;
                } */
                
                // 에러 메시지가 있을 경우
                if (errMsg.length() != 0 || allErrMsg.length() != 0) {
                    resultData.put("errStr", allErrMsg.toString());
                }

                resultData.put("entCode", entCode);
                resultData.put("excelUploadType", excelUploadType);
                resultData.put("dataList", dataList);
                resultData.put("dataSize", dataList.size());
                
                bmsOutNoticeSaveResult.setResultData(resultData);
                bmsOutNoticeSaveResult.setResultCode("200");
                
                String backupPath = customProperties.getBackupDir() + "excel/" + entCode + "/" + DateTime.getDate("yyyy-MM");
                File dir = new File(backupPath);
                if(dir.exists() == false){
                    dir.mkdirs();
                }
                String backupFilePath = backupPath + "/" + file.getName();
                file.renameTo(new File(backupFilePath));
            }catch (Exception e) {
            	e.printStackTrace();
                bmsOutNoticeSaveResult.setResultCode("A00063");
            }finally {
                if (is != null) is.close();
            }

        }else {
            bmsOutNoticeSaveResult.setResultCode("ERROR");
        }
        //log.info("bmsOutNoticeSaveResult: {}", bmsOutNoticeSaveResult.toString());
        return bmsOutNoticeSaveResult;
    }

    public BmsOutNoticeSaveResult saveExcelUpload(ExcelUploadCustomsStock excelUploadCustomsStock, HttpServletRequest request, String excelUploadType) throws Exception {
    	BmsOutNoticeSaveResult bmsOutNoticeSaveResult = new BmsOutNoticeSaveResult();
        ArrayList<BulkData> bulkList = new ArrayList<>();

        if(excelUploadCustomsStock.getDataList() == null) {
        	bmsOutNoticeSaveResult.setResultCode("NO DATA");
            return bmsOutNoticeSaveResult;
        }

        BulkData bulkData = null;
        String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
        Map<String, Object> logMap = null;

        int cnt = 1;
        SqlSession sqlSession = null;
        ProcessLogs processLogs = new ProcessLogs();
        StringBuffer errMsg = new StringBuffer();
        List<String> errCargoNoList = new ArrayList<>();
        int succCnt = 0;

        try {
            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
            BmsOutNoticeMapper bmsOutNoticeBatchMapper = sqlSession.getMapper(BmsOutNoticeMapper.class);
            CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);

            Map<String, Object> tmpLogMap = new HashMap<>();
            tmpLogMap.put("ent_code", excelUploadCustomsStock.getEntCode());
            tmpLogMap.put("ent_business_code", excelUploadCustomsStock.getEntBusinessCode());
            tmpLogMap.put("solution", "BMS");
            tmpLogMap.put("process_user_id", excelUploadCustomsStock.getRegisterUserId());
            tmpLogMap.put("log_job_type", "DATA");
            tmpLogMap.put("job_func_type", "EXCEL");
            tmpLogMap.put("result", "ok");
            tmpLogMap.put("process_date_time", System.currentTimeMillis());
            tmpLogMap.put("ent_business_print_name", excelUploadCustomsStock.getLogUserBusinessPrintName());
            tmpLogMap.put("process_user_name", excelUploadCustomsStock.getLogUserName());
            tmpLogMap.put("process_user_name_eng", excelUploadCustomsStock.getLogUserNameEng());

            if(request != null) {
                tmpLogMap.put("access_ip", Util.getClientIP(request));
            }
            
            Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("entCode", excelUploadCustomsStock.getEntCode());
			paramMap.put("entBusinessCode", excelUploadCustomsStock.getEntBusinessCode());
			paramMap.put("solution", "BMS");
			paramMap.put("optionCode", "A01");
			String optionValueForNoticeDate = commonMapper.selectSystemOption(paramMap);

            for (Map<String, Object> excelStock : excelUploadCustomsStock.getDataList()) {
                excelStock.put("entCode", excelUploadCustomsStock.getEntCode());
                excelStock.put("entBusinessCode", excelUploadCustomsStock.getEntBusinessCode());
                excelStock.put("gmtTime", excelUploadCustomsStock.getGmtTime());
                excelStock.put("registerUserId", excelUploadCustomsStock.getRegisterUserId());
                excelStock.put("updateUserId", excelUploadCustomsStock.getUpdateUserId());

                String cargoReferenceNo = Util.null2str(excelStock.get("cargoReferenceNo")).replaceAll("-","");

                excelStock.put("cargoReferenceNo", cargoReferenceNo);
                excelStock.put("printYn", Util.null2str(excelStock.get("printYn"), "N"));
                excelStock.put("customerName", Util.null2str(excelStock.get("customerName")));
                excelStock.put("customerZipcode", Util.null2str(excelStock.get("customerZipcode")));
                excelStock.put("customerAddress1", Util.null2str(excelStock.get("customerAddress1")));
                excelStock.put("customerAddress2", Util.null2str(excelStock.get("customerAddress2")));
                excelStock.put("customerTel", Util.null2str(excelStock.get("customerTel")));
                excelStock.put("customerRemarks", Util.null2str(excelStock.get("customerRemarks")));
                excelStock.put("outNoticeDate", Util.null2str(excelStock.get("outNoticeDate")));
                excelStock.put("periodOverDate", Util.null2str(excelStock.get("periodOverDate")));

                // 2025.10.17 엑셀 양식 변경으로 인한 주석처리
                /*
                excelStock.put("blNo", Util.null2str(excelStock.get("blNo")));
                excelStock.put("mblNo", Util.null2str(excelStock.get("mblNo")));
                excelStock.put("pkgType", Util.null2str(excelStock.get("pkgType")));
                excelStock.put("customerCode", Util.null2str(excelStock.get("customerCode")));
                excelStock.put("noticeCt", Util.null2int(excelStock.get("noticeCt")));
                excelStock.put("noticeWt", Util.null2dbl(excelStock.get("noticeWt")));
                */

                List<BmsOutNotice> outNotice = bmsOutNoticeBatchMapper.selectBmsOutNoticeForExcelUpload(cargoReferenceNo);
                
                //반출통고 자료가 있는 경우 화주명, 화주우편번호, 화주기본주소, 상세주소, 전화번호 UPDATE
                if(outNotice != null && !outNotice.isEmpty()) {
                	bmsOutNoticeBatchMapper.updateExcelUploadOutNotice(excelStock);
                	succCnt++;
                } else {
                	Map<String, Object> param = new HashMap<String, Object>();
                	param.put("cargoReferenceNo", cargoReferenceNo);
                	param.put("entCode", excelUploadCustomsStock.getEntCode());
                	param.put("entBusinessCode", excelUploadCustomsStock.getEntBusinessCode());
                	param.put("optionValue", optionValueForNoticeDate);
                	 
                	//자료가 없는 경우 반출통고(TB_BMS_OUT_NOTICE) 자동 생성
                	List<InventoryIn> inventoryIn = bmsOutNoticeBatchMapper.selectBmsInForExcelUpload(param);
                	if(inventoryIn != null && !inventoryIn.isEmpty()) {
                		excelStock.put("inNo", Util.null2str(inventoryIn.get(0).getInNo()));
                		excelStock.put("blNo", Util.null2str(inventoryIn.get(0).getBlNo()));
                		excelStock.put("mblNo", Util.null2str(inventoryIn.get(0).getMblNo()));
                		excelStock.put("pkgType", Util.null2str(inventoryIn.get(0).getPkgType()));
                		excelStock.put("noticeCt", Util.null2int(inventoryIn.get(0).getCt()));
                		excelStock.put("noticeWt", Util.null2dbl(inventoryIn.get(0).getWt()));
                		excelStock.put("customerCode", Util.null2str(inventoryIn.get(0).getCustomerCode()));
                		excelStock.put("outNoticeDate", Util.null2str(inventoryIn.get(0).getOutNoticeDate()));
                		excelStock.put("periodOverDate", Util.null2str(inventoryIn.get(0).getPeriodOverDate()));

                		bmsOutNoticeBatchMapper.insertExcelUploadOutNotice(excelStock);
                		succCnt++;
                	} else {
                		errCargoNoList.add(cargoReferenceNo); //미반입인 화물관리번호 저장
                	}
                }
                
                if( (cnt++ % 1000) == 0){
                    sqlSession.flushStatements();
                    sqlSession.clearCache();
                }
            }      
            
            if(!errCargoNoList.isEmpty()){
            	bmsOutNoticeSaveResult.setResultCode("dataErr");
//				errMsg.append("미반입건이 " + errCargoNoList.size() + "건 존재합니다. <br />");
//        		errMsg.append("미반입건은 반출통고 생성 대상에서 제외되었습니다 <br />");
            	errMsg.append("미반입건이거나 반출승인이 있는 " + errCargoNoList.size() + "건 존재하여 반출통고 생성 대상에서 제외되었습니다.<br />");
        		errMsg.append("제외된 화물관리번호 : " + errCargoNoList.toString() + "<br />");
        		
        		Map<String, Object> errMap = new HashMap<String, Object>();
        		errMap.put("errStr", errMsg);
				bmsOutNoticeSaveResult.setResultData(errMap);
            }
            
			bmsOutNoticeSaveResult.setSuccCnt(succCnt);

            sqlSession.flushStatements();
            sqlSession.clearCache();
            sqlSession.commit();
        }catch (Exception e){
            throw e;
        }finally {
            sqlSession.close();
        }

        // 처리 로그 기록
        processLogs.setEntCode(Util.null2str(excelUploadCustomsStock.getEntCode()));
        processLogs.setEntBusinessCode(Util.null2str(excelUploadCustomsStock.getEntBusinessCode()));
        processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
        processLogs.setBound("AIR");
        processLogs.setBoundType("IMP");
        processLogs.setProcessUserId(Util.null2str(excelUploadCustomsStock.getUpdateUserId()));
        processLogs.setJobNo("");
        processLogs.setLogJobType("EXCEL");
        processLogs.setDescription("Upload Excel BMS 반출통고");
        processLogs.setJobLog("");
        processLogs.setResult("ok");
        processLogs.setEntBusinessPrintName(Util.null2str(excelUploadCustomsStock.getLogUserBusinessPrintName()));
        processLogs.setProcessUserName(Util.null2str(excelUploadCustomsStock.getLogUserName()));
        processLogs.setProcessUserNameEng(Util.null2str(excelUploadCustomsStock.getLogUserNameEng()));
        commonService.insertLog(processLogs, request);

        return bmsOutNoticeSaveResult;
    }
    
}
