package org.yht.exps.express.model.bms.bmsAllocateCar;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.exps.express.model.bms.out.BmsContainer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BmsAllocateCar", description = "배차예정정보 조회 리스트")
@Alias("BmsAllocateCar")
@EqualsAndHashCode(callSuper=false)
public class BmsAllocateCar extends ResultList{

	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="배차예정번호")
	private int allocateCarNotiNo;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="파일공유여부")
	private String fileShare;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="관세청")
	private String blStatus;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="통보일시")
	private String notifyDateTime;
	@ApiModelProperty(value="BL번호")
	private String blNo;
	@ApiModelProperty(value="HBL번호")
	private String hblNo;
	@ApiModelProperty(value="MBL번호")
	private String mblNo;
	@ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNo;
	@ApiModelProperty(value="반출입구분코드")
	private String inoutClass;
	@ApiModelProperty(value="반출입구분")
	private String inoutClassName;
	@ApiModelProperty(value="정정구분코드")
	private String changeClass;
	@ApiModelProperty(value="정정구분")
	private String changeClassName;
	@ApiModelProperty(value="보세운송근거번호")
	private String approvalNo;
	@ApiModelProperty(value="차량번호")
	private String companyTruckNo = "";
	@ApiModelProperty(value="실차량번호")
	private String realTruckNo = "";
	@ApiModelProperty(value="차량기사명")
	private String companyTruckDriver = "";
	@ApiModelProperty(value="차량기사연락처")
	private String companyTruckTel = "";
	@ApiModelProperty(value="인수자")
	private String acceptor = "";
	@ApiModelProperty(value="보세운송세관신고번호")
	private String customsReportNo;
	@ApiModelProperty(value="발생지보세구역코드")
	private String departureLocCode;
	@ApiModelProperty(value="최종보세구역코드")
	private String lastLocCode;
	@ApiModelProperty(value="반출예정일자")
	private String outEstDate;
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNo;
	@ApiModelProperty(value="처리구분")
	private String allocateYn;
	@ApiModelProperty(value="입력자ID")
	private String registerUserId = "";
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자ID")
	private String updateUserId = "";
	@ApiModelProperty(value="수정자")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;

	@ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="대표사업장코드", example="YHTXX")
    private String entBusinessCode;
    @ApiModelProperty(value="조회 그리드에서 가져온 리스트")
    private List<Map<String, Object>> dataList;
	
	/** 그리드 상단 Count */
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

    /** 화물관리번호 11-4-4 표시 */
	@ApiModelProperty(value="화물관리번호_표시")
    public String getCargoReferenceNoDisp() {
        String ret = getCargoReferenceNoDisp(this.cargoReferenceNo);
        return ret;
    }

    private String getCargoReferenceNoDisp(String _cargoReferenceNo) {
        String ret = "";
        if(_cargoReferenceNo != null) {
            if(_cargoReferenceNo.length() > 10) {
                if(_cargoReferenceNo.length() == 19) {
                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11, 15) + "-" + _cargoReferenceNo.substring(15, 19);
                } else if (_cargoReferenceNo.length() == 18) { /** 마이그레이션 값은 18이다 */
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
