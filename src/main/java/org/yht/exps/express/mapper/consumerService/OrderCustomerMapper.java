package org.yht.exps.express.mapper.consumerService;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.consumerService.order.OrderCustomerItem;
import org.yht.exps.express.model.consumerService.order.OrderCustomerMulti;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomer;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;

@Mapper
public interface OrderCustomerMapper {
	
	List<Order> selectOrderList(SearchOrderCustomerList search);
	
	Map<String,Object> selectUserInfo(Map<String,Object> search);
	
	String dupOrderNo(Order order);
	void deleteOrder(Order orderCustomer);
	void deleteOrderAdditional(Order orderCustomer);
	void deleteOrderContainer(Order orderCustomer);
	void deleteOrderItem(Order orderCustomer);
	void deleteOrderSpecialItem(Order orderCustomer);
	void deleteOrderCost(Order orderCustomer);
	void deleteOrderMulti(Order orderCustomer);
	
	Map<String,Object> selectCusLvl(Map<String,Object> search);
	
	void insertOrder(Order orderCustomer);
	void insertOrderAdditonal(Order orderCustomer);
	void insertItem(Order orderCustomer);
	
	void deleteSub(Order orderCustomer);
	void updateSub(Order orderCustomer);
	
	void updateOrder(Order orderCustomer);
	void updateOrderAdditonal(Order orderCustomer);
	void updateSumOrder(Order orderCustomer);
	
	Order selectOrder(SearchOrderCustomer search);	
	List<OrderCustomerItem> selectOrderItem(SearchOrderCustomer search);	
	List<OrderCustomerItem> selectOrderSub(SearchOrderCustomer search);
	
	List<String> selectPoInfo(SearchOrderCustomer search);
	
	List<Map<String, Object>> selectOrderCustomerChkList(Map<String, Object> map);
	String selectBatchTargetCount(SearchOrderCustomerList search);
	List<OrderCustomerMulti> selectOrderMulti(SearchOrderCustomer search);
	void insertMulti(Order order);
}
