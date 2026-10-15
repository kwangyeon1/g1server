package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import lombok.*;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModelProperty;
/***
 *  @Builder.Default 를 쓰는 이유는 디비 컬럼 모두가 NOT NULL로 되어 있어서 입력되지 않은 항목을 모두 입력 시키기 위해 선언함
 */
@Data
@Builder
@ApiModel(value = "BmsDelivery", description = "배송지시 VO 객체")
@Alias("bmsDelivery")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsDelivery {

    @ApiModelProperty(value="DELIVERY번호")
    private  Integer   deliveryNo ;

    @ApiModelProperty(value="문서참조번호")
    @Builder.Default  private  String   docRefNo    =  ""      ;

    @ApiModelProperty(value="문서구분(1.취소)")
    @Builder.Default  private  String   docClass    =  ""      ;

    @ApiModelProperty(value="DO번호")
    @Builder.Default  private  String   doNo    =  ""      ;

    @ApiModelProperty(value="MBL번호")
    @Builder.Default  private  String   mblNo    =  ""      ;

    @ApiModelProperty(value="HBL번호")
    @Builder.Default  private  String   blNo    =  ""      ;

    @ApiModelProperty(value="고객명칭")
    @Builder.Default  private  String   customerName    =  ""      ;

    @ApiModelProperty(value="포워더코드")
    @Builder.Default  private  String   fwdCode    =  ""      ;

    @ApiModelProperty(value="포워더명")
    @Builder.Default  private  String   fwdName    =  ""      ;

    @ApiModelProperty(value="포워더전화번호")
    @Builder.Default  private  String   fwdTel    =  ""      ;

    @ApiModelProperty(value="포워더DO담당자")
    @Builder.Default  private  String   fwdAttnName    =  ""      ;

    @ApiModelProperty(value="통지처명")
    @Builder.Default  private  String   notifyName    =  ""      ;

    @ApiModelProperty(value="수량")
    @Builder.Default  private  Integer   ct    =  0      ;

    @ApiModelProperty(value="중량")
    @Builder.Default  private  Double   wt    =   0.0     ;

    @ApiModelProperty(value="취소여부")
    @Builder.Default  private  String   cancelYn    =  "N"      ;

    @ApiModelProperty(value="취소일자")
    @Builder.Default  private  String   cancelDate    =  ""      ;

    @ApiModelProperty(value="취소시간")
    @Builder.Default  private  String   cancelTime    =  ""      ;

    @ApiModelProperty(value="등록여부")
    @Builder.Default  private  String   registYn    =  "N"    ;

    @ApiModelProperty(value="비고")
    @Builder.Default  private  String   remarks    =  ""      ;

    @ApiModelProperty(value="LC여부")
    @Builder.Default  private  String   lcYn    =   "N"      ;

    @ApiModelProperty(value="LC개설은행")
    @Builder.Default  private  Object   lcBank    =  ""      ;

    @ApiModelProperty(value="LC번호(Letter Of Credit):신용장")
    @Builder.Default  private  Object   lcNo    =  ""      ;

    @ApiModelProperty(value="HSN코드")
    @Builder.Default  private  String   hsn    =  ""      ;

    @ApiModelProperty(value="수신일자")
    @Builder.Default  private  String   recvDate    =  ""      ;

    @ApiModelProperty(value="수신일시")
    @Builder.Default  private  String   recvTime    =  ""      ;




}
