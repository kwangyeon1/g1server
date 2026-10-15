package org.yht.exps.express.controller.bms;

import java.io.StringReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo;
import org.yht.exps.express.model.bms.unipass.CntrProgressInfo;
import org.yht.exps.express.model.bms.unipass.SearchCargoProgressInfo;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/***
 * "245XZ75126I	0007	0004"	https://unipass.customs.go.kr:38010/ext/rest/cargCsclPrgsInfoQry/retrieveCargCsclPrgsInfo?crkyCn=j230k135f190w257u000a090w1&cargMtNo=245XZ75126I00070004
 * 019245GNHM9	                    https://unipass.customs.go.kr:38010/ext/rest/cargCsclPrgsInfoQry/retrieveCargCsclPrgsInfo?crkyCn=j230k135f190w257u000a090w1&hblNo=019245GNHM9&blYy=2024
 */
@Slf4j
@RestController
@Api(tags = "BMS")
@RequestMapping("/{entCode}/bms/unipass")
public class BmsUniPassController {


	@Value("${custom.unipass-url:unipass.customs.go.kr}")
	private String unipassBaseUrl_;

	@Value("${custom.unipass-port:38010}")
	int unipassPort_;

	@Value("${custom.unipass-path:/ext/rest/cargCsclPrgsInfoQry/retrieveCargCsclPrgsInfo}")
	private String unipassCargoProgressPath_;

	@Value("${custom.unipass-path:/ext/rest/cntrQryBrkdQry/retrieveCntrQryBrkd}")
	private String unipassCntrProgressPath_;

	@Value("${custom.unipass-key:j230k135f190w257u000a090w1}")
	private String unipassCargoProgressKey_;

	@Value("${custom.unipass-key:b260z146x084s267p030m040w0}")
	private String unipassCntrProgressKey_;
	@ApiOperation(value = "UniPass 화물진행정보 가져오기", notes="UniPass 화물진행정보 가져오기 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/cargoinfo", method = RequestMethod.GET)
	public ResponseEntity<CargoProgressInfo> selectBmsAirDOList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCargoProgressInfo search) throws Exception {
		//search.setSrchEntCode(entCode);
		CargoProgressInfo cpInfo = getCargoProgressInfo(search);
		return new ResponseEntity<CargoProgressInfo>(cpInfo, HttpStatus.OK);
	}
	public CargoProgressInfo getCargoProgressInfo(SearchCargoProgressInfo searchCargoProgressInfo) throws Exception {
		CargoProgressInfo cargoProgressInfo = null;


		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(unipassBaseUrl_)
				.port(unipassPort_)
				.path(unipassCargoProgressPath_)
				.queryParam("crkyCn", URLDecoder.decode(unipassCargoProgressKey_, "UTF-8"));

		String cargMtNo = Util.null2str(searchCargoProgressInfo.getSrchCargoReferenceNo());	// 화물관리번호
		String mblNo = Util.null2str(searchCargoProgressInfo.getSrchMblNo());				// MBL No
		String hblNo = Util.null2str(searchCargoProgressInfo.getSrchHblNo());				// HBL No
		String blYy = Util.null2str(searchCargoProgressInfo.getSrchBlYear());				// BL 연도

		if (!"".equals(cargMtNo))	uriComponentsBuilder.queryParam("cargMtNo", cargMtNo);
		if (!"".equals(mblNo))		uriComponentsBuilder.queryParam("mblNo", mblNo);
		if (!"".equals(hblNo))		uriComponentsBuilder.queryParam("hblNo", hblNo);
		if (!"".equals(blYy))		uriComponentsBuilder.queryParam("blYy", blYy);


		URI uri = uriComponentsBuilder.build().encode().toUri();
		log.info("수입화물진행정보 API uri: {}", uri);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		ResponseEntity<String> response = restTemplate.getForEntity(uri,  String.class);

		String xml = response.getBody();

		JAXBContext jaxbContext = JAXBContext.newInstance(CargoProgressInfo.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		cargoProgressInfo = (CargoProgressInfo) jaxbUnmarshaller.unmarshal(new StringReader(xml));

	/*	JAXBContext jaxbContext = JAXBContext.newInstance(GOVCBR099.class); // JAXBContext 생성
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller(); // Unmarshaller 생성
		GOVCBR099 declaration = (GOVCBR099) jaxbUnmarshaller.unmarshal(path.toAbsolutePath().toFile()); // XML을 Java 객체로 변환
*/


		return cargoProgressInfo;
	}


	@ApiOperation(value = "UniPass 화물진행정보 가져오기", notes="UniPass 화물진행정보 가져오기 반환합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/search/cntrinfo", method = RequestMethod.GET)
	public ResponseEntity<CntrProgressInfo> selectBmsCntrInfo(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchCargoProgressInfo search) throws Exception {
		//search.setSrchEntCode(entCode);
		CntrProgressInfo cntrInfo = getCntrProgressInfo(search);
		log.info("콘테이너INFO:{}",cntrInfo);
		return new ResponseEntity<CntrProgressInfo>(cntrInfo, HttpStatus.OK);
	}

	public CntrProgressInfo getCntrProgressInfo(SearchCargoProgressInfo searchCargoProgressInfo) throws Exception {
		CntrProgressInfo cntrProgressInfo = null;


		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(unipassBaseUrl_)
				.port(unipassPort_)
				.path(unipassCntrProgressPath_)
				.queryParam("crkyCn", URLDecoder.decode(unipassCntrProgressKey_, "UTF-8"));

		String cargMtNo = Util.null2str(searchCargoProgressInfo.getSrchCargoReferenceNo());	// 화물관리번호
		String mblNo = Util.null2str(searchCargoProgressInfo.getSrchMblNo());				// MBL No
		String hblNo = Util.null2str(searchCargoProgressInfo.getSrchHblNo());				// HBL No
		String blYy = Util.null2str(searchCargoProgressInfo.getSrchBlYear());				// BL 연도

		if (!"".equals(cargMtNo))	uriComponentsBuilder.queryParam("cargMtNo", cargMtNo);
		if (!"".equals(mblNo))		uriComponentsBuilder.queryParam("mblNo", mblNo);
		if (!"".equals(hblNo))		uriComponentsBuilder.queryParam("hblNo", hblNo);
		if (!"".equals(blYy))		uriComponentsBuilder.queryParam("blYy", blYy);


		URI uri = uriComponentsBuilder.build().encode().toUri();
		log.info("수입화물 컨테이너 진행정보 API uri: {}", uri);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		ResponseEntity<String> response = restTemplate.getForEntity(uri,  String.class);

		String xml = response.getBody();

		JAXBContext jaxbContext = JAXBContext.newInstance(CntrProgressInfo.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		cntrProgressInfo = (CntrProgressInfo) jaxbUnmarshaller.unmarshal(new StringReader(xml));

	/*	JAXBContext jaxbContext = JAXBContext.newInstance(GOVCBR099.class); // JAXBContext 생성
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller(); // Unmarshaller 생성
		GOVCBR099 declaration = (GOVCBR099) jaxbUnmarshaller.unmarshal(path.toAbsolutePath().toFile()); // XML을 Java 객체로 변환
*/
		return cntrProgressInfo;
	}
}
