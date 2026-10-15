package org.yht.exps.express.model.edi;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SendManifest", description = "적하,통관목록신고")
@Alias("sendManifest")
public class SendManifest {
	
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;
	@ApiModelProperty(value="SOLUTION")
	private String solution;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="마스터오더관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="적하목록정정관리번호리스트")
	private List<String> modJobNoList;
	@ApiModelProperty(value="적하목록정정관리번호")
	private String modJobNo;
	@ApiModelProperty(value="진행상태코드")
	private String podStatus;
	@ApiModelProperty(value="마스터오더관리번호리스트")
	private List<String> masterOdJobNoList;
	@ApiModelProperty(value="하우스오더관리번호리스트")
	private List<String> houseOdJobNoList;
	@ApiModelProperty(value="작성자아이디")
	private String registerUserId;
	@ApiModelProperty(value="작성자명")
	private String registerUserName;
	@ApiModelProperty(value="수정자아이디")
	private String updateUserId;
	@ApiModelProperty(value="GMT")
	private String gmtTime;
	@ApiModelProperty(value="도입사계약일자")
	private String contractDate;
	@ApiModelProperty(value="사용자확인여부")
	private String userConfirmYn;
	@ApiModelProperty(value="전송확인여부")
	private String confirmYn;
	@ApiModelProperty(value="관세청전송결과코드")
	private String customsErrCode;
	@ApiModelProperty(value="취소여부")
	private String cancelYn;
	@ApiModelProperty(value="중계업체")
	private String ediVender;
	@ApiModelProperty(value="과태료여부")
	private String penaltyYn;
	@ApiModelProperty(value="실행타입")
	private String action;
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
	@ApiModelProperty(value="목록신고제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="목록신고제출일자")
	private String simpleDocDate;
	@ApiModelProperty(value="제출번호 리스트")	//통관목록신고-서브그리드에서 n건 선택한 경우
	private List<String> simpleDocNoList;
	@ApiModelProperty(value="서브그리드 여부")
	private String subGridYn;
	@ApiModelProperty(value="오더조회 화면 여부")
	private String orderDetYn;
	@ApiModelProperty(value="DOC 여부")
	private String documentYn;
	@ApiModelProperty(value="도착항")
	private String destPort;
	@ApiModelProperty(value="분리할 제출번호")
	private String newSimpleDocNo;
	@ApiModelProperty(value="출발지")
	private String origCode;
	@ApiModelProperty(value="통관목록취하차수")
	private String simpleDropCnt;
	@ApiModelProperty(value="통관목록취하 제출일자")
	private String simpleDropTime;
	@ApiModelProperty(value="통관목록취하 사유")
	private String simpleDropEtc;
	@ApiModelProperty(value="통관목록취하 코드")
	private String simpleDropCode;
	
	
	@ApiModelProperty(value="M별 제출번호 리스트")
	private Map<String, List<String>> modSimpleDocList;
	@ApiModelProperty(value="하우스번호리스트")
	private List<String> hblNoList;
	@ApiModelProperty(value="마스터리스트")	//통관목록오류건재전송에서 마스터 n건 선택한 경우(UPS)
	private Map<String, Object> masterInfo;
	@ApiModelProperty(value = "반입예정 리스트")
    private List<Map<String, Object>> bmsInPlanList;

	@ApiModelProperty(value="오더조회시작번호")
	private int startNum;
	@ApiModelProperty(value="하우스 개수")	//마스터 선택 시 H.Cnt, 하우스 선택 시 체크한 하우스리스트의 길이 받아옴
	private int houseCount;
	
	@ApiModelProperty(value="mblNo")
	private String mblNo;
	
    @ApiModelProperty(value="mblNo")
    private String manifestDkxYn;
	
	@ApiModelProperty(value="지정창고전송테스트")
	private Map<String, Object> gwTest;
	@ApiModelProperty(value="dataList")
	private List<Map<String, Object>> dataList;
}

