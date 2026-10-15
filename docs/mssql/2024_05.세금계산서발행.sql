/* INSERT 세금 계산서 */

         commonMapper.selectNextStockMaxNo(invoiceStockMap)
              --  년도(2)+특송업체부호(4)+일련번호(8)

        EXEC GET_NEXT_STOCK_MAX
             #{entCode}
			,#{entBusinessCode}
			,#{stockType}
			,@STOCK_SIGN
			,#{stockSeqLength}
			,@DATE1
			<choose>
				<when test='solution != null and solution != ""'>
				,#{solution}
				</when>
				<otherwise>
				,'ECM'
				</otherwise>
			</choose>
			,@DATE2
			,#{resultCode}
			,#{updateUserId}
			,#{stockRemark}
			<choose>
				<when test="subSeq != null and subSeq != ''">
				,#{subSeq}
				</when>
				<otherwise>
				,0
				</otherwise>
			</choose>
			,@RES_STOCK_NO OUTPUT;
        -------
        <include refid="org.yht.exps.express.mapper.CommonMapper.selectNextStockMaxNo"/>
        SELECT @RES_STOCK_NO;

,DATEADD(HOUR, ${gmtTime}, GETDATE())
, userId
        INSERT INTO TB_SAS_INVOICE ( INVOICE_JOB_NO
                                   , ENT_CODE
                                   , ENT_BUSINESS_CODE
                                   , INVOICE_NO
                                   , INVOICE_NO_CUSTOMER
                                   , INVOICE_NO_GROUP
                                   , PRINT_GROUP_NO
                                   , SOLUTION
                                   , BOUND
                                   , BOUND_TYPE
                                   , LOCAL_OVERSEA
                                   , AR_AP
                                   , DOC_ISSUE_TYPE
                                   , HBL_INV_YN
                                   , PREPAID_YN
                                   , OD_JOB_NO
                                   , BL_NO
                                   , INVOICE_DATE
                                   , ACCOUNT_CODE
                                   , ACCOUNT_NAME
                                   , ACCOUNT_ATTN_ID
                                   , ACCOUNT_ATTN
                                   , ACCOUNT_TEL
                                   , ACCOUNT_FAX
                                   , PAYMENT_ACCOUNT_CODE
                                   , CURRENCY_CODE_CUR
                                   , EXRATE_TYPE
                                   , EXRATE_DATE
                                   , EXCHANGE_RATE
                                   , AMOUNT_CUR
                                   , AMOUNT_VAT_CUR
                                   , AMOUNT_TOTAL_CUR
                                   , DR_AMOUNT_TOTAL_CUR
                                   , CR_AMOUNT_TOTAL_CUR
                                   , CURRENCY_CODE_LOC
                                   , AMOUNT_LOC
                                   , AMOUNT_VAT_LOC
                                   , AMOUNT_TOTAL_LOC
                                   , DR_AMOUNT_TOTAL_LOC
                                   , CR_AMOUNT_TOTAL_LOC
                                   , CURRENCY_TOTAL
                                   , EXRATE_USD
                                   , AMOUNT_TOTAL_USD
                                   , REGISTER_USER_ID
                                   , REGISTER_DATE_TIME
                                   , UPDATE_USER_ID
                                   , UPDATE_DATE_TIME)
        SELECT #{invoiceJobNo} INVOICE_JOB_NO
                       ,  #{entCode} ENT_CODE
                       ,  #{entBusinessCode} ENT_BUSINESS_CODE
                       ,  #{invoiceNo} INVOICE_NO
                       ,  '' INVOICE_NO_CUSTOMER
                       ,  '' INVOICE_NO_GROUP
                       ,  '' PRINT_GROUP_NO
                       ,  'BMS' SOLUTION
                       ,  'AIR' BOUND
                       ,  'IMP' BOUND_TYPE
                       ,  'LOCAL' LOCAL_OVERSEA
                       ,  'AR' AR_AP
                       ,  'OP-GENERAL' DOC_ISSUE_TYPE
                       ,  'Y' HBL_INV_YN
                       ,  'N' PREPAID_YN
                       ,  ESTIMATE_NO AS OD_JOB_NO
                       ,  BL_NO
                       ,  ESTIMATE_DATE AS INVOICE_DATE
                       ,  CUSTOMER_CODE AS ACCOUNT_CODE
                       ,  CUSTOMER_NAME AS ACCOUNT_NAME
                       ,  '' ACCOUNT_ATTN_ID
                       ,  '' ACCOUNT_ATTN
                       ,  '' ACCOUNT_TEL
                       ,  '' ACCOUNT_FAX
                       ,  '' PAYMENT_ACCOUNT_CODE
                       ,  'KRW' CURRENCY_CODE_CUR
                       ,  '' EXRATE_TYPE
                       ,  '' EXRATE_DATE
                       ,  1 EXCHANGE_RATE
                       ,  0 AMOUNT_CUR
                       ,  0 AMOUNT_VAT_CUR
                       ,  0 AMOUNT_TOTAL_CUR
                       ,  0 DR_AMOUNT_TOTAL_CUR
                       ,  0 CR_AMOUNT_TOTAL_CUR
                       ,  'KRW' CURRENCY_CODE_LOC
                       ,  AMOUNT_LOC
                       ,  AMOUNT_VAT_LOC
                       ,  AMOUNT_TOTAL_LOC
                       ,  AMOUNT_TOTAL_LOC AS DR_AMOUNT_TOTAL_LOC
                       ,  0 CR_AMOUNT_TOTAL_LOC
                       ,  '' CURRENCY_TOTAL
                       ,  0  EXRATE_USD
                       ,  0 AMOUNT_TOTAL_USD
                       ,  '' REGISTER_USER_ID
                       ,  GETDATE()
                       ,  '' UPDATE_USER_ID
                       ,  GETDATE()
        FROM TB_BMS_ESTIMATE
        WHERE ESTIMATE_NO = 1
        SELECT
            #{invoiceJobNo} INVOICE_JOB_NO
			  , '1'   CHARGE_SEQ_NO
			  , ESTIMATE_NO AS OD_JOB_NO
			  , 'AP' AR_AP
			  , 'WHC' CHARGE_CODE
			  , '창고료' CHARGE_NAME
			  , '' FREIGHT_TERM
			  , 'Y' VAT_YNX
			  , 10 VAT_PERCENT
			  , 'KRW' CURRENCY_CODE_CUR
			  , 1 EXCHANGE_RATE
			  , '' RATE_MULTIPLY
			  , 0 RATE_QTY_WT
			  , 0 RATE_CUR
			  , 0 AMOUNT_CUR
			  , 0 AMOUNT_VAT_CUR
			  , 0 AMOUNT_TOTAL_CUR
			  , 'KRW' CURRENCY_CODE_LOC
			  , 0 RATE_LOC
			  , AMOUNT_LOC
			  , AMOUNT_VAT_LOC
			  , AMOUNT_TOTAL_LOC
			  , 'N' INV_CUR_YN
			  , '' VAT_JOB_NO
			  , 'N'  VAT_ISSUE_YN
			  ,  #{updateUserId} AS UPDATE_USER_ID
			  ,  GETDATE()   AS UPDATE_DATE_TIME
			  , 1 EXRATE_USD
			  , 0 AMOUNT_USD
			  , 0 AMOUNT_VAT_USD
			  , 0 AMOUNT_TOTAL_USD
        FROM
            TB_BMS_ESTIMATE
        WHERE ESTIMATE_NO   = #{estimateNo}


        CREATE PROCEDURE [dbo].[GET_NEXT_STOCK_MAX](
            @p_ENT CHAR(5)
        ,@p_ENT_BUSINESS_CODE VARCHAR(8)
        ,@p_STOCK_TYPE VARCHAR(10)        -- STOCK 구분
        ,@p_STOCK_SIGN VARCHAR(11)         -- STOCK 약어
        ,@p_STOCK_SEQ_LENGTH VARCHAR(11)   -- STOCK 순번자릿수
        ,@p_STOCK_ISSUE_YYMM VARCHAR(8)       -- 발번년월
        ,@p_SOLUTION VARCHAR(3)       			--  SOLUTION
        ,@p_INPUT_DATE VARCHAR(10)         -- 등록일자
        ,@p_RESULT_CODE VARCHAR(40)        -- STOCK 제번종류 기본->'DEFAULT'(2014.03 시점)
        ,@p_USER_ID VARCHAR(10)             -- 사용자 ID
        ,@p_REMARK VARCHAR(50)              -- STOCK REMARK
        ,@p_SUB_SEQ INT               -- sub seq
        ,@RES_STOCK_NO VARCHAR(100) OUTPUT
        )
        AS
        BEGIN
            DECLARE @N_MAX_SEQ_NO INT;
            DECLARE @S_NEW_STOCK_NO VARCHAR(40);
            DECLARE @S_REMARK VARCHAR(50);
            DECLARE @I_MAX_SEQ_NO INT;
            /*
            DECLARE  err INT DEFAULT 0;
            DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET err = -1;
            */

            SET @S_NEW_STOCK_NO = '';
            SET @I_MAX_SEQ_NO = 0;
            SET @N_MAX_SEQ_NO = 0;

            SELECT @N_MAX_SEQ_NO = A.MAX_SEQ_NO
            FROM (
                     SELECT ISNULL(MAX(MAX_NO), 0) + 1 AS MAX_SEQ_NO
                     FROM TB_COM_STOCK_MAX
                     WHERE 1=1
                       AND ENT_CODE = @p_ENT   -- 'YHTXX'
                       AND ENT_BUSINESS_CODE = @p_ENT_BUSINESS_CODE  -- 'YHTXX-KR
                       AND SOLUTION = @p_SOLUTION -- 'EMS'
                       AND STOCK_TYPE = @p_STOCK_TYPE   -- 'OBL'
                       AND STOCK_SIGN = @p_STOCK_SIGN  -- 'OBL'
                       AND STOCK_SEQ_LENGTH = @p_STOCK_SEQ_LENGTH  -- 5
                       AND STOCK_ISSUE_YYMM = @p_STOCK_ISSUE_YYMM  -- '201401'   *문서에 따라 년월을 사용하지 않거나 고정 년월을 사용하기도 한다.
                 ) A;

            -- SELECT GET_NEXT_STOCK_MAX('YHTXX', 'YHTXX-KR', 'S/R', 'OSR', 5, '201403', '2014-01-01', 'DEFAULT', '201309-001',  'S/R No') FROM DUAL
            -- STOCK 제번종류
            IF @p_RESULT_CODE = 'DEFAULT'
                SET @S_NEW_STOCK_NO = CONCAT(SUBSTRING(@p_ENT_BUSINESS_CODE, 7, 2), '-', @p_STOCK_SIGN, @p_STOCK_ISSUE_YYMM, '-', REPLICATE('0', @p_STOCK_SEQ_LENGTH - LEN(CONVERT(VARCHAR, @N_MAX_SEQ_NO))) + CONVERT(VARCHAR, @N_MAX_SEQ_NO));
                -- 2014.11.14 추가 edi용 afr문서 등 송신번호를 제번하기 위하여 사용한다. 쿼리에 마지막 날자를 고정값으로 넘겨서 사용하기도 한다.
            ELSE IF @p_RESULT_CODE = 'FIXED_SIGN_AND_SEQ_NO'     -- 단순히 넘어오는 고정문자에 순번으로만 제번한다.
                SET @S_NEW_STOCK_NO = CONCAT(@p_STOCK_SIGN, REPLICATE('0', @p_STOCK_SEQ_LENGTH - LEN(CONVERT(VARCHAR, @N_MAX_SEQ_NO))) + CONVERT(VARCHAR, @N_MAX_SEQ_NO));
            ELSE IF @p_RESULT_CODE = 'FIXED_SIGN_DATE_SEQ_NO'
                SET @S_NEW_STOCK_NO = CONCAT(@p_STOCK_SIGN, @p_STOCK_ISSUE_YYMM, REPLICATE('0', @p_STOCK_SEQ_LENGTH - LEN(CONVERT(VARCHAR, @N_MAX_SEQ_NO))) + CONVERT(VARCHAR, @N_MAX_SEQ_NO));
            ELSE
                SET @S_NEW_STOCK_NO = CONCAT(SUBSTRING(@p_ENT_BUSINESS_CODE, 7, 2), '-', @p_STOCK_SIGN, @p_STOCK_ISSUE_YYMM, '-', REPLICATE('0', @p_STOCK_SEQ_LENGTH - LEN(CONVERT(VARCHAR, @N_MAX_SEQ_NO))) + CONVERT(VARCHAR, @N_MAX_SEQ_NO));



            -- 비고사항
            SET @S_REMARK = @p_REMARK;
            IF ISNULL(@p_REMARK,'') = ''
                SET @S_REMARK = 'AUTO';

            IF ISNULL(@p_SUB_SEQ, 0) > 0
                SET @I_MAX_SEQ_NO = @N_MAX_SEQ_NO + @p_SUB_SEQ - 1;
            ELSE
                SET @I_MAX_SEQ_NO = @N_MAX_SEQ_NO;

            -- START TRANSACTION;
            MERGE TB_COM_STOCK_MAX A
            USING (
                SELECT
                    @p_ENT AS ENT_CODE
                     ,@p_ENT_BUSINESS_CODE AS ENT_BUSINESS_CODE
                     ,@p_SOLUTION AS SOLUTION
                     ,@p_STOCK_TYPE AS STOCK_TYPE
                     ,@p_STOCK_SIGN AS STOCK_SIGN
                     ,@p_STOCK_SEQ_LENGTH AS STOCK_SEQ_LENGTH
                     ,@p_STOCK_ISSUE_YYMM AS STOCK_ISSUE_YYMM
            ) B
            ON (A.ENT_CODE = B.ENT_CODE
                AND A.ENT_BUSINESS_CODE = B.ENT_BUSINESS_CODE
                AND A.SOLUTION = B.SOLUTION
                AND A.STOCK_TYPE = B.STOCK_TYPE
                AND A.STOCK_SIGN = B.STOCK_SIGN
                AND A.STOCK_SEQ_LENGTH = B.STOCK_SEQ_LENGTH
                AND A.STOCK_ISSUE_YYMM = B.STOCK_ISSUE_YYMM)
            WHEN MATCHED THEN
                UPDATE SET
                    MAX_NO = @I_MAX_SEQ_NO
                         ,STOCK_NO = @S_NEW_STOCK_NO
                         ,UPDATE_USER_ID = @p_USER_ID
                         ,UPDATE_DATE_TIME = GETDATE()
            WHEN NOT MATCHED BY TARGET THEN
                INSERT(
                        ENT_CODE
                      ,ENT_BUSINESS_CODE
                      ,STOCK_TYPE
                      ,STOCK_SIGN
                      ,STOCK_SEQ_LENGTH
                      ,STOCK_ISSUE_YYMM
                      ,SOLUTION
                      ,INPUT_DATE
                      ,MAX_NO
                      ,STOCK_NO
                      ,REMARKS
                      ,REGISTER_USER_ID
                      ,REGISTER_DATE_TIME
                      ,UPDATE_USER_ID
                      ,UPDATE_DATE_TIME
                )VALUES(
                         @p_ENT
                       ,@p_ENT_BUSINESS_CODE
                       ,@p_STOCK_TYPE
                       ,@p_STOCK_SIGN
                       ,@p_STOCK_SEQ_LENGTH
                       ,@p_STOCK_ISSUE_YYMM
                       ,@p_SOLUTION
                       ,@p_INPUT_DATE
                       ,(
                             SELECT ISNULL(MAX(MAX_NO), 0) + 1
                             FROM TB_COM_STOCK_MAX  sur
                             WHERE 1=1
                               AND  ENT_CODE = @p_ENT
                               AND  ENT_BUSINESS_CODE = @p_ENT_BUSINESS_CODE
                               AND  SOLUTION = @p_SOLUTION
                               AND  STOCK_TYPE = @p_STOCK_TYPE
                               AND  STOCK_SIGN = @p_STOCK_SIGN
                               AND  STOCK_SEQ_LENGTH = @p_STOCK_SEQ_LENGTH
                               AND  STOCK_ISSUE_YYMM = @p_STOCK_ISSUE_YYMM
                         )
                       ,@S_NEW_STOCK_NO
                       ,@S_REMARK
                       ,@p_USER_ID
                       ,GETDATE()
                       ,@p_USER_ID
                       ,GETDATE()
                       );
            SET @RES_STOCK_NO = @S_NEW_STOCK_NO;
        END



        ------------
        SELECT
            #{invoiceJobNo} INVOICE_JOB_NO
                       ,  #{entCode} ENT_CODE
                       ,  #{entBusinessCode} ENT_BUSINESS_CODE
                       ,  #{invoiceNo} INVOICE_NO
                       ,  '' INVOICE_NO_CUSTOMER
                       ,  '' INVOICE_NO_GROUP
                       ,  '' PRINT_GROUP_NO
                       ,  'BMS' SOLUTION
                       ,  'AIR' BOUND
                       ,  'IMP' BOUND_TYPE
                       ,  'LOCAL' LOCAL_OVERSEA
                       ,  'AR' AR_AP
                       ,  'OP-GENERAL' DOC_ISSUE_TYPE
                       ,  'Y' HBL_INV_YN
                       ,  'N' PREPAID_YN
                       ,  ESTIMATE_NO AS OD_JOB_NO
                       ,  BL_NO
                       ,  ESTIMATE_DATE AS INVOICE_DATE
                       ,  CUSTOMER_CODE AS ACCOUNT_CODE
                       ,  CUSTOMER_NAME AS ACCOUNT_NAME
                       ,  '' ACCOUNT_ATTN_ID
                       ,  '' ACCOUNT_ATTN
                       ,  '' ACCOUNT_TEL
                       ,  '' ACCOUNT_FAX
                       ,  '' PAYMENT_ACCOUNT_CODE
                       ,  'KRW' CURRENCY_CODE_CUR
                       ,  '' EXRATE_TYPE
                       ,  '' EXRATE_DATE
                       ,  1 EXCHANGE_RATE
                       ,  0 AMOUNT_CUR
                       ,  0 AMOUNT_VAT_CUR
                       ,  0 AMOUNT_TOTAL_CUR
                       ,  0 DR_AMOUNT_TOTAL_CUR
                       ,  0 CR_AMOUNT_TOTAL_CUR
                       ,  'KRW' CURRENCY_CODE_LOC
                       ,  AMOUNT_LOC
                       ,  AMOUNT_VAT_LOC
                       ,  AMOUNT_TOTAL_LOC
                       ,  AMOUNT_TOTAL_LOC AS DR_AMOUNT_TOTAL_LOC
                       ,  0 CR_AMOUNT_TOTAL_LOC
                       ,  '' CURRENCY_TOTAL
                       ,  0  EXRATE_USD
                       ,  0 AMOUNT_TOTAL_USD
                       ,  '' REGISTER_USER_ID
                       ,  GETDATE()
                       ,  '' UPDATE_USER_ID
                       ,  GETDATE()
        FROM TB_BMS_ESTIMATE
        WHERE ESTIMATE_NO = 1