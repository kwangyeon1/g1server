
/* 다른 디비로 이관시에 시퀀스 값을 max 값에 위채 하도록 세팅 처리함
TB_BMS_IN_SEQ_M_2024	 		70002000
TB_BMS_IN_SEQ_A_2024	         70000
TB_BMS_OUT_SEQ_M_2024	 		70002000
TB_BMS_OUT_SEQ_A_2024	 	    60000

   시퀀스 현재값 확인 처리
 SELECT
    name AS sequence_name,
    current_value
FROM
    sys.sequences
    ;
*/

-- 기존 시퀀스를 삭제합니다.
DROP SEQUENCE TB_BMS_IN_SEQ_M_2024;
-- 새로운 시작 값으로 시퀀스를 다시 생성합니다.
CREATE SEQUENCE TB_BMS_IN_SEQ_M_2024
    START WITH 70002000
    INCREMENT BY 1
    MAXVALUE 99999999
    CACHE 500
    CYCLE;  -- 캐시 크기는 500으로 지정합니다.


-- 기존 시퀀스를 삭제합니다.
DROP SEQUENCE TB_BMS_IN_SEQ_A_2024;
-- 새로운 시작 값으로 시퀀스를 다시 생성합니다.
CREATE SEQUENCE TB_BMS_IN_SEQ_A_2024
    START WITH 70000
    INCREMENT BY 1
    MAXVALUE 69999999
    CACHE 500
    CYCLE;  -- 캐시 크기는 500으로 지정합니다.

-- 기존 시퀀스를 삭제합니다.
DROP SEQUENCE TB_BMS_OUT_SEQ_M_2024;
-- 새로운 시작 값으로 시퀀스를 다시 생성합니다.
CREATE SEQUENCE TB_BMS_OUT_SEQ_M_2024
    START WITH 70002000
    INCREMENT BY 1
    MAXVALUE 99999999
    CACHE 500
    CYCLE;  -- 캐시 크기는 500으로 지정합니다.


-- 기존 시퀀스를 삭제합니다.
DROP SEQUENCE TB_BMS_OUT_SEQ_A_2024;
-- 새로운 시작 값으로 시퀀스를 다시 생성합니다.
CREATE SEQUENCE TB_BMS_OUT_SEQ_A_2024
    START WITH 60000
    INCREMENT BY 1
    MAXVALUE 69999999
    CACHE 500
    CYCLE;  -- 캐시 크기는 500으로 지정합니다.



