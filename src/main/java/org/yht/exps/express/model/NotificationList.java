package org.yht.exps.express.model;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "NotificationList", description = "알람(Notification) 리스트")
@Alias("notificationList")
public class NotificationList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<Notification> notificationList;
}
