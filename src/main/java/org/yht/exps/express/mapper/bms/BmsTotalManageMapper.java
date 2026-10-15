package org.yht.exps.express.mapper.bms;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsTotalManage.BmsTotalManage;
import org.yht.exps.express.model.bms.bmsTotalManage.BmsTotalManageDetail;
import org.yht.exps.express.model.bms.bmsTotalManage.search.SearchTotalManage;

@Mapper
public interface BmsTotalManageMapper {
	List<BmsTotalManage> selectTotalManageList(SearchTotalManage search);
	List<BmsTotalManageDetail> selectTotalManageDetailList(SearchTotalManage search);
	Map<String, Object> selectHouseInfoBox(Integer inPlanNo);

	List<Integer> selectEmDeliveryCount(Map<String, Object> search);
	List<Map<String, Object>> selectEmDeliveryListExcel(Map<String, Object> search);
	List<Map<String, Object>> selectCjDeliveryListExcel(Map<String, Object> search);
	List<Map<String, Object>> selectClDeliveryListExcel(Map<String, Object> search);
}
