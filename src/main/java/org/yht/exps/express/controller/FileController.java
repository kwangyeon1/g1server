package org.yht.exps.express.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.DownloadFile;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.com.fileshare.FileShare;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.User;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.DateTime;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "File")
@RequestMapping("/{entCode}/express/file")
public class FileController {
	@Autowired
	CustomProperties customProperties;
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@Resource(name = "commonService")
	CommonService commonService;
	
	@Resource(name = "fileService")
	FileService fileService;
	
	@Resource(name = "smsUserService")
	SmsUserService smsUserService;
	
	/**
	 * Excel File Upload
	 * @param country
	 * @param file
	 * @return
	 */
	@ApiOperation(value = "Excel File Upload", notes="File을 업로드 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "entBusinessCode", value = "사업장코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX-KR"),
		@ApiImplicitParam(name = "solution", value = "업무", required = true, dataType = "string", paramType = "path", defaultValue = "ECM"),
		@ApiImplicitParam(name = "userId", value = "등록자ID", required = true, dataType = "string", paramType = "path", defaultValue = "201808-001"),
		@ApiImplicitParam(name = "excelType", value = "문서유형", required = true, dataType = "string", paramType = "path"),
		@ApiImplicitParam(name = "documentCode", value = "문서양식코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX-KR-00001"),
		@ApiImplicitParam(name = "dateFormat", value = "DB 날짜형식", required = true, dataType = "string", paramType = "path", defaultValue = "23"),
		@ApiImplicitParam(name = "gmtTime", value = "GMT Time", required = true, dataType = "string", paramType = "path", defaultValue = "0"),
		@ApiImplicitParam(name = "menuId", value = "해당 엑셀업로드 Format이 속한 MENU ID 값", required = true, dataType = "string", paramType = "path", defaultValue = "ECM-L0034"),
		@ApiImplicitParam(name = "deliveryCode", value = "배송사", required = true, dataType = "string", paramType = "path", defaultValue = "KOREX")
	})
	@RequestMapping(value = "/excel/upload/{entBusinessCode}/{solution}/{userId}/{excelType}/{documentCode}/{dateFormat}/{gmtTime}/{menuId}/{deliveryCode}", method = RequestMethod.POST)
    public FileUploadResponse excelUpload(
    		@PathVariable("entCode") String entCode,
    		@PathVariable("entBusinessCode") String entBusinessCode,
    		@PathVariable("solution") String solution,
    		@PathVariable("userId") String userId,
    		@PathVariable("excelType") String excelType,
    		@PathVariable("documentCode") String documentCode,
    		@PathVariable("dateFormat") String dateFormat,
    		@PathVariable("gmtTime") String gmtTime,
    		@PathVariable("menuId") String menuId,
    		@PathVariable("deliveryCode") String deliveryCode,
    		@RequestParam("file") MultipartFile file,
    		HttpServletRequest request) throws Exception {
		userId = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"), userId);
		FileUploadResponse result = new FileUploadResponse(FileUploadResponse.RESULT_OK);
		long maxSize = 1024 * 1024 * 5; // 5MB
		long size = file.getSize();

		String resultId = "";
		String userName = "";
		String bound 	= "";

		// 파일 사이즈 체크
		if (size > maxSize)	{
			result.setResult(FileUploadResponse.RESULT_SIZE_OVER);
			result.setResultText("The file size exceeds the upload size.");
		} else {
			if (!file.isEmpty()) {
				
//				if(!Util.isEmpty(mblNoIdx)){
//					if(!fileService.mblNoChk(file, mblNoIdx, entCode)){
//						result.setResultText("mblNoNull");
//						return result;
//					}
//				}
				// 파일 확장자 유효성 검사
				if(!fileService.checkExtension(file, new String[]{"xlsx", "xls"})) {
					result.setResult(FileUploadResponse.RESULT_NOT_FILE_TYPE);
					result.setResultText("Invalid file format.");
					return result;
				}
				
				User user = smsUserService.getUser(userId);
				userName = user.getUserName();
				// 파일 저장
				result = fileService.storeFile(file, entCode, true, true);
				if (FileUploadResponse.RESULT_OK.equals(result.getResult())) {
					// 엑셀 업로드 상태 체크를 위해 ElasticSearh에 업로드 내역 저장
					String resultIndex = null;
					if(excelType.startsWith("INVOICE_")){
						resultIndex = "ecm-invoice-excel-upload-result-" + DateTime.getDate("yyyy.MM.dd");
						documentCode += "|" + Util.null2str(request.getParameter("accountName"));
						bound = Util.null2str(request.getParameter("bound"));
					}else{
						resultIndex = "ecm-excel-upload-result-" + DateTime.getDate("yyyy.MM.dd");
					}
					resultId = fileService.setExcelUploadResult(resultIndex, StringUtils.cleanPath(file.getOriginalFilename()), result.getFileName(), entCode, entBusinessCode, userId, user.getUserName(), documentCode, excelType, deliveryCode, bound);
					if (!"".equals(resultId))
						fileService.setExcelInfoInRedis(resultIndex, resultId, StringUtils.cleanPath(file.getOriginalFilename()), result.getFileName(), entCode, entBusinessCode, userId, user.getUserName(), documentCode, dateFormat, gmtTime, menuId, deliveryCode, excelType, bound);
				}
			} else {
				result.setResult(FileUploadResponse.RESULT_NOT_FILE);
				result.setResultText("The file does not exist.");
			}
		}
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(entCode);
		processLogs.setEntBusinessCode(entBusinessCode);
		processLogs.setSolution(solution);
		processLogs.setBound("COM");
		processLogs.setBoundType("COM");
		processLogs.setProcessUserId(userId);
		processLogs.setProcessUserName(userName);
		processLogs.setExcelType(excelType);
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("EXCEL");
		processLogs.setDescription(StringUtils.cleanPath(file.getOriginalFilename()));
		processLogs.setJobLog(new Gson().toJson(result));
		if ("".equals(resultId))	processLogs.setResult("fail");
		else						processLogs.setResult("ok");
		commonService.insertLog(processLogs, request);

		return result;
    }
    
	/**
	 * File Download 기본(List to excel download) 및 data 다운로드
	 * @param fileName
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ApiOperation(value = "File Download", notes="File을 다운로드 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "fileName", value = "파일명", required = true, dataType = "string", paramType = "path", defaultValue = "파일명")
	})
    @RequestMapping(value = "/download/{fileName:.+}", method = RequestMethod.GET)
    public void downloadFile(
    		@PathVariable("entCode") String entCode,
    		@PathVariable String fileName,
    		@QueryStringArgResolver DownloadFile downloadFile,
    		HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> map = Util.getSessionUserInfo(request, jwtTokenProvider);
		if(!Util.isEmpty(map.get("USER_ID"))){
			downloadFile.setUserId(Util.null2str(map.get("USER_ID")));
		}
        // Load file as Resource
		Path fileLocation = Paths.get(customProperties.getExcelDownload()).toAbsolutePath().normalize();
        Path filePath = fileLocation.resolve(fileName).normalize();
        org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
        
		if (downloadFile != null) {
	        if("".equals(Util.null2str(downloadFile.getOriginFileDelete())))	downloadFile.setOriginFileDelete("Y");
	        
	        // Try to determine file's content type
	        String contentType = null;
	        try {
	            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	        } catch (IOException ex) {
	            log.info("Could not determine file type.");
	        }
	        
	        // Fallback to the default content type if type could not be determined
	        if(contentType == null)	contentType = "application/octet-stream";
	        
	        response.setContentType(contentType);
	
	        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + Util.null2str(downloadFile.getOriginFile()).replace("\r", "").replace("\n", "") + "\"");
	        response.setContentLength((int) resource.getFile().length());
	        
	        BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(resource.getFile()));
	        BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream());
	        
	        byte[] buffer = new byte[1024];
	        int bytesRead = 0;
	        while ((bytesRead = inStream.read(buffer)) != -1) {
	        	outStream.write(buffer, 0, bytesRead);
	        }
	        
	        outStream.flush();
	        inStream.close();
	
	        if("Y".equals(downloadFile.getOriginFileDelete()))	resource.getFile().delete();
	        
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(entCode);
			processLogs.setEntBusinessCode(downloadFile.getEntBusinessCode());
			processLogs.setSolution(downloadFile.getSolution());
			processLogs.setBound("COM");
			processLogs.setBoundType("COM");
			processLogs.setProcessUserId(downloadFile.getUserId());
			processLogs.setProcessUserName(Util.null2str(map.get("USER_NAME")));
			processLogs.setProcessUserNameEng(Util.null2str(map.get("USER_NAME_ENG")));
			processLogs.setLogJobType("DOWNLOAD");
			processLogs.setJobFuncType("EXCEL");
			processLogs.setDescription("[" + downloadFile.getPageTitle() + "] excel download");
			
			/** 2025-05-30 No such file or directory 체크위해 임시 추가 */
			processLogs.setExcelType(fileName);
			
			commonService.insertLog(processLogs, request);
			
		} else {
			resource.getFile().delete();
		}
    }
	
	/**
	 * File Download
	 * @param fileName
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ApiOperation(value = "File Download", notes="File을 다운로드 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "fileName", value = "파일명", required = true, dataType = "string", paramType = "path", defaultValue = "파일명")
	})
    @RequestMapping(value = "/downloadTempate/{fileName:.+}", method = RequestMethod.GET)
    public void downloadTempate(
    		@PathVariable("entCode") String entCode,   
    		@PathVariable String fileName,
    		@QueryStringArgResolver DownloadFile downloadFile,
    		HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Util.getSessionUserInfo(request, jwtTokenProvider, downloadFile, "userId", null, null);
        // Load file as Resource
		Path fileLocation;
		if(Util.null2str(downloadFile.getPathType()).equals("backup")){
			String downPath = customProperties.getBackupDir();
			if(!Util.null2str(downloadFile.getAddPath()).equals("")){
				downPath += downloadFile.getAddPath();
			}
			fileLocation = Paths.get(downPath).toAbsolutePath().normalize();
		}else if(Util.null2str(downloadFile.getPathType()).equals("if")) {
			//인터페이스로그조회 화면에서 파일 다운로드 시 전달받은 경로 안에서 찾음
			fileLocation = Paths.get(Util.null2str(downloadFile.getAddPath())).toAbsolutePath().normalize();
		}else {
			fileLocation = Paths.get(customProperties.getExcelTemplate()).toAbsolutePath().normalize();
		}
		
        Path filePath = fileLocation.resolve(fileName).normalize();
        org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
        
		if (downloadFile != null) {
	        if("".equals(Util.null2str(downloadFile.getOriginFileDelete())))	downloadFile.setOriginFileDelete("Y");
	        
	        // Try to determine file's content type
	        String contentType = null;
	        try {
	            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	        } catch (IOException ex) {
	            log.info("Could not determine file type.");
	        }
	        
	        // Fallback to the default content type if type could not be determined
	        if(contentType == null)	contentType = "application/octet-stream";
	        
	        response.setContentType(contentType);
	
	        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFile.getOriginFile().replace("\r", "").replace("\n", "") + "\"");
	        response.setContentLength((int) resource.getFile().length());
	        
	        BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(resource.getFile()));
	        BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream());
	        
	        byte[] buffer = new byte[1024];
	        int bytesRead = 0;
	        while ((bytesRead = inStream.read(buffer)) != -1) {
	        	outStream.write(buffer, 0, bytesRead);
	        }
	        
	        outStream.flush();
	        inStream.close();
	
	        if("Y".equals(downloadFile.getOriginFileDelete()))	resource.getFile().delete();
		} else {
			resource.getFile().delete();
		}
    }
	
	@ApiOperation(value = "file share 팝업 파일 저장", notes="file share 파일 저장합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/fileShare", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> saveFileShareUpdate(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
		FileShare fileShare = new ObjectMapper().readValue(request.getParameter("params"), FileShare.class);
		OrderSaveResult orderSaveResult = fileService.saveFileShareUpdate(fileShare, request);
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
}