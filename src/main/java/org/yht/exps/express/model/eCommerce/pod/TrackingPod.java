package org.yht.exps.express.model.eCommerce.pod;

import org.apache.ibatis.type.Alias;

import lombok.Data;

/**
 * 트래킹 진행상태
 *
 */
@Data
@Alias("trackingPod")
public class TrackingPod {
	/* 5자리 도입사코드 */
	private String entCode;
	/* Tracking Job No */
	private String TrackingJobNo;
	/* Job No */
	private String odJobNo;
	/* HBL NO */
	private String hblNo;
	/* 배송번호 */
	private String deliveryNo;
	/* 오더번호 */
	private String poNo;
	/* 배송사 코드 */
	private String deliveryCode;
	/* 진행상태코드 */
	private String podStatus;
	/* 진행상태명 */
	private String podName;
	/* 진행상태일자 */
	private String podDate;
	/* 진행상태시간 */
	private String podTime;
	/* 비고 */
	private String remarks;
	/* 처리 상태 (R:준비, S:성공, E:실패) */
	private String status;
	/* 처리 실패 사유 */
	private String errorDesc;
	/* 전송 일시 */
	private String sendDateTime;
	/* 입력자 */
	private String registerUserId;
	/* 입력일시 */
	private String registerDateTime;
	/* 수정자 코드 */
	private String updateUserId;
	/* 수정일시 */
	private String updateDateTime;
	
	/* 배송 코드 */
	private String courierCode;
	/* 출발 국가 */
	private String origCountry;
	/* 쿠팡box번호 */
	private String shipmentBoxId;
	/* 리턴번호 */
	private String returnRequestId;
	/* 바운드 */
	private String bound;
	/* 바운드구분 */
	private String boundType;
	
	/* 배송사 */
	private String branchName;
	/* 배송사 전화번호 */
	private String branchTel;
	/* 배송사 담당자 */
	private String workerName;
	/* 배송사 담당자 전화번호 */
	private String workerTel;
	
	/* 개인통관부호검증 */
	private String customsChk;
}
