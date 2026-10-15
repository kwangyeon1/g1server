package org.yht.exps.express.model.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class CoupangOrderInterface {
	@JsonAlias({"MblNumber", "mblNumber"})
	private String mblNumber;
	@JsonAlias({"CourierCode", "courierCode"})
	private String courierCode;
	@JsonAlias({"OriginCode", "originCode"})
	private String originCode;
	@JsonAlias({"BrokerCode", "brokerCode"})
	private String brokerCode;
	@JsonAlias({"SalesTypeCode", "salesTypeCode"})
	private String salesTypeCode;
	@JsonAlias({"CompanyCode", "companyCode"})
	private String companyCode;
	@JsonAlias({"CompanyName", "companyName"})
	private String companyName;
	@JsonAlias({"FirstMileCourierCode", "firstMileCourierCode"})
	private String firstMileCourierCode;
	@JsonAlias({"DestinationCode", "destinationCode"})
	private String destinationCode;
	@JsonAlias({"FlightNumber", "flightNumber"})
	private String flightNumber;
	@JsonAlias({"NumberOfPackages", "numberOfPackages"})
	private int numberOfPackages;
	@JsonAlias({"HblList", "hblList"})
	private List<HblListVE> hblList;
	@JsonAlias({"MblProperties", "mblProperties"})
	private MblProperties mblProperties;
	
	
	@Data
	public static class MblProperties{
		@JsonAlias({"ETA", "eta"})
		private String eta;
		@JsonAlias({"ETD", "etd"})
		private String etd;
	}
}


