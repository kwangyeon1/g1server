package org.yht.exps.express.model.eCommerce.bondedTrans;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BondedTrans", description = "고객서비스 보세운송")
@Alias("bondedTrans")
@EqualsAndHashCode(callSuper=false)
public class BondedTrans extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="수출입")
	private String boundType;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="상위업체명")
	private String custNameLv1;
	@ApiModelProperty(value="하위업체명")
	private String custNameLv2;
	@ApiModelProperty(value="화주_명칭")
	private String consigneeName;
	@ApiModelProperty(value="M.B/L No.")
	private String mblNo;
	@ApiModelProperty(value="H.B/L No.")
	private String hblNo;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="보세운송신고일")
	private String bondedTransDate;
	@ApiModelProperty(value="보세운송유형")
	private String bondedTransType;
	@ApiModelProperty(value="검사취하")
	private String inspectionDrop;
	@ApiModelProperty(value="운송일자")
	private String transportDate;
	@ApiModelProperty(value="반출일")
	private String carryoutDate;
	@ApiModelProperty(value="반출비고")
	private String carryoutRemark;
	
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자코드")
	private String updateUserId;
	
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자명")
	private String logUserNameEng;
	
	
	@ApiModelProperty(value="odJobNoList")
	private List<String> odJobNoList;
	
}
