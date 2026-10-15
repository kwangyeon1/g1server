package org.yht.exps.express.model.bms.bmsTotalManage.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchTotalManage", description = "통합관리")
@Alias("searchTotalManage")
public class SearchTotalManage extends SearchList {
	/* Search */
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="조회일자타입", example="dateTime")
    private String srchDateType;
	@ApiModelProperty(value="조회일자타입", example="23")
	private String srchDateFormat;
    @ApiModelProperty(value="조회일자 구분")
    private String srchDateGbn;
    @ApiModelProperty(value="조회 시작일자")
    private String srchStartDate;
    @ApiModelProperty(value="조회 종료일자")
    private String srchEndDate;
    @ApiModelProperty(value="조회번호")
    private String srchNo;
    @ApiModelProperty(value="조회번호타입")
    private String srchNoType;
    @ApiModelProperty(value="조회타입 멀티/싱글")
    private String searchType;
    @ApiModelProperty(value="조회타입 진행상태")
    private String srchStatusGbn;
    @ApiModelProperty(value="조회타입 타입")
    private String srchCdTypeGbn;
    @ApiModelProperty(value="조회타입 상세 타입 드롭")
    private String srchCdTypeDetailGbn;
    @ApiModelProperty(value="조회 마스터 컬럼 타입")
    private String srchRowColNm;
    @ApiModelProperty(value="조회 마스터 MBL 번호")
    private String srchRowMblNo;
    @ApiModelProperty(value="조회 마스터 입항 일자")
    private String srchRowArDate;
    @ApiModelProperty(value="조회 마스터 입항 시간")
    private String srchRowArTime;
    @ApiModelProperty(value="조회 마스터 선기명")
    private String srchRowVesselFlight;
    @ApiModelProperty(value="조회타입 상세 타입 체크")
    private List<String> checkTypeTab;
    @ApiModelProperty(value="조회타입 하우스 이고대상 구분")
    private String srchHouseCdTypeGbn;
    @ApiModelProperty(value="이고구분")
	private String srchHouseCdTypeGbnName;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="통관구분 리스트(멀티체크)")
	private List<String> srchCdTypeList;
	
	@ApiModelProperty(value="통관구분")
	private String srchCdType;
}
