package org.yht.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;

//import org.apache.xml.serializer.WriterToUTF8; 

public class FileUtil {
	protected static final Log logger = LogFactory.getLog(FileUtil.class);
	
	public File makeFile(String directory, String fileName, String data) throws FileException {
		File file = null;
//		File dir = new File(directory);
//		WriterToUTF8 outfile = null;
//
//		try {
//			if (dir.exists() == false) {
//				dir.mkdirs();
//			}
//			// logger.debug("dir exists : " + dir.exists());
//			file = new File(directory + fileName);
//			file.createNewFile();
//
//			
//			outfile = new WriterToUTF8(new FileOutputStream(file));
//			
//			outfile.write(data);
//			outfile.flush();
//			//outfile.write(data);
//			//outfile.flush();
//			// logger.debug("file exists :  " + file.exists());
//			// logger.debug("file.getAbsolutePath :  " +
//			// file.getAbsolutePath());
//		} catch (IOException ioe) {
//			logger.error(Util.getPrintStacTraceString(ioe));
//			throw new FileException(ioe.getMessage(), ioe);
//		} finally {
//			if (outfile != null) {
//				try {
//					outfile.close();
//
//				} catch (IOException e) {
//					logger.error(Util.getPrintStacTraceString(e));
//				}
//			}
//		}
		return file;
	}
	
	

	
	/**
	 * 파일을 바이트로 변환
	 * @param file 파일
	 * @return 변경된 바이트값
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		
		if (length > Integer.MAX_VALUE) {
			logger.debug("File is too large...");
		}
		
		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];
		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}
		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}
		// Close the input stream and return bytes
		is.close();
		
		return bytes;
	}
	
	/**
	 * 파일 생성
	 * @param fileStr
	 * @param filePath
	 * @param fileName
	 */
	public static void fileWrite(String fileStr, String filePath, String fileName) {
		fileWrite(fileStr, filePath, fileName, "UTF-8");
	}
	/**
	 * 파일 생성(인코딩)
	 * @param fileStr
	 * @param filePath
	 * @param fileName
	 * @param encoding
	 */
	public static void fileWrite(String fileStr, String filePath, String fileName, String encoding) {
		OutputStreamWriter out = null;
		File pathDir = new File(filePath);
		
		try{
			if(!pathDir.exists()){
				logger.debug("make dir :"+filePath);
				pathDir.mkdirs();
			}

			out = new OutputStreamWriter(new FileOutputStream(filePath+fileName), encoding);
			out.write(fileStr);
			
		} catch(IOException e) {
			logger.error(e.toString());
		}
		finally {
			try	{
				out.close();
			} catch(Exception e) {logger.error(e.toString());}
		}
	}
	
	public static void fileWrite(Workbook wb, String filePath, String fileName) {
		FileOutputStream out = null;
		File pathDir = new File(filePath);
		
		try{
			if(!pathDir.exists()){
				logger.debug("make dir :"+filePath);
				pathDir.mkdirs();
			}
			out = new FileOutputStream(filePath + "/" + fileName);
	        wb.write(out);
	        out.close();			
		} catch(IOException e) {
			logger.error(e.toString());
		}
		finally {
			try	{
				out.close();
			} catch(Exception e) {
				logger.error(e.toString());
			}
		}
	 }
	
	/**
	 * 파일 읽기
	 * @param filePath
	 * @param fileName
	 * @return
	 */
	public static String fileRead(String filePath, String fileName) {
		return fileRead(filePath, fileName, "UTF-8");
	}
	
	/**
	 * 파일 읽기(인코딩)
	 * @param filePath
	 * @param fileName
	 * @param encoding
	 * @return
	 */
	public static String fileRead(String filePath, String fileName, String encoding) {
		
		BufferedReader br = null;
		StringBuffer sf = new StringBuffer();
		String s = null;
		
		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath+fileName), encoding));
			
			while((s=br.readLine())!=null){
				//KTNET FHL문서 엔터값으로 인하여 추가 2016-03-31 HIT
				if(fileName.contains("KTNET_H") || fileName.contains("FNC_LINE") || fileName.contains("awb") ){
					sf.append(s+"\r\n");
				}else{
					sf.append(s);
				}
			}
			
		} catch(IOException e) {
			logger.error(e.toString());
		}
		finally {
			try	{
				if(br != null) try{br.close();}catch(IOException e){logger.error(e.toString());}
			} catch(Exception e) {logger.error(e.toString());}
		}
		return sf.toString();
	}
	
	
	 /**
     * 파일 또는 디렉토리 존재 여부
     *
     * @param strFileName 파일명 또는 디렉토리명
     * @return 존재한다고면 true, 존재하지 않는다면 false
     */
    public static boolean isExists(String strFileName) {
        File objFile = new File(strFileName);
       
        return objFile.exists();
    }
    
    /**
     * 디렉토리 생성하기
     * 
     * 하위디렉토리도 없을때 한번에 다 생성..
     *
     * @param strDirectoryPath 생성할 디렉토리명
     * @return 생성 성공 : true, 실패 : false
     */
    public static boolean createDirectorys(String strDirectoryPath) {
    	boolean retValue = false;
    	
    	retValue = new File(strDirectoryPath).mkdirs();
    	
    	return retValue;
    }
}
