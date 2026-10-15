package org.yht.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileDown {
	
	/**
	 * 파일 다운로드 (절대경로)
	 * @param response
	 * @param fileUrl
	 * @param fileName
	 * @throws Exception
	 */
	public static void down(HttpServletResponse response, String fileUrl) throws Exception{
		if(fileUrl != null){
			File file = new File(fileUrl);
			
			if(file.isFile()){
				down(response, file, file.getName());
			}
		}
	}
	
	/**
	 * 파일 다운로드 (절대경로)
	 * @param response
	 * @param fileUrl
	 * @param fileName
	 * @throws Exception
	 */
	public static void down(HttpServletResponse response, String fileUrl, String fileName) throws Exception{
		if(fileUrl != null && fileName != null){
			File file = new File(fileUrl);
			
			if(file.isFile()){
				down(response, file, fileName);
			}
		}
	}
	
	/**
	 * 파일 다운로드 (파일)
	 * @param response
	 * @param file
	 * @param fileName
	 * @throws Exception
	 */
	public static void down(HttpServletResponse response, File file) throws Exception{
		down(response, file, file.getName());
	}
	
	/**
	 * 파일 다운로드 (파일)
	 * @param response
	 * @param file
	 * @param fileName
	 * @throws Exception
	 */
	public static void down(HttpServletResponse response, File file, String fileName) throws Exception{
		if(file != null){
			FileInputStream fis = null;
			byte[] buf          = new byte[8192];
			int bytesread       = 0;
			int bytesBufferd    = 0;
			OutputStream out    = null;
			
			try{
				fileName = URLEncoder.encode(fileName, "utf-8");
				fileName = Converter.encodingExceptSpecialChar(fileName);
				
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\";");
				response.setHeader("Content-Transfer-Encoding", "binary");
				
				out = response.getOutputStream();
				fis = new FileInputStream(file);
				
				while((bytesread = fis.read(buf)) > -1){
					out.write(buf, 0, bytesread);
					bytesBufferd += bytesread;
					
					if(bytesBufferd > (1024 * 1024)){ // flush after 1M
						bytesBufferd = 0;
						out.flush();
					}
				}
				
				out.flush();
			}catch(Exception e){
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			}finally{
				if(fis != null){try{fis.close();}catch(Exception e){log.error(e.getMessage());}}
				if(out != null){try{out.close();}catch(Exception e){log.error(e.getMessage());}}
			}
		}
	}
}
