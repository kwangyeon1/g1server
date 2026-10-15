package org.yht.exps.express.mapper.invoice;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.invoice.BrokerCharge;
import org.yht.exps.express.model.invoice.CourierCharge;
import org.yht.exps.express.model.invoice.Invoice;
import org.yht.exps.express.model.invoice.PartnerDetailCharge;
import org.yht.exps.express.model.invoice.PartnerSummaryCharge;
import org.yht.exps.express.model.invoice.WarehouseCharge;
import org.yht.exps.express.model.invoice.WorkFeeCharge;
import org.yht.exps.express.model.invoice.WorkFeeRate;
import org.yht.exps.express.model.invoice.search.SearchBatchDeleteCost;
import org.yht.exps.express.model.invoice.search.SearchInvoiceList;

@Mapper
public interface InvoiceMapper {
	List<WorkFeeCharge> selectWorkFeeChargeList(SearchInvoiceList search);
	List<WarehouseCharge> selectWarehouseChargeList(SearchInvoiceList search);
	List<CourierCharge> selectCourierChargeList(SearchInvoiceList search);
	List<BrokerCharge> selectBrokerChargeList(SearchInvoiceList search);
	List<PartnerDetailCharge> selectPartnerDetailChargeList(SearchInvoiceList search);
	List<PartnerSummaryCharge> selectPartnerSummaryChargeList(SearchInvoiceList search);
	
	int selectInvoiceNoCheck(Map<String, Object> map);
	List<Map<String, Object>> selectOdCostSum(SearchInvoiceList search);
	List<Map<String, Object>> selectOdCostSumSelectGrid(Invoice invoice);
	void insertComOd(Map<String, Object> map);
	void insertComOdForStorage(Map<String, Object> map);
	void insertComOdChg(List<Map<String, Object>> odCostList);
	void insertComOdChgForStorage(Map<String, Object> map);
	void updateEcmOdChg(Invoice invoice);
	void insertInvoice(Map<String, Object> map);
	void insertInvoiceItem(Map<String, Object> map);
	void updateOdCost(SearchInvoiceList search);
	void updateOdCostSelectGrid(Invoice invoice);
	void insertObl(Map<String, Object> map);
	void insertOblForStorage(Map<String, Object> map);
	int updateWorkFeeClose(SearchInvoiceList search);
	int updateWorkFeeCloseSelectGrid(Invoice invoice);
	void updateOdChgInvoiceNoNull(Map<String, Object> map);
	int deleteOrderCost(Invoice invoice);
	
	List<WorkFeeRate> selectCustomerTreeList(WorkFeeRate rate);
	List<Map<String, Object>> selectWorkFeeRate(WorkFeeRate rate);
	List<Map<String, Object>> selectCostBatchDelete(SearchBatchDeleteCost search);
	void deleteCostBatch(Map<String, Object> map);
	void insertWorkFee(Map<String, Object> map);
}
