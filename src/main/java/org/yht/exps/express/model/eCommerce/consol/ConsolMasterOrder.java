package org.yht.exps.express.model.eCommerce.consol;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "ConsolMasterOrder", description = "전자상거래 Consol MasterOrder 리스트")
@Alias("consolMasterOrder")
@EqualsAndHashCode(callSuper=false)
public class ConsolMasterOrder extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="비엘관리번호")
	private String oblJobNo;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="출항시간")
	private String obTime;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="상위업체코드")
	private String custCodeLv1;
	@ApiModelProperty(value="상위업체명")
	private String custNameLv1;
	@ApiModelProperty(value="항공사(선사)")
	private String lineCode;
	@ApiModelProperty(value="항공사(선사)명")
	private String lineName;
	@ApiModelProperty(value="voyage")
	private String voyage;
	@ApiModelProperty(value="편명(선명)")
	private String vesselFlight;
	@ApiModelProperty(value="출발지")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="출발국가")
	private String origCountry;
	@ApiModelProperty(value="도착지")
	private String destCode;
	@ApiModelProperty(value="도착지명")
	private String destName;
	@ApiModelProperty(value="목적항")
	private String destPort;
	@ApiModelProperty(value="도착국가")
	private String destCountry;
	@ApiModelProperty(value="MAWB No")
	private String mblNo;
	@ApiModelProperty(value="H.건수")
	private int hblCnt;
	@ApiModelProperty(value="H.수량")
	private int hblPieces;
	@ApiModelProperty(value="H.중량")
	private double hblGwt;
	@ApiModelProperty(value="목록건수")
	private int cdType1Cnt;
	@ApiModelProperty(value="일반건수")
	private int cdType2Cnt;
	@ApiModelProperty(value="배제건수")
	private int cdType3Cnt;
	@ApiModelProperty(value="이행건수")
	private int cdType4Cnt;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="마감여부")
	private String finishYn;
	@ApiModelProperty(value="적하목록전송여부")
	private String sendManifestYn;
	@ApiModelProperty(value="통관목록제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="통관목록전송여부")
	private String sendSimpleDocYn;
	@ApiModelProperty(value="세관")
	private String customsCode;
	@ApiModelProperty(value="세관과")
	private String customsDptCode;
	@ApiModelProperty(value="보세창고")
	private String assignmentWhCode;
	@ApiModelProperty(value="하기장소")
	private String discWhCode;
	@ApiModelProperty(value="전자상거래여부Y카운트")
	private String ecmYCnt;
	@ApiModelProperty(value="전자상거래여부N카운트")
	private String ecmNCnt;
	
	/* 마스터기준 합계목록 */
	@ApiModelProperty(value="마스터sum")
	private int sumCount;
	@ApiModelProperty(value="하우스sum")
	private int sumHblNo;
	@ApiModelProperty(value="수량sum")
	private int sumPieces;
	@ApiModelProperty(value="중량sum")
	private double sumGwt;
	@ApiModelProperty(value="H.CBM")
	private double hblCbm;
	
	@ApiModelProperty(value="cbm sum")
	private double sumCbm;
}
