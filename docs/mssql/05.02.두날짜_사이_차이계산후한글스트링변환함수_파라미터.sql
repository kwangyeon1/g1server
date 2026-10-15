CREATE FUNCTION dbo.GET_DATE_DIFF_KOR_STR_START_END
(
    @v_StartDate VARCHAR(20)
, @v_EndDate VARCHAR(20)
)
    RETURNS VARCHAR(40)
AS
BEGIN
    /**
     * 사용법
     *  select GET_DATE_DIFF_KOR_STR_START_END('2023-01-01 10:14:59', '2024-03-26 08:47:59');
     */
    --DECLARE @StartDate DATETIME = CONVERT(DATETIME, '2023-01-01 10:14:59');
    DECLARE @StartDate DATETIME = CONVERT(DATETIME, @v_StartDate);
    DECLARE @EndDate DATETIME = CONVERT(DATETIME, @v_EndDate);

    DECLARE @retStr nvarchar(40);

    if( LEN(@v_StartDate) != 19)
        begin
            return 'NULL DATE'
        end

    DECLARE @Diff INT = DATEDIFF(SECOND, @StartDate, @EndDate);

    SELECT
        @retStr =CONCAT(
                FLOOR(@Diff / (24 * 3600)), '일 ',
                FLOOR((@Diff % (24 * 3600)) / 3600), '시 ',
                FLOOR(((@Diff % (24 * 3600)) % 3600) / 60), '분 ',
                (@Diff % 60), '초'
                 ) ;

    return @retStr;
end

-- select GET_DATE_DIFF_KOR_STR_START_END('2023-01-01 10:14:59', '2024-03-26 08:47:58');