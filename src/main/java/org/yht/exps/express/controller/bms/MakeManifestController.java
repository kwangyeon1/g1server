package org.yht.exps.express.controller.bms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.service.bms.MakeManifestService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 파일명 : MakeManifestController
 * 작성일자 : 2024.03.20
 * 작성자 : 박영호
 * 설명 : 적하목록 파일 업로드
 */

@RestController
@Api(tags="MakeManifest")
@RequestMapping("/{entCode}/bms/manifest")
public class MakeManifestController {

    @Resource(name = "makeManifestService")
    MakeManifestService makeManifestService;

    /**
     * File Upload
     * @param
     * @param file
     * @return
     */
    @ApiOperation(value = "적하목록 File Upload", notes="적하목록 File을 업로드 합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
            @ApiImplicitParam(name = "entBusinessCode", value = "사업장코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX-KR"),
            @ApiImplicitParam(name = "solution", value = "업무", required = true, dataType = "string", paramType = "path", defaultValue = "BMS"),
            @ApiImplicitParam(name = "userId", value = "등록자ID", required = true, dataType = "string", paramType = "path", defaultValue = "201808-001"),
            @ApiImplicitParam(name = "dateFormat", value = "DB 날짜형식", required = true, dataType = "string", paramType = "path", defaultValue = "23"),
            @ApiImplicitParam(name = "gmtTime", value = "GMT Time", required = true, dataType = "string", paramType = "path", defaultValue = "0"),
            @ApiImplicitParam(name = "menuId", value = "해당 업로드 Format이 속한 MENU ID 값", required = true, dataType = "string", paramType = "path", defaultValue = "BMS-U0010"),
    })
    @RequestMapping(value = "/upload/{entBusinessCode}/{solution}/{userId}/{dateFormat}/{gmtTime}/{menuId}", method = RequestMethod.POST)
    public FileUploadResponse uploadManifestFile(
            @PathVariable("entCode") String entCode,
            @PathVariable("entBusinessCode") String entBusinessCode,
            @PathVariable("solution") String solution,
            @PathVariable("userId") String userId,
            @PathVariable("dateFormat") String dateFormat,
            @PathVariable("gmtTime") String gmtTime,
            @PathVariable("menuId") String menuId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws Exception {

        return makeManifestService.putManifest(file, entCode, entBusinessCode, solution, userId, dateFormat, gmtTime, menuId);
    }

}
