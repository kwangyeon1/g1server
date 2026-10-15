package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ContainerInfoJson {
    @JsonAlias({"ContainerNo", "containerNo"})
    private String containerNo;
    @JsonAlias({"ContainerType", "containerType"})
    private String containerType;
    @JsonAlias({"SealNo1", "sealNo1"})
    private String sealNo1;
    @JsonAlias({"SealNo2", "sealNo2"})
    private String sealNo2;
    @JsonAlias({"SealNo3", "sealNo3"})
    private String sealNo3;
    @JsonAlias({"Qty", "qty"})
    private Integer qty;
    @JsonAlias({"QtyUnit", "qtyUnit"})
    private String qtyUnit;
    @JsonAlias({"GrossWeight", "grossWeight"})
    private String grossWeight;
	@JsonProperty("CBM")
    @JsonAlias({"CBM", "cbm"})
    private String cbm;
}