package org.yht.exps.express.model.bms.bmsXraySend;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "xraySendLog", description = "xray 재전송 리스트")
@Alias("xraySendLog")
@EqualsAndHashCode(callSuper=false)
public class XraySendLog {
	@ApiModelProperty(value="seq")
	private String seq;
	@ApiModelProperty(value="packingNo")
	private String packingNo;
	@ApiModelProperty(value="전송상태")
	private String sendStatus;
	@ApiModelProperty(value="blNo")
	private String blNo;
    @ApiModelProperty(value="재전송일시")
    private String updateDateTime;
    @ApiModelProperty(value="전송타입")
    private String sendType;
    @ApiModelProperty(value="전송타입명")
    private String sendTypeName;
    
	@ApiModelProperty(value="ES Index")
	private String index;
	@ApiModelProperty(value="내부관리번호")
	private String referenceNoId;
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="업무", example="COM, SMS, FMS..")
	private String solution;
	@ApiModelProperty(value="해상/항공", example="COM, AIR, SEA")
	private String bound;
	@ApiModelProperty(value="수출입", example="COM, IMP, EXP")
	private String boundType;
	@ApiModelProperty(value="처리자 ID")
	private String registerUserId;
	@ApiModelProperty(value="처리자명")
	private String registerUserName;
	@ApiModelProperty(value="처리일시")
	private long registerDateTime;
	@ApiModelProperty(value="처리일시_문자열로변환")
	private String registerDateTimeS;
	@ApiModelProperty(value="파일내용")
	private String fileData;
	@ApiModelProperty(value="파일경로")
	private String ifFilePath;
	@ApiModelProperty(value="파일명")
	private String ifFileName;
	@ApiModelProperty(value="처리종류")
	private String ifServiceCode;
	@ApiModelProperty(value="처리상태")
	private String ifStatus;
	@ApiModelProperty(value="doc Reference No")
	private String ifDocReferenceNo;
	@ApiModelProperty(value="H.B/L No")
	private String hblNo;
	@ApiModelProperty(value="비고사항")
	private String ifRemark;
	@ApiModelProperty(value="관리번호")
	private String esJobNo;
	@ApiModelProperty(value="업무코드")
	private String ifDocCode;
	@ApiModelProperty(value="업무명")
	private String ifDocName;
	@ApiModelProperty(value="중계사업자")
	private String ifVender;
	@ApiModelProperty(value="송수신구분")
	private String ifSendRecvType;
	@ApiModelProperty(value="관세청EDI전송결과코드")
	private String customsErrCode;
	@ApiModelProperty(value="관세청EDI전송결과")
	private String customsErrDesc;
	@ApiModelProperty(value="관세청EDI전송결과추가정보")
	private String customsErrDescAdditional;
	
}
