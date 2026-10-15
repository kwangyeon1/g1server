package org.yht.exps.express.model.api;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Alias("returnTrackingDataJson")
public class ReturnTrackingDataJson {
	@JsonProperty("OrdId")
	private String ordId;
	@JsonProperty("HBLNo")
	private String hblNo;
	@JsonProperty("TrackingCode")
	private String trackingCode;
	@JsonProperty("TrackingDateTime")
	private String trackingDateTime;
	@JsonProperty("TrackingCodeName")
	private String trackingCodeName;
	@JsonProperty("TrackingRemark")
	private String trackingRemark;
}
