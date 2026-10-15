package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ItemInfoJson {
    @JsonAlias({"ItemNo", "itemNo"})
    private String itemNo;
    @JsonAlias({"ItemNm", "itemNm"})
    private String itemNm;
    @JsonAlias({"ItemNmLocal", "itemNmLocal"})
    private String itemNmLocal;
    @JsonAlias({"ItemModelNm", "itemModelNm"})
    private String itemModelNm;
    @JsonAlias({"ItemModelNmLocal", "itemModelNmLocal"})
    private String itemModelNmLocal;
    @JsonAlias({"ItemQty", "itemQty"})
    private String itemQty;
    @JsonAlias({"ItemQtyUnit", "itemQtyUnit"})
    private String itemQtyUnit;
    @JsonAlias({"ItemGrossWeight", "itemGrossWeight"})
    private String itemGrossWeight;
    @JsonAlias({"ItemRate", "itemRate"})
    private String itemRate;
    @JsonAlias({"ItemAmount", "itemAmount"})
    private String itemAmount;
    @JsonAlias({"ItemCurrencyCode", "itemCurrencyCode"})
    private String itemCurrencyCode;
    @JsonAlias({"ItemHsCode", "itemHsCode"})
    private String itemHsCode;
    @JsonAlias({"ItemOrigin", "itemOrigin"})
    private String itemOrigin;
    @JsonAlias({"ItemUrl", "itemUrl"})
    private String itemUrl;
    @JsonAlias({"ItemRemark", "itemRemark"})
    private String itemRemark;
}