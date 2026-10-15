package org.yht.exps.express.model.commonMng.stock;


import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "Stock", description = "전자상거래 Stock")
@Alias("stock")
public class Stock {
	
	@ApiModelProperty(value="배송사")
	private String trCode;	
	@ApiModelProperty(value="바운드")
	private String bound;		
	@ApiModelProperty(value="바운드타입")
	private String boundType;	
	@ApiModelProperty(value="대리점")
	private String agentCode;
	@ApiModelProperty(value="상(하)위업체명")
	private String customerName;
	
	@ApiModelProperty(value="상위대리점")
	private String parentAgentCode;
	@ApiModelProperty(value="상위업체명")
	private String parentCustomerName;
	
	@ApiModelProperty(value="SEQ_NO")
	private int seqNo;		
	@ApiModelProperty(value="도입사코드")
	private String entCode;	
	@ApiModelProperty(value="도입사_사업장코드")
	private String entBusinessCode;	
	@ApiModelProperty(value="스탁 일자")
	private String stockDate;
	@ApiModelProperty(value="스탁 재부여 일자")
	private String reStockDate;
	@ApiModelProperty(value="스탁 시작 번호")
	private long startNo;		
	@ApiModelProperty(value="스탁 끝 번호")
	private long endNo;	
	@ApiModelProperty(value="스탁 현재 번호")
	private long currentNo;		
	@ApiModelProperty(value="스탁 타입")
	private String stockType;		
	@ApiModelProperty(value="스탁 메소드")
	private String stockMethod;
	@ApiModelProperty(value="스탁사용여부")
	private String stockUseYn;
	@ApiModelProperty(value="등록자")
	private String registerUserId;		
	@ApiModelProperty(value="등록일자")
	private String registerDateTime;	
	@ApiModelProperty(value="수정자")
	private String updateUserId;		
	@ApiModelProperty(value="수정일자")
	private String updateDateTime;		
	@ApiModelProperty(value="사용건수")
	private String usedCount;	
	@ApiModelProperty(value="미사용건수")
	private String unusedCount;		
	@ApiModelProperty(value="건수")
	private String totalCount;	
	
	@ApiModelProperty(value="분할업체수")
	private String splitCount;
	
	@ApiModelProperty(value="담당자명")
	private String userName;
	@ApiModelProperty(value="배송사명")
	private String trName;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;		
	
	@ApiModelProperty(value="로그인 아이디")
	private String userLoginId;

}



