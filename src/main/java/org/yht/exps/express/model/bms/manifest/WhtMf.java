package org.yht.exps.express.model.bms.manifest;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

//import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@ApiModel(value = "보세운송 house")
@Alias("whtMf")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class WhtMf {
    ///////////////공통///////////////
    @ApiModelProperty(value="도입사 코드")
    private String entCode;
    @ApiModelProperty(value="사업장 코드")
    private String entBusinessCode;
    @ApiModelProperty(value="본지사 코드")
    private String entOfficeCode;
    @ApiModelProperty(value="업무구분")
    private String bound;
    @ApiModelProperty(value="mrn")
    private String mrn;
    @ApiModelProperty(value="IATA등록명")
    private String ownerOperator;
    @ApiModelProperty(value="flightNo")
    private String flightNo;
    @ApiModelProperty(value="dateArrive")
    private String dateArrive;
    @ApiModelProperty(value="mawb 갯수")
    private String mawbNo;
    @ApiModelProperty(value="선별여부_TMS")
    private String tmsMatchYn;
    @ApiModelProperty(value="선별여부_WMS")
    private String wmsMatchYn;

    @ApiModelProperty(value = "WHT적하목록master")
    private List<WhtMf> mList;

    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="msn")
    private String msn;
    @ApiModelProperty(value="mawb")
    private String mawb;
    @ApiModelProperty(value="fwd_code")
    private String fwdCode;
    @ApiModelProperty(value="fwd_name")
    private String fwdName;
    @ApiModelProperty(value="hawb_no")
    private String hawbNo;
    @ApiModelProperty(value="하기장소(8)+하기물품코드(2)")
    private String unloadSpot;

    @ApiModelProperty(value = "WHT적하목록house")
    private List<WhtMf> hList;

    @ApiModelProperty(value="hsn")
    private String hsn;
    @ApiModelProperty(value="hawb")
    private String hawb;
    @ApiModelProperty(value="cargoClass")
    private String cargoClass;
    @ApiModelProperty(value="pieces_no")
    private String piecesNo;
    @ApiModelProperty(value="gross_weight")
    private String grossWeight;

    @ApiModelProperty(value="송하인명")
    private String shipperName;
    @ApiModelProperty(value="통지처 국가코드")
    private String shipperNationCode;
    @ApiModelProperty(value="통지처 도시코드")
    private String shipperCityCode;
    @ApiModelProperty(value="통지처주소")
    private String shipperAddr;
    @ApiModelProperty(value="통지처 전화 팩스")
    private String shipperTelFax;
    
    @ApiModelProperty(value="consignee_code")
    private String consigneeCode;
    @ApiModelProperty(value="consignee_name")
    private String consigneeName;
    @ApiModelProperty(value="수하인 국가코드")
    private String consigneeNationCode;
    @ApiModelProperty(value="수하인 도시코드")
    private String consigneeCityCode;
    @ApiModelProperty(value="consignee addr")
    private String consigneeAddr;
    @ApiModelProperty(value="consignee tel & fax")
    private String consigneeTelFax;
    
    @ApiModelProperty(value="통지처코드")
    private String notifyCode;
    @ApiModelProperty(value="통지처상호")
    private String notifyName;
    @ApiModelProperty(value="통지처 국가코드")
    private String notifyNationCode;
    @ApiModelProperty(value="통지처 도시코드")
    private String notifyCityCode;
    @ApiModelProperty(value="통지처주소")
    private String notifyAddr;
    @ApiModelProperty(value="통지처 전화 팩스")
    private String notifyTelFax;

    @ApiModelProperty(value="warehouse_code")
    private String warehouseCode;
    @ApiModelProperty(value="tr_co_code mapas")
    private String trCoCode;

    @ApiModelProperty(value="goods_descript")
    private String goodsDescript;
    @ApiModelProperty(value="unload_lo_code")
    private String unloadLoCode;
    @ApiModelProperty(value="unload_type")
    private String unloadType;
    @ApiModelProperty(value="하기신고접수여부")
    private String acceptCode;
    @ApiModelProperty(value="등록자")
    private String userId;
    @ApiModelProperty(value="날자형식")
    private String gmtTime;
    @ApiModelProperty(value="업무구분")
    private String solution;
}