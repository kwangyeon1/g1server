package org.yht.exps.express.model.edi.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchManifestCorrectList", description = "적하목록  정정 리스트 검색 조건")
@Alias("searchManifestCorrectList")
public class SearchManifestCorrectList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="검색타입")
	private String srchNoType;
	@ApiModelProperty(value="정정구분")
	private String srchModType;
	
	@ApiModelProperty(value="검색text")
	private String srchNo;
		
	@ApiModelProperty(value="mblNo")
	private String mblNo;
	@ApiModelProperty(value="hblNo")
	private String hblNo;
	
	@ApiModelProperty(value="mrn")
	private String mrn;
	@ApiModelProperty(value="정정구분")
	private String modType;
	
	@ApiModelProperty(value="적하목록정정_관리번호")
	private String modJobNo;
	@ApiModelProperty(value="컨테이너정정구분_코드(I: B/L 추가, A:추가,U:정정, D:삭제)")
	private String cntrModTypeIn;
	@ApiModelProperty(value="컨테이너정정구분_코드(I: B/L 추가, A:추가,U:정정, D:삭제)")
	private List<String> cntrModTypeInList;
	
	@ApiModelProperty(value="바운드")
	private String srchBound;
	@ApiModelProperty(value="바운드타입")
	private String srchBoundType;
	@ApiModelProperty(value="리버스")
	private String srchRvsBoundType;		
	@ApiModelProperty(value="seqNoId")
	private String seqNoId;	
	@ApiModelProperty(value="작업구분")
	private String action;
	@ApiModelProperty(value="containerSeqNoId")
	private String cntrSeqNoId;	
	
	@ApiModelProperty(value="마스트 리스트")
	private String srchModJobNoListStr;	
	@ApiModelProperty(value="마스트 리스트 In")
	private List<String> srchModJobNoListArr;	
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;	
	
	
}
