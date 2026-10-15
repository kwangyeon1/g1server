package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.charge.*;
import org.yht.exps.express.model.bms.charge.search.SearchBmsRate;
import org.yht.exps.express.model.bms.charge.search.SearchTcrNo;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.exps.express.model.bms.out.search.SearchBmsOut;

import java.util.List;
import java.util.Map;

@Mapper
public interface BmsChargeMapper {
	// 재고 조회
	List<InventoryIn> selectEstiBmsInventoryInList(SearchBmsOut search);
	// 반출 조회
	List<BmsOut> selectEstiBmsOutList(SearchBmsOut search);
	// 반출승인/승인취하 조회
	List<BmsOutPlan> selectEstiBmsOutPlanList(SearchBmsOut search);

	List<BmsEstimate> selectEstiBmsEstimateList(SearchBmsOut search);
	
	// 메뉴:창고료정산, 프린트시, 반입번호로 창고료 리스트 조회
	List<BmsEstimate> selectEstimateListByInNoList(Map<String, Object> map);
	// 메뉴:창고료정산, 프린트시, 창고료조회시 요율구분 리스트 조회
	List<Map<String, String>> selectApplyClassNameList(Map<String, Object> map);
	
	// MRN + MSN으로 화물관리번호를 검색해서 가져온다
	List<Map<String, String>> selectBmsOutListByMrnMsnOfCargoNo(Map<String, Object> map);
	
	String selectTcrNoFromEntBusOtion(SearchTcrNo search);
	
	int updateTcrNoFromEntBusOtion(SearchTcrNo search);
	
	// 재고 조회 엑셀
	List<InventoryIn> selectEstiBmsInventoryInListExcel(SearchBmsOut search);
	// 반출 조회 엑셀
	List<BmsOut> selectEstiBmsOutListExcel(SearchBmsOut search);
	// 반출승인/승인취하 조회 엑셀
	List<BmsOutPlan> selectEstiBmsOutPlanListExcel(SearchBmsOut search);
  // 창고료 엑셀
	List<BmsEstimate> selectEstiBmsEstimateListExcel(SearchBmsOut search);

	BmsEstimate selectBmsEstimateDetail(SearchBmsOut search);

	// 창고료 일부필드 업데이트
	int updateBmsEstimateField(BmsEstimate estimate);

	int insertBmsEstimate(BmsEstimate estimate);
	int updateBmsEstimate(BmsEstimate estimate);
	void deleteBmsEstimate(BmsEstimate estimate);

	/***
	 * 세금 계산서 처리후 계산 여부를 Y로 해서 처리여부 남김
	 * @param estimate
	 * @return
	 */
	int updateBmsEstimateInvoiceYnYes(BmsEstimate estimate);
	
	/***
	 * 세금 계산서 계산시, COM_OD_JOB_NO를 업데이트
	 * @param estimate
	 * @return
	 */
	int updateBmsEstimateComOdJobNo(BmsEstimate estimate);
	// 세금 계산서 계산시, CUSTOMER_CODE, CUSTOMER_NAME을 업데이트
	int updateBmsEstimateCustomer(BmsEstimate estimate);


	BmsStorageRate selectBmsStorageRateDetail(SearchBmsRate search);
	BmsWorkRate selectBmsWorkRateDetail(SearchBmsRate search);


	/***
	 * 창고료 조회
	 * @param search
	 * @return
	 */
	List<BmsStorageRate> selectBmsStorageRateList(SearchBmsRate search);

	List<BmsStorageRate> selectBmsStorageRateDetailList(SearchBmsRate search);

	int insertBmsStorageRate(BmsStorageRate storageRate);
	int updateBmsOutEstimateYn_Y(BmsOut bmsOut);

	int updateBmsOutEstimateYn_N(BmsOut bmsOut);
	int updateBmsStorageRate(BmsStorageRate storageRate);
	void deleteBmsStorageRate(BmsStorageRate storageRate);
	/** 작업료  조회 */
	List<BmsWorkRate> selectBmsWorkRateList(SearchBmsRate search);

	List<BmsWorkRate> selectBmsWorkRateDetailList(SearchBmsRate search);

	int insertBmsWorkRate(BmsWorkRate bmsWorkRate);
	int updateBmsWorkRate(BmsWorkRate bmsWorkRate);
	void deleteBmsWorkRate(BmsWorkRate bmsWorkRate);

 	List<BmsHoliday> selectBmsHolidayList(SearchBmsRate search);

	int insertBmsHoliday(BmsHoliday bmsHoliday);
	int updateBmsHoliday(BmsHoliday bmsHoliday);
	void deleteBmsHoliday(BmsHoliday bmsHoliday);



	/**
	 * 요율구분에 따른 발행번호및 보관일수(기본값) 리턴
	 * @param search
	 * @return
	 */
	BmsRateBase selectBmsApplyNoAndWorkDays(SearchBmsRate search);

	BmsStorageRate selectBmsStorageRateBase(SearchBmsRate search);

	BmsWorkRate selectBmsWorkRateBase(SearchBmsRate search);

	Integer selectEstimateCargoAndOutApprovalCount(BmsOut bmsOut);


	/**
	 * 계산서 정보를 위한 화주 정보 리턴
	 * @param bmsOut
	 * @return
	 */
	BmsCustomerInfo selectBmsCustomerInfo(BmsOut bmsOut);

	/***
	 * 정산번호 리턴
	 * @param bmsOut
	 * @return
	 */
	String selectBmsInvoiceNo(BmsOut bmsOut);

	/***
	 * 창고료 세금 계산서 발행
	 * TB_SAS_INVOICE
	 */
	int insertTbSasInvoiceTax(Map<String, Object> map);


	/***
	 * 창고료 세금 계산서 발행
	 * TB_SAS_INVOICE_ITEM
	 */
	int insertTbSasInvoiceItemTax(Map<String, Object> map);

	/***
	 * 콤보 박스 작업용
	 * @return
	 */
	List<BmsWorkRate> selectChargeWorkRateCombo();
	List<BmsStorageRate> selectChargeStorageRateCombo();

	int selectChargeStorageDupCount(BmsStorageRate storageRate);
	int selectChargeWorkDupCount(BmsWorkRate workRate);
	
	BmsEstimate selectOneEstimate(Map<String, Object> map);
	
	Map<String, Object> selectCustomerOfInOut(Map<String, Object> map);
	
	int checkCustomerInfo(BmsEstimate estimate);
	
	int updateCustomerInfoForTax(Map<String, Object> map);
	
	/**
	 * 창고료정산 > 화주정보 팝업
	 * @param bmsCustomerChargeInfo
	 * @return
	 */
	BmsCustomerChargeInfo selectBmsCustomerChargeInfo(BmsCustomerChargeInfo bmsCustomerChargeInfo);
	
	String selectBmsBusRegistrationCode(BmsCustomerChargeInfo bmsCustomerChargeInfo);
	
	String selectBmsCustomerRegistrationCode(BmsCustomerChargeInfo bmsCustomerChargeInfo);
	
	int insertBmsCustomerChargeInfo(BmsCustomerChargeInfo bmsCustomerChargeInfo);
	
	int updateBmsCustomerChargeInfo(BmsCustomerChargeInfo bmsCustomerChargeInfo);
}
