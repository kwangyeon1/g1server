package org.yht.exps.express.model.bms.out;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@Data
@ApiModel(value = "BmsOutDelivery", description = "택배사연계 VO 객체")
@Alias("BmsOutDelivery")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsOutDelivery extends ResultList{
	
    @ApiModelProperty(value="BL번호")
    @Builder.Default private String blNo = "";
    
    @ApiModelProperty(value="스캔일자")
    @Builder.Default private String registerDateTime = "";
    
	@ApiModelProperty(value="박스번호")
    private String packingNo;
	
    @ApiModelProperty(value="택배사코드")
    @Builder.Default private String deliveryCode = "";
    
    @ApiModelProperty(value="택배사코드명")
    @Builder.Default private String deliveryCodeName = "";
    
    @ApiModelProperty(value="화물관리번호")
    @Builder.Default private String cargoReferenceNo = "";
    
    @ApiModelProperty(value="반출승인일자")
    @Builder.Default private String approvalDate = "";
    
    @ApiModelProperty(value="반출승인번호")
    @Builder.Default private String outApprovalNo = "";
    
//    @ApiModelProperty(value="승인통보(31.승인,1:취하)")
    @ApiModelProperty(value="승인구분")
    @Builder.Default private String approvalClass = "";    
    
    @ApiModelProperty(value="승인수량")
    private Integer approvalCt;
    
	@ApiModelProperty(value="승인중량")
	private String approvalWt;  
	
	@ApiModelProperty(value="스캔결과")
	private String scanResult;
	
	@ApiModelProperty(value="전송여부")
    private String deleverySendIfYn;
	
    @ApiModelProperty(value="전송일시")
    @Builder.Default private String deleverySendDateTime = "";
    
    @ApiModelProperty(value="전송결과")
	private String sendResult;
       
	@ApiModelProperty(value="반입번호")
    private Integer inNo;
    
    @ApiModelProperty(value="반입일자")
    private String carryinDate;
    
	@ApiModelProperty(value="총건수")
	private int totalCnt;
	
	@ApiModelProperty(value="SEQ")
    private Integer seq;
	
	@ApiModelProperty(value="반출중량")
    private int wt;
	
	@ApiModelProperty(value="반출부피")
    private int volm;
	
}
