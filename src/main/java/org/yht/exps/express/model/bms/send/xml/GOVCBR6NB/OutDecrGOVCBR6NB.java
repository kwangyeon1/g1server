package org.yht.exps.express.model.bms.send.xml.GOVCBR6NB;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 반출 신고서 XML 구성 (문서번호: GOVCBR6NB)
 *
 * wco:FunctionCode - 전자문서 기능 코드를 나타냅니다.
 * wco:ID - 신고 번호를 나타냅니다.
 * wco:TypeCode - 문서 형태 구분을 나타냅니다.
 * wco:VersionID - 분할 반출 차수를 나타냅니다.
 * kcs:GrossVolumeMeasure - 반출 중량을 나타냅니다.
 * kcs:CarryOutTypeCode - 화물 반출 유형을 나타냅니다.
 * kcs:CarryOutSplitCode - 분할 반출 구분을 나타냅니다.
 * kcs:CarryOutExtensionCode - 반출 기간 연장 구분을 나타냅니다.
 * wco:AdditionalDocument - 추가 문서를 나타내는 컨테이너 요소입니다.
 * wco:TotalPackageQuantity - 반출 총 개수를 나타냅니다.
 * wco:UCR - 화물 관리 번호를 나타내는 컨테이너 요소입니다.
 * kcs:CustomsAssignedReferenceID - 화물 관리 번호를 나타냅니다.
 * wco:Warehouse - 창고 정보를 나타내는 컨테이너 요소입니다.
 * wco:DepartureDateTime - 반출 일시를 나타냅니다.
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Declaration", namespace = "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_6NBSchemaModule:1:0")
public class OutDecrGOVCBR6NB {

    /** 전자문서기능 (9: 원본, 35: 재전송) */
    @XmlElement(name = "wco:FunctionCode", required = true)
    private String functionCode;

    /** 신고번호 신고서를 세관에 제출시 신고인이 부여하는 번호 (보세구역부호(8)+년도(02)+일련번호(08)) */
    @XmlElement(name = "wco:ID", required = true)
    private String id;

    /** 문서형태구분 */
    @XmlElement(name = "wco:TypeCode")
    private String typeCode;

    /** 분할반출차수  */
    @XmlElement(name = "wco:VersionID")
    private String versionID;

    /** 반출중량 */
    @XmlElement(name = "kcs:GrossVolumeMeasure")
    private String grossVolumeMeasure;

    /** 반출중량 */
    @XmlElement(name = "wco:AdditionalCode")
    private AdditionalCode additionalCode;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:AdditionalCode")
    public static class AdditionalCode {
    	/** 화물반출유형 */
    	@XmlElement(name = "kcs:CarryOutTypeCode")
    	private String carryOutTypeCode;
    	/** 분할반출구분 */
    	@XmlElement(name = "kcs:CarryOutSplitCode")
    	private String carryOutSplitCode;
    	/** 반출기간연장 */
    	@XmlElement(name = "kcs:CarryOutExtensionCode")
    	private String carryOutExtensionCode;
    }

    /** 추가 문서를 나타내는 컨테이너 요소 */
    @XmlElement(name = "wco:AdditionalDocument")
    private AdditionalDocument additionalDocument;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:AdditionalDocument")
    public static class AdditionalDocument {
    	/** 반출근거번호 20자리 */
    	@XmlElement(name = "wco:ID")
    	private String id;
    }

    /** 화주 */
    @XmlElement(name = "wco:Consignment")
    private Consignment consignment;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:Consignment")
    public static class Consignment {
        /** 반출 총 개수 */
        @XmlElement(name = "wco:TotalPackageQuantity")
        private String totalPackageQuantity;
        /** 기간연장근거번호 */
        @XmlElement(name = "wco:AdditionalDocument")
        private AdditionalDocument additionalDocument;
        /** 화물관리번호 */
        @XmlElement(name = "wco:UCR")
        private UCR ucr;
        @XmlElement(name = "wco:Warehouse")
        private Warehouse warehouse;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:UCR")
    public static class UCR {
        /** 화물관리번호 19자리 */
        @XmlElement(name = "kcs:CustomsAssignedReferenceID")
        private String customsAssignedReferenceID;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:Warehouse")
    public static class Warehouse {
        /** 반출일시 CCYYMMDDHHMMSS 201401010000 필수 */
        @XmlElement(name = "wco:DepartureDateTime")
        private String departureDateTime;
    }

}
