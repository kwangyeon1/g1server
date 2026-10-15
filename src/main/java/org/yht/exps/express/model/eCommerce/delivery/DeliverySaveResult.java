package org.yht.exps.express.model.eCommerce.delivery;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DeliverySaveResult", description = "Delivery  Save 결과")
@Alias("deliverySaveResult")
public class DeliverySaveResult {
	@ApiModelProperty(value="관리번호")
	private String seqNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="필수값오류리스트")
	private List<Map<String, Object>> errorList;
	@ApiModelProperty(value="유효성오류리스트")
	private List<Map<String, Object>> totalErrorList;
	
	@ApiModelProperty(value="배송요청 전체 카운트")
	private int totalCnt;

	@ApiModelProperty(value="우체국 배송요청 결과 코드")
	private String sendEmsYn;
//	@ApiModelProperty(value="우체국 배송요청 전체 카운트")
//	private int totalEmsCnt;	
	@ApiModelProperty(value="우체국 배송요청 성공 카운트")
	private int successedEmsCnt;		
	@ApiModelProperty(value="우체국 배송요청 실패 카운트")
	private int failEmsCnt;		
	@ApiModelProperty(value="우체국 배송요청 실패 하우스 번호")
	private String failEmsHblNos;		
	@ApiModelProperty(value="우체국 배송요청 주소오류 하우스 번호")
	private String failEmsAddrHblNos;
	@ApiModelProperty(value="우체국 배송요청 interface오류 하우스 번호")
	private String failEmsInterfaceHblNos;
	@ApiModelProperty(value="우체국 배송요청 실패 특수문자 포함된 하우스 번호")
	private String failEmsTelHblNos;
	
	@ApiModelProperty(value="대한통운 배송요청 결과 코드")
	private String sendKorexYn;
//	@ApiModelProperty(value="대한통운 배송요청 전체 카운트")
//	private int totalKorexCnt;	
	@ApiModelProperty(value="대한통운 배송요청 성공 카운트")
	private int successedKorexCnt;		
	@ApiModelProperty(value="대한통운 배송요청 실패 카운트")
	private int failKorexCnt;		
	@ApiModelProperty(value="대한통운 배송요청 실패 하우스 번호")
	private String failKorexHblNos;		
	@ApiModelProperty(value="대한통운 배송요청 주소오류 하우스 번호")
	private String failKorexAddrHblNos;
	@ApiModelProperty(value="대한통운 배송요청 interface오류 하우스 번호")
	private String failKorexInterfaceHblNos;
	
	@ApiModelProperty(value="한진 배송요청 결과 코드")
	private String sendHanjinYn;
//	@ApiModelProperty(value="한진 배송요청 전체 카운트")
//	private int totalHanjinCnt;	
	@ApiModelProperty(value="한진 배송요청 성공 카운트")
	private int successedHanjinCnt;		
	@ApiModelProperty(value="한진 배송요청 실패 카운트")
	private int failHanjinCnt;		
	@ApiModelProperty(value="한진 배송요청 실패 하우스 번호")
	private String failHanjinHblNos;		
	@ApiModelProperty(value="한진 배송요청 주소오류 하우스 번호")
	private String failHanjinAddrHblNos;
	@ApiModelProperty(value="한진 배송요청 interface오류 하우스 번호")
	private String failHanjinInterfaceHblNos;
	@ApiModelProperty(value="한진 배송요청 수화인한글상호오류 하우스 번호")
	private String failHanjinNameHblNos;
	@ApiModelProperty(value="한진 배송요청 수하인우편번호오류 하우스 번호")
	private String failHanjinZipcodeHblNos;
	
	@ApiModelProperty(value="롯데 배송요청 결과 코드")
	private String sendLotteYn;
//	@ApiModelProperty(value="롯데 배송요청 전체 카운트")
//	private int totalLotteCnt;	
	@ApiModelProperty(value="롯데 배송요청 성공 카운트")
	private int successedLotteCnt;		
	@ApiModelProperty(value="롯데 배송요청 실패 카운트")
	private int failLotteCnt;		
	@ApiModelProperty(value="롯데 배송요청 실패 하우스 번호")
	private String failLotteHblNos;		
	@ApiModelProperty(value="롯데 배송요청 필수값오류 하우스 번호")
	private String failLotteManHblNos;
	@ApiModelProperty(value="롯데 배송요청 주소오류 하우스 번호")
	private String failLotteAddrHblNos;
	@ApiModelProperty(value="롯데 배송요청 interface오류 하우스 번호")
	private String failLotteInterfaceHblNos;


	@ApiModelProperty(value="경동 배송요청 결과 코드")
	private String sendKdYn;
	@ApiModelProperty(value="경동 배송요청 성공 카운트")
	private int successedKdCnt;
	@ApiModelProperty(value="경동 배송요청 실패 카운트")
	private int failKdCnt;
	@ApiModelProperty(value="경동 배송요청 실패 하우스 번호")
	private String failKdHblNos;
	@ApiModelProperty(value="경동 배송요청 필수값오류 하우스 번호")
	private String failKdManHblNos;
	@ApiModelProperty(value="경동 배송요청 주소오류 하우스 번호")
	private String failKdAddrHblNos;
	@ApiModelProperty(value="경동 배송요청 interface오류 하우스 번호")
	private String failKdInterfaceHblNos;
}
