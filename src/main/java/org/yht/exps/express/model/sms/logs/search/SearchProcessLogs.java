package org.yht.exps.express.model.sms.logs.search;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchElastic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchProcessLogs", description = "전자상거래 엑셀 업로드 결과 리스트 검색 조건")
@Alias("searchProcessLogs")
public class SearchProcessLogs extends SearchElastic {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="처리종류", example="DATA, CLOSING, ACCESS...")
	private String srchLogJobType;
	@ApiModelProperty(value="JOB 기능구분", example="EXCEL, INSERT, UPDATE, DELETE, LOGIN, LOGOUT")
	private String srchJobFuncType;
	@ApiModelProperty(value="로그 간단 요약")
	private String srchDescription;
	@ApiModelProperty(value="로그상세")
	private String srchJobLog;
	@ApiModelProperty(value="처리자 ID")
	private String srchProcessUserId;
	@ApiModelProperty(value="업무")
	private String srchSolution;
	@ApiModelProperty(value="해상/항공")
	private String srchBound;
	@ApiModelProperty(value="수출입")
	private String srchBoundType;
	@ApiModelProperty(value="Job No")
	private String srchJobNo;
	@ApiModelProperty(value="Job No2")
	private String srchJobNo2;

	@ApiModelProperty(value="삭제:검색건(srchData)/선택건(chkData)")
	private String targetData;
	@ApiModelProperty(value="삭제:선택건 리스트")
	private List<Map<String, Object>> checkDataList;
	@ApiModelProperty(value="검색건 삭제 시 검색데이터 건수")
	private int srchDataCnt;
}
