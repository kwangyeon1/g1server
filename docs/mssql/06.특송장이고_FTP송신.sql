SELECT
    '11110'  AS PART
     ,  LEFT(B.CARRYIN_DATE,4)   AS IN_NO1
     , B.IN_NO  AS IN_NO2
     , (CASE   WHEN A.CARGO_REFERENCE_NO IS NULL THEN ''   ELSE SUBSTRING(A.CARGO_REFERENCE_NO, 1, 11) END) AS MRN
     , (CASE   WHEN A.CARGO_REFERENCE_NO IS NULL THEN ''   ELSE SUBSTRING(A.CARGO_REFERENCE_NO, 12, 4) END) AS MSN
     , (CASE   WHEN A.CARGO_REFERENCE_NO IS NULL THEN ''   ELSE SUBSTRING(A.CARGO_REFERENCE_NO, 16, 4) END) AS HSN
     , '14' AS PKG_GBN
     , CONVERT(varchar, GETDATE(), 112) + REPLACE(CONVERT(varchar, GETDATE(), 108), ':', '') AS IN_DT
     , '040' AS CUS_CD1
     , eb03.SET_VALUE AS OWNER_CD
     , eb04.SET_VALUE  AS OWNER_COMP
     , '99' AS ITEM_CD
     , B.COMMODITY_ITEM_LOC AS ITEM_NM1
     , B.MBL_NO AS MBL_NO
     , B.BL_NO AS HBL_NO
     , B.VESSEL_FLIGHT AS SHIP_NO
     , '21' AS IN_GUBUN
     , '' AS BOND_FM /* 공백 8자리 */
     , '' AS BOND_TO /* 공백 8자리 */
     , '' AS SINGO_REQNO /* 공백 20자리 */
     , '' AS BOND_COMP_CD /* 공백 20자리 */
     , B.PKG_TYPE AS PACK_KIND
     , 'OKY' AS IN_SAGO_CD
     , B.CT AS PACK_CNT
     , '00000000' AS OUT_QTY
     , '00000000' AS JEGO_QTY
     , B.WT AS TOT_WGT
     , '000000.000' AS OUT_WGT
     , '000000.000' AS JEGO_WGT
     , '000000' AS TOT_CBM
     , '000000' AS OUT_CBM1
     , '000000' AS OUT_CBM
     , eb03.SET_VALUE AS PLC_LOC
     , '' AS PLC_CONF_DT /* 공백 8자리 */
     , '' AS PLC_CONF_ID /* 공백 10자리 */
     , '0' AS OVER_YN
     , '' AS OVER_NO
     , '' AS OVER_DT
     , 'I' AS DATA_CREAT_GBN
     , '' AS USER_ID
     , '' AS USER_PW
     , CONVERT(varchar, GETDATE(), 112) AS WORK_DT
     , '' AS TERM_ID
     , '0' AS ELEC_YN
     , B.IN_CLASS AS IN_TYPE
     , '' AS SPC_PKG_GBN
     , '' AS JOUP_DT
     ,  REPLACE(B.AR_DATE,'-', '') AS IN_PORT_DT
     , '' AS VAL
     , '' AS DUTY
     , '' AS UNLD_PORT_CD
     , B.ORIG_COUNTRY AS SEND_NAT_CD
     , B.SHIPPER_NAME  AS SEND_OFFICE
     , SUBSTRING(B.SHIPPER_ADDRESS_1,1,50)  AS SEND_ADDR1
     , SUBSTRING(B.SHIPPER_ADDRESS_1,51,50)  AS SEND_ADDR2
     , SUBSTRING(B.SHIPPER_ADDRESS_1,101,50)  AS SEND_ADDR3
     , B.CONSIGNEE_TEL AS RECV_TEL
     , B.CONSIGNEE_NAME AS RECV_NM
     , SUBSTRING(B.CONSIGNEE_ADDRESS_1,1,50)  AS RECV_ADDR1
     , SUBSTRING(B.CONSIGNEE_ADDRESS_1,51,50)  AS RECV_ADDR2
     , SUBSTRING(B.CONSIGNEE_ADDRESS_1,101,50)  AS RECV_ADDR3
     , '' AS RECV_POST_NO
     , '' AS ELEC_DOMAIN
     , '' AS INV_AMT
     , '' AS INV_AMT_UNIT
     , '' AS DIRECT_NO
     , B.ECM_TYPE AS TRADE_TYPE
     , B.CD_USE_TYPE AS USE_GBN
     , '' AS CONTR_NO1
     , '' AS CONTR_NO2
     , eb03.SET_VALUE AS FWD_CODE
     ,  eb04.SET_VALUE AS FWD_FIRM
     , B.XRAY_INSPECTION_CODE
     , (case B.XRAY_INSPECTION_CODE
            when '01' then '1'
            when '02' then '2'
            when '03' then '3'
            when '04' then '4'
            when '05' then '4'
            when '06' then '6'
            else '' end ) AS PASS_REASION
     , '' AS TAKBAE_NO
     , '' AS TAKBAE_LOC
     , '' AS UNI_MATT
FROM 	TB_BMS_OUT A
            LEFT JOIN TB_BMS_IN B
                      ON A.IN_NO = B.IN_NO
            LEFT JOIN TB_SMS_ENT_BUS_OPTION eb03
                      ON eb03.ENT_CODE = 'YHTXX' AND eb03.ENT_BUSINESS_CODE = 'YHTXX-KR'
                          AND  eb03.OPTION_CODE ='A03'
            LEFT JOIN TB_SMS_ENT_BUS_OPTION eb04
                      ON eb04.ENT_CODE = 'YHTXX' AND eb04.ENT_BUSINESS_CODE = 'YHTXX-KR'
                          AND  eb04.OPTION_CODE ='A04'
WHERE 1 = 1
  AND A.OUT_NO = 560
;

select * from TB_SMS_ENT_BUS_OPTION tsebo
WHERE OPTION_CODE IN ('A01','A02','A03','A04');

select * from TB_SMS_ENT_BUS_OPTION tsebo
WHERE OPTION_CODE ='A04';


private String last_pod_status  /* BMS04 체크용 */
private String out_class        /* 61.이고반출(적출입작업) 확인용 */
private String out_approval_no


PART          	5	- 사무소 코드
IN_NO1        	4	- 반입년도(YYYY)
IN_NO2        	8	년도별 일련번호
MRN           	11	- 적하목록 번호
MSN           	4	적하목록 마스터 일련번호
HSN           	4	적하목록 하우스 일련번호
PKG_GBN       	2	10:일반, 11:관리, 12:이사, 14:특송, 15:수출, K1016:면세, 17:조달
IN_DT         	14	- “YYYYMMDDHH24MISS”
CUS_CD1	        3	- 김포:041, 인천공항:040
OWNER_CD      	4	- ●화주코드테이블 참조(화주코드 없을 때 “9999”) (포워더 코드)
OWNER_COMP	    100	- 화주명 없을 때 “anonym”
ITEM_CD       	2	- ● 품목코드 테이블 참조
ITEM_NM1	    200	- 품명 없을 때 “기타”로 고정
MBL_NO        	20
HBL_NO        	20
SHIP_NO       	35	- 반입시점의 선, 기명
IN_GUBUN	    2	- ● 반입구분 코드 테이블 참조
BOND_FM       	8	- “YYYYMMDD”
BOND_TO       	8	- “YYYYMMDD”
SINGO_REQNO	    20
BOND_COMP_CD    20
PACK_KIND	    3	- ● 포장종류 코드 테이블 참조
IN_SAGO_CD      3	- ● 사고 코드 테이블 참조
PACK_CNT	    8	- 공백은 “0”으로 채움, 예) 00000015
OUT_QTY	        8	- 공백은 “0”으로 채움, 예) 00000015
JEGO_QTY	    8	- 공백은 “0”으로 채움, 예) 00000015
TOT_WGT	        10	- 6,3 예)15.4일때, 000015.400
OUT_WGT	        10	- 6,3 예)15.4일때, 000015.400
JEGO_WGT	    10	- 6,3 예)15.4일때, 000015.400
TOT_CBM	        6	- 공백은 “0”으로 채움, 예) 000015
OUT_CBM1	    6	- 공백은 “0”으로 채움, 예) 000015
OUT_CBM	        6	- 공백은 “0”으로 채움, 예) 000015
PLC_LOC       	10
PLC_CONF_DT   	8
PLC_CONF_ID   	10
OVER_YN       	1	- 1 : 체화, 0 : 정상 OVER_NO	10
OVER_DT       	8	- “YYYYMMDD”
DATA_CREAT_GBN	1	- 생성 : “I”, 정정 : “U”, 삭제 : “D”
                                                   USER_ID       	10	- 유저 아이디
USER_PW       	10	- 유저의 비밀번호
WORK_DT       	8	- Data 생성 또는 수정 된 날짜
TERM_ID       	5	- 스캐너 장비 번호
ELEC_YN       	1	- Default 0, 전자상거래 1
IN_TYPE	        2	"1:목록 2: 검사(목록) 3:간이 4:일반 5:검사(일반/간이) 6:검역(사전검역 요청건) 7:환적 8: 검역(동물검역) 9: 검역(식물검역) 10: 검역(식품검역) 11:검역(서류), 91.거대화물 13 : 개인통관부호 미제출"
SPC_PKG_GBN   	1	- 1:냉동,냉장,2:귀금속,3:무기,4:검역대상  JOUP_DT       	14	- “YYYYMMDDHHMISS”
IN_PORT_DT    	14	- “YYYYMMDDHHMISS”
VAL           	18	- 감정가격
DUTY          	18	- 해당 물품의 관세
UNLD_PORT_CD  	5	적재항 부호 기재 (UN/LOCODE)
SEND_NAT_CD   	2	발송국 국가부호 기재 (ISO 코드)
SEND_OFFICE	    100	상호 또는 성명
SEND_ADDR1    	50	주소1
SEND_ADDR2    	50	주소2
SEND_ADDR3    	50	주소3
RECV_TEL      	40	숫자형태로만 기재
RECV_NM       	100	상호 또는 성명
RECV_ADDR1    	50	주소1
RECV_ADDR2    	50	주소2
RECV_ADDR3    	50	주소3
RECV_POST_NO  	5	수하인 우편번호 기재(관세청요구사항)
ELEC_DOMAIN   	100	전자상거래 사이트주소를 기재함
INV_AMT       	18	통관대상 물품의 가격을 기재
INV_AMT_UNIT  	2	"항목 64(물품가격) 에 대한 단위미화:US,  한화:KR"
DIRECT_NO     	10	특별통관대상업체 지정번호
TRADE_TYPE    	1	"(A:개인직접수입형 전자상거래(배송/결재대행포함),B:수입대행형 전자상거래,  C:일반"
USE_GBN       	1	물품 용도구분 기재(1:개인, 2:회사)
CONTR_NO1     	11	컨테이너 번호를 기재
CONTR_NO2     	11
FWD_CODE	    4        환경설정(TB_SMS_ENT_BUS_OPTION[SOLUTION "BMS", OPTION_CODE "A3"]통합특송장FTP 포더부호
FWD_FIRM	    100    환경설정(TB_SMS_ENT_BUS_OPTION[SOLUTION "BMS", OPTION_CODE "A4"]통합특송장FTP 포더부호명
PASS_REASION	1	null:검사미지정 / 1:마약 / 2:총기 / 3:검역 / 4:식품 / 5:의약품 / 6:수입검사  TAKBAE_NO	10	EMS : 우체국, CJ : CJ대한통운(향후 추가)
TAKBAE_LOC	    10	택배사별 코드 참조 ( 향후배포)
UNI_MATT	    2	NULL: 특이사항없음 / 01:보류(세관), 51:DO확인, 52:LG확인
EOF	            1	  “*” 레코드의 끝을 표시