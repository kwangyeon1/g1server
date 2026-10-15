package org.yht.exps.express.model.eCommerce.quickRequest;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "QuickRequest", description = "고객서비스 퀵요청")
@Alias("quickRequest")
@EqualsAndHashCode(callSuper=false)
public class QuickRequest extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="수출입")
	private String boundType;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="상위업체명")
	private String custNameLv1;
	@ApiModelProperty(value="하위업체명")
	private String custNameLv2;
	@ApiModelProperty(value="H.B/L No.")
	private String hblNo;
	@ApiModelProperty(value="수량")
	private String pieces;
	@ApiModelProperty(value="수하인")
	private String consigneeName;
	@ApiModelProperty(value="운임구분")
	private String incoterms;
	@ApiModelProperty(value="운임구분_이름")
	private String incotermsName;
	@ApiModelProperty(value="퀵운임")
	private String quickFee;
	@ApiModelProperty(value="비고")
	private String remark;
	@ApiModelProperty(value="퀵인계일자")
	private String takeOverDate;
	@ApiModelProperty(value="배송기사이름")
	private String driverName;
	@ApiModelProperty(value="입력방법")
	private String inputMethod;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자명")
	private String logUserNameEng;
	
	
}
