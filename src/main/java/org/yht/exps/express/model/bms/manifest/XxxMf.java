package org.yht.exps.express.model.bms.manifest;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@ApiModel(value = "해상 수입 적하목록")
@Alias("xxxMf")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class XxxMf {

	///////////////공통///////////////
	@ApiModelProperty(value="도입사 코드")
    private String entCode;
    @ApiModelProperty(value="사업장 코드")
    private String entBusinessCode;
    @ApiModelProperty(value="본지사 코드")
    private String entOfficeCode;
    @ApiModelProperty(value="업무구분")
    private String bound;
    @ApiModelProperty(value="등록자")
    private String userId;
    @ApiModelProperty(value="날자형식")
    private String gmtTime;
    @ApiModelProperty(value="업무구분")
    private String solution;
    
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    //////////////////////////////////
    
    
    // XXX-master.json 에 해당하는 필드
    @ApiModelProperty(value = "Type A1")
    private String typeA1;

    @ApiModelProperty(value = "Mrn")
    private String mrn;

    @ApiModelProperty(value = "Customs Code")
    private String customsCode;

    @ApiModelProperty(value = "Customs Dpt Code")
    private String customsDptCode;

    @ApiModelProperty(value = "Vessel Code")
    private String vesselCode;

    @ApiModelProperty(value = "Voyage")
    private String voyage;

    @ApiModelProperty(value = "Callsign")
    private String callsign;

    @ApiModelProperty(value = "Vessel Nation")
    private String vesselNation;

    @ApiModelProperty(value = "Unloading Port")
    private String unloadingPort;

    @ApiModelProperty(value = "Ar Date")
    private String arDate;

    @ApiModelProperty(value = "Bl Cnt")
    private String blCnt;
    
    @ApiModelProperty(value = "XXX적하목록master")
    private List<XxxMf> mList;

    // XXX-mbl.json 에 해당하는 필드
    @ApiModelProperty(value = "Type A2")
    private String typeA2;

    @ApiModelProperty(value = "Msn")
    private String msn;

    @ApiModelProperty(value = "Mawb")
    private String mawb;

    @ApiModelProperty(value = "Hsn")
    private String hsn;

    @ApiModelProperty(value = "Hawb")
    private String hawb;

    @ApiModelProperty(value = "Unloading No")
    private String unloadingNo;

    @ApiModelProperty(value = "Mbl Type")
    private String mblType;

    @ApiModelProperty(value = "Cargo Class")
    private String cargoClass;

    @ApiModelProperty(value = "Carrier Code")
    private String carrierCode;

    @ApiModelProperty(value = "Carrier Sign")
    private String carrierSign;

    @ApiModelProperty(value = "Carrier Name")
    private String carrierName;

    @ApiModelProperty(value = "Loading Port")
    private String loadingPort;

    @ApiModelProperty(value = "Destination Port")
    private String destinationPort;

    @ApiModelProperty(value = "Unloading Goods Code")
    private String unloadingGoodsCode;

    @ApiModelProperty(value = "Unloading Loc")
    private String unloadingLoc;

    @ApiModelProperty(value = "Bwh Loc")
    private String bwhLoc;

    @ApiModelProperty(value = "Bwh Id")
    private String bwhId;

    @ApiModelProperty(value = "Goods")
    private String goods;

    @ApiModelProperty(value = "Ct")
    private String ct;

    @ApiModelProperty(value = "Ct Unit")
    private String ctUnit;

    @ApiModelProperty(value = "Wt")
    private String wt;

    @ApiModelProperty(value = "Cbm")
    private String cbm;

    @ApiModelProperty(value = "Lc No")
    private String lcNo;

    @ApiModelProperty(value = "Special Code 1")
    private String specialCode1;

    @ApiModelProperty(value = "Special Code 2")
    private String specialCode2;

    @ApiModelProperty(value = "Special Code 3")
    private String specialCode3;

    @ApiModelProperty(value = "Tr Code")
    private String trCode;

    @ApiModelProperty(value = "Cargo Type")
    private String cargoType;

    @ApiModelProperty(value = "Unloading Date Time")
    private String unloadingDateTime;

    @ApiModelProperty(value = "Detector Loc")
    private String detectorLoc;

    @ApiModelProperty(value = "Detector Inspection Class")
    private String detectorInspectionClass;

    @ApiModelProperty(value = "Inspect Yn")
    private String inspectYn;

    @ApiModelProperty(value = "Type A3")
    private String typeA3;

    @ApiModelProperty(value = "Shp Name")
    private String shpName;

    @ApiModelProperty(value = "Shp Addr")
    private String shpAddr;

    @ApiModelProperty(value = "Shp Nation")
    private String shpNation;

    @ApiModelProperty(value = "Ship City")
    private String shipCity;

    @ApiModelProperty(value = "Shp Tel")
    private String shpTel;

    @ApiModelProperty(value = "Type A4")
    private String typeA4;

    @ApiModelProperty(value = "Cne Regno")
    private String cneRegno;

    @ApiModelProperty(value = "Cne Name")
    private String cneName;

    @ApiModelProperty(value = "Cne Addr")
    private String cneAddr;

    @ApiModelProperty(value = "Cne Nation")
    private String cneNation;

    @ApiModelProperty(value = "Cne City")
    private String cneCity;

    @ApiModelProperty(value = "Cne Tel")
    private String cneTel;

    @ApiModelProperty(value = "Type A5")
    private String typeA5;

    @ApiModelProperty(value = "Nfy Regno")
    private String nfyRegno;

    @ApiModelProperty(value = "Nfy Name")
    private String nfyName;

    @ApiModelProperty(value = "Nfy Addr")
    private String nfyAddr;

    @ApiModelProperty(value = "Nfy Nation")
    private String nfyNation;

    @ApiModelProperty(value = "Nfy City")
    private String nfyCity;

    @ApiModelProperty(value = "Nfy Tel")
    private String nfyTel;

    @ApiModelProperty(value = "Cntr Cnt")
    private String cntrCnt;

    @ApiModelProperty(value = "XXX적하목록 Container")
    private List<XxxMf> cntrList;
    
    // XXX-cntr.json 에 해당하는 필드
    @ApiModelProperty(value = "Type B1")
    private String typeB1;

    @ApiModelProperty(value = "Cntr No")
    private String cntrNo;

    @ApiModelProperty(value = "Cntr Type")
    private String cntrType;

    @ApiModelProperty(value = "Seal No 1")
    private String sealNo1;

    @ApiModelProperty(value = "Seal No 2")
    private String sealNo2;

    @ApiModelProperty(value = "Seal No 3")
    private String sealNo3;

    @ApiModelProperty(value = "Cntr Ct")
    private String cntrCt;

    @ApiModelProperty(value = "Detector Inspection Yn")
    private String detectorInspectionYn;

}
