package org.yht.exps.express.model.sms.logs.search;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchElastic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchInterfaceLogs", description = "인터페이스로그 리스트 검색 조건")
@Alias("searchInterfaceLogs")
public class SearchInterfaceLogs extends SearchElastic {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="처리종류")
	private String srchIfDocCode;
	@ApiModelProperty(value="처리자 ID")
	private String srchRegisterUserId;
	@ApiModelProperty(value="업무")
	private String srchSolution;
	@ApiModelProperty(value="해상/항공")
	private String srchBound;
	@ApiModelProperty(value="수출입")
	private String srchBoundType;
	@ApiModelProperty(value="Reference No")
	private String srchIfDocReferenceNo;
	@ApiModelProperty(value="송수신구분")
	private String srchIfSendRecvType;
	@ApiModelProperty(value="H.B/L No")
	private String srchHblNo;
	@ApiModelProperty(value="senderId")
	private String srchIfSenderId;
	@ApiModelProperty(value="receiverId")
	private String srchIfReceiverId;
	@ApiModelProperty(value="성공실패여부")
	private String srchIfStatus;
	@ApiModelProperty(value="사용자권한")
	private String userAuth;
	
	
	@ApiModelProperty(value="삭제:검색건(srchData)/선택건(chkData)")
	private String targetData;
	@ApiModelProperty(value="삭제:선택건 리스트")
	private List<Map<String, Object>> checkDataList;
	@ApiModelProperty(value="검색건 삭제 시 검색데이터 건수")
	private int srchDataCnt;
}
