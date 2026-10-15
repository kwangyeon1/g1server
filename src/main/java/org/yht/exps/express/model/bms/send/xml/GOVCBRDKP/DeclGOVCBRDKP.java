package org.yht.exps.express.model.bms.send.xml.GOVCBRDKP;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/**
 * 특송화물 반입물품 이상보고 정정신청서 XML 구성 (문서번호: GOVCBRDKP)
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Declaration", namespace = "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_DKPSchemaModule:1:0")
public class DeclGOVCBRDKP {

	/** 세관/과부호 */
	@XmlElement(name = "wco:DeclarationOfficeID")
	private String declarationOfficeID;

	/** 제출번호 (보세구역부호(8)+반입년도(2)+신고일련번호(8)) */
	@XmlElement(name = "wco:ID", required = true)
	private String id;

	/** 제출일자 (YYYYMMDD) */
	@XmlElement(name = "wco:IssueDateTime")
	private String issueDateTime;

	/** 문서형태구분 */
	@XmlElement(name = "wco:TypeCode")
	private String typeCode;

	/** 정정차수 */
	@XmlElement(name = "wco:VersionID")
	private String versionID;

	/** Amendment */
	@XmlElement(name = "wco:Amendment")
	private Amendment amendment;

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:Amendment")
	public static class Amendment {
		/** 정정사유 */
		@XmlElement(name = "wco:Content")
		private String content;
	}

	/** Consignment */
	@XmlElement(name = "wco:Consignment")
	private Consignment consignment;

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:Consignment")
	public static class Consignment {
		/** 적재화물목록중량 */
		@XmlElement(name = "wco:GrossVolumeMeasure")
		private String grossVolumeMeasure;

		/** 적재화물목록포장개수 */
		@XmlElement(name = "wco:TotalPackageQuantity")
		private String totalPackageQuantity;

		/** 반입결과중량 */
		@XmlElement(name = "wco:ValueAmount")
		private String valueAmount;

		/** 사고중량 */
		@XmlElement(name = "kcs:AbnormalityWeightMeasure")
		private String abnormalityWeightMeasure;

		/** 사고포장개수 */
		@XmlElement(name = "kcs:AbnormalityQuantityQuantity")
		private String abnormalityQuantityQuantity;

		/** 반입결과포장개수 */
		@XmlElement(name = "kcs:ResultsQuantity")
		private String resultsQuantity;

		/** AdditionalInformation */
		@XmlElement(name = "wco:AdditionalInformation")
		private AdditionalInformation additionalInformation;

		/** ConsignmentItem (0..9999 반복 List) */
		@XmlElement(name = "wco:ConsignmentItem")
		private List<ConsignmentItemDKP> consignmentItem;

		/** TransportContractDocument */
		@XmlElement(name = "wco:TransportContractDocument")
		private TransportContractDocument transportContractDocument;
	}

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:AdditionalInformation")
	public static class AdditionalInformation {
		/** 사고/상이 내역부호 */
		@XmlElement(name = "wco:StatementCode")
		private String statementCode;

		/** 정정구분 */
		@XmlElement(name = "wco:StatementTypeCode")
		private String statementTypeCode;

		/** 이상유형상세내용 */
		@XmlElement(name = "wco:StatementDescription")
		private String statementDescription;
	}

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:ConsignmentItem")
	public static class ConsignmentItemDKP {
		/** AdditionalInformation */
		@XmlElement(name = "wco:AdditionalInformation")
		private AdditionalInformationHbl additionalInformation;

		/** GoodsMeasure */
		@XmlElement(name = "wco:GoodsMeasure")
		private GoodsMeasure goodsMeasure;

		/** Packaging */
		@XmlElement(name = "wco:Packaging")
		private Packaging packaging;

		/** TransportContractDocument */
		@XmlElement(name = "wco:TransportContractDocument")
		private TransportContractDocumentHbl transportContractDocument;
	}

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:TransportContractDocument")
	public static class TransportContractDocument {
		/** Master B/L 번호 */
		@XmlElement(name = "wco:ID")
		private String id;

		/** MSN */
		@XmlElement(name = "kcs:MasterBLSequenceID")
		private String masterBLSequenceID;
	}

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:AdditionalInformation")
	public static class AdditionalInformationHbl {
		/** 사고/상이 내역부호 */
		@XmlElement(name = "wco:StatementCode")
		private String statementCode;

		/** 이상유형상세내용 */
		@XmlElement(name = "wco:StatementDescription")
		private String statementDescription;

		/** 정정구분 */
		@XmlElement(name = "wco:StatementTypeCode")
		private String statementTypeCode;
	}

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:GoodsMeasure")
	public static class GoodsMeasure {
		/** 적재화물목록중량 */
		@XmlElement(name = "wco:GrossMassMeasure")
		private String grossMassMeasure;

		/** 반입결과중량 */
		@XmlElement(name = "wco:NetNetWeightMeasure")
		private String netNetWeightMeasure;

		/** 사고중량 */
		@XmlElement(name = "wco:TariffQuantity")
		private String tariffQuantity;
	}

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:Packaging")
	public static class Packaging {
		/** 적재화물목록포장개수 */
		@XmlElement(name = "wco:QuantityQuantity")
		private String quantityQuantity;

		/** 반입결과포장개수 */
		@XmlElement(name = "wco:MarksNumbersID")
		private String marksNumbersID;

		/** 사고포장개수 */
		@XmlElement(name = "kcs:ConcurrentPackagingID")
		private String concurrentPackagingID;
	}

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:TransportContractDocument")
	public static class TransportContractDocumentHbl {
		/** House B/L 번호 */
		@XmlElement(name = "wco:ID")
		private String id;

		/** HSN */
		@XmlElement(name = "kcs:HouseBLSequenceID")
		private String houseBLSequenceID;
	}

	/** PreviousDocument */
	@XmlElement(name = "wco:PreviousDocument")
	private PreviousDocument previousDocument;

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:PreviousDocument")
	public static class PreviousDocument {
		/** 적재화물목록관리번호 */
		@XmlElement(name = "wco:ID")
		private String id;

		/** 해상/항공전송구분 */
		@XmlElement(name = "wco:TypeCode")
		private String typeCode;
	}

}
