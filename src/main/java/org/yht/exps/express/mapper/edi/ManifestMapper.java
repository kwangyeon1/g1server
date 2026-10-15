package org.yht.exps.express.mapper.edi;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderPodUpdate;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.edi.CarryinInHouse;
import org.yht.exps.express.model.edi.CarryoutInHouse;
import org.yht.exps.express.model.edi.CustomsManifestRes;
import org.yht.exps.express.model.edi.ManifestAggregate;
import org.yht.exps.express.model.edi.ManifestCorrect;
import org.yht.exps.express.model.edi.ManifestCorrectDelete;
import org.yht.exps.express.model.edi.ManifestCorrectMaster;
import org.yht.exps.express.model.edi.ManifestCorrectRecvResult;
import org.yht.exps.express.model.edi.ManifestInHouse;
import org.yht.exps.express.model.edi.ManifestInMaster;
import org.yht.exps.express.model.edi.ManifestOrder;
import org.yht.exps.express.model.edi.ManifestOutHouse;
import org.yht.exps.express.model.edi.ManifestOutMaster;
import org.yht.exps.express.model.edi.SendDelivery;
import org.yht.exps.express.model.edi.SendManifest;
import org.yht.exps.express.model.edi.search.SearchCarryoutInList;
import org.yht.exps.express.model.edi.search.SearchManifest;
import org.yht.exps.express.model.edi.search.SearchManifestCorrectList;
import org.yht.exps.express.model.edi.search.SearchManifestCustoms;
import org.yht.exps.express.model.edi.search.SearchManifestInList;
import org.yht.exps.express.model.edi.search.SearchManifestOutList;

@Mapper
public interface ManifestMapper {
	
	List<ManifestInMaster> selectMasterInList(SearchManifestInList search);
	
	List<ManifestInHouse> selectHouseInList(SearchManifestInList search);
	
	List<CarryoutInHouse> selectCarryoutInList(SearchOrderList search);
	List<CarryinInHouse> selectCarryinInList (SearchCarryoutInList search);
	List<ManifestInHouse> selectManifestInList(SearchManifestInList search);
	
	List<ManifestOutMaster> selectMasterOutList(SearchManifestOutList search);
	
	List<ManifestOutHouse> selectHouseOutList(SearchManifestOutList search);
	List<CustomsManifestRes> customsManifestResList(SearchManifest search);
	
	ManifestOrder selectOrder(SearchManifest search);
	Map<String, Object> selectDeleteMasterOrderCheck(SendManifest sendManifest);
	void deleteMasterOrder(Map<String, Object> map);
	
	void updateOrder(Order order);
	void updateSimpleDocNo(Order order);
	void updateModUpdateExcel(ManifestCorrect manifestCorrect);
	
	/** 적하목록신고 */
	Map<String, Object> getEdiMblOdInfo(Map<String, Object> map);
	Map<String, Object> getFwSendAccInfo(Map<String, Object> map);
	Map<String, Object> getEdiInterfaceInfo(Map<String, Object> map);
	Map<String, Object> ediManifestCustomsLicenseInfo(Map<String, Object> map);
	List<Map<String, Object>> ediManifestCustomsLicenseList(Map<String, Object> map);
	List<Map<String, Object>> ediManifestCustomsLicenseCheckListWithGroupBy(Map<String, Object> map);
	List<Map<String, Object>> ediManifestCustomsLicenseCheckList(Map<String, Object> map);
	List<Map<String, Object>> ediManifestCustomsHsList(Map<String, Object> map);
	List<Map<String, Object>> convertAmsAddressList(SendManifest sendManifest);
	List<Map<String, Object>> selectSendEmsOrder(SendManifest sendManifest);
	Map<String, Object> selectEmsInfo(SendManifest sendManifest);
	void updateAmsAddress(Map<String, Object> map);
	void updateOdForEdiStatus(Map<String, Object> map);
	void updateMrnResultType(Map<String, Object> map);
	void updateCarryinNo(Order order);
	void updateComObl(Order order);
	void updateComOrder(Order order);
	void updateComOrderBus(Order order);
	Map<String, Object> ediManifestValidatorMblOdInfo(Map<String, Object> map);
	List<Map<String, Object>> ediManifestValidatorHblOdInfo(Map<String, Object> map);
	List<Map<String, Object>> ediManifestContainerList(Map<String, Object> map);
	Map<String, Object> getEdiMfModifyValidatorValidatorMblOdInfo(Map<String, Object> map);
	List<Map<String, Object>> getEdiMfModifyValidatorHblOdList(Map<String, Object> map);
	List<Map<String, Object>> getEdiMfModifyMblModDeclareNoList(Map<String, Object> map);
	List<Map<String, Object>> getEdiMfModifyHblSingleList(Map<String, Object> map);
	List<Map<String, Object>> getEdiMfModifyHblLicenseList(Map<String, Object> map);
	List<Map<String, Object>> getEdiMfModifyHblCtnrReasonList(Map<String, Object> map);
	void setModifyMblAfterSendUpdate(Map<String, Object> map);
	
	/** 관우회전송 */
	Map<String, Object> selectGwSendMasterOrder(Map<String, Object> map);
	List<Map<String, Object>> selectGwSendHouseOrderList(Map<String, Object> map);
	Map<String, Object> selectKctdiInfo(Map<String, Object> map);
	List<Map<String, Object>> selectResetSendManifestGw(SendManifest sendManifest);
	void deleteResetSendManifestGw(SendManifest sendManifest);
	void updateResetSendManifestGw(Map<String, Object> map);
	void updateManifestGwSend(Map<String, Object> map);
	List<Map<String, Object>> selectInspectionSeqNo(Map<String, Object> map);
	void updateInspectionSeqNo(Map<String, Object> map);
	
	/** 통관목록신고 */
	void updateCustomsManifest(SendManifest sendManifest);
	void updateCustomsManifestExp(SendManifest sendManifest);
	void updateSimpleErrorDescNull(SendManifest sendManifest);
	void deleteResetSendCustomsManifest(SendManifest sendManifest);
	void updateResetSendCustomsManifest(Map<String, Object> map);
	void updateResetSendCustomsManifest2(Map<String, Object> map);
	List<Map<String, Object>> selectResetSendCustomsManifest(SendManifest sendManifest);
	Map<String, Object> selectCustomsManifestMaster(SendManifest sendManifest);
	List<Map<String, Object>> selectCustomsManifestHouse(SendManifest sendManifest);
	List<Map<String, Object>> selectCustomsManifestHouseItem(SendManifest sendManifest);
	List<Map<String, Object>> selectCustomsManifestItem(SendManifest sendManifest);
	
	/** 적하목록 정정 */
	List<ManifestCorrectMaster> selectCorrectMasterList(SearchManifestCorrectList search);	
	List<ManifestCorrect> selectCorrectHouseList(SearchManifestCorrectList search);
	List<ManifestCorrect> selectCorrectContainerList(SearchManifestCorrectList search);	
	ManifestCorrectMaster getSerialNo(SearchManifestCorrectList search);
	List<ManifestCorrectMaster> selectPopupCorrectMasterList(SearchManifestCorrectList search);
	List<ManifestCorrectRecvResult> selectManifestCorrectRecvResultList(SearchManifestCorrectList search);
	Map<String, Object> selectLastMblInfo(Map<String, Object> map);
	Map<String, Object> selectModMblInfo(ManifestCorrect manifestCorrect);
	String selectOrderCorrectionContents(Map<String, Object> map);
	void deleteModResult(ManifestCorrect manifestCorrect);
	void updateModSendReset(Map<String, Object> map);
	void updateMfModifyInfo(Map<String, Object> map);
	
	
	void insertMblCorrect(ManifestCorrect manifestCorrect);
	void insertHblCorrect(ManifestCorrect manifestCorrect);
	void updateMblCorrect(ManifestCorrect manifestCorrect);
	void updateHblCorrect(ManifestCorrect manifestCorrect);
	void insertContainerCorrect(ManifestCorrect manifestCorrect);
	void updateModReason(ManifestCorrect manifestCorrect);
	void updateModMbl(ManifestCorrect manifestCorrect);
	void updateModContainerInfo(ManifestCorrect manifestCorrect);
	void updateOrderContainerInfo(ManifestCorrect manifestCorrect);
	Map<String, Object> getIsHbl(ManifestCorrect manifestCorrect);
	Integer getSeqNoId(ManifestCorrect manifestCorrect);
	void updateOdByCorrect(ManifestCorrect manifestCorrect);
	void updateOdByCorrect1(ManifestCorrect manifestCorrect);
	void updateContainerByCorrect(Map<String, Object> map);
	void insertOrderContainer(ManifestCorrect manifestCorrect);
	void deleteOrderContainer(ManifestCorrect manifestCorrect);
	Map<String, Object> selectOrderContainer(ManifestCorrect manifestCorrect);
	Map<String, Object> selectModCntrDupChk(Map<String, Object> map);
	int deleteCorrectMblCheck(ManifestCorrectDelete manifestCorrectDelete);
	void deleteAllCorrect(ManifestCorrectDelete manifestCorrectDelete);
	int deleteCorrect(ManifestCorrectDelete manifestCorrectDelete);
	List<String> selectMasterOdJobNoList(SearchManifestInList search);
	void updateBusinessFinish(SendDelivery sendDelivery);
	List<Map<String, Object>> selectEmsTargetList(Map<String, Object> map);
	Map<String, Object> selectEmsOblCheck(Map<String, Object> map);
	String selectEmsHblNoMax(Map<String, Object> map);
	Map<String, Object> selectExchangeRateType(Map<String, Object> map);	
	void updateEmsObl(Map<String, Object> map);
	void insertEmsObl(Map<String, Object> map);
	void insertEmsOd(Map<String, Object> map);
	void insertEmsOdBus(Map<String, Object> map);
	void insertEmsOdChg(Map<String, Object> map);
	void insertEmsSbl(Map<String, Object> map);
	Integer getIsModContainerData(ManifestCorrect manifestCorrect);
	Integer getIsModData(ManifestCorrect manifestCorrect);
	
	void insertCopyMblCorrect(ManifestCorrect manifestCorrect);
	void insertCopyHblCorrect(ManifestCorrect manifestCorrect);
	void insertCopyContainerCorrect(ManifestCorrect manifestCorrect);
	
	Map<String, Object> selectSimpleSendChk(Map<String, Object> search);
	Map<String, Object> selectHblValidate(Map<String, Object> search);
	void updateSimpleError(SearchOrderList search);
	List<Map<String, Object>> selectSimpleError(SearchOrderList search);
	List<Map<String, Object>> selectResetSimpleDocNoList(SendManifest sendManifest);
	Map<String, Object> selectModHblValidate(Map<String, Object> search);
	int selectBmsInPlanInsertCheck(SendManifest sendManifest);
	List<Map<String, Object>> selectBmsInPlanInsertTarget(SendManifest sendManifest);
	void updateBmsInPlanYn(Map<String, Object> map);
	void insertBmsInPlan(Map<String, Object> map);
	int selectBmsInPlanDeleteCheck(SendManifest sendManifest);
	List<Map<String, Object>> selectBmsInPlanDeleteTarget(SendManifest sendManifest);
	void deleteBmsInPlan(Map<String, Object> map);
	
	Map<String, Object> getCustomsCode(SearchManifestCustoms searchCustoms);
	
	List<Map<String, Object>> selectCorrectOrderList(ManifestCorrect manifestCorrect);
	void updateCorrectOrder(Map<String, Object> map);
	
	/** UPS 엑셀 다운로드 & 업로드 */
	List<Map<String, Object>> selectChkConsigneeList(SearchManifestInList search);
	Map<String, Object> selectExcelUploadTrackingChk(Map<String, Object> map);
	void updateExcelUploadTracking(Map<String, Object> map);
	void updateExcelUploadUpsAd(Map<String, Object> map);
	void updateExcelUploadUps(Map<String, Object> map);
	
	/** 개인통관오류건 제출번호 분리 */
	Map<String,Object> getSeparationInfo(SendManifest sendManifest);
	List<Map<String,String>> getSeparationList(SendManifest sendManifest);
	int customsErrSeparation(SendManifest sendManifest);
	int selectReHsnInfo(SendManifest sendManifest);
	/* 정정요청에 남아있는 하우스 수 */
	int deleteMblCorrect(ManifestCorrect manifestCorrect);

	List<Map<String, String>> selectSparationSimpleDocNoList(SendManifest sendManifest);
	int updateSimpleDocNoList(SendManifest sendManifest);
	int updateMrnResultTypeManual(SendManifest sendManifest);

	void batchUpdatePodStatus(OrderPodUpdate odPodUpdate);
	void updateClearanceStatus(OrderPodUpdate odPodUpdate);
	void batchDeletePod(Map<String, Object> searchMap);
	void batchInsertPod(Map<String, Object> searchMap);
	
	Map<String, Object> selectClearanceDate(String odJobNo);
	void updateHblInfo(Map<String, Object> updateMap);
	

	void updateCustomsManifestCancel(SendManifest updateSendManifest);
	void updateSimpleCancelErrorDescNull(SendManifest updateSendManifest);
	Map<String, Object> selectCustomsManifestCancelMaster(SendManifest sendManifest);
	List<Map<String, Object>> selectCustomsManifestCancelHouseItem(SendManifest sendManifest);
	void updateResetSendCustomsManifestInfo(Map<String, Object> tmpMap);
	void deleteResetSendCustomsManifestCancel(SendManifest sendManifest);
	List<Map<String, Object>> selectResetSendCustomsManifestCancel(SendManifest sendManifest);
	void updateResetSendCustomsManifestCancelInfo(Map<String, Object> tmpMap);
	void deleteDropHist(SendManifest sendManifest);
	
	/** 허브넷 쿠팡트래킹 Insert*/
	void batchInsertCoupangTrackingPod(Map<String, Object> searchMap);
	
	List<ManifestInMaster> selectInsertEms(SearchManifestInList search);
	Map<String, Object> selectEcmYnCheck(SearchOrderList searchOrderList);
	List<Map<String, Object>> selectConsigneeMatchingList(Map<String, Object> srchMap);
	Map<String, Object> selectConsigneeMatchingInfo(Map<String, Object> hblMap);

	void updateCustomsIdInfo(Map<String, Object> updateMap);

	List<Map<String, Object>> selectBlacklistList(SearchManifestInList search);
	List<Map<String, Object>> selectExpEmailList(SendManifest sendManifest);
	List<ManifestAggregate> selectManifestAggregateList(SearchManifestInList search);

    List<Map<String, Object>> selectHubnInPlanInsertTarget(SendManifest sendManifest);
    void updateHubnBmsInInfo(Map<String, Object> paramMap);
    List<Map<String, Object>> selectOrderContainerList(Map<String, Object> paramMap);
    List<Map<String, Object>> selectHubnOdHoldList(Map<String, Object> validMap);
}
