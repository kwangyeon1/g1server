package org.yht.exps.express.model.bms.bmsOutNotice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import java.util.List;

@Data
@ApiModel(value = "BmsOutNoticeList", description = "반출통고신고 - 반출통고 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsOutNoticeList")
public class BmsOutNoticeList {

    @ApiModelProperty(value="리스트 개수")
    private long total;
    @ApiModelProperty(value="리스트")
    private List<BmsOutNotice> bmsOutNoticeList;
    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
    
    /** 시스템 로그 - ES */
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="대표사업장코드", example="YHTXX-KR")
	private String entBusinessCode;
	@ApiModelProperty(value="등록자")
	private String inputUserId;
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	@ApiModelProperty(value="사유")
	private String reason;
    @ApiModelProperty(value="BL번호")
    private String blNo;
	@ApiModelProperty(value="오더관리번호")
    private String odJobNo;

}
