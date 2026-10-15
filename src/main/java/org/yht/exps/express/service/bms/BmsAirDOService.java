package org.yht.exps.express.service.bms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsAirDOMapper;
import org.yht.exps.express.model.bms.bmsAirDO.BmsAirDO;
import org.yht.exps.express.model.bms.bmsAirDO.BmsAirDOList;
import org.yht.exps.express.model.bms.bmsAirDO.search.SearchBmsAirDOList;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.Filters;
import org.yht.exps.express.model.search.Sort;
import org.yht.util.Util;

@Service
@Transactional
public class BmsAirDOService {

	@Autowired
	CommonMapper commonMapper;
	@Autowired
	BmsAirDOMapper bmsAirDOMapper;

	/**
	 * Paging, Sort, Filter 관련 처리
	 * @param search
	 * @throws Exception
	 */
	private static void setSearchCondition(SearchBmsAirDOList search) throws Exception {
		if(search.getPage() != 0 && search.getPageSize() != 0) {
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}

		/** 화물관리번호(cargoReferenceNo) 매핑 처리 */
		if(Util.null2str(search.getChangeField()).equals("Y")) {
			List<Sort> sortList = search.getSort();

			if(sortList != null) {
				for(Sort sort : sortList) {
					if(sort.getField().equals("cargoReferenceNoDisp")) {
						sort.setField("cargoReferenceNo");
					}
				}
			}
			Util.camelCaseToUnderscore(search);

			Filter filterList = search.getFilter();
			if(filterList != null) {
				for(Filters filter : filterList.getFilters()) {
					if (filter.getField().equals("CARGO_REFERENCE_NO_DISP")) {
						filter.setField("CARGO_REFERENCE_NO");
					}
				}
			}
		}
	}

	/**
	 * 항공D/O조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsAirDOList selectBmsAirDOList(SearchBmsAirDOList search) throws Exception {
		BmsAirDOList bmsAirDOList = new BmsAirDOList();

		setSearchCondition(search);

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
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "|tbi.CARRYIN_DATE|tbd.RECV_DATE|noDateChk", "srchNoType", "|tbd.BL_NO|tbi.CARGO_REFERENCE_NO|tbi.CUSTOMS_REPORT_NO");
		List<BmsAirDO> list = bmsAirDOMapper.selectBmsAirDOList(search);
		bmsAirDOList.setBmsAirDOList(list);

		if(list.size() > 0) {
			bmsAirDOList.setTotal(list.get(0).getTotal());
		}else {
			bmsAirDOList.setTotal(0);
		}
		return bmsAirDOList;
	}

}
