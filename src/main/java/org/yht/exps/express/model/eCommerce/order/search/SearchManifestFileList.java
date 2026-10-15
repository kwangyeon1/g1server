package org.yht.exps.express.model.eCommerce.order.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.eCommerce.order.ManifestFile;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchManifestFileList", description = "적하목록File업로드")
@Alias("searchManifestFileList")
public class SearchManifestFileList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="조회일자타입", example="dateTime")
	private String srchDateType;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회상태")
	private String srchStatus;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="기초코드타입")
	private String srchBasicType;
	@ApiModelProperty(value="그룹코드타입")
	private String srchGroupName;
	@ApiModelProperty(value="조회통관타입")
	private String srchCdType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="업로드조건사용여부")
	private String uploadSearch;
	@ApiModelProperty(value="도착지")
	private String srchDestCode;
	@ApiModelProperty(value="목적항")
	private String destPort;
	@ApiModelProperty(value="도착지국가")
	private String srchDestCountry;
	@ApiModelProperty(value="서류여부")
	private String srchDocumentYn;
//	@ApiModelProperty(value="중량소수점")
//	private String wtDecimalAir;
	@ApiModelProperty(value="환율")
	private double exchangeRate;
	@ApiModelProperty(value="대리점코드")
	private String custCodeLv1;
	@ApiModelProperty(value="USD환율")
	private double usdExchangeRate;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="CONSOL_여부")
	private String consolYn;
	@ApiModelProperty(value="마감여부")
	private String finishYn;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="도착지명")
	private String destName;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="목적국가")
	private String destCountry;
	@ApiModelProperty(value="송하인 국가코드")
	private String shipperCountryCode;
	@ApiModelProperty(value="수하인 국가코드")
	private String consigneeCountryCode;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="출항시간")
	private String obTime;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="항공사_선사코드")
	private String lineCode;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="화물구분")
	private String blTypeIert;
	@ApiModelProperty(value="세관코드")
	private String customsCode;
	@ApiModelProperty(value="세관과코드")
	private String customsDptCode;
	@ApiModelProperty(value="하기(하선)장소코드 (장치장 또는 보세창고코드)")
	private String discWhCode;
	@ApiModelProperty(value="배정코드")
	private String assignmentWhCode;
	@ApiModelProperty(value="중개사업자")
	private String venderId;
	@ApiModelProperty(value="목록신고제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="관세청정보관리번호")
	private String ediMailboxSeqNo;
	@ApiModelProperty(value="목록선별자_아이디")
	private String selectorUserId;
	@ApiModelProperty(value="선별물품확인자_아이디")
	private String confirmerUserId;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="UPS_iFile")
	private String orig;
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
	@ApiModelProperty(value="적하목록리스트")
	private List<ManifestFile> manifestFileList;
	
	// UPS : 항공수입 T/S HAWB 가져오기
	private String srchExpYn;
	
	public SearchManifestFileList(){}
	public SearchManifestFileList(SearchManifestFileList search){
		this.srchEntCode = search.getSrchEntCode();
		this.entBusinessCode = search.getEntBusinessCode();
		this.bound = search.getBound();
		this.boundType = search.getBoundType();
		this.gmtTime = search.getGmtTime();
		this.consolYn = search.getConsolYn();
		this.custCodeLv1 = search.getCustCodeLv1();
		this.dateFormat = search.getDateFormat();
		this.finishYn = search.getFinishYn();
		this.venderId = search.getVenderId();
		this.userId = search.getUserId();
	}
}
