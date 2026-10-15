package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "OdAbnormal", description = "하기이상정보")
@Alias("odAbnormal")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class OdAbnormal {

    @ApiModelProperty(value="gmtTime_기본값 ")
    @Builder.Default
    private String gmtTime ="9";
    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    @Builder.Default
    private String dateFormat ="23";

    @ApiModelProperty(value="관리번호")
    private String odJobNo;
    @ApiModelProperty(value="일련번호_자동")
    private Integer seqNoId;
    @ApiModelProperty(value="하기이상_코드")
    private String abnormalCode;
    @ApiModelProperty(value="MRN")
    @Builder.Default
    private String mrn = "";
    @ApiModelProperty(value="MSN")
    @Builder.Default
    private String msn = "";
    @ApiModelProperty(value="HSN")
    @Builder.Default
    private String hsn= "";
    @ApiModelProperty(value="선명_편명")
    private String vesselFlight;
    @ApiModelProperty(value="항공/해운수입 하기(하선)장소코드 (장치장 또는 보세창고코드)")
    private String discWhCode;
    @ApiModelProperty(value="하기이상_입항일자")
    private String arDate;
    @ApiModelProperty(value="하기이상_수량")
    private Integer pieces;
    @ApiModelProperty(value="하기이상_중량")
    private Double gwt;
    @ApiModelProperty(value="하기이상_사고개수")
    private Integer cnt;
    @ApiModelProperty(value="하기이상보고_신고일시")
    private String sendDate;
    @ApiModelProperty(value="입력자코드")
    private String registerUserId;
    @ApiModelProperty(value="입력자이름")
    private String registerUserName;
    @ApiModelProperty(value="입력일시")
    private String registerDateTime;
    @ApiModelProperty(value="수정자코드")
    private String updateUserId;
    @ApiModelProperty(value="수정자이름")
    private String updateUserName;
    @ApiModelProperty(value="수정일시")
    private String updateDateTime;
}
