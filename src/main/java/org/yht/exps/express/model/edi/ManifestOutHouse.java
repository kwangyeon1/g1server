package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestOutHouse", description = "전자상거래  적하목록수출 하우스 리스트")
@Alias("manifestOutHouse")
public class ManifestOutHouse {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="마스터관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="과태료")
	private String penaltyYn;
	@ApiModelProperty(value="기적여부")
	private String loadCmplYn;
	@ApiModelProperty(value="제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="목록신고일")
	private String simpleReportDate;
	@ApiModelProperty(value="목록접수일")
	private String simpleResponseDate;
	@ApiModelProperty(value="목록신고일")
	private String simpleDocDate;
	@ApiModelProperty(value="접수통보일")
	private String clearanceReceiptDate;
	@ApiModelProperty(value="이행신고일")
	private String simpleDocDateLicense;
	@ApiModelProperty(value="이행신고일시")
	private String simpleReportDateLicense;
	@ApiModelProperty(value="이행신고수신일")
	private String simpleResponseDateLicense;
	@ApiModelProperty(value="이행신고상태")
	private String simpleEdiStatusLicense;
	
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="통관구분")
	private String cdType;
	@ApiModelProperty(value="용도구분")
	private String cdUseType;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="검사_여부")
	private String inspectionYn;
	@ApiModelProperty(value="선별_여부")
	private String seInspectionYn;
	@ApiModelProperty(value="X-RAY_여부")
	private String xrayYn;
	@ApiModelProperty(value="취하_여부")
	private String dropYn;
	@ApiModelProperty(value="일검_여부")
	private String geInspectionYn;
	@ApiModelProperty(value="홀드_여부")
	private String holdReleaseYn;
	@ApiModelProperty(value="하기이상")
	private String abnormalityCode;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="Inner Pieces (실제수량, EDI신고용)")
	private int piecesReal;
	@ApiModelProperty(value="단위_수량")
	private int unitPieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="원화금액_AMOUNT")
	private double amountLoc;
	@ApiModelProperty(value="외화금액_AMOUNT")
	private double amountCur;
	
	@ApiModelProperty(value="목적항")
	private String destPort;
	@ApiModelProperty(value="허용품목코드")
	private String allowedItemCode;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
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
	@ApiModelProperty(value="수하인_명칭_한글")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인_주소_한글")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수하인_사업자번호")
	private String consigneeBusinessId;
	@ApiModelProperty(value="통관_ID_구분")
	private String customsIdType;
	@ApiModelProperty(value="통관_ID")
	private String customsId;
	@ApiModelProperty(value="통관부호_검증상태")
	private String customsIdVerify;
	@ApiModelProperty(value="통관부호_오류내역")
	private String customsIdError;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="출발국")
	private String origCountry;
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
	@ApiModelProperty(value="화주_사업자번호")
	private String shipperBusinessId;
	@ApiModelProperty(value="홈페이지")
	private String url;
	@ApiModelProperty(value="검사")
	private String inspectionResultYn;
	@ApiModelProperty(value="AMS")
	private String amsYn;
	@ApiModelProperty(value="검사결과")
	private String inspectionResutCode;
	@ApiModelProperty(value="신고처리상태")
	private String simpleDeclarationStatus;
	@ApiModelProperty(value="취하코드")
	private String dropCode;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="운송사코드")
	private String trCode;
	@ApiModelProperty(value="운송사_명칭")
	private String trName;
	@ApiModelProperty(value="배송사_코드")
	private String deliveryCode;
	@ApiModelProperty(value="배송사_명")
	private String deliveryName;
	@ApiModelProperty(value="관세사_코드")
	private String cbCode;
	@ApiModelProperty(value="관세사_명칭")
	private String cbName;
	@ApiModelProperty(value="FileShare유무")
	private String fileShare;
	@ApiModelProperty(value="유효기간시작일")
	private String customsIdStartDate;
	@ApiModelProperty(value="유효기간만료일")
	private String customsIdEndDate;
	
	@ApiModelProperty(value="수신결과")
	private String revResult;
	@ApiModelProperty(value="면장수량")
	private int licensePieces;
	@ApiModelProperty(value="면장중량")
	private double licenseGwt;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
	
}

