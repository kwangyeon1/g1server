package org.yht.exps.express.model.edi.abnormal;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AbnormalList", description = "전자상거래  하기이상결과보고신고 리스트")
@Alias("abnormalList")
public class AbnormalList {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;	
	
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="하기이상결과보고신고 리스트")
	private List<Abnormal> abnormalList;
	@ApiModelProperty(value="하기이상결과보고신고 삭제 리스트")
	private List<Abnormal> editDelList;
	@ApiModelProperty(value="하기이상결과보고신고 추가 팝업 신고 그리드")
	private List<Abnormal> alrAbnormalList;

	@ApiModelProperty(value="하기이상결과보고신고 추가 팝업 마스터 그리드")
	private List<Map<String, Object>> mrnList;
	@ApiModelProperty(value="엑셀 업로드 리스트")
	private List<Map<String, Object>> dataList;
	@ApiModelProperty(value="컨테이너리스트")
	private List<Map<String, Object>> containerList;
	@ApiModelProperty(value="다중 BL 번호 리스트")
	private List<String> blNoList;
	
	@ApiModelProperty(value="바운드", example="AIR OR SEA")
	private String bound;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;	
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자코드")
	private String registerUserName;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="정정구분")
	private String modType;
	@ApiModelProperty(value="컨테이너정정구분")
	private String cntrModType;
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNo;
	@ApiModelProperty(value="정정관리번호")
	private String modJobNo;
	@ApiModelProperty(value="사유")
	private String modReason;
	@ApiModelProperty(value="정정구분")
	private String singleModCode;
	@ApiModelProperty(value="정정구분")
	private List<String> singleModCodeList;
	@ApiModelProperty(value="정정사유코드")
	private String singleModReasonCode;
	@ApiModelProperty(value="기준오더")
	private String mfnOdJobNo;
	@ApiModelProperty(value="SOLUTION")
	private String solution;
	@ApiModelProperty(value="하기장소 ")
	private String discWhCode;
	@ApiModelProperty(value="하기물품코드")
	private String discItemCode;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;

	@ApiModelProperty(value="적하삭제여부")
	private String manifestDelYn;

	@ApiModelProperty(value="저장 하기이상코드")
	private String applyAbnormalCode;

	@ApiModelProperty(value="에러 메세지")
	private String errResultMsg;

	@ApiModelProperty(value="마스터 MRN")
	private String masterMrn;

	@ApiModelProperty(value="무적MFN 추가 여부")
	private String noCargoMfnYn;
	
	@ApiModelProperty(value="마스터 MRN 여러개 조회 여부")
	private String masterMrnsYn;
}
