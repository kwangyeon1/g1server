package org.yht.exps.express.model.bms.bmsStock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "BmsExcelStock", description = "엑셀파일 재고데이터")
@Alias("bmsExcelStock")
@AllArgsConstructor
@NoArgsConstructor
public class BmsExcelStock extends ResultList {

    @ApiModelProperty(value="gmtTime_기본값 ")
    @Builder.Default
    private String gmtTime ="9";

    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    @Builder.Default
    private String dateFormat ="23";

    /**TB_BMS_STOCK_TMP 에 입력되는 엑셀 데이터 */
    @ApiModelProperty(value="시퀀스번호")
    private Integer seq;
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="MBL번호")
    private String mblNo;
    @ApiModelProperty(value="영문품명")
    private String commodityItemLoc;
    @ApiModelProperty(value="수하인코드")
    private String consigneeCode;
    @ApiModelProperty(value="수하인상호")
    private String consigneeName;
    @ApiModelProperty(value="수하인기본주소")
    private String consigneeAddress1;
    @ApiModelProperty(value="수하인상세주소")
    private String consigneeAddress2;
    @ApiModelProperty(value="수하인전화번호")
    private String consigneeTel;
    @ApiModelProperty(value="통지처코드")
    private String notifyCode;
    @ApiModelProperty(value="통지처상호")
    private String notifyName;
    @ApiModelProperty(value="통지처기본주소")
    private String notifyAddress1;
    @ApiModelProperty(value="통지처상세주소")
    private String notifyAddress2;
    @ApiModelProperty(value="통지처전화번호")
    private String notifyTel;
    @ApiModelProperty(value="반입일자")
    private String carryinDate;
    @ApiModelProperty(value="예정수량")
    private Integer stockCt;
    @ApiModelProperty(value="예정중량")
    private Double stockWt;
    @ApiModelProperty(value="입력자코드")
    private String registerUserId;
    @ApiModelProperty(value="입력일시")
    private String registerDateTime;
}
