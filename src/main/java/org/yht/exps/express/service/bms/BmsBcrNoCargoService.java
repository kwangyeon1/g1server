package org.yht.exps.express.service.bms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsBcrNoCargoMapper;
import org.yht.exps.express.model.bms.bmsBcrNoCargo.BmsBcrNoCargo;
import org.yht.exps.express.model.bms.bmsBcrNoCargo.BmsBcrNoCargoList;
import org.yht.exps.express.model.bms.bmsBcrNoCargo.search.SearchBmsBcrNoCargoList;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.Filters;
import org.yht.exps.express.model.search.Sort;
import org.yht.util.Util;

@Service
@Transactional
public class BmsBcrNoCargoService {

	@Autowired
	BmsBcrNoCargoMapper bmsBcrNoCargoMapper;

	/**
	 * BCR 무적 조회 - BCR 무적 Max Scan Date 조회
	 * @return String
	 * @throws Exception
	 */
	public String selectMaxScanDateTime() throws Exception {
		return bmsBcrNoCargoMapper.selectMaxScanDateTime();
	}

	/**
	 * BCR 무적 조회 - BCR 무적 이관 데이터 조회
	 * @param maxScanDateTime
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectBcrCargoTransferList(String maxScanDateTime, String entCode) throws Exception {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("maxScanDateTime", StringUtils.isNotEmpty(maxScanDateTime) ? maxScanDateTime : "2025-04-01 00:00:00.000");
		if(entCode.equals("PANTK")) {	// TSV_XRAY_BAK 판토스일경우 TSV_XRAY_BAK_LXP 조회하도록 수정
			return bmsBcrNoCargoMapper.selectBcrCargoTransferListPantos(paramMap);
		}else {
			return bmsBcrNoCargoMapper.selectBcrCargoTransferList(paramMap);
		}
	}

	/**
	 * BCR 무적 조회 - BCR 무적 조회
	 * @param search
	 * @param bcrTransferList
	 * @param req
	 * @return List
	 * @throws Exception
	 */
	public BmsBcrNoCargoList selectBcrNoCargoList(SearchBmsBcrNoCargoList search, List<Map<String, Object>> bcrTransferList, HttpServletRequest req) throws Exception {
		BmsBcrNoCargoList bmsBcrNoCargoList = new BmsBcrNoCargoList();

		/* Insert Transfer Data BCR Cargo */
		for(Map<String, Object> bcrData : bcrTransferList) {
			bcrData.put("userId", search.getSrchUserId());
			bmsBcrNoCargoMapper.insertBcrCheck(bcrData);
		}

		/* Set Multi Search Data */
		search.setSrchNoData(search.getSrchNo(), search.getPage(), search.getPageSize());

		/* Set Search Filter & Sort Data */
		setSearchCondition(search);

		/* Search Data BCR No Cargo */
		Util.accessControlDatabase(search, null, true, null, null, null, null);
		List<BmsBcrNoCargo> searchBcrNoCargoList = bmsBcrNoCargoMapper.selectBcrNoCargoList(search);

		/* Set Data BCR No Cargo */
		bmsBcrNoCargoList.setBmsBcrNoCargoList(searchBcrNoCargoList);
		bmsBcrNoCargoList.setTotal(searchBcrNoCargoList.size() > 0 ? searchBcrNoCargoList.get(0).getTotal() : 0);
		return bmsBcrNoCargoList;
	}

	private void setSearchCondition(SearchBmsBcrNoCargoList search) throws Exception {
		/* Paging 관련 처리 */
		if(search.getPage() != 0 && search.getPageSize() != 0) {
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}

		/* Set Search Sort Data */
		List<Sort> lst = search.getSort();
        if( lst != null) {
            for (Sort sort : lst) { // 가상필드 다른 이름으로 처리
                if(sort.getField().equals("scanNo")) sort.setField("SCAN_NO");
                else if(sort.getField().equals("scanDate")) sort.setField("SCAN_DATE");
                else if(sort.getField().equals("scanTime")) sort.setField("SCAN_TIME");
                else if(sort.getField().equals("checkYn")) sort.setField("CHECK_YN");
                else if(sort.getField().equals("checkRemarks")) sort.setField("CHECK_REMARKS");
                else if(sort.getField().equals("registerUserId")) sort.setField("REGISTER_USER_ID");
                else if(sort.getField().equals("registerDateTime")) sort.setField("REGISTER_DATE_TIME");
                else if(sort.getField().equals("updateUserId")) sort.setField("UPDATE_USER_ID");
                else if(sort.getField().equals("updateDateTime")) sort.setField("UPDATE_DATE_TIME");
            }
        }
        Util.camelCaseToUnderscore(search);

		/* Set Search Filter Data */
		Filter filterList = search.getFilter();
		if(filterList != null) {
			for(Filters filter : filterList.getFilters()) {
				if(filter.getField().equals("scanNo")) filter.setField("SCAN_NO");
				if(filter.getField().equals("checkYn")) filter.setField("CHECK_YN");
				if(filter.getField().equals("checkRemarks")) filter.setField("CHECK_REMARKS");
				if(filter.getField().equals("registerUserId")) filter.setField("REGISTER_USER_ID");
				if(filter.getField().equals("updateUserId")) filter.setField("UPDATE_USER_ID");
			}
		}
	}

	/**
	 * BCR 무적 조회 - BCR 무적 조회 > 일괄체크완료 팝업 저장
	 * @param map
	 * @throws Exception
	 */
	public void updateBcrNoCargo(Map<String, Object> map) throws Exception {
		bmsBcrNoCargoMapper.updateBcrNoCargo(map);
	}
}