package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

/**
 * 휴일 관리
 *
 * @author Woong Jang
 * @since 2024-05-02 16:53:45
 */
@Data
@Builder
@ApiModel(value = "BmsHoliday", description = "휴일관리 VO 객체")
@Alias("bmsHoliday")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsHoliday extends ResultList {

    @ApiModelProperty(value = "휴일날짜")
    @Builder.Default
    private String holidayDate = "";


    @ApiModelProperty(value = "이전휴일날짜(수정에사용)")
    @Builder.Default
    private String oldHolidayDate = "";

    @ApiModelProperty(value = "입력조회수정삭제모드")
    @Builder.Default
    private String crudMode = "";



    @ApiModelProperty(value="총건수")
    private int totalCnt;
    @ApiModelProperty(value="목록건수")
    private int totalSimpleCnt;
    @ApiModelProperty(value="일반건수")
    private int totalGeneralCnt;
    @ApiModelProperty(value="총건수")
    private int cnt;
    @ApiModelProperty(value="목록건수")
    private int simpleCnt;
    @ApiModelProperty(value="일반건수")
    private int generalCnt;

}

