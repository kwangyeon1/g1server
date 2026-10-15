package org.yht.exps.express.model.com.fileshare;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "fileShareFile", description = "다중 업로드 파일 리스트")
@Alias("fileShareFile")
public class FileShareFile {
	@ApiModelProperty(value="관리번호_제번")
	private int referenceNoId;
	@ApiModelProperty(value="일련번호")
	private int seqNo;
	@ApiModelProperty(value="파일문서구분")
	private String fileDocType;
	@ApiModelProperty(value="파일명칭")
	private String fileName;
	@ApiModelProperty(value="파일크기")
	private long fileSize;
	@ApiModelProperty(value="파일명_원격지")
	private String fileNameRemote;
	@ApiModelProperty(value="파일경로_원격지")
	private String filePathRemote;
}
