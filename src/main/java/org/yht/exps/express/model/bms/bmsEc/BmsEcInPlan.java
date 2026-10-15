package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "BmsEcInPlan", description = "EC 반입예정")
@Alias("bmsEcInPlan")
public class BmsEcInPlan extends ResultList {
    @ApiModelProperty(value="인터페이스ID")
    private String interfaceId;
    @ApiModelProperty(value="인터페이스SEQ")
    private Integer interfaceSeq;
    @ApiModelProperty(value="CUD")
    private String isCud;
    @ApiModelProperty(value="사무소코드")
    private String part;
    @ApiModelProperty(value="반입번호(년도)")
    private String inNo1;
    @ApiModelProperty(value="반입번호(일련번호)")
    private String inNo2;
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
    @ApiModelProperty(value="화물구분코드")
    private String pkgGbn;
    @ApiModelProperty(value="반입일자")
    private String inDt;
    @ApiModelProperty(value="입항세관")
    private String cusCd1;
    @ApiModelProperty(value="화주코드")
    private String ownerCd;
    @ApiModelProperty(value="화주명")
    private String ownerComp;
    @ApiModelProperty(value="품목코드")
    private String itemCd;
    @ApiModelProperty(value="품명")
    private String itemNm1;
    @ApiModelProperty(value="M.B/L번호")
    private String mblNo;
    @ApiModelProperty(value="H.B/L번호")
    private String hblNo;
    @ApiModelProperty(value="선명(기명)")
    private String shipNo;
    @ApiModelProperty(value="반입구분코드")
    private String inGubun;
    @ApiModelProperty(value="보세운송기간(from)")
    private String bondFm;
    @ApiModelProperty(value="보세운송기간(to)")
    private String bondTo;
    @ApiModelProperty(value="보세운송 면허번호")
    private String singoReqno;
    @ApiModelProperty(value="보세운송회사")
    private String bondCompCd;
    @ApiModelProperty(value="포장종류")
    private String packKind;
    @ApiModelProperty(value="반입사고구분코드")
    private String inSagoCd;
    @ApiModelProperty(value="반입수량")
    private Integer packCnt;
    @ApiModelProperty(value="반출수량")
    private Integer outQty;
    @ApiModelProperty(value="재고수량")
    private Integer jegoQty;
    @ApiModelProperty(value="반입중량")
    private Double totWgt;
    @ApiModelProperty(value="반출중량")
    private Double outWgt;
    @ApiModelProperty(value="재고중량")
    private Double jegoWgt;
    @ApiModelProperty(value="반입용적")
    private Double totCbm;
    @ApiModelProperty(value="반출용적")
    private Double outCbm1;
    @ApiModelProperty(value="재고용적")
    private Double outCbm;
    @ApiModelProperty(value="장치위치")
    private String plcLoc;
    @ApiModelProperty(value="장치확인일자")
    private String plcConfDt;
    @ApiModelProperty(value="장치확인자")
    private String plcConfId;
    @ApiModelProperty(value="체화여부")
    private String overYn;
    @ApiModelProperty(value="체화번호")
    private String overNo;
    @ApiModelProperty(value="체화일자")
    private String overDt;
    @ApiModelProperty(value="데이터생성구분")
    private String dataCreatGbn;
    @ApiModelProperty(value="사용자코드")
    private String userId;
    @ApiModelProperty(value="비밀번호")
    private String userPw;
    @ApiModelProperty(value="작업일자")
    private String workDt;
    @ApiModelProperty(value="단말기번호")
    private String termId;
    @ApiModelProperty(value="전자상거래여부")
    private String elecYn;
    @ApiModelProperty(value="목록구분(반입유형)")
    private String inType;
    @ApiModelProperty(value="통관유형")
    private String ccSprCd;
    @ApiModelProperty(value="특수화물구분")
    private String spcPkgGbn;
    @ApiModelProperty(value="조업일자")
    private String joupDt;
    @ApiModelProperty(value="입항일시")
    private String inPortDt;
    @ApiModelProperty(value="감가")
    private Double val;
    @ApiModelProperty(value="관세")
    private Double duty;
    @ApiModelProperty(value="적재항")
    private String unldPortCd;
    @ApiModelProperty(value="발송국")
    private String sendNatCd;
    @ApiModelProperty(value="송하인명")
    private String sendOffice;
    @ApiModelProperty(value="송하인주소1")
    private String sendAddr1;
    @ApiModelProperty(value="송하인주소2")
    private String sendAddr2;
    @ApiModelProperty(value="송하인주소3")
    private String sendAddr3;
    @ApiModelProperty(value="송하인 전화번호")
    private String sendTel;
    @ApiModelProperty(value="수하인 전화번호")
    private String recvTel;
    @ApiModelProperty(value="수하인명")
    private String recvNm;
    @ApiModelProperty(value="수하인주소1")
    private String recvAddr1;
    @ApiModelProperty(value="수하인주소2")
    private String recvAddr2;
    @ApiModelProperty(value="수하인주소3")
    private String recvAddr3;
    @ApiModelProperty(value="수하인 우편번호")
    private String recvPostNo;
    @ApiModelProperty(value="전자상거래 사이트주소")
    private String elecDomain;
    @ApiModelProperty(value="물품가격")
    private Integer invAmt;
    @ApiModelProperty(value="물품가격단위")
    private String invAmtUnit;
    @ApiModelProperty(value="지정번호")
    private String directNo;
    @ApiModelProperty(value="거래유형코드")
    private String tradeType;
    @ApiModelProperty(value="용도구분")
    private String useGbn;
    @ApiModelProperty(value="컨테이너번호1")
    private String contrNo1;
    @ApiModelProperty(value="컨테이너번호2")
    private String contrNo2;
    @ApiModelProperty(value="포더부호")
    private String fwdCode;
    @ApiModelProperty(value="포더명(특송업체명)")
    private String fwdFirm;
    @ApiModelProperty(value="UNI-PASS 검사지정사유")
    private String passReasion;
    @ApiModelProperty(value="우범화물여부")
    private String crimeSelectionYn;
    @ApiModelProperty(value="택배사번호")
    private String takbaeNo;
    @ApiModelProperty(value="택배사명칭")
    private String takbaeNm;
    @ApiModelProperty(value="택배집하장번호")
    private String takbaeLoc;
    @ApiModelProperty(value="특이사항")
    private String uniMatt;
    @ApiModelProperty(value="HOLD사유")
    private String holdReason;
    @ApiModelProperty(value="TIMESTAMP")
    private String timestamp;
    @ApiModelProperty(value="Transaction ID")
    private String trnsId;
    @ApiModelProperty(value="송신시스템명")
    private String sndSysCd;
    @ApiModelProperty(value="수신시스템명")
    private String recvSysCd;
    @ApiModelProperty(value="처리상태코드")
    private String prosStatCd;
    @ApiModelProperty(value="수신일자")
    private String recvYmd;
    @ApiModelProperty(value="수신시각")
    private String recvHms;
   
}
