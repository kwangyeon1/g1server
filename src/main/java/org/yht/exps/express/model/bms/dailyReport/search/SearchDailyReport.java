package org.yht.exps.express.model.bms.dailyReport.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.dailyReport.CarryInReport;
import org.yht.exps.express.model.bms.dailyReport.CarryOutReport;
import org.yht.exps.express.model.bms.dailyReport.PeriodReport;
import org.yht.exps.express.model.bms.dailyReport.WorkReport;
import org.yht.exps.express.model.search.SearchList;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchDailyReport", description = "일일보고")
@Alias("searchDailyReport")
public class SearchDailyReport extends SearchList {

    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String srchEntCode;
    @ApiModelProperty(value="조회번호")
    private String srchNo;
    @ApiModelProperty(value="조회번호타입")
    private String srchNoType;
    @ApiModelProperty(value="조회타입 멀티/싱글")
    private String searchType;
    @ApiModelProperty(value="srchNo N개씩 조회시 리스트")
    private List<String> searchNoList;

    @ApiModelProperty(value="기준일자 구분")
    private String srchBaseDate;
    @ApiModelProperty(value="조회일자 구분")
    private String srchDateGbn;
    @ApiModelProperty(value="조회 시작일자")
    private String srchStartDate;
    @ApiModelProperty(value="조회 종료일자")
    private String srchEndDate;

    @ApiModelProperty(value="조회구분(세관기준,창고기준)")
    private String srchBaseState;

    @ApiModelProperty(value="그리드페이지사이즈", example="50")
    private int pageSize;

    /** 엑셀다운 */
    @ApiModelProperty(value="반입보고 리스트")
    private List<CarryInReport> carryInReportList;
    
    @ApiModelProperty(value="반출보고 리스트")
    private List<CarryOutReport> carryOutReportList;
    
    @ApiModelProperty(value="기간별 반출입 리스트")
    private List<PeriodReport> periodReportList;

    @ApiModelProperty(value="근무일지리스트")
    private List<WorkReport> workReportList;
    
    @ApiModelProperty(value="문서양식코드(엑셀다운로드)")
    private String documentCode;
    @ApiModelProperty(value="문서명(엑셀다운로드)")
    private String documentName;

    @ApiModelProperty(value="메뉴명")
    private String menuName;
    @ApiModelProperty(value="일일보고구분")
    private String reportType;
}
