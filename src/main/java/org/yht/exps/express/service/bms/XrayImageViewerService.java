package org.yht.exps.express.service.bms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.XrayImageViewerMapper;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.CarryInList;
import org.yht.exps.express.model.bms.xrayImageViewer.PosSideImage;
import org.yht.exps.express.model.bms.xrayImageViewer.PosTopImage;
import org.yht.exps.express.model.bms.xrayImageViewer.XrayImage;
import org.yht.exps.express.model.bms.xrayImageViewer.XrayImageData;
import org.yht.exps.express.model.bms.xrayImageViewer.XrayImageDown;
import org.yht.exps.express.model.bms.xrayImageViewer.XrayImageList;
import org.yht.exps.express.model.bms.xrayImageViewer.search.SearchXrayImage;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class XrayImageViewerService {

    @Autowired
    XrayImageViewerMapper xrayImageViewerMapper;

    public XrayImageList selectPosTopImageList(SearchXrayImage search) throws Exception {
        XrayImageList xrayImageList = new XrayImageList();
        if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
        if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

        /* N개 검색(멀티타입) */
        List<String> searchNoList = new ArrayList<String>();
        if(!"".equals(Util.null2str(search.getSrchNo()))) {
            searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
        }

        if(searchNoList.size() > 1){
            search.setSearchType("multi");
            search.setSearchNoList(searchNoList);
        }else{
            search.setSearchType("");
            search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
        }
        
        Util.accessControlDatabase(search, null, true, null, "", null, "");
        List<PosTopImage> list  = xrayImageViewerMapper.selectPosTopImageList(search);
        xrayImageList.setPosTopImageList(list);
        if(list.size() > 0) {
            xrayImageList.setTotal(list.get(0).getTotal());
        }else {
            xrayImageList.setTotal(0);
        }

        return xrayImageList;
    }
    
    public XrayImageList selectPosSideImageList(SearchXrayImage search) throws Exception {
        XrayImageList xrayImageList = new XrayImageList();
        if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
        if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

        /* N개 검색(멀티타입) */
        List<String> searchNoList = new ArrayList<String>();
        if(!"".equals(Util.null2str(search.getSrchNo()))) {
            searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
        }

        if(searchNoList.size() > 1){
            search.setSearchType("multi");
            search.setSearchNoList(searchNoList);
        }else{
            search.setSearchType("");
            search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
        }
        Util.accessControlDatabase(search, null, true, null, "", null, "");
        List<PosSideImage> list  = xrayImageViewerMapper.selectPosSideImageList(search);
        xrayImageList.setPosSideImageList(list);
        if(list.size() > 0) {
            xrayImageList.setTotal(list.get(0).getTotal());
        }else {
            xrayImageList.setTotal(0);
        }
        return xrayImageList;
    }
    
    public XrayImageList selectXrayImagePathList(SearchXrayImage search) {
        XrayImageList xrayImageList = new XrayImageList();
        List<XrayImage> list  = xrayImageViewerMapper.selectImagePathList(search);
        List<XrayImage> xrayAllImageList = new ArrayList<>();
        
        for (XrayImage img: list) {
            String imgPathTop = img.getImagePathTop();
            String imgPathSide = img.getImagePathSid();
            //log.info("imgPathTop : {}", imgPathTop);
            //log.info("imgPathSide : {}", imgPathSide);
            
            if(!imgPathTop.equals("")) {
            	img.setImageType("TOP");
                img.setImagePath(imgPathTop);
            	xrayAllImageList.add(img);
            }
            if(!imgPathSide.equals("")) {
            	img.setImageType("SID");
                img.setImagePath(imgPathSide);
            	xrayAllImageList.add(img);
            }
        }
        if(xrayAllImageList.size() > 0) {
            xrayImageList.setXrayAllImageList(xrayAllImageList);
        }
        return xrayImageList;
    }

    public CarryInList selectXrayImageInfoList(SearchXrayImage search) throws Exception {
        CarryInList carryInList = new CarryInList();
        if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
        if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

        /* N개 검색(멀티타입) */
        List<String> searchNoList = new ArrayList<String>();
        if(!"".equals(Util.null2str(search.getSrchNo()))) {
            searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
        }

        if(searchNoList.size() > 1){
            search.setSearchType("multi");
            search.setSearchNoList(searchNoList);
        }else{
            search.setSearchType("");
            search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
        }

        Util.accessControlDatabase(search, null, true, null, "", null, "");
        List<CarryIn> list  = xrayImageViewerMapper.selectXrayImageInfoList(search);
        carryInList.setCarryInList(list);
        if(list.size() > 0) {
            carryInList.setTotal(list.get(0).getTotal());
        }else {
            carryInList.setTotal(0);
        }
        return carryInList;
    }
    
    public XrayImageList selectXrayImageData(SearchXrayImage search) {
    	XrayImageList xrayImageList = new XrayImageList();
    	List<String> base64ImageList = new ArrayList<>();
    	
    	try {
    		XrayImageData xrayImageData = xrayImageViewerMapper.selectXrayImageData(search);
    		
    		if(xrayImageData != null) {
    			String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(xrayImageData.getImageData());
    			base64ImageList.add(base64Image);
    			xrayImageList.setXrayImageDataList(base64ImageList);
    		} else {
    			xrayImageList.setXrayImageDataList(null);
    		}
    	} catch(Exception e) {
    		/**log.error(Util.getPrintStacTraceString(e));
    		SQLServerException 파일 "파일명.jpg"을(를) 열 수 없으므로 대량 로드할 수 없습니다.
    		DB서버에 경로 또는 파일이 없는경우 예외처리
    		**/
    		xrayImageList.setXrayImageDataList(null);
    	}
        return xrayImageList;
    }
    
    public List<XrayImageDown> selectAllXrayImageData(SearchXrayImage search) {
    	List<XrayImageDown> xrayImageDown = new ArrayList<>();
    	
    	search.setSearchType("TOP");
    	List<XrayImageData> posTopXrayImageData = xrayImageViewerMapper.selectAllXrayImageDataBybl(search);
    	xrayImageDown = addAllXrayImageData(search, posTopXrayImageData, xrayImageDown);
    	
    	search.setSearchType("SIDE");
    	List<XrayImageData> posSidXrayImageData = xrayImageViewerMapper.selectAllXrayImageDataBybl(search);
    	xrayImageDown = addAllXrayImageData(search, posSidXrayImageData, xrayImageDown);
    	
    	// top, side 이미지 합쳐서 return
    	return xrayImageDown;
    }
    
    public List<XrayImageDown> addAllXrayImageData(SearchXrayImage search, List<XrayImageData> dataList, List<XrayImageDown> downList) {
    	
    	for(XrayImageData xid : dataList) {
    		if(xid.getImageData() != null) {
    			String base64Image = Base64.getEncoder().encodeToString(xid.getImageData());
    			String fileName = xid.getImagePath();
    			
        		if (fileName == null || fileName.isEmpty()) {
        		    fileName = "unknown_" + System.currentTimeMillis() + ".jpg"; // 기본 파일명 설정
        		} else {
        			int lastDotIndex = fileName.lastIndexOf(".");
        			if(lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) { // 확장자가 없거나 '.'이 마지막 문자일 경우
        				fileName = "unknown_" + System.currentTimeMillis() + ".jpg";
        			} else {
        				fileName = search.getBlNo() + "_" + search.getSearchType() + "_" + xid.getNum() + fileName.substring(lastDotIndex); // BlNo_TOP_1.jpg
        			}
        		}
        		downList.add(new XrayImageDown(base64Image, fileName));
    		}
    	}
    	
    	return downList;
    }
    
}
