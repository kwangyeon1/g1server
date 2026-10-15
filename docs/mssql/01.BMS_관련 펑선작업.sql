-- 시퀀스 생성 ( MANUAL 입력시 사용하는 시퀀스 )
-- bigint 9,223,372,036,854,775,807
--  int 값  2,147,483,647 ( 20억개 )
-- CUSTOMS_REPORT_NO	varchar	18	''		Y	반입신고번호	보세구역(8) + 년도(2) + 일련번호 (8)
-- 보세 구역은 ENT_BUSINESS_CODE //TB_SMS_ENT_BUSINESS
-- 사업장 관리 , 옵션1 관리, 보세 구역 정보에서 가져 온다. TB_SMS_ENT_BUSINESS.BMS_WH_CODE ( 보세 구역 번호 )
-- 향후 10년치 시퀀스 생성한다.
-- MAXVALUE 99999999 : 8자리, 69999999
CREATE SEQUENCE dbo.TB_BMS_IN_SEQ_M
AS int
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 69999999
    CACHE 500
    CYCLE;
-- 시퀀스 생성 ( AUTO 입력시 사용하는 시퀀스 )
CREATE SEQUENCE dbo.TB_BMS_IN_SEQ_A
    AS int
    START WITH 70000001
    INCREMENT BY 1
    MAXVALUE 99999999
    CACHE 500
    CYCLE;

/* NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M 로 사용됨

   IN , OUT
   Year

   */

-- 사용자 함수 생성
    CREATE FUNCTION dbo.GET_CUSTOMER_REPORT_NO_SEQ
    (
        @Mode CHAR(1) -- 파라미터 추가 (M 또는 A)
    , @bmswhCode VARCHAR(8)   -- TB_SMS_ENT_BUSINESS.BMS_WH_CODE
    , @inOut CHAR(1)   -- 반입(I), 반출(O)
    )
        RETURNS VARCHAR(18)
    AS
    BEGIN
        DECLARE @NextValue INT;
        DECLARE @NextSeq VARCHAR(8);
        DECLARE @RetSeq VARCHAR(18);
        DECLARE @curYear VARCHAR(4);
        /* 현재 날짜 구하기 */
        SELECT  @curYear=  cast(DATEPART(YEAR, GETDATE()) as varchar(4));

      --/* 함수에서 시퀀스 호출하는 프로시저 실행은 mssql 에서 되지 않는다. */
     --   EXEC USP_GETNextInOutSeq   @curYear =  @curYear,  @Mode  = @Mode,  @inOut = @inOut , @retSeq = @NextValue OUTPUT;

        SELECT  @NextSeq = FORMAT(@NextValue, '00000000'); /* 시퀀스 값에서 8자리로 포멧 변경 */
        SELECT @RetSeq   = CONCAT(@bmswhCode, @curYear,@NextSeq);
        RETURN @RetSeq;
    END;



ALTER PROCEDURE USP_GETNextInOutSeq
@Mode CHAR(1) -- 파라미터 추가 (M 또는 A)
, @bmswhCode	NVARCHAR(8)   -- TB_SMS_ENT_BUSINESS.BMS_WH_CODE
, @inOut CHAR(1)   -- 반입(I), 반출(O)
, @retNo NVARCHAR(20) OUTPUT
AS
BEGIN
    -- 시퀀스에서 다음 값을 가져오는 쿼리
    DECLARE @NextValue INT;
    DECLARE @curYear VARCHAR(4);
    DECLARE @NextSeq varchar(8);
    /* 현재 날짜 구하기 */
    SELECT  @curYear=  cast(DATEPART(YEAR, GETDATE()) as varchar(4));

    IF ( @Mode = 'M' )
        BEGIN
            -- Manual 모드: 현재 시퀀스 값에 1을 더함
            IF( @curYear='2024')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2024;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2024;
                        END
                END
            ELSE IF ( @curYear='2025')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2025;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2025;
                        END
                END
            ELSE IF ( @curYear='2026')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2026;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2026;
                        END
                END
            ELSE IF ( @curYear='2027')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2027;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2027;
                        END
                END
            ELSE IF ( @curYear='2028')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2028;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2028;
                        END
                END
            ELSE IF ( @curYear='2029')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2029;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2029;
                        END
                END
            ELSE IF ( @curYear='2030')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2030;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2030;
                        END
                END
            ELSE IF ( @curYear='2031')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2031;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2031;
                        END
                END
            ELSE IF ( @curYear='2032')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2032;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2032;
                        END
                END
            ELSE IF ( @curYear='2033')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2033;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2033;
                        END
                END
        END
    ELSE IF @Mode = 'A'
        BEGIN
            -- AUTO 모드: 현재 시퀀스 값에 1을 더함
            IF( @curYear='2024')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_A_2024;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_A_2024;
                        END
                END
            ELSE IF ( @curYear='2025')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_A_2025;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_A_2025;
                        END
                END
            ELSE IF ( @curYear='2026')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_A_2026;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_A_2026;
                        END
                END
            ELSE IF ( @curYear='2027')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_A_2027;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_A_2027;
                        END
                END
            ELSE IF ( @curYear='2028')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_A_2028;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_A_2028;
                        END
                END
            ELSE IF ( @curYear='2029')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_A_2029;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_A_2029;
                        END
                END
            ELSE IF ( @curYear='2030')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_A_2030;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_A_2030;
                        END
                END
            ELSE IF ( @curYear='2031')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_A_2031;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_A_2031;
                        END
                END
            ELSE IF ( @curYear='2032')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_A_2032;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_A_2032;
                        END
                END
            ELSE IF ( @curYear='2033')
                BEGIN
                    IF( @inOut='IN')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_A_2033;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_A_2033;
                        END
                END
        END

    -- 리턴할 값을 OUTPUT 매개변수에 설정
    SET  @NextSeq =  FORMAT(@NextValue, '00000000'); /* 시퀀스 값에서 8자리로 포멧 변경 */
    SET @retNo    =   CONCAT(@bmswhCode, @curYear,@NextSeq);
    RETURN @retNo;
    -- 추가로 필요한 로직 수행 가능
    -- 결과값 출력 (추가로 필요한 로직이 있다면 이 부분에 작성)
    -- SELECT @inOut AS 'Result';
END


--- USP_GETNextInOutSeq 테스트 코드
DECLARE @Year INT = '2024';
--DECLARE @Mode NVARCHAR(50) = 'A'; 70000001
DECLARE @Mode NVARCHAR(50) = 'M';   -- 1
DECLARE @inOut_1 NVARCHAR(50) = 'I';
DECLARE @retSeq INT;

-- 저장 프로시저 호출
    EXEC dbo.USP_GETNextInOutSeq
         @curYear = @Year,
         @Mode  = @Mode,
         @inOut  = @inOut_1,
         @retSeq = @retSeq OUTPUT

-- 결과 출력
SELECT 'Result' AS 'Test', @retSeq AS 'ReturnValue';
-- Auto 모드 호출
DECLARE @ResultAuto INT;
SET @ResultAuto = dbo.GetNextSequenceValue('A');
PRINT 'Next Auto Value: ' + CAST(@ResultAuto AS NVARCHAR(10));


/*
     MSSQL(또는 Microsoft SQL Server)에서 함수에서 시퀀스를 호출하는 프로시저를 실행하는 것은 일반적으로 지원되지 않습니다. SQL Server에서는 함수 내에서 특정 종류의 작업을 수행하는 것에 제한이 있습니다. 시퀀스 호출과 같은 일부 기능은 사용자 정의 함수에서는 허용되지 않습니다.

일반적으로 시퀀스는 SQL Server에서는 주로 시퀀스 객체를 사용하여 생성하고 관리하며, 이는 주로 트랜잭션 처리를 위해 사용됩니다. 시퀀스는 주로 값을 생성하고 해당 값을 테이블의 열에 대입하는 데 사용됩니다.

프로시저(저장 프로시저 또는 stored procedure)는 SQL Server에서 복잡한 로직을 구현하고 호출하는 데 사용되며, 트랜잭션 관리, 데이터 조작 등 다양한 작업을 수행할 수 있습니다. 함수와 프로시저 간에는 몇 가지 차이가 있으며, 함수에서는 특정 제약 조건이나 동작이 제한됩니다.

만약 시퀀스 호출과 같은 기능이 필요하다면, 해당 로직을 저장 프로시저 내에서 구현하거나 다른 방식으로 접근해야 할 것입니다. 함수에서는 트랜잭션을 제어하고 시퀀스 호출과 같은 작업을 수행하는 데는 한계가 있습니다.

더 자세한 도움을 얻기 위해서는 구체적인 상황이나 코드를 제공해 주시면 더 정확한 도움을 드릴 수 있습니다.
     */

    SYSDATETIME()      2007-04-30 13:10:02.0474381
    SYSDATETIMEOFFSET()2007-04-30 13:10:02.0474381 -07:00
    SYSUTCDATETIME()   2007-04-30 20:10:02.0474381
    CURRENT_TIMESTAMP  2007-04-30 13:10:02.047
    GETDATE()          2007-04-30 13:10:02.047
    GETUTCDATE()       2007-04-30 20:10:02.047

-- 차이 일시분 표시
DECLARE @StartDate DATETIME = CONVERT(DATETIME, '2023-01-01 10:14:59');
DECLARE @EndDate DATETIME = GETDATE();

DECLARE @Diff INT = DATEDIFF(SECOND, @StartDate, @EndDate);

SELECT
    CONCAT(
            FLOOR(@Diff / (24 * 3600)), '일 ',
            FLOOR((@Diff % (24 * 3600)) / 3600), '시 ',
            FLOOR(((@Diff % (24 * 3600)) % 3600) / 60), '분 ',
            (@Diff % 60), '초'
    ) AS DateDifference;
