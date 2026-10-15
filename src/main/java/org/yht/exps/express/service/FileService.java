package org.yht.exps.express.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.FileMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.com.fileshare.FileShare;
import org.yht.exps.express.model.com.fileshare.FileShareFile;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadResult;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.util.Converter;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.ExcelSheetHandler;
import org.yht.util.FileUtil;
import org.yht.util.Util;
import org.yht.util.exception.FileDownloadException;
import org.yht.util.exception.FileUploadException;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Service
@Transactional
public class FileService {
	
	@Autowired
	StringRedisTemplate redisTemplate;
	
	@Autowired
	RestHighLevelClient restClient;
	
	@Autowired
	FileMapper fileMapper;
	
	private Path fileLocation = null;
	private Path excelFileLocation = null;
    
    @Autowired
    public FileService(CustomProperties prop) {
        this.fileLocation = Paths.get(prop.getFileUploadDir()).toAbsolutePath().normalize();
        this.excelFileLocation = Paths.get(prop.getInterfaceDir()).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileLocation);
        } catch(Exception e) {
            throw new FileUploadException("파일을 업로드할 디렉토리를 생성하지 못했습니다.", e);
        }
    }
    
    public FileUploadResponse storeFile(MultipartFile file, String entCode, boolean excelUpload, boolean daemonExcel) {
    	String result = FileUploadResponse.RESULT_OK;
    	String resultText = null;
        String originFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName;
        if(daemonExcel){
        	fileName = DateTime.getDate("yyyyMMddhhmmssSSS") + Util.getRandomNum(5) + "." + FilenameUtils.getExtension(originFileName);
		}else{
			fileName = DateTime.getDate("yyyyMMddhhmmssSSS") + "-" + originFileName.substring(0, originFileName.lastIndexOf(".")) + "_" + Util.getRandomNum(8) + originFileName.substring(originFileName.lastIndexOf("."));
		}
        try {
            // 파일명에 부적합 문자가 있는지 확인한다.
            if(originFileName.contains(".."))	{
            	result = FileUploadResponse.RESULT_FAIL;
            	resultText = "File name contains invalid characters.";
            	String logVal = "파일명에 부적합 문자가 포함되어 있습니다. " + originFileName;
            	log.error(logVal);
        	} else {
        		if(excelUpload){
        			Path filePath = this.excelFileLocation.resolve("excel/" + entCode).normalize();
        			Resource resource = new UrlResource(filePath.toUri());
        			
        			// 도입사 디렉토리가 존재하지 않을 경우 생성
//        			if(!resource.exists()) {
//        				Files.createDirectory(Paths.get(this.excelFileLocation.toString() + "/excel/" + entCode).toAbsolutePath().normalize());
//        			}
        			File pathDir = new File(this.excelFileLocation.toString() + "/excel/" + entCode);
    				if(!pathDir.exists()){
    					pathDir.mkdirs();
    				}
        			
        			Path targetLocation = this.excelFileLocation.resolve("excel/" + entCode + "/" + fileName);
        			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        		}else{
        			Path filePath = this.fileLocation.resolve(entCode).normalize();
        			Resource resource = new UrlResource(filePath.toUri());
        			// 도입사 디렉토리가 존재하지 않을 경우 생성
        			if(!resource.exists()) {
        				Files.createDirectory(Paths.get(this.fileLocation.toString() + "/" + entCode).toAbsolutePath().normalize());
        			}
        			Path targetLocation = this.fileLocation.resolve(entCode + "/" + fileName);
        			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        		}
            }
            
        } catch(Exception e) {
        	result = FileUploadResponse.RESULT_FAIL;
        	resultText = "File upload failed. Please try again.";
        	String logVal = "["+originFileName+"] 파일 업로드에 실패하였습니다. 다시 시도하십시오.";
        	log.error(logVal, e);
        }
        
        return new FileUploadResponse(fileName, null, file.getContentType(), file.getSize(), result, resultText);
    }
    public FileUploadResponse storeFile(MultipartFile file, String entCode) {
    	return storeFile(file, entCode, false, false);
    }
    public FileUploadResponse storeFile(MultipartFile file, String entCode, boolean excelUpload) {
    	return storeFile(file, entCode, excelUpload, false);
    }
    
    public Resource loadFileAsResource(String fileName, String entCode, boolean excelUpload) {
        try {
        	Path filePath;
        	if(excelUpload){
        		filePath = this.excelFileLocation.resolve("excel/" + entCode + "/" + fileName).normalize();
        	}else{
        		filePath = this.fileLocation.resolve(entCode + "/" + fileName).normalize();
        	}
            Resource resource = new UrlResource(filePath.toUri());
            
            if(resource.exists()) {
                return resource;
            }else {
                throw new FileDownloadException(fileName + " 파일을 찾을 수 없습니다.");
            }
        } catch(MalformedURLException e) {
            throw new FileDownloadException(fileName + " 파일을 찾을 수 없습니다.", e);
        }
    }
    public Resource loadFileAsResource(String fileName, String entCode) {
    	return loadFileAsResource(fileName, entCode, false);
    }

    /**
     * 업로드한 엑셀정보를 Redis에 임시 저장.
     * @param resultId
     * @param originFileName
     * @param fileName
     * @param entCode
     * @param entBusinessCode
     * @param userId
     * @param userName
     * @param documentCode
     * @param menuId
     */
    public void setExcelInfoInRedis(String resultIndex, String resultId, String originFileName, 
    		String fileName, String entCode, String entBusinessCode, String userId, String userName, 
    		String documentCode, String dateFormat, String gmtTime, String menuId, String deliveryCode, String excelType, String bound) {
    	String key = null;
		JSONObject json = new JSONObject();
    	if(excelType.startsWith("INVOICE_")){
    		key = "E-COMMERCE_INVOICE_UPLOAD_EXCEL_" + entBusinessCode;
    	}else{
    		key = "E-COMMERCE_UPLOAD_EXCEL_" + entBusinessCode;
			json.put("deliveryCode", deliveryCode);
    	}
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        
        json.put("entCode", entCode);
        json.put("entBusinessCode", entBusinessCode);
        json.put("userId", userId);
        json.put("documentCode", documentCode);
        json.put("fileName", fileName);
        json.put("resultIndex", resultIndex);
        json.put("resultId", resultId);
        json.put("dateFormat", dateFormat);
        json.put("gmtTime", gmtTime);
        json.put("menuId", menuId);
        json.put("excelType", excelType);
        json.put("bound", bound);
        
        listOperations.rightPush(key, json.toString());
    }
    
    public String setExcelUploadResult(String index, String originFileName, String fileName, String entCode, String entBusinessCode, String userId, String userName, String documentCode, String excelType, String deliveryCode, String bound) {
		String id = DateTime.getDate("yyyyMMddhhmmssSSS") + "-" + Util.null2str(entBusinessCode);
		ExcelUploadResult excelUploadResult = new ExcelUploadResult();
		
		// 처리 결과 DB에 저장
		excelUploadResult.setEntCode(entCode);
		excelUploadResult.setEntBusinessCode(entBusinessCode);
		excelUploadResult.setSolution("ECM");
		excelUploadResult.setStatus("wait");
		excelUploadResult.setDocumentCode(documentCode);
		excelUploadResult.setFileName(fileName);
		excelUploadResult.setOriginFileName(originFileName);
		excelUploadResult.setRegisterUserId(userId);
		excelUploadResult.setRegisterDateTime(Util.null2str(DateUtils.nowWithMillisResolution()));

		excelUploadResult.setRegisterUserName(userName);
		excelUploadResult.setExcelType(excelType);
		// 오더 엑셀 업로드
		if(excelType.startsWith("ORDER")){
			excelUploadResult.setDeliveryCode(deliveryCode);
		// 정산 엑셀 업로드
		}else if(excelType.startsWith("INVOICE_")) {
			excelUploadResult.setBound(bound);
		}
		
		// 인덱스 생성
		try {
			IndexApi indexApi = new IndexApi(restClient);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			DocumentApi documentApi = new DocumentApi(restClient);
			excelUploadResult.setReferenceNoId(id);
			documentApi.createDocument(index, id, Converter.domainToMap(excelUploadResult));
		} catch (Exception e) {
			log.error("오류가 발생 했습니다.", e);
			id = "";
		}
		
		return id;
    }
    
    public boolean mblNoChk(MultipartFile multipartFile, String mblNoIdx, String entCode) {
    	String mblNo = "";
    	FileUploadResponse result = this.storeFile(multipartFile, entCode);
    	String filePath = this.fileLocation.toString() + "/" + entCode + "/" + result.getFileName();
    	File file = new File(filePath);
    	try {
    		String fileName = multipartFile.getOriginalFilename();
	    	String extension = FilenameUtils.getExtension(fileName);
	    	int mblNoCol = Util.null2int(mblNoIdx.split("\\|")[0]);
	    	int mblNoRow = Util.null2int(mblNoIdx.split("\\|")[1]);
	    	
	    	// xls 사용
			HSSFWorkbook wb = null;
			HSSFSheet sheet = null;
			HSSFCell myCell = null;
			// xlsx 사용
			ExcelSheetHandler excelSheetHandler = null;
			List<List<String>> excelDatas = null;
			DataFormatter df = new DataFormatter();
			
	    	if ("xls".equals(extension)) {
				InputStream ExcelFileToRead;
				ExcelFileToRead = new FileInputStream(filePath);
				wb = new HSSFWorkbook(ExcelFileToRead);
				sheet = wb.getSheetAt(0);
				
			}else { //"xlsx"
				excelSheetHandler = ExcelSheetHandler.readExcel(file);
				excelDatas = excelSheetHandler.getRows();
			}
	    	
	    	if ("xls".equals(extension)) {
	    		if(sheet.getRow(mblNoRow) != null){
	    			myCell = sheet.getRow(mblNoRow).getCell(mblNoCol, MissingCellPolicy.CREATE_NULL_AS_BLANK);
	    			if(myCell != null){
	    				mblNo = Util.null2str(df.formatCellValue(myCell));
	    			}else{
	    				mblNo = "";
	    			}
	    		}else{
	    			mblNo = "";
	    		}
				
			}else if ("xlsx".equals(extension)) {
				try {
					if(excelDatas.get(mblNoRow) != null || excelDatas.get(mblNoRow).get(mblNoCol) != null){
						mblNo = excelDatas.get(mblNoRow).get(mblNoCol);
					}else{
						mblNo = "";
					}
				} catch (IndexOutOfBoundsException e) {  
					// 엑셀에서 row의 마지막 cell 가져올때 공백이면 리스트에 넣지 않아서 exception 발생 >> "" 값으로 처리 (중간cell은 ""으로 들어감)
					mblNo = "";
				}
			}
    	} catch (Exception e) {
    		log.error(Util.getPrintStacTraceString(e));
			mblNo = "";
		}
    	file.delete();
    	if(Util.isEmpty(mblNo)){
    		return false;
    	}
    	
    	return true;
    }
    
    public boolean checkExtension(MultipartFile file, String[] extArr) {
    	String chkExt = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
    	int cnt = 0;
    	for(String ext : extArr) {
    		if(chkExt.equals(ext.toLowerCase())) {
    			cnt++;
    		}
    	}
    	if(cnt == 0) {
    		return false;
    	}else{
    		return true;
    	}
    }

	public OrderSaveResult saveFileShareUpdate(FileShare fileShare, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		MultipartHttpServletRequest multirequest = (MultipartHttpServletRequest) request;
	    List<MultipartFile> uploadFileList = null;
	    Iterator<String> fileNames = multirequest.getFileNames();
	    List<FileShareFile> fileShareList = new ArrayList<FileShareFile>();
	    
	    File uploadFile = null;
	    File deleteFile = null;
	    
	    String filePath = fileLocation.toString(); 
	    String fileName 	= null;
	    String tempFileName = null;

		boolean fileUploadCheck = fileShare.isFileUploadCheck();
	    
		try {
			if(fileUploadCheck) {
				// 파일 업로드 START
				if (!FileUtil.isExists(filePath)) {
					FileUtil.createDirectorys(filePath);
				}
				
				// 파일 삭제 여부
				if("Y".equals(Util.null2str(fileShare.getRemoveFileYn()))) {
					if("Y".equals(Util.null2str(fileShare.getMultipleYn()))) {
						for(FileShareFile fileTemp : fileShare.getFileShareList()) {
							String fileNameRemote = Util.null2str(fileTemp.getFileNameRemote());
							deleteFile = new File(filePath + "/" + Util.null2str(fileNameRemote));
							if(deleteFile.isFile()) {
								deleteFile.delete();
							}
							
							fileMapper.deleteFileShareFile(fileTemp);
						}
					}else {
						FileShareFile fileTemp = new FileShareFile();
						String fileNameRemote = Util.null2str(fileShare.getFileNameRemote());
						deleteFile = new File(filePath + "/" + Util.null2str(fileNameRemote));
						if(deleteFile.isFile()) {
							deleteFile.delete();
						}
						
						fileTemp.setReferenceNoId(fileShare.getReferenceNoId());
						fileTemp.setSeqNo(1);
						fileMapper.deleteFileShareFile(fileTemp);
					}
				}
				
				String referenceNoId = Util.null2str(fileShare.getReferenceNoId());
				String fileShareYn = "N";
				int maxFileSeqNo = 0;
				
				if(!"".equals(referenceNoId) && !"0".equals(referenceNoId)) {
					Map<String, Object> fileShareInfo = fileMapper.selectFileShare(fileShare);
					if(fileShareInfo != null) {
						if(Util.null2int(fileShareInfo.get("FILE_SHARE_COUNT")) == 0) {
							fileMapper.updateFileShare(fileShare);
						}else {
							maxFileSeqNo = Util.null2int(fileShareInfo.get("MAX_SEQ_NO"));
							fileShareYn = "Y";
						}
					}
				}

				while (fileNames.hasNext()) {
					fileName 		= fileNames.next();
					uploadFileList 	= multirequest.getFiles(fileName);
					
					for (int i = 0, size = uploadFileList.size(); i < size; i++) {
						FileShareFile fileShareFile = new FileShareFile();
						// 파일 확장자 유효성 검사: 문서 + 이미지
						String extension = FilenameUtils.getExtension(uploadFileList.get(i).getOriginalFilename()).toLowerCase();
						if (!extension.matches(Util.null2str(fileShare.getFileExtension()))) {
				        	orderSaveResult.setResultCode("extErrorYn");
				            return orderSaveResult;
				        }
						
						tempFileName = Util.null2str(fileShare.getEntBusinessCode()) + "-SHR-" + DateTimeUtil.getNow("yyyyMMddHHmmssSSS");
						
						uploadFile = new File(filePath + "/" + tempFileName);
						uploadFileList.get(i).transferTo(uploadFile);
						
						if(i==0) {
							fileShare.setFileName(uploadFileList.get(i).getOriginalFilename());
							fileShare.setFileNameRemote(tempFileName);
							fileShare.setFilePathRemote(filePath);
							fileShare.setRegisterUserId(Util.null2str(fileShare.getUpdateUserId()));
							
							if("Y".equals(fileShareYn)) fileShare.setReferenceNoId(Util.null2int(referenceNoId));
						}
						
						fileShareFile.setSeqNo(++maxFileSeqNo);
						fileShareFile.setFileDocType(fileShare.getFileDocType());
						fileShareFile.setFileName(uploadFileList.get(i).getOriginalFilename());
						fileShareFile.setFileSize(uploadFileList.get(i).getSize());
						fileShareFile.setFileNameRemote(tempFileName);
						fileShareFile.setFilePathRemote(filePath);

						fileShareList.add(fileShareFile);
					}
				}
				
				if(fileShareList != null && fileShareList.size() > 0) {
					fileShare.setFileShareList(fileShareList);
					
					if("N".equals(fileShareYn)) fileMapper.insertFileShare(fileShare);
					else fileMapper.insertFileShareFile(fileShare);
				}
			}else {
				fileMapper.updateFileShare(fileShare);
			}
		}catch(Exception e) {
			log.error(e.getMessage());
			orderSaveResult.setResultCode("A00001");
		}
		
		return orderSaveResult;
	}
}
