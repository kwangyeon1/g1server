CREATE FUNCTION dbo.GET_DATE_DIFF_WORK_DAYS
(
    @v_StartDate VARCHAR(10)
, @v_EndDate VARCHAR(10)
)
    RETURNS VARCHAR(40)
AS
BEGIN
    /**
     * 창고료 계산시 사용하는 보관일수 계산 함수로, 전체 일수중에서 토,일,휴일등을 제외한 순수 근무 일수 리턴 함
     * 사용법
     *  select dbo.GET_DATE_DIFF_WORK_DAYS('2023-01-01', '2024-03-26');
     *  select dbo.GET_DATE_DIFF_WORK_DAYS('2024-01-01', '2024-12-31'); -- 247
     */
    DECLARE @StartDate DATE = @v_StartDate;
    DECLARE @EndDate DATE = @v_EndDate;
    DECLARE @v_HoliDay INT = 0; /* 휴일 계산 로직 */

    DECLARE @v_workDay INT = 0; /* 최종 일한 일수: 최종일-토일갯수-휴일갯 */

    /* 1) 휴일 제외 카운트 처리 */
    SELECT  @v_HoliDay = COUNT(1)  from TB_SMS_HOLIDAY
    WHERE HOLIDAY_DATE BETWEEN @v_StartDate AND  @v_EndDate
    ;
    /* 2) 토일 (1,7) 제외한 날자에서 휴일 제외후 리턴 처리 */
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
            END)   - @v_HoliDay   - 1 )  /* DATEDIFF 계산시 같은날은 0이다 작은것만 계산한다. 계산법에서 -1을 해줘야 정상값이 리턴된다. */
    FROM DateRange

    /*SELECT @v_workDay AS WORK_DAYS*/

    return @v_workDay
end
