package org.yht.exps.express.service.edi;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.eCommerce.order.OrderPodUpdate;
import org.yht.exps.express.model.eCommerce.order.OrderPodUpdate.Pod;
import org.yht.exps.express.model.edi.EcmCargoProgressInfo;
import org.yht.exps.express.model.edi.EcmCargoProgressInfo.CargCsclPrgsInfo;
import org.yht.exps.express.model.edi.EcmCargoProgressInfo.CargCsclPrgsInfoDtail;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class CargoProgressParser {
	@Autowired
	ManifestMapper manifestMapper;
	
    @Value("${custom.unipass-base-url}")
    private String unipassBaseUrl_;
    
    @Value("${custom.unipass-port}")
    private int unipassPort_;
    
    @Value("${custom.unipass-cargo-progress-path}")
    private String unipassCargoProgressPath_;
    
    @Value("${custom.unipass-cargo-progress-key}")
    private String unipassCargoProgressKey_;
	
	private enum Status_ {
//		ECM070("입항보고수리"),	  항공은 입항보고수리  /  해상은 하선신고수리
		ECM210("반입신고"),
        ECM220("통관목록보류"),
        ECM230("통관목록심사취소"),
        ECM240("통관목록심사완료"), 
        ECM250("수입신고"),
        ECM260("수입(사용소비)심사진행"),
        ECM270("수입(사용소비)결재통보"),
        ECM280("수입신고수리"),
        ECM281("수입신고수리취소"),
        ECM282("수입신고각하"),
        ECM283("보완요구"),
        ECM290("반출신고"),
        ECM300("반출신고취소");
		
        private String value_;
 
        private Status_(String value) {
                this.value_ = value;
        }
	}
	
	/**
	 * 화물진행정보 Parser
	 * @param data
	 * @param entBusinessCode
	 */
	public void getCargoProgressInfo(Map<String, Object> data, String entBusinessCode) throws Exception {
		long sTime = System.currentTimeMillis();
		try {
			
			OrderPodUpdate updateFields = new OrderPodUpdate();
			EcmCargoProgressInfo cargoProgressInfo = new EcmCargoProgressInfo();
			//boolean isLastStatus = false; //반출완료
			ArrayList<Pod> podList = new ArrayList<Pod>(); 
			
			URI uri = null;
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			
			String bound = Util.null2str(data.get("BOUND"));			// 항공, 해상 구분
			String customs = Util.null2str(data.get("CUSTOMS_CODE"));		// 세관코드(3자리)
			String status = Util.null2str(data.get("LAST_POD_STATUS"));		// 통관진행상태
			String odJobNo = Util.null2str(data.get("OD_JOB_NO"));		// Order Job No
			String masterOdJobNo = Util.null2str(data.get("MASTER_OD_JOB_NO"));		// master Order Job No
			String mblNo = Util.null2str(data.get("MBL_NO")).trim();			// MBL No
			String hblNo = Util.null2str(data.get("HBL_NO"));			// HBL No
			String destCode = Util.null2str(data.get("DEST_CODE")); //도착지 
			String destPort = Util.null2str(data.get("DEST_PORT")); //목적항 
			String arDate = Util.null2str(data.get("AR_DATE")); //도착일자 
			String cdType = Util.null2str(data.get("CD_TYPE")); //통관여부
			String dropYn = Util.null2str(data.get("DROP_YN"), "N"); // N : 취하 X, C: 반출신고 후 취하된 건 , Y: 반출취소 없이 보류된 건
			String bondedCargoYn = !destCode.equals(destPort) ? "Y" : "N";	/** 보세화물여부(도착지와 목적항이 다른경우) */
//			String action = Util.null2str(data.get("action"));	/** 수입화물진행정보실행구분 (sendGw/insertCargoProgress/insertCargoProgressHouse/ediGovRecv) */
			String blYy = Util.dateStrToStr(arDate, "yyyy");
			String mrn = Util.null2str(data.get("MRN"));
			if(!"".equals(mrn) && mrn.length() > 1){
				blYy = "20" + mrn.substring(0, 2);
			}
			String processLastDateTime = "";
			
			//취하여부
			boolean isDrop = "N".equals(dropYn)?false:true;
			
			
			/** arDate가 없을경우(오류?) 처리하지않음 */
			if(arDate.equals("")){
				String arDateNullLogVal = "arDate Null ::::::: " + hblNo+ " / status: "+status+" cdType : "+cdType+" bound: "+ bound+" isDrop: "+isDrop+" entBus : "+entBusinessCode;
				log.info(arDateNullLogVal);
				return;
			}
			
			//ES에서 삭제하는 플래그 
			boolean isEnd = false;
			//마지막 상태인지 확인하는 플래그 
			boolean isLast = false;
			
			/** 도착지와 목적항이 다르면 보세운송     보세운송일경우 두번째 반입신고/반출신고만 처리 */
			/** 취하건일경우 반출신고가 두번되면 두번째 반출신고만 처리 */
			boolean carryinFlag = true;
			boolean carryoutFlag = true;
			if(!destCode.equals("") && !destPort.equals("") && !destCode.equals(destPort)) carryinFlag = false;
			if(!carryinFlag) carryoutFlag = false;
			
			//이고반입or이고반출이 있는 건인지 확인하는 플래그
			boolean moveWarehouse = false;
			
			updateFields.setSrchOdJobNo(odJobNo);
			updateFields.setMasterOdJobNo(masterOdJobNo);
			updateFields.setMblNo(mblNo);
			updateFields.setHblNo(hblNo);
			UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
			        .scheme("https")
			        .host(unipassBaseUrl_)
			        .port(unipassPort_)
			        .path(unipassCargoProgressPath_)
			        .queryParam("crkyCn", URLDecoder.decode(unipassCargoProgressKey_, "UTF-8"));

			if (!"".equals(mblNo))	uriComponentsBuilder.queryParam("mblNo", mblNo.toUpperCase());
			if (!"".equals(hblNo))	uriComponentsBuilder.queryParam("hblNo", hblNo.toUpperCase());
			if (!"".equals(blYy))	uriComponentsBuilder.queryParam("blYy", blYy);
			uri = uriComponentsBuilder.build().encode().toUri();
			
			cargoProgressInfo = restTemplate.getForObject(uri, EcmCargoProgressInfo.class);
			if (cargoProgressInfo != null) {
				boolean bScanStatus = false;
				// 반입신고유무
		        boolean inClearance = false;
		        // 유니패스에서 통관목록보류 상태
		        boolean holdNotice_UNIPASS = false;
		        // 통관 취소되었다가 다시 통관목록 심사완료된 경우 true;
		        boolean reCustoms = false;
		        
		        CargCsclPrgsInfo cargCsclPrgsInfoDTO = cargoProgressInfo.getCargCsclPrgsInfo();
		        
		        int ecm290Count = 0;
				if (cargCsclPrgsInfoDTO != null) {
					
					// 통관진행상태
//					String progressStatus = cargCsclPrgsInfoDTO.getPrgsStts();
//					updateFields.setProgressLastStatus(progressStatus);
//					if (progressStatus.contains("반출완료") || progressStatus.contains("반출신고") || ) isLastStatus = true;
//					
//					if ("AIR".equals(bound)) {
//			            if (isLastStatus) {
//			            	updateFields.setStrCarryIn("반입완료");
//			            	updateFields.setStrCarryOut("반출완료");
//			            }
//					}
		            
		            // 화물관리번호
//		            String manifestNo = Util.null2str(cargCsclPrgsInfoDTO.getCargMtNo());
//		            if (manifestNo.length() > 14) {
//			            updateFields.setMrn(manifestNo.substring(0, 11));
//			            updateFields.setMsn(manifestNo.substring(11, 15));
//		            }
					
					List<CargCsclPrgsInfoDtail> podHistory = cargoProgressInfo.getCargCsclPrgsInfoDtails();
		            
		            //관세사 조회시 반출신고값이 있으면 전체 상태값 delete/insert , 없으면 마지막 3건만 insert 
		            //관세청에서 list를 내림차순으로 보내줌
		            for(int i = podHistory.size()-1 ; i >= 0; i--) {
		            	String unipassStatus = podHistory.get(i).getCargTrcnRelaBsopTpcd().replaceAll(" ", "");
		            	String unipassStatusInfo = podHistory.get(i).getRlbrCn().replaceAll(" ", ""); 
		            	if(unipassStatus.equals(Status_.ECM290.value_) && !unipassStatusInfo.contains("이고")) ecm290Count++;
		            }
		            
					//if ("Y".equals(bondedCargoYn) && ecm290Count>1) isLast = true;	/** 보세화물건은 반출신고가 두번됐을때 종료 */
			        //else if(!isDrop && !"Y".equals(bondedCargoYn) && ecm290Count == 1) isLast = true;	/** 취하건도 아니고 보세화물건도 아닐 경우 반출신고가 한번됐을때 종료 */
		            
		            String[] carryinLocationArr;
		            String carryinLocation;
		            String customsHoldDateTime = "";
		            
		            for(int i = podHistory.size()-1 ; i >= 0; i--) {
		            	CargCsclPrgsInfoDtail podInfo = podHistory.get(i);
		            	boolean isSave = false;
		            	Pod pod = new Pod();
		            	
		            	String unipassStatus = Util.null2str(podInfo.getCargTrcnRelaBsopTpcd()).replaceAll(" ", "");
		            	if((unipassStatus.equals("하선신고수리") && bound.equals("SEA")) || (unipassStatus.equals("입항보고수리") && bound.equals("AIR"))){
		            		pod.setProgressStatus("ECM070");
		            	}else{
		            		for (Status_ s: Status_.values()) {
			 		        	if (s.value_.equals(unipassStatus)){
			 		        		pod.setProgressStatus(s.toString());
			 		        		isSave = true;
			 		        		break;
			 		        	}
			 		        }
		            		
		            		if(!isSave) continue;
		            		
			            	if(("ECM240".equals(pod.getProgressStatus()) || "ECM300".equals(pod.getProgressStatus())) && isDrop){	/** 취하건은 ECM240,ECM300 POD 저장하지 않음 */
			            		continue;
			            	}
		            	}
		            	
		            	// 프로세스일시
		            	String processDateTime = Util.null2str(podInfo.getPrcsDttm());
		                if (!"".equals(processDateTime)) {
		                    if (!processDateTime.contains("-")) {
		                        if (processDateTime.length() > 12)
		                        	processDateTime = processDateTime.substring(0, 4) + "-" + 
		                        			processDateTime.substring(4, 6) + "-" + 
		                        			processDateTime.substring(6, 8) + " " + 
		                        			processDateTime.substring(8, 10) + ":" + 
		                        			processDateTime.substring(10, 12) + ":" + 
		                        			processDateTime.substring(12, 14);
		                        else
		                        	processDateTime = processDateTime.substring(0, 4) + "-" + 
		                        			processDateTime.substring(4, 6) + "-" + 
		                        			processDateTime.substring(6, 8) + " " + 
		                        			processDateTime.substring(8, 10) + ":" + 
		                        			processDateTime.substring(10, 12);
		                        
		                        pod.setProcessDateTime(processDateTime);
		                    }
		                }
		                
		                /** 2022-12-21 반출입신고번호 사용안함 */
//		                // 반출입신고번호
//		                // 해상일 경우 특정 세관일 경우에만 반출입 정보를 업데이트 한다.
//		                if ("AIR".equals(bound)) {
//		                	updateFields.setCarryNo(Util.null2str(podInfo.getDclrNo()));
//		                }
		            	
		                /** 2022-12-21 반출입일시 사용하지않고 처리일시로 사용 */
		                // 반출입일자
//		                String carryInOutDateTime = Util.null2str(podInfo.getRlbrDttm());
//		                if (!"".equals(carryInOutDateTime)) {
//		                    if (!carryInOutDateTime.contains("-")) {
//		                        if (carryInOutDateTime.length() > 12)
//		                        	carryInOutDateTime = carryInOutDateTime.substring(0, 4) + "-" + 
//		                        			carryInOutDateTime.substring(4, 6) + "-" + 
//		                        			carryInOutDateTime.substring(6, 8) + " " + 
//		                        			carryInOutDateTime.substring(8, 10) + ":" + 
//		                        			carryInOutDateTime.substring(10, 12) + ":" + 
//		                        			carryInOutDateTime.substring(12, 14);
//		                        else
//		                        	carryInOutDateTime = carryInOutDateTime.substring(0, 4) + "-" + 
//		                        			carryInOutDateTime.substring(4, 6) + "-" + 
//		                        			carryInOutDateTime.substring(6, 8) + " " + 
//		                        			carryInOutDateTime.substring(8, 10) + ":" + 
//		                        			carryInOutDateTime.substring(10, 12);
//		                    }
//		                }
		                
		                switch(unipassStatus) {
			                case "반입신고":
		            			if ("AIR".equals(bound)) {
		            				if(Util.null2str(podInfo.getRlbrCn()).contains("이고")) {	/** 이고반입인 경우 반입처리하지 않고 POD만 저장 */
		            					moveWarehouse = true;
		            				}else {
		            					updateFields.setCarryInDateTime(processDateTime);
		            					inClearance = true;
		            				}
		            				podList.add(pod);
		            			} else if("SEA".equals(bound)){
		            				if(carryinFlag){
			        					if(!"0".equals(podInfo.getPckGcnt())){	/** 보세운송 반입건에 대해서 무적화물인 경우 수량이 0이고 이 경우 반입처리하지않음 */
			        						if(Util.null2str(podInfo.getRlbrCn()).contains("이고")) {	/** 이고반입인 경우 반입처리하지 않고 POD만 저장 */
			        							moveWarehouse = true;
			        						}else {
			        							updateFields.setCarryInDateTime(processDateTime);
			        							inClearance = true;
			        						}
				        					podList.add(pod);
			        					}
			        				}else{
			        					carryinFlag = true;
			        				}
		            			}
		                        break;
			                case "반출신고":
		            			// 취하Y & 통관목록보류Y & 보세N 일 때, 반출 INSERT
	            				if(isDrop && !"Y".equals(bondedCargoYn)) {
	            					carryoutFlag = !"".equals(customsHoldDateTime); // 목록보류일자가 담겨있을 때 반출처리
	            				    if (carryoutFlag) {
	            				        updateFields.setCustomsHoldDateTime(customsHoldDateTime);
	            				        isLast = true;
	            				    }
	            				}
		            			if(carryoutFlag){
		            				if(Util.null2str(podInfo.getRlbrCn()).contains("이고")) { /** 이고반출인 경우 반출처리하지 않고 POD만 저장 */
		            					moveWarehouse = true;
		            				}else {
		            					processLastDateTime = processDateTime;
		            					updateFields.setCarryOutDateTime(processDateTime);
		            					isLast = true;
		            				}
			                    	if(!"".equals(customsHoldDateTime)) {
			                    		updateFields.setCustomsHoldDateTime(customsHoldDateTime);
		                    		}
			                    	podList.add(pod);
		                    	}else{
		                    		carryoutFlag = true;
		                    	}
//		            			if(!"".equals(processLastDateTime))processLastDateTime = processDateTime;
		            			/** 2022-12-21 사용안함 */
//		            			// 해상 폐기반출
//		            			if ("SEA".equals(bound)) {
//		            				String temp = Util.null2str(podInfo.getRlbrCn());
//		                            if (temp.contains("폐기") || temp.contains("보세운송 반출"))	updateFields.setStrWhOutdestroy("폐기반출");
//		            			}
		            			 
		            			break;
		            		
			                case "수입신고수리":
		            			bScanStatus = true;
//		            			updateFields.setImportReceivingReportDateTime(processDateTime);	/** 2022-12-21 사용안함 */
		            			updateFields.setClearanceDate(processDateTime);
		            			updateFields.setCdType(cdType);
		            			if(!"Y".equals(cdType)){
		            				/** SIMPLE_REPORT_DATE은 목록일경우 통관목록접수일자 / 목록이외일경우 수입신고수리일자 저장  */
		            				updateFields.setSimpleReportDate(processDateTime);
		            			}
		            			break;
		            			
		            		case "통관목록심사완료":
//		            			updateFields.setImportClearanceDateTime(processDateTime);	/** 2022-12-21 사용안함 */
		            			updateFields.setClearanceDate(processDateTime);
		            			break;
		            			
		            		case "수입신고":
//		            			updateFields.setCustomsNDateTime(processDateTime);	/** 2022-12-21 사용안함 */
		            			updateFields.setClearanceReceiptDate(processDateTime);
		            			break;
		            		
		            		case "통관목록보류":
//		                        updateFields.setCustomsHoldDateTime(processDateTime);
		                        customsHoldDateTime = processDateTime;
		            			break;
		            			
		            		/** 2022-12-21 사용안함 */
//		            		case "수입(사용소비)심사진행":
//		            			updateFields.setImportScreeningDateTime(processDateTime);
//		            			break;
//		            			
//		            		case "통관목록보류":
//		                        holdNotice_UNIPASS = true;
//		                        updateFields.setCustomsHoldDateTime(processDateTime);
//		            			break;
//		            			
//		            		case"통관목록심사취소":
//		            			updateFields.setCustomsCheckCancelDateTime(processDateTime);
//		            			break;
		                }
		                
		                String shedSgn = Util.null2str(podInfo.getShedSgn());
//		        		if ("AIR".equals(bound)) {
//		        			/** 장치위치 추가개발 필요(필드 생성 후 처리) */
//			            	if (inClearance) {
//			            		if (!"".equals(shedSgn)) {
//			            			updateFields.setWhInPos(shedSgn.substring(shedSgn.length() - 2, shedSgn.length()));	
//			            		}
//			            	}
//		        		} else {
//		        			
		        			/** 목적지/도착항이 다를경우 보세화물로 처리 */
//		        			/** 해상-반입신고/반출신고는 보세화물일경우 두번 반복되기때문에 두번째일 경우에만 POD저장(세관코드/장치장코드로 두번째인지 체크)*/
//		        			boolean carryInOut = false;
//		        			if ("016".equals(customs) && (shedSgn.contains("01302001") || shedSgn.contains("01602003") || shedSgn.contains("03902044"))) {
//		        				carryInOut = true;
//		        			} else if ("020".equals(customs) && (shedSgn.contains("02002079") || shedSgn.contains("01302001") || shedSgn.contains("03902044"))) {
//		        				carryInOut = true;
//		        			} else if (shedSgn.contains("02002079") || shedSgn.contains("01302001") || shedSgn.contains("01602003") || shedSgn.contains("03902044")) {
//		        				carryInOut = true;
//		        			}
//		        			if (carryInOut) {
//			                    if ("반입신고".equals(unipassStatus)) {
//			                    	if(!"0".equals(podInfo.getPckGcnt())){	/** 보세운송 반입건에 대해서 무적화물인 경우 수량이 0이고 이 경우 반입처리하지않음 */
//			                    		updateFields.setCarryInDateTime(processDateTime);
//			                    		podList.add(pod);
//			                    		inClearance = true;
//			                    		
//			                    		/** 2022-12-21 사용안함 */
////				                    	updateFields.setCarryNo(Util.null2str(podInfo.getDclrNo()));
////				                    	updateFields.setStrCarryIn("반입완료");
//			                    	}
//			                    } else if ("반출신고".equals(unipassStatus)) {
//			                    	updateFields.setCarryOutDateTime(processDateTime);
//			                    	podList.add(pod);
//			                    	
////			                    	updateFields.setStrCarryOut("반출완료");	/** 2022-12-21 사용안함 */
//			                    }
//		        			}
		            		/* 2024.07.25 소스 위로 이동하여 사용안함 주석처리 */
//		        			if ("반입신고".equals(unipassStatus)) {
//		        				if(carryinFlag){
//		        					if(!"0".equals(podInfo.getPckGcnt())){	/** 보세운송 반입건에 대해서 무적화물인 경우 수량이 0이고 이 경우 반입처리하지않음 */
//			        					updateFields.setCarryInDateTime(processDateTime);
//			                    		podList.add(pod);
//			                    		inClearance = true;
//		        					}
//		        				}else{
//		        					carryinFlag = true;
//		        				}
//		                    } else if ("반출신고".equals(unipassStatus)) {
//		                    	/** 취하건은 취하(목록보류) 후의 반출신고만 처리 */
//		                    	if(isDrop && !"Y".equals(bondedCargoYn)) {
//		                    		if("".equals(customsHoldDateTime)) {
//		                    			carryoutFlag = false;
//		                    		}else {
//		                    			updateFields.setCustomsHoldDateTime(customsHoldDateTime);
//		                    			isLast = true;
//		                    		}
//		        		        }
//		                    	
//		                    	if(carryoutFlag){
//		                    		processLastDateTime = processDateTime;
//			                    	updateFields.setCarryOutDateTime(processDateTime);
//			                    	if(!"".equals(customsHoldDateTime)) {
//			                    		updateFields.setCustomsHoldDateTime(customsHoldDateTime);
//		                    		}
//			                    	podList.add(pod);
//		                    	}else{
//		                    		carryoutFlag = true;
//		                    	}
//		                    }
//		        			
//		        		}
		                
		                if (inClearance) {
		        			/** 반입위치 */
	            			carryinLocationArr = shedSgn.split("/");
	            			if(carryinLocationArr.length == 2){
	    	            		carryinLocation = carryinLocationArr[1];
	    	            		if(carryinLocation.length() > 1){
	    	            			updateFields.setWhInPos(carryinLocation.substring(carryinLocation.length() - 2));
	    	            		}
	    	            	}
		        		}
		                
		                /** 해상-반입신고/반출신고 이외건 처리 */
		        		//2023.03.21 ECM070 입항 POD 생성은 MRN/MSN 업데이트 배치에서 같이 해주게 수정하므로 주석처리
		        		//해상의 반입신고(ECM210)/반출신고(ECM290)는 보세운송일경우 별도처리 필요해서 상단에서 처리함
		        		//2024.07.25 항공의 반입신고(ECM210)/반출신고(ECM290) 위에서 별도 처리
		        		if((!"ECM210".equals(pod.getProgressStatus()) && !"ECM290".equals(pod.getProgressStatus()) && !"ECM070".equals(pod.getProgressStatus()))){
		        			podList.add(pod);
		        		}
		        		
		        		/** 두번째 ECM290만 저장하기위해 리스트를 오름차순으로 처리되도록 변경함     하단에서 podList가 4개 이상일경우 마지막4개만 저장되도록 처리 */
//		        		/** 짧은시간에 관세청에 신규 상태가 여러개 등록될경우가 있어서 마지막상태부터 3개까지 POD 저장(화물진행정보가 끝나지 않은 데이터) */
//		            	if(!isLast && podList.size() > 3 && !action.equals("insertCargoProgressHouse")){
//		            		break;	
//		            	}
		            }
		            
		            /** 반출이 1개면 반출 플래그 true */
		            if(ecm290Count == 1) {
		            	updateFields.setEcm290Flag(true);
		            }
				}
				
				int podSize = podList.size();
				if(!podList.isEmpty()) {
					updateFields.setPodList(podList);
					if(!isLast) {
						String tmpProcessLastStatus = "";
						String tmpProcessLastDateTime = "";
						for(Pod pod : podList) {
							if(pod.getProgressStatus().equals(Status_.ECM290.toString())){ // 화물진행정보가 끝나지 않았는데 담겨있는 반출은 이고반출이고 이고반출은 POD만 저장 & 진행상태에 반영하지 않음
								continue;
							}
							tmpProcessLastStatus = pod.getProgressStatus();
							tmpProcessLastDateTime = pod.getProcessDateTime();
						}
						updateFields.setProgressLastStatus(tmpProcessLastStatus);
						updateFields.setProcessLastDateTime(tmpProcessLastDateTime);
					}else {
						updateFields.setProgressLastStatus(Status_.ECM290.toString());
						updateFields.setProcessLastDateTime(processLastDateTime);
					}
				}
				
				/**
				 * Update할 Field
		        	WH_CarryIn(strCarryIn) 									반입상태
		        	WH_CarryOut(strCarryOut) 								반출상태
		        	LAST_POD_STATUS(clearanceProgressStatus)				화물진행정보상태
		        	CARRYIN_DATE(carryInDateTime)							반입일시
		        	CARRYOUT_DATE(carryOutDateTime)							반출일시
		        	LAST_POD_DATE(processDateTime)							화물진행정보상태일시
		        	CLEARANCE_DATE(clearanceDate)							통관완료일
		        	CLEARANCE_YN											통관여부
		        	CLEARANCE_RECEIPT_DATE(ClearanceReceiptDate)			통관접수일
		        	CarryOutNo(carryNo)										반출신고번호
		        	WH_CarryNo(carryNo)										반입신고번호
		        	WhInPos(whInPos)										장치장위치
				 **/
			/*	
		       //통관목록보류일자보다 통관목록심사취소일자가 더 이후에 발생하는 경우에는 통관완료처리 한다.
		        if (!"".equals(Util.null2str(updateFields.getCustomsCheckCancelDateTime())) && 
		        		!"".equals(Util.null2str(updateFields.getCustomsHoldDateTime()))) {
		        	
		        	String format = "yyyy-MM-dd HH:mm:ss";
		        	if (Util.null2str(updateFields.getCustomsCheckCancelDateTime()).length() == 16)	format = "yyyy-MM-dd HH:mm";
		        	
		        	Date date1 = DateTime.str2date(updateFields.getCustomsCheckCancelDateTime(), format);
		        	Date date2 = DateTime.str2date(updateFields.getCustomsHoldDateTime(), format);
		        	
		        	if (date2.before(date1)) {
		            	reCustoms = true;
		                holdNotice_UNIPASS = false;
		            }
		        }
		        
		        // 목록취하를 받아서 통관보류로 진행되는 경우 일반수입신고로 진행이 되기때문에 일반수입신고 통관완료(수입신고수리)가 되면 통관보류 Flag는 클리어한다.
	         	if (bScanStatus)	holdNotice_UNIPASS = false;
		        
		        // 취하건은 통관목록심사완료 진행상태를 업데이트 하지 않는다.
		        if (holdNotice && Status_.ECM240.toString().equals(Util.null2str(updateFields.getProgressLastStatus())) && !reCustoms) {
		        	updateFields.setProgressLastStatus(status);
		        }
		
		        // 통관목록보류인 경우에는 반출상태를 삭제한다.
		        if (holdNotice_UNIPASS) {
		        	updateFields.setStrCarryOut("");
		        	updateFields.setCarryOutDateTime("");
		        }
			  */
				// DB Update를 위해 Redis에 Push
				// 보세화물이 취하될 경우 저장되어 있던 반입/반출 지우고 화물진행정보 돌아 반입/반출 다시 저장하기 위해 조건 추가
				if (podSize > 0) {
					if(!status.equals(Util.null2str(updateFields.getProgressLastStatus())) || (bondedCargoYn.equals("Y") && isDrop)) {
						data.put("orderPodUpdate", updateFields);
					}else {
						// 수입신고~보완요구 POD 처리
						updateFields = new OrderPodUpdate();
						updateFields.setSrchOdJobNo(odJobNo);
						updateFields.setMasterOdJobNo(masterOdJobNo);
						updateFields.setMblNo(mblNo);
						updateFields.setHblNo(hblNo);
						
						Map<String, Object> tmpMap = manifestMapper.selectClearanceDate(odJobNo);
						for(int i = podList.size()-1; i >= 0; i--) {
							if(podList.get(i).getProgressStatus().equals(Status_.ECM260.toString()) ||
							   podList.get(i).getProgressStatus().equals(Status_.ECM270.toString()) ||
							   podList.get(i).getProgressStatus().equals(Status_.ECM281.toString()) ||
							   podList.get(i).getProgressStatus().equals(Status_.ECM282.toString()) ||
							   podList.get(i).getProgressStatus().equals(Status_.ECM283.toString())){
								continue;
							}else if(podList.get(i).getProgressStatus().equals(Status_.ECM250.toString())) {
								if(Util.isEmpty(tmpMap) || "".equals(Util.null2str(tmpMap.get("CLEARANCE_RECEIPT_DATE")))) {
									updateFields.setClearanceReceiptDate(podList.get(i).getProcessDateTime());
								}else {
									podList.remove(i);
								}
							}else if(podList.get(i).getProgressStatus().equals(Status_.ECM280.toString())){
								if(Util.isEmpty(tmpMap) || "".equals(Util.null2str(tmpMap.get("CLEARANCE_DATE")))) {
									updateFields.setClearanceDate(podList.get(i).getProcessDateTime());
									updateFields.setCdType(cdType);
									if(!"Y".equals(cdType)){
										updateFields.setSimpleReportDate(podList.get(i).getProcessDateTime());
									}
								}else {
									podList.remove(i);
								}
							}else {
								podList.remove(i);
							}
						}
						
						if(podList.size() > 0) {
							updateFields.setPodList(podList);
							data.put("orderPodUpdate", updateFields);
						}
					}
		        }
			}
			
			long eTime = System.currentTimeMillis() - sTime;
			String logVal = hblNo+ " / status: "+status+" cdType : "+cdType+" bound: "+ bound+" isDrop: "+isDrop +  " entBus : " + entBusinessCode + " / " + eTime / 1000 + "초(" + eTime + ")";
			log.info(logVal);
		}catch(Exception e) {
			throw e;
		}
	};
	
}
