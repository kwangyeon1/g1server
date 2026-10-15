package org.yht.exps.express.model.selectMng;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SaveGoodsSelectionInfo", description = "통관목록 자동선별(기초) 저장")
@Alias("saveGoodsSelectionInfo")
public class SaveGoodsSelectionInfo {
	
	@ApiModelProperty(value="업데이트리스트")
	private List<GoodsSelectionInfo> goodsSelectionInfoList;
	@ApiModelProperty(value="관리번호리스트")
	private List<String> odJobNoList;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="수정자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="수정자아이디")
	private String updateUserId;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}
