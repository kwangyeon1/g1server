package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="CustomsInfo", propOrder = {"MBLNo","HBLNo","CStatus","customsAcceptDate","exchangeRate_Amount","fare_Amount","CIF_Amount","duty_Amount","specialExcise_Amount","liquor_Amount","education_Amount","ruralDevelopment_Amount","vat_Amount"})

public class CustomsInfoList{
	private String MBLNo;
	private String HBLNo;
	private String CStatus;
	private String CustomsAcceptDate;
	private String ExchangeRate_Amount;
	private String Fare_Amount;
	private String CIF_Amount;
	private String Duty_Amount;
	private String SpecialExcise_Amount;
	private String Liquor_Amount;
	private String Education_Amount;
	private String RuralDevelopment_Amount;
	private String Vat_Amount;
	
	@XmlElement(name="MBLNo")
	public String getMBLNo() {
		return MBLNo;
	}
	public void setMBLNo(String MBLNo) {
		this.MBLNo = MBLNo;
	}
	@XmlElement(name="HBLNo")
	public String getHBLNo() {
		return HBLNo;
	}
	public void setHBLNo(String HBLNo) {
		this.HBLNo = HBLNo;
	}
	
	@XmlElement(name="CStatus")
	public String getCStatus() {
		return CStatus;
	}
	public void setCStatus(String CStatus) {
		this.CStatus = CStatus;
	}
	
	@XmlElement(name="CustomsAcceptDate")
	public String getCustomsAcceptDate() {
		return CustomsAcceptDate;
	}
	public void setCustomsAcceptDate(String CustomsAcceptDate) {
		this.CustomsAcceptDate = CustomsAcceptDate;
	}
	@XmlElement(name="ExchangeRate_Amount")
	public String getExchangeRate_Amount() {
		return ExchangeRate_Amount;
	}
	public void setExchangeRate_Amount(String ExchangeRate_Amount) {
		this.ExchangeRate_Amount = ExchangeRate_Amount;
	}
	@XmlElement(name="Fare_Amount")
	public String getFare_Amount() {
		return Fare_Amount;
	}
	public void setFare_Amount(String Fare_Amount) {
		this.Fare_Amount = Fare_Amount;
	}
	@XmlElement(name="CIF_Amount")
	public String getCIF_Amount() {
		return CIF_Amount;
	}
	public void setCIF_Amount(String CIF_Amount) {
		this.CIF_Amount = CIF_Amount;
	}
	@XmlElement(name="Duty_Amount")
	public String getDuty_Amount() {
		return Duty_Amount;
	}
	public void setDuty_Amount(String Duty_Amount) {
		this.Duty_Amount = Duty_Amount;
	}
	@XmlElement(name="SpecialExcise_Amount")
	public String getSpecialExcise_Amount() {
		return SpecialExcise_Amount;
	}
	public void setSpecialExcise_Amount(String SpecialExcise_Amount) {
		this.SpecialExcise_Amount = SpecialExcise_Amount;
	}
	@XmlElement(name="Liquor_Amount")
	public String getLiquor_Amount() {
		return Liquor_Amount;
	}
	public void setLiquor_Amount(String Liquor_Amount) {
		this.Liquor_Amount = Liquor_Amount;
	}
	@XmlElement(name="Education_Amount")
	public String getEducation_Amount() {
		return Education_Amount;
	}
	public void setEducation_Amount(String Education_Amount) {
		this.Education_Amount = Education_Amount;
	}
	@XmlElement(name="RuralDevelopment_Amount")
	public String getRuralDevelopment_Amount() {
		return RuralDevelopment_Amount;
	}
	public void setRuralDevelopment_Amount(String RuralDevelopment_Amount) {
		this.RuralDevelopment_Amount = RuralDevelopment_Amount;
	}
	@XmlElement(name="Vat_Amount")
	public String getVat_Amount() {
		return Vat_Amount;
	}
	public void setVat_Amount(String Vat_Amount) {
		this.Vat_Amount = Vat_Amount;
	}
}