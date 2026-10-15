package org.yht.exps.express.model.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class OrderManifestJsonDKX {
    @JsonAlias({"OrderInfo", "orderInfo"})
    private OrderInfoJsonDKX orderInfo;

    @JsonAlias({"ItemInfo", "itemInfo"})
    private List<ItemInfoJsonDKX> itemInfo;

    @JsonAlias({"MasterInfo", "masterInfo"})
    private List<MasterInfoJson> masterInfo;

    @JsonAlias({"CustomsInfo", "customsInfo"})
    private List<CustomsInfoJson> customsInfo;

    @JsonAlias({"ContainerInfo", "containerInfo"})  
    private List<ContainerInfoJson> containerInfo;
}