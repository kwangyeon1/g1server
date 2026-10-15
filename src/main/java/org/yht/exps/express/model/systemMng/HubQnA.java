package org.yht.exps.express.model.systemMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.util.CustomErrorType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "HubQnA", description = "eHub 문의사항 저장용")
@Alias("hubQnA")
public class HubQnA extends ResultList{
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="도입사사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="관리번호")
	private String referenceNo;
	@ApiModelProperty(value="거래처코드")
	private String customerCode;
	@ApiModelProperty(value="거래처명")
	private String customerName;

	@ApiModelProperty(value="오더번호")
	private String poNo;
	@ApiModelProperty(value="H.B/L No")
	private String hblNo;
	@ApiModelProperty(value="문의자 구분", example="(C:거래처 , S:세중직원)")
	private String questionUserType;
	@ApiModelProperty(value="문의유형", example="(A1,B1,C1,D1)")
	private String questionType;
	@ApiModelProperty(value="문의유형명", example="(A1:시스템 기능/오류,B1:상품관련 문의..)")
	private String questionTypeName;
	@ApiModelProperty(value="중요도")
	private String emergencyYn;
	@ApiModelProperty(value="완료여부")
	private String completeYnh;
	@ApiModelProperty(value="홀드사유")
	private String holdReason;
	@ApiModelProperty(value="제목")
	private String title;
	@ApiModelProperty(value="내용")
	private String contents;
	@ApiModelProperty(value="메인 내용")
	private String mainContents;
	
	@ApiModelProperty(value="일련번호")
	private int seqNo;
	@ApiModelProperty(value="질문의 SEQ_NO")
	private int questionSeqNo;
	@ApiModelProperty(value="유형", example="(Q:질문, A:대답)")
	private String qandaType;
	
	@ApiModelProperty(value="파일관리번호")
	private String fileReferenceNoId;
	@ApiModelProperty(value="파일일련번호")
	private String fileSeqNo;
	@ApiModelProperty(value="파일타입")
	private String fileContentType;
	@ApiModelProperty(value="파일경로")
	private String filePath;
	@ApiModelProperty(value="파일명(임시)")
	private String fileTempName;
	@ApiModelProperty(value="파일명(원본명)")
	private String fileOriginalName;
	@ApiModelProperty(value="파일명(원본명)")
	private String fileDeleteYn;
	@ApiModelProperty(value="파일확장자")
	private String fileExt;
	@ApiModelProperty(value="파일공유여부")
	private String fileShare;

	@ApiModelProperty(value="화주_명칭")
	private String shipperName;
	@ApiModelProperty(value="화주_전화번호")
	private String shipperTel;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인_핸드폰")
	private String consigneeHp;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="아이템 정보", example="품명/수량")
	private String itemInfo;

	@ApiModelProperty(value="이미지파일")
	private Object imageFile;
	@ApiModelProperty(value="파일명")
	private String fileName;
	@ApiModelProperty(value="답변일시")
	private String answerDateTime;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="수정자")
	private String updateUserName;
	
	@ApiModelProperty(value="저장타입")
	private String 	action;
	@ApiModelProperty(value="입력자ID")
	private String 	registerUserId;
	@ApiModelProperty(value="수정자ID")
	private String 	updateUserId;
	@ApiModelProperty(value="시간")
	private String 	gmtTime;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	
	@ApiModelProperty(value="화주코드")
	private String custCodeLv1;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
	
	@ApiModelProperty(value="파일타입")
	private String origFileContentType;
	@ApiModelProperty(value="파일경로")
	private String origFilePath;
	@ApiModelProperty(value="파일명(임시)")
	private String origFileTempName;
	@ApiModelProperty(value="파일명(원본명)")
	private String origFileOriginalName;
}
