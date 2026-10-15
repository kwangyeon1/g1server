package org.yht.exps.express.model.bms.bmsOutCorrection.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchOutCorrection", description = "반출조회조건")
@Alias("searchOutCorrection")
public class SearchOutCorrection extends SearchList {

    @ApiModelProperty(value="조회일자 구분")
    private String srchDateGbn;
    @ApiModelProperty(value="조회일자타입", example="dateTime")
    private String srchDateType;
    @ApiModelProperty(value="조회 시작일자")
    private String srchStartDate;
    @ApiModelProperty(value="조회 종료일자")
    private String srchEndDate;

    @ApiModelProperty(value="조회번호")
    private String srchNo;
    @ApiModelProperty(value="조회번호타입")
    private String srchNoType;
    @ApiModelProperty(value="조회타입 멀티/싱글")
    private String searchType;
    @ApiModelProperty(value="srchNo N개씩 조회시 리스트")
    private List<String> searchNoList;

    @ApiModelProperty(value = "페이지 작업모드", example = "EDIT(수정) or NEW(신규)")
    private String workMode;

    @ApiModelProperty(value = "엔트리팝업검색여부", example = "Y(엔트리에서 팝업에서 검색)")
    private String srchInEntry;

    @ApiModelProperty(value="반입신고", example="입력 or 대기 or 송신 ....") //DB 컬럼: LAST_POD_STATUS 신고상태
    private String srchLastPodStatus;

    //거래처 검색
    @ApiModelProperty(value="거래처구분", example="화주 or 송하인 or 수하인...")
    private String srchCustomerType;
    @ApiModelProperty(value="검색거래처코드(화주)", example="거래처코드(화주)")
    private String srchCustomerCode;
    @ApiModelProperty(value="검색거래처명(화주)", example="거래처명(화주)")
    private String srchCustomerName;
    @ApiModelProperty(value="검색거래처코드(송화인)", example="거래처코드(송화인)")
    private String srchShipperCode;
    @ApiModelProperty(value="검색거래처명(송화인)", example="거래처명(송화인)")
    private String srchShipperName;
    @ApiModelProperty(value="검색거래처코드(수화인)", example="거래처코드(수화인)")
    private String srchConsigneeCode;
    @ApiModelProperty(value="검색거래처명(수화인)", example="거래처명(수화인)")
    private String srchConsigneeName;
    @ApiModelProperty(value="검색거래처코드(통지처)", example="거래처코드(통지처)")
    private String srchNotifyCode;
    @ApiModelProperty(value="검색거래처명(통지처)", example="거래처명(통지처)")
    private String srchNotifyName;
    @ApiModelProperty(value="검색거래처코드(배송사)", example="거래처코드(배송사)")
    private String srchDeliveryCode;
    @ApiModelProperty(value="검색거래처명(배송사)", example="거래처명(배송사)")
    private String srchDeliveryName;
    @ApiModelProperty(value="검색거래처코드(포워더)", example="거래처코드(포워더)")
    private String srchFwdCode;
    @ApiModelProperty(value="검색거래처명(포워더)", example="거래처명(포워더)")
    private String srchFwdName;
    @ApiModelProperty(value="검색거래처명(장치위치)", example="거래처명(장치위치)")
    private String srchLocationPlace;
    @ApiModelProperty(value="검색거래처명(비고1)", example="거래처명(비고1)")
    private String srchRemarks1;
    @ApiModelProperty(value="검색거래처명(비고2)", example="거래처명(비고2)")
    private String srchRemarks2;
    @ApiModelProperty(value="검색거래처명(비고3)", example="거래처명(비고3)")
    private String srchRemarks3;
    @ApiModelProperty(value="검색상품명", example="검색상품명")
    private String srchCommodityItemLoc;

    //ENTRY 호출시 반입신고 번호 생성을 위한 bmswhCode
    @ApiModelProperty(value="bmswh코드")
    private String bmswhCode;
    @ApiModelProperty(value="반출입정정신청번호")
    private Integer changeNo;

    @ApiModelProperty(value="반출번호")
    private Integer srchOutNo;
    @ApiModelProperty(value="반출입정정신청번호")
    private Integer srchChangeNo;

    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String srchEntCode;
    @ApiModelProperty(value="반입상태")
    private String srchCarryOutState;
    @ApiModelProperty(value="반출번호")
    private Integer outNo;
    @ApiModelProperty(value="반출신고번호")
    private String customsReportNo;
    @ApiModelProperty(value="현재진행상태일시")
    private Date lastPodDate;
    @ApiModelProperty(value="현재진행상태코드")
    private String lastPodStatus;
    @ApiModelProperty(value="접수일시")
    private Date lastPodRaqDate;
    @ApiModelProperty(value="반출승인일자")
    private String approvalDate;
    @ApiModelProperty(value="반출구분")
    private String outClass;
    @ApiModelProperty(value="반출근거번호")
    private String outApprovalNo;
    @ApiModelProperty(value="오더관리번호")
    private String odJobNo;
    @ApiModelProperty(value="반입번호")
    private Integer inNo;
    @ApiModelProperty(value="반출승인통보번호")
    private Integer outPlanNo;
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="MBL번호")
    private String mblNo;
    @ApiModelProperty(value="DO번호")
    private String doNo;
    @ApiModelProperty(value="DO일자")
    private String doDate;
    @ApiModelProperty(value="세관코드")
    private String customsCode;
    @ApiModelProperty(value="세관과코드")
    private String customsDptCode;
    @ApiModelProperty(value="화주코드")
    private String customerCode;
    @ApiModelProperty(value="화주명")
    private Object customerName;
    @ApiModelProperty(value="관세사코드")
    private String brokerCode;
    @ApiModelProperty(value="관세사명")
    private Object brokerName;
    @ApiModelProperty(value="포장종류")
    private String pkgType;
    @ApiModelProperty(value="승인수량")
    private Integer approvalCt;
    @ApiModelProperty(value="승인중량")
    private Double approvalWt;
    @ApiModelProperty(value="승인용적")
    private Double approvalCbm;
    @ApiModelProperty(value="반출수량")
    private Integer outCt;
    @ApiModelProperty(value="반출중량")
    private Double outWt;
    @ApiModelProperty(value="반출용적")
    private Double outCbm;
    @ApiModelProperty(value="반출일자")
    private String carryoutDate;
    @ApiModelProperty(value="반출시간")
    private String carryoutTime;
    @ApiModelProperty(value="관세")
    private Object approvalDuty;
    @ApiModelProperty(value="감정가액")
    private Object approvalAppraiseValue;
    @ApiModelProperty(value="결제방법")
    private String insPaymentType;
    @ApiModelProperty(value="결제통화")
    private String currencyCode;
    @ApiModelProperty(value="적하보험료납부")
    private String insPaymentYn;
    @ApiModelProperty(value="검사대상여부")
    private String inspectYn;
    @ApiModelProperty(value="HS코드")
    private String hsCode;
    @ApiModelProperty(value="수입요건법령코드")
    private String criteriaCode;
    @ApiModelProperty(value="반입반출간격")
    private Object inoutDateDiff;
    @ApiModelProperty(value="비고1")
    private Object remarks1;
    @ApiModelProperty(value="비고2")
    private Object remarks2;
    @ApiModelProperty(value="비고3")
    private Object remarks3;
    @ApiModelProperty(value="참조번호")
    private Object referenceNo;
    @ApiModelProperty(value="입력자코드")
    private String registerUserId;
    @ApiModelProperty(value="입력일시")
    private Date registerDateTime;
    @ApiModelProperty(value="수정자코드")
    private String updateUserId;
    @ApiModelProperty(value="수정일시")
    private Date updateDateTime;

    /** 엑셀다운 */
    @ApiModelProperty(value="문서양식코드(엑셀다운로드)")
    private String documentCode;
    @ApiModelProperty(value="문서명(엑셀다운로드)")
    private String documentName;

    /** Web XML Send */
	@ApiModelProperty(value="changeNo 리스트")
	private List<Integer> changeNoList;

}
