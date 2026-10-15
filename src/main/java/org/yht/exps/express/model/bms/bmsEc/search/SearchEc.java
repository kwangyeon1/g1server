package org.yht.exps.express.model.bms.bmsEc.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchEc", description = "EC송수신 조회")
@Alias("searchEc")
public class SearchEc extends SearchList {
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
    @ApiModelProperty(value="인터페이스ID")
    private String srchInterfaceId;
    @ApiModelProperty(value="인터페이스SEQ")
    private String srchInterfaceSeq;
    @ApiModelProperty(value="인터페이스(송신) 진행상태")
    private String srchSndStatCd;
    @ApiModelProperty(value="인터페이스(수신) 진행상태")
    private String srchProsStatCd;
    
 
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	
}
