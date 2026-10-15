package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "BmsEcEtc", description = "EC 통관")
@Alias("bmsEcEtc")
public class BmsEcEtc extends ResultList {
    @ApiModelProperty(value="인터페이스ID")
    private String interfaceId;
    @ApiModelProperty(value="인터페이스SEQ")
    private Integer interfaceSeq;
    @ApiModelProperty(value="CUD")
    private String isCud;
    @ApiModelProperty(value="반입번호(년도)")
    private String inNo1;
    @ApiModelProperty(value="반입번호(일련번호)")
    private String inNo2;
    @ApiModelProperty(value="업무유형")
    private String workGubun;
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
    @ApiModelProperty(value="M.B/L번호")
    private String mbl;
    @ApiModelProperty(value="H.B/L번호")
    private String hbl;
    @ApiModelProperty(value="택배번호")
    private String takbaeLblid;
    @ApiModelProperty(value="비고")
    private String remark;
    @ApiModelProperty(value="전송구분")
    private String tranGbn;
    @ApiModelProperty(value="X-ray 재판독일시")
    private String xrayRescanDate;
    @ApiModelProperty(value="X-ray 검사코드")
    private String xrayInspectionCode;
    @ApiModelProperty(value="로케이션적치일시")
    private String locationScanDate;
    @ApiModelProperty(value="로케이션")
    private String locationPlace;
    @ApiModelProperty(value="검사대상여부")
    private String inspectionYn;
    @ApiModelProperty(value="검사완료일시")
    private String inspectionFinishDate;
    @ApiModelProperty(value="검사완료여부")
    private String inspectionFinishYn;
    @ApiModelProperty(value="재입고일시")
    private String returnDate;
    @ApiModelProperty(value="재입고사유")
    private String returnReason;
    @ApiModelProperty(value="보수완료일시")
    private String repairReason;
    @ApiModelProperty(value="보수수량")
    private String repairCt;
    @ApiModelProperty(value="견품출고수량")
    private Integer sampleOutCt;
    @ApiModelProperty(value="견품출고중량")
    private Double sampleOutWt;
    @ApiModelProperty(value="견품입고수량")
    private Integer sampleInCt;
    @ApiModelProperty(value="견품입고중량")
    private Double sampleInWt;
    @ApiModelProperty(value="이고대상여부")
    private String intellectualPropYn;
    @ApiModelProperty(value="이고진행상태")
    private String intellectualPropStat;
    @ApiModelProperty(value="적하정정 통보수신수량")
    private Integer packCntUnipass;
    @ApiModelProperty(value="적하정정 통보수신중량")
    private Double totWgtUnipass;
    @ApiModelProperty(value="반출승인유형")
    private String outOkType;
    @ApiModelProperty(value="반출승인취하구분")
    private String outOkGbn;
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
