package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class CustomerInfoVE {
	@JsonAlias({"CellPhoneNumber", "cellPhoneNumber"})
	private String cellPhoneNumber;
	@JsonAlias({"EnglishAddress", "englishAddress"})
	private String englishAddress;
	@JsonAlias({"EnglishName", "englishName"})
	private String englishName;
	@JsonAlias({"KoreanAddress", "koreanAddress"})
	private String koreanAddress;
	@JsonAlias({"KoreanName", "koreanName"})
	private String koreanName;
	@JsonAlias({"Pccc", "pccc"})
	private String pccc;
	@JsonAlias({"PcccHolder", "pcccHolder"})
	private String pcccHolder;
	@JsonAlias({"PhoneNumber", "phoneNumber"})
	private String phoneNumber;
	@JsonAlias({"Zipcode", "zipcode"})
	private String zipcode;
	@JsonAlias({"SingleUseCertificateNumber", "singleUseCertificateNumber"})
	private String singleUseCertificateNumber;
	@JsonAlias({"OrdererPhoneNumber", "ordererPhoneNumber"})
	private String ordererPhoneNumber;
	@JsonAlias({"OrdererId", "ordererId"})
	private String ordererId;
	
	
	
}
