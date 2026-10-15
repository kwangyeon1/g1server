package org.yht.exps.express.service.bms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsAllocateCarMapper;
import org.yht.exps.express.model.bms.bmsAllocateCar.BmsAllocateCar;
import org.yht.exps.express.model.bms.bmsAllocateCar.BmsAllocateCarList;
import org.yht.exps.express.model.bms.bmsAllocateCar.BmsAllocateCarSaveResult;
import org.yht.exps.express.model.bms.bmsAllocateCar.search.SearchBmsAllocateCarList;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutSaveResult;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.Filters;
import org.yht.exps.express.model.search.Sort;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsAllocateCarService {

	@Autowired
	CommonMapper commonMapper;
	@Autowired
	BmsAllocateCarMapper bmsAllocateCarMapper;

	/**
	 * Paging, Sort, Filter 관련 처리
	 * @param search
	 * @throws Exception
	 */
	private static void setSearchCondition(SearchBmsAllocateCarList search) throws Exception {
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
	 * 배차예정정보 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsAllocateCarList selectBmsAllocateCarList(SearchBmsAllocateCarList search) throws Exception {
		BmsAllocateCarList bmsAllocateCarList = new BmsAllocateCarList();

		setSearchCondition(search);

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		} else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "noDateChk|A.NOTIFY_DATE_TIME", "srchNoType", "A.APPROVAL_NO|B.BL_NO|A.CARGO_REFERENCE_NO");
		List<BmsAllocateCar> list = bmsAllocateCarMapper.selectBmsAllocateCarList(search);
		bmsAllocateCarList.setBmsAllocateCarList(list);

		if(list.size() > 0) {
			bmsAllocateCarList.setTotal(list.get(0).getTotal());
		}else {
			bmsAllocateCarList.setTotal(0);
		}
		return bmsAllocateCarList;
	}
	
	/**
	 * 일괄배차등록 팝업 상세조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsAllocateCar selectBmsAllocateCarDetail(SearchBmsAllocateCarList search) throws Exception {
		BmsAllocateCar bmsAllocateCar = new BmsAllocateCar();
		bmsAllocateCar = bmsAllocateCarMapper.selectBmsAllocateCarDetail(search);
		
		return bmsAllocateCar;
	}
	
	/**
	 * 일괄배차등록 팝업 update
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsAllocateCarSaveResult updateBmsAllocateCar(BmsAllocateCar bmsAllocateCar, HttpServletRequest request) throws Exception {
		BmsAllocateCarSaveResult bmsAllocateCarSaveResult = new BmsAllocateCarSaveResult();
		List<Map<String, Object>> updateList = new ArrayList<Map<String, Object>>();
		updateList = bmsAllocateCar.getDataList();
		int succCnt = 0;
		int failCnt = 0;
		
		for (Map<String, Object> updateMap : updateList) {
			Integer targetNo = Integer.parseInt(Util.null2str(updateMap.get("allocateCarNotiNo")));
			String cargoReferenceNo = Util.null2str(updateMap.get("cargoReferenceNo"));
			
			//log.info("ParamNo: {}", targetNo);
			bmsAllocateCar.setAllocateCarNotiNo(targetNo);
			bmsAllocateCar.setCargoReferenceNo(cargoReferenceNo);
			
			int row = bmsAllocateCarMapper.updateBmsAllocateCar(bmsAllocateCar);
			if(row > 0) {
				succCnt++;
			} else {
				failCnt++;
			}
		}
		
		//log.info("update cnt:{}", succCnt);
		//log.info("not update cnt:{}", failCnt);
		bmsAllocateCarSaveResult.setSuccCnt(succCnt);
		bmsAllocateCarSaveResult.setFailCnt(failCnt);
		bmsAllocateCarSaveResult.setResultCode("A012051"); // 저장 되었습니다.
		
		return bmsAllocateCarSaveResult;
	}

}