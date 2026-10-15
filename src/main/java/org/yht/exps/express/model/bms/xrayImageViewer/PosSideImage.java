package org.yht.exps.express.model.bms.xrayImageViewer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "PosSideImage", description = "측면에서 촬영한 이미지")
@Alias("posSideImage")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class PosSideImage extends ResultList {

    @ApiModelProperty(value="사업장코드", example="YHTXX-KR")
    private String entBusinessCode;

    @ApiModelProperty(value="gmtTime_기본값 ")
    @Builder.Default
    private String gmtTime ="9";

    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    @Builder.Default
    private String dateFormat ="23";


    /** 반입 그리드 카운트*/
    @ApiModelProperty(value="총건수")
    private int cnt;
    @ApiModelProperty(value="목록건수")
    private int simpleCnt;
    @ApiModelProperty(value="일반건수")
    private int generalCnt;
    @ApiModelProperty(value="총건수")
    private int totalCnt;
    @ApiModelProperty(value="목록건수")
    private int totalSimpleCnt;
    @ApiModelProperty(value="일반건수")
    private int totalGeneralCnt;
    @ApiModelProperty(value="번호")
    private int num;

    @ApiModelProperty(value="시리얼")
    private int serial;
    @ApiModelProperty(value = "시리얼키")
    private String serialkey;
    @ApiModelProperty(value = "이미지 시리얼키")
    private String imgSerialKey;
    @ApiModelProperty(value = "스캔코드")
    private String scanCode;
    @ApiModelProperty(value = "BL번호")
    private String bl;
    @ApiModelProperty(value = "TOP 이미지 경로")
    private String imagePathTop;
    @ApiModelProperty(value = "SIDE 이미지 경로")
    private String imagePathSid;
    @ApiModelProperty(value = "기록일시")
    private String created;
}
