package org.yht.exps.express.model.bms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/***
 * UMrs 서버로 전송하기 위한 모델 정의
 * {"customsErrDesc":"정상처리","customsErrCode":"C200","data":null,"body":"ebXML 메시지가 정상 처리 되었습니다."}
 */
@Data
@Alias("mrsRet")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class MrsRet {
    /**
     * 응답내용: 정상처리
     */
    String customsErrDesc;
    /**
     * 응답코드 : C200 : 정상, 이외코드 : 비정상
     */
    String customsErrCode;
    /**
     * 이외 데이터
     */
    String data;
    /**
     * body : 응답 메시지
     * 에러시: 에러 메시지가 상세 하게 표시됨
     */
    String body;


}
