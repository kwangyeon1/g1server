package org.yht.exps.express.model.bms.bmsXraySend.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.bmsXraySend.XraySend;
import org.yht.exps.express.model.search.SearchElastic;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "searchXraySendLog", description = "xray전송 조회조건")
@Alias("searchXraySendLog")
public class SearchXraySendLog extends SearchElastic {
    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="대표사업장코드", example="YHTXX-KR")
    private String entBusinessCode;
    
    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String srchEntCode;
    
    @ApiModelProperty(value="blNo", example="1244567")
    private String srchHblNo;
	@ApiModelProperty(value="해상/항공")
	private String srchBound;
	@ApiModelProperty(value="수출입")
	private String srchBoundType;
    
    private String searchType;
    @ApiModelProperty(value="전송타입", example="xrayScan")
    private String srchSendStatus;
	@ApiModelProperty(value="처리종류")
	private String srchIfDocCode;
    
    private List<XraySend> xraySendList;
    private List<String> seqNoList;
    private List<String> searchNoList;
    private List<String> seqXrayList;
    private List<String> seqApprovalList;
}
