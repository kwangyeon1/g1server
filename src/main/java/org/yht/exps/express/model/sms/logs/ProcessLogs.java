package org.yht.exps.express.model.sms.logs;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ProcessLogs", description = "프로세스 처리 로그")
@Alias("processLogs")
public class ProcessLogs {
	@ApiModelProperty(value="ES Index", example="ecm-process-logs-20210706")
	private String index;
	@ApiModelProperty(value="내부관리번호", example="")
	private String referenceNoId;
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String entBusinessCode;
	@ApiModelProperty(value="사업장명", example="YHTXX-KR")
	private String entBusinessPrintName;
	@ApiModelProperty(value="업무", example="COM, SMS, FMS..")
	private String solution;
	@ApiModelProperty(value="해상/항공", example="COM, AIR, SEA")
	private String bound;
	@ApiModelProperty(value="수출입", example="COM, IMP, EXP")
	private String boundType;
	@ApiModelProperty(value="처리자 ID")
	private String processUserId;
	@ApiModelProperty(value="처리자명")
	private String processUserName;
	@ApiModelProperty(value="처리자명_영문")
	private String processUserNameEng;	
	@ApiModelProperty(value="처리일시")
	private long processDateTime;
	@ApiModelProperty(value="처리종류", example="DATA : DATA CHANGE(자료변경시), CLOSING : ADMIN CLOSING(마감일자변경), ACCESS : LOGIN & LOGOUT(프로그램 로그인/아웃), USER : EMPLOYEE INFORMATION(사용자정보 변경), CUSTOMER :  CUSTOMER INFORMATION(고객정보 변경)")
	private String logJobType;
	@ApiModelProperty(value="JOB 기능구분", example="EXCEL, INSERT, UPDATE, DELETE, LOGIN, LOGOUT")
	private String jobFuncType;
	@ApiModelProperty(value="Job No")
	private String jobNo;
	@ApiModelProperty(value="로그 간단 요약")
	private String description;
	@ApiModelProperty(value="로그")
	private String jobLog;
	/* 파일데이터로그 */
	private String fileData;
	@ApiModelProperty(value="접속 IP")
	private String accessIp;
	@ApiModelProperty(value="처리결과", example="ok, fail")
	private String result;
	@ApiModelProperty(value="테이블구분")
	private String DbType;

	@ApiModelProperty(value="문서유형", example="Order, Delivery, Broker")
	private String excelType;
	
	@ApiModelProperty(value="job번호")
	private String esJobNo;
	@ApiModelProperty(value="업무유형")
	private String ifDocName;
	@ApiModelProperty(value="처리자 ID")
	private String registerUserId;
	@ApiModelProperty(value="처리일시")
	private long registerDateTime;
}
