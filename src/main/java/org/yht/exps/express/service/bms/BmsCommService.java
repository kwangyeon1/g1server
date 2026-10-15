package org.yht.exps.express.service.bms;

import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.bms.BmsCommMapper;
import org.yht.exps.express.model.bms.BmsConsts;
import org.yht.exps.express.model.bms.bmsComm.BmsEcmPod;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChange;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChange;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.charge.BmsEstimate;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.exps.express.model.bms.out.BmsOutSample;
import org.yht.exps.express.model.mobile.bms.BmsLocalInspectResult;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.util.Converter;
import org.yht.util.DateTime;
import org.yht.util.Util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
@Transactional
public class BmsCommService {

    @Autowired
    BmsCommMapper bmsCommMapper;
    @Autowired
    CommonService commonService;

    @Autowired
    RestHighLevelClient restClient;
    
    /**
	 * 프로세스 처리 로그 기록
	 * @param processLogs
	 * @param request
	 * @throws Exception
	 */
	public void insertLog(ProcessLogs processLogs, HttpServletRequest request) throws Exception {
		String index = "bms-process-logs-" + DateTime.getDate("yyyyMMdd");
		String id = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
		
		processLogs.setReferenceNoId(id);
		processLogs.setProcessDateTime(System.currentTimeMillis());
		
		if (request != null)	processLogs.setAccessIp(Util.getClientIP(request));
		
		try {
			IndexApi indexApi = new IndexApi(restClient);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			DocumentApi documentApi = new DocumentApi(restClient);
			documentApi.createDocument(index, id, Converter.domainToMap(processLogs));
		} catch (Exception e) {
			log.error("오류가 발생 했습니다.", e);
		}
	}
	
	public void insertPod5(Map<String, Object> podMap) throws Exception {
		BmsEcmPod pod = new BmsEcmPod();
		pod.setEntCode(Util.null2str(podMap.get("entCode")));
        pod.setOdJobNo(Util.null2str(podMap.get("odJobNo")));
        pod.setPodName(Util.null2str(podMap.get("podName")));
        pod.setRemarks(Util.null2str(podMap.get("remarks")));
        String registerUserId = Util.null2str(podMap.get("registerUserId"));
        if("".equals(registerUserId)) registerUserId = Util.null2str(podMap.get("updateUserId"));
        if("".equals(registerUserId)) registerUserId = Util.null2str(podMap.get("inputUserId"));
        pod.setRegisterUserId(registerUserId);
		bmsCommMapper.insertBmsEcmPod(pod);
	    log.info ( "TB_ECM_POD_05 insert  odJobNo:{}, podName:{}"  , "",  pod.getOdJobNo(), pod.getPodName());
	}
	
    
    /**
     * elastic Search 입력후에 TB_ECM_POD 01~04까지 입력하는 공통 메소드
     * 1) elastic serarch에 로그 저장
     * 2) 저장후 referenceNoId를 TB_ECM_PD.pod_job_no 에 넣어서 저장
     *  필요값 : POD_STATUS, BL_NO, REGISTER_USER_ID
     * @param processLogs
     */
    public void insertLogAndPodStatus(ProcessLogs processLogs , HttpServletRequest request
    , String podStatus
    , String deleteReason  //Map<String, Object>  deleteMap
    , Object _obj   /* 각 Instance InsertUpdate  별로 넘어 오는 클래스 객체 */
    ) throws Exception {
    	String logVal = "관리자: " + processLogs;
        log.info(logVal);
        //insertLog(processLogs, request);
        commonService.insertLog(processLogs, request);
        //TB_ECM_POD_01~04 저장
        // POD_JOB_NO : processLogs.getReferenceNoId()
        // OD_JOB_NO  : processLogs.getJobNo()
        // POD_STATUS : BMS01~BMS09까지 입력 하는데 01~04는 각자 테이블에 나머지는 _POD 테이블로 입력함
        // POD_DATE(10): 2024-04-02  : 오늘 날짜  : 기본값
        // POD_TIME(5): 15:33         : 오늘 날짜 : 기본값
        // BL_NO     : blNo 를 받아서 넘겨야 함
        /*
            processLogs.setLogJobType(BmsConsts.DbType.BMS_DISPOSAL);
            processLogs.setJobFuncType(BmsConsts.JobFuncType.DELETE);
        */
        log.info ( "elastic getReferenceNoId:{}, odJobNo:{}", processLogs.getReferenceNoId(), processLogs.getJobNo());

        String podName = ""; // POD_NAME은 기초코드_V1.0.1 문서 POD 관리 참고
        // 폐기, 정산료는 POD STATUS 값 입력 하지 않기 => 폐기는 입력하도록 수정 요청(8.13)
        if(
                //BmsConsts.DbType.BMS_DISPOSAL.equals(processLogs.getDbType()) || 
                BmsConsts.DbType.BMS_ESTIMATE.equals(processLogs.getDbType())
        ) {
        	if(_obj != null) {
	        	BmsEstimate estimate = (BmsEstimate) _obj;
	        	if("N".equals(estimate.getApplyClassChangedYn())) { // Y : 이미 정산서가 발행된 상태이고 그때 기존요율을 바꿨을때, N:아닐때
	        		log.info("BMS_DISPOSAL, BMS_ESTIMATE not regist bmsEcmPod");
	                return;
	        	}
	        	podName = "창고료수정";
        	}
        }
        BmsEcmPod pod = new BmsEcmPod();
        pod.setEntCode(processLogs.getEntCode());
       // pod.setEntBusinessCode(processLogs.getEntBusinessCode());
        pod.setPodJobNo(processLogs.getReferenceNoId());
        pod.setOdJobNo(processLogs.getJobNo());
        pod.setRemarks(deleteReason);
        pod.setPodStatus(podStatus);
        
         if(BmsConsts.DbType.BMS_IN.equals(processLogs.getDbType())){
             if(BmsConsts.PodStatus.SEND.equals(podStatus)){
            	 podName ="반입전송(수기)";
            	 Map<String, Object> _objMap = (Map) _obj;
            	 if(_objMap != null && !"".equals(Util.null2str(_objMap.get("podName")))) {
            		 podName += Util.null2str(_objMap.get("podName"));
            	 }
             }
             else if(BmsConsts.PodStatus.LOCATION_SAVE.equals(podStatus)){
            	 podName ="로케이션적치(PDA)";
             }
             else if(BmsConsts.PodStatus.LOCATION_PICKING.equals(podStatus)){
            	 podName ="로케이션피킹(PDA)";
             }
             else if(BmsConsts.PodStatus.LOCATION_PICKING_BATCH.equals(podStatus)){
            	 podName ="로케이션 일괄피킹(PDA)";
             }
             else if(BmsConsts.PodStatus.INSPECT_PICKING.equals(podStatus)){
            	 podName ="검사피킹(PDA)";
             }
             else if(BmsConsts.PodStatus.BIGSIZE_UPDATE_Y.equals(podStatus)){
            	 podName ="반입(PDA거대지정)";
             }
             else if(BmsConsts.PodStatus.BMS_IN_CHANGE_CUSTOMS.equals(podStatus)){
            	 podName ="반입정정수정";
             }
             else if(BmsConsts.PodStatus.CUSTOM_POD_NAME.equals(podStatus)){
            	 podName = Util.null2str(_obj);
             }
             else {
                 podName = getBmsInPodName(podStatus, (CarryIn) _obj, podName, pod);
             }
         }
         else if(BmsConsts.DbType.BMS_IN_PLAN.equals(processLogs.getDbType())) {
        	 if(BmsConsts.PodStatus.INSPECTION_FINISH_YN.equals(podStatus)){
            	 podName =  Util.null2str(_obj) + "(PDA)";
             }else {
            	 podName = getBmsInPlanPodName(podStatus, podName, pod);
             }
         }
         else if(BmsConsts.DbType.BMS_CHANGE_IN.equals(processLogs.getDbType())) {
        	 if(BmsConsts.PodStatus.SEND.equals(podStatus)){
            	 podName ="반입정정전송";
             }
             else {
            	 podName = getBmsChangeInPodName(podStatus, (CarryInChange) _obj, podName, pod);
             }
         }
         else if(BmsConsts.DbType.BMS_CHANGE_OUT.equals(processLogs.getDbType())) {
        	 if(BmsConsts.PodStatus.SEND.equals(podStatus)){
            	 podName ="반출정정전송";
             }
             else {
            	 podName = getBmsChangeOutPodName(podStatus, (CarryOutChange) _obj, podName, pod);
             }
         }
         else if(BmsConsts.DbType.BMS_ABNORMAL.equals(processLogs.getDbType())) {
             podName = getBmsAbnormalPodName(podStatus, podName, pod);
         }
         else if(BmsConsts.DbType.BMS_ABNORMAL_CORR.equals(processLogs.getDbType())) {
             podName = getBmsAbnormalCorrPodName(podStatus, podName, pod);
         }
         else if(BmsConsts.DbType.BMS_OUT.equals(processLogs.getDbType())){
             if( BmsConsts.PodStatus.BATCH.equals(podStatus)){
            	 BmsOut out = (BmsOut) _obj;
            	 if(out != null && "70".equals(out.getOutClass())) {
            		 podName ="통과반출(일괄생성)";
            	 }else {
            		 podName ="이고반출(일괄생성)";
            	 }
             }
             else if(BmsConsts.PodStatus.SEND.equals(podStatus)){
            	 podName ="반출전송(수기)";
             }
             else if(BmsConsts.PodStatus.OUT_SCAN.equals(podStatus)) {
            	 podName = "출고SCAN수기저장";
             }else if(BmsConsts.PodStatus.CUSTOM_POD_NAME.equals(podStatus)){
            	 podName = Util.null2str(_obj);
             }
             else {
                 podName = getBmsOutPodName(podStatus, (BmsOut) _obj, podName, pod);
             }
         }
         else if(BmsConsts.DbType.BMS_OUT_NOTICE.equals(processLogs.getDbType())) {
        	 podName = getBmsOutNoticePodName(podStatus, podName, pod);
         }
         else if(BmsConsts.DbType.BMS_DISPOSAL.equals(processLogs.getDbType())) {
        	 podName = getBmsDisposalPodName(podStatus, podName, pod);
         }
         else if(BmsConsts.DbType.BMS_OUT_PLAN.equals(processLogs.getDbType())) {
        	 podName = getBmsOutPlanPodName(podStatus, (BmsOutPlan) _obj, podName, pod);
         }
         else if(BmsConsts.DbType.BMS_LOCAL_INSPECT_RESULT.equals(processLogs.getDbType())) {
        	 podName = getBmsLocalInspectResultPodName(podStatus, (BmsLocalInspectResult) _obj, podName, pod);
         }
         else if(BmsConsts.DbType.BMS_OUT_SAMPLE.equals(processLogs.getDbType())) {
        	 podName = getBmsOutSamplePodName(podStatus, podName);
         }else if(BmsConsts.DbType.BMS_IN_PLAN_PKG.equals(processLogs.getDbType())) {
        	 if(BmsConsts.PodStatus.DELETE.equals(podStatus)) {
        		 podName = "박스삭제";
        	 }
         }else if(BmsConsts.DbType.BMS_TIC_PALI.equals(processLogs.getDbType())) {
        	 podName = getBmsTicPaliPodName(podStatus, podName);
         }else if(BmsConsts.DbType.BMS_TIC_MOVE_SCAN.equals(processLogs.getDbType())) {
        	 if(BmsConsts.PodStatus.PDA_TIC_IN_CHECK.equals(podStatus)){
            	 podName ="이고반입입고(PDA)";
             }
         }else if(BmsConsts.DbType.BMS_CARGO.equals(processLogs.getDbType())) {
        	 if(BmsConsts.PodStatus.BMS_CARGO_RESTOCK.equals(podStatus)) {
        		 podName ="출고후재입고(PDA)";
        	 }
         }else if(BmsConsts.DbType.BMS_STOCK_SCAN.equals(processLogs.getDbType())) {
        	 if(BmsConsts.PodStatus.BMS_STOCK_SCAN.equals(podStatus)) {
        		 podName ="재고실사(PDA)";
        	 } else if(BmsConsts.PodStatus.BMS_STOCK_SCAN_START.equals(podStatus)) {
        		 podName ="재고실사시작(PDA)";
        	 } else {
        		 podName ="재고실사종료(PDA)";
        	 }
	 	 } else if(BmsConsts.DbType.BMS_INSPECT_RESULT.equals(processLogs.getDbType())) {
	 		podName = "검사사유관리(PDA)";
		 } else if(BmsConsts.DbType.BMS_ABNORMAL_RESULT.equals(processLogs.getDbType())) {
			podName = "하기결과이상보고(관세청)";
         }

        pod.setPodName(podName);
      //  pod.setBlNo(blNo);
        pod.setRegisterUserId(processLogs.getProcessUserId());
        // 테이블 입력 처리
        bmsCommMapper.insertBmsEcmPod(pod);
       // log.info ( "TB_ECM_POD_{} insert   blNo:{}, odJobNo:{}"  , processLogs.getReferenceNoId(), blNo, pod.getOdJobNo());
        log.info ( "TB_ECM_POD_05 insert  podJobNo:{}, odJobNo:{}, podName:{}"  , processLogs.getReferenceNoId(),  pod.getOdJobNo(), podName);

    }

    private static String getBmsOutPodName(String podStatus, BmsOut _obj, String podName , BmsEcmPod pod) {
        switch (podStatus){
            case BmsConsts.PodStatus.INSERT:
                BmsOut out = _obj;
                String outClass = out.getOutClass();
                podName ="기타반출";
                if(outClass.equals("50")){
                    podName ="통관반출";
                } else if (outClass.equals("53")){
                    podName ="목록반출";
                } else if (outClass.equals("55")){
                    podName ="선용품반출";
                } else if (outClass.equals("60")){
                    podName ="보운반출";
                } else if (outClass.equals("61")){
                    podName ="이고반출";
                } else if (outClass.equals("63")){
                    podName ="BL분할반출";
                } else if (outClass.equals("70")){
                    podName ="통과반출";
                } else if (outClass.equals("71")){
                    podName ="반송반출";
                } else if (outClass.equals("74")){
                    podName ="카르네반출";
                } else if (outClass.equals("80")){
                    podName ="멸각폐기반출";
                } else if (outClass.equals("82")){
                    podName ="국고귀속반출";
                } else if (outClass.equals("83")){
                    podName ="체화폐기반출";
                }else {
                    podName ="기타반출";
                }
                // podDate,와 podTime 은 carryoutDate, carroutTime 값을 가지고 간다. 배치 일때는 현재 일자가 들어 감
                pod.setPodDate(out.getCarryoutDate());
                pod.setPodTime(out.getCarryoutTime());
                break;
            case BmsConsts.PodStatus.UPDATE:
                podName ="반출수정(수기)";
                break;
            case BmsConsts.PodStatus.DELETE:
                podName ="반출삭제(수기)";
                break;
            case BmsConsts.PodStatus.BATCH:
                podName ="이고반출";  //61.이고반출 만들어옴
                break;
            default:
                break;
        }
        return podName;
    }

    private static String getBmsInPodName(String podStatus, CarryIn _obj, String podName, BmsEcmPod pod) {
        switch (podStatus){
            case BmsConsts.PodStatus.INSERT:
                CarryIn carryIn = _obj;
                String inClass = carryIn.getInClass();
                podName ="반입등록";
                if("10".equals(inClass)){
                    podName ="입항반입";
                }else if("11".equals(inClass)){
                    podName ="무적반입";
                }else if("19".equals(inClass)){
                    podName ="환적반입";
                }else if("20".equals(inClass)){
                    podName ="보운반입";
                }else if("21".equals(inClass)){
                    podName ="이고반입";
                }else if("23".equals(inClass)){
                    podName ="BL분할반입";
                }else {
                    podName ="기타반입";
                }
                // podDate,와 podTime 은 carryoutDate, carroutTime 값을 가지고 간다. 배치 일때는 현재 일자가 들어 감
                pod.setPodDate(carryIn.getCarryinDate());
                pod.setPodTime(carryIn.getCarryinTime());
                break;
            case BmsConsts.PodStatus.UPDATE:
            	if("10".equals(_obj.getInClass()) && _obj.getIsPrecUpdated()) {
            		podName ="반출승인수신(PREC)";
            	}else {
            		podName ="반입수정(수기)";
            	}  
                break;
            case BmsConsts.PodStatus.DELETE:
                podName ="반입삭제(수기)";
                break;
            case BmsConsts.PodStatus.BATCH:
                podName ="이고반입(일괄생성)"; //21.이고반입 만 들어옴
                // podDate,와 podTime 은 carryoutDate, carroutTime 값이 들어 가나 최초 입력이므로 현재 일자로 들어가서 매핑하지 않음
                break;
            case BmsConsts.PodStatus.MFN_INSERT:
                podName ="MFN(화물있으나 적하목록없음)생성";
                break;
            case BmsConsts.PodStatus.MGN_INSERT:
                podName ="MGN(적하목록있으나 화물없음)생성";
                break;
            case BmsConsts.PodStatus.IPR_UPDATE:
            	if("Y".equals(_obj.getIntellectualPropYn())) {
            		podName ="지재권지정";
            	}else {
            		podName ="지재권해제";
            	}
                break;
            case BmsConsts.PodStatus.BIGSIZE_UPDATE:
            	if("Y".equals(_obj.getBigSizeYn())) {
            		podName ="거대화물지정";
            	}else {
            		podName ="거대화물해제";
            	}
                break;
            case BmsConsts.PodStatus.ISPTCUSTOMS_UPDATE:
            	if("Y".equals(_obj.getInspectionCustomsYn())) {
            		podName ="집중검사지정";
            	}else {
            		podName ="집중검사해제";
            	}
            	break;
            default:
                break;
        }
        return podName;
    }

    private static String getBmsInPlanPodName(String podStatus, String podName, BmsEcmPod pod) {
        switch (podStatus){

            case BmsConsts.PodStatus.DELETE:
                podName ="반입예정삭제";
                break;
            case BmsConsts.PodStatus.BIGSIZE_UPDATE_Y:
                podName ="거대지정(PDA)";
                break;
            case BmsConsts.PodStatus.BIGSIZE_UPDATE_N:
                podName ="거대지정취소(PDA)";
                break;
            default:
                break;
        }
        return podName;
    }

    private static String getBmsChangeInPodName(String podStatus, CarryInChange _obj, String podName, BmsEcmPod pod) {
        switch (podStatus){
            case BmsConsts.PodStatus.INSERT:
                CarryInChange carryInChange = _obj;
                String changeClass = carryInChange.getChangeClass();
                podName ="반입정정등록";
                if("01".equals(changeClass)){
                    podName ="반입정정(항목정정)";
                }else if("03".equals(changeClass)){
                    podName ="반입정정(내역삭제)";
                }else {
                    podName ="반입정정등록";
                }
                break;
            case BmsConsts.PodStatus.DELETE:
                podName ="반입정정삭제(수기)";
                break;
            case BmsConsts.PodStatus.UPDATE:
                podName ="반입정정수정(수기)";
                break;
            default:
                break;
        }
        return podName;
    }

    private static String getBmsChangeOutPodName(String podStatus, CarryOutChange _obj, String podName, BmsEcmPod pod) {
        switch (podStatus){

            case BmsConsts.PodStatus.INSERT:
                CarryOutChange carryOutChange = _obj;
                String changeClass = carryOutChange.getChangeClass();
                podName ="반출정정등록";
                if("01".equals(changeClass)){
                    podName ="반출정정(항목정정)";
                }else if("03".equals(changeClass)){
                    podName ="반출정정(내역삭제)";
                }else {
                    podName ="반출정정등록";
                }
                break;
            case BmsConsts.PodStatus.DELETE:
                podName ="반출정정삭제(수기)";
                break;
            case BmsConsts.PodStatus.UPDATE:
                podName ="반출정정수정(수기)";
                break;
            default:
                break;
        }
        return podName;
    }

    private static String getBmsAbnormalPodName(String podStatus, String podName, BmsEcmPod pod) {
        switch (podStatus){

            case BmsConsts.PodStatus.INSERT:
                podName ="반입물품이상저장";
                break;
            case BmsConsts.PodStatus.DELETE:
                podName ="반입물품이상삭제(수기)";
                break;
            case BmsConsts.PodStatus.UPDATE:
                podName ="반입물품이상수정(수기)";
                break;
            default:
                break;
        }
        return podName;
    }

    private static String getBmsAbnormalCorrPodName(String podStatus, String podName, BmsEcmPod pod) {
        switch (podStatus){

            case BmsConsts.PodStatus.INSERT:
                podName ="반입이상정정등록";
                break;
            case BmsConsts.PodStatus.DELETE:
                podName ="반입물품이상정정삭제(수기)";
                break;
            case BmsConsts.PodStatus.UPDATE:
                podName ="반입물품이상정정수정(수기)";
                break;
            default:
                break;
        }
        return podName;
    }

    private static String getBmsOutNoticePodName(String podStatus, String podName, BmsEcmPod pod) {
    	switch (podStatus) {
	    	case BmsConsts.PodStatus.INSERT: /** 사용자 수기 반출통고최초저장 */
	    		podName ="반출통고저장";
	    		break;
	    	case BmsConsts.PodStatus.UPDATE: /** 사용자가 반출통고 수정시 */
	    		podName ="반출통고수정(수기)";
	    		break;
	    	case BmsConsts.PodStatus.DELETE: /** 사용자가 반출통고 삭제시 */
	    		podName ="반출통고삭제(수기)";
	    		break;
	    	case BmsConsts.PodStatus.SEND: /** 반출통고문서 전송 */
	    		podName ="반출통고전송";
	    		break;
	    	default:
	    		break;
    	}
    	return podName;
    }
    
    private static String getBmsDisposalPodName(String podStatus, String podName, BmsEcmPod pod) {
    	switch (podStatus) {
	    	case BmsConsts.PodStatus.INSERT: /** 사용자 수기 폐기등록최초저장 */
	    		podName ="폐기저장(수기)";
	    		break;
	    	case BmsConsts.PodStatus.UPDATE: /** 사용자가 폐기등록 수정시 */
	    		podName ="폐기수정(수기)";
	    		break;
	    	case BmsConsts.PodStatus.DELETE: /** 사용자가 폐기등록 삭제시 */
	    		podName ="폐기수정(삭제)";
	    		break;
	    	case BmsConsts.PodStatus.BATCH: /** Function 첫번째 combo 재고에서 폐기화물일괄생성 */
	    		podName ="폐기저장(일괄)";
	    		break;
	    	case BmsConsts.PodStatus.BATCH_02: /** Function 첫번째 combo 폐기에서 폐기화물일괄반출생성 => insertOutBatchGen에서 저장중 */
	    		podName ="체화폐기반출(일괄)";
	    		break;
	    	default:
	    		break;
    	}
    	return podName;
    }
    
    private static String getBmsOutPlanPodName(String podStatus, BmsOutPlan _obj, String podName, BmsEcmPod pod) {
    	switch (podStatus){
	        case BmsConsts.PodStatus.UPDATE:
	        	BmsOutPlan bmsOutPlan = _obj;
	            String outScanDateTime = bmsOutPlan.getOutScanDateTime();
	            if(!"".equals(outScanDateTime)) {
	            	podName ="출고확정";
	            }
	            break;
	        case BmsConsts.PodStatus.BMS_OUT_RELEASE:
                podName ="출고(PDA)";
                break;
	        case BmsConsts.PodStatus.BMS_OUT_CANCLE:
	        	podName ="출고(PDA) 취소";
	        	break;
	        default:
	            break;
	    }
	    return podName;
    }
    
    private static String getBmsLocalInspectResultPodName(String podStatus, BmsLocalInspectResult _obj, String podName, BmsEcmPod pod) {
    	switch (podStatus){
	        case BmsConsts.PodStatus.UPDATE:
	        	BmsLocalInspectResult inspectResult = _obj;
	            String ticOutScanUser = inspectResult.getTicOutScanUserId();
	            String ticComeBackScanUser = inspectResult.getTicComeBackScanUserId();
	            if(!"".equals(ticOutScanUser)) {
	            	podName ="이고반출 출고확인";
	            } else if(!"".equals(ticComeBackScanUser)) {
	            	podName ="이고반입 입고확인";
	            } else if("Y".equals(inspectResult.getInspectionFinishYn())) {
	            	podName ="검사완료 PDA";
	            }
	            break;
	        default:
	            break;
	    }
	    return podName;
    }
    
    private static String getBmsOutSamplePodName(String podStatus, String podName) {
    	switch (podStatus) {
	    	case BmsConsts.PodStatus.SAMPLE_SIGNATURE_SAVE:
	    		podName = "견품반출출고(PDA)";
	    		break;
	    	case BmsConsts.PodStatus.SAMPLE_TAKING_OUT_IN:
	    		podName = "견품반출후입고(PDA)";
	    		break;
    		case BmsConsts.PodStatus.INSERT:
    			podName = "견품반출저장";
    			break;
	        case BmsConsts.PodStatus.UPDATE:
	        	podName = "견품반출저장";
	            break;
	        case BmsConsts.PodStatus.DELETE:
	        	podName = "견품반출삭제";
	            break;
	        default:
	            break;
	    }
	    return podName;
    }

    private static String getBmsTicPaliPodName(String podStatus, String podName) {
    	switch (podStatus) {
    		case BmsConsts.PodStatus.BMS_TIC_PALI_CHK:
    			podName = "이고반출대상확인(PDA)";
    			break;
    		case BmsConsts.PodStatus.BMS_TIC_PALI_CFM:
    			podName = "이고반출확정(PDA)";
    			break;
    	default:
    			break;
    	}
    	return podName;
    }
}
