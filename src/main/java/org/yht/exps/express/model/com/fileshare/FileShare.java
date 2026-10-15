package org.yht.exps.express.model.com.fileshare;

import java.util.List;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "fileShare", description = "업로드 파일")
@Alias("fileShare")
public class FileShare {
	@ApiModelProperty(value="관리번호_제번")
	private int referenceNoId;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="도입사사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="solution")
	private String solution;
	@ApiModelProperty(value="solution_바운드")
	private String bound;
	@ApiModelProperty(value="solution_바운드구분")
	private String boundType;
	@ApiModelProperty(value="기능구분")
	private String funcType;
	@ApiModelProperty(value="기능세부구분")
	private String funcSubType;
	@ApiModelProperty(value="기능참조job번호")
	private String funcJobNo;
	@ApiModelProperty(value="기능참조번호")
	private String funcReferenceNo;
	@ApiModelProperty(value="제목")
	private String subject;
	@ApiModelProperty(value="본문")
	private String memo;
	@ApiModelProperty(value="파일공유번호")
	private String fileShareNo;
	@ApiModelProperty(value="파일문서구분")
	private String fileDocType;
	@ApiModelProperty(value="파일명칭")
	private String fileName;
	@ApiModelProperty(value="파일확장자")
	private String fileExt;
	@ApiModelProperty(value="파일크기")
	private long fileSize;
	@ApiModelProperty(value="파일명_원격지")
	private String fileNameRemote;
	@ApiModelProperty(value="파일경로_원격지")
	private String filePathRemote;
	@ApiModelProperty(value="공유여부")
	private String shareYn;
	@ApiModelProperty(value="공유여부_전사업장")
	private String shareBusinessYn;
	@ApiModelProperty(value="공유여부_화주")
	private String shareCustomerYn;
	@ApiModelProperty(value="공유여부_agent")
	private String sharePartnerYn;
	@ApiModelProperty(value="마감_확인여부")
	private String finishCheckYn;
	@ApiModelProperty(value="마감_여부")
	private String finishYn;
	@ApiModelProperty(value="마감_일자")
	private String finishDate;
	@ApiModelProperty(value="마감_시간")
	private String finishTime;
	@ApiModelProperty(value="송화인명")
	private String shipperName;
	@ApiModelProperty(value="메모일자")
	private String memoDate;
	@ApiModelProperty(value="파일공유방법 :  web, ftp")
	private String fsServerType;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="입력자")
	private String registerUserId;
	@ApiModelProperty(value="수정자")
	private String updateUserId;
	
	@ApiModelProperty(value="멀티파일여부")
	private String multipleYn;
	
	@ApiModelProperty(value="사용가능 파일확장자")
	private String fileExtension;
	@ApiModelProperty(value="파일삭제여부")
	private String removeFileYn;
	@ApiModelProperty(value="파일업로드여부")
	private boolean fileUploadCheck;
	
	@ApiModelProperty(value="시간")
	private String 	gmtTime;
	
	List<FileShareFile> fileShareList;
}
