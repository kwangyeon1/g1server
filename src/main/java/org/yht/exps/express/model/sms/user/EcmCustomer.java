package org.yht.exps.express.model.sms.user;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "EcmCustomer", description = "Ecm거래처정보 저장용")
@Alias("ecmCustomer")
public class EcmCustomer extends ResultList{
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="카운트")
	private String count;
	@ApiModelProperty(value="사용자수")
	private String userCount;
	@ApiModelProperty(value="거래처코드")
	private String customerCode;
	@ApiModelProperty(value="거래처타입")
	private String customerType;
	@ApiModelProperty(value="사용유무")
	private String useYn;
	@ApiModelProperty(value="거래처명")
	private String customerName;
	@ApiModelProperty(value="거래처영문명")
	private String customerNameEng;
	@ApiModelProperty(value="주소영문")
	private String addressEng;
	@ApiModelProperty(value="주소A영문")
	private String addressAEng;
	@ApiModelProperty(value="주소B영문")
	private String addressBEng;
	@ApiModelProperty(value="담당자영문")
	private String attnEng;
	@ApiModelProperty(value="전화번호영문")
	private String telEng;
	@ApiModelProperty(value="팩스영문")
	private String faxEng;
	@ApiModelProperty(value="주소")
	private String address;
	@ApiModelProperty(value="주소A")
	private String addressA;
	@ApiModelProperty(value="주소B")
	private String addressB;
	@ApiModelProperty(value="담당자명")
	private String attn;
	@ApiModelProperty(value="담당자휴대폰")
	private String 	attnHp;
	@ApiModelProperty(value="전화번호")
	private String 	tel;
	@ApiModelProperty(value="팩스번호")
	private String 	fax;
	@ApiModelProperty(value="이메일")
	private String 	email;
	@ApiModelProperty(value="홈페이지")
	private String 	homePageUrl;	
	@ApiModelProperty(value="솔루션")
	private String 	solution;	
	@ApiModelProperty(value="저장타입")
	private String 	action;	
	@ApiModelProperty(value="수정자")
	private String 	updateUserId;		
	@ApiModelProperty(value="시간")
	private String 	gmtTime;		
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="거래처코드")
	private String accountCode;
	@ApiModelProperty(value="거래처타입")
	private String accountName;	
	@ApiModelProperty(value="국가")
	private String amsCountryCode;
	@ApiModelProperty(value="상위업체")
	private String parentCustomerName;
	@ApiModelProperty(value="상위업체")
	private String lowList;
	@ApiModelProperty(value="관세사")
	private String cbCode;
	@ApiModelProperty(value="관세사")
	private String cbName;
	@ApiModelProperty(value="배송사")
	private String deliveryCode;
	@ApiModelProperty(value="배송사")
	private String deliveryName;
	@ApiModelProperty(value="엑셀타입")
	private String excelType;
	@ApiModelProperty(value="중량")
	private String lbCftType;
	@ApiModelProperty(value="통화")
	private String currencyCode;
	@ApiModelProperty(value="ecm거래처 계좌정보")
	private EcmCustomerBank bankInfo;
	@ApiModelProperty(value="적재항")
	private String origCode;
	@ApiModelProperty(value="우편번호")
	private String goodsLocCode;
	@ApiModelProperty(value="물품소재지")
	private String goodsLocAddr;
	@ApiModelProperty(value="장치장소명")
	private String bwhName;
	@ApiModelProperty(value="운임조건")
	private String incoterms;
	@ApiModelProperty(value="신고과")
	private String customsDptCode;
	@ApiModelProperty(value="통관고유부호")
	private String customsId;
	@ApiModelProperty(value="신고세관")
	private String customsCode;
	@ApiModelProperty(value="AMS ZIPCODE")
	private String amsZipcode;
	@ApiModelProperty(value="AMS STATE")
	private String amsState;
	@ApiModelProperty(value="AMS PLACE")
	private String amsPlace;
	@ApiModelProperty(value="AMS STREET")
	private String amsStreet;
	@ApiModelProperty(value="BusinessType BROS")
	private String customerCustomsIdType;
	@ApiModelProperty(value="BusinessID BROS")
	private String customerBusinessId;
	@ApiModelProperty(value="수출자상호 BROS")
	private String customerBusinessName;
	@ApiModelProperty(value="수출자대표 BROS")
	private String customerBusinessPresident;
	@ApiModelProperty(value="수출자주소 BROS")
	private String customerBusinessAddress;
	@ApiModelProperty(value="수출자 해외세무등록종류 BROS")
	private String customerExpTransType;
	@ApiModelProperty(value="수출자 VAT BROS")
	private String customerGstNo;
	@ApiModelProperty(value="신고인부호")
	private String declarationRegNo;
	@ApiModelProperty(value="ecm거래처 인터페이스정보")
	private List<EcmCustomerInterface> interfaceList;
	
	@ApiModelProperty(value="크림api id")
	private String kreamApi1;
	@ApiModelProperty(value="크림api pw")
	private String kreamApi2;

	@ApiModelProperty(value="전자상거래업자부호")
	private String commBusinessCode;
	@ApiModelProperty(value="전자상거래 인증정보 ID")
	private String clientId;
	@ApiModelProperty(value="전자상거래 인증정보 Secret")
	private String clientSecret;

	@ApiModelProperty(value="입력일시")
	private String registerDateTime;

	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="수정자")
	private String updateUserName;	
	@ApiModelProperty(value="사업자번호")
	private String businessId;
	@ApiModelProperty(value="파일공유여부")
	private String fileShare;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	
	@ApiModelProperty(value="콘솔사용유무")
	private String consolUseYn;
	
	@ApiModelProperty(value="정산여부")
	private String ecmInvoiceYn;
	@ApiModelProperty(value="발송수정여부")
	private String sendModifyYn;
	@ApiModelProperty(value="미사용유저수")
	private String userCountBefore;
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}
