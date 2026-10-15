package org.yht.exps.express.service.mobile.od;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.ubireport.viewer.report.preview.UbiViewer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.DeliveryMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.mobile.MobEcodMapper;
import org.yht.exps.express.model.eCommerce.delivery.UpdateDelivery;
import org.yht.exps.express.model.mobile.od.MobEcod;
import org.yht.exps.express.model.mobile.od.MobEcodCarryout;
import org.yht.exps.express.model.mobile.od.MobEcodList;
import org.yht.exps.express.model.mobile.od.MobEcodMulti;
import org.yht.exps.express.model.mobile.od.MobSaveResult;
import org.yht.exps.express.model.mobile.od.search.SearchMobEcod;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.eCommerce.DeliveryServiceVE;
import org.yht.util.DateTime;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import javax.annotation.Resource;


@Slf4j
@Service
@Transactional
public class MobEcodService {

	@Autowired
	CustomProperties customProperties;
	@Autowired
	DeliveryMapper deliveryMapper;
	@Autowired
	OrderMapper odMapper;
	@Autowired
	MobEcodMapper mobEcodMapper;
	@Autowired
    CommonMapper commonMapper;
	@Value("${spring.profiles.active}")
	private String profile;
	@Resource(name = "deliveryServiceVE")
	DeliveryServiceVE DeliveryServiceVe;
	@Autowired
	RestHighLevelClient client;
	/**
	 * Order Detail
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobEcod selectOrderDetail(SearchMobEcod search) throws Exception {
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);

		if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")){
			MobEcod order = mobEcodMapper.selectOrderDetailVe(search);
			return order;
		}else {
			MobEcod order = mobEcodMapper.selectOrderDetail(search);
			return order;
		}
	}

	/**
	 * 출고처리 Order Detail
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobEcod selectOrderDetail2(SearchMobEcod search) throws Exception {
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		MobEcod order = mobEcodMapper.selectOrderDetail2(search);

		return order;
	}

	/**
	 * Order Detail
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobEcod selectOrderDetailByList(SearchMobEcod search) throws Exception {
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		MobEcod order = mobEcodMapper.selectOrderDetail(search);

		String outSrchYn = Util.null2str(search.getOutSrchYn());
		if("N".equals(outSrchYn)) {
		order.setOutSum(mobEcodMapper.selectCarryoutCnt(search)); }

		return order;
	}

	/**
	 * 전자상거래 Order - 출고처리
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobSaveResult carryoutSave(MobEcod mobEcod) throws Exception {
		mobEcodMapper.carryoutSave(mobEcod);

		MobSaveResult mobSaveResult = new MobSaveResult();
		return mobSaveResult;
	}

	/**
	 * 전자상거래 Order - 출고처리 저장
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobSaveResult carryoutBatchSave(MobEcod mobEcod) throws Exception {

		List<MobEcodCarryout> carryoutList = mobEcod.getCarryoutList();
		if (carryoutList == null) {
			return null;
		}

		mobEcodMapper.carryoutSave(mobEcod);
		MobSaveResult mobSaveResult = new MobSaveResult();

		return mobSaveResult;
	}

	/**
	 * 전자상거래 로케이션 재고 list 조회
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobEcodList selectInventoryList(SearchMobEcod search) throws Exception {

		// Page size 제한 없애기 위하여 추가
		search.setSrchMode("excel");

		MobEcodList mobEcodList = new MobEcodList();
		if (search.getPage() != 0 && search.getPageSize() != 0) {
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		Util.accessControlDatabase(search, null, true, null, null, null, null);
		List<MobEcod> list = mobEcodMapper.selectInventoryList(search);
		mobEcodList.setList(list);
		if (list.size() > 0) {
			mobEcodList.setTotal(list.get(0).getTotal());
		} else {
			mobEcodList.setTotal(0);
		}
		return mobEcodList;
	}

	public MobEcod inventoryCheck(MobEcod mobEcod) throws Exception {
		MobEcod order = new MobEcod();
		SearchMobEcod search = new SearchMobEcod();

		Util.accessControlDatabase(mobEcod, "userLangMobile|userLanguage", false, null, null, null, null);
		MobEcod list = mobEcodMapper.selectLocationList(mobEcod);
		if(list != null) {
			order = list;
		} else {
			search.setHblNo(mobEcod.getHblNo());
			search.setUserLanguage(mobEcod.getUserLanguage());
			search.setEntCode(mobEcod.getEntCode());
			search.setDestCode(mobEcod.getDestCode());
			search.setCryptographyYn(mobEcod.getCryptographyYn());
			order  = mobEcodMapper.selectOrderDetail(search);
		}
		return order;
	}


	/**
	 * 전자상거래 Order - 재고등록
	 * @param mobEcod
	 * @return
	 * @throws Exception
	 */
	public List<MobEcod> inventorySave(MobEcod mobEcod) throws Exception {
	    List<MobEcod> result = new ArrayList<>();
	    Util.accessControlDatabase(mobEcod, "gmtTime|userLanguage", false, null, null, null, null);

	    List<String> hblNoArray = mobEcod.getHblNoArray();  // 배열 형태의 hblNoArray 값 받아오기

	    for (String hblNo : hblNoArray) {
	        mobEcod.setHblNo(hblNo);
	        mobEcodMapper.inventorySave(mobEcod);

	        SearchMobEcod search = new SearchMobEcod();
	        search.setHblNo(hblNo);
	        search.setUserLanguage(mobEcod.getUserLanguage());
	        search.setEntCode(mobEcod.getEntCode());
	        search.setCryptographyYn(mobEcod.getCryptographyYn());
	        MobEcod order = mobEcodMapper.selectOrderDetail(search);
	        result.add(order);
	    }

	    return result;
	}

	/**
	 * Order Multi list
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobEcod selectOrderDetailByMulti(SearchMobEcod search) throws Exception {
		// 오더정보
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		MobEcod order = mobEcodMapper.selectOrderDetail(search);
		if (order != null) {
			search.setOdJobNo(order.getOdJobNo());
		}
		List<MobEcodMulti> multiList = mobEcodMapper.selectOrderMulti(search);
		if (multiList != null && multiList.size() != 0) {
			order.setMultiList(multiList);
		}

		return order;
	}

	/**
	 * 전자상거래 멀티(중량) - 저장
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobSaveResult saveMultiWgList(MobEcod mobEcod) throws Exception {
		Util.accessControlDatabase(mobEcod, "gmtTime", false, null, null, null, null);

		List<MobEcodMulti> multiList = mobEcod.getMultiList();
		if (multiList == null) {
			return null;
		}

		String gubun = Util.null2str(mobEcod.getGubun());

		for (int i = 0; i < multiList.size(); i++) {
			MobEcodMulti multi = multiList.get(i);
			multi.setGmtTime(mobEcod.getGmtTime());
			multi.setRegisterUserId(mobEcod.getRegisterUserId());
			multi.setGubun(mobEcod.getGubun());

//			String seqNoId = multi.getSeqNoId();
			if (/* StringUtils.isEmpty(seqNoId) && */"CASE".equals(gubun)) {
				mobEcodMapper.deleteOrderMulti(mobEcod);
				mobEcodMapper.insertOrderMulti(multi);
			} else {
				mobEcodMapper.updateOrderMulti(multi);
			}
		}

		MobSaveResult mobSaveResult = new MobSaveResult();
		return mobSaveResult;
	}

	/**
	 * 전자상거래 멀티(중량) - 삭제
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobSaveResult deleteOrderMulti(MobEcod mobEcod) throws Exception {
		Util.accessControlDatabase(mobEcod, "userLanguage", false, null, null, null, null);
		MobSaveResult mobSaveResult = new MobSaveResult();
		String hblNo = mobEcod.getHblNo();
		if (StringUtils.isNotEmpty(hblNo)) {
			SearchMobEcod search = new SearchMobEcod();
			search.setEntCode(mobEcod.getEntCode());
			search.setHblNo(hblNo);
			search.setUserLanguage(mobEcod.getUserLanguage());
			search.setCryptographyYn(mobEcod.getCryptographyYn());
			MobEcod order = mobEcodMapper.selectOrderDetail(search);
			mobSaveResult.setMobEcod(order);
			if (order != null) {
				mobEcod.setOdJobNo(order.getOdJobNo());
			}

			List<MobEcodMulti> multiList  = mobEcodMapper.selectOrderMulti(search);
			if(multiList != null && multiList.size() != 0) {
				order.setMultiList(multiList);
			}


		}

		mobEcodMapper.deleteOrderMulti(mobEcod);
		return mobSaveResult;
	}

	/**
	 * 전자상거래 인계 멀티(중량) - 삭제
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobSaveResult updateOrderReceiptMulti(MobEcod mobEcod) throws Exception {
		Util.accessControlDatabase(mobEcod, "gmtTime", false, null, null, null, null);
		mobEcodMapper.updateOrderReceiptMulti(mobEcod);
		mobEcodMapper.saveReceiptList2(mobEcod);

		MobSaveResult mobSaveResult = new MobSaveResult();

		return mobSaveResult;
	}

	/**
	 * 전자상거래 멀티(중량) - 여러 bl건 일괄저장
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobSaveResult multiBatchSave(MobEcod mobEcod) throws Exception {
		Util.accessControlDatabase(mobEcod, "gmtTime", false, null, null, null, null);
		List<MobEcodMulti> multiList = mobEcod.getMultiList();
		if (multiList == null) {
			return null;
		}

		mobEcodMapper.multiBatchDelete(mobEcod);
		mobEcodMapper.multiBatchSave(mobEcod);

		MobSaveResult mobSaveResult = new MobSaveResult();
		return mobSaveResult;
	}

	/**
	 * 전자상거래 멀티(중량) - bl건 인계 또는 취소 처리
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobSaveResult saveReceiptList(MobEcod mobEcod) throws Exception {

		List<MobEcodMulti> multiList = mobEcod.getMultiList();
		if (multiList == null) {
			return null;
		}
		Util.accessControlDatabase(mobEcod, "gmtTime", false, null, null, null, null);
		mobEcodMapper.saveReceiptList(mobEcod);
		mobEcodMapper.saveReceiptList2(mobEcod);

		MobSaveResult mobSaveResult = new MobSaveResult();
		return mobSaveResult;
	}

	/**
	 * 전자상거래 출고처리 list 조회
	 *
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobEcodList selectOrderDetailByCarryoutList(SearchMobEcod search) {
		MobEcodList mobEcodList = new MobEcodList();
		List<MobEcod> list = mobEcodMapper.selectOrderDetailByCarryoutList(search);
		mobEcodList.setList(list);
		if (list.size() > 0) {
			mobEcodList.setTotal(list.get(0).getTotal());
		} else {
			mobEcodList.setTotal(0);
		}
		return mobEcodList;
	}

	/**
	 * 전자상거래 화물 일괄 - 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobEcod selectOrderListByMulti(SearchMobEcod search) throws Exception {
		//오더정보
		MobEcod order  = mobEcodMapper.selectMultiOrderDetail(search);
		if(order == null) {						/** 하우스번호로 조회 시, 데이터가 없을 경우 - 송장번호로 검색해서 리스트 가져온다 */
			order = new MobEcod();
			List<MobEcodMulti> multiList  = mobEcodMapper.selectMultiOrderList(search);
			if(multiList != null && multiList.size() != 0) {
				order.setHblNo(search.getHblNo());
				order.setMultiList(multiList);
			}
		}
		return order;
	}

	/**
	 * 전자상거래 화물 일괄 - 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobSaveResult saveMultiList(MobEcod mobEcod) throws Exception {

		List<MobEcodMulti> multiList = mobEcod.getMultiList();
		if(multiList == null) {
			return null;
		}
		Util.accessControlDatabase(mobEcod, "gmtTime", false, null, null, null, null);
		for (int i=0;i<multiList.size();i++) {
			MobEcodMulti multi = multiList.get(i);
			multi.setDeliveryCode(mobEcod.getDeliveryCode());
			if (Util.null2str(multi.getSeqNoId()).equals("")){	/**house 번호로 조회, house번호와 같은 송장번호가 없는경우 */
				multi.setRegisterUserId(mobEcod.getRegisterUserId());
				multi.setGmtTime(mobEcod.getGmtTime());
				mobEcodMapper.insertMultiOrderDelivery(multi);
			} else {
				mobEcodMapper.updateMultiOrderDeliveryMul(multi);
			}
		}
		MobSaveResult mobSaveResult = new MobSaveResult();
		return mobSaveResult;
	}

	/**
	 * 전자상거래 화물 건별 - 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobEcod selectCaseOrderDetail(SearchMobEcod search) throws Exception {
		//오더정보
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		int multiCount = 0;
		//int odCount = 0;

		if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")){
			//오더카운트,멀티카운트
			//오더1멀티1 -> 멀티따라감
			//오더0멀티1 -> 멀티따라감
			//오더1멀티0 -> 오더따라감
			HashMap chkMap = new HashMap();
			chkMap.put("deliveryNo",Util.null2str(search.getHblNo()));
			chkMap.put("hblNo",Util.null2str(search.getHblNo()));
			multiCount = deliveryMapper.countMultiByDelNo(chkMap);
			//odCount = deliveryMapper.countOdbyHbl(chkMap);
			log.debug("multiCount -> "+ multiCount);

			if(multiCount<1){
				MobEcod order  = mobEcodMapper.selectCaseOrderDetail(search);
				return order;
			}else {
				MobEcod order  = mobEcodMapper.selectCaseOrderDetailMultiVe(search);
				return order;
			}

		}else {
			MobEcod order  = mobEcodMapper.selectCaseOrderDetail(search);
			return order;
		}

	}

	/**
	 * 전자상거래 화물 건별 - divide value 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public String selectDivideValue( SearchMobEcod search) throws Exception {
		HashMap map = new HashMap();
		map.put("entCode", search.getEntCode());
		map.put("entBusinessCode", search.getEntBusinessCode());
		map.put("solution", search.getSolution());
		map.put("optionCode", search.getOptionCode());

		String optVal = Util.null2str(commonMapper.selectSystemOption(map));
		return StringEscapeUtils.escapeHtml4(optVal);
	}

	/**
	 * 전자상거래 화물 건별 - 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobSaveResult saveCaseList(MobEcodMulti mobEcodMulti) throws Exception {
		List<MobEcodMulti> multiList = mobEcodMulti.getMultiList();
		if(multiList == null) {
			return null;
		}
		MobSaveResult mobSaveResult = new MobSaveResult();
		log.debug("checkMob->"+ mobEcodMulti);
		Util.accessControlDatabase(mobEcodMulti, "gmtTime", false, null, null, null, null);

		if(profile.equals("hubn1")||profile.equals("local")||profile.equals("bms11")){
			String chkOd = "";//들어온 hblNo이 멀티번호인지 오더번호인지 확인
			int multiCount = 0;
			int odCount = 0;
			String singleFlag = "";//오더건인지 멀티건인지 구분하는 플레그
			if(Util.null2str(mobEcodMulti.getDeliveryCodeChange()).equals("KD")){
				//오더카운트,멀티카운트
				//오더1멀티1 -> 멀티따라감
				//오더0멀티1 -> 멀티따라감
				//오더1멀티0 -> 오더따라감
				Map<String,Object> chkMap = new HashMap();
				chkMap.put("deliveryNo",Util.null2str(mobEcodMulti.getNotChkBl()));
				chkMap.put("hblNo",Util.null2str(mobEcodMulti.getNotChkBl()));
				chkMap.put("odJobNo",Util.null2str(mobEcodMulti.getOdJobNo()));
				multiCount = deliveryMapper.countMultiByDelNo(chkMap);
				odCount = deliveryMapper.countOdbyHbl(chkMap);
				Map<String,Object>  brforeOdMap = new HashMap();
				//오더건인데 멀티0이면 새로 insert해야함
				if(odCount>0 && multiCount<1){
					log.debug("singleFlag1(오더건)");
					//오더따라가는경우
					singleFlag = "1";
					brforeOdMap = deliveryMapper.beforeOdChk(chkMap);
					
					//멀티가 없으므로 멀티부분 인서트
					MobEcodMulti multi = mobEcodMulti;
					if(Util.null2str(multi.getNotChkBl()).equals("")||multi.getNotChkBl()==null){
						multi.setNotChkBl("KDCHANGEBL");
					}
					mobEcodMapper.saveCaseOrderDelivery(multi);
				}else {
					log.debug("singleFlag2(멀티건)");
					//멀티따라가는경우
					singleFlag = "2";
					brforeOdMap = deliveryMapper.beforeOdChk(chkMap);
					
					// 배송접수 여부 확인(멀티) - 접수 완료건이면 업데이트 전에 차단
					if(deliveryMapper.countRecMulti(chkMap)>0) {
						String failMsg = "이미 배송접수가 완료된건입니다";
						mobSaveResult.setResultCode("FAIL");
						mobSaveResult.setChkContent(failMsg);
						return mobSaveResult;
					}

					//멀티부분 입력값 업데이트
					MobEcodMulti multi = mobEcodMulti;
					mobEcodMapper.updateCaseOrderDelivery(multi);
				}

				//오더 정보 조회 결과가 없으면 이후 로직 진행 불가 (NPE 방지)
				if(brforeOdMap == null){
					String failMsg = "오더 정보를 찾을 수 없습니다. (OD_JOB_NO 누락 또는 미존재)";
					mobSaveResult.setResultCode("FAIL");
					mobSaveResult.setChkContent(failMsg);
					return mobSaveResult;
				}

				//경동배송접수로직 적용
				UpdateDelivery send = new UpdateDelivery();
				send.setDeliveryNoChanged(mobEcodMulti.getNotChkBl());
				//오더따라가는경우
				if(singleFlag.equals("1")){
					send.setSeq(Util.null2str(odMapper.selectTempSeq(chkMap).get("CHK_SEQ")));
				}
				send.setRegisterUserId(mobEcodMulti.getRegisterUserId());
				send.setSrchBoundType(Util.null2str(brforeOdMap.get("BOUND_TYPE")));
				send.setSrchCustomerCode(Util.null2str(brforeOdMap.get("CUSTOMS_CODE")));
				Map<String, Object> deliveryDataMap = new HashMap<String, Object>();
				List<Map<String, Object>> deliveryListKd = new ArrayList<>();
				Map<String, Object> interfaceMap = new HashMap<String, Object>();
				interfaceMap.put("entCode", Util.null2str(mobEcodMulti.getEntCode()));
				interfaceMap.put("bound", Util.null2str(send.getSrchBoundType()));
				interfaceMap.put("srchBound", Util.null2str(send.getSrchBoundType()));
				interfaceMap.put("srchCustomerCode", Util.null2str(send.getSrchCustomerCode()));

				deliveryDataMap = DeliveryServiceVe.selectSendDeliveryList(interfaceMap, send,singleFlag);
				//현재는 대한 -> 경동케이스만 고려(앞단에서 이미 KD로 업데이트나 인서트됨)
				deliveryListKd = (List<Map<String, Object>>) deliveryDataMap.get("KD");
				if (deliveryListKd.size() > 0) {
					Map<String, Object> result = receiptProcessKd(interfaceMap, deliveryListKd);

					if (Util.null2int(result.get("failKdCnt")) > 0) {
						// 수동 롤백
						TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

						// 실패 메시지 응답
						String failMsg = Util.null2str(result.get("failKdPoNos"));
						mobSaveResult.setResultCode("FAIL");
						mobSaveResult.setChkContent(failMsg);
					} else {
						// prodCodeList에서 송장번호 추출
						List<Map<String, Object>> prodCodeList = (List<Map<String, Object>>) result.get("prodCodeList");
						List<String> hblNoArray = new ArrayList<>();
						if (prodCodeList != null && !prodCodeList.isEmpty()) {
							for (Map<String, Object> item : prodCodeList) {
								hblNoArray.add(Util.null2str(item.get("prodCode")));
							}
						}
						//리턴값 세팅
						MobEcod mobEcod = new MobEcod();
						mobEcod.setHblNoArray(hblNoArray);
						mobSaveResult.setMobEcod(mobEcod);
						mobSaveResult.setResultCode("SUCCESS");
					}

					//송장출력로직
					Map<String, Object> printParamMap = new HashMap<String, Object>();
					printParamMap.put("odJobNo",          Util.null2str(mobEcodMulti.getOdJobNo()));
					printParamMap.put("deliveryCode",     Util.null2str(mobEcodMulti.getDeliveryCodeChange()));
					printParamMap.put("entBusinessCode", Util.null2str(mobEcodMulti.getEntCode()));
					printParamMap.put("entCode",          Util.null2str(mobEcodMulti.getEntCode()));
					printParamMap.put("poDate",           Util.null2str(mobEcodMulti.getPoDate()));
					printParamMap.put("deliveryNo",           Util.null2str(mobEcodMulti.getNotChkBl()));
					printParamMap.put("singleFlag",           singleFlag);
					mobSaveResult.setPrintParamMap(printParamMap);//트랜젝션문제로 진행로직은 컨트롤러쪽에 위치시킴

				}
			}else {
				//저장시 조회한 오더의 모든 멀티(중량)화물 제거 하고   그리드 내용 저장
				mobEcodMapper.deleteCaseOrderDelivery(mobEcodMulti);

				for(int i=0;i<multiList.size();i++) {
					MobEcodMulti multi = multiList.get(i);
					multi.setGmtTime(mobEcodMulti.getGmtTime());
					multi.setRegisterUserId(mobEcodMulti.getRegisterUserId());
					multi.setGubun(mobEcodMulti.getGubun());
					mobEcodMapper.saveCaseOrderDelivery(multi);
				}
			}

		}else {
			//저장시 조회한 오더의 모든 멀티(중량)화물 제거 하고   그리드 내용 저장
			mobEcodMapper.deleteCaseOrderDelivery(mobEcodMulti);

			for(int i=0;i<multiList.size();i++) {
				MobEcodMulti multi = multiList.get(i);
				multi.setGmtTime(mobEcodMulti.getGmtTime());
				multi.setRegisterUserId(mobEcodMulti.getRegisterUserId());
				multi.setGubun(mobEcodMulti.getGubun());
//			multi.setDeliveryCode(mobEcodMulti.getDeliveryCode());

				mobEcodMapper.saveCaseOrderDelivery(multi);
			}
		}

		return mobSaveResult;
	}







	/**
	 * 모바일전용 배송요청
	 * @param interfaceMap
	 * @param deliveryListKd
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> receiptProcessKd(Map<String, Object> interfaceMap, List<Map<String, Object>> deliveryListKd) throws Exception {

		Map<String, Object> returnMap        = new HashMap<String, Object>();
		Map<String, Object> tmpMap           = new HashMap<String, Object>();
		Map<String, Object> sendMap          = new HashMap<String, Object>();
		List<Map<String, Object>> errorList  = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> sndList    = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
		String sendYn                    = "N";
		List<String> failKdPoNosList = new ArrayList<>();
		String entCode                   = (String) interfaceMap.get("entCode");
		String dirEntCode                = entCode;
		String logId  = "";
		String index  = "";
		IndexApi indexApi    = null;
		DocumentApi documentApi = null;
		int totalCnt     = 0;
		int failedCnt    = 0;
		int successedCnt = 0;
		int sendCnt      = 0;
		int oldSendCnt   = 0;
		boolean multiFlag = false;
		String chgDeliveryCode = "";//변경배송사처리저장
		List<Map<String, Object>> resultProdCodeList = new ArrayList<>();
		try {

			log.info("###### 경동택배 인터페이스 시작 ##########");
			totalCnt = deliveryListKd.size();
			int seq        = 1;
			for (Map<String, Object> deliveryMapKd : deliveryListKd) {
				multiFlag = false;
				String poNo = Util.null2str(deliveryMapKd.get("CHK_PO_NO"));
				String chkHblNo = "";
				logList.add(deliveryMapKd);

				//멀티여부체크
				Map<String,Object>chkMultMap = new HashMap<>();
				chkMultMap.put("odJobNo",deliveryMapKd.get("OD_JOB_NO"));
//				chkMultMap.put("blNo",deliveryMapKd.get("DELIVERY_NO"));
				chkMultMap.put("SEQ_NO_ID",deliveryMapKd.get("SEQ_NO_ID"));

				//해당 건 자체가 멀티행인지로 판정(오더에 멀티가 존재하는지가 아니라 접수중인 건이 멀티행인지)
				//멀티행은 SEQ_NO_ID가 1이상, 오더본체는 SEQ_NO_ID가 널/빈문자
				String seqNoIdStr = Util.null2str(deliveryMapKd.get("SEQ_NO_ID"));
				if(!seqNoIdStr.isEmpty() && Util.null2int(seqNoIdStr) >= 1){
					multiFlag = true;
					//변경배송사처리저장(없으면 변경건이 아닌걸로 판단하기위해서도 사용)
					chgDeliveryCode= odMapper.selectChgDeliveryCode(chkMultMap);
				}


				//인터페이스 정보확인
				//키1
				Map<String, Object> deliveryApiMap = new HashMap<String, Object>();
				Map<String, Object> deliveryApiAllMap = new HashMap<String, Object>();
				Map<String, Object> deliveryApiSrchMap = new HashMap<String, Object>();
				deliveryApiSrchMap.put("entCode", entCode);
				deliveryApiSrchMap.put("deliveryCode", "KD_RECV");
				deliveryApiMap = odMapper.selectDeliveryApiInfo(deliveryApiSrchMap);
				//키2
				deliveryApiSrchMap.put("srchCustomerCode", Util.null2str(deliveryMapKd.get("AGENT_CODE")));
				deliveryApiSrchMap.put("srchBound", Util.null2str(deliveryMapKd.get("BOUND")));
				deliveryApiSrchMap.put("srchLogisticsCode", Util.null2str(deliveryMapKd.get("DEST_PORT")));
				deliveryApiAllMap = odMapper.selectDeliveryApiInfoAllMapping(deliveryApiSrchMap);
				//없을경우 COMMON적용
				if(deliveryApiAllMap == null) {
					deliveryApiAllMap = odMapper.selectDeliveryApiInfoAll(deliveryApiSrchMap);//사업자번호,업체코드
				}
				//맵자체의 널포인터방지
				if (deliveryApiMap == null) {
					deliveryApiMap = new HashMap<>();
				}
				if (deliveryApiAllMap == null) {
					deliveryApiAllMap = new HashMap<>();
				}
				String authKey = Util.null2str(deliveryApiMap.get("RECV_API_PW"));

				Map<String, Object> sendData = new HashMap<String, Object>();
				sendData.put("domeOverType",     "D");                                      // 국내 고정
				sendData.put("isDealItem",       "N");                                      // 표준운임 고정
				sendData.put("prodCode",         null);                                     // 사전 운송장번호 없음
				sendData.put("prodRegNo",       multiFlag?Util.null2str(deliveryMapKd.get("CHK_PO_NO")+"_"+Util.null2str(deliveryMapKd.get("SEQ_NO_ID"))):Util.null2str(deliveryMapKd.get("CHK_PO_NO"))); // PO_NO
				sendData.put("prodName",         Util.null2str(deliveryMapKd.get("commodityItemLocConvert")));                              // 품목명
				sendData.put("prodMemo",         Util.null2str(deliveryMapKd.get("REMARKS")));    //비고                                 // 메모 없음
				sendData.put("wrapStatus",       "박스");                                   // 포장상태박스고정
				sendData.put("calcType",         "0001");                                   // 일반 고정
				sendData.put("width",            multiFlag?Util.null2dbl(deliveryMapKd.get("DIM_WIDTH_MULTI")):Util.null2dbl(deliveryMapKd.get("DIM_WIDTH")));                                 // DIM_WIDTH
				sendData.put("vertical",         multiFlag?Util.null2dbl(deliveryMapKd.get("DIM_LENGTH_MULTI")):Util.null2dbl(deliveryMapKd.get("DIM_LENGTH")));                                // DIM_LENGTH
				sendData.put("height",           multiFlag?Util.null2dbl(deliveryMapKd.get("DIM_HEIGHT_MULTI")):Util.null2dbl(deliveryMapKd.get("DIM_HEIGHT")));                                // DIM_HEIGHT
				sendData.put("weight",           multiFlag?Util.null2dbl(deliveryMapKd.get("VWT_MULTI")):Util.null2dbl(deliveryMapKd.get("VWT")));                              // GWT
//				sendData.put("price",           String.valueOf((int)(Math.ceil(Util.null2int(deliveryMapKd.get("AMOUNT_LOC")) / 10000.0)))); // 단가(올림)
				sendData.put("price",           "1"); // 단가(올림)(1로고정)
				sendData.put("count",            null);     // 수량(의미x)
				sendData.put("branSndnCode",     null);                                     // 발송영업소 미지정 (계정 기준 자동)
				sendData.put("branArvlCode",     null);                                     // 도착영업소 미지정 (우편번호 기준 자동)
				sendData.put("custSndrNo",       Util.null2str(deliveryApiMap.get("RECV_API_ID")));                  // 개발:C230000001, 운영:C230000744           // 개발:C230000001, 운영:C230000744
				sendData.put("custSndrName",     Util.null2str(deliveryApiAllMap.get("KD_NAME")));                               // 고객사명
				sendData.put("custSndrTelno",    Util.null2str(deliveryApiAllMap.get("KD_TEL1")));// 고객사 전화
				sendData.put("custSndrEtcTelno", Util.null2str(deliveryApiAllMap.get("KD_TEL2"))); // 고객사 기타연락처
				sendData.put("custSndrZip",      Util.null2str(deliveryApiAllMap.get("KD_ZIPCODE")));                            // 고객사 우편번호
				sendData.put("custSndrAddr",     Util.null2str(deliveryApiAllMap.get("KD_ADDR1")));                               // 고객사 주소
				sendData.put("custSndrDaddr",    Util.null2str(deliveryApiAllMap.get("KD_ADDR2")));                            // 고객사 상세주소
				sendData.put("custRcvrName",      Util.null2str(deliveryMapKd.get("consigneeNameLocConvert")));                             // 수하인명
				sendData.put("custRcvrTelno",    Util.null2str(deliveryMapKd.get("consigneeHpConvert")).replaceAll("[\\-/+ ]", ""));   //수하인연락처
				sendData.put("custRcvrEtcTelno", Util.null2str(deliveryMapKd.get("consigneeHpConvert")).replaceAll("[\\-/+ ]", ""));     // 수하인 기타연락처
				sendData.put("custRcvrZip",      Util.null2str(deliveryMapKd.get("consigneeZipcodeLocConvert")));                          // 수하인 우편번호
				sendData.put("custRcvrAddr",     Util.fullWidthToHalfWidth(Util.null2str(deliveryMapKd.get("consigneeAddressLocConvert"))));                             // 수하인 주소
				sendData.put("custRcvrDaddr",    Util.fullWidthToHalfWidth(Util.null2str(deliveryMapKd.get("consigneeAddressLocConvert"))));                          // 수하인 상세주소
				sendData.put("deliPay",          0);                                        // 배송운임
				sendData.put("etcPay",           100);                                        // 기타운임
				sendData.put("otherPay",         0);                                        // 별도운임
				String payTypeChk =  multiFlag?Util.null2str(deliveryMapKd.get("DELIVERY_PAYMENT_TYPE_MULTI")):Util.null2str(deliveryMapKd.get("DELIVERY_PAYMENT_TYPE"));
				if (payTypeChk.equals("01")) {
					payTypeChk = "0001";  //
				} else if (payTypeChk.equals("07")||payTypeChk.equals("02")||payTypeChk.equals("08")) {
					payTypeChk = "0002";  //
				}
				sendData.put("payType", payTypeChk);                                   // 선불0001 후불0002
				sendData.put("rcvrKakaoYn","N");                                      // 카카오 알림톡 미전송
				sndList.add(sendData);
				sendCnt++;
				log.info("########## 경동택배 전송 데이터 추가 PO_NO={} / 누적건수={} ##########", poNo, sendCnt);

				// 100건 단위 또는 마지막 건일 때 API 호출
				boolean isLast = sendCnt == deliveryListKd.size();
				if ((sendCnt > 0 && sendCnt % 100 == 0) || isLast) {
					if (sndList.isEmpty()) continue;
					log.info("################## KD API SEND : " + sendCnt + " ########################");

					String secretKey = authKey.replace("\r", "").replace("\n", "");
					String kdUrl  = Util.null2str(deliveryApiMap.get("RECV_API_URL"));

					HttpURLConnection conn = null;
					BufferedReader in   = null;
					try {
						URL url  = new URL(kdUrl);
						sendMap.put("list", sndList);
						String json = new Gson().toJson(sendMap);
						byte[] postDataBytes = json.getBytes(Charset.forName("UTF-8"));
						log.info("########## 경동택배 API 요청 BODY : " + json + " ##########");

						conn = (HttpURLConnection) url.openConnection();
						conn.setConnectTimeout(20000);
						conn.setReadTimeout(20000);
						conn.setRequestMethod("PUT");
						conn.setDoOutput(true);
						conn.setRequestProperty("Content-Type",      "application/json");
						conn.setRequestProperty("X-AUTH-SECRET-KEY", secretKey);
						conn.getOutputStream().write(postDataBytes);

						StringBuilder strBuild = new StringBuilder();
						in = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
						String inputLine;
						while ((inputLine = in.readLine()) != null) strBuild.append(inputLine);

						String response = strBuild.toString();
						log.info("########## 경동택배 API 응답 : " + response + " ##########");

						Map<String, Object> responseMap = new ObjectMapper()
								.readValue(response, new TypeReference<Map<String, Object>>() {});
						String respCode   = Util.null2str(responseMap.get("code"));
						String respResult = Util.null2str(responseMap.get("result"));

						if ("Y".equals(respResult) && "API_PRC_S000".equals(respCode)) {
							Map<String, Object> dataObj = (Map<String, Object>) responseMap.get("data");
							List<Map<String, Object>> rtnList = (List<Map<String, Object>>) dataObj.get("list");

							for (int i = 0; i < rtnList.size(); i++) {
								Map<String, Object> rtnItem = rtnList.get(i);
								String itemResult = Util.null2str(rtnItem.get("result"));
								Map<String, Object> content = (Map<String, Object>) rtnItem.get("content");
								if ("Y".equals(itemResult) && content != null) {
									//fare는 성공(content 존재)일 때만 사용하므로 null 체크 이후에 추출(실패 항목의 content null NPE 방지)
									Map<String, Object> fare = (Map<String, Object>)content.get("fare");
									String prodCode = Util.null2str(content.get("prodCode"));   // 운송장번호
									String prodRegNo = Util.null2str(content.get("prodRegNo"));  // PO_NO
									String deliPay = Util.null2str(fare.get("deliPay"));//응답배송운임
									Map<String, Object> delivery     = (Map<String, Object>) content.get("delivery");
									Map<String, Object> departBranch = (Map<String, Object>) content.get("departBranch");
									Map<String, Object> arrivalBranch= (Map<String, Object>) content.get("arrivalBranch");

									// fare 직렬화
									// /10 후 5자리 zero-padding, discPay=0이면 99999 고정
									int deliPayInt   = Integer.parseInt(Util.null2str(fare.get("deliPay")));
									int etcPay    = Integer.parseInt(Util.null2str(fare.get("etcPay")));
									int otherPay  = Integer.parseInt(Util.null2str(fare.get("otherPay")));
									int discPay   = Integer.parseInt(Util.null2str(fare.get("discPay")));
									int dosunPay  = Integer.parseInt(Util.null2str(fare.get("dosunPay")));
									String outDeliPay   = String.format("%05d", deliPayInt  / 10);
									String outEtcPay    = String.format("%05d", etcPay   / 10);
									String outOtherPay  = String.format("%05d", otherPay / 10);
									String outDiscPay   = discPay == 0 ? "99999" : String.format("%05d", discPay / 10);
									String outDosunPay  = String.format("%05d", dosunPay / 10);
									String fareStr = outDeliPay  + "-"
											+ outEtcPay   + "-"
											+ outOtherPay + "-"
											+ outDiscPay  + "-"
											+ outDosunPay;
									log.debug("ChkfareStr -> "+ fareStr);
									//

									successedCnt++;
									sendYn = "Y";
									logList.get(oldSendCnt + i).put("sendStatus", "SEND");
									//PO_NO와 응답받은 prodRegNo로 건매칭
									for (Map<String, Object> tmp : deliveryListKd) {
										// 이 건 자체가 멀티행인지로 multiFlag 재설정(배치 마지막 건 값 오염 방지)
										// 멀티행은 SEQ_NO_ID가 1이상, 오더본체는 널/빈문자
										String tmpSeqNoId = Util.null2str(tmp.get("SEQ_NO_ID"));
										multiFlag = !tmpSeqNoId.isEmpty() && Util.null2int(tmpSeqNoId) >= 1;
										// 건건이 seq + 송장번호 쌍으로 추가
										Map<String, Object> prodCodeMap = new HashMap<>();
										prodCodeMap.put("seqNoId", Util.null2str(tmp.get("SEQ_NO_ID")));
										prodCodeMap.put("odJobNo", Util.null2str(tmp.get("OD_JOB_NO")));
										prodCodeMap.put("prodCode", prodCode);
										resultProdCodeList.add(prodCodeMap);
										// 멀티여부에 따라 비교키 생성
										String cmpKey = multiFlag
												? Util.null2str(tmp.get("CHK_PO_NO")) + "_" + tmpSeqNoId
												: Util.null2str(tmp.get("CHK_PO_NO"));
										if (cmpKey.equals(prodRegNo)) {
											if(multiFlag==false) {
												// LAST POD 업데이트
												Map<String, Object> batchTmpMap = new HashMap<>();
												batchTmpMap.put("podStatus", "ECM360");
												batchTmpMap.put("noCheckLastPodStatus", "Y");
												batchTmpMap.put("odJobNo", Util.null2str(tmp.get("OD_JOB_NO")));
												odMapper.updateOrderLastPod(batchTmpMap);

												//받은 운송장번호를 hbl로 변경(변경배송사가 있는경우는 변경배송번호란에 변경)
												Map<String, Object> kdOdMap = new HashMap<>();
												kdOdMap.put("prodCode", prodCode);
												kdOdMap.put("odJobNo", Util.null2str(tmp.get("OD_JOB_NO")));
												if(!(chgDeliveryCode==null) && !chgDeliveryCode.equals(""))
												{
													//변경배송사코드 있으면 멀티의 변경배송번호 업데이트(변경배송건은 무조건멀티이므로 보통은 들어오지않을것)
													odMapper.updateChgDeliveryNo(kdOdMap);
												}else {
													//변경배송사코드 없으면 HBL업데이트
													odMapper.updateKdHbl(kdOdMap);
												}


												//경동 송장발급에 필요한 추가정보 인서트(일반오더)
												Map<String, Object> kdEtcMap = new HashMap<>();
												kdEtcMap.put("odJobNo",         Util.null2str(tmp.get("OD_JOB_NO")));
												//변경배송건은 원bl번호로저장
												kdEtcMap.put("hblNo",           (!(chgDeliveryCode==null) && !chgDeliveryCode.equals(""))?tmp.get("HBL_NO"):prodCode);  //운송장번호
												kdEtcMap.put("deliveryCode",    "KD");
												kdEtcMap.put("etc1",            delivery     != null ? Util.null2str(delivery.get("departureNosun"))   : ""); // 출발지노선
												kdEtcMap.put("etc2",            delivery     != null ? Util.null2str(delivery.get("arrivalNosun"))     : ""); // 도착지노선
												kdEtcMap.put("etc3",            arrivalBranch!= null ? Util.null2str(arrivalBranch.get("name"))        : ""); // 도착영업소이름
												kdEtcMap.put("etc4",            fareStr);                                                                      // 상세요금내역
												kdEtcMap.put("etc5",            departBranch != null ? Util.null2str(departBranch.get("name"))         : ""); // 출발영업소이름
												kdEtcMap.put("etc6",            departBranch != null ? Util.null2str(departBranch.get("telNo"))        : ""); // 출발영업소 전화번호
												kdEtcMap.put("etc7",            (departBranch != null ? Util.null2str(departBranch.get("regionName")) : "")
														+ "/"
														+ (arrivalBranch!= null ? Util.null2str(arrivalBranch.get("regionName")): "")); // 권역정보
												kdEtcMap.put("registerUserId",  "DAEMON");
												odMapper.deleteKdDeliveryInfoEtc(kdEtcMap);
												odMapper.insertKdDeliveryInfoEtc(kdEtcMap);


											}else if(multiFlag==true){
												log.debug("");
												//받은 운송장번호를 hbl로 변경(멀티전용)
												Map<String, Object> kdOdMapMulti = new HashMap<>();
												kdOdMapMulti.put("prodCode", prodCode);
												kdOdMapMulti.put("seq", Util.null2str(tmp.get("SEQ_NO_ID")));
												kdOdMapMulti.put("odJobNo", Util.null2str(tmp.get("OD_JOB_NO")));
												//kdOdMapMulti.put("chkType",deliveryMapKd.get("MULTI"));
												kdOdMapMulti.put("podDate", LocalDate.now().toString());
												//배송사가 변경된건은 무조건 멀티가 있으며 멀티의 변경배송번호만 업데이트치면됨
												if(!(chgDeliveryCode==null) && !chgDeliveryCode.equals(""))
												{
													//변경배송사코드 있으면멀티의 변경배송번호 업데이트
													odMapper.updateChgDeliveryNo(kdOdMapMulti);
												}else {
													//변경배송사코드 없으면 멀티 송장번호 업데이트
													odMapper.updateKdHblMulti(kdOdMapMulti);
												}

												//멀티건 배송상태(RETURN_DELIVERY_STATUS)를 ECM360으로 반영(SEQ_NO_ID 기준)
												Map<String, Object> multiStatusMap = new HashMap<>();
												multiStatusMap.put("odJobNo", Util.null2str(tmp.get("OD_JOB_NO")));
												multiStatusMap.put("seqNoId", Util.null2str(tmp.get("SEQ_NO_ID")));
												multiStatusMap.put("podStatus", "ECM360");
												odMapper.updateOdMultiStatus(multiStatusMap);

												//경동 송장발급에 필요한 추가정보 인서트(멀티)
												Map<String, Object> kdEtcMap = new HashMap<>();
												kdEtcMap.put("odJobNo",         Util.null2str(tmp.get("OD_JOB_NO")));
												//변경배송건은 원bl번호로저장
												kdEtcMap.put("hblNo",           (!(chgDeliveryCode==null) && !chgDeliveryCode.equals(""))?tmp.get("DELIVERY_NO"):prodCode);  //운송장번호
												kdEtcMap.put("deliveryCode",    "KD");
												kdEtcMap.put("etc1",            delivery     != null ? Util.null2str(delivery.get("departureNosun"))   : ""); // 출발지노선
												kdEtcMap.put("etc2",            delivery     != null ? Util.null2str(delivery.get("arrivalNosun"))     : ""); // 도착지노선
												kdEtcMap.put("etc3",            arrivalBranch!= null ? Util.null2str(arrivalBranch.get("name"))        : ""); // 도착영업소이름
												kdEtcMap.put("etc4",            fareStr);                                                                      // 상세요금내역
												kdEtcMap.put("etc5",            departBranch != null ? Util.null2str(departBranch.get("name"))         : ""); // 출발영업소이름
												kdEtcMap.put("etc6",            departBranch != null ? Util.null2str(departBranch.get("telNo"))        : ""); // 출발영업소 전화번호
												kdEtcMap.put("etc7",            (departBranch != null ? Util.null2str(departBranch.get("regionName")) : "")
														+ "/"
														+ (arrivalBranch!= null ? Util.null2str(arrivalBranch.get("regionName")): "")); // 권역정보
												kdEtcMap.put("registerUserId",  "DAEMON");
												odMapper.deleteKdDeliveryInfoEtc(kdEtcMap);
												odMapper.insertKdDeliveryInfoEtc(kdEtcMap);


//												log.debug("SEQ_NO_ID체크->"+Util.null2str(tmp.get("SEQ_NO_ID")));
												if(Util.null2str(tmp.get("SEQ_NO_ID")).equals("1"))
												{
													// LAST POD 업데이트
													Map<String, Object> batchTmpMap = new HashMap<>();
													batchTmpMap.put("podStatus", "ECM360");
													batchTmpMap.put("noCheckLastPodStatus", "Y");
													batchTmpMap.put("odJobNo", Util.null2str(tmp.get("OD_JOB_NO")));
													odMapper.updateOrderLastPod(batchTmpMap);

													//오더1건최신화(변경건은 원배송번호로 기록저장)
													//변경배송코드가 존재하면 원배송번호가 존재하는 건이므로 pod에 원배송번호로 반영
													String localChkHblNo = multiFlag
															? Util.null2str(tmp.get("DELIVERY_NO"))
															: Util.null2str(tmp.get("HBL_NO"));
													if(!(chgDeliveryCode==null) && !chgDeliveryCode.equals("")) {
														saveOrderPodOd( tmp, dirEntCode, localChkHblNo, "ECM360", seq++);
													}else {
														saveOrderPodOd(tmp, dirEntCode, prodCode, "ECM360", seq++);
													}

												}
											}

											//엘라스틱서치에 넣기
											Map<String, Object> esMap = new HashMap<String, Object>();
											BulkData bulkData = new BulkData();
											String esId = multiFlag
													? tmp.get("OD_JOB_NO").toString() + "_" + Util.null2str(tmp.get("SEQ_NO_ID"))
													: tmp.get("OD_JOB_NO").toString();
											// 배송상태 업데이트를 위해 ES UPDATE (DELETE -> CREATE)
											//String indexName = "ecm-delivery-tracking-rest-";
											String indexName = "ecm-delivery-tracking-rest-" + entCode.toLowerCase(); // "ecm-delivery-tracking-rest-yhtxx"

											ArrayList<BulkData> bulkDelList = new ArrayList<BulkData>();
											ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
											bulkData.setActionType(BulkData.Type.DELETE);
											bulkData.setId(esId);
											bulkData.setIndexName(indexName);
											bulkDelList.add(bulkData);
											if (bulkDelList != null && bulkDelList.size() > 0) {
												documentApi = new DocumentApi(client);
												documentApi.bulkDocumentWithBulkProcessor(bulkDelList, 10000);
											}
											bulkData = new BulkData();
											bulkData.setActionType(BulkData.Type.CREATE);
											bulkData.setIndexName(indexName);
											bulkData.setId(esId);

											//엘라스틱 데이터 매칭
											tmpMap.put("ent_code", entCode);
											tmpMap.put("multi_chk", multiFlag?"multi":"origin");
											tmpMap.put("delivery_code", Util.null2str(tmp.get("DELIVERY_CODE")));
											tmpMap.put("odjobno", Util.null2str(tmp.get("OD_JOB_NO")));
											if(!(chgDeliveryCode==null) && !chgDeliveryCode.equals("")) {
												//변경건일경우
												tmpMap.put("hblno", multiFlag ? Util.null2str(tmp.get("DELIVERY_NO")) : Util.null2str(tmp.get("HBL_NO")));
											}else {
												//변경건이 아닐경우
												tmpMap.put("hblno", prodCode);
											}
											tmpMap.put("cust_code_lv1", Util.null2str(tmp.get("AGENT_CODE")));
											tmpMap.put("chkSeq", Util.null2str(tmp.get("SEQ_NO_ID")));
											tmpMap.put("es_key", Util.null2str(tmp.get("OD_JOB_NO")+prodCode));
											tmpMap.put("deliPay", deliPay);
											tmpMap.put("po_no", Util.null2str(tmp.get("CHK_PO_NO")));
											tmpMap.put("destDeliveryNo", prodCode);
											tmpMap.put("bound_type", Util.null2str(tmp.get("BOUND_TYPE")));
											tmpMap.put("bound", Util.null2str(tmp.get("BOUND")));
											tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
											tmpMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
											tmpMap.put("register_user_id", "DAEMON");
											tmpMap.put("register_date_time", System.currentTimeMillis());
											tmpMap.put("es_job_no", bulkData.getId());
											bulkData.setJsonMap(tmpMap);
											bulkList.add(bulkData);
											// ES에 입력
											if (bulkList != null && bulkList.size() > 0) {
												// Index 확인 후 Index 생성
												indexApi = new IndexApi(client);
												if (!indexApi.existIndex(indexName)) indexApi.createIndex(indexName);

												documentApi = new DocumentApi(client);
												documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
											}

											//pod최신화
											//변경건은 원배송번호로 기록저장
											//변경배송코드가 존재하면 원배송번호가 존재하는 건이므로 pod에 원배송번호로 반영
											String localChkHblNo = multiFlag
													? Util.null2str(tmp.get("DELIVERY_NO"))
													: Util.null2str(tmp.get("HBL_NO"));
											if(!(chgDeliveryCode==null) && !chgDeliveryCode.equals("")) {
												saveOrderPod(tmp, dirEntCode, localChkHblNo, "ECM360", multiFlag, seq++);
											}else {
												saveOrderPod( tmp, dirEntCode, prodCode, "ECM360", multiFlag, seq++);
											}
											break;
										}
									}

									String rtnMsg = Util.null2str(rtnItem.get("message"));
									String invNo = (content != null) ? Util.null2str(content.get("prodRegNo")) : "";
									logList.get(oldSendCnt + i).put("checkResult", rtnMsg);
									logList.get(oldSendCnt + i).put("sendStatus", "SUCCESS");
									logList.get(oldSendCnt + i).put("CHK_PO_NO", invNo);
									log.info("########## 경동택배 배송 요청 성공! poNo >>>> " + invNo + " / rtnMsg >>>> " + rtnMsg + " ##########");

								} else {
									String rtnMsg = Util.null2str(rtnItem.get("message"));
									String invNo = (content != null) ? Util.null2str(content.get("prodRegNo")) : "";
									//lastpod반영
									for (Map<String, Object> tmp : deliveryListKd) {

										if (Util.null2str(tmp.get("CHK_PO_NO")).equals(invNo)) {
											// 이 건 자체가 멀티행인지로 multiFlag 재설정(마지막 건 값 오염 방지)
											String tmpSeqNoId = Util.null2str(tmp.get("SEQ_NO_ID"));
											multiFlag = !tmpSeqNoId.isEmpty() && Util.null2int(tmpSeqNoId) >= 1;
											// LAST POD 업데이트
//											if(multiFlag==false) {
											Map<String, Object> batchTmpMap = new HashMap<>();
											batchTmpMap.put("podStatus", "ECM410");
											batchTmpMap.put("noCheckLastPodStatus", "Y");
											batchTmpMap.put("odJobNo", Util.null2str(tmp.get("OD_JOB_NO")));
											odMapper.updateOrderLastPod(batchTmpMap);
//											}
											if(multiFlag==true && tmpSeqNoId.equals("1"))
											{
												//오더1건최신화
												saveOrderPodOd( tmp, dirEntCode, tmpSeqNoId, "ECM360", seq++);
											}
											//pod세팅
											saveOrderPod(tmp, dirEntCode, tmpSeqNoId, "ECM410", multiFlag, seq++);
											break;
										}

									}
									failKdPoNosList.add(invNo + " : " + rtnMsg);
									failedCnt++;
									logList.get(oldSendCnt + i).put("checkResult", rtnMsg);
									logList.get(oldSendCnt + i).put("sendStatus", "FAIL");
									logList.get(oldSendCnt + i).put("CHK_PO_NO", invNo);
									log.info("########## 경동택배 배송 요청 실패! poNo >>>> " + invNo + " / rtnMsg >>>> " + rtnMsg + " ##########");

								}
							}
						} else{
							String respMsg = Util.null2str(responseMap.get("message"));
							log.info("########## 경동택배 API 실패 응답 : " + respCode + " / " + respMsg + " ##########");
							for (Map<String, Object> failItem : sndList) {
								String failProdRegNo = Util.null2str(failItem.get("prodRegNo")); // PO_NO
								failKdPoNosList.add(failProdRegNo + " : " + respMsg);
							}
							failedCnt += sndList.size();
						}


					} catch (SocketTimeoutException e) {
						log.info("########## 경동택배 배송 요청 [ERROR] connect timed out ##########");
						for (Map<String, Object> failItem : sndList) {
							failKdPoNosList.add(Util.null2str(failItem.get("prodRegNo")) + e.getMessage());
						}
						failedCnt += sndList.size();
						log.error(Util.getPrintStacTraceString(e));
					} catch (Exception e) {

						for (Map<String, Object> failItem : sndList) {
							failKdPoNosList.add(Util.null2str(failItem.get("prodRegNo")) + e.getMessage());
						}
						failedCnt += sndList.size();
						log.error(Util.getPrintStacTraceString(e));
					} finally {
						if (in   != null) in.close();
						if (conn != null) conn.disconnect();
					}

					sndList    = new ArrayList<Map<String, Object>>();
					oldSendCnt = sendCnt;
				}
			}


		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		}

		// ES 로그
		for (Map<String, Object> logMap : logList) {
			tmpMap = new HashMap<String, Object>();
			logId  = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index  = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			tmpMap.clear();
			tmpMap.put("ent_code",            dirEntCode.toUpperCase());
			tmpMap.put("customer_code",       logMap.get("AGENT_CODE"));
			tmpMap.put("solution",            "ECM");
			tmpMap.put("bound",               logMap.get("BOUND"));
			tmpMap.put("bound_type",          logMap.get("BOUND_TYPE"));
			tmpMap.put("hbl_no",              logMap.get("HBL_NO"));
			tmpMap.put("if_service_code",     "KD");
			tmpMap.put("if_doc_code",         "SEND_COURIER");
			tmpMap.put("if_doc_name",         "경동 배송접수");
			tmpMap.put("if_doc_reference_no", logMap.get("P0_NO"));
			tmpMap.put("if_vender",           logMap.get("AGENT_CODE"));
			tmpMap.put("if_send_recv_type",   "S");
			tmpMap.put("if_send_recv_method", "API");
			tmpMap.put("if_file_path",        "");
			tmpMap.put("if_file_name",        "");
			tmpMap.put("if_status",           logMap.get("sendStatus"));
			tmpMap.put("if_status_info",      logMap.get("checkResult"));
			tmpMap.put("if_sender_id",        logMap.get("AGENT_CODE"));
			tmpMap.put("if_sender_name",      "");
			tmpMap.put("if_receiver_id",      "");
			tmpMap.put("if_receiver_name",    "");
			tmpMap.put("if_date",             DateTime.getDate("yyyy-MM-dd"));
			tmpMap.put("if_date_time",        DateTime.getDate("hh:mm:ss"));
			tmpMap.put("if_remark",           "KD");
			tmpMap.put("register_user_id",    logMap.get("registerUserId"));
			tmpMap.put("register_user_name",  logMap.get("registerUserName"));
			tmpMap.put("register_date_time",  System.currentTimeMillis());
			tmpMap.put("es_job_no",           logId);

			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index)) indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);
		}

		log.info("###### 경동택배 인터페이스 종료 ##########");
		String failKdPoNos = String.join("\r\n", failKdPoNosList);
		returnMap.put("sendKdYn",              sendYn);
		returnMap.put("totalKdCnt",            totalCnt);
		returnMap.put("successedKdCnt",        successedCnt);
		returnMap.put("failKdCnt",             failedCnt);
		returnMap.put("failKdPoNos",          failKdPoNos);
		returnMap.put("errorListKd",           errorList);
		returnMap.put("prodCodeList",      resultProdCodeList);
		return returnMap;
	}


	private void saveOrderPodOd(Map<String, Object> deliveryItem,
								String entCode, String blNo, String podStatus,
								int seq) throws Exception {

		String podTableName = "TB_ECM_POD_04";
		String odJobNo = Util.null2str(deliveryItem.get("OD_JOB_NO"));

		if (deliveryItem.get("ECM360_POD_JOB_NO") != null && podStatus.equals("ECM360")) {
			Map<String, Object> updateMap = new HashMap<>();
			updateMap.put("updateUserId", deliveryItem.get("registerUserId"));
			updateMap.put("podJobNo", deliveryItem.get("ECM360_POD_JOB_NO"));
			updateMap.put("podTableName", podTableName);
			updateMap.put("gmtTime", "0");
			odMapper.updateOrderPod(updateMap);  //

		} else if (deliveryItem.get("ECM410_POD_JOB") != null && podStatus.equals("ECM410")) {
			Map<String, Object> updateMap = new HashMap<>();
			updateMap.put("updateUserId", deliveryItem.get("registerUserId"));
			updateMap.put("podJobNo", deliveryItem.get("ECM410_POD_JOB_NO"));
			updateMap.put("podTableName", podTableName);
			updateMap.put("gmtTime", "0");
			odMapper.updateOrderPod(updateMap);  //

		} else {
			Map<String, Object> insertMap = new HashMap<>();
			insertMap.put("entCode", entCode.toUpperCase());
			insertMap.put("gmtTime", "0");
			insertMap.put("podStatus", podStatus);
			insertMap.put("podTableName", podTableName);
			insertMap.put("deliveryCode", "KD");
			insertMap.put("odJobNo", Util.null2str(odJobNo));
			insertMap.put("blNo", blNo);
			insertMap.put("updateUserId", deliveryItem.get("registerUserId"));
			insertMap.put("podJobNo", DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
			odMapper.insertOrderPod(insertMap);
		}
	}

	private void saveOrderPod(Map<String, Object> deliveryItem,
							  String entCode, String blNo, String podStatus,
							  boolean multiFlag,int seq) throws Exception {

		String podTableName = multiFlag ? "TB_ECM_POD_04_M" : "TB_ECM_POD_04";
		//	String odJobNo = multiFlag ? Util.null2str(deliveryItem.get("OD_JOB_NO_ORIGIN")) : Util.null2str(deliveryItem.get("OD_JOB_NO"));
		String odJobNo = Util.null2str(deliveryItem.get("OD_JOB_NO"));
		if(podTableName.equals("TB_ECM_POD_04_M")){
			if (deliveryItem.get("ECM360_POD_JOB_NO_MULTI") != null &&podStatus.equals("ECM360")) {
				// UPDATE
				Map<String, Object> updateMap = new HashMap<>();
				updateMap.put("updateUserId", deliveryItem.get("registerUserId"));
				updateMap.put("podJobNo", multiFlag?deliveryItem.get("ECM360_POD_JOB_NO_MULTI"):deliveryItem.get("ECM360_POD_JOB_NO"));
				updateMap.put("podTableName", podTableName);
				updateMap.put("gmtTime", "0");
				odMapper.updateOrderPod(updateMap);
			}
			else if (deliveryItem.get("ECM410_POD_JOB_NO_MULTI") != null &&podStatus.equals("ECM410")) {
				// UPDATE
				Map<String, Object> updateMap = new HashMap<>();
				updateMap.put("updateUserId", deliveryItem.get("registerUserId"));
				updateMap.put("podJobNo",  multiFlag?deliveryItem.get("ECM410_POD_JOB_NO_MULTI"):deliveryItem.get("ECM410_POD_JOB_NO"));
				updateMap.put("podTableName", podTableName);
				updateMap.put("gmtTime", "0");
				odMapper.updateOrderPod(updateMap);
			} else {
				// INSERT
				Map<String, Object> insertMap = new HashMap<>();
				insertMap.put("entCode", entCode.toUpperCase());
				insertMap.put("gmtTime", "0");
				insertMap.put("podStatus", podStatus);
				insertMap.put("podTableName", podTableName);
				insertMap.put("deliveryCode", "KD");
				insertMap.put("odJobNo", Util.null2str(odJobNo));
				insertMap.put("updateUserId", "DAEMON");
				insertMap.put("blNo", blNo);
				insertMap.put("updateUserId", deliveryItem.get("registerUserId"));
				insertMap.put("podJobNo", DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
				odMapper.insertOrderPod(insertMap);
			}
		}else if(podTableName.equals("TB_ECM_POD_04")){
			if (deliveryItem.get("ECM360_POD_JOB_NO") != null &&podStatus.equals("ECM360")) {
				// UPDATE
				Map<String, Object> updateMap = new HashMap<>();
				updateMap.put("updateUserId", deliveryItem.get("registerUserId"));
				updateMap.put("podJobNo", multiFlag?deliveryItem.get("ECM360_POD_JOB_NO_MULTI"):deliveryItem.get("ECM360_POD_JOB_NO"));
				updateMap.put("podTableName", podTableName);
				updateMap.put("gmtTime", "0");
				odMapper.updateOrderPod(updateMap);
			}
			else if (deliveryItem.get("ECM410_POD_JOB_NO") != null &&podStatus.equals("ECM410")) {
				// UPDATE
				Map<String, Object> updateMap = new HashMap<>();
				updateMap.put("updateUserId", deliveryItem.get("registerUserId"));
				updateMap.put("podJobNo",  multiFlag?deliveryItem.get("ECM410_POD_JOB_NO_MULTI"):deliveryItem.get("ECM410_POD_JOB_NO"));
				updateMap.put("podTableName", podTableName);
				updateMap.put("gmtTime", "0");
				odMapper.updateOrderPod(updateMap);
			} else {
				// INSERT
				Map<String, Object> insertMap = new HashMap<>();
				insertMap.put("entCode", entCode.toUpperCase());
				insertMap.put("gmtTime", "0");
				insertMap.put("podStatus", podStatus);
				insertMap.put("podTableName", podTableName);
				insertMap.put("deliveryCode", "KD");
				insertMap.put("odJobNo", Util.null2str(odJobNo));
				insertMap.put("blNo", blNo);
				insertMap.put("updateUserId", deliveryItem.get("registerUserId"));
				insertMap.put("podJobNo", DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
				odMapper.insertOrderPod(insertMap);
			}
		}
	}



	/**
	 * 출력물 저장
	 * @param paramMap
	 */
	public void savePrintInfo(Map<String, Object> paramMap,String chkPrintPoint) {
		//LocalDate today = LocalDate.now();
		//String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		// 송장 파일 생성 (UBI -> PDF)
		Map<String, Object> ubiParams = new HashMap<String, Object>();
		ubiParams.put("ubi_arg",
				"odJobNo#" + Util.null2str(paramMap.get("odJobNo")) +
						"#userId#" + Util.null2str(paramMap.get("userId")) +
						"#srchEntCode#" + Util.null2str(paramMap.get("entCode")) +
						"#odJobNoArr#" + Util.null2str(paramMap.get("odJobNo")) +
						"#srchDeliveryNo#" + Util.null2str(paramMap.get("deliveryNo")) +
						"#odJobNoUnion#" + Util.null2str(paramMap.get("odJobNo")) + "#"
		);
		ubiParams.put("report_FileDir", "ecm");
		if(paramMap.get("singleFlag").equals("1")){
			ubiParams.put("report_FileName", "ECM-KD-001");
		}else {
			ubiParams.put("report_FileName", "ECM-KD-001_MULTY_SINGLE");
		}
		ubiParams.put("odJobNo", Util.null2str(paramMap.get("odJobNo")));
		ubiParams.put("entCode", Util.null2str(paramMap.get("entCode")));
		ubiParams.put("deliveryCode", "kd");
		ubiParams.put("poDate", Util.null2str(paramMap.get("poDate")));
		getReportResultFileKD(ubiParams,chkPrintPoint);
	}


	/**
	 * UBI 출력물 생성 및 프린트출력
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public boolean getReportResultFileKD(Map<String,Object> params,String chkPrintPoint){
		Map<String,Object> result = new HashMap<String,Object>();
		String jrf_dir = customProperties.getReportJrfTopLoc(); 			// 리포트 파일 위치.
		//개발용
		//jrf_dir = "C:/EXPS/Eclipseworkspace/g1express/src/main/webapp/ubi4/work/ecm";

		log.info("reportFileDir========================"+Util.null2str(params.get("report_FileDir")));

		String entCode = Util.null2str(params.get("entCode"));
		String jrfFileName = Util.null2str(params.get("report_FileName")) + ".jrf";												// 리포트 파일명.
		//String ds	= "dataSource_yhtxx"; // 테스트
		//String ds = "jdbc/exps_yhtxx"; //  운영
		String ds = customProperties.getReportDs() + entCode.toLowerCase();														// WAS에 설정된 DataSource 명. ubl/UbiService/ ubiservice.xml
		String file_url = customProperties.getReportUrl();																		// 리포트에서 사용되는 이미지 또는 공통 아이템 정보를 가져오기위한 정보.
		String ubi_serverUrl = customProperties.getReportDomain() 	+ "/UbiServer";												// 리포트에서 사용되는 이미지 또는 공통 아이템 정보를 가져오기위한 정보.
		String servlet_url1 = customProperties.getReportDomain() 	+ "/UbiForm";												//"/UbiForm";							// Form 서블릿 URL.
		String servlet_url2 = customProperties.getReportDomain() 	+ "/UbiData";												//"/UbiData";							// Data 서블릿 URL.
		String args = Util.null2str(params.get("ubi_arg"));					    												// 아규먼트 정보.
		String odJobNo = Util.null2str(params.get("odJobNo"));
		String poDate = Util.null2str(params.get("poDate"));

		String datePath = "/"+poDate.substring(0,4)+"/"+poDate.substring(5,7)+"/"+poDate.substring(8,10)+"/";
		String export_dir = customProperties.getLabelDir()+Util.null2str(params.get("deliveryCode")).toLowerCase()+datePath;
		String export_file = Util.null2str(params.get("odJobNo")) + "_label.pdf";
		boolean isSuccess = false;

		log.info("FileUtil.isExists========================"+FileUtil.isExists(export_dir));
		if(!FileUtil.isExists(export_dir)){
			FileUtil.createDirectorys(export_dir);
		}

		try {
			UbiViewer ubiviewer = new UbiViewer(false, false);	// 객체 생성

			log.info("file_url========================"+file_url);
			log.info("ubiServerURL===================="+ubi_serverUrl);
			log.info("jrf_dir========================="+jrf_dir);
			log.info("jrfFileName====================="+jrfFileName);

			//테스트
			//args = "odJobNo#COD250616-00000006#userId#201309-001#srchEntCode#YHTXX#odJobNoArr#COD250616-00000006#checkType##odJobNoUnion#COD250616-00000006#";
			log.info("args============================"+args);
			log.info("ds=============================="+ds);
			log.info("export_dir======================"+export_dir);
			log.info("export_file====================="+export_file);

			ubiviewer.fileURL 		= file_url;
			ubiviewer.ubiServerURL 	= ubi_serverUrl;
			ubiviewer.isLocalFile	= true;
			ubiviewer.jrfFileDir 	= jrf_dir;
			ubiviewer.jrfFileName 	= jrfFileName;
			ubiviewer.arg 			= args;
			ubiviewer.dataSource 	= ds;
			ubiviewer.setExportParams("PDF", export_dir + export_file);
			ubiviewer.exectype 		= "TYPE6";
			ubiviewer.fontRevision 	= true;
			ubiviewer.setFontPath(customProperties.getReportFontLoc());		//WAS가 설치된 서버의 OS가 Non-Windows인 경우 폰트 정보를 가져오는 부분을 설정.
			ubiviewer.isDebug 		= true;
			log.info("[" + odJobNo + "] loadReport Start");
			isSuccess = ubiviewer.loadReport();
			log.info("[" + odJobNo + "] loadReport End / isSuccess: " + isSuccess);
		}catch(Exception e) {
			e.printStackTrace();
			isSuccess = false;
		}
		
		//지정된아이피로 출력하기
		Map<String,Object>chkMap = new HashMap<>();
		chkMap.put("code",chkPrintPoint);
		chkMap.put("entCode",params.get("entCode"));
		Map<String,Object>chkCodeResult = odMapper.selectPrint(chkMap);
		if (isSuccess&&chkCodeResult!=null) {

			String printerIp = Util.null2str(chkCodeResult.get("ETC2"));
			int printerPort = Util.null2int(chkCodeResult.get("ETC1"));
			String pdfFilePath = export_dir + export_file; // 생성된 PDF 경로

			try {
				// PDF 파일 읽기
				File pdfFile = new File(pdfFilePath);
				if (!pdfFile.exists()) {
					log.error("PDF 파일이 존재하지 않습니다: " + pdfFilePath);
				} else {
					byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());

					// 프린터로 전송 (연결 타임아웃 5초, 자원 자동 해제)
					try (Socket socket = new Socket()) {
						socket.connect(new InetSocketAddress(printerIp, printerPort), 5000); // 연결 타임아웃 5s
						socket.setSoTimeout(10000); // 전송 타임아웃 10s
						try (OutputStream os = socket.getOutputStream()) {
							os.write(pdfBytes);
							os.flush();
						}
					}

					log.info("PDF 출력 성공: " + pdfFilePath);
				}
			} catch (Exception e) {
				log.error("PDF 출력 실패: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return isSuccess;
	}

}
