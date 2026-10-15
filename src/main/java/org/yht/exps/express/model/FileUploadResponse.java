package org.yht.exps.express.model;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "FileUploadResponse", description = "File Upload 후 응답내용")
@Alias("fileUploadResponse")
public class FileUploadResponse {
	public static final String RESULT_OK = "ok";
	public static final String RESULT_FAIL = "fail";
	public static final String RESULT_SIZE_OVER = "sizeover";
	public static final String RESULT_NOT_FILE = "notfile";
	public static final String RESULT_NOT_FILE_TYPE = "notfiletype";
	
	@ApiModelProperty(value="파일명", example="파일명")
	private String fileName;
	@ApiModelProperty(value="파일 다운로드 URL", example="http://.../download/Filename")
    private String fileDownloadUri;
	@ApiModelProperty(value="파일 타입", example="application/octet-stream")
    private String fileType;
	@ApiModelProperty(value="파일 사이즈", example="1060")
    private long size;
	@ApiModelProperty(value="업로드 결과", example="ok, fail, sizeover, notfile, notfiletype")
	private String result;
	@ApiModelProperty(value="업로드 결과 텍스트", example="The file does not exist.")
	private String resultText;
	@ApiModelProperty(value="업로드 결과 리스트")
	private List<?> resultList;
    
    public FileUploadResponse(String fileName, String fileDownloadUri, String fileType, long size, String result, String resultText) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
        this.result = result;
        this.resultText = resultText;
    }
    
    public FileUploadResponse(String fileName, String fileDownloadUri, String fileType, long size, String result) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
        this.result = result;
    }
    
    public FileUploadResponse(String result) {
        this.fileName = null;
        this.fileDownloadUri = null;
        this.fileType = null;
        this.size = 0;
        this.result = result;
    }
}