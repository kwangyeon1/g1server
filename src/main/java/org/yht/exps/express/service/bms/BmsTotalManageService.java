package org.yht.exps.express.service.bms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsTotalManageMapper;
import org.yht.exps.express.mapper.pda.PdaCargoReceiveMapper;
import org.yht.exps.express.model.bms.bmsCargo.BmsMasterDest;
import org.yht.exps.express.model.bms.bmsTotalManage.BmsTotalManage;
import org.yht.exps.express.model.bms.bmsTotalManage.BmsTotalManageDetail;
import org.yht.exps.express.model.bms.bmsTotalManage.BmsTotalManageList;
import org.yht.exps.express.model.bms.bmsTotalManage.search.SearchTotalManage;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsTotalManageService {

	@Autowired
	BmsTotalManageMapper bmsTotalManageMapper;

	@Autowired
	PdaCargoReceiveMapper pdaCargoReceiveMapper;

	/**
	 * 통합 관리 - 마스터 조회
	 * @param maxScanDateTime
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public BmsTotalManageList selectTotalManageList(SearchTotalManage search) throws Exception {
		BmsTotalManageList bmsTotalManageList = new BmsTotalManageList();

		if( search.getPage() != 0 && search.getPageSize() != 0 ) search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		if( Util.null2str(search.getChangeField()).equals("Y") ) Util.camelCaseToUnderscore(search);

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if( !"".equals(Util.null2str(search.getSrchNo())) ) searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));

		if( searchNoList.size() > 1 ) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		if( "CARGO_REFERENCE_NO".equals(search.getSrchNoType()) ) {
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace("-", ""));
		}

		/* Get Master Data */
		List<BmsTotalManage> lst = bmsTotalManageMapper.selectTotalManageList(search);
		bmsTotalManageList.setTotalManageMasterList(lst);

		if( lst != null && lst.size() > 0 ) bmsTotalManageList.setTotal(lst.get(0).getTotal());
		else bmsTotalManageList.setTotal(0);

		return bmsTotalManageList;
	}

	/**
	 * 통합 관리 - 하우스 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsTotalManageList selectTotalManageDetailList(SearchTotalManage search) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		BmsTotalManageList bmsTotalManageList = new BmsTotalManageList();

		if( search.getPage() != 0 && search.getPageSize() != 0 ) search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		if( Util.null2str(search.getChangeField()).equals("Y") ) Util.camelCaseToUnderscore(search);

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if( !"".equals(Util.null2str(search.getSrchNo())) ) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if( searchNoList.size() > 1 ) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		if( "CARGO_REFERENCE_NO".equals(search.getSrchNoType()) ) {
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace("-", ""));
		}

		log.debug("[selectTotalManageDetailList] srchRowMblNo : {}, srchRowColNm : {}", search.getSrchRowMblNo(), search.getSrchRowColNm());
		/* Get house Data */
		List<BmsTotalManageDetail> lst = bmsTotalManageMapper.selectTotalManageDetailList(search);
		bmsTotalManageList.setTotalManageHouseList(lst);

		if( lst != null && lst.size() > 0 ) bmsTotalManageList.setTotal(lst.get(0).getTotal());
		else bmsTotalManageList.setTotal(0);

		return bmsTotalManageList;
	}

	/**
	 * 통합 관리 - 입항화물도착 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> selectCargoReceiveList(Map<String, Object> search) throws Exception {
		List<BmsMasterDest> cargoRecvList = pdaCargoReceiveMapper.selectMasterDestList(search);

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("cargoReceiveList", cargoRecvList);
		resultMap.put("total", cargoRecvList.size() > 0 ? cargoRecvList.size() : 0);
		return resultMap;
	}

	/**
	 * 통합 관리 - 택배 인터페이스 차수 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> selectDeliveryCount(Map<String, Object> search) throws Exception {
		List<Integer> deliveryCntList = bmsTotalManageMapper.selectEmDeliveryCount(search);

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("deliveryCntList", deliveryCntList);
		return resultMap;
	}

	/**
	 * 통합 관리 - 마스터 조회
	 * @param maxScanDateTime
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public Map<String, Object> selectDeliveryListExcel(Map<String, Object> search) throws Exception {
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

		/* Get Master Data */
		if("EM".equals(Util.null2str(search.get("srchDeliveryTypeGbn")))) {
			dataList = bmsTotalManageMapper.selectEmDeliveryListExcel(search);
		}else if("CJ".equals(Util.null2str(search.get("srchDeliveryTypeGbn"))) || "ALCJ".equals(Util.null2str(search.get("srchDeliveryTypeGbn")))) {
			dataList = bmsTotalManageMapper.selectCjDeliveryListExcel(search);
		}else {
			dataList = bmsTotalManageMapper.selectClDeliveryListExcel(search);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("dataList", dataList);
		return resultMap;
	}
}