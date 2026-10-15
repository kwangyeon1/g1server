package org.yht.exps.express.service.bms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsOutCorrectionMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChange;
import org.yht.exps.express.model.bms.bmsInCorrection.ChangeItem;
import org.yht.exps.express.model.bms.bmsInCorrection.search.SearchInCorrection;
import org.yht.exps.express.model.bms.bmsOutCorrection.BatchBmsOutCorrection;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOut;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChange;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChangeDetail;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChangeDetailList;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChangeList;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChangeSaveResult;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutList;
import org.yht.exps.express.model.bms.bmsOutCorrection.DeleteCarryOutChange;
import org.yht.exps.express.model.bms.bmsOutCorrection.search.SearchOutCorrection;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.bms.send.BmsSendXmlParser;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsOutCorrectionService {

    @Autowired
    BmsOutCorrectionMapper bmsOutCorrectionMapper;

    @Autowired
    CommonService commonService;

    @Autowired
    BmsCommService bmsCommService;
	@Autowired
	BmsSendXmlParser bmsSendXmlParser;

    public CarryOutList selectCarryOutList(SearchOutCorrection search) throws Exception {
        CarryOutList carryOutList = new CarryOutList();
        //setSearchCondition(search);
        if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
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
        Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYOUT_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO");
        List<CarryOut> list  = bmsOutCorrectionMapper.selectCarryOutList(search);
        carryOutList.setCarryOutList(list);
        if(list.size() > 0) {
            carryOutList.setTotal(list.get(0).getTotal());
        }else {
            carryOutList.setTotal(0);
        }
        return carryOutList;
    }

    public CarryOutChangeList selectCarryOutChangeList(SearchOutCorrection search) throws Exception {
        CarryOutChangeList carryOutChangeList = new CarryOutChangeList();
        //setSearchCondition(search);
        if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
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
        Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYOUT_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO");
        List<CarryOutChange> list  = bmsOutCorrectionMapper.selectCarryOutChangeList(search);
        carryOutChangeList.setCarryOutChangeList(list);
        if(list.size() > 0) {
            carryOutChangeList.setTotal(list.get(0).getTotal());
        }else {
            carryOutChangeList.setTotal(0);
        }
        return carryOutChangeList;
    }

    public CarryOutChangeDetailList selectCarryOutChangeBySrchNo(SearchOutCorrection search) throws Exception {
        CarryOutChangeDetailList list = new CarryOutChangeDetailList();

        search.setPageSize(50);
        search.setPage(1);
        search.setDefaultSort(search.getSrchNoType());
        setSearchCondition(search);

        Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYOUT_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO");
        List<CarryOutChange> carryOutChangeList = bmsOutCorrectionMapper.selectCarryOutChangeList(search);
        if(carryOutChangeList.size() > 0) {
            list.setTotalCarryOutChange(carryOutChangeList.get(0).getTotal());
        }else {
            list.setTotalCarryOutChange(0);
        }
        list.setCarryOutChangeList(carryOutChangeList);

        Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYOUT_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO");
        List<CarryOut> carryOutList  = bmsOutCorrectionMapper.selectCarryOutList(search);
        if(carryOutList.size() > 0) {
            list.setTotal(carryOutList.get(0).getTotal());
        }else {
            list.setTotal(0);
        }
        list.setCarryOutList(carryOutList);

        return list;
    }

    public CarryOutChangeDetail selectCarryOutAndCarryOutChangeDetail(SearchOutCorrection search) {
        CarryOutChangeDetail detail = new CarryOutChangeDetail();
        CarryOut carryOut = null;
        CarryOutChange carryOutChange = null;

        if("CARRY_OUT_CHANGE".equals(search.getSrchCarryOutState())) {  //CARRY_OUT, CARRY_OUT_CHANGE
            carryOutChange  = bmsOutCorrectionMapper.selectCarryOutChangeDetail(search);
            List<ChangeItem> changeItemList = bmsOutCorrectionMapper.selectChangeItemListByChangeNo(search);
            carryOutChange.setChangeItemList(changeItemList); //반출정정정보는 여러건의 정정항목을 가짐
            search.setOutNo(carryOutChange.getInoutNo());
            search.setSrchOutNo(carryOutChange.getInoutNo());
            carryOut =  bmsOutCorrectionMapper.selectCarryOutDetail(search); // 상세 정보 조회
        }else {
            carryOutChange    = bmsOutCorrectionMapper.selectCarryOutChangeDetail(search);
            if(carryOutChange != null) {
                search.setChangeNo(carryOutChange.getChangeNo());
                List<ChangeItem> changeItemList = bmsOutCorrectionMapper.selectChangeItemListByChangeNo(search);
                carryOutChange.setChangeItemList(changeItemList); //반출정정정보는 여러건의 정정항목을 가짐
                //log.info("정정항목아이템: {}", changeItemList.toString());
            }
            carryOut =  bmsOutCorrectionMapper.selectCarryOutDetail(search);
        }

        //반입정정된 항목 갯수
        int changeItemCount = bmsOutCorrectionMapper.selectChangeItemListByChangeNo(search).size();

        detail.setCarryOutChange(carryOutChange);
        detail.setCarryOut(carryOut);

        detail.setSrchCarryOutState(search.getSrchCarryOutState());
        detail.setSrchChangeNo(search.getSrchChangeNo());
        detail.setSrchOutNo(search.getSrchOutNo());

        detail.setChangeItemCount(changeItemCount);
        return detail;
    }

    public CarryOutChangeSaveResult insertCarryOutChange(CarryOutChange carryOutChange, HttpServletRequest request) throws Exception {
        CarryOutChangeSaveResult carryOutChangeSaveResult = new CarryOutChangeSaveResult();
        //차수값 3자리로 처리 (ex: 차수가 1일 경우 001)
        String tmpChangeCount = "00"+carryOutChange.getChangeCount();
        String changeCount = tmpChangeCount.substring(tmpChangeCount.length()-3,tmpChangeCount.length());
        carryOutChange.setChangeCount(changeCount);
        int row = bmsOutCorrectionMapper.insertCarryOutChange(carryOutChange);
        int chgNo = carryOutChange.getChangeNo();
        //정정항목 insert
        List<ChangeItem> changeItemList = new ArrayList<>();

        for (Map<String,String> map : carryOutChange.getChangeInfoList()) {
            ChangeItem changeItem = new ChangeItem();
            //int changeNo = bmsOutCorrectionMapper.selectChangeNo(carryOutChange);
            changeItem.setChangeNo(carryOutChange.getChangeNo());
            changeItem.setChangeCode(map.get("changeCode"));
            changeItem.setChangeBefore(map.get("changeBefore"));
            changeItem.setChangeAfter(map.get("changeAfter"));
            changeItemList.add(changeItem);
        }

        //변경항목 입력
        for (ChangeItem changeItem : changeItemList) {
            int insertItemRows = bmsOutCorrectionMapper.insertChangeItem(changeItem);
            //log.info("추가된 정정항목 갯수: {}", insertItemRows);
        }

        /**작업내역 로그시작*/
        // elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
        ProcessLogs processLogs = new ProcessLogs();
        processLogs.setEntCode(Util.null2str(carryOutChange.getEntCode()));
        processLogs.setEntBusinessCode(Util.null2str(carryOutChange.getEntBusinessCode()));
        processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
        processLogs.setProcessUserId(Util.null2str(carryOutChange.getRegisterUserId()));
        processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
        processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setDbType(BmsConsts.DbType.BMS_CHANGE_OUT);
        processLogs.setDescription(BmsConsts.DbType.BMS_CHANGE_OUT+" [입력 CHANGE_NO:"+String.valueOf(carryOutChange.getChangeNo())+"]");
        processLogs.setJobNo(String.valueOf(carryOutChange.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
        processLogs.setJobLog(new Gson().toJson(carryOutChange));
        processLogs.setResult("ok");
        processLogs.setEntBusinessPrintName(carryOutChange.getLogUserBusinessPrintName());
        processLogs.setProcessUserName(carryOutChange.getLogUserName());
        processLogs.setProcessUserNameEng(carryOutChange.getLogUserNameEng());
        //	String podStatus = BmsConsts.PodStatus.BMS99;
        String blNo = String.valueOf(carryOutChange.getBlNo());
        // 호출시 필요 항목 : BL_NO, POD_STATUS
        bmsCommService.insertLogAndPodStatus(processLogs, request
                , BmsConsts.PodStatus.INSERT /* pod Status 값 매핑 */
                , ""
                , carryOutChange
        );
        /**작업내역 로그끝*/

        //입력 후 재조회
        SearchOutCorrection searchOutCorrection = new SearchOutCorrection();
        searchOutCorrection.setChangeNo(carryOutChange.getChangeNo());
        searchOutCorrection.setOutNo(carryOutChange.getInoutNo());
        searchOutCorrection.setCustomsReportNo(carryOutChange.getCustomsReportNo());
        CarryOutChange carryOutChangeDetail= bmsOutCorrectionMapper.selectCarryOutChangeDetail(searchOutCorrection);
        carryOutChangeSaveResult.setChangeNo(carryOutChangeDetail.getChangeNo());

        return carryOutChangeSaveResult;
    }

    public CarryOutChangeSaveResult updateCarryOutChange(CarryOutChange carryOutChange, HttpServletRequest request) throws Exception {
        CarryOutChangeSaveResult carryOutChangeSaveResult = new CarryOutChangeSaveResult();
        //차수값 3자리로 처리 (ex: 차수가 1일 경우 001)
        String tmpChangeCount = "00"+carryOutChange.getChangeCount();
        String changeCount = tmpChangeCount.substring(tmpChangeCount.length()-3,tmpChangeCount.length());
        carryOutChange.setChangeCount(changeCount);
        /*int changeNo = bmsOutCorrectionMapper.selectChangeNo(carryOutChange);
        carryOutChange.setChangeNo(changeNo);*/
        //정정정보 업데이트
        bmsOutCorrectionMapper.updateCarryOutChange(carryOutChange);

        //정정항목 리스트 생성
        List<ChangeItem> changeItemList = new ArrayList<>();

        for (Map<String,String> map : carryOutChange.getChangeInfoList()) {
            ChangeItem changeItem = new ChangeItem();
            //changeNo = bmsOutCorrectionMapper.selectChangeNo(carryOutChange);
            changeItem.setChangeNo(carryOutChange.getChangeNo());
            changeItem.setChangeCode(map.get("changeCode"));
            changeItem.setChangeBefore(map.get("changeBefore"));
            changeItem.setChangeAfter(map.get("changeAfter"));
            changeItemList.add(changeItem);
        }
        //log.info("변경항목리스트:::: {}",changeItemList.toString());

        //기존 내역삭제(delete and insert 방식으로 update 처리)
        bmsOutCorrectionMapper.deleteChangeItem(carryOutChange.getChangeNo());

        //변경항목 업데이트
        for (ChangeItem changeItem : changeItemList) {
            int insertItemRows = bmsOutCorrectionMapper.insertChangeItem(changeItem);
            //log.info("추가된 정정항목 갯수: {}", insertItemRows);
        }

        //수정 후 재조회
        SearchOutCorrection searchOutCorrection = new SearchOutCorrection();
        searchOutCorrection.setOutNo(carryOutChange.getInoutNo());
        searchOutCorrection.setCustomsReportNo(carryOutChange.getCustomsReportNo());
        CarryOutChange carryOutChangeDetail= bmsOutCorrectionMapper.selectCarryOutChangeDetail(searchOutCorrection);
        carryOutChangeSaveResult.setChangeNo(carryOutChangeDetail.getChangeNo());

        /**작업내역 로그시작*/
        // elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
        ProcessLogs processLogs = new ProcessLogs();
        processLogs.setEntCode(Util.null2str(carryOutChange.getEntCode()));
        processLogs.setEntBusinessCode(Util.null2str(carryOutChange.getEntBusinessCode()));
        processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
        processLogs.setProcessUserId(Util.null2str(carryOutChange.getRegisterUserId()));
        processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
        processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_CHANGE_OUT);
        processLogs.setDescription(BmsConsts.DbType.BMS_CHANGE_OUT+" [수정 CHANGE_NO:"+String.valueOf(carryOutChange.getChangeNo())+"]");
        processLogs.setJobNo(String.valueOf(carryOutChange.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
        processLogs.setJobLog(new Gson().toJson(carryOutChange));
        processLogs.setResult("ok");
        processLogs.setEntBusinessPrintName(carryOutChange.getLogUserBusinessPrintName());
        processLogs.setProcessUserName(carryOutChange.getLogUserName());
        processLogs.setProcessUserNameEng(carryOutChange.getLogUserNameEng());
        //	String podStatus = BmsConsts.PodStatus.BMS99;
        String blNo = String.valueOf(carryOutChange.getBlNo());
        // 호출시 필요 항목 : BL_NO, POD_STATUS
        bmsCommService.insertLogAndPodStatus(processLogs, request
                , BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
                , ""
                , carryOutChange
        );
        /**작업내역 로그끝*/

        return carryOutChangeSaveResult;
    }
    private static void setSearchCondition(SearchOutCorrection search) throws Exception {
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
        }
    }

    public CarryOutChangeSaveResult deleteCarryOutChange(DeleteCarryOutChange delete, HttpServletRequest request) throws Exception {
        CarryOutChangeSaveResult result = new CarryOutChangeSaveResult();

        List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
        deleteList = delete.getDataList();
        int notDeletedCnt = 0;
        int deletedCnt = 0;

        CarryOutChange carryOutChange;
        SearchOutCorrection search = new SearchOutCorrection();

        for (Map<String, Object> deleteMap : deleteList) {
            search.setChangeNo(Integer.parseInt(Util.null2str(deleteMap.get("changeNo"))));
            carryOutChange = bmsOutCorrectionMapper.selectCarryOutChangeDetail(search);
            if (null != carryOutChange){
                if("BMS01".equals(carryOutChange.getLastPodStatus()) || "BMS02".equals(carryOutChange.getLastPodStatus())
                        || "BMS05".equals(carryOutChange.getLastPodStatus())){

                    //삭제할 반입정정과 변경내역
                    ChangeItem changeItem = new ChangeItem();
                    changeItem.setChangeNo(carryOutChange.getChangeNo());
                    bmsOutCorrectionMapper.deleteCarryOutChange(carryOutChange);
                    bmsOutCorrectionMapper.deleteCarryOutChangeItem(changeItem);

                    //deletedCnt(변수삭제카운트) count++
                    deletedCnt++;

                    /**작업내역 로그시작*/
                    // elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
                    ProcessLogs processLogs = new ProcessLogs();
                    processLogs.setEntCode(Util.null2str(delete.getEntCode()));
                    processLogs.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
                    processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
                    processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
                    processLogs.setLogJobType("DATA");
            		processLogs.setBound("AIR");
            		processLogs.setBoundType("IMP");
                    processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
            		processLogs.setDbType(BmsConsts.DbType.BMS_CHANGE_OUT);
                    processLogs.setDescription(BmsConsts.DbType.BMS_CHANGE_OUT+"[삭제 CHANGE_NO:" + String.valueOf(deleteMap.get("changeNo")) + ", 삭제사유:" + delete.getReason() + "]");
                    processLogs.setJobNo(String.valueOf(deleteMap.get("odJobNo")));
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
                            , delete.getReason()
                            , null
                    );
                    /**작업내역 로그끝*/
                }else {
                    notDeletedCnt++;
                }
            }else {
                notDeletedCnt++;
            }
        }
        result.setDeletedCnt(deletedCnt);
        result.setNotDeletedCnt(notDeletedCnt);
        return result;
    }

	/**
	 * 관세청 XML 송신(Send) - GOVCBR5LD(반출신고정정신청서)
	 * @param params
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CarryOutChangeSaveResult sendOutToXml(BatchBmsOutCorrection params, HttpServletRequest request) throws Exception {
		CarryOutChangeSaveResult result = new CarryOutChangeSaveResult();
		String batchExecType = params.getBatchExecType();

		if("SEND".equals(batchExecType)) {
			result = bmsSendXmlParser.callSendXmlGOVCBR5LDParserExecutor(params, request);
		}

		return result;
	}
}
