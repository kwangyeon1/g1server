package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "BmsEcIn", description = "EC 반입")
@Alias("bmsEcIn")
public class BmsEcIn extends ResultList {
    @ApiModelProperty(value="인터페이스ID")
    private String interfaceId;
    @ApiModelProperty(value="인터페이스SEQ")
    private Integer interfaceSeq;
    @ApiModelProperty(value="CUD")
    private String isCud;
    @ApiModelProperty(value="제공일련번호")
    private String seqNo;
    @ApiModelProperty(value="반입번호(년도)")
    private String inNo1;
    @ApiModelProperty(value="반입번호(일련번호)")
    private String inNo2;
    @ApiModelProperty(value="보세구역번호")
    private String bondNo;
    @ApiModelProperty(value="반입일시")
    private String inDt;
    @ApiModelProperty(value="장치위치")
    private String plcLoc;
    @ApiModelProperty(value="화물반입유형")
    private String inGubun;
    @ApiModelProperty(value="분할반입구분")
    private String diviInYn;
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
    @ApiModelProperty(value="반입근거번호")
    private String inStdNo;
    @ApiModelProperty(value="X-ray 검사여부")
    private String xrayGbn;
    @ApiModelProperty(value="탱크번호")
    private String tankNo;
    @ApiModelProperty(value="반입개수")
    private Integer inQty;
    @ApiModelProperty(value="포장종류")
    private String packGbn;
    @ApiModelProperty(value="반입중량")
    private Double inWgt;
    @ApiModelProperty(value="업체코드")
    private String ownerCd;
    @ApiModelProperty(value="관세청 전송결과")
    private String sendYn;
    @ApiModelProperty(value="비고")
    private String remark;
    @ApiModelProperty(value="전송구분")
    private String tranGbn;
    @ApiModelProperty(value="거대여부")
    private String bigSizeYn;
    @ApiModelProperty(value="이고 지재권여부")
    private String intellectualPropYn;
    @ApiModelProperty(value="X-ray 검사코드")
    private String xrayInspectionCode;
    @ApiModelProperty(value="정정구분")
    private String changeClass;
    @ApiModelProperty(value="정정항목")
    private String changeCode;
    @ApiModelProperty(value="정정전내용")
    private String changeBefore;
    @ApiModelProperty(value="정정후내용")
    private String changeAfter;
    @ApiModelProperty(value="X-ray 자동/수동")
    private String xrayMachine;
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
