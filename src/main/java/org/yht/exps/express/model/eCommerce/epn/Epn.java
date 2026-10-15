package org.yht.exps.express.model.eCommerce.epn;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Epn", description = "전자상거래 EPN 리스트")
@Alias("epn")
public class Epn extends ResultList{
	@ApiModelProperty(value="관리번호")
	private String epnJobNo;
	@ApiModelProperty(value="관리번호리스트")
	private List<String> epnJobNoList;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="H.B/L NO")
	private String hblNo;
	@ApiModelProperty(value="통관구분")
	private String clearanceType;
	@ApiModelProperty(value="신고번호")
	private String licenseNo;
	@ApiModelProperty(value="수량")
	private double pieces;
	@ApiModelProperty(value="수량단위")
	private String unitPieces;
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="분할여부")
	private String splitYn;
	@ApiModelProperty(value="분할차수")
	private double splitSeq;
	@ApiModelProperty(value="동시포장코드")
	private String packingCode;
	@ApiModelProperty(value="동시포장갯수")
	private double packingPieces;
	@ApiModelProperty(value="수출자")
	private String shipperName;
	@ApiModelProperty(value="반입일자")
	private String carryinDate;
	@ApiModelProperty(value="신고일자")
	private String sendDateTime;
	@ApiModelProperty(value="사업자번호")
	private String shipperBusinessId;
	@ApiModelProperty(value="구매자")
	private String consigneeName;
	@ApiModelProperty(value="제조자")
	private String makerName;
	@ApiModelProperty(value="목적국가코드")
	private String destCountry;
	@ApiModelProperty(value="신고인기제란")
	private String licenseRemark;
	@ApiModelProperty(value="긴급여부")
	private String emergencyYn;
	@ApiModelProperty(value="세번부호")
	private String hsCode;
	@ApiModelProperty(value="품명")
	private String itemName;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="거래품명")
	private String ecmItemName;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="조회신청")
	private String checkRequest;
	@ApiModelProperty(value="조회결과")
	private String checkResult;
	@ApiModelProperty(value="검사요청")
	private String inspectionRequest;
	@ApiModelProperty(value="검사결과")
	private String inspectionResult;
	@ApiModelProperty(value="신고금액(CUR)")
	private double itemAmountCur;
	@ApiModelProperty(value="신고금액")
	private double itemAmountLoc;
	@ApiModelProperty(value="총신고금액(CUR)")
	private double declarationAmountCur;
	@ApiModelProperty(value="총신고금액")
	private double declarationAmountLoc;
	@ApiModelProperty(value="운임")
	private double itemDeliveryFee;
	@ApiModelProperty(value="보험료")
	private double itemInsurance;
	@ApiModelProperty(value="결제금액")
	private double payAmount;
	@ApiModelProperty(value="관세사접수일시")
	private String cbReceiptDateTime;
	@ApiModelProperty(value="관세사수리일시")
	private String cbRecvDateTime;
	@ApiModelProperty(value="관세사이관")
	private String cbTsYn;
	@ApiModelProperty(value="적하목록이관")
	private String mfTsYn;
	@ApiModelProperty(value="이관등록정보")
	private String tsRegisterInfo;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
    @ApiModelProperty(value="관리번호")
    private String odJobNo;
    @ApiModelProperty(value="오더 생성일자")
    private String odRegisterDateTime;
	
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="SOLUTION")
	private String solution;
	@ApiModelProperty(value="stockType")
	private String stockType;
	@ApiModelProperty(value="stockSign")
	private String stockSign;
	@ApiModelProperty(value="stockSeqLength")
	private String stockSeqLength;
	@ApiModelProperty(value="resultCode")
	private String resultCode;
	@ApiModelProperty(value="stockRemark")
	private String stockRemark;
	@ApiModelProperty(value="subSeq")
	private String subSeq;
	@ApiModelProperty(value="seqNo")
	private String seqNo;
	@ApiModelProperty(value="stockDateFormat")
	private String stockDateFormat;
	@ApiModelProperty(value="dateLength")
	private String dateLength;
	@ApiModelProperty(value="stockDateStr")
	private String stockDateStr;
	@ApiModelProperty(value="signType")
	private String signType;
	@ApiModelProperty(value="appendStockSign")
	private String appendStockSign;
    @ApiModelProperty(value="출항일자")
    private String obDate;
}
