package org.yht.exps.express.service.commonMng;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.util.Util;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class CommonMngService {
	
	@Autowired
    CommonMapper commonMapper;
			
	@Autowired
    ThreadPoolTaskExecutor threadPool;
	
	@Value("${customId.thread.pati-size}")
	public int patiSize;
	
			
	/**
	 * 전자상거래 개인통관부호 검증
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> customsVerify(Map<String, Object> map) throws Exception {
				
		//검증할 개인통관부호 리스트 추출
		List<Map<String, Object>> dataList = commonMapper.selectCustomsList(map);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		DecimalFormat df = new DecimalFormat("#####0.000");
		
		int startIndex = 0;
		int endIndex = 0;
				
		//처리할 데이터단위갯수	
		long startJob = 0;
		int threadCnt = 0;
				
		try {
			
			int totCnt = dataList.size();	
			
			returnMap.put("totalCnt", totCnt);
			returnMap.put("succCnt", 0);
			returnMap.put("failCnt", 0);
			returnMap.put("nonCnt", 0);
			returnMap.put("totalThread", 0);
			
			if(dataList == null || dataList.size() == 0) {
				return returnMap;
			}
			
			startJob = System.currentTimeMillis();
			List<Future<Map<String,Object>>> threadList = new ArrayList<Future<Map<String,Object>>>();
			
			//처리할 단위만큼 잘라서 쓰레드 생성 (예>처리단위 50개씩 처리하도록 할당)
			for(int i=0; i<totCnt; i++){
				
				//분할해서 쓰레드 생성
		        if( ((i+1)%patiSize == 0 || totCnt-1 == i) ){	
					startIndex = endIndex;
					endIndex = i+1;
					
					//쓰레드 순번 셋팅
					threadCnt++;
					//CustomsVerifyWebThreadEX threadex = new CustomsVerifyWebThreadEX(commonMapper, returnMap,dataList, startIndex, endIndex,String.valueOf(threadCnt));
					//Future<Map<String,Object>> result = threadPool.submit(threadex);
		            //threadList.add(result);
				}
			}				
			
			//쓰레드 리스트에서 값 추출
			for (int i = 0; i < threadList.size(); i++) {
				 //get 메소드 호출 스레드 종료를 기다림.
				Future<Map<String,Object>> thread = threadList.get(i);  
				Map<String,Object> rst = thread.get();
				if(rst != null) {
					log.info("threadNum="+ rst.get("T_CNT") +" : thread rst ="+rst.get("RESULT"));
				}
			}
			
			//생성된 쓰레드 갯수
			returnMap.put("totalThread", threadCnt);
			
			log.info("CustomsVerify[" + dataList.size() + "], Total Time[" + df.format((System.currentTimeMillis() - startJob) / 1000.0) + "s], Result[OK]");
		}catch(Exception e) {
			log.info("CustomsVerify[" + dataList.size() + "], Total Time[" + df.format((System.currentTimeMillis() - startJob) / 1000.0) + "s], Result[FAIL]");
		}		
		
		return returnMap;
	}
		
	/**
	 * 개인통관부호 API 검증 결과 추출
	 * @param dataMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getpCustomsAPI(Map<String, Object> dataMap) throws Exception {
		
		DocumentBuilderFactory dbFactory = null;
		DocumentBuilder dBuilder = null;
		Document doc = null;
		Element element = null;
		Node nNode = null;
		Node nValue = null;
		
		String ConsigneeIdCardNo_IF_YN = "N";
		String ConsigneeIdCardNoFailRemark = "";
		String postUrl = "";
		
		postUrl = "https://unipass.customs.go.kr:38010/ext/rest/persEcmQry/retrievePersEcm?crkyCn=x250l200o001u115c090p000z0";
		
		postUrl +=  "&persEcm="+ Util.null2str(dataMap.get("customsId")).toUpperCase();
		postUrl +=  "&pltxNm=";
		postUrl +=  URLEncoder.encode(Util.null2str(dataMap.get("consigneeNameLoc")), "UTF-8");
				
		try{
			dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder  = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(postUrl);

            doc.getDocumentElement().normalize();

            nNode = doc.getElementsByTagName("persEcmQryRtnVo").item(0);
            if(nNode.getNodeType()==Node.ELEMENT_NODE) {

                element = (Element) nNode;
                nValue = (Node) element.getElementsByTagName("tCnt").item(0).getChildNodes().item(0);

                // 성공 : 1, 실패 : 0
                if("0".equals(Util.null2str(nValue.getNodeValue()))) {
                	
                	nNode = doc.getElementsByTagName("persEcmQryRtnErrInfoVo").item(0);
                	if(nNode.getNodeType()==Node.ELEMENT_NODE) {
                		element = (Element) nNode;
                        nValue = (Node) element.getElementsByTagName("errMsgCn").item(0).getChildNodes().item(0);
                        ConsigneeIdCardNoFailRemark = nValue.getNodeValue();
                        
                        if(ConsigneeIdCardNoFailRemark.contains("개인통관고유부호의 성명과 일치하지 않습니다.")) {
                        	ConsigneeIdCardNo_IF_YN = "E";
                        	try {
                        		ConsigneeIdCardNoFailRemark = ConsigneeIdCardNoFailRemark.split("\\.")[0] + ".";
                        	}catch(ArrayIndexOutOfBoundsException ex) {
                        		throw ex;
                        	}
                        }
                        
                	}
                } else if("-1".equals(Util.null2str(nValue.getNodeValue()))) {
                	ConsigneeIdCardNoFailRemark = element.getElementsByTagName("ntceInfo").item(0).getChildNodes().item(0).getNodeValue();
            	} else {
                	ConsigneeIdCardNo_IF_YN = "Y";
                	ConsigneeIdCardNoFailRemark = "정상";
                }
                
            }
		} catch(Exception e){
			log.info("Customs API URL["+postUrl+"], Exception["+e.getMessage()+"]");
			log.error(Util.getPrintStacTraceString(e));
		}

		dataMap.put("ConsigneeIdCardNo_IF_YN", ConsigneeIdCardNo_IF_YN);
		dataMap.put("ConsigneeIdCardNoFailRemark", ConsigneeIdCardNoFailRemark);
		
		dataMap.put("verifyYn", ConsigneeIdCardNo_IF_YN);
		dataMap.put("errorMsg", ConsigneeIdCardNoFailRemark);
		
		return dataMap;
	}
	
}