package org.yht.exps.express.model.selectMng;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "GoodsSelectionInfo", description = "통관목록 자동선별(기초)")
@Alias("goodsSelectionInfo")
@EqualsAndHashCode(callSuper=false)
public class GoodsSelectionInfo extends ResultList{
	@ApiModelProperty(value="상세품목")
	private String itemName;
	@ApiModelProperty(value="HS코드")	
	private String hsCode;
	@ApiModelProperty(value="목록여부")
	private String cdYtypeYn;
	@ApiModelProperty(value="배제여부")
	private String cdBtypeYn;
	@ApiModelProperty(value="통관구분")
	private String cdType;
	@ApiModelProperty(value="발송국가")
	private String origCountryCode;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자")
	private String updateUserName;
	@ApiModelProperty(value="수정자아이디")
	private String updateUserId;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	
	@ApiModelProperty(value="검사여부")
	private String inspectionYn;
	@ApiModelProperty(value="용도구분")
	private String cdUseType;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="마스터오더관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="일련번호")	
	private int seqNoId;
	@ApiModelProperty(value="검토완료여부")
	private String reviewYn;
	
	@ApiModelProperty(value="수정여부")
	private String modifyYn;
	@ApiModelProperty(value="오더 세부품목명")
	private String odItemName;
	@ApiModelProperty(value="오더 세부품목 HS Code")
	private String odHsCode;
	@ApiModelProperty(value="오더관리번호리스트")
	private List<String> odJobNoList;
	@ApiModelProperty(value="오더관리번호리스트(Selection)")
	private List<Selection> odJobNoSelectionList;
	@ApiModelProperty(value="USD금액_AMOUNT")
	private double amountUsd;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
}
