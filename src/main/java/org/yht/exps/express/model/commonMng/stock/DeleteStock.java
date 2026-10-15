package org.yht.exps.express.model.commonMng.stock;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "DeleteStock", description = "전자상거래  Stock 삭제 ")
@Alias("deleteStock")
public class DeleteStock {
	
	@ApiModelProperty(value="배송사", example="KOREX OR EMS")
	private String trCode;	
	@ApiModelProperty(value="바운드", example="AIR OR SEA")
	private String bound;		
	@ApiModelProperty(value="바운드타입", example="EXP OR IMP")
	private String boundType;	
	@ApiModelProperty(value="대리점")
	private String agentCode;	
	@ApiModelProperty(value="seqNo")
	private String seqNo;		
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;			
	@ApiModelProperty(value="스탁시작번호", example="")
	private String startNo;		
	@ApiModelProperty(value="스탁끝번호", example="")
	private String endNo;		
	@ApiModelProperty(value="등록자")
	private String inputUserId;		
	@ApiModelProperty(value="리스트")
	private List<Map<String, Object>> dataList;

	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;	

}

