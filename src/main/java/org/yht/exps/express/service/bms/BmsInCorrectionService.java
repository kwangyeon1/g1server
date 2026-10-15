package org.yht.exps.express.service.bms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsCommMapper;
import org.yht.exps.express.mapper.bms.BmsInCorrectionMapper;
import org.yht.exps.express.mapper.bms.BmsInMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsInCorrection.BatchBmsInCorrection;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChange;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChangeDetail;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChangeDetailList;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChangeList;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChangeSaveResult;
import org.yht.exps.express.model.bms.bmsInCorrection.ChangeItem;
import org.yht.exps.express.model.bms.bmsInCorrection.DeleteCarryInChange;
import org.yht.exps.express.model.bms.bmsInCorrection.search.SearchInCorrection;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.CarryInList;
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
public class BmsInCorrectionService {
    @Autowired
    BmsInCorrectionMapper bmsInCorrectionMapper;

    @Autowired
    BmsInMapper bmsInMapper;

    @Autowired
    CommonService commonService;

    @Autowired
    BmsCommService bmsCommService;

	@Autowired
	BmsSendXmlParser bmsSendXmlParser;

    public CarryInList selectCarryInList(SearchInCorrection search) throws Exception {
        CarryInList carryInList = new CarryInList();
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
        Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYIN_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO");
        List<CarryIn> list  = bmsInCorrectionMapper.selectCarryInList(search);
        carryInList.setCarryInList(list);
        if(list.size() > 0) {
            carryInList.setTotal(list.get(0).getTotal());
        }else {
            carryInList.setTotal(0);
        }
        return carryInList;
    }

    public CarryInChangeList selectCarryInChangeList(SearchInCorrection search) throws Exception {
        CarryInChangeList carryInChangeList = new CarryInChangeList();
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
        Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYIN_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO");
        List<CarryInChange> list  = bmsInCorrectionMapper.selectCarryInChangeList(search);
        carryInChangeList.setCarryInChangeList(list);
        if(list.size() > 0) {
            carryInChangeList.setTotal(list.get(0).getTotal());
        }else {
            carryInChangeList.setTotal(0);
        }
        return carryInChangeList;
    }

    public CarryInChangeDetailList selectCarryInChangeBySrchNo(SearchInCorrection search) throws Exception {
        CarryInChangeDetailList list = new CarryInChangeDetailList();

        search.setPageSize(50);
        search.setPage(1);
        search.setDefaultSort(search.getSrchNoType());
        setSearchCondition(search);

        Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYIN_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO");
        List<CarryInChange> carryInChangeList = bmsInCorrectionMapper.selectCarryInChangeList(search);
        if(carryInChangeList.size() > 0) {
            list.setTotalCarryInChange(carryInChangeList.get(0).getTotal());
        }else {
            list.setTotalCarryInChange(0);
        }
        list.setCarryInChangeList(carryInChangeList);

        Util.accessControlDatabase(search, "srchDateFormat|userLanguage", true, "srchDateGbn", "|CARRYIN_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO");
        List<CarryIn> carryInList  = bmsInCorrectionMapper.selectCarryInList(search);
        if(carryInList.size() > 0) {
            list.setTotal(carryInList.get(0).getTotal());
        }else {
            list.setTotal(0);
        }
        list.setCarryInList(carryInList);

        return list;
    }

    private static void setSearchCondition(SearchInCorrection search) throws Exception {
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

    public CarryInChange selectCarryInChangeDetail(SearchInCorrection search) {
        CarryInChange carryInChange = bmsInCorrectionMapper.selectCarryInChangeDetail(search);
        return carryInChange;
    }

    public CarryInChangeDetail selectCarryInAndCarryInChangeDetail(SearchInCorrection search) {
        CarryInChangeDetail detail = new CarryInChangeDetail();
        CarryIn carryIn = null;
        CarryInChange carryInChange = null;

        if("CARRY_IN_CHANGE".equals(search.getSrchCarryInState())) {  //CARRY_IN, CARRY_IN_CHANGE
            carryInChange  = bmsInCorrectionMapper.selectCarryInChangeDetail(search);
            List<ChangeItem> changeItemList = bmsInCorrectionMapper.selectChangeItemListByChangeNo(search);
            carryInChange.setChangeItemList(changeItemList); //반입정정정보는 여러건의 정정항목을 가짐
            log.info("정정번호:{}",search.getChangeNo());
            search.setInNo(carryInChange.getInoutNo());
            search.setSrchInNo(carryInChange.getInoutNo());
            carryIn =  bmsInCorrectionMapper.selectCarryInDetail(search); // 상세 정보 조회
        }else {
            carryInChange    = bmsInCorrectionMapper.selectCarryInChangeDetail(search);
            if(carryInChange != null) {
                search.setChangeNo(carryInChange.getChangeNo());
                List<ChangeItem> changeItemList = bmsInCorrectionMapper.selectChangeItemListByChangeNo(search);
                carryInChange.setChangeItemList(changeItemList); //반입정정정보는 여러건의 정정항목을 가짐
                //log.info("정정항목아이템: {}", changeItemList.toString());
            }
            carryIn =  bmsInCorrectionMapper.selectCarryInDetail(search);
        }

        //반입정정된 항목 갯수
        int changeItemCount = bmsInCorrectionMapper.selectChangeItemListByChangeNo(search).size();

        detail.setCarryInChange(carryInChange);
        detail.setCarryIn(carryIn);

        detail.setSrchCarryInState(search.getSrchCarryInState());
        detail.setSrchChangeNo(search.getSrchChangeNo());
        detail.setSrchInNo(search.getSrchInNo());

        detail.setChangeItemCount(changeItemCount);
        return detail;
    }

    public CarryInChangeSaveResult insertCarryInChange(CarryInChange carryInChange, HttpServletRequest request) throws Exception {
        CarryInChangeSaveResult carryInChangeSaveResult = new CarryInChangeSaveResult();
        //차수값 3자리로 처리 (ex: 차수가 1일 경우 001)
        String tmpChangeCount = "00"+carryInChange.getChangeCount();
        String changeCount = tmpChangeCount.substring(tmpChangeCount.length()-3,tmpChangeCount.length());
        carryInChange.setChangeCount(changeCount);

        int row = bmsInCorrectionMapper.insertCarryInChange(carryInChange);

        //정정항목 insert
        List<ChangeItem> changeItemList = new ArrayList<>();

        for (Map<String,String> map : carryInChange.getChangeInfoList()) {
            ChangeItem changeItem = new ChangeItem();
            changeItem.setChangeNo(carryInChange.getChangeNo());
            changeItem.setChangeCode(map.get("changeCode"));
            changeItem.setChangeBefore(map.get("changeBefore"));
            changeItem.setChangeAfter(map.get("changeAfter"));
            changeItemList.add(changeItem);
        }
        //log.info("변경항목리스트:::: {}",changeItemList.toString());

        //변경항목 입력
        for (ChangeItem changeItem : changeItemList) {
            int insertItemRows = bmsInCorrectionMapper.insertChangeItem(changeItem);
            //log.info("추가된 정정항목 갯수: {}", insertItemRows);
        }

        /**작업내역 로그시작*/
        // elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
        ProcessLogs processLogs = new ProcessLogs();
        processLogs.setEntCode(Util.null2str(carryInChange.getEntCode()));
        processLogs.setEntBusinessCode(Util.null2str(carryInChange.getEntBusinessCode()));
        processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
        processLogs.setProcessUserId(Util.null2str(carryInChange.getRegisterUserId()));
        processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
        processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
		processLogs.setDbType(BmsConsts.DbType.BMS_CHANGE_IN);
        processLogs.setDescription(BmsConsts.DbType.BMS_CHANGE_IN+" [입력 CHANGE_NO:"+String.valueOf(carryInChange.getChangeNo())+"]");
        processLogs.setJobNo(String.valueOf(carryInChange.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
        processLogs.setJobLog(new Gson().toJson(carryInChange));
        processLogs.setResult("ok");
        processLogs.setEntBusinessPrintName(carryInChange.getLogUserBusinessPrintName());
        processLogs.setProcessUserName(carryInChange.getLogUserName());
        processLogs.setProcessUserNameEng(carryInChange.getLogUserNameEng());
        //	String podStatus = BmsConsts.PodStatus.BMS99;
        String blNo = String.valueOf(carryInChange.getBlNo());
        // 호출시 필요 항목 : BL_NO, POD_STATUS
        bmsCommService.insertLogAndPodStatus(processLogs, request
                , BmsConsts.PodStatus.INSERT /* pod Status 값 매핑 */
                , ""
                , carryInChange
        );
        /**작업내역 로그끝*/

        //입력 후 재조회
        SearchInCorrection searchInCorrection = new SearchInCorrection();
        searchInCorrection.setInNo(carryInChange.getInoutNo());
        searchInCorrection.setCustomsReportNo(carryInChange.getCustomsReportNo());
        CarryInChange carryInChangeDetail= bmsInCorrectionMapper.selectCarryInChangeDetail(searchInCorrection);
        carryInChangeSaveResult.setChangeNo(carryInChangeDetail.getChangeNo());

        return carryInChangeSaveResult;
    }

    public CarryInChangeSaveResult updateCarryInChange(CarryInChange carryInChange, HttpServletRequest request) throws Exception {
        CarryInChangeSaveResult carryInChangeSaveResult = new CarryInChangeSaveResult();
        //차수값 3자리로 처리 (ex: 차수가 1일 경우 001)
        String tmpChangeCount = "00"+carryInChange.getChangeCount();
        String changeCount = tmpChangeCount.substring(tmpChangeCount.length()-3,tmpChangeCount.length());
        carryInChange.setChangeCount(changeCount);
        /*int changeNo = bmsInCorrectionMapper.selectChangeNo(carryInChange);
        carryInChange.setChangeNo(changeNo);*/
        //정정정보 업데이트
        bmsInCorrectionMapper.updateCarryInChange(carryInChange);

        //정정항목 리스트 생성
        List<ChangeItem> changeItemList = new ArrayList<>();

        for (Map<String,String> map : carryInChange.getChangeInfoList()) {
            ChangeItem changeItem = new ChangeItem();
            //changeNo = bmsInCorrectionMapper.selectChangeNo(carryInChange);
            changeItem.setChangeNo(carryInChange.getChangeNo());
            changeItem.setChangeCode(map.get("changeCode"));
            changeItem.setChangeBefore(map.get("changeBefore"));
            changeItem.setChangeAfter(map.get("changeAfter"));
            changeItemList.add(changeItem);
        }

        //기존 내역삭제(delete and insert 방식으로 update 처리)
        bmsInCorrectionMapper.deleteChangeItem(carryInChange.getChangeNo());

        //변경항목 입력
        for (ChangeItem changeItem : changeItemList) {
            int insertItemRows = bmsInCorrectionMapper.insertChangeItem(changeItem);
            //log.info("추가된 정정항목 갯수: {}", insertItemRows);
        }

        //수정 후 재조회
        SearchInCorrection searchInCorrection = new SearchInCorrection();
        searchInCorrection.setInNo(carryInChange.getInoutNo());
        searchInCorrection.setCustomsReportNo(carryInChange.getCustomsReportNo());
        CarryInChange carryInChangeDetail= bmsInCorrectionMapper.selectCarryInChangeDetail(searchInCorrection);
        carryInChangeSaveResult.setChangeNo(carryInChangeDetail.getChangeNo());

        /**작업내역 로그시작*/
        // elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
        ProcessLogs processLogs = new ProcessLogs();
        processLogs.setEntCode(Util.null2str(carryInChange.getEntCode()));
        processLogs.setEntBusinessCode(Util.null2str(carryInChange.getEntBusinessCode()));
        processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
        processLogs.setProcessUserId(Util.null2str(carryInChange.getRegisterUserId()));
        processLogs.setLogJobType("DATA");
		processLogs.setBound("AIR");
		processLogs.setBoundType("IMP");
        processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_CHANGE_IN);
        processLogs.setDescription(BmsConsts.DbType.BMS_CHANGE_IN+" [수정 CHANGE_NO:"+String.valueOf(carryInChange.getChangeNo())+"]");
        processLogs.setJobNo(String.valueOf(carryInChange.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
        processLogs.setJobLog(new Gson().toJson(carryInChange));
        processLogs.setResult("ok");
        processLogs.setEntBusinessPrintName(carryInChange.getLogUserBusinessPrintName());
        processLogs.setProcessUserName(carryInChange.getLogUserName());
        processLogs.setProcessUserNameEng(carryInChange.getLogUserNameEng());
        //	String podStatus = BmsConsts.PodStatus.BMS99;
        String blNo = String.valueOf(carryInChange.getBlNo());
        // 호출시 필요 항목 : BL_NO, POD_STATUS
        bmsCommService.insertLogAndPodStatus(processLogs, request
                , BmsConsts.PodStatus.UPDATE /* pod Status 값 매핑 */
                , ""
                , carryInChange
        );
        /**작업내역 로그끝*/

        return carryInChangeSaveResult;
    }

    public CarryInChangeSaveResult deleteCarryInChange(DeleteCarryInChange delete, HttpServletRequest request) throws Exception {
        CarryInChangeSaveResult result = new CarryInChangeSaveResult();

        List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
        deleteList = delete.getDataList();
        int notDeletedCnt = 0;
        int deletedCnt = 0;

        CarryInChange carryInChange;
        SearchInCorrection search = new SearchInCorrection();

        for (Map<String, Object> deleteMap : deleteList) {
            search.setChangeNo(Integer.parseInt(Util.null2str(deleteMap.get("changeNo"))));
            carryInChange = bmsInCorrectionMapper.selectCarryInChangeDetail(search);
            if (null != carryInChange){
                if("BMS01".equals(carryInChange.getLastPodStatus()) || "BMS02".equals(carryInChange.getLastPodStatus())
                        || "BMS05".equals(carryInChange.getLastPodStatus())){

                    //삭제할 반입정정과 변경내역
                    ChangeItem changeItem = new ChangeItem();
                    changeItem.setChangeNo(carryInChange.getChangeNo());
                    bmsInCorrectionMapper.deleteCarryInChange(carryInChange);
                    bmsInCorrectionMapper.deleteCarryInChangeItem(changeItem);

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
            		processLogs.setDbType(BmsConsts.DbType.BMS_CHANGE_IN);
                    processLogs.setDescription(BmsConsts.DbType.BMS_CHANGE_IN+"[삭제 CHANGE_NO:" + String.valueOf(deleteMap.get("changeNo")) + ", 삭제사유:" + delete.getReason() + "]");
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

    public String selectNotificationMsg(String customsReportNo, String changeCount, String lastPodStatus) {
        String notificationMsg = bmsInCorrectionMapper.selectNotificationMsg(customsReportNo, changeCount, lastPodStatus);
        return StringEscapeUtils.escapeHtml4(notificationMsg);
    }

	/**
	 * 관세청 XML 송신(Send) - GOVCBR5LC(반입신고정정신청서)
	 * @param params
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CarryInChangeSaveResult sendOutToXml(BatchBmsInCorrection params, HttpServletRequest request) throws Exception {
		CarryInChangeSaveResult result = new CarryInChangeSaveResult();
		String batchExecType = params.getBatchExecType();

		if("SEND".equals(batchExecType)) {
			result = bmsSendXmlParser.callSendXmlGOVCBR5LCParserExecutor(params, request);
		}

		return result;
	}
}
