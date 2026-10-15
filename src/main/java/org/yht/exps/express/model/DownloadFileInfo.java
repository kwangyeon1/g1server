package org.yht.exps.express.model;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DownloadFileInfo", description = "다운로드할 파일 정보")
@Alias("downloadFileInfo")
public class DownloadFileInfo {
	@ApiModelProperty(value="저장된파일")
	private String saveFile;
	@ApiModelProperty(value="원본파일명")
	private String originFile;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="에러코드")
	private String errCode;
	@ApiModelProperty(value="시퀀스 (고객서비스 등록,발송 그리드 엑셀 다운로드 시 사용)")
	private int seq;
}
