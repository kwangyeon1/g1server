package org.yht.exps.express.service.eCommerce;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.yht.exps.express.model.addrLinkApi.Juso;
import org.yht.exps.express.model.addrLinkApi.Results;
import org.yht.util.Util;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ZipcodeConvert{
	
	@Async("getZipcode")
    public CompletableFuture<Boolean> getZipcode(List<Map<String,Object>> dataList, List<Map<String, Object>> resultList){
    	Map<String, Object> returnMap = new HashMap<String, Object>();
    	int nAddrCnt = 0;
    	int nFail = 0;
    	int nSuccess = 0;
    	String strFailRefNo = "";
    	String failOdJobNoStr = "";
    	List<Juso> jusoList = null;
    	Results results = null;
    	
    	try{
    		for(Map<String, Object> consigneeAddrMap : dataList){
				nAddrCnt = 0;
				results = null;
				/** 2021.06.22 강신정, 장순문 요청 => 기존에는 수하인 우편번호에 5자리가 입력되어 있는 데이터는 우편번호를 업데이트 하지 않았는데 API로 리턴받은 결과로 무조건 업데이트 하는 로직으로 변경한다. */
				while(nAddrCnt < 10){
					try{
						results = this.getAddr_OpenAPI(consigneeAddrMap, nAddrCnt);
						if(results == null){
							strFailRefNo += Util.null2str(consigneeAddrMap.get("HBL_NO")) + ",";
							failOdJobNoStr += Util.null2str(consigneeAddrMap.get("OD_JOB_NO")) + ",";
							nFail++;
							break;
						}else {
							jusoList = results.getJuso();
							if(jusoList == null){
								jusoList = new ArrayList<Juso>();
							}
							/**
							 * 2021.06.24 김태헌 주소에 특수문자 포함되어 생긴 오류는 오류 안내문 때문에 XML에러가 일어나 부득이 하게 Try-Catch로 처리한다.
							 * 우체국측의 오류 메시지 내용 수정이 필요함.
							 */
							// 리턴된 주소가 10건 이상일 경우 처리하지 않아야 함 
							if(jusoList.size() == 0 || jusoList.size() > 10){	//if (ds_Detail.Tables.Count == 1)
								if(!results.getCommon().getErrorCode().equals("0")){		//if (ds_Detail.Tables[0].Rows[0]["errorCode"].ToString() != "0")
									strFailRefNo += Util.null2str(consigneeAddrMap.get("HBL_NO")) + ",";
									failOdJobNoStr += Util.null2str(consigneeAddrMap.get("OD_JOB_NO")) + ",";
									nFail++;
									break;
								}else {
									if(nAddrCnt == 0 && jusoList.size() != 0){
										String addr = Util.null2str(consigneeAddrMap.get("CONSIGNEE_ADDRESS_LOC")).replace("#", "");
										//리턴된 주소가 10건 이상일 경우 첫번째 주소랑 수하인 주소가 같은경우 첫번째 주소 ZIPCODE 가져오기
										if(addr.equals(jusoList.get(0).getRoadAddr())) {
											consigneeAddrMap.put("CONSIGNEE_ZIPCODE", jusoList.get(0).getZipNo());
											nSuccess++;
											break;
										}
									}
								}
								nAddrCnt++;
							}else{
								//주소가 여러개 일때는 무조건 첫번째 주소의 ZIPCODE가져오기
								consigneeAddrMap.put("CONSIGNEE_ZIPCODE", jusoList.get(0).getZipNo());
								nSuccess++;
								break;
							}
						}
					}catch(Exception e){
						/** 오류건수 별도처리? */
						strFailRefNo += Util.null2str(consigneeAddrMap.get("HBL_NO")) + ",";
						failOdJobNoStr += Util.null2str(consigneeAddrMap.get("OD_JOB_NO")) + ",";
						nFail++;
						break;
					}
				}
	
				if (nAddrCnt == 10){
					nFail++;
					strFailRefNo += Util.null2str(consigneeAddrMap.get("HBL_NO")) + ",";
					failOdJobNoStr += Util.null2str(consigneeAddrMap.get("OD_JOB_NO")) + ",";
				}
    		}
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}finally {
			returnMap.put("nSuccess", nSuccess);
			returnMap.put("nFail", nFail);
			returnMap.put("strFailRefNo", strFailRefNo);
			returnMap.put("failOdJobNoStr", failOdJobNoStr);
			resultList.add(returnMap);
		}
	    return CompletableFuture.completedFuture(true);
    }
    
    /**
	 * 전자상거래 Order 수화인우편번호변환
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Results getAddr_OpenAPI(Map<String, Object> addrMap, int nAddrCnt) throws Exception{
		Results results = null;
		try {
			String strConAddr = "";
			String[] arrConAddr = null;
			String strURL = "";
			arrConAddr = Util.null2str(addrMap.get("CONSIGNEE_ADDRESS_LOC")).replace("#", "").split(" ");
	
			for(int nCnt = 0; nCnt < arrConAddr.length - nAddrCnt; nCnt++){
				strConAddr += arrConAddr[nCnt] + " ";
			}
			if(!strConAddr.trim().equals("")){
				strConAddr = strConAddr.substring(0, strConAddr.length() - 1);
			}else{
				return null;
			}
			
			/**
			 * 2021.06.24 김태헌 허용하지 않는 특수문자 제거 박재남 부장 지시
			 * 허용하지 않는 특수문자 목록:%,=,>,<,[,]
			 */
			strConAddr = strConAddr.replace("%", "");
			strConAddr = strConAddr.replace("=", "");
			strConAddr = strConAddr.replace(">", "");
			strConAddr = strConAddr.replace("<", "");
			strConAddr = strConAddr.replace("[", "");
			strConAddr = strConAddr.replace("]", "");
			strURL = "http://business.juso.go.kr/addrlink/addrLinkApi.do?currentPage=1&countPerPage=11&keyword=" + URLEncoder.encode(strConAddr,"UTF-8") + "&confmKey=U01TX0FVVEgyMDE2MDgxNzExMTEwODE0NTgy";
			
			URL url = new URL(strURL);
			URLConnection con = url.openConnection();
			con.setConnectTimeout(5000);	/** 5 seconds */
			con.setReadTimeout(5000);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			
			StringBuffer sb = new StringBuffer();
			String addressXml = "";
			while(true){
				addressXml = br.readLine();
				if(addressXml == null) break;
				sb.append(addressXml);
			}
			br.close();
			JAXBContext jc = JAXBContext.newInstance(Results.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			
			StringReader reader = new StringReader(sb.toString().replace(">,<", ""));
			
			results = (Results)unmarshaller.unmarshal(reader);
		}catch(Exception e) {
			log.error(Util.getPrintStacTraceString(e));
			throw e;
		}
		return results;
	}
}