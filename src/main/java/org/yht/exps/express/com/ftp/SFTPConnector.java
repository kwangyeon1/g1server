package org.yht.exps.express.com.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yht.util.DateTime;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import com.jcraft.jsch.ChannelSftp;

public class SFTPConnector{
	protected static final Log logger = LogFactory.getLog(SFTPConnector.class);
	private SFTPUtil sftp = null;
	
	/*	
     
    private Session session = null;
    private Channel channel = null;
    private ChannelSftp channelSftp = null;*/
    
    /**
     * FTP Connect For KTNET
     * 
     * @param port
     * @param ip
     * @param id
     * @param password
     * @return
     * @throws Exception
     */
    public String connectSFTPForKTNET(int port, String ip, String id, String password, String sa31ORsa32) throws Exception {
    	if(!"".equals(Util.null2str(sa31ORsa32))) id = id + ":" + sa31ORsa32;
    	return connectSFTP(port, ip, id, password);
    }
    
    public String disConnectSFTPForKTNET(){
    	try {
    		if(this.sftp != null){
    			this.sftp.disconnect();
    		}
    	}catch (Exception e) {
    		logger.error(Util.getPrintStacTraceString(e));
    		return "MF-135";
    	}
    	return "";
    }
    
    /**
     * SFTP Connect
     * 
     * @param port
     * @param ip
     * @param id
     * @param password
     * @return
     * @throws Exception
     */
    public String connectSFTP(int port, String ip, String id, String password) throws Exception {
        
        try {
        	this.sftp = new SFTPUtil();
        	//SFTP 연결
        	sftp.init(ip, id, password, port);
        	String logVal = "connectSFTP :::: ip : " + ip + ", id: " + id + ", port: " + port;
        	logger.info(logVal);
        } catch(Exception e) {
        	logger.error(Util.getPrintStacTraceString(e));
            
            return "MF-135";
        }
        
        return "";
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
    public boolean sftpFileSendForKTnet(String localDirPath, String fileName, String tempFileName, String serverRootPath, String serverSendReceivePath) throws Exception {
    	logger.info("전송대상 로컬 파일 경로 : " + localDirPath + ", 파일명 : " + fileName + ", 임시파일명 : " + tempFileName + ", 서버 root : " + serverRootPath + ", 서버 send OR recive : " + serverSendReceivePath);
    	
    	boolean isSuccess = false;
        
    	// 전송대상 로컬 파일 경로
        String localPath = localDirPath;
//        String uploadFileContent = "";
        
		//ByteArrayInputStream fis2 = null;
		//OutputStream fos = null;
		
        try {
        	logger.info("KTNET SFTP file path : " + serverRootPath + serverSendReceivePath); 
                        
            try {
//            	uploadFileContent = FileUtil.fileRead(localPath, tempFileName, "euc-kr");
            	
//     	        logger.debug("KTNET SFTP 전송 내용 : " + uploadFileContent);
     	        File sendFile = new File(localPath + tempFileName);
     	        //파일전송
     	        sftp.upload(serverRootPath + serverSendReceivePath, sendFile);
     	        //연결 해제
     	        sftp.disconnect();
     	        isSuccess = true;

    			
     			logger.debug("KTNET SFTP 전송 완료 : " + fileName);
            } catch(Exception e) {
            	isSuccess = false;
            	throw e;
            }
        } catch (Exception e) {
        	isSuccess = false;
        	throw e;
        }
        
        return isSuccess;
    }
    
    /**
     * FTP 파일 전송
     * 
     * @param localDirPath
     * @param fileName
     * @param tempFileName
     * @param serverRootPath
     * @param serverSendReceivePath
     * @return
     * @throws Exception
     */
    public boolean sftpFileSendForEDI(String localDirPath, String fileName, String tempFileName, String serverRootPath, String serverSendReceivePath) throws Exception {
    	return sftpFileSendForEDI(localDirPath, fileName, tempFileName, serverRootPath, serverSendReceivePath, true);
    }
    public boolean sftpFileSendForEDI(String localDirPath, String fileName, String tempFileName, String serverRootPath, String serverSendReceivePath, boolean disconnect) throws Exception {
    	/** sftpFileSendForKTnet복사하고 이름만 변경함      테스트후 수정필요없으면 하나로 사용 */
    	logger.info("전송대상 로컬 파일 경로 : " + localDirPath + ", 파일명 : " + fileName + ", 임시파일명 : " + tempFileName + ", 서버 root : " + serverRootPath + ", 서버 send OR recive : " + serverSendReceivePath);
    	
    	boolean isSuccess = false;
        
    	// 전송대상 로컬 파일 경로
        String localPath = localDirPath;
//        String uploadFileContent = "";
        
		//ByteArrayInputStream fis2 = null;
		//OutputStream fos = null;
		
        try {
        	logger.info("EDI SFTP file path : " + serverRootPath + serverSendReceivePath); 
                        
            try {
//            	uploadFileContent = FileUtil.fileRead(localPath, tempFileName, "euc-kr");
            	
//     	        logger.debug("EDI SFTP 전송 내용 : " + uploadFileContent);
     	        File sendFile = new File(localPath + tempFileName);
     	        //파일전송
     	        sftp.upload(serverRootPath + serverSendReceivePath, sendFile);
     	        //연결 해제
     	        if(disconnect){
     	        	sftp.disconnect();
     	        }
     	        isSuccess = true;

    			
     			logger.debug("EDI SFTP 전송 완료 : " + fileName);
            } catch(Exception e) {
            	isSuccess = false;
            	throw e;
            }
        } catch (Exception e) {
        	isSuccess = false;
        	throw e;
        }
        
        return isSuccess;
    }
//    
//    /**
//     * SFTP LS 내의 파일 목록 다운로드 
//     * @param localDirPath
//     * @param serverRootPath
//     * @param serverSendReceivePath
//     * @param receivefileList
//     * @return
//     * @throws Exception
//     */
//    @SuppressWarnings("unchecked")
//	public boolean sftpMultiFileReceiveForKTnet(String localDirPath, String serverRootPath, String serverSendReceivePath
//    		, List<File> receivefileList) throws Exception {
//    	ChannelSftp channelSftp = sftp.getChannelSftp();
//    	
//    	logger.info("수신대상 로컬 파일 경로 : " + localDirPath + ", 서버root : " + serverRootPath + ", 서버send : " + serverSendReceivePath);
//    	
//    	boolean isReceive = true;
//    	
//    	// 전송대상 로컬 파일 경로
//        String localPath = localDirPath;
//        String receiveFileName = null;
//        
//        File receiveFile =  null;
//		File folder = null;
//        
//		InputStream in = null;
//		OutputStream os = null;
//		
//		Vector<ChannelSftp.LsEntry> remoteFiles = null;
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
//     		if("".equals(serverRootPath + serverSendReceivePath)){
//     			remoteFiles = channelSftp.ls("/");
//     		}else{
//     			remoteFiles = channelSftp.ls(serverRootPath + serverSendReceivePath);
//     		}
//            // 파일 목록 조회(KTNET 전용)
//            
//            if(remoteFiles != null) {
//            	logger.info("remoteFiles : " + remoteFiles.size());
//            } else {
//            	logger.info("remoteFiles is null");
//            }
//        	logger.info("KTNET FTP 수신 시작");
//            
//        	if(remoteFiles != null) {
//        		boolean isSuccess = false;
//        		
// 	            for (int i = 0; i < remoteFiles.size(); i++) {
// 	            	try {
//     	            	logger.info("FTP Files : " + remoteFiles.get(i).getFilename());
//
//     	            	receiveFileName = Util.null2str(remoteFiles.get(i).getFilename());
//     	            	
//     	            	receiveFile = new File(localPath, receiveFileName);
//     	            	in = channelSftp.get(remoteFiles.get(i).getFilename());
//     	            	
//     	            	os = new FileOutputStream(receiveFile);
//     	            	
//     	            	byte[] buf = new byte[1024];
//     	            	
//     	                int len = 0;
//     	                // 끝까지 읽어들이면서 File 객체에 내용들을 쓴다
//     	                while ((len = in.read(buf)) > 0){
//     	                	os.write(buf, 0, len);
//     	                }
//     	                
//     	                if(receiveFile.exists()){
//     	                	isSuccess = true;
//     	                }
//     	                
//     	                logger.info("KTNET FTP 수신 결과 : " + isSuccess + ", 수신 받은 파일명 : " + receiveFileName);
//     	               
//     	                // 수신 파일 담기
//     	                receivefileList.add(receiveFile);
// 	            	} catch(Exception e) {
// 	                	logger.error(Util.getPrintStacTraceString(e));
// 	                	isReceive = false;
// 	                	throw e;
// 	                } finally {
// 	        			if(os != null){
// 	        				os.close();
// 	        			}
// 	        			if(in != null){
// 	        				in.close();
// 	        			}
// 	                }
// 	            }
// 	        }
// 			logger.info("KTNET FTP 수신 완료");
//           
//        } catch (Exception e) {
//        	logger.error(Util.getPrintStacTraceString(e));
//        	isReceive = false;
//        	throw e;
//        } finally {
// 			if(in != null){
// 				in.close();
// 			}
// 			if(os != null){
// 				os.close();
// 			}
//         }
//        
//        return isReceive;
//    }
//
	public SFTPUtil getConn() {
		return this.sftp;
	}
//    
//    /**
//     * Download file
//     * @param filePath		Download할 FTP서버 파일 경로
//     * @param fileName		Download할 파일 명
//     * @param request
//     * @return
//     * @throws Exception
//     */
//    public int downloadSFtpFileKTnet(String filePath, String fileName, HttpServletRequest request, String downloadPath) throws Exception {
//        
//    	File fPath  = null;
//        File fDir = null;
//        File f   = null; 
//        BufferedInputStream bis = null;
//        BufferedOutputStream bos = null;
//        
//        //BufferedInputStream fis = null;
//        
//		boolean isSuccess = false;
//        
//        // Download 경로
//        if("".equals(Util.null2str(downloadPath)))
//        		downloadPath = Prop.getMessage("fileshare_download_dir");
//        
//        int result = -1;
//        
//        try {
//        	//logger.info("KTNET SFTP file path : " + filePath + downloadPath);
//        	
//        	fPath = new File(downloadPath);
//            fDir = fPath;
//            fDir.mkdirs();
//            
//            f = new File(downloadPath, fileName);
//        	
//            // 파일 가져오기
//            bis = new BufferedInputStream(sftp.download(filePath, fileName));
//        	isSuccess = true;
//        	
//        	byte[] buffer = new byte[1024];
//        	bos = new BufferedOutputStream(new FileOutputStream(f));
//        	
//    		int readCount;
//    		
//    		// 파일 쓰기
//            while ((readCount = bis.read(buffer)) != -1) {
//            	bos.write(buffer, 0, readCount);
//            }
//            
//    		if (isSuccess) 	result = 1; // 성공     
//    		
//    		//fis = new BufferedInputStream(sftp.download(filePath, "STATE_FILE.XXX"));
//    		
//        } catch (IOException e) {
//            logger.error(Util.getPrintStacTraceString(e));
//        } finally {
//			if(bos != null) try{ bos.close(); } catch (IOException e) {logger.error(Util.getPrintStacTraceString(e));}
//			if(bis != null) try{ bis.close(); } catch (IOException e) {logger.error(Util.getPrintStacTraceString(e));}
//			//if(fis != null) try{ fis.close(); } catch (IOException e) {logger.error(Util.getPrintStacTraceString(e));}
//			
//			sftp.disconnect();
//        }
//        
//        return result;
//    }
	
	/**
     * KTNET SFTP 파일 목록 다운로드
     * ls -> get 방식으로 수정 
     * khr 20220331  
     * @param localDirPath
     * @param serverRootPath
     * @param serverSendReceivePath
     * @param receivefileList
     * @return
     * @throws Exception
     */
    public boolean sftpMultiFileReceiveForKTnetNew(String localDirPath, String serverRootPath, String serverSendReceivePath, List<File> receivefileList) throws Exception {
    	ChannelSftp channelSftp = sftp.getChannelSftp();
    	
    	logger.info(new StringBuffer("수신대상 로컬 파일 경로 : ").append(localDirPath).append(", 서버root : ").append(serverRootPath).append(", 서버send : ").append(serverSendReceivePath));
    	
    	boolean isReceive = true;  //수신여부
    	boolean isSuccess = false; //수신파일 생성여부
    	boolean isExist = false;  //수신파일 내용 확인
    	// 전송대상 로컬 파일 경로
        String localPath = localDirPath;
        String receiveFileName = DateTime.getDate("yyyyMMddHHmmss") +"_" + UUID.randomUUID().toString().substring(0, 10) + ".xml";
        
        File receiveFile = null;
		File folder = null;
		
		InputStream in = null;
		OutputStream os = null;
		
        try {
        	logger.info(new StringBuffer("KTNET FTP file path : ").append(serverRootPath).append(serverSendReceivePath));
        	
        	folder = new File(localPath);
        	
        	// 로컬 폴더 존재 유무 체크
     		if(!folder.isDirectory()) {
     			folder.mkdirs();
     		}
     		
     		// 폴더 이동
     		if("".equals(serverRootPath + serverSendReceivePath)){
     			channelSftp.cd("/");
     		}else{
     			channelSftp.cd(serverRootPath + serverSendReceivePath);
     		}
     		
     		receiveFile = new File(localPath, receiveFileName);
     		//channelSftp.get(receiveFileName, localPath);
     		
     		logger.info("KTNET FTP 수신 시작");
     		in = channelSftp.get(receiveFileName);
     		
     		os = new FileOutputStream(receiveFile);
     		
     		byte[] buf = new byte[1024];
     		
     		int len = 0;
     		// 끝까지 읽어들이면서 File 객체에 내용들을 쓴다
     		while ((len = in.read(buf)) > 0){
     			os.write(buf, 0, len);
     		}

     		String tmp = new String(buf, "EUC-KR");
     		if(tmp.indexOf("KTNET이 전송할 EDI 메시지가 없읍니다.") > -1 || tmp.indexOf("NO DATA TO SEND FROM KTNET") > -1) {
     			isExist = false;
     		}else {
     			isExist = true;
     		}
     		
     		
            if(receiveFile.exists()){
             	isSuccess = true;
            }else {
            	isSuccess = false;
            }
            
            logger.info(new StringBuffer("KTNET FTP 수신 결과 : ").append(isSuccess)
            		.append(", 수신 받은 파일 내용 확인 : ").append(isExist)
            		.append(", 수신 받은 파일명 : ").append(receiveFileName));
            	
            // 수신 파일 담기
            if(isExist) receivefileList.add(receiveFile);
 			logger.info("KTNET FTP 수신 완료");
           
        } catch (Exception e) {
        	logger.error(Util.getPrintStacTraceString(e));
        	isReceive = false;
        	throw e;
        } finally {
 			if(in != null){
 				in.close();
 			}
 			if(os != null){
 				os.close();
 			}
         }
        
        return isReceive;
    }
}