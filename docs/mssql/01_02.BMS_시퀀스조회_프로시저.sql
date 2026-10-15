CREATE PROCEDURE dbo.USP_GETNextInOut_CUSTOMS_REPORT_NO
@mode CHAR(1) -- 파라미터 추가 (M 또는 A)
, @bmsWhCode	NVARCHAR(8)   -- TB_SMS_ENT_BUSINESS.BMS_WH_CODE
, @inOut CHAR(1)   -- 반입(I), 반출(O)
, @retCustomsReportNo nvarchar(18) OUTPUT -- 18자리 숫자 채번 번호
AS
BEGIN
    /**
      1) 프로시저 설명
           TB_BMS_IN 및  TB_BMS_OUT 테이블의
              CUSTOMS_REPORT_NO 컬럼에 보세구역(8) + 년도(2) + 일련번호 (8) 넣기 위함

      2) 프로시저 테스트 : retNoStr 변수로 넘어온 시퀀스를 사용 함
      DECLARE @mode_1 NVARCHAR(50) = 'M';
    DECLARE @inOut_1 NVARCHAR(50) = 'I';
    DECLARE @bmswhCode_1 NVARCHAR(8) = 'test1234';
    DECLARE @retSeq_1 NVARCHAR(18);

    -- 저장 프로시저 호출
    EXEC  USP_GETNextInOutSeq
        @mode       = @mode_1,
        @bmswhCode = @bmswhCode_1,
        @inOut     = @inOut_1,
        @retNoStr = @retSeq_1 OUTPUT

    -- 결과 출력
     SELECT 'Result' AS 'Test', @retSeq_1 AS 'ReturnValue';


     */
    -- 시퀀스에서 다음 값을 가져오는 쿼리
    DECLARE @NextValue INT;
    DECLARE @curYear NVARCHAR(4);
    DECLARE @curYY NVARCHAR(2);
    DECLARE @NextSeq NVARCHAR(8);
    /* 현재 날짜 구하기 */
    SELECT  @curYear=  cast(DATEPART(YEAR, GETDATE()) as NVARCHAR(4));

    IF ( @mode = 'M' )
        BEGIN
            -- Manual 모드: 현재 시퀀스 값에 1을 더함
            IF( @curYear='2024')
                BEGIN
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_IN_SEQ_M_2033;
                        END
                    ELSE
                        BEGIN
                            SELECT @NextValue = NEXT VALUE FOR dbo.TB_BMS_OUT_SEQ_M_2033;
                        END
                END
        END
    ELSE IF @mode = 'A'
        BEGIN
            -- AUTO 모드: 현재 시퀀스 값에 1을 더함
            IF( @curYear='2024')
                BEGIN
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
                    IF( @inOut='I')
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
    SET  @NextSeq   = FORMAT(@NextValue, '00000000'); /* 시퀀스 값에서 8자리로 포멧 변경 */
    SET @curYY      = RIGHT(@curYear,2);
    SET @retCustomsReportNo   =  CONCAT(@bmsWhCode, @curYY,@NextSeq);
    -- SELECT @inOut AS 'Result';
END