package org.yht.exps.express.model.selectMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "SelfSelection", description = "통관목록 자체선별(기초)")
@Alias("selfSelection")
@EqualsAndHashCode(callSuper=false)
public class SelfSelection extends ResultList{
	@ApiModelProperty(value="사용여부")
	private String useYn;
	@ApiModelProperty(value="선별종류")	
	private String selectionTarget;
	@ApiModelProperty(value="대상항목")
	private String dataField;
	@ApiModelProperty(value="적용내용")
	private String keyword;
	@ApiModelProperty(value="검색방법")
	private String operator;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="서류여부")
	private String documentYn;
	@ApiModelProperty(value="변경할 HS Code")
	private String updateHsCode;
	@ApiModelProperty(value="관리번호")
	private String jobNo;
	
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="수정자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}
