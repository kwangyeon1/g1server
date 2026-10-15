package org.yht.exps.express.model.api;

import java.util.List;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "OrderInterfaceJson", description = "Order Interface Info(DKX)")
@Alias("OrderInterfaceJsonDKX")
public class OrderInterfaceJsonDKX {
    @JsonAlias({"AccessIdentifier", "accessIdentifier"})
    private AccessIdentifierJson accessIdentifier;
    @JsonAlias({"OrderManifest", "orderManifest"})
    private List<OrderManifestJsonDKX> orderManifest;
}