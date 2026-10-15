package org.yht.exps.express.service.bms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.reactive.function.client.WebClient;


@Slf4j
@Service
@Transactional
public class CargoProgressInfoService {
    //private WebClient webClient;

    /**
     * 입항정보를 이용한 반입정보 업데이트
     * @param cargo
     * @param solution
     * @return
     */
//    public Map<String, Object> checkCargoProgressIn(Map<String, Object> cargo, String solution, boolean isUnloading) {
//        Map<String, Object> updateDatas = null;
//
//        try {
//            SearchCargoProgressInfo searchCargoProgressInfo = new SearchCargoProgressInfo();
//            searchCargoProgressInfo.setSrchCargoReferenceNo(Util.null2str(cargo.get("CARGO_REFERENCE_NO")));
//            searchCargoProgressInfo.setSrchBlYear(Util.null2str(cargo.get("BL_YEAR")));
//            CargoProgressInfo cargoProgressInfo = this.getCargoProgressInfo(searchCargoProgressInfo);
//
//            List<CargoProgressInfo.CargCsclPrgsInfo> crgCsclPrgsInfoList = cargoProgressInfo.getCrgCsclPrgsInfoList();
//
//            if (crgCsclPrgsInfoList != null) {
//                CargoProgressInfo.CargCsclPrgsInfo cargCsclPrgsInfoDTO = crgCsclPrgsInfoList.get(0);
//
//                int prgsTotalPcs = Util.null2int(cargCsclPrgsInfoDTO.getPckGcnt());
//                int prgsPcs = 0; //수입화물진행정보 반출신고 수량
//
//                updateDatas = new HashMap<String, Object>();
//                updateDatas.put("jobNo", cargo.get("JOB_NO"));
//                updateDatas.put("entCode", cargo.get("ENT_CODE"));
//                updateDatas.put("entBusinessCode", cargo.get("ENT_BUSINESS_CODE"));
//                updateDatas.put("entOfficeCode", cargo.get("ENT_OFFICE_CODE"));
//                updateDatas.put("solution", solution);
//                updateDatas.put("customsReportNo", cargo.get("CUSTOMS_REPORT_NO"));
//                updateDatas.put("declarationJobNo", cargo.get("DECLARATION_JOB_NO"));
//
//                // 관세청상태
//                updateDatas.put("customsCargoStatus", cargCsclPrgsInfoDTO.getPrgsStts());
//                // 통관진행상태
//                updateDatas.put("clearanceStatus", cargCsclPrgsInfoDTO.getCsclPrgsStts());
//                // 관세청체크종료여부
//                updateDatas.put("customsCheckedYn", "N");
//
//                List<CargoProgressInfo.CargCsclPrgsInfoDtail> cargCsclPrgsInfoDtailList = cargoProgressInfo.getCargCsclPrgsInfoDtails();
//                if (cargCsclPrgsInfoDtailList != null && cargCsclPrgsInfoDtailList.size() > 0) {
//                    for (CargoProgressInfo.CargCsclPrgsInfoDtail detail : cargCsclPrgsInfoDtailList) {
//
//                        // 입항반입
//                        if ("반입신고".equals(Util.null2str(detail.getCargTrcnRelaBsopTpcd()).replaceAll("\\s", "")) &&
//                                "입항반입".equals(Util.null2str(detail.getRlbrCn()).replaceAll("\\s", ""))) {
//                            // 입항지반입일시
//                            updateDatas.put("entryWhInDateTime", detail.getPrcsDttm());
//                            // 입항반입지코드
//                            updateDatas.put("entryWhCode", Util.null2str(detail.getShedSgn()).split("/")[0]);
//                            // 입항반입지명
//                            updateDatas.put("entryWhName", detail.getShedNm());
//                        }
//
//                        // 검사대상물품여부 체크
//                        if (!"".equals(Util.null2str(cargo.get("CUSTOMS_REPORT_NO"))) &&
//                                "N".equals(Util.null2str(cargo.get("INSPECT_RECV_YN"))) &&
//                                Util.null2str(detail.getCargTrcnRelaBsopTpcd()).replaceAll("\\s", "").startsWith("보세운송검사대상선별")) {
//                            // 검사대상통보
//                            updateDatas.put("inspectRecvUpdate", "Y");
//                        }
//
//                        // 통관
//                        if ("통관목록심사완료".equals(Util.null2str(detail.getCargTrcnRelaBsopTpcd()).replaceAll("\\s", ""))) {
//                            // 통관수리완료일시
//                            updateDatas.put("clearanceDateTime", detail.getPrcsDttm());
//                        }
//
//                        // 보세운송반입
//                        if ("반입신고".equals(Util.null2str(detail.getCargTrcnRelaBsopTpcd()).replaceAll("\\s", "")) &&
//                                "보세운송반입".equals(Util.null2str(detail.getRlbrCn()).replaceAll("\\s", "")) &&
//                                Util.null2str(cargo.get("DEST_CODE")).equals(Util.null2str(detail.getShedSgn()).split("/")[0])) {
//                            // 도착지반입일시
//                            updateDatas.put("destWhInDateTime", detail.getPrcsDttm());
//                        }
//
//                        if ("반출신고".equals(Util.null2str(detail.getCargTrcnRelaBsopTpcd()).replaceAll("\\s", ""))) {
//                            if (Util.null2str(cargo.get("IN_APPROVAL_NO")).equals(detail.getRlbrBssNo()) &&
//                                    "보세운송반출".equals(Util.null2str(detail.getRlbrCn()).replaceAll("\\s", ""))) {
//                                // 이전반출지코드
//                                updateDatas.put("departureLocCode", Util.null2str(detail.getShedSgn()).split("/")[0]);
//                                // 이전반출지명
//                                updateDatas.put("departureLocName", detail.getShedNm());
//                                // 이전반출지반출일시
//                                updateDatas.put("departureDateTime", detail.getPrcsDttm());
//                            }
//
//                            // 분할출고인 경우 확인을 위해 반출신고 수량 체크
//                            if ("수입신고수리후반출".equals(Util.null2str(detail.getRlbrCn()).replaceAll("\\s", "")))
//                                prgsPcs += 	Util.null2int(detail.getPckGcnt());
//                        }
//
//                        // 하기건일 경우 상차지, 반입창고 코드를 다시 검색한다.
//                        if (isUnloading) {
//                            // 입항반입
//                            if ("입항적재화물목록제출".equals(Util.null2str(detail.getCargTrcnRelaBsopTpcd()).replaceAll("\\s", ""))) {
//                                // 입항지반입일시
//                                updateDatas.put("entryWhInDateTime", detail.getPrcsDttm());
//                                // 입항반입지코드
//                                updateDatas.put("entryWhCode", Util.null2str(detail.getShedSgn()).split("/")[0]);
//                                // 입항반입지명
//                                updateDatas.put("entryWhName", detail.getShedNm());
//                            }
//                        }
//                    }
//
//                    cargCsclPrgsInfoDtailList = null;
//                }
//
//                // 반출 총수량 = 반출신고수량, 반출완료(처리상태), 수입신고수리(통관상태) 상태이면 완료
//                String prgsStts = Util.null2str(cargCsclPrgsInfoDTO.getPrgsStts()).replaceAll("\\s", "");
//                String csclPrgsStts = Util.null2str(cargCsclPrgsInfoDTO.getCsclPrgsStts()).replaceAll("\\s", "");
//                if (prgsPcs == prgsTotalPcs &&
//                        ("반출완료".equals(prgsStts) && "수입신고수리".equals(csclPrgsStts)) ||
//                        ("반출완료".equals(prgsStts) && "수입(사용소비)심사진행".equals(csclPrgsStts)) ||
//                        ("통관목록심사완료".equals(prgsStts) && "통관목록심사완료".equals(csclPrgsStts))
//                ) {
//                    updateDatas.put("customsCheckedYn", "Y");
//                }
//
//                cargCsclPrgsInfoDTO = null;
//                crgCsclPrgsInfoList = null;
//            }
//
//            cargoProgressInfo = null;
//
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//
//        return updateDatas;
//    }


    /**
     * 수입화물진행정보 가져오기
     * @param searchCargoProgressInfo
     * @throws Exception
     */
//    private CargoProgressInfo getCargoProgressInfo(SearchCargoProgressInfo searchCargoProgressInfo) throws Exception {
//        CargoProgressInfo cargoProgressInfo = null;
//
//        String unipassBaseUrl = "unipass.customs.go.kr";
//        int unipassPort = 38010;
//        String unipassCargoProgressPath = "/ext/rest/cargCsclPrgsInfoQry/retrieveCargCsclPrgsInfo";
//        String unipassCargoProgressKey = "cargMtNo";
//        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
//                .scheme("https")
//                .host(unipassBaseUrl)
//                .port(unipassPort)
//                .path(unipassCargoProgressPath)
//                .queryParam("crkyCn", URLDecoder.decode(unipassCargoProgressKey, "UTF-8"));
//
//        String cargMtNo = Util.null2str(searchCargoProgressInfo.getSrchCargoReferenceNo());	// 화물관리번호
//        String mblNo = Util.null2str(searchCargoProgressInfo.getSrchMblNo());				// MBL No
//        String hblNo = Util.null2str(searchCargoProgressInfo.getSrchHblNo());				// HBL No
//        String blYy = Util.null2str(searchCargoProgressInfo.getSrchBlYear());				// BL 연도
//
//        if (!"".equals(cargMtNo))	uriComponentsBuilder.queryParam("cargMtNo", cargMtNo);
//        if (!"".equals(mblNo))		uriComponentsBuilder.queryParam("mblNo", mblNo);
//        if (!"".equals(hblNo))		uriComponentsBuilder.queryParam("hblNo", hblNo);
//        if (!"".equals(blYy))		uriComponentsBuilder.queryParam("blYy", blYy);
//
//
//        URI uri = uriComponentsBuilder.build().encode().toUri();
//        String xml = webClient
//                .get()
//                .uri(uri)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//        JAXBContext jaxbContext = JAXBContext.newInstance(CargoProgressInfo.class);
//        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//        cargoProgressInfo = (CargoProgressInfo) jaxbUnmarshaller.unmarshal(new StringReader(xml));
//
//        log.debug("수입화물진행정보 API uri: {}", uri);
//        log.debug("수입화물진행정보: {}", xml);
//
//        return cargoProgressInfo;
//    }
}
