package org.yht.exps.express.mapper.consumerService;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.com.fileshare.FileShare;
import org.yht.exps.express.model.com.fileshare.FileShareFile;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList;
import org.yht.exps.express.model.eCommerce.bondedTrans.BondedTrans;
import org.yht.exps.express.model.eCommerce.bondedTrans.BondedTransDelete;
import org.yht.exps.express.model.eCommerce.bondedTrans.search.SearchBondedTrans;
import org.yht.exps.express.model.eCommerce.bondedTrans.search.SearchBondedTransList;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterSub;
import org.yht.exps.express.model.eCommerce.changeDelivery.ChangeDelivery;
import org.yht.exps.express.model.eCommerce.changeDelivery.search.SearchChangeDelivery;
import org.yht.exps.express.model.eCommerce.changeDelivery.search.SearchChangeDeliveryList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgOrder;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWg;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWgOrderList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderCost;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.eCommerce.quickRequest.QuickRequest;
import org.yht.exps.express.model.eCommerce.quickRequest.QuickRequestDeleteList;
import org.yht.exps.express.model.eCommerce.quickRequest.search.SearchQuickRequest;
import org.yht.exps.express.model.eCommerce.quickRequest.search.SearchQuickRequestList;
import org.yht.exps.express.model.eCommerce.returnMng.ReturnMng;
import org.yht.exps.express.model.eCommerce.returnMng.ReturnMngDeleteList;
import org.yht.exps.express.model.eCommerce.returnMng.search.SearchReturnMngList;
import org.yht.exps.express.model.eCommerce.unusualMng.UnusualMng;
import org.yht.exps.express.model.eCommerce.unusualMng.search.SearchUnusualMngList;

@Mapper
public interface OrderCommonCsMapper {
	//검사관리
	List<Order> selectOrderList(SearchOrderCustomerList search);
	Order selectInspectionDetail(SearchOrderCustomerList search);
	void saveInspectionDetailUpdate(Order order);
	
	//무적,미착관리
	void insertOrderPod(Map<String, Object> map);
	void deleteOrderPod(Map<String, Object> map);
	Map<String, Object> selectOdJobNoByHblNo(Map<String, Object> selectOdJobNo);
	void insertMgnMfn(Map<String, Object> map);
	Map<String, Object>  selectMblInfo(Map<String, Object> map);
	void createHbl(Map<String, Object> map);
	//수입신고
	List<BrokerMasterSub> selectBrokerMasterSubList(SearchOrderList search);
	BrokerMasterSub selectBrokerDetail(SearchOrderList search);
	void saveBrokerUpdate(BrokerMasterSub broker);
	void insertOdCost(OrderCost odCost);
	
	//이영(중량)화물관리
	List<MultiWgOrder> selectSubMultiWgList(SearchMultiWgOrderList search);
	MultiWgOrder selectMultiOrderDetail(SearchMultiWg search);
	void insertMulti(MultiWgOrder order);	
	int deleteMultiWgOrder(MultiWgList order);	
	
	//보세운송관리
	List<BondedTrans> selectBondedTransList(SearchBondedTransList search);
	BondedTrans selectBondedTransDetail(SearchBondedTrans search);
	
	void insertBondedTrans(Map<String, Object> map);
	void insertOdCsHistory(Map<String, Object> map);
	int selectMaxCsSeqNo(Map<String, Object> map);
	void deleteBondedTrans(BondedTransDelete bondedTransDelete);
	
	//송장호출(주소변경)
	List<ChangeDelivery> selectChangeDeliveryList(SearchChangeDeliveryList search);
	ChangeDelivery selectChangeDeliveryDetail(SearchChangeDelivery search);
	void insertChangeDelivery(Map<String, Object> map);
	Map<String, Object> selectMultiOdJobNoByBlNo(Map<String, Object> selectOdJobNo);
	int selectMaxMultiSeqNo(Map<String, Object> map);
	void insertMultiDelivery(Map<String, Object> map);
	void updateOrigMultiOrder(Map<String, Object> map);
	void confirmDelivery(ChangeDelivery search);
	
	//퀵요청
	List<QuickRequest> selectQuickRequestList(SearchQuickRequestList search);
	QuickRequest selectQuickRequestDetail(SearchQuickRequest search);
	void insertQuickRequest(Map<String, Object> map);
	void deleteQuickRequest(QuickRequestDeleteList quickRequestDeleteList);
//	Map<String, Object> selectMultiOdJobNoByBlNo(Map<String, Object> selectOdJobNo);
	
	//반품관리
	List<ReturnMng> selectReturnMngList(SearchReturnMngList search);
	void saveReturnMngFunction(SearchReturnMngList returnMngList);
	void saveReturnMng(ReturnMng returnMng);
	int selectReturnBl(ReturnMng returnMng);	
	void deleteReturnMng(ReturnMngDeleteList returnMngDeleteList);
	
	//이례화물관리
	List<UnusualMng> selectUnusualMngList(SearchUnusualMngList search);
	
	void insertFileShare(FileShare fileShare);
	void insertFileShareFile(FileShare fileShare);
	Map<String, Object> selectFileShare(BrokerMasterSub broker);
	List<FileShareFile> selectFileShareList(BrokerMasterSub brokerMasterSub);
	void deleteFileShareFile(FileShareFile fileTemp);
	void deleteFileShare(BrokerMasterSub broker);
	void insertResendRequest(Map<String, Object> excelOdChg);
	void insertMultiData(Map<String, Object> excelOdChg);
	void updateMultiDelivery(Map<String, Object> excelOdChg);
	void updateDeliveryStatus(Map<String, Object> excelOdChg);
}
