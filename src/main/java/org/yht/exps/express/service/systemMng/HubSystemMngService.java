package org.yht.exps.express.service.systemMng;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.yht.exps.express.mapper.systemMng.HubSystemMngMapper;
import org.yht.exps.express.model.com.fileshare.FileShare;
import org.yht.exps.express.model.com.fileshare.FileShareFile;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.systemMng.HubNotice;
import org.yht.exps.express.model.systemMng.HubNoticeList;
import org.yht.exps.express.model.systemMng.HubQnA;
import org.yht.exps.express.model.systemMng.HubQnADelete;
import org.yht.exps.express.model.systemMng.HubQnAList;
import org.yht.exps.express.model.systemMng.HubQnASaveResult;
import org.yht.exps.express.model.systemMng.HubSubQnAList;
import org.yht.exps.express.model.systemMng.search.SearchHubMainNoticeList;
import org.yht.exps.express.model.systemMng.search.SearchHubQnADetail;
import org.yht.exps.express.model.systemMng.search.SearchHubQnAList;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class HubSystemMngService {
	
	@Autowired
	CustomProperties customProperties;            
	
	@Autowired
	HubSystemMngMapper hubSystemMngMapper;
	
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	
	/**
	 * eHub 문의사항(Q&A) 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public HubQnAList selectQnAList(SearchHubQnAList search) throws Exception {
		HubQnAList HubQnAList = new HubQnAList();
		
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
 		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
 	
		if(search.getEcmCustomerCode() != null && search.getEcmCustomerCode() != "") {
			List<HubQnA> srchCustomerCodeList = hubSystemMngMapper.selectCustomerCodeList(search);
			
			List<String> customerCodeList = new ArrayList<>();
			for (HubQnA HubQnA : srchCustomerCodeList) {
				customerCodeList.add(HubQnA.getCustomerCode());
			}

	        search.setSrchCustomerCodeList(customerCodeList);
		}
		
		List<HubQnA> list  = hubSystemMngMapper.selectQnAList(search);
		HubQnAList.setHubQnAList(list);
		if(list != null && list.size() > 0) {
			HubQnAList.setTotal(list.get(0).getTotal());
		}else {
			HubQnAList.setTotal(0);
		}
		
		return HubQnAList;
	}
	
	/**
	 * eHub 문의사항(Q&A) 건수 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HubQnASaveResult selectQnACountSummary(SearchHubQnAList search) throws Exception {
		HubQnASaveResult HubQnASaveResult = new HubQnASaveResult();
		
		if(search.getEcmCustomerCode() != null && search.getEcmCustomerCode() != "") {
			List<HubQnA> srchCustomerCodeList = hubSystemMngMapper.selectCustomerCodeList(search);
			
			List<String> customerCodeList = new ArrayList<>();
			for (HubQnA HubQnA : srchCustomerCodeList) {
				customerCodeList.add(HubQnA.getCustomerCode());
			}

	        search.setSrchCustomerCodeList(customerCodeList);
		}
		Map<String, Object> summary = hubSystemMngMapper.selectQnASummary(search);
		
		int sUnansweredCnt = ((Number) summary.get("S_UNANSWERED_CNT")).intValue();
		int cUnansweredCnt = ((Number) summary.get("C_UNANSWERED_CNT")).intValue();

		// 관리자 미답변 건수	
		HubQnASaveResult.setSUnansweredCnt(sUnansweredCnt);
		// 거래처 미답변 건수	
		HubQnASaveResult.setCUnansweredCnt(cUnansweredCnt);
		
		return HubQnASaveResult;
	}

	/**
	 * eHub문의사항(Q&A) subGrid 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public HubSubQnAList selectSubQnAList(SearchHubQnAList search) throws Exception {
		HubSubQnAList HubSubQnAList = new HubSubQnAList();
		
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		List<HubQnA> list  = hubSystemMngMapper.selectSubQnAList(search);
		SearchHubQnADetail title = new SearchHubQnADetail();
		title.setEntCode(search.getEntCode());
		title.setReferenceNo(search.getReferenceNo());
		HubQnA detail = hubSystemMngMapper.selectQnADetail(title);
		
		HubSubQnAList.setHubQnA(detail);
		HubSubQnAList.setHubSubQnAList(list);
		
		if(list != null && list.size() > 0) {
			HubSubQnAList.setTotal(list.get(0).getTotal());
		}else {
			HubSubQnAList.setTotal(0);
		}
		
		return HubSubQnAList;
	}
	

	/**
	 * eHub 문의사항(Q&A) - insert, update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HubQnASaveResult saveQnA(HubQnA qna, HttpServletRequest request) throws Exception {
	    HubQnASaveResult HubQnASaveResult = new HubQnASaveResult();
	    
	    String action = Util.null2str(qna.getAction());
	    
	    MultipartHttpServletRequest multirequest = (MultipartHttpServletRequest) request;
	    
	    List<MultipartFile> uploadFileList = null;
	    Iterator<String> fileNames = multirequest.getFileNames();
	    
	    FileShare fileShare = new FileShare();
	    List<FileShareFile> fileShareList = new ArrayList<FileShareFile>();
	    
	    File uploadFile = null;
	    File deleteFile = null;
	    
	    String filePath = customProperties.getFileQnADir(); 
	    String fileName 	= null;
	    String tempFileName = null;
	    String newReferenceNo = null;
	    
	    int blCount = hubSystemMngMapper.getHblCount(qna);
	    
	    if(blCount == 0 || blCount > 1) {
	    	HubQnASaveResult.setResultCode("notBl");
	    	return HubQnASaveResult;
	    }
	    
		if("insert".equals(action) || "update".equals(action)) {
			// 파일 업로드 START
			if (!FileUtil.isExists(filePath)) {
				FileUtil.createDirectorys(filePath);
			}
			
			// 파일 삭제 여부
			if("Y".equals(Util.null2str(qna.getFileDeleteYn()))) {
				
				deleteFile = new File(filePath + Util.null2str(qna.getFileTempName()));
				if(deleteFile.isFile()) {
					deleteFile.delete();
				}
				
		        qna.setFileContentType("");
		        qna.setFilePath("");
		        qna.setFileTempName("");
		        qna.setFileOriginalName("");
			}
			
			while (fileNames.hasNext()) {
				fileName 		= fileNames.next();
				uploadFileList 	= multirequest.getFiles(fileName);
				log.debug("saveQnA uploadFileList : " + uploadFileList.size());
				
				// 파일 업로드시(기존파일 삭제, 1개의 파일 업로드시 사용)
				deleteFile = new File(filePath + Util.null2str(qna.getFileTempName()));
				if(deleteFile.isFile()) {
					deleteFile.delete();
				}
				
				for (int i = 0, size = uploadFileList.size(); i < size; i++) {
					FileShareFile fileShareFile = new FileShareFile();
					// 파일 확장자 유효성 검사: 문서 + 이미지
					String extension = FilenameUtils.getExtension(uploadFileList.get(i).getOriginalFilename()).toLowerCase();
					if (!extension.matches("xlsx|xls|doc|docx|hwp|hwpx|pdf|jpg|jpeg|bmp|gif|png")) {
			        	HubQnASaveResult.setResultCode("extErrorYn");
			            return HubQnASaveResult;
			        }
					
					tempFileName = "QnA_" + DateTimeUtil.getNow("yyyyMMddHHmmssSSS");
					
					uploadFile = new File(filePath + tempFileName);
					uploadFileList.get(i).transferTo(uploadFile);
					
			        qna.setFileContentType(uploadFileList.get(i).getContentType());
			        qna.setFilePath(filePath);
			        qna.setFileTempName(tempFileName);
			        qna.setFileOriginalName(uploadFileList.get(i).getOriginalFilename());
			        String origName = Util.null2str(uploadFileList.get(i).getOriginalFilename());
			        int dotIdx = origName.indexOf(".");
			        qna.setFileExt(origName.substring(dotIdx));
			        
			        if(i==0) {
						fileShare.setEntCode(Util.null2str(qna.getEntCode()));
						fileShare.setEntBusinessCode(Util.null2str(qna.getEntBusinessCode()));
						fileShare.setFileName(uploadFileList.get(i).getOriginalFilename());
						fileShare.setFileExt(origName.substring(dotIdx));
						fileShare.setFileSize(uploadFileList.get(i).getSize());
						fileShare.setFileNameRemote(tempFileName);
						fileShare.setFilePathRemote(filePath);
			        	fileShare.setGmtTime(qna.getGmtTime());
						fileShare.setUpdateUserId(Util.null2str(qna.getUpdateUserId()));
			        }
			        
			        fileShareFile.setSeqNo(i+1);
			        fileShareFile.setFileDocType("OT");
			        fileShareFile.setFileName(uploadFileList.get(i).getOriginalFilename());
			        fileShareFile.setFileSize(uploadFileList.get(i).getSize());
			        fileShareFile.setFileNameRemote(tempFileName);
			        fileShareFile.setFilePathRemote(filePath);
					
					log.debug("saveQnA filePath : " 		+ uploadFile.getAbsolutePath());
					log.debug("saveQnA fileContentType : " 	+ uploadFileList.get(i).getContentType());
					log.debug("saveQnA fileTempName : " 	+ tempFileName);
					log.debug("saveQnA fileOriginalName : " + uploadFileList.get(i).getOriginalFilename());
					
					fileShareList.add(fileShareFile);
				}
			}
			// 파일 업로드 END
			
			if(fileShareList != null && fileShareList.size() > 0) {
				fileShare.setFileShareList(fileShareList);
			}

		    // DB 저장 처리
		    if ("insert".equals(action)) {
		        hubSystemMngMapper.insertQnA(qna);
		    } else {
		        hubSystemMngMapper.updateQnA(qna);
		    }
		    
		    // 파일 데이터 저장
		    if(!Util.isEmpty(uploadFile)) {
				fileShare.setFuncJobNo(qna.getReferenceNo());
				fileShare.setFuncReferenceNo(Util.null2str(Util.null2int(qna.getSeqNo())));
				hubSystemMngMapper.insertQnafileShare(fileShare);
			}
		}

	    HubQnASaveResult.setReferenceNo(qna.getReferenceNo());

	    return HubQnASaveResult;
	}
	
	/**
	 * eHub 문의사항(Q&A) subGrid - insert, update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HubQnASaveResult saveSubQnA(HubQnA qna, HttpServletRequest request) throws Exception {
		HubQnASaveResult HubQnASaveResult = new HubQnASaveResult();
		
		String action = Util.null2str(qna.getAction());
		
		MultipartHttpServletRequest multirequest = (MultipartHttpServletRequest) request;
		
		List<MultipartFile> uploadFileList = null;
		Iterator<String> fileNames = multirequest.getFileNames();
		FileShare fileShare = new FileShare();
	    List<FileShareFile> fileShareList = new ArrayList<FileShareFile>();
		
		File uploadFile = null;
		File deleteFile = null;
		
		String filePath = customProperties.getFileQnADir(); 
		String fileName 	= null;
		String tempFileName = null;
		
		if("insert".equals(action) || "update".equals(action) || "finish".equals(action)) {
			// 파일 업로드 START
			if (!FileUtil.isExists(filePath)) {
				FileUtil.createDirectorys(filePath);
			}
			
			// 파일 삭제 여부
			if("Y".equals(Util.null2str(qna.getFileDeleteYn()))) {
				
				deleteFile = new File(filePath + Util.null2str(qna.getFileTempName()));
				if(deleteFile.isFile()) {
					deleteFile.delete();
				}
				
				qna.setFileContentType("");
				qna.setFilePath("");
				qna.setFileTempName("");
				qna.setFileOriginalName("");
			}
			
			while (fileNames.hasNext()) {
				
				fileName 		= fileNames.next();
				uploadFileList 	= multirequest.getFiles(fileName);
				log.debug("saveQnA uploadFileList : " + uploadFileList.size());
				
				// 파일 업로드시(기존파일 삭제, 1개의 파일 업로드시 사용)
				deleteFile = new File(filePath + Util.null2str(qna.getFileTempName()));
				if(deleteFile.isFile()) {
					deleteFile.delete();
				}
				
				for (int i = 0, size = uploadFileList.size(); i < size; i++) {
					FileShareFile fileShareFile = new FileShareFile();
					// 파일 확장자 유효성 검사: 문서 + 이미지
					String extension = FilenameUtils.getExtension(uploadFileList.get(i).getOriginalFilename()).toLowerCase();
					if (!extension.matches("xlsx|xls|doc|docx|hwp|hwpx|pdf|jpg|jpeg|bmp|gif|png")) {
						HubQnASaveResult.setResultCode("extErrorYn");
						return HubQnASaveResult;
					}
					
					tempFileName = "QnA_" + DateTimeUtil.getNow("yyyyMMddHHmmssSSS");
					
					uploadFile = new File(filePath + tempFileName);
					uploadFileList.get(i).transferTo(uploadFile);
					
					qna.setFileContentType(uploadFileList.get(i).getContentType());
					qna.setFilePath(filePath);
					qna.setFileTempName(tempFileName);
					qna.setFileOriginalName(uploadFileList.get(i).getOriginalFilename());
					String origName = Util.null2str(uploadFileList.get(i).getOriginalFilename());
			        int dotIdx = origName.indexOf(".");
			        qna.setFileExt(origName.substring(dotIdx));
			        
			        if(i==0) {
						fileShare.setEntCode(Util.null2str(qna.getEntCode()));
						fileShare.setEntBusinessCode(Util.null2str(qna.getEntBusinessCode()));
						fileShare.setFileName(uploadFileList.get(i).getOriginalFilename());
						fileShare.setFileExt(origName.substring(dotIdx));
						fileShare.setFileSize(uploadFileList.get(i).getSize());
						fileShare.setFileNameRemote(tempFileName);
						fileShare.setFilePathRemote(filePath);
			        	fileShare.setGmtTime(qna.getGmtTime());
						fileShare.setUpdateUserId(Util.null2str(qna.getUpdateUserId()));
			        }
			        
			        fileShareFile.setSeqNo(i+1);
			        fileShareFile.setFileDocType("OT");
			        fileShareFile.setFileName(uploadFileList.get(i).getOriginalFilename());
			        fileShareFile.setFileSize(uploadFileList.get(i).getSize());
			        fileShareFile.setFileNameRemote(tempFileName);
			        fileShareFile.setFilePathRemote(filePath);
					
					log.debug("saveQnA filePath : " 		+ uploadFile.getAbsolutePath());
					log.debug("saveQnA fileContentType : " 	+ uploadFileList.get(i).getContentType());
					log.debug("saveQnA fileTempName : " 	+ tempFileName);
					log.debug("saveQnA fileOriginalName : " + uploadFileList.get(i).getOriginalFilename());
					
					fileShareList.add(fileShareFile);
				}
			}
			// 파일 업로드 END
			if(fileShareList != null && fileShareList.size() > 0) {
				fileShare.setFileShareList(fileShareList);
			}
			
			// DB 저장 처리
			if ("insert".equals(action)) {
				hubSystemMngMapper.insertSubQnA(qna);
			} else if("finish".equals(action)) {
				hubSystemMngMapper.updateFinishSubQnA(qna);
			} else {
				hubSystemMngMapper.updateSubQnA(qna);
			}
			
			// 파일 데이터 저장
		    if(!Util.isEmpty(uploadFile)) {
				fileShare.setFuncJobNo(qna.getReferenceNo());
				fileShare.setFuncReferenceNo(Util.null2str(Util.null2int(qna.getSeqNo())));
				hubSystemMngMapper.insertQnafileShare(fileShare);
			}
		}
		
		HubQnASaveResult.setReferenceNo(qna.getReferenceNo());
		HubQnASaveResult.setSeqNo(qna.getSeqNo());
	
		return HubQnASaveResult;
	}
	
	
	/**
	 * eHub 문의사항(Q&A) - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HubQnASaveResult deleteQnA(HubQnADelete HubQnADelete, HttpServletRequest request) throws Exception {
		HubQnASaveResult HubQnASaveResult = new HubQnASaveResult();
		
		if(HubQnADelete == null) {
			return null;
		}
		//조회
		if(!"AD".equals(HubQnADelete.getUserAuth())){
			int detailCnt = hubSystemMngMapper.getDetailCount(HubQnADelete);
			if(detailCnt > 1 ) {
				HubQnASaveResult.setResultCode("N");
				return HubQnASaveResult;
			}
		}
		
		File deleteFile = null;
		String filePath 	= customProperties.getFileQnADir();	// 파일 생성 기본 폴더 : 도입사/사업장/notice/;
		
		deleteFile = new File(filePath + Util.null2str(HubQnADelete.getFileTempName()));
		if(deleteFile.isFile()){
			deleteFile.delete();  
		}
		
		hubSystemMngMapper.deleteQnA(HubQnADelete);
		
		return HubQnASaveResult;
	}
	
	/**
	 * eHub 문의사항(Q&A) - subGrid delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HubQnASaveResult deleteSubQnA(HubQnADelete HubQnADelete, HttpServletRequest request) throws Exception {
		HubQnASaveResult HubQnASaveResult = new HubQnASaveResult();
		
		File deleteFile = null;
		String filePath 	= customProperties.getFileQnADir();	// 파일 생성 기본 폴더 : 도입사/사업장/notice/;
		
		deleteFile = new File(filePath + Util.null2str(HubQnADelete.getFileTempName()));
		if(deleteFile.isFile()){
			deleteFile.delete();  
		}
		
		hubSystemMngMapper.deleteSubQnA(HubQnADelete);
		
		return HubQnASaveResult;
	}
	
	/**
	 * 문의사항(Q&A)  상세조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HubQnA selectQnADetail(SearchHubQnADetail search) throws Exception {
		HubQnA detail  = hubSystemMngMapper.selectQnADetail(search);	
		return detail;
	}
	
	/**
	 * 문의사항(Q&A) subGrid - 상세조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HubQnA selectSubQnADetail(SearchHubQnADetail search) throws Exception {
		HubQnA detail  = hubSystemMngMapper.selectSubQnADetail(search);	
		return detail;
	}


	public HubNoticeList selectHubMainNoticeList(SearchHubMainNoticeList search) {
		List<HubNotice> list  = hubSystemMngMapper.selectHubMainNoticeList(search);
		HubNoticeList HubNoticeList = new HubNoticeList();
		HubNoticeList.setHubNoticeList(list);
		if(list != null && list.size() > 0) {
			HubNoticeList.setTotal(list.get(0).getTotal());
		}else {
			HubNoticeList.setTotal(0);
		}

		return HubNoticeList;
	}
}
