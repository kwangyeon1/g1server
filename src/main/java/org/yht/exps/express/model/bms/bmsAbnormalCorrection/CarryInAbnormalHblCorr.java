package org.yht.exps.express.model.bms.bmsAbnormalCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "CarryInAbnormalHblCorr", description = "반입물품이상신고HBL")
@Alias("carryInAbnormalHblCorr")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class CarryInAbnormalHblCorr extends ResultList {

    @ApiModelProperty(value="사업장 보세 구역번호 8자리")
    private String bmsWhCode;

    @ApiModelProperty(value="gmtTime_기본값 ")
    @Builder.Default
    private String gmtTime ="9";

    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    @Builder.Default
    private String dateFormat ="23";

    /**반입이상정정_BL(TB_BMS_ABNORMAL_BL)*/
    @ApiModelProperty(value="반입물품이상정정번호")
    private Integer abnChangeNo;
    @ApiModelProperty(value="순번")
    private Integer seqNo;
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="House B/L 일련번호(House B/L Sequence Number)")
    private String hsn;
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="정정구분")
    private String changeClass;
    @ApiModelProperty(value="포장종류")
    private String pkgType;
    @ApiModelProperty(value="예정수량")
    private Integer exCt;
    @ApiModelProperty(value="예정중량")
    private Double exWt;
    @ApiModelProperty(value="수량")
    private Integer ct;
    @ApiModelProperty(value="중량")
    private Double wt;
    @ApiModelProperty(value="사고구분")
    private String accidentClass;
    @ApiModelProperty(value="사고수량")
    private Integer accidentCt;
    @ApiModelProperty(value="사고중량")
    private Double accidentWt;
    @ApiModelProperty(value="사유")
    private Object rejectReason;
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

    @ApiModelProperty(value="반입이상등록여부") //House / Master Single 영역의 그리드에서 기존에 존재하는 정보와 +버튼으로 추가한 정보를 구분하기 위해 추가
    private String abnYn;

    //시스템 로그
    @ApiModelProperty(value="처리자")
    private String logUserName;
    @ApiModelProperty(value="처리자(영)")
    private String logUserNameEng;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;

    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="사업장코드", example="YHTXX-KR")
    private String entBusinessCode;
    @ApiModelProperty(value="오더관리번호")
    private String odJobNo;
}
