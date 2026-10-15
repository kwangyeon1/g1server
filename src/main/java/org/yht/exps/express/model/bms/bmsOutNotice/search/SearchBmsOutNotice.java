package org.yht.exps.express.model.bms.bmsOutNotice.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNotice;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@ApiModel(value = "SearchBmsOutNotice", description = "반출통고신고 조회 조건")
@Alias("searchBmsOutNotice")
public class SearchBmsOutNotice extends SearchList {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호 LIKE 검색 유무")
	private String srchNoLikeYn;
	@ApiModelProperty(value="조회번호 구분")
	private String srchNoType;
	@ApiModelProperty(value="조회번호 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="조회일자 구분")
	private String srchDateGbn;
	@ApiModelProperty(value="조회일자타입", example="dateTime")
	private String srchDateType;
	@ApiModelProperty(value="조회 시작일자")
	private String srchStartDate;
	@ApiModelProperty(value="조회 종료일자")
	private String srchEndDate;
	@ApiModelProperty(value="조회 구분") /** STOCK(미승인재고), NOTICE(반출통고), OMITTED(누락통보) */
	private String srchGbn;
	@ApiModelProperty(value="반출통고 조회번호(PK)")
	private Integer srchOutNoticeNo;
	@ApiModelProperty(value="화물관리번호(ENTRY 상세 조회조건)")
	private String srchCargoReferenceNo;
	@ApiModelProperty(value="반입번호(ENTRY 상세 조회조건 - 화물관리번호가 중복일 수 있음)")
	private String srchInNo;
	@ApiModelProperty(value="진행상태")
	private String srchLastPodStatus;
	@ApiModelProperty(value="거래처구분")
	private String srchCustomerGuBun;
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
	private String srchLocationPlaceName;
	@ApiModelProperty(value="검색거래처명(비고1)", example="거래처명(비고1)")
	private String srchRemarks1Name;
	@ApiModelProperty(value="검색거래처명(비고2)", example="거래처명(비고2)")
	private String srchRemarks2Name;
	@ApiModelProperty(value="검색거래처명(비고3)", example="거래처명(비고3)")
	private String srchRemarks3Name;
	@ApiModelProperty(value="검색거래처명(품명)", example="거래처명(품명)")
	private String srchCommodityItemLocName;
	@ApiModelProperty(value="세관제출번호")
	private String srchDeclarationNo;
	@ApiModelProperty(value="엑셀파일명")
	private String excelTitle;
	@ApiModelProperty(value="엑셀다운여부")
	private String excelDownYn;

    /** Web XML Send */
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String entBusinessCode;
	@ApiModelProperty(value="outNoticeNo 리스트")
	private List<Integer> outNoticeNoList;

}
