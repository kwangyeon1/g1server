package org.yht.exps.express.model.eCommerce.consol;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ConsolRelease", description = "전자상거래 Consol 해제")
@Alias("consolRelease")
public class ConsolRelease {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="관리번호리스트")
	private List<Map<String,Object>> odJobNoList;
	@ApiModelProperty(value="마스터관리번호")
	private String masterOdJobNo;
	/** 저장 */
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="대표사업장코드")
	private String headBusinessCode;
	@ApiModelProperty(value="사용자거래처코드")
	private String ecmCustomerCode;
	
	/** 리턴값 */
	@ApiModelProperty(value="마스터에 있는 하우스 수")
	private int hblCnt;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}