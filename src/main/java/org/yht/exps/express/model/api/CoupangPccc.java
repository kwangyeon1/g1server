package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class CoupangPccc {
	@JsonAlias({"RequestId", "requestId"})
	private String requestId;
	@JsonAlias({"CoupangOrderId", "coupangOrderId"})
	private String coupangOrderId;
	@JsonAlias({"OrderedAt", "orderedAt"})
	private String orderedAt;
	@JsonAlias({"Pccc", "pccc"})
	private String pccc;
	@JsonAlias({"ReceiverPhoneNumber", "receiverPhoneNumber"})
	private String receiverPhoneNumber;
	@JsonAlias({"ReceiverName", "receiverName"})
	private String receiverName;
	@JsonAlias({"OrdererName", "ordererName"})
	private String ordererName;
	@JsonAlias({"Zipcode", "zipcode"})
	private String zipcode;
	
	private String gubun;
}
