package org.yht.exps.express.model.api;

import java.util.List;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "OrderInterfaceJson", description = "Order Interface Info")
@Alias("OrderInterfaceJson")
public class OrderInterfaceJson {
    @JsonAlias({"AccessIdentifier", "accessIdentifier"})
    private AccessIdentifierJson accessIdentifier;
    @JsonAlias({"OrderManifest", "orderManifest"})
    private List<OrderManifestJson> orderManifest;
}