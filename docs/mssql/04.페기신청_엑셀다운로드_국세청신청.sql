/*#해상운송을 위한 엑셀자료 표시 */
// cfn_excelDownload_cargoManifest(params, "Cargo Manifest", "/ecm/eCommerce/consol/houseOrder/search/list/cargoManifest","ECM");

/* 해상 운송 쿼리 */
SELECT
    A.DISPOSAL_NO
     ,  A.IN_NO
     ,  A.CARGO_REFERENCE_NO
     ,  STUFF(STUFF(A.CARGO_REFERENCE_NO, 16, 0, '-'), 12, 0, '-' ) AS CARGO_REFERENCE_NO
     ,  A.BL_NO
     -- 화주 정보
     ,  A.CUSTOMER_NAME
     -- 신청인 정보 ( 담당자 ( 대표자 ) )  테이블 위치 [신청인,신청업체명, 신청인연락처, 신청인 주소, 신청일자]
     , usr.BUSINESS_PRESIDENT
     , usr.FROM_BUSINESS_NAME
     , usr.TEL
     , CONCAT (usr.ADDRESS  , usr.ADDRESS_A ) FROM_ADDRESS
     ,  A.DISPOSAL_ISSUE_DATE
     -- 폐기 업체 정보 ( 폐기 업체명, 사업자 번호, 폐기 업체 주)
     ,  A.DISPOSAL_NAME
     ,  tsc.BUSINESS_SUB_REG_NO
     , CONCAT (tsc.ADDRESS  , tsc.ADDRESS_A, tsc.ADDRESS_B ) AS DISPOSAL_ADDR
     -- 폐기 정보 (보세구역	폐기장소	폐기예정일자	폐기방법	폐기사유	폐기신청수량	폐기신청중량	폐기신청가격(CIF)	반출일자	폐기완료일자)
     ,  A.DISPOSAL_LOC
     ,  A.DISPOSAL_EXP_DATE  -- 폐기예정일자, 반출일자 동일
     ,  A.DISPOSAL_METHOD
     ,  A.DISPOSAL_REASON
     ,  A.DISPOSAL_CT
     ,  A.DISPOSAL_WT
     ,  'uni-pass 등록 승인' AS DISPOSAL_AMOUNT
     ,  A.DISPOSAL_EXP_DATE DISP_OUT_DATE --  반출일자 ( 폐기에서 반출 넘기는 날자?)
     ,  A.DISPOSAL_FINISH_DATE
     -- 폐기 물품 내역  ( 품명	규격	수량	중량	가격(USD))
     , B.COMMODITY_ITEM_LOC  -- 품명, 규격 동
     , B.COMMODITY_ITEM_LOC ITEM_SPEC
     , B.STOCK_CT
     , B.STOCK_WT
     , 'uni-pass 등록 승인' AMOUNT_USD
FROM
    TB_BMS_DISPOSAL A
        LEFT JOIN TB_BMS_IN B
                  ON A.IN_NO = B.IN_NO
        LEFT JOIN  TB_SMS_CUSTOMER tsc
                   ON A.DISPOSAL_CODE = tsc.CUSTOMER_CODE
        LEFT JOIN (
        SELECT su.USER_ID /* 사용자 아이디 */
             , su.USER_NAME /* 사용자명 */
             , su.ENT_CODE AS FROM_ENT_CODE /* 도입사 코드 */
             , su.USER_TEL AS FROM_USER_TEL /* 사용자 전화번호 */
             , su.USER_TEL_EXTENSION AS FROM_USER_TEL_EX /* 사용자 내선번호 */
             , su.USER_FAX AS FROM_USER_FAX /* 사용자 팩스번호 */
             , su.USER_EMAIL AS FROM_USER_EMAIL /* 사용자 이메일 */
             , busi.ENT_BUSINESS_CODE AS FROM_BUSINESS_CODE /* 사업장 코드 (로그인 오피스의 거래처 코드) */
             , busi.ENT_BUSINESS_NAME AS FROM_BUSINESS_NAME /* 사업장명 (로그인 오피스의 거래처명) */
             , busi.ENT_BUSINESS_NAME_ENG AS OFFICE_NAME_ENG /* 사업장명 영문 (로그인 오피스의 거래처명 영문) */
             , busi.BMS_WH_CODE /* 사업장 보세구역번호*/
             , busi.TEL /* 사업장 전화번호 */
             , busi.FAX /* 사업장 FAX번호  */
             , busi.EMAIL /* 사업장 이메일 */
             , busi.ZIPCODE /* 우편번호 */
             , busi.ADDRESS /* 주소 */
             , busi.ADDRESS_A /* 사업장주소A */
             , busi.ADDRESS_ENG /* 사업장주소 영문 */
             , busi.ADDRESS_A_ENG /* 사업장주소A 영문 */
             , busi.ADDRESS_B_ENG /* 사업장주소B 영문 */
             , busi.HOME_PAGE_URL AS FROM_BUSINESS_HOMPAGE_URL /* 사업장 홈페이지 */
             , ( SELECT BUSINESS_PRESIDENT
                 FROM VTB_SMS_BUSINESS_REG_INFO reg
                 WHERE busi.REGISTRATION_CODE = reg.REGISTRATION_CODE
        ) AS BUSINESS_PRESIDENT /* 사업장 대표자성명 */
        FROM TB_SMS_USER su
                 LEFT JOIN TB_SMS_ENT_BUSINESS busi ON su.ENT_CODE = busi.ENT_CODE AND su.ENT_BUSINESS_CODE = busi.ENT_BUSINESS_CODE
        WHERE su.USER_ID = '201808-001'
          AND su.ENT_CODE = 'YHTXX'
          AND su.ENT_BUSINESS_CODE = 'YHTXX-KR'
    ) usr ON (1=1)
WHERE 1 = 1
  AND B.IMPORT_EXPORT_CLASS  ='1'
;

/*** 항공 운송 **/
SELECT
    STUFF(STUFF(A.CARGO_REFERENCE_NO, 16, 0, '-'), 12, 0, '-' ) AS CARGO_REFERENCE_NO
     , '001' LAN_NO
     ,  A.BL_NO
     , '1' TOT_LAN
     , B.COMMODITY_ITEM_LOC  -- 품명, 규격 동
     , '' ITEM_SPEC
     , '' DISP_YY
     , '' DISP_CHA
     , '' DISP_HS
     , '' DISP_REASON_CD
     ,  A.DISPOSAL_REASON
     ,  A.DISPOSAL_CT
     ,  A.PKG_TYPE
     ,  A.DISPOSAL_WT
     ,  'KG' KG
     , usr.BMS_WH_CODE
     , usr.FROM_BUSINESS_NAME
     , B.CARRYOUT_DATE
     , A.CUSTOMER_NAME
     , '폐기예정 (NCV포함)/유찰(국고귀속/폐기예정)' STATUS
     , usr.FROM_BUSINESS_NAME IN_BUSINESS_NAME
FROM
    TB_BMS_DISPOSAL A
        LEFT JOIN TB_BMS_IN B
                  ON A.IN_NO = B.IN_NO
        LEFT JOIN  TB_SMS_CUSTOMER tsc
                   ON A.DISPOSAL_CODE = tsc.CUSTOMER_CODE
        LEFT JOIN (
        SELECT su.USER_ID /* 사용자 아이디 */
             , su.USER_NAME /* 사용자명 */
             , su.ENT_CODE AS FROM_ENT_CODE /* 도입사 코드 */
             , su.USER_TEL AS FROM_USER_TEL /* 사용자 전화번호 */
             , su.USER_TEL_EXTENSION AS FROM_USER_TEL_EX /* 사용자 내선번호 */
             , su.USER_FAX AS FROM_USER_FAX /* 사용자 팩스번호 */
             , su.USER_EMAIL AS FROM_USER_EMAIL /* 사용자 이메일 */
             , busi.ENT_BUSINESS_CODE AS FROM_BUSINESS_CODE /* 사업장 코드 (로그인 오피스의 거래처 코드) */
             , busi.ENT_BUSINESS_NAME AS FROM_BUSINESS_NAME /* 사업장명 (로그인 오피스의 거래처명) */
             , busi.ENT_BUSINESS_NAME_ENG AS OFFICE_NAME_ENG /* 사업장명 영문 (로그인 오피스의 거래처명 영문) */
             , busi.BMS_WH_CODE /* 사업장 보세구역번호*/
             , busi.TEL /* 사업장 전화번호 */
             , busi.FAX /* 사업장 FAX번호  */
             , busi.EMAIL /* 사업장 이메일 */
             , busi.ZIPCODE /* 우편번호 */
             , busi.ADDRESS /* 주소 */
             , busi.ADDRESS_A /* 사업장주소A */
             , busi.ADDRESS_ENG /* 사업장주소 영문 */
             , busi.ADDRESS_A_ENG /* 사업장주소A 영문 */
             , busi.ADDRESS_B_ENG /* 사업장주소B 영문 */
             , busi.HOME_PAGE_URL AS FROM_BUSINESS_HOMPAGE_URL /* 사업장 홈페이지 */
             , ( SELECT BUSINESS_PRESIDENT
                 FROM VTB_SMS_BUSINESS_REG_INFO reg
                 WHERE busi.REGISTRATION_CODE = reg.REGISTRATION_CODE
        ) AS BUSINESS_PRESIDENT /* 사업장 대표자성명 */
        FROM TB_SMS_USER su
                 LEFT JOIN TB_SMS_ENT_BUSINESS busi ON su.ENT_CODE = busi.ENT_CODE AND su.ENT_BUSINESS_CODE = busi.ENT_BUSINESS_CODE
        WHERE su.USER_ID = '201808-001'
          AND su.ENT_CODE = 'YHTXX'
          AND su.ENT_BUSINESS_CODE = 'YHTXX-KR'
    ) usr ON (1=1)
WHERE 1 = 1
  AND B.IMPORT_EXPORT_CLASS  ='3'





SELECT su.USER_ID /* 사용자 아이디 */
     , su.USER_NAME /* 사용자명 */
     , su.ENT_CODE AS FROM_ENT_CODE /* 도입사 코드 */
     , su.USER_TEL AS FROM_USER_TEL /* 사용자 전화번호 */
     , su.USER_TEL_EXTENSION AS FROM_USER_TEL_EX /* 사용자 내선번호 */
     , su.USER_FAX AS FROM_USER_FAX /* 사용자 팩스번호 */
     , su.USER_EMAIL AS FROM_USER_EMAIL /* 사용자 이메일 */
     , busi.ENT_BUSINESS_CODE AS FROM_BUSINESS_CODE /* 사업장 코드 (로그인 오피스의 거래처 코드) */
     , busi.ENT_BUSINESS_NAME AS FROM_BUSINESS_NAME /* 사업장명 (로그인 오피스의 거래처명) */
     , busi.ENT_BUSINESS_NAME_ENG AS OFFICE_NAME_ENG /* 사업장명 영문 (로그인 오피스의 거래처명 영문) */
     , busi.BMS_WH_CODE /* 사업장 보세구역번호*/
     , busi.TEL /* 사업장 전화번호 */
     , busi.FAX /* 사업장 FAX번호  */
     , busi.EMAIL /* 사업장 이메일 */
     , busi.ZIPCODE /* 우편번호 */
     , busi.ADDRESS /* 주소 */
     , busi.ADDRESS_A /* 사업장주소A */
     , busi.ADDRESS_ENG /* 사업장주소 영문 */
     , busi.ADDRESS_A_ENG /* 사업장주소A 영문 */
     , busi.ADDRESS_B_ENG /* 사업장주소B 영문 */
     , busi.HOME_PAGE_URL AS FROM_BUSINESS_HOMPAGE_URL /* 사업장 홈페이지 */
     , ( SELECT BUSINESS_PRESIDENT
         FROM VTB_SMS_BUSINESS_REG_INFO reg
         WHERE busi.REGISTRATION_CODE = reg.REGISTRATION_CODE
) AS BUSINESS_PRESIDENT /* 사업장 대표자성명 */
     , ( SELECT BUSINESS_REG_NO
         FROM VTB_SMS_BUSINESS_REG_INFO reg
         WHERE busi.REGISTRATION_CODE = reg.REGISTRATION_CODE
) AS BUSINESS_REG_NO /* 사업자등록번호 */
     , ( SELECT ENT_BUSINESS_CODE
         FROM TB_SMS_ENT_BUSINESS
         WHERE su.ENT_CODE = ENT_CODE
           AND su.ENT_BUSINESS_CODE = ENT_BUSINESS_CODE
           AND BMS_WH_CODE = '04002001'
) AS TO_BUSINESS_CODE /* 거래처정보 창고 04002001의 거래처코드 */
     , ( SELECT ENT_BUSINESS_NAME
         FROM TB_SMS_ENT_BUSINESS
         WHERE su.ENT_CODE = ENT_CODE
           AND su.ENT_BUSINESS_CODE = ENT_BUSINESS_CODE
           AND BMS_WH_CODE = '04002001'
) AS TO_BUSINESS_NAME /* 거래처정보 창고 04002001의 거래처명 */
FROM TB_SMS_USER su
         LEFT JOIN TB_SMS_ENT_BUSINESS busi ON su.ENT_CODE = busi.ENT_CODE AND su.ENT_BUSINESS_CODE = busi.ENT_BUSINESS_CODE
WHERE su.USER_ID = '201808-001'
  AND su.ENT_CODE = 'YHTXX'
  AND su.ENT_BUSINESS_CODE = 'YHTXX-KR'
/* */

SELECT su.USER_ID /* 사용자 아이디 */
     , su.USER_NAME /* 사용자명 */
     , su.ENT_CODE AS FROM_ENT_CODE /* 도입사 코드 */
     , su.USER_TEL AS FROM_USER_TEL /* 사용자 전화번호 */
     , su.USER_TEL_EXTENSION AS FROM_USER_TEL_EX /* 사용자 내선번호 */
     , su.USER_FAX AS FROM_USER_FAX /* 사용자 팩스번호 */
     , su.USER_EMAIL AS FROM_USER_EMAIL /* 사용자 이메일 */
     , busi.ENT_BUSINESS_CODE AS FROM_BUSINESS_CODE /* 사업장 코드 (로그인 오피스의 거래처 코드) */
     , busi.ENT_BUSINESS_NAME AS FROM_BUSINESS_NAME /* 사업장명 (로그인 오피스의 거래처명) */
     , busi.ENT_BUSINESS_NAME_ENG AS OFFICE_NAME_ENG /* 사업장명 영문 (로그인 오피스의 거래처명 영문) */
     , busi.BMS_WH_CODE /* 사업장 보세구역번호*/
     , busi.TEL /* 사업장 전화번호 */
     , busi.FAX /* 사업장 FAX번호  */
     , busi.EMAIL /* 사업장 이메일 */
     , busi.ZIPCODE /* 우편번호 */
     , busi.ADDRESS /* 주소 */
     , busi.ADDRESS_A /* 사업장주소A */
     , busi.ADDRESS_ENG /* 사업장주소 영문 */
     , busi.ADDRESS_A_ENG /* 사업장주소A 영문 */
     , busi.ADDRESS_B_ENG /* 사업장주소B 영문 */
     , busi.HOME_PAGE_URL AS FROM_BUSINESS_HOMPAGE_URL /* 사업장 홈페이지 */
     , ( SELECT BUSINESS_PRESIDENT
         FROM VTB_SMS_BUSINESS_REG_INFO reg
         WHERE busi.REGISTRATION_CODE = reg.REGISTRATION_CODE
) AS BUSINESS_PRESIDENT /* 사업장 대표자성명 */
FROM TB_SMS_USER su
         LEFT JOIN TB_SMS_ENT_BUSINESS busi ON su.ENT_CODE = busi.ENT_CODE AND su.ENT_BUSINESS_CODE = busi.ENT_BUSINESS_CODE
WHERE su.USER_ID = :userId
  AND su.ENT_CODE = :userEntCode
  AND su.ENT_BUSINESS_CODE = :userEntBusinessCode

/* 거래처 정보 조회 쿼리 */

SELECT
    bus.ENT_BUSINESS_NAME_ENG   -- Company Name (사업장 명칭)
     ,bus.TEL         -- 전화번호
     ,bus.FAX         -- FAX번호
     ,bus.EMAIL         -- 이메일
     ,bus.ADDRESS   -- Company Address (사업장 주소)
     ,bus.ADDRESS_A   -- Company Address (사업장 주소)
     ,(SELECT USER_NAME FROM TB_SMS_USER WHERE ENT_CODE = bus.ENT_CODE AND USER_ID = :userId) AS USER_NAME   -- 출력자명
     ,(SELECT BUSINESS_PRESIDENT FROM VTB_SMS_BUSINESS_REG_INFO req WHERE req.REGISTRATION_CODE = bus.REGISTRATION_CODE) AS BUSINESS_PRESIDENT   -- 사업장 대표자성명
FROM TB_SMS_ENT_BUSINESS bus
WHERE bus.ENT_CODE = :srchEntCode
  AND bus.ENT_BUSINESS_CODE = :srchEntBusinessCode