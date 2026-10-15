package org.yht.exps.express.service.commonMng;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.text.StringEscapeUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.com.excel.CreateExcelFile;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.commonMng.StockMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.commonMng.stock.DeleteStock;
import org.yht.exps.express.model.commonMng.stock.InsertStock;
import org.yht.exps.express.model.commonMng.stock.RegrantStock;
import org.yht.exps.express.model.commonMng.stock.Stock;
import org.yht.exps.express.model.commonMng.stock.StockList;
import org.yht.exps.express.model.commonMng.stock.StockSaveResult;
import org.yht.exps.express.model.commonMng.stock.VoidStock;
import org.yht.exps.express.model.commonMng.stock.VoidStockList;
import org.yht.exps.express.model.commonMng.stock.search.SearchStockDetail;
import org.yht.exps.express.model.commonMng.stock.search.SearchStockList;
import org.yht.exps.express.model.commonMng.stock.search.SearchVoidStockList;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class StockService {

	@Autowired
	CustomProperties customProperties;
	
	@Autowired
    CommonMapper commonMapper;

	@Autowired
	StockMapper stockMapper;
	
    @Autowired
    EnterpriseMapper enterpriseMapper;

    @Autowired
    CommonService commonService;
    
	@Autowired
	RestHighLevelClient client;	
	
	
	/**
	 * Stock List 조회
	 * @param SearchStockList
	 * @return
	 * @throws Exception
	 */	
	public StockList selectStockList(SearchStockList search) throws Exception {
		
		StockList stockList = new StockList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, null, "", null, "");
		List<Stock> list  = stockMapper.selectStockList(search);
		stockList.setStockList(list);
		if(list.size() > 0) {
			stockList.setTotal(list.get(0).getTotal());
		}else {
			stockList.setTotal(0);
		}
		
		return stockList;
	}
	
	/**
	 * Stock 데이터 등록, 대리점에 부여 
	 * @param InsertStock
	 * @return
	 * @throws Exception
	 */	
	public StockSaveResult insertStock(InsertStock insert, HttpServletRequest request) throws Exception {
		StockSaveResult result = new StockSaveResult();
		InsertStock insertMul = new InsertStock();
		List<Map<String, Object>> insertList = new ArrayList<Map<String, Object>>();
		ProcessLogs processLogs = new ProcessLogs();
		
		//실제 등록하기 전에 다시 중복체크
		RegrantStock reg = new RegrantStock();
		reg.setEntCode(insert.getEntCode());
		reg.setTrCode(insert.getTrCode());
		reg.setBound(insert.getBound());
		reg.setBoundType(insert.getBoundType());
		reg.setStartNo(insert.getStartNo());
		reg.setEndNo(insert.getEndNo());
		int cnt = stockMapper.checkStockDuplication(reg);
		
		//중복 있는 경우
		if(cnt > 0) {
			result.setResultCode("N");
		//중복 없는 경우
		}else {
			
			processLogs.setEntCode(insert.getEntCode());
			processLogs.setEntBusinessCode(insert.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(insert.getBound());
			processLogs.setBoundType(insert.getBoundType());
			processLogs.setProcessUserId(insert.getInputUserId());
			processLogs.setLogJobType("STOCK");
			processLogs.setJobFuncType("ADD");
			processLogs.setDescription("STOCK 생성 "+insert.getStartNo()+ " ~ " +insert.getEndNo());
			processLogs.setJobLog(new Gson().toJson(insert));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(insert.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(insert.getLogUserName());
			processLogs.setProcessUserNameEng(insert.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
			
			insertList = insert.getDataList();
			
			if(insertList != null) { // 대리점 체크 후 여러건 Insert
				
				int i = 0;
				String lastNo = ""; 
				
				for (Map<String, Object> insertMap : insertList) {
					++ i;
	
					//START_NO, END_NO, AGENT_CODE 고유 
					insertMul.setRegisterDateTime(insert.getRegisterDateTime());
					insertMul.setBound(insert.getBound());
					insertMul.setBoundType(insert.getBoundType());
					insertMul.setTrCode(insert.getTrCode());
					insertMul.setEntCode(insert.getEntCode());
					insertMul.setEntBusinessCode(insert.getEntBusinessCode());
					insertMul.setInputUserId(insert.getInputUserId());
					insertMul.setGmtTime(insert.getGmtTime());
					insertMul.setStartNo(String.valueOf(insertMap.get("START_NO")));
					insertMul.setEndNo(String.valueOf(insertMap.get("END_NO")));
					insertMul.setAgentCode(String.valueOf(insertMap.get("AGENT_CODE")));
					insertMul.setStockUseYn(insert.getStockUseYn());
					
					stockMapper.insertStock(insertMul);
					lastNo = insertMul.getEndNo();
				}
				
				//origStartNo, origEndNo 세팅 
//				insertMul.setOrigStartNo(insert.getStartNo());
//				insertMul.setOrigEndNo(insert.getEndNo());
//				stockMapper.updateStock(insertMul); // XXXXXXXX의 스탁 시작번호 갱신
				
				if(!lastNo.equals(insert.getEndNo())){
					insert.setStartNo(null);
					insert.setOrigEndNo(insert.getEndNo());
					insert.setEndNo(lastNo);
					
					if("EMS-KP".equals(insert.getTrCode()) || "EMS-TS".equals(insert.getTrCode())) {
						insert.setEndNo(Util.createEmsKpStockDigit(insert.getEndNo(),true));
					}
					stockMapper.insertStock(insert);
				}
			}else{
				stockMapper.insertStock(insert); // 단일건 등록  
			}
			
			
		}
		
		return result;
		
	}	
	
	/**
	 * Stock 데이터 삭제
	 * @param DeleteStock
	 * @return
	 * @throws Exception
	 */	
	public StockSaveResult deleteStock(DeleteStock delete, HttpServletRequest request) throws Exception {
		StockSaveResult result = new StockSaveResult();
		DeleteStock deleteOne = new DeleteStock();
		List<Map<String, Object>> deleteList = new ArrayList<Map<String, Object>>();
		deleteList = delete.getDataList();
		
		for (Map<String, Object> deleteMap : deleteList) {
			deleteOne.setSeqNo(String.valueOf(deleteMap.get("seqNo")));
			deleteOne.setEntCode(String.valueOf(deleteMap.get("entCode")));
			deleteOne.setBound(String.valueOf(deleteMap.get("bound")));
			deleteOne.setBoundType(String.valueOf(deleteMap.get("boundType")));
			deleteOne.setTrCode(String.valueOf(deleteMap.get("trCode")));
			deleteOne.setAgentCode(String.valueOf(deleteMap.get("agentCode")));
//			deleteOne.setStartNo(String.valueOf(deleteMap.get("startNo")));
//			deleteOne.setEndNo(String.valueOf(deleteMap.get("endNo")));
			stockMapper.deleteStock(deleteOne);
			
			//userAuth가 AD인 경우 사용건수 있더라도 수정이 가능하도록 수정됨/이때 시스템로그 남기도록 추가
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(Util.null2str(deleteMap.get("entCode")));
			processLogs.setEntBusinessCode(Util.null2str(deleteMap.get("entBusinessCode")));
			processLogs.setSolution("ECM");
			processLogs.setBound(Util.null2str(deleteMap.get("bound")));
			processLogs.setBoundType(Util.null2str(deleteMap.get("boundType")));
			processLogs.setProcessUserId(Util.null2str(delete.getInputUserId()));
			processLogs.setLogJobType("STOCK");
			processLogs.setJobFuncType("DELETE");
			processLogs.setDescription("STOCK 삭제 "+String.valueOf(deleteMap.get("startNo"))+ " ~ " +String.valueOf(deleteMap.get("endNo")));
			processLogs.setJobNo(String.valueOf(deleteMap.get("seqNo")));
			processLogs.setJobLog(new Gson().toJson(deleteMap));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(delete.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(delete.getLogUserName());
			processLogs.setProcessUserNameEng(delete.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}
		
		return result;
	}

	/**
	 * Stock 사용여부 변경
	 * @param DeleteStock
	 * @return
	 * @throws Exception
	 */	
	public StockSaveResult updateUseYn(InsertStock stock, HttpServletRequest request) throws Exception {
		StockSaveResult result = new StockSaveResult();
		
		stockMapper.updateStockUseYn(stock);
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(stock.getEntCode());
		processLogs.setEntBusinessCode(stock.getEntBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound("COM");
		processLogs.setBoundType("COM");
		processLogs.setProcessUserId(stock.getInputUserId());
		processLogs.setLogJobType("STOCK");
		processLogs.setJobFuncType("UPDATE");
		processLogs.setDescription("STOCK 사용여부 업데이트");
		processLogs.setJobNo("");
		processLogs.setJobLog(new Gson().toJson(stock));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(stock.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(stock.getLogUserName());
		processLogs.setProcessUserNameEng(stock.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return result;
	}	
	
	
	public Map<String, Object> stockNoExcel(InsertStock stock) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String trCode = stock.getTrCode();
		SearchStockDetail searchStockDetail = new SearchStockDetail();
		searchStockDetail.setSrchTrCode(trCode);
		searchStockDetail.setSrchBound(stock.getBound());
		searchStockDetail.setSrchBoundType(stock.getBoundType());
		searchStockDetail.setSrchAgentCode(stock.getAgentCode());
		searchStockDetail.setSrchSeqNo(Util.null2int(stock.getSeqNo()));
		Stock stockInfo = stockMapper.selectStockDetail(searchStockDetail);
		if(stockInfo == null){
			returnMap.put("errCode", "data");
		}else if(!"N".equals(stockInfo.getStockUseYn())){
			returnMap.put("errCode", "useYn");
		}else{
			long unusedCount = 0;
			int seqSize = 0;
			boolean useCheckDigit = false;
			boolean useStartNo = false;
			String currentNo = null;
			String prefix = "";
			if(stockInfo.getCurrentNo() == 0){	/** 처음사용할경우 startNo사용 */
				currentNo = Util.null2str(stockInfo.getStartNo());
				useStartNo = true;
			}else{
				currentNo = Util.null2str(stockInfo.getCurrentNo());
			}
			
			if("KOREX".equals(trCode) || "KOREXMUL".equals(trCode)){	/** 12자리 = prefix(2) + seq(9) + checkDigit(1) */
				prefix = currentNo.substring(0, 2);
				useCheckDigit = true;
				map.put("currentNo", currentNo.substring(2, 11));
				unusedCount = Util.null2long(Util.null2str(stockInfo.getEndNo()).substring(2, 11)) - Util.null2long(map.get("currentNo"));
				seqSize = 9;
			}else if("HANJIN".equals(trCode) || "LOTTE".equals(trCode) || "JP-SEA".equals(trCode) || "JP-AIR".equals(trCode)){	/** 12자리 = seq(11) + checkDigit(1) */
				useCheckDigit = true;
				map.put("currentNo", currentNo.substring(0, 11));
				unusedCount = Util.null2long(Util.null2str(stockInfo.getEndNo()).substring(0, 11)) - Util.null2long(map.get("currentNo"));
				seqSize = 11;
			}else if("EMS-KP".equals(trCode) || "EMS-TS".equals(trCode)){ /** 9자리 = seq(8) + checkDigit(1) */
				useCheckDigit = true;
				map.put("currentNo", currentNo.substring(0, 8));
				unusedCount = Util.null2long(Util.null2str(stockInfo.getEndNo()).substring(0, 8)) - Util.null2long(map.get("currentNo"));
				seqSize = 8;
			}else{	/** 13자리 = seq(13) */
				map.put("currentNo", currentNo);
				unusedCount = stockInfo.getEndNo() - Util.null2long(currentNo);
				seqSize = 13;
			}
			
			if(useStartNo){	/** currentNo는 다음번호 사용하고 startNo는 해당번호 사용 */
				unusedCount++;
			}
			if(unusedCount == 0){
				returnMap.put("errCode", "count");
			}else{
				map.put("excelNo", 0);
				int pageSize = 10000;
				String fileName = "StockNoList";
				String dir = customProperties.getExcelDownload() + "/";
				String currDtm = DateTimeUtil.getDate("yyyyMMddhhmmss");
				String originFile = StringEscapeUtils.escapeHtml4(fileName + ".csv");
				
				String saveFile = fileName + "_" + currDtm + ".csv";
				if(Common.checkSaveFilenameForExcel(fileName)) {
					String uuid = UUID.randomUUID().toString();
					saveFile = uuid + "_" + currDtm + ".csv";
				}
				
				saveFile = StringEscapeUtils.escapeHtml4(dir + saveFile);
				map.put("saveFile", saveFile);
				map.put("originFile", originFile);
				
		        for (int startNum = 0; startNum < unusedCount; startNum += pageSize) {
		        	if(unusedCount < pageSize){
		        		this.stockNoExcel(map, saveFile, prefix, useCheckDigit, useStartNo, seqSize, unusedCount, trCode);
		        	}else if(startNum + pageSize < unusedCount){
		        		this.stockNoExcel(map, saveFile, prefix, useCheckDigit, useStartNo, seqSize, pageSize, trCode);
		        	}else{
		        		this.stockNoExcel(map, saveFile, prefix, useCheckDigit, useStartNo, seqSize, unusedCount - startNum, trCode);
		        	}
		        	useStartNo = false;
		        }
			}
			
		}
		
		returnMap.put("saveFile", map.get("saveFile"));
		returnMap.put("originFile", map.get("originFile"));
		return returnMap;
	}
	
	public void stockNoExcel(Map<String, Object> map, String saveFile, String prefix, boolean useCheckDigit, boolean useStartNo, int seqSize, long createCnt, String trCode) throws Exception {
		File file = null;
		BufferedWriter bw = null;
        OutputStreamWriter osw = null;
        FileOutputStream fos = null;
        
		String newLine = System.lineSeparator();
		try {
			
			file = new File(saveFile);
			fos = new FileOutputStream(file,true);
			osw = new OutputStreamWriter(fos, "MS949");
			bw = new BufferedWriter(osw);

			int excelNo = Util.null2int(map.get("excelNo"));
			long currentNo = Util.null2long(map.get("currentNo"));
			if(useStartNo){
				currentNo--;
			}
			if(excelNo == 0){
				bw.write("No,H.B/L No");
				bw.write(newLine);
			}

			if(seqSize == Util.null2str(currentNo).length()){
				if((useCheckDigit && "EMS-KP".equals(trCode)) || (useCheckDigit && "EMS-TS".equals(trCode))){
					for (int i = 0; i < createCnt; i++) {
						String stockNo = Util.createEmsKpStockDigit(String.valueOf(currentNo), true);
						bw.write(++excelNo + ",=\"" + prefix + stockNo + "\"" + newLine);
						currentNo++;
						if (i % 1000 == 0) bw.flush();
					}
				}else if(useCheckDigit){
					long checkDigit;
					for (int i = 0; i < createCnt; i++) {
						currentNo++;
						checkDigit = currentNo % 7;
						bw.write(++excelNo + ",=\"" + prefix + currentNo + checkDigit + "\"" + newLine);
						if (i % 1000 == 0) bw.flush();
					}
				}else{
					for (int i = 0; i < createCnt; i++) {
						bw.write(++excelNo + ",=\"" + ++currentNo + "\"" + newLine);
						if (i % 1000 == 0) bw.flush();
					}
				}
			}else{	/** 첫번째숫자가 0일경우 lpad적용 */
				if((useCheckDigit && "EMS-KP".equals(trCode)) || (useCheckDigit && "EMS-TS".equals(trCode)) ){
					for (int i = 0; i < createCnt; i++) {
						String emsStockNo = Util.lpad(Util.null2str(currentNo), seqSize, '0');
						String stockNo = Util.createEmsKpStockDigit(emsStockNo, true);
						bw.write(++excelNo + ",=\"" + prefix + stockNo + "\"" + newLine);
						currentNo++;
						if (i % 1000 == 0) bw.flush();
					}
				}else if(useCheckDigit){
					long checkDigit;
					for (int i = 0; i < createCnt; i++) {
						currentNo++;
						checkDigit = currentNo % 7;
						bw.write(++excelNo + ",=\"" + prefix + Util.lpad(Util.null2str(currentNo), seqSize, '0') + checkDigit + "\"" + newLine);
						if (i % 1000 == 0) bw.flush();
					}
				}else{
					for (int i = 0; i < createCnt; i++) {
						bw.write(++excelNo + ",=\"" + Util.lpad(Util.null2str(++currentNo), seqSize, '0') + "\"" + newLine);
						if (i % 1000 == 0) bw.flush();
					}
				}
			}
			
			
			map.put("excelNo", excelNo);
			map.put("currentNo", currentNo);
			

			bw.flush();
			bw.close();
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
			bw.close();
		}
	}
	
	/**
	 * Stock 데이터 디테일
	 * @param SearchStockDetail
	 * @return
	 * @throws Exception
	 */	
	public Stock selectStockDetail(SearchStockDetail search) throws Exception {
		return stockMapper.selectStockDetail(search);
		
	}		
	
	
	/**
	 * Void Stock List 조회
	 * @param RegrantStock
	 * @return
	 * @throws Exception
	 */
	public VoidStockList selectVoidStockList(SearchVoidStockList search) throws Exception {
		
		VoidStockList voidStockList = new VoidStockList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
	
		Util.accessControlDatabase(search, null, true, null, "", null, "");
		List<VoidStock> list  = stockMapper.selectVoidStockList(search);
		voidStockList.setVoidStockList(list);
		if(list.size() > 0) {
			voidStockList.setTotal(list.get(0).getTotal());
		}else {
			voidStockList.setTotal(0);
		}
		
		return voidStockList;
		
	}	

	/**
	 * Stock 재부여
	 * @param RegrantStock
	 * @return
	 * @throws Exception
	 */
	public StockSaveResult regrantStock(RegrantStock regrant, HttpServletRequest request) throws Exception {
		StockSaveResult result = new StockSaveResult();
		
		int unusedCnt = stockMapper.checkReStockDuplication(regrant);
		
		if(unusedCnt >= Integer.parseInt(regrant.getRegrantCount())) {	//미사용건수가 재부여건수보다 많은 경우
			if("EMS-KP".equals(regrant.getTrCode()) || "EMS-TS".equals(regrant.getTrCode())) {
				regrant.setEmsStockNo(Util.createEmsKpStockDigit(regrant.getStartNo(),false));
			}
			stockMapper.regrantStock(regrant);
			
			ProcessLogs processLogs = new ProcessLogs();
			
			processLogs.setEntCode(regrant.getEntCode());
			processLogs.setEntBusinessCode(regrant.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(regrant.getBound());
			processLogs.setBoundType(regrant.getBoundType());
			processLogs.setProcessUserId(regrant.getUpdateUserId());
			processLogs.setLogJobType("STOCK");
			processLogs.setJobFuncType("ADD");
			processLogs.setDescription("STOCK 재부여 "+regrant.getStartNo()+ " ~ " +regrant.getEndNo());
			processLogs.setJobLog(new Gson().toJson(regrant));
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(regrant.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(regrant.getLogUserName());
			processLogs.setProcessUserNameEng(regrant.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
			
		}else {
			result.setResultCode("N");
		}
		
		return result;
	}
		
	/**
	 * Stock 중복체크
	 * @param RegrantStock
	 * @return
	 * @throws Exception
	 */
	public int checkStockDuplication(RegrantStock regrant) throws Exception {
		return stockMapper.checkStockDuplication(regrant);
	}	
		
	
	/**
	 * Stock List 조회
	 * @param SearchStockList
	 * @return
	 * @throws Exception
	 */	
	public StockList selectStockListV2(SearchStockList search) throws Exception {
		StockList stockList = new StockList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, null, "", null, "");
		List<Stock> list  = stockMapper.selectStockListV2(search);
		stockList.setStockList(list);
		if(list.size() > 0) {
			stockList.setTotal(list.get(0).getTotal());
		}else {
			stockList.setTotal(0);
		}
		
		return stockList;
	}
}
