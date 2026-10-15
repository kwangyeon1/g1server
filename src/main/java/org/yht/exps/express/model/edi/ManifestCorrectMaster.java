package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestCorrectMaster", description = "전자상거래  적하목록정정 마스터리스트")
@Alias("manifestCorrectMaster")
public class ManifestCorrectMaster {
	
	@ApiModelProperty(value="적하목록정정_관리번호")
	private String modJobNo;
	@ApiModelProperty(value="오더번호")
	private String odJobNo;
	@ApiModelProperty(value="HCount")
	private int hCount;
	@ApiModelProperty(value="신청일자")
	private String modReqDate;
	@ApiModelProperty(value="송수신상태")
	private String sendRecvStatus;	
	@ApiModelProperty(value="송신상태")
	private String sendStatus;
	@ApiModelProperty(value="송신상태")
	private String recvStatus;
	@ApiModelProperty(value="송신상태명")
	private String recvStatusName;
	@ApiModelProperty(value="결과상태")
	private String resultStatus;
	@ApiModelProperty(value="정정번호")
	private String modSerialNo;
	@ApiModelProperty(value="중개사업자1")
	private String venderId1;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="정정구분")
	private String modType;
	@ApiModelProperty(value="정정구분명")
	private String modTypeName;
	@ApiModelProperty(value="H건수")
	private int hblCnt;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="바운드")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="세관코드")
	private String customsCode;
	@ApiModelProperty(value="세관코드")
	private String customsName;
	@ApiModelProperty(value="세관과코드")
	private String customsDptCode;
	@ApiModelProperty(value="세관과코드")
	private String customsDptName;
	@ApiModelProperty(value="입력자")
	private String registerUserId;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="전송일자/시간")
	private String sendDateTime;
	@ApiModelProperty(value="접수/오류일자 시간")
	private String recvDateTime;
	@ApiModelProperty(value="결과일자/시간")
	private String resultDateTime;
	@ApiModelProperty(value="정정신고번호")
	private String modReqNo;
			
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
}

