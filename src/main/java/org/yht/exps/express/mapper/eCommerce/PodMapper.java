package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.pod.PodInfo;
import org.yht.exps.express.model.eCommerce.pod.search.SearchPodList;

@Mapper
public interface PodMapper {
	
	List<PodInfo> selectPodList(SearchPodList search);
	List<Map<String, Object>> selectOrderMultiList(Map<String, Object> searchMap);
	List<PodInfo> selectPodListCommon(SearchPodList search);
}
