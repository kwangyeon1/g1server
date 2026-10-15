package org.yht.exps.express.model.eCommerce.function;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "FunctionOrder", description = "전자상거래 Function Order")
@Alias("functionOrder")
public class FunctionOrder extends SearchList  { 
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="솔루션")
	private String solution;
	
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정자거래처코드")
	private String ecmCustomerCode;
	@ApiModelProperty(value="수정자거래처레벨")
	private String ecmCustomerLevel;
	
	@ApiModelProperty(value="오더잡번호리스트")
	private List<Map<String, Object>> odList;
	@ApiModelProperty(value="선택데이터 적하SubGrid여부")
	private String checkMfSubGridYn;
	@ApiModelProperty(value="오더번호리스트")
	private List<Map<String, Object>> odNoList;
	@ApiModelProperty(value="패널티리스트")
	private List<Map<String, Object>> penaltyList;
	@ApiModelProperty(value="H.B/L No")
	private String hblNo;
	
	
	/** 저장 */
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="입항일자")
	private String obDate;
	
	@ApiModelProperty(value="상태")
	private String insStatus;
	
	@ApiModelProperty(value="삭제상태")
	private String delStatus;
	
	@ApiModelProperty(value="모드")
	private String funcMode;
	
	@ApiModelProperty(value="모드")
	private String chkMode;
	
	@ApiModelProperty(value="과태료타입")
	private String penaltyType;
	
	@ApiModelProperty(value="re반입적용")
	private String reCarryYn;
	
	@ApiModelProperty(value="과태료Yn")
	private String penaltyYn;
	
	
	@ApiModelProperty(value="검색여부")
	private String srchYn;
	@ApiModelProperty(value="개인통관고유부호체크타입")
	private String nameType;
	
	@ApiModelProperty(value="호출메뉴")
	private String parentMenu;
	
		
	@ApiModelProperty(value="검색조건")
	private Map<String,Object> srchInfo;
	
	@ApiModelProperty(value="화면에서 선택/검색된 하우스 건수")
	private int houseCnt;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
	//로그저장시 SearchList의 srchMode와 중복으로 오류발생
//	@ApiModelProperty(value="모드")
//	private String srchMode;
//	@ApiModelProperty(value="Language")
//	private String userLanguage;
//	@ApiModelProperty(value="날짜형식")
//	private String dateFormat;
//	// Excel Download 관련 Parameter
//	@ApiModelProperty(value="Excel Sheet Name")
//	private String mainTitle;
//	@ApiModelProperty(value="사용자 이름")
//	private String userName;
//	@ApiModelProperty(value="그리드 컬럼")
//	private List<String> columns;
//	@ApiModelProperty(value="그리드 컬럼 타이틀")
//	private List<String> titles;
//	@ApiModelProperty(value="그리드 컬럼 메모")
//	private List<String> memos;
//	@ApiModelProperty(value="그리드 엑셀 타입")
//	private List<String> excelTypes;
//	@ApiModelProperty(value="그리드 Number 포맷")
//	private List<String> numFormats;
//	@ApiModelProperty(value="그리드 컬럼 width")
//	private List<Integer> columnsWidth;
	
}