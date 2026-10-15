package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestInMaster", description = "전자상거래  적하목록수입 마스터리스트")
@Alias("manifestInMaster")
public class ManifestInMaster {
	
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
	@ApiModelProperty(value="신고일자")
	private String simpleDocDate;
	@ApiModelProperty(value="신고시간")
	private String simpleDocTime;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="P/O번호")
	private String poNo;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="Inner Pieces (실제수량, EDI신고용)")
	private int piecesReal;
	@ApiModelProperty(value="단위_수량")
	private int unitPieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="H수량")
	private int housePieces;
	@ApiModelProperty(value="H총중량")
	private double houseGwt;
	@ApiModelProperty(value="H총중량")
	private double houseCwt;
	@ApiModelProperty(value="HCount")
	private int houseCount;
	@ApiModelProperty(value="HCountAll")
	private int houseCountAll;
	@ApiModelProperty(value="CBM")
	private double cbm;
	@ApiModelProperty(value="지정창고전송건수")
	private String cfaSendCnt;
	@ApiModelProperty(value="목록")
	private String cdType1;
	@ApiModelProperty(value="배제")
	private String cdType2;
	@ApiModelProperty(value="일반")
	private String cdType3;
	@ApiModelProperty(value="일간")
	private String cdType4;
	@ApiModelProperty(value="검사_여부")
	private String inspectionYn;
	@ApiModelProperty(value="선별_여부")
	private String seInspectionYn;
	@ApiModelProperty(value="X-RAY_여부")
	private String xrayYn;
	@ApiModelProperty(value="우범화물_여부")
	private String crimeSelectionYn;
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
	@ApiModelProperty(value="전자상거래여부")
	private String ecmYn;
	@ApiModelProperty(value="전자상거래여부Y카운트")
	private String ecmYCnt;
	@ApiModelProperty(value="전자상거래여부N카운트")
	private String ecmNCnt;
	
	@ApiModelProperty(value="제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="목록신고전송상태")
	private String simpleEdiStatus;
	@ApiModelProperty(value="목록신고오류건수")
	private String simpleEdiStatusError;
	@ApiModelProperty(value="M별 제출번호 개수")
	private int simpleDocNoCnt;
	@ApiModelProperty(value="M별 제출번호 개수(제출번호 없는 건 제외)")
	private int simpleDocNoNotNullCnt;
	@ApiModelProperty(value="업무마감여부")
	private String businessFinishYn;
	@ApiModelProperty(value="업무마감처리자")
	private String businessFinishUserName;
	@ApiModelProperty(value="도착항")
	private String destPort;
	
	@ApiModelProperty(value="하기이상건수")
	private String abnormalCnt;
	@ApiModelProperty(value="반입건수")
	private String carryinCnt;
	@ApiModelProperty(value="미반입건수")
	private String notCarryinCnt;
	@ApiModelProperty(value="미반출건수")
	private String notCarryoutCnt;
	@ApiModelProperty(value="반입율")
	private double carryinRate;
	@ApiModelProperty(value="대기건수")
	private int waitCnt;
	@ApiModelProperty(value="Packing No 존재하는 하우스 건수")
	private int packingCnt;
	@ApiModelProperty(value="보세창고")
	private String assignmentWhCode;
	@ApiModelProperty(value="통관접수일자")
	private String clearanceReceiptDate;
	@ApiModelProperty(value="하기장소")
	private String discWhCode;
	@ApiModelProperty(value="거래처명")
	private String customerCode;
	@ApiModelProperty(value="목록결과일자")
	private String simpleDocResultDate;
	@ApiModelProperty(value="통관접수일자")
	private String simpleReportDate;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
}

