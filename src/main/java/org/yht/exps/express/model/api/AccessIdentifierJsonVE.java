package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class AccessIdentifierJsonVE {
	@JsonAlias({"SenderIdentifier", "senderIdentifier"})
	private String senderIdentifier;
	@JsonAlias({"ReceiverIdentifier", "receiverIdentifier"})
	private String receiverIdentifier;
	@JsonAlias({"AccessKey", "accessKey"})
	private String accessKey;
}
