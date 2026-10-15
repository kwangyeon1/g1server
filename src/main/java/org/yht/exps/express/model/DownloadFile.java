package org.yht.exps.express.model;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DownloadFile", description = "파일 다운로드")
@Alias("downloadFile")
public class DownloadFile {
	@ApiModelProperty(value="저장된파일")
	private String saveFile;
	@ApiModelProperty(value="원본파일명")
	private String originFile;
	@ApiModelProperty(value="다운파일경로타입")
	private String pathType;
	@ApiModelProperty(value="다운파일경로추가")
	private String addPath;
	@ApiModelProperty(value="원본파일삭제여부", example="Y")
	private String originFileDelete;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="업무")
	private String solution;
	@ApiModelProperty(value="사용자 ID")
	private String userId;
	@ApiModelProperty(value="GMT Time")
	private String gmtTime;
	@ApiModelProperty(value="다운로드 하는 페이지명")
	private String pageTitle;
	@ApiModelProperty(value="다운로드 쿼리")
	private String url;
}
