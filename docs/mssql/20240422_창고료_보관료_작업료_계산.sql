/* 0) 발행번호 요율구분선택시 발행번호는 매출일자의 요율구분에 MAX + 1 한 값 4자리 예) 0010. : 서버에서 생성> NEW는 무조건 조회, EDIT에서는 본인 정산번호 제외후 계산 
 *  파리미터: 요율구분, 매출일자, 매출번호 NEW 일때는 0
 * */
SELECT  FORMAT( ISNULL(MAX(APPLY_NO),0) +1,'0000')
FROM TB_BMS_ESTIMATE
WHERE APPLY_CLASS ='2' AND ESTIMATE_DATE ='2024-03-24' AND ESTIMATE_NO <> 0
;


/* 1) 보관일수 계산  : STORAGE_DAYS
 *
 * 1. 휴일 일
 * 1.1 반입일과 반출일 사이에 토요일, 일요일, 공휴일를 더한다.
   1.2 공휴일은 휴일 API (기상청) 을 통해 해당일이면 1일을 더한다.

   2. 보관일수 = 반출일자 - 반입일자
   ---- 클라이언트에서 계산 한후에 ------
   2.1 요율구분이 4. 수출일반인 경우 보관일수가 4보다 작으면 보관일수 0
   2.2 요율구분이 4. 수출일반 이면서 보관일수가 4 일 인 경우
        반출시간 < 반입시간 작으면 보관일수 0
   2.3 2.2 가 아니면서 보관일수가 1일 인 경우
        반출시간 < 반입시간 작으면 보관일수 0
   2.4 요율구분이 2 수입특송이면서 면제여부가 4일 면제 인 경우
        보관일수 = 보관일수 - 휴일일수체크 + 1 - 4  예) 반입일 2024-04-30 반출일 2024-05-07 인 경우 보관일수 1
   2.5 보관일수가 0보다 작으면 0을 넣는다.
   2.6  만으로 1일 미만일 경우만 시간체크해서 2.3적용하고, 2일 이상일 경우는 시간 체크 하지 않고 무조건 올림으로 처리
 * */

/* 반입일, 반출일 사이에 공일 있는지 확인 */
SELECT COUNT(*) AS G_CNT from TB_SMS_HOLIDAY
WHERE HOLIDAY_DATE BETWEEN '2024-02-21' AND '2024-05-15'
;
/* 반입일, 반출일 사이에 토,일 갯수 확인, 공유일 확인, 총일수 확인 */
/* ----------------------------------------------- */
DECLARE @StartDate DATE = '2024-06-01';
DECLARE @EndDate DATE = '2024-06-30';
/* 6월기준으로 토일 :10개, 휴일: 06-06일 30-11: 19개 */

--DECLARE @TotalDays INT = DATEDIFF(DAY, @StartDate, @EndDate) + 1;

DECLARE @v_HoliDay INT = 0; /* 휴일 계산 로직 */

DECLARE @v_workDay INT = 0; /* 최종 일한 일수: 최종일-토일갯수-휴일갯 */

/* 1) 휴일 제외 카운트 처리 */
SELECT  @v_HoliDay = COUNT(1)  from TB_SMS_HOLIDAY
WHERE HOLIDAY_DATE BETWEEN CONVERT (nvarchar(10),@StartDate,120) AND  CONVERT(nvarchar(10),@EndDate,120)
;

WITH DateRange AS (
    SELECT DATEADD(DAY, number, @StartDate) AS Date
    FROM master..spt_values
    WHERE type = 'P'
      AND DATEADD(DAY, number, @StartDate) <= @EndDate
)
SELECT
    @v_workDay = ( SUM(CASE
                           WHEN DATEPART(WEEKDAY, Date) NOT IN (1, 7) THEN 1  /* 일수에서 토,일이 아닌것 카운트 */
                           ELSE 0
        END)   - @v_HoliDay )
FROM DateRange;

SELECT @v_workDay AS WORK_DAYS

/**
 *  2) 토요일, 일요일 제외 카운
 */
/*
WITH DateRange AS (
    SELECT DATEADD(DAY, number, @StartDate) AS Date
    FROM master..spt_values
    WHERE type = 'P'
    AND DATEADD(DAY, number, @StartDate) <= @EndDate
)
SELECT
    SUM(CASE
            WHEN DATEPART(WEEKDAY, Date) NOT IN (1, 7) THEN 1
            ELSE 0
        END)  AS WORK_DAYS
        , @v_HoliDay HODI_DAYS
        , @TotalDays DIFF_DAYS
FROM DateRange;
 */
/* ----------------------------------------------- */


SELECT * from TB_SMS_CODE_DETAIL tscd
where GROUP_CODE ='WAREHOUSE_RATES';

SELECT * from TB_BMS_STORAGE_RATE;
/* STORAGE_WT 보관 중*/

/*  3) 보관료계산
 *   0.9 중량은 소숫점 1자리 반올림 처리
 *   1.0 보관일수가 999가 넘는 경우는 종료일 999 를 읽는다.
 * --- 클라이언트 javascript에서 처리 한다.
 *   1.1 종가기본(STORAGE_DAY_BASIC) = 보관료요율(TB_BMS_STORAGE_RATE) 종가기본료 (AMOUNT_BASIC)
  1.2 종가기본료(STORAGE_DAY_BASIC_AMOUNT) = (감정가(APPROVAL_APPRAISE_VALUE) + 관세(APPROVAL_DUTY)) / 1000 * 종가기본(STORAGE_DAY_BASIC)
  1.3 종가일일할증(STORAGE_DAY_OVER) = 보관료요율(TB_BMS_STORAGE_RATE) 종가일일할증 (AMOUNT_OVER) * 보관일수
  1.4 종가일일할증료(STORAGE_DAY_OVER_AMOUNT) = (감정가(APPROVAL_APPRAISE_VALUE) + 관세(APPROVAL_DUTY)) / 1000 * 종가일일할증(STORAGE_DAY_OVER)
  1.5 종가율(STORAGE_DAY_RATE)= 보관료요율(TB_BMS_STORAGE_RATE) 종가기본료 (AMOUNT_BASIC) + 보관료요율(TB_BMS_STORAGE_RATE) 종가일일할증 (AMOUNT_OVER) * 보관일수
  1.6 종가료(STORAGE_DAY_RATE_AMOUNT) =  (감정가(APPROVAL_APPRAISE_VALUE) + 관세(APPROVAL_DUTY)) / 1000 * 종가율(STORAGE_DAY_RATE)
  1.61 보관료계산중량(STORAGE_WT) 에 반출중량(OUT_WT) 을 넣는다.
  1.63 보관료계산중량(STORAGE_WT) < 보관료 요율 최저적용중량 (WT_MIN) 으면  최저적용중(OUT_WT)를 넣는다.
  1.7 종량기본(STORAGE_WEIGHT_BASIC) = 보관료요율(TB_BMS_STORAGE_RATE) 종량기본료 (WEIGHT_BASIC)
  1.71 보관료요율(TB_BMS_STORAGE_RATE) 최저적용중량(WT_MIN) 이 0보다 크고 계산중량 보다 큰 경우
        계산중량을 보관료요율(TB_BMS_STORAGE_RATE) 최저적용중량(WT_MIN) 으로 적용한다.
  1.8 종량기본료(STORAGE_WEIGHT_BASIC_AMOUNT) = 계산중량 * 종량기본(STORAGE_WEIGHT_BASIC)
  1.9 종량일일할증(STORAGE_WEIGHT_OVER) = 보관료요율(TB_BMS_STORAGE_RATE) 종량할증료 (WEIGHT_OVER) * 보관일수
  1.A 종량율(STORAGE_WEIGHT_RATE)= 보관료요율(TB_BMS_STORAGE_RATE) 종량기본료 (WEIGHT_BASIC + 보관료요율(TB_BMS_STORAGE_RATE) 종량일일할증 (WEIGHT_OVER) * 보관일수
  1.B 종량료(STORAGE_WEIGHT_RATE_AMOUNT)= 계산중량 * 종량율(STORAGE_WEIGHT_RATE)
  1.C 보관료(STORAGE_AMOUNT) = 종가료(STORAGE_DAY_RATE_AMOUNT) + 종량료(STORAGE_WEIGHT_RATE_AMOUNT)
  1.D 보관료(STORAGE_AMOUNT)가 보관료요율(TB_BMS_STORAGE_RATE) 최소금액(PAY_MIN) 보다 작은 경우
        보관료(STORAGE_AMOUNT)는 최소금액(PAY_MIN) 이 되고 종가료(STORAGE_DAY_RATE_AMOUNT) 에 최소금액
        종가료(STORAGE_DAY_RATE_AMOUNT) = 0
*/
/* 3) 보관료 파라미터 : 반입일자, 보관일수, 요율구분 */
SELECT A.* FROM  TB_BMS_STORAGE_RATE A
WHERE A.APPLY_DATE = ( SELECT MAX(APPLY_DATE) FROM TB_BMS_STORAGE_RATE
                       WHERE APPLY_DATE <= CONVERT(varchar(10),GETDATE() ,120) )  /* 반입일자 */
  AND (CASE WHEN ( 1000 > 999) THEN 999 ELSE 3 END)  BETWEEN A.STORAGE_START_DAYS AND A.STORAGE_END_DAYS /* 보관일수 */
  AND APPLY_CLASS = '2' /* 요율구분  */
;

/*  4) 작업료계산
 * 1. 작업료요율(TB_BMS_WORK_RATE) 적용일자 <= 반입일자 가까운 적용일자로 적용한다.
  1.1 요율구분(APPLY_CLASS) 은  매출(TB_BMS_ESTIMATE) 요율구분 (APPLY_CLASS) 으로 적용한다.
  1.2 반출중량(OUT_WT) >=  적용기준시작값(RATE_START_VALUE) AND 반출중량(OUT_WT) <=  적용기준종료값(RATE_END_VALUE) 인 건에 해당하는 작업료요율을 찾느다.
  1.3 작업료계산중량(WORK_WT) 에 반출중량(OUT_WT) 를 적용한다.
  1.4 작업료계산중량(WORK_WT)  < 최저적용중량(WT_MIN) 인 경우 작업료계산중량(WORK_WT)  에 최저적용중량(WT_MIN) 를 적용한다.
  1.5 초과중량(OVER_WEIGHT) < 작업료계산중량(WORK_WT)  인 경우
       작업기본(WORK_PAY_BASIC) = 기본금액(PAY_BASIC)
       작업료(WORK_AMOUNT) = 기본금액(PAY_BASIC)
       초과중량(OVER_WEIGHT) > 작업료계산중량(WORK_WT)  인 경우
       작업기본(WORK_PAY_BASIC) = 기본금액(PAY_BASIC)
       2024-04-22 현도여부체크
       1.5.1 현도여부(ASSIGN_YN) 가 "N" 인 경우
             작업할증단가(WORK_PAY_RATE) = 초과단가(OVER_PRICE)
             작업료(WORK_AMOUNT) = 기본금액(PAY_BASIC) + (작업료계산중량(WORK_WT) - 초과중량(OVER_WEIGHT) ) * 초과단가(OVER_PRICE)
       1.5.2 현도여부(ASSIGN_YN) 가 "Y" 인 경우
             작업할증단가(WORK_PAY_RATE) = 현도초과단가(ASSIGN_OVER_PRICE)
             작업료(WORK_AMOUNT) = 기본금액(PAY_BASIC) + (작업료계산중량(WORK_WT) - 초과중량(OVER_WEIGHT) ) * 현도초과단가(ASSIGN_OVER_PRICE)
 * */

/* 4) 작업료율 기초 데이터 ,  파라미터: 반입일자, 요율구분  */
SELECT A.* FROM  TB_BMS_WORK_RATE  A
WHERE  A.APPLY_DATE = ( SELECT MAX(APPLY_DATE) FROM TB_BMS_WORK_RATE
                        WHERE APPLY_DATE <= CONVERT(varchar(10),GETDATE() ,120) )  /* 반입일자 */
  AND APPLY_CLASS = '2' /* 요율구분  */
 