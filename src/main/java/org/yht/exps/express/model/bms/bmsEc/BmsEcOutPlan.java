package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "BmsEcOutPlan", description = "EC 반출승인")
@Alias("bmsEcOutPlan")
public class BmsEcOutPlan extends ResultList {
    @ApiModelProperty(value="인터페이스ID")
    private String interfaceId;
    @ApiModelProperty(value="인터페이스SEQ")
    private Integer interfaceSeq;
    @ApiModelProperty(value="CUD")
    private String isCud;
    @ApiModelProperty(value="제공일련번호")
    private String seqNo;
    @ApiModelProperty(value="반출승인근거번호")
    private String outOkNo;
    @ApiModelProperty(value="반출승인취하구분")
    private String outOkGbn;
    @ApiModelProperty(value="관세청통보일시")
    private String cusSndDt;
    @ApiModelProperty(value="반출승인취하일자")
    private String outOkDt;
    @ApiModelProperty(value="보세구역")
    private String bondBuho;
    @ApiModelProperty(value="반출승인유형")
    private String outOkType;
    @ApiModelProperty(value="검사대상여부")
    private String watchYn;
    @ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNo;
    @ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNoDisp;
    @ApiModelProperty(value="MRN")
    private String mrn;
    @ApiModelProperty(value="MSN")
    private String msn;
    @ApiModelProperty(value="HSN")
    private String hsn;
    @ApiModelProperty(value="관세액")
    private Double duty;
    @ApiModelProperty(value="인도조건")
    private String cifCond;
    @ApiModelProperty(value="과세가격 합계")
    private Double val;
    @ApiModelProperty(value="B/L번호")
    private String blNo;
    @ApiModelProperty(value="선(기)명")
    private String shipNm;
    @ApiModelProperty(value="입항일자")
    private String inPortDt;
    @ApiModelProperty(value="납세의무자 사업자등록번호(암호화)")
    private String juminNo;
    @ApiModelProperty(value="납세의무자 사업자등록번호")
    private String companyBusinessNo;
    @ApiModelProperty(value="납세의무자 구분")
    private String juminGbn;
    @ApiModelProperty(value="납세의무자 상호")
    private String officeNm;
    @ApiModelProperty(value="납세의무자 성명")
    private String ownerNm;
    @ApiModelProperty(value="반출승인개수")
    private Integer outOkQty;
    @ApiModelProperty(value="반출승인중량")
    private Double outOkWgt;
    @ApiModelProperty(value="비고")
    private String remark;
    @ApiModelProperty(value="전송구분")
    private String tranGbn;
    @ApiModelProperty(value="수입요건법령코드")
    private String criteriaCode;
    @ApiModelProperty(value="결제방법")
    private String insPaymentType;
    @ApiModelProperty(value="적하보험료납부여부")
    private String insPaymentYn;
    @ApiModelProperty(value="HS코드")
    private String hsCode;
    @ApiModelProperty(value="TIMESTAMP")
    private String timestamp;
    @ApiModelProperty(value="송신시스템명")
    private String sndSysCd;
    @ApiModelProperty(value="수신시스템명")
    private String recvSysCd;
    @ApiModelProperty(value="인터페이스 진행상태")
    private String sndStatCd;
    @ApiModelProperty(value="송신에러메세지")
    private String errMsg;
    @ApiModelProperty(value="송신일자")
    private String sndYmd;
    @ApiModelProperty(value="송신시각")
    private String sndHms;
    
}
