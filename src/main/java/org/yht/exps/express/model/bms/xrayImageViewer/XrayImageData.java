package org.yht.exps.express.model.bms.xrayImageViewer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "XrayImageData", description = "XRAY 이미지 데이터")
@Alias("xrayImageData")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class XrayImageData extends ResultList {

    @ApiModelProperty(value = "TOP or SID")
    private String imageType;
    @ApiModelProperty(value = "Base64 인코딩된 이미지 데이터")
    private byte[] imageData;
    @ApiModelProperty(value = "파일 이름")
    private String imagePath;
    @ApiModelProperty(value = "시리얼키")
    private int serialKey;
    @ApiModelProperty(value = "기록일시")
    private String created;
}
