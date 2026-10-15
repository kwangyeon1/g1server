package org.yht.exps.express.model.eCommerce.multiWg;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MultiWgList", description = "전자상거래  멀티중량 리스트")
@Alias("multiWgList")
public class MultiWgList {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;	
	@ApiModelProperty(value="등록하는 유저의 사업장코드")
	private String userBusinessCode;	
	
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="멀티중량 리스트")
	private List<MultiWgOrder> multiWgOrderList;
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="하우스번호")
	private String hblNo;
	@ApiModelProperty(value="바운드")
	private String bound;
	@ApiModelProperty(value="바운드타입")
	private String boundType;
	@ApiModelProperty(value="인수자")
	private String recipientName;	
	@ApiModelProperty(value="인수일자")
	private String receiptDate;
	@ApiModelProperty(value="확정취소구분")
	private String gubun;
	@ApiModelProperty(value="기능구분")
	private String action;
	

	/** 저장 */
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
	

	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
}
