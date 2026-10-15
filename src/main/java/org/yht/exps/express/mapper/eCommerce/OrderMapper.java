package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.order.HblNo;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderAbnormal;
import org.yht.exps.express.model.eCommerce.order.OrderCSum;
import org.yht.exps.express.model.eCommerce.order.OrderCombinedTax;
import org.yht.exps.express.model.eCommerce.order.OrderCommon;
import org.yht.exps.express.model.eCommerce.order.OrderContainer;
import org.yht.exps.express.model.eCommerce.order.OrderCost;
import org.yht.exps.express.model.eCommerce.order.OrderCs;
import org.yht.exps.express.model.eCommerce.order.OrderCus;
import org.yht.exps.express.model.eCommerce.order.OrderDashboard;
import org.yht.exps.express.model.eCommerce.order.OrderDay;
import org.yht.exps.express.model.eCommerce.order.OrderDayHouse;
import org.yht.exps.express.model.eCommerce.order.OrderDelete;
import org.yht.exps.express.model.eCommerce.order.OrderDet;
import org.yht.exps.express.model.eCommerce.order.OrderExclusion;
import org.yht.exps.express.model.eCommerce.order.OrderHold;
import org.yht.exps.express.model.eCommerce.order.OrderHouseDup;
import org.yht.exps.express.model.eCommerce.order.OrderItem;
import org.yht.exps.express.model.eCommerce.order.OrderLicense;
import org.yht.exps.express.model.eCommerce.order.OrderMasterDup;
import org.yht.exps.express.model.eCommerce.order.OrderMulti;
import org.yht.exps.express.model.eCommerce.order.OrderPacking;
import org.yht.exps.express.model.eCommerce.order.OrderPod;
import org.yht.exps.express.model.eCommerce.order.OrderSpecialItem;
import org.yht.exps.express.model.eCommerce.order.OrderSumHouse;
import org.yht.exps.express.model.eCommerce.order.OrderSumMaster;
import org.yht.exps.express.model.eCommerce.order.TrademarkInfringement;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrder;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderBatchUpdate;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderBatchUpdateCntr;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderCostList;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.eCommerce.order.search.SearchPrintOut;
import org.yht.exps.express.model.systemMng.DeliveryInterface;

@Mapper
public interface OrderMapper {
	/** 오더관리(도입사) */
	List<OrderDet> selectOrderListDet(SearchOrderList search);
	List<OrderDay> selectOrderListDay(SearchOrderList search);
	List<OrderDayHouse> selectOrderListDayHouse(SearchOrderList search);
	List<OrderSumMaster> selectOrderListSumMaster(SearchOrderList search);
	List<OrderSumHouse> selectOrderListSumHouse(SearchOrderList search);
	List<OrderCus> selectOrderListCus(SearchOrderList search);
	List<OrderCost> selectOrderListCusCost(SearchOrderCostList search);
	List<HblNo> selectHblNoList(SearchOrderList search);
	Map<String, Object> selectOrderSaveChk(Map<String, Object> search);
	List<String> checkPackingRegisterIdAPI(Map<String, Object> map);
	
	/** 오더관리(도입사) - 합산과세 */
	List<Map<String, Object>> selectExchangeRateNullList(Map<String, Object> search);
	Map<String, Object> selectExchangeRate(Map<String, Object> search);
	void updateExchangeRate(Map<String, Object> search);
	
	List<String> selectCombinedTaxParams(Map<String, Object> search);
	List<OrderCombinedTax> selectCombinedTaxList(Map<String, Object> search);
	void updateExclusion(OrderExclusion exclusion);
	/** 오더관리(도입사) - 일괄변경 */
	void updateOrderBatch(SearchOrderBatchUpdate search);
	void updateOrderAdditionalBatch(SearchOrderBatchUpdate search);
	void updateOrderBatchBySrchData(Map<String, Object> search);
	void updateOrderAdditionalBatchBySrchData(Map<String, Object> search);
	void deleteContainerBatch(SearchOrderBatchUpdate search);
	void deleteContainerBatchBySrchData(Map<String, Object> search);
	void updateReturnNo(Map<String, Object> map);
	void updateReturnNoMulti(Map<String, Object> map);
	List<String> selectBatchUpdateTarget(SearchOrderList search);
	List<String> selectBatchUpdateTarget2(SearchOrderList search);
	List<String> selectBatchUpdateLiceTarget1(SearchOrderList search);
	List<String> selectBatchUpdateLiceTarget2(SearchOrderList search);
	List<Map<String, Object>> selectBatchUpdateHoldTarget(Map<String, Object> map);
	List<String> selectBatchUpdatePiecesList(SearchOrderBatchUpdate search);
	void insertContainerBatch(SearchOrderBatchUpdate search);
	void insertContainerBatchBySrchData(SearchOrderBatchUpdateCntr search);
	void deletePoInfoBatch(SearchOrderBatchUpdate search);
	void deletePoInfoBatchBySrchData(Map<String, Object> search);
	void insertPoInfoBatch(SearchOrderBatchUpdate search);
	void deleteLiceNoInfoBatch(SearchOrderBatchUpdate search);
	void deleteLiceNoInfoBatchBySrchData(Map<String, Object> search);
	void deleteHoldBatch(SearchOrderBatchUpdate search);
	void deleteHoldBatchBySrchData(Map<String, Object> search);
	void updateHoldBatch(SearchOrderBatchUpdate search);
	void updateHoldBatchBySrchData(Map<String, Object> search);
	void insertLiceInfoBatch(SearchOrderBatchUpdate search);
	void insertHoldInfo(Map<String, Object> map);
	void insertHoldInfoBatch(SearchOrderBatchUpdate search);
	String selectOdJobNoByBlNo(Map<String, Object> map);
	/** 오더관리(도입사) - 수화인우편번호변환 */
	void updateConsigneeZipCode(Map<String, Object> search);
	void updateZipCodeErrorYn(String odJobNo);
	/** 오더관리(도입사) - 엑셀업로드수정 */
	Map<String, Object> selectExchangeInfo(Map<String, Object> excelUploadOdChg);
	
	/** 오더조회팝업 */
	List<OrderCommon> selectOrderListCommon(SearchOrderList search);
	
	/** 컨테이너조회팝업 */
	List<OrderCommon> selectContainerListCommon(SearchOrderList search);
	
	/** 하우스오더 */
	Map<String, Object> selectOrderByBlNo(Map<String, Object> map);
	Map<String, Object> selectOrderChk(Map<String, Object> map);
	Map<String, Object> selectMasterOrderEmsTs(Map<String, Object> map);
	List<String> selectHouseOrderEmsTs(Map<String, Object> map);
	void insertOrderEmsTsExcel(Map<String, Object> map);
	List<Map<String, Object>> selectOrderChkList(Map<String, Object> map);
	Order selectOrder(SearchOrder search);
	Map<String, Object> selectSimpleDocNoData(SearchOrder search);
	List<OrderItem> selectItem(SearchOrder search);
	List<OrderSpecialItem> selectSpecialItem(SearchOrder search);
	List<OrderAbnormal> selectAbnormal(SearchOrder search);
	List<OrderContainer> selectContainer(SearchOrder search);
	List<OrderMulti> selectMulti(SearchOrder search);
	List<OrderPacking> selectPacking(SearchOrder search);
	List<String> selectPoInfo(SearchOrder search);
	List<OrderCost> selectCost(SearchOrder search);
	List<OrderPod> selectPod(SearchOrder search);
	List<OrderLicense> selectLicense(SearchOrder search);
	List<OrderCs> selectCs(Map<String, Object> search);
	Map<String, Object> selectSimpleDocNoCnt(Map<String, Object> map);
	void updateSimpleDocNoNull(Map<String, Object> map);
	Map<String, Object> selectMasterInfo(Map<String, Object> map);
	
	void deleterOrder(OrderDelete orderDelete);
	void insertOrder(Order order);
	void insertAdditional(Order order);
	void updateOrder(Order order);
	void updateAdditional(Order order);
//	void updatePackingErrorYn(Map<String, Object> map);
	void deleterAdditional(Map<String, Object> map);
	void deleteItem(Map<String, Object> map);
	void deleteSpecialItem(Map<String, Object> map);
	void deleteAbnormal(Map<String, Object> map);
	void deleteContainer(Map<String, Object> map);
	void deleteMulti(Map<String, Object> map);
	void deletePacking(Map<String, Object> map);
	void deletePoInfo(Map<String, Object> map);
	void deleteCost(Map<String, Object> map);
	void deleteOdChgInv(Map<String, Object> map);
	void deletePod(Map<String, Object> map);
	void updatePodBlNo(Map<String, Object> map);
	int updatePackingOdJobNo(Order order);
	int selectPackingCount(Map<String, Object> map);
	void deleteLicense(Map<String, Object> map);
	void insertItem(Map<String, Object> map);
	void insertItemBatch(OrderItem orderItem);
	void insertSpecialItem(Map<String, Object> map);
	void insertAbnormal(Map<String, Object> map);
	void insertContainer(Map<String, Object> map);
	void insertMulti(Map<String, Object> map);
	void insertPacking(Map<String, Object> map);
	void insertPoInfo(Map<String, Object> map);
	void insertCost(Map<String, Object> map);
	void updateCost(Map<String, Object> map);
	void insertOdChgInv(Map<String, Object> map);
	void insertLicense(Map<String, Object> map);
	void insertLicenseInfo(Map<String, Object> map);
	void updateConsigneeEng(Order order);
	void insertCs(OrderCs cs);
	void updateCs(OrderCs cs);
	void deleteCs(Map<String, Object> map);
	void updateCostInfo(Map<String, Object> map);
	
	List<Order> selectOrderListExcelError(Order order);
	Order selectOrderExcelError(SearchOrder search);
	void insertOrderExcelError(Order order);
	void updateOrderExcelError(Order order);
	void insertItemExcelError(List<OrderItem> list);
	void updateItemExcelError(Order order);
	void insertContainerExcelError(List<OrderContainer> list);
	void insertLicenseExcelError(OrderLicense orderLicense);
	List<Map<String, Object>> selectCntrNoDupChk(Map<String, Object> map);
	List<Map<String, Object>> selectLicenseNoDupChk(Map<String, Object> map);
	
	int selectInvoiceConfirmCheck(Map<String, Object> map);
	
	void updateOrderLastPod(Map<String, Object> map);
	void insertOrderPod(Map<String, Object> map);
	void updateOrderPod(Map<String, Object> map);
	void deleteOrderPod(Map<String, Object> map);
	Map<String, Object> selectOrderPodInfo(Map<String, Object> map);
	
	List<OrderItem> selectOrderItem(SearchOrder search);
	List<Map<String, Object>> selectDeliveryPrintout(SearchPrintOut search);
	Map<String, Object> selectEmsZipCode(Map<String, Object> map);
	List<Map<String, Object>> selectMultiStockList(OrderExclusion search);
	void insertZipCodeInfo(Map<String, Object> map);
	String selectBatchTargetCountChk(Map<String, Object> map);
	String selectBatchTargetCount(SearchOrderList search);
	
	void updatePrintOutDate(SearchPrintOut search);
	void updatePrintOutError(String odJobNo);
	void deletePrintoutData(SearchPrintOut search);
	List<OrderPod> selectOrderPodStatusList(SearchOrderList search);
	List<Order> selectOrderListForLog(Map batchUpdateMap);
	List<DeliveryInterface> selectDeliveryTrackingYn(Map<String, String> map);
	List<Order> checkDeliveryPrintYn(SearchPrintOut search);
	void updateExcelUploadCarryin(Map<String, Object> map);
	List<Order> selectCarryinList(Map<String, Object> map);
	
	Order getGoodsLocInfo(Map<String, String> map);
	
	Order selectOrderGwtPieces(SearchOrderList search);
	
	void deleteOrderPodDelivery(Map<String, Object> map);
	void updateLastPodMax(Map<String, Object> map);
	
	List<OrderHold> selectHold(Map<String, String> map);
	List<OrderHold> selectHoldWhereInOdJobNos(Map<String, Object> map);
	void deleteHold(Map<String, Object> map);
	void insertHold(Map<String, Object> map);
	void insertHoldHistory(Map<String, Object> map);

	/** UPS */
	List<OrderCSum> selectOrderListCSum(SearchOrderList search);
	int selectClsFilterCodeDupCheck(Map<String, Object> map);
	void insertClsFilterExcelUpload(Map<String, Object> map);
	List<OrderMasterDup> selectOrderMasterDupList(SearchOrderList search);
	List<OrderHouseDup> selectOrderHouseDupList(SearchOrderList search);
	/** UPS */
	
	void insertOrderPodInfo(Map<String, Object> consolPodMap);
	void deleteEmsInfo(Map<String, Object> tmpMap);
	void updateOrderEmsInfo(Order order);
	void insertOrderEmsInfo(Order order);
	Object selectOdJobNoByHblNo2(Map<String, Object> excelOdChg);
	Map<String, Object> selectOdJobNoByHblNo(Map<String, Object> selectOdJobNo);
	void insertLicenseExcel(Map<String, Object> excelOdChg);
	
	TrademarkInfringement getTrademark(SearchOrderList search);

	void updateOdMultiDate(Map<String, Object> map);

	void updateOdMultiStatus(Map<String, Object> map);

	void updateOrderKdInfo(Map<String, Object> kdMap);
	Map<String, Object> selectDeliveryApiInfoAll(Map<String, Object> map);
	Map<String, Object> selectDeliveryApiInfoAllMapping(Map<String, Object> map);
	Map<String, Object> selectDeliveryApiInfo(Map<String, Object> map);
	void updateOdDate(Map<String, Object> map);
	List<DeliveryInterface> selectDeliveryTrackingYnVe(Map<String, String> map);

	void updateChgDeliveryNo(Map<String, Object> map);

	void updateKdHbl(Map<String, Object> map);
	void updateKdHblMulti(Map<String, Object> map);
	void deleteKdDeliveryInfoEtc(Map<String, Object> map);
	void insertKdDeliveryInfoEtc(Map<String, Object> map);
	String selectChgDeliveryCode(Map<String, Object> map);

	Map<String, Object> selectPrint(Map<String, Object> map);

	Map<String, Object> selectTempSeq(Map<String, Object> map);
	List<OrderDet> selectMultiOrderList(SearchOrderList search);

	
	void updateOrderSelectorBatchBySrchData(Map<String, Object> batchUpdateMap);
	void updateOrderSelectorBatch(SearchOrderBatchUpdate search);
	void orderResetUpdate(Map<String, Object> map);
	
	List<OrderDet> selectOrderListCustomsExp(SearchOrderList search);
	
	/** 멀티조회 팝업 */
	List<OrderCommon> selectMultiListCommon(SearchOrderList search);
	
	List<OrderDashboard> selectOrderDashboardList(SearchOrderList search);
	List<OrderDashboard> selectOrderDashboardDiff(SearchOrderList search);
	List<Map<String, Object>> selectCSQanda(SearchOrderList search);
	Map<String, Object> selectResendCount(SearchOrderList search);

	List<Map<String, Object>> selectBatchUpdateUnusualTarget(Map<String, Object> map);
	void updateUnusualInfo(Map<String, Object> map);
	void updateUnusualInfoBatch(SearchOrderBatchUpdate search);
	void updateUnusualBatchBySrchData(Map<String, Object> map);
	void updateUnusualBatch(SearchOrderBatchUpdate search);
	List<Map<String, Object>> selectTrackingPodOrderList(Map batchUpdateMap);
	void updateOrderTrackingPod(Map odMap);
	
	String chkRequestId(Map<String, Object> map);

}
