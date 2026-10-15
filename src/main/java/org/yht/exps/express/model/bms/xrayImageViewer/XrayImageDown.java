package org.yht.exps.express.model.bms.xrayImageViewer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "XrayImageDown", description = "XRAY 이미지 다운로드")
@Alias("xrayImageDown")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class XrayImageDown extends ResultList {

    @ApiModelProperty(value = "Base64 인코딩된 이미지 데이터")
    private String base64Data;
    @ApiModelProperty(value = "다운로드 파일명")
    private String fileName;
}
