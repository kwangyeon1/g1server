package org.yht.exps.express.mapper.invoice;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.invoice.search.Easypay;

@Mapper
public interface PayMapper {
	Map<String, Object> selectInvoicePaymentData(Map<String, Object> map);
	Easypay selectEasypayResult(Map<String, Object> map);
	List<Easypay> selectEasypayInvoice(Map<String, Object> map);
	List<Map<String, Object>> selectInvoiceList(Map<String, Object> map);
	String selectEntCode(Map<String, Object> map);
	void insertPayResult(Map<String, Object> map);
	void updatePayResult(Map<String, Object> map);
}
