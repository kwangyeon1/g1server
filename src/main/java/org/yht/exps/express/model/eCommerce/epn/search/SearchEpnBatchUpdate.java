package org.yht.exps.express.model.eCommerce.epn.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "SearchEpnBatchUpdate", description = "EPN 이관")
@Alias("searchEpnBatchUpdate")
public class SearchEpnBatchUpdate {
    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="사업장코드")
    private String userBusinessCode;
    @ApiModelProperty(value="모드")
    private String chkMode;
    @ApiModelProperty(value="수정자코드")
    private String updateUserId;
    @ApiModelProperty(value="GMT_TIME")
    private String gmtTime;

    @ApiModelProperty(value="업데이트대상 조회조건")
    private SearchEpnList searchEpnList;

    @ApiModelProperty(value="epn 리스트")
    private List<Map<String, Object>> epnList;

    //시스템 로그
    @ApiModelProperty(value="처리자")
    private String logUserName;
    @ApiModelProperty(value="처리자(영)")
    private String logUserNameEng;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;

}
