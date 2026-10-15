
srchUserLanguage
srchEntCode
fmtTime
fmtDate


/* 사용자 이름 가져 오기 작업 */
,(SELECT CUSTOMER_NAME_${srchUserLanguage} FROM TB_SMS_CUSTOMER WHERE ENT_CODE = business.ENT_CODE AND CUSTOMER_CODE = acc.ACCOUNT_CODE) AS CUSTOMER_ACCOUNT_NAME


/* 사용자 이름 가져 오기 작업 */

            ( SELECT NAME_${srchUserLanguage} FROM TB_SMS_CODE_DETAIL
            WHERE     (1=1)
            AND CODE = log.BOUND
            AND GROUP_CODE = 'BOUND'
            AND (ENT_BUSINESS_CODE = #{srchEntCode} OR ENT_BUSINESS_CODE IS NULL))
/** srchEntCode를 가지고 작업 */
            (SELECT CUSTOMER_NAME FROM TB_SMS_CUSTOMER WHERE ENT_CODE = #{srchEntCode} AND CUSTOMER_CODE = #{cbCode} AND CUSTOMER_TYPE ='BR')



/***
  세관코드, 세관과 코드 조회 하는 쿼리
 */
select bs_wh_code as 보세창고부호, bms_customs_code as 세관코드, bms_customs_dpt_code as 세관과 from [TB_SMS_ENT_BUSINESS]