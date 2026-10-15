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
@ApiModel(value = "보세운송 master")
@Alias("tmtMf")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class TmtMf {
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
    @ApiModelProperty(value="항공편명")
    private String flightNo;
    @ApiModelProperty(value="최초출발일")
    private String dateArrive;
    @ApiModelProperty(value="IATA등록명")
    private String ownerOperator;
    @ApiModelProperty(value="grossWeight")
    private String grossWeight;
    @ApiModelProperty(value="grossPieces")
    private String grossPieces;
    @ApiModelProperty(value="Mawb 총개수")
    private String mawbNo;
    @ApiModelProperty(value="선별여부_TMS")
    private String tmsMatchYn;
    @ApiModelProperty(value="선별여부_WMS")
    private String wmsMatchYn;

    @ApiModelProperty(value = "IMT적하목록")
    private List<TmtMf> list;

    //sub
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="mawb")
    private String mawb;
    @ApiModelProperty(value="msn")
    private String msn;
    @ApiModelProperty(value="loadPortName")
    private String loadPortName;
    @ApiModelProperty(value="loadIataCode")
    private String loadIataCode;
    @ApiModelProperty(value="unldPortName")
    private String unldPortName;
    @ApiModelProperty(value="unldIataCode")
    private String unldIataCode;
    @ApiModelProperty(value="cargoClass")
    private String cargoClass;
    @ApiModelProperty(value="mawbType")
    private String mawbType;
    @ApiModelProperty(value="pieces_no")
    private String piecesNo;
    @ApiModelProperty(value="weight_KG")
    private String weightKg;
    @ApiModelProperty(value="goods_descript")
    private String goodsDescript;
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
    @ApiModelProperty(value="danger_code1")
    private String dangerCode1;
    @ApiModelProperty(value="danger_code2")
    private String dangerCode2;
    @ApiModelProperty(value="warehouse_code")
    private String warehouseCode;
    @ApiModelProperty(value="하기장소(8)+하기물품코드(2)")
    private String unloadSpot;
    @ApiModelProperty(value="하기신고접수여부")
    private String acceptCode;
    @ApiModelProperty(value="등록자")
    private String userId;
    @ApiModelProperty(value="날자형식")
    private String gmtTime;
    @ApiModelProperty(value="업무구분")
    private String solution;
}