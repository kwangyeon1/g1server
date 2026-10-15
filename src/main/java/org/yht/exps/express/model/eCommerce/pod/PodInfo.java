package org.yht.exps.express.model.eCommerce.pod;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.util.Util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "PodInfo", description = "전자상거래 Pod")
@Alias("podInfo")
@EqualsAndHashCode(callSuper=false)
public class PodInfo extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="마스터관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="항공 / 해상")
	private String bound;
	@ApiModelProperty(value="수출입구분")
	private String boundType;
	@ApiModelProperty(value="요청ID")
	private String requestId;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="상위업체명")
	private String custNameLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="하위업체명")
	private String custNameLv2;
	@ApiModelProperty(value="주문일")
	private String poDate;
	@ApiModelProperty(value="M.B/L No.")
	private String mblNo;
	@ApiModelProperty(value="운송장번호")
	private String hblNo;
	@ApiModelProperty(value="주문번호")
	private String poNo;
	@ApiModelProperty(value="입항일")
	private String arDate;
	@ApiModelProperty(value="배송사")
	private String deliveryCode;
	@ApiModelProperty(value="배송사명")
	private String deliveryName;
	@ApiModelProperty(value="반품송장번호")
	private String returnDeliveryNo;
	@ApiModelProperty(value="오더접수")
	private String ecm010;
	@ApiModelProperty(value="픽업")
	private String ecm020;
	@ApiModelProperty(value="입고")
	private String ecm005;
	@ApiModelProperty(value="출고")
	private String ecm007;
	@ApiModelProperty(value="출발")
	private String ecm060;
	@ApiModelProperty(value="도착")
	private String ecm070;
	@ApiModelProperty(value="반입")
	private String ecm210;
	@ApiModelProperty(value="통관부호오류")
	private String customsIdError;
	@ApiModelProperty(value="통관시작")
	private String customsStart;
	@ApiModelProperty(value="통관완료")
	private String customsClear;
	@ApiModelProperty(value="반출")
	private String ecm290;
	@ApiModelProperty(value="배송접수")
	private String ecm360;
	@ApiModelProperty(value="택배집하")
	private String ecm361;
	@ApiModelProperty(value="간선이동")
	private String ecm370;
	@ApiModelProperty(value="간선이동중")
	private String ecm380;
	@ApiModelProperty(value="배송출발")
	private String ecm390;
	@ApiModelProperty(value="미배송")
	private String ecm400;
	@ApiModelProperty(value="배송완료")
	private String ecm420;
	@ApiModelProperty(value="반품지시")
	private String ecm510;
	@ApiModelProperty(value="미배송반품")
	private String ecm550;
	@ApiModelProperty(value="택배집하_반품")
	private String ecm520;
	@ApiModelProperty(value="배송출발_반품")
	private String ecm530;
	@ApiModelProperty(value="배송완료_반품")
	private String ecm540;
	@ApiModelProperty(value="통관부호오류_전송시간")
	private String ctCustomsIdError;
	@ApiModelProperty(value="통관시작_전송시간")
	private String ctCustomsStart;
	@ApiModelProperty(value="통관완료_전송시간")
	private String ctCustomsClear;
	@ApiModelProperty(value="택배집하_전송시간")
	private String ctEcm361;
	@ApiModelProperty(value="간선이동_전송시간")
	private String ctEcm370;
	@ApiModelProperty(value="간선이동중_전송시간")
	private String ctEcm380;
	@ApiModelProperty(value="배송출발_전송시간")
	private String ctEcm390;
	@ApiModelProperty(value="배송완료_전송시간")
	private String ctEcm420;
	@ApiModelProperty(value="반품지시_전송시간")
	private String ctEcm510;
	@ApiModelProperty(value="미배송반품_전송시간")
	private String ctEcm550;
	@ApiModelProperty(value="택배집하_반품_전송시간")
	private String ctEcm520;
	@ApiModelProperty(value="배송출발_반품_전송시간")
	private String ctEcm530;
	@ApiModelProperty(value="배송완료_반품_전송시간")
	private String ctEcm540;
	@ApiModelProperty(value="입력자")
	private String registerUserId;	
	@ApiModelProperty(value="입력시간")
	private String registerDateTime;
	@ApiModelProperty(value="갱신자")
	private String updateUserId;
	@ApiModelProperty(value="갱신시간")
	private String updateDateTime;
	@ApiModelProperty(value="날짜형식")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String srchDateFormat;
	@ApiModelProperty(value="포장수량")
	private String pieces;
	@ApiModelProperty(value="송하인명")
	private String shipperName;
	@ApiModelProperty(value="수하인명")
	private String consigneeNameLoc;
	@ApiModelProperty(value="중량")
	private String gwt;
	
	private String getCombinedValue(String value, String ctValue) {
        String dataValue = Util.null2str(value).trim();
        String ctDataValue = Util.null2str(ctValue).trim();
        
        if (!dataValue.isEmpty() && !ctDataValue.isEmpty()) {
            return dataValue + "\n" + ctDataValue; 
        }
        if (!dataValue.isEmpty()) return dataValue; 
        return ctDataValue; 
    }

	public String getCustomsError() {
        return getCombinedValue(this.customsIdError, this.ctCustomsIdError);
    }
	
    public String getCustomsStart() {
        return getCombinedValue(this.customsStart, this.ctCustomsStart);
    }
    
    public String getCustomsClear() {
        return getCombinedValue(this.customsClear, this.ctCustomsClear);
    }

    public String getEcm361() { // 택배집하
        return getCombinedValue(this.ecm361, this.ctEcm361);
    }

    public String getEcm370() { // 간선이동
        return getCombinedValue(this.ecm370, this.ctEcm370);
    }

    public String getEcm380() { // 간선이동중
        return getCombinedValue(this.ecm380, this.ctEcm380);
    }

    public String getEcm390() { // 배송출발
        return getCombinedValue(this.ecm390, this.ctEcm390);
    }

    public String getEcm420() { // 배송완료
        return getCombinedValue(this.ecm420, this.ctEcm420);
    }
    public String getEcm510() { // 반품지시
        return getCombinedValue(this.ecm510, this.ctEcm510);
    }

    public String getEcm550() { // 미배송반품
        return getCombinedValue(this.ecm550, this.ctEcm550);
    }

    public String getEcm520() { // 택배집하_반품
        return getCombinedValue(this.ecm520, this.ctEcm520);
    }

    public String getEcm530() { // 배송출발_반품
        return getCombinedValue(this.ecm530, this.ctEcm530);
    }

    public String getEcm540() { // 배송완료_반품
        return getCombinedValue(this.ecm540, this.ctEcm540);
    }
	
}
