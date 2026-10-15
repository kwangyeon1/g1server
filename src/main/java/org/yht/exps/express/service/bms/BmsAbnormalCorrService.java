package org.yht.exps.express.service.bms;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsAbnormalCorrMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsAbnormal.*;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.*;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.search.SearchCarryInAbnormalCorr;
import org.yht.exps.express.model.search.Sort;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.bms.send.BmsSendXmlParser;
import org.yht.util.Util;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class BmsAbnormalCorrService {

    @Autowired
    BmsAbnormalCorrMapper bmsAbnormalCorrMapper;

    @Autowired
    CommonService commonService;

    @Autowired
    BmsCommService bmsCommService;
    @Autowired
    BmsSendXmlParser bmsSendXmlParser;

    public CarryInAbnormalCorrList selectAbnormalCorrList(SearchCarryInAbnormalCorr search) throws Exception {
        CarryInAbnormalCorrList carryInAbnormalCorrList = new CarryInAbnormalCorrList();
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
        
        Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchEtcType", true, "srchDateGbn", "|ABN_REQ_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|CB.BL_NO|CB.CARGO_REFERENCE_NO|A.MBL_NO");
        List<CarryInAbnormalCorr> list  = bmsAbnormalCorrMapper.selectAbnormalCorrList(search);
        carryInAbnormalCorrList.setCarryInAbnormalCorrList(list);
        if(list.size() > 0) {
            carryInAbnormalCorrList.setTotal(list.get(0).getTotal());
        }else {
            carryInAbnormalCorrList.setTotal(0);
        }
        return carryInAbnormalCorrList;
    }

    public CarryInAbnormalCorrSaveResult deleteAbnormalCorr(DeleteCarryInAbnormalCorr delete, HttpServletRequest request) throws Exception {
        CarryInAbnormalCorrSaveResult result = new CarryInAbnormalCorrSaveResult();
        List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
        deleteList = delete.getDataList();
        int notDeletedCnt = 0;
        int deletedCnt = 0;

        CarryInAbnormalCorr carryInAbnormalCorr;
        SearchCarryInAbnormalCorr search = new SearchCarryInAbnormalCorr();

        for (Map<String, Object> deleteMap : deleteList) {
            search.setEntCode(Util.null2str(delete.getEntCode()));
            search.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
            search.setAbnChangeNo((Util.null2str(deleteMap.get("abnChangeNo"))));
            search.setMblNo((Util.null2str(deleteMap.get("mblNo"))));
            search.setArDate((Util.null2str(deleteMap.get("arDate"))));
            carryInAbnormalCorr = bmsAbnormalCorrMapper.selectDetailAbnormalCorr(search);
            if (null != carryInAbnormalCorr){
                if("BMS01".equals(carryInAbnormalCorr.getLastPodStatus()) || "BMS02".equals(carryInAbnormalCorr.getLastPodStatus())
                        || "BMS05".equals(carryInAbnormalCorr.getLastPodStatus())){

                    search.setDefaultSort("ABN_CHANGE_NO");
                    Util.accessControlDatabase(search, null, true, null, null, null, null);
                    List<CarryInAbnormalHblCorr> blList = bmsAbnormalCorrMapper.selectAbnormalHblCorrList(search);
                    for (CarryInAbnormalHblCorr carryInAbnormalHblCorr: blList) {
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
    					processLogs.setDbType(BmsConsts.DbType.BMS_ABNORMAL_CORR);
                        processLogs.setDescription(BmsConsts.DbType.BMS_ABNORMAL_CORR+"[삭제 BL_NO:" + Util.null2str(carryInAbnormalHblCorr.getBlNo()) + ", 삭제사유:" + delete.getReason() + "]");
                        processLogs.setJobNo(Util.null2str(carryInAbnormalHblCorr.getOdJobNo()));
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
                    }
                    //TB_BMS_ABNOAMAL , TB_BMS_ABNORMAL_BL 을 동시에 지움 (MBL : BL = 1 : N )
                    int resultCnt =bmsAbnormalCorrMapper.deleteAbnormalCorr(carryInAbnormalCorr.getAbnChangeNo());
                    bmsAbnormalCorrMapper.deleteAbnormalHBLCorr(carryInAbnormalCorr.getAbnChangeNo());

                    //deletedCnt(변수삭제카운트) count++
                    deletedCnt++;

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

    public CarryInAbnormalCorrDetailList checkExistAbnormal(SearchCarryInAbnormalCorr search) {
        CarryInAbnormalCorrDetailList carryInAbnormalCorrDetailList = new CarryInAbnormalCorrDetailList();
        if(search.getSrchAbnormalState().equals("ABNORMAL")) {
            int abnormalCnt = bmsAbnormalCorrMapper.checkExistAbnormal(search);
            int abnormalCorrCnt = bmsAbnormalCorrMapper.checkExistAbnormalCorr(search);

            //반입이상에서 더블클릭했지만 반입이상정정 테이블에 데이터가 존재하고 정정상태가 입력,오류인 경우는 정정데이터(TB_BMS_ABNORMAL_CHANGE)를 엔트리 화면에 반영한다.
            if(abnormalCorrCnt > 0 ) {
                //이상보고상태(LAST_POD_STATUS) 값 을 가져와서 진행상태값에 따라 다르게 처리
                CarryInAbnormalCorr carryInAbnormalCorr = bmsAbnormalCorrMapper.selectDetailAbnormalCorr(search);
                String lastPodStatus = carryInAbnormalCorr.getLastPodStatus();
                log.info("이상보고상태: {}",lastPodStatus);
                if(lastPodStatus.equals("BMS01") || lastPodStatus.equals("BMS05")) {
                    carryInAbnormalCorrDetailList.setTotalAbnormalCorr(abnormalCorrCnt);
                }else { //반입이상정정데이터가 존재하지만, 정정상태가 입력, 오류가 아닌경우는 정정차수를 1 올려준다.
                    carryInAbnormalCorrDetailList.setTotalAbnormalCorr(abnormalCorrCnt);
                    carryInAbnormalCorrDetailList.setIsCorrected(1);
                }
            }else { //반입정정데이터가 없는 경우는 반입이상데이터(TB_BMS_ABNORMAL)를 엔트리 화면에 반영
                carryInAbnormalCorrDetailList.setTotalAbnormalCorr(0);
            }

            log.info("반입이상갯수: {}",abnormalCnt);
            log.info("반입이상정정갯수: {}",abnormalCorrCnt);
            carryInAbnormalCorrDetailList.setTotalAbnormal(abnormalCnt);
        }
        if(search.getSrchAbnormalState().equals("ABNORMAL_CORR")) {
            int abnormalCorrCnt = bmsAbnormalCorrMapper.checkExistAbnormalCorr(search);
            log.info("반입이상정정(CHANGE)갯수: {}",abnormalCorrCnt);
            carryInAbnormalCorrDetailList.setTotalAbnormalCorr(abnormalCorrCnt);
        }
        return carryInAbnormalCorrDetailList;
    }

    public CarryInAbnormalCorrSaveResult insertAbnormalCorr(CarryInAbnormalCorr carryInAbnormalCorr, HttpServletRequest request) throws Exception {
        CarryInAbnormalCorrSaveResult carryInAbnormalCorrSaveResult = new CarryInAbnormalCorrSaveResult();

        //TB_BMS_ABNORMAL_CHANGE 에 CarryInAbnormalCorr(MASTER입항정보) 데이터 입력
        String logVal = "";
        int intRow = bmsAbnormalCorrMapper.insertAbnormalCorr(carryInAbnormalCorr);
        int abnChangeNo = bmsAbnormalCorrMapper.selectAbnChangeNo();
        log.info("생성된반입이상정정번호: {}",abnChangeNo);
        logVal = "반입이상정정::: " + carryInAbnormalCorr.getGridData();
        log.info(logVal);
        String accidentClass = Util.null2str(carryInAbnormalCorr.getAccidentClass());
        String rejectReason = Util.null2str(carryInAbnormalCorr.getRejectReason());
        logVal = "사고구분::: " + accidentClass;
        log.info(logVal);
        logVal = "이상이유::: " + rejectReason;
        log.info(logVal);
        //반입이상 HOUSE LIST 정보 입력(g1Grid)
        List<CarryInAbnormalHblCorr> carryInAbnormalHblCorrList = new ArrayList<>();
        for (Map<String,String> map : carryInAbnormalCorr.getGridData()) {
            CarryInAbnormalHblCorr carryInAbnormalHblCorr = new CarryInAbnormalHblCorr();
            carryInAbnormalHblCorr.setAbnChangeNo(abnChangeNo);
            carryInAbnormalHblCorr.setCargoReferenceNo(map.get("cargoReferenceNo"));
            carryInAbnormalHblCorr.setOdJobNo(map.get("odJobNo"));
            carryInAbnormalHblCorr.setChangeClass(map.get("changeClass"));
            carryInAbnormalHblCorr.setHsn(map.get("hsn"));
            carryInAbnormalHblCorr.setBlNo(map.get("blNo"));
            carryInAbnormalHblCorr.setPkgType(map.get("pkgType"));
            carryInAbnormalHblCorr.setExCt(Integer.parseInt(map.get("exCt")));
            carryInAbnormalHblCorr.setExWt(Double.parseDouble(map.get("exWt")));
            carryInAbnormalHblCorr.setCt(Integer.parseInt(map.get("ct")));
            carryInAbnormalHblCorr.setWt(Double.parseDouble(map.get("wt")));
            carryInAbnormalHblCorr.setAccidentClass(accidentClass);
            carryInAbnormalHblCorr.setAccidentCt(Integer.parseInt(map.get("accidentCt")));
            carryInAbnormalHblCorr.setAccidentWt(Double.parseDouble(map.get("accidentWt")));
            carryInAbnormalHblCorr.setRejectReason(rejectReason);
            carryInAbnormalHblCorr.setRegisterUserId(carryInAbnormalCorr.getRegisterUserId());
            carryInAbnormalHblCorr.setUpdateUserId(carryInAbnormalCorr.getUpdateUserId());
            String logVal2 = "저장할HBLCorr- : " + carryInAbnormalHblCorr;
            log.info(logVal2);
            carryInAbnormalHblCorrList.add(carryInAbnormalHblCorr);
        }

        for (CarryInAbnormalHblCorr carryInAbnormalHblCorr : carryInAbnormalHblCorrList) {
            bmsAbnormalCorrMapper.insertAbnormalHblCorr(carryInAbnormalHblCorr);

            /**작업내역 로그시작 (BL기준으로 로그 남김)*/
            // elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
            ProcessLogs processLogs = new ProcessLogs();
            processLogs.setEntCode(Util.null2str(carryInAbnormalCorr.getEntCode()));
            processLogs.setEntBusinessCode(Util.null2str(carryInAbnormalCorr.getEntBusinessCode()));
            processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
            processLogs.setProcessUserId(Util.null2str(carryInAbnormalHblCorr.getRegisterUserId()));
            processLogs.setLogJobType("DATA");
    		processLogs.setBound("AIR");
    		processLogs.setBoundType("IMP");
            processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
			processLogs.setDbType(BmsConsts.DbType.BMS_ABNORMAL_CORR);
            processLogs.setDescription(BmsConsts.DbType.BMS_ABNORMAL_CORR+" [입력 BL_NO:"+String.valueOf(carryInAbnormalHblCorr.getBlNo())+"]");
            processLogs.setJobNo(String.valueOf(carryInAbnormalHblCorr.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
            processLogs.setJobLog(new Gson().toJson(carryInAbnormalHblCorr));
            processLogs.setResult("ok");
            processLogs.setEntBusinessPrintName(carryInAbnormalHblCorr.getLogUserBusinessPrintName());
            processLogs.setProcessUserName(carryInAbnormalHblCorr.getLogUserName());
            processLogs.setProcessUserNameEng(carryInAbnormalHblCorr.getLogUserNameEng());
            //	String podStatus = BmsConsts.PodStatus.BMS99;
            String blNo = String.valueOf(carryInAbnormalHblCorr.getBlNo());
            // 호출시 필요 항목 : BL_NO, POD_STATUS
            bmsCommService.insertLogAndPodStatus(processLogs, request
                    , BmsConsts.PodStatus.INSERT
                    , ""
                    , carryInAbnormalHblCorr
            );
            /**작업내역 로그끝*/
        }
        carryInAbnormalCorrSaveResult.setResultCode("A012051");// 저장 되었습니다.
        carryInAbnormalCorrSaveResult.setAbnChangeNo(abnChangeNo);
        return carryInAbnormalCorrSaveResult;
    }

    public CarryInAbnormalCorrSaveResult updateAbnormalCorr(CarryInAbnormalCorr carryInAbnormalCorr, HttpServletRequest request) throws Exception {
        CarryInAbnormalCorrSaveResult carryInAbnormalCorrSaveResult = new CarryInAbnormalCorrSaveResult();
        int updateCnt = bmsAbnormalCorrMapper.updateAbnormalCorr(carryInAbnormalCorr);
        int abnChangeNo = carryInAbnormalCorr.getAbnChangeNo();
        String logVal = "";
        
        logVal = "반입이상정정그리드 데이터::: " + carryInAbnormalCorr.getGridData();
        log.info(logVal);
        String accidentClass = Util.null2str(carryInAbnormalCorr.getAccidentClass());
        String rejectReason = Util.null2str(carryInAbnormalCorr.getRejectReason());
        logVal = "사고구분::: " + accidentClass;
        log.info(logVal);
        logVal = "이상이유::: " + rejectReason;
        log.info(logVal);
        //반입이상정정 HOUSE LIST 정보 입력(g2Grid)
        List<CarryInAbnormalHblCorr> carryInAbnormalHblCorrList = new ArrayList<>();
        for (Map<String,String> map : carryInAbnormalCorr.getGridData()) {
            CarryInAbnormalHblCorr carryInAbnormalHblCorr = new CarryInAbnormalHblCorr();
            carryInAbnormalHblCorr.setAbnChangeNo(abnChangeNo);
            carryInAbnormalHblCorr.setCargoReferenceNo(map.get("cargoReferenceNo"));
            carryInAbnormalHblCorr.setOdJobNo(map.get("odJobNo"));
            carryInAbnormalHblCorr.setChangeClass(map.get("changeClass"));
            carryInAbnormalHblCorr.setHsn(map.get("hsn"));
            carryInAbnormalHblCorr.setBlNo(map.get("blNo"));
            carryInAbnormalHblCorr.setPkgType(map.get("pkgType"));
            carryInAbnormalHblCorr.setExCt(Integer.parseInt(map.get("exCt")));
            carryInAbnormalHblCorr.setExWt(Double.parseDouble(map.get("exWt")));
            carryInAbnormalHblCorr.setCt(Integer.parseInt(map.get("ct")));
            carryInAbnormalHblCorr.setWt(Double.parseDouble(map.get("wt")));
            carryInAbnormalHblCorr.setAccidentClass(accidentClass);
            carryInAbnormalHblCorr.setAccidentCt(Integer.parseInt(map.get("accidentCt")));
            carryInAbnormalHblCorr.setAccidentWt(Double.parseDouble(map.get("accidentWt")));
            carryInAbnormalHblCorr.setRejectReason(rejectReason);
            carryInAbnormalHblCorr.setRegisterUserId(carryInAbnormalCorr.getRegisterUserId());
            carryInAbnormalHblCorr.setUpdateUserId(carryInAbnormalCorr.getUpdateUserId());
            String logVal2 = "저장할CorrHBL- " + carryInAbnormalHblCorr;
            log.info(logVal2);
            carryInAbnormalHblCorrList.add(carryInAbnormalHblCorr);
        }
        bmsAbnormalCorrMapper.deleteAbnormalHBLCorr(abnChangeNo);
        for (CarryInAbnormalHblCorr carryInAbnormalHblCorr : carryInAbnormalHblCorrList) {
            bmsAbnormalCorrMapper.insertAbnormalHblCorr(carryInAbnormalHblCorr);

            /**작업내역 로그시작 (BL기준으로 로그 남김)*/
            // elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
            ProcessLogs processLogs = new ProcessLogs();
            processLogs.setEntCode(Util.null2str(carryInAbnormalCorr.getEntCode()));
            processLogs.setEntBusinessCode(Util.null2str(carryInAbnormalCorr.getEntBusinessCode()));
            processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
            processLogs.setProcessUserId(Util.null2str(carryInAbnormalHblCorr.getRegisterUserId()));
            processLogs.setLogJobType("DATA");
    		processLogs.setBound("AIR");
    		processLogs.setBoundType("IMP");
            processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_ABNORMAL_CORR);
            processLogs.setDescription(BmsConsts.DbType.BMS_ABNORMAL_CORR+" [수정 BL_NO:"+String.valueOf(carryInAbnormalHblCorr.getBlNo())+"]");
            processLogs.setJobNo(String.valueOf(carryInAbnormalHblCorr.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
            processLogs.setJobLog(new Gson().toJson(carryInAbnormalHblCorr));
            processLogs.setResult("ok");
            processLogs.setEntBusinessPrintName(carryInAbnormalHblCorr.getLogUserBusinessPrintName());
            processLogs.setProcessUserName(carryInAbnormalHblCorr.getLogUserName());
            processLogs.setProcessUserNameEng(carryInAbnormalHblCorr.getLogUserNameEng());
            //	String podStatus = BmsConsts.PodStatus.BMS99;
            String blNo = String.valueOf(carryInAbnormalHblCorr.getBlNo());
            // 호출시 필요 항목 : BL_NO, POD_STATUS
            bmsCommService.insertLogAndPodStatus(processLogs, request
                    , BmsConsts.PodStatus.UPDATE
                    , ""
                    , carryInAbnormalHblCorr
            );
            /**작업내역 로그끝*/
        }

        carryInAbnormalCorrSaveResult.setResultCode("A012051");// 저장 되었습니다.
        carryInAbnormalCorrSaveResult.setAbnChangeNo(abnChangeNo);
        return carryInAbnormalCorrSaveResult;
    }

    public CarryInAbnormalCorr selectDetailAbnormalCorr(SearchCarryInAbnormalCorr search) {
        CarryInAbnormalCorr carryInAbnormalCorr = bmsAbnormalCorrMapper.selectDetailAbnormalCorr(search);
        return carryInAbnormalCorr;
    }

    public CarryInAbnormalCorrDetailList selectAbnormalHblCorrList(SearchCarryInAbnormalCorr search) throws Exception {
        CarryInAbnormalCorrDetailList list = new CarryInAbnormalCorrDetailList();
        Util.accessControlDatabase(search, null, true, null, null, null, null);
        List<CarryInAbnormalHblCorr> carryInAbnormalHblCorrList = bmsAbnormalCorrMapper.selectAbnormalHblCorrList(search);
        search.setPageSize(50);
        search.setPage(1);
        search.setDefaultSort(search.getSrchNoType());
        setSearchCondition(search);

        list.setCarryInAbnormalHblCorrList(carryInAbnormalHblCorrList);
        return list;
    }

    public CarryInAbnormalCorrDetailList selectAbnormalCorrListPopup(SearchCarryInAbnormalCorr search) throws Exception {
        CarryInAbnormalCorrDetailList list = new CarryInAbnormalCorrDetailList();

        search.setPageSize(50);
        search.setPage(1);
        search.setDefaultSort(search.getSrchNoType());
        setSearchCondition(search);
        
        // selectAbnormalCorrList, selectAbnormalList 파라미터 검증
        Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchEtcType", true, "srchDateGbn", "|ABN_REQ_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|MBL_NO");  
        List<CarryInAbnormalCorr> carryInAbnormalCorrList = bmsAbnormalCorrMapper.selectAbnormalCorrList(search);
        if(carryInAbnormalCorrList.size() > 0) {
            list.setTotalAbnormalCorr(carryInAbnormalCorrList.get(0).getTotal());
        }else {
            list.setTotalAbnormalCorr(0);
        }

        List<CarryInAbnormal> carryInAbnormalList = bmsAbnormalCorrMapper.selectAbnormalList(search);
        if(carryInAbnormalList.size() > 0) {
            list.setTotalAbnormal(carryInAbnormalList.get(0).getTotal());
        }else {
            list.setTotalAbnormal(0);
        }

        list.setCarryInAbnormalCorrList(carryInAbnormalCorrList);
        list.setCarryInAbnormalList(carryInAbnormalList);
        log.info("detail Abnormals:: {}",list);
        return list;
    }

    private static void setSearchCondition(SearchCarryInAbnormalCorr search) throws Exception {
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

    /** 정정차수 가져오기*/
    public int selectMaxChangeCount(SearchCarryInAbnormalCorr search) {
        int changeCount = 0;
        changeCount =  bmsAbnormalCorrMapper.selectMaxChangeCount(search);
       return changeCount;
    }

    /**
     * 관세청 XML 송신(Send) - GOVCBRDKP(특송화물 반입물품 이상보고 정정신청서)
     * @param params
     * @param request
     * @return
     */
	public CarryInAbnormalCorrSaveResult sendOutToXml(BatchBmsAbnormalCorr params, HttpServletRequest request) {
		CarryInAbnormalCorrSaveResult result = new CarryInAbnormalCorrSaveResult();
		String batchExecType = params.getBatchExecType();

		String entCode = params.getEntCode();
		String entBusinessCode = params.getEntBusinessCode();
		List<Integer> abnChangeNoList = new ArrayList<Integer>();
		List<CarryInAbnormalCorr> dataList = params.getCarryInAbnormalCorrList();

		if("SEND".equals(batchExecType)) {
			for(CarryInAbnormalCorr data : dataList) {
				abnChangeNoList.add(data.getAbnChangeNo());
			}

			result = bmsSendXmlParser.callSendXmlGOVCBRDKPParserExecutor(entCode, entBusinessCode, abnChangeNoList);
		}

		return result;
	}

    public CarryInAbnormalList selectAbnormalList(SearchCarryInAbnormalCorr search) throws Exception {
        CarryInAbnormalList carryInAbnormalList = new CarryInAbnormalList();
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
        Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchEtcType", true, "srchDateGbn", "|ABN_REQ_DATE|CHANGE_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|MBL_NO");  
        List<CarryInAbnormal> list  = bmsAbnormalCorrMapper.selectAbnormalList(search);
        carryInAbnormalList.setCarryInAbnormalList(list);
        if(list.size() > 0) {
            carryInAbnormalList.setTotal(list.get(0).getTotal());
        }else {
            carryInAbnormalList.setTotal(0);
        }
        return carryInAbnormalList;
    }
}
