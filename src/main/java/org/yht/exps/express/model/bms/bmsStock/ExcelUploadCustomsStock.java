package org.yht.exps.express.model.bms.bmsStock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "ExcelUploadCustomsStock", description = "세관재고 엑셀업로드")
@Alias("excelUploadCustomsStock")
public class ExcelUploadCustomsStock {

    @ApiModelProperty(value="도입사코드")
    private String entCode;
    @ApiModelProperty(value="사업장코드")
    private String entBusinessCode;
    @ApiModelProperty(value="AIR : 항공, SEA:해운")
    private String bound;
    @ApiModelProperty(value="IMP:수입, EXP:수출")
    private String boundType;
    @ApiModelProperty(value="입력자ID")
    private String registerUserId;
    @ApiModelProperty(value="수정자ID")
    private String updateUserId;
    @ApiModelProperty(value="gmtTime")
    private String gmtTime;
    @ApiModelProperty(value="날짜형식")
    private String dateFormat;
    @ApiModelProperty(value="오더관리번호")
    private String odJobNo;

    //엑셀 파일을 읽어서 가져온 데이터 리스트
    @ApiModelProperty(value="엑셀 업로드 리스트")
    private List<Map<String, Object>> dataList;

    //시스템 로그
    @ApiModelProperty(value="처리자")
    private String logUserName;
    @ApiModelProperty(value="처리자(영)")
    private String logUserNameEng;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;

}
