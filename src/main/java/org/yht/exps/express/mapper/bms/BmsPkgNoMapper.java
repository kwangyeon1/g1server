package org.yht.exps.express.mapper.bms;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.bms.bmsPkgNo.BmsPkgNo;
import org.yht.exps.express.model.bms.bmsPkgNo.search.SearchBmsPkgNoList;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.InPlan;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;

@Mapper
public interface BmsPkgNoMapper {

	/** 박스번호 리스트 - 반입예정번호(inPlanNo)로 조회 */
	List<BmsPkgNo> selectBmsPkgNoListByInPlanNo(SearchBmsPkgNoList search); 

	/** 박스번호 리스트 - 반입예정번호(inPlanNo)로 조회 */
	List<BmsPkgNo> selectBmsPkgNoListExistPlanByInPlanNo(SearchBmsPkgNoList search); 
	
	/** 박스번호 리스트 - 반입예정번호(OrigInPlanNo)로 조회 */
	List<BmsPkgNo> selectBmsPkgNoListExistPlanByOrigInPlanNo(SearchBmsPkgNoList search);
	
	/** 박스번호 리스트 - 반입번호(inNo)로 조회 */
	List<BmsPkgNo> selectBmsPkgNoListByInNo(SearchBmsPkgNoList search);
	
	int checkDupPkgByPackingNo(Map<String, Object> param);
	
	InPlanPkg selectInPlanWithPkgByMblWithBl(Map<String, Object> param);
	
	CarryIn selectBmsInByInNo(Map<String, Object> param);
	
	CarryIn selectOriginBlByInNo(Map<String, Object> param);
	
	CarryIn selectOriginBlByInNoClass(Map<String, Object> param);
	
	InPlan selectBmsInPlanByInPlanNo(Map<String, Object> param);
	
	List<InPlanPkg> selectPkgListByInPlanNo(Map<String, Object> param);
	
	Integer sumPkgByInPlanNo(Map<String, Object> param);
	
	int batchInsertPkgByInPlanNo(Map<String, Object> param);
	
	int insertPkg(Map<String, Object> param);
	
	int softDeleteInPlanPkgByInPlanNo(Map<String, Object> param);
	
	int softDeleteInPlanPkgByPkgNoList(Map<String, Object> param);
	
	int updatePkgNewCargoNo(Map<String, Object> param);
	
	int updateBlankNewCargoNoFromPkg(Map<String, Object> param);
	
	int insertInPlan(Map<String, Object> param);	/* IN_PLAN 생성 */
	
	Integer selectInPlanNoByCargoWithBl(Map<String, Object> param);	/* IN_PLAN_NO 조회 */
	
	int updateInPlanNo(CarryIn carryIn);
	
	int updateOrigInPlanNo(CarryIn carryIn);
	
	int selectXrayHistoryByInPlanNo(Map<String, Object> param);
	
	int selectXrayResultByInPlanNo(Map<String, Object> param);
	
	int insertXrayHistory(Map<String, Object> param);	
	
	int insertXrayResult(Map<String, Object> param);
	
	int softDeleteInPlanPkgByPkgNo(Map<String, Object> param);
	
	int updateXrayHistory(Map<String, Object> param);
	
	int updatePkg(Map<String, Object> param);
	
	int checkDupPkgByInPackingNo(Map<String, Object> param);
	
	Map<String, Object> checkDupPkgInfoByPackingNo(Map<String, Object> param);
	
	int checkBoxOutScanByPackingNo(Map<String, Object> param);
	
	int checkBoxDeliveryOutScanByPackingNo(Map<String, Object> param);
	
	int updatePantosPkgNo(Map<String, Object> param);

}
