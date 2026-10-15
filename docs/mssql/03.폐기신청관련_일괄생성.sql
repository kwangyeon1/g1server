/*
폐기화물 일괄 생성 :  insertBatchDisposalFromIn
1. 기존에 자료가 있는지 검색 후 자료가 없는 경우에 생성
  폐기 (TB_BMS_DISPOSAL) 검색시 화물관리번호 (CARGO_REFERENCE_NO)k
  로 조회시 없는 경우만 진행
*/
-- 0 건일 경우에만 INSERT 하고 1건 이상인 경우에는 Pass
select count(*) as cnt from TB_BMS_DISPOSAL tbd where CARGO_REFERENCE_NO = '2021-01-01';

INSERT INTO
    TB_BMS_DISPOSAL
    (
      DISPOSAL_ISSUE_DATE
    , IN_NO
    , CARGO_REFERENCE_NO
    , BL_NO
    , MBL_NO
    , DISPOSAL_REASON
    , DISPOSAL_CODE
    , DISPOSAL_NAME
    , CUSTOMER_CODE
    , CUSTOMER_NAME
    , CUSOMTER_TEL
    , CUSTOMER_ADDRESS_1
    , CUSTOMER_ADDRESS_2
    , AMOUNT_USD
    , DISPOSAL_LOC
    , DISPOSAL_EXP_DATE
    , DISPOSAL_METHOD
    , DISPOSAL_FINISH_DATE
    , DISPOSAL_APPROVAL_NO
    , PKG_TYPE
    , DISPOSAL_CT
    , DISPOSAL_WT
    , OUT_YN
    , REGISTER_USER_ID
    , REGISTER_DATE_TIME
    , UPDATE_USER_ID
    , UPDATE_DATE_TIME
    )
SELECT
       CONVERT(VARCHAR(10),GETDATE(),${dateFormat})  AS DISPOSAL_ISSUE_DATE
     , IN_NO
     , CARGO_REFERENCE_NO
     , BL_NO
     , MBL_NO
     , 'CUR' AS DISPOSAL_REASON
     , 'CUR' AS DISPOSAL_CODE
     , 'CUR' AS DISPOSAL_NAME
     , tbi.CUSTOMER_CODE
     , tbi.CUSTOMER_NAME
     , tsc.TEL AS CUSOMTER_TEL
     , tsc.ADDRESS  AS CUSTOMER_ADDRESS_1
     , tsc.ADDRESS_A AS CUSTOMER_ADDRESS_2
     , AMOUNT_USD
     , 'CUR' AS DISPOSAL_LOC
     , 'CUR' AS DISPOSAL_EXP_DATE
     , 'CUR' AS DISPOSAL_METHOD
     , ''    AS DISPOSAL_FINISH_DATE
     , ''    AS DISPOSAL_APPROVAL_NO
     , PKG_TYPE
     , CT AS DISPOSAL_CT
     , WT AS DISPOSAL_WT
     , 'N' OUT_YN
     ,  #{registerUserId} REGISTER_USER_ID
     , DATEADD(HOUR, ${gmtTime}, GETDATE()) REGISTER_DATE_TIME
     , #{updateUserId} UPDATE_USER_ID
     , DATEADD(HOUR, ${gmtTime}, GETDATE()) UPDATE_DATE_TIME
FROM TB_BMS_IN tbi
         LEFT JOIN TB_SMS_CUSTOMER tsc
                   ON tbi.CUSTOMS_CODE = tsc.CUSTOMER_CODE
where tbi.IN_NO = 326432
  AND tbi.CARGO_REFERENCE_NO = '1234567890'
;
, #{registerUserId}
,  DATEADD(HOUR, ${gmtTime}, GETDATE())
, #{updateUserId}
, DATEADD(HOUR, ${gmtTime}, GETDATE())
/**
  가장 최근의 폐기 사유, 폐기업체코드, 폐기 업체명, 폐기 장소, 폐기 방법등이 입력된 값중 가장 최근값 가져 오기
   selectRecentDisposalData
 */
SELECT ISNULL( (
                   SELECT TOP 1    DISPOSAL_REASON
                   FROM TB_BMS_DISPOSAL tbd
                   WHERE DISPOSAL_REASON <> ''
                   ORDER BY  REGISTER_DATE_TIME DESC
               ),'')  AS DISPOSAL_REASON
        ,
       ISNULL((
                  SELECT TOP 1    DISPOSAL_CODE
                  FROM TB_BMS_DISPOSAL tbd
                  WHERE DISPOSAL_CODE <> ''
                  ORDER BY  REGISTER_DATE_TIME DESC
              ),'')  AS DISPOSAL_CODE
        ,
       ISNULL((
                  SELECT TOP 1    DISPOSAL_NAME
                  FROM TB_BMS_DISPOSAL tbd
                  WHERE DISPOSAL_NAME <> ''
                  ORDER BY  REGISTER_DATE_TIME DESC
              ),'')  AS DISPOSAL_NAME
        ,
       ISNULL((
                  SELECT top 1   DISPOSAL_LOC
                  FROM TB_BMS_DISPOSAL
                  WHERE DISPOSAL_LOC <> ''
                  ORDER BY  REGISTER_DATE_TIME DESC
              ),'')  AS DISPOSAL_LOC
        ,
       ISNULL((
                  SELECT top 1   DISPOSAL_METHOD
                  FROM TB_BMS_DISPOSAL
                  WHERE DISPOSAL_METHOD <> ''
                  ORDER BY  REGISTER_DATE_TIME DESC
              ),'')  AS DISPOSAL_METHOD



INSERT INTO
    TB_BMS_OUT
(
  CUSTOMS_REPORT_NO
, LAST_POD_DATE
, LAST_POD_STATUS
, LAST_POD_RAQ_DATE
, APPROVAL_DATE
, OUT_CLASS
, OUT_APPROVAL_NO
, OD_JOB_NO
, IN_NO
, OUT_PLAN_NO
, CARGO_REFERENCE_NO
, BL_NO
, MBL_NO
, DO_NO
, DO_DATE
, CUSTOMS_CODE
, CUSTOMS_DPT_CODE
, CUSTOMER_CODE
, CUSTOMER_NAME
, BROKER_CODE
, BROKER_NAME
, PKG_TYPE
, APPROVAL_CT
, APPROVAL_WT
, APPROVAL_CBM
, OUT_CT
, OUT_WT
, OUT_CBM
, CARRYOUT_DATE
, CARRYOUT_TIME
, APPROVAL_DUTY
, APPROVAL_APPRAISE_VALUE
, INS_PAYMENT_TYPE
, CURRENCY_CODE
, INS_PAYMENT_YN
, INSPECT_YN
, HS_CODE
, CRITERIA_CODE
, INOUT_DATE_DIFF
, REMARKS_1
, REMARKS_2
, REMARKS_3
, REFERENCE_NO
, REGISTER_USER_ID
, REGISTER_DATE_TIME
, UPDATE_USER_ID
, UPDATE_DATE_TIME
)
SELECT
    '' AS CUSTOMS_REPORT_NO
     , '' LAST_POD_DATE
     , 'BWH01' LAST_POD_STATUS
     , '' LAST_POD_RAQ_DATE
     , GETDATE() APPROVAL_DATE
     , '83' OUT_CLASS
     , '04002001' OUT_APPROVAL_NO
     , tbi.OD_JOB_NO
     , tbi.IN_NO
     , '' OUT_PLAN_NO
     , tbi.CARGO_REFERENCE_NO
     , tbi.BL_NO
     , tbi.MBL_NO
     , '' DO_NO
     , '' DO_DATE
     , tbi.CUSTOMS_CODE
     , tbi.CUSTOMS_DPT_CODE
     , tbi.CUSTOMER_CODE
     , tbi.CUSTOMER_NAME
     , '' BROKER_CODE
     , '' BROKER_NAME
     , tbi.PKG_TYPE
     , tbd.DISPOSAL_CT  APPROVAL_CT
     , tbd.DISPOSAL_WT  APPROVAL_WT
     , (CASE WHEN tbi.CBM=0 THEN 0
             WHEN tbi.WT=tbd.DISPOSAL_WT THEN tbi.CBM
             WHEN tbi.WT =0 THEN 0
             ELSE ROUND(tbi.CBM * (tbd.DISPOSAL_WT/tbi.WT),3)
    END) APPROVAL_CBM
     , tbd.DISPOSAL_CT OUT_CT
     , tbd.DISPOSAL_WT OUT_WT
     , (CASE WHEN tbi.CBM=0 THEN 0
             WHEN tbi.WT=tbd.DISPOSAL_WT THEN tbi.CBM
             WHEN tbi.WT =0 THEN 0
             ELSE ROUND(tbi.CBM * (tbd.DISPOSAL_WT/tbi.WT),3)
    END) OUT_CBM
     , CONVERT(VARCHAR(10),GETDATE(),120) CARRYOUT_DATE  /* 현재 날자 10자리 */
     , CONVERT(CHAR(8), GETDATE(), 108) CARRYOUT_TIME  /*현재 시간 8자리*/
     , 0 APPROVAL_DUTY
     , 0 APPROVAL_APPRAISE_VALUE
     , '' INS_PAYMENT_TYPE
     , '' CURRENCY_CODE
     , 'N' INS_PAYMENT_YN
     , 'N' INSPECT_YN
     , '' HS_CODE
     , '' CRITERIA_CODE
     , dbo.GET_DATE_DIFF_KOR_STR(concat(tbi.CARRYIN_DATE,' ',tbi.CARRYIN_TIME )) INOUT_DATE_DIFF
     , '' REMARKS_1
     , '' REMARKS_2
     , '' REMARKS_3
     , tbi.REFERENCE_NO
     , '' REGISTER_USER_ID
     , GETDATE()
     , '' UPDATE_USER_ID
     , GETDATE()
FROM TB_BMS_DISPOSAL tbd
         LEFT JOIN TB_BMS_IN tbi
                   ON tbd.IN_NO  = tbi.IN_NO
         LEFT JOIN TB_SMS_CUSTOMER tsc
                   ON tbd.CUSTOMER_CODE = tsc.CUSTOMER_CODE
where tbd.DISPOSAL_NO  = 3
;
