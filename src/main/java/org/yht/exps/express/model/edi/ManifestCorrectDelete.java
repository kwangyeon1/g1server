package org.yht.exps.express.model.edi;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestCorrectDelete", description = "전자상거래  적하정정 삭제")
@Alias("manifestCorrectDelete")
public class ManifestCorrectDelete {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="적하목록정정_관리번호")
	private String modJobNo;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="리스트")
	private List<ManifestCorrect> manifestCorrectList;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;	
	
}

