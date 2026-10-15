package org.yht.exps.express.model.bms.send.xml.GOVCBR5TL;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 특송물품 XRAY선별속성변경신청서 XML 구성 (문서번호: GOVCBR5TL)
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Declaration", namespace = "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfSPC_5TLSchemaModule:1:0")
public class DeclGOVCBR5TL {

	/** 화물관리번호 */
	@XmlElement(name = "wco:ID", required = true)
	private String id;

	/** XRAY검사선별여부(검사선별:Y, 이상없음:N) */
	@XmlElement(name = "wco:StatusCode")
	private String statusCode;

	/** 문서형태구분 */
	@XmlElement(name = "wco:TypeCode")
	private String typeCode;

	/** 검사지정사유(1:고가/수량과다, 2:마약총포등 사회안전물품, 3:불법의약품등 국민건강위해물품, 4:품명상이, 5:기타(목록비대상물품등) */
	@XmlElement(name = "kcs:ReasonCode")
	private String reasonCode;

}
