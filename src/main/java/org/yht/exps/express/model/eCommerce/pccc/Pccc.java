package org.yht.exps.express.model.eCommerce.pccc;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.exps.express.model.systemMng.Alarm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "Pccc", description = "전자상거래 Pccc")
@Alias("pccc")
@EqualsAndHashCode(callSuper=false)
public class Pccc extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String customsIdJobNo;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="요청ID")
	private String requestId;
	@ApiModelProperty(value="진행상태")
	private String status;
	@ApiModelProperty(value="진행상태_엑셀용")
	private String statusExcel;
	@ApiModelProperty(value="진행상태")
	private String statusName;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="상위업체")
	private String custNameLv1;
	@ApiModelProperty(value="주문일")
	private String poDateTime;
	@ApiModelProperty(value="발송횟수")
	private String sendCnt;
	@ApiModelProperty(value="경과일")
	private String elapseDate;
	@ApiModelProperty(value="운송장번호")
	private String hblNo;
	@ApiModelProperty(value="주문번호")
	private String poNo;
	@ApiModelProperty(value="개인통관고유부호")
	private String origCustomsId;
	@ApiModelProperty(value="소유자명")
	private String origConsigneeName;
	@ApiModelProperty(value="영문(이름)")
	private String origConsigneeFName;
	@ApiModelProperty(value="영문(성)")
	private String origConsigneeLName;
	@ApiModelProperty(value="휴대폰번호")
	private String origConsigneeHp;
	@ApiModelProperty(value="우편번호")
	private String origConsigneeZipCode;
	@ApiModelProperty(value="개인통관고유부호")
	private String afterCustomsId;
	@ApiModelProperty(value="소유자명")
	private String afterConsigneeName;
	@ApiModelProperty(value="영문(이름)")
	private String afterConsigneeFName;
	@ApiModelProperty(value="영문(성)")
	private String afterConsigneeLName;
	@ApiModelProperty(value="휴대폰번호")
	private String afterConsigneeHp;
	@ApiModelProperty(value="우편번호")
	private String afterConsigneeZipCode;
	@ApiModelProperty(value="쿠팡전송시간")
	private String coupangSendDateTime;
	@ApiModelProperty(value="오류코드")
	private String errorCode;
	@ApiModelProperty(value="오류내역")
	private String errorDesc;
	@ApiModelProperty(value="완료일자")
	private String pcccDateTime;
	@ApiModelProperty(value="생성구분")
	private String inputMethod;
	@ApiModelProperty(value="생성구분_엑셀용")
	private String inputMethodExcel;
	@ApiModelProperty(value="아웃콜1")
	private String outCall1;
	@ApiModelProperty(value="아웃콜2")
	private String outCall2;
	@ApiModelProperty(value="통화구분")
	private String outCallType;
	@ApiModelProperty(value="통화구분")
	private String outCallTypeName;
	@ApiModelProperty(value="최근발송일자")
	private String recentSendTime;
	@ApiModelProperty(value="관세사명")
	private String cbCode;
	@ApiModelProperty(value="관세사번호")
	private String cbTel;
	@ApiModelProperty(value="사업유형")
	private String custNameLv2;
	@ApiModelProperty(value="입력자")
	private String registerUserId;	
	@ApiModelProperty(value="입력시간")
	private String registerDateTime;
	@ApiModelProperty(value="갱신자")
	private String updateUserId;
	@ApiModelProperty(value="갱신시간")
	private String updateDateTime;
	@ApiModelProperty(value="날짜형식")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String srchDateFormat;
	
	/* PCCC 합계목록 */
	@ApiModelProperty(value="총건수")
	private int totalCount;
	@ApiModelProperty(value="쿠팡패스건수")
	private int totalPassCount;
	@ApiModelProperty(value="알림톡제외건수")
	private int stopCount;
	@ApiModelProperty(value="수정완료(오류)")
	private int errorCount;
	@ApiModelProperty(value="수정완료(오류)")
	private int totalComplete;
	
	@ApiModelProperty(value="알림톡 List")
	private List<Alarm> alarmList;
	
	/** 알림톡 전송시간 */
	@ApiModelProperty(value="알림톡 전송시간1")
	private String sendTime1;
	@ApiModelProperty(value="알림톡 전송시간2")
	private String sendTime2;
	@ApiModelProperty(value="알림톡 전송시간3")
	private String sendTime3;
	@ApiModelProperty(value="알림톡 전송시간4")
	private String sendTime4;
	@ApiModelProperty(value="알림톡 전송시간5")
	private String sendTime5;
}
