package org.yht.exps.express.model.mobile.od;

import java.util.List;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MobEcodMulti", description = "전자상거래  모바일  Order Multi 리스트")
@Alias("mobEcodMulti")
public class MobEcodMulti {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;

	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="배송번호")
	private String deliveryNo;
	@ApiModelProperty(value="배송지코드")
	private String deliveryCode;
	@ApiModelProperty(value="배송지명")
	private String deliveryName;
	@ApiModelProperty(value="정산처코드")
	private String customerCode;
	@ApiModelProperty(value="정산처명")
	private String customerName;
	@ApiModelProperty(value="변경배송번호")
	private String deliveryNoChanged;
	@ApiModelProperty(value="변경배송코드")
	private String deliveryCodeChange;
	@ApiModelProperty(value="멀티/오더구분없이 들어오는 BL번호")
	private String notChkBl;
	@ApiModelProperty(value="결제조건")
	private String paymentGubn;
	@ApiModelProperty(value="수량")
	private int pieces;	
	@ApiModelProperty(value="배송비")
	private double deliveryFee;
	@ApiModelProperty(value="화물규격_가로")
	private int dimWidth;
	@ApiModelProperty(value="화물규격_길이")
	private int dimLength;
	@ApiModelProperty(value="화물규격_높이")
	private int dimHeight;
	@ApiModelProperty(value="화물규격_볼륨")
	private double vwt;
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
	@ApiModelProperty(value="구분")
	private String gubun;
	
	/** 저장 */
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	
	
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="반출")
	private String ecm280YN;
	@ApiModelProperty(value="홀드")
	private String holdYN;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="도착항")
	private String destCode;
	@ApiModelProperty(value="프린트위치")
	private String chkPrintPoint;

	
	@ApiModelProperty(value="멀티리스트")
	private List<MobEcodMulti> multiList;
}
