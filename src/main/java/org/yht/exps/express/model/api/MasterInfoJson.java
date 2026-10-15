package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;;

@Data
public class MasterInfoJson {
	@JsonAlias({"MBLNo", "mblNo"})
	private String MblNo;
	@JsonAlias({"FlightNo", "flightNo"})
	private String FlightNo;
	@JsonAlias({"ETD", "etd"})
	private String ETD;
	@JsonAlias({"ETA", "eta"})
	private String ETA;
	@JsonAlias({"DepPort", "depPort"})
	private String DepPort;
	@JsonAlias({"ArrPort", "arrPort"})
	private String ArrPort;
	@JsonAlias({"HBLCount", "hBLCount"})
	private String HblCount;
}

