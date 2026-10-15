package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ContainerInfo", propOrder = {"containerNo", "containerType", "sealNo1", "sealNo2", "sealNo3",
		"qty", "qtyUnit", "grossWeight", "CBM"})

public class OceanContainerInfo{
	
	private String ContainerNo;
	private String ContainerType;
	private String SealNo1;
	private String SealNo2;
	private String SealNo3;
	private String Qty;
	private String QtyUnit;
	private String GrossWeight;
	private String CBM;

	@XmlElement(name="ContainerNo")
	public String getContainerNo() {
		return ContainerNo;
	}
	public void setContainerNo(String containerNo) {
		ContainerNo = containerNo;
	}
	
	@XmlElement(name="ContainerType")
	public String getContainerType() {
		return ContainerType;
	}
	public void setContainerType(String containerType) {
		ContainerType = containerType;
	}
	
	@XmlElement(name="SealNo1")
	public String getSealNo1() {
		return SealNo1;
	}
	public void setSealNo1(String sealNo1) {
		SealNo1 = sealNo1;
	}
	
	@XmlElement(name="SealNo2")
	public String getSealNo2() {
		return SealNo2;
	}
	public void setSealNo2(String sealNo2) {
		SealNo2 = sealNo2;
	}
	
	@XmlElement(name="SealNo3")
	public String getSealNo3() {
		return SealNo3;
	}
	public void setSealNo3(String sealNo3) {
		SealNo3 = sealNo3;
	}
	
	@XmlElement(name="Qty")
	public String getQty() {
		return Qty;
	}
	public void setQty(String qty) {
		Qty = qty;
	}
	
	@XmlElement(name="QtyUnit")
	public String getQtyUnit() {
		return QtyUnit;
	}
	public void setQtyUnit(String qtyUnit) {
		QtyUnit = qtyUnit;
	}
	
	@XmlElement(name="GrossWeight")
	public String getGrossWeight() {
		return GrossWeight;
	}
	public void setGrossWeight(String grossWeight) {
		GrossWeight = grossWeight;
	}
	
	@XmlElement(name="CBM")
	public String getCBM() {
		return CBM;
	}
	public void setCBM(String cBM) {
		CBM = cBM;
	}
}