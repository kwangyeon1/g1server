package org.yht.exps.express.service.systemMng;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.systemMng.SystemMngMapperVE;
import org.yht.exps.express.model.com.fileshare.FileShare;
import org.yht.exps.express.model.com.fileshare.FileShareFile;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.systemMng.QnADeleteVE;
import org.yht.exps.express.model.systemMng.QnAListVE;
import org.yht.exps.express.model.systemMng.QnASaveResultVE;
import org.yht.exps.express.model.systemMng.QnAVE;
import org.yht.exps.express.model.systemMng.SubQnAListVE;
import org.yht.exps.express.model.systemMng.search.SearchQnADetailVE;
import org.yht.exps.express.model.systemMng.search.SearchQnAListVE;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class SystemMngServiceVE {
	
	@Autowired
	CustomProperties customProperties;            
	
	@Autowired
	SystemMngMapperVE systemMngMapper;
	
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	@Autowired
	CommonMapper commonMapper;
	
	
	/**
	 * 문의사항(Q&A) 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public QnAListVE selectQnAList(SearchQnAListVE search) throws Exception {
		QnAListVE QnAListVE = new QnAListVE();
		
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
 		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
 	
		if(search.getEcmCustomerCode() != null && search.getEcmCustomerCode() != "") {
			List<QnAVE> srchCustomerCodeList = systemMngMapper.selectCustomerCodeList(search);
			
			List<String> customerCodeList = new ArrayList<>();
			for (QnAVE QnAVE : srchCustomerCodeList) {
				customerCodeList.add(QnAVE.getCustomerCode());
			}

	        search.setSrchCustomerCodeList(customerCodeList);
		}
		
		List<QnAVE> list  = systemMngMapper.selectQnAList(search);
		QnAListVE.setQnAList(list);
		if(list != null && list.size() > 0) {
			QnAListVE.setTotal(list.get(0).getTotal());
		}else {
			QnAListVE.setTotal(0);
		}
		
		return QnAListVE;
	}
	
	/**
	 * eHub문의사항(Q&A) subGrid 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public SubQnAListVE selectSubQnAList(SearchQnAListVE search) throws Exception {
		SubQnAListVE SubQnAListVE = new SubQnAListVE();
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		List<QnAVE> list  = systemMngMapper.selectSubQnAList(search);
		SearchQnADetailVE title = new SearchQnADetailVE();
		title.setEntCode(search.getEntCode());
		title.setReferenceNo(search.getReferenceNo());
		QnAVE detail = systemMngMapper.selectQnADetail(title);
		
		SubQnAListVE.setQnAVE(detail);
		SubQnAListVE.setSubQnAList(list);
		
		if(list != null && list.size() > 0) {
			SubQnAListVE.setTotal(list.get(0).getTotal());
		}else {
			SubQnAListVE.setTotal(0);
		}
		
		return SubQnAListVE;
	}
	

	/**
	 * 문의사항(Q&A) - insert, update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public QnASaveResultVE saveQnA(QnAVE qna, HttpServletRequest request) throws Exception {
		QnASaveResultVE QnASaveResultVE = new QnASaveResultVE();

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
		
				log.debug("saveQnA fileName : " + fileName);
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
						QnASaveResultVE.setResultCode("extErrorYn");
						return QnASaveResultVE;
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
	
			if(fileShareList != null && fileShareList.size() > 0) {
				fileShare.setFileShareList(fileShareList);
			}
			
			//반송&송장교체시 멀티생성시(오더 단건 처리) 새로운 seqNoId를 연결하기 위해 먼저 처리
			if("Q3".equals(qna.getQuestionType())||"Q5".equals(qna.getQuestionType())) {
				Map<String, Object> multiParams = new HashMap<String, Object>();
				Map<String, Object> orderInfo = systemMngMapper.getOrderInfo(qna);
				if(orderInfo != null) {
					String deliveryNoChanged = "";
					String deliveryCode = Util.null2str(orderInfo.get("DELIVERY_CODE"));
					if("Q5".equals(qna.getQuestionType()) && "KOREX".equals(deliveryCode)) {
						Map<String, Object> data = new HashMap<String, Object>();
						String agentCode = Util.null2str(orderInfo.get("CUST_CODE_LV2"), Util.null2str(orderInfo.get("CUST_CODE_LV1")));
						data.put("deliveryCode", deliveryCode); 
						data.put("bound", deliveryCode); 
						data.put("boundType", deliveryCode); 
						data.put("agentCode", agentCode);
						Map<String, Object> hblStockMap = commonMapper.selectHblNoStock(data);
						deliveryNoChanged = Util.null2str(hblStockMap.get("CURRENT_NO"));
					}
				
					multiParams.put("odJobNo", Util.null2str(qna.getOdJobNo()));
					multiParams.put("deliveryNo", Util.null2str(qna.getHblNo()));
					multiParams.put("pieces", orderInfo.get("PIECES"));
					multiParams.put("updateUserId", Util.null2str(qna.getUpdateUserId()));
					multiParams.put("gmtTime", qna.getGmtTime());
					
					Util.accessControlDatabase(multiParams, "gmtTime", false, null, "", null, "");
					if("Q3".equals(qna.getQuestionType())) {
						Map<String, Object> orderReturnInfo = systemMngMapper.getOrderReturnInfo(qna);
						if(orderReturnInfo != null) {//이미 반품요청한 데이터인경우 문의 등록불가처리
							QnASaveResultVE.setResultCode("A11253");
							return QnASaveResultVE;
						}
						
						multiParams.put("return", "return");
						if("KOREX".equals(deliveryCode)) multiParams.put("returnDeliveryCode", deliveryCode);
						
						systemMngMapper.updateOrderReturnInfo(multiParams);
						qna.setSeqNoId(Util.null2int(qna.getSeqNoId())+1);//반품의 경우 무조건 멀티데이터 추가해야한다.
					}else if("Q5".equals(qna.getQuestionType())) {
						multiParams.put("deliveryCode", deliveryCode);
						multiParams.put("deliveryNoChanged", deliveryNoChanged);
						
						if(Util.null2int(orderInfo.get("MULTI_SEQ_NO_ID")) == 0) qna.setSeqNoId(1);//송장교체할때 단건오더인경우 멀티데이터 추가하기
					}
					
					multiParams.put("seqNoId", qna.getSeqNoId());
					systemMngMapper.insertMultiInfo(multiParams);
					systemMngMapper.updateOrderMultiInfo(multiParams);
				}else {
					QnASaveResultVE.setResultCode("A11397");
					return QnASaveResultVE;
				}
			}
			
			if("Q3".equals(qna.getQuestionType())) {//반송반품
				systemMngMapper.updateResendInfo(qna);
			}else if("Q5".equals(qna.getQuestionType())) {//송장교체추가
				//서브그리드가아닌 최초생성시에만 오더에도 정보를 반영
				//배송사이름추가로직
			//	qna.setOdUpdateChk("Y");
			//	Map<String,Object>chkDeliveryNameMap = new HashMap<>();
			//	chkDeliveryNameMap.put("odJobNo", qna.getOdJobNo());
				//멀티가 속한 오더의 배송사코드의 이름을 적용
//				qna.setDeliveryCodeNameChanged(systemMngMapper.selectDeliveryName(chkDeliveryNameMap));
				systemMngMapper.updateDlvChangeInfo(qna);
			}else if("Q6".equals(qna.getQuestionType())) {//퀵오더추가
				systemMngMapper.updateQuickInfo(qna);
			}

			//CS히스토리추가
			systemMngMapper.insertCsHistory(qna);

			// DB 저장 처리
			if ("insert".equals(action)) {
				systemMngMapper.insertQnA(qna);
			} else {
				systemMngMapper.updateQnA(qna);
			}
	
			// 파일 데이터 저장
			if(!Util.isEmpty(uploadFile)) {
				fileShare.setFuncJobNo(qna.getReferenceNo());
				fileShare.setFuncReferenceNo(Util.null2str(Util.null2int(qna.getSeqNo())));
				systemMngMapper.insertQnafileShare(fileShare);
			}
		}
		
		QnASaveResultVE.setReferenceNo(qna.getReferenceNo());
		return QnASaveResultVE;
	}
	
	/**
	 * 문의사항(Q&A) subGrid - insert, update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public QnASaveResultVE saveSubQnA(QnAVE qna, HttpServletRequest request) throws Exception {
		QnASaveResultVE QnASaveResultVE = new QnASaveResultVE();
		
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
				
				log.debug("saveQnA fileName : " + fileName);
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
			        	QnASaveResultVE.setResultCode("extErrorYn");
			            return QnASaveResultVE;
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
				systemMngMapper.insertSubQnA(qna);
			} else if("finish".equals(action)) {
				systemMngMapper.updateFinishSubQnA(qna);
			} else {
				systemMngMapper.updateSubQnA(qna);
			}
			
			if("Q3".equals(qna.getQuestionType())) {//반송반품
				systemMngMapper.updateResendInfo(qna);
			}else if("Q5".equals(qna.getQuestionType()) && "Y".equals(Util.null2str(qna.getDlvChangeYn()))) {//송장교체추가(DlvChangeYn:N >> 일반문의처리)
		    	systemMngMapper.updateDlvChangeInfo(qna);
		    }else if("Q6".equals(qna.getQuestionType())) {//퀵오더추가
		    	systemMngMapper.updateQuickInfo(qna);
		    }
		    //CS히스토리추가
		    systemMngMapper.insertCsHistory(qna);
		    
		    // 파일 데이터 저장
		    if(!Util.isEmpty(uploadFile)) {
				fileShare.setFuncJobNo(qna.getReferenceNo());
				fileShare.setFuncReferenceNo(Util.null2str(Util.null2int(qna.getSeqNo())));
				systemMngMapper.insertQnafileShare(fileShare);
			}
		}
		
		QnASaveResultVE.setReferenceNo(qna.getReferenceNo());
		QnASaveResultVE.setSeqNo(qna.getSeqNo());
	
		return QnASaveResultVE;
	}
	
	
	/**
	 * 문의사항(Q&A) - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public QnASaveResultVE deleteQnA(QnADeleteVE QnADeleteVE, HttpServletRequest request) throws Exception {
		QnASaveResultVE QnASaveResultVE = new QnASaveResultVE();
		
		if(QnADeleteVE == null) {
			return null;
		}
		//조회
		if(!"AD".equals(QnADeleteVE.getUserAuth())){
			int detailCnt = systemMngMapper.getDetailCount(QnADeleteVE);
			if(detailCnt > 0 ) {
				QnASaveResultVE.setResultCode("N");
				return QnASaveResultVE;
			}
		}
		
		File deleteFile = null;
		String filePath 	= customProperties.getFileQnADir();	// 파일 생성 기본 폴더 : 도입사/사업장/notice/;
		
		deleteFile = new File(filePath + Util.null2str(QnADeleteVE.getFileTempName()));
		if(deleteFile.isFile()){
			deleteFile.delete();  
		}
		
		systemMngMapper.deleteQnAFileShare(QnADeleteVE);
		systemMngMapper.deleteQnA(QnADeleteVE);
		
		return QnASaveResultVE;
	}
	
	/**
	 * 문의사항(Q&A) - subGrid delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public QnASaveResultVE deleteSubQnA(QnADeleteVE QnADeleteVE, HttpServletRequest request) throws Exception {
		QnASaveResultVE QnASaveResultVE = new QnASaveResultVE();
		
		File deleteFile = null;
		String filePath 	= customProperties.getFileQnADir();	// 파일 생성 기본 폴더 : 도입사/사업장/notice/;
		
		deleteFile = new File(filePath + Util.null2str(QnADeleteVE.getFileTempName()));
		if(deleteFile.isFile()){
			deleteFile.delete();  
		}
		
		systemMngMapper.deleteSubQnA(QnADeleteVE);
		
		return QnASaveResultVE;
	}
	
	/**
	 * 문의사항(Q&A)  상세조회
	 * @param QnAVE
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public QnAVE selectQnADetail(SearchQnADetailVE search) throws Exception {
		QnAVE detail  = systemMngMapper.selectQnADetail(search);	
		return detail;
	}
	
	/**
	 * 문의사항(Q&A) subGrid - 상세조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public QnAVE selectSubQnADetail(SearchQnADetailVE search) throws Exception {
		QnAVE detail  = systemMngMapper.selectSubQnADetail(search);	
		return detail;
	}

	public List<Map<String, Object>> selectQnAHistoryList(SearchQnAListVE search) throws Exception {
		if("srchMultiNo".equals(Util.null2str(search.getSrchNoType())) && "".equals(Util.null2str(search.getSrchOdJobNo()))) {
			Map<String, Object> selectOdInfo = systemMngMapper.selectOdInfo(search);
			
			search.setSrchOdJobNo(Util.null2str(selectOdInfo.get("OD_JOB_NO")));
			search.setSeqNo(Util.null2int(selectOdInfo.get("SEQ_NO_ID")));
			search.setSrchNo("");
		}
		List<Map<String, Object>> list = systemMngMapper.selectQnAHistoryList(search);
		return list;
	}
}
