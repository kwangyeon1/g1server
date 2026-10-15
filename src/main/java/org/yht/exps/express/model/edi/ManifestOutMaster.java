package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestOutMaster", description = "전자상거래  적하목록수입 마스터리스트")
@Alias("manifestOutMaster")
public class ManifestOutMaster {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="하우스관리번호")
	private String houseOdJobNo;
	@ApiModelProperty(value="비엘관리번호")
	private String oblJobNo;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="상위업체_이름")
	private String upperCusName;
	@ApiModelProperty(value="bound")
	private String bound;
	@ApiModelProperty(value="boundType")
	private String boundType;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="출항시간")
	private String obTime;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="도착지명")
	private String destName;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="P/O번호")
	private String poNo;
	@ApiModelProperty(value="Inner Pieces (실제수량, EDI신고용)")
	private int piecesReal;
	@ApiModelProperty(value="단위_수량")
	private int unitPieces;
	@ApiModelProperty(value="H수량")
	private int housePieces;
	@ApiModelProperty(value="H총중량")
	private double houseGwt;
	@ApiModelProperty(value="HCount")
	private int houseCount;
	@ApiModelProperty(value="목록")
	private int cdType1Cnt;
	@ApiModelProperty(value="배제")
	private int cdType2Cnt;
	@ApiModelProperty(value="일반")
	private int cdType3Cnt;
	@ApiModelProperty(value="일간")
	private int cdType4Cnt;
	@ApiModelProperty(value="이행")
	private int cdType5Cnt;
	@ApiModelProperty(value="검사_여부")
	private int inspectionYn;
	@ApiModelProperty(value="선별_여부")
	private String seInspectionYn;
	@ApiModelProperty(value="X-RAY_여부")
	private String xrayYn;
	@ApiModelProperty(value="취하_여부")
	private String dropYn;
	@ApiModelProperty(value="일검_여부")
	private String geInspectionYn;
	@ApiModelProperty(value="홀드_여부")
	private String holdYn;
	@ApiModelProperty(value="하기이상")
	private String abnormalityCode;
	@ApiModelProperty(value="검역")
	private String inspectionType1;
	@ApiModelProperty(value="동검")
	private String inspectionType2;
	@ApiModelProperty(value="식검")
	private String inspectionType3;
	@ApiModelProperty(value="목록선별자")
	private String selectorUserName;
	@ApiModelProperty(value="목록선별자")
	private String confirmerUserName;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="비고")
	private String remarks;
	
	@ApiModelProperty(value="제출번호")
	private String simpleDocNo;
	
	@ApiModelProperty(value="전자상거래 건수")
	private int ecmType1Cnt;
	@ApiModelProperty(value="기업견품 건수")
	private int ecmType2Cnt;
	@ApiModelProperty(value="기타 건수")
	private int ecmType3Cnt;
	@ApiModelProperty(value="미수신건수")
	private int ediCnt1;
	@ApiModelProperty(value="오류통보건수 UPS:목록오류통보건수")
	private int ediCnt2;
	@ApiModelProperty(value="이행오류통보건수")
	private int ediCnt3;
	@ApiModelProperty(value="일반미전송")
	private int ediSendCnt1;
	@ApiModelProperty(value="목록미전송")
	private int ediSendCnt2;
	@ApiModelProperty(value="이행미전송")
	private int ediSendCnt3;
	@ApiModelProperty(value="목록수량")
	private int listClearPieces;

	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
}

