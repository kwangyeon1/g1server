package org.yht.exps.express.model.selectMng;

import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.exps.express.model.eCommerce.order.OrderItem;
import org.yht.util.Util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ApiModel(value = "Selection", description = "전자상거래 선별 리스트")
@Alias("selection")
@EqualsAndHashCode(callSuper=false)
public class Selection extends ResultList implements Cloneable{
	@ApiModelProperty(value="자동선별내역")
	private String checkInfo;
	@ApiModelProperty(value="H.B/L No")
	private String hblNo;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="서류여부")
	private String documentYn;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="IMPORTER")
	private String actualCstName;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="Inner Pieces (실제수량, EDI신고용)")
	private double piecesReal;
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="금액")
	private double amountLoc;
	@ApiModelProperty(value="금액(CUR)")
	private double amountCur;
	@ApiModelProperty(value="금액(USD)")
	private double amountUsd;
	@ApiModelProperty(value="통관구분")
	private String cdType;
	@ApiModelProperty(value="오더정보")
	private Map<String, Object> orderData;
	@ApiModelProperty(value="검사")
	private String inspectionYn;
	@ApiModelProperty(value="용도구분")
	private String cdUseType;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="품명")
	private String commodityItem;
	@ApiModelProperty(value="선별품명")
	private String selectionItemName;
	@ApiModelProperty(value="품목코드")
	private String allowedItemCode;
	@ApiModelProperty(value="전자상거래주소")
	private String url;
	@ApiModelProperty(value="수하인명")
	private String consigneeName;
	@ApiModelProperty(value="수하인주소")
	private String consigneeAddress;
	@ApiModelProperty(value="개인통관부호")
	private String customsId;
	@ApiModelProperty(value="수하인전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수하인국가코드")
	private String consigneeCountryCode;
	@ApiModelProperty(value="송하인명")
	private String shipperName;
	@ApiModelProperty(value="송하인주소")
	private String shipperAddress;
	@ApiModelProperty(value="송하인전화번호")
	private String shipperTel;
	@ApiModelProperty(value="송하인국가코드")
	private String shipperCountryCode;
	@ApiModelProperty(value="송하인사업자번호")
	private String shipperBusinessId;
	@ApiModelProperty(value="발송국가")
	private String origCountryCode;
	@ApiModelProperty(value="도착지")
	private String destCode;
	@ApiModelProperty(value="도착지국가")
	private String destCountry;
	@ApiModelProperty(value="과태료구분")
	private String penaltyYn;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="자동선별여부")
	private String autoSelectionYn;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="마스터관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="우범구분")
	private String crimeAreaYn;
	@ApiModelProperty(value="데이터변경여부")
	private String dataChangeYn;
	@ApiModelProperty(value="선별타입")
	private String selectionType;
	@ApiModelProperty(value="자동선별비율")
	private String srchRatio;
	@ApiModelProperty(value="선별관리번호")
	private String selectionOdJobNo;
	@ApiModelProperty(value="선별일련번호")
	private String seqNoId;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="제출번호")
	private String simpleDocNo;
	
	
	/** 엑셀 다운로드 관련 */
	@ApiModelProperty(value="상세품명")
	private OrderItem selectionItem;
	
	public Selection clone() throws CloneNotSupportedException{
		Selection vo = null;
		try {
			vo = (Selection) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(Util.getPrintStacTraceString(e));
			throw e;
		}
		return vo;
	}
}
