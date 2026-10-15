package org.yht.exps.express.service.bms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsPrintMapper;
import org.yht.exps.express.model.bms.bmsPrint.BmsGenericPrintList;
import org.yht.exps.express.model.bms.bmsPrint.BmsPrint;
import org.yht.exps.express.model.bms.bmsPrint.BmsPrintList;
import org.yht.exps.express.model.bms.bmsPrint.search.SearchBmsPrintList;
import org.yht.exps.express.model.bms.out.BmsOutSample;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsPrintService {

	@Autowired
	CommonMapper commonMapper;
	@Autowired
	BmsPrintMapper bmsPrintMapper;

	/**
	 * 반입신고 출력용 추가 데이터 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsPrintList selectBmsInPrintList(SearchBmsPrintList search) throws Exception {
		BmsPrintList bmsPrintList = new BmsPrintList();
		if(search.getPage() != 0 && search.getPageSize() != 0) {search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")) {Util.camelCaseToUnderscore(search);}

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

		Util.accessControlDatabase(search, null, false, null, null, "srchNoType", "|BL_NO|CARGO_REFERENCE_NO|CUSTOMS_REPORT_NO");
		List<BmsPrint> list = bmsPrintMapper.selectBmsInPrintList(search);
		bmsPrintList.setBmsPrintList(list);

		if(list.size() > 0) {
			bmsPrintList.setTotal(list.get(0).getTotal());
		} else {
			bmsPrintList.setTotal(0);
		}

		return bmsPrintList;
	}

	/**
	 * 반출신고 출력용 추가 데이터 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsPrintList selectBmsOutPrintList(SearchBmsPrintList search) throws Exception {
		BmsPrintList bmsPrintList = new BmsPrintList();
		if(search.getPage() != 0 && search.getPageSize() != 0) {search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")) {Util.camelCaseToUnderscore(search);}

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

		Util.accessControlDatabase(search, null, false, null, null, "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|A.CUSTOMS_REPORT_NO");
		List<BmsPrint> list = bmsPrintMapper.selectBmsOutPrintList(search);
		bmsPrintList.setBmsPrintList(list);

		if(list.size() > 0) {
			bmsPrintList.setTotal(list.get(0).getTotal());
		} else {
			bmsPrintList.setTotal(0);
		}

		return bmsPrintList;
	}
	
	/**
	 * 반출신고 출력용 추가 데이터 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsGenericPrintList selectBmsSamplePrintList(SearchBmsPrintList search) throws Exception {
		BmsGenericPrintList bmsGenericPrintList = new BmsGenericPrintList();
		if(search.getPage() != 0 && search.getPageSize() != 0) {search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")) {Util.camelCaseToUnderscore(search);}

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
		search.setDefaultSort("SAMPLE_NO");

		Util.accessControlDatabase(search, null, false, null, null, "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO");
		List<BmsOutSample> list = bmsPrintMapper.selectBmsSamplePrintList(search);
		bmsGenericPrintList.setBmsPrintList(list);

		if(list.size() > 0) {
			bmsGenericPrintList.setTotal(list.get(0).getTotal());
		} else {
			bmsGenericPrintList.setTotal(0);
		}

		return bmsGenericPrintList;
	}
	/**
	 * 검사계획통보 출력용 추가 데이터 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsPrintList selectBmsInspectPrintList(SearchBmsPrintList search) throws Exception {
		BmsPrintList bmsPrintList = new BmsPrintList();
		if(search.getPage() != 0 && search.getPageSize() != 0) {search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")) {Util.camelCaseToUnderscore(search);}

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

		Util.accessControlDatabase(search, null, false, null, null, "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO");
		List<BmsPrint> list = bmsPrintMapper.selectBmsInspectPrintList(search);
		bmsPrintList.setBmsPrintList(list);

		if(list.size() > 0) {
			bmsPrintList.setTotal(list.get(0).getTotal());
		} else {
			bmsPrintList.setTotal(0);
		}

		return bmsPrintList;
	}

	/**
	 * 반출통고신고 출력용 추가 데이터 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsPrintList selectBmsOutNoticePrintList(SearchBmsPrintList search) throws Exception {
		BmsPrintList bmsPrintList = new BmsPrintList();
		if(search.getPage() != 0 && search.getPageSize() != 0) {search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")) {Util.camelCaseToUnderscore(search);}

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

		Util.accessControlDatabase(search, null, false, null, null, "srchNoType", "|A.BL_NO|A.CARGO_REFERENCE_NO|B.CUSTOMS_REPORT_NO");
		List<BmsPrint> list = bmsPrintMapper.selectBmsOutNoticePrintList(search);
		bmsPrintList.setBmsPrintList(list);

		if(list.size() > 0) {
			bmsPrintList.setTotal(list.get(0).getTotal());
		} else {
			bmsPrintList.setTotal(0);
		}

		return bmsPrintList;
	}

	/**
	 * 반입물품이상신고 출력용 추가 데이터 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsPrintList selectBmsAbnormalPrintList(SearchBmsPrintList search) throws Exception {
		BmsPrintList bmsPrintList = new BmsPrintList();
		if(search.getPage() != 0 && search.getPageSize() != 0) {search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")) {Util.camelCaseToUnderscore(search);}

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

		Util.accessControlDatabase(search, null, false, null, null, "srchNoType", "|A.MBL_NO|A.CUSTOMS_REPORT_NO");
		List<BmsPrint> list = bmsPrintMapper.selectBmsAbnormalPrintList(search);
		bmsPrintList.setBmsPrintList(list);

		if(list.size() > 0) {
			bmsPrintList.setTotal(list.get(0).getTotal());
		} else {
			bmsPrintList.setTotal(0);
		}

		return bmsPrintList;
	}

	public String selectBmsAbnormalPrintcontactNo(Map<String, Object> search) throws Exception {
		return bmsPrintMapper.selectBmsAbnormalPrintcontactNo(search);
	}

}
