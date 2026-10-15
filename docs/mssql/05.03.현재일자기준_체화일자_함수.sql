
CREATE  FUNCTION  [dbo].[GET_OVER_DATE_END]
    (
         @v_baseDate VARCHAR(10) /* 체화일자 계산 기준일자 CONVERT(VARCHAR(10),GETDATE(),120) */
        , @v_Days INT  /* 체화 더한 월수 옵션 테이블*/
        , @v_Type VARCHAR(4) /* OVER.체화, NOTI.통고 */
        )
    RETURNS VARCHAR(10) /*리턴값은 2024-02-03 */
    AS
    BEGIN
        /**
         * 체화일자 리턴
         * 반출통고 일자: 체화일자에서 -30일 리턴
         * 옵션 테이블에서 세팅된 값 3을 가지고 3달 을 계산 하고 토,일요일이면 월요일 날짜를 리턴 한다.
         * 사용법
         *  select dbo.GET_OVER_DATE_END('2023-01-01',3,'OVER');
         * select dbo.GET_OVER_DATE_END(CONVERT(VARCHAR(10),GETDATE(),120),3,'OVER');
         옵션값참고 조회  select
            CAST( ISNULL(eb01.SET_VALUE,3) AS INT )
            , dbo.GET_OVER_DATE_END(CONVERT(VARCHAR(10),GETDATE(),120), CAST( ISNULL(eb01.SET_VALUE,3) AS INT ),'OVER') AS OVER_DATE
            from
            TB_SMS_ENT_BUS_OPTION eb01
                where  eb01.OPTION_CODE ='A01'

         */
        DECLARE @currentDate DATETIME = CONVERT(DATETIME, @v_baseDate, 120) ; --//GETDATE(); -- 현재 날짜 및 시간 가져오기
        DECLARE @nextDate DATETIME;
        DECLARE @retStr nvarchar(10);
-- 현재 날짜에서 3개월을 더하기  @v_Days
        SET @nextDate = DATEADD(MONTH, @v_Days, @currentDate);
        if (@v_Type = 'NOTI') /* 통고일자는 체회일자에서 -30 */
            begin
                SET @nextDate = DATEADD(DAY,-30,  @nextDate);
            end

        --return DATEPART(WEEKDAY, @nextDate)
-- 만약 더한 날이 토요일 또는 일요일인지 확인
        IF DATEPART(WEEKDAY, @nextDate) IN (1, 7) -- 1은 일요일, 7은 토요일
            BEGIN
                -- 다음 월요일의 날짜 계산
                SET @nextDate = DATEADD(DAY, CASE WHEN DATEPART(WEEKDAY, @nextDate) = 7 THEN 2 ELSE 1 END, @nextDate);
            END

        SET @retStr = CONVERT(VARCHAR(10), @nextDate,120)   ; -- 결과 출력  23일경우 2012-09-13

        return @retStr;
    end;




--select dbo.GET_OVER_DATE_END('2023-01-06',3,'OVER') as O1,dbo.GET_OVER_DATE_END('2023-01-01',3,'NOTI') as N1;

