package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.pod.TrackingPod;

@Mapper
public interface TrackingOrderMapper {
	List<TrackingPod> selectTrackingList(Map<String, Object> trackingSrchMap);
	void updateTracking(Map<String, Object> srchMap);
	Map<String, Object> selectDeliveryApiInfo(Map<String, Object> map);
	
}