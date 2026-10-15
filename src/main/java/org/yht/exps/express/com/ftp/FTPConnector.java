package org.yht.exps.express.com.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.util.DateTime;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;


/**
 * ftp서버 접속하여 파일을 업로드/다운로드.
 *
 */
@Slf4j
public class FTPConnector {
	
	@Autowired
	private CustomProperties customProperties;
	
	protected static final Log logger = LogFactory.getLog(FTPConnector.class);
    
    private org.apache.commons.net.ftp.FTPClient ftp = null;
    
    public FTPConnector() {
    	//FTPConnector
    }
    
    public FTPClient getFtpInfo(){
    	return ftp;
    }
    
    
//    /**
//     * FTP Connect
//     * @param port
//     * @param ip
//     * @param id
//     * @param password
//     */
//    public boolean connectFTP(int port, String ip, String id, String password, String passiveMode) {
//        int reply;
//        boolean result = false;
//        
//        try {
//            this.ftp = new FTPClient();
//            this.ftp.connect( ip, port );
//            
//            logger.debug("Connected to " + ip + " on "+ftp.getRemotePort());
//            
//            reply = ftp.getReplyCode();
//            if ( !FTPReply.isPositiveCompletion(reply) ) {
//                ftp.disconnect();
//                logger.error("FTP server refused connection.");
//                System.exit(1);
//            }
//            if( !ftp.login(id, password) ) {
//                ftp.logout();
//                throw new Exception("ftp 서버에 로그인하지 못했습니다.");
//            }
//            
//            ftp.setFileType(FTP.BINARY_FILE_TYPE);
//            if("Y".equals(passiveMode)){
//            	logger.info("Enter Passive Mode...");
//            	ftp.enterLocalPassiveMode();
//            }            
//            
//            logger.info(ftp.printWorkingDirectory());
//        }catch(Exception e) {
//            logger.error(Util.getPrintStacTraceString(e));
//            result = true;
//        }
//        
//        return result;
//    }
//    
    /**
     * FTP Disconnect
     */
    public void disconnectFTP() {
        try {
            this.ftp.logout();
        }catch(Exception e) {
        	logger.error(Util.getPrintStacTraceString(e));
        }finally {
            if( ftp != null && ftp.isConnected() ) {
                try { ftp.disconnect(); }catch (IOException e) {logger.error(Util.getPrintStacTraceString(e));}
            }
        }
    }
//    
//    /**
//     * Send file
//     * @param folder		ftp서버에 생성할 폴더
//     * @param files			ftp서버에 업로드할 파일 목록
//     * @return
//     */
//    public boolean sendFtpServer( String folder, ArrayList<String> files ) {
//        boolean isSuccess = false;
//        //FTP 서버에 업로드할 파일 경로
//        String localPath = 	customProperties.getFileshare_upload_dir();
//        						
//        logger.debug("folder : " +folder);
//        
//        for(int i = 0; i < files.size(); i++){
//        	logger.debug("files : " + files.get(i));
//        }
//        
//        try {
//            String[] folders = folder.split("/");
//            String folder_str = "/";
//            
//            try {
//                for(int i=0; i<folders.length; i++) {
//                	logger.debug("folders : " + folders[i]);
//                    folder_str += folders[i] + "/";
//                    logger.debug("folder_str : " + folder_str);
//                }
//            }catch( Exception e ) {
//                logger.error(Util.getPrintStacTraceString(e));
//            }
//            
//            logger.info(ftp.printWorkingDirectory());
//            
//            File uploadFile =  null;
//            FileInputStream fis = null;
//            
//            String tempFileName = "";
//            String sourceFile = "";
//            for( int i = 0; i < files.size(); i++ ) {
//                
//            	//ftp서버에 한글파일을 쓸때 한글깨짐 방지
//                tempFileName = new String(files.get(i).getBytes("utf-8"),"iso_8859_1");
//                sourceFile = localPath + files.get(i);        
//                
//                uploadFile = new File(sourceFile);
//                fis = null;
//                
//                try {
//                    fis = new FileInputStream(uploadFile);
//                    logger.debug("대상파일 : " + sourceFile + " : 전송시작 = >");
//                    
//                    logger.debug(ftp.printWorkingDirectory());
//                    
//                     //tempFileName 업로드 될 타겟의 풀 경로가 들어가야 함.  ex) /A/A01/A001/aa.zip   파일명만 들어갈경우 unix에서 전송 실패하는 경우가 생김.
//                    isSuccess = ftp.storeFile(tempFileName, fis);
//                    
//                    logger.debug("업르도 파일 : " + tempFileName + " : 전송결과 = >" + isSuccess);
//                    
//                }catch(IOException e) {
//                    logger.error(Util.getPrintStacTraceString(e));
//                    isSuccess = false;
//                }finally {
//                    if( fis != null ) {
//                        try { fis.close(); } catch(IOException e) {}
//                    }
//                    
//                    if(isSuccess) {
//                    	uploadFile.delete();
//                    	logger.debug("Original File Deleted");
//                    }
//                }
//            }
//            
//        }catch (Exception e) {
//            logger.error(Util.getPrintStacTraceString(e));
//        }
//        
//        return isSuccess;
//    }
//    
//    /**
//     * Exists file
//     * @param filePath		FTP서버 파일 경로 "/"가 없는 경우 "/"를 임의로 붙인다
//     * @param fileName		파일 명
//     * @param request
//     * @return
//     * @throws Exception
//     */
//    public boolean existsFile(String filePath, String fileName) throws Exception {
//        boolean isFileCheck = false;
//    	try {
//    		
//    		if(!filePath.startsWith("/")){
//    			logger.debug(filePath);
//    			filePath = "/" + filePath;
//    			
//    		}
//    		if(!filePath.endsWith("/")){
//    			logger.debug(filePath);
//    			filePath = filePath + "/";
//    			
//    		}
//    		logger.debug(filePath);
//    		FTPFile[] fInfo =  ftp.listFiles(filePath + fileName);             
//            /*logger.debug("파일 목록을 확인하기 위해서 추가해놓음. 확인이 끝나면 해당 함수 주석 처리 할것");
//            for(int i = 0; i < fInfo.length; i++){
//             logger.debug(fInfo[i].getName());
//            }*/             
//            logger.debug("FILE COUNT : " + fInfo.length + " ");
//            if(fInfo.length > 0 ) isFileCheck = true;
//             
//        }catch (Exception e) {
//            logger.error(Util.getPrintStacTraceString(e)); 
//        }finally{ 
//        	
//        }
//        
//        logger.info("existsFile - END " + isFileCheck + " " + ftp.printWorkingDirectory());
//        return isFileCheck;
//    }
//    
//    public int downloadFtpFile(String filePath, String fileName, HttpServletRequest request) throws Exception {
//    	return downloadFtpFile(filePath, fileName, request, null);
//    }
//    
    /**
     * Download file
     * @param filePath		Download할 FTP서버 파일 경로
     * @param fileName		Download할 파일 명
     * @param request
     * @return
     * @throws Exception
     */
    public int downloadFtpFile(String filePath, String fileName, HttpServletRequest request, String downloadPath) throws Exception {
        
        BufferedOutputStream bos = null;
        File fPath  = null;
        File fDir = null;
        File f   = null; 
        
        int result = -1;
        
        try {
             fPath = new File(downloadPath);
             fDir = fPath;
             fDir.mkdirs();
             
             f = new File(downloadPath, fileName);
             
             ftp.changeWorkingDirectory(filePath);
          
             bos = new BufferedOutputStream(new FileOutputStream(f));
             //boolean isSuccess = ftp.retrieveFile("RECV_FILE.XXX", bos); //테스트용
             boolean isSuccess = ftp.retrieveFile(fileName, bos);
              
             if (isSuccess) {
                 result = 1; // 성공     
             }
          
        }catch (Exception e) {
        	logger.error(Util.getPrintStacTraceString(e)); 
        }finally{  
            if(bos != null) try {bos.close();} catch (Exception e) {log.error(e.getMessage());}
        }
        
        return result;
    }
    
    
    
    /**
     * Download file
     * @param filePath		Download할 FTP서버 파일 경로
     * @param fileName		Download할 파일 명
     * @param request
     * @return
     * @throws Exception
     */
    public int downloadFtpFileKTnet(String filePath, String fileName, HttpServletRequest request, String downloadPath) throws Exception {
        
        BufferedOutputStream bos = null;
        File fPath  = null;
        File fDir = null;
        File f   = null; 
        ByteArrayInputStream fis2 = null;
		OutputStream fos = null;
        
        int result = -1;
        
        try {
             fPath = new File(downloadPath);
             fDir = fPath;
             fDir.mkdirs();
             
             f = new File(downloadPath, fileName);
             
             ftp.changeWorkingDirectory(filePath);
          
             bos = new BufferedOutputStream(new FileOutputStream(f));
             boolean isSuccess = ftp.retrieveFile(fileName, bos);
              
             if (isSuccess) {
                 result = 1; // 성공     
             }
             
          // 전송에 대한 응답
  			logger.debug("FTP 파일 전송에 대한 응답 수신 : KTNET");
  			fos = new ByteArrayOutputStream();
  			this.ftp.retrieveFile("STATE_FILE.XXX", fos);
  			
  			// 종료 파일 전송
  			logger.debug("FTP 종료파일 전송 : KTNET");
  			String logoff = new String("LOGOFF;");
  			fis2 = new ByteArrayInputStream(logoff.getBytes());
  			this.ftp.remoteStore("LOGOFF.XXX");
  			isSuccess = this.ftp.storeFile("LOGOFF.XXX", fis2);
  			logger.debug(fileName + " : FTP 종료파일 결과 : " + isSuccess);
          
        }catch (Exception e) {
        	result = -1;
        	logger.error(Util.getPrintStacTraceString(e)); 
        }finally{  
            if(bos != null) try {bos.close();} catch (Exception e) {logger.error(Util.getPrintStacTraceString(e));}
            
            if(fis2 != null) {
				try{ fis2.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
			}
			if(fos != null) {
				try{ fos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
			}
        }
        
        return result;
    }
//    
//    /**
//     * Delete file
//     * @param folder		삭제할 파일이 위치한 폴더
//     * @param files			ftp서버에서 삭제할 파일 목록
//     * @return
//     */
//    public boolean deleteFtpFile( String folder, ArrayList<String> files ) {
//    	logger.debug("deleteFtpFile START - ");
//    	
//    	boolean isSuccess = false;
//                
//        for(int i = 0; i < files.size(); i++){
//        	logger.debug(files.get(i));
//        }
//        
//        //FTP 서버에 업로드할 파일 경로
//        //String localPath = Prop.getMessage("fileshare_upload_dir");
//        
//        try {
//            String[] folders = folder.split("/");
//            String folder_str = "";
//            
//            try {
//                for(int i=0; i<folders.length; i++) {
//                    folder_str += folders[i] + "/";
//                }
//            }catch( Exception e ) {
//                logger.error(Util.getPrintStacTraceString(e));
//            }
//            
//            logger.debug("deleteFtpFile - folder_str : " + folder_str);
//            //ftp.changeWorkingDirectory( folder_str );
//            if(ftp.printWorkingDirectory() == null){
//            	ftp.changeWorkingDirectory( "/"+ folder_str );
//            }
//            
//            logger.info(" deleteFtpFile - printWorkingDirectory : " + ftp.printWorkingDirectory());
//            
//            String sourceFile = "";
//            for( int i = 0; i < files.size(); i++ ) {
//                //sourceFile = localPath + files.get(i);
//                sourceFile = files.get(i);
//
//                try {
//                    logger.debug(sourceFile + " : 삭제시작 = >");
//                    
//                    isSuccess = ftp.deleteFile(sourceFile);
//                    
//                    logger.debug(sourceFile + " : 삭제결과 = >" + isSuccess);
//                    
//                }catch(IOException e) {
//                    logger.error(Util.getPrintStacTraceString(e));
//                    isSuccess = false;
//                }finally { 
//                	
//                }
//            }
//            
//        }catch (Exception e) {
//            logger.error(Util.getPrintStacTraceString(e));
//        }
//        logger.debug("deleteFtpFile END " + isSuccess);
//        return isSuccess;
//    }
//    
    /**
     * FTP Connect For EDI
     * 
     * @param port
     * @param ip
     * @param id
     * @param password
     * @param passiveMode
     * @return
     * @throws Exception
     */
    public String connectFTPForEDI(int port, String ip, String id, String password, String passiveMode) throws Exception {
    	return connectFTPForEDI(port, ip, id, password, passiveMode, null);
    }
    public String connectFTPForEDI(int port, String ip, String id, String password, String passiveMode, Map<String, Object> docSend) throws Exception {
        int reply;
        String logVal = "";
        
        try {
            this.ftp = new FTPClient();
            
            this.ftp.setDataTimeout    (1000 * 60 * 5);	// 5분
            this.ftp.setConnectTimeout (1000 * 60 * 5);	// 5분
			
            // FTP 연결
            this.ftp.connect( ip, port );
            
            // 타임아웃 설정 1000 * 10 = 10초
            this.ftp.setSoTimeout(1000 * 30);
            
            /** 지정창고전송일경우 setControlKeepAliveTimeout 실행 안되도록 테스트 */
            if(docSend == null) {
            	this.ftp.setControlKeepAliveTimeout(300);
            	logger.info("setControlKeepAliveTimeout O");
            }else{
            	logger.info("setControlKeepAliveTimeout X");
            }
            
            logVal = "FTP 아이디:" + id + ", passiveMode:" + passiveMode;
            logger.info(logVal);
            logger.info("Connected to: " + ip + ", port: "+ftp.getRemotePort());
            
            try {
	            reply = ftp.getReplyCode();
	            if ( !FTPReply.isPositiveCompletion(reply) ) {
	                ftp.disconnect();
	                logger.error("FTP server refused connection.");
	                
	                return "MF-109";
	            }
	            
	            if( !ftp.login(id, password) ) {
	                ftp.logout();
	                logVal = "FTP 로그인 실패 접속 아이디 : " + id;
	                logger.info(logVal);
	                return "MF-110";
	            }
            }catch(Exception e){
            	/** 연결실패오류 발생해서 한번 재시도 처리함 */
            	reply = ftp.getReplyCode();
	            if ( !FTPReply.isPositiveCompletion(reply) ) {
	                ftp.disconnect();
	                logger.error("FTP server refused connection.");
	                
	                return "MF-109";
	            }
	            
	            if( !ftp.login(id, password) ) {
	                ftp.logout();
	                logVal = "FTP 로그인 실패 접속 아이디 : " + id;
	                logger.info(logVal);
	                return "MF-110";
	            }
            }
            
            
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            if("Y".equals(passiveMode)){
            	logger.info("Enter Passive Mode...");
            	ftp.enterLocalPassiveMode();
            }else{
            	logger.info("Enter Active Mode...");
            	ftp.enterLocalActiveMode();
            }
            
            logger.info(ftp.printWorkingDirectory());
            
            try {
            	if(docSend != null && Util.null2str(docSend.get("custom")).equals("Y")){
            		logger.info("setFileTransferMode : " + docSend.get("fileTransferMode"));
            		ftp.setFileTransferMode(Util.null2int(docSend.get("fileTransferMode")));
            	}else{
            		ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            	}
            	
//            	ftp.setFileTransferMode(FTP.BLOCK_TRANSFER_MODE);
//            	ftp.setControlEncoding(ftpSiteEncoding);
            }catch(Exception e){
            	logger.info("connectFTPForEDI set Error");
            }
        } catch(Exception e) {
            throw e;
        }
        
        return "";
    }
    
    /**
     * FTP Connect For KTNET
     * 
     * @param port
     * @param ip
     * @param id
     * @param password
     * @param passiveMode
     * @param fl31ORfl32
     * @return
     * @throws Exception
     */
    public String connectFTPForKTNET(int port, String ip, String id, String password, String passiveMode, String fl31ORfl32) throws Exception {
        int reply;
        String logVal = "";
        
        try {
            this.ftp = new FTPClient();
            
            this.ftp.setDataTimeout    (1000 * 60 * 5);	// 5분
            this.ftp.setConnectTimeout (1000 * 60 * 5);	// 5분
			
            // FTP 연결
            this.ftp.connect( ip, port );
            
            // 타임아웃 설정 1000 * 10 = 10초
            this.ftp.setSoTimeout(1000 * 30);
            this.ftp.setControlKeepAliveTimeout(300);
            
            logVal = "KTNET FTP 아이디:" + id + ", passiveMode:" + passiveMode + ", fl31ORfl32:" + fl31ORfl32;
            logger.info(logVal);
            logger.info("KTNET FTP Connected to " + ip + " on "+ftp.getRemotePort());
            
            reply = ftp.getReplyCode();
            if ( !FTPReply.isPositiveCompletion(reply) ) {
                ftp.disconnect();
                logger.error("FTP server refused connection.");
                
                return "MF-109";
            }
            
            // 테스트 FTP 2개 : ftp.yht.co.kr, icube.ufms.co.kr
            if("ftp.yht.co.kr".equalsIgnoreCase(ip)
            		|| "icube.ufms.co.kr".equalsIgnoreCase(ip)
            		) {
            	if( !ftp.login(id, password) ) {
                    ftp.logout();
                    return "MF-110";
                }
            } else {
            	if( !ftp.login(id, password, fl31ORfl32) ) {
                    ftp.logout();
                    return "MF-110";
                }
            }
            
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            if("Y".equals(passiveMode)){
            	logger.info("Enter Passive Mode...");
            	ftp.enterLocalPassiveMode();
            }
            
            logger.info(ftp.printWorkingDirectory());
        } catch(Exception e) {
        	logger.error(Util.getPrintStacTraceString(e));
            
            return "MF-135";
        }
        
        return "";
    }
    
    /**
     * 생성파일 FTP(웹서비스 등) 송신
     * 
     * @param localDirPath
     * @param fileName
     * @param tempFileName
     * @param serverRootPath
     * @param serverSendReceivePath
     * @return
     * @throws Exception
     */
    public boolean ftpFileSendForEDI(String localDirPath, String fileName, String tempFileName, String serverRootPath, String serverSendReceivePath) throws Exception {
    	return ftpFileSendForEDI(localDirPath, fileName, tempFileName, serverRootPath, serverSendReceivePath, null);
    }
    public boolean ftpFileSendForEDI(String localDirPath, String fileName, String tempFileName, String serverRootPath, String serverSendReceivePath, Map<String, Object> docSend) throws Exception {
    	boolean isSuccess = false;
        
        // 전송대상 로컬 파일 경로
        String localPath = localDirPath;
        String uploadFileName = null;
        logger.info("");
        logger.info("[FTP 전송 파일 및 경로]");
        logger.info("전송대상 로컬 파일 경로	: " + localPath);
        logger.info("파일명			: " + fileName);
        logger.info("임시파일명		: " + tempFileName);
        logger.info("서버root		: " + serverRootPath);
        logger.info("서버send		: " + serverSendReceivePath);
		
        File uploadFile =  null;
        FileInputStream fis = null;
		BufferedInputStream bis = null;
		OutputStream outputStream = null;
		
        try {
        	logger.info("");
        	logger.info("[FTP 작업 경로]");
        	logger.info("FTP 현재 경로		: " + ftp.printWorkingDirectory());
        	logger.info("FTP 변경 경로		: " + serverRootPath + serverSendReceivePath); 
        	
        	// 원격 FTP 작업 폴더 변경( 테스트용 경로 지정 )
            ftp.changeWorkingDirectory( ftp.printWorkingDirectory() + serverRootPath + serverSendReceivePath );
            logger.info("FTP 작업 경로 변경	: " + ftp.printWorkingDirectory());
            
//            // 목록보기
// 			FTPFile[] ftpfiles = ftp.listFiles("./");  // 폴더의 모든 파일 list
// 			FTPFile ftpFile = null;
// 	        if (ftpfiles != null) {
// 	        	logger.debug("");
// 	            for (int i = 0; i < ftpfiles.length; i++) {
// 	                ftpFile = ftpfiles[i];
// 	                logger.debug("FTP Files : " + ftpFile.toString());  // file.getName(), file.getSize() 등등..
// 	            }
// 	        }
            
 	        uploadFileName = fileName;
            uploadFile = new File(localPath + tempFileName);
            
            logger.info("");
            logger.info("[FTP 파일 전송]");
            logger.info("FTP 전송 파일명	: " + uploadFileName);
            try {
    			fis	= new FileInputStream(uploadFile);
    			
    			/** 지정창고전송테스트중 */
    			if(docSend != null){
    				try{
    					logger.info("storeFileStream");
            			outputStream = ftp.storeFileStream(uploadFileName);
            			byte[] bytesIn = new byte[4096];
        	            int read = 0;
        	            while ((read = fis.read(bytesIn)) != -1) {
        	                outputStream.write(bytesIn, 0, read);
        	            }
        	            if(outputStream != null) {
            				try{ outputStream.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
            			}
        	            isSuccess = ftp.completePendingCommand();
        	            logger.info("22222FTP 전송 결과	: " + isSuccess);
    				}catch(Exception e){
    					/** 연결실패오류 발생해서 한번 재시도 처리함 */
    					bis	= new BufferedInputStream(fis);
            			isSuccess = ftp.storeFile(uploadFileName, bis);
            			logger.info("11111FTP 전송 결과	: " + isSuccess);
    				}
    			}else{
    				bis	= new BufferedInputStream(fis);
        			
                	// FTP 전송
        			isSuccess = ftp.storeFile(uploadFileName, bis);
        			logger.info("FTP 전송 결과	: " + isSuccess);
    			}
    			
                
            } catch(Exception e) {
            	isSuccess = false;
            	logger.error(Util.getPrintStacTraceString(e));
            	throw e;
            } finally {
            	if(bis != null) {
    				try{ bis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
            	if(outputStream != null) {
    				try{ outputStream.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
    			if(fis != null) {
    				try{ fis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
            }
        } catch (Exception e) {
        	isSuccess = false;
        	logger.error(Util.getPrintStacTraceString(e));
            throw e;
        }
        
        return isSuccess;
    }
    
    
    /**
     * 생성파일 FTP(웹서비스 등) 송신 FNC전용
     * @param localDirPath
     * @param fileName
     * @param tempFileName
     * @param serverRootPath
     * @param serverSendReceivePath
     * @return 
     * @throws Exception
     */
    public boolean ftpFileSendForFnc(String localDirPath, String fileName, String tempFileName, String serverRootPath, String serverSendReceivePath) throws Exception {
    	boolean isSuccess = false;
        
        // 전송대상 로컬 파일 경로
        String localPath = localDirPath;
        String uploadFileName = null;
        logger.info("");
        logger.info("[FTP 전송 파일 및 경로]");
        logger.info("전송대상 로컬 파일 경로	: " + localPath);
        logger.info("파일명			: " + fileName);
        logger.info("임시파일명		: " + tempFileName);
        logger.info("서버root		: " + serverRootPath);
        logger.info("서버send		: " + serverSendReceivePath);
		
        File uploadFile =  null;
        FileInputStream fis = null;
		BufferedInputStream bis = null;
		String uploadFileContent = "";
		
        try {
        	logger.info("");
        	logger.info("[FTP 작업 경로]");
        	logger.info("FTP 현재 경로		: " + ftp.printWorkingDirectory());
        	logger.info("FTP 변경 경로		: " + serverRootPath + serverSendReceivePath); 
        	
        	// 원격 FTP 작업 폴더 변경( 테스트용 경로 지정 )
            ftp.changeWorkingDirectory( ftp.printWorkingDirectory() + serverRootPath + serverSendReceivePath );
            logger.info("FTP 작업 경로 변경	: " + ftp.printWorkingDirectory());
            
            // 목록보기
 			FTPFile[] ftpfiles = ftp.listFiles("./");  // 폴더의 모든 파일 list
 			FTPFile ftpFile = null;
 	        if (ftpfiles != null) {
 	        	logger.debug("");
 	            for (int i = 0; i < ftpfiles.length; i++) {
 	                ftpFile = ftpfiles[i];
 	                logger.debug("FTP Files : " + ftpFile.toString());  // file.getName(), file.getSize() 등등..
 	            }
 	        }
            
 	        uploadFileName = fileName;
            uploadFile = new File(localPath + tempFileName);
            
            try {
            	logger.debug("localPath   " + localPath);
            	logger.debug("tempFileName   " + tempFileName);
            	uploadFileContent = FileUtil.fileRead(localPath, tempFileName, "euc-kr");
            	
            	logger.debug("uploadFileContent" + uploadFileContent.toString());
            	
    			fis	= new FileInputStream(uploadFile);
    			bis	= new BufferedInputStream(fis);
            	
    			logger.info("");
    			logger.info("[FTP 파일 전송]");
    			logger.info("FTP 전송 파일명	: " + uploadFileName);
            	// FTP 전송
    			isSuccess = ftp.storeFile(uploadFileName, new ByteArrayInputStream(uploadFileContent.getBytes("euc-kr")));
    			logger.info("FTP 전송 결과	: " + isSuccess);
                
            } catch(Exception e) {
            	isSuccess = false;
            	logger.error(Util.getPrintStacTraceString(e));
            	throw e;
            } finally {
            	if(bis != null) {
    				try{ bis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
    			if(fis != null) {
    				try{ fis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
            }
        } catch (Exception e) {
        	isSuccess = false;
        	logger.error(Util.getPrintStacTraceString(e));
            throw e;
        }
        
        return isSuccess;
    }
    
    /**
     * FTP 파일 전송 KTNET 전용
     * 
     * @param localDirPath
     * @param fileName
     * @param tempFileName
     * @param serverRootPath
     * @param serverSendReceivePath
     * @return
     * @throws Exception
     */
    public boolean ftpFileSendForKTnet(String localDirPath, String fileName, String tempFileName, String serverRootPath, String serverSendReceivePath) throws Exception {
    	logger.info("전송대상 로컬 파일 경로 : " + localDirPath + ", 파일명 : " + fileName + ", 임시파일명 : " + tempFileName + ", 서버 root : " + serverRootPath + ", 서버 send OR recive : " + serverSendReceivePath);
    	
    	boolean isSuccess = false;
        
    	// 전송대상 로컬 파일 경로
        String localPath = localDirPath;
        String uploadFileContent = "";
        
		ByteArrayInputStream fis2 = null;
		OutputStream fos = null;
		
        try {
        	logger.info("KTNET FTP file path : " + serverRootPath + serverSendReceivePath); 
        	
        	// 원격 FTP 작업 폴더 변경( 테스트용 경로 지정 )
            ftp.changeWorkingDirectory( serverRootPath + serverSendReceivePath );
            logger.info(ftp.printWorkingDirectory());
                        
            try {
            	uploadFileContent = FileUtil.fileRead(localPath, tempFileName, "euc-kr");
     	        logger.debug("KTNET FTP 전송 내용 : " + uploadFileContent);
     	        
            	// FTP 전송
    			isSuccess = ftp.storeFile(fileName, new ByteArrayInputStream(uploadFileContent.getBytes("euc-kr")));
                logger.debug("KTNET FTP 전송 결과 : " + isSuccess + ", 전송 파일명 : " + fileName);
                
            	// 전송에 대한 응답
     			logger.debug("KTNET FTP 파일 전송에 대한 응답 수신");
     			fos = new ByteArrayOutputStream();
     			this.ftp.retrieveFile("STATE_FILE.XXX", fos);
     			
     			// 종료 파일 전송
     			logger.debug("KTNET FTP 종료파일 전송");
     			String logoff = new String("LOGOFF;");
     			fis2 = new ByteArrayInputStream(logoff.getBytes());
     			this.ftp.remoteStore("LOGOFF.XXX");
     			this.ftp.storeFile("LOGOFF.XXX", fis2);
    			
     			logger.debug("KTNET FTP 전송 완료 : " + fileName);
            } catch(Exception e) {
            	isSuccess = false;
            	throw e;
            } finally {
            	
            	if(fis2 != null) {
    				try{ fis2.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
    			if(fos != null) {
    				try{ fos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
            }
        } catch (Exception e) {
        	isSuccess = false;
        	throw e;
        }
        
        return isSuccess;
    }
//    
//    /**
//     * FTP 파일 수신 KTNET 전용
//     * 
//     * @param localDirPath
//     * @param serverRootPath
//     * @param serverSendReceivePath
//     * @param receivefileList
//     * @return
//     * @throws Exception
//     */
//    public boolean ftpFileReceiveForKTnet(String receiveFileName, String localDirPath
//    		, String serverRootPath, String serverSendReceivePath
//    		, List<File> receivefileList) throws Exception {
//    	
//    	logger.info("수신대상 로컬 파일 경로 : " + localDirPath + ", 서버root : " + serverRootPath + ", 서버send : " + serverSendReceivePath);
//    	
//    	FileInputStream fis = null;
//		BufferedInputStream bis = null;
//		
//		FileOutputStream outputStream = null;
//        BufferedOutputStream bufferedOutputStream = null;
//		
//		ByteArrayInputStream fis2 = null;
//		OutputStream fos = null;
//    	
//    	// 전송대상 로컬 파일 경로
//        String localPath = localDirPath;
//        
//        boolean isSuccess = false;	// 수신결과
//        
//        File receiveFile =  null;
//		File folder = null;
//        try {
//        	logger.info("KTNET FTP file path : " + serverRootPath + serverSendReceivePath); 
//        	
//        	folder = new File(localPath);
//        	
//        	// 로컬 폴더 존재 유무 체크
//     		if(!folder.isDirectory()) {
//     			folder.mkdirs();
//     		}
//     		
//        	// 원격 FTP 작업 폴더 변경( 테스트용 경로 지정 )
//            ftp.changeWorkingDirectory( serverRootPath + serverSendReceivePath );
// 			
//            try {
//            	logger.info("KTNET FTP 수신 시작");
//            	
//	            receiveFile = new File(localPath, receiveFileName);
//            	
//	            outputStream = new FileOutputStream(receiveFile);
//            	bufferedOutputStream = new BufferedOutputStream(outputStream);
//            	// KTNETACPS.4PXD.3.UNB
//            	isSuccess = ftp.retrieveFile("LOGOFF.XXX", bufferedOutputStream);
//                
//                logger.info("KTNET FTP 수신 결과 : " + isSuccess + ", 수신 받은 파일명 : " + receiveFileName);
//               
//                // 수신 파일 담기
//                receivefileList.add(receiveFile);
//	            
//            	// 전송에 대한 응답
//     			logger.info("KTNET FTP 파일 전송에 대한 응답 수신");
//     			fos = new ByteArrayOutputStream();
//     			this.ftp.retrieveFile("STATE_FILE.XXX", fos);
//     			
//     			// 종료 파일 전송
//     			logger.info("KTNET FTP 종료파일 전송");
//     			String logoff = new String("LOGOFF;");
//     			fis2 = new ByteArrayInputStream(logoff.getBytes());
//     			this.ftp.remoteStore("LOGOFF.XXX");
//     			this.ftp.storeFile("LOGOFF.XXX", fis2);
//    			
//     			logger.info("KTNET FTP 수신 완료");
//            } catch(Exception e) {
//            	isSuccess = false;
//            	throw e;
//            } finally {
//            	if(bis != null) {
//    				try{ bis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//    			if(fis != null) {
//    				try{ fis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//    			
//    			if(bufferedOutputStream != null) {
//    				try{ bufferedOutputStream.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//    			if(outputStream != null) {
//    				try{ outputStream.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//	    			
//            	if(fis2 != null) {
//    				try{ fis2.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//    			if(fos != null) {
//    				try{ fos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//            }
//        } catch (Exception e) {
//        	isSuccess = false;
//        	throw e;
//        }
//        
//        return isSuccess;
//    }
//    
//    
//    /**
//     * FTP 파일 전송 KTNET 전용(전자세금계산서)
//     * 
//     * @param localDirPath
//     * @param fileName
//     * @param tempFileName
//     * @param serverRootPath
//     * @param serverSendReceivePath
//     * @return
//     * @throws Exception
//     */
//    public boolean ftpFileSendForKTnetETB(String localDirPath, String fileName, String tempFileName, String serverRootPath, String serverSendReceivePath) throws Exception {
//    	logger.debug("전송대상 로컬 파일 경로 : " + localDirPath + ", 파일명 : " + fileName + ", 임시파일명 : " + tempFileName + ", 서버root : " + serverRootPath + ", 서버send : " + serverSendReceivePath);
//    	
//    	boolean isSuccess = false;
//    	boolean isSuccess2 = false;
//        
//    	// 전송대상 로컬 파일 경로
//        String localPath = localDirPath;
//        String uploadFileName = null;
//        
//        File uploadFile =  null;
//        FileInputStream fis = null;
//		BufferedInputStream bis = null;
//		
//		ByteArrayInputStream fis2 = null;
//		OutputStream fos = null;
//        try {
//        	
//        	uploadFileName = fileName;
//        	uploadFile = new File(localPath + tempFileName);
//            
//            try {
//                fis	= new FileInputStream(uploadFile);
//    			bis	= new BufferedInputStream (fis);
//            	
//    			if(ftp.changeWorkingDirectory(serverRootPath)) {
//    				logger.debug("makeDirectory : " + true);
//    			} else {
//    				logger.debug("makeDirectory : " + false);
//    			}
//    			
//    			logger.debug("ftpFileSendForKTnetETB / FTP file path : " + serverRootPath + serverSendReceivePath); 
//    			
//    			logger.debug(tempFileName + " => " + uploadFileName + " : FTP 전송 시작");
//            	// FTP 전송
//    			isSuccess = ftp.storeFile(uploadFileName, fis);
//                logger.debug(tempFileName + " => " + uploadFileName + " : FTP 전송 결과 : " + isSuccess);
//                
//            	// 전송에 대한 응답
//     			logger.debug("KTNET FTP 파일 전송에 대한 응답 수신");
//     			fos = new ByteArrayOutputStream();
//     			this.ftp.retrieveFile("STATE_FILE.XXX", fos);
//     			
//     			// 종료 파일 전송
//     			logger.debug("KTNET FTP 종료파일 전송");
//     			String logoff = new String("LOGOFF;");
//     			fis2 = new ByteArrayInputStream(logoff.getBytes());
//     			this.ftp.remoteStore("LOGOFF.XXX");
//     			isSuccess2 = this.ftp.storeFile("LOGOFF.XXX", fis2);
//     			logger.debug(tempFileName + " => " + uploadFileName + " : FTP 종료파일 결과 : " + isSuccess2);
//    			
//     			logger.debug("KTNET FTP 전송 완료 : " + uploadFileName);
//            } catch(Exception e) {
//            	isSuccess = false;
//            	throw e;
//            } finally {
//            	if(bis != null) {
//    				try{ bis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//    			if(fis != null) {
//    				try{ fis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//    			
//            	if(fis2 != null) {
//    				try{ fis2.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//    			if(fos != null) {
//    				try{ fos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//            }
//        } catch (Exception e) {
//        	isSuccess = false;
//        	throw e;
//        }
//        
//        if(!isSuccess || !isSuccess2){
//        	return false;
//        }
//        
//        return isSuccess;
//    }
//    
//	/**
//	 * Send file
//	 * @param localPath		현재 폴더 경로
//	 * @param uploadPath	ftp서버에 생성할 폴더 경로
//	 * @param files			ftp서버에 업로드할 파일 목록
//	 * @return
//	 */
//	public boolean sendFtpServerForSosius( String localPath, String uploadPath, ArrayList<String> files ) {
//		boolean isSuccess = false;
//		//FTP 서버에 업로드할 파일 경로
////		String localPath = Prop.getMessage("edi_mcube_send_dir");
//		
////		logger.debug("folder : " +folder);
//		
//		for(int i = 0; i < files.size(); i++){
//			logger.debug("files : " + files.get(i));
//		}
//		
//		try {
////			String[] folders = folder.split("/");
////			String folder_str = "/";
////			
////			try {
////				for(int i=0; i<folders.length; i++) {
////					logger.debug("folders : " + folders[i]);
////					folder_str += folders[i] + "/";
////					logger.debug("folder_str : " + folder_str);
////				}
////			}catch( Exception e ) {
////				logger.error(Util.getPrintStacTraceString(e));
////			}
//			
//			logger.info(ftp.printWorkingDirectory());
//			
//			File uploadFile =  null;
//			FileInputStream fis = null;
//			
//			String tempLocalFileName = "";
//			String tempUploadFileName = "";
//			String sourceFile = "";
//			
//			for( int i = 0; i < files.size(); i++ ) {
//				if(!localPath.endsWith("/")) localPath += "/";
//				if(!uploadPath.endsWith("/")) uploadPath += "/";
//				
//				//ftp서버에 한글파일을 쓸때 한글깨짐 방지
//				tempLocalFileName = localPath + new String(files.get(i).getBytes("utf-8"),"iso_8859_1");
//				tempUploadFileName = uploadPath + new String(files.get(i).getBytes("utf-8"),"iso_8859_1");
//				sourceFile = localPath + files.get(i);
//				
//				uploadFile = new File(tempLocalFileName);
//				fis = null;
//				
//				try {
//					fis = new FileInputStream(uploadFile);
//					logger.debug("대상파일 : " + sourceFile + " : 전송시작 = >");
//					
//					logger.debug(ftp.printWorkingDirectory());
//					
//					//tempFileName 업로드 될 타겟의 풀 경로가 들어가야 함.  ex) /A/A01/A001/aa.zip   파일명만 들어갈경우 unix에서 전송 실패하는 경우가 생김.
//					isSuccess = ftp.storeFile(tempUploadFileName, fis);
//					
//					logger.debug("업로드 파일 : " + tempUploadFileName + " : 전송결과 = >" + isSuccess);
//					
//				}catch(IOException e) {
//					logger.error(Util.getPrintStacTraceString(e));
//					isSuccess = false;
//				}finally {
//					if( fis != null ) {
//						try { fis.close(); } catch(IOException e) {}
//					}
//					
//					if(isSuccess) {
//						uploadFile.delete();
//						logger.debug("Original File Deleted");
//					}
//				}
//			}
//			
//		}catch (Exception e) {
//			logger.error(Util.getPrintStacTraceString(e));
//		}
//		
//		return isSuccess;
//	}
//	
//	   /**
//     * FTP 파일 전송 KLNET 전용
//     * 
//     * @param localDirPath
//     * @param fileName
//     * @param tempFileName
//     * @param serverRootPath
//     * @param serverSendReceivePath
//     * @return
//     * @throws Exception
//     */
//    public boolean ftpFileSendForKLnet(String localDirPath, String fileName, String tempFileName, String serverRootPath, String serverSendReceivePath) throws Exception {
//    	logger.info("전송대상 로컬 파일 경로 : " + localDirPath + ", 파일명 : " + fileName + ", 임시파일명 : " + tempFileName + ", 서버 root : " + serverRootPath + ", 서버 send OR recive : " + serverSendReceivePath);
//    	
//    	boolean isSuccess = false;
//        
//    	// 전송대상 로컬 파일 경로
//        String localPath = localDirPath;
//        String uploadFileContent = "";
//        
//		ByteArrayInputStream fis2 = null;
//		OutputStream fos = null;
//		
//        try {
//        	logger.info("KLNET FTP file path : " + serverRootPath + serverSendReceivePath); 
//        	
//        	// 원격 FTP 작업 폴더 변경( 테스트용 경로 지정 )
//            ftp.changeWorkingDirectory( serverRootPath + serverSendReceivePath );
//            logger.info(ftp.printWorkingDirectory());
//                        
//            try {
//            	uploadFileContent = FileUtil.fileRead(localPath, tempFileName, "euc-kr");
//     	        logger.debug("KLNET FTP 전송 내용 : " + uploadFileContent);
//     	        
//            	// FTP 전송
//    			isSuccess = ftp.storeFile(fileName, new ByteArrayInputStream(uploadFileContent.getBytes("euc-kr")));
//                logger.debug("KLNET FTP 전송 결과 : " + isSuccess + ", 전송 파일명 : " + fileName);
//                
//            	// 전송에 대한 응답
//     			logger.debug("KLNET FTP 파일 전송에 대한 응답 수신");
//     			fos = new ByteArrayOutputStream();
//     			this.ftp.retrieveFile("STATE_FILE.XXX", fos);
//     			
//     			// 종료 파일 전송
//     			logger.debug("KLNET FTP 종료파일 전송");
//     			String logoff = new String("LOGOFF;");
//     			fis2 = new ByteArrayInputStream(logoff.getBytes());
//     			this.ftp.remoteStore("LOGOFF.XXX");
//     			this.ftp.storeFile("LOGOFF.XXX", fis2);
//    			
//     			logger.debug("KLNET FTP 전송 완료 : " + fileName);
//            } catch(Exception e) {
//            	isSuccess = false;
//            	throw e;
//            } finally {
//            	
//            	if(fis2 != null) {
//    				try{ fis2.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//    			if(fos != null) {
//    				try{ fos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//            }
//        } catch (Exception e) {
//        	isSuccess = false;
//        	throw e;
//        }
//        
//        return isSuccess;
//    }
//    
//    /**
//     * FTP 파일 전송 TRAXSON 전용
//     * 
//     * @param localDirPath
//     * @param fileName
//     * @param tempFileName
//     * @param serverRootPath
//     * @param serverSendReceivePath
//     * @return
//     * @throws Exception
//     */
//    public boolean ftpFileSendForTraxon(String localDirPath, String fileName, String tempFileName, String serverRootPath, String serverSendReceivePath) throws Exception {
//    	logger.info("전송대상 로컬 파일 경로 : " + localDirPath + ", 파일명 : " + fileName + ", 임시파일명 : " + tempFileName + ", 서버 root : " + serverRootPath + ", 서버 send OR recive : " + serverSendReceivePath);
//    	
//    	boolean isSuccess = false;
//        
//    	// 전송대상 로컬 파일 경로
//        String localPath = localDirPath;
//        String uploadFileContent = "";
//        
//		ByteArrayInputStream fis2 = null;
//		OutputStream fos = null;
//		
//        try {
//        	logger.info("TRAXON FTP file path : " + serverRootPath + serverSendReceivePath); 
//        	
//        	// 원격 FTP 작업 폴더 변경( 테스트용 경로 지정 )
//            ftp.changeWorkingDirectory( serverRootPath + serverSendReceivePath );
//            logger.info(ftp.printWorkingDirectory());
//                        
//            try {
//            	uploadFileContent = FileUtil.fileRead(localPath, tempFileName, "euc-kr");
//     	        logger.debug("TRAXON FTP 전송 내용 : " + uploadFileContent);
//     	        
//            	// FTP 전송
//    			isSuccess = ftp.storeFile(fileName, new ByteArrayInputStream(uploadFileContent.getBytes("euc-kr")));
//                logger.debug("TRAXON FTP 전송 결과 : " + isSuccess + ", 전송 파일명 : " + fileName);
//     			logger.debug("TRAXON FTP 전송 완료 : " + fileName);
//            } catch(Exception e) {
//            	isSuccess = false;
//            	throw e;
//            } finally {
//            	
//            	if(fis2 != null) {
//    				try{ fis2.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//    			if(fos != null) {
//    				try{ fos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//    			}
//            }
//        } catch (Exception e) {
//        	isSuccess = false;
//        	throw e;
//        }
//        
//        return isSuccess;
//    }
//    
//    /**
//     * FTP(웹서비스 등) 수신 FNC SR
//     * @param fileName
//     * @param localDirPath
//     * @param serverRootPath
//     * @param serverSendReceivePath
//     * @return
//     * @throws Exception
//     */
//    public boolean ftpFileReceiveForFncSr(String localDirPath
//    		, String serverRootPath, String serverSendReceivePath, String receiveFileServerMove, List<File> receivefileList, String userId) throws Exception {
//    	
//    	logger.info("수신대상 로컬 파일 경로 : " + localDirPath + ", 서버root : " + serverRootPath + ", 서버send : " + serverSendReceivePath + ", 수신 파일무브패스 : " + receiveFileServerMove);
//    	
//    	FileOutputStream fos = null;
//        BufferedOutputStream bos = null;
//        
//    	File file = null;
//		File folder = null;
//        
//    	boolean isSuccess = false;	// 처리결과
//    	
//        folder = new File(localDirPath);
//    	// 로컬 폴더 존재 유무 체크
// 		if(!folder.isDirectory()) {
// 			folder.mkdirs();
// 		}
// 		
//    	// 원격 FTP 작업 폴더 변경( 테스트용 경로 지정 )
//        ftp.changeWorkingDirectory( serverRootPath + serverSendReceivePath );
//        logger.info(ftp.printWorkingDirectory());
//        
//        
//        // 목록보기
//		FTPFile[] ftpfiles = ftp.listFiles("./");  // public 폴더의 모든 파일을 list 합니다
//
////		FTPFile ftpFile = null;
////        if (ftpfiles != null) {
////            for (int i = 0; i < ftpfiles.length; i++) {
////                ftpFile = ftpfiles[i];
////                logger.debug("FTP Files : " + ftpFile.toString());  // file.getName(), file.getSize() 등등..
////            }
////        }
//		
//		
//		 if (ftpfiles != null && ftpfiles.length > 0) {
//             //loop thru files
//             for (FTPFile ftpFile : ftpfiles) {
//            	 
//            	 
//                 if (!ftpFile.isFile()) {
//                     continue;
//                 }
//                 
////                 logger.debug("File is " + ftpFile.getName() );
////                 //get output stream
////                 OutputStream output;
////                 output = new FileOutputStream("FtpFiles" + "/" + ftpFile.getName());
////                 //get the file from the remote system
////                 ftp.retrieveFile(ftpFile.getName(), output);
////                 //close output stream
////                 output.close();
////                 
//                 try {
//                	String ftpFileName = (String) ftpFile.getName();
//                	String[] ftpFileNameSplit = ftpFileName.split("_");
//                	//현재 FNC폴더에 파일이 많기 때문에 해당월의 건만 가져오기위해 임시로 추가함
//                	//추후 파일을 가져오고 FNC폴더에서 가져온 파일은 새로운 폴더로 전체 이동시켜야함
//                	if(ftpFileNameSplit[0].contains("KKPCSR") && ftpFileNameSplit[2].contains(DateTimeUtil.getDate("yyyyMM"))){
//                		file = new File(localDirPath, ftpFile.getName());
//                     	
//                     	fos = new FileOutputStream(file);
//                     	bos = new BufferedOutputStream(fos);
//                        isSuccess = ftp.retrieveFile( ftpFile.getName(), bos);
//                        
//						// 수신 파일 담기
//                        receivefileList.add(file);
//                        
//                        //FTP 연결서버상의 파일 이동 2016-12-29 HIT
//                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                    	ftp.retrieveFile(ftpFile.getName(), outputStream);
//                    	InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
//                    	
//                    	ftp.setFileType(FTP.BINARY_FILE_TYPE);//binary files
//                    	ftp.changeWorkingDirectory(receiveFileServerMove);
//                    	ftp.storeFile(ftpFile.getName(), is);
//                    	
//                    	// 파일 확인후 삭제
//                    	ftp.changeWorkingDirectory("../");
//                    	ftp.deleteFile(ftpFile.getName());
//                    	
//                	}
//                	
//             	} catch (Exception e) {
//         			isSuccess = false;
//                 	throw e;
//         		} finally {  
//                 	if(bos != null) {
//                 		try{ bos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//                 	}
//                 	if(fos != null) {
//                 		try{ fos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
//                 	}
//                 }
//                 
//
//             }
//         }
//    	
//        
//        return isSuccess;
//    }
    
    /**
     * FTP 멀티파일 수신 KTNET 전용
     * @param localDirPath
     * @param serverRootPath
     * @param serverSendReceivePath
     * @param receivefileList
     * @param userId
     * @return
     * @throws Exception
     */
    public boolean ftpMultiFileReceiveForKTnet(String localDirPath, String serverRootPath, String serverSendReceivePath, List<File> receivefileList, String userId, boolean fileNameAddDate) throws Exception {
    	
    	logger.info(new StringBuffer("수신대상 로컬 파일 경로 : ").append(localDirPath).append(", 서버root : ").append(serverRootPath).append(", 서버send : ").append(serverSendReceivePath));
    	
    	boolean isReceive = true;
    	
    	// 전송대상 로컬 파일 경로
        String localPath = localDirPath;
        String receiveFileName = null;
        
        File receiveFile =  null;
		File folder = null;
        
        FileInputStream fis = null;
		BufferedInputStream bis = null;
		
		FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
		
		ByteArrayInputStream fis2 = null;
		OutputStream fos = null;
		
		String[] remoteFiles = null;
        try {
        	logger.info(new StringBuffer("KTNET FTP file path : ").append(serverRootPath).append(serverSendReceivePath)); 
        	
        	folder = new File(localPath);
        	
        	// 로컬 폴더 존재 유무 체크
     		if(!folder.isDirectory()) {
     			folder.mkdirs();
     		}
     		
        	// 원격 FTP 작업 폴더 변경( 테스트용 경로 지정 )
            ftp.changeWorkingDirectory(new StringBuffer(serverRootPath).append(serverSendReceivePath).toString());
            
            // 파일 목록 조회
// 			FTPFile[] ftpfiles = ftp.listFiles("./");  // 접속된 FTP 현재 폴더의 모든 파일 list
// 			FTPFile ftpFile = null;
            
            // 파일 목록 조회(KTNET 전용)
            remoteFiles = ftp.listNames();
            
            if(remoteFiles != null) {
            	logger.info(new StringBuffer("remoteFiles : ").append(remoteFiles.length));
            } else {
            	logger.info("remoteFiles is null");
            }
            
            try {
            	logger.info("KTNET FTP 수신 시작");
                
            	if(remoteFiles != null) {
            		boolean isSuccess = false;
            		
     	            for (int i = 0; i < remoteFiles.length; i++) {
     	            	logger.info(new StringBuffer("FTP Files : ").append(remoteFiles[i]));
//     	            	// KTNET 파일 수신후 전송응답 파일 및 종료 파일은 skip.
//     	            	if(Util.null2str(remoteFiles[i]).toUpperCase().contains("LOGOFF.XXX")) {
//     	            		logger.info("KTNET FTP skip file : " + Util.null2str(remoteFiles[i]));
//     	            		continue;
//     	            	}
     	            	if(!"".equals(Util.null2str(userId))){
     	            		receiveFileName = new StringBuffer(userId).append(".").append(Util.null2str(remoteFiles[i])).toString();
     	            	}else{
     	            		receiveFileName = Util.null2str(remoteFiles[i]);
     	            	}
     	            	if(fileNameAddDate){
     	            		receiveFileName = DateTime.getDate("yyyyMMddHHmmss") +"_" + UUID.randomUUID().toString().substring(0, 10) + receiveFileName;
     	            	}
     	            	receiveFile = new File(localPath, receiveFileName);
     	            	
     	            	outputStream = new FileOutputStream(receiveFile);
     	            	bufferedOutputStream = new BufferedOutputStream(outputStream);
     	            	
     	            	isSuccess = ftp.retrieveFile(Util.null2str(remoteFiles[i]), bufferedOutputStream);
     	                
     	                logger.info(new StringBuffer("KTNET FTP 수신 결과 : ").append(isSuccess).append(", 수신 받은 파일명 : ").append(receiveFileName));
     	               
     	                // 수신 파일 담기
     	                receivefileList.add(receiveFile);
     	                
     	                if(bufferedOutputStream != null) {
     	    				try{ 
     	    					bufferedOutputStream.close();
     	    				}catch(Exception e){
     	    					logger.error(Util.getPrintStacTraceString(e));
     	    				}finally{
     	    					bufferedOutputStream = null;
     	    				}
     	    			}
     	    			if(outputStream != null) {
     	    				try{ 
     	    					outputStream.close();
     	    				}catch(Exception e){ 
     	    					logger.error(Util.getPrintStacTraceString(e));
     	    				}finally{
	 	    					outputStream = null;
	 	    				}
     	    			}
     	            }
     	        }
            	
            	// 전송에 대한 응답
     			logger.info("KTNET FTP 파일 전송에 대한 응답 수신");
     			fos = new ByteArrayOutputStream();
     			this.ftp.retrieveFile("STATE_FILE.XXX", fos);
     			
     			// 종료 파일 전송
     			logger.info("KTNET FTP 종료파일 전송");
     			String logoff = new String("LOGOFF;");
     			fis2 = new ByteArrayInputStream(logoff.getBytes());
     			this.ftp.remoteStore("LOGOFF.XXX");
     			this.ftp.storeFile("LOGOFF.XXX", fis2);
    			
     			logger.info("KTNET FTP 수신 완료");
            } catch(Exception e) {
            	logger.error(Util.getPrintStacTraceString(e));
            	isReceive = false;
            	throw e;
            } finally {
            	if(fis2 != null) {
            		try{ fis2.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
            	}
            	if(fos != null) {
            		try{ fos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
            	}
            	
            	if(bis != null) {
    				try{ bis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
    			if(fis != null) {
    				try{ fis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
    			
    			if(bufferedOutputStream != null) {
    				try{ bufferedOutputStream.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
    			if(outputStream != null) {
    				try{ outputStream.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
	    			
            }
        } catch (Exception e) {
        	isReceive = false;
        	throw e;
        }
        
        return isReceive;
    }
    public boolean ftpMultiFileReceiveForKTnet(String localDirPath, String serverRootPath, String serverSendReceivePath, List<File> receivefileList, String userId) throws Exception {
    	return ftpMultiFileReceiveForKTnet(localDirPath, serverRootPath, serverSendReceivePath, receivefileList, userId, false);
    }
    public boolean ftpMultiFileReceiveForKLnet(String localDirPath, String serverRootPath, String serverSendReceivePath, List<File> receivefileList, String userId, boolean fileNameAddDate) throws Exception {
    	
    	logger.info(new StringBuffer("수신대상 로컬 파일 경로 : ").append(localDirPath).append(", 서버root : ").append(serverRootPath).append(", 서버send : ").append(serverSendReceivePath));
    	
    	boolean isReceive = true;
    	
    	// 전송대상 로컬 파일 경로
        String localPath = localDirPath;
        String receiveFileName = null;
        
        File receiveFile =  null;
		File folder = null;
        
        FileInputStream fis = null;
		BufferedInputStream bis = null;
		
		FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
		
		ByteArrayInputStream fis2 = null;
		OutputStream fos = null;
		
		String[] remoteFiles = null;
        try {
        	logger.info(new StringBuffer("KLNET FTP file path : ").append(serverRootPath).append(serverSendReceivePath)); 
        	
        	folder = new File(localPath);
        	
        	// 로컬 폴더 존재 유무 체크
     		if(!folder.isDirectory()) {
     			folder.mkdirs();
     		}
     		
        	// 원격 FTP 작업 폴더 변경( 테스트용 경로 지정 )
            ftp.changeWorkingDirectory(new StringBuffer(serverRootPath).append(serverSendReceivePath).toString());
            
            // 파일 목록 조회
            remoteFiles = ftp.listNames();
            
            if(remoteFiles != null) {
            	logger.info(new StringBuffer("remoteFiles : ").append(remoteFiles.length));
            } else {
            	logger.info("remoteFiles is null");
            }
            
            try {
            	logger.info("KLNET FTP 수신 시작");
                
            	if(remoteFiles != null) {
            		boolean isSuccess = false;
            		
     	            for (int i = 0; i < remoteFiles.length; i++) {
     	            	logger.info(new StringBuffer("FTP Files : ").append(remoteFiles[i]));
     	            	
     	            	if(!"".equals(Util.null2str(userId))){
     	            		receiveFileName = new StringBuffer(userId).append(".").append(Util.null2str(remoteFiles[i])).toString();
     	            	}else{
     	            		receiveFileName = Util.null2str(remoteFiles[i]);
     	            	}
     	            	if(fileNameAddDate){
     	            		receiveFileName = DateTime.getDate("yyyyMMddHHmmss") +"_" + UUID.randomUUID().toString().substring(0, 10) + receiveFileName;
     	            	}
     	            	receiveFile = new File(localPath, receiveFileName);
     	            	
     	            	outputStream = new FileOutputStream(receiveFile);
     	            	bufferedOutputStream = new BufferedOutputStream(outputStream);
     	            	
     	            	isSuccess = ftp.retrieveFile(Util.null2str(remoteFiles[i]), bufferedOutputStream);
     	                
     	                logger.info(new StringBuffer("KLNET FTP 수신 결과 : ").append(isSuccess).append(", 수신 받은 파일명 : ").append(receiveFileName));
     	               
     	                // 수신 파일 담기
     	                receivefileList.add(receiveFile);
     	                
     	                if(bufferedOutputStream != null) {
     	    				try{ 
     	    					bufferedOutputStream.close();
     	    				}catch(Exception e){
     	    					logger.error(Util.getPrintStacTraceString(e));
     	    				}finally{
     	    					bufferedOutputStream = null;
     	    				}
     	    			}
     	    			if(outputStream != null) {
     	    				try{ 
     	    					outputStream.close();
     	    				}catch(Exception e){ 
     	    					logger.error(Util.getPrintStacTraceString(e));
     	    				}finally{
	 	    					outputStream = null;
	 	    				}
     	    			}
     	            }
     	        }
            	
     			logger.info("KLNET FTP 수신 완료");
            } catch(Exception e) {
            	logger.error(Util.getPrintStacTraceString(e));
            	isReceive = false;
            	throw e;
            } finally {
            	if(fis2 != null) {
            		try{ fis2.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
            	}
            	if(fos != null) {
            		try{ fos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
            	}
            	
            	if(bis != null) {
    				try{ bis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
    			if(fis != null) {
    				try{ fis.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
    			
    			if(bufferedOutputStream != null) {
    				try{ bufferedOutputStream.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
    			if(outputStream != null) {
    				try{ outputStream.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
    			}
	    			
            }
        } catch (Exception e) {
        	isReceive = false;
        	throw e;
        }
        
        return isReceive;
    }
    /**
     * FTP(웹서비스 등) 수신
     * 
     * @param fileName
     * @param localDirPath
     * @param serverRootPath
     * @param serverSendReceivePath
     * @return
     * @throws Exception
     */
    public boolean ftpFileReceiveForEDI(String receiveFileName, String localDirPath, String serverRootPath, String serverSendReceivePath) throws Exception {
    	/** 정상처리안됨   사용X */
    	logger.info(new StringBuffer("수신대상 로컬 파일 경로 : ").append(localDirPath).append(", 서버root : ").append(serverRootPath).append(", 서버send : ").append(serverSendReceivePath).append(", 수신 파일명 : ").append(receiveFileName));
    	
    	FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        
    	File file = null;
		File folder = null;
        
    	boolean isSuccess = false;	// 처리결과
    	
        folder = new File(localDirPath);
    	// 로컬 폴더 존재 유무 체크
 		if(!folder.isDirectory()) {
 			folder.mkdirs();
 		}
 		
    	// 원격 FTP 작업 폴더 변경( 테스트용 경로 지정 )
        ftp.changeWorkingDirectory(new StringBuffer(serverRootPath).append(serverSendReceivePath).toString());
        logger.info(ftp.printWorkingDirectory());
        
        // 목록보기
		FTPFile[] ftpfiles = ftp.listFiles("./");  // public 폴더의 모든 파일을 list 합니다
		FTPFile ftpFile = null;
        if (ftpfiles != null) {
            for (int i = 0; i < ftpfiles.length; i++) {
                ftpFile = ftpfiles[i];
                if(logger.isDebugEnabled()) logger.debug(new StringBuffer("FTP Files : ").append(ftpFile.toString()));  // file.getName(), file.getSize() 등등..
            }
        }
        
    	try {
        	file = new File(localDirPath, receiveFileName);
        	
        	fos = new FileOutputStream(file);
        	bos = new BufferedOutputStream(fos);
            isSuccess = ftp.retrieveFile(receiveFileName, bos);
    	} catch (Exception e) {
			isSuccess = false;
        	throw e;
		} finally {  
        	if(bos != null) {
        		try{ bos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
        	}
        	if(fos != null) {
        		try{ fos.close(); }catch(Exception e){logger.error(Util.getPrintStacTraceString(e));}
        	}
        }
        
        return isSuccess;
    }
}