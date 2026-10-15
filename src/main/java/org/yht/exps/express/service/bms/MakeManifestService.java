package org.yht.exps.express.service.bms;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.exps.express.mapper.bms.BmsInMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.bms.carryin.BmsInPlanContainer;
import org.yht.exps.express.model.bms.carryin.InPlan;
import org.yht.exps.express.model.bms.manifest.IhtMf;
import org.yht.exps.express.model.bms.manifest.ImtMf;
import org.yht.exps.express.model.bms.manifest.ThtMf;
import org.yht.exps.express.model.bms.manifest.TmtMf;
import org.yht.exps.express.model.bms.manifest.WhtMf;
import org.yht.exps.express.model.bms.manifest.WmtMf;
import org.yht.exps.express.model.bms.manifest.XxxMf;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.util.Util;

import javax.annotation.Resource;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 파일명 : MakeManifestService
 * 작성일자 : 2024.03.20
 * 작성자 : 박영호
 * 설명 : 적하목록 파일 업로드
 */
@Slf4j
@Service
@Transactional
public class MakeManifestService {

    @Resource(name = "commonService")
    private CommonService commonService;

    @Autowired
    BmsInMapper bmsInMapper;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Resource(name = "fileService")
    private FileService fileService;

    @Value("${custom.file-upload-dir}")
    private String fileUpload_;
//
//    @Value("${custom.db-bulk-processor}")
//    private int dbBulkProcessor_;

    public FileUploadResponse putManifest(MultipartFile file, String entCode, String entBusinessCode, String solution, String userId, String dateFormat, String gmtTime,  String menuId) throws Exception {

        if(file.getSize() > 1024 * 1024 * 15){
            FileUploadResponse result = new FileUploadResponse(FileUploadResponse.RESULT_SIZE_OVER);
            result.setResultText("The file size exceeds the upload size.");
            return result;
        }

        FileUploadResponse result = fileService.storeFile(file, entCode);
        // Load file as Resource
        Path fileLocation = Paths.get(fileUpload_).toAbsolutePath().normalize();
        String fileName = result.getFileName();
        // 파일 읽고 처리
        Path filePath = fileLocation.resolve(entCode + "/" + fileName).normalize();
        org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());

        try {
            // 파일 내용 가져오기
            // euc-kr ---> JAVA String 객체(유니코드)  ---> utf-8로 인코딩 변환.
            // 처음부터 utf-8로 받으면 파일이 ISO-8859-1인 경우에 한글을 못 읽고 있어서 위와 같은 방법을 선택함.
            StringBuilder sb = new StringBuilder();
            FileInputStream fileStream = new FileInputStream(new File(filePath.toString()));
            BufferedReader br = new BufferedReader(new InputStreamReader(fileStream, "EUC-KR"));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + System.lineSeparator());
            }

            fileStream.close();

            String contents = sb.toString().replace(System.getProperty("line.separator"), "");
            byte[] euckrStringBuffer  = contents.getBytes(Charset.forName("euc-kr"));
            String decodedFromEucKr = new String(euckrStringBuffer, "euc-kr");

            byte[] utf8StringBuffer = decodedFromEucKr.getBytes("utf-8");
            contents = new String(utf8StringBuffer, "utf-8");

            //확장자 가져오기
            String ext = fileName.substring(fileName.indexOf(".") + 1).toUpperCase();
            Map<String, Object> odParams = new HashMap<String, Object>();
            odParams.put("entCode", entCode);
            odParams.put("entBusinessCode", entBusinessCode);
            odParams.put("solution", solution);
            odParams.put("userId", userId);
            odParams.put("dateFormat", dateFormat);
            odParams.put("gmtTime", gmtTime);
            odParams.put("menuId", menuId);

            //.THT, .TMT 파일은 KTNET등 사이트에서 다운받는 파일형식(정해진 형식과 구조가 있음)
            switch (ext) {
                case "THT":
                    this.insertThtManifestList(result, contents, odParams);
                    break;
                case "TMT":
                    this.insertTmtManifestList(result, contents, odParams);
                    break;
                case "IMT":
                    this.insertImtManifestList(result, contents, odParams);
                    break;
                case "WMT":
                    this.insertWmtManifestList(result, contents, odParams);
                    break;
                case "IHT":
                    this.insertIhtManifestList(result, contents, odParams);
                    break;
                case "WHT":
                    this.insertWhtManifestList(result, contents, odParams);
                    break;
                case "XXX":
                    this.insertXxxManifestList(result, contents, odParams);
                    break;
            }
        } catch (Exception e) {
            log.error(Util.getPrintStacTraceString(e));
            result.setResult(FileUploadResponse.RESULT_FAIL);
            result.setResultText("Data Make Error!");
        }

        // upload한 파일 삭제
        resource.getFile().delete();

        return new FileUploadResponse(result.getFileName(), null, file.getContentType(), file.getSize(), result.getResult(), result.getResultText());
    }

    private void insertTmtManifestList(FileUploadResponse result, String contents, Map<String, Object> odParams)
            throws Exception {
        int insertCnt = 0;
        JSONParser jsonParser = new JSONParser();

        //TMT는 master - mbl(반복) 구조
        String TMTMaster = "manifest/upload/TMT-master.json";
        JSONArray TMTMasterArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(TMTMaster), "UTF-8"));

        String TMTMbl = "manifest/upload/TMT-mbl.json";
        JSONArray TMTMblArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(TMTMbl), "UTF-8"));

        // MRN 반복
        while (true) {

            if ("".equals(Util.null2str(contents)))
                break;

            TmtMf tm = new TmtMf();
            tm.setEntCode(Util.null2str(odParams.get("entCode")));
            tm.setEntBusinessCode(Util.null2str(odParams.get("entBusinessCode")));
            tm.setEntOfficeCode(Util.null2str(odParams.get("entOfficeCode")));
            tm.setUserId(Util.null2str(odParams.get("userId")));
            tm.setGmtTime(Util.null2str(odParams.get("gmtTime")));

            Class<?> cls = Class.forName(tm.getClass().getName());

            int start = 0;
            for (Object TMTMasterObj : TMTMasterArray) {
                JSONObject jsonLineItem = (JSONObject) TMTMasterObj;
                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                int end = start + iByteLength;
                String func = (String) jsonLineItem.get("func");
                String text = Util.subString(contents, start, iByteLength).trim();
                if (!"".equals(Util.null2str(func))) {
                    Method m = cls.getDeclaredMethod(func, String.class);
                    m.invoke(tm, text);
                }
                start = end;
            }
            // Master String 잘라내기
            contents = Util.subString(contents, start, 0);
            // Master 데이터 이후 MBL 데이터 로드
            int mawbCount = Integer.parseInt(tm.getMawbNo());
            if (!"".equals(contents) && mawbCount > 0) {
                List<TmtMf> tmtMfList = new ArrayList<TmtMf>();

                for (int i = 0; i < mawbCount; i++) {
                    TmtMf mblTm = new TmtMf();
                    mblTm.setEntCode(tm.getEntCode());
                    mblTm.setEntBusinessCode(tm.getEntBusinessCode());
                    mblTm.setEntOfficeCode(tm.getEntOfficeCode());
                    mblTm.setBound(tm.getBound());
                    mblTm.setMrn(tm.getMrn());
                    mblTm.setFlightNo(tm.getFlightNo());
                    mblTm.setDateArrive(tm.getDateArrive());
                    mblTm.setOwnerOperator(tm.getOwnerOperator());
                    mblTm.setGrossWeight(tm.getGrossWeight());
                    mblTm.setGrossPieces(tm.getGrossPieces());
                    mblTm.setMawbNo(tm.getMawbNo());
                    mblTm.setUserId(tm.getUserId());

                    start = 0;
                    for (Object TMTMblObj : TMTMblArray) {

                        JSONObject jsonLineItem = (JSONObject) TMTMblObj;
                        int iByteLength = Util.null2int(jsonLineItem.get("size"));
                        int end = start + iByteLength;
                        String func = (String) jsonLineItem.get("func");
                        String text = Util.subString(contents, start, iByteLength).trim();
                        if (!"".equals(Util.null2str(func))) {
                            Method m = cls.getDeclaredMethod(func, String.class);
                            m.invoke(mblTm, text);
                        }
                        start = end;
                    }
                    tmtMfList.add(mblTm);
                    contents = Util.subString(contents, start, 0);
                    log.info("mblTm: {}", mblTm.toString());
                }
                tm.setList(tmtMfList);
                log.info("MasterTMT 객체: {}",tm.toString());
                String logVal = "개별 TmtMf 리스트: " + tmtMfList.toString();
                log.info(logVal);
            } else {
                result.setResult(FileUploadResponse.RESULT_FAIL);
                result.setResultText("Data Make Fail!");
            }

            List<TmtMf> list = tm.getList();
            String logVal = "최종TmtList: " + list.toString();
            log.info(logVal);
            for (int j =0; j <list.size(); j++) {
            	String mrn = list.get(j).getMrn();
                String msn = list.get(j).getMsn();
                msn = Util.lpad(msn == null ? "" : msn, 4, '0');
                String cargoReferenceNo = Util.null2str(mrn) + Util.null2str(msn);
                
                int cnt = bmsInMapper.checkCargoReferenceNoFromInAndInPlan(cargoReferenceNo);
                if(cnt > 0) {
                	log.info("["+ j +"]중복된 화물관리번호 skip CARGO_REFERENCE_NO: ", cargoReferenceNo);
                	continue;
                }
                
                InPlan inPlan = new InPlan();
                inPlan.setEntBusinessCode((String) odParams.get("entBusinessCode"));
                inPlan.setBlNo(list.get(j).getMawb());
                inPlan.setMblNo(list.get(j).getMawb());
                inPlan.setOdJobNo(list.get(j).getMawb());
                inPlan.setCargoReferenceNo(cargoReferenceNo);
                inPlan.setVesselFlight(Util.null2str(list.get(j).getFlightNo()));
                String pkgType ="";
                String flightNo = Util.null2str(list.get(j).getFlightNo());
                if (flightNo.length() >= 3 && Character.isDigit(flightNo.charAt(2))) {
                    pkgType = "CT";
                } else {
                    pkgType = "GT";
                }
                inPlan.setPkgType(Util.null2str(pkgType));
                inPlan.setArDate(Util.null2str(convertDateFormat(list.get(j).getDateArrive())));
                inPlan.setExCt(Util.null2int(Integer.parseInt(list.get(j).getPiecesNo())));
                inPlan.setExWt(Util.null2dbl(Double.parseDouble(list.get(j).getWeightKg())));
                inPlan.setCommodityItemLoc(Util.null2str(list.get(j).getGoodsDescript()));
                inPlan.setConsigneeName(Util.null2str(list.get(j).getConsigneeName()));
                inPlan.setConsigneeAddress1(Util.null2str(list.get(j).getConsigneeAddr()));
                inPlan.setLastLocation(Util.null2str(list.get(j).getWarehouseCode()));
                inPlan.setRegisterUserId(Util.null2str(list.get(j).getUserId()));
                inPlan.setUpdateUserId(Util.null2str(list.get(j).getUserId()));
                
                inPlan.setConsigneeBusinessId("");
                inPlan.setCdType("N");
                inPlan.setBigSizeYn("N");
                try {
                    bmsInMapper.insertInPlan(inPlan);
                    insertCnt++;

                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        }
        log.info("데이터 입력건수: {}", insertCnt);
        String resultText = Integer.toString(insertCnt);
        result.setResultText(resultText);
    }

    private void insertImtManifestList(FileUploadResponse result, String contents, Map<String, Object> odParams)
            throws Exception {
    	
        int insertCnt = 0;
        JSONParser jsonParser = new JSONParser();

        //TMT는 master - mbl(반복) 구조
        String IMTMaster = "manifest/upload/IMT-master.json";
        JSONArray IMTMasterArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(IMTMaster), "UTF-8"));

        String IMTMbl = "manifest/upload/IMT-mbl.json";
        JSONArray IMTMblArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(IMTMbl), "UTF-8"));

        // MRN 반복
        while (true) {

            if ("".equals(Util.null2str(contents)))
                break;

            ImtMf tm = new ImtMf();
            tm.setEntCode(Util.null2str(odParams.get("entCode")));
            tm.setEntBusinessCode(Util.null2str(odParams.get("entBusinessCode")));
            tm.setEntOfficeCode(Util.null2str(odParams.get("entOfficeCode")));
            tm.setUserId(Util.null2str(odParams.get("userId")));
            tm.setGmtTime(Util.null2str(odParams.get("gmtTime")));

            Class<?> cls = Class.forName(tm.getClass().getName());

            int start = 0;
            for (Object IMTMasterObj : IMTMasterArray) {
                JSONObject jsonLineItem = (JSONObject) IMTMasterObj;
                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                int end = start + iByteLength;
                String func = (String) jsonLineItem.get("func");
                String text = Util.subString(contents, start, iByteLength).trim();
                if (!"".equals(Util.null2str(func))) {
                    Method m = cls.getDeclaredMethod(func, String.class);
                    m.invoke(tm, text);
                }
                start = end;
            }
            // Master String 잘라내기
            contents = Util.subString(contents, start, 0);
            // Master 데이터 이후 MBL 데이터 로드
            int mawbCount = Integer.parseInt(tm.getMawbNo());
            if (!"".equals(contents) && mawbCount > 0) {
                List<ImtMf> imtMfList = new ArrayList<ImtMf>();

                for (int i = 0; i < mawbCount; i++) {
                	ImtMf mblTm = new ImtMf();
                    mblTm.setEntCode(tm.getEntCode());
                    mblTm.setEntBusinessCode(tm.getEntBusinessCode());
                    mblTm.setEntOfficeCode(tm.getEntOfficeCode());
                    mblTm.setBound("SEA");
                    mblTm.setMrn(tm.getMrn());
                    mblTm.setFlightNo(tm.getFlightNo());
                    mblTm.setDateArrive(tm.getDateArrive());
                    mblTm.setMarksRegist(tm.getMarksRegist());
                    mblTm.setOwnerOperator(tm.getOwnerOperator());
                    mblTm.setGrossWeight(tm.getGrossWeight());
                    mblTm.setGrossPieces(tm.getGrossPieces());
                    mblTm.setMawbNo(tm.getMawbNo());
                    mblTm.setMawbCnslNo(tm.getMawbCnslNo());
                    mblTm.setUserId(tm.getUserId());

                    start = 0;
                    for (Object IMTMblObj : IMTMblArray) {

                        JSONObject jsonLineItem = (JSONObject) IMTMblObj;
                        int iByteLength = Util.null2int(jsonLineItem.get("size"));
                        int end = start + iByteLength;
                        String func = (String) jsonLineItem.get("func");
                        String text = Util.subString(contents, start, iByteLength).trim();
                        if (!"".equals(Util.null2str(func))) {
                            Method m = cls.getDeclaredMethod(func, String.class);
                            m.invoke(mblTm, text);
                        }
                        start = end;
                    }
                    imtMfList.add(mblTm);
                    contents = Util.subString(contents, start, 0);
                    log.info("mblTm: {}", mblTm.toString());
                }
                tm.setList(imtMfList);
                log.info("MasterIMT 객체: {}",tm.toString());
                log.info("개별 ImtMf 리스트: {}", imtMfList.toString());
            } else {
                result.setResult(FileUploadResponse.RESULT_FAIL);
                result.setResultText("Data Make Fail!");
            }

            List<ImtMf> list = tm.getList();
            log.info("최종TmtList: {}", list.toString());
            for (int j =0; j <list.size(); j++) {
                String mrn = list.get(j).getMrn();
                String msn = list.get(j).getMsn();
                msn = Util.lpad(msn == null ? "" : msn, 4, '0');
                String cargoReferenceNo = Util.null2str(mrn) + Util.null2str(msn);
                
                int cnt = bmsInMapper.checkCargoReferenceNoFromInAndInPlan(cargoReferenceNo);
                if(cnt > 0) {
                	log.info("["+ j +"]중복된 화물관리번호 skip CARGO_REFERENCE_NO: ", cargoReferenceNo);
                	continue;
                }
                
                InPlan inPlan = new InPlan();
                inPlan.setEntBusinessCode((String) odParams.get("entBusinessCode"));
                inPlan.setBlNo(list.get(j).getMawb());
                inPlan.setMblNo(list.get(j).getMawb());
                inPlan.setOdJobNo(list.get(j).getMawb());
                inPlan.setCargoReferenceNo(cargoReferenceNo);
                inPlan.setVesselFlight(Util.null2str(list.get(j).getFlightNo()));
                String pkgType ="";
                String flightNo = Util.null2str(list.get(j).getFlightNo());
                if (flightNo.length() >= 3 && Character.isDigit(flightNo.charAt(2))) {
                    pkgType = "CT";
                } else {
                    pkgType = "GT";
                }
                inPlan.setPkgType(Util.null2str(pkgType));
                inPlan.setArDate(Util.null2str(convertDateFormat(list.get(j).getDateArrive())));
                inPlan.setExCt(Util.null2int(Integer.parseInt(list.get(j).getPiecesNo())));
                inPlan.setExWt(Util.null2dbl(Double.parseDouble(list.get(j).getWeightKg())));
                inPlan.setCommodityItemLoc(Util.null2str(list.get(j).getGoodsDescript()));
                inPlan.setConsigneeName(Util.null2str(list.get(j).getConsigneeName()));
                inPlan.setConsigneeAddress1(Util.null2str(list.get(j).getConsigneeAddr()));
                inPlan.setRegisterUserId(Util.null2str(list.get(j).getUserId()));
                inPlan.setUpdateUserId(Util.null2str(list.get(j).getUserId()));
                
                inPlan.setConsigneeBusinessId("");
                inPlan.setCdType("N");
                inPlan.setBigSizeYn("N");
                
                inPlan.setShipperName(Util.null2str(list.get(j).getShipperName()));
                inPlan.setShipperAddress1(Util.null2str(list.get(j).getShipperAddr()));
                inPlan.setShipperTel(Util.null2str(list.get(j).getShipperTelFax()));
                inPlan.setNotifyName(Util.null2str(list.get(j).getNotifyName()));
                inPlan.setNotifyAddress1(Util.null2str(list.get(j).getNotifyAddr()));
                inPlan.setNotifyTel(Util.null2str(list.get(j).getNotifyTelFax()));
                inPlan.setLastLocation(Util.null2str(list.get(j).getWarehouseCode()));
                
                try {
                    bmsInMapper.insertInPlan(inPlan);
                    insertCnt++;

                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        }
        log.info("데이터 입력건수: {}", insertCnt);
        String resultText = Integer.toString(insertCnt);
        result.setResultText(resultText);
    }
    
    private void insertWmtManifestList(FileUploadResponse result, String contents, Map<String, Object> odParams)
            throws Exception {
        int insertCnt = 0;
        JSONParser jsonParser = new JSONParser();

        //TMT는 master - mbl(반복) 구조
        String WMTMaster = "manifest/upload/WMT-master.json";
        JSONArray WMTMasterArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(WMTMaster), "UTF-8"));

        String WMTMbl = "manifest/upload/WMT-mbl.json";
        JSONArray WMTMblArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(WMTMbl), "UTF-8"));

        // MRN 반복
        while (true) {

            if ("".equals(Util.null2str(contents)))
                break;

            WmtMf tm = new WmtMf();
            tm.setEntCode(Util.null2str(odParams.get("entCode")));
            tm.setEntBusinessCode(Util.null2str(odParams.get("entBusinessCode")));
            tm.setEntOfficeCode(Util.null2str(odParams.get("entOfficeCode")));
            tm.setUserId(Util.null2str(odParams.get("userId")));
            tm.setGmtTime(Util.null2str(odParams.get("gmtTime")));

            Class<?> cls = Class.forName(tm.getClass().getName());

            int start = 0;
            for (Object WMTMasterObj : WMTMasterArray) {
                JSONObject jsonLineItem = (JSONObject) WMTMasterObj;
                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                int end = start + iByteLength;
                String func = (String) jsonLineItem.get("func");
                String text = Util.subString(contents, start, iByteLength).trim();
                if (!"".equals(Util.null2str(func))) {
                    Method m = cls.getDeclaredMethod(func, String.class);
                    m.invoke(tm, text);
                }
                start = end;
            }
            // Master String 잘라내기
            contents = Util.subString(contents, start, 0);
            // Master 데이터 이후 MBL 데이터 로드
            int mawbCount = Integer.parseInt(tm.getMawbNo());
            if (!"".equals(contents) && mawbCount > 0) {
                List<WmtMf> wmtMfList = new ArrayList<WmtMf>();

                for (int i = 0; i < mawbCount; i++) {
                	WmtMf mblTm = new WmtMf();
                    mblTm.setEntCode(tm.getEntCode());
                    mblTm.setEntBusinessCode(tm.getEntBusinessCode());
                    mblTm.setEntOfficeCode(tm.getEntOfficeCode());
                    mblTm.setBound("SEA");
                    mblTm.setMrn(tm.getMrn());
                    mblTm.setFlightNo(tm.getFlightNo());
                    mblTm.setDateArrive(tm.getDateArrive());
                    mblTm.setOwnerOperator(tm.getOwnerOperator());
                    mblTm.setGrossWeight(tm.getGrossWeight());
                    mblTm.setGrossPieces(tm.getGrossPieces());
                    mblTm.setMawbNo(tm.getMawbNo());
                    mblTm.setUserId(tm.getUserId());

                    start = 0;
                    for (Object WMTMblObj : WMTMblArray) {

                        JSONObject jsonLineItem = (JSONObject) WMTMblObj;
                        int iByteLength = Util.null2int(jsonLineItem.get("size"));
                        int end = start + iByteLength;
                        String func = (String) jsonLineItem.get("func");
                        String text = Util.subString(contents, start, iByteLength).trim();
                        if (!"".equals(Util.null2str(func))) {
                            Method m = cls.getDeclaredMethod(func, String.class);
                            m.invoke(mblTm, text);
                        }
                        start = end;
                    }
                    wmtMfList.add(mblTm);
                    contents = Util.subString(contents, start, 0);
                    log.info("mblTm: {}", mblTm.toString());
                }
                tm.setList(wmtMfList);
                log.info("MasterWMT 객체: {}",tm.toString());
                log.info("개별 WmtMf 리스트: {}", wmtMfList.toString());
            } else {
                result.setResult(FileUploadResponse.RESULT_FAIL);
                result.setResultText("Data Make Fail!");
            }

            List<WmtMf> list = tm.getList();
            log.info("최종WmtList: {}", list.toString());
            for (int j =0; j <list.size(); j++) {
                String mrn = list.get(j).getMrn();
                String msn = list.get(j).getMsn();
                msn = Util.lpad(msn == null ? "" : msn, 4, '0');
                String cargoReferenceNo = Util.null2str(mrn) + Util.null2str(msn);
                
                int cnt = bmsInMapper.checkCargoReferenceNoFromInAndInPlan(cargoReferenceNo);
                if(cnt > 0) {
                	log.info("["+ j +"]중복된 화물관리번호 skip CARGO_REFERENCE_NO: ", cargoReferenceNo);
                	continue;
                }
                
                InPlan inPlan = new InPlan();
                inPlan.setEntBusinessCode((String) odParams.get("entBusinessCode"));
                inPlan.setBlNo(list.get(j).getMawb());
                inPlan.setMblNo(list.get(j).getMawb());
                inPlan.setOdJobNo(list.get(j).getMawb());
                inPlan.setCargoReferenceNo(cargoReferenceNo);
                inPlan.setVesselFlight(Util.null2str(list.get(j).getFlightNo()));
                String pkgType ="";
                String flightNo = Util.null2str(list.get(j).getFlightNo());
                if (flightNo.length() >= 3 && Character.isDigit(flightNo.charAt(2))) {
                    pkgType = "CT";
                } else {
                    pkgType = "GT";
                }
                inPlan.setPkgType(Util.null2str(pkgType));
                inPlan.setArDate(Util.null2str(convertDateFormat(list.get(j).getDateArrive())));
                inPlan.setExCt(Util.null2int(Integer.parseInt(list.get(j).getPiecesNo())));
                inPlan.setExWt(Util.null2dbl(Double.parseDouble(list.get(j).getWeightKg())));
                inPlan.setCommodityItemLoc(Util.null2str(list.get(j).getGoodsDescript()));
                inPlan.setConsigneeName(Util.null2str(list.get(j).getConsigneeName()));
                inPlan.setConsigneeAddress1(Util.null2str(list.get(j).getConsigneeAddr()));
                inPlan.setRegisterUserId(Util.null2str(list.get(j).getUserId()));
                inPlan.setUpdateUserId(Util.null2str(list.get(j).getUserId()));
                
                inPlan.setConsigneeBusinessId("");
                inPlan.setCdType("N");
                inPlan.setBigSizeYn("N");
                
                inPlan.setShipperName(Util.null2str(list.get(j).getShipperName()));
                inPlan.setShipperAddress1(Util.null2str(list.get(j).getShipperAddr()));
                inPlan.setShipperTel(Util.null2str(list.get(j).getShipperTelFax()));
                inPlan.setNotifyName(Util.null2str(list.get(j).getNotifyName()));
                inPlan.setNotifyAddress1(Util.null2str(list.get(j).getNotifyAddr()));
                inPlan.setNotifyTel(Util.null2str(list.get(j).getNotifyTelFax()));
                inPlan.setLastLocation(Util.null2str(list.get(j).getWarehouseCode()));
                
                try {
                    bmsInMapper.insertInPlan(inPlan);
                    insertCnt++;

                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        }
        log.info("데이터 입력건수: {}", insertCnt);
        String resultText = Integer.toString(insertCnt);
        result.setResultText(resultText);
    }

    private void insertThtManifestList(FileUploadResponse result, String contents, Map<String, Object> odParams)
            throws Exception {
        JSONParser jsonParser = new JSONParser();
        int insertCnt = 0;

        //THT는 master - mbl(반복) - hbl(반복) 구조
        String THTMaster = "manifest/upload/THT-master.json";
        JSONArray THTMasterArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(THTMaster), "UTF-8"));

        String THTMbl = "manifest/upload/THT-mbl.json";
        JSONArray THTMblArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(THTMbl), "UTF-8"));

        String THTHbl = "manifest/upload/THT-hbl.json";
        JSONArray THTHblArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(THTHbl), "UTF-8"));

        // MRN 반복
        while (true) {

            if ("".equals(Util.null2str(contents)))
                break;
            ThtMf th = new ThtMf();
            th.setEntCode(Util.null2str(odParams.get("entCode")));
            th.setEntBusinessCode(Util.null2str(odParams.get("entBusinessCode")));
            th.setEntOfficeCode(Util.null2str(odParams.get("entOfficeCode")));
            th.setUserId(Util.null2str(odParams.get("userId")));
            th.setGmtTime(Util.null2str(odParams.get("gmtTime")));

            Class<?> cls = Class.forName(th.getClass().getName());

            int start = 0;
            for(Object ThtMasterObj : THTMasterArray) {
                JSONObject jsonLineItem = (JSONObject) ThtMasterObj;
                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                int end = start + iByteLength;
                String func = (String) jsonLineItem.get("func");
                String text = Util.subString(contents, start, iByteLength).trim();

                if(!"".equals(Util.null2str(func))) {
                    Method m = cls.getDeclaredMethod(func, String.class);
                    m.invoke(th, text);
                }
                start = end;
            }

            // Master String 잘라내기
            contents = Util.subString(contents, start, 0);
            // Master 데이터 이후 MBL 데이터 로드
            int mawbCount = Integer.parseInt(th.getMawbNo());
            log.info("mawbCount ::: {}", mawbCount);
            if (!"".equals(contents) && mawbCount > 0) {
                List<ThtMf> thList = new ArrayList<ThtMf>();

                for (int i = 0; i < mawbCount; i++) {
                    ThtMf mblTh = new ThtMf();

                    start = 0;
                    for (Object THTMblObj : THTMblArray) {

                        JSONObject jsonLineItem = (JSONObject) THTMblObj;
                        int iByteLength = Util.null2int(jsonLineItem.get("size"));
                        int end = start + iByteLength;
                        String func = (String) jsonLineItem.get("func");

                        String text = Util.subString(contents, start, iByteLength).trim();

                        if (!"".equals(Util.null2str(func))) {
                            Method m = cls.getDeclaredMethod(func, String.class);
                            m.invoke(mblTh, text);
                        }
                        start = end;
                    }
                    contents = Util.subString(contents, start, 0);
                    log.info("mblth:::: {}",mblTh.toString());
                    int hawbCount = Integer.parseInt(mblTh.getHawbNo());
                    log.info("hawbCount ::: {}", hawbCount);
                    if (!"".equals(contents) && hawbCount > 0) {
                        for (int j = 0; j < hawbCount; j++) {
                            ThtMf hblTh = new ThtMf();
                            // 공통
                            hblTh.setEntCode(th.getEntCode());
                            hblTh.setEntBusinessCode(th.getEntBusinessCode());
                            hblTh.setEntOfficeCode(th.getEntOfficeCode());
                            hblTh.setDateArrive(th.getDateArrive());
                            hblTh.setFlightNo(th.getFlightNo());
                            hblTh.setBound("AIR");
                            hblTh.setUserId(th.getUserId());
                            hblTh.setGmtTime(th.getGmtTime());
                            hblTh.setMawb(mblTh.getMawb());
                            hblTh.setFwdCode(mblTh.getFwdCode());
                            hblTh.setFwdName(mblTh.getFwdName());
                            hblTh.setUnloadSpot(mblTh.getUnloadSpot());

                            start = 0;
                            for (Object THTHblObj : THTHblArray) {

                                JSONObject jsonLineItem = (JSONObject) THTHblObj;
                                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                                int end = start + iByteLength;
                                String func = (String) jsonLineItem.get("func");

                                String text = Util.subString(contents, start, iByteLength).trim();

                                if (!"".equals(Util.null2str(func))) {
                                    Method m = cls.getDeclaredMethod(func, String.class);
                                    m.invoke(hblTh, text);
                                }
                                start = end;
                            }
                            if ("".equals(hblTh.getHsn()) || hblTh.getHsn() == null) {
                                hblTh.setCargoReferenceNo(th.getMrn() + Util.lpad(mblTh.getMsn(), 4, '0'));
                            } else {
                                hblTh.setCargoReferenceNo(th.getMrn() + Util.lpad(mblTh.getMsn(), 4, '0')
                                        + Util.lpad(hblTh.getHsn(), 4, '0'));
                            }
                            thList.add(hblTh);
                            contents = Util.subString(contents, start, 0);
                        }
                    }
                    th.setHList(thList);
                }
            } else {
                result.setResult(FileUploadResponse.RESULT_FAIL);
                result.setResultText("Data Make Fail!");
            }

            List<ThtMf> list = th.getHList();
            String logVal = "HblList:::::: " + list.toString();
            log.info(logVal);
            for (int j =0; j <list.size(); j++) {
                String cargoReferenceNo = list.get(j).getCargoReferenceNo();
                
                int cnt = bmsInMapper.checkCargoReferenceNoFromInAndInPlan(cargoReferenceNo);
                if(cnt > 0) {
                	log.info("["+ j +"]중복된 화물관리번호 skip CARGO_REFERENCE_NO: ", cargoReferenceNo);
                	continue;
                }
                
                InPlan inPlan = new InPlan();
                inPlan.setEntBusinessCode((String) odParams.get("entBusinessCode"));
                inPlan.setBlNo(list.get(j).getHawb());
                inPlan.setOdJobNo(list.get(j).getHawb());
                inPlan.setMblNo(list.get(j).getMawb());
                inPlan.setCargoReferenceNo(list.get(j).getCargoReferenceNo());
                inPlan.setVesselFlight(Util.null2str(list.get(j).getFlightNo()));
                String pkgType ="";
                String flightNo = Util.null2str(list.get(j).getFlightNo());
                if (flightNo.length() >= 3 && Character.isDigit(flightNo.charAt(2))) {
                    pkgType = "CT";
                } else {
                    pkgType = "GT";
                }
                inPlan.setPkgType(Util.null2str(pkgType));
                inPlan.setArDate(Util.null2str(convertDateFormat(list.get(j).getDateArrive())));
                inPlan.setExCt(Util.null2int(Integer.parseInt(list.get(j).getPiecesNo())));
                inPlan.setExWt(Util.null2dbl(Double.parseDouble(list.get(j).getGrossWeight())));
                inPlan.setCommodityItemLoc(Util.null2str(list.get(j).getGoodsDescript()));
                inPlan.setConsigneeName(Util.null2str(list.get(j).getConsigneeName()));
                inPlan.setConsigneeAddress1(Util.null2str(list.get(j).getConsigneeAddr()));
                inPlan.setConsigneeBusinessId(Util.null2str(list.get(j).getConsigneeCode()));
                inPlan.setCdType("N");
                inPlan.setBigSizeYn("N");
                inPlan.setLastLocation(Util.null2str(list.get(j).getWarehouseCode()));
                inPlan.setRegisterUserId(Util.null2str(list.get(j).getUserId()));
                inPlan.setUpdateUserId(Util.null2str(list.get(j).getUserId()));
                try {
                    bmsInMapper.insertInPlan(inPlan);
                    insertCnt++;
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        }
        log.info("데이터 입력건수: {}", insertCnt);
        String resultText = Integer.toString(insertCnt);
        result.setResultText(resultText);
    }

    private void insertIhtManifestList(FileUploadResponse result, String contents, Map<String, Object> odParams)
            throws Exception {
        JSONParser jsonParser = new JSONParser();
        int insertCnt = 0;

        //THT는 master - mbl(반복) - hbl(반복) 구조
        String IHTMaster = "manifest/upload/IHT-master.json";
        JSONArray IHTMasterArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(IHTMaster), "UTF-8"));

        String IHTMbl = "manifest/upload/IHT-mbl.json";
        JSONArray IHTMblArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(IHTMbl), "UTF-8"));

        String IHTHbl = "manifest/upload/IHT-hbl.json";
        JSONArray IHTHblArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(IHTHbl), "UTF-8"));

        // MRN 반복
        while (true) {

            if ("".equals(Util.null2str(contents)))
                break;
            IhtMf th = new IhtMf();
            th.setEntCode(Util.null2str(odParams.get("entCode")));
            th.setEntBusinessCode(Util.null2str(odParams.get("entBusinessCode")));
            th.setEntOfficeCode(Util.null2str(odParams.get("entOfficeCode")));
            th.setUserId(Util.null2str(odParams.get("userId")));
            th.setGmtTime(Util.null2str(odParams.get("gmtTime")));

            Class<?> cls = Class.forName(th.getClass().getName());

            int start = 0;
            for(Object IhtMasterObj : IHTMasterArray) {
                JSONObject jsonLineItem = (JSONObject) IhtMasterObj;
                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                int end = start + iByteLength;
                String func = (String) jsonLineItem.get("func");
                String text = Util.subString(contents, start, iByteLength).trim();

                if(!"".equals(Util.null2str(func))) {
                    Method m = cls.getDeclaredMethod(func, String.class);
                    m.invoke(th, text);
                }
                start = end;
            }

            // Master String 잘라내기
            contents = Util.subString(contents, start, 0);
            // Master 데이터 이후 MBL 데이터 로드
            int mawbCount = Integer.parseInt(th.getMawbNo());
            log.info("mawbCount ::: {}", mawbCount);
            if (!"".equals(contents) && mawbCount > 0) {
                List<IhtMf> thList = new ArrayList<IhtMf>();

                for (int i = 0; i < mawbCount; i++) {
                	IhtMf mblTh = new IhtMf();

                    start = 0;
                    for (Object IHTMblObj : IHTMblArray) {

                        JSONObject jsonLineItem = (JSONObject) IHTMblObj;
                        int iByteLength = Util.null2int(jsonLineItem.get("size"));
                        int end = start + iByteLength;
                        String func = (String) jsonLineItem.get("func");

                        String text = Util.subString(contents, start, iByteLength).trim();

                        if (!"".equals(Util.null2str(func))) {
                            Method m = cls.getDeclaredMethod(func, String.class);
                            m.invoke(mblTh, text);
                        }
                        start = end;
                    }
                    contents = Util.subString(contents, start, 0);
                    log.info("mblth:::: {}",mblTh.toString());
                    int hawbCount = Integer.parseInt(mblTh.getHawbNo());
                    log.info("hawbCount ::: {}", hawbCount);
                    if (!"".equals(contents) && hawbCount > 0) {
                        for (int j = 0; j < hawbCount; j++) {
                        	IhtMf hblTh = new IhtMf();
                            // 공통
                            hblTh.setEntCode(th.getEntCode());
                            hblTh.setEntBusinessCode(th.getEntBusinessCode());
                            hblTh.setEntOfficeCode(th.getEntOfficeCode());
                            hblTh.setDateArrive(th.getDateArrive());
                            hblTh.setFlightNo(th.getFlightNo());
                            hblTh.setBound("AIR");
                            hblTh.setUserId(th.getUserId());
                            hblTh.setGmtTime(th.getGmtTime());
                            hblTh.setMawb(mblTh.getMawb());
                            hblTh.setFwdCode(mblTh.getFwdCode());
                            hblTh.setFwdName(mblTh.getFwdName());
                            hblTh.setUnloadSpot(mblTh.getUnloadSpot());

                            start = 0;
                            for (Object IHTHblObj : IHTHblArray) {

                                JSONObject jsonLineItem = (JSONObject) IHTHblObj;
                                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                                int end = start + iByteLength;
                                String func = (String) jsonLineItem.get("func");

                                String text = Util.subString(contents, start, iByteLength).trim();

                                if (!"".equals(Util.null2str(func))) {
                                    Method m = cls.getDeclaredMethod(func, String.class);
                                    m.invoke(hblTh, text);
                                }
                                start = end;
                            }
                            if ("".equals(hblTh.getHsn()) || hblTh.getHsn() == null) {
                                hblTh.setCargoReferenceNo(th.getMrn() + Util.lpad(mblTh.getMsn(), 4, '0'));
                            } else {
                                hblTh.setCargoReferenceNo(th.getMrn() + Util.lpad(mblTh.getMsn(), 4, '0')
                                        + Util.lpad(hblTh.getHsn(), 4, '0'));
                            }
                            thList.add(hblTh);
                            contents = Util.subString(contents, start, 0);
                        }
                    }
                    th.setHList(thList);
                }
            } else {
                result.setResult(FileUploadResponse.RESULT_FAIL);
                result.setResultText("Data Make Fail!");
            }

            List<IhtMf> list = th.getHList();
            log.info("HblList:::::: {}",list.toString());
            for (int j =0; j <list.size(); j++) {
                String cargoReferenceNo = list.get(j).getCargoReferenceNo();
                
                int cnt = bmsInMapper.checkCargoReferenceNoFromInAndInPlan(cargoReferenceNo);
                if(cnt > 0) {
                	log.info("["+ j +"]중복된 화물관리번호 skip CARGO_REFERENCE_NO: ", cargoReferenceNo);
                	continue;
                }
                
                InPlan inPlan = new InPlan();
                inPlan.setEntBusinessCode((String) odParams.get("entBusinessCode"));
                inPlan.setBlNo(list.get(j).getHawb());
                inPlan.setOdJobNo(list.get(j).getHawb());
                inPlan.setMblNo(list.get(j).getMawb());
                inPlan.setCargoReferenceNo(list.get(j).getCargoReferenceNo());
                inPlan.setVesselFlight(Util.null2str(list.get(j).getFlightNo()));
                String pkgType ="";
                String flightNo = Util.null2str(list.get(j).getFlightNo());
                if (flightNo.length() >= 3 && Character.isDigit(flightNo.charAt(2))) {
                    pkgType = "CT";
                } else {
                    pkgType = "GT";
                }
                inPlan.setPkgType(Util.null2str(pkgType));
                inPlan.setArDate(Util.null2str(convertDateFormat(list.get(j).getDateArrive())));
                inPlan.setExCt(Util.null2int(Integer.parseInt(list.get(j).getPiecesNo())));
                inPlan.setExWt(Util.null2dbl(Double.parseDouble(list.get(j).getGrossWeight())));
                inPlan.setCommodityItemLoc(Util.null2str(list.get(j).getGoodsDescript()));
                inPlan.setConsigneeName(Util.null2str(list.get(j).getConsigneeName()));
                inPlan.setConsigneeAddress1(Util.null2str(list.get(j).getConsigneeAddr()));
                inPlan.setConsigneeBusinessId(Util.null2str(list.get(j).getConsigneeCode()));
                inPlan.setCdType("N");
                inPlan.setBigSizeYn("N");
                inPlan.setRegisterUserId(Util.null2str(list.get(j).getUserId()));
                inPlan.setUpdateUserId(Util.null2str(list.get(j).getUserId()));
                
                inPlan.setShipperName(Util.null2str(list.get(j).getShipperName()));
                inPlan.setShipperAddress1(Util.null2str(list.get(j).getShipperAddr()));
                inPlan.setShipperTel(Util.null2str(list.get(j).getShipperTelFax()));
                inPlan.setNotifyCode(Util.null2str(list.get(j).getNotifyCode()));
                inPlan.setNotifyName(Util.null2str(list.get(j).getNotifyName()));
                inPlan.setNotifyAddress1(Util.null2str(list.get(j).getNotifyAddr()));
                inPlan.setNotifyTel(Util.null2str(list.get(j).getNotifyTelFax()));
                inPlan.setLastLocation(Util.null2str(list.get(j).getWarehouseCode()));
                
                try {
                    bmsInMapper.insertInPlan(inPlan);
                    insertCnt++;
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        }
        log.info("데이터 입력건수: {}", insertCnt);
        String resultText = Integer.toString(insertCnt);
        result.setResultText(resultText);
    }

    private void insertWhtManifestList(FileUploadResponse result, String contents, Map<String, Object> odParams)
            throws Exception {
        JSONParser jsonParser = new JSONParser();
        int insertCnt = 0;

        //THT는 master - mbl(반복) - hbl(반복) 구조
        String WHTMaster = "manifest/upload/WHT-master.json";
        JSONArray WHTMasterArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(WHTMaster), "UTF-8"));

        String WHTMbl = "manifest/upload/WHT-mbl.json";
        JSONArray WHTMblArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(WHTMbl), "UTF-8"));

        String WHTHbl = "manifest/upload/WHT-hbl.json";
        JSONArray WHTHblArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(WHTHbl), "UTF-8"));

        // MRN 반복
        while (true) {

            if ("".equals(Util.null2str(contents)))
                break;
            WhtMf th = new WhtMf();
            th.setEntCode(Util.null2str(odParams.get("entCode")));
            th.setEntBusinessCode(Util.null2str(odParams.get("entBusinessCode")));
            th.setEntOfficeCode(Util.null2str(odParams.get("entOfficeCode")));
            th.setUserId(Util.null2str(odParams.get("userId")));
            th.setGmtTime(Util.null2str(odParams.get("gmtTime")));

            Class<?> cls = Class.forName(th.getClass().getName());

            int start = 0;
            for(Object WhtMasterObj : WHTMasterArray) {
                JSONObject jsonLineItem = (JSONObject) WhtMasterObj;
                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                int end = start + iByteLength;
                String func = (String) jsonLineItem.get("func");
                String text = Util.subString(contents, start, iByteLength).trim();

                if(!"".equals(Util.null2str(func))) {
                    Method m = cls.getDeclaredMethod(func, String.class);
                    m.invoke(th, text);
                }
                start = end;
            }

            // Master String 잘라내기
            contents = Util.subString(contents, start, 0);
            // Master 데이터 이후 MBL 데이터 로드
            int mawbCount = Integer.parseInt(th.getMawbNo());
            log.info("mawbCount ::: {}", mawbCount);
            if (!"".equals(contents) && mawbCount > 0) {
                List<WhtMf> thList = new ArrayList<WhtMf>();

                for (int i = 0; i < mawbCount; i++) {
                	WhtMf mblTh = new WhtMf();

                    start = 0;
                    for (Object WHTMblObj : WHTMblArray) {

                        JSONObject jsonLineItem = (JSONObject) WHTMblObj;
                        int iByteLength = Util.null2int(jsonLineItem.get("size"));
                        int end = start + iByteLength;
                        String func = (String) jsonLineItem.get("func");

                        String text = Util.subString(contents, start, iByteLength).trim();

                        if (!"".equals(Util.null2str(func))) {
                            Method m = cls.getDeclaredMethod(func, String.class);
                            m.invoke(mblTh, text);
                        }
                        start = end;
                    }
                    contents = Util.subString(contents, start, 0);
                    log.info("mblth:::: {}",mblTh.toString());
                    int hawbCount = Integer.parseInt(mblTh.getHawbNo());
                    log.info("hawbCount ::: {}", hawbCount);
                    if (!"".equals(contents) && hawbCount > 0) {
                        for (int j = 0; j < hawbCount; j++) {
                        	WhtMf hblTh = new WhtMf();
                            // 공통
                            hblTh.setEntCode(th.getEntCode());
                            hblTh.setEntBusinessCode(th.getEntBusinessCode());
                            hblTh.setEntOfficeCode(th.getEntOfficeCode());
                            hblTh.setDateArrive(th.getDateArrive());
                            hblTh.setFlightNo(th.getFlightNo());
                            hblTh.setBound("AIR");
                            hblTh.setUserId(th.getUserId());
                            hblTh.setGmtTime(th.getGmtTime());
                            hblTh.setMawb(mblTh.getMawb());
                            hblTh.setFwdCode(mblTh.getFwdCode());
                            hblTh.setFwdName(mblTh.getFwdName());
                            hblTh.setUnloadSpot(mblTh.getUnloadSpot());

                            start = 0;
                            for (Object WHTHblObj : WHTHblArray) {

                                JSONObject jsonLineItem = (JSONObject) WHTHblObj;
                                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                                int end = start + iByteLength;
                                String func = (String) jsonLineItem.get("func");

                                String text = Util.subString(contents, start, iByteLength).trim();

                                if (!"".equals(Util.null2str(func))) {
                                    Method m = cls.getDeclaredMethod(func, String.class);
                                    m.invoke(hblTh, text);
                                }
                                start = end;
                            }
                            if ("".equals(hblTh.getHsn()) || hblTh.getHsn() == null) {
                                hblTh.setCargoReferenceNo(th.getMrn() + Util.lpad(mblTh.getMsn(), 4, '0'));
                            } else {
                                hblTh.setCargoReferenceNo(th.getMrn() + Util.lpad(mblTh.getMsn(), 4, '0')
                                        + Util.lpad(hblTh.getHsn(), 4, '0'));
                            }
                            thList.add(hblTh);
                            contents = Util.subString(contents, start, 0);
                        }
                    }
                    th.setHList(thList);
                }
            } else {
                result.setResult(FileUploadResponse.RESULT_FAIL);
                result.setResultText("Data Make Fail!");
            }

            List<WhtMf> list = th.getHList();
            log.info("HblList:::::: {}",list.toString());
            for (int j =0; j <list.size(); j++) {
                String cargoReferenceNo = list.get(j).getCargoReferenceNo();
                
                int cnt = bmsInMapper.checkCargoReferenceNoFromInAndInPlan(cargoReferenceNo);
                if(cnt > 0) {
                	log.info("["+ j +"]중복된 화물관리번호 skip CARGO_REFERENCE_NO: ", cargoReferenceNo);
                	continue;
                }
                
                InPlan inPlan = new InPlan();
                inPlan.setEntBusinessCode((String) odParams.get("entBusinessCode"));
                inPlan.setBlNo(list.get(j).getHawb());
                inPlan.setOdJobNo(list.get(j).getHawb());
                inPlan.setMblNo(list.get(j).getMawb());
                inPlan.setCargoReferenceNo(list.get(j).getCargoReferenceNo());
                inPlan.setVesselFlight(Util.null2str(list.get(j).getFlightNo()));
                String pkgType ="";
                String flightNo = Util.null2str(list.get(j).getFlightNo());
                if (flightNo.length() >= 3 && Character.isDigit(flightNo.charAt(2))) {
                    pkgType = "CT";
                } else {
                    pkgType = "GT";
                }
                inPlan.setPkgType(Util.null2str(pkgType));
                inPlan.setArDate(Util.null2str(convertDateFormat(list.get(j).getDateArrive())));
                inPlan.setExCt(Util.null2int(Integer.parseInt(list.get(j).getPiecesNo())));
                inPlan.setExWt(Util.null2dbl(Double.parseDouble(list.get(j).getGrossWeight())));
                inPlan.setCommodityItemLoc(Util.null2str(list.get(j).getGoodsDescript()));
                inPlan.setConsigneeName(Util.null2str(list.get(j).getConsigneeName()));
                inPlan.setConsigneeAddress1(Util.null2str(list.get(j).getConsigneeAddr()));
                inPlan.setConsigneeBusinessId(Util.null2str(list.get(j).getConsigneeCode()));
                inPlan.setCdType("N");
                inPlan.setBigSizeYn("N");
                inPlan.setRegisterUserId(Util.null2str(list.get(j).getUserId()));
                inPlan.setUpdateUserId(Util.null2str(list.get(j).getUserId()));
                
                inPlan.setShipperName(Util.null2str(list.get(j).getShipperName()));
                inPlan.setShipperAddress1(Util.null2str(list.get(j).getShipperAddr()));
                inPlan.setShipperTel(Util.null2str(list.get(j).getShipperTelFax()));
                inPlan.setNotifyCode(Util.null2str(list.get(j).getNotifyCode()));
                inPlan.setNotifyName(Util.null2str(list.get(j).getNotifyName()));
                inPlan.setNotifyAddress1(Util.null2str(list.get(j).getNotifyAddr()));
                inPlan.setNotifyTel(Util.null2str(list.get(j).getNotifyTelFax()));
                inPlan.setLastLocation(Util.null2str(list.get(j).getWarehouseCode()));
                try {
                    bmsInMapper.insertInPlan(inPlan);
                    insertCnt++;
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        }
        log.info("데이터 입력건수: {}", insertCnt);
        String resultText = Integer.toString(insertCnt);
        result.setResultText(resultText);
    }

    private void insertXxxManifestList(FileUploadResponse result, String contents, Map<String, Object> odParams)
            throws Exception {
        JSONParser jsonParser = new JSONParser();
        int insertCnt = 0;

        //XXX는 master - mbl(반복) - cntr(반복) 구조
        String XXXMaster = "manifest/upload/XXX-master.json";
        JSONArray XXXMasterArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(XXXMaster), "UTF-8"));

        String XXXMbl = "manifest/upload/XXX-mbl.json";
        JSONArray XXXMblArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(XXXMbl), "UTF-8"));

        String XXXCntr = "manifest/upload/XXX-cntr.json";
        JSONArray XXXCntrArray = (JSONArray) jsonParser
                .parse(new InputStreamReader(getStreamFromResource(XXXCntr), "UTF-8"));

        // MRN 반복
        while (true) {

            if ("".equals(Util.null2str(contents)))
                break;
            XxxMf th = new XxxMf();
            th.setEntCode(Util.null2str(odParams.get("entCode")));
            th.setEntBusinessCode(Util.null2str(odParams.get("entBusinessCode")));
            th.setEntOfficeCode(Util.null2str(odParams.get("entOfficeCode")));
            th.setUserId(Util.null2str(odParams.get("userId")));
            th.setGmtTime(Util.null2str(odParams.get("gmtTime")));

            Class<?> cls = Class.forName(th.getClass().getName());

            int start = 0;
            for(Object XXXMasterObj : XXXMasterArray) {
                JSONObject jsonLineItem = (JSONObject) XXXMasterObj;
                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                int end = start + iByteLength;
                String func = (String) jsonLineItem.get("func");
                String text = Util.subString(contents, start, iByteLength).trim();

                if(!"".equals(Util.null2str(func))) {
                    Method m = cls.getDeclaredMethod(func, String.class);
                    m.invoke(th, text);
                }
                start = end;
            }

            // Master String 잘라내기
            contents = Util.subString(contents, start, 0);
            // Master 데이터 이후 MBL 데이터 로드
            int mawbCount = Integer.parseInt(th.getBlCnt());
            log.info("BL Count ::: {}", mawbCount);
            if (!"".equals(contents) && mawbCount > 0) {
                List<XxxMf> thList = new ArrayList<XxxMf>();

                for (int i = 0; i < mawbCount; i++) {
                	XxxMf mblTh = new XxxMf();

                	 // 공통
                	mblTh.setEntCode(th.getEntCode());
                	mblTh.setEntBusinessCode(th.getEntBusinessCode());
                	mblTh.setEntOfficeCode(th.getEntOfficeCode());
                	mblTh.setArDate(th.getArDate());
                	mblTh.setVoyage(th.getVoyage());
                	mblTh.setCustomsCode(th.getCustomsCode());
                	mblTh.setMrn(th.getMrn());
                	mblTh.setCustomsDptCode(th.getCustomsDptCode());
                	mblTh.setCallsign(th.getCallsign());
                	mblTh.setVesselCode(th.getVesselCode());
                	mblTh.setVesselNation(th.getVesselNation());
                	mblTh.setUnloadingPort(mblTh.getUnloadingPort());
                	mblTh.setBound("SEA");
                	mblTh.setUserId(th.getUserId());
                	mblTh.setGmtTime(th.getGmtTime());
                	
                    start = 0;
                    for (Object XXXMblObj : XXXMblArray) {

                        JSONObject jsonLineItem = (JSONObject) XXXMblObj;
                        int iByteLength = Util.null2int(jsonLineItem.get("size"));
                        int end = start + iByteLength;
                        String func = (String) jsonLineItem.get("func");

                        String text = Util.subString(contents, start, iByteLength).trim();

                        if (!"".equals(Util.null2str(func))) {
                            Method m = cls.getDeclaredMethod(func, String.class);
                            m.invoke(mblTh, text);
                        }
                        start = end;
                    }
                    contents = Util.subString(contents, start, 0);
                    log.info("mblth:::: {}",mblTh.toString());
                    int hawbCount = Integer.parseInt(mblTh.getCntrCnt());
                    log.info("CntrCount ::: {}", hawbCount);
                    if (!"".equals(contents) && hawbCount > 0) {
                    	
                    	List<XxxMf> cntrList = new ArrayList<XxxMf>();
                        for (int j = 0; j < hawbCount; j++) {
                        	XxxMf hblTh = new XxxMf();

                            start = 0;
                            for (Object XXXCntrObj : XXXCntrArray) {

                                JSONObject jsonLineItem = (JSONObject) XXXCntrObj;
                                int iByteLength = Util.null2int(jsonLineItem.get("size"));
                                int end = start + iByteLength;
                                String func = (String) jsonLineItem.get("func");

                                String text = Util.subString(contents, start, iByteLength).trim();

                                if (!"".equals(Util.null2str(func))) {
                                    Method m = cls.getDeclaredMethod(func, String.class);
                                    m.invoke(hblTh, text);
                                }
                                start = end;
                            }
                           
                            cntrList.add(hblTh);
                            contents = Util.subString(contents, start, 0);
                        }
                        mblTh.setCntrList(cntrList);
                    }
                    if ("".equals(mblTh.getHsn()) || mblTh.getHsn() == null) {
                    	mblTh.setCargoReferenceNo(th.getMrn() + Util.lpad(mblTh.getMsn(), 4, '0'));
                    } else {
                    	mblTh.setCargoReferenceNo(th.getMrn() + Util.lpad(mblTh.getMsn(), 4, '0')
                                + Util.lpad(mblTh.getHsn(), 4, '0'));
                    }
                    thList.add(mblTh);
                    
                }
                th.setMList(thList);
            } else {
                result.setResult(FileUploadResponse.RESULT_FAIL);
                result.setResultText("Data Make Fail!");
            }

            List<XxxMf> list = th.getMList();
            log.info("HblList:::::: {}",list.toString());
            for (int j =0; j <list.size(); j++) {
                String cargoReferenceNo = list.get(j).getCargoReferenceNo();
                
                int cnt = bmsInMapper.checkCargoReferenceNoFromInAndInPlan(cargoReferenceNo);
                if(cnt > 0) {
                	log.info("["+ j +"]중복된 화물관리번호 skip CARGO_REFERENCE_NO: ", cargoReferenceNo);
                	continue;
                }
                
                InPlan inPlan = InPlan.initializeInPlan();
                inPlan.setEntBusinessCode((String) odParams.get("entBusinessCode"));
                inPlan.setBlNo(list.get(j).getHawb());
                inPlan.setOdJobNo(list.get(j).getHawb());
                inPlan.setMblNo(list.get(j).getMawb());
                inPlan.setCargoReferenceNo(list.get(j).getCargoReferenceNo());
                inPlan.setArDate(Util.null2str(convertDateFormat(list.get(j).getArDate())));
                inPlan.setExCt(Util.null2int(Integer.parseInt(list.get(j).getCt())));
                inPlan.setExWt(Util.null2dbl(Double.parseDouble(list.get(j).getWt())));
                inPlan.setExCbm(Util.null2dbl(Double.parseDouble(list.get(j).getCbm())));
                inPlan.setCommodityItemLoc(Util.null2str(list.get(j).getGoods()));
				/* inPlan.setConsigneeCode(Util.null2str(list.get(j).getCneRegno())); */
                inPlan.setConsigneeName(Util.null2str(list.get(j).getCneName()));
                inPlan.setConsigneeAddress1(Util.null2str(list.get(j).getCneAddr()));
                inPlan.setConsigneeBusinessId(Util.null2str(list.get(j).getCneRegno()));
                inPlan.setCdType("N");
                inPlan.setBigSizeYn("N");
                inPlan.setRegisterUserId(Util.null2str(list.get(j).getUserId()));
                inPlan.setUpdateUserId(Util.null2str(list.get(j).getUserId()));
                
                inPlan.setShipperName(Util.null2str(list.get(j).getShpName()));
                inPlan.setShipperAddress1(Util.null2str(list.get(j).getShpAddr()));
                inPlan.setShipperTel(Util.null2str(list.get(j).getShpTel()));
                inPlan.setNotifyCode(Util.null2str(list.get(j).getNfyRegno()));
                inPlan.setNotifyName(Util.null2str(list.get(j).getNfyName()));
                inPlan.setNotifyAddress1(Util.null2str(list.get(j).getNfyAddr()));
                inPlan.setNotifyTel(Util.null2str(list.get(j).getNfyTel()));
                
                inPlan.setCarrierCode(Util.null2str(list.get(j).getCarrierCode()));
                inPlan.setCustomsCode(Util.null2str(list.get(j).getCustomsCode()));
                inPlan.setCustomsDptCode(Util.null2str(list.get(j).getCustomsDptCode()));
                inPlan.setCustomsCode(Util.null2str(list.get(j).getCustomsCode()));
                inPlan.setVesselFlight(Util.null2str(list.get(j).getVesselCode()));;
                inPlan.setVoyage(Util.null2str(list.get(j).getVoyage()));
                inPlan.setSpecialCode1(Util.null2str(list.get(j).getSpecialCode1()));
                inPlan.setSpecialCode2(Util.null2str(list.get(j).getSpecialCode2()));
                inPlan.setSpecialCode3(Util.null2str(list.get(j).getSpecialCode3()));
                
                inPlan.setDeliveryCode(Util.null2str(list.get(j).getTrCode()));
                try {
                    bmsInMapper.insertInPlanForXxxUpload(inPlan);
                    inPlan.getInPlanNo();
                    if(inPlan.getInPlanNo() != null) {
                    	List<XxxMf> cntrList = list.get(j).getCntrList();
                    	for (XxxMf cntr : cntrList) {
                    		BmsInPlanContainer bmsInPlanContainer = new BmsInPlanContainer();
                    		
                    		bmsInPlanContainer.setInPlanNo(inPlan.getInPlanNo());
                    		bmsInPlanContainer.setCntrNo(cntr.getCntrNo());
                    		bmsInPlanContainer.setCntrEdiCode(cntr.getCntrType());
                    		bmsInPlanContainer.setSealNo1(cntr.getSealNo1());
                    		bmsInPlanContainer.setSealNo2(cntr.getSealNo2());
                    		bmsInPlanContainer.setSealNo3(cntr.getSealNo3());
                        	bmsInMapper.insertBmsInPlanContainer(bmsInPlanContainer);
                    	}
                    }
                    insertCnt++;
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
                
            }
        }
        log.info("데이터 입력건수: {}", insertCnt);
        String resultText = Integer.toString(insertCnt);
        result.setResultText(resultText);
    }

    /**
     * 파일 읽어서 stream 반환
     * @param file
     * @return
     */
    public static InputStream getStreamFromResource(String file) {
        ClassLoader classLoader = Util.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(file);

        if (inputStream == null) {
            throw new IllegalArgumentException("file \"" + file + "\" not found.");
        } else {
            return inputStream;
        }
    }

    public static String convertDateFormat(String date) {
        if (date == null || date.length() != 8) {
            throw new IllegalArgumentException("Input date must be in the format YYYYMMDD and 8 characters long.");
        }

        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6, 8);

        return year + "-" + month + "-" + day;
    }

}
