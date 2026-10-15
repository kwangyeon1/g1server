package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "BmsEcOut", description = "EC 반출")
@Alias("bmsEcOut")
public class BmsEcOut extends ResultList {
    @ApiModelProperty(value="인터페이스ID")
    private String interfaceId;
    @ApiModelProperty(value="인터페이스SEQ")
    private Integer interfaceSeq;
    @ApiModelProperty(value="CUD")
    private String isCud;
    @ApiModelProperty(value="제공일련번호")
    private String seqNo;
    @ApiModelProperty(value="개발원코드")
    private String part;
    @ApiModelProperty(value="반출년도")
    private String outNo1;
    @ApiModelProperty(value="반출일련번호")
    private String outNo2;
    @ApiModelProperty(value="분할반출차수")
    private String diviOutLast;
    @ApiModelProperty(value="보세구역번호")
    private String bondNo;
    @ApiModelProperty(value="반출일시")
    private String outDt;
    @ApiModelProperty(value="장치위치")
    private String plcLoc;
    @ApiModelProperty(value="화물반출유형")
    private String outGubun;
    @ApiModelProperty(value="분할반출구분")
    private String diviOutYn;
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
    @ApiModelProperty(value="반출근거번호")
    private String outStdNo;
    @ApiModelProperty(value="반출개수")
    private Integer outQty;
    @ApiModelProperty(value="포장종류")
    private String packGbn;
    @ApiModelProperty(value="반입중량")
    private Double outWgt;
    @ApiModelProperty(value="업체코드")
    private String ownerCd;
    @ApiModelProperty(value="관세청 전송결과")
    private String sendYn;
    @ApiModelProperty(value="비고")
    private String remark;
    @ApiModelProperty(value="전송구분")
    private String tranGbn;
    @ApiModelProperty(value="출고슈트번호")
    private String outChuteNo;
    @ApiModelProperty(value="출고일시")
    private String outDateTime;
    @ApiModelProperty(value="정정구분")
    private String changeClass;
    @ApiModelProperty(value="정정항목")
    private String changeCode;
    @ApiModelProperty(value="정정전내용")
    private String changeBefore;
    @ApiModelProperty(value="정정후내용")
    private String changeAfter;
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
