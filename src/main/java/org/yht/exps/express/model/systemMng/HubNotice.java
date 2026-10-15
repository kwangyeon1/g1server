package org.yht.exps.express.model.systemMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "HubNotice", description = "eHub 공지사항")
@Alias("hubNotice")
public class HubNotice extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String referenceNo;
	@ApiModelProperty(value="시스템공지여부")
	private String systemYn;
	@ApiModelProperty(value="공지구분")
	private String noticeType;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="공지일자")
	private String noticeDate;
	@ApiModelProperty(value="공지시간")
	private String noticeTime;
	@ApiModelProperty(value="공지마감일자")
	private String noticeDateEd;
	@ApiModelProperty(value="공지마감시간")
	private String noticeTimeEd;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="공지확정여부")
	private String noticeYn;
	@ApiModelProperty(value="제목")
	private String subject;
	@ApiModelProperty(value="파일명")
	private String fileTempName;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String entBusinessCode;
	@ApiModelProperty(value="사용자 ID")
	private String userId;

}
