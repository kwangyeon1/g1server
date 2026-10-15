package org.yht.exps.express.model.mobile.bms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Builder
@ApiModel(value = "BmsLocalInspectResult", description = "반출 VO 객체")
@Alias("bmsLocalInspectResult")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsLocalInspectResult extends ResultList{
	
	/* PDA 검사완료 화면 항목들 */
	@ApiModelProperty(value = "완료 여부", example = "Y,N")
	private String inspectionYn;
	
	@ApiModelProperty(value = "검사항목이름", example = "총기,마약")
	private String nameDefault;
	
	@ApiModelProperty(value = "검사항목키워드", example = "se,xray")
	private String inspectionKeyword;
	
	@ApiModelProperty(value = "완료자 사용자 ID", example = "fin123")
    private String finishUserId;
	
	/* 반입TB_BMS_IN 항목 */	
	@ApiModelProperty(value = "반입 수량", example = "123")
    private int ct;
	
	@ApiModelProperty(value = "오더관리번호")
    private String odJobNo;
	/* --------------- */

	@ApiModelProperty(value = "반입 번호", example = "123")
    private int inNo;

    @ApiModelProperty(value = "반입 날짜", example = "20230919")
    private String carryinDate;

    @ApiModelProperty(value = "B/L 번호", example = "BL123456")
    private String blNo;
    
    @ApiModelProperty(value = "MB/L 번호", example = "MBL123456")
    private String mblNo;

    @ApiModelProperty(value = "검사 코드", example = "01")
    private String seInspectionCode;

    @ApiModelProperty(value = "검사 시작 사용자 ID", example = "user01")
    private String seInspectionStartUserId;

    @ApiModelProperty(value = "검사 시작 일시", example = "2024-09-19 10:00:00")
    private String seInspectionStartDateTime;

    @ApiModelProperty(value = "검사 완료 사용자 ID", example = "user02")
    private String seInspectionFinishUserId;

    @ApiModelProperty(value = "검사 완료 일시", example = "2024-09-19 12:00:00")
    private String seInspectionFinishDateTime;

    @ApiModelProperty(value = "검사 유형", example = "TYPE_A")
    private String inspectionType;

    @ApiModelProperty(value = "검사 유형 시작 사용자 ID", example = "user03")
    private String inspectionTypeStartUserId;

    @ApiModelProperty(value = "검사 유형 시작 일시", example = "2024-09-19 14:00:00")
    private String inspectionTypeStartDateTime;

    @ApiModelProperty(value = "검사 유형 완료 사용자 ID", example = "user04")
    private String inspectionTypeFinishUserId;

    @ApiModelProperty(value = "검사 유형 완료 일시", example = "2024-09-19 16:00:00")
    private String inspectionTypeFinishDateTime;

    @ApiModelProperty(value = "검사 유형 A", example = "TYPE_A")
    private String inspectionTypeA;

    @ApiModelProperty(value = "검사 유형 A 시작 사용자 ID", example = "user05")
    private String inspectionTypeAStartUserId;

    @ApiModelProperty(value = "검사 유형 A 시작 일시", example = "2024-09-19 18:00:00")
    private String inspectionTypeAStartDateTime;

    @ApiModelProperty(value = "검사 유형 A 완료 사용자 ID", example = "user06")
    private String inspectionTypeAFinishUserId;

    @ApiModelProperty(value = "검사 유형 A 완료 일시", example = "2024-09-19 20:00:00")
    private String inspectionTypeAFinishDateTime;

    @ApiModelProperty(value = "검사 유형 B", example = "TYPE_B")
    private String inspectionTypeB;

    @ApiModelProperty(value = "검사 유형 B 시작 사용자 ID", example = "user07")
    private String inspectionTypeBStartUserId;

    @ApiModelProperty(value = "검사 유형 B 시작 일시", example = "2024-09-19 22:00:00")
    private String inspectionTypeBStartDateTime;

    @ApiModelProperty(value = "검사 유형 B 완료 사용자 ID", example = "user08")
    private String inspectionTypeBFinishUserId;

    @ApiModelProperty(value = "검사 유형 B 완료 일시", example = "2024-09-20 00:00:00")
    private String inspectionTypeBFinishDateTime;

    @ApiModelProperty(value = "X-ray 검사 코드", example = "XR01")
    private String xrayInspectionCode;

    @ApiModelProperty(value = "X-ray 검사 시작 사용자 ID", example = "user09")
    private String xrayInspectionCodeStartUserId;

    @ApiModelProperty(value = "X-ray 검사 시작 일시", example = "2024-09-20 02:00:00")
    private String xrayInspectionCodeStartDateTime;

    @ApiModelProperty(value = "X-ray 검사 완료 사용자 ID", example = "user10")
    private String xrayInspectionCodeFinishUserId;

    @ApiModelProperty(value = "X-ray 검사 완료 일시", example = "2024-09-20 04:00:00")
    private String xrayInspectionCodeFinishDateTime;

    @ApiModelProperty(value = "PALN5EM 검사 코드", example = "PL01")
    private String paln5emInspectionCode;

    @ApiModelProperty(value = "PALN5EM 검사 시작 사용자 ID", example = "user11")
    private String paln5emInspectionCodeStartUserId;

    @ApiModelProperty(value = "PALN5EM 검사 시작 일시", example = "2024-09-20 06:00:00")
    private String paln5emInspectionCodeStartDateTime;

    @ApiModelProperty(value = "PALN5EM 검사 완료 사용자 ID", example = "user12")
    private String paln5emInspectionCodeFinishUserId;

    @ApiModelProperty(value = "PALN5EM 검사 완료 일시", example = "2024-09-20 08:00:00")
    private String paln5emInspectionCodeFinishDateTime;

    @ApiModelProperty(value = "TIC 출고 스캔 사용자 ID", example = "user13")
    private String ticOutScanUserId;

    @ApiModelProperty(value = "TIC 출고 스캔 일시", example = "2024-09-20 10:00:00")
    private String ticOutScanDateTime;

    @ApiModelProperty(value = "TIC 복귀 스캔 사용자 ID", example = "user14")
    private String ticComeBackScanUserId;

    @ApiModelProperty(value = "TIC 복귀 스캔 일시", example = "2024-09-20 12:00:00")
    private String ticComeBackScanDateTime;

    @ApiModelProperty(value = "TIC 복귀 완료 여부", example = "Y")
    private String ticComeBackInFinishYn;

    @ApiModelProperty(value = "검사 완료 여부", example = "Y")
    private String inspectionFinishYn;

    @ApiModelProperty(value = "등록자 사용자 ID", example = "user15")
    private String registerUserId;

    @ApiModelProperty(value = "등록 일시", example = "2024-09-20 14:00:00")
    private String registerDateTime;
    

    /**
	  * PDA > 검사완료 에서 검사완료여부(INSPECTION_FINISH_YN)를 Y로처리 하기위한 메서드이다
	  * 이메서드가 true를 반환하면 INSPECTION_FINISH_YN를 Y로 처리하면된다
	  */
    @JsonIgnore
    public boolean isSuccess() {
        // 검사 코드가 존재하는 경우에만 해당하는 완료자 ID와 완료 일시를 체크
        boolean success = true;

        // 각 검사 코드에 대해 완료자 ID와 완료 일시가 모두 존재하는지 확인
        if(isNotNullorEmpty(getSeInspectionCode()) && !isInspectionComplete(getSeInspectionFinishUserId(), getSeInspectionFinishDateTime()) ){
            success = false;
        }
        
        if (isNotNullorEmpty(getInspectionType()) && !isInspectionComplete(getInspectionTypeFinishUserId(), getInspectionTypeFinishDateTime()) ){
            success = false;
        }

        if(isNotNullorEmpty(getInspectionTypeA()) && !isInspectionComplete(getInspectionTypeAFinishUserId(), getInspectionTypeAFinishDateTime()) ){
            success = false;
        }

        if(isNotNullorEmpty(getInspectionTypeB()) && !isInspectionComplete(getInspectionTypeBFinishUserId(), getInspectionTypeBFinishDateTime()) ){
            success = false;
        }

        if(isNotNullorEmpty(getXrayInspectionCode()) && !isInspectionComplete(getXrayInspectionCodeFinishUserId(), getXrayInspectionCodeFinishDateTime()) ){
            success = false;
        }

        if(isNotNullorEmpty(getPaln5emInspectionCode()) && !isInspectionComplete(getPaln5emInspectionCodeFinishUserId(), getPaln5emInspectionCodeFinishDateTime()) ){
            success = false;
        }

        return success;
    }

    // 검사 코드와 완료자 ID 및 완료 일시가 모두 존재해야 하는 메서드(완료자 ID와 완료 일시가 모두 있어야 함)
    @JsonIgnore
    private boolean isInspectionComplete(Object finishUserId, Object finishDateTime) {
    	if(finishUserId instanceof String && finishDateTime instanceof String) {
    		return !"".equals(finishUserId) && !"1900-01-01 00:00:00.0".equals(finishDateTime);
    	}
        
    	// 그 외의 타입일 경우 null 체크만 진행
        return finishUserId != null && finishDateTime != null;
    }
    
    // 문자열일경우 유효성체크 그외에는 null체크
    @JsonIgnore
    private boolean isNotNullorEmpty(String value) {
    	if(value instanceof String) {
    		return !"".equals(value);
    	}
        
    	return value != null;
    }
    
    @JsonIgnore
    public boolean isInspectionFieldsEmpty() {
        return "".equals(getSeInspectionCode()) &&
               "".equals(getInspectionType()) &&
               "".equals(getInspectionTypeA()) &&
               "".equals(getInspectionTypeB()) &&
               "".equals(getXrayInspectionCode());
    }
}
