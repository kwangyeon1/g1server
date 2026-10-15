package org.yht.exps.express.model.bms.xrayImageViewer.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchXrayImage", description = "xray 이미지 조회")
@Alias("searchXrayImage")
public class SearchXrayImage extends SearchList {
    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String srchEntCode;
    @ApiModelProperty(value="조회번호")
    private String srchNo;
    @ApiModelProperty(value="조회번호타입")
    private String srchNoType;
    @ApiModelProperty(value="조회타입 멀티/싱글")
    private String searchType;
    @ApiModelProperty(value="srchNo N개씩 조회시 리스트")
    private List<String> searchNoList;

    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="이미지경로")
    private String imagePath;
}
