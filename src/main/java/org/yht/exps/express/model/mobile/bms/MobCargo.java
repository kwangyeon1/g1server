package org.yht.exps.express.model.mobile.bms;

import java.util.List;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MobCargo", description = "보세구역관리 모바일 화물 Detail 리스트")
@Alias("mobCargo")
public class MobCargo {
	
	private List<String> hblNoArray;

    public List<String> getHblNoArray() {
        return hblNoArray;
    }
    
    public void setHblNoArray(List<String> hblNoArray) {
        this.hblNoArray = hblNoArray;
    }
	
    @ApiModelProperty(value="데이터구분")
	private String dGbn;
    @ApiModelProperty(value="반입번호")
	private String inNo;
    @ApiModelProperty(value="반입예정번호")
	private String inPlanNo;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="반입구분")
	private String inClassName;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	
	@ApiModelProperty(value="수량")
	private int ct;
	@ApiModelProperty(value="중량")
	private double wt;
	@ApiModelProperty(value="장치위치")
	private String locationPlace;
	@ApiModelProperty(value="장치위치명")
	private String locationPlaceName;
	@ApiModelProperty(value="화물구분")
	private String cdType;
	@ApiModelProperty(value="화물구분명")
	private String cdTypeName;
	@ApiModelProperty(value="거대여부")
	private String bigSizeYn;
	@ApiModelProperty(value="비고1")
	private String remarks1;
	@ApiModelProperty(value="선별결과코드명")
	private String seInspectionCodeName;
	@ApiModelProperty(value="XRAY검사코드명")
	private String xrayInspectionCodeName;
	@ApiModelProperty(value="통관검사계획코드명")
	private String paln5emInspectionCodeName;
	@ApiModelProperty(value="검사완료여부")
	private String inspectionFinishYn;
	@ApiModelProperty(value="홀드_사유")
	private String holdReason;
	@ApiModelProperty(value="홀드여부")
	private String holdReleaseYn;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatusName;
	@ApiModelProperty(value="진행상태")
	private String lastPod5TlStatusName;
	@ApiModelProperty(value="승인구분")
	private String approvalClass;
	@ApiModelProperty(value="진행상태")
	private String oPodStatusName;
	@ApiModelProperty(value="입항일시")
	private String arDateTime;
	@ApiModelProperty(value="반입일시")
	private String carryinDateTime;
	@ApiModelProperty(value="통보일시")
	private String notifyDateTime;
	@ApiModelProperty(value="반출일시")
	private String carryoutDateTime;
	@ApiModelProperty(value="품명")
	private String commodityItemLoc;
	@ApiModelProperty(value="송하인_명칭")
	private String shipperName;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="판독수량")
	private String xrayImageCt;
	@ApiModelProperty(value="배송사_이름")
	private String deliveryName;
	@ApiModelProperty(value="출고")
	private String outScanDateTime;
	
	
	
	@ApiModelProperty(value="bound")
	private String bound;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="도착지명")
	private String destName;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="거래처코드1")
	private String custCodeLv1;
	@ApiModelProperty(value="수하인_코드")
	private String consigneeCode;
	@ApiModelProperty(value="수하인_주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인_주소_A")
	private String consigneeAddressA;
	@ApiModelProperty(value="수하인_주소_B")
	private String consigneeAddressB;
	@ApiModelProperty(value="수하인_주소_C")
	private String consigneeAddressC;
	@ApiModelProperty(value="수하인_주소_D")
	private String consigneeAddressD;
	@ApiModelProperty(value="수하인_핸드폰")
	private String consigneeHp;
	@ApiModelProperty(value="통관결제_타입(선불후불)")
	private String clearancePaymentType;
	@ApiModelProperty(value="배송사_코드")
	private String deliveryCode;
	@ApiModelProperty(value="배송요청")
	private String deliveryNote;
	@ApiModelProperty(value="배송결재조건")
	private String deliveryPaymentType;
	
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="통관여부")
	private String clearanceYn;
	@ApiModelProperty(value="반출_상태(P : 출고예정,C : 출고취소,F : 출고완료)")
	private String carryoutStatus;
	
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	
	@ApiModelProperty(value="반출여부")
	private String carryoutYn;
	@ApiModelProperty(value="반출여부")
	private String carrydateYn;
	@ApiModelProperty(value="홀드명")
	private String holdNm;
	@ApiModelProperty(value="홀드여부")
	private String holdYn;
	@ApiModelProperty(value="인계 또는 취소 구분자")
	private String gubun;
	@ApiModelProperty(value="배송비")
	private String deliveryFee;
	
	@ApiModelProperty(value="반출담당자")
	private String carryoutUserId;
	@ApiModelProperty(value="수입신고수리일자")
	private String clearanceDate;
	@ApiModelProperty(value="재고일시")
	private String stockDateTime;
	@ApiModelProperty(value="재고담당자")
	private String stockUserId;
	@ApiModelProperty(value="재고여부")
	private String stockYn;
	@ApiModelProperty(value="취하여부")
	private String dropYn;
	@ApiModelProperty(value="볼륨")
	private double vwt;
	@ApiModelProperty(value="창고료")
	private double warehouseFee;
	@ApiModelProperty(value="검사여부")
	private String inspectionYn;	
	@ApiModelProperty(value="선별여부")
	private String seInspectionYn;	
	@ApiModelProperty(value="xRay여부")
	private String xRayYn;
	@ApiModelProperty(value="멀티여부")
	private String multiYn;
	
	

	@ApiModelProperty(value="리스트")
	private List<String> dataList;
	
//	@ApiModelProperty(value="멀티리스트")
//	private List<MobEcodMulti> multiList;
//	
//	@ApiModelProperty(value="출고리스트")
//	private List<MobEcodCarryout> carryoutList;
//	
//	@ApiModelProperty(value="재고리스트")
//	private List<MobEcodCarryout> inventoryList;
	
	@ApiModelProperty(value="금일출고건수")
	private int outSum;
			
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
	
	/** 저장 */
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="인계자Id")
	

	private String recipientUserId;
	@ApiModelProperty(value="인수자Id")
	private String recipientName;
	@ApiModelProperty(value="인수일자")
	private String receiptDate;
	@ApiModelProperty(value="진행구분코드")
	private String podStatus;	
	@ApiModelProperty(value="검사순번")
	private int inspectionSeqNo;	
	
	
	@ApiModelProperty(value="반출")
	private String ecm280YN;
	@ApiModelProperty(value="홀드")
	private String holdYN;
	@ApiModelProperty(value="승인")
	private String useYn;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
}
