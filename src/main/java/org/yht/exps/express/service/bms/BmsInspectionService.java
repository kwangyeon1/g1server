package org.yht.exps.express.service.bms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsInMapper;
import org.yht.exps.express.mapper.bms.BmsInspectionMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsInspection.BmsInspection;
import org.yht.exps.express.model.bms.bmsInspection.BmsInspectionList;
import org.yht.exps.express.model.bms.bmsInspection.search.SearchBmsInspectionList;
import org.yht.exps.express.model.bms.carryin.BatchCarryIn;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.CarryInSaveResult;
import org.yht.exps.express.model.bms.disposal.SpCallVo;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.Filters;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsInspectionService {

	@Autowired
	CommonMapper commonMapper;

	@Autowired
	BmsInspectionMapper bmsInspectionMapper;

	@Autowired
	BmsInMapper bmsInMapper;

	@Autowired
	BmsDisposalService bmsDisposalService;

	@Autowired
	BmsCommService bmsCommService;

	/**
	 * Paging, Sort, Filter 관련 처리
	 * @param search
	 * @throws Exception
	 */
	private static void setSearchCondition(SearchBmsInspectionList search) throws Exception {
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
	 * 검사계획통보 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsInspectionList selectBmsInspectionList(SearchBmsInspectionList search) throws Exception {
		BmsInspectionList bmsInspectionList = new BmsInspectionList();

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

		Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|tbi.CARRYIN_DATE|isp.INSPECT_DATE|isp.NOTIFY_DATE_TIME|noDateChk", "srchNoType", "|isp.BL_NO|isp.APPROVAL_NO|tbi.CARGO_REFERENCE_NO|tbi.CUSTOMS_REPORT_NO|isp.MBL_NO");
		List<BmsInspection> list = bmsInspectionMapper.selectBmsInspectionList(search);
		bmsInspectionList.setBmsInspectionList(list);

		if(list.size() > 0) {
			bmsInspectionList.setTotal(list.get(0).getTotal());
		} else {
			bmsInspectionList.setTotal(0);
		}
		return bmsInspectionList;
	}

	/**
	 * 검사계획 통보 - 특송장 이고 - 반출입 생성 처리
	 * @param batchCarryIn
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CarryInSaveResult executeBatchCarryIn(BatchCarryIn batchCarryIn, HttpServletRequest request) throws Exception {
		CarryInSaveResult result = new CarryInSaveResult();
		String batchExecType = batchCarryIn.getBatchExecType(); //DISP,OUT
		String inputUserId = batchCarryIn.getInputUserId();
		String dateTimeFm =  batchCarryIn.getDateFormat();
		String gmtTime     = batchCarryIn.getGmtTime();
		List<String> returnLst = new ArrayList<>();
		List<String> outLst = new ArrayList<>();
		List<String> stockLst = new ArrayList<>();
		Integer nResultCnt = 0;

		// blList
		String bmsWhCode = batchCarryIn.getBmsWhCode();
		//오늘, 반출에서 이고차수 최대값가져오기
		int maxTicOutSeq = bmsInMapper.selectTodayMaxTicOutSeqFromBmsOut();
		int nextTicOutSeq = maxTicOutSeq + 1; // 이고반출에 저장할 이고차수 = 반출에있는 최대 이고차수 + 1
		for (CarryIn in : batchCarryIn.getInList()) {
			String logVal = "batch  IN => OUT  batchCarryIn:" + in;
			log.info(logVal);
			// 그리드 값이 - 가 표시될 가능성이 있으므로 정리
			in.setCargoReferenceNo(in.getCargoReferenceNo().replaceAll("-",""));
			//1) TB_BMS_IN.LAST_POD_STATUS 'BWH0' 접수 리스트 확인
			in.setRegisterUserId(inputUserId);
			in.setUpdateUserId(inputUserId);
			Integer nCnt =  bmsInMapper.selectCarryInBatchCheckReceiptCount(in);
			Integer nOutCnt = bmsInMapper.selectCarryInBatchCheckOutCount(in);
			//2) 1에서 자료있으면 list에 넣어서  리스트 제외 함
			if(nCnt >0){ // BL 번호로 존재 하면 // 카운트에서 제외 처리
				returnLst.add(in.getBlNo()); // 중복 체크
			}else if(nOutCnt >0) {
				outLst.add(in.getBlNo()); // 중복 체크
			}else if(bmsInMapper.selectCarryInBatchCheckStockCount(in) > 0){
				stockLst.add(in.getBlNo());
			}
			else {
				SpCallVo voIn = bmsDisposalService.getBmsOutCustomsReportNo(bmsWhCode);
				//3) TB_BMS_DISPOSAL 에서 TB_BMS_OUT로 데이터 입력
				in.setCustomsReportNo(voIn.getRetCustomsReportNo());
				in.setNextTicOutSeq(nextTicOutSeq);
				
				bmsInMapper.insertCarryInOutBatchGen(in);
				// 3.5) BMS_OUT IN_NO group 하여 BMS_IN에 SUM(OUT_CT), SUM(OUT_WT), SUM(OUT_CBM)
				bmsInMapper.updateCarryInCtWtCbm(in);
				nResultCnt++; // 성공 카운트
				
				// 배차예정정보(TB_BMS_ALLOCATE_CAR_NOTI) 저장
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("bmsWhCode", bmsWhCode);
				tmpMap.put("registerUserId", inputUserId);
				tmpMap.put("updateUserId", inputUserId);
				bmsInMapper.insertBmsAllocateCarNotiByOutClass(tmpMap);

				// elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(Util.null2str(batchCarryIn.getEntCode()));
				processLogs.setEntBusinessCode(Util.null2str(batchCarryIn.getEntBusinessCode()));
				processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
				processLogs.setProcessUserId(Util.null2str(inputUserId));
				processLogs.setLogJobType("DATA");
				processLogs.setBound("AIR");
				processLogs.setBoundType("IMP");
				processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
				processLogs.setDbType(BmsConsts.DbType.BMS_OUT);
				//logMap.put("description", "Consol [반입번호 : " + 반입번호 + ", 내용 : "+ 내용 +"]"); , 사유  받아서 넣기
				processLogs.setDescription(BmsConsts.DbType.BMS_OUT+" [입력 CARGO_REFERENCE_NO:"+ in.getCargoReferenceNo() +"]");
				processLogs.setJobNo(String.valueOf(in.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
				processLogs.setJobLog(new Gson().toJson(in));
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(in.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(in.getLogUserName());
				processLogs.setProcessUserNameEng(in.getLogUserNameEng());
				// 호출시 필요 항목 : BL_NO, POD_STATUS
				bmsCommService.insertLogAndPodStatus(processLogs, request
						, BmsConsts.PodStatus.BATCH /* pod Status 값 매핑 61로만 입력되어 들어 온다.  */
						, ""
						, null
				);

			}
		}

		result.setReturnList(returnLst);
		result.setReturnOutList(outLst);
		result.setReturnStockList(stockLst);
		result.setResultCnt(nResultCnt);
		result.setNextTicOutSeq(nextTicOutSeq);
		return result;
	}

}
