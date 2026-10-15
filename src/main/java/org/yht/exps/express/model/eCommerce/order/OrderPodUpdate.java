package org.yht.exps.express.model.eCommerce.order;

import java.util.List;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("orderPodUpdate")
public class OrderPodUpdate {
	/* 반입상태 */
	private String strCarryIn;
	/* 반출상태 */
  	private String strCarryOut;
  	/* MBL No */
  	private String masterOdJobNo;
  	/* MBL No */
  	private String mblNo;
  	/* HBL No */
  	private String hblNo;
  	/* MRN */
  	private String mrn;
  	/* MSN */
  	private String msn;
  	/* 폐기반출 */
  	private String strWhOutdestroy;
	/* 반출입신고번호 */
	private String carryNo;
	/* 장치장위치 */
	private String whInPos;
	/* 통관구분 */
	private String cdType;
	/* 검색 Job No */
	private String srchOdJobNo;
	/* 화물진행정보상태 LastPod*/
	private String progressLastStatus;
	/* 화물진행정보상태 LastPod*/
	private String processLastDateTime;
	/* 실행타입 */
	private String action;
	
	/* 반입일시 */
	private String carryInDateTime;
	/* 반출일시 */
	private String carryOutDateTime;
	/* 수입신고일시 */
	private String customsNDateTime;
	/* 수입신고수리일시 */
	private String importReceivingReportDateTime;
	/* 통관목록심사완료일시 */
	private String importClearanceDateTime;
	/* 수입(사용소비)심사진행일시 */
	private String importScreeningDateTime;
	/* 통관보류일시 */
	private String customsHoldDateTime;
	/* 통관접수일 */
	private String clearanceReceiptDate;
	/* 통관완료일 */
	private String clearanceDate;
	/* 수입신고수리일자 */
	private String simpleReportDate;
	/* 통관목록심사취소일시 */
	private String customsCheckCancelDateTime;
	/* 첫번째반출플래그 */
	private boolean ecm290Flag = false;
	/* 상위업체 */
	private String custCodeLv1;
	
	private String dropYn;
	
	private List<Pod> podList;
	
	@Data
	public static class Pod{
		/* 화물진행정보상태Pod*/
		private String progressStatus;
		/* 화물진행정보상태일시 */
		private String processDateTime;
		/* 저장여부 */
		private String saveYn;
	}
}
