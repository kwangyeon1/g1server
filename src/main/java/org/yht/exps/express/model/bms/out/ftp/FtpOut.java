package org.yht.exps.express.model.bms.out.ftp;

import io.swagger.annotations.ApiModel;
import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "FtpOut", description = "특송장이고 VO 객체")
@Alias("ftpOut")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class FtpOut {
    @Builder.Default  private String part       =""; //사무소 코드

    @Builder.Default  private String inNo1          =""; //반입번호(년도)

    @Builder.Default  private String inNo2          =""; //반입번호(일련번호)

    @Builder.Default  private String mrn          =""; //MRN

    @Builder.Default  private String msn          =""; //MSN

    @Builder.Default  private String hsn          =""; //HSN

    @Builder.Default  private String pkgGbn          =""; //화물구분 코드

    @Builder.Default  private String inDt          =""; //반입예정일자

    @Builder.Default  private String cusCd1          =""; //입항세관

    @Builder.Default  private String ownerCd          =""; //화주코드

    @Builder.Default  private String ownerComp          =""; //화주명

    @Builder.Default  private String itemCd          =""; //품목코드

    @Builder.Default  private String itemNm1          =""; //품명

    @Builder.Default  private String mblNo          =""; //M B/L

    @Builder.Default  private String hblNo          =""; //H B/L

    @Builder.Default  private String shipNo          =""; //선명(기명)

    @Builder.Default  private String inGubun          =""; //반입구분코드

    @Builder.Default  private String bondFm          =""; //보세운송기간(from)

    @Builder.Default  private String bondTo          =""; //보세운송기간(to)

    @Builder.Default  private String singoReqno          =""; //보세운송 면허번호

    @Builder.Default  private String bondCompCd          =""; //보세운송회사

    @Builder.Default  private String packKind          =""; //포장종류

    @Builder.Default  private String inSagoCd          =""; //반입사고구분 코드

    @Builder.Default  private String packCnt          =""; //반입 수량

    @Builder.Default  private String outQty          =""; //반출 수량

    @Builder.Default  private String jegoQty          =""; //재고 수량

    @Builder.Default  private String totWgt          =""; //반입 중량

    @Builder.Default  private String outWgt          =""; //반출 중량

    @Builder.Default  private String jegoWgt          =""; //재고 중량

    @Builder.Default  private String totCbm          =""; //반입 용적

    @Builder.Default  private String outCbm1          =""; //반출 용적

    @Builder.Default  private String outCbm          =""; //재고 용적

    @Builder.Default  private String plcLoc          =""; //장치위치

    @Builder.Default  private String plcConfDt          =""; //장치확인일자

    @Builder.Default  private String plcConfId          =""; //장치확인자

    @Builder.Default  private String overYn          =""; //체화여부

    @Builder.Default  private String overNo          =""; //체화번호

    @Builder.Default  private String overDt          =""; //체화일자

    @Builder.Default  private String dataCreatGbn          =""; //데이터 생성 구분

    @Builder.Default  private String userId          =""; //사용자코드

    @Builder.Default  private String userPw          =""; //비밀번호

    @Builder.Default  private String workDt          =""; //작업일자

    @Builder.Default  private String termId          =""; //단말기 번호

    @Builder.Default  private String elecYn          =""; //전자상거래 여부

    @Builder.Default  private String inType          =""; //목록구분(반입유형)

    @Builder.Default  private String spcPkgGbn          =""; //특수화물 구분

    @Builder.Default  private String joupDt          =""; //조업일자

    @Builder.Default  private String inPortDt          =""; //입항일자

    @Builder.Default  private String val          =""; //감가

    @Builder.Default  private String duty          =""; //관세

    @Builder.Default  private String unldPortCd          =""; //적재항

    @Builder.Default  private String sendNatCd          =""; //발송국

    @Builder.Default  private String sendOffice          =""; //송하인 명

    @Builder.Default  private String sendAddr1          =""; //송하인 주소1

    @Builder.Default  private String sendAddr2          =""; //송하인 주소2

    @Builder.Default  private String sendAddr3          =""; //송하인 주소3

    @Builder.Default  private String recvTel          =""; //수하인 전화번호

    @Builder.Default  private String recvNm          =""; //수하인 명

    @Builder.Default  private String recvAddr1          =""; //수하인 주소1

    @Builder.Default  private String recvAddr2          =""; //수하인 주소2

    @Builder.Default  private String recvAddr3          =""; //수하인 주소3

    @Builder.Default  private String recvPostNo          =""; //수하인 우편번호

    @Builder.Default  private String elecDomain          =""; //전자상거래 사이트주소

    @Builder.Default  private String invAmt          =""; //물품가격

    @Builder.Default  private String invAmtUnit          =""; //물품가격 단위

    @Builder.Default  private String directNo          =""; //지정번호

    @Builder.Default  private String tradeType          =""; //거래유형코드

    @Builder.Default  private String useGbn          =""; //용도구분

    @Builder.Default  private String contrNo1          =""; //컨테이너 번호1

    @Builder.Default  private String contrNo2          =""; //컨테이너 번호2

    @Builder.Default  private String fwdCode          =""; //포더부호

    @Builder.Default  private String fwdFirm          =""; //포더명(특송업체명)

    @Builder.Default  private String passReasion          =""; //UNI-PASS 검사지정사유

    @Builder.Default  private String takbaeNo          =""; //택배사번호

    @Builder.Default  private String takbaeLoc          =""; //택배집하장번호

    @Builder.Default  private String uniMatt          =""; //특이사항

    @Builder.Default  private String lastPodStatus="";  /* BMS04 체크용 */
    @Builder.Default  private String outClass="";        /* 61.이고반출(적출입작업) 확인용 */
    @Builder.Default  private String outApprovalNo=""; /* 04002001 근거 번호 확인용 */
    
    @Builder.Default  private String ticOutFtpSendYn="N"; /* 반출(TB_BMS_OUT) 에  TIC이고반출FTP전송여부(TIC_OUT_FTP_SEND_YN) */
    private Integer outNo;
}
