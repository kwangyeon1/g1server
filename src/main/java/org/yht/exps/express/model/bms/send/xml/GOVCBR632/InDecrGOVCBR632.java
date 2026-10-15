
package org.yht.exps.express.model.bms.send.xml.GOVCBR632;

import lombok.Data;

import javax.xml.bind.annotation.*;


/**
 *  반입 신고서 XML 구성
 *  4개 항목 XML생성시 김현수 bu , 제외 요청함
 *  Declaration/AdditionalCode/CarryInUseCode
 *  Declaration/TransportEquipment/ID
 *  Declaration/VersionID
 *  Declaration/Consignment/Consignor/ID
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Declaration", namespace = "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_632SchemaModule:1:0")
//@XmlRootElement(name = "Declaration")
//  @XmlNs(prefix = "kcs", namespaceURI = "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_632SchemaModule:1:0"),
public class InDecrGOVCBR632 {
    /** 전자문서기능 9:원본, 35:재전송*/
    @XmlElement(name ="wco:FunctionCode", required = true) //M.Madatory
    private String functionCode;
    /** 신고번호 신고서를 세관에 제출시 신고인이 부여하는 번호 (보세구역부호(8)+년도(02)+일련번호(08)) */
    @XmlElement(name ="wco:ID", required = true)
    private String id;
    /** 문서형태구분 전송될 전자문서의 유형을 기재(632 : 반입신고)*/
    @XmlElement(name ="wco:TypeCode", required = true)
    private String typeCode;
    /** 분할반입차수 3자리
     * 컬럼 제외 처리  : 04-08
     * */
   /* @XmlElement(name ="wco:VersionID", required = true)
    private String versionID;*/
    /**  X-ray 검사여부 01:실시간 X-Ray 시스템검색 *** kcs ***
     02:거대화물용 X-Ray 검색
     03:세관직원 현품검사
     04:세관직원 검사생략
     05:X-Ray 미검색*/
    @XmlElement(name ="kcs:ExaminationIndicatorCode")
    private String examinationIndicatorCode;
    @XmlElement(name = "wco:AdditionalCode")
    private AdditionalCode additionalCode;
    @XmlElement(name = "wco:AdditionalDocument")
    private AdditionalDocument additionalDocument;
    @XmlElement(name = "wco:Consignment")
    private Consignment consignment;
    //컬럼 제외 처리  : 04-08
   /* @XmlElement(name = "wco:TransportEquipment")
    private TransportEquipment transportEquipment;*/

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:AdditionalCode")
    public static class AdditionalCode {
        /** 화물반입유형 반입유형코드 2자리
         *  컬럼 제외 처리  : 04-08
         *  2024-05-20 컬럼 추가 (최정민)
         * *** kcs ****/
       @XmlElement(name = "kcs:CarryInTypeCode")
        private String carryInTypeCode;

        /** 분할반입구분(A:전량, P:분할, L:최종)
         * 실제는 "A" 로 고정 처리 : 04-08
         * *** kcs ****/
        @XmlElement(name = "kcs:CarryInSplitCode")
        private String carryInSplitCode;

        /**   컬럼 제외 처리  : 04-08 반입용도구분(P:보세작업용, C:수입통관용, B:BWT 물품, L:LME, G:정부비축물품, E:물류촉진, M:복합물류용, R:중계무역물품 *** kcs *** */
      /*  @XmlElement(name = "kcs:CarryInUseCode")
        private String carryInUseCode;*/
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:AdditionalDocument")
    public static class AdditionalDocument {
        /** 반입근거번호 20 자리 */
        @XmlElement(name = "wco:ID")
        private String id;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:Consignment")
    public static class Consignment {
        /** 반입중량 반입중량을 기재(단위:KG) 16.3*/
        @XmlElement(name = "wco:GrossVolumeMeasure", required = true)
        private String grossVolumeMeasure;

        /** 반입개수 총 포장개수를 기재 10 */
        @XmlElement(name = "wco:TotalPackageQuantity", required = true)
        private String totalPackageQuantity;

        /** 반입사고 유형  반입사고유형을  기재 반입내역에 이상이 없을시에는 'OKY' 기재 *** kcs ****/
        @XmlElement(name = "kcs:AbnormalityTypeCode", required = true)
        private String abnormalityTypeCode;
        /** 반입사고중량을 기재 16.3 *** kcs ****/
        @XmlElement(name = "kcs:AbnormalityWeightMeasure")
        private String abnormalityWeightMeasure;
        /** 반입사고개수를 기재(단위:CT) 10 *** kcs ****/
        @XmlElement(name = "kcs:AbnormalityQuantityQuantity")
        private String abnormalityQuantityQuantity;
        //컬럼 제외 처리  : 04-08
       /* @XmlElement(name = "wco:Consignor")
        private Consignor consignor;*/
        @XmlElement(name = "wco:Packaging")
        private Packaging packaging;
        @XmlElement(name = "wco:UCR")
        private UCR ucr;
        @XmlElement(name = "wco:Warehouse")
        private Warehouse warehouse;
    }

    /***
     * Consignor 컬럼 제외 처리  : 04-08
     */
   /* @Data
    @XmlRootElement(name = "wco:Consignor")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Consignor {
        *//** 화주사업자등록번호 10 반입용도구분이(Declaration/AdditionalCode/CarryInUseCode) R일 경우 필수 입력 사항
         * 컬럼 제외 처리  : 04-08
         * *//*
     *//*   @XmlElement(name = "wco:ID")
        private String id;*//*
    }
*/
    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:Packaging")
    public static class Packaging {
        /** 포장종류를 기재 2자리*/
        @XmlElement(name = "wco:TypeCode", required = true)
        private String typeCode;
    }
    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:UCR")
    public static class UCR {
        /** 화물관리번호 19자리*/
        @XmlElement(name = "kcs:CustomsAssignedReferenceID")
        private String customsAssignedReferenceID;
    }
    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:Warehouse")
    public static class Warehouse {
        /** 반입일시 CCYYMMDDHHMMSS  필수 */
        @XmlElement(name = "wco:ArrivalDateTime")
        private String arrivalDateTime;

        /** 장치위치   선택*/
        @XmlElement(name = "wco:ID")
        private String id;
    }

    /**
     *   컬럼 제외 처리  : 04-08
     */
   /* @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:TransportEquipment")
    public static class TransportEquipment {
        *//** 탱크번호를 기재 5자리  => 컨테이너번호?
         반입유형 구분부호가 16인 경우 필수입력 TK001
         컬럼 제외 처리  : 04-08
         *//*
        *//*@XmlElement(name = "wco:ID" )
        private String id;*//*
    }*/

}

