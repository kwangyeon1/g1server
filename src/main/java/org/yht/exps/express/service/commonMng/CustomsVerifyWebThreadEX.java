package org.yht.exps.express.service.commonMng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.web.context.WebApplicationContext;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.util.Util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomsVerifyWebThreadEX implements Callable<Map<String,Object>>{
		
	private int startIndex;
	private int endIndex;
	private List<Map<String,Object>> dataList = null;
	private Map<String, Object> returnMap = null;	
	
	WebApplicationContext context = null;	

	//스레드 순번
	@Getter
	private String threadCnt;
	
	public CustomsVerifyWebThreadEX(WebApplicationContext context, Map<String, Object> returnMap, List<Map<String,Object>> dataList, int startIndex, int endIndex,String threadCnt){
		this.context = context;
		this.returnMap = returnMap;
		this.dataList = dataList;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.threadCnt = threadCnt;
	}
	
    @Override
    public Map<String,Object> call() {
    	            	
    	CommonMngService commonMngService = new CommonMngService();
    	
		Map<String,Object> dataMap = null;
		Map<String,Object> apiReturnMap = null;
		Map<String,Object> rstMap = new HashMap<String,Object>();
		
		//DB 업데이트할 목록
		List<Map<String,Object>> rstList = new ArrayList<Map<String,Object>>();
		
		int failCnt = 0;
		int succCnt = 0;
		int nonCnt = 0;
		String suc = "";
		
    	try{
    		//startIndex endIndex 받아서 예>50개씩 분할해서 스레드 생성해서 실행
    		for(int i = startIndex; i<endIndex; i++){
    			
    			dataMap = dataList.get(i);
    			
				if("".equals(Util.null2str(dataMap.get("customsId")))) {// 개인통관부호가 없을 때
					failCnt++;
					dataMap.put("odJobNo", dataMap.get("odJobNo"));
					dataMap.put("ConsigneeIdCardNo_IF_YN", "X");
					dataMap.put("ConsigneeIdCardNoFailRemark", "개인통관부호 입력 값이 없습니다.");
				} else {
					if(String.valueOf(dataMap.get("customsId")).length() == 13) {
						// P + 13자리가 아닌 경우
						if(!dataMap.get("customsId").toString().matches("^(P|p)[0-9]{12}$")) {
							failCnt++;
							dataMap.put("ConsigneeIdCardNo_IF_YN", "N");
							dataMap.put("ConsigneeIdCardNoFailRemark", "개인통관부호의 형식이 맞지 않습니다.");
						} else {		// API
							apiReturnMap = commonMngService.getpCustomsAPI(dataMap);
							dataMap.put("ConsigneeIdCardNo_IF_YN", apiReturnMap.get("ConsigneeIdCardNo_IF_YN"));
							dataMap.put("ConsigneeIdCardNoFailRemark",  apiReturnMap.get("ConsigneeIdCardNoFailRemark"));
							//성공
							if("Y".equals(Util.null2str(apiReturnMap.get("verifyYn")))) {	
								succCnt++;
							}
							//실패
							else {		
								failCnt++;
							}
						}
					}else if(String.valueOf(dataMap.get("customsId")).length() == 8) {
						nonCnt++;
						dataMap.put("ConsigneeIdCardNo_IF_YN", "");
						dataMap.put("ConsigneeIdCardNoFailRemark", "");
					}else {
						failCnt++;
						dataMap.put("ConsigneeIdCardNo_IF_YN", "N");
						dataMap.put("ConsigneeIdCardNoFailRemark", "개인통관부호의 입력 값의 자릿수가 일치하지 않습니다.");
					}
				}
				
				rstList.add(dataMap);				
    		}
    		    		
    		//한번에 DB update
    		Map<String,Object> param = new HashMap<String,Object>();
    		param.put("list", rstList); 
    		
    		//컨텍스트에서 Mapper 추출
    		CommonMapper commonMapper = (CommonMapper) context.getBean(CommonMapper.class);
    		commonMapper.updateCustoms(param);    	
    		
    		suc = "Success! : stInx = "+String.valueOf(startIndex) + " : edIdx = "+String.valueOf(endIndex);
    		rstMap.put("RESULT", suc);
    		
		}catch(Exception e){
			suc = "Fail! "+e.toString();
			rstMap.put("RESULT", suc);
			
			log.error(Util.getPrintStacTraceString(e));
		}finally {
			returnMap.put("succCnt", Integer.parseInt(String.valueOf(returnMap.get("succCnt"))) + succCnt);
			returnMap.put("failCnt", Integer.parseInt(String.valueOf(returnMap.get("failCnt"))) + failCnt);
			returnMap.put("nonCnt", Integer.parseInt(String.valueOf(returnMap.get("nonCnt"))) + nonCnt);	    	
		}
    	    
    	rstMap.put("T_CNT", getThreadCnt());
    	
    	return rstMap;
    }
    
}
