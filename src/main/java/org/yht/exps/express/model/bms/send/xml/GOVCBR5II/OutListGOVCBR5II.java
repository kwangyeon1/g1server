package org.yht.exps.express.model.bms.send.xml.GOVCBR5II;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 반출통고 목록보고서 XML 구성 (문서번호: GOVCBR5II)
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Declaration", namespace = "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5IISchemaModule:1:0")
public class OutListGOVCBR5II {
	// 신고세관/과
	@XmlElement(name = "wco:DeclarationOfficeID")
	private String declarationOfficeID;

	// 전자문서 기능(9:원본, 35:재전송)
	@XmlElement(name = "wco:FunctionCode")
	private String functionCode;

	// 화물관리번호
	@XmlElement(name = "wco:ID")
	private String id;

	// 반출통고일자(CCYYMMDD)
	@XmlElement(name = "wco:IssueDateTime")
	private String issueDateTime;

	// 반입중량
	@XmlElement(name = "wco:TotalGrossMassMeasure")
	private String totalGrossMassMeasure;

	// 반입개수
	@XmlElement(name = "wco:TotalPackageQuantity")
	private String totalPackageQuantity;

	// 문서형태구분
	@XmlElement(name = "wco:TypeCode")
	private String typeCode;

	// AdditionalInformation 요소
	@XmlElement(name = "wco:AdditionalInformation")
	private AdditionalInformation additionalInformation;

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:AdditionalInformation")
	public static class AdditionalInformation {
		// 체화예정일자(CCYYMMDD)
		@XmlElement(name = "wco:LimitDateTime")
		private String limitDateTime;
	}

	// Consignment 요소
	@XmlElement(name = "wco:Consignment")
	private Consignment consignment;

	/**
	 * 위탁 정보
	 */
	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:Consignment")
	public static class Consignment {
		// 반입장소
		@XmlElement(name = "wco:Warehouse")
		private Warehouse warehouse;
	}

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:Warehouse")
	public static class Warehouse {
		// 반입장소 ID // KRPUS
		@XmlElement(name = "wco:ID")
		private String id;
	}

	// Importer 요소
	@XmlElement(name = "wco:Importer")
	private Importer importer;

	/**
	 * 위탁 정보
	 */
	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:Importer")
	public static class Importer {
		// 화주 화주명
		@XmlElement(name = "wco:Name")
		private String name;

		// 화주 주소
		@XmlElement(name = "wco:Address")
		private Address address;

		// 화주 연락처
		@XmlElement(name = "wco:Contact")
		private Contact contact;

		// 화주 통신
		@XmlElement(name = "wco:Communication")
		private Communication communication;
	}

	/**
	 * 위탁 정보
	 */
	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:Address")
	public static class Address {
		// 화주 도로명코드
		@XmlElement(name = "wco:CountrySubDivisionID")
		private String countrySubDivisionID;

		// 화주 상세주소
		@XmlElement(name = "wco:Line")
		private String line;

		// 화주 우편번호
		@XmlElement(name = "wco:PostcodeID")
		private String postcodeID;

		// 화주 건물관리번호  kcs:
		@XmlElement(name = "kcs:BuildingNumber")
		private String buildingNumber;

		// 화주 기본주소  kcs:
		@XmlElement(name = "kcs:Description")
		private String description;
	}

	/**
	 * 계약 조건
	 */
	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:Contact")
	public static class Contact {
		// 화주 생년월일 kcs
		@XmlElement(name = "kcs:BirthDate")
		private String birthDate;
	}

	/**
	 * 위탁 정보
	 */
	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:Communication")
	public static class Communication {
		// 화주 전화번호
		@XmlElement(name = "wco:ID")
		private String id;
	}

	// TransportContractDocument 요소
	@XmlElement(name = "wco:TransportContractDocument")
	private TransportContractDocument transportContractDocument;

	/**
	 * 위탁 정보
	 */
	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "wco:TransportContractDocument")
	public static class TransportContractDocument {
		// B/L 번호
		@XmlElement(name = "wco:ID")
		private String id;
	}

}
