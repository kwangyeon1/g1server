package org.yht.exps.express.controller.bms;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.bms.bmsPrint.BmsPrintList;
import org.yht.exps.express.model.bms.bmsPrint.search.SearchBmsPrintList;
import org.yht.exps.express.service.bms.BmsPrintService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "bms")
@RequestMapping("/{entCode}/bms/bmsPrint")
public class BmsPrintController {

	@Resource(name = "bmsPrintService")
	BmsPrintService bmsPrintService;

	/**
	 * 반입신고 출력용 추가 데이터 리스트
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "반입신고 출력용 추가 데이터 리스트", notes="반입신고 출력용 추가 데이터 리스트를 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/selectBmsInPrintList", method = RequestMethod.GET)
	public ResponseEntity<BmsPrintList> selectBmsInPrintList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPrintList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsPrintList>(bmsPrintService.selectBmsInPrintList(search), HttpStatus.OK);
	}

	/**
	 * 반출신고 출력용 추가 데이터 리스트
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "반출신고 출력용 추가 데이터 리스트", notes="반출신고 출력용 추가 데이터 리스트를 상세조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/selectBmsOutPrintList", method = RequestMethod.GET)
	public ResponseEntity<BmsPrintList> selectBmsOutPrintList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPrintList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsPrintList>(bmsPrintService.selectBmsOutPrintList(search), HttpStatus.OK);
	}

	/**
	 * 견품반출 출력용 추가 데이터 리스트
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "견품반출 출력용 추가 데이터 리스트", notes="견품반출 출력용 추가 데이터 리스트를 상세조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/selectBmsSamplePrintList", method = RequestMethod.GET)
	public ResponseEntity<?> selectBmsSamplePrintList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPrintList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity(bmsPrintService.selectBmsSamplePrintList(search), HttpStatus.OK);
	}
	
	/**
	 * 검사계획통보 출력용 추가 데이터 리스트
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "검사계획통보 출력용 추가 데이터 리스트", notes="검사계획통보 출력용 추가 데이터 리스트를 상세조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/selectBmsInspectPrintList", method = RequestMethod.GET)
	public ResponseEntity<BmsPrintList> selectBmsInspectPrintList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPrintList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsPrintList>(bmsPrintService.selectBmsInspectPrintList(search), HttpStatus.OK);
	}

	/**
	 * 반출통고신고 출력용 추가 데이터 리스트
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "반출통고신고 출력용 추가 데이터 리스트", notes="반출통고신고 출력용 추가 데이터 리스트를 상세조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/selectBmsOutNoticePrintList", method = RequestMethod.GET)
	public ResponseEntity<BmsPrintList> selectBmsOutNoticePrintList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPrintList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsPrintList>(bmsPrintService.selectBmsOutNoticePrintList(search), HttpStatus.OK);
	}

	/**
	 * 반입물품이상신고 출력용 추가 데이터 리스트
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "반입물품이상신고 출력용 추가 데이터 리스트", notes="반입물품이상신고 출력용 추가 데이터 리스트를 상세조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/selectBmsAbnormalPrintList", method = RequestMethod.GET)
	public ResponseEntity<BmsPrintList> selectBmsAbnormalPrintList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchBmsPrintList search) throws Exception {
		search.setSrchEntCode(entCode);
		return new ResponseEntity<BmsPrintList>(bmsPrintService.selectBmsAbnormalPrintList(search), HttpStatus.OK);
	}

	/**
	 * 수입검사이고반출입[별지7호] 연락처 조회
	 * @param entCode
	 * @param search
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "수입검사이고반출입 - 별지7호 연락처 조회", notes="수입검사이고반출입 - 별지7호 연락처 조회합니다.")
	@ApiImplicitParams({@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")})
	@RequestMapping(value="/search/selectBmsPrintList", method = RequestMethod.GET)
	public ResponseEntity<String> selectBmsAbnormalPrintcontactNo(@PathVariable("entCode") String entCode, @QueryStringArgResolver Map<String, Object> search) throws Exception {
		return new ResponseEntity<String>(bmsPrintService.selectBmsAbnormalPrintcontactNo(search), HttpStatus.OK);
	}

}
