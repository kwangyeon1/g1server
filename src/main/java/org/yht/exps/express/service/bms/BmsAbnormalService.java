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
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsAbnormalMapper;
import org.yht.exps.express.mapper.bms.BmsInMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsAbnormal.AbnormalSpCallVo;
import org.yht.exps.express.model.bms.bmsAbnormal.BatchBmsAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalDetailList;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalHbl;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalList;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalSaveResult;
import org.yht.exps.express.model.bms.bmsAbnormal.DeleteCarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormal.search.SearchCarryInAbnormal;
import org.yht.exps.express.model.bms.carryin.InPlan;
import org.yht.exps.express.model.bms.carryin.InPlanList;
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
public class BmsAbnormalService {

    @Autowired
    BmsAbnormalMapper bmsAbnormalMapper;
    
    @Autowired
	BmsInMapper bmsInMapper;

    @Autowired
    CommonMapper commonMapper;

    @Autowired
    CommonService commonService;

    @Autowired
    BmsCommService bmsCommService;

    @Autowired
    BmsSendXmlParser bmsSendXmlParser;

    /**
     * 반입이상 조회
     * CarryInAbnormalList
     * @param search
     * @return
     * @throws Exception
     */
    public CarryInAbnormalList selectAbnormalList(SearchCarryInAbnormal search) throws Exception {
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
        Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchEtcType", true, "srchDateGbn", "|ABN_REQ_DATE|AR_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|MBL_NO");
        List<CarryInAbnormal> list  = bmsAbnormalMapper.selectAbnormalList(search);
        carryInAbnormalList.setCarryInAbnormalList(list);
        if(list.size() > 0) {
            carryInAbnormalList.setTotal(list.get(0).getTotal());
        }else {
            carryInAbnormalList.setTotal(0);
        }
        return carryInAbnormalList;
    }

    /**
     * 반입예정 조회
     * InPlanList
     * @param search
     * @return
     * @throws Exception
     */
    public InPlanList selectInPlanList(SearchCarryInAbnormal search) throws Exception {
        InPlanList inPlanList = new InPlanList();
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
        Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchEtcType", true, "srchDateGbn", "|ABN_REQ_DATE|AR_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|MBL_NO");
        List<InPlan> list  = bmsAbnormalMapper.selectInPlanList(search);
        inPlanList.setInPlanList(list);
        if(list.size() > 0) {
            inPlanList.setTotal(list.get(0).getTotal());
        }else {
            inPlanList.setTotal(0);
        }
        return inPlanList;
    }

    public CarryInAbnormalSaveResult deleteAbnormal(DeleteCarryInAbnormal delete, HttpServletRequest request) throws Exception {
        CarryInAbnormalSaveResult result = new CarryInAbnormalSaveResult();
        List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
        deleteList = delete.getDataList();
        int notDeletedCnt = 0;
        int deletedCnt = 0;

        CarryInAbnormal carryInAbnormal;
        SearchCarryInAbnormal search = new SearchCarryInAbnormal();

        for (Map<String, Object> deleteMap : deleteList) {
            search.setEntCode(Util.null2str(delete.getEntCode()));
            search.setEntBusinessCode(Util.null2str(delete.getEntBusinessCode()));
            search.setAbnNo(Util.null2str(deleteMap.get("abnNo")));
            search.setMblNo(Util.null2str(deleteMap.get("mblNo")));
            search.setArDate(Util.null2str(deleteMap.get("arDate")));
            carryInAbnormal = bmsAbnormalMapper.selectDetailAbnormal(search);
            if (null != carryInAbnormal){
                if("BMS01".equals(carryInAbnormal.getLastPodStatus()) || "BMS02".equals(carryInAbnormal.getLastPodStatus())
                        || "BMS05".equals(carryInAbnormal.getLastPodStatus())){
                    search.setDefaultSort("ABN_NO");
                    Util.accessControlDatabase(search, null, true, null, null, null, null);
                    List<CarryInAbnormalHbl> blList = bmsAbnormalMapper.selectAbnormalHblListByMBL(search);
                    for (CarryInAbnormalHbl carryInAbnormalHbl: blList) {
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
    					processLogs.setDbType(BmsConsts.DbType.BMS_ABNORMAL);
                        processLogs.setDescription(BmsConsts.DbType.BMS_ABNORMAL+"[삭제 BL_NO:" + Util.null2str(carryInAbnormalHbl.getBlNo()) + ", 삭제사유:" + delete.getReason() + "]");
                        processLogs.setJobNo(Util.null2str(carryInAbnormalHbl.getOdJobNo()));
                        processLogs.setJobLog(new Gson().toJson(carryInAbnormalHbl));
                        processLogs.setResult("ok");
                        processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
                        processLogs.setProcessUserName(delete.getLogUserName());
                        processLogs.setProcessUserNameEng(delete.getLogUserNameEng());
                        // 호출시 필요 항목 : BL_NO, POD_STATUS
                        bmsCommService.insertLogAndPodStatus(processLogs, request
                                , BmsConsts.PodStatus.DELETE /* pod Status 값 매핑 */
                                , delete.getReason()
                                , null
                        );
                        /**작업내역 로그끝*/
                    }
                    //TB_BMS_ABNOAMAL , TB_BMS_ABNORMAL_BL 을 동시에 지움 (MBL : BL = 1 : N )
                    int resultCnt =bmsAbnormalMapper.deleteAbnormalMaster(carryInAbnormal.getAbnNo());
                    bmsAbnormalMapper.deleteAbnormalHBL(carryInAbnormal.getAbnNo());
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

    public CarryInAbnormalDetailList checkExistAbnormal(SearchCarryInAbnormal search) {
        CarryInAbnormalDetailList carryInAbnormalDetailList = new CarryInAbnormalDetailList();
        CarryInAbnormal carryInAbnormal = new CarryInAbnormal();
        String logVal = "화물관리번호:" + search.getCargoReferenceNo();
        log.info(logVal);
        String cargoReferenceNo = Util.null2str(search.getCargoReferenceNo());
        //carryInAbnormal.setCargoReferenceNo(cargoReferenceNo);
        if(cargoReferenceNo.length() >= 15) {
            carryInAbnormal.setMrn(cargoReferenceNo.substring(0,11));
            carryInAbnormal.setMsn(cargoReferenceNo.substring(11,15));
        }
        carryInAbnormal.setMblNo(search.getMblNo());
        carryInAbnormal.setArDate(search.getArDate());
        logVal = "반입이상번호1" + search.getAbnNo();
        log.info(logVal);
        if(search.getAbnNo() != null) {
            carryInAbnormal.setAbnNo(Integer.parseInt(search.getAbnNo()));
        }
        int abnormalBlCnt = bmsAbnormalMapper.checkExistAbnormalBl(carryInAbnormal);
        if(abnormalBlCnt == 0) {
            int abnormalCnt = bmsAbnormalMapper.checkExistAbnormal(carryInAbnormal);
            carryInAbnormalDetailList.setTotal(abnormalCnt);
            log.info("반입이상(MBL)갯수: {}",abnormalCnt);
        }else {
            carryInAbnormalDetailList.setTotal(abnormalBlCnt);
            log.info("반입이상갯수(HBL): {}",abnormalBlCnt);
        }

        int inPlanCnt = bmsAbnormalMapper.checkExistInPlan(Integer.parseInt(search.getInPlanNo()));
        log.info("반입예정갯수: {}",inPlanCnt);
        carryInAbnormalDetailList.setTotalInPlan(inPlanCnt);

        return carryInAbnormalDetailList;
    }

    public CarryInAbnormal selectDetailAbnormal(SearchCarryInAbnormal search) {
    	String logVal = "화물관리번호::::" + search.getCargoReferenceNo();
        log.info(logVal);
        /*CarryInAbnormal tempAbnormal = bmsAbnormalMapper.selectAbnNoByCargoRefNo(search);
        search.setAbnNo(String.valueOf(tempAbnormal.getAbnNo()));*/
        CarryInAbnormal carryInAbnormal = bmsAbnormalMapper.selectDetailAbnormal(search);
        return carryInAbnormal;
    }

    public CarryInAbnormalDetailList selectAbnormalHblList(SearchCarryInAbnormal search) throws Exception {
        CarryInAbnormalDetailList list = new CarryInAbnormalDetailList();
        search.setPageSize(50);
        search.setPage(1);
        search.setDefaultSort(search.getSrchNoType());
        setSearchCondition(search);

        Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchEtcType", true, "srchDateGbn", "|ABN_REQ_DATE|AR_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|MBL_NO");
        List<CarryInAbnormal> carryInAbnormalList  = bmsAbnormalMapper.selectAbnormalHblList(search);
        if(carryInAbnormalList.size() > 0) {
            list.setTotal(carryInAbnormalList.get(0).getTotal());
        }else {
            list.setTotal(0);
        }

        list.setCarryInAbnormalList(carryInAbnormalList);

        return list;
    }

    public CarryInAbnormalDetailList selectAbnormalHblNotInList(SearchCarryInAbnormal search) throws Exception {
        CarryInAbnormalDetailList list = new CarryInAbnormalDetailList();
        search.setPageSize(50);
        search.setPage(1);
        search.setDefaultSort(search.getSrchNoType());
        setSearchCondition(search);

        Util.accessControlDatabase(search, null, true, null, null, null, null);
        List<CarryInAbnormal> carryInAbnormalList  = bmsAbnormalMapper.selectAbnormalHblNotInList(search);
        if(carryInAbnormalList.size() > 0) {
            list.setTotal(carryInAbnormalList.get(0).getTotal());
        }else {
            list.setTotal(0);
        }

        list.setCarryInAbnormalList(carryInAbnormalList);

        return list;
    }

    public CarryInAbnormalDetailList selectAbnormalHblListByMBL(SearchCarryInAbnormal search) throws Exception {
        CarryInAbnormalDetailList list =new CarryInAbnormalDetailList();
        Util.accessControlDatabase(search, null, true, null, null, null, null);
        List<CarryInAbnormalHbl> carryInAbnormalHblList = bmsAbnormalMapper.selectAbnormalHblListByMBL(search);
        search.setPageSize(50);
        search.setPage(1);
        search.setDefaultSort(search.getSrchNoType());
        setSearchCondition(search);

        list.setCarryInAbnormalHblList(carryInAbnormalHblList);

        return list;
    }

    public CarryInAbnormalDetailList selectAbnormalListPopup(SearchCarryInAbnormal search) throws Exception {
        CarryInAbnormalDetailList list = new CarryInAbnormalDetailList();
        CarryInAbnormal tmpCarryInAbnormal = new CarryInAbnormal();
        List<CarryInAbnormal> carryInAbnormalList = new ArrayList<>();
        String mblNo = "";
        int existCnt = 0;
        String logVal = "SrchNoType:: " + search.getSrchNoType();
        log.info(logVal);
        if(search.getSrchNoType().equals("BL_NO")) {
            mblNo = bmsAbnormalMapper.selectMblNoByBlNo(search.getSrchNo());
            log.info("가져온MBLNO: {}",mblNo);
            tmpCarryInAbnormal.setMblNo(mblNo);
            existCnt = bmsAbnormalMapper.checkExistAbnormal(tmpCarryInAbnormal);
        }
        log.info("반입이상에 MBL존재여부: {}",existCnt);
        search.setSrchAbnormalState("ABNORMAL");
        if(existCnt > 0) {
            search.setMblNo(mblNo);
            CarryInAbnormal carryInAbnormal = bmsAbnormalMapper.selectDetailAbnormal(search);
            list.setTotal(1);
            carryInAbnormalList.add(carryInAbnormal);
        }else {
        	Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchEtcType", true, "srchDateGbn", "|ABN_REQ_DATE|AR_DATE|noDateChk", "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|MBL_NO");
            carryInAbnormalList = bmsAbnormalMapper.selectAbnormalList(search);
            search.setPageSize(50);
            search.setPage(1);
            search.setDefaultSort(search.getSrchNoType());
            setSearchCondition(search);

            if(carryInAbnormalList.size() > 0) {
                list.setTotal(carryInAbnormalList.get(0).getTotal());
            }else {
                list.setTotal(0);
                //반입이상 테이블의 건수가 0인 경우 반입예정 테이블 조회
                search.setSrchAbnormalState("IN_PLAN");
                List<InPlan> inPlanList = bmsAbnormalMapper.selectInPlanList(search);
                if(inPlanList.size() > 0) {
                    list.setTotalInPlan(inPlanList.get(0).getTotal());
                }else {
                    list.setTotalInPlan(0);
                }
                list.setInPlanList(inPlanList);
            }
        }

        list.setCarryInAbnormalList(carryInAbnormalList);

        return list;
    }

    private static void setSearchCondition(SearchCarryInAbnormal search) throws Exception {
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


    public CarryInAbnormalSaveResult insertAbnormal(CarryInAbnormal carryInAbnormal, HttpServletRequest request) throws Exception {
        CarryInAbnormalSaveResult carryInAbnormalSaveResult = new CarryInAbnormalSaveResult();
        String logVal = "";
        //반입이상제출번호 입력
        String bmsWhCode = carryInAbnormal.getBmsWhCode();
        AbnormalSpCallVo voIn = getAbnormalCustomsReportNo(bmsWhCode);
        log.info("반입이상제출번호: {}",voIn.getRetCustomsReportNo());
        //TB_BMS_ABNORMAL 에 CarryInAbnormal 데이터 입력
        carryInAbnormal.setCustomsReportNo(voIn.getRetCustomsReportNo());
        logVal = "반입이상(carryInAbnormal) - :: " + carryInAbnormal;
        log.info(logVal);
        int intRow = bmsAbnormalMapper.insertAbnormal(carryInAbnormal);
        int abnNo = bmsAbnormalMapper.selectAbnNoByCustomsReportNo(carryInAbnormal.getCustomsReportNo());
        String accidentClass = Util.null2str(carryInAbnormal.getAccidentClass());
        String rejectReason = Util.null2str(carryInAbnormal.getRejectReason());
        logVal = "사고구분::: " + accidentClass;
        log.info(logVal);
        logVal = "이상이유::: " + rejectReason;
        log.info(logVal);
        logVal = "반입이상::: " + carryInAbnormal.getG1GridData();
        log.info(logVal);
        //HOUSE LIST 정보 입력(g1Grid)
        List<CarryInAbnormalHbl> carryInAbnormalHblList = new ArrayList<>();
        for (Map<String,String> map : carryInAbnormal.getG1GridData()) {
            CarryInAbnormalHbl carryInAbnormalHbl = new CarryInAbnormalHbl();
            carryInAbnormalHbl.setAbnNo(abnNo);
            carryInAbnormalHbl.setCargoReferenceNo(map.get("cargoReferenceNo"));
            carryInAbnormalHbl.setOdJobNo(map.get("odJobNo"));
            carryInAbnormalHbl.setInPlanNo(map.get("inPlanNo"));
            carryInAbnormalHbl.setHsn(map.get("hsn"));
            carryInAbnormalHbl.setBlNo(map.get("blNo"));
            carryInAbnormalHbl.setPkgType(map.get("pkgType"));
            carryInAbnormalHbl.setExCt(Integer.parseInt(map.get("exCt")));
            carryInAbnormalHbl.setExWt(Double.parseDouble(map.get("exWt")));
            carryInAbnormalHbl.setCt(Integer.parseInt(map.get("ct")));
            carryInAbnormalHbl.setWt(Double.parseDouble(map.get("wt")));
            carryInAbnormalHbl.setAccidentClass(accidentClass);
            carryInAbnormalHbl.setAccidentCt(Integer.parseInt(map.get("accidentCt")));
            carryInAbnormalHbl.setAccidentWt(Double.parseDouble(map.get("accidentWt")));
            carryInAbnormalHbl.setRejectReason(rejectReason);
            carryInAbnormalHbl.setRegisterUserId(carryInAbnormal.getRegisterUserId());
            carryInAbnormalHbl.setUpdateUserId(carryInAbnormal.getUpdateUserId());
            String logVal2 = "저장할HBL- : " + carryInAbnormalHbl;
            log.info(logVal2);
            carryInAbnormalHblList.add(carryInAbnormalHbl);
        }

        for (CarryInAbnormalHbl carryInAbnormalHbl : carryInAbnormalHblList) {
        	/*
        	if("MFN".equals(accidentClass)) {
            	bmsInMapper.updateMfnIssueYnByInPlan(Integer.parseInt(carryInAbnormalHbl.getInPlanNo()));
            } else
            */ 
            if("MGN".equals(accidentClass)) {
            	bmsInMapper.updateMgnIssueYnByInPlan(Integer.parseInt(carryInAbnormalHbl.getInPlanNo()));
            }
            bmsAbnormalMapper.insertAbnormalHbl(carryInAbnormalHbl);

            /**작업내역 로그시작 (BL기준으로 로그 남김)*/
            // elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
            ProcessLogs processLogs = new ProcessLogs();
            processLogs.setEntCode(Util.null2str(carryInAbnormal.getEntCode()));
            processLogs.setEntBusinessCode(Util.null2str(carryInAbnormal.getEntBusinessCode()));
            processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
            processLogs.setProcessUserId(Util.null2str(carryInAbnormal.getRegisterUserId()));
            processLogs.setLogJobType("DATA");
    		processLogs.setBound("AIR");
    		processLogs.setBoundType("IMP");
            processLogs.setJobFuncType(BmsConsts.JobFuncType.INSERT);
			processLogs.setDbType(BmsConsts.DbType.BMS_ABNORMAL);
            processLogs.setDescription(BmsConsts.DbType.BMS_ABNORMAL+" [입력 BL_NO:"+String.valueOf(carryInAbnormalHbl.getBlNo())+"]");
            processLogs.setJobNo(String.valueOf(carryInAbnormalHbl.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
            processLogs.setJobLog(new Gson().toJson(carryInAbnormalHbl));
            processLogs.setResult("ok");
            processLogs.setEntBusinessPrintName(carryInAbnormal.getLogUserBusinessPrintName());
            processLogs.setProcessUserName(carryInAbnormal.getLogUserName());
            processLogs.setProcessUserNameEng(carryInAbnormal.getLogUserNameEng());
            //	String podStatus = BmsConsts.PodStatus.BMS99;
            String blNo = String.valueOf(carryInAbnormal.getBlNo());
            // 호출시 필요 항목 : BL_NO, POD_STATUS
            bmsCommService.insertLogAndPodStatus(processLogs, request
                    , BmsConsts.PodStatus.INSERT
                    , ""
                    , carryInAbnormal
            );
            /**작업내역 로그끝*/
        }
        carryInAbnormalSaveResult.setResultCode("A012051");// 저장 되었습니다.

        carryInAbnormalSaveResult.setAbnNo(abnNo);
        return carryInAbnormalSaveResult;
    }

    public CarryInAbnormalSaveResult updateAbnormal(CarryInAbnormal carryInAbnormal, HttpServletRequest request) throws Exception {
        CarryInAbnormalSaveResult carryInAbnormalSaveResult = new CarryInAbnormalSaveResult();
        String logVal = "";
        int intRow = bmsAbnormalMapper.updateAbnormal(carryInAbnormal);

        int abnNo = bmsAbnormalMapper.selectAbnNoByCustomsReportNo(carryInAbnormal.getCustomsReportNo());

        String accidentClass = Util.null2str(carryInAbnormal.getAccidentClass());
        String rejectReason = Util.null2str(carryInAbnormal.getRejectReason());
        logVal = "사고구분::: " + accidentClass;
        log.info(logVal);
        logVal = "이상이유::: " + rejectReason;
        log.info(logVal);
        logVal = "반입이상::: " + carryInAbnormal.getG1GridData();
        log.info(logVal);
        //HOUSE LIST 정보 입력(g1Grid)
        List<CarryInAbnormalHbl> carryInAbnormalHblList = new ArrayList<>();
        for (Map<String,String> map : carryInAbnormal.getG1GridData()) {
            CarryInAbnormalHbl carryInAbnormalHbl = new CarryInAbnormalHbl();
            carryInAbnormalHbl.setAbnNo(abnNo);
            carryInAbnormalHbl.setCargoReferenceNo(map.get("cargoReferenceNo"));
            carryInAbnormalHbl.setOdJobNo(map.get("odJobNo"));
            carryInAbnormalHbl.setHsn(map.get("hsn"));
            carryInAbnormalHbl.setBlNo(map.get("blNo"));
            carryInAbnormalHbl.setPkgType(map.get("pkgType"));
            carryInAbnormalHbl.setExCt(Integer.parseInt(map.get("exCt")));
            carryInAbnormalHbl.setExWt(Double.parseDouble(map.get("exWt")));
            carryInAbnormalHbl.setCt(Integer.parseInt(map.get("ct")));
            carryInAbnormalHbl.setWt(Double.parseDouble(map.get("wt")));
            carryInAbnormalHbl.setAccidentClass(accidentClass);
            carryInAbnormalHbl.setAccidentCt(Integer.parseInt(map.get("accidentCt")));
            carryInAbnormalHbl.setAccidentWt(Double.parseDouble(map.get("accidentWt")));
            carryInAbnormalHbl.setRejectReason(rejectReason);
            carryInAbnormalHbl.setRegisterUserId(carryInAbnormal.getRegisterUserId());
            carryInAbnormalHbl.setUpdateUserId(carryInAbnormal.getUpdateUserId());
            String logVal2 = "저장할HBL- : " + carryInAbnormalHbl;
            log.info(logVal2);
            carryInAbnormalHblList.add(carryInAbnormalHbl);
        }
        bmsAbnormalMapper.deleteAbnormalHBL(abnNo);
        for (CarryInAbnormalHbl carryInAbnormalHbl : carryInAbnormalHblList) {

            /**작업내역 로그시작*/
            // elastic search log and TB_ECM_PD_01 ~ TB_ECM_PD_04 입력
            ProcessLogs processLogs = new ProcessLogs();
            processLogs.setEntCode(Util.null2str(carryInAbnormal.getEntCode()));
            processLogs.setEntBusinessCode(Util.null2str(carryInAbnormal.getEntBusinessCode()));
            processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
            processLogs.setProcessUserId(Util.null2str(carryInAbnormal.getRegisterUserId()));
            processLogs.setLogJobType("DATA");
    		processLogs.setBound("AIR");
    		processLogs.setBoundType("IMP");
            processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
			processLogs.setDbType(BmsConsts.DbType.BMS_ABNORMAL);
            processLogs.setDescription(BmsConsts.DbType.BMS_ABNORMAL+" [수정 BL_NO:"+String.valueOf(carryInAbnormalHbl.getBlNo())+"]");
            processLogs.setJobNo(String.valueOf(carryInAbnormalHbl.getOdJobNo()));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
            processLogs.setJobLog(new Gson().toJson(carryInAbnormalHbl));
            processLogs.setResult("ok");
            processLogs.setEntBusinessPrintName(carryInAbnormal.getLogUserBusinessPrintName());
            processLogs.setProcessUserName(carryInAbnormal.getLogUserName());
            processLogs.setProcessUserNameEng(carryInAbnormal.getLogUserNameEng());
            //	String podStatus = BmsConsts.PodStatus.BMS99;
            String blNo = String.valueOf(carryInAbnormal.getBlNo());
            // 호출시 필요 항목 : BL_NO, POD_STATUS
            bmsCommService.insertLogAndPodStatus(processLogs, request
                    , BmsConsts.PodStatus.UPDATE
                    , ""
                    , carryInAbnormal
            );
            /**작업내역 로그끝*/
            bmsAbnormalMapper.insertAbnormalHbl(carryInAbnormalHbl);
        }

        carryInAbnormalSaveResult.setResultCode("A012051");// 저장 되었습니다.
        carryInAbnormalSaveResult.setAbnNo(abnNo);
        return carryInAbnormalSaveResult;
    }

    public AbnormalSpCallVo getAbnormalCustomsReportNo(String bmsWhCode) {
        AbnormalSpCallVo voIn = new AbnormalSpCallVo();
        voIn.setMode("M"); // 	M: Manual
        voIn.setBmsWhCode(bmsWhCode);
        // 0 18자리 반입이상제출번호(CUSTOMS_REPORT_NO) 시퀀스 작성
        bmsAbnormalMapper.callProcBMSAbnormalSeq(voIn);
        return voIn;
    }

	/**
	 * 관세청 XML 송신(Send) - GOVCBRDKO(특송화물 반입물품 이상보고서)
	 * @param params
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CarryInAbnormalSaveResult sendOutToXml(BatchBmsAbnormal params, HttpServletRequest request) throws Exception {
		CarryInAbnormalSaveResult result = new CarryInAbnormalSaveResult();
		String batchExecType = params.getBatchExecType();

		String entCode = params.getEntCode();
		String entBusinessCode = params.getEntBusinessCode();
		List<Integer> abnNoList = new ArrayList<Integer>();
		List<CarryInAbnormal> dataList = params.getCarryInAbnormalList();

		if("SEND".equals(batchExecType)) {
			for(CarryInAbnormal data : dataList) {
				abnNoList.add(data.getAbnNo());
			}

			result = bmsSendXmlParser.callSendXmlGOVCBRDKOParserExecutor(entCode, entBusinessCode, abnNoList);
		}

		return result;
	}

}
