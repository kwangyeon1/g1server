package org.yht.exps.express.controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.SearchExcelList;
import org.yht.exps.express.model.SearchOdJobNo;
import org.yht.exps.express.model.common.AddressInfo;
import org.yht.exps.express.model.common.CustomsClearanceInfo;
import org.yht.exps.express.model.common.CustomsConsigNameAddrInfo;
import org.yht.exps.express.model.common.search.SearchAddressInfo;
import org.yht.exps.express.service.CommonService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.ExchangeKorEng;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "Common")
@RequestMapping("/{entCode}/common")
public class CommonController {
	
	@Resource(name = "commonService")
	CommonService commonService;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	/**
	 * 마스터번호 리스트 조회
	 * @param entCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({"unchecked","static-access"})
	@RequestMapping(value="/selectMblNoList", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> selectCombinedTaxList(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
		return commonService.responseMapData(commonService.selectMblNoList(commonService.setGridParams(request)));
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "공통 설정된 엑셀 포맷에 따라 다운로드", notes="공통 설정된 엑셀 포맷에 따라 다운로드합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/excel", method = RequestMethod.GET)
	public ResponseEntity<DownloadFileInfo> makeExcelDownload(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchExcelList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "registerUserId", null, null);
		long time = System.currentTimeMillis();
		DecimalFormat df = new DecimalFormat("#####0.000");
		log.info("!!!!![EXCEL DOWNLOAD START]!!!!" );
		
		ObjectMapper objectMapper = new ObjectMapper();
		// map => 포멧 아이템 검색용 
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class); 
		
		//	total 갯수
		int totalCount = commonService.selectExcelOrderCount(search);
		int page = 0; 
		int pageSize = 10000; // 10000개씩 나눠서 엑셀 작성
		//page : 0 startNum : 0 pageSize : 0
		//ORDER BY ${DefaultSort} OFFSET ${startNum} ROWS FETCH NEXT ${pageSize} ROWS ONLY
		
		Map<String, Object> excelMap = null;
		List<Map<String, Object>> excelDataList = null;

		// 엑셀다운로드 양식 정보 
		List<Map<String, Object>> formItemList = commonService.selectExcelDownFormatItem(map);
		List<Map<String, Object>> masterItemList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> costList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> codeNameList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempCost;
		Map<String, Object> tempCodeName;
		
		
		
		HashSet<String> tempOdSet = new HashSet<String>(); 
		HashSet<String> tempNotOdAdSet = new HashSet<String>();
		List<String> odColumnList = null;	
		List<String> notOdAdColumnList = null;
		
		String oblOdGubn = "";
		String column = "";
		String itemType = "";
		String chargeCode = "";
		String groupCode = "";
		String itemTableYn = "N"; // 아이템 테이블 조회 유무
		String contTableYn = "N"; // 컨테이너 테이블 조회 유무
		String multiTableYn = "N";
		String holdTableYn = "N"; // 홀드 테이블 조회 유무 
		String masterYn = "N"; // 마스터 정보 생성 유무 세팅
		String licenseTableYn = "N"; // 면장정보 생성 유무 세팅
		
		String srchNameItemOriginCountryYn = "N";
		String srchCodeNameItemOriginCountryYn = "N";
		String srchNameCntrCodeYn = "N";
		String srchCodeNameCntrCodeYn = "N";
		String srchNameMultiDeliveryCodeYn = "N";
		String srchCodeNameMultiDeliveryCodeYn = "N";
		String srchNameMultiCustomerCodeYn = "N";
		String srchCodeNameMultiCustomerCodeYn = "N";
		String srchNameMultiPaymentGubnYn = "N";
		String srchCodeNameMultiPaymentGubnYn = "N";
		
		// 쿼리에 필요한 COLUMN을  나눠서 저장 (item, container는 담지 않아도 됨) 
		// ETC6 => ECM_HOUSE, ECM_MASTER, ECM_CONTAINER, ECM_ITEM, ECM_COMMON, ECM_MULTI
		Map<String, Object> tmpMap;
		for(int i = formItemList.size() - 1; i >= 0; i--){
			tmpMap = formItemList.get(i);
			column = Util.null2str(tmpMap.get("ETC1"));
			if("CONSIGNEE_ADDRESS_ADD".equals(column) || "CONSIGNEE_ADDRESS_LOC_ADD".equals(column)){
				continue;
			}
			oblOdGubn = Util.null2str(tmpMap.get("ETC6")).replace("nullData", "");
			chargeCode = Util.null2str(tmpMap.get("ETC13"));
			groupCode = Util.null2str(tmpMap.get("ETC16"));
			itemType = Util.null2str(tmpMap.get("ITEM_TYPE"));
			if(!"".equals(oblOdGubn)){
				if("ECM_ITEM".equals(oblOdGubn)){
					itemTableYn = "Y"; // DB_FIELD=ECM.ITEM.HS_CODE_20
					if(column.contains("ORIGIN_COUNTRY")){
						if(!"".equals(groupCode)){
							srchNameItemOriginCountryYn = "Y";
							if("Y".equals(tmpMap.get("ETC18"))){
								srchCodeNameItemOriginCountryYn = "Y";
							}
						}
					}
				}else if("ECM_CONTAINER".equals(oblOdGubn)){
					contTableYn = "Y"; // DB_FIELD=ECM.CONTAINER.CNTR_CODE_1
					if(column.contains("CNTR_CODE")){
						if(!"".equals(groupCode)){
							srchNameCntrCodeYn = "Y";
							if("Y".equals(tmpMap.get("ETC18"))){
								srchCodeNameCntrCodeYn = "Y";
							}
						}
					}
				}else if("ECM_MULTI".equals(oblOdGubn)){
					multiTableYn = "Y";
					if(column.contains("CUSTOMER_CODE")){
						if(!"".equals(groupCode)){
							srchNameMultiCustomerCodeYn = "Y";
							if("Y".equals(tmpMap.get("ETC18"))){
								srchCodeNameMultiCustomerCodeYn = "Y";
							}
						}
					}else if(column.contains("DELIVERY_CODE")){
						if(!"".equals(groupCode)){
							srchNameMultiDeliveryCodeYn = "Y";
							if("Y".equals(tmpMap.get("ETC18"))){
								srchCodeNameMultiDeliveryCodeYn = "Y";
							}
						}
					}else if(column.contains("PAYMENT_GUBN")){
						if(!"".equals(groupCode)){
							srchNameMultiPaymentGubnYn = "Y";
							if("Y".equals(tmpMap.get("ETC18"))){
								srchCodeNameMultiPaymentGubnYn = "Y";
							}
						}
					}
				}else if("MASTER".equals(itemType)){
					masterYn = "Y";
					masterItemList.add(formItemList.remove(i));
					// itemType == "MASTER"도 같은 od테이블에서 조회해오기때문에 tempOdSet에 같이 담아준다.
					if(!"MBL_NO".equals(column) && !column.contains("_DATE")) {
						tempOdSet.add(column);
					}
				}else if(!"ECM_LICENSE".equals(oblOdGubn) && !"ECM_MULTI".equals(oblOdGubn)){
					if(!"MBL_NO".equals(column) && !"HBL_NO".equals(column) && !"CUSTOMER_CODE".equals(column) && !column.contains("_DATE")){ // MBL_NO, HBL_NO, _DATE 기본 조회로 세팅되어있어서 중복이 된다.
						if("HOLD_CODE".equals(column)) {
							holdTableYn = "Y";
						}
						// od, ad테이블에 없는 컬럼은 따로 담아서 처리 
						if(!chargeCode.equals("")){
							tempCost = new HashMap<String, Object>();
							tempCost.put("chargeCode", chargeCode);
							tempCost.put("column", column);
							costList.add(tempCost);
						}else if(!groupCode.equals("")){
							tempCodeName = new HashMap<String, Object>();
							tempCodeName.put("groupCode", groupCode);
							tempCodeName.put("field", column);
							tempCodeName.put("column", column + "_NAME");
							if("Y".equals(tmpMap.get("ETC18"))){
								tempCodeName.put("printCodeAndNameYn", "Y");
							}
							/** 2025-04-21 od, ad, multi 중복(CD_TYPE, DELIVERY_CODE) 컬럼 alias od로 고정 */
							if("CD_TYPE".equals(column) || "DELIVERY_CODE".equals(column)){
								tempCodeName.put("codeAlias", "od.");
							}else{
								tempCodeName.put("codeAlias", "");
							}
							
							codeNameList.add(tempCodeName);
							tmpMap.put("ETC1", column + "_NAME");
						}else if("SHIPPER_ADDRESS".equals(column) || "CONSIGNEE_ADDRESS".equals(column) || "MULTI_DELIVERY_NO".equals(column) || "GWT_TYPE".equals(column) || "ORDER_NO".equals(column)){
							tempNotOdAdSet.add(column);
						}else {
							tempOdSet.add(column);
						}
					}
				}else if("ECM_LICENSE".equals(oblOdGubn)){
					licenseTableYn = "Y"; 
				}
			}
		}
			
		// 중복제거(추후 삭제)			
		if(tempOdSet != null) odColumnList = new ArrayList<String>(tempOdSet); 			 		search.setOdColumnList(odColumnList);	
		if(tempNotOdAdSet != null) notOdAdColumnList = new ArrayList<String>(tempNotOdAdSet); 	search.setNotOdAdColumnList(notOdAdColumnList);
		if(costList.size() > 0){
			search.setSrchCostTableList(costList);
		}
		if(codeNameList.size() > 0){
			search.setCodeNameColumnList(codeNameList);
		}
		
		///  아이템/컨테이너/마스터/MULTI/COST/HOLD/LICENSE 테이블 조회, 생성 (Y/N) 세팅 
		search.setSrchItemTableYn(itemTableYn); // 아이템 테이블 조회 유무 세팅
		search.setSrchContTableYn(contTableYn); // 컨테이너 테이블 조회 유무 세팅
		search.setSrchMultiTableYn(multiTableYn);
		search.setSrchHoldTableYn(holdTableYn);	// 홀드 테이블 조회 유무 세팅
		search.setSrchMasterYn(masterYn);  		// 마스터 정보 생성 유무 세팅
		search.setSrchLicenseTableYn(licenseTableYn); // 면장정보 테이블 조회 유무 세팅
		
		search.setSrchNameItemOriginCountryYn(srchNameItemOriginCountryYn);
		search.setSrchCodeNameItemOriginCountryYn(srchCodeNameItemOriginCountryYn);
		search.setSrchNameCntrCodeYn(srchNameCntrCodeYn);
		search.setSrchCodeNameCntrCodeYn(srchCodeNameCntrCodeYn);
		search.setSrchNameMultiDeliveryCodeYn(srchNameMultiDeliveryCodeYn);
		search.setSrchCodeNameMultiDeliveryCodeYn(srchCodeNameMultiDeliveryCodeYn);
		search.setSrchNameMultiCustomerCodeYn(srchNameMultiCustomerCodeYn);
		search.setSrchCodeNameMultiCustomerCodeYn(srchCodeNameMultiCustomerCodeYn);
		search.setSrchNameMultiPaymentGubnYn(srchNameMultiPaymentGubnYn);
		search.setSrchCodeNameMultiPaymentGubnYn(srchCodeNameMultiPaymentGubnYn);
		///
		
		if(totalCount == 0) totalCount = 1;
		
        for (int startNum = 0; startNum < totalCount; startNum += pageSize, page++) {
    		search.setPage(page + 1);     // page 1부터 시작해야 페이징 처리 가능 
    		search.setStartNum(startNum); // 시작 번호    
    		search.setPageSize(pageSize); // 갯수 (n개씩 나눠서 엑셀 작성)
    		
    		// 해당 엑셀다운로드양식에 들어갈 데이터 조회 
    		excelDataList = commonService.selectMakeExcelDownload(search);
            map.put("data", excelDataList);
            map.put("formItemList", formItemList);
            map.put("sheetNo", page); // sheetNo
            map.put("masterItemList", masterItemList);
            map.put("masterYn", masterYn);
            
            // totalCount => 30000건 미만 엑셀, 100000건 이상 csv
            if(totalCount < 30000) { 
            	excelMap = commonService.exceldownloadFormat(map, request); // excel
            }else {
            	excelMap = commonService.exceldownloadFormatCsv(map, request); // csv
                map.put("columnCount", Util.null2int(excelMap.get("columnCount")));
                excelMap.remove("columnCount"); // csv v양식 columnCount 빈 nullLine 만들때 사용
            }
            map.put("saveFile", Util.null2str(excelMap.get("saveFile"))); // 파일 이어서 쓰기 위해 savefile 경로 저장 
        }
        
        String logVal = "!!!!![END]!!!!!  FORM[" + Util.null2str(map.get("documentName"))+ "], OD COUNT[" + totalCount +"], Time["+ df.format((System.currentTimeMillis() - time) / 1000.0) + " s]";
        log.info(logVal);
		return new ResponseEntity<DownloadFileInfo>((DownloadFileInfo)objectMapper.convertValue(excelMap, DownloadFileInfo.class), HttpStatus.OK);
	}
	
	@ApiOperation(value = "통관정보 url 호출을 위한 데이터 암호화정보 조회", notes="통관정보 url 호출을 위한 데이터 암호화정보를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/customsClearanceInfo", method = RequestMethod.GET)
	public ResponseEntity<CustomsClearanceInfo> getCustomsClearanceInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver CustomsClearanceInfo info) throws Exception {
		return new ResponseEntity<CustomsClearanceInfo>(commonService.getCustomsClearanceInfo(info), HttpStatus.OK);
	}
	
	@ApiOperation(value = "하우스번호, 오더번호로 OD_JOB_NO 조회", notes="하우스번호, 오더번호로 OD_JOB_NO 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/odJobNo", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getOdJobNo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchOdJobNo search) throws Exception {
		return new ResponseEntity<Map<String, Object>>(commonService.getOdJobNo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "주소에서 기타정보 조회", notes="주소에서 기타정보를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/get/addressInfo", method = RequestMethod.GET)
	public ResponseEntity<AddressInfo> getAddressInfo(@PathVariable("entCode") String entCode, SearchAddressInfo search) throws Exception {
		return new ResponseEntity<AddressInfo>(commonService.getAddressInfo(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "한글 수하인 이름 주소를 영문으로 변환", notes="한글 수하인 이름 주소를 영문으로 변환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/KorToEngTranslation", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> korToEngTranslation(@PathVariable("entCode") String entCode, CustomsConsigNameAddrInfo info) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		String ConsigneeName = ExchangeKorEng.convert(Util.null2str(info.getConsigneeNameLoc()));
		String ConsigneeAddress = ExchangeKorEng.convert(Util.null2str(info.getConsigneeAddressLoc()));
		
		map.put("ConsigneeName", ConsigneeName);
		map.put("ConsigneeAddress", ConsigneeAddress);
		
		return new ResponseEntity<Map<String, Object>>(map,  HttpStatus.OK);
		
	}
	
	@RequestMapping(value="/srchNoRecords", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> srchNoRecords(@PathVariable("entCode") String entCode) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("srchNoRecords", "Y");
		return new ResponseEntity<Map<String, Object>>(map,  HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "카카오톡 전송", notes="카카오톡 전송합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/send/kakaoAlarmGroup", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> sendKakaoAlarmGroup(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> map, HttpServletRequest request) throws Exception {
		Map<String, Object> userMap = Util.getSessionUserInfo(request, jwtTokenProvider);
		if(!Util.isEmpty(userMap.get("USER_ID"))){
			map.put("userId", userMap.get("USER_ID"));
			map.put("updateUserId", userMap.get("USER_ID"));
			map.put("logUserName", userMap.get("USER_NAME"));
			map.put("logUserNameEng", userMap.get("USER_NAME_ENG"));
			map.put("updateUserName", userMap.get("USER_NAME"));
		}
		commonService.sendKakaoAlarmGroup(map, "SVC_PICKUP", true);
		return new ResponseEntity<Map<String, Object>>(new HashMap<String, Object>(), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "공통 설정된 엑셀 포맷에 따라 다운로드", notes="공통 설정된 엑셀 포맷에 따라 다운로드합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/list/excel/post", method = RequestMethod.POST)
	public ResponseEntity<DownloadFileInfo> makeExcelDownloadPost(@PathVariable("entCode") String entCode, @RequestBody SearchExcelList search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "registerUserId", null, null);
		long time = System.currentTimeMillis();
		DecimalFormat df = new DecimalFormat("#####0.000");
		log.info("!!!!![EXCEL DOWNLOAD START]!!!!" );
		
		ObjectMapper objectMapper = new ObjectMapper();
		// map => 포멧 아이템 검색용 
		Map<String, Object> map = (Map<String, Object>)objectMapper.convertValue(search, Map.class); 
		
		//	total 갯수
		int totalCount = commonService.selectExcelOrderCount(search);
		int page = 0; 
		int pageSize = 10000; // 10000개씩 나눠서 엑셀 작성
		//page : 0 startNum : 0 pageSize : 0
		//ORDER BY ${DefaultSort} OFFSET ${startNum} ROWS FETCH NEXT ${pageSize} ROWS ONLY
		
		Map<String, Object> excelMap = null;
		List<Map<String, Object>> excelDataList = null;

		// 엑셀다운로드 양식 정보 
		List<Map<String, Object>> formItemList = commonService.selectExcelDownFormatItem(map);
		List<Map<String, Object>> masterItemList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> costList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> codeNameList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempCost;
		Map<String, Object> tempCodeName;
		
		
		
		HashSet<String> tempOdSet = new HashSet<String>(); 
		HashSet<String> tempNotOdAdSet = new HashSet<String>();
		List<String> odColumnList = null;	
		List<String> notOdAdColumnList = null;
		
		String oblOdGubn = "";
		String column = "";
		String itemType = "";
		String chargeCode = "";
		String groupCode = "";
		String itemTableYn = "N"; // 아이템 테이블 조회 유무
		String contTableYn = "N"; // 컨테이너 테이블 조회 유무
		String multiTableYn = "N";
		String holdTableYn = "N"; // 홀드 테이블 조회 유무 
		String masterYn = "N"; // 마스터 정보 생성 유무 세팅
		String licenseTableYn = "N"; // 면장정보 생성 유무 세팅
		
		String srchNameItemOriginCountryYn = "N";
		String srchCodeNameItemOriginCountryYn = "N";
		String srchNameCntrCodeYn = "N";
		String srchCodeNameCntrCodeYn = "N";
		String srchNameMultiDeliveryCodeYn = "N";
		String srchCodeNameMultiDeliveryCodeYn = "N";
		String srchNameMultiCustomerCodeYn = "N";
		String srchCodeNameMultiCustomerCodeYn = "N";
		String srchNameMultiPaymentGubnYn = "N";
		String srchCodeNameMultiPaymentGubnYn = "N";
		
		// 쿼리에 필요한 COLUMN을  나눠서 저장 (item, container는 담지 않아도 됨) 
		// ETC6 => ECM_HOUSE, ECM_MASTER, ECM_CONTAINER, ECM_ITEM, ECM_COMMON, ECM_MULTI
		Map<String, Object> tmpMap;
		for(int i = formItemList.size() - 1; i >= 0; i--){
			tmpMap = formItemList.get(i);
			column = Util.null2str(tmpMap.get("ETC1"));
			if("CONSIGNEE_ADDRESS_ADD".equals(column) || "CONSIGNEE_ADDRESS_LOC_ADD".equals(column)){
				continue;
			}
			oblOdGubn = Util.null2str(tmpMap.get("ETC6")).replace("nullData", "");
			chargeCode = Util.null2str(tmpMap.get("ETC13"));
			groupCode = Util.null2str(tmpMap.get("ETC16"));
			itemType = Util.null2str(tmpMap.get("ITEM_TYPE"));
			if(!"".equals(oblOdGubn)){
				if("ECM_ITEM".equals(oblOdGubn)){
					itemTableYn = "Y"; // DB_FIELD=ECM.ITEM.HS_CODE_20
					if(column.contains("ORIGIN_COUNTRY")){
						if(!"".equals(groupCode)){
							srchNameItemOriginCountryYn = "Y";
							if("Y".equals(tmpMap.get("ETC18"))){
								srchCodeNameItemOriginCountryYn = "Y";
							}
						}
					}
				}else if("ECM_CONTAINER".equals(oblOdGubn)){
					contTableYn = "Y"; // DB_FIELD=ECM.CONTAINER.CNTR_CODE_1
					if(column.contains("CNTR_CODE")){
						if(!"".equals(groupCode)){
							srchNameCntrCodeYn = "Y";
							if("Y".equals(tmpMap.get("ETC18"))){
								srchCodeNameCntrCodeYn = "Y";
							}
						}
					}
				}else if("ECM_MULTI".equals(oblOdGubn)){
					multiTableYn = "Y";
					if(column.contains("CUSTOMER_CODE")){
						if(!"".equals(groupCode)){
							srchNameMultiCustomerCodeYn = "Y";
							if("Y".equals(tmpMap.get("ETC18"))){
								srchCodeNameMultiCustomerCodeYn = "Y";
							}
						}
					}else if(column.contains("DELIVERY_CODE")){
						if(!"".equals(groupCode)){
							srchNameMultiDeliveryCodeYn = "Y";
							if("Y".equals(tmpMap.get("ETC18"))){
								srchCodeNameMultiDeliveryCodeYn = "Y";
							}
						}
					}else if(column.contains("PAYMENT_GUBN")){
						if(!"".equals(groupCode)){
							srchNameMultiPaymentGubnYn = "Y";
							if("Y".equals(tmpMap.get("ETC18"))){
								srchCodeNameMultiPaymentGubnYn = "Y";
							}
						}
					}
				}else if("MASTER".equals(itemType)){
					masterYn = "Y";
					masterItemList.add(formItemList.remove(i));
					// itemType == "MASTER"도 같은 od테이블에서 조회해오기때문에 tempOdSet에 같이 담아준다.
					if(!"MBL_NO".equals(column) && !column.contains("_DATE")) {
						tempOdSet.add(column);
					}
				}else if(!"ECM_LICENSE".equals(oblOdGubn) && !"ECM_MULTI".equals(oblOdGubn)){
					if(!"MBL_NO".equals(column) && !"HBL_NO".equals(column) && !"CUSTOMER_CODE".equals(column) && !column.contains("_DATE")){ // MBL_NO, HBL_NO, _DATE 기본 조회로 세팅되어있어서 중복이 된다.
						if("HOLD_CODE".equals(column)) {
							holdTableYn = "Y";
						}
						// od, ad테이블에 없는 컬럼은 따로 담아서 처리 
						if(!chargeCode.equals("")){
							tempCost = new HashMap<String, Object>();
							tempCost.put("chargeCode", chargeCode);
							tempCost.put("column", column);
							costList.add(tempCost);
						}else if(!groupCode.equals("")){
							tempCodeName = new HashMap<String, Object>();
							tempCodeName.put("groupCode", groupCode);
							tempCodeName.put("field", column);
							tempCodeName.put("column", column + "_NAME");
							if("Y".equals(tmpMap.get("ETC18"))){
								tempCodeName.put("printCodeAndNameYn", "Y");
							}
							/** 2025-04-21 od, ad, multi 중복(CD_TYPE, DELIVERY_CODE) 컬럼 alias od로 고정 */
							if("CD_TYPE".equals(column) || "DELIVERY_CODE".equals(column)){
								tempCodeName.put("codeAlias", "od.");
							}else{
								tempCodeName.put("codeAlias", "");
							}
							
							codeNameList.add(tempCodeName);
							tmpMap.put("ETC1", column + "_NAME");
						}else if("SHIPPER_ADDRESS".equals(column) || "CONSIGNEE_ADDRESS".equals(column) || "MULTI_DELIVERY_NO".equals(column) || "GWT_TYPE".equals(column) || "ORDER_NO".equals(column)){
							tempNotOdAdSet.add(column);
						}else {
							tempOdSet.add(column);
						}
					}
				}else if("ECM_LICENSE".equals(oblOdGubn)){
					licenseTableYn = "Y"; 
				}
			}
		}
			
		// 중복제거(추후 삭제)			
		if(tempOdSet != null) odColumnList = new ArrayList<String>(tempOdSet); 			 		search.setOdColumnList(odColumnList);	
		if(tempNotOdAdSet != null) notOdAdColumnList = new ArrayList<String>(tempNotOdAdSet); 	search.setNotOdAdColumnList(notOdAdColumnList);
		if(costList.size() > 0){
			search.setSrchCostTableList(costList);
		}
		if(codeNameList.size() > 0){
			search.setCodeNameColumnList(codeNameList);
		}
		
		///  아이템/컨테이너/마스터/MULTI/COST/HOLD/LICENSE 테이블 조회, 생성 (Y/N) 세팅 
		search.setSrchItemTableYn(itemTableYn); // 아이템 테이블 조회 유무 세팅
		search.setSrchContTableYn(contTableYn); // 컨테이너 테이블 조회 유무 세팅
		search.setSrchMultiTableYn(multiTableYn);
		search.setSrchHoldTableYn(holdTableYn);	// 홀드 테이블 조회 유무 세팅
		search.setSrchMasterYn(masterYn);  		// 마스터 정보 생성 유무 세팅
		search.setSrchLicenseTableYn(licenseTableYn); // 면장정보 테이블 조회 유무 세팅
		
		search.setSrchNameItemOriginCountryYn(srchNameItemOriginCountryYn);
		search.setSrchCodeNameItemOriginCountryYn(srchCodeNameItemOriginCountryYn);
		search.setSrchNameCntrCodeYn(srchNameCntrCodeYn);
		search.setSrchCodeNameCntrCodeYn(srchCodeNameCntrCodeYn);
		search.setSrchNameMultiDeliveryCodeYn(srchNameMultiDeliveryCodeYn);
		search.setSrchCodeNameMultiDeliveryCodeYn(srchCodeNameMultiDeliveryCodeYn);
		search.setSrchNameMultiCustomerCodeYn(srchNameMultiCustomerCodeYn);
		search.setSrchCodeNameMultiCustomerCodeYn(srchCodeNameMultiCustomerCodeYn);
		search.setSrchNameMultiPaymentGubnYn(srchNameMultiPaymentGubnYn);
		search.setSrchCodeNameMultiPaymentGubnYn(srchCodeNameMultiPaymentGubnYn);
		///
		
		if(totalCount == 0) totalCount = 1;
		
        for (int startNum = 0; startNum < totalCount; startNum += pageSize, page++) {
    		search.setPage(page + 1);     // page 1부터 시작해야 페이징 처리 가능 
    		search.setStartNum(startNum); // 시작 번호    
    		search.setPageSize(pageSize); // 갯수 (n개씩 나눠서 엑셀 작성)
    		
    		// 해당 엑셀다운로드양식에 들어갈 데이터 조회 
    		excelDataList = commonService.selectMakeExcelDownload(search);
            map.put("data", excelDataList);
            map.put("formItemList", formItemList);
            map.put("sheetNo", page); // sheetNo
            map.put("masterItemList", masterItemList);
            map.put("masterYn", masterYn);
            
            // totalCount => 30000건 미만 엑셀, 100000건 이상 csv
            if(totalCount < 30000) { 
            	excelMap = commonService.exceldownloadFormat(map, request); // excel
            }else {
            	excelMap = commonService.exceldownloadFormatCsv(map, request); // csv
                map.put("columnCount", Util.null2int(excelMap.get("columnCount")));
                excelMap.remove("columnCount"); // csv v양식 columnCount 빈 nullLine 만들때 사용
            }
            map.put("saveFile", Util.null2str(excelMap.get("saveFile"))); // 파일 이어서 쓰기 위해 savefile 경로 저장 
        }
        
        String logVal = "!!!!![END]!!!!!  FORM[" + Util.null2str(map.get("documentName"))+ "], OD COUNT[" + totalCount +"], Time["+ df.format((System.currentTimeMillis() - time) / 1000.0) + " s]";
        log.info(logVal);
		return new ResponseEntity<DownloadFileInfo>((DownloadFileInfo)objectMapper.convertValue(excelMap, DownloadFileInfo.class), HttpStatus.OK);
	}
	
	@ApiOperation(value = "UPS 통관목록 출력물 메일 전송", notes="UPS 통관목록 출력물 메일 전송합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
    })
    @RequestMapping(value="/saveMailLogs", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> saveMailLogs(@PathVariable("entCode") String entCode, @RequestBody Map<String, Object> map, HttpServletRequest request) throws Exception {
    	Map<String, Object> userMap = Util.getSessionUserInfo(request, jwtTokenProvider);
    	if(!Util.isEmpty(userMap.get("USER_ID"))){
			map.put("userId", userMap.get("USER_ID"));
			map.put("registerUserId", userMap.get("USER_ID"));
			map.put("updateUserId", userMap.get("USER_ID"));
			map.put("logUserName", userMap.get("USER_NAME"));
			map.put("logUserNameEng", userMap.get("USER_NAME_ENG"));
			map.put("updateUserName", userMap.get("USER_NAME"));
		}
		commonService.saveMailLogs(request, map);
		Map returnMap = commonService.sendEmail(map);
		commonService.updateEmailData((List)returnMap.get("result"));
		returnMap.put("result", null);
		return new ResponseEntity<Map<String, Object>>(returnMap, HttpStatus.OK);
    }
}
