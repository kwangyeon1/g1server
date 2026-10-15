package org.yht.exps.express.model.bms.statistics.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchStatistics", description = "통계조회")
@Alias("searchStatistics")
public class SearchStatistics extends SearchList {
    @ApiModelProperty(value="조회일자 구분")
    private String srchDateGbn;
    @ApiModelProperty(value="조회 시작일자")
    private String srchStartDate;
    @ApiModelProperty(value="조회 종료일자")
    private String srchEndDate;

    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String srchEntCode;
    @ApiModelProperty(value="조회번호")
    private String srchNo;
    @ApiModelProperty(value="조회번호타입")
    private String srchNoType;
    @ApiModelProperty(value="조회타입 멀티/싱글")
    private String searchType;
    @ApiModelProperty(value="srchNo N개씩 조회시 리스트")
    private List<String> searchNoList;
    @ApiModelProperty(value="통계상태")
    private String srchStatisticState;

    /**반입그룹항목*/
    @ApiModelProperty(value="반입그룹항목 리스트")
    private List<String> srchInGroupCodes;
    @ApiModelProperty(value="반입그룹항목1")
    private String srchInGroupCode1;
    @ApiModelProperty(value="반입그룹항목2")
    private String srchInGroupCode2;
    @ApiModelProperty(value="반입그룹항목3")
    private String srchInGroupCode3;
    @ApiModelProperty(value="반입그룹항목4")
    private String srchInGroupCode4;
    @ApiModelProperty(value="반입그룹항목5")
    private String srchInGroupCode5;
    @ApiModelProperty(value="반입그룹항목6")
    private String srchInGroupCode6;
    /**반출그룹항목*/
    @ApiModelProperty(value="반출그룹항목 리스트")
    private List<String> srchOutGroupCodes;
    @ApiModelProperty(value="반출그룹항목1")
    private String srchOutGroupCode1;
    @ApiModelProperty(value="반출그룹항목2")
    private String srchOutGroupCode2;
    @ApiModelProperty(value="반출그룹항목3")
    private String srchOutGroupCode3;
    @ApiModelProperty(value="반출그룹항목4")
    private String srchOutGroupCode4;
    @ApiModelProperty(value="반출그룹항목5")
    private String srchOutGroupCode5;
    @ApiModelProperty(value="반출그룹항목6")
    private String srchOutGroupCode6;

    //거래처 검색
    @ApiModelProperty(value="거래처구분", example="화주 or 송하인 or 수하인...")
    private String srchCustomerType;
    @ApiModelProperty(value="검색거래처코드(화주)", example="거래처코드(화주)")
    private String srchCustomerCode;
    @ApiModelProperty(value="검색거래처명(화주)", example="거래처명(화주)")
    private String srchCustomerName;
    @ApiModelProperty(value="검색거래처코드(송화인)", example="거래처코드(송화인)")
    private String srchShipperCode;
    @ApiModelProperty(value="검색거래처명(송화인)", example="거래처명(송화인)")
    private String srchShipperName;
    @ApiModelProperty(value="검색거래처코드(수화인)", example="거래처코드(수화인)")
    private String srchConsigneeCode;
    @ApiModelProperty(value="검색거래처명(수화인)", example="거래처명(수화인)")
    private String srchConsigneeName;
    @ApiModelProperty(value="검색거래처코드(통지처)", example="거래처코드(통지처)")
    private String srchNotifyCode;
    @ApiModelProperty(value="검색거래처명(통지처)", example="거래처명(통지처)")
    private String srchNotifyName;
    @ApiModelProperty(value="검색거래처코드(배송사)", example="거래처코드(배송사)")
    private String srchDeliveryCode;
    @ApiModelProperty(value="검색거래처명(배송사)", example="거래처명(배송사)")
    private String srchDeliveryName;
    @ApiModelProperty(value="검색거래처코드(포워더)", example="거래처코드(포워더)")
    private String srchFwdCode;
    @ApiModelProperty(value="검색거래처명(포워더)", example="거래처명(포워더)")
    private String srchFwdName;
    @ApiModelProperty(value="검색거래처코드(관세사)", example="거래처코드(관세사)")
    private String srchBrokerCode;
    @ApiModelProperty(value="검색거래처명(관세사)", example="거래처명(관세사)")
    private String srchBrokerName;
    @ApiModelProperty(value="검색거래처명(장치위치)", example="거래처명(장치위치)")
    private String srchLocationPlace;
    @ApiModelProperty(value="검색거래처명(비고1)", example="거래처명(비고1)")
    private String srchRemarks1;
    @ApiModelProperty(value="검색거래처명(비고2)", example="거래처명(비고2)")
    private String srchRemarks2;
    @ApiModelProperty(value="검색거래처명(비고3)", example="거래처명(비고3)")
    private String srchRemarks3;
    @ApiModelProperty(value="검색상품명", example="검색상품명")
    private String srchCommodityItemLoc;

    /** 엑셀다운 */
    @ApiModelProperty(value="문서양식코드(엑셀다운로드)")
    private String documentCode;
    @ApiModelProperty(value="문서명(엑셀다운로드)")
    private String documentName;

    @ApiModelProperty(value="반/출입 type")
    private String carryType;
}
