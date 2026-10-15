package org.yht.exps.express.model.api;

import java.util.List;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "TrackingInterfaceJson", description = "Tracking Order Info")
@Alias("TrackingInterfaceJson")
public class TrackingInterfaceJson {
	@JsonAlias({"AccessIdentifier", "accessIdentifier"})
	private AccessIdentifierJsonVE accessIdentifier;
	@JsonAlias({"OrderTracking", "orderTracking"})
	private List<OrderTrackingJson> orderTracking;
}
