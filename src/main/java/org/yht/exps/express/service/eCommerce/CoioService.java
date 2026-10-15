package org.yht.exps.express.service.eCommerce;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.CoioMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.eCommerce.coio.CoioDet;
import org.yht.exps.express.model.eCommerce.coio.CoioDetList;
import org.yht.exps.express.model.eCommerce.coio.CoioLoc;
import org.yht.exps.express.model.eCommerce.coio.CoioLocList;
import org.yht.exps.express.model.eCommerce.coio.CoioLocSub;
import org.yht.exps.express.model.eCommerce.coio.CoioLocSubList;
import org.yht.exps.express.model.eCommerce.coio.CoioSum;
import org.yht.exps.express.model.eCommerce.coio.CoioSumList;
import org.yht.exps.express.model.eCommerce.coio.search.SearchCoioList;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.util.Util;

@Service
@Transactional
public class CoioService {

	@Autowired
	CustomProperties customProperties;
	
	@Autowired
    CommonMapper commonMapper;

	@Autowired
	CoioMapper coioMapper;
	
    @Autowired
    EnterpriseMapper enterpriseMapper;

    @Autowired
    CommonService commonService;
    
	@Autowired
	RestHighLevelClient client;		
	

	/**
	 * 반입/반출(Detail) 조회
	 * @param SearchCoioList
	 * @return
	 * @throws Exception
	 */	
	
	public CoioDetList selectCoioDetailList(SearchCoioList search) throws Exception {
		
		CoioDetList coioDetList = new CoioDetList();
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
		Util.getCryptographyYn(search);
		Util.accessControlDatabase(search, "userLanguage|srchCusType|srchOrigDestType", true, "srchDateGbn", "od.AR_DATE|CARRYIN_DATE|CARRYOUT_DATE|STOCK_DATE_TIME|WH_CARRYOUT_DATE", "srchNoType", "HBL_NO|MBL_NO|PO_NO|CONSIGNEE_NAME|CONSIGNEE_NAME_LOC|CONSIGNEE_TEL|SHIPPER_NAME|SHIPPER_TEL|COMMODITY_ITEM");
		List<CoioDet> list  = coioMapper.selectCoioDetailList(search);
		coioDetList.setCoioDetList(list);
		if(list.size() > 0) {
			coioDetList.setTotal(list.get(0).getTotal());
		}else {
			coioDetList.setTotal(0);
		}
		
		return coioDetList; 
	}	
	
	/**
	 * 반입/반출(summury) 조회
	 * @param SearchCoioList
	 * @return
	 * @throws Exception
	 */	
	public CoioSumList selectCoioSummaryList(SearchCoioList search) throws Exception {
		
		CoioSumList coioSumList = new CoioSumList();
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
		Util.getCryptographyYn(search, "srchCusType");
		Util.accessControlDatabase(search, "srchCusType|srchOrigDestType", true, "srchDateGbn", "AR_DATE", "srchNoType", "MBL_NO");
		List<CoioSum> list  = coioMapper.selectCoioSummaryList(search);
		coioSumList.setCoioSumList(list);
		if(list.size() > 0) {
			coioSumList.setTotal(list.get(0).getTotal());
		}else {
			coioSumList.setTotal(0);
		}
		
		return coioSumList; 
	}		
	
	
	/**
	 * 반입/반출(location) 조회
	 * @param SearchCoioList
	 * @return
	 * @throws Exception
	 */	
	
	public CoioLocList selectCoioLocationList(SearchCoioList search) throws Exception {
		
		CoioLocList coioLocList = new CoioLocList();
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
		Util.getCryptographyYn(search, "srchType");
		Util.accessControlDatabase(search, "userLanguage", true, "srchDateGbn", "", "srchType", "HBL_NO|PO_NO|CUST_CODE_LV1|CONSIGNEE_NAME_LOC|CONSIGNEE_NAME");
		List<CoioLoc> list  = coioMapper.selectCoioLocationList(search);
		coioLocList.setCoioLocList(list);
		if(list.size() > 0) {
			coioLocList.setTotal(list.get(0).getTotal());
		}else {
			coioLocList.setTotal(0);
		}
		
		return coioLocList; 
	}	
	
	/**
	 * 반입/반출(location) sub 조회
	 * @param SearchCoioList
	 * @return
	 * @throws Exception
	 */	
	
	public CoioLocSubList selectCoioLocationSubList(SearchCoioList search) throws Exception {
		
		CoioLocSubList coioLocSubList = new CoioLocSubList();
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
		Util.getCryptographyYn(search, "srchType");
		Util.accessControlDatabase(search, "userLanguage|srchCusType", true, "srchDateGbn", "", "srchType", "HBL_NO|PO_NO|CUST_CODE_LV1|CONSIGNEE_NAME_LOC|CONSIGNEE_NAME");
		List<CoioLocSub> list  = coioMapper.selectCoioLocationSubList(search);
		coioLocSubList.setCoioLocSubList(list);
		if(list.size() > 0) {
			coioLocSubList.setTotal(list.get(0).getTotal());
		}else {
			coioLocSubList.setTotal(0);
		}
		
		return coioLocSubList; 
	}	
	
	
	
	
}
