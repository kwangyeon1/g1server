package org.yht.exps.express.service.bms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsEcMapper;
import org.yht.exps.express.model.bms.bmsEc.BmsEcData;
import org.yht.exps.express.model.bms.bmsEc.BmsEcEtc;
import org.yht.exps.express.model.bms.bmsEc.BmsEcEtcDetail;
import org.yht.exps.express.model.bms.bmsEc.BmsEcEtcList;
import org.yht.exps.express.model.bms.bmsEc.BmsEcIn;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInDetail;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInList;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInPlan;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInPlanDetail;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInPlanList;
import org.yht.exps.express.model.bms.bmsEc.BmsEcInPlanRsgDetail;
import org.yht.exps.express.model.bms.bmsEc.BmsEcOut;
import org.yht.exps.express.model.bms.bmsEc.BmsEcOutDetail;
import org.yht.exps.express.model.bms.bmsEc.BmsEcOutList;
import org.yht.exps.express.model.bms.bmsEc.BmsEcOutPlan;
import org.yht.exps.express.model.bms.bmsEc.BmsEcOutPlanList;
import org.yht.exps.express.model.bms.bmsEc.BmsEcSend;
import org.yht.exps.express.model.bms.bmsEc.BmsEcSendResult;
import org.yht.exps.express.model.bms.bmsEc.search.SearchEc;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsEcService {

	@Autowired
	BmsEcMapper bmsEcMapper;

	/**
	 * EC송수신조회 - 반입예정 마스터 조회
	 * @param maxScanDateTime
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public BmsEcInPlanList selectEcInPlanList(SearchEc search) throws Exception {
		BmsEcInPlanList bmsEcInPlanList = new BmsEcInPlanList();

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
		List<BmsEcInPlan> lst = bmsEcMapper.selectEcInPlanList(search);
		bmsEcInPlanList.setEcInPlanMasterList(lst);

		if( lst != null && lst.size() > 0 ) bmsEcInPlanList.setTotal(lst.get(0).getTotal());
		else bmsEcInPlanList.setTotal(0);

		return bmsEcInPlanList;
	}

	/**
	 * EC송수신조회 - 반입예정 상세 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsEcInPlanList selectEcInPlanDetailList(SearchEc search) throws Exception {
		BmsEcInPlanList bmsEcInPlanList = new BmsEcInPlanList();

		if( search.getPage() != 0 && search.getPageSize() != 0 ) search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		if( Util.null2str(search.getChangeField()).equals("Y") ) Util.camelCaseToUnderscore(search);

		List<BmsEcInPlanDetail> lst = bmsEcMapper.selectEcInPlanDetailList(search);
		bmsEcInPlanList.setEcInPlanDetailList(lst);

		if( lst != null && lst.size() > 0 ) bmsEcInPlanList.setTotal(lst.get(0).getTotal());
		else bmsEcInPlanList.setTotal(0);

		return bmsEcInPlanList;
	}
	
	/**
	 * EC송수신조회 - 반입예정RSG 상세 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsEcInPlanList selectEcInPlanDetailRsgList(SearchEc search) throws Exception {
		BmsEcInPlanList bmsEcInPlanList = new BmsEcInPlanList();

		if( search.getPage() != 0 && search.getPageSize() != 0 ) search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		if( Util.null2str(search.getChangeField()).equals("Y") ) Util.camelCaseToUnderscore(search);

		List<BmsEcInPlanRsgDetail> lst = bmsEcMapper.selectEcInPlanDetailRsgList(search);
		bmsEcInPlanList.setEcInPlanDetailRsgList(lst);

		if( lst != null && lst.size() > 0 ) bmsEcInPlanList.setTotal(lst.get(0).getTotal());
		else bmsEcInPlanList.setTotal(0);

		return bmsEcInPlanList;
	}
	
	/**
	 * EC송수신조회 - 반입 마스터 조회
	 * @param maxScanDateTime
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public BmsEcInList selectEcInList(SearchEc search) throws Exception {
		BmsEcInList bmsEcInList = new BmsEcInList();

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
		List<BmsEcIn> lst = bmsEcMapper.selectEcInList(search);
		bmsEcInList.setEcInMasterList(lst);

		if( lst != null && lst.size() > 0 ) bmsEcInList.setTotal(lst.get(0).getTotal());
		else bmsEcInList.setTotal(0);

		return bmsEcInList;
	}

	/**
	 * EC송수신조회 - 반입 상세 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsEcInList selectEcInDetailList(SearchEc search) throws Exception {
		BmsEcInList bmsEcInList = new BmsEcInList();

		if( search.getPage() != 0 && search.getPageSize() != 0 ) search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		if( Util.null2str(search.getChangeField()).equals("Y") ) Util.camelCaseToUnderscore(search);

		List<BmsEcInDetail> lst = bmsEcMapper.selectEcInDetailList(search);
		bmsEcInList.setEcInDetailList(lst);

		if( lst != null && lst.size() > 0 ) bmsEcInList.setTotal(lst.get(0).getTotal());
		else bmsEcInList.setTotal(0);

		return bmsEcInList;
	}
	
	/**
	 * EC송수신조회 - 통관 마스터 조회
	 * @param maxScanDateTime
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public BmsEcEtcList selectEcEtcList(SearchEc search) throws Exception {
		BmsEcEtcList bmsEcEtcList = new BmsEcEtcList();

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
		List<BmsEcEtc> lst = bmsEcMapper.selectEcEtcList(search);
		bmsEcEtcList.setEcEtcMasterList(lst);

		if( lst != null && lst.size() > 0 ) bmsEcEtcList.setTotal(lst.get(0).getTotal());
		else bmsEcEtcList.setTotal(0);

		return bmsEcEtcList;
	}

	/**
	 * EC송수신조회 - 통관 상세 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsEcEtcList selectEcEtcDetailList(SearchEc search) throws Exception {
		BmsEcEtcList bmsEcEtcList = new BmsEcEtcList();

		if( search.getPage() != 0 && search.getPageSize() != 0 ) search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		if( Util.null2str(search.getChangeField()).equals("Y") ) Util.camelCaseToUnderscore(search);

		List<BmsEcEtcDetail> lst = bmsEcMapper.selectEcEtcDetailList(search);
		bmsEcEtcList.setEcEtcDetailList(lst);

		if( lst != null && lst.size() > 0 ) bmsEcEtcList.setTotal(lst.get(0).getTotal());
		else bmsEcEtcList.setTotal(0);

		return bmsEcEtcList;
	}
	
	/**
	 * EC송수신조회 - 반출승인 조회
	 * @param maxScanDateTime
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public BmsEcOutPlanList selectEcOutPlanList(SearchEc search) throws Exception {
		BmsEcOutPlanList bmsEcOutPlanList = new BmsEcOutPlanList();

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
		List<BmsEcOutPlan> lst = bmsEcMapper.selectEcOutPlanList(search);
		bmsEcOutPlanList.setEcOutPlanList(lst);

		if( lst != null && lst.size() > 0 ) bmsEcOutPlanList.setTotal(lst.get(0).getTotal());
		else bmsEcOutPlanList.setTotal(0);

		return bmsEcOutPlanList;
	}
	
	/**
	 * EC송수신조회 - 반출 마스터 조회
	 * @param maxScanDateTime
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public BmsEcOutList selectEcOutList(SearchEc search) throws Exception {
		BmsEcOutList bmsEcOutList = new BmsEcOutList();

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
		List<BmsEcOut> lst = bmsEcMapper.selectEcOutList(search);
		bmsEcOutList.setEcOutMasterList(lst);

		if( lst != null && lst.size() > 0 ) bmsEcOutList.setTotal(lst.get(0).getTotal());
		else bmsEcOutList.setTotal(0);

		return bmsEcOutList;
	}

	/**
	 * EC송수신조회 - 반출 상세 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsEcOutList selectEcOutDetailList(SearchEc search) throws Exception {
		BmsEcOutList bmsEcOutList = new BmsEcOutList();

		if( search.getPage() != 0 && search.getPageSize() != 0 ) search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		if( Util.null2str(search.getChangeField()).equals("Y") ) Util.camelCaseToUnderscore(search);

		List<BmsEcOutDetail> lst = bmsEcMapper.selectEcOutDetailList(search);
		bmsEcOutList.setEcOutDetailList(lst);

		if( lst != null && lst.size() > 0 ) bmsEcOutList.setTotal(lst.get(0).getTotal());
		else bmsEcOutList.setTotal(0);

		return bmsEcOutList;
	}
	
	
	/**
	 * EC 데이터 재송신
	 * @param params
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public BmsEcSendResult resendEcData(BmsEcSend params, HttpServletRequest request) throws Exception {
		BmsEcSendResult result = new BmsEcSendResult();
		String ecTable = Util.null2str(params.getEcTable());
		List<BmsEcData> ecDataList = params.getEcList();
		int resendCnt = 0;
		if(!ecDataList.isEmpty() && ecDataList.size() > 0) {
			/* INTERFACE_ID-(N) 으로 새로 체번 */
			for(BmsEcData ecData : ecDataList) {
				/* 해당 INTERFACE_ID 조회 */
				String maxInterfaceId = "";
				String orgInterfaceId = makeOrgInterfaceId(Util.null2str(ecData.getInterfaceId()));
				ecData.setOrgInterfaceId(orgInterfaceId);
				if(ecTable.equals("IN")) {
					maxInterfaceId = bmsEcMapper.selectEcInInterfaceId(ecData);
				}else if(ecTable.equals("ETC")) {
					maxInterfaceId = bmsEcMapper.selectEcEtcInterfaceId(ecData);
				}else if(ecTable.equals("OUT_PLAN")) {
					maxInterfaceId = bmsEcMapper.selectEcOutPlanInterfaceId(ecData);
				}else if(ecTable.equals("OUT")) {
					maxInterfaceId = bmsEcMapper.selectEcOutInterfaceId(ecData);
				}
				String newId = makeResendInterfaceId(maxInterfaceId);
				if(Util.null2str(newId).equals("") || Util.null2str(newId).equals(Util.null2str(ecData.getInterfaceId()))) {
					result.setResultCode("99");
					return result;
				}
				ecData.setInterfaceIdNew(newId);
			}
			
			if(ecTable.equals("IN")) {
				resendCnt = bmsEcMapper.updateEcInResend(params);
			}else if(ecTable.equals("ETC")) {
				resendCnt = bmsEcMapper.updateEcEtcResend(params);
			}else if(ecTable.equals("OUT_PLAN")) {
				resendCnt = bmsEcMapper.updateEcOutPlanResend(params);
			}else if(ecTable.equals("OUT")) {
				resendCnt = bmsEcMapper.updateEcOutResend(params);
			}
			
			result.setResultCnt(resendCnt);
			
		}
		
		return result;
	}
	
	/* ORIG INTERFACE_ID 가져오기 */
	private String makeOrgInterfaceId(String interfaceId) {
		int index = interfaceId.lastIndexOf('-');	// '인터페이스ID-N' 재전송했던 이력이있는지 확인
		
		if(index == -1) {	// 재전송한 이력이 없으면 (-가 안붙여져있으면)
			return interfaceId;	// '인터페이스ID-1' 로 리턴
		}
		
		String orgInterfaceId = interfaceId.substring(0, index);	// '인터페이스ID'
		
		try {
			return orgInterfaceId;
		}catch(Exception e) {
			return "";
		}
		
	}
	
	/* 재송신할 INTERFACE_ID 체번 */
	private String makeResendInterfaceId(String interfaceId) {
		int index = interfaceId.lastIndexOf('-');	// '인터페이스ID-N' 재전송했던 이력이있는지 확인
		
		if(index == -1) {	// 재전송한 이력이 없으면 (-가 안붙여져있으면)
			return interfaceId+"-1";	// '인터페이스ID-1' 로 리턴
		}
		
		String orgInterfaceId = interfaceId.substring(0, index);	// '인터페이스ID'
		String numInterfaceId = interfaceId.substring(index+1);		// 'N'
		
		try {
			int num = Integer.parseInt(numInterfaceId);
			return orgInterfaceId + "-" + (num + 1);		// 재전송할 인터페이스 ID 반환
		}catch(Exception e) {
			return "";
		}
		
	}
}