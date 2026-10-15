package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

/**
 * 특송창고_작업료율(BmsStorageRate)Entity
 *
 * @author Woong Jang
 * @since 2024-04-19 08:59:33
 */
@Data
@ApiModel(value = "BmsRateBase", description = "창고료 작업료 base값 리턴용 요율구분, 보관일수,반입일자에 따라서 다른값 매핑 VO 객체")
@Alias("BmsRateBase")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsRateBase  {


    @ApiModelProperty(value="발행번호")
    private  String   applyNo    =   ""       ;
    @ApiModelProperty(value="보관일수 - 휴일, 공휴일 뺀값 : 2.수입특송에서 사용")
    private  Integer   storageDays    =   0       ;
    @ApiModelProperty(value="보관일수 전체 -  단순 종료일-시작일 : 나머지에서  사용")
    private  Integer   storageAllDays    =   0       ;


    @ApiModelProperty(value="보관료 계산 Base 객체")
   private BmsStorageRate storageRate;
    @ApiModelProperty(value="작업료 계산 Base 객체")
   private BmsWorkRate workRate;


}

