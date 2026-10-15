package org.yht.exps.express.model.edi.deliveryChg.search;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChg;
import org.yht.exps.express.model.search.SearchList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchDeliveryChgList", description = "전자상거래 배송지변경신고 리스트 검색 조건")
@Alias("searchDeliveryChgList")
public class SearchDeliveryChgList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="검색타입")
	private String srchNoType;
	@ApiModelProperty(value="검색text")
	private String srchNo;
	@ApiModelProperty(value="자료생성구분")
	private String srchDataGen;
	@ApiModelProperty(value="신고여부")
	private String srchSendStatus;
	@ApiModelProperty(value="신고여부")
	private String srchCustomsCode;
	@ApiModelProperty(value="hblNo")
	private String srchNoHouse;
	
	@ApiModelProperty(value="boundType", example="AI, SI")
	private String srchBoundType;
	
	@ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
	private String srchNoLikeYn;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;	
	
	@ApiModelProperty(value="통관구분")
	private String srchCdType;
	@ApiModelProperty(value="배송지변경신청서_제출번호")
	private String ediDocNo;
	
	@ApiModelProperty(value="오더 리스트 json String")
	private String deliveryChgListStr;	
	
	@ApiModelProperty(value="오더 리스트")
	private List<DeliveryChg> deliveryChgList;	
	
	@ApiModelProperty(value="신고 리스트")
	private List<DeliveryChg> ediDocNoList;	
	
	@ApiModelProperty(value="Reference No")
	private String srchIfDocReferenceNo;
	@ApiModelProperty(value="Scroll ID", example="FGluY2x1ZGVfY29udG....")
	private String srchScrollId;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	

	@ApiModelProperty(value="엑셀다운로드타입")
	private String excelType;
	@ApiModelProperty(value="검색조건")
	private Map<String,Object> srchInfo;
	@ApiModelProperty(value="과태료타입")
	private String penaltyType;
}
