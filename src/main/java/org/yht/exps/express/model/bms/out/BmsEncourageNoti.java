package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Builder
@Data
@ApiModel(value = "BmsEncourageNoti", description = "선반입반출신고독려통보 VO 객체")
@Alias("bmsEncourageNoti")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsEncourageNoti extends ResultList {

	/** 선반입반출신고독려통보 조회 */
	@ApiModelProperty(value="총건수")
	private int totalCnt;
	@ApiModelProperty(value="목록건수")
	private int totalSimpleCnt;
	@ApiModelProperty(value="일반건수")
	private int totalGeneralCnt;
	@ApiModelProperty(value="총건수")
	private int cnt;
	@ApiModelProperty(value="목록건수")
	private int simpleCnt;
	@ApiModelProperty(value="일반건수")
	private int generalCnt;
	@ApiModelProperty(value="파일공유여부")
	private String fileShare;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="반출처리여부")
	private String outYn;
	@ApiModelProperty(value="관세청")
	private String blStatus;
	@ApiModelProperty(value="선반입반출독려통보번호")
	private String encourageNotiNo;
    @ApiModelProperty(value="반입일자")
    private String carryinDate;
    @ApiModelProperty(value="반출승인일자")
    private String approvalDate;
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="MBL번호")
    private String mblNo;
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="반출근거번호")
    private String outApprovalNo;
    @ApiModelProperty(value="화물구분")
    private String cdType;
    @ApiModelProperty(value="장치장소부호")
    private String bwhId;
    @ApiModelProperty(value="승인수량")
    private String approvalCt;
    @ApiModelProperty(value="승인중량")
    private String approvalWt;
    @ApiModelProperty(value="입력자코드")
    private String registerUserId;
    @ApiModelProperty(value="입력일시")
    private String registerDateTime;

    /**
     * 화물관리번호 11-4-4 표시 (그리드에서 사용)
     * @return
     */
    @ApiModelProperty(value="화물관리번호_표시")
    public String getCargoReferenceNoDisp() {
        String ret = getCargoReferenceNoDisp(this.cargoReferenceNo);
        return ret;
    }

    private String getCargoReferenceNoDisp(String _cargoReferenceNo) {
        String ret = "";
        if(!_cargoReferenceNo.equals("") ) {
            if(_cargoReferenceNo.length() > 10) {
                if(_cargoReferenceNo.length() == 19) {
                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11, 15) + "-" + _cargoReferenceNo.substring(15, 19);
                } else if (_cargoReferenceNo.length() == 18) { // 마이그레이션 값은 18이다
                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11, 15) + "-" + _cargoReferenceNo.substring(15);
                } else {
                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11);
                }
            } else {
                ret = _cargoReferenceNo;
            }
        }

        return ret;
    }
}
