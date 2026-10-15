package org.yht.exps.express.model.bms.unipass;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("searchCargoProgressInfo")
public class SearchCargoProgressInfo {
	// 본지사코드
	private String srchEntOfficeCode;
	// 화물관리번호 15~19자리
	private String srchCargoReferenceNo;
	// MBL번호
	private String srchMblNo;
	// HBL번호
	private String srchHblNo;
	// BL년도 2~4자리, MBL 또는 HBL 입력 시 필수 입력
	private String srchBlYear;
	// 설영특허번호
	private String srchBondedWarehouseNo; 
	// 반출근거번호
	private String srchApprovalNoList;
}
