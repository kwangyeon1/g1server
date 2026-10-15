package org.yht.exps.express.model.edi.deliveryChg;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.edi.deliveryChg.search.SearchDeliveryChgList;
import org.yht.exps.express.model.search.SearchList;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "DeliveryChgDeclareList", description = "전자상거래  배송지변경신고 리스트")
@Alias("deliveryChgDeclareList")
public class DeliveryChgDeclareList extends SearchList{
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사업장코드")
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
	
	@ApiModelProperty(value="배송지변경신고 리스트")
	private List<DeliveryChg> deliveryChgList;	
	
	@ApiModelProperty(value="Reference No")
	private String srchIfDocReferenceNo;
	@ApiModelProperty(value="Scroll ID", example="FGluY2x1ZGVfY29udG....")
	private String srchScrollId;
	
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;	
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자코드")
	private String registerUserName;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}
