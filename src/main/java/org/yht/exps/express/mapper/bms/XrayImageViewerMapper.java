package org.yht.exps.express.mapper.bms;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.xrayImageViewer.PosSideImage;
import org.yht.exps.express.model.bms.xrayImageViewer.PosTopImage;
import org.yht.exps.express.model.bms.xrayImageViewer.XrayImage;
import org.yht.exps.express.model.bms.xrayImageViewer.XrayImageData;
import org.yht.exps.express.model.bms.xrayImageViewer.search.SearchXrayImage;

import java.util.List;

@Mapper
public interface XrayImageViewerMapper {
    List<PosTopImage> selectPosTopImageList(SearchXrayImage search);

    List<PosSideImage> selectPosSideImageList(SearchXrayImage search);

    List<XrayImage> selectImagePathList(SearchXrayImage search);

    List<CarryIn> selectXrayImageInfoList(SearchXrayImage search);
    
    XrayImageData selectXrayImageData(SearchXrayImage search);
    List<XrayImageData> selectAllXrayImageDataBybl(SearchXrayImage search);
    
}
