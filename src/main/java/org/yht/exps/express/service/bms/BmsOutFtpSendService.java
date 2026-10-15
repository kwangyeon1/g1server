package org.yht.exps.express.service.bms;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.BmsOutMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.bms.out.BatchBmsOut;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutSaveResult;
import org.yht.exps.express.model.bms.out.ftp.FtpOut;
import org.yht.exps.express.model.bms.out.search.SearchBmsOut;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.edi.EdiCommonService;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 특송장 이고 FTP 서비스 전송 처리
 *
 */
@Slf4j
@Service
@Transactional
public class BmsOutFtpSendService {

	@Autowired
	BmsOutMapper bmsOutMapper;

	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	private CustomProperties customProperties;
	@Autowired
	StringRedisTemplate redisTemplate;
	
	@Resource(name="ediCommonService")
    EdiCommonService ediCommonService;
	@Resource(name = "commonService")
	CommonService commonService;

	@Autowired
	private Environment environment;

	/***
	 * 선택된 항목 FTP 전송 처리 방법
	 * 1) 선택된 항목에 대해서 FTP 전송 데이터 조회 : BMS_IN, BMS_OUT 기준 조회
	 * 2) 이 데이터는 계속 보내도 되므로 따로 전송이력을 관리 하지 않는다.
	 * 3) 각 컬럼에 대해서 FTP_특송센타 이고  스펙에 따라서 NUMBER 타입은 자릿수 만큼 0으로 채우고, VARCHAR 타입은 공백을 채워서 전송
	 * 4) 여러개 선택시 선택된 항목에 대해서 한개의 파일로 만들어서 전송 한다.
	 * 5) 파잉ㄹ 생성규칙은 운영 K002_kctdi0000001.txt로 만들어서 전송하고, 테스트의 경우 T002로 시작해서 전송 해야 하므로 프로파일 사용해서 테스트 한다.
	 *
	 * @param batchBmsOut
	 * @return
	 * @throws Exception
	 */
	public BmsOutSaveResult sendOutFtpData(BatchBmsOut batchBmsOut ) throws Exception{
		BmsOutSaveResult result = new BmsOutSaveResult();
// 에러 정보 리턴
		List<String> errList = new ArrayList<String>();
		List<FtpOut> succList = new ArrayList<FtpOut>();
		SearchBmsOut search = new SearchBmsOut();
		search.setSrchEntCode(batchBmsOut.getEntCode());
		search.setSrchEntBusinessCode(batchBmsOut.getEntBusinessCode());
		search.setSrchUserLanguage(batchBmsOut.getUserLanguage());
		List<String> outNos = new ArrayList<>();
		List<BmsOut> outLst = batchBmsOut.getBmsOutList();
		if(outLst.size()>0){
			for(BmsOut out : outLst){ // 선택된 반출 번호 리스트 넣기
				outNos.add(String.valueOf(out.getOutNo()));
			}
		} else{ // 선택된  데이터 0일경우 조회 안돼는값 매핑
			outNos.add("0");
		}
		// outNo List 연결
		search.setSearchNoList(outNos);


		//1) 선택된 항목에 대해서 FTP 전송 데이터 조회 : BMS_IN, BMS_OUT 기준 조회
		List<FtpOut>  lst = bmsOutMapper.selectBmsOutFtpList(search);

        //3) 각 컬럼에 대해서 FTP_특송센타 이고  스펙에 따라서 NUMBER 타입은 자릿수 만큼 0으로 채우고, VARCHAR 타입은 공백을 채워서 전송
		//StringBuffer sb = new StringBuffer();
		String sendStr = "";
		Map<String,Object> docSend = new HashMap<>();
		docSend.put("entCode",batchBmsOut.getEntCode());  // 화면에서 가져와야 함
		docSend.put("entBusinessCode",batchBmsOut.getEntBusinessCode()); // 화면에서 가져와야 함
		docSend.put("userLanguage", batchBmsOut.getUserLanguage()); // 화면에서 가져와야 함
 		docSend.put("bound", "BMS");
		docSend.put("ediCityPort", "ICN");
 		/** 지정장치장정보조회 */
		Map<String, Object>  kctdiInfo = manifestMapper.selectKctdiInfo(docSend);
		if(kctdiInfo == null || kctdiInfo.size() == 0){
			errList.add("특송장 이고 FTP 송신의 지정장치장정보가 없습니다.");
		} else {
			if(Util.null2str(kctdiInfo.get("EDI_FTP_IP")).equals("") || Util.null2str(kctdiInfo.get("EDI_FTP_ID")).equals("") || Util.null2str(kctdiInfo.get("EDI_FTP_PW")).equals("")){
				errList.add("특송장 이고 FTP 송신의 FTP 설정을 확인해 주시기 바랍니다.");
			}
		}
		if(errList.size()> 0){ // 에러 발생 리턴값 처리
			result.setResultCnt(0); // 정상 성공건
			result.setErrList(errList);
			return result;
		}
		//////////////////////////////////////////
		// 파일 생성
		//////////////////////////////////////////
		String nowDateTime6 = DateTimeUtil.addTimes(0, "yyMMddHHmmss");
		FtpOut fwdOut = null;
		String commFwdCode ="";
		if(lst.size()>0){ // 리스트에서 포워더 코드를 찾아서 첫번째 로우의 코드를 넣는다.
			fwdOut = lst.get(0);
			commFwdCode = fwdOut.getFwdCode();
		}
		String fileName = "";
		// 운영에서는 K002로 시작 하는 구분
		// Active Profile에 의거 해서 파일 생성 함 // 포워더 코드(8)+ 현재일시(6)8+6자리
		if( "ups1".equals(environment.getActiveProfiles()[0])){
			fileName = "K002_" + commFwdCode + nowDateTime6 + ".txt";
		}else {
			fileName = "K002_" + commFwdCode + nowDateTime6 + ".txt";
		}
		
		String dirPath[] = ediCommonService.getSendFilePath(null, "bms/out", docSend);	// 로컬폴더 경로 가져오기
		BufferedWriter bfw = null;
		Integer resultCnt = 0 ;
		try {
			    bfw = ediCommonService.getFileWriterOpen(fileName, dirPath);	// 쓰기 파일 열기
				for( FtpOut ftpOut: lst){
					// 2025-06-13 진행상태 "수입통관이고" 조건 추가
					if(!"BMS04".equals(ftpOut.getLastPodStatus()) && !"BMSA4".equals(ftpOut.getLastPodStatus())){ // (1) 반출(TB_BMS_OUT)에 현재진행상태코드(LAST_POD_STATUS) 가 'BMS04'(접수) 가 아닌 경우 오류메세지변수 <- "BL:#1# 진행상태 접수만 가능합니다."
						errList.add("BL:"+ftpOut.getHblNo()+" 진행상태가 접수 또는 수입통관이고인 경우만 가능합니다.");
					} else { // (2) 위 부분에 해당되지 아닌 경우  반출구분(OUT_CLASS) 이 "61" AND 반출근거번호 (OUT_APPROVAL_NO) "04002001" 인 자료가 있는지 있는 경우 오류메세지변수 <- "BL:#1# 이고반출,근거번호04002001 확인"
						if( !("61".equals(ftpOut.getOutClass()) && "04002001".equals(ftpOut.getOutApprovalNo()))) {
							errList.add("BL:"+ftpOut.getHblNo()+" 이고반출,근거번호04002001 확인");
						}else{
							resultCnt++ ;
							succList.add(ftpOut);
							if( "D".equals(Util.null2str(batchBmsOut.getTicOutDataCreateGbn())) ) {
								ftpOut.setDataCreatGbn(batchBmsOut.getTicOutDataCreateGbn());
								sendStr = getFtpSendData(ftpOut);
								bfw.append(sendStr);
								bfw.append("\n");
							} else if("Y".equals(ftpOut.getTicOutFtpSendYn())) {
								ftpOut.setDataCreatGbn("D");
								sendStr = getFtpSendData(ftpOut);
								bfw.append(sendStr);
								bfw.append("\n");
								sendStr = "";
								
								ftpOut.setDataCreatGbn("I");
								sendStr += getFtpSendData(ftpOut);
								bfw.append(sendStr);
								bfw.append("\n");
							}else {
								sendStr = getFtpSendData(ftpOut);
								bfw.append(sendStr);
								bfw.append("\n");
							}
							bfw.flush();
						}
					}
					/*resultCnt++ ;
					sendStr = getFtpSendData(ftpOut);
					bfw.append(sendStr);
					bfw.append("\n");
					bfw.flush(); */
				}// end for

			} catch(Exception e) {
				throw e;
			} finally {
				if (bfw != null) {
					try{ bfw.close(); }catch(Exception e){ log.error(Util.getPrintStacTraceString(e)); }
				}
			}
		log.info("sendOutFtpData resultCnt:{}, errList.size:{}" , resultCnt, errList.size());
		//resultCnt 0 이면 전송대상 자료 없음
		if(resultCnt==0){
			result.setResultCnt(resultCnt); // 정상 성공건
			result.setErrList(errList); // 에러 정보 리턴
			return result;
		}
		String fileCreateRetVal[] = new String[6];
		fileCreateRetVal[0] = fileName; // 파일명
		fileCreateRetVal[1] = "";
		fileCreateRetVal[2] = dirPath[0];	// 원본 로컬 경로
		fileCreateRetVal[3] = dirPath[1];	// 백업 로컬 경로
		fileCreateRetVal[4] = "BMS_OUT_GW_AIR";
		fileCreateRetVal[5] = "DUMMY_BMS_000001";

		//////////////////////////////////////////
		// 파일 전송
		//////////////////////////////////////////
	 	//masterOrder Hash의 경우는 MBLNO에 그냥 더미값 넣어서 보낸다.
		Map<String, Object> masterOrder = new HashMap<>();
		masterOrder.put("MBL_NO", "DUMMY_BMS_000001");
		if (!ediCommonService.ftpFileSendGw(docSend, masterOrder, fileCreateRetVal, kctdiInfo, errList)) {
			// 실패시 이동 처리 템프 파일에서 이동 처리
			ediCommonService.moveFile(fileCreateRetVal[0], fileCreateRetVal[0], fileCreateRetVal, false);
			errList = new ArrayList<String>();
			errList.add(  "전송건: "+resultCnt+"건 파일:"+fileName+" FTP 파일 전송 실패");
			result.setErrList(errList); // 에러 정보 리턴
			result.setResultCnt(0); // 정상 성공건
			return result;
		}// 실패 건수
		result.setResultCnt(resultCnt); // 정상 성공건
		result.setErrList(errList); // 에러 정보 리턴
		if(succList.size() > 0 && result.getResultCnt() > 0) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("inputUserId", batchBmsOut.getInputUserId());
			map.put("updateOutList", succList);
			bmsOutMapper.update61BmsOutFtpSendY(map);
		}
	 	return result;
	}

	private String getFtpSendData(FtpOut ftpOut) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(Util.rpad(ftpOut.getPart() ,5	));//- 사무소 코드
		sb.append(Util.rpad(ftpOut.getInNo1() ,4	));//- 반입년도(YYYY)
		sb.append(Util.rpad(ftpOut.getInNo2() ,8	));//년도별 일련번호
		sb.append(Util.rpad(ftpOut.getMrn() ,11	));//- 적하목록 번호
		sb.append(Util.rpad(ftpOut.getMsn() ,4	));//적하목록 마스터 일련번호
		sb.append(Util.rpad(ftpOut.getHsn() ,4	));//적하목록 하우스 일련번호
		sb.append(Util.rpad(ftpOut.getPkgGbn() ,2	));//10:일반, 11:관리, 12:이사, 14:특송, 15:수출, K1016:면세, 17:조달
		sb.append(Util.rpad(ftpOut.getInDt() ,14	));//- “YYYYMMDDHH24MISS”
		sb.append(Util.rpad(ftpOut.getCusCd1()  ,3	));//- 김포:041, 인천공항:040 040 고정문구
		sb.append(Util.rpad(ftpOut.getOwnerCd() ,4	));//- ●화주코드테이블 참조(화주코드 없을 때 “9999”) (포워더 코드)
		sb.append(Util.rpad(ftpOut.getOwnerComp()  ,100	));//- 화주명 없을 때 “anonym”
		sb.append(Util.rpad(ftpOut.getItemCd() ,2	));//- ● 품목코드 테이블 참조
		sb.append(Util.rpad(Util.null2str(ftpOut.getItemNm1()).replaceAll("[^\\x00-\\x7F]", " ")  ,200	));//- 품명 없을 때 “기타”로 고정
		sb.append(Util.rpad(ftpOut.getMblNo() ,20));//
		sb.append(Util.rpad(ftpOut.getHblNo() ,20));//
		sb.append(Util.rpad(ftpOut.getShipNo() ,35	));//- 반입시점의 선, 기명
		sb.append(Util.rpad(ftpOut.getInGubun()  ,2	));//- ● 반입구분 코드 테이블 참조
		sb.append(Util.rpad(ftpOut.getBondFm() ,8	));//- “YYYYMMDD”
		sb.append(Util.rpad(ftpOut.getBondTo() ,8	));//- “YYYYMMDD”
		sb.append(Util.rpad(ftpOut.getSingoReqno()  ,20));//
		sb.append(Util.rpad(ftpOut.getBondCompCd()  ,20));//
		sb.append(Util.rpad(ftpOut.getPackKind()  ,3	));//- ● 포장종류 코드 테이블 참조
		sb.append(Util.rpad(ftpOut.getInSagoCd()  ,3	));//- ● 사고 코드 테이블 참조
		sb.append(Util.lpad(Util.null2str(ftpOut.getPackCnt())  ,8,'0' ));//- 공백은 “0”으로 채움, 예) 00000015
		sb.append(Util.lpad(Util.null2str(ftpOut.getOutQty() ) ,8	,'0' ));//- 공백은 “0”으로 채움, 예) 00000015
		sb.append(Util.lpad(Util.null2str(ftpOut.getJegoQty())  ,8	,'0' ));//- 공백은 “0”으로 채움, 예) 00000015
		sb.append(Util.lpad(Util.null2str(ftpOut.getTotWgt() ) ,10	,'0' ));//- 6,3 예)15.4일때, 000015.400
		sb.append(Util.lpad(Util.null2str(ftpOut.getOutWgt() ) ,10	,'0' ));//- 6,3 예)15.4일때, 000015.400
		sb.append(Util.lpad(Util.null2str(ftpOut.getJegoWgt())  ,10	,'0' ));//- 6,3 예)15.4일때, 000015.400
		sb.append(Util.lpad(Util.null2str(ftpOut.getTotCbm() ) ,6	,'0' ));//- 공백은 “0”으로 채움, 예) 000015
		sb.append(Util.lpad(Util.null2str(ftpOut.getOutCbm1())  ,6	,'0' ));//- 공백은 “0”으로 채움, 예) 000015
		sb.append(Util.lpad(Util.null2str(ftpOut.getOutCbm() ) ,6	,'0' ));//- 공백은 “0”으로 채움, 예) 000015
		sb.append(Util.rpad(ftpOut.getPlcLoc() ,10));//
		sb.append(Util.rpad(ftpOut.getPlcConfDt() ,8));//
		sb.append(Util.rpad(ftpOut.getPlcConfId() ,10));//
		sb.append(Util.rpad(ftpOut.getOverYn() ,1	));//- 1 : 체화, 0 : 정상
		sb.append(Util.rpad(ftpOut.getOverNo() ,10	));//OVER_NO	10		"          " 공백 10자리
		sb.append(Util.rpad(ftpOut.getOverDt() ,8	));//- “YYYYMMDD”
		sb.append(Util.rpad(ftpOut.getDataCreatGbn(),1	));//- 생성 : “I”, 정정 : “U”, 삭제 : “D”
		sb.append(Util.rpad(ftpOut.getUserId() ,10	));//- 유저 아이디
		sb.append(Util.rpad(ftpOut.getUserPw() ,10	));//- 유저의 비밀번호
		sb.append(Util.rpad(ftpOut.getWorkDt() ,8	));//- Data 생성 또는 수정 된 날짜
		sb.append(Util.rpad(ftpOut.getTermId() ,5	));//- 스캐너 장비 번호
		sb.append(Util.rpad(ftpOut.getElecYn() ,1	));//- Default 0, 전자상거래 1
		sb.append(Util.rpad(ftpOut.getInType()  ,2	));//"1:목록 2: 검사(목록) 3:간이 4:일반 5:검사(일반/간이) 6:검역(사전검역 요청건) 7:환적 8: 검역(동물검역) 9: 검역(식물검역) 10: 검역(식품검역) 11:검역(서류), 91.거대화물 13 : 개인통관부호 미제출"
		sb.append(Util.rpad(ftpOut.getSpcPkgGbn() ,1	));//- 1:냉동,냉장,2:귀금속,3:무기,4:검역대상
		sb.append(Util.rpad(ftpOut.getJoupDt() ,14	));// JOUP_DT       	14	- “YYYYMMDDHHMISS”
		sb.append(Util.rpad(ftpOut.getInPortDt() ,14	));//- “YYYYMMDDHHMISS”
		sb.append(Util.rpad(ftpOut.getVal() ,18	));//- 감정가격  공백 18자리
		sb.append(Util.rpad(ftpOut.getDuty() ,18	));//- 해당 물품의 관세, 공백 18자리
		sb.append(Util.rpad(ftpOut.getUnldPortCd() ,5	));//적재항 부호 기재 (UN/LOCODE)
		sb.append(Util.rpad(ftpOut.getSendNatCd() ,2	));//발송국 국가부호 기재 (ISO 코드)
		sb.append(Util.rpad(Util.null2str(ftpOut.getSendOffice()).replaceAll("[^\\x00-\\x7F]", " ")  ,100	));//상호 또는 성명
		sb.append(Util.rpad(Util.null2str(ftpOut.getSendAddr1()).replaceAll("[^\\x00-\\x7F]", " ") ,50	));//주소1
		sb.append(Util.rpad(Util.null2str(ftpOut.getSendAddr2()).replaceAll("[^\\x00-\\x7F]", " ") ,50	));//주소2
		sb.append(Util.rpad(Util.null2str(ftpOut.getSendAddr3()).replaceAll("[^\\x00-\\x7F]", " ") ,50	));//주소3
		sb.append(Util.rpad(ftpOut.getRecvTel() ,40	));//숫자형태로만 기재
		sb.append(Util.rpad(Util.null2str(ftpOut.getRecvNm()).replaceAll("[^\\x00-\\x7F]", " ") ,100	));//상호 또는 성명
		sb.append(Util.rpad(Util.null2str(ftpOut.getRecvAddr1()).replaceAll("[^\\x00-\\x7F]", " ") ,50	));//주소1
		sb.append(Util.rpad(Util.null2str(ftpOut.getRecvAddr2()).replaceAll("[^\\x00-\\x7F]", " ") ,50	));//주소2
		sb.append(Util.rpad(Util.null2str(ftpOut.getRecvAddr3()).replaceAll("[^\\x00-\\x7F]", " ") ,50	));//주소3
		sb.append(Util.rpad(ftpOut.getRecvPostNo() ,5	));//수하인 우편번호 기재(관세청요구사항)
		sb.append(Util.rpad(ftpOut.getElecDomain() ,100	));//전자상거래 사이트주소를 기재함
		sb.append(Util.rpad(ftpOut.getInvAmt() ,18	));//통관대상 물품의 가격을 기재 공백 18자리
		sb.append(Util.rpad(ftpOut.getInvAmtUnit() ,2	));//"항목 64(물품가격) 에 대한 단위미화:US,  한화:KR"
		sb.append(Util.rpad(ftpOut.getDirectNo() ,10	));//특별통관대상업체 지정번호
		sb.append(Util.rpad(ftpOut.getTradeType() ,1	));//"(A:개인직접수입형 전자상거래(배송/결재대행포함),B:수입대행형 전자상거래,  C:일반"
		sb.append(Util.rpad(ftpOut.getUseGbn() ,1	));//물품 용도구분 기재(1:개인, 2:회사)
		sb.append(Util.rpad(ftpOut.getContrNo1() ,11	));//컨테이너 번호를 기재
		sb.append(Util.rpad(ftpOut.getContrNo2() ,11));//
		sb.append(Util.rpad(ftpOut.getFwdCode()  ,4   ));//     환경설정(TB_SMS_ENT_BUS_OPTION[SOLUTION "BMS", OPTION_CODE "A3"]통합특송장FTP 포더부호
		sb.append(Util.rpad(ftpOut.getFwdFirm()  ,100 ));//   환경설정(TB_SMS_ENT_BUS_OPTION[SOLUTION "BMS", OPTION_CODE "A4"]통합특송장FTP 포더부호명
		sb.append(Util.rpad(ftpOut.getPassReasion(), 1	));//null:검사미지정 / 1:마약 / 2:총기 / 3:검역 / 4:식품 / 5:의약품 / 6:수입검사
		sb.append(Util.rpad(ftpOut.getTakbaeNo(), 10	)); //// TAKBAE_NO	10	EMS : 우체국, CJ : CJ대한통운(향후 추가)
		sb.append(Util.rpad(ftpOut.getTakbaeLoc()  ,10	));//택배사별 코드 참조 ( 향후배포)
		sb.append(Util.rpad(ftpOut.getUniMatt()  ,2	));//NULL: 특이사항없음 / 01:보류(세관), 51:DO확인, 52:LG확인
		sb.append("*"); //"*"  고정 EOF

		return sb.toString();
	}
}