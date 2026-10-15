package org.yht.exps.express.model.bms.send.xml.GOVCBR5LD;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

/**
 * 반출정정신청서 XML 구성 (문서번호: GOVCBR5LD)
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Declaration", namespace = "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LDSchemaModule:1:0")
public class OutGOVCBR5LD {

    /** 반입신고번호 */
    @XmlElement(name = "wco:ID")
    private String id;

    /** 신청일자(CCYYMMDD) */
    @XmlElement(name = "wco:IssueDateTime")
    private String issueDateTime;

    /**  문서형태구분 */
    @XmlElement(name = "wco:TypeCode")
    private String typeCode;

    /** 신청차수 */
    @XmlElement(name = "wco:VersionID")
    private String versionID;

    /** 신청구분(01:정정, 03:삭제) */
    @XmlElement(name = "kcs:SubTypeCode")
    private String subTypeCode;

    /** 정정사유 */
    @XmlElement(name = "kcs:Reason")
    private String reason;

    @XmlElement(name = "wco:Amendment")
    private List<Amendment5LD> amendments;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    //@XmlRootElement(name = "wco:Amendment")
    public static class Amendment5LD {
    	/** 정정전 */
    	@XmlElement(name = "wco:StatementDescription")
    	private String statementDescription;

    	/** 정정후 */
    	@XmlElement(name = "kcs:AdjustmentDescription")
    	private String adjustmentDescription;

    	@XmlElement(name = "wco:Pointer")
    	private Pointer pointer;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:Pointer")
    public static class Pointer {
    	/** 항목번호 */
    	@XmlElement(name = "wco:TagID")
    	private String tagID;
    }

    @XmlElement(name = "wco:Submitter")
    private Submitter submitter;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:Submitter")
    public static class Submitter {
    	/** 신청자 성명 */
    	@XmlElement(name = "wco:Name")
    	private String name;
    }

    @XmlElement(name = "wco:UCR")
    private UCR ucr;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "wco:UCR")
    public static class UCR {
        /** 화물관리번호 */
        @XmlElement(name = "kcs:CustomsAssignedReferenceID")
        private String customsAssignedReferenceID;
    }

}
