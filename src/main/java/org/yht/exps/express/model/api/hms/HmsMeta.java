package org.yht.exps.express.model.api.hms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HmsMeta", description = "HMS Meta")
@Alias("hmsMeta")
public class HmsMeta{
	@ApiModelProperty(value="API version")
	private String version;
	@ApiModelProperty(value="Where  did the event happen")
	private String eventSource;
	@ApiModelProperty(value="Unique ID for each event/message")
	private String eventId;
	@ApiModelProperty(value="IOS format string of datetime")
	private String eventDateTime;
	@ApiModelProperty(value="Which country did this event process")
	private String eventProcessedCountry;
	@ApiModelProperty(value="Type of event")
	private String eventType;
}
