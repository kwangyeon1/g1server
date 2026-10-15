package org.yht.exps.express.model.bms.bmsTotalManage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "BmsTotalManage", description = "통합관리")
@Alias("bmsTotalManage")
public class BmsTotalManage extends ResultList {
    @ApiModelProperty(value="진행상태")
    private String lastPodStatus;
    @ApiModelProperty(value="입항일자")
    private String arDate;
    @ApiModelProperty(value="입항시간")
    private String arTime;
    @ApiModelProperty(value="도착일시")
    private String destDateTime;
    @ApiModelProperty(value="M.B/L번호")
    private String mblNo;
    @ApiModelProperty(value="편명(선기명)")
    private String vesselFlight;
    @ApiModelProperty(value="수량")
	private Integer ct;
    @ApiModelProperty(value="중량")
	private double wt;
    @ApiModelProperty(value="총건수")
    private Integer hblCnt;
    @ApiModelProperty(value="목록")
    private Integer simpleCnt;
    @ApiModelProperty(value="일반")
    private Integer generalCnt;
    @ApiModelProperty(value="배제")
    private Integer excludeCnt;
    @ApiModelProperty(value="반입예정")
    private Integer inPlanCnt;
    @ApiModelProperty(value="반입")
    private Integer carryInCnt;
    @ApiModelProperty(value="미반입")
    private Integer noCarryinCnt;
    @ApiModelProperty(value="판독")
    private Integer xrayCnt;
    @ApiModelProperty(value="반출")
    private Integer carryOutCnt;
    @ApiModelProperty(value="재고")
    private Integer stockCnt;
    @ApiModelProperty(value="이고반출")
    private Integer moveCarryOutCnt;
    @ApiModelProperty(value="이고반입")
    private Integer moveCarryInCnt;
    @ApiModelProperty(value="견품반출")
    private Integer sampleOutCnt;
    @ApiModelProperty(value="보수")
    private Integer maintenanceCnt;
    @ApiModelProperty(value="BL분할")
    private Integer partialCnt;
    @ApiModelProperty(value="거대")
    private Integer bigSizeCnt;
    @ApiModelProperty(value="선별 마약")
    private Integer seInsp01Cnt;
    @ApiModelProperty(value="선별 총기")
    private Integer seInsp02Cnt;
    @ApiModelProperty(value="선별 검역")
    private Integer seInsp03Cnt;
    @ApiModelProperty(value="선별 협업")
    private Integer seInsp05Cnt;
    @ApiModelProperty(value="선별 기타")
    private Integer seInsp06Cnt;
    @ApiModelProperty(value="선별 신고인검사")
    private Integer seInsp07Cnt;
    @ApiModelProperty(value="검역(일반)")
    private Integer insp05Cnt;
    @ApiModelProperty(value="검역(사전)")
    private Integer insp06Cnt;
    @ApiModelProperty(value="검역(동물)")
    private Integer insp08Cnt;
    @ApiModelProperty(value="검역(식물)")
    private Integer insp09Cnt;
    @ApiModelProperty(value="검역(식품)")
    private Integer insp10Cnt;
    @ApiModelProperty(value="검역(서류)")
    private Integer insp11Cnt;
    @ApiModelProperty(value="XRAY 마약")
    private Integer xray01Cnt;
    @ApiModelProperty(value="XRAY 총기")
    private Integer xray02Cnt;
    @ApiModelProperty(value="XRAY 검역")
    private Integer xray03Cnt;
    @ApiModelProperty(value="XRAY 식품의약")
    private Integer xray04Cnt;
    @ApiModelProperty(value="XRAY 기타")
    private Integer xray06Cnt;
    @ApiModelProperty(value="집중")
    private Integer inspCustomsCnt;
    @ApiModelProperty(value="지재")
    private Integer intellPropCnt;
    @ApiModelProperty(value="박스예정")
    private Integer boxInPlanCnt;
    @ApiModelProperty(value="박스반입")
    private Integer boxInCnt;
    @ApiModelProperty(value="박스판독")
    private Integer boxXrayCnt;
    @ApiModelProperty(value="박스반출")
    private Integer boxOutCnt;
}
