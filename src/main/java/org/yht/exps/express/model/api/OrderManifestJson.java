package org.yht.exps.express.model.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderManifestJson {
    @JsonAlias({"OrderInfo", "orderInfo"})
    private OrderInfoJson orderInfo;

    @JsonAlias({"ItemInfo", "itemInfo"})
    private List<ItemInfoJson> itemInfo;

    @JsonAlias({"MasterInfo", "masterInfo"})
    private List<MasterInfoJson> masterInfo;

    @JsonAlias({"CustomsInfo", "customsInfo"})
    private List<CustomsInfoJson> customsInfo;

    @JsonAlias({"ContainerInfo", "containerInfo"})  
    private List<ContainerInfoJson> containerInfo;
}