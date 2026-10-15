package org.yht.exps.express.model.bms.xrayImageViewer;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.util.CustomErrorType;

import java.util.List;

@Data
@ApiModel(value = "XrayImageList", description = "Xray 이미지 리스트")
@Alias("xrayImageList")
public class XrayImageList {

    @ApiModelProperty(value="리스트 개수")
    private long total;

    @ApiModelProperty(value="위에서 찍은 이미지 리스트")
    List<PosTopImage> posTopImageList;

    @ApiModelProperty(value="측면에서 찍은 이미지 리스트")
    List<PosSideImage> posSideImageList;

    @ApiModelProperty(value="이미지 경로 리스트")
    List<String> xrayImagePathList;
    
    @ApiModelProperty(value="이미지 데이터 리스트")
    List<String> xrayImageDataList;
    
    @ApiModelProperty(value="이미지 리스트")
    List<XrayImage> xrayAllImageList;

    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;

    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}
