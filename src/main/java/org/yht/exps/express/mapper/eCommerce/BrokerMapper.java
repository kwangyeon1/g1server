package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.broker.BrokerHouse;
import org.yht.exps.express.model.eCommerce.broker.BrokerHouseSub;
import org.yht.exps.express.model.eCommerce.broker.BrokerMaster;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterSub;
import org.yht.exps.express.model.eCommerce.broker.search.SearchBrokerBatchUpdate;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;

@Mapper
public interface BrokerMapper {
	
	List<BrokerMaster> selectBrokerMasterList(SearchOrderList search);
	List<BrokerMasterSub> selectBrokerMasterSubList(SearchOrderList search);
	List<BrokerMaster> selectCustomsOfficerList(SearchOrderList search);
	List<BrokerHouse> selectBrokerHouseList(SearchOrderList search);
	List<BrokerHouseSub> selectBrokerHouseSubList(SearchOrderList search);
	List<Order> selectBrokerBatchBySrchData(SearchOrderList schOrder);
	List<BrokerHouse> selectCustomsShipmentRequestList(SearchOrderList search);
	List<BrokerMaster> selectCustomsOfficerList2(SearchOrderList search);
	
	int updateBrokerBatchBySrchData(SearchOrderList search);
	int updateBrokerBatchBySrchData2(SearchOrderList search);
	int updateBrokerBatch(SearchBrokerBatchUpdate search);
	int updateBrokerBatch2(SearchBrokerBatchUpdate search);
	int updateTariffRemarks(Map<String, Object> map);
	
	void updateBrokerClearanceStatus(List<BrokerHouse> brokerList);
	
}
