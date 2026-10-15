package org.yht.exps.express.model.edi;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestCorrect", description = "전자상거래  적하정정")
@Alias("manifestCorrect")
public class ManifestCorrect {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	
	@ApiModelProperty(value="마스터존재여부")
	private String masterYn;
	
	@ApiModelProperty(value="도입사계약일자")
	private String contractDate;
	
	@ApiModelProperty(value="blTypeIert")
	private String blTypeIert;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="하우스관리번호리스트")
	private List<String> odJobNoList;
	@ApiModelProperty(value="정정구분")
	private String modType;
	@ApiModelProperty(value="정정구분명")
	private String modTypeName;
	@ApiModelProperty(value="정정사유")
	private String modReason;
	@ApiModelProperty(value="단일항목정정_코드")
	private String singleModCode;
	@ApiModelProperty(value="단일항목정정_코드명")
	private String singleModName;
	@ApiModelProperty(value="단일항목정정_사유코드")
	private String singleModReasonCode;
	@ApiModelProperty(value="단일항목정정_사유코드명")
	private String singleModReasonName;
	@ApiModelProperty(value="정정구분")
	private List<String> singleModCodeList;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="H건수")
	private int hblCnt;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="정정번호")
	private String modSerialNo;
	@ApiModelProperty(value="정정신청번호")
	private String modReqNo;
	@ApiModelProperty(value="신청일자")
	private String modReqDate;
	@ApiModelProperty(value="신청시간")
	private String modReqTime;
	@ApiModelProperty(value="세관코드")
	private String customsCode;
	@ApiModelProperty(value="세관과코드")
	private String customsDptCode;
	@ApiModelProperty(value="전송일자/시간")
	private String sendDateTime;
	@ApiModelProperty(value="접수/오류일자 시간")
	private String recvDateTime;
	@ApiModelProperty(value="결과일자/시간")
	private String resultDateTime;
	@ApiModelProperty(value="적하목록정정_관리번호")
	private String modJobNo;
	@ApiModelProperty(value="적하목록정정_카피관리번호")
	private String copyModJobNo;
	@ApiModelProperty(value="일련번호")
	private int seqNoId;
	@ApiModelProperty(value="container일련번호")
	private int cntrSeqNoId;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="정정전내역")
	private String beforeValue;
	@ApiModelProperty(value="정정후내역")
	private String afterValue;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자코드")
	private String registerUserName;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="송신상태")
	private String sendStatus;
	@ApiModelProperty(value="수신상태")
	private String recvStatus;
	
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNo;
	@ApiModelProperty(value="수정전 컨테이너번호")
	private String origCntrNo;
	@ApiModelProperty(value="컨테이너정정구분_코드(I: B/L 추가, A:추가,U:정정, D:삭제)")
	private String cntrModType;
	@ApiModelProperty(value="컨테이너정정_코드")
	private String cntrModCode;
	@ApiModelProperty(value="컨테이너정정_코드명")
	private String cntrModName;
	@ApiModelProperty(value="컨테이너타입")
	private String cntrCode;
	@ApiModelProperty(value="컨테이너EDI코드")
	private String cntrEdiCode;
	
	@ApiModelProperty(value="봉인번호")
	private String cntrSealNo1;
	@ApiModelProperty(value="봉인번호")
	private String cntrSealNo2;
	@ApiModelProperty(value="봉인번호")
	private String cntrSealNo3;
	@ApiModelProperty(value="컨테이너수량")
	private int cntrPieces;
	@ApiModelProperty(value="컨테이너수량단위")
	private String cntrUnitPieces;
	@ApiModelProperty(value="컨테이너중량")
	private double cntrGwt;
	@ApiModelProperty(value="컨테이너CBM")
	private double cntrCbm;
	
	@ApiModelProperty(value="SOLUTION")
	private String solution;
	@ApiModelProperty(value="바운드")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="목적항")
	private String destPort;
	@ApiModelProperty(value="목적항명")
	private String destPortName;
	@ApiModelProperty(value="도착지")
	private String destCode;
	@ApiModelProperty(value="도착지명")
	private String destCodeName;
	@ApiModelProperty(value="도착지국가")
	private String destCountry;
	@ApiModelProperty(value="패킹건수불일치여부")
	private String packingErrorYn;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="단위_수량")
	private String unitPieces;
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="용적")
	private double cbm;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
	@ApiModelProperty(value="배정코드")
	private String assignmentCode;
	@ApiModelProperty(value="배정코드명")
	private String assignmentName;
	@ApiModelProperty(value="화주코드")
	private String shipperCode;
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;
	@ApiModelProperty(value="화주_주소")
	private String shipperAddress;
	@ApiModelProperty(value="화주_주소_A")
	private String shipperAddressA;
	@ApiModelProperty(value="화주_주소_B")
	private String shipperAddressB;
	@ApiModelProperty(value="화주_주소_C")
	private String shipperAddressC;
	@ApiModelProperty(value="화주_주소_D")
	private String shipperAddressD;
	@ApiModelProperty(value="화주_전화번호")
	private String shipperTel;
	@ApiModelProperty(value="화주_우편번호")
	private String shipperZipcode;
	@ApiModelProperty(value="화주_국가코드")
	private String shipperCountryCode;
	@ApiModelProperty(value="화주STATE_코드")
	private String shipperStateCode;
	@ApiModelProperty(value="화주PLACE_코드")
	private String shipperPlaceCode;
	@ApiModelProperty(value="화주PLACE")
	private String shipperPlace;
	@ApiModelProperty(value="화주STREET")
	private String shipperStreet;
	@ApiModelProperty(value="화주_사업자번호")
	private String shipperBusinessId;
	@ApiModelProperty(value="화주_담당자_ID")
	private String shipperAttnId;
	@ApiModelProperty(value="화주_담당자")
	private String shipperAttn;
	@ApiModelProperty(value="화주_팩스번호")
	private String shipperFax;
	@ApiModelProperty(value="수하인_코드")
	private String consigneeCode;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인_주소_A")
	private String consigneeAddressA;
	@ApiModelProperty(value="수하인_주소_B")
	private String consigneeAddressB;
	@ApiModelProperty(value="수하인_주소_C")
	private String consigneeAddressC;
	@ApiModelProperty(value="수하인_주소_D")
	private String consigneeAddressD;
	@ApiModelProperty(value="수하인_핸드폰")
	private String consigneeHp;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수하인_우편번호")
	private String consigneeZipcode;
	@ApiModelProperty(value="수하인_국가코드")
	private String consigneeCountryCode;
	@ApiModelProperty(value="수하인STATE_코드")
	private String consigneeStateCode;
	@ApiModelProperty(value="수하인PLACE_코드")
	private String consigneePlaceCode;
	@ApiModelProperty(value="수하인PLACE")
	private String consigneePlace;
	@ApiModelProperty(value="수하인STREET")
	private String consigneeStreet;
	@ApiModelProperty(value="수하인_사업자번호")
	private String consigneeBusinessId;
	@ApiModelProperty(value="수하인_담당자_ID")
	private String consigneeAttnId;
	@ApiModelProperty(value="수하인_담당자")
	private String consigneeAttn;
	@ApiModelProperty(value="수하인_팩스번호")
	private String consigneeFax;
	@ApiModelProperty(value="수하인_명칭_한글")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인_주소_한글")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="결과통보상태")
	private String resultStatus;
	@ApiModelProperty(value="하기장소 ")
	private String discWhCode;
	@ApiModelProperty(value="하기물품코드")
	private String discItemCode;
	@ApiModelProperty(value="구분")
	private String gubun;
	@ApiModelProperty(value="사용자 언어")
	private String userLanguage;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="전자상거래Y/N")
	private String ecmYn;
	@ApiModelProperty(value="컨테이너리스트")
	private List<Map<String, Object>> containerList;
	@ApiModelProperty(value="엑셀업로드 리스트")
	private List<ManifestOrder> odList;	
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="조회일자타입", example="23")
	private String dateFormat;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

