package org.yht.exps.express.model.bms.bmsStock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "BmsStockPkgScan", description = "재고")
@Alias("bmsStockPkgScan")
@AllArgsConstructor
@NoArgsConstructor
public class BmsStockPkgScan extends ResultList {

    @ApiModelProperty(value="데이터행 갯수")
    @Builder.Default
    private Integer inPlanNo = 0;
    @ApiModelProperty(value="SCAN수량")
    @Builder.Default
    private Integer scanCt = 1;

    @ApiModelProperty(value="오더관리번호")
    @Builder.Default
    private String odJobNo = "";
    @ApiModelProperty(value="SCAN번호")
    @Builder.Default
    private String scanNo = "";
    @ApiModelProperty(value="BL번호")
    @Builder.Default
    private String blNo = "";
    @ApiModelProperty(value="Package번호")
    @Builder.Default
    private String packingNo = "";
    @ApiModelProperty(value="SCAN일자")
    private String scanDate;
    @ApiModelProperty(value="SCAN시간")
    private String scanTime;
    @ApiModelProperty(value="SCAN결과")
    private String scanResult;
    @ApiModelProperty(value="입력자코드")
    private String registerUserId;
    @ApiModelProperty(value="입력자이름")
    private String registerUserName;
    @ApiModelProperty(value="입력일시")
    private String registerDateTime;
    
    @ApiModelProperty(value="재고실사SCAN 차수")
    private Integer stockScanSeq;
    
    /** out_plan 반출승인 **/
    @ApiModelProperty(value="승인통보명칭(31.승인,1:취하)")
    private String approvalClassStr;
    
    /** 박스 pkg **/
    @ApiModelProperty(value="출고CT")
    private Integer pkgOutCt;
    @ApiModelProperty(value="박스 로케이션상태")
    private String locationStatus;
    @ApiModelProperty(value="박스 로케이션")
    private String locationPlace;
    @ApiModelProperty(value="박스삭제여부 Y/N")
    private String deletedYn;	
    
    /** 반입 **/
    @ApiModelProperty(value="반입번호")
    private Integer inNo;
    
}
