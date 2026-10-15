package org.yht.exps.express.model.eCommerce.unusualMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "UnusualMng", description = "고객서비스 이례화물관리")
@Alias("unusualMng")
@EqualsAndHashCode(callSuper=false)
public class UnusualMng extends ResultList{

	@ApiModelProperty(value="도입사")
	private String entCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
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
	@ApiModelProperty(value="Order No.")
	private String poNo;
	@ApiModelProperty(value="M.B/L No.")
	private String mblNo;
	@ApiModelProperty(value="H.B/L No.")
	private String hblNo;
	@ApiModelProperty(value="통관구분")
	private String cdType;
	@ApiModelProperty(value="통관구분명")
	private String cdTypeName;
	@ApiModelProperty(value="취하코드")
	private String dropCode;
	@ApiModelProperty(value="취하사유")
	private String dropReason;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="신고여부")
	private String reportYn;
	@ApiModelProperty(value="관세사코드")
	private String cbCode;
	@ApiModelProperty(value="관세사명")
	private String cbName;
	@ApiModelProperty(value="진행사항")
	private String lastPodStatus;
	@ApiModelProperty(value="이례코드")
	private String unusualCode;
	@ApiModelProperty(value="이례코드명")
	private String unusualCodeName;
	@ApiModelProperty(value="발생일자")
	private String unusualDateTime;
	@ApiModelProperty(value="이례사유")
	private String unusualReason;
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
	
	@ApiModelProperty(value="File Share")
	private String fileShare;
	
	
}
