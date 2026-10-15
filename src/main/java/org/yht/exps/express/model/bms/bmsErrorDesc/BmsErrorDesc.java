package org.yht.exps.express.model.bms.bmsErrorDesc;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BmsErrorDesc", description = "보세구역관리(BMS) 오류내역 공통 리스트")
@Alias("BmsErrorDesc")
@EqualsAndHashCode(callSuper=false)
public class BmsErrorDesc extends ResultList{

	@ApiModelProperty(value="오류통보번호 or 결과통보번호")
	private String errorNo;
	@ApiModelProperty(value="전송타입")
	private String docType;
	@ApiModelProperty(value="세관제출번호(신고번호 or 화물관리번호)")
	private String declarationNo;
	@ApiModelProperty(value="세관제출번호(신고번호 or 화물관리번호)")
	private String declarationNoDisp;
	@ApiModelProperty(value="정정차수")
	private String changeSeqNo;
	@ApiModelProperty(value="오류내역 or 신고각하/거절사유")
	private String errorDesc;
	@ApiModelProperty(value="통보일시")
	private String noticeDateTime;

    /** 세관제출번호(신고번호 or 화물관리번호) */
	@ApiModelProperty(value="세관제출번호(신고번호 or 화물관리번호) 표시")
    public String getDeclarationNoDisp() {
        String ret = getDeclarationNoDisp(this.declarationNo, this.docType);
        return ret;
    }

    private String getDeclarationNoDisp(String _declarationNo, String _docType) {
        String ret = "";

        /** 반출통고 - 화물관리번호 */
        if("5II".equals(_docType)) {
            if(_declarationNo.length() > 10) {
                if(_declarationNo.length() == 19) {
                    ret = _declarationNo.substring(0, 11) + "-" + _declarationNo.substring(11, 15) + "-" + _declarationNo.substring(15, 19);
                } else if (_declarationNo.length() == 18) { /** 마이그레이션 값은 18이다 */
                    ret = _declarationNo.substring(0, 11) + "-" + _declarationNo.substring(11, 15) + "-" + _declarationNo.substring(15);
                } else {
                    ret = _declarationNo.substring(0, 11) + "-" + _declarationNo.substring(11);
                }
            } else {
                ret = _declarationNo;
            }
        }
        /** 화물속성 5TL - 화물관리번호 */
        else if("5TL".equals(_docType)) {
        	if(_declarationNo.length() > 10) {
        		if(_declarationNo.length() == 19) {
        			ret = _declarationNo.substring(0, 11) + "-" + _declarationNo.substring(11, 15) + "-" + _declarationNo.substring(15, 19);
        		} else {
        			ret = _declarationNo.substring(0, 11) + "-" + _declarationNo.substring(11);
        		}
        	} else {
        		ret = _declarationNo;
        	}
        }
        /** 하기이상결과 5IG - MRN */
        else if("5IG".equals(_docType)) {
        	if(_declarationNo.length() > 11) {
        		ret = _declarationNo.substring(0, 11) + "-" + _declarationNo.substring(11);
        	} else {
        		ret = _declarationNo;
        	}
        }
        /** 그 외 - 신고번호 */
        else {
        	ret = _declarationNo.substring(0, 8) + "-" + _declarationNo.substring(8, 10) + "-" + _declarationNo.substring(10, 18);
        }

        return ret;
    }

}
