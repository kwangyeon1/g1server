package org.yht.exps.express.controller.systemMng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yht.config.QueryStringArgResolver;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.backup.OdBackup;
import org.yht.exps.express.model.systemMng.AddressZipCodeList;
import org.yht.exps.express.model.systemMng.AlarmGroupList;
import org.yht.exps.express.model.systemMng.DeliveryInterface;
import org.yht.exps.express.model.systemMng.DeliveryInterfaceList;
import org.yht.exps.express.model.systemMng.InterfaceKey;
import org.yht.exps.express.model.systemMng.InterfaceKeyCountList;
import org.yht.exps.express.model.systemMng.InterfaceKeyList;
import org.yht.exps.express.model.systemMng.MonthlyCountList;
import org.yht.exps.express.model.systemMng.ZipCode;
import org.yht.exps.express.model.systemMng.ZipCodeList;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntDelete;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntInsert;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntList;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplate;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplateDelete;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplateInsert;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkEntTemplateList;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkSaveResult;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkSend;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplate;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplateDelete;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplateInsert;
import org.yht.exps.express.model.systemMng.alimTalk.AlimTalkTemplateList;
import org.yht.exps.express.model.systemMng.alimTalk.search.SearchAlimTalkEnt;
import org.yht.exps.express.model.systemMng.alimTalk.search.SearchAlimTalkTemplate;
import org.yht.exps.express.model.systemMng.search.SearchAddressZipCodeList;
import org.yht.exps.express.model.systemMng.search.SearchAlarmGroup;
import org.yht.exps.express.model.systemMng.search.SearchDeliveryInterface;
import org.yht.exps.express.model.systemMng.search.SearchDeliveryInterfaceList;
import org.yht.exps.express.model.systemMng.search.SearchInterfaceKey;
import org.yht.exps.express.model.systemMng.search.SearchInterfaceKeyCountList;
import org.yht.exps.express.model.systemMng.search.SearchInterfaceKeyList;
import org.yht.exps.express.model.systemMng.search.SearchZipCode;
import org.yht.exps.express.model.systemMng.search.SearchZipCodeList;
import org.yht.exps.express.service.systemMng.SystemMngService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "SystemMng")
@RequestMapping("/{entCode}/ecm/systemMng")
public class SystemMngController {
	
	@Resource(name = "systemMngService")
	SystemMngService systemMngService;
	
	@Autowired
	private CustomProperties customProperties;
	@Autowired
	JwtTokenProvider jwtTokenProvider;


//	@ApiOperation(value = "전자상거래 배송사 Interface 조회 - API", notes="배송사 Interface API를 조회합니다.")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
//			@ApiImplicitParam(name = "customerCode", value = "관리번호", required = true, dataType = "string", paramType = "path")
//	})
//	@RequestMapping(value="/deliveryInterface/api/get/{entCode}", method = RequestMethod.GET)
//	public ResponseEntity<DeliveryInterface> selectDeliveryInterfaceApi(@PathVariable("entCode") String entCode, SearchDeliveryInterface search, HttpServletRequest request) throws Exception {
//		Util.setDB(request, search.getSrchEntCode());
//		return new ResponseEntity<DeliveryInterface>(systemMngService.selectDeliveryInterfaceApi(search), HttpStatus.OK);
//	}

	@ApiOperation(value = "전자상거래 InterfaceKey 리스트 조회", notes="InterfaceKey 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/interfaceKey/search/list", method = RequestMethod.GET)
	public ResponseEntity<InterfaceKeyList> selectInterfaceKeyList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInterfaceKeyList search, HttpServletRequest request) throws Exception {
		Util.setDB(request, search.getSrchEntCode());		
		return new ResponseEntity<InterfaceKeyList>(systemMngService.selectInterfaceKeyList(search), HttpStatus.OK);
	}
	@ApiOperation(value = "전자상거래 InterfaceKey건수 리스트 조회", notes="InterfaceKey건수 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/interfaceKeyCount/search/list", method = RequestMethod.GET)
	public ResponseEntity<InterfaceKeyCountList> selectInterfaceKeyCountList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInterfaceKeyCountList search, HttpServletRequest request) throws Exception {
		Util.setDB(request, search.getSrchEntCode());		
		return new ResponseEntity<InterfaceKeyCountList>(systemMngService.selectInterfaceKeyCountList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 InterfaceKey 조회", notes="InterfaceKey를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "customerCode", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/interfaceKey/get/{customerCode}", method = RequestMethod.GET)
	public ResponseEntity<InterfaceKey> selectInterfaceKey(@PathVariable("entCode") String entCode, @PathVariable("customerCode") String customerCode, SearchInterfaceKey search, HttpServletRequest request) throws Exception {
		Util.setDB(request, search.getSrchEntCode());
		search.setSrchCustomerCode(customerCode);
		return new ResponseEntity<InterfaceKey>(systemMngService.selectInterfaceKey(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 InterfaceKey insert", notes="InterfaceKey를 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/interfaceKey/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertInterfaceKey(@PathVariable("entCode") String entCode, @RequestBody InterfaceKey interfaceKey, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, interfaceKey, "updateUserId", null, null);
		interfaceKey.setRegisterUserId(interfaceKey.getUpdateUserId());
		Util.setDB(request, interfaceKey.getEntCode());
		return new ResponseEntity<OrderSaveResult>(systemMngService.saveInterfaceKey(interfaceKey), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 InterfaceKey update", notes="InterfaceKey를 update합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "customerCode", value = "거래처코드", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/interfaceKey/update/{customerCode}", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateInterfaceKey(@PathVariable("entCode") String entCode, @PathVariable("customerCode") String customerCode, @RequestBody InterfaceKey interfaceKey, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, interfaceKey, "updateUserId", null, null);
		interfaceKey.setRegisterUserId(interfaceKey.getUpdateUserId());
		Util.setDB(request, interfaceKey.getEntCode());
		interfaceKey.setCustomerCode(customerCode);
		return new ResponseEntity<OrderSaveResult>(systemMngService.saveInterfaceKey(interfaceKey), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 InterfaceKey delete", notes="InterfaceKey를 delete합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "customerCode", value = "거래처코드", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/interfaceKey/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteInterfaceKey(@PathVariable("entCode") String entCode, @RequestBody List<SearchInterfaceKey> search, HttpServletRequest request) throws Exception {
		String srchEntCode = search.get(0).getSrchEntCode();
		Util.setDB(request, srchEntCode);
		return new ResponseEntity<OrderSaveResult>(systemMngService.deleteInterfaceKey(search, srchEntCode), HttpStatus.OK);
	}
	
	@ApiOperation(value = "시스템관리 배송사 Interface 관리 조회", notes="배송사 Interface 관리 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/deliveryInterface/search/list", method = RequestMethod.GET)
	public ResponseEntity<DeliveryInterfaceList> selectDeliveryInterfaceList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchDeliveryInterfaceList search, HttpServletRequest request) throws Exception {
		Util.setDB(request, search.getSrchEntCode());
		return new ResponseEntity<DeliveryInterfaceList>(systemMngService.selectDeliveryInterfaceList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 배송사 Interface 조회", notes="배송사 Interface를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "customerCode", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/deliveryInterface/get/{entCode}", method = RequestMethod.GET)
	public ResponseEntity<DeliveryInterface> selectDeliveryInterface(@PathVariable("entCode") String entCode, SearchDeliveryInterface search, HttpServletRequest request) throws Exception {
		Util.setDB(request, search.getSrchEntCode());
		return new ResponseEntity<DeliveryInterface>(systemMngService.selectDeliveryInterface(search), HttpStatus.OK);
	}	
	
	@ApiOperation(value = "전자상거래 배송사 Interface 조회 - API", notes="배송사 Interface API를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "customerCode", value = "관리번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/deliveryInterface/api/get/{entCode}", method = RequestMethod.GET)
	public ResponseEntity<DeliveryInterface> selectDeliveryInterfaceApi(@PathVariable("entCode") String entCode, SearchDeliveryInterface search, HttpServletRequest request) throws Exception {
		Util.setDB(request, search.getSrchEntCode());
		return new ResponseEntity<DeliveryInterface>(systemMngService.selectDeliveryInterfaceApi(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 배송사 Interface insert", notes="배송사 Interface를 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/deliveryInterface/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertDeliveryInterface(@PathVariable("entCode") String entCode, @RequestBody DeliveryInterface deliveryInterface, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, deliveryInterface, "updId", null, null);
		deliveryInterface.setRegId(deliveryInterface.getUpdId());
		Util.setDB(request, deliveryInterface.getEntCode());
		return new ResponseEntity<OrderSaveResult>(systemMngService.saveDeliveryInterface(deliveryInterface), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 배송사 Interface update", notes="배송사 Interface를 update합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/deliveryInterface/update/{entCode}", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateDeliveryInterface(@PathVariable("entCode") String entCode, @RequestBody DeliveryInterface deliveryInterface, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, deliveryInterface, "updId", null, null);
		deliveryInterface.setRegId(deliveryInterface.getUpdId());
		Util.setDB(request, deliveryInterface.getEntCode());
		return new ResponseEntity<OrderSaveResult>(systemMngService.saveDeliveryInterface(deliveryInterface), HttpStatus.OK);
	}	
	
	@ApiOperation(value = "전자상거래 배송사 Interface delete", notes="배송사 Interface를 delete합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "customerCode", value = "거래처코드", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/deliveryInterface/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteDeliveryInterface(@PathVariable("entCode") String entCode, @RequestBody SearchDeliveryInterface delete, HttpServletRequest request) throws Exception {
		Util.setDB(request, delete.getSrchEntCode());
		return new ResponseEntity<OrderSaveResult>(systemMngService.deleteDeliveryInterface(delete), HttpStatus.OK);
	}

	@ApiOperation(value = "전자상거래 월별건수 리스트 조회", notes="월별건수 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/monthlyCount/search/list", method = RequestMethod.GET)
	public ResponseEntity<MonthlyCountList> selectMonthlyCountList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInterfaceKeyList search, HttpServletRequest request) throws Exception {
		Util.setDB(request, "YHTXX");	/** 월별건수 mastser아니고 expsDB에 저장 */		
		return new ResponseEntity<MonthlyCountList>(systemMngService.selectMonthlyCountList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 월별 사용자및업체 건수 리스트 조회", notes="월별건수 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/monthlyUserCount/search/list", method = RequestMethod.GET)
	public ResponseEntity<MonthlyCountList> selectMonthlyUserCountList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchInterfaceKeyList search, HttpServletRequest request) throws Exception {
		Util.setDB(request, "YHTXX");	/** 월별건수 mastser아니고 expsDB에 저장 */		
		return new ResponseEntity<MonthlyCountList>(systemMngService.selectMonthlyUserCountList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "알림 서비스 insert", notes="알림 서비스를 insert합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/alarmGroup/insert", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> insertAlarmGroup(@PathVariable("entCode") String entCode, @RequestBody SearchAlarmGroup search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", null, null, true, false, false);
		return new ResponseEntity<OrderSaveResult>(systemMngService.saveAlarmGroup(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "알림 서비스 update", notes="알림 서비스를 update합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "groupId", value = "그룹아이디", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/alarmGroup/update/{groupId}", method = RequestMethod.PUT)
	public ResponseEntity<OrderSaveResult> updateAlarmGroup(@PathVariable("entCode") String entCode, @PathVariable("groupId") String groupId, @RequestBody SearchAlarmGroup search, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, search, "userId", null, null, true, false, false);
		search.setSrchGroupId(groupId);
		return new ResponseEntity<OrderSaveResult>(systemMngService.saveAlarmGroup(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "알림 서비스 delete", notes="알림 서비스를 delete합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/alarmGroup/delete", method = RequestMethod.DELETE)
	public ResponseEntity<OrderSaveResult> deleteAlarmGroup(@PathVariable("entCode") String entCode, @RequestBody SearchAlarmGroup search) throws Exception {
		return new ResponseEntity<OrderSaveResult>(systemMngService.deleteAlarmGroup(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "알림 서비스 리스트 조회", notes="알림 서비스 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/alarmGroup/search/list", method = RequestMethod.GET)
	public ResponseEntity<AlarmGroupList> selectAlarmGroupList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAlarmGroup search) throws Exception {
		return new ResponseEntity<AlarmGroupList>(systemMngService.selectAlarmGroupList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "알림 서비스 수신자 리스트 조회", notes="알림 서비스 수신자 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/alarmGroup/user/search/list", method = RequestMethod.GET)
	public ResponseEntity<AlarmGroupList> selectAlarmGroupUserList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAlarmGroup search) throws Exception {
		return new ResponseEntity<AlarmGroupList>(systemMngService.selectAlarmGroupUserList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "시스템관리 알림톡 업체등록정보 List(main) 조회", notes="알림톡 업체등록정보 리스트(main)를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/alimTalk/search/list", method = RequestMethod.GET)
	public ResponseEntity<AlimTalkEntList> selectAlimTalkEntList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAlimTalkEnt search) throws Exception {
		return new ResponseEntity<AlimTalkEntList>(systemMngService.selectAlimTalkEntList(search), HttpStatus.OK);
	}	
	
	@ApiOperation(value = "시스템관리 알림톡 업체에 등록된 템플릿 List(sub) 조회", notes=" 업체에 등록된 템플릿 리스트(sub)를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/alimTalk/item/search/list", method = RequestMethod.GET)
	public ResponseEntity<AlimTalkEntTemplateList> selectAlimTalkEntTemplateList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAlimTalkEnt search, HttpServletRequest req) throws Exception {
		Util.setDB(req, search.getSrchEntCode());	//조회할 도입사 DB로 연결
		return new ResponseEntity<AlimTalkEntTemplateList>(systemMngService.selectAlimTalkEntTemplateList(search), HttpStatus.OK);
	}	
	
	@ApiOperation(value = "시스템관리 알림톡 조회", notes=" 업체에 등록된 알림톡정보를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/alimTalk/get/{entCode}", method = RequestMethod.GET)
	public ResponseEntity<AlimTalkEntTemplate> selectAlimTalkData(@PathVariable("entCode") String entCode, SearchAlimTalkEnt search, HttpServletRequest req) throws Exception {
		Util.setDB(req, search.getSrchEntCode());
		return new ResponseEntity<AlimTalkEntTemplate>(systemMngService.selectAlimTalkData(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "시스템관리 알림톡 템플릿 조회", notes=" 알림톡 템플릿정보를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "templateType", value = "TEMPLATE_TYPE", required = true, dataType = "string", paramType = "path", defaultValue = "INV")
	}) 
	@RequestMapping(value="/alimTalkTemplate/get/{templateType}", method = RequestMethod.GET)
	public ResponseEntity<AlimTalkTemplate> selectAlimTalkTemplate(@PathVariable("entCode") String entCode, SearchAlimTalkTemplate search) throws Exception {
		return new ResponseEntity<AlimTalkTemplate>(systemMngService.selectAlimTalkTemplate(search), HttpStatus.OK);
	}

	@ApiOperation(value = "시스템관리 알림톡 템플릿 등록", notes="알림톡 템플릿 데이터 등록합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/alimTalk/item/insert", method = RequestMethod.POST)
	public ResponseEntity<AlimTalkSaveResult> insertTemplate(@PathVariable("entCode") String entCode, @RequestBody AlimTalkTemplateInsert insert, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, insert, "updateUserId", null, null);
		insert.setRegisterUserId(insert.getUpdateUserId());
		AlimTalkSaveResult result = null;
		for(String masterDb : customProperties.getSystemDbList()){
			if(masterDb.indexOf("MASTER") > -1){
				Util.setDB(request, masterDb); //마스터 db연결
				result = systemMngService.insertTemplate(insert,request);
			}
		}
		
		return new ResponseEntity<AlimTalkSaveResult>(result, HttpStatus.OK);
	}
	
	@ApiOperation(value = "시스템관리 알림톡 템플릿 수정", notes="알림톡 템플릿 데이터 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "templateType", value = "TEMPLATE_TYPE", required = true, dataType = "string", paramType = "path", defaultValue = "INV")
	})
	@RequestMapping(value="/alimTalk/item/update/{templateType}", method = RequestMethod.PUT)
	public ResponseEntity<AlimTalkSaveResult> updateTemplate(@PathVariable("entCode") String entCode, @RequestBody AlimTalkTemplateInsert insert, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, insert, "updateUserId", null, null);
		insert.setRegisterUserId(insert.getUpdateUserId());
		AlimTalkSaveResult result = null;
		for(String masterDb : customProperties.getSystemDbList()){
			if(masterDb.indexOf("MASTER") > -1){
				Util.setDB(request, masterDb); //마스터 db연결
				result = systemMngService.updateTemplate(insert,request);
			}
		}
		
		return new ResponseEntity<AlimTalkSaveResult>(result, HttpStatus.OK);
	}
	
	@ApiOperation(value = "시스템관리 알림톡 템플릿 List(sub2) 조회", notes=" 알림톡 템플릿 리스트(sub2)를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	}) 
	@RequestMapping(value="/alimTalk/item/search/list2", method = RequestMethod.GET)
	public ResponseEntity<AlimTalkTemplateList> selectAlimTalkTemplateList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAlimTalkTemplate search) throws Exception {
		return new ResponseEntity<AlimTalkTemplateList>(systemMngService.selectAlimTalkTemplateList(search), HttpStatus.OK);
	}	
	
	
	@ApiOperation(value = "시스템관리 알림톡 업체-템플릿 연결 등록", notes="알림톡 업체-템플릿 연결 데이터 등록합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/alimTalk/item/insert2", method = RequestMethod.POST)
	public ResponseEntity<AlimTalkSaveResult> insertEntTemplate(@PathVariable("entCode") String entCode, @RequestBody AlimTalkEntTemplateInsert insert, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, insert, "updateUserId", null, null);
		insert.setRegisterUserId(insert.getUpdateUserId());
		return new ResponseEntity<AlimTalkSaveResult>(systemMngService.insertEntTemplate(insert), HttpStatus.OK);
	}	
	
	@ApiOperation(value = "시스템관리 알림톡 송신정보 수정", notes="알림톡 송신정보 데이터를 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/alimTalk/update/{entCode}", method = RequestMethod.PUT)
	public ResponseEntity<AlimTalkSaveResult> updateEnt(@PathVariable("entCode") String entCode, @RequestBody AlimTalkEntInsert insert, HttpServletRequest request) throws Exception {
		AlimTalkSaveResult result = null;
		for(String masterDb : customProperties.getSystemDbList()){
			if(masterDb.indexOf("MASTER") > -1){
				Util.setDB(request, masterDb); //마스터 db연결
				result = systemMngService.updateEnt(insert);
			}
		}
		
		return new ResponseEntity<AlimTalkSaveResult>(result, HttpStatus.OK);
	}
	
	@ApiOperation(value = "시스템관리 알림톡 업체-템플릿 연결 수정", notes="알림톡 업체-템플릿 연결 데이터를 수정합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/alimTalk/item/update2/{entCode}", method = RequestMethod.PUT)
	public ResponseEntity<AlimTalkSaveResult> updateEntTemplate(@PathVariable("entCode") String entCode, @RequestBody AlimTalkEntTemplateInsert insert, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, insert, "updateUserId", null, null);
		insert.setRegisterUserId(insert.getUpdateUserId());
		return new ResponseEntity<AlimTalkSaveResult>(systemMngService.updateEntTemplate(insert), HttpStatus.OK);
	}
	
	@ApiOperation(value = "시스템관리 알림톡 업체 삭제", notes="알림톡 알림톡 업체 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/alimTalk/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<AlimTalkSaveResult> deleteEnt(@PathVariable("entCode") String entCode, @RequestBody AlimTalkEntDelete delete, @PathVariable("id") String id) throws Exception {
		return new ResponseEntity<AlimTalkSaveResult>(systemMngService.deleteEnt(delete), HttpStatus.OK);
	}	

	@ApiOperation(value = "시스템관리 알림톡 템플릿 삭제", notes="알림톡 알림톡 템플릿 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/alimTalk/item/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<AlimTalkSaveResult> deleteTemplate(@PathVariable("entCode") String entCode, @RequestBody AlimTalkTemplateDelete delete, @PathVariable("id") String id, HttpServletRequest request) throws Exception {
		AlimTalkSaveResult result = null;
		for(String masterDb : customProperties.getSystemDbList()){
			if(masterDb.indexOf("MASTER") > -1){
				Util.setDB(request, masterDb); //마스터 db연결
				result = systemMngService.deleteTemplate(delete,request);
			}
		}
		
		return new ResponseEntity<AlimTalkSaveResult>(result, HttpStatus.OK);
	}	
	
	
	@ApiOperation(value = "시스템관리 알림톡 업체 - 템플릿 삭제", notes="알림톡 알림톡 업체 - 템플릿 삭제 삭제합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/alimTalk/item/delete2/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<AlimTalkSaveResult> deleteEntTemplate(@PathVariable("entCode") String entCode, @RequestBody AlimTalkEntTemplateDelete delete, @PathVariable("id") String id) throws Exception {
		return new ResponseEntity<AlimTalkSaveResult>(systemMngService.deleteEntTemplate(delete), HttpStatus.OK);
	}	

	@ApiOperation(value = "시스템관리 알림톡 전송", notes="알림톡을 전송합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/alimTalk/send", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> sendAlimTalk(@PathVariable("entCode") String entCode, @RequestBody AlimTalkSend send, HttpServletRequest request) throws Exception {
		
		List<Map<String, Object>> sendList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		StringBuffer errorAHBLNos = new StringBuffer();	// 세종 인터페이스시 errMsgA에 해당되는 하우스번호 내역
		StringBuffer errorBHBLNos = new StringBuffer();	// 세종 인터페이스시 errMsgB에 해당되는 하우스번호 내역
		StringBuffer errorCHBLNos = new StringBuffer();	// 세종 인터페이스시 errMsgC에 해당되는 하우스번호 내역		
		
		
		sendList = systemMngService.sendAlimTalkEnt(send);
		
		
		if("F".equals(Util.null2str(sendList.get(0).get("result")))) {
			returnMap.put("result", "F");
		}else {
			Util.setDB(request, "SEJONG"); //SEJONG db연결
			resultList = systemMngService.sendAlimTalkSejong(sendList);
			
			if(resultList.size() > 0) {
				for(Map<String, Object> logMap : resultList) {
					if("Y".equals(Util.null2str(logMap.get("result")))) { // 발송완료 => INSERT 또는 UPDATE
						logMap.put("ENT_CODE", entCode);
						Util.setDB(request, entCode); //YHTXX db연결
						systemMngService.sendAlimTalkResult(logMap);
					}else if("N".equals(Util.null2str(logMap.get("result")))) { // 사유로 인해 발송실패 
						if(!"".equals(Util.null2str(logMap.get("errorMsgA")))) { // 세종텔레콤 DB insert 에러 HBL No
							
							if(!"".equals(Util.null2str(logMap.get("errorMsgA")))){
								errorAHBLNos.append(", ").append(Util.null2str(logMap.get("errorAHBLNo")));
							} else {
								errorAHBLNos.append(Util.null2str(logMap.get("errorAHBLNo")));
							}
						}else if(!"".equals(Util.null2str(logMap.get("errorMsgB")))) { // 송신자 번호가 없는 HBL No
							
							if(!"".equals(Util.null2str(logMap.get("errorMsgB")))){
								errorAHBLNos.append(", ").append(Util.null2str(logMap.get("errorBHBLNos")));
							} else {
								errorAHBLNos.append(Util.null2str(logMap.get("errorBHBLNos")));
							}
						}else if(!"".equals(Util.null2str(logMap.get("errorMsgC")))) { // 수신자 번호가 없는 HBL No
							
							if(!"".equals(Util.null2str(logMap.get("errorMsgC")))){
								errorAHBLNos.append(", ").append(Util.null2str(logMap.get("errorCHBLNos")));
							} else {
								errorAHBLNos.append(Util.null2str(logMap.get("errorCHBLNos")));
							}
						}
					}
				}
			}
			
			
		}
		
		return new ResponseEntity<Map<String, Object>>(returnMap, HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "Address ZipCode 리스트 조회", notes="Address ZipCode 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/addressZipCode/search/list", method = RequestMethod.GET)
	public ResponseEntity<AddressZipCodeList> selectAddressZipCodeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchAddressZipCodeList search, HttpServletRequest request) throws Exception {
		
		Util.setDB(request, "MASTER");
//		Util.setDB(request, entCode);
		
		return new ResponseEntity<AddressZipCodeList>(systemMngService.selectAddressZipCodeList(search), HttpStatus.OK);
	}	
	
	
	@ApiOperation(value = "Address ZipCode 업데이트 일자 조회", notes="Address ZipCode 업데이트 일자를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/addressZipCode/search/list2", method = RequestMethod.POST)
	public ResponseEntity<String> selectAddressZipCodeModifyDate(@PathVariable("entCode") String entCode, HttpServletRequest request) throws Exception {
		
		Util.setDB(request, "MASTER");
//		Util.setDB(request, entCode);

		return new ResponseEntity<String>(systemMngService.selectAddressZipCodeModifyDate(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 ZipCode 리스트 조회", notes="ZipCode 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/zipCode/search/list", method = RequestMethod.GET)
	public ResponseEntity<ZipCodeList> selectZipCodeList(@PathVariable("entCode") String entCode, @QueryStringArgResolver SearchZipCodeList search, HttpServletRequest request) throws Exception {
//		Util.setDB(request, entCode);
		Util.setDB(request, "MASTER");
		return new ResponseEntity<ZipCodeList>(systemMngService.selectZipCodeList(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 ZipCode 조회", notes="ZipCode를 조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX"),
		@ApiImplicitParam(name = "zipCode", value = "우편번호", required = true, dataType = "string", paramType = "path")
	})
	@RequestMapping(value="/zipCode/get/{zipCode}", method = RequestMethod.GET)
	public ResponseEntity<ZipCode> selectZipCode(@PathVariable("entCode") String entCode, @PathVariable("zipCode") String zipCode, SearchZipCode search, HttpServletRequest request) throws Exception {
//		Util.setDB(request, entCode);
		Util.setDB(request, "MASTER");
		return new ResponseEntity<ZipCode>(systemMngService.selectZipCode(search), HttpStatus.OK);
	}
	
	@ApiOperation(value = "전자상거래 Backup Order 조회", notes="Order 리스트를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/order/backup", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> selectOrderList(@PathVariable("entCode") String entCode, @RequestBody OdBackup odBackup, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, odBackup, "userId", null, null);
		Util.setDB(request, entCode);
		return new ResponseEntity<OrderSaveResult>(systemMngService.odBackup(odBackup), HttpStatus.OK);
	}

	@ApiOperation(value = "그리드 환경설정(그룹별) 저장", notes = "그룹별 그리드 컬럼 설정을 저장합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value = "/grid/config/group/save", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> saveGridConfig(
			@PathVariable("entCode") String entCode,
			@RequestBody Map<String, Object> param,
			HttpServletRequest request) throws Exception {

		String userId  = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"));
		String gmtTime = Util.null2str(param.get("gmtTime"), "0");
		String gridId    = Util.null2str(param.get("gridId"));
		String groupCode = Util.null2str(param.get("groupCode"));

		@SuppressWarnings("unchecked")
		List<String> titleList = (List<String>) param.get("titleList");
		@SuppressWarnings("unchecked")
		List<String> hideList  = (List<String>) param.get("hideList");
		@SuppressWarnings("unchecked")
		List<String> lockList  = (List<String>) param.get("lockList");
		@SuppressWarnings("unchecked")
		List<Object> widthList = (List<Object>) param.get("widthList");

		systemMngService.saveGridConfig(entCode, userId, gmtTime, gridId, groupCode, titleList, hideList, lockList, widthList);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "ok");
		return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
	}
	@ApiOperation(value = "그리드 환경설정 그룹 삭제", notes = "지정한 그룹의 그리드 설정을 삭제합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value = "/grid/config/group/delete", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> deleteGridConfigGroup(
			@PathVariable("entCode") String entCode,
			@RequestBody Map<String, Object> param,
			HttpServletRequest request) throws Exception {

		String userId    = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"));
		String gridId    = Util.null2str(param.get("gridId"));
		String groupCode = Util.null2str(param.get("groupCode"));

		systemMngService.deleteGridConfigGroup(entCode, userId, gridId, groupCode);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "ok");
		return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
	}
	@ApiOperation(value = "그리드 환경설정(그룹별) 조회", notes = "그룹별 그리드 컬럼 설정을 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value = "/grid/config/group", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getGridConfig(@PathVariable("entCode") String entCode,
															 HttpServletRequest request) throws Exception {

		String userId    = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"));
		String gridId    = request.getParameter("gridId");
		String groupCode = request.getParameter("groupCode");

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("data", systemMngService.getGridConfigList(entCode, userId, gridId, groupCode));
		return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
	}

	@ApiOperation(value = "그리드 환경설정 그룹 목록 조회", notes = "저장된 그룹 목록을 조회합니다.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value = "/grid/config/group/list", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> getGridConfigGroupList(@PathVariable("entCode") String entCode,
																	  HttpServletRequest request) throws Exception {

		String userId = Util.null2str(Util.getSessionUserInfo(request, jwtTokenProvider).get("USER_ID"));
		String gridId = request.getParameter("gridId");

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("data", systemMngService.getGridConfigGroupList(entCode, userId, gridId));
		return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
	}
}
