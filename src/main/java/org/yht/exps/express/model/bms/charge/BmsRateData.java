package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.util.List;
import java.util.Map;

/**
 * 특송창고_작업료율(BmsStorageRate)Entity
 *
 * @author Woong Jang
 * @since 2024-04-19 08:59:33
 */
@Data
@ApiModel(value = "BmsRateData", description = "창고료 작업료 입력 수정시 리스트 객체  매핑 VO 객체")
@Alias("bmsRateData")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsRateData {




    @ApiModelProperty(value="보관료 계산 Base 객체")
   private List<BmsStorageRate> dataRate;

    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="대표사업장코드", example="YHTXX")
    private String entBusinessCode;
    @ApiModelProperty(value="등록자")
    private String inputUserId;
    @ApiModelProperty(value="리스트")
    private List<Map<String, Object>> dataList;

    @ApiModelProperty(value="gmtTime_기본값 ")
     private String gmtTime ="9";
    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
     private String dateFormat ="23";

    //시스템 로그
    @ApiModelProperty(value="처리자")
    private String logUserName;
    @ApiModelProperty(value="처리자(영)")
    private String logUserNameEng;


}

