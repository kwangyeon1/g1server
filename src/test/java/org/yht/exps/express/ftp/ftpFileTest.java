package org.yht.exps.express.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

class ftpFileTest {
//
//    @Test
//    void registAirOrderInfo() {
//        // ftp file sending pass mode
//
//    }
//    @Test
//    void testBeanCopy(){
//
//
//// then use Spring BeanUtils to copy and ignore null using our function
//
//    }
//    public static void myCopyProperties(Object src, Object target) {
//        org.springframework.beans.BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
//    }
//    private void coppyTest(){
//       /* PersonVO personVO = new PersonVO(1, "venaCode", 7, "superLucky", "abc.com");
//        Person person = new Person();
//        BeanUtils.copyProperties(personVO, person);*/
//    }
//    public static String[] getNullPropertyNames (Object source) {
//        final BeanWrapper src = new BeanWrapperImpl(source);
//        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
//
//        Set<String> emptyNames = new HashSet<String>();
//        for(java.beans.PropertyDescriptor pd : pds) {
//            Object srcValue = src.getPropertyValue(pd.getName());
//            if (srcValue == null) emptyNames.add(pd.getName());
//        }
//
//        String[] result = new String[emptyNames.size()];
//        return emptyNames.toArray(result);
//    }
//    @Test
//    public void testFtpFileDownload(){
//        String server = "ftp.example.com";
//        int port = 21;
//        String username = "your_username";
//        String password = "your_password";
//        String remoteFilePath = "path_to_remote_file";
//        String localFilePath = "path_to_local_file";
//
//        FTPClient ftpClient = new FTPClient();
//        try {
//            ftpClient.connect(server, port);
//            ftpClient.login(username, password);
//            ftpClient.enterLocalPassiveMode();
//            //ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//
//            OutputStream outputStream = new FileOutputStream(localFilePath);
//            boolean downloaded = ftpClient.retrieveFile(remoteFilePath, outputStream);
//            outputStream.close();
//
//            if (downloaded) {
//                System.out.println("File downloaded successfully.");
//            } else {
//                System.out.println("File download failed.");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (ftpClient.isConnected()) {
//                    ftpClient.logout();
//                    ftpClient.disconnect();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//    @Test
//    public void testFtpFileSendingPassiveMode() throws IOException {
//        // Set up FTP server connection details
//        String ftpIP = "your_ftp_ip";
//        int ftpPort = 21;
//        String ftpID = "your_ftp_id";
//        String ftpPassword = "your_ftp_password";
//
//        // Create FTP client
//        FTPClient ftpClient = new FTPClient();
//
//        try {
//            //  Connect to FTP server
//            ftpClient.connect(ftpIP, ftpPort);
//            ftpClient.login(ftpID, ftpPassword);
//
//            // Set passive mode
//            ftpClient.enterLocalPassiveMode();
//
//            // Upload a file to the FTP server
//            File file = new File("path_to_your_file");
//            InputStream inputStream = new FileInputStream(file);
//            boolean success = ftpClient.storeFile(file.getName(), inputStream);
//            inputStream.close();
//
//            // Verify the success of the file upload
//            assertTrue(success);
//        } finally {
//            // Disconnect from the FTP server
//            ftpClient.logout();
//            ftpClient.disconnect();
//        }
//    }
}