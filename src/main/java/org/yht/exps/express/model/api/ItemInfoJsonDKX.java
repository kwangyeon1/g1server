package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ItemInfoJsonDKX {
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
    
    
    //DKX추가
    @JsonAlias({"ItemSeq", "itemSeq"})
    private String itemSeq;
    @JsonAlias({"ItemId", "itemId"})
    private String itemId;
    @JsonAlias({"OrderNo", "orderNo"})
    private String orderNo;
    @JsonAlias({"OrderEndDatetime", "orderEndDatetime"})
    private String orderEndDatetime;
    @JsonAlias({"CustomsId", "customsId"})
    private String customsId;
    @JsonAlias({"CustomsTempNo", "customsTempNo"})
    private String customsTempNo;
    @JsonAlias({"CategoryName1", "categoryName1"})
    private String categoryName1;
    @JsonAlias({"CategoryName2", "categoryName2"})
    private String categoryName2;
    @JsonAlias({"CategoryName3", "categoryName3"})
    private String categoryName3;
    @JsonAlias({"CategoryName4", "categoryName4"})
    private String categoryName4;
    @JsonAlias({"CategoryName5", "categoryName5"})
    private String categoryName5;
    @JsonAlias({"CategoryName6", "categoryName6"})
    private String categoryName6;
    @JsonAlias({"CategoryName7", "categoryName7"})
    private String categoryName7;
    @JsonAlias({"CategoryName8", "categoryName8"})
    private String categoryName8;
    @JsonAlias({"CategoryName9", "categoryName9"})
    private String categoryName9;
    @JsonAlias({"OptionName", "optionName"})
    private String optionName;
    @JsonAlias({"SellerName", "sellerName"})
    private String sellerName;
    @JsonAlias({"SellerId", "sellerId"})
    private String sellerId;
    @JsonAlias({"SellerCode", "sellerCode"})
    private String sellerCode;
    @JsonAlias({"ItemMafName", "itemMafName"})
    private String itemMafName;
    @JsonAlias({"ItemMafModelNo", "itemMafModelNo"})
    private String itemMafModelNo;
    @JsonAlias({"ItemBrandName", "itemBrandName"})
    private String itemBrandName;
}