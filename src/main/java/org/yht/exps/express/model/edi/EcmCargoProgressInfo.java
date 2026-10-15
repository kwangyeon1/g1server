package org.yht.exps.express.model.edi;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "cargCsclPrgsInfoQryRtnVo")
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmCargoProgressInfo {
	
	 @XmlElement(name = "cargCsclPrgsInfoQryVo")
	 private CargCsclPrgsInfo cargCsclPrgsInfo;

	 @XmlElement(name = "cargCsclPrgsInfoDtlQryVo")
     private List<CargCsclPrgsInfoDtail> cargCsclPrgsInfoDtails;
	 
	@Data
	@XmlRootElement(name = "cargCsclPrgsInfoQryVo")
	@XmlAccessorType(XmlAccessType.FIELD)
    public static class CargCsclPrgsInfo {
		//통관진행상태
	    @XmlElement(name = "csclPrgsStts")
	    private String csclPrgsStts;
	    //항차
	    @XmlElement(name = "vydf")
	    private String vydf;
	    //반출의무과태료여부
	    @XmlElement(name = "rlseDtyPridPassTpcd")
	    private String rlseDtyPridPassTpcd;
	    //품명
	    @XmlElement(name = "prnm")
	    private String prnm;
	    //적재항코드
	    @XmlElement(name = "ldprCd")
	    private String ldprCd;
	    //선박국적
	    @XmlElement(name = "shipNat")
	    private String shipNat;
	    //B/L 유형
	    @XmlElement(name = "blPt")
	    private String blPt;
	    //양륙항명
	    @XmlElement(name = "dsprNm")
	    private String dsprNm;
	    //입항일자
	    @XmlElement(name = "etprDt")
	    private String etprDt;
	    //진행상태코드
	    @XmlElement(name = "prgsStCd")
	    private String prgsStCd;
	    //용적
	    @XmlElement(name = "msrm")
	    private String msrm;
	    //중량단위
	    @XmlElement(name = "wghtUt")
	    private String wghtUt;
	    //양륙항코드
	    @XmlElement(name = "dsprCd")
	    private String dsprCd;
	    //컨테이너개수
	    @XmlElement(name = "cntrGcnt")
	    private String cntrGcnt;
	    // 화물구붂
	    @XmlElement(name = "cargTp")
	    private String cargTp;
	    //선사항공사부호
	    @XmlElement(name = "shcoFlcoSgn")
	    private String shcoFlcoSgn;
	    //포장개수
	    @XmlElement(name = "pckGcnt")
	    private String pckGcnt;
	    //입항세관
	    @XmlElement(name = "etprCstm")
	    private String etprCstm;
	    //선박명
	    @XmlElement(name = "shipNm")
	    private String shipNm;
	    //HBL 번호
	    @XmlElement(name = "hblNo")
	    private String hblNo;
	    //처리일시
	    @XmlElement(name = "prcsDttm")
	    private String prcsDttm;
	    //포워더부호
	    @XmlElement(name = "frwrSgn")
	    private String frwrSgn;
	    //특수화물코드
	    @XmlElement(name = "spcnCargCd")
	    private String spcnCargCd;
	    //총중량
	    @XmlElement(name = "ttwg")
	    private String ttwg;
	    //적재항명
	    @XmlElement(name = "ldprNm")
	    private String ldprNm;
	    //포워더명
	    @XmlElement(name = "frwrEntsConm")
	    private String frwrEntsConm;
	    //신고지연가산세여부
	    @XmlElement(name = "dclrDelyAdtxYn")
	    private String dclrDelyAdtxYn;
	    //관리대상화물여부명
	    @XmlElement(name = "mtTrgtCargYnNm")
	    private String mtTrgtCargYnNm;
	    //화물관리번호
	    @XmlElement(name = "cargMtNo")
	    private String cargMtNo;
	    //컨테이너번호
	    @XmlElement(name = "cntrNo")
	    private String cntrNo;
	    //MBL 번호
	    @XmlElement(name = "mblNo")
	    private String mblNo;
	    //B/L 유형명
	    @XmlElement(name = "blPtNm")
	    private String blPtNm;
	    //적출국가코드
	    @XmlElement(name = "lodCntyCd")
	    private String lodCntyCd;
	    //진행상태
	    @XmlElement(name = "prgsStts")
	    private String prgsStts;
	    //선사항공사
	    @XmlElement(name = "shcoFlco")
	    private String shcoFlco;
	    //포장단위
	    @XmlElement(name = "pckUt")
	    private String pckUt;
	    //선박국적명
	    @XmlElement(name = "shipNatNm")
	    private String shipNatNm;
	    //대리점
	    @XmlElement(name = "agnc")
	    private String agnc;
    }
	
	@Data
	@XmlRootElement(name = "cargCsclPrgsInfoDtlQryVo")
	@XmlAccessorType(XmlAccessType.FIELD)
    public static class CargCsclPrgsInfoDtail {
		//장치장명
		@XmlElement(name = "shedNm")
        private String shedNm;
        //처리일시
        @XmlElement(name = "prcsDttm")
        private String prcsDttm;
        //신고번호
        @XmlElement(name = "dclrNo")
        private String dclrNo;
        //반출입일시
        @XmlElement(name = "rlbrDttm")
        private String rlbrDttm;
        //중량
        @XmlElement(name = "wght")
        private String wght;
        //반출입근거번호
        @XmlElement(name = "rlbrBssNo")
        private String rlbrBssNo;
        //사전안내내용
        @XmlElement(name = "bfhnGdncCn")
        private String bfhnGdncCn;
        //중량단위
        @XmlElement(name = "wghtUt")
        private String wghtUt;
        //포장개수
        @XmlElement(name = "pckGcnt")
        private String pckGcnt;
        //처리구붂
        @XmlElement(name = "cargTrcnRelaBsopTpcd")
        private String cargTrcnRelaBsopTpcd;
        //포장단위
        @XmlElement(name = "pckUt")
        private String pckUt;
        //반출입내용
        @XmlElement(name = "rlbrCn")
        private String rlbrCn;
        //장치장부호
        @XmlElement(name = "shedSgn")
        private String shedSgn;
    }

	@Override
	public String toString() {
		return "CargoProgressInfo [cargCsclPrgsInfo=" + cargCsclPrgsInfo + ", cargCsclPrgsInfoDtails="
				+ cargCsclPrgsInfoDtails + "]";
	}	
	
	
}