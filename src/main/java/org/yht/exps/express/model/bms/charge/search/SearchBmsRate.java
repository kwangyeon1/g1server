package org.yht.exps.express.model.bms.charge.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

@Data
@EqualsAndHashCode(callSuper=true)
@ApiModel(value = "SearchBmsRate", description = "창고료및 작업료 조회 조건")
@Alias("searchBmsRate")
public class SearchBmsRate extends SearchList{

	@ApiModelProperty(value="적용일자")
	private String srchApplyDate;
	@ApiModelProperty(value="요율구분(보관료와같음)")
	private  String   srchApplyClass    =  ""      ;


	@ApiModelProperty(value="창고료번호(매출번호)")
	private  Integer   srchEstimateNo ;
	@ApiModelProperty(value="보관요율번호")
	private  Integer   srchStorageRateNo ;

	@ApiModelProperty(value="작업료요율번호")
	private  Integer  srchWorkRateNo ;

	@ApiModelProperty(value="보관일수")
	private  Integer   srchStorgeDays ;

	@ApiModelProperty(value="매출일자(계산일자)")
	private  String   srchEstimateDate ;
	@ApiModelProperty(value="반입일자")
	private  String   srchCarryinDate         ;

	@ApiModelProperty(value="반입시간")
	private  String   srchCarryinTime         ;

	@ApiModelProperty(value="반출일자")
	private  String   srchCarryoutDate          ;

	@ApiModelProperty(value="반출시간")
	private  String   srchCarryoutTime       ;

	@ApiModelProperty(value="면제여부(N.아님,Y.면제,C.4일면제)")
	private  String   srchFreeYnc         ;



	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;

	@ApiModelProperty(value="대표사업장코드", example="YHTXX")
	private String srchEntBusinessCode;

	@ApiModelProperty(value="재고 상태") // STOCK, OUT, OUT_APPROVAL(반출승인), CANCEL
	private String srchCarryInState;

	@ApiModelProperty(value="재고 조회 번호")
	private Integer srchInNo;


	@ApiModelProperty(value="다국어 관련 코드")
	private String srchUserLanguage;

	@ApiModelProperty(value="BL번호, 신고번호등 Like 검색 유무", example="YN")
	private String srchNoLikeYn;



	@ApiModelProperty(value = "조회년도")
	private String srchYyyy = "";
}
