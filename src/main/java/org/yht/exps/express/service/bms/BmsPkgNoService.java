package org.yht.exps.express.service.bms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsPkgNoMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsPkgNo.BmsPkgNo;
import org.yht.exps.express.model.bms.bmsPkgNo.BmsPkgNoList;
import org.yht.exps.express.model.bms.bmsPkgNo.search.SearchBmsPkgNoList;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.InPlan;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.util.Util;

import com.google.gson.Gson;


@Service
@Transactional
public class BmsPkgNoService {

    @Autowired
    BmsPkgNoMapper bmsPkgNoMapper;
    
    @Autowired
	BmsCommService bmsCommService ;

    /**
     * 박스번호조회 리스트 조회
     * @param search
     * @return
     * @throws Exception
     */
    public BmsPkgNoList selectBmsPkgNoList(SearchBmsPkgNoList search) throws Exception {
        BmsPkgNoList bmsPkgNoList = new BmsPkgNoList();
        List<BmsPkgNo> list = null;

        if(search.getPage() != 0 && search.getPageSize() != 0) { search.setStartNum(search.getPageSize() * (search.getPage() - 1)); }
        if(Util.null2str(search.getChangeField()).equals("Y")) { Util.camelCaseToUnderscore(search); }

        /** I : 반입예정번호(inPlanNo)로 조회 */
        if("I".equals(search.getSrchInOutClass())) {
            list = bmsPkgNoMapper.selectBmsPkgNoListByInPlanNo(search);
            if(list == null || (list != null && list.size() == 0) ) {
                list = bmsPkgNoMapper.selectBmsPkgNoListExistPlanByInPlanNo(search);
            }
            if("21".equals(Util.null2str(search.getInClass()))) {
                list = bmsPkgNoMapper.selectBmsPkgNoListExistPlanByOrigInPlanNo(search);
            }
        }
        /** O : 반입번호(inNo)로 조회 */
        else if("O".equals(search.getSrchInOutClass())){
            list = bmsPkgNoMapper.selectBmsPkgNoListByInNo(search);
        }
        bmsPkgNoList.setBmsPkgNoList(list);

        if(list.size() > 0) {
            bmsPkgNoList.setTotal(list.get(0).getTotal()); 
        } else {
            bmsPkgNoList.setTotal(0);
            bmsPkgNoList.setResultCode("notExistInPlan");
        }

        return bmsPkgNoList;
    }

    public BmsPkgNoList insert(Map<String, Object> search, HttpServletRequest request) throws Exception {
        BmsPkgNoList bmsPkgNoList = new BmsPkgNoList();
        
        // 중복되는 박스번호 삭제처리 2026.04.07 2sowell
        if("Y".equals(Util.null2str(search.get("deleteDupPackingNo")))) {
            bmsPkgNoMapper.softDeleteInPlanPkgByPkgNo(search);
        }
        
        // 판토스일경우 체크해야할 사항이 더 있음 (주석처리. 반출되어도 가능하도록 2026.05.08 2sowell)
//        if("PANTK".equals(Util.null2str(search.get("entCode")))) {
//            int pcnt = bmsPkgNoMapper.checkBoxOutScanByPackingNo(search);
//            if(pcnt > 0) {
//                bmsPkgNoList.setResultCode("ABMS100054"); // 이미 출고스캔된 박스번호입니다.
//                return bmsPkgNoList;
//            }
//        }
        
        if(search.get("srchDivideClass") != null) {
            
            int cnt = bmsPkgNoMapper.checkDupPkgByPackingNo(search);
            if(cnt>0) {
                bmsPkgNoList.setResultCode("ABMS100050"); // 해당박스번호는 이미 저장되어 있습니다.
                return bmsPkgNoList;
            }
            
            InPlan toInPlan = null;
            Integer exCt = null;
            
            // 분할등록인 경우
            if( "1".equals(Util.null2str(search.get("srchDivideClass"))) ) {
                
                InPlanPkg fromInPlanWithPkg = bmsPkgNoMapper.selectInPlanWithPkgByMblWithBl(search);
                if(fromInPlanWithPkg == null) {
                    bmsPkgNoList.setResultCode("notExistBl"); //해당 #1# 데이터는 존재하지 않습니다.
                    return bmsPkgNoList;
                }
                
                bmsPkgNoList.setResultCode("ABMS100052"); // 기존 박스번호를 삭제하고 새로운 박스번호로 추가 하시겠습니까?
                return bmsPkgNoList;
            }
            // 보수작업인 경우
            else if( "3".equals(Util.null2str(search.get("srchDivideClass"))) ) {
                toInPlan = bmsPkgNoMapper.selectBmsInPlanByInPlanNo(search);
                if(toInPlan == null) {
                    bmsPkgNoList.setResultCode("notExistBl"); //해당 #1# 데이터는 존재하지 않습니다.
                    return bmsPkgNoList;
                }
                exCt = toInPlan.getExCt();

                Integer pkgInCtSum =  bmsPkgNoMapper.sumPkgByInPlanNo(search);
                if(pkgInCtSum != null && exCt < pkgInCtSum + 1) {
                    bmsPkgNoList.setResultCode("ABMS100051"); // 박스수량이 #1으로 추가된 수량보다 많습니다.
                    return bmsPkgNoList;
                }
                
                bmsPkgNoMapper.insertPkg(search);
                
                // POD 저장
                ProcessLogs processLogs = new ProcessLogs();
        		processLogs.setEntCode(Util.null2str(search.get("entCode")));
        		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
        		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
        		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
        		processLogs.setLogJobType("DATA");
        		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
        		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
        		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [박스번호추가]");
        		processLogs.setJobLog(new Gson().toJson(search));
        		processLogs.setJobNo(String.valueOf(search.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
        		processLogs.setResult("ok");
        		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
        		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
        		//commonService.insertLog(processLogs, request);
        		bmsCommService.insertLogAndPodStatus(processLogs, request
        				, BmsConsts.PodStatus.CUSTOM_POD_NAME /* pod Status 값 매핑 */
        				, "박스번호추가("+Util.null2str(search.get("packingNo"))+")"
        				, "박스번호추가"
        		);
                
                bmsPkgNoList.setResultCode("ABMS100052");
                return bmsPkgNoList;
            }
        }else {
            Integer inPlanNo = null;
            Integer origInPlanNo = null;
            Integer exCt = null;
            String inClass = null;
            
            CarryIn bmsIn = null;
            InPlan inPlan = null;
            
            if(Util.null2int(search.get("inPlanNo")) > 0 ) {
                inPlan = bmsPkgNoMapper.selectBmsInPlanByInPlanNo(search);
                if(inPlan == null) {
                    bmsPkgNoList.setResultCode("notExistBl");
                    return bmsPkgNoList;
                }
            
                if( !"".equals(Util.null2str(search.get("cargoReferenceNo"))) && !"".equals(Util.null2str(search.get("blNo"))) ) {
                    bmsIn = bmsPkgNoMapper.selectOriginBlByInNo(search);
                }
                exCt = inPlan.getExCt();
                inPlanNo = inPlan.getInPlanNo();
                if(bmsIn != null) {
                    origInPlanNo = bmsIn.getOrigInPlanNo();
                    inClass = bmsIn.getInClass();
                }
            }else if( !"".equals(Util.null2str(search.get("inClass"))) && !"".equals(Util.null2str(search.get("cargoReferenceNo"))) ) { // inClass 넣어서 조회하는거 추가 2026.04.07 2sowell
                bmsIn = bmsPkgNoMapper.selectOriginBlByInNoClass(search);
                if(bmsIn == null) {
                    bmsPkgNoList.setResultCode("notExistBl");
                    return bmsPkgNoList;
                }
                exCt = bmsIn.getCt();
                inPlanNo = bmsIn.getInPlanNo();
                origInPlanNo = bmsIn.getOrigInPlanNo();
                inClass = bmsIn.getInClass();
            }else if( !"".equals(Util.null2str(search.get("cargoReferenceNo"))) && !"".equals(Util.null2str(search.get("blNo"))) ) {
                bmsIn = bmsPkgNoMapper.selectOriginBlByInNo(search);
                if(bmsIn == null) { // IN_PLAN_NO 없는 데이터 오류나서 추가 2026.04.07 2sowell
                    bmsPkgNoList.setResultCode("notExistBl");
                    return bmsPkgNoList;
                }
                exCt = bmsIn.getCt();
                inPlanNo = bmsIn.getInPlanNo();
                origInPlanNo = bmsIn.getOrigInPlanNo();
                inClass = bmsIn.getInClass();
            }else if( "".equals(Util.null2str(search.get("inPlanNo"))) || "0".equals(Util.null2str(search.get("inPlanNo"))) ) {
                bmsIn = bmsPkgNoMapper.selectBmsInByInNo(search);
                if(bmsIn == null) {
                    bmsPkgNoList.setResultCode("notExistBl");
                    return bmsPkgNoList;
                }
                exCt = bmsIn.getCt();
                inPlanNo = bmsIn.getInPlanNo();
                origInPlanNo = bmsIn.getOrigInPlanNo();
                inClass = bmsIn.getInClass();
            }
            
            search.put("inPlanNo", inPlanNo);
            
            Map noMap = new HashMap<String, Object>();
            if((inPlanNo != null && inPlanNo != 0) || (origInPlanNo != null && origInPlanNo != 0)) {
                if(inPlanNo != null && inPlanNo != 0) {
                    noMap.put("inPlanNo", inPlanNo);
                }
                if(origInPlanNo != null && origInPlanNo != 0) { // 21.이고반입 ORIG_IN_PLAN_NO 조회 추가 2026.04.07 2sowell
                    noMap.put("inPlanNo", origInPlanNo);
                }
                Integer pkgInCtSum =  bmsPkgNoMapper.sumPkgByInPlanNo(noMap);
                if(pkgInCtSum != null && exCt < pkgInCtSum + 1) {
                    bmsPkgNoList.setResultCode("ABMS100051"); // 박스수량이 #1으로 추가된 수량보다 많습니다.
                    return bmsPkgNoList;
                }
            }
            
            // IN_PLAN , XRAY 관련 데이터 입력 추가 2026.04.07 2sowell
            if(Util.null2str(inClass).equals("21")) {   // 21.이고반입(적출입작업) 경우
                // IN_PLAN_NO 생성 후 ORIG_IN_PLAN_NO 업데이트 
                if(origInPlanNo == null || origInPlanNo == 0) {
                    // IN_PLAN_NO 생성
                    bmsPkgNoMapper.insertInPlan(search);
                    origInPlanNo = bmsPkgNoMapper.selectInPlanNoByCargoWithBl(search);
                    bmsIn.setOrigInPlanNo(origInPlanNo);
                    bmsPkgNoMapper.updateOrigInPlanNo(bmsIn);   // IN 테이블 ORIG_IN_PLAN_NO 값 업데이트
                }
                
                search.put("inPlanNo", origInPlanNo);
                
                // 동일한 IN_PLAN_NO 동일 박스번호 확인
                int icnt = bmsPkgNoMapper.checkDupPkgByInPackingNo(search);
                if(icnt>0) {
                    bmsPkgNoList.setResultCode("ABMS100053"); // 해당박스번호는 이미 저장되어 있습니다.
                    return bmsPkgNoList;
                }
                // 다른 IN_PLAN_NO 동일 박스번호 확인
                Map<String, Object> pkgMap = bmsPkgNoMapper.checkDupPkgInfoByPackingNo(search);
                int cnt = Util.null2int(pkgMap.get("CNT"));
                if(cnt>0) {
                    bmsPkgNoList.setResultCode("ABMS100055"); // 해당박스번호는 이미 저장되어 있습니다.
                    bmsPkgNoList.setRemarks(Util.null2str(pkgMap.get("IN_CLASS")));
                    bmsPkgNoList.setResultMsg("다른 B/L (" + Util.null2str(pkgMap.get("BL_NO")) + ") 에 중복된 박스번호가 존재합니다.");
                    return bmsPkgNoList;
                }
                
                // XRAY 데이터 생성
                int historyCnt = bmsPkgNoMapper.selectXrayHistoryByInPlanNo(search);
                int resultCnt = bmsPkgNoMapper.selectXrayResultByInPlanNo(search);
                if(bmsIn == null) {
                	search.put("xrayInspectionCode", "");
                	search.put("xrayLineNo", "");
                	search.put("xrayScanCt", 0);
                	search.put("xrayImageCt", 0);
                }else {
                	search.put("xrayInspectionCode", bmsIn.getXrayInspectionCode());
                	search.put("xrayLineNo", bmsIn.getXrayLineNo());
                	search.put("xrayScanCt", bmsIn.getXrayScanCt());
                	search.put("xrayImageCt", bmsIn.getXrayImageCt());
                }
                if(historyCnt == 0) {
                    bmsPkgNoMapper.insertXrayHistory(search);
                }
                if(resultCnt == 0) {
                    bmsPkgNoMapper.insertXrayResult(search);
                }
            }else { // 이고반입이 아닌 건들...
                if(inPlanNo == null || inPlanNo == 0) {
                    // IN_PLAN_NO 생성
                    bmsPkgNoMapper.insertInPlan(search);
                    inPlanNo = bmsPkgNoMapper.selectInPlanNoByCargoWithBl(search);
                    bmsIn.setInPlanNo(inPlanNo);
                    bmsPkgNoMapper.updateInPlanNo(bmsIn);   // IN 테이블 IN_PLAN_NO 값 업데이트
                }
                search.put("inPlanNo", inPlanNo);
                // 동일한 IN_PLAN_NO 동일 박스번호 확인
                int icnt = bmsPkgNoMapper.checkDupPkgByInPackingNo(search);
                if(icnt>0) {
                    bmsPkgNoList.setResultCode("ABMS100053"); // 해당박스번호는 이미 저장되어 있습니다.
                    return bmsPkgNoList;
                }
                // 다른 IN_PLAN_NO 동일 박스번호 확인
                Map<String, Object> pkgMap = bmsPkgNoMapper.checkDupPkgInfoByPackingNo(search);
                int cnt = Util.null2int(pkgMap.get("CNT"));
                if(cnt>0) {
                    bmsPkgNoList.setResultCode("ABMS100055"); // 해당박스번호는 이미 저장되어 있습니다.
                    bmsPkgNoList.setRemarks(Util.null2str(pkgMap.get("IN_CLASS")));
                    bmsPkgNoList.setResultMsg("다른 B/L (" + Util.null2str(pkgMap.get("BL_NO")) + ") 에 중복된 박스번호가 존재합니다.");
                    return bmsPkgNoList;
                }
                
                // XRAY 데이터 생성
                int historyCnt = bmsPkgNoMapper.selectXrayHistoryByInPlanNo(search);
                int resultCnt = bmsPkgNoMapper.selectXrayResultByInPlanNo(search);
                
                if(bmsIn == null) {
                	search.put("xrayInspectionCode", "");
                	search.put("xrayLineNo", "");
                	search.put("xrayScanCt", 0);
                	search.put("xrayImageCt", 0);
                }else {
                	search.put("xrayInspectionCode", bmsIn.getXrayInspectionCode());
                	search.put("xrayLineNo", bmsIn.getXrayLineNo());
                	search.put("xrayScanCt", bmsIn.getXrayScanCt());
                	search.put("xrayImageCt", bmsIn.getXrayImageCt());
                }
                if(historyCnt == 0) {
                    bmsPkgNoMapper.insertXrayHistory(search);
                }
                if(resultCnt == 0) {
                    bmsPkgNoMapper.insertXrayResult(search);
                }
            }
            
            //if(bmsIn == null) {
            //    search.put("inCt", 0);
            //}else {
                search.put("inCt", 1);
            //}
            bmsPkgNoMapper.insertPkg(search);
            
            // POD 저장
            ProcessLogs processLogs = new ProcessLogs();
    		processLogs.setEntCode(Util.null2str(search.get("entCode")));
    		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
    		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
    		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
    		processLogs.setLogJobType("DATA");
    		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
    		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
    		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [박스번호추가]");
    		processLogs.setJobLog(new Gson().toJson(search));
    		processLogs.setJobNo(String.valueOf(search.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
    		processLogs.setResult("ok");
    		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
    		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
    		//commonService.insertLog(processLogs, request);
    		bmsCommService.insertLogAndPodStatus(processLogs, request
    				, BmsConsts.PodStatus.CUSTOM_POD_NAME /* pod Status 값 매핑 */
    				, "박스번호추가("+Util.null2str(search.get("packingNo"))+")"
    				, "박스번호추가"
    		);
            
            bmsPkgNoList.setResultCode("A012051");
        }
        
        return bmsPkgNoList;
    }
    
    
    public BmsPkgNoList blSplitPkgInsert(Map<String, Object> search, HttpServletRequest request) throws Exception {
        BmsPkgNoList bmsPkgNoList = new BmsPkgNoList();
        
        InPlan toInPlan = null;
        Integer exCt = null;
        
        toInPlan = bmsPkgNoMapper.selectBmsInPlanByInPlanNo(search);
        if(toInPlan == null) {
            bmsPkgNoList.setResultCode("notExistBl"); //해당 #1# 데이터는 존재하지 않습니다.
            return bmsPkgNoList;
        }
        exCt = toInPlan.getExCt();

        Integer pkgInCtSum =  bmsPkgNoMapper.sumPkgByInPlanNo(search);
        if(pkgInCtSum != null && exCt < pkgInCtSum + 1) {
            bmsPkgNoList.setResultCode("ABMS100051"); // 박스수량이 #1으로 추가된 수량보다 많습니다.
            return bmsPkgNoList;
        }
        
        if("Y".equals(search.get("deleteOriginPkg")) ) { // 삭제하고 
            List<InPlanPkg> fromPkgList = bmsPkgNoMapper.selectPkgListByInPlanNo(search);
            //InPlanPkg fromInPlanWithPkg = bmsPkgNoMapper.selectInPlanWithPkgByMblWithBl(search);
            
            //if(fromInPlanWithPkg != null) {
                //Map<String, Object> batchParam = new HashMap<String, Object>();
                //batchParam.put("inPlanNo", fromInPlanWithPkg.getInPlanNo());
                //batchParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
                //bmsPkgNoMapper.batchInsertPkgByInPlanNo(batchParam);
            //}
            
            if(fromPkgList != null && fromPkgList.size()>0) {
                Map<String, Object> delParam = new HashMap<String, Object>();
                delParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
                delParam.put("packingNoList", fromPkgList.stream().map(pkg -> pkg.getPackingNo()).collect(Collectors.toList()) );
                bmsPkgNoMapper.softDeleteInPlanPkgByPkgNoList(delParam);
            }
            
            search.put("inCt", 1);
            bmsPkgNoMapper.insertPkg(search);
            
            /*InPlanPkg fromInPlanWithPkg = bmsPkgNoMapper.selectInPlanWithPkgByMblWithBl(search);
            
            Map<String, Object> batchParam = new HashMap<String, Object>();
            batchParam.put("inPlanNo", fromInPlanWithPkg.getInPlanNo());
            batchParam.put("updateUserId", Util.null2str(search.get("updateUserId")));
            bmsPkgNoMapper.batchInsertPkgByInPlanNo(batchParam);
            
            bmsPkgNoMapper.softDeleteInPlanPkgByInPlanNo(batchParam);
            
            bmsPkgNoMapper.insertPkg(search);*/
        }else if("N".equals(search.get("deleteOriginPkg")) ) { // 삭제없이 유지하고
            bmsPkgNoMapper.insertPkg(search);
        }       
        
        bmsPkgNoList.setResultCode("A012051");
        return bmsPkgNoList;
    }
    
    
    public BmsPkgNoList saveNewCargoNo(Map<String, Object> search, HttpServletRequest request) throws Exception {
        BmsPkgNoList bmsPkgNoList = new BmsPkgNoList();
        
        bmsPkgNoMapper.updateBlankNewCargoNoFromPkg(search);
        bmsPkgNoMapper.updatePkgNewCargoNo(search);
        
        bmsPkgNoList.setResultCode("A012051");
        return bmsPkgNoList;
    }
    
    
    public BmsPkgNoList update(Map<String, Object> search, HttpServletRequest request) throws Exception {
        BmsPkgNoList bmsPkgNoList = new BmsPkgNoList();
        
        // 중복되는 박스번호 삭제처리
        if("Y".equals(Util.null2str(search.get("deleteDupPackingNo")))) {
            bmsPkgNoMapper.softDeleteInPlanPkgByPkgNo(search);
        }
        
        // 판토스일경우 체크해야할 사항이 더 있음 ** 판토스는 일단 박스변경 기능 막음 2026.04.27 2sowell
        if("PANTK".equals(Util.null2str(search.get("entCode")))) {
        	Map<String, Object> chkMap = new HashMap<String, Object>();
        	chkMap.put("packingNo", search.get("oldPackingNo"));
        	int pcnt = bmsPkgNoMapper.checkBoxDeliveryOutScanByPackingNo(chkMap);
        	if(pcnt > 0) {
        		bmsPkgNoList.setResultCode("ABMS100054"); // 이미 택배스캔된 박스번호입니다.
        		return bmsPkgNoList;
        	}
        	
        	chkMap.put("packingNo", search.get("packingNo"));
        	int pccnt = bmsPkgNoMapper.checkBoxDeliveryOutScanByPackingNo(chkMap);
        	if(pccnt > 0) {
        		bmsPkgNoList.setResultCode("ABMS100056"); // 이미 택배스캔된 박스번호입니다(다른BL).
        		return bmsPkgNoList;
        	}
        }
        
        Integer inPlanNo = null;
        Integer origInPlanNo = null;
        String inClass = null;
        
        CarryIn bmsIn = null;
        InPlan inPlan = null;
        if(Util.null2int(search.get("inPlanNo")) > 0 ) {
            inPlan = bmsPkgNoMapper.selectBmsInPlanByInPlanNo(search);
            if(inPlan == null) {
                bmsPkgNoList.setResultCode("notExistBl");
                return bmsPkgNoList;
            }
        
            if( !"".equals(Util.null2str(search.get("cargoReferenceNo"))) && !"".equals(Util.null2str(search.get("blNo"))) ) {
                bmsIn = bmsPkgNoMapper.selectOriginBlByInNo(search);
            }
            inPlanNo = inPlan.getInPlanNo();
            if(bmsIn != null) {
                origInPlanNo = bmsIn.getOrigInPlanNo();
                inClass = bmsIn.getInClass();
            }
        }else if( !"".equals(Util.null2str(search.get("inClass"))) && !"".equals(Util.null2str(search.get("cargoReferenceNo"))) ) { // inClass 넣어서 조회하는거 추가 2026.04.07 2sowell
            bmsIn = bmsPkgNoMapper.selectOriginBlByInNoClass(search);
            if(bmsIn == null) {
                bmsPkgNoList.setResultCode("notExistBl");
                return bmsPkgNoList;
            }
            inPlanNo = bmsIn.getInPlanNo();
            origInPlanNo = bmsIn.getOrigInPlanNo();
            inClass = bmsIn.getInClass();
        }else if( !"".equals(Util.null2str(search.get("cargoReferenceNo"))) && !"".equals(Util.null2str(search.get("blNo"))) ) {
            bmsIn = bmsPkgNoMapper.selectOriginBlByInNo(search);
            if(bmsIn == null) { // IN_PLAN_NO 없는 데이터 오류나서 추가 2026.04.07 2sowell
                bmsPkgNoList.setResultCode("notExistBl");
                return bmsPkgNoList;
            }
            inPlanNo = bmsIn.getInPlanNo();
            origInPlanNo = bmsIn.getOrigInPlanNo();
            inClass = bmsIn.getInClass();
        }else if( "".equals(Util.null2str(search.get("inPlanNo"))) || "0".equals(Util.null2str(search.get("inPlanNo"))) ) {
            bmsIn = bmsPkgNoMapper.selectBmsInByInNo(search);
            if(bmsIn == null) {
                bmsPkgNoList.setResultCode("notExistBl");
                return bmsPkgNoList;
            }
            inPlanNo = bmsIn.getInPlanNo();
            origInPlanNo = bmsIn.getOrigInPlanNo();
            inClass = bmsIn.getInClass();
        }
        
        if(Util.null2str(inClass).equals("21")) {   // 21.이고반입(적출입작업) 경우
            search.put("inPlanNo", origInPlanNo);
        }else {
            search.put("inPlanNo", inPlanNo);
        }
        
        // 동일한 IN_PLAN_NO 동일 박스번호 확인
        int icnt = bmsPkgNoMapper.checkDupPkgByInPackingNo(search);
        if(icnt>0) {
            bmsPkgNoList.setResultCode("ABMS100053"); // 현재 B/L에 이미 동일한 박스번호가 존재합니다.
            return bmsPkgNoList;
        }
        
        
        // 다른 IN_PLAN_NO 동일 박스번호 확인
        Map<String, Object> pkgMap = bmsPkgNoMapper.checkDupPkgInfoByPackingNo(search);
        int cnt = Util.null2int(pkgMap.get("CNT"));
        if(cnt>0) {
            bmsPkgNoList.setResultCode("ABMS100055"); // 해당박스번호는 이미 저장되어 있습니다.
            bmsPkgNoList.setRemarks(Util.null2str(pkgMap.get("IN_CLASS")));
            bmsPkgNoList.setResultMsg("다른 B/L (" + Util.null2str(pkgMap.get("BL_NO")) + ") 에 중복된 박스번호가 존재합니다.");
            return bmsPkgNoList;
        }
        
        if("PANTK".equals(Util.null2str(search.get("entCode")))) {
        	// 판토스는 업데이트할 테이블 더 있음
        	bmsPkgNoMapper.updatePantosPkgNo(search);
        }
        
        // XRAY_HISTORY PACKING_NO 업데이트
        bmsPkgNoMapper.updateXrayHistory(search);
        // IN_PLAN_PKG PACKING_NO 업데이트
        bmsPkgNoMapper.updatePkg(search);
        
        // POD 저장
        ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(Util.null2str(search.get("entCode")));
		processLogs.setEntBusinessCode(Util.null2str(search.get("entBusinessCode")));
		processLogs.setSolution(BmsConsts.BMS_CONST_SOLUTION);
		processLogs.setProcessUserId(Util.null2str(search.get("updateUserId")));
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType(BmsConsts.JobFuncType.UPDATE);
		processLogs.setDbType(BmsConsts.DbType.BMS_IN);
		processLogs.setDescription(BmsConsts.DbType.BMS_IN+" [박스번호변경]");
		processLogs.setJobLog(new Gson().toJson(search));
		processLogs.setJobNo(String.valueOf(search.get("odJobNo")));  // ** 중요 여기에다가는 odJobNo를 넣어야 한다.
		processLogs.setResult("ok");
		processLogs.setProcessUserName(Util.null2str(search.get("logUserName")));
		processLogs.setProcessUserNameEng(Util.null2str(search.get("logUserNameEng")));
		//commonService.insertLog(processLogs, request);
		bmsCommService.insertLogAndPodStatus(processLogs, request
				, BmsConsts.PodStatus.CUSTOM_POD_NAME /* pod Status 값 매핑 */
				, "박스번호변경(변경전:"+Util.null2str(search.get("oldPackingNo"))+"/변경후:"+Util.null2str(search.get("packingNo"))+")"
				, "박스번호변경"
		);
        
        bmsPkgNoList.setResultCode("A012051");
        
        return bmsPkgNoList;
    }
}
