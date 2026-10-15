package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Alias("excelUploadResult")
public class ExcelUploadResult {
	@ApiModelProperty(value="Index Name")
	private String indexName;
	@ApiModelProperty(value="ES ID")
	private String referenceNoId;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="도입사_사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="솔루션")
	private String solution;
	@ApiModelProperty(value="바운드")
	private String bound;
	@ApiModelProperty(value="바운드 구분")
	private String boundType;
	@ApiModelProperty(value="생성 상태")
	private String status;
	@ApiModelProperty(value="상태 상세 내용")
	private  String statusInfo;
	@ApiModelProperty(value="엑셀 템플릿 코드")
	private String documentCode;
	@ApiModelProperty(value="엑셀 파일명")
	private String fileName;
	@ApiModelProperty(value="엑셀 파일명_오리지널")
	private String originFileName;
	@ApiModelProperty(value="엑셀 파일 백업날짜(yyyy-MM)")
	private String backupDate;
	@ApiModelProperty(value="ElasticSearch Error List Index명")
	private String errorIndexName;
	@ApiModelProperty(value="입력자 코드")
	private String registerUserId;
	@ApiModelProperty(value="입력 일시")
	private String registerDateTime;
	@ApiModelProperty(value="처리 일시")
	private String processDateTime;
	@ApiModelProperty(value="대기 순번")
	private int order;
	
	@ApiModelProperty(value="이름")
	private String registerUserName;
	@ApiModelProperty(value="문서유형", example="Order,Delivery,Broker")
	private String excelType;
	@ApiModelProperty(value="배송사코드", example="KOREX,EMS...")
	private String deliveryCode;
	@ApiModelProperty(value="업로드 성공 오더 건수")
	private int count;
}