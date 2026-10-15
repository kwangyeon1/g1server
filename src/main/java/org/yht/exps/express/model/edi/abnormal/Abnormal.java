package org.yht.exps.express.model.edi.abnormal;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.util.Util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ApiModel(value = "Abnormal", description = "전자상거래  하기이상결과보고신고")
@Alias("abnormal")
public class Abnormal implements Cloneable{
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;	
	@ApiModelProperty(value="적하목록정정_관리번호")
	private String modJobNo;
	@ApiModelProperty(value="일련번호_자동")
	private int seqNoId;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="도착항")
	private String destPort;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="관리번호리스트")
	private List<String> odJobNoList;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="건수")
	private int hblCnt;
	@ApiModelProperty(value="mfn건수")
	private int mfnCnt;
	@ApiModelProperty(value="담당자")
	private String shipperAttn;
	@ApiModelProperty(value="마스터odJobNo")
	private String masterOdJobNo;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="h수량")
	private int hblPieces;
	@ApiModelProperty(value="h수량")
	private int odPieces;
	@ApiModelProperty(value="h중량")
	private double hblGwt;
	@ApiModelProperty(value="외화금액_AMOUNT")
	private double amountCur;
	@ApiModelProperty(value="하기이상_수량")
	private int pieces;
	@ApiModelProperty(value="하기이상_중량")
	private double gwt;
	@ApiModelProperty(value="하기이상_사고개수")
	private int cnt;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="접수통보일시")
	private String lastPodRaqDate;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="하기이상_코드")
	private String abnormalCode;
	@ApiModelProperty(value="하기이상_코드명")
	private String abnormalCodeName;
	@ApiModelProperty(value="장소코드")
	private String discWhCode;
	@ApiModelProperty(value="편명")
	private String vesselFlight;
	@ApiModelProperty(value="기준오더")
	private String mfnOdJobNo;
	
	@ApiModelProperty(value="전송일자")
	private String sendDate;
	@ApiModelProperty(value="신고일자")	// sendDate (신고일자->전송일자) / declarationDate(신고일자) 변경 2025.12.12 2sowell
	private String declarationDate;
	@ApiModelProperty(value="MGN후MFN신고일자")
	private String mgnMfnSendDate;
	@ApiModelProperty(value="MGN신고일자")
	private String mgnSendDate;
	@ApiModelProperty(value="MFN신고일자")
	private String mfnSendDate;
	@ApiModelProperty(value="반입일자")
	private String carryinDate;
	@ApiModelProperty(value="적하정정MRN")
	private String modMrn;
	@ApiModelProperty(value="적하정정수량")
	private String modPieces;
	@ApiModelProperty(value="적하정정중량")
	private String modGwt;
	@ApiModelProperty(value="송하인명")
	private String shipperName;
	@ApiModelProperty(value="송하인전화번호")
	private String shipperTel;
	@ApiModelProperty(value="수하인명")
	private String consigneeName;
	@ApiModelProperty(value="수하인전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="품명")
	private String commodityItem;
	@ApiModelProperty(value="별도관리해제신고일")
	private String unmanagementDate;
	@ApiModelProperty(value="적하목록정정일자")
	private String mfCorrectionDate;
	@ApiModelProperty(value="적하목록정정내용")
	private String mfCorrectionContents;	
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	
	@ApiModelProperty(value="검색타입")
	private String srchNoType;
	@ApiModelProperty(value="검색text")
	private String srchNo;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="반출입구분 리스트(멀티체크)")
	private List<String> srchCarryList;
	@ApiModelProperty(value="통관구분 리스트(멀티체크)")
	private List<String> srchCdTypeList;
	@ApiModelProperty(value="개인통관부호 리스트(멀티체크)")
	private List<String> srchCustomsIdVerifyList;
	@ApiModelProperty(value="거래구분 리스트(멀티체크)")
	private List<String> srchEcmTypeList;
	@ApiModelProperty(value="거래유형 리스트(멀티체크)")
	private List<String> srchEcmBuyTypeList;
	@ApiModelProperty(value="기타조건조회여부", example="Y or N")
	private String srchEtcYn;
	@ApiModelProperty(value="검사여부", example="Y or N")
	private String srchInspectionYn;
	@ApiModelProperty(value="선별대상여부", example="Y or N")
	private String srchSeInspectionYn;	
	@ApiModelProperty(value="X-Ray여부", example="Y or N")
	private String srchXrayYn;
	@ApiModelProperty(value="취하(보류통보)여부", example="Y or N")
	private String srchDropYn;
	@ApiModelProperty(value="하기이상여부", example="Y or N")
	private String srchAbnormalYn;	
	@ApiModelProperty(value="오류통보", example="Y or N")
	private String srchSimpleEdiStatus;	
	@ApiModelProperty(value="수입신고불가", example="Y or N")
	private String srchSimpleStatementYn;
	@ApiModelProperty(value="우범화물여부", example="Y or N")
	private String srchCriminalYn;
	
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="통관_용도구분")
	private String cdUseType;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="정정구분")
	private String modType;
	@ApiModelProperty(value="정정구분명")
	private String modTypeName;
	@ApiModelProperty(value="컨테이너정정구분")
	private String cntrModType;
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNoList;
	@ApiModelProperty(value="사유")
	private String modReason;
	@ApiModelProperty(value="정정구분")
	private String singleModCode;
	@ApiModelProperty(value="정정사유코드")
	private String singleModReasonCode;
	@ApiModelProperty(value="정정전내역")
	private String beforeValue;
	@ApiModelProperty(value="전송상태")
	private String sendYn;
	@ApiModelProperty(value="패킹건수불일치여부")
	private String packingErrorYn;
	@ApiModelProperty(value="전송상태")
	private String abnormalMfnDate;
	@ApiModelProperty(value="전송상태")
	private String abnormalMfnYn;
	@ApiModelProperty(value="중계사업자")	/** 중계사업자 추가*/
	private String venderId1;
	
	@ApiModelProperty(value="항차")
	private String voyage;
	@ApiModelProperty(value="화주_주소")
	private String shipperAddress;
	@ApiModelProperty(value="수하인_주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인_사업자번호")
	private String consigneeBusinessId;
	@ApiModelProperty(value="단위_수량")
	private String unitPieces;

	@ApiModelProperty(value="MGN 여부")
	private String mgnYn;
	@ApiModelProperty(value="MFN 여부")
	private String mfnYn;
	@ApiModelProperty(value="적하삭제 여부")
	private String manifestDelYn;

	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
		
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="HBL조회건수")
	private int totalHbl;
	@ApiModelProperty(value="번호")
	private int num;
	
	@ApiModelProperty(value="선착입항일자")
	private String arOriDate;
	@ApiModelProperty(value="선착편명")
	private String vesselOriFlight;
	@ApiModelProperty(value="기한일 이상건수 표시 여부")
	private String notProcessYn;
	@ApiModelProperty(value="MGN 적하미삭제 및 미반입 기한일")
	private String mgnMarkDate;
	@ApiModelProperty(value="별도관리물품 리스트") //MFN 신고시 별도관리물품 데이터 선착,후착화물을 넣기위한 데이터를 담은 리스트
	private List<Map> mfnOdJobNoList;
	
	@ApiModelProperty(value="noList")	//적하/목록신고(in)화면에서 하우스 여러 건 선택해 무적화물 생성할 경우 각 하우스에 대해 로그 남기기 위해 선택한 하우스 리스트의 hblNo, odJobNo 함께 전달
	private List<Map> noList;			//ex) [{odJobNo=COD220429-00000001, hblNo=20220428_H2, SEQ=0}, {odJobNo=COD220429-00000002, hblNo=20220428_H3, SEQ=1}]

	@ApiModelProperty(value="무적여부")
	private String noCargoYn;
	@ApiModelProperty(value="발송국가")
	private String origCountry;

	@ApiModelProperty(value="경과일자")
	private String elapseDate;
	@ApiModelProperty(value="미착MGN")
	private String sendMgnMbl;
	@ApiModelProperty(value="무적MFN신고 MBL")
	private String noCargoMfnMbl;
	@ApiModelProperty(value="적하추가")
	private String manifestAdd;
	@ApiModelProperty(value="내용")
	private String resultDetail;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="반입여부")
	private String inYn;
	@ApiModelProperty(value="MGN접수통보여부")
	private String mgnRaqYn;
	@ApiModelProperty(value="MFN접수통보여부")
	private String mfnRaqYn;
	@ApiModelProperty(value="MGN아닌것들여부")
	private String notMgnCodeYn;
	@ApiModelProperty(value="MGN아닌것들접수통보여부")
	private String notMgnCodeAllRaqYn;
	@ApiModelProperty(value="MRN 원본")
	private String mrnOrg;
	@ApiModelProperty(value="h수량 원본")
	private int hblPiecesOrg;
	@ApiModelProperty(value="h중량 원본")
	private double hblGwtOrg;
	@ApiModelProperty(value="하기이상_수량 원본")
	private int piecesOrg;
	@ApiModelProperty(value="하기이상_중량 원본")
	private double gwtOrg;
	@ApiModelProperty(value="하기이상_사고개수 원본")
	private int cntOrg;

	public Abnormal clone() throws CloneNotSupportedException{
		Abnormal vo = null;
		try {
			vo = (Abnormal) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(Util.getPrintStacTraceString(e));
			throw e;
		}
		return vo;
	}
}


