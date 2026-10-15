package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AccessIdentifierJson {
    @JsonAlias({"SenderIdentifier", "senderIdentifier"})
    private String senderIdentifier;

    @JsonAlias({"ReceiverIdentifier", "receiverIdentifier"})
    private String receiverIdentifier;

    @JsonAlias({"AccessKey", "accessKey"})
    private String accessKey;

    @JsonAlias({"Bound", "bound"})
    private String bound;

    @JsonAlias({"BoundType", "boundType"})
    private String boundType;
}