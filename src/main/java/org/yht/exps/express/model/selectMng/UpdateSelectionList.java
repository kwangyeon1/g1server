package org.yht.exps.express.model.selectMng;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UpdateSelectionList", description = "전자상거래 목록선별 리스트")
@Alias("updateSelectionList")
public class UpdateSelectionList {
	
	@ApiModelProperty(value="업데이트리스트")
	private List<Selection> selectionList;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="도입사명")
	private String entName;
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
	@ApiModelProperty(value="처리구분")
	private String action;
	
	@ApiModelProperty(value="선별마감여부")
	private String selectionYn;
	@ApiModelProperty(value="선별마감대상관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="자동선별비율")
	private String srchRatio;
	@ApiModelProperty(value="발송국가")
	private String origCountryCode;
	@ApiModelProperty(value="자동선별여부")
	private String autoSelectionYn;
	@ApiModelProperty(value="선별종류")
	private String selectionType;
	@ApiModelProperty(value="다른메뉴선별임시저장삭제여부")
	private String deleteOtherSelectionYn;
	@ApiModelProperty(value="관리번호 삭제 리스트")
	private List<String> deleteOdJobNoList;
//	@ApiModelProperty(value="업데이트 HsCode 리스트")
//	private List<GoodsSelectionInfo> selectionHsList;
	@ApiModelProperty(value="업데이트아이템리스트")
	private List<GoodsSelectionInfo> selectionItemList;
	@ApiModelProperty(value="관세사E-MAIL안내여부")
	private String emailYn;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}
