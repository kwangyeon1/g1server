package org.yht.exps.express.model.bms.bmsCargo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "BmsMasterDest", description = "특송창고 Master 도착")
@Alias("bmsMasterDest")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsMasterDest extends ResultList {
	@ApiModelProperty(value="seq")
	private Integer seq;
	@ApiModelProperty(value="MBL번호")
	@Builder.Default
	private String mblNo = "";
	@ApiModelProperty(value="수량")
	@Builder.Default
	private Integer pieces = 0;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="입항일시")
	private String arDateTime;
	@ApiModelProperty(value="도착일자")
	private String destDate;
	@ApiModelProperty(value="도착시간")
	private String destTime;
	@ApiModelProperty(value="도착일시")
	private String destDateTime;
	@ApiModelProperty(value="생성자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
}
